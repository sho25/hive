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
name|util
operator|.
name|concurrent
operator|.
name|ArrayBlockingQueue
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
name|BlockingQueue
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
name|metadata
operator|.
name|HiveException
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
import|;
end_import

begin_comment
comment|/**  * This class is for managing multiple tez sessions particularly when  * HiveServer2 is being used to submit queries.  *  * In case the user specifies a queue explicitly, a new session is created  * on that queue and assigned to the session state.  */
end_comment

begin_class
specifier|public
class|class
name|TezSessionPoolManager
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TezSessionPoolManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|BlockingQueue
argument_list|<
name|TezSessionState
argument_list|>
name|defaultQueuePool
decl_stmt|;
specifier|private
name|int
name|blockingQueueLength
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|HiveConf
name|initConf
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|inited
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|TezSessionPoolManager
name|sessionPool
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
name|TezSessionPoolManager
name|getInstance
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|sessionPool
operator|==
literal|null
condition|)
block|{
name|sessionPool
operator|=
operator|new
name|TezSessionPoolManager
argument_list|()
expr_stmt|;
block|}
return|return
name|sessionPool
return|;
block|}
specifier|protected
name|TezSessionPoolManager
parameter_list|()
block|{   }
specifier|public
name|void
name|startPool
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|inited
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|blockingQueueLength
condition|;
name|i
operator|++
control|)
block|{
name|HiveConf
name|newConf
init|=
operator|new
name|HiveConf
argument_list|(
name|initConf
argument_list|)
decl_stmt|;
name|TezSessionState
name|sessionState
init|=
name|defaultQueuePool
operator|.
name|take
argument_list|()
decl_stmt|;
name|newConf
operator|.
name|set
argument_list|(
literal|"tez.queue.name"
argument_list|,
name|sessionState
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
name|sessionState
operator|.
name|open
argument_list|(
name|TezSessionState
operator|.
name|makeSessionId
argument_list|()
argument_list|,
name|newConf
argument_list|)
expr_stmt|;
name|defaultQueuePool
operator|.
name|put
argument_list|(
name|sessionState
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|setupPool
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|String
name|defaultQueues
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_DEFAULT_QUEUES
argument_list|)
decl_stmt|;
name|int
name|numSessions
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_SESSIONS_PER_DEFAULT_QUEUE
argument_list|)
decl_stmt|;
comment|// the list of queues is a comma separated list.
name|String
name|defaultQueueList
index|[]
init|=
name|defaultQueues
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|defaultQueuePool
operator|=
operator|new
name|ArrayBlockingQueue
argument_list|<
name|TezSessionState
argument_list|>
argument_list|(
name|numSessions
operator|*
name|defaultQueueList
operator|.
name|length
argument_list|)
expr_stmt|;
name|this
operator|.
name|initConf
operator|=
name|conf
expr_stmt|;
comment|/*      *  with this the ordering of sessions in the queue will be (with 2 sessions 3 queues)      *  s1q1, s1q2, s1q3, s2q1, s2q2, s2q3 there by ensuring uniform distribution of      *  the sessions across queues at least to begin with. Then as sessions get freed up, the list      *  may change this ordering.      */
name|blockingQueueLength
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numSessions
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|String
name|queue
range|:
name|defaultQueueList
control|)
block|{
if|if
condition|(
name|queue
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
name|TezSessionState
name|sessionState
init|=
name|createSession
argument_list|()
decl_stmt|;
name|sessionState
operator|.
name|setQueueName
argument_list|(
name|queue
argument_list|)
expr_stmt|;
name|sessionState
operator|.
name|setDefault
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Created new tez session for queue: "
operator|+
name|queue
operator|+
literal|" with session id: "
operator|+
name|sessionState
operator|.
name|getSessionId
argument_list|()
argument_list|)
expr_stmt|;
name|defaultQueuePool
operator|.
name|put
argument_list|(
name|sessionState
argument_list|)
expr_stmt|;
name|blockingQueueLength
operator|++
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|TezSessionState
name|getSession
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|queueName
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"tez.queue.name"
argument_list|)
decl_stmt|;
name|boolean
name|nonDefaultUser
init|=
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|)
decl_stmt|;
comment|/*      * if the user has specified a queue name themselves, we create a new session.      * also a new session is created if the user tries to submit to a queue using      * their own credentials. We expect that with the new security model, things will      * run as user hive in most cases.      */
if|if
condition|(
operator|!
operator|(
name|this
operator|.
name|inited
operator|)
operator|||
operator|(
operator|(
name|queueName
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|queueName
operator|.
name|isEmpty
argument_list|()
operator|)
operator|)
operator|||
operator|(
name|nonDefaultUser
operator|)
operator|||
operator|(
name|defaultQueuePool
operator|==
literal|null
operator|)
operator|||
operator|(
name|blockingQueueLength
operator|<=
literal|0
operator|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"QueueName: "
operator|+
name|queueName
operator|+
literal|" nonDefaultUser: "
operator|+
name|nonDefaultUser
operator|+
literal|" defaultQueuePool: "
operator|+
name|defaultQueuePool
operator|+
literal|" blockingQueueLength: "
operator|+
name|blockingQueueLength
argument_list|)
expr_stmt|;
return|return
name|getNewSessionState
argument_list|(
name|conf
argument_list|,
name|queueName
argument_list|)
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Choosing a session from the defaultQueuePool"
argument_list|)
expr_stmt|;
return|return
name|defaultQueuePool
operator|.
name|take
argument_list|()
return|;
block|}
comment|/**    * @param conf HiveConf that is used to initialize the session    * @param queueName could be null. Set in the tez session.    * @return    * @throws Exception    */
specifier|private
name|TezSessionState
name|getNewSessionState
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|queueName
parameter_list|)
throws|throws
name|Exception
block|{
name|TezSessionState
name|retTezSessionState
init|=
name|createSession
argument_list|()
decl_stmt|;
name|retTezSessionState
operator|.
name|setQueueName
argument_list|(
name|queueName
argument_list|)
expr_stmt|;
name|retTezSessionState
operator|.
name|open
argument_list|(
name|TezSessionState
operator|.
name|makeSessionId
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started a new session for queue: "
operator|+
name|queueName
operator|+
literal|" session id: "
operator|+
name|retTezSessionState
operator|.
name|getSessionId
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|retTezSessionState
return|;
block|}
specifier|public
name|void
name|returnSession
parameter_list|(
name|TezSessionState
name|tezSessionState
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|tezSessionState
operator|.
name|isDefault
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"The session "
operator|+
name|tezSessionState
operator|.
name|getSessionId
argument_list|()
operator|+
literal|" belongs to the pool. Put it back in"
argument_list|)
expr_stmt|;
name|SessionState
name|sessionState
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|sessionState
operator|!=
literal|null
condition|)
block|{
name|sessionState
operator|.
name|setTezSession
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|defaultQueuePool
operator|.
name|put
argument_list|(
name|tezSessionState
argument_list|)
expr_stmt|;
block|}
comment|// non default session nothing changes. The user can continue to use the existing
comment|// session in the SessionState
block|}
specifier|public
name|void
name|close
parameter_list|(
name|TezSessionState
name|tezSessionState
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Closing tez session default? "
operator|+
name|tezSessionState
operator|.
name|isDefault
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|tezSessionState
operator|.
name|isDefault
argument_list|()
condition|)
block|{
name|tezSessionState
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|stop
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|(
name|sessionPool
operator|==
literal|null
operator|)
operator|||
operator|(
name|this
operator|.
name|inited
operator|==
literal|false
operator|)
condition|)
block|{
return|return;
block|}
comment|// we can just stop all the sessions
for|for
control|(
name|TezSessionState
name|sessionState
range|:
name|TezSessionState
operator|.
name|getOpenSessions
argument_list|()
control|)
block|{
if|if
condition|(
name|sessionState
operator|.
name|isDefault
argument_list|()
condition|)
block|{
name|sessionState
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|protected
name|TezSessionState
name|createSession
parameter_list|()
block|{
return|return
operator|new
name|TezSessionState
argument_list|()
return|;
block|}
specifier|public
name|TezSessionState
name|getSession
parameter_list|(
name|TezSessionState
name|session
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|canWorkWithSameSession
argument_list|(
name|session
argument_list|,
name|conf
argument_list|)
condition|)
block|{
return|return
name|session
return|;
block|}
if|if
condition|(
name|session
operator|!=
literal|null
condition|)
block|{
name|session
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
return|return
name|getSession
argument_list|(
name|conf
argument_list|)
return|;
block|}
comment|/*    * This method helps to re-use a session in case there has been no change in    * the configuration of a session. This will happen only in the case of non-hive-server2    * sessions for e.g. when a CLI session is started. The CLI session could re-use the    * same tez session eliminating the latencies of new AM and containers.    */
specifier|private
name|boolean
name|canWorkWithSameSession
parameter_list|(
name|TezSessionState
name|session
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|session
operator|==
literal|null
operator|||
name|conf
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|HiveConf
name|existingConf
init|=
name|session
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|existingConf
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// either variables will never be null because a default value is returned in case of absence
if|if
condition|(
name|existingConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|)
operator|!=
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|session
operator|.
name|isDefault
argument_list|()
condition|)
block|{
if|if
condition|(
name|existingConf
operator|.
name|get
argument_list|(
literal|"tez.queue.name"
argument_list|)
operator|==
name|conf
operator|.
name|get
argument_list|(
literal|"tez.queue.name"
argument_list|)
condition|)
block|{
comment|// both are null
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|(
name|existingConf
operator|.
name|get
argument_list|(
literal|"tez.queue.name"
argument_list|)
operator|==
literal|null
operator|)
condition|)
block|{
comment|// doesn't matter if the other conf is null or not. if it is null, above case catches it
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|existingConf
operator|.
name|get
argument_list|(
literal|"tez.queue.name"
argument_list|)
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"tez.queue.name"
argument_list|)
argument_list|)
condition|)
block|{
comment|// handles the case of incoming conf having a null for tez.queue.name
return|return
literal|false
return|;
block|}
block|}
else|else
block|{
comment|// this session should never be a default session unless something has messed up.
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Default queue should always be returned."
operator|+
literal|"Hence we should not be here."
argument_list|)
throw|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

