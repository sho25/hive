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
name|function
operator|.
name|show
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
comment|/**  * Analyzer for showing functions commands.  */
end_comment

begin_class
annotation|@
name|DDLType
argument_list|(
name|types
operator|=
name|HiveParser
operator|.
name|TOK_SHOWFUNCTIONS
argument_list|)
specifier|public
class|class
name|ShowFunctionsAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
specifier|public
name|ShowFunctionsAnalyzer
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
name|ctx
operator|.
name|setResFile
argument_list|(
name|ctx
operator|.
name|getLocalTmpPath
argument_list|()
argument_list|)
expr_stmt|;
name|ShowFunctionsDesc
name|desc
decl_stmt|;
if|if
condition|(
name|root
operator|.
name|getChildCount
argument_list|()
operator|>
literal|0
condition|)
block|{
assert|assert
operator|(
name|root
operator|.
name|getChildCount
argument_list|()
operator|==
literal|2
operator|)
assert|;
assert|assert
operator|(
name|root
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|KW_LIKE
operator|)
assert|;
name|String
name|functionNames
init|=
name|stripQuotes
argument_list|(
name|root
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
name|desc
operator|=
operator|new
name|ShowFunctionsDesc
argument_list|(
name|ctx
operator|.
name|getResFile
argument_list|()
argument_list|,
name|functionNames
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|desc
operator|=
operator|new
name|ShowFunctionsDesc
argument_list|(
name|ctx
operator|.
name|getResFile
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|task
operator|.
name|setFetchSource
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|setFetchTask
argument_list|(
name|createFetchTask
argument_list|(
name|ShowFunctionsDesc
operator|.
name|SCHEMA
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

