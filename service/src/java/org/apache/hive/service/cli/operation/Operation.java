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
name|operation
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
name|FileNotFoundException
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
name|EnumSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Set
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
name|MetricsScope
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
name|QueryState
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
name|processors
operator|.
name|CommandProcessorResponse
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
name|OperationLog
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
name|cli
operator|.
name|FetchOrientation
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
name|OperationHandle
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
name|OperationState
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
name|OperationStatus
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
name|OperationType
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
name|RowSet
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
name|TableSchema
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
name|session
operator|.
name|HiveSession
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
name|logging
operator|.
name|log4j
operator|.
name|ThreadContext
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
name|collect
operator|.
name|Sets
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|Operation
block|{
comment|// Constants of the key strings for the log4j ThreadContext.
specifier|public
specifier|static
specifier|final
name|String
name|SESSIONID_LOG_KEY
init|=
literal|"sessionId"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|QUERYID_LOG_KEY
init|=
literal|"queryId"
decl_stmt|;
specifier|protected
specifier|final
name|HiveSession
name|parentSession
decl_stmt|;
specifier|private
specifier|volatile
name|OperationState
name|state
init|=
name|OperationState
operator|.
name|INITIALIZED
decl_stmt|;
specifier|private
specifier|volatile
name|MetricsScope
name|currentStateScope
decl_stmt|;
specifier|private
specifier|final
name|OperationHandle
name|opHandle
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|FetchOrientation
name|DEFAULT_FETCH_ORIENTATION
init|=
name|FetchOrientation
operator|.
name|FETCH_NEXT
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|Operation
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_FETCH_MAX_ROWS
init|=
literal|100
decl_stmt|;
specifier|protected
name|boolean
name|hasResultSet
decl_stmt|;
specifier|protected
specifier|volatile
name|HiveSQLException
name|operationException
decl_stmt|;
specifier|protected
specifier|final
name|boolean
name|runAsync
decl_stmt|;
specifier|protected
specifier|volatile
name|Future
argument_list|<
name|?
argument_list|>
name|backgroundHandle
decl_stmt|;
specifier|protected
name|OperationLog
name|operationLog
decl_stmt|;
specifier|protected
name|boolean
name|isOperationLogEnabled
decl_stmt|;
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|long
name|operationTimeout
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|lastAccessTime
decl_stmt|;
specifier|private
specifier|final
name|long
name|beginTime
decl_stmt|;
specifier|protected
name|long
name|operationStart
decl_stmt|;
specifier|protected
name|long
name|operationComplete
decl_stmt|;
specifier|protected
specifier|final
name|QueryState
name|queryState
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|EnumSet
argument_list|<
name|FetchOrientation
argument_list|>
name|DEFAULT_FETCH_ORIENTATION_SET
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|FetchOrientation
operator|.
name|FETCH_NEXT
argument_list|,
name|FetchOrientation
operator|.
name|FETCH_FIRST
argument_list|)
decl_stmt|;
specifier|protected
name|Operation
parameter_list|(
name|HiveSession
name|parentSession
parameter_list|,
name|OperationType
name|opType
parameter_list|,
name|boolean
name|runInBackground
parameter_list|)
block|{
name|this
argument_list|(
name|parentSession
argument_list|,
literal|null
argument_list|,
name|opType
argument_list|,
name|runInBackground
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|Operation
parameter_list|(
name|HiveSession
name|parentSession
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
parameter_list|,
name|OperationType
name|opType
parameter_list|,
name|boolean
name|runInBackground
parameter_list|)
block|{
name|this
operator|.
name|parentSession
operator|=
name|parentSession
expr_stmt|;
if|if
condition|(
name|confOverlay
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|confOverlay
operator|=
name|confOverlay
expr_stmt|;
block|}
name|this
operator|.
name|runAsync
operator|=
name|runInBackground
expr_stmt|;
name|this
operator|.
name|opHandle
operator|=
operator|new
name|OperationHandle
argument_list|(
name|opType
argument_list|,
name|parentSession
operator|.
name|getProtocolVersion
argument_list|()
argument_list|)
expr_stmt|;
name|beginTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|lastAccessTime
operator|=
name|beginTime
expr_stmt|;
name|operationTimeout
operator|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|parentSession
operator|.
name|getHiveConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_IDLE_OPERATION_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|setMetrics
argument_list|(
name|state
argument_list|)
expr_stmt|;
name|queryState
operator|=
operator|new
name|QueryState
argument_list|(
name|parentSession
operator|.
name|getHiveConf
argument_list|()
argument_list|,
name|confOverlay
argument_list|,
name|runAsync
argument_list|)
expr_stmt|;
block|}
specifier|public
name|QueryState
name|getQueryState
parameter_list|()
block|{
return|return
name|queryState
return|;
block|}
specifier|public
name|Future
argument_list|<
name|?
argument_list|>
name|getBackgroundHandle
parameter_list|()
block|{
return|return
name|backgroundHandle
return|;
block|}
specifier|protected
name|void
name|setBackgroundHandle
parameter_list|(
name|Future
argument_list|<
name|?
argument_list|>
name|backgroundHandle
parameter_list|)
block|{
name|this
operator|.
name|backgroundHandle
operator|=
name|backgroundHandle
expr_stmt|;
block|}
specifier|public
name|boolean
name|shouldRunAsync
parameter_list|()
block|{
return|return
name|runAsync
return|;
block|}
specifier|public
name|HiveSession
name|getParentSession
parameter_list|()
block|{
return|return
name|parentSession
return|;
block|}
specifier|public
name|OperationHandle
name|getHandle
parameter_list|()
block|{
return|return
name|opHandle
return|;
block|}
specifier|public
name|TProtocolVersion
name|getProtocolVersion
parameter_list|()
block|{
return|return
name|opHandle
operator|.
name|getProtocolVersion
argument_list|()
return|;
block|}
specifier|public
name|OperationType
name|getType
parameter_list|()
block|{
return|return
name|opHandle
operator|.
name|getOperationType
argument_list|()
return|;
block|}
specifier|public
name|OperationStatus
name|getStatus
parameter_list|()
block|{
name|String
name|taskStatus
init|=
literal|null
decl_stmt|;
try|try
block|{
name|taskStatus
operator|=
name|getTaskStatus
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|sqlException
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error getting task status for "
operator|+
name|opHandle
operator|.
name|toString
argument_list|()
argument_list|,
name|sqlException
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|OperationStatus
argument_list|(
name|state
argument_list|,
name|taskStatus
argument_list|,
name|operationStart
argument_list|,
name|operationComplete
argument_list|,
name|operationException
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|hasResultSet
parameter_list|()
block|{
return|return
name|hasResultSet
return|;
block|}
specifier|protected
name|void
name|setHasResultSet
parameter_list|(
name|boolean
name|hasResultSet
parameter_list|)
block|{
name|this
operator|.
name|hasResultSet
operator|=
name|hasResultSet
expr_stmt|;
name|opHandle
operator|.
name|setHasResultSet
argument_list|(
name|hasResultSet
argument_list|)
expr_stmt|;
block|}
specifier|public
name|OperationLog
name|getOperationLog
parameter_list|()
block|{
return|return
name|operationLog
return|;
block|}
specifier|protected
specifier|final
name|OperationState
name|setState
parameter_list|(
name|OperationState
name|newState
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|state
operator|.
name|validateTransition
argument_list|(
name|newState
argument_list|)
expr_stmt|;
name|OperationState
name|prevState
init|=
name|state
decl_stmt|;
name|this
operator|.
name|state
operator|=
name|newState
expr_stmt|;
name|setMetrics
argument_list|(
name|state
argument_list|)
expr_stmt|;
name|onNewState
argument_list|(
name|state
argument_list|,
name|prevState
argument_list|)
expr_stmt|;
name|this
operator|.
name|lastAccessTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
return|return
name|this
operator|.
name|state
return|;
block|}
specifier|public
name|boolean
name|isTimedOut
parameter_list|(
name|long
name|current
parameter_list|)
block|{
if|if
condition|(
name|operationTimeout
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|operationTimeout
operator|>
literal|0
condition|)
block|{
comment|// check only when it's in terminal state
return|return
name|state
operator|.
name|isTerminal
argument_list|()
operator|&&
name|lastAccessTime
operator|+
name|operationTimeout
operator|<=
name|current
return|;
block|}
return|return
name|lastAccessTime
operator|+
operator|-
name|operationTimeout
operator|<=
name|current
return|;
block|}
specifier|public
name|long
name|getLastAccessTime
parameter_list|()
block|{
return|return
name|lastAccessTime
return|;
block|}
specifier|public
name|long
name|getOperationTimeout
parameter_list|()
block|{
return|return
name|operationTimeout
return|;
block|}
specifier|public
name|void
name|setOperationTimeout
parameter_list|(
name|long
name|operationTimeout
parameter_list|)
block|{
name|this
operator|.
name|operationTimeout
operator|=
name|operationTimeout
expr_stmt|;
block|}
specifier|protected
name|void
name|setOperationException
parameter_list|(
name|HiveSQLException
name|operationException
parameter_list|)
block|{
name|this
operator|.
name|operationException
operator|=
name|operationException
expr_stmt|;
block|}
specifier|protected
specifier|final
name|void
name|assertState
parameter_list|(
name|List
argument_list|<
name|OperationState
argument_list|>
name|states
parameter_list|)
throws|throws
name|HiveSQLException
block|{
if|if
condition|(
operator|!
name|states
operator|.
name|contains
argument_list|(
name|state
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Expected states: "
operator|+
name|states
operator|.
name|toString
argument_list|()
operator|+
literal|", but found "
operator|+
name|this
operator|.
name|state
argument_list|)
throw|;
block|}
name|this
operator|.
name|lastAccessTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|isRunning
parameter_list|()
block|{
return|return
name|OperationState
operator|.
name|RUNNING
operator|.
name|equals
argument_list|(
name|state
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isFinished
parameter_list|()
block|{
return|return
name|OperationState
operator|.
name|FINISHED
operator|.
name|equals
argument_list|(
name|state
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isCanceled
parameter_list|()
block|{
return|return
name|OperationState
operator|.
name|CANCELED
operator|.
name|equals
argument_list|(
name|state
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isFailed
parameter_list|()
block|{
return|return
name|OperationState
operator|.
name|ERROR
operator|.
name|equals
argument_list|(
name|state
argument_list|)
return|;
block|}
specifier|protected
name|void
name|createOperationLog
parameter_list|()
block|{
if|if
condition|(
name|parentSession
operator|.
name|isOperationLogEnabled
argument_list|()
condition|)
block|{
name|File
name|operationLogFile
init|=
operator|new
name|File
argument_list|(
name|parentSession
operator|.
name|getOperationLogSessionDir
argument_list|()
argument_list|,
name|opHandle
operator|.
name|getHandleIdentifier
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|isOperationLogEnabled
operator|=
literal|true
expr_stmt|;
comment|// create log file
try|try
block|{
if|if
condition|(
name|operationLogFile
operator|.
name|exists
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"The operation log file should not exist, but it is already there: "
operator|+
name|operationLogFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|operationLogFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|operationLogFile
operator|.
name|createNewFile
argument_list|()
condition|)
block|{
comment|// the log file already exists and cannot be deleted.
comment|// If it can be read/written, keep its contents and use it.
if|if
condition|(
operator|!
name|operationLogFile
operator|.
name|canRead
argument_list|()
operator|||
operator|!
name|operationLogFile
operator|.
name|canWrite
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"The already existed operation log file cannot be recreated, "
operator|+
literal|"and it cannot be read or written: "
operator|+
name|operationLogFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|isOperationLogEnabled
operator|=
literal|false
expr_stmt|;
return|return;
block|}
block|}
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
literal|"Unable to create operation log file: "
operator|+
name|operationLogFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|isOperationLogEnabled
operator|=
literal|false
expr_stmt|;
return|return;
block|}
comment|// create OperationLog object with above log file
try|try
block|{
name|operationLog
operator|=
operator|new
name|OperationLog
argument_list|(
name|opHandle
operator|.
name|toString
argument_list|()
argument_list|,
name|operationLogFile
argument_list|,
name|parentSession
operator|.
name|getHiveConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to instantiate OperationLog object for operation: "
operator|+
name|opHandle
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|isOperationLogEnabled
operator|=
literal|false
expr_stmt|;
return|return;
block|}
comment|// register this operationLog to current thread
name|OperationLog
operator|.
name|setCurrentOperationLog
argument_list|(
name|operationLog
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|unregisterOperationLog
parameter_list|()
block|{
if|if
condition|(
name|isOperationLogEnabled
condition|)
block|{
name|OperationLog
operator|.
name|removeCurrentOperationLog
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Invoked before runInternal().    * Set up some preconditions, or configurations.    */
specifier|protected
name|void
name|beforeRun
parameter_list|()
block|{
name|createOperationLog
argument_list|()
expr_stmt|;
name|registerLoggingContext
argument_list|()
expr_stmt|;
block|}
comment|/**    * Register logging context so that Log4J can print QueryId and/or SessionId for each message    */
specifier|protected
name|void
name|registerLoggingContext
parameter_list|()
block|{
name|ThreadContext
operator|.
name|put
argument_list|(
name|SESSIONID_LOG_KEY
argument_list|,
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getSessionId
argument_list|()
argument_list|)
expr_stmt|;
name|ThreadContext
operator|.
name|put
argument_list|(
name|QUERYID_LOG_KEY
argument_list|,
name|confOverlay
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
operator|.
name|varname
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Unregister logging context    */
specifier|protected
name|void
name|unregisterLoggingContext
parameter_list|()
block|{
name|ThreadContext
operator|.
name|clearAll
argument_list|()
expr_stmt|;
block|}
comment|/**    * Invoked after runInternal(), even if an exception is thrown in runInternal().    * Clean up resources, which was set up in beforeRun().    */
specifier|protected
name|void
name|afterRun
parameter_list|()
block|{
name|unregisterLoggingContext
argument_list|()
expr_stmt|;
name|unregisterOperationLog
argument_list|()
expr_stmt|;
block|}
comment|/**    * Implemented by subclass of Operation class to execute specific behaviors.    * @throws HiveSQLException    */
specifier|protected
specifier|abstract
name|void
name|runInternal
parameter_list|()
throws|throws
name|HiveSQLException
function_decl|;
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|beforeRun
argument_list|()
expr_stmt|;
try|try
block|{
name|Metrics
name|metrics
init|=
name|MetricsFactory
operator|.
name|getInstance
argument_list|()
decl_stmt|;
if|if
condition|(
name|metrics
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|metrics
operator|.
name|incrementCounter
argument_list|(
name|MetricsConstant
operator|.
name|OPEN_OPERATIONS
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
literal|"Error Reporting open operation to Metrics system"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|runInternal
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|afterRun
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|cleanupOperationLog
parameter_list|()
block|{
if|if
condition|(
name|isOperationLogEnabled
condition|)
block|{
if|if
condition|(
name|operationLog
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Operation [ "
operator|+
name|opHandle
operator|.
name|getHandleIdentifier
argument_list|()
operator|+
literal|" ] "
operator|+
literal|"logging is enabled, but its OperationLog object cannot be found."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|operationLog
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|// TODO: make this abstract and implement in subclasses.
specifier|public
name|void
name|cancel
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|CANCELED
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"SQLOperation.cancel()"
argument_list|)
throw|;
block|}
specifier|public
specifier|abstract
name|void
name|close
parameter_list|()
throws|throws
name|HiveSQLException
function_decl|;
specifier|public
specifier|abstract
name|TableSchema
name|getResultSetSchema
parameter_list|()
throws|throws
name|HiveSQLException
function_decl|;
specifier|public
specifier|abstract
name|RowSet
name|getNextRowSet
parameter_list|(
name|FetchOrientation
name|orientation
parameter_list|,
name|long
name|maxRows
parameter_list|)
throws|throws
name|HiveSQLException
function_decl|;
specifier|public
name|RowSet
name|getNextRowSet
parameter_list|()
throws|throws
name|HiveSQLException
block|{
return|return
name|getNextRowSet
argument_list|(
name|FetchOrientation
operator|.
name|FETCH_NEXT
argument_list|,
name|DEFAULT_FETCH_MAX_ROWS
argument_list|)
return|;
block|}
specifier|public
name|String
name|getTaskStatus
parameter_list|()
throws|throws
name|HiveSQLException
block|{
return|return
literal|null
return|;
block|}
comment|/**    * Verify if the given fetch orientation is part of the default orientation types.    * @param orientation    * @throws HiveSQLException    */
specifier|protected
name|void
name|validateDefaultFetchOrientation
parameter_list|(
name|FetchOrientation
name|orientation
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|validateFetchOrientation
argument_list|(
name|orientation
argument_list|,
name|DEFAULT_FETCH_ORIENTATION_SET
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify if the given fetch orientation is part of the supported orientation types.    * @param orientation    * @param supportedOrientations    * @throws HiveSQLException    */
specifier|protected
name|void
name|validateFetchOrientation
parameter_list|(
name|FetchOrientation
name|orientation
parameter_list|,
name|EnumSet
argument_list|<
name|FetchOrientation
argument_list|>
name|supportedOrientations
parameter_list|)
throws|throws
name|HiveSQLException
block|{
if|if
condition|(
operator|!
name|supportedOrientations
operator|.
name|contains
argument_list|(
name|orientation
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"The fetch type "
operator|+
name|orientation
operator|.
name|toString
argument_list|()
operator|+
literal|" is not supported for this resultset"
argument_list|,
literal|"HY106"
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|HiveSQLException
name|toSQLException
parameter_list|(
name|String
name|prefix
parameter_list|,
name|CommandProcessorResponse
name|response
parameter_list|)
block|{
name|HiveSQLException
name|ex
init|=
operator|new
name|HiveSQLException
argument_list|(
name|prefix
operator|+
literal|": "
operator|+
name|response
operator|.
name|getErrorMessage
argument_list|()
argument_list|,
name|response
operator|.
name|getSQLState
argument_list|()
argument_list|,
name|response
operator|.
name|getResponseCode
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|getException
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|ex
operator|.
name|initCause
argument_list|(
name|response
operator|.
name|getException
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ex
return|;
block|}
comment|//list of operation states to measure duration of.
specifier|protected
specifier|static
name|Set
argument_list|<
name|OperationState
argument_list|>
name|scopeStates
init|=
name|Sets
operator|.
name|immutableEnumSet
argument_list|(
name|OperationState
operator|.
name|INITIALIZED
argument_list|,
name|OperationState
operator|.
name|PENDING
argument_list|,
name|OperationState
operator|.
name|RUNNING
argument_list|)
decl_stmt|;
comment|//list of terminal operation states.  We measure only completed counts for operations in these states.
specifier|protected
specifier|static
name|Set
argument_list|<
name|OperationState
argument_list|>
name|terminalStates
init|=
name|Sets
operator|.
name|immutableEnumSet
argument_list|(
name|OperationState
operator|.
name|CLOSED
argument_list|,
name|OperationState
operator|.
name|CANCELED
argument_list|,
name|OperationState
operator|.
name|FINISHED
argument_list|,
name|OperationState
operator|.
name|ERROR
argument_list|,
name|OperationState
operator|.
name|UNKNOWN
argument_list|)
decl_stmt|;
specifier|private
name|void
name|setMetrics
parameter_list|(
name|OperationState
name|state
parameter_list|)
block|{
name|currentStateScope
operator|=
name|setMetrics
argument_list|(
name|currentStateScope
argument_list|,
name|MetricsConstant
operator|.
name|OPERATION_PREFIX
argument_list|,
name|MetricsConstant
operator|.
name|COMPLETED_OPERATION_PREFIX
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|MetricsScope
name|setMetrics
parameter_list|(
name|MetricsScope
name|stateScope
parameter_list|,
name|String
name|operationPrefix
parameter_list|,
name|String
name|completedOperationPrefix
parameter_list|,
name|OperationState
name|state
parameter_list|)
block|{
name|Metrics
name|metrics
init|=
name|MetricsFactory
operator|.
name|getInstance
argument_list|()
decl_stmt|;
if|if
condition|(
name|metrics
operator|!=
literal|null
condition|)
block|{
try|try
block|{
if|if
condition|(
name|stateScope
operator|!=
literal|null
condition|)
block|{
name|metrics
operator|.
name|endScope
argument_list|(
name|stateScope
argument_list|)
expr_stmt|;
name|stateScope
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|scopeStates
operator|.
name|contains
argument_list|(
name|state
argument_list|)
condition|)
block|{
name|stateScope
operator|=
name|metrics
operator|.
name|createScope
argument_list|(
name|operationPrefix
operator|+
name|state
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|terminalStates
operator|.
name|contains
argument_list|(
name|state
argument_list|)
condition|)
block|{
name|metrics
operator|.
name|incrementCounter
argument_list|(
name|completedOperationPrefix
operator|+
name|state
argument_list|)
expr_stmt|;
block|}
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
literal|"Error metrics"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|stateScope
return|;
block|}
specifier|public
name|long
name|getBeginTime
parameter_list|()
block|{
return|return
name|beginTime
return|;
block|}
specifier|protected
name|OperationState
name|getState
parameter_list|()
block|{
return|return
name|state
return|;
block|}
specifier|protected
name|void
name|onNewState
parameter_list|(
name|OperationState
name|state
parameter_list|,
name|OperationState
name|prevState
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|RUNNING
case|:
name|markOperationStartTime
argument_list|()
expr_stmt|;
break|break;
case|case
name|ERROR
case|:
case|case
name|FINISHED
case|:
case|case
name|CANCELED
case|:
name|markOperationCompletedTime
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
specifier|public
name|long
name|getOperationComplete
parameter_list|()
block|{
return|return
name|operationComplete
return|;
block|}
specifier|public
name|long
name|getOperationStart
parameter_list|()
block|{
return|return
name|operationStart
return|;
block|}
specifier|protected
name|void
name|markOperationStartTime
parameter_list|()
block|{
name|operationStart
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|markOperationCompletedTime
parameter_list|()
block|{
name|operationComplete
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

