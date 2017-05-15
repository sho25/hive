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
name|TestQBJoinTreeApplyPredicate
block|{
specifier|static
name|QueryState
name|queryState
decl_stmt|;
specifier|static
name|HiveConf
name|conf
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
name|queryState
operator|=
operator|new
name|QueryState
operator|.
name|Builder
argument_list|()
operator|.
name|withHiveConf
argument_list|(
operator|new
name|HiveConf
argument_list|(
name|SemanticAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|conf
operator|=
name|queryState
operator|.
name|getConf
argument_list|()
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
name|sA
operator|=
operator|new
name|CalcitePlanner
argument_list|(
name|queryState
argument_list|)
expr_stmt|;
block|}
specifier|static
name|ASTNode
name|constructIdentifier
parameter_list|(
name|String
name|nm
parameter_list|)
block|{
return|return
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|create
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|nm
argument_list|)
return|;
block|}
specifier|static
name|ASTNode
name|constructTabRef
parameter_list|(
name|String
name|tblNm
parameter_list|)
block|{
name|ASTNode
name|table
init|=
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|create
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLE_OR_COL
argument_list|,
literal|"TOK_TABLE_OR_COL"
argument_list|)
decl_stmt|;
name|ASTNode
name|id
init|=
name|constructIdentifier
argument_list|(
name|tblNm
argument_list|)
decl_stmt|;
name|table
operator|.
name|addChild
argument_list|(
name|id
argument_list|)
expr_stmt|;
return|return
name|table
return|;
block|}
specifier|static
name|ASTNode
name|constructColRef
parameter_list|(
name|String
name|tblNm
parameter_list|,
name|String
name|colNm
parameter_list|)
block|{
name|ASTNode
name|table
init|=
name|constructTabRef
argument_list|(
name|tblNm
argument_list|)
decl_stmt|;
name|ASTNode
name|col
init|=
name|constructIdentifier
argument_list|(
name|colNm
argument_list|)
decl_stmt|;
name|ASTNode
name|dot
init|=
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|create
argument_list|(
name|HiveParser
operator|.
name|DOT
argument_list|,
literal|"."
argument_list|)
decl_stmt|;
name|dot
operator|.
name|addChild
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|dot
operator|.
name|addChild
argument_list|(
name|col
argument_list|)
expr_stmt|;
return|return
name|dot
return|;
block|}
specifier|static
name|ASTNode
name|constructEqualityCond
parameter_list|(
name|String
name|lTbl
parameter_list|,
name|String
name|lCol
parameter_list|,
name|String
name|rTbl
parameter_list|,
name|String
name|rCol
parameter_list|)
block|{
name|ASTNode
name|lRef
init|=
name|constructColRef
argument_list|(
name|lTbl
argument_list|,
name|lCol
argument_list|)
decl_stmt|;
name|ASTNode
name|rRef
init|=
name|constructColRef
argument_list|(
name|rTbl
argument_list|,
name|rCol
argument_list|)
decl_stmt|;
name|ASTNode
name|eq
init|=
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|create
argument_list|(
name|HiveParser
operator|.
name|EQUAL
argument_list|,
literal|"="
argument_list|)
decl_stmt|;
name|eq
operator|.
name|addChild
argument_list|(
name|lRef
argument_list|)
expr_stmt|;
name|eq
operator|.
name|addChild
argument_list|(
name|rRef
argument_list|)
expr_stmt|;
return|return
name|eq
return|;
block|}
name|QBJoinTree
name|createJoinTree
parameter_list|(
name|JoinType
name|type
parameter_list|,
name|String
name|leftAlias
parameter_list|,
name|QBJoinTree
name|leftTree
parameter_list|,
name|String
name|rightAlias
parameter_list|)
block|{
name|QBJoinTree
name|jT
init|=
operator|new
name|QBJoinTree
argument_list|()
decl_stmt|;
name|JoinCond
index|[]
name|condn
init|=
operator|new
name|JoinCond
index|[
literal|1
index|]
decl_stmt|;
name|condn
index|[
literal|0
index|]
operator|=
operator|new
name|JoinCond
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|,
name|type
argument_list|)
expr_stmt|;
if|if
condition|(
name|leftTree
operator|==
literal|null
condition|)
block|{
name|jT
operator|.
name|setLeftAlias
argument_list|(
name|leftAlias
argument_list|)
expr_stmt|;
name|String
index|[]
name|leftAliases
init|=
operator|new
name|String
index|[
literal|1
index|]
decl_stmt|;
name|leftAliases
index|[
literal|0
index|]
operator|=
name|leftAlias
expr_stmt|;
name|jT
operator|.
name|setLeftAliases
argument_list|(
name|leftAliases
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|jT
operator|.
name|setJoinSrc
argument_list|(
name|leftTree
argument_list|)
expr_stmt|;
name|String
index|[]
name|leftChildAliases
init|=
name|leftTree
operator|.
name|getLeftAliases
argument_list|()
decl_stmt|;
name|String
name|leftAliases
index|[]
init|=
operator|new
name|String
index|[
name|leftChildAliases
operator|.
name|length
operator|+
literal|1
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|leftChildAliases
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|leftAliases
index|[
name|i
index|]
operator|=
name|leftChildAliases
index|[
name|i
index|]
expr_stmt|;
block|}
name|leftAliases
index|[
name|leftChildAliases
operator|.
name|length
index|]
operator|=
name|leftTree
operator|.
name|getRightAliases
argument_list|()
index|[
literal|0
index|]
expr_stmt|;
name|jT
operator|.
name|setLeftAliases
argument_list|(
name|leftAliases
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|rightAliases
init|=
operator|new
name|String
index|[
literal|1
index|]
decl_stmt|;
name|rightAliases
index|[
literal|0
index|]
operator|=
name|rightAlias
expr_stmt|;
name|jT
operator|.
name|setRightAliases
argument_list|(
name|rightAliases
argument_list|)
expr_stmt|;
name|String
index|[]
name|children
init|=
operator|new
name|String
index|[
literal|2
index|]
decl_stmt|;
name|children
index|[
literal|0
index|]
operator|=
name|leftAlias
expr_stmt|;
name|children
index|[
literal|1
index|]
operator|=
name|rightAlias
expr_stmt|;
name|jT
operator|.
name|setBaseSrc
argument_list|(
name|children
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|>
name|expressions
init|=
operator|new
name|ArrayList
argument_list|<
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|expressions
operator|.
name|add
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|expressions
operator|.
name|add
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|jT
operator|.
name|setExpressions
argument_list|(
name|expressions
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|Boolean
argument_list|>
name|nullsafes
init|=
operator|new
name|ArrayList
argument_list|<
name|Boolean
argument_list|>
argument_list|()
decl_stmt|;
name|jT
operator|.
name|setNullSafes
argument_list|(
name|nullsafes
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|>
name|filters
init|=
operator|new
name|ArrayList
argument_list|<
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|filters
operator|.
name|add
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|filters
operator|.
name|add
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|jT
operator|.
name|setFilters
argument_list|(
name|filters
argument_list|)
expr_stmt|;
name|jT
operator|.
name|setFilterMap
argument_list|(
operator|new
name|int
index|[
literal|2
index|]
index|[]
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|>
name|filtersForPushing
init|=
operator|new
name|ArrayList
argument_list|<
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|filtersForPushing
operator|.
name|add
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|filtersForPushing
operator|.
name|add
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|jT
operator|.
name|setFiltersForPushing
argument_list|(
name|filtersForPushing
argument_list|)
expr_stmt|;
return|return
name|jT
return|;
block|}
name|ASTNode
name|applyEqPredicate
parameter_list|(
name|QBJoinTree
name|jT
parameter_list|,
name|String
name|lTbl
parameter_list|,
name|String
name|lCol
parameter_list|,
name|String
name|rTbl
parameter_list|,
name|String
name|rCol
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ASTNode
name|joinCond
init|=
name|constructEqualityCond
argument_list|(
name|lTbl
argument_list|,
name|lCol
argument_list|,
name|rTbl
argument_list|,
name|rCol
argument_list|)
decl_stmt|;
name|ASTNode
name|leftCondn
init|=
operator|(
name|ASTNode
operator|)
name|joinCond
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ASTNode
name|rightCondn
init|=
operator|(
name|ASTNode
operator|)
name|joinCond
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|leftSrc
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|leftCondAl1
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|leftCondAl2
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|rightCondAl1
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|rightCondAl2
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|sA
operator|.
name|parseJoinCondPopulateAlias
argument_list|(
name|jT
argument_list|,
name|leftCondn
argument_list|,
name|leftCondAl1
argument_list|,
name|leftCondAl2
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|sA
operator|.
name|parseJoinCondPopulateAlias
argument_list|(
name|jT
argument_list|,
name|rightCondn
argument_list|,
name|rightCondAl1
argument_list|,
name|rightCondAl2
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|sA
operator|.
name|applyEqualityPredicateToQBJoinTree
argument_list|(
name|jT
argument_list|,
name|JoinType
operator|.
name|INNER
argument_list|,
name|leftSrc
argument_list|,
name|joinCond
argument_list|,
name|leftCondn
argument_list|,
name|rightCondn
argument_list|,
name|leftCondAl1
argument_list|,
name|leftCondAl2
argument_list|,
name|rightCondAl1
argument_list|,
name|rightCondAl2
argument_list|)
expr_stmt|;
return|return
name|joinCond
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleCondn
parameter_list|()
throws|throws
name|SemanticException
block|{
name|QBJoinTree
name|jT
init|=
name|createJoinTree
argument_list|(
name|JoinType
operator|.
name|INNER
argument_list|,
literal|"a"
argument_list|,
literal|null
argument_list|,
literal|"b"
argument_list|)
decl_stmt|;
name|ASTNode
name|joinCond
init|=
name|applyEqPredicate
argument_list|(
name|jT
argument_list|,
literal|"a"
argument_list|,
literal|"x"
argument_list|,
literal|"b"
argument_list|,
literal|"y"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test3WayJoin
parameter_list|()
throws|throws
name|SemanticException
block|{
name|QBJoinTree
name|jT1
init|=
name|createJoinTree
argument_list|(
name|JoinType
operator|.
name|INNER
argument_list|,
literal|"a"
argument_list|,
literal|null
argument_list|,
literal|"b"
argument_list|)
decl_stmt|;
name|QBJoinTree
name|jT
init|=
name|createJoinTree
argument_list|(
name|JoinType
operator|.
name|INNER
argument_list|,
literal|"b"
argument_list|,
name|jT1
argument_list|,
literal|"c"
argument_list|)
decl_stmt|;
name|ASTNode
name|joinCond1
init|=
name|applyEqPredicate
argument_list|(
name|jT
argument_list|,
literal|"a"
argument_list|,
literal|"x"
argument_list|,
literal|"b"
argument_list|,
literal|"y"
argument_list|)
decl_stmt|;
name|ASTNode
name|joinCond2
init|=
name|applyEqPredicate
argument_list|(
name|jT
argument_list|,
literal|"b"
argument_list|,
literal|"y"
argument_list|,
literal|"c"
argument_list|,
literal|"z"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT1
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond1
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT1
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond1
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond2
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond2
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test3WayJoinSwitched
parameter_list|()
throws|throws
name|SemanticException
block|{
name|QBJoinTree
name|jT1
init|=
name|createJoinTree
argument_list|(
name|JoinType
operator|.
name|INNER
argument_list|,
literal|"a"
argument_list|,
literal|null
argument_list|,
literal|"b"
argument_list|)
decl_stmt|;
name|QBJoinTree
name|jT
init|=
name|createJoinTree
argument_list|(
name|JoinType
operator|.
name|INNER
argument_list|,
literal|"b"
argument_list|,
name|jT1
argument_list|,
literal|"c"
argument_list|)
decl_stmt|;
name|ASTNode
name|joinCond1
init|=
name|applyEqPredicate
argument_list|(
name|jT
argument_list|,
literal|"b"
argument_list|,
literal|"y"
argument_list|,
literal|"a"
argument_list|,
literal|"x"
argument_list|)
decl_stmt|;
name|ASTNode
name|joinCond2
init|=
name|applyEqPredicate
argument_list|(
name|jT
argument_list|,
literal|"b"
argument_list|,
literal|"y"
argument_list|,
literal|"c"
argument_list|,
literal|"z"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT1
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond1
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT1
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond1
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond2
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond2
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test4WayJoin
parameter_list|()
throws|throws
name|SemanticException
block|{
name|QBJoinTree
name|jT1
init|=
name|createJoinTree
argument_list|(
name|JoinType
operator|.
name|INNER
argument_list|,
literal|"a"
argument_list|,
literal|null
argument_list|,
literal|"b"
argument_list|)
decl_stmt|;
name|QBJoinTree
name|jT2
init|=
name|createJoinTree
argument_list|(
name|JoinType
operator|.
name|INNER
argument_list|,
literal|"b"
argument_list|,
name|jT1
argument_list|,
literal|"c"
argument_list|)
decl_stmt|;
name|QBJoinTree
name|jT
init|=
name|createJoinTree
argument_list|(
name|JoinType
operator|.
name|INNER
argument_list|,
literal|"c"
argument_list|,
name|jT2
argument_list|,
literal|"d"
argument_list|)
decl_stmt|;
name|ASTNode
name|joinCond1
init|=
name|applyEqPredicate
argument_list|(
name|jT
argument_list|,
literal|"a"
argument_list|,
literal|"x"
argument_list|,
literal|"b"
argument_list|,
literal|"y"
argument_list|)
decl_stmt|;
name|ASTNode
name|joinCond2
init|=
name|applyEqPredicate
argument_list|(
name|jT
argument_list|,
literal|"b"
argument_list|,
literal|"y"
argument_list|,
literal|"c"
argument_list|,
literal|"z"
argument_list|)
decl_stmt|;
name|ASTNode
name|joinCond3
init|=
name|applyEqPredicate
argument_list|(
name|jT
argument_list|,
literal|"a"
argument_list|,
literal|"x"
argument_list|,
literal|"c"
argument_list|,
literal|"z"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT1
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond1
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT1
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond1
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT2
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond2
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT2
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond2
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT2
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|joinCond3
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT2
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|joinCond3
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test4WayJoinSwitched
parameter_list|()
throws|throws
name|SemanticException
block|{
name|QBJoinTree
name|jT1
init|=
name|createJoinTree
argument_list|(
name|JoinType
operator|.
name|INNER
argument_list|,
literal|"a"
argument_list|,
literal|null
argument_list|,
literal|"b"
argument_list|)
decl_stmt|;
name|QBJoinTree
name|jT2
init|=
name|createJoinTree
argument_list|(
name|JoinType
operator|.
name|INNER
argument_list|,
literal|"b"
argument_list|,
name|jT1
argument_list|,
literal|"c"
argument_list|)
decl_stmt|;
name|QBJoinTree
name|jT
init|=
name|createJoinTree
argument_list|(
name|JoinType
operator|.
name|INNER
argument_list|,
literal|"c"
argument_list|,
name|jT2
argument_list|,
literal|"d"
argument_list|)
decl_stmt|;
name|ASTNode
name|joinCond1
init|=
name|applyEqPredicate
argument_list|(
name|jT
argument_list|,
literal|"b"
argument_list|,
literal|"y"
argument_list|,
literal|"a"
argument_list|,
literal|"x"
argument_list|)
decl_stmt|;
name|ASTNode
name|joinCond2
init|=
name|applyEqPredicate
argument_list|(
name|jT
argument_list|,
literal|"b"
argument_list|,
literal|"y"
argument_list|,
literal|"c"
argument_list|,
literal|"z"
argument_list|)
decl_stmt|;
name|ASTNode
name|joinCond3
init|=
name|applyEqPredicate
argument_list|(
name|jT
argument_list|,
literal|"c"
argument_list|,
literal|"z"
argument_list|,
literal|"a"
argument_list|,
literal|"x"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT1
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond1
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT1
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond1
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT2
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond2
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT2
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|joinCond2
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT2
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|joinCond3
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|jT2
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|joinCond3
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

