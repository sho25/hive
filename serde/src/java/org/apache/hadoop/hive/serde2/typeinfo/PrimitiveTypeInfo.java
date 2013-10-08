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
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
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
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
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
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
operator|.
name|PrimitiveTypeEntry
import|;
end_import

begin_comment
comment|/**  * There are limited number of Primitive Types. All Primitive Types are defined  * by TypeInfoFactory.isPrimitiveClass().  *  * Always use the TypeInfoFactory to create new TypeInfo objects, instead of  * directly creating an instance of this class.  */
end_comment

begin_class
specifier|public
class|class
name|PrimitiveTypeInfo
extends|extends
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
comment|// Base name (varchar vs fully qualified name such as varchar(200)).
specifier|protected
name|String
name|typeName
decl_stmt|;
comment|/**    * For java serialization use only.    */
specifier|public
name|PrimitiveTypeInfo
parameter_list|()
block|{   }
comment|/**    * For TypeInfoFactory use only.    */
name|PrimitiveTypeInfo
parameter_list|(
name|String
name|typeName
parameter_list|)
block|{
name|this
operator|.
name|typeName
operator|=
name|typeName
expr_stmt|;
block|}
comment|/**    * Returns the category of this TypeInfo.    */
annotation|@
name|Override
specifier|public
name|Category
name|getCategory
parameter_list|()
block|{
return|return
name|Category
operator|.
name|PRIMITIVE
return|;
block|}
specifier|public
name|PrimitiveCategory
name|getPrimitiveCategory
parameter_list|()
block|{
return|return
name|getPrimitiveTypeEntry
argument_list|()
operator|.
name|primitiveCategory
return|;
block|}
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getPrimitiveWritableClass
parameter_list|()
block|{
return|return
name|getPrimitiveTypeEntry
argument_list|()
operator|.
name|primitiveWritableClass
return|;
block|}
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getPrimitiveJavaClass
parameter_list|()
block|{
return|return
name|getPrimitiveTypeEntry
argument_list|()
operator|.
name|primitiveJavaClass
return|;
block|}
comment|// The following 2 methods are for java serialization use only.
specifier|public
name|void
name|setTypeName
parameter_list|(
name|String
name|typeName
parameter_list|)
block|{
name|this
operator|.
name|typeName
operator|=
name|typeName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTypeName
parameter_list|()
block|{
return|return
name|typeName
return|;
block|}
specifier|public
name|PrimitiveTypeEntry
name|getPrimitiveTypeEntry
parameter_list|()
block|{
return|return
name|PrimitiveObjectInspectorUtils
operator|.
name|getTypeEntryFromTypeName
argument_list|(
name|typeName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
literal|null
operator|||
operator|!
operator|(
name|other
operator|instanceof
name|PrimitiveTypeInfo
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|PrimitiveTypeInfo
name|pti
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|other
decl_stmt|;
return|return
name|this
operator|.
name|typeName
operator|.
name|equals
argument_list|(
name|pti
operator|.
name|typeName
argument_list|)
return|;
block|}
comment|/**    * Generate the hashCode for this TypeInfo.    */
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|typeName
operator|.
name|hashCode
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
name|typeName
return|;
block|}
block|}
end_class

end_unit

