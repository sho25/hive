begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|listener
package|;
end_package

begin_comment
comment|/**  * Keeps a list of reserved keys used by Hive listeners when updating the ListenerEvent  * parameters.  */
end_comment

begin_class
specifier|public
class|class
name|MetaStoreEventListenerConstants
block|{
comment|/*    * DbNotificationListener keys reserved for updating ListenerEvent parameters.    *    * DB_NOTIFICATION_EVENT_ID_KEY_NAME This key will have the event identifier that DbNotificationListener    *                                   processed during an event. This event identifier might be shared    *                                   across other MetaStoreEventListener implementations.    */
specifier|public
specifier|static
specifier|final
name|String
name|DB_NOTIFICATION_EVENT_ID_KEY_NAME
init|=
literal|"DB_NOTIFICATION_EVENT_ID_KEY_NAME"
decl_stmt|;
block|}
end_class

end_unit

