begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|//The tests here are heavily based on some timing, so there is some chance to fail.
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
name|hooks
package|;
end_package

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
name|lang
operator|.
name|Override
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|hooks
operator|.
name|ExecuteWithHookContext
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
name|hooks
operator|.
name|HookContext
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
name|hooks
operator|.
name|HookContext
operator|.
name|HookType
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|plan
operator|.
name|HiveOperation
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
name|service
operator|.
name|server
operator|.
name|HiveServer2
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Tests information retrieved from hooks.  */
end_comment

begin_class
specifier|public
class|class
name|TestHs2Hooks
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestHs2Hooks
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HiveServer2
name|hiveServer2
decl_stmt|;
specifier|public
specifier|static
class|class
name|PostExecHook
implements|implements
name|ExecuteWithHookContext
block|{
specifier|public
specifier|static
name|String
name|userName
decl_stmt|;
specifier|public
specifier|static
name|String
name|ipAddress
decl_stmt|;
specifier|public
specifier|static
name|String
name|operation
decl_stmt|;
specifier|public
specifier|static
name|Throwable
name|error
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|HookContext
name|hookContext
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|hookContext
operator|.
name|getHookType
argument_list|()
operator|.
name|equals
argument_list|(
name|HookType
operator|.
name|POST_EXEC_HOOK
argument_list|)
condition|)
block|{
name|ipAddress
operator|=
name|hookContext
operator|.
name|getIpAddress
argument_list|()
expr_stmt|;
name|userName
operator|=
name|hookContext
operator|.
name|getUserName
argument_list|()
expr_stmt|;
name|operation
operator|=
name|hookContext
operator|.
name|getOperationName
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error in PostExecHook: "
operator|+
name|t
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|error
operator|=
name|t
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|PreExecHook
implements|implements
name|ExecuteWithHookContext
block|{
specifier|public
specifier|static
name|String
name|userName
decl_stmt|;
specifier|public
specifier|static
name|String
name|ipAddress
decl_stmt|;
specifier|public
specifier|static
name|String
name|operation
decl_stmt|;
specifier|public
specifier|static
name|Throwable
name|error
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|HookContext
name|hookContext
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|hookContext
operator|.
name|getHookType
argument_list|()
operator|.
name|equals
argument_list|(
name|HookType
operator|.
name|PRE_EXEC_HOOK
argument_list|)
condition|)
block|{
name|ipAddress
operator|=
name|hookContext
operator|.
name|getIpAddress
argument_list|()
expr_stmt|;
name|userName
operator|=
name|hookContext
operator|.
name|getUserName
argument_list|()
expr_stmt|;
name|operation
operator|=
name|hookContext
operator|.
name|getOperationName
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error in PreExecHook: "
operator|+
name|t
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|error
operator|=
name|t
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|SemanticAnalysisHook
implements|implements
name|HiveSemanticAnalyzerHook
block|{
specifier|public
specifier|static
name|String
name|userName
decl_stmt|;
specifier|public
specifier|static
name|String
name|command
decl_stmt|;
specifier|public
specifier|static
name|HiveOperation
name|commandType
decl_stmt|;
specifier|public
specifier|static
name|String
name|ipAddress
decl_stmt|;
specifier|public
specifier|static
name|Throwable
name|preAnalyzeError
decl_stmt|;
specifier|public
specifier|static
name|Throwable
name|postAnalyzeError
decl_stmt|;
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
name|userName
operator|=
name|context
operator|.
name|getUserName
argument_list|()
expr_stmt|;
name|ipAddress
operator|=
name|context
operator|.
name|getIpAddress
argument_list|()
expr_stmt|;
name|command
operator|=
name|context
operator|.
name|getCommand
argument_list|()
expr_stmt|;
name|commandType
operator|=
name|context
operator|.
name|getHiveOperation
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error in semantic analysis hook preAnalyze: "
operator|+
name|t
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|preAnalyzeError
operator|=
name|t
expr_stmt|;
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
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
name|userName
operator|=
name|context
operator|.
name|getUserName
argument_list|()
expr_stmt|;
name|ipAddress
operator|=
name|context
operator|.
name|getIpAddress
argument_list|()
expr_stmt|;
name|command
operator|=
name|context
operator|.
name|getCommand
argument_list|()
expr_stmt|;
name|commandType
operator|=
name|context
operator|.
name|getHiveOperation
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error in semantic analysis hook postAnalyze: "
operator|+
name|t
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|postAnalyzeError
operator|=
name|t
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
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
name|ConfVars
operator|.
name|PREEXECHOOKS
argument_list|,
name|PreExecHook
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|POSTEXECHOOKS
argument_list|,
name|PostExecHook
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|SEMANTIC_ANALYZER_HOOK
argument_list|,
name|SemanticAnalysisHook
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
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|hiveServer2
operator|=
operator|new
name|HiveServer2
argument_list|()
expr_stmt|;
name|hiveServer2
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|hiveServer2
operator|.
name|start
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|hiveServer2
operator|!=
literal|null
condition|)
block|{
name|hiveServer2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUpTest
parameter_list|()
throws|throws
name|Exception
block|{
name|PreExecHook
operator|.
name|userName
operator|=
literal|null
expr_stmt|;
name|PreExecHook
operator|.
name|ipAddress
operator|=
literal|null
expr_stmt|;
name|PreExecHook
operator|.
name|operation
operator|=
literal|null
expr_stmt|;
name|PreExecHook
operator|.
name|error
operator|=
literal|null
expr_stmt|;
name|PostExecHook
operator|.
name|userName
operator|=
literal|null
expr_stmt|;
name|PostExecHook
operator|.
name|ipAddress
operator|=
literal|null
expr_stmt|;
name|PostExecHook
operator|.
name|operation
operator|=
literal|null
expr_stmt|;
name|PostExecHook
operator|.
name|error
operator|=
literal|null
expr_stmt|;
name|SemanticAnalysisHook
operator|.
name|userName
operator|=
literal|null
expr_stmt|;
name|SemanticAnalysisHook
operator|.
name|ipAddress
operator|=
literal|null
expr_stmt|;
name|SemanticAnalysisHook
operator|.
name|command
operator|=
literal|null
expr_stmt|;
name|SemanticAnalysisHook
operator|.
name|commandType
operator|=
literal|null
expr_stmt|;
name|SemanticAnalysisHook
operator|.
name|preAnalyzeError
operator|=
literal|null
expr_stmt|;
name|SemanticAnalysisHook
operator|.
name|postAnalyzeError
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Test that hook context properties are correctly set.    */
annotation|@
name|Test
specifier|public
name|void
name|testHookContexts
parameter_list|()
throws|throws
name|Throwable
block|{
name|Properties
name|connProp
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|connProp
operator|.
name|setProperty
argument_list|(
literal|"user"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
expr_stmt|;
name|connProp
operator|.
name|setProperty
argument_list|(
literal|"password"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|HiveConnection
name|connection
init|=
operator|new
name|HiveConnection
argument_list|(
literal|"jdbc:hive2://localhost:10000/default"
argument_list|,
name|connProp
argument_list|)
decl_stmt|;
name|Statement
name|stmt
init|=
name|connection
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|stmt
operator|.
name|executeQuery
argument_list|(
literal|"show databases"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|executeQuery
argument_list|(
literal|"show tables"
argument_list|)
expr_stmt|;
name|Throwable
name|error
init|=
name|PostExecHook
operator|.
name|error
decl_stmt|;
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
throw|throw
name|error
throw|;
block|}
name|error
operator|=
name|PreExecHook
operator|.
name|error
expr_stmt|;
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
throw|throw
name|error
throw|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|,
name|PostExecHook
operator|.
name|userName
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|PostExecHook
operator|.
name|ipAddress
argument_list|,
literal|"ipaddress is null"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|PostExecHook
operator|.
name|userName
argument_list|,
literal|"userName is null"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|PostExecHook
operator|.
name|operation
argument_list|,
literal|"operation is null"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|PostExecHook
operator|.
name|ipAddress
argument_list|,
name|PostExecHook
operator|.
name|ipAddress
operator|.
name|contains
argument_list|(
literal|"127.0.0.1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"SHOWTABLES"
argument_list|,
name|PostExecHook
operator|.
name|operation
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|,
name|PreExecHook
operator|.
name|userName
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"ipaddress is null"
argument_list|,
name|PreExecHook
operator|.
name|ipAddress
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"userName is null"
argument_list|,
name|PreExecHook
operator|.
name|userName
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"operation is null"
argument_list|,
name|PreExecHook
operator|.
name|operation
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|PreExecHook
operator|.
name|ipAddress
argument_list|,
name|PreExecHook
operator|.
name|ipAddress
operator|.
name|contains
argument_list|(
literal|"127.0.0.1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"SHOWTABLES"
argument_list|,
name|PreExecHook
operator|.
name|operation
argument_list|)
expr_stmt|;
name|error
operator|=
name|SemanticAnalysisHook
operator|.
name|preAnalyzeError
expr_stmt|;
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
throw|throw
name|error
throw|;
block|}
name|error
operator|=
name|SemanticAnalysisHook
operator|.
name|postAnalyzeError
expr_stmt|;
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
throw|throw
name|error
throw|;
block|}
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"semantic hook context ipaddress is null"
argument_list|,
name|SemanticAnalysisHook
operator|.
name|ipAddress
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"semantic hook context userName is null"
argument_list|,
name|SemanticAnalysisHook
operator|.
name|userName
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"semantic hook context command is null"
argument_list|,
name|SemanticAnalysisHook
operator|.
name|command
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"semantic hook context commandType is null"
argument_list|,
name|SemanticAnalysisHook
operator|.
name|commandType
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|SemanticAnalysisHook
operator|.
name|ipAddress
argument_list|,
name|SemanticAnalysisHook
operator|.
name|ipAddress
operator|.
name|contains
argument_list|(
literal|"127.0.0.1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"show tables"
argument_list|,
name|SemanticAnalysisHook
operator|.
name|command
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

