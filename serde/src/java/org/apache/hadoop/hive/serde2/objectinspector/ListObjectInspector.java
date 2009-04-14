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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_interface
specifier|public
interface|interface
name|ListObjectInspector
extends|extends
name|ObjectInspector
block|{
comment|// ** Methods that does not need a data object **
specifier|public
name|ObjectInspector
name|getListElementObjectInspector
parameter_list|()
function_decl|;
comment|// ** Methods that need a data object **
comment|/** returns null for null list, out-of-the-range index.    */
specifier|public
name|Object
name|getListElement
parameter_list|(
name|Object
name|data
parameter_list|,
name|int
name|index
parameter_list|)
function_decl|;
comment|/** returns -1 for data = null.    */
specifier|public
name|int
name|getListLength
parameter_list|(
name|Object
name|data
parameter_list|)
function_decl|;
comment|/** returns null for data = null.    *      *  Note: This method should not return a List object that is reused by the     *  same ListObjectInspector, because it's possible that the same     *  ListObjectInspector will be used in multiple places in the code.    *      *  However it's OK if the List object is part of the Object data.    */
specifier|public
name|List
argument_list|<
name|?
argument_list|>
name|getList
parameter_list|(
name|Object
name|data
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

