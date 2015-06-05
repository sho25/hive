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
name|HCatClient
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
name|NoopReplicationTask
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
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_comment
comment|/**  * EXIMReplicationTaskFactory is an export-import based ReplicationTask.Factory.  *  * It's primary mode of enabling replication is by translating each event it gets  * from the notification subsystem into hive commands that essentially export data  * to be copied over and imported on the other end.  *  * The Commands that Tasks return here are expected to be hive commands.  */
end_comment

begin_class
specifier|public
class|class
name|EximReplicationTaskFactory
implements|implements
name|ReplicationTask
operator|.
name|Factory
block|{
specifier|public
name|ReplicationTask
name|create
parameter_list|(
name|HCatClient
name|client
parameter_list|,
name|HCatNotificationEvent
name|event
parameter_list|)
block|{
comment|// TODO : Java 1.7+ support using String with switches, but IDEs don't all seem to know that.
comment|// If casing is fine for now. But we should eventually remove this. Also, I didn't want to
comment|// create another enum just for this.
if|if
condition|(
name|event
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_DATABASE_EVENT
argument_list|)
condition|)
block|{
return|return
operator|new
name|CreateDatabaseReplicationTask
argument_list|(
name|event
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DROP_DATABASE_EVENT
argument_list|)
condition|)
block|{
return|return
operator|new
name|DropDatabaseReplicationTask
argument_list|(
name|event
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_TABLE_EVENT
argument_list|)
condition|)
block|{
return|return
operator|new
name|CreateTableReplicationTask
argument_list|(
name|event
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DROP_TABLE_EVENT
argument_list|)
condition|)
block|{
return|return
operator|new
name|DropTableReplicationTask
argument_list|(
name|event
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_ADD_PARTITION_EVENT
argument_list|)
condition|)
block|{
return|return
operator|new
name|AddPartitionReplicationTask
argument_list|(
name|event
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DROP_PARTITION_EVENT
argument_list|)
condition|)
block|{
return|return
operator|new
name|DropPartitionReplicationTask
argument_list|(
name|event
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_ALTER_TABLE_EVENT
argument_list|)
condition|)
block|{
return|return
operator|new
name|AlterTableReplicationTask
argument_list|(
name|event
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_ALTER_PARTITION_EVENT
argument_list|)
condition|)
block|{
return|return
operator|new
name|AlterPartitionReplicationTask
argument_list|(
name|event
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|event
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HCAT_INSERT_EVENT
argument_list|)
condition|)
block|{
return|return
operator|new
name|InsertReplicationTask
argument_list|(
name|event
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unrecognized Event type, no replication task available"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

