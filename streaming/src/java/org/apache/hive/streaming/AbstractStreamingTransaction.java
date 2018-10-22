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
name|hive
operator|.
name|streaming
package|;
end_package

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
name|metastore
operator|.
name|api
operator|.
name|TxnToWriteId
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
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
name|Set
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
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_comment
comment|/**  * Common methods for the implementing classes.  */
end_comment

begin_class
specifier|abstract
class|class
name|AbstractStreamingTransaction
implements|implements
name|StreamingTransaction
block|{
comment|/**    * This variable should be initialized by the children.    */
specifier|protected
name|RecordWriter
name|recordWriter
decl_stmt|;
comment|/**    * This variable should be initialized by the children.    */
specifier|protected
name|List
argument_list|<
name|TxnToWriteId
argument_list|>
name|txnToWriteIds
decl_stmt|;
comment|/**    * once any operation on this batch encounters a system exception    * (e.g. IOException on write) it's safest to assume that we can't write to the    * file backing this batch any more.  This guards important public methods    */
specifier|protected
specifier|final
name|AtomicBoolean
name|isTxnClosed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|protected
name|int
name|currentTxnIndex
init|=
operator|-
literal|1
decl_stmt|;
specifier|protected
name|HiveStreamingConnection
operator|.
name|TxnState
name|state
decl_stmt|;
specifier|protected
name|void
name|checkIsClosed
parameter_list|()
throws|throws
name|StreamingException
block|{
if|if
condition|(
name|isTxnClosed
operator|.
name|get
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|StreamingException
argument_list|(
literal|"Transaction"
operator|+
name|toString
argument_list|()
operator|+
literal|" is closed()"
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|void
name|beginNextTransactionImpl
parameter_list|(
name|String
name|errorMessage
parameter_list|)
throws|throws
name|StreamingException
block|{
name|state
operator|=
name|HiveStreamingConnection
operator|.
name|TxnState
operator|.
name|INACTIVE
expr_stmt|;
comment|//clear state from previous txn
if|if
condition|(
operator|(
name|currentTxnIndex
operator|+
literal|1
operator|)
operator|>=
name|txnToWriteIds
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|InvalidTransactionState
argument_list|(
name|errorMessage
argument_list|)
throw|;
block|}
name|currentTxnIndex
operator|++
expr_stmt|;
name|state
operator|=
name|HiveStreamingConnection
operator|.
name|TxnState
operator|.
name|OPEN
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
specifier|final
name|byte
index|[]
name|record
parameter_list|)
throws|throws
name|StreamingException
block|{
name|checkIsClosed
argument_list|()
expr_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|recordWriter
operator|.
name|write
argument_list|(
name|getCurrentWriteId
argument_list|()
argument_list|,
name|record
argument_list|)
expr_stmt|;
name|success
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerializationError
name|ex
parameter_list|)
block|{
comment|//this exception indicates that a {@code record} could not be parsed and the
comment|//caller can decide whether to drop it or send it to dead letter queue.
comment|//rolling back the txn and retrying won't help since the tuple will be exactly the same
comment|//when it's replayed.
name|success
operator|=
literal|true
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
finally|finally
block|{
name|markDead
argument_list|(
name|success
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|write
parameter_list|(
specifier|final
name|InputStream
name|inputStream
parameter_list|)
throws|throws
name|StreamingException
block|{
name|checkIsClosed
argument_list|()
expr_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|recordWriter
operator|.
name|write
argument_list|(
name|getCurrentWriteId
argument_list|()
argument_list|,
name|inputStream
argument_list|)
expr_stmt|;
name|success
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerializationError
name|ex
parameter_list|)
block|{
comment|//this exception indicates that a {@code record} could not be parsed and the
comment|//caller can decide whether to drop it or send it to dead letter queue.
comment|//rolling back the txn and retrying won'table help since the tuple will be exactly the same
comment|//when it's replayed.
name|success
operator|=
literal|true
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
finally|finally
block|{
name|markDead
argument_list|(
name|success
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * A transaction batch opens a single HDFS file and writes multiple transaction to it.  If there is any issue    * with the write, we can't continue to write to the same file any as it may be corrupted now (at the tail).    * This ensures that a client can't ignore these failures and continue to write.    */
specifier|protected
name|void
name|markDead
parameter_list|(
name|boolean
name|success
parameter_list|)
throws|throws
name|StreamingException
block|{
if|if
condition|(
name|success
condition|)
block|{
return|return;
block|}
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|long
name|getCurrentWriteId
parameter_list|()
block|{
if|if
condition|(
name|currentTxnIndex
operator|>=
literal|0
condition|)
block|{
return|return
name|txnToWriteIds
operator|.
name|get
argument_list|(
name|currentTxnIndex
argument_list|)
operator|.
name|getWriteId
argument_list|()
return|;
block|}
return|return
operator|-
literal|1L
return|;
block|}
specifier|public
name|int
name|remainingTransactions
parameter_list|()
block|{
if|if
condition|(
name|currentTxnIndex
operator|>=
literal|0
condition|)
block|{
return|return
name|txnToWriteIds
operator|.
name|size
argument_list|()
operator|-
name|currentTxnIndex
operator|-
literal|1
return|;
block|}
return|return
name|txnToWriteIds
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isClosed
parameter_list|()
block|{
return|return
name|isTxnClosed
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|HiveStreamingConnection
operator|.
name|TxnState
name|getCurrentTransactionState
parameter_list|()
block|{
return|return
name|state
return|;
block|}
specifier|public
name|long
name|getCurrentTxnId
parameter_list|()
block|{
if|if
condition|(
name|currentTxnIndex
operator|>=
literal|0
condition|)
block|{
return|return
name|txnToWriteIds
operator|.
name|get
argument_list|(
name|currentTxnIndex
argument_list|)
operator|.
name|getTxnId
argument_list|()
return|;
block|}
return|return
operator|-
literal|1L
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|TxnToWriteId
argument_list|>
name|getTxnToWriteIds
parameter_list|()
block|{
return|return
name|txnToWriteIds
return|;
block|}
specifier|public
name|void
name|commit
parameter_list|()
throws|throws
name|StreamingException
block|{
name|commit
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|commit
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|partitions
parameter_list|)
throws|throws
name|StreamingException
block|{
name|commit
argument_list|(
name|partitions
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

