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
name|serde2
operator|.
name|lazybinary
package|;
end_package

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
name|conf
operator|.
name|Configuration
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
name|serde
operator|.
name|Constants
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
name|Deserializer
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
name|SerDe
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
name|SerDeException
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
name|lazy
operator|.
name|ByteArrayRef
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
name|objectinspector
operator|.
name|ListObjectInspector
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
name|objectinspector
operator|.
name|MapObjectInspector
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
name|objectinspector
operator|.
name|ObjectInspector
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
name|objectinspector
operator|.
name|PrimitiveObjectInspector
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
name|objectinspector
operator|.
name|StructField
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
name|objectinspector
operator|.
name|StructObjectInspector
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
name|objectinspector
operator|.
name|ObjectInspector
operator|.
name|Category
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
name|objectinspector
operator|.
name|primitive
operator|.
name|BooleanObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|ByteObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|DoubleObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|FloatObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|IntObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|LongObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|ShortObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|StringObjectInspector
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
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|TypeInfoFactory
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
name|typeinfo
operator|.
name|TypeInfoUtils
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * The LazyBinarySerDe class combines the lazy property of   * LazySimpleSerDe class and the binary property of BinarySortable  * class. Lazy means a field is not deserialized until required.   * Binary means a field is serialized in binary compact format.    */
end_comment

