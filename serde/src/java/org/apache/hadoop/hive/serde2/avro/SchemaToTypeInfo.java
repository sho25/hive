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
name|BOOLEAN
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
name|DOUBLE
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
name|FLOAT
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
name|INT
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
name|LONG
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
name|NULL
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
name|STRING
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Hashtable
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
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|typeinfo
operator|.
name|HiveDecimalUtils
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

begin_comment
comment|/**  * Convert an Avro Schema to a Hive TypeInfo  */
end_comment

begin_class
class|class
name|SchemaToTypeInfo
block|{
comment|// Conversion of Avro primitive types to Hive primitive types
comment|// Avro             Hive
comment|// Null
comment|// boolean          boolean    check
comment|// int              int        check
comment|// long             bigint     check
comment|// float            double     check
comment|// double           double     check
comment|// bytes            binary     check
comment|// fixed            binary     check
comment|// string           string     check
comment|//                  tinyint
comment|//                  smallint
comment|// Map of Avro's primitive types to Hives (for those that are supported by both)
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|Schema
operator|.
name|Type
argument_list|,
name|TypeInfo
argument_list|>
name|primitiveTypeToTypeInfo
init|=
name|initTypeMap
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|Schema
operator|.
name|Type
argument_list|,
name|TypeInfo
argument_list|>
name|initTypeMap
parameter_list|()
block|{
name|Map
argument_list|<
name|Schema
operator|.
name|Type
argument_list|,
name|TypeInfo
argument_list|>
name|theMap
init|=
operator|new
name|Hashtable
argument_list|<
name|Schema
operator|.
name|Type
argument_list|,
name|TypeInfo
argument_list|>
argument_list|()
decl_stmt|;
name|theMap
operator|.
name|put
argument_list|(
name|NULL
argument_list|,
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"void"
argument_list|)
argument_list|)
expr_stmt|;
name|theMap
operator|.
name|put
argument_list|(
name|BOOLEAN
argument_list|,
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"boolean"
argument_list|)
argument_list|)
expr_stmt|;
name|theMap
operator|.
name|put
argument_list|(
name|INT
argument_list|,
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"int"
argument_list|)
argument_list|)
expr_stmt|;
name|theMap
operator|.
name|put
argument_list|(
name|LONG
argument_list|,
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"bigint"
argument_list|)
argument_list|)
expr_stmt|;
name|theMap
operator|.
name|put
argument_list|(
name|FLOAT
argument_list|,
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"float"
argument_list|)
argument_list|)
expr_stmt|;
name|theMap
operator|.
name|put
argument_list|(
name|DOUBLE
argument_list|,
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"double"
argument_list|)
argument_list|)
expr_stmt|;
name|theMap
operator|.
name|put
argument_list|(
name|BYTES
argument_list|,
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"binary"
argument_list|)
argument_list|)
expr_stmt|;
name|theMap
operator|.
name|put
argument_list|(
name|FIXED
argument_list|,
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"binary"
argument_list|)
argument_list|)
expr_stmt|;
name|theMap
operator|.
name|put
argument_list|(
name|STRING
argument_list|,
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"string"
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|theMap
argument_list|)
return|;
block|}
comment|/**    * Generate a list of of TypeInfos from an Avro schema.  This method is    * currently public due to some weirdness in deserializing unions, but    * will be made private once that is resolved.    * @param schema Schema to generate field types for    * @return List of TypeInfos, each element of which is a TypeInfo derived    *         from the schema.    * @throws AvroSerdeException for problems during conversion.    */
specifier|public
specifier|static
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|generateColumnTypes
parameter_list|(
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
name|List
argument_list|<
name|Schema
operator|.
name|Field
argument_list|>
name|fields
init|=
name|schema
operator|.
name|getFields
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|types
init|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|(
name|fields
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Schema
operator|.
name|Field
name|field
range|:
name|fields
control|)
block|{
name|types
operator|.
name|add
argument_list|(
name|generateTypeInfo
argument_list|(
name|field
operator|.
name|schema
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|types
return|;
block|}
specifier|static
name|InstanceCache
argument_list|<
name|Schema
argument_list|,
name|TypeInfo
argument_list|>
name|typeInfoCache
init|=
operator|new
name|InstanceCache
argument_list|<
name|Schema
argument_list|,
name|TypeInfo
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|TypeInfo
name|makeInstance
parameter_list|(
name|Schema
name|s
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
return|return
name|generateTypeInfoWorker
argument_list|(
name|s
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|/**    * Convert an Avro Schema into an equivalent Hive TypeInfo.    * @param schema to record. Must be of record type.    * @return TypeInfo matching the Avro schema    * @throws AvroSerdeException for any problems during conversion.    */
specifier|public
specifier|static
name|TypeInfo
name|generateTypeInfo
parameter_list|(
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
comment|// For bytes type, it can be mapped to decimal.
name|Schema
operator|.
name|Type
name|type
init|=
name|schema
operator|.
name|getType
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|==
name|Schema
operator|.
name|Type
operator|.
name|BYTES
operator|&&
name|AvroSerDe
operator|.
name|DECIMAL_TYPE_NAME
operator|.
name|equalsIgnoreCase
argument_list|(
name|schema
operator|.
name|getProp
argument_list|(
name|AvroSerDe
operator|.
name|AVRO_PROP_LOGICAL_TYPE
argument_list|)
argument_list|)
condition|)
block|{
name|int
name|precision
init|=
literal|0
decl_stmt|;
name|int
name|scale
init|=
literal|0
decl_stmt|;
try|try
block|{
name|precision
operator|=
name|schema
operator|.
name|getJsonProp
argument_list|(
name|AvroSerDe
operator|.
name|AVRO_PROP_PRECISION
argument_list|)
operator|.
name|getValueAsInt
argument_list|()
expr_stmt|;
name|scale
operator|=
name|schema
operator|.
name|getJsonProp
argument_list|(
name|AvroSerDe
operator|.
name|AVRO_PROP_SCALE
argument_list|)
operator|.
name|getValueAsInt
argument_list|(
literal|0
argument_list|)
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
name|schema
argument_list|,
name|ex
argument_list|)
throw|;
block|}
try|try
block|{
name|HiveDecimalUtils
operator|.
name|validateParameter
argument_list|(
name|precision
argument_list|,
name|scale
argument_list|)
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
literal|"Invalid precision or scale for decimal type"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
return|return
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
name|precision
argument_list|,
name|scale
argument_list|)
return|;
block|}
return|return
name|typeInfoCache
operator|.
name|retrieve
argument_list|(
name|schema
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TypeInfo
name|generateTypeInfoWorker
parameter_list|(
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
comment|// Avro requires NULLable types to be defined as unions of some type T
comment|// and NULL.  This is annoying and we're going to hide it from the user.
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
return|return
name|generateTypeInfo
argument_list|(
name|AvroSerdeUtils
operator|.
name|getOtherTypeFromNullableType
argument_list|(
name|schema
argument_list|)
argument_list|)
return|;
block|}
name|Schema
operator|.
name|Type
name|type
init|=
name|schema
operator|.
name|getType
argument_list|()
decl_stmt|;
if|if
condition|(
name|primitiveTypeToTypeInfo
operator|.
name|containsKey
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|primitiveTypeToTypeInfo
operator|.
name|get
argument_list|(
name|type
argument_list|)
return|;
block|}
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|RECORD
case|:
return|return
name|generateRecordTypeInfo
argument_list|(
name|schema
argument_list|)
return|;
case|case
name|MAP
case|:
return|return
name|generateMapTypeInfo
argument_list|(
name|schema
argument_list|)
return|;
case|case
name|ARRAY
case|:
return|return
name|generateArrayTypeInfo
argument_list|(
name|schema
argument_list|)
return|;
case|case
name|UNION
case|:
return|return
name|generateUnionTypeInfo
argument_list|(
name|schema
argument_list|)
return|;
case|case
name|ENUM
case|:
return|return
name|generateEnumTypeInfo
argument_list|(
name|schema
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|AvroSerdeException
argument_list|(
literal|"Do not yet support: "
operator|+
name|schema
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|TypeInfo
name|generateRecordTypeInfo
parameter_list|(
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
assert|assert
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
name|RECORD
argument_list|)
assert|;
name|List
argument_list|<
name|Schema
operator|.
name|Field
argument_list|>
name|fields
init|=
name|schema
operator|.
name|getFields
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|fields
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|typeInfos
init|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|(
name|fields
operator|.
name|size
argument_list|()
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
name|fields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|fieldNames
operator|.
name|add
argument_list|(
name|i
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|typeInfos
operator|.
name|add
argument_list|(
name|i
argument_list|,
name|generateTypeInfo
argument_list|(
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|schema
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|TypeInfoFactory
operator|.
name|getStructTypeInfo
argument_list|(
name|fieldNames
argument_list|,
name|typeInfos
argument_list|)
return|;
block|}
comment|/**    * Generate a TypeInfo for an Avro Map.  This is made slightly simpler in that    * Avro only allows maps with strings for keys.    */
specifier|private
specifier|static
name|TypeInfo
name|generateMapTypeInfo
parameter_list|(
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
assert|assert
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
name|MAP
argument_list|)
assert|;
name|Schema
name|valueType
init|=
name|schema
operator|.
name|getValueType
argument_list|()
decl_stmt|;
name|TypeInfo
name|ti
init|=
name|generateTypeInfo
argument_list|(
name|valueType
argument_list|)
decl_stmt|;
return|return
name|TypeInfoFactory
operator|.
name|getMapTypeInfo
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"string"
argument_list|)
argument_list|,
name|ti
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TypeInfo
name|generateArrayTypeInfo
parameter_list|(
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
assert|assert
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
name|ARRAY
argument_list|)
assert|;
name|Schema
name|itemsType
init|=
name|schema
operator|.
name|getElementType
argument_list|()
decl_stmt|;
name|TypeInfo
name|itemsTypeInfo
init|=
name|generateTypeInfo
argument_list|(
name|itemsType
argument_list|)
decl_stmt|;
return|return
name|TypeInfoFactory
operator|.
name|getListTypeInfo
argument_list|(
name|itemsTypeInfo
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|TypeInfo
name|generateUnionTypeInfo
parameter_list|(
name|Schema
name|schema
parameter_list|)
throws|throws
name|AvroSerdeException
block|{
assert|assert
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
name|UNION
argument_list|)
assert|;
name|List
argument_list|<
name|Schema
argument_list|>
name|types
init|=
name|schema
operator|.
name|getTypes
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|typeInfos
init|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|(
name|types
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Schema
name|type
range|:
name|types
control|)
block|{
name|typeInfos
operator|.
name|add
argument_list|(
name|generateTypeInfo
argument_list|(
name|type
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|TypeInfoFactory
operator|.
name|getUnionTypeInfo
argument_list|(
name|typeInfos
argument_list|)
return|;
block|}
comment|// Hive doesn't have an Enum type, so we're going to treat them as Strings.
comment|// During the deserialize/serialize stage we'll check for enumness and
comment|// convert as such.
specifier|private
specifier|static
name|TypeInfo
name|generateEnumTypeInfo
parameter_list|(
name|Schema
name|schema
parameter_list|)
block|{
assert|assert
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
name|ENUM
argument_list|)
assert|;
return|return
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"string"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

