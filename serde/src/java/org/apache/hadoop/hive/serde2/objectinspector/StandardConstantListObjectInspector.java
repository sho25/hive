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

begin_comment
comment|/**  * A StandardListObjectInspector which also implements the  * ConstantObjectInspector interface.  *  * Always use the ObjectInspectorFactory to create new ObjectInspector objects,  * instead of directly creating an instance of this class.  */
end_comment

begin_class
specifier|public
class|class
name|StandardConstantListObjectInspector
extends|extends
name|StandardListObjectInspector
implements|implements
name|ConstantObjectInspector
block|{
specifier|private
name|List
argument_list|<
name|?
argument_list|>
name|value
decl_stmt|;
comment|/**    * Call ObjectInspectorFactory.getStandardListObjectInspector instead.    */
specifier|protected
name|StandardConstantListObjectInspector
parameter_list|(
name|ObjectInspector
name|listElementObjectInspector
parameter_list|,
name|List
argument_list|<
name|?
argument_list|>
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|listElementObjectInspector
argument_list|)
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|?
argument_list|>
name|getWritableConstantValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
block|}
end_class

end_unit

