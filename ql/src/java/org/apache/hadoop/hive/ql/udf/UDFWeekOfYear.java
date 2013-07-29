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
package|;
end_package

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
name|UDF
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
comment|/**  * UDFWeekOfYear.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"yearweek"
argument_list|,
name|value
operator|=
literal|"_FUNC_(date) - Returns the week of the year of the given date. A week "
operator|+
literal|"is considered to start on a Monday and week 1 is the first week with>3 days."
argument_list|,
name|extended
operator|=
literal|"Examples:\n"
operator|+
literal|"> SELECT _FUNC_('2008-02-20') FROM src LIMIT 1;\n"
operator|+
literal|"  8\n"
operator|+
literal|"> SELECT _FUNC_('1980-12-31 12:59:59') FROM src LIMIT 1;\n"
operator|+
literal|"  1"
argument_list|)
specifier|public
class|class
name|UDFWeekOfYear
extends|extends
name|UDF
block|{
specifier|private
specifier|final
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
name|IntWritable
name|result
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
specifier|public
name|UDFWeekOfYear
parameter_list|()
block|{
name|calendar
operator|.
name|setFirstDayOfWeek
argument_list|(
name|Calendar
operator|.
name|MONDAY
argument_list|)
expr_stmt|;
name|calendar
operator|.
name|setMinimalDaysInFirstWeek
argument_list|(
literal|4
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the week of the year from a date string.    *    * @param dateString    *          the dateString in the format of "yyyy-MM-dd HH:mm:ss" or    *          "yyyy-MM-dd".    * @return an int from 1 to 53. null if the dateString is not a valid date    *         string.    */
specifier|public
name|IntWritable
name|evaluate
parameter_list|(
name|Text
name|dateString
parameter_list|)
block|{
if|if
condition|(
name|dateString
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
name|Date
name|date
init|=
name|formatter
operator|.
name|parse
argument_list|(
name|dateString
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|calendar
operator|.
name|setTime
argument_list|(
name|date
argument_list|)
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
name|calendar
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|WEEK_OF_YEAR
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
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
block|}
specifier|public
name|IntWritable
name|evaluate
parameter_list|(
name|DateWritable
name|d
parameter_list|)
block|{
if|if
condition|(
name|d
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
name|setTime
argument_list|(
name|d
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
name|calendar
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|WEEK_OF_YEAR
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|IntWritable
name|evaluate
parameter_list|(
name|TimestampWritable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
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
name|setTime
argument_list|(
name|t
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
name|calendar
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|WEEK_OF_YEAR
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

