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
name|ArrayList
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
name|read
operator|.
name|DataWritableReadSupport
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
name|typeinfo
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
name|io
operator|.
name|ArrayWritable
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
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|Converter
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
name|Type
import|;
end_import

begin_comment
comment|/**  *  * A MapWritableGroupConverter, real converter between hive and parquet types recursively for complex types.  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveStructConverter
extends|extends
name|HiveGroupConverter
block|{
specifier|private
specifier|final
name|int
name|totalFieldCount
decl_stmt|;
specifier|private
name|Converter
index|[]
name|converters
decl_stmt|;
specifier|private
specifier|final
name|ConverterParent
name|parent
decl_stmt|;
specifier|private
specifier|final
name|int
name|index
decl_stmt|;
specifier|private
name|Writable
index|[]
name|writables
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Repeated
argument_list|>
name|repeatedConverters
decl_stmt|;
specifier|private
name|boolean
name|reuseWritableArray
init|=
literal|false
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|hiveFieldNames
decl_stmt|;
specifier|private
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|hiveFieldTypeInfos
decl_stmt|;
specifier|public
name|HiveStructConverter
parameter_list|(
specifier|final
name|GroupType
name|requestedSchema
parameter_list|,
specifier|final
name|GroupType
name|tableSchema
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metadata
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
name|setMetadata
argument_list|(
name|metadata
argument_list|)
expr_stmt|;
name|this
operator|.
name|reuseWritableArray
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|writables
operator|=
operator|new
name|Writable
index|[
name|tableSchema
operator|.
name|getFieldCount
argument_list|()
index|]
expr_stmt|;
name|this
operator|.
name|parent
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|index
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|totalFieldCount
operator|=
name|tableSchema
operator|.
name|getFieldCount
argument_list|()
expr_stmt|;
name|init
argument_list|(
name|requestedSchema
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|,
name|tableSchema
argument_list|,
name|hiveTypeInfo
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveStructConverter
parameter_list|(
specifier|final
name|GroupType
name|groupType
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
name|this
argument_list|(
name|groupType
argument_list|,
name|parent
argument_list|,
name|index
argument_list|,
name|groupType
argument_list|,
name|hiveTypeInfo
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveStructConverter
parameter_list|(
specifier|final
name|GroupType
name|selectedGroupType
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|GroupType
name|containingGroupType
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|totalFieldCount
operator|=
name|containingGroupType
operator|.
name|getFieldCount
argument_list|()
expr_stmt|;
name|init
argument_list|(
name|selectedGroupType
argument_list|,
name|parent
argument_list|,
name|index
argument_list|,
name|containingGroupType
argument_list|,
name|hiveTypeInfo
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|init
parameter_list|(
specifier|final
name|GroupType
name|selectedGroupType
parameter_list|,
specifier|final
name|ConverterParent
name|parent
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|GroupType
name|containingGroupType
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
if|if
condition|(
name|parent
operator|!=
literal|null
condition|)
block|{
name|setMetadata
argument_list|(
name|parent
operator|.
name|getMetadata
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|final
name|int
name|selectedFieldCount
init|=
name|selectedGroupType
operator|.
name|getFieldCount
argument_list|()
decl_stmt|;
name|converters
operator|=
operator|new
name|Converter
index|[
name|selectedFieldCount
index|]
expr_stmt|;
name|this
operator|.
name|repeatedConverters
operator|=
operator|new
name|ArrayList
argument_list|<
name|Repeated
argument_list|>
argument_list|()
expr_stmt|;
if|if
condition|(
name|hiveTypeInfo
operator|!=
literal|null
operator|&&
name|hiveTypeInfo
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
name|STRUCT
argument_list|)
condition|)
block|{
name|this
operator|.
name|hiveFieldNames
operator|=
operator|(
operator|(
name|StructTypeInfo
operator|)
name|hiveTypeInfo
operator|)
operator|.
name|getAllStructFieldNames
argument_list|()
expr_stmt|;
name|this
operator|.
name|hiveFieldTypeInfos
operator|=
operator|(
operator|(
name|StructTypeInfo
operator|)
name|hiveTypeInfo
operator|)
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|Type
argument_list|>
name|selectedFields
init|=
name|selectedGroupType
operator|.
name|getFields
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
name|selectedFieldCount
condition|;
name|i
operator|++
control|)
block|{
name|Type
name|subtype
init|=
name|selectedFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|containingGroupType
operator|.
name|getFields
argument_list|()
operator|.
name|contains
argument_list|(
name|subtype
argument_list|)
condition|)
block|{
name|int
name|fieldIndex
init|=
name|containingGroupType
operator|.
name|getFieldIndex
argument_list|(
name|subtype
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|TypeInfo
name|_hiveTypeInfo
init|=
name|getFieldTypeIgnoreCase
argument_list|(
name|hiveTypeInfo
argument_list|,
name|subtype
operator|.
name|getName
argument_list|()
argument_list|,
name|fieldIndex
argument_list|)
decl_stmt|;
name|converters
index|[
name|i
index|]
operator|=
name|getFieldConverter
argument_list|(
name|subtype
argument_list|,
name|fieldIndex
argument_list|,
name|_hiveTypeInfo
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Group type ["
operator|+
name|containingGroupType
operator|+
literal|"] does not contain requested field: "
operator|+
name|subtype
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|TypeInfo
name|getFieldTypeIgnoreCase
parameter_list|(
name|TypeInfo
name|hiveTypeInfo
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|int
name|fieldIndex
parameter_list|)
block|{
if|if
condition|(
name|hiveTypeInfo
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|hiveTypeInfo
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
name|STRUCT
argument_list|)
condition|)
block|{
return|return
name|getStructFieldTypeInfo
argument_list|(
name|fieldName
argument_list|,
name|fieldIndex
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|hiveTypeInfo
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
name|MAP
argument_list|)
condition|)
block|{
comment|//This cover the case where hive table may have map<key, value> but the data file is
comment|// of type array<struct<value1, value2>>
comment|//Using index in place of type name.
if|if
condition|(
name|fieldIndex
operator|==
literal|0
condition|)
block|{
return|return
operator|(
operator|(
name|MapTypeInfo
operator|)
name|hiveTypeInfo
operator|)
operator|.
name|getMapKeyTypeInfo
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|fieldIndex
operator|==
literal|1
condition|)
block|{
return|return
operator|(
operator|(
name|MapTypeInfo
operator|)
name|hiveTypeInfo
operator|)
operator|.
name|getMapValueTypeInfo
argument_list|()
return|;
block|}
else|else
block|{
comment|//Other fields are skipped for this case
return|return
literal|null
return|;
block|}
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown hive type info "
operator|+
name|hiveTypeInfo
operator|+
literal|" when searching for field "
operator|+
name|fieldName
argument_list|)
throw|;
block|}
specifier|private
name|TypeInfo
name|getStructFieldTypeInfo
parameter_list|(
name|String
name|field
parameter_list|,
name|int
name|fieldIndex
parameter_list|)
block|{
name|String
name|fieldLowerCase
init|=
name|field
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
if|if
condition|(
name|Boolean
operator|.
name|valueOf
argument_list|(
name|getMetadata
argument_list|()
operator|.
name|get
argument_list|(
name|DataWritableReadSupport
operator|.
name|PARQUET_COLUMN_INDEX_ACCESS
argument_list|)
argument_list|)
operator|&&
name|fieldIndex
operator|<
name|hiveFieldNames
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
name|hiveFieldTypeInfos
operator|.
name|get
argument_list|(
name|fieldIndex
argument_list|)
return|;
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
name|hiveFieldNames
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
name|fieldLowerCase
operator|.
name|equalsIgnoreCase
argument_list|(
name|hiveFieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
return|return
name|hiveFieldTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
return|;
block|}
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"cannot find field "
operator|+
name|field
operator|+
literal|" in "
operator|+
name|hiveFieldNames
argument_list|)
throw|;
block|}
specifier|private
name|Converter
name|getFieldConverter
parameter_list|(
name|Type
name|type
parameter_list|,
name|int
name|fieldIndex
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
name|Converter
name|converter
decl_stmt|;
if|if
condition|(
name|type
operator|.
name|isRepetition
argument_list|(
name|Type
operator|.
name|Repetition
operator|.
name|REPEATED
argument_list|)
condition|)
block|{
if|if
condition|(
name|type
operator|.
name|isPrimitive
argument_list|()
condition|)
block|{
name|converter
operator|=
operator|new
name|Repeated
operator|.
name|RepeatedPrimitiveConverter
argument_list|(
name|type
operator|.
name|asPrimitiveType
argument_list|()
argument_list|,
name|this
argument_list|,
name|fieldIndex
argument_list|,
name|hiveTypeInfo
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|converter
operator|=
operator|new
name|Repeated
operator|.
name|RepeatedGroupConverter
argument_list|(
name|type
operator|.
name|asGroupType
argument_list|()
argument_list|,
name|this
argument_list|,
name|fieldIndex
argument_list|,
name|hiveTypeInfo
operator|==
literal|null
condition|?
literal|null
else|:
operator|(
operator|(
name|ListTypeInfo
operator|)
name|hiveTypeInfo
operator|)
operator|.
name|getListElementTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|repeatedConverters
operator|.
name|add
argument_list|(
operator|(
name|Repeated
operator|)
name|converter
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|converter
operator|=
name|getConverterFromDescription
argument_list|(
name|type
argument_list|,
name|fieldIndex
argument_list|,
name|this
argument_list|,
name|hiveTypeInfo
argument_list|)
expr_stmt|;
block|}
return|return
name|converter
return|;
block|}
specifier|public
specifier|final
name|ArrayWritable
name|getCurrentArray
parameter_list|()
block|{
return|return
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|writables
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|set
parameter_list|(
name|int
name|fieldIndex
parameter_list|,
name|Writable
name|value
parameter_list|)
block|{
name|writables
index|[
name|fieldIndex
index|]
operator|=
name|value
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Converter
name|getConverter
parameter_list|(
specifier|final
name|int
name|fieldIndex
parameter_list|)
block|{
return|return
name|converters
index|[
name|fieldIndex
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
if|if
condition|(
name|reuseWritableArray
condition|)
block|{
comment|// reset the array to null values
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|writables
operator|.
name|length
condition|;
name|i
operator|+=
literal|1
control|)
block|{
name|writables
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
else|else
block|{
name|this
operator|.
name|writables
operator|=
operator|new
name|Writable
index|[
name|totalFieldCount
index|]
expr_stmt|;
block|}
for|for
control|(
name|Repeated
name|repeated
range|:
name|repeatedConverters
control|)
block|{
name|repeated
operator|.
name|parentStart
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|end
parameter_list|()
block|{
for|for
control|(
name|Repeated
name|repeated
range|:
name|repeatedConverters
control|)
block|{
name|repeated
operator|.
name|parentEnd
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|parent
operator|!=
literal|null
condition|)
block|{
name|parent
operator|.
name|set
argument_list|(
name|index
argument_list|,
name|getCurrentArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

