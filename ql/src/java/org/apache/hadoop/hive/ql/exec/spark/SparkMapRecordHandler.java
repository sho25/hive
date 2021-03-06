begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|PartitionDesc
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
name|ql
operator|.
name|CompilationOpContext
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
name|AbstractMapOperator
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
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SparkMapRecordHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|AbstractMapOperator
name|mo
decl_stmt|;
specifier|private
name|MapredLocalWork
name|localWork
init|=
literal|null
decl_stmt|;
specifier|private
name|ExecMapperContext
name|execContext
decl_stmt|;
annotation|@
name|Override
specifier|public
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|void
name|init
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|OutputCollector
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|output
parameter_list|,
name|Reporter
name|reporter
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
name|SPARK_INIT_OPERATORS
argument_list|)
expr_stmt|;
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
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|job
argument_list|)
decl_stmt|;
for|for
control|(
name|PartitionDesc
name|part
range|:
name|mrwork
operator|.
name|getAliasToPartnInfo
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|TableDesc
name|tableDesc
init|=
name|part
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
name|Utilities
operator|.
name|copyJobSecretToTableProperties
argument_list|(
name|tableDesc
argument_list|)
expr_stmt|;
block|}
name|CompilationOpContext
name|runtimeCtx
init|=
operator|new
name|CompilationOpContext
argument_list|()
decl_stmt|;
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
argument_list|(
name|runtimeCtx
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|mo
operator|=
operator|new
name|MapOperator
argument_list|(
name|runtimeCtx
argument_list|)
expr_stmt|;
block|}
name|mo
operator|.
name|setConf
argument_list|(
name|mrwork
argument_list|)
expr_stmt|;
comment|// initialize map operator
name|mo
operator|.
name|initialize
argument_list|(
name|jc
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|mo
operator|.
name|setChildren
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|LOG
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
name|getMapRedLocalWork
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
name|passExecContext
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
name|initializeMapOperator
argument_list|(
name|jc
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
name|LOG
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
literal|"Map operator initialization failed: "
operator|+
name|e
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
name|SPARK_INIT_OPERATORS
argument_list|)
expr_stmt|;
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
if|if
condition|(
operator|!
name|anyRow
condition|)
block|{
name|OperatorUtils
operator|.
name|setChildrenCollector
argument_list|(
name|mo
operator|.
name|getChildOperators
argument_list|()
argument_list|,
name|oc
argument_list|)
expr_stmt|;
name|anyRow
operator|=
literal|true
expr_stmt|;
block|}
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
name|incrementRowNumber
argument_list|()
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
name|Utilities
operator|.
name|setMapWork
argument_list|(
name|jc
argument_list|,
literal|null
argument_list|)
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
name|String
name|msg
init|=
literal|"Error processing row: "
operator|+
name|e
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|msg
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
parameter_list|<
name|E
parameter_list|>
name|void
name|processRow
parameter_list|(
name|Object
name|key
parameter_list|,
name|Iterator
argument_list|<
name|E
argument_list|>
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
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// No row was processed
if|if
condition|(
operator|!
name|anyRow
condition|)
block|{
name|LOG
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
name|String
name|msg
init|=
literal|"Hit error while closing operators - failing tree: "
operator|+
name|e
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|msg
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
argument_list|(
name|jc
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

