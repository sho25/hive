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
name|ObjectInspector
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
name|Writable
import|;
end_import

begin_comment
comment|/**  * LazyPrimitive stores a primitive Object in a LazyObject.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|LazyPrimitive
parameter_list|<
name|OI
extends|extends
name|ObjectInspector
parameter_list|,
name|T
extends|extends
name|Writable
parameter_list|>
extends|extends
name|LazyObject
argument_list|<
name|OI
argument_list|>
block|{
name|LazyPrimitive
parameter_list|(
name|OI
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
name|LazyPrimitive
parameter_list|(
name|LazyPrimitive
argument_list|<
name|OI
argument_list|,
name|T
argument_list|>
name|copy
parameter_list|)
block|{
name|super
argument_list|(
name|copy
operator|.
name|oi
argument_list|)
expr_stmt|;
name|isNull
operator|=
name|copy
operator|.
name|isNull
expr_stmt|;
block|}
name|T
name|data
decl_stmt|;
name|boolean
name|isNull
init|=
literal|false
decl_stmt|;
comment|/**    * Returns the primitive object represented by this LazyObject. This is useful    * because it can make sure we have "null" for null objects.    */
annotation|@
name|Override
specifier|public
name|Object
name|getObject
parameter_list|()
block|{
return|return
name|isNull
condition|?
literal|null
else|:
name|this
return|;
block|}
specifier|public
name|T
name|getWritableObject
parameter_list|()
block|{
return|return
name|isNull
condition|?
literal|null
else|:
name|data
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
name|isNull
condition|?
literal|null
else|:
name|data
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|isNull
condition|?
literal|0
else|:
name|data
operator|.
name|hashCode
argument_list|()
return|;
block|}
block|}
end_class

end_unit

