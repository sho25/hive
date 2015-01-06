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
name|parse
package|;
end_package

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
name|List
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|ResourceType
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
name|ResourceUri
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
name|exec
operator|.
name|FunctionRegistry
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
name|FunctionUtils
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
name|hooks
operator|.
name|Entity
operator|.
name|Type
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
name|plan
operator|.
name|CreateFunctionDesc
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
name|DropFunctionDesc
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
name|FunctionWork
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
name|PlanUtils
import|;
end_import

begin_comment
comment|/**  * FunctionSemanticAnalyzer.  *  */
end_comment

begin_class
specifier|public
class|class
name|FunctionSemanticAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|FunctionSemanticAnalyzer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|FunctionSemanticAnalyzer
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|conf
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
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|ast
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_CREATEFUNCTION
condition|)
block|{
name|analyzeCreateFunction
argument_list|(
name|ast
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ast
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_DROPFUNCTION
condition|)
block|{
name|analyzeDropFunction
argument_list|(
name|ast
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"analyze done"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|analyzeCreateFunction
parameter_list|(
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// ^(TOK_CREATEFUNCTION identifier StringLiteral ({isTempFunction}? => TOK_TEMPORARY))
name|String
name|functionName
init|=
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
name|boolean
name|isTemporaryFunction
init|=
operator|(
name|ast
operator|.
name|getFirstChildWithType
argument_list|(
name|HiveParser
operator|.
name|TOK_TEMPORARY
argument_list|)
operator|!=
literal|null
operator|)
decl_stmt|;
name|String
name|className
init|=
name|unescapeSQLString
argument_list|(
name|ast
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
decl_stmt|;
comment|// Temp functions are not allowed to have qualified names.
if|if
condition|(
name|isTemporaryFunction
operator|&&
name|FunctionUtils
operator|.
name|isQualifiedFunctionName
argument_list|(
name|functionName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Temporary function cannot be created with a qualified name."
argument_list|)
throw|;
block|}
comment|// find any referenced resources
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|resources
init|=
name|getResourceList
argument_list|(
name|ast
argument_list|)
decl_stmt|;
name|CreateFunctionDesc
name|desc
init|=
operator|new
name|CreateFunctionDesc
argument_list|(
name|functionName
argument_list|,
name|isTemporaryFunction
argument_list|,
name|className
argument_list|,
name|resources
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
name|FunctionWork
argument_list|(
name|desc
argument_list|)
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|addEntities
argument_list|(
name|functionName
argument_list|,
name|isTemporaryFunction
argument_list|,
name|resources
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|analyzeDropFunction
parameter_list|(
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// ^(TOK_DROPFUNCTION identifier ifExists? $temp?)
name|String
name|functionName
init|=
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
decl_stmt|;
name|boolean
name|ifExists
init|=
operator|(
name|ast
operator|.
name|getFirstChildWithType
argument_list|(
name|HiveParser
operator|.
name|TOK_IFEXISTS
argument_list|)
operator|!=
literal|null
operator|)
decl_stmt|;
comment|// we want to signal an error if the function doesn't exist and we're
comment|// configured not to ignore this
name|boolean
name|throwException
init|=
operator|!
name|ifExists
operator|&&
operator|!
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|DROPIGNORESNONEXISTENT
argument_list|)
decl_stmt|;
if|if
condition|(
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
name|functionName
argument_list|)
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|throwException
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_FUNCTION
operator|.
name|getMsg
argument_list|(
name|functionName
argument_list|)
argument_list|)
throw|;
block|}
else|else
block|{
comment|// Fail silently
return|return;
block|}
block|}
name|boolean
name|isTemporaryFunction
init|=
operator|(
name|ast
operator|.
name|getFirstChildWithType
argument_list|(
name|HiveParser
operator|.
name|TOK_TEMPORARY
argument_list|)
operator|!=
literal|null
operator|)
decl_stmt|;
name|DropFunctionDesc
name|desc
init|=
operator|new
name|DropFunctionDesc
argument_list|(
name|functionName
argument_list|,
name|isTemporaryFunction
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
name|FunctionWork
argument_list|(
name|desc
argument_list|)
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|addEntities
argument_list|(
name|functionName
argument_list|,
name|isTemporaryFunction
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ResourceType
name|getResourceType
parameter_list|(
name|ASTNode
name|token
parameter_list|)
throws|throws
name|SemanticException
block|{
switch|switch
condition|(
name|token
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_JAR
case|:
return|return
name|ResourceType
operator|.
name|JAR
return|;
case|case
name|HiveParser
operator|.
name|TOK_FILE
case|:
return|return
name|ResourceType
operator|.
name|FILE
return|;
case|case
name|HiveParser
operator|.
name|TOK_ARCHIVE
case|:
return|return
name|ResourceType
operator|.
name|ARCHIVE
return|;
default|default:
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Unexpected token "
operator|+
name|token
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|getResourceList
parameter_list|(
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|resources
init|=
literal|null
decl_stmt|;
name|ASTNode
name|resourcesNode
init|=
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getFirstChildWithType
argument_list|(
name|HiveParser
operator|.
name|TOK_RESOURCE_LIST
argument_list|)
decl_stmt|;
if|if
condition|(
name|resourcesNode
operator|!=
literal|null
condition|)
block|{
name|resources
operator|=
operator|new
name|ArrayList
argument_list|<
name|ResourceUri
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|resourcesNode
operator|.
name|getChildCount
argument_list|()
condition|;
operator|++
name|idx
control|)
block|{
comment|// ^(TOK_RESOURCE_URI $resType $resPath)
name|ASTNode
name|resNode
init|=
operator|(
name|ASTNode
operator|)
name|resourcesNode
operator|.
name|getChild
argument_list|(
name|idx
argument_list|)
decl_stmt|;
if|if
condition|(
name|resNode
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|!=
name|HiveParser
operator|.
name|TOK_RESOURCE_URI
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Expected token type TOK_RESOURCE_URI but found "
operator|+
name|resNode
operator|.
name|getToken
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|resNode
operator|.
name|getChildCount
argument_list|()
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Expected 2 child nodes of TOK_RESOURCE_URI but found "
operator|+
name|resNode
operator|.
name|getChildCount
argument_list|()
argument_list|)
throw|;
block|}
name|ASTNode
name|resTypeNode
init|=
operator|(
name|ASTNode
operator|)
name|resNode
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ASTNode
name|resUriNode
init|=
operator|(
name|ASTNode
operator|)
name|resNode
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ResourceType
name|resourceType
init|=
name|getResourceType
argument_list|(
name|resTypeNode
argument_list|)
decl_stmt|;
name|resources
operator|.
name|add
argument_list|(
operator|new
name|ResourceUri
argument_list|(
name|resourceType
argument_list|,
name|PlanUtils
operator|.
name|stripQuotes
argument_list|(
name|resUriNode
operator|.
name|getText
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|resources
return|;
block|}
comment|/**    * Add write entities to the semantic analyzer to restrict function creation to privileged users.    */
specifier|private
name|void
name|addEntities
parameter_list|(
name|String
name|functionName
parameter_list|,
name|boolean
name|isTemporaryFunction
parameter_list|,
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|resources
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// If the function is being added under a database 'namespace', then add an entity representing
comment|// the database (only applicable to permanent/metastore functions).
comment|// We also add a second entity representing the function name.
comment|// The authorization api implementation can decide which entities it wants to use to
comment|// authorize the create/drop function call.
comment|// Add the relevant database 'namespace' as a WriteEntity
name|Database
name|database
init|=
literal|null
decl_stmt|;
comment|// temporary functions don't have any database 'namespace' associated with it,
comment|// it matters only for permanent functions
if|if
condition|(
operator|!
name|isTemporaryFunction
condition|)
block|{
try|try
block|{
name|String
index|[]
name|qualifiedNameParts
init|=
name|FunctionUtils
operator|.
name|getQualifiedFunctionNameParts
argument_list|(
name|functionName
argument_list|)
decl_stmt|;
name|String
name|dbName
init|=
name|qualifiedNameParts
index|[
literal|0
index|]
decl_stmt|;
name|functionName
operator|=
name|qualifiedNameParts
index|[
literal|1
index|]
expr_stmt|;
name|database
operator|=
name|getDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|database
operator|!=
literal|null
condition|)
block|{
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
comment|// Add the function name as a WriteEntity
name|outputs
operator|.
name|add
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|database
argument_list|,
name|functionName
argument_list|,
name|Type
operator|.
name|FUNCTION
argument_list|,
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DDL_NO_LOCK
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|resources
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ResourceUri
name|resource
range|:
name|resources
control|)
block|{
name|String
name|uriPath
init|=
name|resource
operator|.
name|getUri
argument_list|()
decl_stmt|;
name|outputs
operator|.
name|add
argument_list|(
name|toWriteEntity
argument_list|(
name|uriPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

