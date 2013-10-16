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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Semaphore
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
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
name|LocalCommand
import|;
end_import

begin_import
import|import
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
name|LocalCommand
operator|.
name|CollectPolicy
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_class
specifier|public
class|class
name|RSyncCommandExecutor
block|{
specifier|private
specifier|final
name|Logger
name|mLogger
decl_stmt|;
specifier|private
specifier|final
name|Semaphore
name|mSemaphore
decl_stmt|;
specifier|public
name|RSyncCommandExecutor
parameter_list|(
name|Logger
name|logger
parameter_list|)
block|{
name|mLogger
operator|=
name|logger
expr_stmt|;
name|mSemaphore
operator|=
operator|new
name|Semaphore
argument_list|(
literal|5
argument_list|)
expr_stmt|;
block|}
comment|/**    * Execute the given RSync. If the command exits with a non-zero    * exit status the command will be retried up to three times.    */
specifier|public
name|void
name|execute
parameter_list|(
name|RSyncCommand
name|command
parameter_list|)
block|{
name|CollectPolicy
name|collector
init|=
operator|new
name|CollectPolicy
argument_list|()
decl_stmt|;
name|boolean
name|release
init|=
literal|false
decl_stmt|;
try|try
block|{
name|mSemaphore
operator|.
name|acquire
argument_list|()
expr_stmt|;
name|release
operator|=
literal|true
expr_stmt|;
name|int
name|attempts
init|=
literal|0
decl_stmt|;
name|boolean
name|retry
decl_stmt|;
name|LocalCommand
name|cmd
decl_stmt|;
do|do
block|{
name|retry
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|command
operator|.
name|getType
argument_list|()
operator|==
name|RSyncCommand
operator|.
name|Type
operator|.
name|TO_LOCAL
condition|)
block|{
name|cmd
operator|=
operator|new
name|LocalCommand
argument_list|(
name|mLogger
argument_list|,
name|collector
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"timeout 1h rsync -vaPe \"ssh -i %s\" --timeout 600 %s@%s:%s %s"
argument_list|,
name|command
operator|.
name|getPrivateKey
argument_list|()
argument_list|,
name|command
operator|.
name|getUser
argument_list|()
argument_list|,
name|command
operator|.
name|getHost
argument_list|()
argument_list|,
name|command
operator|.
name|getRemoteFile
argument_list|()
argument_list|,
name|command
operator|.
name|getLocalFile
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|command
operator|.
name|getType
argument_list|()
operator|==
name|RSyncCommand
operator|.
name|Type
operator|.
name|FROM_LOCAL
condition|)
block|{
name|cmd
operator|=
operator|new
name|LocalCommand
argument_list|(
name|mLogger
argument_list|,
name|collector
argument_list|,
name|String
operator|.
name|format
argument_list|(
literal|"timeout 1h rsync -vaPe \"ssh -i %s\" --timeout 600 --delete --delete-during --force %s %s@%s:%s"
argument_list|,
name|command
operator|.
name|getPrivateKey
argument_list|()
argument_list|,
name|command
operator|.
name|getLocalFile
argument_list|()
argument_list|,
name|command
operator|.
name|getUser
argument_list|()
argument_list|,
name|command
operator|.
name|getHost
argument_list|()
argument_list|,
name|command
operator|.
name|getRemoteFile
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|command
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
comment|// 12 is timeout and 255 is unspecified error
if|if
condition|(
name|attempts
operator|++
operator|<=
literal|3
operator|&&
name|cmd
operator|.
name|getExitCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|mLogger
operator|.
name|warn
argument_list|(
literal|"Command exited with "
operator|+
name|cmd
operator|.
name|getExitCode
argument_list|()
operator|+
literal|", will retry: "
operator|+
name|command
argument_list|)
expr_stmt|;
name|retry
operator|=
literal|true
expr_stmt|;
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|sleep
argument_list|(
literal|20
argument_list|)
expr_stmt|;
block|}
block|}
do|while
condition|(
name|retry
condition|)
do|;
comment|// an error occurred, re-try
name|command
operator|.
name|setExitCode
argument_list|(
name|cmd
operator|.
name|getExitCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|command
operator|.
name|setException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|command
operator|.
name|setException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|release
condition|)
block|{
name|mSemaphore
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
name|command
operator|.
name|setOutput
argument_list|(
name|collector
operator|.
name|getOutput
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

