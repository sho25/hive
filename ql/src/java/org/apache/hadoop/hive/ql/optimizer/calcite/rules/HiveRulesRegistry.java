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
name|Set
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
name|rel
operator|.
name|RelNode
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
name|ArrayListMultimap
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
name|HashMultimap
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
name|ListMultimap
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
name|SetMultimap
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
name|Sets
import|;
end_import

begin_class
specifier|public
class|class
name|HiveRulesRegistry
block|{
specifier|private
name|SetMultimap
argument_list|<
name|RelOptRule
argument_list|,
name|RelNode
argument_list|>
name|registryVisited
decl_stmt|;
specifier|private
name|ListMultimap
argument_list|<
name|RelNode
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|registryPushedPredicates
decl_stmt|;
specifier|public
name|HiveRulesRegistry
parameter_list|()
block|{
name|this
operator|.
name|registryVisited
operator|=
name|HashMultimap
operator|.
name|create
argument_list|()
expr_stmt|;
name|this
operator|.
name|registryPushedPredicates
operator|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|registerVisited
parameter_list|(
name|RelOptRule
name|rule
parameter_list|,
name|RelNode
name|operator
parameter_list|)
block|{
name|this
operator|.
name|registryVisited
operator|.
name|put
argument_list|(
name|rule
argument_list|,
name|operator
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|RelNode
argument_list|>
name|getVisited
parameter_list|(
name|RelOptRule
name|rule
parameter_list|)
block|{
return|return
name|this
operator|.
name|registryVisited
operator|.
name|get
argument_list|(
name|rule
argument_list|)
return|;
block|}
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getPushedPredicates
parameter_list|(
name|RelNode
name|operator
parameter_list|,
name|int
name|pos
parameter_list|)
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|registryPushedPredicates
operator|.
name|containsKey
argument_list|(
name|operator
argument_list|)
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
name|operator
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
name|this
operator|.
name|registryPushedPredicates
operator|.
name|get
argument_list|(
name|operator
argument_list|)
operator|.
name|add
argument_list|(
name|Sets
operator|.
expr|<
name|String
operator|>
name|newHashSet
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|this
operator|.
name|registryPushedPredicates
operator|.
name|get
argument_list|(
name|operator
argument_list|)
operator|.
name|get
argument_list|(
name|pos
argument_list|)
return|;
block|}
specifier|public
name|void
name|copyPushedPredicates
parameter_list|(
name|RelNode
name|operator
parameter_list|,
name|RelNode
name|otherOperator
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|registryPushedPredicates
operator|.
name|containsKey
argument_list|(
name|operator
argument_list|)
condition|)
block|{
for|for
control|(
name|Set
argument_list|<
name|String
argument_list|>
name|s
range|:
name|this
operator|.
name|registryPushedPredicates
operator|.
name|get
argument_list|(
name|operator
argument_list|)
control|)
block|{
name|this
operator|.
name|registryPushedPredicates
operator|.
name|put
argument_list|(
name|otherOperator
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

