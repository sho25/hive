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
name|serde
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
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|ql
operator|.
name|io
operator|.
name|parquet
operator|.
name|timestamp
operator|.
name|NanoTime
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
name|io
operator|.
name|parquet
operator|.
name|timestamp
operator|.
name|NanoTimeUtils
import|;
end_import

begin_comment
comment|/**  * Tests util-libraries used for parquet-timestamp.  */
end_comment

begin_class
specifier|public
class|class
name|TestParquetTimestampUtils
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testJulianDay
parameter_list|()
block|{
comment|//check if May 23, 1968 is Julian Day 2440000
name|Calendar
name|cal
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|YEAR
argument_list|,
literal|1968
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MONTH
argument_list|,
name|Calendar
operator|.
name|MAY
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|,
literal|23
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|HOUR_OF_DAY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|cal
operator|.
name|setTimeZone
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"GMT"
argument_list|)
argument_list|)
expr_stmt|;
name|Timestamp
name|ts
init|=
operator|new
name|Timestamp
argument_list|(
name|cal
operator|.
name|getTimeInMillis
argument_list|()
argument_list|)
decl_stmt|;
name|NanoTime
name|nt
init|=
name|NanoTimeUtils
operator|.
name|getNanoTime
argument_list|(
name|ts
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|nt
operator|.
name|getJulianDay
argument_list|()
argument_list|,
literal|2440000
argument_list|)
expr_stmt|;
name|Timestamp
name|tsFetched
init|=
name|NanoTimeUtils
operator|.
name|getTimestamp
argument_list|(
name|nt
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|tsFetched
argument_list|,
name|ts
argument_list|)
expr_stmt|;
comment|//check if 30 Julian Days between Jan 1, 2005 and Jan 31, 2005.
name|Calendar
name|cal1
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|cal1
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|YEAR
argument_list|,
literal|2005
argument_list|)
expr_stmt|;
name|cal1
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MONTH
argument_list|,
name|Calendar
operator|.
name|JANUARY
argument_list|)
expr_stmt|;
name|cal1
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|cal1
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|HOUR_OF_DAY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|cal1
operator|.
name|setTimeZone
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"GMT"
argument_list|)
argument_list|)
expr_stmt|;
name|Timestamp
name|ts1
init|=
operator|new
name|Timestamp
argument_list|(
name|cal1
operator|.
name|getTimeInMillis
argument_list|()
argument_list|)
decl_stmt|;
name|NanoTime
name|nt1
init|=
name|NanoTimeUtils
operator|.
name|getNanoTime
argument_list|(
name|ts1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Timestamp
name|ts1Fetched
init|=
name|NanoTimeUtils
operator|.
name|getTimestamp
argument_list|(
name|nt1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ts1Fetched
argument_list|,
name|ts1
argument_list|)
expr_stmt|;
name|Calendar
name|cal2
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|cal2
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|YEAR
argument_list|,
literal|2005
argument_list|)
expr_stmt|;
name|cal2
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MONTH
argument_list|,
name|Calendar
operator|.
name|JANUARY
argument_list|)
expr_stmt|;
name|cal2
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|,
literal|31
argument_list|)
expr_stmt|;
name|cal2
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|HOUR_OF_DAY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|cal2
operator|.
name|setTimeZone
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"UTC"
argument_list|)
argument_list|)
expr_stmt|;
name|Timestamp
name|ts2
init|=
operator|new
name|Timestamp
argument_list|(
name|cal2
operator|.
name|getTimeInMillis
argument_list|()
argument_list|)
decl_stmt|;
name|NanoTime
name|nt2
init|=
name|NanoTimeUtils
operator|.
name|getNanoTime
argument_list|(
name|ts2
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Timestamp
name|ts2Fetched
init|=
name|NanoTimeUtils
operator|.
name|getTimestamp
argument_list|(
name|nt2
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ts2Fetched
argument_list|,
name|ts2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|nt2
operator|.
name|getJulianDay
argument_list|()
operator|-
name|nt1
operator|.
name|getJulianDay
argument_list|()
argument_list|,
literal|30
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testNanos
parameter_list|()
block|{
comment|//case 1: 01:01:01.0000000001
name|Calendar
name|cal
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|YEAR
argument_list|,
literal|1968
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MONTH
argument_list|,
name|Calendar
operator|.
name|MAY
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|,
literal|23
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|HOUR_OF_DAY
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MINUTE
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|SECOND
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|cal
operator|.
name|setTimeZone
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"GMT"
argument_list|)
argument_list|)
expr_stmt|;
name|Timestamp
name|ts
init|=
operator|new
name|Timestamp
argument_list|(
name|cal
operator|.
name|getTimeInMillis
argument_list|()
argument_list|)
decl_stmt|;
name|ts
operator|.
name|setNanos
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|//(1*60*60 + 1*60 + 1) * 10e9 + 1
name|NanoTime
name|nt
init|=
name|NanoTimeUtils
operator|.
name|getNanoTime
argument_list|(
name|ts
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|nt
operator|.
name|getTimeOfDayNanos
argument_list|()
argument_list|,
literal|3661000000001L
argument_list|)
expr_stmt|;
comment|//case 2: 23:59:59.999999999
name|cal
operator|=
name|Calendar
operator|.
name|getInstance
argument_list|()
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|YEAR
argument_list|,
literal|1968
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MONTH
argument_list|,
name|Calendar
operator|.
name|MAY
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|,
literal|23
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|HOUR_OF_DAY
argument_list|,
literal|23
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MINUTE
argument_list|,
literal|59
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|SECOND
argument_list|,
literal|59
argument_list|)
expr_stmt|;
name|cal
operator|.
name|setTimeZone
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"GMT"
argument_list|)
argument_list|)
expr_stmt|;
name|ts
operator|=
operator|new
name|Timestamp
argument_list|(
name|cal
operator|.
name|getTimeInMillis
argument_list|()
argument_list|)
expr_stmt|;
name|ts
operator|.
name|setNanos
argument_list|(
literal|999999999
argument_list|)
expr_stmt|;
comment|//(23*60*60 + 59*60 + 59)*10e9 + 999999999
name|nt
operator|=
name|NanoTimeUtils
operator|.
name|getNanoTime
argument_list|(
name|ts
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|nt
operator|.
name|getTimeOfDayNanos
argument_list|()
argument_list|,
literal|86399999999999L
argument_list|)
expr_stmt|;
comment|//case 3: verify the difference.
name|Calendar
name|cal2
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|cal2
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|YEAR
argument_list|,
literal|1968
argument_list|)
expr_stmt|;
name|cal2
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MONTH
argument_list|,
name|Calendar
operator|.
name|MAY
argument_list|)
expr_stmt|;
name|cal2
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|,
literal|23
argument_list|)
expr_stmt|;
name|cal2
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|HOUR_OF_DAY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|cal2
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MINUTE
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|cal2
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|SECOND
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|cal2
operator|.
name|setTimeZone
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"GMT"
argument_list|)
argument_list|)
expr_stmt|;
name|Timestamp
name|ts2
init|=
operator|new
name|Timestamp
argument_list|(
name|cal2
operator|.
name|getTimeInMillis
argument_list|()
argument_list|)
decl_stmt|;
name|ts2
operator|.
name|setNanos
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|Calendar
name|cal1
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|cal1
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|YEAR
argument_list|,
literal|1968
argument_list|)
expr_stmt|;
name|cal1
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MONTH
argument_list|,
name|Calendar
operator|.
name|MAY
argument_list|)
expr_stmt|;
name|cal1
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|,
literal|23
argument_list|)
expr_stmt|;
name|cal1
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|HOUR_OF_DAY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|cal1
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MINUTE
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|cal1
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|SECOND
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|cal1
operator|.
name|setTimeZone
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"GMT"
argument_list|)
argument_list|)
expr_stmt|;
name|Timestamp
name|ts1
init|=
operator|new
name|Timestamp
argument_list|(
name|cal1
operator|.
name|getTimeInMillis
argument_list|()
argument_list|)
decl_stmt|;
name|ts1
operator|.
name|setNanos
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|NanoTime
name|n2
init|=
name|NanoTimeUtils
operator|.
name|getNanoTime
argument_list|(
name|ts2
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|NanoTime
name|n1
init|=
name|NanoTimeUtils
operator|.
name|getNanoTime
argument_list|(
name|ts1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|n2
operator|.
name|getTimeOfDayNanos
argument_list|()
operator|-
name|n1
operator|.
name|getTimeOfDayNanos
argument_list|()
argument_list|,
literal|600000000009L
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testTimezone
parameter_list|()
block|{
name|Calendar
name|cal
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|YEAR
argument_list|,
literal|1968
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MONTH
argument_list|,
name|Calendar
operator|.
name|MAY
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|,
literal|23
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|HOUR_OF_DAY
argument_list|,
literal|17
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|MINUTE
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|cal
operator|.
name|set
argument_list|(
name|Calendar
operator|.
name|SECOND
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|cal
operator|.
name|setTimeZone
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"US/Pacific"
argument_list|)
argument_list|)
expr_stmt|;
name|Timestamp
name|ts
init|=
operator|new
name|Timestamp
argument_list|(
name|cal
operator|.
name|getTimeInMillis
argument_list|()
argument_list|)
decl_stmt|;
name|ts
operator|.
name|setNanos
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|/**      * 17:00 PDT = 00:00 GMT (daylight-savings)      * (0*60*60 + 1*60 + 1)*10e9 + 1 = 61000000001, or      *      * 17:00 PST = 01:00 GMT (if not daylight savings)      * (1*60*60 + 1*60 + 1)*10e9 + 1 = 3661000000001      */
name|NanoTime
name|nt
init|=
name|NanoTimeUtils
operator|.
name|getNanoTime
argument_list|(
name|ts
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|long
name|timeOfDayNanos
init|=
name|nt
operator|.
name|getTimeOfDayNanos
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|timeOfDayNanos
operator|==
literal|61000000001L
operator|||
name|timeOfDayNanos
operator|==
literal|3661000000001L
argument_list|)
expr_stmt|;
comment|//in both cases, this will be the next day in GMT
name|Assert
operator|.
name|assertEquals
argument_list|(
name|nt
operator|.
name|getJulianDay
argument_list|()
argument_list|,
literal|2440001
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testTimezoneValues
parameter_list|()
block|{
name|valueTest
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testTimezonelessValues
parameter_list|()
block|{
name|valueTest
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testTimezoneless
parameter_list|()
block|{
name|Timestamp
name|ts1
init|=
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"2011-01-01 00:30:30.111111111"
argument_list|)
decl_stmt|;
name|NanoTime
name|nt1
init|=
name|NanoTimeUtils
operator|.
name|getNanoTime
argument_list|(
name|ts1
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|nt1
operator|.
name|getJulianDay
argument_list|()
argument_list|,
literal|2455563
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|nt1
operator|.
name|getTimeOfDayNanos
argument_list|()
argument_list|,
literal|1830111111111L
argument_list|)
expr_stmt|;
name|Timestamp
name|ts1Fetched
init|=
name|NanoTimeUtils
operator|.
name|getTimestamp
argument_list|(
name|nt1
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ts1Fetched
operator|.
name|toString
argument_list|()
argument_list|,
name|ts1
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Timestamp
name|ts2
init|=
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"2011-02-02 08:30:30.222222222"
argument_list|)
decl_stmt|;
name|NanoTime
name|nt2
init|=
name|NanoTimeUtils
operator|.
name|getNanoTime
argument_list|(
name|ts2
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|nt2
operator|.
name|getJulianDay
argument_list|()
argument_list|,
literal|2455595
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|nt2
operator|.
name|getTimeOfDayNanos
argument_list|()
argument_list|,
literal|30630222222222L
argument_list|)
expr_stmt|;
name|Timestamp
name|ts2Fetched
init|=
name|NanoTimeUtils
operator|.
name|getTimestamp
argument_list|(
name|nt2
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ts2Fetched
operator|.
name|toString
argument_list|()
argument_list|,
name|ts2
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|valueTest
parameter_list|(
name|boolean
name|local
parameter_list|)
block|{
comment|//exercise a broad range of timestamps close to the present.
name|verifyTsString
argument_list|(
literal|"2011-01-01 01:01:01.111111111"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2012-02-02 02:02:02.222222222"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2013-03-03 03:03:03.333333333"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2014-04-04 04:04:04.444444444"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2015-05-05 05:05:05.555555555"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2016-06-06 06:06:06.666666666"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2017-07-07 07:07:07.777777777"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2018-08-08 08:08:08.888888888"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2019-09-09 09:09:09.999999999"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2020-10-10 10:10:10.101010101"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2021-11-11 11:11:11.111111111"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2022-12-12 12:12:12.121212121"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2023-01-02 13:13:13.131313131"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2024-02-02 14:14:14.141414141"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2025-03-03 15:15:15.151515151"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2026-04-04 16:16:16.161616161"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2027-05-05 17:17:17.171717171"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2028-06-06 18:18:18.181818181"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2029-07-07 19:19:19.191919191"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2030-08-08 20:20:20.202020202"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"2031-09-09 21:21:21.212121212"
argument_list|,
name|local
argument_list|)
expr_stmt|;
comment|//test some extreme cases.
name|verifyTsString
argument_list|(
literal|"9999-09-09 09:09:09.999999999"
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|verifyTsString
argument_list|(
literal|"0001-01-01 00:00:00.0"
argument_list|,
name|local
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyTsString
parameter_list|(
name|String
name|tsString
parameter_list|,
name|boolean
name|local
parameter_list|)
block|{
name|Timestamp
name|ts
init|=
name|Timestamp
operator|.
name|valueOf
argument_list|(
name|tsString
argument_list|)
decl_stmt|;
name|NanoTime
name|nt
init|=
name|NanoTimeUtils
operator|.
name|getNanoTime
argument_list|(
name|ts
argument_list|,
name|local
argument_list|)
decl_stmt|;
name|Timestamp
name|tsFetched
init|=
name|NanoTimeUtils
operator|.
name|getTimestamp
argument_list|(
name|nt
argument_list|,
name|local
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|tsString
argument_list|,
name|tsFetched
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

