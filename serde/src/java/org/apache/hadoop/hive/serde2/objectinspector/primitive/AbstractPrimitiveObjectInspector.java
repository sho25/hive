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
comment|/**  * An AbstractPrimitiveObjectInspector is based on  * ObjectInspectorUtils.PrimitiveTypeEntry.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractPrimitiveObjectInspector
implements|implements
name|PrimitiveObjectInspector
block|{
name|PrimitiveTypeEntry
name|typeEntry
decl_stmt|;
comment|/**    * Construct a AbstractPrimitiveObjectInspector.    */
specifier|protected
name|AbstractPrimitiveObjectInspector
parameter_list|(
name|PrimitiveTypeEntry
name|typeEntry
parameter_list|)
block|{
name|this
operator|.
name|typeEntry
operator|=
name|typeEntry
expr_stmt|;
block|}
comment|/**    * Return the associated Java primitive class for this primitive    * ObjectInspector.    */
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getJavaPrimitiveClass
parameter_list|()
block|{
return|return
name|typeEntry
operator|.
name|primitiveJavaClass
return|;
block|}
comment|/**    * Return the associated primitive category for this primitive    * ObjectInspector.    */
annotation|@
name|Override
specifier|public
name|PrimitiveCategory
name|getPrimitiveCategory
parameter_list|()
block|{
return|return
name|typeEntry
operator|.
name|primitiveCategory
return|;
block|}
comment|/**    * Return the associated primitive Writable class for this primitive    * ObjectInspector.    */
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getPrimitiveWritableClass
parameter_list|()
block|{
return|return
name|typeEntry
operator|.
name|primitiveWritableClass
return|;
block|}
comment|/**    * Return the associated category this primitive ObjectInspector.    */
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
comment|/**    * Return the type name for this primitive ObjectInspector.    */
annotation|@
name|Override
specifier|public
name|String
name|getTypeName
parameter_list|()
block|{
return|return
name|typeEntry
operator|.
name|typeName
return|;
block|}
block|}
end_class

end_unit

