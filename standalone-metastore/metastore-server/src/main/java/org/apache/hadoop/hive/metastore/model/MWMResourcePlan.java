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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * Storage class for ResourcePlan.  */
end_comment

begin_class
specifier|public
class|class
name|MWMResourcePlan
block|{
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|String
name|ns
decl_stmt|;
specifier|private
name|Integer
name|queryParallelism
decl_stmt|;
specifier|private
name|Status
name|status
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|MWMPool
argument_list|>
name|pools
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|MWMTrigger
argument_list|>
name|triggers
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|MWMMapping
argument_list|>
name|mappings
decl_stmt|;
specifier|private
name|MWMPool
name|defaultPool
decl_stmt|;
specifier|public
enum|enum
name|Status
block|{
name|ACTIVE
block|,
name|ENABLED
block|,
name|DISABLED
block|}
specifier|public
name|MWMResourcePlan
parameter_list|()
block|{}
specifier|public
name|MWMResourcePlan
parameter_list|(
name|String
name|name
parameter_list|,
name|Integer
name|queryParallelism
parameter_list|,
name|Status
name|status
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|queryParallelism
operator|=
name|queryParallelism
expr_stmt|;
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
name|void
name|setNs
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|ns
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|String
name|getNs
parameter_list|()
block|{
return|return
name|ns
return|;
block|}
specifier|public
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
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
name|Status
name|getStatus
parameter_list|()
block|{
return|return
name|status
return|;
block|}
specifier|public
name|void
name|setStatus
parameter_list|(
name|Status
name|status
parameter_list|)
block|{
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|MWMPool
argument_list|>
name|getPools
parameter_list|()
block|{
return|return
name|pools
return|;
block|}
specifier|public
name|void
name|setPools
parameter_list|(
name|Set
argument_list|<
name|MWMPool
argument_list|>
name|pools
parameter_list|)
block|{
name|this
operator|.
name|pools
operator|=
name|pools
expr_stmt|;
block|}
specifier|public
name|MWMPool
name|getDefaultPool
parameter_list|()
block|{
return|return
name|defaultPool
return|;
block|}
specifier|public
name|void
name|setDefaultPool
parameter_list|(
name|MWMPool
name|defaultPool
parameter_list|)
block|{
name|this
operator|.
name|defaultPool
operator|=
name|defaultPool
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
specifier|public
name|Set
argument_list|<
name|MWMMapping
argument_list|>
name|getMappings
parameter_list|()
block|{
return|return
name|mappings
return|;
block|}
specifier|public
name|void
name|setMappings
parameter_list|(
name|Set
argument_list|<
name|MWMMapping
argument_list|>
name|mappings
parameter_list|)
block|{
name|this
operator|.
name|mappings
operator|=
name|mappings
expr_stmt|;
block|}
block|}
end_class

end_unit

