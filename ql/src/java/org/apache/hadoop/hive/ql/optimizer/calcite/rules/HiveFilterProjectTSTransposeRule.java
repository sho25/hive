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
name|RelFactories
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
name|RelFactories
operator|.
name|FilterFactory
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
name|RelFactories
operator|.
name|ProjectFactory
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
name|type
operator|.
name|RelDataTypeFactory
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
name|RexOver
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
name|reloperators
operator|.
name|HiveProject
import|;
end_import

begin_comment
comment|//TODO: Remove this once Calcite FilterProjectTransposeRule can take rule operand
end_comment

begin_class
specifier|public
class|class
name|HiveFilterProjectTSTransposeRule
extends|extends
name|RelOptRule
block|{
specifier|public
specifier|final
specifier|static
name|HiveFilterProjectTSTransposeRule
name|INSTANCE
init|=
operator|new
name|HiveFilterProjectTSTransposeRule
argument_list|(
name|Filter
operator|.
name|class
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_FILTER_FACTORY
argument_list|,
name|HiveProject
operator|.
name|class
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_PROJECT_FACTORY
argument_list|,
name|TableScan
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|HiveFilterProjectTSTransposeRule
name|INSTANCE_DRUID
init|=
operator|new
name|HiveFilterProjectTSTransposeRule
argument_list|(
name|Filter
operator|.
name|class
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_FILTER_FACTORY
argument_list|,
name|HiveProject
operator|.
name|class
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_PROJECT_FACTORY
argument_list|,
name|DruidQuery
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RelFactories
operator|.
name|FilterFactory
name|filterFactory
decl_stmt|;
specifier|private
specifier|final
name|RelFactories
operator|.
name|ProjectFactory
name|projectFactory
decl_stmt|;
specifier|private
name|HiveFilterProjectTSTransposeRule
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Filter
argument_list|>
name|filterClass
parameter_list|,
name|FilterFactory
name|filterFactory
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Project
argument_list|>
name|projectClass
parameter_list|,
name|ProjectFactory
name|projectFactory
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|RelNode
argument_list|>
name|tsClass
parameter_list|)
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|filterClass
argument_list|,
name|operand
argument_list|(
name|projectClass
argument_list|,
name|operand
argument_list|(
name|tsClass
argument_list|,
name|none
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|filterFactory
operator|=
name|filterFactory
expr_stmt|;
name|this
operator|.
name|projectFactory
operator|=
name|projectFactory
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
specifier|final
name|HiveProject
name|projRel
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// Assumption:
comment|// 1. This will be run last after PP, Col Pruning in the PreJoinOrder
comment|// optimizations.
comment|// 2. If ProjectRel is not synthetic then PPD would have already pushed
comment|// relevant pieces down and hence no point in running PPD again.
comment|// 3. For synthetic Projects we don't care about non deterministic UDFs
if|if
condition|(
operator|!
name|projRel
operator|.
name|isSynthetic
argument_list|()
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
comment|// ~ Methods ----------------------------------------------------------------
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
name|Filter
name|filter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|Project
name|project
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|RexOver
operator|.
name|containsOver
argument_list|(
name|project
operator|.
name|getProjects
argument_list|()
argument_list|,
literal|null
argument_list|)
condition|)
block|{
comment|// In general a filter cannot be pushed below a windowing calculation.
comment|// Applying the filter before the aggregation function changes
comment|// the results of the windowing invocation.
comment|//
comment|// When the filter is on the PARTITION BY expression of the OVER clause
comment|// it can be pushed down. For now we don't support this.
return|return;
block|}
if|if
condition|(
name|RexUtil
operator|.
name|containsCorrelation
argument_list|(
name|filter
operator|.
name|getCondition
argument_list|()
argument_list|)
condition|)
block|{
comment|// If there is a correlation condition anywhere in the filter, don't
comment|// push this filter past project since in some cases it can prevent a
comment|// Correlate from being de-correlated.
return|return;
block|}
comment|// convert the filter to one that references the child of the project
name|RexNode
name|newCondition
init|=
name|RelOptUtil
operator|.
name|pushPastProject
argument_list|(
name|filter
operator|.
name|getCondition
argument_list|()
argument_list|,
name|project
argument_list|)
decl_stmt|;
comment|// Remove cast of BOOLEAN NOT NULL to BOOLEAN or vice versa. Filter accepts
comment|// nullable and not-nullable conditions, but a CAST might get in the way of
comment|// other rewrites.
specifier|final
name|RelDataTypeFactory
name|typeFactory
init|=
name|filter
operator|.
name|getCluster
argument_list|()
operator|.
name|getTypeFactory
argument_list|()
decl_stmt|;
if|if
condition|(
name|RexUtil
operator|.
name|isNullabilityCast
argument_list|(
name|typeFactory
argument_list|,
name|newCondition
argument_list|)
condition|)
block|{
name|newCondition
operator|=
operator|(
operator|(
name|RexCall
operator|)
name|newCondition
operator|)
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|RelNode
name|newFilterRel
init|=
name|filterFactory
operator|==
literal|null
condition|?
name|filter
operator|.
name|copy
argument_list|(
name|filter
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|project
operator|.
name|getInput
argument_list|()
argument_list|,
name|newCondition
argument_list|)
else|:
name|filterFactory
operator|.
name|createFilter
argument_list|(
name|project
operator|.
name|getInput
argument_list|()
argument_list|,
name|newCondition
argument_list|)
decl_stmt|;
name|RelNode
name|newProjRel
init|=
name|projectFactory
operator|==
literal|null
condition|?
name|project
operator|.
name|copy
argument_list|(
name|project
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|newFilterRel
argument_list|,
name|project
operator|.
name|getProjects
argument_list|()
argument_list|,
name|project
operator|.
name|getRowType
argument_list|()
argument_list|)
else|:
name|projectFactory
operator|.
name|createProject
argument_list|(
name|newFilterRel
argument_list|,
name|project
operator|.
name|getProjects
argument_list|()
argument_list|,
name|project
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldNames
argument_list|()
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|newProjRel
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

