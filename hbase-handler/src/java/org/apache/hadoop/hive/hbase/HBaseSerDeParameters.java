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
name|Properties
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
name|reflect
operator|.
name|ReflectData
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
name|hbase
operator|.
name|struct
operator|.
name|AvroHBaseValueFactory
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
name|struct
operator|.
name|DefaultHBaseValueFactory
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
name|struct
operator|.
name|HBaseValueFactory
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
name|struct
operator|.
name|StructHBaseValueFactory
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
name|avro
operator|.
name|AvroSerdeUtils
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
name|util
operator|.
name|ReflectionUtils
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
specifier|public
specifier|static
specifier|final
name|String
name|AVRO_SERIALIZATION_TYPE
init|=
literal|"avro"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STRUCT_SERIALIZATION_TYPE
init|=
literal|"struct"
decl_stmt|;
specifier|private
specifier|final
name|SerDeParameters
name|serdeParams
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|job
decl_stmt|;
specifier|private
specifier|final
name|String
name|columnMappingString
decl_stmt|;
specifier|private
specifier|final
name|ColumnMappings
name|columnMappings
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|doColumnRegexMatching
decl_stmt|;
specifier|private
specifier|final
name|long
name|putTimestamp
decl_stmt|;
specifier|private
specifier|final
name|HBaseKeyFactory
name|keyFactory
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|HBaseValueFactory
argument_list|>
name|valueFactories
decl_stmt|;
name|HBaseSerDeParameters
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
name|this
operator|.
name|job
operator|=
name|job
expr_stmt|;
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
name|columnMappings
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
name|String
name|autogenerate
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_AUTOGENERATE_STRUCT
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnTypeProperty
operator|==
literal|null
operator|||
name|columnTypeProperty
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|String
name|columnNameProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnNameProperty
operator|==
literal|null
operator|||
name|columnNameProperty
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|autogenerate
operator|==
literal|null
operator|||
name|autogenerate
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Either the columns must be specified or the "
operator|+
name|HBaseSerDe
operator|.
name|HBASE_AUTOGENERATE_STRUCT
operator|+
literal|" property must be set to true."
argument_list|)
throw|;
block|}
name|tbl
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|columnMappings
operator|.
name|toNamesString
argument_list|(
name|tbl
argument_list|,
name|autogenerate
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|tbl
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|columnMappings
operator|.
name|toTypesString
argument_list|(
name|tbl
argument_list|,
name|job
argument_list|,
name|autogenerate
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
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
name|this
operator|.
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
name|columnMappings
operator|.
name|setHiveColumnDescription
argument_list|(
name|serdeName
argument_list|,
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getColumnTypes
argument_list|()
argument_list|)
expr_stmt|;
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
name|columnMappings
operator|.
name|parseColumnStorageTypes
argument_list|(
name|hbaseTableStorageType
argument_list|)
expr_stmt|;
name|this
operator|.
name|keyFactory
operator|=
name|initKeyFactory
argument_list|(
name|job
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
name|this
operator|.
name|valueFactories
operator|=
name|initValueFactories
argument_list|(
name|job
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
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
name|long
name|getPutTimestamp
parameter_list|()
block|{
return|return
name|putTimestamp
return|;
block|}
specifier|public
name|int
name|getKeyIndex
parameter_list|()
block|{
return|return
name|columnMappings
operator|.
name|getKeyIndex
argument_list|()
return|;
block|}
specifier|public
name|ColumnMapping
name|getKeyColumnMapping
parameter_list|()
block|{
return|return
name|columnMappings
operator|.
name|getKeyMapping
argument_list|()
return|;
block|}
specifier|public
name|ColumnMappings
name|getColumnMappings
parameter_list|()
block|{
return|return
name|columnMappings
return|;
block|}
specifier|public
name|HBaseKeyFactory
name|getKeyFactory
parameter_list|()
block|{
return|return
name|keyFactory
return|;
block|}
specifier|public
name|List
argument_list|<
name|HBaseValueFactory
argument_list|>
name|getValueFactories
parameter_list|()
block|{
return|return
name|valueFactories
return|;
block|}
specifier|public
name|Configuration
name|getBaseConfiguration
parameter_list|()
block|{
return|return
name|job
return|;
block|}
specifier|public
name|TypeInfo
name|getTypeForName
parameter_list|(
name|String
name|columnName
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
decl_stmt|;
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
name|columnNames
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
name|columnName
operator|.
name|equals
argument_list|(
name|columnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
return|return
name|columnTypes
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
name|IllegalArgumentException
argument_list|(
literal|"Invalid column name "
operator|+
name|columnName
argument_list|)
throw|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|columnMappingString
operator|+
literal|":"
operator|+
name|getColumnNames
argument_list|()
operator|+
literal|":"
operator|+
name|getColumnTypes
argument_list|()
operator|+
literal|"]"
return|;
block|}
specifier|private
name|HBaseKeyFactory
name|initKeyFactory
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
try|try
block|{
name|HBaseKeyFactory
name|keyFactory
init|=
name|createKeyFactory
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyFactory
operator|!=
literal|null
condition|)
block|{
name|keyFactory
operator|.
name|init
argument_list|(
name|this
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
block|}
return|return
name|keyFactory
return|;
block|}
catch|catch
parameter_list|(
name|Exception
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
specifier|private
specifier|static
name|HBaseKeyFactory
name|createKeyFactory
parameter_list|(
name|Configuration
name|job
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|factoryClassName
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_COMPOSITE_KEY_FACTORY
argument_list|)
decl_stmt|;
if|if
condition|(
name|factoryClassName
operator|!=
literal|null
condition|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|factoryClazz
init|=
name|Class
operator|.
name|forName
argument_list|(
name|factoryClassName
argument_list|)
decl_stmt|;
return|return
operator|(
name|HBaseKeyFactory
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|factoryClazz
argument_list|,
name|job
argument_list|)
return|;
block|}
name|String
name|keyClassName
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
name|keyClassName
operator|!=
literal|null
condition|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|keyClass
init|=
name|Class
operator|.
name|forName
argument_list|(
name|keyClassName
argument_list|)
decl_stmt|;
return|return
operator|new
name|CompositeHBaseKeyFactory
argument_list|(
name|keyClass
argument_list|)
return|;
block|}
return|return
operator|new
name|DefaultHBaseKeyFactory
argument_list|()
return|;
block|}
specifier|private
name|List
argument_list|<
name|HBaseValueFactory
argument_list|>
name|initValueFactories
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
name|List
argument_list|<
name|HBaseValueFactory
argument_list|>
name|valueFactories
init|=
name|createValueFactories
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|)
decl_stmt|;
for|for
control|(
name|HBaseValueFactory
name|valueFactory
range|:
name|valueFactories
control|)
block|{
name|valueFactory
operator|.
name|init
argument_list|(
name|this
argument_list|,
name|conf
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
block|}
return|return
name|valueFactories
return|;
block|}
specifier|private
name|List
argument_list|<
name|HBaseValueFactory
argument_list|>
name|createValueFactories
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
name|List
argument_list|<
name|HBaseValueFactory
argument_list|>
name|valueFactories
init|=
operator|new
name|ArrayList
argument_list|<
name|HBaseValueFactory
argument_list|>
argument_list|()
decl_stmt|;
try|try
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
name|columnMappings
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|serType
init|=
name|getSerializationType
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|,
name|columnMappings
operator|.
name|getColumnsMapping
argument_list|()
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|AVRO_SERIALIZATION_TYPE
operator|.
name|equals
argument_list|(
name|serType
argument_list|)
condition|)
block|{
name|Schema
name|schema
init|=
name|getSchema
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|,
name|columnMappings
operator|.
name|getColumnsMapping
argument_list|()
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|valueFactories
operator|.
name|add
argument_list|(
operator|new
name|AvroHBaseValueFactory
argument_list|(
name|i
argument_list|,
name|schema
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|STRUCT_SERIALIZATION_TYPE
operator|.
name|equals
argument_list|(
name|serType
argument_list|)
condition|)
block|{
name|String
name|structValueClassName
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_STRUCT_SERIALIZER_CLASS
argument_list|)
decl_stmt|;
if|if
condition|(
name|structValueClassName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_STRUCT_SERIALIZER_CLASS
operator|+
literal|" must be set for hbase columns of type ["
operator|+
name|STRUCT_SERIALIZATION_TYPE
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|Class
argument_list|<
name|?
argument_list|>
name|structValueClass
init|=
name|job
operator|.
name|getClassByName
argument_list|(
name|structValueClassName
argument_list|)
decl_stmt|;
name|valueFactories
operator|.
name|add
argument_list|(
operator|new
name|StructHBaseValueFactory
argument_list|(
name|i
argument_list|,
name|structValueClass
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|valueFactories
operator|.
name|add
argument_list|(
operator|new
name|DefaultHBaseValueFactory
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
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
return|return
name|valueFactories
return|;
block|}
comment|/**    * Get the type for the given {@link ColumnMapping colMap}    * */
specifier|private
name|String
name|getSerializationType
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|,
name|ColumnMapping
name|colMap
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|serType
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|colMap
operator|.
name|qualifierName
operator|==
literal|null
condition|)
block|{
comment|// only a column family
if|if
condition|(
name|colMap
operator|.
name|qualifierPrefix
operator|!=
literal|null
condition|)
block|{
name|serType
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|colMap
operator|.
name|qualifierPrefix
operator|+
literal|"."
operator|+
name|HBaseSerDe
operator|.
name|SERIALIZATION_TYPE
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serType
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|HBaseSerDe
operator|.
name|SERIALIZATION_TYPE
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|colMap
operator|.
name|hbaseRowKey
condition|)
block|{
comment|// not an hbase row key. This should either be a prefix or an individual qualifier
name|String
name|qualifierName
init|=
name|colMap
operator|.
name|qualifierName
decl_stmt|;
if|if
condition|(
name|colMap
operator|.
name|qualifierName
operator|.
name|endsWith
argument_list|(
literal|"*"
argument_list|)
condition|)
block|{
name|qualifierName
operator|=
name|colMap
operator|.
name|qualifierName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|colMap
operator|.
name|qualifierName
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|serType
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|qualifierName
operator|+
literal|"."
operator|+
name|HBaseSerDe
operator|.
name|SERIALIZATION_TYPE
argument_list|)
expr_stmt|;
block|}
return|return
name|serType
return|;
block|}
specifier|private
name|Schema
name|getSchema
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|,
name|ColumnMapping
name|colMap
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|serType
init|=
literal|null
decl_stmt|;
name|String
name|serClassName
init|=
literal|null
decl_stmt|;
name|String
name|schemaLiteral
init|=
literal|null
decl_stmt|;
name|String
name|schemaUrl
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|colMap
operator|.
name|qualifierName
operator|==
literal|null
condition|)
block|{
comment|// only a column family
if|if
condition|(
name|colMap
operator|.
name|qualifierPrefix
operator|!=
literal|null
condition|)
block|{
name|serType
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|colMap
operator|.
name|qualifierPrefix
operator|+
literal|"."
operator|+
name|HBaseSerDe
operator|.
name|SERIALIZATION_TYPE
argument_list|)
expr_stmt|;
name|serClassName
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|colMap
operator|.
name|qualifierPrefix
operator|+
literal|"."
operator|+
name|serdeConstants
operator|.
name|SERIALIZATION_CLASS
argument_list|)
expr_stmt|;
name|schemaLiteral
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|colMap
operator|.
name|qualifierPrefix
operator|+
literal|"."
operator|+
name|AvroSerdeUtils
operator|.
name|SCHEMA_LITERAL
argument_list|)
expr_stmt|;
name|schemaUrl
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|colMap
operator|.
name|qualifierPrefix
operator|+
literal|"."
operator|+
name|AvroSerdeUtils
operator|.
name|SCHEMA_URL
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serType
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|HBaseSerDe
operator|.
name|SERIALIZATION_TYPE
argument_list|)
expr_stmt|;
name|serClassName
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|serdeConstants
operator|.
name|SERIALIZATION_CLASS
argument_list|)
expr_stmt|;
name|schemaLiteral
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|AvroSerdeUtils
operator|.
name|SCHEMA_LITERAL
argument_list|)
expr_stmt|;
name|schemaUrl
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|AvroSerdeUtils
operator|.
name|SCHEMA_URL
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|colMap
operator|.
name|hbaseRowKey
condition|)
block|{
comment|// not an hbase row key. This should either be a prefix or an individual qualifier
name|String
name|qualifierName
init|=
name|colMap
operator|.
name|qualifierName
decl_stmt|;
if|if
condition|(
name|colMap
operator|.
name|qualifierName
operator|.
name|endsWith
argument_list|(
literal|"*"
argument_list|)
condition|)
block|{
name|qualifierName
operator|=
name|colMap
operator|.
name|qualifierName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|colMap
operator|.
name|qualifierName
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|serType
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|qualifierName
operator|+
literal|"."
operator|+
name|HBaseSerDe
operator|.
name|SERIALIZATION_TYPE
argument_list|)
expr_stmt|;
name|serClassName
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|qualifierName
operator|+
literal|"."
operator|+
name|serdeConstants
operator|.
name|SERIALIZATION_CLASS
argument_list|)
expr_stmt|;
name|schemaLiteral
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|qualifierName
operator|+
literal|"."
operator|+
name|AvroSerdeUtils
operator|.
name|SCHEMA_LITERAL
argument_list|)
expr_stmt|;
name|schemaUrl
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|colMap
operator|.
name|familyName
operator|+
literal|"."
operator|+
name|qualifierName
operator|+
literal|"."
operator|+
name|AvroSerdeUtils
operator|.
name|SCHEMA_URL
argument_list|)
expr_stmt|;
block|}
name|String
name|avroSchemaRetClass
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|AvroSerdeUtils
operator|.
name|SCHEMA_RETRIEVER
argument_list|)
decl_stmt|;
if|if
condition|(
name|schemaLiteral
operator|==
literal|null
operator|&&
name|serClassName
operator|==
literal|null
operator|&&
name|schemaUrl
operator|==
literal|null
operator|&&
name|avroSchemaRetClass
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"serialization.type was set to ["
operator|+
name|serType
operator|+
literal|"] but neither "
operator|+
name|AvroSerdeUtils
operator|.
name|SCHEMA_LITERAL
operator|+
literal|", "
operator|+
name|AvroSerdeUtils
operator|.
name|SCHEMA_URL
operator|+
literal|", serialization.class or "
operator|+
name|AvroSerdeUtils
operator|.
name|SCHEMA_RETRIEVER
operator|+
literal|" property was set"
argument_list|)
throw|;
block|}
name|Class
argument_list|<
name|?
argument_list|>
name|deserializerClass
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|serClassName
operator|!=
literal|null
condition|)
block|{
name|deserializerClass
operator|=
name|conf
operator|.
name|getClassByName
argument_list|(
name|serClassName
argument_list|)
expr_stmt|;
block|}
name|Schema
name|schema
init|=
literal|null
decl_stmt|;
comment|// only worry about getting schema if we are dealing with Avro
if|if
condition|(
name|serType
operator|.
name|equalsIgnoreCase
argument_list|(
name|AVRO_SERIALIZATION_TYPE
argument_list|)
condition|)
block|{
if|if
condition|(
name|avroSchemaRetClass
operator|==
literal|null
condition|)
block|{
comment|// bother about generating a schema only if a schema retriever class wasn't provided
if|if
condition|(
name|schemaLiteral
operator|!=
literal|null
condition|)
block|{
name|schema
operator|=
name|Schema
operator|.
name|parse
argument_list|(
name|schemaLiteral
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|schemaUrl
operator|!=
literal|null
condition|)
block|{
name|schema
operator|=
name|HBaseSerDeHelper
operator|.
name|getSchemaFromFS
argument_list|(
name|schemaUrl
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|deserializerClass
operator|!=
literal|null
condition|)
block|{
name|schema
operator|=
name|ReflectData
operator|.
name|get
argument_list|()
operator|.
name|getSchema
argument_list|(
name|deserializerClass
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|schema
return|;
block|}
block|}
end_class

end_unit

