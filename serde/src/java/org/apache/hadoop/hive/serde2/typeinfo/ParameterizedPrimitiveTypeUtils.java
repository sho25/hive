begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_class
specifier|public
class|class
name|ParameterizedPrimitiveTypeUtils
block|{
specifier|public
specifier|static
name|BaseTypeParams
name|getTypeParamsFromTypeInfo
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
name|BaseTypeParams
name|typeParams
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|typeInfo
operator|instanceof
name|PrimitiveTypeInfo
condition|)
block|{
name|PrimitiveTypeInfo
name|ppti
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|typeParams
operator|=
name|ppti
operator|.
name|getTypeParams
argument_list|()
expr_stmt|;
block|}
return|return
name|typeParams
return|;
block|}
specifier|public
specifier|static
name|BaseTypeParams
name|getTypeParamsFromPrimitiveTypeEntry
parameter_list|(
name|PrimitiveTypeEntry
name|typeEntry
parameter_list|)
block|{
return|return
name|typeEntry
operator|.
name|typeParams
return|;
block|}
specifier|public
specifier|static
name|BaseTypeParams
name|getTypeParamsFromPrimitiveObjectInspector
parameter_list|(
name|PrimitiveObjectInspector
name|oi
parameter_list|)
block|{
return|return
name|oi
operator|.
name|getTypeParams
argument_list|()
return|;
block|}
block|}
end_class

end_unit

