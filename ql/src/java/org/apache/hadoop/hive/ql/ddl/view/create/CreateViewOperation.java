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
name|ddl
operator|.
name|view
operator|.
name|create
package|;
end_package

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
name|ValidTxnWriteIdList
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
name|CreationMetadata
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
name|ddl
operator|.
name|DDLOperation
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
name|DDLOperationContext
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
name|DDLUtils
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
name|hooks
operator|.
name|LineageInfo
operator|.
name|DataContainer
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

begin_comment
comment|/**  * Operation process of creating a view.  */
end_comment

begin_class
specifier|public
class|class
name|CreateViewOperation
extends|extends
name|DDLOperation
argument_list|<
name|CreateViewDesc
argument_list|>
block|{
specifier|public
name|CreateViewOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|CreateViewDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
name|Table
name|oldview
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getTable
argument_list|(
name|desc
operator|.
name|getViewName
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldview
operator|!=
literal|null
condition|)
block|{
comment|// Check whether we are replicating
if|if
condition|(
name|desc
operator|.
name|getReplicationSpec
argument_list|()
operator|.
name|isInReplicationScope
argument_list|()
condition|)
block|{
comment|// if this is a replication spec, then replace-mode semantics might apply.
if|if
condition|(
name|desc
operator|.
name|getReplicationSpec
argument_list|()
operator|.
name|allowEventReplacementInto
argument_list|(
name|oldview
operator|.
name|getParameters
argument_list|()
argument_list|)
condition|)
block|{
name|desc
operator|.
name|setReplace
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// we replace existing view.
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"DDLTask: Create View is skipped as view {} is newer than update"
argument_list|,
name|desc
operator|.
name|getViewName
argument_list|()
argument_list|)
expr_stmt|;
comment|// no replacement, the existing table state is newer than our update.
return|return
literal|0
return|;
block|}
block|}
if|if
condition|(
operator|!
name|desc
operator|.
name|isReplace
argument_list|()
condition|)
block|{
if|if
condition|(
name|desc
operator|.
name|getIfNotExists
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
block|}
comment|// View already exists, thus we should be replacing
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|TABLE_ALREADY_EXISTS
operator|.
name|getMsg
argument_list|(
name|desc
operator|.
name|getViewName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
comment|// It should not be a materialized view
assert|assert
operator|!
name|desc
operator|.
name|isMaterialized
argument_list|()
assert|;
comment|// replace existing view
comment|// remove the existing partition columns from the field schema
name|oldview
operator|.
name|setViewOriginalText
argument_list|(
name|desc
operator|.
name|getViewOriginalText
argument_list|()
argument_list|)
expr_stmt|;
name|oldview
operator|.
name|setViewExpandedText
argument_list|(
name|desc
operator|.
name|getViewExpandedText
argument_list|()
argument_list|)
expr_stmt|;
name|oldview
operator|.
name|setFields
argument_list|(
name|desc
operator|.
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|desc
operator|.
name|getComment
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|oldview
operator|.
name|setProperty
argument_list|(
literal|"comment"
argument_list|,
name|desc
operator|.
name|getComment
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|desc
operator|.
name|getTblProps
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|oldview
operator|.
name|getTTable
argument_list|()
operator|.
name|getParameters
argument_list|()
operator|.
name|putAll
argument_list|(
name|desc
operator|.
name|getTblProps
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|oldview
operator|.
name|setPartCols
argument_list|(
name|desc
operator|.
name|getPartCols
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|desc
operator|.
name|getInputFormat
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|oldview
operator|.
name|setInputFormatClass
argument_list|(
name|desc
operator|.
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|desc
operator|.
name|getOutputFormat
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|oldview
operator|.
name|setOutputFormatClass
argument_list|(
name|desc
operator|.
name|getOutputFormat
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|oldview
operator|.
name|checkValidity
argument_list|(
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|desc
operator|.
name|getOwnerName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|oldview
operator|.
name|setOwner
argument_list|(
name|desc
operator|.
name|getOwnerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|alterTable
argument_list|(
name|desc
operator|.
name|getViewName
argument_list|()
argument_list|,
name|oldview
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|DDLUtils
operator|.
name|addIfAbsentByName
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|oldview
argument_list|,
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DDL_NO_LOCK
argument_list|)
argument_list|,
name|context
operator|.
name|getWork
argument_list|()
operator|.
name|getOutputs
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// We create new view
name|Table
name|tbl
init|=
name|desc
operator|.
name|toTable
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
comment|// We set the signature for the view if it is a materialized view
if|if
condition|(
name|tbl
operator|.
name|isMaterializedView
argument_list|()
condition|)
block|{
name|CreationMetadata
name|cm
init|=
operator|new
name|CreationMetadata
argument_list|(
name|MetaStoreUtils
operator|.
name|getDefaultCatalog
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
argument_list|,
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|,
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|desc
operator|.
name|getTablesUsed
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|cm
operator|.
name|setValidTxnList
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|ValidTxnWriteIdList
operator|.
name|VALID_TABLES_WRITEIDS_KEY
argument_list|)
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|getTTable
argument_list|()
operator|.
name|setCreationMetadata
argument_list|(
name|cm
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|createTable
argument_list|(
name|tbl
argument_list|,
name|desc
operator|.
name|getIfNotExists
argument_list|()
argument_list|)
expr_stmt|;
name|DDLUtils
operator|.
name|addIfAbsentByName
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|tbl
argument_list|,
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DDL_NO_LOCK
argument_list|)
argument_list|,
name|context
operator|.
name|getWork
argument_list|()
operator|.
name|getOutputs
argument_list|()
argument_list|)
expr_stmt|;
comment|//set lineage info
name|DataContainer
name|dc
init|=
operator|new
name|DataContainer
argument_list|(
name|tbl
operator|.
name|getTTable
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|getQueryState
argument_list|()
operator|.
name|getLineageState
argument_list|()
operator|.
name|setLineage
argument_list|(
operator|new
name|Path
argument_list|(
name|desc
operator|.
name|getViewName
argument_list|()
argument_list|)
argument_list|,
name|dc
argument_list|,
name|tbl
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit

