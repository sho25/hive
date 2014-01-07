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
name|ql
operator|.
name|udf
operator|.
name|generic
operator|.
name|GenericUDFLTrim
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
name|TestGenericUDFLTrim
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testTrim
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFLTrim
name|udf
init|=
operator|new
name|GenericUDFLTrim
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI
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
name|runAndVerify
argument_list|(
literal|" Hello World! "
argument_list|,
literal|"Hello World! "
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"Hello World! "
argument_list|,
literal|"Hello World! "
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|" Hello World!"
argument_list|,
literal|"Hello World!"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"Hello World!"
argument_list|,
literal|"Hello World!"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerify
argument_list|(
literal|"   "
argument_list|,
literal|""
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
name|expResult
parameter_list|,
name|GenericUDF
name|udf
parameter_list|)
throws|throws
name|HiveException
block|{
name|DeferredObject
name|valueObj
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
index|[]
name|args
init|=
block|{
name|valueObj
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
literal|"ltrim() test "
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
end_class

end_unit

