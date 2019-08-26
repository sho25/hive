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
name|database
operator|.
name|create
package|;
end_package

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
name|QueryState
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
name|ddl
operator|.
name|DDLSemanticAnalyzerFactory
operator|.
name|DDLType
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
name|SemanticException
import|;
end_import

begin_comment
comment|/**  * Analyzer for database creation commands.  */
end_comment

begin_class
annotation|@
name|DDLType
argument_list|(
name|type
operator|=
name|HiveParser
operator|.
name|TOK_CREATEDATABASE
argument_list|)
specifier|public
class|class
name|CreateDatabaseAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
specifier|public
name|CreateDatabaseAnalyzer
parameter_list|(
name|QueryState
name|queryState
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|queryState
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|analyzeInternal
parameter_list|(
name|ASTNode
name|root
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|databaseName
init|=
name|unescapeIdentifier
argument_list|(
name|root
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|ifNotExists
init|=
literal|false
decl_stmt|;
name|String
name|comment
init|=
literal|null
decl_stmt|;
name|String
name|locationUri
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|root
operator|.
name|getChildCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ASTNode
name|childNode
init|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|childNode
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
name|TOK_IFNOTEXISTS
case|:
name|ifNotExists
operator|=
literal|true
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_DATABASECOMMENT
case|:
name|comment
operator|=
name|unescapeSQLString
argument_list|(
name|childNode
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_DATABASEPROPERTIES
case|:
name|props
operator|=
name|getProps
argument_list|(
operator|(
name|ASTNode
operator|)
name|childNode
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_DATABASELOCATION
case|:
name|locationUri
operator|=
name|unescapeSQLString
argument_list|(
name|childNode
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
name|outputs
operator|.
name|add
argument_list|(
name|toWriteEntity
argument_list|(
name|locationUri
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Unrecognized token in CREATE DATABASE statement"
argument_list|)
throw|;
block|}
block|}
name|CreateDatabaseDesc
name|desc
init|=
operator|new
name|CreateDatabaseDesc
argument_list|(
name|databaseName
argument_list|,
name|comment
argument_list|,
name|locationUri
argument_list|,
name|ifNotExists
argument_list|,
name|props
argument_list|)
decl_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork
argument_list|(
name|getInputs
argument_list|()
argument_list|,
name|getOutputs
argument_list|()
argument_list|,
name|desc
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Database
name|database
init|=
operator|new
name|Database
argument_list|(
name|databaseName
argument_list|,
name|comment
argument_list|,
name|locationUri
argument_list|,
name|props
argument_list|)
decl_stmt|;
name|outputs
operator|.
name|add
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|database
argument_list|,
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DDL_NO_LOCK
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

