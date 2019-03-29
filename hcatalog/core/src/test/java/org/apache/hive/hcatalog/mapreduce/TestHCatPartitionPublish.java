begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|mapreduce
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
name|security
operator|.
name|Policy
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
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
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
name|HiveMetaStoreClient
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
name|MetaStoreTestUtils
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
name|Warehouse
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
name|metastore
operator|.
name|conf
operator|.
name|MetastoreConf
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
name|security
operator|.
name|HadoopThriftAuthBridge
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|MiniMRCluster
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
name|hive
operator|.
name|hcatalog
operator|.
name|DerbyPolicy
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
name|NoExitSecurityManager
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
name|cli
operator|.
name|SemanticAnalysis
operator|.
name|HCatSemanticAnalyzer
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
name|DefaultHCatRecord
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
name|HCatFieldSchema
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
name|data
operator|.
name|schema
operator|.
name|HCatSchemaUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
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
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestHCatPartitionPublish
block|{
specifier|private
specifier|static
name|Configuration
name|mrConf
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|MiniMRCluster
name|mrCluster
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|isServerRunning
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|hcatConf
decl_stmt|;
specifier|private
specifier|static
name|HiveMetaStoreClient
name|msc
decl_stmt|;
specifier|private
specifier|static
name|SecurityManager
name|securityManager
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
literal|true
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|testName
decl_stmt|;
specifier|public
specifier|static
name|File
name|handleWorkDir
parameter_list|()
throws|throws
name|IOException
block|{
name|testName
operator|=
literal|"test_hcat_partitionpublish_"
operator|+
name|Math
operator|.
name|abs
argument_list|(
operator|new
name|Random
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|testDir
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.data.dir"
argument_list|,
literal|"./"
argument_list|)
decl_stmt|;
name|testDir
operator|=
name|testDir
operator|+
literal|"/"
operator|+
name|testName
operator|+
literal|"/"
expr_stmt|;
name|File
name|workDir
init|=
operator|new
name|File
argument_list|(
operator|new
name|File
argument_list|(
name|testDir
argument_list|)
operator|.
name|getCanonicalPath
argument_list|()
argument_list|)
decl_stmt|;
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|workDir
argument_list|)
expr_stmt|;
name|workDir
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
return|return
name|workDir
return|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|File
name|workDir
init|=
name|handleWorkDir
argument_list|()
decl_stmt|;
name|Path
name|tmpDir
init|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|,
literal|"target"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"test"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"yarn.app.mapreduce.am.staging-dir"
argument_list|,
name|tmpDir
operator|+
name|File
operator|.
name|separator
operator|+
name|testName
operator|+
name|File
operator|.
name|separator
operator|+
literal|"hadoop-yarn"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"staging"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"yarn.scheduler.capacity.root.queues"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"yarn.scheduler.capacity.root.default.capacity"
argument_list|,
literal|"100"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"fs.pfile.impl"
argument_list|,
literal|"org.apache.hadoop.fs.ProxyLocalFileSystem"
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"hadoop.log.dir"
argument_list|,
operator|new
name|File
argument_list|(
name|workDir
argument_list|,
literal|"/logs"
argument_list|)
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
comment|// LocalJobRunner does not work with mapreduce OutputCommitter. So need
comment|// to use MiniMRCluster. MAPREDUCE-2350
name|mrCluster
operator|=
operator|new
name|MiniMRCluster
argument_list|(
literal|1
argument_list|,
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|mrConf
operator|=
name|mrCluster
operator|.
name|createJobConf
argument_list|()
expr_stmt|;
if|if
condition|(
name|isServerRunning
condition|)
block|{
return|return;
block|}
name|hcatConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|TestHCatPartitionPublish
operator|.
name|class
argument_list|)
expr_stmt|;
name|MetaStoreTestUtils
operator|.
name|startMetaStoreWithRetry
argument_list|(
name|hcatConf
argument_list|)
expr_stmt|;
name|isServerRunning
operator|=
literal|true
expr_stmt|;
name|securityManager
operator|=
name|System
operator|.
name|getSecurityManager
argument_list|()
expr_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
operator|new
name|NoExitSecurityManager
argument_list|()
argument_list|)
expr_stmt|;
name|Policy
operator|.
name|setPolicy
argument_list|(
operator|new
name|DerbyPolicy
argument_list|()
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORETHRIFTCONNECTIONRETRIES
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORETHRIFTFAILURERETRIES
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|setTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CLIENT_SOCKET_TIMEOUT
argument_list|,
literal|120
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SEMANTIC_ANALYZER_HOOK
operator|.
name|varname
argument_list|,
name|HCatSemanticAnalyzer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|hcatConf
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
name|hcatConf
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
name|hcatConf
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
name|msc
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hcatConf
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|" "
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|" "
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
operator|.
name|varname
argument_list|,
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|hcatConf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|WAREHOUSE
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORECONNECTURLKEY
operator|.
name|varname
argument_list|,
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|hcatConf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CONNECT_URL_KEY
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
operator|.
name|varname
argument_list|,
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|hcatConf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|THRIFT_URIS
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|mrCluster
operator|!=
literal|null
condition|)
block|{
name|mrCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|System
operator|.
name|setSecurityManager
argument_list|(
name|securityManager
argument_list|)
expr_stmt|;
name|isServerRunning
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPartitionPublish
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|dbName
init|=
literal|"default"
decl_stmt|;
name|String
name|tableName
init|=
literal|"testHCatPartitionedTable"
decl_stmt|;
name|createTable
argument_list|(
literal|null
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionMap
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
name|partitionMap
operator|.
name|put
argument_list|(
literal|"part1"
argument_list|,
literal|"p1value1"
argument_list|)
expr_stmt|;
name|partitionMap
operator|.
name|put
argument_list|(
literal|"part0"
argument_list|,
literal|"p0value1"
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|HCatFieldSchema
argument_list|>
name|hcatTableColumns
init|=
operator|new
name|ArrayList
argument_list|<
name|HCatFieldSchema
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldSchema
name|fs
range|:
name|getTableColumns
argument_list|()
control|)
block|{
name|hcatTableColumns
operator|.
name|add
argument_list|(
name|HCatSchemaUtils
operator|.
name|getHCatFieldSchema
argument_list|(
name|fs
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|runMRCreateFail
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partitionMap
argument_list|,
name|hcatTableColumns
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|ptns
init|=
name|msc
operator|.
name|listPartitionNames
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
operator|(
name|short
operator|)
literal|10
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ptns
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|msc
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|table
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
operator|+
literal|"/part1=p1value1/part0=p0value1"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|exists
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|void
name|runMRCreateFail
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
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
name|columns
parameter_list|)
throws|throws
name|Exception
block|{
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|mrConf
argument_list|,
literal|"hcat mapreduce write fail test"
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
name|TestHCatPartitionPublish
operator|.
name|MapFail
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
comment|// The write count does not matter, as the map will fail in its first
comment|// call.
name|createInputFile
argument_list|(
name|path
argument_list|,
literal|5
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
name|columns
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
name|Assert
operator|.
name|assertTrue
argument_list|(
name|success
operator|==
literal|false
argument_list|)
expr_stmt|;
block|}
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
name|MapFail
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
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Exception to mimic job failure."
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|void
name|createTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
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
name|Warehouse
operator|.
name|DEFAULT_DATABASE_NAME
else|:
name|dbName
decl_stmt|;
try|try
block|{
name|msc
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
block|{     }
comment|// can fail with NoSuchObjectException
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
name|ColumnarSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setInputFormat
argument_list|(
name|RCFileInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setOutputFormat
argument_list|(
name|RCFileOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
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
name|msc
operator|.
name|createTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getPartitionKeys
parameter_list|()
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
decl_stmt|;
comment|// Defining partition names in unsorted order
name|fields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"PaRT1"
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|fields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"part0"
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|fields
return|;
block|}
specifier|protected
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getTableColumns
parameter_list|()
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
decl_stmt|;
name|fields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"c1"
argument_list|,
name|serdeConstants
operator|.
name|INT_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|fields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"c2"
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|fields
return|;
block|}
block|}
end_class

end_unit

