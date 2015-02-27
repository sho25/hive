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
name|Consumer
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
name|io
operator|.
name|api
operator|.
name|EncodedColumnBatch
operator|.
name|StreamBuffer
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
parameter_list|>
implements|implements
name|ConsumerFeedback
argument_list|<
name|ColumnVectorBatch
argument_list|>
implements|,
name|Consumer
argument_list|<
name|EncodedColumnBatch
argument_list|<
name|BatchKey
argument_list|>
argument_list|>
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
name|EncodedColumnBatch
argument_list|<
name|BatchKey
argument_list|>
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
name|EncodedColumnBatch
operator|.
name|StreamBuffer
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
specifier|final
name|int
name|colCount
decl_stmt|;
specifier|private
name|ColumnVectorProducer
argument_list|<
name|BatchKey
argument_list|>
name|cvp
decl_stmt|;
specifier|public
name|EncodedDataConsumer
parameter_list|(
name|ColumnVectorProducer
argument_list|<
name|BatchKey
argument_list|>
name|cvp
parameter_list|,
name|Consumer
argument_list|<
name|ColumnVectorBatch
argument_list|>
name|consumer
parameter_list|,
name|int
name|colCount
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
name|colCount
operator|=
name|colCount
expr_stmt|;
name|this
operator|.
name|cvp
operator|=
name|cvp
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|(
name|ConsumerFeedback
argument_list|<
name|EncodedColumnBatch
operator|.
name|StreamBuffer
argument_list|>
name|upstreamFeedback
parameter_list|)
block|{
name|this
operator|.
name|upstreamFeedback
operator|=
name|upstreamFeedback
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|consumeData
parameter_list|(
name|EncodedColumnBatch
argument_list|<
name|BatchKey
argument_list|>
name|data
parameter_list|)
block|{
name|EncodedColumnBatch
argument_list|<
name|BatchKey
argument_list|>
name|targetBatch
init|=
literal|null
decl_stmt|;
name|boolean
name|localIsStopped
init|=
literal|false
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
name|batchKey
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
name|batchKey
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|localIsStopped
condition|)
block|{
name|returnProcessed
argument_list|(
name|data
operator|.
name|columnData
argument_list|)
expr_stmt|;
return|return;
block|}
synchronized|synchronized
init|(
name|targetBatch
init|)
block|{
comment|// Check if we are stopped and the batch was already cleaned.
name|localIsStopped
operator|=
operator|(
name|targetBatch
operator|.
name|columnData
operator|==
literal|null
operator|)
expr_stmt|;
if|if
condition|(
operator|!
name|localIsStopped
condition|)
block|{
if|if
condition|(
name|targetBatch
operator|!=
name|data
condition|)
block|{
name|targetBatch
operator|.
name|merge
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
literal|0
operator|==
name|targetBatch
operator|.
name|colsRemaining
condition|)
block|{
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
name|batchKey
argument_list|)
expr_stmt|;
block|}
comment|// Check if we are stopped and the batch had been removed from map.
name|localIsStopped
operator|=
operator|(
name|targetBatch
operator|==
literal|null
operator|)
expr_stmt|;
comment|// We took the batch out of the map. No more contention with stop possible.
block|}
block|}
block|}
if|if
condition|(
name|localIsStopped
condition|)
block|{
name|returnProcessed
argument_list|(
name|data
operator|.
name|columnData
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
literal|0
operator|==
name|targetBatch
operator|.
name|colsRemaining
condition|)
block|{
name|cvp
operator|.
name|decodeBatch
argument_list|(
name|this
argument_list|,
name|targetBatch
argument_list|,
name|downstreamConsumer
argument_list|)
expr_stmt|;
comment|// Batch has been decoded; unlock the buffers in cache
name|returnProcessed
argument_list|(
name|targetBatch
operator|.
name|columnData
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|returnProcessed
parameter_list|(
name|EncodedColumnBatch
operator|.
name|StreamBuffer
index|[]
index|[]
name|data
parameter_list|)
block|{
for|for
control|(
name|EncodedColumnBatch
operator|.
name|StreamBuffer
index|[]
name|sbs
range|:
name|data
control|)
block|{
for|for
control|(
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|sb
range|:
name|sbs
control|)
block|{
if|if
condition|(
name|sb
operator|.
name|decRef
argument_list|()
operator|!=
literal|0
condition|)
continue|continue;
name|upstreamFeedback
operator|.
name|returnData
argument_list|(
name|sb
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
comment|// TODO: column vectors could be added to object pool here
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
name|EncodedColumnBatch
argument_list|<
name|BatchKey
argument_list|>
argument_list|>
name|batches
init|=
operator|new
name|ArrayList
argument_list|<
name|EncodedColumnBatch
argument_list|<
name|BatchKey
argument_list|>
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
name|batches
operator|.
name|addAll
argument_list|(
name|pendingData
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|pendingData
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|EncodedColumnBatch
operator|.
name|StreamBuffer
argument_list|>
name|dataToDiscard
init|=
operator|new
name|ArrayList
argument_list|<
name|StreamBuffer
argument_list|>
argument_list|(
name|batches
operator|.
name|size
argument_list|()
operator|*
name|colCount
operator|*
literal|2
argument_list|)
decl_stmt|;
for|for
control|(
name|EncodedColumnBatch
argument_list|<
name|BatchKey
argument_list|>
name|batch
range|:
name|batches
control|)
block|{
synchronized|synchronized
init|(
name|batch
init|)
block|{
for|for
control|(
name|EncodedColumnBatch
operator|.
name|StreamBuffer
index|[]
name|bb
range|:
name|batch
operator|.
name|columnData
control|)
block|{
for|for
control|(
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|b
range|:
name|bb
control|)
block|{
name|dataToDiscard
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
block|}
name|batch
operator|.
name|columnData
operator|=
literal|null
expr_stmt|;
block|}
block|}
for|for
control|(
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|data
range|:
name|dataToDiscard
control|)
block|{
if|if
condition|(
name|data
operator|.
name|decRef
argument_list|()
operator|==
literal|0
condition|)
block|{
name|upstreamFeedback
operator|.
name|returnData
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
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

