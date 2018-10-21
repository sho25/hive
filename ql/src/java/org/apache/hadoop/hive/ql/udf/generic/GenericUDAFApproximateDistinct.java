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
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutputStream
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
name|ndv
operator|.
name|hll
operator|.
name|HyperLogLog
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
name|ndv
operator|.
name|hll
operator|.
name|HyperLogLogUtils
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
name|Date
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
name|HiveBaseChar
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
name|HiveChar
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
name|common
operator|.
name|type
operator|.
name|HiveIntervalDayTime
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
name|HiveVarchar
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
name|Timestamp
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
name|AbstractAggregationBuffer
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
name|WritableBinaryObjectInspector
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
name|BytesWritable
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

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"approx_distinct"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x) - generate an approximate distinct from input column"
argument_list|)
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
class|class
name|GenericUDAFApproximateDistinct
extends|extends
name|AbstractGenericUDAFResolver
block|{
specifier|static
specifier|final
class|class
name|HyperLogLogBuffer
extends|extends
name|AbstractAggregationBuffer
block|{
specifier|public
name|HyperLogLog
name|hll
decl_stmt|;
specifier|public
name|HyperLogLogBuffer
parameter_list|()
block|{
name|this
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|estimate
parameter_list|()
block|{
return|return
literal|4096
return|;
comment|/* 4kb usually */
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|hll
operator|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|12
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|HyperLogLogEvaluator
extends|extends
name|GenericUDAFEvaluator
block|{
name|ObjectInspector
name|inputOI
decl_stmt|;
name|WritableBinaryObjectInspector
name|partialOI
decl_stmt|;
name|ByteArrayOutputStream
name|output
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
comment|/*      * All modes returns BINARY columns.      *       * PARTIAL1 takes in a primitive inspector      *       * @see org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator#init(org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator.Mode, org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector[])      */
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
name|partialOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBinaryObjectInspector
expr_stmt|;
switch|switch
condition|(
name|m
condition|)
block|{
case|case
name|PARTIAL1
case|:
name|inputOI
operator|=
name|parameters
index|[
literal|0
index|]
expr_stmt|;
return|return
name|partialOI
return|;
case|case
name|PARTIAL2
case|:
return|return
name|partialOI
return|;
case|case
name|FINAL
case|:
case|case
name|COMPLETE
case|:
return|return
name|partialOI
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown UDAF mode "
operator|+
name|m
argument_list|)
throw|;
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
return|return
operator|new
name|HyperLogLogBuffer
argument_list|()
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
name|args
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|args
index|[
literal|0
index|]
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|HyperLogLog
name|hll
init|=
operator|(
operator|(
name|HyperLogLogBuffer
operator|)
name|agg
operator|)
operator|.
name|hll
decl_stmt|;
comment|// should use BinarySortableSerDe, perhaps
name|Object
name|val
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardJavaObject
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|,
name|inputOI
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|val
operator|instanceof
name|Byte
operator|||
name|val
operator|instanceof
name|Character
operator|||
name|val
operator|instanceof
name|Short
condition|)
block|{
name|hll
operator|.
name|add
argument_list|(
name|val
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|Integer
condition|)
block|{
name|hll
operator|.
name|addInt
argument_list|(
operator|(
operator|(
name|Integer
operator|)
name|val
operator|)
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|Long
condition|)
block|{
name|hll
operator|.
name|addLong
argument_list|(
operator|(
operator|(
name|Long
operator|)
name|val
operator|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|Float
condition|)
block|{
name|hll
operator|.
name|addFloat
argument_list|(
operator|(
operator|(
name|Float
operator|)
name|val
operator|)
operator|.
name|floatValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|Double
condition|)
block|{
name|hll
operator|.
name|addDouble
argument_list|(
operator|(
name|Double
operator|)
name|val
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|String
condition|)
block|{
name|hll
operator|.
name|addString
argument_list|(
name|val
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|HiveDecimal
condition|)
block|{
name|hll
operator|.
name|addToEstimator
argument_list|(
operator|(
name|HiveDecimal
operator|)
name|val
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|Date
condition|)
block|{
name|hll
operator|.
name|addInt
argument_list|(
operator|(
operator|(
name|Date
operator|)
name|val
operator|)
operator|.
name|toEpochDay
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|Timestamp
condition|)
block|{
name|hll
operator|.
name|addLong
argument_list|(
operator|(
operator|(
name|Timestamp
operator|)
name|val
operator|)
operator|.
name|toEpochMilli
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|HiveIntervalDayTime
condition|)
block|{
name|hll
operator|.
name|addLong
argument_list|(
operator|(
operator|(
name|HiveIntervalDayTime
operator|)
name|val
operator|)
operator|.
name|getTotalSeconds
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|HiveBaseChar
condition|)
block|{
name|hll
operator|.
name|addString
argument_list|(
operator|(
operator|(
name|HiveBaseChar
operator|)
name|val
operator|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|/* potential multi-key option (does this ever get used?) */
name|output
operator|.
name|reset
argument_list|()
expr_stmt|;
name|ObjectOutputStream
name|out
init|=
operator|new
name|ObjectOutputStream
argument_list|(
name|output
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeObject
argument_list|(
name|val
argument_list|)
expr_stmt|;
name|byte
index|[]
name|key
init|=
name|output
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|hll
operator|.
name|addBytes
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ioe
argument_list|)
throw|;
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
name|HyperLogLog
name|hll
init|=
operator|(
operator|(
name|HyperLogLogBuffer
operator|)
name|agg
operator|)
operator|.
name|hll
decl_stmt|;
name|output
operator|.
name|reset
argument_list|()
expr_stmt|;
try|try
block|{
name|HyperLogLogUtils
operator|.
name|serializeHLL
argument_list|(
name|output
argument_list|,
name|hll
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
return|return
operator|new
name|BytesWritable
argument_list|(
name|output
operator|.
name|toByteArray
argument_list|()
argument_list|)
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
operator|==
literal|null
condition|)
block|{
return|return;
block|}
specifier|final
name|BytesWritable
name|bw
init|=
name|partialOI
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|partial
argument_list|)
decl_stmt|;
name|HyperLogLog
name|hll
init|=
operator|(
operator|(
name|HyperLogLogBuffer
operator|)
name|agg
operator|)
operator|.
name|hll
decl_stmt|;
name|merge
argument_list|(
name|hll
argument_list|,
name|bw
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|merge
parameter_list|(
name|HyperLogLog
name|hll
parameter_list|,
name|BytesWritable
name|bw
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|ByteArrayInputStream
name|input
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|bw
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bw
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
name|HyperLogLog
name|hll2
init|=
name|HyperLogLogUtils
operator|.
name|deserializeHLL
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|hll
operator|.
name|merge
argument_list|(
name|hll2
argument_list|)
expr_stmt|;
name|input
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
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
operator|(
operator|(
name|HyperLogLogBuffer
operator|)
name|agg
operator|)
operator|.
name|reset
argument_list|()
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
name|HyperLogLog
name|hll
init|=
operator|(
operator|(
name|HyperLogLogBuffer
operator|)
name|agg
operator|)
operator|.
name|hll
decl_stmt|;
name|output
operator|.
name|reset
argument_list|()
expr_stmt|;
try|try
block|{
name|HyperLogLogUtils
operator|.
name|serializeHLL
argument_list|(
name|output
argument_list|,
name|hll
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
return|return
operator|new
name|BytesWritable
argument_list|(
name|output
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
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
return|return
name|getEvaluator
argument_list|(
name|info
operator|.
name|getParameters
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
specifier|final
class|class
name|CountApproximateDistinctEvaluator
extends|extends
name|HyperLogLogEvaluator
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
name|ObjectInspector
name|hyperloglog
init|=
name|super
operator|.
name|init
argument_list|(
name|m
argument_list|,
name|parameters
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|==
name|Mode
operator|.
name|FINAL
operator|||
name|m
operator|==
name|Mode
operator|.
name|COMPLETE
condition|)
block|{
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
return|;
block|}
return|return
name|hyperloglog
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
name|HyperLogLog
name|hll
init|=
operator|(
operator|(
name|HyperLogLogBuffer
operator|)
name|agg
operator|)
operator|.
name|hll
decl_stmt|;
return|return
operator|new
name|LongWritable
argument_list|(
name|hll
operator|.
name|count
argument_list|()
argument_list|)
return|;
block|}
block|}
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
name|IllegalArgumentException
argument_list|(
literal|"Function only takes 1 parameter"
argument_list|)
throw|;
block|}
elseif|else
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
operator|&&
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
name|STRUCT
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"Only primitive/struct rows are accepted but "
operator|+
name|parameters
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed."
argument_list|)
throw|;
block|}
return|return
operator|new
name|CountApproximateDistinctEvaluator
argument_list|()
return|;
block|}
block|}
end_class

end_unit

