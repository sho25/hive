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
name|plan
operator|.
name|RelOptRuleCall
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
name|rel
operator|.
name|core
operator|.
name|Filter
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
name|rel
operator|.
name|core
operator|.
name|Project
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
name|rel
operator|.
name|core
operator|.
name|RelFactories
operator|.
name|FilterFactory
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
name|rel
operator|.
name|core
operator|.
name|RelFactories
operator|.
name|ProjectFactory
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
name|rel
operator|.
name|rules
operator|.
name|FilterProjectTransposeRule
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
name|rex
operator|.
name|RexNode
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
name|HiveCalciteUtil
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
name|reloperators
operator|.
name|HiveProject
import|;
end_import

begin_class
specifier|public
class|class
name|HiveFilterProjectTransposeRule
extends|extends
name|FilterProjectTransposeRule
block|{
specifier|public
specifier|static
specifier|final
name|HiveFilterProjectTransposeRule
name|INSTANCE_DETERMINISTIC
init|=
operator|new
name|HiveFilterProjectTransposeRule
argument_list|(
name|Filter
operator|.
name|class
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_FILTER_FACTORY
argument_list|,
name|HiveProject
operator|.
name|class
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_PROJECT_FACTORY
argument_list|,
literal|true
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveFilterProjectTransposeRule
name|INSTANCE
init|=
operator|new
name|HiveFilterProjectTransposeRule
argument_list|(
name|Filter
operator|.
name|class
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_FILTER_FACTORY
argument_list|,
name|HiveProject
operator|.
name|class
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_PROJECT_FACTORY
argument_list|,
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|onlyDeterministic
decl_stmt|;
specifier|public
name|HiveFilterProjectTransposeRule
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Filter
argument_list|>
name|filterClass
parameter_list|,
name|FilterFactory
name|filterFactory
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Project
argument_list|>
name|projectClass
parameter_list|,
name|ProjectFactory
name|projectFactory
parameter_list|,
name|boolean
name|onlyDeterministic
parameter_list|)
block|{
name|super
argument_list|(
name|filterClass
argument_list|,
name|filterFactory
argument_list|,
name|projectClass
argument_list|,
name|projectFactory
argument_list|)
expr_stmt|;
name|this
operator|.
name|onlyDeterministic
operator|=
name|onlyDeterministic
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|matches
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
specifier|final
name|Filter
name|filterRel
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RexNode
name|condition
init|=
name|filterRel
operator|.
name|getCondition
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|onlyDeterministic
operator|&&
operator|!
name|HiveCalciteUtil
operator|.
name|isDeterministic
argument_list|(
name|condition
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|super
operator|.
name|matches
argument_list|(
name|call
argument_list|)
return|;
block|}
block|}
end_class

end_unit

