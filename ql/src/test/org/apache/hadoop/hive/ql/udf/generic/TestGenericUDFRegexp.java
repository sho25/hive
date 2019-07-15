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
name|BooleanWritable
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
name|assertNotNull
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
comment|/**  * TestGenericUDFRegexp.  */
end_comment

begin_class
specifier|public
class|class
name|TestGenericUDFRegexp
block|{
annotation|@
name|Test
specifier|public
name|void
name|testConstant
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFRegExp
name|udf
init|=
operator|new
name|GenericUDFRegExp
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
name|regexText
init|=
operator|new
name|Text
argument_list|(
literal|"^fo"
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
name|regexText
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
literal|"fofo"
argument_list|,
name|regexText
argument_list|,
literal|true
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
literal|"fofofo"
argument_list|,
name|regexText
argument_list|,
literal|true
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
literal|"fobar"
argument_list|,
name|regexText
argument_list|,
literal|true
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
literal|"barfobar"
argument_list|,
name|regexText
argument_list|,
literal|false
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerifyConst
argument_list|(
literal|null
argument_list|,
name|regexText
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
name|testEmptyConstant
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFRegExp
name|udf
init|=
operator|new
name|GenericUDFRegExp
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
name|regexText
init|=
operator|new
name|Text
argument_list|(
literal|""
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
name|regexText
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
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
comment|// empty regex (should be one WARN message)
name|runAndVerifyConst
argument_list|(
literal|"foo"
argument_list|,
name|regexText
argument_list|,
literal|false
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
literal|"bar"
argument_list|,
name|regexText
argument_list|,
literal|false
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerifyConst
argument_list|(
literal|null
argument_list|,
name|regexText
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
name|testNullConstant
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFRegExp
name|udf
init|=
operator|new
name|GenericUDFRegExp
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
name|regexText
init|=
literal|null
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
name|regexText
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
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerifyConst
argument_list|(
literal|"fofo"
argument_list|,
name|regexText
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
literal|"fofofo"
argument_list|,
name|regexText
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
literal|"fobar"
argument_list|,
name|regexText
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
literal|null
argument_list|,
name|regexText
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
name|testNonConstant
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFRegExp
name|udf
init|=
operator|new
name|GenericUDFRegExp
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
name|runAndVerify
argument_list|(
literal|"fofo"
argument_list|,
literal|"^fo"
argument_list|,
literal|true
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"fo\no"
argument_list|,
literal|"^fo\no$"
argument_list|,
literal|true
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"Bn"
argument_list|,
literal|"^Ba*n"
argument_list|,
literal|true
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"afofo"
argument_list|,
literal|"fo"
argument_list|,
literal|true
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"afofo"
argument_list|,
literal|"^fo"
argument_list|,
literal|false
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"Baan"
argument_list|,
literal|"^Ba?n"
argument_list|,
literal|false
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"axe"
argument_list|,
literal|"pi|apa"
argument_list|,
literal|false
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"pip"
argument_list|,
literal|"^(pi)*$"
argument_list|,
literal|false
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// empty regex (should be one WARN message)
name|runAndVerify
argument_list|(
literal|"bar"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"foo"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerify
argument_list|(
literal|null
argument_list|,
literal|"^fo"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"fofo"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|udf
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
name|Text
name|regexText
parameter_list|,
name|Boolean
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
name|regexText
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
name|BooleanWritable
name|output
init|=
operator|(
name|BooleanWritable
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|expResult
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNotNull
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"regexp() const test "
argument_list|,
name|expResult
operator|.
name|booleanValue
argument_list|()
argument_list|,
name|output
operator|.
name|get
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
name|String
name|regex
parameter_list|,
name|Boolean
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
name|regex
operator|!=
literal|null
condition|?
operator|new
name|Text
argument_list|(
name|regex
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
name|BooleanWritable
name|output
init|=
operator|(
name|BooleanWritable
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|expResult
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNotNull
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"regexp() test "
argument_list|,
name|expResult
operator|.
name|booleanValue
argument_list|()
argument_list|,
name|output
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

