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
name|common
operator|.
name|util
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
name|*
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
name|concurrent
operator|.
name|CountDownLatch
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
name|ExecutorService
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
name|Executors
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
name|FutureTask
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
name|common
operator|.
name|util
operator|.
name|FixedSizedObjectPool
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
name|common
operator|.
name|Pool
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
name|TestFixedSizedObjectPool
block|{
specifier|private
specifier|static
specifier|abstract
class|class
name|PoolRunnable
implements|implements
name|Runnable
block|{
specifier|protected
specifier|final
name|FixedSizedObjectPool
argument_list|<
name|Object
argument_list|>
name|pool
decl_stmt|;
specifier|private
specifier|final
name|CountDownLatch
name|cdlIn
decl_stmt|;
specifier|private
specifier|final
name|CountDownLatch
name|cdlOut
decl_stmt|;
specifier|public
specifier|final
name|List
argument_list|<
name|Object
argument_list|>
name|objects
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|count
decl_stmt|;
name|PoolRunnable
parameter_list|(
name|FixedSizedObjectPool
argument_list|<
name|Object
argument_list|>
name|pool
parameter_list|,
name|CountDownLatch
name|cdlIn
parameter_list|,
name|CountDownLatch
name|cdlOut
parameter_list|,
name|int
name|count
parameter_list|)
block|{
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
name|this
operator|.
name|cdlIn
operator|=
name|cdlIn
expr_stmt|;
name|this
operator|.
name|cdlOut
operator|=
name|cdlOut
expr_stmt|;
name|this
operator|.
name|count
operator|=
name|count
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|syncThreadStart
argument_list|(
name|cdlIn
argument_list|,
name|cdlOut
argument_list|)
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
name|count
condition|;
operator|++
name|i
control|)
block|{
name|doOneOp
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
specifier|abstract
name|void
name|doOneOp
parameter_list|()
function_decl|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|OfferRunnable
extends|extends
name|PoolRunnable
block|{
name|OfferRunnable
parameter_list|(
name|FixedSizedObjectPool
argument_list|<
name|Object
argument_list|>
name|pool
parameter_list|,
name|CountDownLatch
name|cdlIn
parameter_list|,
name|CountDownLatch
name|cdlOut
parameter_list|,
name|int
name|count
parameter_list|)
block|{
name|super
argument_list|(
name|pool
argument_list|,
name|cdlIn
argument_list|,
name|cdlOut
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doOneOp
parameter_list|()
block|{
name|Object
name|o
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
if|if
condition|(
name|pool
operator|.
name|tryOffer
argument_list|(
name|o
argument_list|)
condition|)
block|{
name|objects
operator|.
name|add
argument_list|(
name|o
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|TakeRunnable
extends|extends
name|PoolRunnable
block|{
name|TakeRunnable
parameter_list|(
name|FixedSizedObjectPool
argument_list|<
name|Object
argument_list|>
name|pool
parameter_list|,
name|CountDownLatch
name|cdlIn
parameter_list|,
name|CountDownLatch
name|cdlOut
parameter_list|,
name|int
name|count
parameter_list|)
block|{
name|super
argument_list|(
name|pool
argument_list|,
name|cdlIn
argument_list|,
name|cdlOut
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doOneOp
parameter_list|()
block|{
name|Object
name|o
init|=
name|pool
operator|.
name|take
argument_list|()
decl_stmt|;
if|if
condition|(
name|o
operator|!=
name|OneObjHelper
operator|.
name|THE_OBJECT
condition|)
block|{
name|objects
operator|.
name|add
argument_list|(
name|o
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|DummyHelper
implements|implements
name|Pool
operator|.
name|PoolObjectHelper
argument_list|<
name|Object
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|Object
name|create
parameter_list|()
block|{
return|return
operator|new
name|Object
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|resetBeforeOffer
parameter_list|(
name|Object
name|t
parameter_list|)
block|{     }
block|}
specifier|private
specifier|static
class|class
name|OneObjHelper
implements|implements
name|Pool
operator|.
name|PoolObjectHelper
argument_list|<
name|Object
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|Object
name|THE_OBJECT
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Object
name|create
parameter_list|()
block|{
return|return
name|THE_OBJECT
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|resetBeforeOffer
parameter_list|(
name|Object
name|t
parameter_list|)
block|{     }
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFullEmpty
parameter_list|()
block|{
specifier|final
name|int
name|SIZE
init|=
literal|8
decl_stmt|;
name|HashSet
argument_list|<
name|Object
argument_list|>
name|offered
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|FixedSizedObjectPool
argument_list|<
name|Object
argument_list|>
name|pool
init|=
operator|new
name|FixedSizedObjectPool
argument_list|<>
argument_list|(
name|SIZE
argument_list|,
operator|new
name|DummyHelper
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Object
name|newObj
init|=
name|pool
operator|.
name|take
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
name|SIZE
condition|;
operator|++
name|i
control|)
block|{
name|Object
name|obj
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
name|offered
operator|.
name|add
argument_list|(
name|obj
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|pool
operator|.
name|tryOffer
argument_list|(
name|obj
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|pool
operator|.
name|tryOffer
argument_list|(
name|newObj
argument_list|)
argument_list|)
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
name|SIZE
condition|;
operator|++
name|i
control|)
block|{
name|Object
name|obj
init|=
name|pool
operator|.
name|take
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|offered
operator|.
name|remove
argument_list|(
name|obj
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|offered
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|Object
name|newObj2
init|=
name|pool
operator|.
name|take
argument_list|()
decl_stmt|;
name|assertNotSame
argument_list|(
name|newObj
argument_list|,
name|newObj2
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestFixedSizedObjectPool
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testMTT1
parameter_list|()
block|{
name|testMTTImpl
argument_list|(
literal|1
argument_list|,
literal|3
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMTT8
parameter_list|()
block|{
name|testMTTImpl
argument_list|(
literal|8
argument_list|,
literal|3
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMTT4096
parameter_list|()
block|{
name|testMTTImpl
argument_list|(
literal|4096
argument_list|,
literal|3
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMTT4096_1
parameter_list|()
block|{
name|testMTTImpl
argument_list|(
literal|4096
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMTT20000
parameter_list|()
block|{
name|testMTTImpl
argument_list|(
literal|20000
argument_list|,
literal|3
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMTT4096_10
parameter_list|()
block|{
name|testMTTImpl
argument_list|(
literal|4096
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testMTTImpl
parameter_list|(
name|int
name|size
parameter_list|,
name|int
name|takerCount
parameter_list|,
name|int
name|giverCount
parameter_list|)
block|{
specifier|final
name|int
name|TASK_COUNT
init|=
name|takerCount
operator|+
name|giverCount
decl_stmt|,
name|GIVECOUNT
init|=
literal|15000
decl_stmt|,
name|TAKECOUNT
init|=
literal|15000
decl_stmt|;
name|ExecutorService
name|executor
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|TASK_COUNT
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|cdlIn
init|=
operator|new
name|CountDownLatch
argument_list|(
name|TASK_COUNT
argument_list|)
decl_stmt|,
name|cdlOut
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|FixedSizedObjectPool
argument_list|<
name|Object
argument_list|>
name|pool
init|=
operator|new
name|FixedSizedObjectPool
argument_list|<>
argument_list|(
name|size
argument_list|,
operator|new
name|OneObjHelper
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// Pre-fill the pool halfway.
name|HashSet
argument_list|<
name|Object
argument_list|>
name|allGiven
init|=
operator|new
name|HashSet
argument_list|<>
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
operator|(
name|size
operator|>>
literal|1
operator|)
condition|;
operator|++
name|i
control|)
block|{
name|Object
name|o
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
name|allGiven
operator|.
name|add
argument_list|(
name|o
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|pool
operator|.
name|tryOffer
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|FutureTask
argument_list|<
name|Object
argument_list|>
index|[]
name|tasks
init|=
operator|new
name|FutureTask
index|[
name|TASK_COUNT
index|]
decl_stmt|;
name|TakeRunnable
index|[]
name|takers
init|=
operator|new
name|TakeRunnable
index|[
name|takerCount
index|]
decl_stmt|;
name|OfferRunnable
index|[]
name|givers
init|=
operator|new
name|OfferRunnable
index|[
name|giverCount
index|]
decl_stmt|;
name|int
name|ti
init|=
literal|0
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
name|takerCount
condition|;
operator|++
name|i
operator|,
operator|++
name|ti
control|)
block|{
name|takers
index|[
name|i
index|]
operator|=
operator|new
name|TakeRunnable
argument_list|(
name|pool
argument_list|,
name|cdlIn
argument_list|,
name|cdlOut
argument_list|,
name|TAKECOUNT
argument_list|)
expr_stmt|;
name|tasks
index|[
name|ti
index|]
operator|=
operator|new
name|FutureTask
argument_list|<
name|Object
argument_list|>
argument_list|(
name|takers
index|[
name|i
index|]
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|executor
operator|.
name|execute
argument_list|(
name|tasks
index|[
name|ti
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
name|giverCount
condition|;
operator|++
name|i
operator|,
operator|++
name|ti
control|)
block|{
name|givers
index|[
name|i
index|]
operator|=
operator|new
name|OfferRunnable
argument_list|(
name|pool
argument_list|,
name|cdlIn
argument_list|,
name|cdlOut
argument_list|,
name|GIVECOUNT
argument_list|)
expr_stmt|;
name|tasks
index|[
name|ti
index|]
operator|=
operator|new
name|FutureTask
argument_list|<
name|Object
argument_list|>
argument_list|(
name|givers
index|[
name|i
index|]
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|executor
operator|.
name|execute
argument_list|(
name|tasks
index|[
name|ti
index|]
argument_list|)
expr_stmt|;
block|}
name|long
name|time
init|=
literal|0
decl_stmt|;
try|try
block|{
name|cdlIn
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// Wait for all threads to be ready.
name|time
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
name|cdlOut
operator|.
name|countDown
argument_list|()
expr_stmt|;
comment|// Release them at the same time.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|TASK_COUNT
condition|;
operator|++
name|i
control|)
block|{
name|tasks
index|[
name|i
index|]
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|time
operator|=
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|time
operator|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|t
argument_list|)
throw|;
block|}
name|int
name|given
init|=
name|allGiven
operator|.
name|size
argument_list|()
decl_stmt|,
name|takenOld
init|=
literal|0
decl_stmt|;
for|for
control|(
name|OfferRunnable
name|g
range|:
name|givers
control|)
block|{
for|for
control|(
name|Object
name|o
range|:
name|g
operator|.
name|objects
control|)
block|{
name|assertTrue
argument_list|(
name|allGiven
operator|.
name|add
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
operator|++
name|given
expr_stmt|;
block|}
block|}
for|for
control|(
name|TakeRunnable
name|t
range|:
name|takers
control|)
block|{
for|for
control|(
name|Object
name|o
range|:
name|t
operator|.
name|objects
control|)
block|{
name|assertTrue
argument_list|(
name|allGiven
operator|.
name|remove
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
operator|++
name|takenOld
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"MTT test - size "
operator|+
name|size
operator|+
literal|", takers/givers "
operator|+
name|takerCount
operator|+
literal|"/"
operator|+
name|giverCount
operator|+
literal|"; offered "
operator|+
operator|(
name|given
operator|-
operator|(
name|size
operator|>>
literal|1
operator|)
operator|)
operator|+
literal|" (attempted "
operator|+
operator|(
name|GIVECOUNT
operator|*
name|giverCount
operator|)
operator|+
literal|"); reused "
operator|+
name|takenOld
operator|+
literal|", allocated "
operator|+
operator|(
operator|(
name|TAKECOUNT
operator|*
name|takerCount
operator|)
operator|-
name|takenOld
operator|)
operator|+
literal|" (took "
operator|+
name|time
operator|/
literal|1000000L
operator|+
literal|"ms including thread sync)"
argument_list|)
expr_stmt|;
comment|// Most of the above will be failed offers and takes (due to speed of the thing).
comment|// Verify that we can drain the pool, then cycle it, i.e. the state is not corrupted.
while|while
condition|(
name|pool
operator|.
name|take
argument_list|()
operator|!=
name|OneObjHelper
operator|.
name|THE_OBJECT
condition|)
empty_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
operator|++
name|i
control|)
block|{
name|assertTrue
argument_list|(
name|pool
operator|.
name|tryOffer
argument_list|(
operator|new
name|Object
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|pool
operator|.
name|tryOffer
argument_list|(
operator|new
name|Object
argument_list|()
argument_list|)
argument_list|)
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
name|size
condition|;
operator|++
name|i
control|)
block|{
name|assertTrue
argument_list|(
name|OneObjHelper
operator|.
name|THE_OBJECT
operator|!=
name|pool
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|OneObjHelper
operator|.
name|THE_OBJECT
operator|==
name|pool
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|syncThreadStart
parameter_list|(
specifier|final
name|CountDownLatch
name|cdlIn
parameter_list|,
specifier|final
name|CountDownLatch
name|cdlOut
parameter_list|)
block|{
name|cdlIn
operator|.
name|countDown
argument_list|()
expr_stmt|;
try|try
block|{
name|cdlOut
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
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
block|}
end_class

end_unit

