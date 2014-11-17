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
name|MapredLocalTask
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
name|DefaultGraphWalker
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
name|DefaultRuleDispatcher
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
name|lib
operator|.
name|GraphWalker
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
name|Node
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
name|NodeProcessor
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
name|NodeProcessorCtx
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
name|Rule
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
name|RuleRegExp
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
name|TaskGraphWalker
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
name|ConditionalResolver
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
name|ConditionalResolverSkewJoin
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
name|ConditionalResolverSkewJoin
operator|.
name|ConditionalResolverSkewJoinCtx
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

begin_comment
comment|/**  * An implementation of PhysicalPlanResolver. It iterator each MapRedTask to see whether the task  * has a local map work if it has, it will move the local work to a new local map join task. Then it  * will make this new generated task depends on current task's parent task and make current task  * depends on this new generated task.  */
end_comment

begin_class
specifier|public
class|class
name|MapJoinResolver
implements|implements
name|PhysicalPlanResolver
block|{
annotation|@
name|Override
specifier|public
name|PhysicalContext
name|resolve
parameter_list|(
name|PhysicalContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// create dispatcher and graph walker
name|Dispatcher
name|disp
init|=
operator|new
name|LocalMapJoinTaskDispatcher
argument_list|(
name|pctx
argument_list|)
decl_stmt|;
name|TaskGraphWalker
name|ogw
init|=
operator|new
name|TaskGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// get all the tasks nodes from root task
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|topNodes
operator|.
name|addAll
argument_list|(
name|pctx
operator|.
name|getRootTasks
argument_list|()
argument_list|)
expr_stmt|;
comment|// begin to walk through the task tree.
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|pctx
return|;
block|}
comment|/**    * Iterator each tasks. If this task has a local work,create a new task for this local work, named    * MapredLocalTask. then make this new generated task depends on current task's parent task, and    * make current task depends on this new generated task    */
class|class
name|LocalMapJoinTaskDispatcher
implements|implements
name|Dispatcher
block|{
specifier|private
name|PhysicalContext
name|physicalContext
decl_stmt|;
specifier|public
name|LocalMapJoinTaskDispatcher
parameter_list|(
name|PhysicalContext
name|context
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|physicalContext
operator|=
name|context
expr_stmt|;
block|}
specifier|private
name|void
name|processCurrentTask
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
parameter_list|,
name|ConditionalTask
name|conditionalTask
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// get current mapred work and its local work
name|MapredWork
name|mapredWork
init|=
operator|(
name|MapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|MapredLocalWork
name|localwork
init|=
name|mapredWork
operator|.
name|getMapWork
argument_list|()
operator|.
name|getMapRedLocalWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|localwork
operator|!=
literal|null
condition|)
block|{
comment|// get the context info and set up the shared tmp URI
name|Context
name|ctx
init|=
name|physicalContext
operator|.
name|getContext
argument_list|()
decl_stmt|;
name|Path
name|tmpPath
init|=
name|Utilities
operator|.
name|generateTmpPath
argument_list|(
name|ctx
operator|.
name|getLocalTmpPath
argument_list|()
argument_list|,
name|currTask
operator|.
name|getId
argument_list|()
argument_list|)
decl_stmt|;
name|localwork
operator|.
name|setTmpPath
argument_list|(
name|tmpPath
argument_list|)
expr_stmt|;
name|mapredWork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setTmpHDFSPath
argument_list|(
name|Utilities
operator|.
name|generateTmpPath
argument_list|(
name|ctx
operator|.
name|getMRTmpPath
argument_list|()
argument_list|,
name|currTask
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// create a task for this local work; right now, this local work is shared
comment|// by the original MapredTask and this new generated MapredLocalTask.
name|MapredLocalTask
name|localTask
init|=
operator|(
name|MapredLocalTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
name|localwork
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
comment|// set the backup task from curr task
name|localTask
operator|.
name|setBackupTask
argument_list|(
name|currTask
operator|.
name|getBackupTask
argument_list|()
argument_list|)
expr_stmt|;
name|localTask
operator|.
name|setBackupChildrenTasks
argument_list|(
name|currTask
operator|.
name|getBackupChildrenTasks
argument_list|()
argument_list|)
expr_stmt|;
name|currTask
operator|.
name|setBackupChildrenTasks
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|currTask
operator|.
name|setBackupTask
argument_list|(
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|currTask
operator|.
name|getTaskTag
argument_list|()
operator|==
name|Task
operator|.
name|CONVERTED_MAPJOIN
condition|)
block|{
name|localTask
operator|.
name|setTaskTag
argument_list|(
name|Task
operator|.
name|CONVERTED_MAPJOIN_LOCAL
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|localTask
operator|.
name|setTaskTag
argument_list|(
name|Task
operator|.
name|HINTED_MAPJOIN_LOCAL
argument_list|)
expr_stmt|;
name|currTask
operator|.
name|setTaskTag
argument_list|(
name|Task
operator|.
name|HINTED_MAPJOIN
argument_list|)
expr_stmt|;
block|}
comment|// replace the map join operator to local_map_join operator in the operator tree
comment|// and return all the dummy parent
name|LocalMapJoinProcCtx
name|localMapJoinProcCtx
init|=
name|adjustLocalTask
argument_list|(
name|localTask
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|dummyOps
init|=
name|localMapJoinProcCtx
operator|.
name|getDummyParentOp
argument_list|()
decl_stmt|;
comment|// create new local work and setup the dummy ops
name|MapredLocalWork
name|newLocalWork
init|=
name|localwork
operator|.
name|extractDirectWorks
argument_list|(
name|localMapJoinProcCtx
operator|.
name|getDirectWorks
argument_list|()
argument_list|)
decl_stmt|;
name|newLocalWork
operator|.
name|setDummyParentOp
argument_list|(
name|dummyOps
argument_list|)
expr_stmt|;
name|mapredWork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setMapRedLocalWork
argument_list|(
name|newLocalWork
argument_list|)
expr_stmt|;
if|if
condition|(
name|localwork
operator|.
name|getAliasToFetchWork
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// no alias to stage.. no local task
name|newLocalWork
operator|.
name|setHasStagedAlias
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|currTask
operator|.
name|setBackupTask
argument_list|(
name|localTask
operator|.
name|getBackupTask
argument_list|()
argument_list|)
expr_stmt|;
name|currTask
operator|.
name|setBackupChildrenTasks
argument_list|(
name|localTask
operator|.
name|getBackupChildrenTasks
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|newLocalWork
operator|.
name|setHasStagedAlias
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// get all parent tasks
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parentTasks
init|=
name|currTask
operator|.
name|getParentTasks
argument_list|()
decl_stmt|;
name|currTask
operator|.
name|setParentTasks
argument_list|(
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|parentTasks
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
range|:
name|parentTasks
control|)
block|{
comment|// make new generated task depends on all the parent tasks of current task.
name|tsk
operator|.
name|addDependentTask
argument_list|(
name|localTask
argument_list|)
expr_stmt|;
comment|// remove the current task from its original parent task's dependent task
name|tsk
operator|.
name|removeDependentTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// in this case, current task is in the root tasks
comment|// so add this new task into root tasks and remove the current task from root tasks
if|if
condition|(
name|conditionalTask
operator|==
literal|null
condition|)
block|{
name|physicalContext
operator|.
name|addToRootTask
argument_list|(
name|localTask
argument_list|)
expr_stmt|;
name|physicalContext
operator|.
name|removeFromRootTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// set list task
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|listTask
init|=
name|conditionalTask
operator|.
name|getListTasks
argument_list|()
decl_stmt|;
name|ConditionalWork
name|conditionalWork
init|=
name|conditionalTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|int
name|index
init|=
name|listTask
operator|.
name|indexOf
argument_list|(
name|currTask
argument_list|)
decl_stmt|;
name|listTask
operator|.
name|set
argument_list|(
name|index
argument_list|,
name|localTask
argument_list|)
expr_stmt|;
comment|// set list work
name|List
argument_list|<
name|Serializable
argument_list|>
name|listWork
init|=
operator|(
name|List
argument_list|<
name|Serializable
argument_list|>
operator|)
name|conditionalWork
operator|.
name|getListWorks
argument_list|()
decl_stmt|;
name|index
operator|=
name|listWork
operator|.
name|indexOf
argument_list|(
name|mapredWork
argument_list|)
expr_stmt|;
name|listWork
operator|.
name|set
argument_list|(
name|index
argument_list|,
name|localwork
argument_list|)
expr_stmt|;
name|conditionalWork
operator|.
name|setListWorks
argument_list|(
name|listWork
argument_list|)
expr_stmt|;
name|ConditionalResolver
name|resolver
init|=
name|conditionalTask
operator|.
name|getResolver
argument_list|()
decl_stmt|;
if|if
condition|(
name|resolver
operator|instanceof
name|ConditionalResolverSkewJoin
condition|)
block|{
comment|// get bigKeysDirToTaskMap
name|ConditionalResolverSkewJoinCtx
name|context
init|=
operator|(
name|ConditionalResolverSkewJoinCtx
operator|)
name|conditionalTask
operator|.
name|getResolverCtx
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|Path
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|bigKeysDirToTaskMap
init|=
name|context
operator|.
name|getDirToTaskMap
argument_list|()
decl_stmt|;
comment|// to avoid concurrent modify the hashmap
name|HashMap
argument_list|<
name|Path
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|newbigKeysDirToTaskMap
init|=
operator|new
name|HashMap
argument_list|<
name|Path
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// reset the resolver
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Path
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|entry
range|:
name|bigKeysDirToTaskMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Path
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|task
operator|.
name|equals
argument_list|(
name|currTask
argument_list|)
condition|)
block|{
name|newbigKeysDirToTaskMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|localTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newbigKeysDirToTaskMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|task
argument_list|)
expr_stmt|;
block|}
block|}
name|context
operator|.
name|setDirToTaskMap
argument_list|(
name|newbigKeysDirToTaskMap
argument_list|)
expr_stmt|;
name|conditionalTask
operator|.
name|setResolverCtx
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|resolver
operator|instanceof
name|ConditionalResolverCommonJoin
condition|)
block|{
comment|// get bigKeysDirToTaskMap
name|ConditionalResolverCommonJoinCtx
name|context
init|=
operator|(
name|ConditionalResolverCommonJoinCtx
operator|)
name|conditionalTask
operator|.
name|getResolverCtx
argument_list|()
decl_stmt|;
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
name|context
operator|.
name|getTaskToAliases
argument_list|()
decl_stmt|;
comment|// to avoid concurrent modify the hashmap
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
name|newTaskToAliases
init|=
operator|new
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
argument_list|()
decl_stmt|;
comment|// reset the resolver
for|for
control|(
name|Map
operator|.
name|Entry
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
name|entry
range|:
name|taskToAliases
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|key
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|task
operator|.
name|equals
argument_list|(
name|currTask
argument_list|)
condition|)
block|{
name|newTaskToAliases
operator|.
name|put
argument_list|(
name|localTask
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newTaskToAliases
operator|.
name|put
argument_list|(
name|task
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
block|}
name|context
operator|.
name|setTaskToAliases
argument_list|(
name|newTaskToAliases
argument_list|)
expr_stmt|;
name|conditionalTask
operator|.
name|setResolverCtx
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// make current task depends on this new generated localMapJoinTask
comment|// now localTask is the parent task of the current task
name|localTask
operator|.
name|addDependentTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|dispatch
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
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
operator|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
decl_stmt|;
comment|// not map reduce task or not conditional task, just skip
if|if
condition|(
name|currTask
operator|.
name|isMapRedTask
argument_list|()
condition|)
block|{
if|if
condition|(
name|currTask
operator|instanceof
name|ConditionalTask
condition|)
block|{
comment|// get the list of task
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|taskList
init|=
operator|(
operator|(
name|ConditionalTask
operator|)
name|currTask
operator|)
operator|.
name|getListTasks
argument_list|()
decl_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
range|:
name|taskList
control|)
block|{
if|if
condition|(
name|tsk
operator|.
name|isMapRedTask
argument_list|()
condition|)
block|{
name|this
operator|.
name|processCurrentTask
argument_list|(
name|tsk
argument_list|,
operator|(
operator|(
name|ConditionalTask
operator|)
name|currTask
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|this
operator|.
name|processCurrentTask
argument_list|(
name|currTask
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|// replace the map join operator to local_map_join operator in the operator tree
specifier|private
name|LocalMapJoinProcCtx
name|adjustLocalTask
parameter_list|(
name|MapredLocalTask
name|task
parameter_list|)
throws|throws
name|SemanticException
block|{
name|LocalMapJoinProcCtx
name|localMapJoinProcCtx
init|=
operator|new
name|LocalMapJoinProcCtx
argument_list|(
name|task
argument_list|,
name|physicalContext
operator|.
name|getParseContext
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|opRules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
name|MapJoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|LocalMapJoinProcFactory
operator|.
name|getJoinProc
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest
comment|// matching rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|LocalMapJoinProcFactory
operator|.
name|getDefaultProc
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|localMapJoinProcCtx
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// iterator the reducer operator tree
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|topNodes
operator|.
name|addAll
argument_list|(
name|task
operator|.
name|getWork
argument_list|()
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|localMapJoinProcCtx
return|;
block|}
specifier|public
name|PhysicalContext
name|getPhysicalContext
parameter_list|()
block|{
return|return
name|physicalContext
return|;
block|}
specifier|public
name|void
name|setPhysicalContext
parameter_list|(
name|PhysicalContext
name|physicalContext
parameter_list|)
block|{
name|this
operator|.
name|physicalContext
operator|=
name|physicalContext
expr_stmt|;
block|}
block|}
comment|/**    * A container of current task and parse context.    */
specifier|public
specifier|static
class|class
name|LocalMapJoinProcCtx
implements|implements
name|NodeProcessorCtx
block|{
specifier|private
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currentTask
decl_stmt|;
specifier|private
name|ParseContext
name|parseCtx
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|dummyParentOp
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|isFollowedByGroupBy
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|>
name|directWorks
decl_stmt|;
specifier|public
name|LocalMapJoinProcCtx
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|,
name|ParseContext
name|parseCtx
parameter_list|)
block|{
name|currentTask
operator|=
name|task
expr_stmt|;
name|this
operator|.
name|parseCtx
operator|=
name|parseCtx
expr_stmt|;
name|dummyParentOp
operator|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|directWorks
operator|=
operator|new
name|HashMap
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|isFollowedByGroupBy
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|getCurrentTask
parameter_list|()
block|{
return|return
name|currentTask
return|;
block|}
specifier|public
name|void
name|setCurrentTask
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currentTask
parameter_list|)
block|{
name|this
operator|.
name|currentTask
operator|=
name|currentTask
expr_stmt|;
block|}
specifier|public
name|boolean
name|isFollowedByGroupBy
parameter_list|()
block|{
return|return
name|isFollowedByGroupBy
return|;
block|}
specifier|public
name|void
name|setFollowedByGroupBy
parameter_list|(
name|boolean
name|isFollowedByGroupBy
parameter_list|)
block|{
name|this
operator|.
name|isFollowedByGroupBy
operator|=
name|isFollowedByGroupBy
expr_stmt|;
block|}
specifier|public
name|ParseContext
name|getParseCtx
parameter_list|()
block|{
return|return
name|parseCtx
return|;
block|}
specifier|public
name|void
name|setParseCtx
parameter_list|(
name|ParseContext
name|parseCtx
parameter_list|)
block|{
name|this
operator|.
name|parseCtx
operator|=
name|parseCtx
expr_stmt|;
block|}
specifier|public
name|void
name|setDummyParentOp
parameter_list|(
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|op
parameter_list|)
block|{
name|this
operator|.
name|dummyParentOp
operator|=
name|op
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|getDummyParentOp
parameter_list|()
block|{
return|return
name|this
operator|.
name|dummyParentOp
return|;
block|}
specifier|public
name|void
name|addDummyParentOp
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|)
block|{
name|this
operator|.
name|dummyParentOp
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setDirectWorks
parameter_list|(
name|Map
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|>
name|directWorks
parameter_list|)
block|{
name|this
operator|.
name|directWorks
operator|=
name|directWorks
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|>
name|getDirectWorks
parameter_list|()
block|{
return|return
name|directWorks
return|;
block|}
specifier|public
name|void
name|addDirectWorks
parameter_list|(
name|MapJoinOperator
name|mapJoinOp
parameter_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|directOperators
parameter_list|)
block|{
name|directWorks
operator|.
name|put
argument_list|(
name|mapJoinOp
argument_list|,
name|directOperators
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

