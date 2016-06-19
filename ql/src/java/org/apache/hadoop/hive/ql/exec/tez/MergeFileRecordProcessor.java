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
name|concurrent
operator|.
name|Callable
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
name|io
operator|.
name|merge
operator|.
name|MergeFileWork
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
name|library
operator|.
name|api
operator|.
name|KeyValueReader
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
comment|/**  * Record processor for fast merging of files.  */
end_comment

begin_class
specifier|public
class|class
name|MergeFileRecordProcessor
extends|extends
name|RecordProcessor
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MergeFileRecordProcessor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|mergeOp
decl_stmt|;
specifier|private
name|ExecMapperContext
name|execContext
init|=
literal|null
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
name|String
name|cacheKey
decl_stmt|;
specifier|private
name|MergeFileWork
name|mfWork
decl_stmt|;
name|MRInputLegacy
name|mrInput
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|Object
index|[]
name|row
init|=
operator|new
name|Object
index|[
literal|2
index|]
decl_stmt|;
name|ObjectCache
name|cache
decl_stmt|;
specifier|public
name|MergeFileRecordProcessor
parameter_list|(
specifier|final
name|JobConf
name|jconf
parameter_list|,
specifier|final
name|ProcessorContext
name|context
parameter_list|)
block|{
name|super
argument_list|(
name|jconf
argument_list|,
name|context
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
comment|// TODO HIVE-14042. Abort handling.
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
name|execContext
operator|=
operator|new
name|ExecMapperContext
argument_list|(
name|jconf
argument_list|)
expr_stmt|;
comment|//Update JobConf using MRInput, info like filename comes via this
name|mrInput
operator|=
name|getMRInput
argument_list|(
name|inputs
argument_list|)
expr_stmt|;
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
name|Map
operator|.
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
name|Map
operator|.
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
name|TezProcessor
operator|.
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
name|cache
init|=
name|ObjectCacheFactory
operator|.
name|getCache
argument_list|(
name|jconf
argument_list|,
name|queryId
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
name|cacheKey
operator|=
name|MAP_PLAN_KEY
expr_stmt|;
name|MapWork
name|mapWork
init|=
operator|(
name|MapWork
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
name|getMapWork
argument_list|(
name|jconf
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|setMapWork
argument_list|(
name|jconf
argument_list|,
name|mapWork
argument_list|)
expr_stmt|;
if|if
condition|(
name|mapWork
operator|instanceof
name|MergeFileWork
condition|)
block|{
name|mfWork
operator|=
operator|(
name|MergeFileWork
operator|)
name|mapWork
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"MapWork should be an instance of MergeFileWork."
argument_list|)
throw|;
block|}
name|String
name|alias
init|=
name|mfWork
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
name|mergeOp
operator|=
name|mfWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|mergeOp
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
name|mergeOp
operator|.
name|passExecContext
argument_list|(
name|execContext
argument_list|)
expr_stmt|;
name|mergeOp
operator|.
name|initializeLocalWork
argument_list|(
name|jconf
argument_list|)
expr_stmt|;
name|mergeOp
operator|.
name|initialize
argument_list|(
name|jconf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|OperatorUtils
operator|.
name|setChildrenCollector
argument_list|(
name|mergeOp
operator|.
name|getChildOperators
argument_list|()
argument_list|,
name|outMap
argument_list|)
expr_stmt|;
name|mergeOp
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
literal|"Hit an interrupt while initializing MergeFileRecordProcessor. Message={}"
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
name|Exception
block|{
name|KeyValueReader
name|reader
init|=
name|mrInput
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
name|boolean
name|needMore
init|=
name|processRow
argument_list|(
name|reader
operator|.
name|getCurrentKey
argument_list|()
argument_list|,
name|reader
operator|.
name|getCurrentValue
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|needMore
operator|||
name|abort
condition|)
block|{
break|break;
block|}
block|}
block|}
annotation|@
name|Override
name|void
name|abort
parameter_list|()
block|{
name|super
operator|.
name|abort
argument_list|()
expr_stmt|;
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
name|cacheKey
operator|!=
literal|null
condition|)
block|{
name|cache
operator|.
name|release
argument_list|(
name|cacheKey
argument_list|)
expr_stmt|;
block|}
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
name|mergeOp
operator|==
literal|null
operator|||
name|mfWork
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|mergeOp
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
name|ExecMapper
operator|.
name|ReportStats
name|rps
init|=
operator|new
name|ExecMapper
operator|.
name|ReportStats
argument_list|(
name|reporter
argument_list|,
name|jconf
argument_list|)
decl_stmt|;
name|mergeOp
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
comment|/**    * @param key   key to process    * @param value value to process    * @return true if it is not done and can take more inputs    */
specifier|private
name|boolean
name|processRow
parameter_list|(
name|Object
name|key
parameter_list|,
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
name|mergeOp
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
name|row
index|[
literal|0
index|]
operator|=
name|key
expr_stmt|;
name|row
index|[
literal|1
index|]
operator|=
name|value
expr_stmt|;
name|mergeOp
operator|.
name|process
argument_list|(
name|row
argument_list|,
literal|0
argument_list|)
expr_stmt|;
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
name|error
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
name|LOG
operator|.
name|info
argument_list|(
literal|"The inputs are: "
operator|+
name|inputs
argument_list|)
expr_stmt|;
comment|// start the mr input and wait for ready event. number of MRInput is expected to be 1
name|List
argument_list|<
name|Input
argument_list|>
name|li
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|int
name|numMRInputs
init|=
literal|0
decl_stmt|;
for|for
control|(
name|LogicalInput
name|inp
range|:
name|inputs
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|inp
operator|instanceof
name|MRInputLegacy
condition|)
block|{
name|numMRInputs
operator|++
expr_stmt|;
if|if
condition|(
name|numMRInputs
operator|>
literal|1
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
name|inp
operator|.
name|start
argument_list|()
expr_stmt|;
name|li
operator|.
name|add
argument_list|(
name|inp
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expecting only one input of type MRInputLegacy."
operator|+
literal|" Found type: "
operator|+
name|inp
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
comment|// typically alter table .. concatenate is run on only one partition/one table,
comment|// so it doesn't matter if we wait for all inputs or any input to be ready.
name|processorContext
operator|.
name|waitForAnyInputReady
argument_list|(
name|li
argument_list|)
expr_stmt|;
specifier|final
name|MRInputLegacy
name|theMRInput
decl_stmt|;
if|if
condition|(
name|li
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|theMRInput
operator|=
operator|(
name|MRInputLegacy
operator|)
name|li
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|theMRInput
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"MRInputs count is expected to be 1"
argument_list|)
throw|;
block|}
return|return
name|theMRInput
return|;
block|}
block|}
end_class

end_unit

