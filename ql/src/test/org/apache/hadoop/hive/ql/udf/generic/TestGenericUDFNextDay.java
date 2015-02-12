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
operator|.
name|generic
package|;
end_package

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
name|exec
operator|.
name|UDFArgumentException
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
name|metadata
operator|.
name|HiveException
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
operator|.
name|DeferredJavaObject
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
operator|.
name|DeferredObject
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
name|ObjectInspector
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
name|PrimitiveObjectInspectorFactory
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
name|io
operator|.
name|Text
import|;
end_import

begin_class
specifier|public
class|class
name|TestGenericUDFNextDay
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testNextDay
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFNextDay
name|udf
init|=
operator|new
name|GenericUDFNextDay
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|ObjectInspector
name|valueOI1
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|valueOI0
block|,
name|valueOI1
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
comment|// start_date is Sun, 2 letters day name
name|runAndVerify
argument_list|(
literal|"2015-01-11"
argument_list|,
literal|"su"
argument_list|,
literal|"2015-01-18"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-11"
argument_list|,
literal|"MO"
argument_list|,
literal|"2015-01-12"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-11"
argument_list|,
literal|"Tu"
argument_list|,
literal|"2015-01-13"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-11"
argument_list|,
literal|"wE"
argument_list|,
literal|"2015-01-14"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-11"
argument_list|,
literal|"th"
argument_list|,
literal|"2015-01-15"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-11"
argument_list|,
literal|"FR"
argument_list|,
literal|"2015-01-16"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-11"
argument_list|,
literal|"Sa"
argument_list|,
literal|"2015-01-17"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// start_date is Sat, 3 letters day name
name|runAndVerify
argument_list|(
literal|"2015-01-17"
argument_list|,
literal|"sun"
argument_list|,
literal|"2015-01-18"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-17"
argument_list|,
literal|"MON"
argument_list|,
literal|"2015-01-19"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-17"
argument_list|,
literal|"Tue"
argument_list|,
literal|"2015-01-20"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-17"
argument_list|,
literal|"weD"
argument_list|,
literal|"2015-01-21"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-17"
argument_list|,
literal|"tHu"
argument_list|,
literal|"2015-01-22"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-17"
argument_list|,
literal|"FrI"
argument_list|,
literal|"2015-01-23"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-17"
argument_list|,
literal|"SAt"
argument_list|,
literal|"2015-01-24"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// start_date is Wed, full timestamp, full day name
name|runAndVerify
argument_list|(
literal|"2015-01-14 14:04:34"
argument_list|,
literal|"sunday"
argument_list|,
literal|"2015-01-18"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-14 14:04:34"
argument_list|,
literal|"Monday"
argument_list|,
literal|"2015-01-19"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-14 14:04:34"
argument_list|,
literal|"Tuesday"
argument_list|,
literal|"2015-01-20"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-14 14:04:34"
argument_list|,
literal|"wednesday"
argument_list|,
literal|"2015-01-21"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-14 14:04:34"
argument_list|,
literal|"thursDAY"
argument_list|,
literal|"2015-01-15"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-14 14:04:34"
argument_list|,
literal|"FRIDAY"
argument_list|,
literal|"2015-01-16"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-14 14:04:34"
argument_list|,
literal|"SATurday"
argument_list|,
literal|"2015-01-17"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null values
name|runAndVerify
argument_list|(
literal|"2015-01-14"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|null
argument_list|,
literal|"SU"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// not valid values
name|runAndVerify
argument_list|(
literal|"01/14/2015"
argument_list|,
literal|"TU"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-01-14"
argument_list|,
literal|"VT"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testNextDayErrorArg1
parameter_list|()
throws|throws
name|HiveException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"resource"
argument_list|)
name|GenericUDFNextDay
name|udf
init|=
operator|new
name|GenericUDFNextDay
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
decl_stmt|;
name|ObjectInspector
name|valueOI1
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|valueOI0
block|,
name|valueOI1
block|}
decl_stmt|;
try|try
block|{
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"UDFArgumentException expected"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UDFArgumentException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"next_day() only takes STRING/TIMESTAMP/DATEWRITABLE types as first argument, got LONG"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testNextDayErrorArg2
parameter_list|()
throws|throws
name|HiveException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"resource"
argument_list|)
name|GenericUDFNextDay
name|udf
init|=
operator|new
name|GenericUDFNextDay
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|ObjectInspector
name|valueOI1
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|valueOI0
block|,
name|valueOI1
block|}
decl_stmt|;
try|try
block|{
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"UDFArgumentException expected"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UDFArgumentException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"next_day() only takes STRING_GROUP types as second argument, got INT"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|runAndVerify
parameter_list|(
name|String
name|date
parameter_list|,
name|String
name|dayOfWeek
parameter_list|,
name|String
name|expResult
parameter_list|,
name|GenericUDF
name|udf
parameter_list|)
throws|throws
name|HiveException
block|{
name|DeferredObject
name|valueObj0
init|=
operator|new
name|DeferredJavaObject
argument_list|(
name|date
operator|!=
literal|null
condition|?
operator|new
name|Text
argument_list|(
name|date
argument_list|)
else|:
literal|null
argument_list|)
decl_stmt|;
name|DeferredObject
name|valueObj1
init|=
operator|new
name|DeferredJavaObject
argument_list|(
name|dayOfWeek
operator|!=
literal|null
condition|?
operator|new
name|Text
argument_list|(
name|dayOfWeek
argument_list|)
else|:
literal|null
argument_list|)
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
name|valueObj0
block|,
name|valueObj1
block|}
decl_stmt|;
name|Text
name|output
init|=
operator|(
name|Text
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"next_day() test "
argument_list|,
name|expResult
argument_list|,
name|output
operator|!=
literal|null
condition|?
name|output
operator|.
name|toString
argument_list|()
else|:
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

