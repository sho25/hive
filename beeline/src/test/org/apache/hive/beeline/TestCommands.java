begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|beeline
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

begin_class
specifier|public
class|class
name|TestCommands
block|{
annotation|@
name|Test
specifier|public
name|void
name|testLinesEndingWithComments
parameter_list|()
block|{
name|BeeLine
name|beeline
init|=
operator|new
name|BeeLine
argument_list|()
decl_stmt|;
name|Commands
name|commands
init|=
operator|new
name|Commands
argument_list|(
name|beeline
argument_list|)
decl_stmt|;
name|int
index|[]
name|escape
init|=
block|{
operator|-
literal|1
block|}
decl_stmt|;
name|assertEquals
argument_list|(
literal|"show tables;"
argument_list|,
name|commands
operator|.
name|removeComments
argument_list|(
literal|"show tables;"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"show tables;"
argument_list|,
name|commands
operator|.
name|removeComments
argument_list|(
literal|"show tables; --comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"show tables;"
argument_list|,
name|commands
operator|.
name|removeComments
argument_list|(
literal|"show tables; -------comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"show tables;"
argument_list|,
name|commands
operator|.
name|removeComments
argument_list|(
literal|"show tables; -------comments;one;two;three;;;;"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"show"
argument_list|,
name|commands
operator|.
name|removeComments
argument_list|(
literal|"show-- tables; -------comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"show"
argument_list|,
name|commands
operator|.
name|removeComments
argument_list|(
literal|"show --tables; -------comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"s"
argument_list|,
name|commands
operator|.
name|removeComments
argument_list|(
literal|"s--how --tables; -------comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|commands
operator|.
name|removeComments
argument_list|(
literal|"-- show tables; -------comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"\"show tables\""
argument_list|,
name|commands
operator|.
name|removeComments
argument_list|(
literal|"\"show tables\" --comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"\"show --comments tables\""
argument_list|,
name|commands
operator|.
name|removeComments
argument_list|(
literal|"\"show --comments tables\" --comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"\"'show --comments' tables\""
argument_list|,
name|commands
operator|.
name|removeComments
argument_list|(
literal|"\"'show --comments' tables\" --comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"'show --comments tables'"
argument_list|,
name|commands
operator|.
name|removeComments
argument_list|(
literal|"'show --comments tables' --comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"'\"show --comments tables\"'"
argument_list|,
name|commands
operator|.
name|removeComments
argument_list|(
literal|"'\"show --comments tables\"' --comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

