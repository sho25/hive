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
name|io
operator|.
name|IOException
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|MapOperator
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
name|mr
operator|.
name|ExecMapperContext
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
name|exec
operator|.
name|tez
operator|.
name|tools
operator|.
name|KeyValueInputMerger
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
name|vector
operator|.
name|VectorMapOperator
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
name|serde2
operator|.
name|Deserializer
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
name|input
operator|.
name|MRInputLegacy
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
name|input
operator|.
name|MultiMRInput
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
name|library
operator|.
name|api
operator|.
name|KeyValueReader
import|;
end_import

begin_comment
comment|/**  * Process input from tez LogicalInput and write output - for a map plan  * Just pump the records through the query plan.  */
end_comment

begin_class
specifier|public
class|class
name|MapRecordProcessor
extends|extends
name|RecordProcessor
block|{
specifier|private
name|MapOperator
name|mapOp
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|MapOperator
argument_list|>
name|mergeMapOpList
init|=
operator|new
name|ArrayList
argument_list|<
name|MapOperator
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Log
name|l4j
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MapRecordProcessor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|MapRecordSource
index|[]
name|sources
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|MultiMRInput
argument_list|>
name|multiMRInputMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|MultiMRInput
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|int
name|position
init|=
literal|0
decl_stmt|;
specifier|private
name|boolean
name|foundCachedMergeWork
init|=
literal|false
decl_stmt|;
name|MRInputLegacy
name|legacyMRInput
init|=
literal|null
decl_stmt|;
name|MultiMRInput
name|mainWorkMultiMRInput
init|=
literal|null
decl_stmt|;
specifier|private
name|ExecMapperContext
name|execContext
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|abort
init|=
literal|false
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|MAP_PLAN_KEY
init|=
literal|"__MAP_PLAN__"
decl_stmt|;
specifier|private
name|MapWork
name|mapWork
decl_stmt|;
name|List
argument_list|<
name|MapWork
argument_list|>
name|mergeWorkList
init|=
literal|null
decl_stmt|;
specifier|public
name|MapRecordProcessor
parameter_list|(
name|JobConf
name|jconf
parameter_list|)
throws|throws
name|Exception
block|{
name|ObjectCache
name|cache
init|=
name|ObjectCacheFactory
operator|.
name|getCache
argument_list|(
name|jconf
argument_list|)
decl_stmt|;
name|execContext
operator|=
operator|new
name|ExecMapperContext
argument_list|(
name|jconf
argument_list|)
expr_stmt|;
name|execContext
operator|.
name|setJc
argument_list|(
name|jconf
argument_list|)
expr_stmt|;
comment|// create map and fetch operators
name|mapWork
operator|=
operator|(
name|MapWork
operator|)
name|cache
operator|.
name|retrieve
argument_list|(
name|MAP_PLAN_KEY
argument_list|)
expr_stmt|;
if|if
condition|(
name|mapWork
operator|==
literal|null
condition|)
block|{
name|mapWork
operator|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|jconf
argument_list|)
expr_stmt|;
name|cache
operator|.
name|cache
argument_list|(
name|MAP_PLAN_KEY
argument_list|,
name|mapWork
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
literal|"Plan: "
operator|+
name|mapWork
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|mapWork
operator|.
name|getAliases
argument_list|()
control|)
block|{
name|l4j
operator|.
name|debug
argument_list|(
literal|"Alias: "
operator|+
name|s
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|Utilities
operator|.
name|setMapWork
argument_list|(
name|jconf
argument_list|,
name|mapWork
argument_list|)
expr_stmt|;
block|}
name|String
name|prefixes
init|=
name|jconf
operator|.
name|get
argument_list|(
name|DagUtils
operator|.
name|TEZ_MERGE_WORK_FILE_PREFIXES
argument_list|)
decl_stmt|;
if|if
condition|(
name|prefixes
operator|!=
literal|null
condition|)
block|{
name|mergeWorkList
operator|=
operator|new
name|ArrayList
argument_list|<
name|MapWork
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|prefix
range|:
name|prefixes
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
name|MapWork
name|mergeMapWork
init|=
operator|(
name|MapWork
operator|)
name|cache
operator|.
name|retrieve
argument_list|(
name|prefix
argument_list|)
decl_stmt|;
if|if
condition|(
name|mergeMapWork
operator|!=
literal|null
condition|)
block|{
name|l4j
operator|.
name|info
argument_list|(
literal|"Found merge work in cache"
argument_list|)
expr_stmt|;
name|foundCachedMergeWork
operator|=
literal|true
expr_stmt|;
name|mergeWorkList
operator|.
name|add
argument_list|(
name|mergeMapWork
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|foundCachedMergeWork
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Should find all work in cache else operator pipeline will be in non-deterministic state"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|(
name|prefix
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|prefix
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
operator|)
condition|)
block|{
name|mergeMapWork
operator|=
operator|(
name|MapWork
operator|)
name|Utilities
operator|.
name|getMergeWork
argument_list|(
name|jconf
argument_list|,
name|prefix
argument_list|)
expr_stmt|;
name|mergeWorkList
operator|.
name|add
argument_list|(
name|mergeMapWork
argument_list|)
expr_stmt|;
name|cache
operator|.
name|cache
argument_list|(
name|prefix
argument_list|,
name|mergeMapWork
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
name|void
name|init
parameter_list|(
name|JobConf
name|jconf
parameter_list|,
name|ProcessorContext
name|processorContext
parameter_list|,
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
name|jconf
argument_list|,
name|processorContext
argument_list|,
name|mrReporter
argument_list|,
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
comment|// Update JobConf using MRInput, info like filename comes via this
name|legacyMRInput
operator|=
name|getMRInput
argument_list|(
name|inputs
argument_list|)
expr_stmt|;
if|if
condition|(
name|legacyMRInput
operator|!=
literal|null
condition|)
block|{
name|Configuration
name|updatedConf
init|=
name|legacyMRInput
operator|.
name|getConfigUpdates
argument_list|()
decl_stmt|;
if|if
condition|(
name|updatedConf
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|updatedConf
control|)
block|{
name|jconf
operator|.
name|set
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|createOutputMap
argument_list|()
expr_stmt|;
comment|// Start all the Outputs.
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
name|debug
argument_list|(
literal|"Starting Output: "
operator|+
name|outputEntry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
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
try|try
block|{
if|if
condition|(
name|mapWork
operator|.
name|getVectorMode
argument_list|()
condition|)
block|{
name|mapOp
operator|=
operator|new
name|VectorMapOperator
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|mapOp
operator|=
operator|new
name|MapOperator
argument_list|()
expr_stmt|;
block|}
name|mapOp
operator|.
name|clearConnectedOperators
argument_list|()
expr_stmt|;
if|if
condition|(
name|mergeWorkList
operator|!=
literal|null
condition|)
block|{
name|MapOperator
name|mergeMapOp
init|=
literal|null
decl_stmt|;
for|for
control|(
name|MapWork
name|mergeMapWork
range|:
name|mergeWorkList
control|)
block|{
if|if
condition|(
name|mergeMapWork
operator|.
name|getVectorMode
argument_list|()
condition|)
block|{
name|mergeMapOp
operator|=
operator|new
name|VectorMapOperator
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|mergeMapOp
operator|=
operator|new
name|MapOperator
argument_list|()
expr_stmt|;
block|}
name|mergeMapOpList
operator|.
name|add
argument_list|(
name|mergeMapOp
argument_list|)
expr_stmt|;
comment|// initialize the merge operators first.
if|if
condition|(
name|mergeMapOp
operator|!=
literal|null
condition|)
block|{
name|mergeMapOp
operator|.
name|setConf
argument_list|(
name|mergeMapWork
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"Input name is "
operator|+
name|mergeMapWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|jconf
operator|.
name|set
argument_list|(
name|Utilities
operator|.
name|INPUT_NAME
argument_list|,
name|mergeMapWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|mergeMapOp
operator|.
name|setChildren
argument_list|(
name|jconf
argument_list|)
expr_stmt|;
if|if
condition|(
name|foundCachedMergeWork
operator|==
literal|false
condition|)
block|{
name|DummyStoreOperator
name|dummyOp
init|=
name|getJoinParentOp
argument_list|(
name|mergeMapOp
argument_list|)
decl_stmt|;
name|mapOp
operator|.
name|setConnectedOperators
argument_list|(
name|mergeMapWork
operator|.
name|getTag
argument_list|()
argument_list|,
name|dummyOp
argument_list|)
expr_stmt|;
block|}
name|mergeMapOp
operator|.
name|setExecContext
argument_list|(
operator|new
name|ExecMapperContext
argument_list|(
name|jconf
argument_list|)
argument_list|)
expr_stmt|;
name|mergeMapOp
operator|.
name|initializeLocalWork
argument_list|(
name|jconf
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// initialize map operator
name|mapOp
operator|.
name|setConf
argument_list|(
name|mapWork
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"Main input name is "
operator|+
name|mapWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|jconf
operator|.
name|set
argument_list|(
name|Utilities
operator|.
name|INPUT_NAME
argument_list|,
name|mapWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|mapOp
operator|.
name|setChildren
argument_list|(
name|jconf
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|info
argument_list|(
name|mapOp
operator|.
name|dump
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|MapredContext
operator|.
name|init
argument_list|(
literal|true
argument_list|,
operator|new
name|JobConf
argument_list|(
name|jconf
argument_list|)
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
name|mapOp
operator|.
name|setExecContext
argument_list|(
name|execContext
argument_list|)
expr_stmt|;
name|mapOp
operator|.
name|initializeLocalWork
argument_list|(
name|jconf
argument_list|)
expr_stmt|;
name|initializeMapRecordSources
argument_list|()
expr_stmt|;
name|mapOp
operator|.
name|initialize
argument_list|(
name|jconf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|mergeMapOpList
operator|!=
literal|null
operator|)
operator|&&
name|mergeMapOpList
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
for|for
control|(
name|MapOperator
name|mergeMapOp
range|:
name|mergeMapOpList
control|)
block|{
name|jconf
operator|.
name|set
argument_list|(
name|Utilities
operator|.
name|INPUT_NAME
argument_list|,
name|mergeMapOp
operator|.
name|getConf
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|mergeMapOp
operator|.
name|initialize
argument_list|(
name|jconf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Initialization isn't finished until all parents of all operators
comment|// are initialized. For broadcast joins that means initializing the
comment|// dummy parent operators as well.
name|List
argument_list|<
name|HashTableDummyOperator
argument_list|>
name|dummyOps
init|=
name|mapWork
operator|.
name|getDummyOps
argument_list|()
decl_stmt|;
name|jconf
operator|.
name|set
argument_list|(
name|Utilities
operator|.
name|INPUT_NAME
argument_list|,
name|mapWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
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
extends|extends
name|OperatorDesc
argument_list|>
name|dummyOp
range|:
name|dummyOps
control|)
block|{
name|dummyOp
operator|.
name|setExecContext
argument_list|(
name|execContext
argument_list|)
expr_stmt|;
name|dummyOp
operator|.
name|initialize
argument_list|(
name|jconf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
name|OperatorUtils
operator|.
name|setChildrenCollector
argument_list|(
name|mapOp
operator|.
name|getChildOperators
argument_list|()
argument_list|,
name|outMap
argument_list|)
expr_stmt|;
name|mapOp
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
comment|// will this be true here?
comment|// Don't create a new object if we are already out of memory
throw|throw
operator|(
name|OutOfMemoryError
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
literal|"Map operator initialization failed"
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
name|initializeMapRecordSources
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|size
init|=
name|mergeMapOpList
operator|.
name|size
argument_list|()
operator|+
literal|1
decl_stmt|;
comment|// the +1 is for the main map operator itself
name|sources
operator|=
operator|new
name|MapRecordSource
index|[
name|size
index|]
expr_stmt|;
name|position
operator|=
name|mapOp
operator|.
name|getConf
argument_list|()
operator|.
name|getTag
argument_list|()
expr_stmt|;
name|sources
index|[
name|position
index|]
operator|=
operator|new
name|MapRecordSource
argument_list|()
expr_stmt|;
name|KeyValueReader
name|reader
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|mainWorkMultiMRInput
operator|!=
literal|null
condition|)
block|{
name|reader
operator|=
name|getKeyValueReader
argument_list|(
name|mainWorkMultiMRInput
operator|.
name|getKeyValueReaders
argument_list|()
argument_list|,
name|mapOp
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|reader
operator|=
name|legacyMRInput
operator|.
name|getReader
argument_list|()
expr_stmt|;
block|}
name|sources
index|[
name|position
index|]
operator|.
name|init
argument_list|(
name|jconf
argument_list|,
name|mapOp
argument_list|,
name|reader
argument_list|)
expr_stmt|;
for|for
control|(
name|MapOperator
name|mapOp
range|:
name|mergeMapOpList
control|)
block|{
name|int
name|tag
init|=
name|mapOp
operator|.
name|getConf
argument_list|()
operator|.
name|getTag
argument_list|()
decl_stmt|;
name|sources
index|[
name|tag
index|]
operator|=
operator|new
name|MapRecordSource
argument_list|()
expr_stmt|;
name|String
name|inputName
init|=
name|mapOp
operator|.
name|getConf
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|MultiMRInput
name|multiMRInput
init|=
name|multiMRInputMap
operator|.
name|get
argument_list|(
name|inputName
argument_list|)
decl_stmt|;
name|Collection
argument_list|<
name|KeyValueReader
argument_list|>
name|kvReaders
init|=
name|multiMRInput
operator|.
name|getKeyValueReaders
argument_list|()
decl_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
literal|"There are "
operator|+
name|kvReaders
operator|.
name|size
argument_list|()
operator|+
literal|" key-value readers for input "
operator|+
name|inputName
argument_list|)
expr_stmt|;
name|reader
operator|=
name|getKeyValueReader
argument_list|(
name|kvReaders
argument_list|,
name|mapOp
argument_list|)
expr_stmt|;
name|sources
index|[
name|tag
index|]
operator|.
name|init
argument_list|(
name|jconf
argument_list|,
name|mapOp
argument_list|,
name|reader
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
name|setRecordSources
argument_list|(
name|sources
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|private
name|KeyValueReader
name|getKeyValueReader
parameter_list|(
name|Collection
argument_list|<
name|KeyValueReader
argument_list|>
name|keyValueReaders
parameter_list|,
name|MapOperator
name|mapOp
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|KeyValueReader
argument_list|>
name|kvReaderList
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValueReader
argument_list|>
argument_list|(
name|keyValueReaders
argument_list|)
decl_stmt|;
comment|// this sets up the map operator contexts correctly
name|mapOp
operator|.
name|initializeContexts
argument_list|()
expr_stmt|;
name|Deserializer
name|deserializer
init|=
name|mapOp
operator|.
name|getCurrentDeserializer
argument_list|()
decl_stmt|;
name|KeyValueReader
name|reader
init|=
operator|new
name|KeyValueInputMerger
argument_list|(
name|kvReaderList
argument_list|,
name|deserializer
argument_list|,
operator|new
name|ObjectInspector
index|[]
block|{
name|deserializer
operator|.
name|getObjectInspector
argument_list|()
block|}
argument_list|,
name|mapOp
operator|.
name|getConf
argument_list|()
operator|.
name|getSortCols
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|reader
return|;
block|}
specifier|private
name|DummyStoreOperator
name|getJoinParentOp
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|mergeMapOp
parameter_list|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|childOp
range|:
name|mergeMapOp
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
return|return
operator|(
name|DummyStoreOperator
operator|)
name|childOp
return|;
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
return|return
literal|null
return|;
block|}
annotation|@
name|Override
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
while|while
condition|(
name|sources
index|[
name|position
index|]
operator|.
name|pushRecord
argument_list|()
condition|)
block|{}
block|}
annotation|@
name|Override
name|void
name|close
parameter_list|()
block|{
comment|// check if there are IOExceptions
if|if
condition|(
operator|!
name|abort
condition|)
block|{
name|abort
operator|=
name|execContext
operator|.
name|getIoCxt
argument_list|()
operator|.
name|getIOExceptions
argument_list|()
expr_stmt|;
block|}
comment|// detecting failed executions by exceptions thrown by the operator tree
try|try
block|{
if|if
condition|(
name|mapOp
operator|==
literal|null
operator|||
name|mapWork
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|mapOp
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
if|if
condition|(
name|mergeMapOpList
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
for|for
control|(
name|MapOperator
name|mergeMapOp
range|:
name|mergeMapOpList
control|)
block|{
name|mergeMapOp
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
name|mapWork
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
extends|extends
name|OperatorDesc
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
name|mapOp
operator|.
name|preorderMap
argument_list|(
name|rps
argument_list|)
expr_stmt|;
return|return;
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
literal|"Hive Runtime Error while closing operators"
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
argument_list|()
expr_stmt|;
name|MapredContext
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|MRInputLegacy
name|getMRInput
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
comment|// there should be only one MRInput
name|MRInputLegacy
name|theMRInput
init|=
literal|null
decl_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"The input names are: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|inputs
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|LogicalInput
argument_list|>
name|inp
range|:
name|inputs
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|inp
operator|.
name|getValue
argument_list|()
operator|instanceof
name|MRInputLegacy
condition|)
block|{
if|if
condition|(
name|theMRInput
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Only one MRInput is expected"
argument_list|)
throw|;
block|}
comment|// a better logic would be to find the alias
name|theMRInput
operator|=
operator|(
name|MRInputLegacy
operator|)
name|inp
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|inp
operator|.
name|getValue
argument_list|()
operator|instanceof
name|MultiMRInput
condition|)
block|{
name|multiMRInputMap
operator|.
name|put
argument_list|(
name|inp
operator|.
name|getKey
argument_list|()
argument_list|,
operator|(
name|MultiMRInput
operator|)
name|inp
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|theMRInput
operator|!=
literal|null
condition|)
block|{
name|theMRInput
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|String
name|alias
init|=
name|mapWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|inputs
operator|.
name|get
argument_list|(
name|alias
argument_list|)
operator|instanceof
name|MultiMRInput
condition|)
block|{
name|mainWorkMultiMRInput
operator|=
operator|(
name|MultiMRInput
operator|)
name|inputs
operator|.
name|get
argument_list|(
name|alias
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected input type found: "
operator|+
name|inputs
operator|.
name|get
argument_list|(
name|alias
argument_list|)
operator|.
name|getClass
argument_list|()
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
throw|;
block|}
block|}
return|return
name|theMRInput
return|;
block|}
block|}
end_class

end_unit

