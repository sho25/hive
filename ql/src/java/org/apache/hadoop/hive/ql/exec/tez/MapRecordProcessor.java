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
name|Collection
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
name|MRInput
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
specifier|static
specifier|final
name|String
name|PLAN_KEY
init|=
literal|"__MAP_PLAN__"
decl_stmt|;
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
annotation|@
name|Override
name|void
name|init
parameter_list|(
name|JobConf
name|jconf
parameter_list|,
name|MRTaskReporter
name|mrReporter
parameter_list|,
name|Collection
argument_list|<
name|LogicalInput
argument_list|>
name|inputs
parameter_list|,
name|OutputCollector
name|out
parameter_list|)
block|{
name|super
operator|.
name|init
argument_list|(
name|jconf
argument_list|,
name|mrReporter
argument_list|,
name|inputs
argument_list|,
name|out
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
name|MapWork
name|mrwork
init|=
operator|(
name|MapWork
operator|)
name|cache
operator|.
name|retrieve
argument_list|(
name|PLAN_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|mrwork
operator|==
literal|null
condition|)
block|{
name|mrwork
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
name|PLAN_KEY
argument_list|,
name|mrwork
argument_list|)
expr_stmt|;
block|}
name|mapOp
operator|=
operator|new
name|MapOperator
argument_list|()
expr_stmt|;
comment|// initialize map operator
name|mapOp
operator|.
name|setConf
argument_list|(
name|mrwork
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
name|mapOp
operator|.
name|setOutputCollector
argument_list|(
name|out
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
block|}
annotation|@
name|Override
name|void
name|run
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|inputs
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"MapRecordProcessor expects single input"
operator|+
literal|", inputCount="
operator|+
name|inputs
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
name|MRInput
name|in
init|=
operator|(
name|MRInput
operator|)
name|inputs
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
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
name|mapOp
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
if|if
condition|(
name|isLogInfoEnabled
condition|)
block|{
name|logCloseInfo
argument_list|()
expr_stmt|;
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

