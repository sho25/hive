begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|plan
operator|.
name|RelOptCluster
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
name|RelOptRuleOperand
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
name|rex
operator|.
name|RexCall
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
name|RexCallBinding
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
name|sql
operator|.
name|SqlKind
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
name|validate
operator|.
name|SqlMonotonicity
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
name|mapping
operator|.
name|Mappings
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
name|HiveProject
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
name|HiveSortLimit
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
name|HiveProjectSortTransposeRule
extends|extends
name|RelOptRule
block|{
specifier|public
specifier|static
specifier|final
name|HiveProjectSortTransposeRule
name|INSTANCE
init|=
operator|new
name|HiveProjectSortTransposeRule
argument_list|()
decl_stmt|;
comment|//~ Constructors -----------------------------------------------------------
comment|/**    * Creates a HiveProjectSortTransposeRule.    */
specifier|private
name|HiveProjectSortTransposeRule
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|HiveProject
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|HiveSortLimit
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|HiveProjectSortTransposeRule
parameter_list|(
name|RelOptRuleOperand
name|operand
parameter_list|)
block|{
name|super
argument_list|(
name|operand
argument_list|)
expr_stmt|;
block|}
comment|//~ Methods ----------------------------------------------------------------
comment|// implement RelOptRule
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
specifier|final
name|HiveProject
name|project
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|HiveSortLimit
name|sort
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|RelOptCluster
name|cluster
init|=
name|project
operator|.
name|getCluster
argument_list|()
decl_stmt|;
comment|// Determine mapping between project input and output fields. If sort
comment|// relies on non-trivial expressions, we can't push.
specifier|final
name|Mappings
operator|.
name|TargetMapping
name|map
init|=
name|RelOptUtil
operator|.
name|permutationIgnoreCast
argument_list|(
name|project
operator|.
name|getProjects
argument_list|()
argument_list|,
name|project
operator|.
name|getInput
argument_list|()
operator|.
name|getRowType
argument_list|()
argument_list|)
operator|.
name|inverse
argument_list|()
decl_stmt|;
for|for
control|(
name|RelFieldCollation
name|fc
range|:
name|sort
operator|.
name|getCollation
argument_list|()
operator|.
name|getFieldCollations
argument_list|()
control|)
block|{
if|if
condition|(
name|map
operator|.
name|getTarget
argument_list|(
name|fc
operator|.
name|getFieldIndex
argument_list|()
argument_list|)
operator|<
literal|0
condition|)
block|{
return|return;
block|}
specifier|final
name|RexNode
name|node
init|=
name|project
operator|.
name|getProjects
argument_list|()
operator|.
name|get
argument_list|(
name|map
operator|.
name|getTarget
argument_list|(
name|fc
operator|.
name|getFieldIndex
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|.
name|isA
argument_list|(
name|SqlKind
operator|.
name|CAST
argument_list|)
condition|)
block|{
comment|// Check whether it is a monotonic preserving cast, otherwise we cannot push
specifier|final
name|RexCall
name|cast
init|=
operator|(
name|RexCall
operator|)
name|node
decl_stmt|;
specifier|final
name|RexCallBinding
name|binding
init|=
name|RexCallBinding
operator|.
name|create
argument_list|(
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|,
name|cast
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|RexUtil
operator|.
name|apply
argument_list|(
name|map
argument_list|,
name|sort
operator|.
name|getCollation
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|cast
operator|.
name|getOperator
argument_list|()
operator|.
name|getMonotonicity
argument_list|(
name|binding
argument_list|)
operator|==
name|SqlMonotonicity
operator|.
name|NOT_MONOTONIC
condition|)
block|{
return|return;
block|}
block|}
block|}
comment|// Create new collation
specifier|final
name|RelCollation
name|newCollation
init|=
name|RelCollationTraitDef
operator|.
name|INSTANCE
operator|.
name|canonize
argument_list|(
name|RexUtil
operator|.
name|apply
argument_list|(
name|map
argument_list|,
name|sort
operator|.
name|getCollation
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|// New operators
specifier|final
name|RelNode
name|newProject
init|=
name|project
operator|.
name|copy
argument_list|(
name|sort
operator|.
name|getInput
argument_list|()
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|ImmutableList
operator|.
expr|<
name|RelNode
operator|>
name|of
argument_list|(
name|sort
operator|.
name|getInput
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|HiveSortLimit
name|newSort
init|=
name|sort
operator|.
name|copy
argument_list|(
name|newProject
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|newProject
argument_list|,
name|newCollation
argument_list|,
name|sort
operator|.
name|offset
argument_list|,
name|sort
operator|.
name|fetch
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|newSort
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

