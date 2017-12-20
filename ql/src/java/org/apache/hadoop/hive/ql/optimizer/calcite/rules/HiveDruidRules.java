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
name|ql
operator|.
name|optimizer
operator|.
name|calcite
operator|.
name|rules
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidAggregateFilterTransposeRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidAggregateProjectRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidAggregateRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidFilterAggregateTransposeRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidFilterProjectTransposeRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidFilterRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidPostAggregationProjectRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidProjectFilterTransposeRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidProjectRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidProjectSortTransposeRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidSortProjectTransposeRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidRules
operator|.
name|DruidSortRule
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
name|optimizer
operator|.
name|calcite
operator|.
name|HiveRelFactories
import|;
end_import

begin_comment
comment|/**  * Druid rules with Hive builder factory.  */
end_comment

begin_class
specifier|public
class|class
name|HiveDruidRules
block|{
specifier|public
specifier|static
specifier|final
name|DruidFilterRule
name|FILTER
init|=
operator|new
name|DruidFilterRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidProjectRule
name|PROJECT
init|=
operator|new
name|DruidProjectRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidAggregateRule
name|AGGREGATE
init|=
operator|new
name|DruidAggregateRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidAggregateProjectRule
name|AGGREGATE_PROJECT
init|=
operator|new
name|DruidAggregateProjectRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidSortRule
name|SORT
init|=
operator|new
name|DruidSortRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidSortProjectTransposeRule
name|SORT_PROJECT_TRANSPOSE
init|=
operator|new
name|DruidSortProjectTransposeRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidProjectSortTransposeRule
name|PROJECT_SORT_TRANSPOSE
init|=
operator|new
name|DruidProjectSortTransposeRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidProjectFilterTransposeRule
name|PROJECT_FILTER_TRANSPOSE
init|=
operator|new
name|DruidProjectFilterTransposeRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidFilterProjectTransposeRule
name|FILTER_PROJECT_TRANSPOSE
init|=
operator|new
name|DruidFilterProjectTransposeRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidAggregateFilterTransposeRule
name|AGGREGATE_FILTER_TRANSPOSE
init|=
operator|new
name|DruidAggregateFilterTransposeRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidFilterAggregateTransposeRule
name|FILTER_AGGREGATE_TRANSPOSE
init|=
operator|new
name|DruidFilterAggregateTransposeRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|DruidPostAggregationProjectRule
name|POST_AGGREGATION_PROJECT
init|=
operator|new
name|DruidPostAggregationProjectRule
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|)
decl_stmt|;
block|}
end_class

end_unit

