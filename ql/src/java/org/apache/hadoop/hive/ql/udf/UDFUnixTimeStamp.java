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
name|io
operator|.
name|LongWritable
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
comment|/**  * UDFUnixTimeStamp.  *  */
end_comment

begin_class
annotation|@
name|UDFType
argument_list|(
name|deterministic
operator|=
literal|false
argument_list|)
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"unix_timestamp"
argument_list|,
name|value
operator|=
literal|"_FUNC_([date[, pattern]]) - Returns the UNIX timestamp"
argument_list|,
name|extended
operator|=
literal|"Converts the current or specified time to number of seconds "
operator|+
literal|"since 1970-01-01."
argument_list|)
specifier|public
class|class
name|UDFUnixTimeStamp
extends|extends
name|UDF
block|{
comment|// For now, we just use the default time zone.
specifier|private
specifier|final
name|SimpleDateFormat
name|formatter
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd HH:mm:ss"
argument_list|)
decl_stmt|;
name|LongWritable
name|result
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
specifier|public
name|UDFUnixTimeStamp
parameter_list|()
block|{   }
comment|/**    * Return current UnixTime.    *     * @return long Number of seconds from 1970-01-01 00:00:00    */
specifier|public
name|LongWritable
name|evaluate
parameter_list|()
block|{
name|Date
name|date
init|=
operator|new
name|Date
argument_list|()
decl_stmt|;
name|result
operator|.
name|set
argument_list|(
name|date
operator|.
name|getTime
argument_list|()
operator|/
literal|1000
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Convert time string to UnixTime.    *     * @param dateText    *          Time string in format yyyy-MM-dd HH:mm:ss    * @return long Number of seconds from 1970-01-01 00:00:00    */
specifier|public
name|LongWritable
name|evaluate
parameter_list|(
name|Text
name|dateText
parameter_list|)
block|{
if|if
condition|(
name|dateText
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
name|dateText
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|result
operator|.
name|set
argument_list|(
name|date
operator|.
name|getTime
argument_list|()
operator|/
literal|1000
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
name|Text
name|lastPatternText
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
comment|/**    * Convert time string to UnixTime with user defined pattern.    *     * @param dateText    *          Time string in format patternstring    * @param patternText    *          Time patterns string supported by SimpleDateFormat    * @return long Number of seconds from 1970-01-01 00:00:00    */
specifier|public
name|LongWritable
name|evaluate
parameter_list|(
name|Text
name|dateText
parameter_list|,
name|Text
name|patternText
parameter_list|)
block|{
if|if
condition|(
name|dateText
operator|==
literal|null
operator|||
name|patternText
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
if|if
condition|(
operator|!
name|patternText
operator|.
name|equals
argument_list|(
name|lastPatternText
argument_list|)
condition|)
block|{
name|formatter
operator|.
name|applyPattern
argument_list|(
name|patternText
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|lastPatternText
operator|.
name|set
argument_list|(
name|patternText
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|evaluate
argument_list|(
name|dateText
argument_list|)
return|;
block|}
block|}
end_class

end_unit

