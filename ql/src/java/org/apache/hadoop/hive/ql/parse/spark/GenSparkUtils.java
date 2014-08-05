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
name|Deque
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
name|Iterator
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
name|LinkedList
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
name|FetchTask
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
name|PrunedPartitionList
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
name|FileSinkDesc
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
comment|/**  * GenSparkUtils is a collection of shared helper methods to produce SparkWork  * Cloned from GenTezUtils.  * TODO: need to make it fit to Spark  */
end_comment

begin_class
specifier|public
class|class
name|GenSparkUtils
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|logger
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|GenSparkUtils
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
comment|// singleton
specifier|private
specifier|static
name|GenSparkUtils
name|utils
decl_stmt|;
specifier|public
specifier|static
name|GenSparkUtils
name|getUtils
parameter_list|()
block|{
if|if
condition|(
name|utils
operator|==
literal|null
condition|)
block|{
name|utils
operator|=
operator|new
name|GenSparkUtils
argument_list|()
expr_stmt|;
block|}
return|return
name|utils
return|;
block|}
specifier|protected
name|GenSparkUtils
parameter_list|()
block|{   }
specifier|public
name|void
name|resetSequenceNumber
parameter_list|()
block|{
name|sequenceNumber
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|UnionWork
name|createUnionWork
parameter_list|(
name|GenSparkProcContext
name|context
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|operator
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
block|{
name|UnionWork
name|unionWork
init|=
operator|new
name|UnionWork
argument_list|(
literal|"Union "
operator|+
operator|(
operator|++
name|sequenceNumber
operator|)
argument_list|)
decl_stmt|;
name|context
operator|.
name|unionWorkMap
operator|.
name|put
argument_list|(
name|operator
argument_list|,
name|unionWork
argument_list|)
expr_stmt|;
name|sparkWork
operator|.
name|add
argument_list|(
name|unionWork
argument_list|)
expr_stmt|;
return|return
name|unionWork
return|;
block|}
specifier|public
name|ReduceWork
name|createReduceWork
parameter_list|(
name|GenSparkProcContext
name|context
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|root
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
operator|!
name|root
operator|.
name|getParentOperators
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|,
literal|"AssertionError: expected root.getParentOperators() to be non-empty"
argument_list|)
expr_stmt|;
name|boolean
name|isAutoReduceParallelism
init|=
name|context
operator|.
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|TEZ_AUTO_REDUCER_PARALLELISM
argument_list|)
decl_stmt|;
name|float
name|maxPartitionFactor
init|=
name|context
operator|.
name|conf
operator|.
name|getFloatVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|TEZ_MAX_PARTITION_FACTOR
argument_list|)
decl_stmt|;
name|float
name|minPartitionFactor
init|=
name|context
operator|.
name|conf
operator|.
name|getFloatVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|TEZ_MIN_PARTITION_FACTOR
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
name|logger
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
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|context
operator|.
name|parentOfRoot
operator|instanceof
name|ReduceSinkOperator
argument_list|,
literal|"AssertionError: expected context.parentOfRoot to be an instance of ReduceSinkOperator, but was "
operator|+
name|context
operator|.
name|parentOfRoot
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|isAutoReduceParallelism
operator|&&
name|reduceSink
operator|.
name|getConf
argument_list|()
operator|.
name|isAutoParallel
argument_list|()
condition|)
block|{
name|reduceWork
operator|.
name|setAutoReduceParallelism
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// configured limit for reducers
name|int
name|maxReducers
init|=
name|context
operator|.
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
comment|// min we allow spark to pick
name|int
name|minPartition
init|=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
call|(
name|int
call|)
argument_list|(
name|reduceSink
operator|.
name|getConf
argument_list|()
operator|.
name|getNumReducers
argument_list|()
operator|*
name|minPartitionFactor
argument_list|)
argument_list|)
decl_stmt|;
name|minPartition
operator|=
operator|(
name|minPartition
operator|>
name|maxReducers
operator|)
condition|?
name|maxReducers
else|:
name|minPartition
expr_stmt|;
comment|// max we allow spark to pick
name|int
name|maxPartition
init|=
call|(
name|int
call|)
argument_list|(
name|reduceSink
operator|.
name|getConf
argument_list|()
operator|.
name|getNumReducers
argument_list|()
operator|*
name|maxPartitionFactor
argument_list|)
decl_stmt|;
name|maxPartition
operator|=
operator|(
name|maxPartition
operator|>
name|maxReducers
operator|)
condition|?
name|maxReducers
else|:
name|maxPartition
expr_stmt|;
name|reduceWork
operator|.
name|setMinReduceTasks
argument_list|(
name|minPartition
argument_list|)
expr_stmt|;
name|reduceWork
operator|.
name|setMaxReduceTasks
argument_list|(
name|maxPartition
argument_list|)
expr_stmt|;
block|}
name|setupReduceSink
argument_list|(
name|context
argument_list|,
name|reduceWork
argument_list|,
name|reduceSink
argument_list|)
expr_stmt|;
name|sparkWork
operator|.
name|add
argument_list|(
name|reduceWork
argument_list|)
expr_stmt|;
name|SparkEdgeProperty
name|edgeProp
decl_stmt|;
if|if
condition|(
name|reduceWork
operator|.
name|isAutoReduceParallelism
argument_list|()
condition|)
block|{
name|edgeProp
operator|=
operator|new
name|SparkEdgeProperty
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|edgeProp
operator|=
operator|new
name|SparkEdgeProperty
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|sparkWork
operator|.
name|connect
argument_list|(
name|context
operator|.
name|preceedingWork
argument_list|,
name|reduceWork
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
name|reduceSink
argument_list|)
expr_stmt|;
return|return
name|reduceWork
return|;
block|}
specifier|protected
name|void
name|setupReduceSink
parameter_list|(
name|GenSparkProcContext
name|context
parameter_list|,
name|ReduceWork
name|reduceWork
parameter_list|,
name|ReduceSinkOperator
name|reduceSink
parameter_list|)
block|{
name|logger
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
specifier|public
name|MapWork
name|createMapWork
parameter_list|(
name|GenSparkProcContext
name|context
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|root
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|,
name|PrunedPartitionList
name|partitions
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|root
operator|.
name|getParentOperators
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|,
literal|"AssertionError: expected root.getParentOperators() to be empty"
argument_list|)
expr_stmt|;
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
name|logger
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
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|root
operator|instanceof
name|TableScanOperator
argument_list|,
literal|"AssertionError: expected root to be an instance of TableScanOperator, but was "
operator|+
name|root
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
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
name|setupMapWork
argument_list|(
name|mapWork
argument_list|,
name|context
argument_list|,
name|partitions
argument_list|,
name|root
argument_list|,
name|alias
argument_list|)
expr_stmt|;
comment|// add new item to the Spark work
name|sparkWork
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
comment|// this method's main use is to help unit testing this class
specifier|protected
name|void
name|setupMapWork
parameter_list|(
name|MapWork
name|mapWork
parameter_list|,
name|GenSparkProcContext
name|context
parameter_list|,
name|PrunedPartitionList
name|partitions
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|root
parameter_list|,
name|String
name|alias
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// All the setup is done in GenMapRedUtils
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
name|partitions
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
block|}
comment|// removes any union operator and clones the plan
specifier|public
name|void
name|removeUnionOperators
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|GenSparkProcContext
name|context
parameter_list|,
name|BaseWork
name|work
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|roots
init|=
name|work
operator|.
name|getAllRootOperators
argument_list|()
decl_stmt|;
if|if
condition|(
name|work
operator|.
name|getDummyOps
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|roots
operator|.
name|addAll
argument_list|(
name|work
operator|.
name|getDummyOps
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// need to clone the plan.
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|newRoots
init|=
name|Utilities
operator|.
name|cloneOperatorTree
argument_list|(
name|conf
argument_list|,
name|roots
argument_list|)
decl_stmt|;
comment|// we're cloning the operator plan but we're retaining the original work. That means
comment|// that root operators have to be replaced with the cloned ops. The replacement map
comment|// tells you what that mapping is.
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|replacementMap
init|=
operator|new
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// there's some special handling for dummyOps required. Mapjoins won't be properly
comment|// initialized if their dummy parents aren't initialized. Since we cloned the plan
comment|// we need to replace the dummy operators in the work with the cloned ones.
name|List
argument_list|<
name|HashTableDummyOperator
argument_list|>
name|dummyOps
init|=
operator|new
name|LinkedList
argument_list|<
name|HashTableDummyOperator
argument_list|>
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|it
init|=
name|newRoots
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|orig
range|:
name|roots
control|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|newRoot
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|newRoot
operator|instanceof
name|HashTableDummyOperator
condition|)
block|{
name|dummyOps
operator|.
name|add
argument_list|(
operator|(
name|HashTableDummyOperator
operator|)
name|newRoot
argument_list|)
expr_stmt|;
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|replacementMap
operator|.
name|put
argument_list|(
name|orig
argument_list|,
name|newRoot
argument_list|)
expr_stmt|;
block|}
block|}
comment|// now we remove all the unions. we throw away any branch that's not reachable from
comment|// the current set of roots. The reason is that those branches will be handled in
comment|// different tasks.
name|Deque
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|operators
init|=
operator|new
name|LinkedList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|operators
operator|.
name|addAll
argument_list|(
name|newRoots
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|seen
init|=
operator|new
name|HashSet
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|operators
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|current
init|=
name|operators
operator|.
name|pop
argument_list|()
decl_stmt|;
name|seen
operator|.
name|add
argument_list|(
name|current
argument_list|)
expr_stmt|;
if|if
condition|(
name|current
operator|instanceof
name|FileSinkOperator
condition|)
block|{
name|FileSinkOperator
name|fileSink
init|=
operator|(
name|FileSinkOperator
operator|)
name|current
decl_stmt|;
comment|// remember it for additional processing later
name|context
operator|.
name|fileSinkSet
operator|.
name|add
argument_list|(
name|fileSink
argument_list|)
expr_stmt|;
name|FileSinkDesc
name|desc
init|=
name|fileSink
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|Path
name|path
init|=
name|desc
operator|.
name|getDirName
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FileSinkDesc
argument_list|>
name|linked
decl_stmt|;
if|if
condition|(
operator|!
name|context
operator|.
name|linkedFileSinks
operator|.
name|containsKey
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|linked
operator|=
operator|new
name|ArrayList
argument_list|<
name|FileSinkDesc
argument_list|>
argument_list|()
expr_stmt|;
name|context
operator|.
name|linkedFileSinks
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|linked
argument_list|)
expr_stmt|;
block|}
name|linked
operator|=
name|context
operator|.
name|linkedFileSinks
operator|.
name|get
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|linked
operator|.
name|add
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setDirName
argument_list|(
operator|new
name|Path
argument_list|(
name|path
argument_list|,
literal|""
operator|+
name|linked
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setLinkedFileSinkDesc
argument_list|(
name|linked
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|current
operator|instanceof
name|UnionOperator
condition|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|parent
init|=
literal|null
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|current
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
name|seen
operator|.
name|contains
argument_list|(
name|op
argument_list|)
condition|)
block|{
operator|++
name|count
expr_stmt|;
name|parent
operator|=
name|op
expr_stmt|;
block|}
block|}
comment|// we should have been able to reach the union from only one side.
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|count
operator|<=
literal|1
argument_list|,
literal|"AssertionError: expected count to be<= 1, but was "
operator|+
name|count
argument_list|)
expr_stmt|;
if|if
condition|(
name|parent
operator|==
literal|null
condition|)
block|{
comment|// root operator is union (can happen in reducers)
name|replacementMap
operator|.
name|put
argument_list|(
name|current
argument_list|,
name|current
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|parent
operator|.
name|removeChildAndAdoptItsChildren
argument_list|(
name|current
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|current
operator|instanceof
name|FileSinkOperator
operator|||
name|current
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
name|current
operator|.
name|setChildOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|operators
operator|.
name|addAll
argument_list|(
name|current
operator|.
name|getChildOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|work
operator|.
name|setDummyOps
argument_list|(
name|dummyOps
argument_list|)
expr_stmt|;
name|work
operator|.
name|replaceRoots
argument_list|(
name|replacementMap
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|processFileSink
parameter_list|(
name|GenSparkProcContext
name|context
parameter_list|,
name|FileSinkOperator
name|fileSink
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ParseContext
name|parseContext
init|=
name|context
operator|.
name|parseContext
decl_stmt|;
name|boolean
name|isInsertTable
init|=
comment|// is INSERT OVERWRITE TABLE
name|GenMapRedUtils
operator|.
name|isInsertInto
argument_list|(
name|parseContext
argument_list|,
name|fileSink
argument_list|)
decl_stmt|;
name|HiveConf
name|hconf
init|=
name|parseContext
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|boolean
name|chDir
init|=
name|GenMapRedUtils
operator|.
name|isMergeRequired
argument_list|(
name|context
operator|.
name|moveTask
argument_list|,
name|hconf
argument_list|,
name|fileSink
argument_list|,
name|context
operator|.
name|currentTask
argument_list|,
name|isInsertTable
argument_list|)
decl_stmt|;
name|Path
name|finalName
init|=
name|GenMapRedUtils
operator|.
name|createMoveTask
argument_list|(
name|context
operator|.
name|currentTask
argument_list|,
name|chDir
argument_list|,
name|fileSink
argument_list|,
name|parseContext
argument_list|,
name|context
operator|.
name|moveTask
argument_list|,
name|hconf
argument_list|,
name|context
operator|.
name|dependencyTask
argument_list|)
decl_stmt|;
if|if
condition|(
name|chDir
condition|)
block|{
comment|// Merge the files in the destination table/partitions by creating Map-only merge job
comment|// If underlying data is RCFile a RCFileBlockMerge task would be created.
name|logger
operator|.
name|info
argument_list|(
literal|"using CombineHiveInputformat for the merge job"
argument_list|)
expr_stmt|;
name|GenMapRedUtils
operator|.
name|createMRWorkForMergingFiles
argument_list|(
name|fileSink
argument_list|,
name|finalName
argument_list|,
name|context
operator|.
name|dependencyTask
argument_list|,
name|context
operator|.
name|moveTask
argument_list|,
name|hconf
argument_list|,
name|context
operator|.
name|currentTask
argument_list|)
expr_stmt|;
block|}
name|FetchTask
name|fetchTask
init|=
name|parseContext
operator|.
name|getFetchTask
argument_list|()
decl_stmt|;
if|if
condition|(
name|fetchTask
operator|!=
literal|null
operator|&&
name|context
operator|.
name|currentTask
operator|.
name|getNumChild
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|fetchTask
operator|.
name|isFetchFrom
argument_list|(
name|fileSink
operator|.
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
name|context
operator|.
name|currentTask
operator|.
name|setFetchSource
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

