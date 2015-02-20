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
name|io
operator|.
name|orc
operator|.
name|BitFieldReader
import|;
end_import

begin_class
specifier|public
class|class
name|LlapUtils
block|{
specifier|public
specifier|static
specifier|final
name|int
name|DOUBLE_GROUP_SIZE
init|=
literal|64
decl_stmt|;
comment|// just happens to be equal to bitmask size
comment|/** Helper for readPresentStream. */
specifier|public
specifier|static
class|class
name|PresentStreamReadResult
block|{
specifier|public
name|int
name|availLength
decl_stmt|;
specifier|public
name|boolean
name|isNullsRun
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|isFollowedByOther
init|=
literal|false
decl_stmt|;
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|isFollowedByOther
operator|=
name|isNullsRun
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|/**    * Helper method that reads present stream to find the size of the run of nulls, or a    * count of contiguous of non-null values based on the run length from the main stream.    * @param r Result is returned via this because java is not a real language.    * @param present The present stream.    * @param availLength The run length from the main stream.    * @param rowsLeftToRead Total number of rows that may be read from the main stream.    */
specifier|public
specifier|static
name|void
name|readPresentStream
parameter_list|(
name|PresentStreamReadResult
name|r
parameter_list|,
name|BitFieldReader
name|present
parameter_list|,
name|long
name|rowsLeftToRead
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|presentBitsRead
init|=
literal|0
decl_stmt|;
name|r
operator|.
name|reset
argument_list|()
expr_stmt|;
comment|// We are looking for a run of nulls no longer than rows to rowsLeftToRead (presumes
comment|// they will all fit in the writer), OR a run of non-nulls no longer than availLength.
comment|// If there's a partial byte in present stream, we will read bits from it.
name|boolean
name|doneWithPresent
init|=
literal|false
decl_stmt|;
name|int
name|rowLimit
init|=
name|r
operator|.
name|availLength
decl_stmt|;
while|while
condition|(
operator|!
name|present
operator|.
name|hasFullByte
argument_list|()
operator|&&
operator|!
name|doneWithPresent
operator|&&
operator|(
name|presentBitsRead
operator|==
literal|0
operator|||
operator|(
name|presentBitsRead
operator|<
name|rowLimit
operator|)
operator|)
condition|)
block|{
name|int
name|bit
init|=
name|present
operator|.
name|peekOneBit
argument_list|()
decl_stmt|;
if|if
condition|(
name|presentBitsRead
operator|==
literal|0
condition|)
block|{
name|r
operator|.
name|isNullsRun
operator|=
operator|(
name|bit
operator|==
literal|0
operator|)
expr_stmt|;
name|rowLimit
operator|=
call|(
name|int
call|)
argument_list|(
name|r
operator|.
name|isNullsRun
condition|?
name|rowsLeftToRead
else|:
name|r
operator|.
name|availLength
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|r
operator|.
name|isNullsRun
operator|!=
operator|(
name|bit
operator|==
literal|0
operator|)
condition|)
block|{
name|doneWithPresent
operator|=
name|r
operator|.
name|isFollowedByOther
operator|=
literal|true
expr_stmt|;
break|break;
block|}
name|present
operator|.
name|skipInCurrentByte
argument_list|(
literal|1
argument_list|)
expr_stmt|;
operator|++
name|presentBitsRead
expr_stmt|;
block|}
comment|// Now, if we are not done, read the full bytes.
comment|// TODO: we could ask the underlying byte stream of "present" reader for runs;
comment|//       many bitmasks might have long sequences of 0x00 or 0xff.
while|while
condition|(
operator|!
name|doneWithPresent
operator|&&
operator|(
name|presentBitsRead
operator|==
literal|0
operator|||
operator|(
name|presentBitsRead
operator|<
name|rowLimit
operator|)
operator|)
condition|)
block|{
name|int
name|bits
init|=
name|present
operator|.
name|peekFullByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|presentBitsRead
operator|==
literal|0
condition|)
block|{
name|r
operator|.
name|isNullsRun
operator|=
operator|(
name|bits
operator|&
operator|(
literal|1
operator|<<
literal|7
operator|)
operator|)
operator|==
literal|0
expr_stmt|;
name|rowLimit
operator|=
call|(
name|int
call|)
argument_list|(
name|r
operator|.
name|isNullsRun
condition|?
name|rowsLeftToRead
else|:
name|r
operator|.
name|availLength
argument_list|)
expr_stmt|;
block|}
name|int
name|bitsToTake
init|=
literal|0
decl_stmt|;
if|if
condition|(
operator|(
name|bits
operator|==
literal|0
operator|&&
name|r
operator|.
name|isNullsRun
operator|)
operator|||
operator|(
name|bits
operator|==
literal|0xff
operator|&&
operator|!
name|r
operator|.
name|isNullsRun
operator|)
condition|)
block|{
name|bitsToTake
operator|=
literal|8
expr_stmt|;
block|}
else|else
block|{
name|doneWithPresent
operator|=
name|r
operator|.
name|isFollowedByOther
operator|=
literal|true
expr_stmt|;
comment|// Get the number of leading 0s or 1s in this byte.
name|bitsToTake
operator|=
name|Integer
operator|.
name|numberOfLeadingZeros
argument_list|(
name|r
operator|.
name|isNullsRun
condition|?
name|bits
else|:
operator|(
operator|~
operator|(
name|bits
operator||
operator|~
literal|255
operator|)
operator|)
argument_list|)
operator|-
literal|24
expr_stmt|;
block|}
name|bitsToTake
operator|=
name|Math
operator|.
name|min
argument_list|(
name|bitsToTake
argument_list|,
name|rowLimit
operator|-
name|presentBitsRead
argument_list|)
expr_stmt|;
name|presentBitsRead
operator|+=
name|bitsToTake
expr_stmt|;
name|present
operator|.
name|skipInCurrentByte
argument_list|(
name|bitsToTake
argument_list|)
expr_stmt|;
block|}
comment|// End of the loop reading full bytes.
assert|assert
name|presentBitsRead
operator|<=
name|rowLimit
operator|:
literal|"Read "
operator|+
name|presentBitsRead
operator|+
literal|" bits for "
operator|+
operator|(
name|r
operator|.
name|isNullsRun
condition|?
literal|""
else|:
literal|"non-"
operator|)
operator|+
literal|"null run: "
operator|+
name|rowsLeftToRead
operator|+
literal|", "
operator|+
name|r
operator|.
name|availLength
assert|;
assert|assert
name|presentBitsRead
operator|>
literal|0
assert|;
name|r
operator|.
name|availLength
operator|=
name|presentBitsRead
expr_stmt|;
block|}
block|}
end_class

end_unit

