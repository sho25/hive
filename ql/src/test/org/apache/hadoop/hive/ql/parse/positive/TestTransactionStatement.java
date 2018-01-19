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
operator|.
name|positive
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
name|ParseDriver
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
name|ParseException
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
name|SemanticAnalyzer
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
name|junit
operator|.
name|AfterClass
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
comment|/**  * Basic parser tests for multi-statement transactions  */
end_comment

begin_class
specifier|public
class|class
name|TestTransactionStatement
block|{
specifier|private
specifier|static
name|SessionState
name|sessionState
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
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|SemanticAnalyzer
operator|.
name|class
argument_list|)
decl_stmt|;
name|sessionState
operator|=
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|cleanUp
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|sessionState
operator|!=
literal|null
condition|)
block|{
name|sessionState
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
name|testTxnStart
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"START TRANSACTION"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"tok_start_transaction"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
name|ast
operator|=
name|parse
argument_list|(
literal|"START TRANSACTION ISOLATION LEVEL SNAPSHOT"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(tok_start_transaction (tok_isolation_level tok_isolation_snapshot))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
name|ast
operator|=
name|parse
argument_list|(
literal|"START TRANSACTION READ ONLY"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(tok_start_transaction (tok_txn_access_mode tok_txn_read_only))"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
name|ast
operator|=
name|parse
argument_list|(
literal|"START TRANSACTION READ WRITE, ISOLATION LEVEL SNAPSHOT"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(tok_start_transaction (tok_txn_access_mode tok_txn_read_write) (tok_isolation_level tok_isolation_snapshot))"
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
name|testTxnCommitRollback
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"COMMIT"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"tok_commit"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
name|ast
operator|=
name|parse
argument_list|(
literal|"COMMIT WORK"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"tok_commit"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
name|ast
operator|=
name|parse
argument_list|(
literal|"ROLLBACK"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"tok_rollback"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
name|ast
operator|=
name|parse
argument_list|(
literal|"ROLLBACK WORK"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"tok_rollback"
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
name|testAutoCommit
parameter_list|()
throws|throws
name|ParseException
block|{
name|ASTNode
name|ast
init|=
name|parse
argument_list|(
literal|"SET AUTOCOMMIT TRUE"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(tok_set_autocommit tok_true)"
argument_list|,
name|ast
operator|.
name|toStringTree
argument_list|()
argument_list|)
expr_stmt|;
name|ast
operator|=
name|parse
argument_list|(
literal|"SET AUTOCOMMIT FALSE"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"AST doesn't match"
argument_list|,
literal|"(tok_set_autocommit tok_false)"
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

