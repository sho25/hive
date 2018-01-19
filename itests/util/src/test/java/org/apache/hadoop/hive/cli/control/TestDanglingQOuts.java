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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|io
operator|.
name|FilenameFilter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
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
name|com
operator|.
name|beust
operator|.
name|jcommander
operator|.
name|JCommander
import|;
end_import

begin_import
import|import
name|com
operator|.
name|beust
operator|.
name|jcommander
operator|.
name|Parameter
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
name|Sets
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
name|Sets
operator|.
name|SetView
import|;
end_import

begin_comment
comment|/**  * This test ensures that there are no dangling q.out files in the project  *  * It has a cli functionlity to remove them if there are any.  */
end_comment

begin_class
specifier|public
class|class
name|TestDanglingQOuts
block|{
specifier|public
specifier|static
class|class
name|QOutFilter
implements|implements
name|FilenameFilter
block|{
name|Pattern
name|pattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|".*q.out$"
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|File
name|dir
parameter_list|,
name|String
name|fileName
parameter_list|)
block|{
return|return
name|pattern
operator|.
name|matcher
argument_list|(
name|fileName
argument_list|)
operator|.
name|matches
argument_list|()
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|Params
block|{
annotation|@
name|Parameter
argument_list|(
name|names
operator|=
literal|"--delete"
argument_list|,
name|description
operator|=
literal|"Removes any unreferenced q.out"
argument_list|)
specifier|private
name|boolean
name|delete
init|=
literal|false
decl_stmt|;
block|}
specifier|private
name|Set
argument_list|<
name|File
argument_list|>
name|outsFound
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|File
argument_list|,
name|AbstractCliConfig
argument_list|>
name|outsNeeded
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|TestDanglingQOuts
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|clz
range|:
name|CliConfigs
operator|.
name|class
operator|.
name|getDeclaredClasses
argument_list|()
control|)
block|{
if|if
condition|(
name|clz
operator|==
name|CliConfigs
operator|.
name|DummyConfig
operator|.
name|class
condition|)
block|{
continue|continue;
block|}
name|AbstractCliConfig
name|config
init|=
operator|(
name|AbstractCliConfig
operator|)
name|clz
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|File
argument_list|>
name|qfiles
init|=
name|config
operator|.
name|getQueryFiles
argument_list|()
decl_stmt|;
for|for
control|(
name|File
name|file
range|:
name|qfiles
control|)
block|{
name|String
name|baseName
init|=
name|file
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|rd
init|=
name|config
operator|.
name|getResultsDir
argument_list|()
decl_stmt|;
name|File
name|of
init|=
operator|new
name|File
argument_list|(
name|rd
argument_list|,
name|baseName
operator|+
literal|".out"
argument_list|)
decl_stmt|;
if|if
condition|(
name|outsNeeded
operator|.
name|containsKey
argument_list|(
name|of
argument_list|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|printf
argument_list|(
literal|"duplicate: [%s;%s] %s\n"
argument_list|,
name|config
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|outsNeeded
operator|.
name|get
argument_list|(
name|of
argument_list|)
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|of
argument_list|)
expr_stmt|;
comment|// throw new RuntimeException("duplicate?!");
block|}
name|outsNeeded
operator|.
name|put
argument_list|(
name|of
argument_list|,
name|config
argument_list|)
expr_stmt|;
block|}
name|File
name|od
init|=
operator|new
name|File
argument_list|(
name|config
operator|.
name|getResultsDir
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|File
name|file
range|:
name|od
operator|.
name|listFiles
argument_list|(
operator|new
name|QOutFilter
argument_list|()
argument_list|)
control|)
block|{
name|outsFound
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|checkDanglingQOut
parameter_list|()
block|{
name|SetView
argument_list|<
name|File
argument_list|>
name|dangling
init|=
name|Sets
operator|.
name|difference
argument_list|(
name|outsFound
argument_list|,
name|outsNeeded
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"dangling qouts: "
operator|+
name|dangling
argument_list|,
name|dangling
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
argument_list|(
literal|"Seems like there are some from this class as well..."
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|checkMissingQOut
parameter_list|()
block|{
name|SetView
argument_list|<
name|File
argument_list|>
name|missing
init|=
name|Sets
operator|.
name|difference
argument_list|(
name|outsNeeded
operator|.
name|keySet
argument_list|()
argument_list|,
name|outsFound
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|missing
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|missing
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Params
name|params
init|=
operator|new
name|Params
argument_list|()
decl_stmt|;
operator|new
name|JCommander
argument_list|(
name|params
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|TestDanglingQOuts
name|c
init|=
operator|new
name|TestDanglingQOuts
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|File
argument_list|>
name|unused
init|=
name|Sets
operator|.
name|difference
argument_list|(
name|c
operator|.
name|outsFound
argument_list|,
name|c
operator|.
name|outsNeeded
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|File
name|file
range|:
name|unused
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|file
argument_list|)
expr_stmt|;
if|if
condition|(
name|params
operator|.
name|delete
condition|)
block|{
name|file
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

