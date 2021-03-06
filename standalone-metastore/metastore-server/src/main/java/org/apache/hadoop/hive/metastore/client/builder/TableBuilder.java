begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
operator|.
name|client
operator|.
name|builder
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
name|common
operator|.
name|ValidTxnList
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
name|IMetaStoreClient
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
name|Warehouse
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
name|api
operator|.
name|Database
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
name|MetaException
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
name|PrincipalType
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
name|metastore
operator|.
name|utils
operator|.
name|SecurityUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * Build a {@link Table}.  The database name and table name must be provided, plus whatever is  * needed by the underlying {@link StorageDescriptorBuilder}.  */
end_comment

begin_class
specifier|public
class|class
name|TableBuilder
extends|extends
name|StorageDescriptorBuilder
argument_list|<
name|TableBuilder
argument_list|>
block|{
specifier|private
name|String
name|catName
decl_stmt|,
name|dbName
decl_stmt|,
name|tableName
decl_stmt|,
name|owner
decl_stmt|,
name|viewOriginalText
decl_stmt|,
name|viewExpandedText
decl_stmt|,
name|type
decl_stmt|,
name|mvValidTxnList
decl_stmt|;
specifier|private
name|CreationMetadata
name|cm
decl_stmt|;
specifier|private
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
decl_stmt|;
specifier|private
name|int
name|createTime
decl_stmt|,
name|lastAccessTime
decl_stmt|,
name|retention
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableParams
decl_stmt|;
specifier|private
name|boolean
name|rewriteEnabled
decl_stmt|,
name|temporary
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|mvReferencedTables
decl_stmt|;
specifier|private
name|PrincipalType
name|ownerType
decl_stmt|;
specifier|public
name|TableBuilder
parameter_list|()
block|{
comment|// Set some reasonable defaults
name|dbName
operator|=
name|Warehouse
operator|.
name|DEFAULT_DATABASE_NAME
expr_stmt|;
name|tableParams
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|createTime
operator|=
name|lastAccessTime
operator|=
call|(
name|int
call|)
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
argument_list|)
expr_stmt|;
name|retention
operator|=
literal|0
expr_stmt|;
name|partCols
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|type
operator|=
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|name
argument_list|()
expr_stmt|;
name|mvReferencedTables
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|temporary
operator|=
literal|false
expr_stmt|;
name|super
operator|.
name|setChild
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TableBuilder
name|setCatName
parameter_list|(
name|String
name|catName
parameter_list|)
block|{
name|this
operator|.
name|catName
operator|=
name|catName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|setDbName
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|inDb
parameter_list|(
name|Database
name|db
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|db
operator|.
name|getName
argument_list|()
expr_stmt|;
name|this
operator|.
name|catName
operator|=
name|db
operator|.
name|getCatalogName
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
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
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|setOwner
parameter_list|(
name|String
name|owner
parameter_list|)
block|{
name|this
operator|.
name|owner
operator|=
name|owner
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|setOwnerType
parameter_list|(
name|PrincipalType
name|ownerType
parameter_list|)
block|{
name|this
operator|.
name|ownerType
operator|=
name|ownerType
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|setViewOriginalText
parameter_list|(
name|String
name|viewOriginalText
parameter_list|)
block|{
name|this
operator|.
name|viewOriginalText
operator|=
name|viewOriginalText
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|setViewExpandedText
parameter_list|(
name|String
name|viewExpandedText
parameter_list|)
block|{
name|this
operator|.
name|viewExpandedText
operator|=
name|viewExpandedText
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|setType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|setCreationMetadata
parameter_list|(
name|CreationMetadata
name|cm
parameter_list|)
block|{
name|this
operator|.
name|cm
operator|=
name|cm
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
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
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|addPartCol
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|comment
parameter_list|)
block|{
name|partCols
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
name|name
argument_list|,
name|type
argument_list|,
name|comment
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|addPartCol
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|type
parameter_list|)
block|{
return|return
name|addPartCol
argument_list|(
name|name
argument_list|,
name|type
argument_list|,
literal|""
argument_list|)
return|;
block|}
specifier|public
name|TableBuilder
name|setCreateTime
parameter_list|(
name|int
name|createTime
parameter_list|)
block|{
name|this
operator|.
name|createTime
operator|=
name|createTime
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|setLastAccessTime
parameter_list|(
name|int
name|lastAccessTime
parameter_list|)
block|{
name|this
operator|.
name|lastAccessTime
operator|=
name|lastAccessTime
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|setRetention
parameter_list|(
name|int
name|retention
parameter_list|)
block|{
name|this
operator|.
name|retention
operator|=
name|retention
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|setTableParams
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableParams
parameter_list|)
block|{
name|this
operator|.
name|tableParams
operator|=
name|tableParams
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|addTableParam
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|tableParams
operator|==
literal|null
condition|)
block|{
name|tableParams
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|tableParams
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
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
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|setTemporary
parameter_list|(
name|boolean
name|temporary
parameter_list|)
block|{
name|this
operator|.
name|temporary
operator|=
name|temporary
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|addMaterializedViewReferencedTable
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|mvReferencedTables
operator|.
name|add
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|addMaterializedViewReferencedTables
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|tableNames
parameter_list|)
block|{
name|mvReferencedTables
operator|.
name|addAll
argument_list|(
name|tableNames
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|setMaterializedViewValidTxnList
parameter_list|(
name|ValidTxnList
name|validTxnList
parameter_list|)
block|{
name|mvValidTxnList
operator|=
name|validTxnList
operator|.
name|writeToString
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Table
name|build
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|tableName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"You must set the table name"
argument_list|)
throw|;
block|}
if|if
condition|(
name|ownerType
operator|==
literal|null
condition|)
block|{
name|ownerType
operator|=
name|PrincipalType
operator|.
name|USER
expr_stmt|;
block|}
if|if
condition|(
name|owner
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|owner
operator|=
name|SecurityUtils
operator|.
name|getUser
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|MetaStoreUtils
operator|.
name|newMetaException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|catName
operator|==
literal|null
condition|)
name|catName
operator|=
name|MetaStoreUtils
operator|.
name|getDefaultCatalog
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Table
name|t
init|=
operator|new
name|Table
argument_list|(
name|tableName
argument_list|,
name|dbName
argument_list|,
name|owner
argument_list|,
name|createTime
argument_list|,
name|lastAccessTime
argument_list|,
name|retention
argument_list|,
name|buildSd
argument_list|()
argument_list|,
name|partCols
argument_list|,
name|tableParams
argument_list|,
name|viewOriginalText
argument_list|,
name|viewExpandedText
argument_list|,
name|type
argument_list|)
decl_stmt|;
if|if
condition|(
name|rewriteEnabled
condition|)
name|t
operator|.
name|setRewriteEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|temporary
condition|)
name|t
operator|.
name|setTemporary
argument_list|(
name|temporary
argument_list|)
expr_stmt|;
name|t
operator|.
name|setCatName
argument_list|(
name|catName
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|mvReferencedTables
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|CreationMetadata
name|cm
init|=
operator|new
name|CreationMetadata
argument_list|(
name|catName
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|mvReferencedTables
argument_list|)
decl_stmt|;
if|if
condition|(
name|mvValidTxnList
operator|!=
literal|null
condition|)
name|cm
operator|.
name|setValidTxnList
argument_list|(
name|mvValidTxnList
argument_list|)
expr_stmt|;
name|t
operator|.
name|setCreationMetadata
argument_list|(
name|cm
argument_list|)
expr_stmt|;
block|}
return|return
name|t
return|;
block|}
specifier|public
name|Table
name|create
parameter_list|(
name|IMetaStoreClient
name|client
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|TException
block|{
name|Table
name|t
init|=
name|build
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|client
operator|.
name|createTable
argument_list|(
name|t
argument_list|)
expr_stmt|;
return|return
name|t
return|;
block|}
block|}
end_class

end_unit

