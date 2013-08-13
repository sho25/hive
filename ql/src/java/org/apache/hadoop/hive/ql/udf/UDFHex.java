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
name|Description
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
name|UDF
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

begin_comment
comment|/**  * UDFHex.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"hex"
argument_list|,
name|value
operator|=
literal|"_FUNC_(n, bin, or str) - Convert the argument to hexadecimal "
argument_list|,
name|extended
operator|=
literal|"If the argument is a string, returns two hex digits for each "
operator|+
literal|"character in the string.\n"
operator|+
literal|"If the argument is a number or binary, returns the hexadecimal representation.\n"
operator|+
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_(17) FROM src LIMIT 1;\n"
operator|+
literal|"  'H1'\n"
operator|+
literal|"> SELECT _FUNC_('Facebook') FROM src LIMIT 1;\n"
operator|+
literal|"  '46616365626F6F6B'"
argument_list|)
specifier|public
class|class
name|UDFHex
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|Text
name|result
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|private
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
literal|16
index|]
decl_stmt|;
comment|/**    * Convert num to hex.    *    */
specifier|private
name|Text
name|evaluate
parameter_list|(
name|long
name|num
parameter_list|)
block|{
comment|// Extract the hex digits of num into value[] from right to left
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
name|value
index|[
name|value
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
name|result
operator|.
name|set
argument_list|(
name|value
argument_list|,
name|value
operator|.
name|length
operator|-
name|len
argument_list|,
name|len
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|Text
name|evaluate
parameter_list|(
name|LongWritable
name|n
parameter_list|)
block|{
if|if
condition|(
name|n
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|evaluate
argument_list|(
name|n
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|Text
name|evaluate
parameter_list|(
name|IntWritable
name|n
parameter_list|)
block|{
if|if
condition|(
name|n
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|evaluate
argument_list|(
name|n
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Convert every character in s to two hex digits.    *    */
specifier|public
name|Text
name|evaluate
parameter_list|(
name|Text
name|s
parameter_list|)
block|{
if|if
condition|(
name|s
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|byte
index|[]
name|str
init|=
name|s
operator|.
name|getBytes
argument_list|()
decl_stmt|;
return|return
name|evaluate
argument_list|(
name|str
argument_list|,
name|s
operator|.
name|getLength
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Convert bytes to a hex string    */
specifier|public
name|Text
name|evaluate
parameter_list|(
name|BytesWritable
name|b
parameter_list|)
block|{
if|if
condition|(
name|b
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|byte
index|[]
name|bytes
init|=
name|b
operator|.
name|getBytes
argument_list|()
decl_stmt|;
return|return
name|evaluate
argument_list|(
name|bytes
argument_list|,
name|b
operator|.
name|getLength
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|Text
name|evaluate
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|value
operator|.
name|length
operator|<
name|length
operator|*
literal|2
condition|)
block|{
name|value
operator|=
operator|new
name|byte
index|[
name|length
operator|*
literal|2
index|]
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
name|value
index|[
name|i
operator|*
literal|2
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
operator|(
name|bytes
index|[
name|i
index|]
operator|&
literal|0xF0
operator|)
operator|>>>
literal|4
argument_list|,
literal|16
argument_list|)
argument_list|)
expr_stmt|;
name|value
index|[
name|i
operator|*
literal|2
operator|+
literal|1
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
name|bytes
index|[
name|i
index|]
operator|&
literal|0x0F
argument_list|,
literal|16
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|set
argument_list|(
name|value
argument_list|,
literal|0
argument_list|,
name|length
operator|*
literal|2
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

