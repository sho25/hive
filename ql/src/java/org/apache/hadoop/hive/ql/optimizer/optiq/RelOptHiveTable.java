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
name|BitSet
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
name|Set
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
name|common
operator|.
name|StatsSetupConst
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
name|ql
operator|.
name|exec
operator|.
name|ColumnInfo
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
name|metadata
operator|.
name|HiveException
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
name|metadata
operator|.
name|Table
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
name|ExprNodeConverter
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
name|ppr
operator|.
name|PartitionPruner
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
name|PrunedPartitionList
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
name|ColStatistics
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
name|ExprNodeDesc
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
name|Statistics
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
name|stats
operator|.
name|StatsUtils
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
name|TableAccessRel
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
name|RelOptAbstractTable
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
name|RelOptUtil
operator|.
name|InputFinder
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
name|rex
operator|.
name|RexNode
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
operator|.
name|Builder
import|;
end_import

begin_class
specifier|public
class|class
name|RelOptHiveTable
extends|extends
name|RelOptAbstractTable
block|{
specifier|private
specifier|final
name|Table
name|m_hiveTblMetadata
decl_stmt|;
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|ColumnInfo
argument_list|>
name|m_hiveNonPartitionCols
decl_stmt|;
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|Integer
argument_list|,
name|ColumnInfo
argument_list|>
name|m_hiveNonPartitionColsMap
decl_stmt|;
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|Integer
argument_list|,
name|ColumnInfo
argument_list|>
name|m_hivePartitionColsMap
decl_stmt|;
specifier|private
specifier|final
name|int
name|m_noOfProjs
decl_stmt|;
specifier|final
name|HiveConf
name|m_hiveConf
decl_stmt|;
specifier|private
name|double
name|m_rowCount
init|=
operator|-
literal|1
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|ColStatistics
argument_list|>
name|m_hiveColStatsMap
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|ColStatistics
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|Integer
name|m_numPartitions
decl_stmt|;
name|PrunedPartitionList
name|partitionList
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|PrunedPartitionList
argument_list|>
name|partitionCache
decl_stmt|;
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
name|RelOptHiveTable
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|RelOptHiveTable
parameter_list|(
name|RelOptSchema
name|optiqSchema
parameter_list|,
name|String
name|name
parameter_list|,
name|RelDataType
name|rowType
parameter_list|,
name|Table
name|hiveTblMetadata
parameter_list|,
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|hiveNonPartitionCols
parameter_list|,
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|hivePartitionCols
parameter_list|,
name|HiveConf
name|hconf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|PrunedPartitionList
argument_list|>
name|partitionCache
parameter_list|)
block|{
name|super
argument_list|(
name|optiqSchema
argument_list|,
name|name
argument_list|,
name|rowType
argument_list|)
expr_stmt|;
name|m_hiveTblMetadata
operator|=
name|hiveTblMetadata
expr_stmt|;
name|m_hiveNonPartitionCols
operator|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|hiveNonPartitionCols
argument_list|)
expr_stmt|;
name|m_hiveNonPartitionColsMap
operator|=
name|getColInfoMap
argument_list|(
name|hiveNonPartitionCols
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|m_hivePartitionColsMap
operator|=
name|getColInfoMap
argument_list|(
name|hivePartitionCols
argument_list|,
name|m_hiveNonPartitionColsMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|m_noOfProjs
operator|=
name|hiveNonPartitionCols
operator|.
name|size
argument_list|()
operator|+
name|hivePartitionCols
operator|.
name|size
argument_list|()
expr_stmt|;
name|m_hiveConf
operator|=
name|hconf
expr_stmt|;
name|this
operator|.
name|partitionCache
operator|=
name|partitionCache
expr_stmt|;
block|}
specifier|private
specifier|static
name|ImmutableMap
argument_list|<
name|Integer
argument_list|,
name|ColumnInfo
argument_list|>
name|getColInfoMap
parameter_list|(
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|hiveCols
parameter_list|,
name|int
name|startIndx
parameter_list|)
block|{
name|Builder
argument_list|<
name|Integer
argument_list|,
name|ColumnInfo
argument_list|>
name|bldr
init|=
name|ImmutableMap
operator|.
expr|<
name|Integer
decl_stmt|,
name|ColumnInfo
decl|>
name|builder
argument_list|()
decl_stmt|;
name|int
name|indx
init|=
name|startIndx
decl_stmt|;
for|for
control|(
name|ColumnInfo
name|ci
range|:
name|hiveCols
control|)
block|{
name|bldr
operator|.
name|put
argument_list|(
name|indx
argument_list|,
name|ci
argument_list|)
expr_stmt|;
name|indx
operator|++
expr_stmt|;
block|}
return|return
name|bldr
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isKey
parameter_list|(
name|BitSet
name|arg0
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|RelNode
name|toRel
parameter_list|(
name|ToRelContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|TableAccessRel
argument_list|(
name|context
operator|.
name|getCluster
argument_list|()
argument_list|,
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|unwrap
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|arg0
parameter_list|)
block|{
return|return
name|arg0
operator|.
name|isInstance
argument_list|(
name|this
argument_list|)
condition|?
name|arg0
operator|.
name|cast
argument_list|(
name|this
argument_list|)
else|:
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getRowCount
parameter_list|()
block|{
if|if
condition|(
name|m_rowCount
operator|==
operator|-
literal|1
condition|)
block|{
if|if
condition|(
literal|null
operator|==
name|partitionList
condition|)
block|{
comment|// we are here either unpartitioned table or partitioned table with no predicates
name|computePartitionList
argument_list|(
name|m_hiveConf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|m_hiveTblMetadata
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|Long
argument_list|>
name|rowCounts
init|=
name|StatsUtils
operator|.
name|getBasicStatForPartitions
argument_list|(
name|m_hiveTblMetadata
argument_list|,
name|partitionList
operator|.
name|getNotDeniedPartns
argument_list|()
argument_list|,
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
decl_stmt|;
name|m_rowCount
operator|=
name|StatsUtils
operator|.
name|getSumIgnoreNegatives
argument_list|(
name|rowCounts
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|m_rowCount
operator|=
name|StatsUtils
operator|.
name|getNumRows
argument_list|(
name|m_hiveTblMetadata
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|m_rowCount
return|;
block|}
specifier|public
name|Table
name|getHiveTableMD
parameter_list|()
block|{
return|return
name|m_hiveTblMetadata
return|;
block|}
specifier|private
name|String
name|getColNamesForLogging
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|colLst
parameter_list|)
block|{
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|boolean
name|firstEntry
init|=
literal|true
decl_stmt|;
for|for
control|(
name|String
name|colName
range|:
name|colLst
control|)
block|{
if|if
condition|(
name|firstEntry
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|colName
argument_list|)
expr_stmt|;
name|firstEntry
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
operator|+
name|colName
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|void
name|computePartitionList
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|RexNode
name|pruneNode
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
operator|!
name|m_hiveTblMetadata
operator|.
name|isPartitioned
argument_list|()
operator|||
name|pruneNode
operator|==
literal|null
operator|||
name|InputFinder
operator|.
name|bits
argument_list|(
name|pruneNode
argument_list|)
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// there is no predicate on partitioning column, we need all partitions in this case.
name|partitionList
operator|=
name|PartitionPruner
operator|.
name|prune
argument_list|(
name|m_hiveTblMetadata
argument_list|,
literal|null
argument_list|,
name|conf
argument_list|,
name|getName
argument_list|()
argument_list|,
name|partitionCache
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// We have valid pruning expressions, only retrieve qualifying partitions
name|ExprNodeDesc
name|pruneExpr
init|=
name|pruneNode
operator|.
name|accept
argument_list|(
operator|new
name|ExprNodeConverter
argument_list|(
name|getName
argument_list|()
argument_list|,
name|getRowType
argument_list|()
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|partitionList
operator|=
name|PartitionPruner
operator|.
name|prune
argument_list|(
name|m_hiveTblMetadata
argument_list|,
name|pruneExpr
argument_list|,
name|conf
argument_list|,
name|getName
argument_list|()
argument_list|,
name|partitionCache
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|he
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|he
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|updateColStats
parameter_list|(
name|Set
argument_list|<
name|Integer
argument_list|>
name|projIndxLst
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|nonPartColNamesThatRqrStats
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|nonPartColIndxsThatRqrStats
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partColNamesThatRqrStats
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|partColIndxsThatRqrStats
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|colNamesFailedStats
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// 1. Separate required columns to Non Partition and Partition Cols
name|ColumnInfo
name|tmp
decl_stmt|;
for|for
control|(
name|Integer
name|pi
range|:
name|projIndxLst
control|)
block|{
if|if
condition|(
name|m_hiveColStatsMap
operator|.
name|get
argument_list|(
name|pi
argument_list|)
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|(
name|tmp
operator|=
name|m_hiveNonPartitionColsMap
operator|.
name|get
argument_list|(
name|pi
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|nonPartColNamesThatRqrStats
operator|.
name|add
argument_list|(
name|tmp
operator|.
name|getInternalName
argument_list|()
argument_list|)
expr_stmt|;
name|nonPartColIndxsThatRqrStats
operator|.
name|add
argument_list|(
name|pi
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|tmp
operator|=
name|m_hivePartitionColsMap
operator|.
name|get
argument_list|(
name|pi
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|partColNamesThatRqrStats
operator|.
name|add
argument_list|(
name|tmp
operator|.
name|getInternalName
argument_list|()
argument_list|)
expr_stmt|;
name|partColIndxsThatRqrStats
operator|.
name|add
argument_list|(
name|pi
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|logMsg
init|=
literal|"Unable to find Column Index: "
operator|+
name|pi
operator|+
literal|", in "
operator|+
name|m_hiveTblMetadata
operator|.
name|getCompleteName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|logMsg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|logMsg
argument_list|)
throw|;
block|}
block|}
block|}
comment|// 2. Obtain Col Stats for Non Partition Cols
if|if
condition|(
name|nonPartColNamesThatRqrStats
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|hiveColStats
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|partitionList
condition|)
block|{
comment|// We could be here either because its an unpartitioned table or because
comment|// there are no pruning predicates on a partitioned table.
name|computePartitionList
argument_list|(
name|m_hiveConf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|m_hiveTblMetadata
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
comment|// 2.1 Handle the case for unpartitioned table.
name|hiveColStats
operator|=
name|StatsUtils
operator|.
name|getTableColumnStats
argument_list|(
name|m_hiveTblMetadata
argument_list|,
name|m_hiveNonPartitionCols
argument_list|,
name|nonPartColNamesThatRqrStats
argument_list|)
expr_stmt|;
comment|// 2.1.1 Record Column Names that we needed stats for but couldn't
if|if
condition|(
name|hiveColStats
operator|==
literal|null
condition|)
block|{
name|colNamesFailedStats
operator|.
name|addAll
argument_list|(
name|nonPartColNamesThatRqrStats
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|hiveColStats
operator|.
name|size
argument_list|()
operator|!=
name|nonPartColNamesThatRqrStats
operator|.
name|size
argument_list|()
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|setOfFiledCols
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|nonPartColNamesThatRqrStats
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|setOfObtainedColStats
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ColStatistics
name|cs
range|:
name|hiveColStats
control|)
block|{
name|setOfObtainedColStats
operator|.
name|add
argument_list|(
name|cs
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|setOfFiledCols
operator|.
name|removeAll
argument_list|(
name|setOfObtainedColStats
argument_list|)
expr_stmt|;
name|colNamesFailedStats
operator|.
name|addAll
argument_list|(
name|setOfFiledCols
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// 2.2 Obtain col stats for partitioned table.
try|try
block|{
name|Statistics
name|stats
init|=
name|StatsUtils
operator|.
name|collectStatistics
argument_list|(
name|m_hiveConf
argument_list|,
name|partitionList
argument_list|,
name|m_hiveTblMetadata
argument_list|,
name|m_hiveNonPartitionCols
argument_list|,
name|nonPartColNamesThatRqrStats
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|m_rowCount
operator|=
name|stats
operator|.
name|getNumRows
argument_list|()
expr_stmt|;
name|hiveColStats
operator|=
operator|new
name|ArrayList
argument_list|<
name|ColStatistics
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|c
range|:
name|nonPartColNamesThatRqrStats
control|)
block|{
name|ColStatistics
name|cs
init|=
name|stats
operator|.
name|getColumnStatisticsFromColName
argument_list|(
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
name|cs
operator|!=
literal|null
condition|)
block|{
name|hiveColStats
operator|.
name|add
argument_list|(
name|cs
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|colNamesFailedStats
operator|.
name|add
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|String
name|logMsg
init|=
literal|"Collecting stats failed."
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|logMsg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|logMsg
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|hiveColStats
operator|!=
literal|null
operator|&&
name|hiveColStats
operator|.
name|size
argument_list|()
operator|==
name|nonPartColNamesThatRqrStats
operator|.
name|size
argument_list|()
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|hiveColStats
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|m_hiveColStatsMap
operator|.
name|put
argument_list|(
name|nonPartColIndxsThatRqrStats
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|hiveColStats
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// 3. Obtain Stats for Partition Cols
comment|// TODO: Just using no of partitions for NDV is a gross approximation for
comment|// multi col partitions; Hack till HIVE-7392 gets fixed.
if|if
condition|(
name|colNamesFailedStats
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
name|partColNamesThatRqrStats
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|m_numPartitions
operator|=
name|partitionList
operator|.
name|getPartitions
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
name|ColStatistics
name|cStats
init|=
literal|null
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
name|partColNamesThatRqrStats
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|cStats
operator|=
operator|new
name|ColStatistics
argument_list|(
name|m_hiveTblMetadata
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partColNamesThatRqrStats
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|m_hivePartitionColsMap
operator|.
name|get
argument_list|(
name|partColIndxsThatRqrStats
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
name|cStats
operator|.
name|setCountDistint
argument_list|(
name|m_numPartitions
argument_list|)
expr_stmt|;
name|m_hiveColStatsMap
operator|.
name|put
argument_list|(
name|partColIndxsThatRqrStats
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|cStats
argument_list|)
expr_stmt|;
block|}
block|}
comment|// 4. Warn user if we could get stats for required columns
if|if
condition|(
operator|!
name|colNamesFailedStats
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|String
name|logMsg
init|=
literal|"No Stats for "
operator|+
name|m_hiveTblMetadata
operator|.
name|getCompleteName
argument_list|()
operator|+
literal|", Columns: "
operator|+
name|getColNamesForLogging
argument_list|(
name|colNamesFailedStats
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|logMsg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|logMsg
argument_list|)
throw|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|getColStat
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|projIndxLst
parameter_list|)
block|{
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|ColStatistics
argument_list|>
name|colStatsBldr
init|=
name|ImmutableList
operator|.
expr|<
name|ColStatistics
operator|>
name|builder
argument_list|()
decl_stmt|;
if|if
condition|(
name|projIndxLst
operator|!=
literal|null
condition|)
block|{
name|updateColStats
argument_list|(
operator|new
name|HashSet
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|projIndxLst
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Integer
name|i
range|:
name|projIndxLst
control|)
block|{
name|colStatsBldr
operator|.
name|add
argument_list|(
name|m_hiveColStatsMap
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|pILst
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Integer
name|i
init|=
literal|0
init|;
name|i
operator|<
name|m_noOfProjs
condition|;
name|i
operator|++
control|)
block|{
name|pILst
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|updateColStats
argument_list|(
operator|new
name|HashSet
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|pILst
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Integer
name|pi
range|:
name|pILst
control|)
block|{
name|colStatsBldr
operator|.
name|add
argument_list|(
name|m_hiveColStatsMap
operator|.
name|get
argument_list|(
name|pi
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|colStatsBldr
operator|.
name|build
argument_list|()
return|;
block|}
comment|/*    * use to check if a set of columns are all partition columns.    * true only if:    * - all columns in BitSet are partition    * columns.    */
specifier|public
name|boolean
name|containsPartitionColumnsOnly
parameter_list|(
name|BitSet
name|cols
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
name|cols
operator|.
name|nextSetBit
argument_list|(
literal|0
argument_list|)
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|++
operator|,
name|i
operator|=
name|cols
operator|.
name|nextSetBit
argument_list|(
name|i
operator|+
literal|1
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|m_hivePartitionColsMap
operator|.
name|containsKey
argument_list|(
name|i
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

