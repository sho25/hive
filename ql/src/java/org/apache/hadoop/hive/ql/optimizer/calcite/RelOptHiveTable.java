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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|RelOptAbstractTable
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
name|RelOptSchema
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
operator|.
name|InputFinder
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
name|logical
operator|.
name|LogicalTableScan
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
name|Partition
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
name|metadata
operator|.
name|VirtualColumn
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
name|hiveTblMetadata
decl_stmt|;
specifier|private
specifier|final
name|String
name|tblAlias
decl_stmt|;
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|ColumnInfo
argument_list|>
name|hiveNonPartitionCols
decl_stmt|;
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|ColumnInfo
argument_list|>
name|hivePartitionCols
decl_stmt|;
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|Integer
argument_list|,
name|ColumnInfo
argument_list|>
name|hiveNonPartitionColsMap
decl_stmt|;
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|Integer
argument_list|,
name|ColumnInfo
argument_list|>
name|hivePartitionColsMap
decl_stmt|;
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|VirtualColumn
argument_list|>
name|hiveVirtualCols
decl_stmt|;
specifier|private
specifier|final
name|int
name|noOfNonVirtualCols
decl_stmt|;
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|double
name|rowCount
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
name|hiveColStatsMap
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
name|AtomicInteger
name|noColsMissingStats
decl_stmt|;
specifier|private
specifier|final
name|String
name|qbID
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
name|calciteSchema
parameter_list|,
name|String
name|qualifiedTblName
parameter_list|,
name|String
name|tblAlias
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
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|hiveVirtualCols
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
parameter_list|,
name|AtomicInteger
name|noColsMissingStats
parameter_list|,
name|String
name|qbID
parameter_list|)
block|{
name|super
argument_list|(
name|calciteSchema
argument_list|,
name|qualifiedTblName
argument_list|,
name|rowType
argument_list|)
expr_stmt|;
name|this
operator|.
name|hiveTblMetadata
operator|=
name|hiveTblMetadata
expr_stmt|;
name|this
operator|.
name|tblAlias
operator|=
name|tblAlias
expr_stmt|;
name|this
operator|.
name|hiveNonPartitionCols
operator|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|hiveNonPartitionCols
argument_list|)
expr_stmt|;
name|this
operator|.
name|hiveNonPartitionColsMap
operator|=
name|HiveCalciteUtil
operator|.
name|getColInfoMap
argument_list|(
name|hiveNonPartitionCols
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|hivePartitionCols
operator|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|hivePartitionCols
argument_list|)
expr_stmt|;
name|this
operator|.
name|hivePartitionColsMap
operator|=
name|HiveCalciteUtil
operator|.
name|getColInfoMap
argument_list|(
name|hivePartitionCols
argument_list|,
name|hiveNonPartitionColsMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|noOfNonVirtualCols
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
name|this
operator|.
name|hiveVirtualCols
operator|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|hiveVirtualCols
argument_list|)
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hconf
expr_stmt|;
name|this
operator|.
name|partitionCache
operator|=
name|partitionCache
expr_stmt|;
name|this
operator|.
name|noColsMissingStats
operator|=
name|noColsMissingStats
expr_stmt|;
name|this
operator|.
name|qbID
operator|=
name|qbID
expr_stmt|;
block|}
specifier|public
name|RelOptHiveTable
name|copy
parameter_list|(
name|RelDataType
name|newRowType
parameter_list|)
block|{
comment|// 1. Build map of column name to col index of original schema
comment|// Assumption: Hive Table can not contain duplicate column names
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|nameToColIndxMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|RelDataTypeField
name|f
range|:
name|this
operator|.
name|rowType
operator|.
name|getFieldList
argument_list|()
control|)
block|{
name|nameToColIndxMap
operator|.
name|put
argument_list|(
name|f
operator|.
name|getName
argument_list|()
argument_list|,
name|f
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// 2. Build nonPart/Part/Virtual column info for new RowSchema
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|newHiveNonPartitionCols
init|=
operator|new
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|newHivePartitionCols
init|=
operator|new
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|newHiveVirtualCols
init|=
operator|new
name|ArrayList
argument_list|<
name|VirtualColumn
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|VirtualColumn
argument_list|>
name|virtualColInfoMap
init|=
name|HiveCalciteUtil
operator|.
name|getVColsMap
argument_list|(
name|this
operator|.
name|hiveVirtualCols
argument_list|,
name|this
operator|.
name|noOfNonVirtualCols
argument_list|)
decl_stmt|;
name|Integer
name|originalColIndx
decl_stmt|;
name|ColumnInfo
name|cInfo
decl_stmt|;
name|VirtualColumn
name|vc
decl_stmt|;
for|for
control|(
name|RelDataTypeField
name|f
range|:
name|newRowType
operator|.
name|getFieldList
argument_list|()
control|)
block|{
name|originalColIndx
operator|=
name|nameToColIndxMap
operator|.
name|get
argument_list|(
name|f
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|cInfo
operator|=
name|hiveNonPartitionColsMap
operator|.
name|get
argument_list|(
name|originalColIndx
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|newHiveNonPartitionCols
operator|.
name|add
argument_list|(
operator|new
name|ColumnInfo
argument_list|(
name|cInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|cInfo
operator|=
name|hivePartitionColsMap
operator|.
name|get
argument_list|(
name|originalColIndx
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|newHivePartitionCols
operator|.
name|add
argument_list|(
operator|new
name|ColumnInfo
argument_list|(
name|cInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|vc
operator|=
name|virtualColInfoMap
operator|.
name|get
argument_list|(
name|originalColIndx
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
name|newHiveVirtualCols
operator|.
name|add
argument_list|(
name|vc
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Copy encountered a column not seen in original TS"
argument_list|)
throw|;
block|}
block|}
comment|// 3. Build new Table
return|return
operator|new
name|RelOptHiveTable
argument_list|(
name|this
operator|.
name|schema
argument_list|,
name|this
operator|.
name|name
argument_list|,
name|this
operator|.
name|tblAlias
argument_list|,
name|newRowType
argument_list|,
name|this
operator|.
name|hiveTblMetadata
argument_list|,
name|newHiveNonPartitionCols
argument_list|,
name|newHivePartitionCols
argument_list|,
name|newHiveVirtualCols
argument_list|,
name|this
operator|.
name|hiveConf
argument_list|,
name|this
operator|.
name|partitionCache
argument_list|,
name|this
operator|.
name|noColsMissingStats
argument_list|,
name|qbID
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isKey
parameter_list|(
name|ImmutableBitSet
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
name|LogicalTableScan
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
name|rowCount
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
comment|// we are here either unpartitioned table or partitioned table with no
comment|// predicates
name|computePartitionList
argument_list|(
name|hiveConf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hiveTblMetadata
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
name|hiveTblMetadata
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
name|rowCount
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
name|rowCount
operator|=
name|StatsUtils
operator|.
name|getNumRows
argument_list|(
name|hiveTblMetadata
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|rowCount
operator|==
operator|-
literal|1
condition|)
name|noColsMissingStats
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
return|return
name|rowCount
return|;
block|}
specifier|public
name|Table
name|getHiveTableMD
parameter_list|()
block|{
return|return
name|hiveTblMetadata
return|;
block|}
specifier|public
name|String
name|getTableAlias
parameter_list|()
block|{
comment|// NOTE: Calcite considers tbls to be equal if their names are the same.
comment|// Hence
comment|// we need to provide Calcite the fully qualified table name
comment|// (dbname.tblname)
comment|// and not the user provided aliases.
comment|// However in HIVE DB name can not appear in select list; in case of join
comment|// where table names differ only in DB name, Hive would require user
comment|// introducing explicit aliases for tbl.
if|if
condition|(
name|tblAlias
operator|==
literal|null
condition|)
return|return
name|hiveTblMetadata
operator|.
name|getTableName
argument_list|()
return|;
else|else
return|return
name|tblAlias
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
name|hiveTblMetadata
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
comment|// there is no predicate on partitioning column, we need all partitions
comment|// in this case.
name|partitionList
operator|=
name|PartitionPruner
operator|.
name|prune
argument_list|(
name|hiveTblMetadata
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
name|hiveTblMetadata
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
name|hiveColStatsMap
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
name|hiveNonPartitionColsMap
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
name|hivePartitionColsMap
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
name|noColsMissingStats
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
name|String
name|logMsg
init|=
literal|"Unable to find Column Index: "
operator|+
name|pi
operator|+
literal|", in "
operator|+
name|hiveTblMetadata
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
name|hiveConf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
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
operator|!
name|hiveTblMetadata
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
name|hiveTblMetadata
argument_list|,
name|hiveNonPartitionCols
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
if|if
condition|(
name|partitionList
operator|.
name|getNotDeniedPartns
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// no need to make a metastore call
name|rowCount
operator|=
literal|0
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
comment|// add empty stats object for each column
name|hiveColStats
operator|.
name|add
argument_list|(
operator|new
name|ColStatistics
argument_list|(
name|hiveTblMetadata
operator|.
name|getTableName
argument_list|()
argument_list|,
name|c
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|colNamesFailedStats
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|Statistics
name|stats
init|=
name|StatsUtils
operator|.
name|collectStatistics
argument_list|(
name|hiveConf
argument_list|,
name|partitionList
argument_list|,
name|hiveTblMetadata
argument_list|,
name|hiveNonPartitionCols
argument_list|,
name|nonPartColNamesThatRqrStats
argument_list|,
name|nonPartColNamesThatRqrStats
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|rowCount
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
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|logMsg
argument_list|,
name|e
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
name|hiveColStatsMap
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
name|hiveTblMetadata
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
name|hivePartitionColsMap
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
name|getDistinctCount
argument_list|(
name|partitionList
operator|.
name|getPartitions
argument_list|()
argument_list|,
name|partColNamesThatRqrStats
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|hiveColStatsMap
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
name|hiveTblMetadata
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
name|noColsMissingStats
operator|.
name|getAndAdd
argument_list|(
name|colNamesFailedStats
operator|.
name|size
argument_list|()
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
specifier|private
name|int
name|getDistinctCount
parameter_list|(
name|Set
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|,
name|String
name|partColName
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|distinctVals
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|partitions
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Partition
name|partition
range|:
name|partitions
control|)
block|{
name|distinctVals
operator|.
name|add
argument_list|(
name|partition
operator|.
name|getSpec
argument_list|()
operator|.
name|get
argument_list|(
name|partColName
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|distinctVals
operator|.
name|size
argument_list|()
return|;
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
name|hiveColStatsMap
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
name|noOfNonVirtualCols
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
name|hiveColStatsMap
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
comment|/*    * use to check if a set of columns are all partition columns. true only if: -    * all columns in BitSet are partition columns.    */
specifier|public
name|boolean
name|containsPartitionColumnsOnly
parameter_list|(
name|ImmutableBitSet
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
name|hivePartitionColsMap
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
specifier|public
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|getVirtualCols
parameter_list|()
block|{
return|return
name|this
operator|.
name|hiveVirtualCols
return|;
block|}
specifier|public
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|getPartColumns
parameter_list|()
block|{
return|return
name|this
operator|.
name|hivePartitionCols
return|;
block|}
specifier|public
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|getNonPartColumns
parameter_list|()
block|{
return|return
name|this
operator|.
name|hiveNonPartitionCols
return|;
block|}
specifier|public
name|String
name|getQBID
parameter_list|()
block|{
return|return
name|qbID
return|;
block|}
block|}
end_class

end_unit

