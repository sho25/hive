begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|stats
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
name|Set
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
name|BuiltinMethod
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
name|JoinUtil
operator|.
name|JoinLeafPredicateInfo
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
name|JoinUtil
operator|.
name|JoinPredicateInfo
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
name|HiveJoinRel
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
name|HiveTableScanRel
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
name|JoinRelType
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
name|ReflectiveRelMetadataProvider
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
name|RelMdSelectivity
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
name|RelMdUtil
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
name|eigenbase
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
name|eigenbase
operator|.
name|rex
operator|.
name|RexUtil
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
name|HiveRelMdSelectivity
extends|extends
name|RelMdSelectivity
block|{
specifier|public
specifier|static
specifier|final
name|RelMetadataProvider
name|SOURCE
init|=
name|ReflectiveRelMetadataProvider
operator|.
name|reflectiveSource
argument_list|(
name|BuiltinMethod
operator|.
name|SELECTIVITY
operator|.
name|method
argument_list|,
operator|new
name|HiveRelMdSelectivity
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
name|HiveRelMdSelectivity
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Double
name|getSelectivity
parameter_list|(
name|HiveTableScanRel
name|t
parameter_list|,
name|RexNode
name|predicate
parameter_list|)
block|{
if|if
condition|(
name|predicate
operator|!=
literal|null
condition|)
block|{
name|FilterSelectivityEstimator
name|filterSelEstmator
init|=
operator|new
name|FilterSelectivityEstimator
argument_list|(
name|t
argument_list|)
decl_stmt|;
return|return
name|filterSelEstmator
operator|.
name|estimateSelectivity
argument_list|(
name|predicate
argument_list|)
return|;
block|}
return|return
literal|1.0
return|;
block|}
specifier|public
name|Double
name|getSelectivity
parameter_list|(
name|HiveJoinRel
name|j
parameter_list|,
name|RexNode
name|predicate
parameter_list|)
block|{
if|if
condition|(
name|j
operator|.
name|getJoinType
argument_list|()
operator|.
name|equals
argument_list|(
name|JoinRelType
operator|.
name|INNER
argument_list|)
condition|)
block|{
return|return
name|computeInnerJoinSelectivity
argument_list|(
name|j
argument_list|,
name|predicate
argument_list|)
return|;
block|}
return|return
literal|1.0
return|;
block|}
specifier|private
name|Double
name|computeInnerJoinSelectivity
parameter_list|(
name|HiveJoinRel
name|j
parameter_list|,
name|RexNode
name|predicate
parameter_list|)
block|{
name|double
name|ndvCrossProduct
init|=
literal|1
decl_stmt|;
name|RexNode
name|combinedPredicate
init|=
name|getCombinedPredicateForJoin
argument_list|(
name|j
argument_list|,
name|predicate
argument_list|)
decl_stmt|;
name|JoinPredicateInfo
name|jpi
init|=
name|JoinPredicateInfo
operator|.
name|constructJoinPredicateInfo
argument_list|(
name|j
argument_list|,
name|combinedPredicate
argument_list|)
decl_stmt|;
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|Integer
argument_list|,
name|Double
argument_list|>
name|colStatMapBuilder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|ImmutableMap
argument_list|<
name|Integer
argument_list|,
name|Double
argument_list|>
name|colStatMap
decl_stmt|;
name|int
name|rightOffSet
init|=
name|j
operator|.
name|getLeft
argument_list|()
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldCount
argument_list|()
decl_stmt|;
comment|// 1. Update Col Stats Map with col stats for columns from left side of
comment|// Join which are part of join keys
for|for
control|(
name|Integer
name|ljk
range|:
name|jpi
operator|.
name|getProjsFromLeftPartOfJoinKeysInChildSchema
argument_list|()
control|)
block|{
name|colStatMapBuilder
operator|.
name|put
argument_list|(
name|ljk
argument_list|,
name|HiveRelMdDistinctRowCount
operator|.
name|getDistinctRowCount
argument_list|(
name|j
operator|.
name|getLeft
argument_list|()
argument_list|,
name|ljk
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// 2. Update Col Stats Map with col stats for columns from right side of
comment|// Join which are part of join keys
for|for
control|(
name|Integer
name|rjk
range|:
name|jpi
operator|.
name|getProjsFromRightPartOfJoinKeysInChildSchema
argument_list|()
control|)
block|{
name|colStatMapBuilder
operator|.
name|put
argument_list|(
name|rjk
operator|+
name|rightOffSet
argument_list|,
name|HiveRelMdDistinctRowCount
operator|.
name|getDistinctRowCount
argument_list|(
name|j
operator|.
name|getRight
argument_list|()
argument_list|,
name|rjk
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|colStatMap
operator|=
name|colStatMapBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
comment|// 3. Walk through the Join Condition Building NDV for selectivity
comment|// NDV of the join can not exceed the cardinality of cross join.
name|List
argument_list|<
name|JoinLeafPredicateInfo
argument_list|>
name|peLst
init|=
name|jpi
operator|.
name|getEquiJoinPredicateElements
argument_list|()
decl_stmt|;
name|int
name|noOfPE
init|=
name|peLst
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|noOfPE
operator|>
literal|0
condition|)
block|{
name|ndvCrossProduct
operator|=
name|exponentialBackoff
argument_list|(
name|peLst
argument_list|,
name|colStatMap
argument_list|)
expr_stmt|;
if|if
condition|(
name|j
operator|.
name|isLeftSemiJoin
argument_list|()
condition|)
name|ndvCrossProduct
operator|=
name|Math
operator|.
name|min
argument_list|(
name|RelMetadataQuery
operator|.
name|getRowCount
argument_list|(
name|j
operator|.
name|getLeft
argument_list|()
argument_list|)
argument_list|,
name|ndvCrossProduct
argument_list|)
expr_stmt|;
else|else
name|ndvCrossProduct
operator|=
name|Math
operator|.
name|min
argument_list|(
name|RelMetadataQuery
operator|.
name|getRowCount
argument_list|(
name|j
operator|.
name|getLeft
argument_list|()
argument_list|)
operator|*
name|RelMetadataQuery
operator|.
name|getRowCount
argument_list|(
name|j
operator|.
name|getRight
argument_list|()
argument_list|)
argument_list|,
name|ndvCrossProduct
argument_list|)
expr_stmt|;
block|}
comment|// 4. Join Selectivity = 1/NDV
return|return
operator|(
literal|1
operator|/
name|ndvCrossProduct
operator|)
return|;
block|}
comment|// 3.2 if conjunctive predicate elements are more than one, then walk
comment|// through them one by one. Compute cross product of NDV. Cross product is
comment|// computed by multiplying the largest NDV of all of the conjunctive
comment|// predicate
comment|// elements with degraded NDV of rest of the conjunctive predicate
comment|// elements. NDV is
comment|// degraded using log function.Finally the ndvCrossProduct is fenced at
comment|// the join
comment|// cross product to ensure that NDV can not exceed worst case join
comment|// cardinality.<br>
comment|// NDV of a conjunctive predicate element is the max NDV of all arguments
comment|// to lhs, rhs expressions.
comment|// NDV(JoinCondition) = min (left cardinality * right cardinality,
comment|// ndvCrossProduct(JoinCondition))
comment|// ndvCrossProduct(JoinCondition) = ndv(pex)*log(ndv(pe1))*log(ndv(pe2))
comment|// where pex is the predicate element of join condition with max ndv.
comment|// ndv(pe) = max(NDV(left.Expr), NDV(right.Expr))
comment|// NDV(expr) = max(NDV( expr args))
specifier|protected
name|double
name|logSmoothing
parameter_list|(
name|List
argument_list|<
name|JoinLeafPredicateInfo
argument_list|>
name|peLst
parameter_list|,
name|ImmutableMap
argument_list|<
name|Integer
argument_list|,
name|Double
argument_list|>
name|colStatMap
parameter_list|)
block|{
name|int
name|noOfPE
init|=
name|peLst
operator|.
name|size
argument_list|()
decl_stmt|;
name|double
name|ndvCrossProduct
init|=
name|getMaxNDVForJoinSelectivity
argument_list|(
name|peLst
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|colStatMap
argument_list|)
decl_stmt|;
if|if
condition|(
name|noOfPE
operator|>
literal|1
condition|)
block|{
name|double
name|maxNDVSoFar
init|=
name|ndvCrossProduct
decl_stmt|;
name|double
name|ndvToBeSmoothed
decl_stmt|;
name|double
name|tmpNDV
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|noOfPE
condition|;
name|i
operator|++
control|)
block|{
name|tmpNDV
operator|=
name|getMaxNDVForJoinSelectivity
argument_list|(
name|peLst
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|colStatMap
argument_list|)
expr_stmt|;
if|if
condition|(
name|tmpNDV
operator|>
name|maxNDVSoFar
condition|)
block|{
name|ndvToBeSmoothed
operator|=
name|maxNDVSoFar
expr_stmt|;
name|maxNDVSoFar
operator|=
name|tmpNDV
expr_stmt|;
name|ndvCrossProduct
operator|=
operator|(
name|ndvCrossProduct
operator|/
name|ndvToBeSmoothed
operator|)
operator|*
name|tmpNDV
expr_stmt|;
block|}
else|else
block|{
name|ndvToBeSmoothed
operator|=
name|tmpNDV
expr_stmt|;
block|}
comment|// TODO: revisit the fence
if|if
condition|(
name|ndvToBeSmoothed
operator|>
literal|3
condition|)
name|ndvCrossProduct
operator|*=
name|Math
operator|.
name|log
argument_list|(
name|ndvToBeSmoothed
argument_list|)
expr_stmt|;
else|else
name|ndvCrossProduct
operator|*=
name|ndvToBeSmoothed
expr_stmt|;
block|}
block|}
return|return
name|ndvCrossProduct
return|;
block|}
comment|/*    * a) Order predciates based on ndv in reverse order. b) ndvCrossProduct =    * ndv(pe0) * ndv(pe1) ^(1/2) * ndv(pe2) ^(1/4) * ndv(pe3) ^(1/8) ...    */
specifier|protected
name|double
name|exponentialBackoff
parameter_list|(
name|List
argument_list|<
name|JoinLeafPredicateInfo
argument_list|>
name|peLst
parameter_list|,
name|ImmutableMap
argument_list|<
name|Integer
argument_list|,
name|Double
argument_list|>
name|colStatMap
parameter_list|)
block|{
name|int
name|noOfPE
init|=
name|peLst
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Double
argument_list|>
name|ndvs
init|=
operator|new
name|ArrayList
argument_list|<
name|Double
argument_list|>
argument_list|(
name|noOfPE
argument_list|)
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
name|noOfPE
condition|;
name|i
operator|++
control|)
block|{
name|ndvs
operator|.
name|add
argument_list|(
name|getMaxNDVForJoinSelectivity
argument_list|(
name|peLst
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|colStatMap
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|ndvs
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|reverse
argument_list|(
name|ndvs
argument_list|)
expr_stmt|;
name|double
name|ndvCrossProduct
init|=
literal|1.0
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
name|ndvs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|double
name|n
init|=
name|Math
operator|.
name|pow
argument_list|(
name|ndvs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|Math
operator|.
name|pow
argument_list|(
literal|1
operator|/
literal|2.0
argument_list|,
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|ndvCrossProduct
operator|*=
name|n
expr_stmt|;
block|}
return|return
name|ndvCrossProduct
return|;
block|}
specifier|private
name|RexNode
name|getCombinedPredicateForJoin
parameter_list|(
name|HiveJoinRel
name|j
parameter_list|,
name|RexNode
name|additionalPredicate
parameter_list|)
block|{
name|RexNode
name|minusPred
init|=
name|RelMdUtil
operator|.
name|minusPreds
argument_list|(
name|j
operator|.
name|getCluster
argument_list|()
operator|.
name|getRexBuilder
argument_list|()
argument_list|,
name|additionalPredicate
argument_list|,
name|j
operator|.
name|getCondition
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|minusPred
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|RexNode
argument_list|>
name|minusList
init|=
operator|new
name|ArrayList
argument_list|<
name|RexNode
argument_list|>
argument_list|()
decl_stmt|;
name|minusList
operator|.
name|add
argument_list|(
name|j
operator|.
name|getCondition
argument_list|()
argument_list|)
expr_stmt|;
name|minusList
operator|.
name|add
argument_list|(
name|minusPred
argument_list|)
expr_stmt|;
return|return
name|RexUtil
operator|.
name|composeConjunction
argument_list|(
name|j
operator|.
name|getCluster
argument_list|()
operator|.
name|getRexBuilder
argument_list|()
argument_list|,
name|minusList
argument_list|,
literal|true
argument_list|)
return|;
block|}
return|return
name|j
operator|.
name|getCondition
argument_list|()
return|;
block|}
comment|/**    * Compute Max NDV to determine Join Selectivity.    *     * @param jlpi    * @param colStatMap    *          Immutable Map of Projection Index (in Join Schema) to Column Stat    * @param rightProjOffSet    * @return    */
specifier|private
specifier|static
name|Double
name|getMaxNDVForJoinSelectivity
parameter_list|(
name|JoinLeafPredicateInfo
name|jlpi
parameter_list|,
name|ImmutableMap
argument_list|<
name|Integer
argument_list|,
name|Double
argument_list|>
name|colStatMap
parameter_list|)
block|{
name|Double
name|maxNDVSoFar
init|=
literal|1.0
decl_stmt|;
name|maxNDVSoFar
operator|=
name|getMaxNDVFromProjections
argument_list|(
name|colStatMap
argument_list|,
name|jlpi
operator|.
name|getProjsFromLeftPartOfJoinKeysInJoinSchema
argument_list|()
argument_list|,
name|maxNDVSoFar
argument_list|)
expr_stmt|;
name|maxNDVSoFar
operator|=
name|getMaxNDVFromProjections
argument_list|(
name|colStatMap
argument_list|,
name|jlpi
operator|.
name|getProjsFromRightPartOfJoinKeysInJoinSchema
argument_list|()
argument_list|,
name|maxNDVSoFar
argument_list|)
expr_stmt|;
return|return
name|maxNDVSoFar
return|;
block|}
specifier|private
specifier|static
name|Double
name|getMaxNDVFromProjections
parameter_list|(
name|Map
argument_list|<
name|Integer
argument_list|,
name|Double
argument_list|>
name|colStatMap
parameter_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
name|projectionSet
parameter_list|,
name|Double
name|defaultMaxNDV
parameter_list|)
block|{
name|Double
name|colNDV
init|=
literal|null
decl_stmt|;
name|Double
name|maxNDVSoFar
init|=
name|defaultMaxNDV
decl_stmt|;
for|for
control|(
name|Integer
name|projIndx
range|:
name|projectionSet
control|)
block|{
name|colNDV
operator|=
name|colStatMap
operator|.
name|get
argument_list|(
name|projIndx
argument_list|)
expr_stmt|;
if|if
condition|(
name|colNDV
operator|>
name|maxNDVSoFar
condition|)
name|maxNDVSoFar
operator|=
name|colNDV
expr_stmt|;
block|}
return|return
name|maxNDVSoFar
return|;
block|}
block|}
end_class

end_unit

