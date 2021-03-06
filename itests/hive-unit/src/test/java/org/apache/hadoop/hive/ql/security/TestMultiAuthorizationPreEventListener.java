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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|security
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
name|security
operator|.
name|DummyHiveMetastoreAuthorizationProvider
operator|.
name|AuthCallContext
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
name|AuthorizationPreEventListener
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

begin_comment
comment|/**  * Test case for verifying that multiple  * {@link org.apache.hadoop.hive.metastore.AuthorizationPreEventListener}s can  * be set and they get called.  */
end_comment

begin_class
specifier|public
class|class
name|TestMultiAuthorizationPreEventListener
block|{
specifier|private
specifier|static
name|HiveConf
name|clientHiveConf
decl_stmt|;
specifier|private
specifier|static
name|HiveMetaStoreClient
name|msc
decl_stmt|;
specifier|private
specifier|static
name|IDriver
name|driver
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PRE_EVENT_LISTENERS
operator|.
name|varname
argument_list|,
name|AuthorizationPreEventListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Set two dummy classes as authorizatin managers. Two instances should get created.
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_AUTHORIZATION_MANAGER
operator|.
name|varname
argument_list|,
name|DummyHiveMetastoreAuthorizationProvider
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|","
operator|+
name|DummyHiveMetastoreAuthorizationProvider
operator|.
name|class
operator|.
name|getName
argument_list|()
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
name|HIVE_METASTORE_AUTHENTICATOR_MANAGER
operator|.
name|varname
argument_list|,
name|HadoopDefaultMetastoreAuthenticator
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
name|MetaStoreTestUtils
operator|.
name|startMetaStoreWithRetry
argument_list|()
decl_stmt|;
name|clientHiveConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|clientHiveConf
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
name|clientHiveConf
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
name|clientHiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|msc
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|clientHiveConf
argument_list|)
expr_stmt|;
name|driver
operator|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|clientHiveConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultipleAuthorizationListners
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|dbName
init|=
literal|"hive"
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|AuthCallContext
argument_list|>
name|authCalls
init|=
name|DummyHiveMetastoreAuthorizationProvider
operator|.
name|authCalls
decl_stmt|;
name|int
name|listSize
init|=
literal|0
decl_stmt|;
name|assertEquals
argument_list|(
name|listSize
argument_list|,
name|authCalls
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create database "
operator|+
name|dbName
argument_list|)
expr_stmt|;
comment|// verify that there are two calls because of two instances of the authorization provider
name|listSize
operator|=
literal|2
expr_stmt|;
name|assertEquals
argument_list|(
name|listSize
argument_list|,
name|authCalls
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// verify that the actual action also went through
name|Database
name|db
init|=
name|msc
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
name|listSize
operator|+=
literal|2
expr_stmt|;
comment|// 1 read database auth calls for each authorization provider
name|Database
name|dbFromEvent
init|=
operator|(
name|Database
operator|)
name|assertAndExtractSingleObjectFromEvent
argument_list|(
name|listSize
argument_list|,
name|authCalls
argument_list|,
name|DummyHiveMetastoreAuthorizationProvider
operator|.
name|AuthCallContextType
operator|.
name|DB
argument_list|)
decl_stmt|;
name|validateCreateDb
argument_list|(
name|db
argument_list|,
name|dbFromEvent
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Object
name|assertAndExtractSingleObjectFromEvent
parameter_list|(
name|int
name|listSize
parameter_list|,
name|List
argument_list|<
name|AuthCallContext
argument_list|>
name|authCalls
parameter_list|,
name|DummyHiveMetastoreAuthorizationProvider
operator|.
name|AuthCallContextType
name|callType
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|listSize
argument_list|,
name|authCalls
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|authCalls
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
operator|.
name|authObjects
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|callType
argument_list|,
name|authCalls
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
operator|.
name|type
argument_list|)
expr_stmt|;
return|return
operator|(
name|authCalls
operator|.
name|get
argument_list|(
name|listSize
operator|-
literal|1
argument_list|)
operator|.
name|authObjects
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
return|;
block|}
specifier|private
name|void
name|validateCreateDb
parameter_list|(
name|Database
name|expectedDb
parameter_list|,
name|Database
name|actualDb
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expectedDb
operator|.
name|getName
argument_list|()
argument_list|,
name|actualDb
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedDb
operator|.
name|getLocationUri
argument_list|()
argument_list|,
name|actualDb
operator|.
name|getLocationUri
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

