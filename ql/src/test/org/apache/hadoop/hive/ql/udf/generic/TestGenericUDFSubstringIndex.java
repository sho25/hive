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
name|hive
operator|.
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
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
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * TestGenericUDFSubstringIndex.  */
end_comment

begin_class
specifier|public
class|class
name|TestGenericUDFSubstringIndex
block|{
annotation|@
name|Test
specifier|public
name|void
name|testSubstringIndex
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSubstringIndex
name|udf
init|=
operator|new
name|GenericUDFSubstringIndex
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
name|valueOI2
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
block|,
name|valueOI2
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"www.apache.org"
argument_list|,
literal|"."
argument_list|,
literal|3
argument_list|,
literal|"www.apache.org"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"www.apache.org"
argument_list|,
literal|"."
argument_list|,
literal|2
argument_list|,
literal|"www.apache"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"www.apache.org"
argument_list|,
literal|"."
argument_list|,
literal|1
argument_list|,
literal|"www"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"www.apache.org"
argument_list|,
literal|"."
argument_list|,
literal|0
argument_list|,
literal|""
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"www.apache.org"
argument_list|,
literal|"."
argument_list|,
operator|-
literal|1
argument_list|,
literal|"org"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"www.apache.org"
argument_list|,
literal|"."
argument_list|,
operator|-
literal|2
argument_list|,
literal|"apache.org"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"www.apache.org"
argument_list|,
literal|"."
argument_list|,
operator|-
literal|3
argument_list|,
literal|"www.apache.org"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// str is empty string
name|runAndVerify
argument_list|(
literal|""
argument_list|,
literal|"."
argument_list|,
literal|1
argument_list|,
literal|""
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// empty string delim
name|runAndVerify
argument_list|(
literal|"www.apache.org"
argument_list|,
literal|""
argument_list|,
literal|1
argument_list|,
literal|""
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// delim does not exist in str
name|runAndVerify
argument_list|(
literal|"www.apache.org"
argument_list|,
literal|"-"
argument_list|,
literal|2
argument_list|,
literal|"www.apache.org"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// delim is 2 chars
name|runAndVerify
argument_list|(
literal|"www||apache||org"
argument_list|,
literal|"||"
argument_list|,
literal|2
argument_list|,
literal|"www||apache"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerify
argument_list|(
literal|null
argument_list|,
literal|"."
argument_list|,
literal|2
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"www.apache.org"
argument_list|,
literal|null
argument_list|,
literal|2
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"www.apache.org"
argument_list|,
literal|"."
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSubstringIndexConst
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSubstringIndex
name|udf
init|=
operator|new
name|GenericUDFSubstringIndex
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|Text
name|delim
init|=
operator|new
name|Text
argument_list|(
literal|"."
argument_list|)
decl_stmt|;
name|ObjectInspector
name|valueOI1
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableConstantObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|delim
argument_list|)
decl_stmt|;
name|IntWritable
name|count
init|=
operator|new
name|IntWritable
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|ObjectInspector
name|valueOI2
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableConstantObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
name|count
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|valueOI0
block|,
name|valueOI1
block|,
name|valueOI2
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
literal|"www.apache.org"
argument_list|,
literal|"www.apache"
argument_list|,
name|udf
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
name|String
name|delim
parameter_list|,
name|Integer
name|count
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
name|str
operator|!=
literal|null
condition|?
operator|new
name|Text
argument_list|(
name|str
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
name|delim
operator|!=
literal|null
condition|?
operator|new
name|Text
argument_list|(
name|delim
argument_list|)
else|:
name|delim
argument_list|)
decl_stmt|;
name|DeferredObject
name|valueObj2
init|=
operator|new
name|DeferredJavaObject
argument_list|(
name|count
operator|!=
literal|null
condition|?
operator|new
name|IntWritable
argument_list|(
name|count
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
block|,
name|valueObj2
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
literal|"substring_index() test "
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
name|runAndVerifyConst
parameter_list|(
name|String
name|str
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
name|str
operator|!=
literal|null
condition|?
operator|new
name|Text
argument_list|(
name|str
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
literal|"substring_index() test "
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

