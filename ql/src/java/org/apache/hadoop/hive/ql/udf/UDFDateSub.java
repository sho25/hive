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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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

begin_class
specifier|public
class|class
name|UDFDateSub
extends|extends
name|UDF
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|UDFDateSub
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
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
name|Text
name|result
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|public
name|UDFDateSub
parameter_list|()
block|{   }
comment|/**    * Subtract a number of days to the date.     * The time part of the string will be ignored.    *     * NOTE: This is a subset of what MySQL offers as:    * http://dev.mysql.com/doc/refman/5.1/en/date-and-time-functions.html#function_date-sub    *     * @param dateString1 the date string in the format of "yyyy-MM-dd HH:mm:ss" or "yyyy-MM-dd".    * @param days the number of days to subtract.    * @return the date in the format of "yyyy-MM-dd".    */
specifier|public
name|Text
name|evaluate
parameter_list|(
name|Text
name|dateString1
parameter_list|,
name|IntWritable
name|days
parameter_list|)
block|{
try|try
block|{
name|calendar
operator|.
name|setTime
argument_list|(
name|formatter
operator|.
name|parse
argument_list|(
name|dateString1
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|calendar
operator|.
name|add
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|,
operator|-
name|days
operator|.
name|get
argument_list|()
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
name|result
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

