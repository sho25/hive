begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HashSet
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
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
name|metastore
operator|.
name|TableType
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
name|ddl
operator|.
name|DDLWork2
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
name|ddl
operator|.
name|table
operator|.
name|CreateTableDesc
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
name|ddl
operator|.
name|table
operator|.
name|CreateViewDesc
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
name|Task
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
name|TaskFactory
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|HiveException
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
name|Table
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
name|ReplicationSpec
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

begin_comment
comment|/**  * ImportTableDesc.  *  */
end_comment

begin_class
specifier|public
class|class
name|ImportTableDesc
block|{
specifier|private
name|String
name|dbName
init|=
literal|null
decl_stmt|;
specifier|private
name|Table
name|table
init|=
literal|null
decl_stmt|;
specifier|private
name|CreateTableDesc
name|createTblDesc
init|=
literal|null
decl_stmt|;
specifier|private
name|CreateViewDesc
name|createViewDesc
init|=
literal|null
decl_stmt|;
specifier|public
enum|enum
name|TYPE
block|{
name|TABLE
block|,
name|VIEW
block|}
empty_stmt|;
specifier|public
name|ImportTableDesc
parameter_list|(
name|String
name|dbName
parameter_list|,
name|Table
name|table
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
name|this
operator|.
name|createTblDesc
operator|=
operator|new
name|CreateTableDesc
argument_list|(
name|dbName
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|false
argument_list|,
comment|// isExternal: set to false here, can be overwritten by the IMPORT stmt
literal|false
argument_list|,
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
argument_list|,
name|table
operator|.
name|getPartitionKeys
argument_list|()
argument_list|,
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getBucketCols
argument_list|()
argument_list|,
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getSortCols
argument_list|()
argument_list|,
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getNumBuckets
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
comment|// these 5 delims passed as serde params
literal|null
argument_list|,
comment|// comment passed as table params
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getInputFormat
argument_list|()
argument_list|,
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getOutputFormat
argument_list|()
argument_list|,
literal|null
argument_list|,
comment|// location: set to null here, can be overwritten by the IMPORT stmt
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getSerializationLib
argument_list|()
argument_list|,
literal|null
argument_list|,
comment|// storagehandler passed as table params
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getParameters
argument_list|()
argument_list|,
name|table
operator|.
name|getParameters
argument_list|()
argument_list|,
literal|false
argument_list|,
operator|(
literal|null
operator|==
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getSkewedInfo
argument_list|()
operator|)
condition|?
literal|null
else|:
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getSkewedInfo
argument_list|()
operator|.
name|getSkewedColNames
argument_list|()
argument_list|,
operator|(
literal|null
operator|==
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getSkewedInfo
argument_list|()
operator|)
condition|?
literal|null
else|:
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getSkewedInfo
argument_list|()
operator|.
name|getSkewedColValues
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|table
operator|.
name|getColStats
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|createTblDesc
operator|.
name|setStoredAsSubDirectories
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|isStoredAsSubDirectories
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|VIEW
case|:
name|String
index|[]
name|qualViewName
init|=
block|{
name|dbName
block|,
name|table
operator|.
name|getTableName
argument_list|()
block|}
decl_stmt|;
name|String
name|dbDotView
init|=
name|BaseSemanticAnalyzer
operator|.
name|getDotName
argument_list|(
name|qualViewName
argument_list|)
decl_stmt|;
if|if
condition|(
name|table
operator|.
name|isMaterializedView
argument_list|()
condition|)
block|{
name|this
operator|.
name|createViewDesc
operator|=
operator|new
name|CreateViewDesc
argument_list|(
name|dbDotView
argument_list|,
name|table
operator|.
name|getAllCols
argument_list|()
argument_list|,
literal|null
argument_list|,
comment|// comment passed as table params
name|table
operator|.
name|getParameters
argument_list|()
argument_list|,
name|table
operator|.
name|getPartColNames
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getInputFormat
argument_list|()
argument_list|,
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getOutputFormat
argument_list|()
argument_list|,
literal|null
argument_list|,
comment|// location: set to null here, can be overwritten by the IMPORT stmt
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getSerializationLib
argument_list|()
argument_list|,
literal|null
argument_list|,
comment|// storagehandler passed as table params
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getParameters
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO: If the DB name from the creation metadata for any of the tables has changed,
comment|// we should update it. Currently it refers to the source database name.
name|this
operator|.
name|createViewDesc
operator|.
name|setTablesUsed
argument_list|(
name|table
operator|.
name|getCreationMetadata
argument_list|()
operator|!=
literal|null
condition|?
name|table
operator|.
name|getCreationMetadata
argument_list|()
operator|.
name|getTablesUsed
argument_list|()
else|:
name|ImmutableSet
operator|.
name|of
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|createViewDesc
operator|=
operator|new
name|CreateViewDesc
argument_list|(
name|dbDotView
argument_list|,
name|table
operator|.
name|getAllCols
argument_list|()
argument_list|,
literal|null
argument_list|,
comment|// comment passed as table params
name|table
operator|.
name|getParameters
argument_list|()
argument_list|,
name|table
operator|.
name|getPartColNames
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getInputFormat
argument_list|()
argument_list|,
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getOutputFormat
argument_list|()
argument_list|,
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getSerializationLib
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|setViewAsReferenceText
argument_list|(
name|dbName
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|this
operator|.
name|createViewDesc
operator|.
name|setPartCols
argument_list|(
name|table
operator|.
name|getPartCols
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Invalid table type"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|TYPE
name|getDescType
parameter_list|()
block|{
if|if
condition|(
name|table
operator|.
name|isView
argument_list|()
operator|||
name|table
operator|.
name|isMaterializedView
argument_list|()
condition|)
block|{
return|return
name|TYPE
operator|.
name|VIEW
return|;
block|}
return|return
name|TYPE
operator|.
name|TABLE
return|;
block|}
specifier|public
name|void
name|setViewAsReferenceText
parameter_list|(
name|String
name|dbName
parameter_list|,
name|Table
name|table
parameter_list|)
block|{
name|String
name|originalText
init|=
name|table
operator|.
name|getViewOriginalText
argument_list|()
decl_stmt|;
name|String
name|expandedText
init|=
name|table
operator|.
name|getViewExpandedText
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|dbName
operator|.
name|equals
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|)
condition|)
block|{
comment|// TODO: If the DB name doesn't match with the metadata from dump, then need to rewrite the original and expanded
comment|// texts using new DB name. Currently it refers to the source database name.
block|}
name|this
operator|.
name|createViewDesc
operator|.
name|setViewOriginalText
argument_list|(
name|originalText
argument_list|)
expr_stmt|;
name|this
operator|.
name|createViewDesc
operator|.
name|setViewExpandedText
argument_list|(
name|expandedText
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setReplicationSpec
parameter_list|(
name|ReplicationSpec
name|replSpec
parameter_list|)
block|{
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
name|createTblDesc
operator|.
name|setReplicationSpec
argument_list|(
name|replSpec
argument_list|)
expr_stmt|;
break|break;
case|case
name|VIEW
case|:
name|createViewDesc
operator|.
name|setReplicationSpec
argument_list|(
name|replSpec
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
specifier|public
name|void
name|setExternal
parameter_list|(
name|boolean
name|isExternal
parameter_list|)
block|{
if|if
condition|(
name|TYPE
operator|.
name|TABLE
operator|.
name|equals
argument_list|(
name|getDescType
argument_list|()
argument_list|)
condition|)
block|{
name|createTblDesc
operator|.
name|setExternal
argument_list|(
name|isExternal
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|isExternal
parameter_list|()
block|{
if|if
condition|(
name|TYPE
operator|.
name|TABLE
operator|.
name|equals
argument_list|(
name|getDescType
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|createTblDesc
operator|.
name|isExternal
argument_list|()
return|;
block|}
return|return
literal|false
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
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
name|createTblDesc
operator|.
name|setLocation
argument_list|(
name|location
argument_list|)
expr_stmt|;
break|break;
case|case
name|VIEW
case|:
name|createViewDesc
operator|.
name|setLocation
argument_list|(
name|location
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
return|return
name|createTblDesc
operator|.
name|getLocation
argument_list|()
return|;
case|case
name|VIEW
case|:
return|return
name|createViewDesc
operator|.
name|getLocation
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|void
name|setTableName
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|SemanticException
block|{
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
name|createTblDesc
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
break|break;
case|case
name|VIEW
case|:
name|String
index|[]
name|qualViewName
init|=
block|{
name|dbName
block|,
name|tableName
block|}
decl_stmt|;
name|String
name|dbDotView
init|=
name|BaseSemanticAnalyzer
operator|.
name|getDotName
argument_list|(
name|qualViewName
argument_list|)
decl_stmt|;
name|createViewDesc
operator|.
name|setViewName
argument_list|(
name|dbDotView
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
specifier|public
name|String
name|getTableName
parameter_list|()
throws|throws
name|SemanticException
block|{
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
return|return
name|createTblDesc
operator|.
name|getTableName
argument_list|()
return|;
case|case
name|VIEW
case|:
name|String
name|dbDotView
init|=
name|createViewDesc
operator|.
name|getViewName
argument_list|()
decl_stmt|;
name|String
index|[]
name|names
init|=
name|Utilities
operator|.
name|getDbTableName
argument_list|(
name|dbDotView
argument_list|)
decl_stmt|;
return|return
name|names
index|[
literal|1
index|]
return|;
comment|// names[0] have the Db name and names[1] have the view name
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getPartCols
parameter_list|()
block|{
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
return|return
name|createTblDesc
operator|.
name|getPartCols
argument_list|()
return|;
case|case
name|VIEW
case|:
return|return
name|createViewDesc
operator|.
name|getPartCols
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getCols
parameter_list|()
block|{
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
return|return
name|createTblDesc
operator|.
name|getCols
argument_list|()
return|;
case|case
name|VIEW
case|:
return|return
name|createViewDesc
operator|.
name|getSchema
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
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
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
return|return
name|createTblDesc
operator|.
name|getTblProps
argument_list|()
return|;
case|case
name|VIEW
case|:
return|return
name|createViewDesc
operator|.
name|getTblProps
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|String
name|getInputFormat
parameter_list|()
block|{
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
return|return
name|createTblDesc
operator|.
name|getInputFormat
argument_list|()
return|;
case|case
name|VIEW
case|:
return|return
name|createViewDesc
operator|.
name|getInputFormat
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|String
name|getOutputFormat
parameter_list|()
block|{
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
return|return
name|createTblDesc
operator|.
name|getOutputFormat
argument_list|()
return|;
case|case
name|VIEW
case|:
return|return
name|createViewDesc
operator|.
name|getOutputFormat
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|String
name|getSerName
parameter_list|()
block|{
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
return|return
name|createTblDesc
operator|.
name|getSerName
argument_list|()
return|;
case|case
name|VIEW
case|:
return|return
name|createViewDesc
operator|.
name|getSerde
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
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
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
return|return
name|createTblDesc
operator|.
name|getSerdeProps
argument_list|()
return|;
case|case
name|VIEW
case|:
return|return
name|createViewDesc
operator|.
name|getSerdeProps
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getBucketCols
parameter_list|()
block|{
if|if
condition|(
name|TYPE
operator|.
name|TABLE
operator|.
name|equals
argument_list|(
name|getDescType
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|createTblDesc
operator|.
name|getBucketCols
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|List
argument_list|<
name|Order
argument_list|>
name|getSortCols
parameter_list|()
block|{
if|if
condition|(
name|TYPE
operator|.
name|TABLE
operator|.
name|equals
argument_list|(
name|getDescType
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|createTblDesc
operator|.
name|getSortCols
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * @param replaceMode Determine if this CreateTable should behave like a replace-into alter instead    */
specifier|public
name|void
name|setReplaceMode
parameter_list|(
name|boolean
name|replaceMode
parameter_list|)
block|{
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
name|createTblDesc
operator|.
name|setReplaceMode
argument_list|(
name|replaceMode
argument_list|)
expr_stmt|;
break|break;
case|case
name|VIEW
case|:
name|createViewDesc
operator|.
name|setReplace
argument_list|(
name|replaceMode
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|String
name|getDatabaseName
parameter_list|()
block|{
return|return
name|dbName
return|;
block|}
specifier|public
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|getCreateTableTask
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
return|return
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork2
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|,
name|createTblDesc
argument_list|)
argument_list|,
name|conf
argument_list|)
return|;
case|case
name|VIEW
case|:
return|return
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork2
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|,
name|createViewDesc
argument_list|)
argument_list|,
name|conf
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * @return whether this table is actually a view    */
specifier|public
name|boolean
name|isView
parameter_list|()
block|{
return|return
name|table
operator|.
name|isView
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isMaterializedView
parameter_list|()
block|{
return|return
name|table
operator|.
name|isMaterializedView
argument_list|()
return|;
block|}
specifier|public
name|TableType
name|tableType
parameter_list|()
block|{
if|if
condition|(
name|isView
argument_list|()
condition|)
block|{
return|return
name|TableType
operator|.
name|VIRTUAL_VIEW
return|;
block|}
elseif|else
if|if
condition|(
name|isMaterializedView
argument_list|()
condition|)
block|{
return|return
name|TableType
operator|.
name|MATERIALIZED_VIEW
return|;
block|}
return|return
name|TableType
operator|.
name|MANAGED_TABLE
return|;
block|}
specifier|public
name|Table
name|toTable
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
return|return
name|createTblDesc
operator|.
name|toTable
argument_list|(
name|conf
argument_list|)
return|;
case|case
name|VIEW
case|:
return|return
name|createViewDesc
operator|.
name|toTable
argument_list|(
name|conf
argument_list|)
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
specifier|public
name|void
name|setReplWriteId
parameter_list|(
name|Long
name|replWriteId
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|createTblDesc
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|createTblDesc
operator|.
name|setReplWriteId
argument_list|(
name|replWriteId
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|setOwnerName
parameter_list|(
name|String
name|ownerName
parameter_list|)
block|{
switch|switch
condition|(
name|getDescType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
name|createTblDesc
operator|.
name|setOwnerName
argument_list|(
name|ownerName
argument_list|)
expr_stmt|;
break|break;
case|case
name|VIEW
case|:
name|createViewDesc
operator|.
name|setOwnerName
argument_list|(
name|ownerName
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid table type : "
operator|+
name|getDescType
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

