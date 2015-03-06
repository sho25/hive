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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|lang
operator|.
name|builder
operator|.
name|HashCodeBuilder
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
name|fs
operator|.
name|FSDataInputStream
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|hdfs
operator|.
name|DFSClient
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
name|hdfs
operator|.
name|DistributedFileSystem
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
name|common
operator|.
name|DiskRange
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
name|common
operator|.
name|DiskRangeList
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
name|common
operator|.
name|DiskRangeList
operator|.
name|DiskRangeListCreateHelper
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
name|common
operator|.
name|DiskRangeList
operator|.
name|DiskRangeListMutateHelper
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
name|RecordReaderImpl
operator|.
name|BufferChunk
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
name|shims
operator|.
name|ShimLoader
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
name|shims
operator|.
name|HadoopShims
operator|.
name|ByteBufferPoolShim
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
name|shims
operator|.
name|HadoopShims
operator|.
name|ZeroCopyReaderShim
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ComparisonChain
import|;
end_import

begin_comment
comment|/**  * Stateless methods shared between RecordReaderImpl and EncodedReaderImpl.  */
end_comment

begin_class
specifier|public
class|class
name|RecordReaderUtils
block|{
specifier|static
name|boolean
index|[]
name|findPresentStreamsByColumn
parameter_list|(
name|List
argument_list|<
name|OrcProto
operator|.
name|Stream
argument_list|>
name|streamList
parameter_list|,
name|List
argument_list|<
name|OrcProto
operator|.
name|Type
argument_list|>
name|types
parameter_list|)
block|{
name|boolean
index|[]
name|hasNull
init|=
operator|new
name|boolean
index|[
name|types
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|OrcProto
operator|.
name|Stream
name|stream
range|:
name|streamList
control|)
block|{
if|if
condition|(
name|stream
operator|.
name|hasKind
argument_list|()
operator|&&
operator|(
name|stream
operator|.
name|getKind
argument_list|()
operator|==
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|PRESENT
operator|)
condition|)
block|{
name|hasNull
index|[
name|stream
operator|.
name|getColumn
argument_list|()
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|hasNull
return|;
block|}
comment|/**    * Does region A overlap region B? The end points are inclusive on both sides.    * @param leftA A's left point    * @param rightA A's right point    * @param leftB B's left point    * @param rightB B's right point    * @return Does region A overlap region B?    */
specifier|static
name|boolean
name|overlap
parameter_list|(
name|long
name|leftA
parameter_list|,
name|long
name|rightA
parameter_list|,
name|long
name|leftB
parameter_list|,
name|long
name|rightB
parameter_list|)
block|{
if|if
condition|(
name|leftA
operator|<=
name|leftB
condition|)
block|{
return|return
name|rightA
operator|>=
name|leftB
return|;
block|}
return|return
name|rightB
operator|>=
name|leftA
return|;
block|}
specifier|static
name|void
name|addEntireStreamToRanges
parameter_list|(
name|long
name|offset
parameter_list|,
name|long
name|length
parameter_list|,
name|DiskRangeListCreateHelper
name|list
parameter_list|,
name|boolean
name|doMergeBuffers
parameter_list|)
block|{
name|list
operator|.
name|addOrMerge
argument_list|(
name|offset
argument_list|,
name|offset
operator|+
name|length
argument_list|,
name|doMergeBuffers
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|static
name|void
name|addRgFilteredStreamToRanges
parameter_list|(
name|OrcProto
operator|.
name|Stream
name|stream
parameter_list|,
name|boolean
index|[]
name|includedRowGroups
parameter_list|,
name|boolean
name|isCompressed
parameter_list|,
name|OrcProto
operator|.
name|RowIndex
name|index
parameter_list|,
name|OrcProto
operator|.
name|ColumnEncoding
name|encoding
parameter_list|,
name|OrcProto
operator|.
name|Type
name|type
parameter_list|,
name|int
name|compressionSize
parameter_list|,
name|boolean
name|hasNull
parameter_list|,
name|long
name|offset
parameter_list|,
name|long
name|length
parameter_list|,
name|DiskRangeListCreateHelper
name|list
parameter_list|,
name|boolean
name|doMergeBuffers
parameter_list|)
block|{
for|for
control|(
name|int
name|group
init|=
literal|0
init|;
name|group
operator|<
name|includedRowGroups
operator|.
name|length
condition|;
operator|++
name|group
control|)
block|{
if|if
condition|(
operator|!
name|includedRowGroups
index|[
name|group
index|]
condition|)
continue|continue;
name|int
name|posn
init|=
name|getIndexPosition
argument_list|(
name|encoding
operator|.
name|getKind
argument_list|()
argument_list|,
name|type
operator|.
name|getKind
argument_list|()
argument_list|,
name|stream
operator|.
name|getKind
argument_list|()
argument_list|,
name|isCompressed
argument_list|,
name|hasNull
argument_list|)
decl_stmt|;
name|long
name|start
init|=
name|index
operator|.
name|getEntry
argument_list|(
name|group
argument_list|)
operator|.
name|getPositions
argument_list|(
name|posn
argument_list|)
decl_stmt|;
specifier|final
name|long
name|nextGroupOffset
decl_stmt|;
name|boolean
name|isLast
init|=
name|group
operator|==
operator|(
name|includedRowGroups
operator|.
name|length
operator|-
literal|1
operator|)
decl_stmt|;
name|nextGroupOffset
operator|=
name|isLast
condition|?
name|length
else|:
name|index
operator|.
name|getEntry
argument_list|(
name|group
operator|+
literal|1
argument_list|)
operator|.
name|getPositions
argument_list|(
name|posn
argument_list|)
expr_stmt|;
name|start
operator|+=
name|offset
expr_stmt|;
name|long
name|end
init|=
name|offset
operator|+
name|estimateRgEndOffset
argument_list|(
name|isCompressed
argument_list|,
name|isLast
argument_list|,
name|nextGroupOffset
argument_list|,
name|length
argument_list|,
name|compressionSize
argument_list|)
decl_stmt|;
name|list
operator|.
name|addOrMerge
argument_list|(
name|start
argument_list|,
name|end
argument_list|,
name|doMergeBuffers
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|long
name|estimateRgEndOffset
parameter_list|(
name|boolean
name|isCompressed
parameter_list|,
name|boolean
name|isLast
parameter_list|,
name|long
name|nextGroupOffset
parameter_list|,
name|long
name|streamLength
parameter_list|,
name|int
name|bufferSize
parameter_list|)
block|{
comment|// figure out the worst case last location
comment|// if adjacent groups have the same compressed block offset then stretch the slop
comment|// by factor of 2 to safely accommodate the next compression block.
comment|// One for the current compression block and another for the next compression block.
name|long
name|slop
init|=
name|isCompressed
condition|?
literal|2
operator|*
operator|(
name|OutStream
operator|.
name|HEADER_SIZE
operator|+
name|bufferSize
operator|)
else|:
name|WORST_UNCOMPRESSED_SLOP
decl_stmt|;
return|return
name|isLast
condition|?
name|streamLength
else|:
name|Math
operator|.
name|min
argument_list|(
name|streamLength
argument_list|,
name|nextGroupOffset
operator|+
name|slop
argument_list|)
return|;
block|}
specifier|private
specifier|static
specifier|final
name|int
name|BYTE_STREAM_POSITIONS
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|RUN_LENGTH_BYTE_POSITIONS
init|=
name|BYTE_STREAM_POSITIONS
operator|+
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|BITFIELD_POSITIONS
init|=
name|RUN_LENGTH_BYTE_POSITIONS
operator|+
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|RUN_LENGTH_INT_POSITIONS
init|=
name|BYTE_STREAM_POSITIONS
operator|+
literal|1
decl_stmt|;
comment|/**    * Get the offset in the index positions for the column that the given    * stream starts.    * @param columnEncoding the encoding of the column    * @param columnType the type of the column    * @param streamType the kind of the stream    * @param isCompressed is the file compressed    * @param hasNulls does the column have a PRESENT stream?    * @return the number of positions that will be used for that stream    */
specifier|public
specifier|static
name|int
name|getIndexPosition
parameter_list|(
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
name|columnEncoding
parameter_list|,
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
name|columnType
parameter_list|,
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
name|streamType
parameter_list|,
name|boolean
name|isCompressed
parameter_list|,
name|boolean
name|hasNulls
parameter_list|)
block|{
if|if
condition|(
name|streamType
operator|==
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|PRESENT
condition|)
block|{
return|return
literal|0
return|;
block|}
name|int
name|compressionValue
init|=
name|isCompressed
condition|?
literal|1
else|:
literal|0
decl_stmt|;
name|int
name|base
init|=
name|hasNulls
condition|?
operator|(
name|BITFIELD_POSITIONS
operator|+
name|compressionValue
operator|)
else|:
literal|0
decl_stmt|;
switch|switch
condition|(
name|columnType
condition|)
block|{
case|case
name|BOOLEAN
case|:
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
case|case
name|DATE
case|:
case|case
name|STRUCT
case|:
case|case
name|MAP
case|:
case|case
name|LIST
case|:
case|case
name|UNION
case|:
return|return
name|base
return|;
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
case|case
name|STRING
case|:
if|if
condition|(
name|columnEncoding
operator|==
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
operator|.
name|DICTIONARY
operator|||
name|columnEncoding
operator|==
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
operator|.
name|DICTIONARY_V2
condition|)
block|{
return|return
name|base
return|;
block|}
else|else
block|{
if|if
condition|(
name|streamType
operator|==
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|DATA
condition|)
block|{
return|return
name|base
return|;
block|}
else|else
block|{
return|return
name|base
operator|+
name|BYTE_STREAM_POSITIONS
operator|+
name|compressionValue
return|;
block|}
block|}
case|case
name|BINARY
case|:
if|if
condition|(
name|streamType
operator|==
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|DATA
condition|)
block|{
return|return
name|base
return|;
block|}
return|return
name|base
operator|+
name|BYTE_STREAM_POSITIONS
operator|+
name|compressionValue
return|;
case|case
name|DECIMAL
case|:
if|if
condition|(
name|streamType
operator|==
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|DATA
condition|)
block|{
return|return
name|base
return|;
block|}
return|return
name|base
operator|+
name|BYTE_STREAM_POSITIONS
operator|+
name|compressionValue
return|;
case|case
name|TIMESTAMP
case|:
if|if
condition|(
name|streamType
operator|==
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|DATA
condition|)
block|{
return|return
name|base
return|;
block|}
return|return
name|base
operator|+
name|RUN_LENGTH_INT_POSITIONS
operator|+
name|compressionValue
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown type "
operator|+
name|columnType
argument_list|)
throw|;
block|}
block|}
comment|// for uncompressed streams, what is the most overlap with the following set
comment|// of rows (long vint literal group).
specifier|static
specifier|final
name|int
name|WORST_UNCOMPRESSED_SLOP
init|=
literal|2
operator|+
literal|8
operator|*
literal|512
decl_stmt|;
comment|/**    * Is this stream part of a dictionary?    * @return is this part of a dictionary?    */
specifier|static
name|boolean
name|isDictionary
parameter_list|(
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
name|kind
parameter_list|,
name|OrcProto
operator|.
name|ColumnEncoding
name|encoding
parameter_list|)
block|{
assert|assert
name|kind
operator|!=
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|DICTIONARY_COUNT
assert|;
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
name|encodingKind
init|=
name|encoding
operator|.
name|getKind
argument_list|()
decl_stmt|;
return|return
name|kind
operator|==
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|DICTIONARY_DATA
operator|||
operator|(
name|kind
operator|==
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|LENGTH
operator|&&
operator|(
name|encodingKind
operator|==
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
operator|.
name|DICTIONARY
operator|||
name|encodingKind
operator|==
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
operator|.
name|DICTIONARY_V2
operator|)
operator|)
return|;
block|}
comment|/**    * Build a string representation of a list of disk ranges.    * @param ranges ranges to stringify    * @return the resulting string    */
specifier|static
name|String
name|stringifyDiskRanges
parameter_list|(
name|DiskRangeList
name|range
parameter_list|)
block|{
name|StringBuilder
name|buffer
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
expr_stmt|;
name|boolean
name|isFirst
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|range
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|isFirst
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|isFirst
operator|=
literal|false
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|range
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|range
operator|=
name|range
operator|.
name|next
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
return|return
name|buffer
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Read the list of ranges from the file.    * @param file the file to read    * @param base the base of the stripe    * @param ranges the disk ranges within the stripe to read    * @return the bytes read for each disk range, which is the same length as    *    ranges    * @throws IOException    */
specifier|static
name|DiskRangeList
name|readDiskRanges
parameter_list|(
name|FSDataInputStream
name|file
parameter_list|,
name|ZeroCopyReaderShim
name|zcr
parameter_list|,
name|long
name|base
parameter_list|,
name|DiskRangeList
name|range
parameter_list|,
name|boolean
name|doForceDirect
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|range
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|DiskRangeList
name|prev
init|=
name|range
operator|.
name|prev
decl_stmt|;
if|if
condition|(
name|prev
operator|==
literal|null
condition|)
block|{
name|prev
operator|=
operator|new
name|DiskRangeListMutateHelper
argument_list|(
name|range
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
name|range
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|range
operator|.
name|hasData
argument_list|()
condition|)
block|{
name|range
operator|=
name|range
operator|.
name|next
expr_stmt|;
continue|continue;
block|}
name|int
name|len
init|=
call|(
name|int
call|)
argument_list|(
name|range
operator|.
name|end
operator|-
name|range
operator|.
name|offset
argument_list|)
decl_stmt|;
name|long
name|off
init|=
name|range
operator|.
name|offset
decl_stmt|;
name|file
operator|.
name|seek
argument_list|(
name|base
operator|+
name|off
argument_list|)
expr_stmt|;
if|if
condition|(
name|zcr
operator|!=
literal|null
condition|)
block|{
name|boolean
name|hasReplaced
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|len
operator|>
literal|0
condition|)
block|{
name|ByteBuffer
name|partial
init|=
name|zcr
operator|.
name|readBuffer
argument_list|(
name|len
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|BufferChunk
name|bc
init|=
operator|new
name|BufferChunk
argument_list|(
name|partial
argument_list|,
name|off
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|hasReplaced
condition|)
block|{
name|range
operator|.
name|replaceSelfWith
argument_list|(
name|bc
argument_list|)
expr_stmt|;
name|hasReplaced
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|range
operator|.
name|insertAfter
argument_list|(
name|bc
argument_list|)
expr_stmt|;
block|}
name|range
operator|=
name|bc
expr_stmt|;
name|int
name|read
init|=
name|partial
operator|.
name|remaining
argument_list|()
decl_stmt|;
name|len
operator|-=
name|read
expr_stmt|;
name|off
operator|+=
name|read
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|doForceDirect
condition|)
block|{
name|ByteBuffer
name|directBuf
init|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|len
argument_list|)
decl_stmt|;
comment|// TODO: HDFS API is a mess, so handle all kinds of crap.
comment|// Before 2.7, read() also doesn't adjust position correctly, so track it.
name|int
name|pos
init|=
name|directBuf
operator|.
name|position
argument_list|()
decl_stmt|,
name|startPos
init|=
name|pos
decl_stmt|,
name|endPos
init|=
name|pos
operator|+
name|len
decl_stmt|;
try|try
block|{
while|while
condition|(
name|pos
operator|<
name|endPos
condition|)
block|{
name|int
name|count
init|=
name|file
operator|.
name|read
argument_list|(
name|directBuf
argument_list|)
decl_stmt|;
if|if
condition|(
name|count
operator|<
literal|0
condition|)
throw|throw
operator|new
name|EOFException
argument_list|()
throw|;
if|if
condition|(
name|count
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"0-length read: "
operator|+
operator|(
name|endPos
operator|-
name|pos
operator|)
operator|+
literal|"@"
operator|+
operator|(
name|pos
operator|-
name|startPos
operator|)
operator|+
literal|" and "
operator|+
name|pos
argument_list|)
throw|;
block|}
name|pos
operator|+=
name|count
expr_stmt|;
if|if
condition|(
name|pos
operator|>
name|endPos
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Position "
operator|+
name|pos
operator|+
literal|" length "
operator|+
name|len
operator|+
literal|"/"
operator|+
name|endPos
operator|+
literal|" after reading "
operator|+
name|count
argument_list|)
throw|;
block|}
name|directBuf
operator|.
name|position
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|ex
parameter_list|)
block|{
name|RecordReaderImpl
operator|.
name|LOG
operator|.
name|error
argument_list|(
literal|"Stream does not support direct read; we will copy."
argument_list|)
expr_stmt|;
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|file
operator|.
name|readFully
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
name|directBuf
operator|.
name|put
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
name|directBuf
operator|.
name|position
argument_list|(
name|startPos
argument_list|)
expr_stmt|;
name|directBuf
operator|.
name|limit
argument_list|(
name|startPos
operator|+
name|len
argument_list|)
expr_stmt|;
name|range
operator|=
name|range
operator|.
name|replaceSelfWith
argument_list|(
operator|new
name|BufferChunk
argument_list|(
name|directBuf
argument_list|,
name|range
operator|.
name|offset
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|file
operator|.
name|readFully
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
name|range
operator|=
name|range
operator|.
name|replaceSelfWith
argument_list|(
operator|new
name|BufferChunk
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buffer
argument_list|)
argument_list|,
name|range
operator|.
name|offset
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|range
operator|=
name|range
operator|.
name|next
expr_stmt|;
block|}
return|return
name|prev
operator|.
name|next
return|;
block|}
specifier|static
name|List
argument_list|<
name|DiskRange
argument_list|>
name|getStreamBuffers
parameter_list|(
name|DiskRangeList
name|range
parameter_list|,
name|long
name|offset
parameter_list|,
name|long
name|length
parameter_list|)
block|{
comment|// This assumes sorted ranges (as do many other parts of ORC code.
name|ArrayList
argument_list|<
name|DiskRange
argument_list|>
name|buffers
init|=
operator|new
name|ArrayList
argument_list|<
name|DiskRange
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|length
operator|==
literal|0
condition|)
return|return
name|buffers
return|;
name|long
name|streamEnd
init|=
name|offset
operator|+
name|length
decl_stmt|;
name|boolean
name|inRange
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|range
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|inRange
condition|)
block|{
if|if
condition|(
name|range
operator|.
name|end
operator|<=
name|offset
condition|)
block|{
name|range
operator|=
name|range
operator|.
name|next
expr_stmt|;
continue|continue;
comment|// Skip until we are in range.
block|}
name|inRange
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|range
operator|.
name|offset
operator|<
name|offset
condition|)
block|{
comment|// Partial first buffer, add a slice of it.
name|DiskRange
name|partial
init|=
name|range
operator|.
name|slice
argument_list|(
name|offset
argument_list|,
name|Math
operator|.
name|min
argument_list|(
name|streamEnd
argument_list|,
name|range
operator|.
name|end
argument_list|)
argument_list|)
decl_stmt|;
name|partial
operator|.
name|shiftBy
argument_list|(
operator|-
name|offset
argument_list|)
expr_stmt|;
name|buffers
operator|.
name|add
argument_list|(
name|partial
argument_list|)
expr_stmt|;
if|if
condition|(
name|range
operator|.
name|end
operator|>=
name|streamEnd
condition|)
break|break;
comment|// Partial first buffer is also partial last buffer.
name|range
operator|=
name|range
operator|.
name|next
expr_stmt|;
continue|continue;
block|}
block|}
elseif|else
if|if
condition|(
name|range
operator|.
name|offset
operator|>=
name|streamEnd
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|range
operator|.
name|end
operator|>
name|streamEnd
condition|)
block|{
comment|// Partial last buffer (may also be the first buffer), add a slice of it.
name|DiskRange
name|partial
init|=
name|range
operator|.
name|slice
argument_list|(
name|range
operator|.
name|offset
argument_list|,
name|streamEnd
argument_list|)
decl_stmt|;
name|partial
operator|.
name|shiftBy
argument_list|(
operator|-
name|offset
argument_list|)
expr_stmt|;
name|buffers
operator|.
name|add
argument_list|(
name|partial
argument_list|)
expr_stmt|;
break|break;
block|}
comment|// Buffer that belongs entirely to one stream.
comment|// TODO: ideally we would want to reuse the object and remove it from the list, but we cannot
comment|//       because bufferChunks is also used by clearStreams for zcr. Create a useless dup.
name|DiskRange
name|full
init|=
name|range
operator|.
name|slice
argument_list|(
name|range
operator|.
name|offset
argument_list|,
name|range
operator|.
name|end
argument_list|)
decl_stmt|;
name|full
operator|.
name|shiftBy
argument_list|(
operator|-
name|offset
argument_list|)
expr_stmt|;
name|buffers
operator|.
name|add
argument_list|(
name|full
argument_list|)
expr_stmt|;
if|if
condition|(
name|range
operator|.
name|end
operator|==
name|streamEnd
condition|)
break|break;
name|range
operator|=
name|range
operator|.
name|next
expr_stmt|;
block|}
return|return
name|buffers
return|;
block|}
specifier|static
name|ZeroCopyReaderShim
name|createZeroCopyShim
parameter_list|(
name|FSDataInputStream
name|file
parameter_list|,
name|CompressionCodec
name|codec
parameter_list|,
name|ByteBufferAllocatorPool
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|(
name|codec
operator|==
literal|null
operator|||
operator|(
operator|(
name|codec
operator|instanceof
name|DirectDecompressionCodec
operator|)
operator|&&
operator|(
operator|(
name|DirectDecompressionCodec
operator|)
name|codec
operator|)
operator|.
name|isAvailable
argument_list|()
operator|)
operator|)
condition|)
block|{
comment|/* codec is null or is available */
return|return
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getZeroCopyReader
argument_list|(
name|file
argument_list|,
name|pool
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|// this is an implementation copied from ElasticByteBufferPool in hadoop-2,
comment|// which lacks a clear()/clean() operation
specifier|public
specifier|final
specifier|static
class|class
name|ByteBufferAllocatorPool
implements|implements
name|ByteBufferPoolShim
block|{
specifier|private
specifier|static
specifier|final
class|class
name|Key
implements|implements
name|Comparable
argument_list|<
name|Key
argument_list|>
block|{
specifier|private
specifier|final
name|int
name|capacity
decl_stmt|;
specifier|private
specifier|final
name|long
name|insertionGeneration
decl_stmt|;
name|Key
parameter_list|(
name|int
name|capacity
parameter_list|,
name|long
name|insertionGeneration
parameter_list|)
block|{
name|this
operator|.
name|capacity
operator|=
name|capacity
expr_stmt|;
name|this
operator|.
name|insertionGeneration
operator|=
name|insertionGeneration
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Key
name|other
parameter_list|)
block|{
return|return
name|ComparisonChain
operator|.
name|start
argument_list|()
operator|.
name|compare
argument_list|(
name|capacity
argument_list|,
name|other
operator|.
name|capacity
argument_list|)
operator|.
name|compare
argument_list|(
name|insertionGeneration
argument_list|,
name|other
operator|.
name|insertionGeneration
argument_list|)
operator|.
name|result
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|rhs
parameter_list|)
block|{
if|if
condition|(
name|rhs
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
try|try
block|{
name|Key
name|o
init|=
operator|(
name|Key
operator|)
name|rhs
decl_stmt|;
return|return
operator|(
name|compareTo
argument_list|(
name|o
argument_list|)
operator|==
literal|0
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
operator|new
name|HashCodeBuilder
argument_list|()
operator|.
name|append
argument_list|(
name|capacity
argument_list|)
operator|.
name|append
argument_list|(
name|insertionGeneration
argument_list|)
operator|.
name|toHashCode
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|final
name|TreeMap
argument_list|<
name|Key
argument_list|,
name|ByteBuffer
argument_list|>
name|buffers
init|=
operator|new
name|TreeMap
argument_list|<
name|Key
argument_list|,
name|ByteBuffer
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|TreeMap
argument_list|<
name|Key
argument_list|,
name|ByteBuffer
argument_list|>
name|directBuffers
init|=
operator|new
name|TreeMap
argument_list|<
name|Key
argument_list|,
name|ByteBuffer
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|long
name|currentGeneration
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|TreeMap
argument_list|<
name|Key
argument_list|,
name|ByteBuffer
argument_list|>
name|getBufferTree
parameter_list|(
name|boolean
name|direct
parameter_list|)
block|{
return|return
name|direct
condition|?
name|directBuffers
else|:
name|buffers
return|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|buffers
operator|.
name|clear
argument_list|()
expr_stmt|;
name|directBuffers
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getBuffer
parameter_list|(
name|boolean
name|direct
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|TreeMap
argument_list|<
name|Key
argument_list|,
name|ByteBuffer
argument_list|>
name|tree
init|=
name|getBufferTree
argument_list|(
name|direct
argument_list|)
decl_stmt|;
name|Map
operator|.
name|Entry
argument_list|<
name|Key
argument_list|,
name|ByteBuffer
argument_list|>
name|entry
init|=
name|tree
operator|.
name|ceilingEntry
argument_list|(
operator|new
name|Key
argument_list|(
name|length
argument_list|,
literal|0
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
return|return
name|direct
condition|?
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|length
argument_list|)
else|:
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|length
argument_list|)
return|;
block|}
name|tree
operator|.
name|remove
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|entry
operator|.
name|getValue
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|putBuffer
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|)
block|{
name|TreeMap
argument_list|<
name|Key
argument_list|,
name|ByteBuffer
argument_list|>
name|tree
init|=
name|getBufferTree
argument_list|(
name|buffer
operator|.
name|isDirect
argument_list|()
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|Key
name|key
init|=
operator|new
name|Key
argument_list|(
name|buffer
operator|.
name|capacity
argument_list|()
argument_list|,
name|currentGeneration
operator|++
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|tree
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|tree
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|buffer
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Buffers are indexed by (capacity, generation).
comment|// If our key is not unique on the first try, we try again
block|}
block|}
block|}
specifier|public
specifier|static
name|long
name|getFileId
parameter_list|(
name|FileSystem
name|fileSystem
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|pathStr
init|=
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileSystem
operator|instanceof
name|DistributedFileSystem
condition|)
block|{
name|DFSClient
name|client
init|=
operator|(
operator|(
name|DistributedFileSystem
operator|)
name|fileSystem
operator|)
operator|.
name|getClient
argument_list|()
decl_stmt|;
return|return
name|client
operator|.
name|getFileInfo
argument_list|(
name|pathStr
argument_list|)
operator|.
name|getFileId
argument_list|()
return|;
block|}
comment|// If we are not on DFS, we just hash the file name + size and hope for the best.
comment|// TODO: we assume it only happens in tests. Fix?
name|int
name|nameHash
init|=
name|pathStr
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|long
name|fileSize
init|=
name|fileSystem
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
operator|.
name|getLen
argument_list|()
decl_stmt|;
name|long
name|id
init|=
operator|(
operator|(
name|fileSize
operator|^
operator|(
name|fileSize
operator|>>>
literal|32
operator|)
operator|)
operator|<<
literal|32
operator|)
operator||
operator|(
operator|(
name|long
operator|)
name|nameHash
operator|&
literal|0xffffffffL
operator|)
decl_stmt|;
name|RecordReaderImpl
operator|.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cannot get unique file ID from "
operator|+
name|fileSystem
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"; using "
operator|+
name|id
operator|+
literal|"("
operator|+
name|pathStr
operator|+
literal|","
operator|+
name|nameHash
operator|+
literal|","
operator|+
name|fileSize
operator|+
literal|")"
argument_list|)
expr_stmt|;
return|return
name|id
return|;
block|}
block|}
end_class

end_unit

