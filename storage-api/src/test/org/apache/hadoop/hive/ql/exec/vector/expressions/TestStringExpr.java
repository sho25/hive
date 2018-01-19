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
name|exec
operator|.
name|vector
operator|.
name|expressions
package|;
end_package

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
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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

begin_class
specifier|public
class|class
name|TestStringExpr
block|{
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|Exception
block|{
name|StringExpr
operator|.
name|Finder
name|pattern
init|=
name|compile
argument_list|(
literal|"pattern"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|pattern
argument_list|)
expr_stmt|;
name|StringExpr
operator|.
name|Finder
name|patternOneChar
init|=
name|compile
argument_list|(
literal|"g"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|patternOneChar
argument_list|)
expr_stmt|;
name|StringExpr
operator|.
name|Finder
name|patternZero
init|=
name|compile
argument_list|(
literal|""
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|patternZero
argument_list|)
expr_stmt|;
name|String
name|input1
init|=
literal|"string that contains a patterN..."
decl_stmt|;
name|String
name|input2
init|=
literal|"string that contains a pattern..."
decl_stmt|;
name|String
name|input3
init|=
literal|"pattern at the start of a string"
decl_stmt|;
name|String
name|input4
init|=
literal|"string that ends with a pattern"
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Testing invalid match"
argument_list|,
operator|-
literal|1
argument_list|,
name|find
argument_list|(
name|pattern
argument_list|,
name|input1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Testing valid match"
argument_list|,
literal|23
argument_list|,
name|find
argument_list|(
name|pattern
argument_list|,
name|input2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Testing single-character match"
argument_list|,
literal|5
argument_list|,
name|find
argument_list|(
name|patternOneChar
argument_list|,
name|input1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Testing zero-length pattern"
argument_list|,
literal|0
argument_list|,
name|find
argument_list|(
name|patternZero
argument_list|,
name|input1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Testing match at start of string"
argument_list|,
literal|0
argument_list|,
name|find
argument_list|(
name|pattern
argument_list|,
name|input3
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Testing match at end of string"
argument_list|,
literal|24
argument_list|,
name|find
argument_list|(
name|pattern
argument_list|,
name|input4
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|StringExpr
operator|.
name|Finder
name|compile
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
return|return
name|StringExpr
operator|.
name|compile
argument_list|(
name|pattern
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|int
name|find
parameter_list|(
name|StringExpr
operator|.
name|Finder
name|finder
parameter_list|,
name|String
name|string
parameter_list|)
block|{
name|byte
index|[]
name|bytes
init|=
name|string
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
return|return
name|finder
operator|.
name|find
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|)
return|;
block|}
block|}
end_class

end_unit

