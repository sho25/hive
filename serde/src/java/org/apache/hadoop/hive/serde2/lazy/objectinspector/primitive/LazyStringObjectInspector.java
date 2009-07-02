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
name|LazyString
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
name|StringObjectInspector
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
name|Text
import|;
end_import

begin_comment
comment|/**  * A WritableStringObjectInspector inspects a Text Object.  */
end_comment

begin_class
specifier|public
class|class
name|LazyStringObjectInspector
extends|extends
name|AbstractPrimitiveLazyObjectInspector
argument_list|<
name|Text
argument_list|>
implements|implements
name|StringObjectInspector
block|{
name|boolean
name|escaped
decl_stmt|;
name|byte
name|escapeChar
decl_stmt|;
name|LazyStringObjectInspector
parameter_list|(
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|)
block|{
name|super
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|stringTypeEntry
argument_list|)
expr_stmt|;
name|this
operator|.
name|escaped
operator|=
name|escaped
expr_stmt|;
name|this
operator|.
name|escapeChar
operator|=
name|escapeChar
expr_stmt|;
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
name|LazyString
argument_list|(
operator|(
name|LazyString
operator|)
name|o
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Text
name|getPrimitiveWritableObject
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
operator|(
operator|(
name|LazyString
operator|)
name|o
operator|)
operator|.
name|getWritableObject
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
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
operator|(
operator|(
name|LazyString
operator|)
name|o
operator|)
operator|.
name|getWritableObject
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isEscaped
parameter_list|()
block|{
return|return
name|escaped
return|;
block|}
specifier|public
name|byte
name|getEscapeChar
parameter_list|()
block|{
return|return
name|escapeChar
return|;
block|}
block|}
end_class

end_unit

