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
name|VectorUDFDayOfWeekDate
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
name|VectorUDFDayOfWeekString
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
name|VectorUDFDayOfWeekTimestamp
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
comment|/**  * UDFDayOfWeek.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"dayofweek"
argument_list|,
name|value
operator|=
literal|"_FUNC_(param) - Returns the day of the week of date/timestamp "
operator|+
literal|"(1 = Sunday, 2 = Monday, ..., 7 = Saturday)"
argument_list|,
name|extended
operator|=
literal|"param can be one of:\n"
operator|+
literal|"1. A string in the format of 'yyyy-MM-dd HH:mm:ss' or 'yyyy-MM-dd'.\n"
operator|+
literal|"2. A date value\n"
operator|+
literal|"3. A timestamp value"
operator|+
literal|"Example:\n "
operator|+
literal|"> SELECT _FUNC_('2009-07-30') FROM src LIMIT 1;\n"
operator|+
literal|"  5"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|VectorUDFDayOfWeekDate
operator|.
name|class
block|,
name|VectorUDFDayOfWeekString
operator|.
name|class
block|,
name|VectorUDFDayOfWeekTimestamp
operator|.
name|class
block|}
argument_list|)
annotation|@
name|NDV
argument_list|(
name|maxNdv
operator|=
literal|7
argument_list|)
specifier|public
class|class
name|UDFDayOfWeek
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
specifier|final
name|IntWritable
name|result
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
specifier|public
name|UDFDayOfWeek
parameter_list|()
block|{   }
comment|/**    * Get the day of week from a date string.    *    * @param dateString    *          the dateString in the format of "yyyy-MM-dd HH:mm:ss" or    *          "yyyy-MM-dd".    * @return an int from 1 to 7. null if the dateString is not a valid date    *         string.    */
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
name|DAY_OF_WEEK
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
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// Time doesn't matter.
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
name|DAY_OF_WEEK
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
name|DAY_OF_WEEK
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

