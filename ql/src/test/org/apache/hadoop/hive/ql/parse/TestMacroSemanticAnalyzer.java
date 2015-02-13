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
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|Context
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFMacro
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
name|TestMacroSemanticAnalyzer
block|{
specifier|private
name|ParseDriver
name|parseDriver
decl_stmt|;
specifier|private
name|MacroSemanticAnalyzer
name|analyzer
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|Context
name|context
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
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|context
operator|=
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|parseDriver
operator|=
operator|new
name|ParseDriver
argument_list|()
expr_stmt|;
name|analyzer
operator|=
operator|new
name|MacroSemanticAnalyzer
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ASTNode
name|parse
parameter_list|(
name|String
name|command
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|ParseUtils
operator|.
name|findRootNonNullToken
argument_list|(
name|parseDriver
operator|.
name|parse
argument_list|(
name|command
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|analyze
parameter_list|(
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|Exception
block|{
name|analyzer
operator|.
name|analyze
argument_list|(
name|ast
argument_list|,
name|context
argument_list|)
expr_stmt|;
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
init|=
name|analyzer
operator|.
name|getRootTasks
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rootTasks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
range|:
name|rootTasks
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|task
operator|.
name|executeTask
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDropMacroDoesNotExist
parameter_list|()
throws|throws
name|Exception
block|{
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"DROP TEMPORARY MACRO SOME_MACRO"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDropMacroExistsDoNotIgnoreErrors
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|DROPIGNORESNONEXISTENT
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|FunctionRegistry
operator|.
name|registerTemporaryUDF
argument_list|(
literal|"SOME_MACRO"
argument_list|,
name|GenericUDFMacro
operator|.
name|class
argument_list|)
expr_stmt|;
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"DROP TEMPORARY MACRO SOME_MACRO"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDropMacro
parameter_list|()
throws|throws
name|Exception
block|{
name|FunctionRegistry
operator|.
name|registerTemporaryUDF
argument_list|(
literal|"SOME_MACRO"
argument_list|,
name|GenericUDFMacro
operator|.
name|class
argument_list|)
expr_stmt|;
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"DROP TEMPORARY MACRO SOME_MACRO"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|SemanticException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testDropMacroNonExistent
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|DROPIGNORESNONEXISTENT
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"DROP TEMPORARY MACRO SOME_MACRO"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDropMacroNonExistentWithIfExists
parameter_list|()
throws|throws
name|Exception
block|{
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"DROP TEMPORARY MACRO IF EXISTS SOME_MACRO"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDropMacroNonExistentWithIfExistsDoNotIgnoreNonExistent
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|DROPIGNORESNONEXISTENT
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"DROP TEMPORARY MACRO IF EXISTS SOME_MACRO"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testZeroInputParamters
parameter_list|()
throws|throws
name|Exception
block|{
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"CREATE TEMPORARY MACRO FIXED_NUMBER() 1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOneInputParamters
parameter_list|()
throws|throws
name|Exception
block|{
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"CREATE TEMPORARY MACRO SIGMOID (x DOUBLE) 1.0 / (1.0 + EXP(-x))"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTwoInputParamters
parameter_list|()
throws|throws
name|Exception
block|{
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"CREATE TEMPORARY MACRO DUMB_ADD (x INT, y INT) x + y"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testThreeInputParamters
parameter_list|()
throws|throws
name|Exception
block|{
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"CREATE TEMPORARY MACRO DUMB_ADD (x INT, y INT, z INT) x + y + z"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|ParseException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testCannotUseReservedWordAsName
parameter_list|()
throws|throws
name|Exception
block|{
name|parse
argument_list|(
literal|"CREATE TEMPORARY MACRO DOUBLE (x DOUBLE) 1.0 / (1.0 + EXP(-x))"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|ParseException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testNoBody
parameter_list|()
throws|throws
name|Exception
block|{
name|parse
argument_list|(
literal|"CREATE TEMPORARY MACRO DUMB_MACRO()"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|SemanticException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testUnknownInputParameter
parameter_list|()
throws|throws
name|Exception
block|{
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"CREATE TEMPORARY MACRO BAD_MACRO (x INT, y INT) x + y + z"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|SemanticException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testOneUnusedParameterName
parameter_list|()
throws|throws
name|Exception
block|{
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"CREATE TEMPORARY MACRO BAD_MACRO (x INT, y INT) x"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|SemanticException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testTwoUnusedParameterNames
parameter_list|()
throws|throws
name|Exception
block|{
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"CREATE TEMPORARY MACRO BAD_MACRO (x INT, y INT, z INT) x"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|SemanticException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testTwoDuplicateParameterNames
parameter_list|()
throws|throws
name|Exception
block|{
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"CREATE TEMPORARY MACRO BAD_MACRO (x INT, x INT) x + x"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|SemanticException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testThreeDuplicateParameters
parameter_list|()
throws|throws
name|Exception
block|{
name|analyze
argument_list|(
name|parse
argument_list|(
literal|"CREATE TEMPORARY MACRO BAD_MACRO (x INT, x INT, x INT) x + x + x"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

