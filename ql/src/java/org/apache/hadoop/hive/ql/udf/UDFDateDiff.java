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
comment|/**  * UDFDateDiff.  *  */
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
literal|"> SELECT _FUNC_('2009-30-07', '2009-31-07') FROM src LIMIT 1;\n"
operator|+
literal|"  1"
argument_list|)
specifier|public
class|class
name|UDFDateDiff
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
name|IntWritable
name|result
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
specifier|public
name|UDFDateDiff
parameter_list|()
block|{
name|formatter
operator|.
name|setTimeZone
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"UTC"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Calculate the difference in the number of days. The time part of the string    * will be ignored. If dateString1 is earlier than dateString2, then the    * result can be negative.    *     * @param dateString1    *          the date string in the format of "yyyy-MM-dd HH:mm:ss" or    *          "yyyy-MM-dd".    * @param dateString2    *          the date string in the format of "yyyy-MM-dd HH:mm:ss" or    *          "yyyy-MM-dd".    * @return the difference in days.    */
specifier|public
name|IntWritable
name|evaluate
parameter_list|(
name|Text
name|dateString1
parameter_list|,
name|Text
name|dateString2
parameter_list|)
block|{
if|if
condition|(
name|dateString1
operator|==
literal|null
operator|||
name|dateString2
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
comment|// NOTE: This implementation avoids the extra-second problem
comment|// by comparing with UTC epoch and integer division.
name|long
name|diffInMilliSeconds
init|=
operator|(
name|formatter
operator|.
name|parse
argument_list|(
name|dateString1
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|getTime
argument_list|()
operator|-
name|formatter
operator|.
name|parse
argument_list|(
name|dateString2
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|getTime
argument_list|()
operator|)
decl_stmt|;
comment|// 86400 is the number of seconds in a day
name|result
operator|.
name|set
argument_list|(
call|(
name|int
call|)
argument_list|(
name|diffInMilliSeconds
operator|/
operator|(
literal|86400
operator|*
literal|1000
operator|)
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
block|}
end_class

end_unit

