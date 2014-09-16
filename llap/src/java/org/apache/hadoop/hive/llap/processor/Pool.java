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
name|llap
operator|.
name|processor
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|llap
operator|.
name|api
operator|.
name|impl
operator|.
name|RequestImpl
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
name|llap
operator|.
name|loader
operator|.
name|Loader
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
name|llap
operator|.
name|loader
operator|.
name|OrcLoader
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
import|;
end_import

begin_comment
comment|// TODO: write unit tests if this class becomes less primitive.
end_comment

begin_class
specifier|public
class|class
name|Pool
block|{
comment|// TODO: for now, pool is of dubious value. There's one processor per request.
comment|//       So, this provides thread safety that may or may not be needed.
specifier|private
specifier|final
name|LinkedList
argument_list|<
name|Processor
argument_list|>
name|processors
init|=
operator|new
name|LinkedList
argument_list|<
name|Processor
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|POOL_LIMIT
init|=
literal|10
decl_stmt|;
comment|// There's only one loader, assumed to be thread safe.
specifier|private
specifier|final
name|Loader
name|loader
decl_stmt|;
specifier|private
specifier|final
name|ExecutorService
name|threadPool
decl_stmt|;
specifier|public
name|Pool
parameter_list|(
name|Loader
name|loader
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|loader
operator|=
name|loader
expr_stmt|;
name|int
name|threadCount
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
name|LLAP_REQUEST_THREAD_COUNT
argument_list|)
decl_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|threadCount
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"Llap thread %d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|enqueue
parameter_list|(
name|RequestImpl
name|request
parameter_list|,
name|ChunkConsumer
name|consumer
parameter_list|)
block|{
name|Processor
name|proc
init|=
literal|null
decl_stmt|;
synchronized|synchronized
init|(
name|processors
init|)
block|{
name|proc
operator|=
name|processors
operator|.
name|poll
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|proc
operator|==
literal|null
condition|)
block|{
name|proc
operator|=
operator|new
name|Processor
argument_list|(
name|this
argument_list|,
name|loader
argument_list|)
expr_stmt|;
block|}
name|proc
operator|.
name|setRequest
argument_list|(
name|request
argument_list|,
name|consumer
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|submit
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
name|void
name|returnProcessor
parameter_list|(
name|Processor
name|proc
parameter_list|)
block|{
synchronized|synchronized
init|(
name|processors
init|)
block|{
if|if
condition|(
name|processors
operator|.
name|size
argument_list|()
operator|<
name|POOL_LIMIT
condition|)
block|{
name|processors
operator|.
name|add
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

