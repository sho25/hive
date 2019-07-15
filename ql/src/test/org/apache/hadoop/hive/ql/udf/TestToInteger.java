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
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * TestToInteger.  */
end_comment

begin_class
specifier|public
class|class
name|TestToInteger
block|{
annotation|@
name|Test
specifier|public
name|void
name|testTextToInteger
parameter_list|()
throws|throws
name|Exception
block|{
name|UDFToInteger
name|ti
init|=
operator|new
name|UDFToInteger
argument_list|()
decl_stmt|;
name|Text
name|t1
init|=
operator|new
name|Text
argument_list|(
literal|"-1"
argument_list|)
decl_stmt|;
name|IntWritable
name|i1
init|=
name|ti
operator|.
name|evaluate
argument_list|(
name|t1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|i1
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|Text
name|t2
init|=
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
decl_stmt|;
name|IntWritable
name|i2
init|=
name|ti
operator|.
name|evaluate
argument_list|(
name|t2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|i2
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|Text
name|t3
init|=
operator|new
name|Text
argument_list|(
literal|"A"
argument_list|)
decl_stmt|;
name|IntWritable
name|i3
init|=
name|ti
operator|.
name|evaluate
argument_list|(
name|t3
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|i3
argument_list|)
expr_stmt|;
name|Text
name|t4
init|=
operator|new
name|Text
argument_list|(
literal|"1.1"
argument_list|)
decl_stmt|;
name|IntWritable
name|i4
init|=
name|ti
operator|.
name|evaluate
argument_list|(
name|t4
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|i4
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

