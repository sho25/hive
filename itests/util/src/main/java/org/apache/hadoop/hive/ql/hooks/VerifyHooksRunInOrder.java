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
name|List
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
name|ql
operator|.
name|HiveDriverRunHook
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
name|HiveDriverRunHookContext
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
name|AbstractSemanticAnalyzerHook
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
name|session
operator|.
name|SessionState
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
operator|.
name|LogHelper
import|;
end_import

begin_comment
comment|/**  * VerifyHooksRunInOrder.  *  * Has to subclasses RunFirst and RunSecond which can be run as either pre or post hooks.  * Verifies that RunFirst is executed before RunSecond as the same type of hook.  I.e. if they are  * run as both Pre and Post hooks, RunSecond checks that RunFirst was run as a Pre or Post hook  * respectively.  *  * When running this, be sure to specify RunFirst before RunSecond in the configuration variable.  */
end_comment

begin_class
specifier|public
class|class
name|VerifyHooksRunInOrder
block|{
specifier|private
specifier|static
name|boolean
name|preHookRunFirstRan
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|postHookRunFirstRan
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|staticAnalysisPreHookFirstRan
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|staticAnalysisPostHookFirstRan
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|driverRunPreHookFirstRan
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|driverRunPostHookFirstRan
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
class|class
name|RunFirst
implements|implements
name|ExecuteWithHookContext
block|{
specifier|public
name|void
name|run
parameter_list|(
name|HookContext
name|hookContext
parameter_list|)
block|{
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
if|if
condition|(
name|console
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// This is simply to verify that the hooks were in fact run
name|console
operator|.
name|printError
argument_list|(
literal|"Running RunFirst for "
operator|+
name|hookContext
operator|.
name|getHookType
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|hookContext
operator|.
name|getHookType
argument_list|()
operator|==
name|HookType
operator|.
name|PRE_EXEC_HOOK
condition|)
block|{
name|preHookRunFirstRan
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|postHookRunFirstRan
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|RunSecond
implements|implements
name|ExecuteWithHookContext
block|{
specifier|public
name|void
name|run
parameter_list|(
name|HookContext
name|hookContext
parameter_list|)
throws|throws
name|Exception
block|{
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
if|if
condition|(
name|console
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// This is simply to verify that the hooks were in fact run
name|console
operator|.
name|printError
argument_list|(
literal|"Running RunSecond for "
operator|+
name|hookContext
operator|.
name|getHookType
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|hookContext
operator|.
name|getHookType
argument_list|()
operator|==
name|HookType
operator|.
name|PRE_EXEC_HOOK
condition|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Pre hooks did not run in the order specified."
argument_list|,
name|preHookRunFirstRan
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Post hooks did not run in the order specified."
argument_list|,
name|postHookRunFirstRan
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|RunFirstSemanticAnalysisHook
extends|extends
name|AbstractSemanticAnalyzerHook
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
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
if|if
condition|(
name|console
operator|==
literal|null
condition|)
block|{
return|return
name|ast
return|;
block|}
comment|// This is simply to verify that the hooks were in fact run
name|console
operator|.
name|printError
argument_list|(
literal|"Running RunFirst for Pre Analysis Hook"
argument_list|)
expr_stmt|;
name|staticAnalysisPreHookFirstRan
operator|=
literal|true
expr_stmt|;
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
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
if|if
condition|(
name|console
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// This is simply to verify that the hooks were in fact run
name|console
operator|.
name|printError
argument_list|(
literal|"Running RunFirst for Post Analysis Hook"
argument_list|)
expr_stmt|;
name|staticAnalysisPostHookFirstRan
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|RunSecondSemanticAnalysisHook
extends|extends
name|AbstractSemanticAnalyzerHook
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
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
if|if
condition|(
name|console
operator|==
literal|null
condition|)
block|{
return|return
name|ast
return|;
block|}
comment|// This is simply to verify that the hooks were in fact run
name|console
operator|.
name|printError
argument_list|(
literal|"Running RunSecond for Pre Analysis Hook"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Pre Analysis Hooks did not run in the order specified."
argument_list|,
name|staticAnalysisPreHookFirstRan
argument_list|)
expr_stmt|;
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
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
if|if
condition|(
name|console
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// This is simply to verify that the hooks were in fact run
name|console
operator|.
name|printError
argument_list|(
literal|"Running RunSecond for Post Analysis Hook"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Post Analysis Hooks did not run in the order specified."
argument_list|,
name|staticAnalysisPostHookFirstRan
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|RunFirstDriverRunHook
implements|implements
name|HiveDriverRunHook
block|{
annotation|@
name|Override
specifier|public
name|void
name|preDriverRun
parameter_list|(
name|HiveDriverRunHookContext
name|hookContext
parameter_list|)
throws|throws
name|Exception
block|{
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
if|if
condition|(
name|console
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// This is simply to verify that the hooks were in fact run
name|console
operator|.
name|printError
argument_list|(
literal|"Running RunFirst for Pre Driver Run Hook"
argument_list|)
expr_stmt|;
name|driverRunPreHookFirstRan
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postDriverRun
parameter_list|(
name|HiveDriverRunHookContext
name|hookContext
parameter_list|)
throws|throws
name|Exception
block|{
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
if|if
condition|(
name|console
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// This is simply to verify that the hooks were in fact run
name|console
operator|.
name|printError
argument_list|(
literal|"Running RunFirst for Post Driver Run Hook"
argument_list|)
expr_stmt|;
name|driverRunPostHookFirstRan
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|RunSecondDriverRunHook
implements|implements
name|HiveDriverRunHook
block|{
annotation|@
name|Override
specifier|public
name|void
name|preDriverRun
parameter_list|(
name|HiveDriverRunHookContext
name|hookContext
parameter_list|)
throws|throws
name|Exception
block|{
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
if|if
condition|(
name|console
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// This is simply to verify that the hooks were in fact run
name|console
operator|.
name|printError
argument_list|(
literal|"Running RunSecond for Pre Driver Run Hook"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Driver Run Hooks did not run in the order specified."
argument_list|,
name|driverRunPreHookFirstRan
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postDriverRun
parameter_list|(
name|HiveDriverRunHookContext
name|hookContext
parameter_list|)
throws|throws
name|Exception
block|{
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
if|if
condition|(
name|console
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// This is simply to verify that the hooks were in fact run
name|console
operator|.
name|printError
argument_list|(
literal|"Running RunSecond for Post Driver Run Hook"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Driver Run Hooks did not run in the order specified."
argument_list|,
name|driverRunPostHookFirstRan
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

