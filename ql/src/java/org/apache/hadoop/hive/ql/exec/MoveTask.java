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
name|LinkedHashMap
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
name|Context
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
name|hooks
operator|.
name|LineageInfo
operator|.
name|DataContainer
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
name|DynamicPartitionCtx
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
name|LoadFileDesc
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
name|LoadMultiFilesDesc
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
name|LoadTableDesc
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
name|MoveWork
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
name|hive
operator|.
name|ql
operator|.
name|session
operator|.
name|SessionState
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
comment|/**  * MoveTask implementation.  **/
end_comment

begin_class
specifier|public
class|class
name|MoveTask
extends|extends
name|Task
argument_list|<
name|MoveWork
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
specifier|private
name|void
name|moveFile
parameter_list|(
name|Path
name|sourcePath
parameter_list|,
name|Path
name|targetPath
parameter_list|,
name|boolean
name|isDfsDir
parameter_list|)
throws|throws
name|Exception
block|{
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
name|isDfsDir
condition|)
block|{
comment|// Just do a rename on the URIs, they belong to the same FS
name|String
name|mesg
init|=
literal|"Moving data to: "
operator|+
name|targetPath
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|mesg_detail
init|=
literal|" from "
operator|+
name|sourcePath
operator|.
name|toString
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
block|{
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
block|{
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
block|}
else|else
block|{
comment|// This is a local file
name|String
name|mesg
init|=
literal|"Copying data to local directory "
operator|+
name|targetPath
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|mesg_detail
init|=
literal|" from "
operator|+
name|sourcePath
operator|.
name|toString
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
block|{
name|fs
operator|.
name|copyToLocalFile
argument_list|(
name|sourcePath
argument_list|,
name|targetPath
argument_list|)
expr_stmt|;
block|}
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
block|{
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
try|try
block|{
comment|// Do any hive related operations like moving tables and files
comment|// to appropriate locations
name|LoadFileDesc
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
name|moveFile
argument_list|(
name|sourcePath
argument_list|,
name|targetPath
argument_list|,
name|lfd
operator|.
name|getIsDfsDir
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Multi-file load is for dynamic partitions when some partitions do not
comment|// need to merge and they can simply be moved to the target directory.
name|LoadMultiFilesDesc
name|lmfd
init|=
name|work
operator|.
name|getLoadMultiFilesWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|lmfd
operator|!=
literal|null
condition|)
block|{
name|boolean
name|isDfsDir
init|=
name|lmfd
operator|.
name|getIsDfsDir
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|i
operator|<
name|lmfd
operator|.
name|getSourceDirs
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
name|Path
name|srcPath
init|=
operator|new
name|Path
argument_list|(
name|lmfd
operator|.
name|getSourceDirs
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|destPath
init|=
operator|new
name|Path
argument_list|(
name|lmfd
operator|.
name|getTargetDirs
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|destPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|destPath
operator|.
name|getParent
argument_list|()
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|destPath
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|moveFile
argument_list|(
name|srcPath
argument_list|,
name|destPath
argument_list|,
name|isDfsDir
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
block|}
comment|// Next we do this for tables and partitions
name|LoadTableDesc
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
name|StringBuilder
name|mesg
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Loading data to table "
argument_list|)
operator|.
name|append
argument_list|(
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
name|tbd
operator|.
name|getPartitionSpec
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|mesg
operator|.
name|append
argument_list|(
literal|" partition ("
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|tbd
operator|.
name|getPartitionSpec
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|partSpec
operator|.
name|keySet
argument_list|()
control|)
block|{
name|mesg
operator|.
name|append
argument_list|(
name|key
argument_list|)
operator|.
name|append
argument_list|(
literal|'='
argument_list|)
operator|.
name|append
argument_list|(
name|partSpec
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|mesg
operator|.
name|setLength
argument_list|(
name|mesg
operator|.
name|length
argument_list|()
operator|-
literal|2
argument_list|)
expr_stmt|;
name|mesg
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
block|}
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
operator|.
name|toString
argument_list|()
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
comment|// We only check one file, so exit the loop when we have at least
comment|// one.
if|if
condition|(
name|files
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
break|break;
block|}
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
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECHECKFILEFORMAT
argument_list|)
condition|)
block|{
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
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Wrong file format. Please check the file's format."
argument_list|)
throw|;
block|}
block|}
block|}
comment|// Create a data container
name|DataContainer
name|dc
init|=
literal|null
decl_stmt|;
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
name|dc
operator|=
operator|new
name|DataContainer
argument_list|(
name|table
operator|.
name|getTTable
argument_list|()
argument_list|)
expr_stmt|;
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
name|tbd
operator|.
name|getHoldDDLTime
argument_list|()
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
block|{
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
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
comment|// deal with dynamic partitions
name|DynamicPartitionCtx
name|dpCtx
init|=
name|tbd
operator|.
name|getDPCtx
argument_list|()
decl_stmt|;
if|if
condition|(
name|dpCtx
operator|!=
literal|null
operator|&&
name|dpCtx
operator|.
name|getNumDPCols
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// dynamic partitions
name|List
argument_list|<
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|dps
init|=
name|Utilities
operator|.
name|getFullDPSpecs
argument_list|(
name|conf
argument_list|,
name|dpCtx
argument_list|)
decl_stmt|;
comment|// publish DP columns to its subscribers
if|if
condition|(
name|dps
operator|!=
literal|null
operator|&&
name|dps
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|pushFeed
argument_list|(
name|FeedType
operator|.
name|DYNAMIC_PARTITIONS
argument_list|,
name|dps
argument_list|)
expr_stmt|;
block|}
comment|// load the list of DP partitions and return the list of partition specs
comment|// TODO: In a follow-up to HIVE-1361, we should refactor loadDynamicPartitions
comment|// to use Utilities.getFullDPSpecs() to get the list of full partSpecs.
comment|// After that check the number of DPs created to not exceed the limit and
comment|// iterate over it and call loadPartition() here.
comment|// The reason we don't do inside HIVE-1361 is the latter is large and we
comment|// want to isolate any potential issue it may introduce.
name|ArrayList
argument_list|<
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|dp
init|=
name|db
operator|.
name|loadDynamicPartitions
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
name|dpCtx
operator|.
name|getNumDPCols
argument_list|()
argument_list|,
name|tbd
operator|.
name|getHoldDDLTime
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|dp
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ERROR_ON_EMPTY_PARTITION
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"This query creates no partitions."
operator|+
literal|" To turn off this error, set hive.error.on.empty.partition=false."
argument_list|)
throw|;
block|}
comment|// for each partition spec, get the partition
comment|// and put it to WriteEntity for post-exec hook
for|for
control|(
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
range|:
name|dp
control|)
block|{
name|Partition
name|partn
init|=
name|db
operator|.
name|getPartition
argument_list|(
name|table
argument_list|,
name|partSpec
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|WriteEntity
name|enty
init|=
operator|new
name|WriteEntity
argument_list|(
name|partn
argument_list|,
literal|true
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
block|{
name|work
operator|.
name|getOutputs
argument_list|()
operator|.
name|add
argument_list|(
name|enty
argument_list|)
expr_stmt|;
block|}
comment|// Need to update the queryPlan's output as well so that post-exec hook get executed.
comment|// This is only needed for dynamic partitioning since for SP the the WriteEntity is
comment|// constructed at compile time and the queryPlan already contains that.
comment|// For DP, WriteEntity creation is deferred at this stage so we need to update
comment|// queryPlan here.
if|if
condition|(
name|queryPlan
operator|.
name|getOutputs
argument_list|()
operator|==
literal|null
condition|)
block|{
name|queryPlan
operator|.
name|setOutputs
argument_list|(
operator|new
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|queryPlan
operator|.
name|getOutputs
argument_list|()
operator|.
name|add
argument_list|(
name|enty
argument_list|)
expr_stmt|;
comment|// update columnar lineage for each partition
name|dc
operator|=
operator|new
name|DataContainer
argument_list|(
name|table
operator|.
name|getTTable
argument_list|()
argument_list|,
name|partn
operator|.
name|getTPartition
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getLineageState
argument_list|()
operator|.
name|setLineage
argument_list|(
name|tbd
operator|.
name|getSourceDir
argument_list|()
argument_list|,
name|dc
argument_list|,
name|table
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|console
operator|.
name|printInfo
argument_list|(
literal|"\tLoading partition "
operator|+
name|partSpec
argument_list|)
expr_stmt|;
block|}
name|dc
operator|=
literal|null
expr_stmt|;
comment|// reset data container to prevent it being added again.
block|}
else|else
block|{
comment|// static partitions
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
name|tbd
operator|.
name|getHoldDDLTime
argument_list|()
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
name|dc
operator|=
operator|new
name|DataContainer
argument_list|(
name|table
operator|.
name|getTTable
argument_list|()
argument_list|,
name|partn
operator|.
name|getTPartition
argument_list|()
argument_list|)
expr_stmt|;
comment|// add this partition to post-execution hook
if|if
condition|(
name|work
operator|.
name|getOutputs
argument_list|()
operator|!=
literal|null
condition|)
block|{
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
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
operator|&&
name|dc
operator|!=
literal|null
condition|)
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getLineageState
argument_list|()
operator|.
name|setLineage
argument_list|(
name|tbd
operator|.
name|getSourceDir
argument_list|()
argument_list|,
name|dc
argument_list|,
name|table
operator|.
name|getCols
argument_list|()
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
name|LoadTableDesc
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
return|return
literal|false
return|;
block|}
name|LoadFileDesc
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
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
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
name|MOVE
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
literal|"MOVE"
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|localizeMRTmpFilesImpl
parameter_list|(
name|Context
name|ctx
parameter_list|)
block|{
comment|// no-op
block|}
block|}
end_class

end_unit

