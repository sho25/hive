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
name|index
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
name|Collection
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
name|LinkedHashMap
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|metastore
operator|.
name|api
operator|.
name|Index
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
name|metastore
operator|.
name|api
operator|.
name|StorageDescriptor
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
name|Operator
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
name|TableScanOperator
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
name|Utilities
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
name|index
operator|.
name|AggregateIndexHandler
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
name|Hive
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
name|optimizer
operator|.
name|IndexUtils
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
name|Transform
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
name|ParseContext
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
name|SemanticException
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
name|OperatorDesc
import|;
end_import

begin_comment
comment|/**  * RewriteGBUsingIndex is implemented as one of the Rule-based Optimizations.  * Implements optimizations for GroupBy clause rewrite using aggregate index.  * This optimization rewrites GroupBy query over base table to the query over simple table-scan  * over index table, if there is index on the group by key(s) or the distinct column(s).  * E.g.  *<code>  *   select count(key)  *   from table  *   group by key;  *</code>  *  to  *<code>  *   select sum(_count_of_key)  *   from idx_table  *   group by key;  *</code>  *  *  The rewrite supports following queries:  *<ul>  *<li> Queries having only those col refs that are in the index key.  *<li> Queries that have index key col refs  *<ul>  *<li> in SELECT  *<li> in WHERE  *<li> in GROUP BY  *</ul>  *<li> Queries with agg func COUNT(index key col ref) in SELECT  *<li> Queries with SELECT DISTINCT index_key_col_refs  *<li> Queries having a subquery satisfying above condition (only the subquery is rewritten)  *</ul>  *  *  @see AggregateIndexHandler  *  @see IndexUtils  *  @see RewriteCanApplyCtx  *  @see RewriteCanApplyProcFactory  *  @see RewriteParseContextGenerator  *  @see RewriteQueryUsingAggregateIndexCtx  *  @see RewriteQueryUsingAggregateIndex  *  For test cases, @see ql_rewrite_gbtoidx.q  */
end_comment

