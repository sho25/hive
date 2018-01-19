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
name|optimizer
operator|.
name|ppr
package|;
end_package

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
name|exec
operator|.
name|UDFArgumentException
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
name|optimizer
operator|.
name|PrunerOperatorFactory
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
name|ExprNodeDesc
import|;
end_import

begin_comment
comment|/**  * Operator factory for partition pruning processing of operator graph We find  * all the filter operators that appear just beneath the table scan operators.  * We then pass the filter to the partition pruner to construct a pruner for  * that table alias and store a mapping from the table scan operator to that  * pruner. We call that pruner later during plan generation.  *  *  * Refactor:  * Move main logic to PrunerOperatorFactory. OpProcFactory extends it to reuse logic.  *  * Any other pruner can reuse it by creating a class extending from PrunerOperatorFactory.  *  * Only specific logic is in generatePredicate(..) which is in its own class like OpProcFactory.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|OpProcFactory
extends|extends
name|PrunerOperatorFactory
block|{
comment|/**    * Determines the partition pruner for the filter. This is called only when    * the filter follows a table scan operator.    */
specifier|public
specifier|static
class|class
name|FilterPPR
extends|extends
name|FilterPruner
block|{
annotation|@
name|Override
specifier|protected
name|void
name|generatePredicate
parameter_list|(
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|FilterOperator
name|fop
parameter_list|,
name|TableScanOperator
name|top
parameter_list|)
throws|throws
name|SemanticException
throws|,
name|UDFArgumentException
block|{
name|OpWalkerCtx
name|owc
init|=
operator|(
name|OpWalkerCtx
operator|)
name|procCtx
decl_stmt|;
comment|// Otherwise this is not a sampling predicate and we need to
name|ExprNodeDesc
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
name|ExprNodeDesc
name|ppr_pred
init|=
name|ExprProcFactory
operator|.
name|genPruner
argument_list|(
name|alias
argument_list|,
name|predicate
argument_list|)
decl_stmt|;
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
specifier|private
name|OpProcFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

