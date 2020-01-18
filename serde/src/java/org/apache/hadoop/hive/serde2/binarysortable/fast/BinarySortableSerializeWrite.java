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
operator|.
name|fast
package|;
end_package

begin_import
import|import static
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
operator|.
name|BinarySortableSerDe
operator|.
name|ONE
import|;
end_import

begin_import
import|import static
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
operator|.
name|BinarySortableSerDe
operator|.
name|ZERO
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
name|Properties
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
name|Date
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
name|HiveChar
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
name|HiveDecimal
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
name|HiveIntervalDayTime
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
name|HiveIntervalYearMonth
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
name|HiveVarchar
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
name|Timestamp
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
operator|.
name|Output
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
name|binarysortable
operator|.
name|BinarySortableSerDe
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
name|binarysortable
operator|.
name|BinarySortableUtils
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
name|fast
operator|.
name|SerializeWrite
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
name|TimestampWritableV2
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/*  * Directly serialize, field-by-field, the BinarySortable format.  *  * This is an alternative way to serialize than what is provided by BinarySortableSerDe.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|BinarySortableSerializeWrite
implements|implements
name|SerializeWrite
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|BinarySortableSerializeWrite
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|BinarySortableSerializeWrite
name|with
parameter_list|(
name|Properties
name|tbl
parameter_list|,
name|int
name|columnCount
parameter_list|)
block|{
name|boolean
index|[]
name|columnSortOrderIsDesc
init|=
operator|new
name|boolean
index|[
name|columnCount
index|]
decl_stmt|;
name|byte
index|[]
name|columnNullMarker
init|=
operator|new
name|byte
index|[
name|columnCount
index|]
decl_stmt|;
name|byte
index|[]
name|columnNotNullMarker
init|=
operator|new
name|byte
index|[
name|columnCount
index|]
decl_stmt|;
name|BinarySortableUtils
operator|.
name|fillOrderArrays
argument_list|(
name|tbl
argument_list|,
name|columnSortOrderIsDesc
argument_list|,
name|columnNullMarker
argument_list|,
name|columnNotNullMarker
argument_list|)
expr_stmt|;
return|return
operator|new
name|BinarySortableSerializeWrite
argument_list|(
name|columnSortOrderIsDesc
argument_list|,
name|columnNullMarker
argument_list|,
name|columnNotNullMarker
argument_list|)
return|;
block|}
specifier|private
name|Output
name|output
decl_stmt|;
comment|// The sort order (ascending/descending) for each field. Set to true when descending (invert).
specifier|private
name|boolean
index|[]
name|columnSortOrderIsDesc
decl_stmt|;
comment|// Null first/last
specifier|private
name|byte
index|[]
name|columnNullMarker
decl_stmt|;
specifier|private
name|byte
index|[]
name|columnNotNullMarker
decl_stmt|;
comment|// Which field we are on.  We start with -1 to be consistent in style with
comment|// BinarySortableDeserializeRead.
specifier|private
name|int
name|index
decl_stmt|;
specifier|private
name|int
name|level
decl_stmt|;
specifier|private
name|TimestampWritableV2
name|tempTimestampWritable
decl_stmt|;
specifier|private
name|HiveDecimalWritable
name|hiveDecimalWritable
decl_stmt|;
specifier|private
name|byte
index|[]
name|decimalBytesScratch
decl_stmt|;
specifier|public
name|BinarySortableSerializeWrite
parameter_list|(
name|boolean
index|[]
name|columnSortOrderIsDesc
parameter_list|,
name|byte
index|[]
name|columnNullMarker
parameter_list|,
name|byte
index|[]
name|columnNotNullMarker
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|columnSortOrderIsDesc
operator|=
name|columnSortOrderIsDesc
expr_stmt|;
name|this
operator|.
name|columnNullMarker
operator|=
name|columnNullMarker
expr_stmt|;
name|this
operator|.
name|columnNotNullMarker
operator|=
name|columnNotNullMarker
expr_stmt|;
block|}
comment|/*    * Use this constructor when only ascending sort order is used.    * By default for ascending order, NULL first.    */
specifier|public
name|BinarySortableSerializeWrite
parameter_list|(
name|int
name|fieldCount
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|columnSortOrderIsDesc
operator|=
operator|new
name|boolean
index|[
name|fieldCount
index|]
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|columnSortOrderIsDesc
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|columnNullMarker
operator|=
operator|new
name|byte
index|[
name|fieldCount
index|]
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|columnNullMarker
argument_list|,
name|ZERO
argument_list|)
expr_stmt|;
name|columnNotNullMarker
operator|=
operator|new
name|byte
index|[
name|fieldCount
index|]
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|columnNotNullMarker
argument_list|,
name|ONE
argument_list|)
expr_stmt|;
block|}
comment|// Not public since we must have the field count or column sort order information.
specifier|private
name|BinarySortableSerializeWrite
parameter_list|()
block|{
name|tempTimestampWritable
operator|=
operator|new
name|TimestampWritableV2
argument_list|()
expr_stmt|;
block|}
comment|/*    * Set the buffer that will receive the serialized data.  The output buffer will be reset.    */
annotation|@
name|Override
specifier|public
name|void
name|set
parameter_list|(
name|Output
name|output
parameter_list|)
block|{
name|this
operator|.
name|output
operator|=
name|output
expr_stmt|;
name|this
operator|.
name|output
operator|.
name|reset
argument_list|()
expr_stmt|;
name|index
operator|=
operator|-
literal|1
expr_stmt|;
name|level
operator|=
literal|0
expr_stmt|;
block|}
comment|/*    * Set the buffer that will receive the serialized data.  The output buffer will NOT be reset.    */
annotation|@
name|Override
specifier|public
name|void
name|setAppend
parameter_list|(
name|Output
name|output
parameter_list|)
block|{
name|this
operator|.
name|output
operator|=
name|output
expr_stmt|;
name|index
operator|=
operator|-
literal|1
expr_stmt|;
name|level
operator|=
literal|0
expr_stmt|;
block|}
comment|/*    * Reset the previously supplied buffer that will receive the serialized data.    */
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|output
operator|.
name|reset
argument_list|()
expr_stmt|;
name|index
operator|=
operator|-
literal|1
expr_stmt|;
name|level
operator|=
literal|0
expr_stmt|;
block|}
comment|/*    * Write a NULL field.    */
annotation|@
name|Override
specifier|public
name|void
name|writeNull
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|level
operator|==
literal|0
condition|)
block|{
name|index
operator|++
expr_stmt|;
block|}
name|BinarySortableSerDe
operator|.
name|writeByte
argument_list|(
name|output
argument_list|,
name|columnNullMarker
index|[
name|index
index|]
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|beginElement
parameter_list|()
block|{
if|if
condition|(
name|level
operator|==
literal|0
condition|)
block|{
name|index
operator|++
expr_stmt|;
block|}
name|BinarySortableSerDe
operator|.
name|writeByte
argument_list|(
name|output
argument_list|,
name|columnNotNullMarker
index|[
name|index
index|]
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * BOOLEAN.    */
annotation|@
name|Override
specifier|public
name|void
name|writeBoolean
parameter_list|(
name|boolean
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|writeByte
argument_list|(
name|output
argument_list|,
call|(
name|byte
call|)
argument_list|(
name|v
condition|?
literal|2
else|:
literal|1
argument_list|)
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * BYTE.    */
annotation|@
name|Override
specifier|public
name|void
name|writeByte
parameter_list|(
name|byte
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|writeByte
argument_list|(
name|output
argument_list|,
call|(
name|byte
call|)
argument_list|(
name|v
operator|^
literal|0x80
argument_list|)
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * SHORT.    */
annotation|@
name|Override
specifier|public
name|void
name|writeShort
parameter_list|(
name|short
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeShort
argument_list|(
name|output
argument_list|,
name|v
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * INT.    */
annotation|@
name|Override
specifier|public
name|void
name|writeInt
parameter_list|(
name|int
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeInt
argument_list|(
name|output
argument_list|,
name|v
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * LONG.    */
annotation|@
name|Override
specifier|public
name|void
name|writeLong
parameter_list|(
name|long
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeLong
argument_list|(
name|output
argument_list|,
name|v
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * FLOAT.    */
annotation|@
name|Override
specifier|public
name|void
name|writeFloat
parameter_list|(
name|float
name|vf
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeFloat
argument_list|(
name|output
argument_list|,
name|vf
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * DOUBLE.    */
annotation|@
name|Override
specifier|public
name|void
name|writeDouble
parameter_list|(
name|double
name|vd
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeDouble
argument_list|(
name|output
argument_list|,
name|vd
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * STRING.    *     * Can be used to write CHAR and VARCHAR when the caller takes responsibility for    * truncation/padding issues.    */
annotation|@
name|Override
specifier|public
name|void
name|writeString
parameter_list|(
name|byte
index|[]
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeBytes
argument_list|(
name|output
argument_list|,
name|v
argument_list|,
literal|0
argument_list|,
name|v
operator|.
name|length
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeString
parameter_list|(
name|byte
index|[]
name|v
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeBytes
argument_list|(
name|output
argument_list|,
name|v
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * CHAR.    */
annotation|@
name|Override
specifier|public
name|void
name|writeHiveChar
parameter_list|(
name|HiveChar
name|hiveChar
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|string
init|=
name|hiveChar
operator|.
name|getStrippedValue
argument_list|()
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|string
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|writeString
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
comment|/*    * VARCHAR.    */
annotation|@
name|Override
specifier|public
name|void
name|writeHiveVarchar
parameter_list|(
name|HiveVarchar
name|hiveVarchar
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|string
init|=
name|hiveVarchar
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|string
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|writeString
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
comment|/*    * BINARY.    */
annotation|@
name|Override
specifier|public
name|void
name|writeBinary
parameter_list|(
name|byte
index|[]
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeBytes
argument_list|(
name|output
argument_list|,
name|v
argument_list|,
literal|0
argument_list|,
name|v
operator|.
name|length
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeBinary
parameter_list|(
name|byte
index|[]
name|v
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeBytes
argument_list|(
name|output
argument_list|,
name|v
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * DATE.    */
annotation|@
name|Override
specifier|public
name|void
name|writeDate
parameter_list|(
name|Date
name|date
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeInt
argument_list|(
name|output
argument_list|,
name|DateWritableV2
operator|.
name|dateToDays
argument_list|(
name|date
argument_list|)
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|// We provide a faster way to write a date without a Date object.
annotation|@
name|Override
specifier|public
name|void
name|writeDate
parameter_list|(
name|int
name|dateAsDays
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeInt
argument_list|(
name|output
argument_list|,
name|dateAsDays
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * TIMESTAMP.    */
annotation|@
name|Override
specifier|public
name|void
name|writeTimestamp
parameter_list|(
name|Timestamp
name|vt
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|tempTimestampWritable
operator|.
name|set
argument_list|(
name|vt
argument_list|)
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeTimestampWritable
argument_list|(
name|output
argument_list|,
name|tempTimestampWritable
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * INTERVAL_YEAR_MONTH.    */
annotation|@
name|Override
specifier|public
name|void
name|writeHiveIntervalYearMonth
parameter_list|(
name|HiveIntervalYearMonth
name|viyt
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeHiveIntervalYearMonth
argument_list|(
name|output
argument_list|,
name|viyt
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeHiveIntervalYearMonth
parameter_list|(
name|int
name|totalMonths
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeInt
argument_list|(
name|output
argument_list|,
name|totalMonths
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * INTERVAL_DAY_TIME.    */
annotation|@
name|Override
specifier|public
name|void
name|writeHiveIntervalDayTime
parameter_list|(
name|HiveIntervalDayTime
name|vidt
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|serializeHiveIntervalDayTime
argument_list|(
name|output
argument_list|,
name|vidt
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * DECIMAL.    *    * NOTE: The scale parameter is for text serialization (e.g. HiveDecimal.toFormatString) that    * creates trailing zeroes output decimals.    */
annotation|@
name|Override
specifier|public
name|void
name|writeDecimal64
parameter_list|(
name|long
name|decimal64Long
parameter_list|,
name|int
name|scale
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|hiveDecimalWritable
operator|==
literal|null
condition|)
block|{
name|hiveDecimalWritable
operator|=
operator|new
name|HiveDecimalWritable
argument_list|()
expr_stmt|;
block|}
name|hiveDecimalWritable
operator|.
name|deserialize64
argument_list|(
name|decimal64Long
argument_list|,
name|scale
argument_list|)
expr_stmt|;
name|writeHiveDecimal
argument_list|(
name|hiveDecimalWritable
argument_list|,
name|scale
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeHiveDecimal
parameter_list|(
name|HiveDecimal
name|dec
parameter_list|,
name|int
name|scale
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
if|if
condition|(
name|decimalBytesScratch
operator|==
literal|null
condition|)
block|{
name|decimalBytesScratch
operator|=
operator|new
name|byte
index|[
name|HiveDecimal
operator|.
name|SCRATCH_BUFFER_LEN_TO_BYTES
index|]
expr_stmt|;
block|}
name|BinarySortableSerDe
operator|.
name|serializeHiveDecimal
argument_list|(
name|output
argument_list|,
name|dec
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|,
name|decimalBytesScratch
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeHiveDecimal
parameter_list|(
name|HiveDecimalWritable
name|decWritable
parameter_list|,
name|int
name|scale
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
if|if
condition|(
name|decimalBytesScratch
operator|==
literal|null
condition|)
block|{
name|decimalBytesScratch
operator|=
operator|new
name|byte
index|[
name|HiveDecimal
operator|.
name|SCRATCH_BUFFER_LEN_TO_BYTES
index|]
expr_stmt|;
block|}
name|BinarySortableSerDe
operator|.
name|serializeHiveDecimal
argument_list|(
name|output
argument_list|,
name|decWritable
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|,
name|decimalBytesScratch
argument_list|)
expr_stmt|;
block|}
comment|/*    * List    */
annotation|@
name|Override
specifier|public
name|void
name|beginList
parameter_list|(
name|List
name|list
parameter_list|)
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|level
operator|++
expr_stmt|;
if|if
condition|(
operator|!
name|list
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|BinarySortableSerDe
operator|.
name|writeByte
argument_list|(
name|output
argument_list|,
operator|(
name|byte
operator|)
literal|1
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|separateList
parameter_list|()
block|{
name|BinarySortableSerDe
operator|.
name|writeByte
argument_list|(
name|output
argument_list|,
operator|(
name|byte
operator|)
literal|1
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|finishList
parameter_list|()
block|{
name|level
operator|--
expr_stmt|;
comment|// and \0 to terminate
name|BinarySortableSerDe
operator|.
name|writeByte
argument_list|(
name|output
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * Map    */
annotation|@
name|Override
specifier|public
name|void
name|beginMap
parameter_list|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|map
parameter_list|)
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|level
operator|++
expr_stmt|;
if|if
condition|(
operator|!
name|map
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|BinarySortableSerDe
operator|.
name|writeByte
argument_list|(
name|output
argument_list|,
operator|(
name|byte
operator|)
literal|1
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|separateKey
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|separateKeyValuePair
parameter_list|()
block|{
name|BinarySortableSerDe
operator|.
name|writeByte
argument_list|(
name|output
argument_list|,
operator|(
name|byte
operator|)
literal|1
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|finishMap
parameter_list|()
block|{
name|level
operator|--
expr_stmt|;
comment|// and \0 to terminate
name|BinarySortableSerDe
operator|.
name|writeByte
argument_list|(
name|output
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * Struct    */
annotation|@
name|Override
specifier|public
name|void
name|beginStruct
parameter_list|(
name|List
name|fieldValues
parameter_list|)
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|level
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|separateStruct
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|finishStruct
parameter_list|()
block|{
name|level
operator|--
expr_stmt|;
block|}
comment|/*    * Union    */
annotation|@
name|Override
specifier|public
name|void
name|beginUnion
parameter_list|(
name|int
name|tag
parameter_list|)
throws|throws
name|IOException
block|{
name|beginElement
argument_list|()
expr_stmt|;
name|BinarySortableSerDe
operator|.
name|writeByte
argument_list|(
name|output
argument_list|,
operator|(
name|byte
operator|)
name|tag
argument_list|,
name|columnSortOrderIsDesc
index|[
name|index
index|]
argument_list|)
expr_stmt|;
name|level
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|finishUnion
parameter_list|()
block|{
name|level
operator|--
expr_stmt|;
block|}
block|}
end_class

end_unit