begin_class
specifier|public
class|class
name|LazyBinarySerDe
implements|implements
name|SerDe
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
name|LazyBinarySerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|LazyBinarySerDe
parameter_list|()
throws|throws
name|SerDeException
block|{       }
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
decl_stmt|;
name|TypeInfo
name|rowTypeInfo
decl_stmt|;
name|ObjectInspector
name|cachedObjectInspector
decl_stmt|;
comment|// The object for storing row data
name|LazyBinaryStruct
name|cachedLazyBinaryStruct
decl_stmt|;
comment|/**    * Initialize the SerDe with configuration and table information    * @see SerDe#initialize(Configuration, Properties)    */
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Get column names and types
name|String
name|columnNameProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|LIST_COLUMNS
argument_list|)
decl_stmt|;
name|String
name|columnTypeProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|LIST_COLUMN_TYPES
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnNameProperty
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|columnNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|columnNames
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|columnNameProperty
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|columnTypeProperty
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|columnTypes
operator|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|columnTypes
operator|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|columnTypeProperty
argument_list|)
expr_stmt|;
block|}
assert|assert
operator|(
name|columnNames
operator|.
name|size
argument_list|()
operator|==
name|columnTypes
operator|.
name|size
argument_list|()
operator|)
assert|;
comment|// Create row related objects
name|rowTypeInfo
operator|=
name|TypeInfoFactory
operator|.
name|getStructTypeInfo
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|)
expr_stmt|;
comment|// Create the object inspector and the lazy binary struct object
name|cachedObjectInspector
operator|=
name|LazyBinaryUtils
operator|.
name|getLazyBinaryObjectInspectorFromTypeInfo
argument_list|(
name|rowTypeInfo
argument_list|)
expr_stmt|;
name|cachedLazyBinaryStruct
operator|=
operator|(
name|LazyBinaryStruct
operator|)
name|LazyBinaryFactory
operator|.
name|createLazyBinaryObject
argument_list|(
name|cachedObjectInspector
argument_list|)
expr_stmt|;
comment|// output debug info
name|LOG
operator|.
name|debug
argument_list|(
literal|"LazyBinarySerDe initialized with: columnNames="
operator|+
name|columnNames
operator|+
literal|" columnTypes="
operator|+
name|columnTypes
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the ObjectInspector for the row.    * @see Deserializer#getObjectInspector()    */
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
throws|throws
name|SerDeException
block|{
return|return
name|cachedObjectInspector
return|;
block|}
comment|/**    * Returns the Writable Class after serialization.    * @see Serializer#getSerializedClass()    */
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|getSerializedClass
parameter_list|()
block|{
return|return
name|BytesWritable
operator|.
name|class
return|;
block|}
comment|// The wrapper for byte array
name|ByteArrayRef
name|byteArrayRef
decl_stmt|;
comment|/**    * Deserialize a table record to a lazybinary struct.    * @see Deserializer#deserialize(Writable)    */
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|field
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
name|byteArrayRef
operator|==
literal|null
condition|)
block|{
name|byteArrayRef
operator|=
operator|new
name|ByteArrayRef
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|instanceof
name|BytesWritable
condition|)
block|{
name|BytesWritable
name|b
init|=
operator|(
name|BytesWritable
operator|)
name|field
decl_stmt|;
if|if
condition|(
name|b
operator|.
name|getSize
argument_list|()
operator|==
literal|0
condition|)
return|return
literal|null
return|;
comment|// For backward-compatibility with hadoop 0.17
name|byteArrayRef
operator|.
name|setData
argument_list|(
name|b
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|cachedLazyBinaryStruct
operator|.
name|init
argument_list|(
name|byteArrayRef
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|field
operator|instanceof
name|Text
condition|)
block|{
name|Text
name|t
init|=
operator|(
name|Text
operator|)
name|field
decl_stmt|;
if|if
condition|(
name|t
operator|.
name|getLength
argument_list|()
operator|==
literal|0
condition|)
return|return
literal|null
return|;
name|byteArrayRef
operator|.
name|setData
argument_list|(
name|t
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|cachedLazyBinaryStruct
operator|.
name|init
argument_list|(
name|byteArrayRef
argument_list|,
literal|0
argument_list|,
name|t
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|": expects either BytesWritable or Text object!"
argument_list|)
throw|;
block|}
return|return
name|cachedLazyBinaryStruct
return|;
block|}
comment|/**    * The reusable output buffer and serialize byte buffer.    */
name|BytesWritable
name|serializeBytesWritable
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
name|ByteStream
operator|.
name|Output
name|serializeByteStream
init|=
operator|new
name|ByteStream
operator|.
name|Output
argument_list|()
decl_stmt|;
comment|/**    * Serialize an object to a byte buffer in a binary compact way.    * @see Serializer#serialize(Object, ObjectInspector)    */
annotation|@
name|Override
specifier|public
name|Writable
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// make sure it is a struct record
if|if
condition|(
name|objInspector
operator|.
name|getCategory
argument_list|()
operator|!=
name|Category
operator|.
name|STRUCT
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" can only serialize struct types, but we got: "
operator|+
name|objInspector
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
name|serializeByteStream
operator|.
name|reset
argument_list|()
expr_stmt|;
comment|// serialize the row as a struct
name|serializeStruct
argument_list|(
name|serializeByteStream
argument_list|,
name|obj
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|objInspector
argument_list|)
expr_stmt|;
comment|// return the serialized bytes
name|serializeBytesWritable
operator|.
name|set
argument_list|(
name|serializeByteStream
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|serializeByteStream
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|serializeBytesWritable
return|;
block|}
name|boolean
name|nullMapKey
init|=
literal|false
decl_stmt|;
comment|/**    * Serialize a struct object without writing the byte size.     * This function is shared by both row serialization and    * struct serialization.    *     * @param byteStream      the byte stream storing the serialization data    * @param obj             the struct object to serialize    * @param objInspector    the struct object inspector    */
specifier|private
name|void
name|serializeStruct
parameter_list|(
name|Output
name|byteStream
parameter_list|,
name|Object
name|obj
parameter_list|,
name|StructObjectInspector
name|soi
parameter_list|)
block|{
comment|// do nothing for null struct
if|if
condition|(
literal|null
operator|==
name|obj
condition|)
return|return;
comment|/*       * Interleave serializing one null byte and 8 struct fields      * in each round, in order to support data deserialization      * with different table schemas      */
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|int
name|size
init|=
name|fields
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|lasti
init|=
literal|0
decl_stmt|;
name|byte
name|nullByte
init|=
literal|0
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
name|size
condition|;
name|i
operator|++
control|)
block|{
comment|// set bit to 1 if a field is not null
if|if
condition|(
literal|null
operator|!=
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|obj
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
name|nullByte
operator||=
literal|1
operator|<<
operator|(
name|i
operator|%
literal|8
operator|)
expr_stmt|;
block|}
comment|// write the null byte every eight elements or
comment|// if this is the last element and serialize the
comment|// corresponding 8 struct fields at the same time
if|if
condition|(
literal|7
operator|==
name|i
operator|%
literal|8
operator|||
name|i
operator|==
name|size
operator|-
literal|1
condition|)
block|{
name|serializeByteStream
operator|.
name|write
argument_list|(
name|nullByte
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
name|lasti
init|;
name|j
operator|<=
name|i
condition|;
name|j
operator|++
control|)
block|{
name|serialize
argument_list|(
name|serializeByteStream
argument_list|,
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|obj
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|lasti
operator|=
name|i
operator|+
literal|1
expr_stmt|;
name|nullByte
operator|=
literal|0
expr_stmt|;
block|}
block|}
block|}
comment|/**    * A recursive function that serialize an object to a byte buffer     * based on its object inspector.    * @param byteStream      the byte stream storing the serialization data    * @param obj             the object to serialize    * @param objInspector    the object inspector    * @see LazyBinaryUtils#checkObjectByteInfo(ObjectInspector, byte[], int, LazyBinaryUtils.RecordInfo) for    *      how the byte sizes of different object are decoded.      */
specifier|private
name|void
name|serialize
parameter_list|(
name|Output
name|byteStream
parameter_list|,
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|)
block|{
comment|// do nothing for null object
if|if
condition|(
literal|null
operator|==
name|obj
condition|)
return|return;
switch|switch
condition|(
name|objInspector
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
block|{
name|PrimitiveObjectInspector
name|poi
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|objInspector
decl_stmt|;
switch|switch
condition|(
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|VOID
case|:
block|{
return|return;
block|}
case|case
name|BOOLEAN
case|:
block|{
name|BooleanObjectInspector
name|boi
init|=
operator|(
name|BooleanObjectInspector
operator|)
name|poi
decl_stmt|;
name|boolean
name|v
init|=
operator|(
operator|(
name|BooleanObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|obj
argument_list|)
decl_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
condition|?
literal|1
else|:
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
case|case
name|BYTE
case|:
block|{
name|ByteObjectInspector
name|boi
init|=
operator|(
name|ByteObjectInspector
operator|)
name|poi
decl_stmt|;
name|byte
name|v
init|=
name|boi
operator|.
name|get
argument_list|(
name|obj
argument_list|)
decl_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
name|v
argument_list|)
expr_stmt|;
return|return;
block|}
case|case
name|SHORT
case|:
block|{
name|ShortObjectInspector
name|spoi
init|=
operator|(
name|ShortObjectInspector
operator|)
name|poi
decl_stmt|;
name|short
name|v
init|=
name|spoi
operator|.
name|get
argument_list|(
name|obj
argument_list|)
decl_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
case|case
name|INT
case|:
block|{
name|IntObjectInspector
name|ioi
init|=
operator|(
name|IntObjectInspector
operator|)
name|poi
decl_stmt|;
name|int
name|v
init|=
name|ioi
operator|.
name|get
argument_list|(
name|obj
argument_list|)
decl_stmt|;
name|LazyBinaryUtils
operator|.
name|writeVInt
argument_list|(
name|byteStream
argument_list|,
name|v
argument_list|)
expr_stmt|;
return|return;
block|}
case|case
name|LONG
case|:
block|{
name|LongObjectInspector
name|loi
init|=
operator|(
name|LongObjectInspector
operator|)
name|poi
decl_stmt|;
name|long
name|v
init|=
name|loi
operator|.
name|get
argument_list|(
name|obj
argument_list|)
decl_stmt|;
name|LazyBinaryUtils
operator|.
name|writeVLong
argument_list|(
name|byteStream
argument_list|,
name|v
argument_list|)
expr_stmt|;
return|return;
block|}
case|case
name|FLOAT
case|:
block|{
name|FloatObjectInspector
name|foi
init|=
operator|(
name|FloatObjectInspector
operator|)
name|poi
decl_stmt|;
name|int
name|v
init|=
name|Float
operator|.
name|floatToIntBits
argument_list|(
name|foi
operator|.
name|get
argument_list|(
name|obj
argument_list|)
argument_list|)
decl_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|24
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|16
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
case|case
name|DOUBLE
case|:
block|{
name|DoubleObjectInspector
name|doi
init|=
operator|(
name|DoubleObjectInspector
operator|)
name|poi
decl_stmt|;
name|long
name|v
init|=
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|doi
operator|.
name|get
argument_list|(
name|obj
argument_list|)
argument_list|)
decl_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|56
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|48
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|40
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|32
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|24
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|16
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
operator|>>
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|v
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
case|case
name|STRING
case|:
block|{
name|StringObjectInspector
name|soi
init|=
operator|(
name|StringObjectInspector
operator|)
name|poi
decl_stmt|;
name|Text
name|t
init|=
name|soi
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|obj
argument_list|)
decl_stmt|;
comment|/* write byte size of the string which is a vint */
name|int
name|length
init|=
name|t
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|LazyBinaryUtils
operator|.
name|writeVInt
argument_list|(
name|byteStream
argument_list|,
name|length
argument_list|)
expr_stmt|;
comment|/* write string itself */
name|byte
index|[]
name|data
init|=
name|t
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return;
block|}
default|default:
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unrecognized type: "
operator|+
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
case|case
name|LIST
case|:
block|{
name|ListObjectInspector
name|loi
init|=
operator|(
name|ListObjectInspector
operator|)
name|objInspector
decl_stmt|;
name|ObjectInspector
name|eoi
init|=
name|loi
operator|.
name|getListElementObjectInspector
argument_list|()
decl_stmt|;
comment|// 1/ reserve spaces for the byte size of the list
comment|//    which is a integer and takes four bytes
name|int
name|byteSizeStart
init|=
name|byteStream
operator|.
name|getCount
argument_list|()
decl_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|int
name|listStart
init|=
name|byteStream
operator|.
name|getCount
argument_list|()
decl_stmt|;
comment|// 2/ write the size of the list as a VInt
name|int
name|size
init|=
name|loi
operator|.
name|getListLength
argument_list|(
name|obj
argument_list|)
decl_stmt|;
name|LazyBinaryUtils
operator|.
name|writeVInt
argument_list|(
name|byteStream
argument_list|,
name|size
argument_list|)
expr_stmt|;
comment|// 3/ write the null bytes
name|byte
name|nullByte
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|eid
init|=
literal|0
init|;
name|eid
operator|<
name|size
condition|;
name|eid
operator|++
control|)
block|{
comment|// set the bit to 1 if an element is not null
if|if
condition|(
literal|null
operator|!=
name|loi
operator|.
name|getListElement
argument_list|(
name|obj
argument_list|,
name|eid
argument_list|)
condition|)
block|{
name|nullByte
operator||=
literal|1
operator|<<
operator|(
name|eid
operator|%
literal|8
operator|)
expr_stmt|;
block|}
comment|// store the byte every eight elements or
comment|// if this is the last element
if|if
condition|(
literal|7
operator|==
name|eid
operator|%
literal|8
operator|||
name|eid
operator|==
name|size
operator|-
literal|1
condition|)
block|{
name|byteStream
operator|.
name|write
argument_list|(
name|nullByte
argument_list|)
expr_stmt|;
name|nullByte
operator|=
literal|0
expr_stmt|;
block|}
block|}
comment|// 4/ write element by element from the list
for|for
control|(
name|int
name|eid
init|=
literal|0
init|;
name|eid
operator|<
name|size
condition|;
name|eid
operator|++
control|)
block|{
name|serialize
argument_list|(
name|byteStream
argument_list|,
name|loi
operator|.
name|getListElement
argument_list|(
name|obj
argument_list|,
name|eid
argument_list|)
argument_list|,
name|eoi
argument_list|)
expr_stmt|;
block|}
comment|// 5/ update the list byte size
name|int
name|listEnd
init|=
name|byteStream
operator|.
name|getCount
argument_list|()
decl_stmt|;
name|int
name|listSize
init|=
name|listEnd
operator|-
name|listStart
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|byteStream
operator|.
name|getData
argument_list|()
decl_stmt|;
name|bytes
index|[
name|byteSizeStart
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|listSize
operator|>>
literal|24
argument_list|)
expr_stmt|;
name|bytes
index|[
name|byteSizeStart
operator|+
literal|1
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|listSize
operator|>>
literal|16
argument_list|)
expr_stmt|;
name|bytes
index|[
name|byteSizeStart
operator|+
literal|2
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|listSize
operator|>>
literal|8
argument_list|)
expr_stmt|;
name|bytes
index|[
name|byteSizeStart
operator|+
literal|3
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|listSize
argument_list|)
expr_stmt|;
return|return;
block|}
case|case
name|MAP
case|:
block|{
name|MapObjectInspector
name|moi
init|=
operator|(
name|MapObjectInspector
operator|)
name|objInspector
decl_stmt|;
name|ObjectInspector
name|koi
init|=
name|moi
operator|.
name|getMapKeyObjectInspector
argument_list|()
decl_stmt|;
name|ObjectInspector
name|voi
init|=
name|moi
operator|.
name|getMapValueObjectInspector
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|map
init|=
name|moi
operator|.
name|getMap
argument_list|(
name|obj
argument_list|)
decl_stmt|;
comment|// 1/ reserve spaces for the byte size of the map
comment|//    which is a integer and takes four bytes
name|int
name|byteSizeStart
init|=
name|byteStream
operator|.
name|getCount
argument_list|()
decl_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|int
name|mapStart
init|=
name|byteStream
operator|.
name|getCount
argument_list|()
decl_stmt|;
comment|// 2/ write the size of the map which is a VInt
name|int
name|size
init|=
name|map
operator|.
name|size
argument_list|()
decl_stmt|;
name|LazyBinaryUtils
operator|.
name|writeVInt
argument_list|(
name|byteStream
argument_list|,
name|size
argument_list|)
expr_stmt|;
comment|// 3/ write the null bytes
name|int
name|b
init|=
literal|0
decl_stmt|;
name|byte
name|nullByte
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|entry
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// set the bit to 1 if a key is not null
if|if
condition|(
literal|null
operator|!=
name|entry
operator|.
name|getKey
argument_list|()
condition|)
block|{
name|nullByte
operator||=
literal|1
operator|<<
operator|(
name|b
operator|%
literal|8
operator|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|nullMapKey
condition|)
block|{
name|nullMapKey
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Null map key encountered! Ignoring similar problems."
argument_list|)
expr_stmt|;
block|}
name|b
operator|++
expr_stmt|;
comment|// set the bit to 1 if a value is not null
if|if
condition|(
literal|null
operator|!=
name|entry
operator|.
name|getValue
argument_list|()
condition|)
block|{
name|nullByte
operator||=
literal|1
operator|<<
operator|(
name|b
operator|%
literal|8
operator|)
expr_stmt|;
block|}
name|b
operator|++
expr_stmt|;
comment|// write the byte to stream every 4 key-value pairs
comment|// or if this is the last key-value pair
if|if
condition|(
literal|0
operator|==
name|b
operator|%
literal|8
operator|||
name|b
operator|==
name|size
operator|*
literal|2
condition|)
block|{
name|byteStream
operator|.
name|write
argument_list|(
name|nullByte
argument_list|)
expr_stmt|;
name|nullByte
operator|=
literal|0
expr_stmt|;
block|}
block|}
comment|// 4/ write key-value pairs one by one
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|entry
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|serialize
argument_list|(
name|byteStream
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|koi
argument_list|)
expr_stmt|;
name|serialize
argument_list|(
name|byteStream
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|voi
argument_list|)
expr_stmt|;
block|}
comment|// 5/ update the byte size of the map
name|int
name|mapEnd
init|=
name|byteStream
operator|.
name|getCount
argument_list|()
decl_stmt|;
name|int
name|mapSize
init|=
name|mapEnd
operator|-
name|mapStart
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|byteStream
operator|.
name|getData
argument_list|()
decl_stmt|;
name|bytes
index|[
name|byteSizeStart
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|mapSize
operator|>>
literal|24
argument_list|)
expr_stmt|;
name|bytes
index|[
name|byteSizeStart
operator|+
literal|1
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|mapSize
operator|>>
literal|16
argument_list|)
expr_stmt|;
name|bytes
index|[
name|byteSizeStart
operator|+
literal|2
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|mapSize
operator|>>
literal|8
argument_list|)
expr_stmt|;
name|bytes
index|[
name|byteSizeStart
operator|+
literal|3
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|mapSize
argument_list|)
expr_stmt|;
return|return;
block|}
case|case
name|STRUCT
case|:
block|{
comment|// 1/ reserve spaces for the byte size of the struct
comment|//    which is a integer and takes four bytes
name|int
name|byteSizeStart
init|=
name|byteStream
operator|.
name|getCount
argument_list|()
decl_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|byteStream
operator|.
name|write
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|int
name|structStart
init|=
name|byteStream
operator|.
name|getCount
argument_list|()
decl_stmt|;
comment|// 2/ serialize the struct
name|serializeStruct
argument_list|(
name|byteStream
argument_list|,
name|obj
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|objInspector
argument_list|)
expr_stmt|;
comment|// 3/ update the byte size of the struct
name|int
name|structEnd
init|=
name|byteStream
operator|.
name|getCount
argument_list|()
decl_stmt|;
name|int
name|structSize
init|=
name|structEnd
operator|-
name|structStart
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|byteStream
operator|.
name|getData
argument_list|()
decl_stmt|;
name|bytes
index|[
name|byteSizeStart
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|structSize
operator|>>
literal|24
argument_list|)
expr_stmt|;
name|bytes
index|[
name|byteSizeStart
operator|+
literal|1
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|structSize
operator|>>
literal|16
argument_list|)
expr_stmt|;
name|bytes
index|[
name|byteSizeStart
operator|+
literal|2
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|structSize
operator|>>
literal|8
argument_list|)
expr_stmt|;
name|bytes
index|[
name|byteSizeStart
operator|+
literal|3
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|structSize
argument_list|)
expr_stmt|;
return|return;
block|}
default|default:
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unrecognized type: "
operator|+
name|objInspector
operator|.
name|getCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

