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
name|ql
operator|.
name|processors
package|;
end_package

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
name|ErrorMsg
import|;
end_import

begin_comment
comment|/**  * Encapsulates the basic response info returned by classes the implement the  *<code>CommandProcessor</code> interface. Typically<code>errorMessage</code>  * and<code>SQLState</code> will only be set if the<code>responseCode</code>  * is not 0.  Note that often {@code responseCode} ends up the exit value of  * command shell process so should keep it to< 127.  */
end_comment

begin_class
specifier|public
class|class
name|CommandProcessorResponse
block|{
specifier|private
specifier|final
name|int
name|responseCode
decl_stmt|;
specifier|private
specifier|final
name|String
name|errorMessage
decl_stmt|;
specifier|private
specifier|final
name|int
name|hiveErrorCode
decl_stmt|;
specifier|private
specifier|final
name|String
name|SQLState
decl_stmt|;
specifier|private
specifier|final
name|Schema
name|resSchema
decl_stmt|;
specifier|private
specifier|final
name|Throwable
name|exception
decl_stmt|;
specifier|public
name|CommandProcessorResponse
parameter_list|(
name|int
name|responseCode
parameter_list|)
block|{
name|this
argument_list|(
name|responseCode
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CommandProcessorResponse
parameter_list|(
name|int
name|responseCode
parameter_list|,
name|String
name|errorMessage
parameter_list|,
name|String
name|SQLState
parameter_list|)
block|{
name|this
argument_list|(
name|responseCode
argument_list|,
name|errorMessage
argument_list|,
name|SQLState
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CommandProcessorResponse
parameter_list|(
name|int
name|responseCode
parameter_list|,
name|String
name|errorMessage
parameter_list|,
name|String
name|SQLState
parameter_list|,
name|Throwable
name|exception
parameter_list|)
block|{
name|this
argument_list|(
name|responseCode
argument_list|,
name|errorMessage
argument_list|,
name|SQLState
argument_list|,
literal|null
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CommandProcessorResponse
parameter_list|(
name|int
name|responseCode
parameter_list|,
name|String
name|errorMessage
parameter_list|,
name|String
name|SQLState
parameter_list|,
name|Schema
name|schema
parameter_list|)
block|{
name|this
argument_list|(
name|responseCode
argument_list|,
name|errorMessage
argument_list|,
name|SQLState
argument_list|,
name|schema
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CommandProcessorResponse
parameter_list|(
name|int
name|responseCode
parameter_list|,
name|ErrorMsg
name|canonicalErrMsg
parameter_list|,
name|Throwable
name|t
parameter_list|,
name|String
modifier|...
name|msgArgs
parameter_list|)
block|{
name|this
argument_list|(
name|responseCode
argument_list|,
name|canonicalErrMsg
operator|.
name|format
argument_list|(
name|msgArgs
argument_list|)
argument_list|,
name|canonicalErrMsg
operator|.
name|getSQLState
argument_list|()
argument_list|,
literal|null
argument_list|,
name|t
argument_list|,
name|canonicalErrMsg
operator|.
name|getErrorCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create CommandProcessorResponse object indicating an error.    * Creates new CommandProcessorResponse with responseCode=1, and sets message    * from exception argument    *    * @param e    * @return    */
specifier|public
specifier|static
name|CommandProcessorResponse
name|create
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
operator|new
name|CommandProcessorResponse
argument_list|(
literal|1
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
name|CommandProcessorResponse
parameter_list|(
name|int
name|responseCode
parameter_list|,
name|String
name|errorMessage
parameter_list|,
name|String
name|SQLState
parameter_list|,
name|Schema
name|schema
parameter_list|,
name|Throwable
name|exception
parameter_list|)
block|{
name|this
argument_list|(
name|responseCode
argument_list|,
name|errorMessage
argument_list|,
name|SQLState
argument_list|,
name|schema
argument_list|,
name|exception
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CommandProcessorResponse
parameter_list|(
name|int
name|responseCode
parameter_list|,
name|String
name|errorMessage
parameter_list|,
name|String
name|SQLState
parameter_list|,
name|Schema
name|schema
parameter_list|,
name|Throwable
name|exception
parameter_list|,
name|int
name|hiveErrorCode
parameter_list|)
block|{
name|this
operator|.
name|responseCode
operator|=
name|responseCode
expr_stmt|;
name|this
operator|.
name|errorMessage
operator|=
name|errorMessage
expr_stmt|;
name|this
operator|.
name|SQLState
operator|=
name|SQLState
expr_stmt|;
name|this
operator|.
name|resSchema
operator|=
name|schema
expr_stmt|;
name|this
operator|.
name|exception
operator|=
name|exception
expr_stmt|;
name|this
operator|.
name|hiveErrorCode
operator|=
name|hiveErrorCode
expr_stmt|;
block|}
specifier|public
name|int
name|getResponseCode
parameter_list|()
block|{
return|return
name|responseCode
return|;
block|}
specifier|public
name|String
name|getErrorMessage
parameter_list|()
block|{
return|return
name|errorMessage
return|;
block|}
specifier|public
name|String
name|getSQLState
parameter_list|()
block|{
return|return
name|SQLState
return|;
block|}
specifier|public
name|Schema
name|getSchema
parameter_list|()
block|{
return|return
name|resSchema
return|;
block|}
specifier|public
name|Throwable
name|getException
parameter_list|()
block|{
return|return
name|exception
return|;
block|}
specifier|public
name|int
name|getErrorCode
parameter_list|()
block|{
return|return
name|hiveErrorCode
return|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"("
operator|+
name|responseCode
operator|+
literal|","
operator|+
name|errorMessage
operator|+
literal|","
operator|+
operator|(
name|hiveErrorCode
operator|>
literal|0
condition|?
name|hiveErrorCode
operator|+
literal|","
else|:
literal|""
operator|)
operator|+
name|SQLState
operator|+
operator|(
name|resSchema
operator|==
literal|null
condition|?
literal|""
else|:
literal|","
operator|)
operator|+
operator|(
name|exception
operator|==
literal|null
condition|?
literal|""
else|:
name|exception
operator|.
name|getMessage
argument_list|()
operator|)
operator|+
literal|")"
return|;
block|}
block|}
end_class

end_unit

