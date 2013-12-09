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
name|List
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
name|EdgeType
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
comment|// sequence number is used to name vertices (e.g.: Map 1, Reduce 14, ...)
specifier|private
name|int
name|sequenceNumber
init|=
literal|0
decl_stmt|;
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
name|work
operator|=
name|createMapWork
argument_list|(
name|context
argument_list|,
name|root
argument_list|,
name|tezWork
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|work
operator|=
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
comment|// need to add this branch to the key + value info
assert|assert
name|operator
operator|instanceof
name|ReduceSinkOperator
operator|&&
name|followingWork
operator|instanceof
name|ReduceWork
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
operator|(
name|ReduceWork
operator|)
name|followingWork
decl_stmt|;
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
name|rWork
operator|.
name|getTagToInput
argument_list|()
operator|.
name|put
argument_list|(
name|rs
operator|.
name|getConf
argument_list|()
operator|.
name|getTag
argument_list|()
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
comment|// add dependency between the two work items
name|tezWork
operator|.
name|connect
argument_list|(
name|work
argument_list|,
name|rWork
argument_list|,
name|EdgeType
operator|.
name|SIMPLE_EDGE
argument_list|)
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
comment|/*      * this happens in case of map join operations.      * The tree looks like this:      *      *       RS<--- we are here perhaps      *       |      *    MapJoin      *    /     \      *  RS       TS      *  /      * TS      *      * If we are at the RS pointed above, and we may have already visited the      * RS following the TS, we have already generated work for the TS-RS.      * We need to hook the current work to this generated work.      */
name|context
operator|.
name|operatorWorkMap
operator|.
name|put
argument_list|(
name|operator
argument_list|,
name|work
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|BaseWork
argument_list|>
name|linkWorkList
init|=
name|context
operator|.
name|linkOpWithWorkMap
operator|.
name|get
argument_list|(
name|operator
argument_list|)
decl_stmt|;
if|if
condition|(
name|linkWorkList
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
name|operator
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
name|operator
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
name|BaseWork
name|parentWork
range|:
name|linkWorkList
control|)
block|{
name|tezWork
operator|.
name|connect
argument_list|(
name|parentWork
argument_list|,
name|work
argument_list|,
name|EdgeType
operator|.
name|BROADCAST_EDGE
argument_list|)
expr_stmt|;
comment|// need to set up output name for reduce sink not that we know the name
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
return|return
literal|null
return|;
block|}
specifier|private
name|ReduceWork
name|createReduceWork
parameter_list|(
name|GenTezProcContext
name|context
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|root
parameter_list|,
name|TezWork
name|tezWork
parameter_list|)
block|{
assert|assert
operator|!
name|root
operator|.
name|getParentOperators
argument_list|()
operator|.
name|isEmpty
argument_list|()
assert|;
name|ReduceWork
name|reduceWork
init|=
operator|new
name|ReduceWork
argument_list|(
literal|"Reducer "
operator|+
operator|(
operator|++
name|sequenceNumber
operator|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding reduce work ("
operator|+
name|reduceWork
operator|.
name|getName
argument_list|()
operator|+
literal|") for "
operator|+
name|root
argument_list|)
expr_stmt|;
name|reduceWork
operator|.
name|setReducer
argument_list|(
name|root
argument_list|)
expr_stmt|;
name|reduceWork
operator|.
name|setNeedsTagging
argument_list|(
name|GenMapRedUtils
operator|.
name|needsTagging
argument_list|(
name|reduceWork
argument_list|)
argument_list|)
expr_stmt|;
comment|// All parents should be reduce sinks. We pick the one we just walked
comment|// to choose the number of reducers. In the join/union case they will
comment|// all be -1. In sort/order case where it matters there will be only
comment|// one parent.
assert|assert
name|context
operator|.
name|parentOfRoot
operator|instanceof
name|ReduceSinkOperator
assert|;
name|ReduceSinkOperator
name|reduceSink
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|context
operator|.
name|parentOfRoot
decl_stmt|;
name|reduceWork
operator|.
name|setNumReduceTasks
argument_list|(
name|reduceSink
operator|.
name|getConf
argument_list|()
operator|.
name|getNumReducers
argument_list|()
argument_list|)
expr_stmt|;
name|setupReduceSink
argument_list|(
name|context
argument_list|,
name|reduceWork
argument_list|,
name|reduceSink
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|add
argument_list|(
name|reduceWork
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|connect
argument_list|(
name|context
operator|.
name|preceedingWork
argument_list|,
name|reduceWork
argument_list|,
name|EdgeType
operator|.
name|SIMPLE_EDGE
argument_list|)
expr_stmt|;
return|return
name|reduceWork
return|;
block|}
specifier|private
name|void
name|setupReduceSink
parameter_list|(
name|GenTezProcContext
name|context
parameter_list|,
name|ReduceWork
name|reduceWork
parameter_list|,
name|ReduceSinkOperator
name|reduceSink
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Setting up reduce sink: "
operator|+
name|reduceSink
operator|+
literal|" with following reduce work: "
operator|+
name|reduceWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// need to fill in information about the key and value in the reducer
name|GenMapRedUtils
operator|.
name|setKeyAndValueDesc
argument_list|(
name|reduceWork
argument_list|,
name|reduceSink
argument_list|)
expr_stmt|;
comment|// remember which parent belongs to which tag
name|reduceWork
operator|.
name|getTagToInput
argument_list|()
operator|.
name|put
argument_list|(
name|reduceSink
operator|.
name|getConf
argument_list|()
operator|.
name|getTag
argument_list|()
argument_list|,
name|context
operator|.
name|preceedingWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// remember the output name of the reduce sink
name|reduceSink
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
block|}
specifier|private
name|MapWork
name|createMapWork
parameter_list|(
name|GenTezProcContext
name|context
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|root
parameter_list|,
name|TezWork
name|tezWork
parameter_list|)
throws|throws
name|SemanticException
block|{
assert|assert
name|root
operator|.
name|getParentOperators
argument_list|()
operator|.
name|isEmpty
argument_list|()
assert|;
name|MapWork
name|mapWork
init|=
operator|new
name|MapWork
argument_list|(
literal|"Map "
operator|+
operator|(
operator|++
name|sequenceNumber
operator|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding map work ("
operator|+
name|mapWork
operator|.
name|getName
argument_list|()
operator|+
literal|") for "
operator|+
name|root
argument_list|)
expr_stmt|;
comment|// map work starts with table scan operators
assert|assert
name|root
operator|instanceof
name|TableScanOperator
assert|;
name|String
name|alias
init|=
operator|(
operator|(
name|TableScanOperator
operator|)
name|root
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getAlias
argument_list|()
decl_stmt|;
name|GenMapRedUtils
operator|.
name|setMapWork
argument_list|(
name|mapWork
argument_list|,
name|context
operator|.
name|parseContext
argument_list|,
name|context
operator|.
name|inputs
argument_list|,
literal|null
argument_list|,
name|root
argument_list|,
name|alias
argument_list|,
name|context
operator|.
name|conf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|add
argument_list|(
name|mapWork
argument_list|)
expr_stmt|;
return|return
name|mapWork
return|;
block|}
block|}
end_class

end_unit

