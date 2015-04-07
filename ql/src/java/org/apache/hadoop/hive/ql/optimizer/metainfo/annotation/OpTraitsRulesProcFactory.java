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
name|metainfo
operator|.
name|annotation
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
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Stack
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
name|Order
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
name|GroupByOperator
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
name|JoinOperator
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
name|ReduceSinkOperator
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
name|SelectOperator
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
name|lib
operator|.
name|Node
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
name|lib
operator|.
name|NodeProcessor
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
name|lib
operator|.
name|NodeProcessorCtx
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
name|AbstractBucketJoinProc
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
name|ExprNodeColumnDesc
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
name|OpTraits
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
comment|/*  * This class populates the following operator traits for the entire operator tree:  * 1. Bucketing columns.  * 2. Table  * 3. Pruned partitions  *  * Bucketing columns refer to not to the bucketing columns from the table object but instead  * to the dynamic 'bucketing' done by operators such as reduce sinks and group-bys.  * All the operators have a translation from their input names to the output names corresponding  * to the bucketing column. The colExprMap that is a part of every operator is used in this  * transformation.  *  * The table object is used for the base-case in map-reduce when deciding to perform a bucket  * map join. This object is used in the BucketMapJoinProc to find if number of files for the  * table correspond to the number of buckets specified in the meta data.  *  * The pruned partition information has the same purpose as the table object at the moment.  *  * The traits of sorted-ness etc. can be populated as well for future optimizations to make use of.  */
end_comment

