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
name|ql
operator|.
name|exec
operator|.
name|tez
package|;
end_package

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
name|fail
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
name|HashSet
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
name|Random
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
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

begin_class
specifier|public
class|class
name|TestTezSessionPool
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestTezSessionPoolManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|HiveConf
name|conf
decl_stmt|;
name|Random
name|random
decl_stmt|;
specifier|private
name|TezSessionPoolManager
name|poolManager
decl_stmt|;
specifier|private
class|class
name|TestTezSessionPoolManager
extends|extends
name|TezSessionPoolManager
block|{
specifier|public
name|TestTezSessionPoolManager
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setupPool
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|LLAP_TASK_SCHEDULER_AM_REGISTRY_NAME
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|super
operator|.
name|setupPool
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TezSessionPoolSession
name|createSession
parameter_list|(
name|String
name|sessionId
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
return|return
operator|new
name|SampleTezSessionState
argument_list|(
name|sessionId
argument_list|,
name|this
argument_list|,
name|conf
argument_list|)
return|;
block|}
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetNonDefaultSession
parameter_list|()
block|{
name|poolManager
operator|=
operator|new
name|TestTezSessionPoolManager
argument_list|()
expr_stmt|;
try|try
block|{
name|TezSessionState
name|sessionState
init|=
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|TezSessionState
name|sessionState1
init|=
name|poolManager
operator|.
name|getSession
argument_list|(
name|sessionState
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|sessionState1
operator|!=
name|sessionState
condition|)
block|{
name|fail
argument_list|()
expr_stmt|;
block|}
name|conf
operator|.
name|set
argument_list|(
literal|"tez.queue.name"
argument_list|,
literal|"nondefault"
argument_list|)
expr_stmt|;
name|TezSessionState
name|sessionState2
init|=
name|poolManager
operator|.
name|getSession
argument_list|(
name|sessionState
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|sessionState2
operator|==
name|sessionState
condition|)
block|{
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSessionPoolGetInOrder
parameter_list|()
block|{
try|try
block|{
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_DEFAULT_QUEUES
argument_list|,
literal|"a,b,c"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_SESSIONS_PER_DEFAULT_QUEUE
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_SESSION_MAX_INIT_THREADS
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|poolManager
operator|=
operator|new
name|TestTezSessionPoolManager
argument_list|()
expr_stmt|;
name|poolManager
operator|.
name|setupPool
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|startPool
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// this is now a LIFO operation
comment|// draw 1 and replace
name|TezSessionState
name|sessionState
init|=
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"a"
argument_list|,
name|sessionState
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|returnSession
argument_list|(
name|sessionState
argument_list|)
expr_stmt|;
name|sessionState
operator|=
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"a"
argument_list|,
name|sessionState
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|returnSession
argument_list|(
name|sessionState
argument_list|)
expr_stmt|;
comment|// [a,b,c,a,b,c]
comment|// draw 2 and return in order - further run should return last returned
name|TezSessionState
name|first
init|=
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|TezSessionState
name|second
init|=
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"a"
argument_list|,
name|first
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"b"
argument_list|,
name|second
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|returnSession
argument_list|(
name|first
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|returnSession
argument_list|(
name|second
argument_list|)
expr_stmt|;
name|TezSessionState
name|third
init|=
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"b"
argument_list|,
name|third
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|returnSession
argument_list|(
name|third
argument_list|)
expr_stmt|;
comment|// [b,a,c,a,b,c]
name|first
operator|=
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|second
operator|=
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|third
operator|=
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"b"
argument_list|,
name|first
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"a"
argument_list|,
name|second
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"c"
argument_list|,
name|third
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|returnSession
argument_list|(
name|first
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|returnSession
argument_list|(
name|second
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|returnSession
argument_list|(
name|third
argument_list|)
expr_stmt|;
comment|// [c,a,b,a,b,c]
name|first
operator|=
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"c"
argument_list|,
name|third
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|returnSession
argument_list|(
name|first
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSessionPoolThreads
parameter_list|()
block|{
comment|// Make sure we get a correct number of sessions in each queue and that we don't crash.
try|try
block|{
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_DEFAULT_QUEUES
argument_list|,
literal|"0,1,2"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_SESSIONS_PER_DEFAULT_QUEUE
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_SESSION_MAX_INIT_THREADS
argument_list|,
literal|16
argument_list|)
expr_stmt|;
name|poolManager
operator|=
operator|new
name|TestTezSessionPoolManager
argument_list|()
expr_stmt|;
name|poolManager
operator|.
name|setupPool
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|startPool
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|TezSessionState
index|[]
name|sessions
init|=
operator|new
name|TezSessionState
index|[
literal|12
index|]
decl_stmt|;
name|int
index|[]
name|queueCounts
init|=
operator|new
name|int
index|[
literal|3
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|sessions
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|sessions
index|[
name|i
index|]
operator|=
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|queueCounts
index|[
name|Integer
operator|.
name|parseInt
argument_list|(
name|sessions
index|[
name|i
index|]
operator|.
name|getQueueName
argument_list|()
argument_list|)
index|]
operator|+=
literal|1
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|queueCounts
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|queueCounts
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|sessions
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|poolManager
operator|.
name|returnSession
argument_list|(
name|sessions
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSessionReopen
parameter_list|()
block|{
try|try
block|{
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_DEFAULT_QUEUES
argument_list|,
literal|"default,tezq1"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_SESSIONS_PER_DEFAULT_QUEUE
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|poolManager
operator|=
operator|new
name|TestTezSessionPoolManager
argument_list|()
expr_stmt|;
name|TezSessionState
name|session
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|TezSessionState
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|session
operator|.
name|getQueueName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"default"
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|session
operator|.
name|isDefault
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|session
operator|.
name|getConf
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|reopen
argument_list|(
name|session
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|session
argument_list|)
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|session
argument_list|)
operator|.
name|open
argument_list|(
name|Mockito
operator|.
expr|<
name|TezSessionState
operator|.
name|HiveResources
operator|>
name|any
argument_list|()
argument_list|)
expr_stmt|;
comment|// mocked session starts with default queue
name|assertEquals
argument_list|(
literal|"default"
argument_list|,
name|session
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
comment|// user explicitly specified queue name
name|conf
operator|.
name|set
argument_list|(
literal|"tez.queue.name"
argument_list|,
literal|"tezq1"
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|reopen
argument_list|(
name|session
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"tezq1"
argument_list|,
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
comment|// user unsets queue name, will fallback to default session queue
name|conf
operator|.
name|unset
argument_list|(
literal|"tez.queue.name"
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|reopen
argument_list|(
name|session
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"default"
argument_list|,
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
comment|// session.open will unset the queue name from conf but Mockito intercepts the open call
comment|// and does not call the real method, so explicitly unset the queue name here
name|conf
operator|.
name|unset
argument_list|(
literal|"tez.queue.name"
argument_list|)
expr_stmt|;
comment|// change session's default queue to tezq1 and rerun test sequence
name|Mockito
operator|.
name|when
argument_list|(
name|session
operator|.
name|getQueueName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"tezq1"
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|reopen
argument_list|(
name|session
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"tezq1"
argument_list|,
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
comment|// user sets default queue now
name|conf
operator|.
name|set
argument_list|(
literal|"tez.queue.name"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|reopen
argument_list|(
name|session
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"default"
argument_list|,
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
comment|// user does not specify queue so use session default
name|conf
operator|.
name|unset
argument_list|(
literal|"tez.queue.name"
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|reopen
argument_list|(
name|session
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"tezq1"
argument_list|,
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
operator|.
name|getQueueName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLlapSessionQueuing
parameter_list|()
block|{
try|try
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_LLAP_CONCURRENT_QUERIES
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|poolManager
operator|=
operator|new
name|TestTezSessionPoolManager
argument_list|()
expr_stmt|;
name|poolManager
operator|.
name|setupPool
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|startPool
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Initialization error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|Thread
argument_list|>
name|threadList
init|=
operator|new
name|ArrayList
argument_list|<
name|Thread
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|15
condition|;
name|i
operator|++
control|)
block|{
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
operator|new
name|SessionThread
argument_list|(
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|threadList
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|Thread
name|t
range|:
name|threadList
control|)
block|{
try|try
block|{
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
class|class
name|SessionThread
implements|implements
name|Runnable
block|{
specifier|private
name|boolean
name|llap
init|=
literal|false
decl_stmt|;
specifier|public
name|SessionThread
parameter_list|(
name|boolean
name|llap
parameter_list|)
block|{
name|this
operator|.
name|llap
operator|=
name|llap
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|HiveConf
name|tmpConf
init|=
operator|new
name|HiveConf
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|random
operator|.
name|nextDouble
argument_list|()
operator|>
literal|0.5
condition|)
block|{
name|tmpConf
operator|.
name|set
argument_list|(
literal|"tez.queue.name"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tmpConf
operator|.
name|set
argument_list|(
literal|"tez.queue.name"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
name|TezSessionState
name|session
init|=
name|poolManager
operator|.
name|getSession
argument_list|(
literal|null
argument_list|,
name|tmpConf
argument_list|,
literal|true
argument_list|,
name|llap
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
operator|(
name|random
operator|.
name|nextInt
argument_list|(
literal|9
argument_list|)
operator|%
literal|10
operator|)
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|session
operator|.
name|setLegacyLlapMode
argument_list|(
name|llap
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|returnSession
argument_list|(
name|session
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReturn
parameter_list|()
block|{
name|conf
operator|.
name|set
argument_list|(
literal|"tez.queue.name"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|random
operator|=
operator|new
name|Random
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_DEFAULT_QUEUES
argument_list|,
literal|"a,b,c"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_SESSIONS_PER_DEFAULT_QUEUE
argument_list|,
literal|2
argument_list|)
expr_stmt|;
try|try
block|{
name|poolManager
operator|=
operator|new
name|TestTezSessionPoolManager
argument_list|()
expr_stmt|;
name|poolManager
operator|.
name|setupPool
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|startPool
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|Thread
argument_list|>
name|threadList
init|=
operator|new
name|ArrayList
argument_list|<
name|Thread
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|15
condition|;
name|i
operator|++
control|)
block|{
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
operator|new
name|SessionThread
argument_list|(
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|threadList
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|Thread
name|t
range|:
name|threadList
control|)
block|{
try|try
block|{
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCloseAndOpenDefault
parameter_list|()
throws|throws
name|Exception
block|{
name|poolManager
operator|=
operator|new
name|TestTezSessionPoolManager
argument_list|()
expr_stmt|;
name|TezSessionState
name|session
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|TezSessionState
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|session
operator|.
name|isDefault
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|session
operator|.
name|getConf
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|reopen
argument_list|(
name|session
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|session
argument_list|)
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|session
argument_list|)
operator|.
name|open
argument_list|(
name|Mockito
operator|.
expr|<
name|TezSessionState
operator|.
name|HiveResources
operator|>
name|any
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSessionDestroy
parameter_list|()
throws|throws
name|Exception
block|{
name|poolManager
operator|=
operator|new
name|TestTezSessionPoolManager
argument_list|()
expr_stmt|;
name|TezSessionState
name|session
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|TezSessionState
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|session
operator|.
name|isDefault
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|poolManager
operator|.
name|destroy
argument_list|(
name|session
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

