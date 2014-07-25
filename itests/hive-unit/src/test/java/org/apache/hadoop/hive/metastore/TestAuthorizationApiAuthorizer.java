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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
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
name|util
operator|.
name|ArrayList
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
name|lang3
operator|.
name|exception
operator|.
name|ExceptionUtils
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
name|HiveObjectPrivilege
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
name|HiveObjectRef
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
name|MetaException
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
name|PrincipalType
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
name|PrivilegeBag
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
name|Role
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
name|MetaStoreAuthzAPIAuthorizerEmbedOnly
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
name|shims
operator|.
name|ShimLoader
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
comment|/**  * Test case for {@link MetaStoreAuthzAPIAuthorizerEmbedOnly} The authorizer is  * supposed to allow api calls for metastore in embedded mode while disallowing  * them in remote metastore mode. Note that this is an abstract class, the  * subclasses that set the mode and the tests here get run as part of their  * testing.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|TestAuthorizationApiAuthorizer
block|{
specifier|protected
specifier|static
name|boolean
name|isRemoteMetastoreMode
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|static
name|HiveMetaStoreClient
name|msc
decl_stmt|;
specifier|protected
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Running with remoteMode = "
operator|+
name|isRemoteMetastoreMode
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"hive.metastore.pre.event.listeners"
argument_list|,
name|AuthorizationPreEventListener
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
literal|"hive.security.metastore.authorization.manager"
argument_list|,
name|MetaStoreAuthzAPIAuthorizerEmbedOnly
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
if|if
condition|(
name|isRemoteMetastoreMode
condition|)
block|{
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
block|}
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
name|msc
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
interface|interface
name|FunctionInvoker
block|{
specifier|public
name|void
name|invoke
parameter_list|()
throws|throws
name|Exception
function_decl|;
block|}
comment|/**    * Test the if authorization failed/passed for FunctionInvoker that invokes a metastore client    * api call    * @param mscFunctionInvoker    * @throws Exception    */
specifier|private
name|void
name|testFunction
parameter_list|(
name|FunctionInvoker
name|mscFunctionInvoker
parameter_list|)
throws|throws
name|Exception
block|{
name|boolean
name|caughtEx
init|=
literal|false
decl_stmt|;
try|try
block|{
try|try
block|{
name|mscFunctionInvoker
operator|.
name|invoke
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
comment|// A hack to verify that authorization check passed. Exception can be thrown be cause
comment|// the functions are not being called with valid params.
comment|// verify that exception has come from ObjectStore code, which means that the
comment|// authorization checks passed.
name|String
name|exStackString
init|=
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|e
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Verifying this exception came after authorization check"
argument_list|,
name|exStackString
operator|.
name|contains
argument_list|(
literal|"org.apache.hadoop.hive.metastore.ObjectStore"
argument_list|)
argument_list|)
expr_stmt|;
comment|// If its not an exception caused by auth check, ignore it
block|}
name|assertFalse
argument_list|(
literal|"Authz Exception should have been thrown in remote mode"
argument_list|,
name|isRemoteMetastoreMode
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"No auth exception thrown"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Caught exception"
argument_list|)
expr_stmt|;
name|caughtEx
operator|=
literal|true
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
name|MetaStoreAuthzAPIAuthorizerEmbedOnly
operator|.
name|errMsg
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|isRemoteMetastoreMode
condition|)
block|{
name|assertFalse
argument_list|(
literal|"No exception should be thrown in embedded mode"
argument_list|,
name|caughtEx
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGrantPriv
parameter_list|()
throws|throws
name|Exception
block|{
name|FunctionInvoker
name|invoker
init|=
operator|new
name|FunctionInvoker
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|invoke
parameter_list|()
throws|throws
name|Exception
block|{
name|msc
operator|.
name|grant_privileges
argument_list|(
operator|new
name|PrivilegeBag
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|HiveObjectPrivilege
argument_list|>
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|testFunction
argument_list|(
name|invoker
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRevokePriv
parameter_list|()
throws|throws
name|Exception
block|{
name|FunctionInvoker
name|invoker
init|=
operator|new
name|FunctionInvoker
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|invoke
parameter_list|()
throws|throws
name|Exception
block|{
name|msc
operator|.
name|revoke_privileges
argument_list|(
operator|new
name|PrivilegeBag
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|HiveObjectPrivilege
argument_list|>
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|testFunction
argument_list|(
name|invoker
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGrantRole
parameter_list|()
throws|throws
name|Exception
block|{
name|FunctionInvoker
name|invoker
init|=
operator|new
name|FunctionInvoker
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|invoke
parameter_list|()
throws|throws
name|Exception
block|{
name|msc
operator|.
name|grant_role
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|testFunction
argument_list|(
name|invoker
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRevokeRole
parameter_list|()
throws|throws
name|Exception
block|{
name|FunctionInvoker
name|invoker
init|=
operator|new
name|FunctionInvoker
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|invoke
parameter_list|()
throws|throws
name|Exception
block|{
name|msc
operator|.
name|revoke_role
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|testFunction
argument_list|(
name|invoker
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateRole
parameter_list|()
throws|throws
name|Exception
block|{
name|FunctionInvoker
name|invoker
init|=
operator|new
name|FunctionInvoker
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|invoke
parameter_list|()
throws|throws
name|Exception
block|{
name|msc
operator|.
name|create_role
argument_list|(
operator|new
name|Role
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|testFunction
argument_list|(
name|invoker
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDropRole
parameter_list|()
throws|throws
name|Exception
block|{
name|FunctionInvoker
name|invoker
init|=
operator|new
name|FunctionInvoker
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|invoke
parameter_list|()
throws|throws
name|Exception
block|{
name|msc
operator|.
name|drop_role
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|testFunction
argument_list|(
name|invoker
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListRoles
parameter_list|()
throws|throws
name|Exception
block|{
name|FunctionInvoker
name|invoker
init|=
operator|new
name|FunctionInvoker
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|invoke
parameter_list|()
throws|throws
name|Exception
block|{
name|msc
operator|.
name|list_roles
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|testFunction
argument_list|(
name|invoker
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetPrivSet
parameter_list|()
throws|throws
name|Exception
block|{
name|FunctionInvoker
name|invoker
init|=
operator|new
name|FunctionInvoker
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|invoke
parameter_list|()
throws|throws
name|Exception
block|{
name|msc
operator|.
name|get_privilege_set
argument_list|(
operator|new
name|HiveObjectRef
argument_list|()
argument_list|,
literal|null
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|testFunction
argument_list|(
name|invoker
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListPriv
parameter_list|()
throws|throws
name|Exception
block|{
name|FunctionInvoker
name|invoker
init|=
operator|new
name|FunctionInvoker
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|invoke
parameter_list|()
throws|throws
name|Exception
block|{
name|msc
operator|.
name|list_privileges
argument_list|(
literal|null
argument_list|,
name|PrincipalType
operator|.
name|USER
argument_list|,
operator|new
name|HiveObjectRef
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|testFunction
argument_list|(
name|invoker
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

