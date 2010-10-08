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
name|parse
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

begin_comment
comment|/**  * HiveSemanticAnalyzerHook allows Hive to be extended with custom  * logic for semantic analysis of QL statements.  This interface  * and any Hive internals it exposes are currently  * "limited private and evolving" (unless otherwise stated elsewhere)  * and intended mainly for use by the Howl project.  *  *<p>  *  * Note that the lifetime of an instantiated hook object is scoped to  * the analysis of a single statement; hook instances are never reused.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveSemanticAnalyzerHook
block|{
comment|/**    * Invoked before Hive performs its own semantic analysis on    * a statement.  The implementation may inspect the statement AST and    * prevent its execution by throwing a SemanticException.    * Optionally, it may also augment/rewrite the AST, but must produce    * a form equivalent to one which could have    * been returned directly from Hive's own parser.    *    * @param context context information for semantic analysis    *    * @param ast AST being analyzed and optionally rewritten    *    * @return replacement AST (typically the same as the original AST unless the    * entire tree had to be replaced; must not be null)    */
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
function_decl|;
comment|/**    * Invoked after Hive performs its own semantic analysis on a    * statement (including optimization).    * Hive calls postAnalyze on the same hook object    * as preAnalyze, so the hook can maintain state across the calls.    *    * @param context context information for semantic analysis    * @param rootTasks root tasks produced by semantic analysis;    * the hook is free to modify this list or its contents    */
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
function_decl|;
block|}
end_interface

end_unit

