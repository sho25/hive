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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|FixMethodOrder
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
name|runners
operator|.
name|MethodSorters
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
name|io
operator|.
name|Files
import|;
end_import

begin_class
annotation|@
name|FixMethodOrder
argument_list|(
name|MethodSorters
operator|.
name|NAME_ASCENDING
argument_list|)
specifier|public
class|class
name|TestParseDriver
block|{
name|ParseDriver
name|parseDriver
init|=
operator|new
name|ParseDriver
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|atFirstWarmup
parameter_list|()
throws|throws
name|Exception
block|{
comment|// this test method is here to do an initial call to parsedriver; and prevent any tests with timeouts to be the first.
name|parseDriver
operator|.
name|parse
argument_list|(
literal|"select 1"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParse
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|selectStr
init|=
literal|"select field1, field2, sum(field3+field4)"
decl_stmt|;
name|String
name|whereStr
init|=
literal|"field5=1 and field6 in ('a', 'b')"
decl_stmt|;
name|String
name|havingStr
init|=
literal|"sum(field7)> 11"
decl_stmt|;
name|ASTNode
name|tree
init|=
name|parseDriver
operator|.
name|parse
argument_list|(
name|selectStr
operator|+
literal|" from table1 where "
operator|+
name|whereStr
operator|+
literal|" group by field1, field2 having  "
operator|+
name|havingStr
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|tree
operator|.
name|getType
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tree
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|ASTNode
name|queryTree
init|=
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|tree
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|EOF
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|queryTree
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|ASTNode
name|fromAST
init|=
operator|(
name|ASTNode
operator|)
name|queryTree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ASTNode
name|insertAST
init|=
operator|(
name|ASTNode
operator|)
name|queryTree
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|fromAST
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|TOK_FROM
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fromAST
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fromAST
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|TOK_TABREF
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fromAST
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fromAST
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|TOK_TABNAME
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fromAST
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fromAST
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|Identifier
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fromAST
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|,
literal|"table1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|insertAST
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|insertAST
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|TOK_DESTINATION
argument_list|)
expr_stmt|;
name|assertTree
argument_list|(
operator|(
name|ASTNode
operator|)
name|insertAST
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|,
name|parseDriver
operator|.
name|parseSelect
argument_list|(
name|selectStr
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|insertAST
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|TOK_WHERE
argument_list|)
expr_stmt|;
name|assertTree
argument_list|(
operator|(
name|ASTNode
operator|)
name|insertAST
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|,
name|parseDriver
operator|.
name|parseExpression
argument_list|(
name|whereStr
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|insertAST
operator|.
name|getChild
argument_list|(
literal|3
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|TOK_GROUPBY
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|insertAST
operator|.
name|getChild
argument_list|(
literal|3
argument_list|)
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|insertAST
operator|.
name|getChild
argument_list|(
literal|3
argument_list|)
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|TOK_TABLE_OR_COL
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|insertAST
operator|.
name|getChild
argument_list|(
literal|3
argument_list|)
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|Identifier
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|insertAST
operator|.
name|getChild
argument_list|(
literal|3
argument_list|)
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|,
literal|"field"
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|insertAST
operator|.
name|getChild
argument_list|(
literal|4
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|TOK_HAVING
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|insertAST
operator|.
name|getChild
argument_list|(
literal|4
argument_list|)
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertTree
argument_list|(
operator|(
name|ASTNode
operator|)
name|insertAST
operator|.
name|getChild
argument_list|(
literal|4
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|,
name|parseDriver
operator|.
name|parseExpression
argument_list|(
name|havingStr
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseSelect
parameter_list|()
throws|throws
name|Exception
block|{
name|ASTNode
name|tree
init|=
name|parseDriver
operator|.
name|parseSelect
argument_list|(
literal|"select field1, field2, sum(field3+field4)"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|tree
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|TOK_SELECT
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tree
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
operator|(
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
operator|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|TOK_SELEXPR
argument_list|)
expr_stmt|;
block|}
name|assertTree
argument_list|(
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|,
name|parseDriver
operator|.
name|parseExpression
argument_list|(
literal|"field1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTree
argument_list|(
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|,
name|parseDriver
operator|.
name|parseExpression
argument_list|(
literal|"field2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTree
argument_list|(
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|,
name|parseDriver
operator|.
name|parseExpression
argument_list|(
literal|"sum(field3+field4)"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseExpression
parameter_list|()
throws|throws
name|Exception
block|{
name|ASTNode
name|plusNode
init|=
name|parseDriver
operator|.
name|parseExpression
argument_list|(
literal|"field3 + field4"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|plusNode
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|PLUS
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|plusNode
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|plusNode
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|TOK_TABLE_OR_COL
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|plusNode
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|plusNode
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|Identifier
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|plusNode
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|,
literal|"field"
operator|+
operator|(
name|i
operator|+
literal|3
operator|)
argument_list|)
expr_stmt|;
block|}
name|ASTNode
name|sumNode
init|=
name|parseDriver
operator|.
name|parseExpression
argument_list|(
literal|"sum(field3 + field4)"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|sumNode
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|TOK_FUNCTION
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|sumNode
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|sumNode
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|Identifier
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|sumNode
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|,
literal|"sum"
argument_list|)
expr_stmt|;
name|assertTree
argument_list|(
operator|(
name|ASTNode
operator|)
name|sumNode
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|,
name|plusNode
argument_list|)
expr_stmt|;
name|ASTNode
name|tree
init|=
name|parseDriver
operator|.
name|parseExpression
argument_list|(
literal|"case when field1 = 1 then sum(field3 + field4) when field1 != 2 then "
operator|+
literal|"sum(field3-field4) else sum(field3 * field4) end"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|tree
operator|.
name|getChildCount
argument_list|()
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|KW_WHEN
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tree
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|EQUAL
argument_list|)
expr_stmt|;
name|assertTree
argument_list|(
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
argument_list|,
name|sumNode
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tree
operator|.
name|getChild
argument_list|(
literal|3
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|HiveParser
operator|.
name|NOTEQUAL
argument_list|)
expr_stmt|;
name|assertTree
argument_list|(
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|4
argument_list|)
argument_list|,
name|parseDriver
operator|.
name|parseExpression
argument_list|(
literal|"sum(field3-field4)"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTree
argument_list|(
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|5
argument_list|)
argument_list|,
name|parseDriver
operator|.
name|parseExpression
argument_list|(
literal|"sum(field3*field4)"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertTree
parameter_list|(
name|ASTNode
name|astNode1
parameter_list|,
name|ASTNode
name|astNode2
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|astNode1
operator|.
name|getType
argument_list|()
argument_list|,
name|astNode2
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|astNode1
operator|.
name|getChildCount
argument_list|()
argument_list|,
name|astNode2
operator|.
name|getChildCount
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|astNode1
operator|.
name|getChildCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertTree
argument_list|(
operator|(
name|ASTNode
operator|)
name|astNode1
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
argument_list|,
operator|(
name|ASTNode
operator|)
name|astNode2
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|1000
argument_list|)
specifier|public
name|void
name|testNestedFunctionCalls
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Expectation here is not to run into a timeout
name|parseDriver
operator|.
name|parse
argument_list|(
literal|"select greatest(1,greatest(1,greatest(1,greatest(1,greatest(1,greatest(1,greatest(1,"
operator|+
literal|"greatest(1,greatest(1,greatest(1,greatest(1,greatest(1,greatest(1,greatest(1,"
operator|+
literal|"greatest(1,greatest(1,(greatest(1,greatest(1,2)))))))))))))))))))"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|1000
argument_list|)
specifier|public
name|void
name|testHIVE18624
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Expectation here is not to run into a timeout
name|parseDriver
operator|.
name|parse
argument_list|(
literal|"EXPLAIN\n"
operator|+
literal|"SELECT DISTINCT\n"
operator|+
literal|"\n"
operator|+
literal|"\n"
operator|+
literal|"  IF(lower('a')<= lower('a')\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF(('a' IS NULL AND from_unixtime(UNIX_TIMESTAMP())<= 'a')\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF(if('a' = 'a', TRUE, FALSE) = 1\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF(('a' = 1 and lower('a') NOT IN ('a', 'a')\n"
operator|+
literal|"       and lower(if('a' = 'a','a','a'))<= lower('a'))\n"
operator|+
literal|"      OR ('a' like 'a' OR 'a' like 'a')\n"
operator|+
literal|"      OR 'a' in ('a','a')\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF(if(lower('a') in ('a', 'a') and 'a'='a', TRUE, FALSE) = 1\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF('a'='a' and unix_timestamp(if('a' = 'a',cast('a' as string),coalesce('a',cast('a' as string),from_unixtime(unix_timestamp()))))<= unix_timestamp(concat_ws('a',cast(lower('a') as string),'00:00:00')) + 9*3600\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"\n"
operator|+
literal|"  ,If(lower('a')<= lower('a')\n"
operator|+
literal|"      and if(lower('a') in ('a', 'a') and 'a'<>'a', TRUE, FALSE)<> 1\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF('a'=1 AND 'a'=1\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF('a' = 1 and COALESCE(cast('a' as int),0) = 0\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF('a' = 'a'\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"\n"
operator|+
literal|"  ,If('a' = 'a' AND lower('a')>lower(if(lower('a')<1830,'a',cast(date_add('a',1) as timestamp)))\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"\n"
operator|+
literal|"\n"
operator|+
literal|"\n"
operator|+
literal|"  ,IF('a' = 1\n"
operator|+
literal|"\n"
operator|+
literal|"  ,IF('a' in ('a', 'a') and ((unix_timestamp('a')-unix_timestamp('a')) / 60)> 30 and 'a' = 1\n"
operator|+
literal|"\n"
operator|+
literal|"\n"
operator|+
literal|"  ,'a', 'a')\n"
operator|+
literal|"\n"
operator|+
literal|"\n"
operator|+
literal|"  ,IF(if('a' = 'a', FALSE, TRUE ) = 1 AND 'a' IS NULL\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF('a' = 1 and 'a'>0\n"
operator|+
literal|"  , 'a'\n"
operator|+
literal|"\n"
operator|+
literal|"  ,IF('a' = 1 AND 'a' ='a'\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF('a' is not null and 'a' is not null and 'a'> 'a'\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF('a' = 1\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"\n"
operator|+
literal|"  ,IF('a' = 'a'\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"\n"
operator|+
literal|"  ,If('a' = 1\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF('a' = 1\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF('a' = 1\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"\n"
operator|+
literal|"  ,IF('a' ='a' and 'a' ='a' and cast(unix_timestamp('a') as  int) + 93600< cast(unix_timestamp()  as int)\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF('a' = 'a'\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF('a' = 'a' and 'a' in ('a','a','a')\n"
operator|+
literal|"  ,'a'\n"
operator|+
literal|"  ,IF('a' = 'a'\n"
operator|+
literal|"  ,'a','a'))\n"
operator|+
literal|"      )))))))))))))))))))))))\n"
operator|+
literal|"AS test_comp_exp"
argument_list|)
expr_stmt|;
block|}
specifier|static
class|class
name|ExoticQueryBuilder
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
specifier|public
name|void
name|recursiveSJS
parameter_list|(
name|int
name|depth
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"select "
argument_list|)
expr_stmt|;
name|addColumns
argument_list|(
literal|30
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" from \n"
argument_list|)
expr_stmt|;
name|tablePart
argument_list|(
name|depth
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" join \n"
argument_list|)
expr_stmt|;
name|tablePart
argument_list|(
name|depth
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" on ( "
argument_list|)
expr_stmt|;
name|wherePart
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" ) "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" where "
argument_list|)
expr_stmt|;
name|wherePart
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|tablePart
parameter_list|(
name|int
name|depth
parameter_list|)
block|{
if|if
condition|(
name|depth
operator|==
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" baseTable "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
name|recursiveSJS
argument_list|(
name|depth
operator|-
literal|1
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|") aa"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|wherePart
parameter_list|(
name|int
name|num
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|num
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"x = "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" or "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"x = -1"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addColumns
parameter_list|(
name|int
name|num
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|num
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"c"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" + 2*sqrt(11)+"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"cE"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getQuery
parameter_list|()
block|{
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10000
argument_list|)
specifier|public
name|void
name|testExoticSJSSubQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|ExoticQueryBuilder
name|eqb
init|=
operator|new
name|ExoticQueryBuilder
argument_list|()
decl_stmt|;
name|eqb
operator|.
name|recursiveSJS
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|String
name|q
init|=
name|eqb
operator|.
name|getQuery
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|q
argument_list|)
expr_stmt|;
name|parseDriver
operator|.
name|parse
argument_list|(
name|q
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testJoinResulInBraces
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|q
init|=
literal|"explain select a.key, b.value from"
operator|+
literal|"( (select key from src)a join (select value from src)b on a.key=b.value)"
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|q
argument_list|)
expr_stmt|;
name|ASTNode
name|root
init|=
name|parseDriver
operator|.
name|parse
argument_list|(
name|q
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|root
operator|.
name|dump
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFromSubqueryIsSetop
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|q
init|=
literal|"explain select key from ((select key from src) union (select key from src))subq "
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|q
argument_list|)
expr_stmt|;
name|ASTNode
name|root
init|=
name|parseDriver
operator|.
name|parse
argument_list|(
name|q
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|root
operator|.
name|dump
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseCreateScheduledQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|parseDriver
operator|.
name|parse
argument_list|(
literal|"create scheduled query asd cron '123' as select 1"
argument_list|)
expr_stmt|;
name|parseDriver
operator|.
name|parse
argument_list|(
literal|"create scheduled query asd cron '123' executed as 'x' as select 1"
argument_list|)
expr_stmt|;
name|parseDriver
operator|.
name|parse
argument_list|(
literal|"create scheduled query asd cron '123' executed as 'x' defined as select 1"
argument_list|)
expr_stmt|;
name|parseDriver
operator|.
name|parse
argument_list|(
literal|"create scheduled query asd cron '123' executed as 'x' disabled defined as select 1"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseAlterScheduledQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|parseDriver
operator|.
name|parse
argument_list|(
literal|"alter scheduled query asd cron '123'"
argument_list|)
expr_stmt|;
name|parseDriver
operator|.
name|parse
argument_list|(
literal|"alter scheduled query asd disabled"
argument_list|)
expr_stmt|;
name|parseDriver
operator|.
name|parse
argument_list|(
literal|"alter scheduled query asd defined as select 22"
argument_list|)
expr_stmt|;
name|parseDriver
operator|.
name|parse
argument_list|(
literal|"alter scheduled query asd executed as 'joe'"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseDropScheduledQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|parseDriver
operator|.
name|parse
argument_list|(
literal|"drop scheduled query asd"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

