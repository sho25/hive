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
name|Iterator
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
name|reportStats
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
name|InputMerger
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
name|metadata
operator|.
name|HiveException
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
name|SerDe
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
name|SerDeException
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
name|SerDeUtils
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorFactory
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
name|io
operator|.
name|BytesWritable
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
name|hadoop
operator|.
name|mapred
operator|.
name|OutputCollector
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
name|util
operator|.
name|ReflectionUtils
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
name|util
operator|.
name|StringUtils
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
name|TezProcessorContext
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
name|KeyValuesReader
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
name|ReduceRecordProcessor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ExecMapperContext
name|execContext
init|=
operator|new
name|ExecMapperContext
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|abort
init|=
literal|false
decl_stmt|;
specifier|private
name|Deserializer
name|inputKeyDeserializer
decl_stmt|;
comment|// Input value serde needs to be an array to support different SerDe
comment|// for different tags
specifier|private
specifier|final
name|SerDe
index|[]
name|inputValueDeserializer
init|=
operator|new
name|SerDe
index|[
name|Byte
operator|.
name|MAX_VALUE
index|]
decl_stmt|;
name|TableDesc
name|keyTableDesc
decl_stmt|;
name|TableDesc
index|[]
name|valueTableDesc
decl_stmt|;
name|ObjectInspector
index|[]
name|rowObjectInspector
decl_stmt|;
specifier|private
name|Operator
argument_list|<
name|?
argument_list|>
name|reducer
decl_stmt|;
specifier|private
name|boolean
name|isTagged
init|=
literal|false
decl_stmt|;
specifier|private
name|Object
name|keyObject
init|=
literal|null
decl_stmt|;
specifier|private
name|BytesWritable
name|groupKey
decl_stmt|;
specifier|private
name|ReduceWork
name|redWork
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|row
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|Utilities
operator|.
name|reduceFieldNameList
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
name|void
name|init
parameter_list|(
name|JobConf
name|jconf
parameter_list|,
name|TezProcessorContext
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
name|rowObjectInspector
operator|=
operator|new
name|ObjectInspector
index|[
name|Byte
operator|.
name|MAX_VALUE
index|]
expr_stmt|;
name|ObjectInspector
index|[]
name|valueObjectInspector
init|=
operator|new
name|ObjectInspector
index|[
name|Byte
operator|.
name|MAX_VALUE
index|]
decl_stmt|;
name|ObjectInspector
name|keyObjectInspector
decl_stmt|;
name|redWork
operator|=
operator|(
name|ReduceWork
operator|)
name|cache
operator|.
name|retrieve
argument_list|(
name|REDUCE_PLAN_KEY
argument_list|)
expr_stmt|;
if|if
condition|(
name|redWork
operator|==
literal|null
condition|)
block|{
name|redWork
operator|=
name|Utilities
operator|.
name|getReduceWork
argument_list|(
name|jconf
argument_list|)
expr_stmt|;
name|cache
operator|.
name|cache
argument_list|(
name|REDUCE_PLAN_KEY
argument_list|,
name|redWork
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Utilities
operator|.
name|setReduceWork
argument_list|(
name|jconf
argument_list|,
name|redWork
argument_list|)
expr_stmt|;
block|}
name|reducer
operator|=
name|redWork
operator|.
name|getReducer
argument_list|()
expr_stmt|;
name|reducer
operator|.
name|setParentOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
comment|// clear out any parents as reducer is the
comment|// root
name|isTagged
operator|=
name|redWork
operator|.
name|getNeedsTagging
argument_list|()
expr_stmt|;
try|try
block|{
name|keyTableDesc
operator|=
name|redWork
operator|.
name|getKeyDesc
argument_list|()
expr_stmt|;
name|inputKeyDeserializer
operator|=
operator|(
name|SerDe
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|keyTableDesc
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|inputKeyDeserializer
argument_list|,
literal|null
argument_list|,
name|keyTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|keyObjectInspector
operator|=
name|inputKeyDeserializer
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
name|reducer
operator|.
name|setGroupKeyObjectInspector
argument_list|(
name|keyObjectInspector
argument_list|)
expr_stmt|;
name|valueTableDesc
operator|=
operator|new
name|TableDesc
index|[
name|redWork
operator|.
name|getTagToValueDesc
argument_list|()
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
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
comment|// We should initialize the SerDe with the TypeInfo when available.
name|valueTableDesc
index|[
name|tag
index|]
operator|=
name|redWork
operator|.
name|getTagToValueDesc
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
expr_stmt|;
name|inputValueDeserializer
index|[
name|tag
index|]
operator|=
operator|(
name|SerDe
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|valueTableDesc
index|[
name|tag
index|]
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|inputValueDeserializer
index|[
name|tag
index|]
argument_list|,
literal|null
argument_list|,
name|valueTableDesc
index|[
name|tag
index|]
operator|.
name|getProperties
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|valueObjectInspector
index|[
name|tag
index|]
operator|=
name|inputValueDeserializer
index|[
name|tag
index|]
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|ois
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|keyObjectInspector
argument_list|)
expr_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|valueObjectInspector
index|[
name|tag
index|]
argument_list|)
expr_stmt|;
name|rowObjectInspector
index|[
name|tag
index|]
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|Utilities
operator|.
name|reduceFieldNameList
argument_list|,
name|ois
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
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
name|reducer
operator|.
name|initialize
argument_list|(
name|jconf
argument_list|,
name|rowObjectInspector
argument_list|)
expr_stmt|;
comment|// Initialization isn't finished until all parents of all operators
comment|// are initialized. For broadcast joins that means initializing the
comment|// dummy parent operators as well.
name|List
argument_list|<
name|HashTableDummyOperator
argument_list|>
name|dummyOps
init|=
name|redWork
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
comment|// set output collector for any reduce sink operators in the pipeline.
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
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
extends|extends
name|OperatorDesc
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
annotation|@
name|Override
name|void
name|run
parameter_list|()
throws|throws
name|Exception
block|{
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
name|KeyValuesReader
name|kvsReader
decl_stmt|;
try|try
block|{
if|if
condition|(
name|shuffleInputs
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
comment|//no merging of inputs required
name|kvsReader
operator|=
operator|(
name|KeyValuesReader
operator|)
name|shuffleInputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getReader
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|//get a sort merged input
name|kvsReader
operator|=
operator|new
name|InputMerger
argument_list|(
name|shuffleInputs
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
while|while
condition|(
name|kvsReader
operator|.
name|next
argument_list|()
condition|)
block|{
name|Object
name|key
init|=
name|kvsReader
operator|.
name|getCurrentKey
argument_list|()
decl_stmt|;
name|Iterable
argument_list|<
name|Object
argument_list|>
name|values
init|=
name|kvsReader
operator|.
name|getCurrentValues
argument_list|()
decl_stmt|;
name|boolean
name|needMore
init|=
name|processKeyValues
argument_list|(
name|key
argument_list|,
name|values
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|needMore
condition|)
block|{
break|break;
block|}
block|}
block|}
comment|/**    * Get the inputs that should be streamed through reduce plan.    * @param inputs    * @return    */
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
block|{
comment|//the reduce plan inputs have tags, add all inputs that have tags
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|tag2input
init|=
name|redWork
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
name|tag2input
operator|.
name|values
argument_list|()
control|)
block|{
name|shuffleInputs
operator|.
name|add
argument_list|(
operator|(
name|LogicalInput
operator|)
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
comment|/**    * @param key    * @param values    * @return true if it is not done and can take more inputs    */
specifier|private
name|boolean
name|processKeyValues
parameter_list|(
name|Object
name|key
parameter_list|,
name|Iterable
argument_list|<
name|Object
argument_list|>
name|values
parameter_list|)
block|{
if|if
condition|(
name|reducer
operator|.
name|getDone
argument_list|()
condition|)
block|{
comment|//done - no more records needed
return|return
literal|false
return|;
block|}
comment|// reset the execContext for each new row
name|execContext
operator|.
name|resetRow
argument_list|()
expr_stmt|;
try|try
block|{
name|BytesWritable
name|keyWritable
init|=
operator|(
name|BytesWritable
operator|)
name|key
decl_stmt|;
name|byte
name|tag
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|isTagged
condition|)
block|{
comment|// remove the tag from key coming out of reducer
comment|// and store it in separate variable.
name|int
name|size
init|=
name|keyWritable
operator|.
name|getSize
argument_list|()
operator|-
literal|1
decl_stmt|;
name|tag
operator|=
name|keyWritable
operator|.
name|get
argument_list|()
index|[
name|size
index|]
expr_stmt|;
name|keyWritable
operator|.
name|setSize
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
comment|//Set the key, check if this is a new group or same group
if|if
condition|(
operator|!
name|keyWritable
operator|.
name|equals
argument_list|(
name|groupKey
argument_list|)
condition|)
block|{
comment|// If a operator wants to do some work at the beginning of a group
if|if
condition|(
name|groupKey
operator|==
literal|null
condition|)
block|{
comment|// the first group
name|groupKey
operator|=
operator|new
name|BytesWritable
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// If a operator wants to do some work at the end of a group
name|l4j
operator|.
name|trace
argument_list|(
literal|"End Group"
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|endGroup
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|keyObject
operator|=
name|inputKeyDeserializer
operator|.
name|deserialize
argument_list|(
name|keyWritable
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Hive Runtime Error: Unable to deserialize reduce input key from "
operator|+
name|Utilities
operator|.
name|formatBinaryString
argument_list|(
name|keyWritable
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyWritable
operator|.
name|getSize
argument_list|()
argument_list|)
operator|+
literal|" with properties "
operator|+
name|keyTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|groupKey
operator|.
name|set
argument_list|(
name|keyWritable
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyWritable
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|trace
argument_list|(
literal|"Start Group"
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|setGroupKeyObject
argument_list|(
name|keyObject
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|startGroup
argument_list|()
expr_stmt|;
block|}
comment|//process all the values we have for this key
name|Iterator
argument_list|<
name|Object
argument_list|>
name|valuesIt
init|=
name|values
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|valuesIt
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|BytesWritable
name|valueWritable
init|=
operator|(
name|BytesWritable
operator|)
name|valuesIt
operator|.
name|next
argument_list|()
decl_stmt|;
name|Object
name|valueObj
decl_stmt|;
try|try
block|{
name|valueObj
operator|=
name|inputValueDeserializer
index|[
name|tag
index|]
operator|.
name|deserialize
argument_list|(
name|valueWritable
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Hive Runtime Error: Unable to deserialize reduce input value (tag="
operator|+
name|tag
operator|+
literal|") from "
operator|+
name|Utilities
operator|.
name|formatBinaryString
argument_list|(
name|valueWritable
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|valueWritable
operator|.
name|getSize
argument_list|()
argument_list|)
operator|+
literal|" with properties "
operator|+
name|valueTableDesc
index|[
name|tag
index|]
operator|.
name|getProperties
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|row
operator|.
name|clear
argument_list|()
expr_stmt|;
name|row
operator|.
name|add
argument_list|(
name|keyObject
argument_list|)
expr_stmt|;
name|row
operator|.
name|add
argument_list|(
name|valueObj
argument_list|)
expr_stmt|;
try|try
block|{
name|reducer
operator|.
name|processOp
argument_list|(
name|row
argument_list|,
name|tag
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|rowString
init|=
literal|null
decl_stmt|;
try|try
block|{
name|rowString
operator|=
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|row
argument_list|,
name|rowObjectInspector
index|[
name|tag
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e2
parameter_list|)
block|{
name|rowString
operator|=
literal|"[Error getting row data with exception "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e2
argument_list|)
operator|+
literal|" ]"
expr_stmt|;
block|}
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Hive Runtime Error while processing row (tag="
operator|+
name|tag
operator|+
literal|") "
operator|+
name|rowString
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|logProgress
argument_list|()
expr_stmt|;
block|}
block|}
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
else|else
block|{
name|l4j
operator|.
name|fatal
argument_list|(
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|true
return|;
comment|//give me more
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
try|try
block|{
if|if
condition|(
name|groupKey
operator|!=
literal|null
condition|)
block|{
comment|// If a operator wants to do some work at the end of a group
name|l4j
operator|.
name|trace
argument_list|(
literal|"End Group"
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|endGroup
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|logCloseInfo
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
comment|// Need to close the dummyOps as well. The operator pipeline
comment|// is not considered "closed/done" unless all operators are
comment|// done. For broadcast joins that includes the dummy parents.
name|List
argument_list|<
name|HashTableDummyOperator
argument_list|>
name|dummyOps
init|=
name|redWork
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
name|reportStats
name|rps
init|=
operator|new
name|reportStats
argument_list|(
name|reporter
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
argument_list|()
expr_stmt|;
name|MapredContext
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

