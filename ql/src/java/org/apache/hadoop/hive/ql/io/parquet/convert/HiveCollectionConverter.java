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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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

begin_class
specifier|public
class|class
name|HiveCollectionConverter
extends|extends
name|HiveGroupConverter
block|{
specifier|private
specifier|final
name|GroupType
name|collectionType
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
specifier|final
name|Converter
name|innerConverter
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Writable
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Writable
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|HiveGroupConverter
name|forMap
parameter_list|(
name|GroupType
name|mapType
parameter_list|,
name|ConverterParent
name|parent
parameter_list|,
name|int
name|index
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
return|return
operator|new
name|HiveCollectionConverter
argument_list|(
name|mapType
argument_list|,
name|parent
argument_list|,
name|index
argument_list|,
literal|true
comment|/* its a map */
argument_list|,
name|hiveTypeInfo
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HiveGroupConverter
name|forList
parameter_list|(
name|GroupType
name|listType
parameter_list|,
name|ConverterParent
name|parent
parameter_list|,
name|int
name|index
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
return|return
operator|new
name|HiveCollectionConverter
argument_list|(
name|listType
argument_list|,
name|parent
argument_list|,
name|index
argument_list|,
literal|false
comment|/* nUnknown hive type infoot a map */
argument_list|,
name|hiveTypeInfo
argument_list|)
return|;
block|}
specifier|private
name|HiveCollectionConverter
parameter_list|(
name|GroupType
name|collectionType
parameter_list|,
name|ConverterParent
name|parent
parameter_list|,
name|int
name|index
parameter_list|,
name|boolean
name|isMap
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
name|setMetadata
argument_list|(
name|parent
operator|.
name|getMetadata
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|collectionType
operator|=
name|collectionType
expr_stmt|;
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
name|Type
name|repeatedType
init|=
name|collectionType
operator|.
name|getType
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|isMap
condition|)
block|{
name|this
operator|.
name|innerConverter
operator|=
operator|new
name|KeyValueConverter
argument_list|(
name|repeatedType
operator|.
name|asGroupType
argument_list|()
argument_list|,
name|this
argument_list|,
name|hiveTypeInfo
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|isElementType
argument_list|(
name|repeatedType
argument_list|,
name|collectionType
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|this
operator|.
name|innerConverter
operator|=
name|getConverterFromDescription
argument_list|(
name|repeatedType
argument_list|,
literal|0
argument_list|,
name|this
argument_list|,
name|extractListCompatibleType
argument_list|(
name|hiveTypeInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|innerConverter
operator|=
operator|new
name|ElementConverter
argument_list|(
name|repeatedType
operator|.
name|asGroupType
argument_list|()
argument_list|,
name|this
argument_list|,
name|extractListCompatibleType
argument_list|(
name|hiveTypeInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|TypeInfo
name|extractListCompatibleType
parameter_list|(
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
if|if
condition|(
name|hiveTypeInfo
operator|!=
literal|null
operator|&&
name|hiveTypeInfo
operator|instanceof
name|ListTypeInfo
condition|)
block|{
return|return
operator|(
operator|(
name|ListTypeInfo
operator|)
name|hiveTypeInfo
operator|)
operator|.
name|getListElementTypeInfo
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|hiveTypeInfo
return|;
comment|//to handle map can read list of struct data (i.e. list<struct<key, value>> --> map<key,
comment|// value>)
block|}
block|}
annotation|@
name|Override
specifier|public
name|Converter
name|getConverter
parameter_list|(
name|int
name|fieldIndex
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|fieldIndex
operator|==
literal|0
argument_list|,
literal|"Invalid field index: "
operator|+
name|fieldIndex
argument_list|)
expr_stmt|;
return|return
name|innerConverter
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|list
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|end
parameter_list|()
block|{
name|parent
operator|.
name|set
argument_list|(
name|index
argument_list|,
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|list
operator|.
name|toArray
argument_list|(
operator|new
name|Writable
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|set
parameter_list|(
name|int
name|index
parameter_list|,
name|Writable
name|value
parameter_list|)
block|{
name|list
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|KeyValueConverter
extends|extends
name|HiveGroupConverter
block|{
specifier|private
specifier|final
name|HiveGroupConverter
name|parent
decl_stmt|;
specifier|private
specifier|final
name|Converter
name|keyConverter
decl_stmt|;
specifier|private
specifier|final
name|Converter
name|valueConverter
decl_stmt|;
specifier|private
name|Writable
index|[]
name|keyValue
init|=
literal|null
decl_stmt|;
specifier|public
name|KeyValueConverter
parameter_list|(
name|GroupType
name|keyValueType
parameter_list|,
name|HiveGroupConverter
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
name|setMetadata
argument_list|(
name|parent
operator|.
name|getMetadata
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|keyConverter
operator|=
name|getConverterFromDescription
argument_list|(
name|keyValueType
operator|.
name|getType
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|0
argument_list|,
name|this
argument_list|,
name|hiveTypeInfo
operator|==
literal|null
condition|?
literal|null
else|:
operator|(
operator|(
name|MapTypeInfo
operator|)
name|hiveTypeInfo
operator|)
operator|.
name|getMapKeyTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|valueConverter
operator|=
name|getConverterFromDescription
argument_list|(
name|keyValueType
operator|.
name|getType
argument_list|(
literal|1
argument_list|)
argument_list|,
literal|1
argument_list|,
name|this
argument_list|,
name|hiveTypeInfo
operator|==
literal|null
condition|?
literal|null
else|:
operator|(
operator|(
name|MapTypeInfo
operator|)
name|hiveTypeInfo
operator|)
operator|.
name|getMapValueTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
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
name|keyValue
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
name|int
name|fieldIndex
parameter_list|)
block|{
switch|switch
condition|(
name|fieldIndex
condition|)
block|{
case|case
literal|0
case|:
return|return
name|keyConverter
return|;
case|case
literal|1
case|:
return|return
name|valueConverter
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid field index for map key-value: "
operator|+
name|fieldIndex
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|this
operator|.
name|keyValue
operator|=
operator|new
name|Writable
index|[
literal|2
index|]
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|end
parameter_list|()
block|{
name|parent
operator|.
name|set
argument_list|(
literal|0
argument_list|,
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|keyValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|ElementConverter
extends|extends
name|HiveGroupConverter
block|{
specifier|private
specifier|final
name|HiveGroupConverter
name|parent
decl_stmt|;
specifier|private
specifier|final
name|Converter
name|elementConverter
decl_stmt|;
specifier|private
name|Writable
name|element
init|=
literal|null
decl_stmt|;
specifier|public
name|ElementConverter
parameter_list|(
name|GroupType
name|repeatedType
parameter_list|,
name|HiveGroupConverter
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
parameter_list|)
block|{
name|setMetadata
argument_list|(
name|parent
operator|.
name|getMetadata
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|elementConverter
operator|=
name|getConverterFromDescription
argument_list|(
name|repeatedType
operator|.
name|getType
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|0
argument_list|,
name|this
argument_list|,
name|hiveTypeInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|set
parameter_list|(
name|int
name|index
parameter_list|,
name|Writable
name|value
parameter_list|)
block|{
name|this
operator|.
name|element
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
name|int
name|i
parameter_list|)
block|{
return|return
name|elementConverter
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|this
operator|.
name|element
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|end
parameter_list|()
block|{
name|parent
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|element
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|boolean
name|isElementType
parameter_list|(
name|Type
name|repeatedType
parameter_list|,
name|String
name|parentName
parameter_list|)
block|{
if|if
condition|(
name|repeatedType
operator|.
name|isPrimitive
argument_list|()
operator|||
operator|(
name|repeatedType
operator|.
name|asGroupType
argument_list|()
operator|.
name|getFieldCount
argument_list|()
operator|!=
literal|1
operator|)
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|repeatedType
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"array"
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
comment|// existing avro data
block|}
elseif|else
if|if
condition|(
name|repeatedType
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|parentName
operator|+
literal|"_tuple"
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
comment|// existing thrift data
block|}
comment|// false for the following cases:
comment|// * name is "list", which matches the spec
comment|// * name is "bag", which indicates existing hive or pig data
comment|// * ambiguous case, which should be assumed is 3-level according to spec
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

