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
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|lang3
operator|.
name|builder
operator|.
name|HashCodeBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|IntervalDayTimeUtils
import|;
end_import

begin_comment
comment|/**  * Day-time interval type representing an offset in days/hours/minutes/seconds,  * with nanosecond precision.  * 1 day = 24 hours = 1440 minutes = 86400 seconds  */
end_comment

begin_class
specifier|public
class|class
name|HiveIntervalDayTime
implements|implements
name|Comparable
argument_list|<
name|HiveIntervalDayTime
argument_list|>
block|{
comment|// days/hours/minutes/seconds all represented as seconds
specifier|protected
name|long
name|totalSeconds
decl_stmt|;
specifier|protected
name|int
name|nanos
decl_stmt|;
specifier|public
name|HiveIntervalDayTime
parameter_list|()
block|{   }
specifier|public
name|HiveIntervalDayTime
parameter_list|(
name|int
name|days
parameter_list|,
name|int
name|hours
parameter_list|,
name|int
name|minutes
parameter_list|,
name|int
name|seconds
parameter_list|,
name|int
name|nanos
parameter_list|)
block|{
name|set
argument_list|(
name|days
argument_list|,
name|hours
argument_list|,
name|minutes
argument_list|,
name|seconds
argument_list|,
name|nanos
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveIntervalDayTime
parameter_list|(
name|long
name|seconds
parameter_list|,
name|int
name|nanos
parameter_list|)
block|{
name|set
argument_list|(
name|seconds
argument_list|,
name|nanos
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveIntervalDayTime
parameter_list|(
name|BigDecimal
name|seconds
parameter_list|)
block|{
name|set
argument_list|(
name|seconds
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveIntervalDayTime
parameter_list|(
name|HiveIntervalDayTime
name|other
parameter_list|)
block|{
name|set
argument_list|(
name|other
operator|.
name|totalSeconds
argument_list|,
name|other
operator|.
name|nanos
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getDays
parameter_list|()
block|{
return|return
operator|(
name|int
operator|)
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|toDays
argument_list|(
name|totalSeconds
argument_list|)
return|;
block|}
specifier|public
name|int
name|getHours
parameter_list|()
block|{
return|return
call|(
name|int
call|)
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|toHours
argument_list|(
name|totalSeconds
argument_list|)
operator|%
name|TimeUnit
operator|.
name|DAYS
operator|.
name|toHours
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|int
name|getMinutes
parameter_list|()
block|{
return|return
call|(
name|int
call|)
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|toMinutes
argument_list|(
name|totalSeconds
argument_list|)
operator|%
name|TimeUnit
operator|.
name|HOURS
operator|.
name|toMinutes
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|int
name|getSeconds
parameter_list|()
block|{
return|return
call|(
name|int
call|)
argument_list|(
name|totalSeconds
operator|%
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|toSeconds
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|int
name|getNanos
parameter_list|()
block|{
return|return
name|nanos
return|;
block|}
comment|/**    * Returns days/hours/minutes all converted into seconds.    * Nanos still need to be retrieved using getNanos()    * @return    */
specifier|public
name|long
name|getTotalSeconds
parameter_list|()
block|{
return|return
name|totalSeconds
return|;
block|}
comment|/**    *    * @return double representation of the interval day time, accurate to nanoseconds    */
specifier|public
name|double
name|getDouble
parameter_list|()
block|{
return|return
name|totalSeconds
operator|+
name|nanos
operator|/
literal|1000000000
return|;
block|}
comment|/**    * Ensures that the seconds and nanoseconds fields have consistent sign    */
specifier|protected
name|void
name|normalizeSecondsAndNanos
parameter_list|()
block|{
if|if
condition|(
name|totalSeconds
operator|>
literal|0
operator|&&
name|nanos
operator|<
literal|0
condition|)
block|{
operator|--
name|totalSeconds
expr_stmt|;
name|nanos
operator|+=
name|IntervalDayTimeUtils
operator|.
name|NANOS_PER_SEC
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|totalSeconds
argument_list|<
literal|0
operator|&&
name|nanos
argument_list|>
literal|0
condition|)
block|{
operator|++
name|totalSeconds
expr_stmt|;
name|nanos
operator|-=
name|IntervalDayTimeUtils
operator|.
name|NANOS_PER_SEC
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|set
parameter_list|(
name|int
name|days
parameter_list|,
name|int
name|hours
parameter_list|,
name|int
name|minutes
parameter_list|,
name|int
name|seconds
parameter_list|,
name|int
name|nanos
parameter_list|)
block|{
name|long
name|totalSeconds
init|=
name|seconds
decl_stmt|;
name|totalSeconds
operator|+=
name|TimeUnit
operator|.
name|DAYS
operator|.
name|toSeconds
argument_list|(
name|days
argument_list|)
expr_stmt|;
name|totalSeconds
operator|+=
name|TimeUnit
operator|.
name|HOURS
operator|.
name|toSeconds
argument_list|(
name|hours
argument_list|)
expr_stmt|;
name|totalSeconds
operator|+=
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|toSeconds
argument_list|(
name|minutes
argument_list|)
expr_stmt|;
name|totalSeconds
operator|+=
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toSeconds
argument_list|(
name|nanos
argument_list|)
expr_stmt|;
name|nanos
operator|=
name|nanos
operator|%
name|IntervalDayTimeUtils
operator|.
name|NANOS_PER_SEC
expr_stmt|;
name|this
operator|.
name|totalSeconds
operator|=
name|totalSeconds
expr_stmt|;
name|this
operator|.
name|nanos
operator|=
name|nanos
expr_stmt|;
name|normalizeSecondsAndNanos
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|long
name|seconds
parameter_list|,
name|int
name|nanos
parameter_list|)
block|{
name|this
operator|.
name|totalSeconds
operator|=
name|seconds
expr_stmt|;
name|this
operator|.
name|nanos
operator|=
name|nanos
expr_stmt|;
name|normalizeSecondsAndNanos
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|BigDecimal
name|totalSecondsBd
parameter_list|)
block|{
name|long
name|totalSeconds
init|=
name|totalSecondsBd
operator|.
name|longValue
argument_list|()
decl_stmt|;
name|BigDecimal
name|fractionalSecs
init|=
name|totalSecondsBd
operator|.
name|remainder
argument_list|(
name|BigDecimal
operator|.
name|ONE
argument_list|)
decl_stmt|;
name|int
name|nanos
init|=
name|fractionalSecs
operator|.
name|multiply
argument_list|(
name|IntervalDayTimeUtils
operator|.
name|NANOS_PER_SEC_BD
argument_list|)
operator|.
name|intValue
argument_list|()
decl_stmt|;
name|set
argument_list|(
name|totalSeconds
argument_list|,
name|nanos
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|HiveIntervalDayTime
name|other
parameter_list|)
block|{
name|set
argument_list|(
name|other
operator|.
name|getTotalSeconds
argument_list|()
argument_list|,
name|other
operator|.
name|getNanos
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveIntervalDayTime
name|negate
parameter_list|()
block|{
return|return
operator|new
name|HiveIntervalDayTime
argument_list|(
operator|-
name|getTotalSeconds
argument_list|()
argument_list|,
operator|-
name|getNanos
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|HiveIntervalDayTime
name|other
parameter_list|)
block|{
name|long
name|cmp
init|=
name|this
operator|.
name|totalSeconds
operator|-
name|other
operator|.
name|totalSeconds
decl_stmt|;
if|if
condition|(
name|cmp
operator|==
literal|0
condition|)
block|{
name|cmp
operator|=
name|this
operator|.
name|nanos
operator|-
name|other
operator|.
name|nanos
expr_stmt|;
block|}
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
name|cmp
operator|=
name|cmp
operator|>
literal|0
condition|?
literal|1
else|:
operator|-
literal|1
expr_stmt|;
block|}
return|return
operator|(
name|int
operator|)
name|cmp
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|HiveIntervalDayTime
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|0
operator|==
name|compareTo
argument_list|(
operator|(
name|HiveIntervalDayTime
operator|)
name|obj
argument_list|)
return|;
block|}
comment|/**    * Return a copy of this object.    */
annotation|@
name|Override
specifier|public
name|Object
name|clone
parameter_list|()
block|{
return|return
operator|new
name|HiveIntervalDayTime
argument_list|(
name|totalSeconds
argument_list|,
name|nanos
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
operator|new
name|HashCodeBuilder
argument_list|()
operator|.
name|append
argument_list|(
name|totalSeconds
argument_list|)
operator|.
name|append
argument_list|(
name|nanos
argument_list|)
operator|.
name|toHashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
comment|// If normalize() was used, then day-hour-minute-second-nanos should have the same sign.
comment|// This is currently working with that assumption.
name|boolean
name|isNegative
init|=
operator|(
name|totalSeconds
operator|<
literal|0
operator|||
name|nanos
operator|<
literal|0
operator|)
decl_stmt|;
name|String
name|daySecondSignStr
init|=
name|isNegative
condition|?
literal|"-"
else|:
literal|""
decl_stmt|;
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s%d %02d:%02d:%02d.%09d"
argument_list|,
name|daySecondSignStr
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|getDays
argument_list|()
argument_list|)
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|getHours
argument_list|()
argument_list|)
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|getMinutes
argument_list|()
argument_list|)
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|getSeconds
argument_list|()
argument_list|)
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|getNanos
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HiveIntervalDayTime
name|valueOf
parameter_list|(
name|String
name|strVal
parameter_list|)
block|{
name|HiveIntervalDayTime
name|result
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|strVal
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Interval day-time string was null"
argument_list|)
throw|;
block|}
name|Matcher
name|patternMatcher
init|=
name|PATTERN_MATCHER
operator|.
name|get
argument_list|()
decl_stmt|;
name|patternMatcher
operator|.
name|reset
argument_list|(
name|strVal
argument_list|)
expr_stmt|;
if|if
condition|(
name|patternMatcher
operator|.
name|matches
argument_list|()
condition|)
block|{
comment|// Parse out the individual parts
try|try
block|{
comment|// Sign - whether interval is positive or negative
name|int
name|sign
init|=
literal|1
decl_stmt|;
name|String
name|field
init|=
name|patternMatcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|field
operator|!=
literal|null
operator|&&
name|field
operator|.
name|equals
argument_list|(
literal|"-"
argument_list|)
condition|)
block|{
name|sign
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|int
name|days
init|=
name|sign
operator|*
name|IntervalDayTimeUtils
operator|.
name|parseNumericValueWithRange
argument_list|(
literal|"day"
argument_list|,
name|patternMatcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|,
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|byte
name|hours
init|=
call|(
name|byte
call|)
argument_list|(
name|sign
operator|*
name|IntervalDayTimeUtils
operator|.
name|parseNumericValueWithRange
argument_list|(
literal|"hour"
argument_list|,
name|patternMatcher
operator|.
name|group
argument_list|(
literal|3
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|23
argument_list|)
argument_list|)
decl_stmt|;
name|byte
name|minutes
init|=
call|(
name|byte
call|)
argument_list|(
name|sign
operator|*
name|IntervalDayTimeUtils
operator|.
name|parseNumericValueWithRange
argument_list|(
literal|"minute"
argument_list|,
name|patternMatcher
operator|.
name|group
argument_list|(
literal|4
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|59
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|seconds
init|=
literal|0
decl_stmt|;
name|int
name|nanos
init|=
literal|0
decl_stmt|;
name|field
operator|=
name|patternMatcher
operator|.
name|group
argument_list|(
literal|5
argument_list|)
expr_stmt|;
if|if
condition|(
name|field
operator|!=
literal|null
condition|)
block|{
name|BigDecimal
name|bdSeconds
init|=
operator|new
name|BigDecimal
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|bdSeconds
operator|.
name|compareTo
argument_list|(
name|IntervalDayTimeUtils
operator|.
name|MAX_INT_BD
argument_list|)
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"seconds value of "
operator|+
name|bdSeconds
operator|+
literal|" too large"
argument_list|)
throw|;
block|}
name|seconds
operator|=
name|sign
operator|*
name|bdSeconds
operator|.
name|intValue
argument_list|()
expr_stmt|;
name|nanos
operator|=
name|sign
operator|*
name|bdSeconds
operator|.
name|subtract
argument_list|(
operator|new
name|BigDecimal
argument_list|(
name|bdSeconds
operator|.
name|toBigInteger
argument_list|()
argument_list|)
argument_list|)
operator|.
name|multiply
argument_list|(
name|IntervalDayTimeUtils
operator|.
name|NANOS_PER_SEC_BD
argument_list|)
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
name|result
operator|=
operator|new
name|HiveIntervalDayTime
argument_list|(
name|days
argument_list|,
name|hours
argument_list|,
name|minutes
argument_list|,
name|seconds
argument_list|,
name|nanos
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Error parsing interval day-time string: "
operator|+
name|strVal
argument_list|,
name|err
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Interval string does not match day-time format of 'd h:m:s.n': "
operator|+
name|strVal
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
comment|// Simple pattern: D H:M:S.nnnnnnnnn
specifier|private
specifier|final
specifier|static
name|String
name|PARSE_PATTERN
init|=
literal|"([+|-])?(\\d+) (\\d+):(\\d+):((\\d+)(\\.(\\d+))?)"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|Matcher
argument_list|>
name|PATTERN_MATCHER
init|=
operator|new
name|ThreadLocal
argument_list|<
name|Matcher
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Matcher
name|initialValue
parameter_list|()
block|{
return|return
name|Pattern
operator|.
name|compile
argument_list|(
name|PARSE_PATTERN
argument_list|)
operator|.
name|matcher
argument_list|(
literal|""
argument_list|)
return|;
block|}
block|}
decl_stmt|;
block|}
end_class

end_unit

