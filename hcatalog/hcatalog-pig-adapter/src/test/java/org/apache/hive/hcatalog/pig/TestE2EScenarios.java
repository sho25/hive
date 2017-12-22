begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|pig
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|FileUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|cli
operator|.
name|CliSessionState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|conf
operator|.
name|HiveConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|CommandNeedRetryException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|DriverFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|IDriver
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|session
operator|.
name|SessionState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|WritableComparable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|InputSplit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|Job
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|JobID
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|OutputCommitter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|RecordReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|RecordWriter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|TaskAttemptContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|TaskAttemptID
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|HcatTestUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|HCatRecord
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatInputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatOutputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|OutputJobInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatMapRedUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|ExecType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|PigServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|data
operator|.
name|Tuple
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestE2EScenarios
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TEST_DATA_DIR
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
name|TestE2EScenarios
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
operator|+
literal|"-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_WAREHOUSE_DIR
init|=
name|TEST_DATA_DIR
operator|+
literal|"/warehouse"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEXTFILE_LOCN
init|=
name|TEST_DATA_DIR
operator|+
literal|"/textfile"
decl_stmt|;
specifier|private
specifier|static
name|IDriver
name|driver
decl_stmt|;
specifier|protected
name|String
name|storageFormat
parameter_list|()
block|{
return|return
literal|"orc"
return|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|TEST_WAREHOUSE_DIR
argument_list|)
decl_stmt|;
if|if
condition|(
name|f
operator|.
name|exists
argument_list|()
condition|)
block|{
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
operator|(
operator|new
name|File
argument_list|(
name|TEST_WAREHOUSE_DIR
argument_list|)
operator|.
name|mkdirs
argument_list|()
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Could not create "
operator|+
name|TEST_WAREHOUSE_DIR
argument_list|)
throw|;
block|}
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
operator|.
name|varname
argument_list|,
name|TEST_WAREHOUSE_DIR
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|)
expr_stmt|;
name|driver
operator|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|dropTable
argument_list|(
literal|"inpy"
argument_list|)
expr_stmt|;
name|dropTable
argument_list|(
literal|"rc5318"
argument_list|)
expr_stmt|;
name|dropTable
argument_list|(
literal|"orc5318"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|FileUtils
operator|.
name|deleteDirectory
argument_list|(
operator|new
name|File
argument_list|(
name|TEST_DATA_DIR
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|dropTable
parameter_list|(
name|String
name|tablename
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"drop table "
operator|+
name|tablename
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createTable
parameter_list|(
name|String
name|tablename
parameter_list|,
name|String
name|schema
parameter_list|,
name|String
name|partitionedBy
parameter_list|,
name|String
name|storageFormat
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|AbstractHCatLoaderTest
operator|.
name|createTable
argument_list|(
name|tablename
argument_list|,
name|schema
argument_list|,
name|partitionedBy
argument_list|,
name|driver
argument_list|,
name|storageFormat
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|driverRun
parameter_list|(
name|String
name|cmd
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|int
name|retCode
init|=
name|driver
operator|.
name|run
argument_list|(
name|cmd
argument_list|)
operator|.
name|getResponseCode
argument_list|()
decl_stmt|;
if|if
condition|(
name|retCode
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to run ["
operator|+
name|cmd
operator|+
literal|"], return code from hive driver : ["
operator|+
name|retCode
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|pigDump
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|PigServer
name|server
init|=
operator|new
name|PigServer
argument_list|(
name|ExecType
operator|.
name|LOCAL
argument_list|)
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"==="
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|tableName
operator|+
literal|":"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"X = load '"
operator|+
name|tableName
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatLoader();"
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Tuple
argument_list|>
name|XIter
init|=
name|server
operator|.
name|openIterator
argument_list|(
literal|"X"
argument_list|)
decl_stmt|;
while|while
condition|(
name|XIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Tuple
name|t
init|=
name|XIter
operator|.
name|next
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|o
range|:
name|t
operator|.
name|getAll
argument_list|()
control|)
block|{
name|System
operator|.
name|err
operator|.
name|print
argument_list|(
literal|"\t("
operator|+
name|o
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|":"
operator|+
name|o
operator|.
name|toString
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"==="
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|copyTable
parameter_list|(
name|String
name|in
parameter_list|,
name|String
name|out
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Job
name|ijob
init|=
operator|new
name|Job
argument_list|()
decl_stmt|;
name|Job
name|ojob
init|=
operator|new
name|Job
argument_list|()
decl_stmt|;
name|HCatInputFormat
name|inpy
init|=
operator|new
name|HCatInputFormat
argument_list|()
decl_stmt|;
name|inpy
operator|.
name|setInput
argument_list|(
name|ijob
argument_list|,
literal|null
argument_list|,
name|in
argument_list|)
expr_stmt|;
name|HCatOutputFormat
name|oupy
init|=
operator|new
name|HCatOutputFormat
argument_list|()
decl_stmt|;
name|oupy
operator|.
name|setOutput
argument_list|(
name|ojob
argument_list|,
name|OutputJobInfo
operator|.
name|create
argument_list|(
literal|null
argument_list|,
name|out
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test HCatContext
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"HCatContext INSTANCE is present : "
operator|+
name|HCatContext
operator|.
name|INSTANCE
operator|.
name|getConf
argument_list|()
operator|.
name|isPresent
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|HCatContext
operator|.
name|INSTANCE
operator|.
name|getConf
argument_list|()
operator|.
name|isPresent
argument_list|()
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"HCatContext tinyint->int promotion says "
operator|+
name|HCatContext
operator|.
name|INSTANCE
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getBoolean
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DATA_TINY_SMALL_INT_PROMOTION
argument_list|,
name|HCatConstants
operator|.
name|HCAT_DATA_TINY_SMALL_INT_PROMOTION_DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HCatSchema
name|tableSchema
init|=
name|inpy
operator|.
name|getTableSchema
argument_list|(
name|ijob
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Copying from ["
operator|+
name|in
operator|+
literal|"] to ["
operator|+
name|out
operator|+
literal|"] with schema : "
operator|+
name|tableSchema
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|oupy
operator|.
name|setSchema
argument_list|(
name|ojob
argument_list|,
name|tableSchema
argument_list|)
expr_stmt|;
name|oupy
operator|.
name|checkOutputSpecs
argument_list|(
name|ojob
argument_list|)
expr_stmt|;
name|OutputCommitter
name|oc
init|=
name|oupy
operator|.
name|getOutputCommitter
argument_list|(
name|createTaskAttemptContext
argument_list|(
name|ojob
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|oc
operator|.
name|setupJob
argument_list|(
name|ojob
argument_list|)
expr_stmt|;
for|for
control|(
name|InputSplit
name|split
range|:
name|inpy
operator|.
name|getSplits
argument_list|(
name|ijob
argument_list|)
control|)
block|{
name|TaskAttemptContext
name|rtaskContext
init|=
name|createTaskAttemptContext
argument_list|(
name|ijob
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|TaskAttemptContext
name|wtaskContext
init|=
name|createTaskAttemptContext
argument_list|(
name|ojob
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|RecordReader
argument_list|<
name|WritableComparable
argument_list|,
name|HCatRecord
argument_list|>
name|rr
init|=
name|inpy
operator|.
name|createRecordReader
argument_list|(
name|split
argument_list|,
name|rtaskContext
argument_list|)
decl_stmt|;
name|rr
operator|.
name|initialize
argument_list|(
name|split
argument_list|,
name|rtaskContext
argument_list|)
expr_stmt|;
name|OutputCommitter
name|taskOc
init|=
name|oupy
operator|.
name|getOutputCommitter
argument_list|(
name|wtaskContext
argument_list|)
decl_stmt|;
name|taskOc
operator|.
name|setupTask
argument_list|(
name|wtaskContext
argument_list|)
expr_stmt|;
name|RecordWriter
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|HCatRecord
argument_list|>
name|rw
init|=
name|oupy
operator|.
name|getRecordWriter
argument_list|(
name|wtaskContext
argument_list|)
decl_stmt|;
while|while
condition|(
name|rr
operator|.
name|nextKeyValue
argument_list|()
condition|)
block|{
name|rw
operator|.
name|write
argument_list|(
name|rr
operator|.
name|getCurrentKey
argument_list|()
argument_list|,
name|rr
operator|.
name|getCurrentValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rw
operator|.
name|close
argument_list|(
name|wtaskContext
argument_list|)
expr_stmt|;
name|taskOc
operator|.
name|commitTask
argument_list|(
name|wtaskContext
argument_list|)
expr_stmt|;
name|rr
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|oc
operator|.
name|commitJob
argument_list|(
name|ojob
argument_list|)
expr_stmt|;
block|}
specifier|private
name|TaskAttemptContext
name|createTaskAttemptContext
parameter_list|(
name|Configuration
name|tconf
parameter_list|)
block|{
name|Configuration
name|conf
init|=
operator|(
name|tconf
operator|==
literal|null
operator|)
condition|?
operator|(
operator|new
name|Configuration
argument_list|()
operator|)
else|:
name|tconf
decl_stmt|;
name|TaskAttemptID
name|taskId
init|=
name|HCatMapRedUtil
operator|.
name|createTaskAttemptID
argument_list|(
operator|new
name|JobID
argument_list|(
literal|"200908190029"
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"mapred.task.partition"
argument_list|,
name|taskId
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"mapred.task.id"
argument_list|,
name|taskId
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|TaskAttemptContext
name|rtaskContext
init|=
name|HCatMapRedUtil
operator|.
name|createTaskAttemptContext
argument_list|(
name|conf
argument_list|,
name|taskId
argument_list|)
decl_stmt|;
return|return
name|rtaskContext
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadOrcAndRCFromPig
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|tableSchema
init|=
literal|"ti tinyint, si smallint,i int, bi bigint, f float, d double, b boolean"
decl_stmt|;
name|HcatTestUtils
operator|.
name|createTestDataFile
argument_list|(
name|TEXTFILE_LOCN
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-3\0019001\00186400\0014294967297\00134.532\0012184239842983489.1231231234\001true"
block|,
literal|"0\0010\0010\0010\0010\0010\001false"
block|}
argument_list|)
expr_stmt|;
comment|// write this out to a file, and import it into hive
name|createTable
argument_list|(
literal|"inpy"
argument_list|,
name|tableSchema
argument_list|,
literal|null
argument_list|,
literal|"textfile"
argument_list|)
expr_stmt|;
name|createTable
argument_list|(
literal|"rc5318"
argument_list|,
name|tableSchema
argument_list|,
literal|null
argument_list|,
literal|"rcfile"
argument_list|)
expr_stmt|;
name|createTable
argument_list|(
literal|"orc5318"
argument_list|,
name|tableSchema
argument_list|,
literal|null
argument_list|,
literal|"orc"
argument_list|)
expr_stmt|;
name|driverRun
argument_list|(
literal|"LOAD DATA LOCAL INPATH '"
operator|+
name|TEXTFILE_LOCN
operator|+
literal|"' OVERWRITE INTO TABLE inpy"
argument_list|)
expr_stmt|;
comment|// write it out from hive to an rcfile table, and to an orc table
comment|//driverRun("insert overwrite table rc5318 select * from inpy");
name|copyTable
argument_list|(
literal|"inpy"
argument_list|,
literal|"rc5318"
argument_list|)
expr_stmt|;
comment|//driverRun("insert overwrite table orc5318 select * from inpy");
name|copyTable
argument_list|(
literal|"inpy"
argument_list|,
literal|"orc5318"
argument_list|)
expr_stmt|;
name|pigDump
argument_list|(
literal|"inpy"
argument_list|)
expr_stmt|;
name|pigDump
argument_list|(
literal|"rc5318"
argument_list|)
expr_stmt|;
name|pigDump
argument_list|(
literal|"orc5318"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

