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
name|service
operator|.
name|cli
package|;
end_package

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
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|CLIServiceRestoreTest
block|{
name|CLIService
name|service
init|=
name|getService
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testRestore
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|SessionHandle
name|session
init|=
name|service
operator|.
name|openSession
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|service
operator|.
name|stop
argument_list|()
expr_stmt|;
name|service
operator|=
name|getService
argument_list|()
expr_stmt|;
try|try
block|{
name|service
operator|.
name|getSessionManager
argument_list|()
operator|.
name|getSession
argument_list|(
name|session
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"session already exists before restore"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Invalid SessionHandle"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|service
operator|.
name|createSessionWithSessionHandle
argument_list|(
name|session
argument_list|,
literal|"foo"
argument_list|,
literal|"bar"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|service
operator|.
name|getSessionManager
argument_list|()
operator|.
name|getSession
argument_list|(
name|session
argument_list|)
argument_list|)
expr_stmt|;
name|service
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CLIService
name|getService
parameter_list|()
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
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|)
expr_stmt|;
name|CLIService
name|service
init|=
operator|new
name|CLIService
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|service
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|service
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|service
return|;
block|}
block|}
end_class

end_unit

