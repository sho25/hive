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
name|RSyncCommand
extends|extends
name|AbstractSSHCommand
argument_list|<
name|RSyncResult
argument_list|>
block|{
specifier|private
specifier|final
name|RSyncCommandExecutor
name|executor
decl_stmt|;
specifier|private
specifier|final
name|String
name|localFile
decl_stmt|;
specifier|private
specifier|final
name|String
name|remoteFile
decl_stmt|;
specifier|private
name|long
name|elapsedTimeInMs
decl_stmt|;
specifier|private
name|RSyncCommand
operator|.
name|Type
name|type
decl_stmt|;
specifier|public
name|RSyncCommand
parameter_list|(
name|RSyncCommandExecutor
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
name|localFile
parameter_list|,
name|String
name|remoteFile
parameter_list|,
name|RSyncCommand
operator|.
name|Type
name|type
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
name|localFile
operator|=
name|localFile
expr_stmt|;
name|this
operator|.
name|remoteFile
operator|=
name|remoteFile
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
specifier|public
name|RSyncCommand
operator|.
name|Type
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
specifier|public
name|void
name|setElapsedTimeInMs
parameter_list|(
name|long
name|timeInMs
parameter_list|)
block|{
name|this
operator|.
name|elapsedTimeInMs
operator|=
name|timeInMs
expr_stmt|;
block|}
specifier|public
name|String
name|getLocalFile
parameter_list|()
block|{
return|return
name|localFile
return|;
block|}
specifier|public
name|String
name|getRemoteFile
parameter_list|()
block|{
return|return
name|remoteFile
return|;
block|}
annotation|@
name|Override
specifier|public
name|RSyncResult
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
name|RSyncResult
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
name|getLocalFile
argument_list|()
argument_list|,
name|getRemoteFile
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
argument_list|,
name|getElapsedTimeInMs
argument_list|()
argument_list|)
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
literal|"RSyncCommand [executor="
operator|+
name|executor
operator|+
literal|", localFile="
operator|+
name|localFile
operator|+
literal|", remoteFile="
operator|+
name|remoteFile
operator|+
literal|", type="
operator|+
name|type
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
specifier|public
name|long
name|getElapsedTimeInMs
parameter_list|()
block|{
return|return
name|elapsedTimeInMs
return|;
block|}
specifier|public
specifier|static
enum|enum
name|Type
block|{
name|FROM_LOCAL
parameter_list|()
operator|,
constructor|TO_LOCAL(
block|)
enum|,
name|TO_LOCAL_NON_RECURSIVE
parameter_list|()
constructor_decl|;
block|}
end_class

unit|}
end_unit

