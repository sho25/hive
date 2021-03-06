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
name|workloadmanagement
operator|.
name|resourceplan
operator|.
name|alter
operator|.
name|enable
package|;
end_package

begin_import
import|import
name|org
operator|.
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|Tree
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
comment|/**  * Analyzer for enable resource plan commands.  */
end_comment

begin_class
annotation|@
name|DDLType
argument_list|(
name|types
operator|=
name|HiveParser
operator|.
name|TOK_ALTER_RP_ENABLE
argument_list|)
specifier|public
class|class
name|AlterResourcePlanEnableAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
specifier|public
name|AlterResourcePlanEnableAnalyzer
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
if|if
condition|(
name|root
operator|.
name|getChildCount
argument_list|()
operator|==
literal|0
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Activate a resource plan to enable workload management!"
argument_list|)
expr_stmt|;
return|return;
block|}
name|String
name|resourcePlanName
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
name|enable
init|=
literal|false
decl_stmt|;
name|boolean
name|activate
init|=
literal|false
decl_stmt|;
name|boolean
name|replace
init|=
literal|false
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
operator|++
name|i
control|)
block|{
name|Tree
name|child
init|=
name|root
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|child
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_ACTIVATE
case|:
name|activate
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|child
operator|.
name|getChildCount
argument_list|()
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Expected 0 or 1 arguments "
operator|+
name|root
operator|.
name|toStringTree
argument_list|()
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|child
operator|.
name|getChildCount
argument_list|()
operator|==
literal|1
condition|)
block|{
if|if
condition|(
name|child
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
operator|!=
name|HiveParser
operator|.
name|TOK_REPLACE
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Incorrect syntax "
operator|+
name|root
operator|.
name|toStringTree
argument_list|()
argument_list|)
throw|;
block|}
name|replace
operator|=
literal|true
expr_stmt|;
block|}
break|break;
case|case
name|HiveParser
operator|.
name|TOK_ENABLE
case|:
name|enable
operator|=
literal|true
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_REPLACE
case|:
name|replace
operator|=
literal|true
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Unexpected token in alter resource plan statement: "
operator|+
name|child
operator|.
name|getType
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|AlterResourcePlanEnableDesc
name|desc
init|=
operator|new
name|AlterResourcePlanEnableDesc
argument_list|(
name|resourcePlanName
argument_list|,
name|enable
argument_list|,
name|activate
argument_list|,
name|replace
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|DDLWork
argument_list|>
name|task
init|=
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
decl_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
name|DDLUtils
operator|.
name|addServiceOutput
argument_list|(
name|conf
argument_list|,
name|getOutputs
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

