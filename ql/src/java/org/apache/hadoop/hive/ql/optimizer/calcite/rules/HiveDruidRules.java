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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|Lists
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
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidAggregateFilterTransposeRule
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
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidAggregateProjectRule
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
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidAggregateRule
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
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidFilterAggregateTransposeRule
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
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidFilterProjectTransposeRule
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
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidFilterRule
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
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidHavingFilterRule
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
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidPostAggregationProjectRule
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
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidProjectFilterTransposeRule
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
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidProjectRule
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
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidProjectSortTransposeRule
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
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidSortProjectTransposeRule
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
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidSortRule
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
name|fun
operator|.
name|SqlSumEmptyIsZeroAggFunction
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
name|calcite
operator|.
name|util
operator|.
name|Pair
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_comment
comment|/**  * Druid rules with Hive builder factory.  */
end_comment

begin_class
specifier|public
class|class
name|HiveDruidRules
block|{
specifier|public
specifier|static
specifier|final
name|DruidFilterRule
name|FILTER
init|=
operator|new
name|DruidFilterRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidProjectRule
name|PROJECT
init|=
operator|new
name|DruidProjectRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidAggregateRule
name|AGGREGATE
init|=
operator|new
name|DruidAggregateRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidAggregateProjectRule
name|AGGREGATE_PROJECT
init|=
operator|new
name|DruidAggregateProjectRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidSortRule
name|SORT
init|=
operator|new
name|DruidSortRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidSortProjectTransposeRule
name|SORT_PROJECT_TRANSPOSE
init|=
operator|new
name|DruidSortProjectTransposeRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidProjectSortTransposeRule
name|PROJECT_SORT_TRANSPOSE
init|=
operator|new
name|DruidProjectSortTransposeRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidProjectFilterTransposeRule
name|PROJECT_FILTER_TRANSPOSE
init|=
operator|new
name|DruidProjectFilterTransposeRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidFilterProjectTransposeRule
name|FILTER_PROJECT_TRANSPOSE
init|=
operator|new
name|DruidFilterProjectTransposeRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidAggregateFilterTransposeRule
name|AGGREGATE_FILTER_TRANSPOSE
init|=
operator|new
name|DruidAggregateFilterTransposeRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidFilterAggregateTransposeRule
name|FILTER_AGGREGATE_TRANSPOSE
init|=
operator|new
name|DruidFilterAggregateTransposeRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidPostAggregationProjectRule
name|POST_AGGREGATION_PROJECT
init|=
operator|new
name|DruidPostAggregationProjectRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidHavingFilterRule
name|HAVING_FILTER_RULE
init|=
operator|new
name|DruidHavingFilterRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|AggregateExpandDistinctAggregatesDruidRule
name|EXPAND_SINGLE_DISTINCT_AGGREGATES_DRUID_RULE
init|=
operator|new
name|AggregateExpandDistinctAggregatesDruidRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
comment|/**    * This is a simplified version of {@link org.apache.calcite.rel.rules.AggregateExpandDistinctAggregatesRule}    * The goal of this simplified version is to help pushing single count distinct as multi-phase aggregates.    * This is an okay solution before we actually support grouping sets push-down to Druid.    * We are limiting it to one Distinct count to avoid expensive cross join and running into issue    * https://issues.apache.org/jira/browse/HIVE-19601    */
specifier|public
specifier|static
class|class
name|AggregateExpandDistinctAggregatesDruidRule
extends|extends
name|RelOptRule
block|{
specifier|public
name|AggregateExpandDistinctAggregatesDruidRule
parameter_list|(
name|RelBuilderFactory
name|relBuilderFactory
parameter_list|)
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|Aggregate
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|DruidQuery
operator|.
name|class
argument_list|,
name|none
argument_list|()
argument_list|)
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
if|if
condition|(
operator|!
name|aggregate
operator|.
name|containsDistinctCall
argument_list|()
condition|)
block|{
return|return;
block|}
specifier|final
name|long
name|numCountDistinct
init|=
name|aggregate
operator|.
name|getAggCallList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|aggregateCall
lambda|->
name|aggregateCall
operator|.
name|getAggregation
argument_list|()
operator|.
name|getKind
argument_list|()
operator|.
name|equals
argument_list|(
name|SqlKind
operator|.
name|COUNT
argument_list|)
operator|&&
name|aggregateCall
operator|.
name|isDistinct
argument_list|()
argument_list|)
operator|.
name|count
argument_list|()
decl_stmt|;
if|if
condition|(
name|numCountDistinct
operator|!=
literal|1
condition|)
block|{
return|return;
block|}
comment|// Find all of the agg expressions. We use a LinkedHashSet to ensure determinism.
name|int
name|nonDistinctAggCallCount
init|=
literal|0
decl_stmt|;
comment|// find all aggregate calls without distinct
name|int
name|filterCount
init|=
literal|0
decl_stmt|;
name|int
name|unsupportedNonDistinctAggCallCount
init|=
literal|0
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|Pair
argument_list|<
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|argLists
init|=
operator|new
name|LinkedHashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|AggregateCall
name|aggCall
range|:
name|aggregate
operator|.
name|getAggCallList
argument_list|()
control|)
block|{
if|if
condition|(
name|aggCall
operator|.
name|filterArg
operator|>=
literal|0
condition|)
block|{
operator|++
name|filterCount
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|aggCall
operator|.
name|isDistinct
argument_list|()
condition|)
block|{
operator|++
name|nonDistinctAggCallCount
expr_stmt|;
specifier|final
name|SqlKind
name|aggCallKind
init|=
name|aggCall
operator|.
name|getAggregation
argument_list|()
operator|.
name|getKind
argument_list|()
decl_stmt|;
comment|// We only support COUNT/SUM/MIN/MAX for the "single" count distinct optimization
switch|switch
condition|(
name|aggCallKind
condition|)
block|{
case|case
name|COUNT
case|:
case|case
name|SUM
case|:
case|case
name|SUM0
case|:
case|case
name|MIN
case|:
case|case
name|MAX
case|:
break|break;
default|default:
operator|++
name|unsupportedNonDistinctAggCallCount
expr_stmt|;
block|}
block|}
else|else
block|{
name|argLists
operator|.
name|add
argument_list|(
name|Pair
operator|.
name|of
argument_list|(
name|aggCall
operator|.
name|getArgList
argument_list|()
argument_list|,
name|aggCall
operator|.
name|filterArg
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// If only one distinct aggregate and one or more non-distinct aggregates,
comment|// we can generate multi-phase aggregates
if|if
condition|(
name|numCountDistinct
operator|==
literal|1
comment|// one distinct aggregate
operator|&&
name|filterCount
operator|==
literal|0
comment|// no filter
operator|&&
name|unsupportedNonDistinctAggCallCount
operator|==
literal|0
comment|// sum/min/max/count in non-distinct aggregate
operator|&&
name|nonDistinctAggCallCount
operator|>
literal|0
condition|)
block|{
comment|// one or more non-distinct aggregates
specifier|final
name|RelBuilder
name|relBuilder
init|=
name|call
operator|.
name|builder
argument_list|()
decl_stmt|;
name|convertSingletonDistinct
argument_list|(
name|relBuilder
argument_list|,
name|aggregate
argument_list|,
name|argLists
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
return|return;
block|}
block|}
comment|/**      * Converts an aggregate with one distinct aggregate and one or more      * non-distinct aggregates to multi-phase aggregates (see reference example      * below).      *      * @param relBuilder Contains the input relational expression      * @param aggregate  Original aggregate      * @param argLists   Arguments and filters to the distinct aggregate function      *      */
specifier|private
name|RelBuilder
name|convertSingletonDistinct
parameter_list|(
name|RelBuilder
name|relBuilder
parameter_list|,
name|Aggregate
name|aggregate
parameter_list|,
name|Set
argument_list|<
name|Pair
argument_list|<
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|argLists
parameter_list|)
block|{
comment|// In this case, we are assuming that there is a single distinct function.
comment|// So make sure that argLists is of size one.
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|argLists
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
comment|// For example,
comment|//    SELECT deptno, COUNT(*), SUM(bonus), MIN(DISTINCT sal)
comment|//    FROM emp
comment|//    GROUP BY deptno
comment|//
comment|// becomes
comment|//
comment|//    SELECT deptno, SUM(cnt), SUM(bonus), MIN(sal)
comment|//    FROM (
comment|//          SELECT deptno, COUNT(*) as cnt, SUM(bonus), sal
comment|//          FROM EMP
comment|//          GROUP BY deptno, sal)            // Aggregate B
comment|//    GROUP BY deptno                        // Aggregate A
name|relBuilder
operator|.
name|push
argument_list|(
name|aggregate
operator|.
name|getInput
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|AggregateCall
argument_list|>
name|originalAggCalls
init|=
name|aggregate
operator|.
name|getAggCallList
argument_list|()
decl_stmt|;
specifier|final
name|ImmutableBitSet
name|originalGroupSet
init|=
name|aggregate
operator|.
name|getGroupSet
argument_list|()
decl_stmt|;
comment|// Add the distinct aggregate column(s) to the group-by columns,
comment|// if not already a part of the group-by
specifier|final
name|SortedSet
argument_list|<
name|Integer
argument_list|>
name|bottomGroupSet
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
decl_stmt|;
name|bottomGroupSet
operator|.
name|addAll
argument_list|(
name|aggregate
operator|.
name|getGroupSet
argument_list|()
operator|.
name|asList
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|AggregateCall
name|aggCall
range|:
name|originalAggCalls
control|)
block|{
if|if
condition|(
name|aggCall
operator|.
name|isDistinct
argument_list|()
condition|)
block|{
name|bottomGroupSet
operator|.
name|addAll
argument_list|(
name|aggCall
operator|.
name|getArgList
argument_list|()
argument_list|)
expr_stmt|;
break|break;
comment|// since we only have single distinct call
block|}
block|}
comment|// Generate the intermediate aggregate B, the one on the bottom that converts
comment|// a distinct call to group by call.
comment|// Bottom aggregate is the same as the original aggregate, except that
comment|// the bottom aggregate has converted the DISTINCT aggregate to a group by clause.
specifier|final
name|List
argument_list|<
name|AggregateCall
argument_list|>
name|bottomAggregateCalls
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|AggregateCall
name|aggCall
range|:
name|originalAggCalls
control|)
block|{
comment|// Project the column corresponding to the distinct aggregate. Project
comment|// as-is all the non-distinct aggregates
if|if
condition|(
operator|!
name|aggCall
operator|.
name|isDistinct
argument_list|()
condition|)
block|{
specifier|final
name|AggregateCall
name|newCall
init|=
name|AggregateCall
operator|.
name|create
argument_list|(
name|aggCall
operator|.
name|getAggregation
argument_list|()
argument_list|,
literal|false
argument_list|,
name|aggCall
operator|.
name|isApproximate
argument_list|()
argument_list|,
name|aggCall
operator|.
name|getArgList
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|,
name|ImmutableBitSet
operator|.
name|of
argument_list|(
name|bottomGroupSet
argument_list|)
operator|.
name|cardinality
argument_list|()
argument_list|,
name|relBuilder
operator|.
name|peek
argument_list|()
argument_list|,
literal|null
argument_list|,
name|aggCall
operator|.
name|name
argument_list|)
decl_stmt|;
name|bottomAggregateCalls
operator|.
name|add
argument_list|(
name|newCall
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Generate the aggregate B (see the reference example above)
name|relBuilder
operator|.
name|push
argument_list|(
name|aggregate
operator|.
name|copy
argument_list|(
name|aggregate
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|relBuilder
operator|.
name|build
argument_list|()
argument_list|,
literal|false
argument_list|,
name|ImmutableBitSet
operator|.
name|of
argument_list|(
name|bottomGroupSet
argument_list|)
argument_list|,
literal|null
argument_list|,
name|bottomAggregateCalls
argument_list|)
argument_list|)
expr_stmt|;
comment|// Add aggregate A (see the reference example above), the top aggregate
comment|// to handle the rest of the aggregation that the bottom aggregate hasn't handled
specifier|final
name|List
argument_list|<
name|AggregateCall
argument_list|>
name|topAggregateCalls
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
comment|// Use the remapped arguments for the (non)distinct aggregate calls
name|int
name|nonDistinctAggCallProcessedSoFar
init|=
literal|0
decl_stmt|;
for|for
control|(
name|AggregateCall
name|aggCall
range|:
name|originalAggCalls
control|)
block|{
specifier|final
name|AggregateCall
name|newCall
decl_stmt|;
if|if
condition|(
name|aggCall
operator|.
name|isDistinct
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|newArgList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|arg
range|:
name|aggCall
operator|.
name|getArgList
argument_list|()
control|)
block|{
name|newArgList
operator|.
name|add
argument_list|(
name|bottomGroupSet
operator|.
name|headSet
argument_list|(
name|arg
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|newCall
operator|=
name|AggregateCall
operator|.
name|create
argument_list|(
name|aggCall
operator|.
name|getAggregation
argument_list|()
argument_list|,
literal|false
argument_list|,
name|aggCall
operator|.
name|isApproximate
argument_list|()
argument_list|,
name|newArgList
argument_list|,
operator|-
literal|1
argument_list|,
name|originalGroupSet
operator|.
name|cardinality
argument_list|()
argument_list|,
name|relBuilder
operator|.
name|peek
argument_list|()
argument_list|,
name|aggCall
operator|.
name|getType
argument_list|()
argument_list|,
name|aggCall
operator|.
name|name
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// If aggregate B had a COUNT aggregate call the corresponding aggregate at
comment|// aggregate A must be SUM. For other aggregates, it remains the same.
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|newArgs
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|bottomGroupSet
operator|.
name|size
argument_list|()
operator|+
name|nonDistinctAggCallProcessedSoFar
argument_list|)
decl_stmt|;
if|if
condition|(
name|aggCall
operator|.
name|getAggregation
argument_list|()
operator|.
name|getKind
argument_list|()
operator|==
name|SqlKind
operator|.
name|COUNT
condition|)
block|{
name|newCall
operator|=
name|AggregateCall
operator|.
name|create
argument_list|(
operator|new
name|SqlSumEmptyIsZeroAggFunction
argument_list|()
argument_list|,
literal|false
argument_list|,
name|aggCall
operator|.
name|isApproximate
argument_list|()
argument_list|,
name|newArgs
argument_list|,
operator|-
literal|1
argument_list|,
name|originalGroupSet
operator|.
name|cardinality
argument_list|()
argument_list|,
name|relBuilder
operator|.
name|peek
argument_list|()
argument_list|,
name|aggCall
operator|.
name|getType
argument_list|()
argument_list|,
name|aggCall
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newCall
operator|=
name|AggregateCall
operator|.
name|create
argument_list|(
name|aggCall
operator|.
name|getAggregation
argument_list|()
argument_list|,
literal|false
argument_list|,
name|aggCall
operator|.
name|isApproximate
argument_list|()
argument_list|,
name|newArgs
argument_list|,
operator|-
literal|1
argument_list|,
name|originalGroupSet
operator|.
name|cardinality
argument_list|()
argument_list|,
name|relBuilder
operator|.
name|peek
argument_list|()
argument_list|,
name|aggCall
operator|.
name|getType
argument_list|()
argument_list|,
name|aggCall
operator|.
name|name
argument_list|)
expr_stmt|;
block|}
name|nonDistinctAggCallProcessedSoFar
operator|++
expr_stmt|;
block|}
name|topAggregateCalls
operator|.
name|add
argument_list|(
name|newCall
argument_list|)
expr_stmt|;
block|}
comment|// Populate the group-by keys with the remapped arguments for aggregate A
comment|// The top groupset is basically an identity (first X fields of aggregate B's
comment|// output), minus the distinct aggCall's input.
specifier|final
name|Set
argument_list|<
name|Integer
argument_list|>
name|topGroupSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|groupSetToAdd
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|bottomGroup
range|:
name|bottomGroupSet
control|)
block|{
if|if
condition|(
name|originalGroupSet
operator|.
name|get
argument_list|(
name|bottomGroup
argument_list|)
condition|)
block|{
name|topGroupSet
operator|.
name|add
argument_list|(
name|groupSetToAdd
argument_list|)
expr_stmt|;
block|}
name|groupSetToAdd
operator|++
expr_stmt|;
block|}
name|relBuilder
operator|.
name|push
argument_list|(
name|aggregate
operator|.
name|copy
argument_list|(
name|aggregate
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|relBuilder
operator|.
name|build
argument_list|()
argument_list|,
name|aggregate
operator|.
name|indicator
argument_list|,
name|ImmutableBitSet
operator|.
name|of
argument_list|(
name|topGroupSet
argument_list|)
argument_list|,
literal|null
argument_list|,
name|topAggregateCalls
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|relBuilder
return|;
block|}
block|}
block|}
end_class

end_unit

