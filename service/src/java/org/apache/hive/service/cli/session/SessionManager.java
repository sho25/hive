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
name|io
operator|.
name|File
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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
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
name|BlockingQueue
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
name|io
operator|.
name|FileUtils
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
name|metrics
operator|.
name|common
operator|.
name|Metrics
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
name|metrics
operator|.
name|common
operator|.
name|MetricsConstant
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
name|metrics
operator|.
name|common
operator|.
name|MetricsFactory
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
name|metrics
operator|.
name|common
operator|.
name|MetricsVariable
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
name|Operation
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
name|rpc
operator|.
name|thrift
operator|.
name|TOpenSessionReq
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
name|rpc
operator|.
name|thrift
operator|.
name|TProtocolVersion
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
name|server
operator|.
name|HiveServer2
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
name|server
operator|.
name|ThreadFactoryWithGarbageCleanup
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
comment|/**  * SessionManager.  *  */
end_comment

begin_class
specifier|public
class|class
name|SessionManager
extends|extends
name|CompositeService
block|{
specifier|public
specifier|static
specifier|final
name|String
name|HIVERCFILE
init|=
literal|".hiverc"
decl_stmt|;
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
name|CompositeService
operator|.
name|class
argument_list|)
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
specifier|private
name|boolean
name|isOperationLogEnabled
decl_stmt|;
specifier|private
name|File
name|operationLogRootDir
decl_stmt|;
specifier|private
name|long
name|checkInterval
decl_stmt|;
specifier|private
name|long
name|sessionTimeout
decl_stmt|;
specifier|private
name|boolean
name|checkOperation
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|shutdown
decl_stmt|;
comment|// The HiveServer2 instance running this service
specifier|private
specifier|final
name|HiveServer2
name|hiveServer2
decl_stmt|;
specifier|private
name|String
name|sessionImplWithUGIclassName
decl_stmt|;
specifier|private
name|String
name|sessionImplclassName
decl_stmt|;
specifier|public
name|SessionManager
parameter_list|(
name|HiveServer2
name|hiveServer2
parameter_list|)
block|{
name|super
argument_list|(
name|SessionManager
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|hiveServer2
operator|=
name|hiveServer2
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
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
comment|//Create operation log root directory, if operation logging is enabled
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_LOGGING_OPERATION_ENABLED
argument_list|)
condition|)
block|{
name|initOperationLogRootDir
argument_list|()
expr_stmt|;
block|}
name|createBackgroundOperationPool
argument_list|()
expr_stmt|;
name|addService
argument_list|(
name|operationManager
argument_list|)
expr_stmt|;
name|initSessionImplClassName
argument_list|()
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
name|initSessionImplClassName
parameter_list|()
block|{
name|this
operator|.
name|sessionImplclassName
operator|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SESSION_IMPL_CLASSNAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|sessionImplWithUGIclassName
operator|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SESSION_IMPL_WITH_UGI_CLASSNAME
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createBackgroundOperationPool
parameter_list|()
block|{
name|int
name|poolSize
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
literal|"HiveServer2: Background operation thread pool size: "
operator|+
name|poolSize
argument_list|)
expr_stmt|;
name|int
name|poolQueueSize
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
literal|"HiveServer2: Background operation thread wait queue size: "
operator|+
name|poolQueueSize
argument_list|)
expr_stmt|;
name|long
name|keepAliveTime
init|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|hiveConf
argument_list|,
name|ConfVars
operator|.
name|HIVE_SERVER2_ASYNC_EXEC_KEEPALIVE_TIME
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HiveServer2: Background operation thread keepalive time: "
operator|+
name|keepAliveTime
operator|+
literal|" seconds"
argument_list|)
expr_stmt|;
comment|// Create a thread pool with #poolSize threads
comment|// Threads terminate when they are idle for more than the keepAliveTime
comment|// A bounded blocking queue is used to queue incoming operations, if #operations> poolSize
name|String
name|threadPoolName
init|=
literal|"HiveServer2-Background-Pool"
decl_stmt|;
specifier|final
name|BlockingQueue
name|queue
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|(
name|poolQueueSize
argument_list|)
decl_stmt|;
name|backgroundOperationPool
operator|=
operator|new
name|ThreadPoolExecutor
argument_list|(
name|poolSize
argument_list|,
name|poolSize
argument_list|,
name|keepAliveTime
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|queue
argument_list|,
operator|new
name|ThreadFactoryWithGarbageCleanup
argument_list|(
name|threadPoolName
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
name|checkInterval
operator|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|hiveConf
argument_list|,
name|ConfVars
operator|.
name|HIVE_SERVER2_SESSION_CHECK_INTERVAL
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|sessionTimeout
operator|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|hiveConf
argument_list|,
name|ConfVars
operator|.
name|HIVE_SERVER2_IDLE_SESSION_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|checkOperation
operator|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|ConfVars
operator|.
name|HIVE_SERVER2_IDLE_SESSION_CHECK_OPERATION
argument_list|)
expr_stmt|;
name|Metrics
name|m
init|=
name|MetricsFactory
operator|.
name|getInstance
argument_list|()
decl_stmt|;
if|if
condition|(
name|m
operator|!=
literal|null
condition|)
block|{
name|m
operator|.
name|addGauge
argument_list|(
name|MetricsConstant
operator|.
name|EXEC_ASYNC_QUEUE_SIZE
argument_list|,
operator|new
name|MetricsVariable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|getValue
parameter_list|()
block|{
return|return
name|queue
operator|.
name|size
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|m
operator|.
name|addGauge
argument_list|(
name|MetricsConstant
operator|.
name|EXEC_ASYNC_POOL_SIZE
argument_list|,
operator|new
name|MetricsVariable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|getValue
parameter_list|()
block|{
return|return
name|backgroundOperationPool
operator|.
name|getPoolSize
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|initOperationLogRootDir
parameter_list|()
block|{
name|operationLogRootDir
operator|=
operator|new
name|File
argument_list|(
name|hiveConf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_LOGGING_OPERATION_LOG_LOCATION
argument_list|)
argument_list|)
expr_stmt|;
name|isOperationLogEnabled
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|operationLogRootDir
operator|.
name|exists
argument_list|()
operator|&&
operator|!
name|operationLogRootDir
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"The operation log root directory exists, but it is not a directory: "
operator|+
name|operationLogRootDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|isOperationLogEnabled
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|operationLogRootDir
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|operationLogRootDir
operator|.
name|mkdirs
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to create operation log root directory: "
operator|+
name|operationLogRootDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|isOperationLogEnabled
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
name|isOperationLogEnabled
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Operation log root directory is created: "
operator|+
name|operationLogRootDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|FileUtils
operator|.
name|forceDeleteOnExit
argument_list|(
name|operationLogRootDir
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
name|warn
argument_list|(
literal|"Failed to schedule cleanup HS2 operation logging root dir: "
operator|+
name|operationLogRootDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
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
if|if
condition|(
name|checkInterval
operator|>
literal|0
condition|)
block|{
name|startTimeoutChecker
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|startTimeoutChecker
parameter_list|()
block|{
specifier|final
name|long
name|interval
init|=
name|Math
operator|.
name|max
argument_list|(
name|checkInterval
argument_list|,
literal|3000l
argument_list|)
decl_stmt|;
comment|// minimum 3 seconds
name|Runnable
name|timeoutChecker
init|=
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
for|for
control|(
name|sleepInterval
argument_list|(
name|interval
argument_list|)
init|;
operator|!
name|shutdown
condition|;
name|sleepInterval
argument_list|(
name|interval
argument_list|)
control|)
block|{
name|long
name|current
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
for|for
control|(
name|HiveSession
name|session
range|:
operator|new
name|ArrayList
argument_list|<
name|HiveSession
argument_list|>
argument_list|(
name|handleToSession
operator|.
name|values
argument_list|()
argument_list|)
control|)
block|{
if|if
condition|(
name|sessionTimeout
operator|>
literal|0
operator|&&
name|session
operator|.
name|getLastAccessTime
argument_list|()
operator|+
name|sessionTimeout
operator|<=
name|current
operator|&&
operator|(
operator|!
name|checkOperation
operator|||
name|session
operator|.
name|getNoOperationTime
argument_list|()
operator|>
name|sessionTimeout
operator|)
condition|)
block|{
name|SessionHandle
name|handle
init|=
name|session
operator|.
name|getSessionHandle
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Session "
operator|+
name|handle
operator|+
literal|" is Timed-out (last access : "
operator|+
operator|new
name|Date
argument_list|(
name|session
operator|.
name|getLastAccessTime
argument_list|()
argument_list|)
operator|+
literal|") and will be closed"
argument_list|)
expr_stmt|;
try|try
block|{
name|closeSession
argument_list|(
name|handle
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception is thrown closing session "
operator|+
name|handle
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|session
operator|.
name|closeExpiredOperations
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|sleepInterval
parameter_list|(
name|long
name|interval
parameter_list|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|interval
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
decl_stmt|;
name|backgroundOperationPool
operator|.
name|execute
argument_list|(
name|timeoutChecker
argument_list|)
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
name|shutdown
operator|=
literal|true
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
name|long
name|timeout
init|=
name|hiveConf
operator|.
name|getTimeVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ASYNC_EXEC_SHUTDOWN_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
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
name|backgroundOperationPool
operator|=
literal|null
expr_stmt|;
block|}
name|cleanupLoggingRootDir
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|cleanupLoggingRootDir
parameter_list|()
block|{
if|if
condition|(
name|isOperationLogEnabled
condition|)
block|{
try|try
block|{
name|FileUtils
operator|.
name|forceDelete
argument_list|(
name|operationLogRootDir
argument_list|)
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
name|warn
argument_list|(
literal|"Failed to cleanup root dir of HS2 logging: "
operator|+
name|operationLogRootDir
operator|.
name|getAbsolutePath
argument_list|()
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
comment|/**    * Opens a new session and creates a session handle.    * The username passed to this method is the effective username.    * If withImpersonation is true (==doAs true) we wrap all the calls in HiveSession    * within a UGI.doAs, where UGI corresponds to the effective user.    * @see org.apache.hive.service.cli.thrift.ThriftCLIService#getUserName(TOpenSessionReq)    *    * @param protocol    * @param username    * @param password    * @param ipAddress    * @param sessionConf    * @param withImpersonation    * @param delegationToken    * @return    * @throws HiveSQLException    */
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
return|return
name|createSession
argument_list|(
literal|null
argument_list|,
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
name|withImpersonation
argument_list|,
name|delegationToken
argument_list|)
operator|.
name|getSessionHandle
argument_list|()
return|;
block|}
specifier|public
name|HiveSession
name|createSession
parameter_list|(
name|SessionHandle
name|sessionHandle
parameter_list|,
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
comment|// If doAs is set to true for HiveServer2, we will create a proxy object for the session impl.
comment|// Within the proxy object, we wrap the method call in a UserGroupInformation#doAs
if|if
condition|(
name|withImpersonation
condition|)
block|{
name|HiveSessionImplwithUGI
name|hiveSessionUgi
decl_stmt|;
if|if
condition|(
name|sessionImplWithUGIclassName
operator|==
literal|null
condition|)
block|{
name|hiveSessionUgi
operator|=
operator|new
name|HiveSessionImplwithUGI
argument_list|(
name|sessionHandle
argument_list|,
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
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|Class
operator|.
name|forName
argument_list|(
name|sessionImplWithUGIclassName
argument_list|)
decl_stmt|;
name|Constructor
argument_list|<
name|?
argument_list|>
name|constructor
init|=
name|clazz
operator|.
name|getConstructor
argument_list|(
name|SessionHandle
operator|.
name|class
argument_list|,
name|TProtocolVersion
operator|.
name|class
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|HiveConf
operator|.
name|class
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
name|hiveSessionUgi
operator|=
operator|(
name|HiveSessionImplwithUGI
operator|)
name|constructor
operator|.
name|newInstance
argument_list|(
name|sessionHandle
argument_list|,
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
literal|"Cannot initilize session class:"
operator|+
name|sessionImplWithUGIclassName
argument_list|)
throw|;
block|}
block|}
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
if|if
condition|(
name|sessionImplclassName
operator|==
literal|null
condition|)
block|{
name|session
operator|=
operator|new
name|HiveSessionImpl
argument_list|(
name|sessionHandle
argument_list|,
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
else|else
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|Class
operator|.
name|forName
argument_list|(
name|sessionImplclassName
argument_list|)
decl_stmt|;
name|Constructor
argument_list|<
name|?
argument_list|>
name|constructor
init|=
name|clazz
operator|.
name|getConstructor
argument_list|(
name|SessionHandle
operator|.
name|class
argument_list|,
name|TProtocolVersion
operator|.
name|class
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|HiveConf
operator|.
name|class
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
name|session
operator|=
operator|(
name|HiveSession
operator|)
name|constructor
operator|.
name|newInstance
argument_list|(
name|sessionHandle
argument_list|,
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
literal|"Cannot initilize session class:"
operator|+
name|sessionImplclassName
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
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
try|try
block|{
name|session
operator|.
name|open
argument_list|(
name|sessionConf
argument_list|)
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
name|warn
argument_list|(
literal|"Failed to open session"
argument_list|,
name|e
argument_list|)
expr_stmt|;
try|try
block|{
name|session
operator|.
name|close
argument_list|()
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
name|warn
argument_list|(
literal|"Error closing session"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
name|session
operator|=
literal|null
expr_stmt|;
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Failed to open new session: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|isOperationLogEnabled
condition|)
block|{
name|session
operator|.
name|setOperationLogSessionDir
argument_list|(
name|operationLogRootDir
argument_list|)
expr_stmt|;
block|}
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
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to execute session hooks"
argument_list|,
name|e
argument_list|)
expr_stmt|;
try|try
block|{
name|session
operator|.
name|close
argument_list|()
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
name|warn
argument_list|(
literal|"Error closing session"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
name|session
operator|=
literal|null
expr_stmt|;
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Failed to execute session hooks: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
return|return
name|session
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
literal|"Session does not exist: "
operator|+
name|sessionHandle
argument_list|)
throw|;
block|}
try|try
block|{
name|session
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
comment|// Shutdown HiveServer2 if it has been deregistered from ZooKeeper and has no active sessions
if|if
condition|(
operator|!
operator|(
name|hiveServer2
operator|==
literal|null
operator|)
operator|&&
operator|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_SUPPORT_DYNAMIC_SERVICE_DISCOVERY
argument_list|)
operator|)
operator|&&
operator|(
operator|!
name|hiveServer2
operator|.
name|isRegisteredWithZooKeeper
argument_list|()
operator|)
condition|)
block|{
comment|// Asynchronously shutdown this instance of HiveServer2,
comment|// if there are no active client sessions
if|if
condition|(
name|getOpenSessionCount
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"This instance of HiveServer2 has been removed from the list of server "
operator|+
literal|"instances available for dynamic service discovery. "
operator|+
literal|"The last client session has ended - will shutdown now."
argument_list|)
expr_stmt|;
name|Thread
name|shutdownThread
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|hiveServer2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|shutdownThread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
block|}
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
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|threadLocalForwardedAddresses
init|=
operator|new
name|ThreadLocal
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|void
name|setForwardedAddresses
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|ipAddress
parameter_list|)
block|{
name|threadLocalForwardedAddresses
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
name|clearForwardedAddresses
parameter_list|()
block|{
name|threadLocalForwardedAddresses
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getForwardedAddresses
parameter_list|()
block|{
return|return
name|threadLocalForwardedAddresses
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
specifier|public
name|Collection
argument_list|<
name|Operation
argument_list|>
name|getOperations
parameter_list|()
block|{
return|return
name|operationManager
operator|.
name|getOperations
argument_list|()
return|;
block|}
specifier|public
name|Collection
argument_list|<
name|HiveSession
argument_list|>
name|getSessions
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableCollection
argument_list|(
name|handleToSession
operator|.
name|values
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|int
name|getOpenSessionCount
parameter_list|()
block|{
return|return
name|handleToSession
operator|.
name|size
argument_list|()
return|;
block|}
block|}
end_class

end_unit

