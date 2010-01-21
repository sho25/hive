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
name|Map
import|;
end_import

begin_interface
specifier|public
interface|interface
name|MapObjectInspector
extends|extends
name|ObjectInspector
block|{
comment|// ** Methods that does not need a data object **
comment|// Map Type
specifier|public
name|ObjectInspector
name|getMapKeyObjectInspector
parameter_list|()
function_decl|;
specifier|public
name|ObjectInspector
name|getMapValueObjectInspector
parameter_list|()
function_decl|;
comment|// ** Methods that need a data object **
comment|// In this function, key has to be of the same structure as the Map expects.
comment|// Most cases key will be primitive type, so it's OK.
comment|// In rare cases that key is not primitive, the user is responsible for
comment|// defining
comment|// the hashCode() and equals() methods of the key class.
specifier|public
name|Object
name|getMapValueElement
parameter_list|(
name|Object
name|data
parameter_list|,
name|Object
name|key
parameter_list|)
function_decl|;
comment|/**    * returns null for data = null.    *     * Note: This method should not return a Map object that is reused by the same    * MapObjectInspector, because it's possible that the same MapObjectInspector    * will be used in multiple places in the code.    *     * However it's OK if the Map object is part of the Object data.    */
specifier|public
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|getMap
parameter_list|(
name|Object
name|data
parameter_list|)
function_decl|;
comment|/**    * returns -1 for NULL map.    */
specifier|public
name|int
name|getMapSize
parameter_list|(
name|Object
name|data
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

