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
name|reloperators
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
name|RelTraitSet
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
name|InvalidRelException
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
name|JoinInfo
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
name|JoinRelType
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
name|SemiJoin
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
name|util
operator|.
name|ImmutableIntList
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
name|CalciteSemanticException
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
name|HiveRelOptUtil
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
name|HiveRulesRegistry
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
name|HiveSemiJoin
extends|extends
name|SemiJoin
implements|implements
name|HiveRelNode
block|{
specifier|private
specifier|final
name|RexNode
name|joinFilter
decl_stmt|;
specifier|public
specifier|static
name|HiveSemiJoin
name|getSemiJoin
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelTraitSet
name|traitSet
parameter_list|,
name|RelNode
name|left
parameter_list|,
name|RelNode
name|right
parameter_list|,
name|RexNode
name|condition
parameter_list|,
name|ImmutableIntList
name|leftKeys
parameter_list|,
name|ImmutableIntList
name|rightKeys
parameter_list|)
block|{
try|try
block|{
name|HiveSemiJoin
name|semiJoin
init|=
operator|new
name|HiveSemiJoin
argument_list|(
name|cluster
argument_list|,
name|traitSet
argument_list|,
name|left
argument_list|,
name|right
argument_list|,
name|condition
argument_list|,
name|leftKeys
argument_list|,
name|rightKeys
argument_list|)
decl_stmt|;
return|return
name|semiJoin
return|;
block|}
catch|catch
parameter_list|(
name|InvalidRelException
decl||
name|CalciteSemanticException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|HiveSemiJoin
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelTraitSet
name|traitSet
parameter_list|,
name|RelNode
name|left
parameter_list|,
name|RelNode
name|right
parameter_list|,
name|RexNode
name|condition
parameter_list|,
name|ImmutableIntList
name|leftKeys
parameter_list|,
name|ImmutableIntList
name|rightKeys
parameter_list|)
throws|throws
name|InvalidRelException
throws|,
name|CalciteSemanticException
block|{
name|super
argument_list|(
name|cluster
argument_list|,
name|traitSet
argument_list|,
name|left
argument_list|,
name|right
argument_list|,
name|condition
argument_list|,
name|leftKeys
argument_list|,
name|rightKeys
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|RelDataTypeField
argument_list|>
name|systemFieldList
init|=
name|ImmutableList
operator|.
name|of
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|RexNode
argument_list|>
argument_list|>
name|joinKeyExprs
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|RexNode
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|filterNulls
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|this
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
name|joinKeyExprs
operator|.
name|add
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|joinFilter
operator|=
name|HiveRelOptUtil
operator|.
name|splitHiveJoinCondition
argument_list|(
name|systemFieldList
argument_list|,
name|this
operator|.
name|getInputs
argument_list|()
argument_list|,
name|this
operator|.
name|getCondition
argument_list|()
argument_list|,
name|joinKeyExprs
argument_list|,
name|filterNulls
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RexNode
name|getJoinFilter
parameter_list|()
block|{
return|return
name|joinFilter
return|;
block|}
annotation|@
name|Override
specifier|public
name|SemiJoin
name|copy
parameter_list|(
name|RelTraitSet
name|traitSet
parameter_list|,
name|RexNode
name|condition
parameter_list|,
name|RelNode
name|left
parameter_list|,
name|RelNode
name|right
parameter_list|,
name|JoinRelType
name|joinType
parameter_list|,
name|boolean
name|semiJoinDone
parameter_list|)
block|{
try|try
block|{
specifier|final
name|JoinInfo
name|joinInfo
init|=
name|JoinInfo
operator|.
name|of
argument_list|(
name|left
argument_list|,
name|right
argument_list|,
name|condition
argument_list|)
decl_stmt|;
name|HiveSemiJoin
name|semijoin
init|=
operator|new
name|HiveSemiJoin
argument_list|(
name|getCluster
argument_list|()
argument_list|,
name|traitSet
argument_list|,
name|left
argument_list|,
name|right
argument_list|,
name|condition
argument_list|,
name|joinInfo
operator|.
name|leftKeys
argument_list|,
name|joinInfo
operator|.
name|rightKeys
argument_list|)
decl_stmt|;
comment|// If available, copy state to registry for optimization rules
name|HiveRulesRegistry
name|registry
init|=
name|semijoin
operator|.
name|getCluster
argument_list|()
operator|.
name|getPlanner
argument_list|()
operator|.
name|getContext
argument_list|()
operator|.
name|unwrap
argument_list|(
name|HiveRulesRegistry
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|registry
operator|!=
literal|null
condition|)
block|{
name|registry
operator|.
name|copyPushedPredicates
argument_list|(
name|this
argument_list|,
name|semijoin
argument_list|)
expr_stmt|;
block|}
return|return
name|semijoin
return|;
block|}
catch|catch
parameter_list|(
name|InvalidRelException
decl||
name|CalciteSemanticException
name|e
parameter_list|)
block|{
comment|// Semantic error not possible. Must be a bug. Convert to
comment|// internal error.
throw|throw
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|implement
parameter_list|(
name|Implementor
name|implementor
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|RelOptCost
name|computeSelfCost
parameter_list|(
name|RelOptPlanner
name|planner
parameter_list|,
name|RelMetadataQuery
name|mq
parameter_list|)
block|{
return|return
name|mq
operator|.
name|getNonCumulativeCost
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
end_class

end_unit

