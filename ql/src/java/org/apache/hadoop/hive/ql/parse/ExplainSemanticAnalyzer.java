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
name|ArrayList
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
name|fs
operator|.
name|Path
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
name|Context
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
name|exec
operator|.
name|TaskFactory
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
name|explainWork
import|;
end_import

begin_class
specifier|public
class|class
name|ExplainSemanticAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
specifier|public
name|ExplainSemanticAnalyzer
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|analyzeInternal
parameter_list|(
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ctx
operator|.
name|setExplain
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Create a semantic analyzer for the query
name|BaseSemanticAnalyzer
name|sem
init|=
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
name|conf
argument_list|,
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|sem
operator|.
name|analyze
argument_list|(
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|boolean
name|extended
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|ast
operator|.
name|getChildCount
argument_list|()
operator|>
literal|1
condition|)
block|{
name|extended
operator|=
literal|true
expr_stmt|;
block|}
name|ctx
operator|.
name|setResFile
argument_list|(
operator|new
name|Path
argument_list|(
name|ctx
operator|.
name|getLocalTmpFileURI
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|tasks
init|=
name|sem
operator|.
name|getRootTasks
argument_list|()
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|fetchTask
init|=
name|sem
operator|.
name|getFetchTask
argument_list|()
decl_stmt|;
if|if
condition|(
name|tasks
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|fetchTask
operator|!=
literal|null
condition|)
block|{
name|tasks
operator|=
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|tasks
operator|.
name|add
argument_list|(
name|fetchTask
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|fetchTask
operator|!=
literal|null
condition|)
name|tasks
operator|.
name|add
argument_list|(
name|fetchTask
argument_list|)
expr_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|explainWork
argument_list|(
name|ctx
operator|.
name|getResFile
argument_list|()
argument_list|,
name|tasks
argument_list|,
operator|(
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|toStringTree
argument_list|()
argument_list|,
name|extended
argument_list|)
argument_list|,
name|this
operator|.
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

