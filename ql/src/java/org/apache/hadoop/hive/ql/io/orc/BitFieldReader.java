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
name|io
operator|.
name|orc
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
name|LongColumnVector
import|;
end_import

begin_class
class|class
name|BitFieldReader
block|{
specifier|private
specifier|final
name|RunLengthByteReader
name|input
decl_stmt|;
specifier|private
specifier|final
name|int
name|bitSize
decl_stmt|;
specifier|private
name|int
name|current
decl_stmt|;
specifier|private
name|int
name|bitsLeft
decl_stmt|;
specifier|private
specifier|final
name|int
name|mask
decl_stmt|;
name|BitFieldReader
parameter_list|(
name|InStream
name|input
parameter_list|,
name|int
name|bitSize
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|input
operator|=
operator|new
name|RunLengthByteReader
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|this
operator|.
name|bitSize
operator|=
name|bitSize
expr_stmt|;
name|mask
operator|=
operator|(
literal|1
operator|<<
name|bitSize
operator|)
operator|-
literal|1
expr_stmt|;
block|}
specifier|private
name|void
name|readByte
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|input
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|current
operator|=
literal|0xff
operator|&
name|input
operator|.
name|next
argument_list|()
expr_stmt|;
name|bitsLeft
operator|=
literal|8
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|EOFException
argument_list|(
literal|"Read past end of bit field from "
operator|+
name|input
argument_list|)
throw|;
block|}
block|}
name|int
name|next
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|result
init|=
literal|0
decl_stmt|;
name|int
name|bitsLeftToRead
init|=
name|bitSize
decl_stmt|;
while|while
condition|(
name|bitsLeftToRead
operator|>
name|bitsLeft
condition|)
block|{
name|result
operator|<<=
name|bitsLeft
expr_stmt|;
name|result
operator||=
name|current
operator|&
operator|(
operator|(
literal|1
operator|<<
name|bitsLeft
operator|)
operator|-
literal|1
operator|)
expr_stmt|;
name|bitsLeftToRead
operator|-=
name|bitsLeft
expr_stmt|;
name|readByte
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|bitsLeftToRead
operator|>
literal|0
condition|)
block|{
name|result
operator|<<=
name|bitsLeftToRead
expr_stmt|;
name|bitsLeft
operator|-=
name|bitsLeftToRead
expr_stmt|;
name|result
operator||=
operator|(
name|current
operator|>>>
name|bitsLeft
operator|)
operator|&
operator|(
operator|(
literal|1
operator|<<
name|bitsLeftToRead
operator|)
operator|-
literal|1
operator|)
expr_stmt|;
block|}
return|return
name|result
operator|&
name|mask
return|;
block|}
name|void
name|nextVector
parameter_list|(
name|LongColumnVector
name|previous
parameter_list|,
name|long
name|previousLen
parameter_list|)
throws|throws
name|IOException
block|{
name|previous
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|previousLen
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|previous
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|previous
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|next
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// The default value of null for int types in vectorized
comment|// processing is 1, so set that if the value is null
name|previous
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|1
expr_stmt|;
block|}
comment|// The default value for nulls in Vectorization for int types is 1
comment|// and given that non null value can also be 1, we need to check for isNull also
comment|// when determining the isRepeating flag.
if|if
condition|(
name|previous
operator|.
name|isRepeating
operator|&&
name|i
operator|>
literal|0
operator|&&
operator|(
operator|(
name|previous
operator|.
name|vector
index|[
name|i
operator|-
literal|1
index|]
operator|!=
name|previous
operator|.
name|vector
index|[
name|i
index|]
operator|)
operator|||
operator|(
name|previous
operator|.
name|isNull
index|[
name|i
operator|-
literal|1
index|]
operator|!=
name|previous
operator|.
name|isNull
index|[
name|i
index|]
operator|)
operator|)
condition|)
block|{
name|previous
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
name|void
name|seek
parameter_list|(
name|PositionProvider
name|index
parameter_list|)
throws|throws
name|IOException
block|{
name|input
operator|.
name|seek
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|int
name|consumed
init|=
operator|(
name|int
operator|)
name|index
operator|.
name|getNext
argument_list|()
decl_stmt|;
if|if
condition|(
name|consumed
operator|>
literal|8
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Seek past end of byte at "
operator|+
name|consumed
operator|+
literal|" in "
operator|+
name|input
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|consumed
operator|!=
literal|0
condition|)
block|{
name|readByte
argument_list|()
expr_stmt|;
name|bitsLeft
operator|=
literal|8
operator|-
name|consumed
expr_stmt|;
block|}
else|else
block|{
name|bitsLeft
operator|=
literal|0
expr_stmt|;
block|}
block|}
name|void
name|skip
parameter_list|(
name|long
name|items
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|totalBits
init|=
name|bitSize
operator|*
name|items
decl_stmt|;
if|if
condition|(
name|bitsLeft
operator|>=
name|totalBits
condition|)
block|{
name|bitsLeft
operator|-=
name|totalBits
expr_stmt|;
block|}
else|else
block|{
name|totalBits
operator|-=
name|bitsLeft
expr_stmt|;
name|input
operator|.
name|skip
argument_list|(
name|totalBits
operator|/
literal|8
argument_list|)
expr_stmt|;
name|current
operator|=
name|input
operator|.
name|next
argument_list|()
expr_stmt|;
name|bitsLeft
operator|=
call|(
name|int
call|)
argument_list|(
literal|8
operator|-
operator|(
name|totalBits
operator|%
literal|8
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

