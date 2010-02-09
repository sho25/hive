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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|StructField
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
name|StructObjectInspector
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
name|LongObjectInspector
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
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|typeinfo
operator|.
name|TypeInfo
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
name|LongWritable
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Compute the variance. This class is extended by: GenericUDAFVarianceSample  * GenericUDAFStd GenericUDAFStdSample  *   */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"variance,var_pop"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x) - Returns the variance of a set of numbers"
argument_list|)
specifier|public
class|class
name|GenericUDAFVariance
implements|implements
name|GenericUDAFResolver
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|GenericUDAFVariance
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getEvaluator
parameter_list|(
name|TypeInfo
index|[]
name|parameters
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|parameters
operator|.
name|length
operator|!=
literal|1
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
literal|"Exactly one argument is expected."
argument_list|)
throw|;
block|}
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
literal|" is passed."
argument_list|)
throw|;
block|}
switch|switch
condition|(
operator|(
operator|(
name|PrimitiveTypeInfo
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
name|STRING
case|:
return|return
operator|new
name|GenericUDAFVarianceEvaluator
argument_list|()
return|;
case|case
name|BOOLEAN
case|:
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Only numeric or string type arguments are accepted but "
operator|+
name|parameters
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" is passed."
argument_list|)
throw|;
block|}
block|}
comment|/**    * Evaluate the variance using the following modification of the formula from    * The Art of Computer Programming, vol. 2, p. 232:    *     * variance = variance1 + variance2 + n*alpha^2 + m*betha^2    *     * where: - variance is sum[x-avg^2] (this is actually n times the variance)    * and is updated at every step. - n is the count of elements in chunk1 - m is    * the count of elements in chunk2 - alpha = avg-a - betha = avg-b - avg is    * the the average of all elements from both chunks - a is the average of    * elements in chunk1 - b is the average of elements in chunk2    *     */
specifier|public
specifier|static
class|class
name|GenericUDAFVarianceEvaluator
extends|extends
name|GenericUDAFEvaluator
block|{
comment|// For PARTIAL1 and COMPLETE
specifier|private
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
comment|// For PARTIAL2 and FINAL
specifier|private
name|StructObjectInspector
name|soi
decl_stmt|;
specifier|private
name|StructField
name|countField
decl_stmt|;
specifier|private
name|StructField
name|sumField
decl_stmt|;
specifier|private
name|StructField
name|varianceField
decl_stmt|;
specifier|private
name|LongObjectInspector
name|countFieldOI
decl_stmt|;
specifier|private
name|DoubleObjectInspector
name|sumFieldOI
decl_stmt|;
specifier|private
name|DoubleObjectInspector
name|varianceFieldOI
decl_stmt|;
comment|// For PARTIAL1 and PARTIAL2
specifier|private
name|Object
index|[]
name|partialResult
decl_stmt|;
comment|// For FINAL and COMPLETE
specifier|private
name|DoubleWritable
name|result
decl_stmt|;
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
assert|assert
operator|(
name|parameters
operator|.
name|length
operator|==
literal|1
operator|)
assert|;
name|super
operator|.
name|init
argument_list|(
name|m
argument_list|,
name|parameters
argument_list|)
expr_stmt|;
comment|// init input
if|if
condition|(
name|mode
operator|==
name|Mode
operator|.
name|PARTIAL1
operator|||
name|mode
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
block|}
else|else
block|{
name|soi
operator|=
operator|(
name|StructObjectInspector
operator|)
name|parameters
index|[
literal|0
index|]
expr_stmt|;
name|countField
operator|=
name|soi
operator|.
name|getStructFieldRef
argument_list|(
literal|"count"
argument_list|)
expr_stmt|;
name|sumField
operator|=
name|soi
operator|.
name|getStructFieldRef
argument_list|(
literal|"sum"
argument_list|)
expr_stmt|;
name|varianceField
operator|=
name|soi
operator|.
name|getStructFieldRef
argument_list|(
literal|"variance"
argument_list|)
expr_stmt|;
name|countFieldOI
operator|=
operator|(
name|LongObjectInspector
operator|)
name|countField
operator|.
name|getFieldObjectInspector
argument_list|()
expr_stmt|;
name|sumFieldOI
operator|=
operator|(
name|DoubleObjectInspector
operator|)
name|sumField
operator|.
name|getFieldObjectInspector
argument_list|()
expr_stmt|;
name|varianceFieldOI
operator|=
operator|(
name|DoubleObjectInspector
operator|)
name|varianceField
operator|.
name|getFieldObjectInspector
argument_list|()
expr_stmt|;
block|}
comment|// init output
if|if
condition|(
name|mode
operator|==
name|Mode
operator|.
name|PARTIAL1
operator|||
name|mode
operator|==
name|Mode
operator|.
name|PARTIAL2
condition|)
block|{
comment|// The output of a partial aggregation is a struct containing
comment|// a long count and doubles sum and variance.
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|foi
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|foi
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
argument_list|)
expr_stmt|;
name|foi
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
argument_list|)
expr_stmt|;
name|foi
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|fname
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|fname
operator|.
name|add
argument_list|(
literal|"count"
argument_list|)
expr_stmt|;
name|fname
operator|.
name|add
argument_list|(
literal|"sum"
argument_list|)
expr_stmt|;
name|fname
operator|.
name|add
argument_list|(
literal|"variance"
argument_list|)
expr_stmt|;
name|partialResult
operator|=
operator|new
name|Object
index|[
literal|3
index|]
expr_stmt|;
name|partialResult
index|[
literal|0
index|]
operator|=
operator|new
name|LongWritable
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|partialResult
index|[
literal|1
index|]
operator|=
operator|new
name|DoubleWritable
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|partialResult
index|[
literal|2
index|]
operator|=
operator|new
name|DoubleWritable
argument_list|(
literal|0
argument_list|)
expr_stmt|;
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|fname
argument_list|,
name|foi
argument_list|)
return|;
block|}
else|else
block|{
name|setResult
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
return|;
block|}
block|}
specifier|static
class|class
name|StdAgg
implements|implements
name|AggregationBuffer
block|{
name|long
name|count
decl_stmt|;
comment|// number of elements
name|double
name|sum
decl_stmt|;
comment|// sum of elements
name|double
name|variance
decl_stmt|;
comment|// sum[x-avg^2] (this is actually n times the variance)
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
name|StdAgg
name|result
init|=
operator|new
name|StdAgg
argument_list|()
decl_stmt|;
name|reset
argument_list|(
name|result
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
name|reset
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
name|StdAgg
name|myagg
init|=
operator|(
name|StdAgg
operator|)
name|agg
decl_stmt|;
name|myagg
operator|.
name|count
operator|=
literal|0
expr_stmt|;
name|myagg
operator|.
name|sum
operator|=
literal|0
expr_stmt|;
name|myagg
operator|.
name|variance
operator|=
literal|0
expr_stmt|;
block|}
specifier|private
name|boolean
name|warned
init|=
literal|false
decl_stmt|;
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
literal|1
operator|)
assert|;
name|Object
name|p
init|=
name|parameters
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|p
operator|!=
literal|null
condition|)
block|{
name|StdAgg
name|myagg
init|=
operator|(
name|StdAgg
operator|)
name|agg
decl_stmt|;
try|try
block|{
name|double
name|v
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getDouble
argument_list|(
name|p
argument_list|,
name|inputOI
argument_list|)
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|count
operator|!=
literal|0
condition|)
block|{
comment|// if count==0 => the variance is going to be
comment|// 0
comment|// after 1 iteration
name|double
name|alpha
init|=
operator|(
name|myagg
operator|.
name|sum
operator|+
name|v
operator|)
operator|/
operator|(
name|myagg
operator|.
name|count
operator|+
literal|1
operator|)
operator|-
name|myagg
operator|.
name|sum
operator|/
name|myagg
operator|.
name|count
decl_stmt|;
name|double
name|betha
init|=
operator|(
name|myagg
operator|.
name|sum
operator|+
name|v
operator|)
operator|/
operator|(
name|myagg
operator|.
name|count
operator|+
literal|1
operator|)
operator|-
name|v
decl_stmt|;
comment|// variance = variance1 + variance2 + n*alpha^2 + m*betha^2
comment|// => variance += n*alpha^2 + betha^2
name|myagg
operator|.
name|variance
operator|+=
name|myagg
operator|.
name|count
operator|*
name|alpha
operator|*
name|alpha
operator|+
name|betha
operator|*
name|betha
expr_stmt|;
block|}
name|myagg
operator|.
name|count
operator|++
expr_stmt|;
name|myagg
operator|.
name|sum
operator|+=
name|v
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|warned
condition|)
block|{
name|warned
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" ignoring similar exceptions."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
name|StdAgg
name|myagg
init|=
operator|(
name|StdAgg
operator|)
name|agg
decl_stmt|;
operator|(
operator|(
name|LongWritable
operator|)
name|partialResult
index|[
literal|0
index|]
operator|)
operator|.
name|set
argument_list|(
name|myagg
operator|.
name|count
argument_list|)
expr_stmt|;
operator|(
operator|(
name|DoubleWritable
operator|)
name|partialResult
index|[
literal|1
index|]
operator|)
operator|.
name|set
argument_list|(
name|myagg
operator|.
name|sum
argument_list|)
expr_stmt|;
operator|(
operator|(
name|DoubleWritable
operator|)
name|partialResult
index|[
literal|2
index|]
operator|)
operator|.
name|set
argument_list|(
name|myagg
operator|.
name|variance
argument_list|)
expr_stmt|;
return|return
name|partialResult
return|;
block|}
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
operator|!=
literal|null
condition|)
block|{
name|StdAgg
name|myagg
init|=
operator|(
name|StdAgg
operator|)
name|agg
decl_stmt|;
name|Object
name|partialCount
init|=
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|partial
argument_list|,
name|countField
argument_list|)
decl_stmt|;
name|Object
name|partialSum
init|=
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|partial
argument_list|,
name|sumField
argument_list|)
decl_stmt|;
name|Object
name|partialVariance
init|=
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|partial
argument_list|,
name|varianceField
argument_list|)
decl_stmt|;
name|long
name|n
init|=
name|myagg
operator|.
name|count
decl_stmt|;
name|long
name|m
init|=
name|countFieldOI
operator|.
name|get
argument_list|(
name|partialCount
argument_list|)
decl_stmt|;
if|if
condition|(
name|n
operator|==
literal|0
condition|)
block|{
comment|// Just copy the information since there is nothing so far
name|myagg
operator|.
name|variance
operator|=
name|sumFieldOI
operator|.
name|get
argument_list|(
name|partialVariance
argument_list|)
expr_stmt|;
name|myagg
operator|.
name|count
operator|=
name|countFieldOI
operator|.
name|get
argument_list|(
name|partialCount
argument_list|)
expr_stmt|;
name|myagg
operator|.
name|sum
operator|=
name|sumFieldOI
operator|.
name|get
argument_list|(
name|partialSum
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|m
operator|!=
literal|0
operator|&&
name|n
operator|!=
literal|0
condition|)
block|{
comment|// Merge the two partials
name|double
name|a
init|=
name|myagg
operator|.
name|sum
decl_stmt|;
name|double
name|b
init|=
name|sumFieldOI
operator|.
name|get
argument_list|(
name|partialSum
argument_list|)
decl_stmt|;
name|double
name|alpha
init|=
operator|(
name|a
operator|+
name|b
operator|)
operator|/
operator|(
name|n
operator|+
name|m
operator|)
operator|-
name|a
operator|/
name|n
decl_stmt|;
name|double
name|betha
init|=
operator|(
name|a
operator|+
name|b
operator|)
operator|/
operator|(
name|n
operator|+
name|m
operator|)
operator|-
name|b
operator|/
name|m
decl_stmt|;
comment|// variance = variance1 + variance2 + n*alpha^2 + m*betha^2
name|myagg
operator|.
name|variance
operator|+=
name|sumFieldOI
operator|.
name|get
argument_list|(
name|partialVariance
argument_list|)
operator|+
operator|(
name|n
operator|*
name|alpha
operator|*
name|alpha
operator|+
name|m
operator|*
name|betha
operator|*
name|betha
operator|)
expr_stmt|;
name|myagg
operator|.
name|count
operator|+=
name|m
expr_stmt|;
name|myagg
operator|.
name|sum
operator|+=
name|b
expr_stmt|;
block|}
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
name|StdAgg
name|myagg
init|=
operator|(
name|StdAgg
operator|)
name|agg
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|count
operator|==
literal|0
condition|)
block|{
comment|// SQL standard - return null for zero elements
return|return
literal|null
return|;
block|}
else|else
block|{
if|if
condition|(
name|myagg
operator|.
name|count
operator|>
literal|1
condition|)
block|{
name|getResult
argument_list|()
operator|.
name|set
argument_list|(
name|myagg
operator|.
name|variance
operator|/
operator|(
name|myagg
operator|.
name|count
operator|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// for one element the variance is always 0
name|getResult
argument_list|()
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
name|getResult
argument_list|()
return|;
block|}
block|}
specifier|public
name|void
name|setResult
parameter_list|(
name|DoubleWritable
name|result
parameter_list|)
block|{
name|this
operator|.
name|result
operator|=
name|result
expr_stmt|;
block|}
specifier|public
name|DoubleWritable
name|getResult
parameter_list|()
block|{
return|return
name|result
return|;
block|}
block|}
block|}
end_class

end_unit

