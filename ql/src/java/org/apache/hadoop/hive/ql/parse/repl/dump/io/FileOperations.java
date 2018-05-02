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
name|parse
operator|.
name|SemanticException
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
name|java
operator|.
name|io
operator|.
name|BufferedWriter
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
name|OutputStreamWriter
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
specifier|final
name|FileSystem
name|dataFileSystem
decl_stmt|,
name|exportFileSystem
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
name|ReplicationSpec
name|forReplicationSpec
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|forReplicationSpec
operator|.
name|isLazy
argument_list|()
condition|)
block|{
name|exportFilesAsList
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|copyFiles
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
for|for
control|(
name|Path
name|dataPath
range|:
name|dataPathList
control|)
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
name|dataPath
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
comment|/**    * This needs the root data directory to which the data needs to be exported to.    * The data export here is a list of files either in table/partition that are written to the _files    * in the exportRootDataDir provided.    */
specifier|private
name|void
name|exportFilesAsList
parameter_list|()
throws|throws
name|SemanticException
throws|,
name|IOException
block|{
try|try
init|(
name|BufferedWriter
name|writer
init|=
name|writer
argument_list|()
init|)
block|{
for|for
control|(
name|Path
name|dataPath
range|:
name|dataPathList
control|)
block|{
name|writeFilesList
argument_list|(
name|listFilesInDir
argument_list|(
name|dataPath
argument_list|)
argument_list|,
name|writer
argument_list|,
name|AcidUtils
operator|.
name|getAcidSubDir
argument_list|(
name|dataPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|writeFilesList
parameter_list|(
name|FileStatus
index|[]
name|fileStatuses
parameter_list|,
name|BufferedWriter
name|writer
parameter_list|,
name|String
name|encodedSubDirs
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|FileStatus
name|fileStatus
range|:
name|fileStatuses
control|)
block|{
if|if
condition|(
name|fileStatus
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
comment|// Write files inside the sub-directory.
name|Path
name|subDir
init|=
name|fileStatus
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|writeFilesList
argument_list|(
name|listFilesInDir
argument_list|(
name|subDir
argument_list|)
argument_list|,
name|writer
argument_list|,
name|encodedSubDir
argument_list|(
name|encodedSubDirs
argument_list|,
name|subDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|writer
operator|.
name|write
argument_list|(
name|encodedUri
argument_list|(
name|fileStatus
argument_list|,
name|encodedSubDirs
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|newLine
argument_list|()
expr_stmt|;
block|}
block|}
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
specifier|private
name|BufferedWriter
name|writer
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|exportToFile
init|=
operator|new
name|Path
argument_list|(
name|exportRootDataDir
argument_list|,
name|EximUtil
operator|.
name|FILES_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|exportFileSystem
operator|.
name|exists
argument_list|(
name|exportToFile
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|exportToFile
operator|.
name|toString
argument_list|()
operator|+
literal|" already exists and cant export data from path(dir) "
operator|+
name|dataPathList
argument_list|)
throw|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"exporting data files in dir : "
operator|+
name|dataPathList
operator|+
literal|" to "
operator|+
name|exportToFile
argument_list|)
expr_stmt|;
return|return
operator|new
name|BufferedWriter
argument_list|(
operator|new
name|OutputStreamWriter
argument_list|(
name|exportFileSystem
operator|.
name|create
argument_list|(
name|exportToFile
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|String
name|encodedSubDir
parameter_list|(
name|String
name|encodedParentDirs
parameter_list|,
name|Path
name|subDir
parameter_list|)
block|{
if|if
condition|(
literal|null
operator|==
name|encodedParentDirs
condition|)
block|{
return|return
name|subDir
operator|.
name|getName
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|encodedParentDirs
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|subDir
operator|.
name|getName
argument_list|()
return|;
block|}
block|}
specifier|private
name|String
name|encodedUri
parameter_list|(
name|FileStatus
name|fileStatus
parameter_list|,
name|String
name|encodedSubDir
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|currentDataFilePath
init|=
name|fileStatus
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|String
name|checkSum
init|=
name|ReplChangeManager
operator|.
name|checksumFor
argument_list|(
name|currentDataFilePath
argument_list|,
name|dataFileSystem
argument_list|)
decl_stmt|;
return|return
name|ReplChangeManager
operator|.
name|encodeFileUri
argument_list|(
name|currentDataFilePath
operator|.
name|toString
argument_list|()
argument_list|,
name|checkSum
argument_list|,
name|encodedSubDir
argument_list|)
return|;
block|}
block|}
end_class

end_unit

