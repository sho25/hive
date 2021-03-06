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
name|java
operator|.
name|util
operator|.
name|Map
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
name|RelCollation
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
name|RelCollationTraitDef
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_class
specifier|public
class|class
name|HiveSortLimit
extends|extends
name|Sort
implements|implements
name|HiveRelNode
block|{
comment|// NOTE: this is to work around Hive Calcite Limitations w.r.t OB.
comment|// 1. Calcite can not accept expressions in OB; instead it needs to be expressed
comment|// as VC in input Select.
comment|// 2. Hive can not preserve ordering through select boundaries.
comment|// 3. This map is used for outermost OB to migrate the VC corresponding OB
comment|// expressions from input select.
comment|// 4. This is used by ASTConverter after we are done with Calcite Planning
specifier|private
name|ImmutableMap
argument_list|<
name|Integer
argument_list|,
name|RexNode
argument_list|>
name|mapOfInputRefToRexCall
decl_stmt|;
specifier|private
name|boolean
name|ruleCreated
decl_stmt|;
specifier|public
name|HiveSortLimit
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
name|RelCollation
name|collation
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
name|getSortTraitSet
argument_list|(
name|cluster
argument_list|,
name|traitSet
argument_list|,
name|collation
argument_list|)
argument_list|,
name|child
argument_list|,
name|collation
argument_list|,
name|offset
argument_list|,
name|fetch
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a HiveSortLimit.    *    * @param input     Input relational expression    * @param collation array of sort specifications    * @param offset    Expression for number of rows to discard before returning    *                  first row    * @param fetch     Expression for number of rows to fetch    */
specifier|public
specifier|static
name|HiveSortLimit
name|create
parameter_list|(
name|RelNode
name|input
parameter_list|,
name|RelCollation
name|collation
parameter_list|,
name|RexNode
name|offset
parameter_list|,
name|RexNode
name|fetch
parameter_list|)
block|{
name|RelOptCluster
name|cluster
init|=
name|input
operator|.
name|getCluster
argument_list|()
decl_stmt|;
name|collation
operator|=
name|RelCollationTraitDef
operator|.
name|INSTANCE
operator|.
name|canonize
argument_list|(
name|collation
argument_list|)
expr_stmt|;
name|RelTraitSet
name|traitSet
init|=
name|TraitsUtil
operator|.
name|getSortTraitSet
argument_list|(
name|cluster
argument_list|,
name|input
operator|.
name|getTraitSet
argument_list|()
argument_list|,
name|collation
argument_list|)
decl_stmt|;
return|return
operator|new
name|HiveSortLimit
argument_list|(
name|cluster
argument_list|,
name|traitSet
argument_list|,
name|input
argument_list|,
name|collation
argument_list|,
name|offset
argument_list|,
name|fetch
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveSortLimit
name|copy
parameter_list|(
name|RelTraitSet
name|traitSet
parameter_list|,
name|RelNode
name|newInput
parameter_list|,
name|RelCollation
name|newCollation
parameter_list|,
name|RexNode
name|offset
parameter_list|,
name|RexNode
name|fetch
parameter_list|)
block|{
comment|// TODO: can we blindly copy sort trait? What if inputs changed and we
comment|// are now sorting by different cols
name|RelCollation
name|canonizedCollation
init|=
name|traitSet
operator|.
name|canonize
argument_list|(
name|newCollation
argument_list|)
decl_stmt|;
name|HiveSortLimit
name|sortLimit
init|=
operator|new
name|HiveSortLimit
argument_list|(
name|getCluster
argument_list|()
argument_list|,
name|traitSet
argument_list|,
name|newInput
argument_list|,
name|canonizedCollation
argument_list|,
name|offset
argument_list|,
name|fetch
argument_list|)
decl_stmt|;
name|sortLimit
operator|.
name|setRuleCreated
argument_list|(
name|ruleCreated
argument_list|)
expr_stmt|;
return|return
name|sortLimit
return|;
block|}
specifier|public
name|RexNode
name|getFetchExpr
parameter_list|()
block|{
return|return
name|fetch
return|;
block|}
specifier|public
name|RexNode
name|getOffsetExpr
parameter_list|()
block|{
return|return
name|offset
return|;
block|}
specifier|public
name|void
name|setInputRefToCallMap
parameter_list|(
name|ImmutableMap
argument_list|<
name|Integer
argument_list|,
name|RexNode
argument_list|>
name|refToCall
parameter_list|)
block|{
name|this
operator|.
name|mapOfInputRefToRexCall
operator|=
name|refToCall
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|Integer
argument_list|,
name|RexNode
argument_list|>
name|getInputRefToCallMap
parameter_list|()
block|{
return|return
name|this
operator|.
name|mapOfInputRefToRexCall
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
specifier|public
name|boolean
name|isRuleCreated
parameter_list|()
block|{
return|return
name|ruleCreated
return|;
block|}
specifier|public
name|void
name|setRuleCreated
parameter_list|(
name|boolean
name|ruleCreated
parameter_list|)
block|{
name|this
operator|.
name|ruleCreated
operator|=
name|ruleCreated
expr_stmt|;
block|}
comment|//required for HiveRelDecorrelator
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

