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
name|metastore
operator|.
name|model
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_class
specifier|public
class|class
name|MWMPool
block|{
specifier|private
name|MWMResourcePlan
name|resourcePlan
decl_stmt|;
specifier|private
name|String
name|path
decl_stmt|;
specifier|private
name|Double
name|allocFraction
decl_stmt|;
specifier|private
name|Integer
name|queryParallelism
decl_stmt|;
specifier|private
name|String
name|schedulingPolicy
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|MWMTrigger
argument_list|>
name|triggers
decl_stmt|;
specifier|public
name|MWMPool
parameter_list|()
block|{}
specifier|public
name|MWMPool
parameter_list|(
name|MWMResourcePlan
name|resourcePlan
parameter_list|,
name|String
name|path
parameter_list|,
name|Double
name|allocFraction
parameter_list|,
name|Integer
name|queryParallelism
parameter_list|,
name|String
name|schedulingPolicy
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
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|allocFraction
operator|=
name|allocFraction
expr_stmt|;
name|this
operator|.
name|queryParallelism
operator|=
name|queryParallelism
expr_stmt|;
name|this
operator|.
name|schedulingPolicy
operator|=
name|schedulingPolicy
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
name|String
name|getPath
parameter_list|()
block|{
return|return
name|path
return|;
block|}
specifier|public
name|void
name|setPath
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
block|}
specifier|public
name|Double
name|getAllocFraction
parameter_list|()
block|{
return|return
name|allocFraction
return|;
block|}
specifier|public
name|void
name|setAllocFraction
parameter_list|(
name|Double
name|allocFraction
parameter_list|)
block|{
name|this
operator|.
name|allocFraction
operator|=
name|allocFraction
expr_stmt|;
block|}
specifier|public
name|Integer
name|getQueryParallelism
parameter_list|()
block|{
return|return
name|queryParallelism
return|;
block|}
specifier|public
name|void
name|setQueryParallelism
parameter_list|(
name|Integer
name|queryParallelism
parameter_list|)
block|{
name|this
operator|.
name|queryParallelism
operator|=
name|queryParallelism
expr_stmt|;
block|}
specifier|public
name|String
name|getSchedulingPolicy
parameter_list|()
block|{
return|return
name|schedulingPolicy
return|;
block|}
specifier|public
name|void
name|setSchedulingPolicy
parameter_list|(
name|String
name|schedulingPolicy
parameter_list|)
block|{
name|this
operator|.
name|schedulingPolicy
operator|=
name|schedulingPolicy
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|MWMTrigger
argument_list|>
name|getTriggers
parameter_list|()
block|{
return|return
name|triggers
return|;
block|}
specifier|public
name|void
name|setTriggers
parameter_list|(
name|Set
argument_list|<
name|MWMTrigger
argument_list|>
name|triggers
parameter_list|)
block|{
name|this
operator|.
name|triggers
operator|=
name|triggers
expr_stmt|;
block|}
block|}
end_class

end_unit

