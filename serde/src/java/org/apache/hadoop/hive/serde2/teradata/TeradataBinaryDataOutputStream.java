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
name|teradata
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
name|io
operator|.
name|EndianUtils
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
name|io
operator|.
name|output
operator|.
name|ByteArrayOutputStream
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
name|ArrayUtils
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
name|serde2
operator|.
name|io
operator|.
name|DateWritableV2
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
name|io
operator|.
name|HiveCharWritable
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
name|io
operator|.
name|HiveDecimalWritable
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
name|io
operator|.
name|HiveVarcharWritable
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
name|io
operator|.
name|TimestampWritableV2
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
name|Text
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
name|math
operator|.
name|BigInteger
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
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|String
operator|.
name|join
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|String
operator|.
name|format
import|;
end_import

begin_comment
comment|/**  * The TeradataBinaryDataOutputStream is used to produce the output in compliance with the Teradata binary format,  * so the output can be directly used to load into Teradata DB using TPT fastload.  * Since the TD binary format uses little-endian to handle the SHORT, INT, LONG, DOUBLE and etc.  * while the Hadoop uses big-endian,  * We extend SwappedDataInputStream to return qualified bytes for these types and extend to handle the Teradata  * specific types like VARCHAR, CHAR, TIMESTAMP, DATE...  */
end_comment

