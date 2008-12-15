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
package|;
end_package

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
name|ArrayList
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Stack
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
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|TableScanOperator
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
name|JoinOperator
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
name|OperatorFactory
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
name|mapredWork
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
name|reduceSinkDesc
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
name|tableDesc
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
name|partitionDesc
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
name|fileSinkDesc
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
name|PlanUtils
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
name|*
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
name|*
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
name|optimizer
operator|.
name|GenMRProcContext
operator|.
name|GenMapRedCtx
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_comment
comment|/**  * General utility common functions for the Processor to convert operator into map-reduce tasks  */
end_comment

begin_class
specifier|public
class|class
name|GenMapRedUtils
block|{
specifier|private
specifier|static
name|Log
name|LOG
decl_stmt|;
static|static
block|{
name|LOG
operator|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"org.apache.hadoop.hive.ql.optimizer.GenMapRedUtils"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Initialize the current plan by adding it to root tasks    * @param op the reduce sink operator encountered    * @param opProcCtx processing context    */
specifier|public
specifier|static
name|void
name|initPlan
parameter_list|(
name|ReduceSinkOperator
name|op
parameter_list|,
name|GenMRProcContext
name|opProcCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|reducer
init|=
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|GenMapRedCtx
argument_list|>
name|mapCurrCtx
init|=
name|opProcCtx
operator|.
name|getMapCurrCtx
argument_list|()
decl_stmt|;
name|GenMapRedCtx
name|mapredCtx
init|=
name|mapCurrCtx
operator|.
name|get
argument_list|(
name|op
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|mapredCtx
operator|.
name|getCurrTask
argument_list|()
decl_stmt|;
name|mapredWork
name|plan
init|=
operator|(
name|mapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|opTaskMap
init|=
name|opProcCtx
operator|.
name|getOpTaskMap
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTopOp
init|=
name|opProcCtx
operator|.
name|getCurrTopOp
argument_list|()
decl_stmt|;
name|opTaskMap
operator|.
name|put
argument_list|(
name|reducer
argument_list|,
name|currTask
argument_list|)
expr_stmt|;
name|plan
operator|.
name|setReducer
argument_list|(
name|reducer
argument_list|)
expr_stmt|;
name|reduceSinkDesc
name|desc
init|=
operator|(
name|reduceSinkDesc
operator|)
name|op
operator|.
name|getConf
argument_list|()
decl_stmt|;
comment|// The number of reducers may be specified in the plan in some cases, or may need to be inferred
if|if
condition|(
name|desc
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|desc
operator|.
name|getNumReducers
argument_list|()
operator|!=
operator|-
literal|1
condition|)
name|plan
operator|.
name|setNumReduceTasks
argument_list|(
operator|new
name|Integer
argument_list|(
name|desc
operator|.
name|getNumReducers
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
elseif|else
if|if
condition|(
name|desc
operator|.
name|getInferNumReducers
argument_list|()
operator|==
literal|true
condition|)
name|plan
operator|.
name|setInferNumReducers
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
init|=
name|opProcCtx
operator|.
name|getRootTasks
argument_list|()
decl_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
if|if
condition|(
name|reducer
operator|.
name|getClass
argument_list|()
operator|==
name|JoinOperator
operator|.
name|class
condition|)
name|plan
operator|.
name|setNeedsTagging
argument_list|(
literal|true
argument_list|)
expr_stmt|;
assert|assert
name|currTopOp
operator|!=
literal|null
assert|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|seenOps
init|=
name|opProcCtx
operator|.
name|getSeenOps
argument_list|()
decl_stmt|;
name|String
name|currAliasId
init|=
name|opProcCtx
operator|.
name|getCurrAliasId
argument_list|()
decl_stmt|;
name|seenOps
operator|.
name|add
argument_list|(
name|currTopOp
argument_list|)
expr_stmt|;
name|setTaskPlan
argument_list|(
name|currAliasId
argument_list|,
name|currTopOp
argument_list|,
name|plan
argument_list|,
literal|false
argument_list|,
name|opProcCtx
argument_list|)
expr_stmt|;
name|currTopOp
operator|=
literal|null
expr_stmt|;
name|currAliasId
operator|=
literal|null
expr_stmt|;
name|opProcCtx
operator|.
name|setCurrTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
name|opProcCtx
operator|.
name|setCurrTopOp
argument_list|(
name|currTopOp
argument_list|)
expr_stmt|;
name|opProcCtx
operator|.
name|setCurrAliasId
argument_list|(
name|currAliasId
argument_list|)
expr_stmt|;
block|}
comment|/**    * Merge the current task with the task for the current reducer    * @param task for the old task for the current reducer    * @param opProcCtx processing context    */
specifier|public
specifier|static
name|void
name|joinPlan
parameter_list|(
name|ReduceSinkOperator
name|op
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|oldTask
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|,
name|GenMRProcContext
name|opProcCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|task
decl_stmt|;
name|mapredWork
name|plan
init|=
operator|(
name|mapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTopOp
init|=
name|opProcCtx
operator|.
name|getCurrTopOp
argument_list|()
decl_stmt|;
comment|// terminate the old task and make current task dependent on it
if|if
condition|(
name|oldTask
operator|!=
literal|null
condition|)
block|{
name|splitTasks
argument_list|(
name|op
argument_list|,
name|oldTask
argument_list|,
name|currTask
argument_list|,
name|opProcCtx
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|currTopOp
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|seenOps
init|=
name|opProcCtx
operator|.
name|getSeenOps
argument_list|()
decl_stmt|;
name|String
name|currAliasId
init|=
name|opProcCtx
operator|.
name|getCurrAliasId
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|seenOps
operator|.
name|contains
argument_list|(
name|currTopOp
argument_list|)
condition|)
block|{
name|seenOps
operator|.
name|add
argument_list|(
name|currTopOp
argument_list|)
expr_stmt|;
name|setTaskPlan
argument_list|(
name|currAliasId
argument_list|,
name|currTopOp
argument_list|,
name|plan
argument_list|,
literal|false
argument_list|,
name|opProcCtx
argument_list|)
expr_stmt|;
block|}
name|currTopOp
operator|=
literal|null
expr_stmt|;
name|opProcCtx
operator|.
name|setCurrTopOp
argument_list|(
name|currTopOp
argument_list|)
expr_stmt|;
block|}
name|opProcCtx
operator|.
name|setCurrTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
block|}
comment|/**    * Split the current plan by creating a temporary destination    * @param op the reduce sink operator encountered    * @param opProcCtx processing context    */
specifier|public
specifier|static
name|void
name|splitPlan
parameter_list|(
name|ReduceSinkOperator
name|op
parameter_list|,
name|GenMRProcContext
name|opProcCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Generate a new task
name|mapredWork
name|cplan
init|=
name|getMapRedWork
argument_list|()
decl_stmt|;
name|ParseContext
name|parseCtx
init|=
name|opProcCtx
operator|.
name|getParseCtx
argument_list|()
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|redTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|cplan
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|reducer
init|=
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Add the reducer
name|cplan
operator|.
name|setReducer
argument_list|(
name|reducer
argument_list|)
expr_stmt|;
name|reduceSinkDesc
name|desc
init|=
operator|(
name|reduceSinkDesc
operator|)
name|op
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|getNumReducers
argument_list|()
operator|!=
operator|-
literal|1
condition|)
name|cplan
operator|.
name|setNumReduceTasks
argument_list|(
operator|new
name|Integer
argument_list|(
name|desc
operator|.
name|getNumReducers
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
else|else
name|cplan
operator|.
name|setInferNumReducers
argument_list|(
name|desc
operator|.
name|getInferNumReducers
argument_list|()
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|opTaskMap
init|=
name|opProcCtx
operator|.
name|getOpTaskMap
argument_list|()
decl_stmt|;
name|opTaskMap
operator|.
name|put
argument_list|(
name|reducer
argument_list|,
name|redTask
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|opProcCtx
operator|.
name|getCurrTask
argument_list|()
decl_stmt|;
name|splitTasks
argument_list|(
name|op
argument_list|,
name|currTask
argument_list|,
name|redTask
argument_list|,
name|opProcCtx
argument_list|)
expr_stmt|;
name|opProcCtx
operator|.
name|getRootOps
argument_list|()
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
comment|/**    * set the current task in the mapredWork    * @param alias_id current alias    * @param topOp    the top operator of the stack    * @param plan     current plan    * @param local    whether you need to add to map-reduce or local work    * @param opProcCtx processing context    */
specifier|public
specifier|static
name|void
name|setTaskPlan
parameter_list|(
name|String
name|alias_id
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|topOp
parameter_list|,
name|mapredWork
name|plan
parameter_list|,
name|boolean
name|local
parameter_list|,
name|GenMRProcContext
name|opProcCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ParseContext
name|parseCtx
init|=
name|opProcCtx
operator|.
name|getParseCtx
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|local
condition|)
block|{
comment|// Generate the map work for this alias_id
name|PartitionPruner
name|pruner
init|=
name|parseCtx
operator|.
name|getAliasToPruner
argument_list|()
operator|.
name|get
argument_list|(
name|alias_id
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Partition
argument_list|>
name|parts
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// pass both confirmed and unknown partitions through the map-reduce framework
name|PartitionPruner
operator|.
name|PrunedPartitionList
name|partsList
init|=
name|pruner
operator|.
name|prune
argument_list|()
decl_stmt|;
name|parts
operator|=
name|partsList
operator|.
name|getConfirmedPartns
argument_list|()
expr_stmt|;
name|parts
operator|.
name|addAll
argument_list|(
name|partsList
operator|.
name|getUnknownPartns
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
comment|// Has to use full name to make sure it does not conflict with org.apache.commons.lang.StringUtils
name|LOG
operator|.
name|error
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|SamplePruner
name|samplePruner
init|=
name|parseCtx
operator|.
name|getAliasToSamplePruner
argument_list|()
operator|.
name|get
argument_list|(
name|alias_id
argument_list|)
decl_stmt|;
for|for
control|(
name|Partition
name|part
range|:
name|parts
control|)
block|{
comment|// Later the properties have to come from the partition as opposed
comment|// to from the table in order to support versioning.
name|Path
name|paths
index|[]
decl_stmt|;
if|if
condition|(
name|samplePruner
operator|!=
literal|null
condition|)
block|{
name|paths
operator|=
name|samplePruner
operator|.
name|prune
argument_list|(
name|part
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|paths
operator|=
name|part
operator|.
name|getPath
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|Path
name|p
range|:
name|paths
control|)
block|{
name|String
name|path
init|=
name|p
operator|.
name|toString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding "
operator|+
name|path
operator|+
literal|" of table"
operator|+
name|alias_id
argument_list|)
expr_stmt|;
comment|// Add the path to alias mapping
if|if
condition|(
name|plan
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|get
argument_list|(
name|path
argument_list|)
operator|==
literal|null
condition|)
block|{
name|plan
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|put
argument_list|(
name|path
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|plan
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|get
argument_list|(
name|path
argument_list|)
operator|.
name|add
argument_list|(
name|alias_id
argument_list|)
expr_stmt|;
name|plan
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|Utilities
operator|.
name|getPartitionDesc
argument_list|(
name|part
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Information added for path "
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
block|}
name|plan
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|put
argument_list|(
name|alias_id
argument_list|,
name|topOp
argument_list|)
expr_stmt|;
name|setKeyAndValueDesc
argument_list|(
name|plan
argument_list|,
name|topOp
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Created Map Work for "
operator|+
name|alias_id
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|FileSinkOperator
name|fOp
init|=
operator|(
name|FileSinkOperator
operator|)
name|topOp
decl_stmt|;
name|fileSinkDesc
name|fConf
init|=
operator|(
name|fileSinkDesc
operator|)
name|fOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
comment|// populate local work if needed
block|}
block|}
comment|/**    * set key and value descriptor    * @param plan     current plan    * @param topOp    current top operator in the path    */
specifier|private
specifier|static
name|void
name|setKeyAndValueDesc
parameter_list|(
name|mapredWork
name|plan
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|topOp
parameter_list|)
block|{
if|if
condition|(
name|topOp
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
name|ReduceSinkOperator
name|rs
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|topOp
decl_stmt|;
name|plan
operator|.
name|setKeyDesc
argument_list|(
name|rs
operator|.
name|getConf
argument_list|()
operator|.
name|getKeySerializeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|tag
init|=
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|rs
operator|.
name|getConf
argument_list|()
operator|.
name|getTag
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|tableDesc
argument_list|>
name|tagToSchema
init|=
name|plan
operator|.
name|getTagToValueDesc
argument_list|()
decl_stmt|;
while|while
condition|(
name|tag
operator|+
literal|1
operator|>
name|tagToSchema
operator|.
name|size
argument_list|()
condition|)
block|{
name|tagToSchema
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|tagToSchema
operator|.
name|set
argument_list|(
name|tag
argument_list|,
name|rs
operator|.
name|getConf
argument_list|()
operator|.
name|getValueSerializeInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|children
init|=
name|topOp
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
range|:
name|children
control|)
block|{
name|setKeyAndValueDesc
argument_list|(
name|plan
argument_list|,
name|op
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * create a new plan and return    * @return the new plan    */
specifier|public
specifier|static
name|mapredWork
name|getMapRedWork
parameter_list|()
block|{
name|mapredWork
name|work
init|=
operator|new
name|mapredWork
argument_list|()
decl_stmt|;
name|work
operator|.
name|setPathToAliases
argument_list|(
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|work
operator|.
name|setPathToPartitionInfo
argument_list|(
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|partitionDesc
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|work
operator|.
name|setAliasToWork
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|work
operator|.
name|setTagToValueDesc
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|tableDesc
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|work
operator|.
name|setReducer
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
name|work
return|;
block|}
comment|/**    * insert in the map for the operator to row resolver    * @param op operator created    * @param rr row resolver    * @param parseCtx parse context    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|private
specifier|static
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|putOpInsertMap
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
parameter_list|,
name|RowResolver
name|rr
parameter_list|,
name|ParseContext
name|parseCtx
parameter_list|)
block|{
name|OpParseContext
name|ctx
init|=
operator|new
name|OpParseContext
argument_list|(
name|rr
argument_list|)
decl_stmt|;
name|parseCtx
operator|.
name|getOpParseCtx
argument_list|()
operator|.
name|put
argument_list|(
name|op
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
return|return
name|op
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
comment|/**    * Merge the tasks - by creating a temporary file between them.    * @param op reduce sink operator being processed    * @param oldTask the parent task    * @param task the child task    * @param opProcCtx context    **/
specifier|private
specifier|static
name|void
name|splitTasks
parameter_list|(
name|ReduceSinkOperator
name|op
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|oldTask
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|,
name|GenMRProcContext
name|opProcCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|task
decl_stmt|;
name|mapredWork
name|plan
init|=
operator|(
name|mapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTopOp
init|=
name|opProcCtx
operator|.
name|getCurrTopOp
argument_list|()
decl_stmt|;
name|ParseContext
name|parseCtx
init|=
name|opProcCtx
operator|.
name|getParseCtx
argument_list|()
decl_stmt|;
name|oldTask
operator|.
name|addDependentTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
comment|// generate the temporary file
name|String
name|scratchDir
init|=
name|opProcCtx
operator|.
name|getScratchDir
argument_list|()
decl_stmt|;
name|int
name|randomid
init|=
name|opProcCtx
operator|.
name|getRandomId
argument_list|()
decl_stmt|;
name|int
name|pathid
init|=
name|opProcCtx
operator|.
name|getPathId
argument_list|()
decl_stmt|;
name|String
name|taskTmpDir
init|=
name|scratchDir
operator|+
name|File
operator|.
name|separator
operator|+
name|randomid
operator|+
literal|'.'
operator|+
name|pathid
decl_stmt|;
name|pathid
operator|++
expr_stmt|;
name|opProcCtx
operator|.
name|setPathId
argument_list|(
name|pathid
argument_list|)
expr_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|parent
init|=
name|op
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|tableDesc
name|tt_desc
init|=
name|PlanUtils
operator|.
name|getBinaryTableDesc
argument_list|(
name|PlanUtils
operator|.
name|getFieldSchemasFromRowSchema
argument_list|(
name|parent
operator|.
name|getSchema
argument_list|()
argument_list|,
literal|"temporarycol"
argument_list|)
argument_list|)
decl_stmt|;
comment|// Create a file sink operator for this file name
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|fs_op
init|=
name|putOpInsertMap
argument_list|(
name|OperatorFactory
operator|.
name|get
argument_list|(
operator|new
name|fileSinkDesc
argument_list|(
name|taskTmpDir
argument_list|,
name|tt_desc
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|COMPRESSINTERMEDIATE
argument_list|)
argument_list|)
argument_list|,
name|parent
operator|.
name|getSchema
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|,
name|parseCtx
argument_list|)
decl_stmt|;
comment|// replace the reduce child with this operator
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|childOpList
init|=
name|parent
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|childOpList
operator|.
name|size
argument_list|()
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
name|childOpList
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|==
name|op
condition|)
block|{
name|childOpList
operator|.
name|set
argument_list|(
name|pos
argument_list|,
name|fs_op
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parentOpList
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|parentOpList
operator|.
name|add
argument_list|(
name|parent
argument_list|)
expr_stmt|;
name|fs_op
operator|.
name|setParentOperators
argument_list|(
name|parentOpList
argument_list|)
expr_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|reducer
init|=
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|streamDesc
decl_stmt|;
name|mapredWork
name|cplan
init|=
operator|(
name|mapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|reducer
operator|.
name|getClass
argument_list|()
operator|==
name|JoinOperator
operator|.
name|class
condition|)
block|{
name|String
name|origStreamDesc
decl_stmt|;
name|streamDesc
operator|=
literal|"$INTNAME"
expr_stmt|;
name|origStreamDesc
operator|=
name|streamDesc
expr_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|cplan
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|streamDesc
argument_list|)
operator|!=
literal|null
condition|)
name|streamDesc
operator|=
name|origStreamDesc
operator|.
name|concat
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
operator|++
name|pos
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
name|streamDesc
operator|=
name|taskTmpDir
expr_stmt|;
comment|// Add the path to alias mapping
if|if
condition|(
name|cplan
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|get
argument_list|(
name|taskTmpDir
argument_list|)
operator|==
literal|null
condition|)
block|{
name|cplan
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|put
argument_list|(
name|taskTmpDir
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|cplan
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|get
argument_list|(
name|taskTmpDir
argument_list|)
operator|.
name|add
argument_list|(
name|streamDesc
argument_list|)
expr_stmt|;
name|cplan
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|put
argument_list|(
name|taskTmpDir
argument_list|,
operator|new
name|partitionDesc
argument_list|(
name|tt_desc
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|cplan
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|put
argument_list|(
name|streamDesc
argument_list|,
name|op
argument_list|)
expr_stmt|;
name|setKeyAndValueDesc
argument_list|(
name|cplan
argument_list|,
name|op
argument_list|)
expr_stmt|;
comment|// TODO: Allocate work to remove the temporary files and make that
comment|// dependent on the redTask
if|if
condition|(
name|reducer
operator|.
name|getClass
argument_list|()
operator|==
name|JoinOperator
operator|.
name|class
condition|)
name|cplan
operator|.
name|setNeedsTagging
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|currTopOp
operator|=
literal|null
expr_stmt|;
name|String
name|currAliasId
init|=
literal|null
decl_stmt|;
name|opProcCtx
operator|.
name|setCurrTopOp
argument_list|(
name|currTopOp
argument_list|)
expr_stmt|;
name|opProcCtx
operator|.
name|setCurrAliasId
argument_list|(
name|currAliasId
argument_list|)
expr_stmt|;
name|opProcCtx
operator|.
name|setCurrTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

