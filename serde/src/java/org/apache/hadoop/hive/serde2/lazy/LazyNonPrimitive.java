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

begin_comment
comment|/**  * LazyPrimitive stores a primitive Object in a LazyObject.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|LazyNonPrimitive
parameter_list|<
name|OI
extends|extends
name|ObjectInspector
parameter_list|>
extends|extends
name|LazyObject
argument_list|<
name|OI
argument_list|>
block|{
specifier|protected
name|ByteArrayRef
name|bytes
decl_stmt|;
specifier|protected
name|int
name|start
decl_stmt|;
specifier|protected
name|int
name|length
decl_stmt|;
comment|/**    * Create a LazyNonPrimitive object with the specified ObjectInspector.    * @param oi  The ObjectInspector would have to have a hierarchy of     *            LazyObjectInspectors with the leaf nodes being     *            WritableObjectInspectors.  It's used both for accessing the    *            type hierarchy of the complex object, as well as getting    *            meta information (separator, nullSequence, etc) when parsing    *            the lazy object.    */
specifier|protected
name|LazyNonPrimitive
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
name|bytes
operator|=
literal|null
expr_stmt|;
name|start
operator|=
literal|0
expr_stmt|;
name|length
operator|=
literal|0
expr_stmt|;
block|}
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
name|bytes
operator|=
name|bytes
expr_stmt|;
name|this
operator|.
name|start
operator|=
name|start
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getObject
parameter_list|()
block|{
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

