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
operator|.
name|control
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|Description
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
name|model
operator|.
name|Statement
import|;
end_import

begin_comment
comment|/**  * This class adapts old vm test-executors to be executed in multiple instances  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|CliAdapter
block|{
specifier|protected
specifier|final
name|AbstractCliConfig
name|cliConfig
decl_stmt|;
specifier|public
name|CliAdapter
parameter_list|(
name|AbstractCliConfig
name|cliConfig
parameter_list|)
block|{
name|this
operator|.
name|cliConfig
operator|=
name|cliConfig
expr_stmt|;
block|}
specifier|public
specifier|final
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
name|Set
argument_list|<
name|File
argument_list|>
name|f
init|=
name|cliConfig
operator|.
name|getQueryFiles
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|File
name|file
range|:
name|f
control|)
block|{
name|String
name|label
init|=
name|file
operator|.
name|getName
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"\\.[^\\.]+$"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|ret
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|label
block|,
name|file
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|public
specifier|abstract
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
function_decl|;
comment|// HIVE-14444 pending rename: before
specifier|public
specifier|abstract
name|void
name|setUp
parameter_list|()
function_decl|;
comment|// HIVE-14444 pending rename: after
specifier|public
specifier|abstract
name|void
name|tearDown
parameter_list|()
function_decl|;
comment|// HIVE-14444 pending rename: afterClass
specifier|public
specifier|abstract
name|void
name|shutdown
parameter_list|()
throws|throws
name|Exception
function_decl|;
specifier|public
specifier|abstract
name|void
name|runTest
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|fileName
parameter_list|,
name|String
name|absolutePath
parameter_list|)
throws|throws
name|Exception
function_decl|;
specifier|public
specifier|final
name|TestRule
name|buildClassRule
parameter_list|()
block|{
return|return
operator|new
name|TestRule
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Statement
name|apply
parameter_list|(
specifier|final
name|Statement
name|base
parameter_list|,
name|Description
name|description
parameter_list|)
block|{
return|return
operator|new
name|Statement
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|evaluate
parameter_list|()
throws|throws
name|Throwable
block|{
name|CliAdapter
operator|.
name|this
operator|.
name|beforeClass
argument_list|()
expr_stmt|;
try|try
block|{
name|base
operator|.
name|evaluate
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|CliAdapter
operator|.
name|this
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
block|}
return|;
block|}
specifier|public
specifier|final
name|TestRule
name|buildTestRule
parameter_list|()
block|{
return|return
operator|new
name|TestRule
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Statement
name|apply
parameter_list|(
specifier|final
name|Statement
name|base
parameter_list|,
name|Description
name|description
parameter_list|)
block|{
return|return
operator|new
name|Statement
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|evaluate
parameter_list|()
throws|throws
name|Throwable
block|{
name|CliAdapter
operator|.
name|this
operator|.
name|setUp
argument_list|()
expr_stmt|;
try|try
block|{
name|base
operator|.
name|evaluate
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|CliAdapter
operator|.
name|this
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
block|}
return|;
block|}
comment|// HIVE-14444: pending refactor to push File forward
specifier|public
specifier|final
name|void
name|runTest
parameter_list|(
name|String
name|name
parameter_list|,
name|File
name|qfile
parameter_list|)
throws|throws
name|Exception
block|{
name|runTest
argument_list|(
name|name
argument_list|,
name|qfile
operator|.
name|getName
argument_list|()
argument_list|,
name|qfile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

