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
name|metastore
operator|.
name|model
package|;
end_package

begin_class
specifier|public
class|class
name|MWMMapping
block|{
specifier|private
name|MWMResourcePlan
name|resourcePlan
decl_stmt|;
specifier|private
name|EntityType
name|entityType
decl_stmt|;
specifier|private
name|String
name|entityName
decl_stmt|;
specifier|private
name|MWMPool
name|pool
decl_stmt|;
specifier|private
name|Integer
name|ordering
decl_stmt|;
specifier|public
enum|enum
name|EntityType
block|{
name|USER
block|,
name|GROUP
block|,
name|APPLICATION
block|}
specifier|public
name|MWMMapping
parameter_list|(
name|MWMResourcePlan
name|resourcePlan
parameter_list|,
name|EntityType
name|entityType
parameter_list|,
name|String
name|entityName
parameter_list|,
name|MWMPool
name|pool
parameter_list|,
name|Integer
name|ordering
parameter_list|)
block|{
name|this
operator|.
name|resourcePlan
operator|=
name|resourcePlan
expr_stmt|;
name|this
operator|.
name|entityType
operator|=
name|entityType
expr_stmt|;
name|this
operator|.
name|entityName
operator|=
name|entityName
expr_stmt|;
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
name|this
operator|.
name|ordering
operator|=
name|ordering
expr_stmt|;
block|}
specifier|public
name|MWMResourcePlan
name|getResourcePlan
parameter_list|()
block|{
return|return
name|resourcePlan
return|;
block|}
specifier|public
name|void
name|setResourcePlan
parameter_list|(
name|MWMResourcePlan
name|resourcePlan
parameter_list|)
block|{
name|this
operator|.
name|resourcePlan
operator|=
name|resourcePlan
expr_stmt|;
block|}
specifier|public
name|EntityType
name|getEntityType
parameter_list|()
block|{
return|return
name|entityType
return|;
block|}
specifier|public
name|void
name|setEntityType
parameter_list|(
name|EntityType
name|entityType
parameter_list|)
block|{
name|this
operator|.
name|entityType
operator|=
name|entityType
expr_stmt|;
block|}
specifier|public
name|String
name|getEntityName
parameter_list|()
block|{
return|return
name|entityName
return|;
block|}
specifier|public
name|void
name|setEntityName
parameter_list|(
name|String
name|entityName
parameter_list|)
block|{
name|this
operator|.
name|entityName
operator|=
name|entityName
expr_stmt|;
block|}
specifier|public
name|MWMPool
name|getPool
parameter_list|()
block|{
return|return
name|pool
return|;
block|}
specifier|public
name|void
name|setPool
parameter_list|(
name|MWMPool
name|pool
parameter_list|)
block|{
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
block|}
specifier|public
name|Integer
name|getOrdering
parameter_list|()
block|{
return|return
name|ordering
return|;
block|}
specifier|public
name|void
name|setOrdering
parameter_list|(
name|Integer
name|ordering
parameter_list|)
block|{
name|this
operator|.
name|ordering
operator|=
name|ordering
expr_stmt|;
block|}
block|}
end_class

end_unit

