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
name|plan
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
name|Iterator
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
name|Warehouse
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
name|Task
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

begin_comment
comment|/**  * Conditional task resolution interface. This is invoked at run time to get the  * task to invoke. Developers can plug in their own resolvers  */
end_comment

begin_class
specifier|public
class|class
name|ConditionalResolverMergeFiles
implements|implements
name|ConditionalResolver
implements|,
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
name|ConditionalResolverMergeFiles
parameter_list|()
block|{   }
comment|/**    * ConditionalResolverMergeFilesCtx.    *    */
specifier|public
specifier|static
class|class
name|ConditionalResolverMergeFilesCtx
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
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|listTasks
decl_stmt|;
specifier|private
name|String
name|dir
decl_stmt|;
specifier|private
name|DynamicPartitionCtx
name|dpCtx
decl_stmt|;
comment|// merge task could be after dynamic partition insert
specifier|public
name|ConditionalResolverMergeFilesCtx
parameter_list|()
block|{     }
comment|/**      * @param dir      */
specifier|public
name|ConditionalResolverMergeFilesCtx
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|listTasks
parameter_list|,
name|String
name|dir
parameter_list|)
block|{
name|this
operator|.
name|listTasks
operator|=
name|listTasks
expr_stmt|;
name|this
operator|.
name|dir
operator|=
name|dir
expr_stmt|;
block|}
comment|/**      * @return the dir      */
specifier|public
name|String
name|getDir
parameter_list|()
block|{
return|return
name|dir
return|;
block|}
comment|/**      * @param dir      *          the dir to set      */
specifier|public
name|void
name|setDir
parameter_list|(
name|String
name|dir
parameter_list|)
block|{
name|this
operator|.
name|dir
operator|=
name|dir
expr_stmt|;
block|}
comment|/**      * @return the listTasks      */
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getListTasks
parameter_list|()
block|{
return|return
name|listTasks
return|;
block|}
comment|/**      * @param listTasks      *          the listTasks to set      */
specifier|public
name|void
name|setListTasks
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|listTasks
parameter_list|)
block|{
name|this
operator|.
name|listTasks
operator|=
name|listTasks
expr_stmt|;
block|}
specifier|public
name|DynamicPartitionCtx
name|getDPCtx
parameter_list|()
block|{
return|return
name|dpCtx
return|;
block|}
specifier|public
name|void
name|setDPCtx
parameter_list|(
name|DynamicPartitionCtx
name|dp
parameter_list|)
block|{
name|dpCtx
operator|=
name|dp
expr_stmt|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getTasks
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Object
name|objCtx
parameter_list|)
block|{
name|ConditionalResolverMergeFilesCtx
name|ctx
init|=
operator|(
name|ConditionalResolverMergeFilesCtx
operator|)
name|objCtx
decl_stmt|;
name|String
name|dirName
init|=
name|ctx
operator|.
name|getDir
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|resTsks
init|=
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// check if a map-reduce job is needed to merge the files
comment|// If the current size is smaller than the target, merge
name|long
name|trgtSize
init|=
name|conf
operator|.
name|getLongVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMERGEMAPFILESSIZE
argument_list|)
decl_stmt|;
name|long
name|avgConditionSize
init|=
name|conf
operator|.
name|getLongVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMERGEMAPFILESAVGSIZE
argument_list|)
decl_stmt|;
name|trgtSize
operator|=
name|trgtSize
operator|>
name|avgConditionSize
condition|?
name|trgtSize
else|:
name|avgConditionSize
expr_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|mvTask
init|=
name|ctx
operator|.
name|getListTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|mrTask
init|=
name|ctx
operator|.
name|getListTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
try|try
block|{
name|Path
name|dirPath
init|=
operator|new
name|Path
argument_list|(
name|dirName
argument_list|)
decl_stmt|;
name|FileSystem
name|inpFs
init|=
name|dirPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|DynamicPartitionCtx
name|dpCtx
init|=
name|ctx
operator|.
name|getDPCtx
argument_list|()
decl_stmt|;
if|if
condition|(
name|inpFs
operator|.
name|exists
argument_list|(
name|dirPath
argument_list|)
condition|)
block|{
comment|// For each dynamic partition, check if it needs to be merged.
name|MapredWork
name|work
init|=
operator|(
name|MapredWork
operator|)
name|mrTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
comment|// Dynamic partition: replace input path (root to dp paths) with dynamic partition
comment|// input paths.
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
comment|// get list of dynamic partitions
name|FileStatus
index|[]
name|status
init|=
name|Utilities
operator|.
name|getFileStatusRecurse
argument_list|(
name|dirPath
argument_list|,
name|dpCtx
operator|.
name|getNumDPCols
argument_list|()
argument_list|,
name|inpFs
argument_list|)
decl_stmt|;
comment|// cleanup pathToPartitionInfo
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|ptpi
init|=
name|work
operator|.
name|getPathToPartitionInfo
argument_list|()
decl_stmt|;
assert|assert
name|ptpi
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|String
name|path
init|=
name|ptpi
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|TableDesc
name|tblDesc
init|=
name|ptpi
operator|.
name|get
argument_list|(
name|path
argument_list|)
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
name|ptpi
operator|.
name|remove
argument_list|(
name|path
argument_list|)
expr_stmt|;
comment|// the root path is not useful anymore
comment|// cleanup pathToAliases
name|Map
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pta
init|=
name|work
operator|.
name|getPathToAliases
argument_list|()
decl_stmt|;
assert|assert
name|pta
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|path
operator|=
name|pta
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|pta
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|pta
operator|.
name|remove
argument_list|(
name|path
argument_list|)
expr_stmt|;
comment|// the root path is not useful anymore
comment|// populate pathToPartitionInfo and pathToAliases w/ DP paths
name|long
name|totalSz
init|=
literal|0
decl_stmt|;
name|boolean
name|doMerge
init|=
literal|false
decl_stmt|;
comment|// list of paths that don't need to merge but need to move to the dest location
name|List
argument_list|<
name|String
argument_list|>
name|toMove
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
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
name|status
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|long
name|len
init|=
name|getMergeSize
argument_list|(
name|inpFs
argument_list|,
name|status
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
argument_list|,
name|avgConditionSize
argument_list|)
decl_stmt|;
if|if
condition|(
name|len
operator|>=
literal|0
condition|)
block|{
name|doMerge
operator|=
literal|true
expr_stmt|;
name|totalSz
operator|+=
name|len
expr_stmt|;
name|work
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|put
argument_list|(
name|status
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|aliases
argument_list|)
expr_stmt|;
comment|// get the full partition spec from the path and update the PartitionDesc
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|fullPartSpec
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|dpCtx
operator|.
name|getPartSpec
argument_list|()
argument_list|)
decl_stmt|;
name|Warehouse
operator|.
name|makeSpecFromName
argument_list|(
name|fullPartSpec
argument_list|,
name|status
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|PartitionDesc
name|pDesc
init|=
operator|new
name|PartitionDesc
argument_list|(
name|tblDesc
argument_list|,
operator|(
name|LinkedHashMap
operator|)
name|fullPartSpec
argument_list|)
decl_stmt|;
name|work
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|put
argument_list|(
name|status
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|pDesc
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|toMove
operator|.
name|add
argument_list|(
name|status
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|doMerge
condition|)
block|{
comment|// add the merge MR job
name|setupMapRedWork
argument_list|(
name|conf
argument_list|,
name|work
argument_list|,
name|trgtSize
argument_list|,
name|totalSz
argument_list|)
expr_stmt|;
name|resTsks
operator|.
name|add
argument_list|(
name|mrTask
argument_list|)
expr_stmt|;
comment|// add the move task for those partitions that do not need merging
if|if
condition|(
name|toMove
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|//
comment|// modify the existing move task as it is already in the candidate running tasks
name|MoveWork
name|mvWork
init|=
operator|(
name|MoveWork
operator|)
name|mvTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|LoadFileDesc
name|lfd
init|=
name|mvWork
operator|.
name|getLoadFileWork
argument_list|()
decl_stmt|;
name|String
name|targetDir
init|=
name|lfd
operator|.
name|getTargetDir
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|targetDirs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|toMove
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|numDPCols
init|=
name|dpCtx
operator|.
name|getNumDPCols
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
name|toMove
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|toMoveStr
init|=
name|toMove
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|toMoveStr
operator|.
name|endsWith
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
condition|)
block|{
name|toMoveStr
operator|=
name|toMoveStr
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|toMoveStr
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|moveStrSplits
init|=
name|toMoveStr
operator|.
name|split
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
decl_stmt|;
name|int
name|dpIndex
init|=
name|moveStrSplits
operator|.
name|length
operator|-
name|numDPCols
decl_stmt|;
name|String
name|target
init|=
name|targetDir
decl_stmt|;
while|while
condition|(
name|dpIndex
operator|<
name|moveStrSplits
operator|.
name|length
condition|)
block|{
name|target
operator|=
name|target
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|moveStrSplits
index|[
name|dpIndex
index|]
expr_stmt|;
name|dpIndex
operator|++
expr_stmt|;
block|}
name|targetDirs
operator|.
name|add
argument_list|(
name|target
argument_list|)
expr_stmt|;
block|}
name|LoadMultiFilesDesc
name|lmfd
init|=
operator|new
name|LoadMultiFilesDesc
argument_list|(
name|toMove
argument_list|,
name|targetDirs
argument_list|,
name|lfd
operator|.
name|getIsDfsDir
argument_list|()
argument_list|,
name|lfd
operator|.
name|getColumns
argument_list|()
argument_list|,
name|lfd
operator|.
name|getColumnTypes
argument_list|()
argument_list|)
decl_stmt|;
name|mvWork
operator|.
name|setLoadFileWork
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|mvWork
operator|.
name|setLoadTableWork
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|mvWork
operator|.
name|setMultiFilesDesc
argument_list|(
name|lmfd
argument_list|)
expr_stmt|;
comment|// running the MoveTask and MR task in parallel may
comment|// cause the mvTask write to /ds=1 and MR task write
comment|// to /ds=1_1 for the same partition.
comment|// make the MoveTask as the child of the MR Task
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|cTasks
init|=
name|mrTask
operator|.
name|getDependentTasks
argument_list|()
decl_stmt|;
if|if
condition|(
name|cTasks
operator|!=
literal|null
condition|)
block|{
name|Iterator
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|itr
init|=
name|cTasks
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|itr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|cld
init|=
name|itr
operator|.
name|next
argument_list|()
decl_stmt|;
name|itr
operator|.
name|remove
argument_list|()
expr_stmt|;
name|mvTask
operator|.
name|addDependentTask
argument_list|(
name|cld
argument_list|)
expr_stmt|;
block|}
block|}
name|mrTask
operator|.
name|addDependentTask
argument_list|(
name|mvTask
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// add the move task
name|resTsks
operator|.
name|add
argument_list|(
name|mvTask
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// no dynamic partitions
name|long
name|totalSz
init|=
name|getMergeSize
argument_list|(
name|inpFs
argument_list|,
name|dirPath
argument_list|,
name|avgConditionSize
argument_list|)
decl_stmt|;
if|if
condition|(
name|totalSz
operator|>=
literal|0
condition|)
block|{
comment|// add the merge job
name|setupMapRedWork
argument_list|(
name|conf
argument_list|,
name|work
argument_list|,
name|trgtSize
argument_list|,
name|totalSz
argument_list|)
expr_stmt|;
name|resTsks
operator|.
name|add
argument_list|(
name|mrTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// don't need to merge, add the move job
name|resTsks
operator|.
name|add
argument_list|(
name|mvTask
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|resTsks
operator|.
name|add
argument_list|(
name|mvTask
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
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
return|return
name|resTsks
return|;
block|}
specifier|private
name|void
name|setupMapRedWork
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|MapredWork
name|work
parameter_list|,
name|long
name|targetSize
parameter_list|,
name|long
name|totalSize
parameter_list|)
block|{
if|if
condition|(
name|work
operator|.
name|getNumReduceTasks
argument_list|()
operator|>
literal|0
condition|)
block|{
name|int
name|maxReducers
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAXREDUCERS
argument_list|)
decl_stmt|;
name|int
name|reducers
init|=
call|(
name|int
call|)
argument_list|(
operator|(
name|totalSize
operator|+
name|targetSize
operator|-
literal|1
operator|)
operator|/
name|targetSize
argument_list|)
decl_stmt|;
name|reducers
operator|=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|reducers
argument_list|)
expr_stmt|;
name|reducers
operator|=
name|Math
operator|.
name|min
argument_list|(
name|maxReducers
argument_list|,
name|reducers
argument_list|)
expr_stmt|;
name|work
operator|.
name|setNumReduceTasks
argument_list|(
name|reducers
argument_list|)
expr_stmt|;
block|}
name|work
operator|.
name|setMinSplitSize
argument_list|(
name|targetSize
argument_list|)
expr_stmt|;
block|}
comment|/**    * Whether to merge files inside directory given the threshold of the average file size.    *    * @param inpFs input file system.    * @param dirPath input file directory.    * @param avgSize threshold of average file size.    * @return -1 if not need to merge (either because of there is only 1 file or the    * average size is larger than avgSize). Otherwise the size of the total size of files.    * If return value is 0 that means there are multiple files each of which is an empty file.    * This could be true when the table is bucketized and all buckets are empty.    */
specifier|private
name|long
name|getMergeSize
parameter_list|(
name|FileSystem
name|inpFs
parameter_list|,
name|Path
name|dirPath
parameter_list|,
name|long
name|avgSize
parameter_list|)
block|{
try|try
block|{
name|FileStatus
index|[]
name|fStats
init|=
name|inpFs
operator|.
name|listStatus
argument_list|(
name|dirPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|fStats
operator|.
name|length
operator|<=
literal|1
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|long
name|totalSz
init|=
literal|0
decl_stmt|;
for|for
control|(
name|FileStatus
name|fStat
range|:
name|fStats
control|)
block|{
name|totalSz
operator|+=
name|fStat
operator|.
name|getLen
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|totalSz
operator|<
name|avgSize
operator|*
name|fStats
operator|.
name|length
condition|)
block|{
return|return
name|totalSz
return|;
block|}
else|else
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
block|}
end_class

end_unit

