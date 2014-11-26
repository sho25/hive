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
name|SSHCommand
extends|extends
name|AbstractSSHCommand
argument_list|<
name|SSHResult
argument_list|>
block|{
specifier|private
specifier|final
name|SSHCommandExecutor
name|executor
decl_stmt|;
specifier|private
specifier|final
name|String
name|command
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|reportErrors
decl_stmt|;
specifier|public
name|SSHCommand
parameter_list|(
name|SSHCommandExecutor
name|executor
parameter_list|,
name|String
name|privateKey
parameter_list|,
name|String
name|user
parameter_list|,
name|String
name|host
parameter_list|,
name|int
name|instance
parameter_list|,
name|String
name|command
parameter_list|,
name|boolean
name|reportErrors
parameter_list|)
block|{
name|super
argument_list|(
name|privateKey
argument_list|,
name|user
argument_list|,
name|host
argument_list|,
name|instance
argument_list|)
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|executor
expr_stmt|;
name|this
operator|.
name|command
operator|=
name|command
expr_stmt|;
name|this
operator|.
name|reportErrors
operator|=
name|reportErrors
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SSHResult
name|call
parameter_list|()
block|{
name|executor
operator|.
name|execute
argument_list|(
name|this
argument_list|)
expr_stmt|;
return|return
operator|new
name|SSHResult
argument_list|(
name|getUser
argument_list|()
argument_list|,
name|getHost
argument_list|()
argument_list|,
name|getInstance
argument_list|()
argument_list|,
name|getCommand
argument_list|()
argument_list|,
name|getExitCode
argument_list|()
argument_list|,
name|getException
argument_list|()
argument_list|,
name|getOutput
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isReportErrors
parameter_list|()
block|{
return|return
name|reportErrors
return|;
block|}
specifier|public
name|String
name|getCommand
parameter_list|()
block|{
return|return
name|command
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
literal|"SSHCommand [command="
operator|+
name|command
operator|+
literal|", getHost()="
operator|+
name|getHost
argument_list|()
operator|+
literal|", getInstance()="
operator|+
name|getInstance
argument_list|()
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

