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
name|optimizer
operator|.
name|spark
operator|.
name|SparkSortMergeJoinFactory
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
name|SparkEdgeProperty
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
name|SparkWork
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
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * GenSparkWork separates the operator tree into spark tasks.  * It is called once per leaf operator (operator that forces a new execution unit.)  * and break the operators into work and tasks along the way.  *  * Cloned from GenTezWork.  */
end_comment

begin_class
specifier|public
class|class
name|GenSparkWork
implements|implements
name|NodeProcessor
block|{
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|GenSparkWork
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// instance of shared utils
specifier|private
name|GenSparkUtils
name|utils
init|=
literal|null
decl_stmt|;
comment|/**    * Constructor takes utils as parameter to facilitate testing    */
specifier|public
name|GenSparkWork
parameter_list|(
name|GenSparkUtils
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
name|GenSparkProcContext
name|context
init|=
operator|(
name|GenSparkProcContext
operator|)
name|procContext
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|context
operator|!=
literal|null
argument_list|,
literal|"AssertionError: expected context to be not null"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|context
operator|.
name|currentTask
operator|!=
literal|null
argument_list|,
literal|"AssertionError: expected context.currentTask to be not null"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|context
operator|.
name|currentRootOperator
operator|!=
literal|null
argument_list|,
literal|"AssertionError: expected context.currentRootOperator to be not null"
argument_list|)
expr_stmt|;
comment|// Operator is a file sink or reduce sink. Something that forces a new vertex.
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|operator
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
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
name|SparkWork
name|sparkWork
init|=
name|context
operator|.
name|currentTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|SMBMapJoinOperator
name|smbOp
init|=
name|GenSparkUtils
operator|.
name|getChildOperator
argument_list|(
name|root
argument_list|,
name|SMBMapJoinOperator
operator|.
name|class
argument_list|)
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
comment|// At this point we don't have to do anything special in this case. Just
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
if|if
condition|(
name|smbOp
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
name|sparkWork
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|//save work to be initialized later with SMB information.
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
name|sparkWork
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|context
operator|.
name|smbMapJoinCtxMap
operator|.
name|get
argument_list|(
name|smbOp
argument_list|)
operator|.
name|mapWork
operator|=
operator|(
name|MapWork
operator|)
name|work
expr_stmt|;
block|}
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
name|sparkWork
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
comment|// remember the mapping in case we scan another branch of the mapjoin later
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
name|SparkEdgeProperty
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
name|SparkEdgeProperty
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
name|SparkEdgeProperty
name|edgeProp
init|=
name|parentWorkMap
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|sparkWork
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
name|r
operator|.
name|getCompilationOpContext
argument_list|()
argument_list|,
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
name|r
operator|.
name|getParentOperators
argument_list|()
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
comment|// Here we are disconnecting root with its parents. However, we need to save
comment|// a few information, since in future we may reach the parent operators via a
comment|// different path, and we may need to connect parent works with the work associated
comment|// with this root operator.
if|if
condition|(
name|root
operator|.
name|getNumParent
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|work
operator|instanceof
name|ReduceWork
argument_list|,
literal|"AssertionError: expected work to be a ReduceWork, but was "
operator|+
name|work
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|ReduceWork
name|reduceWork
init|=
operator|(
name|ReduceWork
operator|)
name|work
decl_stmt|;
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
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|parent
operator|instanceof
name|ReduceSinkOperator
argument_list|,
literal|"AssertionError: expected operator to be a ReduceSinkOperator, but was "
operator|+
name|parent
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|ReduceSinkOperator
name|rsOp
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|parent
decl_stmt|;
name|SparkEdgeProperty
name|edgeProp
init|=
name|GenSparkUtils
operator|.
name|getEdgeProperty
argument_list|(
name|rsOp
argument_list|,
name|reduceWork
argument_list|)
decl_stmt|;
name|rsOp
operator|.
name|getConf
argument_list|()
operator|.
name|setOutputName
argument_list|(
name|reduceWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|GenMapRedUtils
operator|.
name|setKeyAndValueDesc
argument_list|(
name|reduceWork
argument_list|,
name|rsOp
argument_list|)
expr_stmt|;
name|context
operator|.
name|leafOpToFollowingWorkInfo
operator|.
name|put
argument_list|(
name|rsOp
argument_list|,
name|ObjectPair
operator|.
name|create
argument_list|(
name|edgeProp
argument_list|,
name|reduceWork
argument_list|)
argument_list|)
expr_stmt|;
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
name|root
operator|.
name|removeParent
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
block|}
comment|// If `currentUnionOperators` is not empty, it means we are creating BaseWork whose operator tree
comment|// contains union operators. In this case, we need to save these BaseWorks, and remove
comment|// the union operators from the operator tree later.
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
name|leafOpToFollowingWorkInfo
operator|.
name|containsKey
argument_list|(
name|operator
argument_list|)
condition|)
block|{
name|ObjectPair
argument_list|<
name|SparkEdgeProperty
argument_list|,
name|ReduceWork
argument_list|>
name|childWorkInfo
init|=
name|context
operator|.
name|leafOpToFollowingWorkInfo
operator|.
name|get
argument_list|(
name|operator
argument_list|)
decl_stmt|;
name|SparkEdgeProperty
name|edgeProp
init|=
name|childWorkInfo
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|ReduceWork
name|childWork
init|=
name|childWorkInfo
operator|.
name|getSecond
argument_list|()
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
name|childWork
argument_list|)
expr_stmt|;
comment|// We may have already connected `work` with `childWork`, in case, for example, lateral view:
comment|//    TS
comment|//     |
comment|//    ...
comment|//     |
comment|//    LVF
comment|//     | \
comment|//    SEL SEL
comment|//     |    |
comment|//    LVJ-UDTF
comment|//     |
comment|//    SEL
comment|//     |
comment|//    RS
comment|// Here, RS can be reached from TS via two different paths. If there is any child work after RS,
comment|// we don't want to connect them with the work associated with TS more than once.
if|if
condition|(
name|sparkWork
operator|.
name|getEdgeProperty
argument_list|(
name|work
argument_list|,
name|childWork
argument_list|)
operator|==
literal|null
condition|)
block|{
name|sparkWork
operator|.
name|connect
argument_list|(
name|work
argument_list|,
name|childWork
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"work "
operator|+
name|work
operator|.
name|getName
argument_list|()
operator|+
literal|" is already connected to "
operator|+
name|childWork
operator|.
name|getName
argument_list|()
operator|+
literal|" before"
argument_list|)
expr_stmt|;
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
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|operator
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|,
literal|"AssertionError: expected operator.getChildOperators().size() to be 1, but was "
operator|+
name|operator
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
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
block|}
end_class

end_unit

