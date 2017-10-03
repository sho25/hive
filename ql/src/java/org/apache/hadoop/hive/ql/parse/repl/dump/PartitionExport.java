begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*   Licensed to the Apache Software Foundation (ASF) under one   or more contributor license agreements.  See the NOTICE file   distributed with this work for additional information   regarding copyright ownership.  The ASF licenses this file   to you under the Apache License, Version 2.0 (the   "License"); you may not use this file except in compliance   with the License.  You may obtain a copy of the License at        http://www.apache.org/licenses/LICENSE-2.0    Unless required by applicable law or agreed to in writing, software   distributed under the License is distributed on an "AS IS" BASIS,   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   See the License for the specific language governing permissions and   limitations under the License.  */
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
name|repl
operator|.
name|dump
package|;
end_package

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
name|ThreadFactoryBuilder
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
name|hooks
operator|.
name|ReadEntity
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
name|Partition
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
name|PartitionIterable
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
name|ReplicationSpec
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
name|repl
operator|.
name|dump
operator|.
name|io
operator|.
name|FileOperations
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ArrayBlockingQueue
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
name|ExecutorService
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
name|Executors
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
name|ThreadFactory
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
import|import static
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
name|repl
operator|.
name|dump
operator|.
name|TableExport
operator|.
name|AuthEntities
import|;
end_import

begin_import
import|import static
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
name|repl
operator|.
name|dump
operator|.
name|TableExport
operator|.
name|Paths
import|;
end_import

begin_comment
comment|/**  * This class manages writing multiple partitions _data files simultaneously.  * it has a blocking queue that stores partitions to be dumped via a producer thread.  * it has a worker thread pool that reads of the queue to perform the various tasks.  */
end_comment

begin_class
class|class
name|PartitionExport
block|{
specifier|private
specifier|final
name|Paths
name|paths
decl_stmt|;
specifier|private
specifier|final
name|PartitionIterable
name|partitionIterable
decl_stmt|;
specifier|private
specifier|final
name|String
name|distCpDoAsUser
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|final
name|int
name|nThreads
decl_stmt|;
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
name|PartitionExport
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|BlockingQueue
argument_list|<
name|Partition
argument_list|>
name|queue
decl_stmt|;
name|PartitionExport
parameter_list|(
name|Paths
name|paths
parameter_list|,
name|PartitionIterable
name|partitionIterable
parameter_list|,
name|String
name|distCpDoAsUser
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|this
operator|.
name|paths
operator|=
name|paths
expr_stmt|;
name|this
operator|.
name|partitionIterable
operator|=
name|partitionIterable
expr_stmt|;
name|this
operator|.
name|distCpDoAsUser
operator|=
name|distCpDoAsUser
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|this
operator|.
name|nThreads
operator|=
name|hiveConf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPL_PARTITIONS_DUMP_PARALLELISM
argument_list|)
expr_stmt|;
name|this
operator|.
name|queue
operator|=
operator|new
name|ArrayBlockingQueue
argument_list|<>
argument_list|(
literal|2
operator|*
name|nThreads
argument_list|)
expr_stmt|;
block|}
name|void
name|write
parameter_list|(
specifier|final
name|ReplicationSpec
name|forReplicationSpec
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|ExecutorService
name|producer
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|producer
operator|.
name|submit
argument_list|(
parameter_list|()
lambda|->
block|{
for|for
control|(
name|Partition
name|partition
range|:
name|partitionIterable
control|)
block|{
try|try
block|{
name|queue
operator|.
name|put
argument_list|(
name|partition
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error while queuing up the partitions for export of data files"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|producer
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|ThreadFactory
name|namingThreadFactory
init|=
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setNameFormat
argument_list|(
literal|"partition-dump-thread-%d"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ExecutorService
name|consumer
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|nThreads
argument_list|,
name|namingThreadFactory
argument_list|)
decl_stmt|;
while|while
condition|(
operator|!
name|producer
operator|.
name|isTerminated
argument_list|()
operator|||
operator|!
name|queue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|/*       This is removed using a poll because there can be a case where there partitions iterator is empty       but because both the producer and consumer are started simultaneously the while loop will execute       because producer is not terminated but it wont produce anything so queue will be empty and then we       should only wait for a specific time before continuing, as the next loop cycle will fail.        */
name|Partition
name|partition
init|=
name|queue
operator|.
name|poll
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
if|if
condition|(
name|partition
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"scheduling partition dump {}"
argument_list|,
name|partition
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|consumer
operator|.
name|submit
argument_list|(
parameter_list|()
lambda|->
block|{
name|String
name|partitionName
init|=
name|partition
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|threadName
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Thread: {}, start partition dump {}"
argument_list|,
name|threadName
argument_list|,
name|partitionName
argument_list|)
expr_stmt|;
name|Path
name|fromPath
init|=
name|partition
operator|.
name|getDataLocation
argument_list|()
decl_stmt|;
try|try
block|{
comment|// this the data copy
name|Path
name|rootDataDumpDir
init|=
name|paths
operator|.
name|partitionExportDir
argument_list|(
name|partitionName
argument_list|)
decl_stmt|;
operator|new
name|FileOperations
argument_list|(
name|fromPath
argument_list|,
name|rootDataDumpDir
argument_list|,
name|distCpDoAsUser
argument_list|,
name|hiveConf
argument_list|)
operator|.
name|export
argument_list|(
name|forReplicationSpec
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Thread: {}, finish partition dump {}"
argument_list|,
name|threadName
argument_list|,
name|partitionName
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
name|RuntimeException
argument_list|(
literal|"Error while export of data files"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|consumer
operator|.
name|shutdown
argument_list|()
expr_stmt|;
comment|// may be drive this via configuration as well.
name|consumer
operator|.
name|awaitTermination
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

