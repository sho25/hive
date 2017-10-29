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
name|exec
operator|.
name|vector
operator|.
name|expressions
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
name|vector
operator|.
name|BytesColumnVector
import|;
end_import

begin_comment
comment|// Vectorized implementation of Hex(long) that returns string
end_comment

begin_class
specifier|public
class|class
name|FuncHex
extends|extends
name|FuncLongToString
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
name|FuncHex
parameter_list|(
name|int
name|inputCol
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|super
argument_list|(
name|inputCol
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FuncHex
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|prepareResult
parameter_list|(
name|int
name|i
parameter_list|,
name|long
index|[]
name|vector
parameter_list|,
name|BytesColumnVector
name|outV
parameter_list|)
block|{
name|long
name|num
init|=
name|vector
index|[
name|i
index|]
decl_stmt|;
comment|// Extract the bits of num into bytes[] from right to left
name|int
name|len
init|=
literal|0
decl_stmt|;
do|do
block|{
name|len
operator|++
expr_stmt|;
name|bytes
index|[
name|bytes
operator|.
name|length
operator|-
name|len
index|]
operator|=
operator|(
name|byte
operator|)
name|Character
operator|.
name|toUpperCase
argument_list|(
name|Character
operator|.
name|forDigit
argument_list|(
call|(
name|int
call|)
argument_list|(
name|num
operator|&
literal|0xF
argument_list|)
argument_list|,
literal|16
argument_list|)
argument_list|)
expr_stmt|;
name|num
operator|>>>=
literal|4
expr_stmt|;
block|}
do|while
condition|(
name|num
operator|!=
literal|0
condition|)
do|;
name|outV
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|bytes
argument_list|,
name|bytes
operator|.
name|length
operator|-
name|len
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

