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
name|LongWritable
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
name|TestUDFCrc32
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testCrc32Str
parameter_list|()
throws|throws
name|HiveException
block|{
name|UDFCrc32
name|udf
init|=
operator|new
name|UDFCrc32
argument_list|()
decl_stmt|;
name|runAndVerifyStr
argument_list|(
literal|"ABC"
argument_list|,
literal|2743272264L
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|""
argument_list|,
literal|0L
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// repeat again
name|runAndVerifyStr
argument_list|(
literal|"ABC"
argument_list|,
literal|2743272264L
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runAndVerifyStr
argument_list|(
literal|""
argument_list|,
literal|0L
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
specifier|public
name|void
name|testCrc32Bin
parameter_list|()
throws|throws
name|HiveException
block|{
name|UDFCrc32
name|udf
init|=
operator|new
name|UDFCrc32
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
literal|2743272264L
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
literal|0L
argument_list|,
name|udf
argument_list|)
expr_stmt|;
comment|// repeat again
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
literal|2743272264L
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
literal|0L
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
name|Long
name|expResult
parameter_list|,
name|UDFCrc32
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
name|t
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
literal|"crc32() test "
argument_list|,
name|expResult
operator|.
name|longValue
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
name|runAndVerifyBin
parameter_list|(
name|byte
index|[]
name|binV
parameter_list|,
name|Long
name|expResult
parameter_list|,
name|UDFCrc32
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
name|binWr
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
literal|"crc32() test "
argument_list|,
name|expResult
operator|.
name|longValue
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

