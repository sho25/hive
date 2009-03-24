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
name|ql
operator|.
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
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
name|hive
operator|.
name|metastore
operator|.
name|MetaStoreUtils
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|exec
operator|.
name|ColumnInfo
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
name|exec
operator|.
name|Operator
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
name|exec
operator|.
name|RowSchema
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
name|exec
operator|.
name|Utilities
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
name|IgnoreKeyTextOutputFormat
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
name|parse
operator|.
name|TypeCheckProcFactory
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
name|Constants
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
name|MetadataTypedColumnsetSerDe
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
name|dynamic_type
operator|.
name|DynamicSerDe
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
name|thrift
operator|.
name|TBinarySortableProtocol
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
name|SequenceFileInputFormat
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
name|SequenceFileOutputFormat
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
name|TextInputFormat
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_class
specifier|public
class|class
name|PlanUtils
block|{
specifier|public
specifier|static
enum|enum
name|ExpressionTypes
block|{
name|FIELD
block|,
name|JEXL
block|}
empty_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
specifier|static
name|mapredWork
name|getMapRedWork
parameter_list|()
block|{
return|return
operator|new
name|mapredWork
argument_list|(
literal|""
argument_list|,
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
argument_list|,
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|partitionDesc
argument_list|>
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
argument_list|,
operator|new
name|tableDesc
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|tableDesc
argument_list|>
argument_list|()
argument_list|,
literal|null
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
comment|/**     * Generate the table descriptor of MetadataTypedColumnsetSerDe with the separatorCode    * and column names (comma separated string).    */
specifier|public
specifier|static
name|tableDesc
name|getDefaultTableDesc
parameter_list|(
name|String
name|separatorCode
parameter_list|,
name|String
name|columns
parameter_list|)
block|{
return|return
name|getDefaultTableDesc
argument_list|(
name|separatorCode
argument_list|,
name|columns
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**     * Generate the table descriptor of MetadataTypedColumnsetSerDe with the separatorCode    * and column names (comma separated string), and whether the last column should take    * the rest of the line.    */
specifier|public
specifier|static
name|tableDesc
name|getDefaultTableDesc
parameter_list|(
name|String
name|separatorCode
parameter_list|,
name|String
name|columns
parameter_list|,
name|boolean
name|lastColumnTakesRestOfTheLine
parameter_list|)
block|{
name|Properties
name|properties
init|=
name|Utilities
operator|.
name|makeProperties
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
name|separatorCode
argument_list|,
literal|"columns"
argument_list|,
name|columns
argument_list|)
decl_stmt|;
if|if
condition|(
name|lastColumnTakesRestOfTheLine
condition|)
block|{
name|properties
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_LAST_COLUMN_TAKES_REST
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|tableDesc
argument_list|(
name|LazySimpleSerDe
operator|.
name|class
argument_list|,
name|TextInputFormat
operator|.
name|class
argument_list|,
name|IgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|,
name|properties
argument_list|)
return|;
block|}
comment|/**     * Generate the table descriptor of MetadataTypedColumnsetSerDe with the separatorCode.    * MetaDataTypedColumnsetSerDe is used because LazySimpleSerDe does not support a table    * with a single column "col" with type "array<string>".    */
specifier|public
specifier|static
name|tableDesc
name|getDefaultTableDesc
parameter_list|(
name|String
name|separatorCode
parameter_list|)
block|{
return|return
operator|new
name|tableDesc
argument_list|(
name|MetadataTypedColumnsetSerDe
operator|.
name|class
argument_list|,
name|TextInputFormat
operator|.
name|class
argument_list|,
name|IgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|,
name|Utilities
operator|.
name|makeProperties
argument_list|(
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
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
name|separatorCode
argument_list|)
argument_list|)
return|;
block|}
comment|/**     * Generate the table descriptor of DynamicSerDe and TBinarySortableProtocol.    */
specifier|public
specifier|static
name|tableDesc
name|getBinarySortableTableDesc
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldSchemas
parameter_list|,
name|String
name|order
parameter_list|)
block|{
name|String
name|structName
init|=
literal|"binary_sortable_table"
decl_stmt|;
return|return
operator|new
name|tableDesc
argument_list|(
name|DynamicSerDe
operator|.
name|class
argument_list|,
name|SequenceFileInputFormat
operator|.
name|class
argument_list|,
name|SequenceFileOutputFormat
operator|.
name|class
argument_list|,
name|Utilities
operator|.
name|makeProperties
argument_list|(
literal|"name"
argument_list|,
name|structName
argument_list|,
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
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
name|TBinarySortableProtocol
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
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
name|Constants
operator|.
name|SERIALIZATION_DDL
argument_list|,
name|MetaStoreUtils
operator|.
name|getDDLFromFieldSchema
argument_list|(
name|structName
argument_list|,
name|fieldSchemas
argument_list|)
argument_list|,
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
name|Constants
operator|.
name|SERIALIZATION_SORT_ORDER
argument_list|,
name|order
argument_list|)
argument_list|)
return|;
block|}
comment|/**     * Generate the table descriptor of DynamicSerDe and TBinaryProtocol.    */
specifier|public
specifier|static
name|tableDesc
name|getBinaryTableDesc
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldSchemas
parameter_list|)
block|{
name|String
name|structName
init|=
literal|"binary_table"
decl_stmt|;
return|return
operator|new
name|tableDesc
argument_list|(
name|DynamicSerDe
operator|.
name|class
argument_list|,
name|SequenceFileInputFormat
operator|.
name|class
argument_list|,
name|SequenceFileOutputFormat
operator|.
name|class
argument_list|,
name|Utilities
operator|.
name|makeProperties
argument_list|(
literal|"name"
argument_list|,
name|structName
argument_list|,
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
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
name|TBinaryProtocol
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
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
name|Constants
operator|.
name|SERIALIZATION_DDL
argument_list|,
name|MetaStoreUtils
operator|.
name|getDDLFromFieldSchema
argument_list|(
name|structName
argument_list|,
name|fieldSchemas
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/**     * Generate the table descriptor of LazySimpleSerDe.    */
specifier|public
specifier|static
name|tableDesc
name|getLazySimpleSerDeTableDesc
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldSchemas
parameter_list|)
block|{
return|return
operator|new
name|tableDesc
argument_list|(
name|LazySimpleSerDe
operator|.
name|class
argument_list|,
name|SequenceFileInputFormat
operator|.
name|class
argument_list|,
name|SequenceFileOutputFormat
operator|.
name|class
argument_list|,
name|Utilities
operator|.
name|makeProperties
argument_list|(
literal|"columns"
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnNamesFromFieldSchema
argument_list|(
name|fieldSchemas
argument_list|)
argument_list|,
literal|"columns.types"
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnTypesFromFieldSchema
argument_list|(
name|fieldSchemas
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/**     * Convert the ColumnList to FieldSchema list.    */
specifier|public
specifier|static
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getFieldSchemasFromColumnList
parameter_list|(
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|cols
parameter_list|,
name|String
name|fieldPrefix
parameter_list|)
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schemas
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
name|cols
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
name|cols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|schemas
operator|.
name|add
argument_list|(
name|MetaStoreUtils
operator|.
name|getFieldSchemaFromTypeInfo
argument_list|(
name|fieldPrefix
operator|+
name|i
argument_list|,
name|cols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|schemas
return|;
block|}
comment|/**     * Convert the RowSchema to FieldSchema list.    */
specifier|public
specifier|static
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getFieldSchemasFromRowSchema
parameter_list|(
name|RowSchema
name|row
parameter_list|,
name|String
name|fieldPrefix
parameter_list|)
block|{
name|Vector
argument_list|<
name|ColumnInfo
argument_list|>
name|c
init|=
name|row
operator|.
name|getSignature
argument_list|()
decl_stmt|;
return|return
name|getFieldSchemasFromColumnInfo
argument_list|(
name|c
argument_list|,
name|fieldPrefix
argument_list|)
return|;
block|}
comment|/**     * Convert the ColumnInfo to FieldSchema.    */
specifier|public
specifier|static
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getFieldSchemasFromColumnInfo
parameter_list|(
name|Vector
argument_list|<
name|ColumnInfo
argument_list|>
name|cols
parameter_list|,
name|String
name|fieldPrefix
parameter_list|)
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schemas
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
name|cols
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
name|cols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|name
init|=
name|cols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getInternalName
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
name|name
operator|=
name|fieldPrefix
operator|+
name|name
expr_stmt|;
block|}
name|schemas
operator|.
name|add
argument_list|(
name|MetaStoreUtils
operator|.
name|getFieldSchemaFromTypeInfo
argument_list|(
name|name
argument_list|,
name|cols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|schemas
return|;
block|}
comment|/**    * Create the reduce sink descriptor.    * @param keyCols   The columns to be stored in the key    * @param valueCols The columns to be stored in the value    * @param tag       The tag for this reducesink    * @param partitionCols The columns for partitioning.    * @param numReducers  The number of reducers, set to -1 for automatic inference     *                     based on input data size.    * @return The reduceSinkDesc object.    */
specifier|public
specifier|static
name|reduceSinkDesc
name|getReduceSinkDesc
parameter_list|(
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|keyCols
parameter_list|,
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|valueCols
parameter_list|,
name|int
name|tag
parameter_list|,
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|partitionCols
parameter_list|,
name|String
name|order
parameter_list|,
name|int
name|numReducers
parameter_list|)
block|{
return|return
operator|new
name|reduceSinkDesc
argument_list|(
name|keyCols
argument_list|,
name|valueCols
argument_list|,
name|tag
argument_list|,
name|partitionCols
argument_list|,
name|numReducers
argument_list|,
name|getBinarySortableTableDesc
argument_list|(
name|getFieldSchemasFromColumnList
argument_list|(
name|keyCols
argument_list|,
literal|"reducesinkkey"
argument_list|)
argument_list|,
name|order
argument_list|)
argument_list|,
comment|// Revert to DynamicSerDe: getBinaryTableDesc(getFieldSchemasFromColumnList(valueCols, "reducesinkvalue")));
name|getLazySimpleSerDeTableDesc
argument_list|(
name|getFieldSchemasFromColumnList
argument_list|(
name|valueCols
argument_list|,
literal|"reducesinkvalue"
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Create the reduce sink descriptor.    * @param keyCols   The columns to be stored in the key    * @param valueCols The columns to be stored in the value    * @param tag       The tag for this reducesink    * @param numPartitionFields  The first numPartitionFields of keyCols will be partition columns.    *                  If numPartitionFields=-1, then partition randomly.    * @param numReducers  The number of reducers, set to -1 for automatic inference     *                     based on input data size.    * @return The reduceSinkDesc object.    */
specifier|public
specifier|static
name|reduceSinkDesc
name|getReduceSinkDesc
parameter_list|(
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|keyCols
parameter_list|,
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|valueCols
parameter_list|,
name|int
name|tag
parameter_list|,
name|int
name|numPartitionFields
parameter_list|,
name|int
name|numReducers
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|partitionCols
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|numPartitionFields
operator|>=
name|keyCols
operator|.
name|size
argument_list|()
condition|)
block|{
name|partitionCols
operator|=
name|keyCols
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|numPartitionFields
operator|>=
literal|0
condition|)
block|{
name|partitionCols
operator|=
operator|new
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
argument_list|(
name|numPartitionFields
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numPartitionFields
condition|;
name|i
operator|++
control|)
block|{
name|partitionCols
operator|.
name|add
argument_list|(
name|keyCols
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// numPartitionFields = -1 means random partitioning
name|partitionCols
operator|=
operator|new
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|partitionCols
operator|.
name|add
argument_list|(
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"rand"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|StringBuilder
name|order
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
name|keyCols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|order
operator|.
name|append
argument_list|(
literal|"+"
argument_list|)
expr_stmt|;
block|}
return|return
name|getReduceSinkDesc
argument_list|(
name|keyCols
argument_list|,
name|valueCols
argument_list|,
name|tag
argument_list|,
name|partitionCols
argument_list|,
name|order
operator|.
name|toString
argument_list|()
argument_list|,
name|numReducers
argument_list|)
return|;
block|}
block|}
end_class

end_unit

