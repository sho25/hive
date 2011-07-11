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
comment|/**  * AlterTableDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Alter Table"
argument_list|)
specifier|public
class|class
name|AlterTableDesc
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
comment|/**    * alterTableTypes.    *    */
specifier|public
specifier|static
enum|enum
name|AlterTableTypes
block|{
name|RENAME
block|,
name|ADDCOLS
block|,
name|REPLACECOLS
block|,
name|ADDPROPS
block|,
name|ADDSERDE
block|,
name|ADDSERDEPROPS
block|,
name|ADDFILEFORMAT
block|,
name|ADDCLUSTERSORTCOLUMN
block|,
name|RENAMECOLUMN
block|,
name|ADDPARTITION
block|,
name|TOUCH
block|,
name|ARCHIVE
block|,
name|UNARCHIVE
block|,
name|ALTERPROTECTMODE
block|,
name|ALTERPARTITIONPROTECTMODE
block|,
name|ALTERLOCATION
block|,
name|DROPPARTITION
block|}
empty_stmt|;
specifier|public
specifier|static
enum|enum
name|ProtectModeType
block|{
name|NO_DROP
block|,
name|OFFLINE
block|,
name|READ_ONLY
block|}
empty_stmt|;
name|AlterTableTypes
name|op
decl_stmt|;
name|String
name|oldName
decl_stmt|;
name|String
name|newName
decl_stmt|;
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
name|newCols
decl_stmt|;
name|String
name|serdeName
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
decl_stmt|;
name|String
name|inputFormat
decl_stmt|;
name|String
name|outputFormat
decl_stmt|;
name|String
name|storageHandler
decl_stmt|;
name|int
name|numberBuckets
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|bucketColumns
decl_stmt|;
name|ArrayList
argument_list|<
name|Order
argument_list|>
name|sortColumns
decl_stmt|;
name|String
name|oldColName
decl_stmt|;
name|String
name|newColName
decl_stmt|;
name|String
name|newColType
decl_stmt|;
name|String
name|newColComment
decl_stmt|;
name|boolean
name|first
decl_stmt|;
name|String
name|afterCol
decl_stmt|;
name|boolean
name|expectView
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
decl_stmt|;
specifier|private
name|String
name|newLocation
decl_stmt|;
name|boolean
name|protectModeEnable
decl_stmt|;
name|ProtectModeType
name|protectModeType
decl_stmt|;
specifier|public
name|AlterTableDesc
parameter_list|()
block|{   }
comment|/**    * @param tblName    *          table name    * @param oldColName    *          old column name    * @param newColName    *          new column name    * @param newComment    * @param newType    */
specifier|public
name|AlterTableDesc
parameter_list|(
name|String
name|tblName
parameter_list|,
name|String
name|oldColName
parameter_list|,
name|String
name|newColName
parameter_list|,
name|String
name|newType
parameter_list|,
name|String
name|newComment
parameter_list|,
name|boolean
name|first
parameter_list|,
name|String
name|afterCol
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|oldName
operator|=
name|tblName
expr_stmt|;
name|this
operator|.
name|oldColName
operator|=
name|oldColName
expr_stmt|;
name|this
operator|.
name|newColName
operator|=
name|newColName
expr_stmt|;
name|newColType
operator|=
name|newType
expr_stmt|;
name|newColComment
operator|=
name|newComment
expr_stmt|;
name|this
operator|.
name|first
operator|=
name|first
expr_stmt|;
name|this
operator|.
name|afterCol
operator|=
name|afterCol
expr_stmt|;
name|op
operator|=
name|AlterTableTypes
operator|.
name|RENAMECOLUMN
expr_stmt|;
block|}
comment|/**    * @param oldName    *          old name of the table    * @param newName    *          new name of the table    */
specifier|public
name|AlterTableDesc
parameter_list|(
name|String
name|oldName
parameter_list|,
name|String
name|newName
parameter_list|,
name|boolean
name|expectView
parameter_list|)
block|{
name|op
operator|=
name|AlterTableTypes
operator|.
name|RENAME
expr_stmt|;
name|this
operator|.
name|oldName
operator|=
name|oldName
expr_stmt|;
name|this
operator|.
name|newName
operator|=
name|newName
expr_stmt|;
name|this
operator|.
name|expectView
operator|=
name|expectView
expr_stmt|;
block|}
comment|/**    * @param name    *          name of the table    * @param newCols    *          new columns to be added    */
specifier|public
name|AlterTableDesc
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|newCols
parameter_list|,
name|AlterTableTypes
name|alterType
parameter_list|)
block|{
name|op
operator|=
name|alterType
expr_stmt|;
name|oldName
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|newCols
operator|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
name|newCols
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param alterType    *          type of alter op    */
specifier|public
name|AlterTableDesc
parameter_list|(
name|AlterTableTypes
name|alterType
parameter_list|)
block|{
name|this
argument_list|(
name|alterType
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param alterType    *          type of alter op    */
specifier|public
name|AlterTableDesc
parameter_list|(
name|AlterTableTypes
name|alterType
parameter_list|,
name|boolean
name|expectView
parameter_list|)
block|{
name|op
operator|=
name|alterType
expr_stmt|;
name|this
operator|.
name|expectView
operator|=
name|expectView
expr_stmt|;
block|}
comment|/**    *    * @param name    *          name of the table    * @param inputFormat    *          new table input format    * @param outputFormat    *          new table output format    * @param partSpec    */
specifier|public
name|AlterTableDesc
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|inputFormat
parameter_list|,
name|String
name|outputFormat
parameter_list|,
name|String
name|serdeName
parameter_list|,
name|String
name|storageHandler
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|op
operator|=
name|AlterTableTypes
operator|.
name|ADDFILEFORMAT
expr_stmt|;
name|oldName
operator|=
name|name
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
name|serdeName
operator|=
name|serdeName
expr_stmt|;
name|this
operator|.
name|storageHandler
operator|=
name|storageHandler
expr_stmt|;
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
block|}
specifier|public
name|AlterTableDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|int
name|numBuckets
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
parameter_list|)
block|{
name|oldName
operator|=
name|tableName
expr_stmt|;
name|op
operator|=
name|AlterTableTypes
operator|.
name|ADDCLUSTERSORTCOLUMN
expr_stmt|;
name|numberBuckets
operator|=
name|numBuckets
expr_stmt|;
name|bucketColumns
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
name|sortColumns
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
block|}
specifier|public
name|AlterTableDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|String
name|newLocation
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|op
operator|=
name|AlterTableTypes
operator|.
name|ALTERLOCATION
expr_stmt|;
name|this
operator|.
name|oldName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|newLocation
operator|=
name|newLocation
expr_stmt|;
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"new columns"
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getNewColsString
parameter_list|()
block|{
return|return
name|Utilities
operator|.
name|getFieldSchemaString
argument_list|(
name|getNewCols
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"type"
argument_list|)
specifier|public
name|String
name|getAlterTableTypeString
parameter_list|()
block|{
switch|switch
condition|(
name|op
condition|)
block|{
case|case
name|RENAME
case|:
return|return
literal|"rename"
return|;
case|case
name|ADDCOLS
case|:
return|return
literal|"add columns"
return|;
case|case
name|REPLACECOLS
case|:
return|return
literal|"replace columns"
return|;
block|}
return|return
literal|"unknown"
return|;
block|}
comment|/**    * @return the old name of the table    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"old name"
argument_list|)
specifier|public
name|String
name|getOldName
parameter_list|()
block|{
return|return
name|oldName
return|;
block|}
comment|/**    * @param oldName    *          the oldName to set    */
specifier|public
name|void
name|setOldName
parameter_list|(
name|String
name|oldName
parameter_list|)
block|{
name|this
operator|.
name|oldName
operator|=
name|oldName
expr_stmt|;
block|}
comment|/**    * @return the newName    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"new name"
argument_list|)
specifier|public
name|String
name|getNewName
parameter_list|()
block|{
return|return
name|newName
return|;
block|}
comment|/**    * @param newName    *          the newName to set    */
specifier|public
name|void
name|setNewName
parameter_list|(
name|String
name|newName
parameter_list|)
block|{
name|this
operator|.
name|newName
operator|=
name|newName
expr_stmt|;
block|}
comment|/**    * @return the op    */
specifier|public
name|AlterTableTypes
name|getOp
parameter_list|()
block|{
return|return
name|op
return|;
block|}
comment|/**    * @param op    *          the op to set    */
specifier|public
name|void
name|setOp
parameter_list|(
name|AlterTableTypes
name|op
parameter_list|)
block|{
name|this
operator|.
name|op
operator|=
name|op
expr_stmt|;
block|}
comment|/**    * @return the newCols    */
specifier|public
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
name|getNewCols
parameter_list|()
block|{
return|return
name|newCols
return|;
block|}
comment|/**    * @param newCols    *          the newCols to set    */
specifier|public
name|void
name|setNewCols
parameter_list|(
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
name|newCols
parameter_list|)
block|{
name|this
operator|.
name|newCols
operator|=
name|newCols
expr_stmt|;
block|}
comment|/**    * @return the serdeName    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"deserializer library"
argument_list|)
specifier|public
name|String
name|getSerdeName
parameter_list|()
block|{
return|return
name|serdeName
return|;
block|}
comment|/**    * @param serdeName    *          the serdeName to set    */
specifier|public
name|void
name|setSerdeName
parameter_list|(
name|String
name|serdeName
parameter_list|)
block|{
name|this
operator|.
name|serdeName
operator|=
name|serdeName
expr_stmt|;
block|}
comment|/**    * @return the props    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"properties"
argument_list|)
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getProps
parameter_list|()
block|{
return|return
name|props
return|;
block|}
comment|/**    * @param props    *          the props to set    */
specifier|public
name|void
name|setProps
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
parameter_list|)
block|{
name|this
operator|.
name|props
operator|=
name|props
expr_stmt|;
block|}
comment|/**    * @return the input format    */
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
comment|/**    * @param inputFormat    *          the input format to set    */
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
comment|/**    * @return the output format    */
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
comment|/**    * @param outputFormat    *          the output format to set    */
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
comment|/**    * @return the storage handler    */
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
comment|/**    * @param storageHandler    *          the storage handler to set    */
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
comment|/**    * @return the number of buckets    */
specifier|public
name|int
name|getNumberBuckets
parameter_list|()
block|{
return|return
name|numberBuckets
return|;
block|}
comment|/**    * @param numberBuckets    *          the number of buckets to set    */
specifier|public
name|void
name|setNumberBuckets
parameter_list|(
name|int
name|numberBuckets
parameter_list|)
block|{
name|this
operator|.
name|numberBuckets
operator|=
name|numberBuckets
expr_stmt|;
block|}
comment|/**    * @return the bucket columns    */
specifier|public
name|ArrayList
argument_list|<
name|String
argument_list|>
name|getBucketColumns
parameter_list|()
block|{
return|return
name|bucketColumns
return|;
block|}
comment|/**    * @param bucketColumns    *          the bucket columns to set    */
specifier|public
name|void
name|setBucketColumns
parameter_list|(
name|ArrayList
argument_list|<
name|String
argument_list|>
name|bucketColumns
parameter_list|)
block|{
name|this
operator|.
name|bucketColumns
operator|=
name|bucketColumns
expr_stmt|;
block|}
comment|/**    * @return the sort columns    */
specifier|public
name|ArrayList
argument_list|<
name|Order
argument_list|>
name|getSortColumns
parameter_list|()
block|{
return|return
name|sortColumns
return|;
block|}
comment|/**    * @param sortColumns    *          the sort columns to set    */
specifier|public
name|void
name|setSortColumns
parameter_list|(
name|ArrayList
argument_list|<
name|Order
argument_list|>
name|sortColumns
parameter_list|)
block|{
name|this
operator|.
name|sortColumns
operator|=
name|sortColumns
expr_stmt|;
block|}
comment|/**    * @return old column name    */
specifier|public
name|String
name|getOldColName
parameter_list|()
block|{
return|return
name|oldColName
return|;
block|}
comment|/**    * @param oldColName    *          the old column name    */
specifier|public
name|void
name|setOldColName
parameter_list|(
name|String
name|oldColName
parameter_list|)
block|{
name|this
operator|.
name|oldColName
operator|=
name|oldColName
expr_stmt|;
block|}
comment|/**    * @return new column name    */
specifier|public
name|String
name|getNewColName
parameter_list|()
block|{
return|return
name|newColName
return|;
block|}
comment|/**    * @param newColName    *          the new column name    */
specifier|public
name|void
name|setNewColName
parameter_list|(
name|String
name|newColName
parameter_list|)
block|{
name|this
operator|.
name|newColName
operator|=
name|newColName
expr_stmt|;
block|}
comment|/**    * @return new column type    */
specifier|public
name|String
name|getNewColType
parameter_list|()
block|{
return|return
name|newColType
return|;
block|}
comment|/**    * @param newType    *          new column's type    */
specifier|public
name|void
name|setNewColType
parameter_list|(
name|String
name|newType
parameter_list|)
block|{
name|newColType
operator|=
name|newType
expr_stmt|;
block|}
comment|/**    * @return new column's comment    */
specifier|public
name|String
name|getNewColComment
parameter_list|()
block|{
return|return
name|newColComment
return|;
block|}
comment|/**    * @param newComment    *          new column's comment    */
specifier|public
name|void
name|setNewColComment
parameter_list|(
name|String
name|newComment
parameter_list|)
block|{
name|newColComment
operator|=
name|newComment
expr_stmt|;
block|}
comment|/**    * @return if the column should be changed to position 0    */
specifier|public
name|boolean
name|getFirst
parameter_list|()
block|{
return|return
name|first
return|;
block|}
comment|/**    * @param first    *          set the column to position 0    */
specifier|public
name|void
name|setFirst
parameter_list|(
name|boolean
name|first
parameter_list|)
block|{
name|this
operator|.
name|first
operator|=
name|first
expr_stmt|;
block|}
comment|/**    * @return the column's after position    */
specifier|public
name|String
name|getAfterCol
parameter_list|()
block|{
return|return
name|afterCol
return|;
block|}
comment|/**    * @param afterCol    *          set the column's after position    */
specifier|public
name|void
name|setAfterCol
parameter_list|(
name|String
name|afterCol
parameter_list|)
block|{
name|this
operator|.
name|afterCol
operator|=
name|afterCol
expr_stmt|;
block|}
comment|/**    * @return whether to expect a view being altered    */
specifier|public
name|boolean
name|getExpectView
parameter_list|()
block|{
return|return
name|expectView
return|;
block|}
comment|/**    * @param expectView    *          set whether to expect a view being altered    */
specifier|public
name|void
name|setExpectView
parameter_list|(
name|boolean
name|expectView
parameter_list|)
block|{
name|this
operator|.
name|expectView
operator|=
name|expectView
expr_stmt|;
block|}
comment|/**    * @return part specification    */
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartSpec
parameter_list|()
block|{
return|return
name|partSpec
return|;
block|}
comment|/**    * @param partSpec    */
specifier|public
name|void
name|setPartSpec
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
block|}
comment|/**    * @return new location    */
specifier|public
name|String
name|getNewLocation
parameter_list|()
block|{
return|return
name|newLocation
return|;
block|}
comment|/**    * @param newLocation new location    */
specifier|public
name|void
name|setNewLocation
parameter_list|(
name|String
name|newLocation
parameter_list|)
block|{
name|this
operator|.
name|newLocation
operator|=
name|newLocation
expr_stmt|;
block|}
specifier|public
name|boolean
name|isProtectModeEnable
parameter_list|()
block|{
return|return
name|protectModeEnable
return|;
block|}
specifier|public
name|void
name|setProtectModeEnable
parameter_list|(
name|boolean
name|protectModeEnable
parameter_list|)
block|{
name|this
operator|.
name|protectModeEnable
operator|=
name|protectModeEnable
expr_stmt|;
block|}
specifier|public
name|ProtectModeType
name|getProtectModeType
parameter_list|()
block|{
return|return
name|protectModeType
return|;
block|}
specifier|public
name|void
name|setProtectModeType
parameter_list|(
name|ProtectModeType
name|protectModeType
parameter_list|)
block|{
name|this
operator|.
name|protectModeType
operator|=
name|protectModeType
expr_stmt|;
block|}
block|}
end_class

end_unit

