begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * License); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an AS IS BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|contrib
operator|.
name|serde2
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
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|serdeConstants
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
name|*
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
name|lazy
operator|.
name|LazyFactory
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
name|LazySimpleSerDe
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
name|LazyStruct
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
name|LazyUtils
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
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
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
name|primitive
operator|.
name|BinaryObjectInspector
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
comment|/**  * This SerDe allows user to use multiple characters as the field delimiter for a table.  * To use this SerDe, user has to specify field.delim in SERDEPROPERTIES.  * If the table contains a column which is a collection or map, user can optionally  * specify collection.delim or mapkey.delim as well.  * Currently field.delim can be multiple character while collection.delim  * and mapkey.delim should be just single character.  */
end_comment

begin_class
annotation|@
name|SerDeSpec
argument_list|(
name|schemaProps
operator|=
block|{
name|serdeConstants
operator|.
name|LIST_COLUMNS
block|,
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
block|,
name|serdeConstants
operator|.
name|FIELD_DELIM
block|,
name|serdeConstants
operator|.
name|COLLECTION_DELIM
block|,
name|serdeConstants
operator|.
name|MAPKEY_DELIM
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_NULL_FORMAT
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_LAST_COLUMN_TAKES_REST
block|,
name|serdeConstants
operator|.
name|ESCAPE_CHAR
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_ENCODING
block|,
name|LazySimpleSerDe
operator|.
name|SERIALIZATION_EXTEND_NESTING_LEVELS
block|}
argument_list|)
specifier|public
class|class
name|MultiDelimitSerDe
extends|extends
name|AbstractSerDe
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
name|MultiDelimitSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|DEFAULT_SEPARATORS
init|=
block|{
operator|(
name|byte
operator|)
literal|1
block|,
operator|(
name|byte
operator|)
literal|2
block|,
operator|(
name|byte
operator|)
literal|3
block|}
decl_stmt|;
comment|// Due to HIVE-6404, define our own constant
specifier|private
specifier|static
specifier|final
name|String
name|COLLECTION_DELIM
init|=
literal|"collection.delim"
decl_stmt|;
specifier|private
name|int
name|numColumns
decl_stmt|;
specifier|private
name|String
name|fieldDelimited
decl_stmt|;
comment|// we don't support using multiple chars as delimiters within complex types
comment|// collection separator
specifier|private
name|byte
name|collSep
decl_stmt|;
comment|// map key separator
specifier|private
name|byte
name|keySep
decl_stmt|;
comment|// The object for storing row data
specifier|private
name|LazyStruct
name|cachedLazyStruct
decl_stmt|;
comment|//the lazy struct object inspector
specifier|private
name|ObjectInspector
name|cachedObjectInspector
decl_stmt|;
comment|// The wrapper for byte array
specifier|private
name|ByteArrayRef
name|byteArrayRef
decl_stmt|;
specifier|private
name|LazySimpleSerDe
operator|.
name|SerDeParameters
name|serdeParams
init|=
literal|null
decl_stmt|;
comment|// The output stream of serialized objects
specifier|private
specifier|final
name|ByteStream
operator|.
name|Output
name|serializeStream
init|=
operator|new
name|ByteStream
operator|.
name|Output
argument_list|()
decl_stmt|;
comment|// The Writable to return in serialize
specifier|private
specifier|final
name|Text
name|serializeCache
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
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
comment|// get the SerDe parameters
name|serdeParams
operator|=
name|LazySimpleSerDe
operator|.
name|initSerdeParams
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|fieldDelimited
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|FIELD_DELIM
argument_list|)
expr_stmt|;
if|if
condition|(
name|fieldDelimited
operator|==
literal|null
operator|||
name|fieldDelimited
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"This table does not have serde property \"field.delim\"!"
argument_list|)
throw|;
block|}
comment|// get the collection separator and map key separator
comment|// TODO: use serdeConstants.COLLECTION_DELIM when the typo is fixed
name|collSep
operator|=
name|LazySimpleSerDe
operator|.
name|getByte
argument_list|(
name|tbl
operator|.
name|getProperty
argument_list|(
name|COLLECTION_DELIM
argument_list|)
argument_list|,
name|DEFAULT_SEPARATORS
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|keySep
operator|=
name|LazySimpleSerDe
operator|.
name|getByte
argument_list|(
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|MAPKEY_DELIM
argument_list|)
argument_list|,
name|DEFAULT_SEPARATORS
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|serdeParams
operator|.
name|getSeparators
argument_list|()
index|[
literal|1
index|]
operator|=
name|collSep
expr_stmt|;
name|serdeParams
operator|.
name|getSeparators
argument_list|()
index|[
literal|2
index|]
operator|=
name|keySep
expr_stmt|;
comment|// Create the ObjectInspectors for the fields
name|cachedObjectInspector
operator|=
name|LazyFactory
operator|.
name|createLazyStructInspector
argument_list|(
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getColumnTypes
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getSeparators
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getNullSequence
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|isLastColumnTakesRest
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|isEscaped
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getEscapeChar
argument_list|()
argument_list|)
expr_stmt|;
name|cachedLazyStruct
operator|=
operator|(
name|LazyStruct
operator|)
name|LazyFactory
operator|.
name|createLazyObject
argument_list|(
name|cachedObjectInspector
argument_list|)
expr_stmt|;
assert|assert
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|serdeParams
operator|.
name|getColumnTypes
argument_list|()
operator|.
name|size
argument_list|()
assert|;
name|numColumns
operator|=
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
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
name|Text
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|blob
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
comment|// we use the default field delimiter('\1') to replace the multiple-char field delimiter
comment|// but we cannot use it to parse the row since column data can contain '\1' as well
name|String
name|rowStr
decl_stmt|;
if|if
condition|(
name|blob
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
name|blob
decl_stmt|;
name|rowStr
operator|=
operator|new
name|String
argument_list|(
name|b
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|blob
operator|instanceof
name|Text
condition|)
block|{
name|Text
name|rowText
init|=
operator|(
name|Text
operator|)
name|blob
decl_stmt|;
name|rowStr
operator|=
name|rowText
operator|.
name|toString
argument_list|()
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
operator|+
literal|": expects either BytesWritable or Text object!"
argument_list|)
throw|;
block|}
name|byteArrayRef
operator|.
name|setData
argument_list|(
name|rowStr
operator|.
name|replaceAll
argument_list|(
name|Pattern
operator|.
name|quote
argument_list|(
name|fieldDelimited
argument_list|)
argument_list|,
literal|"\1"
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|cachedLazyStruct
operator|.
name|init
argument_list|(
name|byteArrayRef
argument_list|,
literal|0
argument_list|,
name|byteArrayRef
operator|.
name|getData
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// use the multi-char delimiter to parse the lazy struct
name|cachedLazyStruct
operator|.
name|parseMultiDelimit
argument_list|(
name|rowStr
operator|.
name|getBytes
argument_list|()
argument_list|,
name|fieldDelimited
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|cachedLazyStruct
return|;
block|}
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
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|objInspector
decl_stmt|;
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
name|List
argument_list|<
name|Object
argument_list|>
name|list
init|=
name|soi
operator|.
name|getStructFieldsDataAsList
argument_list|(
name|obj
argument_list|)
decl_stmt|;
if|if
condition|(
name|fields
operator|.
name|size
argument_list|()
operator|!=
name|numColumns
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Cannot serialize the object because there are "
operator|+
name|fields
operator|.
name|size
argument_list|()
operator|+
literal|" fields but the table has "
operator|+
name|numColumns
operator|+
literal|" columns."
argument_list|)
throw|;
block|}
name|serializeStream
operator|.
name|reset
argument_list|()
expr_stmt|;
comment|// Get all data out.
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|numColumns
condition|;
name|c
operator|++
control|)
block|{
comment|//write the delimiter to the stream, which means we don't need output.format.string anymore
if|if
condition|(
name|c
operator|>
literal|0
condition|)
block|{
name|serializeStream
operator|.
name|write
argument_list|(
name|fieldDelimited
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|fieldDelimited
operator|.
name|getBytes
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
name|Object
name|field
init|=
name|list
operator|==
literal|null
condition|?
literal|null
else|:
name|list
operator|.
name|get
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|ObjectInspector
name|fieldOI
init|=
name|fields
operator|.
name|get
argument_list|(
name|c
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
try|try
block|{
name|serializeNoEncode
argument_list|(
name|serializeStream
argument_list|,
name|field
argument_list|,
name|fieldOI
argument_list|,
name|serdeParams
operator|.
name|getSeparators
argument_list|()
argument_list|,
literal|1
argument_list|,
name|serdeParams
operator|.
name|getNullSequence
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|isEscaped
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getEscapeChar
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getNeedsEscape
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
name|serializeCache
operator|.
name|set
argument_list|(
name|serializeStream
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|serializeStream
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|serializeCache
return|;
block|}
comment|// This is basically the same as LazySimpleSerDe.serialize. Except that we don't use
comment|// Base64 to encode binary data because we're using printable string as delimiter.
comment|// Consider such a row "strAQ==\1", str is a string, AQ== is the delimiter and \1
comment|// is the binary data.
specifier|private
specifier|static
name|void
name|serializeNoEncode
parameter_list|(
name|ByteStream
operator|.
name|Output
name|out
parameter_list|,
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|,
name|byte
index|[]
name|separators
parameter_list|,
name|int
name|level
parameter_list|,
name|Text
name|nullSequence
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|,
name|boolean
index|[]
name|needsEscape
parameter_list|)
throws|throws
name|IOException
throws|,
name|SerDeException
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|nullSequence
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|nullSequence
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|char
name|separator
decl_stmt|;
name|List
argument_list|<
name|?
argument_list|>
name|list
decl_stmt|;
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
name|PrimitiveObjectInspector
name|oi
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|objInspector
decl_stmt|;
if|if
condition|(
name|oi
operator|.
name|getPrimitiveCategory
argument_list|()
operator|==
name|PrimitiveCategory
operator|.
name|BINARY
condition|)
block|{
name|BytesWritable
name|bw
init|=
operator|(
operator|(
name|BinaryObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|obj
argument_list|)
decl_stmt|;
name|byte
index|[]
name|toWrite
init|=
operator|new
name|byte
index|[
name|bw
operator|.
name|getLength
argument_list|()
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|bw
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|toWrite
argument_list|,
literal|0
argument_list|,
name|bw
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|toWrite
argument_list|,
literal|0
argument_list|,
name|toWrite
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LazyUtils
operator|.
name|writePrimitiveUTF8
argument_list|(
name|out
argument_list|,
name|obj
argument_list|,
name|oi
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|needsEscape
argument_list|)
expr_stmt|;
block|}
return|return;
case|case
name|LIST
case|:
name|separator
operator|=
operator|(
name|char
operator|)
name|separators
index|[
name|level
index|]
expr_stmt|;
name|ListObjectInspector
name|loi
init|=
operator|(
name|ListObjectInspector
operator|)
name|objInspector
decl_stmt|;
name|list
operator|=
name|loi
operator|.
name|getList
argument_list|(
name|obj
argument_list|)
expr_stmt|;
name|ObjectInspector
name|eoi
init|=
name|loi
operator|.
name|getListElementObjectInspector
argument_list|()
decl_stmt|;
if|if
condition|(
name|list
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|nullSequence
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|nullSequence
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
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
name|list
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|separator
argument_list|)
expr_stmt|;
block|}
name|serializeNoEncode
argument_list|(
name|out
argument_list|,
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|eoi
argument_list|,
name|separators
argument_list|,
name|level
operator|+
literal|1
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|needsEscape
argument_list|)
expr_stmt|;
block|}
block|}
return|return;
case|case
name|MAP
case|:
name|separator
operator|=
operator|(
name|char
operator|)
name|separators
index|[
name|level
index|]
expr_stmt|;
name|char
name|keyValueSeparator
init|=
operator|(
name|char
operator|)
name|separators
index|[
name|level
operator|+
literal|1
index|]
decl_stmt|;
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
if|if
condition|(
name|map
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|nullSequence
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|nullSequence
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|boolean
name|first
init|=
literal|true
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
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|write
argument_list|(
name|separator
argument_list|)
expr_stmt|;
block|}
name|serializeNoEncode
argument_list|(
name|out
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|koi
argument_list|,
name|separators
argument_list|,
name|level
operator|+
literal|2
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|needsEscape
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|keyValueSeparator
argument_list|)
expr_stmt|;
name|serializeNoEncode
argument_list|(
name|out
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|voi
argument_list|,
name|separators
argument_list|,
name|level
operator|+
literal|2
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|needsEscape
argument_list|)
expr_stmt|;
block|}
block|}
return|return;
case|case
name|STRUCT
case|:
name|separator
operator|=
operator|(
name|char
operator|)
name|separators
index|[
name|level
index|]
expr_stmt|;
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|objInspector
decl_stmt|;
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
name|list
operator|=
name|soi
operator|.
name|getStructFieldsDataAsList
argument_list|(
name|obj
argument_list|)
expr_stmt|;
if|if
condition|(
name|list
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|nullSequence
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|nullSequence
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
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
name|list
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|separator
argument_list|)
expr_stmt|;
block|}
name|serializeNoEncode
argument_list|(
name|out
argument_list|,
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|,
name|separators
argument_list|,
name|level
operator|+
literal|1
argument_list|,
name|nullSequence
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|needsEscape
argument_list|)
expr_stmt|;
block|}
block|}
return|return;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown category type: "
operator|+
name|objInspector
operator|.
name|getCategory
argument_list|()
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
comment|// no support for statistics
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

