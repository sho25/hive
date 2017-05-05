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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|hasItems
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|is
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|FetchTask
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
name|io
operator|.
name|orc
operator|.
name|OrcInputFormat
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
name|io
operator|.
name|orc
operator|.
name|OrcOutputFormat
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
name|metadata
operator|.
name|Hive
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
name|metadata
operator|.
name|HiveException
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
name|metadata
operator|.
name|Table
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
name|TestReplicationSemanticAnalyzer
block|{
specifier|static
name|QueryState
name|queryState
decl_stmt|;
specifier|static
name|HiveConf
name|conf
decl_stmt|;
specifier|static
name|String
name|defaultDB
init|=
literal|"default"
decl_stmt|;
specifier|static
name|String
name|tblName
init|=
literal|"testReplSA"
decl_stmt|;
specifier|static
name|ArrayList
argument_list|<
name|String
argument_list|>
name|cols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col1"
argument_list|,
literal|"col2"
argument_list|)
argument_list|)
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
throws|throws
name|HiveException
block|{
name|queryState
operator|=
operator|new
name|QueryState
argument_list|(
operator|new
name|HiveConf
argument_list|(
name|SemanticAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|=
name|queryState
operator|.
name|getConf
argument_list|()
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hive.security.authorization.manager"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Hive
name|hiveDb
init|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|hiveDb
operator|.
name|createTable
argument_list|(
name|defaultDB
operator|+
literal|"."
operator|+
name|tblName
argument_list|,
name|cols
argument_list|,
literal|null
argument_list|,
name|OrcInputFormat
operator|.
name|class
argument_list|,
name|OrcOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|Table
name|t
init|=
name|hiveDb
operator|.
name|getTable
argument_list|(
name|tblName
argument_list|)
decl_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|teardown
parameter_list|()
throws|throws
name|HiveException
block|{   }
annotation|@
name|Test
specifier|public
name|void
name|testReplDumpParse
parameter_list|()
throws|throws
name|Exception
block|{
name|ParseDriver
name|pd
init|=
operator|new
name|ParseDriver
argument_list|()
decl_stmt|;
name|String
name|fromEventId
init|=
literal|"100"
decl_stmt|;
name|String
name|toEventId
init|=
literal|"200"
decl_stmt|;
name|String
name|maxEventLimit
init|=
literal|"50"
decl_stmt|;
name|ASTNode
name|root
decl_stmt|;
name|ASTNode
name|child
decl_stmt|;
name|String
name|query
init|=
literal|"repl dump "
operator|+
name|defaultDB
decl_stmt|;
name|root
operator|=
operator|(
name|ASTNode
operator|)
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getText
argument_list|()
argument_list|,
literal|"TOK_REPL_DUMP"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|defaultDB
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|query
operator|=
literal|"repl dump "
operator|+
name|defaultDB
operator|+
literal|"."
operator|+
name|tblName
expr_stmt|;
name|root
operator|=
operator|(
name|ASTNode
operator|)
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|defaultDB
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|query
operator|=
literal|"repl dump "
operator|+
name|defaultDB
operator|+
literal|"."
operator|+
name|tblName
operator|+
literal|" from "
operator|+
name|fromEventId
expr_stmt|;
name|root
operator|=
operator|(
name|ASTNode
operator|)
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|defaultDB
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|root
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getText
argument_list|()
argument_list|,
literal|"TOK_FROM"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|fromEventId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|query
operator|=
literal|"repl dump "
operator|+
name|defaultDB
operator|+
literal|"."
operator|+
name|tblName
operator|+
literal|" from "
operator|+
name|fromEventId
operator|+
literal|" to "
operator|+
name|toEventId
expr_stmt|;
name|root
operator|=
operator|(
name|ASTNode
operator|)
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|defaultDB
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|root
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getText
argument_list|()
argument_list|,
literal|"TOK_FROM"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|fromEventId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
literal|"TOK_TO"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|toEventId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|query
operator|=
literal|"repl dump "
operator|+
name|defaultDB
operator|+
literal|"."
operator|+
name|tblName
operator|+
literal|" from "
operator|+
name|fromEventId
operator|+
literal|" to "
operator|+
name|toEventId
operator|+
literal|" limit "
operator|+
name|maxEventLimit
expr_stmt|;
name|root
operator|=
operator|(
name|ASTNode
operator|)
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|defaultDB
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|root
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getText
argument_list|()
argument_list|,
literal|"TOK_FROM"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|fromEventId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
literal|"TOK_TO"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|toEventId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
literal|"TOK_LIMIT"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|maxEventLimit
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReplLoadParse
parameter_list|()
throws|throws
name|Exception
block|{
comment|// FileSystem fs = FileSystem.get(conf);
name|ParseDriver
name|pd
init|=
operator|new
name|ParseDriver
argument_list|()
decl_stmt|;
name|ASTNode
name|root
decl_stmt|;
name|ASTNode
name|child
decl_stmt|;
name|String
name|replRoot
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPLDIR
argument_list|)
decl_stmt|;
name|Path
name|dumpRoot
init|=
operator|new
name|Path
argument_list|(
name|replRoot
argument_list|,
literal|"next"
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|replRoot
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|dumpRoot
argument_list|)
expr_stmt|;
name|String
name|newDB
init|=
literal|"default_bak"
decl_stmt|;
name|String
name|query
init|=
literal|"repl load  from '"
operator|+
name|dumpRoot
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
decl_stmt|;
name|root
operator|=
operator|(
name|ASTNode
operator|)
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getText
argument_list|()
argument_list|,
literal|"TOK_REPL_LOAD"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
literal|"'"
operator|+
name|dumpRoot
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|query
operator|=
literal|"repl load "
operator|+
name|newDB
operator|+
literal|" from '"
operator|+
name|dumpRoot
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
expr_stmt|;
name|root
operator|=
operator|(
name|ASTNode
operator|)
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getText
argument_list|()
argument_list|,
literal|"TOK_REPL_LOAD"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|root
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
literal|"'"
operator|+
name|dumpRoot
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|child
operator|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getText
argument_list|()
argument_list|,
name|newDB
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|child
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// TODO: add this test after repl dump analyze generates tasks
comment|//@Test
specifier|public
name|void
name|testReplDumpAnalyze
parameter_list|()
throws|throws
name|Exception
block|{    }
comment|//@Test
specifier|public
name|void
name|testReplLoadAnalyze
parameter_list|()
throws|throws
name|Exception
block|{
name|ParseDriver
name|pd
init|=
operator|new
name|ParseDriver
argument_list|()
decl_stmt|;
name|ASTNode
name|root
decl_stmt|;
name|String
name|replRoot
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPLDIR
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|dumpRoot
init|=
operator|new
name|Path
argument_list|(
name|replRoot
argument_list|,
literal|"next"
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|replRoot
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|dumpRoot
argument_list|)
expr_stmt|;
name|String
name|newDB
init|=
literal|"default_bak"
decl_stmt|;
comment|// First create a dump
name|String
name|query
init|=
literal|"repl dump "
operator|+
name|defaultDB
decl_stmt|;
name|root
operator|=
operator|(
name|ASTNode
operator|)
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|ReplicationSemanticAnalyzer
name|rs
init|=
operator|(
name|ReplicationSemanticAnalyzer
operator|)
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
name|queryState
argument_list|,
name|root
argument_list|)
decl_stmt|;
name|rs
operator|.
name|analyze
argument_list|(
name|root
argument_list|,
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
comment|// Then analyze load
name|query
operator|=
literal|"repl load  from '"
operator|+
name|dumpRoot
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
expr_stmt|;
name|root
operator|=
operator|(
name|ASTNode
operator|)
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|rs
operator|=
operator|(
name|ReplicationSemanticAnalyzer
operator|)
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
name|queryState
argument_list|,
name|root
argument_list|)
expr_stmt|;
name|rs
operator|.
name|analyze
argument_list|(
name|root
argument_list|,
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
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
name|roots
init|=
name|rs
operator|.
name|getRootTasks
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|roots
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|query
operator|=
literal|"repl load "
operator|+
name|newDB
operator|+
literal|" from '"
operator|+
name|dumpRoot
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
expr_stmt|;
name|root
operator|=
operator|(
name|ASTNode
operator|)
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|rs
operator|=
operator|(
name|ReplicationSemanticAnalyzer
operator|)
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
name|queryState
argument_list|,
name|root
argument_list|)
expr_stmt|;
name|rs
operator|.
name|analyze
argument_list|(
name|root
argument_list|,
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|roots
operator|=
name|rs
operator|.
name|getRootTasks
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|roots
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReplStatusAnalyze
parameter_list|()
throws|throws
name|Exception
block|{
name|ParseDriver
name|pd
init|=
operator|new
name|ParseDriver
argument_list|()
decl_stmt|;
name|ASTNode
name|root
decl_stmt|;
comment|// Repl status command
name|String
name|query
init|=
literal|"repl status "
operator|+
name|defaultDB
decl_stmt|;
name|root
operator|=
operator|(
name|ASTNode
operator|)
name|pd
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|ReplicationSemanticAnalyzer
name|rs
init|=
operator|(
name|ReplicationSemanticAnalyzer
operator|)
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
name|queryState
argument_list|,
name|root
argument_list|)
decl_stmt|;
name|rs
operator|.
name|analyze
argument_list|(
name|root
argument_list|,
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|FetchTask
name|fetchTask
init|=
name|rs
operator|.
name|getFetchTask
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|fetchTask
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|removeTemporaryTablesForMetadataDump
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|validTables
init|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|ReplicationSemanticAnalyzer
operator|.
name|removeValuesTemporaryTables
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
block|{
name|add
argument_list|(
name|SemanticAnalyzer
operator|.
name|VALUES_TMP_TABLE_NAME_PREFIX
operator|+
literal|"a"
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|SemanticAnalyzer
operator|.
name|VALUES_TMP_TABLE_NAME_PREFIX
operator|+
literal|"b"
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|SemanticAnalyzer
operator|.
name|VALUES_TMP_TABLE_NAME_PREFIX
operator|+
literal|"c"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"c"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"b"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|validTables
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|validTables
argument_list|,
name|hasItems
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

