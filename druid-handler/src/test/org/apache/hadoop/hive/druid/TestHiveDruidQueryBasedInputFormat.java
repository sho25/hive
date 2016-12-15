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
name|druid
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
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
name|druid
operator|.
name|io
operator|.
name|DruidQueryBasedInputFormat
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
name|Interval
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
name|org
operator|.
name|junit
operator|.
name|Test
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

begin_class
specifier|public
class|class
name|TestHiveDruidQueryBasedInputFormat
extends|extends
name|TestCase
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testCreateSplitsIntervals
parameter_list|()
throws|throws
name|Exception
block|{
name|DruidQueryBasedInputFormat
name|input
init|=
operator|new
name|DruidQueryBasedInputFormat
argument_list|()
decl_stmt|;
name|Method
name|method1
init|=
name|DruidQueryBasedInputFormat
operator|.
name|class
operator|.
name|getDeclaredMethod
argument_list|(
literal|"createSplitsIntervals"
argument_list|,
name|List
operator|.
name|class
argument_list|,
name|int
operator|.
name|class
argument_list|)
decl_stmt|;
name|method1
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Interval
argument_list|>
name|intervals
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|Interval
argument_list|>
argument_list|>
name|resultList
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|Interval
argument_list|>
argument_list|>
name|expectedResultList
decl_stmt|;
comment|// Test 1 : single split, create 4
name|intervals
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|intervals
operator|.
name|add
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1262304000000L
argument_list|,
literal|1293840000000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|resultList
operator|=
operator|(
name|List
argument_list|<
name|List
argument_list|<
name|Interval
argument_list|>
argument_list|>
operator|)
name|method1
operator|.
name|invoke
argument_list|(
name|input
argument_list|,
name|intervals
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|expectedResultList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1262304000000L
argument_list|,
literal|1270188000000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1270188000000L
argument_list|,
literal|1278072000000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1278072000000L
argument_list|,
literal|1285956000000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1285956000000L
argument_list|,
literal|1293840000000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedResultList
argument_list|,
name|resultList
argument_list|)
expr_stmt|;
comment|// Test 2 : two splits, create 4
name|intervals
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|intervals
operator|.
name|add
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1262304000000L
argument_list|,
literal|1293840000000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|intervals
operator|.
name|add
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1325376000000L
argument_list|,
literal|1356998400000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|resultList
operator|=
operator|(
name|List
argument_list|<
name|List
argument_list|<
name|Interval
argument_list|>
argument_list|>
operator|)
name|method1
operator|.
name|invoke
argument_list|(
name|input
argument_list|,
name|intervals
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|expectedResultList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1262304000000L
argument_list|,
literal|1278093600000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1278093600000L
argument_list|,
literal|1293840000000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Interval
argument_list|(
literal|1325376000000L
argument_list|,
literal|1325419200000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1325419200000L
argument_list|,
literal|1341208800000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1341208800000L
argument_list|,
literal|1356998400000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedResultList
argument_list|,
name|resultList
argument_list|)
expr_stmt|;
comment|// Test 3 : two splits, create 5
name|intervals
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|intervals
operator|.
name|add
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1262304000000L
argument_list|,
literal|1293840000000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|intervals
operator|.
name|add
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1325376000000L
argument_list|,
literal|1356998400000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|resultList
operator|=
operator|(
name|List
argument_list|<
name|List
argument_list|<
name|Interval
argument_list|>
argument_list|>
operator|)
name|method1
operator|.
name|invoke
argument_list|(
name|input
argument_list|,
name|intervals
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|expectedResultList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1262304000000L
argument_list|,
literal|1274935680000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1274935680000L
argument_list|,
literal|1287567360000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1287567360000L
argument_list|,
literal|1293840000000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Interval
argument_list|(
literal|1325376000000L
argument_list|,
literal|1331735040000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1331735040000L
argument_list|,
literal|1344366720000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1344366720000L
argument_list|,
literal|1356998400000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedResultList
argument_list|,
name|resultList
argument_list|)
expr_stmt|;
comment|// Test 4 : three splits, different ranges, create 6
name|intervals
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|intervals
operator|.
name|add
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1199145600000L
argument_list|,
literal|1201824000000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// one month
name|intervals
operator|.
name|add
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1325376000000L
argument_list|,
literal|1356998400000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// one year
name|intervals
operator|.
name|add
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1407283200000L
argument_list|,
literal|1407888000000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// 7 days
name|resultList
operator|=
operator|(
name|List
argument_list|<
name|List
argument_list|<
name|Interval
argument_list|>
argument_list|>
operator|)
name|method1
operator|.
name|invoke
argument_list|(
name|input
argument_list|,
name|intervals
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|expectedResultList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1199145600000L
argument_list|,
literal|1201824000000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Interval
argument_list|(
literal|1325376000000L
argument_list|,
literal|1328515200000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1328515200000L
argument_list|,
literal|1334332800000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1334332800000L
argument_list|,
literal|1340150400000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1340150400000L
argument_list|,
literal|1345968000000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1345968000000L
argument_list|,
literal|1351785600000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectedResultList
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1351785600000L
argument_list|,
literal|1356998400000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Interval
argument_list|(
literal|1407283200000L
argument_list|,
literal|1407888000000L
argument_list|,
name|ISOChronology
operator|.
name|getInstanceUTC
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedResultList
argument_list|,
name|resultList
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

