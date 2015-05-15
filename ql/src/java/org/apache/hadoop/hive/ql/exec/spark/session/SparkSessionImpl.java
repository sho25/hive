begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|session
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
name|UUID
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
name|common
operator|.
name|ObjectPair
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
name|DriverContext
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
name|spark
operator|.
name|HiveSparkClient
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
name|spark
operator|.
name|HiveSparkClientFactory
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
name|spark
operator|.
name|status
operator|.
name|SparkJobRef
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
name|SparkWork
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|SparkConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|util
operator|.
name|Utils
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
name|Preconditions
import|;
end_import

begin_class
specifier|public
class|class
name|SparkSessionImpl
implements|implements
name|SparkSession
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SparkSession
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|boolean
name|isOpen
decl_stmt|;
specifier|private
specifier|final
name|String
name|sessionId
decl_stmt|;
specifier|private
name|HiveSparkClient
name|hiveSparkClient
decl_stmt|;
specifier|public
name|SparkSessionImpl
parameter_list|()
block|{
name|sessionId
operator|=
name|makeSessionId
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|open
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|isOpen
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|hiveSparkClient
operator|=
name|HiveSparkClientFactory
operator|.
name|createHiveSparkClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Failed to create spark client."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|SparkJobRef
name|submit
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
throws|throws
name|Exception
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|isOpen
argument_list|,
literal|"Session is not open. Can't submit jobs."
argument_list|)
expr_stmt|;
return|return
name|hiveSparkClient
operator|.
name|execute
argument_list|(
name|driverContext
argument_list|,
name|sparkWork
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectPair
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
name|getMemoryAndCores
parameter_list|()
throws|throws
name|Exception
block|{
name|SparkConf
name|sparkConf
init|=
name|hiveSparkClient
operator|.
name|getSparkConf
argument_list|()
decl_stmt|;
name|int
name|numExecutors
init|=
name|hiveSparkClient
operator|.
name|getExecutorCount
argument_list|()
decl_stmt|;
comment|// at start-up, we may be unable to get number of executors
if|if
condition|(
name|numExecutors
operator|<=
literal|0
condition|)
block|{
return|return
operator|new
name|ObjectPair
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
argument_list|(
operator|-
literal|1L
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
name|int
name|executorMemoryInMB
init|=
name|Utils
operator|.
name|memoryStringToMb
argument_list|(
name|sparkConf
operator|.
name|get
argument_list|(
literal|"spark.executor.memory"
argument_list|,
literal|"512m"
argument_list|)
argument_list|)
decl_stmt|;
name|double
name|memoryFraction
init|=
literal|1.0
operator|-
name|sparkConf
operator|.
name|getDouble
argument_list|(
literal|"spark.storage.memoryFraction"
argument_list|,
literal|0.6
argument_list|)
decl_stmt|;
name|long
name|totalMemory
init|=
call|(
name|long
call|)
argument_list|(
name|numExecutors
operator|*
name|executorMemoryInMB
operator|*
name|memoryFraction
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
decl_stmt|;
name|int
name|totalCores
decl_stmt|;
name|String
name|masterURL
init|=
name|sparkConf
operator|.
name|get
argument_list|(
literal|"spark.master"
argument_list|)
decl_stmt|;
if|if
condition|(
name|masterURL
operator|.
name|startsWith
argument_list|(
literal|"spark"
argument_list|)
condition|)
block|{
name|totalCores
operator|=
name|sparkConf
operator|.
name|contains
argument_list|(
literal|"spark.default.parallelism"
argument_list|)
condition|?
name|sparkConf
operator|.
name|getInt
argument_list|(
literal|"spark.default.parallelism"
argument_list|,
literal|1
argument_list|)
else|:
name|hiveSparkClient
operator|.
name|getDefaultParallelism
argument_list|()
expr_stmt|;
name|totalCores
operator|=
name|Math
operator|.
name|max
argument_list|(
name|totalCores
argument_list|,
name|numExecutors
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|coresPerExecutor
init|=
name|sparkConf
operator|.
name|getInt
argument_list|(
literal|"spark.executor.cores"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|totalCores
operator|=
name|numExecutors
operator|*
name|coresPerExecutor
expr_stmt|;
block|}
name|totalCores
operator|=
name|totalCores
operator|/
name|sparkConf
operator|.
name|getInt
argument_list|(
literal|"spark.task.cpus"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|long
name|memoryPerTaskInBytes
init|=
name|totalMemory
operator|/
name|totalCores
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Spark cluster current has executors: "
operator|+
name|numExecutors
operator|+
literal|", total cores: "
operator|+
name|totalCores
operator|+
literal|", memory per executor: "
operator|+
name|executorMemoryInMB
operator|+
literal|"M, memoryFraction: "
operator|+
name|memoryFraction
argument_list|)
expr_stmt|;
return|return
operator|new
name|ObjectPair
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|memoryPerTaskInBytes
argument_list|)
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|totalCores
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isOpen
parameter_list|()
block|{
return|return
name|isOpen
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getSessionId
parameter_list|()
block|{
return|return
name|sessionId
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|isOpen
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|hiveSparkClient
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|hiveSparkClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to close spark session ("
operator|+
name|sessionId
operator|+
literal|")."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|hiveSparkClient
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
specifier|static
name|String
name|makeSessionId
parameter_list|()
block|{
return|return
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

