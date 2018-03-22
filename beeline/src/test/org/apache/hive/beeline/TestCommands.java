begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|HiveStringUtils
operator|.
name|removeComments
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|removeComments
argument_list|(
literal|"'\"show --comments tables\"' --comments"
argument_list|,
name|escape
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test the commands directly call from beeline.    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testBeelineCommands
parameter_list|()
throws|throws
name|IOException
block|{
comment|// avoid System.exit() call in beeline which causes JVM to exit and fails the test
name|System
operator|.
name|setProperty
argument_list|(
name|BeeLineOpts
operator|.
name|PROPERTY_NAME_EXIT
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
comment|// Verify the command without ';' at the end also works fine
name|BeeLine
operator|.
name|mainWithInputRedirection
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"jdbc:hive2://"
block|,
literal|"-e"
block|,
literal|"select 3"
block|}
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|BeeLine
operator|.
name|mainWithInputRedirection
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"jdbc:hive2://"
block|,
literal|"-e"
block|,
literal|"create table t1(x int); show tables"
block|}
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

