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
name|read
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
name|ListIterator
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
name|conf
operator|.
name|HiveConf
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
name|IOConstants
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
name|convert
operator|.
name|DataWritableRecordConverter
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
name|metadata
operator|.
name|VirtualColumn
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
name|ColumnProjectionUtils
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
name|TypeInfoUtils
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
name|io
operator|.
name|ObjectArrayWritable
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
name|StringUtils
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|hadoop
operator|.
name|api
operator|.
name|InitContext
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|hadoop
operator|.
name|api
operator|.
name|ReadSupport
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|RecordMaterializer
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|GroupType
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|MessageType
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|Type
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|Types
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|PrimitiveType
operator|.
name|PrimitiveTypeName
import|;
end_import

begin_comment
comment|/**  *  * A MapWritableReadSupport  *  * Manages the translation between Hive and Parquet  *  */
end_comment

begin_class
specifier|public
class|class
name|DataWritableReadSupport
extends|extends
name|ReadSupport
argument_list|<
name|ObjectArrayWritable
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_TABLE_AS_PARQUET_SCHEMA
init|=
literal|"HIVE_TABLE_SCHEMA"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PARQUET_COLUMN_INDEX_ACCESS
init|=
literal|"parquet.column.index.access"
decl_stmt|;
comment|/**    * From a string which columns names (including hive column), return a list    * of string columns    *    * @param columns comma separated list of columns    * @return list with virtual columns removed    */
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getColumnNames
parameter_list|(
specifier|final
name|String
name|columns
parameter_list|)
block|{
return|return
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|VirtualColumn
operator|.
name|removeVirtualColumns
argument_list|(
name|StringUtils
operator|.
name|getStringCollection
argument_list|(
name|columns
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Returns a list of TypeInfo objects from a string which contains column    * types strings.    *    * @param types Comma separated list of types    * @return A list of TypeInfo objects.    */
specifier|private
specifier|static
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|getColumnTypes
parameter_list|(
specifier|final
name|String
name|types
parameter_list|)
block|{
return|return
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|types
argument_list|)
return|;
block|}
comment|/**    * Searchs for a fieldName into a parquet GroupType by ignoring string case.    * GroupType#getType(String fieldName) is case sensitive, so we use this method.    *    * @param groupType Group of field types where to search for fieldName    * @param fieldName The field what we are searching    * @return The Type object of the field found; null otherwise.    */
specifier|private
specifier|static
name|Type
name|getFieldTypeIgnoreCase
parameter_list|(
name|GroupType
name|groupType
parameter_list|,
name|String
name|fieldName
parameter_list|)
block|{
for|for
control|(
name|Type
name|type
range|:
name|groupType
operator|.
name|getFields
argument_list|()
control|)
block|{
if|if
condition|(
name|type
operator|.
name|getName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
return|return
name|type
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Searchs column names by name on a given Parquet schema, and returns its corresponded    * Parquet schema types.    *    * @param schema Group schema where to search for column names.    * @param colNames List of column names.    * @param colTypes List of column types.    * @return List of GroupType objects of projected columns.    */
specifier|private
specifier|static
name|List
argument_list|<
name|Type
argument_list|>
name|getProjectedGroupFields
parameter_list|(
name|GroupType
name|schema
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|colTypes
parameter_list|)
block|{
name|List
argument_list|<
name|Type
argument_list|>
name|schemaTypes
init|=
operator|new
name|ArrayList
argument_list|<
name|Type
argument_list|>
argument_list|()
decl_stmt|;
name|ListIterator
name|columnIterator
init|=
name|colNames
operator|.
name|listIterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|columnIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|TypeInfo
name|colType
init|=
name|colTypes
operator|.
name|get
argument_list|(
name|columnIterator
operator|.
name|nextIndex
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|colName
init|=
operator|(
name|String
operator|)
name|columnIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|Type
name|fieldType
init|=
name|getFieldTypeIgnoreCase
argument_list|(
name|schema
argument_list|,
name|colName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldType
operator|!=
literal|null
condition|)
block|{
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
name|STRUCT
condition|)
block|{
if|if
condition|(
name|fieldType
operator|.
name|isPrimitive
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Invalid schema data type, found: PRIMITIVE, expected: STRUCT"
argument_list|)
throw|;
block|}
name|GroupType
name|groupFieldType
init|=
name|fieldType
operator|.
name|asGroupType
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Type
argument_list|>
name|groupFields
init|=
name|getProjectedGroupFields
argument_list|(
name|groupFieldType
argument_list|,
operator|(
operator|(
name|StructTypeInfo
operator|)
name|colType
operator|)
operator|.
name|getAllStructFieldNames
argument_list|()
argument_list|,
operator|(
operator|(
name|StructTypeInfo
operator|)
name|colType
operator|)
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
argument_list|)
decl_stmt|;
name|Type
index|[]
name|typesArray
init|=
name|groupFields
operator|.
name|toArray
argument_list|(
operator|new
name|Type
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|schemaTypes
operator|.
name|add
argument_list|(
name|Types
operator|.
name|buildGroup
argument_list|(
name|groupFieldType
operator|.
name|getRepetition
argument_list|()
argument_list|)
operator|.
name|addFields
argument_list|(
name|typesArray
argument_list|)
operator|.
name|named
argument_list|(
name|fieldType
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|schemaTypes
operator|.
name|add
argument_list|(
name|fieldType
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Add type for schema evolution
name|schemaTypes
operator|.
name|add
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|BINARY
argument_list|)
operator|.
name|named
argument_list|(
name|colName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|schemaTypes
return|;
block|}
comment|/**    * Searchs column names by name on a given Parquet message schema, and returns its projected    * Parquet schema types.    *    * @param schema Message type schema where to search for column names.    * @param colNames List of column names.    * @param colTypes List of column types.    * @return A MessageType object of projected columns.    */
specifier|private
specifier|static
name|MessageType
name|getSchemaByName
parameter_list|(
name|MessageType
name|schema
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|colTypes
parameter_list|)
block|{
name|List
argument_list|<
name|Type
argument_list|>
name|projectedFields
init|=
name|getProjectedGroupFields
argument_list|(
name|schema
argument_list|,
name|colNames
argument_list|,
name|colTypes
argument_list|)
decl_stmt|;
name|Type
index|[]
name|typesArray
init|=
name|projectedFields
operator|.
name|toArray
argument_list|(
operator|new
name|Type
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
return|return
name|Types
operator|.
name|buildMessage
argument_list|()
operator|.
name|addFields
argument_list|(
name|typesArray
argument_list|)
operator|.
name|named
argument_list|(
name|schema
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Searchs column names by index on a given Parquet file schema, and returns its corresponded    * Parquet schema types.    *    * @param schema Message schema where to search for column names.    * @param colNames List of column names.    * @param colIndexes List of column indexes.    * @return A MessageType object of the column names found.    */
specifier|private
specifier|static
name|MessageType
name|getSchemaByIndex
parameter_list|(
name|MessageType
name|schema
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|colIndexes
parameter_list|)
block|{
name|List
argument_list|<
name|Type
argument_list|>
name|schemaTypes
init|=
operator|new
name|ArrayList
argument_list|<
name|Type
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Integer
name|i
range|:
name|colIndexes
control|)
block|{
if|if
condition|(
name|i
operator|<
name|colNames
operator|.
name|size
argument_list|()
condition|)
block|{
if|if
condition|(
name|i
operator|<
name|schema
operator|.
name|getFieldCount
argument_list|()
condition|)
block|{
name|schemaTypes
operator|.
name|add
argument_list|(
name|schema
operator|.
name|getType
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|//prefixing with '_mask_' to ensure no conflict with named
comment|//columns in the file schema
name|schemaTypes
operator|.
name|add
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|BINARY
argument_list|)
operator|.
name|named
argument_list|(
literal|"_mask_"
operator|+
name|colNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|MessageType
argument_list|(
name|schema
operator|.
name|getName
argument_list|()
argument_list|,
name|schemaTypes
argument_list|)
return|;
block|}
comment|/**    * It creates the readContext for Parquet side with the requested schema during the init phase.    *    * @param context    * @return the parquet ReadContext    */
annotation|@
name|Override
specifier|public
name|parquet
operator|.
name|hadoop
operator|.
name|api
operator|.
name|ReadSupport
operator|.
name|ReadContext
name|init
parameter_list|(
name|InitContext
name|context
parameter_list|)
block|{
name|Configuration
name|configuration
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|MessageType
name|fileSchema
init|=
name|context
operator|.
name|getFileSchema
argument_list|()
decl_stmt|;
name|String
name|columnNames
init|=
name|configuration
operator|.
name|get
argument_list|(
name|IOConstants
operator|.
name|COLUMNS
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|contextMetadata
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|indexAccess
init|=
name|configuration
operator|.
name|getBoolean
argument_list|(
name|PARQUET_COLUMN_INDEX_ACCESS
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnNames
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|columnNamesList
init|=
name|getColumnNames
argument_list|(
name|columnNames
argument_list|)
decl_stmt|;
name|MessageType
name|tableSchema
decl_stmt|;
if|if
condition|(
name|indexAccess
condition|)
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|indexSequence
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
comment|// Generates a sequence list of indexes
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|columnNamesList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|indexSequence
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|tableSchema
operator|=
name|getSchemaByIndex
argument_list|(
name|fileSchema
argument_list|,
name|columnNamesList
argument_list|,
name|indexSequence
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|columnTypes
init|=
name|configuration
operator|.
name|get
argument_list|(
name|IOConstants
operator|.
name|COLUMNS_TYPES
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypesList
init|=
name|getColumnTypes
argument_list|(
name|columnTypes
argument_list|)
decl_stmt|;
name|tableSchema
operator|=
name|getSchemaByName
argument_list|(
name|fileSchema
argument_list|,
name|columnNamesList
argument_list|,
name|columnTypesList
argument_list|)
expr_stmt|;
block|}
name|contextMetadata
operator|.
name|put
argument_list|(
name|HIVE_TABLE_AS_PARQUET_SCHEMA
argument_list|,
name|tableSchema
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|indexColumnsWanted
init|=
name|ColumnProjectionUtils
operator|.
name|getReadColumnIDs
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|MessageType
name|requestedSchemaByUser
init|=
name|getSchemaByIndex
argument_list|(
name|tableSchema
argument_list|,
name|columnNamesList
argument_list|,
name|indexColumnsWanted
argument_list|)
decl_stmt|;
return|return
operator|new
name|ReadContext
argument_list|(
name|requestedSchemaByUser
argument_list|,
name|contextMetadata
argument_list|)
return|;
block|}
else|else
block|{
name|contextMetadata
operator|.
name|put
argument_list|(
name|HIVE_TABLE_AS_PARQUET_SCHEMA
argument_list|,
name|fileSchema
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|ReadContext
argument_list|(
name|fileSchema
argument_list|,
name|contextMetadata
argument_list|)
return|;
block|}
block|}
comment|/**    *    * It creates the hive read support to interpret data from parquet to hive    *    * @param configuration // unused    * @param keyValueMetaData    * @param fileSchema // unused    * @param readContext containing the requested schema and the schema of the hive table    * @return Record Materialize for Hive    */
annotation|@
name|Override
specifier|public
name|RecordMaterializer
argument_list|<
name|ObjectArrayWritable
argument_list|>
name|prepareForRead
parameter_list|(
specifier|final
name|Configuration
name|configuration
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|keyValueMetaData
parameter_list|,
specifier|final
name|MessageType
name|fileSchema
parameter_list|,
specifier|final
name|parquet
operator|.
name|hadoop
operator|.
name|api
operator|.
name|ReadSupport
operator|.
name|ReadContext
name|readContext
parameter_list|)
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metadata
init|=
name|readContext
operator|.
name|getReadSupportMetadata
argument_list|()
decl_stmt|;
if|if
condition|(
name|metadata
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"ReadContext not initialized properly. "
operator|+
literal|"Don't know the Hive Schema."
argument_list|)
throw|;
block|}
name|String
name|key
init|=
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_PARQUET_TIMESTAMP_SKIP_CONVERSION
operator|.
name|varname
decl_stmt|;
if|if
condition|(
operator|!
name|metadata
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|metadata
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|configuration
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_PARQUET_TIMESTAMP_SKIP_CONVERSION
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|DataWritableRecordConverter
argument_list|(
name|readContext
operator|.
name|getRequestedSchema
argument_list|()
argument_list|,
name|metadata
argument_list|)
return|;
block|}
block|}
end_class

end_unit

