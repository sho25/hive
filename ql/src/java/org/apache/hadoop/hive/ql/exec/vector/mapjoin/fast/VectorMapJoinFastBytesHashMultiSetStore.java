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
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|MemoryEstimate
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
name|JoinUtil
operator|.
name|JoinResult
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
name|mapjoin
operator|.
name|fast
operator|.
name|VectorMapJoinFastBytesHashKeyRef
operator|.
name|KeyRef
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
name|mapjoin
operator|.
name|hashtable
operator|.
name|VectorMapJoinHashMultiSetResult
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
operator|.
name|Position
import|;
end_import

begin_comment
comment|// import com.google.common.base.Preconditions;
end_comment

begin_comment
comment|/*  * Used by VectorMapJoinFastBytesHashMultiSet to store the key and count for a hash multi-set with  * a bytes key.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinFastBytesHashMultiSetStore
implements|implements
name|MemoryEstimate
block|{
specifier|private
name|WriteBuffers
name|writeBuffers
decl_stmt|;
comment|/**    * A store for a key and set membership count in memory.    *    * The memory is a "infinite" byte array as a WriteBuffers object.    *    * We give the client (e.g. hash multi-set logic) a 64-bit key and count reference to keep that    * has the offset within the "infinite" byte array of the key.  The 64 bits includes about half    * of the upper hash code to help during matching.    *    * We optimize the common case when the key length is short and store that information in the    * 64 bit reference.    *    * Cases:    *    *  1) One element when key and is small (and stored in the reference word):    *    *    Key and Value Reference    *      |    *      | absoluteOffset    *      |    *      --------------------------------------    *                                           |    *                                           v    *&lt;4 bytes's for set membership count&gt;&lt;Key Bytes&gt;    *            COUNT                              KEY    *    * NOTE: MultiSetCount.byteLength = 4    *    *  2) One element, general: shows optional big key length.    *    *   Key and Value Reference    *      |    *      | absoluteOffset    *      |    *      -------------------------------------    *                                          |    *                                          v    *&lt;4 byte's for set membership count&gt; [Big Key Length]&lt;Key Bytes&gt;    *                NEXT (NONE)                optional           KEY    */
specifier|public
name|WriteBuffers
name|getWriteBuffers
parameter_list|()
block|{
return|return
name|writeBuffers
return|;
block|}
comment|/**    * A hash multi-set result that can read the set membership count for the key.    * It also has support routines for checking the hash code and key equality.    *    * It implements the standard map join hash multi-set result interface.    *    */
specifier|public
specifier|static
class|class
name|HashMultiSetResult
extends|extends
name|VectorMapJoinHashMultiSetResult
block|{
specifier|private
name|VectorMapJoinFastBytesHashMultiSetStore
name|multiSetStore
decl_stmt|;
specifier|private
name|int
name|keyLength
decl_stmt|;
specifier|private
name|boolean
name|isSingleCount
decl_stmt|;
specifier|private
name|long
name|refWord
decl_stmt|;
specifier|private
name|long
name|absoluteOffset
decl_stmt|;
specifier|private
name|Position
name|readPos
decl_stmt|;
specifier|public
name|HashMultiSetResult
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|refWord
operator|=
operator|-
literal|1
expr_stmt|;
name|readPos
operator|=
operator|new
name|Position
argument_list|()
expr_stmt|;
block|}
comment|/**      * Setup for reading the key of an entry with the equalKey method.      * @param multiSetStore      * @param refWord      */
specifier|public
name|void
name|setKey
parameter_list|(
name|VectorMapJoinFastBytesHashMultiSetStore
name|multiSetStore
parameter_list|,
name|long
name|refWord
parameter_list|)
block|{
comment|// Preconditions.checkState(!KeyRef.getIsInvalidFlag(refWord));
name|this
operator|.
name|multiSetStore
operator|=
name|multiSetStore
expr_stmt|;
name|this
operator|.
name|refWord
operator|=
name|refWord
expr_stmt|;
name|absoluteOffset
operator|=
name|KeyRef
operator|.
name|getAbsoluteOffset
argument_list|(
name|refWord
argument_list|)
expr_stmt|;
comment|// Position after next relative offset (fixed length) to the key.
name|multiSetStore
operator|.
name|writeBuffers
operator|.
name|setReadPoint
argument_list|(
name|absoluteOffset
argument_list|,
name|readPos
argument_list|)
expr_stmt|;
name|keyLength
operator|=
name|KeyRef
operator|.
name|getSmallKeyLength
argument_list|(
name|refWord
argument_list|)
expr_stmt|;
name|boolean
name|isKeyLengthSmall
init|=
operator|(
name|keyLength
operator|!=
name|KeyRef
operator|.
name|SmallKeyLength
operator|.
name|allBitsOn
operator|)
decl_stmt|;
if|if
condition|(
operator|!
name|isKeyLengthSmall
condition|)
block|{
comment|// And, if current value is big we must read it.
name|keyLength
operator|=
name|multiSetStore
operator|.
name|writeBuffers
operator|.
name|readVInt
argument_list|(
name|readPos
argument_list|)
expr_stmt|;
block|}
comment|// NOTE: Reading is now positioned before the key bytes.
block|}
comment|/**      * Compare a key with the key positioned with the setKey method.      * @param keyBytes      * @param keyStart      * @param keyLength      * @return      */
specifier|public
name|boolean
name|equalKey
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
if|if
condition|(
name|this
operator|.
name|keyLength
operator|!=
name|keyLength
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Our reading was positioned to the key.
if|if
condition|(
operator|!
name|multiSetStore
operator|.
name|writeBuffers
operator|.
name|isEqual
argument_list|(
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|readPos
argument_list|,
name|keyLength
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// NOTE: WriteBuffers.isEqual does not advance the read position...
return|return
literal|true
return|;
block|}
comment|/**      * Mark the key matched with equalKey as a match and read the set membership count,      * if necessary.      */
specifier|public
name|void
name|setContains
parameter_list|()
block|{
name|isSingleCount
operator|=
name|KeyRef
operator|.
name|getIsSingleFlag
argument_list|(
name|refWord
argument_list|)
expr_stmt|;
if|if
condition|(
name|isSingleCount
condition|)
block|{
name|count
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|count
operator|=
name|multiSetStore
operator|.
name|writeBuffers
operator|.
name|readInt
argument_list|(
name|absoluteOffset
operator|-
name|MultiSetCount
operator|.
name|byteLength
argument_list|,
name|readPos
argument_list|)
expr_stmt|;
block|}
name|setJoinResult
argument_list|(
name|JoinResult
operator|.
name|MATCH
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"("
operator|+
name|super
operator|.
name|toString
argument_list|()
operator|+
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"count "
operator|+
name|count
operator|+
literal|")"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|MultiSetCount
block|{
specifier|private
specifier|static
specifier|final
name|int
name|byteLength
init|=
name|Integer
operator|.
name|SIZE
operator|/
name|Byte
operator|.
name|SIZE
decl_stmt|;
comment|// Relative offset zero padding.
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|oneCount
init|=
operator|new
name|byte
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|1
block|}
decl_stmt|;
block|}
comment|/**    * Two 64-bit long result is the key and value reference.    * @param partialHashCode    * @param keyBytes    * @param keyStart    * @param keyLength    */
specifier|public
name|long
name|addFirst
parameter_list|(
name|long
name|partialHashCode
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
comment|// Zero pad out bytes for fixed size next relative offset if more values are added later.
name|writeBuffers
operator|.
name|write
argument_list|(
name|MultiSetCount
operator|.
name|oneCount
argument_list|)
expr_stmt|;
comment|// We require the absolute offset to be non-zero so the 64 key and value reference is non-zero.
comment|// So, we make it the offset after the relative offset and to the key.
specifier|final
name|long
name|absoluteOffset
init|=
name|writeBuffers
operator|.
name|getWritePoint
argument_list|()
decl_stmt|;
comment|// Preconditions.checkState(absoluteOffset> 0);
name|boolean
name|isKeyLengthBig
init|=
operator|(
name|keyLength
operator|>=
name|KeyRef
operator|.
name|SmallKeyLength
operator|.
name|threshold
operator|)
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
comment|/*      * Form 64 bit key and value reference.      */
name|long
name|refWord
init|=
name|partialHashCode
decl_stmt|;
name|refWord
operator||=
name|absoluteOffset
operator|<<
name|KeyRef
operator|.
name|AbsoluteOffset
operator|.
name|bitShift
expr_stmt|;
if|if
condition|(
name|isKeyLengthBig
condition|)
block|{
name|refWord
operator||=
name|KeyRef
operator|.
name|SmallKeyLength
operator|.
name|allBitsOnBitShifted
expr_stmt|;
block|}
else|else
block|{
name|refWord
operator||=
operator|(
operator|(
name|long
operator|)
name|keyLength
operator|)
operator|<<
name|KeyRef
operator|.
name|SmallKeyLength
operator|.
name|bitShift
expr_stmt|;
block|}
name|refWord
operator||=
name|KeyRef
operator|.
name|IsSingleFlag
operator|.
name|flagOnMask
expr_stmt|;
comment|// Preconditions.checkState(!KeyRef.getIsInvalidFlag(refWord));
return|return
name|refWord
return|;
block|}
comment|/**    * @param refWord    */
specifier|public
name|long
name|bumpCount
parameter_list|(
name|long
name|refWord
parameter_list|,
name|WriteBuffers
operator|.
name|Position
name|unsafeReadPos
parameter_list|)
block|{
comment|// Preconditions.checkState(!KeyRef.getIsInvalidFlag(refWord));
comment|/*      * Extract information from the reference word.      */
specifier|final
name|long
name|countAbsoluteOffset
init|=
name|KeyRef
operator|.
name|getAbsoluteOffset
argument_list|(
name|refWord
argument_list|)
operator|-
name|MultiSetCount
operator|.
name|byteLength
decl_stmt|;
specifier|final
name|int
name|currentCount
init|=
name|writeBuffers
operator|.
name|readInt
argument_list|(
name|countAbsoluteOffset
argument_list|,
name|unsafeReadPos
argument_list|)
decl_stmt|;
comment|// Mark reference as having more than 1 as the count.
name|refWord
operator|&=
name|KeyRef
operator|.
name|IsSingleFlag
operator|.
name|flagOffMask
expr_stmt|;
comment|// Save current write position.
specifier|final
name|long
name|saveAbsoluteOffset
init|=
name|writeBuffers
operator|.
name|getWritePoint
argument_list|()
decl_stmt|;
name|writeBuffers
operator|.
name|setWritePoint
argument_list|(
name|countAbsoluteOffset
argument_list|)
expr_stmt|;
name|writeBuffers
operator|.
name|writeInt
argument_list|(
name|countAbsoluteOffset
argument_list|,
name|currentCount
operator|+
literal|1
argument_list|)
expr_stmt|;
comment|// Restore current write position.
name|writeBuffers
operator|.
name|setWritePoint
argument_list|(
name|saveAbsoluteOffset
argument_list|)
expr_stmt|;
return|return
name|refWord
return|;
block|}
specifier|public
name|VectorMapJoinFastBytesHashMultiSetStore
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
name|KeyRef
operator|.
name|AbsoluteOffset
operator|.
name|maxSize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getEstimatedMemorySize
parameter_list|()
block|{
name|long
name|size
init|=
literal|0
decl_stmt|;
name|size
operator|+=
name|writeBuffers
operator|==
literal|null
condition|?
literal|0
else|:
name|writeBuffers
operator|.
name|getEstimatedMemorySize
argument_list|()
expr_stmt|;
return|return
name|size
return|;
block|}
block|}
end_class

end_unit

