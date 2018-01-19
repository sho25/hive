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
name|plan
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
name|Arrays
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
name|Objects
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
name|conf
operator|.
name|HiveConf
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
name|exec
operator|.
name|vector
operator|.
name|VectorAggregationDesc
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
name|exec
operator|.
name|vector
operator|.
name|expressions
operator|.
name|aggregates
operator|.
name|VectorAggregateExpression
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
name|udf
operator|.
name|UDFType
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDAFEvaluator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|AnnotationUtils
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
name|metadata
operator|.
name|HiveException
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
name|physical
operator|.
name|Vectorizer
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
name|Explain
operator|.
name|Level
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
name|Explain
operator|.
name|Vectorization
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * GroupByDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Group By Operator"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|GroupByDesc
extends|extends
name|AbstractOperatorDesc
block|{
comment|/**    * Group-by Mode: COMPLETE: complete 1-phase aggregation: iterate, terminate    * PARTIAL1: partial aggregation - first phase: iterate, terminatePartial    * PARTIAL2: partial aggregation - second phase: merge, terminatePartial    * PARTIALS: For non-distinct the same as PARTIAL2, for distinct the same as    * PARTIAL1    * FINAL: partial aggregation - final phase: merge, terminate    * HASH: For non-distinct the same as PARTIAL1 but use hash-table-based aggregation    * MERGEPARTIAL: FINAL for non-distinct aggregations, COMPLETE for distinct    * aggregations.    */
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/**    * Mode.    *    */
specifier|public
specifier|static
enum|enum
name|Mode
block|{
name|COMPLETE
block|,
name|PARTIAL1
block|,
name|PARTIAL2
block|,
name|PARTIALS
block|,
name|FINAL
block|,
name|HASH
block|,
name|MERGEPARTIAL
block|}
empty_stmt|;
specifier|private
name|Mode
name|mode
decl_stmt|;
comment|// no hash aggregations for group by
specifier|private
name|boolean
name|bucketGroup
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keys
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|listGroupingSets
decl_stmt|;
specifier|private
name|boolean
name|groupingSetsPresent
decl_stmt|;
specifier|private
name|int
name|groupingSetPosition
init|=
operator|-
literal|1
decl_stmt|;
comment|//  /* in case of grouping sets; groupby1 will output values for every setgroup; this is the index of the column that information will be sent */
specifier|private
name|ArrayList
argument_list|<
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
name|AggregationDesc
argument_list|>
name|aggregators
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|outputColumnNames
decl_stmt|;
specifier|private
name|float
name|groupByMemoryUsage
decl_stmt|;
specifier|private
name|float
name|memoryThreshold
decl_stmt|;
specifier|transient
specifier|private
name|boolean
name|isDistinct
decl_stmt|;
specifier|private
name|boolean
name|dontResetAggrsDistinct
decl_stmt|;
specifier|public
name|GroupByDesc
parameter_list|()
block|{   }
specifier|public
name|GroupByDesc
parameter_list|(
specifier|final
name|Mode
name|mode
parameter_list|,
specifier|final
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|outputColumnNames
parameter_list|,
specifier|final
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keys
parameter_list|,
specifier|final
name|ArrayList
argument_list|<
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
name|AggregationDesc
argument_list|>
name|aggregators
parameter_list|,
specifier|final
name|float
name|groupByMemoryUsage
parameter_list|,
specifier|final
name|float
name|memoryThreshold
parameter_list|,
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|listGroupingSets
parameter_list|,
specifier|final
name|boolean
name|groupingSetsPresent
parameter_list|,
specifier|final
name|int
name|groupingSetsPosition
parameter_list|,
specifier|final
name|boolean
name|isDistinct
parameter_list|)
block|{
name|this
argument_list|(
name|mode
argument_list|,
name|outputColumnNames
argument_list|,
name|keys
argument_list|,
name|aggregators
argument_list|,
literal|false
argument_list|,
name|groupByMemoryUsage
argument_list|,
name|memoryThreshold
argument_list|,
name|listGroupingSets
argument_list|,
name|groupingSetsPresent
argument_list|,
name|groupingSetsPosition
argument_list|,
name|isDistinct
argument_list|)
expr_stmt|;
block|}
specifier|public
name|GroupByDesc
parameter_list|(
specifier|final
name|Mode
name|mode
parameter_list|,
specifier|final
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|outputColumnNames
parameter_list|,
specifier|final
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keys
parameter_list|,
specifier|final
name|ArrayList
argument_list|<
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
name|AggregationDesc
argument_list|>
name|aggregators
parameter_list|,
specifier|final
name|boolean
name|bucketGroup
parameter_list|,
specifier|final
name|float
name|groupByMemoryUsage
parameter_list|,
specifier|final
name|float
name|memoryThreshold
parameter_list|,
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|listGroupingSets
parameter_list|,
specifier|final
name|boolean
name|groupingSetsPresent
parameter_list|,
specifier|final
name|int
name|groupingSetsPosition
parameter_list|,
specifier|final
name|boolean
name|isDistinct
parameter_list|)
block|{
name|this
operator|.
name|mode
operator|=
name|mode
expr_stmt|;
name|this
operator|.
name|outputColumnNames
operator|=
name|outputColumnNames
expr_stmt|;
name|this
operator|.
name|keys
operator|=
name|keys
expr_stmt|;
name|this
operator|.
name|aggregators
operator|=
name|aggregators
expr_stmt|;
name|this
operator|.
name|bucketGroup
operator|=
name|bucketGroup
expr_stmt|;
name|this
operator|.
name|groupByMemoryUsage
operator|=
name|groupByMemoryUsage
expr_stmt|;
name|this
operator|.
name|memoryThreshold
operator|=
name|memoryThreshold
expr_stmt|;
name|this
operator|.
name|listGroupingSets
operator|=
name|listGroupingSets
expr_stmt|;
name|this
operator|.
name|groupingSetsPresent
operator|=
name|groupingSetsPresent
expr_stmt|;
name|this
operator|.
name|groupingSetPosition
operator|=
name|groupingSetsPosition
expr_stmt|;
name|this
operator|.
name|isDistinct
operator|=
name|isDistinct
expr_stmt|;
block|}
specifier|public
name|Mode
name|getMode
parameter_list|()
block|{
return|return
name|mode
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"mode"
argument_list|)
specifier|public
name|String
name|getModeString
parameter_list|()
block|{
switch|switch
condition|(
name|mode
condition|)
block|{
case|case
name|COMPLETE
case|:
return|return
literal|"complete"
return|;
case|case
name|PARTIAL1
case|:
return|return
literal|"partial1"
return|;
case|case
name|PARTIAL2
case|:
return|return
literal|"partial2"
return|;
case|case
name|PARTIALS
case|:
return|return
literal|"partials"
return|;
case|case
name|HASH
case|:
return|return
literal|"hash"
return|;
case|case
name|FINAL
case|:
return|return
literal|"final"
return|;
case|case
name|MERGEPARTIAL
case|:
return|return
literal|"mergepartial"
return|;
block|}
return|return
literal|"unknown"
return|;
block|}
specifier|public
name|void
name|setMode
parameter_list|(
specifier|final
name|Mode
name|mode
parameter_list|)
block|{
name|this
operator|.
name|mode
operator|=
name|mode
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"keys"
argument_list|)
specifier|public
name|String
name|getKeyString
parameter_list|()
block|{
return|return
name|PlanUtils
operator|.
name|getExprListString
argument_list|(
name|keys
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"keys"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|}
argument_list|)
specifier|public
name|String
name|getUserLevelExplainKeyString
parameter_list|()
block|{
return|return
name|PlanUtils
operator|.
name|getExprListString
argument_list|(
name|keys
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|getKeys
parameter_list|()
block|{
return|return
name|keys
return|;
block|}
specifier|public
name|void
name|setKeys
parameter_list|(
specifier|final
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keys
parameter_list|)
block|{
name|this
operator|.
name|keys
operator|=
name|keys
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"outputColumnNames"
argument_list|)
specifier|public
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|getOutputColumnNames
parameter_list|()
block|{
return|return
name|outputColumnNames
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Output"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|}
argument_list|)
specifier|public
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|getUserLevelExplainOutputColumnNames
parameter_list|()
block|{
return|return
name|outputColumnNames
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"pruneGroupingSetId"
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
argument_list|)
specifier|public
name|boolean
name|pruneGroupingSetId
parameter_list|()
block|{
return|return
name|groupingSetPosition
operator|>=
literal|0
operator|&&
name|outputColumnNames
operator|.
name|size
argument_list|()
operator|!=
name|keys
operator|.
name|size
argument_list|()
operator|+
name|aggregators
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|void
name|setOutputColumnNames
parameter_list|(
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|outputColumnNames
parameter_list|)
block|{
name|this
operator|.
name|outputColumnNames
operator|=
name|outputColumnNames
expr_stmt|;
block|}
specifier|public
name|float
name|getGroupByMemoryUsage
parameter_list|()
block|{
return|return
name|groupByMemoryUsage
return|;
block|}
specifier|public
name|void
name|setGroupByMemoryUsage
parameter_list|(
name|float
name|groupByMemoryUsage
parameter_list|)
block|{
name|this
operator|.
name|groupByMemoryUsage
operator|=
name|groupByMemoryUsage
expr_stmt|;
block|}
specifier|public
name|float
name|getMemoryThreshold
parameter_list|()
block|{
return|return
name|memoryThreshold
return|;
block|}
specifier|public
name|void
name|setMemoryThreshold
parameter_list|(
name|float
name|memoryThreshold
parameter_list|)
block|{
name|this
operator|.
name|memoryThreshold
operator|=
name|memoryThreshold
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"aggregations"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAggregatorStrings
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|AggregationDesc
name|agg
range|:
name|aggregators
control|)
block|{
name|res
operator|.
name|add
argument_list|(
name|agg
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
specifier|public
name|ArrayList
argument_list|<
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
name|AggregationDesc
argument_list|>
name|getAggregators
parameter_list|()
block|{
return|return
name|aggregators
return|;
block|}
specifier|public
name|void
name|setAggregators
parameter_list|(
specifier|final
name|ArrayList
argument_list|<
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
name|AggregationDesc
argument_list|>
name|aggregators
parameter_list|)
block|{
name|this
operator|.
name|aggregators
operator|=
name|aggregators
expr_stmt|;
block|}
specifier|public
name|boolean
name|isAggregate
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|aggregators
operator|!=
literal|null
operator|&&
operator|!
name|this
operator|.
name|aggregators
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"bucketGroup"
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
argument_list|)
specifier|public
name|boolean
name|getBucketGroup
parameter_list|()
block|{
return|return
name|bucketGroup
return|;
block|}
specifier|public
name|void
name|setBucketGroup
parameter_list|(
name|boolean
name|bucketGroup
parameter_list|)
block|{
name|this
operator|.
name|bucketGroup
operator|=
name|bucketGroup
expr_stmt|;
block|}
comment|/**    * Checks if this grouping is like distinct, which means that all non-distinct grouping    * columns behave like they were distinct - for example min and max operators.    */
specifier|public
name|boolean
name|isDistinctLike
parameter_list|()
block|{
name|ArrayList
argument_list|<
name|AggregationDesc
argument_list|>
name|aggregators
init|=
name|getAggregators
argument_list|()
decl_stmt|;
for|for
control|(
name|AggregationDesc
name|ad
range|:
name|aggregators
control|)
block|{
if|if
condition|(
operator|!
name|ad
operator|.
name|getDistinct
argument_list|()
condition|)
block|{
name|GenericUDAFEvaluator
name|udafEval
init|=
name|ad
operator|.
name|getGenericUDAFEvaluator
argument_list|()
decl_stmt|;
name|UDFType
name|annot
init|=
name|AnnotationUtils
operator|.
name|getAnnotation
argument_list|(
name|udafEval
operator|.
name|getClass
argument_list|()
argument_list|,
name|UDFType
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|annot
operator|==
literal|null
operator|||
operator|!
name|annot
operator|.
name|distinctLike
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
comment|// Consider a query like:
comment|// select a, b, count(distinct c) from T group by a,b with rollup;
comment|// Assume that hive.map.aggr is set to true and hive.groupby.skewindata is false,
comment|// in which case the group by would execute as a single map-reduce job.
comment|// For the group-by, the group by keys should be: a,b,groupingSet(for rollup), c
comment|// So, the starting position of grouping set need to be known
specifier|public
name|List
argument_list|<
name|Integer
argument_list|>
name|getListGroupingSets
parameter_list|()
block|{
return|return
name|listGroupingSets
return|;
block|}
specifier|public
name|void
name|setListGroupingSets
parameter_list|(
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|listGroupingSets
parameter_list|)
block|{
name|this
operator|.
name|listGroupingSets
operator|=
name|listGroupingSets
expr_stmt|;
block|}
specifier|public
name|boolean
name|isGroupingSetsPresent
parameter_list|()
block|{
return|return
name|groupingSetsPresent
return|;
block|}
specifier|public
name|void
name|setGroupingSetsPresent
parameter_list|(
name|boolean
name|groupingSetsPresent
parameter_list|)
block|{
name|this
operator|.
name|groupingSetsPresent
operator|=
name|groupingSetsPresent
expr_stmt|;
block|}
specifier|public
name|int
name|getGroupingSetPosition
parameter_list|()
block|{
return|return
name|groupingSetPosition
return|;
block|}
specifier|public
name|void
name|setGroupingSetPosition
parameter_list|(
name|int
name|groupingSetPosition
parameter_list|)
block|{
name|this
operator|.
name|groupingSetPosition
operator|=
name|groupingSetPosition
expr_stmt|;
block|}
specifier|public
name|boolean
name|isDontResetAggrsDistinct
parameter_list|()
block|{
return|return
name|dontResetAggrsDistinct
return|;
block|}
specifier|public
name|void
name|setDontResetAggrsDistinct
parameter_list|(
name|boolean
name|dontResetAggrsDistinct
parameter_list|)
block|{
name|this
operator|.
name|dontResetAggrsDistinct
operator|=
name|dontResetAggrsDistinct
expr_stmt|;
block|}
specifier|public
name|boolean
name|isDistinct
parameter_list|()
block|{
return|return
name|isDistinct
return|;
block|}
specifier|public
name|void
name|setDistinct
parameter_list|(
name|boolean
name|isDistinct
parameter_list|)
block|{
name|this
operator|.
name|isDistinct
operator|=
name|isDistinct
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|clone
parameter_list|()
block|{
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|outputColumnNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|outputColumnNames
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|outputColumnNames
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|keys
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|keys
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
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
name|AggregationDesc
argument_list|>
name|aggregators
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|aggregators
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|aggregators
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|listGroupingSets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|listGroupingSets
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|listGroupingSets
argument_list|)
expr_stmt|;
return|return
operator|new
name|GroupByDesc
argument_list|(
name|this
operator|.
name|mode
argument_list|,
name|outputColumnNames
argument_list|,
name|keys
argument_list|,
name|aggregators
argument_list|,
name|this
operator|.
name|groupByMemoryUsage
argument_list|,
name|this
operator|.
name|memoryThreshold
argument_list|,
name|listGroupingSets
argument_list|,
name|this
operator|.
name|groupingSetsPresent
argument_list|,
name|this
operator|.
name|groupingSetPosition
argument_list|,
name|this
operator|.
name|isDistinct
argument_list|)
return|;
block|}
specifier|public
class|class
name|GroupByOperatorExplainVectorization
extends|extends
name|OperatorExplainVectorization
block|{
specifier|private
specifier|final
name|GroupByDesc
name|groupByDesc
decl_stmt|;
specifier|private
specifier|final
name|VectorGroupByDesc
name|vectorGroupByDesc
decl_stmt|;
specifier|public
name|GroupByOperatorExplainVectorization
parameter_list|(
name|GroupByDesc
name|groupByDesc
parameter_list|,
name|VectorGroupByDesc
name|vectorGroupByDesc
parameter_list|)
block|{
comment|// Native vectorization not supported.
name|super
argument_list|(
name|vectorGroupByDesc
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|groupByDesc
operator|=
name|groupByDesc
expr_stmt|;
name|this
operator|.
name|vectorGroupByDesc
operator|=
name|vectorGroupByDesc
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|EXPRESSION
argument_list|,
name|displayName
operator|=
literal|"keyExpressions"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getKeysExpression
parameter_list|()
block|{
return|return
name|vectorExpressionsToStringList
argument_list|(
name|vectorGroupByDesc
operator|.
name|getKeyExpressions
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|EXPRESSION
argument_list|,
name|displayName
operator|=
literal|"aggregators"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAggregators
parameter_list|()
block|{
name|VectorAggregationDesc
index|[]
name|vecAggrDescs
init|=
name|vectorGroupByDesc
operator|.
name|getVecAggrDescs
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|vecAggrList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|vecAggrDescs
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|VectorAggregationDesc
name|vecAggrDesc
range|:
name|vecAggrDescs
control|)
block|{
name|vecAggrList
operator|.
name|add
argument_list|(
name|vecAggrDesc
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|vecAggrList
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|OPERATOR
argument_list|,
name|displayName
operator|=
literal|"vectorProcessingMode"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getProcessingMode
parameter_list|()
block|{
return|return
name|vectorGroupByDesc
operator|.
name|getProcessingMode
argument_list|()
operator|.
name|name
argument_list|()
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|OPERATOR
argument_list|,
name|displayName
operator|=
literal|"groupByMode"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getGroupByMode
parameter_list|()
block|{
return|return
name|groupByDesc
operator|.
name|getMode
argument_list|()
operator|.
name|name
argument_list|()
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|OPERATOR
argument_list|,
name|displayName
operator|=
literal|"vectorOutputConditionsNotMet"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getVectorOutputConditionsNotMet
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|isVectorizationComplexTypesEnabled
init|=
name|vectorGroupByDesc
operator|.
name|getIsVectorizationComplexTypesEnabled
argument_list|()
decl_stmt|;
name|boolean
name|isVectorizationGroupByComplexTypesEnabled
init|=
name|vectorGroupByDesc
operator|.
name|getIsVectorizationGroupByComplexTypesEnabled
argument_list|()
decl_stmt|;
if|if
condition|(
name|isVectorizationComplexTypesEnabled
operator|&&
name|isVectorizationGroupByComplexTypesEnabled
condition|)
block|{
return|return
literal|null
return|;
block|}
name|results
operator|.
name|add
argument_list|(
name|getComplexTypeWithGroupByEnabledCondition
argument_list|(
name|isVectorizationComplexTypesEnabled
argument_list|,
name|isVectorizationGroupByComplexTypesEnabled
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|results
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|EXPRESSION
argument_list|,
name|displayName
operator|=
literal|"projectedOutputColumnNums"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getProjectedOutputColumnNums
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|toString
argument_list|(
name|vectorGroupByDesc
operator|.
name|getProjectedOutputColumns
argument_list|()
argument_list|)
return|;
block|}
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|OPERATOR
argument_list|,
name|displayName
operator|=
literal|"Group By Vectorization"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|GroupByOperatorExplainVectorization
name|getGroupByVectorization
parameter_list|()
block|{
name|VectorGroupByDesc
name|vectorGroupByDesc
init|=
operator|(
name|VectorGroupByDesc
operator|)
name|getVectorDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|vectorGroupByDesc
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|GroupByOperatorExplainVectorization
argument_list|(
name|this
argument_list|,
name|vectorGroupByDesc
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getComplexTypeEnabledCondition
parameter_list|(
name|boolean
name|isVectorizationComplexTypesEnabled
parameter_list|)
block|{
return|return
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_COMPLEX_TYPES_ENABLED
operator|.
name|varname
operator|+
literal|" IS "
operator|+
name|isVectorizationComplexTypesEnabled
return|;
block|}
specifier|public
specifier|static
name|String
name|getComplexTypeWithGroupByEnabledCondition
parameter_list|(
name|boolean
name|isVectorizationComplexTypesEnabled
parameter_list|,
name|boolean
name|isVectorizationGroupByComplexTypesEnabled
parameter_list|)
block|{
specifier|final
name|boolean
name|enabled
init|=
operator|(
name|isVectorizationComplexTypesEnabled
operator|&&
name|isVectorizationGroupByComplexTypesEnabled
operator|)
decl_stmt|;
return|return
literal|"("
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_COMPLEX_TYPES_ENABLED
operator|.
name|varname
operator|+
literal|" "
operator|+
name|isVectorizationComplexTypesEnabled
operator|+
literal|" AND "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_GROUPBY_COMPLEX_TYPES_ENABLED
operator|.
name|varname
operator|+
literal|" "
operator|+
name|isVectorizationGroupByComplexTypesEnabled
operator|+
literal|") IS "
operator|+
name|enabled
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSame
parameter_list|(
name|OperatorDesc
name|other
parameter_list|)
block|{
if|if
condition|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|GroupByDesc
name|otherDesc
init|=
operator|(
name|GroupByDesc
operator|)
name|other
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|getModeString
argument_list|()
argument_list|,
name|otherDesc
operator|.
name|getModeString
argument_list|()
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|getKeyString
argument_list|()
argument_list|,
name|otherDesc
operator|.
name|getKeyString
argument_list|()
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|getOutputColumnNames
argument_list|()
argument_list|,
name|otherDesc
operator|.
name|getOutputColumnNames
argument_list|()
argument_list|)
operator|&&
name|pruneGroupingSetId
argument_list|()
operator|==
name|otherDesc
operator|.
name|pruneGroupingSetId
argument_list|()
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|getAggregatorStrings
argument_list|()
argument_list|,
name|otherDesc
operator|.
name|getAggregatorStrings
argument_list|()
argument_list|)
operator|&&
name|getBucketGroup
argument_list|()
operator|==
name|otherDesc
operator|.
name|getBucketGroup
argument_list|()
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

