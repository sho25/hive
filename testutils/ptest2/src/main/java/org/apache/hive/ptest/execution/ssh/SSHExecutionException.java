begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
operator|.
name|ssh
package|;
end_package

begin_class
specifier|public
class|class
name|SSHExecutionException
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
name|RemoteCommandResult
name|result
decl_stmt|;
specifier|public
name|SSHExecutionException
parameter_list|(
name|RemoteCommandResult
name|result
parameter_list|)
block|{
name|super
argument_list|(
name|result
operator|.
name|toString
argument_list|()
argument_list|,
name|result
operator|.
name|getException
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|result
operator|=
name|result
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getMessage
parameter_list|()
block|{
name|String
name|msg
init|=
name|String
operator|.
name|format
argument_list|(
literal|"%s: '%s'"
argument_list|,
name|super
operator|.
name|getMessage
argument_list|()
argument_list|,
name|result
operator|.
name|getOutput
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|msg
return|;
block|}
block|}
end_class

end_unit

