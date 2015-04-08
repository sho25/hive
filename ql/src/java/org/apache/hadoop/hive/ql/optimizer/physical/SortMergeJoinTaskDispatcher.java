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
name|optimizer
operator|.
name|physical
package|;
end_package

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
name|io
operator|.
name|UnsupportedEncodingException
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
name|HashMap
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
name|exec
operator|.
name|ConditionalTask
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
name|DummyStoreOperator
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
name|FileSinkOperator
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
name|MapJoinOperator
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
name|Operator
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
name|ReduceSinkOperator
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
name|SMBMapJoinOperator
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
name|TaskFactory
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
name|exec
operator|.
name|mr
operator|.
name|MapRedTask
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
name|lib
operator|.
name|Dispatcher
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
name|optimizer
operator|.
name|GenMapRedUtils
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
name|optimizer
operator|.
name|MapJoinProcessor
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
name|ParseContext
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
name|plan
operator|.
name|ConditionalResolverCommonJoin
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
name|ConditionalResolverCommonJoin
operator|.
name|ConditionalResolverCommonJoinCtx
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
name|ConditionalWork
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
name|FetchWork
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
name|MapWork
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
name|MapredLocalWork
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
name|MapredWork
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
name|OperatorDesc
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
name|PartitionDesc
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
name|ReduceWork
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
name|SMBJoinDesc
import|;
end_import

begin_comment
comment|/**  * Iterator over each task. If the task has a smb join, convert the task to a conditional task.  * The conditional task will first try all mapjoin possibilities, and go the the smb join if the  * mapjoin fails. The smb join will be a backup task for all the mapjoin tasks.  */
end_comment

