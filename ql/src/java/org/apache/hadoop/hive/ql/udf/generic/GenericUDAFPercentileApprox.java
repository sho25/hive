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
name|UDFArgumentTypeException
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
name|parse
operator|.
name|SemanticException
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
name|util
operator|.
name|JavaDataModel
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
name|ConstantObjectInspector
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
name|ListObjectInspector
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
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
name|PrimitiveObjectInspector
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
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
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
name|StandardListObjectInspector
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
name|primitive
operator|.
name|DoubleObjectInspector
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
import|;
end_import

begin_comment
comment|/**  * Computes an approximate percentile (quantile) from an approximate histogram, for very  * large numbers of rows where the regular percentile() UDAF might run out of memory.  *  * The input is a single double value or an array of double values representing the quantiles  * requested. The output, corresponding to the input, is either an single double value or an  * array of doubles that are the quantile values.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"percentile_approx"
argument_list|,
name|value
operator|=
literal|"_FUNC_(expr, pc, [nb]) - For very large data, computes an approximate percentile "
operator|+
literal|"value from a histogram, using the optional argument [nb] as the number of histogram"
operator|+
literal|" bins to use. A higher value of nb results in a more accurate approximation, at "
operator|+
literal|"the cost of higher memory usage."
argument_list|,
name|extended
operator|=
literal|"'expr' can be any numeric column, including doubles and floats, and 'pc' is "
operator|+
literal|"either a single double/float with a requested percentile, or an array of double/"
operator|+
literal|"float with multiple percentiles. If 'nb' is not specified, the default "
operator|+
literal|"approximation is done with 10,000 histogram bins, which means that if there are "
operator|+
literal|"10,000 or fewer unique values in 'expr', you can expect an exact result. The "
operator|+
literal|"percentile() function always computes an exact percentile and can run out of "
operator|+
literal|"memory if there are too many unique values in a column, which necessitates "
operator|+
literal|"this function.\n"
operator|+
literal|"Example (three percentiles requested using a finer histogram approximation):\n"
operator|+
literal|"> SELECT percentile_approx(val, array(0.5, 0.95, 0.98), 100000) FROM somedata;\n"
operator|+
literal|"[0.05,1.64,2.26]\n"
argument_list|)
specifier|public
class|class
name|GenericUDAFPercentileApprox
extends|extends
name|AbstractGenericUDAFResolver
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
name|GenericUDAFPercentileApprox
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|void
name|verifyFractionType
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|)
throws|throws
name|UDFArgumentTypeException
block|{
name|PrimitiveCategory
name|pc
init|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|pc
condition|)
block|{
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
case|case
name|DECIMAL
case|:
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"Only a floating point or decimal, or "
operator|+
literal|"floating point or decimal array argument is accepted as parameter 2, but "
operator|+
name|pc
operator|+
literal|" was passed instead."
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getEvaluator
parameter_list|(
name|GenericUDAFParameterInfo
name|info
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ObjectInspector
index|[]
name|parameters
init|=
name|info
operator|.
name|getParameterObjectInspectors
argument_list|()
decl_stmt|;
if|if
condition|(
name|parameters
operator|.
name|length
operator|!=
literal|2
operator|&&
name|parameters
operator|.
name|length
operator|!=
literal|3
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|parameters
operator|.
name|length
operator|-
literal|1
argument_list|,
literal|"Please specify either two or three arguments."
argument_list|)
throw|;
block|}
comment|// Validate the first parameter, which is the expression to compute over. This should be a
comment|// numeric primitive type.
if|if
condition|(
name|parameters
index|[
literal|0
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Only primitive type arguments are accepted but "
operator|+
name|parameters
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed as parameter 1."
argument_list|)
throw|;
block|}
switch|switch
condition|(
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|parameters
index|[
literal|0
index|]
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
case|case
name|TIMESTAMP
case|:
case|case
name|DECIMAL
case|:
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Only numeric type arguments are accepted but "
operator|+
name|parameters
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed as parameter 1."
argument_list|)
throw|;
block|}
comment|// Validate the second parameter, which is either a solitary double or an array of doubles.
name|boolean
name|wantManyQuantiles
init|=
literal|false
decl_stmt|;
switch|switch
condition|(
name|parameters
index|[
literal|1
index|]
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
comment|// Only a single double was passed as parameter 2, a single quantile is being requested
name|verifyFractionType
argument_list|(
name|parameters
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
name|LIST
case|:
comment|// An array was passed as parameter 2, make sure it's an array of primitives
if|if
condition|(
operator|(
operator|(
name|ListObjectInspector
operator|)
name|parameters
index|[
literal|1
index|]
operator|)
operator|.
name|getListElementObjectInspector
argument_list|()
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"A floating point or decimal array argument may be passed as parameter 2, but "
operator|+
name|parameters
index|[
literal|1
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed instead."
argument_list|)
throw|;
block|}
comment|// Now make sure it's an array of doubles or floats. We don't allow integer types here
comment|// because percentile (really, quantile) values should generally be strictly between 0 and 1.
name|verifyFractionType
argument_list|(
operator|(
operator|(
name|ListObjectInspector
operator|)
name|parameters
index|[
literal|1
index|]
operator|)
operator|.
name|getListElementObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
name|wantManyQuantiles
operator|=
literal|true
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"Only a floating point or decimal, or floating point or decimal array argument is accepted"
operator|+
literal|" as parameter 2, but "
operator|+
name|parameters
index|[
literal|1
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed instead."
argument_list|)
throw|;
block|}
comment|// Also make sure it is a constant.
if|if
condition|(
operator|!
name|ObjectInspectorUtils
operator|.
name|isConstantObjectInspector
argument_list|(
name|parameters
index|[
literal|1
index|]
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"The second argument must be a constant, but "
operator|+
name|parameters
index|[
literal|1
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed instead."
argument_list|)
throw|;
block|}
comment|// If a third parameter has been specified, it should be an integer that specifies the number
comment|// of histogram bins to use in the percentile approximation.
if|if
condition|(
name|parameters
operator|.
name|length
operator|==
literal|3
condition|)
block|{
if|if
condition|(
name|parameters
index|[
literal|2
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|2
argument_list|,
literal|"Only a primitive argument is accepted as "
operator|+
literal|"parameter 3, but "
operator|+
name|parameters
index|[
literal|2
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed instead."
argument_list|)
throw|;
block|}
switch|switch
condition|(
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|parameters
index|[
literal|2
index|]
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
case|case
name|TIMESTAMP
case|:
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|2
argument_list|,
literal|"Only an integer argument is accepted as "
operator|+
literal|"parameter 3, but "
operator|+
name|parameters
index|[
literal|2
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed instead."
argument_list|)
throw|;
block|}
comment|// Also make sure it is a constant.
if|if
condition|(
operator|!
name|ObjectInspectorUtils
operator|.
name|isConstantObjectInspector
argument_list|(
name|parameters
index|[
literal|2
index|]
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|2
argument_list|,
literal|"The third argument must be a constant, but "
operator|+
name|parameters
index|[
literal|2
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed instead."
argument_list|)
throw|;
block|}
block|}
comment|// Return an evaluator depending on the return type
if|if
condition|(
name|wantManyQuantiles
condition|)
block|{
return|return
operator|new
name|GenericUDAFMultiplePercentileApproxEvaluator
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|new
name|GenericUDAFSinglePercentileApproxEvaluator
argument_list|()
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|GenericUDAFSinglePercentileApproxEvaluator
extends|extends
name|GenericUDAFPercentileApproxEvaluator
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
comment|// init input object inspectors
if|if
condition|(
name|m
operator|==
name|Mode
operator|.
name|PARTIAL1
operator|||
name|m
operator|==
name|Mode
operator|.
name|COMPLETE
condition|)
block|{
name|inputOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|parameters
index|[
literal|0
index|]
expr_stmt|;
name|quantiles
operator|=
name|getQuantileArray
argument_list|(
operator|(
name|ConstantObjectInspector
operator|)
name|parameters
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|parameters
operator|.
name|length
operator|>
literal|2
condition|)
block|{
name|nbins
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getInt
argument_list|(
operator|(
operator|(
name|ConstantObjectInspector
operator|)
name|parameters
index|[
literal|2
index|]
operator|)
operator|.
name|getWritableConstantValue
argument_list|()
argument_list|,
operator|(
name|PrimitiveObjectInspector
operator|)
name|parameters
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|loi
operator|=
operator|(
name|StandardListObjectInspector
operator|)
name|parameters
index|[
literal|0
index|]
expr_stmt|;
block|}
comment|// Init output object inspectors.
comment|//
comment|// The return type for a partial aggregation is still a list of doubles, as in
comment|// GenericUDAFHistogramNumeric, but we add on the percentile values requested to the
comment|// end, and handle serializing/deserializing before we pass things on to the parent
comment|// method.
comment|// The return type for FINAL and COMPLETE is a full aggregation result, which is a
comment|// single double value
if|if
condition|(
name|m
operator|==
name|Mode
operator|.
name|PARTIAL1
operator|||
name|m
operator|==
name|Mode
operator|.
name|PARTIAL2
condition|)
block|{
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
return|;
block|}
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
name|PercentileAggBuf
name|myagg
init|=
operator|(
name|PercentileAggBuf
operator|)
name|agg
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|histogram
operator|.
name|getUsedBins
argument_list|()
operator|<
literal|1
condition|)
block|{
comment|// SQL standard - return null for zero elements
return|return
literal|null
return|;
block|}
else|else
block|{
assert|assert
operator|(
name|myagg
operator|.
name|quantiles
operator|!=
literal|null
operator|)
assert|;
return|return
operator|new
name|DoubleWritable
argument_list|(
name|myagg
operator|.
name|histogram
operator|.
name|quantile
argument_list|(
name|myagg
operator|.
name|quantiles
index|[
literal|0
index|]
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|GenericUDAFMultiplePercentileApproxEvaluator
extends|extends
name|GenericUDAFPercentileApproxEvaluator
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
comment|// init input object inspectors
if|if
condition|(
name|m
operator|==
name|Mode
operator|.
name|PARTIAL1
operator|||
name|m
operator|==
name|Mode
operator|.
name|COMPLETE
condition|)
block|{
name|inputOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|parameters
index|[
literal|0
index|]
expr_stmt|;
name|quantiles
operator|=
name|getQuantileArray
argument_list|(
operator|(
name|ConstantObjectInspector
operator|)
name|parameters
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|parameters
operator|.
name|length
operator|>
literal|2
condition|)
block|{
name|nbins
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getInt
argument_list|(
operator|(
operator|(
name|ConstantObjectInspector
operator|)
name|parameters
index|[
literal|2
index|]
operator|)
operator|.
name|getWritableConstantValue
argument_list|()
argument_list|,
operator|(
name|PrimitiveObjectInspector
operator|)
name|parameters
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|loi
operator|=
operator|(
name|StandardListObjectInspector
operator|)
name|parameters
index|[
literal|0
index|]
expr_stmt|;
block|}
comment|// Init output object inspectors.
comment|//
comment|// The return type for a partial aggregation is still a list of doubles, as in
comment|// GenericUDAFHistogramNumeric, but we add on the percentile values requested to the
comment|// end, and handle serializing/deserializing before we pass things on to the parent
comment|// method.
comment|// The return type for FINAL and COMPLETE is a full aggregation result, which is also
comment|// a list of doubles
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
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
name|PercentileAggBuf
name|myagg
init|=
operator|(
name|PercentileAggBuf
operator|)
name|agg
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|histogram
operator|.
name|getUsedBins
argument_list|()
operator|<
literal|1
condition|)
block|{
comment|// SQL standard - return null for zero elements
return|return
literal|null
return|;
block|}
else|else
block|{
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|myagg
operator|.
name|quantiles
operator|!=
literal|null
operator|)
assert|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|myagg
operator|.
name|quantiles
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
name|myagg
operator|.
name|histogram
operator|.
name|quantile
argument_list|(
name|myagg
operator|.
name|quantiles
index|[
name|i
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
block|}
comment|/**    * Construct a histogram using the algorithm described by Ben-Haim and Tom-Tov, and then    * use it to compute an approximate percentile value.    */
specifier|public
specifier|abstract
specifier|static
class|class
name|GenericUDAFPercentileApproxEvaluator
extends|extends
name|GenericUDAFEvaluator
block|{
comment|// For PARTIAL1 and COMPLETE: ObjectInspectors for original data
specifier|protected
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
specifier|protected
name|double
name|quantiles
index|[]
decl_stmt|;
specifier|protected
name|Integer
name|nbins
init|=
literal|10000
decl_stmt|;
comment|// For PARTIAL2 and FINAL: ObjectInspectors for partial aggregations (list of doubles)
specifier|protected
specifier|transient
name|StandardListObjectInspector
name|loi
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|merge
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|,
name|Object
name|partial
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|partial
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|PercentileAggBuf
name|myagg
init|=
operator|(
name|PercentileAggBuf
operator|)
name|agg
decl_stmt|;
name|List
name|partialHistogram
init|=
operator|(
name|List
operator|)
name|loi
operator|.
name|getList
argument_list|(
name|partial
argument_list|)
decl_stmt|;
name|DoubleObjectInspector
name|doi
init|=
operator|(
name|DoubleObjectInspector
operator|)
name|loi
operator|.
name|getListElementObjectInspector
argument_list|()
decl_stmt|;
comment|// remove requested quantiles from the head of the list
name|int
name|nquantiles
init|=
operator|(
name|int
operator|)
name|doi
operator|.
name|get
argument_list|(
name|partialHistogram
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|nquantiles
operator|>
literal|0
condition|)
block|{
name|myagg
operator|.
name|quantiles
operator|=
operator|new
name|double
index|[
name|nquantiles
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|nquantiles
condition|;
name|i
operator|++
control|)
block|{
name|myagg
operator|.
name|quantiles
index|[
name|i
operator|-
literal|1
index|]
operator|=
name|doi
operator|.
name|get
argument_list|(
name|partialHistogram
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|partialHistogram
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|nquantiles
operator|+
literal|1
argument_list|)
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|partialHistogram
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|// merge histograms
name|myagg
operator|.
name|histogram
operator|.
name|merge
argument_list|(
name|partialHistogram
argument_list|,
name|doi
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|terminatePartial
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
name|PercentileAggBuf
name|myagg
init|=
operator|(
name|PercentileAggBuf
operator|)
name|agg
decl_stmt|;
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|quantiles
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
name|myagg
operator|.
name|quantiles
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|myagg
operator|.
name|quantiles
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
name|myagg
operator|.
name|quantiles
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|addAll
argument_list|(
name|myagg
operator|.
name|histogram
operator|.
name|serialize
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|iterate
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|,
name|Object
index|[]
name|parameters
parameter_list|)
throws|throws
name|HiveException
block|{
assert|assert
operator|(
name|parameters
operator|.
name|length
operator|==
literal|2
operator|||
name|parameters
operator|.
name|length
operator|==
literal|3
operator|)
assert|;
if|if
condition|(
name|parameters
index|[
literal|0
index|]
operator|==
literal|null
operator|||
name|parameters
index|[
literal|1
index|]
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|PercentileAggBuf
name|myagg
init|=
operator|(
name|PercentileAggBuf
operator|)
name|agg
decl_stmt|;
comment|// Get and process the current datum
name|double
name|v
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getDouble
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|,
name|inputOI
argument_list|)
decl_stmt|;
name|myagg
operator|.
name|histogram
operator|.
name|add
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
comment|// Aggregation buffer methods. We wrap GenericUDAFHistogramNumeric's aggregation buffer
comment|// inside our own, so that we can also store requested quantile values between calls
annotation|@
name|AggregationType
argument_list|(
name|estimable
operator|=
literal|true
argument_list|)
specifier|static
class|class
name|PercentileAggBuf
extends|extends
name|AbstractAggregationBuffer
block|{
name|NumericHistogram
name|histogram
decl_stmt|;
comment|// histogram used for quantile approximation
name|double
index|[]
name|quantiles
decl_stmt|;
comment|// the quantiles requested
annotation|@
name|Override
specifier|public
name|int
name|estimate
parameter_list|()
block|{
name|JavaDataModel
name|model
init|=
name|JavaDataModel
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
name|histogram
operator|.
name|lengthFor
argument_list|(
name|model
argument_list|)
operator|+
name|model
operator|.
name|array
argument_list|()
operator|+
name|JavaDataModel
operator|.
name|PRIMITIVES2
operator|*
name|quantiles
operator|.
name|length
return|;
block|}
block|}
empty_stmt|;
annotation|@
name|Override
specifier|public
name|AggregationBuffer
name|getNewAggregationBuffer
parameter_list|()
throws|throws
name|HiveException
block|{
name|PercentileAggBuf
name|result
init|=
operator|new
name|PercentileAggBuf
argument_list|()
decl_stmt|;
name|result
operator|.
name|histogram
operator|=
operator|new
name|NumericHistogram
argument_list|()
expr_stmt|;
name|reset
argument_list|(
name|result
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|protected
name|double
index|[]
name|getQuantileArray
parameter_list|(
name|ConstantObjectInspector
name|quantileOI
parameter_list|)
throws|throws
name|HiveException
block|{
name|double
index|[]
name|result
init|=
literal|null
decl_stmt|;
name|Object
name|quantileObj
init|=
name|quantileOI
operator|.
name|getWritableConstantValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|quantileOI
operator|instanceof
name|ListObjectInspector
condition|)
block|{
name|ObjectInspector
name|elemOI
init|=
operator|(
operator|(
name|ListObjectInspector
operator|)
name|quantileOI
operator|)
operator|.
name|getListElementObjectInspector
argument_list|()
decl_stmt|;
name|result
operator|=
operator|new
name|double
index|[
operator|(
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|quantileObj
operator|)
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
assert|assert
operator|(
name|result
operator|.
name|length
operator|>=
literal|1
operator|)
assert|;
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
name|result
operator|.
name|length
condition|;
operator|++
name|ii
control|)
block|{
name|result
index|[
name|ii
index|]
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getDouble
argument_list|(
operator|(
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|quantileObj
operator|)
operator|.
name|get
argument_list|(
name|ii
argument_list|)
argument_list|,
operator|(
name|PrimitiveObjectInspector
operator|)
name|elemOI
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|result
operator|=
operator|new
name|double
index|[
literal|1
index|]
expr_stmt|;
name|result
index|[
literal|0
index|]
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getDouble
argument_list|(
name|quantileObj
argument_list|,
operator|(
name|PrimitiveObjectInspector
operator|)
name|quantileOI
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
name|result
operator|.
name|length
condition|;
operator|++
name|ii
control|)
block|{
if|if
condition|(
name|result
index|[
name|ii
index|]
operator|<=
literal|0
operator|||
name|result
index|[
name|ii
index|]
operator|>=
literal|1
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" requires percentile values to "
operator|+
literal|"lie strictly between 0 and 1, but you supplied "
operator|+
name|result
index|[
name|ii
index|]
argument_list|)
throw|;
block|}
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
name|PercentileAggBuf
name|result
init|=
operator|(
name|PercentileAggBuf
operator|)
name|agg
decl_stmt|;
name|result
operator|.
name|histogram
operator|.
name|reset
argument_list|()
expr_stmt|;
name|result
operator|.
name|quantiles
operator|=
literal|null
expr_stmt|;
name|result
operator|.
name|histogram
operator|.
name|allocate
argument_list|(
name|nbins
argument_list|)
expr_stmt|;
name|result
operator|.
name|quantiles
operator|=
name|quantiles
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

