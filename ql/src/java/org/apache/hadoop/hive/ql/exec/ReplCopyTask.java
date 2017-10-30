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
package|;
end_package

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
name|metastore
operator|.
name|ReplChangeManager
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|EximUtil
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
name|ReplicationSpec
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
name|CopyWork
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
name|ReplCopyWork
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
name|io
operator|.
name|Serializable
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
name|DriverContext
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
name|plan
operator|.
name|api
operator|.
name|StageType
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_class
specifier|public
class|class
name|ReplCopyTask
extends|extends
name|Task
argument_list|<
name|ReplCopyWork
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
specifier|transient
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ReplCopyTask
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|ReplCopyTask
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"ReplCopyTask.execute()"
argument_list|)
expr_stmt|;
name|FileSystem
name|dstFs
init|=
literal|null
decl_stmt|;
name|Path
name|toPath
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Note: CopyWork supports copying multiple files, but ReplCopyWork doesn't.
comment|//       Not clear of ReplCopyWork should inherit from CopyWork.
if|if
condition|(
name|work
operator|.
name|getFromPaths
argument_list|()
operator|.
name|length
operator|>
literal|1
operator|||
name|work
operator|.
name|getToPaths
argument_list|()
operator|.
name|length
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid ReplCopyWork: "
operator|+
name|work
operator|.
name|getFromPaths
argument_list|()
operator|+
literal|", "
operator|+
name|work
operator|.
name|getToPaths
argument_list|()
argument_list|)
throw|;
block|}
name|Path
name|fromPath
init|=
name|work
operator|.
name|getFromPaths
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|toPath
operator|=
name|work
operator|.
name|getToPaths
argument_list|()
index|[
literal|0
index|]
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Copying data from "
operator|+
name|fromPath
operator|.
name|toString
argument_list|()
argument_list|,
literal|" to "
operator|+
name|toPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ReplCopyWork
name|rwork
init|=
operator|(
operator|(
name|ReplCopyWork
operator|)
name|work
operator|)
decl_stmt|;
name|FileSystem
name|srcFs
init|=
name|fromPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|dstFs
operator|=
name|toPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// This should only be true for copy tasks created from functions, otherwise there should never
comment|// be a CM uri in the from path.
if|if
condition|(
name|ReplChangeManager
operator|.
name|isCMFileUri
argument_list|(
name|fromPath
argument_list|,
name|srcFs
argument_list|)
condition|)
block|{
name|String
index|[]
name|result
init|=
name|ReplChangeManager
operator|.
name|getFileWithChksumFromURI
argument_list|(
name|fromPath
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|ReplChangeManager
operator|.
name|FileInfo
name|sourceInfo
init|=
name|ReplChangeManager
operator|.
name|getFileInfo
argument_list|(
operator|new
name|Path
argument_list|(
name|result
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|result
index|[
literal|1
index|]
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|FileUtils
operator|.
name|copy
argument_list|(
name|sourceInfo
operator|.
name|getSrcFs
argument_list|()
argument_list|,
name|sourceInfo
operator|.
name|getSourcePath
argument_list|()
argument_list|,
name|dstFs
argument_list|,
name|toPath
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|conf
argument_list|)
condition|)
block|{
return|return
literal|0
return|;
block|}
else|else
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Failed to copy: '"
operator|+
name|fromPath
operator|.
name|toString
argument_list|()
operator|+
literal|"to: '"
operator|+
name|toPath
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
name|List
argument_list|<
name|ReplChangeManager
operator|.
name|FileInfo
argument_list|>
name|srcFiles
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|rwork
operator|.
name|readSrcAsFilesList
argument_list|()
condition|)
block|{
comment|// This flow is usually taken for REPL LOAD
comment|// Our input is the result of a _files listing, we should expand out _files.
name|srcFiles
operator|=
name|filesInFileListing
argument_list|(
name|srcFs
argument_list|,
name|fromPath
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"ReplCopyTask _files contains: {}"
argument_list|,
operator|(
name|srcFiles
operator|==
literal|null
condition|?
literal|"null"
else|:
name|srcFiles
operator|.
name|size
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|srcFiles
operator|==
literal|null
operator|)
operator|||
operator|(
name|srcFiles
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
if|if
condition|(
name|work
operator|.
name|isErrorOnSrcEmpty
argument_list|()
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"No _files entry found on source: "
operator|+
name|fromPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|5
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
block|}
else|else
block|{
comment|// This flow is usually taken for IMPORT command
name|FileStatus
index|[]
name|srcs
init|=
name|LoadSemanticAnalyzer
operator|.
name|matchFilesOrDir
argument_list|(
name|srcFs
argument_list|,
name|fromPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"ReplCopyTasks srcs= {}"
argument_list|,
operator|(
name|srcs
operator|==
literal|null
condition|?
literal|"null"
else|:
name|srcs
operator|.
name|length
operator|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|srcs
operator|==
literal|null
operator|||
name|srcs
operator|.
name|length
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|work
operator|.
name|isErrorOnSrcEmpty
argument_list|()
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"No files matching path: "
operator|+
name|fromPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|3
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
for|for
control|(
name|FileStatus
name|oneSrc
range|:
name|srcs
control|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Copying file: "
operator|+
name|oneSrc
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"ReplCopyTask :cp:{}=>{}"
argument_list|,
name|oneSrc
operator|.
name|getPath
argument_list|()
argument_list|,
name|toPath
argument_list|)
expr_stmt|;
name|srcFiles
operator|.
name|add
argument_list|(
operator|new
name|ReplChangeManager
operator|.
name|FileInfo
argument_list|(
name|oneSrc
operator|.
name|getPath
argument_list|()
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
argument_list|,
name|oneSrc
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"ReplCopyTask numFiles: {}"
argument_list|,
name|srcFiles
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|FileUtils
operator|.
name|mkdir
argument_list|(
name|dstFs
argument_list|,
name|toPath
argument_list|,
name|conf
argument_list|)
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Cannot make target directory: "
operator|+
name|toPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|2
return|;
block|}
comment|// Copy the files from different source file systems to one destination directory
operator|new
name|CopyUtils
argument_list|(
name|rwork
operator|.
name|distCpDoAsUser
argument_list|()
argument_list|,
name|conf
argument_list|)
operator|.
name|copyAndVerify
argument_list|(
name|dstFs
argument_list|,
name|toPath
argument_list|,
name|srcFiles
argument_list|)
expr_stmt|;
comment|// If a file is copied from CM path, then need to rename them using original source file name
comment|// This is needed to avoid having duplicate files in target if same event is applied twice
comment|// where the first event refers to source path and  second event refers to CM path
for|for
control|(
name|ReplChangeManager
operator|.
name|FileInfo
name|srcFile
range|:
name|srcFiles
control|)
block|{
if|if
condition|(
name|srcFile
operator|.
name|isUseSourcePath
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|String
name|destFileName
init|=
name|srcFile
operator|.
name|getCmPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Path
name|destFile
init|=
operator|new
name|Path
argument_list|(
name|toPath
argument_list|,
name|destFileName
argument_list|)
decl_stmt|;
if|if
condition|(
name|dstFs
operator|.
name|exists
argument_list|(
name|destFile
argument_list|)
condition|)
block|{
name|String
name|destFileWithSourceName
init|=
name|srcFile
operator|.
name|getSourcePath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Path
name|newDestFile
init|=
operator|new
name|Path
argument_list|(
name|toPath
argument_list|,
name|destFileWithSourceName
argument_list|)
decl_stmt|;
name|boolean
name|result
init|=
name|dstFs
operator|.
name|rename
argument_list|(
name|destFile
argument_list|,
name|newDestFile
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|result
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"could not rename "
operator|+
name|destFile
operator|.
name|getName
argument_list|()
operator|+
literal|" to "
operator|+
name|newDestFile
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
return|return
literal|0
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Failed with exception "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"\n"
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|(
literal|1
operator|)
return|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|ReplChangeManager
operator|.
name|FileInfo
argument_list|>
name|filesInFileListing
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|dataPath
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|fileListing
init|=
operator|new
name|Path
argument_list|(
name|dataPath
argument_list|,
name|EximUtil
operator|.
name|FILES_NAME
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"ReplCopyTask filesInFileListing() reading {}"
argument_list|,
name|fileListing
operator|.
name|toUri
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|fileListing
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"ReplCopyTask : _files does not exist"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
comment|// Returning null from this fn can serve as an err condition.
comment|// On success, but with nothing to return, we can return an empty list.
block|}
name|List
argument_list|<
name|ReplChangeManager
operator|.
name|FileInfo
argument_list|>
name|filePaths
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|BufferedReader
name|br
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|fs
operator|.
name|open
argument_list|(
name|fileListing
argument_list|)
argument_list|)
argument_list|)
init|)
block|{
comment|// TODO : verify if skipping charset here is okay
name|String
name|line
init|=
literal|null
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"ReplCopyTask :_filesReadLine: {}"
argument_list|,
name|line
argument_list|)
expr_stmt|;
name|String
index|[]
name|fileWithChksum
init|=
name|ReplChangeManager
operator|.
name|getFileWithChksumFromURI
argument_list|(
name|line
argument_list|)
decl_stmt|;
try|try
block|{
name|ReplChangeManager
operator|.
name|FileInfo
name|f
init|=
name|ReplChangeManager
operator|.
name|getFileInfo
argument_list|(
operator|new
name|Path
argument_list|(
name|fileWithChksum
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|fileWithChksum
index|[
literal|1
index|]
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|filePaths
operator|.
name|add
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
comment|// issue warning for missing file and throw exception
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cannot find {} in source repo or cmroot"
argument_list|,
name|fileWithChksum
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
comment|// Note - we need srcFs rather than fs, because it is possible that the _files lists files
comment|// which are from a different filesystem than the fs where the _files file itself was loaded
comment|// from. Currently, it is possible, for eg., to do REPL LOAD hdfs://<ip>/dir/ and for the _files
comment|// in it to contain hdfs://<name>/ entries, and/or vice-versa, and this causes errors.
comment|// It might also be possible that there will be a mix of them in a given _files file.
comment|// TODO: revisit close to the end of replv2 dev, to see if our assumption now still holds,
comment|// and if not so, optimize.
block|}
block|}
return|return
name|filePaths
return|;
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|COPY
return|;
comment|// there's no extensive need for this to have its own type - it mirrors
comment|// the intent of copy enough. This might change later, though.
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"REPL_COPY"
return|;
block|}
specifier|public
specifier|static
name|Task
argument_list|<
name|?
argument_list|>
name|getLoadCopyTask
parameter_list|(
name|ReplicationSpec
name|replicationSpec
parameter_list|,
name|Path
name|srcPath
parameter_list|,
name|Path
name|dstPath
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
name|Task
argument_list|<
name|?
argument_list|>
name|copyTask
init|=
literal|null
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"ReplCopyTask:getLoadCopyTask: {}=>{}"
argument_list|,
name|srcPath
argument_list|,
name|dstPath
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|replicationSpec
operator|!=
literal|null
operator|)
operator|&&
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
condition|)
block|{
name|ReplCopyWork
name|rcwork
init|=
operator|new
name|ReplCopyWork
argument_list|(
name|srcPath
argument_list|,
name|dstPath
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"ReplCopyTask:\trcwork"
argument_list|)
expr_stmt|;
if|if
condition|(
name|replicationSpec
operator|.
name|isLazy
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"ReplCopyTask:\tlazy"
argument_list|)
expr_stmt|;
name|rcwork
operator|.
name|setReadSrcAsFilesList
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// It is assumed isLazy flag is set only for REPL LOAD flow.
comment|// IMPORT always do deep copy. So, distCpDoAsUser will be null by default in ReplCopyWork.
name|String
name|distCpDoAsUser
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_DISTCP_DOAS_USER
argument_list|)
decl_stmt|;
name|rcwork
operator|.
name|setDistCpDoAsUser
argument_list|(
name|distCpDoAsUser
argument_list|)
expr_stmt|;
block|}
name|copyTask
operator|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|rcwork
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"ReplCopyTask:\tcwork"
argument_list|)
expr_stmt|;
name|copyTask
operator|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|CopyWork
argument_list|(
name|srcPath
argument_list|,
name|dstPath
argument_list|,
literal|false
argument_list|)
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
name|copyTask
return|;
block|}
block|}
end_class

end_unit

