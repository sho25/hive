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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|DateUtils
import|;
end_import

begin_class
specifier|public
class|class
name|HiveIntervalYearMonth
implements|implements
name|Comparable
argument_list|<
name|HiveIntervalYearMonth
argument_list|>
block|{
comment|// years/months represented in months
specifier|protected
name|int
name|totalMonths
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|int
name|MONTHS_PER_YEAR
init|=
literal|12
decl_stmt|;
specifier|public
name|HiveIntervalYearMonth
parameter_list|()
block|{   }
specifier|public
name|HiveIntervalYearMonth
parameter_list|(
name|int
name|years
parameter_list|,
name|int
name|months
parameter_list|)
block|{
name|set
argument_list|(
name|years
argument_list|,
name|months
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveIntervalYearMonth
parameter_list|(
name|int
name|totalMonths
parameter_list|)
block|{
name|set
argument_list|(
name|totalMonths
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveIntervalYearMonth
parameter_list|(
name|HiveIntervalYearMonth
name|hiveInterval
parameter_list|)
block|{
name|set
argument_list|(
name|hiveInterval
operator|.
name|getTotalMonths
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|//
comment|// Getters
comment|//
specifier|public
name|int
name|getYears
parameter_list|()
block|{
return|return
name|totalMonths
operator|/
name|MONTHS_PER_YEAR
return|;
block|}
specifier|public
name|int
name|getMonths
parameter_list|()
block|{
return|return
name|totalMonths
operator|%
name|MONTHS_PER_YEAR
return|;
block|}
specifier|public
name|int
name|getTotalMonths
parameter_list|()
block|{
return|return
name|totalMonths
return|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|int
name|years
parameter_list|,
name|int
name|months
parameter_list|)
block|{
name|this
operator|.
name|totalMonths
operator|=
name|months
expr_stmt|;
name|this
operator|.
name|totalMonths
operator|+=
name|years
operator|*
name|MONTHS_PER_YEAR
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|int
name|totalMonths
parameter_list|)
block|{
name|this
operator|.
name|totalMonths
operator|=
name|totalMonths
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|HiveIntervalYearMonth
name|other
parameter_list|)
block|{
name|set
argument_list|(
name|other
operator|.
name|getTotalMonths
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveIntervalYearMonth
name|negate
parameter_list|()
block|{
return|return
operator|new
name|HiveIntervalYearMonth
argument_list|(
operator|-
name|getTotalMonths
argument_list|()
argument_list|)
return|;
block|}
comment|//
comment|// Comparison
comment|//
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|HiveIntervalYearMonth
name|other
parameter_list|)
block|{
name|int
name|cmp
init|=
name|this
operator|.
name|getTotalMonths
argument_list|()
operator|-
name|other
operator|.
name|getTotalMonths
argument_list|()
decl_stmt|;
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
name|HiveIntervalYearMonth
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
name|HiveIntervalYearMonth
operator|)
name|obj
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
name|totalMonths
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|yearMonthSignStr
init|=
name|totalMonths
operator|>=
literal|0
condition|?
literal|""
else|:
literal|"-"
decl_stmt|;
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s%d-%d"
argument_list|,
name|yearMonthSignStr
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|getYears
argument_list|()
argument_list|)
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|getMonths
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HiveIntervalYearMonth
name|valueOf
parameter_list|(
name|String
name|strVal
parameter_list|)
block|{
name|HiveIntervalYearMonth
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
literal|"Interval year-month string was null"
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
name|years
init|=
name|sign
operator|*
name|DateUtils
operator|.
name|parseNumericValueWithRange
argument_list|(
literal|"year"
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
name|months
init|=
call|(
name|byte
call|)
argument_list|(
name|sign
operator|*
name|DateUtils
operator|.
name|parseNumericValueWithRange
argument_list|(
literal|"month"
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
literal|11
argument_list|)
argument_list|)
decl_stmt|;
name|result
operator|=
operator|new
name|HiveIntervalYearMonth
argument_list|(
name|years
argument_list|,
name|months
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
literal|"Error parsing interval year-month string: "
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
literal|"Interval string does not match year-month format of 'y-m': "
operator|+
name|strVal
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
comment|// Simple pattern: Y-M
specifier|private
specifier|final
specifier|static
name|String
name|PARSE_PATTERN
init|=
literal|"([+|-])?(\\d+)-(\\d+)"
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

