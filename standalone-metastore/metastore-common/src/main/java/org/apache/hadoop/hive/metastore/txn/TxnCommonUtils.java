begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
operator|.
name|txn
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
name|common
operator|.
name|ValidReadTxnList
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
name|ValidReaderWriteIdList
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
name|ValidTxnList
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
name|ValidTxnWriteIdList
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
name|metastore
operator|.
name|api
operator|.
name|GetOpenTxnsResponse
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
name|metastore
operator|.
name|api
operator|.
name|TableValidWriteIds
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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

begin_class
specifier|public
class|class
name|TxnCommonUtils
block|{
comment|/**    * Transform a {@link org.apache.hadoop.hive.metastore.api.GetOpenTxnsResponse} to a    * {@link org.apache.hadoop.hive.common.ValidTxnList}.  This assumes that the caller intends to    * read the files, and thus treats both open and aborted transactions as invalid.    * @param txns txn list from the metastore    * @param currentTxn Current transaction that the user has open.  If this is greater than 0 it    *                   will be removed from the exceptions list so that the user sees his own    *                   transaction as valid.    * @return a valid txn list.    */
specifier|public
specifier|static
name|ValidTxnList
name|createValidReadTxnList
parameter_list|(
name|GetOpenTxnsResponse
name|txns
parameter_list|,
name|long
name|currentTxn
parameter_list|)
block|{
assert|assert
name|currentTxn
operator|<=
name|txns
operator|.
name|getTxn_high_water_mark
argument_list|()
assert|;
comment|/*      * The highWaterMark should be min(currentTxn,txns.getTxn_high_water_mark()) assuming currentTxn>0      * otherwise if currentTxn=7 and 8 commits before 7, then 7 will see result of 8 which      * doesn't make sense for Snapshot Isolation. Of course for Read Committed, the list should      * include the latest committed set.      */
name|long
name|highWaterMark
init|=
operator|(
name|currentTxn
operator|>
literal|0
operator|)
condition|?
name|Math
operator|.
name|min
argument_list|(
name|currentTxn
argument_list|,
name|txns
operator|.
name|getTxn_high_water_mark
argument_list|()
argument_list|)
else|:
name|txns
operator|.
name|getTxn_high_water_mark
argument_list|()
decl_stmt|;
comment|// Open txns are already sorted in ascending order. This list may or may not include HWM
comment|// but it is guaranteed that list won't have txn> HWM. But, if we overwrite the HWM with currentTxn
comment|// then need to truncate the exceptions list accordingly.
name|List
argument_list|<
name|Long
argument_list|>
name|openTxns
init|=
name|txns
operator|.
name|getOpen_txns
argument_list|()
decl_stmt|;
comment|// We care only about open/aborted txns below currentTxn and hence the size should be determined
comment|// for the exceptions list. The currentTxn will be missing in openTxns list only in rare case like
comment|// txn is aborted by AcidHouseKeeperService and compactor actually cleans up the aborted txns.
comment|// So, for such cases, we get negative value for sizeToHwm with found position for currentTxn, and so,
comment|// we just negate it to get the size.
name|int
name|sizeToHwm
init|=
operator|(
name|currentTxn
operator|>
literal|0
operator|)
condition|?
name|Collections
operator|.
name|binarySearch
argument_list|(
name|openTxns
argument_list|,
name|currentTxn
argument_list|)
else|:
name|openTxns
operator|.
name|size
argument_list|()
decl_stmt|;
name|sizeToHwm
operator|=
operator|(
name|sizeToHwm
operator|<
literal|0
operator|)
condition|?
operator|(
operator|-
name|sizeToHwm
operator|)
else|:
name|sizeToHwm
expr_stmt|;
name|long
index|[]
name|exceptions
init|=
operator|new
name|long
index|[
name|sizeToHwm
index|]
decl_stmt|;
name|BitSet
name|inAbortedBits
init|=
name|BitSet
operator|.
name|valueOf
argument_list|(
name|txns
operator|.
name|getAbortedBits
argument_list|()
argument_list|)
decl_stmt|;
name|BitSet
name|outAbortedBits
init|=
operator|new
name|BitSet
argument_list|()
decl_stmt|;
name|long
name|minOpenTxnId
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|long
name|txn
range|:
name|openTxns
control|)
block|{
comment|// For snapshot isolation, we don't care about txns greater than current txn and so stop here.
comment|// Also, we need not include current txn to exceptions list.
if|if
condition|(
operator|(
name|currentTxn
operator|>
literal|0
operator|)
operator|&&
operator|(
name|txn
operator|>=
name|currentTxn
operator|)
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|inAbortedBits
operator|.
name|get
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|outAbortedBits
operator|.
name|set
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|minOpenTxnId
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
name|minOpenTxnId
operator|=
name|txn
expr_stmt|;
block|}
name|exceptions
index|[
name|i
operator|++
index|]
operator|=
name|txn
expr_stmt|;
block|}
return|return
operator|new
name|ValidReadTxnList
argument_list|(
name|exceptions
argument_list|,
name|outAbortedBits
argument_list|,
name|highWaterMark
argument_list|,
name|minOpenTxnId
argument_list|)
return|;
block|}
comment|/**    * Transform a {@link org.apache.hadoop.hive.metastore.api.GetValidWriteIdsResponse} to a    * {@link org.apache.hadoop.hive.common.ValidTxnWriteIdList}.  This assumes that the caller intends to    * read the files, and thus treats both open and aborted transactions as invalid.    * @param currentTxnId current txn ID for which we get the valid write ids list    * @param validIds valid write ids list from the metastore    * @return a valid write IDs list for the whole transaction.    */
specifier|public
specifier|static
name|ValidTxnWriteIdList
name|createValidTxnWriteIdList
parameter_list|(
name|Long
name|currentTxnId
parameter_list|,
name|List
argument_list|<
name|TableValidWriteIds
argument_list|>
name|validIds
parameter_list|)
block|{
name|ValidTxnWriteIdList
name|validTxnWriteIdList
init|=
operator|new
name|ValidTxnWriteIdList
argument_list|(
name|currentTxnId
argument_list|)
decl_stmt|;
for|for
control|(
name|TableValidWriteIds
name|tableWriteIds
range|:
name|validIds
control|)
block|{
name|validTxnWriteIdList
operator|.
name|addTableValidWriteIdList
argument_list|(
name|createValidReaderWriteIdList
argument_list|(
name|tableWriteIds
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|validTxnWriteIdList
return|;
block|}
comment|/**    * Transform a {@link TableValidWriteIds} to a    * {@link org.apache.hadoop.hive.common.ValidReaderWriteIdList}.  This assumes that the caller intends to    * read the files, and thus treats both open and aborted write ids as invalid.    * @param tableWriteIds valid write ids for the given table from the metastore    * @return a valid write IDs list for the input table    */
specifier|public
specifier|static
name|ValidReaderWriteIdList
name|createValidReaderWriteIdList
parameter_list|(
name|TableValidWriteIds
name|tableWriteIds
parameter_list|)
block|{
name|String
name|fullTableName
init|=
name|tableWriteIds
operator|.
name|getFullTableName
argument_list|()
decl_stmt|;
name|long
name|highWater
init|=
name|tableWriteIds
operator|.
name|getWriteIdHighWaterMark
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|invalids
init|=
name|tableWriteIds
operator|.
name|getInvalidWriteIds
argument_list|()
decl_stmt|;
name|BitSet
name|abortedBits
init|=
name|BitSet
operator|.
name|valueOf
argument_list|(
name|tableWriteIds
operator|.
name|getAbortedBits
argument_list|()
argument_list|)
decl_stmt|;
name|long
index|[]
name|exceptions
init|=
operator|new
name|long
index|[
name|invalids
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|long
name|writeId
range|:
name|invalids
control|)
block|{
name|exceptions
index|[
name|i
operator|++
index|]
operator|=
name|writeId
expr_stmt|;
block|}
if|if
condition|(
name|tableWriteIds
operator|.
name|isSetMinOpenWriteId
argument_list|()
condition|)
block|{
return|return
operator|new
name|ValidReaderWriteIdList
argument_list|(
name|fullTableName
argument_list|,
name|exceptions
argument_list|,
name|abortedBits
argument_list|,
name|highWater
argument_list|,
name|tableWriteIds
operator|.
name|getMinOpenWriteId
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|ValidReaderWriteIdList
argument_list|(
name|fullTableName
argument_list|,
name|exceptions
argument_list|,
name|abortedBits
argument_list|,
name|highWater
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

