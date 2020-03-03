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
name|schq
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertThat
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
name|List
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
name|api
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
name|metastore
operator|.
name|api
operator|.
name|ScheduledQueryKey
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
name|ScheduledQueryPollResponse
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
name|ScheduledQueryProgressInfo
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
name|DriverFactory
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
name|IDriver
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
name|FetchTask
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
name|ParseException
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
name|scheduled
operator|.
name|IScheduledQueryMaintenanceService
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
name|scheduled
operator|.
name|ScheduledQueryExecutionContext
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
name|scheduled
operator|.
name|ScheduledQueryExecutionService
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
name|hive
operator|.
name|testutils
operator|.
name|HiveTestEnvSetup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestRule
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
name|ThreadFactoryBuilder
import|;
end_import

begin_class
specifier|public
class|class
name|TestScheduledQueryService
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
name|HiveTestEnvSetup
name|env_setup
init|=
operator|new
name|HiveTestEnvSetup
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestRule
name|methodRule
init|=
name|env_setup
operator|.
name|getMethodRule
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|env_setup
operator|.
name|getTestCtx
argument_list|()
operator|.
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SCHEDULED_QUERIES_EXECUTOR_IDLE_SLEEP_TIME
argument_list|,
literal|"1s"
argument_list|)
expr_stmt|;
name|env_setup
operator|.
name|getTestCtx
argument_list|()
operator|.
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SCHEDULED_QUERIES_EXECUTOR_PROGRESS_REPORT_INTERVAL
argument_list|,
literal|"1s"
argument_list|)
expr_stmt|;
name|IDriver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|dropTables
argument_list|(
name|driver
argument_list|)
expr_stmt|;
name|String
name|cmds
index|[]
init|=
block|{
comment|// @formatter:off
literal|"create table tu(c int)"
block|,
comment|// @formatter:on
block|}
decl_stmt|;
for|for
control|(
name|String
name|cmd
range|:
name|cmds
control|)
block|{
name|driver
operator|.
name|run
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|IDriver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|dropTables
argument_list|(
name|driver
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|dropTables
parameter_list|(
name|IDriver
name|driver
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|tables
index|[]
init|=
block|{
literal|"tu"
block|}
decl_stmt|;
for|for
control|(
name|String
name|t
range|:
name|tables
control|)
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"drop table if exists "
operator|+
name|t
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|int
name|getNumRowsReturned
parameter_list|(
name|IDriver
name|driver
parameter_list|,
name|String
name|query
parameter_list|)
throws|throws
name|Exception
block|{
name|driver
operator|.
name|run
argument_list|(
name|query
argument_list|)
expr_stmt|;
name|FetchTask
name|ft
init|=
name|driver
operator|.
name|getFetchTask
argument_list|()
decl_stmt|;
name|List
name|res
init|=
operator|new
name|ArrayList
argument_list|()
decl_stmt|;
if|if
condition|(
name|ft
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
name|ft
operator|.
name|fetch
argument_list|(
name|res
argument_list|)
expr_stmt|;
return|return
name|res
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|MockScheduledQueryService
implements|implements
name|IScheduledQueryMaintenanceService
block|{
comment|// Use notify/wait on this object to indicate when the scheduled query has finished executing.
name|Object
name|notifier
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
name|int
name|id
init|=
literal|0
decl_stmt|;
specifier|private
name|String
name|stmt
decl_stmt|;
name|ScheduledQueryProgressInfo
name|lastProgressInfo
decl_stmt|;
specifier|public
name|MockScheduledQueryService
parameter_list|(
name|String
name|string
parameter_list|)
block|{
name|stmt
operator|=
name|string
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ScheduledQueryPollResponse
name|scheduledQueryPoll
parameter_list|()
block|{
name|ScheduledQueryPollResponse
name|r
init|=
operator|new
name|ScheduledQueryPollResponse
argument_list|()
decl_stmt|;
name|r
operator|.
name|setExecutionId
argument_list|(
name|id
operator|++
argument_list|)
expr_stmt|;
name|r
operator|.
name|setQuery
argument_list|(
name|stmt
argument_list|)
expr_stmt|;
name|r
operator|.
name|setScheduleKey
argument_list|(
operator|new
name|ScheduledQueryKey
argument_list|(
literal|"sch1"
argument_list|,
name|getClusterNamespace
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|id
operator|==
literal|1
condition|)
block|{
return|return
name|r
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|scheduledQueryProgress
parameter_list|(
name|ScheduledQueryProgressInfo
name|info
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"%d, state: %s, error: %s"
argument_list|,
name|info
operator|.
name|getScheduledExecutionId
argument_list|()
argument_list|,
name|info
operator|.
name|getState
argument_list|()
argument_list|,
name|info
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|lastProgressInfo
operator|=
name|info
expr_stmt|;
if|if
condition|(
name|info
operator|.
name|getState
argument_list|()
operator|==
name|QueryState
operator|.
name|FINISHED
operator|||
name|info
operator|.
name|getState
argument_list|()
operator|==
name|QueryState
operator|.
name|FAILED
condition|)
block|{
comment|// Query is done, notify any waiters
synchronized|synchronized
init|(
name|notifier
init|)
block|{
name|notifier
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getClusterNamespace
parameter_list|()
block|{
return|return
literal|"default"
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScheduledQueryExecution
parameter_list|()
throws|throws
name|ParseException
throws|,
name|Exception
block|{
name|IDriver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|ExecutorService
name|executor
init|=
name|Executors
operator|.
name|newCachedThreadPool
argument_list|(
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"SchQ %d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|HiveConf
name|conf
init|=
name|env_setup
operator|.
name|getTestCtx
argument_list|()
operator|.
name|hiveConf
decl_stmt|;
name|MockScheduledQueryService
name|qService
init|=
operator|new
name|MockScheduledQueryService
argument_list|(
literal|"insert into tu values(1),(2),(3),(4),(5)"
argument_list|)
decl_stmt|;
name|ScheduledQueryExecutionContext
name|ctx
init|=
operator|new
name|ScheduledQueryExecutionContext
argument_list|(
name|executor
argument_list|,
name|conf
argument_list|,
name|qService
argument_list|)
decl_stmt|;
try|try
init|(
name|ScheduledQueryExecutionService
name|sQ
init|=
name|ScheduledQueryExecutionService
operator|.
name|startScheduledQueryExecutorService
argument_list|(
name|ctx
argument_list|)
init|)
block|{
comment|// Wait for the scheduled query to finish. Hopefully 30 seconds should be more than enough.
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|logInfo
argument_list|(
literal|"Waiting for query execution to finish ..."
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|qService
operator|.
name|notifier
init|)
block|{
name|qService
operator|.
name|notifier
operator|.
name|wait
argument_list|(
literal|30000
argument_list|)
expr_stmt|;
block|}
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|logInfo
argument_list|(
literal|"Done waiting for query execution!"
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|qService
operator|.
name|lastProgressInfo
operator|.
name|isSetExecutorQueryId
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|qService
operator|.
name|lastProgressInfo
operator|.
name|getExecutorQueryId
argument_list|()
argument_list|,
name|Matchers
operator|.
name|containsString
argument_list|(
name|ctx
operator|.
name|executorHostName
operator|+
literal|"/"
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|nr
init|=
name|getNumRowsReturned
argument_list|(
name|driver
argument_list|,
literal|"select 1 from tu"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|nr
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|IDriver
name|createDriver
parameter_list|()
block|{
name|HiveConf
name|conf
init|=
name|env_setup
operator|.
name|getTestCtx
argument_list|()
operator|.
name|hiveConf
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|IDriver
name|driver
init|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|conf
argument_list|)
decl_stmt|;
return|return
name|driver
return|;
block|}
block|}
end_class

end_unit

