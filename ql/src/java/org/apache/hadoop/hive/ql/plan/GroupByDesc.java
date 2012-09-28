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
argument_list|)
specifier|public
class|class
name|GroupByDesc
extends|extends
name|AbstractOperatorDesc
block|{
comment|/**    * Group-by Mode: COMPLETE: complete 1-phase aggregation: iterate, terminate    * PARTIAL1: partial aggregation - first phase: iterate, terminatePartial    * PARTIAL2: partial aggregation - second phase: merge, terminatePartial    * PARTIALS: For non-distinct the same as PARTIAL2, for distinct the same as    *           PARTIAL1    * FINAL: partial aggregation - final phase: merge, terminate    * HASH: For non-distinct the same as PARTIAL1 but use hash-table-based aggregation    * MERGEPARTIAL: FINAL for non-distinct aggregations, COMPLETE for distinct    * aggregations.    */
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
specifier|private
name|boolean
name|groupKeyNotReductionKey
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
name|boolean
name|groupKeyNotReductionKey
parameter_list|,
name|float
name|groupByMemoryUsage
parameter_list|,
name|float
name|memoryThreshold
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
name|groupKeyNotReductionKey
argument_list|,
literal|false
argument_list|,
name|groupByMemoryUsage
argument_list|,
name|memoryThreshold
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
name|groupKeyNotReductionKey
parameter_list|,
specifier|final
name|boolean
name|bucketGroup
parameter_list|,
name|float
name|groupByMemoryUsage
parameter_list|,
name|float
name|memoryThreshold
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
name|groupKeyNotReductionKey
operator|=
name|groupKeyNotReductionKey
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
argument_list|)
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
name|getGroupKeyNotReductionKey
parameter_list|()
block|{
return|return
name|groupKeyNotReductionKey
return|;
block|}
specifier|public
name|void
name|setGroupKeyNotReductionKey
parameter_list|(
specifier|final
name|boolean
name|groupKeyNotReductionKey
parameter_list|)
block|{
name|this
operator|.
name|groupKeyNotReductionKey
operator|=
name|groupKeyNotReductionKey
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"bucketGroup"
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
name|udafEval
operator|.
name|getClass
argument_list|()
operator|.
name|getAnnotation
argument_list|(
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
block|}
end_class

end_unit

