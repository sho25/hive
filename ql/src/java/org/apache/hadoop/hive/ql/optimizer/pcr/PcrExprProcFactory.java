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
name|optimizer
operator|.
name|pcr
package|;
end_package

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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Stack
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
name|FunctionRegistry
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
name|lib
operator|.
name|DefaultGraphWalker
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
name|lib
operator|.
name|DefaultRuleDispatcher
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
name|lib
operator|.
name|Dispatcher
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
name|lib
operator|.
name|GraphWalker
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
name|lib
operator|.
name|Node
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
name|lib
operator|.
name|NodeProcessor
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
name|lib
operator|.
name|NodeProcessorCtx
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
name|lib
operator|.
name|Rule
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
name|lib
operator|.
name|RuleRegExp
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
name|metadata
operator|.
name|HiveException
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
name|metadata
operator|.
name|Partition
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
name|metadata
operator|.
name|Table
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
name|metadata
operator|.
name|VirtualColumn
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
name|optimizer
operator|.
name|ppr
operator|.
name|PartExprEvalUtils
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
name|ExprNodeColumnDesc
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
name|ExprNodeConstantDesc
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
name|ExprNodeDesc
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
name|ExprNodeFieldDesc
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
name|ExprNodeGenericFuncDesc
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
name|serde2
operator|.
name|SerDeException
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
name|serde2
operator|.
name|objectinspector
operator|.
name|StructObjectInspector
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
import|;
end_import

