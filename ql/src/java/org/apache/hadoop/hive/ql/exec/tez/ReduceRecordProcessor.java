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
name|exec
operator|.
name|tez
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
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
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
name|llap
operator|.
name|LlapUtil
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
name|MapredContext
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
name|ObjectCache
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
name|ObjectCacheFactory
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
name|ExecMapper
operator|.
name|ReportStats
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
name|tez
operator|.
name|TezProcessor
operator|.
name|TezKVOutputCollector
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
name|log
operator|.
name|PerfLogger
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|mapreduce
operator|.
name|processor
operator|.
name|MRTaskReporter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|Input
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|LogicalInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|LogicalOutput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|ProcessorContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|Reader
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
name|Lists
import|;
end_import

begin_comment
comment|/**  * Process input from tez LogicalInput and write output - for a map plan  * Just pump the records through the query plan.  */
end_comment

begin_class
specifier|public
class|class
name|ReduceRecordProcessor
extends|extends
name|RecordProcessor
block|{
specifier|private
specifier|static
specifier|final
name|String
name|REDUCE_PLAN_KEY
init|=
literal|"__REDUCE_PLAN__"
decl_stmt|;
specifier|private
name|ObjectCache
name|cache
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Logger
name|l4j
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ReduceRecordProcessor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ReduceWork
name|reduceWork
decl_stmt|;
name|List
argument_list|<
name|BaseWork
argument_list|>
name|mergeWorkList
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|cacheKeys
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|DummyStoreOperator
argument_list|>
name|connectOps
init|=
operator|new
name|TreeMap
argument_list|<
name|Integer
argument_list|,
name|DummyStoreOperator
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|ReduceWork
argument_list|>
name|tagToReducerMap
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|ReduceWork
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Operator
argument_list|<
name|?
argument_list|>
name|reducer
decl_stmt|;
specifier|private
name|ReduceRecordSource
index|[]
name|sources
decl_stmt|;
specifier|private
name|byte
name|bigTablePosition
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|nRows
init|=
literal|0
decl_stmt|;
specifier|public
name|ReduceRecordProcessor
parameter_list|(
specifier|final
name|JobConf
name|jconf
parameter_list|,
specifier|final
name|ProcessorContext
name|context
parameter_list|)
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|jconf
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|String
name|queryId
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|jconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
decl_stmt|;
name|cache
operator|=
name|ObjectCacheFactory
operator|.
name|getCache
argument_list|(
name|jconf
argument_list|,
name|queryId
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|String
name|cacheKey
init|=
name|processorContext
operator|.
name|getTaskVertexName
argument_list|()
operator|+
name|REDUCE_PLAN_KEY
decl_stmt|;
name|cacheKeys
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|cacheKey
argument_list|)
expr_stmt|;
name|reduceWork
operator|=
operator|(
name|ReduceWork
operator|)
name|cache
operator|.
name|retrieve
argument_list|(
name|cacheKey
argument_list|,
operator|new
name|Callable
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|call
parameter_list|()
block|{
return|return
name|Utilities
operator|.
name|getReduceWork
argument_list|(
name|jconf
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|setReduceWork
argument_list|(
name|jconf
argument_list|,
name|reduceWork
argument_list|)
expr_stmt|;
name|mergeWorkList
operator|=
name|getMergeWorkList
argument_list|(
name|jconf
argument_list|,
name|cacheKey
argument_list|,
name|queryId
argument_list|,
name|cache
argument_list|,
name|cacheKeys
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|init
parameter_list|(
name|MRTaskReporter
name|mrReporter
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalInput
argument_list|>
name|inputs
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalOutput
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|Exception
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_INIT_OPERATORS
argument_list|)
expr_stmt|;
name|super
operator|.
name|init
argument_list|(
name|mrReporter
argument_list|,
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|MapredContext
operator|.
name|init
argument_list|(
literal|false
argument_list|,
operator|new
name|JobConf
argument_list|(
name|jconf
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|LogicalInput
argument_list|>
name|shuffleInputs
init|=
name|getShuffleInputs
argument_list|(
name|inputs
argument_list|)
decl_stmt|;
comment|// TODO HIVE-14042. Move to using a loop and a timed wait once TEZ-3302 is fixed.
name|checkAbortCondition
argument_list|()
expr_stmt|;
if|if
condition|(
name|shuffleInputs
operator|!=
literal|null
condition|)
block|{
name|l4j
operator|.
name|info
argument_list|(
literal|"Waiting for ShuffleInputs to become ready"
argument_list|)
expr_stmt|;
name|processorContext
operator|.
name|waitForAllInputsReady
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Input
argument_list|>
argument_list|(
name|shuffleInputs
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|connectOps
operator|.
name|clear
argument_list|()
expr_stmt|;
name|ReduceWork
name|redWork
init|=
name|reduceWork
decl_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"Main work is "
operator|+
name|reduceWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HashTableDummyOperator
argument_list|>
name|workOps
init|=
name|reduceWork
operator|.
name|getDummyOps
argument_list|()
decl_stmt|;
name|HashSet
argument_list|<
name|HashTableDummyOperator
argument_list|>
name|dummyOps
init|=
name|workOps
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|workOps
argument_list|)
decl_stmt|;
name|tagToReducerMap
operator|.
name|put
argument_list|(
name|redWork
operator|.
name|getTag
argument_list|()
argument_list|,
name|redWork
argument_list|)
expr_stmt|;
if|if
condition|(
name|mergeWorkList
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|BaseWork
name|mergeWork
range|:
name|mergeWorkList
control|)
block|{
if|if
condition|(
name|l4j
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|l4j
operator|.
name|debug
argument_list|(
literal|"Additional work "
operator|+
name|mergeWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|workOps
operator|=
name|mergeWork
operator|.
name|getDummyOps
argument_list|()
expr_stmt|;
if|if
condition|(
name|workOps
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|dummyOps
operator|==
literal|null
condition|)
block|{
name|dummyOps
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|workOps
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|dummyOps
operator|.
name|addAll
argument_list|(
name|workOps
argument_list|)
expr_stmt|;
block|}
block|}
name|ReduceWork
name|mergeReduceWork
init|=
operator|(
name|ReduceWork
operator|)
name|mergeWork
decl_stmt|;
name|reducer
operator|=
name|mergeReduceWork
operator|.
name|getReducer
argument_list|()
expr_stmt|;
comment|// Check immediately after reducer is assigned, in cae the abort came in during
name|checkAbortCondition
argument_list|()
expr_stmt|;
name|DummyStoreOperator
name|dummyStoreOp
init|=
name|getJoinParentOp
argument_list|(
name|reducer
argument_list|)
decl_stmt|;
name|connectOps
operator|.
name|put
argument_list|(
name|mergeReduceWork
operator|.
name|getTag
argument_list|()
argument_list|,
name|dummyStoreOp
argument_list|)
expr_stmt|;
name|tagToReducerMap
operator|.
name|put
argument_list|(
name|mergeReduceWork
operator|.
name|getTag
argument_list|()
argument_list|,
name|mergeReduceWork
argument_list|)
expr_stmt|;
block|}
operator|(
operator|(
name|TezContext
operator|)
name|MapredContext
operator|.
name|get
argument_list|()
operator|)
operator|.
name|setDummyOpsMap
argument_list|(
name|connectOps
argument_list|)
expr_stmt|;
block|}
name|checkAbortCondition
argument_list|()
expr_stmt|;
name|bigTablePosition
operator|=
operator|(
name|byte
operator|)
name|reduceWork
operator|.
name|getTag
argument_list|()
expr_stmt|;
name|ObjectInspector
index|[]
name|mainWorkOIs
init|=
literal|null
decl_stmt|;
operator|(
operator|(
name|TezContext
operator|)
name|MapredContext
operator|.
name|get
argument_list|()
operator|)
operator|.
name|setInputs
argument_list|(
name|inputs
argument_list|)
expr_stmt|;
operator|(
operator|(
name|TezContext
operator|)
name|MapredContext
operator|.
name|get
argument_list|()
operator|)
operator|.
name|setTezProcessorContext
argument_list|(
name|processorContext
argument_list|)
expr_stmt|;
name|int
name|numTags
init|=
name|reduceWork
operator|.
name|getTagToValueDesc
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|reducer
operator|=
name|reduceWork
operator|.
name|getReducer
argument_list|()
expr_stmt|;
comment|// Check immediately after reducer is assigned, in cae the abort came in during
name|checkAbortCondition
argument_list|()
expr_stmt|;
comment|// set memory available for operators
name|long
name|memoryAvailableToTask
init|=
name|processorContext
operator|.
name|getTotalMemoryAvailableToTask
argument_list|()
decl_stmt|;
if|if
condition|(
name|reducer
operator|.
name|getConf
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|reducer
operator|.
name|getConf
argument_list|()
operator|.
name|setMaxMemoryAvailable
argument_list|(
name|memoryAvailableToTask
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"Memory available for operators set to {}"
argument_list|,
name|LlapUtil
operator|.
name|humanReadableByteCount
argument_list|(
name|memoryAvailableToTask
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|OperatorUtils
operator|.
name|setMemoryAvailable
argument_list|(
name|reducer
operator|.
name|getChildOperators
argument_list|()
argument_list|,
name|memoryAvailableToTask
argument_list|)
expr_stmt|;
if|if
condition|(
name|numTags
operator|>
literal|1
condition|)
block|{
name|sources
operator|=
operator|new
name|ReduceRecordSource
index|[
name|numTags
index|]
expr_stmt|;
name|mainWorkOIs
operator|=
operator|new
name|ObjectInspector
index|[
name|numTags
index|]
expr_stmt|;
name|initializeMultipleSources
argument_list|(
name|reduceWork
argument_list|,
name|numTags
argument_list|,
name|mainWorkOIs
argument_list|,
name|sources
argument_list|)
expr_stmt|;
operator|(
operator|(
name|TezContext
operator|)
name|MapredContext
operator|.
name|get
argument_list|()
operator|)
operator|.
name|setRecordSources
argument_list|(
name|sources
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|initialize
argument_list|(
name|jconf
argument_list|,
name|mainWorkOIs
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|numTags
operator|=
name|tagToReducerMap
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
name|sources
operator|=
operator|new
name|ReduceRecordSource
index|[
name|numTags
index|]
expr_stmt|;
name|mainWorkOIs
operator|=
operator|new
name|ObjectInspector
index|[
name|numTags
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
range|:
name|tagToReducerMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|redWork
operator|=
name|tagToReducerMap
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|reducer
operator|=
name|redWork
operator|.
name|getReducer
argument_list|()
expr_stmt|;
comment|// Check immediately after reducer is assigned, in cae the abort came in during
name|checkAbortCondition
argument_list|()
expr_stmt|;
name|initializeSourceForTag
argument_list|(
name|redWork
argument_list|,
name|i
argument_list|,
name|mainWorkOIs
argument_list|,
name|sources
argument_list|,
name|redWork
operator|.
name|getTagToValueDesc
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|redWork
operator|.
name|getTagToInput
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|initializeLocalWork
argument_list|(
name|jconf
argument_list|)
expr_stmt|;
block|}
name|reducer
operator|=
name|reduceWork
operator|.
name|getReducer
argument_list|()
expr_stmt|;
comment|// Check immediately after reducer is assigned, in cae the abort came in during
name|checkAbortCondition
argument_list|()
expr_stmt|;
operator|(
operator|(
name|TezContext
operator|)
name|MapredContext
operator|.
name|get
argument_list|()
operator|)
operator|.
name|setRecordSources
argument_list|(
name|sources
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|initialize
argument_list|(
name|jconf
argument_list|,
operator|new
name|ObjectInspector
index|[]
block|{
name|mainWorkOIs
index|[
name|bigTablePosition
index|]
block|}
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
range|:
name|tagToReducerMap
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|i
operator|==
name|bigTablePosition
condition|)
block|{
continue|continue;
block|}
name|redWork
operator|=
name|tagToReducerMap
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|reducer
operator|=
name|redWork
operator|.
name|getReducer
argument_list|()
expr_stmt|;
comment|// Check immediately after reducer is assigned, in cae the abort came in during
name|checkAbortCondition
argument_list|()
expr_stmt|;
name|reducer
operator|.
name|initialize
argument_list|(
name|jconf
argument_list|,
operator|new
name|ObjectInspector
index|[]
block|{
name|mainWorkOIs
index|[
name|i
index|]
block|}
argument_list|)
expr_stmt|;
block|}
block|}
name|checkAbortCondition
argument_list|()
expr_stmt|;
name|reducer
operator|=
name|reduceWork
operator|.
name|getReducer
argument_list|()
expr_stmt|;
comment|// initialize reduce operator tree
try|try
block|{
name|l4j
operator|.
name|info
argument_list|(
name|reducer
operator|.
name|dump
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Initialization isn't finished until all parents of all operators
comment|// are initialized. For broadcast joins that means initializing the
comment|// dummy parent operators as well.
if|if
condition|(
name|dummyOps
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|HashTableDummyOperator
name|dummyOp
range|:
name|dummyOps
control|)
block|{
comment|// TODO HIVE-14042. Propagating abort to dummyOps.
name|dummyOp
operator|.
name|initialize
argument_list|(
name|jconf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|checkAbortCondition
argument_list|()
expr_stmt|;
block|}
block|}
comment|// set output collector for any reduce sink operators in the pipeline.
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|children
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
name|children
operator|.
name|add
argument_list|(
name|reducer
argument_list|)
expr_stmt|;
if|if
condition|(
name|dummyOps
operator|!=
literal|null
condition|)
block|{
name|children
operator|.
name|addAll
argument_list|(
name|dummyOps
argument_list|)
expr_stmt|;
block|}
name|createOutputMap
argument_list|()
expr_stmt|;
name|OperatorUtils
operator|.
name|setChildrenCollector
argument_list|(
name|children
argument_list|,
name|outMap
argument_list|)
expr_stmt|;
name|checkAbortCondition
argument_list|()
expr_stmt|;
name|reducer
operator|.
name|setReporter
argument_list|(
name|reporter
argument_list|)
expr_stmt|;
name|MapredContext
operator|.
name|get
argument_list|()
operator|.
name|setReporter
argument_list|(
name|reporter
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|abort
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|OutOfMemoryError
condition|)
block|{
comment|// Don't create a new object if we are already out of memory
throw|throw
operator|(
name|OutOfMemoryError
operator|)
name|e
throw|;
block|}
elseif|else
if|if
condition|(
name|e
operator|instanceof
name|InterruptedException
condition|)
block|{
name|l4j
operator|.
name|info
argument_list|(
literal|"Hit an interrupt while initializing ReduceRecordProcessor. Message={}"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|(
name|InterruptedException
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Reduce operator initialization failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_INIT_OPERATORS
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|initializeMultipleSources
parameter_list|(
name|ReduceWork
name|redWork
parameter_list|,
name|int
name|numTags
parameter_list|,
name|ObjectInspector
index|[]
name|ois
parameter_list|,
name|ReduceRecordSource
index|[]
name|sources
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|tag
init|=
literal|0
init|;
name|tag
operator|<
name|redWork
operator|.
name|getTagToValueDesc
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|tag
operator|++
control|)
block|{
if|if
condition|(
name|redWork
operator|.
name|getTagToValueDesc
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|checkAbortCondition
argument_list|()
expr_stmt|;
name|initializeSourceForTag
argument_list|(
name|redWork
argument_list|,
name|tag
argument_list|,
name|ois
argument_list|,
name|sources
argument_list|,
name|redWork
operator|.
name|getTagToValueDesc
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
argument_list|,
name|redWork
operator|.
name|getTagToInput
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|initializeSourceForTag
parameter_list|(
name|ReduceWork
name|redWork
parameter_list|,
name|int
name|tag
parameter_list|,
name|ObjectInspector
index|[]
name|ois
parameter_list|,
name|ReduceRecordSource
index|[]
name|sources
parameter_list|,
name|TableDesc
name|valueTableDesc
parameter_list|,
name|String
name|inputName
parameter_list|)
throws|throws
name|Exception
block|{
name|reducer
operator|=
name|redWork
operator|.
name|getReducer
argument_list|()
expr_stmt|;
name|reducer
operator|.
name|getParentOperators
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|reducer
operator|.
name|setParentOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
comment|// clear out any parents as reducer is the root
name|TableDesc
name|keyTableDesc
init|=
name|redWork
operator|.
name|getKeyDesc
argument_list|()
decl_stmt|;
name|Reader
name|reader
init|=
name|inputs
operator|.
name|get
argument_list|(
name|inputName
argument_list|)
operator|.
name|getReader
argument_list|()
decl_stmt|;
name|sources
index|[
name|tag
index|]
operator|=
operator|new
name|ReduceRecordSource
argument_list|()
expr_stmt|;
comment|// Only the big table input source should be vectorized (if applicable)
comment|// Note this behavior may have to change if we ever implement a vectorized merge join
name|boolean
name|vectorizedRecordSource
init|=
operator|(
name|tag
operator|==
name|bigTablePosition
operator|)
operator|&&
name|redWork
operator|.
name|getVectorMode
argument_list|()
decl_stmt|;
name|sources
index|[
name|tag
index|]
operator|.
name|init
argument_list|(
name|jconf
argument_list|,
name|redWork
operator|.
name|getReducer
argument_list|()
argument_list|,
name|vectorizedRecordSource
argument_list|,
name|keyTableDesc
argument_list|,
name|valueTableDesc
argument_list|,
name|reader
argument_list|,
name|tag
operator|==
name|bigTablePosition
argument_list|,
operator|(
name|byte
operator|)
name|tag
argument_list|,
name|redWork
operator|.
name|getVectorizedRowBatchCtx
argument_list|()
argument_list|)
expr_stmt|;
name|ois
index|[
name|tag
index|]
operator|=
name|sources
index|[
name|tag
index|]
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|LogicalOutput
argument_list|>
name|outputEntry
range|:
name|outputs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|l4j
operator|.
name|info
argument_list|(
literal|"Starting Output: "
operator|+
name|outputEntry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|abort
condition|)
block|{
name|outputEntry
operator|.
name|getValue
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
operator|(
operator|(
name|TezKVOutputCollector
operator|)
name|outMap
operator|.
name|get
argument_list|(
name|outputEntry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|)
operator|.
name|initialize
argument_list|()
expr_stmt|;
block|}
block|}
comment|// run the operator pipeline
while|while
condition|(
name|sources
index|[
name|bigTablePosition
index|]
operator|.
name|pushRecord
argument_list|()
condition|)
block|{
if|if
condition|(
name|nRows
operator|++
operator|==
name|CHECK_INTERRUPTION_AFTER_ROWS
condition|)
block|{
name|checkAbortCondition
argument_list|()
expr_stmt|;
name|nRows
operator|=
literal|0
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|()
block|{
comment|// this will stop run() from pushing records, along with potentially
comment|// blocking initialization.
name|super
operator|.
name|abort
argument_list|()
expr_stmt|;
if|if
condition|(
name|reducer
operator|!=
literal|null
condition|)
block|{
name|l4j
operator|.
name|info
argument_list|(
literal|"Forwarding abort to reducer: {} "
operator|+
name|reducer
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|abort
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|l4j
operator|.
name|info
argument_list|(
literal|"reducer not setup yet. abort not being forwarded"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get the inputs that should be streamed through reduce plan.    *    * @param inputs    * @return    * @throws Exception    */
specifier|private
name|List
argument_list|<
name|LogicalInput
argument_list|>
name|getShuffleInputs
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalInput
argument_list|>
name|inputs
parameter_list|)
throws|throws
name|Exception
block|{
comment|// the reduce plan inputs have tags, add all inputs that have tags
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|tagToinput
init|=
name|reduceWork
operator|.
name|getTagToInput
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|LogicalInput
argument_list|>
name|shuffleInputs
init|=
operator|new
name|ArrayList
argument_list|<
name|LogicalInput
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|inpStr
range|:
name|tagToinput
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|inputs
operator|.
name|get
argument_list|(
name|inpStr
argument_list|)
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Cound not find input: "
operator|+
name|inpStr
argument_list|)
throw|;
block|}
name|inputs
operator|.
name|get
argument_list|(
name|inpStr
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|shuffleInputs
operator|.
name|add
argument_list|(
name|inputs
operator|.
name|get
argument_list|(
name|inpStr
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|shuffleInputs
return|;
block|}
annotation|@
name|Override
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|cache
operator|!=
literal|null
operator|&&
name|cacheKeys
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|key
range|:
name|cacheKeys
control|)
block|{
name|cache
operator|.
name|release
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
try|try
block|{
for|for
control|(
name|ReduceRecordSource
name|rs
range|:
name|sources
control|)
block|{
name|abort
operator|=
name|abort
operator|&&
name|rs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|reducer
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
if|if
condition|(
name|mergeWorkList
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|BaseWork
name|redWork
range|:
name|mergeWorkList
control|)
block|{
operator|(
operator|(
name|ReduceWork
operator|)
name|redWork
operator|)
operator|.
name|getReducer
argument_list|()
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Need to close the dummyOps as well. The operator pipeline
comment|// is not considered "closed/done" unless all operators are
comment|// done. For broadcast joins that includes the dummy parents.
name|List
argument_list|<
name|HashTableDummyOperator
argument_list|>
name|dummyOps
init|=
name|reduceWork
operator|.
name|getDummyOps
argument_list|()
decl_stmt|;
if|if
condition|(
name|dummyOps
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|dummyOp
range|:
name|dummyOps
control|)
block|{
name|dummyOp
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
block|}
name|ReportStats
name|rps
init|=
operator|new
name|ReportStats
argument_list|(
name|reporter
argument_list|,
name|jconf
argument_list|)
decl_stmt|;
name|reducer
operator|.
name|preorderMap
argument_list|(
name|rps
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|abort
condition|)
block|{
comment|// signal new failure to map-reduce
name|l4j
operator|.
name|error
argument_list|(
literal|"Hit error while closing operators - failing tree"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive Runtime Error while closing operators: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
finally|finally
block|{
name|Utilities
operator|.
name|clearWorkMap
argument_list|(
name|jconf
argument_list|)
expr_stmt|;
name|MapredContext
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|DummyStoreOperator
name|getJoinParentOp
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|mergeReduceOp
parameter_list|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|childOp
range|:
name|mergeReduceOp
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
if|if
condition|(
operator|(
name|childOp
operator|.
name|getChildOperators
argument_list|()
operator|==
literal|null
operator|)
operator|||
operator|(
name|childOp
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
name|childOp
operator|instanceof
name|DummyStoreOperator
condition|)
block|{
return|return
operator|(
name|DummyStoreOperator
operator|)
name|childOp
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Was expecting dummy store operator but found: "
operator|+
name|childOp
argument_list|)
throw|;
block|}
block|}
else|else
block|{
return|return
name|getJoinParentOp
argument_list|(
name|childOp
argument_list|)
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Expecting a DummyStoreOperator found op: "
operator|+
name|mergeReduceOp
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

