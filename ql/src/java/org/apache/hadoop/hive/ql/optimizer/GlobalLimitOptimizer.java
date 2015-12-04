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
name|Collection
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
name|hive
operator|.
name|ql
operator|.
name|Context
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
name|FilterOperator
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
name|LimitOperator
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
name|GlobalLimitCtx
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
name|parse
operator|.
name|SplitSample
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
name|FilterDesc
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
name|GroupByDesc
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
name|ReduceSinkDesc
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
name|ImmutableSet
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
name|Multimap
import|;
end_import

begin_comment
comment|/**  * This optimizer is used to reduce the input size for the query for queries which are  * specifying a limit.  *<p/>  * For eg. for a query of type:  *<p/>  * select expr from T where<filter> limit 100;  *<p/>  * Most probably, the whole table T need not be scanned.  * Chances are that even if we scan the first file of T, we would get the 100 rows  * needed by this query.  * This optimizer step populates the GlobalLimitCtx which is used later on to prune the inputs.  */
end_comment

begin_class
specifier|public
class|class
name|GlobalLimitOptimizer
implements|implements
name|Transform
block|{
specifier|private
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|GlobalLimitOptimizer
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
name|ParseContext
name|transform
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Context
name|ctx
init|=
name|pctx
operator|.
name|getContext
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|TableScanOperator
argument_list|>
name|topOps
init|=
name|pctx
operator|.
name|getTopOps
argument_list|()
decl_stmt|;
name|GlobalLimitCtx
name|globalLimitCtx
init|=
name|pctx
operator|.
name|getGlobalLimitCtx
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|SplitSample
argument_list|>
name|nameToSplitSample
init|=
name|pctx
operator|.
name|getNameToSplitSample
argument_list|()
decl_stmt|;
comment|// determine the query qualifies reduce input size for LIMIT
comment|// The query only qualifies when there are only one top operator
comment|// and there is no transformer or UDTF and no block sampling
comment|// is used.
if|if
condition|(
name|ctx
operator|.
name|getTryCount
argument_list|()
operator|==
literal|0
operator|&&
name|topOps
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
operator|!
name|globalLimitCtx
operator|.
name|ifHasTransformOrUDTF
argument_list|()
operator|&&
name|nameToSplitSample
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// Here we recursively check:
comment|// 1. whether there are exact one LIMIT in the query
comment|// 2. whether there is no aggregation, group-by, distinct, sort by,
comment|//    distributed by, or table sampling in any of the sub-query.
comment|// The query only qualifies if both conditions are satisfied.
comment|//
comment|// Example qualified queries:
comment|//    CREATE TABLE ... AS SELECT col1, col2 FROM tbl LIMIT ..
comment|//    INSERT OVERWRITE TABLE ... SELECT col1, hash(col2), split(col1)
comment|//                               FROM ... LIMIT...
comment|//    SELECT * FROM (SELECT col1 as col2 (SELECT * FROM ...) t1 LIMIT ...) t2);
comment|//
name|TableScanOperator
name|ts
init|=
name|topOps
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|Integer
name|tempGlobalLimit
init|=
name|checkQbpForGlobalLimit
argument_list|(
name|ts
argument_list|)
decl_stmt|;
comment|// query qualify for the optimization
if|if
condition|(
name|tempGlobalLimit
operator|!=
literal|null
operator|&&
name|tempGlobalLimit
operator|!=
literal|0
condition|)
block|{
name|Table
name|tab
init|=
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|FilterOperator
argument_list|>
name|filterOps
init|=
name|OperatorUtils
operator|.
name|findOperators
argument_list|(
name|ts
argument_list|,
name|FilterOperator
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|tab
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
if|if
condition|(
name|filterOps
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|globalLimitCtx
operator|.
name|enableOpt
argument_list|(
name|tempGlobalLimit
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// check if the pruner only contains partition columns
if|if
condition|(
name|onlyContainsPartnCols
argument_list|(
name|tab
argument_list|,
name|filterOps
argument_list|)
condition|)
block|{
name|String
name|alias
init|=
operator|(
name|String
operator|)
name|topOps
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|PrunedPartitionList
name|partsList
init|=
name|pctx
operator|.
name|getPrunedPartitions
argument_list|(
name|alias
argument_list|,
name|ts
argument_list|)
decl_stmt|;
comment|// If there is any unknown partition, create a map-reduce job for
comment|// the filter to prune correctly
if|if
condition|(
operator|!
name|partsList
operator|.
name|hasUnknownPartitions
argument_list|()
condition|)
block|{
name|globalLimitCtx
operator|.
name|enableOpt
argument_list|(
name|tempGlobalLimit
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|globalLimitCtx
operator|.
name|isEnable
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Qualify the optimize that reduces input size for 'limit' for limit "
operator|+
name|globalLimitCtx
operator|.
name|getGlobalLimit
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|pctx
return|;
block|}
specifier|private
name|boolean
name|onlyContainsPartnCols
parameter_list|(
name|Table
name|table
parameter_list|,
name|Set
argument_list|<
name|FilterOperator
argument_list|>
name|filters
parameter_list|)
block|{
for|for
control|(
name|FilterOperator
name|filter
range|:
name|filters
control|)
block|{
if|if
condition|(
operator|!
name|PartitionPruner
operator|.
name|onlyContainsPartnCols
argument_list|(
name|table
argument_list|,
name|filter
operator|.
name|getConf
argument_list|()
operator|.
name|getPredicate
argument_list|()
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
comment|/**    * Check the limit number in all sub queries    *    * @return if there is one and only one limit for all subqueries, return the limit    *         if there is no limit, return 0    *         otherwise, return null    */
specifier|private
specifier|static
name|Integer
name|checkQbpForGlobalLimit
parameter_list|(
name|TableScanOperator
name|ts
parameter_list|)
block|{
name|Set
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|searchedClasses
init|=
operator|new
name|ImmutableSet
operator|.
name|Builder
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
argument_list|()
operator|.
name|add
argument_list|(
name|ReduceSinkOperator
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|GroupByOperator
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|FilterOperator
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|LimitOperator
operator|.
name|class
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Multimap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|ops
init|=
name|OperatorUtils
operator|.
name|classifyOperators
argument_list|(
name|ts
argument_list|,
name|searchedClasses
argument_list|)
decl_stmt|;
comment|// To apply this optimization, in the input query:
comment|// - There cannot exist any order by/sort by clause,
comment|// thus existsOrdering should be false.
comment|// - There cannot exist any distribute by clause, thus
comment|// existsPartitioning should be false.
comment|// - There cannot exist any cluster by clause, thus
comment|// existsOrdering AND existsPartitioning should be false.
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|ops
operator|.
name|get
argument_list|(
name|ReduceSinkOperator
operator|.
name|class
argument_list|)
control|)
block|{
name|ReduceSinkDesc
name|reduceSinkConf
init|=
operator|(
operator|(
name|ReduceSinkOperator
operator|)
name|op
operator|)
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|reduceSinkConf
operator|.
name|isOrdering
argument_list|()
operator|||
name|reduceSinkConf
operator|.
name|isPartitioning
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
comment|// - There cannot exist any (distinct) aggregate.
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|ops
operator|.
name|get
argument_list|(
name|GroupByOperator
operator|.
name|class
argument_list|)
control|)
block|{
name|GroupByDesc
name|groupByConf
init|=
operator|(
operator|(
name|GroupByOperator
operator|)
name|op
operator|)
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|groupByConf
operator|.
name|isAggregate
argument_list|()
operator|||
name|groupByConf
operator|.
name|isDistinct
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
comment|// - There cannot exist any sampling predicate.
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|ops
operator|.
name|get
argument_list|(
name|FilterOperator
operator|.
name|class
argument_list|)
control|)
block|{
name|FilterDesc
name|filterConf
init|=
operator|(
operator|(
name|FilterOperator
operator|)
name|op
operator|)
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|filterConf
operator|.
name|getIsSamplingPred
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
comment|// If there is one and only one limit starting at op, return the limit
comment|// If there is no limit, return 0
comment|// Otherwise, return null
name|Collection
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|limitOps
init|=
name|ops
operator|.
name|get
argument_list|(
name|LimitOperator
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|limitOps
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
operator|(
operator|(
name|LimitOperator
operator|)
name|limitOps
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getLimit
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|limitOps
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

