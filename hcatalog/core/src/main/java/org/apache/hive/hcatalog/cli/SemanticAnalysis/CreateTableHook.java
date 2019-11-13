begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|cli
operator|.
name|SemanticAnalysis
package|;
end_package

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
name|ddl
operator|.
name|DDLDesc
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
name|ddl
operator|.
name|table
operator|.
name|create
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
name|metadata
operator|.
name|Hive
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
name|ASTNode
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
name|HiveParser
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
name|HiveSemanticAnalyzerHookContext
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
name|ql
operator|.
name|parse
operator|.
name|StorageFormat
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
name|security
operator|.
name|authorization
operator|.
name|Privilege
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_class
specifier|final
class|class
name|CreateTableHook
extends|extends
name|HCatSemanticAnalyzerBase
block|{
specifier|private
name|String
name|tableName
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ASTNode
name|preAnalyze
parameter_list|(
name|HiveSemanticAnalyzerHookContext
name|context
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Hive
name|db
decl_stmt|;
try|try
block|{
name|db
operator|=
name|context
operator|.
name|getHive
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Couldn't get Hive DB instance in semantic analysis phase."
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// Analyze and create tbl properties object
name|int
name|numCh
init|=
name|ast
operator|.
name|getChildCount
argument_list|()
decl_stmt|;
name|tableName
operator|=
name|BaseSemanticAnalyzer
operator|.
name|getUnescapedName
argument_list|(
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|boolean
name|likeTable
init|=
literal|false
decl_stmt|;
name|StorageFormat
name|format
init|=
operator|new
name|StorageFormat
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|num
init|=
literal|1
init|;
name|num
operator|<
name|numCh
condition|;
name|num
operator|++
control|)
block|{
name|ASTNode
name|child
init|=
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
name|num
argument_list|)
decl_stmt|;
if|if
condition|(
name|format
operator|.
name|fillStorageFormat
argument_list|(
name|child
argument_list|)
condition|)
block|{
if|if
condition|(
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|format
operator|.
name|getStorageHandler
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|ast
return|;
block|}
continue|continue;
block|}
switch|switch
condition|(
name|child
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_QUERY
case|:
comment|// CTAS
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Operation not supported. Create table as "
operator|+
literal|"Select is not a valid operation."
argument_list|)
throw|;
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_BUCKETS
case|:
break|break;
case|case
name|HiveParser
operator|.
name|TOK_LIKETABLE
case|:
name|likeTable
operator|=
literal|true
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_IFNOTEXISTS
case|:
try|try
block|{
name|List
argument_list|<
name|String
argument_list|>
name|tables
init|=
name|db
operator|.
name|getTablesByPattern
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|tables
operator|!=
literal|null
operator|&&
name|tables
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// table
comment|// exists
return|return
name|ast
return|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
break|break;
case|case
name|HiveParser
operator|.
name|TOK_TABLEPARTCOLS
case|:
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
init|=
name|BaseSemanticAnalyzer
operator|.
name|getColumns
argument_list|(
name|child
argument_list|,
literal|false
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|FieldSchema
name|fs
range|:
name|partCols
control|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|getType
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"string"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Operation not supported. HCatalog only "
operator|+
literal|"supports partition columns of type string. "
operator|+
literal|"For column: "
operator|+
name|fs
operator|.
name|getName
argument_list|()
operator|+
literal|" Found type: "
operator|+
name|fs
operator|.
name|getType
argument_list|()
argument_list|)
throw|;
block|}
block|}
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|likeTable
operator|&&
operator|(
name|format
operator|.
name|getInputFormat
argument_list|()
operator|==
literal|null
operator|||
name|format
operator|.
name|getOutputFormat
argument_list|()
operator|==
literal|null
operator|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"STORED AS specification is either incomplete or incorrect."
argument_list|)
throw|;
block|}
return|return
name|ast
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postAnalyze
parameter_list|(
name|HiveSemanticAnalyzerHookContext
name|context
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|rootTasks
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|rootTasks
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// There will be no DDL task created in case if its CREATE TABLE IF NOT EXISTS
return|return;
block|}
name|Task
argument_list|<
name|?
argument_list|>
name|t
init|=
name|rootTasks
operator|.
name|get
argument_list|(
name|rootTasks
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|t
operator|instanceof
name|DDLTask
operator|)
condition|)
block|{
return|return;
block|}
name|DDLTask
name|task
init|=
operator|(
name|DDLTask
operator|)
name|t
decl_stmt|;
name|DDLDesc
name|d
init|=
name|task
operator|.
name|getWork
argument_list|()
operator|.
name|getDDLDesc
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|d
operator|instanceof
name|CreateTableDesc
operator|)
condition|)
block|{
return|return;
block|}
name|CreateTableDesc
name|desc
init|=
operator|(
name|CreateTableDesc
operator|)
name|d
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
init|=
name|desc
operator|.
name|getTblProps
argument_list|()
decl_stmt|;
if|if
condition|(
name|tblProps
operator|==
literal|null
condition|)
block|{
comment|// tblProps will be null if user didnt use tblprops in his CREATE
comment|// TABLE cmd.
name|tblProps
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|// first check if we will allow the user to create table.
name|String
name|storageHandler
init|=
name|desc
operator|.
name|getStorageHandler
argument_list|()
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|storageHandler
argument_list|)
condition|)
block|{
try|try
block|{
name|HiveStorageHandler
name|storageHandlerInst
init|=
name|HCatUtil
operator|.
name|getStorageHandler
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|desc
operator|.
name|getStorageHandler
argument_list|()
argument_list|,
name|desc
operator|.
name|getSerName
argument_list|()
argument_list|,
name|desc
operator|.
name|getInputFormat
argument_list|()
argument_list|,
name|desc
operator|.
name|getOutputFormat
argument_list|()
argument_list|)
decl_stmt|;
comment|//Authorization checks are performed by the storageHandler.getAuthorizationProvider(), if
comment|//StorageDelegationAuthorizationProvider is used.
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
try|try
block|{
name|Table
name|table
init|=
name|context
operator|.
name|getHive
argument_list|()
operator|.
name|newTable
argument_list|(
name|desc
operator|.
name|getDbTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|getLocation
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|setDataLocation
argument_list|(
operator|new
name|Path
argument_list|(
name|desc
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|desc
operator|.
name|getStorageHandler
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|table
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
name|desc
operator|.
name|getStorageHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|prop
range|:
name|tblProps
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|table
operator|.
name|setProperty
argument_list|(
name|prop
operator|.
name|getKey
argument_list|()
argument_list|,
name|prop
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|prop
range|:
name|desc
operator|.
name|getSerdeProps
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|table
operator|.
name|setSerdeParam
argument_list|(
name|prop
operator|.
name|getKey
argument_list|()
argument_list|,
name|prop
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|//TODO: set other Table properties as needed
comment|//authorize against the table operation so that location permissions can be checked if any
if|if
condition|(
name|HCatAuthUtil
operator|.
name|isAuthorizationEnabled
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
name|authorize
argument_list|(
name|table
argument_list|,
name|Privilege
operator|.
name|CREATE
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
name|desc
operator|.
name|setTblProps
argument_list|(
name|tblProps
argument_list|)
expr_stmt|;
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_TBL_NAME
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

