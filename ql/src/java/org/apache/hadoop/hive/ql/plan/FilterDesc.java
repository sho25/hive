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
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|Objects
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
name|Explain
operator|.
name|Level
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
name|Explain
operator|.
name|Vectorization
import|;
end_import

begin_comment
comment|/**  * FilterDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Filter Operator"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|FilterDesc
extends|extends
name|AbstractOperatorDesc
block|{
comment|/**    * sampleDesc is used to keep track of the sampling descriptor.    */
specifier|public
specifier|static
class|class
name|SampleDesc
implements|implements
name|Cloneable
block|{
comment|// The numerator of the TABLESAMPLE clause
specifier|private
name|int
name|numerator
decl_stmt|;
comment|// The denominator of the TABLESAMPLE clause
specifier|private
name|int
name|denominator
decl_stmt|;
comment|// Input files can be pruned
specifier|private
name|boolean
name|inputPruning
decl_stmt|;
specifier|public
name|SampleDesc
parameter_list|()
block|{     }
specifier|public
name|SampleDesc
parameter_list|(
name|int
name|numerator
parameter_list|,
name|int
name|denominator
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|tabBucketCols
parameter_list|,
name|boolean
name|inputPruning
parameter_list|)
block|{
name|this
operator|.
name|numerator
operator|=
name|numerator
expr_stmt|;
name|this
operator|.
name|denominator
operator|=
name|denominator
expr_stmt|;
name|this
operator|.
name|inputPruning
operator|=
name|inputPruning
expr_stmt|;
block|}
specifier|public
name|int
name|getNumerator
parameter_list|()
block|{
return|return
name|numerator
return|;
block|}
specifier|public
name|int
name|getDenominator
parameter_list|()
block|{
return|return
name|denominator
return|;
block|}
specifier|public
name|boolean
name|getInputPruning
parameter_list|()
block|{
return|return
name|inputPruning
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|clone
parameter_list|()
block|{
name|SampleDesc
name|desc
init|=
operator|new
name|SampleDesc
argument_list|(
name|numerator
argument_list|,
name|denominator
argument_list|,
literal|null
argument_list|,
name|inputPruning
argument_list|)
decl_stmt|;
return|return
name|desc
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|inputPruning
condition|?
literal|"BUCKET "
operator|+
name|numerator
operator|+
literal|" OUT OF "
operator|+
name|denominator
else|:
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
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
name|ExprNodeDesc
name|predicate
decl_stmt|;
specifier|private
name|boolean
name|isSamplingPred
decl_stmt|;
specifier|private
name|boolean
name|syntheticJoinPredicate
decl_stmt|;
specifier|private
specifier|transient
name|SampleDesc
name|sampleDescr
decl_stmt|;
comment|//Is this a filter that should perform a comparison for sorted searches
specifier|private
name|boolean
name|isSortedFilter
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|isGenerated
decl_stmt|;
specifier|public
name|FilterDesc
parameter_list|()
block|{   }
specifier|public
name|FilterDesc
parameter_list|(
specifier|final
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
name|ExprNodeDesc
name|predicate
parameter_list|,
name|boolean
name|isSamplingPred
parameter_list|)
block|{
name|this
operator|.
name|predicate
operator|=
name|predicate
expr_stmt|;
name|this
operator|.
name|isSamplingPred
operator|=
name|isSamplingPred
expr_stmt|;
name|sampleDescr
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|FilterDesc
parameter_list|(
specifier|final
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
name|ExprNodeDesc
name|predicate
parameter_list|,
name|boolean
name|isSamplingPred
parameter_list|,
specifier|final
name|SampleDesc
name|sampleDescr
parameter_list|)
block|{
name|this
operator|.
name|predicate
operator|=
name|predicate
expr_stmt|;
name|this
operator|.
name|isSamplingPred
operator|=
name|isSamplingPred
expr_stmt|;
name|this
operator|.
name|sampleDescr
operator|=
name|sampleDescr
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"predicate"
argument_list|)
specifier|public
name|String
name|getPredicateString
parameter_list|()
block|{
return|return
name|PlanUtils
operator|.
name|getExprListString
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|predicate
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"predicate"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|}
argument_list|)
specifier|public
name|String
name|getUserLevelExplainPredicateString
parameter_list|()
block|{
return|return
name|PlanUtils
operator|.
name|getExprListString
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|predicate
argument_list|)
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
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
name|ExprNodeDesc
name|getPredicate
parameter_list|()
block|{
return|return
name|predicate
return|;
block|}
specifier|public
name|void
name|setPredicate
parameter_list|(
specifier|final
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
name|ExprNodeDesc
name|predicate
parameter_list|)
block|{
name|this
operator|.
name|predicate
operator|=
name|predicate
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"isSamplingPred"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|boolean
name|getIsSamplingPred
parameter_list|()
block|{
return|return
name|isSamplingPred
return|;
block|}
specifier|public
name|void
name|setIsSamplingPred
parameter_list|(
specifier|final
name|boolean
name|isSamplingPred
parameter_list|)
block|{
name|this
operator|.
name|isSamplingPred
operator|=
name|isSamplingPred
expr_stmt|;
block|}
specifier|public
name|SampleDesc
name|getSampleDescr
parameter_list|()
block|{
return|return
name|sampleDescr
return|;
block|}
specifier|public
name|void
name|setSampleDescr
parameter_list|(
specifier|final
name|SampleDesc
name|sampleDescr
parameter_list|)
block|{
name|this
operator|.
name|sampleDescr
operator|=
name|sampleDescr
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"sampleDesc"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getSampleDescExpr
parameter_list|()
block|{
return|return
name|sampleDescr
operator|==
literal|null
condition|?
literal|null
else|:
name|sampleDescr
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isSortedFilter
parameter_list|()
block|{
return|return
name|isSortedFilter
return|;
block|}
specifier|public
name|void
name|setSortedFilter
parameter_list|(
name|boolean
name|isSortedFilter
parameter_list|)
block|{
name|this
operator|.
name|isSortedFilter
operator|=
name|isSortedFilter
expr_stmt|;
block|}
comment|/**    * Some filters are generated or implied, which means it is not in the query.    * It is added by the analyzer. For example, when we do an inner join, we add    * filters to exclude those rows with null join key values.    */
specifier|public
name|boolean
name|isGenerated
parameter_list|()
block|{
return|return
name|isGenerated
return|;
block|}
specifier|public
name|void
name|setGenerated
parameter_list|(
name|boolean
name|isGenerated
parameter_list|)
block|{
name|this
operator|.
name|isGenerated
operator|=
name|isGenerated
expr_stmt|;
block|}
specifier|public
name|boolean
name|isSyntheticJoinPredicate
parameter_list|()
block|{
return|return
name|syntheticJoinPredicate
return|;
block|}
specifier|public
name|void
name|setSyntheticJoinPredicate
parameter_list|(
name|boolean
name|syntheticJoinPredicate
parameter_list|)
block|{
name|this
operator|.
name|syntheticJoinPredicate
operator|=
name|syntheticJoinPredicate
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|clone
parameter_list|()
block|{
name|FilterDesc
name|filterDesc
init|=
operator|new
name|FilterDesc
argument_list|(
name|getPredicate
argument_list|()
operator|.
name|clone
argument_list|()
argument_list|,
name|getIsSamplingPred
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|getIsSamplingPred
argument_list|()
condition|)
block|{
name|filterDesc
operator|.
name|setSampleDescr
argument_list|(
name|getSampleDescr
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|filterDesc
operator|.
name|setSortedFilter
argument_list|(
name|isSortedFilter
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|filterDesc
return|;
block|}
specifier|public
class|class
name|FilterOperatorExplainVectorization
extends|extends
name|OperatorExplainVectorization
block|{
specifier|private
specifier|final
name|FilterDesc
name|filterDesc
decl_stmt|;
specifier|private
specifier|final
name|VectorFilterDesc
name|vectorFilterDesc
decl_stmt|;
specifier|public
name|FilterOperatorExplainVectorization
parameter_list|(
name|FilterDesc
name|filterDesc
parameter_list|,
name|VectorDesc
name|vectorDesc
parameter_list|)
block|{
comment|// Native vectorization supported.
name|super
argument_list|(
name|vectorDesc
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|filterDesc
operator|=
name|filterDesc
expr_stmt|;
name|vectorFilterDesc
operator|=
operator|(
name|VectorFilterDesc
operator|)
name|vectorDesc
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|EXPRESSION
argument_list|,
name|displayName
operator|=
literal|"predicateExpression"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getPredicateExpression
parameter_list|()
block|{
return|return
name|vectorFilterDesc
operator|.
name|getPredicateExpression
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|OPERATOR
argument_list|,
name|displayName
operator|=
literal|"Filter Vectorization"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|FilterOperatorExplainVectorization
name|getFilterVectorization
parameter_list|()
block|{
if|if
condition|(
name|vectorDesc
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|FilterOperatorExplainVectorization
argument_list|(
name|this
argument_list|,
name|vectorDesc
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSame
parameter_list|(
name|OperatorDesc
name|other
parameter_list|)
block|{
if|if
condition|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|FilterDesc
name|otherDesc
init|=
operator|(
name|FilterDesc
operator|)
name|other
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|getPredicateString
argument_list|()
argument_list|,
name|otherDesc
operator|.
name|getPredicateString
argument_list|()
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|getSampleDescExpr
argument_list|()
argument_list|,
name|otherDesc
operator|.
name|getSampleDescExpr
argument_list|()
argument_list|)
operator|&&
name|getIsSamplingPred
argument_list|()
operator|==
name|otherDesc
operator|.
name|getIsSamplingPred
argument_list|()
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

