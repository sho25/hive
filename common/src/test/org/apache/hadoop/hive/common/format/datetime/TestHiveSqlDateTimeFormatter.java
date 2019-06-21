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
name|format
operator|.
name|datetime
package|;
end_package

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|tools
operator|.
name|javac
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|common
operator|.
name|type
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
name|common
operator|.
name|type
operator|.
name|Timestamp
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|LocalDate
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|LocalDateTime
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|ZoneOffset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|format
operator|.
name|DateTimeFormatter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|format
operator|.
name|DateTimeFormatterBuilder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|format
operator|.
name|ResolverStyle
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|format
operator|.
name|SignStyle
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|TemporalField
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
operator|.
name|DAY_OF_MONTH
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
operator|.
name|HOUR_OF_DAY
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
operator|.
name|MINUTE_OF_HOUR
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
operator|.
name|MONTH_OF_YEAR
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
operator|.
name|SECOND_OF_MINUTE
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|time
operator|.
name|temporal
operator|.
name|ChronoField
operator|.
name|YEAR
import|;
end_import

begin_comment
comment|/**  * Tests HiveSqlDateTimeFormatter.  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveSqlDateTimeFormatter
extends|extends
name|TestCase
block|{
specifier|private
name|HiveSqlDateTimeFormatter
name|formatter
decl_stmt|;
specifier|public
name|void
name|testSetPattern
parameter_list|()
block|{
name|verifyPatternParsing
argument_list|(
literal|" ---yyyy-\'-:-  -,.;/MM-dd--"
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|List
operator|.
name|of
argument_list|(
literal|null
argument_list|,
comment|// represents separator, which has no temporal field
name|ChronoField
operator|.
name|YEAR
argument_list|,
literal|null
argument_list|,
name|ChronoField
operator|.
name|MONTH_OF_YEAR
argument_list|,
literal|null
argument_list|,
name|ChronoField
operator|.
name|DAY_OF_MONTH
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|verifyPatternParsing
argument_list|(
literal|"ymmdddhh24::mi:ss A.M. pm"
argument_list|,
literal|25
argument_list|,
literal|"ymmdddhh24::mi:ss A.M. pm"
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|List
operator|.
name|of
argument_list|(
name|ChronoField
operator|.
name|YEAR
argument_list|,
name|ChronoField
operator|.
name|MONTH_OF_YEAR
argument_list|,
name|ChronoField
operator|.
name|DAY_OF_YEAR
argument_list|,
name|ChronoField
operator|.
name|HOUR_OF_DAY
argument_list|,
literal|null
argument_list|,
name|ChronoField
operator|.
name|MINUTE_OF_HOUR
argument_list|,
literal|null
argument_list|,
name|ChronoField
operator|.
name|SECOND_OF_MINUTE
argument_list|,
literal|null
argument_list|,
name|ChronoField
operator|.
name|AMPM_OF_DAY
argument_list|,
literal|null
argument_list|,
name|ChronoField
operator|.
name|AMPM_OF_DAY
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testSetPatternWithBadPatterns
parameter_list|()
block|{
name|verifyBadPattern
argument_list|(
literal|"eyyyy-ddd"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"1yyyy-mm-dd"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|//duplicates
name|verifyBadPattern
argument_list|(
literal|"yyyy Y"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy R"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|//missing year or (month + dayofmonth or dayofyear)
name|verifyBadPattern
argument_list|(
literal|"yyyy"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-dd"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"mm-dd"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"ddd"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-MM-DDD"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm-DD DDD"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm-dd HH24 HH12"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm-dd HH24 AM"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm-dd HH24 SSSSS"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm-dd HH12 SSSSS"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm-dd SSSSS AM"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm-dd MI SSSSS"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm-dd SS SSSSS"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"tzm"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"tzh"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testFormatTimestamp
parameter_list|()
block|{
name|checkFormatTs
argument_list|(
literal|"rr rrrr ddd"
argument_list|,
literal|"2018-01-03 00:00:00"
argument_list|,
literal|"18 2018 003"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"yyyy-mm-ddtsssss.ff4z"
argument_list|,
literal|"2018-02-03 00:00:10.777777777"
argument_list|,
literal|"2018-02-03T00010.7777Z"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"hh24:mi:ss.ff1"
argument_list|,
literal|"2018-02-03 01:02:03.999999999"
argument_list|,
literal|"01:02:03.9"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"y yyy hh:mi:ss.ffz"
argument_list|,
literal|"2018-02-03 01:02:03.0070070"
argument_list|,
literal|"8 018 01:02:03.007007Z"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"am a.m. pm p.m. AM A.M. PM P.M."
argument_list|,
literal|"2018-02-03 01:02:03.0070070"
argument_list|,
literal|"am a.m. am a.m. AM A.M. AM A.M."
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"HH12 P.M."
argument_list|,
literal|"2019-01-01 00:15:10"
argument_list|,
literal|"12 A.M."
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"HH12 AM"
argument_list|,
literal|"2019-01-01 12:15:10"
argument_list|,
literal|"12 PM"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-MM-DD HH12PM"
argument_list|,
literal|"2017-05-05 00:00:00"
argument_list|,
literal|"2017-05-05 12AM"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkFormatTs
parameter_list|(
name|String
name|pattern
parameter_list|,
name|String
name|input
parameter_list|,
name|String
name|expectedOutput
parameter_list|)
block|{
name|formatter
operator|=
operator|new
name|HiveSqlDateTimeFormatter
argument_list|(
name|pattern
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedOutput
argument_list|,
name|formatter
operator|.
name|format
argument_list|(
name|toTimestamp
argument_list|(
name|input
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testFormatDate
parameter_list|()
block|{
name|checkFormatDate
argument_list|(
literal|"rr rrrr ddd"
argument_list|,
literal|"2018-01-03"
argument_list|,
literal|"18 2018 003"
argument_list|)
expr_stmt|;
name|checkFormatDate
argument_list|(
literal|"yyyy-mm-ddtsssss.ff4z"
argument_list|,
literal|"2018-02-03"
argument_list|,
literal|"2018-02-03T00000.0000Z"
argument_list|)
expr_stmt|;
name|checkFormatDate
argument_list|(
literal|"hh24:mi:ss.ff1"
argument_list|,
literal|"2018-02-03"
argument_list|,
literal|"00:00:00.0"
argument_list|)
expr_stmt|;
name|checkFormatDate
argument_list|(
literal|"y yyy T hh:mi:ss.ff am z"
argument_list|,
literal|"2018-02-03"
argument_list|,
literal|"8 018 T 12:00:00.0 am Z"
argument_list|)
expr_stmt|;
name|checkFormatDate
argument_list|(
literal|"am a.m. pm p.m. AM A.M. PM P.M."
argument_list|,
literal|"2018-02-03"
argument_list|,
literal|"am a.m. am a.m. AM A.M. AM A.M."
argument_list|)
expr_stmt|;
name|checkFormatDate
argument_list|(
literal|"DDD"
argument_list|,
literal|"2019-12-31"
argument_list|,
literal|"365"
argument_list|)
expr_stmt|;
name|checkFormatDate
argument_list|(
literal|"DDD"
argument_list|,
literal|"2020-12-31"
argument_list|,
literal|"366"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkFormatDate
parameter_list|(
name|String
name|pattern
parameter_list|,
name|String
name|input
parameter_list|,
name|String
name|expectedOutput
parameter_list|)
block|{
name|formatter
operator|=
operator|new
name|HiveSqlDateTimeFormatter
argument_list|(
name|pattern
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedOutput
argument_list|,
name|formatter
operator|.
name|format
argument_list|(
name|toDate
argument_list|(
name|input
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testParseTimestamp
parameter_list|()
block|{
name|String
name|thisYearString
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|LocalDateTime
operator|.
name|now
argument_list|()
operator|.
name|getYear
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|firstTwoDigits
init|=
name|getFirstTwoDigits
argument_list|()
decl_stmt|;
comment|//y
name|checkParseTimestamp
argument_list|(
literal|"y-mm-dd"
argument_list|,
literal|"0-02-03"
argument_list|,
name|thisYearString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
operator|+
literal|"0-02-03 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yy-mm-dd"
argument_list|,
literal|"00-02-03"
argument_list|,
name|thisYearString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
operator|+
literal|"00-02-03 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyy-mm-dd"
argument_list|,
literal|"000-02-03"
argument_list|,
name|thisYearString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
operator|+
literal|"000-02-03 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy-mm-dd"
argument_list|,
literal|"000-02-03"
argument_list|,
name|thisYearString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
operator|+
literal|"000-02-03 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"rr-mm-dd"
argument_list|,
literal|"0-02-03"
argument_list|,
name|thisYearString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
operator|+
literal|"0-02-03 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"rrrr-mm-dd"
argument_list|,
literal|"000-02-03"
argument_list|,
name|thisYearString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
operator|+
literal|"000-02-03 00:00:00"
argument_list|)
expr_stmt|;
comment|//rr, rrrr
name|checkParseTimestamp
argument_list|(
literal|"rr-mm-dd"
argument_list|,
literal|"00-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|1
operator|+
literal|"00-02-03 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"rr-mm-dd"
argument_list|,
literal|"49-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|1
operator|+
literal|"49-02-03 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"rr-mm-dd"
argument_list|,
literal|"50-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|"50-02-03 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"rr-mm-dd"
argument_list|,
literal|"99-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|"99-02-03 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"rrrr-mm-dd"
argument_list|,
literal|"00-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|1
operator|+
literal|"00-02-03 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"rrrr-mm-dd"
argument_list|,
literal|"49-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|1
operator|+
literal|"49-02-03 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"rrrr-mm-dd"
argument_list|,
literal|"50-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|"50-02-03 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"rrrr-mm-dd"
argument_list|,
literal|"99-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|"99-02-03 00:00:00"
argument_list|)
expr_stmt|;
comment|//everything else
name|checkParseTimestamp
argument_list|(
literal|"yyyy-mm-ddThh24:mi:ss.ff8z"
argument_list|,
literal|"2018-02-03T04:05:06.5665Z"
argument_list|,
literal|"2018-02-03 04:05:06.5665"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy-mm-dd hh24:mi:ss.ff"
argument_list|,
literal|"2018-02-03 04:05:06.555555555"
argument_list|,
literal|"2018-02-03 04:05:06.555555555"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy-mm-dd hh12:mi:ss"
argument_list|,
literal|"2099-2-03 04:05:06"
argument_list|,
literal|"2099-02-03 04:05:06"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyyddd"
argument_list|,
literal|"2018284"
argument_list|,
literal|"2018-10-11 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyyddd"
argument_list|,
literal|"20184"
argument_list|,
literal|"2018-01-04 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy-mm-ddThh24:mi:ss.ffz"
argument_list|,
literal|"2018-02-03t04:05:06.444Z"
argument_list|,
literal|"2018-02-03 04:05:06.444"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy-mm-dd hh:mi:ss A.M."
argument_list|,
literal|"2018-02-03 04:05:06 P.M."
argument_list|,
literal|"2018-02-03 16:05:06"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"YYYY-MM-DD HH24:MI TZH:TZM"
argument_list|,
literal|"2019-1-1 14:00--1:-30"
argument_list|,
literal|"2019-01-01 14:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"YYYY-MM-DD HH24:MI TZH:TZM"
argument_list|,
literal|"2019-1-1 14:00-1:30"
argument_list|,
literal|"2019-01-01 14:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy-mm-dd TZM:TZH"
argument_list|,
literal|"2019-01-01 1 -3"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy-mm-dd TZH:TZM"
argument_list|,
literal|"2019-01-01 -0:30"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"TZM/YYY-MM-TZH/DD"
argument_list|,
literal|"0/333-01-11/02"
argument_list|,
literal|"2333-01-02 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"YYYY-MM-DD HH12:MI AM"
argument_list|,
literal|"2019-01-01 11:00 p.m."
argument_list|,
literal|"2019-01-01 23:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"YYYY-MM-DD HH12:MI A.M.."
argument_list|,
literal|"2019-01-01 11:00 pm."
argument_list|,
literal|"2019-01-01 23:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"MI DD-TZM-YYYY-MM TZHPM SS:HH12.FF9"
argument_list|,
literal|"59 03-30-2017-05 01PM 01:08.123456789"
argument_list|,
literal|"2017-05-03 20:59:01.123456789"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"YYYYDDMMHH12MISSFFAMTZHTZM"
argument_list|,
literal|"20170501123159123456789AM-0130"
argument_list|,
literal|"2017-01-05 00:31:59.123456789"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"YYYY-MM-DD AMHH12"
argument_list|,
literal|"2017-05-06 P.M.12"
argument_list|,
literal|"2017-05-06 12:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"YYYY-MM-DD HH12PM"
argument_list|,
literal|"2017-05-05 12AM"
argument_list|,
literal|"2017-05-05 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"YYYY-MM-DD HH12:MI:SS.FF9PM TZH:TZM"
argument_list|,
literal|"2017-05-03 08:59:01.123456789PM 01:30"
argument_list|,
literal|"2017-05-03 20:59:01.123456789"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"YYYYDDMMHH12MISSFFAMTZHTZM"
argument_list|,
literal|"20170501120159123456789AM-0130"
argument_list|,
literal|"2017-01-05 00:01:59.123456789"
argument_list|)
expr_stmt|;
comment|//Test "day in year" token in a leap year scenario
name|checkParseTimestamp
argument_list|(
literal|"YYYY DDD"
argument_list|,
literal|"2000 60"
argument_list|,
literal|"2000-02-29 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"YYYY DDD"
argument_list|,
literal|"2000 61"
argument_list|,
literal|"2000-03-01 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"YYYY DDD"
argument_list|,
literal|"2000 366"
argument_list|,
literal|"2000-12-31 00:00:00"
argument_list|)
expr_stmt|;
comment|//Test timezone offset parsing without separators
name|checkParseTimestamp
argument_list|(
literal|"YYYYMMDDHH12MIA.M.TZHTZM"
argument_list|,
literal|"201812310800AM+0515"
argument_list|,
literal|"2018-12-31 08:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"YYYYMMDDHH12MIA.M.TZHTZM"
argument_list|,
literal|"201812310800AM0515"
argument_list|,
literal|"2018-12-31 08:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"YYYYMMDDHH12MIA.M.TZHTZM"
argument_list|,
literal|"201812310800AM-0515"
argument_list|,
literal|"2018-12-31 08:00:00"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|getFirstTwoDigits
parameter_list|()
block|{
name|int
name|thisYear
init|=
name|LocalDateTime
operator|.
name|now
argument_list|()
operator|.
name|getYear
argument_list|()
decl_stmt|;
name|int
name|firstTwoDigits
init|=
name|thisYear
operator|/
literal|100
decl_stmt|;
if|if
condition|(
name|thisYear
operator|%
literal|100
operator|<
literal|50
condition|)
block|{
name|firstTwoDigits
operator|-=
literal|1
expr_stmt|;
block|}
return|return
name|firstTwoDigits
return|;
block|}
specifier|private
name|void
name|checkParseTimestamp
parameter_list|(
name|String
name|pattern
parameter_list|,
name|String
name|input
parameter_list|,
name|String
name|expectedOutput
parameter_list|)
block|{
name|formatter
operator|=
operator|new
name|HiveSqlDateTimeFormatter
argument_list|(
name|pattern
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|toTimestamp
argument_list|(
name|expectedOutput
argument_list|)
argument_list|,
name|formatter
operator|.
name|parseTimestamp
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testParseDate
parameter_list|()
block|{
name|String
name|thisYearString
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|LocalDateTime
operator|.
name|now
argument_list|()
operator|.
name|getYear
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|firstTwoDigits
init|=
name|getFirstTwoDigits
argument_list|()
decl_stmt|;
comment|//y
name|checkParseDate
argument_list|(
literal|"y-mm-dd"
argument_list|,
literal|"0-02-03"
argument_list|,
name|thisYearString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
operator|+
literal|"0-02-03"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"yy-mm-dd"
argument_list|,
literal|"00-02-03"
argument_list|,
name|thisYearString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
operator|+
literal|"00-02-03"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"yyy-mm-dd"
argument_list|,
literal|"000-02-03"
argument_list|,
name|thisYearString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
operator|+
literal|"000-02-03"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"yyyy-mm-dd"
argument_list|,
literal|"000-02-03"
argument_list|,
name|thisYearString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
operator|+
literal|"000-02-03"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"rr-mm-dd"
argument_list|,
literal|"0-02-03"
argument_list|,
name|thisYearString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
operator|+
literal|"0-02-03"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"rrrr-mm-dd"
argument_list|,
literal|"000-02-03"
argument_list|,
name|thisYearString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
operator|+
literal|"000-02-03"
argument_list|)
expr_stmt|;
comment|//rr, rrrr
name|checkParseDate
argument_list|(
literal|"rr-mm-dd"
argument_list|,
literal|"00-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|1
operator|+
literal|"00-02-03"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"rr-mm-dd"
argument_list|,
literal|"49-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|1
operator|+
literal|"49-02-03"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"rr-mm-dd"
argument_list|,
literal|"50-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|"50-02-03"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"rr-mm-dd"
argument_list|,
literal|"99-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|"99-02-03"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"rrrr-mm-dd"
argument_list|,
literal|"00-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|1
operator|+
literal|"00-02-03"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"rrrr-mm-dd"
argument_list|,
literal|"49-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|1
operator|+
literal|"49-02-03"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"rrrr-mm-dd"
argument_list|,
literal|"50-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|"50-02-03"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"rrrr-mm-dd"
argument_list|,
literal|"99-02-03"
argument_list|,
name|firstTwoDigits
operator|+
literal|"99-02-03"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"yyyy-mm-dd hh mi ss.ff7"
argument_list|,
literal|"2018/01/01 2.2.2.55"
argument_list|,
literal|"2018-01-01"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkParseDate
parameter_list|(
name|String
name|pattern
parameter_list|,
name|String
name|input
parameter_list|,
name|String
name|expectedOutput
parameter_list|)
block|{
name|formatter
operator|=
operator|new
name|HiveSqlDateTimeFormatter
argument_list|(
name|pattern
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|toDate
argument_list|(
name|expectedOutput
argument_list|)
argument_list|,
name|formatter
operator|.
name|parseDate
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testParseTimestampError
parameter_list|()
block|{
name|verifyBadParseString
argument_list|(
literal|"yyyy"
argument_list|,
literal|"2019-02-03"
argument_list|)
expr_stmt|;
name|verifyBadParseString
argument_list|(
literal|"yyyy-mm-dd  "
argument_list|,
literal|"2019-02-03"
argument_list|)
expr_stmt|;
comment|//separator missing
name|verifyBadParseString
argument_list|(
literal|"yyyy-mm-dd"
argument_list|,
literal|"2019-02-03..."
argument_list|)
expr_stmt|;
comment|//extra separators
name|verifyBadParseString
argument_list|(
literal|"yyyy-mm-dd hh12:mi:ss"
argument_list|,
literal|"2019-02-03 14:00:00"
argument_list|)
expr_stmt|;
comment|//hh12 out of range
name|verifyBadParseString
argument_list|(
literal|"yyyy-dddsssss"
argument_list|,
literal|"2019-912345"
argument_list|)
expr_stmt|;
name|verifyBadParseString
argument_list|(
literal|"yyyy-mm-dd"
argument_list|,
literal|"2019-13-23"
argument_list|)
expr_stmt|;
comment|//mm out of range
name|verifyBadParseString
argument_list|(
literal|"yyyy-mm-dd tzh:tzm"
argument_list|,
literal|"2019-01-01 +16:00"
argument_list|)
expr_stmt|;
comment|//tzh out of range
name|verifyBadParseString
argument_list|(
literal|"yyyy-mm-dd tzh:tzm"
argument_list|,
literal|"2019-01-01 +14:60"
argument_list|)
expr_stmt|;
comment|//tzm out of range
name|verifyBadParseString
argument_list|(
literal|"YYYY DDD"
argument_list|,
literal|"2000 367"
argument_list|)
expr_stmt|;
comment|//ddd out of range
block|}
specifier|private
name|void
name|verifyBadPattern
parameter_list|(
name|String
name|string
parameter_list|,
name|boolean
name|forParsing
parameter_list|)
block|{
try|try
block|{
name|formatter
operator|=
operator|new
name|HiveSqlDateTimeFormatter
argument_list|(
name|string
argument_list|,
name|forParsing
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|e
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|IllegalArgumentException
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Verify pattern is parsed correctly.    * Check:    * -token.temporalField for each token    * -sum of token.lengths    * -concatenation of token.strings    */
specifier|private
name|void
name|verifyPatternParsing
parameter_list|(
name|String
name|pattern
parameter_list|,
name|ArrayList
argument_list|<
name|TemporalField
argument_list|>
name|temporalFields
parameter_list|)
block|{
name|verifyPatternParsing
argument_list|(
name|pattern
argument_list|,
name|pattern
operator|.
name|length
argument_list|()
argument_list|,
name|pattern
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|temporalFields
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyPatternParsing
parameter_list|(
name|String
name|pattern
parameter_list|,
name|int
name|expectedPatternLength
parameter_list|,
name|String
name|expectedPattern
parameter_list|,
name|ArrayList
argument_list|<
name|TemporalField
argument_list|>
name|temporalFields
parameter_list|)
block|{
name|formatter
operator|=
operator|new
name|HiveSqlDateTimeFormatter
argument_list|(
name|pattern
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|temporalFields
operator|.
name|size
argument_list|()
argument_list|,
name|formatter
operator|.
name|getTokens
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|actualPatternLength
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|temporalFields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
literal|"Generated list of tokens not correct"
argument_list|,
name|temporalFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|formatter
operator|.
name|getTokens
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|temporalField
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|formatter
operator|.
name|getTokens
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|string
argument_list|)
expr_stmt|;
name|actualPatternLength
operator|+=
name|formatter
operator|.
name|getTokens
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|length
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Token strings concatenated don't match original pattern string"
argument_list|,
name|expectedPattern
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedPatternLength
argument_list|,
name|actualPatternLength
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyBadParseString
parameter_list|(
name|String
name|pattern
parameter_list|,
name|String
name|string
parameter_list|)
block|{
try|try
block|{
name|formatter
operator|=
operator|new
name|HiveSqlDateTimeFormatter
argument_list|(
name|pattern
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|formatter
operator|.
name|parseTimestamp
argument_list|(
name|string
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|e
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|IllegalArgumentException
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Methods that construct datetime objects using java.time.DateTimeFormatter.
specifier|public
specifier|static
name|Date
name|toDate
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|LocalDate
name|localDate
init|=
name|LocalDate
operator|.
name|parse
argument_list|(
name|s
argument_list|,
name|DATE_FORMATTER
argument_list|)
decl_stmt|;
return|return
name|Date
operator|.
name|ofEpochDay
argument_list|(
operator|(
name|int
operator|)
name|localDate
operator|.
name|toEpochDay
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * This is effectively the old Timestamp.valueOf method.    */
specifier|public
specifier|static
name|Timestamp
name|toTimestamp
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|LocalDateTime
name|localDateTime
init|=
name|LocalDateTime
operator|.
name|parse
argument_list|(
name|s
operator|.
name|trim
argument_list|()
argument_list|,
name|TIMESTAMP_FORMATTER
argument_list|)
decl_stmt|;
return|return
name|Timestamp
operator|.
name|ofEpochSecond
argument_list|(
name|localDateTime
operator|.
name|toEpochSecond
argument_list|(
name|ZoneOffset
operator|.
name|UTC
argument_list|)
argument_list|,
name|localDateTime
operator|.
name|getNano
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|static
specifier|final
name|DateTimeFormatter
name|DATE_FORMATTER
init|=
name|DateTimeFormatter
operator|.
name|ofPattern
argument_list|(
literal|"yyyy-MM-dd"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|DateTimeFormatter
name|TIMESTAMP_FORMATTER
decl_stmt|;
static|static
block|{
name|DateTimeFormatterBuilder
name|builder
init|=
operator|new
name|DateTimeFormatterBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|appendValue
argument_list|(
name|YEAR
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|,
name|SignStyle
operator|.
name|NORMAL
argument_list|)
operator|.
name|appendLiteral
argument_list|(
literal|'-'
argument_list|)
operator|.
name|appendValue
argument_list|(
name|MONTH_OF_YEAR
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
name|SignStyle
operator|.
name|NORMAL
argument_list|)
operator|.
name|appendLiteral
argument_list|(
literal|'-'
argument_list|)
operator|.
name|appendValue
argument_list|(
name|DAY_OF_MONTH
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
name|SignStyle
operator|.
name|NORMAL
argument_list|)
operator|.
name|optionalStart
argument_list|()
operator|.
name|appendLiteral
argument_list|(
literal|" "
argument_list|)
operator|.
name|appendValue
argument_list|(
name|HOUR_OF_DAY
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
name|SignStyle
operator|.
name|NORMAL
argument_list|)
operator|.
name|appendLiteral
argument_list|(
literal|':'
argument_list|)
operator|.
name|appendValue
argument_list|(
name|MINUTE_OF_HOUR
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
name|SignStyle
operator|.
name|NORMAL
argument_list|)
operator|.
name|appendLiteral
argument_list|(
literal|':'
argument_list|)
operator|.
name|appendValue
argument_list|(
name|SECOND_OF_MINUTE
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
name|SignStyle
operator|.
name|NORMAL
argument_list|)
operator|.
name|optionalStart
argument_list|()
operator|.
name|appendFraction
argument_list|(
name|ChronoField
operator|.
name|NANO_OF_SECOND
argument_list|,
literal|1
argument_list|,
literal|9
argument_list|,
literal|true
argument_list|)
operator|.
name|optionalEnd
argument_list|()
operator|.
name|optionalEnd
argument_list|()
expr_stmt|;
name|TIMESTAMP_FORMATTER
operator|=
name|builder
operator|.
name|toFormatter
argument_list|()
operator|.
name|withResolverStyle
argument_list|(
name|ResolverStyle
operator|.
name|LENIENT
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

