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
name|io
package|;
end_package

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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStreamReader
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
name|HashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|fs
operator|.
name|FileStatus
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|common
operator|.
name|FileUtils
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
name|plan
operator|.
name|MapredWork
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
name|plan
operator|.
name|PartitionDesc
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
name|mapred
operator|.
name|TextInputFormat
import|;
end_import

begin_class
specifier|public
class|class
name|SymbolicInputFormat
implements|implements
name|ReworkMapredInputFormat
block|{
specifier|public
name|void
name|rework
parameter_list|(
name|HiveConf
name|job
parameter_list|,
name|MapredWork
name|work
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToParts
init|=
name|work
operator|.
name|getMapWork
argument_list|()
operator|.
name|getPathToPartitionInfo
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|toRemovePaths
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|toAddPathToPart
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Path
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
init|=
name|work
operator|.
name|getMapWork
argument_list|()
operator|.
name|getPathToAliases
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|pathPartEntry
range|:
name|pathToParts
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Path
name|path
init|=
name|pathPartEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|PartitionDesc
name|partDesc
init|=
name|pathPartEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|// this path points to a symlink path
if|if
condition|(
name|partDesc
operator|.
name|getInputFileFormatClass
argument_list|()
operator|.
name|equals
argument_list|(
name|SymlinkTextInputFormat
operator|.
name|class
argument_list|)
condition|)
block|{
comment|// change to TextInputFormat
name|partDesc
operator|.
name|setInputFileFormatClass
argument_list|(
name|TextInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|FileSystem
name|fileSystem
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|FileStatus
name|fStatus
init|=
name|fileSystem
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|symlinks
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|fStatus
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|symlinks
operator|=
operator|new
name|FileStatus
index|[]
block|{
name|fStatus
block|}
expr_stmt|;
block|}
else|else
block|{
name|symlinks
operator|=
name|fileSystem
operator|.
name|listStatus
argument_list|(
name|path
argument_list|,
name|FileUtils
operator|.
name|HIDDEN_FILES_PATH_FILTER
argument_list|)
expr_stmt|;
block|}
name|toRemovePaths
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|pathToAliases
operator|.
name|remove
argument_list|(
name|path
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|symlink
range|:
name|symlinks
control|)
block|{
name|BufferedReader
name|reader
init|=
literal|null
decl_stmt|;
try|try
block|{
name|reader
operator|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|fileSystem
operator|.
name|open
argument_list|(
name|symlink
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|partDesc
operator|.
name|setInputFileFormatClass
argument_list|(
name|TextInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|String
name|line
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
comment|// no check for the line? How to check?
comment|// if the line is invalid for any reason, the job will fail.
name|FileStatus
index|[]
name|matches
init|=
name|fileSystem
operator|.
name|globStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|line
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|fileStatus
range|:
name|matches
control|)
block|{
name|Path
name|schemaLessPath
init|=
name|Path
operator|.
name|getPathWithoutSchemeAndAuthority
argument_list|(
name|fileStatus
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|toAddPathToPart
operator|.
name|put
argument_list|(
name|schemaLessPath
argument_list|,
name|partDesc
argument_list|)
expr_stmt|;
name|pathToAliases
operator|.
name|put
argument_list|(
name|schemaLessPath
argument_list|,
name|aliases
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
for|for
control|(
name|Entry
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|toAdd
range|:
name|toAddPathToPart
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|work
operator|.
name|getMapWork
argument_list|()
operator|.
name|addPathToPartitionInfo
argument_list|(
name|toAdd
operator|.
name|getKey
argument_list|()
argument_list|,
name|toAdd
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Path
name|toRemove
range|:
name|toRemovePaths
control|)
block|{
name|work
operator|.
name|getMapWork
argument_list|()
operator|.
name|removePathToPartitionInfo
argument_list|(
name|toRemove
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

