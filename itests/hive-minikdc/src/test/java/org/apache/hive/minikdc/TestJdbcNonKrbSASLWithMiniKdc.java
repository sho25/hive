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
name|DriverManager
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
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|AuthenticationException
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
name|PasswdAuthenticationProvider
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
name|TestJdbcNonKrbSASLWithMiniKdc
extends|extends
name|TestJdbcWithMiniKdc
block|{
specifier|public
specifier|static
specifier|final
name|String
name|SASL_NONKRB_USER1
init|=
literal|"nonkrbuser"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SASL_NONKRB_USER2
init|=
literal|"nonkrbuser@realm.com"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SASL_NONKRB_PWD
init|=
literal|"mypwd"
decl_stmt|;
specifier|public
specifier|static
class|class
name|CustomAuthenticator
implements|implements
name|PasswdAuthenticationProvider
block|{
annotation|@
name|Override
specifier|public
name|void
name|Authenticate
parameter_list|(
name|String
name|user
parameter_list|,
name|String
name|password
parameter_list|)
throws|throws
name|AuthenticationException
block|{
if|if
condition|(
operator|!
operator|(
name|SASL_NONKRB_USER1
operator|.
name|equals
argument_list|(
name|user
argument_list|)
operator|&&
name|SASL_NONKRB_PWD
operator|.
name|equals
argument_list|(
name|password
argument_list|)
operator|)
operator|&&
operator|!
operator|(
name|SASL_NONKRB_USER2
operator|.
name|equals
argument_list|(
name|user
argument_list|)
operator|&&
name|SASL_NONKRB_PWD
operator|.
name|equals
argument_list|(
name|password
argument_list|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|AuthenticationException
argument_list|(
literal|"Authentication failed"
argument_list|)
throw|;
block|}
block|}
block|}
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
name|confOverlay
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_CUSTOM_AUTHENTICATION_CLASS
operator|.
name|varname
argument_list|,
name|CustomAuthenticator
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
name|getMiniHS2WithKerbWithRemoteHMS
argument_list|(
name|miniHiveKdc
argument_list|,
name|hiveConf
argument_list|,
literal|"CUSTOM"
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
comment|/***    * Test a nonkrb user could login the kerberized HS2 with authentication type SASL NONE    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testNonKrbSASLAuth
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|"default;user="
operator|+
name|SASL_NONKRB_USER1
operator|+
literal|";password="
operator|+
name|SASL_NONKRB_PWD
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
name|SESSION_USER_NAME
argument_list|,
name|SASL_NONKRB_USER1
argument_list|)
expr_stmt|;
name|hs2Conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/***    * Test a nonkrb user could login the kerberized HS2 with authentication type SASL NONE    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testNonKrbSASLFullNameAuth
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|"default;user="
operator|+
name|SASL_NONKRB_USER2
operator|+
literal|";password="
operator|+
name|SASL_NONKRB_PWD
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
name|SESSION_USER_NAME
argument_list|,
name|SASL_NONKRB_USER1
argument_list|)
expr_stmt|;
name|hs2Conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/***    * Negative test, verify that connection to secure HS2 fails if it is noSasl    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testNoSaslConnectionNeg
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|String
name|url
init|=
name|miniHS2
operator|.
name|getBaseJdbcURL
argument_list|()
operator|+
literal|"default;auth=noSasl"
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
literal|"noSasl connection should fail"
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
comment|/***    * Negative test, verify that NonKrb connection to secure HS2 fails if it is    * user/pwd do not match.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testNoKrbConnectionNeg
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|String
name|url
init|=
name|miniHS2
operator|.
name|getBaseJdbcURL
argument_list|()
operator|+
literal|"default;user=wronguser;pwd=mypwd"
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
literal|"noSasl connection should fail"
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
comment|/***    * Negative test for token based authentication    * Verify that token is not applicable to non-Kerberos SASL user    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testNoKrbSASLTokenAuthNeg
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|"default;user="
operator|+
name|SASL_NONKRB_USER1
operator|+
literal|";password="
operator|+
name|SASL_NONKRB_PWD
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
name|SESSION_USER_NAME
argument_list|,
name|SASL_NONKRB_USER1
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
name|HIVE_TEST_USER_1
argument_list|,
name|MiniHiveKdc
operator|.
name|HIVE_SERVICE_PRINCIPAL
argument_list|)
decl_stmt|;
name|fail
argument_list|(
name|SASL_NONKRB_USER1
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
literal|"Delegation token only supported over remote client with kerberos authentication"
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
block|}
end_class

end_unit

