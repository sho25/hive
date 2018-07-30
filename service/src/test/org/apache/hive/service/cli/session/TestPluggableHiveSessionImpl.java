begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|session
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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|CLIService
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
name|service
operator|.
name|cli
operator|.
name|HiveSQLException
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
name|service
operator|.
name|cli
operator|.
name|SessionHandle
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
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|ThriftBinaryCLIService
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
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|ThriftCLIServiceClient
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
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TProtocolVersion
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
name|TestPluggableHiveSessionImpl
block|{
annotation|@
name|Test
specifier|public
name|void
name|testSessionImpl
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
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
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
operator|.
name|getDefaultValue
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
name|HIVE_SESSION_IMPL_CLASSNAME
argument_list|,
name|SampleHiveSessionImpl
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|CLIService
name|cliService
init|=
operator|new
name|CLIService
argument_list|(
literal|null
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|cliService
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|ThriftBinaryCLIService
name|service
init|=
operator|new
name|ThriftBinaryCLIService
argument_list|(
name|cliService
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|service
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|ThriftCLIServiceClient
name|client
init|=
operator|new
name|ThriftCLIServiceClient
argument_list|(
name|service
argument_list|)
decl_stmt|;
name|SessionHandle
name|sessionHandle
init|=
literal|null
decl_stmt|;
name|sessionHandle
operator|=
name|client
operator|.
name|openSession
argument_list|(
literal|"tom"
argument_list|,
literal|"password"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|SampleHiveSessionImpl
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|service
operator|.
name|getHiveConf
argument_list|()
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SESSION_IMPL_CLASSNAME
argument_list|)
argument_list|)
expr_stmt|;
name|HiveSession
name|session
init|=
name|cliService
operator|.
name|getSessionManager
argument_list|()
operator|.
name|getSession
argument_list|(
name|sessionHandle
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|SampleHiveSessionImpl
operator|.
name|MAGIC_RETURN_VALUE
argument_list|,
name|session
operator|.
name|getNoOperationTime
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|closeSession
argument_list|(
name|sessionHandle
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSessionImplWithUGI
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
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
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
operator|.
name|getDefaultValue
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
name|HIVE_SESSION_IMPL_WITH_UGI_CLASSNAME
argument_list|,
name|SampleHiveSessionImplWithUGI
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|CLIService
name|cliService
init|=
operator|new
name|CLIService
argument_list|(
literal|null
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|cliService
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|ThriftBinaryCLIService
name|service
init|=
operator|new
name|ThriftBinaryCLIService
argument_list|(
name|cliService
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|service
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|ThriftCLIServiceClient
name|client
init|=
operator|new
name|ThriftCLIServiceClient
argument_list|(
name|service
argument_list|)
decl_stmt|;
name|SessionHandle
name|sessionHandle
init|=
literal|null
decl_stmt|;
name|sessionHandle
operator|=
name|client
operator|.
name|openSession
argument_list|(
literal|"tom"
argument_list|,
literal|"password"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|SampleHiveSessionImplWithUGI
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|service
operator|.
name|getHiveConf
argument_list|()
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SESSION_IMPL_WITH_UGI_CLASSNAME
argument_list|)
argument_list|)
expr_stmt|;
name|HiveSession
name|session
init|=
name|cliService
operator|.
name|getSessionManager
argument_list|()
operator|.
name|getSession
argument_list|(
name|sessionHandle
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|SampleHiveSessionImplWithUGI
operator|.
name|MAGIC_RETURN_VALUE
argument_list|,
name|session
operator|.
name|getNoOperationTime
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|closeSession
argument_list|(
name|sessionHandle
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|SampleHiveSessionImpl
extends|extends
name|HiveSessionImpl
block|{
specifier|public
specifier|static
specifier|final
name|int
name|MAGIC_RETURN_VALUE
init|=
literal|0xbeef0001
decl_stmt|;
specifier|public
name|SampleHiveSessionImpl
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|,
name|TProtocolVersion
name|protocol
parameter_list|,
name|String
name|username
parameter_list|,
name|String
name|password
parameter_list|,
name|HiveConf
name|serverhiveConf
parameter_list|,
name|String
name|ipAddress
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|forwardAddresses
parameter_list|)
block|{
name|super
argument_list|(
name|sessionHandle
argument_list|,
name|protocol
argument_list|,
name|username
argument_list|,
name|password
argument_list|,
name|serverhiveConf
argument_list|,
name|ipAddress
argument_list|,
name|forwardAddresses
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNoOperationTime
parameter_list|()
block|{
return|return
name|MAGIC_RETURN_VALUE
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|SampleHiveSessionImplWithUGI
extends|extends
name|HiveSessionImplwithUGI
block|{
specifier|public
specifier|static
specifier|final
name|int
name|MAGIC_RETURN_VALUE
init|=
literal|0xbeef0002
decl_stmt|;
specifier|public
name|SampleHiveSessionImplWithUGI
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|,
name|TProtocolVersion
name|protocol
parameter_list|,
name|String
name|username
parameter_list|,
name|String
name|password
parameter_list|,
name|HiveConf
name|serverhiveConf
parameter_list|,
name|String
name|ipAddress
parameter_list|,
name|String
name|delegationToken
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|forwardedAddresses
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|super
argument_list|(
name|sessionHandle
argument_list|,
name|protocol
argument_list|,
name|username
argument_list|,
name|password
argument_list|,
name|serverhiveConf
argument_list|,
name|ipAddress
argument_list|,
name|delegationToken
argument_list|,
name|forwardedAddresses
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNoOperationTime
parameter_list|()
block|{
return|return
name|MAGIC_RETURN_VALUE
return|;
block|}
block|}
block|}
end_class

end_unit

