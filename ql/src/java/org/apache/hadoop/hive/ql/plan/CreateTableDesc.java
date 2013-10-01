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
name|io
operator|.
name|Serializable
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
name|Map
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
name|hive
operator|.
name|common
operator|.
name|JavaUtils
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
name|metastore
operator|.
name|api
operator|.
name|Order
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
name|ErrorMsg
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
name|HiveFileFormatUtils
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
name|HiveOutputFormat
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
name|BaseSemanticAnalyzer
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
name|ParseUtils
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
name|SemanticException
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
name|SerDeUtils
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

begin_comment
comment|/**  * CreateTableDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Create Table"
argument_list|)
specifier|public
class|class
name|CreateTableDesc
extends|extends
name|DDLDesc
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CreateTableDesc
operator|.
name|class
argument_list|)
decl_stmt|;
name|String
name|databaseName
decl_stmt|;
name|String
name|tableName
decl_stmt|;
name|boolean
name|isExternal
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
decl_stmt|;
name|List
argument_list|<
name|Order
argument_list|>
name|sortCols
decl_stmt|;
name|int
name|numBuckets
decl_stmt|;
name|String
name|fieldDelim
decl_stmt|;
name|String
name|fieldEscape
decl_stmt|;
name|String
name|collItemDelim
decl_stmt|;
name|String
name|mapKeyDelim
decl_stmt|;
name|String
name|lineDelim
decl_stmt|;
name|String
name|comment
decl_stmt|;
name|String
name|inputFormat
decl_stmt|;
name|String
name|outputFormat
decl_stmt|;
name|String
name|location
decl_stmt|;
name|String
name|serName
decl_stmt|;
name|String
name|storageHandler
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serdeProps
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
decl_stmt|;
name|boolean
name|ifNotExists
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|skewedColNames
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|skewedColValues
decl_stmt|;
name|boolean
name|isStoredAsSubDirectories
init|=
literal|false
decl_stmt|;
specifier|public
name|CreateTableDesc
parameter_list|()
block|{   }
specifier|public
name|CreateTableDesc
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|boolean
name|isExternal
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
parameter_list|,
name|List
argument_list|<
name|Order
argument_list|>
name|sortCols
parameter_list|,
name|int
name|numBuckets
parameter_list|,
name|String
name|fieldDelim
parameter_list|,
name|String
name|fieldEscape
parameter_list|,
name|String
name|collItemDelim
parameter_list|,
name|String
name|mapKeyDelim
parameter_list|,
name|String
name|lineDelim
parameter_list|,
name|String
name|comment
parameter_list|,
name|String
name|inputFormat
parameter_list|,
name|String
name|outputFormat
parameter_list|,
name|String
name|location
parameter_list|,
name|String
name|serName
parameter_list|,
name|String
name|storageHandler
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serdeProps
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
parameter_list|,
name|boolean
name|ifNotExists
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|skewedColNames
parameter_list|,
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|skewedColValues
parameter_list|)
block|{
name|this
argument_list|(
name|tableName
argument_list|,
name|isExternal
argument_list|,
name|cols
argument_list|,
name|partCols
argument_list|,
name|bucketCols
argument_list|,
name|sortCols
argument_list|,
name|numBuckets
argument_list|,
name|fieldDelim
argument_list|,
name|fieldEscape
argument_list|,
name|collItemDelim
argument_list|,
name|mapKeyDelim
argument_list|,
name|lineDelim
argument_list|,
name|comment
argument_list|,
name|inputFormat
argument_list|,
name|outputFormat
argument_list|,
name|location
argument_list|,
name|serName
argument_list|,
name|storageHandler
argument_list|,
name|serdeProps
argument_list|,
name|tblProps
argument_list|,
name|ifNotExists
argument_list|,
name|skewedColNames
argument_list|,
name|skewedColValues
argument_list|)
expr_stmt|;
name|this
operator|.
name|databaseName
operator|=
name|databaseName
expr_stmt|;
block|}
specifier|public
name|CreateTableDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|boolean
name|isExternal
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
parameter_list|,
name|List
argument_list|<
name|Order
argument_list|>
name|sortCols
parameter_list|,
name|int
name|numBuckets
parameter_list|,
name|String
name|fieldDelim
parameter_list|,
name|String
name|fieldEscape
parameter_list|,
name|String
name|collItemDelim
parameter_list|,
name|String
name|mapKeyDelim
parameter_list|,
name|String
name|lineDelim
parameter_list|,
name|String
name|comment
parameter_list|,
name|String
name|inputFormat
parameter_list|,
name|String
name|outputFormat
parameter_list|,
name|String
name|location
parameter_list|,
name|String
name|serName
parameter_list|,
name|String
name|storageHandler
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serdeProps
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
parameter_list|,
name|boolean
name|ifNotExists
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|skewedColNames
parameter_list|,
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|skewedColValues
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|isExternal
operator|=
name|isExternal
expr_stmt|;
name|this
operator|.
name|bucketCols
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|bucketCols
argument_list|)
expr_stmt|;
name|this
operator|.
name|sortCols
operator|=
operator|new
name|ArrayList
argument_list|<
name|Order
argument_list|>
argument_list|(
name|sortCols
argument_list|)
expr_stmt|;
name|this
operator|.
name|collItemDelim
operator|=
name|collItemDelim
expr_stmt|;
name|this
operator|.
name|cols
operator|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
name|this
operator|.
name|fieldDelim
operator|=
name|fieldDelim
expr_stmt|;
name|this
operator|.
name|fieldEscape
operator|=
name|fieldEscape
expr_stmt|;
name|this
operator|.
name|inputFormat
operator|=
name|inputFormat
expr_stmt|;
name|this
operator|.
name|outputFormat
operator|=
name|outputFormat
expr_stmt|;
name|this
operator|.
name|lineDelim
operator|=
name|lineDelim
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
name|this
operator|.
name|mapKeyDelim
operator|=
name|mapKeyDelim
expr_stmt|;
name|this
operator|.
name|numBuckets
operator|=
name|numBuckets
expr_stmt|;
name|this
operator|.
name|partCols
operator|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
name|partCols
argument_list|)
expr_stmt|;
name|this
operator|.
name|serName
operator|=
name|serName
expr_stmt|;
name|this
operator|.
name|storageHandler
operator|=
name|storageHandler
expr_stmt|;
name|this
operator|.
name|serdeProps
operator|=
name|serdeProps
expr_stmt|;
name|this
operator|.
name|tblProps
operator|=
name|tblProps
expr_stmt|;
name|this
operator|.
name|ifNotExists
operator|=
name|ifNotExists
expr_stmt|;
name|this
operator|.
name|skewedColNames
operator|=
name|copyList
argument_list|(
name|skewedColNames
argument_list|)
expr_stmt|;
name|this
operator|.
name|skewedColValues
operator|=
name|copyList
argument_list|(
name|skewedColValues
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|copyList
parameter_list|(
name|List
argument_list|<
name|T
argument_list|>
name|copy
parameter_list|)
block|{
return|return
name|copy
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|ArrayList
argument_list|<
name|T
argument_list|>
argument_list|(
name|copy
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"columns"
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getColsString
parameter_list|()
block|{
return|return
name|Utilities
operator|.
name|getFieldSchemaString
argument_list|(
name|getCols
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"partition columns"
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getPartColsString
parameter_list|()
block|{
return|return
name|Utilities
operator|.
name|getFieldSchemaString
argument_list|(
name|getPartCols
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"if not exists"
argument_list|)
specifier|public
name|boolean
name|getIfNotExists
parameter_list|()
block|{
return|return
name|ifNotExists
return|;
block|}
specifier|public
name|void
name|setIfNotExists
parameter_list|(
name|boolean
name|ifNotExists
parameter_list|)
block|{
name|this
operator|.
name|ifNotExists
operator|=
name|ifNotExists
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"name"
argument_list|)
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
specifier|public
name|String
name|getDatabaseName
parameter_list|()
block|{
return|return
name|databaseName
return|;
block|}
specifier|public
name|void
name|setTableName
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getCols
parameter_list|()
block|{
return|return
name|cols
return|;
block|}
specifier|public
name|void
name|setCols
parameter_list|(
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|)
block|{
name|this
operator|.
name|cols
operator|=
name|cols
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getPartCols
parameter_list|()
block|{
return|return
name|partCols
return|;
block|}
specifier|public
name|void
name|setPartCols
parameter_list|(
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
parameter_list|)
block|{
name|this
operator|.
name|partCols
operator|=
name|partCols
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"bucket columns"
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getBucketCols
parameter_list|()
block|{
return|return
name|bucketCols
return|;
block|}
specifier|public
name|void
name|setBucketCols
parameter_list|(
name|ArrayList
argument_list|<
name|String
argument_list|>
name|bucketCols
parameter_list|)
block|{
name|this
operator|.
name|bucketCols
operator|=
name|bucketCols
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"# buckets"
argument_list|)
specifier|public
name|int
name|getNumBuckets
parameter_list|()
block|{
return|return
name|numBuckets
return|;
block|}
specifier|public
name|void
name|setNumBuckets
parameter_list|(
name|int
name|numBuckets
parameter_list|)
block|{
name|this
operator|.
name|numBuckets
operator|=
name|numBuckets
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"field delimiter"
argument_list|)
specifier|public
name|String
name|getFieldDelim
parameter_list|()
block|{
return|return
name|fieldDelim
return|;
block|}
specifier|public
name|void
name|setFieldDelim
parameter_list|(
name|String
name|fieldDelim
parameter_list|)
block|{
name|this
operator|.
name|fieldDelim
operator|=
name|fieldDelim
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"field escape"
argument_list|)
specifier|public
name|String
name|getFieldEscape
parameter_list|()
block|{
return|return
name|fieldEscape
return|;
block|}
specifier|public
name|void
name|setFieldEscape
parameter_list|(
name|String
name|fieldEscape
parameter_list|)
block|{
name|this
operator|.
name|fieldEscape
operator|=
name|fieldEscape
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"collection delimiter"
argument_list|)
specifier|public
name|String
name|getCollItemDelim
parameter_list|()
block|{
return|return
name|collItemDelim
return|;
block|}
specifier|public
name|void
name|setCollItemDelim
parameter_list|(
name|String
name|collItemDelim
parameter_list|)
block|{
name|this
operator|.
name|collItemDelim
operator|=
name|collItemDelim
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"map key delimiter"
argument_list|)
specifier|public
name|String
name|getMapKeyDelim
parameter_list|()
block|{
return|return
name|mapKeyDelim
return|;
block|}
specifier|public
name|void
name|setMapKeyDelim
parameter_list|(
name|String
name|mapKeyDelim
parameter_list|)
block|{
name|this
operator|.
name|mapKeyDelim
operator|=
name|mapKeyDelim
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"line delimiter"
argument_list|)
specifier|public
name|String
name|getLineDelim
parameter_list|()
block|{
return|return
name|lineDelim
return|;
block|}
specifier|public
name|void
name|setLineDelim
parameter_list|(
name|String
name|lineDelim
parameter_list|)
block|{
name|this
operator|.
name|lineDelim
operator|=
name|lineDelim
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"comment"
argument_list|)
specifier|public
name|String
name|getComment
parameter_list|()
block|{
return|return
name|comment
return|;
block|}
specifier|public
name|void
name|setComment
parameter_list|(
name|String
name|comment
parameter_list|)
block|{
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"input format"
argument_list|)
specifier|public
name|String
name|getInputFormat
parameter_list|()
block|{
return|return
name|inputFormat
return|;
block|}
specifier|public
name|void
name|setInputFormat
parameter_list|(
name|String
name|inputFormat
parameter_list|)
block|{
name|this
operator|.
name|inputFormat
operator|=
name|inputFormat
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"output format"
argument_list|)
specifier|public
name|String
name|getOutputFormat
parameter_list|()
block|{
return|return
name|outputFormat
return|;
block|}
specifier|public
name|void
name|setOutputFormat
parameter_list|(
name|String
name|outputFormat
parameter_list|)
block|{
name|this
operator|.
name|outputFormat
operator|=
name|outputFormat
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"storage handler"
argument_list|)
specifier|public
name|String
name|getStorageHandler
parameter_list|()
block|{
return|return
name|storageHandler
return|;
block|}
specifier|public
name|void
name|setStorageHandler
parameter_list|(
name|String
name|storageHandler
parameter_list|)
block|{
name|this
operator|.
name|storageHandler
operator|=
name|storageHandler
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"location"
argument_list|)
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
specifier|public
name|void
name|setLocation
parameter_list|(
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"isExternal"
argument_list|)
specifier|public
name|boolean
name|isExternal
parameter_list|()
block|{
return|return
name|isExternal
return|;
block|}
specifier|public
name|void
name|setExternal
parameter_list|(
name|boolean
name|isExternal
parameter_list|)
block|{
name|this
operator|.
name|isExternal
operator|=
name|isExternal
expr_stmt|;
block|}
comment|/**    * @return the sortCols    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"sort columns"
argument_list|)
specifier|public
name|List
argument_list|<
name|Order
argument_list|>
name|getSortCols
parameter_list|()
block|{
return|return
name|sortCols
return|;
block|}
comment|/**    * @param sortCols    *          the sortCols to set    */
specifier|public
name|void
name|setSortCols
parameter_list|(
name|ArrayList
argument_list|<
name|Order
argument_list|>
name|sortCols
parameter_list|)
block|{
name|this
operator|.
name|sortCols
operator|=
name|sortCols
expr_stmt|;
block|}
comment|/**    * @return the serDeName    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"serde name"
argument_list|)
specifier|public
name|String
name|getSerName
parameter_list|()
block|{
return|return
name|serName
return|;
block|}
comment|/**    * @param serName    *          the serName to set    */
specifier|public
name|void
name|setSerName
parameter_list|(
name|String
name|serName
parameter_list|)
block|{
name|this
operator|.
name|serName
operator|=
name|serName
expr_stmt|;
block|}
comment|/**    * @return the serDe properties    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"serde properties"
argument_list|)
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getSerdeProps
parameter_list|()
block|{
return|return
name|serdeProps
return|;
block|}
comment|/**    * @param serdeProps    *          the serde properties to set    */
specifier|public
name|void
name|setSerdeProps
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serdeProps
parameter_list|)
block|{
name|this
operator|.
name|serdeProps
operator|=
name|serdeProps
expr_stmt|;
block|}
comment|/**    * @return the table properties    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"table properties"
argument_list|)
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getTblProps
parameter_list|()
block|{
return|return
name|tblProps
return|;
block|}
comment|/**    * @param tblProps    *          the table properties to set    */
specifier|public
name|void
name|setTblProps
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
parameter_list|)
block|{
name|this
operator|.
name|tblProps
operator|=
name|tblProps
expr_stmt|;
block|}
comment|/**    * @return the skewedColNames    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getSkewedColNames
parameter_list|()
block|{
return|return
name|skewedColNames
return|;
block|}
comment|/**    * @param skewedColNames the skewedColNames to set    */
specifier|public
name|void
name|setSkewedColNames
parameter_list|(
name|ArrayList
argument_list|<
name|String
argument_list|>
name|skewedColNames
parameter_list|)
block|{
name|this
operator|.
name|skewedColNames
operator|=
name|skewedColNames
expr_stmt|;
block|}
comment|/**    * @return the skewedColValues    */
specifier|public
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getSkewedColValues
parameter_list|()
block|{
return|return
name|skewedColValues
return|;
block|}
comment|/**    * @param skewedColValues the skewedColValues to set    */
specifier|public
name|void
name|setSkewedColValues
parameter_list|(
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|skewedColValues
parameter_list|)
block|{
name|this
operator|.
name|skewedColValues
operator|=
name|skewedColValues
expr_stmt|;
block|}
specifier|public
name|void
name|validate
parameter_list|()
throws|throws
name|SemanticException
block|{
if|if
condition|(
operator|(
name|this
operator|.
name|getCols
argument_list|()
operator|==
literal|null
operator|)
operator|||
operator|(
name|this
operator|.
name|getCols
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|)
condition|)
block|{
comment|// for now make sure that serde exists
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|this
operator|.
name|getSerName
argument_list|()
argument_list|)
operator|||
operator|!
name|SerDeUtils
operator|.
name|shouldGetColsFromSerDe
argument_list|(
name|this
operator|.
name|getSerName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_TBL_DDL_SERDE
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
return|return;
block|}
if|if
condition|(
name|this
operator|.
name|getStorageHandler
argument_list|()
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|origin
init|=
name|Class
operator|.
name|forName
argument_list|(
name|this
operator|.
name|getOutputFormat
argument_list|()
argument_list|,
literal|true
argument_list|,
name|JavaUtils
operator|.
name|getClassLoader
argument_list|()
argument_list|)
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
name|replaced
init|=
name|HiveFileFormatUtils
operator|.
name|getOutputFormatSubstitute
argument_list|(
name|origin
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|replaced
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_OUTPUT_FORMAT_TYPE
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_OUTPUT_FORMAT_TYPE
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
name|ParseUtils
operator|.
name|validateColumnNameUniqueness
argument_list|(
name|this
operator|.
name|getCols
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|getBucketCols
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// all columns in cluster and sort are valid columns
name|Iterator
argument_list|<
name|String
argument_list|>
name|bucketCols
init|=
name|this
operator|.
name|getBucketCols
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|bucketCols
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|bucketCol
init|=
name|bucketCols
operator|.
name|next
argument_list|()
decl_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|colNamesIter
init|=
name|colNames
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|colNamesIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|colName
init|=
name|colNamesIter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|bucketCol
operator|.
name|equalsIgnoreCase
argument_list|(
name|colName
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|found
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_COLUMN
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
name|this
operator|.
name|getSortCols
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// all columns in cluster and sort are valid columns
name|Iterator
argument_list|<
name|Order
argument_list|>
name|sortCols
init|=
name|this
operator|.
name|getSortCols
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|sortCols
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|sortCol
init|=
name|sortCols
operator|.
name|next
argument_list|()
operator|.
name|getCol
argument_list|()
decl_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|colNamesIter
init|=
name|colNames
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|colNamesIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|colName
init|=
name|colNamesIter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|sortCol
operator|.
name|equalsIgnoreCase
argument_list|(
name|colName
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|found
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_COLUMN
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
name|this
operator|.
name|getPartCols
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// there is no overlap between columns and partitioning columns
name|Iterator
argument_list|<
name|FieldSchema
argument_list|>
name|partColsIter
init|=
name|this
operator|.
name|getPartCols
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|partColsIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|FieldSchema
name|fs
init|=
name|partColsIter
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|partCol
init|=
name|fs
operator|.
name|getName
argument_list|()
decl_stmt|;
name|TypeInfo
name|pti
init|=
literal|null
decl_stmt|;
try|try
block|{
name|pti
operator|=
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|fs
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|err
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
literal|null
operator|==
name|pti
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|PARTITION_COLUMN_NON_PRIMITIVE
operator|.
name|getMsg
argument_list|()
operator|+
literal|" Found "
operator|+
name|partCol
operator|+
literal|" of type: "
operator|+
name|fs
operator|.
name|getType
argument_list|()
argument_list|)
throw|;
block|}
name|Iterator
argument_list|<
name|String
argument_list|>
name|colNamesIter
init|=
name|colNames
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|colNamesIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|colName
init|=
name|BaseSemanticAnalyzer
operator|.
name|unescapeIdentifier
argument_list|(
name|colNamesIter
operator|.
name|next
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|partCol
operator|.
name|equalsIgnoreCase
argument_list|(
name|colName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|COLUMN_REPEATED_IN_PARTITIONING_COLS
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
block|}
comment|/* Validate skewed information. */
name|ValidationUtility
operator|.
name|validateSkewedInformation
argument_list|(
name|colNames
argument_list|,
name|this
operator|.
name|getSkewedColNames
argument_list|()
argument_list|,
name|this
operator|.
name|getSkewedColValues
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return the isStoredAsSubDirectories    */
specifier|public
name|boolean
name|isStoredAsSubDirectories
parameter_list|()
block|{
return|return
name|isStoredAsSubDirectories
return|;
block|}
comment|/**    * @param isStoredAsSubDirectories the isStoredAsSubDirectories to set    */
specifier|public
name|void
name|setStoredAsSubDirectories
parameter_list|(
name|boolean
name|isStoredAsSubDirectories
parameter_list|)
block|{
name|this
operator|.
name|isStoredAsSubDirectories
operator|=
name|isStoredAsSubDirectories
expr_stmt|;
block|}
block|}
end_class

end_unit

