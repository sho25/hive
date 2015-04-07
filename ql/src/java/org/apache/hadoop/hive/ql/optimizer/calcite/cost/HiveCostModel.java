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
name|EnumSet
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
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HiveCostModel
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// NOTE: COMMON_JOIN& SMB_JOIN are Sort Merge Join (in case of COMMON_JOIN
comment|// each parallel computation handles multiple splits where as in case of SMB
comment|// each parallel computation handles one bucket). MAP_JOIN and BUCKET_JOIN is
comment|// hash joins where MAP_JOIN keeps the whole data set of non streaming tables
comment|// in memory where as BUCKET_JOIN keeps only the b
specifier|public
enum|enum
name|JoinAlgorithm
block|{
name|NONE
block|,
name|COMMON_JOIN
block|,
name|MAP_JOIN
block|,
name|BUCKET_JOIN
block|,
name|SMB_JOIN
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
name|RelOptCost
name|getJoinCost
parameter_list|(
name|HiveJoin
name|join
parameter_list|)
block|{
comment|// Retrieve algorithms
name|EnumSet
argument_list|<
name|JoinAlgorithm
argument_list|>
name|possibleAlgorithms
init|=
name|getExecutableJoinAlgorithms
argument_list|(
name|join
argument_list|)
decl_stmt|;
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
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
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
name|possibleAlgorithms
control|)
block|{
name|RelOptCost
name|joinCost
init|=
name|getJoinCost
argument_list|(
name|join
argument_list|,
name|possibleAlgorithm
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
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
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|joinAlgorithm
operator|+
literal|" selected"
argument_list|)
expr_stmt|;
block|}
return|return
name|minJoinCost
return|;
block|}
comment|/**    * Returns the possible algorithms for a given join operator.    *    * @param join the join operator    * @return a set containing all the possible join algorithms that can be    * executed for this join operator    */
specifier|abstract
name|EnumSet
argument_list|<
name|JoinAlgorithm
argument_list|>
name|getExecutableJoinAlgorithms
parameter_list|(
name|HiveJoin
name|join
parameter_list|)
function_decl|;
comment|/**    * Returns the cost for a given algorithm and execution engine.    *    * @param join the join operator    * @param algorithm the join algorithm    * @return the cost for the given algorithm, or null if the algorithm is not    * defined for this execution engine    */
specifier|abstract
name|RelOptCost
name|getJoinCost
parameter_list|(
name|HiveJoin
name|join
parameter_list|,
name|JoinAlgorithm
name|algorithm
parameter_list|)
function_decl|;
block|}
end_class

end_unit

