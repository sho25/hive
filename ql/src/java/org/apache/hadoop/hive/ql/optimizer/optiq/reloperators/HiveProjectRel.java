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
name|reloperators
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
name|HiveOptiqUtil
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
name|TraitsUtil
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
name|HiveCost
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
name|ProjectRelBase
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
name|RelCollation
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
name|RelFactories
operator|.
name|ProjectFactory
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
name|RelOptCost
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
name|RelOptRule
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
name|reltype
operator|.
name|RelDataType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|reltype
operator|.
name|RelDataTypeField
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
name|org
operator|.
name|eigenbase
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
name|eigenbase
operator|.
name|util
operator|.
name|mapping
operator|.
name|MappingType
import|;
end_import

begin_class
specifier|public
class|class
name|HiveProjectRel
extends|extends
name|ProjectRelBase
implements|implements
name|HiveRel
block|{
specifier|public
specifier|static
specifier|final
name|ProjectFactory
name|DEFAULT_PROJECT_FACTORY
init|=
operator|new
name|HiveProjectFactoryImpl
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|m_virtualCols
decl_stmt|;
comment|/**    * Creates a HiveProjectRel.    *    * @param cluster    *          Cluster this relational expression belongs to    * @param child    *          input relational expression    * @param exps    *          List of expressions for the input columns    * @param rowType    *          output row type    * @param flags    *          values as in {@link ProjectRelBase.Flags}    */
specifier|public
name|HiveProjectRel
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
name|List
argument_list|<
name|?
extends|extends
name|RexNode
argument_list|>
name|exps
parameter_list|,
name|RelDataType
name|rowType
parameter_list|,
name|int
name|flags
parameter_list|)
block|{
name|super
argument_list|(
name|cluster
argument_list|,
name|traitSet
argument_list|,
name|child
argument_list|,
name|exps
argument_list|,
name|rowType
argument_list|,
name|flags
argument_list|)
expr_stmt|;
name|m_virtualCols
operator|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|HiveOptiqUtil
operator|.
name|getVirtualCols
argument_list|(
name|exps
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a HiveProjectRel with no sort keys.    *    * @param child    *          input relational expression    * @param exps    *          set of expressions for the input columns    * @param fieldNames    *          aliases of the expressions    */
specifier|public
specifier|static
name|HiveProjectRel
name|create
parameter_list|(
name|RelNode
name|child
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|RexNode
argument_list|>
name|exps
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
parameter_list|)
block|{
name|RelOptCluster
name|cluster
init|=
name|child
operator|.
name|getCluster
argument_list|()
decl_stmt|;
name|RelDataType
name|rowType
init|=
name|RexUtil
operator|.
name|createStructType
argument_list|(
name|cluster
operator|.
name|getTypeFactory
argument_list|()
argument_list|,
name|exps
argument_list|,
name|fieldNames
argument_list|)
decl_stmt|;
return|return
name|create
argument_list|(
name|cluster
argument_list|,
name|child
argument_list|,
name|exps
argument_list|,
name|rowType
argument_list|,
name|Collections
operator|.
expr|<
name|RelCollation
operator|>
name|emptyList
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Creates a HiveProjectRel.    */
specifier|public
specifier|static
name|HiveProjectRel
name|create
parameter_list|(
name|RelOptCluster
name|cluster
parameter_list|,
name|RelNode
name|child
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|RexNode
argument_list|>
name|exps
parameter_list|,
name|RelDataType
name|rowType
parameter_list|,
specifier|final
name|List
argument_list|<
name|RelCollation
argument_list|>
name|collationList
parameter_list|)
block|{
name|RelTraitSet
name|traitSet
init|=
name|TraitsUtil
operator|.
name|getSelectTraitSet
argument_list|(
name|cluster
argument_list|,
name|child
argument_list|)
decl_stmt|;
return|return
operator|new
name|HiveProjectRel
argument_list|(
name|cluster
argument_list|,
name|traitSet
argument_list|,
name|child
argument_list|,
name|exps
argument_list|,
name|rowType
argument_list|,
name|Flags
operator|.
name|BOXED
argument_list|)
return|;
block|}
comment|/**    * Creates a relational expression which projects the output fields of a    * relational expression according to a partial mapping.    *    *<p>    * A partial mapping is weaker than a permutation: every target has one    * source, but a source may have 0, 1 or more than one targets. Usually the    * result will have fewer fields than the source, unless some source fields    * are projected multiple times.    *    *<p>    * This method could optimize the result as {@link #permute} does, but does    * not at present.    *    * @param rel    *          Relational expression    * @param mapping    *          Mapping from source fields to target fields. The mapping type must    *          obey the constraints {@link MappingType#isMandatorySource()} and    *          {@link MappingType#isSingleSource()}, as does    *          {@link MappingType#INVERSE_FUNCTION}.    * @param fieldNames    *          Field names; if null, or if a particular entry is null, the name    *          of the permuted field is used    * @return relational expression which projects a subset of the input fields    */
specifier|public
specifier|static
name|RelNode
name|projectMapping
parameter_list|(
name|RelNode
name|rel
parameter_list|,
name|Mapping
name|mapping
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
parameter_list|)
block|{
assert|assert
name|mapping
operator|.
name|getMappingType
argument_list|()
operator|.
name|isSingleSource
argument_list|()
assert|;
assert|assert
name|mapping
operator|.
name|getMappingType
argument_list|()
operator|.
name|isMandatorySource
argument_list|()
assert|;
if|if
condition|(
name|mapping
operator|.
name|isIdentity
argument_list|()
condition|)
block|{
return|return
name|rel
return|;
block|}
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|outputNameList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|RexNode
argument_list|>
name|outputProjList
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
name|RelDataTypeField
argument_list|>
name|fields
init|=
name|rel
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
decl_stmt|;
specifier|final
name|RexBuilder
name|rexBuilder
init|=
name|rel
operator|.
name|getCluster
argument_list|()
operator|.
name|getRexBuilder
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
name|mapping
operator|.
name|getTargetCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|int
name|source
init|=
name|mapping
operator|.
name|getSource
argument_list|(
name|i
argument_list|)
decl_stmt|;
specifier|final
name|RelDataTypeField
name|sourceField
init|=
name|fields
operator|.
name|get
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|outputNameList
operator|.
name|add
argument_list|(
operator|(
operator|(
name|fieldNames
operator|==
literal|null
operator|)
operator|||
operator|(
name|fieldNames
operator|.
name|size
argument_list|()
operator|<=
name|i
operator|)
operator|||
operator|(
name|fieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|==
literal|null
operator|)
operator|)
condition|?
name|sourceField
operator|.
name|getName
argument_list|()
else|:
name|fieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|outputProjList
operator|.
name|add
argument_list|(
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|rel
argument_list|,
name|source
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|create
argument_list|(
name|rel
argument_list|,
name|outputProjList
argument_list|,
name|outputNameList
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ProjectRelBase
name|copy
parameter_list|(
name|RelTraitSet
name|traitSet
parameter_list|,
name|RelNode
name|input
parameter_list|,
name|List
argument_list|<
name|RexNode
argument_list|>
name|exps
parameter_list|,
name|RelDataType
name|rowType
parameter_list|)
block|{
assert|assert
name|traitSet
operator|.
name|containsIfApplicable
argument_list|(
name|HiveRel
operator|.
name|CONVENTION
argument_list|)
assert|;
return|return
operator|new
name|HiveProjectRel
argument_list|(
name|getCluster
argument_list|()
argument_list|,
name|traitSet
argument_list|,
name|input
argument_list|,
name|exps
argument_list|,
name|rowType
argument_list|,
name|getFlags
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RelOptCost
name|computeSelfCost
parameter_list|(
name|RelOptPlanner
name|planner
parameter_list|)
block|{
return|return
name|HiveCost
operator|.
name|FACTORY
operator|.
name|makeZeroCost
argument_list|()
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
name|List
argument_list|<
name|Integer
argument_list|>
name|getVirtualCols
parameter_list|()
block|{
return|return
name|m_virtualCols
return|;
block|}
comment|/**    * Implementation of {@link ProjectFactory} that returns    * {@link org.apache.hadoop.hive.ql.optimizer.optiq.reloperators.HiveProjectRel}    * .    */
specifier|private
specifier|static
class|class
name|HiveProjectFactoryImpl
implements|implements
name|ProjectFactory
block|{
annotation|@
name|Override
specifier|public
name|RelNode
name|createProject
parameter_list|(
name|RelNode
name|child
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|RexNode
argument_list|>
name|childExprs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
parameter_list|)
block|{
name|RelNode
name|project
init|=
name|HiveProjectRel
operator|.
name|create
argument_list|(
name|child
argument_list|,
name|childExprs
argument_list|,
name|fieldNames
argument_list|)
decl_stmt|;
comment|// Make sure extra traits are carried over from the original rel
name|project
operator|=
name|RelOptRule
operator|.
name|convert
argument_list|(
name|project
argument_list|,
name|child
operator|.
name|getTraitSet
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|project
return|;
block|}
block|}
block|}
end_class

end_unit

