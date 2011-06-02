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

begin_class
specifier|public
class|class
name|HiveIOExceptionHandlerUtil
block|{
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|HiveIOExceptionHandlerChain
argument_list|>
name|handlerChainInstance
init|=
operator|new
name|ThreadLocal
argument_list|<
name|HiveIOExceptionHandlerChain
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|HiveIOExceptionHandlerChain
name|get
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
name|HiveIOExceptionHandlerChain
name|cache
init|=
name|HiveIOExceptionHandlerUtil
operator|.
name|handlerChainInstance
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|cache
operator|==
literal|null
condition|)
block|{
name|HiveIOExceptionHandlerChain
name|toSet
init|=
name|HiveIOExceptionHandlerChain
operator|.
name|getHiveIOExceptionHandlerChain
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|handlerChainInstance
operator|.
name|set
argument_list|(
name|toSet
argument_list|)
expr_stmt|;
name|cache
operator|=
name|HiveIOExceptionHandlerUtil
operator|.
name|handlerChainInstance
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
return|return
name|cache
return|;
block|}
comment|/**    * Handle exception thrown when creating record reader. In case that there is    * an exception raised when construction the record reader and one handler can    * handle this exception, it should return an record reader, which is either a    * dummy empty record reader or a specific record reader that do some magic.    *     * @param e    * @param job    * @return    * @throws IOException    */
specifier|public
specifier|static
name|RecordReader
name|handleRecordReaderCreationException
parameter_list|(
name|Exception
name|e
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
name|HiveIOExceptionHandlerChain
name|ioExpectionHandlerChain
init|=
name|get
argument_list|(
name|job
argument_list|)
decl_stmt|;
if|if
condition|(
name|ioExpectionHandlerChain
operator|!=
literal|null
condition|)
block|{
return|return
name|ioExpectionHandlerChain
operator|.
name|handleRecordReaderCreationException
argument_list|(
name|e
argument_list|)
return|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|/**    * Handle exception thrown when calling record reader's next. If this    * exception is handled by one handler, will just return true. Otherwise,    * either re-throw this exception in one handler or at the end of the handler    * chain.    *     * @param e    * @param job    * @return    * @throws IOException    */
specifier|public
specifier|static
name|boolean
name|handleRecordReaderNextException
parameter_list|(
name|Exception
name|e
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
name|HiveIOExceptionHandlerChain
name|ioExpectionHandlerChain
init|=
name|get
argument_list|(
name|job
argument_list|)
decl_stmt|;
if|if
condition|(
name|ioExpectionHandlerChain
operator|!=
literal|null
condition|)
block|{
return|return
name|ioExpectionHandlerChain
operator|.
name|handleRecordReaderNextException
argument_list|(
name|e
argument_list|)
return|;
block|}
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

