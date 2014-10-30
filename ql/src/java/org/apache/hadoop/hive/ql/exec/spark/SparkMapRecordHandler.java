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
name|spark
package|;
end_package

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
name|io
operator|.
name|IOContext
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
name|MapredLocalWork
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
name|mapred
operator|.
name|Reporter
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

begin_comment
comment|/**  * Clone from ExecMapper. SparkMapRecordHandler is the bridge between the spark framework and  * the Hive operator pipeline at execution time. It's main responsibilities are:  *  * - Load and setup the operator pipeline from XML  * - Run the pipeline by transforming key value pairs to records and forwarding them to the operators  * - Stop execution when the "limit" is reached  * - Catch and handle errors during execution of the operators.  *  */
end_comment

begin_class
specifier|public
class|class
name|SparkMapRecordHandler
extends|extends
name|SparkRecordHandler
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
name|mo
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
name|SparkMapRecordHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|done
decl_stmt|;
specifier|private
name|MapredLocalWork
name|localWork
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|isLogInfoEnabled
init|=
literal|false
decl_stmt|;
specifier|private
name|ExecMapperContext
name|execContext
decl_stmt|;
specifier|public
name|void
name|init
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|OutputCollector
name|output
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
block|{
name|super
operator|.
name|init
argument_list|(
name|job
argument_list|,
name|output
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
name|isLogInfoEnabled
operator|=
name|l4j
operator|.
name|isInfoEnabled
argument_list|()
expr_stmt|;
name|ObjectCache
name|cache
init|=
name|ObjectCacheFactory
operator|.
name|getCache
argument_list|(
name|job
argument_list|)
decl_stmt|;
try|try
block|{
name|jc
operator|=
name|job
expr_stmt|;
name|execContext
operator|=
operator|new
name|ExecMapperContext
argument_list|(
name|jc
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
name|job
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
else|else
block|{
name|Utilities
operator|.
name|setMapWork
argument_list|(
name|job
argument_list|,
name|mrwork
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|mrwork
operator|.
name|getVectorMode
argument_list|()
condition|)
block|{
name|mo
operator|=
operator|new
name|VectorMapOperator
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|mo
operator|=
operator|new
name|MapOperator
argument_list|()
expr_stmt|;
block|}
name|mo
operator|.
name|setConf
argument_list|(
name|mrwork
argument_list|)
expr_stmt|;
comment|// If the current thread's IOContext is not initialized (because it's reading from a
comment|// cached input HadoopRDD), copy from the saved result.
name|IOContext
name|ioContext
init|=
name|IOContext
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|ioContext
operator|.
name|getInputPath
argument_list|()
operator|==
literal|null
condition|)
block|{
name|IOContext
operator|.
name|copy
argument_list|(
name|ioContext
argument_list|,
name|IOContext
operator|.
name|getMap
argument_list|()
operator|.
name|get
argument_list|(
name|SparkUtilities
operator|.
name|MAP_IO_CONTEXT
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// initialize map operator
name|mo
operator|.
name|setChildren
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|info
argument_list|(
name|mo
operator|.
name|dump
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// initialize map local work
name|localWork
operator|=
name|mrwork
operator|.
name|getMapLocalWork
argument_list|()
expr_stmt|;
name|execContext
operator|.
name|setLocalWork
argument_list|(
name|localWork
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
name|jc
argument_list|)
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
name|mo
operator|.
name|setExecContext
argument_list|(
name|execContext
argument_list|)
expr_stmt|;
name|mo
operator|.
name|initializeLocalWork
argument_list|(
name|jc
argument_list|)
expr_stmt|;
name|mo
operator|.
name|initialize
argument_list|(
name|jc
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|OperatorUtils
operator|.
name|setChildrenCollector
argument_list|(
name|mo
operator|.
name|getChildOperators
argument_list|()
argument_list|,
name|output
argument_list|)
expr_stmt|;
name|mo
operator|.
name|setReporter
argument_list|(
name|rp
argument_list|)
expr_stmt|;
if|if
condition|(
name|localWork
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|//The following code is for mapjoin
comment|//initialize all the dummy ops
name|l4j
operator|.
name|info
argument_list|(
literal|"Initializing dummy operator"
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
name|dummyOps
init|=
name|localWork
operator|.
name|getDummyParentOp
argument_list|()
decl_stmt|;
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
name|jc
argument_list|,
literal|null
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
specifier|public
name|void
name|processRow
parameter_list|(
name|Object
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|IOException
block|{
comment|// reset the execContext for each new row
name|execContext
operator|.
name|resetRow
argument_list|()
expr_stmt|;
try|try
block|{
comment|// Since there is no concept of a group, we don't invoke
comment|// startGroup/endGroup for a mapper
name|mo
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
name|logMemoryInfo
argument_list|()
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
block|}
annotation|@
name|Override
specifier|public
name|void
name|processRow
parameter_list|(
name|Object
name|key
parameter_list|,
name|Iterator
name|values
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Do not support this method in SparkMapRecordHandler."
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// No row was processed
if|if
condition|(
name|oc
operator|==
literal|null
condition|)
block|{
name|l4j
operator|.
name|trace
argument_list|(
literal|"Close called. no row processed by map."
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
comment|// ideally hadoop should let us know whether map execution failed or not
try|try
block|{
name|mo
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
comment|//for close the local work
if|if
condition|(
name|localWork
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|dummyOps
init|=
name|localWork
operator|.
name|getDummyParentOp
argument_list|()
decl_stmt|;
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
name|rp
argument_list|,
name|jc
argument_list|)
decl_stmt|;
name|mo
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
name|Utilities
operator|.
name|clearWorkMap
argument_list|()
expr_stmt|;
comment|// It's possible that a thread get reused for different queries, so we need to
comment|// reset the input path.
name|IOContext
operator|.
name|get
argument_list|()
operator|.
name|setInputPath
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getDone
parameter_list|()
block|{
return|return
name|mo
operator|.
name|getDone
argument_list|()
return|;
block|}
block|}
end_class

end_unit

