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
name|ql
operator|.
name|exec
operator|.
name|tez
package|;
end_package

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
name|net
operator|.
name|URISyntaxException
import|;
end_import

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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
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
name|fs
operator|.
name|Path
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
name|ql
operator|.
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|TezException
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
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * TezSession that is aware of the session pool, and also keeps track of expiration and use.  * It has 3 states - not in use, in use, and expired. When in the pool, it is not in use;  * use and expiration may compete to take the session out of the pool and change it to the  * corresponding states. When someone tries to get a session, they check for expiration time;  * if it's time, the expiration is triggered; in that case, or if it was already triggered, the  * caller gets a different session. When the session is in use when it expires, the expiration  * thread ignores it and lets the return to the pool take care of the expiration.  */
end_comment

begin_class
annotation|@
name|VisibleForTesting
class|class
name|TezSessionPoolSession
extends|extends
name|TezSessionState
block|{
specifier|private
specifier|static
specifier|final
name|int
name|STATE_NONE
init|=
literal|0
decl_stmt|,
name|STATE_IN_USE
init|=
literal|1
decl_stmt|,
name|STATE_EXPIRED
init|=
literal|2
decl_stmt|;
interface|interface
name|OpenSessionTracker
block|{
name|void
name|registerOpenSession
parameter_list|(
name|TezSessionPoolSession
name|session
parameter_list|)
function_decl|;
name|void
name|unregisterOpenSession
parameter_list|(
name|TezSessionPoolSession
name|session
parameter_list|)
function_decl|;
block|}
specifier|private
specifier|final
name|AtomicInteger
name|sessionState
init|=
operator|new
name|AtomicInteger
argument_list|(
name|STATE_NONE
argument_list|)
decl_stmt|;
specifier|private
name|Long
name|expirationNs
decl_stmt|;
specifier|private
specifier|final
name|OpenSessionTracker
name|parent
decl_stmt|;
specifier|private
specifier|final
name|SessionExpirationTracker
name|expirationTracker
decl_stmt|;
specifier|public
name|TezSessionPoolSession
parameter_list|(
name|String
name|sessionId
parameter_list|,
name|OpenSessionTracker
name|parent
parameter_list|,
name|SessionExpirationTracker
name|expirationTracker
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|sessionId
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|expirationTracker
operator|=
name|expirationTracker
expr_stmt|;
block|}
name|void
name|setExpirationNs
parameter_list|(
name|long
name|expirationNs
parameter_list|)
block|{
name|this
operator|.
name|expirationNs
operator|=
name|expirationNs
expr_stmt|;
block|}
name|Long
name|getExpirationNs
parameter_list|()
block|{
return|return
name|expirationNs
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|keepTmpDir
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|super
operator|.
name|close
argument_list|(
name|keepTmpDir
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|parent
operator|.
name|unregisterOpenSession
argument_list|(
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
name|expirationTracker
operator|!=
literal|null
condition|)
block|{
name|expirationTracker
operator|.
name|removeFromExpirationQueue
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|openInternal
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|additionalFiles
parameter_list|,
name|boolean
name|isAsync
parameter_list|,
name|LogHelper
name|console
parameter_list|,
name|Path
name|scratchDir
parameter_list|)
throws|throws
name|IOException
throws|,
name|LoginException
throws|,
name|URISyntaxException
throws|,
name|TezException
block|{
name|super
operator|.
name|openInternal
argument_list|(
name|additionalFiles
argument_list|,
name|isAsync
argument_list|,
name|console
argument_list|,
name|scratchDir
argument_list|)
expr_stmt|;
name|parent
operator|.
name|registerOpenSession
argument_list|(
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
name|expirationTracker
operator|!=
literal|null
condition|)
block|{
name|expirationTracker
operator|.
name|addToExpirationQueue
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|expirationNs
operator|==
literal|null
condition|)
return|return
name|super
operator|.
name|toString
argument_list|()
return|;
name|long
name|expiresInMs
init|=
operator|(
name|expirationNs
operator|-
name|System
operator|.
name|nanoTime
argument_list|()
operator|)
operator|/
literal|1000000L
decl_stmt|;
return|return
name|super
operator|.
name|toString
argument_list|()
operator|+
literal|", expires in "
operator|+
name|expiresInMs
operator|+
literal|"ms"
return|;
block|}
comment|/**    * Tries to use this session. When the session is in use, it will not expire.    * @return true if the session can be used; false if it has already expired.    */
specifier|public
name|boolean
name|tryUse
parameter_list|()
throws|throws
name|Exception
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|oldValue
init|=
name|sessionState
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|oldValue
operator|==
name|STATE_IN_USE
condition|)
throw|throw
operator|new
name|AssertionError
argument_list|(
name|this
operator|+
literal|" is already in use"
argument_list|)
throw|;
if|if
condition|(
name|oldValue
operator|==
name|STATE_EXPIRED
condition|)
return|return
literal|false
return|;
name|int
name|finalState
init|=
name|shouldExpire
argument_list|()
condition|?
name|STATE_EXPIRED
else|:
name|STATE_IN_USE
decl_stmt|;
if|if
condition|(
name|sessionState
operator|.
name|compareAndSet
argument_list|(
name|STATE_NONE
argument_list|,
name|finalState
argument_list|)
condition|)
block|{
if|if
condition|(
name|finalState
operator|==
name|STATE_IN_USE
condition|)
return|return
literal|true
return|;
comment|// Restart asynchronously, don't block the caller.
name|expirationTracker
operator|.
name|closeAndRestartExpiredSession
argument_list|(
name|this
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
comment|/**    * Notifies the session that it's no longer in use. If the session has expired while in use,    * this method will take care of the expiration.    * @return True if the session was returned, false if it was restarted.    */
specifier|public
name|boolean
name|returnAfterUse
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|finalState
init|=
name|shouldExpire
argument_list|()
condition|?
name|STATE_EXPIRED
else|:
name|STATE_NONE
decl_stmt|;
if|if
condition|(
operator|!
name|sessionState
operator|.
name|compareAndSet
argument_list|(
name|STATE_IN_USE
argument_list|,
name|finalState
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unexpected state change; currently "
operator|+
name|sessionState
operator|.
name|get
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|finalState
operator|==
name|STATE_NONE
condition|)
return|return
literal|true
return|;
name|expirationTracker
operator|.
name|closeAndRestartExpiredSession
argument_list|(
name|this
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|/**    * Tries to expire and restart the session.    * @param isAsync Whether the restart should happen asynchronously.    * @return True if the session was, or will be restarted.    */
specifier|public
name|boolean
name|tryExpire
parameter_list|(
name|boolean
name|isAsync
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|expirationNs
operator|==
literal|null
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
name|shouldExpire
argument_list|()
condition|)
return|return
literal|false
return|;
comment|// Try to expire the session if it's not in use; if in use, bail.
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|sessionState
operator|.
name|get
argument_list|()
operator|!=
name|STATE_NONE
condition|)
return|return
literal|true
return|;
comment|// returnAfterUse will take care of this
if|if
condition|(
name|sessionState
operator|.
name|compareAndSet
argument_list|(
name|STATE_NONE
argument_list|,
name|STATE_EXPIRED
argument_list|)
condition|)
block|{
name|expirationTracker
operator|.
name|closeAndRestartExpiredSession
argument_list|(
name|this
argument_list|,
name|isAsync
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
specifier|private
name|boolean
name|shouldExpire
parameter_list|()
block|{
return|return
name|expirationNs
operator|!=
literal|null
operator|&&
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|expirationNs
operator|)
operator|>=
literal|0
return|;
block|}
block|}
end_class

end_unit

