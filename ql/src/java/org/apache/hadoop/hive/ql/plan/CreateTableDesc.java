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
name|exec
operator|.
name|Utilities
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
name|String
name|tableName
decl_stmt|;
name|boolean
name|isExternal
decl_stmt|;
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
name|cols
decl_stmt|;
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|bucketCols
decl_stmt|;
name|ArrayList
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
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapProp
decl_stmt|;
name|boolean
name|ifNotExists
decl_stmt|;
specifier|public
name|CreateTableDesc
parameter_list|()
block|{   }
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
name|mapProp
parameter_list|,
name|boolean
name|ifNotExists
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
name|mapProp
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|mapProp
argument_list|)
expr_stmt|;
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
name|ArrayList
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
name|ArrayList
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
name|ArrayList
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
name|ArrayList
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
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getMapProp
parameter_list|()
block|{
return|return
name|mapProp
return|;
block|}
comment|/**    * @param mapProp    *          the map properties to set    */
specifier|public
name|void
name|setMapProp
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapProp
parameter_list|)
block|{
name|this
operator|.
name|mapProp
operator|=
name|mapProp
expr_stmt|;
block|}
block|}
end_class

end_unit

