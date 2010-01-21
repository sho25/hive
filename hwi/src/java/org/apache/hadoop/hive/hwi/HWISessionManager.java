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
name|hadoop
operator|.
name|hive
operator|.
name|hwi
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Vector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_comment
comment|/**  * HiveSessionManager is a Runnable started inside a web application context.  * It's basic function is to hold a collection of SessionItem(s). It also works  * as a facade, as jsp clients can not create a Hive Session directly. Hive  * Sessions are long lived, unlike a traditional Query and Block system clients  * set up the query to be started with an instance of this class.  *   */
end_comment

begin_class
specifier|public
class|class
name|HWISessionManager
implements|implements
name|Runnable
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|l4j
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HWISessionManager
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|goOn
decl_stmt|;
specifier|private
name|TreeMap
argument_list|<
name|HWIAuth
argument_list|,
name|Set
argument_list|<
name|HWISessionItem
argument_list|>
argument_list|>
name|items
decl_stmt|;
specifier|protected
name|HWISessionManager
parameter_list|()
block|{
name|goOn
operator|=
literal|true
expr_stmt|;
name|items
operator|=
operator|new
name|TreeMap
argument_list|<
name|HWIAuth
argument_list|,
name|Set
argument_list|<
name|HWISessionItem
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|/**    * This method scans the SessionItem collection. If a SessionItem is in the    * QUERY_SET state that signals that its thread should be started. If the    * SessionItem is in the DESTROY state it should be cleaned up and removed    * from the collection. Currently we are using a sleep. A wait/notify could be    * implemented. Queries will run for a long time, a one second wait on start    * will not be noticed.    *     */
specifier|public
name|void
name|run
parameter_list|()
block|{
name|l4j
operator|.
name|debug
argument_list|(
literal|"Entered run() thread has started"
argument_list|)
expr_stmt|;
while|while
condition|(
name|goOn
condition|)
block|{
name|l4j
operator|.
name|debug
argument_list|(
literal|"locking items"
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|items
init|)
block|{
for|for
control|(
name|HWIAuth
name|a
range|:
name|items
operator|.
name|keySet
argument_list|()
control|)
block|{
for|for
control|(
name|HWISessionItem
name|i
range|:
name|items
operator|.
name|get
argument_list|(
name|a
argument_list|)
control|)
block|{
if|if
condition|(
name|i
operator|.
name|getStatus
argument_list|()
operator|==
name|HWISessionItem
operator|.
name|WebSessionItemStatus
operator|.
name|DESTROY
condition|)
block|{
name|items
operator|.
name|get
argument_list|(
name|a
argument_list|)
operator|.
name|remove
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|.
name|getStatus
argument_list|()
operator|==
name|HWISessionItem
operator|.
name|WebSessionItemStatus
operator|.
name|KILL_QUERY
condition|)
block|{
name|l4j
operator|.
name|debug
argument_list|(
literal|"Killing item: "
operator|+
name|i
operator|.
name|getSessionName
argument_list|()
argument_list|)
expr_stmt|;
name|i
operator|.
name|killIt
argument_list|()
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
literal|"Killed item: "
operator|+
name|i
operator|.
name|getSessionName
argument_list|()
argument_list|)
expr_stmt|;
name|items
operator|.
name|get
argument_list|(
name|a
argument_list|)
operator|.
name|remove
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// end sync
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|l4j
operator|.
name|error
argument_list|(
literal|"Could not sleep "
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
comment|// end while
name|l4j
operator|.
name|debug
argument_list|(
literal|"goOn is false. Loop has ended."
argument_list|)
expr_stmt|;
comment|// Cleanup used here to stop all threads
synchronized|synchronized
init|(
name|items
init|)
block|{
for|for
control|(
name|HWIAuth
name|a
range|:
name|items
operator|.
name|keySet
argument_list|()
control|)
block|{
for|for
control|(
name|HWISessionItem
name|i
range|:
name|items
operator|.
name|get
argument_list|(
name|a
argument_list|)
control|)
block|{
try|try
block|{
if|if
condition|(
name|i
operator|.
name|getStatus
argument_list|()
operator|==
name|HWISessionItem
operator|.
name|WebSessionItemStatus
operator|.
name|QUERY_RUNNING
condition|)
block|{
name|l4j
operator|.
name|debug
argument_list|(
name|i
operator|.
name|getSessionName
argument_list|()
operator|+
literal|"Joining "
argument_list|)
expr_stmt|;
name|i
operator|.
name|runnable
operator|.
name|join
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
name|i
operator|.
name|getSessionName
argument_list|()
operator|+
literal|"Joined "
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|l4j
operator|.
name|error
argument_list|(
name|i
operator|.
name|getSessionName
argument_list|()
operator|+
literal|"while joining "
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|// end run
specifier|protected
name|boolean
name|isGoOn
parameter_list|()
block|{
return|return
name|goOn
return|;
block|}
specifier|protected
name|void
name|setGoOn
parameter_list|(
name|boolean
name|goOn
parameter_list|)
block|{
name|this
operator|.
name|goOn
operator|=
name|goOn
expr_stmt|;
block|}
specifier|protected
name|TreeMap
argument_list|<
name|HWIAuth
argument_list|,
name|Set
argument_list|<
name|HWISessionItem
argument_list|>
argument_list|>
name|getItems
parameter_list|()
block|{
return|return
name|items
return|;
block|}
specifier|protected
name|void
name|setItems
parameter_list|(
name|TreeMap
argument_list|<
name|HWIAuth
argument_list|,
name|Set
argument_list|<
name|HWISessionItem
argument_list|>
argument_list|>
name|items
parameter_list|)
block|{
name|this
operator|.
name|items
operator|=
name|items
expr_stmt|;
block|}
comment|// client methods called from JSP
comment|/**    * Rather then return the actual items we return a list copies. This enforces    * our HWISessionManager by preventing the ability of the client(jsp) to    * create SessionItems.    *     * @return A set of SessionItems this framework manages    */
specifier|public
name|Vector
argument_list|<
name|HWISessionItem
argument_list|>
name|findAllSessionItems
parameter_list|()
block|{
name|Vector
argument_list|<
name|HWISessionItem
argument_list|>
name|otherItems
init|=
operator|new
name|Vector
argument_list|<
name|HWISessionItem
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HWIAuth
name|a
range|:
name|items
operator|.
name|keySet
argument_list|()
control|)
block|{
name|otherItems
operator|.
name|addAll
argument_list|(
name|items
operator|.
name|get
argument_list|(
name|a
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|otherItems
return|;
block|}
comment|/**    * Here we handle creating the SessionItem, we do this for the JSP client    * because we need to set parameters the client is not aware of. One such    * parameter is the command line arguments the server was started with.    *     * @param a    *          Authenticated user    * @param sessionName    *          Represents the session name    * @return a new SessionItem or null if a session with that name already    *         exists    */
specifier|public
name|HWISessionItem
name|createSession
parameter_list|(
name|HWIAuth
name|a
parameter_list|,
name|String
name|sessionName
parameter_list|)
block|{
name|l4j
operator|.
name|debug
argument_list|(
literal|"Creating session: "
operator|+
name|sessionName
argument_list|)
expr_stmt|;
name|HWISessionItem
name|si
init|=
literal|null
decl_stmt|;
synchronized|synchronized
init|(
name|items
init|)
block|{
if|if
condition|(
name|findSessionItemByName
argument_list|(
name|a
argument_list|,
name|sessionName
argument_list|)
operator|==
literal|null
condition|)
block|{
name|l4j
operator|.
name|debug
argument_list|(
literal|"Initializing session: "
operator|+
name|sessionName
operator|+
literal|" a for "
operator|+
name|a
operator|.
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
name|si
operator|=
operator|new
name|HWISessionItem
argument_list|(
name|a
argument_list|,
name|sessionName
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|items
operator|.
name|containsKey
argument_list|(
name|a
argument_list|)
condition|)
block|{
name|l4j
operator|.
name|debug
argument_list|(
literal|"SessionList is empty "
operator|+
name|a
operator|.
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
name|TreeSet
argument_list|<
name|HWISessionItem
argument_list|>
name|list
init|=
operator|new
name|TreeSet
argument_list|<
name|HWISessionItem
argument_list|>
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|si
argument_list|)
expr_stmt|;
name|items
operator|.
name|put
argument_list|(
name|a
argument_list|,
name|list
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
literal|"Item added "
operator|+
name|si
operator|.
name|getSessionName
argument_list|()
operator|+
literal|" for user "
operator|+
name|a
operator|.
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|items
operator|.
name|get
argument_list|(
name|a
argument_list|)
operator|.
name|add
argument_list|(
name|si
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
literal|"Item added "
operator|+
name|si
operator|.
name|getSessionName
argument_list|()
operator|+
literal|" for user "
operator|+
name|a
operator|.
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|l4j
operator|.
name|debug
argument_list|(
literal|"Creating session: "
operator|+
name|sessionName
operator|+
literal|" already exists "
operator|+
name|a
operator|.
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|si
return|;
block|}
comment|/**    * Helper method useful when you know the session name you wish to reference.    *     * @param sessionname    * @return A SessionItem matching the sessionname or null if it does not    *         exists    */
specifier|public
name|HWISessionItem
name|findSessionItemByName
parameter_list|(
name|HWIAuth
name|auth
parameter_list|,
name|String
name|sessionname
parameter_list|)
block|{
name|Collection
argument_list|<
name|HWISessionItem
argument_list|>
name|sessForUser
init|=
name|items
operator|.
name|get
argument_list|(
name|auth
argument_list|)
decl_stmt|;
if|if
condition|(
name|sessForUser
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|HWISessionItem
name|si
range|:
name|sessForUser
control|)
block|{
if|if
condition|(
name|si
operator|.
name|getSessionName
argument_list|()
operator|.
name|equals
argument_list|(
name|sessionname
argument_list|)
condition|)
block|{
return|return
name|si
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Used to list all users that have at least one session    *     * @return keySet of items all users that have any sessions    */
specifier|public
name|Set
argument_list|<
name|HWIAuth
argument_list|>
name|findAllUsersWithSessions
parameter_list|()
block|{
return|return
name|items
operator|.
name|keySet
argument_list|()
return|;
block|}
comment|/**    * Used to list all the sessions of a user    *     * @param auth    *          the user being enquired about    * @return all the sessions of that user    */
specifier|public
name|Set
argument_list|<
name|HWISessionItem
argument_list|>
name|findAllSessionsForUser
parameter_list|(
name|HWIAuth
name|auth
parameter_list|)
block|{
return|return
name|items
operator|.
name|get
argument_list|(
name|auth
argument_list|)
return|;
block|}
block|}
end_class

end_unit

