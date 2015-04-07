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
import|import static
name|java
operator|.
name|math
operator|.
name|BigDecimal
operator|.
name|ROUND_HALF_UP
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Calendar
operator|.
name|DATE
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Calendar
operator|.
name|HOUR_OF_DAY
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Calendar
operator|.
name|MINUTE
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Calendar
operator|.
name|MONTH
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Calendar
operator|.
name|SECOND
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Calendar
operator|.
name|YEAR
import|;
end_import

begin_import
import|import static
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
operator|.
name|PrimitiveGrouping
operator|.
name|DATE_GROUP
import|;
end_import

begin_import
import|import static
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
operator|.
name|PrimitiveGrouping
operator|.
name|STRING_GROUP
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
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

begin_comment
comment|/**  * UDFMonthsBetween.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"months_between"
argument_list|,
name|value
operator|=
literal|"_FUNC_(date1, date2) - returns number of months between dates date1 and date2"
argument_list|,
name|extended
operator|=
literal|"If date1 is later than date2, then the result is positive. "
operator|+
literal|"If date1 is earlier than date2, then the result is negative. "
operator|+
literal|"If date1 and date2 are either the same days of the month or both last days of months, "
operator|+
literal|"then the result is always an integer. "
operator|+
literal|"Otherwise the UDF calculates the fractional portion of the result based on a 31-day "
operator|+
literal|"month and considers the difference in time components date1 and date2.\n"
operator|+
literal|"date1 and date2 type can be date, timestamp or string in the format "
operator|+
literal|"'yyyy-MM-dd' or 'yyyy-MM-dd HH:mm:ss'. "
operator|+
literal|"The result is rounded to 8 decimal places.\n"
operator|+
literal|" Example:\n"
operator|+
literal|"> SELECT _FUNC_('1997-02-28 10:30:00', '1996-10-30');\n 3.94959677"
argument_list|)
specifier|public
class|class
name|GenericUDFMonthsBetween
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|Converter
index|[]
name|tsConverters
init|=
operator|new
name|Converter
index|[
literal|2
index|]
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveCategory
index|[]
name|tsInputTypes
init|=
operator|new
name|PrimitiveCategory
index|[
literal|2
index|]
decl_stmt|;
specifier|private
specifier|transient
name|Converter
index|[]
name|dtConverters
init|=
operator|new
name|Converter
index|[
literal|2
index|]
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveCategory
index|[]
name|dtInputTypes
init|=
operator|new
name|PrimitiveCategory
index|[
literal|2
index|]
decl_stmt|;
specifier|private
specifier|final
name|Calendar
name|cal1
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Calendar
name|cal2
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|DoubleWritable
name|output
init|=
operator|new
name|DoubleWritable
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
name|checkArgsSize
argument_list|(
name|arguments
argument_list|,
literal|2
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|checkArgPrimitive
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|checkArgPrimitive
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// the function should support both short date and full timestamp format
comment|// time part of the timestamp should not be skipped
name|checkArgGroups
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
name|tsInputTypes
argument_list|,
name|STRING_GROUP
argument_list|,
name|DATE_GROUP
argument_list|)
expr_stmt|;
name|checkArgGroups
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|,
name|tsInputTypes
argument_list|,
name|STRING_GROUP
argument_list|,
name|DATE_GROUP
argument_list|)
expr_stmt|;
name|checkArgGroups
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
name|dtInputTypes
argument_list|,
name|STRING_GROUP
argument_list|,
name|DATE_GROUP
argument_list|)
expr_stmt|;
name|checkArgGroups
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|,
name|dtInputTypes
argument_list|,
name|STRING_GROUP
argument_list|,
name|DATE_GROUP
argument_list|)
expr_stmt|;
name|obtainTimestampConverter
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
name|tsInputTypes
argument_list|,
name|tsConverters
argument_list|)
expr_stmt|;
name|obtainTimestampConverter
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|,
name|tsInputTypes
argument_list|,
name|tsConverters
argument_list|)
expr_stmt|;
name|obtainDateConverter
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
name|dtInputTypes
argument_list|,
name|dtConverters
argument_list|)
expr_stmt|;
name|obtainDateConverter
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|,
name|dtInputTypes
argument_list|,
name|dtConverters
argument_list|)
expr_stmt|;
name|ObjectInspector
name|outputOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
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
comment|// the function should support both short date and full timestamp format
comment|// time part of the timestamp should not be skipped
name|Date
name|date1
init|=
name|getTimestampValue
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
name|tsConverters
argument_list|)
decl_stmt|;
if|if
condition|(
name|date1
operator|==
literal|null
condition|)
block|{
name|date1
operator|=
name|getDateValue
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
name|dtInputTypes
argument_list|,
name|dtConverters
argument_list|)
expr_stmt|;
if|if
condition|(
name|date1
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
name|Date
name|date2
init|=
name|getTimestampValue
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|,
name|tsConverters
argument_list|)
decl_stmt|;
if|if
condition|(
name|date2
operator|==
literal|null
condition|)
block|{
name|date2
operator|=
name|getDateValue
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|,
name|dtInputTypes
argument_list|,
name|dtConverters
argument_list|)
expr_stmt|;
if|if
condition|(
name|date2
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
name|cal1
operator|.
name|setTime
argument_list|(
name|date1
argument_list|)
expr_stmt|;
name|cal2
operator|.
name|setTime
argument_list|(
name|date2
argument_list|)
expr_stmt|;
comment|// skip day/time part if both dates are end of the month
comment|// or the same day of the month
name|int
name|monDiffInt
init|=
operator|(
name|cal1
operator|.
name|get
argument_list|(
name|YEAR
argument_list|)
operator|-
name|cal2
operator|.
name|get
argument_list|(
name|YEAR
argument_list|)
operator|)
operator|*
literal|12
operator|+
operator|(
name|cal1
operator|.
name|get
argument_list|(
name|MONTH
argument_list|)
operator|-
name|cal2
operator|.
name|get
argument_list|(
name|MONTH
argument_list|)
operator|)
decl_stmt|;
if|if
condition|(
name|cal1
operator|.
name|get
argument_list|(
name|DATE
argument_list|)
operator|==
name|cal2
operator|.
name|get
argument_list|(
name|DATE
argument_list|)
operator|||
operator|(
name|cal1
operator|.
name|get
argument_list|(
name|DATE
argument_list|)
operator|==
name|cal1
operator|.
name|getActualMaximum
argument_list|(
name|DATE
argument_list|)
operator|&&
name|cal2
operator|.
name|get
argument_list|(
name|DATE
argument_list|)
operator|==
name|cal2
operator|.
name|getActualMaximum
argument_list|(
name|DATE
argument_list|)
operator|)
condition|)
block|{
name|output
operator|.
name|set
argument_list|(
name|monDiffInt
argument_list|)
expr_stmt|;
return|return
name|output
return|;
block|}
name|int
name|sec1
init|=
name|getDayPartInSec
argument_list|(
name|cal1
argument_list|)
decl_stmt|;
name|int
name|sec2
init|=
name|getDayPartInSec
argument_list|(
name|cal2
argument_list|)
decl_stmt|;
comment|// 1 sec is 0.000000373 months (1/2678400). 1 month is 31 days.
comment|// there should be no adjustments for leap seconds
name|double
name|monBtwDbl
init|=
name|monDiffInt
operator|+
operator|(
name|sec1
operator|-
name|sec2
operator|)
operator|/
literal|2678400D
decl_stmt|;
comment|// Round a double to 8 decimal places.
name|double
name|result
init|=
name|BigDecimal
operator|.
name|valueOf
argument_list|(
name|monBtwDbl
argument_list|)
operator|.
name|setScale
argument_list|(
literal|8
argument_list|,
name|ROUND_HALF_UP
argument_list|)
operator|.
name|doubleValue
argument_list|()
decl_stmt|;
name|output
operator|.
name|set
argument_list|(
name|result
argument_list|)
expr_stmt|;
return|return
name|output
return|;
block|}
specifier|protected
name|int
name|getDayPartInSec
parameter_list|(
name|Calendar
name|cal
parameter_list|)
block|{
name|int
name|dd
init|=
name|cal
operator|.
name|get
argument_list|(
name|DATE
argument_list|)
decl_stmt|;
name|int
name|HH
init|=
name|cal
operator|.
name|get
argument_list|(
name|HOUR_OF_DAY
argument_list|)
decl_stmt|;
name|int
name|mm
init|=
name|cal
operator|.
name|get
argument_list|(
name|MINUTE
argument_list|)
decl_stmt|;
name|int
name|ss
init|=
name|cal
operator|.
name|get
argument_list|(
name|SECOND
argument_list|)
decl_stmt|;
name|int
name|dayInSec
init|=
name|dd
operator|*
literal|86400
operator|+
name|HH
operator|*
literal|3600
operator|+
name|mm
operator|*
literal|60
operator|+
name|ss
decl_stmt|;
return|return
name|dayInSec
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
annotation|@
name|Override
specifier|protected
name|String
name|getFuncName
parameter_list|()
block|{
return|return
literal|"months_between"
return|;
block|}
block|}
end_class

end_unit

