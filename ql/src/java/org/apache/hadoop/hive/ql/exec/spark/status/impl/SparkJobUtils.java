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
operator|.
name|status
operator|.
name|impl
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
operator|.
name|metrics
operator|.
name|Metrics
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
name|spark
operator|.
name|client
operator|.
name|metrics
operator|.
name|ShuffleReadMetrics
import|;
end_import

begin_class
specifier|public
class|class
name|SparkJobUtils
block|{
specifier|private
specifier|final
specifier|static
name|String
name|EXECUTOR_DESERIALIZE_TIME
init|=
literal|"ExecutorDeserializeTime"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|EXECUTOR_RUN_TIME
init|=
literal|"ExecutorRunTime"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|RESULT_SIZE
init|=
literal|"ResultSize"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|JVM_GC_TIME
init|=
literal|"JvmGCTime"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|RESULT_SERIALIZATION_TIME
init|=
literal|"ResultSerializationTime"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|MEMORY_BYTES_SPLIED
init|=
literal|"MemoryBytesSpilled"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|DISK_BYTES_SPLIED
init|=
literal|"DiskBytesSpilled"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|BYTES_READ
init|=
literal|"BytesRead"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|REMOTE_BLOCKS_FETCHED
init|=
literal|"RemoteBlocksFetched"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|LOCAL_BLOCKS_FETCHED
init|=
literal|"LocalBlocksFetched"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|TOTAL_BLOCKS_FETCHED
init|=
literal|"TotalBlocksFetched"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|FETCH_WAIT_TIME
init|=
literal|"FetchWaitTime"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|REMOTE_BYTES_READ
init|=
literal|"RemoteBytesRead"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|SHUFFLE_BYTES_WRITTEN
init|=
literal|"ShuffleBytesWritten"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|SHUFFLE_WRITE_TIME
init|=
literal|"ShuffleWriteTime"
decl_stmt|;
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|collectMetrics
parameter_list|(
name|Metrics
name|allMetrics
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|results
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|results
operator|.
name|put
argument_list|(
name|EXECUTOR_DESERIALIZE_TIME
argument_list|,
name|allMetrics
operator|.
name|executorDeserializeTime
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|EXECUTOR_RUN_TIME
argument_list|,
name|allMetrics
operator|.
name|executorRunTime
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|RESULT_SIZE
argument_list|,
name|allMetrics
operator|.
name|resultSize
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|JVM_GC_TIME
argument_list|,
name|allMetrics
operator|.
name|jvmGCTime
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|RESULT_SERIALIZATION_TIME
argument_list|,
name|allMetrics
operator|.
name|resultSerializationTime
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|MEMORY_BYTES_SPLIED
argument_list|,
name|allMetrics
operator|.
name|memoryBytesSpilled
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|DISK_BYTES_SPLIED
argument_list|,
name|allMetrics
operator|.
name|diskBytesSpilled
argument_list|)
expr_stmt|;
if|if
condition|(
name|allMetrics
operator|.
name|inputMetrics
operator|!=
literal|null
condition|)
block|{
name|results
operator|.
name|put
argument_list|(
name|BYTES_READ
argument_list|,
name|allMetrics
operator|.
name|inputMetrics
operator|.
name|bytesRead
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|allMetrics
operator|.
name|shuffleReadMetrics
operator|!=
literal|null
condition|)
block|{
name|ShuffleReadMetrics
name|shuffleReadMetrics
init|=
name|allMetrics
operator|.
name|shuffleReadMetrics
decl_stmt|;
name|long
name|rbf
init|=
name|shuffleReadMetrics
operator|.
name|remoteBlocksFetched
decl_stmt|;
name|long
name|lbf
init|=
name|shuffleReadMetrics
operator|.
name|localBlocksFetched
decl_stmt|;
name|results
operator|.
name|put
argument_list|(
name|REMOTE_BLOCKS_FETCHED
argument_list|,
name|rbf
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|LOCAL_BLOCKS_FETCHED
argument_list|,
name|lbf
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|TOTAL_BLOCKS_FETCHED
argument_list|,
name|rbf
operator|+
name|lbf
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|FETCH_WAIT_TIME
argument_list|,
name|shuffleReadMetrics
operator|.
name|fetchWaitTime
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|REMOTE_BYTES_READ
argument_list|,
name|shuffleReadMetrics
operator|.
name|remoteBytesRead
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|allMetrics
operator|.
name|shuffleWriteMetrics
operator|!=
literal|null
condition|)
block|{
name|results
operator|.
name|put
argument_list|(
name|SHUFFLE_BYTES_WRITTEN
argument_list|,
name|allMetrics
operator|.
name|shuffleWriteMetrics
operator|.
name|shuffleBytesWritten
argument_list|)
expr_stmt|;
name|results
operator|.
name|put
argument_list|(
name|SHUFFLE_WRITE_TIME
argument_list|,
name|allMetrics
operator|.
name|shuffleWriteMetrics
operator|.
name|shuffleWriteTime
argument_list|)
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
block|}
end_class

end_unit

