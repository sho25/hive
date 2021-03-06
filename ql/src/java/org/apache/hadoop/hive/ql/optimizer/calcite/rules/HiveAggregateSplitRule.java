begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HashMap
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
name|Map
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
name|core
operator|.
name|Aggregate
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
name|Aggregate
operator|.
name|Group
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
name|sql
operator|.
name|SqlAggFunction
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
name|HiveRelBuilder
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
name|HiveGroupingID
import|;
end_import

begin_comment
comment|/**  * Rule that matches an aggregate with grouping sets and splits it into an aggregate  * without grouping sets (bottom) and an aggregate with grouping sets (top).  */
end_comment

begin_class
specifier|public
class|class
name|HiveAggregateSplitRule
extends|extends
name|RelOptRule
block|{
specifier|public
specifier|static
specifier|final
name|HiveAggregateSplitRule
name|INSTANCE
init|=
operator|new
name|HiveAggregateSplitRule
argument_list|(
name|HiveAggregate
operator|.
name|class
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|private
name|HiveAggregateSplitRule
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Aggregate
argument_list|>
name|aggregateClass
parameter_list|,
name|RelBuilderFactory
name|relBuilderFactory
parameter_list|)
block|{
name|super
argument_list|(
name|operandJ
argument_list|(
name|aggregateClass
argument_list|,
literal|null
argument_list|,
name|agg
lambda|->
name|agg
operator|.
name|getGroupType
argument_list|()
operator|!=
name|Group
operator|.
name|SIMPLE
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|,
name|relBuilderFactory
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
specifier|final
name|Aggregate
name|aggregate
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
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
comment|// If any aggregate is distinct, bail out
comment|// If any aggregate is the grouping id, bail out
comment|// If any aggregate call has a filter, bail out
comment|// If any aggregate functions do not support splitting, bail out
specifier|final
name|ImmutableBitSet
name|bottomAggregateGroupSet
init|=
name|aggregate
operator|.
name|getGroupSet
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|AggregateCall
argument_list|>
name|topAggregateCalls
init|=
operator|new
name|ArrayList
argument_list|<>
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
name|aggregate
operator|.
name|getAggCallList
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|AggregateCall
name|aggregateCall
init|=
name|aggregate
operator|.
name|getAggCallList
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|aggregateCall
operator|.
name|isDistinct
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|aggregateCall
operator|.
name|getAggregation
argument_list|()
operator|.
name|equals
argument_list|(
name|HiveGroupingID
operator|.
name|INSTANCE
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|aggregateCall
operator|.
name|filterArg
operator|>=
literal|0
condition|)
block|{
return|return;
block|}
name|SqlAggFunction
name|aggFunction
init|=
name|HiveRelBuilder
operator|.
name|getRollup
argument_list|(
name|aggregateCall
operator|.
name|getAggregation
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|aggFunction
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|topAggregateCalls
operator|.
name|add
argument_list|(
name|AggregateCall
operator|.
name|create
argument_list|(
name|aggFunction
argument_list|,
name|aggregateCall
operator|.
name|isDistinct
argument_list|()
argument_list|,
name|aggregateCall
operator|.
name|isApproximate
argument_list|()
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|bottomAggregateGroupSet
operator|.
name|cardinality
argument_list|()
operator|+
name|i
argument_list|)
argument_list|,
operator|-
literal|1
argument_list|,
name|aggregateCall
operator|.
name|collation
argument_list|,
name|aggregateCall
operator|.
name|type
argument_list|,
name|aggregateCall
operator|.
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|aggregate
operator|.
name|getCluster
argument_list|()
operator|.
name|getMetadataQuery
argument_list|()
operator|.
name|areColumnsUnique
argument_list|(
name|aggregate
operator|.
name|getInput
argument_list|()
argument_list|,
name|bottomAggregateGroupSet
argument_list|)
condition|)
block|{
comment|// Nothing to do, probably already pushed
return|return;
block|}
specifier|final
name|ImmutableBitSet
name|topAggregateGroupSet
init|=
name|ImmutableBitSet
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|bottomAggregateGroupSet
operator|.
name|cardinality
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|bottomAggregateGroupSet
operator|.
name|forEach
argument_list|(
name|k
lambda|->
name|map
operator|.
name|put
argument_list|(
name|k
argument_list|,
name|map
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ImmutableList
argument_list|<
name|ImmutableBitSet
argument_list|>
name|topAggregateGroupSets
init|=
name|ImmutableBitSet
operator|.
name|ORDERING
operator|.
name|immutableSortedCopy
argument_list|(
name|ImmutableBitSet
operator|.
name|permute
argument_list|(
name|aggregate
operator|.
name|groupSets
argument_list|,
name|map
argument_list|)
argument_list|)
decl_stmt|;
name|relBuilder
operator|.
name|push
argument_list|(
name|aggregate
operator|.
name|getInput
argument_list|()
argument_list|)
operator|.
name|aggregate
argument_list|(
name|relBuilder
operator|.
name|groupKey
argument_list|(
name|bottomAggregateGroupSet
argument_list|)
argument_list|,
name|aggregate
operator|.
name|getAggCallList
argument_list|()
argument_list|)
operator|.
name|aggregate
argument_list|(
name|relBuilder
operator|.
name|groupKey
argument_list|(
name|topAggregateGroupSet
argument_list|,
name|topAggregateGroupSets
argument_list|)
argument_list|,
name|topAggregateCalls
argument_list|)
expr_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|relBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

