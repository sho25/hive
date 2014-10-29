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
name|optimizer
operator|.
name|correlation
operator|.
name|CorrelationOptimizer
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
name|correlation
operator|.
name|ReduceSinkDeDuplication
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
name|index
operator|.
name|RewriteGBUsingIndex
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
name|lineage
operator|.
name|Generator
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
name|listbucketingpruner
operator|.
name|ListBucketingPruner
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
name|metainfo
operator|.
name|annotation
operator|.
name|AnnotateWithOpTraits
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
name|pcr
operator|.
name|PartitionConditionRemover
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
name|optimizer
operator|.
name|stats
operator|.
name|annotation
operator|.
name|AnnotateWithStatistics
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
name|unionproc
operator|.
name|UnionProcessor
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
name|ppd
operator|.
name|PredicatePushDown
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
name|ppd
operator|.
name|PredicateTransitivePropagate
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
name|ppd
operator|.
name|SyntheticJoinPredicate
import|;
end_import

begin_comment
comment|/**  * Implementation of the optimizer.  */
end_comment

begin_class
specifier|public
class|class
name|Optimizer
block|{
specifier|private
name|ParseContext
name|pctx
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Transform
argument_list|>
name|transformations
decl_stmt|;
comment|/**    * Create the list of transformations.    *    * @param hiveConf    */
specifier|public
name|void
name|initialize
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|boolean
name|isTezExecEngine
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"tez"
argument_list|)
decl_stmt|;
name|boolean
name|isSparkExecEngine
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"spark"
argument_list|)
decl_stmt|;
name|boolean
name|bucketMapJoinOptimizer
init|=
literal|false
decl_stmt|;
name|transformations
operator|=
operator|new
name|ArrayList
argument_list|<
name|Transform
argument_list|>
argument_list|()
expr_stmt|;
comment|// Add the transformation that computes the lineage information.
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|Generator
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTPPD
argument_list|)
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|PredicateTransitivePropagate
argument_list|()
argument_list|)
expr_stmt|;
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|SyntheticJoinPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|PredicatePushDown
argument_list|()
argument_list|)
expr_stmt|;
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|PartitionPruner
argument_list|()
argument_list|)
expr_stmt|;
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|PartitionConditionRemover
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTLISTBUCKETING
argument_list|)
condition|)
block|{
comment|/* Add list bucketing pruner. */
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|ListBucketingPruner
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTGROUPBY
argument_list|)
operator|||
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_MAP_GROUPBY_SORT
argument_list|)
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|GroupByOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTCONSTANTPROPAGATION
argument_list|)
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|ConstantPropagate
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|ColumnPruner
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_OPTIMIZE_SKEWJOIN_COMPILETIME
argument_list|)
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|SkewJoinOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTGBYUSINGINDEX
argument_list|)
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|RewriteGBUsingIndex
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|SamplePruner
argument_list|()
argument_list|)
expr_stmt|;
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|MapJoinProcessor
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTBUCKETMAPJOIN
argument_list|)
operator|)
operator|&&
operator|!
name|isTezExecEngine
operator|&&
operator|!
name|isSparkExecEngine
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|BucketMapJoinOptimizer
argument_list|()
argument_list|)
expr_stmt|;
name|bucketMapJoinOptimizer
operator|=
literal|true
expr_stmt|;
block|}
comment|// If optimize hive.optimize.bucketmapjoin.sortedmerge is set, add both
comment|// BucketMapJoinOptimizer and SortedMergeBucketMapJoinOptimizer
if|if
condition|(
operator|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTSORTMERGEBUCKETMAPJOIN
argument_list|)
operator|)
operator|&&
operator|!
name|isTezExecEngine
operator|&&
operator|!
name|isSparkExecEngine
condition|)
block|{
if|if
condition|(
operator|!
name|bucketMapJoinOptimizer
condition|)
block|{
comment|// No need to add BucketMapJoinOptimizer twice
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|BucketMapJoinOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|SortedMergeBucketMapJoinOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTIMIZEBUCKETINGSORTING
argument_list|)
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|BucketingSortingReduceSinkOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|UnionProcessor
argument_list|()
argument_list|)
expr_stmt|;
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|JoinReorder
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DYNAMICPARTITIONING
argument_list|)
operator|&&
name|HiveConf
operator|.
name|getVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DYNAMICPARTITIONINGMODE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"nonstrict"
argument_list|)
operator|&&
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTSORTDYNAMICPARTITION
argument_list|)
operator|&&
operator|!
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTLISTBUCKETING
argument_list|)
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|SortedDynPartitionOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTREDUCEDEDUPLICATION
argument_list|)
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|ReduceSinkDeDuplication
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|NonBlockingOpDeDupProc
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVELIMITOPTENABLE
argument_list|)
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|GlobalLimitOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTCORRELATION
argument_list|)
operator|&&
operator|!
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEGROUPBYSKEW
argument_list|)
operator|&&
operator|!
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_OPTIMIZE_SKEWJOIN_COMPILETIME
argument_list|)
operator|&&
operator|!
name|isTezExecEngine
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|CorrelationOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVELIMITPUSHDOWNMEMORYUSAGE
argument_list|)
operator|>
literal|0
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|LimitPushdownOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTIMIZEMETADATAQUERIES
argument_list|)
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|StatsOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isSparkExecEngine
operator|||
operator|(
name|pctx
operator|.
name|getContext
argument_list|()
operator|.
name|getExplain
argument_list|()
operator|&&
operator|!
name|isTezExecEngine
operator|)
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|AnnotateWithStatistics
argument_list|()
argument_list|)
expr_stmt|;
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|AnnotateWithOpTraits
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|SimpleFetchOptimizer
argument_list|()
argument_list|)
expr_stmt|;
comment|// must be called last
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEFETCHTASKAGGR
argument_list|)
condition|)
block|{
name|transformations
operator|.
name|add
argument_list|(
operator|new
name|SimpleFetchAggregation
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Invoke all the transformations one-by-one, and alter the query plan.    *    * @return ParseContext    * @throws SemanticException    */
specifier|public
name|ParseContext
name|optimize
parameter_list|()
throws|throws
name|SemanticException
block|{
for|for
control|(
name|Transform
name|t
range|:
name|transformations
control|)
block|{
name|pctx
operator|=
name|t
operator|.
name|transform
argument_list|(
name|pctx
argument_list|)
expr_stmt|;
block|}
return|return
name|pctx
return|;
block|}
comment|/**    * @return the pctx    */
specifier|public
name|ParseContext
name|getPctx
parameter_list|()
block|{
return|return
name|pctx
return|;
block|}
comment|/**    * @param pctx    *          the pctx to set    */
specifier|public
name|void
name|setPctx
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
block|{
name|this
operator|.
name|pctx
operator|=
name|pctx
expr_stmt|;
block|}
block|}
end_class

end_unit

