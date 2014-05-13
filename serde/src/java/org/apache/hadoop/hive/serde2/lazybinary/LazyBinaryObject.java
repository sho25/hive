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
name|lazybinary
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
name|lazy
operator|.
name|LazyObjectBase
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
import|;
end_import

begin_comment
comment|/**  * LazyBinaryObject stores an object in a binary format in a byte[]. For  * example, a double takes four bytes.  *   * A LazyBinaryObject can represent any primitive object or hierarchical object  * like string, list, map or struct.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|LazyBinaryObject
parameter_list|<
name|OI
extends|extends
name|ObjectInspector
parameter_list|>
implements|implements
name|LazyObjectBase
block|{
name|OI
name|oi
decl_stmt|;
comment|/**    * Create a LazyBinaryObject.    *     * @param oi    *          Derived classes can access meta information about this Lazy Binary    *          Object (e.g, length, null-bits) from it.    */
specifier|protected
name|LazyBinaryObject
parameter_list|(
name|OI
name|oi
parameter_list|)
block|{
name|this
operator|.
name|oi
operator|=
name|oi
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|abstract
name|int
name|hashCode
parameter_list|()
function_decl|;
block|}
end_class

end_unit

