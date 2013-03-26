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
name|Partition
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
name|AbstractSemanticAnalyzerHook
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
name|plan
operator|.
name|AlterTableDesc
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
name|DDLWork
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
name|DescDatabaseDesc
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
name|DescTableDesc
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
name|DropDatabaseDesc
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
name|DropTableDesc
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
name|HiveOperation
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
name|PartitionSpec
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
name|ShowDatabasesDesc
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
name|ShowPartitionsDesc
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
name|ShowTableStatusDesc
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
name|ShowTablesDesc
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
name|SwitchDatabaseDesc
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
name|hcatalog
operator|.
name|common
operator|.
name|ErrorType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_class
specifier|public
class|class
name|HCatSemanticAnalyzer
extends|extends
name|HCatSemanticAnalyzerBase
block|{
specifier|private
name|AbstractSemanticAnalyzerHook
name|hook
decl_stmt|;
specifier|private
name|ASTNode
name|ast
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
name|this
operator|.
name|ast
operator|=
name|ast
expr_stmt|;
switch|switch
condition|(
name|ast
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
condition|)
block|{
comment|// HCat wants to intercept following tokens and special-handle them.
case|case
name|HiveParser
operator|.
name|TOK_CREATETABLE
case|:
name|hook
operator|=
operator|new
name|CreateTableHook
argument_list|()
expr_stmt|;
return|return
name|hook
operator|.
name|preAnalyze
argument_list|(
name|context
argument_list|,
name|ast
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_CREATEDATABASE
case|:
name|hook
operator|=
operator|new
name|CreateDatabaseHook
argument_list|()
expr_stmt|;
return|return
name|hook
operator|.
name|preAnalyze
argument_list|(
name|context
argument_list|,
name|ast
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PARTITION
case|:
if|if
condition|(
operator|(
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|)
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_ALTERTABLE_FILEFORMAT
condition|)
block|{
return|return
name|ast
return|;
block|}
elseif|else
if|if
condition|(
operator|(
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|)
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ALTERPARTS_MERGEFILES
condition|)
block|{
comment|// unsupported
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Operation not supported."
argument_list|)
throw|;
block|}
else|else
block|{
return|return
name|ast
return|;
block|}
comment|// HCat will allow these operations to be performed.
comment|// Database DDL
case|case
name|HiveParser
operator|.
name|TOK_SHOWDATABASES
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPDATABASE
case|:
case|case
name|HiveParser
operator|.
name|TOK_SWITCHDATABASE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DESCDATABASE
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERDATABASE_PROPERTIES
case|:
comment|// Index DDL
case|case
name|HiveParser
operator|.
name|TOK_ALTERINDEX_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_CREATEINDEX
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPINDEX
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWINDEXES
case|:
comment|// View DDL
comment|// "alter view add partition" does not work because of the nature of implementation
comment|// of the DDL in hive. Hive will internally invoke another Driver on the select statement,
comment|// and HCat does not let "select" statement through. I cannot find a way to get around it
comment|// without modifying hive code. So just leave it unsupported.
comment|//case HiveParser.TOK_ALTERVIEW_ADDPARTS:
case|case
name|HiveParser
operator|.
name|TOK_ALTERVIEW_DROPPARTS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERVIEW_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERVIEW_RENAME
case|:
case|case
name|HiveParser
operator|.
name|TOK_CREATEVIEW
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPVIEW
case|:
comment|// Authorization DDL
case|case
name|HiveParser
operator|.
name|TOK_CREATEROLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPROLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_GRANT_ROLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_GRANT_WITH_OPTION
case|:
case|case
name|HiveParser
operator|.
name|TOK_GRANT
case|:
case|case
name|HiveParser
operator|.
name|TOK_REVOKE_ROLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_REVOKE
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_GRANT
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_ROLE_GRANT
case|:
comment|// Misc DDL
case|case
name|HiveParser
operator|.
name|TOK_LOCKTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_UNLOCKTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWLOCKS
case|:
case|case
name|HiveParser
operator|.
name|TOK_DESCFUNCTION
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWFUNCTIONS
case|:
case|case
name|HiveParser
operator|.
name|TOK_EXPLAIN
case|:
comment|// Table DDL
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDPARTS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDCOLS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_CHANGECOL_AFTER_POSITION
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SERDEPROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_CLUSTER_SORT
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_DROPPARTS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAME
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAMECOL
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_REPLACECOLS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SERIALIZER
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_TOUCH
case|:
case|case
name|HiveParser
operator|.
name|TOK_DESCTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_TABLESTATUS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWPARTITIONS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWTABLES
case|:
return|return
name|ast
return|;
comment|// In all other cases, throw an exception. Its a white-list of allowed operations.
default|default:
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Operation not supported."
argument_list|)
throw|;
block|}
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
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
switch|switch
condition|(
name|ast
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
name|TOK_CREATETABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_CREATEDATABASE
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PARTITION
case|:
comment|// HCat will allow these operations to be performed.
comment|// Database DDL
case|case
name|HiveParser
operator|.
name|TOK_SHOWDATABASES
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPDATABASE
case|:
case|case
name|HiveParser
operator|.
name|TOK_SWITCHDATABASE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DESCDATABASE
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERDATABASE_PROPERTIES
case|:
comment|// Index DDL
case|case
name|HiveParser
operator|.
name|TOK_ALTERINDEX_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_CREATEINDEX
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPINDEX
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWINDEXES
case|:
comment|// View DDL
comment|//case HiveParser.TOK_ALTERVIEW_ADDPARTS:
case|case
name|HiveParser
operator|.
name|TOK_ALTERVIEW_DROPPARTS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERVIEW_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERVIEW_RENAME
case|:
case|case
name|HiveParser
operator|.
name|TOK_CREATEVIEW
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPVIEW
case|:
comment|// Authorization DDL
case|case
name|HiveParser
operator|.
name|TOK_CREATEROLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPROLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_GRANT_ROLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_GRANT_WITH_OPTION
case|:
case|case
name|HiveParser
operator|.
name|TOK_GRANT
case|:
case|case
name|HiveParser
operator|.
name|TOK_REVOKE_ROLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_REVOKE
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_GRANT
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_ROLE_GRANT
case|:
comment|// Misc DDL
case|case
name|HiveParser
operator|.
name|TOK_LOCKTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_UNLOCKTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWLOCKS
case|:
case|case
name|HiveParser
operator|.
name|TOK_DESCFUNCTION
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWFUNCTIONS
case|:
case|case
name|HiveParser
operator|.
name|TOK_EXPLAIN
case|:
comment|// Table DDL
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDPARTS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDCOLS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_CHANGECOL_AFTER_POSITION
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SERDEPROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_CLUSTER_SORT
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_DROPPARTS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAME
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAMECOL
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_REPLACECOLS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SERIALIZER
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_TOUCH
case|:
case|case
name|HiveParser
operator|.
name|TOK_DESCTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_TABLESTATUS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWPARTITIONS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWTABLES
case|:
break|break;
default|default:
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_INTERNAL_EXCEPTION
argument_list|,
literal|"Unexpected token: "
operator|+
name|ast
operator|.
name|getToken
argument_list|()
argument_list|)
throw|;
block|}
name|authorizeDDL
argument_list|(
name|context
argument_list|,
name|rootTasks
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HCatException
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
if|if
condition|(
name|hook
operator|!=
literal|null
condition|)
block|{
name|hook
operator|.
name|postAnalyze
argument_list|(
name|context
argument_list|,
name|rootTasks
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|extractTableName
parameter_list|(
name|String
name|compoundName
parameter_list|)
block|{
comment|/*       * the table name can potentially be a dot-format one with column names      * specified as part of the table name. e.g. a.b.c where b is a column in      * a and c is a field of the object/column b etc. For authorization       * purposes, we should use only the first part of the dotted name format.      *      */
name|String
index|[]
name|words
init|=
name|compoundName
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
return|return
name|words
index|[
literal|0
index|]
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|authorizeDDLWork
parameter_list|(
name|HiveSemanticAnalyzerHookContext
name|cntxt
parameter_list|,
name|Hive
name|hive
parameter_list|,
name|DDLWork
name|work
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// DB opereations, none of them are enforced by Hive right now.
name|ShowDatabasesDesc
name|showDatabases
init|=
name|work
operator|.
name|getShowDatabasesDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|showDatabases
operator|!=
literal|null
condition|)
block|{
name|authorize
argument_list|(
name|HiveOperation
operator|.
name|SHOWDATABASES
operator|.
name|getInputRequiredPrivileges
argument_list|()
argument_list|,
name|HiveOperation
operator|.
name|SHOWDATABASES
operator|.
name|getOutputRequiredPrivileges
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|DropDatabaseDesc
name|dropDb
init|=
name|work
operator|.
name|getDropDatabaseDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|dropDb
operator|!=
literal|null
condition|)
block|{
name|Database
name|db
init|=
name|cntxt
operator|.
name|getHive
argument_list|()
operator|.
name|getDatabase
argument_list|(
name|dropDb
operator|.
name|getDatabaseName
argument_list|()
argument_list|)
decl_stmt|;
name|authorize
argument_list|(
name|db
argument_list|,
name|Privilege
operator|.
name|DROP
argument_list|)
expr_stmt|;
block|}
name|DescDatabaseDesc
name|descDb
init|=
name|work
operator|.
name|getDescDatabaseDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|descDb
operator|!=
literal|null
condition|)
block|{
name|Database
name|db
init|=
name|cntxt
operator|.
name|getHive
argument_list|()
operator|.
name|getDatabase
argument_list|(
name|descDb
operator|.
name|getDatabaseName
argument_list|()
argument_list|)
decl_stmt|;
name|authorize
argument_list|(
name|db
argument_list|,
name|Privilege
operator|.
name|SELECT
argument_list|)
expr_stmt|;
block|}
name|SwitchDatabaseDesc
name|switchDb
init|=
name|work
operator|.
name|getSwitchDatabaseDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|switchDb
operator|!=
literal|null
condition|)
block|{
name|Database
name|db
init|=
name|cntxt
operator|.
name|getHive
argument_list|()
operator|.
name|getDatabase
argument_list|(
name|switchDb
operator|.
name|getDatabaseName
argument_list|()
argument_list|)
decl_stmt|;
name|authorize
argument_list|(
name|db
argument_list|,
name|Privilege
operator|.
name|SELECT
argument_list|)
expr_stmt|;
block|}
name|ShowTablesDesc
name|showTables
init|=
name|work
operator|.
name|getShowTblsDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|showTables
operator|!=
literal|null
condition|)
block|{
name|String
name|dbName
init|=
name|showTables
operator|.
name|getDbName
argument_list|()
operator|==
literal|null
condition|?
name|cntxt
operator|.
name|getHive
argument_list|()
operator|.
name|getCurrentDatabase
argument_list|()
else|:
name|showTables
operator|.
name|getDbName
argument_list|()
decl_stmt|;
name|authorize
argument_list|(
name|cntxt
operator|.
name|getHive
argument_list|()
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
argument_list|,
name|Privilege
operator|.
name|SELECT
argument_list|)
expr_stmt|;
block|}
name|ShowTableStatusDesc
name|showTableStatus
init|=
name|work
operator|.
name|getShowTblStatusDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|showTableStatus
operator|!=
literal|null
condition|)
block|{
name|String
name|dbName
init|=
name|showTableStatus
operator|.
name|getDbName
argument_list|()
operator|==
literal|null
condition|?
name|cntxt
operator|.
name|getHive
argument_list|()
operator|.
name|getCurrentDatabase
argument_list|()
else|:
name|showTableStatus
operator|.
name|getDbName
argument_list|()
decl_stmt|;
name|authorize
argument_list|(
name|cntxt
operator|.
name|getHive
argument_list|()
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
argument_list|,
name|Privilege
operator|.
name|SELECT
argument_list|)
expr_stmt|;
block|}
comment|// TODO: add alter database support in HCat
comment|// Table operations.
name|DropTableDesc
name|dropTable
init|=
name|work
operator|.
name|getDropTblDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|dropTable
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|dropTable
operator|.
name|getPartSpecs
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// drop table is already enforced by Hive. We only check for table level location even if the
comment|// table is partitioned.
block|}
else|else
block|{
comment|//this is actually a ALTER TABLE DROP PARITITION statement
for|for
control|(
name|PartitionSpec
name|partSpec
range|:
name|dropTable
operator|.
name|getPartSpecs
argument_list|()
control|)
block|{
comment|// partitions are not added as write entries in drop partitions in Hive
name|Table
name|table
init|=
name|hive
operator|.
name|getTable
argument_list|(
name|hive
operator|.
name|getCurrentDatabase
argument_list|()
argument_list|,
name|dropTable
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
literal|null
decl_stmt|;
try|try
block|{
name|partitions
operator|=
name|hive
operator|.
name|getPartitionsByFilter
argument_list|(
name|table
argument_list|,
name|partSpec
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
for|for
control|(
name|Partition
name|part
range|:
name|partitions
control|)
block|{
name|authorize
argument_list|(
name|part
argument_list|,
name|Privilege
operator|.
name|DROP
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|AlterTableDesc
name|alterTable
init|=
name|work
operator|.
name|getAlterTblDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|alterTable
operator|!=
literal|null
condition|)
block|{
name|Table
name|table
init|=
name|hive
operator|.
name|getTable
argument_list|(
name|hive
operator|.
name|getCurrentDatabase
argument_list|()
argument_list|,
name|alterTable
operator|.
name|getOldName
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Partition
name|part
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|alterTable
operator|.
name|getPartSpec
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|part
operator|=
name|hive
operator|.
name|getPartition
argument_list|(
name|table
argument_list|,
name|alterTable
operator|.
name|getPartSpec
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|String
name|newLocation
init|=
name|alterTable
operator|.
name|getNewLocation
argument_list|()
decl_stmt|;
comment|/* Hcat requires ALTER_DATA privileges for ALTER TABLE LOCATION statements        * for the old table/partition location and the new location.        */
if|if
condition|(
name|alterTable
operator|.
name|getOp
argument_list|()
operator|==
name|AlterTableDesc
operator|.
name|AlterTableTypes
operator|.
name|ALTERLOCATION
condition|)
block|{
if|if
condition|(
name|part
operator|!=
literal|null
condition|)
block|{
name|authorize
argument_list|(
name|part
argument_list|,
name|Privilege
operator|.
name|ALTER_DATA
argument_list|)
expr_stmt|;
comment|// authorize for the old
comment|// location, and new location
name|part
operator|.
name|setLocation
argument_list|(
name|newLocation
argument_list|)
expr_stmt|;
name|authorize
argument_list|(
name|part
argument_list|,
name|Privilege
operator|.
name|ALTER_DATA
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|authorize
argument_list|(
name|table
argument_list|,
name|Privilege
operator|.
name|ALTER_DATA
argument_list|)
expr_stmt|;
comment|// authorize for the old
comment|// location, and new location
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getSd
argument_list|()
operator|.
name|setLocation
argument_list|(
name|newLocation
argument_list|)
expr_stmt|;
name|authorize
argument_list|(
name|table
argument_list|,
name|Privilege
operator|.
name|ALTER_DATA
argument_list|)
expr_stmt|;
block|}
block|}
comment|//other alter operations are already supported by Hive
block|}
comment|// we should be careful when authorizing table based on just the
comment|// table name. If columns have separate authorization domain, it
comment|// must be honored
name|DescTableDesc
name|descTable
init|=
name|work
operator|.
name|getDescTblDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|descTable
operator|!=
literal|null
condition|)
block|{
name|String
name|tableName
init|=
name|extractTableName
argument_list|(
name|descTable
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|authorizeTable
argument_list|(
name|cntxt
operator|.
name|getHive
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|Privilege
operator|.
name|SELECT
argument_list|)
expr_stmt|;
block|}
name|ShowPartitionsDesc
name|showParts
init|=
name|work
operator|.
name|getShowPartsDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|showParts
operator|!=
literal|null
condition|)
block|{
name|String
name|tableName
init|=
name|extractTableName
argument_list|(
name|showParts
operator|.
name|getTabName
argument_list|()
argument_list|)
decl_stmt|;
name|authorizeTable
argument_list|(
name|cntxt
operator|.
name|getHive
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|Privilege
operator|.
name|SELECT
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

