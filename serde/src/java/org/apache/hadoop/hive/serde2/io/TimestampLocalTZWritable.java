begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|type
operator|.
name|TimestampTZ
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
name|ByteStream
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
name|lazybinary
operator|.
name|LazyBinaryUtils
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
name|WritableComparable
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
name|WritableUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|time
operator|.
name|ZoneId
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

begin_comment
comment|/**  * Writable for TimestampTZ. Copied from TimestampWritableV2.  * After we replace {@link java.sql.Timestamp} with {@link java.time.LocalDateTime} for Timestamp,  * it'll need a new Writable.  * All timestamp with time zone will be serialized as UTC retaining the instant.  * E.g. "2017-04-14 18:00:00 Asia/Shanghai" will be converted to  * "2017-04-14 10:00:00.0 Z".  */
end_comment

begin_class
specifier|public
class|class
name|TimestampLocalTZWritable
implements|implements
name|WritableComparable
argument_list|<
name|TimestampLocalTZWritable
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|nullBytes
init|=
block|{
literal|0x0
block|,
literal|0x0
block|,
literal|0x0
block|,
literal|0x0
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DECIMAL_OR_SECOND_VINT_FLAG
init|=
literal|1
operator|<<
literal|31
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|SEVEN_BYTE_LONG_SIGN_FLIP
init|=
literal|0xff80L
operator|<<
literal|48
decl_stmt|;
comment|// only need flip the MSB?
comment|/**    * The maximum number of bytes required for a TimestampWritableV2    */
specifier|public
specifier|static
specifier|final
name|int
name|MAX_BYTES
init|=
literal|13
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|BINARY_SORTABLE_LENGTH
init|=
literal|11
decl_stmt|;
specifier|private
name|TimestampTZ
name|timestampTZ
init|=
operator|new
name|TimestampTZ
argument_list|()
decl_stmt|;
specifier|private
name|ZoneId
name|timeZone
decl_stmt|;
comment|/**    * true if data is stored in timestamptz field rather than byte arrays.    * allows for lazy conversion to bytes when necessary    * false otherwise    */
specifier|private
name|boolean
name|bytesEmpty
init|=
literal|true
decl_stmt|;
specifier|private
name|boolean
name|timestampTZEmpty
init|=
literal|true
decl_stmt|;
comment|/* Allow use of external byte[] for efficiency */
specifier|private
name|byte
index|[]
name|currentBytes
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|internalBytes
init|=
operator|new
name|byte
index|[
name|MAX_BYTES
index|]
decl_stmt|;
specifier|private
name|byte
index|[]
name|externalBytes
decl_stmt|;
specifier|private
name|int
name|offset
decl_stmt|;
specifier|public
name|TimestampLocalTZWritable
parameter_list|()
block|{
name|this
operator|.
name|bytesEmpty
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|currentBytes
operator|=
name|internalBytes
expr_stmt|;
name|this
operator|.
name|offset
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|TimestampLocalTZWritable
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|ZoneId
name|timeZone
parameter_list|)
block|{
name|set
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|timeZone
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TimestampLocalTZWritable
parameter_list|(
name|TimestampLocalTZWritable
name|other
parameter_list|)
block|{
name|this
argument_list|(
name|other
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|other
operator|.
name|getTimestampTZ
argument_list|()
operator|.
name|getZonedDateTime
argument_list|()
operator|.
name|getZone
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TimestampLocalTZWritable
parameter_list|(
name|TimestampTZ
name|tstz
parameter_list|)
block|{
name|set
argument_list|(
name|tstz
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|ZoneId
name|timeZone
parameter_list|)
block|{
name|externalBytes
operator|=
name|bytes
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|timeZone
operator|=
name|timeZone
expr_stmt|;
name|bytesEmpty
operator|=
literal|false
expr_stmt|;
name|timestampTZEmpty
operator|=
literal|true
expr_stmt|;
name|currentBytes
operator|=
name|externalBytes
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|TimestampTZ
name|tstz
parameter_list|)
block|{
if|if
condition|(
name|tstz
operator|==
literal|null
condition|)
block|{
name|timestampTZ
operator|.
name|setZonedDateTime
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return;
block|}
name|timestampTZ
operator|=
name|tstz
expr_stmt|;
name|timeZone
operator|=
name|timestampTZ
operator|.
name|getZonedDateTime
argument_list|()
operator|.
name|getZone
argument_list|()
expr_stmt|;
name|bytesEmpty
operator|=
literal|true
expr_stmt|;
name|timestampTZEmpty
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|void
name|set
parameter_list|(
name|TimestampLocalTZWritable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|.
name|bytesEmpty
condition|)
block|{
name|set
argument_list|(
name|t
operator|.
name|getTimestampTZ
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|t
operator|.
name|currentBytes
operator|==
name|t
operator|.
name|externalBytes
condition|)
block|{
name|set
argument_list|(
name|t
operator|.
name|currentBytes
argument_list|,
name|t
operator|.
name|offset
argument_list|,
name|t
operator|.
name|timeZone
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|set
argument_list|(
name|t
operator|.
name|currentBytes
argument_list|,
literal|0
argument_list|,
name|t
operator|.
name|timeZone
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|setTimeZone
parameter_list|(
name|ZoneId
name|timeZone
parameter_list|)
block|{
if|if
condition|(
name|timestampTZ
operator|!=
literal|null
condition|)
block|{
name|timestampTZ
operator|.
name|setZonedDateTime
argument_list|(
name|timestampTZ
operator|.
name|getZonedDateTime
argument_list|()
operator|.
name|withZoneSameInstant
argument_list|(
name|timeZone
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|timeZone
operator|=
name|timeZone
expr_stmt|;
block|}
specifier|public
name|ZoneId
name|getTimeZone
parameter_list|()
block|{
return|return
name|timeZone
return|;
block|}
specifier|public
name|TimestampTZ
name|getTimestampTZ
parameter_list|()
block|{
name|populateTimestampTZ
argument_list|()
expr_stmt|;
return|return
name|timestampTZ
return|;
block|}
comment|/**    * Used to create copies of objects    *    * @return a copy of the internal TimestampTZWritable byte[]    */
specifier|public
name|byte
index|[]
name|getBytes
parameter_list|()
block|{
name|checkBytes
argument_list|()
expr_stmt|;
name|int
name|len
init|=
name|getTotalLength
argument_list|()
decl_stmt|;
name|byte
index|[]
name|b
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|currentBytes
argument_list|,
name|offset
argument_list|,
name|b
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
return|return
name|b
return|;
block|}
comment|/**    * @return length of serialized TimestampTZWritable data. As a side effect, populates the internal    * byte array if empty.    */
specifier|private
name|int
name|getTotalLength
parameter_list|()
block|{
name|checkBytes
argument_list|()
expr_stmt|;
return|return
name|getTotalLength
argument_list|(
name|currentBytes
argument_list|,
name|offset
argument_list|)
return|;
block|}
comment|/**    * The data of TimestampTZWritable can be stored either in a byte[]    * or in a TimestampTZ object. Calling this method ensures that the byte[]    * is populated from the TimestampTZ object if previously empty.    */
specifier|private
name|void
name|checkBytes
parameter_list|()
block|{
if|if
condition|(
name|bytesEmpty
condition|)
block|{
name|populateBytes
argument_list|()
expr_stmt|;
name|offset
operator|=
literal|0
expr_stmt|;
name|currentBytes
operator|=
name|internalBytes
expr_stmt|;
name|bytesEmpty
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|// Writes the TimestampTZ's serialized value to the internal byte array.
specifier|private
name|void
name|populateBytes
parameter_list|()
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|internalBytes
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|long
name|seconds
init|=
name|timestampTZ
operator|.
name|getEpochSecond
argument_list|()
decl_stmt|;
name|int
name|nanos
init|=
name|timestampTZ
operator|.
name|getNanos
argument_list|()
decl_stmt|;
name|boolean
name|hasSecondVInt
init|=
name|seconds
argument_list|<
literal|0
operator|||
name|seconds
argument_list|>
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
name|boolean
name|hasDecimal
init|=
name|setNanosBytes
argument_list|(
name|nanos
argument_list|,
name|internalBytes
argument_list|,
name|offset
operator|+
literal|4
argument_list|,
name|hasSecondVInt
argument_list|)
decl_stmt|;
name|int
name|firstInt
init|=
operator|(
name|int
operator|)
name|seconds
decl_stmt|;
if|if
condition|(
name|hasDecimal
operator|||
name|hasSecondVInt
condition|)
block|{
name|firstInt
operator||=
name|DECIMAL_OR_SECOND_VINT_FLAG
expr_stmt|;
block|}
name|intToBytes
argument_list|(
name|firstInt
argument_list|,
name|internalBytes
argument_list|,
name|offset
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasSecondVInt
condition|)
block|{
name|LazyBinaryUtils
operator|.
name|writeVLongToByteArray
argument_list|(
name|internalBytes
argument_list|,
name|offset
operator|+
literal|4
operator|+
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|internalBytes
index|[
name|offset
operator|+
literal|4
index|]
argument_list|)
argument_list|,
name|seconds
operator|>>
literal|31
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|populateTimestampTZ
parameter_list|()
block|{
if|if
condition|(
name|timestampTZEmpty
condition|)
block|{
if|if
condition|(
name|bytesEmpty
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Bytes are empty"
argument_list|)
throw|;
block|}
name|long
name|seconds
init|=
name|getSeconds
argument_list|(
name|currentBytes
argument_list|,
name|offset
argument_list|)
decl_stmt|;
name|int
name|nanos
init|=
name|hasDecimalOrSecondVInt
argument_list|(
name|currentBytes
index|[
name|offset
index|]
argument_list|)
condition|?
name|getNanos
argument_list|(
name|currentBytes
argument_list|,
name|offset
operator|+
literal|4
argument_list|)
else|:
literal|0
decl_stmt|;
name|timestampTZ
operator|.
name|set
argument_list|(
name|seconds
argument_list|,
name|nanos
argument_list|,
name|timeZone
argument_list|)
expr_stmt|;
name|timestampTZEmpty
operator|=
literal|false
expr_stmt|;
block|}
block|}
specifier|public
name|long
name|getSeconds
parameter_list|()
block|{
if|if
condition|(
operator|!
name|timestampTZEmpty
condition|)
block|{
return|return
name|timestampTZ
operator|.
name|getEpochSecond
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|bytesEmpty
condition|)
block|{
return|return
name|getSeconds
argument_list|(
name|currentBytes
argument_list|,
name|offset
argument_list|)
return|;
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Both timestamp and bytes are empty"
argument_list|)
throw|;
block|}
specifier|public
name|int
name|getNanos
parameter_list|()
block|{
if|if
condition|(
operator|!
name|timestampTZEmpty
condition|)
block|{
return|return
name|timestampTZ
operator|.
name|getNanos
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|bytesEmpty
condition|)
block|{
return|return
name|hasDecimalOrSecondVInt
argument_list|(
name|currentBytes
index|[
name|offset
index|]
argument_list|)
condition|?
name|getNanos
argument_list|(
name|currentBytes
argument_list|,
name|offset
operator|+
literal|4
argument_list|)
else|:
literal|0
return|;
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Both timestamp and bytes are empty"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|TimestampLocalTZWritable
name|o
parameter_list|)
block|{
return|return
name|getTimestampTZ
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|getTimestampTZ
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|instanceof
name|TimestampLocalTZWritable
condition|)
block|{
return|return
name|compareTo
argument_list|(
operator|(
name|TimestampLocalTZWritable
operator|)
name|o
argument_list|)
operator|==
literal|0
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|getTimestampTZ
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|populateTimestampTZ
argument_list|()
expr_stmt|;
return|return
name|timestampTZ
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|dataOutput
parameter_list|)
throws|throws
name|IOException
block|{
name|checkBytes
argument_list|()
expr_stmt|;
name|dataOutput
operator|.
name|write
argument_list|(
name|currentBytes
argument_list|,
name|offset
argument_list|,
name|getTotalLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|dataInput
parameter_list|)
throws|throws
name|IOException
block|{
name|dataInput
operator|.
name|readFully
argument_list|(
name|internalBytes
argument_list|,
literal|0
argument_list|,
literal|4
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasDecimalOrSecondVInt
argument_list|(
name|internalBytes
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
name|dataInput
operator|.
name|readFully
argument_list|(
name|internalBytes
argument_list|,
literal|4
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|int
name|len
init|=
operator|(
name|byte
operator|)
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|internalBytes
index|[
literal|4
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|len
operator|>
literal|1
condition|)
block|{
name|dataInput
operator|.
name|readFully
argument_list|(
name|internalBytes
argument_list|,
literal|5
argument_list|,
name|len
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|long
name|vlong
init|=
name|LazyBinaryUtils
operator|.
name|readVLongFromByteArray
argument_list|(
name|internalBytes
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|vlong
operator|>=
operator|-
literal|1000000000
operator|&&
name|vlong
operator|<=
literal|999999999
argument_list|,
literal|"Invalid nanos value for a TimestampTZWritable: "
operator|+
name|vlong
operator|+
literal|", expected to be between -1000000000 and 999999999."
argument_list|)
expr_stmt|;
if|if
condition|(
name|vlong
operator|<
literal|0
condition|)
block|{
comment|// This indicates there is a second VInt containing the additional bits of the seconds
comment|// field.
name|dataInput
operator|.
name|readFully
argument_list|(
name|internalBytes
argument_list|,
literal|4
operator|+
name|len
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|int
name|secondVIntLen
init|=
operator|(
name|byte
operator|)
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|internalBytes
index|[
literal|4
operator|+
name|len
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|secondVIntLen
operator|>
literal|1
condition|)
block|{
name|dataInput
operator|.
name|readFully
argument_list|(
name|internalBytes
argument_list|,
literal|5
operator|+
name|len
argument_list|,
name|secondVIntLen
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|currentBytes
operator|=
name|internalBytes
expr_stmt|;
name|offset
operator|=
literal|0
expr_stmt|;
name|timestampTZEmpty
operator|=
literal|true
expr_stmt|;
name|bytesEmpty
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|toBinarySortable
parameter_list|()
block|{
name|byte
index|[]
name|b
init|=
operator|new
name|byte
index|[
name|BINARY_SORTABLE_LENGTH
index|]
decl_stmt|;
name|int
name|nanos
init|=
name|getNanos
argument_list|()
decl_stmt|;
comment|// We flip the highest-order bit of the seven-byte representation of seconds to make negative
comment|// values come before positive ones.
name|long
name|seconds
init|=
name|getSeconds
argument_list|()
operator|^
name|SEVEN_BYTE_LONG_SIGN_FLIP
decl_stmt|;
name|sevenByteLongToBytes
argument_list|(
name|seconds
argument_list|,
name|b
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|intToBytes
argument_list|(
name|nanos
argument_list|,
name|b
argument_list|,
literal|7
argument_list|)
expr_stmt|;
return|return
name|b
return|;
block|}
specifier|public
name|void
name|fromBinarySortable
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|binSortOffset
parameter_list|,
name|ZoneId
name|timeZone
parameter_list|)
block|{
comment|// Flip the sign bit (and unused bits of the high-order byte) of the seven-byte long back.
name|long
name|seconds
init|=
name|readSevenByteLong
argument_list|(
name|bytes
argument_list|,
name|binSortOffset
argument_list|)
operator|^
name|SEVEN_BYTE_LONG_SIGN_FLIP
decl_stmt|;
name|int
name|nanos
init|=
name|bytesToInt
argument_list|(
name|bytes
argument_list|,
name|binSortOffset
operator|+
literal|7
argument_list|)
decl_stmt|;
name|timestampTZ
operator|.
name|set
argument_list|(
name|seconds
argument_list|,
name|nanos
argument_list|,
name|timeZone
argument_list|)
expr_stmt|;
name|timestampTZEmpty
operator|=
literal|false
expr_stmt|;
name|bytesEmpty
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|writeToByteStream
parameter_list|(
name|ByteStream
operator|.
name|RandomAccessOutput
name|byteStream
parameter_list|)
block|{
name|checkBytes
argument_list|()
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
name|currentBytes
argument_list|,
name|offset
argument_list|,
name|getTotalLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Given an integer representing nanoseconds, write its serialized    * value to the byte array b at offset    *    * @param nanos    * @param b    * @param offset    * @return    */
specifier|private
specifier|static
name|boolean
name|setNanosBytes
parameter_list|(
name|int
name|nanos
parameter_list|,
name|byte
index|[]
name|b
parameter_list|,
name|int
name|offset
parameter_list|,
name|boolean
name|hasSecondVInt
parameter_list|)
block|{
name|int
name|decimal
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|nanos
operator|!=
literal|0
condition|)
block|{
name|int
name|counter
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|counter
operator|<
literal|9
condition|)
block|{
name|decimal
operator|*=
literal|10
expr_stmt|;
name|decimal
operator|+=
name|nanos
operator|%
literal|10
expr_stmt|;
name|nanos
operator|/=
literal|10
expr_stmt|;
name|counter
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|hasSecondVInt
operator|||
name|decimal
operator|!=
literal|0
condition|)
block|{
comment|// We use the sign of the reversed-nanoseconds field to indicate that there is a second VInt
comment|// present.
name|LazyBinaryUtils
operator|.
name|writeVLongToByteArray
argument_list|(
name|b
argument_list|,
name|offset
argument_list|,
name|hasSecondVInt
condition|?
operator|(
operator|-
name|decimal
operator|-
literal|1
operator|)
else|:
name|decimal
argument_list|)
expr_stmt|;
block|}
return|return
name|decimal
operator|!=
literal|0
return|;
block|}
specifier|public
specifier|static
name|void
name|setTimestampTZ
parameter_list|(
name|TimestampTZ
name|t
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|ZoneId
name|timeZone
parameter_list|)
block|{
name|long
name|seconds
init|=
name|getSeconds
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|)
decl_stmt|;
name|int
name|nanos
init|=
name|hasDecimalOrSecondVInt
argument_list|(
name|bytes
index|[
name|offset
index|]
argument_list|)
condition|?
name|getNanos
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
literal|4
argument_list|)
else|:
literal|0
decl_stmt|;
name|t
operator|.
name|set
argument_list|(
name|seconds
argument_list|,
name|nanos
argument_list|,
name|timeZone
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|int
name|getTotalLength
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|int
name|len
init|=
literal|4
decl_stmt|;
if|if
condition|(
name|hasDecimalOrSecondVInt
argument_list|(
name|bytes
index|[
name|offset
index|]
argument_list|)
condition|)
block|{
name|int
name|firstVIntLen
init|=
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|bytes
index|[
name|offset
operator|+
literal|4
index|]
argument_list|)
decl_stmt|;
name|len
operator|+=
name|firstVIntLen
expr_stmt|;
if|if
condition|(
name|hasSecondVInt
argument_list|(
name|bytes
index|[
name|offset
operator|+
literal|4
index|]
argument_list|)
condition|)
block|{
name|len
operator|+=
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|bytes
index|[
name|offset
operator|+
literal|4
operator|+
name|firstVIntLen
index|]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|len
return|;
block|}
specifier|public
specifier|static
name|long
name|getSeconds
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|int
name|firstVInt
init|=
name|bytesToInt
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|)
decl_stmt|;
if|if
condition|(
name|firstVInt
operator|>=
literal|0
operator|||
operator|!
name|hasSecondVInt
argument_list|(
name|bytes
index|[
name|offset
operator|+
literal|4
index|]
argument_list|)
condition|)
block|{
return|return
name|firstVInt
operator|&
operator|~
name|DECIMAL_OR_SECOND_VINT_FLAG
return|;
block|}
return|return
operator|(
call|(
name|long
call|)
argument_list|(
name|firstVInt
operator|&
operator|~
name|DECIMAL_OR_SECOND_VINT_FLAG
argument_list|)
operator|)
operator||
operator|(
name|LazyBinaryUtils
operator|.
name|readVLongFromByteArray
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
literal|4
operator|+
name|WritableUtils
operator|.
name|decodeVIntSize
argument_list|(
name|bytes
index|[
name|offset
operator|+
literal|4
index|]
argument_list|)
argument_list|)
operator|<<
literal|31
operator|)
return|;
block|}
specifier|public
specifier|static
name|int
name|getNanos
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|int
name|val
init|=
operator|(
name|int
operator|)
name|LazyBinaryUtils
operator|.
name|readVLongFromByteArray
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|<
literal|0
condition|)
block|{
name|val
operator|=
operator|-
name|val
operator|-
literal|1
expr_stmt|;
block|}
name|int
name|len
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|floor
argument_list|(
name|Math
operator|.
name|log10
argument_list|(
name|val
argument_list|)
argument_list|)
operator|+
literal|1
decl_stmt|;
comment|// Reverse the value
name|int
name|tmp
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|val
operator|!=
literal|0
condition|)
block|{
name|tmp
operator|*=
literal|10
expr_stmt|;
name|tmp
operator|+=
name|val
operator|%
literal|10
expr_stmt|;
name|val
operator|/=
literal|10
expr_stmt|;
block|}
name|val
operator|=
name|tmp
expr_stmt|;
if|if
condition|(
name|len
operator|<
literal|9
condition|)
block|{
name|val
operator|*=
name|Math
operator|.
name|pow
argument_list|(
literal|10
argument_list|,
literal|9
operator|-
name|len
argument_list|)
expr_stmt|;
block|}
return|return
name|val
return|;
block|}
specifier|private
specifier|static
name|boolean
name|hasDecimalOrSecondVInt
parameter_list|(
name|byte
name|b
parameter_list|)
block|{
return|return
name|b
operator|<
literal|0
return|;
block|}
specifier|private
specifier|static
name|boolean
name|hasSecondVInt
parameter_list|(
name|byte
name|b
parameter_list|)
block|{
return|return
name|WritableUtils
operator|.
name|isNegativeVInt
argument_list|(
name|b
argument_list|)
return|;
block|}
comment|/**    * Writes<code>value</code> into<code>dest</code> at<code>offset</code>    *    * @param value    * @param dest    * @param offset    */
specifier|private
specifier|static
name|void
name|intToBytes
parameter_list|(
name|int
name|value
parameter_list|,
name|byte
index|[]
name|dest
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|dest
index|[
name|offset
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
name|value
operator|>>
literal|24
operator|)
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
name|dest
index|[
name|offset
operator|+
literal|1
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
name|value
operator|>>
literal|16
operator|)
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
name|dest
index|[
name|offset
operator|+
literal|2
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
name|value
operator|>>
literal|8
operator|)
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
name|dest
index|[
name|offset
operator|+
literal|3
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|value
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
block|}
comment|/**    * Writes<code>value</code> into<code>dest</code> at<code>offset</code> as a seven-byte    * serialized long number.    */
specifier|private
specifier|static
name|void
name|sevenByteLongToBytes
parameter_list|(
name|long
name|value
parameter_list|,
name|byte
index|[]
name|dest
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|dest
index|[
name|offset
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
name|value
operator|>>
literal|48
operator|)
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
name|dest
index|[
name|offset
operator|+
literal|1
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
name|value
operator|>>
literal|40
operator|)
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
name|dest
index|[
name|offset
operator|+
literal|2
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
name|value
operator|>>
literal|32
operator|)
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
name|dest
index|[
name|offset
operator|+
literal|3
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
name|value
operator|>>
literal|24
operator|)
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
name|dest
index|[
name|offset
operator|+
literal|4
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
name|value
operator|>>
literal|16
operator|)
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
name|dest
index|[
name|offset
operator|+
literal|5
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
operator|(
name|value
operator|>>
literal|8
operator|)
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
name|dest
index|[
name|offset
operator|+
literal|6
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|value
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param bytes    * @param offset    * @return integer represented by the four bytes in<code>bytes</code>    * beginning at<code>offset</code>    */
specifier|private
specifier|static
name|int
name|bytesToInt
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
return|return
operator|(
operator|(
literal|0xFF
operator|&
name|bytes
index|[
name|offset
index|]
operator|)
operator|<<
literal|24
operator|)
operator||
operator|(
operator|(
literal|0xFF
operator|&
name|bytes
index|[
name|offset
operator|+
literal|1
index|]
operator|)
operator|<<
literal|16
operator|)
operator||
operator|(
operator|(
literal|0xFF
operator|&
name|bytes
index|[
name|offset
operator|+
literal|2
index|]
operator|)
operator|<<
literal|8
operator|)
operator||
operator|(
literal|0xFF
operator|&
name|bytes
index|[
name|offset
operator|+
literal|3
index|]
operator|)
return|;
block|}
specifier|private
specifier|static
name|long
name|readSevenByteLong
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
comment|// We need to shift everything 8 bits left and then shift back to populate the sign field.
return|return
operator|(
operator|(
operator|(
literal|0xFFL
operator|&
name|bytes
index|[
name|offset
index|]
operator|)
operator|<<
literal|56
operator|)
operator||
operator|(
operator|(
literal|0xFFL
operator|&
name|bytes
index|[
name|offset
operator|+
literal|1
index|]
operator|)
operator|<<
literal|48
operator|)
operator||
operator|(
operator|(
literal|0xFFL
operator|&
name|bytes
index|[
name|offset
operator|+
literal|2
index|]
operator|)
operator|<<
literal|40
operator|)
operator||
operator|(
operator|(
literal|0xFFL
operator|&
name|bytes
index|[
name|offset
operator|+
literal|3
index|]
operator|)
operator|<<
literal|32
operator|)
operator||
operator|(
operator|(
literal|0xFFL
operator|&
name|bytes
index|[
name|offset
operator|+
literal|4
index|]
operator|)
operator|<<
literal|24
operator|)
operator||
operator|(
operator|(
literal|0xFFL
operator|&
name|bytes
index|[
name|offset
operator|+
literal|5
index|]
operator|)
operator|<<
literal|16
operator|)
operator||
operator|(
operator|(
literal|0xFFL
operator|&
name|bytes
index|[
name|offset
operator|+
literal|6
index|]
operator|)
operator|<<
literal|8
operator|)
operator|)
operator|>>
literal|8
return|;
block|}
block|}
end_class

end_unit

