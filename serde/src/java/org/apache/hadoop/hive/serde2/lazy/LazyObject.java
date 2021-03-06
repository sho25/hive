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

begin_comment
comment|/**  * LazyObject stores an object in a range of bytes in a byte[].  *  * A LazyObject can represent any primitive object or hierarchical object like  * array, map or struct.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|LazyObject
parameter_list|<
name|OI
extends|extends
name|ObjectInspector
parameter_list|>
implements|implements
name|LazyObjectBase
block|{
specifier|protected
name|OI
name|oi
decl_stmt|;
comment|/**    * Create a LazyObject.    *    * @param oi    *          Derived classes can access meta information about this Lazy Object    *          (e.g, separator, nullSequence, escaper) from it.    */
specifier|protected
name|LazyObject
parameter_list|(
name|OI
name|oi
parameter_list|)
block|{
name|this
operator|.
name|oi
operator|=
name|oi
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|abstract
name|int
name|hashCode
parameter_list|()
function_decl|;
specifier|protected
name|OI
name|getInspector
parameter_list|()
block|{
return|return
name|oi
return|;
block|}
specifier|protected
name|void
name|setInspector
parameter_list|(
name|OI
name|oi
parameter_list|)
block|{
name|this
operator|.
name|oi
operator|=
name|oi
expr_stmt|;
block|}
specifier|protected
name|boolean
name|isNull
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ByteArrayRef
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
name|bytes
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"bytes cannot be null!"
argument_list|)
throw|;
block|}
name|this
operator|.
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setNull
parameter_list|()
block|{
name|this
operator|.
name|isNull
operator|=
literal|true
expr_stmt|;
block|}
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
block|}
end_class

end_unit

