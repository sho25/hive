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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RetryingHMSHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|IHMSHandler
name|base
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
name|hiveConf
decl_stmt|;
specifier|protected
name|RetryingHMSHandler
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|MetaException
block|{
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
comment|// This has to be called before initializing the instance of HMSHandler
name|init
argument_list|()
expr_stmt|;
name|this
operator|.
name|base
operator|=
operator|(
name|IHMSHandler
operator|)
operator|new
name|HiveMetaStore
operator|.
name|HMSHandler
argument_list|(
name|name
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|IHMSHandler
name|getProxy
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|name
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
name|name
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
specifier|private
name|void
name|init
parameter_list|()
throws|throws
name|MetaException
block|{
comment|// Using the hook on startup ensures that the hook always has priority
comment|// over settings in *.xml.  The thread local conf needs to be used because at this point
comment|// it has already been initialized using hiveConf.
name|MetaStoreInit
operator|.
name|updateConnectionURL
argument_list|(
name|hiveConf
argument_list|,
name|getConf
argument_list|()
argument_list|,
literal|null
argument_list|,
name|metaStoreInitData
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|initMS
parameter_list|()
block|{
name|base
operator|.
name|setConf
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|invoke
parameter_list|(
name|Object
name|proxy
parameter_list|,
name|Method
name|method
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
name|Object
name|ret
init|=
literal|null
decl_stmt|;
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
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HMSHANDLERFORCERELOADCONF
argument_list|)
decl_stmt|;
name|int
name|retryInterval
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HMSHANDLERINTERVAL
argument_list|)
decl_stmt|;
name|int
name|retryLimit
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HMSHANDLERATTEMPTS
argument_list|)
decl_stmt|;
if|if
condition|(
name|reloadConf
condition|)
block|{
name|MetaStoreInit
operator|.
name|updateConnectionURL
argument_list|(
name|hiveConf
argument_list|,
name|getConf
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
comment|// Exception caughtException = null;
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
name|initMS
argument_list|()
expr_stmt|;
block|}
name|ret
operator|=
name|method
operator|.
name|invoke
argument_list|(
name|base
argument_list|,
name|args
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|javax
operator|.
name|jdo
operator|.
name|JDOException
name|e
parameter_list|)
block|{
name|caughtException
operator|=
name|e
expr_stmt|;
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
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|caughtException
argument_list|)
argument_list|)
expr_stmt|;
comment|// Since returning exceptions with a nested "cause" can be a problem in
comment|// Thrift, we are stuffing the stack trace into the message itself.
throw|throw
operator|new
name|MetaException
argument_list|(
name|ExceptionUtils
operator|.
name|getStackTrace
argument_list|(
name|caughtException
argument_list|)
argument_list|)
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
literal|"JDO datastore error. Retrying HMSHandler "
operator|+
literal|"after %d ms (attempt %d of %d)"
argument_list|,
name|retryInterval
argument_list|,
name|retryCount
argument_list|,
name|retryLimit
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
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|gotNewConnectUrl
operator|=
name|MetaStoreInit
operator|.
name|updateConnectionURL
argument_list|(
name|hiveConf
argument_list|,
name|getConf
argument_list|()
argument_list|,
name|lastUrl
argument_list|,
name|metaStoreInitData
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|hiveConf
return|;
block|}
block|}
end_class

end_unit

