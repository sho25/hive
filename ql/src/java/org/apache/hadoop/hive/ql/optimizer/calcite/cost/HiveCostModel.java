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
name|calcite
operator|.
name|cost
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptCost
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelCollation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelDistribution
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|metadata
operator|.
name|RelMetadataQuery
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
name|calcite
operator|.
name|reloperators
operator|.
name|HiveAggregate
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
name|calcite
operator|.
name|reloperators
operator|.
name|HiveJoin
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
name|calcite
operator|.
name|reloperators
operator|.
name|HiveTableScan
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
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
comment|/**  * Cost model interface.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HiveCostModel
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
name|HiveCostModel
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|JoinAlgorithm
argument_list|>
name|joinAlgorithms
decl_stmt|;
specifier|public
name|HiveCostModel
parameter_list|(
name|Set
argument_list|<
name|JoinAlgorithm
argument_list|>
name|joinAlgorithms
parameter_list|)
block|{
name|this
operator|.
name|joinAlgorithms
operator|=
name|joinAlgorithms
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|RelOptCost
name|getDefaultCost
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|RelOptCost
name|getAggregateCost
parameter_list|(
name|HiveAggregate
name|aggregate
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|RelOptCost
name|getScanCost
parameter_list|(
name|HiveTableScan
name|ts
parameter_list|,
name|RelMetadataQuery
name|mq
parameter_list|)
function_decl|;
specifier|public
name|RelOptCost
name|getJoinCost
parameter_list|(
name|HiveJoin
name|join
parameter_list|)
block|{
comment|// Select algorithm with min cost
name|JoinAlgorithm
name|joinAlgorithm
init|=
literal|null
decl_stmt|;
name|RelOptCost
name|minJoinCost
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Join algorithm selection for:\n"
operator|+
name|RelOptUtil
operator|.
name|toString
argument_list|(
name|join
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|JoinAlgorithm
name|possibleAlgorithm
range|:
name|this
operator|.
name|joinAlgorithms
control|)
block|{
if|if
condition|(
operator|!
name|possibleAlgorithm
operator|.
name|isExecutable
argument_list|(
name|join
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|RelOptCost
name|joinCost
init|=
name|possibleAlgorithm
operator|.
name|getCost
argument_list|(
name|join
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|possibleAlgorithm
operator|+
literal|" cost: "
operator|+
name|joinCost
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|minJoinCost
operator|==
literal|null
operator|||
name|joinCost
operator|.
name|isLt
argument_list|(
name|minJoinCost
argument_list|)
condition|)
block|{
name|joinAlgorithm
operator|=
name|possibleAlgorithm
expr_stmt|;
name|minJoinCost
operator|=
name|joinCost
expr_stmt|;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|joinAlgorithm
operator|+
literal|" selected"
argument_list|)
expr_stmt|;
block|}
name|join
operator|.
name|setJoinAlgorithm
argument_list|(
name|joinAlgorithm
argument_list|)
expr_stmt|;
name|join
operator|.
name|setJoinCost
argument_list|(
name|minJoinCost
argument_list|)
expr_stmt|;
return|return
name|minJoinCost
return|;
block|}
comment|/**    * Interface for join algorithm.    */
specifier|public
interface|interface
name|JoinAlgorithm
block|{
specifier|public
name|String
name|toString
parameter_list|()
function_decl|;
specifier|public
name|boolean
name|isExecutable
parameter_list|(
name|HiveJoin
name|join
parameter_list|)
function_decl|;
specifier|public
name|RelOptCost
name|getCost
parameter_list|(
name|HiveJoin
name|join
parameter_list|)
function_decl|;
specifier|public
name|ImmutableList
argument_list|<
name|RelCollation
argument_list|>
name|getCollation
parameter_list|(
name|HiveJoin
name|join
parameter_list|)
function_decl|;
specifier|public
name|RelDistribution
name|getDistribution
parameter_list|(
name|HiveJoin
name|join
parameter_list|)
function_decl|;
specifier|public
name|Double
name|getMemory
parameter_list|(
name|HiveJoin
name|join
parameter_list|)
function_decl|;
specifier|public
name|Double
name|getCumulativeMemoryWithinPhaseSplit
parameter_list|(
name|HiveJoin
name|join
parameter_list|)
function_decl|;
specifier|public
name|Boolean
name|isPhaseTransition
parameter_list|(
name|HiveJoin
name|join
parameter_list|)
function_decl|;
specifier|public
name|Integer
name|getSplitCount
parameter_list|(
name|HiveJoin
name|join
parameter_list|)
function_decl|;
block|}
block|}
end_class

end_unit

