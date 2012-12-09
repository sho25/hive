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
name|thrift
operator|.
name|TApplicationException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportException
import|;
end_import

begin_class
specifier|public
class|class
name|RetryingMetaStoreClient
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
name|RetryingMetaStoreClient
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|IMetaStoreClient
name|base
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|final
name|int
name|retryLimit
decl_stmt|;
specifier|private
specifier|final
name|int
name|retryDelaySeconds
decl_stmt|;
specifier|protected
name|RetryingMetaStoreClient
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|HiveMetaHookLoader
name|hookLoader
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|IMetaStoreClient
argument_list|>
name|msClientClass
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
name|this
operator|.
name|retryLimit
operator|=
name|hiveConf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORETHRIFTFAILURERETRIES
argument_list|)
expr_stmt|;
name|this
operator|.
name|retryDelaySeconds
operator|=
name|hiveConf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CLIENT_CONNECT_RETRY_DELAY
argument_list|)
expr_stmt|;
name|this
operator|.
name|base
operator|=
operator|(
name|IMetaStoreClient
operator|)
name|MetaStoreUtils
operator|.
name|newInstance
argument_list|(
name|msClientClass
argument_list|,
operator|new
name|Class
index|[]
block|{
name|HiveConf
operator|.
name|class
block|,
name|HiveMetaHookLoader
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|hiveConf
block|,
name|hookLoader
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|IMetaStoreClient
name|getProxy
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|HiveMetaHookLoader
name|hookLoader
parameter_list|,
name|String
name|mscClassName
parameter_list|)
throws|throws
name|MetaException
block|{
name|Class
argument_list|<
name|?
extends|extends
name|IMetaStoreClient
argument_list|>
name|baseClass
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|IMetaStoreClient
argument_list|>
operator|)
name|MetaStoreUtils
operator|.
name|getClass
argument_list|(
name|mscClassName
argument_list|)
decl_stmt|;
name|RetryingMetaStoreClient
name|handler
init|=
operator|new
name|RetryingMetaStoreClient
argument_list|(
name|hiveConf
argument_list|,
name|hookLoader
argument_list|,
name|baseClass
argument_list|)
decl_stmt|;
return|return
operator|(
name|IMetaStoreClient
operator|)
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|RetryingMetaStoreClient
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
argument_list|,
name|baseClass
operator|.
name|getInterfaces
argument_list|()
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
name|int
name|retriesMade
init|=
literal|0
decl_stmt|;
name|TException
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
name|UndeclaredThrowableException
name|e
parameter_list|)
block|{
throw|throw
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
if|if
condition|(
operator|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|TApplicationException
operator|)
operator|||
operator|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|TProtocolException
operator|)
operator|||
operator|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|TTransportException
operator|)
condition|)
block|{
name|caughtException
operator|=
operator|(
name|TException
operator|)
name|e
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|MetaException
operator|)
operator|&&
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|matches
argument_list|(
literal|"JDO[a-zA-Z]*Exception"
argument_list|)
condition|)
block|{
name|caughtException
operator|=
operator|(
name|MetaException
operator|)
name|e
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
else|else
block|{
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
name|retriesMade
operator|>=
name|retryLimit
condition|)
block|{
throw|throw
name|caughtException
throw|;
block|}
name|retriesMade
operator|++
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"MetaStoreClient lost connection. Attempting to reconnect."
argument_list|,
name|caughtException
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|retryDelaySeconds
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|base
operator|.
name|reconnect
argument_list|()
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit

