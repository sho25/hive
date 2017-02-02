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
name|translator
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
name|adapter
operator|.
name|druid
operator|.
name|DruidQuery
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
name|plan
operator|.
name|hep
operator|.
name|HepRelVertex
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
name|volcano
operator|.
name|RelSubset
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
name|SingleRel
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
name|Aggregate
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
name|AggregateCall
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
name|Join
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
name|SetOp
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
name|Sort
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
name|Window
operator|.
name|RexWinAggCall
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
name|MultiJoin
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
name|sql
operator|.
name|SqlAggFunction
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
name|Pair
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
name|optimizer
operator|.
name|calcite
operator|.
name|CalciteSemanticException
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
name|HiveAggregate
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
name|HiveSortLimit
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
name|HiveTableScan
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
name|HiveRelColumnsAlignment
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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

begin_class
specifier|public
class|class
name|PlanModifierForASTConv
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|PlanModifierForASTConv
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|RelNode
name|convertOpTree
parameter_list|(
name|RelNode
name|rel
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|resultSchema
parameter_list|,
name|boolean
name|alignColumns
parameter_list|)
throws|throws
name|CalciteSemanticException
block|{
name|RelNode
name|newTopNode
init|=
name|rel
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Original plan for PlanModifier\n "
operator|+
name|RelOptUtil
operator|.
name|toString
argument_list|(
name|newTopNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
operator|(
name|newTopNode
operator|instanceof
name|Project
operator|)
operator|&&
operator|!
operator|(
name|newTopNode
operator|instanceof
name|Sort
operator|)
condition|)
block|{
name|newTopNode
operator|=
name|introduceDerivedTable
argument_list|(
name|newTopNode
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Plan after top-level introduceDerivedTable\n "
operator|+
name|RelOptUtil
operator|.
name|toString
argument_list|(
name|newTopNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|convertOpTree
argument_list|(
name|newTopNode
argument_list|,
operator|(
name|RelNode
operator|)
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Plan after nested convertOpTree\n "
operator|+
name|RelOptUtil
operator|.
name|toString
argument_list|(
name|newTopNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|alignColumns
condition|)
block|{
name|HiveRelColumnsAlignment
name|propagator
init|=
operator|new
name|HiveRelColumnsAlignment
argument_list|(
name|HiveRelFactories
operator|.
name|HIVE_BUILDER
operator|.
name|create
argument_list|(
name|newTopNode
operator|.
name|getCluster
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|newTopNode
operator|=
name|propagator
operator|.
name|align
argument_list|(
name|newTopNode
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Plan after propagating order\n "
operator|+
name|RelOptUtil
operator|.
name|toString
argument_list|(
name|newTopNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Pair
argument_list|<
name|RelNode
argument_list|,
name|RelNode
argument_list|>
name|topSelparentPair
init|=
name|HiveCalciteUtil
operator|.
name|getTopLevelSelect
argument_list|(
name|newTopNode
argument_list|)
decl_stmt|;
name|PlanModifierUtil
operator|.
name|fixTopOBSchema
argument_list|(
name|newTopNode
argument_list|,
name|topSelparentPair
argument_list|,
name|resultSchema
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Plan after fixTopOBSchema\n "
operator|+
name|RelOptUtil
operator|.
name|toString
argument_list|(
name|newTopNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|topSelparentPair
operator|=
name|HiveCalciteUtil
operator|.
name|getTopLevelSelect
argument_list|(
name|newTopNode
argument_list|)
expr_stmt|;
name|newTopNode
operator|=
name|renameTopLevelSelectInResultSchema
argument_list|(
name|newTopNode
argument_list|,
name|topSelparentPair
argument_list|,
name|resultSchema
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Final plan after modifier\n "
operator|+
name|RelOptUtil
operator|.
name|toString
argument_list|(
name|newTopNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|newTopNode
return|;
block|}
specifier|private
specifier|static
name|String
name|getTblAlias
parameter_list|(
name|RelNode
name|rel
parameter_list|)
block|{
if|if
condition|(
literal|null
operator|==
name|rel
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|rel
operator|instanceof
name|HiveTableScan
condition|)
block|{
return|return
operator|(
operator|(
name|HiveTableScan
operator|)
name|rel
operator|)
operator|.
name|getTableAlias
argument_list|()
return|;
block|}
if|if
condition|(
name|rel
operator|instanceof
name|DruidQuery
condition|)
block|{
name|DruidQuery
name|dq
init|=
operator|(
name|DruidQuery
operator|)
name|rel
decl_stmt|;
return|return
operator|(
operator|(
name|HiveTableScan
operator|)
name|dq
operator|.
name|getTableScan
argument_list|()
operator|)
operator|.
name|getTableAlias
argument_list|()
return|;
block|}
if|if
condition|(
name|rel
operator|instanceof
name|Project
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|rel
operator|.
name|getInputs
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
name|getTblAlias
argument_list|(
name|rel
operator|.
name|getInput
argument_list|(
literal|0
argument_list|)
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|void
name|convertOpTree
parameter_list|(
name|RelNode
name|rel
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
if|if
condition|(
name|rel
operator|instanceof
name|HepRelVertex
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Found HepRelVertex"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|Join
condition|)
block|{
if|if
condition|(
operator|!
name|validJoinParent
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
condition|)
block|{
name|introduceDerivedTable
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
name|String
name|leftChild
init|=
name|getTblAlias
argument_list|(
operator|(
operator|(
name|Join
operator|)
name|rel
operator|)
operator|.
name|getLeft
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|leftChild
operator|&&
name|leftChild
operator|.
name|equalsIgnoreCase
argument_list|(
name|getTblAlias
argument_list|(
operator|(
operator|(
name|Join
operator|)
name|rel
operator|)
operator|.
name|getRight
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
comment|// introduce derived table above one child, if this is self-join
comment|// since user provided aliases are lost at this point.
name|introduceDerivedTable
argument_list|(
operator|(
operator|(
name|Join
operator|)
name|rel
operator|)
operator|.
name|getLeft
argument_list|()
argument_list|,
name|rel
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|MultiJoin
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Found MultiJoin"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|RelSubset
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Found RelSubset"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|SetOp
condition|)
block|{
comment|// TODO: Handle more than 2 inputs for setop
if|if
condition|(
operator|!
name|validSetopParent
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
condition|)
name|introduceDerivedTable
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|SetOp
name|setop
init|=
operator|(
name|SetOp
operator|)
name|rel
decl_stmt|;
for|for
control|(
name|RelNode
name|inputRel
range|:
name|setop
operator|.
name|getInputs
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|validSetopChild
argument_list|(
name|inputRel
argument_list|)
condition|)
block|{
name|introduceDerivedTable
argument_list|(
name|inputRel
argument_list|,
name|setop
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|SingleRel
condition|)
block|{
if|if
condition|(
name|rel
operator|instanceof
name|Filter
condition|)
block|{
if|if
condition|(
operator|!
name|validFilterParent
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
condition|)
block|{
name|introduceDerivedTable
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|HiveSortLimit
condition|)
block|{
if|if
condition|(
operator|!
name|validSortParent
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
condition|)
block|{
name|introduceDerivedTable
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|validSortChild
argument_list|(
operator|(
name|HiveSortLimit
operator|)
name|rel
argument_list|)
condition|)
block|{
name|introduceDerivedTable
argument_list|(
operator|(
operator|(
name|HiveSortLimit
operator|)
name|rel
operator|)
operator|.
name|getInput
argument_list|()
argument_list|,
name|rel
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|rel
operator|instanceof
name|HiveAggregate
condition|)
block|{
name|RelNode
name|newParent
init|=
name|parent
decl_stmt|;
if|if
condition|(
operator|!
name|validGBParent
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
condition|)
block|{
name|newParent
operator|=
name|introduceDerivedTable
argument_list|(
name|rel
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
comment|// check if groupby is empty and there is no other cols in aggr
comment|// this should only happen when newParent is constant.
if|if
condition|(
name|isEmptyGrpAggr
argument_list|(
name|rel
argument_list|)
condition|)
block|{
name|replaceEmptyGroupAggr
argument_list|(
name|rel
argument_list|,
name|newParent
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|List
argument_list|<
name|RelNode
argument_list|>
name|childNodes
init|=
name|rel
operator|.
name|getInputs
argument_list|()
decl_stmt|;
if|if
condition|(
name|childNodes
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|RelNode
name|r
range|:
name|childNodes
control|)
block|{
name|convertOpTree
argument_list|(
name|r
argument_list|,
name|rel
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|RelNode
name|renameTopLevelSelectInResultSchema
parameter_list|(
specifier|final
name|RelNode
name|rootRel
parameter_list|,
name|Pair
argument_list|<
name|RelNode
argument_list|,
name|RelNode
argument_list|>
name|topSelparentPair
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|resultSchema
parameter_list|)
throws|throws
name|CalciteSemanticException
block|{
name|RelNode
name|parentOforiginalProjRel
init|=
name|topSelparentPair
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|HiveProject
name|originalProjRel
init|=
operator|(
name|HiveProject
operator|)
name|topSelparentPair
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|// Assumption: top portion of tree could only be
comment|// (limit)?(OB)?(Project)....
name|List
argument_list|<
name|RexNode
argument_list|>
name|rootChildExps
init|=
name|originalProjRel
operator|.
name|getChildExps
argument_list|()
decl_stmt|;
if|if
condition|(
name|resultSchema
operator|.
name|size
argument_list|()
operator|!=
name|rootChildExps
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// Safeguard against potential issues in CBO RowResolver construction. Disable CBO for now.
name|LOG
operator|.
name|error
argument_list|(
name|PlanModifierUtil
operator|.
name|generateInvalidSchemaMessage
argument_list|(
name|originalProjRel
argument_list|,
name|resultSchema
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|CalciteSemanticException
argument_list|(
literal|"Result Schema didn't match Optimized Op Tree Schema"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|newSelAliases
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|colAlias
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
name|rootChildExps
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|colAlias
operator|=
name|resultSchema
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getName
argument_list|()
expr_stmt|;
name|colAlias
operator|=
name|getNewColAlias
argument_list|(
name|newSelAliases
argument_list|,
name|colAlias
argument_list|)
expr_stmt|;
name|newSelAliases
operator|.
name|add
argument_list|(
name|colAlias
argument_list|)
expr_stmt|;
block|}
name|HiveProject
name|replacementProjectRel
init|=
name|HiveProject
operator|.
name|create
argument_list|(
name|originalProjRel
operator|.
name|getInput
argument_list|()
argument_list|,
name|originalProjRel
operator|.
name|getChildExps
argument_list|()
argument_list|,
name|newSelAliases
argument_list|)
decl_stmt|;
if|if
condition|(
name|rootRel
operator|==
name|originalProjRel
condition|)
block|{
return|return
name|replacementProjectRel
return|;
block|}
else|else
block|{
name|parentOforiginalProjRel
operator|.
name|replaceInput
argument_list|(
literal|0
argument_list|,
name|replacementProjectRel
argument_list|)
expr_stmt|;
return|return
name|rootRel
return|;
block|}
block|}
specifier|private
specifier|static
name|String
name|getNewColAlias
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|newSelAliases
parameter_list|,
name|String
name|colAlias
parameter_list|)
block|{
name|int
name|index
init|=
literal|1
decl_stmt|;
name|String
name|newColAlias
init|=
name|colAlias
decl_stmt|;
while|while
condition|(
name|newSelAliases
operator|.
name|contains
argument_list|(
name|newColAlias
argument_list|)
condition|)
block|{
comment|//This means that the derived colAlias collides with existing ones.
name|newColAlias
operator|=
name|colAlias
operator|+
literal|"_"
operator|+
operator|(
name|index
operator|++
operator|)
expr_stmt|;
block|}
return|return
name|newColAlias
return|;
block|}
specifier|private
specifier|static
name|RelNode
name|introduceDerivedTable
parameter_list|(
specifier|final
name|RelNode
name|rel
parameter_list|)
block|{
name|List
argument_list|<
name|RexNode
argument_list|>
name|projectList
init|=
name|HiveCalciteUtil
operator|.
name|getProjsFromBelowAsInputRef
argument_list|(
name|rel
argument_list|)
decl_stmt|;
name|HiveProject
name|select
init|=
name|HiveProject
operator|.
name|create
argument_list|(
name|rel
operator|.
name|getCluster
argument_list|()
argument_list|,
name|rel
argument_list|,
name|projectList
argument_list|,
name|rel
operator|.
name|getRowType
argument_list|()
argument_list|,
name|rel
operator|.
name|getCollationList
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|select
return|;
block|}
specifier|private
specifier|static
name|RelNode
name|introduceDerivedTable
parameter_list|(
specifier|final
name|RelNode
name|rel
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
name|int
name|pos
init|=
operator|-
literal|1
decl_stmt|;
name|List
argument_list|<
name|RelNode
argument_list|>
name|childList
init|=
name|parent
operator|.
name|getInputs
argument_list|()
decl_stmt|;
for|for
control|(
name|RelNode
name|child
range|:
name|childList
control|)
block|{
if|if
condition|(
name|child
operator|==
name|rel
condition|)
block|{
name|pos
operator|=
name|i
expr_stmt|;
break|break;
block|}
name|i
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|pos
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Couldn't find child node in parent's inputs"
argument_list|)
throw|;
block|}
name|RelNode
name|select
init|=
name|introduceDerivedTable
argument_list|(
name|rel
argument_list|)
decl_stmt|;
name|parent
operator|.
name|replaceInput
argument_list|(
name|pos
argument_list|,
name|select
argument_list|)
expr_stmt|;
return|return
name|select
return|;
block|}
specifier|private
specifier|static
name|boolean
name|validJoinParent
parameter_list|(
name|RelNode
name|joinNode
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
name|boolean
name|validParent
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|parent
operator|instanceof
name|Join
condition|)
block|{
comment|// In Hive AST, right child of join cannot be another join,
comment|// thus we need to introduce a project on top of it.
comment|// But we only need the additional project if the left child
comment|// is another join too; if it is not, ASTConverter will swap
comment|// the join inputs, leaving the join operator on the left.
comment|// This will help triggering multijoin recognition methods that
comment|// are embedded in SemanticAnalyzer.
if|if
condition|(
operator|(
operator|(
name|Join
operator|)
name|parent
operator|)
operator|.
name|getRight
argument_list|()
operator|==
name|joinNode
operator|&&
operator|(
operator|(
operator|(
name|Join
operator|)
name|parent
operator|)
operator|.
name|getLeft
argument_list|()
operator|instanceof
name|Join
operator|)
condition|)
block|{
name|validParent
operator|=
literal|false
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|parent
operator|instanceof
name|SetOp
condition|)
block|{
name|validParent
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|validParent
return|;
block|}
specifier|private
specifier|static
name|boolean
name|validFilterParent
parameter_list|(
name|RelNode
name|filterNode
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
name|boolean
name|validParent
init|=
literal|true
decl_stmt|;
comment|// TODO: Verify GB having is not a separate filter (if so we shouldn't
comment|// introduce derived table)
if|if
condition|(
name|parent
operator|instanceof
name|Filter
operator|||
name|parent
operator|instanceof
name|Join
operator|||
name|parent
operator|instanceof
name|SetOp
operator|||
operator|(
name|parent
operator|instanceof
name|Aggregate
operator|&&
name|filterNode
operator|.
name|getInputs
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|Aggregate
operator|)
condition|)
block|{
name|validParent
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|validParent
return|;
block|}
specifier|private
specifier|static
name|boolean
name|validGBParent
parameter_list|(
name|RelNode
name|gbNode
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
name|boolean
name|validParent
init|=
literal|true
decl_stmt|;
comment|// TOODO: Verify GB having is not a seperate filter (if so we shouldn't
comment|// introduce derived table)
if|if
condition|(
name|parent
operator|instanceof
name|Join
operator|||
name|parent
operator|instanceof
name|SetOp
operator|||
name|parent
operator|instanceof
name|Aggregate
operator|||
operator|(
name|parent
operator|instanceof
name|Filter
operator|&&
operator|(
operator|(
name|Aggregate
operator|)
name|gbNode
operator|)
operator|.
name|getGroupSet
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|validParent
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|parent
operator|instanceof
name|Project
condition|)
block|{
for|for
control|(
name|RexNode
name|child
range|:
name|parent
operator|.
name|getChildExps
argument_list|()
control|)
block|{
if|if
condition|(
name|child
operator|instanceof
name|RexOver
operator|||
name|child
operator|instanceof
name|RexWinAggCall
condition|)
block|{
comment|// Hive can't handle select rank() over(order by sum(c1)/sum(c2)) from t1 group by c3
comment|// but can handle    select rank() over (order by c4) from
comment|// (select sum(c1)/sum(c2)  as c4 from t1 group by c3) t2;
comment|// so introduce a project on top of this gby.
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
name|validParent
return|;
block|}
specifier|private
specifier|static
name|boolean
name|validSortParent
parameter_list|(
name|RelNode
name|sortNode
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
name|boolean
name|validParent
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|parent
operator|!=
literal|null
operator|&&
operator|!
operator|(
name|parent
operator|instanceof
name|Project
operator|)
operator|&&
operator|!
operator|(
name|HiveCalciteUtil
operator|.
name|pureLimitRelNode
argument_list|(
name|parent
argument_list|)
operator|&&
name|HiveCalciteUtil
operator|.
name|pureOrderRelNode
argument_list|(
name|sortNode
argument_list|)
operator|)
condition|)
block|{
name|validParent
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|validParent
return|;
block|}
specifier|private
specifier|static
name|boolean
name|validSortChild
parameter_list|(
name|HiveSortLimit
name|sortNode
parameter_list|)
block|{
name|boolean
name|validChild
init|=
literal|true
decl_stmt|;
name|RelNode
name|child
init|=
name|sortNode
operator|.
name|getInput
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|child
operator|instanceof
name|Project
operator|)
operator|&&
operator|!
operator|(
name|HiveCalciteUtil
operator|.
name|pureLimitRelNode
argument_list|(
name|sortNode
argument_list|)
operator|&&
name|HiveCalciteUtil
operator|.
name|pureOrderRelNode
argument_list|(
name|child
argument_list|)
operator|)
condition|)
block|{
name|validChild
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|validChild
return|;
block|}
specifier|private
specifier|static
name|boolean
name|validSetopParent
parameter_list|(
name|RelNode
name|setop
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
name|boolean
name|validChild
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|parent
operator|!=
literal|null
operator|&&
operator|!
operator|(
name|parent
operator|instanceof
name|Project
operator|)
condition|)
block|{
name|validChild
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|validChild
return|;
block|}
specifier|private
specifier|static
name|boolean
name|validSetopChild
parameter_list|(
name|RelNode
name|setopChild
parameter_list|)
block|{
name|boolean
name|validChild
init|=
literal|true
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|setopChild
operator|instanceof
name|Project
operator|)
condition|)
block|{
name|validChild
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|validChild
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isEmptyGrpAggr
parameter_list|(
name|RelNode
name|gbNode
parameter_list|)
block|{
comment|// Verify if both groupset and aggrfunction are empty)
name|Aggregate
name|aggrnode
init|=
operator|(
name|Aggregate
operator|)
name|gbNode
decl_stmt|;
if|if
condition|(
name|aggrnode
operator|.
name|getGroupSet
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|&&
name|aggrnode
operator|.
name|getAggCallList
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
specifier|static
name|void
name|replaceEmptyGroupAggr
parameter_list|(
specifier|final
name|RelNode
name|rel
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
comment|// If this function is called, the parent should only include constant
name|List
argument_list|<
name|RexNode
argument_list|>
name|exps
init|=
name|parent
operator|.
name|getChildExps
argument_list|()
decl_stmt|;
for|for
control|(
name|RexNode
name|rexNode
range|:
name|exps
control|)
block|{
if|if
condition|(
operator|!
name|rexNode
operator|.
name|accept
argument_list|(
operator|new
name|HiveCalciteUtil
operator|.
name|ConstantFinder
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"We expect "
operator|+
name|parent
operator|.
name|toString
argument_list|()
operator|+
literal|" to contain only constants. However, "
operator|+
name|rexNode
operator|.
name|toString
argument_list|()
operator|+
literal|" is "
operator|+
name|rexNode
operator|.
name|getKind
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|HiveAggregate
name|oldAggRel
init|=
operator|(
name|HiveAggregate
operator|)
name|rel
decl_stmt|;
name|RelDataTypeFactory
name|typeFactory
init|=
name|oldAggRel
operator|.
name|getCluster
argument_list|()
operator|.
name|getTypeFactory
argument_list|()
decl_stmt|;
name|RelDataType
name|longType
init|=
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|typeFactory
argument_list|)
decl_stmt|;
name|RelDataType
name|intType
init|=
name|TypeConverter
operator|.
name|convert
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
name|typeFactory
argument_list|)
decl_stmt|;
comment|// Create the dummy aggregation.
name|SqlAggFunction
name|countFn
init|=
name|SqlFunctionConverter
operator|.
name|getCalciteAggFn
argument_list|(
literal|"count"
argument_list|,
literal|false
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|intType
argument_list|)
argument_list|,
name|longType
argument_list|)
decl_stmt|;
comment|// TODO: Using 0 might be wrong; might need to walk down to find the
comment|// proper index of a dummy.
name|List
argument_list|<
name|Integer
argument_list|>
name|argList
init|=
name|ImmutableList
operator|.
name|of
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|AggregateCall
name|dummyCall
init|=
operator|new
name|AggregateCall
argument_list|(
name|countFn
argument_list|,
literal|false
argument_list|,
name|argList
argument_list|,
name|longType
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Aggregate
name|newAggRel
init|=
name|oldAggRel
operator|.
name|copy
argument_list|(
name|oldAggRel
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|oldAggRel
operator|.
name|getInput
argument_list|()
argument_list|,
name|oldAggRel
operator|.
name|indicator
argument_list|,
name|oldAggRel
operator|.
name|getGroupSet
argument_list|()
argument_list|,
name|oldAggRel
operator|.
name|getGroupSets
argument_list|()
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|dummyCall
argument_list|)
argument_list|)
decl_stmt|;
name|RelNode
name|select
init|=
name|introduceDerivedTable
argument_list|(
name|newAggRel
argument_list|)
decl_stmt|;
name|parent
operator|.
name|replaceInput
argument_list|(
literal|0
argument_list|,
name|select
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

