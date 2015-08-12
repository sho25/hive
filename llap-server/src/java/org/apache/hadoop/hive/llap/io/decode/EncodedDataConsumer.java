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
name|io
operator|.
name|decode
package|;
end_package

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
name|HashMap
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
name|Callable
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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|io
operator|.
name|encoded
operator|.
name|EncodedColumnBatch
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
name|ConsumerFeedback
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
name|io
operator|.
name|api
operator|.
name|impl
operator|.
name|ColumnVectorBatch
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
name|metrics
operator|.
name|LlapDaemonQueueMetrics
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
name|io
operator|.
name|orc
operator|.
name|encoded
operator|.
name|Consumer
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

begin_comment
comment|/**  *  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|EncodedDataConsumer
parameter_list|<
name|BatchKey
parameter_list|,
name|BatchType
extends|extends
name|EncodedColumnBatch
parameter_list|<
name|BatchKey
parameter_list|>
parameter_list|>
implements|implements
name|Consumer
argument_list|<
name|BatchType
argument_list|>
implements|,
name|ReadPipeline
block|{
specifier|private
specifier|volatile
name|boolean
name|isStopped
init|=
literal|false
decl_stmt|;
comment|// TODO: use array, precreate array based on metadata first? Works for ORC. For now keep dumb.
specifier|private
specifier|final
name|HashMap
argument_list|<
name|BatchKey
argument_list|,
name|BatchType
argument_list|>
name|pendingData
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|ConsumerFeedback
argument_list|<
name|BatchType
argument_list|>
name|upstreamFeedback
decl_stmt|;
specifier|private
specifier|final
name|Consumer
argument_list|<
name|ColumnVectorBatch
argument_list|>
name|downstreamConsumer
decl_stmt|;
specifier|private
name|Callable
argument_list|<
name|Void
argument_list|>
name|readCallable
decl_stmt|;
specifier|private
specifier|final
name|LlapDaemonQueueMetrics
name|queueMetrics
decl_stmt|;
comment|// TODO: if we were using Exchanger, pool would not be necessary here - it would be 1/N items
specifier|private
specifier|final
specifier|static
name|int
name|CVB_POOL_SIZE
init|=
literal|8
decl_stmt|;
comment|// Note that the pool is per EDC - within EDC, CVBs are expected to have the same schema.
specifier|protected
specifier|final
name|FixedSizedObjectPool
argument_list|<
name|ColumnVectorBatch
argument_list|>
name|cvbPool
decl_stmt|;
specifier|public
name|EncodedDataConsumer
parameter_list|(
name|Consumer
argument_list|<
name|ColumnVectorBatch
argument_list|>
name|consumer
parameter_list|,
specifier|final
name|int
name|colCount
parameter_list|,
name|LlapDaemonQueueMetrics
name|queueMetrics
parameter_list|)
block|{
name|this
operator|.
name|downstreamConsumer
operator|=
name|consumer
expr_stmt|;
name|this
operator|.
name|queueMetrics
operator|=
name|queueMetrics
expr_stmt|;
name|cvbPool
operator|=
operator|new
name|FixedSizedObjectPool
argument_list|<
name|ColumnVectorBatch
argument_list|>
argument_list|(
name|CVB_POOL_SIZE
argument_list|,
operator|new
name|Pool
operator|.
name|PoolObjectHelper
argument_list|<
name|ColumnVectorBatch
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ColumnVectorBatch
name|create
parameter_list|()
block|{
return|return
operator|new
name|ColumnVectorBatch
argument_list|(
name|colCount
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|resetBeforeOffer
parameter_list|(
name|ColumnVectorBatch
name|t
parameter_list|)
block|{           }
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|(
name|ConsumerFeedback
argument_list|<
name|BatchType
argument_list|>
name|upstreamFeedback
parameter_list|,
name|Callable
argument_list|<
name|Void
argument_list|>
name|readCallable
parameter_list|)
block|{
name|this
operator|.
name|upstreamFeedback
operator|=
name|upstreamFeedback
expr_stmt|;
name|this
operator|.
name|readCallable
operator|=
name|readCallable
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Callable
argument_list|<
name|Void
argument_list|>
name|getReadCallable
parameter_list|()
block|{
return|return
name|readCallable
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|consumeData
parameter_list|(
name|BatchType
name|data
parameter_list|)
block|{
comment|// TODO: data arrives in whole batches now, not in columns. We could greatly simplify this.
name|BatchType
name|targetBatch
init|=
literal|null
decl_stmt|;
name|boolean
name|localIsStopped
init|=
literal|false
decl_stmt|;
name|Integer
name|targetBatchVersion
init|=
literal|null
decl_stmt|;
synchronized|synchronized
init|(
name|pendingData
init|)
block|{
name|localIsStopped
operator|=
name|isStopped
expr_stmt|;
if|if
condition|(
operator|!
name|localIsStopped
condition|)
block|{
name|targetBatch
operator|=
name|pendingData
operator|.
name|get
argument_list|(
name|data
operator|.
name|getBatchKey
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|targetBatch
operator|==
literal|null
condition|)
block|{
name|targetBatch
operator|=
name|data
expr_stmt|;
name|pendingData
operator|.
name|put
argument_list|(
name|data
operator|.
name|getBatchKey
argument_list|()
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
comment|// We have the map locked; the code the throws things away from map only bumps the version
comment|// under the same map lock; code the throws things away here only bumps the version when
comment|// the batch was taken out of the map.
name|targetBatchVersion
operator|=
name|targetBatch
operator|.
name|version
expr_stmt|;
block|}
name|queueMetrics
operator|.
name|setQueueSize
argument_list|(
name|pendingData
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|localIsStopped
condition|)
block|{
name|returnSourceData
argument_list|(
name|data
argument_list|)
expr_stmt|;
return|return;
block|}
assert|assert
name|targetBatchVersion
operator|!=
literal|null
assert|;
synchronized|synchronized
init|(
name|targetBatch
init|)
block|{
if|if
condition|(
name|targetBatch
operator|!=
name|data
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Merging is not supported"
argument_list|)
throw|;
block|}
synchronized|synchronized
init|(
name|pendingData
init|)
block|{
name|targetBatch
operator|=
name|isStopped
condition|?
literal|null
else|:
name|pendingData
operator|.
name|remove
argument_list|(
name|data
operator|.
name|getBatchKey
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check if someone already threw this away and changed the version.
name|localIsStopped
operator|=
operator|(
name|targetBatchVersion
operator|!=
name|targetBatch
operator|.
name|version
operator|)
expr_stmt|;
block|}
comment|// We took the batch out of the map. No more contention with stop possible.
block|}
if|if
condition|(
name|localIsStopped
operator|&&
operator|(
name|targetBatch
operator|!=
name|data
operator|)
condition|)
block|{
name|returnSourceData
argument_list|(
name|data
argument_list|)
expr_stmt|;
return|return;
block|}
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|decodeBatch
argument_list|(
name|targetBatch
argument_list|,
name|downstreamConsumer
argument_list|)
expr_stmt|;
name|long
name|end
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|queueMetrics
operator|.
name|addProcessingTime
argument_list|(
name|end
operator|-
name|start
argument_list|)
expr_stmt|;
name|returnSourceData
argument_list|(
name|targetBatch
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the ECB to caller for reuse. Only safe to call if the thread is the only owner    * of the ECB in question; or, if ECB is still in pendingData, pendingData must be locked.    */
specifier|private
name|void
name|returnSourceData
parameter_list|(
name|BatchType
name|data
parameter_list|)
block|{
operator|++
name|data
operator|.
name|version
expr_stmt|;
name|upstreamFeedback
operator|.
name|returnData
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|void
name|decodeBatch
parameter_list|(
name|BatchType
name|batch
parameter_list|,
name|Consumer
argument_list|<
name|ColumnVectorBatch
argument_list|>
name|downstreamConsumer
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|void
name|setDone
parameter_list|()
block|{
synchronized|synchronized
init|(
name|pendingData
init|)
block|{
if|if
condition|(
operator|!
name|pendingData
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Not all data has been sent downstream: "
operator|+
name|pendingData
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|downstreamConsumer
operator|.
name|setDone
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setError
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|downstreamConsumer
operator|.
name|setError
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|dicardPendingData
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|returnData
parameter_list|(
name|ColumnVectorBatch
name|data
parameter_list|)
block|{
name|cvbPool
operator|.
name|offer
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|dicardPendingData
parameter_list|(
name|boolean
name|isStopped
parameter_list|)
block|{
name|List
argument_list|<
name|BatchType
argument_list|>
name|batches
init|=
operator|new
name|ArrayList
argument_list|<
name|BatchType
argument_list|>
argument_list|(
name|pendingData
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
synchronized|synchronized
init|(
name|pendingData
init|)
block|{
if|if
condition|(
name|isStopped
condition|)
block|{
name|this
operator|.
name|isStopped
operator|=
literal|true
expr_stmt|;
block|}
for|for
control|(
name|BatchType
name|ecb
range|:
name|pendingData
operator|.
name|values
argument_list|()
control|)
block|{
operator|++
name|ecb
operator|.
name|version
expr_stmt|;
name|batches
operator|.
name|add
argument_list|(
name|ecb
argument_list|)
expr_stmt|;
block|}
name|pendingData
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|BatchType
name|batch
range|:
name|batches
control|)
block|{
name|upstreamFeedback
operator|.
name|returnData
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|upstreamFeedback
operator|.
name|stop
argument_list|()
expr_stmt|;
name|dicardPendingData
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|pause
parameter_list|()
block|{
comment|// We are just a relay; send pause to encoded data producer.
name|upstreamFeedback
operator|.
name|pause
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|unpause
parameter_list|()
block|{
comment|// We are just a relay; send unpause to encoded data producer.
name|upstreamFeedback
operator|.
name|unpause
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

