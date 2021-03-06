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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|QueryState
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
name|ExplainSQRewriteTask
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
name|ExplainSQRewriteWork
import|;
end_import

begin_class
specifier|public
class|class
name|ExplainSQRewriteSemanticAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldList
decl_stmt|;
specifier|public
name|ExplainSQRewriteSemanticAnalyzer
parameter_list|(
name|QueryState
name|queryState
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|queryState
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
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
name|setExplainConfig
argument_list|(
operator|new
name|ExplainConfiguration
argument_list|()
argument_list|)
expr_stmt|;
comment|// Create a semantic analyzer for the query
name|ASTNode
name|input
init|=
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|SemanticAnalyzer
name|sem
init|=
operator|(
name|SemanticAnalyzer
operator|)
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
name|queryState
argument_list|,
name|input
argument_list|)
decl_stmt|;
name|sem
operator|.
name|analyze
argument_list|(
name|input
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|sem
operator|.
name|validate
argument_list|()
expr_stmt|;
name|ctx
operator|.
name|setResFile
argument_list|(
name|ctx
operator|.
name|getLocalTmpPath
argument_list|()
argument_list|)
expr_stmt|;
name|ExplainSQRewriteWork
name|work
init|=
operator|new
name|ExplainSQRewriteWork
argument_list|(
name|ctx
operator|.
name|getResFile
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|sem
operator|.
name|getQB
argument_list|()
argument_list|,
name|input
argument_list|,
name|ctx
argument_list|)
decl_stmt|;
name|ExplainSQRewriteTask
name|explTask
init|=
operator|(
name|ExplainSQRewriteTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
name|work
argument_list|)
decl_stmt|;
name|fieldList
operator|=
name|explTask
operator|.
name|getResultSchema
argument_list|()
expr_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|explTask
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getResultSchema
parameter_list|()
block|{
return|return
name|fieldList
return|;
block|}
block|}
end_class

end_unit

