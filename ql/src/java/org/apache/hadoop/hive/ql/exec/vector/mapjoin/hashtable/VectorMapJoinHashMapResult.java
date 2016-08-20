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
name|mapjoin
operator|.
name|hashtable
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
name|serde2
operator|.
name|WriteBuffers
operator|.
name|ByteSegmentRef
import|;
end_import

begin_comment
comment|/*  * Abstract class for a hash map result.  For reading the values, one-by-one.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorMapJoinHashMapResult
extends|extends
name|VectorMapJoinHashTableResult
block|{
comment|/**    * @return Whether there are any rows (i.e. true for match).    */
specifier|public
specifier|abstract
name|boolean
name|hasRows
parameter_list|()
function_decl|;
comment|/**    * @return Whether there is 1 value row.    */
specifier|public
specifier|abstract
name|boolean
name|isSingleRow
parameter_list|()
function_decl|;
comment|/**    * @return Whether there is a capped count available from cappedCount.    */
specifier|public
specifier|abstract
name|boolean
name|isCappedCountAvailable
parameter_list|()
function_decl|;
comment|/**    * @return The count of values, up to a arbitrary cap limit.  When available, the capped    *         count can be used to make decisions on how to optimally generate join results.    */
specifier|public
specifier|abstract
name|int
name|cappedCount
parameter_list|()
function_decl|;
comment|/**    * @return A reference to the first value, or null if there are no values.    */
specifier|public
specifier|abstract
name|ByteSegmentRef
name|first
parameter_list|()
function_decl|;
comment|/**    * @return The next value, or null if there are no more values to be read.    */
specifier|public
specifier|abstract
name|ByteSegmentRef
name|next
parameter_list|()
function_decl|;
comment|/**    * Get detailed HashMap result position information to help diagnose exceptions.    */
specifier|public
specifier|abstract
name|String
name|getDetailedHashMapResultPositionString
parameter_list|()
function_decl|;
block|}
end_class

end_unit

