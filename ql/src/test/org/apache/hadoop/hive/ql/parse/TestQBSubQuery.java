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
name|util
operator|.
name|ArrayList
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

begin_class
specifier|public
class|class
name|TestQBSubQuery
block|{
specifier|static
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|static
name|String
name|IN_QUERY
init|=
literal|" select * "
operator|+
literal|"from src "
operator|+
literal|"where src.key in (select key from src s1 where s1.key> '9' and s1.value> '9') "
decl_stmt|;
specifier|private
specifier|static
name|String
name|IN_QUERY2
init|=
literal|" select * "
operator|+
literal|"from src "
operator|+
literal|"where src.key in (select key from src s1 where s1.key> '9' and s1.value> '9') and value> '9'"
decl_stmt|;
specifier|private
specifier|static
name|String
name|QUERY3
init|=
literal|"select p_mfgr, min(p_size), rank() over(partition by p_mfgr) as r from part group by p_mfgr"
decl_stmt|;
name|ParseDriver
name|pd
decl_stmt|;
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
name|testExtractSubQueries
parameter_list|()
throws|throws
name|Exception
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
name|IN_QUERY
argument_list|)
decl_stmt|;
name|ASTNode
name|where
init|=
name|where
argument_list|(
name|ast
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ASTNode
argument_list|>
name|sqs
init|=
name|SubQueryUtils
operator|.
name|findSubQueries
argument_list|(
operator|(
name|ASTNode
operator|)
name|where
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|sqs
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|ASTNode
name|sq
init|=
name|sqs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|sq
operator|.
name|toStringTree
argument_list|()
argument_list|,
literal|"(TOK_SUBQUERY_EXPR (TOK_SUBQUERY_OP in) (TOK_QUERY (TOK_FROM (TOK_TABREF (TOK_TABNAME src) s1)) (TOK_INSERT (TOK_DESTINATION (TOK_DIR TOK_TMP_FILE)) (TOK_SELECT (TOK_SELEXPR (TOK_TABLE_OR_COL key))) (TOK_WHERE (and (> (. (TOK_TABLE_OR_COL s1) key) '9') (> (. (TOK_TABLE_OR_COL s1) value) '9'))))) (. (TOK_TABLE_OR_COL src) key))"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExtractConjuncts
parameter_list|()
throws|throws
name|Exception
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
name|IN_QUERY
argument_list|)
decl_stmt|;
name|ASTNode
name|where
init|=
name|where
argument_list|(
name|ast
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ASTNode
argument_list|>
name|sqs
init|=
name|SubQueryUtils
operator|.
name|findSubQueries
argument_list|(
operator|(
name|ASTNode
operator|)
name|where
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|ASTNode
name|sq
init|=
name|sqs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ASTNode
name|sqWhere
init|=
name|where
argument_list|(
operator|(
name|ASTNode
operator|)
name|sq
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ASTNode
argument_list|>
name|conjuncts
init|=
operator|new
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|()
decl_stmt|;
name|SubQueryUtils
operator|.
name|extractConjuncts
argument_list|(
operator|(
name|ASTNode
operator|)
name|sqWhere
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|,
name|conjuncts
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|conjuncts
operator|.
name|size
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|conjuncts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toStringTree
argument_list|()
argument_list|,
literal|"(> (. (TOK_TABLE_OR_COL s1) key) '9')"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|conjuncts
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|toStringTree
argument_list|()
argument_list|,
literal|"(> (. (TOK_TABLE_OR_COL s1) value) '9')"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRewriteOuterQueryWhere
parameter_list|()
throws|throws
name|Exception
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
name|IN_QUERY
argument_list|)
decl_stmt|;
name|ASTNode
name|where
init|=
name|where
argument_list|(
name|ast
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ASTNode
argument_list|>
name|sqs
init|=
name|SubQueryUtils
operator|.
name|findSubQueries
argument_list|(
operator|(
name|ASTNode
operator|)
name|where
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|ASTNode
name|sq
init|=
name|sqs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ASTNode
name|newWhere
init|=
name|SubQueryUtils
operator|.
name|rewriteParentQueryWhere
argument_list|(
operator|(
name|ASTNode
operator|)
name|where
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|,
name|sq
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|newWhere
operator|.
name|toStringTree
argument_list|()
argument_list|,
literal|"(= 1 1)"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRewriteOuterQueryWhere2
parameter_list|()
throws|throws
name|Exception
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
name|IN_QUERY2
argument_list|)
decl_stmt|;
name|ASTNode
name|where
init|=
name|where
argument_list|(
name|ast
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ASTNode
argument_list|>
name|sqs
init|=
name|SubQueryUtils
operator|.
name|findSubQueries
argument_list|(
operator|(
name|ASTNode
operator|)
name|where
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|ASTNode
name|sq
init|=
name|sqs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ASTNode
name|newWhere
init|=
name|SubQueryUtils
operator|.
name|rewriteParentQueryWhere
argument_list|(
operator|(
name|ASTNode
operator|)
name|where
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|,
name|sq
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|newWhere
operator|.
name|toStringTree
argument_list|()
argument_list|,
literal|"(> (TOK_TABLE_OR_COL value) '9')"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCheckAggOrWindowing
parameter_list|()
throws|throws
name|Exception
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
name|QUERY3
argument_list|)
decl_stmt|;
name|ASTNode
name|select
init|=
name|select
argument_list|(
name|ast
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|SubQueryUtils
operator|.
name|checkAggOrWindowing
argument_list|(
operator|(
name|ASTNode
operator|)
name|select
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|SubQueryUtils
operator|.
name|checkAggOrWindowing
argument_list|(
operator|(
name|ASTNode
operator|)
name|select
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|SubQueryUtils
operator|.
name|checkAggOrWindowing
argument_list|(
operator|(
name|ASTNode
operator|)
name|select
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ASTNode
name|where
parameter_list|(
name|ASTNode
name|qry
parameter_list|)
block|{
return|return
operator|(
name|ASTNode
operator|)
name|qry
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
return|;
block|}
specifier|private
name|ASTNode
name|select
parameter_list|(
name|ASTNode
name|qry
parameter_list|)
block|{
return|return
operator|(
name|ASTNode
operator|)
name|qry
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
return|;
block|}
block|}
end_class

end_unit

