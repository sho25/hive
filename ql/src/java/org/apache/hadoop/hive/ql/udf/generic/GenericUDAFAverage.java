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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDAFEvaluator
operator|.
name|AggregationBuffer
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
name|io
operator|.
name|HiveDecimalWritable
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
name|HiveDecimalObjectInspector
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
comment|/**  * GenericUDAFAverage.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"avg"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x) - Returns the mean of a set of numbers"
argument_list|)
specifier|public
class|class
name|GenericUDAFAverage
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
name|GenericUDAFAverage
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
case|case
name|TIMESTAMP
case|:
return|return
operator|new
name|GenericUDAFAverageEvaluatorDouble
argument_list|()
return|;
case|case
name|DECIMAL
case|:
return|return
operator|new
name|GenericUDAFAverageEvaluatorDecimal
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
specifier|public
specifier|static
class|class
name|GenericUDAFAverageEvaluatorDouble
extends|extends
name|AbstractGenericUDAFAverageEvaluator
argument_list|<
name|Double
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|doReset
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|Double
argument_list|>
name|aggregation
parameter_list|)
throws|throws
name|HiveException
block|{
name|aggregation
operator|.
name|count
operator|=
literal|0
expr_stmt|;
name|aggregation
operator|.
name|sum
operator|=
operator|new
name|Double
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|ObjectInspector
name|getSumFieldJavaObjectInspector
parameter_list|()
block|{
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|javaDoubleObjectInspector
return|;
block|}
annotation|@
name|Override
specifier|protected
name|ObjectInspector
name|getSumFieldWritableObjectInspector
parameter_list|()
block|{
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doIterate
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|Double
argument_list|>
name|aggregation
parameter_list|,
name|PrimitiveObjectInspector
name|oi
parameter_list|,
name|Object
name|parameter
parameter_list|)
block|{
name|double
name|value
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getDouble
argument_list|(
name|parameter
argument_list|,
name|oi
argument_list|)
decl_stmt|;
name|aggregation
operator|.
name|count
operator|++
expr_stmt|;
name|aggregation
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doMerge
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|Double
argument_list|>
name|aggregation
parameter_list|,
name|Long
name|partialCount
parameter_list|,
name|ObjectInspector
name|sumFieldOI
parameter_list|,
name|Object
name|partialSum
parameter_list|)
block|{
name|double
name|value
init|=
operator|(
operator|(
name|DoubleObjectInspector
operator|)
name|sumFieldOI
operator|)
operator|.
name|get
argument_list|(
name|partialSum
argument_list|)
decl_stmt|;
name|aggregation
operator|.
name|count
operator|+=
name|partialCount
expr_stmt|;
name|aggregation
operator|.
name|sum
operator|+=
name|value
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doTerminatePartial
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|Double
argument_list|>
name|aggregation
parameter_list|)
block|{
if|if
condition|(
name|partialResult
index|[
literal|1
index|]
operator|==
literal|null
condition|)
block|{
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
block|}
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
name|aggregation
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
name|aggregation
operator|.
name|sum
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Object
name|doTerminate
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|Double
argument_list|>
name|aggregation
parameter_list|)
block|{
if|if
condition|(
name|aggregation
operator|.
name|count
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|DoubleWritable
name|result
init|=
operator|new
name|DoubleWritable
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|result
operator|.
name|set
argument_list|(
name|aggregation
operator|.
name|sum
operator|/
name|aggregation
operator|.
name|count
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AggregationBuffer
name|getNewAggregationBuffer
parameter_list|()
throws|throws
name|HiveException
block|{
name|AverageAggregationBuffer
argument_list|<
name|Double
argument_list|>
name|result
init|=
operator|new
name|AverageAggregationBuffer
argument_list|<
name|Double
argument_list|>
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
block|}
specifier|public
specifier|static
class|class
name|GenericUDAFAverageEvaluatorDecimal
extends|extends
name|AbstractGenericUDAFAverageEvaluator
argument_list|<
name|HiveDecimal
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|doReset
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|HiveDecimal
argument_list|>
name|aggregation
parameter_list|)
throws|throws
name|HiveException
block|{
name|aggregation
operator|.
name|count
operator|=
literal|0
expr_stmt|;
name|aggregation
operator|.
name|sum
operator|=
name|HiveDecimal
operator|.
name|ZERO
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|ObjectInspector
name|getSumFieldJavaObjectInspector
parameter_list|()
block|{
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|javaHiveDecimalObjectInspector
return|;
block|}
annotation|@
name|Override
specifier|protected
name|ObjectInspector
name|getSumFieldWritableObjectInspector
parameter_list|()
block|{
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableHiveDecimalObjectInspector
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doIterate
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|HiveDecimal
argument_list|>
name|aggregation
parameter_list|,
name|PrimitiveObjectInspector
name|oi
parameter_list|,
name|Object
name|parameter
parameter_list|)
block|{
name|HiveDecimal
name|value
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveDecimal
argument_list|(
name|parameter
argument_list|,
name|oi
argument_list|)
decl_stmt|;
name|aggregation
operator|.
name|count
operator|++
expr_stmt|;
if|if
condition|(
name|aggregation
operator|.
name|sum
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|aggregation
operator|.
name|sum
operator|=
name|aggregation
operator|.
name|sum
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
name|aggregation
operator|.
name|sum
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doMerge
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|HiveDecimal
argument_list|>
name|aggregation
parameter_list|,
name|Long
name|partialCount
parameter_list|,
name|ObjectInspector
name|sumFieldOI
parameter_list|,
name|Object
name|partialSum
parameter_list|)
block|{
name|HiveDecimal
name|value
init|=
operator|(
operator|(
name|HiveDecimalObjectInspector
operator|)
name|sumFieldOI
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|partialSum
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|aggregation
operator|.
name|sum
operator|=
literal|null
expr_stmt|;
block|}
name|aggregation
operator|.
name|count
operator|+=
name|partialCount
expr_stmt|;
if|if
condition|(
name|aggregation
operator|.
name|sum
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|aggregation
operator|.
name|sum
operator|=
name|aggregation
operator|.
name|sum
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
name|aggregation
operator|.
name|sum
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doTerminatePartial
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|HiveDecimal
argument_list|>
name|aggregation
parameter_list|)
block|{
if|if
condition|(
name|partialResult
index|[
literal|1
index|]
operator|==
literal|null
operator|&&
name|aggregation
operator|.
name|sum
operator|!=
literal|null
condition|)
block|{
name|partialResult
index|[
literal|1
index|]
operator|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|ZERO
argument_list|)
expr_stmt|;
block|}
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
name|aggregation
operator|.
name|count
argument_list|)
expr_stmt|;
if|if
condition|(
name|aggregation
operator|.
name|sum
operator|!=
literal|null
condition|)
block|{
operator|(
operator|(
name|HiveDecimalWritable
operator|)
name|partialResult
index|[
literal|1
index|]
operator|)
operator|.
name|set
argument_list|(
name|aggregation
operator|.
name|sum
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|partialResult
index|[
literal|1
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|Object
name|doTerminate
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|HiveDecimal
argument_list|>
name|aggregation
parameter_list|)
block|{
if|if
condition|(
name|aggregation
operator|.
name|count
operator|==
literal|0
operator|||
name|aggregation
operator|.
name|sum
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|HiveDecimalWritable
name|result
init|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|ZERO
argument_list|)
decl_stmt|;
try|try
block|{
name|result
operator|.
name|set
argument_list|(
name|aggregation
operator|.
name|sum
operator|.
name|divide
argument_list|(
operator|new
name|HiveDecimal
argument_list|(
name|aggregation
operator|.
name|count
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
name|result
operator|=
literal|null
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AggregationBuffer
name|getNewAggregationBuffer
parameter_list|()
throws|throws
name|HiveException
block|{
name|AverageAggregationBuffer
argument_list|<
name|HiveDecimal
argument_list|>
name|result
init|=
operator|new
name|AverageAggregationBuffer
argument_list|<
name|HiveDecimal
argument_list|>
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
block|}
specifier|private
specifier|static
class|class
name|AverageAggregationBuffer
parameter_list|<
name|TYPE
parameter_list|>
implements|implements
name|AggregationBuffer
block|{
specifier|private
name|long
name|count
decl_stmt|;
specifier|private
name|TYPE
name|sum
decl_stmt|;
block|}
empty_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
specifier|abstract
class|class
name|AbstractGenericUDAFAverageEvaluator
parameter_list|<
name|TYPE
parameter_list|>
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
name|LongObjectInspector
name|countFieldOI
decl_stmt|;
specifier|private
name|ObjectInspector
name|sumFieldOI
decl_stmt|;
comment|// For PARTIAL1 and PARTIAL2
specifier|protected
name|Object
index|[]
name|partialResult
decl_stmt|;
specifier|private
name|boolean
name|warned
init|=
literal|false
decl_stmt|;
specifier|protected
specifier|abstract
name|ObjectInspector
name|getSumFieldJavaObjectInspector
parameter_list|()
function_decl|;
specifier|protected
specifier|abstract
name|ObjectInspector
name|getSumFieldWritableObjectInspector
parameter_list|()
function_decl|;
specifier|protected
specifier|abstract
name|void
name|doIterate
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|TYPE
argument_list|>
name|aggregation
parameter_list|,
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|Object
name|parameter
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|void
name|doMerge
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|TYPE
argument_list|>
name|aggregation
parameter_list|,
name|Long
name|partialCount
parameter_list|,
name|ObjectInspector
name|sumFieldOI
parameter_list|,
name|Object
name|partialSum
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|void
name|doTerminatePartial
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|TYPE
argument_list|>
name|aggregation
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|Object
name|doTerminate
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|TYPE
argument_list|>
name|aggregation
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|void
name|doReset
parameter_list|(
name|AverageAggregationBuffer
argument_list|<
name|TYPE
argument_list|>
name|aggregation
parameter_list|)
throws|throws
name|HiveException
function_decl|;
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
name|sumField
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
comment|// a "long" count and a "double" sum.
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
name|getSumFieldWritableObjectInspector
argument_list|()
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
name|partialResult
operator|=
operator|new
name|Object
index|[
literal|2
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
comment|// index 1 set by child
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
return|return
name|getSumFieldWritableObjectInspector
argument_list|()
return|;
block|}
block|}
annotation|@
name|AggregationType
argument_list|(
name|estimable
operator|=
literal|true
argument_list|)
specifier|static
class|class
name|AverageAgg
extends|extends
name|AbstractAggregationBuffer
block|{
name|long
name|count
decl_stmt|;
name|double
name|sum
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|estimate
parameter_list|()
block|{
return|return
name|JavaDataModel
operator|.
name|PRIMITIVES2
operator|*
literal|2
return|;
block|}
block|}
empty_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|(
name|AggregationBuffer
name|aggregation
parameter_list|)
throws|throws
name|HiveException
block|{
name|doReset
argument_list|(
operator|(
name|AverageAggregationBuffer
argument_list|<
name|TYPE
argument_list|>
operator|)
name|aggregation
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|iterate
parameter_list|(
name|AggregationBuffer
name|aggregation
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
name|parameter
init|=
name|parameters
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|parameter
operator|!=
literal|null
condition|)
block|{
name|AverageAggregationBuffer
argument_list|<
name|TYPE
argument_list|>
name|averageAggregation
init|=
operator|(
name|AverageAggregationBuffer
argument_list|<
name|TYPE
argument_list|>
operator|)
name|aggregation
decl_stmt|;
try|try
block|{
name|doIterate
argument_list|(
name|averageAggregation
argument_list|,
name|inputOI
argument_list|,
name|parameter
argument_list|)
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
literal|"Ignoring similar exceptions: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
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
name|aggregation
parameter_list|)
throws|throws
name|HiveException
block|{
name|doTerminatePartial
argument_list|(
operator|(
name|AverageAggregationBuffer
argument_list|<
name|TYPE
argument_list|>
operator|)
name|aggregation
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
name|aggregation
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
name|doMerge
argument_list|(
operator|(
name|AverageAggregationBuffer
argument_list|<
name|TYPE
argument_list|>
operator|)
name|aggregation
argument_list|,
name|countFieldOI
operator|.
name|get
argument_list|(
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|partial
argument_list|,
name|countField
argument_list|)
argument_list|)
argument_list|,
name|sumFieldOI
argument_list|,
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|partial
argument_list|,
name|sumField
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|terminate
parameter_list|(
name|AggregationBuffer
name|aggregation
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|doTerminate
argument_list|(
operator|(
name|AverageAggregationBuffer
argument_list|<
name|TYPE
argument_list|>
operator|)
name|aggregation
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