begin_class
specifier|public
class|class
name|OpTraitsRulesProcFactory
block|{
specifier|public
specifier|static
class|class
name|DefaultRule
implements|implements
name|NodeProcessor
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|)
name|nd
decl_stmt|;
name|op
operator|.
name|setOpTraits
argument_list|(
name|op
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getOpTraits
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/*    * Reduce sink operator is the de-facto operator    * for determining keyCols (emit keys of a map phase)    */
specifier|public
specifier|static
class|class
name|ReduceSinkRule
implements|implements
name|NodeProcessor
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ReduceSinkOperator
name|rs
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|nd
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|rs
operator|.
name|getColumnExprMap
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ExprNodeDesc
name|exprDesc
range|:
name|rs
operator|.
name|getConf
argument_list|()
operator|.
name|getKeyCols
argument_list|()
control|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|entry
range|:
name|rs
operator|.
name|getColumnExprMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|exprDesc
operator|.
name|isSame
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
name|bucketCols
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|listBucketCols
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|listBucketCols
operator|.
name|add
argument_list|(
name|bucketCols
argument_list|)
expr_stmt|;
name|int
name|numBuckets
init|=
operator|-
literal|1
decl_stmt|;
name|OpTraits
name|parentOpTraits
init|=
name|rs
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getConf
argument_list|()
operator|.
name|getTraits
argument_list|()
decl_stmt|;
if|if
condition|(
name|parentOpTraits
operator|!=
literal|null
condition|)
block|{
name|numBuckets
operator|=
name|parentOpTraits
operator|.
name|getNumBuckets
argument_list|()
expr_stmt|;
block|}
name|OpTraits
name|opTraits
init|=
operator|new
name|OpTraits
argument_list|(
name|listBucketCols
argument_list|,
name|numBuckets
argument_list|,
name|listBucketCols
argument_list|)
decl_stmt|;
name|rs
operator|.
name|setOpTraits
argument_list|(
name|opTraits
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/*    * Table scan has the table object and pruned partitions that has information    * such as bucketing, sorting, etc. that is used later for optimization.    */
specifier|public
specifier|static
class|class
name|TableScanRule
implements|implements
name|NodeProcessor
block|{
specifier|public
name|boolean
name|checkBucketedTable
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|ParseContext
name|pGraphContext
parameter_list|,
name|PrunedPartitionList
name|prunedParts
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|tbl
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
name|prunedParts
operator|.
name|getNotDeniedPartns
argument_list|()
decl_stmt|;
comment|// construct a mapping of (Partition->bucket file names) and (Partition -> bucket number)
if|if
condition|(
operator|!
name|partitions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|Partition
name|p
range|:
name|partitions
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|fileNames
init|=
name|AbstractBucketJoinProc
operator|.
name|getBucketFilePathsOfPartition
argument_list|(
name|p
operator|.
name|getDataLocation
argument_list|()
argument_list|,
name|pGraphContext
argument_list|)
decl_stmt|;
comment|// The number of files for the table should be same as number of
comment|// buckets.
name|int
name|bucketCount
init|=
name|p
operator|.
name|getBucketCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileNames
operator|.
name|size
argument_list|()
operator|!=
literal|0
operator|&&
name|fileNames
operator|.
name|size
argument_list|()
operator|!=
name|bucketCount
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
else|else
block|{
name|List
argument_list|<
name|String
argument_list|>
name|fileNames
init|=
name|AbstractBucketJoinProc
operator|.
name|getBucketFilePathsOfPartition
argument_list|(
name|tbl
operator|.
name|getDataLocation
argument_list|()
argument_list|,
name|pGraphContext
argument_list|)
decl_stmt|;
name|Integer
name|num
init|=
operator|new
name|Integer
argument_list|(
name|tbl
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
decl_stmt|;
comment|// The number of files for the table should be same as number of buckets.
if|if
condition|(
name|fileNames
operator|.
name|size
argument_list|()
operator|!=
literal|0
operator|&&
name|fileNames
operator|.
name|size
argument_list|()
operator|!=
name|num
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
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|TableScanOperator
name|ts
init|=
operator|(
name|TableScanOperator
operator|)
name|nd
decl_stmt|;
name|AnnotateOpTraitsProcCtx
name|opTraitsCtx
init|=
operator|(
name|AnnotateOpTraitsProcCtx
operator|)
name|procCtx
decl_stmt|;
name|Table
name|table
init|=
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
decl_stmt|;
name|PrunedPartitionList
name|prunedPartList
init|=
literal|null
decl_stmt|;
try|try
block|{
name|prunedPartList
operator|=
name|opTraitsCtx
operator|.
name|getParseContext
argument_list|()
operator|.
name|getPrunedPartitions
argument_list|(
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getAlias
argument_list|()
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|prunedPartList
operator|=
literal|null
expr_stmt|;
block|}
name|boolean
name|isBucketed
init|=
name|checkBucketedTable
argument_list|(
name|table
argument_list|,
name|opTraitsCtx
operator|.
name|getParseContext
argument_list|()
argument_list|,
name|prunedPartList
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|bucketColsList
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|sortedColsList
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|numBuckets
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|isBucketed
condition|)
block|{
name|bucketColsList
operator|.
name|add
argument_list|(
name|table
operator|.
name|getBucketCols
argument_list|()
argument_list|)
expr_stmt|;
name|numBuckets
operator|=
name|table
operator|.
name|getNumBuckets
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|sortCols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Order
name|colSortOrder
range|:
name|table
operator|.
name|getSortCols
argument_list|()
control|)
block|{
name|sortCols
operator|.
name|add
argument_list|(
name|colSortOrder
operator|.
name|getCol
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sortedColsList
operator|.
name|add
argument_list|(
name|sortCols
argument_list|)
expr_stmt|;
block|}
comment|// num reduce sinks hardcoded to 0 because TS has no parents
name|OpTraits
name|opTraits
init|=
operator|new
name|OpTraits
argument_list|(
name|bucketColsList
argument_list|,
name|numBuckets
argument_list|,
name|sortedColsList
argument_list|)
decl_stmt|;
name|ts
operator|.
name|setOpTraits
argument_list|(
name|opTraits
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/*    * Group-by re-orders the keys emitted hence, the keyCols would change.    */
specifier|public
specifier|static
class|class
name|GroupByRule
implements|implements
name|NodeProcessor
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|GroupByOperator
name|gbyOp
init|=
operator|(
name|GroupByOperator
operator|)
name|nd
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|gbyKeys
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|exprDesc
range|:
name|gbyOp
operator|.
name|getConf
argument_list|()
operator|.
name|getKeys
argument_list|()
control|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|entry
range|:
name|gbyOp
operator|.
name|getColumnExprMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|exprDesc
operator|.
name|isSame
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
name|gbyKeys
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|listBucketCols
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|listBucketCols
operator|.
name|add
argument_list|(
name|gbyKeys
argument_list|)
expr_stmt|;
name|OpTraits
name|opTraits
init|=
operator|new
name|OpTraits
argument_list|(
name|listBucketCols
argument_list|,
operator|-
literal|1
argument_list|,
name|listBucketCols
argument_list|)
decl_stmt|;
name|gbyOp
operator|.
name|setOpTraits
argument_list|(
name|opTraits
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|SelectRule
implements|implements
name|NodeProcessor
block|{
specifier|public
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getConvertedColNames
parameter_list|(
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|parentColNames
parameter_list|,
name|SelectOperator
name|selOp
parameter_list|)
block|{
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|listBucketCols
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|selOp
operator|.
name|getColumnExprMap
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|parentColNames
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|List
argument_list|<
name|String
argument_list|>
name|colNames
range|:
name|parentColNames
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|bucketColNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|colName
range|:
name|colNames
control|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|entry
range|:
name|selOp
operator|.
name|getColumnExprMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
if|if
condition|(
operator|(
call|(
name|ExprNodeColumnDesc
call|)
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
operator|)
operator|.
name|getColumn
argument_list|()
operator|.
name|equals
argument_list|(
name|colName
argument_list|)
condition|)
block|{
name|bucketColNames
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|listBucketCols
operator|.
name|add
argument_list|(
name|bucketColNames
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|listBucketCols
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|SelectOperator
name|selOp
init|=
operator|(
name|SelectOperator
operator|)
name|nd
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|parentBucketColNames
init|=
name|selOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getOpTraits
argument_list|()
operator|.
name|getBucketColNames
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|listBucketCols
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|listSortCols
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|selOp
operator|.
name|getColumnExprMap
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|parentBucketColNames
operator|!=
literal|null
condition|)
block|{
name|listBucketCols
operator|=
name|getConvertedColNames
argument_list|(
name|parentBucketColNames
argument_list|,
name|selOp
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|parentSortColNames
init|=
name|selOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getOpTraits
argument_list|()
operator|.
name|getSortCols
argument_list|()
decl_stmt|;
if|if
condition|(
name|parentSortColNames
operator|!=
literal|null
condition|)
block|{
name|listSortCols
operator|=
name|getConvertedColNames
argument_list|(
name|parentSortColNames
argument_list|,
name|selOp
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|numBuckets
init|=
operator|-
literal|1
decl_stmt|;
name|OpTraits
name|parentOpTraits
init|=
name|selOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getOpTraits
argument_list|()
decl_stmt|;
if|if
condition|(
name|parentOpTraits
operator|!=
literal|null
condition|)
block|{
name|numBuckets
operator|=
name|parentOpTraits
operator|.
name|getNumBuckets
argument_list|()
expr_stmt|;
block|}
name|OpTraits
name|opTraits
init|=
operator|new
name|OpTraits
argument_list|(
name|listBucketCols
argument_list|,
name|numBuckets
argument_list|,
name|listSortCols
argument_list|)
decl_stmt|;
name|selOp
operator|.
name|setOpTraits
argument_list|(
name|opTraits
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|JoinRule
implements|implements
name|NodeProcessor
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|JoinOperator
name|joinOp
init|=
operator|(
name|JoinOperator
operator|)
name|nd
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|bucketColsList
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|sortColsList
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|byte
name|pos
init|=
literal|0
decl_stmt|;
name|int
name|numReduceSinks
init|=
literal|0
decl_stmt|;
comment|// will be set to the larger of the parents
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOp
range|:
name|joinOp
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|parentOp
operator|instanceof
name|ReduceSinkOperator
operator|)
condition|)
block|{
comment|// can be mux operator
break|break;
block|}
name|ReduceSinkOperator
name|rsOp
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|parentOp
decl_stmt|;
if|if
condition|(
name|rsOp
operator|.
name|getOpTraits
argument_list|()
operator|==
literal|null
condition|)
block|{
name|ReduceSinkRule
name|rsRule
init|=
operator|new
name|ReduceSinkRule
argument_list|()
decl_stmt|;
name|rsRule
operator|.
name|process
argument_list|(
name|rsOp
argument_list|,
name|stack
argument_list|,
name|procCtx
argument_list|,
name|nodeOutputs
argument_list|)
expr_stmt|;
block|}
name|OpTraits
name|parentOpTraits
init|=
name|rsOp
operator|.
name|getOpTraits
argument_list|()
decl_stmt|;
name|bucketColsList
operator|.
name|add
argument_list|(
name|getOutputColNames
argument_list|(
name|joinOp
argument_list|,
name|parentOpTraits
operator|.
name|getBucketColNames
argument_list|()
argument_list|,
name|pos
argument_list|)
argument_list|)
expr_stmt|;
name|sortColsList
operator|.
name|add
argument_list|(
name|getOutputColNames
argument_list|(
name|joinOp
argument_list|,
name|parentOpTraits
operator|.
name|getSortCols
argument_list|()
argument_list|,
name|pos
argument_list|)
argument_list|)
expr_stmt|;
name|pos
operator|++
expr_stmt|;
block|}
name|joinOp
operator|.
name|setOpTraits
argument_list|(
operator|new
name|OpTraits
argument_list|(
name|bucketColsList
argument_list|,
operator|-
literal|1
argument_list|,
name|bucketColsList
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getOutputColNames
parameter_list|(
name|JoinOperator
name|joinOp
parameter_list|,
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|parentColNames
parameter_list|,
name|byte
name|pos
parameter_list|)
block|{
if|if
condition|(
name|parentColNames
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|bucketColNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// guaranteed that there is only 1 list within this list because
comment|// a reduce sink always brings down the bucketing cols to a single list.
comment|// may not be true with correlation operators (mux-demux)
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
name|parentColNames
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|colName
range|:
name|colNames
control|)
block|{
for|for
control|(
name|ExprNodeDesc
name|exprNode
range|:
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getExprs
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
control|)
block|{
if|if
condition|(
name|exprNode
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
if|if
condition|(
operator|(
call|(
name|ExprNodeColumnDesc
call|)
argument_list|(
name|exprNode
argument_list|)
operator|)
operator|.
name|getColumn
argument_list|()
operator|.
name|equals
argument_list|(
name|colName
argument_list|)
condition|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|entry
range|:
name|joinOp
operator|.
name|getColumnExprMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|isSame
argument_list|(
name|exprNode
argument_list|)
condition|)
block|{
name|bucketColNames
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
comment|// we have found the colName
break|break;
block|}
block|}
block|}
else|else
block|{
comment|// continue on to the next exprNode to find a match
continue|continue;
block|}
comment|// we have found the colName. No need to search more exprNodes.
break|break;
block|}
block|}
block|}
return|return
name|bucketColNames
return|;
block|}
comment|// no col names in parent
return|return
literal|null
return|;
block|}
block|}
comment|/*    * When we have operators that have multiple parents, it is not clear which    * parent's traits we need to propagate forward.    */
specifier|public
specifier|static
class|class
name|MultiParentRule
implements|implements
name|NodeProcessor
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|operator
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|)
name|nd
decl_stmt|;
name|OpTraits
name|opTraits
init|=
operator|new
name|OpTraits
argument_list|(
literal|null
argument_list|,
operator|-
literal|1
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|operator
operator|.
name|setOpTraits
argument_list|(
name|opTraits
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getTableScanRule
parameter_list|()
block|{
return|return
operator|new
name|TableScanRule
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getReduceSinkRule
parameter_list|()
block|{
return|return
operator|new
name|ReduceSinkRule
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getSelectRule
parameter_list|()
block|{
return|return
operator|new
name|SelectRule
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getDefaultRule
parameter_list|()
block|{
return|return
operator|new
name|DefaultRule
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getMultiParentRule
parameter_list|()
block|{
return|return
operator|new
name|MultiParentRule
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getGroupByRule
parameter_list|()
block|{
return|return
operator|new
name|GroupByRule
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getJoinRule
parameter_list|()
block|{
return|return
operator|new
name|JoinRule
argument_list|()
return|;
block|}
block|}
end_class

end_unit

