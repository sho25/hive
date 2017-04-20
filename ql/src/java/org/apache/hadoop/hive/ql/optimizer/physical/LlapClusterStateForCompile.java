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
name|optimizer
operator|.
name|physical
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
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
name|cache
operator|.
name|Cache
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
name|cache
operator|.
name|CacheBuilder
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
name|Map
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
name|Callable
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
name|registry
operator|.
name|ServiceInstance
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
name|registry
operator|.
name|ServiceInstanceSet
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
name|registry
operator|.
name|impl
operator|.
name|InactiveServiceInstance
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
name|registry
operator|.
name|impl
operator|.
name|LlapRegistryService
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

begin_class
specifier|public
class|class
name|LlapClusterStateForCompile
block|{
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LlapClusterStateForCompile
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|CLUSTER_UPDATE_INTERVAL_NS
init|=
literal|120
operator|*
literal|1000000000L
decl_stmt|;
comment|// 2 minutes.
specifier|private
name|Long
name|lastClusterUpdateNs
decl_stmt|;
specifier|private
name|Integer
name|noConfigNodeCount
decl_stmt|,
name|executorCount
decl_stmt|;
specifier|private
name|LlapRegistryService
name|svc
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
comment|// It's difficult to impossible to pass global things to compilation, so we have a static cache.
specifier|private
specifier|static
specifier|final
name|Cache
argument_list|<
name|String
argument_list|,
name|LlapClusterStateForCompile
argument_list|>
name|CACHE
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|initialCapacity
argument_list|(
literal|10
argument_list|)
operator|.
name|maximumSize
argument_list|(
literal|100
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|LlapClusterStateForCompile
name|getClusterInfo
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
specifier|final
name|String
name|nodes
init|=
name|HiveConf
operator|.
name|getTrimmedVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_SERVICE_HOSTS
argument_list|)
decl_stmt|;
specifier|final
name|String
name|userName
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_ZK_REGISTRY_USER
argument_list|,
name|LlapRegistryService
operator|.
name|currentUser
argument_list|()
argument_list|)
decl_stmt|;
name|Callable
argument_list|<
name|LlapClusterStateForCompile
argument_list|>
name|generator
init|=
operator|new
name|Callable
argument_list|<
name|LlapClusterStateForCompile
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|LlapClusterStateForCompile
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating cluster info for "
operator|+
name|userName
operator|+
literal|":"
operator|+
name|nodes
argument_list|)
expr_stmt|;
return|return
operator|new
name|LlapClusterStateForCompile
argument_list|(
name|conf
argument_list|)
return|;
block|}
block|}
decl_stmt|;
try|try
block|{
return|return
name|CACHE
operator|.
name|get
argument_list|(
name|userName
operator|+
literal|":"
operator|+
name|nodes
argument_list|,
name|generator
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
comment|// Should never happen... ctor is just assignments.
block|}
block|}
specifier|private
name|LlapClusterStateForCompile
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasClusterInfo
parameter_list|()
block|{
return|return
name|lastClusterUpdateNs
operator|!=
literal|null
return|;
block|}
specifier|public
name|int
name|getKnownExecutorCount
parameter_list|()
block|{
return|return
name|executorCount
return|;
block|}
specifier|public
name|int
name|getNodeCountWithUnknownExecutors
parameter_list|()
block|{
return|return
name|noConfigNodeCount
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|initClusterInfo
parameter_list|()
block|{
if|if
condition|(
name|lastClusterUpdateNs
operator|!=
literal|null
condition|)
block|{
name|long
name|elapsed
init|=
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|lastClusterUpdateNs
decl_stmt|;
if|if
condition|(
name|elapsed
operator|<
name|CLUSTER_UPDATE_INTERVAL_NS
condition|)
return|return;
block|}
if|if
condition|(
name|svc
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|svc
operator|=
name|LlapRegistryService
operator|.
name|getClient
argument_list|(
name|conf
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
name|info
argument_list|(
literal|"Cannot create the client; ignoring"
argument_list|,
name|t
argument_list|)
expr_stmt|;
return|return;
comment|// Don't fail; this is best-effort.
block|}
block|}
name|ServiceInstanceSet
name|instances
decl_stmt|;
try|try
block|{
name|instances
operator|=
name|svc
operator|.
name|getInstances
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cannot update cluster information; ignoring"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
comment|// Don't wait for the cluster if not started; this is best-effort.
block|}
name|int
name|executorsLocal
init|=
literal|0
decl_stmt|,
name|noConfigNodesLocal
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ServiceInstance
name|si
range|:
name|instances
operator|.
name|getAll
argument_list|()
control|)
block|{
if|if
condition|(
name|si
operator|instanceof
name|InactiveServiceInstance
condition|)
continue|continue;
comment|// Shouldn't happen in getAll.
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
init|=
name|si
operator|.
name|getProperties
argument_list|()
decl_stmt|;
if|if
condition|(
name|props
operator|==
literal|null
condition|)
block|{
operator|++
name|noConfigNodesLocal
expr_stmt|;
continue|continue;
block|}
try|try
block|{
name|executorsLocal
operator|+=
name|Integer
operator|.
name|parseInt
argument_list|(
name|props
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_NUM_EXECUTORS
operator|.
name|varname
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
operator|++
name|noConfigNodesLocal
expr_stmt|;
block|}
block|}
name|lastClusterUpdateNs
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
name|noConfigNodeCount
operator|=
name|noConfigNodesLocal
expr_stmt|;
name|executorCount
operator|=
name|executorsLocal
expr_stmt|;
block|}
block|}
end_class

end_unit

