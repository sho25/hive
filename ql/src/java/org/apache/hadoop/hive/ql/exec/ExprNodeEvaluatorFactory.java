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
name|exec
package|;
end_package

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
name|conf
operator|.
name|Configuration
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
name|ExprNodeDynamicValueDesc
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

begin_comment
comment|/**  * ExprNodeEvaluatorFactory.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|ExprNodeEvaluatorFactory
block|{
specifier|private
name|ExprNodeEvaluatorFactory
parameter_list|()
block|{   }
specifier|public
specifier|static
name|ExprNodeEvaluator
name|get
parameter_list|(
name|ExprNodeDesc
name|desc
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|get
argument_list|(
name|desc
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ExprNodeEvaluator
name|get
parameter_list|(
name|ExprNodeDesc
name|desc
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Constant node
if|if
condition|(
name|desc
operator|instanceof
name|ExprNodeConstantDesc
condition|)
block|{
return|return
operator|new
name|ExprNodeConstantEvaluator
argument_list|(
operator|(
name|ExprNodeConstantDesc
operator|)
name|desc
argument_list|,
name|conf
argument_list|)
return|;
block|}
comment|// Column-reference node, e.g. a column in the input row
if|if
condition|(
name|desc
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
return|return
operator|new
name|ExprNodeColumnEvaluator
argument_list|(
operator|(
name|ExprNodeColumnDesc
operator|)
name|desc
argument_list|,
name|conf
argument_list|)
return|;
block|}
comment|// Generic Function node, e.g. CASE, an operator or a UDF node
if|if
condition|(
name|desc
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
return|return
operator|new
name|ExprNodeGenericFuncEvaluator
argument_list|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|desc
argument_list|,
name|conf
argument_list|)
return|;
block|}
comment|// Field node, e.g. get a.myfield1 from a
if|if
condition|(
name|desc
operator|instanceof
name|ExprNodeFieldDesc
condition|)
block|{
return|return
operator|new
name|ExprNodeFieldEvaluator
argument_list|(
operator|(
name|ExprNodeFieldDesc
operator|)
name|desc
argument_list|,
name|conf
argument_list|)
return|;
block|}
comment|// Dynamic value which will be determined during query runtime
if|if
condition|(
name|desc
operator|instanceof
name|ExprNodeDynamicValueDesc
condition|)
block|{
return|return
operator|new
name|ExprNodeDynamicValueEvaluator
argument_list|(
operator|(
name|ExprNodeDynamicValueDesc
operator|)
name|desc
argument_list|,
name|conf
argument_list|)
return|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot find ExprNodeEvaluator for the exprNodeDesc = "
operator|+
name|desc
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|ExprNodeEvaluator
index|[]
name|toCachedEvals
parameter_list|(
name|ExprNodeEvaluator
index|[]
name|evals
parameter_list|)
block|{
name|EvaluatorContext
name|context
init|=
operator|new
name|EvaluatorContext
argument_list|()
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
name|evals
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|evals
index|[
name|i
index|]
operator|instanceof
name|ExprNodeGenericFuncEvaluator
condition|)
block|{
name|iterate
argument_list|(
name|evals
index|[
name|i
index|]
argument_list|,
name|context
argument_list|)
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|hasReference
condition|)
block|{
name|evals
index|[
name|i
index|]
operator|=
operator|new
name|ExprNodeEvaluatorHead
argument_list|(
name|evals
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|context
operator|.
name|hasReference
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
return|return
name|evals
return|;
block|}
comment|/**    * Should be called before eval is initialized    */
specifier|public
specifier|static
name|ExprNodeEvaluator
name|toCachedEval
parameter_list|(
name|ExprNodeEvaluator
name|eval
parameter_list|)
block|{
if|if
condition|(
name|eval
operator|instanceof
name|ExprNodeGenericFuncEvaluator
condition|)
block|{
name|EvaluatorContext
name|context
init|=
operator|new
name|EvaluatorContext
argument_list|()
decl_stmt|;
name|iterate
argument_list|(
name|eval
argument_list|,
name|context
argument_list|)
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|hasReference
condition|)
block|{
return|return
operator|new
name|ExprNodeEvaluatorHead
argument_list|(
name|eval
argument_list|)
return|;
block|}
block|}
comment|// has nothing to be cached
return|return
name|eval
return|;
block|}
specifier|private
specifier|static
name|ExprNodeEvaluator
name|iterate
parameter_list|(
name|ExprNodeEvaluator
name|eval
parameter_list|,
name|EvaluatorContext
name|context
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|eval
operator|instanceof
name|ExprNodeConstantEvaluator
operator|)
operator|&&
name|eval
operator|.
name|isDeterministic
argument_list|()
condition|)
block|{
name|ExprNodeEvaluator
name|replace
init|=
name|context
operator|.
name|getEvaluated
argument_list|(
name|eval
argument_list|)
decl_stmt|;
if|if
condition|(
name|replace
operator|!=
literal|null
condition|)
block|{
return|return
name|replace
return|;
block|}
block|}
name|ExprNodeEvaluator
index|[]
name|children
init|=
name|eval
operator|.
name|getChildren
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|!=
literal|null
operator|&&
name|children
operator|.
name|length
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|children
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ExprNodeEvaluator
name|replace
init|=
name|iterate
argument_list|(
name|children
index|[
name|i
index|]
argument_list|,
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|replace
operator|!=
literal|null
condition|)
block|{
name|children
index|[
name|i
index|]
operator|=
name|replace
expr_stmt|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
class|class
name|EvaluatorContext
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|ExprNodeDesc
operator|.
name|ExprNodeDescEqualityWrapper
argument_list|,
name|ExprNodeEvaluator
argument_list|>
name|cached
init|=
operator|new
name|HashMap
argument_list|<
name|ExprNodeDesc
operator|.
name|ExprNodeDescEqualityWrapper
argument_list|,
name|ExprNodeEvaluator
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|hasReference
decl_stmt|;
specifier|public
name|ExprNodeEvaluator
name|getEvaluated
parameter_list|(
name|ExprNodeEvaluator
name|eval
parameter_list|)
block|{
name|ExprNodeDesc
operator|.
name|ExprNodeDescEqualityWrapper
name|key
init|=
operator|new
name|ExprNodeDesc
operator|.
name|ExprNodeDescEqualityWrapper
argument_list|(
name|eval
operator|.
name|expr
argument_list|)
decl_stmt|;
name|ExprNodeEvaluator
name|prev
init|=
name|cached
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|==
literal|null
condition|)
block|{
name|cached
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|eval
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|hasReference
operator|=
literal|true
expr_stmt|;
return|return
operator|new
name|ExprNodeEvaluatorRef
argument_list|(
name|prev
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

