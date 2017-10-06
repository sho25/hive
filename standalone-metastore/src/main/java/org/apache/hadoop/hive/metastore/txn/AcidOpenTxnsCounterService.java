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
name|RunnableConfigurable
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

begin_comment
comment|/**  * Background running thread, periodically updating number of open transactions.  * Runs inside Hive Metastore Service.  */
end_comment

begin_class
specifier|public
class|class
name|AcidOpenTxnsCounterService
implements|implements
name|RunnableConfigurable
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
name|AcidOpenTxnsCounterService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|int
name|isAliveCounter
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|lastLogTime
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|isAliveCounter
operator|++
expr_stmt|;
name|TxnStore
name|txnHandler
init|=
name|TxnUtils
operator|.
name|getTxnStore
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|txnHandler
operator|.
name|countOpenTxns
argument_list|()
expr_stmt|;
if|if
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|lastLogTime
operator|>
literal|60
operator|*
literal|1000
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"AcidOpenTxnsCounterService ran for "
operator|+
operator|(
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
operator|)
operator|+
literal|" seconds.  isAliveCounter = "
operator|+
name|isAliveCounter
argument_list|)
expr_stmt|;
name|lastLogTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
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
block|}
end_class

end_unit

