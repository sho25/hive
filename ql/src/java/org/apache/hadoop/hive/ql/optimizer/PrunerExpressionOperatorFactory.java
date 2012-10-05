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
name|ql
operator|.
name|plan
operator|.
name|ExprNodeNullDesc
import|;
end_import

begin_comment
comment|/**  * Expression processor factory for pruning. Each processor tries to  * convert the expression subtree into a pruning expression.  *  * It can be used for partition prunner and list bucketing pruner.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|PrunerExpressionOperatorFactory
block|{
comment|/**    * If all children are candidates and refer only to one table alias then this    * expr is a candidate else it is not a candidate but its children could be    * final candidates.    */
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
name|ExprNodeDesc
name|newfd
init|=
literal|null
decl_stmt|;
name|ExprNodeGenericFuncDesc
name|fd
init|=
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|nd
decl_stmt|;
name|boolean
name|unknown
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|FunctionRegistry
operator|.
name|isOpAndOrNot
argument_list|(
name|fd
argument_list|)
condition|)
block|{
comment|// do nothing because "And" and "Or" and "Not" supports null value
comment|// evaluation
comment|// NOTE: In the future all UDFs that treats null value as UNKNOWN (both
comment|// in parameters and return
comment|// values) should derive from a common base class UDFNullAsUnknown, so
comment|// instead of listing the classes
comment|// here we would test whether a class is derived from that base class.
comment|// If All childs are null, set unknown to true
name|boolean
name|isAllNull
init|=
literal|true
decl_stmt|;
for|for
control|(
name|Object
name|child
range|:
name|nodeOutputs
control|)
block|{
name|ExprNodeDesc
name|child_nd
init|=
operator|(
name|ExprNodeDesc
operator|)
name|child
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|child_nd
operator|instanceof
name|ExprNodeConstantDesc
operator|&&
operator|(
operator|(
name|ExprNodeConstantDesc
operator|)
name|child_nd
operator|)
operator|.
name|getValue
argument_list|()
operator|==
literal|null
operator|)
condition|)
block|{
name|isAllNull
operator|=
literal|false
expr_stmt|;
block|}
block|}
name|unknown
operator|=
name|isAllNull
expr_stmt|;
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
name|unknown
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
comment|// If any child is null, set unknown to true
for|for
control|(
name|Object
name|child
range|:
name|nodeOutputs
control|)
block|{
name|ExprNodeDesc
name|child_nd
init|=
operator|(
name|ExprNodeDesc
operator|)
name|child
decl_stmt|;
if|if
condition|(
name|child_nd
operator|instanceof
name|ExprNodeConstantDesc
operator|&&
operator|(
operator|(
name|ExprNodeConstantDesc
operator|)
name|child_nd
operator|)
operator|.
name|getValue
argument_list|()
operator|==
literal|null
condition|)
block|{
name|unknown
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|unknown
condition|)
block|{
name|newfd
operator|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|fd
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Create the list of children
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
for|for
control|(
name|Object
name|child
range|:
name|nodeOutputs
control|)
block|{
name|children
operator|.
name|add
argument_list|(
operator|(
name|ExprNodeDesc
operator|)
name|child
argument_list|)
expr_stmt|;
block|}
comment|// Create a copy of the function descriptor
name|newfd
operator|=
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|fd
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|fd
operator|.
name|getGenericUDF
argument_list|()
argument_list|,
name|children
argument_list|)
expr_stmt|;
block|}
return|return
name|newfd
return|;
block|}
block|}
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
name|int
name|idx
init|=
literal|0
decl_stmt|;
name|ExprNodeDesc
name|left_nd
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Object
name|child
range|:
name|nodeOutputs
control|)
block|{
name|ExprNodeDesc
name|child_nd
init|=
operator|(
name|ExprNodeDesc
operator|)
name|child
decl_stmt|;
if|if
condition|(
name|child_nd
operator|instanceof
name|ExprNodeConstantDesc
operator|&&
operator|(
operator|(
name|ExprNodeConstantDesc
operator|)
name|child_nd
operator|)
operator|.
name|getValue
argument_list|()
operator|==
literal|null
condition|)
block|{
name|unknown
operator|=
literal|true
expr_stmt|;
block|}
name|left_nd
operator|=
name|child_nd
expr_stmt|;
block|}
assert|assert
operator|(
name|idx
operator|==
literal|0
operator|)
assert|;
name|ExprNodeDesc
name|newnd
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|unknown
condition|)
block|{
name|newnd
operator|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|fnd
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newnd
operator|=
operator|new
name|ExprNodeFieldDesc
argument_list|(
name|fnd
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|left_nd
argument_list|,
name|fnd
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|fnd
operator|.
name|getIsList
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|newnd
return|;
block|}
block|}
comment|/**    * Processor for column expressions.    */
specifier|public
specifier|static
specifier|abstract
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
name|ExprNodeDesc
name|newcd
init|=
literal|null
decl_stmt|;
name|ExprNodeColumnDesc
name|cd
init|=
operator|(
name|ExprNodeColumnDesc
operator|)
name|nd
decl_stmt|;
name|newcd
operator|=
name|processColumnDesc
argument_list|(
name|procCtx
argument_list|,
name|cd
argument_list|)
expr_stmt|;
return|return
name|newcd
return|;
block|}
comment|/**      * Process column desc. It should be done by subclass.      *      * @param procCtx      * @param cd      * @return      */
specifier|protected
specifier|abstract
name|ExprNodeDesc
name|processColumnDesc
parameter_list|(
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|ExprNodeColumnDesc
name|cd
parameter_list|)
function_decl|;
block|}
comment|/**    * Processor for constants and null expressions. For such expressions the    * processor simply clones the exprNodeDesc and returns it.    */
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
operator|(
operator|(
name|ExprNodeConstantDesc
operator|)
name|nd
operator|)
operator|.
name|clone
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|nd
operator|instanceof
name|ExprNodeNullDesc
condition|)
block|{
return|return
operator|(
operator|(
name|ExprNodeNullDesc
operator|)
name|nd
operator|)
operator|.
name|clone
argument_list|()
return|;
block|}
assert|assert
operator|(
literal|false
operator|)
assert|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Instantiate default expression processor.    * @return    */
specifier|public
specifier|static
specifier|final
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
comment|/**    * Instantiate generic function processor.    *    * @return    */
specifier|public
specifier|static
specifier|final
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
comment|/**    * Instantiate field processor.    *    * @return    */
specifier|public
specifier|static
specifier|final
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
block|}
end_class

end_unit

