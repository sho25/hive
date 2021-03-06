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
name|Stack
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|Operator
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
name|SemanticNodeProcessor
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
name|optimizer
operator|.
name|ConstantPropagateProcFactory
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
name|ParseContext
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
name|PrunedPartitionList
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
name|OperatorDesc
import|;
end_import

begin_comment
comment|/**  * PcrOpProcFactory contains processors that process expression tree of filter operators  * following table scan operators. It walks the expression tree of the filter operator  * to remove partition predicates when possible. If the filter operator can be removed,  * the whole operator is marked to be removed later on, otherwise the predicate is changed  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|PcrOpProcFactory
block|{
comment|// The log
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"hive.ql.optimizer.pcr.OpProcFactory"
argument_list|)
decl_stmt|;
comment|/**    * Remove partition condition in a filter operator when possible. This is    * called only when the filter follows a table scan operator.    */
specifier|public
specifier|static
class|class
name|FilterPCR
implements|implements
name|SemanticNodeProcessor
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
name|PcrOpWalkerCtx
name|owc
init|=
operator|(
name|PcrOpWalkerCtx
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
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|pop
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
name|pop
operator|=
name|top
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
name|pop
operator|=
name|fop2
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
if|if
condition|(
name|fop
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
comment|// It's not likely if there is no bug. But in case it happens, we must
comment|// have found a wrong filter operator. We skip the optimization then.
return|return
literal|null
return|;
block|}
name|ParseContext
name|pctx
init|=
name|owc
operator|.
name|getParseContext
argument_list|()
decl_stmt|;
name|PrunedPartitionList
name|prunedPartList
decl_stmt|;
try|try
block|{
name|String
name|alias
init|=
operator|(
name|String
operator|)
name|owc
operator|.
name|getParseContext
argument_list|()
operator|.
name|getTopOps
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|prunedPartList
operator|=
name|pctx
operator|.
name|getPrunedPartitions
argument_list|(
name|alias
argument_list|,
name|top
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
comment|// Has to use full name to make sure it does not conflict with
comment|// org.apache.commons.lang3.StringUtils
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// Otherwise this is not a sampling predicate. We need to process it.
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
name|ArrayList
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
operator|new
name|ArrayList
argument_list|<
name|Partition
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|prunedPartList
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|Partition
name|p
range|:
name|prunedPartList
operator|.
name|getPartitions
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|p
operator|.
name|getTable
argument_list|()
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
name|partitions
operator|.
name|addAll
argument_list|(
name|prunedPartList
operator|.
name|getPartitions
argument_list|()
argument_list|)
expr_stmt|;
name|PcrExprProcFactory
operator|.
name|NodeInfoWrapper
name|wrapper
init|=
name|PcrExprProcFactory
operator|.
name|walkExprTree
argument_list|(
name|alias
argument_list|,
name|partitions
argument_list|,
name|top
operator|.
name|getConf
argument_list|()
operator|.
name|getVirtualCols
argument_list|()
argument_list|,
name|predicate
argument_list|)
decl_stmt|;
if|if
condition|(
name|wrapper
operator|.
name|state
operator|==
name|PcrExprProcFactory
operator|.
name|WalkState
operator|.
name|TRUE
condition|)
block|{
name|owc
operator|.
name|getOpToRemove
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|PcrOpWalkerCtx
operator|.
name|OpToDeleteInfo
argument_list|(
name|pop
argument_list|,
name|fop
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|wrapper
operator|.
name|state
operator|==
name|PcrExprProcFactory
operator|.
name|WalkState
operator|.
name|CONSTANT
operator|&&
name|wrapper
operator|.
name|outExpr
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
name|ExprNodeDesc
name|desc
init|=
name|ConstantPropagateProcFactory
operator|.
name|foldExpr
argument_list|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|wrapper
operator|.
name|outExpr
argument_list|)
decl_stmt|;
if|if
condition|(
name|desc
operator|!=
literal|null
operator|&&
name|desc
operator|instanceof
name|ExprNodeConstantDesc
operator|&&
name|Boolean
operator|.
name|TRUE
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|ExprNodeConstantDesc
operator|)
name|desc
operator|)
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
name|owc
operator|.
name|getOpToRemove
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|PcrOpWalkerCtx
operator|.
name|OpToDeleteInfo
argument_list|(
name|pop
argument_list|,
name|fop
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fop
operator|.
name|getConf
argument_list|()
operator|.
name|setPredicate
argument_list|(
name|wrapper
operator|.
name|outExpr
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|wrapper
operator|.
name|state
operator|!=
name|PcrExprProcFactory
operator|.
name|WalkState
operator|.
name|FALSE
condition|)
block|{
name|fop
operator|.
name|getConf
argument_list|()
operator|.
name|setPredicate
argument_list|(
name|wrapper
operator|.
name|outExpr
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Filter passes no row"
argument_list|)
expr_stmt|;
name|fop
operator|.
name|getConf
argument_list|()
operator|.
name|setPredicate
argument_list|(
name|wrapper
operator|.
name|outExpr
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Default processor which does nothing    */
specifier|public
specifier|static
class|class
name|DefaultPCR
implements|implements
name|SemanticNodeProcessor
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
name|SemanticNodeProcessor
name|getFilterProc
parameter_list|()
block|{
return|return
operator|new
name|FilterPCR
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|SemanticNodeProcessor
name|getDefaultProc
parameter_list|()
block|{
return|return
operator|new
name|DefaultPCR
argument_list|()
return|;
block|}
specifier|private
name|PcrOpProcFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

