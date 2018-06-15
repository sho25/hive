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

begin_class
specifier|public
class|class
name|StringLTrim
extends|extends
name|StringUnaryUDFDirect
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|EMPTY_BYTES
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
specifier|public
name|StringLTrim
parameter_list|(
name|int
name|inputColumn
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|super
argument_list|(
name|inputColumn
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
block|}
specifier|public
name|StringLTrim
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * LTRIM element i of the vector, and place the result in outV.    * Operate on the data in place, and set the output by reference    * to improve performance. Ignore null handling. That will be handled separately.    */
specifier|protected
name|void
name|func
parameter_list|(
name|BytesColumnVector
name|outV
parameter_list|,
name|byte
index|[]
index|[]
name|vector
parameter_list|,
name|int
index|[]
name|start
parameter_list|,
name|int
index|[]
name|length
parameter_list|,
name|int
name|batchIndex
parameter_list|)
block|{
name|byte
index|[]
name|bytes
init|=
name|vector
index|[
name|batchIndex
index|]
decl_stmt|;
specifier|final
name|int
name|startIndex
init|=
name|start
index|[
name|batchIndex
index|]
decl_stmt|;
comment|// Skip past blank characters.
specifier|final
name|int
name|end
init|=
name|startIndex
operator|+
name|length
index|[
name|batchIndex
index|]
decl_stmt|;
name|int
name|index
init|=
name|startIndex
decl_stmt|;
while|while
condition|(
name|index
operator|<
name|end
operator|&&
name|bytes
index|[
name|index
index|]
operator|==
literal|0x20
condition|)
block|{
name|index
operator|++
expr_stmt|;
block|}
specifier|final
name|int
name|resultLength
init|=
name|end
operator|-
name|index
decl_stmt|;
if|if
condition|(
name|resultLength
operator|==
literal|0
condition|)
block|{
name|outV
operator|.
name|setVal
argument_list|(
name|batchIndex
argument_list|,
name|EMPTY_BYTES
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
return|return;
block|}
name|outV
operator|.
name|setVal
argument_list|(
name|batchIndex
argument_list|,
name|bytes
argument_list|,
name|index
argument_list|,
name|resultLength
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

