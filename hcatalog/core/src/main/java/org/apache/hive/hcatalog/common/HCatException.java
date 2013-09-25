begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
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

begin_comment
comment|/**  * Class representing exceptions thrown by HCat.  */
end_comment

begin_class
specifier|public
class|class
name|HCatException
extends|extends
name|IOException
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/** The error type enum for this exception. */
specifier|private
specifier|final
name|ErrorType
name|errorType
decl_stmt|;
comment|/**    * Instantiates a new hcat exception.    * @param errorType the error type    */
specifier|public
name|HCatException
parameter_list|(
name|ErrorType
name|errorType
parameter_list|)
block|{
name|this
argument_list|(
name|errorType
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Instantiates a new hcat exception.    * @param errorType the error type    * @param cause the cause    */
specifier|public
name|HCatException
parameter_list|(
name|ErrorType
name|errorType
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|this
argument_list|(
name|errorType
argument_list|,
literal|null
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
comment|/**    * Instantiates a new hcat exception.    * @param errorType the error type    * @param extraMessage extra messages to add to the message string    */
specifier|public
name|HCatException
parameter_list|(
name|ErrorType
name|errorType
parameter_list|,
name|String
name|extraMessage
parameter_list|)
block|{
name|this
argument_list|(
name|errorType
argument_list|,
name|extraMessage
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Instantiates a new hcat exception.    * @param errorType the error type    * @param extraMessage extra messages to add to the message string    * @param cause the cause    */
specifier|public
name|HCatException
parameter_list|(
name|ErrorType
name|errorType
parameter_list|,
name|String
name|extraMessage
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|buildErrorMessage
argument_list|(
name|errorType
argument_list|,
name|extraMessage
argument_list|,
name|cause
argument_list|)
argument_list|,
name|cause
argument_list|)
expr_stmt|;
name|this
operator|.
name|errorType
operator|=
name|errorType
expr_stmt|;
block|}
comment|//TODO : remove default error type constructors after all exceptions
comment|//are changed to use error types
comment|/**    * Instantiates a new hcat exception.    * @param message the error message    */
specifier|public
name|HCatException
parameter_list|(
name|String
name|message
parameter_list|)
block|{
name|this
argument_list|(
name|ErrorType
operator|.
name|ERROR_INTERNAL_EXCEPTION
argument_list|,
name|message
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Instantiates a new hcat exception.    * @param message the error message    * @param cause the cause    */
specifier|public
name|HCatException
parameter_list|(
name|String
name|message
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|this
argument_list|(
name|ErrorType
operator|.
name|ERROR_INTERNAL_EXCEPTION
argument_list|,
name|message
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
comment|/**    * Builds the error message string. The error type message is appended with the extra message. If appendCause    * is true for the error type, then the message of the cause also is added to the message.    * @param type the error type    * @param extraMessage the extra message string    * @param cause the cause for the exception    * @return the exception message string    */
specifier|public
specifier|static
name|String
name|buildErrorMessage
parameter_list|(
name|ErrorType
name|type
parameter_list|,
name|String
name|extraMessage
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
comment|//Initial message is just the error type message
name|StringBuffer
name|message
init|=
operator|new
name|StringBuffer
argument_list|(
name|HCatException
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|message
operator|.
name|append
argument_list|(
literal|" : "
operator|+
name|type
operator|.
name|getErrorCode
argument_list|()
argument_list|)
expr_stmt|;
name|message
operator|.
name|append
argument_list|(
literal|" : "
operator|+
name|type
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|extraMessage
operator|!=
literal|null
condition|)
block|{
comment|//Add the extra message value to buffer
name|message
operator|.
name|append
argument_list|(
literal|" : "
operator|+
name|extraMessage
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|type
operator|.
name|appendCauseMessage
argument_list|()
condition|)
block|{
if|if
condition|(
name|cause
operator|!=
literal|null
condition|)
block|{
comment|//Add the cause message to buffer
name|message
operator|.
name|append
argument_list|(
literal|". Cause : "
operator|+
name|cause
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|message
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Is this a retriable error.    * @return is it retriable    */
specifier|public
name|boolean
name|isRetriable
parameter_list|()
block|{
return|return
name|errorType
operator|.
name|isRetriable
argument_list|()
return|;
block|}
comment|/**    * Gets the error type.    * @return the error type enum    */
specifier|public
name|ErrorType
name|getErrorType
parameter_list|()
block|{
return|return
name|errorType
return|;
block|}
comment|/**    * Gets the error code.    * @return the error code    */
specifier|public
name|int
name|getErrorCode
parameter_list|()
block|{
return|return
name|errorType
operator|.
name|getErrorCode
argument_list|()
return|;
block|}
comment|/* (non-Javadoc)   * @see java.lang.Throwable#toString()   */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getMessage
argument_list|()
return|;
block|}
block|}
end_class

end_unit

