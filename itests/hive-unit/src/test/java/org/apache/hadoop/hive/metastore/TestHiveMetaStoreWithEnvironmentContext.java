begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
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
name|EnvironmentContext
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
name|metastore
operator|.
name|events
operator|.
name|AddPartitionEvent
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
name|events
operator|.
name|AlterTableEvent
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
name|events
operator|.
name|CreateDatabaseEvent
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
name|events
operator|.
name|CreateTableEvent
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
name|events
operator|.
name|DropDatabaseEvent
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
name|events
operator|.
name|DropPartitionEvent
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
name|events
operator|.
name|DropTableEvent
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
name|events
operator|.
name|ListenerEvent
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
name|HiveInputFormat
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
name|HiveOutputFormat
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
name|lazy
operator|.
name|LazySimpleSerDe
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

begin_comment
comment|/**  * TestHiveMetaStoreWithEnvironmentContext. Test case for _with_environment_context  * calls in {@link org.apache.hadoop.hive.metastore.HiveMetaStore}  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveMetaStoreWithEnvironmentContext
extends|extends
name|TestCase
block|{
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|HiveMetaStoreClient
name|msc
decl_stmt|;
specifier|private
name|EnvironmentContext
name|envContext
decl_stmt|;
specifier|private
specifier|final
name|Database
name|db
init|=
operator|new
name|Database
argument_list|()
decl_stmt|;
specifier|private
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Partition
name|partition
init|=
operator|new
name|Partition
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|dbName
init|=
literal|"hive3252"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|tblName
init|=
literal|"tmptbl"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|renamed
init|=
literal|"tmptbl2"
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
name|System
operator|.
name|setProperty
argument_list|(
literal|"hive.metastore.event.listeners"
argument_list|,
name|DummyListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|port
init|=
name|MetaStoreUtils
operator|.
name|findFreePort
argument_list|()
decl_stmt|;
name|MetaStoreUtils
operator|.
name|startMetaStore
argument_list|(
name|port
argument_list|,
name|ShimLoader
operator|.
name|getHadoopThriftAuthBridge
argument_list|()
argument_list|)
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
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|,
literal|"thrift://localhost:"
operator|+
name|port
argument_list|)
expr_stmt|;
name|hiveConf
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
name|msc
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|msc
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|envProperties
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
name|envProperties
operator|.
name|put
argument_list|(
literal|"hadoop.job.ugi"
argument_list|,
literal|"test_user"
argument_list|)
expr_stmt|;
name|envContext
operator|=
operator|new
name|EnvironmentContext
argument_list|(
name|envProperties
argument_list|)
expr_stmt|;
name|db
operator|.
name|setName
argument_list|(
name|dbName
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
literal|"a"
argument_list|,
literal|"string"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partitionKeys
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
decl_stmt|;
name|partitionKeys
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"b"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
decl_stmt|;
name|cols
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"a"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"b"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
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
name|cols
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setCompressed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setParameters
argument_list|(
name|tableParams
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
name|tblName
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
name|LazySimpleSerDe
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
name|HiveInputFormat
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
name|HiveOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|table
operator|.
name|setTableName
argument_list|(
name|tblName
argument_list|)
expr_stmt|;
name|table
operator|.
name|setParameters
argument_list|(
name|tableParams
argument_list|)
expr_stmt|;
name|table
operator|.
name|setPartitionKeys
argument_list|(
name|partitionKeys
argument_list|)
expr_stmt|;
name|table
operator|.
name|setSd
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partValues
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|partValues
operator|.
name|add
argument_list|(
literal|"2011"
argument_list|)
expr_stmt|;
name|partition
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|partition
operator|.
name|setTableName
argument_list|(
name|tblName
argument_list|)
expr_stmt|;
name|partition
operator|.
name|setValues
argument_list|(
name|partValues
argument_list|)
expr_stmt|;
name|partition
operator|.
name|setSd
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|deepCopy
argument_list|()
argument_list|)
expr_stmt|;
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|setSerdeInfo
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|deepCopy
argument_list|()
argument_list|)
expr_stmt|;
name|DummyListener
operator|.
name|notifyList
operator|.
name|clear
argument_list|()
expr_stmt|;
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
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testEnvironmentContext
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|listSize
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|ListenerEvent
argument_list|>
name|notifyList
init|=
name|DummyListener
operator|.
name|notifyList
decl_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|msc
operator|.
name|createDatabase
argument_list|(
name|db
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|listSize
argument_list|,
name|notifyList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|CreateDatabaseEvent
name|dbEvent
init|=
call|(
name|CreateDatabaseEvent
call|)
argument_list|(
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
assert|assert
name|dbEvent
operator|.
name|getStatus
argument_list|()
assert|;
name|msc
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|envContext
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|CreateTableEvent
name|tblEvent
init|=
call|(
name|CreateTableEvent
call|)
argument_list|(
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
assert|assert
name|tblEvent
operator|.
name|getStatus
argument_list|()
assert|;
name|assertEquals
argument_list|(
name|envContext
argument_list|,
name|tblEvent
operator|.
name|getEnvironmentContext
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|=
name|msc
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|setLocation
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
operator|+
literal|"/part1"
argument_list|)
expr_stmt|;
name|msc
operator|.
name|add_partition
argument_list|(
name|partition
argument_list|,
name|envContext
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|AddPartitionEvent
name|partEvent
init|=
call|(
name|AddPartitionEvent
call|)
argument_list|(
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
assert|assert
name|partEvent
operator|.
name|getStatus
argument_list|()
assert|;
name|assertEquals
argument_list|(
name|envContext
argument_list|,
name|partEvent
operator|.
name|getEnvironmentContext
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partVals
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|partVals
operator|.
name|add
argument_list|(
literal|"2012"
argument_list|)
expr_stmt|;
name|msc
operator|.
name|appendPartition
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|partVals
argument_list|,
name|envContext
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|AddPartitionEvent
name|appendPartEvent
init|=
call|(
name|AddPartitionEvent
call|)
argument_list|(
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
assert|assert
name|appendPartEvent
operator|.
name|getStatus
argument_list|()
assert|;
name|assertEquals
argument_list|(
name|envContext
argument_list|,
name|appendPartEvent
operator|.
name|getEnvironmentContext
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|setTableName
argument_list|(
name|renamed
argument_list|)
expr_stmt|;
name|msc
operator|.
name|alter_table_with_environmentContext
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|table
argument_list|,
name|envContext
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|AlterTableEvent
name|alterTableEvent
init|=
operator|(
name|AlterTableEvent
operator|)
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|alterTableEvent
operator|.
name|getStatus
argument_list|()
assert|;
name|assertEquals
argument_list|(
name|envContext
argument_list|,
name|alterTableEvent
operator|.
name|getEnvironmentContext
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|setTableName
argument_list|(
name|tblName
argument_list|)
expr_stmt|;
name|msc
operator|.
name|alter_table_with_environmentContext
argument_list|(
name|dbName
argument_list|,
name|renamed
argument_list|,
name|table
argument_list|,
name|envContext
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|dropPartVals
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|dropPartVals
operator|.
name|add
argument_list|(
literal|"2011"
argument_list|)
expr_stmt|;
name|msc
operator|.
name|dropPartition
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|dropPartVals
argument_list|,
name|envContext
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|DropPartitionEvent
name|dropPartEvent
init|=
operator|(
name|DropPartitionEvent
operator|)
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|dropPartEvent
operator|.
name|getStatus
argument_list|()
assert|;
name|assertEquals
argument_list|(
name|envContext
argument_list|,
name|dropPartEvent
operator|.
name|getEnvironmentContext
argument_list|()
argument_list|)
expr_stmt|;
name|msc
operator|.
name|dropPartition
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
literal|"b=2012"
argument_list|,
literal|true
argument_list|,
name|envContext
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|DropPartitionEvent
name|dropPartByNameEvent
init|=
operator|(
name|DropPartitionEvent
operator|)
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|dropPartByNameEvent
operator|.
name|getStatus
argument_list|()
assert|;
name|assertEquals
argument_list|(
name|envContext
argument_list|,
name|dropPartByNameEvent
operator|.
name|getEnvironmentContext
argument_list|()
argument_list|)
expr_stmt|;
name|msc
operator|.
name|dropTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
name|envContext
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|DropTableEvent
name|dropTblEvent
init|=
operator|(
name|DropTableEvent
operator|)
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|dropTblEvent
operator|.
name|getStatus
argument_list|()
assert|;
name|assertEquals
argument_list|(
name|envContext
argument_list|,
name|dropTblEvent
operator|.
name|getEnvironmentContext
argument_list|()
argument_list|)
expr_stmt|;
name|msc
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|listSize
operator|++
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|DropDatabaseEvent
name|dropDB
init|=
operator|(
name|DropDatabaseEvent
operator|)
name|notifyList
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|dropDB
operator|.
name|getStatus
argument_list|()
assert|;
block|}
block|}
end_class

end_unit

