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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
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
name|Properties
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
name|RejectedExecutionException
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
name|codec
operator|.
name|binary
operator|.
name|Base64
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
name|HiveVariableSource
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
name|VariableSubstitution
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
name|api
operator|.
name|FieldSchema
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
name|api
operator|.
name|Schema
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
name|CommandNeedRetryException
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
name|Driver
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
name|exec
operator|.
name|ExplainTask
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
name|exec
operator|.
name|Task
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
name|Hive
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
name|hadoop
operator|.
name|hive
operator|.
name|serde
operator|.
name|serdeConstants
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
name|serde2
operator|.
name|SerDe
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
name|serde2
operator|.
name|SerDeException
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
name|serde2
operator|.
name|SerDeUtils
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
name|serde2
operator|.
name|lazy
operator|.
name|LazySimpleSerDe
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|serde2
operator|.
name|objectinspector
operator|.
name|StructField
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
name|serde2
operator|.
name|objectinspector
operator|.
name|StructObjectInspector
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
name|io
operator|.
name|BytesWritable
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
name|RowSetFactory
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
name|server
operator|.
name|ThreadWithGarbageCleanup
import|;
end_import

begin_comment
comment|/**  * SQLOperation.  *  */
end_comment

begin_class
specifier|public
class|class
name|SQLOperation
extends|extends
name|ExecuteStatementOperation
block|{
specifier|private
name|Driver
name|driver
init|=
literal|null
decl_stmt|;
specifier|private
name|CommandProcessorResponse
name|response
decl_stmt|;
specifier|private
name|TableSchema
name|resultSchema
init|=
literal|null
decl_stmt|;
specifier|private
name|Schema
name|mResultSchema
init|=
literal|null
decl_stmt|;
specifier|private
name|SerDe
name|serde
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|fetchStarted
init|=
literal|false
decl_stmt|;
specifier|public
name|SQLOperation
parameter_list|(
name|HiveSession
name|parentSession
parameter_list|,
name|String
name|statement
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
parameter_list|,
name|boolean
name|runInBackground
parameter_list|)
block|{
comment|// TODO: call setRemoteUser in ExecuteStatementOperation or higher.
name|super
argument_list|(
name|parentSession
argument_list|,
name|statement
argument_list|,
name|confOverlay
argument_list|,
name|runInBackground
argument_list|)
expr_stmt|;
block|}
comment|/***    * Compile the query and extract metadata    * @param sqlOperationConf    * @throws HiveSQLException    */
specifier|public
name|void
name|prepare
parameter_list|(
name|HiveConf
name|sqlOperationConf
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
try|try
block|{
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|sqlOperationConf
argument_list|,
name|getParentSession
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
comment|// set the operation handle information in Driver, so that thrift API users
comment|// can use the operation handle they receive, to lookup query information in
comment|// Yarn ATS
name|String
name|guid64
init|=
name|Base64
operator|.
name|encodeBase64URLSafeString
argument_list|(
name|getHandle
argument_list|()
operator|.
name|getHandleIdentifier
argument_list|()
operator|.
name|toTHandleIdentifier
argument_list|()
operator|.
name|getGuid
argument_list|()
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|driver
operator|.
name|setOperationId
argument_list|(
name|guid64
argument_list|)
expr_stmt|;
comment|// In Hive server mode, we are not able to retry in the FetchTask
comment|// case, when calling fetch queries since execute() has returned.
comment|// For now, we disable the test attempts.
name|driver
operator|.
name|setTryCount
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|String
name|subStatement
init|=
operator|new
name|VariableSubstitution
argument_list|(
operator|new
name|HiveVariableSource
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getHiveVariable
parameter_list|()
block|{
return|return
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveVariables
argument_list|()
return|;
block|}
block|}
argument_list|)
operator|.
name|substitute
argument_list|(
name|sqlOperationConf
argument_list|,
name|statement
argument_list|)
decl_stmt|;
name|response
operator|=
name|driver
operator|.
name|compileAndRespond
argument_list|(
name|subStatement
argument_list|)
expr_stmt|;
if|if
condition|(
literal|0
operator|!=
name|response
operator|.
name|getResponseCode
argument_list|()
condition|)
block|{
throw|throw
name|toSQLException
argument_list|(
literal|"Error while compiling statement"
argument_list|,
name|response
argument_list|)
throw|;
block|}
name|mResultSchema
operator|=
name|driver
operator|.
name|getSchema
argument_list|()
expr_stmt|;
comment|// hasResultSet should be true only if the query has a FetchTask
comment|// "explain" is an exception for now
if|if
condition|(
name|driver
operator|.
name|getPlan
argument_list|()
operator|.
name|getFetchTask
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|//Schema has to be set
if|if
condition|(
name|mResultSchema
operator|==
literal|null
operator|||
operator|!
name|mResultSchema
operator|.
name|isSetFieldSchemas
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Error compiling query: Schema and FieldSchema "
operator|+
literal|"should be set when query plan has a FetchTask"
argument_list|)
throw|;
block|}
name|resultSchema
operator|=
operator|new
name|TableSchema
argument_list|(
name|mResultSchema
argument_list|)
expr_stmt|;
name|setHasResultSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setHasResultSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|// Set hasResultSet true if the plan has ExplainTask
comment|// TODO explain should use a FetchTask for reading
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
range|:
name|driver
operator|.
name|getPlan
argument_list|()
operator|.
name|getRootTasks
argument_list|()
control|)
block|{
if|if
condition|(
name|task
operator|.
name|getClass
argument_list|()
operator|==
name|ExplainTask
operator|.
name|class
condition|)
block|{
name|resultSchema
operator|=
operator|new
name|TableSchema
argument_list|(
name|mResultSchema
argument_list|)
expr_stmt|;
name|setHasResultSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|ERROR
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|ERROR
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Error running query: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|runQuery
parameter_list|(
name|HiveConf
name|sqlOperationConf
parameter_list|)
throws|throws
name|HiveSQLException
block|{
try|try
block|{
comment|// In Hive server mode, we are not able to retry in the FetchTask
comment|// case, when calling fetch queries since execute() has returned.
comment|// For now, we disable the test attempts.
name|driver
operator|.
name|setTryCount
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|response
operator|=
name|driver
operator|.
name|run
argument_list|()
expr_stmt|;
if|if
condition|(
literal|0
operator|!=
name|response
operator|.
name|getResponseCode
argument_list|()
condition|)
block|{
throw|throw
name|toSQLException
argument_list|(
literal|"Error while processing statement"
argument_list|,
name|response
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
comment|// If the operation was cancelled by another thread,
comment|// Driver#run will return a non-zero response code.
comment|// We will simply return if the operation state is CANCELED,
comment|// otherwise throw an exception
if|if
condition|(
name|getStatus
argument_list|()
operator|.
name|getState
argument_list|()
operator|==
name|OperationState
operator|.
name|CANCELED
condition|)
block|{
return|return;
block|}
else|else
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|ERROR
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|ERROR
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Error running query: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|setState
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|runInternal
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|PENDING
argument_list|)
expr_stmt|;
specifier|final
name|HiveConf
name|opConfig
init|=
name|getConfigForOperation
argument_list|()
decl_stmt|;
name|prepare
argument_list|(
name|opConfig
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|shouldRunAsync
argument_list|()
condition|)
block|{
name|runQuery
argument_list|(
name|opConfig
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// We'll pass ThreadLocals in the background thread from the foreground (handler) thread
specifier|final
name|SessionState
name|parentSessionState
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// ThreadLocal Hive object needs to be set in background thread.
comment|// The metastore client in Hive is associated with right user.
specifier|final
name|Hive
name|parentHive
init|=
name|parentSession
operator|.
name|getSessionHive
argument_list|()
decl_stmt|;
comment|// Current UGI will get used by metastore when metsatore is in embedded mode
comment|// So this needs to get passed to the new background thread
specifier|final
name|UserGroupInformation
name|currentUGI
init|=
name|getCurrentUGI
argument_list|(
name|opConfig
argument_list|)
decl_stmt|;
comment|// Runnable impl to call runInternal asynchronously,
comment|// from a different thread
name|Runnable
name|backgroundOperation
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
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
name|doAsAction
init|=
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|Hive
operator|.
name|set
argument_list|(
name|parentHive
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|setCurrentSessionState
argument_list|(
name|parentSessionState
argument_list|)
expr_stmt|;
comment|// Set current OperationLog in this async thread for keeping on saving query log.
name|registerCurrentOperationLog
argument_list|()
expr_stmt|;
try|try
block|{
name|runQuery
argument_list|(
name|opConfig
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
name|setOperationException
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Error running hive query: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|unregisterOperationLog
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
try|try
block|{
name|currentUGI
operator|.
name|doAs
argument_list|(
name|doAsAction
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|setOperationException
argument_list|(
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Error running hive query as user : "
operator|+
name|currentUGI
operator|.
name|getShortUserName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|/**              * We'll cache the ThreadLocal RawStore object for this background thread for an orderly cleanup              * when this thread is garbage collected later.              * @see org.apache.hive.service.server.ThreadWithGarbageCleanup#finalize()              */
if|if
condition|(
name|ThreadWithGarbageCleanup
operator|.
name|currentThread
argument_list|()
operator|instanceof
name|ThreadWithGarbageCleanup
condition|)
block|{
name|ThreadWithGarbageCleanup
name|currentThread
init|=
operator|(
name|ThreadWithGarbageCleanup
operator|)
name|ThreadWithGarbageCleanup
operator|.
name|currentThread
argument_list|()
decl_stmt|;
name|currentThread
operator|.
name|cacheThreadLocalRawStore
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
decl_stmt|;
try|try
block|{
comment|// This submit blocks if no background threads are available to run this operation
name|Future
argument_list|<
name|?
argument_list|>
name|backgroundHandle
init|=
name|getParentSession
argument_list|()
operator|.
name|getSessionManager
argument_list|()
operator|.
name|submitBackgroundOperation
argument_list|(
name|backgroundOperation
argument_list|)
decl_stmt|;
name|setBackgroundHandle
argument_list|(
name|backgroundHandle
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RejectedExecutionException
name|rejected
parameter_list|)
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|ERROR
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"The background threadpool cannot accept"
operator|+
literal|" new task for execution, please retry the operation"
argument_list|,
name|rejected
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * Returns the current UGI on the stack    * @param opConfig    * @return UserGroupInformation    * @throws HiveSQLException    */
specifier|private
name|UserGroupInformation
name|getCurrentUGI
parameter_list|(
name|HiveConf
name|opConfig
parameter_list|)
throws|throws
name|HiveSQLException
block|{
try|try
block|{
return|return
name|Utils
operator|.
name|getUGI
argument_list|()
return|;
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
literal|"Unable to get current user"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|registerCurrentOperationLog
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
name|warn
argument_list|(
literal|"Failed to get current OperationLog object of Operation: "
operator|+
name|getHandle
argument_list|()
operator|.
name|getHandleIdentifier
argument_list|()
argument_list|)
expr_stmt|;
name|isOperationLogEnabled
operator|=
literal|false
expr_stmt|;
return|return;
block|}
name|OperationLog
operator|.
name|setCurrentOperationLog
argument_list|(
name|operationLog
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|cleanup
parameter_list|(
name|OperationState
name|state
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|setState
argument_list|(
name|state
argument_list|)
expr_stmt|;
if|if
condition|(
name|shouldRunAsync
argument_list|()
condition|)
block|{
name|Future
argument_list|<
name|?
argument_list|>
name|backgroundHandle
init|=
name|getBackgroundHandle
argument_list|()
decl_stmt|;
if|if
condition|(
name|backgroundHandle
operator|!=
literal|null
condition|)
block|{
name|backgroundHandle
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|driver
operator|!=
literal|null
condition|)
block|{
name|driver
operator|.
name|close
argument_list|()
expr_stmt|;
name|driver
operator|.
name|destroy
argument_list|()
expr_stmt|;
block|}
name|driver
operator|=
literal|null
expr_stmt|;
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|ss
operator|.
name|getTmpOutputFile
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|ss
operator|.
name|getTmpOutputFile
argument_list|()
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|ss
operator|.
name|getTmpErrOutputFile
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|ss
operator|.
name|getTmpErrOutputFile
argument_list|()
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|cancel
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|cleanup
argument_list|(
name|OperationState
operator|.
name|CANCELED
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|cleanup
argument_list|(
name|OperationState
operator|.
name|CLOSED
argument_list|)
expr_stmt|;
name|cleanupOperationLog
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TableSchema
name|getResultSetSchema
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|assertState
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|)
expr_stmt|;
if|if
condition|(
name|resultSchema
operator|==
literal|null
condition|)
block|{
name|resultSchema
operator|=
operator|new
name|TableSchema
argument_list|(
name|driver
operator|.
name|getSchema
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|resultSchema
return|;
block|}
specifier|private
specifier|transient
specifier|final
name|List
argument_list|<
name|Object
argument_list|>
name|convey
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
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
block|{
name|validateDefaultFetchOrientation
argument_list|(
name|orientation
argument_list|)
expr_stmt|;
name|assertState
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|)
expr_stmt|;
name|RowSet
name|rowSet
init|=
name|RowSetFactory
operator|.
name|create
argument_list|(
name|resultSchema
argument_list|,
name|getProtocolVersion
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
comment|/* if client is requesting fetch-from-start and its not the first time reading from this operation        * then reset the fetch position to beginning        */
if|if
condition|(
name|orientation
operator|.
name|equals
argument_list|(
name|FetchOrientation
operator|.
name|FETCH_FIRST
argument_list|)
operator|&&
name|fetchStarted
condition|)
block|{
name|driver
operator|.
name|resetFetch
argument_list|()
expr_stmt|;
block|}
name|fetchStarted
operator|=
literal|true
expr_stmt|;
name|driver
operator|.
name|setMaxRows
argument_list|(
operator|(
name|int
operator|)
name|maxRows
argument_list|)
expr_stmt|;
if|if
condition|(
name|driver
operator|.
name|getResults
argument_list|(
name|convey
argument_list|)
condition|)
block|{
return|return
name|decode
argument_list|(
name|convey
argument_list|,
name|rowSet
argument_list|)
return|;
block|}
return|return
name|rowSet
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|CommandNeedRetryException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
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
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|convey
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|RowSet
name|decode
parameter_list|(
name|List
argument_list|<
name|Object
argument_list|>
name|rows
parameter_list|,
name|RowSet
name|rowSet
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|driver
operator|.
name|isFetchingTable
argument_list|()
condition|)
block|{
return|return
name|prepareFromRow
argument_list|(
name|rows
argument_list|,
name|rowSet
argument_list|)
return|;
block|}
return|return
name|decodeFromString
argument_list|(
name|rows
argument_list|,
name|rowSet
argument_list|)
return|;
block|}
comment|// already encoded to thrift-able object in ThriftFormatter
specifier|private
name|RowSet
name|prepareFromRow
parameter_list|(
name|List
argument_list|<
name|Object
argument_list|>
name|rows
parameter_list|,
name|RowSet
name|rowSet
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|Object
name|row
range|:
name|rows
control|)
block|{
name|rowSet
operator|.
name|addRow
argument_list|(
operator|(
name|Object
index|[]
operator|)
name|row
argument_list|)
expr_stmt|;
block|}
return|return
name|rowSet
return|;
block|}
specifier|private
name|RowSet
name|decodeFromString
parameter_list|(
name|List
argument_list|<
name|Object
argument_list|>
name|rows
parameter_list|,
name|RowSet
name|rowSet
parameter_list|)
throws|throws
name|SQLException
throws|,
name|SerDeException
block|{
name|getSerDe
argument_list|()
expr_stmt|;
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|serde
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fieldRefs
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|Object
index|[]
name|deserializedFields
init|=
operator|new
name|Object
index|[
name|fieldRefs
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|Object
name|rowObj
decl_stmt|;
name|ObjectInspector
name|fieldOI
decl_stmt|;
name|int
name|protocol
init|=
name|getProtocolVersion
argument_list|()
operator|.
name|getValue
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|rowString
range|:
name|rows
control|)
block|{
try|try
block|{
name|rowObj
operator|=
name|serde
operator|.
name|deserialize
argument_list|(
operator|new
name|BytesWritable
argument_list|(
operator|(
operator|(
name|String
operator|)
name|rowString
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fieldRefs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|StructField
name|fieldRef
init|=
name|fieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|fieldOI
operator|=
name|fieldRef
operator|.
name|getFieldObjectInspector
argument_list|()
expr_stmt|;
name|Object
name|fieldData
init|=
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|rowObj
argument_list|,
name|fieldRef
argument_list|)
decl_stmt|;
name|deserializedFields
index|[
name|i
index|]
operator|=
name|SerDeUtils
operator|.
name|toThriftPayload
argument_list|(
name|fieldData
argument_list|,
name|fieldOI
argument_list|,
name|protocol
argument_list|)
expr_stmt|;
block|}
name|rowSet
operator|.
name|addRow
argument_list|(
name|deserializedFields
argument_list|)
expr_stmt|;
block|}
return|return
name|rowSet
return|;
block|}
specifier|private
name|SerDe
name|getSerDe
parameter_list|()
throws|throws
name|SQLException
block|{
if|if
condition|(
name|serde
operator|!=
literal|null
condition|)
block|{
return|return
name|serde
return|;
block|}
try|try
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldSchemas
init|=
name|mResultSchema
operator|.
name|getFieldSchemas
argument_list|()
decl_stmt|;
name|StringBuilder
name|namesSb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|StringBuilder
name|typesSb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldSchemas
operator|!=
literal|null
operator|&&
operator|!
name|fieldSchemas
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|fieldSchemas
operator|.
name|size
argument_list|()
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
name|pos
operator|!=
literal|0
condition|)
block|{
name|namesSb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|typesSb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|namesSb
operator|.
name|append
argument_list|(
name|fieldSchemas
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|typesSb
operator|.
name|append
argument_list|(
name|fieldSchemas
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|String
name|names
init|=
name|namesSb
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|types
init|=
name|typesSb
operator|.
name|toString
argument_list|()
decl_stmt|;
name|serde
operator|=
operator|new
name|LazySimpleSerDe
argument_list|()
expr_stmt|;
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
if|if
condition|(
name|names
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Column names: "
operator|+
name|names
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|names
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|types
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Column types: "
operator|+
name|types
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|types
argument_list|)
expr_stmt|;
block|}
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serde
argument_list|,
operator|new
name|HiveConf
argument_list|()
argument_list|,
name|props
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Could not create ResultSet: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ex
argument_list|)
throw|;
block|}
return|return
name|serde
return|;
block|}
comment|/**    * If there are query specific settings to overlay, then create a copy of config    * There are two cases we need to clone the session config that's being passed to hive driver    * 1. Async query -    *    If the client changes a config setting, that shouldn't reflect in the execution already underway    * 2. confOverlay -    *    The query specific settings should only be applied to the query config and not session    * @return new configuration    * @throws HiveSQLException    */
specifier|private
name|HiveConf
name|getConfigForOperation
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|HiveConf
name|sqlOperationConf
init|=
name|getParentSession
argument_list|()
operator|.
name|getHiveConf
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|getConfOverlay
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|||
name|shouldRunAsync
argument_list|()
condition|)
block|{
comment|// clone the partent session config for this query
name|sqlOperationConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|sqlOperationConf
argument_list|)
expr_stmt|;
comment|// apply overlay query specific settings, if any
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confEntry
range|:
name|getConfOverlay
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
try|try
block|{
name|sqlOperationConf
operator|.
name|verifyAndSet
argument_list|(
name|confEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|confEntry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Error applying statement specific settings"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
return|return
name|sqlOperationConf
return|;
block|}
block|}
end_class

end_unit

