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
name|exec
package|;
end_package

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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|JavaUtils
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
name|fs
operator|.
name|PathFilter
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

begin_comment
comment|/**  * CopyTask implementation.  **/
end_comment

begin_class
specifier|public
class|class
name|CopyTask
extends|extends
name|Task
argument_list|<
name|CopyWork
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
name|CopyTask
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|CopyTask
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|Path
index|[]
name|from
init|=
name|work
operator|.
name|getFromPaths
argument_list|()
decl_stmt|,
name|to
init|=
name|work
operator|.
name|getToPaths
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|from
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|int
name|result
init|=
name|copyOnePath
argument_list|(
name|from
index|[
name|i
index|]
argument_list|,
name|to
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
return|return
name|result
return|;
block|}
return|return
literal|0
return|;
block|}
specifier|protected
name|int
name|copyOnePath
parameter_list|(
name|Path
name|fromPath
parameter_list|,
name|Path
name|toPath
parameter_list|)
block|{
name|FileSystem
name|dstFs
init|=
literal|null
decl_stmt|;
try|try
block|{
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
name|FileStatus
index|[]
name|srcs
init|=
name|matchFilesOrDir
argument_list|(
name|srcFs
argument_list|,
name|fromPath
argument_list|,
name|work
operator|.
name|doSkipSourceMmDirs
argument_list|()
argument_list|)
decl_stmt|;
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
if|if
condition|(
operator|!
name|FileUtils
operator|.
name|copy
argument_list|(
name|srcFs
argument_list|,
name|oneSrc
operator|.
name|getPath
argument_list|()
argument_list|,
name|dstFs
argument_list|,
name|toPath
argument_list|,
literal|false
argument_list|,
comment|// delete source
literal|true
argument_list|,
comment|// overwrite destination
name|conf
argument_list|)
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Failed to copy: '"
operator|+
name|oneSrc
operator|.
name|getPath
argument_list|()
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
comment|// Note: initially copied from LoadSemanticAnalyzer.
specifier|private
specifier|static
name|FileStatus
index|[]
name|matchFilesOrDir
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|boolean
name|isSourceMm
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
return|return
literal|null
return|;
if|if
condition|(
operator|!
name|isSourceMm
condition|)
return|return
name|matchFilesOneDir
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
literal|null
argument_list|)
return|;
comment|// TODO: this doesn't handle list bucketing properly. Does the original exim do that?
name|FileStatus
index|[]
name|mmDirs
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|path
argument_list|,
operator|new
name|JavaUtils
operator|.
name|AnyIdDirFilter
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|mmDirs
operator|==
literal|null
operator|||
name|mmDirs
operator|.
name|length
operator|==
literal|0
condition|)
return|return
literal|null
return|;
name|List
argument_list|<
name|FileStatus
argument_list|>
name|allFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|mmDir
range|:
name|mmDirs
control|)
block|{
name|Utilities
operator|.
name|LOG14535
operator|.
name|info
argument_list|(
literal|"Found source MM directory "
operator|+
name|mmDir
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|matchFilesOneDir
argument_list|(
name|fs
argument_list|,
name|mmDir
operator|.
name|getPath
argument_list|()
argument_list|,
name|allFiles
argument_list|)
expr_stmt|;
block|}
return|return
name|allFiles
operator|.
name|toArray
argument_list|(
operator|new
name|FileStatus
index|[
name|allFiles
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|FileStatus
index|[]
name|matchFilesOneDir
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|List
argument_list|<
name|FileStatus
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
index|[]
name|srcs
init|=
name|fs
operator|.
name|globStatus
argument_list|(
name|path
argument_list|,
operator|new
name|EximPathFilter
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|srcs
operator|!=
literal|null
operator|&&
name|srcs
operator|.
name|length
operator|==
literal|1
condition|)
block|{
if|if
condition|(
name|srcs
index|[
literal|0
index|]
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
name|srcs
operator|=
name|fs
operator|.
name|listStatus
argument_list|(
name|srcs
index|[
literal|0
index|]
operator|.
name|getPath
argument_list|()
argument_list|,
name|FileUtils
operator|.
name|HIDDEN_FILES_PATH_FILTER
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|result
operator|!=
literal|null
operator|&&
name|srcs
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|srcs
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|srcs
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|srcs
return|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|EximPathFilter
implements|implements
name|PathFilter
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
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
name|name
operator|.
name|equals
argument_list|(
literal|"_metadata"
argument_list|)
condition|?
literal|true
else|:
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
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"COPY"
return|;
block|}
block|}
end_class

end_unit

