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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashSet
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
name|RelFactories
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
name|RexPermuteInputsShuttle
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
name|RexVisitor
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
name|sql
operator|.
name|validate
operator|.
name|SqlValidator
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
name|sql2rel
operator|.
name|RelFieldTrimmer
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
name|util
operator|.
name|ImmutableBitSet
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
name|util
operator|.
name|mapping
operator|.
name|IntPair
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
name|util
operator|.
name|mapping
operator|.
name|Mapping
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
name|util
operator|.
name|mapping
operator|.
name|MappingType
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
name|util
operator|.
name|mapping
operator|.
name|Mappings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|HiveMultiJoin
import|;
end_import

begin_class
specifier|public
class|class
name|HiveRelFieldTrimmer
extends|extends
name|RelFieldTrimmer
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HiveRelFieldTrimmer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|HiveRelFieldTrimmer
parameter_list|(
name|SqlValidator
name|validator
parameter_list|,
name|RelOptCluster
name|cluster
parameter_list|,
name|RelFactories
operator|.
name|ProjectFactory
name|projectFactory
parameter_list|,
name|RelFactories
operator|.
name|FilterFactory
name|filterFactory
parameter_list|,
name|RelFactories
operator|.
name|JoinFactory
name|joinFactory
parameter_list|,
name|RelFactories
operator|.
name|SemiJoinFactory
name|semiJoinFactory
parameter_list|,
name|RelFactories
operator|.
name|SortFactory
name|sortFactory
parameter_list|,
name|RelFactories
operator|.
name|AggregateFactory
name|aggregateFactory
parameter_list|,
name|RelFactories
operator|.
name|SetOpFactory
name|setOpFactory
parameter_list|)
block|{
name|super
argument_list|(
name|validator
argument_list|,
name|RelBuilder
operator|.
name|proto
argument_list|(
name|projectFactory
argument_list|,
name|filterFactory
argument_list|,
name|joinFactory
argument_list|,
name|semiJoinFactory
argument_list|,
name|sortFactory
argument_list|,
name|aggregateFactory
argument_list|,
name|setOpFactory
argument_list|)
operator|.
name|create
argument_list|(
name|cluster
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Variant of {@link #trimFields(RelNode, ImmutableBitSet, Set)} for    * {@link org.apache.hadoop.hive.ql.optimizer.calcite.reloperators.HiveMultiJoin}.    */
specifier|public
name|TrimResult
name|trimFields
parameter_list|(
name|HiveMultiJoin
name|join
parameter_list|,
name|ImmutableBitSet
name|fieldsUsed
parameter_list|,
name|Set
argument_list|<
name|RelDataTypeField
argument_list|>
name|extraFields
parameter_list|)
block|{
specifier|final
name|int
name|fieldCount
init|=
name|join
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldCount
argument_list|()
decl_stmt|;
specifier|final
name|RexNode
name|conditionExpr
init|=
name|join
operator|.
name|getCondition
argument_list|()
decl_stmt|;
comment|// Add in fields used in the condition.
specifier|final
name|Set
argument_list|<
name|RelDataTypeField
argument_list|>
name|combinedInputExtraFields
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|RelDataTypeField
argument_list|>
argument_list|(
name|extraFields
argument_list|)
decl_stmt|;
name|RelOptUtil
operator|.
name|InputFinder
name|inputFinder
init|=
operator|new
name|RelOptUtil
operator|.
name|InputFinder
argument_list|(
name|combinedInputExtraFields
argument_list|)
decl_stmt|;
name|inputFinder
operator|.
name|inputBitSet
operator|.
name|addAll
argument_list|(
name|fieldsUsed
argument_list|)
expr_stmt|;
name|conditionExpr
operator|.
name|accept
argument_list|(
name|inputFinder
argument_list|)
expr_stmt|;
specifier|final
name|ImmutableBitSet
name|fieldsUsedPlus
init|=
name|inputFinder
operator|.
name|inputBitSet
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|inputStartPos
init|=
literal|0
decl_stmt|;
name|int
name|changeCount
init|=
literal|0
decl_stmt|;
name|int
name|newFieldCount
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|RelNode
argument_list|>
name|newInputs
init|=
operator|new
name|ArrayList
argument_list|<
name|RelNode
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Mapping
argument_list|>
name|inputMappings
init|=
operator|new
name|ArrayList
argument_list|<
name|Mapping
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|RelNode
name|input
range|:
name|join
operator|.
name|getInputs
argument_list|()
control|)
block|{
specifier|final
name|RelDataType
name|inputRowType
init|=
name|input
operator|.
name|getRowType
argument_list|()
decl_stmt|;
specifier|final
name|int
name|inputFieldCount
init|=
name|inputRowType
operator|.
name|getFieldCount
argument_list|()
decl_stmt|;
comment|// Compute required mapping.
name|ImmutableBitSet
operator|.
name|Builder
name|inputFieldsUsed
init|=
name|ImmutableBitSet
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|bit
range|:
name|fieldsUsedPlus
control|)
block|{
if|if
condition|(
name|bit
operator|>=
name|inputStartPos
operator|&&
name|bit
operator|<
name|inputStartPos
operator|+
name|inputFieldCount
condition|)
block|{
name|inputFieldsUsed
operator|.
name|set
argument_list|(
name|bit
operator|-
name|inputStartPos
argument_list|)
expr_stmt|;
block|}
block|}
name|Set
argument_list|<
name|RelDataTypeField
argument_list|>
name|inputExtraFields
init|=
name|Collections
operator|.
expr|<
name|RelDataTypeField
operator|>
name|emptySet
argument_list|()
decl_stmt|;
name|TrimResult
name|trimResult
init|=
name|trimChild
argument_list|(
name|join
argument_list|,
name|input
argument_list|,
name|inputFieldsUsed
operator|.
name|build
argument_list|()
argument_list|,
name|inputExtraFields
argument_list|)
decl_stmt|;
name|newInputs
operator|.
name|add
argument_list|(
name|trimResult
operator|.
name|left
argument_list|)
expr_stmt|;
if|if
condition|(
name|trimResult
operator|.
name|left
operator|!=
name|input
condition|)
block|{
operator|++
name|changeCount
expr_stmt|;
block|}
specifier|final
name|Mapping
name|inputMapping
init|=
name|trimResult
operator|.
name|right
decl_stmt|;
name|inputMappings
operator|.
name|add
argument_list|(
name|inputMapping
argument_list|)
expr_stmt|;
comment|// Move offset to point to start of next input.
name|inputStartPos
operator|+=
name|inputFieldCount
expr_stmt|;
name|newFieldCount
operator|+=
name|inputMapping
operator|.
name|getTargetCount
argument_list|()
expr_stmt|;
block|}
name|Mapping
name|mapping
init|=
name|Mappings
operator|.
name|create
argument_list|(
name|MappingType
operator|.
name|INVERSE_SURJECTION
argument_list|,
name|fieldCount
argument_list|,
name|newFieldCount
argument_list|)
decl_stmt|;
name|int
name|offset
init|=
literal|0
decl_stmt|;
name|int
name|newOffset
init|=
literal|0
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
name|inputMappings
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Mapping
name|inputMapping
init|=
name|inputMappings
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
for|for
control|(
name|IntPair
name|pair
range|:
name|inputMapping
control|)
block|{
name|mapping
operator|.
name|set
argument_list|(
name|pair
operator|.
name|source
operator|+
name|offset
argument_list|,
name|pair
operator|.
name|target
operator|+
name|newOffset
argument_list|)
expr_stmt|;
block|}
name|offset
operator|+=
name|inputMapping
operator|.
name|getSourceCount
argument_list|()
expr_stmt|;
name|newOffset
operator|+=
name|inputMapping
operator|.
name|getTargetCount
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|changeCount
operator|==
literal|0
operator|&&
name|mapping
operator|.
name|isIdentity
argument_list|()
condition|)
block|{
return|return
operator|new
name|TrimResult
argument_list|(
name|join
argument_list|,
name|Mappings
operator|.
name|createIdentity
argument_list|(
name|fieldCount
argument_list|)
argument_list|)
return|;
block|}
comment|// Build new join.
specifier|final
name|RexVisitor
argument_list|<
name|RexNode
argument_list|>
name|shuttle
init|=
operator|new
name|RexPermuteInputsShuttle
argument_list|(
name|mapping
argument_list|,
name|newInputs
operator|.
name|toArray
argument_list|(
operator|new
name|RelNode
index|[
name|newInputs
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|RexNode
name|newConditionExpr
init|=
name|conditionExpr
operator|.
name|accept
argument_list|(
name|shuttle
argument_list|)
decl_stmt|;
specifier|final
name|RelDataType
name|newRowType
init|=
name|RelOptUtil
operator|.
name|permute
argument_list|(
name|join
operator|.
name|getCluster
argument_list|()
operator|.
name|getTypeFactory
argument_list|()
argument_list|,
name|join
operator|.
name|getRowType
argument_list|()
argument_list|,
name|mapping
argument_list|)
decl_stmt|;
specifier|final
name|RelNode
name|newJoin
init|=
operator|new
name|HiveMultiJoin
argument_list|(
name|join
operator|.
name|getCluster
argument_list|()
argument_list|,
name|newInputs
argument_list|,
name|newConditionExpr
argument_list|,
name|newRowType
argument_list|,
name|join
operator|.
name|getJoinInputs
argument_list|()
argument_list|,
name|join
operator|.
name|getJoinTypes
argument_list|()
argument_list|,
name|join
operator|.
name|getJoinFilters
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|TrimResult
argument_list|(
name|newJoin
argument_list|,
name|mapping
argument_list|)
return|;
block|}
block|}
end_class

end_unit

