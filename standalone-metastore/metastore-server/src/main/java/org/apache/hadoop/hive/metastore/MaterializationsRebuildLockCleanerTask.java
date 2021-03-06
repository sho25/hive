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
name|metastore
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
name|common
operator|.
name|ValidTxnList
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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|txn
operator|.
name|TxnCommonUtils
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
name|txn
operator|.
name|TxnStore
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
name|txn
operator|.
name|TxnUtils
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
comment|/**  * Cleaner for the {@link MaterializationsRebuildLockHandler}. It removes outdated locks  * in the intervals specified by the input property.  */
end_comment

begin_class
specifier|public
class|class
name|MaterializationsRebuildLockCleanerTask
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
name|MaterializationsRebuildLockCleanerTask
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
name|TXN_TIMEOUT
argument_list|,
name|unit
argument_list|)
operator|/
literal|2
return|;
block|}
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
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cleaning up materialization rebuild locks"
argument_list|)
expr_stmt|;
block|}
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
name|MaterializationRebuild
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|ValidTxnList
name|validTxnList
init|=
name|TxnCommonUtils
operator|.
name|createValidReadTxnList
argument_list|(
name|txnHandler
operator|.
name|getOpenTxns
argument_list|()
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|long
name|removedCnt
init|=
name|txnHandler
operator|.
name|cleanupMaterializationRebuildLocks
argument_list|(
name|validTxnList
argument_list|,
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
name|TXN_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|removedCnt
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Number of materialization locks deleted: "
operator|+
name|removedCnt
argument_list|)
expr_stmt|;
block|}
block|}
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

