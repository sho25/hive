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

begin_comment
comment|/**  * Proxy wrapper on HiveSession to execute operations  * by impersonating given user  */
end_comment

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
name|security
operator|.
name|PrivilegedActionException
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
name|HiveSQLException
import|;
end_import

begin_class
specifier|public
class|class
name|HiveSessionProxy
implements|implements
name|InvocationHandler
block|{
specifier|private
specifier|final
name|HiveSession
name|base
decl_stmt|;
specifier|private
specifier|final
name|UserGroupInformation
name|ugi
decl_stmt|;
specifier|public
name|HiveSessionProxy
parameter_list|(
name|HiveSession
name|hiveSession
parameter_list|,
name|UserGroupInformation
name|ugi
parameter_list|)
block|{
name|this
operator|.
name|base
operator|=
name|hiveSession
expr_stmt|;
name|this
operator|.
name|ugi
operator|=
name|ugi
expr_stmt|;
block|}
specifier|public
specifier|static
name|HiveSession
name|getProxy
parameter_list|(
name|HiveSession
name|hiveSession
parameter_list|,
name|UserGroupInformation
name|ugi
parameter_list|)
throws|throws
name|IllegalArgumentException
throws|,
name|HiveSQLException
block|{
return|return
operator|(
name|HiveSession
operator|)
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|HiveSession
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|HiveSession
operator|.
name|class
block|}
operator|,
operator|new
name|HiveSessionProxy
argument_list|(
name|hiveSession
argument_list|,
name|ugi
argument_list|)
block|)
function|;
block|}
end_class

begin_function
annotation|@
name|Override
specifier|public
name|Object
name|invoke
parameter_list|(
name|Object
name|arg0
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
try|try
block|{
if|if
condition|(
name|method
operator|.
name|getDeclaringClass
argument_list|()
operator|==
name|HiveSessionBase
operator|.
name|class
condition|)
block|{
return|return
name|invoke
argument_list|(
name|method
argument_list|,
name|args
argument_list|)
return|;
block|}
return|return
name|ugi
operator|.
name|doAs
argument_list|(
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
return|return
name|invoke
argument_list|(
name|method
argument_list|,
name|args
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|UndeclaredThrowableException
name|e
parameter_list|)
block|{
name|Throwable
name|innerException
init|=
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|innerException
operator|instanceof
name|PrivilegedActionException
condition|)
block|{
throw|throw
name|innerException
operator|.
name|getCause
argument_list|()
throw|;
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
block|}
end_function

begin_function
specifier|private
name|Object
name|invoke
parameter_list|(
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
name|HiveSQLException
block|{
try|try
block|{
return|return
name|method
operator|.
name|invoke
argument_list|(
name|base
argument_list|,
name|args
argument_list|)
return|;
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
name|HiveSQLException
condition|)
block|{
throw|throw
operator|(
name|HiveSQLException
operator|)
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
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
catch|catch
parameter_list|(
name|IllegalAccessException
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
end_function

unit|}
end_unit

