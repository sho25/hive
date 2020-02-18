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
name|ddl
operator|.
name|table
operator|.
name|storage
operator|.
name|archive
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
name|ql
operator|.
name|ddl
operator|.
name|DDLOperationContext
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
name|ArchiveUtils
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
name|HdfsUtils
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
name|ArchiveUtils
operator|.
name|PartSpecInfo
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
name|ddl
operator|.
name|table
operator|.
name|storage
operator|.
name|archive
operator|.
name|AlterTableArchiveUtils
operator|.
name|ARCHIVE_NAME
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|TableType
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
name|ddl
operator|.
name|DDLOperation
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
name|mapreduce
operator|.
name|MRJobConfig
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
name|tools
operator|.
name|HadoopArchives
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
name|ToolRunner
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
name|ImmutableList
import|;
end_import

begin_comment
comment|/**  * Operation process of archiving a table.  */
end_comment

begin_class
specifier|public
class|class
name|AlterTableArchiveOperation
extends|extends
name|DDLOperation
argument_list|<
name|AlterTableArchiveDesc
argument_list|>
block|{
specifier|public
name|AlterTableArchiveOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|AlterTableArchiveDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
name|Table
name|table
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|table
operator|.
name|getTableType
argument_list|()
operator|!=
name|TableType
operator|.
name|MANAGED_TABLE
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"ARCHIVE can only be performed on managed tables"
argument_list|)
throw|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
init|=
name|desc
operator|.
name|getPartitionSpec
argument_list|()
decl_stmt|;
name|PartSpecInfo
name|partitionSpecInfo
init|=
name|PartSpecInfo
operator|.
name|create
argument_list|(
name|table
argument_list|,
name|partitionSpec
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getPartitions
argument_list|(
name|table
argument_list|,
name|partitionSpec
argument_list|)
decl_stmt|;
name|Path
name|originalDir
init|=
name|getOriginalDir
argument_list|(
name|table
argument_list|,
name|partitionSpecInfo
argument_list|,
name|partitions
argument_list|)
decl_stmt|;
name|Path
name|intermediateArchivedDir
init|=
name|AlterTableArchiveUtils
operator|.
name|getInterMediateDir
argument_list|(
name|originalDir
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|ConfVars
operator|.
name|METASTORE_INT_ARCHIVED
argument_list|)
decl_stmt|;
name|Path
name|intermediateOriginalDir
init|=
name|AlterTableArchiveUtils
operator|.
name|getInterMediateDir
argument_list|(
name|originalDir
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|ConfVars
operator|.
name|METASTORE_INT_ORIGINAL
argument_list|)
decl_stmt|;
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"intermediate.archived is "
operator|+
name|intermediateArchivedDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"intermediate.original is "
operator|+
name|intermediateOriginalDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|checkIfAlreadyArchived
argument_list|(
name|partitionSpecInfo
argument_list|,
name|partitions
argument_list|)
expr_stmt|;
name|boolean
name|recovery
init|=
name|isRecovery
argument_list|(
name|intermediateArchivedDir
argument_list|,
name|intermediateOriginalDir
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|fs
operator|=
name|originalDir
operator|.
name|getFileSystem
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
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
name|e
argument_list|)
throw|;
block|}
comment|// The following steps seem roundabout, but they are meant to aid in recovery if a failure occurs and to keep a
comment|// consistent state in the FS
comment|// If the intermediate directory exists, we assume the dir is good to use as it's creation is atomic (move)
if|if
condition|(
operator|!
name|recovery
condition|)
block|{
name|Path
name|tmpPath
init|=
name|createArchiveInTmpDir
argument_list|(
name|table
argument_list|,
name|partitionSpecInfo
argument_list|,
name|originalDir
argument_list|)
decl_stmt|;
name|moveTmpDirToIntermediateDir
argument_list|(
name|intermediateArchivedDir
argument_list|,
name|fs
argument_list|,
name|tmpPath
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|HdfsUtils
operator|.
name|pathExists
argument_list|(
name|intermediateArchivedDir
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Intermediate archive directory "
operator|+
name|intermediateArchivedDir
operator|+
literal|" already exists. Assuming it contains an archived version of the partition"
argument_list|)
expr_stmt|;
block|}
block|}
name|moveOriginalDirToIntermediateDir
argument_list|(
name|originalDir
argument_list|,
name|intermediateOriginalDir
argument_list|,
name|fs
argument_list|)
expr_stmt|;
comment|// If there's a failure from here to when the metadata is updated, there will be no data in the partition, or an
comment|// error while trying to read the partition (if the archive files have been moved to the original partition
comment|// directory.) But re-running the archive command will allow recovery
name|moveIntermediateArchivedDirToOriginalParent
argument_list|(
name|originalDir
argument_list|,
name|intermediateArchivedDir
argument_list|,
name|fs
argument_list|)
expr_stmt|;
name|writeArchivationToMetastore
argument_list|(
name|partitionSpecInfo
argument_list|,
name|partitions
argument_list|,
name|originalDir
argument_list|)
expr_stmt|;
comment|// If a failure occurs here, the directory containing the original files will not be deleted. The user will run
comment|// ARCHIVE again to clear this up. The presence of these files are used to indicate whether the original partition
comment|// directory contains archived or unarchived files.
name|deleteIntermediateOriginalDir
argument_list|(
name|table
argument_list|,
name|intermediateOriginalDir
argument_list|)
expr_stmt|;
if|if
condition|(
name|recovery
condition|)
block|{
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Recovery after ARCHIVE succeeded"
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|Path
name|getOriginalDir
parameter_list|(
name|Table
name|table
parameter_list|,
name|PartSpecInfo
name|partitionSpecInfo
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// when we have partial partitions specification we must assume partitions lie in standard place -
comment|// if they were in custom locations putting them into one archive would involve mass amount of copying
comment|// in full partition specification case we allow custom locations to keep backward compatibility
if|if
condition|(
name|partitions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"No partition matches the specification"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|partitionSpecInfo
operator|.
name|values
operator|.
name|size
argument_list|()
operator|!=
name|table
operator|.
name|getPartCols
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// for partial specifications we need partitions to follow the scheme
for|for
control|(
name|Partition
name|partition
range|:
name|partitions
control|)
block|{
if|if
condition|(
name|AlterTableArchiveUtils
operator|.
name|partitionInCustomLocation
argument_list|(
name|table
argument_list|,
name|partition
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"ARCHIVE cannot run for partition groups with custom locations like %s"
argument_list|,
name|partition
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
block|}
return|return
name|partitionSpecInfo
operator|.
name|createPath
argument_list|(
name|table
argument_list|)
return|;
block|}
else|else
block|{
name|Partition
name|p
init|=
name|partitions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// partition can be archived if during recovery
if|if
condition|(
name|ArchiveUtils
operator|.
name|isArchived
argument_list|(
name|p
argument_list|)
condition|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|AlterTableArchiveUtils
operator|.
name|getOriginalLocation
argument_list|(
name|p
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|p
operator|.
name|getDataLocation
argument_list|()
return|;
block|}
block|}
block|}
specifier|private
name|void
name|checkIfAlreadyArchived
parameter_list|(
name|PartSpecInfo
name|partitionSpecInfo
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// we checked if partitions matching specification are marked as archived in the metadata; if they are then
comment|// throw an exception
for|for
control|(
name|Partition
name|partition
range|:
name|partitions
control|)
block|{
if|if
condition|(
name|ArchiveUtils
operator|.
name|isArchived
argument_list|(
name|partition
argument_list|)
condition|)
block|{
if|if
condition|(
name|ArchiveUtils
operator|.
name|getArchivingLevel
argument_list|(
name|partition
argument_list|)
operator|!=
name|partitionSpecInfo
operator|.
name|values
operator|.
name|size
argument_list|()
condition|)
block|{
name|String
name|name
init|=
name|ArchiveUtils
operator|.
name|getPartialName
argument_list|(
name|partition
argument_list|,
name|ArchiveUtils
operator|.
name|getArchivingLevel
argument_list|(
name|partition
argument_list|)
argument_list|)
decl_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Conflict with existing archive %s"
argument_list|,
name|name
argument_list|)
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Partition(s) already archived"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
specifier|private
name|boolean
name|isRecovery
parameter_list|(
name|Path
name|intermediateArchivedDir
parameter_list|,
name|Path
name|intermediateOriginalDir
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|HdfsUtils
operator|.
name|pathExists
argument_list|(
name|intermediateArchivedDir
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
operator|||
name|HdfsUtils
operator|.
name|pathExists
argument_list|(
name|intermediateOriginalDir
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Starting recovery after failed ARCHIVE"
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|Path
name|createArchiveInTmpDir
parameter_list|(
name|Table
name|table
parameter_list|,
name|PartSpecInfo
name|partitionSpecInfo
parameter_list|,
name|Path
name|originalDir
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// First create the archive in a tmp dir so that if the job fails, the bad files don't pollute the filesystem
name|Path
name|tmpPath
init|=
operator|new
name|Path
argument_list|(
name|context
operator|.
name|getContext
argument_list|()
operator|.
name|getExternalTmpPath
argument_list|(
name|originalDir
argument_list|)
argument_list|,
literal|"partlevel"
argument_list|)
decl_stmt|;
comment|// Create the Hadoop archive
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Creating "
operator|+
name|ARCHIVE_NAME
operator|+
literal|" for "
operator|+
name|originalDir
operator|.
name|toString
argument_list|()
operator|+
literal|" in "
operator|+
name|tmpPath
argument_list|)
expr_stmt|;
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Please wait... (this may take a while)"
argument_list|)
expr_stmt|;
try|try
block|{
name|int
name|maxJobNameLength
init|=
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEJOBNAMELENGTH
argument_list|)
decl_stmt|;
name|String
name|jobName
init|=
name|String
operator|.
name|format
argument_list|(
literal|"Archiving %s@%s"
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partitionSpecInfo
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|jobName
operator|=
name|Utilities
operator|.
name|abbreviate
argument_list|(
name|jobName
argument_list|,
name|maxJobNameLength
operator|-
literal|6
argument_list|)
expr_stmt|;
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|set
argument_list|(
name|MRJobConfig
operator|.
name|JOB_NAME
argument_list|,
name|jobName
argument_list|)
expr_stmt|;
name|HadoopArchives
name|har
init|=
operator|new
name|HadoopArchives
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|args
init|=
name|ImmutableList
operator|.
name|of
argument_list|(
literal|"-archiveName"
argument_list|,
name|ARCHIVE_NAME
argument_list|,
literal|"-p"
argument_list|,
name|originalDir
operator|.
name|toString
argument_list|()
argument_list|,
name|tmpPath
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|har
argument_list|,
name|args
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Error while creating HAR"
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|tmpPath
return|;
block|}
comment|/**    * Move from the tmp dir to an intermediate directory, in the same level as the partition directory.    * e.g. .../hr=12-intermediate-archived    */
specifier|private
name|void
name|moveTmpDirToIntermediateDir
parameter_list|(
name|Path
name|intermediateArchivedDir
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|tmpPath
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Moving "
operator|+
name|tmpPath
operator|+
literal|" to "
operator|+
name|intermediateArchivedDir
argument_list|)
expr_stmt|;
if|if
condition|(
name|HdfsUtils
operator|.
name|pathExists
argument_list|(
name|intermediateArchivedDir
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"The intermediate archive directory already exists."
argument_list|)
throw|;
block|}
name|fs
operator|.
name|rename
argument_list|(
name|tmpPath
argument_list|,
name|intermediateArchivedDir
argument_list|)
expr_stmt|;
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
literal|"Error while moving tmp directory"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Move the original parent directory to the intermediate original directory if the move hasn't been made already.    */
specifier|private
name|void
name|moveOriginalDirToIntermediateDir
parameter_list|(
name|Path
name|originalDir
parameter_list|,
name|Path
name|intermediateOriginalDir
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// If we get to here, we know that we've archived the partition files, but they may be in the original partition
comment|// location, or in the intermediate original dir.
if|if
condition|(
operator|!
name|HdfsUtils
operator|.
name|pathExists
argument_list|(
name|intermediateOriginalDir
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Moving "
operator|+
name|originalDir
operator|+
literal|" to "
operator|+
name|intermediateOriginalDir
argument_list|)
expr_stmt|;
name|moveDir
argument_list|(
name|fs
argument_list|,
name|originalDir
argument_list|,
name|intermediateOriginalDir
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
name|intermediateOriginalDir
operator|+
literal|" already exists. "
operator|+
literal|"Assuming it contains the original files in the partition"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Move the intermediate archived directory to the original parent directory.    */
specifier|private
name|void
name|moveIntermediateArchivedDirToOriginalParent
parameter_list|(
name|Path
name|originalDir
parameter_list|,
name|Path
name|intermediateArchivedDir
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|HdfsUtils
operator|.
name|pathExists
argument_list|(
name|originalDir
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Moving "
operator|+
name|intermediateArchivedDir
operator|+
literal|" to "
operator|+
name|originalDir
argument_list|)
expr_stmt|;
name|moveDir
argument_list|(
name|fs
argument_list|,
name|intermediateArchivedDir
argument_list|,
name|originalDir
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
name|originalDir
operator|+
literal|" already exists. Assuming it contains the archived version of "
operator|+
literal|"the partition"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|moveDir
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|from
parameter_list|,
name|Path
name|to
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|from
argument_list|,
name|to
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Moving "
operator|+
name|from
operator|+
literal|" to "
operator|+
name|to
operator|+
literal|" failed!"
argument_list|)
throw|;
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
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Record the previous changes in the metastore.    */
specifier|private
name|void
name|writeArchivationToMetastore
parameter_list|(
name|PartSpecInfo
name|partitionSpecInfo
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|,
name|Path
name|originalDir
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|URI
name|archiveUri
init|=
operator|new
name|Path
argument_list|(
name|originalDir
argument_list|,
name|ARCHIVE_NAME
argument_list|)
operator|.
name|toUri
argument_list|()
decl_stmt|;
name|URI
name|originalUri
init|=
name|ArchiveUtils
operator|.
name|addSlash
argument_list|(
name|originalDir
operator|.
name|toUri
argument_list|()
argument_list|)
decl_stmt|;
name|ArchiveUtils
operator|.
name|HarPathHelper
name|harHelper
init|=
operator|new
name|ArchiveUtils
operator|.
name|HarPathHelper
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|archiveUri
argument_list|,
name|originalUri
argument_list|)
decl_stmt|;
for|for
control|(
name|Partition
name|partition
range|:
name|partitions
control|)
block|{
name|URI
name|originalPartitionUri
init|=
name|ArchiveUtils
operator|.
name|addSlash
argument_list|(
name|partition
operator|.
name|getDataLocation
argument_list|()
operator|.
name|toUri
argument_list|()
argument_list|)
decl_stmt|;
name|URI
name|harPartitionDir
init|=
name|harHelper
operator|.
name|getHarUri
argument_list|(
name|originalPartitionUri
argument_list|)
decl_stmt|;
name|StringBuilder
name|authority
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|harPartitionDir
operator|.
name|getUserInfo
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|authority
operator|.
name|append
argument_list|(
name|harPartitionDir
operator|.
name|getUserInfo
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"@"
argument_list|)
expr_stmt|;
block|}
name|authority
operator|.
name|append
argument_list|(
name|harPartitionDir
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|harPartitionDir
operator|.
name|getPort
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
name|authority
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
operator|.
name|append
argument_list|(
name|harPartitionDir
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// make in Path to ensure no slash at the end
name|Path
name|harPath
init|=
operator|new
name|Path
argument_list|(
name|harPartitionDir
operator|.
name|getScheme
argument_list|()
argument_list|,
name|authority
operator|.
name|toString
argument_list|()
argument_list|,
name|harPartitionDir
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|setArchived
argument_list|(
name|partition
argument_list|,
name|harPath
argument_list|,
name|partitionSpecInfo
operator|.
name|values
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO: catalog
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|alterPartition
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partition
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unable to change the partition info for HAR"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Sets the appropriate attributes in the supplied Partition object to mark    * it as archived. Note that the metastore is not touched - a separate    * call to alter_partition is needed.    *    * @param p - the partition object to modify    * @param harPath - new location of partition (har schema URI)    */
specifier|private
name|void
name|setArchived
parameter_list|(
name|Partition
name|p
parameter_list|,
name|Path
name|harPath
parameter_list|,
name|int
name|level
parameter_list|)
block|{
assert|assert
operator|(
operator|!
name|ArchiveUtils
operator|.
name|isArchived
argument_list|(
name|p
argument_list|)
operator|)
assert|;
name|AlterTableArchiveUtils
operator|.
name|setIsArchived
argument_list|(
name|p
argument_list|,
literal|true
argument_list|,
name|level
argument_list|)
expr_stmt|;
name|AlterTableArchiveUtils
operator|.
name|setOriginalLocation
argument_list|(
name|p
argument_list|,
name|p
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|p
operator|.
name|setLocation
argument_list|(
name|harPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|deleteIntermediateOriginalDir
parameter_list|(
name|Table
name|table
parameter_list|,
name|Path
name|intermediateOriginalDir
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|HdfsUtils
operator|.
name|pathExists
argument_list|(
name|intermediateOriginalDir
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
name|boolean
name|shouldEnableCm
init|=
name|ReplChangeManager
operator|.
name|shouldEnableCm
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getDatabase
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|)
argument_list|,
name|table
operator|.
name|getTTable
argument_list|()
argument_list|)
decl_stmt|;
name|AlterTableArchiveUtils
operator|.
name|deleteDir
argument_list|(
name|intermediateOriginalDir
argument_list|,
name|shouldEnableCm
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

