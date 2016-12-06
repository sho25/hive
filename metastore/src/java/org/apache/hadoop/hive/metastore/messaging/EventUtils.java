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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|messaging
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
name|conf
operator|.
name|HiveConf
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
name|thrift
operator|.
name|TException
import|;
end_import

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
name|ArrayList
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_class
specifier|public
class|class
name|EventUtils
block|{
comment|/**    * Utility function that constructs a notification filter to match a given db name and/or table name.    * If dbName == null, fetches all warehouse events.    * If dnName != null, but tableName == null, fetches all events for the db    * If dbName != null&& tableName != null, fetches all events for the specified table    * @param dbName    * @param tableName    * @return    */
specifier|public
specifier|static
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|getDbTblNotificationFilter
parameter_list|(
specifier|final
name|String
name|dbName
parameter_list|,
specifier|final
name|String
name|tableName
parameter_list|)
block|{
return|return
operator|new
name|IMetaStoreClient
operator|.
name|NotificationFilter
argument_list|()
block|{
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
name|tableName
operator|==
literal|null
operator|)
comment|// if our dbName is equal, but tableName is blank, we're interested in this db-level event
operator|||
operator|(
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
return|;
block|}
specifier|public
specifier|static
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|getEventBoundaryFilter
parameter_list|(
specifier|final
name|Long
name|eventFrom
parameter_list|,
specifier|final
name|Long
name|eventTo
parameter_list|)
block|{
return|return
operator|new
name|IMetaStoreClient
operator|.
name|NotificationFilter
argument_list|()
block|{
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
operator|(
name|event
operator|==
literal|null
operator|)
operator|||
operator|(
name|event
operator|.
name|getEventId
argument_list|()
operator|<
name|eventFrom
operator|)
operator|||
operator|(
name|event
operator|.
name|getEventId
argument_list|()
operator|>
name|eventTo
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
return|;
block|}
specifier|public
specifier|static
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|andFilter
parameter_list|(
specifier|final
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|filter1
parameter_list|,
specifier|final
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|filter2
parameter_list|)
block|{
return|return
operator|new
name|IMetaStoreClient
operator|.
name|NotificationFilter
argument_list|()
block|{
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
return|return
name|filter1
operator|.
name|accept
argument_list|(
name|event
argument_list|)
operator|&&
name|filter2
operator|.
name|accept
argument_list|(
name|event
argument_list|)
return|;
block|}
block|}
return|;
block|}
specifier|public
interface|interface
name|NotificationFetcher
block|{
specifier|public
name|int
name|getBatchSize
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|public
name|long
name|getCurrentNotificationEventId
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|public
name|List
argument_list|<
name|NotificationEvent
argument_list|>
name|getNextNotificationEvents
parameter_list|(
name|long
name|pos
parameter_list|,
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|filter
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|// MetaStoreClient-based impl of NotificationFetcher
specifier|public
specifier|static
class|class
name|MSClientNotificationFetcher
implements|implements
name|NotificationFetcher
block|{
specifier|private
name|IMetaStoreClient
name|msc
init|=
literal|null
decl_stmt|;
specifier|private
name|Integer
name|batchSize
init|=
literal|null
decl_stmt|;
specifier|public
name|MSClientNotificationFetcher
parameter_list|(
name|IMetaStoreClient
name|msc
parameter_list|)
block|{
name|this
operator|.
name|msc
operator|=
name|msc
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getBatchSize
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|batchSize
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|batchSize
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|msc
operator|.
name|getConfigValue
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_BATCH_RETRIEVE_MAX
operator|.
name|varname
argument_list|,
literal|"50"
argument_list|)
argument_list|)
expr_stmt|;
comment|// TODO: we're asking the metastore what its configuration for this var is - we may
comment|// want to revisit to pull from client side instead. The reason I have it this way
comment|// is because the metastore is more likely to have a reasonable config for this than
comment|// an arbitrary client.
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|batchSize
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCurrentNotificationEventId
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|msc
operator|.
name|getCurrentNotificationEventId
argument_list|()
operator|.
name|getEventId
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|NotificationEvent
argument_list|>
name|getNextNotificationEvents
parameter_list|(
name|long
name|pos
parameter_list|,
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|filter
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|msc
operator|.
name|getNextNotification
argument_list|(
name|pos
argument_list|,
name|getBatchSize
argument_list|()
argument_list|,
name|filter
argument_list|)
operator|.
name|getEvents
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|NotificationEventIterator
implements|implements
name|Iterator
argument_list|<
name|NotificationEvent
argument_list|>
block|{
specifier|private
name|NotificationFetcher
name|nfetcher
decl_stmt|;
specifier|private
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|filter
decl_stmt|;
specifier|private
name|int
name|maxEvents
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|NotificationEvent
argument_list|>
name|batchIter
init|=
literal|null
decl_stmt|;
specifier|private
name|List
argument_list|<
name|NotificationEvent
argument_list|>
name|batch
init|=
literal|null
decl_stmt|;
specifier|private
name|long
name|pos
decl_stmt|;
specifier|private
name|long
name|maxPos
decl_stmt|;
specifier|private
name|int
name|eventCount
decl_stmt|;
specifier|public
name|NotificationEventIterator
parameter_list|(
name|NotificationFetcher
name|nfetcher
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
name|IOException
block|{
name|init
argument_list|(
name|nfetcher
argument_list|,
name|eventFrom
argument_list|,
name|maxEvents
argument_list|,
name|EventUtils
operator|.
name|getDbTblNotificationFilter
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
comment|// using init(..) instead of this(..) because the EventUtils.getDbTblNotificationFilter
comment|// is an operation that needs to run before delegating to the other ctor, and this messes up chaining
comment|// ctors
block|}
specifier|public
name|NotificationEventIterator
parameter_list|(
name|NotificationFetcher
name|nfetcher
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
name|IOException
block|{
name|init
argument_list|(
name|nfetcher
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
name|NotificationFetcher
name|nfetcher
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
name|IOException
block|{
name|this
operator|.
name|nfetcher
operator|=
name|nfetcher
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
name|this
operator|.
name|pos
operator|=
name|eventFrom
expr_stmt|;
if|if
condition|(
name|maxEvents
operator|<
literal|1
condition|)
block|{
comment|// 0 or -1 implies fetch everything
name|this
operator|.
name|maxEvents
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|maxEvents
operator|=
name|maxEvents
expr_stmt|;
block|}
name|this
operator|.
name|eventCount
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|maxPos
operator|=
name|nfetcher
operator|.
name|getCurrentNotificationEventId
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|fetchNextBatch
parameter_list|()
throws|throws
name|IOException
block|{
name|batch
operator|=
name|nfetcher
operator|.
name|getNextNotificationEvents
argument_list|(
name|pos
argument_list|,
name|filter
argument_list|)
expr_stmt|;
name|int
name|batchSize
init|=
name|nfetcher
operator|.
name|getBatchSize
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
operator|(
name|batch
operator|==
literal|null
operator|)
operator|||
operator|(
name|batch
operator|.
name|isEmpty
argument_list|()
operator|)
operator|)
operator|&&
operator|(
name|pos
operator|<
name|maxPos
operator|)
condition|)
block|{
comment|// no valid events this batch, but we're still not done processing events
name|pos
operator|+=
name|batchSize
expr_stmt|;
name|batch
operator|=
name|nfetcher
operator|.
name|getNextNotificationEvents
argument_list|(
name|pos
argument_list|,
name|filter
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|batch
operator|==
literal|null
condition|)
block|{
name|batch
operator|=
operator|new
name|ArrayList
argument_list|<
name|NotificationEvent
argument_list|>
argument_list|()
expr_stmt|;
comment|// instantiate empty list so that we don't error out on iterator fetching.
comment|// If we're here, then the next check of pos will show our caller that
comment|// that we've exhausted our event supply
block|}
name|batchIter
operator|=
name|batch
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
if|if
condition|(
name|eventCount
operator|>=
name|maxEvents
condition|)
block|{
comment|// If we've already satisfied the number of events we were supposed to deliver, we end it.
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|(
name|batchIter
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|batchIter
operator|.
name|hasNext
argument_list|()
operator|)
condition|)
block|{
comment|// If we have a valid batchIter and it has more elements, return them.
return|return
literal|true
return|;
block|}
comment|// If we're here, we want more events, and either batchIter is null, or batchIter
comment|// has reached the end of the current batch. Let's fetch the next batch.
try|try
block|{
name|fetchNextBatch
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Regrettable that we have to wrap the IOException into a RuntimeException,
comment|// but throwing the exception is the appropriate result here, and hasNext()
comment|// signature will only allow RuntimeExceptions. Iterator.hasNext() really
comment|// should have allowed IOExceptions
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// New batch has been fetched. If it's not empty, we have more elements to process.
return|return
operator|!
name|batch
operator|.
name|isEmpty
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|NotificationEvent
name|next
parameter_list|()
block|{
name|eventCount
operator|++
expr_stmt|;
name|NotificationEvent
name|ev
init|=
name|batchIter
operator|.
name|next
argument_list|()
decl_stmt|;
name|pos
operator|=
name|ev
operator|.
name|getEventId
argument_list|()
expr_stmt|;
return|return
name|ev
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
literal|"remove() not supported on NotificationEventIterator"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

