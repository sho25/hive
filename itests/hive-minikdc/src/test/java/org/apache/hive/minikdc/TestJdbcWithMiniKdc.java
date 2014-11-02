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
name|hive
operator|.
name|minikdc
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|DriverManager
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|ResultSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Statement
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|security
operator|.
name|UserGroupInformation
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
name|jdbc
operator|.
name|HiveConnection
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
name|jdbc
operator|.
name|miniHS2
operator|.
name|MiniHS2
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
name|auth
operator|.
name|HiveAuthFactory
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
name|session
operator|.
name|HiveSessionHook
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
name|session
operator|.
name|HiveSessionHookContext
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
name|AfterClass
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
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestJdbcWithMiniKdc
block|{
comment|// Need to hive.server2.session.hook to SessionHookTest in hive-site
specifier|public
specifier|static
specifier|final
name|String
name|SESSION_USER_NAME
init|=
literal|"proxy.test.session.user"
decl_stmt|;
comment|// set current user in session conf
specifier|public
specifier|static
class|class
name|SessionHookTest
implements|implements
name|HiveSessionHook
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|HiveSessionHookContext
name|sessionHookContext
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|sessionHookContext
operator|.
name|getSessionConf
argument_list|()
operator|.
name|set
argument_list|(
name|SESSION_USER_NAME
argument_list|,
name|sessionHookContext
operator|.
name|getSessionUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|MiniHS2
name|miniHS2
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|MiniHiveKdc
name|miniHiveKdc
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
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
specifier|private
name|Connection
name|hs2Conn
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeTest
parameter_list|()
throws|throws
name|Exception
block|{
name|Class
operator|.
name|forName
argument_list|(
name|MiniHS2
operator|.
name|getJdbcDriverName
argument_list|()
argument_list|)
expr_stmt|;
name|confOverlay
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_SESSION_HOOK
operator|.
name|varname
argument_list|,
name|SessionHookTest
operator|.
name|class
operator|.
name|getName
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
name|miniHiveKdc
operator|=
name|MiniHiveKdc
operator|.
name|getMiniHiveKdc
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|miniHS2
operator|=
name|MiniHiveKdc
operator|.
name|getMiniHS2WithKerb
argument_list|(
name|miniHiveKdc
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{   }
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|hs2Conn
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|hs2Conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Ignore shutdown errors since there are negative tests
block|}
block|}
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterTest
parameter_list|()
throws|throws
name|Exception
block|{
name|miniHS2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
comment|/***    * Basic connection test    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testConnection
parameter_list|()
throws|throws
name|Exception
block|{
name|miniHiveKdc
operator|.
name|loginUser
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_TEST_USER_1
argument_list|)
expr_stmt|;
name|hs2Conn
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|miniHS2
operator|.
name|getJdbcURL
argument_list|()
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
name|SESSION_USER_NAME
argument_list|,
name|MiniHiveKdc
operator|.
name|HIVE_TEST_USER_1
argument_list|)
expr_stmt|;
block|}
comment|/***    * Negative test, verify that connection to secure HS2 fails when    * required connection attributes are not provided    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testConnectionNeg
parameter_list|()
throws|throws
name|Exception
block|{
name|miniHiveKdc
operator|.
name|loginUser
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_TEST_USER_1
argument_list|)
expr_stmt|;
try|try
block|{
name|String
name|url
init|=
name|miniHS2
operator|.
name|getJdbcURL
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|";principal.*"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|hs2Conn
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|url
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"NON kerberos connection should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
comment|// expected error
name|assertEquals
argument_list|(
literal|"08S01"
argument_list|,
name|e
operator|.
name|getSQLState
argument_list|()
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/***    * Test token based authentication over kerberos    * Login as super user and retrieve the token for normal user    * use the token to connect connect as normal user    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testTokenAuth
parameter_list|()
throws|throws
name|Exception
block|{
name|miniHiveKdc
operator|.
name|loginUser
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_TEST_SUPER_USER
argument_list|)
expr_stmt|;
name|hs2Conn
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|miniHS2
operator|.
name|getJdbcURL
argument_list|()
argument_list|)
expr_stmt|;
comment|// retrieve token and store in the cache
name|String
name|token
init|=
operator|(
operator|(
name|HiveConnection
operator|)
name|hs2Conn
operator|)
operator|.
name|getDelegationToken
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_TEST_USER_1
argument_list|,
name|MiniHiveKdc
operator|.
name|HIVE_SERVICE_PRINCIPAL
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|token
operator|!=
literal|null
operator|&&
operator|!
name|token
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|hs2Conn
operator|.
name|close
argument_list|()
expr_stmt|;
name|UserGroupInformation
name|ugi
init|=
name|miniHiveKdc
operator|.
name|loginUser
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_TEST_USER_1
argument_list|)
decl_stmt|;
comment|// Store token in the cache
name|storeToken
argument_list|(
name|token
argument_list|,
name|ugi
argument_list|)
expr_stmt|;
name|hs2Conn
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|miniHS2
operator|.
name|getBaseJdbcURL
argument_list|()
operator|+
literal|"default;auth=delegationToken"
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
name|SESSION_USER_NAME
argument_list|,
name|MiniHiveKdc
operator|.
name|HIVE_TEST_USER_1
argument_list|)
expr_stmt|;
block|}
comment|/***    * Negative test for token based authentication    * Verify that a user can't retrieve a token for user that    * it's not allowed to impersonate    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testNegativeTokenAuth
parameter_list|()
throws|throws
name|Exception
block|{
name|miniHiveKdc
operator|.
name|loginUser
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_TEST_SUPER_USER
argument_list|)
expr_stmt|;
name|hs2Conn
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|miniHS2
operator|.
name|getJdbcURL
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
comment|// retrieve token and store in the cache
name|String
name|token
init|=
operator|(
operator|(
name|HiveConnection
operator|)
name|hs2Conn
operator|)
operator|.
name|getDelegationToken
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_TEST_USER_2
argument_list|,
name|MiniHiveKdc
operator|.
name|HIVE_SERVICE_PRINCIPAL
argument_list|)
decl_stmt|;
name|fail
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_TEST_SUPER_USER
operator|+
literal|" shouldn't be allowed to retrieve token for "
operator|+
name|MiniHiveKdc
operator|.
name|HIVE_TEST_USER_2
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
comment|// Expected error
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Error retrieving delegation token for user"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"is not allowed to impersonate"
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|hs2Conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Test connection using the proxy user connection property    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testProxyAuth
parameter_list|()
throws|throws
name|Exception
block|{
name|miniHiveKdc
operator|.
name|loginUser
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_TEST_SUPER_USER
argument_list|)
expr_stmt|;
name|hs2Conn
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|miniHS2
operator|.
name|getJdbcURL
argument_list|(
literal|"default"
argument_list|,
literal|";hive.server2.proxy.user="
operator|+
name|MiniHiveKdc
operator|.
name|HIVE_TEST_USER_1
argument_list|)
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
name|SESSION_USER_NAME
argument_list|,
name|MiniHiveKdc
operator|.
name|HIVE_TEST_USER_1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test connection using the proxy user connection property.    * Verify proxy connection fails when super user doesn't have privilege to    * impersonate the given user    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testNegativeProxyAuth
parameter_list|()
throws|throws
name|Exception
block|{
name|miniHiveKdc
operator|.
name|loginUser
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_TEST_SUPER_USER
argument_list|)
expr_stmt|;
try|try
block|{
name|hs2Conn
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|miniHS2
operator|.
name|getJdbcURL
argument_list|(
literal|"default"
argument_list|,
literal|";hive.server2.proxy.user="
operator|+
name|MiniHiveKdc
operator|.
name|HIVE_TEST_USER_2
argument_list|)
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
name|SESSION_USER_NAME
argument_list|,
name|MiniHiveKdc
operator|.
name|HIVE_TEST_USER_2
argument_list|)
expr_stmt|;
name|fail
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_TEST_SUPER_USER
operator|+
literal|" shouldn't be allowed proxy connection for "
operator|+
name|MiniHiveKdc
operator|.
name|HIVE_TEST_USER_2
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
comment|// Expected error
name|e
operator|.
name|printStackTrace
argument_list|()
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
literal|"Failed to validate proxy privilege"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"is not allowed to impersonate"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Verify the config property value    * @param propertyName    * @param expectedValue    * @throws Exception    */
specifier|private
name|void
name|verifyProperty
parameter_list|(
name|String
name|propertyName
parameter_list|,
name|String
name|expectedValue
parameter_list|)
throws|throws
name|Exception
block|{
name|Statement
name|stmt
init|=
name|hs2Conn
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|ResultSet
name|res
init|=
name|stmt
operator|.
name|executeQuery
argument_list|(
literal|"set "
operator|+
name|propertyName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|res
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|results
index|[]
init|=
name|res
operator|.
name|getString
argument_list|(
literal|1
argument_list|)
operator|.
name|split
argument_list|(
literal|"="
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Property should be set"
argument_list|,
name|results
operator|.
name|length
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Property should be set"
argument_list|,
name|expectedValue
argument_list|,
name|results
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
comment|// Store the given token in the UGI
specifier|private
name|void
name|storeToken
parameter_list|(
name|String
name|tokenStr
parameter_list|,
name|UserGroupInformation
name|ugi
parameter_list|)
throws|throws
name|Exception
block|{
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|setTokenStr
argument_list|(
name|ugi
argument_list|,
name|tokenStr
argument_list|,
name|HiveAuthFactory
operator|.
name|HS2_CLIENT_TOKEN
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

