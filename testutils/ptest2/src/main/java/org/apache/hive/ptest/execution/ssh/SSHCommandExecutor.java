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
name|Constants
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
name|SSHCommandExecutor
block|{
specifier|private
specifier|final
name|Logger
name|mLogger
decl_stmt|;
specifier|public
name|SSHCommandExecutor
parameter_list|(
name|Logger
name|logger
parameter_list|)
block|{
name|mLogger
operator|=
name|logger
expr_stmt|;
block|}
comment|/**    * Execute the given command via the ssh command line tool. If the command    * exits with status code 255 the command will be tries up to three times.    */
specifier|public
name|void
name|execute
parameter_list|(
name|SSHCommand
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
try|try
block|{
name|String
name|commandText
init|=
name|String
operator|.
name|format
argument_list|(
literal|"ssh -v -i %s -l %s %s '%s'"
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
name|getCommand
argument_list|()
argument_list|)
decl_stmt|;
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
name|cmd
operator|=
operator|new
name|LocalCommand
argument_list|(
name|mLogger
argument_list|,
name|collector
argument_list|,
name|commandText
argument_list|)
expr_stmt|;
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
operator|==
name|Constants
operator|.
name|EXIT_CODE_UNKNOWN
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
literal|5
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
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|command
operator|.
name|getExitCode
argument_list|()
operator|==
name|Constants
operator|.
name|EXIT_CODE_SUCCESS
condition|)
block|{
name|command
operator|.
name|setExitCode
argument_list|(
name|Constants
operator|.
name|EXIT_CODE_EXCEPTION
argument_list|)
expr_stmt|;
block|}
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

