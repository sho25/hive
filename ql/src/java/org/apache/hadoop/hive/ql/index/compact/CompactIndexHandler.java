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
name|compact
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
name|IndexMetadataChangeTask
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
name|IndexMetadataChangeWork
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
name|metadata
operator|.
name|HiveStoragePredicateHandler
operator|.
name|DecomposedPredicate
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

begin_class
specifier|public
class|class
name|CompactIndexHandler
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CompactIndexHandler
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
literal|"_offsets"
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
name|offSets
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
parameter_list|)
block|{
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
name|command
operator|.
name|append
argument_list|(
literal|"INSERT OVERWRITE TABLE "
operator|+
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|indexTableName
argument_list|)
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
literal|" collect_set ("
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
literal|") "
argument_list|)
expr_stmt|;
name|command
operator|.
name|append
argument_list|(
literal|" FROM "
operator|+
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|baseTableName
argument_list|)
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
name|indexCols
operator|+
literal|", "
operator|+
name|VirtualColumn
operator|.
name|FILENAME
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Driver
name|driver
init|=
operator|new
name|Driver
argument_list|(
operator|new
name|HiveConf
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|CompactIndexHandler
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|driver
operator|.
name|compile
argument_list|(
name|command
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|rootTask
init|=
name|driver
operator|.
name|getPlan
argument_list|()
operator|.
name|getRootTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|inputs
operator|.
name|addAll
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
name|outputs
operator|.
name|addAll
argument_list|(
name|driver
operator|.
name|getPlan
argument_list|()
operator|.
name|getOutputs
argument_list|()
argument_list|)
expr_stmt|;
name|IndexMetadataChangeWork
name|indexMetaChange
init|=
operator|new
name|IndexMetadataChangeWork
argument_list|(
name|partSpec
argument_list|,
name|indexTableName
argument_list|,
name|dbName
argument_list|)
decl_stmt|;
name|IndexMetadataChangeTask
name|indexMetaChangeTsk
init|=
operator|new
name|IndexMetadataChangeTask
argument_list|()
decl_stmt|;
name|indexMetaChangeTsk
operator|.
name|setWork
argument_list|(
name|indexMetaChange
argument_list|)
expr_stmt|;
name|rootTask
operator|.
name|addDependentTask
argument_list|(
name|indexMetaChangeTsk
argument_list|)
expr_stmt|;
return|return
name|rootTask
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|generateIndexQuery
parameter_list|(
name|Index
name|index
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
name|DecomposedPredicate
name|decomposedPredicate
init|=
name|decomposePredicate
argument_list|(
name|predicate
argument_list|,
name|index
argument_list|,
name|queryContext
operator|.
name|getQueryPartitions
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|decomposedPredicate
operator|==
literal|null
condition|)
block|{
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
comment|// pass residual predicate back out for further processing
name|queryContext
operator|.
name|setResidualPredicate
argument_list|(
name|decomposedPredicate
operator|.
name|residualPredicate
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
name|getMRTmpFileURI
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
literal|"SELECT `_bucketname` ,  `_offsets` FROM "
argument_list|)
expr_stmt|;
name|qlCommand
operator|.
name|append
argument_list|(
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|index
operator|.
name|getIndexTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|qlCommand
operator|.
name|append
argument_list|(
literal|" WHERE "
argument_list|)
expr_stmt|;
name|String
name|predicateString
init|=
name|decomposedPredicate
operator|.
name|pushedPredicate
operator|.
name|getExprString
argument_list|()
decl_stmt|;
name|qlCommand
operator|.
name|append
argument_list|(
name|predicateString
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
name|Driver
name|driver
init|=
operator|new
name|Driver
argument_list|(
name|pctx
operator|.
name|getConf
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
comment|// setup TableScanOperator to change input format for original query
name|queryContext
operator|.
name|setIndexInputFormat
argument_list|(
name|HiveCompactIndexInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
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
return|return;
block|}
comment|/**    * Split the predicate into the piece we can deal with (pushed), and the one we can't (residual)    * @param predicate    * @param index    * @return    */
specifier|private
name|DecomposedPredicate
name|decomposePredicate
parameter_list|(
name|ExprNodeDesc
name|predicate
parameter_list|,
name|Index
name|index
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
name|getIndexPredicateAnalyzer
argument_list|(
name|index
argument_list|,
name|queryPartitions
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
comment|// split predicate into pushed (what we can handle), and residual (what we can't handle)
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
name|DecomposedPredicate
name|decomposedPredicate
init|=
operator|new
name|DecomposedPredicate
argument_list|()
decl_stmt|;
name|decomposedPredicate
operator|.
name|pushedPredicate
operator|=
name|analyzer
operator|.
name|translateSearchConditions
argument_list|(
name|searchConditions
argument_list|)
expr_stmt|;
name|decomposedPredicate
operator|.
name|residualPredicate
operator|=
name|residualPredicate
expr_stmt|;
return|return
name|decomposedPredicate
return|;
block|}
comment|/**    * Instantiate a new predicate analyzer suitable for determining    * whether we can use an index, based on rules for indexes in    * WHERE clauses that we support    *    * @return preconfigured predicate analyzer for WHERE queries    */
specifier|private
name|IndexPredicateAnalyzer
name|getIndexPredicateAnalyzer
parameter_list|(
name|Index
name|index
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
comment|// only return results for columns in this index
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
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partitionColumns
init|=
name|part
operator|.
name|getCols
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldSchema
name|column
range|:
name|partitionColumns
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
return|return
name|analyzer
return|;
block|}
annotation|@
name|Override
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
name|long
name|minSize
init|=
name|hiveConf
operator|.
name|getLongVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTINDEXFILTER_COMPACT_MINSIZE
argument_list|)
decl_stmt|;
name|long
name|maxSize
init|=
name|hiveConf
operator|.
name|getLongVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTINDEXFILTER_COMPACT_MAXSIZE
argument_list|)
decl_stmt|;
if|if
condition|(
name|maxSize
operator|<
literal|0
condition|)
block|{
name|maxSize
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
return|return
operator|(
name|querySize
operator|>
name|minSize
operator|&
name|querySize
operator|<
name|maxSize
operator|)
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

