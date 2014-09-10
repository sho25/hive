begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/*  * This source file is based on code taken from SQLLine 1.0.2  * See SQLLine notice in LICENSE  */
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
name|Iterator
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
name|lang
operator|.
name|StringUtils
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterators
import|;
end_import

begin_class
specifier|public
class|class
name|ColumnMappings
implements|implements
name|Iterable
argument_list|<
name|ColumnMappings
operator|.
name|ColumnMapping
argument_list|>
block|{
specifier|private
specifier|final
name|int
name|keyIndex
decl_stmt|;
specifier|private
specifier|final
name|ColumnMapping
index|[]
name|columnsMapping
decl_stmt|;
specifier|public
name|ColumnMappings
parameter_list|(
name|List
argument_list|<
name|ColumnMapping
argument_list|>
name|columnMapping
parameter_list|,
name|int
name|keyIndex
parameter_list|)
block|{
name|this
operator|.
name|columnsMapping
operator|=
name|columnMapping
operator|.
name|toArray
argument_list|(
operator|new
name|ColumnMapping
index|[
name|columnMapping
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
name|this
operator|.
name|keyIndex
operator|=
name|keyIndex
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|ColumnMapping
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|Iterators
operator|.
name|forArray
argument_list|(
name|columnsMapping
argument_list|)
return|;
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|columnsMapping
operator|.
name|length
return|;
block|}
name|String
name|toNamesString
parameter_list|(
name|Properties
name|tbl
parameter_list|,
name|String
name|autogenerate
parameter_list|)
block|{
if|if
condition|(
name|autogenerate
operator|!=
literal|null
operator|&&
name|autogenerate
operator|.
name|equals
argument_list|(
literal|"true"
argument_list|)
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|HBaseSerDeHelper
operator|.
name|generateColumns
argument_list|(
name|tbl
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|columnsMapping
argument_list|)
argument_list|,
name|sb
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
return|return
name|StringUtils
operator|.
name|EMPTY
return|;
comment|// return empty string
block|}
name|String
name|toTypesString
parameter_list|(
name|Properties
name|tbl
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|autogenerate
parameter_list|)
throws|throws
name|SerDeException
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|autogenerate
operator|!=
literal|null
operator|&&
name|autogenerate
operator|.
name|equals
argument_list|(
literal|"true"
argument_list|)
condition|)
block|{
name|HBaseSerDeHelper
operator|.
name|generateColumnTypes
argument_list|(
name|tbl
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|columnsMapping
argument_list|)
argument_list|,
name|sb
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|ColumnMapping
name|colMap
range|:
name|columnsMapping
control|)
block|{
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|colMap
operator|.
name|hbaseRowKey
condition|)
block|{
comment|// the row key column becomes a STRING
name|sb
operator|.
name|append
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|colMap
operator|.
name|qualifierName
operator|==
literal|null
condition|)
block|{
comment|// a column family become a MAP
name|sb
operator|.
name|append
argument_list|(
name|serdeConstants
operator|.
name|MAP_TYPE_NAME
operator|+
literal|"<"
operator|+
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
operator|+
literal|","
operator|+
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
operator|+
literal|">"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// an individual column becomes a STRING
name|sb
operator|.
name|append
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
name|void
name|setHiveColumnDescription
parameter_list|(
name|String
name|serdeName
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
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
name|columnsMapping
operator|.
name|length
operator|!=
name|columnNames
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|serdeName
operator|+
literal|": columns has "
operator|+
name|columnNames
operator|.
name|size
argument_list|()
operator|+
literal|" elements while hbase.columns.mapping has "
operator|+
name|columnsMapping
operator|.
name|length
operator|+
literal|" elements"
operator|+
literal|" (counting the key if implicit)"
argument_list|)
throw|;
block|}
comment|// check that the mapping schema is right;
comment|// check that the "column-family:" is mapped to  Map<key,?>
comment|// where key extends LazyPrimitive<?, ?> and thus has type Category.PRIMITIVE
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
name|ColumnMapping
name|colMap
init|=
name|columnsMapping
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|colMap
operator|.
name|qualifierName
operator|==
literal|null
operator|&&
operator|!
name|colMap
operator|.
name|hbaseRowKey
condition|)
block|{
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
if|if
condition|(
operator|(
name|typeInfo
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|MAP
operator|)
operator|||
operator|(
operator|(
operator|(
name|MapTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getMapKeyTypeInfo
argument_list|()
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
operator|)
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|serdeName
operator|+
literal|": hbase column family '"
operator|+
name|colMap
operator|.
name|familyName
operator|+
literal|"' should be mapped to Map<? extends LazyPrimitive<?, ?>,?>, that is "
operator|+
literal|"the Key for the map should be of primitive type, but is mapped to "
operator|+
name|typeInfo
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|colMap
operator|.
name|columnName
operator|=
name|columnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|colMap
operator|.
name|columnType
operator|=
name|columnTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Utility method for parsing a string of the form '-,b,s,-,s:b,...' as a means of specifying    * whether to use a binary or an UTF string format to serialize and de-serialize primitive    * data types like boolean, byte, short, int, long, float, and double. This applies to    * regular columns and also to map column types which are associated with an HBase column    * family. For the map types, we apply the specification to the key or the value provided it    * is one of the above primitive types. The specifier is a colon separated value of the form    * -:s, or b:b where we have 's', 'b', or '-' on either side of the colon. 's' is for string    * format storage, 'b' is for native fixed width byte oriented storage, and '-' uses the    * table level default.    *    * @param hbaseTableDefaultStorageType - the specification associated with the table property    *        hbase.table.default.storage.type    * @throws SerDeException on parse error.    */
name|void
name|parseColumnStorageTypes
parameter_list|(
name|String
name|hbaseTableDefaultStorageType
parameter_list|)
throws|throws
name|SerDeException
block|{
name|boolean
name|tableBinaryStorage
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|hbaseTableDefaultStorageType
operator|!=
literal|null
operator|&&
operator|!
literal|""
operator|.
name|equals
argument_list|(
name|hbaseTableDefaultStorageType
argument_list|)
condition|)
block|{
if|if
condition|(
name|hbaseTableDefaultStorageType
operator|.
name|equals
argument_list|(
literal|"binary"
argument_list|)
condition|)
block|{
name|tableBinaryStorage
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|hbaseTableDefaultStorageType
operator|.
name|equals
argument_list|(
literal|"string"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Error: "
operator|+
name|HBaseSerDe
operator|.
name|HBASE_TABLE_DEFAULT_STORAGE_TYPE
operator|+
literal|" parameter must be specified as"
operator|+
literal|" 'string' or 'binary'; '"
operator|+
name|hbaseTableDefaultStorageType
operator|+
literal|"' is not a valid specification for this table/serde property."
argument_list|)
throw|;
block|}
block|}
comment|// parse the string to determine column level storage type for primitive types
comment|// 's' is for variable length string format storage
comment|// 'b' is for fixed width binary storage of bytes
comment|// '-' is for table storage type, which defaults to UTF8 string
comment|// string data is always stored in the default escaped storage format; the data types
comment|// byte, short, int, long, float, and double have a binary byte oriented storage option
for|for
control|(
name|ColumnMapping
name|colMap
range|:
name|columnsMapping
control|)
block|{
name|TypeInfo
name|colType
init|=
name|colMap
operator|.
name|columnType
decl_stmt|;
name|String
name|mappingSpec
init|=
name|colMap
operator|.
name|mappingSpec
decl_stmt|;
name|String
index|[]
name|mapInfo
init|=
name|mappingSpec
operator|.
name|split
argument_list|(
literal|"#"
argument_list|)
decl_stmt|;
name|String
index|[]
name|storageInfo
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|mapInfo
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|storageInfo
operator|=
name|mapInfo
index|[
literal|1
index|]
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|storageInfo
operator|==
literal|null
condition|)
block|{
comment|// use the table default storage specification
if|if
condition|(
name|colType
operator|.
name|getCategory
argument_list|()
operator|==
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
if|if
condition|(
operator|!
name|colType
operator|.
name|getTypeName
argument_list|()
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
condition|)
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
name|tableBinaryStorage
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|colType
operator|.
name|getCategory
argument_list|()
operator|==
name|ObjectInspector
operator|.
name|Category
operator|.
name|MAP
condition|)
block|{
name|TypeInfo
name|keyTypeInfo
init|=
operator|(
operator|(
name|MapTypeInfo
operator|)
name|colType
operator|)
operator|.
name|getMapKeyTypeInfo
argument_list|()
decl_stmt|;
name|TypeInfo
name|valueTypeInfo
init|=
operator|(
operator|(
name|MapTypeInfo
operator|)
name|colType
operator|)
operator|.
name|getMapValueTypeInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|keyTypeInfo
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
operator|!
name|keyTypeInfo
operator|.
name|getTypeName
argument_list|()
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
condition|)
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
name|tableBinaryStorage
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|valueTypeInfo
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
operator|!
name|valueTypeInfo
operator|.
name|getTypeName
argument_list|()
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
condition|)
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
name|tableBinaryStorage
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|storageInfo
operator|.
name|length
operator|==
literal|1
condition|)
block|{
comment|// we have a storage specification for a primitive column type
name|String
name|storageOption
init|=
name|storageInfo
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
operator|(
name|colType
operator|.
name|getCategory
argument_list|()
operator|==
name|ObjectInspector
operator|.
name|Category
operator|.
name|MAP
operator|)
operator|||
operator|!
operator|(
name|storageOption
operator|.
name|equals
argument_list|(
literal|"-"
argument_list|)
operator|||
literal|"string"
operator|.
name|startsWith
argument_list|(
name|storageOption
argument_list|)
operator|||
literal|"binary"
operator|.
name|startsWith
argument_list|(
name|storageOption
argument_list|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Error: A column storage specification is one of the following:"
operator|+
literal|" '-', a prefix of 'string', or a prefix of 'binary'. "
operator|+
name|storageOption
operator|+
literal|" is not a valid storage option specification for "
operator|+
name|colMap
operator|.
name|columnName
argument_list|)
throw|;
block|}
if|if
condition|(
name|colType
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
operator|!
name|colType
operator|.
name|getTypeName
argument_list|()
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
condition|)
block|{
if|if
condition|(
literal|"-"
operator|.
name|equals
argument_list|(
name|storageOption
argument_list|)
condition|)
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
name|tableBinaryStorage
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"binary"
operator|.
name|startsWith
argument_list|(
name|storageOption
argument_list|)
condition|)
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|storageInfo
operator|.
name|length
operator|==
literal|2
condition|)
block|{
comment|// we have a storage specification for a map column type
name|String
name|keyStorage
init|=
name|storageInfo
index|[
literal|0
index|]
decl_stmt|;
name|String
name|valStorage
init|=
name|storageInfo
index|[
literal|1
index|]
decl_stmt|;
if|if
condition|(
operator|(
name|colType
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|MAP
operator|)
operator|||
operator|!
operator|(
name|keyStorage
operator|.
name|equals
argument_list|(
literal|"-"
argument_list|)
operator|||
literal|"string"
operator|.
name|startsWith
argument_list|(
name|keyStorage
argument_list|)
operator|||
literal|"binary"
operator|.
name|startsWith
argument_list|(
name|keyStorage
argument_list|)
operator|)
operator|||
operator|!
operator|(
name|valStorage
operator|.
name|equals
argument_list|(
literal|"-"
argument_list|)
operator|||
literal|"string"
operator|.
name|startsWith
argument_list|(
name|valStorage
argument_list|)
operator|||
literal|"binary"
operator|.
name|startsWith
argument_list|(
name|valStorage
argument_list|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Error: To specify a valid column storage type for a Map"
operator|+
literal|" column, use any two specifiers from '-', a prefix of 'string', "
operator|+
literal|" and a prefix of 'binary' separated by a ':'."
operator|+
literal|" Valid examples are '-:-', 's:b', etc. They specify the storage type for the"
operator|+
literal|" key and value parts of the Map<?,?> respectively."
operator|+
literal|" Invalid storage specification for column "
operator|+
name|colMap
operator|.
name|columnName
operator|+
literal|"; "
operator|+
name|storageInfo
index|[
literal|0
index|]
operator|+
literal|":"
operator|+
name|storageInfo
index|[
literal|1
index|]
argument_list|)
throw|;
block|}
name|TypeInfo
name|keyTypeInfo
init|=
operator|(
operator|(
name|MapTypeInfo
operator|)
name|colType
operator|)
operator|.
name|getMapKeyTypeInfo
argument_list|()
decl_stmt|;
name|TypeInfo
name|valueTypeInfo
init|=
operator|(
operator|(
name|MapTypeInfo
operator|)
name|colType
operator|)
operator|.
name|getMapValueTypeInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|keyTypeInfo
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
operator|!
name|keyTypeInfo
operator|.
name|getTypeName
argument_list|()
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
condition|)
block|{
if|if
condition|(
name|keyStorage
operator|.
name|equals
argument_list|(
literal|"-"
argument_list|)
condition|)
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
name|tableBinaryStorage
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"binary"
operator|.
name|startsWith
argument_list|(
name|keyStorage
argument_list|)
condition|)
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|valueTypeInfo
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
operator|!
name|valueTypeInfo
operator|.
name|getTypeName
argument_list|()
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
condition|)
block|{
if|if
condition|(
name|valStorage
operator|.
name|equals
argument_list|(
literal|"-"
argument_list|)
condition|)
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
name|tableBinaryStorage
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"binary"
operator|.
name|startsWith
argument_list|(
name|valStorage
argument_list|)
condition|)
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|colMap
operator|.
name|binaryStorage
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|colMap
operator|.
name|binaryStorage
operator|.
name|size
argument_list|()
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Error: In parsing the storage specification for column "
operator|+
name|colMap
operator|.
name|columnName
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|// error in storage specification
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Error: "
operator|+
name|HBaseSerDe
operator|.
name|HBASE_COLUMNS_MAPPING
operator|+
literal|" storage specification "
operator|+
name|mappingSpec
operator|+
literal|" is not valid for column: "
operator|+
name|colMap
operator|.
name|columnName
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
name|ColumnMapping
name|getKeyMapping
parameter_list|()
block|{
return|return
name|columnsMapping
index|[
name|keyIndex
index|]
return|;
block|}
specifier|public
name|int
name|getKeyIndex
parameter_list|()
block|{
return|return
name|keyIndex
return|;
block|}
specifier|public
name|ColumnMapping
index|[]
name|getColumnsMapping
parameter_list|()
block|{
return|return
name|columnsMapping
return|;
block|}
comment|/**    * Represents a mapping from a single Hive column to an HBase column qualifier, column family or row key.    */
comment|// todo use final fields
specifier|public
specifier|static
class|class
name|ColumnMapping
block|{
name|ColumnMapping
parameter_list|()
block|{
name|binaryStorage
operator|=
operator|new
name|ArrayList
argument_list|<
name|Boolean
argument_list|>
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
name|String
name|columnName
decl_stmt|;
name|TypeInfo
name|columnType
decl_stmt|;
name|String
name|familyName
decl_stmt|;
name|String
name|qualifierName
decl_stmt|;
name|byte
index|[]
name|familyNameBytes
decl_stmt|;
name|byte
index|[]
name|qualifierNameBytes
decl_stmt|;
name|List
argument_list|<
name|Boolean
argument_list|>
name|binaryStorage
decl_stmt|;
name|boolean
name|hbaseRowKey
decl_stmt|;
name|String
name|mappingSpec
decl_stmt|;
name|String
name|qualifierPrefix
decl_stmt|;
name|byte
index|[]
name|qualifierPrefixBytes
decl_stmt|;
specifier|public
name|String
name|getColumnName
parameter_list|()
block|{
return|return
name|columnName
return|;
block|}
specifier|public
name|TypeInfo
name|getColumnType
parameter_list|()
block|{
return|return
name|columnType
return|;
block|}
specifier|public
name|String
name|getFamilyName
parameter_list|()
block|{
return|return
name|familyName
return|;
block|}
specifier|public
name|String
name|getQualifierName
parameter_list|()
block|{
return|return
name|qualifierName
return|;
block|}
specifier|public
name|byte
index|[]
name|getFamilyNameBytes
parameter_list|()
block|{
return|return
name|familyNameBytes
return|;
block|}
specifier|public
name|byte
index|[]
name|getQualifierNameBytes
parameter_list|()
block|{
return|return
name|qualifierNameBytes
return|;
block|}
specifier|public
name|List
argument_list|<
name|Boolean
argument_list|>
name|getBinaryStorage
parameter_list|()
block|{
return|return
name|binaryStorage
return|;
block|}
specifier|public
name|boolean
name|isHbaseRowKey
parameter_list|()
block|{
return|return
name|hbaseRowKey
return|;
block|}
specifier|public
name|String
name|getMappingSpec
parameter_list|()
block|{
return|return
name|mappingSpec
return|;
block|}
specifier|public
name|String
name|getQualifierPrefix
parameter_list|()
block|{
return|return
name|qualifierPrefix
return|;
block|}
specifier|public
name|byte
index|[]
name|getQualifierPrefixBytes
parameter_list|()
block|{
return|return
name|qualifierPrefixBytes
return|;
block|}
specifier|public
name|boolean
name|isCategory
parameter_list|(
name|ObjectInspector
operator|.
name|Category
name|category
parameter_list|)
block|{
return|return
name|columnType
operator|.
name|getCategory
argument_list|()
operator|==
name|category
return|;
block|}
block|}
block|}
end_class

end_unit

