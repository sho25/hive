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
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|LinkedList
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
name|metrics
operator|.
name|common
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
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|metrics
operator|.
name|common
operator|.
name|MetricsConstant
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
name|QueryPlan
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
name|QueryState
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
name|FileSinkOperator
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
name|JoinOperator
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
name|ReduceSinkOperator
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
name|ScriptOperator
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
name|Task
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
name|spark
operator|.
name|Statistic
operator|.
name|SparkStatistic
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
name|Statistic
operator|.
name|SparkStatisticGroup
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
name|Statistic
operator|.
name|SparkStatistics
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
name|session
operator|.
name|SparkSession
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
name|session
operator|.
name|SparkSessionManager
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
name|session
operator|.
name|SparkSessionManagerImpl
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
name|exec
operator|.
name|spark
operator|.
name|status
operator|.
name|SparkJobStatus
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
name|history
operator|.
name|HiveHistory
operator|.
name|Keys
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
name|BaseWork
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
name|hive
operator|.
name|ql
operator|.
name|plan
operator|.
name|ReduceWork
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|plan
operator|.
name|api
operator|.
name|StageType
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
name|session
operator|.
name|SessionState
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
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
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

begin_class
specifier|public
class|class
name|SparkTask
extends|extends
name|Task
argument_list|<
name|SparkWork
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|SparkTask
operator|.
name|class
operator|.
name|getName
argument_list|()
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
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|LogHelper
name|console
init|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|PerfLogger
name|perfLogger
init|=
name|SessionState
operator|.
name|getPerfLogger
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|transient
name|String
name|sparkJobID
decl_stmt|;
specifier|private
specifier|transient
name|SparkStatistics
name|sparkStatistics
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|QueryState
name|queryState
parameter_list|,
name|QueryPlan
name|queryPlan
parameter_list|,
name|DriverContext
name|driverContext
parameter_list|,
name|CompilationOpContext
name|opContext
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|queryState
argument_list|,
name|queryPlan
argument_list|,
name|driverContext
argument_list|,
name|opContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|int
name|rc
init|=
literal|0
decl_stmt|;
name|SparkSession
name|sparkSession
init|=
literal|null
decl_stmt|;
name|SparkSessionManager
name|sparkSessionManager
init|=
literal|null
decl_stmt|;
try|try
block|{
name|printConfigInfo
argument_list|()
expr_stmt|;
name|sparkSessionManager
operator|=
name|SparkSessionManagerImpl
operator|.
name|getInstance
argument_list|()
expr_stmt|;
name|sparkSession
operator|=
name|SparkUtilities
operator|.
name|getSparkSession
argument_list|(
name|conf
argument_list|,
name|sparkSessionManager
argument_list|)
expr_stmt|;
name|SparkWork
name|sparkWork
init|=
name|getWork
argument_list|()
decl_stmt|;
name|sparkWork
operator|.
name|setRequiredCounterPrefix
argument_list|(
name|getOperatorCounters
argument_list|()
argument_list|)
expr_stmt|;
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_SUBMIT_JOB
argument_list|)
expr_stmt|;
name|SparkJobRef
name|jobRef
init|=
name|sparkSession
operator|.
name|submit
argument_list|(
name|driverContext
argument_list|,
name|sparkWork
argument_list|)
decl_stmt|;
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_SUBMIT_JOB
argument_list|)
expr_stmt|;
name|addToHistory
argument_list|(
name|jobRef
argument_list|)
expr_stmt|;
name|sparkJobID
operator|=
name|jobRef
operator|.
name|getJobId
argument_list|()
expr_stmt|;
name|this
operator|.
name|jobID
operator|=
name|jobRef
operator|.
name|getSparkJobStatus
argument_list|()
operator|.
name|getAppID
argument_list|()
expr_stmt|;
name|rc
operator|=
name|jobRef
operator|.
name|monitorJob
argument_list|()
expr_stmt|;
name|SparkJobStatus
name|sparkJobStatus
init|=
name|jobRef
operator|.
name|getSparkJobStatus
argument_list|()
decl_stmt|;
if|if
condition|(
name|rc
operator|==
literal|0
condition|)
block|{
name|sparkStatistics
operator|=
name|sparkJobStatus
operator|.
name|getSparkStatistics
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
operator|&&
name|sparkStatistics
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"=====Spark Job[%s] statistics====="
argument_list|,
name|jobRef
operator|.
name|getJobId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|logSparkStatistic
argument_list|(
name|sparkStatistics
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Execution completed successfully"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|rc
operator|==
literal|2
condition|)
block|{
comment|// Cancel job if the monitor found job submission timeout.
comment|// TODO: If the timeout is because of lack of resources in the cluster, we should
comment|// ideally also cancel the app request here. But w/o facilities from Spark or YARN,
comment|// it's difficult to do it on hive side alone. See HIVE-12650.
name|jobRef
operator|.
name|cancelJob
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|jobID
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|jobID
operator|=
name|sparkJobStatus
operator|.
name|getAppID
argument_list|()
expr_stmt|;
block|}
name|sparkJobStatus
operator|.
name|cleanup
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Failed to execute spark task, with exception '"
operator|+
name|Utilities
operator|.
name|getNameMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"'"
decl_stmt|;
comment|// Has to use full name to make sure it does not conflict with
comment|// org.apache.commons.lang.StringUtils
name|console
operator|.
name|printError
argument_list|(
name|msg
argument_list|,
literal|"\n"
operator|+
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|rc
operator|=
literal|1
expr_stmt|;
block|}
finally|finally
block|{
name|Utilities
operator|.
name|clearWork
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|sparkSession
operator|!=
literal|null
operator|&&
name|sparkSessionManager
operator|!=
literal|null
condition|)
block|{
name|rc
operator|=
name|close
argument_list|(
name|rc
argument_list|)
expr_stmt|;
try|try
block|{
name|sparkSessionManager
operator|.
name|returnSession
argument_list|(
name|sparkSession
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to return the session to SessionManager"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|rc
return|;
block|}
specifier|private
name|void
name|addToHistory
parameter_list|(
name|SparkJobRef
name|jobRef
parameter_list|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Starting Spark Job = "
operator|+
name|jobRef
operator|.
name|getJobId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveHistory
argument_list|()
operator|.
name|setQueryProperty
argument_list|(
name|queryState
operator|.
name|getQueryId
argument_list|()
argument_list|,
name|Keys
operator|.
name|SPARK_JOB_ID
argument_list|,
name|jobRef
operator|.
name|getJobId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|logSparkStatistic
parameter_list|(
name|SparkStatistics
name|sparkStatistic
parameter_list|)
block|{
name|Iterator
argument_list|<
name|SparkStatisticGroup
argument_list|>
name|groupIterator
init|=
name|sparkStatistic
operator|.
name|getStatisticGroups
argument_list|()
decl_stmt|;
while|while
condition|(
name|groupIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|SparkStatisticGroup
name|group
init|=
name|groupIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|group
operator|.
name|getGroupName
argument_list|()
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|SparkStatistic
argument_list|>
name|statisticIterator
init|=
name|group
operator|.
name|getStatistics
argument_list|()
decl_stmt|;
while|while
condition|(
name|statisticIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|SparkStatistic
name|statistic
init|=
name|statisticIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"\t"
operator|+
name|statistic
operator|.
name|getName
argument_list|()
operator|+
literal|": "
operator|+
name|statistic
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Close will move the temp files into the right place for the fetch    * task. If the job has failed it will clean up the files.    */
specifier|private
name|int
name|close
parameter_list|(
name|int
name|rc
parameter_list|)
block|{
try|try
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|ws
init|=
name|work
operator|.
name|getAllWork
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|ws
control|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|w
operator|.
name|getAllOperators
argument_list|()
control|)
block|{
name|op
operator|.
name|jobClose
argument_list|(
name|conf
argument_list|,
name|rc
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// jobClose needs to execute successfully otherwise fail task
if|if
condition|(
name|rc
operator|==
literal|0
condition|)
block|{
name|rc
operator|=
literal|3
expr_stmt|;
name|String
name|mesg
init|=
literal|"Job Commit failed with exception '"
operator|+
name|Utilities
operator|.
name|getNameMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"'"
decl_stmt|;
name|console
operator|.
name|printError
argument_list|(
name|mesg
argument_list|,
literal|"\n"
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|rc
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateTaskMetrics
parameter_list|(
name|Metrics
name|metrics
parameter_list|)
block|{
name|metrics
operator|.
name|incrementCounter
argument_list|(
name|MetricsConstant
operator|.
name|HIVE_SPARK_TASKS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isMapRedTask
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|MAPRED
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"SPARK"
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|MapWork
argument_list|>
name|getMapWork
parameter_list|()
block|{
name|List
argument_list|<
name|MapWork
argument_list|>
name|result
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|getWork
argument_list|()
operator|.
name|getRoots
argument_list|()
control|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|(
name|MapWork
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|getReducer
parameter_list|(
name|MapWork
name|mapWork
parameter_list|)
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|children
init|=
name|getWork
argument_list|()
operator|.
name|getChildren
argument_list|(
name|mapWork
argument_list|)
decl_stmt|;
if|if
condition|(
name|children
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|ReduceWork
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|(
operator|(
name|ReduceWork
operator|)
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|getReducer
argument_list|()
return|;
block|}
specifier|public
name|String
name|getSparkJobID
parameter_list|()
block|{
return|return
name|sparkJobID
return|;
block|}
specifier|public
name|SparkStatistics
name|getSparkStatistics
parameter_list|()
block|{
return|return
name|sparkStatistics
return|;
block|}
comment|/**    * Set the number of reducers for the spark work.    */
specifier|private
name|void
name|printConfigInfo
parameter_list|()
throws|throws
name|IOException
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"In order to change the average load for a reducer (in bytes):"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"  set "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|BYTESPERREDUCER
operator|.
name|varname
operator|+
literal|"=<number>"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"In order to limit the maximum number of reducers:"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"  set "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAXREDUCERS
operator|.
name|varname
operator|+
literal|"=<number>"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"In order to set a constant number of reducers:"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"  set "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPNUMREDUCERS
operator|+
literal|"=<number>"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getOperatorCounters
parameter_list|()
block|{
name|String
name|groupName
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECOUNTERGROUP
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|counters
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|hiveCounters
init|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|counters
operator|.
name|put
argument_list|(
name|groupName
argument_list|,
name|hiveCounters
argument_list|)
expr_stmt|;
name|hiveCounters
operator|.
name|add
argument_list|(
name|Operator
operator|.
name|HIVECOUNTERCREATEDFILES
argument_list|)
expr_stmt|;
comment|// MapOperator is out of SparkWork, SparkMapRecordHandler use it to bridge
comment|// Spark transformation and Hive operators in SparkWork.
for|for
control|(
name|MapOperator
operator|.
name|Counter
name|counter
range|:
name|MapOperator
operator|.
name|Counter
operator|.
name|values
argument_list|()
control|)
block|{
name|hiveCounters
operator|.
name|add
argument_list|(
name|counter
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|SparkWork
name|sparkWork
init|=
name|this
operator|.
name|getWork
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|work
range|:
name|sparkWork
operator|.
name|getAllWork
argument_list|()
control|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|operator
range|:
name|work
operator|.
name|getAllOperators
argument_list|()
control|)
block|{
if|if
condition|(
name|operator
operator|instanceof
name|FileSinkOperator
condition|)
block|{
for|for
control|(
name|FileSinkOperator
operator|.
name|Counter
name|counter
range|:
name|FileSinkOperator
operator|.
name|Counter
operator|.
name|values
argument_list|()
control|)
block|{
name|hiveCounters
operator|.
name|add
argument_list|(
operator|(
operator|(
name|FileSinkOperator
operator|)
name|operator
operator|)
operator|.
name|getCounterName
argument_list|(
name|counter
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|operator
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
for|for
control|(
name|ReduceSinkOperator
operator|.
name|Counter
name|counter
range|:
name|ReduceSinkOperator
operator|.
name|Counter
operator|.
name|values
argument_list|()
control|)
block|{
name|hiveCounters
operator|.
name|add
argument_list|(
operator|(
operator|(
name|ReduceSinkOperator
operator|)
name|operator
operator|)
operator|.
name|getCounterName
argument_list|(
name|counter
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|operator
operator|instanceof
name|ScriptOperator
condition|)
block|{
for|for
control|(
name|ScriptOperator
operator|.
name|Counter
name|counter
range|:
name|ScriptOperator
operator|.
name|Counter
operator|.
name|values
argument_list|()
control|)
block|{
name|hiveCounters
operator|.
name|add
argument_list|(
name|counter
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|operator
operator|instanceof
name|JoinOperator
condition|)
block|{
for|for
control|(
name|JoinOperator
operator|.
name|SkewkeyTableCounter
name|counter
range|:
name|JoinOperator
operator|.
name|SkewkeyTableCounter
operator|.
name|values
argument_list|()
control|)
block|{
name|hiveCounters
operator|.
name|add
argument_list|(
name|counter
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|counters
return|;
block|}
block|}
end_class

end_unit

