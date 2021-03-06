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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|primitives
operator|.
name|Ints
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
name|CurrentNotificationEventId
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
name|MetaException
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
name|ql
operator|.
name|metadata
operator|.
name|Hive
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
name|ql
operator|.
name|metadata
operator|.
name|events
operator|.
name|EventUtils
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
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|Description
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|model
operator|.
name|Statement
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
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|LinkedHashMap
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_comment
comment|/**  * Utility class to enable testing of Replv1 compatibility testing.  *  * If event formats/etc change in the future, testing against this allows tests  * to determine if they break backward compatibility with Replv1.  *  * Use as a junit TestRule on tests that generate events to test if the events  * generated are compatible with replv1.  */
end_comment

begin_class
specifier|public
class|class
name|ReplicationV1CompatRule
implements|implements
name|TestRule
block|{
specifier|public
annotation_defn|@interface
name|SkipReplV1CompatCheck
block|{    }
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ReplicationV1CompatRule
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|Long
argument_list|>
name|testEventId
init|=
literal|null
decl_stmt|;
specifier|private
name|IMetaStoreClient
name|metaStoreClient
init|=
literal|null
decl_stmt|;
specifier|private
name|HiveConf
name|hconf
init|=
literal|null
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|testsToSkip
init|=
literal|null
decl_stmt|;
specifier|private
name|Hive
name|hiveDb
decl_stmt|;
specifier|public
name|ReplicationV1CompatRule
parameter_list|(
name|IMetaStoreClient
name|metaStoreClient
parameter_list|,
name|HiveConf
name|hconf
parameter_list|)
block|{
name|this
argument_list|(
name|metaStoreClient
argument_list|,
name|hconf
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReplicationV1CompatRule
parameter_list|(
name|IMetaStoreClient
name|metaStoreClient
parameter_list|,
name|HiveConf
name|hconf
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|testsToSkip
parameter_list|)
block|{
name|this
operator|.
name|metaStoreClient
operator|=
name|metaStoreClient
expr_stmt|;
name|this
operator|.
name|hconf
operator|=
name|hconf
expr_stmt|;
name|testEventId
operator|=
operator|new
name|ThreadLocal
argument_list|<
name|Long
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Long
name|initialValue
parameter_list|()
block|{
return|return
name|getCurrentNotificationId
argument_list|()
return|;
block|}
block|}
expr_stmt|;
name|this
operator|.
name|testsToSkip
operator|=
name|testsToSkip
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Replv1 backward compatibility tester initialized at "
operator|+
name|testEventId
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|hiveDb
operator|=
name|mock
argument_list|(
name|Hive
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Long
name|getCurrentNotificationId
parameter_list|()
block|{
name|CurrentNotificationEventId
name|cid
init|=
literal|null
decl_stmt|;
try|try
block|{
name|cid
operator|=
name|metaStoreClient
operator|.
name|getCurrentNotificationEventId
argument_list|()
expr_stmt|;
name|Long
name|l
init|=
name|cid
operator|.
name|getEventId
argument_list|()
decl_stmt|;
return|return
operator|(
name|l
operator|==
literal|null
operator|)
condition|?
literal|0L
else|:
name|l
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Helper method to verify that all events generated since last call are compatible with    * replv1. If this is called multiple times, it does this check for all events incurred    * since the last time it was called.    *    * @param eventsMustExist : Determines whether or not non-presence of events should be    *   considered an error. You probably don't need this except during test development    *   for validation. If you're running this for a whole set of tests in one go, not    *   having any events is probably an error condition.    */
specifier|public
name|void
name|doBackwardCompatibilityCheck
parameter_list|(
name|boolean
name|eventsMustExist
parameter_list|)
block|{
name|Long
name|testEventIdPrev
init|=
name|testEventId
operator|.
name|get
argument_list|()
decl_stmt|;
name|Long
name|testEventIdNow
init|=
name|getCurrentNotificationId
argument_list|()
decl_stmt|;
name|testEventId
operator|.
name|set
argument_list|(
name|testEventIdNow
argument_list|)
expr_stmt|;
if|if
condition|(
name|eventsMustExist
condition|)
block|{
name|assertTrue
argument_list|(
literal|"New events must exist between old["
operator|+
name|testEventIdPrev
operator|+
literal|"] and ["
operator|+
name|testEventIdNow
operator|+
literal|"]"
argument_list|,
name|testEventIdNow
operator|>
name|testEventIdPrev
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|testEventIdNow
operator|<=
name|testEventIdPrev
condition|)
block|{
return|return;
comment|// nothing further to test.
block|}
name|doBackwardCompatibilityCheck
argument_list|(
name|testEventIdPrev
argument_list|,
name|testEventIdNow
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|doBackwardCompatibilityCheck
parameter_list|(
name|long
name|testEventIdBefore
parameter_list|,
name|long
name|testEventIdAfter
parameter_list|)
block|{
comment|// try to instantiate the old replv1 task generation on every event produced.
name|long
name|timeBefore
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|NotificationEvent
argument_list|,
name|RuntimeException
argument_list|>
name|unhandledTasks
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|NotificationEvent
argument_list|,
name|RuntimeException
argument_list|>
name|incompatibleTasks
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|eventCount
init|=
literal|0
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Checking replv1 backward compatibility for events between : "
operator|+
name|testEventIdBefore
operator|+
literal|" -> "
operator|+
name|testEventIdAfter
argument_list|)
expr_stmt|;
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|evFilter
init|=
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
name|notificationEvent
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
decl_stmt|;
try|try
block|{
name|when
argument_list|(
name|hiveDb
operator|.
name|getMSC
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|metaStoreClient
argument_list|)
expr_stmt|;
name|EventUtils
operator|.
name|MSClientNotificationFetcher
name|evFetcher
init|=
operator|new
name|EventUtils
operator|.
name|MSClientNotificationFetcher
argument_list|(
name|hiveDb
argument_list|)
decl_stmt|;
name|EventUtils
operator|.
name|NotificationEventIterator
name|evIter
init|=
operator|new
name|EventUtils
operator|.
name|NotificationEventIterator
argument_list|(
name|evFetcher
argument_list|,
name|testEventIdBefore
argument_list|,
name|Ints
operator|.
name|checkedCast
argument_list|(
name|testEventIdAfter
operator|-
name|testEventIdBefore
argument_list|)
operator|+
literal|1
argument_list|,
name|evFilter
argument_list|)
decl_stmt|;
name|ReplicationTask
operator|.
name|resetFactory
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"We should have found some events"
argument_list|,
name|evIter
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
name|evIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|eventCount
operator|++
expr_stmt|;
name|NotificationEvent
name|ev
init|=
name|evIter
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// convert to HCatNotificationEvent, and then try to instantiate a ReplicationTask on it.
try|try
block|{
name|ReplicationTask
name|rtask
init|=
name|ReplicationTask
operator|.
name|create
argument_list|(
name|HCatClient
operator|.
name|create
argument_list|(
name|hconf
argument_list|)
argument_list|,
operator|new
name|HCatNotificationEvent
argument_list|(
name|ev
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|rtask
operator|instanceof
name|ErroredReplicationTask
condition|)
block|{
name|unhandledTasks
operator|.
name|put
argument_list|(
name|ev
argument_list|,
operator|(
operator|(
name|ErroredReplicationTask
operator|)
name|rtask
operator|)
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|re
parameter_list|)
block|{
name|incompatibleTasks
operator|.
name|put
argument_list|(
name|ev
argument_list|,
name|re
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|MetaException
name|e
parameter_list|)
block|{
name|assertNull
argument_list|(
literal|"Got an exception when we shouldn't have - replv1 backward incompatibility issue:"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|unhandledTasks
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Events found that would not be coverable by replv1 replication: "
operator|+
name|unhandledTasks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|NotificationEvent
name|ev
range|:
name|unhandledTasks
operator|.
name|keySet
argument_list|()
control|)
block|{
name|RuntimeException
name|re
init|=
name|unhandledTasks
operator|.
name|get
argument_list|(
name|ev
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"ErroredReplicationTask encountered - new event type does not correspond to a replv1 task:"
operator|+
name|ev
operator|.
name|toString
argument_list|()
argument_list|,
name|re
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|incompatibleTasks
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Events found that caused errors in replv1 replication: "
operator|+
name|incompatibleTasks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|NotificationEvent
name|ev
range|:
name|incompatibleTasks
operator|.
name|keySet
argument_list|()
control|)
block|{
name|RuntimeException
name|re
init|=
name|incompatibleTasks
operator|.
name|get
argument_list|(
name|ev
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"RuntimeException encountered - new event type caused a replv1 break."
operator|+
name|ev
operator|.
name|toString
argument_list|()
argument_list|,
name|re
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|incompatibleTasks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|timeAfter
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Backward compatibility check timing:"
operator|+
name|timeBefore
operator|+
literal|" -> "
operator|+
name|timeAfter
operator|+
literal|", ev: "
operator|+
name|testEventIdBefore
operator|+
literal|" => "
operator|+
name|testEventIdAfter
operator|+
literal|", #events processed="
operator|+
name|eventCount
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Statement
name|apply
parameter_list|(
name|Statement
name|statement
parameter_list|,
name|Description
name|description
parameter_list|)
block|{
return|return
operator|new
name|Statement
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|evaluate
parameter_list|()
throws|throws
name|Throwable
block|{
name|Long
name|prevNotificationId
init|=
name|getCurrentNotificationId
argument_list|()
decl_stmt|;
name|statement
operator|.
name|evaluate
argument_list|()
expr_stmt|;
name|Long
name|currNotificationId
init|=
name|getCurrentNotificationId
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|testsToSkip
operator|.
name|contains
argument_list|(
name|description
operator|.
name|getMethodName
argument_list|()
argument_list|)
condition|)
block|{
name|doBackwardCompatibilityCheck
argument_list|(
name|prevNotificationId
argument_list|,
name|currNotificationId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipping backward compatibility check, as requested, for test :"
operator|+
name|description
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

