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
name|serde2
operator|.
name|lazy
package|;
end_package

begin_comment
comment|/**  * LazyObject stores an object in a range of bytes in a byte[].  *   * A LazyObject can represent any primitive object or hierarchical object  * like array, map or struct.  */
end_comment

begin_interface
specifier|public
interface|interface
name|LazyObject
block|{
comment|/**    * Set the data for this LazyObject.    * We take ByteArrayRef instead of byte[] so that we will be able to drop    * the reference to byte[] by a single assignment.    * The ByteArrayRef object can be reused across multiple rows.    * @param bytes  The wrapper of the byte[].    * @param start  The start position inside the bytes.    * @param length The length of the data, starting from "start"    * @see ByteArrayRef    */
name|void
name|init
parameter_list|(
name|ByteArrayRef
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
function_decl|;
comment|/**    * If the LazyObject is a primitive Object, then deserialize it and return    * the actual primitive Object.    * Otherwise (array, map, struct), return this.     */
specifier|public
name|Object
name|getObject
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

