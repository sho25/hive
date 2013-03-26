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
name|fs
operator|.
name|permission
operator|.
name|FsAction
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
name|InvalidTableException
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
name|hcatalog
operator|.
name|common
operator|.
name|AuthUtils
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
name|AbstractSemanticAnalyzerHook
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
comment|// Howl wants to intercept following tokens and special-handle them.
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
comment|// DML commands used in Howl where we use the same implementation as default Hive.
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
return|return
name|ast
return|;
comment|// Howl will allow these operations to be performed since they are DDL statements.
case|case
name|HiveParser
operator|.
name|TOK_DROPTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DESCTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDCOLS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAME
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
name|TOK_ALTERTABLE_SERIALIZER
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SERDEPROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_TABLESTATUS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWTABLES
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWPARTITIONS
case|:
return|return
name|ast
return|;
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDPARTS
case|:
name|hook
operator|=
operator|new
name|AddPartitionHook
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
name|hook
operator|=
operator|new
name|AlterTableFileFormatHook
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
block|}
else|else
block|{
return|return
name|ast
return|;
block|}
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
name|TOK_DESCTABLE
case|:
name|authorize
argument_list|(
name|getFullyQualifiedName
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
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|,
name|context
argument_list|,
name|FsAction
operator|.
name|READ
argument_list|,
literal|false
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_SHOWPARTITIONS
case|:
name|authorize
argument_list|(
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
argument_list|,
name|context
argument_list|,
name|FsAction
operator|.
name|READ
argument_list|,
literal|false
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDPARTS
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDCOLS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAME
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
name|TOK_ALTERTABLE_SERIALIZER
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SERDEPROPERTIES
case|:
name|authorize
argument_list|(
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
argument_list|,
name|context
argument_list|,
name|FsAction
operator|.
name|WRITE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PARTITION
case|:
name|authorize
argument_list|(
name|BaseSemanticAnalyzer
operator|.
name|unescapeIdentifier
argument_list|(
operator|(
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
argument_list|,
name|context
argument_list|,
name|FsAction
operator|.
name|WRITE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_SWITCHDATABASE
case|:
name|authorize
argument_list|(
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
argument_list|,
name|context
argument_list|,
name|FsAction
operator|.
name|READ
argument_list|,
literal|true
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_DROPDATABASE
case|:
name|authorize
argument_list|(
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
argument_list|,
name|context
argument_list|,
name|FsAction
operator|.
name|WRITE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_CREATEDATABASE
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWDATABASES
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_TABLESTATUS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWTABLES
case|:
comment|// We do no checks for show tables/db , create db. Its always allowed.
case|case
name|HiveParser
operator|.
name|TOK_CREATETABLE
case|:
comment|// No checks for Create Table, since its not possible to compute location
comment|// here easily. So, it is especially handled in CreateTable post hook.
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
name|MetaException
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
name|void
name|authorize
parameter_list|(
name|String
name|name
parameter_list|,
name|HiveSemanticAnalyzerHookContext
name|cntxt
parameter_list|,
name|FsAction
name|action
parameter_list|,
name|boolean
name|isDBOp
parameter_list|)
throws|throws
name|MetaException
throws|,
name|HiveException
throws|,
name|HCatException
block|{
name|Warehouse
name|wh
init|=
operator|new
name|Warehouse
argument_list|(
name|cntxt
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isDBOp
condition|)
block|{
comment|// Do validations for table path.
name|Table
name|tbl
decl_stmt|;
try|try
block|{
name|tbl
operator|=
name|cntxt
operator|.
name|getHive
argument_list|()
operator|.
name|getTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidTableException
name|ite
parameter_list|)
block|{
comment|// Table itself doesn't exist in metastore, nothing to validate.
return|return;
block|}
name|Path
name|path
init|=
name|tbl
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|path
operator|!=
literal|null
condition|)
block|{
name|AuthUtils
operator|.
name|authorize
argument_list|(
name|wh
operator|.
name|getDnsPath
argument_list|(
name|path
argument_list|)
argument_list|,
name|action
argument_list|,
name|cntxt
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// This will happen, if table exists in metastore for a given
comment|// tablename, but has no path associated with it, so there is nothing to check.
comment|// In such cases, do no checks and allow whatever hive behavior is for it.
return|return;
block|}
block|}
else|else
block|{
comment|// Else, its a DB operation.
name|AuthUtils
operator|.
name|authorize
argument_list|(
name|wh
operator|.
name|getDefaultDatabasePath
argument_list|(
name|name
argument_list|)
argument_list|,
name|action
argument_list|,
name|cntxt
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|getFullyQualifiedName
parameter_list|(
name|ASTNode
name|ast
parameter_list|)
block|{
comment|// Copied verbatim from DDLSemanticAnalyzer, since its private there.
if|if
condition|(
name|ast
operator|.
name|getChildCount
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|ast
operator|.
name|getText
argument_list|()
return|;
block|}
return|return
name|getFullyQualifiedName
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
operator|+
literal|"."
operator|+
name|getFullyQualifiedName
argument_list|(
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

