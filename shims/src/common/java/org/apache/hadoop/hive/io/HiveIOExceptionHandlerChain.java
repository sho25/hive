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
name|io
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputFormat
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|RecordReader
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * An exception handler chain that process the input exception by going through  * all exceptions defined in this chain one by one until either one exception  * handler returns true or it reaches the end of the chain. If it reaches the  * end of the chain, and still no exception handler returns true, throw the  * exception to the caller.  */
end_comment

begin_class
specifier|public
class|class
name|HiveIOExceptionHandlerChain
block|{
specifier|public
specifier|static
name|String
name|HIVE_IO_EXCEPTION_HANDLE_CHAIN
init|=
literal|"hive.io.exception.handlers"
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
name|HiveIOExceptionHandlerChain
name|getHiveIOExceptionHandlerChain
parameter_list|(
name|JobConf
name|conf
parameter_list|)
block|{
name|HiveIOExceptionHandlerChain
name|chain
init|=
operator|new
name|HiveIOExceptionHandlerChain
argument_list|()
decl_stmt|;
name|String
name|exceptionHandlerStr
init|=
name|conf
operator|.
name|get
argument_list|(
name|HIVE_IO_EXCEPTION_HANDLE_CHAIN
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HiveIOExceptionHandler
argument_list|>
name|handlerChain
init|=
operator|new
name|ArrayList
argument_list|<
name|HiveIOExceptionHandler
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|exceptionHandlerStr
operator|!=
literal|null
operator|&&
operator|!
name|exceptionHandlerStr
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|String
index|[]
name|handlerArr
init|=
name|exceptionHandlerStr
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
if|if
condition|(
name|handlerArr
operator|!=
literal|null
operator|&&
name|handlerArr
operator|.
name|length
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|String
name|handlerStr
range|:
name|handlerArr
control|)
block|{
if|if
condition|(
operator|!
name|handlerStr
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|HiveIOExceptionHandler
argument_list|>
name|handlerCls
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HiveIOExceptionHandler
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|handlerStr
argument_list|)
decl_stmt|;
name|HiveIOExceptionHandler
name|handler
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|handlerCls
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|handlerChain
operator|.
name|add
argument_list|(
name|handler
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{             }
block|}
block|}
block|}
block|}
name|chain
operator|.
name|setHandlerChain
argument_list|(
name|handlerChain
argument_list|)
expr_stmt|;
return|return
name|chain
return|;
block|}
specifier|private
name|List
argument_list|<
name|HiveIOExceptionHandler
argument_list|>
name|handlerChain
decl_stmt|;
comment|/**    * @return the exception handler chain defined    */
specifier|protected
name|List
argument_list|<
name|HiveIOExceptionHandler
argument_list|>
name|getHandlerChain
parameter_list|()
block|{
return|return
name|handlerChain
return|;
block|}
comment|/**    * set the exception handler chain    * @param handlerChain    */
specifier|protected
name|void
name|setHandlerChain
parameter_list|(
name|List
argument_list|<
name|HiveIOExceptionHandler
argument_list|>
name|handlerChain
parameter_list|)
block|{
name|this
operator|.
name|handlerChain
operator|=
name|handlerChain
expr_stmt|;
block|}
specifier|public
name|RecordReader
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|handleRecordReaderCreationException
parameter_list|(
name|Exception
name|e
parameter_list|)
throws|throws
name|IOException
block|{
name|RecordReader
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|ret
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|handlerChain
operator|!=
literal|null
operator|&&
name|handlerChain
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|HiveIOExceptionHandler
name|handler
range|:
name|handlerChain
control|)
block|{
name|ret
operator|=
name|handler
operator|.
name|handleRecordReaderCreationException
argument_list|(
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|ret
operator|!=
literal|null
condition|)
block|{
return|return
name|ret
return|;
block|}
block|}
block|}
comment|//re-throw the exception as an IOException
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|/**    * This is to handle exception when doing next operations. Here we use a    * HiveIOExceptionNextHandleResult to store the results of each handler. If    * the exception is handled by one handler, the handler should set    * HiveIOExceptionNextHandleResult to be handled, and also set the handle    * result. The handle result is used to return the reader's next to determine    * if need to open a new file for read or not.    */
specifier|public
name|boolean
name|handleRecordReaderNextException
parameter_list|(
name|Exception
name|e
parameter_list|)
throws|throws
name|IOException
block|{
name|HiveIOExceptionNextHandleResult
name|result
init|=
operator|new
name|HiveIOExceptionNextHandleResult
argument_list|()
decl_stmt|;
if|if
condition|(
name|handlerChain
operator|!=
literal|null
operator|&&
name|handlerChain
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|HiveIOExceptionHandler
name|handler
range|:
name|handlerChain
control|)
block|{
name|handler
operator|.
name|handleRecorReaderNextException
argument_list|(
name|e
argument_list|,
name|result
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|.
name|getHandled
argument_list|()
condition|)
block|{
return|return
name|result
operator|.
name|getHandleResult
argument_list|()
return|;
block|}
block|}
block|}
comment|//re-throw the exception as an IOException
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

