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
name|BytesWritable
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
comment|/**  * TestGenericUDFSha2.  */
end_comment

begin_class
specifier|public
class|class
name|TestGenericUDFSha2
block|{
annotation|@
name|Test
specifier|public
name|void
name|testSha0Str
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSha2
name|udf
init|=
operator|new
name|GenericUDFSha2
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|IntWritable
name|lenWr
init|=
operator|new
name|IntWritable
argument_list|(
literal|0
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
name|intTypeInfo
argument_list|,
name|lenWr
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
name|runAndVerifyStr
argument_list|(
literal|"ABC"
argument_list|,
name|lenWr
argument_list|,
literal|"b5d4045c3f466fa91fe2cc6abe79232a1a57cdf104f7a26e716e0a1e2789df78"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|""
argument_list|,
name|lenWr
argument_list|,
literal|"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerifyStr
argument_list|(
literal|null
argument_list|,
name|lenWr
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
name|testSha0Bin
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSha2
name|udf
init|=
operator|new
name|GenericUDFSha2
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBinaryObjectInspector
decl_stmt|;
name|IntWritable
name|lenWr
init|=
operator|new
name|IntWritable
argument_list|(
literal|0
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
name|intTypeInfo
argument_list|,
name|lenWr
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
name|runAndVerifyBin
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|65
block|,
literal|66
block|,
literal|67
block|}
argument_list|,
name|lenWr
argument_list|,
literal|"b5d4045c3f466fa91fe2cc6abe79232a1a57cdf104f7a26e716e0a1e2789df78"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyBin
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|lenWr
argument_list|,
literal|"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerifyBin
argument_list|(
literal|null
argument_list|,
name|lenWr
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
name|testSha200Str
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSha2
name|udf
init|=
operator|new
name|GenericUDFSha2
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|IntWritable
name|lenWr
init|=
operator|new
name|IntWritable
argument_list|(
literal|200
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
name|intTypeInfo
argument_list|,
name|lenWr
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
name|runAndVerifyStr
argument_list|(
literal|"ABC"
argument_list|,
name|lenWr
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
name|testSha200Bin
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSha2
name|udf
init|=
operator|new
name|GenericUDFSha2
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBinaryObjectInspector
decl_stmt|;
name|IntWritable
name|lenWr
init|=
operator|new
name|IntWritable
argument_list|(
literal|200
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
name|intTypeInfo
argument_list|,
name|lenWr
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
name|runAndVerifyBin
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|65
block|,
literal|66
block|,
literal|67
block|}
argument_list|,
name|lenWr
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
name|testSha256Str
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSha2
name|udf
init|=
operator|new
name|GenericUDFSha2
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|IntWritable
name|lenWr
init|=
operator|new
name|IntWritable
argument_list|(
literal|256
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
name|intTypeInfo
argument_list|,
name|lenWr
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
name|runAndVerifyStr
argument_list|(
literal|"ABC"
argument_list|,
name|lenWr
argument_list|,
literal|"b5d4045c3f466fa91fe2cc6abe79232a1a57cdf104f7a26e716e0a1e2789df78"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|""
argument_list|,
name|lenWr
argument_list|,
literal|"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerifyStr
argument_list|(
literal|null
argument_list|,
name|lenWr
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
name|testSha256Bin
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSha2
name|udf
init|=
operator|new
name|GenericUDFSha2
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBinaryObjectInspector
decl_stmt|;
name|IntWritable
name|lenWr
init|=
operator|new
name|IntWritable
argument_list|(
literal|256
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
name|intTypeInfo
argument_list|,
name|lenWr
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
name|runAndVerifyBin
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|65
block|,
literal|66
block|,
literal|67
block|}
argument_list|,
name|lenWr
argument_list|,
literal|"b5d4045c3f466fa91fe2cc6abe79232a1a57cdf104f7a26e716e0a1e2789df78"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyBin
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|lenWr
argument_list|,
literal|"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerifyBin
argument_list|(
literal|null
argument_list|,
name|lenWr
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
name|testSha384Str
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSha2
name|udf
init|=
operator|new
name|GenericUDFSha2
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|IntWritable
name|lenWr
init|=
operator|new
name|IntWritable
argument_list|(
literal|384
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
name|intTypeInfo
argument_list|,
name|lenWr
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
name|runAndVerifyStr
argument_list|(
literal|"ABC"
argument_list|,
name|lenWr
argument_list|,
literal|"1e02dc92a41db610c9bcdc9b5935d1fb9be5639116f6c67e97bc1a3ac649753baba7ba021c813e1fe20c0480213ad371"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|""
argument_list|,
name|lenWr
argument_list|,
literal|"38b060a751ac96384cd9327eb1b1e36a21fdb71114be07434c0cc7bf63f6e1da274edebfe76f65fbd51ad2f14898b95b"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerifyStr
argument_list|(
literal|null
argument_list|,
name|lenWr
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
name|testSha384Bin
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSha2
name|udf
init|=
operator|new
name|GenericUDFSha2
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBinaryObjectInspector
decl_stmt|;
name|IntWritable
name|lenWr
init|=
operator|new
name|IntWritable
argument_list|(
literal|384
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
name|intTypeInfo
argument_list|,
name|lenWr
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
name|runAndVerifyBin
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|65
block|,
literal|66
block|,
literal|67
block|}
argument_list|,
name|lenWr
argument_list|,
literal|"1e02dc92a41db610c9bcdc9b5935d1fb9be5639116f6c67e97bc1a3ac649753baba7ba021c813e1fe20c0480213ad371"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyBin
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|lenWr
argument_list|,
literal|"38b060a751ac96384cd9327eb1b1e36a21fdb71114be07434c0cc7bf63f6e1da274edebfe76f65fbd51ad2f14898b95b"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerifyBin
argument_list|(
literal|null
argument_list|,
name|lenWr
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
name|testSha512Str
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSha2
name|udf
init|=
operator|new
name|GenericUDFSha2
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|IntWritable
name|lenWr
init|=
operator|new
name|IntWritable
argument_list|(
literal|512
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
name|intTypeInfo
argument_list|,
name|lenWr
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
name|runAndVerifyStr
argument_list|(
literal|"ABC"
argument_list|,
name|lenWr
argument_list|,
literal|"397118fdac8d83ad98813c50759c85b8c47565d8268bf10da483153b747a74743a58a90e85aa9f705ce6984ffc128db567489817e4092d050d8a1cc596ddc119"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|""
argument_list|,
name|lenWr
argument_list|,
literal|"cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerifyStr
argument_list|(
literal|null
argument_list|,
name|lenWr
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
name|testSha512Bin
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSha2
name|udf
init|=
operator|new
name|GenericUDFSha2
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBinaryObjectInspector
decl_stmt|;
name|IntWritable
name|lenWr
init|=
operator|new
name|IntWritable
argument_list|(
literal|512
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
name|intTypeInfo
argument_list|,
name|lenWr
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
name|runAndVerifyBin
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|65
block|,
literal|66
block|,
literal|67
block|}
argument_list|,
name|lenWr
argument_list|,
literal|"397118fdac8d83ad98813c50759c85b8c47565d8268bf10da483153b747a74743a58a90e85aa9f705ce6984ffc128db567489817e4092d050d8a1cc596ddc119"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyBin
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|lenWr
argument_list|,
literal|"cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerifyBin
argument_list|(
literal|null
argument_list|,
name|lenWr
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
name|testShaNullStr
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSha2
name|udf
init|=
operator|new
name|GenericUDFSha2
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
name|IntWritable
name|lenWr
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
name|intTypeInfo
argument_list|,
name|lenWr
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
name|runAndVerifyStr
argument_list|(
literal|"ABC"
argument_list|,
name|lenWr
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
name|testShaNullBin
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFSha2
name|udf
init|=
operator|new
name|GenericUDFSha2
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBinaryObjectInspector
decl_stmt|;
name|IntWritable
name|lenWr
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
name|intTypeInfo
argument_list|,
name|lenWr
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
name|runAndVerifyBin
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|65
block|,
literal|66
block|,
literal|67
block|}
argument_list|,
name|lenWr
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runAndVerifyStr
parameter_list|(
name|String
name|str
parameter_list|,
name|IntWritable
name|lenWr
parameter_list|,
name|String
name|expResult
parameter_list|,
name|GenericUDFSha2
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
name|lenWr
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
literal|"sha2() test "
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
name|runAndVerifyBin
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|IntWritable
name|lenWr
parameter_list|,
name|String
name|expResult
parameter_list|,
name|GenericUDFSha2
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
name|b
operator|!=
literal|null
condition|?
operator|new
name|BytesWritable
argument_list|(
name|b
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
name|lenWr
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
literal|"sha2() test "
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

