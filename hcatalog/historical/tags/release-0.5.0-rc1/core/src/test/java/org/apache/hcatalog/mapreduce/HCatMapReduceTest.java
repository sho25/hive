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
name|hcatalog
operator|.
name|mapreduce
package|;
end_package

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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|FSDataOutputStream
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
name|FileSystem
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
name|LocalFileSystem
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
name|Path
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|api
operator|.
name|SerDeInfo
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
name|api
operator|.
name|StorageDescriptor
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
name|api
operator|.
name|Table
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
name|io
operator|.
name|RCFileInputFormat
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
name|io
operator|.
name|RCFileOutputFormat
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
name|serde
operator|.
name|serdeConstants
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
name|serde2
operator|.
name|columnar
operator|.
name|ColumnarSerDe
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
name|BytesWritable
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
name|JobStatus
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
name|hadoop
operator|.
name|mapreduce
operator|.
name|lib
operator|.
name|output
operator|.
name|TextOutputFormat
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
name|HCatRecord
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
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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

begin_comment
comment|/**  * Test for HCatOutputFormat. Writes a partition using HCatOutputFormat and reads  * it back using HCatInputFormat, checks the column values and counts.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HCatMapReduceTest
extends|extends
name|HCatBaseTest
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HCatMapReduceTest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|String
name|dbName
init|=
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
decl_stmt|;
specifier|protected
specifier|static
name|String
name|tableName
init|=
literal|"testHCatMapReduceTable"
decl_stmt|;
specifier|protected
name|String
name|inputFormat
init|=
name|RCFileInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|protected
name|String
name|outputFormat
init|=
name|RCFileOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|protected
name|String
name|serdeClass
init|=
name|ColumnarSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|List
argument_list|<
name|HCatRecord
argument_list|>
name|writeRecords
init|=
operator|new
name|ArrayList
argument_list|<
name|HCatRecord
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|List
argument_list|<
name|HCatRecord
argument_list|>
name|readRecords
init|=
operator|new
name|ArrayList
argument_list|<
name|HCatRecord
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
specifier|abstract
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getPartitionKeys
parameter_list|()
function_decl|;
specifier|protected
specifier|abstract
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getTableColumns
parameter_list|()
function_decl|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpOneTime
parameter_list|()
throws|throws
name|Exception
block|{
name|fs
operator|=
operator|new
name|LocalFileSystem
argument_list|()
expr_stmt|;
name|fs
operator|.
name|initialize
argument_list|(
name|fs
operator|.
name|getWorkingDirectory
argument_list|()
operator|.
name|toUri
argument_list|()
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
expr_stmt|;
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|hiveConf
operator|.
name|setInt
argument_list|(
name|HCatConstants
operator|.
name|HCAT_HIVE_CLIENT_EXPIRY_TIME
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// Hack to initialize cache with 0 expiry time causing it to return a new hive client every time
comment|// Otherwise the cache doesn't play well with the second test method with the client gets closed() in the
comment|// tearDown() of the previous test
name|HCatUtil
operator|.
name|getHiveClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|MapCreate
operator|.
name|writeCount
operator|=
literal|0
expr_stmt|;
name|MapRead
operator|.
name|readCount
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|deleteTable
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|String
name|databaseName
init|=
operator|(
name|dbName
operator|==
literal|null
operator|)
condition|?
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
else|:
name|dbName
decl_stmt|;
name|client
operator|.
name|dropTable
argument_list|(
name|databaseName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
annotation|@
name|Before
specifier|public
name|void
name|createTable
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|databaseName
init|=
operator|(
name|dbName
operator|==
literal|null
operator|)
condition|?
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
else|:
name|dbName
decl_stmt|;
try|try
block|{
name|client
operator|.
name|dropTable
argument_list|(
name|databaseName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{         }
comment|//can fail with NoSuchObjectException
name|Table
name|tbl
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|tbl
operator|.
name|setDbName
argument_list|(
name|databaseName
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setTableType
argument_list|(
literal|"MANAGED_TABLE"
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|sd
operator|.
name|setCols
argument_list|(
name|getTableColumns
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setPartitionKeys
argument_list|(
name|getPartitionKeys
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSd
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setBucketCols
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setSerdeInfo
argument_list|(
operator|new
name|SerDeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setName
argument_list|(
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setParameters
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setSerializationLib
argument_list|(
name|serdeClass
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setInputFormat
argument_list|(
name|inputFormat
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setOutputFormat
argument_list|(
name|outputFormat
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableParams
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|tbl
operator|.
name|setParameters
argument_list|(
name|tableParams
argument_list|)
expr_stmt|;
name|client
operator|.
name|createTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
block|}
comment|//Create test input file with specified number of rows
specifier|private
name|void
name|createInputFile
parameter_list|(
name|Path
name|path
parameter_list|,
name|int
name|rowCount
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|path
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|FSDataOutputStream
name|os
init|=
name|fs
operator|.
name|create
argument_list|(
name|path
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rowCount
condition|;
name|i
operator|++
control|)
block|{
name|os
operator|.
name|writeChars
argument_list|(
name|i
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
name|os
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|MapCreate
extends|extends
name|Mapper
argument_list|<
name|LongWritable
argument_list|,
name|Text
argument_list|,
name|BytesWritable
argument_list|,
name|HCatRecord
argument_list|>
block|{
specifier|static
name|int
name|writeCount
init|=
literal|0
decl_stmt|;
comment|//test will be in local mode
annotation|@
name|Override
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
block|{
try|try
block|{
name|HCatRecord
name|rec
init|=
name|writeRecords
operator|.
name|get
argument_list|(
name|writeCount
argument_list|)
decl_stmt|;
name|context
operator|.
name|write
argument_list|(
literal|null
argument_list|,
name|rec
argument_list|)
expr_stmt|;
name|writeCount
operator|++
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|(
name|System
operator|.
name|err
argument_list|)
expr_stmt|;
comment|//print since otherwise exception is lost
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|MapRead
extends|extends
name|Mapper
argument_list|<
name|WritableComparable
argument_list|,
name|HCatRecord
argument_list|,
name|BytesWritable
argument_list|,
name|Text
argument_list|>
block|{
specifier|static
name|int
name|readCount
init|=
literal|0
decl_stmt|;
comment|//test will be in local mode
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
name|WritableComparable
name|key
parameter_list|,
name|HCatRecord
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
block|{
try|try
block|{
name|readRecords
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|readCount
operator|++
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
comment|//print since otherwise exception is lost
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
name|Job
name|runMRCreate
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
parameter_list|,
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|partitionColumns
parameter_list|,
name|List
argument_list|<
name|HCatRecord
argument_list|>
name|records
parameter_list|,
name|int
name|writeCount
parameter_list|,
name|boolean
name|assertWrite
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|runMRCreate
argument_list|(
name|partitionValues
argument_list|,
name|partitionColumns
argument_list|,
name|records
argument_list|,
name|writeCount
argument_list|,
name|assertWrite
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**      * Run a local map reduce job to load data from in memory records to an HCatalog Table      * @param partitionValues      * @param partitionColumns      * @param records data to be written to HCatalog table      * @param writeCount      * @param assertWrite      * @param asSingleMapTask      * @return      * @throws Exception      */
name|Job
name|runMRCreate
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
parameter_list|,
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|partitionColumns
parameter_list|,
name|List
argument_list|<
name|HCatRecord
argument_list|>
name|records
parameter_list|,
name|int
name|writeCount
parameter_list|,
name|boolean
name|assertWrite
parameter_list|,
name|boolean
name|asSingleMapTask
parameter_list|)
throws|throws
name|Exception
block|{
name|writeRecords
operator|=
name|records
expr_stmt|;
name|MapCreate
operator|.
name|writeCount
operator|=
literal|0
expr_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|,
literal|"hcat mapreduce write test"
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|HCatMapReduceTest
operator|.
name|MapCreate
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// input/output settings
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|TextInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
if|if
condition|(
name|asSingleMapTask
condition|)
block|{
comment|// One input path would mean only one map task
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|fs
operator|.
name|getWorkingDirectory
argument_list|()
argument_list|,
literal|"mapred/testHCatMapReduceInput"
argument_list|)
decl_stmt|;
name|createInputFile
argument_list|(
name|path
argument_list|,
name|writeCount
argument_list|)
expr_stmt|;
name|TextInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Create two input paths so that two map tasks get triggered. There could be other ways
comment|// to trigger two map tasks.
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|fs
operator|.
name|getWorkingDirectory
argument_list|()
argument_list|,
literal|"mapred/testHCatMapReduceInput"
argument_list|)
decl_stmt|;
name|createInputFile
argument_list|(
name|path
argument_list|,
name|writeCount
operator|/
literal|2
argument_list|)
expr_stmt|;
name|Path
name|path2
init|=
operator|new
name|Path
argument_list|(
name|fs
operator|.
name|getWorkingDirectory
argument_list|()
argument_list|,
literal|"mapred/testHCatMapReduceInput2"
argument_list|)
decl_stmt|;
name|createInputFile
argument_list|(
name|path2
argument_list|,
operator|(
name|writeCount
operator|-
name|writeCount
operator|/
literal|2
operator|)
argument_list|)
expr_stmt|;
name|TextInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
name|path
argument_list|,
name|path2
argument_list|)
expr_stmt|;
block|}
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|HCatOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|OutputJobInfo
name|outputJobInfo
init|=
name|OutputJobInfo
operator|.
name|create
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partitionValues
argument_list|)
decl_stmt|;
name|HCatOutputFormat
operator|.
name|setOutput
argument_list|(
name|job
argument_list|,
name|outputJobInfo
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|BytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|DefaultHCatRecord
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|HCatOutputFormat
operator|.
name|setSchema
argument_list|(
name|job
argument_list|,
operator|new
name|HCatSchema
argument_list|(
name|partitionColumns
argument_list|)
argument_list|)
expr_stmt|;
name|boolean
name|success
init|=
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
decl_stmt|;
comment|// Ensure counters are set when data has actually been read.
if|if
condition|(
name|partitionValues
operator|!=
literal|null
condition|)
block|{
name|assertTrue
argument_list|(
name|job
operator|.
name|getCounters
argument_list|()
operator|.
name|getGroup
argument_list|(
literal|"FileSystemCounters"
argument_list|)
operator|.
name|findCounter
argument_list|(
literal|"FILE_BYTES_READ"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|HcatTestUtils
operator|.
name|isHadoop23
argument_list|()
condition|)
block|{
comment|// Local mode outputcommitter hook is not invoked in Hadoop 1.x
if|if
condition|(
name|success
condition|)
block|{
operator|new
name|FileOutputCommitterContainer
argument_list|(
name|job
argument_list|,
literal|null
argument_list|)
operator|.
name|commitJob
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|new
name|FileOutputCommitterContainer
argument_list|(
name|job
argument_list|,
literal|null
argument_list|)
operator|.
name|abortJob
argument_list|(
name|job
argument_list|,
name|JobStatus
operator|.
name|State
operator|.
name|FAILED
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|assertWrite
condition|)
block|{
comment|// we assert only if we expected to assert with this call.
name|Assert
operator|.
name|assertEquals
argument_list|(
name|writeCount
argument_list|,
name|MapCreate
operator|.
name|writeCount
argument_list|)
expr_stmt|;
block|}
return|return
name|job
return|;
block|}
name|List
argument_list|<
name|HCatRecord
argument_list|>
name|runMRRead
parameter_list|(
name|int
name|readCount
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|runMRRead
argument_list|(
name|readCount
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**      * Run a local map reduce job to read records from HCatalog table and verify if the count is as expected      * @param readCount      * @param filter      * @return      * @throws Exception      */
name|List
argument_list|<
name|HCatRecord
argument_list|>
name|runMRRead
parameter_list|(
name|int
name|readCount
parameter_list|,
name|String
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
name|MapRead
operator|.
name|readCount
operator|=
literal|0
expr_stmt|;
name|readRecords
operator|.
name|clear
argument_list|()
expr_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|,
literal|"hcat mapreduce read test"
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|HCatMapReduceTest
operator|.
name|MapRead
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// input/output settings
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|HCatInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|TextOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|HCatInputFormat
operator|.
name|setInput
argument_list|(
name|job
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|)
operator|.
name|setFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|BytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|fs
operator|.
name|getWorkingDirectory
argument_list|()
argument_list|,
literal|"mapred/testHCatMapReduceOutput"
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|path
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|TextOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|job
argument_list|,
name|path
argument_list|)
expr_stmt|;
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|readCount
argument_list|,
name|MapRead
operator|.
name|readCount
argument_list|)
expr_stmt|;
return|return
name|readRecords
return|;
block|}
specifier|protected
name|HCatSchema
name|getTableSchema
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|,
literal|"hcat mapreduce read schema test"
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
comment|// input/output settings
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|HCatInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|TextOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|HCatInputFormat
operator|.
name|setInput
argument_list|(
name|job
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
return|return
name|HCatInputFormat
operator|.
name|getTableSchema
argument_list|(
name|job
argument_list|)
return|;
block|}
block|}
end_class

end_unit

