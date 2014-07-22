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

begin_comment
comment|/**  * Models the list of transactions that should be included in a snapshot.  * It is modelled as a high water mark, which is the largest transaction id that  * has been committed and a list of transactions that are not included.  */
end_comment

begin_interface
specifier|public
interface|interface
name|ValidTxnList
block|{
comment|/**    * Key used to store valid txn list in a    * {@link org.apache.hadoop.conf.Configuration} object.    */
specifier|public
specifier|static
specifier|final
name|String
name|VALID_TXNS_KEY
init|=
literal|"hive.txn.valid.txns"
decl_stmt|;
comment|/**    * The response to a range query.  NONE means no values in this range match,    * SOME mean that some do, and ALL means that every value does.    */
specifier|public
enum|enum
name|RangeResponse
block|{
name|NONE
block|,
name|SOME
block|,
name|ALL
block|}
empty_stmt|;
comment|/**    * Indicates whether a given transaction has been committed and should be    * viewed as valid for read.    * @param txnid id for the transaction    * @return true if committed, false otherwise    */
specifier|public
name|boolean
name|isTxnCommitted
parameter_list|(
name|long
name|txnid
parameter_list|)
function_decl|;
comment|/**    * Find out if a range of transaction ids have been committed.    * @param minTxnId minimum txnid to look for, inclusive    * @param maxTxnId maximum txnid to look for, inclusive    * @return Indicate whether none, some, or all of these transactions have been    * committed.    */
specifier|public
name|RangeResponse
name|isTxnRangeCommitted
parameter_list|(
name|long
name|minTxnId
parameter_list|,
name|long
name|maxTxnId
parameter_list|)
function_decl|;
comment|/**    * Write this validTxnList into a string. This should produce a string that    * can be used by {@link #readFromString(String)} to populate a validTxnsList.    */
specifier|public
name|String
name|writeToString
parameter_list|()
function_decl|;
comment|/**    * Populate this validTxnList from the string.  It is assumed that the string    * was created via {@link #writeToString()}.    * @param src source string.    */
specifier|public
name|void
name|readFromString
parameter_list|(
name|String
name|src
parameter_list|)
function_decl|;
comment|/**    * Get the largest committed transaction id.    * @return largest committed transaction id    */
specifier|public
name|long
name|getHighWatermark
parameter_list|()
function_decl|;
comment|/**    * Get the list of transactions under the high water mark that are still    * open.    * @return a list of open transaction ids    */
specifier|public
name|long
index|[]
name|getOpenTransactions
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

