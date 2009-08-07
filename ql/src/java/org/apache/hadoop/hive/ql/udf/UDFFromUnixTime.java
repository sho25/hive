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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|description
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
annotation|@
name|description
argument_list|(
name|name
operator|=
literal|"from_unixtime"
argument_list|,
name|value
operator|=
literal|"_FUNC_(unix_time, format) - returns unix_time in the specified "
operator|+
literal|"format"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_(0, 'yyyy-MM-dd HH:mm:ss') FROM src LIMIT 1;\n"
operator|+
literal|"  '1970-01-01 00:00:00'"
argument_list|)
specifier|public
class|class
name|UDFFromUnixTime
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
name|UDFFromUnixTime
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
decl_stmt|;
name|Text
name|result
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|Text
name|lastFormat
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|public
name|UDFFromUnixTime
parameter_list|()
block|{   }
name|Text
name|defaultFormat
init|=
operator|new
name|Text
argument_list|(
literal|"yyyy-MM-dd HH:mm:ss"
argument_list|)
decl_stmt|;
specifier|public
name|Text
name|evaluate
parameter_list|(
name|IntWritable
name|unixtime
parameter_list|)
block|{
return|return
name|evaluate
argument_list|(
name|unixtime
argument_list|,
name|defaultFormat
argument_list|)
return|;
block|}
comment|/**    * Convert UnixTime to a string format.    * @param unixtime  The number of seconds from 1970-01-01 00:00:00    * @param format See http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html    * @return a String in the format specified.    */
specifier|public
name|Text
name|evaluate
parameter_list|(
name|IntWritable
name|unixtime
parameter_list|,
name|Text
name|format
parameter_list|)
block|{
if|if
condition|(
name|unixtime
operator|==
literal|null
operator|||
name|format
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
operator|!
name|format
operator|.
name|equals
argument_list|(
name|lastFormat
argument_list|)
condition|)
block|{
name|formatter
operator|=
operator|new
name|SimpleDateFormat
argument_list|(
name|format
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|lastFormat
operator|.
name|set
argument_list|(
name|format
argument_list|)
expr_stmt|;
block|}
comment|// convert seconds to milliseconds
name|Date
name|date
init|=
operator|new
name|Date
argument_list|(
name|unixtime
operator|.
name|get
argument_list|()
operator|*
literal|1000L
argument_list|)
decl_stmt|;
name|result
operator|.
name|set
argument_list|(
name|formatter
operator|.
name|format
argument_list|(
name|date
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

