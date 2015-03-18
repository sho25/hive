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
name|HashMap
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
name|IMockUtils
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
name|IMockUtils
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|HBaseTestingUtility
name|utility
decl_stmt|;
specifier|protected
specifier|static
name|HTableInterface
name|tblTable
decl_stmt|;
specifier|protected
specifier|static
name|HTableInterface
name|sdTable
decl_stmt|;
specifier|protected
specifier|static
name|HTableInterface
name|partTable
decl_stmt|;
specifier|protected
specifier|static
name|HTableInterface
name|dbTable
decl_stmt|;
specifier|protected
specifier|static
name|HTableInterface
name|funcTable
decl_stmt|;
specifier|protected
specifier|static
name|HTableInterface
name|roleTable
decl_stmt|;
specifier|protected
specifier|static
name|HTableInterface
name|globalPrivsTable
decl_stmt|;
specifier|protected
specifier|static
name|HTableInterface
name|principalRoleMapTable
decl_stmt|;
specifier|protected
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
name|Mock
specifier|private
name|HBaseConnection
name|hconn
decl_stmt|;
specifier|protected
name|HBaseStore
name|store
decl_stmt|;
specifier|protected
name|HiveConf
name|conf
decl_stmt|;
specifier|protected
name|Driver
name|driver
decl_stmt|;
specifier|protected
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
name|funcTable
operator|=
name|utility
operator|.
name|createTable
argument_list|(
name|HBaseReadWrite
operator|.
name|FUNC_TABLE
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
specifier|protected
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
specifier|protected
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
name|getHBaseTable
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
name|getHBaseTable
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
name|getHBaseTable
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
name|getHBaseTable
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
name|getHBaseTable
argument_list|(
name|HBaseReadWrite
operator|.
name|FUNC_TABLE
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|funcTable
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|hconn
operator|.
name|getHBaseTable
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
name|getHBaseTable
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
name|getHBaseTable
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
block|}
specifier|protected
name|void
name|setupDriver
parameter_list|()
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_HBASE_CONNECTION_CLASS
argument_list|,
name|HBaseReadWrite
operator|.
name|TEST_CONN
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DYNAMICPARTITIONINGMODE
argument_list|,
literal|"nonstrict"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_RAW_STORE_IMPL
argument_list|,
literal|"org.apache.hadoop.hive.metastore.hbase.HBaseStore"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_FASTPATH
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|HBaseReadWrite
operator|.
name|setTestConnection
argument_list|(
name|hconn
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|setupHBaseStore
parameter_list|()
block|{
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
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_HBASE_CONNECTION_CLASS
argument_list|,
name|HBaseReadWrite
operator|.
name|TEST_CONN
argument_list|)
expr_stmt|;
name|HBaseReadWrite
operator|.
name|setTestConnection
argument_list|(
name|hconn
argument_list|)
expr_stmt|;
comment|// HBaseReadWrite hbase = HBaseReadWrite.getInstance(conf);
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
block|}
end_class

end_unit

