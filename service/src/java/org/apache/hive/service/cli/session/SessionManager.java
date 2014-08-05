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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|session
package|;
end_package

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
name|ConcurrentHashMap
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
name|LinkedBlockingQueue
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
name|ThreadPoolExecutor
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
name|ql
operator|.
name|hooks
operator|.
name|HookUtils
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
name|metadata
operator|.
name|HiveException
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
name|session
operator|.
name|SessionState
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
name|service
operator|.
name|CompositeService
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
name|service
operator|.
name|auth
operator|.
name|TSetIpAddressProcessor
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
name|service
operator|.
name|cli
operator|.
name|HiveSQLException
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
name|service
operator|.
name|cli
operator|.
name|SessionHandle
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
name|service
operator|.
name|cli
operator|.
name|operation
operator|.
name|OperationManager
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
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TProtocolVersion
import|;
end_import

begin_comment
comment|/**  * SessionManager.  *  */
end_comment

begin_class
specifier|public
class|class
name|SessionManager
extends|extends
name|CompositeService
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CompositeService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HIVERCFILE
init|=
literal|".hiverc"
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|SessionHandle
argument_list|,
name|HiveSession
argument_list|>
name|handleToSession
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|SessionHandle
argument_list|,
name|HiveSession
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|OperationManager
name|operationManager
init|=
operator|new
name|OperationManager
argument_list|()
decl_stmt|;
specifier|private
name|ThreadPoolExecutor
name|backgroundOperationPool
decl_stmt|;
specifier|public
name|SessionManager
parameter_list|()
block|{
name|super
argument_list|(
literal|"SessionManager"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|init
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
try|try
block|{
name|applyAuthorizationConfigPolicy
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error applying authorization policy on hive configuration"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|int
name|backgroundPoolSize
init|=
name|hiveConf
operator|.
name|getIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ASYNC_EXEC_THREADS
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HiveServer2: Async execution thread pool size: "
operator|+
name|backgroundPoolSize
argument_list|)
expr_stmt|;
name|int
name|backgroundPoolQueueSize
init|=
name|hiveConf
operator|.
name|getIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ASYNC_EXEC_WAIT_QUEUE_SIZE
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HiveServer2: Async execution wait queue size: "
operator|+
name|backgroundPoolQueueSize
argument_list|)
expr_stmt|;
name|int
name|keepAliveTime
init|=
name|hiveConf
operator|.
name|getIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ASYNC_EXEC_KEEPALIVE_TIME
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HiveServer2: Async execution thread keepalive time: "
operator|+
name|keepAliveTime
argument_list|)
expr_stmt|;
comment|// Create a thread pool with #backgroundPoolSize threads
comment|// Threads terminate when they are idle for more than the keepAliveTime
comment|// An bounded blocking queue is used to queue incoming operations, if #operations> backgroundPoolSize
name|backgroundOperationPool
operator|=
operator|new
name|ThreadPoolExecutor
argument_list|(
name|backgroundPoolSize
argument_list|,
name|backgroundPoolSize
argument_list|,
name|keepAliveTime
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|new
name|LinkedBlockingQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|(
name|backgroundPoolQueueSize
argument_list|)
argument_list|)
expr_stmt|;
name|backgroundOperationPool
operator|.
name|allowCoreThreadTimeOut
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|addService
argument_list|(
name|operationManager
argument_list|)
expr_stmt|;
name|super
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|applyAuthorizationConfigPolicy
parameter_list|(
name|HiveConf
name|newHiveConf
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// authorization setup using SessionState should be revisited eventually, as
comment|// authorization and authentication are not session specific settings
name|SessionState
name|ss
init|=
operator|new
name|SessionState
argument_list|(
name|newHiveConf
argument_list|)
decl_stmt|;
name|ss
operator|.
name|setIsHiveServerQuery
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|ss
operator|.
name|applyAuthorizationPolicy
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|start
parameter_list|()
block|{
name|super
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|stop
parameter_list|()
block|{
name|super
operator|.
name|stop
argument_list|()
expr_stmt|;
if|if
condition|(
name|backgroundOperationPool
operator|!=
literal|null
condition|)
block|{
name|backgroundOperationPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|int
name|timeout
init|=
name|hiveConf
operator|.
name|getIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ASYNC_EXEC_SHUTDOWN_TIMEOUT
argument_list|)
decl_stmt|;
try|try
block|{
name|backgroundOperationPool
operator|.
name|awaitTermination
argument_list|(
name|timeout
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
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
name|warn
argument_list|(
literal|"HIVE_SERVER2_ASYNC_EXEC_SHUTDOWN_TIMEOUT = "
operator|+
name|timeout
operator|+
literal|" seconds has been exceeded. RUNNING background operations will be shut down"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|SessionHandle
name|openSession
parameter_list|(
name|TProtocolVersion
name|protocol
parameter_list|,
name|String
name|username
parameter_list|,
name|String
name|password
parameter_list|,
name|String
name|ipAddress
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sessionConf
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|openSession
argument_list|(
name|protocol
argument_list|,
name|username
argument_list|,
name|password
argument_list|,
name|ipAddress
argument_list|,
name|sessionConf
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
name|SessionHandle
name|openSession
parameter_list|(
name|TProtocolVersion
name|protocol
parameter_list|,
name|String
name|username
parameter_list|,
name|String
name|password
parameter_list|,
name|String
name|ipAddress
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sessionConf
parameter_list|,
name|boolean
name|withImpersonation
parameter_list|,
name|String
name|delegationToken
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|HiveSession
name|session
decl_stmt|;
if|if
condition|(
name|withImpersonation
condition|)
block|{
name|HiveSessionImplwithUGI
name|hiveSessionUgi
init|=
operator|new
name|HiveSessionImplwithUGI
argument_list|(
name|protocol
argument_list|,
name|username
argument_list|,
name|password
argument_list|,
name|hiveConf
argument_list|,
name|ipAddress
argument_list|,
name|delegationToken
argument_list|)
decl_stmt|;
name|session
operator|=
name|HiveSessionProxy
operator|.
name|getProxy
argument_list|(
name|hiveSessionUgi
argument_list|,
name|hiveSessionUgi
operator|.
name|getSessionUgi
argument_list|()
argument_list|)
expr_stmt|;
name|hiveSessionUgi
operator|.
name|setProxySession
argument_list|(
name|session
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|session
operator|=
operator|new
name|HiveSessionImpl
argument_list|(
name|protocol
argument_list|,
name|username
argument_list|,
name|password
argument_list|,
name|hiveConf
argument_list|,
name|ipAddress
argument_list|)
expr_stmt|;
block|}
name|session
operator|.
name|setSessionManager
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|session
operator|.
name|setOperationManager
argument_list|(
name|operationManager
argument_list|)
expr_stmt|;
name|session
operator|.
name|initialize
argument_list|(
name|sessionConf
argument_list|)
expr_stmt|;
name|session
operator|.
name|open
argument_list|()
expr_stmt|;
name|handleToSession
operator|.
name|put
argument_list|(
name|session
operator|.
name|getSessionHandle
argument_list|()
argument_list|,
name|session
argument_list|)
expr_stmt|;
try|try
block|{
name|executeSessionHooks
argument_list|(
name|session
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Failed to execute session hooks"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|session
operator|.
name|getSessionHandle
argument_list|()
return|;
block|}
specifier|public
name|void
name|closeSession
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|HiveSession
name|session
init|=
name|handleToSession
operator|.
name|remove
argument_list|(
name|sessionHandle
argument_list|)
decl_stmt|;
if|if
condition|(
name|session
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Session does not exist!"
argument_list|)
throw|;
block|}
name|session
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|HiveSession
name|getSession
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|HiveSession
name|session
init|=
name|handleToSession
operator|.
name|get
argument_list|(
name|sessionHandle
argument_list|)
decl_stmt|;
if|if
condition|(
name|session
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Invalid SessionHandle: "
operator|+
name|sessionHandle
argument_list|)
throw|;
block|}
return|return
name|session
return|;
block|}
specifier|public
name|OperationManager
name|getOperationManager
parameter_list|()
block|{
return|return
name|operationManager
return|;
block|}
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|String
argument_list|>
name|threadLocalIpAddress
init|=
operator|new
name|ThreadLocal
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|String
name|initialValue
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
name|void
name|setIpAddress
parameter_list|(
name|String
name|ipAddress
parameter_list|)
block|{
name|threadLocalIpAddress
operator|.
name|set
argument_list|(
name|ipAddress
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|clearIpAddress
parameter_list|()
block|{
name|threadLocalIpAddress
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|String
name|getIpAddress
parameter_list|()
block|{
return|return
name|threadLocalIpAddress
operator|.
name|get
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|String
argument_list|>
name|threadLocalUserName
init|=
operator|new
name|ThreadLocal
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|String
name|initialValue
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
name|void
name|setUserName
parameter_list|(
name|String
name|userName
parameter_list|)
block|{
name|threadLocalUserName
operator|.
name|set
argument_list|(
name|userName
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|clearUserName
parameter_list|()
block|{
name|threadLocalUserName
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|String
name|getUserName
parameter_list|()
block|{
return|return
name|threadLocalUserName
operator|.
name|get
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|String
argument_list|>
name|threadLocalProxyUserName
init|=
operator|new
name|ThreadLocal
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|String
name|initialValue
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
name|void
name|setProxyUserName
parameter_list|(
name|String
name|userName
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"setting proxy user name based on query param to: "
operator|+
name|userName
argument_list|)
expr_stmt|;
name|threadLocalProxyUserName
operator|.
name|set
argument_list|(
name|userName
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|String
name|getProxyUserName
parameter_list|()
block|{
return|return
name|threadLocalProxyUserName
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|void
name|clearProxyUserName
parameter_list|()
block|{
name|threadLocalProxyUserName
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
comment|// execute session hooks
specifier|private
name|void
name|executeSessionHooks
parameter_list|(
name|HiveSession
name|session
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|HiveSessionHook
argument_list|>
name|sessionHooks
init|=
name|HookUtils
operator|.
name|getHooks
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_SESSION_HOOK
argument_list|,
name|HiveSessionHook
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|HiveSessionHook
name|sessionHook
range|:
name|sessionHooks
control|)
block|{
name|sessionHook
operator|.
name|run
argument_list|(
operator|new
name|HiveSessionHookContextImpl
argument_list|(
name|session
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Future
argument_list|<
name|?
argument_list|>
name|submitBackgroundOperation
parameter_list|(
name|Runnable
name|r
parameter_list|)
block|{
return|return
name|backgroundOperationPool
operator|.
name|submit
argument_list|(
name|r
argument_list|)
return|;
block|}
block|}
end_class

end_unit

