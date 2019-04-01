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
name|rules
operator|.
name|views
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
name|ImmutableList
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
name|avatica
operator|.
name|util
operator|.
name|TimeUnitRange
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
name|HepProgram
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
name|HepProgramBuilder
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
name|Filter
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
name|rules
operator|.
name|AbstractMaterializedViewRule
operator|.
name|MaterializedViewProjectFilterRule
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
name|AbstractMaterializedViewRule
operator|.
name|MaterializedViewOnlyFilterRule
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
name|AbstractMaterializedViewRule
operator|.
name|MaterializedViewProjectJoinRule
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
name|AbstractMaterializedViewRule
operator|.
name|MaterializedViewOnlyJoinRule
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
name|AbstractMaterializedViewRule
operator|.
name|MaterializedViewProjectAggregateRule
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
name|AbstractMaterializedViewRule
operator|.
name|MaterializedViewOnlyAggregateRule
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
name|ProjectRemoveRule
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
name|calcite
operator|.
name|sql
operator|.
name|SqlAggFunction
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
name|sql
operator|.
name|SqlFunction
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
name|tools
operator|.
name|RelBuilder
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
name|tools
operator|.
name|RelBuilderFactory
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
name|util
operator|.
name|ImmutableBitSet
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
name|util
operator|.
name|Util
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
name|HiveRelBuilder
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
name|HiveRelFactories
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
name|HiveFilterProjectTransposeRule
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
name|HiveJoinProjectTransposeRule
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
name|HiveProjectMergeRule
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

begin_comment
comment|/**  * Enable join and aggregate materialized view rewriting  */
end_comment

