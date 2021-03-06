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
name|kafka
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
name|base
operator|.
name|Preconditions
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
name|base
operator|.
name|Throwables
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
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
name|ThreadLocalRandom
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Predicate
import|;
end_import

begin_comment
comment|/**  * Retry utils class mostly taken from Apache Druid Project org.apache.druid.java.util.common.RetryUtils.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|RetryUtils
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
name|RetryUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|MAX_SLEEP_MILLIS
init|=
literal|60000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|BASE_SLEEP_MILLIS
init|=
literal|1000
decl_stmt|;
specifier|private
name|RetryUtils
parameter_list|()
block|{   }
comment|/**    * Task to be performed.    * @param<T> returned type of the task.    */
specifier|public
interface|interface
name|Task
parameter_list|<
name|T
parameter_list|>
block|{
comment|/**      * This method is tried up to maxTries times unless it succeeds.      */
name|T
name|perform
parameter_list|()
throws|throws
name|Exception
function_decl|;
block|}
comment|/**    * Cleanup procedure after each failed attempt.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"WeakerAccess"
argument_list|)
specifier|public
interface|interface
name|CleanupAfterFailure
block|{
comment|/**      * This is called once {@link Task#perform()} fails. Retrying is stopped once this method throws an exception,      * so errors inside this method should be ignored if you don't want to stop retrying.      */
name|void
name|cleanup
parameter_list|()
function_decl|;
block|}
comment|/**    * Retry an operation using fuzzy exponentially increasing backoff. The wait time after the nth failed attempt is    * min(60000ms, 1000ms * pow(2, n - 1)), fuzzed by a number drawn from a Gaussian distribution with mean 0 and    * standard deviation 0.2.    *    * If maxTries is exhausted, or if shouldRetry returns false, the last exception thrown by "f" will be thrown    * by this function.    *    * @param f           the operation    * @param shouldRetry predicate determining whether we should retry after a particular exception thrown by "f"    * @param quietTries  first quietTries attempts will LOG exceptions at DEBUG level rather than WARN    * @param maxTries    maximum number of attempts    *    * @return result of the first successful operation    *    * @throws Exception if maxTries is exhausted, or shouldRetry returns false    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"WeakerAccess"
argument_list|)
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|retry
parameter_list|(
specifier|final
name|Task
argument_list|<
name|T
argument_list|>
name|f
parameter_list|,
specifier|final
name|Predicate
argument_list|<
name|Throwable
argument_list|>
name|shouldRetry
parameter_list|,
specifier|final
name|int
name|quietTries
parameter_list|,
specifier|final
name|int
name|maxTries
parameter_list|,
annotation|@
name|Nullable
specifier|final
name|CleanupAfterFailure
name|cleanupAfterFailure
parameter_list|,
annotation|@
name|Nullable
specifier|final
name|String
name|messageOnRetry
parameter_list|)
throws|throws
name|Exception
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|maxTries
operator|>
literal|0
argument_list|,
literal|"maxTries> 0"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|quietTries
operator|>=
literal|0
argument_list|,
literal|"quietTries>= 0"
argument_list|)
expr_stmt|;
name|int
name|nTry
init|=
literal|0
decl_stmt|;
specifier|final
name|int
name|maxRetries
init|=
name|maxTries
operator|-
literal|1
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|nTry
operator|++
expr_stmt|;
return|return
name|f
operator|.
name|perform
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|cleanupAfterFailure
operator|!=
literal|null
condition|)
block|{
name|cleanupAfterFailure
operator|.
name|cleanup
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|nTry
operator|<
name|maxTries
operator|&&
name|shouldRetry
operator|.
name|test
argument_list|(
name|e
argument_list|)
condition|)
block|{
name|awaitNextRetry
argument_list|(
name|e
argument_list|,
name|messageOnRetry
argument_list|,
name|nTry
argument_list|,
name|maxRetries
argument_list|,
name|nTry
operator|<=
name|quietTries
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Throwables
operator|.
name|propagateIfInstanceOf
argument_list|(
name|e
argument_list|,
name|Exception
operator|.
name|class
argument_list|)
expr_stmt|;
throw|throw
name|Throwables
operator|.
name|propagate
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|retry
parameter_list|(
specifier|final
name|Task
argument_list|<
name|T
argument_list|>
name|f
parameter_list|,
name|Predicate
argument_list|<
name|Throwable
argument_list|>
name|shouldRetry
parameter_list|,
specifier|final
name|int
name|maxTries
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|retry
argument_list|(
name|f
argument_list|,
name|shouldRetry
argument_list|,
literal|0
argument_list|,
name|maxTries
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"WeakerAccess"
block|,
literal|"SameParameterValue"
block|}
argument_list|)
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|retry
parameter_list|(
specifier|final
name|Task
argument_list|<
name|T
argument_list|>
name|f
parameter_list|,
specifier|final
name|Predicate
argument_list|<
name|Throwable
argument_list|>
name|shouldRetry
parameter_list|,
specifier|final
name|int
name|quietTries
parameter_list|,
specifier|final
name|int
name|maxTries
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|retry
argument_list|(
name|f
argument_list|,
name|shouldRetry
argument_list|,
name|quietTries
argument_list|,
name|maxTries
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|retry
parameter_list|(
specifier|final
name|Task
argument_list|<
name|T
argument_list|>
name|f
parameter_list|,
specifier|final
name|Predicate
argument_list|<
name|Throwable
argument_list|>
name|shouldRetry
parameter_list|,
specifier|final
name|CleanupAfterFailure
name|onEachFailure
parameter_list|,
specifier|final
name|int
name|maxTries
parameter_list|,
specifier|final
name|String
name|messageOnRetry
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|retry
argument_list|(
name|f
argument_list|,
name|shouldRetry
argument_list|,
literal|0
argument_list|,
name|maxTries
argument_list|,
name|onEachFailure
argument_list|,
name|messageOnRetry
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|void
name|awaitNextRetry
parameter_list|(
specifier|final
name|Throwable
name|e
parameter_list|,
annotation|@
name|Nullable
specifier|final
name|String
name|messageOnRetry
parameter_list|,
specifier|final
name|int
name|nTry
parameter_list|,
specifier|final
name|int
name|maxRetries
parameter_list|,
specifier|final
name|boolean
name|quiet
parameter_list|)
throws|throws
name|InterruptedException
block|{
specifier|final
name|long
name|sleepMillis
init|=
name|nextRetrySleepMillis
argument_list|(
name|nTry
argument_list|)
decl_stmt|;
specifier|final
name|String
name|fullMessage
decl_stmt|;
if|if
condition|(
name|messageOnRetry
operator|==
literal|null
condition|)
block|{
name|fullMessage
operator|=
name|String
operator|.
name|format
argument_list|(
literal|"Retrying (%d of %d) in %,dms."
argument_list|,
name|nTry
argument_list|,
name|maxRetries
argument_list|,
name|sleepMillis
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fullMessage
operator|=
name|String
operator|.
name|format
argument_list|(
literal|"%s, retrying (%d of %d) in %,dms."
argument_list|,
name|messageOnRetry
argument_list|,
name|nTry
argument_list|,
name|maxRetries
argument_list|,
name|sleepMillis
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|quiet
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|fullMessage
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|fullMessage
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepMillis
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|long
name|nextRetrySleepMillis
parameter_list|(
specifier|final
name|int
name|nTry
parameter_list|)
block|{
specifier|final
name|double
name|fuzzyMultiplier
init|=
name|Math
operator|.
name|min
argument_list|(
name|Math
operator|.
name|max
argument_list|(
literal|1
operator|+
literal|0.2
operator|*
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextGaussian
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|,
literal|2
argument_list|)
decl_stmt|;
return|return
call|(
name|long
call|)
argument_list|(
name|Math
operator|.
name|min
argument_list|(
name|MAX_SLEEP_MILLIS
argument_list|,
name|BASE_SLEEP_MILLIS
operator|*
name|Math
operator|.
name|pow
argument_list|(
literal|2
argument_list|,
name|nTry
operator|-
literal|1
argument_list|)
argument_list|)
operator|*
name|fuzzyMultiplier
argument_list|)
return|;
block|}
block|}
end_class

end_unit

