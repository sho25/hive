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
comment|/**  * TestUDFMd5.  */
end_comment

begin_class
specifier|public
class|class
name|TestUDFMd5
block|{
annotation|@
name|Test
specifier|public
name|void
name|testMD5Str
parameter_list|()
throws|throws
name|HiveException
block|{
name|UDFMd5
name|udf
init|=
operator|new
name|UDFMd5
argument_list|()
decl_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"ABC"
argument_list|,
literal|"902fbdd2b1df0c4f70b4a5d23525e932"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|""
argument_list|,
literal|"d41d8cd98f00b204e9800998ecf8427e"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerifyStr
argument_list|(
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
name|testMD5Bin
parameter_list|()
throws|throws
name|HiveException
block|{
name|UDFMd5
name|udf
init|=
operator|new
name|UDFMd5
argument_list|()
decl_stmt|;
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
literal|"902fbdd2b1df0c4f70b4a5d23525e932"
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
literal|"d41d8cd98f00b204e9800998ecf8427e"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// null
name|runAndVerifyBin
argument_list|(
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
name|runAndVerifyStr
parameter_list|(
name|String
name|str
parameter_list|,
name|String
name|expResult
parameter_list|,
name|UDFMd5
name|udf
parameter_list|)
throws|throws
name|HiveException
block|{
name|Text
name|t
init|=
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
name|t
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"md5() test "
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
name|binV
parameter_list|,
name|String
name|expResult
parameter_list|,
name|UDFMd5
name|udf
parameter_list|)
throws|throws
name|HiveException
block|{
name|BytesWritable
name|binWr
init|=
name|binV
operator|!=
literal|null
condition|?
operator|new
name|BytesWritable
argument_list|(
name|binV
argument_list|)
else|:
literal|null
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
name|binWr
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"md5() test "
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

