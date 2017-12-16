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
name|index
operator|.
name|bitmap
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|conf
operator|.
name|Configuration
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
name|metastore
operator|.
name|api
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
name|Driver
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
name|Task
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|HiveIndexQueryContext
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
name|HiveIndexedInputFormat
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
name|IndexPredicateAnalyzer
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
name|IndexSearchCondition
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
name|TableBasedIndexHandler
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
name|HiveUtils
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
name|PartitionDesc
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
name|session
operator|.
name|LineageState
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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPEqual
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPEqualOrGreaterThan
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPEqualOrLessThan
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPGreaterThan
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPLessThan
import|;
end_import

begin_comment
comment|/**  * Index handler for the bitmap index. Bitmap index uses an EWAH-compressed  * bitmap to represent the values in a table.  */
end_comment

begin_class
specifier|public
class|class
name|BitmapIndexHandler
extends|extends
name|TableBasedIndexHandler
block|{
specifier|private
name|Configuration
name|configuration
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|BitmapIndexHandler
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|generateIndexQuery
parameter_list|(
name|List
argument_list|<
name|Index
argument_list|>
name|indexes
parameter_list|,
name|ExprNodeDesc
name|predicate
parameter_list|,
name|ParseContext
name|pctx
parameter_list|,
name|HiveIndexQueryContext
name|queryContext
parameter_list|)
block|{
name|Map
argument_list|<
name|Index
argument_list|,
name|ExprNodeDesc
argument_list|>
name|indexPredicates
init|=
name|decomposePredicate
argument_list|(
name|predicate
argument_list|,
name|indexes
argument_list|,
name|queryContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexPredicates
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No decomposed predicate found"
argument_list|)
expr_stmt|;
name|queryContext
operator|.
name|setQueryTasks
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return;
comment|// abort if we couldn't pull out anything from the predicate
block|}
name|List
argument_list|<
name|BitmapInnerQuery
argument_list|>
name|iqs
init|=
operator|new
name|ArrayList
argument_list|<
name|BitmapInnerQuery
argument_list|>
argument_list|(
name|indexes
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Index
name|index
range|:
name|indexes
control|)
block|{
name|ExprNodeDesc
name|indexPredicate
init|=
name|indexPredicates
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexPredicate
operator|!=
literal|null
condition|)
block|{
name|iqs
operator|.
name|add
argument_list|(
operator|new
name|BitmapInnerQuery
argument_list|(
name|index
operator|.
name|getIndexTableName
argument_list|()
argument_list|,
name|indexPredicate
argument_list|,
literal|"ind"
operator|+
name|i
operator|++
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// setup TableScanOperator to change input format for original query
name|queryContext
operator|.
name|setIndexInputFormat
argument_list|(
name|HiveIndexedInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Build reentrant QL for index query
name|StringBuilder
name|qlCommand
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"INSERT OVERWRITE DIRECTORY "
argument_list|)
decl_stmt|;
name|String
name|tmpFile
init|=
name|pctx
operator|.
name|getContext
argument_list|()
operator|.
name|getMRTmpPath
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|qlCommand
operator|.
name|append
argument_list|(
literal|"\""
operator|+
name|tmpFile
operator|+
literal|"\" "
argument_list|)
expr_stmt|;
comment|// QL includes " around file name
name|qlCommand
operator|.
name|append
argument_list|(
literal|"SELECT bucketname AS `_bucketname` , COLLECT_SET(offset) AS `_offsets` FROM "
argument_list|)
expr_stmt|;
name|qlCommand
operator|.
name|append
argument_list|(
literal|"(SELECT `_bucketname` AS bucketname , `_offset` AS offset FROM "
argument_list|)
expr_stmt|;
name|BitmapQuery
name|head
init|=
name|iqs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|i
operator|=
literal|1
init|;
name|i
operator|<
name|iqs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|head
operator|=
operator|new
name|BitmapOuterQuery
argument_list|(
literal|"oind"
operator|+
name|i
argument_list|,
name|head
argument_list|,
name|iqs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|qlCommand
operator|.
name|append
argument_list|(
name|head
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|qlCommand
operator|.
name|append
argument_list|(
literal|" WHERE NOT EWAH_BITMAP_EMPTY("
operator|+
name|head
operator|.
name|getAlias
argument_list|()
operator|+
literal|".`_bitmaps`) ) tmp_index GROUP BY bucketname"
argument_list|)
expr_stmt|;
comment|// generate tasks from index query string
name|LOG
operator|.
name|info
argument_list|(
literal|"Generating tasks for re-entrant QL query: "
operator|+
name|qlCommand
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|HiveConf
name|queryConf
init|=
operator|new
name|HiveConf
argument_list|(
name|pctx
operator|.
name|getConf
argument_list|()
argument_list|,
name|BitmapIndexHandler
operator|.
name|class
argument_list|)
decl_stmt|;
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|queryConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|COMPRESSRESULT
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Driver
name|driver
init|=
operator|new
name|Driver
argument_list|(
name|queryConf
argument_list|,
name|pctx
operator|.
name|getQueryState
argument_list|()
operator|.
name|getLineageState
argument_list|()
argument_list|)
decl_stmt|;
name|driver
operator|.
name|compile
argument_list|(
name|qlCommand
operator|.
name|toString
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|queryContext
operator|.
name|setIndexIntermediateFile
argument_list|(
name|tmpFile
argument_list|)
expr_stmt|;
name|queryContext
operator|.
name|addAdditionalSemanticInputs
argument_list|(
name|driver
operator|.
name|getPlan
argument_list|()
operator|.
name|getInputs
argument_list|()
argument_list|)
expr_stmt|;
name|queryContext
operator|.
name|setQueryTasks
argument_list|(
name|driver
operator|.
name|getPlan
argument_list|()
operator|.
name|getRootTasks
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Split the predicate into the piece we can deal with (pushed), and the one we can't (residual)    * @param predicate    * @param index    * @return    */
specifier|private
name|Map
argument_list|<
name|Index
argument_list|,
name|ExprNodeDesc
argument_list|>
name|decomposePredicate
parameter_list|(
name|ExprNodeDesc
name|predicate
parameter_list|,
name|List
argument_list|<
name|Index
argument_list|>
name|indexes
parameter_list|,
name|HiveIndexQueryContext
name|queryContext
parameter_list|)
block|{
name|Map
argument_list|<
name|Index
argument_list|,
name|ExprNodeDesc
argument_list|>
name|indexPredicates
init|=
operator|new
name|HashMap
argument_list|<
name|Index
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
comment|// compute overall residual
name|IndexPredicateAnalyzer
name|analyzer
init|=
name|getIndexPredicateAnalyzer
argument_list|(
name|indexes
argument_list|,
name|queryContext
operator|.
name|getQueryPartitions
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|IndexSearchCondition
argument_list|>
name|searchConditions
init|=
operator|new
name|ArrayList
argument_list|<
name|IndexSearchCondition
argument_list|>
argument_list|()
decl_stmt|;
name|ExprNodeDesc
name|residualPredicate
init|=
name|analyzer
operator|.
name|analyzePredicate
argument_list|(
name|predicate
argument_list|,
name|searchConditions
argument_list|)
decl_stmt|;
comment|// pass residual predicate back out for further processing
name|queryContext
operator|.
name|setResidualPredicate
argument_list|(
name|residualPredicate
argument_list|)
expr_stmt|;
if|if
condition|(
name|searchConditions
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|Index
name|index
range|:
name|indexes
control|)
block|{
name|ArrayList
argument_list|<
name|Index
argument_list|>
name|in
init|=
operator|new
name|ArrayList
argument_list|<
name|Index
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|in
operator|.
name|add
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|analyzer
operator|=
name|getIndexPredicateAnalyzer
argument_list|(
name|in
argument_list|,
name|queryContext
operator|.
name|getQueryPartitions
argument_list|()
argument_list|)
expr_stmt|;
name|searchConditions
operator|=
operator|new
name|ArrayList
argument_list|<
name|IndexSearchCondition
argument_list|>
argument_list|()
expr_stmt|;
comment|// split predicate into pushed (what we can handle), and residual (what we can't handle)
comment|// pushed predicate from translateSearchConditions is stored for the current index
comment|// This ensures that we apply all possible predicates to each index
name|analyzer
operator|.
name|analyzePredicate
argument_list|(
name|predicate
argument_list|,
name|searchConditions
argument_list|)
expr_stmt|;
if|if
condition|(
name|searchConditions
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|indexPredicates
operator|.
name|put
argument_list|(
name|index
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|indexPredicates
operator|.
name|put
argument_list|(
name|index
argument_list|,
name|analyzer
operator|.
name|translateSearchConditions
argument_list|(
name|searchConditions
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|indexPredicates
return|;
block|}
comment|/**    * Instantiate a new predicate analyzer suitable for determining    * whether we can use an index, based on rules for indexes in    * WHERE clauses that we support    *    * @return preconfigured predicate analyzer for WHERE queries    */
specifier|private
name|IndexPredicateAnalyzer
name|getIndexPredicateAnalyzer
parameter_list|(
name|List
argument_list|<
name|Index
argument_list|>
name|indexes
parameter_list|,
name|Set
argument_list|<
name|Partition
argument_list|>
name|queryPartitions
parameter_list|)
block|{
name|IndexPredicateAnalyzer
name|analyzer
init|=
operator|new
name|IndexPredicateAnalyzer
argument_list|()
decl_stmt|;
name|analyzer
operator|.
name|addComparisonOp
argument_list|(
name|GenericUDFOPEqual
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|analyzer
operator|.
name|addComparisonOp
argument_list|(
name|GenericUDFOPLessThan
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|analyzer
operator|.
name|addComparisonOp
argument_list|(
name|GenericUDFOPEqualOrLessThan
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|analyzer
operator|.
name|addComparisonOp
argument_list|(
name|GenericUDFOPGreaterThan
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|analyzer
operator|.
name|addComparisonOp
argument_list|(
name|GenericUDFOPEqualOrGreaterThan
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// only return results for columns in the list of indexes
for|for
control|(
name|Index
name|index
range|:
name|indexes
control|)
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|columnSchemas
init|=
name|index
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldSchema
name|column
range|:
name|columnSchemas
control|)
block|{
name|analyzer
operator|.
name|allowColumnName
argument_list|(
name|column
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// partitioned columns are treated as if they have indexes so that the partitions
comment|// are used during the index query generation
for|for
control|(
name|Partition
name|part
range|:
name|queryPartitions
control|)
block|{
if|if
condition|(
name|part
operator|.
name|getSpec
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
comment|// empty partitions are from whole tables, so we don't want to add them in
block|}
for|for
control|(
name|String
name|column
range|:
name|part
operator|.
name|getSpec
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|analyzer
operator|.
name|allowColumnName
argument_list|(
name|column
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|analyzer
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|analyzeIndexDefinition
parameter_list|(
name|Table
name|baseTable
parameter_list|,
name|Index
name|index
parameter_list|,
name|Table
name|indexTable
parameter_list|)
throws|throws
name|HiveException
block|{
name|StorageDescriptor
name|storageDesc
init|=
name|index
operator|.
name|getSd
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|usesIndexTable
argument_list|()
operator|&&
name|indexTable
operator|!=
literal|null
condition|)
block|{
name|StorageDescriptor
name|indexTableSd
init|=
name|storageDesc
operator|.
name|deepCopy
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|indexTblCols
init|=
name|indexTableSd
operator|.
name|getCols
argument_list|()
decl_stmt|;
name|FieldSchema
name|bucketFileName
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"_bucketname"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|indexTblCols
operator|.
name|add
argument_list|(
name|bucketFileName
argument_list|)
expr_stmt|;
name|FieldSchema
name|offSets
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"_offset"
argument_list|,
literal|"bigint"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|indexTblCols
operator|.
name|add
argument_list|(
name|offSets
argument_list|)
expr_stmt|;
name|FieldSchema
name|bitmaps
init|=
operator|new
name|FieldSchema
argument_list|(
literal|"_bitmaps"
argument_list|,
literal|"array<bigint>"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|indexTblCols
operator|.
name|add
argument_list|(
name|bitmaps
argument_list|)
expr_stmt|;
name|indexTable
operator|.
name|setSd
argument_list|(
name|indexTableSd
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|Task
argument_list|<
name|?
argument_list|>
name|getIndexBuilderMapRedTask
parameter_list|(
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|indexField
parameter_list|,
name|boolean
name|partitioned
parameter_list|,
name|PartitionDesc
name|indexTblPartDesc
parameter_list|,
name|String
name|indexTableName
parameter_list|,
name|PartitionDesc
name|baseTablePartDesc
parameter_list|,
name|String
name|baseTableName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|LineageState
name|lineageState
parameter_list|)
throws|throws
name|HiveException
block|{
name|HiveConf
name|builderConf
init|=
operator|new
name|HiveConf
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|BitmapIndexHandler
operator|.
name|class
argument_list|)
decl_stmt|;
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|builderConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEROWOFFSET
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|String
name|indexCols
init|=
name|HiveUtils
operator|.
name|getUnparsedColumnNamesFromFieldSchema
argument_list|(
name|indexField
argument_list|)
decl_stmt|;
comment|//form a new insert overwrite query.
name|StringBuilder
name|command
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|indexTblPartDesc
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
name|String
name|fullIndexTableName
init|=
name|StatsUtils
operator|.
name|getFullyQualifiedTableName
argument_list|(
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|dbName
argument_list|)
argument_list|,
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|indexTableName
argument_list|)
argument_list|)
decl_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|"INSERT OVERWRITE TABLE "
operator|+
name|fullIndexTableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|partitioned
operator|&&
name|indexTblPartDesc
operator|!=
literal|null
condition|)
block|{
name|command
operator|.
name|append
argument_list|(
literal|" PARTITION ( "
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|ret
init|=
name|getPartKVPairStringArray
argument_list|(
name|partSpec
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
name|ret
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|partKV
init|=
name|ret
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|command
operator|.
name|append
argument_list|(
name|partKV
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|<
name|ret
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
name|command
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
block|}
name|command
operator|.
name|append
argument_list|(
literal|" ) "
argument_list|)
expr_stmt|;
block|}
name|String
name|fullBaseTableName
init|=
name|StatsUtils
operator|.
name|getFullyQualifiedTableName
argument_list|(
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|dbName
argument_list|)
argument_list|,
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|baseTableName
argument_list|)
argument_list|)
decl_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|" SELECT "
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
name|indexCols
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
name|VirtualColumn
operator|.
name|FILENAME
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
name|VirtualColumn
operator|.
name|BLOCKOFFSET
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|"EWAH_BITMAP("
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
name|VirtualColumn
operator|.
name|ROWOFFSET
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|" FROM "
operator|+
name|fullBaseTableName
argument_list|)
expr_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|basePartSpec
init|=
name|baseTablePartDesc
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
if|if
condition|(
name|basePartSpec
operator|!=
literal|null
condition|)
block|{
name|command
operator|.
name|append
argument_list|(
literal|" WHERE "
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|pkv
init|=
name|getPartKVPairStringArray
argument_list|(
name|basePartSpec
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
name|pkv
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|partKV
init|=
name|pkv
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|command
operator|.
name|append
argument_list|(
name|partKV
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|<
name|pkv
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
name|command
operator|.
name|append
argument_list|(
literal|" AND "
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|command
operator|.
name|append
argument_list|(
literal|" GROUP BY "
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
name|VirtualColumn
operator|.
name|FILENAME
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
name|VirtualColumn
operator|.
name|BLOCKOFFSET
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|FieldSchema
name|fieldSchema
range|:
name|indexField
control|)
block|{
name|command
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|fieldSchema
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Require clusterby ROWOFFSET if map-size aggregation is off.
comment|// TODO: Make this work without map side aggregation
if|if
condition|(
operator|!
name|builderConf
operator|.
name|get
argument_list|(
literal|"hive.map.aggr"
argument_list|,
literal|null
argument_list|)
operator|.
name|equals
argument_list|(
literal|"true"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Cannot construct index without map-side aggregation"
argument_list|)
throw|;
block|}
name|Task
argument_list|<
name|?
argument_list|>
name|rootTask
init|=
name|IndexUtils
operator|.
name|createRootTask
argument_list|(
name|builderConf
argument_list|,
name|inputs
argument_list|,
name|outputs
argument_list|,
name|command
argument_list|,
name|partSpec
argument_list|,
name|indexTableName
argument_list|,
name|dbName
argument_list|,
name|lineageState
argument_list|)
decl_stmt|;
return|return
name|rootTask
return|;
block|}
annotation|@
name|Override
comment|/**    * No lower bound on bitmap index query size, so this will always return true    */
specifier|public
name|boolean
name|checkQuerySize
parameter_list|(
name|long
name|querySize
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|usesIndexTable
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

