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
name|java
operator|.
name|util
operator|.
name|Set
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
name|fs
operator|.
name|Path
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
name|StatsSetupConst
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
name|utils
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
name|ql
operator|.
name|exec
operator|.
name|DDLTask
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
name|HiveStorageHandler
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
name|plan
operator|.
name|Explain
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * CreateViewDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Create View"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|CreateViewDesc
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
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CreateViewDesc
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|String
name|viewName
decl_stmt|;
specifier|private
name|String
name|originalText
decl_stmt|;
specifier|private
name|String
name|expandedText
decl_stmt|;
specifier|private
name|boolean
name|rewriteEnabled
decl_stmt|;
specifier|private
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schema
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|partColNames
decl_stmt|;
specifier|private
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
decl_stmt|;
specifier|private
name|String
name|comment
decl_stmt|;
specifier|private
name|boolean
name|ifNotExists
decl_stmt|;
specifier|private
name|boolean
name|replace
decl_stmt|;
specifier|private
name|boolean
name|isAlterViewAs
decl_stmt|;
specifier|private
name|boolean
name|isMaterialized
decl_stmt|;
specifier|private
name|String
name|inputFormat
decl_stmt|;
specifier|private
name|String
name|outputFormat
decl_stmt|;
specifier|private
name|String
name|location
decl_stmt|;
comment|// only used for materialized views
specifier|private
name|String
name|serde
decl_stmt|;
comment|// only used for materialized views
specifier|private
name|String
name|storageHandler
decl_stmt|;
comment|// only used for materialized views
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serdeProps
decl_stmt|;
comment|// only used for materialized views
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|tablesUsed
decl_stmt|;
comment|// only used for materialized views
specifier|private
name|ReplicationSpec
name|replicationSpec
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|ownerName
init|=
literal|null
decl_stmt|;
comment|/**    * For serialization only.    */
specifier|public
name|CreateViewDesc
parameter_list|()
block|{   }
comment|/**    * Used to create a materialized view descriptor    * @param viewName    * @param schema    * @param comment    * @param tblProps    * @param partColNames    * @param ifNotExists    * @param orReplace    * @param isAlterViewAs    * @param inputFormat    * @param outputFormat    * @param location    * @param serName    * @param serde    * @param storageHandler    * @param serdeProps    */
specifier|public
name|CreateViewDesc
parameter_list|(
name|String
name|viewName
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schema
parameter_list|,
name|String
name|comment
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partColNames
parameter_list|,
name|boolean
name|ifNotExists
parameter_list|,
name|boolean
name|replace
parameter_list|,
name|boolean
name|rewriteEnabled
parameter_list|,
name|boolean
name|isAlterViewAs
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
name|serde
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
parameter_list|)
block|{
name|this
operator|.
name|viewName
operator|=
name|viewName
expr_stmt|;
name|this
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
name|this
operator|.
name|tblProps
operator|=
name|tblProps
expr_stmt|;
name|this
operator|.
name|partColNames
operator|=
name|partColNames
expr_stmt|;
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
name|this
operator|.
name|ifNotExists
operator|=
name|ifNotExists
expr_stmt|;
name|this
operator|.
name|replace
operator|=
name|replace
expr_stmt|;
name|this
operator|.
name|isMaterialized
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|rewriteEnabled
operator|=
name|rewriteEnabled
expr_stmt|;
name|this
operator|.
name|isAlterViewAs
operator|=
name|isAlterViewAs
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
name|location
operator|=
name|location
expr_stmt|;
name|this
operator|.
name|serde
operator|=
name|serde
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
block|}
comment|/**    * Used to create a view descriptor    * @param viewName    * @param schema    * @param comment    * @param tblProps    * @param partColNames    * @param ifNotExists    * @param orReplace    * @param isAlterViewAs    * @param inputFormat    * @param outputFormat    * @param serde    */
specifier|public
name|CreateViewDesc
parameter_list|(
name|String
name|viewName
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schema
parameter_list|,
name|String
name|comment
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partColNames
parameter_list|,
name|boolean
name|ifNotExists
parameter_list|,
name|boolean
name|orReplace
parameter_list|,
name|boolean
name|isAlterViewAs
parameter_list|,
name|String
name|inputFormat
parameter_list|,
name|String
name|outputFormat
parameter_list|,
name|String
name|serde
parameter_list|)
block|{
name|this
operator|.
name|viewName
operator|=
name|viewName
expr_stmt|;
name|this
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
name|this
operator|.
name|tblProps
operator|=
name|tblProps
expr_stmt|;
name|this
operator|.
name|partColNames
operator|=
name|partColNames
expr_stmt|;
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
name|this
operator|.
name|ifNotExists
operator|=
name|ifNotExists
expr_stmt|;
name|this
operator|.
name|replace
operator|=
name|orReplace
expr_stmt|;
name|this
operator|.
name|isAlterViewAs
operator|=
name|isAlterViewAs
expr_stmt|;
name|this
operator|.
name|isMaterialized
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|rewriteEnabled
operator|=
literal|false
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
name|serde
operator|=
name|serde
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"name"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getViewName
parameter_list|()
block|{
return|return
name|viewName
return|;
block|}
specifier|public
name|void
name|setViewName
parameter_list|(
name|String
name|viewName
parameter_list|)
block|{
name|this
operator|.
name|viewName
operator|=
name|viewName
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"original text"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getViewOriginalText
parameter_list|()
block|{
return|return
name|originalText
return|;
block|}
specifier|public
name|void
name|setViewOriginalText
parameter_list|(
name|String
name|originalText
parameter_list|)
block|{
name|this
operator|.
name|originalText
operator|=
name|originalText
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"expanded text"
argument_list|)
specifier|public
name|String
name|getViewExpandedText
parameter_list|()
block|{
return|return
name|expandedText
return|;
block|}
specifier|public
name|void
name|setViewExpandedText
parameter_list|(
name|String
name|expandedText
parameter_list|)
block|{
name|this
operator|.
name|expandedText
operator|=
name|expandedText
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"rewrite enabled"
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
argument_list|)
specifier|public
name|boolean
name|isRewriteEnabled
parameter_list|()
block|{
return|return
name|rewriteEnabled
return|;
block|}
specifier|public
name|void
name|setRewriteEnabled
parameter_list|(
name|boolean
name|rewriteEnabled
parameter_list|)
block|{
name|this
operator|.
name|rewriteEnabled
operator|=
name|rewriteEnabled
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
name|getSchemaString
parameter_list|()
block|{
return|return
name|Utilities
operator|.
name|getFieldSchemaString
argument_list|(
name|schema
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getSchema
parameter_list|()
block|{
return|return
name|schema
return|;
block|}
specifier|public
name|void
name|setSchema
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schema
parameter_list|)
block|{
name|this
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
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
name|partCols
argument_list|)
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
return|return
name|partCols
return|;
block|}
specifier|public
name|void
name|setPartCols
parameter_list|(
name|List
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
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getPartColNames
parameter_list|()
block|{
return|return
name|partColNames
return|;
block|}
specifier|public
name|void
name|setPartColNames
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partColNames
parameter_list|)
block|{
name|this
operator|.
name|partColNames
operator|=
name|partColNames
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
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"if not exists"
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
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
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getTablesUsed
parameter_list|()
block|{
return|return
name|tablesUsed
return|;
block|}
specifier|public
name|void
name|setTablesUsed
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|tablesUsed
parameter_list|)
block|{
name|this
operator|.
name|tablesUsed
operator|=
name|tablesUsed
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"replace"
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
argument_list|)
specifier|public
name|boolean
name|isReplace
parameter_list|()
block|{
return|return
name|replace
return|;
block|}
specifier|public
name|void
name|setReplace
parameter_list|(
name|boolean
name|replace
parameter_list|)
block|{
name|this
operator|.
name|replace
operator|=
name|replace
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"is alter view as select"
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
argument_list|)
specifier|public
name|boolean
name|getIsAlterViewAs
parameter_list|()
block|{
return|return
name|isAlterViewAs
return|;
block|}
specifier|public
name|void
name|setIsAlterViewAs
parameter_list|(
name|boolean
name|isAlterViewAs
parameter_list|)
block|{
name|this
operator|.
name|isAlterViewAs
operator|=
name|isAlterViewAs
expr_stmt|;
block|}
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
specifier|public
name|boolean
name|isMaterialized
parameter_list|()
block|{
return|return
name|isMaterialized
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
name|String
name|getSerde
parameter_list|()
block|{
return|return
name|serde
return|;
block|}
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
comment|/**    * @param replicationSpec Sets the replication spec governing this create.    * This parameter will have meaningful values only for creates happening as a result of a replication.    */
specifier|public
name|void
name|setReplicationSpec
parameter_list|(
name|ReplicationSpec
name|replicationSpec
parameter_list|)
block|{
name|this
operator|.
name|replicationSpec
operator|=
name|replicationSpec
expr_stmt|;
block|}
comment|/**    * @return what kind of replication spec this create is running under.    */
specifier|public
name|ReplicationSpec
name|getReplicationSpec
parameter_list|()
block|{
if|if
condition|(
name|replicationSpec
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|replicationSpec
operator|=
operator|new
name|ReplicationSpec
argument_list|()
expr_stmt|;
block|}
return|return
name|this
operator|.
name|replicationSpec
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
name|HiveException
block|{
name|String
index|[]
name|names
init|=
name|Utilities
operator|.
name|getDbTableName
argument_list|(
name|getViewName
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|databaseName
init|=
name|names
index|[
literal|0
index|]
decl_stmt|;
name|String
name|tableName
init|=
name|names
index|[
literal|1
index|]
decl_stmt|;
name|Table
name|tbl
init|=
operator|new
name|Table
argument_list|(
name|databaseName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|tbl
operator|.
name|setViewOriginalText
argument_list|(
name|getViewOriginalText
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setViewExpandedText
argument_list|(
name|getViewExpandedText
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|isMaterialized
argument_list|()
condition|)
block|{
name|tbl
operator|.
name|setRewriteEnabled
argument_list|(
name|isRewriteEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setTableType
argument_list|(
name|TableType
operator|.
name|MATERIALIZED_VIEW
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tbl
operator|.
name|setTableType
argument_list|(
name|TableType
operator|.
name|VIRTUAL_VIEW
argument_list|)
expr_stmt|;
block|}
name|tbl
operator|.
name|setSerializationLib
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|clearSerDeInfo
argument_list|()
expr_stmt|;
name|tbl
operator|.
name|setFields
argument_list|(
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|getComment
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tbl
operator|.
name|setProperty
argument_list|(
literal|"comment"
argument_list|,
name|getComment
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getTblProps
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tbl
operator|.
name|getTTable
argument_list|()
operator|.
name|getParameters
argument_list|()
operator|.
name|putAll
argument_list|(
name|getTblProps
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getPartCols
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tbl
operator|.
name|setPartCols
argument_list|(
name|getPartCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getInputFormat
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tbl
operator|.
name|setInputFormatClass
argument_list|(
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getOutputFormat
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tbl
operator|.
name|setOutputFormatClass
argument_list|(
name|getOutputFormat
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isMaterialized
argument_list|()
condition|)
block|{
if|if
condition|(
name|getLocation
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tbl
operator|.
name|setDataLocation
argument_list|(
operator|new
name|Path
argument_list|(
name|getLocation
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getStorageHandler
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tbl
operator|.
name|setProperty
argument_list|(
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
name|hive_metastoreConstants
operator|.
name|META_TABLE_STORAGE
argument_list|,
name|getStorageHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|HiveStorageHandler
name|storageHandler
init|=
name|tbl
operator|.
name|getStorageHandler
argument_list|()
decl_stmt|;
comment|/*        * If the user didn't specify a SerDe, we use the default.        */
name|String
name|serDeClassName
decl_stmt|;
if|if
condition|(
name|getSerde
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|storageHandler
operator|==
literal|null
condition|)
block|{
name|serDeClassName
operator|=
name|PlanUtils
operator|.
name|getDefaultSerDe
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Default to {} for materialized view {}"
argument_list|,
name|serDeClassName
argument_list|,
name|getViewName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serDeClassName
operator|=
name|storageHandler
operator|.
name|getSerDeClass
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Use StorageHandler-supplied {} for materialized view {}"
argument_list|,
name|serDeClassName
argument_list|,
name|getViewName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// let's validate that the serde exists
name|serDeClassName
operator|=
name|getSerde
argument_list|()
expr_stmt|;
name|DDLTask
operator|.
name|validateSerDe
argument_list|(
name|serDeClassName
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
name|tbl
operator|.
name|setSerializationLib
argument_list|(
name|serDeClassName
argument_list|)
expr_stmt|;
comment|// To remain consistent, we need to set input and output formats both
comment|// at the table level and the storage handler level.
name|tbl
operator|.
name|setInputFormatClass
argument_list|(
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setOutputFormatClass
argument_list|(
name|getOutputFormat
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|getInputFormat
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|getInputFormat
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|setInputFormat
argument_list|(
name|tbl
operator|.
name|getInputFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getOutputFormat
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|getOutputFormat
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|setOutputFormat
argument_list|(
name|tbl
operator|.
name|getOutputFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|ownerName
operator|!=
literal|null
condition|)
block|{
name|tbl
operator|.
name|setOwner
argument_list|(
name|ownerName
argument_list|)
expr_stmt|;
block|}
comment|// Sets the column state for the create view statement (false since it is a creation).
comment|// Similar to logic in CreateTableDesc.
name|StatsSetupConst
operator|.
name|setStatsStateForCreateTable
argument_list|(
name|tbl
operator|.
name|getTTable
argument_list|()
operator|.
name|getParameters
argument_list|()
argument_list|,
literal|null
argument_list|,
name|StatsSetupConst
operator|.
name|FALSE
argument_list|)
expr_stmt|;
return|return
name|tbl
return|;
block|}
specifier|public
name|void
name|setOwnerName
parameter_list|(
name|String
name|ownerName
parameter_list|)
block|{
name|this
operator|.
name|ownerName
operator|=
name|ownerName
expr_stmt|;
block|}
specifier|public
name|String
name|getOwnerName
parameter_list|()
block|{
return|return
name|this
operator|.
name|ownerName
return|;
block|}
block|}
end_class

end_unit

