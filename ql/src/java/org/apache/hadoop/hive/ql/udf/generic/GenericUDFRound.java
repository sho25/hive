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
name|UDFArgumentException
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
name|UDFArgumentLengthException
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
name|exec
operator|.
name|vector
operator|.
name|VectorizedExpressions
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
name|FuncRoundWithNumDigitsDecimalToDecimal
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
name|RoundWithNumDigitsDoubleToDouble
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
name|gen
operator|.
name|FuncRoundDecimalToDecimal
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
name|gen
operator|.
name|FuncRoundDoubleToDouble
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
name|ByteWritable
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
name|io
operator|.
name|ShortWritable
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
name|ObjectInspectorConverters
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
name|ObjectInspectorConverters
operator|.
name|Converter
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
name|WritableConstantByteObjectInspector
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
name|WritableConstantIntObjectInspector
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
name|WritableConstantLongObjectInspector
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
name|WritableConstantShortObjectInspector
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
name|DecimalTypeInfo
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
name|TypeInfoFactory
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
name|FloatWritable
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
comment|/**  * Note: rounding function permits rounding off integer digits in decimal numbers, which essentially  * downgrades the scale to negative territory. However, Hive decimal only allow non-negative scales.  * This can cause confusion, for example, when a decimal number 1234.567 of type decimal(7,3) is  * rounded with -2 scale, which produces a decimal number, 1200. The type of the output is  * decimal(5,0), which does not exactly represents what the number means. Thus, user should  * be aware of this, and use negative rounding for decimal with caution. However, Hive is in line  * with the behavior that MYSQL demonstrates.  *  * At a certain point, we should probably support negative scale for decimal type.  *  * GenericUDFRound.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"round"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x[, d]) - round x to d decimal places"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_(12.3456, 1) FROM src LIMIT 1;\n"
operator|+
literal|"  12.3'"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|FuncRoundDoubleToDouble
operator|.
name|class
block|,
name|RoundWithNumDigitsDoubleToDouble
operator|.
name|class
block|,
name|FuncRoundWithNumDigitsDecimalToDecimal
operator|.
name|class
block|,
name|FuncRoundDecimalToDecimal
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|GenericUDFRound
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
specifier|private
name|int
name|scale
init|=
literal|0
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveCategory
name|inputType
decl_stmt|;
specifier|private
specifier|transient
name|Converter
name|converterFromString
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
if|if
condition|(
name|arguments
operator|.
name|length
argument_list|<
literal|1
operator|||
name|arguments
operator|.
name|length
argument_list|>
literal|2
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"ROUND requires one or two argument, got "
operator|+
name|arguments
operator|.
name|length
argument_list|)
throw|;
block|}
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
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
literal|"ROUND input only takes primitive types, got "
operator|+
name|arguments
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
name|inputOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
expr_stmt|;
if|if
condition|(
name|arguments
operator|.
name|length
operator|==
literal|2
condition|)
block|{
if|if
condition|(
name|arguments
index|[
literal|1
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
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
literal|"ROUND second argument only takes primitive types, got "
operator|+
name|arguments
index|[
literal|1
index|]
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
name|PrimitiveObjectInspector
name|scaleOI
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|1
index|]
decl_stmt|;
switch|switch
condition|(
name|scaleOI
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|VOID
case|:
break|break;
case|case
name|BYTE
case|:
if|if
condition|(
operator|!
operator|(
name|scaleOI
operator|instanceof
name|WritableConstantByteObjectInspector
operator|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"ROUND second argument only takes constant"
argument_list|)
throw|;
block|}
name|scale
operator|=
operator|(
operator|(
name|WritableConstantByteObjectInspector
operator|)
name|scaleOI
operator|)
operator|.
name|getWritableConstantValue
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
if|if
condition|(
operator|!
operator|(
name|scaleOI
operator|instanceof
name|WritableConstantShortObjectInspector
operator|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"ROUND second argument only takes constant"
argument_list|)
throw|;
block|}
name|scale
operator|=
operator|(
operator|(
name|WritableConstantShortObjectInspector
operator|)
name|scaleOI
operator|)
operator|.
name|getWritableConstantValue
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
break|break;
case|case
name|INT
case|:
if|if
condition|(
operator|!
operator|(
name|scaleOI
operator|instanceof
name|WritableConstantIntObjectInspector
operator|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"ROUND second argument only takes constant"
argument_list|)
throw|;
block|}
name|scale
operator|=
operator|(
operator|(
name|WritableConstantIntObjectInspector
operator|)
name|scaleOI
operator|)
operator|.
name|getWritableConstantValue
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
break|break;
case|case
name|LONG
case|:
if|if
condition|(
operator|!
operator|(
name|scaleOI
operator|instanceof
name|WritableConstantLongObjectInspector
operator|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"ROUND second argument only takes constant"
argument_list|)
throw|;
block|}
name|long
name|l
init|=
operator|(
operator|(
name|WritableConstantLongObjectInspector
operator|)
name|scaleOI
operator|)
operator|.
name|getWritableConstantValue
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|l
argument_list|<
name|Integer
operator|.
name|MIN_VALUE
operator|||
name|l
argument_list|>
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"ROUND scale argument out of allowed range"
argument_list|)
throw|;
block|}
name|scale
operator|=
operator|(
name|int
operator|)
name|l
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"ROUND second argument only takes integer constant"
argument_list|)
throw|;
block|}
block|}
name|inputType
operator|=
name|inputOI
operator|.
name|getPrimitiveCategory
argument_list|()
expr_stmt|;
name|ObjectInspector
name|outputOI
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|inputType
condition|)
block|{
case|case
name|DECIMAL
case|:
name|DecimalTypeInfo
name|inputTypeInfo
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|inputOI
operator|.
name|getTypeInfo
argument_list|()
decl_stmt|;
name|DecimalTypeInfo
name|typeInfo
init|=
name|getOutputTypeInfo
argument_list|(
name|inputTypeInfo
argument_list|,
name|scale
argument_list|)
decl_stmt|;
name|outputOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
break|break;
case|case
name|VOID
case|:
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
name|outputOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|inputType
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
case|case
name|VARCHAR
case|:
case|case
name|CHAR
case|:
name|outputOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|DOUBLE
argument_list|)
expr_stmt|;
name|converterFromString
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|inputOI
argument_list|,
name|outputOI
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Only numeric or string group data types are allowed for ROUND function. Got "
operator|+
name|inputType
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|outputOI
return|;
block|}
specifier|private
specifier|static
name|DecimalTypeInfo
name|getOutputTypeInfo
parameter_list|(
name|DecimalTypeInfo
name|inputTypeInfo
parameter_list|,
name|int
name|dec
parameter_list|)
block|{
name|int
name|prec
init|=
name|inputTypeInfo
operator|.
name|precision
argument_list|()
decl_stmt|;
name|int
name|scale
init|=
name|inputTypeInfo
operator|.
name|scale
argument_list|()
decl_stmt|;
name|int
name|intParts
init|=
name|prec
operator|-
name|scale
decl_stmt|;
comment|// If we are rounding, we may introduce one more integer digit.
name|int
name|newIntParts
init|=
name|dec
operator|<
name|scale
condition|?
name|intParts
operator|+
literal|1
else|:
name|intParts
decl_stmt|;
name|int
name|newScale
init|=
name|dec
operator|<
literal|0
condition|?
literal|0
else|:
name|Math
operator|.
name|min
argument_list|(
name|dec
argument_list|,
name|HiveDecimal
operator|.
name|MAX_SCALE
argument_list|)
decl_stmt|;
name|int
name|newPrec
init|=
name|Math
operator|.
name|min
argument_list|(
name|newIntParts
operator|+
name|newScale
argument_list|,
name|HiveDecimal
operator|.
name|MAX_PRECISION
argument_list|)
decl_stmt|;
return|return
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
name|newPrec
argument_list|,
name|newScale
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|arguments
operator|.
name|length
operator|==
literal|2
operator|&&
operator|(
name|arguments
index|[
literal|1
index|]
operator|==
literal|null
operator|||
name|arguments
index|[
literal|1
index|]
operator|.
name|get
argument_list|()
operator|==
literal|null
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Object
name|input
init|=
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
switch|switch
condition|(
name|inputType
condition|)
block|{
case|case
name|VOID
case|:
return|return
literal|null
return|;
case|case
name|DECIMAL
case|:
name|HiveDecimalWritable
name|decimalWritable
init|=
operator|(
name|HiveDecimalWritable
operator|)
name|inputOI
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|HiveDecimal
name|dec
init|=
name|RoundUtils
operator|.
name|round
argument_list|(
name|decimalWritable
operator|.
name|getHiveDecimal
argument_list|()
argument_list|,
name|scale
argument_list|)
decl_stmt|;
if|if
condition|(
name|dec
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
name|HiveDecimalWritable
argument_list|(
name|dec
argument_list|)
return|;
case|case
name|BYTE
case|:
name|ByteWritable
name|byteWritable
init|=
operator|(
name|ByteWritable
operator|)
name|inputOI
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
decl_stmt|;
if|if
condition|(
name|scale
operator|>=
literal|0
condition|)
block|{
return|return
name|byteWritable
return|;
block|}
else|else
block|{
return|return
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
name|RoundUtils
operator|.
name|round
argument_list|(
name|byteWritable
operator|.
name|get
argument_list|()
argument_list|,
name|scale
argument_list|)
argument_list|)
return|;
block|}
case|case
name|SHORT
case|:
name|ShortWritable
name|shortWritable
init|=
operator|(
name|ShortWritable
operator|)
name|inputOI
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
decl_stmt|;
if|if
condition|(
name|scale
operator|>=
literal|0
condition|)
block|{
return|return
name|shortWritable
return|;
block|}
else|else
block|{
return|return
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
name|RoundUtils
operator|.
name|round
argument_list|(
name|shortWritable
operator|.
name|get
argument_list|()
argument_list|,
name|scale
argument_list|)
argument_list|)
return|;
block|}
case|case
name|INT
case|:
name|IntWritable
name|intWritable
init|=
operator|(
name|IntWritable
operator|)
name|inputOI
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
decl_stmt|;
if|if
condition|(
name|scale
operator|>=
literal|0
condition|)
block|{
return|return
name|intWritable
return|;
block|}
else|else
block|{
return|return
operator|new
name|IntWritable
argument_list|(
operator|(
name|int
operator|)
name|RoundUtils
operator|.
name|round
argument_list|(
name|intWritable
operator|.
name|get
argument_list|()
argument_list|,
name|scale
argument_list|)
argument_list|)
return|;
block|}
case|case
name|LONG
case|:
name|LongWritable
name|longWritable
init|=
operator|(
name|LongWritable
operator|)
name|inputOI
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
decl_stmt|;
if|if
condition|(
name|scale
operator|>=
literal|0
condition|)
block|{
return|return
name|longWritable
return|;
block|}
else|else
block|{
return|return
operator|new
name|LongWritable
argument_list|(
name|RoundUtils
operator|.
name|round
argument_list|(
name|longWritable
operator|.
name|get
argument_list|()
argument_list|,
name|scale
argument_list|)
argument_list|)
return|;
block|}
case|case
name|FLOAT
case|:
name|float
name|f
init|=
operator|(
operator|(
name|FloatWritable
operator|)
name|inputOI
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
operator|)
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
operator|new
name|FloatWritable
argument_list|(
operator|(
name|float
operator|)
name|RoundUtils
operator|.
name|round
argument_list|(
name|f
argument_list|,
name|scale
argument_list|)
argument_list|)
return|;
case|case
name|DOUBLE
case|:
return|return
name|round
argument_list|(
operator|(
operator|(
name|DoubleWritable
operator|)
name|inputOI
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
operator|)
argument_list|,
name|scale
argument_list|)
return|;
case|case
name|STRING
case|:
case|case
name|VARCHAR
case|:
case|case
name|CHAR
case|:
name|DoubleWritable
name|doubleValue
init|=
operator|(
name|DoubleWritable
operator|)
name|converterFromString
operator|.
name|convert
argument_list|(
name|input
argument_list|)
decl_stmt|;
if|if
condition|(
name|doubleValue
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|round
argument_list|(
name|doubleValue
argument_list|,
name|scale
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Only numeric or string group data types are allowed for ROUND function. Got "
operator|+
name|inputType
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|DoubleWritable
name|round
parameter_list|(
name|DoubleWritable
name|input
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
name|double
name|d
init|=
name|input
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|Double
operator|.
name|isNaN
argument_list|(
name|d
argument_list|)
operator|||
name|Double
operator|.
name|isInfinite
argument_list|(
name|d
argument_list|)
condition|)
block|{
return|return
operator|new
name|DoubleWritable
argument_list|(
name|d
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|DoubleWritable
argument_list|(
name|RoundUtils
operator|.
name|round
argument_list|(
name|d
argument_list|,
name|scale
argument_list|)
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"round("
argument_list|)
expr_stmt|;
if|if
condition|(
name|children
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|children
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|children
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|children
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

