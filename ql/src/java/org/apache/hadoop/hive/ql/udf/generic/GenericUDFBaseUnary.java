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
name|ql
operator|.
name|exec
operator|.
name|FunctionRegistry
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
name|HiveIntervalDayTimeWritable
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
name|HiveIntervalYearMonthWritable
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

begin_class
specifier|public
specifier|abstract
class|class
name|GenericUDFBaseUnary
extends|extends
name|GenericUDF
block|{
specifier|protected
name|String
name|opName
decl_stmt|;
specifier|protected
name|String
name|opDisplayName
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
specifier|protected
specifier|transient
name|PrimitiveObjectInspector
name|resultOI
decl_stmt|;
specifier|protected
specifier|transient
name|Converter
name|converter
decl_stmt|;
specifier|protected
name|ByteWritable
name|byteWritable
init|=
operator|new
name|ByteWritable
argument_list|()
decl_stmt|;
specifier|protected
name|ShortWritable
name|shortWritable
init|=
operator|new
name|ShortWritable
argument_list|()
decl_stmt|;
specifier|protected
name|IntWritable
name|intWritable
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
specifier|protected
name|LongWritable
name|longWritable
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
specifier|protected
name|FloatWritable
name|floatWritable
init|=
operator|new
name|FloatWritable
argument_list|()
decl_stmt|;
specifier|protected
name|DoubleWritable
name|doubleWritable
init|=
operator|new
name|DoubleWritable
argument_list|()
decl_stmt|;
specifier|protected
name|HiveDecimalWritable
name|decimalWritable
init|=
operator|new
name|HiveDecimalWritable
argument_list|()
decl_stmt|;
specifier|protected
name|HiveIntervalYearMonthWritable
name|intervalYearMonthWritable
init|=
operator|new
name|HiveIntervalYearMonthWritable
argument_list|()
decl_stmt|;
specifier|protected
name|HiveIntervalDayTimeWritable
name|intervalDayTimeWritable
init|=
operator|new
name|HiveIntervalDayTimeWritable
argument_list|()
decl_stmt|;
specifier|public
name|GenericUDFBaseUnary
parameter_list|()
block|{
name|opName
operator|=
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
expr_stmt|;
block|}
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
name|UDFArgumentException
argument_list|(
name|opName
operator|+
literal|" requires one argument."
argument_list|)
throw|;
block|}
name|Category
name|category
init|=
name|arguments
index|[
literal|0
index|]
operator|.
name|getCategory
argument_list|()
decl_stmt|;
if|if
condition|(
name|category
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
literal|"The "
operator|+
name|GenericUDFUtils
operator|.
name|getOrdinal
argument_list|(
literal|1
argument_list|)
operator|+
literal|" argument of "
operator|+
name|opName
operator|+
literal|"  is expected to a "
operator|+
name|Category
operator|.
name|PRIMITIVE
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|" type, but "
operator|+
name|category
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|" is found"
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
operator|!
name|FunctionRegistry
operator|.
name|isNumericType
argument_list|(
name|inputOI
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
operator|&&
operator|(
name|inputOI
operator|.
name|getTypeInfo
argument_list|()
operator|!=
name|TypeInfoFactory
operator|.
name|intervalDayTimeTypeInfo
operator|)
operator|&&
operator|(
name|inputOI
operator|.
name|getTypeInfo
argument_list|()
operator|!=
name|TypeInfoFactory
operator|.
name|intervalYearMonthTypeInfo
operator|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"The "
operator|+
name|GenericUDFUtils
operator|.
name|getOrdinal
argument_list|(
literal|1
argument_list|)
operator|+
literal|" argument of "
operator|+
name|opName
operator|+
literal|"  is expected to be a "
operator|+
literal|"numeric or interval type, but "
operator|+
name|inputOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" is found"
argument_list|)
throw|;
block|}
name|PrimitiveTypeInfo
name|resultTypeInfo
init|=
name|deriveResultTypeInfo
argument_list|(
name|inputOI
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
decl_stmt|;
name|resultOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|resultTypeInfo
argument_list|)
expr_stmt|;
name|converter
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|inputOI
argument_list|,
name|resultOI
argument_list|)
expr_stmt|;
return|return
name|resultOI
return|;
block|}
specifier|private
name|PrimitiveTypeInfo
name|deriveResultTypeInfo
parameter_list|(
name|PrimitiveTypeInfo
name|typeInfo
parameter_list|)
block|{
switch|switch
condition|(
name|typeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|STRING
case|:
case|case
name|VARCHAR
case|:
case|case
name|CHAR
case|:
return|return
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
return|;
default|default:
return|return
name|typeInfo
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
assert|assert
operator|(
name|children
operator|.
name|length
operator|==
literal|1
operator|)
assert|;
return|return
literal|"("
operator|+
name|opDisplayName
operator|+
literal|" "
operator|+
name|children
index|[
literal|0
index|]
operator|+
literal|")"
return|;
block|}
block|}
end_class

end_unit

