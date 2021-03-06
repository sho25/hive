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
name|llap
operator|.
name|io
operator|.
name|encoded
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
name|Queue
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
name|ConcurrentLinkedQueue
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
name|decode
operator|.
name|EncodedDataConsumer
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
name|exec
operator|.
name|vector
operator|.
name|LongColumnVector
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
name|exec
operator|.
name|vector
operator|.
name|VectorizedRowBatch
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
name|WriterImpl
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
name|impl
operator|.
name|SchemaEvolution
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
name|assertNotEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|CALLS_REAL_METHODS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|withSettings
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|internal
operator|.
name|util
operator|.
name|reflection
operator|.
name|Whitebox
operator|.
name|getInternalState
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|internal
operator|.
name|util
operator|.
name|reflection
operator|.
name|Whitebox
operator|.
name|setInternalState
import|;
end_import

begin_comment
comment|/**  * Unit tests for VectorDeserializeOrcWriter.  */
end_comment

begin_class
specifier|public
class|class
name|TestVectorDeserializeOrcWriter
block|{
specifier|private
specifier|static
specifier|final
name|int
name|TEST_NUM_COLS
init|=
literal|2
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testConcurrencyIssueWhileWriting
parameter_list|()
throws|throws
name|Exception
block|{
comment|//Setup////////////////////////////////////////////////////////////////////////////////////////
name|EncodedDataConsumer
name|consumer
init|=
name|createBlankEncodedDataConsumer
argument_list|()
decl_stmt|;
name|FixedSizedObjectPool
argument_list|<
name|ColumnVectorBatch
argument_list|>
name|cvbPool
init|=
operator|(
name|FixedSizedObjectPool
argument_list|<
name|ColumnVectorBatch
argument_list|>
operator|)
name|getInternalState
argument_list|(
name|consumer
argument_list|,
literal|"cvbPool"
argument_list|)
decl_stmt|;
name|ColumnVectorBatch
name|cvb
init|=
operator|new
name|ColumnVectorBatch
argument_list|(
name|TEST_NUM_COLS
argument_list|)
decl_stmt|;
name|VectorizedRowBatch
name|vrb
init|=
operator|new
name|VectorizedRowBatch
argument_list|(
name|TEST_NUM_COLS
argument_list|)
decl_stmt|;
name|createTestVectors
argument_list|(
name|cvb
argument_list|,
name|vrb
argument_list|)
expr_stmt|;
name|Queue
argument_list|<
name|VectorDeserializeOrcWriter
operator|.
name|WriteOperation
argument_list|>
name|writeOpQueue
init|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<>
argument_list|()
decl_stmt|;
name|VectorDeserializeOrcWriter
name|orcWriter
init|=
name|createOrcWriter
argument_list|(
name|writeOpQueue
argument_list|,
name|vrb
argument_list|)
decl_stmt|;
comment|//Simulating unfortunate order of events///////////////////////////////////////////////////////
comment|//Add CVs to writer -> should increase their refcount
comment|//Happens when IO thread has generated a vector batch and hands it over to async ORC thread
name|orcWriter
operator|.
name|addBatchToWriter
argument_list|()
expr_stmt|;
comment|//Return CVs to pool -> should check their refcount, and as they're 1, this should be a no-op
comment|//Happens when LLAPRecordReader on Tez thread received and used the batch and now wants to
comment|// return it for CVB recycling
name|consumer
operator|.
name|returnData
argument_list|(
name|cvb
argument_list|)
expr_stmt|;
comment|//Do the write -> should decrease the refcount of CVs
comment|//Happens when ORC thread gets to writing and hands the vectors of the batch over to ORC
comment|// WriterImpl for encoding and cache storage
name|writeOpQueue
operator|.
name|poll
argument_list|()
operator|.
name|apply
argument_list|(
name|mock
argument_list|(
name|WriterImpl
operator|.
name|class
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|//Verifications////////////////////////////////////////////////////////////////////////////////
comment|//Pool should be empty as the CVB return should have been a no-op, so this call should create a
comment|// NEW instance of CVBs
name|ColumnVectorBatch
name|newCvb
init|=
name|cvbPool
operator|.
name|take
argument_list|()
decl_stmt|;
name|assertNotEquals
argument_list|(
name|newCvb
argument_list|,
name|cvb
argument_list|)
expr_stmt|;
comment|//Simulating a 'clean' CVB return -> the CVB now does have to make its way back to the pool
name|consumer
operator|.
name|returnData
argument_list|(
name|cvb
argument_list|)
expr_stmt|;
name|newCvb
operator|=
name|cvbPool
operator|.
name|take
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|newCvb
argument_list|,
name|cvb
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|createTestVectors
parameter_list|(
name|ColumnVectorBatch
name|cvb
parameter_list|,
name|VectorizedRowBatch
name|vrb
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|TEST_NUM_COLS
condition|;
operator|++
name|i
control|)
block|{
name|LongColumnVector
name|cv
init|=
operator|new
name|LongColumnVector
argument_list|()
decl_stmt|;
name|cv
operator|.
name|fill
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|cvb
operator|.
name|cols
index|[
name|i
index|]
operator|=
name|cv
expr_stmt|;
name|vrb
operator|.
name|cols
index|[
name|i
index|]
operator|=
name|cv
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|VectorDeserializeOrcWriter
name|createOrcWriter
parameter_list|(
name|Queue
argument_list|<
name|VectorDeserializeOrcWriter
operator|.
name|WriteOperation
argument_list|>
name|writeOpQueue
parameter_list|,
name|VectorizedRowBatch
name|vrb
parameter_list|)
block|{
name|VectorDeserializeOrcWriter
name|orcWriter
init|=
name|mock
argument_list|(
name|VectorDeserializeOrcWriter
operator|.
name|class
argument_list|,
name|withSettings
argument_list|()
operator|.
name|defaultAnswer
argument_list|(
name|CALLS_REAL_METHODS
argument_list|)
argument_list|)
decl_stmt|;
name|setInternalState
argument_list|(
name|orcWriter
argument_list|,
literal|"sourceBatch"
argument_list|,
name|vrb
argument_list|)
expr_stmt|;
name|setInternalState
argument_list|(
name|orcWriter
argument_list|,
literal|"destinationBatch"
argument_list|,
name|vrb
argument_list|)
expr_stmt|;
name|setInternalState
argument_list|(
name|orcWriter
argument_list|,
literal|"currentBatches"
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|VectorizedRowBatch
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|setInternalState
argument_list|(
name|orcWriter
argument_list|,
literal|"queue"
argument_list|,
name|writeOpQueue
argument_list|)
expr_stmt|;
name|setInternalState
argument_list|(
name|orcWriter
argument_list|,
literal|"isAsync"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
name|orcWriter
return|;
block|}
specifier|private
specifier|static
name|EncodedDataConsumer
name|createBlankEncodedDataConsumer
parameter_list|()
block|{
return|return
operator|new
name|EncodedDataConsumer
argument_list|(
literal|null
argument_list|,
literal|1
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|decodeBatch
parameter_list|(
name|EncodedColumnBatch
name|batch
parameter_list|,
name|Consumer
name|downstreamConsumer
parameter_list|)
throws|throws
name|InterruptedException
block|{       }
annotation|@
name|Override
specifier|public
name|SchemaEvolution
name|getSchemaEvolution
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|consumeData
parameter_list|(
name|EncodedColumnBatch
name|data
parameter_list|)
throws|throws
name|InterruptedException
block|{       }
block|}
return|;
block|}
block|}
end_class

end_unit

