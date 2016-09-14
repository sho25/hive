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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|atomic
operator|.
name|AtomicInteger
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
name|ssh
operator|.
name|SSHCommand
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
name|ssh
operator|.
name|SSHCommandExecutor
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_class
specifier|public
class|class
name|MockSSHCommandExecutor
extends|extends
name|SSHCommandExecutor
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|mCommands
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Queue
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|mFailures
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|matchCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
name|MockSSHCommandExecutor
parameter_list|(
name|Logger
name|logger
parameter_list|)
block|{
name|super
argument_list|(
name|logger
argument_list|)
expr_stmt|;
name|mCommands
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
name|mFailures
operator|=
name|Maps
operator|.
name|newHashMap
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|List
argument_list|<
name|String
argument_list|>
name|getCommands
parameter_list|()
block|{
return|return
name|mCommands
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|putFailure
parameter_list|(
name|String
name|command
parameter_list|,
name|Integer
modifier|...
name|exitCodes
parameter_list|)
block|{
name|Queue
argument_list|<
name|Integer
argument_list|>
name|queue
init|=
name|mFailures
operator|.
name|get
argument_list|(
name|command
argument_list|)
decl_stmt|;
if|if
condition|(
name|queue
operator|==
literal|null
condition|)
block|{
name|queue
operator|=
operator|new
name|LinkedList
argument_list|<
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
name|mFailures
operator|.
name|put
argument_list|(
name|command
argument_list|,
name|queue
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|queue
operator|=
name|mFailures
operator|.
name|get
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Integer
name|exitCode
range|:
name|exitCodes
control|)
block|{
name|queue
operator|.
name|add
argument_list|(
name|exitCode
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|execute
parameter_list|(
name|SSHCommand
name|command
parameter_list|)
block|{
name|mCommands
operator|.
name|add
argument_list|(
name|command
operator|.
name|getCommand
argument_list|()
argument_list|)
expr_stmt|;
name|command
operator|.
name|setOutput
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|Queue
argument_list|<
name|Integer
argument_list|>
name|queue
init|=
name|mFailures
operator|.
name|get
argument_list|(
name|command
operator|.
name|getCommand
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|queue
operator|==
literal|null
operator|||
name|queue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|command
operator|.
name|setExitCode
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|matchCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|command
operator|.
name|setExitCode
argument_list|(
name|queue
operator|.
name|remove
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|getMatchCount
parameter_list|()
block|{
return|return
name|matchCount
operator|.
name|get
argument_list|()
return|;
block|}
block|}
end_class

end_unit

