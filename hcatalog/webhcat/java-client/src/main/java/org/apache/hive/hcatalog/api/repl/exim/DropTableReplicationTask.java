begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
operator|.
name|exim
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
name|Command
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
name|ReplicationTask
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
name|ReplicationUtils
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
name|DropTableCommand
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
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
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
name|messaging
operator|.
name|DropTableMessage
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_class
specifier|public
class|class
name|DropTableReplicationTask
extends|extends
name|ReplicationTask
block|{
specifier|private
name|DropTableMessage
name|dropTableMessage
init|=
literal|null
decl_stmt|;
specifier|public
name|DropTableReplicationTask
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
name|validateEventType
argument_list|(
name|event
argument_list|,
name|HCatConstants
operator|.
name|HCAT_DROP_TABLE_EVENT
argument_list|)
expr_stmt|;
name|dropTableMessage
operator|=
name|messageFactory
operator|.
name|getDeserializer
argument_list|()
operator|.
name|getDropTableMessage
argument_list|(
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|needsStagingDirs
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
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
name|Collections
operator|.
name|singletonList
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
return|;
block|}
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
specifier|final
name|String
name|dstDbName
init|=
name|ReplicationUtils
operator|.
name|mapIfMapAvailable
argument_list|(
name|dropTableMessage
operator|.
name|getDB
argument_list|()
argument_list|,
name|dbNameMapping
argument_list|)
decl_stmt|;
specifier|final
name|String
name|dstTableName
init|=
name|ReplicationUtils
operator|.
name|mapIfMapAvailable
argument_list|(
name|dropTableMessage
operator|.
name|getTable
argument_list|()
argument_list|,
name|tableNameMapping
argument_list|)
decl_stmt|;
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|DropTableCommand
argument_list|(
name|dstDbName
argument_list|,
name|dstTableName
argument_list|,
literal|true
argument_list|,
name|event
operator|.
name|getEventId
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

