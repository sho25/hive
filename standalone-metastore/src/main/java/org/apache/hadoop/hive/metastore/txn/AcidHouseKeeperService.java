begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
operator|.
name|txn
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
name|conf
operator|.
name|Configuration
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
name|MetastoreTaskThread
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
name|conf
operator|.
name|MetastoreConf
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
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * Performs background tasks for Transaction management in Hive.  * Runs inside Hive Metastore Service.  */
end_comment

begin_class
specifier|public
class|class
name|AcidHouseKeeperService
implements|implements
name|MetastoreTaskThread
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
name|AcidHouseKeeperService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|TxnStore
name|txnHandler
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|configuration
expr_stmt|;
name|txnHandler
operator|=
name|TxnUtils
operator|.
name|getTxnStore
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
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
name|long
name|runFrequency
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|MetastoreConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|TIMEDOUT_TXN_REAPER_INTERVAL
argument_list|,
name|unit
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|TxnStore
operator|.
name|MutexAPI
operator|.
name|LockHandle
name|handle
init|=
literal|null
decl_stmt|;
try|try
block|{
name|handle
operator|=
name|txnHandler
operator|.
name|getMutexAPI
argument_list|()
operator|.
name|acquireLock
argument_list|(
name|TxnStore
operator|.
name|MUTEX_KEY
operator|.
name|HouseKeeper
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|txnHandler
operator|.
name|performTimeOuts
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"timeout reaper ran for "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|/
literal|1000
operator|+
literal|"seconds."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Serious error in {}"
argument_list|,
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
literal|": {}"
operator|+
name|t
operator|.
name|getMessage
argument_list|()
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|handle
operator|!=
literal|null
condition|)
block|{
name|handle
operator|.
name|releaseLocks
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

