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
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
package|;
end_package

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|DateFormat
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
name|TimeZone
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
name|HiveChar
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
name|HiveDecimal
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
name|HiveVarchar
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
name|objectinspector
operator|.
name|PrimitiveObjectInspector
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
name|objectinspector
operator|.
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
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
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
operator|.
name|PrimitiveGrouping
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
name|typeinfo
operator|.
name|DecimalTypeInfo
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
name|TestPrimitiveObjectInspectorUtils
extends|extends
name|TestCase
block|{
annotation|@
name|Test
specifier|public
name|void
name|testGetPrimitiveGrouping
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|PrimitiveGrouping
operator|.
name|NUMERIC_GROUP
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|PrimitiveCategory
operator|.
name|BYTE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrimitiveGrouping
operator|.
name|NUMERIC_GROUP
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|PrimitiveCategory
operator|.
name|SHORT
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrimitiveGrouping
operator|.
name|NUMERIC_GROUP
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|PrimitiveCategory
operator|.
name|INT
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrimitiveGrouping
operator|.
name|NUMERIC_GROUP
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|PrimitiveCategory
operator|.
name|LONG
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrimitiveGrouping
operator|.
name|NUMERIC_GROUP
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|PrimitiveCategory
operator|.
name|FLOAT
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrimitiveGrouping
operator|.
name|NUMERIC_GROUP
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|PrimitiveCategory
operator|.
name|DOUBLE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrimitiveGrouping
operator|.
name|NUMERIC_GROUP
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|PrimitiveCategory
operator|.
name|DECIMAL
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrimitiveGrouping
operator|.
name|STRING_GROUP
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|PrimitiveCategory
operator|.
name|STRING
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrimitiveGrouping
operator|.
name|DATE_GROUP
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|PrimitiveCategory
operator|.
name|DATE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrimitiveGrouping
operator|.
name|DATE_GROUP
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|PrimitiveCategory
operator|.
name|TIMESTAMP
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrimitiveGrouping
operator|.
name|BOOLEAN_GROUP
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|PrimitiveCategory
operator|.
name|BOOLEAN
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrimitiveGrouping
operator|.
name|BINARY_GROUP
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|PrimitiveCategory
operator|.
name|BINARY
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrimitiveGrouping
operator|.
name|UNKNOWN_GROUP
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|PrimitiveCategory
operator|.
name|UNKNOWN
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrimitiveGrouping
operator|.
name|VOID_GROUP
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|PrimitiveCategory
operator|.
name|VOID
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testgetTimestampWithMillisecondsInt
parameter_list|()
block|{
name|DateFormat
name|gmtDateFormat
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd HH:mm:ss.SSS"
argument_list|)
decl_stmt|;
name|gmtDateFormat
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
name|PrimitiveObjectInspector
name|voidOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|VOID
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|new
name|Object
argument_list|()
argument_list|,
name|voidOI
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|booleanOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|BOOLEAN
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1970-01-01 00:00:00.001"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
literal|true
argument_list|,
name|booleanOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1970-01-01 00:00:00.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
literal|false
argument_list|,
name|booleanOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|byteOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|BYTE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1970-01-01 00:00:00.001"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
name|byteOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1969-12-31 23:59:59.999"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|byte
operator|)
operator|-
literal|1
argument_list|,
name|byteOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|shortOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|SHORT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1970-01-01 00:00:00.001"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|short
operator|)
literal|1
argument_list|,
name|shortOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1969-12-31 23:59:59.999"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|,
name|shortOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|intOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|INT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1970-01-17 11:22:01.282"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|int
operator|)
literal|1423321282
argument_list|,
name|intOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1969-12-31 23:59:59.999"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|int
operator|)
operator|-
literal|1
argument_list|,
name|intOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|longOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|LONG
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1970-01-17 11:22:01.282"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
literal|1423321282L
argument_list|,
name|longOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1969-12-31 23:59:59.999"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|-
literal|1L
argument_list|,
name|longOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Float loses some precisions
name|PrimitiveObjectInspector
name|floatOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|FLOAT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:02:24.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
literal|1423321282.123f
argument_list|,
name|floatOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1969-12-31 23:59:58.876"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|-
literal|1.123f
argument_list|,
name|floatOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|doubleOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|DOUBLE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|double
operator|)
literal|1423321282.123
argument_list|,
name|doubleOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1969-12-31 23:59:58.877"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|double
operator|)
operator|-
literal|1.123
argument_list|,
name|doubleOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|decimalOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|DECIMAL
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:01:22.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|1423321282L
argument_list|)
argument_list|,
name|decimalOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1969-12-31 23:59:59.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
name|decimalOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|stringOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|STRING
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
name|stringOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|charOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|CHAR
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|new
name|HiveChar
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
literal|30
argument_list|)
argument_list|,
name|charOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|varcharOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|VARCHAR
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|new
name|HiveVarchar
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
literal|30
argument_list|)
argument_list|,
name|varcharOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|dateOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|DATE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 00:00:00.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
name|Date
operator|.
name|ofEpochMilli
argument_list|(
literal|1423321282123L
argument_list|)
argument_list|,
name|dateOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|timestampOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|TIMESTAMP
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
name|Timestamp
operator|.
name|ofEpochMilli
argument_list|(
literal|1423321282123L
argument_list|)
argument_list|,
name|timestampOI
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testgetTimestampWithSecondsInt
parameter_list|()
block|{
name|DateFormat
name|gmtDateFormat
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd HH:mm:ss.SSS"
argument_list|)
decl_stmt|;
name|gmtDateFormat
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
name|PrimitiveObjectInspector
name|voidOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|VOID
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|new
name|Object
argument_list|()
argument_list|,
name|voidOI
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|booleanOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|BOOLEAN
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1970-01-01 00:00:01.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
literal|true
argument_list|,
name|booleanOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1970-01-01 00:00:00.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
literal|false
argument_list|,
name|booleanOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|byteOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|BYTE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1970-01-01 00:00:01.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
name|byteOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1969-12-31 23:59:59.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|byte
operator|)
operator|-
literal|1
argument_list|,
name|byteOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|shortOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|SHORT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1970-01-01 00:00:01.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|short
operator|)
literal|1
argument_list|,
name|shortOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1969-12-31 23:59:59.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|,
name|shortOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|intOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|INT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:01:22.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|int
operator|)
literal|1423321282
argument_list|,
name|intOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1969-12-31 23:59:59.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|int
operator|)
operator|-
literal|1
argument_list|,
name|intOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|longOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|LONG
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:01:22.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
literal|1423321282L
argument_list|,
name|longOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1969-12-31 23:59:59.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|-
literal|1L
argument_list|,
name|longOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Float loses some precisions
name|PrimitiveObjectInspector
name|floatOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|FLOAT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:02:24.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
literal|1423321282.123f
argument_list|,
name|floatOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1969-12-31 23:59:58.876"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|-
literal|1.123f
argument_list|,
name|floatOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|doubleOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|DOUBLE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|double
operator|)
literal|1423321282.123
argument_list|,
name|doubleOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1969-12-31 23:59:58.877"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|(
name|double
operator|)
operator|-
literal|1.123
argument_list|,
name|doubleOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|decimalOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|DECIMAL
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:01:22.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|1423321282L
argument_list|)
argument_list|,
name|decimalOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1969-12-31 23:59:59.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|,
name|decimalOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|stringOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|STRING
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
name|stringOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|charOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|CHAR
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|new
name|HiveChar
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
literal|30
argument_list|)
argument_list|,
name|charOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|varcharOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|VARCHAR
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
operator|new
name|HiveVarchar
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
literal|30
argument_list|)
argument_list|,
name|varcharOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|dateOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|DATE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 00:00:00.000"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
name|Date
operator|.
name|ofEpochMilli
argument_list|(
literal|1423321282123L
argument_list|)
argument_list|,
name|dateOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|timestampOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|TIMESTAMP
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2015-02-07 15:01:22.123"
argument_list|,
name|gmtDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
name|Timestamp
operator|.
name|ofEpochMilli
argument_list|(
literal|1423321282123L
argument_list|)
argument_list|,
name|timestampOI
argument_list|,
literal|true
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetTimestampFromString
parameter_list|()
block|{
name|DateFormat
name|udfDateFormat
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd HH:mm:ss.SSS"
argument_list|)
decl_stmt|;
name|udfDateFormat
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
name|assertEquals
argument_list|(
literal|"2015-02-07 00:00:00.000"
argument_list|,
name|udfDateFormat
operator|.
name|format
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestampFromString
argument_list|(
literal|"2015-02-07"
argument_list|)
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetBoolean
parameter_list|()
block|{
name|String
name|mustEvaluateToTrue
index|[]
init|=
block|{
literal|"yes"
block|,
literal|"Yes"
block|,
literal|"ON"
block|,
literal|"on"
block|,
literal|"True"
block|,
literal|"1"
block|,
literal|"ANYTHING?"
block|}
decl_stmt|;
name|String
name|mustEvaluateToFalse
index|[]
init|=
block|{
literal|""
block|,
literal|"No"
block|,
literal|"OFF"
block|,
literal|"FaLsE"
block|,
literal|"0"
block|}
decl_stmt|;
for|for
control|(
name|String
name|falseStr
range|:
name|mustEvaluateToFalse
control|)
block|{
name|assertFalse
argument_list|(
name|falseStr
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getBoolean
argument_list|(
name|falseStr
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|b1
init|=
operator|(
literal|"asd"
operator|+
name|falseStr
operator|)
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|falseStr
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|parseBoolean
argument_list|(
name|b1
argument_list|,
literal|3
argument_list|,
name|falseStr
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|trueStr
range|:
name|mustEvaluateToTrue
control|)
block|{
name|assertTrue
argument_list|(
name|trueStr
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getBoolean
argument_list|(
name|trueStr
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|b1
init|=
operator|(
literal|"asd"
operator|+
name|trueStr
operator|)
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|trueStr
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|parseBoolean
argument_list|(
name|b1
argument_list|,
literal|3
argument_list|,
name|trueStr
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalToString
parameter_list|()
block|{
name|HiveDecimal
name|dec1
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
literal|"0.0"
argument_list|)
decl_stmt|;
name|PrimitiveObjectInspector
name|decOI_7_0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
operator|new
name|DecimalTypeInfo
argument_list|(
literal|7
argument_list|,
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|PrimitiveObjectInspector
name|decOI_7_1
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
operator|new
name|DecimalTypeInfo
argument_list|(
literal|7
argument_list|,
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|PrimitiveObjectInspector
name|decOI_7_3
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
operator|new
name|DecimalTypeInfo
argument_list|(
literal|7
argument_list|,
literal|3
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"0"
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getString
argument_list|(
name|dec1
argument_list|,
name|decOI_7_0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"0.0"
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getString
argument_list|(
name|dec1
argument_list|,
name|decOI_7_1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"0.000"
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getString
argument_list|(
name|dec1
argument_list|,
name|decOI_7_3
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

