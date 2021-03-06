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
name|common
operator|.
name|type
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
name|java
operator|.
name|util
operator|.
name|GregorianCalendar
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * Conversion utilities from the hybrid Julian/Gregorian calendar to/from the  * proleptic Gregorian.  *  * The semantics here are to hold the string representation constant and change  * the epoch offset rather than holding the instant in time constant and change  * the string representation.  *  * These utilities will be fast for the common case (> 1582 AD), but slow for  * old dates.  */
end_comment

begin_class
specifier|public
class|class
name|CalendarUtils
block|{
specifier|public
specifier|static
specifier|final
name|long
name|SWITCHOVER_MILLIS
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|SWITCHOVER_DAYS
decl_stmt|;
specifier|private
specifier|static
name|SimpleDateFormat
name|createFormatter
parameter_list|(
name|String
name|fmt
parameter_list|,
name|boolean
name|proleptic
parameter_list|)
block|{
name|SimpleDateFormat
name|result
init|=
operator|new
name|SimpleDateFormat
argument_list|(
name|fmt
argument_list|)
decl_stmt|;
name|GregorianCalendar
name|calendar
init|=
operator|new
name|GregorianCalendar
argument_list|(
name|UTC
argument_list|)
decl_stmt|;
if|if
condition|(
name|proleptic
condition|)
block|{
name|calendar
operator|.
name|setGregorianChange
argument_list|(
operator|new
name|Date
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|setCalendar
argument_list|(
name|calendar
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|private
specifier|static
specifier|final
name|String
name|DATE
init|=
literal|"yyyy-MM-dd"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TIME
init|=
name|DATE
operator|+
literal|" HH:mm:ss.SSS"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TimeZone
name|UTC
init|=
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"UTC"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|SimpleDateFormat
argument_list|>
name|HYBRID_DATE_FORMAT
init|=
name|ThreadLocal
operator|.
name|withInitial
argument_list|(
parameter_list|()
lambda|->
name|createFormatter
argument_list|(
name|DATE
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|SimpleDateFormat
argument_list|>
name|HYBRID_TIME_FORMAT
init|=
name|ThreadLocal
operator|.
name|withInitial
argument_list|(
parameter_list|()
lambda|->
name|createFormatter
argument_list|(
name|TIME
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|SimpleDateFormat
argument_list|>
name|PROLEPTIC_DATE_FORMAT
init|=
name|ThreadLocal
operator|.
name|withInitial
argument_list|(
parameter_list|()
lambda|->
name|createFormatter
argument_list|(
name|DATE
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|SimpleDateFormat
argument_list|>
name|PROLEPTIC_TIME_FORMAT
init|=
name|ThreadLocal
operator|.
name|withInitial
argument_list|(
parameter_list|()
lambda|->
name|createFormatter
argument_list|(
name|TIME
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
static|static
block|{
comment|// Get the last day where the two calendars agree with each other.
try|try
block|{
name|SWITCHOVER_MILLIS
operator|=
name|HYBRID_DATE_FORMAT
operator|.
name|get
argument_list|()
operator|.
name|parse
argument_list|(
literal|"1582-10-15"
argument_list|)
operator|.
name|getTime
argument_list|()
expr_stmt|;
name|SWITCHOVER_DAYS
operator|=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toDays
argument_list|(
name|SWITCHOVER_MILLIS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't parse switch over date"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Convert an epoch day from the hybrid Julian/Gregorian calendar to the    * proleptic Gregorian.    * @param hybrid day of epoch in the hybrid Julian/Gregorian    * @return day of epoch in the proleptic Gregorian    */
specifier|public
specifier|static
name|int
name|convertDateToProleptic
parameter_list|(
name|int
name|hybrid
parameter_list|)
block|{
name|int
name|proleptic
init|=
name|hybrid
decl_stmt|;
if|if
condition|(
name|hybrid
operator|<
name|SWITCHOVER_DAYS
condition|)
block|{
name|String
name|dateStr
init|=
name|HYBRID_DATE_FORMAT
operator|.
name|get
argument_list|()
operator|.
name|format
argument_list|(
operator|new
name|Date
argument_list|(
name|TimeUnit
operator|.
name|DAYS
operator|.
name|toMillis
argument_list|(
name|hybrid
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|proleptic
operator|=
operator|(
name|int
operator|)
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toDays
argument_list|(
name|PROLEPTIC_DATE_FORMAT
operator|.
name|get
argument_list|()
operator|.
name|parse
argument_list|(
name|dateStr
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
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't parse "
operator|+
name|dateStr
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|proleptic
return|;
block|}
comment|/**    * Convert an epoch day from the proleptic Gregorian calendar to the hybrid    * Julian/Gregorian.    * @param proleptic day of epoch in the proleptic Gregorian    * @return day of epoch in the hybrid Julian/Gregorian    */
specifier|public
specifier|static
name|int
name|convertDateToHybrid
parameter_list|(
name|int
name|proleptic
parameter_list|)
block|{
name|int
name|hyrbid
init|=
name|proleptic
decl_stmt|;
if|if
condition|(
name|proleptic
operator|<
name|SWITCHOVER_DAYS
condition|)
block|{
name|String
name|dateStr
init|=
name|PROLEPTIC_DATE_FORMAT
operator|.
name|get
argument_list|()
operator|.
name|format
argument_list|(
operator|new
name|Date
argument_list|(
name|TimeUnit
operator|.
name|DAYS
operator|.
name|toMillis
argument_list|(
name|proleptic
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|hyrbid
operator|=
operator|(
name|int
operator|)
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toDays
argument_list|(
name|HYBRID_DATE_FORMAT
operator|.
name|get
argument_list|()
operator|.
name|parse
argument_list|(
name|dateStr
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
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't parse "
operator|+
name|dateStr
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|hyrbid
return|;
block|}
specifier|public
specifier|static
name|int
name|convertDate
parameter_list|(
name|int
name|original
parameter_list|,
name|boolean
name|fromProleptic
parameter_list|,
name|boolean
name|toProleptic
parameter_list|)
block|{
if|if
condition|(
name|fromProleptic
operator|!=
name|toProleptic
condition|)
block|{
return|return
name|toProleptic
condition|?
name|convertDateToProleptic
argument_list|(
name|original
argument_list|)
else|:
name|convertDateToHybrid
argument_list|(
name|original
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|original
return|;
block|}
block|}
specifier|public
specifier|static
name|long
name|convertTime
parameter_list|(
name|long
name|original
parameter_list|,
name|boolean
name|fromProleptic
parameter_list|,
name|boolean
name|toProleptic
parameter_list|)
block|{
if|if
condition|(
name|fromProleptic
operator|!=
name|toProleptic
condition|)
block|{
return|return
name|toProleptic
condition|?
name|convertTimeToProleptic
argument_list|(
name|original
argument_list|)
else|:
name|convertTimeToHybrid
argument_list|(
name|original
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|original
return|;
block|}
block|}
comment|/**    * Convert epoch millis from the hybrid Julian/Gregorian calendar to the    * proleptic Gregorian.    * @param hybrid millis of epoch in the hybrid Julian/Gregorian    * @return millis of epoch in the proleptic Gregorian    */
specifier|public
specifier|static
name|long
name|convertTimeToProleptic
parameter_list|(
name|long
name|hybrid
parameter_list|)
block|{
name|long
name|proleptic
init|=
name|hybrid
decl_stmt|;
if|if
condition|(
name|hybrid
operator|<
name|SWITCHOVER_MILLIS
condition|)
block|{
name|String
name|dateStr
init|=
name|HYBRID_TIME_FORMAT
operator|.
name|get
argument_list|()
operator|.
name|format
argument_list|(
operator|new
name|Date
argument_list|(
name|hybrid
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|proleptic
operator|=
name|PROLEPTIC_TIME_FORMAT
operator|.
name|get
argument_list|()
operator|.
name|parse
argument_list|(
name|dateStr
argument_list|)
operator|.
name|getTime
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't parse "
operator|+
name|dateStr
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|proleptic
return|;
block|}
comment|/**    * Convert epoch millis from the proleptic Gregorian calendar to the hybrid    * Julian/Gregorian.    * @param proleptic millis of epoch in the proleptic Gregorian    * @return millis of epoch in the hybrid Julian/Gregorian    */
specifier|public
specifier|static
name|long
name|convertTimeToHybrid
parameter_list|(
name|long
name|proleptic
parameter_list|)
block|{
name|long
name|hybrid
init|=
name|proleptic
decl_stmt|;
if|if
condition|(
name|proleptic
operator|<
name|SWITCHOVER_MILLIS
condition|)
block|{
name|String
name|dateStr
init|=
name|PROLEPTIC_TIME_FORMAT
operator|.
name|get
argument_list|()
operator|.
name|format
argument_list|(
operator|new
name|Date
argument_list|(
name|proleptic
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|hybrid
operator|=
name|HYBRID_TIME_FORMAT
operator|.
name|get
argument_list|()
operator|.
name|parse
argument_list|(
name|dateStr
argument_list|)
operator|.
name|getTime
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't parse "
operator|+
name|dateStr
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|hybrid
return|;
block|}
comment|/**    *    * Formats epoch day to date according to proleptic or hybrid calendar    *    * @param epochDay  epoch day    * @param useProleptic if true - uses proleptic formatter, else uses hybrid formatter    * @return formatted date    */
specifier|public
specifier|static
name|String
name|formatDate
parameter_list|(
name|long
name|epochDay
parameter_list|,
name|boolean
name|useProleptic
parameter_list|)
block|{
name|long
name|millis
init|=
name|TimeUnit
operator|.
name|DAYS
operator|.
name|toMillis
argument_list|(
name|epochDay
argument_list|)
decl_stmt|;
return|return
name|useProleptic
condition|?
name|PROLEPTIC_DATE_FORMAT
operator|.
name|get
argument_list|()
operator|.
name|format
argument_list|(
name|millis
argument_list|)
else|:
name|HYBRID_DATE_FORMAT
operator|.
name|get
argument_list|()
operator|.
name|format
argument_list|(
name|millis
argument_list|)
return|;
block|}
specifier|private
name|CalendarUtils
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

