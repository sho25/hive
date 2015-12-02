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
name|hbase
operator|.
name|HBaseConfiguration
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
name|HBaseAdmin
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
name|security
operator|.
name|SessionStateConfigUserAuthenticator
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|sqlstd
operator|.
name|SQLStdHiveAuthorizerFactoryForTest
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
name|HBaseIntegrationTests
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
name|HBaseIntegrationTests
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
name|HBaseAdmin
name|admin
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
argument_list|<>
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|HiveConf
name|conf
decl_stmt|;
specifier|protected
name|HBaseStore
name|store
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
name|String
name|connectionClassName
init|=
name|System
operator|.
name|getProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_HBASE_CONNECTION_CLASS
operator|.
name|varname
argument_list|)
decl_stmt|;
name|boolean
name|testingTephra
init|=
name|connectionClassName
operator|!=
literal|null
operator|&&
name|connectionClassName
operator|.
name|equals
argument_list|(
name|TephraHBaseConnection
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|testingTephra
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Testing with Tephra"
argument_list|)
expr_stmt|;
block|}
name|Configuration
name|hbaseConf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|hbaseConf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.info.port"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|utility
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|hbaseConf
argument_list|)
expr_stmt|;
name|utility
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|utility
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|HBaseIntegrationTests
operator|.
name|class
argument_list|)
expr_stmt|;
name|admin
operator|=
name|utility
operator|.
name|getHBaseAdmin
argument_list|()
expr_stmt|;
name|HBaseStoreTestUtil
operator|.
name|initHBaseMetastore
argument_list|(
name|admin
argument_list|,
literal|null
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
block|{    }
specifier|protected
name|void
name|setupDriver
parameter_list|()
block|{
comment|// This chicanery is necessary to make the driver work.  Hive tests need the pfile file
comment|// system, while the hbase one uses something else.  So first make sure we've configured our
comment|// hbase connection, then get a new config file and populate it as desired.
name|HBaseReadWrite
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
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
comment|// Setup so we can test SQL standard auth
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TEST_AUTHORIZATION_SQLSTD_HS2_MODE
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
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
name|SQLStdHiveAuthorizerFactoryForTest
operator|.
name|class
operator|.
name|getName
argument_list|()
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
name|HIVE_AUTHENTICATOR_MANAGER
argument_list|,
name|SessionStateConfigUserAuthenticator
operator|.
name|class
operator|.
name|getName
argument_list|()
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
name|HIVE_AUTHORIZATION_ENABLED
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
name|USERS_IN_ADMIN_ROLE
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
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
name|HIVEMAPREDMODE
argument_list|,
literal|"nonstrict"
argument_list|)
expr_stmt|;
comment|//HBaseReadWrite.setTestConnection(hconn);
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