begin_class
specifier|public
class|class
name|RewriteGBUsingIndex
implements|implements
name|Transform
block|{
specifier|private
name|ParseContext
name|parseContext
decl_stmt|;
specifier|private
name|Hive
name|hiveDb
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RewriteGBUsingIndex
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/*    * Stores the list of top TableScanOperator names for which the rewrite    * can be applied and the action that needs to be performed for operator tree    * starting from this TableScanOperator    */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|RewriteCanApplyCtx
argument_list|>
name|tsOpToProcess
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|RewriteCanApplyCtx
argument_list|>
argument_list|()
decl_stmt|;
comment|//Index Validation Variables
specifier|private
specifier|static
specifier|final
name|String
name|IDX_BUCKET_COL
init|=
literal|"_bucketname"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|IDX_OFFSETS_ARRAY_COL
init|=
literal|"_offsets"
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ParseContext
name|transform
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|parseContext
operator|=
name|pctx
expr_stmt|;
name|hiveConf
operator|=
name|parseContext
operator|.
name|getConf
argument_list|()
expr_stmt|;
try|try
block|{
name|hiveDb
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// Don't try to index optimize the query to build the index
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTINDEXFILTER
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|/* Check if the input query passes all the tests to be eligible for a rewrite      * If yes, rewrite original query; else, return the current parseContext      */
if|if
condition|(
name|shouldApplyOptimization
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Rewriting Original Query using "
operator|+
name|getName
argument_list|()
operator|+
literal|" optimization."
argument_list|)
expr_stmt|;
name|rewriteOriginalQuery
argument_list|()
expr_stmt|;
block|}
return|return
name|parseContext
return|;
block|}
specifier|private
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"RewriteGBUsingIndex"
return|;
block|}
comment|/**    * We traverse the current operator tree to check for conditions in which the    * optimization cannot be applied.    *    * At the end, we check if all conditions have passed for rewrite. If yes, we    * determine if the the index is usable for rewrite. Else, we log the condition which    * did not meet the rewrite criterion.    *    * @return    * @throws SemanticException    */
name|boolean
name|shouldApplyOptimization
parameter_list|()
throws|throws
name|SemanticException
block|{
name|Map
argument_list|<
name|Table
argument_list|,
name|List
argument_list|<
name|Index
argument_list|>
argument_list|>
name|tableToIndex
init|=
name|getIndexesForRewrite
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableToIndex
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No Valid Index Found to apply Rewrite, "
operator|+
literal|"skipping "
operator|+
name|getName
argument_list|()
operator|+
literal|" optimization"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|/*      * This code iterates over each TableScanOperator from the topOps map from ParseContext.      * For each operator tree originating from this top TableScanOperator, we determine      * if the optimization can be applied. If yes, we add the name of the top table to      * the tsOpToProcess to apply rewrite later on.      * */
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|entry
range|:
name|parseContext
operator|.
name|getTopOps
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|alias
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|TableScanOperator
name|topOp
init|=
operator|(
name|TableScanOperator
operator|)
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Table
name|table
init|=
name|topOp
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Index
argument_list|>
name|indexes
init|=
name|tableToIndex
operator|.
name|get
argument_list|(
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
comment|//if base table has partitions, we need to check if index is built for
comment|//all partitions. If not, then we do not apply the optimization
if|if
condition|(
operator|!
name|checkIfIndexBuiltOnAllTablePartitions
argument_list|(
name|topOp
argument_list|,
name|indexes
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Index is not built for all table partitions, "
operator|+
literal|"skipping "
operator|+
name|getName
argument_list|()
operator|+
literal|" optimization"
argument_list|)
expr_stmt|;
continue|continue;
block|}
block|}
comment|//check if rewrite can be applied for operator tree
comment|//if there are no partitions on base table
name|checkIfRewriteCanBeApplied
argument_list|(
name|alias
argument_list|,
name|topOp
argument_list|,
name|table
argument_list|,
name|indexes
argument_list|)
expr_stmt|;
block|}
return|return
operator|!
name|tsOpToProcess
operator|.
name|isEmpty
argument_list|()
return|;
block|}
comment|/**    * This methods checks if rewrite can be applied using the index and also    * verifies all conditions of the operator tree.    *    * @param topOp - TableScanOperator for a single the operator tree branch    * @param indexes - Map of a table and list of indexes on it    * @return - true if rewrite can be applied on the current branch; false otherwise    * @throws SemanticException    */
specifier|private
name|boolean
name|checkIfRewriteCanBeApplied
parameter_list|(
name|String
name|alias
parameter_list|,
name|TableScanOperator
name|topOp
parameter_list|,
name|Table
name|baseTable
parameter_list|,
name|List
argument_list|<
name|Index
argument_list|>
name|indexes
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|//Context for checking if this optimization can be applied to the input query
name|RewriteCanApplyCtx
name|canApplyCtx
init|=
name|RewriteCanApplyCtx
operator|.
name|getInstance
argument_list|(
name|parseContext
argument_list|)
decl_stmt|;
name|canApplyCtx
operator|.
name|setAlias
argument_list|(
name|alias
argument_list|)
expr_stmt|;
name|canApplyCtx
operator|.
name|setBaseTableName
argument_list|(
name|baseTable
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|canApplyCtx
operator|.
name|populateRewriteVars
argument_list|(
name|topOp
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Index
argument_list|,
name|String
argument_list|>
name|indexTableMap
init|=
name|getIndexToKeysMap
argument_list|(
name|indexes
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Index
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|indexTableMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|//we rewrite the original query using the first valid index encountered
comment|//this can be changed if we have a better mechanism to
comment|//decide which index will produce a better rewrite
name|Index
name|index
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|indexKeyName
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|//break here if any valid index is found to apply rewrite
if|if
condition|(
name|canApplyCtx
operator|.
name|getIndexKey
argument_list|()
operator|!=
literal|null
operator|&&
name|canApplyCtx
operator|.
name|getIndexKey
argument_list|()
operator|.
name|equals
argument_list|(
name|indexKeyName
argument_list|)
operator|&&
name|checkIfAllRewriteCriteriaIsMet
argument_list|(
name|canApplyCtx
argument_list|)
condition|)
block|{
name|canApplyCtx
operator|.
name|setAggFunction
argument_list|(
literal|"_count_of_"
operator|+
name|indexKeyName
operator|+
literal|""
argument_list|)
expr_stmt|;
name|canApplyCtx
operator|.
name|addTable
argument_list|(
name|canApplyCtx
operator|.
name|getBaseTableName
argument_list|()
argument_list|,
name|index
operator|.
name|getIndexTableName
argument_list|()
argument_list|)
expr_stmt|;
name|canApplyCtx
operator|.
name|setIndexTableName
argument_list|(
name|index
operator|.
name|getIndexTableName
argument_list|()
argument_list|)
expr_stmt|;
name|tsOpToProcess
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|canApplyCtx
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Get a list of indexes which can be used for rewrite.    * @return    * @throws SemanticException    */
specifier|private
name|Map
argument_list|<
name|Table
argument_list|,
name|List
argument_list|<
name|Index
argument_list|>
argument_list|>
name|getIndexesForRewrite
parameter_list|()
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|supportedIndexes
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|supportedIndexes
operator|.
name|add
argument_list|(
name|AggregateIndexHandler
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// query the metastore to know what columns we have indexed
name|Collection
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|topTables
init|=
name|parseContext
operator|.
name|getTopOps
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Table
argument_list|,
name|List
argument_list|<
name|Index
argument_list|>
argument_list|>
name|indexes
init|=
operator|new
name|HashMap
argument_list|<
name|Table
argument_list|,
name|List
argument_list|<
name|Index
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|topTables
control|)
block|{
if|if
condition|(
name|op
operator|instanceof
name|TableScanOperator
condition|)
block|{
name|TableScanOperator
name|tsOP
init|=
operator|(
name|TableScanOperator
operator|)
name|op
decl_stmt|;
name|List
argument_list|<
name|Index
argument_list|>
name|tblIndexes
init|=
name|IndexUtils
operator|.
name|getIndexes
argument_list|(
name|tsOP
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
argument_list|,
name|supportedIndexes
argument_list|)
decl_stmt|;
if|if
condition|(
name|tblIndexes
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|indexes
operator|.
name|put
argument_list|(
name|tsOP
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
argument_list|,
name|tblIndexes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|indexes
return|;
block|}
comment|/**    * This method checks if the index is built on all partitions of the base    * table. If not, then the method returns false as we do not apply optimization    * for this case.    * @param tableScan    * @param indexes    * @return    * @throws SemanticException    */
specifier|private
name|boolean
name|checkIfIndexBuiltOnAllTablePartitions
parameter_list|(
name|TableScanOperator
name|tableScan
parameter_list|,
name|List
argument_list|<
name|Index
argument_list|>
name|indexes
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// check if we have indexes on all partitions in this table scan
name|Set
argument_list|<
name|Partition
argument_list|>
name|queryPartitions
decl_stmt|;
try|try
block|{
name|queryPartitions
operator|=
name|IndexUtils
operator|.
name|checkPartitionsCoveredByIndex
argument_list|(
name|tableScan
argument_list|,
name|parseContext
argument_list|,
name|indexes
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryPartitions
operator|==
literal|null
condition|)
block|{
comment|// partitions not covered
return|return
literal|false
return|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Fatal Error: problem accessing metastore"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|queryPartitions
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * This code block iterates over indexes on the table and populates the indexToKeys map    * for all the indexes that satisfy the rewrite criteria.    * @param indexTables    * @return    * @throws SemanticException    */
name|Map
argument_list|<
name|Index
argument_list|,
name|String
argument_list|>
name|getIndexToKeysMap
parameter_list|(
name|List
argument_list|<
name|Index
argument_list|>
name|indexTables
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Hive
name|hiveInstance
init|=
name|hiveDb
decl_stmt|;
name|Map
argument_list|<
name|Index
argument_list|,
name|String
argument_list|>
name|indexToKeysMap
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Index
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|idxCtr
init|=
literal|0
init|;
name|idxCtr
operator|<
name|indexTables
operator|.
name|size
argument_list|()
condition|;
name|idxCtr
operator|++
control|)
block|{
name|Index
name|index
init|=
name|indexTables
operator|.
name|get
argument_list|(
name|idxCtr
argument_list|)
decl_stmt|;
comment|//Getting index key columns
name|StorageDescriptor
name|sd
init|=
name|index
operator|.
name|getSd
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|idxColList
init|=
name|sd
operator|.
name|getCols
argument_list|()
decl_stmt|;
assert|assert
name|idxColList
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|String
name|indexKeyName
init|=
name|idxColList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// Check that the index schema is as expected. This code block should
comment|// catch problems of this rewrite breaking when the AggregateIndexHandler
comment|// index is changed.
name|List
argument_list|<
name|String
argument_list|>
name|idxTblColNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|String
index|[]
name|qualified
init|=
name|Utilities
operator|.
name|getDbTableName
argument_list|(
name|index
operator|.
name|getDbName
argument_list|()
argument_list|,
name|index
operator|.
name|getIndexTableName
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|idxTbl
init|=
name|hiveInstance
operator|.
name|getTable
argument_list|(
name|qualified
index|[
literal|0
index|]
argument_list|,
name|qualified
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
for|for
control|(
name|FieldSchema
name|idxTblCol
range|:
name|idxTbl
operator|.
name|getCols
argument_list|()
control|)
block|{
name|idxTblColNames
operator|.
name|add
argument_list|(
name|idxTblCol
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Got exception while locating index table, "
operator|+
literal|"skipping "
operator|+
name|getName
argument_list|()
operator|+
literal|" optimization"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
assert|assert
operator|(
name|idxTblColNames
operator|.
name|contains
argument_list|(
name|IDX_BUCKET_COL
argument_list|)
operator|)
assert|;
assert|assert
operator|(
name|idxTblColNames
operator|.
name|contains
argument_list|(
name|IDX_OFFSETS_ARRAY_COL
argument_list|)
operator|)
assert|;
comment|// we add all index tables which can be used for rewrite
comment|// and defer the decision of using a particular index for later
comment|// this is to allow choosing a index if a better mechanism is
comment|// designed later to chose a better rewrite
name|indexToKeysMap
operator|.
name|put
argument_list|(
name|index
argument_list|,
name|indexKeyName
argument_list|)
expr_stmt|;
block|}
return|return
name|indexToKeysMap
return|;
block|}
comment|/**    * Method to rewrite the input query if all optimization criteria is passed.    * The method iterates over the tsOpToProcess {@link ArrayList} to apply the rewrites    * @throws SemanticException    *    */
specifier|private
name|void
name|rewriteOriginalQuery
parameter_list|()
throws|throws
name|SemanticException
block|{
for|for
control|(
name|RewriteCanApplyCtx
name|canApplyCtx
range|:
name|tsOpToProcess
operator|.
name|values
argument_list|()
control|)
block|{
name|RewriteQueryUsingAggregateIndexCtx
name|rewriteQueryCtx
init|=
name|RewriteQueryUsingAggregateIndexCtx
operator|.
name|getInstance
argument_list|(
name|parseContext
argument_list|,
name|hiveDb
argument_list|,
name|canApplyCtx
argument_list|)
decl_stmt|;
name|rewriteQueryCtx
operator|.
name|invokeRewriteQueryProc
argument_list|()
expr_stmt|;
name|parseContext
operator|=
name|rewriteQueryCtx
operator|.
name|getParseContext
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished Rewriting query"
argument_list|)
expr_stmt|;
block|}
comment|/**    * This method logs the reason for which we cannot apply the rewrite optimization.    * @return    */
name|boolean
name|checkIfAllRewriteCriteriaIsMet
parameter_list|(
name|RewriteCanApplyCtx
name|canApplyCtx
parameter_list|)
block|{
if|if
condition|(
name|canApplyCtx
operator|.
name|isSelClauseColsFetchException
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Got exception while locating child col refs for select list, "
operator|+
literal|"skipping "
operator|+
name|getName
argument_list|()
operator|+
literal|" optimization."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|canApplyCtx
operator|.
name|isAggFuncIsNotCount
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Agg func other than count is "
operator|+
literal|"not supported by "
operator|+
name|getName
argument_list|()
operator|+
literal|" optimization."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|canApplyCtx
operator|.
name|isAggParameterException
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Got exception while locating parameter refs for aggregation, "
operator|+
literal|"skipping "
operator|+
name|getName
argument_list|()
operator|+
literal|" optimization."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

