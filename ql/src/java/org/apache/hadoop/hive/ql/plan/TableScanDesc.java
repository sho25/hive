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
name|exec
operator|.
name|PTFUtils
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
static|static
block|{
name|PTFUtils
operator|.
name|makeTransient
argument_list|(
name|TableScanDesc
operator|.
name|class
argument_list|,
literal|"filterObject"
argument_list|,
literal|"referencedColumns"
argument_list|,
literal|"tableMetadata"
argument_list|)
expr_stmt|;
block|}
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
name|int
name|maxStatsKeyPrefixLength
init|=
operator|-
literal|1
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
specifier|transient
name|TableSample
name|tableSample
decl_stmt|;
specifier|private
specifier|transient
specifier|final
name|Table
name|tableMetadata
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
name|displayName
operator|=
literal|"filterExpr"
argument_list|)
specifier|public
name|String
name|getFilterExprString
parameter_list|()
block|{
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|PlanUtils
operator|.
name|addExprToStringBuffer
argument_list|(
name|filterExpr
argument_list|,
name|sb
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
name|int
name|getMaxStatsKeyPrefixLength
parameter_list|()
block|{
return|return
name|maxStatsKeyPrefixLength
return|;
block|}
specifier|public
name|void
name|setMaxStatsKeyPrefixLength
parameter_list|(
name|int
name|maxStatsKeyPrefixLength
parameter_list|)
block|{
name|this
operator|.
name|maxStatsKeyPrefixLength
operator|=
name|maxStatsKeyPrefixLength
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
block|}
end_class

end_unit

