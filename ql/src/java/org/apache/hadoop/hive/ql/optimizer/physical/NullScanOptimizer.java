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
name|physical
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayDeque
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
name|Iterator
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
name|Queue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|commons
operator|.
name|collections
operator|.
name|CollectionUtils
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
name|LimitOperator
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
name|optimizer
operator|.
name|physical
operator|.
name|MetadataOnlyOptimizer
operator|.
name|WalkerCtx
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

begin_comment
comment|/**  * This optimizer attempts following two optimizations:  * 1. If it finds TS followed By FIL which has been determined at compile time to evaluate to  *    zero, it removes all input paths for that table scan.  * 2. If it finds TS followed by Limit 0, it removes all input paths from table scan.  */
end_comment

begin_class
specifier|public
class|class
name|NullScanOptimizer
implements|implements
name|PhysicalPlanResolver
block|{
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
name|NullScanOptimizer
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|PhysicalContext
name|resolve
parameter_list|(
name|PhysicalContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|opRules
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%.*"
operator|+
name|FilterOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|WhereFalseProcessor
argument_list|()
argument_list|)
expr_stmt|;
name|Dispatcher
name|disp
init|=
operator|new
name|NullScanTaskDispatcher
argument_list|(
name|pctx
argument_list|,
name|opRules
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
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
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|pctx
operator|.
name|getRootTasks
argument_list|()
argument_list|)
decl_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|clear
argument_list|()
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|TSMarker
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R2"
argument_list|,
name|LimitOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|Limit0Processor
argument_list|()
argument_list|)
expr_stmt|;
name|disp
operator|=
operator|new
name|NullScanTaskDispatcher
argument_list|(
name|pctx
argument_list|,
name|opRules
argument_list|)
expr_stmt|;
name|ogw
operator|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
expr_stmt|;
name|topNodes
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|pctx
operator|.
name|getRootTasks
argument_list|()
argument_list|)
expr_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|pctx
return|;
block|}
comment|//We need to make sure that Null Operator (LIM or FIL) is present in all branches of multi-insert query before
comment|//applying the optimization. This method does full tree traversal starting from TS and will return true only if
comment|//it finds target Null operator on each branch.
specifier|private
specifier|static
name|boolean
name|isNullOpPresentInAllBranches
parameter_list|(
name|TableScanOperator
name|ts
parameter_list|,
name|Node
name|causeOfNullNode
parameter_list|)
block|{
name|Queue
argument_list|<
name|Node
argument_list|>
name|middleNodes
init|=
operator|new
name|ArrayDeque
argument_list|<>
argument_list|()
decl_stmt|;
name|middleNodes
operator|.
name|add
argument_list|(
name|ts
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|middleNodes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Node
name|curNode
init|=
name|middleNodes
operator|.
name|remove
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Node
argument_list|>
name|curChd
init|=
name|curNode
operator|.
name|getChildren
argument_list|()
decl_stmt|;
for|for
control|(
name|Node
name|chd
range|:
name|curChd
control|)
block|{
name|List
argument_list|<
name|?
extends|extends
name|Node
argument_list|>
name|children
init|=
name|chd
operator|.
name|getChildren
argument_list|()
decl_stmt|;
if|if
condition|(
name|CollectionUtils
operator|.
name|isEmpty
argument_list|(
name|children
argument_list|)
operator|||
name|chd
operator|==
name|causeOfNullNode
condition|)
block|{
comment|// If there is an end node that not the limit0/wherefalse..
if|if
condition|(
name|chd
operator|!=
name|causeOfNullNode
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
else|else
block|{
name|middleNodes
operator|.
name|add
argument_list|(
name|chd
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
specifier|static
class|class
name|WhereFalseProcessor
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
name|FilterOperator
name|filter
init|=
operator|(
name|FilterOperator
operator|)
name|nd
decl_stmt|;
name|ExprNodeDesc
name|condition
init|=
name|filter
operator|.
name|getConf
argument_list|()
operator|.
name|getPredicate
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|condition
operator|instanceof
name|ExprNodeConstantDesc
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|ExprNodeConstantDesc
name|c
init|=
operator|(
name|ExprNodeConstantDesc
operator|)
name|condition
decl_stmt|;
if|if
condition|(
operator|!
name|Boolean
operator|.
name|FALSE
operator|.
name|equals
argument_list|(
name|c
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|WalkerCtx
name|ctx
init|=
operator|(
name|WalkerCtx
operator|)
name|procCtx
decl_stmt|;
for|for
control|(
name|Node
name|op
range|:
name|stack
control|)
block|{
if|if
condition|(
name|op
operator|instanceof
name|TableScanOperator
condition|)
block|{
if|if
condition|(
name|isNullOpPresentInAllBranches
argument_list|(
operator|(
name|TableScanOperator
operator|)
name|op
argument_list|,
name|filter
argument_list|)
condition|)
block|{
name|ctx
operator|.
name|setMayBeMetadataOnly
argument_list|(
operator|(
name|TableScanOperator
operator|)
name|op
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found where false TableScan. {}"
argument_list|,
name|op
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|ctx
operator|.
name|convertMetadataOnly
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|Limit0Processor
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
name|LimitOperator
name|limitOp
init|=
operator|(
name|LimitOperator
operator|)
name|nd
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|limitOp
operator|.
name|getConf
argument_list|()
operator|.
name|getLimit
argument_list|()
operator|==
literal|0
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Set
argument_list|<
name|TableScanOperator
argument_list|>
name|tsOps
init|=
operator|(
operator|(
name|WalkerCtx
operator|)
name|procCtx
operator|)
operator|.
name|getMayBeMetadataOnlyTableScans
argument_list|()
decl_stmt|;
if|if
condition|(
name|tsOps
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Iterator
argument_list|<
name|TableScanOperator
argument_list|>
name|tsOp
init|=
name|tsOps
operator|.
name|iterator
argument_list|()
init|;
name|tsOp
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
if|if
condition|(
operator|!
name|isNullOpPresentInAllBranches
argument_list|(
name|tsOp
operator|.
name|next
argument_list|()
argument_list|,
name|limitOp
argument_list|)
condition|)
block|{
name|tsOp
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found Limit 0 TableScan. {}"
argument_list|,
name|nd
argument_list|)
expr_stmt|;
operator|(
operator|(
name|WalkerCtx
operator|)
name|procCtx
operator|)
operator|.
name|convertMetadataOnly
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TSMarker
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
operator|(
operator|(
name|WalkerCtx
operator|)
name|procCtx
operator|)
operator|.
name|setMayBeMetadataOnly
argument_list|(
operator|(
name|TableScanOperator
operator|)
name|nd
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

