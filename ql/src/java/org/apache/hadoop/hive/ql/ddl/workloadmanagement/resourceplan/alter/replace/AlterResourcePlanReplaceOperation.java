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
name|ql
operator|.
name|ddl
operator|.
name|workloadmanagement
operator|.
name|resourceplan
operator|.
name|alter
operator|.
name|replace
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|metastore
operator|.
name|api
operator|.
name|WMFullResourcePlan
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
name|metastore
operator|.
name|api
operator|.
name|WMNullableResourcePlan
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
name|metastore
operator|.
name|api
operator|.
name|WMResourcePlanStatus
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
name|ql
operator|.
name|ddl
operator|.
name|DDLOperationContext
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
name|ql
operator|.
name|ddl
operator|.
name|workloadmanagement
operator|.
name|resourceplan
operator|.
name|alter
operator|.
name|AbstractAlterResourcePlanStatusOperation
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_comment
comment|/**  * Operation process of replacing a resource plan.  */
end_comment

begin_class
specifier|public
class|class
name|AlterResourcePlanReplaceOperation
extends|extends
name|AbstractAlterResourcePlanStatusOperation
argument_list|<
name|AlterResourcePlanReplaceDesc
argument_list|>
block|{
comment|// Note: the resource plan operations are going to be annotated with namespace based on the config
comment|//       inside Hive.java. We don't want HS2 to be aware of namespaces beyond that, or to even see
comment|//       that there exist other namespaces, because one HS2 always operates inside just one and we
comment|//       don't want this complexity to bleed everywhere. Therefore, this code doesn't care about
comment|//       namespaces - Hive.java will transparently scope everything. That's the idea anyway.
specifier|public
name|AlterResourcePlanReplaceOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|AlterResourcePlanReplaceDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|WMNullableResourcePlan
name|resourcePlan
init|=
operator|new
name|WMNullableResourcePlan
argument_list|()
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|getDestinationResourcePlanName
argument_list|()
operator|==
literal|null
condition|)
block|{
name|resourcePlan
operator|.
name|setStatus
argument_list|(
name|WMResourcePlanStatus
operator|.
name|ACTIVE
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|resourcePlan
operator|.
name|setName
argument_list|(
name|desc
operator|.
name|getDestinationResourcePlanName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|WMFullResourcePlan
name|appliedResourcePlan
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|alterResourcePlan
argument_list|(
name|desc
operator|.
name|getResourcePlanName
argument_list|()
argument_list|,
name|resourcePlan
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|handleWMServiceChangeIfNeeded
argument_list|(
name|appliedResourcePlan
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit

