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
name|lazybinary
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
name|lazybinary
operator|.
name|LazyBinaryUnion
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
name|StandardUnionObjectInspector
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
name|UnionObject
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * ObjectInspector for LazyBinaryUnion.  *  * @see org.apache.hadoop.hive.serde2.lazybinary.LazyBinaryUnion  */
end_comment

begin_class
specifier|public
class|class
name|LazyBinaryUnionObjectInspector
extends|extends
name|StandardUnionObjectInspector
block|{
specifier|protected
name|LazyBinaryUnionObjectInspector
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|LazyBinaryUnionObjectInspector
parameter_list|(
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|unionFieldObjectInspectors
parameter_list|)
block|{
name|super
argument_list|(
name|unionFieldObjectInspectors
argument_list|)
expr_stmt|;
block|}
comment|/**    * Return the tag of the object.    */
specifier|public
name|byte
name|getTag
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|LazyBinaryUnion
name|lazyBinaryUnion
init|=
operator|(
name|LazyBinaryUnion
operator|)
name|o
decl_stmt|;
return|return
name|lazyBinaryUnion
operator|.
name|getTag
argument_list|()
return|;
block|}
comment|/**    * Return the field based on the tag value associated with the Object.    */
specifier|public
name|Object
name|getField
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|LazyBinaryUnion
name|lazyBinaryUnion
init|=
operator|(
name|LazyBinaryUnion
operator|)
name|o
decl_stmt|;
return|return
name|lazyBinaryUnion
operator|.
name|getField
argument_list|()
return|;
block|}
block|}
end_class

end_unit

