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
name|serde
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * A Hive Field knows how to get a component of a given object  * Implementations are likely to be tied to the corresponding  * SerDe implementation.  *  * Hive allows base types (Java numbers,string,boolean types), maps and lists  * and complex types created out arbitrary nesting and composition of these.  *  */
end_comment

begin_interface
specifier|public
interface|interface
name|SerDeField
block|{
comment|/**    * Get the field from an object. The object must be of valid type    * Will return null if the field is not defined.    * @param obj The object from which to get the Field    * @return the object corresponding to the Field    *    */
specifier|public
name|Object
name|get
parameter_list|(
name|Object
name|obj
parameter_list|)
throws|throws
name|SerDeException
function_decl|;
comment|/**      * Does this Field represent a List?      * @return true is the Field represents a list. False otherwise      */
specifier|public
name|boolean
name|isList
parameter_list|()
function_decl|;
comment|/**      * Does this Field represent a Map?      * @return true is the Field represents a map. False otherwise      */
specifier|public
name|boolean
name|isMap
parameter_list|()
function_decl|;
comment|/**      * Does this Field represent a primitive object?      * @return false if map or list. false if object has subfields (composition).      * true otherwise      */
specifier|public
name|boolean
name|isPrimitive
parameter_list|()
function_decl|;
comment|/**      * Get the type of the object represented by this Field. ie. all the      * objects returned by the get() call will be of the returned type      * @return Class of objects represented by this Field.      */
specifier|public
name|Class
name|getType
parameter_list|()
function_decl|;
comment|/**      * Type of List member objects if List      */
specifier|public
name|Class
name|getListElementType
parameter_list|()
function_decl|;
comment|/**      * Type of Map Key type if Map      */
specifier|public
name|Class
name|getMapKeyType
parameter_list|()
function_decl|;
comment|/**      * Type of Map Value type if Map      */
specifier|public
name|Class
name|getMapValueType
parameter_list|()
function_decl|;
comment|/**      * Name of this field      */
specifier|public
name|String
name|getName
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

