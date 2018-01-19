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
name|hooks
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
name|AfterClass
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
name|TestHooks
block|{
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|onetimeSetup
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|TestHooks
operator|.
name|class
argument_list|)
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
name|Driver
name|driver
init|=
name|createDriver
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|int
name|ret
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"create table t1(i int)"
argument_list|)
operator|.
name|getResponseCode
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Checking command success"
argument_list|,
literal|0
argument_list|,
name|ret
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|onetimeTeardown
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|TestHooks
operator|.
name|class
argument_list|)
decl_stmt|;
name|Driver
name|driver
init|=
name|createDriver
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop table t1"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{   }
annotation|@
name|Test
specifier|public
name|void
name|testRedactLogString
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|TestHooks
operator|.
name|class
argument_list|)
decl_stmt|;
name|String
name|str
decl_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|QUERYREDACTORHOOKS
argument_list|,
name|SimpleQueryRedactor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|str
operator|=
name|HookUtils
operator|.
name|redactLogString
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|str
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|str
operator|=
name|HookUtils
operator|.
name|redactLogString
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|str
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|str
operator|=
name|HookUtils
operator|.
name|redactLogString
argument_list|(
name|conf
argument_list|,
literal|"select 'XXX' from t1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|str
argument_list|,
literal|"select 'AAA' from t1"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQueryRedactor
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|TestHooks
operator|.
name|class
argument_list|)
decl_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|QUERYREDACTORHOOKS
argument_list|,
name|SimpleQueryRedactor
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
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|)
expr_stmt|;
name|Driver
name|driver
init|=
name|createDriver
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|int
name|ret
init|=
name|driver
operator|.
name|compile
argument_list|(
literal|"select 'XXX' from t1"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Checking command success"
argument_list|,
literal|0
argument_list|,
name|ret
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"select 'AAA' from t1"
argument_list|,
name|conf
operator|.
name|getQueryString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|SimpleQueryRedactor
extends|extends
name|Redactor
block|{
annotation|@
name|Override
specifier|public
name|String
name|redactQuery
parameter_list|(
name|String
name|query
parameter_list|)
block|{
return|return
name|query
operator|.
name|replaceAll
argument_list|(
literal|"XXX"
argument_list|,
literal|"AAA"
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
name|Driver
name|createDriver
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Driver
name|driver
init|=
operator|new
name|Driver
argument_list|(
name|conf
argument_list|)
decl_stmt|;
return|return
name|driver
return|;
block|}
block|}
end_class

end_unit

