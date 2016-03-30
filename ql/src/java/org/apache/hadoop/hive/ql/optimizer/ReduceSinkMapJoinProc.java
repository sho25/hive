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
import|import static
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
operator|.
name|ReducerTraits
operator|.
name|FIXED
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
name|EnumSet
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
name|OperatorUtils
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
name|parse
operator|.
name|GenTezProcContext
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
name|ColStatistics
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
name|ExprNodeDesc
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
name|HashTableDummyDesc
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
name|MapJoinDesc
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
name|OpTraits
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
name|Statistics
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
name|TableDesc
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
name|stats
operator|.
name|StatsUtils
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
name|Sets
import|;
end_import

begin_class
specifier|public
class|class
name|ReduceSinkMapJoinProc
implements|implements
name|NodeProcessor
block|{
specifier|private
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ReduceSinkMapJoinProc
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/* (non-Javadoc)    * This processor addresses the RS-MJ case that occurs in tez on the small/hash    * table side of things. The work that RS will be a part of must be connected    * to the MJ work via be a broadcast edge.    * We should not walk down the tree when we encounter this pattern because:    * the type of work (map work or reduce work) needs to be determined    * on the basis of the big table side because it may be a mapwork (no need for shuffle)    * or reduce work.    */
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
name|MapJoinOperator
name|mapJoinOp
init|=
operator|(
name|MapJoinOperator
operator|)
name|nd
decl_stmt|;
comment|// remember the original parent list before we start modifying it.
if|if
condition|(
operator|!
name|context
operator|.
name|mapJoinParentMap
operator|.
name|containsKey
argument_list|(
name|mapJoinOp
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|parents
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|(
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|mapJoinParentMap
operator|.
name|put
argument_list|(
name|mapJoinOp
argument_list|,
name|parents
argument_list|)
expr_stmt|;
block|}
name|boolean
name|isBigTable
init|=
name|stack
operator|.
name|size
argument_list|()
operator|<
literal|2
operator|||
operator|!
operator|(
name|stack
operator|.
name|get
argument_list|(
name|stack
operator|.
name|size
argument_list|()
operator|-
literal|2
argument_list|)
operator|instanceof
name|ReduceSinkOperator
operator|)
decl_stmt|;
name|ReduceSinkOperator
name|parentRS
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|isBigTable
condition|)
block|{
name|parentRS
operator|=
operator|(
name|ReduceSinkOperator
operator|)
name|stack
operator|.
name|get
argument_list|(
name|stack
operator|.
name|size
argument_list|()
operator|-
literal|2
argument_list|)
expr_stmt|;
comment|// For dynamic partitioned hash join, the big table will also be coming from a ReduceSinkOperator
comment|// Check for this condition.
comment|// TODO: use indexOf(), or parentRS.getTag()?
name|isBigTable
operator|=
operator|(
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|indexOf
argument_list|(
name|parentRS
argument_list|)
operator|==
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getPosBigTable
argument_list|()
operator|)
expr_stmt|;
block|}
if|if
condition|(
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|isDynamicPartitionHashJoin
argument_list|()
operator|&&
operator|!
name|context
operator|.
name|mapJoinToUnprocessedSmallTableReduceSinks
operator|.
name|containsKey
argument_list|(
name|mapJoinOp
argument_list|)
condition|)
block|{
comment|// Initialize set of unprocessed small tables
name|Set
argument_list|<
name|ReduceSinkOperator
argument_list|>
name|rsSet
init|=
name|Sets
operator|.
name|newIdentityHashSet
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
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
condition|;
operator|++
name|pos
control|)
block|{
if|if
condition|(
name|pos
operator|==
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getPosBigTable
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|rsSet
operator|.
name|add
argument_list|(
operator|(
name|ReduceSinkOperator
operator|)
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|mapJoinToUnprocessedSmallTableReduceSinks
operator|.
name|put
argument_list|(
name|mapJoinOp
argument_list|,
name|rsSet
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isBigTable
condition|)
block|{
name|context
operator|.
name|currentMapJoinOperators
operator|.
name|add
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|context
operator|.
name|preceedingWork
operator|=
literal|null
expr_stmt|;
name|context
operator|.
name|currentRootOperator
operator|=
literal|null
expr_stmt|;
return|return
name|processReduceSinkToHashJoin
argument_list|(
name|parentRS
argument_list|,
name|mapJoinOp
argument_list|,
name|context
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|BaseWork
name|getMapJoinParentWork
parameter_list|(
name|GenTezProcContext
name|context
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|parentRS
parameter_list|)
block|{
name|BaseWork
name|parentWork
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|unionWorkMap
operator|.
name|containsKey
argument_list|(
name|parentRS
argument_list|)
condition|)
block|{
name|parentWork
operator|=
name|context
operator|.
name|unionWorkMap
operator|.
name|get
argument_list|(
name|parentRS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|context
operator|.
name|childToWorkMap
operator|.
name|get
argument_list|(
name|parentRS
argument_list|)
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|parentWork
operator|=
name|context
operator|.
name|childToWorkMap
operator|.
name|get
argument_list|(
name|parentRS
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
name|parentWork
return|;
block|}
specifier|public
specifier|static
name|Object
name|processReduceSinkToHashJoin
parameter_list|(
name|ReduceSinkOperator
name|parentRS
parameter_list|,
name|MapJoinOperator
name|mapJoinOp
parameter_list|,
name|GenTezProcContext
name|context
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// remove the tag for in-memory side of mapjoin
name|parentRS
operator|.
name|getConf
argument_list|()
operator|.
name|setSkipTag
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|parentRS
operator|.
name|setSkipTag
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Mark this small table as being processed
if|if
condition|(
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|isDynamicPartitionHashJoin
argument_list|()
condition|)
block|{
name|context
operator|.
name|mapJoinToUnprocessedSmallTableReduceSinks
operator|.
name|get
argument_list|(
name|mapJoinOp
argument_list|)
operator|.
name|remove
argument_list|(
name|parentRS
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|BaseWork
argument_list|>
name|mapJoinWork
init|=
literal|null
decl_stmt|;
comment|/*      *  if there was a pre-existing work generated for the big-table mapjoin side,      *  we need to hook the work generated for the RS (associated with the RS-MJ pattern)      *  with the pre-existing work.      *      *  Otherwise, we need to associate that the mapjoin op      *  to be linked to the RS work (associated with the RS-MJ pattern).      *      */
name|mapJoinWork
operator|=
name|context
operator|.
name|mapJoinWorkMap
operator|.
name|get
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
name|BaseWork
name|parentWork
init|=
name|getMapJoinParentWork
argument_list|(
name|context
argument_list|,
name|parentRS
argument_list|)
decl_stmt|;
comment|// set the link between mapjoin and parent vertex
name|int
name|pos
init|=
name|context
operator|.
name|mapJoinParentMap
operator|.
name|get
argument_list|(
name|mapJoinOp
argument_list|)
operator|.
name|indexOf
argument_list|(
name|parentRS
argument_list|)
decl_stmt|;
if|if
condition|(
name|pos
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Cannot find position of parent in mapjoin"
argument_list|)
throw|;
block|}
name|MapJoinDesc
name|joinConf
init|=
name|mapJoinOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|long
name|keyCount
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|,
name|rowCount
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|,
name|bucketCount
init|=
literal|1
decl_stmt|;
name|long
name|tableSize
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|Statistics
name|stats
init|=
name|parentRS
operator|.
name|getStatistics
argument_list|()
decl_stmt|;
if|if
condition|(
name|stats
operator|!=
literal|null
condition|)
block|{
name|keyCount
operator|=
name|rowCount
operator|=
name|stats
operator|.
name|getNumRows
argument_list|()
expr_stmt|;
if|if
condition|(
name|keyCount
operator|<=
literal|0
condition|)
block|{
name|keyCount
operator|=
name|rowCount
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
name|tableSize
operator|=
name|stats
operator|.
name|getDataSize
argument_list|()
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|keyCols
init|=
name|parentRS
operator|.
name|getConf
argument_list|()
operator|.
name|getOutputKeyColumnNames
argument_list|()
decl_stmt|;
if|if
condition|(
name|keyCols
operator|!=
literal|null
operator|&&
operator|!
name|keyCols
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// See if we can arrive at a smaller number using distinct stats from key columns.
name|long
name|maxKeyCount
init|=
literal|1
decl_stmt|;
name|String
name|prefix
init|=
name|Utilities
operator|.
name|ReduceField
operator|.
name|KEY
operator|.
name|toString
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|keyCol
range|:
name|keyCols
control|)
block|{
name|ExprNodeDesc
name|realCol
init|=
name|parentRS
operator|.
name|getColumnExprMap
argument_list|()
operator|.
name|get
argument_list|(
name|prefix
operator|+
literal|"."
operator|+
name|keyCol
argument_list|)
decl_stmt|;
name|ColStatistics
name|cs
init|=
name|StatsUtils
operator|.
name|getColStatisticsFromExpression
argument_list|(
name|context
operator|.
name|conf
argument_list|,
name|stats
argument_list|,
name|realCol
argument_list|)
decl_stmt|;
if|if
condition|(
name|cs
operator|==
literal|null
operator|||
name|cs
operator|.
name|getCountDistint
argument_list|()
operator|<=
literal|0
condition|)
block|{
name|maxKeyCount
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
break|break;
block|}
name|maxKeyCount
operator|*=
name|cs
operator|.
name|getCountDistint
argument_list|()
expr_stmt|;
if|if
condition|(
name|maxKeyCount
operator|>=
name|keyCount
condition|)
block|{
break|break;
block|}
block|}
name|keyCount
operator|=
name|Math
operator|.
name|min
argument_list|(
name|maxKeyCount
argument_list|,
name|keyCount
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|joinConf
operator|.
name|isBucketMapJoin
argument_list|()
condition|)
block|{
name|OpTraits
name|opTraits
init|=
name|mapJoinOp
operator|.
name|getOpTraits
argument_list|()
decl_stmt|;
name|bucketCount
operator|=
operator|(
name|opTraits
operator|==
literal|null
operator|)
condition|?
operator|-
literal|1
else|:
name|opTraits
operator|.
name|getNumBuckets
argument_list|()
expr_stmt|;
if|if
condition|(
name|bucketCount
operator|>
literal|0
condition|)
block|{
comment|// We cannot obtain a better estimate without CustomPartitionVertex providing it
comment|// to us somehow; in which case using statistics would be completely unnecessary.
name|keyCount
operator|/=
name|bucketCount
expr_stmt|;
name|tableSize
operator|/=
name|bucketCount
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|joinConf
operator|.
name|isDynamicPartitionHashJoin
argument_list|()
condition|)
block|{
comment|// For dynamic partitioned hash join, assuming table is split evenly among the reduce tasks.
name|bucketCount
operator|=
name|parentRS
operator|.
name|getConf
argument_list|()
operator|.
name|getNumReducers
argument_list|()
expr_stmt|;
name|keyCount
operator|/=
name|bucketCount
expr_stmt|;
name|tableSize
operator|/=
name|bucketCount
expr_stmt|;
block|}
block|}
if|if
condition|(
name|keyCount
operator|==
literal|0
condition|)
block|{
name|keyCount
operator|=
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|tableSize
operator|==
literal|0
condition|)
block|{
name|tableSize
operator|=
literal|1
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Mapjoin "
operator|+
name|mapJoinOp
operator|+
literal|"(bucket map join = )"
operator|+
name|joinConf
operator|.
name|isBucketMapJoin
argument_list|()
operator|+
literal|", pos: "
operator|+
name|pos
operator|+
literal|" --> "
operator|+
name|parentWork
operator|.
name|getName
argument_list|()
operator|+
literal|" ("
operator|+
name|keyCount
operator|+
literal|" keys estimated from "
operator|+
name|rowCount
operator|+
literal|" rows, "
operator|+
name|bucketCount
operator|+
literal|" buckets)"
argument_list|)
expr_stmt|;
name|joinConf
operator|.
name|getParentToInput
argument_list|()
operator|.
name|put
argument_list|(
name|pos
argument_list|,
name|parentWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|keyCount
operator|!=
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
name|joinConf
operator|.
name|getParentKeyCounts
argument_list|()
operator|.
name|put
argument_list|(
name|pos
argument_list|,
name|keyCount
argument_list|)
expr_stmt|;
block|}
name|joinConf
operator|.
name|getParentDataSizes
argument_list|()
operator|.
name|put
argument_list|(
name|pos
argument_list|,
name|tableSize
argument_list|)
expr_stmt|;
name|int
name|numBuckets
init|=
operator|-
literal|1
decl_stmt|;
name|EdgeType
name|edgeType
init|=
name|EdgeType
operator|.
name|BROADCAST_EDGE
decl_stmt|;
if|if
condition|(
name|joinConf
operator|.
name|isBucketMapJoin
argument_list|()
condition|)
block|{
name|numBuckets
operator|=
operator|(
name|Integer
operator|)
name|joinConf
operator|.
name|getBigTableBucketNumMapping
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|toArray
argument_list|()
index|[
literal|0
index|]
expr_stmt|;
comment|/*        * Here, we can be in one of 4 states.        *        * 1. If map join work is null implies that we have not yet traversed the big table side. We        * just need to see if we can find a reduce sink operator in the big table side. This would        * imply a reduce side operation.        *        * 2. If we don't find a reducesink in 1 it has to be the case that it is a map side operation.        *        * 3. If we have already created a work item for the big table side, we need to see if we can        * find a table scan operator in the big table side. This would imply a map side operation.        *        * 4. If we don't find a table scan operator, it has to be a reduce side operation.        */
if|if
condition|(
name|mapJoinWork
operator|==
literal|null
condition|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|rootOp
init|=
name|OperatorUtils
operator|.
name|findSingleOperatorUpstreamJoinAccounted
argument_list|(
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|joinConf
operator|.
name|getPosBigTable
argument_list|()
argument_list|)
argument_list|,
name|ReduceSinkOperator
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|rootOp
operator|==
literal|null
condition|)
block|{
comment|// likely we found a table scan operator
name|edgeType
operator|=
name|EdgeType
operator|.
name|CUSTOM_EDGE
expr_stmt|;
block|}
else|else
block|{
comment|// we have found a reduce sink
name|edgeType
operator|=
name|EdgeType
operator|.
name|CUSTOM_SIMPLE_EDGE
expr_stmt|;
block|}
block|}
else|else
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|rootOp
init|=
name|OperatorUtils
operator|.
name|findSingleOperatorUpstreamJoinAccounted
argument_list|(
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|joinConf
operator|.
name|getPosBigTable
argument_list|()
argument_list|)
argument_list|,
name|TableScanOperator
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|rootOp
operator|!=
literal|null
condition|)
block|{
comment|// likely we found a table scan operator
name|edgeType
operator|=
name|EdgeType
operator|.
name|CUSTOM_EDGE
expr_stmt|;
block|}
else|else
block|{
comment|// we have found a reduce sink
name|edgeType
operator|=
name|EdgeType
operator|.
name|CUSTOM_SIMPLE_EDGE
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|isDynamicPartitionHashJoin
argument_list|()
condition|)
block|{
name|edgeType
operator|=
name|EdgeType
operator|.
name|CUSTOM_SIMPLE_EDGE
expr_stmt|;
block|}
if|if
condition|(
name|edgeType
operator|==
name|EdgeType
operator|.
name|CUSTOM_EDGE
condition|)
block|{
comment|// disable auto parallelism for bucket map joins
name|parentRS
operator|.
name|getConf
argument_list|()
operator|.
name|setReducerTraits
argument_list|(
name|EnumSet
operator|.
name|of
argument_list|(
name|FIXED
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|TezEdgeProperty
name|edgeProp
init|=
operator|new
name|TezEdgeProperty
argument_list|(
literal|null
argument_list|,
name|edgeType
argument_list|,
name|numBuckets
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapJoinWork
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|BaseWork
name|myWork
range|:
name|mapJoinWork
control|)
block|{
comment|// link the work with the work associated with the reduce sink that triggered this rule
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
name|myWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|connect
argument_list|(
name|parentWork
argument_list|,
name|myWork
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
if|if
condition|(
name|edgeType
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
name|myWork
argument_list|,
name|VertexType
operator|.
name|INITIALIZED_EDGES
argument_list|)
expr_stmt|;
block|}
name|ReduceSinkOperator
name|r
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|connectedReduceSinks
operator|.
name|contains
argument_list|(
name|parentRS
argument_list|)
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
name|parentRS
operator|.
name|getCompilationOpContext
argument_list|()
argument_list|,
operator|(
name|ReduceSinkDesc
operator|)
name|parentRS
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
name|parentRS
operator|.
name|getSchema
argument_list|()
argument_list|)
argument_list|,
name|parentRS
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
else|else
block|{
name|r
operator|=
name|parentRS
expr_stmt|;
block|}
comment|// remember the output name of the reduce sink
name|r
operator|.
name|getConf
argument_list|()
operator|.
name|setOutputName
argument_list|(
name|myWork
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
comment|// remember in case we need to connect additional work later
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|TezEdgeProperty
argument_list|>
name|linkWorkMap
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|linkOpWithWorkMap
operator|.
name|containsKey
argument_list|(
name|mapJoinOp
argument_list|)
condition|)
block|{
name|linkWorkMap
operator|=
name|context
operator|.
name|linkOpWithWorkMap
operator|.
name|get
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|linkWorkMap
operator|=
operator|new
name|HashMap
argument_list|<
name|BaseWork
argument_list|,
name|TezEdgeProperty
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|linkWorkMap
operator|.
name|put
argument_list|(
name|parentWork
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
name|context
operator|.
name|linkOpWithWorkMap
operator|.
name|put
argument_list|(
name|mapJoinOp
argument_list|,
name|linkWorkMap
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ReduceSinkOperator
argument_list|>
name|reduceSinks
init|=
name|context
operator|.
name|linkWorkWithReduceSinkMap
operator|.
name|get
argument_list|(
name|parentWork
argument_list|)
decl_stmt|;
if|if
condition|(
name|reduceSinks
operator|==
literal|null
condition|)
block|{
name|reduceSinks
operator|=
operator|new
name|ArrayList
argument_list|<
name|ReduceSinkOperator
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|reduceSinks
operator|.
name|add
argument_list|(
name|parentRS
argument_list|)
expr_stmt|;
name|context
operator|.
name|linkWorkWithReduceSinkMap
operator|.
name|put
argument_list|(
name|parentWork
argument_list|,
name|reduceSinks
argument_list|)
expr_stmt|;
comment|// create the dummy operators
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|dummyOperators
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// create an new operator: HashTableDummyOperator, which share the table desc
name|HashTableDummyDesc
name|desc
init|=
operator|new
name|HashTableDummyDesc
argument_list|()
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|HashTableDummyOperator
name|dummyOp
init|=
operator|(
name|HashTableDummyOperator
operator|)
name|OperatorFactory
operator|.
name|get
argument_list|(
name|parentRS
operator|.
name|getCompilationOpContext
argument_list|()
argument_list|,
name|desc
argument_list|)
decl_stmt|;
name|TableDesc
name|tbl
decl_stmt|;
comment|// need to create the correct table descriptor for key/value
name|RowSchema
name|rowSchema
init|=
name|parentRS
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSchema
argument_list|()
decl_stmt|;
name|tbl
operator|=
name|PlanUtils
operator|.
name|getReduceValueTableDesc
argument_list|(
name|PlanUtils
operator|.
name|getFieldSchemasFromRowSchema
argument_list|(
name|rowSchema
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|dummyOp
operator|.
name|getConf
argument_list|()
operator|.
name|setTbl
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|keyExprMap
init|=
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getKeys
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyCols
init|=
name|keyExprMap
operator|.
name|get
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|StringBuilder
name|keyOrder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|StringBuilder
name|keyNullOrder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|k
range|:
name|keyCols
control|)
block|{
name|keyOrder
operator|.
name|append
argument_list|(
literal|"+"
argument_list|)
expr_stmt|;
name|keyNullOrder
operator|.
name|append
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
block|}
name|TableDesc
name|keyTableDesc
init|=
name|PlanUtils
operator|.
name|getReduceKeyTableDesc
argument_list|(
name|PlanUtils
operator|.
name|getFieldSchemasFromColumnList
argument_list|(
name|keyCols
argument_list|,
literal|"mapjoinkey"
argument_list|)
argument_list|,
name|keyOrder
operator|.
name|toString
argument_list|()
argument_list|,
name|keyNullOrder
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|setKeyTableDesc
argument_list|(
name|keyTableDesc
argument_list|)
expr_stmt|;
comment|// let the dummy op be the parent of mapjoin op
name|mapJoinOp
operator|.
name|replaceParent
argument_list|(
name|parentRS
argument_list|,
name|dummyOp
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|dummyChildren
init|=
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
decl_stmt|;
name|dummyChildren
operator|.
name|add
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
name|dummyOp
operator|.
name|setChildOperators
argument_list|(
name|dummyChildren
argument_list|)
expr_stmt|;
name|dummyOperators
operator|.
name|add
argument_list|(
name|dummyOp
argument_list|)
expr_stmt|;
comment|// cut the operator tree so as to not retain connections from the parent RS downstream
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|childOperators
init|=
name|parentRS
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
name|int
name|childIndex
init|=
name|childOperators
operator|.
name|indexOf
argument_list|(
name|mapJoinOp
argument_list|)
decl_stmt|;
name|childOperators
operator|.
name|remove
argument_list|(
name|childIndex
argument_list|)
expr_stmt|;
comment|// the "work" needs to know about the dummy operators. They have to be separately initialized
comment|// at task startup
if|if
condition|(
name|mapJoinWork
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|BaseWork
name|myWork
range|:
name|mapJoinWork
control|)
block|{
name|myWork
operator|.
name|addDummyOp
argument_list|(
name|dummyOp
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|context
operator|.
name|linkChildOpWithDummyOp
operator|.
name|containsKey
argument_list|(
name|mapJoinOp
argument_list|)
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|context
operator|.
name|linkChildOpWithDummyOp
operator|.
name|get
argument_list|(
name|mapJoinOp
argument_list|)
control|)
block|{
name|dummyOperators
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
block|}
name|context
operator|.
name|linkChildOpWithDummyOp
operator|.
name|put
argument_list|(
name|mapJoinOp
argument_list|,
name|dummyOperators
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

