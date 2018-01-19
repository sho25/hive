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
name|HiveUnion
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
comment|/**  * Planner rule that merges multiple union into one  * Before the rule, it is   *                        union all-branch1  *                            |-----union all-branch2  *                                      |-----branch3  * After the rule, it becomes  *                        union all-branch1  *                            |-----branch2  *                            |-----branch3  * {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveUnion}  */
end_comment

begin_class
specifier|public
class|class
name|HiveUnionMergeRule
extends|extends
name|RelOptRule
block|{
specifier|public
specifier|static
specifier|final
name|HiveUnionMergeRule
name|INSTANCE
init|=
operator|new
name|HiveUnionMergeRule
argument_list|()
decl_stmt|;
comment|// ~ Constructors -----------------------------------------------------------
specifier|private
name|HiveUnionMergeRule
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|HiveUnion
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
name|HiveUnion
name|topUnion
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|HiveUnion
name|bottomUnion
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
name|HiveUnion
condition|)
block|{
name|bottomUnion
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
name|HiveUnion
condition|)
block|{
name|bottomUnion
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
name|HiveUnion
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|topUnion
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
if|if
condition|(
name|i
operator|!=
literal|1
condition|)
block|{
name|inputs
operator|.
name|add
argument_list|(
name|topUnion
operator|.
name|getInput
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|inputs
operator|.
name|addAll
argument_list|(
name|bottomUnion
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
name|bottomUnion
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
name|topUnion
operator|.
name|getInputs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HiveUnion
name|newUnion
init|=
operator|(
name|HiveUnion
operator|)
name|topUnion
operator|.
name|copy
argument_list|(
name|topUnion
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|inputs
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|newUnion
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

