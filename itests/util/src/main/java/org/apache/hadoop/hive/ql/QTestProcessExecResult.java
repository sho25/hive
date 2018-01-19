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
package|;
end_package

begin_comment
comment|/**  * Standard output and return code of a process executed during the qtests.  */
end_comment

begin_class
specifier|public
class|class
name|QTestProcessExecResult
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TRUNCATED_OUTPUT
init|=
literal|"Output was too long and had to be truncated..."
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|short
name|MAX_OUTPUT_CHAR_LENGTH
init|=
literal|2000
decl_stmt|;
specifier|private
specifier|final
name|int
name|returnCode
decl_stmt|;
specifier|private
specifier|final
name|String
name|standardOut
decl_stmt|;
name|QTestProcessExecResult
parameter_list|(
name|int
name|code
parameter_list|,
name|String
name|output
parameter_list|)
block|{
name|this
operator|.
name|returnCode
operator|=
name|code
expr_stmt|;
name|this
operator|.
name|standardOut
operator|=
name|truncatefNeeded
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return executed process return code    */
specifier|public
name|int
name|getReturnCode
parameter_list|()
block|{
return|return
name|this
operator|.
name|returnCode
return|;
block|}
comment|/**    * @return output captured from stdout while process was executing    */
specifier|public
name|String
name|getCapturedOutput
parameter_list|()
block|{
return|return
name|this
operator|.
name|standardOut
return|;
block|}
specifier|public
specifier|static
name|QTestProcessExecResult
name|create
parameter_list|(
name|int
name|code
parameter_list|,
name|String
name|output
parameter_list|)
block|{
return|return
operator|new
name|QTestProcessExecResult
argument_list|(
name|code
argument_list|,
name|output
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|QTestProcessExecResult
name|createWithoutOutput
parameter_list|(
name|int
name|code
parameter_list|)
block|{
return|return
operator|new
name|QTestProcessExecResult
argument_list|(
name|code
argument_list|,
literal|""
argument_list|)
return|;
block|}
specifier|private
name|String
name|truncatefNeeded
parameter_list|(
name|String
name|orig
parameter_list|)
block|{
if|if
condition|(
name|orig
operator|.
name|length
argument_list|()
operator|>
name|MAX_OUTPUT_CHAR_LENGTH
condition|)
block|{
return|return
name|orig
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|MAX_OUTPUT_CHAR_LENGTH
argument_list|)
operator|+
literal|"\r\n"
operator|+
name|TRUNCATED_OUTPUT
return|;
block|}
else|else
block|{
return|return
name|orig
return|;
block|}
block|}
block|}
end_class

end_unit

