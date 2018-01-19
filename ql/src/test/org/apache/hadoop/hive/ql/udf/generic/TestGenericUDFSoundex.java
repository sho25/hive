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
name|UDFArgumentLengthException
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
name|TestGenericUDFSoundex
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testSoundex
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSoundex
name|udf
init|=
operator|new
name|GenericUDFSoundex
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
index|[]
name|arguments
init|=
block|{
name|valueOI0
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
literal|"Miller"
argument_list|,
literal|"M460"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"miler"
argument_list|,
literal|"M460"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"myller"
argument_list|,
literal|"M460"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"muller"
argument_list|,
literal|"M460"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"m"
argument_list|,
literal|"M000"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"mu"
argument_list|,
literal|"M000"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"mil"
argument_list|,
literal|"M400"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"Peterson"
argument_list|,
literal|"P362"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"Pittersen"
argument_list|,
literal|"P362"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|""
argument_list|,
literal|""
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
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"\u3500\u3501\u3502\u3503"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testSoundexWrongType0
parameter_list|()
throws|throws
name|HiveException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"resource"
argument_list|)
name|GenericUDFSoundex
name|udf
init|=
operator|new
name|GenericUDFSoundex
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
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
literal|"soundex test. UDFArgumentTypeException is expected"
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
literal|"soundex test"
argument_list|,
literal|"soundex only takes STRING_GROUP types as 1st argument, got INT"
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
name|testSoundexWrongLength
parameter_list|()
throws|throws
name|HiveException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"resource"
argument_list|)
name|GenericUDFSoundex
name|udf
init|=
operator|new
name|GenericUDFSoundex
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableHiveVarcharObjectInspector
decl_stmt|;
name|ObjectInspector
name|valueOI1
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableHiveVarcharObjectInspector
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
literal|"soundex test. UDFArgumentLengthException is expected"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UDFArgumentLengthException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"soundex test"
argument_list|,
literal|"soundex requires 1 argument, got 2"
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
name|str0
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
name|str0
operator|!=
literal|null
condition|?
operator|new
name|Text
argument_list|(
name|str0
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
if|if
condition|(
name|expResult
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
literal|"soundex test "
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNotNull
argument_list|(
literal|"soundex test"
argument_list|,
name|output
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"soundex test"
argument_list|,
name|expResult
argument_list|,
name|output
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

