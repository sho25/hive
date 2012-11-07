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
name|CreateFunctionDesc
name|desc
init|=
operator|new
name|CreateFunctionDesc
argument_list|(
name|functionName
argument_list|,
name|className
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
name|DropFunctionDesc
name|desc
init|=
operator|new
name|DropFunctionDesc
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
block|}
block|}
end_class

end_unit

