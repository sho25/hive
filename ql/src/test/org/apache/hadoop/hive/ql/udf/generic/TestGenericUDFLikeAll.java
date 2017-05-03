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
name|HiveVarcharWritable
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
name|NullWritable
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
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestGenericUDFLikeAll
block|{
name|GenericUDFLikeAll
name|udf
init|=
literal|null
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testTrue
parameter_list|()
throws|throws
name|HiveException
block|{
name|udf
operator|=
operator|new
name|GenericUDFLikeAll
argument_list|()
expr_stmt|;
name|ObjectInspector
name|valueOIOne
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|ObjectInspector
name|valueOITwo
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|ObjectInspector
name|valueOIThree
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
name|valueOIOne
block|,
name|valueOITwo
block|,
name|valueOIThree
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
name|DeferredJavaObject
name|valueObjOne
init|=
operator|new
name|DeferredJavaObject
argument_list|(
operator|new
name|Text
argument_list|(
literal|"abc"
argument_list|)
argument_list|)
decl_stmt|;
name|DeferredJavaObject
name|valueObjTwo
init|=
operator|new
name|DeferredJavaObject
argument_list|(
operator|new
name|Text
argument_list|(
literal|"%b%"
argument_list|)
argument_list|)
decl_stmt|;
name|HiveVarchar
name|vc
init|=
operator|new
name|HiveVarchar
argument_list|()
decl_stmt|;
name|vc
operator|.
name|setValue
argument_list|(
literal|"a%"
argument_list|)
expr_stmt|;
name|GenericUDF
operator|.
name|DeferredJavaObject
index|[]
name|args
init|=
block|{
name|valueObjOne
block|,
name|valueObjTwo
block|,
operator|new
name|GenericUDF
operator|.
name|DeferredJavaObject
argument_list|(
operator|new
name|HiveVarcharWritable
argument_list|(
name|vc
argument_list|)
argument_list|)
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
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|output
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|UDFArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|expectException
parameter_list|()
throws|throws
name|HiveException
block|{
name|udf
operator|=
operator|new
name|GenericUDFLikeAll
argument_list|()
expr_stmt|;
name|ObjectInspector
name|valueOIOne
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
name|valueOIOne
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNull
parameter_list|()
throws|throws
name|HiveException
block|{
name|udf
operator|=
operator|new
name|GenericUDFLikeAll
argument_list|()
expr_stmt|;
name|ObjectInspector
name|valueOIOne
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|ObjectInspector
name|valueOITwo
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|ObjectInspector
name|valueOIThree
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
name|valueOIOne
block|,
name|valueOITwo
block|,
name|valueOIThree
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
name|valueObjOne
init|=
operator|new
name|DeferredJavaObject
argument_list|(
operator|new
name|Text
argument_list|(
literal|"linkedin"
argument_list|)
argument_list|)
decl_stmt|;
name|DeferredObject
name|valueObjTwo
init|=
operator|new
name|DeferredJavaObject
argument_list|(
operator|new
name|Text
argument_list|(
literal|"%oo%"
argument_list|)
argument_list|)
decl_stmt|;
name|DeferredObject
name|valueObjThree
init|=
operator|new
name|DeferredJavaObject
argument_list|(
name|NullWritable
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|DeferredObject
index|[]
name|args
init|=
block|{
name|valueObjOne
block|,
name|valueObjTwo
block|,
name|valueObjThree
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
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

