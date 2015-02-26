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
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|ql
operator|.
name|ErrorMsg
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
name|RunLengthIntegerWriterV2
operator|.
name|EncodingType
import|;
end_import

begin_comment
comment|/**  * A reader that reads a sequence of light weight compressed integers. Refer  * {@link RunLengthIntegerWriterV2} for description of various lightweight  * compression techniques.  */
end_comment

begin_class
specifier|public
class|class
name|RunLengthIntegerReaderV2
implements|implements
name|IntegerReader
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RunLengthIntegerReaderV2
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|InStream
name|input
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|signed
decl_stmt|;
specifier|private
specifier|final
name|long
index|[]
name|literals
init|=
operator|new
name|long
index|[
name|RunLengthIntegerWriterV2
operator|.
name|MAX_SCOPE
index|]
decl_stmt|;
specifier|private
name|boolean
name|isRepeating
init|=
literal|false
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
specifier|final
name|boolean
name|skipCorrupt
decl_stmt|;
specifier|private
specifier|final
name|SerializationUtils
name|utils
decl_stmt|;
specifier|private
name|EncodingType
name|currentEncoding
decl_stmt|;
specifier|public
name|RunLengthIntegerReaderV2
parameter_list|(
name|InStream
name|input
parameter_list|,
name|boolean
name|signed
parameter_list|,
name|boolean
name|skipCorrupt
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
name|this
operator|.
name|signed
operator|=
name|signed
expr_stmt|;
name|this
operator|.
name|skipCorrupt
operator|=
name|skipCorrupt
expr_stmt|;
name|this
operator|.
name|utils
operator|=
operator|new
name|SerializationUtils
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|final
specifier|static
name|EncodingType
index|[]
name|encodings
init|=
name|EncodingType
operator|.
name|values
argument_list|()
decl_stmt|;
specifier|private
name|void
name|readValues
parameter_list|(
name|boolean
name|ignoreEof
parameter_list|)
throws|throws
name|IOException
block|{
comment|// read the first 2 bits and determine the encoding type
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|int
name|firstByte
init|=
name|input
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|firstByte
operator|<
literal|0
condition|)
block|{
if|if
condition|(
operator|!
name|ignoreEof
condition|)
block|{
throw|throw
operator|new
name|EOFException
argument_list|(
literal|"Read past end of RLE integer from "
operator|+
name|input
argument_list|)
throw|;
block|}
name|used
operator|=
name|numLiterals
operator|=
literal|0
expr_stmt|;
return|return;
block|}
name|currentEncoding
operator|=
name|encodings
index|[
operator|(
name|firstByte
operator|>>>
literal|6
operator|)
operator|&
literal|0x03
index|]
expr_stmt|;
switch|switch
condition|(
name|currentEncoding
condition|)
block|{
case|case
name|SHORT_REPEAT
case|:
name|readShortRepeatValues
argument_list|(
name|firstByte
argument_list|)
expr_stmt|;
break|break;
case|case
name|DIRECT
case|:
name|readDirectValues
argument_list|(
name|firstByte
argument_list|)
expr_stmt|;
break|break;
case|case
name|PATCHED_BASE
case|:
name|readPatchedBaseValues
argument_list|(
name|firstByte
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELTA
case|:
name|readDeltaValues
argument_list|(
name|firstByte
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unknown encoding "
operator|+
name|currentEncoding
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|readDeltaValues
parameter_list|(
name|int
name|firstByte
parameter_list|)
throws|throws
name|IOException
block|{
comment|// extract the number of fixed bits
name|int
name|fb
init|=
operator|(
name|firstByte
operator|>>>
literal|1
operator|)
operator|&
literal|0x1f
decl_stmt|;
if|if
condition|(
name|fb
operator|!=
literal|0
condition|)
block|{
name|fb
operator|=
name|utils
operator|.
name|decodeBitWidth
argument_list|(
name|fb
argument_list|)
expr_stmt|;
block|}
comment|// extract the blob run length
name|int
name|len
init|=
operator|(
name|firstByte
operator|&
literal|0x01
operator|)
operator|<<
literal|8
decl_stmt|;
name|len
operator||=
name|input
operator|.
name|read
argument_list|()
expr_stmt|;
comment|// read the first value stored as vint
name|long
name|firstVal
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|signed
condition|)
block|{
name|firstVal
operator|=
name|utils
operator|.
name|readVslong
argument_list|(
name|input
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|firstVal
operator|=
name|utils
operator|.
name|readVulong
argument_list|(
name|input
argument_list|)
expr_stmt|;
block|}
comment|// store first value to result buffer
name|long
name|prevVal
init|=
name|firstVal
decl_stmt|;
name|literals
index|[
name|numLiterals
operator|++
index|]
operator|=
name|firstVal
expr_stmt|;
comment|// if fixed bits is 0 then all values have fixed delta
if|if
condition|(
name|fb
operator|==
literal|0
condition|)
block|{
comment|// read the fixed delta value stored as vint (deltas can be negative even
comment|// if all number are positive)
name|long
name|fd
init|=
name|utils
operator|.
name|readVslong
argument_list|(
name|input
argument_list|)
decl_stmt|;
if|if
condition|(
name|fd
operator|==
literal|0
condition|)
block|{
name|isRepeating
operator|=
literal|true
expr_stmt|;
assert|assert
name|numLiterals
operator|==
literal|1
assert|;
name|Arrays
operator|.
name|fill
argument_list|(
name|literals
argument_list|,
name|numLiterals
argument_list|,
name|numLiterals
operator|+
name|len
argument_list|,
name|literals
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|numLiterals
operator|+=
name|len
expr_stmt|;
block|}
else|else
block|{
comment|// add fixed deltas to adjacent values
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|len
condition|;
name|i
operator|++
control|)
block|{
name|literals
index|[
name|numLiterals
operator|++
index|]
operator|=
name|literals
index|[
name|numLiterals
operator|-
literal|2
index|]
operator|+
name|fd
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|long
name|deltaBase
init|=
name|utils
operator|.
name|readVslong
argument_list|(
name|input
argument_list|)
decl_stmt|;
comment|// add delta base and first value
name|literals
index|[
name|numLiterals
operator|++
index|]
operator|=
name|firstVal
operator|+
name|deltaBase
expr_stmt|;
name|prevVal
operator|=
name|literals
index|[
name|numLiterals
operator|-
literal|1
index|]
expr_stmt|;
name|len
operator|-=
literal|1
expr_stmt|;
comment|// write the unpacked values, add it to previous value and store final
comment|// value to result buffer. if the delta base value is negative then it
comment|// is a decreasing sequence else an increasing sequence
name|utils
operator|.
name|readInts
argument_list|(
name|literals
argument_list|,
name|numLiterals
argument_list|,
name|len
argument_list|,
name|fb
argument_list|,
name|input
argument_list|)
expr_stmt|;
while|while
condition|(
name|len
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|deltaBase
operator|<
literal|0
condition|)
block|{
name|literals
index|[
name|numLiterals
index|]
operator|=
name|prevVal
operator|-
name|literals
index|[
name|numLiterals
index|]
expr_stmt|;
block|}
else|else
block|{
name|literals
index|[
name|numLiterals
index|]
operator|=
name|prevVal
operator|+
name|literals
index|[
name|numLiterals
index|]
expr_stmt|;
block|}
name|prevVal
operator|=
name|literals
index|[
name|numLiterals
index|]
expr_stmt|;
name|len
operator|--
expr_stmt|;
name|numLiterals
operator|++
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|readPatchedBaseValues
parameter_list|(
name|int
name|firstByte
parameter_list|)
throws|throws
name|IOException
block|{
comment|// extract the number of fixed bits
name|int
name|fbo
init|=
operator|(
name|firstByte
operator|>>>
literal|1
operator|)
operator|&
literal|0x1f
decl_stmt|;
name|int
name|fb
init|=
name|utils
operator|.
name|decodeBitWidth
argument_list|(
name|fbo
argument_list|)
decl_stmt|;
comment|// extract the run length of data blob
name|int
name|len
init|=
operator|(
name|firstByte
operator|&
literal|0x01
operator|)
operator|<<
literal|8
decl_stmt|;
name|len
operator||=
name|input
operator|.
name|read
argument_list|()
expr_stmt|;
comment|// runs are always one off
name|len
operator|+=
literal|1
expr_stmt|;
comment|// extract the number of bytes occupied by base
name|int
name|thirdByte
init|=
name|input
operator|.
name|read
argument_list|()
decl_stmt|;
name|int
name|bw
init|=
operator|(
name|thirdByte
operator|>>>
literal|5
operator|)
operator|&
literal|0x07
decl_stmt|;
comment|// base width is one off
name|bw
operator|+=
literal|1
expr_stmt|;
comment|// extract patch width
name|int
name|pwo
init|=
name|thirdByte
operator|&
literal|0x1f
decl_stmt|;
name|int
name|pw
init|=
name|utils
operator|.
name|decodeBitWidth
argument_list|(
name|pwo
argument_list|)
decl_stmt|;
comment|// read fourth byte and extract patch gap width
name|int
name|fourthByte
init|=
name|input
operator|.
name|read
argument_list|()
decl_stmt|;
name|int
name|pgw
init|=
operator|(
name|fourthByte
operator|>>>
literal|5
operator|)
operator|&
literal|0x07
decl_stmt|;
comment|// patch gap width is one off
name|pgw
operator|+=
literal|1
expr_stmt|;
comment|// extract the length of the patch list
name|int
name|pl
init|=
name|fourthByte
operator|&
literal|0x1f
decl_stmt|;
comment|// read the next base width number of bytes to extract base value
name|long
name|base
init|=
name|utils
operator|.
name|bytesToLongBE
argument_list|(
name|input
argument_list|,
name|bw
argument_list|)
decl_stmt|;
name|long
name|mask
init|=
operator|(
literal|1L
operator|<<
operator|(
operator|(
name|bw
operator|*
literal|8
operator|)
operator|-
literal|1
operator|)
operator|)
decl_stmt|;
comment|// if MSB of base value is 1 then base is negative value else positive
if|if
condition|(
operator|(
name|base
operator|&
name|mask
operator|)
operator|!=
literal|0
condition|)
block|{
name|base
operator|=
name|base
operator|&
operator|~
name|mask
expr_stmt|;
name|base
operator|=
operator|-
name|base
expr_stmt|;
block|}
comment|// unpack the data blob
name|long
index|[]
name|unpacked
init|=
operator|new
name|long
index|[
name|len
index|]
decl_stmt|;
name|utils
operator|.
name|readInts
argument_list|(
name|unpacked
argument_list|,
literal|0
argument_list|,
name|len
argument_list|,
name|fb
argument_list|,
name|input
argument_list|)
expr_stmt|;
comment|// unpack the patch blob
name|long
index|[]
name|unpackedPatch
init|=
operator|new
name|long
index|[
name|pl
index|]
decl_stmt|;
if|if
condition|(
operator|(
name|pw
operator|+
name|pgw
operator|)
operator|>
literal|64
operator|&&
operator|!
name|skipCorrupt
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ErrorMsg
operator|.
name|ORC_CORRUPTED_READ
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
name|int
name|bitSize
init|=
name|utils
operator|.
name|getClosestFixedBits
argument_list|(
name|pw
operator|+
name|pgw
argument_list|)
decl_stmt|;
name|utils
operator|.
name|readInts
argument_list|(
name|unpackedPatch
argument_list|,
literal|0
argument_list|,
name|pl
argument_list|,
name|bitSize
argument_list|,
name|input
argument_list|)
expr_stmt|;
comment|// apply the patch directly when decoding the packed data
name|int
name|patchIdx
init|=
literal|0
decl_stmt|;
name|long
name|currGap
init|=
literal|0
decl_stmt|;
name|long
name|currPatch
init|=
literal|0
decl_stmt|;
name|long
name|patchMask
init|=
operator|(
operator|(
literal|1L
operator|<<
name|pw
operator|)
operator|-
literal|1
operator|)
decl_stmt|;
name|currGap
operator|=
name|unpackedPatch
index|[
name|patchIdx
index|]
operator|>>>
name|pw
expr_stmt|;
name|currPatch
operator|=
name|unpackedPatch
index|[
name|patchIdx
index|]
operator|&
name|patchMask
expr_stmt|;
name|long
name|actualGap
init|=
literal|0
decl_stmt|;
comment|// special case: gap is>255 then patch value will be 0.
comment|// if gap is<=255 then patch value cannot be 0
while|while
condition|(
name|currGap
operator|==
literal|255
operator|&&
name|currPatch
operator|==
literal|0
condition|)
block|{
name|actualGap
operator|+=
literal|255
expr_stmt|;
name|patchIdx
operator|++
expr_stmt|;
name|currGap
operator|=
name|unpackedPatch
index|[
name|patchIdx
index|]
operator|>>>
name|pw
expr_stmt|;
name|currPatch
operator|=
name|unpackedPatch
index|[
name|patchIdx
index|]
operator|&
name|patchMask
expr_stmt|;
block|}
comment|// add the left over gap
name|actualGap
operator|+=
name|currGap
expr_stmt|;
comment|// unpack data blob, patch it (if required), add base to get final result
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|unpacked
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|actualGap
condition|)
block|{
comment|// extract the patch value
name|long
name|patchedVal
init|=
name|unpacked
index|[
name|i
index|]
operator||
operator|(
name|currPatch
operator|<<
name|fb
operator|)
decl_stmt|;
comment|// add base to patched value
name|literals
index|[
name|numLiterals
operator|++
index|]
operator|=
name|base
operator|+
name|patchedVal
expr_stmt|;
comment|// increment the patch to point to next entry in patch list
name|patchIdx
operator|++
expr_stmt|;
if|if
condition|(
name|patchIdx
operator|<
name|pl
condition|)
block|{
comment|// read the next gap and patch
name|currGap
operator|=
name|unpackedPatch
index|[
name|patchIdx
index|]
operator|>>>
name|pw
expr_stmt|;
name|currPatch
operator|=
name|unpackedPatch
index|[
name|patchIdx
index|]
operator|&
name|patchMask
expr_stmt|;
name|actualGap
operator|=
literal|0
expr_stmt|;
comment|// special case: gap is>255 then patch will be 0. if gap is
comment|//<=255 then patch cannot be 0
while|while
condition|(
name|currGap
operator|==
literal|255
operator|&&
name|currPatch
operator|==
literal|0
condition|)
block|{
name|actualGap
operator|+=
literal|255
expr_stmt|;
name|patchIdx
operator|++
expr_stmt|;
name|currGap
operator|=
name|unpackedPatch
index|[
name|patchIdx
index|]
operator|>>>
name|pw
expr_stmt|;
name|currPatch
operator|=
name|unpackedPatch
index|[
name|patchIdx
index|]
operator|&
name|patchMask
expr_stmt|;
block|}
comment|// add the left over gap
name|actualGap
operator|+=
name|currGap
expr_stmt|;
comment|// next gap is relative to the current gap
name|actualGap
operator|+=
name|i
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// no patching required. add base to unpacked value to get final value
name|literals
index|[
name|numLiterals
operator|++
index|]
operator|=
name|base
operator|+
name|unpacked
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|readDirectValues
parameter_list|(
name|int
name|firstByte
parameter_list|)
throws|throws
name|IOException
block|{
comment|// extract the number of fixed bits
name|int
name|fbo
init|=
operator|(
name|firstByte
operator|>>>
literal|1
operator|)
operator|&
literal|0x1f
decl_stmt|;
name|int
name|fb
init|=
name|utils
operator|.
name|decodeBitWidth
argument_list|(
name|fbo
argument_list|)
decl_stmt|;
comment|// extract the run length
name|int
name|len
init|=
operator|(
name|firstByte
operator|&
literal|0x01
operator|)
operator|<<
literal|8
decl_stmt|;
name|len
operator||=
name|input
operator|.
name|read
argument_list|()
expr_stmt|;
comment|// runs are one off
name|len
operator|+=
literal|1
expr_stmt|;
comment|// write the unpacked values and zigzag decode to result buffer
name|utils
operator|.
name|readInts
argument_list|(
name|literals
argument_list|,
name|numLiterals
argument_list|,
name|len
argument_list|,
name|fb
argument_list|,
name|input
argument_list|)
expr_stmt|;
if|if
condition|(
name|signed
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|len
condition|;
name|i
operator|++
control|)
block|{
name|literals
index|[
name|numLiterals
index|]
operator|=
name|utils
operator|.
name|zigzagDecode
argument_list|(
name|literals
index|[
name|numLiterals
index|]
argument_list|)
expr_stmt|;
name|numLiterals
operator|++
expr_stmt|;
block|}
block|}
else|else
block|{
name|numLiterals
operator|+=
name|len
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|readShortRepeatValues
parameter_list|(
name|int
name|firstByte
parameter_list|)
throws|throws
name|IOException
block|{
comment|// read the number of bytes occupied by the value
name|int
name|size
init|=
operator|(
name|firstByte
operator|>>>
literal|3
operator|)
operator|&
literal|0x07
decl_stmt|;
comment|// #bytes are one off
name|size
operator|+=
literal|1
expr_stmt|;
comment|// read the run length
name|int
name|len
init|=
name|firstByte
operator|&
literal|0x07
decl_stmt|;
comment|// run lengths values are stored only after MIN_REPEAT value is met
name|len
operator|+=
name|RunLengthIntegerWriterV2
operator|.
name|MIN_REPEAT
expr_stmt|;
comment|// read the repeated value which is store using fixed bytes
name|long
name|val
init|=
name|utils
operator|.
name|bytesToLongBE
argument_list|(
name|input
argument_list|,
name|size
argument_list|)
decl_stmt|;
if|if
condition|(
name|signed
condition|)
block|{
name|val
operator|=
name|utils
operator|.
name|zigzagDecode
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|numLiterals
operator|!=
literal|0
condition|)
block|{
comment|// Currently this always holds, which makes peekNextAvailLength simpler.
comment|// If this changes, peekNextAvailLength should be adjusted accordingly.
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"readValues called with existing values present"
argument_list|)
throw|;
block|}
comment|// repeat the value for length times
name|isRepeating
operator|=
literal|true
expr_stmt|;
comment|// TODO: this is not so useful and V1 reader doesn't do that. Fix? Same if delta == 0
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|len
condition|;
name|i
operator|++
control|)
block|{
name|literals
index|[
name|i
index|]
operator|=
name|val
expr_stmt|;
block|}
name|numLiterals
operator|=
name|len
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
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
annotation|@
name|Override
specifier|public
name|long
name|next
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|result
decl_stmt|;
if|if
condition|(
name|used
operator|==
name|numLiterals
condition|)
block|{
name|numLiterals
operator|=
literal|0
expr_stmt|;
name|used
operator|=
literal|0
expr_stmt|;
name|readValues
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|result
operator|=
name|literals
index|[
name|used
operator|++
index|]
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
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
comment|// a loop is required for cases where we break the run into two
comment|// parts
while|while
condition|(
name|consumed
operator|>
literal|0
condition|)
block|{
name|numLiterals
operator|=
literal|0
expr_stmt|;
name|readValues
argument_list|(
literal|false
argument_list|)
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
annotation|@
name|Override
specifier|public
name|void
name|skip
parameter_list|(
name|long
name|numValues
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
name|numValues
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
name|numLiterals
operator|=
literal|0
expr_stmt|;
name|used
operator|=
literal|0
expr_stmt|;
name|readValues
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|long
name|consume
init|=
name|Math
operator|.
name|min
argument_list|(
name|numValues
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
name|numValues
operator|-=
name|consume
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
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
comment|// The default value of null for int type in vectorized
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
operator|||
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
annotation|@
name|Override
specifier|public
name|void
name|setInStream
parameter_list|(
name|InStream
name|data
parameter_list|)
block|{
name|input
operator|=
name|data
expr_stmt|;
block|}
block|}
end_class

end_unit

