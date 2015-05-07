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
name|JoinUtil
import|;
end_import

begin_comment
comment|/*  * The interface for a single byte array key hash map lookup method.  */
end_comment

begin_interface
specifier|public
interface|interface
name|VectorMapJoinBytesHashMap
extends|extends
name|VectorMapJoinBytesHashTable
extends|,
name|VectorMapJoinHashMap
block|{
comment|/*    * Lookup a byte array key in the hash map.    *    * @param keyBytes    *         A byte array containing the key within a range.    * @param keyStart    *         The offset the beginning of the key.    * @param keyLength    *         The length of the key.    * @param hashMapResult    *         The object to receive small table value(s) information on a MATCH.    *         Or, for SPILL, it has information on where to spill the big table row.    *    * @return    *         Whether the lookup was a match, no match, or spill (the partition with the key    *         is currently spilled).    */
name|JoinUtil
operator|.
name|JoinResult
name|lookup
parameter_list|(
name|byte
index|[]
name|keyBytes
parameter_list|,
name|int
name|keyStart
parameter_list|,
name|int
name|keyLength
parameter_list|,
name|VectorMapJoinHashMapResult
name|hashMapResult
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

