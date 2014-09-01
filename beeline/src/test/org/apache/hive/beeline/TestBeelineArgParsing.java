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
name|hive
operator|.
name|beeline
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
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * Unit test for Beeline arg parser.  */
end_comment

begin_class
specifier|public
class|class
name|TestBeelineArgParsing
block|{
specifier|public
class|class
name|TestBeeline
extends|extends
name|BeeLine
block|{
name|String
name|connectArgs
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|properties
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|queries
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
name|boolean
name|dispatch
parameter_list|(
name|String
name|command
parameter_list|)
block|{
name|String
name|connectCommand
init|=
literal|"!connect"
decl_stmt|;
name|String
name|propertyCommand
init|=
literal|"!property"
decl_stmt|;
if|if
condition|(
name|command
operator|.
name|startsWith
argument_list|(
name|connectCommand
argument_list|)
condition|)
block|{
name|this
operator|.
name|connectArgs
operator|=
name|command
operator|.
name|substring
argument_list|(
name|connectCommand
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|,
name|command
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|command
operator|.
name|startsWith
argument_list|(
name|propertyCommand
argument_list|)
condition|)
block|{
name|this
operator|.
name|properties
operator|.
name|add
argument_list|(
name|command
operator|.
name|substring
argument_list|(
name|propertyCommand
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|,
name|command
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|queries
operator|.
name|add
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleArgs
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-n"
block|,
literal|"name"
block|,
literal|"-p"
block|,
literal|"password"
block|,
literal|"-d"
block|,
literal|"driver"
block|,
literal|"-a"
block|,
literal|"authType"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|connectArgs
operator|.
name|equals
argument_list|(
literal|"url name password driver"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getAuthType
argument_list|()
operator|.
name|equals
argument_list|(
literal|"authType"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * The first flag is taken by the parser.    */
annotation|@
name|Test
specifier|public
name|void
name|testDuplicateArgs
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-u"
block|,
literal|"url2"
block|,
literal|"-n"
block|,
literal|"name"
block|,
literal|"-p"
block|,
literal|"password"
block|,
literal|"-d"
block|,
literal|"driver"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|connectArgs
operator|.
name|equals
argument_list|(
literal|"url name password driver"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQueryScripts
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-n"
block|,
literal|"name"
block|,
literal|"-p"
block|,
literal|"password"
block|,
literal|"-d"
block|,
literal|"driver"
block|,
literal|"-e"
block|,
literal|"select1"
block|,
literal|"-e"
block|,
literal|"select2"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|connectArgs
operator|.
name|equals
argument_list|(
literal|"url name password driver"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|queries
operator|.
name|contains
argument_list|(
literal|"select1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|queries
operator|.
name|contains
argument_list|(
literal|"select2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test setting hive conf and hive vars with --hiveconf and --hivevar    */
annotation|@
name|Test
specifier|public
name|void
name|testHiveConfAndVars
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-n"
block|,
literal|"name"
block|,
literal|"-p"
block|,
literal|"password"
block|,
literal|"-d"
block|,
literal|"driver"
block|,
literal|"--hiveconf"
block|,
literal|"a=avalue"
block|,
literal|"--hiveconf"
block|,
literal|"b=bvalue"
block|,
literal|"--hivevar"
block|,
literal|"c=cvalue"
block|,
literal|"--hivevar"
block|,
literal|"d=dvalue"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|connectArgs
operator|.
name|equals
argument_list|(
literal|"url name password driver"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getHiveConfVariables
argument_list|()
operator|.
name|get
argument_list|(
literal|"a"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"avalue"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getHiveConfVariables
argument_list|()
operator|.
name|get
argument_list|(
literal|"b"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"bvalue"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getHiveVariables
argument_list|()
operator|.
name|get
argument_list|(
literal|"c"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"cvalue"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getHiveVariables
argument_list|()
operator|.
name|get
argument_list|(
literal|"d"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"dvalue"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBeelineOpts
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-n"
block|,
literal|"name"
block|,
literal|"-p"
block|,
literal|"password"
block|,
literal|"-d"
block|,
literal|"driver"
block|,
literal|"--autoCommit=true"
block|,
literal|"--verbose"
block|,
literal|"--truncateTable"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|connectArgs
operator|.
name|equals
argument_list|(
literal|"url name password driver"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getAutoCommit
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getVerbose
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getTruncateTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test setting script file with -f option.    */
annotation|@
name|Test
specifier|public
name|void
name|testScriptFile
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-n"
block|,
literal|"name"
block|,
literal|"-p"
block|,
literal|"password"
block|,
literal|"-d"
block|,
literal|"driver"
block|,
literal|"-f"
block|,
literal|"myscript"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|connectArgs
operator|.
name|equals
argument_list|(
literal|"url name password driver"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getScriptFile
argument_list|()
operator|.
name|equals
argument_list|(
literal|"myscript"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Displays the usage.    */
annotation|@
name|Test
specifier|public
name|void
name|testHelp
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"--help"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Displays the usage.    */
annotation|@
name|Test
specifier|public
name|void
name|testUnmatchedArgs
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-n"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

