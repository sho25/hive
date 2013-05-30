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

begin_comment
comment|/**  * A reader that reads a sequence of bytes. A control byte is read before  * each run with positive values 0 to 127 meaning 3 to 130 repetitions. If the  * byte is -1 to -128, 1 to 128 literal byte values follow.  */
end_comment

begin_class
class|class
name|RunLengthByteReader
block|{
specifier|private
specifier|final
name|InStream
name|input
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|literals
init|=
operator|new
name|byte
index|[
name|RunLengthByteWriter
operator|.
name|MAX_LITERAL_SIZE
index|]
decl_stmt|;
specifier|private
name|int
name|numLiterals
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|used
init|=
literal|0
decl_stmt|;
specifier|private
name|boolean
name|repeat
init|=
literal|false
decl_stmt|;
name|RunLengthByteReader
parameter_list|(
name|InStream
name|input
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|input
operator|=
name|input
expr_stmt|;
block|}
specifier|private
name|void
name|readValues
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|control
init|=
name|input
operator|.
name|read
argument_list|()
decl_stmt|;
name|used
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|control
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|(
literal|"Read past end of buffer RLE byte from "
operator|+
name|input
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|control
operator|<
literal|0x80
condition|)
block|{
name|repeat
operator|=
literal|true
expr_stmt|;
name|numLiterals
operator|=
name|control
operator|+
name|RunLengthByteWriter
operator|.
name|MIN_REPEAT_SIZE
expr_stmt|;
name|int
name|val
init|=
name|input
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|val
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|(
literal|"Reading RLE byte got EOF"
argument_list|)
throw|;
block|}
name|literals
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|val
expr_stmt|;
block|}
else|else
block|{
name|repeat
operator|=
literal|false
expr_stmt|;
name|numLiterals
operator|=
literal|0x100
operator|-
name|control
expr_stmt|;
name|int
name|bytes
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|bytes
operator|<
name|numLiterals
condition|)
block|{
name|int
name|result
init|=
name|input
operator|.
name|read
argument_list|(
name|literals
argument_list|,
name|bytes
argument_list|,
name|numLiterals
operator|-
name|bytes
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|(
literal|"Reading RLE byte literal got EOF"
argument_list|)
throw|;
block|}
name|bytes
operator|+=
name|result
expr_stmt|;
block|}
block|}
block|}
name|boolean
name|hasNext
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|used
operator|!=
name|numLiterals
operator|||
name|input
operator|.
name|available
argument_list|()
operator|>
literal|0
return|;
block|}
name|byte
name|next
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
name|result
decl_stmt|;
if|if
condition|(
name|used
operator|==
name|numLiterals
condition|)
block|{
name|readValues
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|repeat
condition|)
block|{
name|used
operator|+=
literal|1
expr_stmt|;
name|result
operator|=
name|literals
index|[
literal|0
index|]
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
name|literals
index|[
name|used
operator|++
index|]
expr_stmt|;
block|}
return|return
name|result
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
operator|!=
literal|0
condition|)
block|{
comment|// a loop is required for cases where we break the run into two parts
while|while
condition|(
name|consumed
operator|>
literal|0
condition|)
block|{
name|readValues
argument_list|()
expr_stmt|;
name|used
operator|=
name|consumed
expr_stmt|;
name|consumed
operator|-=
name|numLiterals
expr_stmt|;
block|}
block|}
else|else
block|{
name|used
operator|=
literal|0
expr_stmt|;
name|numLiterals
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
while|while
condition|(
name|items
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|used
operator|==
name|numLiterals
condition|)
block|{
name|readValues
argument_list|()
expr_stmt|;
block|}
name|long
name|consume
init|=
name|Math
operator|.
name|min
argument_list|(
name|items
argument_list|,
name|numLiterals
operator|-
name|used
argument_list|)
decl_stmt|;
name|used
operator|+=
name|consume
expr_stmt|;
name|items
operator|-=
name|consume
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

