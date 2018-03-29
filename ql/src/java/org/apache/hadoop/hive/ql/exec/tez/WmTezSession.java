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
name|ListenableFuture
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
name|SettableFuture
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
name|Future
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
name|ScheduledFuture
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
name|registry
operator|.
name|impl
operator|.
name|TezAmInstance
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
name|common
operator|.
name|util
operator|.
name|Ref
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|annotate
operator|.
name|JsonIgnore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|annotate
operator|.
name|JsonProperty
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|annotate
operator|.
name|JsonSerialize
import|;
end_import

begin_class
annotation|@
name|JsonSerialize
specifier|public
class|class
name|WmTezSession
extends|extends
name|TezSessionPoolSession
implements|implements
name|AmPluginNode
block|{
annotation|@
name|JsonProperty
argument_list|(
literal|"poolName"
argument_list|)
specifier|private
name|String
name|poolName
decl_stmt|;
annotation|@
name|JsonProperty
argument_list|(
literal|"clusterFraction"
argument_list|)
specifier|private
name|Double
name|clusterFraction
decl_stmt|;
comment|/**    * The reason to kill an AM. Note that this is for the entire session, not just for a query.    * Once set, this can never be unset because you can only kill the session once.    */
annotation|@
name|JsonProperty
argument_list|(
literal|"killReason"
argument_list|)
specifier|private
name|String
name|killReason
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|Object
name|amPluginInfoLock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
annotation|@
name|JsonProperty
argument_list|(
literal|"amPluginInfo"
argument_list|)
specifier|private
name|AmPluginInfo
name|amPluginInfo
init|=
literal|null
decl_stmt|;
specifier|private
name|Integer
name|amPluginEndpointVersion
init|=
literal|null
decl_stmt|;
specifier|private
name|SettableFuture
argument_list|<
name|WmTezSession
argument_list|>
name|amRegistryFuture
init|=
literal|null
decl_stmt|;
specifier|private
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|timeoutTimer
init|=
literal|null
decl_stmt|;
annotation|@
name|JsonProperty
argument_list|(
literal|"queryId"
argument_list|)
specifier|private
name|String
name|queryId
decl_stmt|;
specifier|private
name|SettableFuture
argument_list|<
name|Boolean
argument_list|>
name|returnFuture
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|WorkloadManager
name|wmParent
decl_stmt|;
comment|/** The actual state of the guaranteed task, and the update state, for the session. */
comment|// Note: hypothetically, a generic WM-aware-session should not know about guaranteed tasks.
comment|//       We should have another subclass for a WM-aware-session-implemented-using-ducks.
comment|//       However, since this is the only type of WM for now, this can live here.
specifier|private
specifier|final
specifier|static
class|class
name|ActualWmState
block|{
comment|// All accesses synchronized on the object itself. Could be replaced with CAS.
name|int
name|sending
init|=
operator|-
literal|1
decl_stmt|,
name|sent
init|=
operator|-
literal|1
decl_stmt|,
name|target
init|=
literal|0
decl_stmt|;
block|}
specifier|private
specifier|final
name|ActualWmState
name|actualState
init|=
operator|new
name|ActualWmState
argument_list|()
decl_stmt|;
specifier|public
name|WmTezSession
parameter_list|(
name|String
name|sessionId
parameter_list|,
name|WorkloadManager
name|parent
parameter_list|,
name|SessionExpirationTracker
name|expiration
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|sessionId
argument_list|,
name|parent
argument_list|,
name|expiration
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|wmParent
operator|=
name|parent
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|WmTezSession
parameter_list|(
name|String
name|sessionId
parameter_list|,
name|Manager
name|testParent
parameter_list|,
name|SessionExpirationTracker
name|expiration
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|sessionId
argument_list|,
name|testParent
argument_list|,
name|expiration
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|wmParent
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|ListenableFuture
argument_list|<
name|WmTezSession
argument_list|>
name|waitForAmRegistryAsync
parameter_list|(
name|int
name|timeoutMs
parameter_list|,
name|ScheduledExecutorService
name|timeoutPool
parameter_list|)
block|{
name|SettableFuture
argument_list|<
name|WmTezSession
argument_list|>
name|future
init|=
name|SettableFuture
operator|.
name|create
argument_list|()
decl_stmt|;
synchronized|synchronized
init|(
name|amPluginInfoLock
init|)
block|{
if|if
condition|(
name|amPluginInfo
operator|!=
literal|null
condition|)
block|{
name|future
operator|.
name|set
argument_list|(
name|this
argument_list|)
expr_stmt|;
return|return
name|future
return|;
block|}
if|if
condition|(
name|amRegistryFuture
operator|!=
literal|null
condition|)
block|{
comment|// We don't need this for now, so do not support it.
name|future
operator|.
name|setException
argument_list|(
operator|new
name|RuntimeException
argument_list|(
literal|"Multiple waits are not suported"
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|future
return|;
block|}
name|amRegistryFuture
operator|=
name|future
expr_stmt|;
if|if
condition|(
name|timeoutMs
operator|<=
literal|0
condition|)
return|return
name|future
return|;
comment|// TODO: replace with withTimeout after we get the relevant guava upgrade.
name|this
operator|.
name|timeoutTimer
operator|=
name|timeoutPool
operator|.
name|schedule
argument_list|(
operator|new
name|TimeoutRunnable
argument_list|()
argument_list|,
name|timeoutMs
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
return|return
name|future
return|;
block|}
annotation|@
name|Override
name|void
name|updateFromRegistry
parameter_list|(
name|TezAmInstance
name|si
parameter_list|,
name|int
name|ephSeqVersion
parameter_list|)
block|{
name|updateAmEndpointInfo
argument_list|(
name|si
argument_list|,
name|ephSeqVersion
argument_list|)
expr_stmt|;
if|if
condition|(
name|si
operator|!=
literal|null
condition|)
block|{
name|handleGuaranteedTasksChange
argument_list|(
name|si
operator|.
name|getGuaranteedCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|updateAmEndpointInfo
parameter_list|(
name|TezAmInstance
name|si
parameter_list|,
name|int
name|ephSeqVersion
parameter_list|)
block|{
name|AmPluginInfo
name|info
init|=
name|si
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|AmPluginInfo
argument_list|(
name|si
operator|.
name|getHost
argument_list|()
argument_list|,
name|si
operator|.
name|getPluginPort
argument_list|()
argument_list|,
name|si
operator|.
name|getPluginToken
argument_list|()
argument_list|,
name|si
operator|.
name|getPluginTokenJobId
argument_list|()
argument_list|)
decl_stmt|;
synchronized|synchronized
init|(
name|amPluginInfoLock
init|)
block|{
comment|// Ignore the outdated updates; for the same version, ignore non-null updates because
comment|// we assume that removal is the last thing that happens for any given version.
if|if
condition|(
operator|(
name|amPluginEndpointVersion
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|(
name|amPluginEndpointVersion
operator|>
name|ephSeqVersion
operator|)
operator|||
operator|(
name|amPluginEndpointVersion
operator|==
name|ephSeqVersion
operator|&&
name|info
operator|!=
literal|null
operator|)
operator|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Ignoring an outdated info update {}: {}"
argument_list|,
name|ephSeqVersion
argument_list|,
name|si
argument_list|)
expr_stmt|;
return|return;
block|}
name|this
operator|.
name|amPluginEndpointVersion
operator|=
name|ephSeqVersion
expr_stmt|;
name|this
operator|.
name|amPluginInfo
operator|=
name|info
expr_stmt|;
if|if
condition|(
name|info
operator|!=
literal|null
condition|)
block|{
comment|// Only update someone waiting for info if we have the info.
if|if
condition|(
name|amRegistryFuture
operator|!=
literal|null
condition|)
block|{
name|amRegistryFuture
operator|.
name|set
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|amRegistryFuture
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|timeoutTimer
operator|!=
literal|null
condition|)
block|{
name|timeoutTimer
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|timeoutTimer
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|handleGuaranteedTasksChange
parameter_list|(
name|int
name|guaranteedCount
parameter_list|)
block|{
name|boolean
name|doNotify
init|=
literal|false
decl_stmt|;
synchronized|synchronized
init|(
name|actualState
init|)
block|{
comment|// A noop if we are in process of sending or if we have the correct value.
if|if
condition|(
name|actualState
operator|.
name|sending
operator|!=
operator|-
literal|1
operator|||
name|actualState
operator|.
name|sent
operator|==
name|guaranteedCount
condition|)
return|return;
name|actualState
operator|.
name|sent
operator|=
name|guaranteedCount
expr_stmt|;
name|doNotify
operator|=
name|actualState
operator|.
name|target
operator|!=
name|guaranteedCount
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|doNotify
condition|)
return|return;
name|wmParent
operator|.
name|notifyOfInconsistentAllocation
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|AmPluginInfo
name|getAmPluginInfo
parameter_list|(
name|Ref
argument_list|<
name|Integer
argument_list|>
name|version
parameter_list|)
block|{
synchronized|synchronized
init|(
name|amPluginInfoLock
init|)
block|{
name|version
operator|.
name|value
operator|=
name|amPluginEndpointVersion
expr_stmt|;
return|return
name|amPluginInfo
return|;
block|}
block|}
name|void
name|setPoolName
parameter_list|(
name|String
name|poolName
parameter_list|)
block|{
name|this
operator|.
name|poolName
operator|=
name|poolName
expr_stmt|;
block|}
specifier|public
name|String
name|getPoolName
parameter_list|()
block|{
return|return
name|poolName
return|;
block|}
name|void
name|setClusterFraction
parameter_list|(
name|double
name|fraction
parameter_list|)
block|{
name|this
operator|.
name|clusterFraction
operator|=
name|fraction
expr_stmt|;
block|}
name|void
name|clearWm
parameter_list|()
block|{
name|this
operator|.
name|poolName
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|clusterFraction
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasClusterFraction
parameter_list|()
block|{
return|return
name|this
operator|.
name|clusterFraction
operator|!=
literal|null
return|;
block|}
specifier|public
name|double
name|getClusterFraction
parameter_list|()
block|{
return|return
name|this
operator|.
name|clusterFraction
return|;
block|}
name|Integer
name|setSendingGuaranteed
parameter_list|(
name|Integer
name|intAlloc
parameter_list|)
block|{
assert|assert
name|intAlloc
operator|==
literal|null
operator|||
name|intAlloc
operator|>=
literal|0
assert|;
synchronized|synchronized
init|(
name|actualState
init|)
block|{
if|if
condition|(
name|intAlloc
operator|!=
literal|null
condition|)
block|{
name|actualState
operator|.
name|target
operator|=
name|intAlloc
expr_stmt|;
block|}
else|else
block|{
name|intAlloc
operator|=
name|actualState
operator|.
name|target
expr_stmt|;
block|}
if|if
condition|(
name|actualState
operator|.
name|sending
operator|!=
operator|-
literal|1
condition|)
return|return
literal|null
return|;
comment|// The sender will take care of this.
if|if
condition|(
name|actualState
operator|.
name|sent
operator|==
name|intAlloc
condition|)
return|return
literal|null
return|;
comment|// The value didn't change.
name|actualState
operator|.
name|sending
operator|=
name|intAlloc
expr_stmt|;
return|return
name|intAlloc
return|;
block|}
block|}
specifier|public
name|String
name|getAllocationState
parameter_list|()
block|{
synchronized|synchronized
init|(
name|actualState
init|)
block|{
return|return
literal|"actual/target "
operator|+
name|actualState
operator|.
name|sent
operator|+
literal|"/"
operator|+
name|actualState
operator|.
name|target
operator|+
operator|(
name|actualState
operator|.
name|sending
operator|>=
literal|0
condition|?
literal|"; sending"
else|:
literal|""
operator|)
return|;
block|}
block|}
name|int
name|setSentGuaranteed
parameter_list|()
block|{
comment|// Only one send can be active at the same time.
synchronized|synchronized
init|(
name|actualState
init|)
block|{
assert|assert
name|actualState
operator|.
name|sending
operator|!=
operator|-
literal|1
assert|;
name|actualState
operator|.
name|sent
operator|=
name|actualState
operator|.
name|sending
expr_stmt|;
name|actualState
operator|.
name|sending
operator|=
operator|-
literal|1
expr_stmt|;
return|return
operator|(
name|actualState
operator|.
name|sent
operator|==
name|actualState
operator|.
name|target
operator|)
condition|?
operator|-
literal|1
else|:
name|actualState
operator|.
name|target
return|;
block|}
block|}
name|boolean
name|setFailedToSendGuaranteed
parameter_list|()
block|{
synchronized|synchronized
init|(
name|actualState
init|)
block|{
assert|assert
name|actualState
operator|.
name|sending
operator|!=
operator|-
literal|1
assert|;
name|actualState
operator|.
name|sending
operator|=
operator|-
literal|1
expr_stmt|;
comment|// It's ok to skip a failed message if the target has changed back to the old value.
return|return
operator|(
name|actualState
operator|.
name|sent
operator|==
name|actualState
operator|.
name|target
operator|)
return|;
block|}
block|}
specifier|public
name|void
name|handleUpdateError
parameter_list|(
name|int
name|endpointVersion
parameter_list|)
block|{
name|wmParent
operator|.
name|addUpdateError
argument_list|(
name|this
argument_list|,
name|endpointVersion
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|System
operator|.
name|identityHashCode
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
name|obj
operator|==
name|this
return|;
block|}
name|boolean
name|isIrrelevantForWm
parameter_list|()
block|{
return|return
name|killReason
operator|!=
literal|null
return|;
block|}
name|String
name|getReasonForKill
parameter_list|()
block|{
return|return
name|killReason
return|;
block|}
name|void
name|setIsIrrelevantForWm
parameter_list|(
name|String
name|killReason
parameter_list|)
block|{
if|if
condition|(
name|killReason
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Cannot reset the kill reason "
operator|+
name|this
operator|.
name|killReason
argument_list|)
throw|;
block|}
name|this
operator|.
name|killReason
operator|=
name|killReason
expr_stmt|;
block|}
specifier|private
specifier|final
class|class
name|TimeoutRunnable
implements|implements
name|Runnable
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
synchronized|synchronized
init|(
name|amPluginInfoLock
init|)
block|{
name|timeoutTimer
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|amRegistryFuture
operator|==
literal|null
operator|||
name|amRegistryFuture
operator|.
name|isDone
argument_list|()
condition|)
return|return;
name|amRegistryFuture
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|amRegistryFuture
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|setQueryId
parameter_list|(
name|String
name|queryId
parameter_list|)
block|{
name|this
operator|.
name|queryId
operator|=
name|queryId
expr_stmt|;
block|}
specifier|public
name|String
name|getQueryId
parameter_list|()
block|{
return|return
name|this
operator|.
name|queryId
return|;
block|}
name|void
name|createAndSetReturnFuture
parameter_list|()
block|{
name|this
operator|.
name|returnFuture
operator|=
name|SettableFuture
operator|.
name|create
argument_list|()
expr_stmt|;
if|if
condition|(
name|getWmContext
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|getWmContext
argument_list|()
operator|.
name|setReturnEventFuture
argument_list|(
name|returnFuture
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|resolveReturnFuture
parameter_list|()
block|{
if|if
condition|(
name|returnFuture
operator|!=
literal|null
condition|)
block|{
name|returnFuture
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|returnFuture
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|super
operator|.
name|toString
argument_list|()
operator|+
literal|", WM state poolName="
operator|+
name|poolName
operator|+
literal|", clusterFraction="
operator|+
name|clusterFraction
operator|+
literal|", queryId="
operator|+
name|queryId
operator|+
literal|", killReason="
operator|+
name|killReason
return|;
block|}
block|}
end_class

end_unit

