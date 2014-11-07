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
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|Maps
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
name|ContentSummary
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
name|StatsSetupConst
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
name|metastore
operator|.
name|Warehouse
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|StatsTask
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
name|counter
operator|.
name|SparkCounters
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
name|SparkJobMonitor
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
name|Table
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
name|BaseSemanticAnalyzer
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
name|DynamicPartitionCtx
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
name|LoadTableDesc
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
name|StatsWork
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
name|UnionWork
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
name|stats
operator|.
name|StatsFactory
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
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|transient
name|JobConf
name|job
decl_stmt|;
specifier|private
specifier|transient
name|ContentSummary
name|inputSummary
decl_stmt|;
specifier|private
name|SparkCounters
name|sparkCounters
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|QueryPlan
name|queryPlan
parameter_list|,
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|queryPlan
argument_list|,
name|driverContext
argument_list|)
expr_stmt|;
name|job
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|,
name|SparkTask
operator|.
name|class
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
literal|1
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
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getSparkSession
argument_list|()
expr_stmt|;
comment|// Spark configurations are updated close the existing session
if|if
condition|(
name|conf
operator|.
name|getSparkConfigUpdated
argument_list|()
condition|)
block|{
name|sparkSessionManager
operator|.
name|closeSession
argument_list|(
name|sparkSession
argument_list|)
expr_stmt|;
name|sparkSession
operator|=
literal|null
expr_stmt|;
name|conf
operator|.
name|setSparkConfigUpdated
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|sparkSession
operator|=
name|sparkSessionManager
operator|.
name|getSession
argument_list|(
name|sparkSession
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|setSparkSession
argument_list|(
name|sparkSession
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
name|getCounterPrefixes
argument_list|()
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
name|SparkJobStatus
name|sparkJobStatus
init|=
name|jobRef
operator|.
name|getSparkJobStatus
argument_list|()
decl_stmt|;
name|sparkCounters
operator|=
name|sparkJobStatus
operator|.
name|getCounter
argument_list|()
expr_stmt|;
name|SparkJobMonitor
name|monitor
init|=
operator|new
name|SparkJobMonitor
argument_list|(
name|sparkJobStatus
argument_list|)
decl_stmt|;
name|monitor
operator|.
name|startMonitor
argument_list|()
expr_stmt|;
name|SparkStatistics
name|sparkStatistics
init|=
name|sparkJobStatus
operator|.
name|getSparkStatistics
argument_list|()
decl_stmt|;
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
literal|"=====Spark Job[%d] statistics====="
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
name|sparkJobStatus
operator|.
name|cleanup
argument_list|()
expr_stmt|;
name|rc
operator|=
literal|0
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to execute spark task."
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
finally|finally
block|{
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
comment|/**    * close will move the temp files into the right place for the fetch    * task. If the job has failed it will clean up the files.    */
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
name|SparkWork
name|work
init|=
name|getWork
argument_list|()
decl_stmt|;
comment|// framework expects MapWork instances that have no physical parents (i.e.: union parent is
comment|// fine, broadcast parent isn't)
for|for
control|(
name|BaseWork
name|w
range|:
name|work
operator|.
name|getAllWorkUnsorted
argument_list|()
control|)
block|{
if|if
condition|(
name|w
operator|instanceof
name|MapWork
condition|)
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|parents
init|=
name|work
operator|.
name|getParents
argument_list|(
name|w
argument_list|)
decl_stmt|;
name|boolean
name|candidate
init|=
literal|true
decl_stmt|;
for|for
control|(
name|BaseWork
name|parent
range|:
name|parents
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|parent
operator|instanceof
name|UnionWork
operator|)
condition|)
block|{
name|candidate
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
name|candidate
condition|)
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
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
name|SparkCounters
name|getSparkCounters
parameter_list|()
block|{
return|return
name|sparkCounters
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
name|getCounterPrefixes
parameter_list|()
throws|throws
name|HiveException
throws|,
name|MetaException
block|{
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
name|getOperatorCounters
argument_list|()
decl_stmt|;
name|StatsTask
name|statsTask
init|=
name|getStatsTaskInChildTasks
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|String
name|statsImpl
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
name|HIVESTATSDBCLASS
argument_list|)
decl_stmt|;
comment|// fetch table prefix if SparkTask try to gather table statistics based on counter.
if|if
condition|(
name|statsImpl
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"counter"
argument_list|)
operator|&&
name|statsTask
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|prefixes
init|=
name|getRequiredCounterPrefix
argument_list|(
name|statsTask
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|prefix
range|:
name|prefixes
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|counterGroup
init|=
name|counters
operator|.
name|get
argument_list|(
name|prefix
argument_list|)
decl_stmt|;
if|if
condition|(
name|counterGroup
operator|==
literal|null
condition|)
block|{
name|counterGroup
operator|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|counters
operator|.
name|put
argument_list|(
name|prefix
argument_list|,
name|counterGroup
argument_list|)
expr_stmt|;
block|}
name|counterGroup
operator|.
name|add
argument_list|(
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
expr_stmt|;
name|counterGroup
operator|.
name|add
argument_list|(
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|counters
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getRequiredCounterPrefix
parameter_list|(
name|StatsTask
name|statsTask
parameter_list|)
throws|throws
name|HiveException
throws|,
name|MetaException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|prefixs
init|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|StatsWork
name|statsWork
init|=
name|statsTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|String
name|tablePrefix
init|=
name|getTablePrefix
argument_list|(
name|statsWork
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
name|getPartitionsList
argument_list|(
name|statsWork
argument_list|)
decl_stmt|;
name|int
name|maxPrefixLength
init|=
name|StatsFactory
operator|.
name|getMaxPrefixLength
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|partitions
operator|==
literal|null
condition|)
block|{
name|prefixs
operator|.
name|add
argument_list|(
name|Utilities
operator|.
name|getHashedStatsPrefix
argument_list|(
name|tablePrefix
argument_list|,
name|maxPrefixLength
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|Partition
name|partition
range|:
name|partitions
control|)
block|{
name|String
name|prefixWithPartition
init|=
name|Utilities
operator|.
name|join
argument_list|(
name|tablePrefix
argument_list|,
name|Warehouse
operator|.
name|makePartPath
argument_list|(
name|partition
operator|.
name|getSpec
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|prefixs
operator|.
name|add
argument_list|(
name|Utilities
operator|.
name|getHashedStatsPrefix
argument_list|(
name|prefixWithPartition
argument_list|,
name|maxPrefixLength
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|prefixs
return|;
block|}
specifier|private
name|String
name|getTablePrefix
parameter_list|(
name|StatsWork
name|work
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
name|tableName
decl_stmt|;
if|if
condition|(
name|work
operator|.
name|getLoadTableDesc
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tableName
operator|=
name|work
operator|.
name|getLoadTableDesc
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|work
operator|.
name|getTableSpecs
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tableName
operator|=
name|work
operator|.
name|getTableSpecs
argument_list|()
operator|.
name|tableName
expr_stmt|;
block|}
else|else
block|{
name|tableName
operator|=
name|work
operator|.
name|getLoadFileDesc
argument_list|()
operator|.
name|getDestinationCreateTable
argument_list|()
expr_stmt|;
block|}
name|Table
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
name|table
operator|=
name|db
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get table:"
operator|+
name|tableName
argument_list|)
expr_stmt|;
comment|// For CTAS query, table does not exist in this period, just use table name as prefix.
return|return
name|tableName
operator|.
name|toLowerCase
argument_list|()
return|;
block|}
return|return
name|table
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|table
operator|.
name|getTableName
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|StatsTask
name|getStatsTaskInChildTasks
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|rootTask
parameter_list|)
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|childTasks
init|=
name|rootTask
operator|.
name|getChildTasks
argument_list|()
decl_stmt|;
if|if
condition|(
name|childTasks
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
range|:
name|childTasks
control|)
block|{
if|if
condition|(
name|task
operator|instanceof
name|StatsTask
condition|)
block|{
return|return
operator|(
name|StatsTask
operator|)
name|task
return|;
block|}
else|else
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|childTask
init|=
name|getStatsTaskInChildTasks
argument_list|(
name|task
argument_list|)
decl_stmt|;
if|if
condition|(
name|childTask
operator|instanceof
name|StatsTask
condition|)
block|{
return|return
operator|(
name|StatsTask
operator|)
name|childTask
return|;
block|}
else|else
block|{
continue|continue;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|List
argument_list|<
name|Partition
argument_list|>
name|getPartitionsList
parameter_list|(
name|StatsWork
name|work
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|work
operator|.
name|getLoadFileDesc
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
literal|null
return|;
comment|//we are in CTAS, so we know there are no partitions
block|}
name|Table
name|table
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Partition
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|work
operator|.
name|getTableSpecs
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// ANALYZE command
name|BaseSemanticAnalyzer
operator|.
name|tableSpec
name|tblSpec
init|=
name|work
operator|.
name|getTableSpecs
argument_list|()
decl_stmt|;
name|table
operator|=
name|tblSpec
operator|.
name|tableHandle
expr_stmt|;
if|if
condition|(
operator|!
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// get all partitions that matches with the partition spec
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
name|tblSpec
operator|.
name|partitions
decl_stmt|;
if|if
condition|(
name|partitions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Partition
name|partn
range|:
name|partitions
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|partn
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|work
operator|.
name|getLoadTableDesc
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// INSERT OVERWRITE command
name|LoadTableDesc
name|tbd
init|=
name|work
operator|.
name|getLoadTableDesc
argument_list|()
decl_stmt|;
name|table
operator|=
name|db
operator|.
name|getTable
argument_list|(
name|tbd
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|DynamicPartitionCtx
name|dpCtx
init|=
name|tbd
operator|.
name|getDPCtx
argument_list|()
decl_stmt|;
if|if
condition|(
name|dpCtx
operator|!=
literal|null
operator|&&
name|dpCtx
operator|.
name|getNumDPCols
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// dynamic partitions
comment|// we could not get dynamic partition information before SparkTask execution.
block|}
else|else
block|{
comment|// static partition
name|Partition
name|partn
init|=
name|db
operator|.
name|getPartition
argument_list|(
name|table
argument_list|,
name|tbd
operator|.
name|getPartitionSpec
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|partn
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|list
return|;
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
name|MapOperator
condition|)
block|{
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
block|}
elseif|else
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

