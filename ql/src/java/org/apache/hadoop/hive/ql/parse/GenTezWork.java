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
package|;
end_package

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
name|Map
operator|.
name|Entry
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
name|CommonMergeJoinOperator
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
name|HashTableDummyOperator
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
name|RowSchema
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
name|MergeJoinWork
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
name|ReduceSinkDesc
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
name|TezEdgeProperty
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
name|TezEdgeProperty
operator|.
name|EdgeType
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
name|TezWork
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
name|TezWork
operator|.
name|VertexType
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
name|UnionWork
import|;
end_import

begin_comment
comment|/**  * GenTezWork separates the operator tree into tez tasks.  * It is called once per leaf operator (operator that forces  * a new execution unit.) and break the operators into work  * and tasks along the way.  */
end_comment

begin_class
specifier|public
class|class
name|GenTezWork
implements|implements
name|NodeProcessor
block|{
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|GenTezWork
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// instance of shared utils
specifier|private
name|GenTezUtils
name|utils
init|=
literal|null
decl_stmt|;
comment|/**    * Constructor takes utils as parameter to facilitate testing    */
specifier|public
name|GenTezWork
parameter_list|(
name|GenTezUtils
name|utils
parameter_list|)
block|{
name|this
operator|.
name|utils
operator|=
name|utils
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|process
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
name|NodeProcessorCtx
name|procContext
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|GenTezProcContext
name|context
init|=
operator|(
name|GenTezProcContext
operator|)
name|procContext
decl_stmt|;
assert|assert
name|context
operator|!=
literal|null
operator|&&
name|context
operator|.
name|currentTask
operator|!=
literal|null
operator|&&
name|context
operator|.
name|currentRootOperator
operator|!=
literal|null
assert|;
comment|// Operator is a file sink or reduce sink. Something that forces
comment|// a new vertex.
name|Operator
argument_list|<
name|?
argument_list|>
name|operator
init|=
operator|(
name|Operator
argument_list|<
name|?
argument_list|>
operator|)
name|nd
decl_stmt|;
comment|// root is the start of the operator pipeline we're currently
comment|// packing into a vertex, typically a table scan, union or join
name|Operator
argument_list|<
name|?
argument_list|>
name|root
init|=
name|context
operator|.
name|currentRootOperator
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Root operator: "
operator|+
name|root
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Leaf operator: "
operator|+
name|operator
argument_list|)
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|clonedReduceSinks
operator|.
name|contains
argument_list|(
name|operator
argument_list|)
condition|)
block|{
comment|// if we're visiting a terminal we've created ourselves,
comment|// just skip and keep going
return|return
literal|null
return|;
block|}
name|TezWork
name|tezWork
init|=
name|context
operator|.
name|currentTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
comment|// Right now the work graph is pretty simple. If there is no
comment|// Preceding work we have a root and will generate a map
comment|// vertex. If there is a preceding work we will generate
comment|// a reduce vertex
name|BaseWork
name|work
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|rootToWorkMap
operator|.
name|containsKey
argument_list|(
name|root
argument_list|)
condition|)
block|{
comment|// having seen the root operator before means there was a branch in the
comment|// operator graph. There's typically two reasons for that: a) mux/demux
comment|// b) multi insert. Mux/Demux will hit the same leaf again, multi insert
comment|// will result into a vertex with multiple FS or RS operators.
if|if
condition|(
name|context
operator|.
name|childToWorkMap
operator|.
name|containsKey
argument_list|(
name|operator
argument_list|)
condition|)
block|{
comment|// if we've seen both root and child, we can bail.
comment|// clear out the mapjoin set. we don't need it anymore.
name|context
operator|.
name|currentMapJoinOperators
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// clear out the union set. we don't need it anymore.
name|context
operator|.
name|currentUnionOperators
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
else|else
block|{
comment|// At this point we don't have to do anything special. Just
comment|// run through the regular paces w/o creating a new task.
name|work
operator|=
name|context
operator|.
name|rootToWorkMap
operator|.
name|get
argument_list|(
name|root
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// create a new vertex
if|if
condition|(
name|context
operator|.
name|preceedingWork
operator|==
literal|null
condition|)
block|{
name|work
operator|=
name|utils
operator|.
name|createMapWork
argument_list|(
name|context
argument_list|,
name|root
argument_list|,
name|tezWork
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|work
operator|=
name|utils
operator|.
name|createReduceWork
argument_list|(
name|context
argument_list|,
name|root
argument_list|,
name|tezWork
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|rootToWorkMap
operator|.
name|put
argument_list|(
name|root
argument_list|,
name|work
argument_list|)
expr_stmt|;
block|}
comment|// this is where we set the sort columns that we will be using for KeyValueInputMerge
if|if
condition|(
name|operator
operator|instanceof
name|DummyStoreOperator
condition|)
block|{
name|work
operator|.
name|addSortCols
argument_list|(
name|root
operator|.
name|getOpTraits
argument_list|()
operator|.
name|getSortCols
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|context
operator|.
name|childToWorkMap
operator|.
name|containsKey
argument_list|(
name|operator
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|workItems
init|=
operator|new
name|LinkedList
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
decl_stmt|;
name|workItems
operator|.
name|add
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|context
operator|.
name|childToWorkMap
operator|.
name|put
argument_list|(
name|operator
argument_list|,
name|workItems
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|context
operator|.
name|childToWorkMap
operator|.
name|get
argument_list|(
name|operator
argument_list|)
operator|.
name|add
argument_list|(
name|work
argument_list|)
expr_stmt|;
block|}
comment|// this transformation needs to be first because it changes the work item itself.
comment|// which can affect the working of all downstream transformations.
if|if
condition|(
name|context
operator|.
name|currentMergeJoinOperator
operator|!=
literal|null
condition|)
block|{
comment|// we are currently walking the big table side of the merge join. we need to create or hook up
comment|// merge join work.
name|MergeJoinWork
name|mergeJoinWork
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|opMergeJoinWorkMap
operator|.
name|containsKey
argument_list|(
name|context
operator|.
name|currentMergeJoinOperator
argument_list|)
condition|)
block|{
comment|// we have found a merge work corresponding to this closing operator. Hook up this work.
name|mergeJoinWork
operator|=
name|context
operator|.
name|opMergeJoinWorkMap
operator|.
name|get
argument_list|(
name|context
operator|.
name|currentMergeJoinOperator
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// we need to create the merge join work
name|mergeJoinWork
operator|=
operator|new
name|MergeJoinWork
argument_list|()
expr_stmt|;
name|mergeJoinWork
operator|.
name|setMergeJoinOperator
argument_list|(
name|context
operator|.
name|currentMergeJoinOperator
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|add
argument_list|(
name|mergeJoinWork
argument_list|)
expr_stmt|;
name|context
operator|.
name|opMergeJoinWorkMap
operator|.
name|put
argument_list|(
name|context
operator|.
name|currentMergeJoinOperator
argument_list|,
name|mergeJoinWork
argument_list|)
expr_stmt|;
block|}
comment|// connect the work correctly.
name|work
operator|.
name|addSortCols
argument_list|(
name|root
operator|.
name|getOpTraits
argument_list|()
operator|.
name|getSortCols
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|mergeJoinWork
operator|.
name|addMergedWork
argument_list|(
name|work
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOp
init|=
name|getParentFromStack
argument_list|(
name|context
operator|.
name|currentMergeJoinOperator
argument_list|,
name|stack
argument_list|)
decl_stmt|;
name|int
name|pos
init|=
name|context
operator|.
name|currentMergeJoinOperator
operator|.
name|getTagForOperator
argument_list|(
name|parentOp
argument_list|)
decl_stmt|;
name|work
operator|.
name|setTag
argument_list|(
name|pos
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|setVertexType
argument_list|(
name|work
argument_list|,
name|VertexType
operator|.
name|MULTI_INPUT_UNINITIALIZED_EDGES
argument_list|)
expr_stmt|;
for|for
control|(
name|BaseWork
name|parentWork
range|:
name|tezWork
operator|.
name|getParents
argument_list|(
name|work
argument_list|)
control|)
block|{
name|TezEdgeProperty
name|edgeProp
init|=
name|tezWork
operator|.
name|getEdgeProperty
argument_list|(
name|parentWork
argument_list|,
name|work
argument_list|)
decl_stmt|;
name|tezWork
operator|.
name|disconnect
argument_list|(
name|parentWork
argument_list|,
name|work
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|connect
argument_list|(
name|parentWork
argument_list|,
name|mergeJoinWork
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|BaseWork
name|childWork
range|:
name|tezWork
operator|.
name|getChildren
argument_list|(
name|work
argument_list|)
control|)
block|{
name|TezEdgeProperty
name|edgeProp
init|=
name|tezWork
operator|.
name|getEdgeProperty
argument_list|(
name|work
argument_list|,
name|childWork
argument_list|)
decl_stmt|;
name|tezWork
operator|.
name|disconnect
argument_list|(
name|work
argument_list|,
name|childWork
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|connect
argument_list|(
name|mergeJoinWork
argument_list|,
name|childWork
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
block|}
name|tezWork
operator|.
name|remove
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|context
operator|.
name|rootToWorkMap
operator|.
name|put
argument_list|(
name|root
argument_list|,
name|mergeJoinWork
argument_list|)
expr_stmt|;
name|context
operator|.
name|childToWorkMap
operator|.
name|get
argument_list|(
name|operator
argument_list|)
operator|.
name|remove
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|context
operator|.
name|childToWorkMap
operator|.
name|get
argument_list|(
name|operator
argument_list|)
operator|.
name|add
argument_list|(
name|mergeJoinWork
argument_list|)
expr_stmt|;
name|work
operator|=
name|mergeJoinWork
expr_stmt|;
name|context
operator|.
name|currentMergeJoinOperator
operator|=
literal|null
expr_stmt|;
block|}
comment|// remember which mapjoin operator links with which work
if|if
condition|(
operator|!
name|context
operator|.
name|currentMapJoinOperators
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|MapJoinOperator
name|mj
range|:
name|context
operator|.
name|currentMapJoinOperators
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Processing map join: "
operator|+
name|mj
argument_list|)
expr_stmt|;
comment|// remember the mapping in case we scan another branch of the
comment|// mapjoin later
if|if
condition|(
operator|!
name|context
operator|.
name|mapJoinWorkMap
operator|.
name|containsKey
argument_list|(
name|mj
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|workItems
init|=
operator|new
name|LinkedList
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
decl_stmt|;
name|workItems
operator|.
name|add
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|context
operator|.
name|mapJoinWorkMap
operator|.
name|put
argument_list|(
name|mj
argument_list|,
name|workItems
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|context
operator|.
name|mapJoinWorkMap
operator|.
name|get
argument_list|(
name|mj
argument_list|)
operator|.
name|add
argument_list|(
name|work
argument_list|)
expr_stmt|;
block|}
comment|/*          * this happens in case of map join operations.          * The tree looks like this:          *          *        RS<--- we are here perhaps          *        |          *     MapJoin          *     /     \          *   RS       TS          *  /          * TS          *          * If we are at the RS pointed above, and we may have already visited the          * RS following the TS, we have already generated work for the TS-RS.          * We need to hook the current work to this generated work.          */
if|if
condition|(
name|context
operator|.
name|linkOpWithWorkMap
operator|.
name|containsKey
argument_list|(
name|mj
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|TezEdgeProperty
argument_list|>
name|linkWorkMap
init|=
name|context
operator|.
name|linkOpWithWorkMap
operator|.
name|get
argument_list|(
name|mj
argument_list|)
decl_stmt|;
if|if
condition|(
name|linkWorkMap
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|context
operator|.
name|linkChildOpWithDummyOp
operator|.
name|containsKey
argument_list|(
name|mj
argument_list|)
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|dummy
range|:
name|context
operator|.
name|linkChildOpWithDummyOp
operator|.
name|get
argument_list|(
name|mj
argument_list|)
control|)
block|{
name|work
operator|.
name|addDummyOp
argument_list|(
operator|(
name|HashTableDummyOperator
operator|)
name|dummy
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|Entry
argument_list|<
name|BaseWork
argument_list|,
name|TezEdgeProperty
argument_list|>
name|parentWorkMap
range|:
name|linkWorkMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|BaseWork
name|parentWork
init|=
name|parentWorkMap
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"connecting "
operator|+
name|parentWork
operator|.
name|getName
argument_list|()
operator|+
literal|" with "
operator|+
name|work
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TezEdgeProperty
name|edgeProp
init|=
name|parentWorkMap
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|tezWork
operator|.
name|connect
argument_list|(
name|parentWork
argument_list|,
name|work
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
if|if
condition|(
name|edgeProp
operator|.
name|getEdgeType
argument_list|()
operator|==
name|EdgeType
operator|.
name|CUSTOM_EDGE
condition|)
block|{
name|tezWork
operator|.
name|setVertexType
argument_list|(
name|work
argument_list|,
name|VertexType
operator|.
name|INITIALIZED_EDGES
argument_list|)
expr_stmt|;
block|}
comment|// need to set up output name for reduce sink now that we know the name
comment|// of the downstream work
for|for
control|(
name|ReduceSinkOperator
name|r
range|:
name|context
operator|.
name|linkWorkWithReduceSinkMap
operator|.
name|get
argument_list|(
name|parentWork
argument_list|)
control|)
block|{
if|if
condition|(
name|r
operator|.
name|getConf
argument_list|()
operator|.
name|getOutputName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cloning reduce sink for multi-child broadcast edge"
argument_list|)
expr_stmt|;
comment|// we've already set this one up. Need to clone for the next work.
name|r
operator|=
operator|(
name|ReduceSinkOperator
operator|)
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
operator|(
name|ReduceSinkDesc
operator|)
name|r
operator|.
name|getConf
argument_list|()
operator|.
name|clone
argument_list|()
argument_list|,
operator|new
name|RowSchema
argument_list|(
name|r
operator|.
name|getSchema
argument_list|()
argument_list|)
argument_list|,
name|r
operator|.
name|getParentOperators
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|clonedReduceSinks
operator|.
name|add
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
name|r
operator|.
name|getConf
argument_list|()
operator|.
name|setOutputName
argument_list|(
name|work
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|connectedReduceSinks
operator|.
name|add
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|// clear out the set. we don't need it anymore.
name|context
operator|.
name|currentMapJoinOperators
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|// This is where we cut the tree as described above. We also remember that
comment|// we might have to connect parent work with this work later.
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|parent
range|:
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|(
name|root
operator|.
name|getParentOperators
argument_list|()
argument_list|)
control|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing "
operator|+
name|parent
operator|+
literal|" as parent from "
operator|+
name|root
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|leafOperatorToFollowingWork
operator|.
name|put
argument_list|(
name|parent
argument_list|,
name|work
argument_list|)
expr_stmt|;
name|root
operator|.
name|removeParent
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|context
operator|.
name|currentUnionOperators
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// if there are union all operators we need to add the work to the set
comment|// of union operators.
name|UnionWork
name|unionWork
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|unionWorkMap
operator|.
name|containsKey
argument_list|(
name|operator
argument_list|)
condition|)
block|{
comment|// we've seen this terminal before and have created a union work object.
comment|// just need to add this work to it. There will be no children of this one
comment|// since we've passed this operator before.
assert|assert
name|operator
operator|.
name|getChildOperators
argument_list|()
operator|.
name|isEmpty
argument_list|()
assert|;
name|unionWork
operator|=
operator|(
name|UnionWork
operator|)
name|context
operator|.
name|unionWorkMap
operator|.
name|get
argument_list|(
name|operator
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// first time through. we need to create a union work object and add this
comment|// work to it. Subsequent work should reference the union and not the actual
comment|// work.
name|unionWork
operator|=
name|utils
operator|.
name|createUnionWork
argument_list|(
name|context
argument_list|,
name|operator
argument_list|,
name|tezWork
argument_list|)
expr_stmt|;
block|}
comment|// finally hook everything up
name|LOG
operator|.
name|debug
argument_list|(
literal|"Connecting union work ("
operator|+
name|unionWork
operator|+
literal|") with work ("
operator|+
name|work
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|TezEdgeProperty
name|edgeProp
init|=
operator|new
name|TezEdgeProperty
argument_list|(
name|EdgeType
operator|.
name|CONTAINS
argument_list|)
decl_stmt|;
name|tezWork
operator|.
name|connect
argument_list|(
name|unionWork
argument_list|,
name|work
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
name|unionWork
operator|.
name|addUnionOperators
argument_list|(
name|context
operator|.
name|currentUnionOperators
argument_list|)
expr_stmt|;
name|context
operator|.
name|currentUnionOperators
operator|.
name|clear
argument_list|()
expr_stmt|;
name|context
operator|.
name|workWithUnionOperators
operator|.
name|add
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|work
operator|=
name|unionWork
expr_stmt|;
block|}
comment|// We're scanning a tree from roots to leaf (this is not technically
comment|// correct, demux and mux operators might form a diamond shape, but
comment|// we will only scan one path and ignore the others, because the
comment|// diamond shape is always contained in a single vertex). The scan
comment|// is depth first and because we remove parents when we pack a pipeline
comment|// into a vertex we will never visit any node twice. But because of that
comment|// we might have a situation where we need to connect 'work' that comes after
comment|// the 'work' we're currently looking at.
comment|//
comment|// Also note: the concept of leaf and root is reversed in hive for historical
comment|// reasons. Roots are data sources, leaves are data sinks. I know.
if|if
condition|(
name|context
operator|.
name|leafOperatorToFollowingWork
operator|.
name|containsKey
argument_list|(
name|operator
argument_list|)
condition|)
block|{
name|BaseWork
name|followingWork
init|=
name|context
operator|.
name|leafOperatorToFollowingWork
operator|.
name|get
argument_list|(
name|operator
argument_list|)
decl_stmt|;
name|long
name|bytesPerReducer
init|=
name|context
operator|.
name|conf
operator|.
name|getLongVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|BYTESPERREDUCER
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Second pass. Leaf operator: "
operator|+
name|operator
operator|+
literal|" has common downstream work:"
operator|+
name|followingWork
argument_list|)
expr_stmt|;
if|if
condition|(
name|operator
operator|instanceof
name|DummyStoreOperator
condition|)
block|{
comment|// this is the small table side.
assert|assert
operator|(
name|followingWork
operator|instanceof
name|MergeJoinWork
operator|)
assert|;
name|MergeJoinWork
name|mergeJoinWork
init|=
operator|(
name|MergeJoinWork
operator|)
name|followingWork
decl_stmt|;
name|CommonMergeJoinOperator
name|mergeJoinOp
init|=
name|mergeJoinWork
operator|.
name|getMergeJoinOperator
argument_list|()
decl_stmt|;
name|work
operator|.
name|setTag
argument_list|(
name|mergeJoinOp
operator|.
name|getTagForOperator
argument_list|(
name|operator
argument_list|)
argument_list|)
expr_stmt|;
name|mergeJoinWork
operator|.
name|addMergedWork
argument_list|(
literal|null
argument_list|,
name|work
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|setVertexType
argument_list|(
name|mergeJoinWork
argument_list|,
name|VertexType
operator|.
name|MULTI_INPUT_UNINITIALIZED_EDGES
argument_list|)
expr_stmt|;
for|for
control|(
name|BaseWork
name|parentWork
range|:
name|tezWork
operator|.
name|getParents
argument_list|(
name|work
argument_list|)
control|)
block|{
name|TezEdgeProperty
name|edgeProp
init|=
name|tezWork
operator|.
name|getEdgeProperty
argument_list|(
name|parentWork
argument_list|,
name|work
argument_list|)
decl_stmt|;
name|tezWork
operator|.
name|disconnect
argument_list|(
name|parentWork
argument_list|,
name|work
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|connect
argument_list|(
name|parentWork
argument_list|,
name|mergeJoinWork
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
block|}
name|work
operator|=
name|mergeJoinWork
expr_stmt|;
block|}
else|else
block|{
comment|// need to add this branch to the key + value info
assert|assert
name|operator
operator|instanceof
name|ReduceSinkOperator
operator|&&
operator|(
operator|(
name|followingWork
operator|instanceof
name|ReduceWork
operator|)
operator|||
operator|(
name|followingWork
operator|instanceof
name|MergeJoinWork
operator|)
operator|||
name|followingWork
operator|instanceof
name|UnionWork
operator|)
assert|;
name|ReduceSinkOperator
name|rs
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|operator
decl_stmt|;
name|ReduceWork
name|rWork
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|followingWork
operator|instanceof
name|MergeJoinWork
condition|)
block|{
name|MergeJoinWork
name|mergeJoinWork
init|=
operator|(
name|MergeJoinWork
operator|)
name|followingWork
decl_stmt|;
name|rWork
operator|=
operator|(
name|ReduceWork
operator|)
name|mergeJoinWork
operator|.
name|getMainWork
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|followingWork
operator|instanceof
name|UnionWork
condition|)
block|{
comment|// this can only be possible if there is merge work followed by the union
name|UnionWork
name|unionWork
init|=
operator|(
name|UnionWork
operator|)
name|followingWork
decl_stmt|;
name|int
name|index
init|=
name|getFollowingWorkIndex
argument_list|(
name|tezWork
argument_list|,
name|unionWork
argument_list|,
name|rs
argument_list|)
decl_stmt|;
name|BaseWork
name|baseWork
init|=
name|tezWork
operator|.
name|getChildren
argument_list|(
name|unionWork
argument_list|)
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|baseWork
operator|instanceof
name|MergeJoinWork
condition|)
block|{
name|MergeJoinWork
name|mergeJoinWork
init|=
operator|(
name|MergeJoinWork
operator|)
name|baseWork
decl_stmt|;
comment|// disconnect the connection to union work and connect to merge work
name|followingWork
operator|=
name|mergeJoinWork
expr_stmt|;
name|rWork
operator|=
operator|(
name|ReduceWork
operator|)
name|mergeJoinWork
operator|.
name|getMainWork
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|rWork
operator|=
operator|(
name|ReduceWork
operator|)
name|baseWork
expr_stmt|;
block|}
block|}
else|else
block|{
name|rWork
operator|=
operator|(
name|ReduceWork
operator|)
name|followingWork
expr_stmt|;
block|}
name|GenMapRedUtils
operator|.
name|setKeyAndValueDesc
argument_list|(
name|rWork
argument_list|,
name|rs
argument_list|)
expr_stmt|;
comment|// remember which parent belongs to which tag
name|int
name|tag
init|=
name|rs
operator|.
name|getConf
argument_list|()
operator|.
name|getTag
argument_list|()
decl_stmt|;
name|rWork
operator|.
name|getTagToInput
argument_list|()
operator|.
name|put
argument_list|(
name|tag
operator|==
operator|-
literal|1
condition|?
literal|0
else|:
name|tag
argument_list|,
name|work
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// remember the output name of the reduce sink
name|rs
operator|.
name|getConf
argument_list|()
operator|.
name|setOutputName
argument_list|(
name|rWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|context
operator|.
name|connectedReduceSinks
operator|.
name|contains
argument_list|(
name|rs
argument_list|)
condition|)
block|{
comment|// add dependency between the two work items
name|TezEdgeProperty
name|edgeProp
decl_stmt|;
if|if
condition|(
name|rWork
operator|.
name|isAutoReduceParallelism
argument_list|()
condition|)
block|{
name|edgeProp
operator|=
operator|new
name|TezEdgeProperty
argument_list|(
name|context
operator|.
name|conf
argument_list|,
name|EdgeType
operator|.
name|SIMPLE_EDGE
argument_list|,
literal|true
argument_list|,
name|rWork
operator|.
name|getMinReduceTasks
argument_list|()
argument_list|,
name|rWork
operator|.
name|getMaxReduceTasks
argument_list|()
argument_list|,
name|bytesPerReducer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|edgeProp
operator|=
operator|new
name|TezEdgeProperty
argument_list|(
name|EdgeType
operator|.
name|SIMPLE_EDGE
argument_list|)
expr_stmt|;
block|}
name|tezWork
operator|.
name|connect
argument_list|(
name|work
argument_list|,
name|followingWork
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
name|context
operator|.
name|connectedReduceSinks
operator|.
name|add
argument_list|(
name|rs
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"First pass. Leaf operator: "
operator|+
name|operator
argument_list|)
expr_stmt|;
block|}
comment|// No children means we're at the bottom. If there are more operators to scan
comment|// the next item will be a new root.
if|if
condition|(
operator|!
name|operator
operator|.
name|getChildOperators
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
assert|assert
name|operator
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|context
operator|.
name|parentOfRoot
operator|=
name|operator
expr_stmt|;
name|context
operator|.
name|currentRootOperator
operator|=
name|operator
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|context
operator|.
name|preceedingWork
operator|=
name|work
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|int
name|getFollowingWorkIndex
parameter_list|(
name|TezWork
name|tezWork
parameter_list|,
name|UnionWork
name|unionWork
parameter_list|,
name|ReduceSinkOperator
name|rs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|BaseWork
name|baseWork
range|:
name|tezWork
operator|.
name|getChildren
argument_list|(
name|unionWork
argument_list|)
control|)
block|{
name|TezEdgeProperty
name|edgeProperty
init|=
name|tezWork
operator|.
name|getEdgeProperty
argument_list|(
name|unionWork
argument_list|,
name|baseWork
argument_list|)
decl_stmt|;
if|if
condition|(
name|edgeProperty
operator|.
name|getEdgeType
argument_list|()
operator|!=
name|TezEdgeProperty
operator|.
name|EdgeType
operator|.
name|CONTAINS
condition|)
block|{
return|return
name|index
return|;
block|}
name|index
operator|++
expr_stmt|;
block|}
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Following work not found for the reduce sink: "
operator|+
name|rs
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|getParentFromStack
parameter_list|(
name|Node
name|currentMergeJoinOperator
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|)
block|{
name|int
name|pos
init|=
name|stack
operator|.
name|indexOf
argument_list|(
name|currentMergeJoinOperator
argument_list|)
decl_stmt|;
return|return
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|)
name|stack
operator|.
name|get
argument_list|(
name|pos
operator|-
literal|1
argument_list|)
return|;
block|}
block|}
end_class

end_unit

