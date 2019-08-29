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
name|parse
package|;
end_package

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|antlr
operator|.
name|runtime
operator|.
name|CommonToken
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
name|function
operator|.
name|macro
operator|.
name|create
operator|.
name|CreateMacroAnalyzer
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
name|function
operator|.
name|macro
operator|.
name|drop
operator|.
name|DropMacroAnalyzer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestSemanticAnalyzerFactory
block|{
specifier|private
name|QueryState
name|queryState
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|queryState
operator|=
operator|new
name|QueryState
operator|.
name|Builder
argument_list|()
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreate
parameter_list|()
throws|throws
name|Exception
block|{
name|BaseSemanticAnalyzer
name|analyzer
init|=
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
name|queryState
argument_list|,
operator|new
name|ASTNode
argument_list|(
operator|new
name|CommonToken
argument_list|(
name|HiveParser
operator|.
name|TOK_CREATEMACRO
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|analyzer
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|analyzer
operator|instanceof
name|CreateMacroAnalyzer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDrop
parameter_list|()
throws|throws
name|Exception
block|{
name|BaseSemanticAnalyzer
name|analyzer
init|=
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
name|queryState
argument_list|,
operator|new
name|ASTNode
argument_list|(
operator|new
name|CommonToken
argument_list|(
name|HiveParser
operator|.
name|TOK_DROPMACRO
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|analyzer
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|analyzer
operator|instanceof
name|DropMacroAnalyzer
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

