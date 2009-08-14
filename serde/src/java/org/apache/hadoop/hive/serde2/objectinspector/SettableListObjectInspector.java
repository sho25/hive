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
name|objectinspector
package|;
end_package

begin_interface
specifier|public
interface|interface
name|SettableListObjectInspector
extends|extends
name|ListObjectInspector
block|{
comment|/**    * Create a list with the given size. All elements will be null.    *     * NOTE: This is different from ArrayList constructor where the argument    * is capacity.  We decided to have size here to allow creation of Java    * array.     */
name|Object
name|create
parameter_list|(
name|int
name|size
parameter_list|)
function_decl|;
comment|/**    * Set the element at index. Returns the list.    */
name|Object
name|set
parameter_list|(
name|Object
name|list
parameter_list|,
name|int
name|index
parameter_list|,
name|Object
name|element
parameter_list|)
function_decl|;
comment|/**    * Resize the list. Returns the list.    * If the new size is bigger than the current size, new elements will    * be null.  If the new size is smaller than the current size, elements    * at the end are truncated.    */
name|Object
name|resize
parameter_list|(
name|Object
name|list
parameter_list|,
name|int
name|newSize
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

