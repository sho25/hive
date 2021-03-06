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
name|lock
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
name|Semaphore
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
name|TimeUnit
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
name|locks
operator|.
name|Condition
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
name|locks
operator|.
name|Lock
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
name|locks
operator|.
name|ReentrantLock
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
import|;
end_import

begin_comment
comment|/**  * Compile Lock Factory.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|CompileLockFactory
block|{
specifier|private
specifier|static
specifier|final
name|ReentrantLock
name|SERIALIZABLE_COMPILE_LOCK
init|=
operator|new
name|ReentrantLock
argument_list|(
literal|true
argument_list|)
decl_stmt|;
specifier|private
name|CompileLockFactory
parameter_list|()
block|{   }
specifier|public
specifier|static
name|CompileLock
name|newInstance
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|command
parameter_list|)
block|{
name|Lock
name|underlying
init|=
name|SERIALIZABLE_COMPILE_LOCK
decl_stmt|;
name|boolean
name|isParallelEnabled
init|=
operator|(
name|conf
operator|!=
literal|null
operator|)
operator|&&
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PARALLEL_COMPILATION
argument_list|)
decl_stmt|;
if|if
condition|(
name|isParallelEnabled
condition|)
block|{
name|int
name|compileQuota
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PARALLEL_COMPILATION_LIMIT
argument_list|)
decl_stmt|;
name|underlying
operator|=
operator|(
name|compileQuota
operator|>
literal|0
operator|)
condition|?
name|SessionWithQuotaCompileLock
operator|.
name|instance
else|:
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getCompileLock
argument_list|()
expr_stmt|;
block|}
name|long
name|timeout
init|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_COMPILE_LOCK_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
return|return
operator|new
name|CompileLock
argument_list|(
name|underlying
argument_list|,
name|timeout
argument_list|,
name|command
argument_list|)
return|;
block|}
comment|/**    * Combination of global semaphore and session reentrant lock.    */
specifier|private
enum|enum
name|SessionWithQuotaCompileLock
implements|implements
name|Lock
block|{
name|instance
argument_list|(
name|SessionState
operator|.
name|getSessionConf
argument_list|()
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PARALLEL_COMPILATION_LIMIT
argument_list|)
argument_list|)
block|;
specifier|private
specifier|final
name|Semaphore
name|globalCompileQuotas
decl_stmt|;
name|SessionWithQuotaCompileLock
parameter_list|(
name|int
name|compilePoolSize
parameter_list|)
block|{
name|globalCompileQuotas
operator|=
operator|new
name|Semaphore
argument_list|(
name|compilePoolSize
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|lock
parameter_list|()
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getCompileLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
name|globalCompileQuotas
operator|.
name|acquireUninterruptibly
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|tryLock
parameter_list|(
name|long
name|time
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|ReentrantLock
name|compileLock
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getCompileLock
argument_list|()
decl_stmt|;
try|try
block|{
name|result
operator|=
name|compileLock
operator|.
name|tryLock
argument_list|(
name|time
argument_list|,
name|unit
argument_list|)
operator|&&
name|globalCompileQuotas
operator|.
name|tryAcquire
argument_list|(
name|getRemainingTime
argument_list|(
name|startTime
argument_list|,
name|unit
operator|.
name|toNanos
argument_list|(
name|time
argument_list|)
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|result
operator|&&
name|compileLock
operator|.
name|isHeldByCurrentThread
argument_list|()
condition|)
block|{
name|compileLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|unlock
parameter_list|()
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getCompileLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
name|globalCompileQuotas
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
specifier|private
name|long
name|getRemainingTime
parameter_list|(
name|long
name|startTime
parameter_list|,
name|long
name|time
parameter_list|)
block|{
name|long
name|timeout
init|=
name|time
operator|-
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startTime
operator|)
decl_stmt|;
return|return
operator|(
name|timeout
operator|<
literal|0
operator|)
condition|?
literal|0
else|:
name|timeout
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|lockInterruptibly
parameter_list|()
throws|throws
name|InterruptedException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|tryLock
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Condition
name|newCondition
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
block|}
end_class

end_unit

