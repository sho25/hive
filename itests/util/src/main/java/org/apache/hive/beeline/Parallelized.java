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
name|beeline
package|;
end_package

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
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
name|RunnerScheduler
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
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * Class to run Parameterized test in parallel.  * Source: http://hwellmann.blogspot.hu/2009/12/running-parameterized-junit-tests-in.html  */
end_comment

begin_class
specifier|public
class|class
name|Parallelized
extends|extends
name|Parameterized
block|{
specifier|private
specifier|static
class|class
name|ThreadPoolScheduler
implements|implements
name|RunnerScheduler
block|{
specifier|private
name|ExecutorService
name|executor
decl_stmt|;
specifier|public
name|ThreadPoolScheduler
parameter_list|()
block|{
name|String
name|threads
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"junit.parallel.threads"
argument_list|)
decl_stmt|;
name|int
name|numThreads
init|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
decl_stmt|;
if|if
condition|(
name|threads
operator|!=
literal|null
condition|)
block|{
name|numThreads
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|threads
argument_list|)
expr_stmt|;
block|}
name|executor
operator|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|numThreads
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|finished
parameter_list|()
block|{
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
try|try
block|{
name|executor
operator|.
name|awaitTermination
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|exc
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|exc
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|schedule
parameter_list|(
name|Runnable
name|childStatement
parameter_list|)
block|{
name|executor
operator|.
name|submit
argument_list|(
name|childStatement
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Parallelized
parameter_list|(
name|Class
name|klass
parameter_list|)
throws|throws
name|Throwable
block|{
name|super
argument_list|(
name|klass
argument_list|)
expr_stmt|;
name|setScheduler
argument_list|(
operator|new
name|ThreadPoolScheduler
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

