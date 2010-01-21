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
name|FilterOperator
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
name|TableScanOperator
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
name|parse
operator|.
name|TypeCheckProcFactory
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

begin_comment
comment|/**  * Operator factory for partition pruning processing of operator graph We find  * all the filter operators that appear just beneath the table scan operators.  * We then pass the filter to the partition pruner to construct a pruner for  * that table alias and store a mapping from the table scan operator to that  * pruner. We call that pruner later during plan generation.  */
end_comment

begin_class
specifier|public
class|class
name|OpProcFactory
block|{
comment|/**    * Determines the partition pruner for the filter. This is called only when    * the filter follows a table scan operator.    */
specifier|public
specifier|static
class|class
name|FilterPPR
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
name|OpWalkerCtx
name|owc
init|=
operator|(
name|OpWalkerCtx
operator|)
name|procCtx
decl_stmt|;
name|FilterOperator
name|fop
init|=
operator|(
name|FilterOperator
operator|)
name|nd
decl_stmt|;
name|FilterOperator
name|fop2
init|=
literal|null
decl_stmt|;
comment|// The stack contains either ... TS, Filter or
comment|// ... TS, Filter, Filter with the head of the stack being the rightmost
comment|// symbol. So we just pop out the two elements from the top and if the
comment|// second one of them is not a table scan then the operator on the top of
comment|// the stack is the Table scan operator.
name|Node
name|tmp
init|=
name|stack
operator|.
name|pop
argument_list|()
decl_stmt|;
name|Node
name|tmp2
init|=
name|stack
operator|.
name|pop
argument_list|()
decl_stmt|;
name|TableScanOperator
name|top
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|tmp2
operator|instanceof
name|TableScanOperator
condition|)
block|{
name|top
operator|=
operator|(
name|TableScanOperator
operator|)
name|tmp2
expr_stmt|;
block|}
else|else
block|{
name|top
operator|=
operator|(
name|TableScanOperator
operator|)
name|stack
operator|.
name|peek
argument_list|()
expr_stmt|;
name|fop2
operator|=
operator|(
name|FilterOperator
operator|)
name|tmp2
expr_stmt|;
block|}
name|stack
operator|.
name|push
argument_list|(
name|tmp2
argument_list|)
expr_stmt|;
name|stack
operator|.
name|push
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
comment|// If fop2 exists (i.e this is not the top level filter and fop2 is not
comment|// a sampling filter then we ignore the current filter
if|if
condition|(
name|fop2
operator|!=
literal|null
operator|&&
operator|!
name|fop2
operator|.
name|getConf
argument_list|()
operator|.
name|getIsSamplingPred
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// ignore the predicate in case it is not a sampling predicate
if|if
condition|(
name|fop
operator|.
name|getConf
argument_list|()
operator|.
name|getIsSamplingPred
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Otherwise this is not a sampling predicate and we need to
name|exprNodeDesc
name|predicate
init|=
name|fop
operator|.
name|getConf
argument_list|()
operator|.
name|getPredicate
argument_list|()
decl_stmt|;
name|String
name|alias
init|=
name|top
operator|.
name|getConf
argument_list|()
operator|.
name|getAlias
argument_list|()
decl_stmt|;
comment|// Generate the partition pruning predicate
name|boolean
name|hasNonPartCols
init|=
literal|false
decl_stmt|;
name|exprNodeDesc
name|ppr_pred
init|=
name|ExprProcFactory
operator|.
name|genPruner
argument_list|(
name|alias
argument_list|,
name|predicate
argument_list|,
name|hasNonPartCols
argument_list|)
decl_stmt|;
name|owc
operator|.
name|addHasNonPartCols
argument_list|(
name|hasNonPartCols
argument_list|)
expr_stmt|;
comment|// Add the pruning predicate to the table scan operator
name|addPruningPred
argument_list|(
name|owc
operator|.
name|getOpToPartPruner
argument_list|()
argument_list|,
name|top
argument_list|,
name|ppr_pred
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|addPruningPred
parameter_list|(
name|Map
argument_list|<
name|TableScanOperator
argument_list|,
name|exprNodeDesc
argument_list|>
name|opToPPR
parameter_list|,
name|TableScanOperator
name|top
parameter_list|,
name|exprNodeDesc
name|new_ppr_pred
parameter_list|)
block|{
name|exprNodeDesc
name|old_ppr_pred
init|=
name|opToPPR
operator|.
name|get
argument_list|(
name|top
argument_list|)
decl_stmt|;
name|exprNodeDesc
name|ppr_pred
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|old_ppr_pred
operator|!=
literal|null
condition|)
block|{
comment|// or the old_ppr_pred and the new_ppr_pred
name|ppr_pred
operator|=
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"OR"
argument_list|,
name|old_ppr_pred
argument_list|,
name|new_ppr_pred
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ppr_pred
operator|=
name|new_ppr_pred
expr_stmt|;
block|}
comment|// Put the mapping from table scan operator to ppr_pred
name|opToPPR
operator|.
name|put
argument_list|(
name|top
argument_list|,
name|ppr_pred
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
comment|/**    * Default processor which just merges its children    */
specifier|public
specifier|static
class|class
name|DefaultPPR
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
comment|// Nothing needs to be done.
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getFilterProc
parameter_list|()
block|{
return|return
operator|new
name|FilterPPR
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getDefaultProc
parameter_list|()
block|{
return|return
operator|new
name|DefaultPPR
argument_list|()
return|;
block|}
block|}
end_class

end_unit

