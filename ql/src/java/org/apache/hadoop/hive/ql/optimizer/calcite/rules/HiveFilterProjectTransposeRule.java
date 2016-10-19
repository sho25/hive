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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|Set
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
name|plan
operator|.
name|RelOptUtil
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
name|RelNode
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
name|rel
operator|.
name|type
operator|.
name|RelDataTypeFactory
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
name|RexCall
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
name|RexInputRef
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
name|calcite
operator|.
name|rex
operator|.
name|RexOver
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
name|RexUtil
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
name|tools
operator|.
name|RelBuilder
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
name|tools
operator|.
name|RelBuilderFactory
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
name|INSTANCE_DETERMINISTIC_WINDOWING
init|=
operator|new
name|HiveFilterProjectTransposeRule
argument_list|(
name|Filter
operator|.
name|class
argument_list|,
name|HiveProject
operator|.
name|class
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
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
name|HiveProject
operator|.
name|class
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|,
literal|true
argument_list|,
literal|false
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
name|HiveProject
operator|.
name|class
argument_list|,
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|onlyDeterministic
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|pushThroughWindowing
decl_stmt|;
specifier|private
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
name|Class
argument_list|<
name|?
extends|extends
name|Project
argument_list|>
name|projectClass
parameter_list|,
name|RelBuilderFactory
name|relBuilderFactory
parameter_list|,
name|boolean
name|onlyDeterministic
parameter_list|,
name|boolean
name|pushThroughWindowing
parameter_list|)
block|{
name|super
argument_list|(
name|filterClass
argument_list|,
name|projectClass
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|relBuilderFactory
argument_list|)
expr_stmt|;
name|this
operator|.
name|onlyDeterministic
operator|=
name|onlyDeterministic
expr_stmt|;
name|this
operator|.
name|pushThroughWindowing
operator|=
name|pushThroughWindowing
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
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
specifier|final
name|Filter
name|filter
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|Project
name|origproject
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|RexNode
name|filterCondToPushBelowProj
init|=
name|filter
operator|.
name|getCondition
argument_list|()
decl_stmt|;
name|RexNode
name|unPushedFilCondAboveProj
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|RexUtil
operator|.
name|containsCorrelation
argument_list|(
name|filterCondToPushBelowProj
argument_list|)
condition|)
block|{
comment|// If there is a correlation condition anywhere in the filter, don't
comment|// push this filter past project since in some cases it can prevent a
comment|// Correlate from being de-correlated.
return|return;
block|}
if|if
condition|(
name|RexOver
operator|.
name|containsOver
argument_list|(
name|origproject
operator|.
name|getProjects
argument_list|()
argument_list|,
literal|null
argument_list|)
condition|)
block|{
name|RexNode
name|origFilterCond
init|=
name|filterCondToPushBelowProj
decl_stmt|;
name|filterCondToPushBelowProj
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|pushThroughWindowing
condition|)
block|{
name|Set
argument_list|<
name|Integer
argument_list|>
name|commonPartitionKeys
init|=
name|getCommonPartitionCols
argument_list|(
name|origproject
operator|.
name|getProjects
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RexNode
argument_list|>
name|newPartKeyFilConds
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RexNode
argument_list|>
name|unpushedFilConds
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
comment|// TODO:
comment|// 1) Handle compound partition keys (partition by k1+k2)
comment|// 2) When multiple window clauses are present in same select Even if
comment|// Predicate can not pushed past all of them, we might still able to
comment|// push
comment|// it below some of them.
comment|// Ex: select * from (select key, value, avg(c_int) over (partition by
comment|// key), sum(c_float) over(partition by value) from t1)t1 where value<
comment|// 10
comment|// --> select * from (select key, value, avg(c_int) over (partition by
comment|// key) from (select key, value, sum(c_float) over(partition by value)
comment|// from t1 where value< 10)t1)t2
if|if
condition|(
operator|!
name|commonPartitionKeys
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|RexNode
name|ce
range|:
name|RelOptUtil
operator|.
name|conjunctions
argument_list|(
name|origFilterCond
argument_list|)
control|)
block|{
name|RexNode
name|newCondition
init|=
name|RelOptUtil
operator|.
name|pushPastProject
argument_list|(
name|ce
argument_list|,
name|origproject
argument_list|)
decl_stmt|;
if|if
condition|(
name|HiveCalciteUtil
operator|.
name|isDeterministicFuncWithSingleInputRef
argument_list|(
name|newCondition
argument_list|,
name|commonPartitionKeys
argument_list|)
condition|)
block|{
name|newPartKeyFilConds
operator|.
name|add
argument_list|(
name|newCondition
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|unpushedFilConds
operator|.
name|add
argument_list|(
name|ce
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|newPartKeyFilConds
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|filterCondToPushBelowProj
operator|=
name|RexUtil
operator|.
name|composeConjunction
argument_list|(
name|filter
operator|.
name|getCluster
argument_list|()
operator|.
name|getRexBuilder
argument_list|()
argument_list|,
name|newPartKeyFilConds
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|unpushedFilConds
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|unPushedFilCondAboveProj
operator|=
name|RexUtil
operator|.
name|composeConjunction
argument_list|(
name|filter
operator|.
name|getCluster
argument_list|()
operator|.
name|getRexBuilder
argument_list|()
argument_list|,
name|unpushedFilConds
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|filterCondToPushBelowProj
operator|!=
literal|null
condition|)
block|{
name|RelNode
name|newProjRel
init|=
name|getNewProject
argument_list|(
name|filterCondToPushBelowProj
argument_list|,
name|unPushedFilCondAboveProj
argument_list|,
name|origproject
argument_list|,
name|filter
operator|.
name|getCluster
argument_list|()
operator|.
name|getTypeFactory
argument_list|()
argument_list|,
name|call
operator|.
name|builder
argument_list|()
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|newProjRel
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|RelNode
name|getNewProject
parameter_list|(
name|RexNode
name|filterCondToPushBelowProj
parameter_list|,
name|RexNode
name|unPushedFilCondAboveProj
parameter_list|,
name|Project
name|oldProj
parameter_list|,
name|RelDataTypeFactory
name|typeFactory
parameter_list|,
name|RelBuilder
name|relBuilder
parameter_list|)
block|{
comment|// convert the filter to one that references the child of the project
name|RexNode
name|newPushedCondition
init|=
name|RelOptUtil
operator|.
name|pushPastProject
argument_list|(
name|filterCondToPushBelowProj
argument_list|,
name|oldProj
argument_list|)
decl_stmt|;
comment|// Remove cast of BOOLEAN NOT NULL to BOOLEAN or vice versa. Filter accepts
comment|// nullable and not-nullable conditions, but a CAST might get in the way of
comment|// other rewrites.
if|if
condition|(
name|RexUtil
operator|.
name|isNullabilityCast
argument_list|(
name|typeFactory
argument_list|,
name|newPushedCondition
argument_list|)
condition|)
block|{
name|newPushedCondition
operator|=
operator|(
operator|(
name|RexCall
operator|)
name|newPushedCondition
operator|)
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|RelNode
name|newPushedFilterRel
init|=
name|relBuilder
operator|.
name|push
argument_list|(
name|oldProj
operator|.
name|getInput
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
name|newPushedCondition
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RelNode
name|newProjRel
init|=
name|relBuilder
operator|.
name|push
argument_list|(
name|newPushedFilterRel
argument_list|)
operator|.
name|project
argument_list|(
name|oldProj
operator|.
name|getProjects
argument_list|()
argument_list|,
name|oldProj
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldNames
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
if|if
condition|(
name|unPushedFilCondAboveProj
operator|!=
literal|null
condition|)
block|{
comment|// Remove cast of BOOLEAN NOT NULL to BOOLEAN or vice versa. Filter accepts
comment|// nullable and not-nullable conditions, but a CAST might get in the way of
comment|// other rewrites.
if|if
condition|(
name|RexUtil
operator|.
name|isNullabilityCast
argument_list|(
name|typeFactory
argument_list|,
name|newPushedCondition
argument_list|)
condition|)
block|{
name|unPushedFilCondAboveProj
operator|=
operator|(
operator|(
name|RexCall
operator|)
name|unPushedFilCondAboveProj
operator|)
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|newProjRel
operator|=
name|relBuilder
operator|.
name|push
argument_list|(
name|newProjRel
argument_list|)
operator|.
name|filter
argument_list|(
name|unPushedFilCondAboveProj
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
return|return
name|newProjRel
return|;
block|}
specifier|private
specifier|static
name|Set
argument_list|<
name|Integer
argument_list|>
name|getCommonPartitionCols
parameter_list|(
name|List
argument_list|<
name|RexNode
argument_list|>
name|projections
parameter_list|)
block|{
name|RexOver
name|overClause
decl_stmt|;
name|boolean
name|firstOverClause
init|=
literal|true
decl_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|commonPartitionKeys
init|=
operator|new
name|HashSet
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|RexNode
name|expr
range|:
name|projections
control|)
block|{
if|if
condition|(
name|expr
operator|instanceof
name|RexOver
condition|)
block|{
name|overClause
operator|=
operator|(
name|RexOver
operator|)
name|expr
expr_stmt|;
if|if
condition|(
name|firstOverClause
condition|)
block|{
name|firstOverClause
operator|=
literal|false
expr_stmt|;
name|commonPartitionKeys
operator|.
name|addAll
argument_list|(
name|getPartitionCols
argument_list|(
name|overClause
operator|.
name|getWindow
argument_list|()
operator|.
name|partitionKeys
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|commonPartitionKeys
operator|.
name|retainAll
argument_list|(
name|getPartitionCols
argument_list|(
name|overClause
operator|.
name|getWindow
argument_list|()
operator|.
name|partitionKeys
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|commonPartitionKeys
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|Integer
argument_list|>
name|getPartitionCols
parameter_list|(
name|List
argument_list|<
name|RexNode
argument_list|>
name|partitionKeys
parameter_list|)
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|pCols
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|RexNode
name|key
range|:
name|partitionKeys
control|)
block|{
if|if
condition|(
name|key
operator|instanceof
name|RexInputRef
condition|)
block|{
name|pCols
operator|.
name|add
argument_list|(
operator|(
operator|(
name|RexInputRef
operator|)
name|key
operator|)
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|pCols
return|;
block|}
block|}
end_class

end_unit

