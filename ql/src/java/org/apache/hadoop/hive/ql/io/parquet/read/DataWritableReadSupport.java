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
name|MessageType
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|MessageTypeParser
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|PrimitiveType
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
name|Type
operator|.
name|Repetition
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
name|ArrayWritable
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_SCHEMA
init|=
literal|"table_schema"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_SCHEMA_KEY
init|=
literal|"HIVE_TABLE_SCHEMA"
decl_stmt|;
comment|/**    * From a string which columns names (including hive column), return a list    * of string columns    *    * @param comma separated list of columns    * @return list with virtual columns removed    */
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getColumns
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
comment|/**    *    * It creates the readContext for Parquet side with the requested schema during the init phase.    *    * @param configuration needed to get the wanted columns    * @param keyValueMetaData // unused    * @param fileSchema parquet file schema    * @return the parquet ReadContext    */
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
parameter_list|)
block|{
specifier|final
name|String
name|columns
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
specifier|final
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
if|if
condition|(
name|columns
operator|!=
literal|null
condition|)
block|{
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|listColumns
init|=
name|getColumns
argument_list|(
name|columns
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Type
argument_list|>
name|typeListTable
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
specifier|final
name|String
name|col
range|:
name|listColumns
control|)
block|{
comment|// listColumns contains partition columns which are metadata only
if|if
condition|(
name|fileSchema
operator|.
name|containsField
argument_list|(
name|col
argument_list|)
condition|)
block|{
name|typeListTable
operator|.
name|add
argument_list|(
name|fileSchema
operator|.
name|getType
argument_list|(
name|col
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|MessageType
name|tableSchema
init|=
operator|new
name|MessageType
argument_list|(
name|TABLE_SCHEMA
argument_list|,
name|typeListTable
argument_list|)
decl_stmt|;
name|contextMetadata
operator|.
name|put
argument_list|(
name|HIVE_SCHEMA_KEY
argument_list|,
name|tableSchema
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|MessageType
name|requestedSchemaByUser
init|=
name|tableSchema
decl_stmt|;
specifier|final
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
specifier|final
name|List
argument_list|<
name|Type
argument_list|>
name|typeListWanted
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
specifier|final
name|Integer
name|idx
range|:
name|indexColumnsWanted
control|)
block|{
name|typeListWanted
operator|.
name|add
argument_list|(
name|tableSchema
operator|.
name|getType
argument_list|(
name|listColumns
operator|.
name|get
argument_list|(
name|idx
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|requestedSchemaByUser
operator|=
operator|new
name|MessageType
argument_list|(
name|fileSchema
operator|.
name|getName
argument_list|()
argument_list|,
name|typeListWanted
argument_list|)
expr_stmt|;
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
name|HIVE_SCHEMA_KEY
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
name|ArrayWritable
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
specifier|final
name|MessageType
name|tableSchema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
name|metadata
operator|.
name|get
argument_list|(
name|HIVE_SCHEMA_KEY
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|DataWritableRecordConverter
argument_list|(
name|readContext
operator|.
name|getRequestedSchema
argument_list|()
argument_list|,
name|tableSchema
argument_list|)
return|;
block|}
block|}
end_class

end_unit

