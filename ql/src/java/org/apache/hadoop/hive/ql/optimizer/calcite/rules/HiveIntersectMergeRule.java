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
name|HiveIntersect
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

begin_comment
comment|/**  * Planner rule that merges multiple intersect into one  * Before the rule, it is   *                        intersect-branch1  *                            |-----intersect-branch2  *                                      |-----branch3  * After the rule, it becomes  *                        intersect-branch1  *                            |-----branch2  *                            |-----branch3  * {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveIntersect}  */
end_comment

begin_class
specifier|public
class|class
name|HiveIntersectMergeRule
extends|extends
name|RelOptRule
block|{
specifier|public
specifier|static
specifier|final
name|HiveIntersectMergeRule
name|INSTANCE
init|=
operator|new
name|HiveIntersectMergeRule
argument_list|()
decl_stmt|;
comment|// ~ Constructors -----------------------------------------------------------
specifier|private
name|HiveIntersectMergeRule
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|HiveIntersect
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
name|RelNode
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
comment|// ~ Methods ----------------------------------------------------------------
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
specifier|final
name|HiveIntersect
name|topHiveIntersect
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|HiveIntersect
name|bottomHiveIntersect
decl_stmt|;
if|if
condition|(
name|call
operator|.
name|rel
argument_list|(
literal|2
argument_list|)
operator|instanceof
name|HiveIntersect
condition|)
block|{
name|bottomHiveIntersect
operator|=
name|call
operator|.
name|rel
argument_list|(
literal|2
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
literal|1
argument_list|)
operator|instanceof
name|HiveIntersect
condition|)
block|{
name|bottomHiveIntersect
operator|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return;
block|}
name|boolean
name|all
init|=
name|topHiveIntersect
operator|.
name|all
decl_stmt|;
comment|// top is distinct, we can always merge whether bottom is distinct or not
comment|// top is all, we can only merge if bottom is also all
comment|// that is to say, we should bail out if top is all and bottom is distinct
if|if
condition|(
name|all
operator|&&
operator|!
name|bottomHiveIntersect
operator|.
name|all
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|RelNode
argument_list|>
name|inputs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|call
operator|.
name|rel
argument_list|(
literal|2
argument_list|)
operator|instanceof
name|HiveIntersect
condition|)
block|{
name|inputs
operator|.
name|add
argument_list|(
name|topHiveIntersect
operator|.
name|getInput
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|inputs
operator|.
name|addAll
argument_list|(
name|bottomHiveIntersect
operator|.
name|getInputs
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|inputs
operator|.
name|addAll
argument_list|(
name|bottomHiveIntersect
operator|.
name|getInputs
argument_list|()
argument_list|)
expr_stmt|;
name|inputs
operator|.
name|addAll
argument_list|(
name|Util
operator|.
name|skip
argument_list|(
name|topHiveIntersect
operator|.
name|getInputs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HiveIntersect
name|newIntersect
init|=
operator|(
name|HiveIntersect
operator|)
name|topHiveIntersect
operator|.
name|copy
argument_list|(
name|topHiveIntersect
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|inputs
argument_list|,
name|all
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|newIntersect
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

