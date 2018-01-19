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
name|llap
operator|.
name|coordinator
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
name|util
operator|.
name|Random
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
name|llap
operator|.
name|DaemonId
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
name|LlapUtil
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
name|coordinator
operator|.
name|LlapCoordinator
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
name|security
operator|.
name|LlapSigner
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
name|security
operator|.
name|LlapSignerImpl
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
name|security
operator|.
name|LlapTokenLocalClient
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
name|security
operator|.
name|LlapTokenLocalClientImpl
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
name|security
operator|.
name|UserGroupInformation
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationId
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|RemovalListener
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
name|RemovalNotification
import|;
end_import

begin_comment
comment|/**  * The class containing facilities for LLAP interactions in HS2.  * This may eventually evolve into a central LLAP manager hosted by HS2 or elsewhere.  * Refactor as needed.  */
end_comment

begin_class
specifier|public
class|class
name|LlapCoordinator
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
name|LlapCoordinator
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** We'll keep signers per cluster around for some time, for reuse. */
specifier|private
specifier|final
name|Cache
argument_list|<
name|String
argument_list|,
name|LlapSigner
argument_list|>
name|signers
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|removalListener
argument_list|(
operator|new
name|RemovalListener
argument_list|<
name|String
argument_list|,
name|LlapSigner
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onRemoval
parameter_list|(
name|RemovalNotification
argument_list|<
name|String
argument_list|,
name|LlapSigner
argument_list|>
name|notification
parameter_list|)
block|{
if|if
condition|(
name|notification
operator|.
name|getValue
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|notification
operator|.
name|getValue
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
operator|.
name|expireAfterAccess
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// TODO: probably temporary before HIVE-13698; after that we may create one per session.
specifier|private
specifier|static
specifier|final
name|Cache
argument_list|<
name|String
argument_list|,
name|LlapTokenLocalClient
argument_list|>
name|localClientCache
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|expireAfterAccess
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
operator|.
name|removalListener
argument_list|(
operator|new
name|RemovalListener
argument_list|<
name|String
argument_list|,
name|LlapTokenLocalClient
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onRemoval
parameter_list|(
name|RemovalNotification
argument_list|<
name|String
argument_list|,
name|LlapTokenLocalClient
argument_list|>
name|notification
parameter_list|)
block|{
if|if
condition|(
name|notification
operator|.
name|getValue
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|notification
operator|.
name|getValue
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|String
name|clusterUser
decl_stmt|;
specifier|private
name|long
name|startTime
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|appIdCounter
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|LlapCoordinator
parameter_list|()
block|{   }
specifier|private
name|void
name|init
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Only do the lightweight stuff in ctor; by default, LLAP coordinator is created during
comment|// HS2 init without the knowledge of LLAP usage (or lack thereof) in the cluster.
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|this
operator|.
name|clusterUser
operator|=
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
operator|.
name|getShortUserName
argument_list|()
expr_stmt|;
comment|// TODO: if two HS2s start at exactly the same time, which could happen during a coordinated
comment|//       restart, they could start generating the same IDs. Should we store the startTime
comment|//       somewhere like ZK? Try to randomize it a bit for now...
name|long
name|randomBits
init|=
call|(
name|long
call|)
argument_list|(
operator|new
name|Random
argument_list|()
operator|.
name|nextInt
argument_list|()
argument_list|)
operator|<<
literal|32
decl_stmt|;
name|this
operator|.
name|startTime
operator|=
name|Math
operator|.
name|abs
argument_list|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|&
operator|(
name|long
operator|)
name|Integer
operator|.
name|MAX_VALUE
operator|)
operator||
name|randomBits
argument_list|)
expr_stmt|;
block|}
specifier|public
name|LlapSigner
name|getLlapSigner
parameter_list|(
specifier|final
name|Configuration
name|jobConf
parameter_list|)
block|{
comment|// Note that we create the cluster name from user conf (hence, a user can target a cluster),
comment|// but then we create the signer using hiveConf (hence, we control the ZK config and stuff).
assert|assert
name|UserGroupInformation
operator|.
name|isSecurityEnabled
argument_list|()
assert|;
specifier|final
name|String
name|clusterId
init|=
name|DaemonId
operator|.
name|createClusterString
argument_list|(
name|clusterUser
argument_list|,
name|LlapUtil
operator|.
name|generateClusterName
argument_list|(
name|jobConf
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|signers
operator|.
name|get
argument_list|(
name|clusterId
argument_list|,
operator|new
name|Callable
argument_list|<
name|LlapSigner
argument_list|>
argument_list|()
block|{
specifier|public
name|LlapSigner
name|call
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|new
name|LlapSignerImpl
argument_list|(
name|hiveConf
argument_list|,
name|clusterId
argument_list|)
return|;
block|}
block|}
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
block|}
block|}
specifier|public
name|ApplicationId
name|createExtClientAppId
parameter_list|()
block|{
comment|// Note that we cannot allow users to provide app ID, since providing somebody else's appId
comment|// would give one LLAP token (and splits) for that app ID. If we could verify it somehow
comment|// (YARN token? nothing we can do in an UDF), we could get it from client already running on
comment|// YARN. As such, the clients running on YARN will have two app IDs to be aware of.
return|return
name|ApplicationId
operator|.
name|newInstance
argument_list|(
name|startTime
argument_list|,
name|appIdCounter
operator|.
name|incrementAndGet
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|LlapTokenLocalClient
name|getLocalTokenClient
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
name|String
name|clusterUser
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Note that we create the cluster name from user conf (hence, a user can target a cluster),
comment|// but then we create the signer using hiveConf (hence, we control the ZK config and stuff).
assert|assert
name|UserGroupInformation
operator|.
name|isSecurityEnabled
argument_list|()
assert|;
name|String
name|clusterName
init|=
name|LlapUtil
operator|.
name|generateClusterName
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// This assumes that the LLAP cluster and session are both running under HS2 user.
specifier|final
name|String
name|clusterId
init|=
name|DaemonId
operator|.
name|createClusterString
argument_list|(
name|clusterUser
argument_list|,
name|clusterName
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|localClientCache
operator|.
name|get
argument_list|(
name|clusterId
argument_list|,
operator|new
name|Callable
argument_list|<
name|LlapTokenLocalClientImpl
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|LlapTokenLocalClientImpl
name|call
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|new
name|LlapTokenLocalClientImpl
argument_list|(
name|hiveConf
argument_list|,
name|clusterId
argument_list|)
return|;
block|}
block|}
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|localClientCache
operator|.
name|invalidateAll
argument_list|()
expr_stmt|;
name|signers
operator|.
name|invalidateAll
argument_list|()
expr_stmt|;
name|localClientCache
operator|.
name|cleanUp
argument_list|()
expr_stmt|;
name|signers
operator|.
name|cleanUp
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error closing the coordinator; ignoring"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** TODO: ideally, when the splits UDF is made a proper API, coordinator should not    *        be managed as a global. HS2 should create it and then pass it around. */
specifier|private
specifier|static
specifier|final
name|LlapCoordinator
name|INSTANCE
init|=
operator|new
name|LlapCoordinator
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|void
name|initializeInstance
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|IOException
block|{
name|INSTANCE
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|LlapCoordinator
name|getInstance
parameter_list|()
block|{
return|return
name|INSTANCE
return|;
block|}
block|}
end_class

end_unit

