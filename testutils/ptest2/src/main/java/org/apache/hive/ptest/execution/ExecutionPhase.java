begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|Collections
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
name|List
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
name|concurrent
operator|.
name|BlockingQueue
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
name|LinkedBlockingQueue
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
name|TimeUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
operator|.
name|conf
operator|.
name|Host
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
operator|.
name|conf
operator|.
name|TestBatch
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
operator|.
name|context
operator|.
name|ExecutionContext
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Supplier
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
name|ImmutableList
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
name|ImmutableMap
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Futures
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
name|util
operator|.
name|concurrent
operator|.
name|ListenableFuture
import|;
end_import

begin_class
specifier|public
class|class
name|ExecutionPhase
extends|extends
name|Phase
block|{
specifier|private
specifier|static
specifier|final
name|long
name|FOUR_HOURS
init|=
literal|4L
operator|*
literal|60L
operator|*
literal|60L
operator|*
literal|1000L
decl_stmt|;
specifier|private
specifier|final
name|ExecutionContext
name|executionContext
decl_stmt|;
specifier|private
specifier|final
name|HostExecutorBuilder
name|hostExecutorBuilder
decl_stmt|;
specifier|private
specifier|final
name|File
name|succeededLogDir
decl_stmt|;
specifier|private
specifier|final
name|File
name|failedLogDir
decl_stmt|;
specifier|private
specifier|final
name|BlockingQueue
argument_list|<
name|TestBatch
argument_list|>
name|parallelWorkQueue
decl_stmt|;
specifier|private
specifier|final
name|BlockingQueue
argument_list|<
name|TestBatch
argument_list|>
name|isolatedWorkQueue
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|executedTests
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|failedTests
decl_stmt|;
specifier|private
specifier|final
name|Supplier
argument_list|<
name|List
argument_list|<
name|TestBatch
argument_list|>
argument_list|>
name|testBatchSupplier
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|TestBatch
argument_list|>
name|failedTestResults
decl_stmt|;
specifier|public
name|ExecutionPhase
parameter_list|(
name|List
argument_list|<
name|HostExecutor
argument_list|>
name|hostExecutors
parameter_list|,
name|ExecutionContext
name|executionContext
parameter_list|,
name|HostExecutorBuilder
name|hostExecutorBuilder
parameter_list|,
name|LocalCommandFactory
name|localCommandFactory
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateDefaults
parameter_list|,
name|File
name|succeededLogDir
parameter_list|,
name|File
name|failedLogDir
parameter_list|,
name|Supplier
argument_list|<
name|List
argument_list|<
name|TestBatch
argument_list|>
argument_list|>
name|testBatchSupplier
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|executedTests
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|failedTests
parameter_list|,
name|Logger
name|logger
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|hostExecutors
argument_list|,
name|localCommandFactory
argument_list|,
name|templateDefaults
argument_list|,
name|logger
argument_list|)
expr_stmt|;
name|this
operator|.
name|executionContext
operator|=
name|executionContext
expr_stmt|;
name|this
operator|.
name|hostExecutorBuilder
operator|=
name|hostExecutorBuilder
expr_stmt|;
name|this
operator|.
name|succeededLogDir
operator|=
name|succeededLogDir
expr_stmt|;
name|this
operator|.
name|failedLogDir
operator|=
name|failedLogDir
expr_stmt|;
name|this
operator|.
name|testBatchSupplier
operator|=
name|testBatchSupplier
expr_stmt|;
name|this
operator|.
name|executedTests
operator|=
name|executedTests
expr_stmt|;
name|this
operator|.
name|failedTests
operator|=
name|failedTests
expr_stmt|;
name|this
operator|.
name|parallelWorkQueue
operator|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|TestBatch
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|isolatedWorkQueue
operator|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|TestBatch
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|failedTestResults
operator|=
name|Collections
operator|.
name|synchronizedSet
argument_list|(
operator|new
name|HashSet
argument_list|<
name|TestBatch
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|()
throws|throws
name|Throwable
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TestBatch
argument_list|>
name|testBatches
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|TestBatch
name|batch
range|:
name|testBatchSupplier
operator|.
name|get
argument_list|()
control|)
block|{
name|testBatches
operator|.
name|add
argument_list|(
name|batch
argument_list|)
expr_stmt|;
if|if
condition|(
name|batch
operator|.
name|isParallel
argument_list|()
condition|)
block|{
name|parallelWorkQueue
operator|.
name|add
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|isolatedWorkQueue
operator|.
name|add
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"ParallelWorkQueueSize={}, IsolatedWorkQueueSize={}"
argument_list|,
name|parallelWorkQueue
operator|.
name|size
argument_list|()
argument_list|,
name|isolatedWorkQueue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
for|for
control|(
name|TestBatch
name|testBatch
range|:
name|parallelWorkQueue
control|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"PBatch: {}"
argument_list|,
name|testBatch
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|TestBatch
name|testBatch
range|:
name|isolatedWorkQueue
control|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"IBatch: {}"
argument_list|,
name|testBatch
argument_list|)
expr_stmt|;
block|}
block|}
try|try
block|{
name|int
name|expectedNumHosts
init|=
name|hostExecutors
operator|.
name|size
argument_list|()
decl_stmt|;
name|initalizeHosts
argument_list|()
expr_stmt|;
do|do
block|{
name|replaceBadHosts
argument_list|(
name|expectedNumHosts
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ListenableFuture
argument_list|<
name|Void
argument_list|>
argument_list|>
name|results
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|HostExecutor
name|hostExecutor
range|:
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|hostExecutors
argument_list|)
control|)
block|{
name|results
operator|.
name|add
argument_list|(
name|hostExecutor
operator|.
name|submitTests
argument_list|(
name|parallelWorkQueue
argument_list|,
name|isolatedWorkQueue
argument_list|,
name|failedTestResults
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Futures
operator|.
name|allAsList
argument_list|(
name|results
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
operator|!
operator|(
name|parallelWorkQueue
operator|.
name|isEmpty
argument_list|()
operator|&&
name|isolatedWorkQueue
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
do|;
for|for
control|(
name|TestBatch
name|batch
range|:
name|testBatches
control|)
block|{
name|File
name|batchLogDir
decl_stmt|;
if|if
condition|(
name|failedTestResults
operator|.
name|contains
argument_list|(
name|batch
argument_list|)
condition|)
block|{
name|batchLogDir
operator|=
operator|new
name|File
argument_list|(
name|failedLogDir
argument_list|,
name|batch
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|batchLogDir
operator|=
operator|new
name|File
argument_list|(
name|succeededLogDir
argument_list|,
name|batch
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|JUnitReportParser
name|parser
init|=
operator|new
name|JUnitReportParser
argument_list|(
name|logger
argument_list|,
name|batchLogDir
argument_list|)
decl_stmt|;
name|executedTests
operator|.
name|addAll
argument_list|(
name|parser
operator|.
name|getExecutedTests
argument_list|()
argument_list|)
expr_stmt|;
name|failedTests
operator|.
name|addAll
argument_list|(
name|parser
operator|.
name|getFailedTests
argument_list|()
argument_list|)
expr_stmt|;
comment|// if the TEST*.xml was not generated or was corrupt, let someone know
if|if
condition|(
name|parser
operator|.
name|getNumAttemptedTests
argument_list|()
operator|==
literal|0
condition|)
block|{
name|failedTests
operator|.
name|add
argument_list|(
name|batch
operator|.
name|getName
argument_list|()
operator|+
literal|" - did not produce a TEST-*.xml file"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|long
name|elapsed
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"PERF: exec phase "
operator|+
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|convert
argument_list|(
name|elapsed
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|+
literal|" minutes"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|replaceBadHosts
parameter_list|(
name|int
name|expectedNumHosts
parameter_list|)
throws|throws
name|Exception
block|{
name|Set
argument_list|<
name|Host
argument_list|>
name|goodHosts
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|HostExecutor
name|hostExecutor
range|:
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|hostExecutors
argument_list|)
control|)
block|{
if|if
condition|(
name|hostExecutor
operator|.
name|isBad
argument_list|()
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Removing host during execution phase: "
operator|+
name|hostExecutor
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|executionContext
operator|.
name|addBadHost
argument_list|(
name|hostExecutor
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|hostExecutors
operator|.
name|remove
argument_list|(
name|hostExecutor
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|goodHosts
operator|.
name|add
argument_list|(
name|hostExecutor
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
name|hostExecutors
operator|.
name|size
argument_list|()
operator|<
name|expectedNumHosts
condition|)
block|{
if|if
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|>
name|FOUR_HOURS
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Waited over fours for hosts, still have only "
operator|+
name|hostExecutors
operator|.
name|size
argument_list|()
operator|+
literal|" hosts out of an expected "
operator|+
name|expectedNumHosts
argument_list|)
throw|;
block|}
name|logger
operator|.
name|warn
argument_list|(
literal|"Only "
operator|+
name|hostExecutors
operator|.
name|size
argument_list|()
operator|+
literal|" hosts out of an expected "
operator|+
name|expectedNumHosts
operator|+
literal|", attempting to replace bad hosts"
argument_list|)
expr_stmt|;
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|sleep
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|executionContext
operator|.
name|replaceBadHosts
argument_list|()
expr_stmt|;
for|for
control|(
name|Host
name|host
range|:
name|executionContext
operator|.
name|getHosts
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|goodHosts
operator|.
name|contains
argument_list|(
name|host
argument_list|)
condition|)
block|{
name|HostExecutor
name|hostExecutor
init|=
name|hostExecutorBuilder
operator|.
name|build
argument_list|(
name|host
argument_list|)
decl_stmt|;
name|initalizeHost
argument_list|(
name|hostExecutor
argument_list|)
expr_stmt|;
if|if
condition|(
name|hostExecutor
operator|.
name|isBad
argument_list|()
condition|)
block|{
name|executionContext
operator|.
name|addBadHost
argument_list|(
name|hostExecutor
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Adding new host during execution phase: "
operator|+
name|host
argument_list|)
expr_stmt|;
name|hostExecutors
operator|.
name|add
argument_list|(
name|hostExecutor
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

