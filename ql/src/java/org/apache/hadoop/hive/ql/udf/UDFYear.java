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
name|HiveIntervalYearMonth
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
name|VectorUDFYearDate
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
name|VectorUDFYearString
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
name|VectorUDFYearTimestamp
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
name|ql
operator|.
name|udf
operator|.
name|generic
operator|.
name|NDV
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
name|io
operator|.
name|IntWritable
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
name|TimeZone
import|;
end_import

begin_comment
comment|/**  * UDFYear.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"year"
argument_list|,
name|value
operator|=
literal|"_FUNC_(param) - Returns the year component of the date/timestamp/interval"
argument_list|,
name|extended
operator|=
literal|"param can be one of:\n"
operator|+
literal|"1. A string in the format of 'yyyy-MM-dd HH:mm:ss' or 'yyyy-MM-dd'.\n"
operator|+
literal|"2. A date value\n"
operator|+
literal|"3. A timestamp value\n"
operator|+
literal|"4. A year-month interval value"
operator|+
literal|"Example:\n "
operator|+
literal|"> SELECT _FUNC_('2009-07-30') FROM src LIMIT 1;\n"
operator|+
literal|"  2009"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|VectorUDFYearDate
operator|.
name|class
block|,
name|VectorUDFYearString
operator|.
name|class
block|,
name|VectorUDFYearTimestamp
operator|.
name|class
block|}
argument_list|)
annotation|@
name|NDV
argument_list|(
name|maxNdv
operator|=
literal|20
argument_list|)
comment|// although technically its unbounded, its unlikely we will ever see ndv> 20
specifier|public
class|class
name|UDFYear
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|ObjectInspectorConverters
operator|.
name|Converter
index|[]
name|converters
init|=
operator|new
name|ObjectInspectorConverters
operator|.
name|Converter
index|[
literal|1
index|]
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
index|[]
name|inputTypes
init|=
operator|new
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
index|[
literal|1
index|]
decl_stmt|;
specifier|private
specifier|final
name|IntWritable
name|output
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Calendar
name|calendar
init|=
name|Calendar
operator|.
name|getInstance
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"UTC"
argument_list|)
argument_list|)
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
name|checkArgsSize
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|checkArgPrimitive
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|)
expr_stmt|;
switch|switch
condition|(
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
condition|)
block|{
case|case
name|INTERVAL_YEAR_MONTH
case|:
name|inputTypes
index|[
literal|0
index|]
operator|=
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|INTERVAL_YEAR_MONTH
expr_stmt|;
name|converters
index|[
literal|0
index|]
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
name|writableHiveIntervalYearMonthObjectInspector
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
case|case
name|DATE
case|:
case|case
name|TIMESTAMP
case|:
case|case
name|TIMESTAMPLOCALTZ
case|:
case|case
name|VOID
case|:
name|obtainDateConverter
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
name|inputTypes
argument_list|,
name|converters
argument_list|)
expr_stmt|;
break|break;
default|default:
comment|// build error message
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
name|getFuncName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" does not take "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
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
name|getPrimitiveCategory
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" type"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
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
switch|switch
condition|(
name|inputTypes
index|[
literal|0
index|]
condition|)
block|{
case|case
name|INTERVAL_YEAR_MONTH
case|:
name|HiveIntervalYearMonth
name|intervalYearMonth
init|=
name|getIntervalYearMonthValue
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
name|inputTypes
argument_list|,
name|converters
argument_list|)
decl_stmt|;
if|if
condition|(
name|intervalYearMonth
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|output
operator|.
name|set
argument_list|(
name|intervalYearMonth
operator|.
name|getYears
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRING
case|:
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
case|case
name|DATE
case|:
case|case
name|TIMESTAMP
case|:
case|case
name|TIMESTAMPLOCALTZ
case|:
case|case
name|VOID
case|:
name|Date
name|date
init|=
name|getDateValue
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
name|inputTypes
argument_list|,
name|converters
argument_list|)
decl_stmt|;
if|if
condition|(
name|date
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|calendar
operator|.
name|setTimeInMillis
argument_list|(
name|date
operator|.
name|toEpochMilli
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|set
argument_list|(
name|calendar
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|YEAR
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|output
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|getFuncName
parameter_list|()
block|{
return|return
literal|"year"
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
name|getFuncName
argument_list|()
argument_list|,
name|children
argument_list|)
return|;
block|}
block|}
end_class

end_unit

