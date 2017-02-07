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
name|DebugUtils
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
name|io
operator|.
name|api
operator|.
name|impl
operator|.
name|LlapIoImpl
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
name|LlapDaemonIOMetrics
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|TypeDescription
import|;
end_import

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
name|LlapDaemonIOMetrics
name|ioMetrics
decl_stmt|;
comment|// Note that the pool is per EDC - within EDC, CVBs are expected to have the same schema.
specifier|private
specifier|final
specifier|static
name|int
name|CVB_POOL_SIZE
init|=
literal|128
decl_stmt|;
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
name|LlapDaemonIOMetrics
name|ioMetrics
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
name|ioMetrics
operator|=
name|ioMetrics
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
block|{
comment|// Don't reset anything, we are reusing column vectors.
block|}
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
if|if
condition|(
name|isStopped
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
try|try
block|{
name|decodeBatch
argument_list|(
name|data
argument_list|,
name|downstreamConsumer
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ex
parameter_list|)
block|{
comment|// This probably should not happen; but it does... at least also stop the consumer.
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|error
argument_list|(
literal|"decodeBatch threw"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|downstreamConsumer
operator|.
name|setError
argument_list|(
name|ex
argument_list|)
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
finally|finally
block|{
name|long
name|end
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|ioMetrics
operator|.
name|addDecodeBatchTime
argument_list|(
name|end
operator|-
name|start
argument_list|)
expr_stmt|;
block|}
name|returnSourceData
argument_list|(
name|data
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
name|this
operator|.
name|isStopped
operator|=
literal|true
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

