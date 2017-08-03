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
name|common
operator|.
name|HiveStatsUtils
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
specifier|private
name|ListBucketingCtx
name|lbCtx
decl_stmt|;
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
comment|/**      * @return the lbCtx      */
specifier|public
name|ListBucketingCtx
name|getLbCtx
parameter_list|()
block|{
return|return
name|lbCtx
return|;
block|}
comment|/**      * @param lbCtx the lbCtx to set      */
specifier|public
name|void
name|setLbCtx
parameter_list|(
name|ListBucketingCtx
name|lbCtx
parameter_list|)
block|{
name|this
operator|.
name|lbCtx
operator|=
name|lbCtx
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
name|Math
operator|.
name|max
argument_list|(
name|trgtSize
argument_list|,
name|avgConditionSize
argument_list|)
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
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|mrAndMvTask
init|=
name|ctx
operator|.
name|getListTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|2
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
name|MapWork
name|work
decl_stmt|;
if|if
condition|(
name|mrTask
operator|.
name|getWork
argument_list|()
operator|instanceof
name|MapredWork
condition|)
block|{
name|work
operator|=
operator|(
operator|(
name|MapredWork
operator|)
name|mrTask
operator|.
name|getWork
argument_list|()
operator|)
operator|.
name|getMapWork
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|mrTask
operator|.
name|getWork
argument_list|()
operator|instanceof
name|TezWork
condition|)
block|{
name|work
operator|=
call|(
name|MapWork
call|)
argument_list|(
operator|(
name|TezWork
operator|)
name|mrTask
operator|.
name|getWork
argument_list|()
argument_list|)
operator|.
name|getAllWork
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|mrTask
operator|.
name|getWork
argument_list|()
operator|instanceof
name|SparkWork
condition|)
block|{
name|work
operator|=
call|(
name|MapWork
call|)
argument_list|(
operator|(
name|SparkWork
operator|)
name|mrTask
operator|.
name|getWork
argument_list|()
argument_list|)
operator|.
name|getAllWork
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|work
operator|=
operator|(
name|MapWork
operator|)
name|mrTask
operator|.
name|getWork
argument_list|()
expr_stmt|;
block|}
name|int
name|lbLevel
init|=
operator|(
name|ctx
operator|.
name|getLbCtx
argument_list|()
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|ctx
operator|.
name|getLbCtx
argument_list|()
operator|.
name|calculateListBucketingLevel
argument_list|()
decl_stmt|;
comment|/**          * In order to make code easier to read, we write the following in the way:          * 1. the first if clause to differ dynamic partition and static partition          * 2. with static partition, we differ list bucketing from non-list bucketing.          * Another way to write it is to merge static partition w/ LB wit DP. In that way,          * we still need to further differ them, since one uses lbLevel and          * another lbLevel+numDPCols.          * The first one is selected mainly for easy to read.          */
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
name|int
name|numDPCols
init|=
name|dpCtx
operator|.
name|getNumDPCols
argument_list|()
decl_stmt|;
name|int
name|dpLbLevel
init|=
name|numDPCols
operator|+
name|lbLevel
decl_stmt|;
name|generateActualTasks
argument_list|(
name|conf
argument_list|,
name|resTsks
argument_list|,
name|trgtSize
argument_list|,
name|avgConditionSize
argument_list|,
name|mvTask
argument_list|,
name|mrTask
argument_list|,
name|mrAndMvTask
argument_list|,
name|dirPath
argument_list|,
name|inpFs
argument_list|,
name|ctx
argument_list|,
name|work
argument_list|,
name|dpLbLevel
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// no dynamic partitions
if|if
condition|(
name|lbLevel
operator|==
literal|0
condition|)
block|{
comment|// static partition without list bucketing
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
else|else
block|{
comment|// static partition and list bucketing
name|generateActualTasks
argument_list|(
name|conf
argument_list|,
name|resTsks
argument_list|,
name|trgtSize
argument_list|,
name|avgConditionSize
argument_list|,
name|mvTask
argument_list|,
name|mrTask
argument_list|,
name|mrAndMvTask
argument_list|,
name|dirPath
argument_list|,
name|inpFs
argument_list|,
name|ctx
argument_list|,
name|work
argument_list|,
name|lbLevel
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
comment|// Only one of the tasks should ever be added to resTsks
assert|assert
operator|(
name|resTsks
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|)
assert|;
return|return
name|resTsks
return|;
block|}
comment|/**    * This method generates actual task for conditional tasks. It could be    * 1. move task only    * 2. merge task only    * 3. merge task followed by a move task.    * It used to be true for dynamic partition only since static partition doesn't have #3.    * It changes w/ list bucketing. Static partition has #3 since it has sub-directories.    * For example, if a static partition is defined as skewed and stored-as-directores,    * instead of all files in one directory, it will create a sub-dir per skewed value plus    * default directory. So #3 is required for static partition.    * So, we move it to a method so that it can be used by both SP and DP.    * @param conf    * @param resTsks    * @param trgtSize    * @param avgConditionSize    * @param mvTask    * @param mrTask    * @param mrAndMvTask    * @param dirPath    * @param inpFs    * @param ctx    * @param work    * @param dpLbLevel    * @throws IOException    */
specifier|private
name|void
name|generateActualTasks
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
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
parameter_list|,
name|long
name|trgtSize
parameter_list|,
name|long
name|avgConditionSize
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|mvTask
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|mrTask
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|mrAndMvTask
parameter_list|,
name|Path
name|dirPath
parameter_list|,
name|FileSystem
name|inpFs
parameter_list|,
name|ConditionalResolverMergeFilesCtx
name|ctx
parameter_list|,
name|MapWork
name|work
parameter_list|,
name|int
name|dpLbLevel
parameter_list|)
throws|throws
name|IOException
block|{
name|DynamicPartitionCtx
name|dpCtx
init|=
name|ctx
operator|.
name|getDPCtx
argument_list|()
decl_stmt|;
comment|// get list of dynamic partitions
name|FileStatus
index|[]
name|status
init|=
name|HiveStatsUtils
operator|.
name|getFileStatusRecurse
argument_list|(
name|dirPath
argument_list|,
name|dpLbLevel
argument_list|,
name|inpFs
argument_list|)
decl_stmt|;
comment|// cleanup pathToPartitionInfo
name|Map
argument_list|<
name|Path
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
name|Path
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
name|PartitionDesc
name|partDesc
init|=
name|ptpi
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|TableDesc
name|tblDesc
init|=
name|partDesc
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
name|work
operator|.
name|removePathToPartitionInfo
argument_list|(
name|path
argument_list|)
expr_stmt|;
comment|// the root path is not useful anymore
comment|// cleanup pathToAliases
name|LinkedHashMap
argument_list|<
name|Path
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
name|work
operator|.
name|removePathToAlias
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
name|Path
argument_list|>
name|toMove
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
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
name|PartitionDesc
name|pDesc
init|=
operator|(
name|dpCtx
operator|!=
literal|null
operator|)
condition|?
name|generateDPFullPartSpec
argument_list|(
name|dpCtx
argument_list|,
name|status
argument_list|,
name|tblDesc
argument_list|,
name|i
argument_list|)
else|:
name|partDesc
decl_stmt|;
name|work
operator|.
name|resolveDynamicPartitionStoredAsSubDirsMerge
argument_list|(
name|conf
argument_list|,
name|status
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
argument_list|,
name|tblDesc
argument_list|,
name|aliases
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
comment|// modify the existing move task as it is already in the candidate running tasks
comment|// running the MoveTask and MR task in parallel may
comment|// cause the mvTask write to /ds=1 and MR task write
comment|// to /ds=1_1 for the same partition.
comment|// make the MoveTask as the child of the MR Task
name|resTsks
operator|.
name|add
argument_list|(
name|mrAndMvTask
argument_list|)
expr_stmt|;
comment|// Originally the mvTask and the child move task of the mrAndMvTask contain the same
comment|// MoveWork object.
comment|// If the blobstore optimizations are on and the input/output paths are merged
comment|// in the move only MoveWork, the mvTask and the child move task of the mrAndMvTask
comment|// will contain different MoveWork objects, which causes problems.
comment|// Not just in this case, but also in general the child move task of the mrAndMvTask should
comment|// be used, because that is the correct move task for the "merge and move" use case.
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|mergeAndMoveMoveTask
init|=
name|mrAndMvTask
operator|.
name|getChildTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|MoveWork
name|mvWork
init|=
operator|(
name|MoveWork
operator|)
name|mergeAndMoveMoveTask
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
name|Path
name|targetDir
init|=
name|lfd
operator|.
name|getTargetDir
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|targetDirs
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|(
name|toMove
operator|.
name|size
argument_list|()
argument_list|)
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
index|[]
name|moveStrSplits
init|=
name|toMove
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
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
name|dpLbLevel
decl_stmt|;
name|Path
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
operator|new
name|Path
argument_list|(
name|target
argument_list|,
name|moveStrSplits
index|[
name|dpIndex
index|]
argument_list|)
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
block|}
else|else
block|{
name|resTsks
operator|.
name|add
argument_list|(
name|mrTask
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
specifier|private
name|PartitionDesc
name|generateDPFullPartSpec
parameter_list|(
name|DynamicPartitionCtx
name|dpCtx
parameter_list|,
name|FileStatus
index|[]
name|status
parameter_list|,
name|TableDesc
name|tblDesc
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|fullPartSpec
init|=
operator|new
name|LinkedHashMap
argument_list|<>
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
name|fullPartSpec
argument_list|)
decl_stmt|;
return|return
name|pDesc
return|;
block|}
specifier|private
name|void
name|setupMapRedWork
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|MapWork
name|mWork
parameter_list|,
name|long
name|targetSize
parameter_list|,
name|long
name|totalSize
parameter_list|)
block|{
name|mWork
operator|.
name|setMaxSplitSize
argument_list|(
name|targetSize
argument_list|)
expr_stmt|;
name|mWork
operator|.
name|setMinSplitSize
argument_list|(
name|targetSize
argument_list|)
expr_stmt|;
name|mWork
operator|.
name|setMinSplitSizePerNode
argument_list|(
name|targetSize
argument_list|)
expr_stmt|;
name|mWork
operator|.
name|setMinSplitSizePerRack
argument_list|(
name|targetSize
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|AverageSize
block|{
specifier|private
specifier|final
name|long
name|totalSize
decl_stmt|;
specifier|private
specifier|final
name|int
name|numFiles
decl_stmt|;
specifier|public
name|AverageSize
parameter_list|(
name|long
name|totalSize
parameter_list|,
name|int
name|numFiles
parameter_list|)
block|{
name|this
operator|.
name|totalSize
operator|=
name|totalSize
expr_stmt|;
name|this
operator|.
name|numFiles
operator|=
name|numFiles
expr_stmt|;
block|}
specifier|public
name|long
name|getTotalSize
parameter_list|()
block|{
return|return
name|totalSize
return|;
block|}
specifier|public
name|int
name|getNumFiles
parameter_list|()
block|{
return|return
name|numFiles
return|;
block|}
block|}
specifier|private
name|AverageSize
name|getAverageSize
parameter_list|(
name|FileSystem
name|inpFs
parameter_list|,
name|Path
name|dirPath
parameter_list|)
block|{
name|AverageSize
name|error
init|=
operator|new
name|AverageSize
argument_list|(
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
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
name|long
name|totalSz
init|=
literal|0
decl_stmt|;
name|int
name|numFiles
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
if|if
condition|(
name|fStat
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|AverageSize
name|avgSzDir
init|=
name|getAverageSize
argument_list|(
name|inpFs
argument_list|,
name|fStat
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|avgSzDir
operator|.
name|getTotalSize
argument_list|()
operator|<
literal|0
condition|)
block|{
return|return
name|error
return|;
block|}
name|totalSz
operator|+=
name|avgSzDir
operator|.
name|getTotalSize
argument_list|()
expr_stmt|;
name|numFiles
operator|+=
name|avgSzDir
operator|.
name|getNumFiles
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|totalSz
operator|+=
name|fStat
operator|.
name|getLen
argument_list|()
expr_stmt|;
name|numFiles
operator|++
expr_stmt|;
block|}
block|}
return|return
operator|new
name|AverageSize
argument_list|(
name|totalSz
argument_list|,
name|numFiles
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
name|error
return|;
block|}
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
name|AverageSize
name|averageSize
init|=
name|getAverageSize
argument_list|(
name|inpFs
argument_list|,
name|dirPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|averageSize
operator|.
name|getTotalSize
argument_list|()
operator|<=
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|averageSize
operator|.
name|getNumFiles
argument_list|()
operator|<=
literal|1
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|averageSize
operator|.
name|getTotalSize
argument_list|()
operator|/
name|averageSize
operator|.
name|getNumFiles
argument_list|()
operator|<
name|avgSize
condition|)
block|{
return|return
name|averageSize
operator|.
name|getTotalSize
argument_list|()
return|;
block|}
return|return
operator|-
literal|1
return|;
block|}
block|}
end_class

end_unit

