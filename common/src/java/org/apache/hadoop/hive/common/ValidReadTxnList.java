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
name|common
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
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|BitSet
import|;
end_import

begin_comment
comment|/**  * An implementation of {@link org.apache.hadoop.hive.common.ValidTxnList} for use by readers.  * This class will view a transaction as valid only if it is committed.  Both open and aborted  * transactions will be seen as invalid.  */
end_comment

begin_class
specifier|public
class|class
name|ValidReadTxnList
implements|implements
name|ValidTxnList
block|{
specifier|protected
name|long
index|[]
name|exceptions
decl_stmt|;
specifier|protected
name|BitSet
name|abortedBits
decl_stmt|;
comment|// BitSet for flagging aborted transactions. Bit is true if aborted, false if open
comment|//default value means there are no open txn in the snapshot
specifier|private
name|long
name|minOpenTxn
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|protected
name|long
name|highWatermark
decl_stmt|;
specifier|public
name|ValidReadTxnList
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|long
index|[
literal|0
index|]
argument_list|,
operator|new
name|BitSet
argument_list|()
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Used if there are no open transactions in the snapshot    */
specifier|public
name|ValidReadTxnList
parameter_list|(
name|long
index|[]
name|exceptions
parameter_list|,
name|BitSet
name|abortedBits
parameter_list|,
name|long
name|highWatermark
parameter_list|)
block|{
name|this
argument_list|(
name|exceptions
argument_list|,
name|abortedBits
argument_list|,
name|highWatermark
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ValidReadTxnList
parameter_list|(
name|long
index|[]
name|exceptions
parameter_list|,
name|BitSet
name|abortedBits
parameter_list|,
name|long
name|highWatermark
parameter_list|,
name|long
name|minOpenTxn
parameter_list|)
block|{
if|if
condition|(
name|exceptions
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|minOpenTxn
operator|=
name|minOpenTxn
expr_stmt|;
block|}
name|this
operator|.
name|exceptions
operator|=
name|exceptions
expr_stmt|;
name|this
operator|.
name|abortedBits
operator|=
name|abortedBits
expr_stmt|;
name|this
operator|.
name|highWatermark
operator|=
name|highWatermark
expr_stmt|;
block|}
specifier|public
name|ValidReadTxnList
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|readFromString
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isTxnValid
parameter_list|(
name|long
name|txnid
parameter_list|)
block|{
if|if
condition|(
name|highWatermark
operator|<
name|txnid
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|Arrays
operator|.
name|binarySearch
argument_list|(
name|exceptions
argument_list|,
name|txnid
argument_list|)
operator|<
literal|0
return|;
block|}
comment|/**    * We cannot use a base file if its range contains an open txn.    * @param txnid from base_xxxx    */
annotation|@
name|Override
specifier|public
name|boolean
name|isValidBase
parameter_list|(
name|long
name|txnid
parameter_list|)
block|{
return|return
name|minOpenTxn
operator|>
name|txnid
operator|&&
name|txnid
operator|<=
name|highWatermark
return|;
block|}
annotation|@
name|Override
specifier|public
name|RangeResponse
name|isTxnRangeValid
parameter_list|(
name|long
name|minTxnId
parameter_list|,
name|long
name|maxTxnId
parameter_list|)
block|{
comment|// check the easy cases first
if|if
condition|(
name|highWatermark
operator|<
name|minTxnId
condition|)
block|{
return|return
name|RangeResponse
operator|.
name|NONE
return|;
block|}
elseif|else
if|if
condition|(
name|exceptions
operator|.
name|length
operator|>
literal|0
operator|&&
name|exceptions
index|[
literal|0
index|]
operator|>
name|maxTxnId
condition|)
block|{
return|return
name|RangeResponse
operator|.
name|ALL
return|;
block|}
comment|// since the exceptions and the range in question overlap, count the
comment|// exceptions in the range
name|long
name|count
init|=
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|maxTxnId
operator|-
name|highWatermark
argument_list|)
decl_stmt|;
for|for
control|(
name|long
name|txn
range|:
name|exceptions
control|)
block|{
if|if
condition|(
name|minTxnId
operator|<=
name|txn
operator|&&
name|txn
operator|<=
name|maxTxnId
condition|)
block|{
name|count
operator|+=
literal|1
expr_stmt|;
block|}
block|}
if|if
condition|(
name|count
operator|==
literal|0
condition|)
block|{
return|return
name|RangeResponse
operator|.
name|ALL
return|;
block|}
elseif|else
if|if
condition|(
name|count
operator|==
operator|(
name|maxTxnId
operator|-
name|minTxnId
operator|+
literal|1
operator|)
condition|)
block|{
return|return
name|RangeResponse
operator|.
name|NONE
return|;
block|}
else|else
block|{
return|return
name|RangeResponse
operator|.
name|SOME
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|writeToString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|writeToString
parameter_list|()
block|{
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|highWatermark
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|minOpenTxn
argument_list|)
expr_stmt|;
if|if
condition|(
name|exceptions
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
comment|// separator for open txns
name|buf
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
comment|// separator for aborted txns
block|}
else|else
block|{
name|StringBuilder
name|open
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|StringBuilder
name|abort
init|=
operator|new
name|StringBuilder
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
name|exceptions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|abortedBits
operator|.
name|get
argument_list|(
name|i
argument_list|)
condition|)
block|{
if|if
condition|(
name|abort
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|abort
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|abort
operator|.
name|append
argument_list|(
name|exceptions
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|open
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|open
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|open
operator|.
name|append
argument_list|(
name|exceptions
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|buf
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|open
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
return|return
name|buf
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFromString
parameter_list|(
name|String
name|src
parameter_list|)
block|{
if|if
condition|(
name|src
operator|==
literal|null
operator|||
name|src
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|highWatermark
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
name|exceptions
operator|=
operator|new
name|long
index|[
literal|0
index|]
expr_stmt|;
name|abortedBits
operator|=
operator|new
name|BitSet
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|String
index|[]
name|values
init|=
name|src
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
name|highWatermark
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|values
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|minOpenTxn
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|values
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|String
index|[]
name|openTxns
init|=
operator|new
name|String
index|[
literal|0
index|]
decl_stmt|;
name|String
index|[]
name|abortedTxns
init|=
operator|new
name|String
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|values
operator|.
name|length
operator|<
literal|3
condition|)
block|{
name|openTxns
operator|=
operator|new
name|String
index|[
literal|0
index|]
expr_stmt|;
name|abortedTxns
operator|=
operator|new
name|String
index|[
literal|0
index|]
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|values
operator|.
name|length
operator|==
literal|3
condition|)
block|{
if|if
condition|(
operator|!
name|values
index|[
literal|2
index|]
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|openTxns
operator|=
name|values
index|[
literal|2
index|]
operator|.
name|split
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|values
index|[
literal|2
index|]
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|openTxns
operator|=
name|values
index|[
literal|2
index|]
operator|.
name|split
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|values
index|[
literal|3
index|]
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|abortedTxns
operator|=
name|values
index|[
literal|3
index|]
operator|.
name|split
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
block|}
name|exceptions
operator|=
operator|new
name|long
index|[
name|openTxns
operator|.
name|length
operator|+
name|abortedTxns
operator|.
name|length
index|]
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|open
range|:
name|openTxns
control|)
block|{
name|exceptions
index|[
name|i
operator|++
index|]
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|open
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|abort
range|:
name|abortedTxns
control|)
block|{
name|exceptions
index|[
name|i
operator|++
index|]
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
name|Arrays
operator|.
name|sort
argument_list|(
name|exceptions
argument_list|)
expr_stmt|;
name|abortedBits
operator|=
operator|new
name|BitSet
argument_list|(
name|exceptions
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|abort
range|:
name|abortedTxns
control|)
block|{
name|int
name|index
init|=
name|Arrays
operator|.
name|binarySearch
argument_list|(
name|exceptions
argument_list|,
name|Long
operator|.
name|parseLong
argument_list|(
name|abort
argument_list|)
argument_list|)
decl_stmt|;
name|abortedBits
operator|.
name|set
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getHighWatermark
parameter_list|()
block|{
return|return
name|highWatermark
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
index|[]
name|getInvalidTransactions
parameter_list|()
block|{
return|return
name|exceptions
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|getMinOpenTxn
parameter_list|()
block|{
return|return
name|minOpenTxn
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|?
literal|null
else|:
name|minOpenTxn
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isTxnAborted
parameter_list|(
name|long
name|txnid
parameter_list|)
block|{
name|int
name|index
init|=
name|Arrays
operator|.
name|binarySearch
argument_list|(
name|exceptions
argument_list|,
name|txnid
argument_list|)
decl_stmt|;
return|return
name|index
operator|>=
literal|0
operator|&&
name|abortedBits
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RangeResponse
name|isTxnRangeAborted
parameter_list|(
name|long
name|minTxnId
parameter_list|,
name|long
name|maxTxnId
parameter_list|)
block|{
comment|// check the easy cases first
if|if
condition|(
name|highWatermark
operator|<
name|minTxnId
condition|)
block|{
return|return
name|RangeResponse
operator|.
name|NONE
return|;
block|}
name|int
name|count
init|=
literal|0
decl_stmt|;
comment|// number of aborted txns found in exceptions
comment|// traverse the aborted txns list, starting at first aborted txn index
for|for
control|(
name|int
name|i
init|=
name|abortedBits
operator|.
name|nextSetBit
argument_list|(
literal|0
argument_list|)
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|=
name|abortedBits
operator|.
name|nextSetBit
argument_list|(
name|i
operator|+
literal|1
argument_list|)
control|)
block|{
name|long
name|abortedTxnId
init|=
name|exceptions
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|abortedTxnId
operator|>
name|maxTxnId
condition|)
block|{
comment|// we've already gone beyond the specified range
break|break;
block|}
if|if
condition|(
name|abortedTxnId
operator|>=
name|minTxnId
operator|&&
name|abortedTxnId
operator|<=
name|maxTxnId
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|count
operator|==
literal|0
condition|)
block|{
return|return
name|RangeResponse
operator|.
name|NONE
return|;
block|}
elseif|else
if|if
condition|(
name|count
operator|==
operator|(
name|maxTxnId
operator|-
name|minTxnId
operator|+
literal|1
operator|)
condition|)
block|{
return|return
name|RangeResponse
operator|.
name|ALL
return|;
block|}
else|else
block|{
return|return
name|RangeResponse
operator|.
name|SOME
return|;
block|}
block|}
block|}
end_class

end_unit

