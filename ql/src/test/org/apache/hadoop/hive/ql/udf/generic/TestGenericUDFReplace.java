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
name|hive
operator|.
name|ql
operator|.
name|udf
operator|.
name|UDFReplace
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
name|TestGenericUDFReplace
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testReplace
parameter_list|()
throws|throws
name|HiveException
block|{
name|UDFReplace
name|udf
init|=
operator|new
name|UDFReplace
argument_list|()
decl_stmt|;
comment|// One of the params is null, then expected is null.
name|verify
argument_list|(
name|udf
argument_list|,
literal|null
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|udf
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
literal|null
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|udf
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Empty string
name|verify
argument_list|(
name|udf
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
literal|""
argument_list|)
expr_stmt|;
comment|// No match
name|verify
argument_list|(
name|udf
argument_list|,
operator|new
name|Text
argument_list|(
literal|"ABCDEF"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"X"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"Z"
argument_list|)
argument_list|,
literal|"ABCDEF"
argument_list|)
expr_stmt|;
comment|// Case-sensitive string found
name|verify
argument_list|(
name|udf
argument_list|,
operator|new
name|Text
argument_list|(
literal|"Hack and Hue"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"H"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"BL"
argument_list|)
argument_list|,
literal|"BLack and BLue"
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|udf
argument_list|,
operator|new
name|Text
argument_list|(
literal|"ABABrdvABrk"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"AB"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"a"
argument_list|)
argument_list|,
literal|"aardvark"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verify
parameter_list|(
name|UDFReplace
name|udf
parameter_list|,
name|Text
name|str
parameter_list|,
name|Text
name|search
parameter_list|,
name|Text
name|replacement
parameter_list|,
name|String
name|expResult
parameter_list|)
throws|throws
name|HiveException
block|{
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
name|str
argument_list|,
name|search
argument_list|,
name|replacement
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
literal|"replace() test "
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

