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
name|cli
package|;
end_package

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
name|util
operator|.
name|List
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
name|cli
operator|.
name|control
operator|.
name|CliAdapter
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
name|cli
operator|.
name|control
operator|.
name|CliConfigs
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|beeline
operator|.
name|Parallelized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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
name|TestRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
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
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parallelized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestBeeLineDriver
block|{
specifier|static
name|CliAdapter
name|adapter
init|=
operator|new
name|CliConfigs
operator|.
name|BeeLineConfig
argument_list|()
operator|.
name|getCliAdapter
argument_list|()
decl_stmt|;
annotation|@
name|Parameters
argument_list|(
name|name
operator|=
literal|"{0}"
argument_list|)
specifier|public
specifier|static
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|getParameters
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|adapter
operator|.
name|getParameters
argument_list|()
return|;
block|}
annotation|@
name|ClassRule
specifier|public
specifier|static
name|TestRule
name|cliClassRule
init|=
name|adapter
operator|.
name|buildClassRule
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestRule
name|cliTestRule
init|=
name|adapter
operator|.
name|buildTestRule
argument_list|()
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|File
name|qfile
decl_stmt|;
specifier|public
name|TestBeeLineDriver
parameter_list|(
name|String
name|name
parameter_list|,
name|File
name|qfile
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|qfile
operator|=
name|qfile
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCliDriver
parameter_list|()
throws|throws
name|Exception
block|{
name|adapter
operator|.
name|runTest
argument_list|(
name|name
argument_list|,
name|qfile
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

