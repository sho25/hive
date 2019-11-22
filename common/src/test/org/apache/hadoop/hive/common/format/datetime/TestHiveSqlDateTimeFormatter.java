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
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_comment
comment|/**  * Tests HiveSqlDateTimeFormatter.  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveSqlDateTimeFormatter
block|{
specifier|private
name|HiveSqlDateTimeFormatter
name|formatter
decl_stmt|;
annotation|@
name|Test
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
annotation|@
name|Test
specifier|public
name|void
name|testSetPatternWithBadPatterns
parameter_list|()
block|{
name|verifyBadPattern
argument_list|(
literal|""
argument_list|,
literal|true
argument_list|)
expr_stmt|;
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
literal|"yyyy mm-MON dd"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy mm-MONTH dd"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy MON, month dd"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"iyyy-mm-dd"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// can't mix iso and Gregorian
name|verifyBadPattern
argument_list|(
literal|"iyyy-id"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// missing iyyy, iw, or id
name|verifyBadPattern
argument_list|(
literal|"iyyy-iw"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"iw-id"
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
comment|//illegal for parsing
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm-dd q"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm-dd d"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm-dd dy"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm-dd day"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm-dd w"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyyy-mm-dd ww"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
name|checkFormatTs
argument_list|(
literal|"YYYY-MONTH-DD"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"2019-JANUARY  -01"
argument_list|)
expr_stmt|;
comment|//fill to length 9
name|checkFormatTs
argument_list|(
literal|"YYYY-MOnth-DD"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"2019-JANUARY  -01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-Month-DD"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"2019-January  -01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-MoNTH-DD"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"2019-January  -01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-month-DD"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"2019-january  -01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-mONTH-DD"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"2019-january  -01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-MON-DD"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"2019-JAN-01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-MOn-DD"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"2019-JAN-01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-Mon-DD"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"2019-Jan-01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-MoN-DD"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"2019-Jan-01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-mon-DD"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"2019-jan-01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-mON-DD"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"2019-jan-01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"D: DAY"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"3: TUESDAY  "
argument_list|)
expr_stmt|;
comment|//fill to length 9
name|checkFormatTs
argument_list|(
literal|"D: DAy"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"3: TUESDAY  "
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"D: Day"
argument_list|,
literal|"2019-01-02 00:00:00"
argument_list|,
literal|"4: Wednesday"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"D: DaY"
argument_list|,
literal|"2019-01-02 00:00:00"
argument_list|,
literal|"4: Wednesday"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"D: day"
argument_list|,
literal|"2019-01-03 00:00:00"
argument_list|,
literal|"5: thursday "
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"D: dAY"
argument_list|,
literal|"2019-01-03 00:00:00"
argument_list|,
literal|"5: thursday "
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"D: DY"
argument_list|,
literal|"2019-01-04 00:00:00"
argument_list|,
literal|"6: FRI"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"D: Dy"
argument_list|,
literal|"2019-01-05 00:00:00"
argument_list|,
literal|"7: Sat"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"D: dy"
argument_list|,
literal|"2019-01-06 00:00:00"
argument_list|,
literal|"1: sun"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"D: DAY"
argument_list|,
literal|"2019-01-07 00:00:00"
argument_list|,
literal|"2: MONDAY   "
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-mm-dd: Q WW W"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"2019-01-01: 1 01 1"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-mm-dd: Q WW W"
argument_list|,
literal|"2019-01-07 00:00:00"
argument_list|,
literal|"2019-01-07: 1 01 1"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-mm-dd: Q WW W"
argument_list|,
literal|"2019-01-08 00:00:00"
argument_list|,
literal|"2019-01-08: 1 02 2"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-mm-dd: Q WW W"
argument_list|,
literal|"2019-03-31 00:00:00"
argument_list|,
literal|"2019-03-31: 1 13 5"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-mm-dd: Q WW W"
argument_list|,
literal|"2019-04-01 00:00:00"
argument_list|,
literal|"2019-04-01: 2 13 1"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-mm-dd: Q WW W"
argument_list|,
literal|"2019-12-31 00:00:00"
argument_list|,
literal|"2019-12-31: 4 53 5"
argument_list|)
expr_stmt|;
comment|//ISO 8601
name|checkFormatTs
argument_list|(
literal|"YYYY-MM-DD : IYYY-IW-ID"
argument_list|,
literal|"2018-12-31 00:00:00"
argument_list|,
literal|"2018-12-31 : 2019-01-01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-MM-DD : IYYY-IW-ID"
argument_list|,
literal|"2019-01-06 00:00:00"
argument_list|,
literal|"2019-01-06 : 2019-01-07"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-MM-DD : IYYY-IW-ID"
argument_list|,
literal|"2019-01-07 00:00:00"
argument_list|,
literal|"2019-01-07 : 2019-02-01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-MM-DD : IYYY-IW-ID"
argument_list|,
literal|"2019-12-29 00:00:00"
argument_list|,
literal|"2019-12-29 : 2019-52-07"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-MM-DD : IYYY-IW-ID"
argument_list|,
literal|"2019-12-30 00:00:00"
argument_list|,
literal|"2019-12-30 : 2020-01-01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-MM-DD : IYY-IW-ID"
argument_list|,
literal|"2019-12-30 00:00:00"
argument_list|,
literal|"2019-12-30 : 020-01-01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-MM-DD : IY-IW-ID"
argument_list|,
literal|"2019-12-30 00:00:00"
argument_list|,
literal|"2019-12-30 : 20-01-01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"YYYY-MM-DD : I-IW-ID"
argument_list|,
literal|"2019-12-30 00:00:00"
argument_list|,
literal|"2019-12-30 : 0-01-01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"id: Day"
argument_list|,
literal|"2018-12-31 00:00:00"
argument_list|,
literal|"01: Monday   "
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"id: Day"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"02: Tuesday  "
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"id: Day"
argument_list|,
literal|"2019-01-02 00:00:00"
argument_list|,
literal|"03: Wednesday"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"id: Day"
argument_list|,
literal|"2019-01-03 00:00:00"
argument_list|,
literal|"04: Thursday "
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"id: Day"
argument_list|,
literal|"2019-01-04 00:00:00"
argument_list|,
literal|"05: Friday   "
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"id: Day"
argument_list|,
literal|"2019-01-05 00:00:00"
argument_list|,
literal|"06: Saturday "
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"id: Day"
argument_list|,
literal|"2019-01-06 00:00:00"
argument_list|,
literal|"07: Sunday   "
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
literal|"Format timestamp to string failed with pattern: "
operator|+
name|pattern
argument_list|,
name|expectedOutput
argument_list|,
name|formatter
operator|.
name|format
argument_list|(
name|Timestamp
operator|.
name|valueOf
argument_list|(
name|input
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
literal|"Format date to string failed with pattern: "
operator|+
name|pattern
argument_list|,
name|expectedOutput
argument_list|,
name|formatter
operator|.
name|format
argument_list|(
name|Date
operator|.
name|valueOf
argument_list|(
name|input
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
comment|//MONTH, MON : case really doesn't matter
name|checkParseTimestamp
argument_list|(
literal|"yyyy-MONTH-dd"
argument_list|,
literal|"2018-FEBRUARY-28"
argument_list|,
literal|"2018-02-28 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy-Month-dd"
argument_list|,
literal|"2018-february-28"
argument_list|,
literal|"2018-02-28 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy-month-dd"
argument_list|,
literal|"2018-FEBRUARY-28"
argument_list|,
literal|"2018-02-28 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy-montH-dd"
argument_list|,
literal|"2018-febRuary-28"
argument_list|,
literal|"2018-02-28 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy-MON-dd"
argument_list|,
literal|"2018-FEB-28"
argument_list|,
literal|"2018-02-28 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy-moN-dd"
argument_list|,
literal|"2018-FeB-28"
argument_list|,
literal|"2018-02-28 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy-mon-dd"
argument_list|,
literal|"2018-FEB-28"
argument_list|,
literal|"2018-02-28 00:00:00"
argument_list|)
expr_stmt|;
name|verifyBadParseString
argument_list|(
literal|"yyyy-MON-dd"
argument_list|,
literal|"2018-FEBRUARY-28"
argument_list|)
expr_stmt|;
name|verifyBadParseString
argument_list|(
literal|"yyyy-MON-dd"
argument_list|,
literal|"2018-FEBR-28"
argument_list|)
expr_stmt|;
name|verifyBadParseString
argument_list|(
literal|"yyyy-MONTH-dd"
argument_list|,
literal|"2018-FEB-28"
argument_list|)
expr_stmt|;
comment|//letters and numbers are delimiters to each other, respectively
name|checkParseDate
argument_list|(
literal|"yyyy-ddMONTH"
argument_list|,
literal|"2018-4March"
argument_list|,
literal|"2018-03-04"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"yyyy-MONTHdd"
argument_list|,
literal|"2018-March4"
argument_list|,
literal|"2018-03-04"
argument_list|)
expr_stmt|;
comment|//ISO 8601
name|checkParseTimestamp
argument_list|(
literal|"IYYY-IW-ID"
argument_list|,
literal|"2019-01-01"
argument_list|,
literal|"2018-12-31 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"IYYY-IW-ID"
argument_list|,
literal|"2019-01-07"
argument_list|,
literal|"2019-01-06 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"IYYY-IW-ID"
argument_list|,
literal|"2019-02-01"
argument_list|,
literal|"2019-01-07 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"IYYY-IW-ID"
argument_list|,
literal|"2019-52-07"
argument_list|,
literal|"2019-12-29 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"IYYY-IW-ID"
argument_list|,
literal|"2020-01-01"
argument_list|,
literal|"2019-12-30 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"IYYY-IW-ID"
argument_list|,
literal|"020-01-04"
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
literal|"020-01-02 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"IYY-IW-ID"
argument_list|,
literal|"020-01-04"
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
literal|"020-01-02 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"IYY-IW-ID"
argument_list|,
literal|"20-01-04"
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
literal|"20-01-02 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"IY-IW-ID"
argument_list|,
literal|"20-01-04"
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
literal|"20-01-02 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"IYYY-IW-DAY"
argument_list|,
literal|"2019-01-monday"
argument_list|,
literal|"2018-12-31 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"IYYY-IW-Day"
argument_list|,
literal|"2019-01-Sunday"
argument_list|,
literal|"2019-01-06 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"IYYY-IW-Dy"
argument_list|,
literal|"2019-02-MON"
argument_list|,
literal|"2019-01-07 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"IYYY-IW-DY"
argument_list|,
literal|"2019-52-sun"
argument_list|,
literal|"2019-12-29 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"IYYY-IW-dy"
argument_list|,
literal|"2020-01-Mon"
argument_list|,
literal|"2019-12-30 00:00:00"
argument_list|)
expr_stmt|;
comment|//Tests for these patterns would need changing every decade if done in the above way.
comment|//Thursday of the first week in an ISO year always matches the Gregorian year.
name|checkParseTimestampIso
argument_list|(
literal|"IY-IW-ID"
argument_list|,
literal|"0-01-04"
argument_list|,
literal|"iw, yyyy"
argument_list|,
literal|"01, "
operator|+
name|thisYearString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
operator|+
literal|"0"
argument_list|)
expr_stmt|;
name|checkParseTimestampIso
argument_list|(
literal|"I-IW-ID"
argument_list|,
literal|"0-01-04"
argument_list|,
literal|"iw, yyyy"
argument_list|,
literal|"01, "
operator|+
name|thisYearString
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
operator|+
literal|"0"
argument_list|)
expr_stmt|;
comment|//time patterns are allowed; date patterns are not
name|checkParseTimestamp
argument_list|(
literal|"IYYY-IW-ID hh24:mi:ss"
argument_list|,
literal|"2019-01-01 01:02:03"
argument_list|,
literal|"2018-12-31 01:02:03"
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
literal|"Parse string to timestamp failed. Pattern: "
operator|+
name|pattern
argument_list|,
name|Timestamp
operator|.
name|valueOf
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
specifier|private
name|void
name|checkParseTimestampIso
parameter_list|(
name|String
name|parsePattern
parameter_list|,
name|String
name|input
parameter_list|,
name|String
name|formatPattern
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
name|parsePattern
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Timestamp
name|ts
init|=
name|formatter
operator|.
name|parseTimestamp
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|formatter
operator|=
operator|new
name|HiveSqlDateTimeFormatter
argument_list|(
name|formatPattern
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
name|ts
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
name|checkParseDate
argument_list|(
literal|"dd/MonthT/yyyy"
argument_list|,
literal|"31/AugustT/2020"
argument_list|,
literal|"2020-08-31"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"dd/MonthT/yyyy"
argument_list|,
literal|"31/MarchT/2020"
argument_list|,
literal|"2020-03-31"
argument_list|)
expr_stmt|;
comment|//ISO 8601
name|checkParseDate
argument_list|(
literal|"IYYY-IW-ID"
argument_list|,
literal|"2019-01-01"
argument_list|,
literal|"2018-12-31"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"IW-ID-IYYY"
argument_list|,
literal|"01-02-2019"
argument_list|,
literal|"2019-01-01"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"ID-IW-IYYY"
argument_list|,
literal|"02-01-2019"
argument_list|,
literal|"2019-01-01"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"IYYY-IW-ID"
argument_list|,
literal|"2019-01-07"
argument_list|,
literal|"2019-01-06"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"IYYY-IW-ID"
argument_list|,
literal|"2019-02-01"
argument_list|,
literal|"2019-01-07"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"IYYY-IW-ID"
argument_list|,
literal|"2019-52-07"
argument_list|,
literal|"2019-12-29"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"IYYY-IW-ID"
argument_list|,
literal|"2020-01-01"
argument_list|,
literal|"2019-12-30"
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
literal|"Parse string to date failed. Pattern: "
operator|+
name|pattern
argument_list|,
name|Date
operator|.
name|valueOf
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
annotation|@
name|Test
specifier|public
name|void
name|testParseTimestampError
parameter_list|()
block|{
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
comment|//ddd out of range
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
name|verifyBadParseString
argument_list|(
literal|"yyyy-month-dd"
argument_list|,
literal|"2019-merch-23"
argument_list|)
expr_stmt|;
comment|//invalid month of year
name|verifyBadParseString
argument_list|(
literal|"yyyy-mon-dd"
argument_list|,
literal|"2019-mer-23"
argument_list|)
expr_stmt|;
comment|//invalid month of year
name|verifyBadParseString
argument_list|(
literal|"yyyy-MON-dd"
argument_list|,
literal|"2018-FEBRUARY-28"
argument_list|)
expr_stmt|;
comment|// can't mix and match mon and month
name|verifyBadParseString
argument_list|(
literal|"yyyy-MON-dd"
argument_list|,
literal|"2018-FEBR-28"
argument_list|)
expr_stmt|;
name|verifyBadParseString
argument_list|(
literal|"yyyy-MONTH-dd"
argument_list|,
literal|"2018-FEB-28"
argument_list|)
expr_stmt|;
name|verifyBadParseString
argument_list|(
literal|"iyyy-iw-id"
argument_list|,
literal|"2019-00-01"
argument_list|)
expr_stmt|;
comment|//ISO 8601 week number out of range for year
name|verifyBadParseString
argument_list|(
literal|"iyyy-iw-id"
argument_list|,
literal|"2019-53-01"
argument_list|)
expr_stmt|;
comment|//ISO 8601 week number out of range for year
name|verifyBadParseString
argument_list|(
literal|"iw-iyyy-id"
argument_list|,
literal|"53-2019-01"
argument_list|)
expr_stmt|;
comment|//ISO 8601 week number out of range for year
name|verifyBadParseString
argument_list|(
literal|"iw-iyyy-id"
argument_list|,
literal|"54-2019-01"
argument_list|)
expr_stmt|;
comment|//ISO 8601 week number out of range
name|verifyBadParseString
argument_list|(
literal|"iyyy-iw-id"
argument_list|,
literal|"2019-52-00"
argument_list|)
expr_stmt|;
comment|//ISO 8601 day of week out of range
name|verifyBadParseString
argument_list|(
literal|"iyyy-iw-id"
argument_list|,
literal|"2019-52-08"
argument_list|)
expr_stmt|;
comment|//ISO 8601 day of week out of range
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
argument_list|(
literal|"Bad pattern "
operator|+
name|string
operator|+
literal|" should have thrown IllegalArgumentException but didn't"
argument_list|)
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
literal|"Expected IllegalArgumentException, got another exception."
argument_list|,
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
annotation|@
name|Test
specifier|public
name|void
name|testFm
parameter_list|()
block|{
comment|//year (019) becomes 19 even if pattern is yyy
name|checkFormatTs
argument_list|(
literal|"FMyyy-FMmm-dd FMHH12:MI:FMSS"
argument_list|,
literal|"2019-01-01 01:01:01"
argument_list|,
literal|"19-1-01 1:01:1"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"FMiyy-FMiw-id FMHH12:MI:FMSS"
argument_list|,
literal|"2018-12-31 01:01:01"
argument_list|,
literal|"19-1-01 1:01:1"
argument_list|)
expr_stmt|;
comment|//ff[1-9] shouldn't be affected, because leading zeroes hold information
name|checkFormatTs
argument_list|(
literal|"FF5/FMFF5"
argument_list|,
literal|"2019-01-01 01:01:01.0333"
argument_list|,
literal|"03330/03330"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"FF/FMFF"
argument_list|,
literal|"2019-01-01 01:01:01.0333"
argument_list|,
literal|"0333/0333"
argument_list|)
expr_stmt|;
comment|//omit trailing spaces from character temporal elements
name|checkFormatTs
argument_list|(
literal|"YYYY-fmMonth-DD"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"2019-January-01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"D: fmDAY"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|,
literal|"3: TUESDAY"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"D: fmDay"
argument_list|,
literal|"2019-01-02 00:00:00"
argument_list|,
literal|"4: Wednesday"
argument_list|)
expr_stmt|;
comment|//only affects temporals that immediately follow
name|verifyBadPattern
argument_list|(
literal|"yyy-mm-dd FM,HH12"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyy-mm-dd FM,HH12"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"yyy-mm-dd HH12 tzh:fmtzm"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"FMFMyyy-mm-dd"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyBadPattern
argument_list|(
literal|"FMFXDD-MM-YYYY ff2"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFx
parameter_list|()
block|{
name|checkParseDate
argument_list|(
literal|"FXDD-MM-YYYY"
argument_list|,
literal|"01-01-1998"
argument_list|,
literal|"1998-01-01"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"FXDD-MM-YYYY hh12:mi:ss.ff"
argument_list|,
literal|"15-01-1998 11:12:13.0"
argument_list|,
literal|"1998-01-15 11:12:13"
argument_list|)
expr_stmt|;
comment|//ff[1-9] are exempt
name|checkParseTimestamp
argument_list|(
literal|"FXDD-MM-YYYY hh12:mi:ss.ff6"
argument_list|,
literal|"01-01-1998 00:00:00.4440"
argument_list|,
literal|"1998-01-01 00:00:00.444"
argument_list|)
expr_stmt|;
comment|//fx can be anywhere in the pattern string
name|checkParseTimestamp
argument_list|(
literal|"DD-MM-YYYYFX"
argument_list|,
literal|"01-01-1998"
argument_list|,
literal|"1998-01-01 00:00:00"
argument_list|)
expr_stmt|;
name|verifyBadParseString
argument_list|(
literal|"DD-MM-YYYYFX"
argument_list|,
literal|"1-01-1998"
argument_list|)
expr_stmt|;
comment|//same separators required
name|verifyBadParseString
argument_list|(
literal|"FXDD-MM-YYYY"
argument_list|,
literal|"15/01/1998"
argument_list|)
expr_stmt|;
comment|//no filling in zeroes or year digits
name|verifyBadParseString
argument_list|(
literal|"FXDD-MM-YYYY"
argument_list|,
literal|"1-01-1998"
argument_list|)
expr_stmt|;
name|verifyBadParseString
argument_list|(
literal|"FXDD-MM-YYYY"
argument_list|,
literal|"01-01-98"
argument_list|)
expr_stmt|;
comment|//no leading or trailing whitespace
name|verifyBadParseString
argument_list|(
literal|"FXDD-MM-YYYY"
argument_list|,
literal|"   01-01-1998   "
argument_list|)
expr_stmt|;
comment|//enforce correct amount of leading zeroes
name|verifyBadParseString
argument_list|(
literal|"FXyyyy-mm-dd hh24:miss"
argument_list|,
literal|"2018-01-01 17:005"
argument_list|)
expr_stmt|;
name|verifyBadParseString
argument_list|(
literal|"FXyyyy-mm-dd sssss"
argument_list|,
literal|"2019-01-01 003"
argument_list|)
expr_stmt|;
name|verifyBadParseString
argument_list|(
literal|"FXiyyy-iw-id hh24:mi:ss"
argument_list|,
literal|"019-01-02 17:00:05"
argument_list|)
expr_stmt|;
comment|//text case does not matter
name|checkParseTimestamp
argument_list|(
literal|"\"the DATE is\" yyyy-mm-dd"
argument_list|,
literal|"the date is 2018-01-01"
argument_list|,
literal|"2018-01-01 00:00:00"
argument_list|)
expr_stmt|;
comment|//AM/PM length has to match, but case doesn't
name|checkParseTimestamp
argument_list|(
literal|"FXDD-MM-YYYY hh12 am"
argument_list|,
literal|"01-01-1998 12 PM"
argument_list|,
literal|"1998-01-01 12:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"FXDD-MM-YYYY hh12 A.M."
argument_list|,
literal|"01-01-1998 12 p.m."
argument_list|,
literal|"1998-01-01 12:00:00"
argument_list|)
expr_stmt|;
name|verifyBadParseString
argument_list|(
literal|"FXDD-MM-YYYY hh12 am"
argument_list|,
literal|"01-01-1998 12 p.m."
argument_list|)
expr_stmt|;
name|verifyBadParseString
argument_list|(
literal|"FXDD-MM-YYYY hh12 a.m."
argument_list|,
literal|"01-01-1998 12 pm"
argument_list|)
expr_stmt|;
comment|//character temporals shouldn't have trailing spaces
name|checkParseTimestamp
argument_list|(
literal|"FXDD-month-YYYY"
argument_list|,
literal|"15-March-1998"
argument_list|,
literal|"1998-03-15 00:00:00"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFmFx
parameter_list|()
block|{
name|checkParseTimestamp
argument_list|(
literal|"FXDD-FMMM-YYYY hh12 am"
argument_list|,
literal|"01-1-1998 12 PM"
argument_list|,
literal|"1998-01-01 12:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"FXFMDD-MM-YYYY hh12 am"
argument_list|,
literal|"1-01-1998 12 PM"
argument_list|,
literal|"1998-01-01 12:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"FXFMiyyy-iw-id hh24:mi:ss"
argument_list|,
literal|"019-01-02 17:00:05"
argument_list|,
literal|"2019-01-01 17:00:05"
argument_list|)
expr_stmt|;
name|verifyBadParseString
argument_list|(
literal|"FXFMiyyy-iw-id hh24:mi:ss"
argument_list|,
literal|"019-01-02 17:0:05"
argument_list|)
expr_stmt|;
comment|//ff[1-9] unaffected
name|checkParseTimestamp
argument_list|(
literal|"FXFMDD-MM-YYYY FMff2"
argument_list|,
literal|"1-01-1998 4"
argument_list|,
literal|"1998-01-01 00:00:00.4"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"FXFMDD-MM-YYYY ff2"
argument_list|,
literal|"1-01-1998 4"
argument_list|,
literal|"1998-01-01 00:00:00.4"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testText
parameter_list|()
block|{
comment|// keep exact text upon format
name|checkFormatTs
argument_list|(
literal|"hh24:mi \" Is \" hh12 PM\".\""
argument_list|,
literal|"2008-01-01 17:00:00"
argument_list|,
literal|"17:00  Is  05 PM."
argument_list|)
expr_stmt|;
name|checkFormatDate
argument_list|(
literal|"\" `the _year_ is` \" yyyy\".\""
argument_list|,
literal|"2008-01-01"
argument_list|,
literal|" `the _year_ is`  2008."
argument_list|)
expr_stmt|;
comment|// empty text strings work
name|checkParseTimestamp
argument_list|(
literal|"\"\"yyyy\"\"-mm-dd\"\""
argument_list|,
literal|"2019-01-01"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"\"\"yyyy\"\"-mm-dd\"\""
argument_list|,
literal|"2019-01-01"
argument_list|,
literal|"2019-01-01"
argument_list|)
expr_stmt|;
comment|// Case doesn't matter upon parsing
name|checkParseTimestamp
argument_list|(
literal|"\"Year \"YYYY \"month\" MM \"day\" DD.\"!\""
argument_list|,
literal|"YEaR 3000 mOnTh 3 DaY 1...!"
argument_list|,
literal|"3000-03-01 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"\"Year \"YYYY \"month\" MM \"day\" DD.\"!\""
argument_list|,
literal|"YEaR 3000 mOnTh 3 DaY 1...!"
argument_list|,
literal|"3000-03-01"
argument_list|)
expr_stmt|;
comment|// Characters matter upon parsing
name|verifyBadParseString
argument_list|(
literal|"\"Year! \"YYYY \"m\" MM \"d\" DD.\"!\""
argument_list|,
literal|"Year 3000 m 3 d 1,!"
argument_list|)
expr_stmt|;
comment|// non-numeric characters in text counts as a delimiter
name|checkParseDate
argument_list|(
literal|"yyyy\"m\"mm\"d\"dd"
argument_list|,
literal|"19m1d1"
argument_list|,
name|LocalDate
operator|.
name|now
argument_list|()
operator|.
name|getYear
argument_list|()
operator|/
literal|100
operator|+
literal|"19-01-01"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"yyyy\"[\"mm\"]\"dd"
argument_list|,
literal|"19[1]1"
argument_list|,
name|LocalDate
operator|.
name|now
argument_list|()
operator|.
name|getYear
argument_list|()
operator|/
literal|100
operator|+
literal|"19-01-01"
argument_list|)
expr_stmt|;
comment|// parse character temporals correctly
name|checkParseDate
argument_list|(
literal|"dd/Month\"arch\"/yyyy"
argument_list|,
literal|"31/Marcharch/2020"
argument_list|,
literal|"2020-03-31"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"dd/Month\"ember\"/yyyy"
argument_list|,
literal|"31/Decemberember/2020"
argument_list|,
literal|"2020-12-31"
argument_list|)
expr_stmt|;
comment|// single quotes are separators and not text delimiters
name|checkParseTimestamp
argument_list|(
literal|"\"Y\'ear \"YYYY \' \"month\" MM \"day\" DD.\"!\""
argument_list|,
literal|"Y'EaR 3000 ' mOnTh 3 DaY 1...!"
argument_list|,
literal|"3000-03-01 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseDate
argument_list|(
literal|"\"Y\'ear \"YYYY \' \"month\" MM \"day\" DD.\"!\""
argument_list|,
literal|"Y'EaR 3000 ' mOnTh 3 DaY 1...!"
argument_list|,
literal|"3000-03-01"
argument_list|)
expr_stmt|;
comment|// literal double quotes are escaped
name|checkFormatTs
argument_list|(
literal|"\"the \\\"DATE\\\" is\" yyyy-mm-dd"
argument_list|,
literal|"2018-01-01 00:00:00"
argument_list|,
literal|"the \"DATE\" is 2018-01-01"
argument_list|)
expr_stmt|;
name|checkFormatTs
argument_list|(
literal|"\"\\\"\\\"\\\"\""
argument_list|,
literal|"2018-01-01 00:00:00"
argument_list|,
literal|"\"\"\""
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"\"the \\\"DATE\\\" is\" yyyy-mm-dd"
argument_list|,
literal|"the \"date\" is 2018-01-01"
argument_list|,
literal|"2018-01-01 00:00:00"
argument_list|)
expr_stmt|;
comment|// Check variations of apostrophes, literal and non-literal double quotes
name|checkParseTimestamp
argument_list|(
literal|"yyyy'\"\"mm-dd"
argument_list|,
literal|"2019\'01-01"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy\'\"\"mm-dd"
argument_list|,
literal|"2019\'01-01"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy'\"\"mm-dd"
argument_list|,
literal|"2019'01-01"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy\'\"\"mm-dd"
argument_list|,
literal|"2019'01-01"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy\'\"\\\"\"mm-dd"
argument_list|,
literal|"2019'\"01-01"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|)
expr_stmt|;
name|checkParseTimestamp
argument_list|(
literal|"yyyy\'\"\\\"\"mm-dd"
argument_list|,
literal|"2019\'\"01-01"
argument_list|,
literal|"2019-01-01 00:00:00"
argument_list|)
expr_stmt|;
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
try|try
block|{
name|Timestamp
name|output
init|=
name|formatter
operator|.
name|parseTimestamp
argument_list|(
name|string
argument_list|)
decl_stmt|;
name|fail
argument_list|(
literal|"Parse string to timestamp should have failed.\nString: "
operator|+
name|string
operator|+
literal|"\nPattern: "
operator|+
name|pattern
operator|+
literal|", output = "
operator|+
name|output
argument_list|)
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
literal|"Expected IllegalArgumentException, got another exception."
argument_list|,
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
block|}
end_class

end_unit

