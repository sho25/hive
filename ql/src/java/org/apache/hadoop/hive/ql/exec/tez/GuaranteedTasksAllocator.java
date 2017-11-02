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
name|tez
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|llap
operator|.
name|AsyncPbRpcProxy
operator|.
name|ExecuteRequestCallback
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
name|llap
operator|.
name|plugin
operator|.
name|rpc
operator|.
name|LlapPluginProtocolProtos
operator|.
name|UpdateQueryRequestProto
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
name|llap
operator|.
name|plugin
operator|.
name|rpc
operator|.
name|LlapPluginProtocolProtos
operator|.
name|UpdateQueryResponseProto
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
name|optimizer
operator|.
name|physical
operator|.
name|LlapClusterStateForCompile
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
comment|/** Implements query resource allocation using guaranteed tasks. */
end_comment

begin_class
specifier|public
class|class
name|GuaranteedTasksAllocator
implements|implements
name|QueryAllocationManager
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
name|GuaranteedTasksAllocator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|long
name|CLUSTER_INFO_UPDATE_INTERVAL_MS
init|=
literal|120
operator|*
literal|1000L
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|LlapClusterStateForCompile
name|clusterState
decl_stmt|;
specifier|private
specifier|final
name|Thread
name|clusterStateUpdateThread
decl_stmt|;
specifier|private
specifier|final
name|LlapPluginEndpointClient
name|amCommunicator
decl_stmt|;
specifier|public
name|GuaranteedTasksAllocator
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|LlapPluginEndpointClient
name|amCommunicator
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|clusterState
operator|=
operator|new
name|LlapClusterStateForCompile
argument_list|(
name|conf
argument_list|,
name|CLUSTER_INFO_UPDATE_INTERVAL_MS
argument_list|)
expr_stmt|;
name|this
operator|.
name|amCommunicator
operator|=
name|amCommunicator
expr_stmt|;
name|this
operator|.
name|clusterStateUpdateThread
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|getExecutorCount
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Trigger an update if needed.
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|CLUSTER_INFO_UPDATE_INTERVAL_MS
operator|/
literal|2
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cluster state update thread was interrupted"
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
block|}
block|}
argument_list|,
literal|"Cluster State Updater"
argument_list|)
expr_stmt|;
name|clusterStateUpdateThread
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
comment|// Try to get cluster information once, to avoid immediate cluster-update event in WM.
name|clusterState
operator|.
name|initClusterInfo
argument_list|()
expr_stmt|;
name|clusterStateUpdateThread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|clusterStateUpdateThread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
comment|// Don't wait for the thread.
block|}
specifier|public
name|void
name|initClusterInfo
parameter_list|()
block|{
name|clusterState
operator|.
name|initClusterInfo
argument_list|()
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|protected
name|int
name|getExecutorCount
parameter_list|(
name|boolean
name|allowUpdate
parameter_list|)
block|{
if|if
condition|(
name|allowUpdate
operator|&&
operator|!
name|clusterState
operator|.
name|initClusterInfo
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get LLAP cluster information for "
operator|+
name|HiveConf
operator|.
name|getTrimmedVar
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_SERVICE_HOSTS
argument_list|)
operator|+
literal|"; we may rely on outdated cluster status"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|clusterState
operator|.
name|hasClusterInfo
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"No cluster information available to allocate; no guaranteed tasks will be used"
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
name|int
name|unknownNodes
init|=
name|clusterState
operator|.
name|getNodeCountWithUnknownExecutors
argument_list|()
decl_stmt|;
if|if
condition|(
name|unknownNodes
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"There are "
operator|+
name|unknownNodes
operator|+
literal|" nodes with unknown executor count; only "
operator|+
name|clusterState
operator|.
name|getKnownExecutorCount
argument_list|()
operator|+
literal|" guaranteed tasks will be allocated"
argument_list|)
expr_stmt|;
block|}
return|return
name|clusterState
operator|.
name|getKnownExecutorCount
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateSessionsAsync
parameter_list|(
name|Double
name|totalMaxAlloc
parameter_list|,
name|List
argument_list|<
name|WmTezSession
argument_list|>
name|sessionsToUpdate
parameter_list|)
block|{
comment|// Do not make a remote call under any circumstances - this is supposed to be async.
name|int
name|totalCount
init|=
name|getExecutorCount
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|int
name|totalToDistribute
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|totalMaxAlloc
operator|!=
literal|null
condition|)
block|{
name|totalToDistribute
operator|=
operator|(
name|int
operator|)
name|Math
operator|.
name|round
argument_list|(
name|totalCount
operator|*
name|totalMaxAlloc
argument_list|)
expr_stmt|;
block|}
name|double
name|lastDelta
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|sessionsToUpdate
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|WmTezSession
name|session
init|=
name|sessionsToUpdate
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|int
name|intAlloc
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|i
operator|+
literal|1
operator|==
name|sessionsToUpdate
operator|.
name|size
argument_list|()
operator|&&
name|totalToDistribute
operator|>=
literal|0
condition|)
block|{
name|intAlloc
operator|=
name|totalToDistribute
expr_stmt|;
comment|// We rely on the caller to supply a reasonable total; we could log a warning
comment|// if this doesn't match the allocation of the last session beyond some threshold.
block|}
else|else
block|{
comment|// This ensures we don't create skew, e.g. with 8 ducks and 5 queries with simple rounding
comment|// we'd produce 2-2-2-2-0 as we round 1.6; whereas adding the last delta to the next query
comment|// we'd round 1.6-1.2-1.8-1.4-2.0 and thus give out 2-1-2-1-2, as intended.
comment|// Note that fractions don't have to all be the same like in this example.
name|double
name|fraction
init|=
name|session
operator|.
name|getClusterFraction
argument_list|()
decl_stmt|;
name|double
name|allocation
init|=
name|fraction
operator|*
name|totalCount
operator|+
name|lastDelta
decl_stmt|;
name|double
name|roundedAlloc
init|=
name|Math
operator|.
name|round
argument_list|(
name|allocation
argument_list|)
decl_stmt|;
name|lastDelta
operator|=
name|allocation
operator|-
name|roundedAlloc
expr_stmt|;
if|if
condition|(
name|roundedAlloc
operator|<
literal|0
condition|)
block|{
name|roundedAlloc
operator|=
literal|0
expr_stmt|;
comment|// Can this happen? Delta cannot exceed 0.5.
block|}
name|intAlloc
operator|=
operator|(
name|int
operator|)
name|roundedAlloc
expr_stmt|;
block|}
comment|// Make sure we don't give out more than allowed due to double/rounding artifacts.
if|if
condition|(
name|totalToDistribute
operator|>=
literal|0
condition|)
block|{
if|if
condition|(
name|intAlloc
operator|>
name|totalToDistribute
condition|)
block|{
name|intAlloc
operator|=
name|totalToDistribute
expr_stmt|;
block|}
name|totalToDistribute
operator|-=
name|intAlloc
expr_stmt|;
block|}
comment|// This will only send update if it's necessary.
name|updateSessionAsync
argument_list|(
name|session
argument_list|,
name|intAlloc
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|updateSessionAsync
parameter_list|(
specifier|final
name|WmTezSession
name|session
parameter_list|,
specifier|final
name|int
name|intAlloc
parameter_list|)
block|{
name|boolean
name|needsUpdate
init|=
name|session
operator|.
name|setSendingGuaranteed
argument_list|(
name|intAlloc
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|needsUpdate
condition|)
return|return;
comment|// Note: this assumes that the pattern where the same session object is reset with a different
comment|//       Tez client is not used. It was used a lot in the past but appears to be gone from most
comment|//       HS2 session pool paths, and this patch removes the last one (reopen).
name|UpdateQueryRequestProto
name|request
init|=
name|UpdateQueryRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setGuaranteedTaskCount
argument_list|(
name|intAlloc
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|amCommunicator
operator|.
name|sendUpdateQuery
argument_list|(
name|request
argument_list|,
operator|(
name|AmPluginNode
operator|)
name|session
argument_list|,
operator|new
name|UpdateCallback
argument_list|(
name|session
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|final
class|class
name|UpdateCallback
implements|implements
name|ExecuteRequestCallback
argument_list|<
name|UpdateQueryResponseProto
argument_list|>
block|{
specifier|private
specifier|final
name|WmTezSession
name|session
decl_stmt|;
specifier|private
name|UpdateCallback
parameter_list|(
name|WmTezSession
name|session
parameter_list|)
block|{
name|this
operator|.
name|session
operator|=
name|session
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setResponse
parameter_list|(
name|UpdateQueryResponseProto
name|response
parameter_list|)
block|{
name|int
name|nextUpdate
init|=
name|session
operator|.
name|setSentGuaranteed
argument_list|()
decl_stmt|;
if|if
condition|(
name|nextUpdate
operator|>=
literal|0
condition|)
block|{
name|updateSessionAsync
argument_list|(
name|session
argument_list|,
name|nextUpdate
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|indicateError
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to update guaranteed tasks count for the session "
operator|+
name|session
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|boolean
name|isOkToFail
init|=
name|session
operator|.
name|setFailedToSendGuaranteed
argument_list|()
decl_stmt|;
if|if
condition|(
name|isOkToFail
condition|)
return|return;
comment|// RPC already handles retries, so we will just try to kill the session here.
comment|// This will cause the current query to fail. We could instead keep retrying.
try|try
block|{
name|session
operator|.
name|handleUpdateError
argument_list|()
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
literal|"Failed to kill the session "
operator|+
name|session
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

