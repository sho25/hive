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
name|junit
operator|.
name|Assert
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
name|BeforeClass
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

begin_comment
comment|/**  * various Parser tests for INSERT/UPDATE/DELETE  */
end_comment

begin_class
specifier|public
class|class
name|TestIUD
block|{
specifier|private
specifier|static
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|ParseDriver
name|pd
decl_stmt|;
specifier|private
name|SemanticAnalyzer
name|sA
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|initialize
parameter_list|()
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|SemanticAnalyzer
operator|.
name|class
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|SemanticException
block|{
name|pd
operator|=
operator|new
name|ParseDriver
argument_list|()
expr_stmt|;
name|sA
operator|=
operator|new
name|SemanticAnalyzer
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|ASTNode
name|parse
parameter_list|(
name|String
name|query
parameter_list|)
throws|throws
name|ParseException
block|{
name|ASTNode
name|nd
init|=
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|)
decl_stmt|;
return|return
operator|(
name|ASTNode
operator|)
name|nd
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteNoWhere
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"DELETE FROM src"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(TOK_DELETE_FROM "
operator|+
literal|"(TOK_TABNAME src))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteWithWhere
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"DELETE FROM src WHERE key IS NOT NULL AND src.value< 0"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(TOK_DELETE_FROM "
operator|+
literal|"(TOK_TABNAME src) "
operator|+
literal|"(TOK_WHERE "
operator|+
literal|"(AND "
operator|+
literal|"(TOK_FUNCTION TOK_ISNOTNULL (TOK_TABLE_OR_COL key)) "
operator|+
literal|"(< (. (TOK_TABLE_OR_COL src) value) 0))))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateNoWhereSingleSet
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"UPDATE src set key = 3"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(TOK_UPDATE_TABLE "
operator|+
literal|"(TOK_TABNAME src) "
operator|+
literal|"(TOK_SET_COLUMNS_CLAUSE "
operator|+
literal|"(= "
operator|+
literal|"(TOK_TABLE_OR_COL key) 3)))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateNoWhereMultiSet
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"UPDATE src set key = 3, value = 8"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(TOK_UPDATE_TABLE "
operator|+
literal|"(TOK_TABNAME src) "
operator|+
literal|"(TOK_SET_COLUMNS_CLAUSE "
operator|+
literal|"(= "
operator|+
literal|"(TOK_TABLE_OR_COL key) 3) "
operator|+
literal|"(= "
operator|+
literal|"(TOK_TABLE_OR_COL value) 8)))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateWithWhereSingleSet
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"UPDATE src SET key = 3 WHERE value IS NULL"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(TOK_UPDATE_TABLE "
operator|+
literal|"(TOK_TABNAME src) "
operator|+
literal|"(TOK_SET_COLUMNS_CLAUSE "
operator|+
literal|"(= "
operator|+
literal|"(TOK_TABLE_OR_COL key) 3)) "
operator|+
literal|"(TOK_WHERE (TOK_FUNCTION TOK_ISNULL (TOK_TABLE_OR_COL value))))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateWithWhereSingleSetExpr
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"UPDATE src SET key = -3+(5*9)%8, val = cast(6.1 + c as INT), d = d - 1 WHERE value IS NULL"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(TOK_UPDATE_TABLE (TOK_TABNAME src) "
operator|+
literal|"(TOK_SET_COLUMNS_CLAUSE "
operator|+
literal|"(= (TOK_TABLE_OR_COL key) (+ (- 3) (% (* 5 9) 8))) "
operator|+
literal|"(= (TOK_TABLE_OR_COL val) (TOK_FUNCTION TOK_INT (+ 6.1 (TOK_TABLE_OR_COL c)))) "
operator|+
literal|"(= (TOK_TABLE_OR_COL d) (- (TOK_TABLE_OR_COL d) 1))) "
operator|+
literal|"(TOK_WHERE (TOK_FUNCTION TOK_ISNULL (TOK_TABLE_OR_COL value))))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateWithWhereMultiSet
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"UPDATE src SET key = 3, value = 8 WHERE VALUE = 1230997"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(TOK_UPDATE_TABLE "
operator|+
literal|"(TOK_TABNAME src) "
operator|+
literal|"(TOK_SET_COLUMNS_CLAUSE "
operator|+
literal|"(= "
operator|+
literal|"(TOK_TABLE_OR_COL key) 3) "
operator|+
literal|"(= "
operator|+
literal|"(TOK_TABLE_OR_COL value) 8)) "
operator|+
literal|"(TOK_WHERE (= (TOK_TABLE_OR_COL VALUE) 1230997)))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStandardInsertIntoTable
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"INSERT into TABLE page_view SELECT pvs.viewTime, pvs.userid from page_view_stg pvs where pvs.userid is null"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(TOK_QUERY "
operator|+
literal|"(TOK_FROM "
operator|+
literal|"(TOK_TABREF (TOK_TABNAME page_view_stg) pvs)) "
operator|+
literal|"(TOK_INSERT (TOK_INSERT_INTO (TOK_TAB (TOK_TABNAME page_view))) "
operator|+
literal|"(TOK_SELECT "
operator|+
literal|"(TOK_SELEXPR (. (TOK_TABLE_OR_COL pvs) viewTime)) "
operator|+
literal|"(TOK_SELEXPR (. (TOK_TABLE_OR_COL pvs) userid))) "
operator|+
literal|"(TOK_WHERE (TOK_FUNCTION TOK_ISNULL (. (TOK_TABLE_OR_COL pvs) userid)))))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSelectStarFromAnonymousVirtTable1Row
parameter_list|()
throws|throws
name|ParseException
block|{
try|try
block|{
name|parse
argument_list|(
literal|"select * from values (3,4)"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
literal|"Expected ParseException"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|ex
parameter_list|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Failure didn't match."
argument_list|,
literal|"line 1:21 missing EOF at '(' near 'values'"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSelectStarFromVirtTable1Row
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"select * from (values (3,4)) as VC(a,b)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(TOK_QUERY "
operator|+
literal|"(TOK_FROM "
operator|+
literal|"(TOK_VIRTUAL_TABLE "
operator|+
literal|"(TOK_VIRTUAL_TABREF (TOK_TABNAME VC) (TOK_COL_NAME a b)) "
operator|+
literal|"(TOK_VALUES_TABLE (TOK_VALUE_ROW 3 4)))) "
operator|+
literal|"(TOK_INSERT (TOK_DESTINATION (TOK_DIR TOK_TMP_FILE)) (TOK_SELECT (TOK_SELEXPR TOK_ALLCOLREF))))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSelectStarFromVirtTable2Row
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"select * from (values (1,2),(3,4)) as VC(a,b)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(TOK_QUERY "
operator|+
literal|"(TOK_FROM "
operator|+
literal|"(TOK_VIRTUAL_TABLE "
operator|+
literal|"(TOK_VIRTUAL_TABREF (TOK_TABNAME VC) (TOK_COL_NAME a b)) "
operator|+
literal|"(TOK_VALUES_TABLE (TOK_VALUE_ROW 1 2) (TOK_VALUE_ROW 3 4)))) "
operator|+
literal|"(TOK_INSERT (TOK_DESTINATION (TOK_DIR TOK_TMP_FILE)) (TOK_SELECT (TOK_SELEXPR TOK_ALLCOLREF))))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSelectStarFromVirtTable2RowNamedProjections
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"select a as c, b as d from (values (1,2),(3,4)) as VC(a,b)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(TOK_QUERY "
operator|+
literal|"(TOK_FROM "
operator|+
literal|"(TOK_VIRTUAL_TABLE "
operator|+
literal|"(TOK_VIRTUAL_TABREF (TOK_TABNAME VC) (TOK_COL_NAME a b)) "
operator|+
literal|"(TOK_VALUES_TABLE (TOK_VALUE_ROW 1 2) (TOK_VALUE_ROW 3 4)))) "
operator|+
literal|"(TOK_INSERT (TOK_DESTINATION (TOK_DIR TOK_TMP_FILE)) "
operator|+
literal|"(TOK_SELECT (TOK_SELEXPR (TOK_TABLE_OR_COL a) c) (TOK_SELEXPR (TOK_TABLE_OR_COL b) d))))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInsertIntoTableAsSelectFromNamedVirtTable
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"insert into table page_view select a,b as c from (values (1,2),(3,4)) as VC(a,b) where b = 9"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(TOK_QUERY "
operator|+
literal|"(TOK_FROM "
operator|+
literal|"(TOK_VIRTUAL_TABLE "
operator|+
literal|"(TOK_VIRTUAL_TABREF (TOK_TABNAME VC) (TOK_COL_NAME a b)) "
operator|+
literal|"(TOK_VALUES_TABLE (TOK_VALUE_ROW 1 2) (TOK_VALUE_ROW 3 4)))) "
operator|+
literal|"(TOK_INSERT (TOK_INSERT_INTO (TOK_TAB (TOK_TABNAME page_view))) "
operator|+
literal|"(TOK_SELECT "
operator|+
literal|"(TOK_SELEXPR (TOK_TABLE_OR_COL a)) "
operator|+
literal|"(TOK_SELEXPR (TOK_TABLE_OR_COL b) c)) "
operator|+
literal|"(TOK_WHERE (= (TOK_TABLE_OR_COL b) 9))))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInsertIntoTableFromAnonymousTable1Row
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"insert into table page_view values(1,2)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(TOK_QUERY "
operator|+
literal|"(TOK_FROM "
operator|+
literal|"(TOK_VIRTUAL_TABLE "
operator|+
literal|"(TOK_VIRTUAL_TABREF TOK_ANONYMOUS) "
operator|+
literal|"(TOK_VALUES_TABLE (TOK_VALUE_ROW 1 2)))) "
operator|+
literal|"(TOK_INSERT (TOK_INSERT_INTO (TOK_TAB (TOK_TABNAME page_view))) "
operator|+
literal|"(TOK_SELECT (TOK_SELEXPR TOK_ALLCOLREF))))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInsertIntoTableFromAnonymousTable
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"insert into table page_view values(-1,2),(3,+4)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(TOK_QUERY "
operator|+
literal|"(TOK_FROM "
operator|+
literal|"(TOK_VIRTUAL_TABLE "
operator|+
literal|"(TOK_VIRTUAL_TABREF TOK_ANONYMOUS) "
operator|+
literal|"(TOK_VALUES_TABLE (TOK_VALUE_ROW (- 1) 2) (TOK_VALUE_ROW 3 (+ 4))))) "
operator|+
literal|"(TOK_INSERT (TOK_INSERT_INTO (TOK_TAB (TOK_TABNAME page_view))) "
operator|+
literal|"(TOK_SELECT (TOK_SELEXPR TOK_ALLCOLREF))))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

