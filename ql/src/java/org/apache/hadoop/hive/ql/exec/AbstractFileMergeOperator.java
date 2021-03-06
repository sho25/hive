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
name|HashSet
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|ql
operator|.
name|CompilationOpContext
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
name|FileMergeDesc
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
name|JobConf
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Fast file merge operator for ORC and RCfile. This is an abstract class which  * does not process any rows. Refer {@link org.apache.hadoop.hive.ql.exec.OrcFileMergeOperator}  * or {@link org.apache.hadoop.hive.ql.exec.RCFileMergeOperator} for more details.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractFileMergeOperator
parameter_list|<
name|T
extends|extends
name|FileMergeDesc
parameter_list|>
extends|extends
name|Operator
argument_list|<
name|T
argument_list|>
implements|implements
name|Serializable
block|{
specifier|public
specifier|static
specifier|final
name|String
name|BACKUP_PREFIX
init|=
literal|"_backup."
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|UNION_SUDBIR_PREFIX
init|=
literal|"HIVE_UNION_SUBDIR_"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|AbstractFileMergeOperator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|JobConf
name|jc
decl_stmt|;
specifier|protected
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|boolean
name|autoDelete
decl_stmt|;
specifier|private
name|Path
name|outPath
decl_stmt|;
comment|// The output path used by the subclasses.
specifier|private
name|Path
name|finalPath
decl_stmt|;
comment|// Used as a final destination; same as outPath for MM tables.
specifier|private
name|Path
name|dpPath
decl_stmt|;
specifier|private
name|Path
name|tmpPath
decl_stmt|;
comment|// Only stored to update based on the original in fixTmpPath.
specifier|private
name|Path
name|taskTmpPath
decl_stmt|;
comment|// Only stored to update based on the original in fixTmpPath.
specifier|private
name|int
name|listBucketingDepth
decl_stmt|;
specifier|private
name|boolean
name|hasDynamicPartitions
decl_stmt|;
specifier|private
name|boolean
name|isListBucketingAlterTableConcatenate
decl_stmt|;
specifier|private
name|boolean
name|tmpPathFixedConcatenate
decl_stmt|;
specifier|private
name|boolean
name|tmpPathFixed
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|Path
argument_list|>
name|incompatFileSet
decl_stmt|;
specifier|private
specifier|transient
name|DynamicPartitionCtx
name|dpCtx
decl_stmt|;
specifier|private
name|boolean
name|isMmTable
decl_stmt|;
specifier|private
name|String
name|taskId
decl_stmt|;
comment|/** Kryo ctor. */
specifier|protected
name|AbstractFileMergeOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|AbstractFileMergeOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|this
operator|.
name|jc
operator|=
operator|new
name|JobConf
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|incompatFileSet
operator|=
operator|new
name|HashSet
argument_list|<
name|Path
argument_list|>
argument_list|()
expr_stmt|;
name|autoDelete
operator|=
literal|false
expr_stmt|;
name|tmpPathFixed
operator|=
literal|false
expr_stmt|;
name|tmpPathFixedConcatenate
operator|=
literal|false
expr_stmt|;
name|dpPath
operator|=
literal|null
expr_stmt|;
name|dpCtx
operator|=
name|conf
operator|.
name|getDpCtx
argument_list|()
expr_stmt|;
name|hasDynamicPartitions
operator|=
name|conf
operator|.
name|hasDynamicPartitions
argument_list|()
expr_stmt|;
name|isListBucketingAlterTableConcatenate
operator|=
name|conf
operator|.
name|isListBucketingAlterTableConcatenate
argument_list|()
expr_stmt|;
name|listBucketingDepth
operator|=
name|conf
operator|.
name|getListBucketingDepth
argument_list|()
expr_stmt|;
name|Path
name|specPath
init|=
name|conf
operator|.
name|getOutputPath
argument_list|()
decl_stmt|;
name|isMmTable
operator|=
name|conf
operator|.
name|getIsMmTable
argument_list|()
expr_stmt|;
if|if
condition|(
name|isMmTable
condition|)
block|{
name|updatePaths
argument_list|(
name|specPath
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|updatePaths
argument_list|(
name|Utilities
operator|.
name|toTempPath
argument_list|(
name|specPath
argument_list|)
argument_list|,
name|Utilities
operator|.
name|toTaskTempPath
argument_list|(
name|specPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|fs
operator|=
name|specPath
operator|.
name|getFileSystem
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isMmTable
condition|)
block|{
comment|// Do not delete for MM tables. We either want the file if we succeed, or we must
comment|// delete is explicitly before proceeding if the merge fails.
name|autoDelete
operator|=
name|fs
operator|.
name|deleteOnExit
argument_list|(
name|outPath
argument_list|)
expr_stmt|;
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
literal|"Failed to initialize AbstractFileMergeOperator"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|// sets up temp and task temp path
specifier|private
name|void
name|updatePaths
parameter_list|(
name|Path
name|tp
parameter_list|,
name|Path
name|ttp
parameter_list|)
block|{
name|taskId
operator|=
name|Utilities
operator|.
name|getTaskId
argument_list|(
name|jc
argument_list|)
expr_stmt|;
name|tmpPath
operator|=
name|tp
expr_stmt|;
if|if
condition|(
name|isMmTable
condition|)
block|{
name|taskTmpPath
operator|=
literal|null
expr_stmt|;
comment|// Make sure we don't collide with the source.
name|outPath
operator|=
name|finalPath
operator|=
operator|new
name|Path
argument_list|(
name|tmpPath
argument_list|,
name|taskId
operator|+
literal|".merged"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|taskTmpPath
operator|=
name|ttp
expr_stmt|;
name|finalPath
operator|=
operator|new
name|Path
argument_list|(
name|tp
argument_list|,
name|taskId
argument_list|)
expr_stmt|;
name|outPath
operator|=
operator|new
name|Path
argument_list|(
name|ttp
argument_list|,
name|Utilities
operator|.
name|toTempPath
argument_list|(
name|taskId
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|trace
argument_list|(
literal|"Paths for merge "
operator|+
name|taskId
operator|+
literal|": tmp "
operator|+
name|tmpPath
operator|+
literal|", task "
operator|+
name|taskTmpPath
operator|+
literal|", final "
operator|+
name|finalPath
operator|+
literal|", out "
operator|+
name|outPath
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Fixes tmpPath to point to the correct partition. Initialize operator will    * set tmpPath and taskTmpPath based on root table directory. So initially,    * tmpPath will be&lt;prefix&gt;/_tmp.-ext-10000 and taskTmpPath will be    *&lt;prefix&gt;/_task_tmp.-ext-10000. The depth of these two paths will be 0.    * Now, in case of dynamic partitioning or list bucketing the inputPath will    * have additional sub-directories under root table directory. This function    * updates the tmpPath and taskTmpPath to reflect these additional    * subdirectories. It updates tmpPath and taskTmpPath in the following way    * 1. finds out the difference in path based on depthDiff provided    * and saves the path difference in newPath    * 2. newPath is used to update the existing tmpPath and taskTmpPath similar    * to the way initializeOp() does.    *    * Note: The path difference between inputPath and tmpDepth can be DP or DP+LB.    * This method will automatically handle it.    *    * Continuing the example above, if inputPath is&lt;prefix&gt;/-ext-10000/hr=a1/,    * newPath will be hr=a1/. Then, tmpPath and taskTmpPath will be updated to    *&lt;prefix&gt;/-ext-10000/hr=a1/_tmp.ext-10000 and    *&lt;prefix&gt;/-ext-10000/hr=a1/_task_tmp.ext-10000 respectively.    * We have list_bucket_dml_6.q cover this case: DP + LP + multiple skewed    * values + merge.    *    * @param inputPath - input path    * @throws java.io.IOException    */
specifier|protected
name|void
name|fixTmpPath
parameter_list|(
name|Path
name|inputPath
parameter_list|,
name|int
name|depthDiff
parameter_list|)
throws|throws
name|IOException
block|{
comment|// don't need to update tmp paths when there is no depth difference in paths
if|if
condition|(
name|depthDiff
operator|<=
literal|0
condition|)
block|{
return|return;
block|}
name|dpPath
operator|=
name|inputPath
expr_stmt|;
name|Path
name|newPath
init|=
operator|new
name|Path
argument_list|(
literal|"."
argument_list|)
decl_stmt|;
comment|// Build the path from bottom up
while|while
condition|(
name|inputPath
operator|!=
literal|null
operator|&&
name|depthDiff
operator|>
literal|0
condition|)
block|{
name|newPath
operator|=
operator|new
name|Path
argument_list|(
name|inputPath
operator|.
name|getName
argument_list|()
argument_list|,
name|newPath
argument_list|)
expr_stmt|;
name|depthDiff
operator|--
expr_stmt|;
name|inputPath
operator|=
name|inputPath
operator|.
name|getParent
argument_list|()
expr_stmt|;
block|}
name|Path
name|newTmpPath
init|=
operator|new
name|Path
argument_list|(
name|tmpPath
argument_list|,
name|newPath
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|newTmpPath
argument_list|)
condition|)
block|{
if|if
condition|(
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|trace
argument_list|(
literal|"Creating "
operator|+
name|newTmpPath
argument_list|)
expr_stmt|;
block|}
name|fs
operator|.
name|mkdirs
argument_list|(
name|newTmpPath
argument_list|)
expr_stmt|;
block|}
name|Path
name|newTaskTmpPath
init|=
operator|(
name|taskTmpPath
operator|!=
literal|null
operator|)
condition|?
operator|new
name|Path
argument_list|(
name|taskTmpPath
argument_list|,
name|newPath
argument_list|)
else|:
literal|null
decl_stmt|;
name|updatePaths
argument_list|(
name|newTmpPath
argument_list|,
name|newTaskTmpPath
argument_list|)
expr_stmt|;
block|}
comment|/**    * Validates that each input path belongs to the same partition since each    * mapper merges the input to a single output directory    *    * @param inputPath - input path    */
specifier|protected
name|void
name|checkPartitionsMatch
parameter_list|(
name|Path
name|inputPath
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|dpPath
operator|.
name|equals
argument_list|(
name|inputPath
argument_list|)
condition|)
block|{
comment|// Temp partition input path does not match exist temp path
name|String
name|msg
init|=
literal|"Multiple partitions for one merge mapper: "
operator|+
name|dpPath
operator|+
literal|" NOT EQUAL TO "
operator|+
name|inputPath
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|void
name|fixTmpPath
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|trace
argument_list|(
literal|"Calling fixTmpPath with "
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
comment|// Fix temp path for alter table ... concatenate
if|if
condition|(
name|isListBucketingAlterTableConcatenate
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|tmpPathFixedConcatenate
condition|)
block|{
name|checkPartitionsMatch
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fixTmpPath
argument_list|(
name|path
argument_list|,
name|listBucketingDepth
argument_list|)
expr_stmt|;
name|tmpPathFixedConcatenate
operator|=
literal|true
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|hasDynamicPartitions
operator|||
operator|(
name|listBucketingDepth
operator|>
literal|0
operator|)
condition|)
block|{
comment|// In light of results from union queries, we need to be aware that
comment|// sub-directories can exist in the partition directory. We want to
comment|// ignore these sub-directories and promote merged files to the
comment|// partition directory.
name|String
name|name
init|=
name|path
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Path
name|realPartitionPath
init|=
name|name
operator|.
name|startsWith
argument_list|(
name|UNION_SUDBIR_PREFIX
argument_list|)
condition|?
name|path
operator|.
name|getParent
argument_list|()
else|:
name|path
decl_stmt|;
if|if
condition|(
name|tmpPathFixed
condition|)
block|{
name|checkPartitionsMatch
argument_list|(
name|realPartitionPath
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// We haven't fixed the TMP path for this mapper yet
name|int
name|depthDiff
init|=
name|realPartitionPath
operator|.
name|depth
argument_list|()
operator|-
name|tmpPath
operator|.
name|depth
argument_list|()
decl_stmt|;
name|fixTmpPath
argument_list|(
name|realPartitionPath
argument_list|,
name|depthDiff
argument_list|)
expr_stmt|;
name|tmpPathFixed
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|closeOp
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
if|if
condition|(
name|abort
condition|)
block|{
if|if
condition|(
operator|!
name|autoDelete
operator|||
name|isMmTable
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|outPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|// if outPath does not exist, then it means all paths within combine split are skipped as
comment|// they are incompatible for merge (for example: files without stripe stats).
comment|// Those files will be added to incompatFileSet
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|outPath
argument_list|)
condition|)
block|{
name|FileStatus
name|fss
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|outPath
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isMmTable
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|outPath
argument_list|,
name|finalPath
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to rename "
operator|+
name|outPath
operator|+
literal|" to "
operator|+
name|finalPath
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Renamed path "
operator|+
name|outPath
operator|+
literal|" to "
operator|+
name|finalPath
operator|+
literal|"("
operator|+
name|fss
operator|.
name|getLen
argument_list|()
operator|+
literal|" bytes)."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|finalPath
operator|.
name|equals
argument_list|(
name|outPath
argument_list|)
assert|;
comment|// There's always just one file that we have merged.
comment|// The union/DP/etc. should already be account for in the path.
name|Utilities
operator|.
name|writeCommitManifest
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|outPath
argument_list|)
argument_list|,
name|tmpPath
operator|.
name|getParent
argument_list|()
argument_list|,
name|fs
argument_list|,
name|taskId
argument_list|,
name|conf
operator|.
name|getWriteId
argument_list|()
argument_list|,
name|conf
operator|.
name|getStmtId
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Merged into "
operator|+
name|finalPath
operator|+
literal|"("
operator|+
name|fss
operator|.
name|getLen
argument_list|()
operator|+
literal|" bytes)."
argument_list|)
expr_stmt|;
block|}
block|}
comment|// move any incompatible files to final path
if|if
condition|(
name|incompatFileSet
operator|!=
literal|null
operator|&&
operator|!
name|incompatFileSet
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|isMmTable
condition|)
block|{
comment|// We only support query-time merge for MM tables, so don't handle this.
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Incompatible files should not happen in MM tables."
argument_list|)
throw|;
block|}
name|Path
name|destDir
init|=
name|finalPath
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|Path
name|destPath
init|=
name|destDir
decl_stmt|;
comment|// move any incompatible files to final path
if|if
condition|(
name|incompatFileSet
operator|!=
literal|null
operator|&&
operator|!
name|incompatFileSet
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|Path
name|incompatFile
range|:
name|incompatFileSet
control|)
block|{
comment|// check if path conforms to Hive's file name convention. Hive expects filenames to be in specific format
comment|// like 000000_0, but "LOAD DATA" commands can let you add any files to any partitions/tables without
comment|// renaming. This can cause MoveTask to remove files in some cases where MoveTask assumes the files are
comment|// are generated by speculatively executed tasks.
comment|// Example: MoveTask thinks the following files are same
comment|// part-m-00000_1417075294718
comment|// part-m-00001_1417075294718
comment|// Assumes 1417075294718 as taskId and retains only large file supposedly generated by speculative execution.
comment|// This can result in data loss in case of CONCATENATE/merging. Filter out files that does not match Hive's
comment|// filename convention.
if|if
condition|(
operator|!
name|Utilities
operator|.
name|isHiveManagedFile
argument_list|(
name|incompatFile
argument_list|)
condition|)
block|{
comment|// rename un-managed files to conform to Hive's naming standard
comment|// Example:
comment|// /warehouse/table/part-m-00000_1417075294718 will get renamed to /warehouse/table/.hive-staging/000000_0
comment|// If staging directory already contains the file, taskId_copy_N naming will be used.
specifier|final
name|String
name|taskId
init|=
name|Utilities
operator|.
name|getTaskId
argument_list|(
name|jc
argument_list|)
decl_stmt|;
name|Path
name|destFilePath
init|=
operator|new
name|Path
argument_list|(
name|destDir
argument_list|,
operator|new
name|Path
argument_list|(
name|taskId
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|counter
init|=
literal|1
init|;
name|fs
operator|.
name|exists
argument_list|(
name|destFilePath
argument_list|)
condition|;
name|counter
operator|++
control|)
block|{
name|destFilePath
operator|=
operator|new
name|Path
argument_list|(
name|destDir
argument_list|,
name|taskId
operator|+
operator|(
name|Utilities
operator|.
name|COPY_KEYWORD
operator|+
name|counter
operator|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Path doesn't conform to Hive's expectation. Renaming {} to {}"
argument_list|,
name|incompatFile
argument_list|,
name|destFilePath
argument_list|)
expr_stmt|;
name|destPath
operator|=
name|destFilePath
expr_stmt|;
block|}
try|try
block|{
name|Utilities
operator|.
name|renameOrMoveFiles
argument_list|(
name|fs
argument_list|,
name|incompatFile
argument_list|,
name|destPath
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Moved incompatible file "
operator|+
name|incompatFile
operator|+
literal|" to "
operator|+
name|destPath
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to move "
operator|+
name|incompatFile
operator|+
literal|" to "
operator|+
name|destPath
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
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
literal|"Failed to close AbstractFileMergeOperator"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|jobCloseOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|,
name|boolean
name|success
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|Path
name|outputDir
init|=
name|conf
operator|.
name|getOutputPath
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|outputDir
operator|.
name|getFileSystem
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
name|Long
name|mmWriteId
init|=
name|conf
operator|.
name|getWriteId
argument_list|()
decl_stmt|;
name|int
name|stmtId
init|=
name|conf
operator|.
name|getStmtId
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|isMmTable
condition|)
block|{
name|Path
name|backupPath
init|=
name|backupOutputPath
argument_list|(
name|fs
argument_list|,
name|outputDir
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|mvFileToFinalPath
argument_list|(
name|outputDir
argument_list|,
name|hconf
argument_list|,
name|success
argument_list|,
name|LOG
argument_list|,
name|conf
operator|.
name|getDpCtx
argument_list|()
argument_list|,
literal|null
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
if|if
condition|(
name|success
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"jobCloseOp moved merged files to output dir: "
operator|+
name|outputDir
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|backupPath
operator|!=
literal|null
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|backupPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|int
name|dpLevels
init|=
name|dpCtx
operator|==
literal|null
condition|?
literal|0
else|:
name|dpCtx
operator|.
name|getNumDPCols
argument_list|()
decl_stmt|,
name|lbLevels
init|=
name|conf
operator|.
name|getListBucketingDepth
argument_list|()
decl_stmt|;
comment|// We don't expect missing buckets from mere (actually there should be no buckets),
comment|// so just pass null as bucketing context. Union suffix should also be accounted for.
name|Utilities
operator|.
name|handleDirectInsertTableFinalPath
argument_list|(
name|outputDir
operator|.
name|getParent
argument_list|()
argument_list|,
literal|null
argument_list|,
name|hconf
argument_list|,
name|success
argument_list|,
name|dpLevels
argument_list|,
name|lbLevels
argument_list|,
literal|null
argument_list|,
name|mmWriteId
argument_list|,
name|stmtId
argument_list|,
name|reporter
argument_list|,
name|isMmTable
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
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
literal|"Failed jobCloseOp for AbstractFileMergeOperator"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|super
operator|.
name|jobCloseOp
argument_list|(
name|hconf
argument_list|,
name|success
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Path
name|backupOutputPath
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|outpath
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|outpath
argument_list|)
condition|)
block|{
name|Path
name|backupPath
init|=
operator|new
name|Path
argument_list|(
name|outpath
operator|.
name|getParent
argument_list|()
argument_list|,
name|BACKUP_PREFIX
operator|+
name|outpath
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|rename
argument_list|(
name|fs
argument_list|,
name|outpath
argument_list|,
name|backupPath
argument_list|)
expr_stmt|;
return|return
name|backupPath
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|AbstractFileMergeOperator
operator|.
name|getOperatorName
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|String
name|getOperatorName
parameter_list|()
block|{
return|return
literal|"MERGE"
return|;
block|}
specifier|protected
specifier|final
name|Path
name|getOutPath
parameter_list|()
block|{
return|return
name|outPath
return|;
block|}
specifier|protected
specifier|final
name|void
name|addIncompatibleFile
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|incompatFileSet
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

