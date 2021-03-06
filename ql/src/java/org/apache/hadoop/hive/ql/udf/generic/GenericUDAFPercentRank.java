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
name|udf
operator|.
name|generic
package|;
end_package

begin_import
import|import static
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
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
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|Description
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
name|WindowFunctionDescription
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
name|serde2
operator|.
name|io
operator|.
name|DoubleWritable
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
name|ObjectInspectorFactory
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
name|io
operator|.
name|IntWritable
import|;
end_import

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"percent_rank"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x) PERCENT_RANK is similar to CUME_DIST, but it uses rank values rather "
operator|+
literal|"than row counts in its numerator. PERCENT_RANK of a row is calculated as: "
operator|+
literal|"(rank of row in its partition - 1) / (number of rows in the partition - 1)"
argument_list|)
annotation|@
name|WindowFunctionDescription
argument_list|(
name|supportsWindow
operator|=
literal|false
argument_list|,
name|pivotResult
operator|=
literal|true
argument_list|,
name|rankingFunction
operator|=
literal|true
argument_list|,
name|orderedAggregate
operator|=
literal|true
argument_list|)
specifier|public
class|class
name|GenericUDAFPercentRank
extends|extends
name|GenericUDAFRank
block|{
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|GenericUDAFPercentRank
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|GenericUDAFAbstractRankEvaluator
name|createWindowingEvaluator
parameter_list|()
block|{
return|return
operator|new
name|GenericUDAFPercentRankEvaluator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|GenericUDAFHypotheticalSetRankEvaluator
name|createHypotheticalSetEvaluator
parameter_list|()
block|{
return|return
operator|new
name|GenericUDAFHypotheticalSetPercentRankEvaluator
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|GenericUDAFPercentRankEvaluator
extends|extends
name|GenericUDAFAbstractRankEvaluator
block|{
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|init
parameter_list|(
name|Mode
name|m
parameter_list|,
name|ObjectInspector
index|[]
name|parameters
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|init
argument_list|(
name|m
argument_list|,
name|parameters
argument_list|)
expr_stmt|;
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|writableDoubleObjectInspector
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|terminate
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
name|ArrayList
argument_list|<
name|IntWritable
argument_list|>
name|ranks
init|=
operator|(
operator|(
name|RankBuffer
operator|)
name|agg
operator|)
operator|.
name|rowNums
decl_stmt|;
name|double
name|sz
init|=
name|ranks
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|sz
operator|>
literal|1
condition|)
block|{
name|sz
operator|=
name|sz
operator|-
literal|1
expr_stmt|;
block|}
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
name|pranks
init|=
operator|new
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
argument_list|(
name|ranks
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|IntWritable
name|i
range|:
name|ranks
control|)
block|{
name|double
name|pr
init|=
operator|(
operator|(
name|double
operator|)
name|i
operator|.
name|get
argument_list|()
operator|-
literal|1
operator|)
operator|/
name|sz
decl_stmt|;
name|pranks
operator|.
name|add
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
name|pr
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|pranks
return|;
block|}
block|}
comment|/**    * Evaluator for calculating the percent rank.    * SELECT percent_rank(expression1[, expressionn]*) WITHIN GROUP (ORDER BY col1[, coln]*)    * Implementation is based on hypothetical rank calculation: rank - 1 / count    */
specifier|public
specifier|static
class|class
name|GenericUDAFHypotheticalSetPercentRankEvaluator
extends|extends
name|GenericUDAFHypotheticalSetRankEvaluator
block|{
specifier|public
name|GenericUDAFHypotheticalSetPercentRankEvaluator
parameter_list|()
block|{
name|super
argument_list|(
literal|false
argument_list|,
name|PARTIAL_RANK_OI
argument_list|,
name|writableDoubleObjectInspector
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|terminate
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
name|HypotheticalSetRankBuffer
name|rankBuffer
init|=
operator|(
name|HypotheticalSetRankBuffer
operator|)
name|agg
decl_stmt|;
return|return
operator|new
name|DoubleWritable
argument_list|(
operator|(
operator|(
name|double
operator|)
name|rankBuffer
operator|.
name|rank
operator|)
operator|/
name|rankBuffer
operator|.
name|rowCount
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

