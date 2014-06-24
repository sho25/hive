begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0    *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.   */
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
package|;
end_package

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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
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

begin_import
import|import
name|net
operator|.
name|hydromatic
operator|.
name|optiq
operator|.
name|SchemaPlus
import|;
end_import

begin_import
import|import
name|net
operator|.
name|hydromatic
operator|.
name|optiq
operator|.
name|tools
operator|.
name|Frameworks
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|QueryProperties
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
name|exec
operator|.
name|Operator
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
name|HiveDefaultRelMetadataProvider
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
name|HiveVolcanoPlanner
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
name|reloperators
operator|.
name|HiveRel
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
name|rules
operator|.
name|HiveMergeProjectRule
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
name|rules
operator|.
name|HivePullUpProjectsAboveJoinRule
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
name|rules
operator|.
name|HivePushJoinThroughJoinRule
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
name|rules
operator|.
name|HiveSwapJoinRule
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
name|translator
operator|.
name|ASTConverter
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
name|translator
operator|.
name|RelNodeConverter
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
name|parse
operator|.
name|ASTNode
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
name|parse
operator|.
name|ParseContext
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
name|parse
operator|.
name|SemanticAnalyzer
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
name|plan
operator|.
name|api
operator|.
name|OperatorType
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
name|RelCollationImpl
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
name|metadata
operator|.
name|CachingRelMetadataProvider
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
name|metadata
operator|.
name|ChainedRelMetadataProvider
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
name|metadata
operator|.
name|RelMetadataProvider
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
name|RelOptQuery
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
name|RelOptSchema
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
name|RexBuilder
import|;
end_import

begin_comment
comment|/*   * Entry point to Optimizations using Optiq.    */
end_comment

