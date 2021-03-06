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
operator|.
name|repl
operator|.
name|dump
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
name|FileNotFoundException
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
name|net
operator|.
name|URI
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
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
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
name|ValidWriteIdList
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
name|exec
operator|.
name|Utilities
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
name|io
operator|.
name|AcidUtils
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
name|io
operator|.
name|HiveInputFormat
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
name|parse
operator|.
name|LoadSemanticAnalyzer
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
name|parse
operator|.
name|repl
operator|.
name|CopyUtils
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
name|ExportWork
operator|.
name|MmContext
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
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import static
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
name|ErrorMsg
operator|.
name|FILE_NOT_FOUND
import|;
end_import

begin_comment
comment|//TODO: this object is created once to call one method and then immediately destroyed.
end_comment

begin_comment
comment|//So it's basically just a roundabout way to pass arguments to a static method. Simplify?
end_comment

begin_class
specifier|public
class|class
name|FileOperations
block|{
specifier|private
specifier|static
name|Logger
name|logger
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|FileOperations
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Path
argument_list|>
name|dataPathList
decl_stmt|;
specifier|private
specifier|final
name|Path
name|exportRootDataDir
decl_stmt|;
specifier|private
specifier|final
name|String
name|distCpDoAsUser
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|FileSystem
name|exportFileSystem
decl_stmt|,
name|dataFileSystem
decl_stmt|;
specifier|private
specifier|final
name|MmContext
name|mmCtx
decl_stmt|;
specifier|public
name|FileOperations
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|dataPathList
parameter_list|,
name|Path
name|exportRootDataDir
parameter_list|,
name|String
name|distCpDoAsUser
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|,
name|MmContext
name|mmCtx
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|dataPathList
operator|=
name|dataPathList
expr_stmt|;
name|this
operator|.
name|exportRootDataDir
operator|=
name|exportRootDataDir
expr_stmt|;
name|this
operator|.
name|distCpDoAsUser
operator|=
name|distCpDoAsUser
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|this
operator|.
name|mmCtx
operator|=
name|mmCtx
expr_stmt|;
if|if
condition|(
operator|(
name|dataPathList
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|dataPathList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|dataFileSystem
operator|=
name|dataPathList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|dataFileSystem
operator|=
literal|null
expr_stmt|;
block|}
name|exportFileSystem
operator|=
name|exportRootDataDir
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|export
parameter_list|(
name|boolean
name|isExportTask
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|isExportTask
condition|)
block|{
name|copyFiles
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|validateSrcPathListExists
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * This writes the actual data in the exportRootDataDir from the source.    */
specifier|private
name|void
name|copyFiles
parameter_list|()
throws|throws
name|IOException
throws|,
name|LoginException
block|{
if|if
condition|(
name|mmCtx
operator|==
literal|null
condition|)
block|{
for|for
control|(
name|Path
name|dataPath
range|:
name|dataPathList
control|)
block|{
name|copyOneDataPath
argument_list|(
name|dataPath
argument_list|,
name|exportRootDataDir
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|copyMmPath
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|copyOneDataPath
parameter_list|(
name|Path
name|fromPath
parameter_list|,
name|Path
name|toPath
parameter_list|)
throws|throws
name|IOException
throws|,
name|LoginException
block|{
name|FileStatus
index|[]
name|fileStatuses
init|=
name|LoadSemanticAnalyzer
operator|.
name|matchFilesOrDir
argument_list|(
name|dataFileSystem
argument_list|,
name|fromPath
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|srcPaths
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|fileStatus
range|:
name|fileStatuses
control|)
block|{
name|srcPaths
operator|.
name|add
argument_list|(
name|fileStatus
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
operator|new
name|CopyUtils
argument_list|(
name|distCpDoAsUser
argument_list|,
name|hiveConf
argument_list|,
name|toPath
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
operator|.
name|doCopy
argument_list|(
name|toPath
argument_list|,
name|srcPaths
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|copyMmPath
parameter_list|()
throws|throws
name|LoginException
throws|,
name|IOException
block|{
name|ValidWriteIdList
name|ids
init|=
name|AcidUtils
operator|.
name|getTableValidWriteIdList
argument_list|(
name|hiveConf
argument_list|,
name|mmCtx
operator|.
name|getFqTableName
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|fromPath
range|:
name|dataPathList
control|)
block|{
name|fromPath
operator|=
name|dataFileSystem
operator|.
name|makeQualified
argument_list|(
name|fromPath
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|validPaths
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|,
name|dirsWithOriginals
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|HiveInputFormat
operator|.
name|processPathsForMmRead
argument_list|(
name|dataPathList
argument_list|,
name|hiveConf
argument_list|,
name|ids
argument_list|,
name|validPaths
argument_list|,
name|dirsWithOriginals
argument_list|)
expr_stmt|;
name|String
name|fromPathStr
init|=
name|fromPath
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|fromPathStr
operator|.
name|endsWith
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
condition|)
block|{
name|fromPathStr
operator|+=
name|Path
operator|.
name|SEPARATOR
expr_stmt|;
block|}
for|for
control|(
name|Path
name|validPath
range|:
name|validPaths
control|)
block|{
comment|// Export valid directories with a modified name so they don't look like bases/deltas.
comment|// We could also dump the delta contents all together and rename the files if names collide.
name|String
name|mmChildPath
init|=
literal|"export_old_"
operator|+
name|validPath
operator|.
name|toString
argument_list|()
operator|.
name|substring
argument_list|(
name|fromPathStr
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|destPath
init|=
operator|new
name|Path
argument_list|(
name|exportRootDataDir
argument_list|,
name|mmChildPath
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|debug
argument_list|(
literal|"Exporting {} to {}"
argument_list|,
name|validPath
argument_list|,
name|destPath
argument_list|)
expr_stmt|;
name|exportFileSystem
operator|.
name|mkdirs
argument_list|(
name|destPath
argument_list|)
expr_stmt|;
name|copyOneDataPath
argument_list|(
name|validPath
argument_list|,
name|destPath
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Path
name|dirWithOriginals
range|:
name|dirsWithOriginals
control|)
block|{
name|FileStatus
index|[]
name|files
init|=
name|dataFileSystem
operator|.
name|listStatus
argument_list|(
name|dirWithOriginals
argument_list|,
name|AcidUtils
operator|.
name|hiddenFileFilter
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|srcPaths
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|fileStatus
range|:
name|files
control|)
block|{
if|if
condition|(
name|fileStatus
operator|.
name|isDirectory
argument_list|()
condition|)
continue|continue;
name|srcPaths
operator|.
name|add
argument_list|(
name|fileStatus
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|debug
argument_list|(
literal|"Exporting originals from {} to {}"
argument_list|,
name|dirWithOriginals
argument_list|,
name|exportRootDataDir
argument_list|)
expr_stmt|;
operator|new
name|CopyUtils
argument_list|(
name|distCpDoAsUser
argument_list|,
name|hiveConf
argument_list|,
name|exportRootDataDir
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
operator|.
name|doCopy
argument_list|(
name|exportRootDataDir
argument_list|,
name|srcPaths
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|Path
name|getPathWithSchemeAndAuthority
parameter_list|(
name|Path
name|targetFilePath
parameter_list|,
name|Path
name|currentFilePath
parameter_list|)
block|{
if|if
condition|(
name|targetFilePath
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|==
literal|null
condition|)
block|{
name|URI
name|currentURI
init|=
name|currentFilePath
operator|.
name|toUri
argument_list|()
decl_stmt|;
name|targetFilePath
operator|=
operator|new
name|Path
argument_list|(
name|currentURI
operator|.
name|getScheme
argument_list|()
argument_list|,
name|currentURI
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|targetFilePath
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|targetFilePath
return|;
block|}
specifier|private
name|FileStatus
index|[]
name|listFilesInDir
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|dataFileSystem
operator|.
name|listStatus
argument_list|(
name|path
argument_list|,
name|p
lambda|->
block|{
name|String
name|name
init|=
name|p
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
operator|&&
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"."
argument_list|)
return|;
block|}
argument_list|)
return|;
block|}
comment|/**    * Since the bootstrap will do table directory level copy, need to check for existence of src path.    */
specifier|private
name|void
name|validateSrcPathListExists
parameter_list|()
throws|throws
name|IOException
throws|,
name|LoginException
block|{
if|if
condition|(
name|dataPathList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
try|try
block|{
for|for
control|(
name|Path
name|dataPath
range|:
name|dataPathList
control|)
block|{
name|listFilesInDir
argument_list|(
name|dataPath
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"exporting data files in dir : "
operator|+
name|dataPathList
operator|+
literal|" to "
operator|+
name|exportRootDataDir
operator|+
literal|" failed"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
name|FILE_NOT_FOUND
operator|.
name|format
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