begin_class
specifier|public
class|class
name|SortMergeJoinTaskDispatcher
extends|extends
name|AbstractJoinTaskDispatcher
implements|implements
name|Dispatcher
block|{
specifier|public
name|SortMergeJoinTaskDispatcher
parameter_list|(
name|PhysicalContext
name|context
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
comment|// Convert the work in the SMB plan to a regular join
comment|// Note that the operator tree is not fixed, only the path/alias mappings in the
comment|// plan are fixed. The operator tree will still contain the SMBJoinOperator
specifier|private
name|void
name|genSMBJoinWork
parameter_list|(
name|MapWork
name|currWork
parameter_list|,
name|SMBMapJoinOperator
name|smbJoinOp
parameter_list|)
block|{
comment|// Remove the paths which are not part of aliasToPartitionInfo
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|aliasToPartitionInfo
init|=
name|currWork
operator|.
name|getAliasToPartnInfo
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|removePaths
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
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|currWork
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|boolean
name|keepPath
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|alias
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
if|if
condition|(
name|aliasToPartitionInfo
operator|.
name|containsKey
argument_list|(
name|alias
argument_list|)
condition|)
block|{
name|keepPath
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
comment|// Remove if the path is not present
if|if
condition|(
operator|!
name|keepPath
condition|)
block|{
name|removePaths
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|String
argument_list|>
name|removeAliases
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
name|String
name|removePath
range|:
name|removePaths
control|)
block|{
name|removeAliases
operator|.
name|addAll
argument_list|(
name|currWork
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|get
argument_list|(
name|removePath
argument_list|)
argument_list|)
expr_stmt|;
name|currWork
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|remove
argument_list|(
name|removePath
argument_list|)
expr_stmt|;
name|currWork
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|remove
argument_list|(
name|removePath
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|alias
range|:
name|removeAliases
control|)
block|{
name|currWork
operator|.
name|getAliasToPartnInfo
argument_list|()
operator|.
name|remove
argument_list|(
name|alias
argument_list|)
expr_stmt|;
name|currWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|remove
argument_list|(
name|alias
argument_list|)
expr_stmt|;
block|}
comment|// Get the MapredLocalWork
name|MapredLocalWork
name|localWork
init|=
name|smbJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getLocalWork
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|entry
range|:
name|localWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|alias
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|FetchWork
name|fetchWork
init|=
name|localWork
operator|.
name|getAliasToFetchWork
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
comment|// Add the entry in mapredwork
name|currWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|op
argument_list|)
expr_stmt|;
name|PartitionDesc
name|partitionInfo
init|=
name|currWork
operator|.
name|getAliasToPartnInfo
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|fetchWork
operator|.
name|getTblDir
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|currWork
operator|.
name|mergeAliasedInput
argument_list|(
name|alias
argument_list|,
name|fetchWork
operator|.
name|getTblDir
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|partitionInfo
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|Path
name|pathDir
range|:
name|fetchWork
operator|.
name|getPartDir
argument_list|()
control|)
block|{
name|currWork
operator|.
name|mergeAliasedInput
argument_list|(
name|alias
argument_list|,
name|pathDir
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|partitionInfo
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Remove the dummy store operator from the tree
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOp
range|:
name|smbJoinOp
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
name|parentOp
operator|instanceof
name|DummyStoreOperator
condition|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|grandParentOp
init|=
name|parentOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|smbJoinOp
operator|.
name|replaceParent
argument_list|(
name|parentOp
argument_list|,
name|grandParentOp
argument_list|)
expr_stmt|;
name|grandParentOp
operator|.
name|setChildOperators
argument_list|(
name|parentOp
operator|.
name|getChildOperators
argument_list|()
argument_list|)
expr_stmt|;
name|parentOp
operator|.
name|setParentOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|parentOp
operator|.
name|setParentOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/*    * Convert the work containing to sort-merge join into a work, as if it had a regular join.    * Note that the operator tree is not changed - is still contains the SMB join, but the    * plan is changed (aliasToWork etc.) to contain all the paths as if it was a regular join.    */
specifier|private
name|MapredWork
name|convertSMBWorkToJoinWork
parameter_list|(
name|MapredWork
name|currWork
parameter_list|,
name|SMBMapJoinOperator
name|oldSMBJoinOp
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
comment|// deep copy a new mapred work
name|MapredWork
name|currJoinWork
init|=
name|Utilities
operator|.
name|clonePlan
argument_list|(
name|currWork
argument_list|)
decl_stmt|;
name|SMBMapJoinOperator
name|newSMBJoinOp
init|=
name|getSMBMapJoinOp
argument_list|(
name|currJoinWork
argument_list|)
decl_stmt|;
comment|// change the newly created map-red plan as if it was a join operator
name|genSMBJoinWork
argument_list|(
name|currJoinWork
operator|.
name|getMapWork
argument_list|()
argument_list|,
name|newSMBJoinOp
argument_list|)
expr_stmt|;
return|return
name|currJoinWork
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Generate Map Join Task Error: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|// create map join task and set big table as bigTablePosition
specifier|private
name|MapRedTask
name|convertSMBTaskToMapJoinTask
parameter_list|(
name|MapredWork
name|origWork
parameter_list|,
name|int
name|bigTablePosition
parameter_list|,
name|SMBMapJoinOperator
name|smbJoinOp
parameter_list|)
throws|throws
name|UnsupportedEncodingException
throws|,
name|SemanticException
block|{
comment|// deep copy a new mapred work
name|MapredWork
name|newWork
init|=
name|Utilities
operator|.
name|clonePlan
argument_list|(
name|origWork
argument_list|)
decl_stmt|;
comment|// create a mapred task for this work
name|MapRedTask
name|newTask
init|=
operator|(
name|MapRedTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
name|newWork
argument_list|,
name|physicalContext
operator|.
name|getParseContext
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
comment|// generate the map join operator; already checked the map join
name|MapJoinOperator
name|newMapJoinOp
init|=
name|getMapJoinOperator
argument_list|(
name|newTask
argument_list|,
name|newWork
argument_list|,
name|smbJoinOp
argument_list|,
name|bigTablePosition
argument_list|)
decl_stmt|;
comment|// The reducer needs to be restored - Consider a query like:
comment|// select count(*) FROM bucket_big a JOIN bucket_small b ON a.key = b.key;
comment|// The reducer contains a groupby, which needs to be restored.
name|ReduceWork
name|rWork
init|=
name|newWork
operator|.
name|getReduceWork
argument_list|()
decl_stmt|;
comment|// create the local work for this plan
name|MapJoinProcessor
operator|.
name|genLocalWorkForMapJoin
argument_list|(
name|newWork
argument_list|,
name|newMapJoinOp
argument_list|,
name|bigTablePosition
argument_list|)
expr_stmt|;
comment|// restore the reducer
name|newWork
operator|.
name|setReduceWork
argument_list|(
name|rWork
argument_list|)
expr_stmt|;
return|return
name|newTask
return|;
block|}
specifier|private
name|boolean
name|isEligibleForOptimization
parameter_list|(
name|SMBMapJoinOperator
name|originalSMBJoinOp
parameter_list|)
block|{
if|if
condition|(
name|originalSMBJoinOp
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// Only create a map-join if the user explicitly gave a join (without a mapjoin hint)
if|if
condition|(
operator|!
name|originalSMBJoinOp
operator|.
name|isConvertedAutomaticallySMBJoin
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|currOp
init|=
name|originalSMBJoinOp
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
operator|(
name|currOp
operator|.
name|getChildOperators
argument_list|()
operator|==
literal|null
operator|)
operator|||
operator|(
name|currOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
if|if
condition|(
name|currOp
operator|instanceof
name|FileSinkOperator
condition|)
block|{
name|FileSinkOperator
name|fsOp
init|=
operator|(
name|FileSinkOperator
operator|)
name|currOp
decl_stmt|;
comment|// The query has enforced that a sort-merge join should be performed.
comment|// For more details, look at 'removedReduceSinkBucketSort' in FileSinkDesc.java
return|return
operator|!
name|fsOp
operator|.
name|getConf
argument_list|()
operator|.
name|isRemovedReduceSinkBucketSort
argument_list|()
return|;
block|}
comment|// If it contains a reducer, the optimization is always on.
comment|// Since there exists a reducer, the sorting/bucketing properties due to the
comment|// sort-merge join operator are lost anyway. So, the plan cannot be wrong by
comment|// changing the sort-merge join to a map-join
if|if
condition|(
name|currOp
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
if|if
condition|(
name|currOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
return|return
literal|true
return|;
block|}
name|currOp
operator|=
name|currOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|processCurrentTask
parameter_list|(
name|MapRedTask
name|currTask
parameter_list|,
name|ConditionalTask
name|conditionalTask
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// whether it contains a sort merge join operator
name|MapredWork
name|currWork
init|=
name|currTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|SMBMapJoinOperator
name|originalSMBJoinOp
init|=
name|getSMBMapJoinOp
argument_list|(
name|currWork
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isEligibleForOptimization
argument_list|(
name|originalSMBJoinOp
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|currTask
operator|.
name|setTaskTag
argument_list|(
name|Task
operator|.
name|CONVERTED_SORTMERGEJOIN
argument_list|)
expr_stmt|;
comment|// get parseCtx for this Join Operator
name|ParseContext
name|parseCtx
init|=
name|physicalContext
operator|.
name|getParseContext
argument_list|()
decl_stmt|;
comment|// Convert the work containing to sort-merge join into a work, as if it had a regular join.
comment|// Note that the operator tree is not changed - is still contains the SMB join, but the
comment|// plan is changed (aliasToWork etc.) to contain all the paths as if it was a regular join.
comment|// This is used to convert the plan to a map-join, and then the original SMB join plan is used
comment|// as a backup task.
name|MapredWork
name|currJoinWork
init|=
name|convertSMBWorkToJoinWork
argument_list|(
name|currWork
argument_list|,
name|originalSMBJoinOp
argument_list|)
decl_stmt|;
name|SMBMapJoinOperator
name|newSMBJoinOp
init|=
name|getSMBMapJoinOp
argument_list|(
name|currJoinWork
argument_list|)
decl_stmt|;
name|currWork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setLeftInputJoin
argument_list|(
name|originalSMBJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|isLeftInputJoin
argument_list|()
argument_list|)
expr_stmt|;
name|currWork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setBaseSrc
argument_list|(
name|originalSMBJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getBaseSrc
argument_list|()
argument_list|)
expr_stmt|;
name|currWork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setMapAliases
argument_list|(
name|originalSMBJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getMapAliases
argument_list|()
argument_list|)
expr_stmt|;
name|currJoinWork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setLeftInputJoin
argument_list|(
name|originalSMBJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|isLeftInputJoin
argument_list|()
argument_list|)
expr_stmt|;
name|currJoinWork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setBaseSrc
argument_list|(
name|originalSMBJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getBaseSrc
argument_list|()
argument_list|)
expr_stmt|;
name|currJoinWork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setMapAliases
argument_list|(
name|originalSMBJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getMapAliases
argument_list|()
argument_list|)
expr_stmt|;
comment|// create conditional work list and task list
name|List
argument_list|<
name|Serializable
argument_list|>
name|listWorks
init|=
operator|new
name|ArrayList
argument_list|<
name|Serializable
argument_list|>
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
name|listTasks
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
comment|// create task to aliases mapping and alias to input file mapping for resolver
comment|// Must be deterministic order map for consistent q-test output across Java versions
name|HashMap
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|taskToAliases
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// Note that pathToAlias will behave as if the original plan was a join plan
name|HashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
init|=
name|currJoinWork
operator|.
name|getMapWork
argument_list|()
operator|.
name|getPathToAliases
argument_list|()
decl_stmt|;
comment|// generate a map join task for the big table
name|SMBJoinDesc
name|originalSMBJoinDesc
init|=
name|originalSMBJoinOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|Byte
index|[]
name|order
init|=
name|originalSMBJoinDesc
operator|.
name|getTagOrder
argument_list|()
decl_stmt|;
name|int
name|numAliases
init|=
name|order
operator|.
name|length
decl_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|bigTableCandidates
init|=
name|MapJoinProcessor
operator|.
name|getBigTableCandidates
argument_list|(
name|originalSMBJoinDesc
operator|.
name|getConds
argument_list|()
argument_list|)
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|aliasToSize
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|context
operator|.
name|getConf
argument_list|()
decl_stmt|;
try|try
block|{
name|long
name|aliasTotalKnownInputSize
init|=
name|getTotalKnownInputSize
argument_list|(
name|context
argument_list|,
name|currJoinWork
operator|.
name|getMapWork
argument_list|()
argument_list|,
name|pathToAliases
argument_list|,
name|aliasToSize
argument_list|)
decl_stmt|;
name|long
name|ThresholdOfSmallTblSizeSum
init|=
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESMALLTABLESFILESIZE
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|bigTablePosition
init|=
literal|0
init|;
name|bigTablePosition
operator|<
name|numAliases
condition|;
name|bigTablePosition
operator|++
control|)
block|{
comment|// this table cannot be big table
if|if
condition|(
operator|!
name|bigTableCandidates
operator|.
name|contains
argument_list|(
name|bigTablePosition
argument_list|)
condition|)
block|{
continue|continue;
block|}
comment|// create map join task for the given big table position
name|MapRedTask
name|newTask
init|=
name|convertSMBTaskToMapJoinTask
argument_list|(
name|currJoinWork
argument_list|,
name|bigTablePosition
argument_list|,
name|newSMBJoinOp
argument_list|)
decl_stmt|;
name|MapWork
name|mapWork
init|=
name|newTask
operator|.
name|getWork
argument_list|()
operator|.
name|getMapWork
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
argument_list|>
name|parentOp
init|=
name|originalSMBJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|bigTablePosition
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|GenMapRedUtils
operator|.
name|findAliases
argument_list|(
name|mapWork
argument_list|,
name|parentOp
argument_list|)
decl_stmt|;
name|long
name|aliasKnownSize
init|=
name|Utilities
operator|.
name|sumOf
argument_list|(
name|aliasToSize
argument_list|,
name|aliases
argument_list|)
decl_stmt|;
if|if
condition|(
name|aliasKnownSize
operator|>
literal|0
condition|)
block|{
name|long
name|smallTblTotalKnownSize
init|=
name|aliasTotalKnownInputSize
operator|-
name|aliasKnownSize
decl_stmt|;
if|if
condition|(
name|smallTblTotalKnownSize
operator|>
name|ThresholdOfSmallTblSizeSum
condition|)
block|{
comment|// this table is not good to be a big table.
continue|continue;
block|}
block|}
comment|// add into conditional task
name|listWorks
operator|.
name|add
argument_list|(
name|newTask
operator|.
name|getWork
argument_list|()
argument_list|)
expr_stmt|;
name|listTasks
operator|.
name|add
argument_list|(
name|newTask
argument_list|)
expr_stmt|;
name|newTask
operator|.
name|setTaskTag
argument_list|(
name|Task
operator|.
name|CONVERTED_MAPJOIN
argument_list|)
expr_stmt|;
name|newTask
operator|.
name|setFetchSource
argument_list|(
name|currTask
operator|.
name|isFetchSource
argument_list|()
argument_list|)
expr_stmt|;
comment|// set up backup task
name|newTask
operator|.
name|setBackupTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
name|newTask
operator|.
name|setBackupChildrenTasks
argument_list|(
name|currTask
operator|.
name|getChildTasks
argument_list|()
argument_list|)
expr_stmt|;
comment|// put the mapping task to aliases
name|taskToAliases
operator|.
name|put
argument_list|(
name|newTask
argument_list|,
name|aliases
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
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Generate Map Join Task Error: "
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// insert current common join task to conditional task
name|listWorks
operator|.
name|add
argument_list|(
name|currTask
operator|.
name|getWork
argument_list|()
argument_list|)
expr_stmt|;
name|listTasks
operator|.
name|add
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
comment|// clear JoinTree and OP Parse Context
name|currWork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setLeftInputJoin
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|currWork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setBaseSrc
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|currWork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setMapAliases
argument_list|(
literal|null
argument_list|)
expr_stmt|;
comment|// create conditional task and insert conditional task into task tree
name|ConditionalWork
name|cndWork
init|=
operator|new
name|ConditionalWork
argument_list|(
name|listWorks
argument_list|)
decl_stmt|;
name|ConditionalTask
name|cndTsk
init|=
operator|(
name|ConditionalTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
name|cndWork
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|cndTsk
operator|.
name|setListTasks
argument_list|(
name|listTasks
argument_list|)
expr_stmt|;
comment|// set resolver and resolver context
name|cndTsk
operator|.
name|setResolver
argument_list|(
operator|new
name|ConditionalResolverCommonJoin
argument_list|()
argument_list|)
expr_stmt|;
name|ConditionalResolverCommonJoinCtx
name|resolverCtx
init|=
operator|new
name|ConditionalResolverCommonJoinCtx
argument_list|()
decl_stmt|;
name|resolverCtx
operator|.
name|setPathToAliases
argument_list|(
name|pathToAliases
argument_list|)
expr_stmt|;
name|resolverCtx
operator|.
name|setAliasToKnownSize
argument_list|(
name|aliasToSize
argument_list|)
expr_stmt|;
name|resolverCtx
operator|.
name|setTaskToAliases
argument_list|(
name|taskToAliases
argument_list|)
expr_stmt|;
name|resolverCtx
operator|.
name|setCommonJoinTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
name|resolverCtx
operator|.
name|setLocalTmpDir
argument_list|(
name|context
operator|.
name|getLocalScratchDir
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|resolverCtx
operator|.
name|setHdfsTmpDir
argument_list|(
name|context
operator|.
name|getMRScratchDir
argument_list|()
argument_list|)
expr_stmt|;
name|cndTsk
operator|.
name|setResolverCtx
argument_list|(
name|resolverCtx
argument_list|)
expr_stmt|;
comment|// replace the current task with the new generated conditional task
name|replaceTaskWithConditionalTask
argument_list|(
name|currTask
argument_list|,
name|cndTsk
argument_list|)
expr_stmt|;
return|return
name|cndTsk
return|;
block|}
comment|/**    * If a join/union is followed by a SMB join, this cannot be converted to a conditional task.    */
specifier|private
name|boolean
name|reducerAllowedSMBJoinOp
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|reducer
parameter_list|)
block|{
while|while
condition|(
name|reducer
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|reducer
operator|.
name|opAllowedBeforeSortMergeJoin
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|childOps
init|=
name|reducer
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|childOps
operator|==
literal|null
operator|)
operator|||
operator|(
name|childOps
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// multi-table inserts not supported
if|if
condition|(
name|childOps
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
return|return
literal|false
return|;
block|}
name|reducer
operator|=
name|childOps
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|SMBMapJoinOperator
name|getSMBMapJoinOp
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|currOp
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|reducer
parameter_list|)
block|{
name|SMBMapJoinOperator
name|ret
init|=
literal|null
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|currOp
operator|instanceof
name|SMBMapJoinOperator
condition|)
block|{
if|if
condition|(
name|ret
operator|!=
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|ret
operator|=
operator|(
name|SMBMapJoinOperator
operator|)
name|currOp
expr_stmt|;
block|}
comment|// Does any operator in the tree stop the task from being converted to a conditional task
if|if
condition|(
operator|!
name|currOp
operator|.
name|opAllowedBeforeSortMergeJoin
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|childOps
init|=
name|currOp
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|childOps
operator|==
literal|null
operator|)
operator|||
operator|(
name|childOps
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
return|return
name|reducerAllowedSMBJoinOp
argument_list|(
name|reducer
argument_list|)
condition|?
name|ret
else|:
literal|null
return|;
block|}
comment|// multi-table inserts not supported
if|if
condition|(
name|childOps
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
name|currOp
operator|=
name|childOps
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|SMBMapJoinOperator
name|getSMBMapJoinOp
parameter_list|(
name|MapredWork
name|work
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|work
operator|!=
literal|null
operator|&&
name|work
operator|.
name|getReduceWork
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|reducer
init|=
name|work
operator|.
name|getReduceWork
argument_list|()
operator|.
name|getReducer
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|work
operator|.
name|getMapWork
argument_list|()
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|SMBMapJoinOperator
name|smbMapJoinOp
init|=
name|getSMBMapJoinOp
argument_list|(
name|op
argument_list|,
name|reducer
argument_list|)
decl_stmt|;
if|if
condition|(
name|smbMapJoinOp
operator|!=
literal|null
condition|)
block|{
return|return
name|smbMapJoinOp
return|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|MapJoinOperator
name|getMapJoinOperator
parameter_list|(
name|MapRedTask
name|task
parameter_list|,
name|MapredWork
name|work
parameter_list|,
name|SMBMapJoinOperator
name|oldSMBJoinOp
parameter_list|,
name|int
name|mapJoinPos
parameter_list|)
throws|throws
name|SemanticException
block|{
name|SMBMapJoinOperator
name|newSMBJoinOp
init|=
name|getSMBMapJoinOp
argument_list|(
name|task
operator|.
name|getWork
argument_list|()
argument_list|)
decl_stmt|;
comment|// generate the map join operator
return|return
name|MapJoinProcessor
operator|.
name|convertSMBJoinToMapJoin
argument_list|(
name|physicalContext
operator|.
name|getConf
argument_list|()
argument_list|,
name|newSMBJoinOp
argument_list|,
name|mapJoinPos
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
end_class

end_unit