begin_class
specifier|public
class|class
name|CostBasedOptimizer
implements|implements
name|Frameworks
operator|.
name|PlannerAction
argument_list|<
name|RelNode
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|OperatorType
argument_list|>
name|m_unsupportedOpTypes
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
name|OperatorType
operator|.
name|DEMUX
argument_list|,
name|OperatorType
operator|.
name|FORWARD
argument_list|,
name|OperatorType
operator|.
name|LATERALVIEWFORWARD
argument_list|,
name|OperatorType
operator|.
name|LATERALVIEWJOIN
argument_list|,
name|OperatorType
operator|.
name|MUX
argument_list|,
name|OperatorType
operator|.
name|PTF
argument_list|,
name|OperatorType
operator|.
name|SCRIPT
argument_list|,
name|OperatorType
operator|.
name|UDTF
argument_list|,
name|OperatorType
operator|.
name|UNION
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|private
specifier|final
name|Operator
name|m_sinkOp
decl_stmt|;
specifier|private
specifier|final
name|SemanticAnalyzer
name|m_semanticAnalyzer
decl_stmt|;
specifier|private
specifier|final
name|ParseContext
name|m_ParseContext
decl_stmt|;
specifier|public
name|CostBasedOptimizer
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|Operator
name|sinkOp
parameter_list|,
name|SemanticAnalyzer
name|semanticAnalyzer
parameter_list|,
name|ParseContext
name|pCtx
parameter_list|)
block|{
name|m_sinkOp
operator|=
name|sinkOp
expr_stmt|;
name|m_semanticAnalyzer
operator|=
name|semanticAnalyzer
expr_stmt|;
name|m_ParseContext
operator|=
name|pCtx
expr_stmt|;
block|}
comment|/*    * Currently contract is given a Hive Operator Tree, it returns an optimal    * plan as an Hive AST.    */
specifier|public
specifier|static
name|ASTNode
name|optimize
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|Operator
name|sinkOp
parameter_list|,
name|SemanticAnalyzer
name|semanticAnalyzer
parameter_list|,
name|ParseContext
name|pCtx
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|resultSchema
parameter_list|)
block|{
name|ASTNode
name|optiqOptimizedAST
init|=
literal|null
decl_stmt|;
name|RelNode
name|optimizedOptiqPlan
init|=
name|Frameworks
operator|.
name|withPlanner
argument_list|(
operator|new
name|CostBasedOptimizer
argument_list|(
name|sinkOp
argument_list|,
name|semanticAnalyzer
argument_list|,
name|pCtx
argument_list|)
argument_list|)
decl_stmt|;
name|optiqOptimizedAST
operator|=
name|ASTConverter
operator|.
name|convert
argument_list|(
name|optimizedOptiqPlan
argument_list|,
name|resultSchema
argument_list|)
expr_stmt|;
return|return
name|optiqOptimizedAST
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|RelNode
name|apply
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelOptSchema
name|relOptSchema
parameter_list|,
name|SchemaPlus
name|schema
parameter_list|)
block|{
name|RelOptPlanner
name|planner
init|=
name|HiveVolcanoPlanner
operator|.
name|createPlanner
argument_list|()
decl_stmt|;
comment|/*      * recreate cluster, so that it picks up the additional traitDef      */
specifier|final
name|RelOptQuery
name|query
init|=
operator|new
name|RelOptQuery
argument_list|(
name|planner
argument_list|)
decl_stmt|;
specifier|final
name|RexBuilder
name|rexBuilder
init|=
name|cluster
operator|.
name|getRexBuilder
argument_list|()
decl_stmt|;
name|cluster
operator|=
name|query
operator|.
name|createCluster
argument_list|(
name|rexBuilder
operator|.
name|getTypeFactory
argument_list|()
argument_list|,
name|rexBuilder
argument_list|)
expr_stmt|;
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
name|HiveDefaultRelMetadataProvider
operator|.
name|INSTANCE
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
name|cluster
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
name|RelNode
name|opTreeInOptiq
init|=
name|RelNodeConverter
operator|.
name|convert
argument_list|(
name|m_sinkOp
argument_list|,
name|cluster
argument_list|,
name|relOptSchema
argument_list|,
name|m_semanticAnalyzer
argument_list|,
name|m_ParseContext
argument_list|)
decl_stmt|;
name|planner
operator|.
name|clearRules
argument_list|()
expr_stmt|;
name|planner
operator|.
name|addRule
argument_list|(
name|HiveSwapJoinRule
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
name|planner
operator|.
name|addRule
argument_list|(
name|HivePushJoinThroughJoinRule
operator|.
name|LEFT
argument_list|)
expr_stmt|;
name|planner
operator|.
name|addRule
argument_list|(
name|HivePushJoinThroughJoinRule
operator|.
name|RIGHT
argument_list|)
expr_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|m_ParseContext
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_CBO_PULLPROJECTABOVEJOIN_RULE
argument_list|)
condition|)
block|{
name|planner
operator|.
name|addRule
argument_list|(
name|HivePullUpProjectsAboveJoinRule
operator|.
name|BOTH_PROJECT
argument_list|)
expr_stmt|;
name|planner
operator|.
name|addRule
argument_list|(
name|HivePullUpProjectsAboveJoinRule
operator|.
name|LEFT_PROJECT
argument_list|)
expr_stmt|;
name|planner
operator|.
name|addRule
argument_list|(
name|HivePullUpProjectsAboveJoinRule
operator|.
name|RIGHT_PROJECT
argument_list|)
expr_stmt|;
name|planner
operator|.
name|addRule
argument_list|(
name|HiveMergeProjectRule
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
name|RelTraitSet
name|desiredTraits
init|=
name|cluster
operator|.
name|traitSetOf
argument_list|(
name|HiveRel
operator|.
name|CONVENTION
argument_list|,
name|RelCollationImpl
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|RelNode
name|rootRel
init|=
name|opTreeInOptiq
decl_stmt|;
if|if
condition|(
operator|!
name|rootRel
operator|.
name|getTraitSet
argument_list|()
operator|.
name|equals
argument_list|(
name|desiredTraits
argument_list|)
condition|)
block|{
name|rootRel
operator|=
name|planner
operator|.
name|changeTraits
argument_list|(
name|opTreeInOptiq
argument_list|,
name|desiredTraits
argument_list|)
expr_stmt|;
block|}
name|planner
operator|.
name|setRoot
argument_list|(
name|rootRel
argument_list|)
expr_stmt|;
return|return
name|planner
operator|.
name|findBestExp
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|boolean
name|canHandleOpTree
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|Operator
name|sinkOp
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|QueryProperties
name|qp
parameter_list|)
block|{
name|boolean
name|runOptiq
init|=
literal|false
decl_stmt|;
if|if
condition|(
operator|(
name|qp
operator|.
name|getJoinCount
argument_list|()
operator|>
literal|1
operator|)
operator|&&
operator|(
name|qp
operator|.
name|getJoinCount
argument_list|()
operator|<
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_CBO_MAX_JOINS_SUPPORTED
argument_list|)
operator|)
operator|&&
operator|(
name|qp
operator|.
name|getOuterJoinCount
argument_list|()
operator|==
literal|0
operator|)
operator|&&
operator|!
name|qp
operator|.
name|hasClusterBy
argument_list|()
operator|&&
operator|!
name|qp
operator|.
name|hasDistributeBy
argument_list|()
operator|&&
operator|!
name|qp
operator|.
name|hasSortBy
argument_list|()
operator|&&
operator|!
name|qp
operator|.
name|hasWindowing
argument_list|()
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|final
name|HashSet
argument_list|<
name|Operator
argument_list|>
name|start
init|=
operator|new
name|HashSet
argument_list|<
name|Operator
argument_list|>
argument_list|()
decl_stmt|;
name|start
operator|.
name|add
argument_list|(
name|sinkOp
argument_list|)
expr_stmt|;
comment|// TODO: use queryproperties instead of walking the tree
if|if
condition|(
operator|!
name|CostBasedOptimizer
operator|.
name|operatorExists
argument_list|(
name|start
argument_list|,
literal|true
argument_list|,
name|m_unsupportedOpTypes
argument_list|)
condition|)
block|{
name|runOptiq
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|runOptiq
return|;
block|}
comment|/*    * TODO: moved this out of OperatorUtils for now HIVE-6403 is going to bring    * in iterateParents: https://reviews.apache.org/r/18137/diff/#index_header    * Will just use/enhance that once it is in. hb 2/15    */
comment|/**    * Check if operator tree, in the direction specified forward/backward,    * contains any operator specified in the targetOPTypes.    *     * @param start    *          list of operators to start checking from    * @param backward    *          direction of DAG traversal; if true implies get parent ops for    *          traversal otherwise children will be used    * @param targetOPTypes    *          Set of operator types to look for    *     * @return true if any of the operator or its parent/children is of the name    *         specified in the targetOPTypes    *     *         NOTE: 1. This employs breadth first search 2. By using HashSet for    *         "start" we avoid revisiting same operator twice. However it doesn't    *         prevent revisiting the same node more than once for some complex    *         dags.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
name|boolean
name|operatorExists
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|final
name|HashSet
argument_list|<
name|Operator
argument_list|>
name|start
parameter_list|,
specifier|final
name|boolean
name|backward
parameter_list|,
specifier|final
name|Set
argument_list|<
name|OperatorType
argument_list|>
name|targetOPTypes
parameter_list|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|HashSet
argument_list|<
name|Operator
argument_list|>
name|nextSetOfOperators
init|=
operator|new
name|HashSet
argument_list|<
name|Operator
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|Operator
name|op
range|:
name|start
control|)
block|{
if|if
condition|(
name|targetOPTypes
operator|.
name|contains
argument_list|(
name|op
operator|.
name|getType
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|backward
condition|)
block|{
if|if
condition|(
name|op
operator|.
name|getParentOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|nextSetOfOperators
operator|.
name|addAll
argument_list|(
name|op
operator|.
name|getParentOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|op
operator|.
name|getChildOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|nextSetOfOperators
operator|.
name|addAll
argument_list|(
name|op
operator|.
name|getChildOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|nextSetOfOperators
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|operatorExists
argument_list|(
name|nextSetOfOperators
argument_list|,
name|backward
argument_list|,
name|targetOPTypes
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

