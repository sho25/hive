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
name|optiq
operator|.
name|reloperators
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
name|ql
operator|.
name|optimizer
operator|.
name|optiq
operator|.
name|TraitsUtil
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
name|optiq
operator|.
name|cost
operator|.
name|HiveCost
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|RelNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|SingleRel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|RelOptCluster
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|RelOptCost
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|RelOptPlanner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|RelTraitSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexNode
import|;
end_import

begin_class
specifier|public
class|class
name|HiveLimitRel
extends|extends
name|SingleRel
implements|implements
name|HiveRel
block|{
specifier|private
specifier|final
name|RexNode
name|offset
decl_stmt|;
specifier|private
specifier|final
name|RexNode
name|fetch
decl_stmt|;
name|HiveLimitRel
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelTraitSet
name|traitSet
parameter_list|,
name|RelNode
name|child
parameter_list|,
name|RexNode
name|offset
parameter_list|,
name|RexNode
name|fetch
parameter_list|)
block|{
name|super
argument_list|(
name|cluster
argument_list|,
name|TraitsUtil
operator|.
name|getDefaultTraitSet
argument_list|(
name|cluster
argument_list|)
argument_list|,
name|child
argument_list|)
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|fetch
operator|=
name|fetch
expr_stmt|;
assert|assert
name|getConvention
argument_list|()
operator|instanceof
name|HiveRel
assert|;
assert|assert
name|getConvention
argument_list|()
operator|==
name|child
operator|.
name|getConvention
argument_list|()
assert|;
block|}
annotation|@
name|Override
specifier|public
name|HiveLimitRel
name|copy
parameter_list|(
name|RelTraitSet
name|traitSet
parameter_list|,
name|List
argument_list|<
name|RelNode
argument_list|>
name|newInputs
parameter_list|)
block|{
return|return
operator|new
name|HiveLimitRel
argument_list|(
name|getCluster
argument_list|()
argument_list|,
name|traitSet
argument_list|,
name|sole
argument_list|(
name|newInputs
argument_list|)
argument_list|,
name|offset
argument_list|,
name|fetch
argument_list|)
return|;
block|}
specifier|public
name|void
name|implement
parameter_list|(
name|Implementor
name|implementor
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|RelOptCost
name|computeSelfCost
parameter_list|(
name|RelOptPlanner
name|planner
parameter_list|)
block|{
return|return
name|HiveCost
operator|.
name|FACTORY
operator|.
name|makeZeroCost
argument_list|()
return|;
block|}
block|}
end_class

end_unit

