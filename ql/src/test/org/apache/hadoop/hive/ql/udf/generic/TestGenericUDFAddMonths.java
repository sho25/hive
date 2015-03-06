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
name|UDFArgumentTypeException
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
name|io
operator|.
name|ByteWritable
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
name|ShortWritable
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
name|IntWritable
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
name|TestGenericUDFAddMonths
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testAddMonthsInt
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFAddMonths
name|udf
init|=
operator|new
name|GenericUDFAddMonths
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
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
comment|// date str
name|runAndVerify
argument_list|(
literal|"2014-01-14"
argument_list|,
literal|1
argument_list|,
literal|"2014-02-14"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2014-01-31"
argument_list|,
literal|1
argument_list|,
literal|"2014-02-28"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2014-02-28"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"2014-01-31"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2014-02-28"
argument_list|,
literal|2
argument_list|,
literal|"2014-04-30"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2014-04-30"
argument_list|,
operator|-
literal|2
argument_list|,
literal|"2014-02-28"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-02-28"
argument_list|,
literal|12
argument_list|,
literal|"2016-02-29"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2016-02-29"
argument_list|,
operator|-
literal|12
argument_list|,
literal|"2015-02-28"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2016-01-29"
argument_list|,
literal|1
argument_list|,
literal|"2016-02-29"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2016-02-29"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"2016-01-31"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// wrong date str
name|runAndVerify
argument_list|(
literal|"2014-02-30"
argument_list|,
literal|1
argument_list|,
literal|"2014-04-02"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2014-02-32"
argument_list|,
literal|1
argument_list|,
literal|"2014-04-04"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2014-01"
argument_list|,
literal|1
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// ts str
name|runAndVerify
argument_list|(
literal|"2014-01-14 10:30:00"
argument_list|,
literal|1
argument_list|,
literal|"2014-02-14"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2014-01-31 10:30:00"
argument_list|,
literal|1
argument_list|,
literal|"2014-02-28"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2014-02-28 10:30:00.1"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"2014-01-31"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2014-02-28 10:30:00.100"
argument_list|,
literal|2
argument_list|,
literal|"2014-04-30"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2014-04-30 10:30:00.001"
argument_list|,
operator|-
literal|2
argument_list|,
literal|"2014-02-28"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2015-02-28 10:30:00.000000001"
argument_list|,
literal|12
argument_list|,
literal|"2016-02-29"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2016-02-29 10:30:00"
argument_list|,
operator|-
literal|12
argument_list|,
literal|"2015-02-28"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2016-01-29 10:30:00"
argument_list|,
literal|1
argument_list|,
literal|"2016-02-29"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2016-02-29 10:30:00"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"2016-01-31"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// wrong ts str
name|runAndVerify
argument_list|(
literal|"2014-02-30 10:30:00"
argument_list|,
literal|1
argument_list|,
literal|"2014-04-02"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2014-02-32 10:30:00"
argument_list|,
literal|1
argument_list|,
literal|"2014-04-04"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2014/01/31 10:30:00"
argument_list|,
literal|1
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"2014-01-31T10:30:00"
argument_list|,
literal|1
argument_list|,
literal|"2014-02-28"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testAddMonthsShort
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFAddMonths
name|udf
init|=
operator|new
name|GenericUDFAddMonths
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
name|writableShortObjectInspector
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
comment|// short
name|runAndVerify
argument_list|(
literal|"2014-01-14"
argument_list|,
operator|(
name|short
operator|)
literal|1
argument_list|,
literal|"2014-02-14"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testAddMonthsByte
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFAddMonths
name|udf
init|=
operator|new
name|GenericUDFAddMonths
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
name|writableByteObjectInspector
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
comment|// short
name|runAndVerify
argument_list|(
literal|"2014-01-14"
argument_list|,
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|"2014-02-14"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testAddMonthsLong
parameter_list|()
throws|throws
name|HiveException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"resource"
argument_list|)
name|GenericUDFAddMonths
name|udf
init|=
operator|new
name|GenericUDFAddMonths
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
name|writableLongObjectInspector
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
literal|"add_months exception expected"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UDFArgumentTypeException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"add_months test"
argument_list|,
literal|"add_months only takes INT/SHORT/BYTE types as 2nd argument, got LONG"
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
name|str
parameter_list|,
name|int
name|months
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
operator|new
name|Text
argument_list|(
name|str
argument_list|)
argument_list|)
decl_stmt|;
name|DeferredObject
name|valueObj1
init|=
operator|new
name|DeferredJavaObject
argument_list|(
operator|new
name|IntWritable
argument_list|(
name|months
argument_list|)
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
literal|"add_months() test "
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
specifier|private
name|void
name|runAndVerify
parameter_list|(
name|String
name|str
parameter_list|,
name|short
name|months
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
operator|new
name|Text
argument_list|(
name|str
argument_list|)
argument_list|)
decl_stmt|;
name|DeferredObject
name|valueObj1
init|=
operator|new
name|DeferredJavaObject
argument_list|(
operator|new
name|ShortWritable
argument_list|(
name|months
argument_list|)
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
literal|"add_months() test "
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
specifier|private
name|void
name|runAndVerify
parameter_list|(
name|String
name|str
parameter_list|,
name|byte
name|months
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
operator|new
name|Text
argument_list|(
name|str
argument_list|)
argument_list|)
decl_stmt|;
name|DeferredObject
name|valueObj1
init|=
operator|new
name|DeferredJavaObject
argument_list|(
operator|new
name|ByteWritable
argument_list|(
name|months
argument_list|)
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
literal|"add_months() test "
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

