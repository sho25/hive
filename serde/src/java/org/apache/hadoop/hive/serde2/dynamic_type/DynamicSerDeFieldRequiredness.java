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
name|dynamic_type
package|;
end_package

begin_comment
comment|/**  * DynamicSerDeFieldRequiredness.  *  */
end_comment

begin_class
specifier|public
class|class
name|DynamicSerDeFieldRequiredness
extends|extends
name|SimpleNode
block|{
comment|/**    * RequirednessTypes.    *    */
specifier|public
enum|enum
name|RequirednessTypes
block|{
name|Required
block|,
name|Skippable
block|,
name|Optional
block|,   }
empty_stmt|;
comment|/**    * Is this a required, skippable or optional field. Used by DynamicSerDe for    * optimizations.    */
specifier|protected
name|RequirednessTypes
name|requiredness
decl_stmt|;
comment|/**    * Get the requiredness attribute of this field.    */
specifier|public
name|RequirednessTypes
name|getRequiredness
parameter_list|()
block|{
return|return
name|requiredness
return|;
block|}
specifier|public
name|DynamicSerDeFieldRequiredness
parameter_list|(
name|int
name|id
parameter_list|)
block|{
name|super
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DynamicSerDeFieldRequiredness
parameter_list|(
name|thrift_grammar
name|p
parameter_list|,
name|int
name|id
parameter_list|)
block|{
name|super
argument_list|(
name|p
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

