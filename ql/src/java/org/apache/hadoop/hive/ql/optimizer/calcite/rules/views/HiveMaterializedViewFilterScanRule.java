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
operator|.
name|views
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|RelOptMaterialization
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
name|Project
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

begin_comment
comment|/**  * Planner rule that replaces (if possible)  * a {@link org.apache.calcite.rel.core.Project}  * on a {@link org.apache.calcite.rel.core.Filter}  * on a {@link org.apache.calcite.rel.core.TableScan}  * to use a Materialized View.  */
end_comment

begin_class
specifier|public
class|class
name|HiveMaterializedViewFilterScanRule
extends|extends
name|RelOptRule
block|{
specifier|public
specifier|static
specifier|final
name|HiveMaterializedViewFilterScanRule
name|INSTANCE
init|=
operator|new
name|HiveMaterializedViewFilterScanRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
comment|//~ Constructors -----------------------------------------------------------
comment|/** Creates a HiveMaterializedViewFilterScanRule. */
specifier|protected
name|HiveMaterializedViewFilterScanRule
parameter_list|(
name|RelBuilderFactory
name|relBuilderFactory
parameter_list|)
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|Project
operator|.
name|class
argument_list|,
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
literal|null
argument_list|,
name|none
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|relBuilderFactory
argument_list|,
literal|"MaterializedViewFilterScanRule"
argument_list|)
expr_stmt|;
block|}
comment|//~ Methods ----------------------------------------------------------------
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
specifier|final
name|Project
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
name|Filter
name|filter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|TableScan
name|scan
init|=
name|call
operator|.
name|rel
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|apply
argument_list|(
name|call
argument_list|,
name|project
argument_list|,
name|filter
argument_list|,
name|scan
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|apply
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|,
name|Project
name|project
parameter_list|,
name|Filter
name|filter
parameter_list|,
name|TableScan
name|scan
parameter_list|)
block|{
name|RelOptPlanner
name|planner
init|=
name|call
operator|.
name|getPlanner
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RelOptMaterialization
argument_list|>
name|materializations
init|=
operator|(
name|planner
operator|instanceof
name|VolcanoPlanner
operator|)
condition|?
operator|(
operator|(
name|VolcanoPlanner
operator|)
name|planner
operator|)
operator|.
name|getMaterializations
argument_list|()
else|:
name|ImmutableList
operator|.
expr|<
name|RelOptMaterialization
operator|>
name|of
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|materializations
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|RelNode
name|root
init|=
name|project
operator|.
name|copy
argument_list|(
name|project
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|filter
operator|.
name|copy
argument_list|(
name|filter
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
operator|(
name|RelNode
operator|)
name|scan
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
comment|// Costing is done in transformTo(), so we call it repeatedly with all applicable
comment|// materialized views and cheapest one will be picked
name|List
argument_list|<
name|RelOptMaterialization
argument_list|>
name|applicableMaterializations
init|=
name|VolcanoPlanner
operator|.
name|getApplicableMaterializations
argument_list|(
name|root
argument_list|,
name|materializations
argument_list|)
decl_stmt|;
for|for
control|(
name|RelOptMaterialization
name|materialization
range|:
name|applicableMaterializations
control|)
block|{
name|List
argument_list|<
name|RelNode
argument_list|>
name|subs
init|=
operator|new
name|MaterializedViewSubstitutionVisitor
argument_list|(
name|materialization
operator|.
name|queryRel
argument_list|,
name|root
argument_list|,
name|relBuilderFactory
argument_list|)
operator|.
name|go
argument_list|(
name|materialization
operator|.
name|tableRel
argument_list|)
decl_stmt|;
for|for
control|(
name|RelNode
name|s
range|:
name|subs
control|)
block|{
name|call
operator|.
name|transformTo
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

