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
name|codehaus
operator|.
name|jackson
operator|.
name|JsonNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|node
operator|.
name|JsonNodeFactory
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

begin_comment
comment|/**  * Convert Hive TypeInfo to an Avro Schema  */
end_comment

begin_class
specifier|public
class|class
name|TypeInfoToSchema
block|{
specifier|private
specifier|static
specifier|final
name|Schema
operator|.
name|Parser
name|PARSER
init|=
operator|new
name|Schema
operator|.
name|Parser
argument_list|()
decl_stmt|;
specifier|private
name|long
name|recordCounter
init|=
literal|0
decl_stmt|;
comment|/**    * Converts Hive schema to avro schema    *    * @param columnNames Names of the hive columns    * @param columnTypes Hive Column types    * @param namespace   Namespace of Avro schema    * @param name        Avro schema name    * @param doc         Avro schema doc    * @return Avro Schema    */
specifier|public
name|Schema
name|convert
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
name|List
argument_list|<
name|String
argument_list|>
name|columnComments
parameter_list|,
name|String
name|namespace
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|doc
parameter_list|)
block|{
name|List
argument_list|<
name|Schema
operator|.
name|Field
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<
name|Schema
operator|.
name|Field
argument_list|>
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
name|columnNames
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|String
name|comment
init|=
name|columnComments
operator|.
name|size
argument_list|()
operator|>
name|i
condition|?
name|columnComments
operator|.
name|get
argument_list|(
name|i
argument_list|)
else|:
literal|null
decl_stmt|;
specifier|final
name|Schema
operator|.
name|Field
name|avroField
init|=
name|createAvroField
argument_list|(
name|columnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|columnTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|comment
argument_list|)
decl_stmt|;
name|fields
operator|.
name|addAll
argument_list|(
name|getFields
argument_list|(
name|avroField
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|name
operator|==
literal|null
operator|||
name|name
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|name
operator|=
literal|"baseRecord"
expr_stmt|;
block|}
name|Schema
name|avroSchema
init|=
name|Schema
operator|.
name|createRecord
argument_list|(
name|name
argument_list|,
name|doc
argument_list|,
name|namespace
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|avroSchema
operator|.
name|setFields
argument_list|(
name|fields
argument_list|)
expr_stmt|;
return|return
name|avroSchema
return|;
block|}
specifier|private
name|Schema
operator|.
name|Field
name|createAvroField
parameter_list|(
name|String
name|name
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|,
name|String
name|comment
parameter_list|)
block|{
return|return
operator|new
name|Schema
operator|.
name|Field
argument_list|(
name|name
argument_list|,
name|createAvroSchema
argument_list|(
name|typeInfo
argument_list|)
argument_list|,
name|comment
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|private
name|Schema
name|createAvroSchema
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
name|Schema
name|schema
init|=
literal|null
decl_stmt|;
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
name|schema
operator|=
name|createAvroPrimitive
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
break|break;
case|case
name|LIST
case|:
name|schema
operator|=
name|createAvroArray
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
break|break;
case|case
name|MAP
case|:
name|schema
operator|=
name|createAvroMap
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
break|break;
case|case
name|STRUCT
case|:
name|schema
operator|=
name|createAvroRecord
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
break|break;
case|case
name|UNION
case|:
name|schema
operator|=
name|createAvroUnion
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
break|break;
block|}
return|return
name|wrapInUnionWithNull
argument_list|(
name|schema
argument_list|)
return|;
block|}
specifier|private
name|Schema
name|createAvroPrimitive
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
name|PrimitiveTypeInfo
name|primitiveTypeInfo
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|Schema
name|schema
decl_stmt|;
switch|switch
condition|(
name|primitiveTypeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|STRING
case|:
name|schema
operator|=
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
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
name|schema
operator|=
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
expr_stmt|;
break|break;
case|case
name|VARCHAR
case|:
name|schema
operator|=
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
expr_stmt|;
break|break;
case|case
name|BINARY
case|:
name|schema
operator|=
name|Schema
operator|.
name|create
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|BYTES
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|schema
operator|=
name|Schema
operator|.
name|create
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|INT
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|schema
operator|=
name|Schema
operator|.
name|create
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|INT
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|schema
operator|=
name|Schema
operator|.
name|create
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|INT
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|schema
operator|=
name|Schema
operator|.
name|create
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|LONG
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|schema
operator|=
name|Schema
operator|.
name|create
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|FLOAT
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|schema
operator|=
name|Schema
operator|.
name|create
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|DOUBLE
argument_list|)
expr_stmt|;
break|break;
case|case
name|BOOLEAN
case|:
name|schema
operator|=
name|Schema
operator|.
name|create
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|BOOLEAN
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|DecimalTypeInfo
name|decimalTypeInfo
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|String
name|precision
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|decimalTypeInfo
operator|.
name|precision
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|scale
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|decimalTypeInfo
operator|.
name|scale
argument_list|()
argument_list|)
decl_stmt|;
name|schema
operator|=
name|PARSER
operator|.
name|parse
argument_list|(
literal|"{"
operator|+
literal|"\"type\":\"bytes\","
operator|+
literal|"\"logicalType\":\"decimal\","
operator|+
literal|"\"precision\":"
operator|+
name|precision
operator|+
literal|","
operator|+
literal|"\"scale\":"
operator|+
name|scale
operator|+
literal|"}"
argument_list|)
expr_stmt|;
break|break;
case|case
name|VOID
case|:
name|schema
operator|=
name|Schema
operator|.
name|create
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|NULL
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
name|typeInfo
operator|+
literal|" is not supported."
argument_list|)
throw|;
block|}
return|return
name|schema
return|;
block|}
specifier|private
name|Schema
name|createAvroUnion
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
name|List
argument_list|<
name|Schema
argument_list|>
name|childSchemas
init|=
operator|new
name|ArrayList
argument_list|<
name|Schema
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|TypeInfo
name|childTypeInfo
range|:
operator|(
operator|(
name|UnionTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getAllUnionObjectTypeInfos
argument_list|()
control|)
block|{
specifier|final
name|Schema
name|childSchema
init|=
name|createAvroSchema
argument_list|(
name|childTypeInfo
argument_list|)
decl_stmt|;
if|if
condition|(
name|childSchema
operator|.
name|getType
argument_list|()
operator|==
name|Schema
operator|.
name|Type
operator|.
name|UNION
condition|)
block|{
name|childSchemas
operator|.
name|addAll
argument_list|(
name|childSchema
operator|.
name|getTypes
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|childSchemas
operator|.
name|add
argument_list|(
name|childSchema
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|Schema
operator|.
name|createUnion
argument_list|(
name|removeDuplicateNullSchemas
argument_list|(
name|childSchemas
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|Schema
name|createAvroRecord
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
name|List
argument_list|<
name|Schema
operator|.
name|Field
argument_list|>
name|childFields
init|=
operator|new
name|ArrayList
argument_list|<
name|Schema
operator|.
name|Field
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|allStructFieldNames
init|=
operator|(
operator|(
name|StructTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getAllStructFieldNames
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|allStructFieldTypeInfos
init|=
operator|(
operator|(
name|StructTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
decl_stmt|;
if|if
condition|(
name|allStructFieldNames
operator|.
name|size
argument_list|()
operator|!=
name|allStructFieldTypeInfos
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Failed to generate avro schema from hive schema. "
operator|+
literal|"name and column type differs. names = "
operator|+
name|allStructFieldNames
operator|+
literal|", types = "
operator|+
name|allStructFieldTypeInfos
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|allStructFieldNames
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|TypeInfo
name|childTypeInfo
init|=
name|allStructFieldTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
specifier|final
name|Schema
operator|.
name|Field
name|grandChildSchemaField
init|=
name|createAvroField
argument_list|(
name|allStructFieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|childTypeInfo
argument_list|,
name|childTypeInfo
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Schema
operator|.
name|Field
argument_list|>
name|grandChildFields
init|=
name|getFields
argument_list|(
name|grandChildSchemaField
argument_list|)
decl_stmt|;
name|childFields
operator|.
name|addAll
argument_list|(
name|grandChildFields
argument_list|)
expr_stmt|;
block|}
name|Schema
name|recordSchema
init|=
name|Schema
operator|.
name|createRecord
argument_list|(
literal|"record_"
operator|+
name|recordCounter
argument_list|,
name|typeInfo
operator|.
name|toString
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
operator|++
name|recordCounter
expr_stmt|;
name|recordSchema
operator|.
name|setFields
argument_list|(
name|childFields
argument_list|)
expr_stmt|;
return|return
name|recordSchema
return|;
block|}
specifier|private
name|Schema
name|createAvroMap
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
name|TypeInfo
name|keyTypeInfo
init|=
operator|(
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getMapKeyTypeInfo
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|keyTypeInfo
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
operator|!=
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|STRING
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Key of Map can only be a String"
argument_list|)
throw|;
block|}
name|TypeInfo
name|valueTypeInfo
init|=
operator|(
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getMapValueTypeInfo
argument_list|()
decl_stmt|;
name|Schema
name|valueSchema
init|=
name|createAvroSchema
argument_list|(
name|valueTypeInfo
argument_list|)
decl_stmt|;
return|return
name|Schema
operator|.
name|createMap
argument_list|(
name|valueSchema
argument_list|)
return|;
block|}
specifier|private
name|Schema
name|createAvroArray
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
name|ListTypeInfo
name|listTypeInfo
init|=
operator|(
name|ListTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|Schema
name|listSchema
init|=
name|createAvroSchema
argument_list|(
name|listTypeInfo
operator|.
name|getListElementTypeInfo
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|Schema
operator|.
name|createArray
argument_list|(
name|listSchema
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|Schema
operator|.
name|Field
argument_list|>
name|getFields
parameter_list|(
name|Schema
operator|.
name|Field
name|schemaField
parameter_list|)
block|{
name|List
argument_list|<
name|Schema
operator|.
name|Field
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<
name|Schema
operator|.
name|Field
argument_list|>
argument_list|()
decl_stmt|;
name|JsonNode
name|nullDefault
init|=
name|JsonNodeFactory
operator|.
name|instance
operator|.
name|nullNode
argument_list|()
decl_stmt|;
if|if
condition|(
name|schemaField
operator|.
name|schema
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|Schema
operator|.
name|Type
operator|.
name|RECORD
condition|)
block|{
for|for
control|(
name|Schema
operator|.
name|Field
name|field
range|:
name|schemaField
operator|.
name|schema
argument_list|()
operator|.
name|getFields
argument_list|()
control|)
block|{
name|fields
operator|.
name|add
argument_list|(
operator|new
name|Schema
operator|.
name|Field
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|,
name|field
operator|.
name|schema
argument_list|()
argument_list|,
name|field
operator|.
name|doc
argument_list|()
argument_list|,
name|nullDefault
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|fields
operator|.
name|add
argument_list|(
operator|new
name|Schema
operator|.
name|Field
argument_list|(
name|schemaField
operator|.
name|name
argument_list|()
argument_list|,
name|schemaField
operator|.
name|schema
argument_list|()
argument_list|,
name|schemaField
operator|.
name|doc
argument_list|()
argument_list|,
name|nullDefault
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|fields
return|;
block|}
specifier|private
name|Schema
name|wrapInUnionWithNull
parameter_list|(
name|Schema
name|schema
parameter_list|)
block|{
name|Schema
name|wrappedSchema
init|=
name|schema
decl_stmt|;
switch|switch
condition|(
name|schema
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|NULL
case|:
break|break;
case|case
name|UNION
case|:
name|List
argument_list|<
name|Schema
argument_list|>
name|existingSchemas
init|=
name|removeDuplicateNullSchemas
argument_list|(
name|schema
operator|.
name|getTypes
argument_list|()
argument_list|)
decl_stmt|;
name|wrappedSchema
operator|=
name|Schema
operator|.
name|createUnion
argument_list|(
name|existingSchemas
argument_list|)
expr_stmt|;
break|break;
default|default:
name|wrappedSchema
operator|=
name|Schema
operator|.
name|createUnion
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|Schema
operator|.
name|create
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|NULL
argument_list|)
argument_list|,
name|schema
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|wrappedSchema
return|;
block|}
specifier|private
name|List
argument_list|<
name|Schema
argument_list|>
name|removeDuplicateNullSchemas
parameter_list|(
name|List
argument_list|<
name|Schema
argument_list|>
name|childSchemas
parameter_list|)
block|{
name|List
argument_list|<
name|Schema
argument_list|>
name|prunedSchemas
init|=
operator|new
name|ArrayList
argument_list|<
name|Schema
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|isNullPresent
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Schema
name|schema
range|:
name|childSchemas
control|)
block|{
if|if
condition|(
name|schema
operator|.
name|getType
argument_list|()
operator|==
name|Schema
operator|.
name|Type
operator|.
name|NULL
condition|)
block|{
name|isNullPresent
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|prunedSchemas
operator|.
name|add
argument_list|(
name|schema
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|isNullPresent
condition|)
block|{
name|prunedSchemas
operator|.
name|add
argument_list|(
literal|0
argument_list|,
name|Schema
operator|.
name|create
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|NULL
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|prunedSchemas
return|;
block|}
block|}
end_class

end_unit

