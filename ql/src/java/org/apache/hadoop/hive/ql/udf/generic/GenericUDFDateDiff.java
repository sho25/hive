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
name|sql
operator|.
name|Timestamp
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|ParseException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|SimpleDateFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TimeZone
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
name|VectorUDFDateDiffColCol
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
name|VectorUDFDateDiffColScalar
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
name|VectorUDFDateDiffScalarCol
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
name|DateWritable
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
name|TimestampWritable
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
name|PrimitiveObjectInspectorConverter
operator|.
name|TimestampConverter
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

begin_comment
comment|/**  * UDFDateDiff.  *  * Calculate the difference in the number of days. The time part of the string  * will be ignored. If dateString1 is earlier than dateString2, then the  * result can be negative.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"datediff"
argument_list|,
name|value
operator|=
literal|"_FUNC_(date1, date2) - Returns the number of days between date1 and date2"
argument_list|,
name|extended
operator|=
literal|"date1 and date2 are strings in the format "
operator|+
literal|"'yyyy-MM-dd HH:mm:ss' or 'yyyy-MM-dd'. The time parts are ignored."
operator|+
literal|"If date1 is earlier than date2, the result is negative.\n"
operator|+
literal|"Example:\n "
operator|+
literal|"> SELECT _FUNC_('2009-07-30', '2009-07-31') FROM src LIMIT 1;\n"
operator|+
literal|"  1"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|VectorUDFDateDiffColScalar
operator|.
name|class
block|,
name|VectorUDFDateDiffColCol
operator|.
name|class
block|,
name|VectorUDFDateDiffScalarCol
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|GenericUDFDateDiff
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|SimpleDateFormat
name|formatter
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd"
argument_list|)
decl_stmt|;
specifier|private
specifier|transient
name|Converter
name|inputConverter1
decl_stmt|;
specifier|private
specifier|transient
name|Converter
name|inputConverter2
decl_stmt|;
specifier|private
name|IntWritable
name|output
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveCategory
name|inputType1
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveCategory
name|inputType2
decl_stmt|;
specifier|private
name|IntWritable
name|result
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
specifier|public
name|GenericUDFDateDiff
parameter_list|()
block|{   }
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
literal|2
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"datediff() requires 2 argument, got "
operator|+
name|arguments
operator|.
name|length
argument_list|)
throw|;
block|}
name|inputConverter1
operator|=
name|checkArguments
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|inputConverter2
operator|=
name|checkArguments
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|inputType1
operator|=
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
name|getPrimitiveCategory
argument_list|()
expr_stmt|;
name|inputType2
operator|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|1
index|]
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
expr_stmt|;
name|ObjectInspector
name|outputOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
decl_stmt|;
return|return
name|outputOI
return|;
block|}
annotation|@
name|Override
specifier|public
name|IntWritable
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
name|output
operator|=
name|evaluate
argument_list|(
name|convertToDate
argument_list|(
name|inputType1
argument_list|,
name|inputConverter1
argument_list|,
name|arguments
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|convertToDate
argument_list|(
name|inputType2
argument_list|,
name|inputConverter2
argument_list|,
name|arguments
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|output
return|;
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
literal|"datediff"
argument_list|,
name|children
argument_list|)
return|;
block|}
specifier|private
name|Date
name|convertToDate
parameter_list|(
name|PrimitiveCategory
name|inputType
parameter_list|,
name|Converter
name|converter
parameter_list|,
name|DeferredObject
name|argument
parameter_list|)
throws|throws
name|HiveException
block|{
assert|assert
operator|(
name|converter
operator|!=
literal|null
operator|)
assert|;
assert|assert
operator|(
name|argument
operator|!=
literal|null
operator|)
assert|;
if|if
condition|(
name|argument
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Date
name|date
init|=
operator|new
name|Date
argument_list|(
literal|0
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|inputType
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
name|String
name|dateString
init|=
name|converter
operator|.
name|convert
argument_list|(
name|argument
operator|.
name|get
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
try|try
block|{
name|date
operator|.
name|setTime
argument_list|(
name|formatter
operator|.
name|parse
argument_list|(
name|dateString
argument_list|)
operator|.
name|getTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
break|break;
case|case
name|TIMESTAMP
case|:
name|Timestamp
name|ts
init|=
operator|(
operator|(
name|TimestampWritable
operator|)
name|converter
operator|.
name|convert
argument_list|(
name|argument
operator|.
name|get
argument_list|()
argument_list|)
operator|)
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
name|date
operator|.
name|setTime
argument_list|(
name|ts
operator|.
name|getTime
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|DateWritable
name|dw
init|=
operator|(
name|DateWritable
operator|)
name|converter
operator|.
name|convert
argument_list|(
name|argument
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|date
operator|=
name|dw
operator|.
name|get
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"TO_DATE() only takes STRING/TIMESTAMP/DATEWRITABLE types, got "
operator|+
name|inputType
argument_list|)
throw|;
block|}
return|return
name|date
return|;
block|}
specifier|private
name|Converter
name|checkArguments
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|,
name|int
name|i
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
if|if
condition|(
name|arguments
index|[
name|i
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
name|arguments
index|[
name|i
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" is passed. as first arguments"
argument_list|)
throw|;
block|}
name|PrimitiveCategory
name|inputType
init|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
name|i
index|]
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
name|Converter
name|converter
decl_stmt|;
switch|switch
condition|(
name|inputType
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
name|converter
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
name|i
index|]
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|converter
operator|=
operator|new
name|TimestampConverter
argument_list|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
name|i
index|]
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableTimestampObjectInspector
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|converter
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
name|i
index|]
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDateObjectInspector
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|" DATEDIFF() only takes STRING/TIMESTAMP/DATEWRITABLE types as "
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
operator|+
literal|"-th argument, got "
operator|+
name|inputType
argument_list|)
throw|;
block|}
return|return
name|converter
return|;
block|}
specifier|private
name|IntWritable
name|evaluate
parameter_list|(
name|Date
name|date
parameter_list|,
name|Date
name|date2
parameter_list|)
block|{
if|if
condition|(
name|date
operator|==
literal|null
operator|||
name|date2
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|result
operator|.
name|set
argument_list|(
name|DateWritable
operator|.
name|dateToDays
argument_list|(
name|date
argument_list|)
operator|-
name|DateWritable
operator|.
name|dateToDays
argument_list|(
name|date2
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

