begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Statistic
package|;
end_package

begin_comment
comment|/**  * A collection of names that define different {@link SparkStatistic} objects.  */
end_comment

begin_class
specifier|public
class|class
name|SparkStatisticsNames
block|{
specifier|public
specifier|static
specifier|final
name|String
name|EXECUTOR_DESERIALIZE_TIME
init|=
literal|"ExecutorDeserializeTime"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXECUTOR_DESERIALIZE_CPU_TIME
init|=
literal|"ExecutorDeserializeCpuTime"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXECUTOR_RUN_TIME
init|=
literal|"ExecutorRunTime"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXECUTOR_CPU_TIME
init|=
literal|"ExecutorCpuTime"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RESULT_SIZE
init|=
literal|"ResultSize"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JVM_GC_TIME
init|=
literal|"JvmGCTime"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RESULT_SERIALIZATION_TIME
init|=
literal|"ResultSerializationTime"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MEMORY_BYTES_SPILLED
init|=
literal|"MemoryBytesSpilled"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DISK_BYTES_SPILLED
init|=
literal|"DiskBytesSpilled"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BYTES_READ
init|=
literal|"BytesRead"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REMOTE_BLOCKS_FETCHED
init|=
literal|"RemoteBlocksFetched"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LOCAL_BLOCKS_FETCHED
init|=
literal|"LocalBlocksFetched"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TOTAL_BLOCKS_FETCHED
init|=
literal|"TotalBlocksFetched"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FETCH_WAIT_TIME
init|=
literal|"FetchWaitTime"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REMOTE_BYTES_READ
init|=
literal|"RemoteBytesRead"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SHUFFLE_BYTES_WRITTEN
init|=
literal|"ShuffleBytesWritten"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SHUFFLE_WRITE_TIME
init|=
literal|"ShuffleWriteTime"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TASK_DURATION_TIME
init|=
literal|"TaskDurationTime"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SPARK_GROUP_NAME
init|=
literal|"SPARK"
decl_stmt|;
block|}
end_class

end_unit

