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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|hbase
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|hbase
operator|.
name|HBaseTestingUtility
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
name|hbase
operator|.
name|client
operator|.
name|HConnection
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
name|hbase
operator|.
name|client
operator|.
name|HTableInterface
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
name|Assert
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
name|junit
operator|.
name|Rule
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|ExpectedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|MockitoAnnotations
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

begin_comment
comment|/**  * Integration tests with HBase Mini-cluster for HBaseStore  */
end_comment

begin_class
specifier|public
class|class
name|TestStorageDescriptorSharing
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestHBaseStoreIntegration
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|utility
decl_stmt|;
specifier|private
specifier|static
name|HTableInterface
name|tblTable
decl_stmt|;
specifier|private
specifier|static
name|HTableInterface
name|sdTable
decl_stmt|;
specifier|private
specifier|static
name|HTableInterface
name|partTable
decl_stmt|;
specifier|private
specifier|static
name|HTableInterface
name|dbTable
decl_stmt|;
specifier|private
specifier|static
name|HTableInterface
name|roleTable
decl_stmt|;
specifier|private
specifier|static
name|HTableInterface
name|globalPrivsTable
decl_stmt|;
specifier|private
specifier|static
name|HTableInterface
name|principalRoleMapTable
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|emptyParameters
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
annotation|@
name|Rule
specifier|public
name|ExpectedException
name|thrown
init|=
name|ExpectedException
operator|.
name|none
argument_list|()
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|HConnection
name|hconn
decl_stmt|;
specifier|private
name|HBaseStore
name|store
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|startMiniCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|utility
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|utility
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|byte
index|[]
index|[]
name|families
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|HBaseReadWrite
operator|.
name|CATALOG_CF
block|,
name|HBaseReadWrite
operator|.
name|STATS_CF
block|}
decl_stmt|;
name|tblTable
operator|=
name|utility
operator|.
name|createTable
argument_list|(
name|HBaseReadWrite
operator|.
name|TABLE_TABLE
operator|.
name|getBytes
argument_list|(
name|HBaseUtils
operator|.
name|ENCODING
argument_list|)
argument_list|,
name|families
argument_list|)
expr_stmt|;
name|sdTable
operator|=
name|utility
operator|.
name|createTable
argument_list|(
name|HBaseReadWrite
operator|.
name|SD_TABLE
operator|.
name|getBytes
argument_list|(
name|HBaseUtils
operator|.
name|ENCODING
argument_list|)
argument_list|,
name|HBaseReadWrite
operator|.
name|CATALOG_CF
argument_list|)
expr_stmt|;
name|partTable
operator|=
name|utility
operator|.
name|createTable
argument_list|(
name|HBaseReadWrite
operator|.
name|PART_TABLE
operator|.
name|getBytes
argument_list|(
name|HBaseUtils
operator|.
name|ENCODING
argument_list|)
argument_list|,
name|families
argument_list|)
expr_stmt|;
name|dbTable
operator|=
name|utility
operator|.
name|createTable
argument_list|(
name|HBaseReadWrite
operator|.
name|DB_TABLE
operator|.
name|getBytes
argument_list|(
name|HBaseUtils
operator|.
name|ENCODING
argument_list|)
argument_list|,
name|HBaseReadWrite
operator|.
name|CATALOG_CF
argument_list|)
expr_stmt|;
name|roleTable
operator|=
name|utility
operator|.
name|createTable
argument_list|(
name|HBaseReadWrite
operator|.
name|ROLE_TABLE
operator|.
name|getBytes
argument_list|(
name|HBaseUtils
operator|.
name|ENCODING
argument_list|)
argument_list|,
name|HBaseReadWrite
operator|.
name|CATALOG_CF
argument_list|)
expr_stmt|;
name|globalPrivsTable
operator|=
name|utility
operator|.
name|createTable
argument_list|(
name|HBaseReadWrite
operator|.
name|GLOBAL_PRIVS_TABLE
operator|.
name|getBytes
argument_list|(
name|HBaseUtils
operator|.
name|ENCODING
argument_list|)
argument_list|,
name|HBaseReadWrite
operator|.
name|CATALOG_CF
argument_list|)
expr_stmt|;
name|principalRoleMapTable
operator|=
name|utility
operator|.
name|createTable
argument_list|(
name|HBaseReadWrite
operator|.
name|USER_TO_ROLE_TABLE
operator|.
name|getBytes
argument_list|(
name|HBaseUtils
operator|.
name|ENCODING
argument_list|)
argument_list|,
name|HBaseReadWrite
operator|.
name|CATALOG_CF
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|shutdownMiniCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|utility
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setupConnection
parameter_list|()
throws|throws
name|IOException
block|{
name|MockitoAnnotations
operator|.
name|initMocks
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|hconn
operator|.
name|getTable
argument_list|(
name|HBaseReadWrite
operator|.
name|SD_TABLE
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|sdTable
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|hconn
operator|.
name|getTable
argument_list|(
name|HBaseReadWrite
operator|.
name|TABLE_TABLE
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|tblTable
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|hconn
operator|.
name|getTable
argument_list|(
name|HBaseReadWrite
operator|.
name|PART_TABLE
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|partTable
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|hconn
operator|.
name|getTable
argument_list|(
name|HBaseReadWrite
operator|.
name|DB_TABLE
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|dbTable
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|hconn
operator|.
name|getTable
argument_list|(
name|HBaseReadWrite
operator|.
name|ROLE_TABLE
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|roleTable
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|hconn
operator|.
name|getTable
argument_list|(
name|HBaseReadWrite
operator|.
name|GLOBAL_PRIVS_TABLE
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|globalPrivsTable
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|hconn
operator|.
name|getTable
argument_list|(
name|HBaseReadWrite
operator|.
name|USER_TO_ROLE_TABLE
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|principalRoleMapTable
argument_list|)
expr_stmt|;
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
comment|// Turn off caching, as we want to test actual interaction with HBase
name|conf
operator|.
name|setBoolean
argument_list|(
name|HBaseReadWrite
operator|.
name|NO_CACHE_CONF
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|HBaseReadWrite
name|hbase
init|=
name|HBaseReadWrite
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|hbase
operator|.
name|setConnection
argument_list|(
name|hconn
argument_list|)
expr_stmt|;
name|store
operator|=
operator|new
name|HBaseStore
argument_list|()
expr_stmt|;
name|store
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|createManyPartitions
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
literal|"manyParts"
decl_stmt|;
name|int
name|startTime
init|=
call|(
name|int
call|)
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
argument_list|)
decl_stmt|;
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
literal|"col1"
argument_list|,
literal|"int"
argument_list|,
literal|"nocomment"
argument_list|)
argument_list|)
expr_stmt|;
name|SerDeInfo
name|serde
init|=
operator|new
name|SerDeInfo
argument_list|(
literal|"serde"
argument_list|,
literal|"seriallib"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|(
name|cols
argument_list|,
literal|"file:/tmp"
argument_list|,
literal|"input"
argument_list|,
literal|"output"
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
name|serde
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|emptyParameters
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
decl_stmt|;
name|partCols
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"pc"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
operator|new
name|Table
argument_list|(
name|tableName
argument_list|,
name|dbName
argument_list|,
literal|"me"
argument_list|,
name|startTime
argument_list|,
name|startTime
argument_list|,
literal|0
argument_list|,
name|sd
argument_list|,
name|partCols
argument_list|,
name|emptyParameters
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|store
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partVals
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"alan"
argument_list|,
literal|"bob"
argument_list|,
literal|"carl"
argument_list|,
literal|"doug"
argument_list|,
literal|"ethan"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|val
range|:
name|partVals
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|vals
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|vals
operator|.
name|add
argument_list|(
name|val
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|psd
init|=
operator|new
name|StorageDescriptor
argument_list|(
name|sd
argument_list|)
decl_stmt|;
name|psd
operator|.
name|setLocation
argument_list|(
literal|"file:/tmp/pc="
operator|+
name|val
argument_list|)
expr_stmt|;
name|Partition
name|part
init|=
operator|new
name|Partition
argument_list|(
name|vals
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|startTime
argument_list|,
name|startTime
argument_list|,
name|psd
argument_list|,
name|emptyParameters
argument_list|)
decl_stmt|;
name|store
operator|.
name|addPartition
argument_list|(
name|part
argument_list|)
expr_stmt|;
name|Partition
name|p
init|=
name|store
operator|.
name|getPartition
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|vals
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"file:/tmp/pc="
operator|+
name|val
argument_list|,
name|p
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|HBaseReadWrite
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|)
operator|.
name|countStorageDescriptor
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|=
operator|new
name|StorageDescriptor
argument_list|(
name|cols
argument_list|,
literal|"file:/tmp"
argument_list|,
literal|"input2"
argument_list|,
literal|"output"
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
name|serde
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|emptyParameters
argument_list|)
expr_stmt|;
name|table
operator|=
operator|new
name|Table
argument_list|(
literal|"differenttable"
argument_list|,
literal|"default"
argument_list|,
literal|"me"
argument_list|,
name|startTime
argument_list|,
name|startTime
argument_list|,
literal|0
argument_list|,
name|sd
argument_list|,
literal|null
argument_list|,
name|emptyParameters
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|store
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|HBaseReadWrite
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|)
operator|.
name|countStorageDescriptor
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

