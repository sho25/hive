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
name|Map
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
name|LazyBinaryMap
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
name|StandardMapObjectInspector
import|;
end_import

begin_comment
comment|/**  * ObjectInspector for LazyBinaryMap.  *   * @see LazyBinaryMap  */
end_comment

begin_class
specifier|public
class|class
name|LazyBinaryMapObjectInspector
extends|extends
name|StandardMapObjectInspector
block|{
specifier|protected
name|LazyBinaryMapObjectInspector
parameter_list|(
name|ObjectInspector
name|mapKeyObjectInspector
parameter_list|,
name|ObjectInspector
name|mapValueObjectInspector
parameter_list|)
block|{
name|super
argument_list|(
name|mapKeyObjectInspector
argument_list|,
name|mapValueObjectInspector
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|getMap
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
return|return
operator|(
operator|(
name|LazyBinaryMap
operator|)
name|data
operator|)
operator|.
name|getMap
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMapSize
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
return|return
operator|(
operator|(
name|LazyBinaryMap
operator|)
name|data
operator|)
operator|.
name|getMapSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getMapValueElement
parameter_list|(
name|Object
name|data
parameter_list|,
name|Object
name|key
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
return|return
operator|(
operator|(
name|LazyBinaryMap
operator|)
name|data
operator|)
operator|.
name|getMapValueElement
argument_list|(
name|key
argument_list|)
return|;
block|}
block|}
end_class

end_unit

