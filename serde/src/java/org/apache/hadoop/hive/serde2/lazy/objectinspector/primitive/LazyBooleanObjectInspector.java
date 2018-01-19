begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|lazy
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
name|lazy
operator|.
name|LazyBoolean
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
name|BooleanObjectInspector
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
name|typeinfo
operator|.
name|TypeInfoFactory
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
name|io
operator|.
name|BooleanWritable
import|;
end_import

begin_comment
comment|/**  * A WritableBooleanObjectInspector inspects a BooleanWritable Object.  */
end_comment

begin_class
specifier|public
class|class
name|LazyBooleanObjectInspector
extends|extends
name|AbstractPrimitiveLazyObjectInspector
argument_list|<
name|BooleanWritable
argument_list|>
implements|implements
name|BooleanObjectInspector
block|{
comment|// Whether characters, such as 't/T', 'f/F', and '1/0' are interpreted as valid boolean literals.
specifier|private
name|boolean
name|extendedLiteral
init|=
literal|false
decl_stmt|;
name|LazyBooleanObjectInspector
parameter_list|()
block|{
name|super
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|get
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|getPrimitiveWritableObject
argument_list|(
name|o
argument_list|)
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|copyObject
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|o
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|LazyBoolean
argument_list|(
operator|(
name|LazyBoolean
operator|)
name|o
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getPrimitiveJavaObject
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|o
operator|==
literal|null
condition|?
literal|null
else|:
name|Boolean
operator|.
name|valueOf
argument_list|(
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isExtendedLiteral
parameter_list|()
block|{
return|return
name|extendedLiteral
return|;
block|}
specifier|public
name|void
name|setExtendedLiteral
parameter_list|(
name|boolean
name|extendedLiteral
parameter_list|)
block|{
name|this
operator|.
name|extendedLiteral
operator|=
name|extendedLiteral
expr_stmt|;
block|}
block|}
end_class

end_unit

