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
name|ppr
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
name|exprNodeColumnDesc
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
name|exprNodeConstantDesc
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
name|exprNodeDesc
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
name|exprNodeFieldDesc
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
name|exprNodeGenericFuncDesc
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
name|exprNodeNullDesc
import|;
end_import

begin_comment
comment|/**  * Expression processor factory for partition pruning. Each processor tries to  * convert the expression subtree into a partition pruning expression. This  * expression is then used to figure out whether a particular partition should  * be scanned or not.  */
end_comment

begin_class
specifier|public
class|class
name|ExprProcFactory
block|{
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
name|exprNodeDesc
name|newcd
init|=
literal|null
decl_stmt|;
name|exprNodeColumnDesc
name|cd
init|=
operator|(
name|exprNodeColumnDesc
operator|)
name|nd
decl_stmt|;
name|ExprProcCtx
name|epc
init|=
operator|(
name|ExprProcCtx
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
name|getIsParititonCol
argument_list|()
condition|)
block|{
name|newcd
operator|=
name|cd
operator|.
name|clone
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|newcd
operator|=
operator|new
name|exprNodeConstantDesc
argument_list|(
name|cd
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|epc
operator|.
name|setHasNonPartCols
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
name|newcd
return|;
block|}
block|}
comment|/**    * If all children are candidates and refer only to one table alias then this    * expr is a candidate else it is not a candidate but its children could be    * final candidates    */
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
name|exprNodeDesc
name|newfd
init|=
literal|null
decl_stmt|;
name|exprNodeGenericFuncDesc
name|fd
init|=
operator|(
name|exprNodeGenericFuncDesc
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
name|exprNodeDesc
name|child_nd
init|=
operator|(
name|exprNodeDesc
operator|)
name|child
decl_stmt|;
if|if
condition|(
name|child_nd
operator|instanceof
name|exprNodeConstantDesc
operator|&&
operator|(
operator|(
name|exprNodeConstantDesc
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
name|exprNodeConstantDesc
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
name|exprNodeDesc
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<
name|exprNodeDesc
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
name|exprNodeDesc
operator|)
name|child
argument_list|)
expr_stmt|;
block|}
comment|// Create a copy of the function descriptor
name|newfd
operator|=
operator|new
name|exprNodeGenericFuncDesc
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
name|exprNodeFieldDesc
name|fnd
init|=
operator|(
name|exprNodeFieldDesc
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
name|exprNodeDesc
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
name|exprNodeDesc
name|child_nd
init|=
operator|(
name|exprNodeDesc
operator|)
name|child
decl_stmt|;
if|if
condition|(
name|child_nd
operator|instanceof
name|exprNodeConstantDesc
operator|&&
operator|(
operator|(
name|exprNodeConstantDesc
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
name|exprNodeDesc
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
name|exprNodeConstantDesc
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
name|exprNodeFieldDesc
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
name|exprNodeConstantDesc
condition|)
block|{
return|return
operator|(
operator|(
name|exprNodeConstantDesc
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
name|exprNodeNullDesc
condition|)
block|{
return|return
operator|(
operator|(
name|exprNodeNullDesc
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
comment|/**    * Generates the partition pruner for the expression tree    *     * @param tabAlias    *          The table alias of the partition table that is being considered    *          for pruning    * @param pred    *          The predicate from which the partition pruner needs to be    *          generated    * @return hasNonPartCols returns true/false depending upon whether this pred    *         has a non partition column    * @throws SemanticException    */
specifier|public
specifier|static
name|exprNodeDesc
name|genPruner
parameter_list|(
name|String
name|tabAlias
parameter_list|,
name|exprNodeDesc
name|pred
parameter_list|,
name|boolean
name|hasNonPartCols
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Create the walker, the rules dispatcher and the context.
name|ExprProcCtx
name|pprCtx
init|=
operator|new
name|ExprProcCtx
argument_list|(
name|tabAlias
argument_list|)
decl_stmt|;
comment|// create a walker which walks the tree in a DFS manner while maintaining
comment|// the operator stack. The dispatcher
comment|// generates the plan from the operator tree
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
name|exprNodeColumnDesc
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
name|exprNodeFieldDesc
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
name|exprNodeGenericFuncDesc
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
name|hasNonPartCols
operator|=
name|pprCtx
operator|.
name|getHasNonPartCols
argument_list|()
expr_stmt|;
comment|// Get the exprNodeDesc corresponding to the first start node;
return|return
operator|(
name|exprNodeDesc
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