begin_comment
comment|/**  * Expression processor factory for partition condition removing. Each processor tries to  * calculate a result vector from its children's result vectors.  * Each element is the result for one of the pruned partitions.  * It also generates node by Modifying expr trees with partition conditions removed  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|PcrExprProcFactory
block|{
specifier|static
name|Object
name|evalExprWithPart
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|,
name|Partition
name|p
parameter_list|,
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|vcs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|StructObjectInspector
name|rowObjectInspector
decl_stmt|;
name|Table
name|tbl
init|=
name|p
operator|.
name|getTable
argument_list|()
decl_stmt|;
try|try
block|{
name|rowObjectInspector
operator|=
operator|(
name|StructObjectInspector
operator|)
name|tbl
operator|.
name|getDeserializer
argument_list|()
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
try|try
block|{
return|return
name|PartExprEvalUtils
operator|.
name|evalExprWithPart
argument_list|(
name|expr
argument_list|,
name|p
argument_list|,
name|vcs
argument_list|,
name|rowObjectInspector
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|static
name|Boolean
name|ifResultsAgree
parameter_list|(
name|Boolean
index|[]
name|resultVector
parameter_list|)
block|{
name|Boolean
name|result
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Boolean
name|b
range|:
name|resultVector
control|)
block|{
if|if
condition|(
name|b
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|result
operator|=
name|b
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|result
operator|.
name|equals
argument_list|(
name|b
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|static
name|Object
name|ifResultsAgree
parameter_list|(
name|Object
index|[]
name|resultVector
parameter_list|)
block|{
name|Object
name|result
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Object
name|b
range|:
name|resultVector
control|)
block|{
if|if
condition|(
name|b
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|result
operator|=
name|b
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|result
operator|.
name|equals
argument_list|(
name|b
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|static
name|NodeInfoWrapper
name|getResultWrapFromResults
parameter_list|(
name|Boolean
index|[]
name|results
parameter_list|,
name|ExprNodeGenericFuncDesc
name|fd
parameter_list|,
name|Object
index|[]
name|nodeOutputs
parameter_list|)
block|{
name|Boolean
name|ifAgree
init|=
name|ifResultsAgree
argument_list|(
name|results
argument_list|)
decl_stmt|;
if|if
condition|(
name|ifAgree
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|DIVIDED
argument_list|,
name|results
argument_list|,
name|getOutExpr
argument_list|(
name|fd
argument_list|,
name|nodeOutputs
argument_list|)
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|ifAgree
operator|.
name|booleanValue
argument_list|()
operator|==
literal|true
condition|)
block|{
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|TRUE
argument_list|,
literal|null
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|fd
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|FALSE
argument_list|,
literal|null
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|fd
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|private
name|PcrExprProcFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
specifier|static
name|Boolean
name|opAnd
parameter_list|(
name|Boolean
name|op1
parameter_list|,
name|Boolean
name|op2
parameter_list|)
block|{
comment|// When people forget to quote a string, op1/op2 is null.
comment|// For example, select * from some_table where ds> 2012-12-1 and ds< 2012-12-2 .
if|if
condition|(
name|op1
operator|!=
literal|null
operator|&&
name|op1
operator|.
name|equals
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|)
operator|||
name|op2
operator|!=
literal|null
operator|&&
name|op2
operator|.
name|equals
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|)
condition|)
block|{
return|return
name|Boolean
operator|.
name|FALSE
return|;
block|}
if|if
condition|(
name|op1
operator|==
literal|null
operator|||
name|op2
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|Boolean
operator|.
name|TRUE
return|;
block|}
specifier|static
name|Boolean
name|opOr
parameter_list|(
name|Boolean
name|op1
parameter_list|,
name|Boolean
name|op2
parameter_list|)
block|{
comment|// When people forget to quote a string, op1/op2 is null.
comment|// For example, select * from some_table where ds> 2012-12-1 or ds< 2012-12-2 .
if|if
condition|(
name|op1
operator|!=
literal|null
operator|&&
name|op1
operator|.
name|equals
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
operator|||
name|op2
operator|!=
literal|null
operator|&&
name|op2
operator|.
name|equals
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
condition|)
block|{
return|return
name|Boolean
operator|.
name|TRUE
return|;
block|}
if|if
condition|(
name|op1
operator|==
literal|null
operator|||
name|op2
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|Boolean
operator|.
name|FALSE
return|;
block|}
specifier|static
name|Boolean
name|opNot
parameter_list|(
name|Boolean
name|op
parameter_list|)
block|{
comment|// When people forget to quote a string, op1/op2 is null.
comment|// For example, select * from some_table where not ds> 2012-12-1 .
if|if
condition|(
name|op
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|op
operator|.
name|equals
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
condition|)
block|{
return|return
name|Boolean
operator|.
name|FALSE
return|;
block|}
if|if
condition|(
name|op
operator|.
name|equals
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|)
condition|)
block|{
return|return
name|Boolean
operator|.
name|TRUE
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|public
enum|enum
name|WalkState
block|{
name|PART_COL
block|,
name|TRUE
block|,
name|FALSE
block|,
name|CONSTANT
block|,
name|UNKNOWN
block|,
name|DIVIDED
block|}
specifier|public
specifier|static
class|class
name|NodeInfoWrapper
block|{
specifier|public
name|NodeInfoWrapper
parameter_list|(
name|WalkState
name|state
parameter_list|,
name|Boolean
index|[]
name|resultVector
parameter_list|,
name|ExprNodeDesc
name|outExpr
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|ResultVector
operator|=
name|resultVector
expr_stmt|;
name|this
operator|.
name|outExpr
operator|=
name|outExpr
expr_stmt|;
block|}
name|WalkState
name|state
decl_stmt|;
specifier|public
name|Boolean
index|[]
name|ResultVector
decl_stmt|;
specifier|public
name|ExprNodeDesc
name|outExpr
decl_stmt|;
block|}
comment|/**    * Processor for column expressions.    */
specifier|public
specifier|static
class|class
name|ColumnExprProcessor
implements|implements
name|NodeProcessor
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ExprNodeColumnDesc
name|cd
init|=
operator|(
name|ExprNodeColumnDesc
operator|)
name|nd
decl_stmt|;
name|PcrExprProcCtx
name|epc
init|=
operator|(
name|PcrExprProcCtx
operator|)
name|procCtx
decl_stmt|;
if|if
condition|(
name|cd
operator|.
name|getTabAlias
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|epc
operator|.
name|getTabAlias
argument_list|()
argument_list|)
operator|&&
name|cd
operator|.
name|getIsPartitionColOrVirtualCol
argument_list|()
condition|)
block|{
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|PART_COL
argument_list|,
literal|null
argument_list|,
name|cd
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|UNKNOWN
argument_list|,
literal|null
argument_list|,
name|cd
argument_list|)
return|;
block|}
block|}
block|}
specifier|public
specifier|static
name|ExprNodeGenericFuncDesc
name|getOutExpr
parameter_list|(
name|ExprNodeGenericFuncDesc
name|funcExpr
parameter_list|,
name|Object
index|[]
name|nodeOutputs
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|nodeOutputs
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Object
name|child
range|:
name|nodeOutputs
control|)
block|{
name|NodeInfoWrapper
name|wrapper
init|=
operator|(
name|NodeInfoWrapper
operator|)
name|child
decl_stmt|;
name|children
operator|.
name|add
argument_list|(
name|wrapper
operator|.
name|outExpr
argument_list|)
expr_stmt|;
block|}
block|}
name|funcExpr
operator|.
name|setChildren
argument_list|(
name|children
argument_list|)
expr_stmt|;
return|return
name|funcExpr
return|;
block|}
comment|/**    * Processor for Generic functions    *    * If it is AND, OR or NOT, we replace the node to be the constant true or    * false if we are sure the result from children, or cut one of the child    * if we know partial results. In case of both child has a result vector,    * we calculate the result vector for the node. If all partitions agree on    * a result, we replace the node with constant true or false. Otherwise, we    * pass the vector result. For other Generic functions, if it is non-deterministic    * we simply pass it (with children adjusted based on results from children).    * If it is deterministic, we evaluate result vector if any of the children    * is partition column. Otherwise, we pass it as it is.    */
specifier|public
specifier|static
class|class
name|GenericFuncExprProcessor
implements|implements
name|NodeProcessor
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|PcrExprProcCtx
name|ctx
init|=
operator|(
name|PcrExprProcCtx
operator|)
name|procCtx
decl_stmt|;
name|ExprNodeGenericFuncDesc
name|fd
init|=
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|nd
decl_stmt|;
if|if
condition|(
name|FunctionRegistry
operator|.
name|isOpNot
argument_list|(
name|fd
argument_list|)
condition|)
block|{
assert|assert
operator|(
name|nodeOutputs
operator|.
name|length
operator|==
literal|1
operator|)
assert|;
name|NodeInfoWrapper
name|wrapper
init|=
operator|(
name|NodeInfoWrapper
operator|)
name|nodeOutputs
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|wrapper
operator|.
name|state
operator|==
name|WalkState
operator|.
name|TRUE
condition|)
block|{
name|ExprNodeConstantDesc
name|falseDesc
init|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|wrapper
operator|.
name|outExpr
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
decl_stmt|;
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|FALSE
argument_list|,
literal|null
argument_list|,
name|falseDesc
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|wrapper
operator|.
name|state
operator|==
name|WalkState
operator|.
name|FALSE
condition|)
block|{
name|ExprNodeConstantDesc
name|trueDesc
init|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|wrapper
operator|.
name|outExpr
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
decl_stmt|;
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|TRUE
argument_list|,
literal|null
argument_list|,
name|trueDesc
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|wrapper
operator|.
name|state
operator|==
name|WalkState
operator|.
name|DIVIDED
condition|)
block|{
name|Boolean
index|[]
name|results
init|=
operator|new
name|Boolean
index|[
name|ctx
operator|.
name|getPartList
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ctx
operator|.
name|getPartList
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|results
index|[
name|i
index|]
operator|=
name|opNot
argument_list|(
name|wrapper
operator|.
name|ResultVector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|DIVIDED
argument_list|,
name|results
argument_list|,
name|getOutExpr
argument_list|(
name|fd
argument_list|,
name|nodeOutputs
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|wrapper
operator|.
name|state
argument_list|,
literal|null
argument_list|,
name|getOutExpr
argument_list|(
name|fd
argument_list|,
name|nodeOutputs
argument_list|)
argument_list|)
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|FunctionRegistry
operator|.
name|isOpAnd
argument_list|(
name|fd
argument_list|)
condition|)
block|{
assert|assert
operator|(
name|nodeOutputs
operator|.
name|length
operator|==
literal|2
operator|)
assert|;
name|NodeInfoWrapper
name|c1
init|=
operator|(
name|NodeInfoWrapper
operator|)
name|nodeOutputs
index|[
literal|0
index|]
decl_stmt|;
name|NodeInfoWrapper
name|c2
init|=
operator|(
name|NodeInfoWrapper
operator|)
name|nodeOutputs
index|[
literal|1
index|]
decl_stmt|;
if|if
condition|(
name|c1
operator|.
name|state
operator|==
name|WalkState
operator|.
name|FALSE
condition|)
block|{
return|return
name|c1
return|;
block|}
elseif|else
if|if
condition|(
name|c2
operator|.
name|state
operator|==
name|WalkState
operator|.
name|FALSE
condition|)
block|{
return|return
name|c2
return|;
block|}
elseif|else
if|if
condition|(
name|c1
operator|.
name|state
operator|==
name|WalkState
operator|.
name|TRUE
condition|)
block|{
return|return
name|c2
return|;
block|}
elseif|else
if|if
condition|(
name|c2
operator|.
name|state
operator|==
name|WalkState
operator|.
name|TRUE
condition|)
block|{
return|return
name|c1
return|;
block|}
elseif|else
if|if
condition|(
name|c1
operator|.
name|state
operator|==
name|WalkState
operator|.
name|UNKNOWN
operator|||
name|c2
operator|.
name|state
operator|==
name|WalkState
operator|.
name|UNKNOWN
condition|)
block|{
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|UNKNOWN
argument_list|,
literal|null
argument_list|,
name|getOutExpr
argument_list|(
name|fd
argument_list|,
name|nodeOutputs
argument_list|)
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|c1
operator|.
name|state
operator|==
name|WalkState
operator|.
name|DIVIDED
operator|&&
name|c2
operator|.
name|state
operator|==
name|WalkState
operator|.
name|DIVIDED
condition|)
block|{
name|Boolean
index|[]
name|results
init|=
operator|new
name|Boolean
index|[
name|ctx
operator|.
name|getPartList
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ctx
operator|.
name|getPartList
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|results
index|[
name|i
index|]
operator|=
name|opAnd
argument_list|(
name|c1
operator|.
name|ResultVector
index|[
name|i
index|]
argument_list|,
name|c2
operator|.
name|ResultVector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|getResultWrapFromResults
argument_list|(
name|results
argument_list|,
name|fd
argument_list|,
name|nodeOutputs
argument_list|)
return|;
block|}
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|UNKNOWN
argument_list|,
literal|null
argument_list|,
name|getOutExpr
argument_list|(
name|fd
argument_list|,
name|nodeOutputs
argument_list|)
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|FunctionRegistry
operator|.
name|isOpOr
argument_list|(
name|fd
argument_list|)
condition|)
block|{
assert|assert
operator|(
name|nodeOutputs
operator|.
name|length
operator|==
literal|2
operator|)
assert|;
name|NodeInfoWrapper
name|c1
init|=
operator|(
name|NodeInfoWrapper
operator|)
name|nodeOutputs
index|[
literal|0
index|]
decl_stmt|;
name|NodeInfoWrapper
name|c2
init|=
operator|(
name|NodeInfoWrapper
operator|)
name|nodeOutputs
index|[
literal|1
index|]
decl_stmt|;
if|if
condition|(
name|c1
operator|.
name|state
operator|==
name|WalkState
operator|.
name|TRUE
condition|)
block|{
return|return
name|c1
return|;
block|}
elseif|else
if|if
condition|(
name|c2
operator|.
name|state
operator|==
name|WalkState
operator|.
name|TRUE
condition|)
block|{
return|return
name|c2
return|;
block|}
elseif|else
if|if
condition|(
name|c1
operator|.
name|state
operator|==
name|WalkState
operator|.
name|FALSE
condition|)
block|{
return|return
name|c2
return|;
block|}
elseif|else
if|if
condition|(
name|c2
operator|.
name|state
operator|==
name|WalkState
operator|.
name|FALSE
condition|)
block|{
return|return
name|c1
return|;
block|}
elseif|else
if|if
condition|(
name|c1
operator|.
name|state
operator|==
name|WalkState
operator|.
name|UNKNOWN
operator|||
name|c2
operator|.
name|state
operator|==
name|WalkState
operator|.
name|UNKNOWN
condition|)
block|{
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|UNKNOWN
argument_list|,
literal|null
argument_list|,
name|getOutExpr
argument_list|(
name|fd
argument_list|,
name|nodeOutputs
argument_list|)
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|c1
operator|.
name|state
operator|==
name|WalkState
operator|.
name|DIVIDED
operator|&&
name|c2
operator|.
name|state
operator|==
name|WalkState
operator|.
name|DIVIDED
condition|)
block|{
name|Boolean
index|[]
name|results
init|=
operator|new
name|Boolean
index|[
name|ctx
operator|.
name|getPartList
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ctx
operator|.
name|getPartList
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|results
index|[
name|i
index|]
operator|=
name|opOr
argument_list|(
name|c1
operator|.
name|ResultVector
index|[
name|i
index|]
argument_list|,
name|c2
operator|.
name|ResultVector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|getResultWrapFromResults
argument_list|(
name|results
argument_list|,
name|fd
argument_list|,
name|nodeOutputs
argument_list|)
return|;
block|}
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|UNKNOWN
argument_list|,
literal|null
argument_list|,
name|getOutExpr
argument_list|(
name|fd
argument_list|,
name|nodeOutputs
argument_list|)
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|FunctionRegistry
operator|.
name|isDeterministic
argument_list|(
name|fd
operator|.
name|getGenericUDF
argument_list|()
argument_list|)
condition|)
block|{
comment|// If it's a non-deterministic UDF, set unknown to true
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|UNKNOWN
argument_list|,
literal|null
argument_list|,
name|getOutExpr
argument_list|(
name|fd
argument_list|,
name|nodeOutputs
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
comment|// If any child is unknown, set unknown to true
name|boolean
name|has_part_col
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Object
name|child
range|:
name|nodeOutputs
control|)
block|{
name|NodeInfoWrapper
name|wrapper
init|=
operator|(
name|NodeInfoWrapper
operator|)
name|child
decl_stmt|;
if|if
condition|(
name|wrapper
operator|.
name|state
operator|==
name|WalkState
operator|.
name|UNKNOWN
condition|)
block|{
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|UNKNOWN
argument_list|,
literal|null
argument_list|,
name|getOutExpr
argument_list|(
name|fd
argument_list|,
name|nodeOutputs
argument_list|)
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|wrapper
operator|.
name|state
operator|==
name|WalkState
operator|.
name|PART_COL
condition|)
block|{
name|has_part_col
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
name|has_part_col
condition|)
block|{
comment|//  we need to evaluate result for every pruned partition
if|if
condition|(
name|fd
operator|.
name|getTypeInfo
argument_list|()
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|)
condition|)
block|{
comment|// if the return type of the GenericUDF is boolean and all partitions agree on
comment|// a result, we update the state of the node to be TRUE of FALSE
name|Boolean
index|[]
name|results
init|=
operator|new
name|Boolean
index|[
name|ctx
operator|.
name|getPartList
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ctx
operator|.
name|getPartList
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|results
index|[
name|i
index|]
operator|=
operator|(
name|Boolean
operator|)
name|evalExprWithPart
argument_list|(
name|fd
argument_list|,
name|ctx
operator|.
name|getPartList
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|ctx
operator|.
name|getVirtualColumns
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|getResultWrapFromResults
argument_list|(
name|results
argument_list|,
name|fd
argument_list|,
name|nodeOutputs
argument_list|)
return|;
block|}
comment|// the case that return type of the GenericUDF is not boolean, and if not all partition
comment|// agree on result, we make the node UNKNOWN. If they all agree, we replace the node
comment|// to be a CONSTANT node with value to be the agreed result.
name|Object
index|[]
name|results
init|=
operator|new
name|Object
index|[
name|ctx
operator|.
name|getPartList
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ctx
operator|.
name|getPartList
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|results
index|[
name|i
index|]
operator|=
name|evalExprWithPart
argument_list|(
name|fd
argument_list|,
name|ctx
operator|.
name|getPartList
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|ctx
operator|.
name|getVirtualColumns
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Object
name|result
init|=
name|ifResultsAgree
argument_list|(
name|results
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
comment|// if the result is not boolean and not all partition agree on the
comment|// result, we don't remove the condition. Potentially, it can miss
comment|// the case like "where ds % 3 == 1 or ds % 3 == 2"
comment|// TODO: handle this case by making result vector to handle all
comment|// constant values.
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|UNKNOWN
argument_list|,
literal|null
argument_list|,
name|getOutExpr
argument_list|(
name|fd
argument_list|,
name|nodeOutputs
argument_list|)
argument_list|)
return|;
block|}
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|CONSTANT
argument_list|,
literal|null
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|fd
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|result
argument_list|)
argument_list|)
return|;
block|}
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|CONSTANT
argument_list|,
literal|null
argument_list|,
name|getOutExpr
argument_list|(
name|fd
argument_list|,
name|nodeOutputs
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
empty_stmt|;
comment|/**    * FieldExprProcessor.    *    */
specifier|public
specifier|static
class|class
name|FieldExprProcessor
implements|implements
name|NodeProcessor
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ExprNodeFieldDesc
name|fnd
init|=
operator|(
name|ExprNodeFieldDesc
operator|)
name|nd
decl_stmt|;
name|boolean
name|unknown
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Object
name|child
range|:
name|nodeOutputs
control|)
block|{
name|NodeInfoWrapper
name|wrapper
init|=
operator|(
name|NodeInfoWrapper
operator|)
name|child
decl_stmt|;
if|if
condition|(
name|wrapper
operator|.
name|state
operator|==
name|WalkState
operator|.
name|UNKNOWN
condition|)
block|{
name|unknown
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|unknown
condition|)
block|{
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|UNKNOWN
argument_list|,
literal|null
argument_list|,
name|fnd
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|CONSTANT
argument_list|,
literal|null
argument_list|,
name|fnd
argument_list|)
return|;
block|}
block|}
block|}
comment|/**    * Processor for constants and null expressions. For such expressions the    * processor simply returns.    */
specifier|public
specifier|static
class|class
name|DefaultExprProcessor
implements|implements
name|NodeProcessor
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|nd
operator|instanceof
name|ExprNodeConstantDesc
condition|)
block|{
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|CONSTANT
argument_list|,
literal|null
argument_list|,
operator|(
name|ExprNodeDesc
operator|)
name|nd
argument_list|)
return|;
block|}
return|return
operator|new
name|NodeInfoWrapper
argument_list|(
name|WalkState
operator|.
name|UNKNOWN
argument_list|,
literal|null
argument_list|,
operator|(
name|ExprNodeDesc
operator|)
name|nd
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getDefaultExprProcessor
parameter_list|()
block|{
return|return
operator|new
name|DefaultExprProcessor
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getGenericFuncProcessor
parameter_list|()
block|{
return|return
operator|new
name|GenericFuncExprProcessor
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getFieldProcessor
parameter_list|()
block|{
return|return
operator|new
name|FieldExprProcessor
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getColumnProcessor
parameter_list|()
block|{
return|return
operator|new
name|ColumnExprProcessor
argument_list|()
return|;
block|}
comment|/**    * Remove partition conditions when necessary from the the expression tree.    *    * @param tabAlias    *          the table alias    * @param parts    *          the list of all pruned partitions for the table    * @param vcs    *          virtual columns referenced    * @param pred    *          expression tree of the target filter operator    * @return the node information of the root expression    * @throws SemanticException    */
specifier|public
specifier|static
name|NodeInfoWrapper
name|walkExprTree
parameter_list|(
name|String
name|tabAlias
parameter_list|,
name|ArrayList
argument_list|<
name|Partition
argument_list|>
name|parts
parameter_list|,
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|vcs
parameter_list|,
name|ExprNodeDesc
name|pred
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Create the walker, the rules dispatcher and the context.
name|PcrExprProcCtx
name|pprCtx
init|=
operator|new
name|PcrExprProcCtx
argument_list|(
name|tabAlias
argument_list|,
name|parts
argument_list|,
name|vcs
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|exprRules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
name|exprRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
name|ExprNodeColumnDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|getColumnProcessor
argument_list|()
argument_list|)
expr_stmt|;
name|exprRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R2"
argument_list|,
name|ExprNodeFieldDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|getFieldProcessor
argument_list|()
argument_list|)
expr_stmt|;
name|exprRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R5"
argument_list|,
name|ExprNodeGenericFuncDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|getGenericFuncProcessor
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|getDefaultExprProcessor
argument_list|()
argument_list|,
name|exprRules
argument_list|,
name|pprCtx
argument_list|)
decl_stmt|;
name|GraphWalker
name|egw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Node
argument_list|>
name|startNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|startNodes
operator|.
name|add
argument_list|(
name|pred
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|outputMap
init|=
operator|new
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|egw
operator|.
name|startWalking
argument_list|(
name|startNodes
argument_list|,
name|outputMap
argument_list|)
expr_stmt|;
comment|// Return the wrapper of the root node
return|return
operator|(
name|NodeInfoWrapper
operator|)
name|outputMap
operator|.
name|get
argument_list|(
name|pred
argument_list|)
return|;
block|}
block|}
end_class

end_unit

