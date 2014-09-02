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
package|;
end_package

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
name|LockException
import|;
end_import

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
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * Class to handle heartbeats for MR and Tez tasks.  */
end_comment

begin_class
specifier|public
class|class
name|Heartbeater
block|{
specifier|private
name|long
name|lastHeartbeat
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|heartbeatInterval
init|=
literal|0
decl_stmt|;
specifier|private
name|boolean
name|dontHeartbeat
init|=
literal|false
decl_stmt|;
specifier|private
name|HiveTxnManager
name|txnMgr
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Heartbeater
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    *    * @param txnMgr transaction manager for this operation    * @param conf Configuration for this operation    */
specifier|public
name|Heartbeater
parameter_list|(
name|HiveTxnManager
name|txnMgr
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|txnMgr
operator|=
name|txnMgr
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
comment|/**    * Send a heartbeat to the metastore for locks and transactions.    * @throws IOException    */
specifier|public
name|void
name|heartbeat
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|dontHeartbeat
condition|)
return|return;
if|if
condition|(
name|txnMgr
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"txnMgr null, not heartbeating"
argument_list|)
expr_stmt|;
name|dontHeartbeat
operator|=
literal|true
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|heartbeatInterval
operator|==
literal|0
condition|)
block|{
comment|// Multiply the heartbeat interval by 1000 to convert to milliseconds,
comment|// but divide by 2 to give us a safety factor.
name|heartbeatInterval
operator|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|/
literal|2
expr_stmt|;
if|if
condition|(
name|heartbeatInterval
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_MANAGER
operator|.
name|toString
argument_list|()
operator|+
literal|" not set, heartbeats won't be sent"
argument_list|)
expr_stmt|;
name|dontHeartbeat
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"heartbeat interval 0, not heartbeating"
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|now
operator|-
name|lastHeartbeat
operator|>
name|heartbeatInterval
condition|)
block|{
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"heartbeating"
argument_list|)
expr_stmt|;
name|txnMgr
operator|.
name|heartbeat
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed trying to heartbeat "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|lastHeartbeat
operator|=
name|now
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

