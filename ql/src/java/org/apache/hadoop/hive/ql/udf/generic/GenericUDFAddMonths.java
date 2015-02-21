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
name|util
operator|.
name|Calendar
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
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
name|Text
import|;
end_import

begin_comment
comment|/**  * GenericUDFAddMonths.  *  * Add a number of months to the date. The time part of the string will be  * ignored.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"add_months"
argument_list|,
name|value
operator|=
literal|"_FUNC_(start_date, num_months) - Returns the date that is num_months after start_date."
argument_list|,
name|extended
operator|=
literal|"start_date is a string in the format 'yyyy-MM-dd HH:mm:ss' or"
operator|+
literal|" 'yyyy-MM-dd'. num_months is a number. The time part of start_date is "
operator|+
literal|"ignored.\n"
operator|+
literal|"Example:\n "
operator|+
literal|"> SELECT _FUNC_('2009-08-31', 1) FROM src LIMIT 1;\n"
operator|+
literal|" '2009-09-30'"
argument_list|)
specifier|public
class|class
name|GenericUDFAddMonths
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
name|TimestampConverter
name|timestampConverter
decl_stmt|;
specifier|private
specifier|transient
name|Converter
name|textConverter
decl_stmt|;
specifier|private
specifier|transient
name|Converter
name|dateWritableConverter
decl_stmt|;
specifier|private
specifier|transient
name|Converter
name|intWritableConverter
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
specifier|final
name|Calendar
name|calendar
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Text
name|output
init|=
operator|new
name|Text
argument_list|()
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
literal|2
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"add_months() requires 2 argument, got "
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
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" is passed as first arguments"
argument_list|)
throw|;
block|}
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
name|arguments
index|[
literal|1
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" is passed as second arguments"
argument_list|)
throw|;
block|}
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
name|ObjectInspector
name|outputOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
switch|switch
condition|(
name|inputType1
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
name|inputType1
operator|=
name|PrimitiveCategory
operator|.
name|STRING
expr_stmt|;
name|textConverter
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
literal|0
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
name|timestampConverter
operator|=
operator|new
name|TimestampConverter
argument_list|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|0
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
name|dateWritableConverter
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
literal|0
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
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"ADD_MONTHS() only takes STRING/TIMESTAMP/DATEWRITABLE types as first argument, got "
operator|+
name|inputType1
argument_list|)
throw|;
block|}
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
if|if
condition|(
name|inputType2
operator|!=
name|PrimitiveCategory
operator|.
name|INT
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"ADD_MONTHS() only takes INT types as second argument, got "
operator|+
name|inputType2
argument_list|)
throw|;
block|}
name|intWritableConverter
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
literal|1
index|]
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|arguments
index|[
literal|0
index|]
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
name|IntWritable
name|toBeAdded
init|=
operator|(
name|IntWritable
operator|)
name|intWritableConverter
operator|.
name|convert
argument_list|(
name|arguments
index|[
literal|1
index|]
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|toBeAdded
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
decl_stmt|;
switch|switch
condition|(
name|inputType1
condition|)
block|{
case|case
name|STRING
case|:
name|String
name|dateString
init|=
name|textConverter
operator|.
name|convert
argument_list|(
name|arguments
index|[
literal|0
index|]
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
operator|=
name|formatter
operator|.
name|parse
argument_list|(
name|dateString
operator|.
name|toString
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
name|timestampConverter
operator|.
name|convert
argument_list|(
name|arguments
index|[
literal|0
index|]
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
operator|=
name|ts
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
name|dateWritableConverter
operator|.
name|convert
argument_list|(
name|arguments
index|[
literal|0
index|]
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
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"ADD_MONTHS() only takes STRING/TIMESTAMP/DATEWRITABLE types, got "
operator|+
name|inputType1
argument_list|)
throw|;
block|}
name|int
name|numMonth
init|=
name|toBeAdded
operator|.
name|get
argument_list|()
decl_stmt|;
name|addMonth
argument_list|(
name|date
argument_list|,
name|numMonth
argument_list|)
expr_stmt|;
name|Date
name|newDate
init|=
name|calendar
operator|.
name|getTime
argument_list|()
decl_stmt|;
name|output
operator|.
name|set
argument_list|(
name|formatter
operator|.
name|format
argument_list|(
name|newDate
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
literal|"add_months"
argument_list|,
name|children
argument_list|)
return|;
block|}
specifier|protected
name|Calendar
name|addMonth
parameter_list|(
name|Date
name|d
parameter_list|,
name|int
name|numMonths
parameter_list|)
block|{
name|calendar
operator|.
name|setTime
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|boolean
name|lastDatOfMonth
init|=
name|isLastDayOfMonth
argument_list|(
name|calendar
argument_list|)
decl_stmt|;
name|calendar
operator|.
name|add
argument_list|(
name|Calendar
operator|.
name|MONTH
argument_list|,
name|numMonths
argument_list|)
expr_stmt|;
if|if
condition|(
name|lastDatOfMonth
condition|)
block|{
name|int
name|maxDd
init|=
name|calendar
operator|.
name|getActualMaximum
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|)
decl_stmt|;
name|calendar
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|,
name|maxDd
argument_list|)
expr_stmt|;
block|}
return|return
name|calendar
return|;
block|}
specifier|protected
name|boolean
name|isLastDayOfMonth
parameter_list|(
name|Calendar
name|cal
parameter_list|)
block|{
name|int
name|maxDd
init|=
name|cal
operator|.
name|getActualMaximum
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|)
decl_stmt|;
name|int
name|dd
init|=
name|cal
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|)
decl_stmt|;
return|return
name|dd
operator|==
name|maxDd
return|;
block|}
block|}
end_class

end_unit

