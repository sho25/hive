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
name|Field
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
name|GenericEnumSymbol
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
name|UnionObjectInspector
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
name|HashMap
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
import|import static
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
operator|.
name|Type
operator|.
name|BYTES
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
operator|.
name|Type
operator|.
name|FIXED
import|;
end_import

begin_class
class|class
name|AvroSerializer
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
name|AvroSerializer
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * The Schema to use when serializing Map keys.    * Since we're sharing this across Serializer instances, it must be immutable;    * any properties need to be added in a static initializer.    */
specifier|private
specifier|static
specifier|final
name|Schema
name|STRING_SCHEMA
init|=
name|Schema
operator|.
name|create
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|STRING
argument_list|)
decl_stmt|;
name|AvroGenericRecordWritable
name|cache
init|=
operator|new
name|AvroGenericRecordWritable
argument_list|()
decl_stmt|;
comment|// Hive is pretty simple (read: stupid) in writing out values via the serializer.
comment|// We're just going to go through, matching indices.  Hive formats normally
comment|// handle mismatches with null.  We don't have that option, so instead we'll
comment|// end up throwing an exception for invalid records.
specifier|public
name|Writable
name|serialize
parameter_list|(
name|Object
name|o
parameter_list|,
name|ObjectInspector
name|objectInspector
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
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|objectInspector
decl_stmt|;
name|GenericData
operator|.
name|Record
name|record
init|=
operator|new
name|GenericData
operator|.
name|Record
argument_list|(
name|schema
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|outputFieldRefs
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
if|if
condition|(
name|outputFieldRefs
operator|.
name|size
argument_list|()
operator|!=
name|columnNames
operator|.
name|size
argument_list|()
condition|)
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Number of input columns was different than output columns (in = "
operator|+
name|columnNames
operator|.
name|size
argument_list|()
operator|+
literal|" vs out = "
operator|+
name|outputFieldRefs
operator|.
name|size
argument_list|()
argument_list|)
throw|;
name|int
name|size
init|=
name|schema
operator|.
name|getFields
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|outputFieldRefs
operator|.
name|size
argument_list|()
operator|!=
name|size
condition|)
comment|// Hive does this check for us, so we should be ok.
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Hive passed in a different number of fields than the schema expected: (Hive wanted "
operator|+
name|outputFieldRefs
operator|.
name|size
argument_list|()
operator|+
literal|", Avro expected "
operator|+
name|schema
operator|.
name|getFields
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
throw|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|allStructFieldRefs
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
name|structFieldsDataAsList
init|=
name|soi
operator|.
name|getStructFieldsDataAsList
argument_list|(
name|o
argument_list|)
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
name|Field
name|field
init|=
name|schema
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|TypeInfo
name|typeInfo
init|=
name|columnTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|StructField
name|structFieldRef
init|=
name|allStructFieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Object
name|structFieldData
init|=
name|structFieldsDataAsList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ObjectInspector
name|fieldOI
init|=
name|structFieldRef
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
name|Object
name|val
init|=
name|serialize
argument_list|(
name|typeInfo
argument_list|,
name|fieldOI
argument_list|,
name|structFieldData
argument_list|,
name|field
operator|.
name|schema
argument_list|()
argument_list|)
decl_stmt|;
name|record
operator|.
name|put
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|GenericData
operator|.
name|get
argument_list|()
operator|.
name|validate
argument_list|(
name|schema
argument_list|,
name|record
argument_list|)
condition|)
throw|throw
operator|new
name|SerializeToAvroException
argument_list|(
name|schema
argument_list|,
name|record
argument_list|)
throw|;
name|cache
operator|.
name|setRecord
argument_list|(
name|record
argument_list|)
expr_stmt|;
return|return
name|cache
return|;
block|}
specifier|private
name|Object
name|serialize
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|ObjectInspector
name|fieldOI
parameter_list|,
name|Object
name|structFieldData
parameter_list|,
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
if|if
condition|(
literal|null
operator|==
name|structFieldData
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|AvroSerdeUtils
operator|.
name|isNullableType
argument_list|(
name|schema
argument_list|)
condition|)
block|{
name|schema
operator|=
name|AvroSerdeUtils
operator|.
name|getOtherTypeFromNullableType
argument_list|(
name|schema
argument_list|)
expr_stmt|;
block|}
comment|/* Because we use Hive's 'string' type when Avro calls for enum, we have to expressly check for enum-ness */
if|if
condition|(
name|Schema
operator|.
name|Type
operator|.
name|ENUM
operator|.
name|equals
argument_list|(
name|schema
operator|.
name|getType
argument_list|()
argument_list|)
condition|)
block|{
assert|assert
name|fieldOI
operator|instanceof
name|PrimitiveObjectInspector
assert|;
return|return
name|serializeEnum
argument_list|(
name|typeInfo
argument_list|,
operator|(
name|PrimitiveObjectInspector
operator|)
name|fieldOI
argument_list|,
name|structFieldData
argument_list|,
name|schema
argument_list|)
return|;
block|}
switch|switch
condition|(
name|typeInfo
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
assert|assert
name|fieldOI
operator|instanceof
name|PrimitiveObjectInspector
assert|;
return|return
name|serializePrimitive
argument_list|(
name|typeInfo
argument_list|,
operator|(
name|PrimitiveObjectInspector
operator|)
name|fieldOI
argument_list|,
name|structFieldData
argument_list|)
return|;
case|case
name|MAP
case|:
assert|assert
name|fieldOI
operator|instanceof
name|MapObjectInspector
assert|;
assert|assert
name|typeInfo
operator|instanceof
name|MapTypeInfo
assert|;
return|return
name|serializeMap
argument_list|(
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
argument_list|,
operator|(
name|MapObjectInspector
operator|)
name|fieldOI
argument_list|,
name|structFieldData
argument_list|,
name|schema
argument_list|)
return|;
case|case
name|LIST
case|:
assert|assert
name|fieldOI
operator|instanceof
name|ListObjectInspector
assert|;
assert|assert
name|typeInfo
operator|instanceof
name|ListTypeInfo
assert|;
return|return
name|serializeList
argument_list|(
operator|(
name|ListTypeInfo
operator|)
name|typeInfo
argument_list|,
operator|(
name|ListObjectInspector
operator|)
name|fieldOI
argument_list|,
name|structFieldData
argument_list|,
name|schema
argument_list|)
return|;
case|case
name|UNION
case|:
assert|assert
name|fieldOI
operator|instanceof
name|UnionObjectInspector
assert|;
assert|assert
name|typeInfo
operator|instanceof
name|UnionTypeInfo
assert|;
return|return
name|serializeUnion
argument_list|(
operator|(
name|UnionTypeInfo
operator|)
name|typeInfo
argument_list|,
operator|(
name|UnionObjectInspector
operator|)
name|fieldOI
argument_list|,
name|structFieldData
argument_list|,
name|schema
argument_list|)
return|;
case|case
name|STRUCT
case|:
assert|assert
name|fieldOI
operator|instanceof
name|StructObjectInspector
assert|;
assert|assert
name|typeInfo
operator|instanceof
name|StructTypeInfo
assert|;
return|return
name|serializeStruct
argument_list|(
operator|(
name|StructTypeInfo
operator|)
name|typeInfo
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|fieldOI
argument_list|,
name|structFieldData
argument_list|,
name|schema
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Ran out of TypeInfo Categories: "
operator|+
name|typeInfo
operator|.
name|getCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/** private cache to avoid lots of EnumSymbol creation while serializing.    *  Two levels because the enum symbol is specific to a schema.    *  Object because we want to avoid the overhead of repeated toString calls while maintaining compatability.    *  Provided there are few enum types per record, and few symbols per enum, memory use should be moderate.    *  eg 20 types with 50 symbols each as length-10 Strings should be on the order of 100KB per AvroSerializer.    */
specifier|final
name|InstanceCache
argument_list|<
name|Schema
argument_list|,
name|InstanceCache
argument_list|<
name|Object
argument_list|,
name|GenericEnumSymbol
argument_list|>
argument_list|>
name|enums
init|=
operator|new
name|InstanceCache
argument_list|<
name|Schema
argument_list|,
name|InstanceCache
argument_list|<
name|Object
argument_list|,
name|GenericEnumSymbol
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|InstanceCache
argument_list|<
name|Object
argument_list|,
name|GenericEnumSymbol
argument_list|>
name|makeInstance
parameter_list|(
specifier|final
name|Schema
name|schema
parameter_list|)
block|{
return|return
operator|new
name|InstanceCache
argument_list|<
name|Object
argument_list|,
name|GenericEnumSymbol
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|GenericEnumSymbol
name|makeInstance
parameter_list|(
name|Object
name|seed
parameter_list|)
block|{
return|return
operator|new
name|GenericData
operator|.
name|EnumSymbol
argument_list|(
name|schema
argument_list|,
name|seed
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
decl_stmt|;
specifier|private
name|Object
name|serializeEnum
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|PrimitiveObjectInspector
name|fieldOI
parameter_list|,
name|Object
name|structFieldData
parameter_list|,
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
return|return
name|enums
operator|.
name|retrieve
argument_list|(
name|schema
argument_list|)
operator|.
name|retrieve
argument_list|(
name|serializePrimitive
argument_list|(
name|typeInfo
argument_list|,
name|fieldOI
argument_list|,
name|structFieldData
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|Object
name|serializeStruct
parameter_list|(
name|StructTypeInfo
name|typeInfo
parameter_list|,
name|StructObjectInspector
name|ssoi
parameter_list|,
name|Object
name|o
parameter_list|,
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
name|int
name|size
init|=
name|schema
operator|.
name|getFields
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|allStructFieldRefs
init|=
name|ssoi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|structFieldsDataAsList
init|=
name|ssoi
operator|.
name|getStructFieldsDataAsList
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|GenericData
operator|.
name|Record
name|record
init|=
operator|new
name|GenericData
operator|.
name|Record
argument_list|(
name|schema
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
name|allStructFieldTypeInfos
init|=
name|typeInfo
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
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
name|Field
name|field
init|=
name|schema
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|TypeInfo
name|colTypeInfo
init|=
name|allStructFieldTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|StructField
name|structFieldRef
init|=
name|allStructFieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Object
name|structFieldData
init|=
name|structFieldsDataAsList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ObjectInspector
name|fieldOI
init|=
name|structFieldRef
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
name|Object
name|val
init|=
name|serialize
argument_list|(
name|colTypeInfo
argument_list|,
name|fieldOI
argument_list|,
name|structFieldData
argument_list|,
name|field
operator|.
name|schema
argument_list|()
argument_list|)
decl_stmt|;
name|record
operator|.
name|put
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
return|return
name|record
return|;
block|}
specifier|private
name|Object
name|serializePrimitive
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|PrimitiveObjectInspector
name|fieldOI
parameter_list|,
name|Object
name|structFieldData
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
switch|switch
condition|(
name|fieldOI
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|UNKNOWN
case|:
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Received UNKNOWN primitive category."
argument_list|)
throw|;
case|case
name|VOID
case|:
return|return
literal|null
return|;
default|default:
comment|// All other primitive types are simple
return|return
name|fieldOI
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|structFieldData
argument_list|)
return|;
block|}
block|}
specifier|private
name|Object
name|serializeUnion
parameter_list|(
name|UnionTypeInfo
name|typeInfo
parameter_list|,
name|UnionObjectInspector
name|fieldOI
parameter_list|,
name|Object
name|structFieldData
parameter_list|,
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
name|byte
name|tag
init|=
name|fieldOI
operator|.
name|getTag
argument_list|(
name|structFieldData
argument_list|)
decl_stmt|;
comment|// Invariant that Avro's tag ordering must match Hive's.
return|return
name|serialize
argument_list|(
name|typeInfo
operator|.
name|getAllUnionObjectTypeInfos
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
argument_list|,
name|fieldOI
operator|.
name|getObjectInspectors
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
argument_list|,
name|fieldOI
operator|.
name|getField
argument_list|(
name|structFieldData
argument_list|)
argument_list|,
name|schema
operator|.
name|getTypes
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
argument_list|)
return|;
block|}
comment|// We treat FIXED and BYTES as arrays of tinyints within Hive.  Check
comment|// if we're dealing with either of these types and thus need to serialize
comment|// them as their Avro types.
specifier|private
name|boolean
name|isTransformedType
parameter_list|(
name|Schema
name|schema
parameter_list|)
block|{
return|return
name|schema
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|FIXED
argument_list|)
operator|||
name|schema
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|BYTES
argument_list|)
return|;
block|}
specifier|private
name|Object
name|serializeTransformedType
parameter_list|(
name|ListTypeInfo
name|typeInfo
parameter_list|,
name|ListObjectInspector
name|fieldOI
parameter_list|,
name|Object
name|structFieldData
parameter_list|,
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Beginning to transform "
operator|+
name|typeInfo
operator|+
literal|" with Avro schema "
operator|+
name|schema
operator|.
name|toString
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|schema
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|FIXED
argument_list|)
condition|)
return|return
name|serializedAvroFixed
argument_list|(
name|typeInfo
argument_list|,
name|fieldOI
argument_list|,
name|structFieldData
argument_list|,
name|schema
argument_list|)
return|;
else|else
return|return
name|serializeAvroBytes
argument_list|(
name|typeInfo
argument_list|,
name|fieldOI
argument_list|,
name|structFieldData
argument_list|,
name|schema
argument_list|)
return|;
block|}
specifier|private
name|Object
name|serializeAvroBytes
parameter_list|(
name|ListTypeInfo
name|typeInfo
parameter_list|,
name|ListObjectInspector
name|fieldOI
parameter_list|,
name|Object
name|structFieldData
parameter_list|,
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|extraByteArray
argument_list|(
name|fieldOI
argument_list|,
name|structFieldData
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|bb
operator|.
name|rewind
argument_list|()
return|;
block|}
specifier|private
name|Object
name|serializedAvroFixed
parameter_list|(
name|ListTypeInfo
name|typeInfo
parameter_list|,
name|ListObjectInspector
name|fieldOI
parameter_list|,
name|Object
name|structFieldData
parameter_list|,
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
return|return
operator|new
name|GenericData
operator|.
name|Fixed
argument_list|(
name|schema
argument_list|,
name|extraByteArray
argument_list|(
name|fieldOI
argument_list|,
name|structFieldData
argument_list|)
argument_list|)
return|;
block|}
comment|// For transforming to BYTES and FIXED, pull out the byte array Avro will want
specifier|private
name|byte
index|[]
name|extraByteArray
parameter_list|(
name|ListObjectInspector
name|fieldOI
parameter_list|,
name|Object
name|structFieldData
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
comment|// Grab a book.  This is going to be slow.
name|int
name|listLength
init|=
name|fieldOI
operator|.
name|getListLength
argument_list|(
name|structFieldData
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|listLength
index|]
decl_stmt|;
assert|assert
name|fieldOI
operator|.
name|getListElementObjectInspector
argument_list|()
operator|instanceof
name|PrimitiveObjectInspector
assert|;
name|PrimitiveObjectInspector
name|poi
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|fieldOI
operator|.
name|getListElementObjectInspector
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
argument_list|>
name|list
init|=
name|fieldOI
operator|.
name|getList
argument_list|(
name|structFieldData
argument_list|)
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
name|listLength
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|b
init|=
name|poi
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|b
operator|instanceof
name|Byte
operator|)
condition|)
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Attempting to transform to bytes, element was not byte but "
operator|+
name|b
operator|.
name|getClass
argument_list|()
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
throw|;
name|bytes
index|[
name|i
index|]
operator|=
operator|(
name|Byte
operator|)
name|b
expr_stmt|;
block|}
return|return
name|bytes
return|;
block|}
specifier|private
name|Object
name|serializeList
parameter_list|(
name|ListTypeInfo
name|typeInfo
parameter_list|,
name|ListObjectInspector
name|fieldOI
parameter_list|,
name|Object
name|structFieldData
parameter_list|,
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
if|if
condition|(
name|isTransformedType
argument_list|(
name|schema
argument_list|)
condition|)
return|return
name|serializeTransformedType
argument_list|(
name|typeInfo
argument_list|,
name|fieldOI
argument_list|,
name|structFieldData
argument_list|,
name|schema
argument_list|)
return|;
name|List
argument_list|<
name|?
argument_list|>
name|list
init|=
name|fieldOI
operator|.
name|getList
argument_list|(
name|structFieldData
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|deserialized
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|list
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|TypeInfo
name|listElementTypeInfo
init|=
name|typeInfo
operator|.
name|getListElementTypeInfo
argument_list|()
decl_stmt|;
name|ObjectInspector
name|listElementObjectInspector
init|=
name|fieldOI
operator|.
name|getListElementObjectInspector
argument_list|()
decl_stmt|;
name|Schema
name|elementType
init|=
name|schema
operator|.
name|getElementType
argument_list|()
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
name|list
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|deserialized
operator|.
name|add
argument_list|(
name|i
argument_list|,
name|serialize
argument_list|(
name|listElementTypeInfo
argument_list|,
name|listElementObjectInspector
argument_list|,
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|elementType
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|deserialized
return|;
block|}
specifier|private
name|Object
name|serializeMap
parameter_list|(
name|MapTypeInfo
name|typeInfo
parameter_list|,
name|MapObjectInspector
name|fieldOI
parameter_list|,
name|Object
name|structFieldData
parameter_list|,
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
comment|// Avro only allows maps with string keys
if|if
condition|(
operator|!
name|mapHasStringKey
argument_list|(
name|fieldOI
operator|.
name|getMapKeyObjectInspector
argument_list|()
argument_list|)
condition|)
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Avro only supports maps with keys as Strings.  Current Map is: "
operator|+
name|typeInfo
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
name|ObjectInspector
name|mapKeyObjectInspector
init|=
name|fieldOI
operator|.
name|getMapKeyObjectInspector
argument_list|()
decl_stmt|;
name|ObjectInspector
name|mapValueObjectInspector
init|=
name|fieldOI
operator|.
name|getMapValueObjectInspector
argument_list|()
decl_stmt|;
name|TypeInfo
name|mapKeyTypeInfo
init|=
name|typeInfo
operator|.
name|getMapKeyTypeInfo
argument_list|()
decl_stmt|;
name|TypeInfo
name|mapValueTypeInfo
init|=
name|typeInfo
operator|.
name|getMapValueTypeInfo
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
name|fieldOI
operator|.
name|getMap
argument_list|(
name|structFieldData
argument_list|)
decl_stmt|;
name|Schema
name|valueType
init|=
name|schema
operator|.
name|getValueType
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|deserialized
init|=
operator|new
name|HashMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
argument_list|(
name|fieldOI
operator|.
name|getMapSize
argument_list|(
name|structFieldData
argument_list|)
argument_list|)
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
name|deserialized
operator|.
name|put
argument_list|(
name|serialize
argument_list|(
name|mapKeyTypeInfo
argument_list|,
name|mapKeyObjectInspector
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|STRING_SCHEMA
argument_list|)
argument_list|,
name|serialize
argument_list|(
name|mapValueTypeInfo
argument_list|,
name|mapValueObjectInspector
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|valueType
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|deserialized
return|;
block|}
specifier|private
name|boolean
name|mapHasStringKey
parameter_list|(
name|ObjectInspector
name|mapKeyObjectInspector
parameter_list|)
block|{
return|return
name|mapKeyObjectInspector
operator|instanceof
name|PrimitiveObjectInspector
operator|&&
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|mapKeyObjectInspector
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|STRING
argument_list|)
return|;
block|}
comment|/**    * Thrown when, during serialization of a Hive row to an Avro record, Avro    * cannot verify the converted row to the record's schema.    */
specifier|public
specifier|static
class|class
name|SerializeToAvroException
extends|extends
name|AvroSerdeException
block|{
specifier|final
specifier|private
name|Schema
name|schema
decl_stmt|;
specifier|final
specifier|private
name|GenericData
operator|.
name|Record
name|record
decl_stmt|;
specifier|public
name|SerializeToAvroException
parameter_list|(
name|Schema
name|schema
parameter_list|,
name|GenericData
operator|.
name|Record
name|record
parameter_list|)
block|{
name|this
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
name|this
operator|.
name|record
operator|=
name|record
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Avro could not validate record against schema (record = "
operator|+
name|record
operator|+
literal|") (schema = "
operator|+
name|schema
operator|.
name|toString
argument_list|(
literal|false
argument_list|)
operator|+
literal|")"
return|;
block|}
block|}
block|}
end_class

end_unit

