begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

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
name|ArrayList
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
name|metastore
operator|.
name|MetaStoreUtils
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
name|Driver
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
name|LongWritable
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
name|NullWritable
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
name|Text
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
name|Mapper
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
name|lib
operator|.
name|input
operator|.
name|TextInputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
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
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|DefaultHCatRecord
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatFieldSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
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
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestSequenceFileReadWrite
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
literal|"user.dir"
argument_list|)
operator|+
literal|"/build/test/data/"
operator|+
name|TestSequenceFileReadWrite
operator|.
name|class
operator|.
name|getCanonicalName
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
name|INPUT_FILE_NAME
init|=
name|TEST_DATA_DIR
operator|+
literal|"/input.data"
decl_stmt|;
specifier|private
specifier|static
name|Driver
name|driver
decl_stmt|;
specifier|private
specifier|static
name|PigServer
name|server
decl_stmt|;
specifier|private
specifier|static
name|String
index|[]
name|input
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|public
name|void
name|Initialize
parameter_list|()
throws|throws
name|Exception
block|{
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
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
name|driver
operator|=
operator|new
name|Driver
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
operator|new
name|File
argument_list|(
name|TEST_WAREHOUSE_DIR
argument_list|)
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|int
name|numRows
init|=
literal|3
decl_stmt|;
name|input
operator|=
operator|new
name|String
index|[
name|numRows
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numRows
condition|;
name|i
operator|++
control|)
block|{
name|String
name|col1
init|=
literal|"a"
operator|+
name|i
decl_stmt|;
name|String
name|col2
init|=
literal|"b"
operator|+
name|i
decl_stmt|;
name|input
index|[
name|i
index|]
operator|=
name|i
operator|+
literal|","
operator|+
name|col1
operator|+
literal|","
operator|+
name|col2
expr_stmt|;
block|}
name|HcatTestUtils
operator|.
name|createTestDataFile
argument_list|(
name|INPUT_FILE_NAME
argument_list|,
name|input
argument_list|)
expr_stmt|;
name|server
operator|=
operator|new
name|PigServer
argument_list|(
name|ExecType
operator|.
name|LOCAL
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSequenceTableWriteRead
parameter_list|()
throws|throws
name|Exception
block|{
name|Initialize
argument_list|()
expr_stmt|;
name|String
name|createTable
init|=
literal|"CREATE TABLE demo_table(a0 int, a1 String, a2 String) STORED AS SEQUENCEFILE"
decl_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop table demo_table"
argument_list|)
expr_stmt|;
name|int
name|retCode1
init|=
name|driver
operator|.
name|run
argument_list|(
name|createTable
argument_list|)
operator|.
name|getResponseCode
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|retCode1
operator|==
literal|0
argument_list|)
expr_stmt|;
name|server
operator|.
name|setBatchOn
argument_list|()
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"A = load '"
operator|+
name|INPUT_FILE_NAME
operator|+
literal|"' using PigStorage(',') as (a0:int,a1:chararray,a2:chararray);"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"store A into 'demo_table' using org.apache.hcatalog.pig.HCatStorer();"
argument_list|)
expr_stmt|;
name|server
operator|.
name|executeBatch
argument_list|()
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"B = load 'demo_table' using org.apache.hcatalog.pig.HCatLoader();"
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
literal|"B"
argument_list|)
decl_stmt|;
name|int
name|numTuplesRead
init|=
literal|0
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
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|t
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|""
operator|+
name|numTuplesRead
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|"a"
operator|+
name|numTuplesRead
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|"b"
operator|+
name|numTuplesRead
argument_list|)
expr_stmt|;
name|numTuplesRead
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|input
operator|.
name|length
argument_list|,
name|numTuplesRead
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTextTableWriteRead
parameter_list|()
throws|throws
name|Exception
block|{
name|Initialize
argument_list|()
expr_stmt|;
name|String
name|createTable
init|=
literal|"CREATE TABLE demo_table_1(a0 int, a1 String, a2 String) STORED AS TEXTFILE"
decl_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop table demo_table_1"
argument_list|)
expr_stmt|;
name|int
name|retCode1
init|=
name|driver
operator|.
name|run
argument_list|(
name|createTable
argument_list|)
operator|.
name|getResponseCode
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|retCode1
operator|==
literal|0
argument_list|)
expr_stmt|;
name|server
operator|.
name|setBatchOn
argument_list|()
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"A = load '"
operator|+
name|INPUT_FILE_NAME
operator|+
literal|"' using PigStorage(',') as (a0:int,a1:chararray,a2:chararray);"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"store A into 'demo_table_1' using org.apache.hcatalog.pig.HCatStorer();"
argument_list|)
expr_stmt|;
name|server
operator|.
name|executeBatch
argument_list|()
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"B = load 'demo_table_1' using org.apache.hcatalog.pig.HCatLoader();"
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
literal|"B"
argument_list|)
decl_stmt|;
name|int
name|numTuplesRead
init|=
literal|0
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
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|t
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|""
operator|+
name|numTuplesRead
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|"a"
operator|+
name|numTuplesRead
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|"b"
operator|+
name|numTuplesRead
argument_list|)
expr_stmt|;
name|numTuplesRead
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|input
operator|.
name|length
argument_list|,
name|numTuplesRead
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSequenceTableWriteReadMR
parameter_list|()
throws|throws
name|Exception
block|{
name|Initialize
argument_list|()
expr_stmt|;
name|String
name|createTable
init|=
literal|"CREATE TABLE demo_table_2(a0 int, a1 String, a2 String) STORED AS SEQUENCEFILE"
decl_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop table demo_table_2"
argument_list|)
expr_stmt|;
name|int
name|retCode1
init|=
name|driver
operator|.
name|run
argument_list|(
name|createTable
argument_list|)
operator|.
name|getResponseCode
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|retCode1
operator|==
literal|0
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_HIVE_CONF
argument_list|,
name|HCatUtil
operator|.
name|serialize
argument_list|(
name|hiveConf
operator|.
name|getAllProperties
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|,
literal|"Write-hcat-seq-table"
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|TestSequenceFileReadWrite
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|Map
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputKeyClass
argument_list|(
name|NullWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputValueClass
argument_list|(
name|DefaultHCatRecord
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|TextInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|TextInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
name|INPUT_FILE_NAME
argument_list|)
expr_stmt|;
name|HCatOutputFormat
operator|.
name|setOutput
argument_list|(
name|job
argument_list|,
name|OutputJobInfo
operator|.
name|create
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
literal|"demo_table_2"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|HCatOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|HCatOutputFormat
operator|.
name|setSchema
argument_list|(
name|job
argument_list|,
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
operator|new
name|FileOutputCommitterContainer
argument_list|(
name|job
argument_list|,
literal|null
argument_list|)
operator|.
name|cleanupJob
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|isSuccessful
argument_list|()
argument_list|)
expr_stmt|;
name|server
operator|.
name|setBatchOn
argument_list|()
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"C = load 'default.demo_table_2' using org.apache.hcatalog.pig.HCatLoader();"
argument_list|)
expr_stmt|;
name|server
operator|.
name|executeBatch
argument_list|()
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
literal|"C"
argument_list|)
decl_stmt|;
name|int
name|numTuplesRead
init|=
literal|0
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
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|t
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|""
operator|+
name|numTuplesRead
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|"a"
operator|+
name|numTuplesRead
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|"b"
operator|+
name|numTuplesRead
argument_list|)
expr_stmt|;
name|numTuplesRead
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|input
operator|.
name|length
argument_list|,
name|numTuplesRead
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTextTableWriteReadMR
parameter_list|()
throws|throws
name|Exception
block|{
name|Initialize
argument_list|()
expr_stmt|;
name|String
name|createTable
init|=
literal|"CREATE TABLE demo_table_3(a0 int, a1 String, a2 String) STORED AS TEXTFILE"
decl_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop table demo_table_3"
argument_list|)
expr_stmt|;
name|int
name|retCode1
init|=
name|driver
operator|.
name|run
argument_list|(
name|createTable
argument_list|)
operator|.
name|getResponseCode
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|retCode1
operator|==
literal|0
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_HIVE_CONF
argument_list|,
name|HCatUtil
operator|.
name|serialize
argument_list|(
name|hiveConf
operator|.
name|getAllProperties
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|,
literal|"Write-hcat-text-table"
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|TestSequenceFileReadWrite
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|Map
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputKeyClass
argument_list|(
name|NullWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputValueClass
argument_list|(
name|DefaultHCatRecord
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|TextInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|TextInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
name|INPUT_FILE_NAME
argument_list|)
expr_stmt|;
name|HCatOutputFormat
operator|.
name|setOutput
argument_list|(
name|job
argument_list|,
name|OutputJobInfo
operator|.
name|create
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
literal|"demo_table_3"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|HCatOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|HCatOutputFormat
operator|.
name|setSchema
argument_list|(
name|job
argument_list|,
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
operator|new
name|FileOutputCommitterContainer
argument_list|(
name|job
argument_list|,
literal|null
argument_list|)
operator|.
name|cleanupJob
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|job
operator|.
name|isSuccessful
argument_list|()
argument_list|)
expr_stmt|;
name|server
operator|.
name|setBatchOn
argument_list|()
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"D = load 'default.demo_table_3' using org.apache.hcatalog.pig.HCatLoader();"
argument_list|)
expr_stmt|;
name|server
operator|.
name|executeBatch
argument_list|()
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
literal|"D"
argument_list|)
decl_stmt|;
name|int
name|numTuplesRead
init|=
literal|0
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
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|t
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|""
operator|+
name|numTuplesRead
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|"a"
operator|+
name|numTuplesRead
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|"b"
operator|+
name|numTuplesRead
argument_list|)
expr_stmt|;
name|numTuplesRead
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|input
operator|.
name|length
argument_list|,
name|numTuplesRead
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|Map
extends|extends
name|Mapper
argument_list|<
name|LongWritable
argument_list|,
name|Text
argument_list|,
name|NullWritable
argument_list|,
name|DefaultHCatRecord
argument_list|>
block|{
specifier|public
name|void
name|map
parameter_list|(
name|LongWritable
name|key
parameter_list|,
name|Text
name|value
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
index|[]
name|cols
init|=
name|value
operator|.
name|toString
argument_list|()
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|DefaultHCatRecord
name|record
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|record
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|Integer
operator|.
name|parseInt
argument_list|(
name|cols
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|record
operator|.
name|set
argument_list|(
literal|1
argument_list|,
name|cols
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|record
operator|.
name|set
argument_list|(
literal|2
argument_list|,
name|cols
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|context
operator|.
name|write
argument_list|(
name|NullWritable
operator|.
name|get
argument_list|()
argument_list|,
name|record
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|HCatSchema
name|getSchema
parameter_list|()
throws|throws
name|HCatException
block|{
name|HCatSchema
name|schema
init|=
operator|new
name|HCatSchema
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|HCatFieldSchema
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
name|schema
operator|.
name|append
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
literal|"a0"
argument_list|,
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|INT
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|schema
operator|.
name|append
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
literal|"a1"
argument_list|,
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|STRING
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|schema
operator|.
name|append
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
literal|"a2"
argument_list|,
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|STRING
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|schema
return|;
block|}
block|}
end_class

end_unit

