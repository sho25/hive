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
name|gen
operator|.
name|FuncAbsDecimalToDecimal
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
name|FuncAbsDoubleToDouble
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
name|FuncAbsLongToLong
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
comment|/**  * GenericUDFAbs.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"abs"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x) - returns the absolute value of x"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_(0) FROM src LIMIT 1;\n"
operator|+
literal|"  0\n"
operator|+
literal|"> SELECT _FUNC_(-5) FROM src LIMIT 1;\n"
operator|+
literal|"  5"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|FuncAbsLongToLong
operator|.
name|class
block|,
name|FuncAbsDoubleToDouble
operator|.
name|class
block|,
name|FuncAbsDecimalToDecimal
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|GenericUDFAbs
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|PrimitiveCategory
name|inputType
decl_stmt|;
specifier|private
specifier|final
name|DoubleWritable
name|resultDouble
init|=
operator|new
name|DoubleWritable
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongWritable
name|resultLong
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|IntWritable
name|resultInt
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|HiveDecimalWritable
name|resultDecimal
init|=
operator|new
name|HiveDecimalWritable
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveObjectInspector
name|argumentOI
decl_stmt|;
specifier|private
specifier|transient
name|Converter
name|inputConverter
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
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"ABS() requires 1 argument, got "
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
name|UDFArgumentException
argument_list|(
literal|"ABS only takes primitive types, got "
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
name|argumentOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
expr_stmt|;
name|inputType
operator|=
name|argumentOI
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
name|SHORT
case|:
case|case
name|BYTE
case|:
case|case
name|INT
case|:
name|inputConverter
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|arguments
index|[
literal|0
index|]
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
argument_list|)
expr_stmt|;
name|outputOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|inputConverter
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|arguments
index|[
literal|0
index|]
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
argument_list|)
expr_stmt|;
name|outputOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
case|case
name|STRING
case|:
case|case
name|DOUBLE
case|:
name|inputConverter
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|arguments
index|[
literal|0
index|]
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
argument_list|)
expr_stmt|;
name|outputOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|outputOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
operator|)
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|inputConverter
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|arguments
index|[
literal|0
index|]
argument_list|,
name|outputOI
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"ABS only takes SHORT/BYTE/INT/LONG/DOUBLE/FLOAT/STRING/DECIMAL types, got "
operator|+
name|inputType
argument_list|)
throw|;
block|}
return|return
name|outputOI
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
name|Object
name|valObject
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
name|valObject
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
name|SHORT
case|:
case|case
name|BYTE
case|:
case|case
name|INT
case|:
name|valObject
operator|=
name|inputConverter
operator|.
name|convert
argument_list|(
name|valObject
argument_list|)
expr_stmt|;
name|resultInt
operator|.
name|set
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
operator|(
operator|(
name|IntWritable
operator|)
name|valObject
operator|)
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|resultInt
return|;
case|case
name|LONG
case|:
name|valObject
operator|=
name|inputConverter
operator|.
name|convert
argument_list|(
name|valObject
argument_list|)
expr_stmt|;
name|resultLong
operator|.
name|set
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
operator|(
operator|(
name|LongWritable
operator|)
name|valObject
operator|)
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|resultLong
return|;
case|case
name|FLOAT
case|:
case|case
name|STRING
case|:
case|case
name|DOUBLE
case|:
name|valObject
operator|=
name|inputConverter
operator|.
name|convert
argument_list|(
name|valObject
argument_list|)
expr_stmt|;
name|resultDouble
operator|.
name|set
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
operator|(
operator|(
name|DoubleWritable
operator|)
name|valObject
operator|)
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|resultDouble
return|;
case|case
name|DECIMAL
case|:
name|HiveDecimalObjectInspector
name|decimalOI
init|=
operator|(
name|HiveDecimalObjectInspector
operator|)
name|argumentOI
decl_stmt|;
name|HiveDecimalWritable
name|val
init|=
name|decimalOI
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|valObject
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|!=
literal|null
condition|)
block|{
name|resultDecimal
operator|.
name|set
argument_list|(
name|val
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|abs
argument_list|()
argument_list|)
expr_stmt|;
name|val
operator|=
name|resultDecimal
expr_stmt|;
block|}
return|return
name|val
return|;
default|default:
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"ABS only takes SHORT/BYTE/INT/LONG/DOUBLE/FLOAT/STRING/DECIMAL types, got "
operator|+
name|inputType
argument_list|)
throw|;
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
return|return
name|getStandardDisplayString
argument_list|(
literal|"abs"
argument_list|,
name|children
argument_list|)
return|;
block|}
block|}
end_class

end_unit

