begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|utils
package|;
end_package

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

begin_class
specifier|public
class|class
name|RetryUtilities
block|{
specifier|public
specifier|static
class|class
name|RetryException
extends|extends
name|Exception
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
name|RetryException
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|super
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RetryException
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Interface used to create a ExponentialBackOffRetry policy    */
specifier|public
specifier|static
interface|interface
name|ExponentialBackOffRetry
parameter_list|<
name|T
parameter_list|>
block|{
comment|/**      * This method should be called by implementations of this ExponentialBackOffRetry policy      * It represents the actual work which needs to be done based on a given batch size      * @param batchSize The batch size for the work which needs to be executed      * @return      * @throws Exception      */
specifier|public
name|T
name|execute
parameter_list|(
name|int
name|batchSize
parameter_list|)
throws|throws
name|Exception
function_decl|;
block|}
comment|/**    * This class is a base implementation of a simple exponential back retry policy. The batch size    * and decaying factor are provided with the constructor. It reduces the batch size by dividing    * it by the decaying factor every time there is an exception in the execute method.    */
specifier|public
specifier|static
specifier|abstract
class|class
name|ExponentiallyDecayingBatchWork
parameter_list|<
name|T
parameter_list|>
implements|implements
name|ExponentialBackOffRetry
argument_list|<
name|T
argument_list|>
block|{
specifier|private
name|int
name|batchSize
decl_stmt|;
specifier|private
specifier|final
name|int
name|decayingFactor
decl_stmt|;
specifier|private
name|int
name|maxRetries
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
name|ExponentiallyDecayingBatchWork
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|ExponentiallyDecayingBatchWork
parameter_list|(
name|int
name|batchSize
parameter_list|,
name|int
name|reducingFactor
parameter_list|,
name|int
name|maxRetries
parameter_list|)
block|{
if|if
condition|(
name|batchSize
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Invalid batch size %d provided. Batch size must be greater than 0"
argument_list|,
name|batchSize
argument_list|)
argument_list|)
throw|;
block|}
name|this
operator|.
name|batchSize
operator|=
name|batchSize
expr_stmt|;
if|if
condition|(
name|reducingFactor
operator|<=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Invalid decaying factor %d provided. Decaying factor must be greater than 1"
argument_list|,
name|batchSize
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|maxRetries
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Invalid number of maximum retries %d provided. It must be a non-negative integer value"
argument_list|,
name|maxRetries
argument_list|)
argument_list|)
throw|;
block|}
comment|//if maxRetries is 0 code retries until batch decays to zero
name|this
operator|.
name|maxRetries
operator|=
name|maxRetries
expr_stmt|;
name|this
operator|.
name|decayingFactor
operator|=
name|reducingFactor
expr_stmt|;
block|}
specifier|public
name|T
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|attempt
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|size
init|=
name|getNextBatchSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|RetryException
argument_list|(
literal|"Batch size reduced to zero"
argument_list|)
throw|;
block|}
try|try
block|{
return|return
name|execute
argument_list|(
name|size
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Exception thrown while processing using a batch size %d"
argument_list|,
name|size
argument_list|)
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|attempt
operator|++
expr_stmt|;
if|if
condition|(
name|attempt
operator|==
name|maxRetries
condition|)
block|{
throw|throw
operator|new
name|RetryException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Maximum number of retry attempts %d exhausted"
argument_list|,
name|maxRetries
argument_list|)
argument_list|)
throw|;
block|}
block|}
block|}
block|}
specifier|private
name|int
name|getNextBatchSize
parameter_list|()
block|{
name|int
name|ret
init|=
name|batchSize
decl_stmt|;
name|batchSize
operator|/=
name|decayingFactor
expr_stmt|;
return|return
name|ret
return|;
block|}
block|}
block|}
end_class

end_unit

