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
name|hbase
operator|.
name|HBaseSerDe
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
name|LazySimpleSerDe
operator|.
name|SerDeParameters
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * HBaseSerDeParameters encapsulates SerDeParameters and additional configurations that are specific for  * HBaseSerDe.  *  */
end_comment

begin_class
specifier|public
class|class
name|HBaseSerDeParameters
block|{
specifier|private
name|SerDeParameters
name|serdeParams
decl_stmt|;
specifier|private
name|String
name|columnMappingString
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ColumnMapping
argument_list|>
name|columnMapping
decl_stmt|;
specifier|private
name|boolean
name|doColumnRegexMatching
decl_stmt|;
specifier|private
name|long
name|putTimestamp
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
argument_list|>
name|compositeKeyClass
decl_stmt|;
specifier|private
name|int
name|keyIndex
decl_stmt|;
name|void
name|init
parameter_list|(
name|Configuration
name|job
parameter_list|,
name|Properties
name|tbl
parameter_list|,
name|String
name|serdeName
parameter_list|)
throws|throws
name|SerDeException
block|{
name|serdeParams
operator|=
name|LazySimpleSerDe
operator|.
name|initSerdeParams
argument_list|(
name|job
argument_list|,
name|tbl
argument_list|,
name|serdeName
argument_list|)
expr_stmt|;
name|putTimestamp
operator|=
name|Long
operator|.
name|valueOf
argument_list|(
name|tbl
operator|.
name|getProperty
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_PUT_TIMESTAMP
argument_list|,
literal|"-1"
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|compKeyClass
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_COMPOSITE_KEY_CLASS
argument_list|)
decl_stmt|;
if|if
condition|(
name|compKeyClass
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|compositeKeyClass
operator|=
name|job
operator|.
name|getClassByName
argument_list|(
name|compKeyClass
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
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
comment|// Read configuration parameters
name|columnMappingString
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_COLUMNS_MAPPING
argument_list|)
expr_stmt|;
name|doColumnRegexMatching
operator|=
name|Boolean
operator|.
name|valueOf
argument_list|(
name|tbl
operator|.
name|getProperty
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_COLUMNS_REGEX_MATCHING
argument_list|,
literal|"true"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Parse and initialize the HBase columns mapping
name|columnMapping
operator|=
name|HBaseSerDe
operator|.
name|parseColumnsMapping
argument_list|(
name|columnMappingString
argument_list|,
name|doColumnRegexMatching
argument_list|)
expr_stmt|;
comment|// Build the type property string if not supplied
name|String
name|columnTypeProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnTypeProperty
operator|==
literal|null
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
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
name|columnMapping
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
name|ColumnMapping
name|colMap
init|=
name|columnMapping
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
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
name|tbl
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|columnMapping
operator|.
name|size
argument_list|()
operator|!=
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
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
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
operator|.
name|size
argument_list|()
operator|+
literal|" elements while hbase.columns.mapping has "
operator|+
name|columnMapping
operator|.
name|size
argument_list|()
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
name|columnMapping
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
name|columnMapping
operator|.
name|get
argument_list|(
name|i
argument_list|)
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
name|serdeParams
operator|.
name|getColumnTypes
argument_list|()
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
block|}
comment|// Precondition: make sure this is done after the rest of the SerDe initialization is done.
name|String
name|hbaseTableStorageType
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_TABLE_DEFAULT_STORAGE_TYPE
argument_list|)
decl_stmt|;
name|parseColumnStorageTypes
argument_list|(
name|hbaseTableStorageType
argument_list|)
expr_stmt|;
name|setKeyColumnOffset
argument_list|()
expr_stmt|;
block|}
comment|/*    * Utility method for parsing a string of the form '-,b,s,-,s:b,...' as a means of specifying    * whether to use a binary or an UTF string format to serialize and de-serialize primitive    * data types like boolean, byte, short, int, long, float, and double. This applies to    * regular columns and also to map column types which are associated with an HBase column    * family. For the map types, we apply the specification to the key or the value provided it    * is one of the above primitive types. The specifier is a colon separated value of the form    * -:s, or b:b where we have 's', 'b', or '-' on either side of the colon. 's' is for string    * format storage, 'b' is for native fixed width byte oriented storage, and '-' uses the    * table level default.    *    * @param hbaseTableDefaultStorageType - the specification associated with the table property    *        hbase.table.default.storage.type    * @throws SerDeException on parse error.    */
specifier|public
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
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|serdeParams
operator|.
name|getColumnTypes
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
name|columnMapping
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
name|columnMapping
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|TypeInfo
name|colType
init|=
name|columnTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
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
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
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
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
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
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
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
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
throw|;
block|}
block|}
block|}
name|void
name|setKeyColumnOffset
parameter_list|()
throws|throws
name|SerDeException
block|{
name|setKeyIndex
argument_list|(
name|getRowKeyColumnOffset
argument_list|(
name|columnMapping
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|int
name|getRowKeyColumnOffset
parameter_list|(
name|List
argument_list|<
name|ColumnMapping
argument_list|>
name|columnsMapping
parameter_list|)
throws|throws
name|SerDeException
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
name|columnsMapping
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
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|colMap
operator|.
name|hbaseRowKey
operator|&&
name|colMap
operator|.
name|familyName
operator|.
name|equals
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_KEY_COL
argument_list|)
condition|)
block|{
return|return
name|i
return|;
block|}
block|}
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"HBaseSerDe Error: columns mapping list does not contain"
operator|+
literal|" row key column."
argument_list|)
throw|;
block|}
specifier|public
name|StructTypeInfo
name|getRowTypeInfo
parameter_list|()
block|{
return|return
operator|(
name|StructTypeInfo
operator|)
name|serdeParams
operator|.
name|getRowTypeInfo
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getColumnNames
parameter_list|()
block|{
return|return
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
return|;
block|}
specifier|public
name|byte
index|[]
name|getSeparators
parameter_list|()
block|{
return|return
name|serdeParams
operator|.
name|getSeparators
argument_list|()
return|;
block|}
specifier|public
name|Text
name|getNullSequence
parameter_list|()
block|{
return|return
name|serdeParams
operator|.
name|getNullSequence
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isLastColumnTakesRest
parameter_list|()
block|{
return|return
name|serdeParams
operator|.
name|isLastColumnTakesRest
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isEscaped
parameter_list|()
block|{
return|return
name|serdeParams
operator|.
name|isEscaped
argument_list|()
return|;
block|}
specifier|public
name|byte
name|getEscapeChar
parameter_list|()
block|{
return|return
name|serdeParams
operator|.
name|getEscapeChar
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|getColumnTypes
parameter_list|()
block|{
return|return
name|serdeParams
operator|.
name|getColumnTypes
argument_list|()
return|;
block|}
specifier|public
name|SerDeParameters
name|getSerdeParams
parameter_list|()
block|{
return|return
name|serdeParams
return|;
block|}
specifier|public
name|void
name|setSerdeParams
parameter_list|(
name|SerDeParameters
name|serdeParams
parameter_list|)
block|{
name|this
operator|.
name|serdeParams
operator|=
name|serdeParams
expr_stmt|;
block|}
specifier|public
name|String
name|getColumnMappingString
parameter_list|()
block|{
return|return
name|columnMappingString
return|;
block|}
specifier|public
name|void
name|setColumnMappingString
parameter_list|(
name|String
name|columnMappingString
parameter_list|)
block|{
name|this
operator|.
name|columnMappingString
operator|=
name|columnMappingString
expr_stmt|;
block|}
specifier|public
name|long
name|getPutTimestamp
parameter_list|()
block|{
return|return
name|putTimestamp
return|;
block|}
specifier|public
name|void
name|setPutTimestamp
parameter_list|(
name|long
name|putTimestamp
parameter_list|)
block|{
name|this
operator|.
name|putTimestamp
operator|=
name|putTimestamp
expr_stmt|;
block|}
specifier|public
name|boolean
name|isDoColumnRegexMatching
parameter_list|()
block|{
return|return
name|doColumnRegexMatching
return|;
block|}
specifier|public
name|void
name|setDoColumnRegexMatching
parameter_list|(
name|boolean
name|doColumnRegexMatching
parameter_list|)
block|{
name|this
operator|.
name|doColumnRegexMatching
operator|=
name|doColumnRegexMatching
expr_stmt|;
block|}
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getCompositeKeyClass
parameter_list|()
block|{
return|return
name|compositeKeyClass
return|;
block|}
specifier|public
name|void
name|setCompositeKeyClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|compositeKeyClass
parameter_list|)
block|{
name|this
operator|.
name|compositeKeyClass
operator|=
name|compositeKeyClass
expr_stmt|;
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
name|void
name|setKeyIndex
parameter_list|(
name|int
name|keyIndex
parameter_list|)
block|{
name|this
operator|.
name|keyIndex
operator|=
name|keyIndex
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|ColumnMapping
argument_list|>
name|getColumnMapping
parameter_list|()
block|{
return|return
name|columnMapping
return|;
block|}
specifier|public
name|ColumnMapping
name|getKeyColumnMapping
parameter_list|()
block|{
return|return
name|columnMapping
operator|.
name|get
argument_list|(
name|keyIndex
argument_list|)
return|;
block|}
specifier|public
name|boolean
index|[]
name|getNeedsEscape
parameter_list|()
block|{
return|return
name|serdeParams
operator|.
name|getNeedsEscape
argument_list|()
return|;
block|}
block|}
end_class

end_unit

