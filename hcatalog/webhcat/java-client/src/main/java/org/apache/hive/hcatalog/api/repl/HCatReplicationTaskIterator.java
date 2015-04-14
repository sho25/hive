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
name|metastore
operator|.
name|IMetaStoreClient
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
name|common
operator|.
name|HCatException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_class
specifier|public
class|class
name|HCatReplicationTaskIterator
implements|implements
name|Iterator
argument_list|<
name|ReplicationTask
argument_list|>
block|{
specifier|private
name|Iterator
argument_list|<
name|HCatNotificationEvent
argument_list|>
name|notifIter
init|=
literal|null
decl_stmt|;
specifier|private
class|class
name|HCatReplicationTaskIteratorNotificationFilter
implements|implements
name|IMetaStoreClient
operator|.
name|NotificationFilter
block|{
specifier|private
name|String
name|dbName
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|public
name|HCatReplicationTaskIteratorNotificationFilter
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|NotificationEvent
name|event
parameter_list|)
block|{
if|if
condition|(
name|event
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
comment|// get rid of trivial case first, so that we can safely assume non-null
block|}
if|if
condition|(
name|this
operator|.
name|dbName
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
comment|// if our dbName is null, we're interested in all wh events
block|}
if|if
condition|(
name|this
operator|.
name|dbName
operator|.
name|equalsIgnoreCase
argument_list|(
name|event
operator|.
name|getDbName
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
operator|(
name|this
operator|.
name|tableName
operator|==
literal|null
operator|)
comment|// if our dbName is equal, but tableName is blank, we're interested in this db-level event
operator|||
operator|(
name|this
operator|.
name|tableName
operator|.
name|equalsIgnoreCase
argument_list|(
name|event
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|)
comment|// table level event that matches us
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
specifier|public
name|HCatReplicationTaskIterator
parameter_list|(
name|HCatClient
name|hcatClient
parameter_list|,
name|long
name|eventFrom
parameter_list|,
name|int
name|maxEvents
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|HCatException
block|{
name|init
argument_list|(
name|hcatClient
argument_list|,
name|eventFrom
argument_list|,
name|maxEvents
argument_list|,
operator|new
name|HCatReplicationTaskIteratorNotificationFilter
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HCatReplicationTaskIterator
parameter_list|(
name|HCatClient
name|hcatClient
parameter_list|,
name|long
name|eventFrom
parameter_list|,
name|int
name|maxEvents
parameter_list|,
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|filter
parameter_list|)
throws|throws
name|HCatException
block|{
name|init
argument_list|(
name|hcatClient
argument_list|,
name|eventFrom
argument_list|,
name|maxEvents
argument_list|,
name|filter
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|init
parameter_list|(
name|HCatClient
name|hcatClient
parameter_list|,
name|long
name|eventFrom
parameter_list|,
name|int
name|maxEvents
parameter_list|,
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|filter
parameter_list|)
throws|throws
name|HCatException
block|{
comment|// Simple implementation for now, this will later expand to do DAG evaluation.
name|this
operator|.
name|notifIter
operator|=
name|hcatClient
operator|.
name|getNextNotification
argument_list|(
name|eventFrom
argument_list|,
name|maxEvents
argument_list|,
name|filter
argument_list|)
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|notifIter
operator|.
name|hasNext
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationTask
name|next
parameter_list|()
block|{
return|return
name|ReplicationTask
operator|.
name|create
argument_list|(
name|notifIter
operator|.
name|next
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"remove() not supported on HCatReplicationTaskIterator"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

