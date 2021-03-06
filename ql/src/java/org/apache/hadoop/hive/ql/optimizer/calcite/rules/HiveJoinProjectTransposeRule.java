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
name|rules
operator|.
name|JoinProjectTransposeRule
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
name|HiveProject
import|;
end_import

begin_class
specifier|public
class|class
name|HiveJoinProjectTransposeRule
extends|extends
name|JoinProjectTransposeRule
block|{
specifier|public
specifier|static
specifier|final
name|HiveJoinProjectTransposeRule
name|LEFT_PROJECT_BTW_JOIN
init|=
operator|new
name|HiveJoinProjectTransposeRule
argument_list|(
name|operand
argument_list|(
name|HiveJoin
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|HiveProject
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|HiveJoin
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|operand
argument_list|(
name|RelNode
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|,
literal|"JoinProjectTransposeRule(Project-Join-Other)"
argument_list|,
literal|false
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveJoinProjectTransposeRule
name|RIGHT_PROJECT_BTW_JOIN
init|=
operator|new
name|HiveJoinProjectTransposeRule
argument_list|(
name|operand
argument_list|(
name|HiveJoin
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|RelNode
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|,
name|operand
argument_list|(
name|HiveProject
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|HiveJoin
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|,
literal|"JoinProjectTransposeRule(Other-Project-Join)"
argument_list|,
literal|false
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveJoinProjectTransposeRule
name|BOTH_PROJECT
init|=
operator|new
name|HiveJoinProjectTransposeRule
argument_list|(
name|operand
argument_list|(
name|HiveJoin
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|HiveProject
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|,
name|operand
argument_list|(
name|HiveProject
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|,
literal|"JoinProjectTransposeRule(Project-Project)"
argument_list|,
literal|false
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveJoinProjectTransposeRule
name|LEFT_PROJECT
init|=
operator|new
name|HiveJoinProjectTransposeRule
argument_list|(
name|operand
argument_list|(
name|HiveJoin
operator|.
name|class
argument_list|,
name|some
argument_list|(
name|operand
argument_list|(
name|HiveProject
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|,
literal|"JoinProjectTransposeRule(Project-Other)"
argument_list|,
literal|false
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveJoinProjectTransposeRule
name|RIGHT_PROJECT
init|=
operator|new
name|HiveJoinProjectTransposeRule
argument_list|(
name|operand
argument_list|(
name|HiveJoin
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|RelNode
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|,
name|operand
argument_list|(
name|HiveProject
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|,
literal|"JoinProjectTransposeRule(Other-Project)"
argument_list|,
literal|false
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveJoinProjectTransposeRule
name|BOTH_PROJECT_INCLUDE_OUTER
init|=
operator|new
name|HiveJoinProjectTransposeRule
argument_list|(
name|operand
argument_list|(
name|HiveJoin
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|HiveProject
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|,
name|operand
argument_list|(
name|HiveProject
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|,
literal|"Join(IncludingOuter)ProjectTransposeRule(Project-Project)"
argument_list|,
literal|true
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveJoinProjectTransposeRule
name|LEFT_PROJECT_INCLUDE_OUTER
init|=
operator|new
name|HiveJoinProjectTransposeRule
argument_list|(
name|operand
argument_list|(
name|HiveJoin
operator|.
name|class
argument_list|,
name|some
argument_list|(
name|operand
argument_list|(
name|HiveProject
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|,
literal|"Join(IncludingOuter)ProjectTransposeRule(Project-Other)"
argument_list|,
literal|true
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveJoinProjectTransposeRule
name|RIGHT_PROJECT_INCLUDE_OUTER
init|=
operator|new
name|HiveJoinProjectTransposeRule
argument_list|(
name|operand
argument_list|(
name|HiveJoin
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|RelNode
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|,
name|operand
argument_list|(
name|HiveProject
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|,
literal|"Join(IncludingOuter)ProjectTransposeRule(Other-Project)"
argument_list|,
literal|true
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|private
name|HiveJoinProjectTransposeRule
parameter_list|(
name|RelOptRuleOperand
name|operand
parameter_list|,
name|String
name|description
parameter_list|,
name|boolean
name|includeOuter
parameter_list|,
name|RelBuilderFactory
name|relBuilderFactory
parameter_list|)
block|{
name|super
argument_list|(
name|operand
argument_list|,
name|description
argument_list|,
name|includeOuter
argument_list|,
name|relBuilderFactory
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
comment|//TODO: this can be removed once CALCITE-3824 is released
name|HiveProject
name|proj
decl_stmt|;
if|if
condition|(
name|hasLeftChild
argument_list|(
name|call
argument_list|)
condition|)
block|{
name|proj
operator|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|proj
operator|.
name|containsOver
argument_list|()
condition|)
block|{
return|return;
block|}
block|}
if|if
condition|(
name|hasRightChild
argument_list|(
name|call
argument_list|)
condition|)
block|{
name|proj
operator|=
operator|(
name|HiveProject
operator|)
name|getRightChild
argument_list|(
name|call
argument_list|)
expr_stmt|;
if|if
condition|(
name|proj
operator|.
name|containsOver
argument_list|()
condition|)
block|{
return|return;
block|}
block|}
name|super
operator|.
name|onMatch
argument_list|(
name|call
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

