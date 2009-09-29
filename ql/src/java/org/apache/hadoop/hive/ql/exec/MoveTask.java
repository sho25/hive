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
name|security
operator|.
name|AccessControlException
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
name|Arrays
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
name|LocalFileSystem
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
name|metastore
operator|.
name|MetaStoreUtils
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
name|hooks
operator|.
name|WriteEntity
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
name|HiveFileFormatUtils
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
name|metadata
operator|.
name|HiveException
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
name|metadata
operator|.
name|Partition
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
name|metadata
operator|.
name|Table
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
name|loadFileDesc
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
name|loadTableDesc
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
name|moveWork
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
comment|/**  * MoveTask implementation  **/
end_comment

begin_class
specifier|public
class|class
name|MoveTask
extends|extends
name|Task
argument_list|<
name|moveWork
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
specifier|public
name|MoveTask
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|int
name|execute
parameter_list|()
block|{
try|try
block|{
comment|// Do any hive related operations like moving tables and files
comment|// to appropriate locations
name|loadFileDesc
name|lfd
init|=
name|work
operator|.
name|getLoadFileWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|lfd
operator|!=
literal|null
condition|)
block|{
name|Path
name|targetPath
init|=
operator|new
name|Path
argument_list|(
name|lfd
operator|.
name|getTargetDir
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|sourcePath
init|=
operator|new
name|Path
argument_list|(
name|lfd
operator|.
name|getSourceDir
argument_list|()
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|sourcePath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|lfd
operator|.
name|getIsDfsDir
argument_list|()
condition|)
block|{
comment|// Just do a rename on the URIs, they belong to the same FS
name|String
name|mesg
init|=
literal|"Moving data to: "
operator|+
name|lfd
operator|.
name|getTargetDir
argument_list|()
decl_stmt|;
name|String
name|mesg_detail
init|=
literal|" from "
operator|+
name|lfd
operator|.
name|getSourceDir
argument_list|()
decl_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|mesg
argument_list|,
name|mesg_detail
argument_list|)
expr_stmt|;
comment|// delete the output directory if it already exists
name|fs
operator|.
name|delete
argument_list|(
name|targetPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// if source exists, rename. Otherwise, create a empty directory
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|sourcePath
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|sourcePath
argument_list|,
name|targetPath
argument_list|)
condition|)
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unable to rename: "
operator|+
name|sourcePath
operator|+
literal|" to: "
operator|+
name|targetPath
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|targetPath
argument_list|)
condition|)
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unable to make directory: "
operator|+
name|targetPath
argument_list|)
throw|;
block|}
else|else
block|{
comment|// This is a local file
name|String
name|mesg
init|=
literal|"Copying data to local directory "
operator|+
name|lfd
operator|.
name|getTargetDir
argument_list|()
decl_stmt|;
name|String
name|mesg_detail
init|=
literal|" from "
operator|+
name|lfd
operator|.
name|getSourceDir
argument_list|()
decl_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|mesg
argument_list|,
name|mesg_detail
argument_list|)
expr_stmt|;
comment|// delete the existing dest directory
name|LocalFileSystem
name|dstFs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|dstFs
operator|.
name|delete
argument_list|(
name|targetPath
argument_list|,
literal|true
argument_list|)
operator|||
operator|!
name|dstFs
operator|.
name|exists
argument_list|(
name|targetPath
argument_list|)
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|mesg
argument_list|,
name|mesg_detail
argument_list|)
expr_stmt|;
comment|// if source exists, rename. Otherwise, create a empty directory
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|sourcePath
argument_list|)
condition|)
name|fs
operator|.
name|copyToLocalFile
argument_list|(
name|sourcePath
argument_list|,
name|targetPath
argument_list|)
expr_stmt|;
else|else
block|{
if|if
condition|(
operator|!
name|dstFs
operator|.
name|mkdirs
argument_list|(
name|targetPath
argument_list|)
condition|)
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unable to make local directory: "
operator|+
name|targetPath
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|AccessControlException
argument_list|(
literal|"Unable to delete the existing destination directory: "
operator|+
name|targetPath
argument_list|)
throw|;
block|}
block|}
block|}
comment|// Next we do this for tables and partitions
name|loadTableDesc
name|tbd
init|=
name|work
operator|.
name|getLoadTableWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|tbd
operator|!=
literal|null
condition|)
block|{
name|String
name|mesg
init|=
literal|"Loading data to table "
operator|+
name|tbd
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
operator|+
operator|(
operator|(
name|tbd
operator|.
name|getPartitionSpec
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|)
condition|?
literal|" partition "
operator|+
name|tbd
operator|.
name|getPartitionSpec
argument_list|()
operator|.
name|toString
argument_list|()
else|:
literal|""
operator|)
decl_stmt|;
name|String
name|mesg_detail
init|=
literal|" from "
operator|+
name|tbd
operator|.
name|getSourceDir
argument_list|()
decl_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|mesg
argument_list|,
name|mesg_detail
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|db
operator|.
name|getTable
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
name|tbd
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|work
operator|.
name|getCheckFileFormat
argument_list|()
condition|)
block|{
comment|// Get all files from the src directory
name|FileStatus
index|[]
name|dirs
decl_stmt|;
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
name|files
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
try|try
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|table
operator|.
name|getDataLocation
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|dirs
operator|=
name|fs
operator|.
name|globStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|tbd
operator|.
name|getSourceDir
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|files
operator|=
operator|new
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
operator|(
name|dirs
operator|!=
literal|null
operator|&&
name|i
operator|<
name|dirs
operator|.
name|length
operator|)
condition|;
name|i
operator|++
control|)
block|{
name|files
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|fs
operator|.
name|listStatus
argument_list|(
name|dirs
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// We only check one file, so exit the loop when we have at least one.
if|if
condition|(
name|files
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
break|break;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"addFiles: filesystem error in check phase"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// Check if the file format of the file matches that of the table.
name|boolean
name|flag
init|=
name|HiveFileFormatUtils
operator|.
name|checkInputFormat
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|tbd
operator|.
name|getTable
argument_list|()
operator|.
name|getInputFileFormatClass
argument_list|()
argument_list|,
name|files
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|flag
condition|)
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Wrong file format. Please check the file's format."
argument_list|)
throw|;
block|}
if|if
condition|(
name|tbd
operator|.
name|getPartitionSpec
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|db
operator|.
name|loadTable
argument_list|(
operator|new
name|Path
argument_list|(
name|tbd
operator|.
name|getSourceDir
argument_list|()
argument_list|)
argument_list|,
name|tbd
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|,
name|tbd
operator|.
name|getReplace
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
name|tbd
operator|.
name|getTmpDir
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|work
operator|.
name|getOutputs
argument_list|()
operator|!=
literal|null
condition|)
name|work
operator|.
name|getOutputs
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Partition is: "
operator|+
name|tbd
operator|.
name|getPartitionSpec
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|db
operator|.
name|loadPartition
argument_list|(
operator|new
name|Path
argument_list|(
name|tbd
operator|.
name|getSourceDir
argument_list|()
argument_list|)
argument_list|,
name|tbd
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|,
name|tbd
operator|.
name|getPartitionSpec
argument_list|()
argument_list|,
name|tbd
operator|.
name|getReplace
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
name|tbd
operator|.
name|getTmpDir
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Partition
name|partn
init|=
name|db
operator|.
name|getPartition
argument_list|(
name|table
argument_list|,
name|tbd
operator|.
name|getPartitionSpec
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|work
operator|.
name|getOutputs
argument_list|()
operator|!=
literal|null
condition|)
name|work
operator|.
name|getOutputs
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|partn
argument_list|)
argument_list|)
expr_stmt|;
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
comment|/*    * Does the move task involve moving to a local file system    */
specifier|public
name|boolean
name|isLocal
parameter_list|()
block|{
name|loadTableDesc
name|tbd
init|=
name|work
operator|.
name|getLoadTableWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|tbd
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
name|loadFileDesc
name|lfd
init|=
name|work
operator|.
name|getLoadFileWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|lfd
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|lfd
operator|.
name|getIsDfsDir
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|int
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|MOVE
return|;
block|}
block|}
end_class

end_unit

