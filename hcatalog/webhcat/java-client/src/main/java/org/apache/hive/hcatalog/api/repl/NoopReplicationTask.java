begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|api
operator|.
name|repl
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|api
operator|.
name|HCatNotificationEvent
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
name|hcatalog
operator|.
name|api
operator|.
name|repl
operator|.
name|commands
operator|.
name|NoopCommand
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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

begin_comment
comment|/**  * Noop replication task - a replication task that is actionable,  * does not need any further info, and returns NoopCommands.  *  * Useful for testing, and also for tasks that need to be represented  * but actually do nothing.  */
end_comment

begin_class
specifier|public
class|class
name|NoopReplicationTask
extends|extends
name|ReplicationTask
block|{
name|List
argument_list|<
name|Command
argument_list|>
name|noopReturn
init|=
literal|null
decl_stmt|;
specifier|public
name|NoopReplicationTask
parameter_list|(
name|HCatNotificationEvent
name|event
parameter_list|)
block|{
name|super
argument_list|(
name|event
argument_list|)
expr_stmt|;
name|noopReturn
operator|=
operator|new
name|ArrayList
argument_list|<
name|Command
argument_list|>
argument_list|()
expr_stmt|;
name|noopReturn
operator|.
name|add
argument_list|(
operator|new
name|NoopCommand
argument_list|(
name|event
operator|.
name|getEventId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needsStagingDirs
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isActionable
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|/**    * Returns a list of commands to send to a hive driver on the source warehouse    * @return a list of commands to send to a hive driver on the source warehouse    */
annotation|@
name|Override
specifier|public
name|Iterable
argument_list|<
name|?
extends|extends
name|Command
argument_list|>
name|getSrcWhCommands
parameter_list|()
block|{
name|verifyActionable
argument_list|()
expr_stmt|;
return|return
name|noopReturn
return|;
block|}
comment|/**    * Returns a list of commands to send to a hive driver on the dest warehouse    * @return a list of commands to send to a hive driver on the dest warehouse    */
annotation|@
name|Override
specifier|public
name|Iterable
argument_list|<
name|?
extends|extends
name|Command
argument_list|>
name|getDstWhCommands
parameter_list|()
block|{
name|verifyActionable
argument_list|()
expr_stmt|;
return|return
name|noopReturn
return|;
block|}
block|}
end_class

end_unit

