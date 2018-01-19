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
name|util
operator|.
name|concurrent
operator|.
name|Futures
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
name|FutureCallback
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
name|ScheduledExecutorService
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
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
name|shims
operator|.
name|Utils
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
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|TezException
import|;
end_import

begin_comment
comment|/**  * This class is needed for writing junit tests. For testing the multi-session  * use case from hive server 2, we need a session simulation.  *  */
end_comment

begin_class
specifier|public
class|class
name|SampleTezSessionState
extends|extends
name|WmTezSession
block|{
specifier|private
name|boolean
name|open
decl_stmt|;
specifier|private
specifier|final
name|String
name|sessionId
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|String
name|user
decl_stmt|;
specifier|private
name|boolean
name|doAsEnabled
decl_stmt|;
specifier|private
name|ListenableFuture
argument_list|<
name|Boolean
argument_list|>
name|waitForAmRegFuture
decl_stmt|;
specifier|public
name|SampleTezSessionState
parameter_list|(
name|String
name|sessionId
parameter_list|,
name|TezSessionPoolSession
operator|.
name|Manager
name|parent
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
operator|(
name|parent
operator|instanceof
name|TezSessionPoolManager
operator|)
condition|?
operator|(
operator|(
name|TezSessionPoolManager
operator|)
name|parent
operator|)
operator|.
name|getExpirationTracker
argument_list|()
else|:
literal|null
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|sessionId
operator|=
name|sessionId
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|conf
expr_stmt|;
name|waitForAmRegFuture
operator|=
name|createDefaultWaitForAmRegistryFuture
argument_list|()
expr_stmt|;
block|}
specifier|private
name|SettableFuture
argument_list|<
name|Boolean
argument_list|>
name|createDefaultWaitForAmRegistryFuture
parameter_list|()
block|{
name|SettableFuture
argument_list|<
name|Boolean
argument_list|>
name|noWait
init|=
name|SettableFuture
operator|.
name|create
argument_list|()
decl_stmt|;
name|noWait
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// By default, do not wait.
return|return
name|noWait
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isOpen
parameter_list|()
block|{
return|return
name|open
return|;
block|}
specifier|public
name|void
name|setOpen
parameter_list|(
name|boolean
name|open
parameter_list|)
block|{
name|this
operator|.
name|open
operator|=
name|open
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|open
parameter_list|()
throws|throws
name|LoginException
throws|,
name|IOException
block|{
name|UserGroupInformation
name|ugi
init|=
name|Utils
operator|.
name|getUGI
argument_list|()
decl_stmt|;
name|user
operator|=
name|ugi
operator|.
name|getShortUserName
argument_list|()
expr_stmt|;
name|this
operator|.
name|doAsEnabled
operator|=
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|)
expr_stmt|;
name|setOpen
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|open
parameter_list|(
name|HiveResources
name|resources
parameter_list|)
throws|throws
name|LoginException
throws|,
name|IOException
block|{
name|open
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|open
parameter_list|(
name|String
index|[]
name|additionalFiles
parameter_list|)
throws|throws
name|IOException
throws|,
name|LoginException
block|{
name|open
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|close
parameter_list|(
name|boolean
name|keepTmpDir
parameter_list|)
throws|throws
name|TezException
throws|,
name|IOException
block|{
name|open
operator|=
name|keepTmpDir
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|this
operator|.
name|hiveConf
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getSessionId
parameter_list|()
block|{
return|return
name|sessionId
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getUser
parameter_list|()
block|{
return|return
name|user
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getDoAsEnabled
parameter_list|()
block|{
return|return
name|this
operator|.
name|doAsEnabled
return|;
block|}
annotation|@
name|Override
specifier|public
name|SettableFuture
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
specifier|final
name|SampleTezSessionState
name|session
init|=
name|this
decl_stmt|;
specifier|final
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
name|Futures
operator|.
name|addCallback
argument_list|(
name|waitForAmRegFuture
argument_list|,
operator|new
name|FutureCallback
argument_list|<
name|Boolean
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|(
name|Boolean
name|result
parameter_list|)
block|{
name|future
operator|.
name|set
argument_list|(
name|session
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|future
operator|.
name|setException
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|future
return|;
block|}
specifier|public
name|void
name|setWaitForAmRegistryFuture
parameter_list|(
name|ListenableFuture
argument_list|<
name|Boolean
argument_list|>
name|future
parameter_list|)
block|{
name|waitForAmRegFuture
operator|=
name|future
operator|!=
literal|null
condition|?
name|future
else|:
name|createDefaultWaitForAmRegistryFuture
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

