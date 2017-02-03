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
name|io
operator|.
name|Serializable
import|;
end_import

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
name|Arrays
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
name|io
operator|.
name|AcidUtils
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
name|parse
operator|.
name|TableSample
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
name|serde
operator|.
name|serdeConstants
import|;
end_import

begin_comment
comment|/**  * Table Scan Descriptor Currently, data is only read from a base source as part  * of map-reduce framework. So, nothing is stored in the descriptor. But, more  * things will be added here as table scan is invoked as part of local work.  **/
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"TableScan"
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
name|TableScanDesc
extends|extends
name|AbstractOperatorDesc
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|String
name|alias
decl_stmt|;
specifier|private
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|virtualCols
decl_stmt|;
specifier|private
name|String
name|statsAggKeyPrefix
decl_stmt|;
comment|// stats publishing/aggregating key prefix
comment|/**   * A list of the partition columns of the table.   * Set by the semantic analyzer only in case of the analyze command.   */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|partColumns
decl_stmt|;
comment|/**    * Used for split sampling (row count per split)    * For example,    *   select count(1) from ss_src2 tablesample (10 ROWS) s;    * provides first 10 rows from all input splits    */
specifier|private
name|int
name|rowLimit
init|=
operator|-
literal|1
decl_stmt|;
comment|/**    * A boolean variable set to true by the semantic analyzer only in case of the analyze command.    *    */
specifier|private
name|boolean
name|gatherStats
decl_stmt|;
specifier|private
name|boolean
name|statsReliable
decl_stmt|;
specifier|private
name|String
name|tmpStatsDir
decl_stmt|;
specifier|private
name|ExprNodeGenericFuncDesc
name|filterExpr
decl_stmt|;
specifier|private
specifier|transient
name|Serializable
name|filterObject
decl_stmt|;
specifier|private
name|String
name|serializedFilterExpr
decl_stmt|;
specifier|private
name|String
name|serializedFilterObject
decl_stmt|;
comment|// Both neededColumnIDs and neededColumns should never be null.
comment|// When neededColumnIDs is an empty list,
comment|// it means no needed column (e.g. we do not need any column to evaluate
comment|// SELECT count(*) FROM t).
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|neededColumnIDs
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|neededColumns
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|neededNestedColumnPaths
decl_stmt|;
comment|// all column names referenced including virtual columns. used in ColumnAccessAnalyzer
specifier|private
specifier|transient
name|List
argument_list|<
name|String
argument_list|>
name|referencedColumns
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FILTER_EXPR_CONF_STR
init|=
literal|"hive.io.filter.expr.serialized"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FILTER_TEXT_CONF_STR
init|=
literal|"hive.io.filter.text"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FILTER_OBJECT_CONF_STR
init|=
literal|"hive.io.filter.object"
decl_stmt|;
comment|// input file name (big) to bucket number
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|bucketFileNameMapping
decl_stmt|;
specifier|private
name|boolean
name|isMetadataOnly
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|isAcidTable
decl_stmt|;
specifier|private
name|AcidUtils
operator|.
name|AcidOperationalProperties
name|acidOperationalProperties
init|=
literal|null
decl_stmt|;
specifier|private
specifier|transient
name|TableSample
name|tableSample
decl_stmt|;
specifier|private
specifier|transient
name|Table
name|tableMetadata
decl_stmt|;
specifier|private
name|BitSet
name|includedBuckets
decl_stmt|;
specifier|private
name|int
name|numBuckets
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|TableScanDesc
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
name|TableScanDesc
parameter_list|(
name|Table
name|tblMetadata
parameter_list|)
block|{
name|this
argument_list|(
literal|null
argument_list|,
name|tblMetadata
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TableScanDesc
parameter_list|(
specifier|final
name|String
name|alias
parameter_list|,
name|Table
name|tblMetadata
parameter_list|)
block|{
name|this
argument_list|(
name|alias
argument_list|,
literal|null
argument_list|,
name|tblMetadata
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TableScanDesc
parameter_list|(
specifier|final
name|String
name|alias
parameter_list|,
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|vcs
parameter_list|,
name|Table
name|tblMetadata
parameter_list|)
block|{
name|this
operator|.
name|alias
operator|=
name|alias
expr_stmt|;
name|this
operator|.
name|virtualCols
operator|=
name|vcs
expr_stmt|;
name|this
operator|.
name|tableMetadata
operator|=
name|tblMetadata
expr_stmt|;
name|isAcidTable
operator|=
name|AcidUtils
operator|.
name|isAcidTable
argument_list|(
name|this
operator|.
name|tableMetadata
argument_list|)
expr_stmt|;
if|if
condition|(
name|isAcidTable
condition|)
block|{
name|acidOperationalProperties
operator|=
name|AcidUtils
operator|.
name|getAcidOperationalProperties
argument_list|(
name|this
operator|.
name|tableMetadata
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|clone
parameter_list|()
block|{
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|vcs
init|=
operator|new
name|ArrayList
argument_list|<
name|VirtualColumn
argument_list|>
argument_list|(
name|getVirtualCols
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|TableScanDesc
argument_list|(
name|getAlias
argument_list|()
argument_list|,
name|vcs
argument_list|,
name|this
operator|.
name|tableMetadata
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"alias"
argument_list|)
specifier|public
name|String
name|getAlias
parameter_list|()
block|{
return|return
name|alias
return|;
block|}
annotation|@
name|Explain
argument_list|(
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
name|getTbl
parameter_list|()
block|{
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|tableMetadata
operator|.
name|getCompleteName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|","
operator|+
name|alias
argument_list|)
expr_stmt|;
if|if
condition|(
name|isAcidTable
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", ACID table"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|",Tbl:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|statistics
operator|.
name|getBasicStatsState
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",Col:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|statistics
operator|.
name|getColumnStatsState
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isAcidTable
parameter_list|()
block|{
return|return
name|isAcidTable
return|;
block|}
specifier|public
name|AcidUtils
operator|.
name|AcidOperationalProperties
name|getAcidOperationalProperties
parameter_list|()
block|{
return|return
name|acidOperationalProperties
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Output"
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
name|List
argument_list|<
name|String
argument_list|>
name|getOutputColumnNames
parameter_list|()
block|{
return|return
name|this
operator|.
name|neededColumns
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"filterExpr"
argument_list|)
specifier|public
name|String
name|getFilterExprString
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
name|filterExpr
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|ExprNodeGenericFuncDesc
name|getFilterExpr
parameter_list|()
block|{
return|return
name|filterExpr
return|;
block|}
specifier|public
name|void
name|setFilterExpr
parameter_list|(
name|ExprNodeGenericFuncDesc
name|filterExpr
parameter_list|)
block|{
name|this
operator|.
name|filterExpr
operator|=
name|filterExpr
expr_stmt|;
block|}
specifier|public
name|Serializable
name|getFilterObject
parameter_list|()
block|{
return|return
name|filterObject
return|;
block|}
specifier|public
name|void
name|setFilterObject
parameter_list|(
name|Serializable
name|filterObject
parameter_list|)
block|{
name|this
operator|.
name|filterObject
operator|=
name|filterObject
expr_stmt|;
block|}
specifier|public
name|void
name|setNeededColumnIDs
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|neededColumnIDs
parameter_list|)
block|{
name|this
operator|.
name|neededColumnIDs
operator|=
name|neededColumnIDs
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Integer
argument_list|>
name|getNeededColumnIDs
parameter_list|()
block|{
return|return
name|neededColumnIDs
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getNeededNestedColumnPaths
parameter_list|()
block|{
return|return
name|neededNestedColumnPaths
return|;
block|}
specifier|public
name|void
name|setNeededNestedColumnPaths
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|neededNestedColumnPaths
parameter_list|)
block|{
name|this
operator|.
name|neededNestedColumnPaths
operator|=
name|neededNestedColumnPaths
expr_stmt|;
block|}
specifier|public
name|void
name|setNeededColumns
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|neededColumns
parameter_list|)
block|{
name|this
operator|.
name|neededColumns
operator|=
name|neededColumns
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getNeededColumns
parameter_list|()
block|{
return|return
name|neededColumns
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Pruned Column Paths"
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getPrunedColumnPaths
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|p
range|:
name|neededNestedColumnPaths
control|)
block|{
if|if
condition|(
name|p
operator|.
name|indexOf
argument_list|(
literal|'.'
argument_list|)
operator|>=
literal|0
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
name|void
name|setReferencedColumns
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|referencedColumns
parameter_list|)
block|{
name|this
operator|.
name|referencedColumns
operator|=
name|referencedColumns
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getReferencedColumns
parameter_list|()
block|{
return|return
name|referencedColumns
return|;
block|}
specifier|public
name|void
name|setAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
name|this
operator|.
name|alias
operator|=
name|alias
expr_stmt|;
block|}
specifier|public
name|void
name|setPartColumns
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partColumns
parameter_list|)
block|{
name|this
operator|.
name|partColumns
operator|=
name|partColumns
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getPartColumns
parameter_list|()
block|{
return|return
name|partColumns
return|;
block|}
specifier|public
name|void
name|setGatherStats
parameter_list|(
name|boolean
name|gatherStats
parameter_list|)
block|{
name|this
operator|.
name|gatherStats
operator|=
name|gatherStats
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"GatherStats"
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
name|isGatherStats
parameter_list|()
block|{
return|return
name|gatherStats
return|;
block|}
specifier|public
name|String
name|getTmpStatsDir
parameter_list|()
block|{
return|return
name|tmpStatsDir
return|;
block|}
specifier|public
name|void
name|setTmpStatsDir
parameter_list|(
name|String
name|tmpStatsDir
parameter_list|)
block|{
name|this
operator|.
name|tmpStatsDir
operator|=
name|tmpStatsDir
expr_stmt|;
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
name|virtualCols
return|;
block|}
specifier|public
name|void
name|setVirtualCols
parameter_list|(
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|virtualCols
parameter_list|)
block|{
name|this
operator|.
name|virtualCols
operator|=
name|virtualCols
expr_stmt|;
block|}
specifier|public
name|void
name|addVirtualCols
parameter_list|(
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|virtualCols
parameter_list|)
block|{
name|this
operator|.
name|virtualCols
operator|.
name|addAll
argument_list|(
name|virtualCols
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasVirtualCols
parameter_list|()
block|{
return|return
name|virtualCols
operator|!=
literal|null
operator|&&
operator|!
name|virtualCols
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
name|void
name|setStatsAggPrefix
parameter_list|(
name|String
name|k
parameter_list|)
block|{
name|statsAggKeyPrefix
operator|=
name|k
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Statistics Aggregation Key Prefix"
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
name|getStatsAggPrefix
parameter_list|()
block|{
return|return
name|statsAggKeyPrefix
return|;
block|}
specifier|public
name|boolean
name|isStatsReliable
parameter_list|()
block|{
return|return
name|statsReliable
return|;
block|}
specifier|public
name|void
name|setStatsReliable
parameter_list|(
name|boolean
name|statsReliable
parameter_list|)
block|{
name|this
operator|.
name|statsReliable
operator|=
name|statsReliable
expr_stmt|;
block|}
specifier|public
name|void
name|setRowLimit
parameter_list|(
name|int
name|rowLimit
parameter_list|)
block|{
name|this
operator|.
name|rowLimit
operator|=
name|rowLimit
expr_stmt|;
block|}
specifier|public
name|int
name|getRowLimit
parameter_list|()
block|{
return|return
name|rowLimit
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Row Limit Per Split"
argument_list|)
specifier|public
name|Integer
name|getRowLimitExplain
parameter_list|()
block|{
return|return
name|rowLimit
operator|>=
literal|0
condition|?
name|rowLimit
else|:
literal|null
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|getBucketFileNameMapping
parameter_list|()
block|{
return|return
name|bucketFileNameMapping
return|;
block|}
specifier|public
name|void
name|setBucketFileNameMapping
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|bucketFileNameMapping
parameter_list|)
block|{
name|this
operator|.
name|bucketFileNameMapping
operator|=
name|bucketFileNameMapping
expr_stmt|;
block|}
specifier|public
name|void
name|setIsMetadataOnly
parameter_list|(
name|boolean
name|metadata_only
parameter_list|)
block|{
name|isMetadataOnly
operator|=
name|metadata_only
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIsMetadataOnly
parameter_list|()
block|{
return|return
name|isMetadataOnly
return|;
block|}
specifier|public
name|Table
name|getTableMetadata
parameter_list|()
block|{
return|return
name|tableMetadata
return|;
block|}
specifier|public
name|void
name|setTableMetadata
parameter_list|(
name|Table
name|tableMetadata
parameter_list|)
block|{
name|this
operator|.
name|tableMetadata
operator|=
name|tableMetadata
expr_stmt|;
block|}
specifier|public
name|TableSample
name|getTableSample
parameter_list|()
block|{
return|return
name|tableSample
return|;
block|}
specifier|public
name|void
name|setTableSample
parameter_list|(
name|TableSample
name|tableSample
parameter_list|)
block|{
name|this
operator|.
name|tableSample
operator|=
name|tableSample
expr_stmt|;
block|}
specifier|public
name|String
name|getSerializedFilterExpr
parameter_list|()
block|{
return|return
name|serializedFilterExpr
return|;
block|}
specifier|public
name|void
name|setSerializedFilterExpr
parameter_list|(
name|String
name|serializedFilterExpr
parameter_list|)
block|{
name|this
operator|.
name|serializedFilterExpr
operator|=
name|serializedFilterExpr
expr_stmt|;
block|}
specifier|public
name|String
name|getSerializedFilterObject
parameter_list|()
block|{
return|return
name|serializedFilterObject
return|;
block|}
specifier|public
name|void
name|setSerializedFilterObject
parameter_list|(
name|String
name|serializedFilterObject
parameter_list|)
block|{
name|this
operator|.
name|serializedFilterObject
operator|=
name|serializedFilterObject
expr_stmt|;
block|}
specifier|public
name|void
name|setIncludedBuckets
parameter_list|(
name|BitSet
name|bitset
parameter_list|)
block|{
name|this
operator|.
name|includedBuckets
operator|=
name|bitset
expr_stmt|;
block|}
specifier|public
name|BitSet
name|getIncludedBuckets
parameter_list|()
block|{
return|return
name|this
operator|.
name|includedBuckets
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"buckets included"
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
name|getIncludedBucketExplain
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|includedBuckets
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|this
operator|.
name|includedBuckets
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|this
operator|.
name|includedBuckets
operator|.
name|get
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"] of %d"
argument_list|,
name|numBuckets
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|int
name|getNumBuckets
parameter_list|()
block|{
return|return
name|numBuckets
return|;
block|}
specifier|public
name|void
name|setNumBuckets
parameter_list|(
name|int
name|numBuckets
parameter_list|)
block|{
name|this
operator|.
name|numBuckets
operator|=
name|numBuckets
expr_stmt|;
block|}
specifier|public
name|boolean
name|isNeedSkipHeaderFooters
parameter_list|()
block|{
name|boolean
name|rtn
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|tableMetadata
operator|!=
literal|null
operator|&&
name|tableMetadata
operator|.
name|getTTable
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
name|tableMetadata
operator|.
name|getTTable
argument_list|()
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|params
operator|!=
literal|null
condition|)
block|{
name|String
name|skipHVal
init|=
name|params
operator|.
name|get
argument_list|(
name|serdeConstants
operator|.
name|HEADER_COUNT
argument_list|)
decl_stmt|;
name|int
name|hcount
init|=
name|skipHVal
operator|==
literal|null
condition|?
literal|0
else|:
name|Integer
operator|.
name|parseInt
argument_list|(
name|skipHVal
argument_list|)
decl_stmt|;
name|String
name|skipFVal
init|=
name|params
operator|.
name|get
argument_list|(
name|serdeConstants
operator|.
name|FOOTER_COUNT
argument_list|)
decl_stmt|;
name|int
name|fcount
init|=
name|skipFVal
operator|==
literal|null
condition|?
literal|0
else|:
name|Integer
operator|.
name|parseInt
argument_list|(
name|skipFVal
argument_list|)
decl_stmt|;
name|rtn
operator|=
operator|(
name|hcount
operator|!=
literal|0
operator|||
name|fcount
operator|!=
literal|0
operator|)
expr_stmt|;
block|}
block|}
return|return
name|rtn
return|;
block|}
annotation|@
name|Override
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"properties"
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
name|USER
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getOpProps
parameter_list|()
block|{
return|return
name|opProps
return|;
block|}
specifier|public
class|class
name|TableScanOperatorExplainVectorization
extends|extends
name|OperatorExplainVectorization
block|{
specifier|private
specifier|final
name|TableScanDesc
name|tableScanDesc
decl_stmt|;
specifier|private
specifier|final
name|VectorTableScanDesc
name|vectorTableScanDesc
decl_stmt|;
specifier|public
name|TableScanOperatorExplainVectorization
parameter_list|(
name|TableScanDesc
name|tableScanDesc
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
name|tableScanDesc
operator|=
name|tableScanDesc
expr_stmt|;
name|vectorTableScanDesc
operator|=
operator|(
name|VectorTableScanDesc
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
literal|"projectedOutputColumns"
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
name|getProjectedOutputColumns
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|toString
argument_list|(
name|vectorTableScanDesc
operator|.
name|getProjectedOutputColumns
argument_list|()
argument_list|)
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
literal|"TableScan Vectorization"
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
name|TableScanOperatorExplainVectorization
name|getTableScanVectorization
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
name|TableScanOperatorExplainVectorization
argument_list|(
name|this
argument_list|,
name|vectorDesc
argument_list|)
return|;
block|}
block|}
end_class

end_unit

