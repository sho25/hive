begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|Cache
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
name|cache
operator|.
name|CacheBuilder
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
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|FileReader
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|Matcher
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

begin_class
specifier|public
class|class
name|TestCheckPhase
extends|extends
name|Phase
block|{
specifier|private
specifier|final
name|File
name|mPatchFile
decl_stmt|;
specifier|private
specifier|final
name|String
name|mPatchURL
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|modifiedTestFiles
decl_stmt|;
specifier|private
specifier|static
name|Cache
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|patchUrls
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|expireAfterWrite
argument_list|(
literal|7
argument_list|,
name|TimeUnit
operator|.
name|DAYS
argument_list|)
operator|.
name|maximumSize
argument_list|(
literal|10000
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
name|fileNameFromDiff
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"[/][^\\s]*"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
name|javaTest
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"Test.*java"
argument_list|)
decl_stmt|;
specifier|public
name|TestCheckPhase
parameter_list|(
name|List
argument_list|<
name|HostExecutor
argument_list|>
name|hostExecutors
parameter_list|,
name|LocalCommandFactory
name|localCommandFactory
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateDefaults
parameter_list|,
name|String
name|patchUrl
parameter_list|,
name|File
name|patchFile
parameter_list|,
name|Logger
name|logger
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|modifiedTestFiles
parameter_list|)
block|{
name|super
argument_list|(
name|hostExecutors
argument_list|,
name|localCommandFactory
argument_list|,
name|templateDefaults
argument_list|,
name|logger
argument_list|)
expr_stmt|;
name|this
operator|.
name|mPatchFile
operator|=
name|patchFile
expr_stmt|;
name|this
operator|.
name|mPatchURL
operator|=
name|patchUrl
expr_stmt|;
name|this
operator|.
name|modifiedTestFiles
operator|=
name|modifiedTestFiles
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|mPatchURL
operator|!=
literal|null
condition|)
block|{
name|boolean
name|patchUrlWasSeen
init|=
name|patchUrls
operator|.
name|asMap
argument_list|()
operator|.
name|containsKey
argument_list|(
name|mPatchURL
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|patchUrlWasSeen
condition|)
block|{
name|patchUrls
operator|.
name|put
argument_list|(
name|mPatchURL
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Patch URL "
operator|+
name|mPatchURL
operator|+
literal|" was found in seen patch url's cache and "
operator|+
literal|"a test was probably run already on it. Aborting..."
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|mPatchFile
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Reading patchfile "
operator|+
name|mPatchFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|FileReader
name|fr
init|=
literal|null
decl_stmt|;
try|try
block|{
name|fr
operator|=
operator|new
name|FileReader
argument_list|(
name|mPatchFile
argument_list|)
expr_stmt|;
name|BufferedReader
name|br
init|=
operator|new
name|BufferedReader
argument_list|(
name|fr
argument_list|)
decl_stmt|;
name|String
name|line
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|br
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|line
operator|.
name|startsWith
argument_list|(
literal|"+++"
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Searching line : "
operator|+
name|line
argument_list|)
expr_stmt|;
name|Matcher
name|fileNameMatcher
init|=
name|fileNameFromDiff
operator|.
name|matcher
argument_list|(
name|line
argument_list|)
decl_stmt|;
if|if
condition|(
name|fileNameMatcher
operator|.
name|find
argument_list|()
condition|)
block|{
name|String
name|filePath
init|=
name|fileNameMatcher
operator|.
name|group
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|fileName
init|=
name|filePath
operator|.
name|substring
argument_list|(
name|filePath
operator|.
name|lastIndexOf
argument_list|(
literal|"/"
argument_list|)
operator|+
literal|1
argument_list|)
decl_stmt|;
name|Matcher
name|javaTestMatcher
init|=
name|javaTest
operator|.
name|matcher
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
if|if
condition|(
name|javaTestMatcher
operator|.
name|find
argument_list|()
operator|||
name|fileName
operator|.
name|endsWith
argument_list|(
literal|".q"
argument_list|)
condition|)
block|{
name|modifiedTestFiles
operator|.
name|add
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
finally|finally
block|{
name|fr
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Patch file is null"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

