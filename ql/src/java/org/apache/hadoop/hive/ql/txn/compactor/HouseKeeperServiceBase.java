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
name|txn
operator|.
name|compactor
package|;
end_package

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
name|HouseKeeperService
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
name|lockmgr
operator|.
name|HiveTxnManager
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
name|lockmgr
operator|.
name|TxnManagerFactory
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
name|ScheduledExecutorService
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|HouseKeeperServiceBase
implements|implements
name|HouseKeeperService
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
name|HouseKeeperServiceBase
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ScheduledExecutorService
name|pool
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|final
name|AtomicInteger
name|isAliveCounter
init|=
operator|new
name|AtomicInteger
argument_list|(
name|Integer
operator|.
name|MIN_VALUE
argument_list|)
decl_stmt|;
specifier|protected
name|HiveConf
name|hiveConf
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|HiveTxnManager
name|mgr
init|=
name|TxnManagerFactory
operator|.
name|getTxnManagerFactory
argument_list|()
operator|.
name|getTxnManager
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|mgr
operator|.
name|supportsAcid
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" not started since "
operator|+
name|mgr
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" does not support Acid."
argument_list|)
expr_stmt|;
return|return;
comment|//there are no transactions in this case
block|}
name|pool
operator|=
name|Executors
operator|.
name|newScheduledThreadPool
argument_list|(
literal|1
argument_list|,
operator|new
name|ThreadFactory
argument_list|()
block|{
specifier|private
specifier|final
name|AtomicInteger
name|threadCounter
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Thread
name|newThread
parameter_list|(
name|Runnable
name|r
parameter_list|)
block|{
return|return
operator|new
name|Thread
argument_list|(
name|r
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"-"
operator|+
name|threadCounter
operator|.
name|getAndIncrement
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|TimeUnit
name|tu
init|=
name|TimeUnit
operator|.
name|MILLISECONDS
decl_stmt|;
name|pool
operator|.
name|scheduleAtFixedRate
argument_list|(
name|getScheduedAction
argument_list|(
name|hiveConf
argument_list|,
name|isAliveCounter
argument_list|)
argument_list|,
name|getStartDelayMs
argument_list|()
argument_list|,
name|getIntervalMs
argument_list|()
argument_list|,
name|tu
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started "
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" with delay/interval = "
operator|+
name|getStartDelayMs
argument_list|()
operator|+
literal|"/"
operator|+
name|getIntervalMs
argument_list|()
operator|+
literal|" "
operator|+
name|tu
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
name|pool
operator|!=
literal|null
operator|&&
operator|!
name|pool
operator|.
name|isShutdown
argument_list|()
condition|)
block|{
name|pool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|pool
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * This is used for testing only.  Each time the housekeeper runs, counter is incremented by 1.    * Starts with {@link java.lang.Integer#MIN_VALUE}    */
annotation|@
name|Override
specifier|public
name|int
name|getIsAliveCounter
parameter_list|()
block|{
return|return
name|isAliveCounter
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * Delay in millis before first run of the task of this service.    */
specifier|protected
specifier|abstract
name|long
name|getStartDelayMs
parameter_list|()
function_decl|;
comment|/**    * Determines how fequently the service is running its task.    */
specifier|protected
specifier|abstract
name|long
name|getIntervalMs
parameter_list|()
function_decl|;
comment|/**    * The actual task implementation.  Must increment the counter on each iteration.    */
specifier|protected
specifier|abstract
name|Runnable
name|getScheduedAction
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|AtomicInteger
name|isAliveCounter
parameter_list|)
function_decl|;
block|}
end_class

end_unit

