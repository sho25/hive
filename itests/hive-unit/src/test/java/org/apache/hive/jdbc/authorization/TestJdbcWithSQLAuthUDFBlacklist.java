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
name|jdbc
operator|.
name|authorization
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
name|ql
operator|.
name|security
operator|.
name|SessionStateUserAuthenticator
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
name|SQLStdHiveAuthorizerFactory
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
name|Test
import|;
end_import

begin_comment
comment|/**  * Test SQL standard authorization with jdbc/hiveserver2. Specifically udf blacklist behavior.  * This test needs to start minihs2 with specific configuration, so it is in a different  * test file from {@link TestJdbcWithSQLAuthorization}  */
end_comment

begin_class
specifier|public
class|class
name|TestJdbcWithSQLAuthUDFBlacklist
block|{
specifier|private
name|MiniHS2
name|miniHS2
init|=
literal|null
decl_stmt|;
specifier|public
name|void
name|startHS2
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
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
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
name|SQLStdHiveAuthorizerFactory
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
name|ConfVars
operator|.
name|HIVE_AUTHENTICATOR_MANAGER
argument_list|,
name|SessionStateUserAuthenticator
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
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|miniHS2
operator|=
operator|new
name|MiniHS2
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|start
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
block|}
annotation|@
name|After
specifier|public
name|void
name|shutDownHS2
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|miniHS2
operator|.
name|isStarted
argument_list|()
condition|)
block|{
name|miniHS2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBlackListedUdfUsage
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_BUILTIN_UDF_BLACKLIST
argument_list|,
literal|"sqrt"
argument_list|)
expr_stmt|;
name|startHS2
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// create tables as user1
name|Connection
name|hs2Conn
init|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|miniHS2
operator|.
name|getJdbcURL
argument_list|()
argument_list|,
literal|"user1"
argument_list|,
literal|"bar"
argument_list|)
decl_stmt|;
name|Statement
name|stmt
init|=
name|hs2Conn
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|String
name|tableName1
init|=
literal|"test_jdbc_sql_auth_udf_blacklist"
decl_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"create table "
operator|+
name|tableName1
operator|+
literal|"(i int) "
argument_list|)
expr_stmt|;
name|verifyUDFNotAllowed
argument_list|(
name|stmt
argument_list|,
name|tableName1
argument_list|,
literal|"sqrt(1)"
argument_list|,
literal|"sqrt"
argument_list|)
expr_stmt|;
comment|// verify udf reflect is allowed (no exception will be thrown)
name|stmt
operator|.
name|execute
argument_list|(
literal|"SELECT reflect('java.lang.String', 'valueOf', 1) from "
operator|+
name|tableName1
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|close
argument_list|()
expr_stmt|;
name|hs2Conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|verifyUDFNotAllowed
parameter_list|(
name|Statement
name|stmt
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|udfcall
parameter_list|,
name|String
name|udfname
parameter_list|)
block|{
try|try
block|{
name|stmt
operator|.
name|execute
argument_list|(
literal|"SELECT "
operator|+
name|udfcall
operator|+
literal|" from "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Disallowed udf usage should have resulted in error"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|checkAssertContains
argument_list|(
literal|"UDF "
operator|+
name|udfname
operator|+
literal|" is not allowed"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|checkAssertContains
parameter_list|(
name|String
name|expectedSubString
parameter_list|,
name|String
name|message
parameter_list|)
block|{
if|if
condition|(
name|message
operator|.
name|contains
argument_list|(
name|expectedSubString
argument_list|)
condition|)
block|{
return|return;
block|}
name|fail
argument_list|(
literal|"Message ["
operator|+
name|message
operator|+
literal|"] does not contain substring ["
operator|+
name|expectedSubString
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

