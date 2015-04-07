begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|avro
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|rmi
operator|.
name|server
operator|.
name|UID
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
operator|.
name|Type
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|generic
operator|.
name|GenericData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|generic
operator|.
name|GenericData
operator|.
name|Fixed
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|generic
operator|.
name|GenericDatumReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|generic
operator|.
name|GenericDatumWriter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|generic
operator|.
name|GenericRecord
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|io
operator|.
name|BinaryDecoder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|io
operator|.
name|BinaryEncoder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|io
operator|.
name|DecoderFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|io
operator|.
name|EncoderFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|UnresolvedUnionException
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
name|serde2
operator|.
name|io
operator|.
name|DateWritable
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
name|StandardUnionObjectInspector
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
name|JavaHiveDecimalObjectInspector
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
name|PrimitiveObjectInspectorFactory
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
name|DecimalTypeInfo
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
name|ListTypeInfo
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
name|MapTypeInfo
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
name|PrimitiveTypeInfo
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
name|StructTypeInfo
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
name|UnionTypeInfo
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

begin_class
class|class
name|AvroDeserializer
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
name|AvroDeserializer
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Set of already seen and valid record readers IDs which doesn't need re-encoding    */
specifier|private
specifier|final
name|HashSet
argument_list|<
name|UID
argument_list|>
name|noEncodingNeeded
init|=
operator|new
name|HashSet
argument_list|<
name|UID
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Map of record reader ID and the associated re-encoder. It contains only the record readers    *  that record needs to be re-encoded.    */
specifier|private
specifier|final
name|HashMap
argument_list|<
name|UID
argument_list|,
name|SchemaReEncoder
argument_list|>
name|reEncoderCache
init|=
operator|new
name|HashMap
argument_list|<
name|UID
argument_list|,
name|SchemaReEncoder
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Flag to print the re-encoding warning message only once. Avoid excessive logging for each    * record encoding.    */
specifier|private
specifier|static
name|boolean
name|warnedOnce
init|=
literal|false
decl_stmt|;
comment|/**    * When encountering a record with an older schema than the one we're trying    * to read, it is necessary to re-encode with a reader against the newer schema.    * Because Hive doesn't provide a way to pass extra information to the    * inputformat, we're unable to provide the newer schema when we have it and it    * would be most useful - when the inputformat is reading the file.    *    * This is a slow process, so we try to cache as many of the objects as possible.    */
specifier|static
class|class
name|SchemaReEncoder
block|{
specifier|private
specifier|final
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|GenericDatumWriter
argument_list|<
name|GenericRecord
argument_list|>
name|gdw
init|=
operator|new
name|GenericDatumWriter
argument_list|<
name|GenericRecord
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|BinaryDecoder
name|binaryDecoder
init|=
literal|null
decl_stmt|;
name|GenericDatumReader
argument_list|<
name|GenericRecord
argument_list|>
name|gdr
init|=
literal|null
decl_stmt|;
specifier|public
name|SchemaReEncoder
parameter_list|(
name|Schema
name|writer
parameter_list|,
name|Schema
name|reader
parameter_list|)
block|{
name|gdr
operator|=
operator|new
name|GenericDatumReader
argument_list|<
name|GenericRecord
argument_list|>
argument_list|(
name|writer
argument_list|,
name|reader
argument_list|)
expr_stmt|;
block|}
specifier|public
name|GenericRecord
name|reencode
parameter_list|(
name|GenericRecord
name|r
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
name|baos
operator|.
name|reset
argument_list|()
expr_stmt|;
name|BinaryEncoder
name|be
init|=
name|EncoderFactory
operator|.
name|get
argument_list|()
operator|.
name|directBinaryEncoder
argument_list|(
name|baos
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|gdw
operator|.
name|setSchema
argument_list|(
name|r
operator|.
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|gdw
operator|.
name|write
argument_list|(
name|r
argument_list|,
name|be
argument_list|)
expr_stmt|;
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|binaryDecoder
operator|=
name|DecoderFactory
operator|.
name|defaultFactory
argument_list|()
operator|.
name|createBinaryDecoder
argument_list|(
name|bais
argument_list|,
name|binaryDecoder
argument_list|)
expr_stmt|;
return|return
name|gdr
operator|.
name|read
argument_list|(
name|r
argument_list|,
name|binaryDecoder
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Exception trying to re-encode record to new schema"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|List
argument_list|<
name|Object
argument_list|>
name|row
decl_stmt|;
comment|/**    * Deserialize an Avro record, recursing into its component fields and    * deserializing them as well.  Fields of the record are matched by name    * against fields in the Hive row.    *    * Because Avro has some data types that Hive does not, these are converted    * during deserialization to types Hive will work with.    *    * @param columnNames List of columns Hive is expecting from record.    * @param columnTypes List of column types matched by index to names    * @param writable Instance of GenericAvroWritable to deserialize    * @param readerSchema Schema of the writable to deserialize    * @return A list of objects suitable for Hive to work with further    * @throws AvroSerdeException For any exception during deseriliazation    */
specifier|public
name|Object
name|deserialize
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|,
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
parameter_list|,
name|Writable
name|writable
parameter_list|,
name|Schema
name|readerSchema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
if|if
condition|(
operator|!
operator|(
name|writable
operator|instanceof
name|AvroGenericRecordWritable
operator|)
condition|)
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Expecting a AvroGenericRecordWritable"
argument_list|)
throw|;
block|}
if|if
condition|(
name|row
operator|==
literal|null
operator|||
name|row
operator|.
name|size
argument_list|()
operator|!=
name|columnNames
operator|.
name|size
argument_list|()
condition|)
block|{
name|row
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|columnNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|row
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|AvroGenericRecordWritable
name|recordWritable
init|=
operator|(
name|AvroGenericRecordWritable
operator|)
name|writable
decl_stmt|;
name|GenericRecord
name|r
init|=
name|recordWritable
operator|.
name|getRecord
argument_list|()
decl_stmt|;
name|Schema
name|fileSchema
init|=
name|recordWritable
operator|.
name|getFileSchema
argument_list|()
decl_stmt|;
name|UID
name|recordReaderId
init|=
name|recordWritable
operator|.
name|getRecordReaderID
argument_list|()
decl_stmt|;
comment|//If the record reader (from which the record is originated) is already seen and valid,
comment|//no need to re-encode the record.
if|if
condition|(
operator|!
name|noEncodingNeeded
operator|.
name|contains
argument_list|(
name|recordReaderId
argument_list|)
condition|)
block|{
name|SchemaReEncoder
name|reEncoder
init|=
literal|null
decl_stmt|;
comment|//Check if the record record is already encoded once. If it does
comment|//reuse the encoder.
if|if
condition|(
name|reEncoderCache
operator|.
name|containsKey
argument_list|(
name|recordReaderId
argument_list|)
condition|)
block|{
name|reEncoder
operator|=
name|reEncoderCache
operator|.
name|get
argument_list|(
name|recordReaderId
argument_list|)
expr_stmt|;
comment|//Reuse the re-encoder
block|}
elseif|else
if|if
condition|(
operator|!
name|r
operator|.
name|getSchema
argument_list|()
operator|.
name|equals
argument_list|(
name|readerSchema
argument_list|)
condition|)
block|{
comment|//Evolved schema?
comment|//Create and store new encoder in the map for re-use
name|reEncoder
operator|=
operator|new
name|SchemaReEncoder
argument_list|(
name|r
operator|.
name|getSchema
argument_list|()
argument_list|,
name|readerSchema
argument_list|)
expr_stmt|;
name|reEncoderCache
operator|.
name|put
argument_list|(
name|recordReaderId
argument_list|,
name|reEncoder
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding new valid RRID :"
operator|+
name|recordReaderId
argument_list|)
expr_stmt|;
name|noEncodingNeeded
operator|.
name|add
argument_list|(
name|recordReaderId
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|reEncoder
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|warnedOnce
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Received different schemas.  Have to re-encode: "
operator|+
name|r
operator|.
name|getSchema
argument_list|()
operator|.
name|toString
argument_list|(
literal|false
argument_list|)
operator|+
literal|"\nSIZE"
operator|+
name|reEncoderCache
operator|+
literal|" ID "
operator|+
name|recordReaderId
argument_list|)
expr_stmt|;
name|warnedOnce
operator|=
literal|true
expr_stmt|;
block|}
name|r
operator|=
name|reEncoder
operator|.
name|reencode
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
block|}
name|workerBase
argument_list|(
name|row
argument_list|,
name|fileSchema
argument_list|,
name|columnNames
argument_list|,
name|columnTypes
argument_list|,
name|r
argument_list|)
expr_stmt|;
return|return
name|row
return|;
block|}
comment|// The actual deserialization may involve nested records, which require recursion.
specifier|private
name|List
argument_list|<
name|Object
argument_list|>
name|workerBase
parameter_list|(
name|List
argument_list|<
name|Object
argument_list|>
name|objectRow
parameter_list|,
name|Schema
name|fileSchema
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|,
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
parameter_list|,
name|GenericRecord
name|record
parameter_list|)
throws|throws
name|AvroSerdeException
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
name|columnNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|TypeInfo
name|columnType
init|=
name|columnTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
name|columnName
init|=
name|columnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Object
name|datum
init|=
name|record
operator|.
name|get
argument_list|(
name|columnName
argument_list|)
decl_stmt|;
name|Schema
name|datumSchema
init|=
name|record
operator|.
name|getSchema
argument_list|()
operator|.
name|getField
argument_list|(
name|columnName
argument_list|)
operator|.
name|schema
argument_list|()
decl_stmt|;
name|Schema
operator|.
name|Field
name|field
init|=
name|AvroSerdeUtils
operator|.
name|isNullableType
argument_list|(
name|fileSchema
argument_list|)
condition|?
name|AvroSerdeUtils
operator|.
name|getOtherTypeFromNullableType
argument_list|(
name|fileSchema
argument_list|)
operator|.
name|getField
argument_list|(
name|columnName
argument_list|)
else|:
name|fileSchema
operator|.
name|getField
argument_list|(
name|columnName
argument_list|)
decl_stmt|;
name|objectRow
operator|.
name|add
argument_list|(
name|worker
argument_list|(
name|datum
argument_list|,
name|field
operator|==
literal|null
condition|?
literal|null
else|:
name|field
operator|.
name|schema
argument_list|()
argument_list|,
name|datumSchema
argument_list|,
name|columnType
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|objectRow
return|;
block|}
specifier|private
name|Object
name|worker
parameter_list|(
name|Object
name|datum
parameter_list|,
name|Schema
name|fileSchema
parameter_list|,
name|Schema
name|recordSchema
parameter_list|,
name|TypeInfo
name|columnType
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
comment|// Klaxon! Klaxon! Klaxon!
comment|// Avro requires NULLable types to be defined as unions of some type T
comment|// and NULL.  This is annoying and we're going to hide it from the user.
if|if
condition|(
name|AvroSerdeUtils
operator|.
name|isNullableType
argument_list|(
name|recordSchema
argument_list|)
condition|)
block|{
return|return
name|deserializeNullableUnion
argument_list|(
name|datum
argument_list|,
name|fileSchema
argument_list|,
name|recordSchema
argument_list|)
return|;
block|}
switch|switch
condition|(
name|columnType
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|STRUCT
case|:
return|return
name|deserializeStruct
argument_list|(
operator|(
name|GenericData
operator|.
name|Record
operator|)
name|datum
argument_list|,
name|fileSchema
argument_list|,
operator|(
name|StructTypeInfo
operator|)
name|columnType
argument_list|)
return|;
case|case
name|UNION
case|:
return|return
name|deserializeUnion
argument_list|(
name|datum
argument_list|,
name|fileSchema
argument_list|,
name|recordSchema
argument_list|,
operator|(
name|UnionTypeInfo
operator|)
name|columnType
argument_list|)
return|;
case|case
name|LIST
case|:
return|return
name|deserializeList
argument_list|(
name|datum
argument_list|,
name|fileSchema
argument_list|,
name|recordSchema
argument_list|,
operator|(
name|ListTypeInfo
operator|)
name|columnType
argument_list|)
return|;
case|case
name|MAP
case|:
return|return
name|deserializeMap
argument_list|(
name|datum
argument_list|,
name|fileSchema
argument_list|,
name|recordSchema
argument_list|,
operator|(
name|MapTypeInfo
operator|)
name|columnType
argument_list|)
return|;
case|case
name|PRIMITIVE
case|:
return|return
name|deserializePrimitive
argument_list|(
name|datum
argument_list|,
name|fileSchema
argument_list|,
name|recordSchema
argument_list|,
operator|(
name|PrimitiveTypeInfo
operator|)
name|columnType
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Unknown TypeInfo: "
operator|+
name|columnType
operator|.
name|getCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
name|Object
name|deserializePrimitive
parameter_list|(
name|Object
name|datum
parameter_list|,
name|Schema
name|fileSchema
parameter_list|,
name|Schema
name|recordSchema
parameter_list|,
name|PrimitiveTypeInfo
name|columnType
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
switch|switch
condition|(
name|columnType
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|STRING
case|:
return|return
name|datum
operator|.
name|toString
argument_list|()
return|;
comment|// To workaround AvroUTF8
comment|// This also gets us around the Enum issue since we just take the value
comment|// and convert it to a string. Yay!
case|case
name|BINARY
case|:
if|if
condition|(
name|recordSchema
operator|.
name|getType
argument_list|()
operator|==
name|Type
operator|.
name|FIXED
condition|)
block|{
name|Fixed
name|fixed
init|=
operator|(
name|Fixed
operator|)
name|datum
decl_stmt|;
return|return
name|fixed
operator|.
name|bytes
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|recordSchema
operator|.
name|getType
argument_list|()
operator|==
name|Type
operator|.
name|BYTES
condition|)
block|{
return|return
name|AvroSerdeUtils
operator|.
name|getBytesFromByteBuffer
argument_list|(
operator|(
name|ByteBuffer
operator|)
name|datum
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Unexpected Avro schema for Binary TypeInfo: "
operator|+
name|recordSchema
operator|.
name|getType
argument_list|()
argument_list|)
throw|;
block|}
case|case
name|DECIMAL
case|:
if|if
condition|(
name|fileSchema
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"File schema is missing for decimal field. Reader schema is "
operator|+
name|columnType
argument_list|)
throw|;
block|}
name|int
name|scale
init|=
literal|0
decl_stmt|;
try|try
block|{
name|scale
operator|=
name|fileSchema
operator|.
name|getJsonProp
argument_list|(
name|AvroSerDe
operator|.
name|AVRO_PROP_SCALE
argument_list|)
operator|.
name|getIntValue
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Failed to obtain scale value from file schema: "
operator|+
name|fileSchema
argument_list|,
name|ex
argument_list|)
throw|;
block|}
name|HiveDecimal
name|dec
init|=
name|AvroSerdeUtils
operator|.
name|getHiveDecimalFromByteBuffer
argument_list|(
operator|(
name|ByteBuffer
operator|)
name|datum
argument_list|,
name|scale
argument_list|)
decl_stmt|;
name|JavaHiveDecimalObjectInspector
name|oi
init|=
operator|(
name|JavaHiveDecimalObjectInspector
operator|)
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
operator|(
name|DecimalTypeInfo
operator|)
name|columnType
argument_list|)
decl_stmt|;
return|return
name|oi
operator|.
name|set
argument_list|(
literal|null
argument_list|,
name|dec
argument_list|)
return|;
case|case
name|CHAR
case|:
if|if
condition|(
name|fileSchema
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"File schema is missing for char field. Reader schema is "
operator|+
name|columnType
argument_list|)
throw|;
block|}
name|int
name|maxLength
init|=
literal|0
decl_stmt|;
try|try
block|{
name|maxLength
operator|=
name|fileSchema
operator|.
name|getJsonProp
argument_list|(
name|AvroSerDe
operator|.
name|AVRO_PROP_MAX_LENGTH
argument_list|)
operator|.
name|getValueAsInt
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Failed to obtain maxLength value for char field from file schema: "
operator|+
name|fileSchema
argument_list|,
name|ex
argument_list|)
throw|;
block|}
name|String
name|str
init|=
name|datum
operator|.
name|toString
argument_list|()
decl_stmt|;
name|HiveChar
name|hc
init|=
operator|new
name|HiveChar
argument_list|(
name|str
argument_list|,
name|maxLength
argument_list|)
decl_stmt|;
return|return
name|hc
return|;
case|case
name|VARCHAR
case|:
if|if
condition|(
name|fileSchema
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"File schema is missing for varchar field. Reader schema is "
operator|+
name|columnType
argument_list|)
throw|;
block|}
name|maxLength
operator|=
literal|0
expr_stmt|;
try|try
block|{
name|maxLength
operator|=
name|fileSchema
operator|.
name|getJsonProp
argument_list|(
name|AvroSerDe
operator|.
name|AVRO_PROP_MAX_LENGTH
argument_list|)
operator|.
name|getValueAsInt
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Failed to obtain maxLength value for varchar field from file schema: "
operator|+
name|fileSchema
argument_list|,
name|ex
argument_list|)
throw|;
block|}
name|str
operator|=
name|datum
operator|.
name|toString
argument_list|()
expr_stmt|;
name|HiveVarchar
name|hvc
init|=
operator|new
name|HiveVarchar
argument_list|(
name|str
argument_list|,
name|maxLength
argument_list|)
decl_stmt|;
return|return
name|hvc
return|;
case|case
name|DATE
case|:
if|if
condition|(
name|recordSchema
operator|.
name|getType
argument_list|()
operator|!=
name|Type
operator|.
name|INT
condition|)
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Unexpected Avro schema for Date TypeInfo: "
operator|+
name|recordSchema
operator|.
name|getType
argument_list|()
argument_list|)
throw|;
block|}
return|return
operator|new
name|Date
argument_list|(
name|DateWritable
operator|.
name|daysToMillis
argument_list|(
operator|(
name|Integer
operator|)
name|datum
argument_list|)
argument_list|)
return|;
case|case
name|TIMESTAMP
case|:
if|if
condition|(
name|recordSchema
operator|.
name|getType
argument_list|()
operator|!=
name|Type
operator|.
name|LONG
condition|)
block|{
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Unexpected Avro schema for Date TypeInfo: "
operator|+
name|recordSchema
operator|.
name|getType
argument_list|()
argument_list|)
throw|;
block|}
return|return
operator|new
name|Timestamp
argument_list|(
operator|(
name|Long
operator|)
name|datum
argument_list|)
return|;
default|default:
return|return
name|datum
return|;
block|}
block|}
comment|/**    * Extract either a null or the correct type from a Nullable type.  This is    * horrible in that we rebuild the TypeInfo every time.    */
specifier|private
name|Object
name|deserializeNullableUnion
parameter_list|(
name|Object
name|datum
parameter_list|,
name|Schema
name|fileSchema
parameter_list|,
name|Schema
name|recordSchema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
name|int
name|tag
init|=
name|GenericData
operator|.
name|get
argument_list|()
operator|.
name|resolveUnion
argument_list|(
name|recordSchema
argument_list|,
name|datum
argument_list|)
decl_stmt|;
comment|// Determine index of value
name|Schema
name|schema
init|=
name|recordSchema
operator|.
name|getTypes
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
decl_stmt|;
if|if
condition|(
name|schema
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|NULL
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Schema
name|currentFileSchema
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|fileSchema
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|fileSchema
operator|.
name|getType
argument_list|()
operator|==
name|Type
operator|.
name|UNION
condition|)
block|{
comment|// The fileSchema may have the null value in a different position, so
comment|// we need to get the correct tag
try|try
block|{
name|tag
operator|=
name|GenericData
operator|.
name|get
argument_list|()
operator|.
name|resolveUnion
argument_list|(
name|fileSchema
argument_list|,
name|datum
argument_list|)
expr_stmt|;
name|currentFileSchema
operator|=
name|fileSchema
operator|.
name|getTypes
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnresolvedUnionException
name|e
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|String
name|datumClazz
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|datum
operator|!=
literal|null
condition|)
block|{
name|datumClazz
operator|=
name|datum
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
name|String
name|msg
init|=
literal|"File schema union could not resolve union. fileSchema = "
operator|+
name|fileSchema
operator|+
literal|", recordSchema = "
operator|+
name|recordSchema
operator|+
literal|", datum class = "
operator|+
name|datumClazz
operator|+
literal|": "
operator|+
name|e
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
comment|// This occurs when the datum type is different between
comment|// the file and record schema. For example if datum is long
comment|// and the field in the file schema is int. See HIVE-9462.
comment|// in this case we will re-use the record schema as the file
comment|// schema, Ultimately we need to clean this code up and will
comment|// do as a follow-on to HIVE-9462.
name|currentFileSchema
operator|=
name|schema
expr_stmt|;
block|}
block|}
else|else
block|{
name|currentFileSchema
operator|=
name|fileSchema
expr_stmt|;
block|}
block|}
return|return
name|worker
argument_list|(
name|datum
argument_list|,
name|currentFileSchema
argument_list|,
name|schema
argument_list|,
name|SchemaToTypeInfo
operator|.
name|generateTypeInfo
argument_list|(
name|schema
argument_list|,
literal|null
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|Object
name|deserializeStruct
parameter_list|(
name|GenericData
operator|.
name|Record
name|datum
parameter_list|,
name|Schema
name|fileSchema
parameter_list|,
name|StructTypeInfo
name|columnType
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
comment|// No equivalent Java type for the backing structure, need to recurse and build a list
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
name|innerFieldTypes
init|=
name|columnType
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|innerFieldNames
init|=
name|columnType
operator|.
name|getAllStructFieldNames
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|innerObjectRow
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|innerFieldTypes
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|workerBase
argument_list|(
name|innerObjectRow
argument_list|,
name|fileSchema
argument_list|,
name|innerFieldNames
argument_list|,
name|innerFieldTypes
argument_list|,
name|datum
argument_list|)
return|;
block|}
specifier|private
name|Object
name|deserializeUnion
parameter_list|(
name|Object
name|datum
parameter_list|,
name|Schema
name|fileSchema
parameter_list|,
name|Schema
name|recordSchema
parameter_list|,
name|UnionTypeInfo
name|columnType
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
name|int
name|tag
init|=
name|GenericData
operator|.
name|get
argument_list|()
operator|.
name|resolveUnion
argument_list|(
name|recordSchema
argument_list|,
name|datum
argument_list|)
decl_stmt|;
comment|// Determine index of value
name|Object
name|desered
init|=
name|worker
argument_list|(
name|datum
argument_list|,
name|fileSchema
operator|==
literal|null
condition|?
literal|null
else|:
name|fileSchema
operator|.
name|getTypes
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
argument_list|,
name|recordSchema
operator|.
name|getTypes
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
argument_list|,
name|columnType
operator|.
name|getAllUnionObjectTypeInfos
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|StandardUnionObjectInspector
operator|.
name|StandardUnion
argument_list|(
operator|(
name|byte
operator|)
name|tag
argument_list|,
name|desered
argument_list|)
return|;
block|}
specifier|private
name|Object
name|deserializeList
parameter_list|(
name|Object
name|datum
parameter_list|,
name|Schema
name|fileSchema
parameter_list|,
name|Schema
name|recordSchema
parameter_list|,
name|ListTypeInfo
name|columnType
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
comment|// Need to check the original schema to see if this is actually a Fixed.
if|if
condition|(
name|recordSchema
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|FIXED
argument_list|)
condition|)
block|{
comment|// We're faking out Hive to work through a type system impedence mismatch.
comment|// Pull out the backing array and convert to a list.
name|GenericData
operator|.
name|Fixed
name|fixed
init|=
operator|(
name|GenericData
operator|.
name|Fixed
operator|)
name|datum
decl_stmt|;
name|List
argument_list|<
name|Byte
argument_list|>
name|asList
init|=
operator|new
name|ArrayList
argument_list|<
name|Byte
argument_list|>
argument_list|(
name|fixed
operator|.
name|bytes
argument_list|()
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|fixed
operator|.
name|bytes
argument_list|()
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|asList
operator|.
name|add
argument_list|(
name|fixed
operator|.
name|bytes
argument_list|()
index|[
name|j
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|asList
return|;
block|}
elseif|else
if|if
condition|(
name|recordSchema
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|BYTES
argument_list|)
condition|)
block|{
comment|// This is going to be slow... hold on.
name|ByteBuffer
name|bb
init|=
operator|(
name|ByteBuffer
operator|)
name|datum
decl_stmt|;
name|List
argument_list|<
name|Byte
argument_list|>
name|asList
init|=
operator|new
name|ArrayList
argument_list|<
name|Byte
argument_list|>
argument_list|(
name|bb
operator|.
name|capacity
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|array
init|=
name|bb
operator|.
name|array
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|array
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|asList
operator|.
name|add
argument_list|(
name|array
index|[
name|j
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|asList
return|;
block|}
else|else
block|{
comment|// An actual list, deser its values
name|List
name|listData
init|=
operator|(
name|List
operator|)
name|datum
decl_stmt|;
name|Schema
name|listSchema
init|=
name|recordSchema
operator|.
name|getElementType
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|listContents
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|listData
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Object
name|obj
range|:
name|listData
control|)
block|{
name|listContents
operator|.
name|add
argument_list|(
name|worker
argument_list|(
name|obj
argument_list|,
name|fileSchema
operator|==
literal|null
condition|?
literal|null
else|:
name|fileSchema
operator|.
name|getElementType
argument_list|()
argument_list|,
name|listSchema
argument_list|,
name|columnType
operator|.
name|getListElementTypeInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|listContents
return|;
block|}
block|}
specifier|private
name|Object
name|deserializeMap
parameter_list|(
name|Object
name|datum
parameter_list|,
name|Schema
name|fileSchema
parameter_list|,
name|Schema
name|mapSchema
parameter_list|,
name|MapTypeInfo
name|columnType
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
comment|// Avro only allows maps with Strings for keys, so we only have to worry
comment|// about deserializing the values
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|CharSequence
argument_list|,
name|Object
argument_list|>
name|mapDatum
init|=
operator|(
name|Map
operator|)
name|datum
decl_stmt|;
name|Schema
name|valueSchema
init|=
name|mapSchema
operator|.
name|getValueType
argument_list|()
decl_stmt|;
name|TypeInfo
name|valueTypeInfo
init|=
name|columnType
operator|.
name|getMapValueTypeInfo
argument_list|()
decl_stmt|;
for|for
control|(
name|CharSequence
name|key
range|:
name|mapDatum
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Object
name|value
init|=
name|mapDatum
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
name|key
operator|.
name|toString
argument_list|()
argument_list|,
name|worker
argument_list|(
name|value
argument_list|,
name|fileSchema
operator|==
literal|null
condition|?
literal|null
else|:
name|fileSchema
operator|.
name|getValueType
argument_list|()
argument_list|,
name|valueSchema
argument_list|,
name|valueTypeInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|map
return|;
block|}
specifier|public
name|HashSet
argument_list|<
name|UID
argument_list|>
name|getNoEncodingNeeded
parameter_list|()
block|{
return|return
name|noEncodingNeeded
return|;
block|}
specifier|public
name|HashMap
argument_list|<
name|UID
argument_list|,
name|SchemaReEncoder
argument_list|>
name|getReEncoderCache
parameter_list|()
block|{
return|return
name|reEncoderCache
return|;
block|}
block|}
end_class

end_unit

