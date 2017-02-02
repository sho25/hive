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
name|IOException
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
throws|,
name|IOException
block|{
name|pd
operator|=
operator|new
name|ParseDriver
argument_list|()
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
return|return
name|parse
argument_list|(
name|query
argument_list|,
name|pd
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|static
name|ASTNode
name|parse
parameter_list|(
name|String
name|query
parameter_list|,
name|ParseDriver
name|pd
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|ParseException
block|{
name|ASTNode
name|nd
init|=
literal|null
decl_stmt|;
try|try
block|{
name|nd
operator|=
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|,
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
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
literal|"(tok_delete_from "
operator|+
literal|"(tok_tabname src))"
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
literal|"(tok_delete_from "
operator|+
literal|"(tok_tabname src) "
operator|+
literal|"(tok_where "
operator|+
literal|"(and "
operator|+
literal|"(tok_function tok_isnotnull (tok_table_or_col key)) "
operator|+
literal|"(< (. (tok_table_or_col src) value) 0))))"
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
literal|"(tok_update_table "
operator|+
literal|"(tok_tabname src) "
operator|+
literal|"(tok_set_columns_clause "
operator|+
literal|"(= "
operator|+
literal|"(tok_table_or_col key) 3)))"
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
literal|"(tok_update_table "
operator|+
literal|"(tok_tabname src) "
operator|+
literal|"(tok_set_columns_clause "
operator|+
literal|"(= "
operator|+
literal|"(tok_table_or_col key) 3) "
operator|+
literal|"(= "
operator|+
literal|"(tok_table_or_col value) 8)))"
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
literal|"(tok_update_table "
operator|+
literal|"(tok_tabname src) "
operator|+
literal|"(tok_set_columns_clause "
operator|+
literal|"(= "
operator|+
literal|"(tok_table_or_col key) 3)) "
operator|+
literal|"(tok_where (tok_function tok_isnull (tok_table_or_col value))))"
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
literal|"(tok_update_table (tok_tabname src) "
operator|+
literal|"(tok_set_columns_clause "
operator|+
literal|"(= (tok_table_or_col key) (+ (- 3) (% (* 5 9) 8))) "
operator|+
literal|"(= (tok_table_or_col val) (tok_function tok_int (+ 6.1 (tok_table_or_col c)))) "
operator|+
literal|"(= (tok_table_or_col d) (- (tok_table_or_col d) 1))) "
operator|+
literal|"(tok_where (tok_function tok_isnull (tok_table_or_col value))))"
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
literal|"(tok_update_table "
operator|+
literal|"(tok_tabname src) "
operator|+
literal|"(tok_set_columns_clause "
operator|+
literal|"(= "
operator|+
literal|"(tok_table_or_col key) 3) "
operator|+
literal|"(= "
operator|+
literal|"(tok_table_or_col value) 8)) "
operator|+
literal|"(tok_where (= (tok_table_or_col value) 1230997)))"
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
literal|"(tok_query "
operator|+
literal|"(tok_from "
operator|+
literal|"(tok_tabref (tok_tabname page_view_stg) pvs)) "
operator|+
literal|"(tok_insert (tok_insert_into (tok_tab (tok_tabname page_view))) "
operator|+
literal|"(tok_select "
operator|+
literal|"(tok_selexpr (. (tok_table_or_col pvs) viewtime)) "
operator|+
literal|"(tok_selexpr (. (tok_table_or_col pvs) userid))) "
operator|+
literal|"(tok_where (tok_function tok_isnull (. (tok_table_or_col pvs) userid)))))"
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
literal|"select * from `values` (3,4)"
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
literal|"line 1:24 cannot recognize input near 'values' '(' '3' in joinSource"
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
literal|"select * from (values (3,4)) as vc(a,b)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(tok_query "
operator|+
literal|"(tok_from "
operator|+
literal|"(tok_virtual_table "
operator|+
literal|"(tok_virtual_tabref (tok_tabname vc) (tok_col_name a b)) "
operator|+
literal|"(tok_values_table (tok_value_row 3 4)))) "
operator|+
literal|"(tok_insert (tok_destination (tok_dir tok_tmp_file)) (tok_select (tok_selexpr tok_allcolref))))"
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
literal|"select * from (values (1,2),(3,4)) as vc(a,b)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(tok_query "
operator|+
literal|"(tok_from "
operator|+
literal|"(tok_virtual_table "
operator|+
literal|"(tok_virtual_tabref (tok_tabname vc) (tok_col_name a b)) "
operator|+
literal|"(tok_values_table (tok_value_row 1 2) (tok_value_row 3 4)))) "
operator|+
literal|"(tok_insert (tok_destination (tok_dir tok_tmp_file)) (tok_select (tok_selexpr tok_allcolref))))"
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
literal|"select a as c, b as d from (values (1,2),(3,4)) as vc(a,b)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(tok_query "
operator|+
literal|"(tok_from "
operator|+
literal|"(tok_virtual_table "
operator|+
literal|"(tok_virtual_tabref (tok_tabname vc) (tok_col_name a b)) "
operator|+
literal|"(tok_values_table (tok_value_row 1 2) (tok_value_row 3 4)))) "
operator|+
literal|"(tok_insert (tok_destination (tok_dir tok_tmp_file)) "
operator|+
literal|"(tok_select (tok_selexpr (tok_table_or_col a) c) (tok_selexpr (tok_table_or_col b) d))))"
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
literal|"insert into page_view select a,b as c from (values (1,2),(3,4)) as vc(a,b) where b = 9"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(tok_query "
operator|+
literal|"(tok_from "
operator|+
literal|"(tok_virtual_table "
operator|+
literal|"(tok_virtual_tabref (tok_tabname vc) (tok_col_name a b)) "
operator|+
literal|"(tok_values_table (tok_value_row 1 2) (tok_value_row 3 4)))) "
operator|+
literal|"(tok_insert (tok_insert_into (tok_tab (tok_tabname page_view))) "
operator|+
literal|"(tok_select "
operator|+
literal|"(tok_selexpr (tok_table_or_col a)) "
operator|+
literal|"(tok_selexpr (tok_table_or_col b) c)) "
operator|+
literal|"(tok_where (= (tok_table_or_col b) 9))))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * same as testInsertIntoTableAsSelectFromNamedVirtTable but with column list on target table    * @throws ParseException    */
annotation|@
name|Test
specifier|public
name|void
name|testInsertIntoTableAsSelectFromNamedVirtTableNamedCol
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"insert into page_view(c1,c2) select a,b as c from (values (1,2),(3,4)) as vc(a,b) where b = 9"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(tok_query "
operator|+
literal|"(tok_from "
operator|+
literal|"(tok_virtual_table "
operator|+
literal|"(tok_virtual_tabref (tok_tabname vc) (tok_col_name a b)) "
operator|+
literal|"(tok_values_table (tok_value_row 1 2) (tok_value_row 3 4)))) "
operator|+
literal|"(tok_insert (tok_insert_into (tok_tab (tok_tabname page_view)) (tok_tabcolname c1 c2)) "
operator|+
literal|"(tok_select "
operator|+
literal|"(tok_selexpr (tok_table_or_col a)) "
operator|+
literal|"(tok_selexpr (tok_table_or_col b) c)) "
operator|+
literal|"(tok_where (= (tok_table_or_col b) 9))))"
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
literal|"insert into page_view values(1,2)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(tok_query "
operator|+
literal|"(tok_from "
operator|+
literal|"(tok_virtual_table "
operator|+
literal|"(tok_virtual_tabref tok_anonymous) "
operator|+
literal|"(tok_values_table (tok_value_row 1 2)))) "
operator|+
literal|"(tok_insert (tok_insert_into (tok_tab (tok_tabname page_view))) "
operator|+
literal|"(tok_select (tok_selexpr tok_allcolref))))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Same as testInsertIntoTableFromAnonymousTable1Row but with column list on target table    * @throws ParseException    */
annotation|@
name|Test
specifier|public
name|void
name|testInsertIntoTableFromAnonymousTable1RowNamedCol
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"insert into page_view(a,b) values(1,2)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(tok_query "
operator|+
literal|"(tok_from "
operator|+
literal|"(tok_virtual_table "
operator|+
literal|"(tok_virtual_tabref tok_anonymous) "
operator|+
literal|"(tok_values_table (tok_value_row 1 2))"
operator|+
literal|")"
operator|+
literal|") "
operator|+
literal|"(tok_insert "
operator|+
literal|"(tok_insert_into "
operator|+
literal|"(tok_tab (tok_tabname page_view)) "
operator|+
literal|"(tok_tabcolname a b)"
operator|+
comment|//this is "extra" piece we get vs previous query
literal|") "
operator|+
literal|"(tok_select "
operator|+
literal|"(tok_selexpr tok_allcolref)"
operator|+
literal|")"
operator|+
literal|")"
operator|+
literal|")"
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
literal|"(tok_query "
operator|+
literal|"(tok_from "
operator|+
literal|"(tok_virtual_table "
operator|+
literal|"(tok_virtual_tabref tok_anonymous) "
operator|+
literal|"(tok_values_table (tok_value_row (- 1) 2) (tok_value_row 3 (+ 4))))) "
operator|+
literal|"(tok_insert (tok_insert_into (tok_tab (tok_tabname page_view))) "
operator|+
literal|"(tok_select (tok_selexpr tok_allcolref))))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
comment|//same query as above less the "table" keyword KW_table
name|ast
operator|=
name|parse
argument_list|(
literal|"insert into page_view values(-1,2),(3,+4)"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(tok_query "
operator|+
literal|"(tok_from "
operator|+
literal|"(tok_virtual_table "
operator|+
literal|"(tok_virtual_tabref tok_anonymous) "
operator|+
literal|"(tok_values_table (tok_value_row (- 1) 2) (tok_value_row 3 (+ 4))))) "
operator|+
literal|"(tok_insert (tok_insert_into (tok_tab (tok_tabname page_view))) "
operator|+
literal|"(tok_select (tok_selexpr tok_allcolref))))"
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
name|testMultiInsert
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"from S insert into T1 select a, b insert into T2 select c, d"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(tok_query (tok_from (tok_tabref (tok_tabname s))) "
operator|+
literal|"(tok_insert (tok_insert_into (tok_tab (tok_tabname t1))) (tok_select (tok_selexpr (tok_table_or_col a)) (tok_selexpr (tok_table_or_col b)))) "
operator|+
literal|"(tok_insert (tok_insert_into (tok_tab (tok_tabname t2))) (tok_select (tok_selexpr (tok_table_or_col c)) (tok_selexpr (tok_table_or_col d)))))"
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

