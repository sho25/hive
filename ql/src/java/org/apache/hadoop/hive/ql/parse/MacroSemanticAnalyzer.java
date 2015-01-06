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
import|import static
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
operator|.
name|TOK_IFEXISTS
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
name|Collections
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Stack
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashSet
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
name|ColumnInfo
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
name|lib
operator|.
name|Dispatcher
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
name|lib
operator|.
name|Node
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
name|lib
operator|.
name|PreOrderWalker
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
name|plan
operator|.
name|CreateMacroDesc
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
name|DropMacroDesc
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
name|ExprNodeDesc
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
name|session
operator|.
name|SessionState
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoUtils
import|;
end_import

begin_comment
comment|/**  * MacroSemanticAnalyzer.  *  */
end_comment

begin_class
specifier|public
class|class
name|MacroSemanticAnalyzer
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
name|MacroSemanticAnalyzer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|MacroSemanticAnalyzer
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
name|TOK_CREATEMACRO
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Analyzing create macro "
operator|+
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
expr_stmt|;
name|analyzeCreateMacro
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
name|TOK_DROPMACRO
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Analyzing drop macro "
operator|+
name|ast
operator|.
name|dump
argument_list|()
argument_list|)
expr_stmt|;
name|analyzeDropMacro
argument_list|(
name|ast
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|void
name|analyzeCreateMacro
parameter_list|(
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
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
comment|// Temp macros are not allowed to have qualified names.
if|if
condition|(
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
literal|"Temporary macro cannot be created with a qualified name."
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|arguments
init|=
name|BaseSemanticAnalyzer
operator|.
name|getColumns
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
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|boolean
name|isNoArgumentMacro
init|=
name|arguments
operator|.
name|size
argument_list|()
operator|==
literal|0
decl_stmt|;
name|RowResolver
name|rowResolver
init|=
operator|new
name|RowResolver
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|macroColNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|arguments
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
name|macroColTypes
init|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|(
name|arguments
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|actualColumnNames
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|isNoArgumentMacro
condition|)
block|{
comment|/*        * Walk down expression to see which arguments are actually used.        */
name|Node
name|expression
init|=
operator|(
name|Node
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|PreOrderWalker
name|walker
init|=
operator|new
name|PreOrderWalker
argument_list|(
operator|new
name|Dispatcher
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|dispatch
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|nd
operator|instanceof
name|ASTNode
condition|)
block|{
name|ASTNode
name|node
init|=
operator|(
name|ASTNode
operator|)
name|nd
decl_stmt|;
if|if
condition|(
name|node
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_TABLE_OR_COL
condition|)
block|{
name|actualColumnNames
operator|.
name|add
argument_list|(
name|node
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
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|walker
operator|.
name|startWalking
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|expression
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|FieldSchema
name|argument
range|:
name|arguments
control|)
block|{
name|TypeInfo
name|colType
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
name|argument
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
name|rowResolver
operator|.
name|put
argument_list|(
literal|""
argument_list|,
name|argument
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|ColumnInfo
argument_list|(
name|argument
operator|.
name|getName
argument_list|()
argument_list|,
name|colType
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|macroColNames
operator|.
name|add
argument_list|(
name|argument
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|macroColTypes
operator|.
name|add
argument_list|(
name|colType
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|expectedColumnNames
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|macroColNames
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|expectedColumnNames
operator|.
name|equals
argument_list|(
name|actualColumnNames
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Expected columns "
operator|+
name|expectedColumnNames
operator|+
literal|" but found "
operator|+
name|actualColumnNames
argument_list|)
throw|;
block|}
if|if
condition|(
name|expectedColumnNames
operator|.
name|size
argument_list|()
operator|!=
name|macroColNames
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"At least one parameter name was used more than once "
operator|+
name|macroColNames
argument_list|)
throw|;
block|}
name|SemanticAnalyzer
name|sa
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_CBO_ENABLED
argument_list|)
condition|?
operator|new
name|CalcitePlanner
argument_list|(
name|conf
argument_list|)
else|:
operator|new
name|SemanticAnalyzer
argument_list|(
name|conf
argument_list|)
decl_stmt|;
empty_stmt|;
name|ExprNodeDesc
name|body
decl_stmt|;
if|if
condition|(
name|isNoArgumentMacro
condition|)
block|{
name|body
operator|=
name|sa
operator|.
name|genExprNodeDesc
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
argument_list|,
name|rowResolver
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|body
operator|=
name|sa
operator|.
name|genExprNodeDesc
argument_list|(
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
argument_list|,
name|rowResolver
argument_list|)
expr_stmt|;
block|}
name|CreateMacroDesc
name|desc
init|=
operator|new
name|CreateMacroDesc
argument_list|(
name|functionName
argument_list|,
name|macroColNames
argument_list|,
name|macroColTypes
argument_list|,
name|body
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
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|void
name|analyzeDropMacro
parameter_list|(
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
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
comment|// Temp macros are not allowed to have qualified names.
if|if
condition|(
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
literal|"Temporary macro name cannot be a qualified name."
argument_list|)
throw|;
block|}
if|if
condition|(
name|throwException
operator|&&
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
name|DropMacroDesc
name|desc
init|=
operator|new
name|DropMacroDesc
argument_list|(
name|functionName
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
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|addEntities
parameter_list|()
throws|throws
name|SemanticException
block|{
name|Database
name|database
init|=
name|getDatabase
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|)
decl_stmt|;
comment|// This restricts macro creation to privileged users.
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

