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
name|lazybinary
operator|.
name|objectinspector
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|lazybinary
operator|.
name|LazyBinaryArray
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
name|StandardListObjectInspector
import|;
end_import

begin_comment
comment|/**  * ObjectInspector for LazyBinaryList.  */
end_comment

begin_class
specifier|public
class|class
name|LazyBinaryListObjectInspector
extends|extends
name|StandardListObjectInspector
block|{
specifier|protected
name|LazyBinaryListObjectInspector
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|LazyBinaryListObjectInspector
parameter_list|(
name|ObjectInspector
name|listElementObjectInspector
parameter_list|)
block|{
name|super
argument_list|(
name|listElementObjectInspector
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|?
argument_list|>
name|getList
parameter_list|(
name|Object
name|data
parameter_list|)
block|{
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|LazyBinaryArray
name|array
init|=
operator|(
name|LazyBinaryArray
operator|)
name|data
decl_stmt|;
return|return
name|array
operator|.
name|getList
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getListElement
parameter_list|(
name|Object
name|data
parameter_list|,
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|LazyBinaryArray
name|array
init|=
operator|(
name|LazyBinaryArray
operator|)
name|data
decl_stmt|;
return|return
name|array
operator|.
name|getListElementObject
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getListLength
parameter_list|(
name|Object
name|data
parameter_list|)
block|{
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|LazyBinaryArray
name|array
init|=
operator|(
name|LazyBinaryArray
operator|)
name|data
decl_stmt|;
return|return
name|array
operator|.
name|getListLength
argument_list|()
return|;
block|}
block|}
end_class

end_unit

