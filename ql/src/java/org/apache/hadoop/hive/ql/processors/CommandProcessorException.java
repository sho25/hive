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
name|ql
operator|.
name|processors
package|;
end_package

begin_comment
comment|/**  * Exception thrown during command processing class.  */
end_comment

begin_class
specifier|public
class|class
name|CommandProcessorException
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
specifier|private
specifier|final
name|int
name|responseCode
decl_stmt|;
specifier|private
specifier|final
name|int
name|hiveErrorCode
decl_stmt|;
specifier|private
specifier|final
name|String
name|sqlState
decl_stmt|;
specifier|public
name|CommandProcessorException
parameter_list|(
name|int
name|responseCode
parameter_list|)
block|{
name|this
argument_list|(
name|responseCode
argument_list|,
operator|-
literal|1
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
name|CommandProcessorException
parameter_list|(
name|String
name|errorMessage
parameter_list|)
block|{
name|this
argument_list|(
name|errorMessage
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CommandProcessorException
parameter_list|(
name|Throwable
name|exception
parameter_list|)
block|{
name|this
argument_list|(
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CommandProcessorException
parameter_list|(
name|String
name|errorMessage
parameter_list|,
name|Throwable
name|exception
parameter_list|)
block|{
name|this
argument_list|(
literal|1
argument_list|,
operator|-
literal|1
argument_list|,
name|errorMessage
argument_list|,
literal|null
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CommandProcessorException
parameter_list|(
name|int
name|responseCode
parameter_list|,
name|int
name|hiveErrorCode
parameter_list|,
name|String
name|errorMessage
parameter_list|,
name|String
name|sqlState
parameter_list|,
name|Throwable
name|exception
parameter_list|)
block|{
name|super
argument_list|(
name|errorMessage
argument_list|,
name|exception
argument_list|)
expr_stmt|;
name|this
operator|.
name|responseCode
operator|=
name|responseCode
expr_stmt|;
name|this
operator|.
name|hiveErrorCode
operator|=
name|hiveErrorCode
expr_stmt|;
name|this
operator|.
name|sqlState
operator|=
name|sqlState
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
name|getSqlState
parameter_list|()
block|{
return|return
name|sqlState
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"(responseCode = "
operator|+
name|responseCode
operator|+
literal|", errorMessage = "
operator|+
name|getMessage
argument_list|()
operator|+
literal|", "
operator|+
operator|(
name|hiveErrorCode
operator|>
literal|0
condition|?
literal|"hiveErrorCode = "
operator|+
name|hiveErrorCode
operator|+
literal|", "
else|:
literal|""
operator|)
operator|+
literal|"SQLState = "
operator|+
name|sqlState
operator|+
operator|(
name|getCause
argument_list|()
operator|==
literal|null
condition|?
literal|""
else|:
literal|", exception = "
operator|+
name|getCause
argument_list|()
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

