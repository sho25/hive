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
name|metadata
package|;
end_package

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
name|metastore
operator|.
name|Warehouse
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
name|processors
operator|.
name|CommandProcessorException
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
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * TestSemanticAnalyzerHookLoading.  */
end_comment

begin_class
specifier|public
class|class
name|TestSemanticAnalyzerHookLoading
block|{
annotation|@
name|Test
specifier|public
name|void
name|testHookLoading
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
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|SEMANTIC_ANALYZER_HOOK
operator|.
name|varname
argument_list|,
name|DummySemanticAnalyzerHook
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
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
name|conf
argument_list|)
expr_stmt|;
name|run
argument_list|(
literal|"drop table testDL"
argument_list|,
name|conf
argument_list|)
expr_stmt|;
try|try
block|{
name|run
argument_list|(
literal|"create table testDL (a int) as select * from tbl2"
argument_list|,
name|conf
argument_list|)
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|40000
argument_list|,
name|e
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|run
argument_list|(
literal|"create table testDL (a int)"
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
operator|.
name|getTable
argument_list|(
name|Warehouse
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
literal|"testDL"
argument_list|)
operator|.
name|getParameters
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|DummyCreateTableHook
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|params
operator|.
name|get
argument_list|(
literal|"createdBy"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Open Source rocks!!"
argument_list|,
name|params
operator|.
name|get
argument_list|(
literal|"Message"
argument_list|)
argument_list|)
expr_stmt|;
name|run
argument_list|(
literal|"drop table testDL"
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|run
parameter_list|(
name|String
name|command
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|CommandProcessorException
block|{
try|try
init|(
name|IDriver
name|driver
init|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|conf
argument_list|)
init|)
block|{
name|driver
operator|.
name|run
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

