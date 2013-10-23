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
specifier|private
specifier|transient
name|Table
name|table
decl_stmt|;
specifier|private
specifier|transient
name|ExprNodeDesc
name|pruningPredicate
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
literal|"pruningPredicate"
argument_list|)
expr_stmt|;
name|PTFUtils
operator|.
name|makeTransient
argument_list|(
name|TableScanDesc
operator|.
name|class
argument_list|,
literal|"table"
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
parameter_list|()
block|{   }
specifier|public
name|Table
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
specifier|public
name|void
name|setTable
parameter_list|(
name|Table
name|t
parameter_list|)
block|{
name|table
operator|=
name|t
expr_stmt|;
block|}
specifier|public
name|ExprNodeDesc
name|getPruningPredicate
parameter_list|()
block|{
return|return
name|pruningPredicate
return|;
block|}
specifier|public
name|void
name|setPruningPredicate
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|)
block|{
name|pruningPredicate
operator|=
name|expr
expr_stmt|;
block|}
specifier|public
name|TableScanDesc
parameter_list|(
specifier|final
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
name|displayName
operator|=
literal|"filterExpr"
argument_list|)
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
name|normalExplain
operator|=
literal|false
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
name|normalExplain
operator|=
literal|false
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
block|}
end_class

end_unit

