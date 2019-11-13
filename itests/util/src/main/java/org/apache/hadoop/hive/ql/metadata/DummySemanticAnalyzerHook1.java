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
name|metadata
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
name|ql
operator|.
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
import|;
end_import

begin_class
specifier|public
class|class
name|DummySemanticAnalyzerHook1
extends|extends
name|AbstractSemanticAnalyzerHook
block|{
specifier|static
name|int
name|count
init|=
literal|0
decl_stmt|;
name|int
name|myCount
init|=
operator|-
literal|1
decl_stmt|;
name|boolean
name|isCreateTable
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
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
name|isCreateTable
operator|=
operator|(
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
name|TOK_CREATETABLE
operator|)
expr_stmt|;
name|myCount
operator|=
name|count
operator|++
expr_stmt|;
if|if
condition|(
name|isCreateTable
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"DummySemanticAnalyzerHook1 Pre: Count "
operator|+
name|myCount
argument_list|)
expr_stmt|;
block|}
return|return
name|ast
return|;
block|}
specifier|public
name|DummySemanticAnalyzerHook1
parameter_list|()
block|{   }
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
name|count
operator|=
literal|0
expr_stmt|;
if|if
condition|(
operator|!
name|isCreateTable
condition|)
block|{
return|return;
block|}
name|CreateTableDesc
name|desc
init|=
call|(
name|CreateTableDesc
call|)
argument_list|(
operator|(
name|DDLTask
operator|)
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
argument_list|)
operator|.
name|getWork
argument_list|()
operator|.
name|getDDLDesc
argument_list|()
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
name|tblProps
operator|.
name|put
argument_list|(
literal|"createdBy"
argument_list|,
name|DummyCreateTableHook
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tblProps
operator|.
name|put
argument_list|(
literal|"Message"
argument_list|,
literal|"Hive rocks!! Count: "
operator|+
name|myCount
argument_list|)
expr_stmt|;
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"DummySemanticAnalyzerHook1 Post: Hive rocks!! Count: "
operator|+
name|myCount
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

