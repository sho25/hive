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
name|java
operator|.
name|util
operator|.
name|Optional
import|;
end_import

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
name|Context
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
name|Contexts
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
name|AggregateCall
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
name|CorrelationId
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
name|RelFactories
operator|.
name|AggregateFactory
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
name|JoinFactory
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
name|RelFactories
operator|.
name|SemiJoinFactory
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
name|SetOpFactory
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
name|SortFactory
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
name|RelDataType
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
name|SqlValidatorUtil
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
name|HiveAggregate
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
name|HiveExcept
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
name|HiveFilter
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
name|HiveSemiJoin
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|optimizer
operator|.
name|signature
operator|.
name|RelTreeSignature
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
name|plan
operator|.
name|mapper
operator|.
name|StatsSource
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
name|stats
operator|.
name|OperatorStats
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
name|HiveRelFactories
block|{
specifier|public
specifier|static
specifier|final
name|ProjectFactory
name|HIVE_PROJECT_FACTORY
init|=
operator|new
name|HiveProjectFactoryImpl
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|FilterFactory
name|HIVE_FILTER_FACTORY
init|=
operator|new
name|HiveFilterFactoryImpl
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|JoinFactory
name|HIVE_JOIN_FACTORY
init|=
operator|new
name|HiveJoinFactoryImpl
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|SemiJoinFactory
name|HIVE_SEMI_JOIN_FACTORY
init|=
operator|new
name|HiveSemiJoinFactoryImpl
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|SortFactory
name|HIVE_SORT_FACTORY
init|=
operator|new
name|HiveSortFactoryImpl
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|AggregateFactory
name|HIVE_AGGREGATE_FACTORY
init|=
operator|new
name|HiveAggregateFactoryImpl
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|SetOpFactory
name|HIVE_SET_OP_FACTORY
init|=
operator|new
name|HiveSetOpFactoryImpl
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|RelBuilderFactory
name|HIVE_BUILDER
init|=
name|HiveRelBuilder
operator|.
name|proto
argument_list|(
name|Contexts
operator|.
name|of
argument_list|(
name|HIVE_PROJECT_FACTORY
argument_list|,
name|HIVE_FILTER_FACTORY
argument_list|,
name|HIVE_JOIN_FACTORY
argument_list|,
name|HIVE_SEMI_JOIN_FACTORY
argument_list|,
name|HIVE_SORT_FACTORY
argument_list|,
name|HIVE_AGGREGATE_FACTORY
argument_list|,
name|HIVE_SET_OP_FACTORY
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
name|HiveRelFactories
parameter_list|()
block|{   }
comment|/**    * Implementation of {@link ProjectFactory} that returns    * {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveProject}    * .    */
specifier|private
specifier|static
class|class
name|HiveProjectFactoryImpl
implements|implements
name|ProjectFactory
block|{
annotation|@
name|Override
specifier|public
name|RelNode
name|createProject
parameter_list|(
name|RelNode
name|child
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|RexNode
argument_list|>
name|childExprs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
parameter_list|)
block|{
name|RelOptCluster
name|cluster
init|=
name|child
operator|.
name|getCluster
argument_list|()
decl_stmt|;
name|RelDataType
name|rowType
init|=
name|RexUtil
operator|.
name|createStructType
argument_list|(
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|,
name|childExprs
argument_list|,
name|fieldNames
argument_list|,
name|SqlValidatorUtil
operator|.
name|EXPR_SUGGESTER
argument_list|)
decl_stmt|;
name|RelTraitSet
name|trait
init|=
name|TraitsUtil
operator|.
name|getDefaultTraitSet
argument_list|(
name|cluster
argument_list|,
name|child
operator|.
name|getTraitSet
argument_list|()
argument_list|)
decl_stmt|;
name|RelNode
name|project
init|=
name|HiveProject
operator|.
name|create
argument_list|(
name|cluster
argument_list|,
name|child
argument_list|,
name|childExprs
argument_list|,
name|rowType
argument_list|,
name|trait
argument_list|,
name|Collections
operator|.
expr|<
name|RelCollation
operator|>
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|project
return|;
block|}
block|}
comment|/**    * Implementation of {@link FilterFactory} that returns    * {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveFilter}    * .    */
specifier|private
specifier|static
class|class
name|HiveFilterFactoryImpl
implements|implements
name|FilterFactory
block|{
annotation|@
name|Override
specifier|public
name|RelNode
name|createFilter
parameter_list|(
name|RelNode
name|child
parameter_list|,
name|RexNode
name|condition
parameter_list|)
block|{
name|RelOptCluster
name|cluster
init|=
name|child
operator|.
name|getCluster
argument_list|()
decl_stmt|;
name|HiveFilter
name|filter
init|=
operator|new
name|HiveFilter
argument_list|(
name|cluster
argument_list|,
name|TraitsUtil
operator|.
name|getDefaultTraitSet
argument_list|(
name|cluster
argument_list|)
argument_list|,
name|child
argument_list|,
name|condition
argument_list|)
decl_stmt|;
return|return
name|filter
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|HiveJoinFactoryImpl
implements|implements
name|JoinFactory
block|{
comment|/**      * Creates a join.      *      * @param left      *          Left input      * @param right      *          Right input      * @param condition      *          Join condition      * @param joinType      *          Join type      * @param variablesStopped      *          Set of names of variables which are set by the LHS and used by      *          the RHS and are not available to nodes above this JoinRel in the      *          tree      * @param semiJoinDone      *          Whether this join has been translated to a semi-join      */
annotation|@
name|Override
specifier|public
name|RelNode
name|createJoin
parameter_list|(
name|RelNode
name|left
parameter_list|,
name|RelNode
name|right
parameter_list|,
name|RexNode
name|condition
parameter_list|,
name|JoinRelType
name|joinType
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|variablesStopped
parameter_list|,
name|boolean
name|semiJoinDone
parameter_list|)
block|{
return|return
name|HiveJoin
operator|.
name|getJoin
argument_list|(
name|left
operator|.
name|getCluster
argument_list|()
argument_list|,
name|left
argument_list|,
name|right
argument_list|,
name|condition
argument_list|,
name|joinType
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RelNode
name|createJoin
parameter_list|(
name|RelNode
name|left
parameter_list|,
name|RelNode
name|right
parameter_list|,
name|RexNode
name|condition
parameter_list|,
name|Set
argument_list|<
name|CorrelationId
argument_list|>
name|variablesSet
parameter_list|,
name|JoinRelType
name|joinType
parameter_list|,
name|boolean
name|semiJoinDone
parameter_list|)
block|{
comment|// According to calcite, it is going to be removed before Calcite-2.0
comment|// TODO: to handle CorrelationId
return|return
name|HiveJoin
operator|.
name|getJoin
argument_list|(
name|left
operator|.
name|getCluster
argument_list|()
argument_list|,
name|left
argument_list|,
name|right
argument_list|,
name|condition
argument_list|,
name|joinType
argument_list|)
return|;
block|}
block|}
comment|/**    * Implementation of {@link SemiJoinFactory} that returns    * {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveSemiJoin}    * .    */
specifier|private
specifier|static
class|class
name|HiveSemiJoinFactoryImpl
implements|implements
name|SemiJoinFactory
block|{
annotation|@
name|Override
specifier|public
name|RelNode
name|createSemiJoin
parameter_list|(
name|RelNode
name|left
parameter_list|,
name|RelNode
name|right
parameter_list|,
name|RexNode
name|condition
parameter_list|)
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
specifier|final
name|RelOptCluster
name|cluster
init|=
name|left
operator|.
name|getCluster
argument_list|()
decl_stmt|;
return|return
name|HiveSemiJoin
operator|.
name|getSemiJoin
argument_list|(
name|cluster
argument_list|,
name|left
operator|.
name|getTraitSet
argument_list|()
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
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|HiveSortFactoryImpl
implements|implements
name|SortFactory
block|{
annotation|@
name|Override
specifier|public
name|RelNode
name|createSort
parameter_list|(
name|RelTraitSet
name|traits
parameter_list|,
name|RelNode
name|input
parameter_list|,
name|RelCollation
name|collation
parameter_list|,
name|RexNode
name|offset
parameter_list|,
name|RexNode
name|fetch
parameter_list|)
block|{
return|return
name|createSort
argument_list|(
name|input
argument_list|,
name|collation
argument_list|,
name|offset
argument_list|,
name|fetch
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RelNode
name|createSort
parameter_list|(
name|RelNode
name|input
parameter_list|,
name|RelCollation
name|collation
parameter_list|,
name|RexNode
name|offset
parameter_list|,
name|RexNode
name|fetch
parameter_list|)
block|{
return|return
name|HiveSortLimit
operator|.
name|create
argument_list|(
name|input
argument_list|,
name|collation
argument_list|,
name|offset
argument_list|,
name|fetch
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|HiveAggregateFactoryImpl
implements|implements
name|AggregateFactory
block|{
annotation|@
name|Override
specifier|public
name|RelNode
name|createAggregate
parameter_list|(
name|RelNode
name|child
parameter_list|,
name|boolean
name|indicator
parameter_list|,
name|ImmutableBitSet
name|groupSet
parameter_list|,
name|ImmutableList
argument_list|<
name|ImmutableBitSet
argument_list|>
name|groupSets
parameter_list|,
name|List
argument_list|<
name|AggregateCall
argument_list|>
name|aggCalls
parameter_list|)
block|{
if|if
condition|(
name|indicator
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Hive does not support indicator columns but Calcite "
operator|+
literal|"created an Aggregate operator containing them"
argument_list|)
throw|;
block|}
return|return
operator|new
name|HiveAggregate
argument_list|(
name|child
operator|.
name|getCluster
argument_list|()
argument_list|,
name|child
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|child
argument_list|,
name|groupSet
argument_list|,
name|groupSets
argument_list|,
name|aggCalls
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|HiveSetOpFactoryImpl
implements|implements
name|SetOpFactory
block|{
annotation|@
name|Override
specifier|public
name|RelNode
name|createSetOp
parameter_list|(
name|SqlKind
name|kind
parameter_list|,
name|List
argument_list|<
name|RelNode
argument_list|>
name|inputs
parameter_list|,
name|boolean
name|all
parameter_list|)
block|{
if|if
condition|(
name|kind
operator|==
name|SqlKind
operator|.
name|UNION
condition|)
block|{
return|return
operator|new
name|HiveUnion
argument_list|(
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getCluster
argument_list|()
argument_list|,
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|inputs
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|kind
operator|==
name|SqlKind
operator|.
name|INTERSECT
condition|)
block|{
return|return
operator|new
name|HiveIntersect
argument_list|(
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getCluster
argument_list|()
argument_list|,
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|inputs
argument_list|,
name|all
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|kind
operator|==
name|SqlKind
operator|.
name|EXCEPT
condition|)
block|{
return|return
operator|new
name|HiveExcept
argument_list|(
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getCluster
argument_list|()
argument_list|,
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|inputs
argument_list|,
name|all
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Expected to get set operator of type Union, Intersect or Except(Minus). Found : "
operator|+
name|kind
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

