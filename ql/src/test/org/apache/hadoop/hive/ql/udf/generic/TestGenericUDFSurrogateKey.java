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
name|exec
operator|.
name|MapredContext
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
name|tez
operator|.
name|TezContext
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
name|ConstantObjectInspector
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
name|LongWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|ProcessorContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|ExpectedException
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
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
specifier|public
class|class
name|TestGenericUDFSurrogateKey
block|{
specifier|private
name|GenericUDFSurrogateKey
name|udf
decl_stmt|;
specifier|private
name|TezContext
name|mockTezContext
decl_stmt|;
specifier|private
name|ProcessorContext
name|mockProcessorContest
decl_stmt|;
specifier|private
name|ObjectInspector
index|[]
name|emptyArguments
init|=
block|{}
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|ExpectedException
name|expectedException
init|=
name|ExpectedException
operator|.
name|none
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|init
parameter_list|()
block|{
name|udf
operator|=
operator|new
name|GenericUDFSurrogateKey
argument_list|()
expr_stmt|;
name|mockTezContext
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|TezContext
operator|.
name|class
argument_list|)
expr_stmt|;
name|mockProcessorContest
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ProcessorContext
operator|.
name|class
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockTezContext
operator|.
name|getTezProcessorContext
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockProcessorContest
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSurrogateKeyDefault
parameter_list|()
throws|throws
name|HiveException
block|{
name|when
argument_list|(
name|mockProcessorContest
operator|.
name|getTaskIndex
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|emptyArguments
argument_list|)
expr_stmt|;
name|udf
operator|.
name|configure
argument_list|(
name|mockTezContext
argument_list|)
expr_stmt|;
name|udf
operator|.
name|setWriteId
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
operator|(
literal|1L
operator|<<
literal|40
operator|)
operator|+
operator|(
literal|1L
operator|<<
literal|24
operator|)
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
operator|(
literal|1L
operator|<<
literal|40
operator|)
operator|+
operator|(
literal|1L
operator|<<
literal|24
operator|)
operator|+
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
operator|(
literal|1L
operator|<<
literal|40
operator|)
operator|+
operator|(
literal|1L
operator|<<
literal|24
operator|)
operator|+
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSurrogateKeyBitsSet
parameter_list|()
throws|throws
name|HiveException
block|{
name|when
argument_list|(
name|mockProcessorContest
operator|.
name|getTaskIndex
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|getArguments
argument_list|(
literal|10
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|udf
operator|.
name|configure
argument_list|(
name|mockTezContext
argument_list|)
expr_stmt|;
name|udf
operator|.
name|setWriteId
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
operator|(
literal|1L
operator|<<
literal|54
operator|)
operator|+
operator|(
literal|1L
operator|<<
literal|44
operator|)
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
operator|(
literal|1L
operator|<<
literal|54
operator|)
operator|+
operator|(
literal|1L
operator|<<
literal|44
operator|)
operator|+
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
operator|(
literal|1L
operator|<<
literal|54
operator|)
operator|+
operator|(
literal|1L
operator|<<
literal|44
operator|)
operator|+
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIllegalNumberOfArgs
parameter_list|()
throws|throws
name|HiveException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|UDFArgumentLengthException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"The function SURROGATE_KEY takes 0 or 2 integer arguments (write id bits, taks id bits), but found 1"
argument_list|)
expr_stmt|;
name|ConstantObjectInspector
name|argument0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableConstantObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|10
argument_list|)
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|argument0
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
name|testWriteIdBitsOutOfRange
parameter_list|()
throws|throws
name|HiveException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|UDFArgumentException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"Write ID bits must be between 1 and 62 (value: 63)"
argument_list|)
expr_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|getArguments
argument_list|(
literal|63
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTaskIdBitsOutOfRange
parameter_list|()
throws|throws
name|HiveException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|UDFArgumentException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"Task ID bits must be between 1 and 62 (value: 0)"
argument_list|)
expr_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|getArguments
argument_list|(
literal|10
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBitSumOutOfRange
parameter_list|()
throws|throws
name|HiveException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|UDFArgumentException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"Write ID bits + Task ID bits must be less than 63 (value: 80)"
argument_list|)
expr_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|getArguments
argument_list|(
literal|40
argument_list|,
literal|40
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNotTezContext
parameter_list|()
throws|throws
name|HiveException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"surrogate_key function is only supported if the execution engine is Tez"
argument_list|)
expr_stmt|;
name|MapredContext
name|mockContext
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MapredContext
operator|.
name|class
argument_list|)
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|emptyArguments
argument_list|)
expr_stmt|;
name|udf
operator|.
name|configure
argument_list|(
name|mockContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoWriteId
parameter_list|()
throws|throws
name|HiveException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|HiveException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"Could not obtain Write ID for the surrogate_key function"
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockProcessorContest
operator|.
name|getTaskIndex
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|emptyArguments
argument_list|)
expr_stmt|;
name|udf
operator|.
name|configure
argument_list|(
name|mockTezContext
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
literal|0
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWriteIdOverLimit
parameter_list|()
throws|throws
name|HiveException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"Write ID is out of range (10 bits) in surrogate_key"
argument_list|)
expr_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|getArguments
argument_list|(
literal|10
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|udf
operator|.
name|setWriteId
argument_list|(
literal|1
operator|<<
literal|10
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTaskIdOverLimit
parameter_list|()
throws|throws
name|HiveException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"Task ID is out of range (10 bits) in surrogate_key"
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockProcessorContest
operator|.
name|getTaskIndex
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|1
operator|<<
literal|10
argument_list|)
expr_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|getArguments
argument_list|(
literal|10
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|udf
operator|.
name|configure
argument_list|(
name|mockTezContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRowIdOverLimit
parameter_list|()
throws|throws
name|HiveException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|HiveException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"Row ID is out of range (1 bits) in surrogate_key"
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockProcessorContest
operator|.
name|getTaskIndex
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|getArguments
argument_list|(
literal|32
argument_list|,
literal|31
argument_list|)
argument_list|)
expr_stmt|;
name|udf
operator|.
name|configure
argument_list|(
name|mockTezContext
argument_list|)
expr_stmt|;
name|udf
operator|.
name|setWriteId
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
operator|(
literal|1L
operator|<<
literal|32
operator|)
operator|+
operator|(
literal|1L
operator|<<
literal|1
operator|)
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
operator|(
literal|1L
operator|<<
literal|32
operator|)
operator|+
operator|(
literal|1L
operator|<<
literal|1
operator|)
operator|+
literal|1
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyConst
argument_list|(
operator|(
literal|1L
operator|<<
literal|32
operator|)
operator|+
operator|(
literal|1L
operator|<<
literal|1
operator|)
operator|+
literal|2
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ObjectInspector
index|[]
name|getArguments
parameter_list|(
name|int
name|writeIdBits
parameter_list|,
name|int
name|taskIdBits
parameter_list|)
block|{
name|ConstantObjectInspector
name|argument0
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableConstantObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
operator|new
name|IntWritable
argument_list|(
name|writeIdBits
argument_list|)
argument_list|)
decl_stmt|;
name|ConstantObjectInspector
name|argument1
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableConstantObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
operator|new
name|IntWritable
argument_list|(
name|taskIdBits
argument_list|)
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|argument0
block|,
name|argument1
block|}
decl_stmt|;
return|return
name|arguments
return|;
block|}
specifier|private
name|void
name|runAndVerifyConst
parameter_list|(
name|long
name|expResult
parameter_list|,
name|GenericUDFSurrogateKey
name|udf
parameter_list|)
throws|throws
name|HiveException
block|{
name|DeferredObject
index|[]
name|args
init|=
block|{}
decl_stmt|;
name|LongWritable
name|output
init|=
operator|(
name|LongWritable
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
literal|"surrogate_key() test "
argument_list|,
name|expResult
argument_list|,
name|output
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|udf
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

