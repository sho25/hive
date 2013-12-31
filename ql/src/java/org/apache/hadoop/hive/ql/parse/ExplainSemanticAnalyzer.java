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
name|Collections
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
name|ExplainWork
import|;
end_import

begin_comment
comment|/**  * ExplainSemanticAnalyzer.  *  */
end_comment

begin_class
specifier|public
class|class
name|ExplainSemanticAnalyzer
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
name|boolean
name|extended
init|=
literal|false
decl_stmt|;
name|boolean
name|formatted
init|=
literal|false
decl_stmt|;
name|boolean
name|dependency
init|=
literal|false
decl_stmt|;
name|boolean
name|logical
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|ast
operator|.
name|getChildCount
argument_list|()
operator|==
literal|2
condition|)
block|{
name|int
name|explainOptions
init|=
name|ast
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getType
argument_list|()
decl_stmt|;
name|formatted
operator|=
operator|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_FORMATTED
operator|)
expr_stmt|;
name|extended
operator|=
operator|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_EXTENDED
operator|)
expr_stmt|;
name|dependency
operator|=
operator|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_DEPENDENCY
operator|)
expr_stmt|;
name|logical
operator|=
operator|(
name|explainOptions
operator|==
name|HiveParser
operator|.
name|KW_LOGICAL
operator|)
expr_stmt|;
block|}
name|ctx
operator|.
name|setExplain
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setExplainLogical
argument_list|(
name|logical
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
name|BaseSemanticAnalyzer
name|sem
init|=
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
name|conf
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
name|tasks
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
block|}
name|ParseContext
name|pCtx
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|sem
operator|instanceof
name|SemanticAnalyzer
condition|)
block|{
name|pCtx
operator|=
operator|(
operator|(
name|SemanticAnalyzer
operator|)
name|sem
operator|)
operator|.
name|getParseContext
argument_list|()
expr_stmt|;
block|}
name|ExplainWork
name|work
init|=
operator|new
name|ExplainWork
argument_list|(
name|ctx
operator|.
name|getResFile
argument_list|()
argument_list|,
name|pCtx
argument_list|,
name|tasks
argument_list|,
name|fetchTask
argument_list|,
name|input
operator|.
name|toStringTree
argument_list|()
argument_list|,
name|sem
operator|.
name|getInputs
argument_list|()
argument_list|,
name|extended
argument_list|,
name|formatted
argument_list|,
name|dependency
argument_list|,
name|logical
argument_list|)
decl_stmt|;
name|work
operator|.
name|setAppendTaskType
argument_list|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEEXPLAINDEPENDENCYAPPENDTASKTYPES
argument_list|)
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|explTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|work
argument_list|,
name|conf
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

