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
name|hbase
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
name|hbase
operator|.
name|client
operator|.
name|Put
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
name|hbase
operator|.
name|ColumnMappings
operator|.
name|ColumnMapping
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
name|SerDeUtils
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
name|io
operator|.
name|Writable
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

begin_class
specifier|public
class|class
name|HBaseRowSerializer
block|{
specifier|private
specifier|final
name|HBaseKeyFactory
name|keyFactory
decl_stmt|;
specifier|private
specifier|final
name|HBaseSerDeParameters
name|hbaseParam
decl_stmt|;
specifier|private
specifier|final
name|LazySimpleSerDe
operator|.
name|SerDeParameters
name|serdeParam
decl_stmt|;
specifier|private
specifier|final
name|int
name|keyIndex
decl_stmt|;
specifier|private
specifier|final
name|ColumnMapping
name|keyMapping
decl_stmt|;
specifier|private
specifier|final
name|ColumnMapping
index|[]
name|columnMappings
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|separators
decl_stmt|;
comment|// the separators array
specifier|private
specifier|final
name|boolean
name|escaped
decl_stmt|;
comment|// whether we need to escape the data when writing out
specifier|private
specifier|final
name|byte
name|escapeChar
decl_stmt|;
comment|// which char to use as the escape char, e.g. '\\'
specifier|private
specifier|final
name|boolean
index|[]
name|needsEscape
decl_stmt|;
comment|// which chars need to be escaped. This array should have size
comment|// of 128. Negative byte values (or byte values>= 128) are
comment|// never escaped.
specifier|private
specifier|final
name|long
name|putTimestamp
decl_stmt|;
specifier|private
specifier|final
name|ByteStream
operator|.
name|Output
name|output
init|=
operator|new
name|ByteStream
operator|.
name|Output
argument_list|()
decl_stmt|;
specifier|public
name|HBaseRowSerializer
parameter_list|(
name|HBaseSerDeParameters
name|hbaseParam
parameter_list|)
block|{
name|this
operator|.
name|hbaseParam
operator|=
name|hbaseParam
expr_stmt|;
name|this
operator|.
name|keyFactory
operator|=
name|hbaseParam
operator|.
name|getKeyFactory
argument_list|()
expr_stmt|;
name|this
operator|.
name|serdeParam
operator|=
name|hbaseParam
operator|.
name|getSerdeParams
argument_list|()
expr_stmt|;
name|this
operator|.
name|separators
operator|=
name|serdeParam
operator|.
name|getSeparators
argument_list|()
expr_stmt|;
name|this
operator|.
name|escaped
operator|=
name|serdeParam
operator|.
name|isEscaped
argument_list|()
expr_stmt|;
name|this
operator|.
name|escapeChar
operator|=
name|serdeParam
operator|.
name|getEscapeChar
argument_list|()
expr_stmt|;
name|this
operator|.
name|needsEscape
operator|=
name|serdeParam
operator|.
name|getNeedsEscape
argument_list|()
expr_stmt|;
name|this
operator|.
name|keyIndex
operator|=
name|hbaseParam
operator|.
name|getKeyIndex
argument_list|()
expr_stmt|;
name|this
operator|.
name|columnMappings
operator|=
name|hbaseParam
operator|.
name|getColumnMappings
argument_list|()
operator|.
name|getColumnsMapping
argument_list|()
expr_stmt|;
name|this
operator|.
name|keyMapping
operator|=
name|hbaseParam
operator|.
name|getColumnMappings
argument_list|()
operator|.
name|getKeyMapping
argument_list|()
expr_stmt|;
name|this
operator|.
name|putTimestamp
operator|=
name|hbaseParam
operator|.
name|getPutTimestamp
argument_list|()
expr_stmt|;
block|}
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
name|Exception
block|{
if|if
condition|(
name|objInspector
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
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
comment|// Prepare the field ObjectInspectors
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
name|StructField
name|field
init|=
name|fields
operator|.
name|get
argument_list|(
name|keyIndex
argument_list|)
decl_stmt|;
name|Object
name|value
init|=
name|list
operator|.
name|get
argument_list|(
name|keyIndex
argument_list|)
decl_stmt|;
name|byte
index|[]
name|key
init|=
name|keyFactory
operator|.
name|serializeKey
argument_list|(
name|value
argument_list|,
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"HBase row key cannot be NULL"
argument_list|)
throw|;
block|}
name|Put
name|put
init|=
name|putTimestamp
operator|>=
literal|0
condition|?
operator|new
name|Put
argument_list|(
name|key
argument_list|,
name|putTimestamp
argument_list|)
else|:
operator|new
name|Put
argument_list|(
name|key
argument_list|)
decl_stmt|;
comment|// Serialize each field
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fields
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
operator|==
name|keyIndex
condition|)
block|{
continue|continue;
block|}
name|field
operator|=
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|value
operator|=
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|serializeField
argument_list|(
name|value
argument_list|,
name|field
argument_list|,
name|columnMappings
index|[
name|i
index|]
argument_list|,
name|put
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|PutWritable
argument_list|(
name|put
argument_list|)
return|;
block|}
name|byte
index|[]
name|serializeKeyField
parameter_list|(
name|Object
name|keyValue
parameter_list|,
name|StructField
name|keyField
parameter_list|,
name|ColumnMapping
name|keyMapping
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|keyValue
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"HBase row key cannot be NULL"
argument_list|)
throw|;
block|}
name|ObjectInspector
name|keyFieldOI
init|=
name|keyField
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|keyFieldOI
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
argument_list|)
operator|&&
name|keyMapping
operator|.
name|isCategory
argument_list|(
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
argument_list|)
condition|)
block|{
comment|// we always serialize the String type using the escaped algorithm for LazyString
return|return
name|serialize
argument_list|(
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|keyValue
argument_list|,
name|keyFieldOI
argument_list|)
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|// use the serialization option switch to write primitive values as either a variable
comment|// length UTF8 string or a fixed width bytes if serializing in binary format
name|boolean
name|writeBinary
init|=
name|keyMapping
operator|.
name|binaryStorage
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
return|return
name|serialize
argument_list|(
name|keyValue
argument_list|,
name|keyFieldOI
argument_list|,
literal|1
argument_list|,
name|writeBinary
argument_list|)
return|;
block|}
specifier|private
name|void
name|serializeField
parameter_list|(
name|Object
name|value
parameter_list|,
name|StructField
name|field
parameter_list|,
name|ColumnMapping
name|colMap
parameter_list|,
name|Put
name|put
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
comment|// a null object, we do not serialize it
return|return;
block|}
comment|// Get the field objectInspector and the field object.
name|ObjectInspector
name|foi
init|=
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
comment|// If the field corresponds to a column family in HBase
if|if
condition|(
name|colMap
operator|.
name|qualifierName
operator|==
literal|null
condition|)
block|{
name|MapObjectInspector
name|moi
init|=
operator|(
name|MapObjectInspector
operator|)
name|foi
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
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|map
operator|==
literal|null
condition|)
block|{
return|return;
block|}
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
comment|// Get the Key
comment|// Map keys are required to be primitive and may be serialized in binary format
name|byte
index|[]
name|columnQualifierBytes
init|=
name|serialize
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|koi
argument_list|,
literal|3
argument_list|,
name|colMap
operator|.
name|binaryStorage
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnQualifierBytes
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
comment|// Map values may be serialized in binary format when they are primitive and binary
comment|// serialization is the option selected
name|byte
index|[]
name|bytes
init|=
name|serialize
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|voi
argument_list|,
literal|3
argument_list|,
name|colMap
operator|.
name|binaryStorage
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|bytes
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|put
operator|.
name|add
argument_list|(
name|colMap
operator|.
name|familyNameBytes
argument_list|,
name|columnQualifierBytes
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|byte
index|[]
name|bytes
decl_stmt|;
comment|// If the field that is passed in is NOT a primitive, and either the
comment|// field is not declared (no schema was given at initialization), or
comment|// the field is declared as a primitive in initialization, serialize
comment|// the data to JSON string.  Otherwise serialize the data in the
comment|// delimited way.
if|if
condition|(
operator|!
name|foi
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
argument_list|)
operator|&&
name|colMap
operator|.
name|isCategory
argument_list|(
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
argument_list|)
condition|)
block|{
comment|// we always serialize the String type using the escaped algorithm for LazyString
name|bytes
operator|=
name|serialize
argument_list|(
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|value
argument_list|,
name|foi
argument_list|)
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// use the serialization option switch to write primitive values as either a variable
comment|// length UTF8 string or a fixed width bytes if serializing in binary format
name|bytes
operator|=
name|serialize
argument_list|(
name|value
argument_list|,
name|foi
argument_list|,
literal|1
argument_list|,
name|colMap
operator|.
name|binaryStorage
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|bytes
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|put
operator|.
name|add
argument_list|(
name|colMap
operator|.
name|familyNameBytes
argument_list|,
name|colMap
operator|.
name|qualifierNameBytes
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*    * Serialize the row into a ByteStream.    *    * @param obj           The object for the current field.    * @param objInspector  The ObjectInspector for the current Object.    * @param level         The current level of separator.    * @param writeBinary   Whether to write a primitive object as an UTF8 variable length string or    *                      as a fixed width byte array onto the byte stream.    * @throws IOException  On error in writing to the serialization stream.    * @return true         On serializing a non-null object, otherwise false.    */
specifier|private
name|byte
index|[]
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|,
name|int
name|level
parameter_list|,
name|boolean
name|writeBinary
parameter_list|)
throws|throws
name|IOException
block|{
name|output
operator|.
name|reset
argument_list|()
expr_stmt|;
if|if
condition|(
name|objInspector
operator|.
name|getCategory
argument_list|()
operator|==
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
operator|&&
name|writeBinary
condition|)
block|{
name|LazyUtils
operator|.
name|writePrimitive
argument_list|(
name|output
argument_list|,
name|obj
argument_list|,
operator|(
name|PrimitiveObjectInspector
operator|)
name|objInspector
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|serialize
argument_list|(
name|obj
argument_list|,
name|objInspector
argument_list|,
name|level
argument_list|,
name|output
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
return|return
name|output
operator|.
name|toByteArray
argument_list|()
return|;
block|}
specifier|private
name|boolean
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|,
name|int
name|level
parameter_list|,
name|ByteStream
operator|.
name|Output
name|ss
parameter_list|)
throws|throws
name|IOException
block|{
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
name|LazyUtils
operator|.
name|writePrimitiveUTF8
argument_list|(
name|ss
argument_list|,
name|obj
argument_list|,
operator|(
name|PrimitiveObjectInspector
operator|)
name|objInspector
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|needsEscape
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
case|case
name|LIST
case|:
name|char
name|separator
init|=
operator|(
name|char
operator|)
name|separators
index|[
name|level
index|]
decl_stmt|;
name|ListObjectInspector
name|loi
init|=
operator|(
name|ListObjectInspector
operator|)
name|objInspector
decl_stmt|;
name|List
argument_list|<
name|?
argument_list|>
name|list
init|=
name|loi
operator|.
name|getList
argument_list|(
name|obj
argument_list|)
decl_stmt|;
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
return|return
literal|false
return|;
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
name|ss
operator|.
name|write
argument_list|(
name|separator
argument_list|)
expr_stmt|;
block|}
name|serialize
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|eoi
argument_list|,
name|level
operator|+
literal|1
argument_list|,
name|ss
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
case|case
name|MAP
case|:
name|char
name|sep
init|=
operator|(
name|char
operator|)
name|separators
index|[
name|level
index|]
decl_stmt|;
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
return|return
literal|false
return|;
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
name|ss
operator|.
name|write
argument_list|(
name|sep
argument_list|)
expr_stmt|;
block|}
name|serialize
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|koi
argument_list|,
name|level
operator|+
literal|2
argument_list|,
name|ss
argument_list|)
expr_stmt|;
name|ss
operator|.
name|write
argument_list|(
name|keyValueSeparator
argument_list|)
expr_stmt|;
name|serialize
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|voi
argument_list|,
name|level
operator|+
literal|2
argument_list|,
name|ss
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
case|case
name|STRUCT
case|:
name|sep
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
return|return
literal|false
return|;
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
name|ss
operator|.
name|write
argument_list|(
name|sep
argument_list|)
expr_stmt|;
block|}
name|serialize
argument_list|(
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
name|level
operator|+
literal|1
argument_list|,
name|ss
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
default|default:
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
block|}
block|}
end_class

end_unit

