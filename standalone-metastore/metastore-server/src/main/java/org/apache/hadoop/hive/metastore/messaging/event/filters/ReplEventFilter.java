begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
operator|.
name|messaging
operator|.
name|event
operator|.
name|filters
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
name|common
operator|.
name|repl
operator|.
name|ReplScope
import|;
end_import

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
name|metastore
operator|.
name|api
operator|.
name|NotificationEvent
import|;
end_import

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
name|metastore
operator|.
name|messaging
operator|.
name|MessageBuilder
import|;
end_import

begin_comment
comment|/**  * Utility function that constructs a notification filter to check if table is accepted for replication.  */
end_comment

begin_class
specifier|public
class|class
name|ReplEventFilter
extends|extends
name|BasicFilter
block|{
specifier|private
specifier|final
name|ReplScope
name|replScope
decl_stmt|;
specifier|public
name|ReplEventFilter
parameter_list|(
specifier|final
name|ReplScope
name|replScope
parameter_list|)
block|{
name|this
operator|.
name|replScope
operator|=
name|replScope
expr_stmt|;
block|}
annotation|@
name|Override
name|boolean
name|shouldAccept
parameter_list|(
specifier|final
name|NotificationEvent
name|event
parameter_list|)
block|{
comment|// All txn related events are global ones and should be always accepted.
comment|// For other events, if the DB/table names are included as per replication scope, then should
comment|// accept the event. For alter table with table name filter, bootstrap of the table will be done if the new table
comment|// name matches the filter but the old table name does not. This can be judge only after deserialize of the message.
return|return
operator|(
name|isTxnRelatedEvent
argument_list|(
name|event
argument_list|)
operator|||
name|replScope
operator|.
name|includedInReplScope
argument_list|(
name|event
operator|.
name|getDbName
argument_list|()
argument_list|,
name|event
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|||
operator|(
name|replScope
operator|.
name|dbIncludedInReplScope
argument_list|(
name|event
operator|.
name|getDbName
argument_list|()
argument_list|)
operator|&&
name|event
operator|.
name|getEventType
argument_list|()
operator|.
name|equals
argument_list|(
name|MessageBuilder
operator|.
name|ALTER_TABLE_EVENT
argument_list|)
operator|)
operator|)
return|;
block|}
block|}
end_class

end_unit

