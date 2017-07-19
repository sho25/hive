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
name|metadata
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
name|ql
operator|.
name|ErrorMsg
import|;
end_import

begin_comment
comment|/**  * Generic exception class for Hive.  */
end_comment

begin_class
specifier|public
class|class
name|HiveException
extends|extends
name|Exception
block|{
comment|/**    * Standard predefined message with error code and possibly SQL State, etc.    */
specifier|private
name|ErrorMsg
name|canonicalErrorMsg
init|=
name|ErrorMsg
operator|.
name|GENERIC_ERROR
decl_stmt|;
comment|/**    * Error Messages returned from remote exception (eg. hadoop error)    */
specifier|private
name|String
name|remoteErrorMsg
decl_stmt|;
specifier|public
name|HiveException
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|HiveException
parameter_list|(
name|String
name|message
parameter_list|)
block|{
name|super
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveException
parameter_list|(
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|cause
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveException
parameter_list|(
name|String
name|message
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|message
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveException
parameter_list|(
name|ErrorMsg
name|message
parameter_list|,
name|String
modifier|...
name|msgArgs
parameter_list|)
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|message
argument_list|,
name|msgArgs
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveException
parameter_list|(
name|Throwable
name|cause
parameter_list|,
name|ErrorMsg
name|errorMsg
parameter_list|,
name|String
modifier|...
name|msgArgs
parameter_list|)
block|{
name|this
argument_list|(
name|cause
argument_list|,
literal|null
argument_list|,
name|errorMsg
argument_list|,
name|msgArgs
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveException
parameter_list|(
name|Throwable
name|cause
parameter_list|,
name|ErrorMsg
name|errorMsg
parameter_list|)
block|{
name|this
argument_list|(
name|cause
argument_list|,
literal|null
argument_list|,
name|errorMsg
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveException
parameter_list|(
name|ErrorMsg
name|errorMsg
parameter_list|)
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|errorMsg
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**    * This is the recommended constructor to use since it helps use    * canonical messages throughout and propagate remote errors.    *    * @param errorMsg Canonical error message    * @param msgArgs message arguments if message is parametrized; must be {@code null} is message takes no arguments    */
specifier|public
name|HiveException
parameter_list|(
name|Throwable
name|cause
parameter_list|,
name|String
name|remErrMsg
parameter_list|,
name|ErrorMsg
name|errorMsg
parameter_list|,
name|String
modifier|...
name|msgArgs
parameter_list|)
block|{
name|super
argument_list|(
name|errorMsg
operator|.
name|format
argument_list|(
name|msgArgs
argument_list|)
argument_list|,
name|cause
argument_list|)
expr_stmt|;
name|canonicalErrorMsg
operator|=
name|errorMsg
expr_stmt|;
name|remoteErrorMsg
operator|=
name|remErrMsg
expr_stmt|;
block|}
comment|/**    * @return {@link ErrorMsg#GENERIC_ERROR} by default    */
specifier|public
name|ErrorMsg
name|getCanonicalErrorMsg
parameter_list|()
block|{
return|return
name|canonicalErrorMsg
return|;
block|}
specifier|public
name|String
name|getRemoteErrorMsg
parameter_list|()
block|{
return|return
name|remoteErrorMsg
return|;
block|}
block|}
end_class

end_unit

