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
name|spark
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
name|ql
operator|.
name|exec
operator|.
name|OperatorUtils
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
name|MapJoinOperator
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
name|MuxOperator
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
name|UnionOperator
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
name|optimizer
operator|.
name|BucketMapjoinProc
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
name|MapJoinProcessor
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
name|parse
operator|.
name|spark
operator|.
name|OptimizeSparkProcContext
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
name|MapJoinDesc
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
name|serde
operator|.
name|serdeConstants
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
name|serde2
operator|.
name|binarysortable
operator|.
name|BinarySortableSerDe
import|;
end_import

begin_comment
comment|/**  * SparkMapJoinOptimizer cloned from ConvertJoinMapJoin is an optimization that replaces a common join  * (aka shuffle join) with a map join (aka broadcast or fragment replicate  * join when possible. Map joins have restrictions on which joins can be  * converted (e.g.: full outer joins cannot be handled as map joins) as well  * as memory restrictions (one side of the join has to fit into memory).  */
end_comment

begin_class
specifier|public
class|class
name|SparkMapJoinOptimizer
implements|implements
name|NodeProcessor
block|{
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
name|SparkMapJoinOptimizer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
comment|/**    * We should ideally not modify the tree we traverse. However,    * since we need to walk the tree at any time when we modify the operator, we    * might as well do it here.    */
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
name|OptimizeSparkProcContext
name|context
init|=
operator|(
name|OptimizeSparkProcContext
operator|)
name|procCtx
decl_stmt|;
name|HiveConf
name|conf
init|=
name|context
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|JoinOperator
name|joinOp
init|=
operator|(
name|JoinOperator
operator|)
name|nd
decl_stmt|;
if|if
condition|(
operator|!
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECONVERTJOIN
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Check if it can be converted to map join"
argument_list|)
expr_stmt|;
name|long
index|[]
name|mapJoinInfo
init|=
name|getMapJoinConversionInfo
argument_list|(
name|joinOp
argument_list|,
name|context
argument_list|)
decl_stmt|;
name|int
name|mapJoinConversionPos
init|=
operator|(
name|int
operator|)
name|mapJoinInfo
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|mapJoinConversionPos
operator|<
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|numBuckets
init|=
operator|-
literal|1
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|bucketColNames
init|=
literal|null
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Convert to non-bucketed map join"
argument_list|)
expr_stmt|;
name|MapJoinOperator
name|mapJoinOp
init|=
name|convertJoinMapJoin
argument_list|(
name|joinOp
argument_list|,
name|context
argument_list|,
name|mapJoinConversionPos
argument_list|)
decl_stmt|;
comment|// For native vectorized map join, we require the key SerDe to be BinarySortableSerDe
comment|// Note: the MJ may not really get natively-vectorized later,
comment|// but changing SerDe won't hurt correctness
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_MAPJOIN_NATIVE_ENABLED
argument_list|)
operator|&&
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_ENABLED
argument_list|)
condition|)
block|{
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getKeyTblDesc
argument_list|()
operator|.
name|getProperties
argument_list|()
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_LIB
argument_list|,
name|BinarySortableSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTBUCKETMAPJOIN
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Check if it can be converted to bucketed map join"
argument_list|)
expr_stmt|;
name|numBuckets
operator|=
name|convertJoinBucketMapJoin
argument_list|(
name|joinOp
argument_list|,
name|mapJoinOp
argument_list|,
name|context
argument_list|,
name|mapJoinConversionPos
argument_list|)
expr_stmt|;
if|if
condition|(
name|numBuckets
operator|>
literal|1
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Converted to map join with "
operator|+
name|numBuckets
operator|+
literal|" buckets"
argument_list|)
expr_stmt|;
name|bucketColNames
operator|=
name|joinOp
operator|.
name|getOpTraits
argument_list|()
operator|.
name|getBucketColNames
argument_list|()
expr_stmt|;
name|mapJoinInfo
index|[
literal|2
index|]
operator|/=
name|numBuckets
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Can not convert to bucketed map join"
argument_list|)
expr_stmt|;
block|}
block|}
comment|// we can set the traits for this join operator
name|OpTraits
name|opTraits
init|=
operator|new
name|OpTraits
argument_list|(
name|bucketColNames
argument_list|,
name|numBuckets
argument_list|,
literal|null
argument_list|,
name|joinOp
operator|.
name|getOpTraits
argument_list|()
operator|.
name|getNumReduceSinks
argument_list|()
argument_list|)
decl_stmt|;
name|mapJoinOp
operator|.
name|setOpTraits
argument_list|(
name|opTraits
argument_list|)
expr_stmt|;
name|mapJoinOp
operator|.
name|setStatistics
argument_list|(
name|joinOp
operator|.
name|getStatistics
argument_list|()
argument_list|)
expr_stmt|;
name|setNumberOfBucketsOnChildren
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
name|context
operator|.
name|getMjOpSizes
argument_list|()
operator|.
name|put
argument_list|(
name|mapJoinOp
argument_list|,
name|mapJoinInfo
index|[
literal|1
index|]
operator|+
name|mapJoinInfo
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
return|return
name|mapJoinOp
return|;
block|}
specifier|private
name|void
name|setNumberOfBucketsOnChildren
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|currentOp
parameter_list|)
block|{
name|int
name|numBuckets
init|=
name|currentOp
operator|.
name|getOpTraits
argument_list|()
operator|.
name|getNumBuckets
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
name|currentOp
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|op
operator|instanceof
name|ReduceSinkOperator
operator|)
operator|&&
operator|!
operator|(
name|op
operator|instanceof
name|GroupByOperator
operator|)
condition|)
block|{
name|op
operator|.
name|getOpTraits
argument_list|()
operator|.
name|setNumBuckets
argument_list|(
name|numBuckets
argument_list|)
expr_stmt|;
if|if
condition|(
name|numBuckets
operator|<
literal|0
condition|)
block|{
name|op
operator|.
name|getOpTraits
argument_list|()
operator|.
name|setBucketColNames
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|setNumberOfBucketsOnChildren
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|int
name|convertJoinBucketMapJoin
parameter_list|(
name|JoinOperator
name|joinOp
parameter_list|,
name|MapJoinOperator
name|mapJoinOp
parameter_list|,
name|OptimizeSparkProcContext
name|context
parameter_list|,
name|int
name|bigTablePosition
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ParseContext
name|parseContext
init|=
name|context
operator|.
name|getParseContext
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|joinAliases
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|baseBigAlias
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|posToAliasMap
init|=
name|joinOp
operator|.
name|getPosToAliasMap
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Integer
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|posToAliasMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|intValue
argument_list|()
operator|==
name|bigTablePosition
condition|)
block|{
name|baseBigAlias
operator|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|String
name|alias
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|joinAliases
operator|.
name|contains
argument_list|(
name|alias
argument_list|)
condition|)
block|{
name|joinAliases
operator|.
name|add
argument_list|(
name|alias
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|mapJoinOp
operator|.
name|setPosToAliasMap
argument_list|(
name|posToAliasMap
argument_list|)
expr_stmt|;
name|BucketMapjoinProc
operator|.
name|checkAndConvertBucketMapJoin
argument_list|(
name|parseContext
argument_list|,
name|mapJoinOp
argument_list|,
name|baseBigAlias
argument_list|,
name|joinAliases
argument_list|)
expr_stmt|;
name|MapJoinDesc
name|joinDesc
init|=
name|mapJoinOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
return|return
name|joinDesc
operator|.
name|isBucketMapJoin
argument_list|()
condition|?
name|joinDesc
operator|.
name|getBigTableBucketNumMapping
argument_list|()
operator|.
name|size
argument_list|()
else|:
operator|-
literal|1
return|;
block|}
comment|/**    *   This method returns the big table position in a map-join. If the given join    *   cannot be converted to a map-join (This could happen for several reasons - one    *   of them being presence of 2 or more big tables that cannot fit in-memory), it returns -1.    *    *   Otherwise, it returns an int value that is the index of the big table in the set    *   MapJoinProcessor.bigTableCandidateSet    *    * @param joinOp    * @param context    * @return an array of 3 long values, first value is the position,    *   second value is the connected map join size, and the third is big table data size.    */
specifier|private
name|long
index|[]
name|getMapJoinConversionInfo
parameter_list|(
name|JoinOperator
name|joinOp
parameter_list|,
name|OptimizeSparkProcContext
name|context
parameter_list|)
block|{
name|Set
argument_list|<
name|Integer
argument_list|>
name|bigTableCandidateSet
init|=
name|MapJoinProcessor
operator|.
name|getBigTableCandidates
argument_list|(
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getConds
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|maxSize
init|=
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|getLongVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECONVERTJOINNOCONDITIONALTASKTHRESHOLD
argument_list|)
decl_stmt|;
name|int
name|bigTablePosition
init|=
operator|-
literal|1
decl_stmt|;
name|Statistics
name|bigInputStat
init|=
literal|null
decl_stmt|;
name|long
name|totalSize
init|=
literal|0
decl_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
comment|// bigTableFound means we've encountered a table that's bigger than the
comment|// max. This table is either the big table or we cannot convert.
name|boolean
name|bigTableFound
init|=
literal|false
decl_stmt|;
name|boolean
name|useTsStats
init|=
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolean
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_USE_TS_STATS_FOR_MAPJOIN
operator|.
name|varname
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|boolean
name|hasUpstreamSinks
init|=
literal|false
decl_stmt|;
comment|// Check whether there's any upstream RS.
comment|// If so, don't use TS stats because they could be inaccurate.
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
name|Set
argument_list|<
name|ReduceSinkOperator
argument_list|>
name|parentSinks
init|=
name|OperatorUtils
operator|.
name|findOperatorsUpstream
argument_list|(
name|parentOp
argument_list|,
name|ReduceSinkOperator
operator|.
name|class
argument_list|)
decl_stmt|;
name|parentSinks
operator|.
name|remove
argument_list|(
name|parentOp
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|parentSinks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|hasUpstreamSinks
operator|=
literal|true
expr_stmt|;
block|}
block|}
comment|// If we are using TS stats and this JOIN has at least one upstream RS, disable MapJoin conversion.
if|if
condition|(
name|useTsStats
operator|&&
name|hasUpstreamSinks
condition|)
block|{
return|return
operator|new
name|long
index|[]
block|{
operator|-
literal|1
block|,
literal|0
block|,
literal|0
block|}
return|;
block|}
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
name|Statistics
name|currInputStat
decl_stmt|;
if|if
condition|(
name|useTsStats
condition|)
block|{
name|currInputStat
operator|=
operator|new
name|Statistics
argument_list|()
expr_stmt|;
comment|// Find all root TSs and add up all data sizes
comment|// Not adding other stats (e.g., # of rows, col stats) since only data size is used here
for|for
control|(
name|TableScanOperator
name|root
range|:
name|OperatorUtils
operator|.
name|findOperatorsUpstream
argument_list|(
name|parentOp
argument_list|,
name|TableScanOperator
operator|.
name|class
argument_list|)
control|)
block|{
name|currInputStat
operator|.
name|addToDataSize
argument_list|(
name|root
operator|.
name|getStatistics
argument_list|()
operator|.
name|getDataSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|currInputStat
operator|=
name|parentOp
operator|.
name|getStatistics
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|currInputStat
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Couldn't get statistics from: "
operator|+
name|parentOp
argument_list|)
expr_stmt|;
return|return
operator|new
name|long
index|[]
block|{
operator|-
literal|1
block|,
literal|0
block|,
literal|0
block|}
return|;
block|}
comment|// Union is hard to handle. For instance, the following case:
comment|//  TS    TS
comment|//  |      |
comment|//  FIL   FIL
comment|//  |      |
comment|//  SEL   SEL
comment|//    \   /
comment|//    UNION
comment|//      |
comment|//      RS
comment|//      |
comment|//     JOIN
comment|// If we treat this as a MJ case, then after the RS is removed, we would
comment|// create two MapWorks, for each of the TS. Each of these MapWork will contain
comment|// a MJ operator, which is wrong.
comment|// Otherwise, we could try to break the op tree at the UNION, and create two MapWorks
comment|// for the branches above. Then, MJ will be in the following ReduceWork.
comment|// But, this is tricky to implement, and we'll leave it as a future work for now.
if|if
condition|(
name|containUnionWithoutRS
argument_list|(
name|parentOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
condition|)
block|{
return|return
operator|new
name|long
index|[]
block|{
operator|-
literal|1
block|,
literal|0
block|,
literal|0
block|}
return|;
block|}
name|long
name|inputSize
init|=
name|currInputStat
operator|.
name|getDataSize
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|bigInputStat
operator|==
literal|null
operator|)
operator|||
operator|(
operator|(
name|bigInputStat
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|inputSize
operator|>
name|bigInputStat
operator|.
name|getDataSize
argument_list|()
operator|)
operator|)
condition|)
block|{
if|if
condition|(
name|bigTableFound
condition|)
block|{
comment|// cannot convert to map join; we've already chosen a big table
comment|// on size and there's another one that's bigger.
return|return
operator|new
name|long
index|[]
block|{
operator|-
literal|1
block|,
literal|0
block|,
literal|0
block|}
return|;
block|}
if|if
condition|(
name|inputSize
operator|>
name|maxSize
condition|)
block|{
if|if
condition|(
operator|!
name|bigTableCandidateSet
operator|.
name|contains
argument_list|(
name|pos
argument_list|)
condition|)
block|{
comment|// can't use the current table as the big table, but it's too
comment|// big for the map side.
return|return
operator|new
name|long
index|[]
block|{
operator|-
literal|1
block|,
literal|0
block|,
literal|0
block|}
return|;
block|}
name|bigTableFound
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|bigInputStat
operator|!=
literal|null
condition|)
block|{
comment|// we're replacing the current big table with a new one. Need
comment|// to count the current one as a map table then.
name|totalSize
operator|+=
name|bigInputStat
operator|.
name|getDataSize
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|totalSize
operator|>
name|maxSize
condition|)
block|{
comment|// sum of small tables size in this join exceeds configured limit
comment|// hence cannot convert.
return|return
operator|new
name|long
index|[]
block|{
operator|-
literal|1
block|,
literal|0
block|,
literal|0
block|}
return|;
block|}
if|if
condition|(
name|bigTableCandidateSet
operator|.
name|contains
argument_list|(
name|pos
argument_list|)
condition|)
block|{
name|bigTablePosition
operator|=
name|pos
expr_stmt|;
name|bigInputStat
operator|=
name|currInputStat
expr_stmt|;
block|}
block|}
else|else
block|{
name|totalSize
operator|+=
name|currInputStat
operator|.
name|getDataSize
argument_list|()
expr_stmt|;
if|if
condition|(
name|totalSize
operator|>
name|maxSize
condition|)
block|{
comment|// cannot hold all map tables in memory. Cannot convert.
return|return
operator|new
name|long
index|[]
block|{
operator|-
literal|1
block|,
literal|0
block|,
literal|0
block|}
return|;
block|}
block|}
name|pos
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|bigTablePosition
operator|==
operator|-
literal|1
condition|)
block|{
comment|//No big table candidates.
return|return
operator|new
name|long
index|[]
block|{
operator|-
literal|1
block|,
literal|0
block|,
literal|0
block|}
return|;
block|}
comment|//Final check, find size of already-calculated Mapjoin Operators in same work (spark-stage).
comment|//We need to factor this in to prevent overwhelming Spark executor-memory.
name|long
name|connectedMapJoinSize
init|=
name|getConnectedMapJoinSize
argument_list|(
name|joinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|bigTablePosition
argument_list|)
argument_list|,
name|joinOp
argument_list|,
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|connectedMapJoinSize
operator|+
name|totalSize
operator|)
operator|>
name|maxSize
condition|)
block|{
return|return
operator|new
name|long
index|[]
block|{
operator|-
literal|1
block|,
literal|0
block|,
literal|0
block|}
return|;
block|}
return|return
operator|new
name|long
index|[]
block|{
name|bigTablePosition
block|,
name|connectedMapJoinSize
block|,
name|totalSize
block|}
return|;
block|}
comment|/**    * Examines this operator and all the connected operators, for mapjoins that will be in the same work.    * @param parentOp potential big-table parent operator, explore up from this.    * @param joinOp potential mapjoin operator, explore down from this.    * @param ctx context to pass information.    * @return total size of parent mapjoins in same work as this operator.    */
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"rawtypes"
block|,
literal|"unchecked"
block|}
argument_list|)
specifier|private
name|long
name|getConnectedMapJoinSize
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOp
parameter_list|,
name|Operator
name|joinOp
parameter_list|,
name|OptimizeSparkProcContext
name|ctx
parameter_list|)
block|{
name|long
name|result
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|grandParentOp
range|:
name|parentOp
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
name|result
operator|+=
name|getConnectedParentMapJoinSize
argument_list|(
name|grandParentOp
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
name|result
operator|+=
name|getConnectedChildMapJoinSize
argument_list|(
name|joinOp
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Examines this operator and all the parents, for mapjoins that will be in the same work.    * @param op given operator    * @param ctx context to pass information.    * @return total size of parent mapjoins in same work as this operator.    */
specifier|private
name|long
name|getConnectedParentMapJoinSize
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|OptimizeSparkProcContext
name|ctx
parameter_list|)
block|{
if|if
condition|(
operator|(
name|op
operator|instanceof
name|UnionOperator
operator|)
operator|||
operator|(
name|op
operator|instanceof
name|ReduceSinkOperator
operator|)
condition|)
block|{
comment|//Work Boundary, stop exploring.
return|return
literal|0
return|;
block|}
if|if
condition|(
name|op
operator|instanceof
name|MapJoinOperator
condition|)
block|{
comment|//found parent mapjoin operator.  Its size should already reflect any other mapjoins connected to it.
name|long
name|mjSize
init|=
name|ctx
operator|.
name|getMjOpSizes
argument_list|()
operator|.
name|get
argument_list|(
name|op
argument_list|)
decl_stmt|;
return|return
name|mjSize
return|;
block|}
name|long
name|result
init|=
literal|0
decl_stmt|;
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
name|op
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
comment|//Else, recurse up the parents.
name|result
operator|+=
name|getConnectedParentMapJoinSize
argument_list|(
name|parentOp
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Examines this operator and all the children, for mapjoins that will be in the same work.    * @param op given operator    * @param ctx context to pass information.    * @return total size of child mapjoins in same work as this operator.    */
specifier|private
name|long
name|getConnectedChildMapJoinSize
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|OptimizeSparkProcContext
name|ctx
parameter_list|)
block|{
if|if
condition|(
operator|(
name|op
operator|instanceof
name|UnionOperator
operator|)
operator|||
operator|(
name|op
operator|instanceof
name|ReduceSinkOperator
operator|)
condition|)
block|{
comment|//Work Boundary, stop exploring.
return|return
literal|0
return|;
block|}
if|if
condition|(
name|op
operator|instanceof
name|MapJoinOperator
condition|)
block|{
comment|//Found child mapjoin operator.
comment|//Its size should already reflect any mapjoins connected to it, so stop processing.
name|long
name|mjSize
init|=
name|ctx
operator|.
name|getMjOpSizes
argument_list|()
operator|.
name|get
argument_list|(
name|op
argument_list|)
decl_stmt|;
return|return
name|mjSize
return|;
block|}
name|long
name|result
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|childOp
range|:
name|op
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
comment|//Else, recurse to the children.
name|result
operator|+=
name|getConnectedChildMapJoinSize
argument_list|(
name|childOp
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/*    * Once we have decided on the map join, the tree would transform from    *    *        |                   |    *       Join               MapJoin    *       / \                /   \    *     RS   RS   --->     RS    TS (big table)    *    /      \           /    *   TS       TS        TS (small table)    *    * for spark.    */
specifier|public
name|MapJoinOperator
name|convertJoinMapJoin
parameter_list|(
name|JoinOperator
name|joinOp
parameter_list|,
name|OptimizeSparkProcContext
name|context
parameter_list|,
name|int
name|bigTablePosition
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// bail on mux operator because currently the mux operator masks the emit keys
comment|// of the constituent reduce sinks.
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
name|parentOp
operator|instanceof
name|MuxOperator
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
comment|//can safely convert the join to a map join.
name|MapJoinOperator
name|mapJoinOp
init|=
name|MapJoinProcessor
operator|.
name|convertJoinOpMapJoinOp
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|joinOp
argument_list|,
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|isLeftInputJoin
argument_list|()
argument_list|,
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getBaseSrc
argument_list|()
argument_list|,
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getMapAliases
argument_list|()
argument_list|,
name|bigTablePosition
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentBigTableOp
init|=
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|bigTablePosition
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentBigTableOp
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|remove
argument_list|(
name|bigTablePosition
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
operator|(
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|contains
argument_list|(
name|parentBigTableOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|)
condition|)
block|{
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|add
argument_list|(
name|bigTablePosition
argument_list|,
name|parentBigTableOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|parentBigTableOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|removeChild
argument_list|(
name|parentBigTableOp
argument_list|)
expr_stmt|;
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
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|contains
argument_list|(
name|mapJoinOp
argument_list|)
operator|)
condition|)
block|{
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|add
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
block|}
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|remove
argument_list|(
name|joinOp
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Data structures
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|setQBJoinTreeProps
argument_list|(
name|joinOp
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|mapJoinOp
return|;
block|}
specifier|private
name|boolean
name|containUnionWithoutRS
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|)
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|op
operator|instanceof
name|UnionOperator
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|pop
range|:
name|op
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|pop
operator|instanceof
name|ReduceSinkOperator
operator|)
condition|)
block|{
name|result
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|op
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
name|result
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|pop
range|:
name|op
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
name|containUnionWithoutRS
argument_list|(
name|pop
argument_list|)
condition|)
block|{
name|result
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

