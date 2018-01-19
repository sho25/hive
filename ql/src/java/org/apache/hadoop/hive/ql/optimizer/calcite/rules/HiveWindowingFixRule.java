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
name|HashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|rex
operator|.
name|RexFieldCollation
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
comment|/**  * Rule to fix windowing issue when it is done over  * aggregation columns (more info in HIVE-10627).  *  * This rule is applied as a post-processing step after  * optimization by Calcite in order to add columns  * that may be pruned by RelFieldTrimmer, but are  * still needed due to the concrete implementation of  * Windowing processing in Hive.  */
end_comment

begin_class
specifier|public
class|class
name|HiveWindowingFixRule
extends|extends
name|RelOptRule
block|{
specifier|public
specifier|static
specifier|final
name|HiveWindowingFixRule
name|INSTANCE
init|=
operator|new
name|HiveWindowingFixRule
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ProjectFactory
name|projectFactory
decl_stmt|;
specifier|private
name|HiveWindowingFixRule
parameter_list|()
block|{
name|super
argument_list|(
name|operand
argument_list|(
name|Project
operator|.
name|class
argument_list|,
name|operand
argument_list|(
name|Aggregate
operator|.
name|class
argument_list|,
name|any
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|projectFactory
operator|=
name|HiveRelFactories
operator|.
name|HIVE_PROJECT_FACTORY
expr_stmt|;
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
name|Project
name|project
init|=
name|call
operator|.
name|rel
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Aggregate
name|aggregate
init|=
name|call
operator|.
name|rel
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// 1. We go over the expressions in the project operator
comment|//    and we separate the windowing nodes that are result
comment|//    of an aggregate expression from the rest of nodes
specifier|final
name|int
name|groupingFields
init|=
name|aggregate
operator|.
name|getGroupCount
argument_list|()
operator|+
name|aggregate
operator|.
name|getIndicatorCount
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|projectExprsDigest
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|RexNode
argument_list|>
name|windowingExprsDigestToNodes
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|RexNode
name|r
range|:
name|project
operator|.
name|getChildExps
argument_list|()
control|)
block|{
if|if
condition|(
name|r
operator|instanceof
name|RexOver
condition|)
block|{
name|RexOver
name|rexOverNode
init|=
operator|(
name|RexOver
operator|)
name|r
decl_stmt|;
comment|// Operands
for|for
control|(
name|RexNode
name|operand
range|:
name|rexOverNode
operator|.
name|getOperands
argument_list|()
control|)
block|{
if|if
condition|(
name|operand
operator|instanceof
name|RexInputRef
operator|&&
operator|(
operator|(
name|RexInputRef
operator|)
name|operand
operator|)
operator|.
name|getIndex
argument_list|()
operator|>=
name|groupingFields
condition|)
block|{
name|windowingExprsDigestToNodes
operator|.
name|put
argument_list|(
name|operand
operator|.
name|toString
argument_list|()
argument_list|,
name|operand
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Partition keys
for|for
control|(
name|RexNode
name|partitionKey
range|:
name|rexOverNode
operator|.
name|getWindow
argument_list|()
operator|.
name|partitionKeys
control|)
block|{
if|if
condition|(
name|partitionKey
operator|instanceof
name|RexInputRef
operator|&&
operator|(
operator|(
name|RexInputRef
operator|)
name|partitionKey
operator|)
operator|.
name|getIndex
argument_list|()
operator|>=
name|groupingFields
condition|)
block|{
name|windowingExprsDigestToNodes
operator|.
name|put
argument_list|(
name|partitionKey
operator|.
name|toString
argument_list|()
argument_list|,
name|partitionKey
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Order keys
for|for
control|(
name|RexFieldCollation
name|orderKey
range|:
name|rexOverNode
operator|.
name|getWindow
argument_list|()
operator|.
name|orderKeys
control|)
block|{
if|if
condition|(
name|orderKey
operator|.
name|left
operator|instanceof
name|RexInputRef
operator|&&
operator|(
operator|(
name|RexInputRef
operator|)
name|orderKey
operator|.
name|left
operator|)
operator|.
name|getIndex
argument_list|()
operator|>=
name|groupingFields
condition|)
block|{
name|windowingExprsDigestToNodes
operator|.
name|put
argument_list|(
name|orderKey
operator|.
name|left
operator|.
name|toString
argument_list|()
argument_list|,
name|orderKey
operator|.
name|left
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|projectExprsDigest
operator|.
name|add
argument_list|(
name|r
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// 2. We check whether there is a column needed by the
comment|//    windowing operation that is missing in the
comment|//    project expressions. For instance, if the windowing
comment|//    operation is over an aggregation column, Hive expects
comment|//    that column to be in the Select clause of the query.
comment|//    The idea is that if there is a column missing, we will
comment|//    replace the old project operator by two new project
comment|//    operators:
comment|//    - a project operator containing the original columns
comment|//      of the project operator plus all the columns that were
comment|//      missing
comment|//    - a project on top of the previous one, that will take
comment|//      out the columns that were missing and were added by the
comment|//      previous project
comment|// These data structures are needed to create the new project
comment|// operator (below)
specifier|final
name|List
argument_list|<
name|RexNode
argument_list|>
name|belowProjectExprs
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|belowProjectColumnNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// This data structure is needed to create the new project
comment|// operator (top)
specifier|final
name|List
argument_list|<
name|RexNode
argument_list|>
name|topProjectExprs
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|int
name|projectCount
init|=
name|project
operator|.
name|getChildExps
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
name|projectCount
condition|;
name|i
operator|++
control|)
block|{
name|belowProjectExprs
operator|.
name|add
argument_list|(
name|project
operator|.
name|getChildExps
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|belowProjectColumnNames
operator|.
name|add
argument_list|(
name|project
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldNames
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|topProjectExprs
operator|.
name|add
argument_list|(
name|RexInputRef
operator|.
name|of
argument_list|(
name|i
argument_list|,
name|project
operator|.
name|getRowType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|boolean
name|windowingFix
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|RexNode
argument_list|>
name|windowingExpr
range|:
name|windowingExprsDigestToNodes
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|projectExprsDigest
operator|.
name|contains
argument_list|(
name|windowingExpr
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|windowingFix
operator|=
literal|true
expr_stmt|;
name|belowProjectExprs
operator|.
name|add
argument_list|(
name|windowingExpr
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|colIndex
init|=
literal|0
decl_stmt|;
name|String
name|alias
init|=
literal|"window_col_"
operator|+
name|colIndex
decl_stmt|;
while|while
condition|(
name|belowProjectColumnNames
operator|.
name|contains
argument_list|(
name|alias
argument_list|)
condition|)
block|{
name|alias
operator|=
literal|"window_col_"
operator|+
operator|(
name|colIndex
operator|++
operator|)
expr_stmt|;
block|}
name|belowProjectColumnNames
operator|.
name|add
argument_list|(
name|alias
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|windowingFix
condition|)
block|{
comment|// We do not need to do anything, we bail out
return|return;
block|}
comment|// 3. We need to fix it, we create the two replacement project
comment|//    operators
name|RelNode
name|newProjectRel
init|=
name|projectFactory
operator|.
name|createProject
argument_list|(
name|aggregate
argument_list|,
name|belowProjectExprs
argument_list|,
name|belowProjectColumnNames
argument_list|)
decl_stmt|;
name|RelNode
name|newTopProjectRel
init|=
name|projectFactory
operator|.
name|createProject
argument_list|(
name|newProjectRel
argument_list|,
name|topProjectExprs
argument_list|,
name|project
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldNames
argument_list|()
argument_list|)
decl_stmt|;
name|call
operator|.
name|transformTo
argument_list|(
name|newTopProjectRel
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

