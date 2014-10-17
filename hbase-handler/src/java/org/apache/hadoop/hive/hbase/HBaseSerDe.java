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
name|hbase
operator|.
name|util
operator|.
name|Bytes
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
name|ql
operator|.
name|plan
operator|.
name|TableDesc
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
name|AbstractSerDe
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
name|SerDe
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
name|SerDeSpec
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
name|SerDeStats
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
name|objectinspector
operator|.
name|LazySimpleStructObjectInspector
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
name|hadoop
operator|.
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_comment
comment|/**  * HBaseSerDe can be used to serialize object into an HBase table and  * deserialize objects from an HBase table.  */
end_comment

begin_class
annotation|@
name|SerDeSpec
argument_list|(
name|schemaProps
operator|=
block|{
name|serdeConstants
operator|.
name|LIST_COLUMNS
block|,
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
block|,
name|serdeConstants
operator|.
name|FIELD_DELIM
block|,
name|serdeConstants
operator|.
name|COLLECTION_DELIM
block|,
name|serdeConstants
operator|.
name|MAPKEY_DELIM
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_NULL_FORMAT
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_LAST_COLUMN_TAKES_REST
block|,
name|serdeConstants
operator|.
name|ESCAPE_CHAR
block|,
name|serdeConstants
operator|.
name|SERIALIZATION_ENCODING
block|,
name|LazySimpleSerDe
operator|.
name|SERIALIZATION_EXTEND_NESTING_LEVELS
block|,
name|HBaseSerDe
operator|.
name|HBASE_COLUMNS_MAPPING
block|,
name|HBaseSerDe
operator|.
name|HBASE_TABLE_NAME
block|,
name|HBaseSerDe
operator|.
name|HBASE_TABLE_DEFAULT_STORAGE_TYPE
block|,
name|HBaseSerDe
operator|.
name|HBASE_KEY_COL
block|,
name|HBaseSerDe
operator|.
name|HBASE_PUT_TIMESTAMP
block|,
name|HBaseSerDe
operator|.
name|HBASE_COMPOSITE_KEY_CLASS
block|,
name|HBaseSerDe
operator|.
name|HBASE_COMPOSITE_KEY_TYPES
block|,
name|HBaseSerDe
operator|.
name|HBASE_COMPOSITE_KEY_FACTORY
block|,
name|HBaseSerDe
operator|.
name|HBASE_STRUCT_SERIALIZER_CLASS
block|,
name|HBaseSerDe
operator|.
name|HBASE_SCAN_CACHE
block|,
name|HBaseSerDe
operator|.
name|HBASE_SCAN_CACHEBLOCKS
block|,
name|HBaseSerDe
operator|.
name|HBASE_SCAN_BATCH
block|,
name|HBaseSerDe
operator|.
name|HBASE_AUTOGENERATE_STRUCT
block|}
argument_list|)
specifier|public
class|class
name|HBaseSerDe
extends|extends
name|AbstractSerDe
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HBaseSerDe
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_COLUMNS_MAPPING
init|=
literal|"hbase.columns.mapping"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_TABLE_NAME
init|=
literal|"hbase.table.name"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_TABLE_DEFAULT_STORAGE_TYPE
init|=
literal|"hbase.table.default.storage.type"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_KEY_COL
init|=
literal|":key"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_TIMESTAMP_COL
init|=
literal|":timestamp"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_PUT_TIMESTAMP
init|=
literal|"hbase.put.timestamp"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_COMPOSITE_KEY_CLASS
init|=
literal|"hbase.composite.key.class"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_COMPOSITE_KEY_TYPES
init|=
literal|"hbase.composite.key.types"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_COMPOSITE_KEY_FACTORY
init|=
literal|"hbase.composite.key.factory"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_STRUCT_SERIALIZER_CLASS
init|=
literal|"hbase.struct.serialization.class"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_SCAN_CACHE
init|=
literal|"hbase.scan.cache"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_SCAN_CACHEBLOCKS
init|=
literal|"hbase.scan.cacheblock"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_SCAN_BATCH
init|=
literal|"hbase.scan.batch"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_AUTOGENERATE_STRUCT
init|=
literal|"hbase.struct.autogenerate"
decl_stmt|;
comment|/**    * Determines whether a regex matching should be done on the columns or not. Defaults to true.    *<strong>WARNING: Note that currently this only supports the suffix wildcard .*</strong>    */
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_COLUMNS_REGEX_MATCHING
init|=
literal|"hbase.columns.mapping.regex.matching"
decl_stmt|;
comment|/**    * Defines the type for a column.    **/
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZATION_TYPE
init|=
literal|"serialization.type"
decl_stmt|;
specifier|private
name|ObjectInspector
name|cachedObjectInspector
decl_stmt|;
specifier|private
name|LazyHBaseRow
name|cachedHBaseRow
decl_stmt|;
specifier|private
name|HBaseSerDeParameters
name|serdeParams
decl_stmt|;
specifier|private
name|HBaseRowSerializer
name|serializer
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getClass
argument_list|()
operator|+
literal|"["
operator|+
name|serdeParams
operator|+
literal|"]"
return|;
block|}
specifier|public
name|HBaseSerDe
parameter_list|()
throws|throws
name|SerDeException
block|{   }
comment|/**    * Initialize the SerDe given parameters.    * @see SerDe#initialize(Configuration, Properties)    */
annotation|@
name|Override
specifier|public
name|void
name|initialize
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
name|serdeParams
operator|=
operator|new
name|HBaseSerDeParameters
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|cachedObjectInspector
operator|=
name|HBaseLazyObjectFactory
operator|.
name|createLazyHBaseStructInspector
argument_list|(
name|serdeParams
operator|.
name|getSerdeParams
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getKeyIndex
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getKeyFactory
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getValueFactories
argument_list|()
argument_list|)
expr_stmt|;
name|cachedHBaseRow
operator|=
operator|new
name|LazyHBaseRow
argument_list|(
operator|(
name|LazySimpleStructObjectInspector
operator|)
name|cachedObjectInspector
argument_list|,
name|serdeParams
argument_list|)
expr_stmt|;
name|serializer
operator|=
operator|new
name|HBaseRowSerializer
argument_list|(
name|serdeParams
argument_list|)
expr_stmt|;
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
literal|"HBaseSerDe initialized with : "
operator|+
name|serdeParams
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|ColumnMappings
name|parseColumnsMapping
parameter_list|(
name|String
name|columnsMappingSpec
parameter_list|)
throws|throws
name|SerDeException
block|{
return|return
name|parseColumnsMapping
argument_list|(
name|columnsMappingSpec
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * Parses the HBase columns mapping specifier to identify the column families, qualifiers    * and also caches the byte arrays corresponding to them. One of the Hive table    * columns maps to the HBase row key, by default the first column.    *    * @param columnsMappingSpec string hbase.columns.mapping specified when creating table    * @param doColumnRegexMatching whether to do a regex matching on the columns or not    * @return List<ColumnMapping> which contains the column mapping information by position    * @throws org.apache.hadoop.hive.serde2.SerDeException    */
specifier|public
specifier|static
name|ColumnMappings
name|parseColumnsMapping
parameter_list|(
name|String
name|columnsMappingSpec
parameter_list|,
name|boolean
name|doColumnRegexMatching
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
name|columnsMappingSpec
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Error: hbase.columns.mapping missing for this HBase table."
argument_list|)
throw|;
block|}
if|if
condition|(
name|columnsMappingSpec
operator|.
name|isEmpty
argument_list|()
operator|||
name|columnsMappingSpec
operator|.
name|equals
argument_list|(
name|HBASE_KEY_COL
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Error: hbase.columns.mapping specifies only the HBase table"
operator|+
literal|" row key. A valid Hive-HBase table must specify at least one additional column."
argument_list|)
throw|;
block|}
name|int
name|rowKeyIndex
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|timestampIndex
init|=
operator|-
literal|1
decl_stmt|;
name|List
argument_list|<
name|ColumnMapping
argument_list|>
name|columnsMapping
init|=
operator|new
name|ArrayList
argument_list|<
name|ColumnMapping
argument_list|>
argument_list|()
decl_stmt|;
name|String
index|[]
name|columnSpecs
init|=
name|columnsMappingSpec
operator|.
name|split
argument_list|(
literal|","
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
name|columnSpecs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|mappingSpec
init|=
name|columnSpecs
index|[
name|i
index|]
operator|.
name|trim
argument_list|()
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
name|colInfo
init|=
name|mapInfo
index|[
literal|0
index|]
decl_stmt|;
name|int
name|idxFirst
init|=
name|colInfo
operator|.
name|indexOf
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
name|int
name|idxLast
init|=
name|colInfo
operator|.
name|lastIndexOf
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|idxFirst
operator|<
literal|0
operator|||
operator|!
operator|(
name|idxFirst
operator|==
name|idxLast
operator|)
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Error: the HBase columns mapping contains a badly formed "
operator|+
literal|"column family, column qualifier specification."
argument_list|)
throw|;
block|}
name|ColumnMapping
name|columnMapping
init|=
operator|new
name|ColumnMapping
argument_list|()
decl_stmt|;
if|if
condition|(
name|colInfo
operator|.
name|equals
argument_list|(
name|HBASE_KEY_COL
argument_list|)
condition|)
block|{
name|rowKeyIndex
operator|=
name|i
expr_stmt|;
name|columnMapping
operator|.
name|familyName
operator|=
name|colInfo
expr_stmt|;
name|columnMapping
operator|.
name|familyNameBytes
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|colInfo
argument_list|)
expr_stmt|;
name|columnMapping
operator|.
name|qualifierName
operator|=
literal|null
expr_stmt|;
name|columnMapping
operator|.
name|qualifierNameBytes
operator|=
literal|null
expr_stmt|;
name|columnMapping
operator|.
name|hbaseRowKey
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|colInfo
operator|.
name|equals
argument_list|(
name|HBASE_TIMESTAMP_COL
argument_list|)
condition|)
block|{
name|timestampIndex
operator|=
name|i
expr_stmt|;
name|columnMapping
operator|.
name|familyName
operator|=
name|colInfo
expr_stmt|;
name|columnMapping
operator|.
name|familyNameBytes
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|colInfo
argument_list|)
expr_stmt|;
name|columnMapping
operator|.
name|qualifierName
operator|=
literal|null
expr_stmt|;
name|columnMapping
operator|.
name|qualifierNameBytes
operator|=
literal|null
expr_stmt|;
name|columnMapping
operator|.
name|hbaseTimestamp
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|String
index|[]
name|parts
init|=
name|colInfo
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|parts
operator|.
name|length
operator|>
literal|0
operator|&&
name|parts
operator|.
name|length
operator|<=
literal|2
operator|)
assert|;
name|columnMapping
operator|.
name|familyName
operator|=
name|parts
index|[
literal|0
index|]
expr_stmt|;
name|columnMapping
operator|.
name|familyNameBytes
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|columnMapping
operator|.
name|hbaseRowKey
operator|=
literal|false
expr_stmt|;
name|columnMapping
operator|.
name|hbaseTimestamp
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|parts
operator|.
name|length
operator|==
literal|2
condition|)
block|{
if|if
condition|(
name|doColumnRegexMatching
operator|&&
name|parts
index|[
literal|1
index|]
operator|.
name|endsWith
argument_list|(
literal|".*"
argument_list|)
condition|)
block|{
comment|// we have a prefix with a wildcard
name|columnMapping
operator|.
name|qualifierPrefix
operator|=
name|parts
index|[
literal|1
index|]
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|parts
index|[
literal|1
index|]
operator|.
name|length
argument_list|()
operator|-
literal|2
argument_list|)
expr_stmt|;
name|columnMapping
operator|.
name|qualifierPrefixBytes
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columnMapping
operator|.
name|qualifierPrefix
argument_list|)
expr_stmt|;
comment|// we weren't provided any actual qualifier name. Set these to
comment|// null.
name|columnMapping
operator|.
name|qualifierName
operator|=
literal|null
expr_stmt|;
name|columnMapping
operator|.
name|qualifierNameBytes
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
comment|// set the regular provided qualifier names
name|columnMapping
operator|.
name|qualifierName
operator|=
name|parts
index|[
literal|1
index|]
expr_stmt|;
name|columnMapping
operator|.
name|qualifierNameBytes
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|parts
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|columnMapping
operator|.
name|qualifierName
operator|=
literal|null
expr_stmt|;
name|columnMapping
operator|.
name|qualifierNameBytes
operator|=
literal|null
expr_stmt|;
block|}
block|}
name|columnMapping
operator|.
name|mappingSpec
operator|=
name|mappingSpec
expr_stmt|;
name|columnsMapping
operator|.
name|add
argument_list|(
name|columnMapping
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rowKeyIndex
operator|==
operator|-
literal|1
condition|)
block|{
name|rowKeyIndex
operator|=
literal|0
expr_stmt|;
name|ColumnMapping
name|columnMapping
init|=
operator|new
name|ColumnMapping
argument_list|()
decl_stmt|;
name|columnMapping
operator|.
name|familyName
operator|=
name|HBaseSerDe
operator|.
name|HBASE_KEY_COL
expr_stmt|;
name|columnMapping
operator|.
name|familyNameBytes
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_KEY_COL
argument_list|)
expr_stmt|;
name|columnMapping
operator|.
name|qualifierName
operator|=
literal|null
expr_stmt|;
name|columnMapping
operator|.
name|qualifierNameBytes
operator|=
literal|null
expr_stmt|;
name|columnMapping
operator|.
name|hbaseRowKey
operator|=
literal|true
expr_stmt|;
name|columnMapping
operator|.
name|mappingSpec
operator|=
name|HBaseSerDe
operator|.
name|HBASE_KEY_COL
expr_stmt|;
name|columnsMapping
operator|.
name|add
argument_list|(
literal|0
argument_list|,
name|columnMapping
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ColumnMappings
argument_list|(
name|columnsMapping
argument_list|,
name|rowKeyIndex
argument_list|,
name|timestampIndex
argument_list|)
return|;
block|}
specifier|public
name|LazySimpleSerDe
operator|.
name|SerDeParameters
name|getSerdeParams
parameter_list|()
block|{
return|return
name|serdeParams
operator|.
name|getSerdeParams
argument_list|()
return|;
block|}
specifier|public
name|HBaseSerDeParameters
name|getHBaseSerdeParam
parameter_list|()
block|{
return|return
name|serdeParams
return|;
block|}
comment|/**    * Deserialize a row from the HBase Result writable to a LazyObject    * @param result the HBase Result Writable containing the row    * @return the deserialized object    * @see SerDe#deserialize(Writable)    */
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|result
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
operator|!
operator|(
name|result
operator|instanceof
name|ResultWritable
operator|)
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": expects ResultWritable!"
argument_list|)
throw|;
block|}
name|cachedHBaseRow
operator|.
name|init
argument_list|(
operator|(
operator|(
name|ResultWritable
operator|)
name|result
operator|)
operator|.
name|getResult
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|cachedHBaseRow
return|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
throws|throws
name|SerDeException
block|{
return|return
name|cachedObjectInspector
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|getSerializedClass
parameter_list|()
block|{
return|return
name|PutWritable
operator|.
name|class
return|;
block|}
annotation|@
name|Override
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
name|SerDeException
block|{
try|try
block|{
return|return
name|serializer
operator|.
name|serialize
argument_list|(
name|obj
argument_list|,
name|objInspector
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
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
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
comment|// no support for statistics
return|return
literal|null
return|;
block|}
specifier|public
name|HBaseKeyFactory
name|getKeyFactory
parameter_list|()
block|{
return|return
name|serdeParams
operator|.
name|getKeyFactory
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|void
name|configureJobConf
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|JobConf
name|jobConf
parameter_list|)
throws|throws
name|Exception
block|{
name|HBaseSerDeParameters
name|serdeParams
init|=
operator|new
name|HBaseSerDeParameters
argument_list|(
name|jobConf
argument_list|,
name|tableDesc
operator|.
name|getProperties
argument_list|()
argument_list|,
name|HBaseSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|serdeParams
operator|.
name|getKeyFactory
argument_list|()
operator|.
name|configureJobConf
argument_list|(
name|tableDesc
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

