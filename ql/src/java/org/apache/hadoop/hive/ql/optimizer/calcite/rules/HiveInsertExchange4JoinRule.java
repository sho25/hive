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
name|rules
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
name|List
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
name|RelOptRule
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
name|RelOptRuleCall
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
name|hep
operator|.
name|HepRelVertex
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
name|RelFieldCollation
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
name|RelNode
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
name|core
operator|.
name|Exchange
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
name|core
operator|.
name|Join
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
name|rules
operator|.
name|MultiJoin
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
name|rex
operator|.
name|RexNode
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
name|HiveCalciteUtil
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
name|HiveCalciteUtil
operator|.
name|JoinLeafPredicateInfo
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
name|HiveCalciteUtil
operator|.
name|JoinPredicateInfo
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
name|HiveRelCollation
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
name|HiveRelDistribution
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
name|HiveSortExchange
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

begin_comment
comment|/** Not an optimization rule.  * Rule to aid in translation from Calcite tree -> Hive tree.  * Transforms :  *   Left     Right                  Left                    Right  *       \   /           ->             \                   /  *       Join                          HashExchange       HashExchange  *                                             \         /  *                                                 Join  */
end_comment

begin_class
specifier|public
class|class
name|HiveInsertExchange4JoinRule
extends|extends
name|RelOptRule
block|{
specifier|protected
specifier|static
specifier|transient
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HiveInsertExchange4JoinRule
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Rule that creates Exchange operators under a MultiJoin operator. */
specifier|public
specifier|static
specifier|final
name|HiveInsertExchange4JoinRule
name|EXCHANGE_BELOW_MULTIJOIN
init|=
operator|new
name|HiveInsertExchange4JoinRule
argument_list|(
name|MultiJoin
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Rule that creates Exchange operators under a Join operator. */
specifier|public
specifier|static
specifier|final
name|HiveInsertExchange4JoinRule
name|EXCHANGE_BELOW_JOIN
init|=
operator|new
name|HiveInsertExchange4JoinRule
argument_list|(
name|Join
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|HiveInsertExchange4JoinRule
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|RelNode
argument_list|>
name|clazz
parameter_list|)
block|{
comment|// match multijoin or join
name|super
argument_list|(
name|RelOptRule
operator|.
name|operand
argument_list|(
name|clazz
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
name|JoinPredicateInfo
name|joinPredInfo
decl_stmt|;
if|if
condition|(
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|MultiJoin
condition|)
block|{
name|MultiJoin
name|multiJoin
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|joinPredInfo
operator|=
name|HiveCalciteUtil
operator|.
name|JoinPredicateInfo
operator|.
name|constructJoinPredicateInfo
argument_list|(
name|multiJoin
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|Join
condition|)
block|{
name|Join
name|join
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|joinPredInfo
operator|=
name|HiveCalciteUtil
operator|.
name|JoinPredicateInfo
operator|.
name|constructJoinPredicateInfo
argument_list|(
name|join
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return;
block|}
for|for
control|(
name|RelNode
name|child
range|:
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
operator|.
name|getInputs
argument_list|()
control|)
block|{
if|if
condition|(
operator|(
operator|(
name|HepRelVertex
operator|)
name|child
operator|)
operator|.
name|getCurrentRel
argument_list|()
operator|instanceof
name|Exchange
condition|)
block|{
return|return;
block|}
block|}
comment|// get key columns from inputs. Those are the columns on which we will distribute on.
comment|// It is also the columns we will sort on.
name|List
argument_list|<
name|RelNode
argument_list|>
name|newInputs
init|=
operator|new
name|ArrayList
argument_list|<
name|RelNode
argument_list|>
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
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
operator|.
name|getInputs
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|joinKeyPositions
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RexNode
argument_list|>
name|keyListBuilder
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelFieldCollation
argument_list|>
name|collationListBuilder
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelFieldCollation
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|joinPredInfo
operator|.
name|getEquiJoinPredicateElements
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|JoinLeafPredicateInfo
name|joinLeafPredInfo
init|=
name|joinPredInfo
operator|.
name|getEquiJoinPredicateElements
argument_list|()
operator|.
name|get
argument_list|(
name|j
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|pos
range|:
name|joinLeafPredInfo
operator|.
name|getProjsJoinKeysInChildSchema
argument_list|(
name|i
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|joinKeyPositions
operator|.
name|contains
argument_list|(
name|pos
argument_list|)
condition|)
block|{
name|joinKeyPositions
operator|.
name|add
argument_list|(
name|pos
argument_list|)
expr_stmt|;
name|collationListBuilder
operator|.
name|add
argument_list|(
operator|new
name|RelFieldCollation
argument_list|(
name|pos
argument_list|)
argument_list|)
expr_stmt|;
name|keyListBuilder
operator|.
name|add
argument_list|(
name|joinLeafPredInfo
operator|.
name|getJoinKeyExprs
argument_list|(
name|i
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|HiveSortExchange
name|exchange
init|=
name|HiveSortExchange
operator|.
name|create
argument_list|(
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
operator|.
name|getInput
argument_list|(
name|i
argument_list|)
argument_list|,
operator|new
name|HiveRelDistribution
argument_list|(
name|RelDistribution
operator|.
name|Type
operator|.
name|HASH_DISTRIBUTED
argument_list|,
name|joinKeyPositions
argument_list|)
argument_list|,
operator|new
name|HiveRelCollation
argument_list|(
name|collationListBuilder
operator|.
name|build
argument_list|()
argument_list|)
argument_list|,
name|keyListBuilder
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|newInputs
operator|.
name|add
argument_list|(
name|exchange
argument_list|)
expr_stmt|;
block|}
name|RelNode
name|newOp
decl_stmt|;
if|if
condition|(
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|MultiJoin
condition|)
block|{
name|MultiJoin
name|multiJoin
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|newOp
operator|=
name|multiJoin
operator|.
name|copy
argument_list|(
name|multiJoin
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|newInputs
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|Join
condition|)
block|{
name|Join
name|join
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|newOp
operator|=
name|join
operator|.
name|copy
argument_list|(
name|join
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|join
operator|.
name|getCondition
argument_list|()
argument_list|,
name|newInputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|newInputs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|join
operator|.
name|getJoinType
argument_list|()
argument_list|,
name|join
operator|.
name|isSemiJoinDone
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return;
block|}
name|call
operator|.
name|getPlanner
argument_list|()
operator|.
name|onCopy
argument_list|(
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
argument_list|,
name|newOp
argument_list|)
expr_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|newOp
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

