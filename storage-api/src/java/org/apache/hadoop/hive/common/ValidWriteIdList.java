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
name|common
package|;
end_package

begin_comment
comment|/**  * Models the list of transactions that should be included in a snapshot.  * It is modelled as a high water mark, which is the largest write id that  * has been committed and a list of write ids that are not included.  */
end_comment

begin_interface
specifier|public
interface|interface
name|ValidWriteIdList
block|{
comment|/**    * Key used to store valid write id list in a    * {@link org.apache.hadoop.conf.Configuration} object.    */
name|String
name|VALID_WRITEIDS_KEY
init|=
literal|"hive.txn.valid.writeids"
decl_stmt|;
comment|/**    * The response to a range query.  NONE means no values in this range match,    * SOME mean that some do, and ALL means that every value does.    */
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
comment|/**    * Indicates whether a given write ID is valid. Note that valid may have different meanings    * for different implementations, as some will only want to see committed transactions and some    * both committed and aborted.    * @param writeId write ID of the table    * @return true if valid, false otherwise    */
name|boolean
name|isWriteIdValid
parameter_list|(
name|long
name|writeId
parameter_list|)
function_decl|;
comment|/**    * Returns {@code true} if such base file can be used to materialize the snapshot represented by    * this {@code ValidWriteIdList}.    * @param writeId highest write ID in a given base_xxxx file    * @return true if the base file can be used    */
name|boolean
name|isValidBase
parameter_list|(
name|long
name|writeId
parameter_list|)
function_decl|;
comment|/**    * Find out if a range of write ids are valid.  Note that valid may have different meanings    * for different implementations, as some will only want to see committed transactions and some    * both committed and aborted.    * @param minWriteId minimum write ID to look for, inclusive    * @param maxWriteId maximum write ID to look for, inclusive    * @return Indicate whether none, some, or all of these transactions are valid.    */
name|RangeResponse
name|isWriteIdRangeValid
parameter_list|(
name|long
name|minWriteId
parameter_list|,
name|long
name|maxWriteId
parameter_list|)
function_decl|;
comment|/**    * Write this ValidWriteIdList into a string. This should produce a string that    * can be used by {@link #readFromString(String)} to populate a ValidWriteIdList.    * @return the list as a string    */
name|String
name|writeToString
parameter_list|()
function_decl|;
comment|/**    * Populate this ValidWriteIdList from the string.  It is assumed that the string    * was created via {@link #writeToString()} and the exceptions list is sorted.    * @param src source string.    */
name|void
name|readFromString
parameter_list|(
name|String
name|src
parameter_list|)
function_decl|;
comment|/**    * Get the table for which the ValidWriteIdList is formed    * @return table name (&lt;db_name&gt;.&lt;table_name&gt;) associated with ValidWriteIdList.    */
name|String
name|getTableName
parameter_list|()
function_decl|;
comment|/**    * Get the largest write id used.    * @return largest write id used    */
name|long
name|getHighWatermark
parameter_list|()
function_decl|;
comment|/**    * Get the list of write ids under the high water mark that are not valid.  Note that invalid    * may have different meanings for different implementations, as some will only want to see open    * transactions and some both open and aborted.    * @return a list of invalid write ids    */
name|long
index|[]
name|getInvalidWriteIds
parameter_list|()
function_decl|;
comment|/**    * Indicates whether a given write maps to aborted transaction.    * @param writeId write id to be validated    * @return true if aborted, false otherwise    */
name|boolean
name|isWriteIdAborted
parameter_list|(
name|long
name|writeId
parameter_list|)
function_decl|;
comment|/**    * Find out if a range of write ids are aborted.    * @param minWriteId minimum write Id to look for, inclusive    * @param maxWriteId maximum write Id  to look for, inclusive    * @return Indicate whether none, some, or all of these write ids are aborted.    */
name|RangeResponse
name|isWriteIdRangeAborted
parameter_list|(
name|long
name|minWriteId
parameter_list|,
name|long
name|maxWriteId
parameter_list|)
function_decl|;
comment|/**    * The smallest open write id.    * @return smallest Open write Id in this set, {@code null} if there is none.    */
name|Long
name|getMinOpenWriteId
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

