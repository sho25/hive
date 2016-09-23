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
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
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
name|TimestampWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|Chronology
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTime
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|Period
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|ReadableDuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|chrono
operator|.
name|ISOChronology
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_comment
comment|/**  * UDFDateFloor.  *  * Abstract class that converts a timestamp to a timestamp with a given granularity.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|UDFDateFloor
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|QueryGranularity
name|granularity
decl_stmt|;
specifier|private
specifier|final
name|TimestampWritable
name|result
decl_stmt|;
specifier|public
name|UDFDateFloor
parameter_list|(
name|String
name|granularity
parameter_list|)
block|{
name|this
operator|.
name|granularity
operator|=
name|QueryGranularity
operator|.
name|fromString
argument_list|(
name|granularity
argument_list|)
expr_stmt|;
name|this
operator|.
name|result
operator|=
operator|new
name|TimestampWritable
argument_list|()
expr_stmt|;
block|}
specifier|public
name|TimestampWritable
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
specifier|final
name|long
name|originalTimestamp
init|=
name|t
operator|.
name|getTimestamp
argument_list|()
operator|.
name|getTime
argument_list|()
decl_stmt|;
comment|// default
specifier|final
name|long
name|originalTimestampUTC
init|=
operator|new
name|DateTime
argument_list|(
name|originalTimestamp
argument_list|)
operator|.
name|withZoneRetainFields
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|getMillis
argument_list|()
decl_stmt|;
comment|// default -> utc
specifier|final
name|long
name|newTimestampUTC
init|=
name|granularity
operator|.
name|truncate
argument_list|(
name|originalTimestampUTC
argument_list|)
decl_stmt|;
comment|// utc
specifier|final
name|long
name|newTimestamp
init|=
operator|new
name|DateTime
argument_list|(
name|newTimestampUTC
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|withZoneRetainFields
argument_list|(
name|DateTimeZone
operator|.
name|getDefault
argument_list|()
argument_list|)
operator|.
name|getMillis
argument_list|()
decl_stmt|;
comment|// utc -> default
name|result
operator|.
name|setTime
argument_list|(
name|newTimestamp
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/*    * This code that creates the result for the granularity functions has been brought from Druid    */
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|PeriodGranularity
argument_list|>
name|CALENDRIC_GRANULARITIES
init|=
name|ImmutableMap
operator|.
name|of
argument_list|(
literal|"YEAR"
argument_list|,
operator|new
name|PeriodGranularity
argument_list|(
operator|new
name|Period
argument_list|(
literal|"P1Y"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|"MONTH"
argument_list|,
operator|new
name|PeriodGranularity
argument_list|(
operator|new
name|Period
argument_list|(
literal|"P1M"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|"QUARTER"
argument_list|,
operator|new
name|PeriodGranularity
argument_list|(
operator|new
name|Period
argument_list|(
literal|"P3M"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|"WEEK"
argument_list|,
operator|new
name|PeriodGranularity
argument_list|(
operator|new
name|Period
argument_list|(
literal|"P1W"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|abstract
class|class
name|QueryGranularity
block|{
specifier|public
specifier|abstract
name|long
name|next
parameter_list|(
name|long
name|offset
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|long
name|truncate
parameter_list|(
name|long
name|offset
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|DateTime
name|toDateTime
parameter_list|(
name|long
name|offset
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|Iterable
argument_list|<
name|Long
argument_list|>
name|iterable
parameter_list|(
specifier|final
name|long
name|start
parameter_list|,
specifier|final
name|long
name|end
parameter_list|)
function_decl|;
specifier|public
specifier|static
name|QueryGranularity
name|fromString
parameter_list|(
name|String
name|str
parameter_list|)
block|{
name|String
name|name
init|=
name|str
operator|.
name|toUpperCase
argument_list|()
decl_stmt|;
if|if
condition|(
name|CALENDRIC_GRANULARITIES
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|CALENDRIC_GRANULARITIES
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
return|return
operator|new
name|DurationGranularity
argument_list|(
name|convertValue
argument_list|(
name|str
argument_list|)
argument_list|,
literal|0
argument_list|)
return|;
block|}
specifier|private
specifier|static
enum|enum
name|MillisIn
block|{
name|SECOND
argument_list|(
literal|1000
argument_list|)
block|,
name|MINUTE
argument_list|(
literal|60
operator|*
literal|1000
argument_list|)
block|,
name|FIFTEEN_MINUTE
argument_list|(
literal|15
operator|*
literal|60
operator|*
literal|1000
argument_list|)
block|,
name|THIRTY_MINUTE
argument_list|(
literal|30
operator|*
literal|60
operator|*
literal|1000
argument_list|)
block|,
name|HOUR
argument_list|(
literal|3600
operator|*
literal|1000
argument_list|)
block|,
name|DAY
argument_list|(
literal|24
operator|*
literal|3600
operator|*
literal|1000
argument_list|)
block|;
specifier|private
specifier|final
name|long
name|millis
decl_stmt|;
name|MillisIn
parameter_list|(
specifier|final
name|long
name|millis
parameter_list|)
block|{
name|this
operator|.
name|millis
operator|=
name|millis
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|long
name|convertValue
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|instanceof
name|String
condition|)
block|{
return|return
name|MillisIn
operator|.
name|valueOf
argument_list|(
operator|(
operator|(
name|String
operator|)
name|o
operator|)
operator|.
name|toUpperCase
argument_list|()
argument_list|)
operator|.
name|millis
return|;
block|}
elseif|else
if|if
condition|(
name|o
operator|instanceof
name|ReadableDuration
condition|)
block|{
return|return
operator|(
operator|(
name|ReadableDuration
operator|)
name|o
operator|)
operator|.
name|getMillis
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|o
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|o
operator|)
operator|.
name|longValue
argument_list|()
return|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Granularity not recognized"
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
specifier|abstract
class|class
name|BaseQueryGranularity
extends|extends
name|QueryGranularity
block|{
specifier|public
specifier|abstract
name|long
name|next
parameter_list|(
name|long
name|offset
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|long
name|truncate
parameter_list|(
name|long
name|offset
parameter_list|)
function_decl|;
specifier|public
name|DateTime
name|toDateTime
parameter_list|(
name|long
name|offset
parameter_list|)
block|{
return|return
operator|new
name|DateTime
argument_list|(
name|offset
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
return|;
block|}
specifier|public
name|Iterable
argument_list|<
name|Long
argument_list|>
name|iterable
parameter_list|(
specifier|final
name|long
name|start
parameter_list|,
specifier|final
name|long
name|end
parameter_list|)
block|{
return|return
operator|new
name|Iterable
argument_list|<
name|Long
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Long
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|Iterator
argument_list|<
name|Long
argument_list|>
argument_list|()
block|{
name|long
name|curr
init|=
name|truncate
argument_list|(
name|start
argument_list|)
decl_stmt|;
name|long
name|next
init|=
name|BaseQueryGranularity
operator|.
name|this
operator|.
name|next
argument_list|(
name|curr
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|curr
operator|<
name|end
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|next
parameter_list|()
block|{
if|if
condition|(
operator|!
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|()
throw|;
block|}
name|long
name|retVal
init|=
name|curr
decl_stmt|;
name|curr
operator|=
name|next
expr_stmt|;
name|next
operator|=
name|BaseQueryGranularity
operator|.
name|this
operator|.
name|next
argument_list|(
name|curr
argument_list|)
expr_stmt|;
return|return
name|retVal
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
return|;
block|}
block|}
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|PeriodGranularity
extends|extends
name|BaseQueryGranularity
block|{
specifier|private
specifier|final
name|Period
name|period
decl_stmt|;
specifier|private
specifier|final
name|Chronology
name|chronology
decl_stmt|;
specifier|private
specifier|final
name|long
name|origin
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|hasOrigin
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isCompound
decl_stmt|;
specifier|public
name|PeriodGranularity
parameter_list|(
name|Period
name|period
parameter_list|,
name|DateTime
name|origin
parameter_list|,
name|DateTimeZone
name|tz
parameter_list|)
block|{
name|this
operator|.
name|period
operator|=
name|period
expr_stmt|;
name|this
operator|.
name|chronology
operator|=
name|tz
operator|==
literal|null
condition|?
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
else|:
name|ISOChronology
operator|.
name|getInstance
argument_list|(
name|tz
argument_list|)
expr_stmt|;
if|if
condition|(
name|origin
operator|==
literal|null
condition|)
block|{
comment|// default to origin in given time zone when aligning multi-period granularities
name|this
operator|.
name|origin
operator|=
operator|new
name|DateTime
argument_list|(
literal|0
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|withZoneRetainFields
argument_list|(
name|chronology
operator|.
name|getZone
argument_list|()
argument_list|)
operator|.
name|getMillis
argument_list|()
expr_stmt|;
name|this
operator|.
name|hasOrigin
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|origin
operator|=
name|origin
operator|.
name|getMillis
argument_list|()
expr_stmt|;
name|this
operator|.
name|hasOrigin
operator|=
literal|true
expr_stmt|;
block|}
name|this
operator|.
name|isCompound
operator|=
name|isCompoundPeriod
argument_list|(
name|period
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|DateTime
name|toDateTime
parameter_list|(
name|long
name|t
parameter_list|)
block|{
return|return
operator|new
name|DateTime
argument_list|(
name|t
argument_list|,
name|chronology
operator|.
name|getZone
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|next
parameter_list|(
name|long
name|t
parameter_list|)
block|{
return|return
name|chronology
operator|.
name|add
argument_list|(
name|period
argument_list|,
name|t
argument_list|,
literal|1
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|truncate
parameter_list|(
name|long
name|t
parameter_list|)
block|{
if|if
condition|(
name|isCompound
condition|)
block|{
try|try
block|{
return|return
name|truncateMillisPeriod
argument_list|(
name|t
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
return|return
name|truncateCompoundPeriod
argument_list|(
name|t
argument_list|)
return|;
block|}
block|}
specifier|final
name|int
name|years
init|=
name|period
operator|.
name|getYears
argument_list|()
decl_stmt|;
if|if
condition|(
name|years
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|years
operator|>
literal|1
operator|||
name|hasOrigin
condition|)
block|{
name|int
name|y
init|=
name|chronology
operator|.
name|years
argument_list|()
operator|.
name|getDifference
argument_list|(
name|t
argument_list|,
name|origin
argument_list|)
decl_stmt|;
name|y
operator|-=
name|y
operator|%
name|years
expr_stmt|;
name|long
name|tt
init|=
name|chronology
operator|.
name|years
argument_list|()
operator|.
name|add
argument_list|(
name|origin
argument_list|,
name|y
argument_list|)
decl_stmt|;
comment|// always round down to the previous period (for timestamps prior to origin)
if|if
condition|(
name|t
operator|<
name|tt
condition|)
name|t
operator|=
name|chronology
operator|.
name|years
argument_list|()
operator|.
name|add
argument_list|(
name|tt
argument_list|,
operator|-
name|years
argument_list|)
expr_stmt|;
else|else
name|t
operator|=
name|tt
expr_stmt|;
return|return
name|t
return|;
block|}
else|else
block|{
return|return
name|chronology
operator|.
name|year
argument_list|()
operator|.
name|roundFloor
argument_list|(
name|t
argument_list|)
return|;
block|}
block|}
specifier|final
name|int
name|months
init|=
name|period
operator|.
name|getMonths
argument_list|()
decl_stmt|;
if|if
condition|(
name|months
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|months
operator|>
literal|1
operator|||
name|hasOrigin
condition|)
block|{
name|int
name|m
init|=
name|chronology
operator|.
name|months
argument_list|()
operator|.
name|getDifference
argument_list|(
name|t
argument_list|,
name|origin
argument_list|)
decl_stmt|;
name|m
operator|-=
name|m
operator|%
name|months
expr_stmt|;
name|long
name|tt
init|=
name|chronology
operator|.
name|months
argument_list|()
operator|.
name|add
argument_list|(
name|origin
argument_list|,
name|m
argument_list|)
decl_stmt|;
comment|// always round down to the previous period (for timestamps prior to origin)
if|if
condition|(
name|t
operator|<
name|tt
condition|)
name|t
operator|=
name|chronology
operator|.
name|months
argument_list|()
operator|.
name|add
argument_list|(
name|tt
argument_list|,
operator|-
name|months
argument_list|)
expr_stmt|;
else|else
name|t
operator|=
name|tt
expr_stmt|;
return|return
name|t
return|;
block|}
else|else
block|{
return|return
name|chronology
operator|.
name|monthOfYear
argument_list|()
operator|.
name|roundFloor
argument_list|(
name|t
argument_list|)
return|;
block|}
block|}
specifier|final
name|int
name|weeks
init|=
name|period
operator|.
name|getWeeks
argument_list|()
decl_stmt|;
if|if
condition|(
name|weeks
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|weeks
operator|>
literal|1
operator|||
name|hasOrigin
condition|)
block|{
comment|// align on multiples from origin
name|int
name|w
init|=
name|chronology
operator|.
name|weeks
argument_list|()
operator|.
name|getDifference
argument_list|(
name|t
argument_list|,
name|origin
argument_list|)
decl_stmt|;
name|w
operator|-=
name|w
operator|%
name|weeks
expr_stmt|;
name|long
name|tt
init|=
name|chronology
operator|.
name|weeks
argument_list|()
operator|.
name|add
argument_list|(
name|origin
argument_list|,
name|w
argument_list|)
decl_stmt|;
comment|// always round down to the previous period (for timestamps prior to origin)
if|if
condition|(
name|t
operator|<
name|tt
condition|)
name|t
operator|=
name|chronology
operator|.
name|weeks
argument_list|()
operator|.
name|add
argument_list|(
name|tt
argument_list|,
operator|-
name|weeks
argument_list|)
expr_stmt|;
else|else
name|t
operator|=
name|tt
expr_stmt|;
return|return
name|t
return|;
block|}
else|else
block|{
name|t
operator|=
name|chronology
operator|.
name|dayOfWeek
argument_list|()
operator|.
name|roundFloor
argument_list|(
name|t
argument_list|)
expr_stmt|;
comment|// default to Monday as beginning of the week
return|return
name|chronology
operator|.
name|dayOfWeek
argument_list|()
operator|.
name|set
argument_list|(
name|t
argument_list|,
literal|1
argument_list|)
return|;
block|}
block|}
specifier|final
name|int
name|days
init|=
name|period
operator|.
name|getDays
argument_list|()
decl_stmt|;
if|if
condition|(
name|days
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|days
operator|>
literal|1
operator|||
name|hasOrigin
condition|)
block|{
comment|// align on multiples from origin
name|int
name|d
init|=
name|chronology
operator|.
name|days
argument_list|()
operator|.
name|getDifference
argument_list|(
name|t
argument_list|,
name|origin
argument_list|)
decl_stmt|;
name|d
operator|-=
name|d
operator|%
name|days
expr_stmt|;
name|long
name|tt
init|=
name|chronology
operator|.
name|days
argument_list|()
operator|.
name|add
argument_list|(
name|origin
argument_list|,
name|d
argument_list|)
decl_stmt|;
comment|// always round down to the previous period (for timestamps prior to origin)
if|if
condition|(
name|t
operator|<
name|tt
condition|)
name|t
operator|=
name|chronology
operator|.
name|days
argument_list|()
operator|.
name|add
argument_list|(
name|tt
argument_list|,
operator|-
name|days
argument_list|)
expr_stmt|;
else|else
name|t
operator|=
name|tt
expr_stmt|;
return|return
name|t
return|;
block|}
else|else
block|{
name|t
operator|=
name|chronology
operator|.
name|hourOfDay
argument_list|()
operator|.
name|roundFloor
argument_list|(
name|t
argument_list|)
expr_stmt|;
return|return
name|chronology
operator|.
name|hourOfDay
argument_list|()
operator|.
name|set
argument_list|(
name|t
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
specifier|final
name|int
name|hours
init|=
name|period
operator|.
name|getHours
argument_list|()
decl_stmt|;
if|if
condition|(
name|hours
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|hours
operator|>
literal|1
operator|||
name|hasOrigin
condition|)
block|{
comment|// align on multiples from origin
name|long
name|h
init|=
name|chronology
operator|.
name|hours
argument_list|()
operator|.
name|getDifferenceAsLong
argument_list|(
name|t
argument_list|,
name|origin
argument_list|)
decl_stmt|;
name|h
operator|-=
name|h
operator|%
name|hours
expr_stmt|;
name|long
name|tt
init|=
name|chronology
operator|.
name|hours
argument_list|()
operator|.
name|add
argument_list|(
name|origin
argument_list|,
name|h
argument_list|)
decl_stmt|;
comment|// always round down to the previous period (for timestamps prior to origin)
if|if
condition|(
name|t
operator|<
name|tt
condition|)
name|t
operator|=
name|chronology
operator|.
name|hours
argument_list|()
operator|.
name|add
argument_list|(
name|tt
argument_list|,
operator|-
name|hours
argument_list|)
expr_stmt|;
else|else
name|t
operator|=
name|tt
expr_stmt|;
return|return
name|t
return|;
block|}
else|else
block|{
name|t
operator|=
name|chronology
operator|.
name|minuteOfHour
argument_list|()
operator|.
name|roundFloor
argument_list|(
name|t
argument_list|)
expr_stmt|;
return|return
name|chronology
operator|.
name|minuteOfHour
argument_list|()
operator|.
name|set
argument_list|(
name|t
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
specifier|final
name|int
name|minutes
init|=
name|period
operator|.
name|getMinutes
argument_list|()
decl_stmt|;
if|if
condition|(
name|minutes
operator|>
literal|0
condition|)
block|{
comment|// align on multiples from origin
if|if
condition|(
name|minutes
operator|>
literal|1
operator|||
name|hasOrigin
condition|)
block|{
name|long
name|m
init|=
name|chronology
operator|.
name|minutes
argument_list|()
operator|.
name|getDifferenceAsLong
argument_list|(
name|t
argument_list|,
name|origin
argument_list|)
decl_stmt|;
name|m
operator|-=
name|m
operator|%
name|minutes
expr_stmt|;
name|long
name|tt
init|=
name|chronology
operator|.
name|minutes
argument_list|()
operator|.
name|add
argument_list|(
name|origin
argument_list|,
name|m
argument_list|)
decl_stmt|;
comment|// always round down to the previous period (for timestamps prior to origin)
if|if
condition|(
name|t
operator|<
name|tt
condition|)
name|t
operator|=
name|chronology
operator|.
name|minutes
argument_list|()
operator|.
name|add
argument_list|(
name|tt
argument_list|,
operator|-
name|minutes
argument_list|)
expr_stmt|;
else|else
name|t
operator|=
name|tt
expr_stmt|;
return|return
name|t
return|;
block|}
else|else
block|{
name|t
operator|=
name|chronology
operator|.
name|secondOfMinute
argument_list|()
operator|.
name|roundFloor
argument_list|(
name|t
argument_list|)
expr_stmt|;
return|return
name|chronology
operator|.
name|secondOfMinute
argument_list|()
operator|.
name|set
argument_list|(
name|t
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
specifier|final
name|int
name|seconds
init|=
name|period
operator|.
name|getSeconds
argument_list|()
decl_stmt|;
if|if
condition|(
name|seconds
operator|>
literal|0
condition|)
block|{
comment|// align on multiples from origin
if|if
condition|(
name|seconds
operator|>
literal|1
operator|||
name|hasOrigin
condition|)
block|{
name|long
name|s
init|=
name|chronology
operator|.
name|seconds
argument_list|()
operator|.
name|getDifferenceAsLong
argument_list|(
name|t
argument_list|,
name|origin
argument_list|)
decl_stmt|;
name|s
operator|-=
name|s
operator|%
name|seconds
expr_stmt|;
name|long
name|tt
init|=
name|chronology
operator|.
name|seconds
argument_list|()
operator|.
name|add
argument_list|(
name|origin
argument_list|,
name|s
argument_list|)
decl_stmt|;
comment|// always round down to the previous period (for timestamps prior to origin)
if|if
condition|(
name|t
operator|<
name|tt
condition|)
name|t
operator|=
name|chronology
operator|.
name|seconds
argument_list|()
operator|.
name|add
argument_list|(
name|tt
argument_list|,
operator|-
name|seconds
argument_list|)
expr_stmt|;
else|else
name|t
operator|=
name|tt
expr_stmt|;
return|return
name|t
return|;
block|}
else|else
block|{
return|return
name|chronology
operator|.
name|millisOfSecond
argument_list|()
operator|.
name|set
argument_list|(
name|t
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
specifier|final
name|int
name|millis
init|=
name|period
operator|.
name|getMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|millis
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|millis
operator|>
literal|1
condition|)
block|{
name|long
name|ms
init|=
name|chronology
operator|.
name|millis
argument_list|()
operator|.
name|getDifferenceAsLong
argument_list|(
name|t
argument_list|,
name|origin
argument_list|)
decl_stmt|;
name|ms
operator|-=
name|ms
operator|%
name|millis
expr_stmt|;
name|long
name|tt
init|=
name|chronology
operator|.
name|millis
argument_list|()
operator|.
name|add
argument_list|(
name|origin
argument_list|,
name|ms
argument_list|)
decl_stmt|;
comment|// always round down to the previous period (for timestamps prior to origin)
if|if
condition|(
name|t
operator|<
name|tt
condition|)
name|t
operator|=
name|chronology
operator|.
name|millis
argument_list|()
operator|.
name|add
argument_list|(
name|tt
argument_list|,
operator|-
name|millis
argument_list|)
expr_stmt|;
else|else
name|t
operator|=
name|tt
expr_stmt|;
return|return
name|t
return|;
block|}
else|else
block|{
return|return
name|t
return|;
block|}
block|}
return|return
name|t
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isCompoundPeriod
parameter_list|(
name|Period
name|period
parameter_list|)
block|{
name|int
index|[]
name|values
init|=
name|period
operator|.
name|getValues
argument_list|()
decl_stmt|;
name|boolean
name|single
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|v
range|:
name|values
control|)
block|{
if|if
condition|(
name|v
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|single
condition|)
return|return
literal|true
return|;
name|single
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|long
name|truncateCompoundPeriod
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|long
name|current
decl_stmt|;
if|if
condition|(
name|t
operator|>=
name|origin
condition|)
block|{
name|long
name|next
init|=
name|origin
decl_stmt|;
do|do
block|{
name|current
operator|=
name|next
expr_stmt|;
name|next
operator|=
name|chronology
operator|.
name|add
argument_list|(
name|period
argument_list|,
name|current
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|t
operator|>=
name|next
condition|)
do|;
block|}
else|else
block|{
name|current
operator|=
name|origin
expr_stmt|;
do|do
block|{
name|current
operator|=
name|chronology
operator|.
name|add
argument_list|(
name|period
argument_list|,
name|current
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|t
operator|<
name|current
condition|)
do|;
block|}
return|return
name|current
return|;
block|}
specifier|private
name|long
name|truncateMillisPeriod
parameter_list|(
specifier|final
name|long
name|t
parameter_list|)
block|{
comment|// toStandardDuration assumes days are always 24h, and hours are always 60 minutes,
comment|// which may not always be the case, e.g if there are daylight saving changes.
if|if
condition|(
name|chronology
operator|.
name|days
argument_list|()
operator|.
name|isPrecise
argument_list|()
operator|&&
name|chronology
operator|.
name|hours
argument_list|()
operator|.
name|isPrecise
argument_list|()
condition|)
block|{
specifier|final
name|long
name|millis
init|=
name|period
operator|.
name|toStandardDuration
argument_list|()
operator|.
name|getMillis
argument_list|()
decl_stmt|;
name|long
name|offset
init|=
name|t
operator|%
name|millis
operator|-
name|origin
operator|%
name|millis
decl_stmt|;
if|if
condition|(
name|offset
operator|<
literal|0
condition|)
block|{
name|offset
operator|+=
name|millis
expr_stmt|;
block|}
return|return
name|t
operator|-
name|offset
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Period cannot be converted to milliseconds as some fields mays vary in length with chronology "
operator|+
name|chronology
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|PeriodGranularity
name|that
init|=
operator|(
name|PeriodGranularity
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|hasOrigin
operator|!=
name|that
operator|.
name|hasOrigin
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|origin
operator|!=
name|that
operator|.
name|origin
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|chronology
operator|.
name|equals
argument_list|(
name|that
operator|.
name|chronology
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|period
operator|.
name|equals
argument_list|(
name|that
operator|.
name|period
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|period
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|chronology
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
call|(
name|int
call|)
argument_list|(
name|origin
operator|^
operator|(
name|origin
operator|>>>
literal|32
operator|)
argument_list|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|hasOrigin
condition|?
literal|1
else|:
literal|0
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"PeriodGranularity{"
operator|+
literal|"period="
operator|+
name|period
operator|+
literal|", timeZone="
operator|+
name|chronology
operator|.
name|getZone
argument_list|()
operator|+
literal|", origin="
operator|+
operator|(
name|hasOrigin
condition|?
name|origin
else|:
literal|"null"
operator|)
operator|+
literal|'}'
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|DurationGranularity
extends|extends
name|BaseQueryGranularity
block|{
specifier|private
specifier|final
name|long
name|length
decl_stmt|;
specifier|private
specifier|final
name|long
name|origin
decl_stmt|;
specifier|public
name|DurationGranularity
parameter_list|(
name|long
name|millis
parameter_list|,
name|long
name|origin
parameter_list|)
block|{
name|this
operator|.
name|length
operator|=
name|millis
expr_stmt|;
name|this
operator|.
name|origin
operator|=
name|origin
operator|%
name|length
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|next
parameter_list|(
name|long
name|t
parameter_list|)
block|{
return|return
name|t
operator|+
name|getDurationMillis
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|truncate
parameter_list|(
specifier|final
name|long
name|t
parameter_list|)
block|{
specifier|final
name|long
name|duration
init|=
name|getDurationMillis
argument_list|()
decl_stmt|;
name|long
name|offset
init|=
name|t
operator|%
name|duration
operator|-
name|origin
operator|%
name|duration
decl_stmt|;
if|if
condition|(
name|offset
operator|<
literal|0
condition|)
block|{
name|offset
operator|+=
name|duration
expr_stmt|;
block|}
return|return
name|t
operator|-
name|offset
return|;
block|}
specifier|public
name|long
name|getDurationMillis
parameter_list|()
block|{
return|return
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|DurationGranularity
name|that
init|=
operator|(
name|DurationGranularity
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|length
operator|!=
name|that
operator|.
name|length
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|origin
operator|!=
name|that
operator|.
name|origin
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
call|(
name|int
call|)
argument_list|(
name|length
operator|^
operator|(
name|length
operator|>>>
literal|32
operator|)
argument_list|)
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
call|(
name|int
call|)
argument_list|(
name|origin
operator|^
operator|(
name|origin
operator|>>>
literal|32
operator|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"DurationGranularity{"
operator|+
literal|"length="
operator|+
name|length
operator|+
literal|", origin="
operator|+
name|origin
operator|+
literal|'}'
return|;
block|}
block|}
block|}
end_class

end_unit

