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
name|parse
operator|.
name|spark
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
name|common
operator|.
name|ObjectPair
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
name|exec
operator|.
name|DependencyCollectionTask
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
name|UnionOperator
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
name|spark
operator|.
name|SparkTask
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
name|spark
operator|.
name|SparkUtilities
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
name|ReadEntity
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
name|plan
operator|.
name|BaseWork
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
name|DependencyCollectionWork
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
name|SparkEdgeProperty
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
name|LinkedHashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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

begin_comment
comment|/**  * GenSparkProcContext maintains information about the tasks and operators  * as we walk the operator tree to break them into SparkTasks.  *  * Cloned from GenTezProcContext.  *  */
end_comment

begin_class
specifier|public
class|class
name|GenSparkProcContext
implements|implements
name|NodeProcessorCtx
block|{
specifier|public
specifier|final
name|ParseContext
name|parseContext
decl_stmt|;
specifier|public
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|public
specifier|final
name|List
argument_list|<
name|Task
argument_list|<
name|MoveWork
argument_list|>
argument_list|>
name|moveTask
decl_stmt|;
comment|// rootTasks is the entry point for all generated tasks
specifier|public
specifier|final
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
decl_stmt|;
specifier|public
specifier|final
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
decl_stmt|;
specifier|public
specifier|final
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
decl_stmt|;
comment|// holds the root of the operator tree we're currently processing
comment|// this could be a table scan, but also a join, ptf, etc (i.e.:
comment|// first operator of a reduce task.
specifier|public
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|currentRootOperator
decl_stmt|;
comment|// this is the original parent of the currentRootOperator as we scan
comment|// through the graph. A root operator might have multiple parents and
comment|// we just use this one to remember where we came from in the current
comment|// walk.
specifier|public
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOfRoot
decl_stmt|;
comment|// Spark task we're currently processing
specifier|public
name|SparkTask
name|currentTask
decl_stmt|;
comment|// last work we've processed (in order to hook it up to the current
comment|// one.
specifier|public
name|BaseWork
name|preceedingWork
decl_stmt|;
comment|// map that keeps track of the last operator of a task to the following work
comment|// of this operator. This is used for connecting them later.
specifier|public
specifier|final
name|Map
argument_list|<
name|ReduceSinkOperator
argument_list|,
name|ObjectPair
argument_list|<
name|SparkEdgeProperty
argument_list|,
name|ReduceWork
argument_list|>
argument_list|>
name|leafOpToFollowingWorkInfo
decl_stmt|;
comment|// a map that keeps track of work that need to be linked while
comment|// traversing an operator tree
specifier|public
specifier|final
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|SparkEdgeProperty
argument_list|>
argument_list|>
name|linkOpWithWorkMap
decl_stmt|;
comment|// a map to keep track of what reduce sinks have to be hooked up to
comment|// map join work
specifier|public
specifier|final
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|List
argument_list|<
name|ReduceSinkOperator
argument_list|>
argument_list|>
name|linkWorkWithReduceSinkMap
decl_stmt|;
comment|// map that says which mapjoin belongs to which work item
specifier|public
specifier|final
name|Map
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
name|mapJoinWorkMap
decl_stmt|;
comment|// Map to keep track of which SMB Join operators and their information to annotate their MapWork with.
specifier|public
specifier|final
name|Map
argument_list|<
name|SMBMapJoinOperator
argument_list|,
name|SparkSMBMapJoinInfo
argument_list|>
name|smbMapJoinCtxMap
decl_stmt|;
comment|// a map to keep track of which root generated which work
specifier|public
specifier|final
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|BaseWork
argument_list|>
name|rootToWorkMap
decl_stmt|;
comment|// a map to keep track of which child generated with work
specifier|public
specifier|final
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|List
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
name|childToWorkMap
decl_stmt|;
comment|// we need to keep the original list of operators in the map join to know
comment|// what position in the mapjoin the different parent work items will have.
specifier|public
specifier|final
name|Map
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|mapJoinParentMap
decl_stmt|;
comment|// remember the dummy ops we created
specifier|public
specifier|final
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|linkChildOpWithDummyOp
decl_stmt|;
comment|// used to group dependent tasks for multi table inserts
specifier|public
specifier|final
name|DependencyCollectionTask
name|dependencyTask
decl_stmt|;
comment|// remember map joins as we encounter them.
specifier|public
specifier|final
name|Set
argument_list|<
name|MapJoinOperator
argument_list|>
name|currentMapJoinOperators
decl_stmt|;
comment|// used to hook up unions
specifier|public
specifier|final
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|BaseWork
argument_list|>
name|unionWorkMap
decl_stmt|;
specifier|public
specifier|final
name|List
argument_list|<
name|UnionOperator
argument_list|>
name|currentUnionOperators
decl_stmt|;
specifier|public
specifier|final
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|workWithUnionOperators
decl_stmt|;
specifier|public
specifier|final
name|Set
argument_list|<
name|FileSinkOperator
argument_list|>
name|fileSinkSet
decl_stmt|;
specifier|public
specifier|final
name|Map
argument_list|<
name|FileSinkOperator
argument_list|,
name|List
argument_list|<
name|FileSinkOperator
argument_list|>
argument_list|>
name|fileSinkMap
decl_stmt|;
comment|// Alias to operator map, from the semantic analyzer.
comment|// This is necessary as sometimes semantic analyzer's mapping is different than operator's own alias.
specifier|public
specifier|final
name|Map
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
name|topOps
decl_stmt|;
comment|// The set of pruning sinks
specifier|public
specifier|final
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|pruningSinkSet
decl_stmt|;
comment|// The set of TableScanOperators for pruning OP trees
specifier|public
specifier|final
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|clonedPruningTableScanSet
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|GenSparkProcContext
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|ParseContext
name|parseContext
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|MoveWork
argument_list|>
argument_list|>
name|moveTask
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
name|rootTasks
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|Map
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
name|topOps
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|parseContext
operator|=
name|parseContext
expr_stmt|;
name|this
operator|.
name|moveTask
operator|=
name|moveTask
expr_stmt|;
name|this
operator|.
name|rootTasks
operator|=
name|rootTasks
expr_stmt|;
name|this
operator|.
name|inputs
operator|=
name|inputs
expr_stmt|;
name|this
operator|.
name|outputs
operator|=
name|outputs
expr_stmt|;
name|this
operator|.
name|topOps
operator|=
name|topOps
expr_stmt|;
name|this
operator|.
name|currentTask
operator|=
name|SparkUtilities
operator|.
name|createSparkTask
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|rootTasks
operator|.
name|add
argument_list|(
name|currentTask
argument_list|)
expr_stmt|;
name|this
operator|.
name|leafOpToFollowingWorkInfo
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|ReduceSinkOperator
argument_list|,
name|ObjectPair
argument_list|<
name|SparkEdgeProperty
argument_list|,
name|ReduceWork
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|linkOpWithWorkMap
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|SparkEdgeProperty
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|linkWorkWithReduceSinkMap
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|BaseWork
argument_list|,
name|List
argument_list|<
name|ReduceSinkOperator
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|smbMapJoinCtxMap
operator|=
operator|new
name|HashMap
argument_list|<
name|SMBMapJoinOperator
argument_list|,
name|SparkSMBMapJoinInfo
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|mapJoinWorkMap
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|rootToWorkMap
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|BaseWork
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|childToWorkMap
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|List
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|mapJoinParentMap
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|currentMapJoinOperators
operator|=
operator|new
name|LinkedHashSet
argument_list|<
name|MapJoinOperator
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|linkChildOpWithDummyOp
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|dependencyTask
operator|=
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_MULTI_INSERT_MOVE_TASKS_SHARE_DEPENDENCIES
argument_list|)
condition|?
operator|(
name|DependencyCollectionTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DependencyCollectionWork
argument_list|()
argument_list|,
name|conf
argument_list|)
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|unionWorkMap
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|BaseWork
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|currentUnionOperators
operator|=
operator|new
name|LinkedList
argument_list|<
name|UnionOperator
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|workWithUnionOperators
operator|=
operator|new
name|LinkedHashSet
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|fileSinkSet
operator|=
operator|new
name|LinkedHashSet
argument_list|<
name|FileSinkOperator
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|fileSinkMap
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|FileSinkOperator
argument_list|,
name|List
argument_list|<
name|FileSinkOperator
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|pruningSinkSet
operator|=
operator|new
name|LinkedHashSet
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|clonedPruningTableScanSet
operator|=
operator|new
name|LinkedHashSet
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

