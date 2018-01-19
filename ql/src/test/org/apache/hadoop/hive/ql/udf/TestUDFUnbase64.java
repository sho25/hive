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

begin_class
specifier|public
class|class
name|TestUDFUnbase64
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testUnbase64Conversion
parameter_list|()
block|{
name|Text
name|base64
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
comment|// Let's make sure we only read the relevant part of the writable in case of reuse
name|base64
operator|.
name|set
argument_list|(
literal|"Garbage 64. Should be ignored."
argument_list|)
expr_stmt|;
name|base64
operator|.
name|set
argument_list|(
literal|"c3RyaW5n"
argument_list|)
expr_stmt|;
name|BytesWritable
name|expected
init|=
operator|new
name|BytesWritable
argument_list|(
literal|"string"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|UDFUnbase64
name|udf
init|=
operator|new
name|UDFUnbase64
argument_list|()
decl_stmt|;
name|BytesWritable
name|output
init|=
name|udf
operator|.
name|evaluate
argument_list|(
name|base64
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

