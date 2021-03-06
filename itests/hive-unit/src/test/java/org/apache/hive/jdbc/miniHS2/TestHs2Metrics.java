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
name|hive
operator|.
name|jdbc
operator|.
name|miniHS2
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
name|common
operator|.
name|metrics
operator|.
name|MetricsTestUtils
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
name|common
operator|.
name|metrics
operator|.
name|common
operator|.
name|MetricsFactory
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
name|common
operator|.
name|metrics
operator|.
name|metrics2
operator|.
name|CodahaleMetrics
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
name|exec
operator|.
name|Task
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
name|parse
operator|.
name|ASTNode
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
name|parse
operator|.
name|HiveSemanticAnalyzerHook
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
name|parse
operator|.
name|HiveSemanticAnalyzerHookContext
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
name|parse
operator|.
name|SemanticException
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
name|CLIServiceClient
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|List
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
comment|/**  * Tests HiveServer2 metrics.  */
end_comment

begin_class
specifier|public
class|class
name|TestHs2Metrics
block|{
specifier|private
specifier|static
name|MiniHS2
name|miniHS2
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
decl_stmt|;
comment|//Check metrics during semantic analysis.
specifier|public
specifier|static
class|class
name|MetricCheckingHook
implements|implements
name|HiveSemanticAnalyzerHook
block|{
annotation|@
name|Override
specifier|public
name|ASTNode
name|preAnalyze
parameter_list|(
name|HiveSemanticAnalyzerHookContext
name|context
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
name|CodahaleMetrics
name|metrics
init|=
operator|(
name|CodahaleMetrics
operator|)
name|MetricsFactory
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|String
name|json
init|=
name|metrics
operator|.
name|dumpJson
argument_list|()
decl_stmt|;
comment|//Pre-analyze hook is fired in the middle of these calls
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
literal|"active_calls_api_semanticAnalyze"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
literal|"active_calls_api_compile"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
literal|"active_calls_api_hs2_operation_RUNNING"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
literal|"active_calls_api_hs2_sql_operation_RUNNING"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"metrics verification failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|ast
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postAnalyze
parameter_list|(
name|HiveSemanticAnalyzerHookContext
name|context
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|rootTasks
parameter_list|)
throws|throws
name|SemanticException
block|{     }
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|miniHS2
operator|=
operator|new
name|MiniHS2
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|)
expr_stmt|;
name|confOverlay
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|confOverlay
operator|.
name|put
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
name|confOverlay
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SEMANTIC_ANALYZER_HOOK
operator|.
name|varname
argument_list|,
name|MetricCheckingHook
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
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_METRICS_ENABLED
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|confOverlay
operator|.
name|put
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
name|before
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
name|MetricsFactory
operator|.
name|close
argument_list|()
expr_stmt|;
name|MetricsFactory
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMetrics
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|tableName
init|=
literal|"testMetrics"
decl_stmt|;
name|CLIServiceClient
name|serviceClient
init|=
name|miniHS2
operator|.
name|getServiceClient
argument_list|()
decl_stmt|;
name|SessionHandle
name|sessHandle
init|=
name|serviceClient
operator|.
name|openSession
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
decl_stmt|;
comment|//Block on semantic analysis to check 'active_calls'
name|serviceClient
operator|.
name|executeStatement
argument_list|(
name|sessHandle
argument_list|,
literal|"CREATE TABLE "
operator|+
name|tableName
operator|+
literal|" (id INT)"
argument_list|,
name|confOverlay
argument_list|)
expr_stmt|;
comment|//check that all calls were recorded.
name|CodahaleMetrics
name|metrics
init|=
operator|(
name|CodahaleMetrics
operator|)
name|MetricsFactory
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|String
name|json
init|=
name|metrics
operator|.
name|dumpJson
argument_list|()
decl_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|TIMER
argument_list|,
literal|"api_hs2_operation_INITIALIZED"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|TIMER
argument_list|,
literal|"api_hs2_operation_PENDING"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|TIMER
argument_list|,
literal|"api_hs2_operation_RUNNING"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
literal|"hs2_completed_operation_FINISHED"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|TIMER
argument_list|,
literal|"api_hs2_sql_operation_PENDING"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|TIMER
argument_list|,
literal|"api_hs2_sql_operation_RUNNING"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
literal|"hs2_completed_sql_operation_FINISHED"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|//but there should be no more active calls.
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
literal|"active_calls_api_semanticAnalyze"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
literal|"active_calls_api_compile"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
literal|"active_calls_api_hs2_operation_RUNNING"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
literal|"active_calls_api_hs2_sql_operation_RUNNING"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|serviceClient
operator|.
name|closeSession
argument_list|(
name|sessHandle
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClosedScopes
parameter_list|()
throws|throws
name|Exception
block|{
name|CLIServiceClient
name|serviceClient
init|=
name|miniHS2
operator|.
name|getServiceClient
argument_list|()
decl_stmt|;
name|SessionHandle
name|sessHandle
init|=
name|serviceClient
operator|.
name|openSession
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
decl_stmt|;
comment|//this should error at analyze scope
name|Exception
name|expectedException
init|=
literal|null
decl_stmt|;
try|try
block|{
name|serviceClient
operator|.
name|executeStatement
argument_list|(
name|sessHandle
argument_list|,
literal|"select aaa"
argument_list|,
name|confOverlay
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|expectedException
operator|=
name|e
expr_stmt|;
block|}
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"Expected semantic exception"
argument_list|,
name|expectedException
argument_list|)
expr_stmt|;
comment|//verify all scopes were recorded
name|CodahaleMetrics
name|metrics
init|=
operator|(
name|CodahaleMetrics
operator|)
name|MetricsFactory
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|String
name|json
init|=
name|metrics
operator|.
name|dumpJson
argument_list|()
decl_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|TIMER
argument_list|,
literal|"api_parse"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|TIMER
argument_list|,
literal|"api_semanticAnalyze"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|//verify all scopes are closed.
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
literal|"active_calls_api_parse"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
literal|"active_calls_api_semanticAnalyze"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|serviceClient
operator|.
name|closeSession
argument_list|(
name|sessHandle
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