begin_class
specifier|public
class|class
name|HiveMaterializedViewRule
block|{
comment|/**    * This PROGRAM will be executed when there is a partial rewriting    * (using union operator) to pull up the projection expressions    * on top of the input that executes the modified query. The goal    * of the program is to expose all available expressions below    * the root of the plan.    */
specifier|private
specifier|static
specifier|final
name|HepProgram
name|PROGRAM
init|=
operator|new
name|HepProgramBuilder
argument_list|()
operator|.
name|addRuleInstance
argument_list|(
name|HiveHepExtractRelNodeRule
operator|.
name|INSTANCE
argument_list|)
operator|.
name|addRuleInstance
argument_list|(
name|HiveVolcanoExtractRelNodeRule
operator|.
name|INSTANCE
argument_list|)
operator|.
name|addRuleInstance
argument_list|(
name|HiveTableScanProjectInsert
operator|.
name|INSTANCE
argument_list|)
operator|.
name|addRuleCollection
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
name|HiveFilterProjectTransposeRule
operator|.
name|INSTANCE
argument_list|,
name|HiveJoinProjectTransposeRule
operator|.
name|BOTH_PROJECT
argument_list|,
name|HiveJoinProjectTransposeRule
operator|.
name|LEFT_PROJECT
argument_list|,
name|HiveJoinProjectTransposeRule
operator|.
name|RIGHT_PROJECT
argument_list|,
name|HiveProjectMergeRule
operator|.
name|INSTANCE
argument_list|)
argument_list|)
operator|.
name|addRuleInstance
argument_list|(
name|ProjectRemoveRule
operator|.
name|INSTANCE
argument_list|)
operator|.
name|addRuleInstance
argument_list|(
name|HiveRootJoinProjectInsert
operator|.
name|INSTANCE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|MaterializedViewProjectFilterRule
name|INSTANCE_PROJECT_FILTER
init|=
operator|new
name|MaterializedViewProjectFilterRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|,
literal|true
argument_list|,
name|PROGRAM
argument_list|,
literal|false
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|MaterializedViewOnlyFilterRule
name|INSTANCE_FILTER
init|=
operator|new
name|MaterializedViewOnlyFilterRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|,
literal|true
argument_list|,
name|PROGRAM
argument_list|,
literal|false
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|MaterializedViewProjectJoinRule
name|INSTANCE_PROJECT_JOIN
init|=
operator|new
name|MaterializedViewProjectJoinRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|,
literal|true
argument_list|,
name|PROGRAM
argument_list|,
literal|false
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|MaterializedViewOnlyJoinRule
name|INSTANCE_JOIN
init|=
operator|new
name|MaterializedViewOnlyJoinRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|,
literal|true
argument_list|,
name|PROGRAM
argument_list|,
literal|false
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveMaterializedViewProjectAggregateRule
name|INSTANCE_PROJECT_AGGREGATE
init|=
operator|new
name|HiveMaterializedViewProjectAggregateRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|,
literal|true
argument_list|,
name|PROGRAM
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveMaterializedViewOnlyAggregateRule
name|INSTANCE_AGGREGATE
init|=
operator|new
name|HiveMaterializedViewOnlyAggregateRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|,
literal|true
argument_list|,
name|PROGRAM
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|RelOptRule
index|[]
name|MATERIALIZED_VIEW_REWRITING_RULES
init|=
operator|new
name|RelOptRule
index|[]
block|{
name|HiveMaterializedViewRule
operator|.
name|INSTANCE_PROJECT_FILTER
block|,
name|HiveMaterializedViewRule
operator|.
name|INSTANCE_FILTER
block|,
name|HiveMaterializedViewRule
operator|.
name|INSTANCE_PROJECT_JOIN
block|,
name|HiveMaterializedViewRule
operator|.
name|INSTANCE_JOIN
block|,
name|HiveMaterializedViewRule
operator|.
name|INSTANCE_PROJECT_AGGREGATE
block|,
name|HiveMaterializedViewRule
operator|.
name|INSTANCE_AGGREGATE
block|}
decl_stmt|;
specifier|protected
specifier|static
class|class
name|HiveMaterializedViewProjectAggregateRule
extends|extends
name|MaterializedViewProjectAggregateRule
block|{
specifier|public
name|HiveMaterializedViewProjectAggregateRule
parameter_list|(
name|RelBuilderFactory
name|relBuilderFactory
parameter_list|,
name|boolean
name|generateUnionRewriting
parameter_list|,
name|HepProgram
name|unionRewritingPullProgram
parameter_list|)
block|{
name|super
argument_list|(
name|relBuilderFactory
argument_list|,
name|generateUnionRewriting
argument_list|,
name|unionRewritingPullProgram
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|SqlFunction
name|getFloorSqlFunction
parameter_list|(
name|TimeUnitRange
name|flag
parameter_list|)
block|{
return|return
name|HiveRelBuilder
operator|.
name|getFloorSqlFunction
argument_list|(
name|flag
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SqlAggFunction
name|getRollup
parameter_list|(
name|SqlAggFunction
name|aggregation
parameter_list|)
block|{
return|return
name|HiveRelBuilder
operator|.
name|getRollup
argument_list|(
name|aggregation
argument_list|)
return|;
block|}
block|}
specifier|protected
specifier|static
class|class
name|HiveMaterializedViewOnlyAggregateRule
extends|extends
name|MaterializedViewOnlyAggregateRule
block|{
specifier|public
name|HiveMaterializedViewOnlyAggregateRule
parameter_list|(
name|RelBuilderFactory
name|relBuilderFactory
parameter_list|,
name|boolean
name|generateUnionRewriting
parameter_list|,
name|HepProgram
name|unionRewritingPullProgram
parameter_list|)
block|{
name|super
argument_list|(
name|relBuilderFactory
argument_list|,
name|generateUnionRewriting
argument_list|,
name|unionRewritingPullProgram
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|SqlFunction
name|getFloorSqlFunction
parameter_list|(
name|TimeUnitRange
name|flag
parameter_list|)
block|{
return|return
name|HiveRelBuilder
operator|.
name|getFloorSqlFunction
argument_list|(
name|flag
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SqlAggFunction
name|getRollup
parameter_list|(
name|SqlAggFunction
name|aggregation
parameter_list|)
block|{
return|return
name|HiveRelBuilder
operator|.
name|getRollup
argument_list|(
name|aggregation
argument_list|)
return|;
block|}
block|}
comment|/**    * This rule is used within the PROGRAM that rewrites the query for    * partial rewritings. Its goal is to extract the RelNode from the    * HepRelVertex node so the rest of the rules in the PROGRAM can be    * applied correctly.    */
specifier|private
specifier|static
class|class
name|HiveHepExtractRelNodeRule
extends|extends
name|RelOptRule
block|{
specifier|private
specifier|static
specifier|final
name|HiveHepExtractRelNodeRule
name|INSTANCE
init|=
operator|new
name|HiveHepExtractRelNodeRule
argument_list|()
decl_stmt|;
specifier|private
name|HiveHepExtractRelNodeRule
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|HepRelVertex
operator|.
name|class
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
specifier|final
name|HepRelVertex
name|rel
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|rel
operator|.
name|getCurrentRel
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This rule is used within the PROGRAM that rewrites the query for    * partial rewritings. Its goal is to extract the RelNode from the    * RelSubset node so the rest of the rules in the PROGRAM can be    * applied correctly.    */
specifier|private
specifier|static
class|class
name|HiveVolcanoExtractRelNodeRule
extends|extends
name|RelOptRule
block|{
specifier|private
specifier|static
specifier|final
name|HiveVolcanoExtractRelNodeRule
name|INSTANCE
init|=
operator|new
name|HiveVolcanoExtractRelNodeRule
argument_list|()
decl_stmt|;
specifier|private
name|HiveVolcanoExtractRelNodeRule
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|RelSubset
operator|.
name|class
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
specifier|final
name|RelSubset
name|rel
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|Util
operator|.
name|first
argument_list|(
name|rel
operator|.
name|getBest
argument_list|()
argument_list|,
name|rel
operator|.
name|getOriginal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This rule inserts an identity Project operator on top of a TableScan.    * The rule is useful to pull-up the projection expressions during partial    * rewriting using Union operator, as we would like to have all those    * expressions available at the top of the input to insert Filter conditions    * if needed.    */
specifier|private
specifier|static
class|class
name|HiveTableScanProjectInsert
extends|extends
name|RelOptRule
block|{
specifier|private
specifier|static
specifier|final
name|HiveTableScanProjectInsert
name|INSTANCE
init|=
operator|new
name|HiveTableScanProjectInsert
argument_list|()
decl_stmt|;
specifier|private
name|HiveTableScanProjectInsert
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|Filter
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|TableScan
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|,
literal|"HiveTableScanProjectInsert"
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
specifier|final
name|Filter
name|fil
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|TableScan
name|rel
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// Add identity
name|RelBuilder
name|relBuilder
init|=
name|call
operator|.
name|builder
argument_list|()
decl_stmt|;
name|relBuilder
operator|.
name|push
argument_list|(
name|rel
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RexNode
argument_list|>
name|identityFields
init|=
name|relBuilder
operator|.
name|fields
argument_list|(
name|ImmutableBitSet
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|rel
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldCount
argument_list|()
argument_list|)
operator|.
name|asList
argument_list|()
argument_list|)
decl_stmt|;
name|RelNode
name|newRel
init|=
name|relBuilder
operator|.
name|project
argument_list|(
name|identityFields
argument_list|,
name|ImmutableList
operator|.
expr|<
name|String
operator|>
name|of
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|fil
operator|.
name|copy
argument_list|(
name|fil
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|newRel
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This rule adds a Project operator on top of the root operator if it is a join.    * This is important to meet the requirements set by the rewriting rule with    * respect to the plan returned by the input program.    */
specifier|private
specifier|static
class|class
name|HiveRootJoinProjectInsert
extends|extends
name|RelOptRule
block|{
specifier|private
specifier|static
specifier|final
name|HiveRootJoinProjectInsert
name|INSTANCE
init|=
operator|new
name|HiveRootJoinProjectInsert
argument_list|()
decl_stmt|;
specifier|private
name|HiveRootJoinProjectInsert
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|Join
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|,
literal|"HiveRootJoinProjectInsert"
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
specifier|final
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
specifier|final
name|HepRelVertex
name|root
init|=
operator|(
name|HepRelVertex
operator|)
name|call
operator|.
name|getPlanner
argument_list|()
operator|.
name|getRoot
argument_list|()
decl_stmt|;
if|if
condition|(
name|root
operator|.
name|getCurrentRel
argument_list|()
operator|!=
name|join
condition|)
block|{
comment|// Bail out
return|return;
block|}
comment|// The join is the root, but we should always end up with a Project operator
comment|// on top. We will add it.
name|RelBuilder
name|relBuilder
init|=
name|call
operator|.
name|builder
argument_list|()
decl_stmt|;
name|relBuilder
operator|.
name|push
argument_list|(
name|join
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RexNode
argument_list|>
name|identityFields
init|=
name|relBuilder
operator|.
name|fields
argument_list|(
name|ImmutableBitSet
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|join
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldCount
argument_list|()
argument_list|)
operator|.
name|asList
argument_list|()
argument_list|)
decl_stmt|;
name|relBuilder
operator|.
name|project
argument_list|(
name|identityFields
argument_list|,
name|ImmutableList
operator|.
expr|<
name|String
operator|>
name|of
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|relBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

