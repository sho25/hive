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
operator|.
name|primitive
package|;
end_package

begin_comment
comment|/**  * A SettableDoubleObjectInspector can set a double value to an object.  */
end_comment

begin_interface
specifier|public
interface|interface
name|SettableDoubleObjectInspector
extends|extends
name|DoubleObjectInspector
block|{
comment|/**    * Set the object with the value. Return the object that has the new value.    *     * In most cases the returned value should be the same as o, but in case o is    * unmodifiable, this will return a new object with new value.    */
name|Object
name|set
parameter_list|(
name|Object
name|o
parameter_list|,
name|double
name|value
parameter_list|)
function_decl|;
comment|/**    * Create an object with the value.    */
name|Object
name|create
parameter_list|(
name|double
name|value
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

