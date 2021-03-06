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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
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
name|RelShuttle
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
name|metadata
operator|.
name|RelMetadataQuery
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
name|RexCorrelVariable
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
name|RexFieldAccess
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
name|RexSubQuery
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
name|HiveRelShuttle
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
name|TraitsUtil
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
name|CorrelationId
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
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_class
specifier|public
class|class
name|HiveFilter
extends|extends
name|Filter
implements|implements
name|HiveRelNode
block|{
specifier|public
specifier|static
class|class
name|StatEnhancedHiveFilter
extends|extends
name|HiveFilter
block|{
specifier|private
name|long
name|rowCount
decl_stmt|;
comment|// FIXME: use a generic proxy wrapper to create runtimestat enhanced nodes
specifier|public
name|StatEnhancedHiveFilter
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelTraitSet
name|traits
parameter_list|,
name|RelNode
name|child
parameter_list|,
name|RexNode
name|condition
parameter_list|,
name|long
name|rowCount
parameter_list|)
block|{
name|super
argument_list|(
name|cluster
argument_list|,
name|traits
argument_list|,
name|child
argument_list|,
name|condition
argument_list|)
expr_stmt|;
name|this
operator|.
name|rowCount
operator|=
name|rowCount
expr_stmt|;
block|}
specifier|public
name|long
name|getRowCount
parameter_list|()
block|{
return|return
name|rowCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|estimateRowCount
parameter_list|(
name|RelMetadataQuery
name|mq
parameter_list|)
block|{
return|return
name|rowCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|Filter
name|copy
parameter_list|(
name|RelTraitSet
name|traitSet
parameter_list|,
name|RelNode
name|input
parameter_list|,
name|RexNode
name|condition
parameter_list|)
block|{
assert|assert
name|traitSet
operator|.
name|containsIfApplicable
argument_list|(
name|HiveRelNode
operator|.
name|CONVENTION
argument_list|)
assert|;
return|return
operator|new
name|StatEnhancedHiveFilter
argument_list|(
name|getCluster
argument_list|()
argument_list|,
name|traitSet
argument_list|,
name|input
argument_list|,
name|condition
argument_list|,
name|rowCount
argument_list|)
return|;
block|}
block|}
specifier|public
name|HiveFilter
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelTraitSet
name|traits
parameter_list|,
name|RelNode
name|child
parameter_list|,
name|RexNode
name|condition
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
argument_list|,
name|condition
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Filter
name|copy
parameter_list|(
name|RelTraitSet
name|traitSet
parameter_list|,
name|RelNode
name|input
parameter_list|,
name|RexNode
name|condition
parameter_list|)
block|{
assert|assert
name|traitSet
operator|.
name|containsIfApplicable
argument_list|(
name|HiveRelNode
operator|.
name|CONVENTION
argument_list|)
assert|;
return|return
operator|new
name|HiveFilter
argument_list|(
name|getCluster
argument_list|()
argument_list|,
name|traitSet
argument_list|,
name|input
argument_list|,
name|condition
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|implement
parameter_list|(
name|Implementor
name|implementor
parameter_list|)
block|{   }
specifier|private
specifier|static
name|void
name|findCorrelatedVar
parameter_list|(
name|RexNode
name|node
parameter_list|,
name|Set
argument_list|<
name|CorrelationId
argument_list|>
name|allVars
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|RexCall
condition|)
block|{
name|RexCall
name|nd
init|=
operator|(
name|RexCall
operator|)
name|node
decl_stmt|;
for|for
control|(
name|RexNode
name|rn
range|:
name|nd
operator|.
name|getOperands
argument_list|()
control|)
block|{
if|if
condition|(
name|rn
operator|instanceof
name|RexFieldAccess
condition|)
block|{
specifier|final
name|RexNode
name|ref
init|=
operator|(
operator|(
name|RexFieldAccess
operator|)
name|rn
operator|)
operator|.
name|getReferenceExpr
argument_list|()
decl_stmt|;
if|if
condition|(
name|ref
operator|instanceof
name|RexCorrelVariable
condition|)
block|{
name|allVars
operator|.
name|add
argument_list|(
operator|(
operator|(
name|RexCorrelVariable
operator|)
name|ref
operator|)
operator|.
name|id
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|findCorrelatedVar
argument_list|(
name|rn
argument_list|,
name|allVars
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|//traverse the given node to find all correlated variables
comment|// Note that correlated variables are supported in Filter only i.e. Where& Having
specifier|private
specifier|static
name|void
name|traverseFilter
parameter_list|(
name|RexNode
name|node
parameter_list|,
name|Set
argument_list|<
name|CorrelationId
argument_list|>
name|allVars
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|RexSubQuery
condition|)
block|{
comment|//we expect correlated variables in HiveFilter only for now.
comment|// Also check for case where operator has 0 inputs .e.g TableScan
name|RelNode
name|input
init|=
operator|(
operator|(
name|RexSubQuery
operator|)
name|node
operator|)
operator|.
name|rel
operator|.
name|getInput
argument_list|(
literal|0
argument_list|)
decl_stmt|;
while|while
condition|(
name|input
operator|!=
literal|null
operator|&&
operator|!
operator|(
name|input
operator|instanceof
name|HiveFilter
operator|)
operator|&&
name|input
operator|.
name|getInputs
argument_list|()
operator|.
name|size
argument_list|()
operator|>=
literal|1
condition|)
block|{
comment|//we don't expect corr vars withing JOIN or UNION for now
comment|// we only expect cor vars in top level filter
if|if
condition|(
name|input
operator|.
name|getInputs
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
return|return;
block|}
name|input
operator|=
name|input
operator|.
name|getInput
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|input
operator|!=
literal|null
operator|&&
name|input
operator|instanceof
name|HiveFilter
condition|)
block|{
name|findCorrelatedVar
argument_list|(
operator|(
operator|(
name|HiveFilter
operator|)
name|input
operator|)
operator|.
name|getCondition
argument_list|()
argument_list|,
name|allVars
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|//AND, NOT etc
if|if
condition|(
name|node
operator|instanceof
name|RexCall
condition|)
block|{
name|int
name|numOperands
init|=
operator|(
operator|(
name|RexCall
operator|)
name|node
operator|)
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numOperands
condition|;
name|i
operator|++
control|)
block|{
name|RexNode
name|op
init|=
operator|(
operator|(
name|RexCall
operator|)
name|node
operator|)
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|traverseFilter
argument_list|(
name|op
argument_list|,
name|allVars
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|CorrelationId
argument_list|>
name|getVariablesSet
parameter_list|()
block|{
name|Set
argument_list|<
name|CorrelationId
argument_list|>
name|allCorrVars
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|traverseFilter
argument_list|(
name|condition
argument_list|,
name|allCorrVars
argument_list|)
expr_stmt|;
return|return
name|allCorrVars
return|;
block|}
specifier|public
specifier|static
name|Set
argument_list|<
name|CorrelationId
argument_list|>
name|getVariablesSet
parameter_list|(
name|RexSubQuery
name|e
parameter_list|)
block|{
name|Set
argument_list|<
name|CorrelationId
argument_list|>
name|allCorrVars
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|traverseFilter
argument_list|(
name|e
argument_list|,
name|allCorrVars
argument_list|)
expr_stmt|;
return|return
name|allCorrVars
return|;
block|}
annotation|@
name|Override
specifier|public
name|RelNode
name|accept
parameter_list|(
name|RelShuttle
name|shuttle
parameter_list|)
block|{
if|if
condition|(
name|shuttle
operator|instanceof
name|HiveRelShuttle
condition|)
block|{
return|return
operator|(
operator|(
name|HiveRelShuttle
operator|)
name|shuttle
operator|)
operator|.
name|visit
argument_list|(
name|this
argument_list|)
return|;
block|}
return|return
name|shuttle
operator|.
name|visit
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
end_class

end_unit

