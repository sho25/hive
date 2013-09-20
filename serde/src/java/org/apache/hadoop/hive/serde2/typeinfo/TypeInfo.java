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
name|typeinfo
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * Stores information about a type. Always use the TypeInfoFactory to create new  * TypeInfo objects.  *  * We support 5 categories of types: 1. Primitive objects (String, Number, etc)  * 2. List objects (a list of objects of a single type) 3. Map objects (a map  * from objects of one type to objects of another type) 4. Struct objects (a  * list of fields with names and their own types) 5. Union objects  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|TypeInfo
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|protected
name|TypeInfo
parameter_list|()
block|{   }
comment|/**    * The Category of this TypeInfo. Possible values are Primitive, List, Map,    * Struct and Union, which corresponds to the 5 sub-classes of TypeInfo.    */
specifier|public
specifier|abstract
name|Category
name|getCategory
parameter_list|()
function_decl|;
comment|/**    * A String representation of the TypeInfo.    */
specifier|public
specifier|abstract
name|String
name|getTypeName
parameter_list|()
function_decl|;
comment|/**    * String representing the qualified type name.    * Qualified types should override this method.    * @return    */
specifier|public
name|String
name|getQualifiedName
parameter_list|()
block|{
return|return
name|getTypeName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getTypeName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|abstract
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
function_decl|;
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

