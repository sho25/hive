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
name|physical
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
name|FileSinkOperator
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
name|ForwardOperator
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
name|GroupByOperator
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
name|JoinOperator
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
name|LateralViewForwardOperator
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
name|LateralViewJoinOperator
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
name|SelectOperator
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
name|Utilities
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
name|mr
operator|.
name|ExecDriver
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
name|PreOrderWalker
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
name|RuleExactMatch
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
name|OperatorDesc
import|;
end_import

begin_comment
comment|/**  *  * BucketingSortingInferenceOptimizer.  *  * For each map reduce task, attmepts to infer bucketing and sorting metadata for the outputs.  *  * Currently only map reduce tasks which produce final output have there output metadata inferred,  * but it can be extended to intermediate tasks as well.  *  * This should be run as the last physical optimizer, as other physical optimizers may invalidate  * the inferences made.  If a physical optimizer depends on the results and is designed to  * carefully maintain these inferences, it may follow this one.  */
end_comment

begin_class
specifier|public
class|class
name|BucketingSortingInferenceOptimizer
implements|implements
name|PhysicalPlanResolver
block|{
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
name|inferBucketingSorting
argument_list|(
name|Utilities
operator|.
name|getMRTasks
argument_list|(
name|pctx
operator|.
name|rootTasks
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|pctx
return|;
block|}
comment|/**    * For each map reduce task, if it has a reducer, infer whether or not the final output of the    * reducer is bucketed and/or sorted    *    * @param mapRedTasks    * @throws SemanticException    */
specifier|private
name|void
name|inferBucketingSorting
parameter_list|(
name|List
argument_list|<
name|ExecDriver
argument_list|>
name|mapRedTasks
parameter_list|)
throws|throws
name|SemanticException
block|{
for|for
control|(
name|ExecDriver
name|mapRedTask
range|:
name|mapRedTasks
control|)
block|{
comment|// For now this only is used to determine the bucketing/sorting of outputs, in the future
comment|// this can be removed to optimize the query plan based on the bucketing/sorting properties
comment|// of the outputs of intermediate map reduce jobs.
if|if
condition|(
operator|!
name|mapRedTask
operator|.
name|getWork
argument_list|()
operator|.
name|isFinalMapRed
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|mapRedTask
operator|.
name|getWork
argument_list|()
operator|.
name|getReduceWork
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|reducer
init|=
name|mapRedTask
operator|.
name|getWork
argument_list|()
operator|.
name|getReduceWork
argument_list|()
operator|.
name|getReducer
argument_list|()
decl_stmt|;
comment|// uses sampling, which means it's not bucketed
name|boolean
name|disableBucketing
init|=
name|mapRedTask
operator|.
name|getWork
argument_list|()
operator|.
name|getMapWork
argument_list|()
operator|.
name|getSamplingType
argument_list|()
operator|>
literal|0
decl_stmt|;
name|BucketingSortingCtx
name|bCtx
init|=
operator|new
name|BucketingSortingCtx
argument_list|(
name|disableBucketing
argument_list|)
decl_stmt|;
comment|// RuleRegExp rules are used to match operators anywhere in the tree
comment|// RuleExactMatch rules are used to specify exactly what the tree should look like
comment|// In particular, this guarantees that the first operator is the reducer
comment|// (and its parent(s) are ReduceSinkOperators) since it begins walking the tree from
comment|// the reducer.
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
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
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
name|SelectOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|BucketingSortingOpProcFactory
operator|.
name|getSelProc
argument_list|()
argument_list|)
expr_stmt|;
comment|// Matches only GroupByOperators which are reducers, rather than map group by operators,
comment|// or multi group by optimization specific operators
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleExactMatch
argument_list|(
literal|"R2"
argument_list|,
name|GroupByOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|BucketingSortingOpProcFactory
operator|.
name|getGroupByProc
argument_list|()
argument_list|)
expr_stmt|;
comment|// Matches only JoinOperators which are reducers, rather than map joins, SMB map joins, etc.
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleExactMatch
argument_list|(
literal|"R3"
argument_list|,
name|JoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|BucketingSortingOpProcFactory
operator|.
name|getJoinProc
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
literal|"R5"
argument_list|,
name|FileSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|BucketingSortingOpProcFactory
operator|.
name|getFileSinkProc
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
literal|"R7"
argument_list|,
name|FilterOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|BucketingSortingOpProcFactory
operator|.
name|getFilterProc
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
literal|"R8"
argument_list|,
name|LimitOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|BucketingSortingOpProcFactory
operator|.
name|getLimitProc
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
literal|"R9"
argument_list|,
name|LateralViewForwardOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|BucketingSortingOpProcFactory
operator|.
name|getLateralViewForwardProc
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
literal|"R10"
argument_list|,
name|LateralViewJoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|BucketingSortingOpProcFactory
operator|.
name|getLateralViewJoinProc
argument_list|()
argument_list|)
expr_stmt|;
comment|// Matches only ForwardOperators which are preceded by some other operator in the tree,
comment|// in particular it can't be a reducer (and hence cannot be one of the ForwardOperators
comment|// added by the multi group by optimization)
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R11"
argument_list|,
literal|".+"
operator|+
name|ForwardOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|BucketingSortingOpProcFactory
operator|.
name|getForwardProc
argument_list|()
argument_list|)
expr_stmt|;
comment|// Matches only ForwardOperators which are reducers and are followed by GroupByOperators
comment|// (specific to the multi group by optimization)
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleExactMatch
argument_list|(
literal|"R12"
argument_list|,
name|ForwardOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
operator|+
name|GroupByOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|BucketingSortingOpProcFactory
operator|.
name|getMultiGroupByProc
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching rule and passes
comment|// the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|BucketingSortingOpProcFactory
operator|.
name|getDefaultProc
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|bCtx
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|PreOrderWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// Create a list of topop nodes
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|topNodes
operator|.
name|add
argument_list|(
name|reducer
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
name|mapRedTask
operator|.
name|getWork
argument_list|()
operator|.
name|getMapWork
argument_list|()
operator|.
name|getBucketedColsByDirectory
argument_list|()
operator|.
name|putAll
argument_list|(
name|bCtx
operator|.
name|getBucketedColsByDirectory
argument_list|()
argument_list|)
expr_stmt|;
name|mapRedTask
operator|.
name|getWork
argument_list|()
operator|.
name|getMapWork
argument_list|()
operator|.
name|getSortedColsByDirectory
argument_list|()
operator|.
name|putAll
argument_list|(
name|bCtx
operator|.
name|getSortedColsByDirectory
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

