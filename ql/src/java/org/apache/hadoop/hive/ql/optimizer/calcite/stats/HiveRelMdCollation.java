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
name|calcite
operator|.
name|stats
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
name|RelFieldCollation
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
name|ReflectiveRelMetadataProvider
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
name|RelMdCollation
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
name|util
operator|.
name|BuiltInMethod
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
name|HiveCalciteUtil
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
name|calcite
operator|.
name|HiveCalciteUtil
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
name|calcite
operator|.
name|HiveRelCollation
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
name|HiveJoin
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
name|HiveJoin
operator|.
name|MapJoinStreamingRelation
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
name|HiveRelMdCollation
block|{
specifier|public
specifier|static
specifier|final
name|RelMetadataProvider
name|SOURCE
init|=
name|ChainedRelMetadataProvider
operator|.
name|of
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
name|ReflectiveRelMetadataProvider
operator|.
name|reflectiveSource
argument_list|(
name|BuiltInMethod
operator|.
name|COLLATIONS
operator|.
name|method
argument_list|,
operator|new
name|HiveRelMdCollation
argument_list|()
argument_list|)
argument_list|,
name|RelMdCollation
operator|.
name|SOURCE
argument_list|)
argument_list|)
decl_stmt|;
comment|//~ Constructors -----------------------------------------------------------
specifier|private
name|HiveRelMdCollation
parameter_list|()
block|{}
comment|//~ Methods ----------------------------------------------------------------
specifier|public
name|ImmutableList
argument_list|<
name|RelCollation
argument_list|>
name|collations
parameter_list|(
name|HiveAggregate
name|aggregate
parameter_list|)
block|{
comment|// Compute collations
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelFieldCollation
argument_list|>
name|collationListBuilder
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelFieldCollation
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|pos
range|:
name|aggregate
operator|.
name|getGroupSet
argument_list|()
operator|.
name|asList
argument_list|()
control|)
block|{
specifier|final
name|RelFieldCollation
name|fieldCollation
init|=
operator|new
name|RelFieldCollation
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|collationListBuilder
operator|.
name|add
argument_list|(
name|fieldCollation
argument_list|)
expr_stmt|;
block|}
comment|// Return aggregate collations
return|return
name|ImmutableList
operator|.
name|of
argument_list|(
name|RelCollationTraitDef
operator|.
name|INSTANCE
operator|.
name|canonize
argument_list|(
operator|new
name|HiveRelCollation
argument_list|(
name|collationListBuilder
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|ImmutableList
argument_list|<
name|RelCollation
argument_list|>
name|collations
parameter_list|(
name|HiveJoin
name|join
parameter_list|)
block|{
comment|// Compute collations
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelFieldCollation
argument_list|>
name|collationListBuilder
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelFieldCollation
argument_list|>
argument_list|()
decl_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelFieldCollation
argument_list|>
name|leftCollationListBuilder
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelFieldCollation
argument_list|>
argument_list|()
decl_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelFieldCollation
argument_list|>
name|rightCollationListBuilder
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RelFieldCollation
argument_list|>
argument_list|()
decl_stmt|;
name|JoinPredicateInfo
name|joinPredInfo
init|=
name|HiveCalciteUtil
operator|.
name|JoinPredicateInfo
operator|.
name|constructJoinPredicateInfo
argument_list|(
name|join
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
name|joinPredInfo
operator|.
name|getEquiJoinPredicateElements
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|JoinLeafPredicateInfo
name|joinLeafPredInfo
init|=
name|joinPredInfo
operator|.
name|getEquiJoinPredicateElements
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|leftPos
range|:
name|joinLeafPredInfo
operator|.
name|getProjsFromLeftPartOfJoinKeysInJoinSchema
argument_list|()
control|)
block|{
specifier|final
name|RelFieldCollation
name|leftFieldCollation
init|=
operator|new
name|RelFieldCollation
argument_list|(
name|leftPos
argument_list|)
decl_stmt|;
name|collationListBuilder
operator|.
name|add
argument_list|(
name|leftFieldCollation
argument_list|)
expr_stmt|;
name|leftCollationListBuilder
operator|.
name|add
argument_list|(
name|leftFieldCollation
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|rightPos
range|:
name|joinLeafPredInfo
operator|.
name|getProjsFromRightPartOfJoinKeysInJoinSchema
argument_list|()
control|)
block|{
specifier|final
name|RelFieldCollation
name|rightFieldCollation
init|=
operator|new
name|RelFieldCollation
argument_list|(
name|rightPos
argument_list|)
decl_stmt|;
name|collationListBuilder
operator|.
name|add
argument_list|(
name|rightFieldCollation
argument_list|)
expr_stmt|;
name|rightCollationListBuilder
operator|.
name|add
argument_list|(
name|rightFieldCollation
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Return join collations
specifier|final
name|ImmutableList
argument_list|<
name|RelCollation
argument_list|>
name|collation
decl_stmt|;
switch|switch
condition|(
name|join
operator|.
name|getJoinAlgorithm
argument_list|()
condition|)
block|{
case|case
name|SMB_JOIN
case|:
case|case
name|COMMON_JOIN
case|:
name|collation
operator|=
name|ImmutableList
operator|.
name|of
argument_list|(
name|RelCollationTraitDef
operator|.
name|INSTANCE
operator|.
name|canonize
argument_list|(
operator|new
name|HiveRelCollation
argument_list|(
name|collationListBuilder
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|BUCKET_JOIN
case|:
case|case
name|MAP_JOIN
case|:
comment|// Keep order from the streaming relation
if|if
condition|(
name|join
operator|.
name|getMapJoinStreamingSide
argument_list|()
operator|==
name|MapJoinStreamingRelation
operator|.
name|LEFT_RELATION
condition|)
block|{
name|collation
operator|=
name|ImmutableList
operator|.
name|of
argument_list|(
name|RelCollationTraitDef
operator|.
name|INSTANCE
operator|.
name|canonize
argument_list|(
operator|new
name|HiveRelCollation
argument_list|(
name|leftCollationListBuilder
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|join
operator|.
name|getMapJoinStreamingSide
argument_list|()
operator|==
name|MapJoinStreamingRelation
operator|.
name|RIGHT_RELATION
condition|)
block|{
name|collation
operator|=
name|ImmutableList
operator|.
name|of
argument_list|(
name|RelCollationTraitDef
operator|.
name|INSTANCE
operator|.
name|canonize
argument_list|(
operator|new
name|HiveRelCollation
argument_list|(
name|rightCollationListBuilder
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|collation
operator|=
literal|null
expr_stmt|;
block|}
break|break;
default|default:
name|collation
operator|=
literal|null
expr_stmt|;
block|}
return|return
name|collation
return|;
block|}
block|}
end_class

end_unit

