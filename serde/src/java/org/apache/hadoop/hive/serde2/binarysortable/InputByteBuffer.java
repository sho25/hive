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
name|serde2
operator|.
name|binarysortable
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|EOFException
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

begin_comment
comment|/**  * This class is much more efficient than ByteArrayInputStream because none of  * the methods are synchronized.  */
end_comment

begin_class
specifier|public
class|class
name|InputByteBuffer
block|{
name|byte
index|[]
name|data
decl_stmt|;
name|int
name|start
decl_stmt|;
name|int
name|end
decl_stmt|;
comment|/**    * Reset the byte buffer to the given byte range.    */
specifier|public
name|void
name|reset
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
block|{
name|this
operator|.
name|data
operator|=
name|data
expr_stmt|;
name|this
operator|.
name|start
operator|=
name|start
expr_stmt|;
name|this
operator|.
name|end
operator|=
name|end
expr_stmt|;
block|}
specifier|public
specifier|final
name|byte
name|read
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|read
argument_list|(
literal|false
argument_list|)
return|;
block|}
comment|/**    * Read one byte from the byte buffer. Final method to help inlining.    *     * @param invert    *          whether we want to invert all the bits.    */
specifier|public
specifier|final
name|byte
name|read
parameter_list|(
name|boolean
name|invert
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|start
operator|>=
name|end
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|()
throw|;
block|}
if|if
condition|(
name|invert
condition|)
block|{
return|return
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|^
name|data
index|[
name|start
operator|++
index|]
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|data
index|[
name|start
operator|++
index|]
return|;
block|}
block|}
comment|/**    * Return the current position. Final method to help inlining.    */
specifier|public
specifier|final
name|int
name|tell
parameter_list|()
block|{
return|return
name|start
return|;
block|}
comment|/**    * Set the current position. Final method to help inlining.    */
specifier|public
specifier|final
name|void
name|seek
parameter_list|(
name|int
name|position
parameter_list|)
block|{
name|start
operator|=
name|position
expr_stmt|;
block|}
specifier|public
specifier|final
name|int
name|getEnd
parameter_list|()
block|{
return|return
name|end
return|;
block|}
specifier|public
specifier|final
name|boolean
name|isEof
parameter_list|()
block|{
return|return
operator|(
name|start
operator|>=
name|end
operator|)
return|;
block|}
comment|/**    * Returns the underlying byte array.    */
specifier|public
specifier|final
name|byte
index|[]
name|getData
parameter_list|()
block|{
return|return
name|data
return|;
block|}
comment|/**    * Return the bytes in hex format.    */
specifier|public
name|String
name|dumpHex
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<
name|end
condition|;
name|i
operator|++
control|)
block|{
name|byte
name|b
init|=
name|data
index|[
name|i
index|]
decl_stmt|;
name|int
name|v
init|=
operator|(
name|b
operator|<
literal|0
condition|?
literal|256
operator|+
name|b
else|:
name|b
operator|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"x%02x"
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

