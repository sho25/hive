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

begin_class
specifier|public
class|class
name|StringTrim
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
specifier|public
name|StringTrim
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
name|StringTrim
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * TRIM element i of the vector, eliminating blanks from the left    * and right sides of the string, and place the result in outV.    * Operate on the data in place, and set the output by reference    * to improve performance. Ignore null handling. That will be handled separately.    */
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
name|i
parameter_list|)
block|{
name|int
name|l
init|=
name|start
index|[
name|i
index|]
decl_stmt|;
name|int
name|r
init|=
name|start
index|[
name|i
index|]
operator|+
name|length
index|[
name|i
index|]
operator|-
literal|1
decl_stmt|;
comment|// skip blank character on left
while|while
condition|(
name|l
operator|<=
name|r
operator|&&
name|vector
index|[
name|i
index|]
index|[
name|l
index|]
operator|==
literal|0x20
condition|)
block|{
name|l
operator|++
expr_stmt|;
block|}
comment|// skip blank characters on right
while|while
condition|(
name|l
operator|<=
name|r
operator|&&
name|vector
index|[
name|i
index|]
index|[
name|r
index|]
operator|==
literal|0x20
condition|)
block|{
name|r
operator|--
expr_stmt|;
block|}
name|outV
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|vector
index|[
name|i
index|]
argument_list|,
name|l
argument_list|,
operator|(
name|r
operator|-
name|l
operator|)
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

