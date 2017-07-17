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
name|RelOptPredicateList
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
name|SetOp
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
name|Union
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
name|calcite
operator|.
name|rel
operator|.
name|rules
operator|.
name|FilterSetOpTransposeRule
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
name|type
operator|.
name|RelDataTypeField
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
name|RexBuilder
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
name|RexExecutor
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
name|rex
operator|.
name|RexSimplify
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
name|RexUtil
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
name|HiveRelFactories
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

begin_class
specifier|public
class|class
name|HiveFilterSetOpTransposeRule
extends|extends
name|FilterSetOpTransposeRule
block|{
specifier|public
specifier|static
specifier|final
name|HiveFilterSetOpTransposeRule
name|INSTANCE
init|=
operator|new
name|HiveFilterSetOpTransposeRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
comment|/**    * Creates a HiveFilterSetOpTransposeRule.     * This rule rewrites     *       Fil     *        |     *      Union     *       / \    *     Op1 Op2    *     * to     *       Union     *         /\     *         FIL     *         | |     *       Op1 Op2    *     *     * It additionally can remove branch(es) of filter if its able to determine    * that they are going to generate empty result set.    */
specifier|private
name|HiveFilterSetOpTransposeRule
parameter_list|(
name|RelBuilderFactory
name|relBuilderFactory
parameter_list|)
block|{
name|super
argument_list|(
name|relBuilderFactory
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|matches
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
name|Filter
name|filterRel
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RexNode
name|condition
init|=
name|filterRel
operator|.
name|getCondition
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|HiveCalciteUtil
operator|.
name|isDeterministic
argument_list|(
name|condition
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|super
operator|.
name|matches
argument_list|(
name|call
argument_list|)
return|;
block|}
comment|//~ Methods ----------------------------------------------------------------
comment|// implement RelOptRule
comment|// We override the rule in order to do union all branch elimination
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
name|Filter
name|filterRel
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|SetOp
name|setOp
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|RexNode
name|condition
init|=
name|filterRel
operator|.
name|getCondition
argument_list|()
decl_stmt|;
comment|// create filters on top of each setop child, modifying the filter
comment|// condition to reference each setop child
name|RexBuilder
name|rexBuilder
init|=
name|filterRel
operator|.
name|getCluster
argument_list|()
operator|.
name|getRexBuilder
argument_list|()
decl_stmt|;
specifier|final
name|RelBuilder
name|relBuilder
init|=
name|call
operator|.
name|builder
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RelDataTypeField
argument_list|>
name|origFields
init|=
name|setOp
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
decl_stmt|;
name|int
index|[]
name|adjustments
init|=
operator|new
name|int
index|[
name|origFields
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
specifier|final
name|List
argument_list|<
name|RelNode
argument_list|>
name|newSetOpInputs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|RelNode
name|lastInput
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|setOp
operator|.
name|getInputs
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|RelNode
name|input
init|=
name|setOp
operator|.
name|getInput
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|RexNode
name|newCondition
init|=
name|condition
operator|.
name|accept
argument_list|(
operator|new
name|RelOptUtil
operator|.
name|RexInputConverter
argument_list|(
name|rexBuilder
argument_list|,
name|origFields
argument_list|,
name|input
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
argument_list|,
name|adjustments
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|setOp
operator|instanceof
name|Union
operator|&&
name|setOp
operator|.
name|all
condition|)
block|{
specifier|final
name|RelMetadataQuery
name|mq
init|=
name|call
operator|.
name|getMetadataQuery
argument_list|()
decl_stmt|;
specifier|final
name|RelOptPredicateList
name|predicates
init|=
name|mq
operator|.
name|getPulledUpPredicates
argument_list|(
name|input
argument_list|)
decl_stmt|;
if|if
condition|(
name|predicates
operator|!=
literal|null
condition|)
block|{
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RexNode
argument_list|>
name|listBuilder
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
name|listBuilder
operator|.
name|addAll
argument_list|(
name|predicates
operator|.
name|pulledUpPredicates
argument_list|)
expr_stmt|;
name|listBuilder
operator|.
name|add
argument_list|(
name|newCondition
argument_list|)
expr_stmt|;
name|RexExecutor
name|executor
init|=
name|Util
operator|.
name|first
argument_list|(
name|filterRel
operator|.
name|getCluster
argument_list|()
operator|.
name|getPlanner
argument_list|()
operator|.
name|getExecutor
argument_list|()
argument_list|,
name|RexUtil
operator|.
name|EXECUTOR
argument_list|)
decl_stmt|;
specifier|final
name|RexSimplify
name|simplify
init|=
operator|new
name|RexSimplify
argument_list|(
name|rexBuilder
argument_list|,
literal|true
argument_list|,
name|executor
argument_list|)
decl_stmt|;
specifier|final
name|RexNode
name|x
init|=
name|simplify
operator|.
name|simplifyAnds
argument_list|(
name|listBuilder
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|x
operator|.
name|isAlwaysFalse
argument_list|()
condition|)
block|{
comment|// this is the last branch, and it is always false
comment|// We assume alwaysFalse filter will get pushed down to TS so this
comment|// branch so it won't read any data.
if|if
condition|(
name|index
operator|==
name|setOp
operator|.
name|getInputs
argument_list|()
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
name|lastInput
operator|=
name|relBuilder
operator|.
name|push
argument_list|(
name|input
argument_list|)
operator|.
name|filter
argument_list|(
name|newCondition
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
comment|// remove this branch
continue|continue;
block|}
block|}
block|}
name|newSetOpInputs
operator|.
name|add
argument_list|(
name|relBuilder
operator|.
name|push
argument_list|(
name|input
argument_list|)
operator|.
name|filter
argument_list|(
name|newCondition
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|newSetOpInputs
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
comment|// create a new setop whose children are the filters created above
name|SetOp
name|newSetOp
init|=
name|setOp
operator|.
name|copy
argument_list|(
name|setOp
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|newSetOpInputs
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|newSetOp
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|newSetOpInputs
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|call
operator|.
name|transformTo
argument_list|(
name|newSetOpInputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// we have to keep at least a branch before we support empty values() in
comment|// hive
name|call
operator|.
name|transformTo
argument_list|(
name|lastInput
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

