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
name|mapreduce
package|;
end_package

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
name|Arrays
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
name|TestCase
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
name|api
operator|.
name|Database
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
name|Partition
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
name|shims
operator|.
name|ShimLoader
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
name|TaskAttemptContext
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

begin_class
specifier|public
class|class
name|TestHCatOutputFormat
extends|extends
name|TestCase
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
name|TestHCatOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HiveMetaStoreClient
name|client
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|dbName
init|=
literal|"hcatOutputFormatTestDB"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|tblName
init|=
literal|"hcatOutputFormatTestTable"
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
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
try|try
block|{
name|client
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|initTable
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to open the metastore"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|client
operator|.
name|dropTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to close metastore"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|initTable
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|client
operator|.
name|dropTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{     }
try|try
block|{
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{     }
name|client
operator|.
name|createDatabase
argument_list|(
operator|new
name|Database
argument_list|(
name|dbName
argument_list|,
literal|""
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
operator|(
name|client
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
operator|.
name|getLocationUri
argument_list|()
operator|)
argument_list|)
expr_stmt|;
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
literal|"colname"
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
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
name|dbName
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setTableName
argument_list|(
name|tblName
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
name|fields
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSd
argument_list|(
name|sd
argument_list|)
expr_stmt|;
comment|//sd.setLocation("hdfs://tmp");
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
name|sd
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
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
literal|"test_param_1"
argument_list|,
literal|"Use this for comments etc"
argument_list|)
expr_stmt|;
comment|//sd.setBucketCols(new ArrayList<String>(2));
comment|//sd.getBucketCols().add("name");
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
name|lazy
operator|.
name|LazySimpleSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setPartitionKeys
argument_list|(
name|fields
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
name|tableParams
operator|.
name|put
argument_list|(
literal|"hcat.testarg"
argument_list|,
literal|"testArgValue"
argument_list|)
expr_stmt|;
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
name|Path
name|tblPath
init|=
operator|new
name|Path
argument_list|(
name|client
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|tblPath
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
operator|.
name|mkdirs
argument_list|(
operator|new
name|Path
argument_list|(
name|tblPath
argument_list|,
literal|"colname=p1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testSetOutput
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
literal|"test outputformat"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
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
name|partitionValues
operator|.
name|put
argument_list|(
literal|"colname"
argument_list|,
literal|"p1"
argument_list|)
expr_stmt|;
comment|//null server url means local mode
name|OutputJobInfo
name|info
init|=
name|OutputJobInfo
operator|.
name|create
argument_list|(
name|dbName
argument_list|,
name|tblName
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
name|info
argument_list|)
expr_stmt|;
name|OutputJobInfo
name|jobInfo
init|=
name|HCatOutputFormat
operator|.
name|getJobInfo
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|jobInfo
operator|.
name|getPartitionValues
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"p1"
argument_list|,
name|jobInfo
operator|.
name|getPartitionValues
argument_list|()
operator|.
name|get
argument_list|(
literal|"colname"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getDataColumns
argument_list|()
operator|.
name|getFields
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"colname"
argument_list|,
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getDataColumns
argument_list|()
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|publishTest
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|publishTest
parameter_list|(
name|Job
name|job
parameter_list|)
throws|throws
name|Exception
block|{
name|HCatOutputFormat
name|hcof
init|=
operator|new
name|HCatOutputFormat
argument_list|()
decl_stmt|;
name|TaskAttemptContext
name|tac
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createTaskAttemptContext
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createTaskAttemptID
argument_list|()
argument_list|)
decl_stmt|;
name|OutputCommitter
name|committer
init|=
name|hcof
operator|.
name|getOutputCommitter
argument_list|(
name|tac
argument_list|)
decl_stmt|;
name|committer
operator|.
name|setupJob
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|committer
operator|.
name|setupTask
argument_list|(
name|tac
argument_list|)
expr_stmt|;
name|committer
operator|.
name|commitTask
argument_list|(
name|tac
argument_list|)
expr_stmt|;
name|committer
operator|.
name|commitJob
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|Partition
name|part
init|=
name|client
operator|.
name|getPartition
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"p1"
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|part
argument_list|)
expr_stmt|;
name|StorerInfo
name|storer
init|=
name|InternalUtil
operator|.
name|extractStorerInfo
argument_list|(
name|part
operator|.
name|getSd
argument_list|()
argument_list|,
name|part
operator|.
name|getParameters
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|storer
operator|.
name|getProperties
argument_list|()
operator|.
name|get
argument_list|(
literal|"hcat.testarg"
argument_list|)
argument_list|,
literal|"testArgValue"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|part
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
operator|.
name|indexOf
argument_list|(
literal|"p1"
argument_list|)
operator|!=
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

