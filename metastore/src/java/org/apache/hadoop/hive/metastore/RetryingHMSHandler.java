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
name|metastore
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationHandler
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
name|InvocationTargetException
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
name|Method
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
name|Proxy
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
name|UndeclaredThrowableException
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
name|lang
operator|.
name|exception
operator|.
name|ExceptionUtils
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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|NoSuchObjectException
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
name|log
operator|.
name|PerfLogger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|datanucleus
operator|.
name|exceptions
operator|.
name|NucleusException
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|RetryingHMSHandler
implements|implements
name|InvocationHandler
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
name|RetryingHMSHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|RetryingHMSHandler
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
class|class
name|Result
block|{
specifier|private
specifier|final
name|Object
name|result
decl_stmt|;
specifier|private
specifier|final
name|int
name|numRetries
decl_stmt|;
specifier|public
name|Result
parameter_list|(
name|Object
name|result
parameter_list|,
name|int
name|numRetries
parameter_list|)
block|{
name|this
operator|.
name|result
operator|=
name|result
expr_stmt|;
name|this
operator|.
name|numRetries
operator|=
name|numRetries
expr_stmt|;
block|}
block|}
specifier|private
specifier|final
name|IHMSHandler
name|baseHandler
decl_stmt|;
specifier|private
specifier|final
name|MetaStoreInit
operator|.
name|MetaStoreInitData
name|metaStoreInitData
init|=
operator|new
name|MetaStoreInit
operator|.
name|MetaStoreInitData
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|origConf
decl_stmt|;
comment|// base configuration
specifier|private
specifier|final
name|Configuration
name|activeConf
decl_stmt|;
comment|// active configuration
specifier|private
name|RetryingHMSHandler
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|IHMSHandler
name|baseHandler
parameter_list|,
name|boolean
name|local
parameter_list|)
throws|throws
name|MetaException
block|{
name|this
operator|.
name|origConf
operator|=
name|hiveConf
expr_stmt|;
name|this
operator|.
name|baseHandler
operator|=
name|baseHandler
expr_stmt|;
if|if
condition|(
name|local
condition|)
block|{
name|baseHandler
operator|.
name|setConf
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
comment|// tests expect configuration changes applied directly to metastore
block|}
name|activeConf
operator|=
name|baseHandler
operator|.
name|getConf
argument_list|()
expr_stmt|;
comment|// This has to be called before initializing the instance of HMSHandler
comment|// Using the hook on startup ensures that the hook always has priority
comment|// over settings in *.xml.  The thread local conf needs to be used because at this point
comment|// it has already been initialized using hiveConf.
name|MetaStoreInit
operator|.
name|updateConnectionURL
argument_list|(
name|hiveConf
argument_list|,
name|getActiveConf
argument_list|()
argument_list|,
literal|null
argument_list|,
name|metaStoreInitData
argument_list|)
expr_stmt|;
try|try
block|{
comment|//invoking init method of baseHandler this way since it adds the retry logic
comment|//in case of transient failures in init method
name|invoke
argument_list|(
name|baseHandler
argument_list|,
name|baseHandler
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredMethod
argument_list|(
literal|"init"
argument_list|,
operator|(
name|Class
argument_list|<
name|?
argument_list|>
index|[]
operator|)
literal|null
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"HMSHandler Fatal error: "
operator|+
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|MetaException
name|me
init|=
operator|new
name|MetaException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|me
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|me
throw|;
block|}
block|}
specifier|public
specifier|static
name|IHMSHandler
name|getProxy
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|IHMSHandler
name|baseHandler
parameter_list|,
name|boolean
name|local
parameter_list|)
throws|throws
name|MetaException
block|{
name|RetryingHMSHandler
name|handler
init|=
operator|new
name|RetryingHMSHandler
argument_list|(
name|hiveConf
argument_list|,
name|baseHandler
argument_list|,
name|local
argument_list|)
decl_stmt|;
return|return
operator|(
name|IHMSHandler
operator|)
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|RetryingHMSHandler
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
argument_list|,
operator|new
name|Class
index|[]
block|{
name|IHMSHandler
operator|.
name|class
block|}
argument_list|,
name|handler
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|invoke
parameter_list|(
specifier|final
name|Object
name|proxy
parameter_list|,
specifier|final
name|Method
name|method
parameter_list|,
specifier|final
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
name|int
name|retryCount
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|threadId
init|=
name|HiveMetaStore
operator|.
name|HMSHandler
operator|.
name|get
argument_list|()
decl_stmt|;
name|boolean
name|error
init|=
literal|true
decl_stmt|;
name|PerfLogger
name|perfLogger
init|=
name|PerfLogger
operator|.
name|getPerfLogger
argument_list|(
name|origConf
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|method
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|Result
name|result
init|=
name|invokeInternal
argument_list|(
name|proxy
argument_list|,
name|method
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|retryCount
operator|=
name|result
operator|.
name|numRetries
expr_stmt|;
name|error
operator|=
literal|false
expr_stmt|;
return|return
name|result
operator|.
name|result
return|;
block|}
finally|finally
block|{
name|StringBuilder
name|additionalInfo
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|additionalInfo
operator|.
name|append
argument_list|(
literal|"threadId="
argument_list|)
operator|.
name|append
argument_list|(
name|threadId
argument_list|)
operator|.
name|append
argument_list|(
literal|" retryCount="
argument_list|)
operator|.
name|append
argument_list|(
name|retryCount
argument_list|)
operator|.
name|append
argument_list|(
literal|" error="
argument_list|)
operator|.
name|append
argument_list|(
name|error
argument_list|)
expr_stmt|;
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|method
operator|.
name|getName
argument_list|()
argument_list|,
name|additionalInfo
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Result
name|invokeInternal
parameter_list|(
specifier|final
name|Object
name|proxy
parameter_list|,
specifier|final
name|Method
name|method
parameter_list|,
specifier|final
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
name|boolean
name|gotNewConnectUrl
init|=
literal|false
decl_stmt|;
name|boolean
name|reloadConf
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|origConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HMSHANDLERFORCERELOADCONF
argument_list|)
decl_stmt|;
name|long
name|retryInterval
init|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|origConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HMSHANDLERINTERVAL
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|int
name|retryLimit
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|origConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HMSHANDLERATTEMPTS
argument_list|)
decl_stmt|;
name|long
name|timeout
init|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|origConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CLIENT_SOCKET_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|Deadline
operator|.
name|registerIfNot
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
if|if
condition|(
name|reloadConf
condition|)
block|{
name|MetaStoreInit
operator|.
name|updateConnectionURL
argument_list|(
name|origConf
argument_list|,
name|getActiveConf
argument_list|()
argument_list|,
literal|null
argument_list|,
name|metaStoreInitData
argument_list|)
expr_stmt|;
block|}
name|int
name|retryCount
init|=
literal|0
decl_stmt|;
name|Throwable
name|caughtException
init|=
literal|null
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
if|if
condition|(
name|reloadConf
operator|||
name|gotNewConnectUrl
condition|)
block|{
name|baseHandler
operator|.
name|setConf
argument_list|(
name|getActiveConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Object
name|object
init|=
literal|null
decl_stmt|;
name|boolean
name|isStarted
init|=
name|Deadline
operator|.
name|startTimer
argument_list|(
name|method
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|object
operator|=
name|method
operator|.
name|invoke
argument_list|(
name|baseHandler
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|isStarted
condition|)
block|{
name|Deadline
operator|.
name|stopTimer
argument_list|()
expr_stmt|;
block|}
block|}
return|return
operator|new
name|Result
argument_list|(
name|object
argument_list|,
name|retryCount
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|UndeclaredThrowableException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|javax
operator|.
name|jdo
operator|.
name|JDOException
condition|)
block|{
comment|// Due to reflection, the jdo exception is wrapped in
comment|// invocationTargetException
name|caughtException
operator|=
name|e
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|MetaException
operator|&&
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
operator|&&
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|instanceof
name|javax
operator|.
name|jdo
operator|.
name|JDOException
condition|)
block|{
comment|// The JDOException may be wrapped further in a MetaException
name|caughtException
operator|=
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|javax
operator|.
name|jdo
operator|.
name|JDOException
condition|)
block|{
comment|// Due to reflection, the jdo exception is wrapped in
comment|// invocationTargetException
name|caughtException
operator|=
name|e
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|NoSuchObjectException
operator|||
name|e
operator|.
name|getTargetException
argument_list|()
operator|.
name|getCause
argument_list|()
operator|instanceof
name|NoSuchObjectException
condition|)
block|{
name|String
name|methodName
init|=
name|method
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|methodName
operator|.
name|startsWith
argument_list|(
literal|"get_database"
argument_list|)
operator|&&
operator|!
name|methodName
operator|.
name|startsWith
argument_list|(
literal|"get_table"
argument_list|)
operator|&&
operator|!
name|methodName
operator|.
name|startsWith
argument_list|(
literal|"get_partition"
argument_list|)
operator|&&
operator|!
name|methodName
operator|.
name|startsWith
argument_list|(
literal|"get_function"
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
throw|throw
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
elseif|else
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|MetaException
operator|&&
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|instanceof
name|javax
operator|.
name|jdo
operator|.
name|JDOException
operator|||
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|instanceof
name|NucleusException
condition|)
block|{
comment|// The JDOException or the Nucleus Exception may be wrapped further in a MetaException
name|caughtException
operator|=
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|instanceof
name|DeadlineException
condition|)
block|{
comment|// The Deadline Exception needs no retry and be thrown immediately.
name|Deadline
operator|.
name|clear
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Error happens in method "
operator|+
name|method
operator|.
name|getName
argument_list|()
operator|+
literal|": "
operator|+
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
block|}
if|if
condition|(
name|retryCount
operator|>=
name|retryLimit
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"HMSHandler Fatal error: "
operator|+
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|caughtException
argument_list|)
argument_list|)
expr_stmt|;
name|MetaException
name|me
init|=
operator|new
name|MetaException
argument_list|(
name|caughtException
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|me
operator|.
name|initCause
argument_list|(
name|caughtException
argument_list|)
expr_stmt|;
throw|throw
name|me
throw|;
block|}
assert|assert
operator|(
name|retryInterval
operator|>=
literal|0
operator|)
assert|;
name|retryCount
operator|++
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Retrying HMSHandler after %d ms (attempt %d of %d)"
argument_list|,
name|retryInterval
argument_list|,
name|retryCount
argument_list|,
name|retryLimit
argument_list|)
operator|+
literal|" with error: "
operator|+
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|caughtException
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|retryInterval
argument_list|)
expr_stmt|;
comment|// If we have a connection error, the JDO connection URL hook might
comment|// provide us with a new URL to access the datastore.
name|String
name|lastUrl
init|=
name|MetaStoreInit
operator|.
name|getConnectionURL
argument_list|(
name|getActiveConf
argument_list|()
argument_list|)
decl_stmt|;
name|gotNewConnectUrl
operator|=
name|MetaStoreInit
operator|.
name|updateConnectionURL
argument_list|(
name|origConf
argument_list|,
name|getActiveConf
argument_list|()
argument_list|,
name|lastUrl
argument_list|,
name|metaStoreInitData
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Configuration
name|getActiveConf
parameter_list|()
block|{
return|return
name|activeConf
return|;
block|}
block|}
end_class

end_unit

