begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|parquet
operator|.
name|timestamp
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

begin_import
import|import
name|jodd
operator|.
name|datetime
operator|.
name|JDateTime
import|;
end_import

begin_comment
comment|/**  * Utilities for converting from java.sql.Timestamp to parquet timestamp.  * This utilizes the Jodd library.  */
end_comment

begin_class
specifier|public
class|class
name|NanoTimeUtils
block|{
specifier|static
specifier|final
name|long
name|NANOS_PER_HOUR
init|=
name|TimeUnit
operator|.
name|HOURS
operator|.
name|toNanos
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|long
name|NANOS_PER_MINUTE
init|=
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|toNanos
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|long
name|NANOS_PER_SECOND
init|=
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|toNanos
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|long
name|NANOS_PER_DAY
init|=
name|TimeUnit
operator|.
name|DAYS
operator|.
name|toNanos
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|Calendar
argument_list|>
name|parquetGMTCalendar
init|=
operator|new
name|ThreadLocal
argument_list|<
name|Calendar
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|Calendar
argument_list|>
name|parquetLocalCalendar
init|=
operator|new
name|ThreadLocal
argument_list|<
name|Calendar
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Calendar
name|getGMTCalendar
parameter_list|()
block|{
comment|//Calendar.getInstance calculates the current-time needlessly, so cache an instance.
if|if
condition|(
name|parquetGMTCalendar
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|)
block|{
name|parquetGMTCalendar
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|getInstance
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"GMT"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|parquetGMTCalendar
operator|.
name|get
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|Calendar
name|getLocalCalendar
parameter_list|()
block|{
if|if
condition|(
name|parquetLocalCalendar
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|)
block|{
name|parquetLocalCalendar
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|parquetLocalCalendar
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Calendar
name|getCalendar
parameter_list|(
name|boolean
name|skipConversion
parameter_list|)
block|{
name|Calendar
name|calendar
init|=
name|skipConversion
condition|?
name|getLocalCalendar
argument_list|()
else|:
name|getGMTCalendar
argument_list|()
decl_stmt|;
name|calendar
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// Reset all fields before reusing this instance
return|return
name|calendar
return|;
block|}
specifier|public
specifier|static
name|NanoTime
name|getNanoTime
parameter_list|(
name|Timestamp
name|ts
parameter_list|,
name|boolean
name|skipConversion
parameter_list|)
block|{
name|Calendar
name|calendar
init|=
name|getCalendar
argument_list|(
name|skipConversion
argument_list|)
decl_stmt|;
name|calendar
operator|.
name|setTime
argument_list|(
name|ts
argument_list|)
expr_stmt|;
name|int
name|year
init|=
name|calendar
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|YEAR
argument_list|)
decl_stmt|;
if|if
condition|(
name|calendar
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|ERA
argument_list|)
operator|==
name|GregorianCalendar
operator|.
name|BC
condition|)
block|{
name|year
operator|=
literal|1
operator|-
name|year
expr_stmt|;
block|}
name|JDateTime
name|jDateTime
init|=
operator|new
name|JDateTime
argument_list|(
name|year
argument_list|,
name|calendar
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|MONTH
argument_list|)
operator|+
literal|1
argument_list|,
comment|//java calendar index starting at 1.
name|calendar
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|days
init|=
name|jDateTime
operator|.
name|getJulianDayNumber
argument_list|()
decl_stmt|;
name|long
name|hour
init|=
name|calendar
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|HOUR_OF_DAY
argument_list|)
decl_stmt|;
name|long
name|minute
init|=
name|calendar
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|MINUTE
argument_list|)
decl_stmt|;
name|long
name|second
init|=
name|calendar
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|SECOND
argument_list|)
decl_stmt|;
name|long
name|nanos
init|=
name|ts
operator|.
name|getNanos
argument_list|()
decl_stmt|;
name|long
name|nanosOfDay
init|=
name|nanos
operator|+
name|NANOS_PER_SECOND
operator|*
name|second
operator|+
name|NANOS_PER_MINUTE
operator|*
name|minute
operator|+
name|NANOS_PER_HOUR
operator|*
name|hour
decl_stmt|;
return|return
operator|new
name|NanoTime
argument_list|(
name|days
argument_list|,
name|nanosOfDay
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Timestamp
name|getTimestamp
parameter_list|(
name|NanoTime
name|nt
parameter_list|,
name|boolean
name|skipConversion
parameter_list|)
block|{
name|int
name|julianDay
init|=
name|nt
operator|.
name|getJulianDay
argument_list|()
decl_stmt|;
name|long
name|nanosOfDay
init|=
name|nt
operator|.
name|getTimeOfDayNanos
argument_list|()
decl_stmt|;
name|long
name|remainder
init|=
name|nanosOfDay
decl_stmt|;
name|julianDay
operator|+=
name|remainder
operator|/
name|NANOS_PER_DAY
expr_stmt|;
name|remainder
operator|%=
name|NANOS_PER_DAY
expr_stmt|;
if|if
condition|(
name|remainder
operator|<
literal|0
condition|)
block|{
name|remainder
operator|+=
name|NANOS_PER_DAY
expr_stmt|;
name|julianDay
operator|--
expr_stmt|;
block|}
name|JDateTime
name|jDateTime
init|=
operator|new
name|JDateTime
argument_list|(
operator|(
name|double
operator|)
name|julianDay
argument_list|)
decl_stmt|;
name|Calendar
name|calendar
init|=
name|getCalendar
argument_list|(
name|skipConversion
argument_list|)
decl_stmt|;
name|calendar
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|YEAR
argument_list|,
name|jDateTime
operator|.
name|getYear
argument_list|()
argument_list|)
expr_stmt|;
name|calendar
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MONTH
argument_list|,
name|jDateTime
operator|.
name|getMonth
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|//java calendar index starting at 1.
name|calendar
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|,
name|jDateTime
operator|.
name|getDay
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|hour
init|=
call|(
name|int
call|)
argument_list|(
name|remainder
operator|/
operator|(
name|NANOS_PER_HOUR
operator|)
argument_list|)
decl_stmt|;
name|remainder
operator|=
name|remainder
operator|%
operator|(
name|NANOS_PER_HOUR
operator|)
expr_stmt|;
name|int
name|minutes
init|=
call|(
name|int
call|)
argument_list|(
name|remainder
operator|/
operator|(
name|NANOS_PER_MINUTE
operator|)
argument_list|)
decl_stmt|;
name|remainder
operator|=
name|remainder
operator|%
operator|(
name|NANOS_PER_MINUTE
operator|)
expr_stmt|;
name|int
name|seconds
init|=
call|(
name|int
call|)
argument_list|(
name|remainder
operator|/
operator|(
name|NANOS_PER_SECOND
operator|)
argument_list|)
decl_stmt|;
name|long
name|nanos
init|=
name|remainder
operator|%
name|NANOS_PER_SECOND
decl_stmt|;
name|calendar
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|HOUR_OF_DAY
argument_list|,
name|hour
argument_list|)
expr_stmt|;
name|calendar
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MINUTE
argument_list|,
name|minutes
argument_list|)
expr_stmt|;
name|calendar
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|SECOND
argument_list|,
name|seconds
argument_list|)
expr_stmt|;
name|Timestamp
name|ts
init|=
operator|new
name|Timestamp
argument_list|(
name|calendar
operator|.
name|getTimeInMillis
argument_list|()
argument_list|)
decl_stmt|;
name|ts
operator|.
name|setNanos
argument_list|(
operator|(
name|int
operator|)
name|nanos
argument_list|)
expr_stmt|;
return|return
name|ts
return|;
block|}
block|}
end_class

end_unit

