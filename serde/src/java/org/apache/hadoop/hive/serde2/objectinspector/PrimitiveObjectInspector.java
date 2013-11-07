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
name|typeinfo
operator|.
name|PrimitiveTypeInfo
import|;
end_import

begin_comment
comment|/**  * PrimitiveObjectInspector.  *  */
end_comment

begin_interface
specifier|public
interface|interface
name|PrimitiveObjectInspector
extends|extends
name|ObjectInspector
block|{
comment|/**    * The primitive types supported by Hive.    */
specifier|public
specifier|static
enum|enum
name|PrimitiveCategory
block|{
name|VOID
block|,
name|BOOLEAN
block|,
name|BYTE
block|,
name|SHORT
block|,
name|INT
block|,
name|LONG
block|,
name|FLOAT
block|,
name|DOUBLE
block|,
name|STRING
block|,
name|DATE
block|,
name|TIMESTAMP
block|,
name|BINARY
block|,
name|DECIMAL
block|,
name|VARCHAR
block|,
name|CHAR
block|,
name|UNKNOWN
block|}
empty_stmt|;
specifier|public
name|PrimitiveTypeInfo
name|getTypeInfo
parameter_list|()
function_decl|;
comment|/**    * Get the primitive category of the PrimitiveObjectInspector.    */
name|PrimitiveCategory
name|getPrimitiveCategory
parameter_list|()
function_decl|;
comment|/**    * Get the Primitive Writable class which is the return type of    * getPrimitiveWritableObject() and copyToPrimitiveWritableObject().    */
name|Class
argument_list|<
name|?
argument_list|>
name|getPrimitiveWritableClass
parameter_list|()
function_decl|;
comment|/**    * Return the data in an instance of primitive writable Object. If the Object    * is already a primitive writable Object, just return o.    */
name|Object
name|getPrimitiveWritableObject
parameter_list|(
name|Object
name|o
parameter_list|)
function_decl|;
comment|/**    * Get the Java Primitive class which is the return type of    * getJavaPrimitiveObject().    */
name|Class
argument_list|<
name|?
argument_list|>
name|getJavaPrimitiveClass
parameter_list|()
function_decl|;
comment|/**    * Get the Java Primitive object.    */
name|Object
name|getPrimitiveJavaObject
parameter_list|(
name|Object
name|o
parameter_list|)
function_decl|;
comment|/**    * Get a copy of the Object in the same class, so the return value can be    * stored independently of the parameter.    *    * If the Object is a Primitive Java Object, we just return the parameter    * since Primitive Java Object is immutable.    */
name|Object
name|copyObject
parameter_list|(
name|Object
name|o
parameter_list|)
function_decl|;
comment|/**    * Whether the ObjectInspector prefers to return a Primitive Writable Object    * instead of a Primitive Java Object. This can be useful for determining the    * most efficient way to getting data out of the Object.    */
name|boolean
name|preferWritable
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

