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
name|ql
operator|.
name|udf
operator|.
name|generic
package|;
end_package

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
name|io
operator|.
name|DateWritableV2
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
name|TimestampWritableV2
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
name|assertNull
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * TestGenericUDFDate.  */
end_comment

begin_class
specifier|public
class|class
name|TestGenericUDFDate
block|{
annotation|@
name|Test
specifier|public
name|void
name|testStringToDate
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFDate
name|udf
init|=
operator|new
name|GenericUDFDate
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|valueOI
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
name|DeferredObject
name|valueObj
init|=
operator|new
name|DeferredJavaObject
argument_list|(
operator|new
name|Text
argument_list|(
literal|"2009-07-30"
argument_list|)
argument_list|)
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
name|valueObj
block|}
decl_stmt|;
name|DateWritableV2
name|output
init|=
operator|(
name|DateWritableV2
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
literal|"to_date() test for STRING failed "
argument_list|,
literal|"2009-07-30"
argument_list|,
name|output
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Try with null args
name|DeferredObject
index|[]
name|nullArgs
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
literal|null
argument_list|)
block|}
decl_stmt|;
name|output
operator|=
operator|(
name|DateWritableV2
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|nullArgs
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"to_date() with null STRING"
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimestampToDate
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFDate
name|udf
init|=
operator|new
name|GenericUDFDate
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableTimestampObjectInspector
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|valueOI
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
name|DeferredObject
name|valueObj
init|=
operator|new
name|DeferredJavaObject
argument_list|(
operator|new
name|TimestampWritableV2
argument_list|(
name|Timestamp
operator|.
name|valueOf
argument_list|(
name|LocalDateTime
operator|.
name|of
argument_list|(
literal|109
argument_list|,
literal|06
argument_list|,
literal|30
argument_list|,
literal|4
argument_list|,
literal|17
argument_list|,
literal|52
argument_list|,
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
name|valueObj
block|}
decl_stmt|;
name|DateWritableV2
name|output
init|=
operator|(
name|DateWritableV2
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
literal|"to_date() test for TIMESTAMP failed "
argument_list|,
literal|"0109-06-30"
argument_list|,
name|output
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Try with null args
name|DeferredObject
index|[]
name|nullArgs
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
literal|null
argument_list|)
block|}
decl_stmt|;
name|output
operator|=
operator|(
name|DateWritableV2
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|nullArgs
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"to_date() with null TIMESTAMP"
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDateWritablepToDate
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFDate
name|udf
init|=
operator|new
name|GenericUDFDate
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDateObjectInspector
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|valueOI
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
name|DeferredObject
name|valueObj
init|=
operator|new
name|DeferredJavaObject
argument_list|(
operator|new
name|DateWritableV2
argument_list|(
name|Date
operator|.
name|of
argument_list|(
literal|109
argument_list|,
literal|06
argument_list|,
literal|30
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
name|valueObj
block|}
decl_stmt|;
name|DateWritableV2
name|output
init|=
operator|(
name|DateWritableV2
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
literal|"to_date() test for DATEWRITABLE failed "
argument_list|,
literal|"0109-06-30"
argument_list|,
name|output
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Try with null args
name|DeferredObject
index|[]
name|nullArgs
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
literal|null
argument_list|)
block|}
decl_stmt|;
name|output
operator|=
operator|(
name|DateWritableV2
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|nullArgs
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"to_date() with null DATE"
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVoidToDate
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFDate
name|udf
init|=
operator|new
name|GenericUDFDate
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableVoidObjectInspector
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|valueOI
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
literal|null
argument_list|)
block|}
decl_stmt|;
name|DateWritableV2
name|output
init|=
operator|(
name|DateWritableV2
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
comment|// Try with null VOID
name|assertNull
argument_list|(
literal|"to_date() with null DATE "
argument_list|,
name|output
argument_list|)
expr_stmt|;
comment|// Try with erroneously generated VOID
name|DeferredObject
index|[]
name|junkArgs
init|=
block|{
operator|new
name|DeferredJavaObject
argument_list|(
operator|new
name|Text
argument_list|(
literal|"2015-11-22"
argument_list|)
argument_list|)
block|}
decl_stmt|;
try|try
block|{
name|udf
operator|.
name|evaluate
argument_list|(
name|junkArgs
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"to_date() test with VOID non-null failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UDFArgumentException
name|udfae
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"TO_DATE() received non-null object of VOID type"
argument_list|,
name|udfae
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

