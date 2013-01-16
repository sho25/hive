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

begin_comment
comment|/**  * Compute the Pearson correlation coefficient corr(x, y), using the following  * stable one-pass method, based on:  * "Formulas for Robust, One-Pass Parallel Computation of Covariances and  * Arbitrary-Order Statistical Moments", Philippe Pebay, Sandia Labs  * and "The Art of Computer Programming, volume 2: Seminumerical Algorithms",  * Donald Knuth.  *  *  Incremental:  *   n :<count>  *   mx_n = mx_(n-1) + [x_n - mx_(n-1)]/n :<xavg>  *   my_n = my_(n-1) + [y_n - my_(n-1)]/n :<yavg>  *   c_n = c_(n-1) + (x_n - mx_(n-1))*(y_n - my_n) :<covariance * n>  *   vx_n = vx_(n-1) + (x_n - mx_n)(x_n - mx_(n-1)):<variance * n>  *   vy_n = vy_(n-1) + (y_n - my_n)(y_n - my_(n-1)):<variance * n>  *  *  Merge:  *   c_(A,B) = c_A + c_B + (mx_A - mx_B)*(my_A - my_B)*n_A*n_B/(n_A+n_B)  *   vx_(A,B) = vx_A + vx_B + (mx_A - mx_B)*(mx_A - mx_B)*n_A*n_B/(n_A+n_B)  *   vy_(A,B) = vy_A + vy_B + (my_A - my_B)*(my_A - my_B)*n_A*n_B/(n_A+n_B)  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"corr"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x,y) - Returns the Pearson coefficient of correlation\n"
operator|+
literal|"between a set of number pairs"
argument_list|,
name|extended
operator|=
literal|"The function takes as arguments any pair of numeric types and returns a double.\n"
operator|+
literal|"Any pair with a NULL is ignored. If the function is applied to an empty set or\n"
operator|+
literal|"a singleton set, NULL will be returned. Otherwise, it computes the following:\n"
operator|+
literal|"   COVAR_POP(x,y)/(STDDEV_POP(x)*STDDEV_POP(y))\n"
operator|+
literal|"where neither x nor y is null,\n"
operator|+
literal|"COVAR_POP is the population covariance,\n"
operator|+
literal|"and STDDEV_POP is the population standard deviation."
argument_list|)
specifier|public
class|class
name|GenericUDAFCorrelation
extends|extends
name|AbstractGenericUDAFResolver
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
name|GenericUDAFCorrelation
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
literal|2
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
literal|"Exactly two arguments are expected."
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
if|if
condition|(
name|parameters
index|[
literal|1
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
literal|1
argument_list|,
literal|"Only primitive type arguments are accepted but "
operator|+
name|parameters
index|[
literal|1
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
name|TIMESTAMP
case|:
case|case
name|DECIMAL
case|:
switch|switch
condition|(
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|parameters
index|[
literal|1
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
return|return
operator|new
name|GenericUDAFCorrelationEvaluator
argument_list|()
return|;
case|case
name|STRING
case|:
case|case
name|BOOLEAN
case|:
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"Only numeric type arguments are accepted but "
operator|+
name|parameters
index|[
literal|1
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" is passed."
argument_list|)
throw|;
block|}
case|case
name|STRING
case|:
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
literal|" is passed."
argument_list|)
throw|;
block|}
block|}
comment|/**    * Evaluate the Pearson correlation coefficient using a stable one-pass    * algorithm, based on work by Philippe Pébay and Donald Knuth.    *    *  Incremental:    *   n :<count>    *   mx_n = mx_(n-1) + [x_n - mx_(n-1)]/n :<xavg>    *   my_n = my_(n-1) + [y_n - my_(n-1)]/n :<yavg>    *   c_n = c_(n-1) + (x_n - mx_(n-1))*(y_n - my_n) :<covariance * n>    *   vx_n = vx_(n-1) + (x_n - mx_n)(x_n - mx_(n-1)):<variance * n>    *   vy_n = vy_(n-1) + (y_n - my_n)(y_n - my_(n-1)):<variance * n>    *    *  Merge:    *   c_X = c_A + c_B + (mx_A - mx_B)*(my_A - my_B)*n_A*n_B/n_X    *   vx_(A,B) = vx_A + vx_B + (mx_A - mx_B)*(mx_A - mx_B)*n_A*n_B/(n_A+n_B)    *   vy_(A,B) = vy_A + vy_B + (my_A - my_B)*(my_A - my_B)*n_A*n_B/(n_A+n_B)    *    */
specifier|public
specifier|static
class|class
name|GenericUDAFCorrelationEvaluator
extends|extends
name|GenericUDAFEvaluator
block|{
comment|// For PARTIAL1 and COMPLETE
specifier|private
name|PrimitiveObjectInspector
name|xInputOI
decl_stmt|;
specifier|private
name|PrimitiveObjectInspector
name|yInputOI
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
name|xavgField
decl_stmt|;
specifier|private
name|StructField
name|yavgField
decl_stmt|;
specifier|private
name|StructField
name|xvarField
decl_stmt|;
specifier|private
name|StructField
name|yvarField
decl_stmt|;
specifier|private
name|StructField
name|covarField
decl_stmt|;
specifier|private
name|LongObjectInspector
name|countFieldOI
decl_stmt|;
specifier|private
name|DoubleObjectInspector
name|xavgFieldOI
decl_stmt|;
specifier|private
name|DoubleObjectInspector
name|yavgFieldOI
decl_stmt|;
specifier|private
name|DoubleObjectInspector
name|xvarFieldOI
decl_stmt|;
specifier|private
name|DoubleObjectInspector
name|yvarFieldOI
decl_stmt|;
specifier|private
name|DoubleObjectInspector
name|covarFieldOI
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
assert|assert
operator|(
name|parameters
operator|.
name|length
operator|==
literal|2
operator|)
assert|;
name|xInputOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|parameters
index|[
literal|0
index|]
expr_stmt|;
name|yInputOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|parameters
index|[
literal|1
index|]
expr_stmt|;
block|}
else|else
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
name|xavgField
operator|=
name|soi
operator|.
name|getStructFieldRef
argument_list|(
literal|"xavg"
argument_list|)
expr_stmt|;
name|yavgField
operator|=
name|soi
operator|.
name|getStructFieldRef
argument_list|(
literal|"yavg"
argument_list|)
expr_stmt|;
name|xvarField
operator|=
name|soi
operator|.
name|getStructFieldRef
argument_list|(
literal|"xvar"
argument_list|)
expr_stmt|;
name|yvarField
operator|=
name|soi
operator|.
name|getStructFieldRef
argument_list|(
literal|"yvar"
argument_list|)
expr_stmt|;
name|covarField
operator|=
name|soi
operator|.
name|getStructFieldRef
argument_list|(
literal|"covar"
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
name|xavgFieldOI
operator|=
operator|(
name|DoubleObjectInspector
operator|)
name|xavgField
operator|.
name|getFieldObjectInspector
argument_list|()
expr_stmt|;
name|yavgFieldOI
operator|=
operator|(
name|DoubleObjectInspector
operator|)
name|yavgField
operator|.
name|getFieldObjectInspector
argument_list|()
expr_stmt|;
name|xvarFieldOI
operator|=
operator|(
name|DoubleObjectInspector
operator|)
name|xvarField
operator|.
name|getFieldObjectInspector
argument_list|()
expr_stmt|;
name|yvarFieldOI
operator|=
operator|(
name|DoubleObjectInspector
operator|)
name|yvarField
operator|.
name|getFieldObjectInspector
argument_list|()
expr_stmt|;
name|covarFieldOI
operator|=
operator|(
name|DoubleObjectInspector
operator|)
name|covarField
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
comment|// a long count, two double averages, two double variances,
comment|// and a double covariance.
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
literal|"xavg"
argument_list|)
expr_stmt|;
name|fname
operator|.
name|add
argument_list|(
literal|"yavg"
argument_list|)
expr_stmt|;
name|fname
operator|.
name|add
argument_list|(
literal|"xvar"
argument_list|)
expr_stmt|;
name|fname
operator|.
name|add
argument_list|(
literal|"yvar"
argument_list|)
expr_stmt|;
name|fname
operator|.
name|add
argument_list|(
literal|"covar"
argument_list|)
expr_stmt|;
name|partialResult
operator|=
operator|new
name|Object
index|[
literal|6
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
name|partialResult
index|[
literal|3
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
literal|4
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
literal|5
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
comment|// number n of elements
name|double
name|xavg
decl_stmt|;
comment|// average of x elements
name|double
name|yavg
decl_stmt|;
comment|// average of y elements
name|double
name|xvar
decl_stmt|;
comment|// n times the variance of x elements
name|double
name|yvar
decl_stmt|;
comment|// n times the variance of y elements
name|double
name|covar
decl_stmt|;
comment|// n times the covariance
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
name|xavg
operator|=
literal|0
expr_stmt|;
name|myagg
operator|.
name|yavg
operator|=
literal|0
expr_stmt|;
name|myagg
operator|.
name|xvar
operator|=
literal|0
expr_stmt|;
name|myagg
operator|.
name|yvar
operator|=
literal|0
expr_stmt|;
name|myagg
operator|.
name|covar
operator|=
literal|0
expr_stmt|;
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
operator|)
assert|;
name|Object
name|px
init|=
name|parameters
index|[
literal|0
index|]
decl_stmt|;
name|Object
name|py
init|=
name|parameters
index|[
literal|1
index|]
decl_stmt|;
if|if
condition|(
name|px
operator|!=
literal|null
operator|&&
name|py
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
name|double
name|vx
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getDouble
argument_list|(
name|px
argument_list|,
name|xInputOI
argument_list|)
decl_stmt|;
name|double
name|vy
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getDouble
argument_list|(
name|py
argument_list|,
name|yInputOI
argument_list|)
decl_stmt|;
name|double
name|xavgOld
init|=
name|myagg
operator|.
name|xavg
decl_stmt|;
name|double
name|yavgOld
init|=
name|myagg
operator|.
name|yavg
decl_stmt|;
name|myagg
operator|.
name|count
operator|++
expr_stmt|;
name|myagg
operator|.
name|xavg
operator|+=
operator|(
name|vx
operator|-
name|xavgOld
operator|)
operator|/
name|myagg
operator|.
name|count
expr_stmt|;
name|myagg
operator|.
name|yavg
operator|+=
operator|(
name|vy
operator|-
name|yavgOld
operator|)
operator|/
name|myagg
operator|.
name|count
expr_stmt|;
if|if
condition|(
name|myagg
operator|.
name|count
operator|>
literal|1
condition|)
block|{
name|myagg
operator|.
name|covar
operator|+=
operator|(
name|vx
operator|-
name|xavgOld
operator|)
operator|*
operator|(
name|vy
operator|-
name|myagg
operator|.
name|yavg
operator|)
expr_stmt|;
name|myagg
operator|.
name|xvar
operator|+=
operator|(
name|vx
operator|-
name|xavgOld
operator|)
operator|*
operator|(
name|vx
operator|-
name|myagg
operator|.
name|xavg
operator|)
expr_stmt|;
name|myagg
operator|.
name|yvar
operator|+=
operator|(
name|vy
operator|-
name|yavgOld
operator|)
operator|*
operator|(
name|vy
operator|-
name|myagg
operator|.
name|yavg
operator|)
expr_stmt|;
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
name|xavg
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
name|yavg
argument_list|)
expr_stmt|;
operator|(
operator|(
name|DoubleWritable
operator|)
name|partialResult
index|[
literal|3
index|]
operator|)
operator|.
name|set
argument_list|(
name|myagg
operator|.
name|xvar
argument_list|)
expr_stmt|;
operator|(
operator|(
name|DoubleWritable
operator|)
name|partialResult
index|[
literal|4
index|]
operator|)
operator|.
name|set
argument_list|(
name|myagg
operator|.
name|yvar
argument_list|)
expr_stmt|;
operator|(
operator|(
name|DoubleWritable
operator|)
name|partialResult
index|[
literal|5
index|]
operator|)
operator|.
name|set
argument_list|(
name|myagg
operator|.
name|covar
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
name|partialXAvg
init|=
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|partial
argument_list|,
name|xavgField
argument_list|)
decl_stmt|;
name|Object
name|partialYAvg
init|=
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|partial
argument_list|,
name|yavgField
argument_list|)
decl_stmt|;
name|Object
name|partialXVar
init|=
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|partial
argument_list|,
name|xvarField
argument_list|)
decl_stmt|;
name|Object
name|partialYVar
init|=
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|partial
argument_list|,
name|yvarField
argument_list|)
decl_stmt|;
name|Object
name|partialCovar
init|=
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|partial
argument_list|,
name|covarField
argument_list|)
decl_stmt|;
name|long
name|nA
init|=
name|myagg
operator|.
name|count
decl_stmt|;
name|long
name|nB
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
name|nA
operator|==
literal|0
condition|)
block|{
comment|// Just copy the information since there is nothing so far
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
name|xavg
operator|=
name|xavgFieldOI
operator|.
name|get
argument_list|(
name|partialXAvg
argument_list|)
expr_stmt|;
name|myagg
operator|.
name|yavg
operator|=
name|yavgFieldOI
operator|.
name|get
argument_list|(
name|partialYAvg
argument_list|)
expr_stmt|;
name|myagg
operator|.
name|xvar
operator|=
name|xvarFieldOI
operator|.
name|get
argument_list|(
name|partialXVar
argument_list|)
expr_stmt|;
name|myagg
operator|.
name|yvar
operator|=
name|yvarFieldOI
operator|.
name|get
argument_list|(
name|partialYVar
argument_list|)
expr_stmt|;
name|myagg
operator|.
name|covar
operator|=
name|covarFieldOI
operator|.
name|get
argument_list|(
name|partialCovar
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nA
operator|!=
literal|0
operator|&&
name|nB
operator|!=
literal|0
condition|)
block|{
comment|// Merge the two partials
name|double
name|xavgA
init|=
name|myagg
operator|.
name|xavg
decl_stmt|;
name|double
name|yavgA
init|=
name|myagg
operator|.
name|yavg
decl_stmt|;
name|double
name|xavgB
init|=
name|xavgFieldOI
operator|.
name|get
argument_list|(
name|partialXAvg
argument_list|)
decl_stmt|;
name|double
name|yavgB
init|=
name|yavgFieldOI
operator|.
name|get
argument_list|(
name|partialYAvg
argument_list|)
decl_stmt|;
name|double
name|xvarB
init|=
name|xvarFieldOI
operator|.
name|get
argument_list|(
name|partialXVar
argument_list|)
decl_stmt|;
name|double
name|yvarB
init|=
name|yvarFieldOI
operator|.
name|get
argument_list|(
name|partialYVar
argument_list|)
decl_stmt|;
name|double
name|covarB
init|=
name|covarFieldOI
operator|.
name|get
argument_list|(
name|partialCovar
argument_list|)
decl_stmt|;
name|myagg
operator|.
name|count
operator|+=
name|nB
expr_stmt|;
name|myagg
operator|.
name|xavg
operator|=
operator|(
name|xavgA
operator|*
name|nA
operator|+
name|xavgB
operator|*
name|nB
operator|)
operator|/
name|myagg
operator|.
name|count
expr_stmt|;
name|myagg
operator|.
name|yavg
operator|=
operator|(
name|yavgA
operator|*
name|nA
operator|+
name|yavgB
operator|*
name|nB
operator|)
operator|/
name|myagg
operator|.
name|count
expr_stmt|;
name|myagg
operator|.
name|xvar
operator|+=
name|xvarB
operator|+
operator|(
name|xavgA
operator|-
name|xavgB
operator|)
operator|*
operator|(
name|xavgA
operator|-
name|xavgB
operator|)
operator|*
name|myagg
operator|.
name|count
expr_stmt|;
name|myagg
operator|.
name|yvar
operator|+=
name|yvarB
operator|+
operator|(
name|yavgA
operator|-
name|yavgB
operator|)
operator|*
operator|(
name|yavgA
operator|-
name|yavgB
operator|)
operator|*
name|myagg
operator|.
name|count
expr_stmt|;
name|myagg
operator|.
name|covar
operator|+=
name|covarB
operator|+
operator|(
name|xavgA
operator|-
name|xavgB
operator|)
operator|*
operator|(
name|yavgA
operator|-
name|yavgB
operator|)
operator|*
operator|(
call|(
name|double
call|)
argument_list|(
name|nA
operator|*
name|nB
argument_list|)
operator|/
name|myagg
operator|.
name|count
operator|)
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
operator|<
literal|2
condition|)
block|{
comment|// SQL standard - return null for zero or one pair
return|return
literal|null
return|;
block|}
else|else
block|{
name|getResult
argument_list|()
operator|.
name|set
argument_list|(
name|myagg
operator|.
name|covar
operator|/
name|java
operator|.
name|lang
operator|.
name|Math
operator|.
name|sqrt
argument_list|(
name|myagg
operator|.
name|xvar
argument_list|)
operator|/
name|java
operator|.
name|lang
operator|.
name|Math
operator|.
name|sqrt
argument_list|(
name|myagg
operator|.
name|yvar
argument_list|)
argument_list|)
expr_stmt|;
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