begin_class
specifier|public
class|class
name|TeradataBinaryDataOutputStream
extends|extends
name|ByteArrayOutputStream
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
name|TeradataBinaryDataOutputStream
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|TIMESTAMP_NO_NANOS_BYTE_NUM
init|=
literal|19
decl_stmt|;
specifier|public
name|TeradataBinaryDataOutputStream
parameter_list|()
block|{   }
comment|/**    * Write VARCHAR(N).    * The representation of Varchar in Teradata binary format is:    * the first two bytes represent the length N of this varchar field,    * the next N bytes represent the content of this varchar field.    * To pad the null varchar, the length will be 0 and the content will be none.    *    * @param writable the writable    * @throws IOException the io exception    */
specifier|public
name|void
name|writeVarChar
parameter_list|(
name|HiveVarcharWritable
name|writable
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|writable
operator|==
literal|null
condition|)
block|{
name|EndianUtils
operator|.
name|writeSwappedShort
argument_list|(
name|this
argument_list|,
operator|(
name|short
operator|)
literal|0
argument_list|)
expr_stmt|;
return|return;
block|}
name|Text
name|t
init|=
name|writable
operator|.
name|getTextValue
argument_list|()
decl_stmt|;
name|int
name|varcharLength
init|=
name|t
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|EndianUtils
operator|.
name|writeSwappedShort
argument_list|(
name|this
argument_list|,
operator|(
name|short
operator|)
name|varcharLength
argument_list|)
expr_stmt|;
comment|// write the varchar length
name|write
argument_list|(
name|t
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|varcharLength
argument_list|)
expr_stmt|;
comment|// write the varchar content
block|}
comment|/**    * Write INT.    * using little-endian to write integer.    *    * @param i the    * @throws IOException the io exception    */
specifier|public
name|void
name|writeInt
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
name|EndianUtils
operator|.
name|writeSwappedInteger
argument_list|(
name|this
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
comment|/**    * Write TIMESTAMP(N).    * The representation of timestamp in Teradata binary format is:    * the byte number to read is based on the precision of timestamp,    * each byte represents one char and the timestamp is using string representation,    * eg: for 1911-11-11 19:20:21.433200 in TIMESTAMP(3), we will cut it to be 1911-11-11 19:20:21.433 and write    * 31 39  31 31 2d 31 31 2d 31 31 20 31 39 3a 32 30 3a 32 31 2e 34 33 33.    * the null timestamp will use space to pad.    *    * @param timestamp the timestamp    * @param byteNum the byte number the timestamp will write    * @throws IOException the io exception    */
specifier|public
name|void
name|writeTimestamp
parameter_list|(
name|TimestampWritableV2
name|timestamp
parameter_list|,
name|int
name|byteNum
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|timestamp
operator|==
literal|null
condition|)
block|{
name|String
name|pad
init|=
name|join
argument_list|(
literal|""
argument_list|,
name|Collections
operator|.
name|nCopies
argument_list|(
name|byteNum
argument_list|,
literal|" "
argument_list|)
argument_list|)
decl_stmt|;
name|write
argument_list|(
name|pad
operator|.
name|getBytes
argument_list|(
literal|"UTF8"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|String
name|sTimeStamp
init|=
name|timestamp
operator|.
name|getTimestamp
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|sTimeStamp
operator|.
name|length
argument_list|()
operator|>=
name|byteNum
condition|)
block|{
name|write
argument_list|(
name|sTimeStamp
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|byteNum
argument_list|)
operator|.
name|getBytes
argument_list|(
literal|"UTF8"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|write
argument_list|(
name|sTimeStamp
operator|.
name|getBytes
argument_list|(
literal|"UTF8"
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|pad
decl_stmt|;
if|if
condition|(
name|sTimeStamp
operator|.
name|length
argument_list|()
operator|==
name|TIMESTAMP_NO_NANOS_BYTE_NUM
condition|)
block|{
name|pad
operator|=
literal|"."
operator|+
name|join
argument_list|(
literal|""
argument_list|,
name|Collections
operator|.
name|nCopies
argument_list|(
name|byteNum
operator|-
name|sTimeStamp
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|,
literal|"0"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|pad
operator|=
name|join
argument_list|(
literal|""
argument_list|,
name|Collections
operator|.
name|nCopies
argument_list|(
name|byteNum
operator|-
name|sTimeStamp
operator|.
name|length
argument_list|()
argument_list|,
literal|"0"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|write
argument_list|(
name|pad
operator|.
name|getBytes
argument_list|(
literal|"UTF8"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Write DOUBLE.    * using little-endian to write double.    *    * @param d the d    * @throws IOException the io exception    */
specifier|public
name|void
name|writeDouble
parameter_list|(
name|double
name|d
parameter_list|)
throws|throws
name|IOException
block|{
name|EndianUtils
operator|.
name|writeSwappedDouble
argument_list|(
name|this
argument_list|,
name|d
argument_list|)
expr_stmt|;
block|}
comment|/**    * Write DATE.    * The representation of date in Teradata binary format is:    * The Date D is a int with 4 bytes using little endian.    * The representation is (YYYYMMDD - 19000000).toInt -&gt; D    * eg. 1911.11.11 -&gt; 19111111 -&gt; 111111 -&gt; 07 b2 01 00 in little endian.    * the null date will use 0 to pad.    *    * @param date the date    * @throws IOException the io exception    */
specifier|public
name|void
name|writeDate
parameter_list|(
name|DateWritableV2
name|date
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|date
operator|==
literal|null
condition|)
block|{
name|EndianUtils
operator|.
name|writeSwappedInteger
argument_list|(
name|this
argument_list|,
literal|0
argument_list|)
expr_stmt|;
return|return;
block|}
name|int
name|toWrite
init|=
name|date
operator|.
name|get
argument_list|()
operator|.
name|getYear
argument_list|()
operator|*
literal|10000
operator|+
name|date
operator|.
name|get
argument_list|()
operator|.
name|getMonth
argument_list|()
operator|*
literal|100
operator|+
name|date
operator|.
name|get
argument_list|()
operator|.
name|getDay
argument_list|()
operator|-
literal|19000000
decl_stmt|;
name|EndianUtils
operator|.
name|writeSwappedInteger
argument_list|(
name|this
argument_list|,
name|toWrite
argument_list|)
expr_stmt|;
block|}
comment|/**    * Write LONG.    * using little-endian to write double.    *    * @param l the l    * @throws IOException the io exception    */
specifier|public
name|void
name|writeLong
parameter_list|(
name|long
name|l
parameter_list|)
throws|throws
name|IOException
block|{
name|EndianUtils
operator|.
name|writeSwappedLong
argument_list|(
name|this
argument_list|,
name|l
argument_list|)
expr_stmt|;
block|}
comment|/**    * Write CHAR(N).    * The representation of char in Teradata binary format is:    * the byte number to read is based on the [charLength] * [bytePerChar]&lt;- totalLength,    * bytePerChar is decided by the charset: LATAIN charset is 2 bytes per char and UNICODE charset is 3 bytes per char.    * the null char will use space to pad.    *    * @param writable the writable    * @param length the byte n    * @throws IOException the io exception    */
specifier|public
name|void
name|writeChar
parameter_list|(
name|HiveCharWritable
name|writable
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|writable
operator|==
literal|null
condition|)
block|{
name|String
name|pad
init|=
name|join
argument_list|(
literal|""
argument_list|,
name|Collections
operator|.
name|nCopies
argument_list|(
name|length
argument_list|,
literal|" "
argument_list|)
argument_list|)
decl_stmt|;
name|write
argument_list|(
name|pad
operator|.
name|getBytes
argument_list|(
literal|"UTF8"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|Text
name|t
init|=
name|writable
operator|.
name|getStrippedValue
argument_list|()
decl_stmt|;
name|int
name|contentLength
init|=
name|t
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|write
argument_list|(
name|t
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|contentLength
argument_list|)
expr_stmt|;
if|if
condition|(
name|length
operator|-
name|contentLength
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|format
argument_list|(
literal|"The byte num %s of HiveCharWritable is more than the byte num %s we can hold. "
operator|+
literal|"The content of HiveCharWritable is %s"
argument_list|,
name|contentLength
argument_list|,
name|length
argument_list|,
name|writable
operator|.
name|getPaddedValue
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|length
operator|>
name|contentLength
condition|)
block|{
name|String
name|pad
init|=
name|join
argument_list|(
literal|""
argument_list|,
name|Collections
operator|.
name|nCopies
argument_list|(
name|length
operator|-
name|contentLength
argument_list|,
literal|" "
argument_list|)
argument_list|)
decl_stmt|;
name|write
argument_list|(
name|pad
operator|.
name|getBytes
argument_list|(
literal|"UTF8"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Write DECIMAL(P, S).    * The representation of decimal in Teradata binary format is:    * the byte number to read is decided solely by the precision(P),    * HiveDecimal is constructed through the byte array and scale.    * the rest of byte will use 0x00 to pad (positive) and use 0xFF to pad (negative).    * the null DECIMAL will use 0x00 to pad.    *    * @param writable the writable    * @param byteNum the byte num    * @throws IOException the io exception    */
specifier|public
name|void
name|writeDecimal
parameter_list|(
name|HiveDecimalWritable
name|writable
parameter_list|,
name|int
name|byteNum
parameter_list|,
name|int
name|scale
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|writable
operator|==
literal|null
condition|)
block|{
name|byte
index|[]
name|pad
init|=
operator|new
name|byte
index|[
name|byteNum
index|]
decl_stmt|;
name|write
argument_list|(
name|pad
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// since the HiveDecimal will auto adjust the scale to save resource
comment|// we need to adjust it back otherwise the output bytes will be wrong
name|int
name|hiveScale
init|=
name|writable
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|scale
argument_list|()
decl_stmt|;
name|BigInteger
name|bigInteger
init|=
name|writable
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|unscaledValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|hiveScale
operator|<
name|scale
condition|)
block|{
name|BigInteger
name|multiplicand
init|=
operator|new
name|BigInteger
argument_list|(
literal|"1"
operator|+
name|join
argument_list|(
literal|""
argument_list|,
name|Collections
operator|.
name|nCopies
argument_list|(
name|scale
operator|-
name|hiveScale
argument_list|,
literal|"0"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|bigInteger
operator|=
name|bigInteger
operator|.
name|multiply
argument_list|(
name|multiplicand
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|content
init|=
name|bigInteger
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|int
name|signBit
init|=
name|content
index|[
literal|0
index|]
operator|>>
literal|7
operator|&
literal|1
decl_stmt|;
name|ArrayUtils
operator|.
name|reverse
argument_list|(
name|content
argument_list|)
expr_stmt|;
name|write
argument_list|(
name|content
argument_list|)
expr_stmt|;
if|if
condition|(
name|byteNum
operator|>
name|content
operator|.
name|length
condition|)
block|{
name|byte
index|[]
name|pad
decl_stmt|;
if|if
condition|(
name|signBit
operator|==
literal|0
condition|)
block|{
name|pad
operator|=
operator|new
name|byte
index|[
name|byteNum
operator|-
name|content
operator|.
name|length
index|]
expr_stmt|;
block|}
else|else
block|{
name|pad
operator|=
operator|new
name|byte
index|[
name|byteNum
operator|-
name|content
operator|.
name|length
index|]
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|pad
argument_list|,
operator|(
name|byte
operator|)
literal|255
argument_list|)
expr_stmt|;
block|}
name|write
argument_list|(
name|pad
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Write SHORT.    * using little-endian to write short.    *    * @param s the s    * @throws IOException the io exception    */
specifier|public
name|void
name|writeShort
parameter_list|(
name|short
name|s
parameter_list|)
throws|throws
name|IOException
block|{
name|EndianUtils
operator|.
name|writeSwappedShort
argument_list|(
name|this
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
comment|/**    * Write VARBYTE(N).    * The representation of VARBYTE in Teradata binary format is:    * the first two bytes represent the length N of this varchar field,    * the next N bytes represent the content of this varchar field.    * To pad the null varbyte, the length will be 0 and the content will be none.    *    * @param writable the writable    * @throws IOException the io exception    */
specifier|public
name|void
name|writeVarByte
parameter_list|(
name|BytesWritable
name|writable
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|writable
operator|==
literal|null
condition|)
block|{
name|EndianUtils
operator|.
name|writeSwappedShort
argument_list|(
name|this
argument_list|,
operator|(
name|short
operator|)
literal|0
argument_list|)
expr_stmt|;
return|return;
block|}
name|int
name|varbyteLength
init|=
name|writable
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|EndianUtils
operator|.
name|writeSwappedShort
argument_list|(
name|this
argument_list|,
operator|(
name|short
operator|)
name|varbyteLength
argument_list|)
expr_stmt|;
comment|// write the varbyte length
name|write
argument_list|(
name|writable
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|varbyteLength
argument_list|)
expr_stmt|;
comment|// write the varchar content
block|}
block|}
end_class

end_unit

