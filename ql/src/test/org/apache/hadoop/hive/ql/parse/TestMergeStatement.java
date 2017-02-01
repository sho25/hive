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
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|RewriteEmptyStreamException
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
name|Ignore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|ExpectedException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Testing parsing for SQL Merge statement  */
end_comment

begin_class
specifier|public
class|class
name|TestMergeStatement
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
name|TestIUD
operator|.
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
annotation|@
name|Rule
specifier|public
name|ExpectedException
name|expectedException
init|=
name|ExpectedException
operator|.
name|none
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
comment|//using target.a breaks this
literal|"MERGE INTO target USING source ON target.pk = source.pk WHEN MATCHED THEN UPDATE set a = source.b, c=d+1"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"(tok_merge "
operator|+
literal|"(tok_tabref (tok_tabname target)) "
operator|+
literal|"(tok_tabref (tok_tabname source)) "
operator|+
literal|"(= (. (tok_table_or_col target) pk) (. (tok_table_or_col source) pk)) "
operator|+
literal|"(tok_matched "
operator|+
literal|"(tok_update "
operator|+
literal|"(tok_set_columns_clause "
operator|+
literal|"(= (tok_table_or_col a) (. (tok_table_or_col source) b)) "
operator|+
literal|"(= (tok_table_or_col c) (+ (tok_table_or_col d) 1))"
operator|+
literal|")"
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
name|test1
parameter_list|()
throws|throws
name|ParseException
block|{
comment|//testing MATCHED AND with CASE statement
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
comment|//using target.a breaks this
literal|"MERGE INTO target USING source ON target.pk = source.pk WHEN MATCHED "
operator|+
literal|"AND source.c2< current_time() "
operator|+
literal|"THEN UPDATE set a = source.b, b = case when c1 is null then c1 else c1 end"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"(tok_merge "
operator|+
literal|"(tok_tabref (tok_tabname target)) (tok_tabref (tok_tabname source)) (= (. (tok_table_or_col target) pk) (. (tok_table_or_col source) pk)) "
operator|+
literal|"(tok_matched "
operator|+
literal|"(tok_update "
operator|+
literal|"(tok_set_columns_clause "
operator|+
literal|"(= (tok_table_or_col a) (. (tok_table_or_col source) b)) "
operator|+
literal|"(= (tok_table_or_col b) (tok_function when (tok_function tok_isnull (tok_table_or_col c1)) (tok_table_or_col c1) (tok_table_or_col c1)))"
operator|+
literal|")"
operator|+
literal|") "
operator|+
literal|"(< (. (tok_table_or_col source) c2) (tok_function current_time)))"
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
name|test2
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"MERGE INTO target USING source ON target.pk = source.pk WHEN MATCHED THEN DELETE"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"(tok_merge "
operator|+
literal|"(tok_tabref (tok_tabname target)) (tok_tabref (tok_tabname source)) (= (. (tok_table_or_col target) pk) (. (tok_table_or_col source) pk)) "
operator|+
literal|"(tok_matched "
operator|+
literal|"tok_delete)"
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
name|test3
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"MERGE INTO target USING source ON target.pk = source.pk WHEN MATCHED AND target.a + source.b> 8 THEN DELETE"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"(tok_merge "
operator|+
literal|"(tok_tabref (tok_tabname target)) (tok_tabref (tok_tabname source)) (= (. (tok_table_or_col target) pk) (. (tok_table_or_col source) pk)) "
operator|+
literal|"(tok_matched "
operator|+
literal|"tok_delete "
operator|+
literal|"(> (+ (. (tok_table_or_col target) a) (. (tok_table_or_col source) b)) 8))"
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
name|test4
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"MERGE INTO target USING source ON target.pk = source.pk WHEN NOT MATCHED THEN INSERT VALUES(source.a, case when source.b is null then target.b else source.b end)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"(tok_merge "
operator|+
literal|"(tok_tabref (tok_tabname target)) (tok_tabref (tok_tabname source)) (= (. (tok_table_or_col target) pk) (. (tok_table_or_col source) pk)) "
operator|+
literal|"(tok_not_matched "
operator|+
literal|"(tok_insert "
operator|+
literal|"(tok_value_row "
operator|+
literal|"(. (tok_table_or_col source) a) "
operator|+
literal|"(tok_function when "
operator|+
literal|"(tok_function tok_isnull (. (tok_table_or_col source) b)) (. (tok_table_or_col target) b) "
operator|+
literal|"(. (tok_table_or_col source) b)"
operator|+
literal|")"
operator|+
literal|")"
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
comment|/**    * both UPDATE and INSERT    * @throws ParseException    */
annotation|@
name|Test
specifier|public
name|void
name|test5
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"MERGE INTO target USING source ON target.pk = source.pk WHEN MATCHED THEN UPDATE set a = source.b, c=d+1 WHEN NOT MATCHED THEN INSERT VALUES(source.a, 2, current_date())"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"(tok_merge "
operator|+
literal|"(tok_tabref (tok_tabname target)) (tok_tabref (tok_tabname source)) (= (. (tok_table_or_col target) pk) (. (tok_table_or_col source) pk)) "
operator|+
literal|"(tok_matched "
operator|+
literal|"(tok_update "
operator|+
literal|"(tok_set_columns_clause (= (tok_table_or_col a) (. (tok_table_or_col source) b)) (= (tok_table_or_col c) (+ (tok_table_or_col d) 1)))"
operator|+
literal|")"
operator|+
literal|") "
operator|+
literal|"(tok_not_matched "
operator|+
literal|"(tok_insert "
operator|+
literal|"(tok_value_row "
operator|+
literal|"(. (tok_table_or_col source) a) "
operator|+
literal|"2 "
operator|+
literal|"(tok_function current_date)"
operator|+
literal|")"
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
name|testNegative
parameter_list|()
throws|throws
name|ParseException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|ParseException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"line 1:74 cannot recognize input near 'INSERT' '<EOF>' '<EOF>' in WHEN MATCHED THEN clause"
argument_list|)
expr_stmt|;
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"MERGE INTO target USING source ON target.pk = source.pk WHEN MATCHED THEN INSERT"
argument_list|)
decl_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNegative1
parameter_list|()
throws|throws
name|ParseException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|ParseException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"line 1:78 mismatched input 'DELETE' expecting INSERT near 'THEN' in WHEN NOT MATCHED clause"
argument_list|)
expr_stmt|;
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"MERGE INTO target USING source ON target.pk = source.pk WHEN NOT MATCHED THEN DELETE"
argument_list|)
decl_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test8
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"MERGE INTO target USING source ON target.pk = source.pk WHEN MATCHED AND a = 1 THEN UPDATE set a = b WHEN MATCHED THEN DELETE"
argument_list|)
decl_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test9
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"MERGE INTO target USING source ON target.pk = source.pk "
operator|+
literal|"WHEN MATCHED AND a = 1 THEN UPDATE set a = b "
operator|+
literal|"WHEN MATCHED THEN DELETE "
operator|+
literal|"WHEN NOT MATCHED AND d< e THEN INSERT VALUES(1,2)"
argument_list|)
decl_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test10
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"MERGE INTO target USING source ON target.pk = source.pk "
operator|+
literal|"WHEN MATCHED AND a = 1 THEN DELETE "
operator|+
literal|"WHEN MATCHED THEN UPDATE set a = b "
operator|+
literal|"WHEN NOT MATCHED AND d< e THEN INSERT VALUES(1,2)"
argument_list|)
decl_stmt|;
block|}
comment|/**    * we always expect 0 or 1 update/delete WHEN clause and 0 or 1 insert WHEN clause (and 1 or 2 WHEN clauses altogether)    * @throws ParseException    */
annotation|@
name|Test
specifier|public
name|void
name|testNegative3
parameter_list|()
throws|throws
name|ParseException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|ParseException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"line 1:119 cannot recognize input near 'INSERT' 'VALUES' '(' in WHEN MATCHED THEN clause"
argument_list|)
expr_stmt|;
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"MERGE INTO target USING source ON target.pk = source.pk WHEN MATCHED AND a = 1 THEN UPDATE set a = b WHEN MATCHED THEN INSERT VALUES(1,2)"
argument_list|)
decl_stmt|;
block|}
comment|/**    * here we reverse the order of WHEN MATCHED/WHEN NOT MATCHED - should we allow it?    * @throws ParseException    */
annotation|@
name|Test
specifier|public
name|void
name|testNegative4
parameter_list|()
throws|throws
name|ParseException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|ParseException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"line 1:104 missing EOF at 'WHEN' near ')'"
argument_list|)
expr_stmt|;
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"MERGE INTO target USING source ON target.pk = source.pk WHEN NOT MATCHED THEN INSERT VALUES(a,source.b) WHEN MATCHED THEN DELETE"
argument_list|)
decl_stmt|;
block|}
comment|/**    * why does this fail but next one passes    * @throws ParseException    */
annotation|@
name|Test
specifier|public
name|void
name|testNegative5
parameter_list|()
throws|throws
name|ParseException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|ParseException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"line 1:103 mismatched input '+' expecting ) near 'b' in value row constructor"
argument_list|)
expr_stmt|;
comment|//todo: why does this fail but next one passes?
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"MERGE INTO target USING source ON target.pk = source.pk WHEN NOT MATCHED THEN INSERT VALUES(a,source.b + 1)"
argument_list|)
decl_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test6
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"MERGE INTO target USING source ON target.pk = source.pk WHEN NOT MATCHED THEN INSERT VALUES(a,(source.b + 1))"
argument_list|)
decl_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNegative6
parameter_list|()
throws|throws
name|ParseException
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|RewriteEmptyStreamException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expectMessage
argument_list|(
literal|"rule whenClauses"
argument_list|)
expr_stmt|;
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"MERGE INTO target USING source ON target.pk = source.pk"
argument_list|)
decl_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test7
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"merge into acidTbl"
operator|+
literal|" using nonAcidPart2 source ON acidTbl.a = source.a2 "
operator|+
literal|"WHEN MATCHED THEN UPDATE set b = source.b2 "
operator|+
literal|"WHEN NOT MATCHED THEN INSERT VALUES(source.a2, source.b2)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|,
literal|"(tok_merge "
operator|+
literal|"(tok_tabref (tok_tabname acidtbl)) (tok_tabref (tok_tabname nonacidpart2) source) "
operator|+
literal|"(= (. (tok_table_or_col acidtbl) a) (. (tok_table_or_col source) a2)) "
operator|+
literal|"(tok_matched "
operator|+
literal|"(tok_update "
operator|+
literal|"(tok_set_columns_clause (= (tok_table_or_col b) (. (tok_table_or_col source) b2))))) "
operator|+
literal|"(tok_not_matched "
operator|+
literal|"(tok_insert "
operator|+
literal|"(tok_value_row (. (tok_table_or_col source) a2) (. (tok_table_or_col source) b2)))))"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

