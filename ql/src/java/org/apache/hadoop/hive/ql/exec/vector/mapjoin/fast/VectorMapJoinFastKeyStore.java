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
name|mapjoin
operator|.
name|fast
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|serde2
operator|.
name|WriteBuffers
import|;
end_import

begin_comment
comment|// Optimized for sequential key lookup.
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinFastKeyStore
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|VectorMapJoinFastKeyStore
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|WriteBuffers
name|writeBuffers
decl_stmt|;
specifier|private
name|WriteBuffers
operator|.
name|ByteSegmentRef
name|byteSegmentRef
decl_stmt|;
specifier|private
name|WriteBuffers
operator|.
name|Position
name|readPos
decl_stmt|;
comment|/**    * A store for arbitrary length keys in memory.    *    * The memory is a "infinite" byte array or WriteBuffers object.    *    * We give the client a 64-bit (long) key reference to keep that has the offset within    * the "infinite" byte array of the key.    *    * We optimize the common case when keys are short and store the key length in the key reference    * word.    *    * If the key is big, the big length will be encoded as an integer at the beginning of the key    * followed by the big key bytes.    */
comment|/**    * Bit-length fields within a 64-bit (long) key reference.    *    * Lowest field: An absolute byte offset the the key in the WriteBuffers.    *    * Next field: For short keys, the length of the key.  Otherwise, a special constant    * indicating a big key whose length is stored with the key.    *    * Last field: an always on bit to insure the key reference non-zero when the offset and    * length are zero.    */
comment|/*    * The absolute offset to the beginning of the key within the WriteBuffers.    */
specifier|private
specifier|final
class|class
name|AbsoluteKeyOffset
block|{
specifier|private
specifier|static
specifier|final
name|int
name|bitLength
init|=
literal|40
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|allBitsOn
init|=
operator|(
operator|(
operator|(
name|long
operator|)
literal|1
operator|)
operator|<<
name|bitLength
operator|)
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|bitMask
init|=
name|allBitsOn
decl_stmt|;
comment|// Make it a power of 2 by backing down (i.e. the -2).
specifier|private
specifier|static
specifier|final
name|long
name|maxSize
init|=
operator|(
operator|(
name|long
operator|)
literal|1
operator|)
operator|<<
operator|(
name|bitLength
operator|-
literal|2
operator|)
decl_stmt|;
block|}
comment|/*    * The small key length.    *    * If the key is big (i.e. length>= allBitsOn), then the key length is stored in the    * WriteBuffers.    */
specifier|private
specifier|final
class|class
name|SmallKeyLength
block|{
specifier|private
specifier|static
specifier|final
name|int
name|bitLength
init|=
literal|20
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|allBitsOn
init|=
operator|(
literal|1
operator|<<
name|bitLength
operator|)
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|threshold
init|=
name|allBitsOn
decl_stmt|;
comment|// Lower this for big key testing.
specifier|private
specifier|static
specifier|final
name|int
name|bitShift
init|=
name|AbsoluteKeyOffset
operator|.
name|bitLength
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|bitMask
init|=
operator|(
operator|(
name|long
operator|)
name|allBitsOn
operator|)
operator|<<
name|bitShift
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|allBitsOnBitShifted
init|=
operator|(
operator|(
name|long
operator|)
name|allBitsOn
operator|)
operator|<<
name|bitShift
decl_stmt|;
block|}
comment|/*    * An always on bit to insure the key reference non-zero.    */
specifier|private
specifier|final
class|class
name|IsNonZeroFlag
block|{
specifier|private
specifier|static
specifier|final
name|int
name|bitShift
init|=
name|SmallKeyLength
operator|.
name|bitShift
operator|+
name|SmallKeyLength
operator|.
name|bitLength
decl_stmt|;
empty_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|flagOnMask
init|=
operator|(
operator|(
name|long
operator|)
literal|1
operator|)
operator|<<
name|bitShift
decl_stmt|;
block|}
specifier|public
name|long
name|add
parameter_list|(
name|byte
index|[]
name|keyBytes
parameter_list|,
name|int
name|keyStart
parameter_list|,
name|int
name|keyLength
parameter_list|)
block|{
name|boolean
name|isKeyLengthBig
init|=
operator|(
name|keyLength
operator|>=
name|SmallKeyLength
operator|.
name|threshold
operator|)
decl_stmt|;
name|long
name|absoluteKeyOffset
init|=
name|writeBuffers
operator|.
name|getWritePoint
argument_list|()
decl_stmt|;
if|if
condition|(
name|isKeyLengthBig
condition|)
block|{
name|writeBuffers
operator|.
name|writeVInt
argument_list|(
name|keyLength
argument_list|)
expr_stmt|;
block|}
name|writeBuffers
operator|.
name|write
argument_list|(
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
name|long
name|keyRefWord
init|=
name|IsNonZeroFlag
operator|.
name|flagOnMask
decl_stmt|;
if|if
condition|(
name|isKeyLengthBig
condition|)
block|{
name|keyRefWord
operator||=
name|SmallKeyLength
operator|.
name|allBitsOnBitShifted
expr_stmt|;
block|}
else|else
block|{
name|keyRefWord
operator||=
operator|(
operator|(
name|long
operator|)
name|keyLength
operator|)
operator|<<
name|SmallKeyLength
operator|.
name|bitShift
expr_stmt|;
block|}
name|keyRefWord
operator||=
name|absoluteKeyOffset
expr_stmt|;
comment|// LOG.info("VectorMapJoinFastKeyStore add keyLength " + keyLength + " absoluteKeyOffset " + absoluteKeyOffset + " keyRefWord " + Long.toHexString(keyRefWord));
return|return
name|keyRefWord
return|;
block|}
specifier|public
name|boolean
name|equalKey
parameter_list|(
name|long
name|keyRefWord
parameter_list|,
name|byte
index|[]
name|keyBytes
parameter_list|,
name|int
name|keyStart
parameter_list|,
name|int
name|keyLength
parameter_list|)
block|{
name|int
name|storedKeyLengthLength
init|=
call|(
name|int
call|)
argument_list|(
operator|(
name|keyRefWord
operator|&
name|SmallKeyLength
operator|.
name|bitMask
operator|)
operator|>>
name|SmallKeyLength
operator|.
name|bitShift
argument_list|)
decl_stmt|;
name|boolean
name|isKeyLengthSmall
init|=
operator|(
name|storedKeyLengthLength
operator|!=
name|SmallKeyLength
operator|.
name|allBitsOn
operator|)
decl_stmt|;
comment|// LOG.info("VectorMapJoinFastKeyStore equalKey keyLength " + keyLength + " isKeyLengthSmall " + isKeyLengthSmall + " storedKeyLengthLength " + storedKeyLengthLength + " keyRefWord " + Long.toHexString(keyRefWord));
if|if
condition|(
name|isKeyLengthSmall
operator|&&
name|storedKeyLengthLength
operator|!=
name|keyLength
condition|)
block|{
return|return
literal|false
return|;
block|}
name|long
name|absoluteKeyOffset
init|=
operator|(
name|keyRefWord
operator|&
name|AbsoluteKeyOffset
operator|.
name|bitMask
operator|)
decl_stmt|;
name|writeBuffers
operator|.
name|setReadPoint
argument_list|(
name|absoluteKeyOffset
argument_list|,
name|readPos
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isKeyLengthSmall
condition|)
block|{
comment|// Read big value length we wrote with the value.
name|storedKeyLengthLength
operator|=
name|writeBuffers
operator|.
name|readVInt
argument_list|(
name|readPos
argument_list|)
expr_stmt|;
if|if
condition|(
name|storedKeyLengthLength
operator|!=
name|keyLength
condition|)
block|{
comment|// LOG.info("VectorMapJoinFastKeyStore equalKey no match big length");
return|return
literal|false
return|;
block|}
block|}
comment|// Our reading is positioned to the key.
name|writeBuffers
operator|.
name|getByteSegmentRefToCurrent
argument_list|(
name|byteSegmentRef
argument_list|,
name|keyLength
argument_list|,
name|readPos
argument_list|)
expr_stmt|;
name|byte
index|[]
name|currentBytes
init|=
name|byteSegmentRef
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|int
name|currentStart
init|=
operator|(
name|int
operator|)
name|byteSegmentRef
operator|.
name|getOffset
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keyLength
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|currentBytes
index|[
name|currentStart
operator|+
name|i
index|]
operator|!=
name|keyBytes
index|[
name|keyStart
operator|+
name|i
index|]
condition|)
block|{
comment|// LOG.info("VectorMapJoinFastKeyStore equalKey no match on bytes");
return|return
literal|false
return|;
block|}
block|}
comment|// LOG.info("VectorMapJoinFastKeyStore equalKey match on bytes");
return|return
literal|true
return|;
block|}
specifier|public
name|VectorMapJoinFastKeyStore
parameter_list|(
name|int
name|writeBuffersSize
parameter_list|)
block|{
name|writeBuffers
operator|=
operator|new
name|WriteBuffers
argument_list|(
name|writeBuffersSize
argument_list|,
name|AbsoluteKeyOffset
operator|.
name|maxSize
argument_list|)
expr_stmt|;
name|byteSegmentRef
operator|=
operator|new
name|WriteBuffers
operator|.
name|ByteSegmentRef
argument_list|()
expr_stmt|;
name|readPos
operator|=
operator|new
name|WriteBuffers
operator|.
name|Position
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorMapJoinFastKeyStore
parameter_list|(
name|WriteBuffers
name|writeBuffers
parameter_list|)
block|{
comment|// TODO: Check if maximum size compatible with AbsoluteKeyOffset.maxSize.
name|this
operator|.
name|writeBuffers
operator|=
name|writeBuffers
expr_stmt|;
name|byteSegmentRef
operator|=
operator|new
name|WriteBuffers
operator|.
name|ByteSegmentRef
argument_list|()
expr_stmt|;
name|readPos
operator|=
operator|new
name|WriteBuffers
operator|.
name|Position
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

