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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Multimap
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
name|adapter
operator|.
name|druid
operator|.
name|DruidQuery
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
name|Convention
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
name|ConventionTraitDef
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
name|RelOptCostImpl
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
name|RelOptPlanner
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
name|volcano
operator|.
name|RelSubset
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
name|volcano
operator|.
name|VolcanoPlanner
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
name|RelCollationTraitDef
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
name|TableScan
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
name|commons
operator|.
name|math3
operator|.
name|util
operator|.
name|FastMath
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
name|HiveConfPlannerContext
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
name|HivePlannerContext
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
name|RelOptHiveTable
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
name|rules
operator|.
name|HiveDruidRules
import|;
end_import

begin_comment
comment|/**  * Refinement of {@link org.apache.calcite.plan.volcano.VolcanoPlanner} for Hive.  *   *<p>  * It uses {@link org.apache.hadoop.hive.ql.optimizer.calcite.cost.HiveCost} as  * its cost model.  */
end_comment

begin_class
specifier|public
class|class
name|HiveVolcanoPlanner
extends|extends
name|VolcanoPlanner
block|{
specifier|private
specifier|static
specifier|final
name|boolean
name|ENABLE_COLLATION_TRAIT
init|=
literal|true
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isHeuristic
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|double
name|FACTOR
init|=
literal|0.2d
decl_stmt|;
comment|/** Creates a HiveVolcanoPlanner. */
specifier|public
name|HiveVolcanoPlanner
parameter_list|(
name|HivePlannerContext
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|HiveCost
operator|.
name|FACTORY
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|isHeuristic
operator|=
name|conf
operator|.
name|unwrap
argument_list|(
name|HiveConfPlannerContext
operator|.
name|class
argument_list|)
operator|.
name|isHeuristicMaterializationStrategy
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|RelOptPlanner
name|createPlanner
parameter_list|(
name|HivePlannerContext
name|conf
parameter_list|)
block|{
specifier|final
name|VolcanoPlanner
name|planner
init|=
operator|new
name|HiveVolcanoPlanner
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|planner
operator|.
name|addRelTraitDef
argument_list|(
name|ConventionTraitDef
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
if|if
condition|(
name|ENABLE_COLLATION_TRAIT
condition|)
block|{
name|planner
operator|.
name|addRelTraitDef
argument_list|(
name|RelCollationTraitDef
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
return|return
name|planner
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|registerClass
parameter_list|(
name|RelNode
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|DruidQuery
condition|)
block|{
comment|// Special handling for Druid rules here as otherwise
comment|// planner will add Druid rules with logical builder
name|addRule
argument_list|(
name|HiveDruidRules
operator|.
name|FILTER
argument_list|)
expr_stmt|;
name|addRule
argument_list|(
name|HiveDruidRules
operator|.
name|PROJECT_FILTER_TRANSPOSE
argument_list|)
expr_stmt|;
name|addRule
argument_list|(
name|HiveDruidRules
operator|.
name|AGGREGATE_FILTER_TRANSPOSE
argument_list|)
expr_stmt|;
name|addRule
argument_list|(
name|HiveDruidRules
operator|.
name|AGGREGATE_PROJECT
argument_list|)
expr_stmt|;
name|addRule
argument_list|(
name|HiveDruidRules
operator|.
name|PROJECT
argument_list|)
expr_stmt|;
name|addRule
argument_list|(
name|HiveDruidRules
operator|.
name|AGGREGATE
argument_list|)
expr_stmt|;
name|addRule
argument_list|(
name|HiveDruidRules
operator|.
name|POST_AGGREGATION_PROJECT
argument_list|)
expr_stmt|;
name|addRule
argument_list|(
name|HiveDruidRules
operator|.
name|FILTER_AGGREGATE_TRANSPOSE
argument_list|)
expr_stmt|;
name|addRule
argument_list|(
name|HiveDruidRules
operator|.
name|FILTER_PROJECT_TRANSPOSE
argument_list|)
expr_stmt|;
name|addRule
argument_list|(
name|HiveDruidRules
operator|.
name|SORT_PROJECT_TRANSPOSE
argument_list|)
expr_stmt|;
name|addRule
argument_list|(
name|HiveDruidRules
operator|.
name|SORT
argument_list|)
expr_stmt|;
name|addRule
argument_list|(
name|HiveDruidRules
operator|.
name|PROJECT_SORT_TRANSPOSE
argument_list|)
expr_stmt|;
return|return;
block|}
name|super
operator|.
name|registerClass
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
comment|/**    * The method extends the logic of the super method to decrease    * the cost of the plan if it contains materialized views    * (heuristic).    */
specifier|public
name|RelOptCost
name|getCost
parameter_list|(
name|RelNode
name|rel
parameter_list|,
name|RelMetadataQuery
name|mq
parameter_list|)
block|{
assert|assert
name|rel
operator|!=
literal|null
operator|:
literal|"pre-condition: rel != null"
assert|;
if|if
condition|(
name|rel
operator|instanceof
name|RelSubset
condition|)
block|{
return|return
name|getCost
argument_list|(
operator|(
operator|(
name|RelSubset
operator|)
name|rel
operator|)
operator|.
name|getBest
argument_list|()
argument_list|,
name|mq
argument_list|)
return|;
block|}
if|if
condition|(
name|rel
operator|.
name|getTraitSet
argument_list|()
operator|.
name|getTrait
argument_list|(
name|ConventionTraitDef
operator|.
name|INSTANCE
argument_list|)
operator|==
name|Convention
operator|.
name|NONE
condition|)
block|{
return|return
name|costFactory
operator|.
name|makeInfiniteCost
argument_list|()
return|;
block|}
comment|// We get the cost of the operator
name|RelOptCost
name|cost
init|=
name|mq
operator|.
name|getNonCumulativeCost
argument_list|(
name|rel
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|costFactory
operator|.
name|makeZeroCost
argument_list|()
operator|.
name|isLt
argument_list|(
name|cost
argument_list|)
condition|)
block|{
comment|// cost must be positive, so nudge it
name|cost
operator|=
name|costFactory
operator|.
name|makeTinyCost
argument_list|()
expr_stmt|;
block|}
comment|// If this operator has a materialized view below,
comment|// we make its cost tiny and adjust the cost of its
comment|// inputs
name|boolean
name|usesMaterializedViews
init|=
literal|false
decl_stmt|;
name|Multimap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|RelNode
argument_list|>
argument_list|,
name|RelNode
argument_list|>
name|nodeTypes
init|=
name|mq
operator|.
name|getNodeTypes
argument_list|(
name|rel
argument_list|)
decl_stmt|;
for|for
control|(
name|RelNode
name|scan
range|:
name|nodeTypes
operator|.
name|get
argument_list|(
name|TableScan
operator|.
name|class
argument_list|)
control|)
block|{
if|if
condition|(
operator|(
operator|(
name|RelOptHiveTable
operator|)
name|scan
operator|.
name|getTable
argument_list|()
operator|)
operator|.
name|getHiveTableMD
argument_list|()
operator|.
name|isMaterializedView
argument_list|()
condition|)
block|{
name|usesMaterializedViews
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|isHeuristic
operator|&&
name|usesMaterializedViews
condition|)
block|{
name|cost
operator|=
name|costFactory
operator|.
name|makeTinyCost
argument_list|()
expr_stmt|;
for|for
control|(
name|RelNode
name|input
range|:
name|rel
operator|.
name|getInputs
argument_list|()
control|)
block|{
comment|// If a child of this expression uses a materialized view,
comment|// then we decrease its cost by a certain factor. This is
comment|// useful for e.g. partial rewritings, where a part of plan
comment|// does not use the materialization, but we still want to
comment|// decrease its cost so it is chosen instead of the original
comment|// plan
name|cost
operator|=
name|cost
operator|.
name|plus
argument_list|(
name|getCost
argument_list|(
name|input
argument_list|,
name|mq
argument_list|)
operator|.
name|multiplyBy
argument_list|(
name|FACTOR
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// No materialized view or not heuristic approach, normal costing
for|for
control|(
name|RelNode
name|input
range|:
name|rel
operator|.
name|getInputs
argument_list|()
control|)
block|{
name|cost
operator|=
name|cost
operator|.
name|plus
argument_list|(
name|getCost
argument_list|(
name|input
argument_list|,
name|mq
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|cost
return|;
block|}
block|}
end_class

end_unit

