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
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|keyseries
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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

begin_comment
comment|/**  * An abstraction of keys within a VectorizedRowBatch.  *  * A key is one or more columns.  *  * When there is a sequential "run" of equal keys, they are collapsed and represented by a  * duplicate count.  *  * The batch of keys (with sequential duplicates collapsed) is called a series.  *  * A key can be all null, or a key with no or some nulls.  *  * All keys have a duplicate count.  *  * A key with no or some nulls has:  *   1) A hash code.  *   2) Key values and other value(s) defined by other interfaces.  *  * The key series is logically indexed.  That is, if batch.selectedInUse is true, the indices  * will be logical and need to be mapped through batch.selected to get the physical batch  * indices.  Otherwise, the indices are physical batch indices.  */
end_comment

begin_interface
specifier|public
interface|interface
name|VectorKeySeries
block|{
comment|/**    * Process a non-empty batch of rows and compute a key series.    *    * The key series will be positioned to the beginning.    *    * @param batch    * @throws IOException    */
name|void
name|processBatch
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Position to the beginning of the key series.    */
name|void
name|positionToFirst
parameter_list|()
function_decl|;
comment|/**    * @return the current logical index of the first row of the current key.    * The next duplicate count rows have the same key.    */
name|int
name|getCurrentLogical
parameter_list|()
function_decl|;
comment|/**    * @return true when the current key is all nulls.    */
name|boolean
name|getCurrentIsAllNull
parameter_list|()
function_decl|;
comment|/**    * @return the number of duplicate keys of the current key.    */
name|int
name|getCurrentDuplicateCount
parameter_list|()
function_decl|;
comment|/**    * @return true when there is at least one null in the current key.    * Only valid when getCurrentIsAllNull is false.  Otherwise, undefined.    */
name|boolean
name|getCurrentHasAnyNulls
parameter_list|()
function_decl|;
comment|/**    * @return the hash code of the current key.    * Only valid when getCurrentIsAllNull is false.  Otherwise, undefined.    */
name|int
name|getCurrentHashCode
parameter_list|()
function_decl|;
comment|/**    * Move to the next key.    * @return true when there is another key.  Otherwise, the key series is complete.    */
name|boolean
name|next
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

