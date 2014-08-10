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
name|io
operator|.
name|Writable
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
comment|//Update JobConf using MRInput, info like filename comes via this
name|MRInputLegacy
name|mrInput
init|=
name|TezProcessor
operator|.
name|getMRInput
argument_list|(
name|inputs
argument_list|)
decl_stmt|;
name|Configuration
name|updatedConf
init|=
name|mrInput
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
try|try
block|{
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
name|info
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
name|info
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
comment|// initialize map operator
name|mapOp
operator|.
name|setConf
argument_list|(
name|mapWork
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
name|mapOp
operator|.
name|initialize
argument_list|(
name|jconf
argument_list|,
literal|null
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
annotation|@
name|Override
name|void
name|run
parameter_list|()
throws|throws
name|IOException
block|{
name|MRInputLegacy
name|in
init|=
name|TezProcessor
operator|.
name|getMRInput
argument_list|(
name|inputs
argument_list|)
decl_stmt|;
name|KeyValueReader
name|reader
init|=
name|in
operator|.
name|getReader
argument_list|()
decl_stmt|;
comment|//process records until done
while|while
condition|(
name|reader
operator|.
name|next
argument_list|()
condition|)
block|{
comment|//ignore the key for maps -  reader.getCurrentKey();
name|Object
name|value
init|=
name|reader
operator|.
name|getCurrentValue
argument_list|()
decl_stmt|;
name|boolean
name|needMore
init|=
name|processRow
argument_list|(
name|value
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
comment|/**    * @param value  value to process    * @return true if it is not done and can take more inputs    */
specifier|private
name|boolean
name|processRow
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
comment|// reset the execContext for each new row
name|execContext
operator|.
name|resetRow
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|mapOp
operator|.
name|getDone
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
comment|//done
block|}
else|else
block|{
comment|// Since there is no concept of a group, we don't invoke
comment|// startGroup/endGroup for a mapper
name|mapOp
operator|.
name|process
argument_list|(
operator|(
name|Writable
operator|)
name|value
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|logCloseInfo
argument_list|()
expr_stmt|;
block|}
name|ReportStats
name|rps
init|=
operator|new
name|ReportStats
argument_list|(
name|reporter
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
block|}
end_class

end_unit

