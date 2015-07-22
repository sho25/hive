begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ql
operator|.
name|io
operator|.
name|parquet
operator|.
name|convert
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|parquet
operator|.
name|serde
operator|.
name|ParquetHiveSerDe
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
name|TypeInfoFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|ConversionPatterns
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|GroupType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|MessageType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|OriginalType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|PrimitiveType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|PrimitiveType
operator|.
name|PrimitiveTypeName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
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
name|parquet
operator|.
name|schema
operator|.
name|Type
operator|.
name|Repetition
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|Types
import|;
end_import

begin_class
specifier|public
class|class
name|HiveSchemaConverter
block|{
specifier|public
specifier|static
name|MessageType
name|convert
parameter_list|(
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|,
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
parameter_list|)
block|{
specifier|final
name|MessageType
name|schema
init|=
operator|new
name|MessageType
argument_list|(
literal|"hive_schema"
argument_list|,
name|convertTypes
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|schema
return|;
block|}
specifier|private
specifier|static
name|Type
index|[]
name|convertTypes
parameter_list|(
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|,
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
parameter_list|)
block|{
if|if
condition|(
name|columnNames
operator|.
name|size
argument_list|()
operator|!=
name|columnTypes
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Mismatched Hive columns and types. Hive columns names"
operator|+
literal|" found : "
operator|+
name|columnNames
operator|+
literal|" . And Hive types found : "
operator|+
name|columnTypes
argument_list|)
throw|;
block|}
specifier|final
name|Type
index|[]
name|types
init|=
operator|new
name|Type
index|[
name|columnNames
operator|.
name|size
argument_list|()
index|]
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
name|types
index|[
name|i
index|]
operator|=
name|convertType
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
argument_list|)
expr_stmt|;
block|}
return|return
name|types
return|;
block|}
specifier|private
specifier|static
name|Type
name|convertType
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
return|return
name|convertType
argument_list|(
name|name
argument_list|,
name|typeInfo
argument_list|,
name|Repetition
operator|.
name|OPTIONAL
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Type
name|convertType
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|TypeInfo
name|typeInfo
parameter_list|,
specifier|final
name|Repetition
name|repetition
parameter_list|)
block|{
if|if
condition|(
name|typeInfo
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|Category
operator|.
name|PRIMITIVE
argument_list|)
condition|)
block|{
if|if
condition|(
name|typeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
condition|)
block|{
return|return
name|Types
operator|.
name|primitive
argument_list|(
name|PrimitiveTypeName
operator|.
name|BINARY
argument_list|,
name|repetition
argument_list|)
operator|.
name|as
argument_list|(
name|OriginalType
operator|.
name|UTF8
argument_list|)
operator|.
name|named
argument_list|(
name|name
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|)
operator|||
name|typeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|shortTypeInfo
argument_list|)
operator|||
name|typeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|byteTypeInfo
argument_list|)
condition|)
block|{
return|return
name|Types
operator|.
name|primitive
argument_list|(
name|PrimitiveTypeName
operator|.
name|INT32
argument_list|,
name|repetition
argument_list|)
operator|.
name|named
argument_list|(
name|name
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|)
condition|)
block|{
return|return
name|Types
operator|.
name|primitive
argument_list|(
name|PrimitiveTypeName
operator|.
name|INT64
argument_list|,
name|repetition
argument_list|)
operator|.
name|named
argument_list|(
name|name
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|)
condition|)
block|{
return|return
name|Types
operator|.
name|primitive
argument_list|(
name|PrimitiveTypeName
operator|.
name|DOUBLE
argument_list|,
name|repetition
argument_list|)
operator|.
name|named
argument_list|(
name|name
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|floatTypeInfo
argument_list|)
condition|)
block|{
return|return
name|Types
operator|.
name|primitive
argument_list|(
name|PrimitiveTypeName
operator|.
name|FLOAT
argument_list|,
name|repetition
argument_list|)
operator|.
name|named
argument_list|(
name|name
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|)
condition|)
block|{
return|return
name|Types
operator|.
name|primitive
argument_list|(
name|PrimitiveTypeName
operator|.
name|BOOLEAN
argument_list|,
name|repetition
argument_list|)
operator|.
name|named
argument_list|(
name|name
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|binaryTypeInfo
argument_list|)
condition|)
block|{
return|return
name|Types
operator|.
name|primitive
argument_list|(
name|PrimitiveTypeName
operator|.
name|BINARY
argument_list|,
name|repetition
argument_list|)
operator|.
name|named
argument_list|(
name|name
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|timestampTypeInfo
argument_list|)
condition|)
block|{
return|return
name|Types
operator|.
name|primitive
argument_list|(
name|PrimitiveTypeName
operator|.
name|INT96
argument_list|,
name|repetition
argument_list|)
operator|.
name|named
argument_list|(
name|name
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|voidTypeInfo
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Void type not implemented"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|getTypeName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|startsWith
argument_list|(
name|serdeConstants
operator|.
name|CHAR_TYPE_NAME
argument_list|)
condition|)
block|{
return|return
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|BINARY
argument_list|)
operator|.
name|as
argument_list|(
name|OriginalType
operator|.
name|UTF8
argument_list|)
operator|.
name|named
argument_list|(
name|name
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|getTypeName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|startsWith
argument_list|(
name|serdeConstants
operator|.
name|VARCHAR_TYPE_NAME
argument_list|)
condition|)
block|{
return|return
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|BINARY
argument_list|)
operator|.
name|as
argument_list|(
name|OriginalType
operator|.
name|UTF8
argument_list|)
operator|.
name|named
argument_list|(
name|name
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|instanceof
name|DecimalTypeInfo
condition|)
block|{
name|DecimalTypeInfo
name|decimalTypeInfo
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|int
name|prec
init|=
name|decimalTypeInfo
operator|.
name|precision
argument_list|()
decl_stmt|;
name|int
name|scale
init|=
name|decimalTypeInfo
operator|.
name|scale
argument_list|()
decl_stmt|;
name|int
name|bytes
init|=
name|ParquetHiveSerDe
operator|.
name|PRECISION_TO_BYTE_COUNT
index|[
name|prec
operator|-
literal|1
index|]
decl_stmt|;
return|return
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|FIXED_LEN_BYTE_ARRAY
argument_list|)
operator|.
name|length
argument_list|(
name|bytes
argument_list|)
operator|.
name|as
argument_list|(
name|OriginalType
operator|.
name|DECIMAL
argument_list|)
operator|.
name|scale
argument_list|(
name|scale
argument_list|)
operator|.
name|precision
argument_list|(
name|prec
argument_list|)
operator|.
name|named
argument_list|(
name|name
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|dateTypeInfo
argument_list|)
condition|)
block|{
return|return
name|Types
operator|.
name|primitive
argument_list|(
name|PrimitiveTypeName
operator|.
name|INT32
argument_list|,
name|repetition
argument_list|)
operator|.
name|as
argument_list|(
name|OriginalType
operator|.
name|DATE
argument_list|)
operator|.
name|named
argument_list|(
name|name
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|unknownTypeInfo
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unknown type not implemented"
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown type: "
operator|+
name|typeInfo
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|Category
operator|.
name|LIST
argument_list|)
condition|)
block|{
return|return
name|convertArrayType
argument_list|(
name|name
argument_list|,
operator|(
name|ListTypeInfo
operator|)
name|typeInfo
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|Category
operator|.
name|STRUCT
argument_list|)
condition|)
block|{
return|return
name|convertStructType
argument_list|(
name|name
argument_list|,
operator|(
name|StructTypeInfo
operator|)
name|typeInfo
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|Category
operator|.
name|MAP
argument_list|)
condition|)
block|{
return|return
name|convertMapType
argument_list|(
name|name
argument_list|,
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|typeInfo
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|Category
operator|.
name|UNION
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Union type not implemented"
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown type: "
operator|+
name|typeInfo
argument_list|)
throw|;
block|}
block|}
comment|// An optional group containing a repeated anonymous group "bag", containing
comment|// 1 anonymous element "array_element"
specifier|private
specifier|static
name|GroupType
name|convertArrayType
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|ListTypeInfo
name|typeInfo
parameter_list|)
block|{
specifier|final
name|TypeInfo
name|subType
init|=
name|typeInfo
operator|.
name|getListElementTypeInfo
argument_list|()
decl_stmt|;
return|return
name|listWrapper
argument_list|(
name|name
argument_list|,
name|OriginalType
operator|.
name|LIST
argument_list|,
operator|new
name|GroupType
argument_list|(
name|Repetition
operator|.
name|REPEATED
argument_list|,
name|ParquetHiveSerDe
operator|.
name|ARRAY
operator|.
name|toString
argument_list|()
argument_list|,
name|convertType
argument_list|(
literal|"array_element"
argument_list|,
name|subType
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|// An optional group containing multiple elements
specifier|private
specifier|static
name|GroupType
name|convertStructType
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|StructTypeInfo
name|typeInfo
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
name|typeInfo
operator|.
name|getAllStructFieldNames
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|typeInfo
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
decl_stmt|;
return|return
operator|new
name|GroupType
argument_list|(
name|Repetition
operator|.
name|OPTIONAL
argument_list|,
name|name
argument_list|,
name|convertTypes
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|)
argument_list|)
return|;
block|}
comment|// An optional group containing a repeated anonymous group "map", containing
comment|// 2 elements: "key", "value"
specifier|private
specifier|static
name|GroupType
name|convertMapType
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|MapTypeInfo
name|typeInfo
parameter_list|)
block|{
specifier|final
name|Type
name|keyType
init|=
name|convertType
argument_list|(
name|ParquetHiveSerDe
operator|.
name|MAP_KEY
operator|.
name|toString
argument_list|()
argument_list|,
name|typeInfo
operator|.
name|getMapKeyTypeInfo
argument_list|()
argument_list|,
name|Repetition
operator|.
name|REQUIRED
argument_list|)
decl_stmt|;
specifier|final
name|Type
name|valueType
init|=
name|convertType
argument_list|(
name|ParquetHiveSerDe
operator|.
name|MAP_VALUE
operator|.
name|toString
argument_list|()
argument_list|,
name|typeInfo
operator|.
name|getMapValueTypeInfo
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|ConversionPatterns
operator|.
name|mapType
argument_list|(
name|Repetition
operator|.
name|OPTIONAL
argument_list|,
name|name
argument_list|,
name|keyType
argument_list|,
name|valueType
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|GroupType
name|listWrapper
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|OriginalType
name|originalType
parameter_list|,
specifier|final
name|GroupType
name|groupType
parameter_list|)
block|{
return|return
operator|new
name|GroupType
argument_list|(
name|Repetition
operator|.
name|OPTIONAL
argument_list|,
name|name
argument_list|,
name|originalType
argument_list|,
name|groupType
argument_list|)
return|;
block|}
block|}
end_class

end_unit

