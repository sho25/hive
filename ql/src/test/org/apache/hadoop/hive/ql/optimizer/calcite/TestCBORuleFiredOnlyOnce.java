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
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|jdbc
operator|.
name|JavaTypeFactoryImpl
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
name|RelOptCluster
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
name|RelOptRule
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
name|RelTraitSet
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
name|hep
operator|.
name|HepMatchOrder
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
name|hep
operator|.
name|HepPlanner
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
name|hep
operator|.
name|HepProgramBuilder
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
name|AbstractRelNode
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
name|metadata
operator|.
name|CachingRelMetadataProvider
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
name|metadata
operator|.
name|ChainedRelMetadataProvider
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
name|metadata
operator|.
name|RelMetadataProvider
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
name|RelDataType
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
name|RelDataTypeField
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
name|RelRecordType
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
name|RexBuilder
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
name|conf
operator|.
name|HiveConf
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
name|rules
operator|.
name|HiveRulesRegistry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
specifier|public
class|class
name|TestCBORuleFiredOnlyOnce
block|{
annotation|@
name|Test
specifier|public
name|void
name|testRuleFiredOnlyOnce
parameter_list|()
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
comment|// Create HepPlanner
name|HepProgramBuilder
name|programBuilder
init|=
operator|new
name|HepProgramBuilder
argument_list|()
decl_stmt|;
name|programBuilder
operator|.
name|addMatchOrder
argument_list|(
name|HepMatchOrder
operator|.
name|TOP_DOWN
argument_list|)
expr_stmt|;
name|programBuilder
operator|=
name|programBuilder
operator|.
name|addRuleCollection
argument_list|(
name|ImmutableList
operator|.
expr|<
name|RelOptRule
operator|>
name|of
argument_list|(
name|DummyRule
operator|.
name|INSTANCE
argument_list|)
argument_list|)
expr_stmt|;
comment|// Create rules registry to not trigger a rule more than once
name|HiveRulesRegistry
name|registry
init|=
operator|new
name|HiveRulesRegistry
argument_list|()
decl_stmt|;
name|HivePlannerContext
name|context
init|=
operator|new
name|HivePlannerContext
argument_list|(
literal|null
argument_list|,
name|registry
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|HepPlanner
name|planner
init|=
operator|new
name|HepPlanner
argument_list|(
name|programBuilder
operator|.
name|build
argument_list|()
argument_list|,
name|context
argument_list|)
decl_stmt|;
comment|// Cluster
name|RexBuilder
name|rexBuilder
init|=
operator|new
name|RexBuilder
argument_list|(
operator|new
name|JavaTypeFactoryImpl
argument_list|()
argument_list|)
decl_stmt|;
name|RelOptCluster
name|cluster
init|=
name|RelOptCluster
operator|.
name|create
argument_list|(
name|planner
argument_list|,
name|rexBuilder
argument_list|)
decl_stmt|;
comment|// Create MD provider
name|HiveDefaultRelMetadataProvider
name|mdProvider
init|=
operator|new
name|HiveDefaultRelMetadataProvider
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RelMetadataProvider
argument_list|>
name|list
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|mdProvider
operator|.
name|getMetadataProvider
argument_list|()
argument_list|)
expr_stmt|;
name|planner
operator|.
name|registerMetadataProviders
argument_list|(
name|list
argument_list|)
expr_stmt|;
name|RelMetadataProvider
name|chainedProvider
init|=
name|ChainedRelMetadataProvider
operator|.
name|of
argument_list|(
name|list
argument_list|)
decl_stmt|;
specifier|final
name|RelNode
name|node
init|=
operator|new
name|DummyNode
argument_list|(
name|cluster
argument_list|,
name|cluster
operator|.
name|traitSet
argument_list|()
argument_list|)
decl_stmt|;
name|node
operator|.
name|getCluster
argument_list|()
operator|.
name|setMetadataProvider
argument_list|(
operator|new
name|CachingRelMetadataProvider
argument_list|(
name|chainedProvider
argument_list|,
name|planner
argument_list|)
argument_list|)
expr_stmt|;
name|planner
operator|.
name|setRoot
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|planner
operator|.
name|findBestExp
argument_list|()
expr_stmt|;
comment|// Matches 3 times: 2 times the original node, 1 time the new node created by the rule
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|DummyRule
operator|.
name|INSTANCE
operator|.
name|numberMatches
argument_list|)
expr_stmt|;
comment|// It is fired only once: on the original node
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|DummyRule
operator|.
name|INSTANCE
operator|.
name|numberOnMatch
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|DummyRule
extends|extends
name|RelOptRule
block|{
specifier|public
specifier|static
specifier|final
name|DummyRule
name|INSTANCE
init|=
operator|new
name|DummyRule
argument_list|()
decl_stmt|;
specifier|public
name|int
name|numberMatches
decl_stmt|;
specifier|public
name|int
name|numberOnMatch
decl_stmt|;
specifier|private
name|DummyRule
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|RelNode
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|numberMatches
operator|=
literal|0
expr_stmt|;
name|numberOnMatch
operator|=
literal|0
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
name|RelNode
name|node
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|numberMatches
operator|++
expr_stmt|;
name|HiveRulesRegistry
name|registry
init|=
name|call
operator|.
name|getPlanner
argument_list|()
operator|.
name|getContext
argument_list|()
operator|.
name|unwrap
argument_list|(
name|HiveRulesRegistry
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// If this operator has been visited already by the rule,
comment|// we do not need to apply the optimization
if|if
condition|(
name|registry
operator|!=
literal|null
operator|&&
name|registry
operator|.
name|getVisited
argument_list|(
name|this
argument_list|)
operator|.
name|contains
argument_list|(
name|node
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onMatch
parameter_list|(
name|RelOptRuleCall
name|call
parameter_list|)
block|{
specifier|final
name|RelNode
name|node
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|numberOnMatch
operator|++
expr_stmt|;
comment|// If we have fired it already once, we return and the test will fail
if|if
condition|(
name|numberOnMatch
operator|>
literal|1
condition|)
block|{
return|return;
block|}
comment|// Register that we have visited this operator in this rule
name|HiveRulesRegistry
name|registry
init|=
name|call
operator|.
name|getPlanner
argument_list|()
operator|.
name|getContext
argument_list|()
operator|.
name|unwrap
argument_list|(
name|HiveRulesRegistry
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|registry
operator|!=
literal|null
condition|)
block|{
name|registry
operator|.
name|registerVisited
argument_list|(
name|this
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
comment|// We create a new op if it is the first time we fire the rule
specifier|final
name|RelNode
name|newNode
init|=
operator|new
name|DummyNode
argument_list|(
name|node
operator|.
name|getCluster
argument_list|()
argument_list|,
name|node
operator|.
name|getTraitSet
argument_list|()
argument_list|)
decl_stmt|;
comment|// We register it so we do not fire the rule on it again
if|if
condition|(
name|registry
operator|!=
literal|null
condition|)
block|{
name|registry
operator|.
name|registerVisited
argument_list|(
name|this
argument_list|,
name|newNode
argument_list|)
expr_stmt|;
block|}
name|call
operator|.
name|transformTo
argument_list|(
name|newNode
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|DummyNode
extends|extends
name|AbstractRelNode
block|{
specifier|protected
name|DummyNode
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelTraitSet
name|traits
parameter_list|)
block|{
name|super
argument_list|(
name|cluster
argument_list|,
name|cluster
operator|.
name|traitSet
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|RelDataType
name|deriveRowType
parameter_list|()
block|{
return|return
operator|new
name|RelRecordType
argument_list|(
name|Lists
operator|.
expr|<
name|RelDataTypeField
operator|>
name|newArrayList
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

