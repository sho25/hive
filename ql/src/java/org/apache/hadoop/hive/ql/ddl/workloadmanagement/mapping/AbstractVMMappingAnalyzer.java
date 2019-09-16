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
name|mapping
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
name|workloadmanagement
operator|.
name|WMUtils
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
comment|/**  * Abstract ancestor of Create and Alter WM Mapping analyzers.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractVMMappingAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
specifier|public
name|AbstractVMMappingAnalyzer
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
operator|<
literal|4
operator|||
name|root
operator|.
name|getChildCount
argument_list|()
operator|>
literal|5
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Invalid syntax for create or alter mapping."
argument_list|)
throw|;
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
name|String
name|entityType
init|=
name|root
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getText
argument_list|()
decl_stmt|;
name|String
name|entityName
init|=
name|PlanUtils
operator|.
name|stripQuotes
argument_list|(
name|root
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|poolPath
init|=
name|root
operator|.
name|getChild
argument_list|(
literal|3
argument_list|)
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_UNMANAGED
condition|?
literal|null
else|:
name|WMUtils
operator|.
name|poolPath
argument_list|(
name|root
operator|.
name|getChild
argument_list|(
literal|3
argument_list|)
argument_list|)
decl_stmt|;
comment|// Null path => unmanaged
name|Integer
name|ordering
init|=
name|root
operator|.
name|getChildCount
argument_list|()
operator|==
literal|5
condition|?
name|Integer
operator|.
name|valueOf
argument_list|(
name|root
operator|.
name|getChild
argument_list|(
literal|4
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
else|:
literal|null
decl_stmt|;
name|DDLDesc
name|desc
init|=
name|getDesc
argument_list|(
name|resourcePlanName
argument_list|,
name|entityType
argument_list|,
name|entityName
argument_list|,
name|poolPath
argument_list|,
name|ordering
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
specifier|protected
specifier|abstract
name|DDLDesc
name|getDesc
parameter_list|(
name|String
name|resourcePlanName
parameter_list|,
name|String
name|entityType
parameter_list|,
name|String
name|entityName
parameter_list|,
name|String
name|poolPath
parameter_list|,
name|Integer
name|ordering
parameter_list|)
function_decl|;
block|}
end_class

end_unit

