begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
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
name|plan
operator|.
name|ExprNodeDesc
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
name|commons
operator|.
name|lang3
operator|.
name|tuple
operator|.
name|Pair
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
name|hive_metastoreConstants
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
name|FileSinkOperator
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
name|exec
operator|.
name|TerminalOperator
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
name|exec
operator|.
name|spark
operator|.
name|SparkUtilities
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
name|spark
operator|.
name|session
operator|.
name|SparkSession
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
name|spark
operator|.
name|session
operator|.
name|SparkSessionManager
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
name|spark
operator|.
name|session
operator|.
name|SparkSessionManagerImpl
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
name|SemanticNodeProcessor
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
name|GenSparkUtils
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
name|ReduceSinkDesc
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
import|import static
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
operator|.
name|ReducerTraits
operator|.
name|UNIFORM
import|;
end_import

begin_comment
comment|/**  * SetSparkReducerParallelism determines how many reducers should  * be run for a given reduce sink, clone from SetReducerParallelism.  */
end_comment

begin_class
specifier|public
class|class
name|SetSparkReducerParallelism
implements|implements
name|SemanticNodeProcessor
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
name|SetSparkReducerParallelism
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SPARK_DYNAMIC_ALLOCATION_ENABLED
init|=
literal|"spark.dynamicAllocation.enabled"
decl_stmt|;
comment|// Spark memory per task, and total number of cores
specifier|private
name|Pair
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
name|sparkMemoryAndCores
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|useOpStats
decl_stmt|;
specifier|public
name|SetSparkReducerParallelism
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|sparkMemoryAndCores
operator|=
literal|null
expr_stmt|;
name|useOpStats
operator|=
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_USE_OP_STATS
argument_list|)
expr_stmt|;
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
name|procContext
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
name|procContext
decl_stmt|;
name|ReduceSinkOperator
name|sink
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|nd
decl_stmt|;
name|ReduceSinkDesc
name|desc
init|=
name|sink
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|ReduceSinkOperator
argument_list|>
name|parentSinks
init|=
literal|null
decl_stmt|;
name|int
name|maxReducers
init|=
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAXREDUCERS
argument_list|)
decl_stmt|;
name|int
name|constantReducers
init|=
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPNUMREDUCERS
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|useOpStats
condition|)
block|{
name|parentSinks
operator|=
name|OperatorUtils
operator|.
name|findOperatorsUpstream
argument_list|(
name|sink
argument_list|,
name|ReduceSinkOperator
operator|.
name|class
argument_list|)
expr_stmt|;
name|parentSinks
operator|.
name|remove
argument_list|(
name|sink
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|context
operator|.
name|getVisitedReduceSinks
argument_list|()
operator|.
name|containsAll
argument_list|(
name|parentSinks
argument_list|)
condition|)
block|{
comment|// We haven't processed all the parent sinks, and we need
comment|// them to be done in order to compute the parallelism for this sink.
comment|// In this case, skip. We should visit this again from another path.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping sink "
operator|+
name|sink
operator|+
literal|" for now as we haven't seen all its parents."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
if|if
condition|(
name|context
operator|.
name|getVisitedReduceSinks
argument_list|()
operator|.
name|contains
argument_list|(
name|sink
argument_list|)
condition|)
block|{
comment|// skip walking the children
name|LOG
operator|.
name|debug
argument_list|(
literal|"Already processed reduce sink: "
operator|+
name|sink
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|context
operator|.
name|getVisitedReduceSinks
argument_list|()
operator|.
name|add
argument_list|(
name|sink
argument_list|)
expr_stmt|;
if|if
condition|(
name|needSetParallelism
argument_list|(
name|sink
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|constantReducers
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Parallelism for reduce sink "
operator|+
name|sink
operator|+
literal|" set by user to "
operator|+
name|constantReducers
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setNumReducers
argument_list|(
name|constantReducers
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|//If it's a FileSink to bucketed files, use the bucket count as the reducer number
name|FileSinkOperator
name|fso
init|=
name|GenSparkUtils
operator|.
name|getChildOperator
argument_list|(
name|sink
argument_list|,
name|FileSinkOperator
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|fso
operator|!=
literal|null
condition|)
block|{
name|String
name|bucketCount
init|=
name|fso
operator|.
name|getConf
argument_list|()
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getProperties
argument_list|()
operator|.
name|getProperty
argument_list|(
name|hive_metastoreConstants
operator|.
name|BUCKET_COUNT
argument_list|)
decl_stmt|;
name|int
name|numBuckets
init|=
name|bucketCount
operator|==
literal|null
condition|?
literal|0
else|:
name|Integer
operator|.
name|parseInt
argument_list|(
name|bucketCount
argument_list|)
decl_stmt|;
if|if
condition|(
name|numBuckets
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Set parallelism for reduce sink "
operator|+
name|sink
operator|+
literal|" to: "
operator|+
name|numBuckets
operator|+
literal|" (buckets)"
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setNumReducers
argument_list|(
name|numBuckets
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
if|if
condition|(
name|useOpStats
operator|||
name|parentSinks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|long
name|numberOfBytes
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|useOpStats
condition|)
block|{
comment|// we need to add up all the estimates from the siblings of this reduce sink
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|sibling
range|:
name|sink
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
name|sibling
operator|.
name|getStatistics
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|numberOfBytes
operator|=
name|StatsUtils
operator|.
name|safeAdd
argument_list|(
name|numberOfBytes
argument_list|,
name|sibling
operator|.
name|getStatistics
argument_list|()
operator|.
name|getDataSize
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Sibling "
operator|+
name|sibling
operator|+
literal|" has stats: "
operator|+
name|sibling
operator|.
name|getStatistics
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No stats available from: "
operator|+
name|sibling
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// Not using OP stats and this is the first sink in the path, meaning that
comment|// we should use TS stats to infer parallelism
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|sibling
range|:
name|sink
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
name|Set
argument_list|<
name|TableScanOperator
argument_list|>
name|sources
init|=
name|OperatorUtils
operator|.
name|findOperatorsUpstream
argument_list|(
name|sibling
argument_list|,
name|TableScanOperator
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|TableScanOperator
name|source
range|:
name|sources
control|)
block|{
if|if
condition|(
name|source
operator|.
name|getStatistics
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|numberOfBytes
operator|=
name|StatsUtils
operator|.
name|safeAdd
argument_list|(
name|numberOfBytes
argument_list|,
name|source
operator|.
name|getStatistics
argument_list|()
operator|.
name|getDataSize
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table source "
operator|+
name|source
operator|+
literal|" has stats: "
operator|+
name|source
operator|.
name|getStatistics
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No stats available from table source: "
operator|+
name|source
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Gathered stats for sink "
operator|+
name|sink
operator|+
literal|". Total size is "
operator|+
name|numberOfBytes
operator|+
literal|" bytes."
argument_list|)
expr_stmt|;
block|}
comment|// Divide it by 2 so that we can have more reducers
name|long
name|bytesPerReducer
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
name|BYTESPERREDUCER
argument_list|)
operator|/
literal|2
decl_stmt|;
name|int
name|numReducers
init|=
name|Utilities
operator|.
name|estimateReducers
argument_list|(
name|numberOfBytes
argument_list|,
name|bytesPerReducer
argument_list|,
name|maxReducers
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|getSparkMemoryAndCores
argument_list|(
name|context
argument_list|)
expr_stmt|;
if|if
condition|(
name|sparkMemoryAndCores
operator|!=
literal|null
operator|&&
name|sparkMemoryAndCores
operator|.
name|getLeft
argument_list|()
operator|>
literal|0
operator|&&
name|sparkMemoryAndCores
operator|.
name|getRight
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// warn the user if bytes per reducer is much larger than memory per task
if|if
condition|(
operator|(
name|double
operator|)
name|sparkMemoryAndCores
operator|.
name|getLeft
argument_list|()
operator|/
name|bytesPerReducer
operator|<
literal|0.5
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Average load of a reducer is much larger than its available memory. "
operator|+
literal|"Consider decreasing hive.exec.reducers.bytes.per.reducer"
argument_list|)
expr_stmt|;
block|}
comment|// If there are more cores, use the number of cores
name|numReducers
operator|=
name|Math
operator|.
name|max
argument_list|(
name|numReducers
argument_list|,
name|sparkMemoryAndCores
operator|.
name|getRight
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|numReducers
operator|=
name|Math
operator|.
name|min
argument_list|(
name|numReducers
argument_list|,
name|maxReducers
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Set parallelism for reduce sink "
operator|+
name|sink
operator|+
literal|" to: "
operator|+
name|numReducers
operator|+
literal|" (calculated)"
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setNumReducers
argument_list|(
name|numReducers
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Use the maximum parallelism from all parent reduce sinks
name|int
name|numberOfReducers
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ReduceSinkOperator
name|parent
range|:
name|parentSinks
control|)
block|{
name|numberOfReducers
operator|=
name|Math
operator|.
name|max
argument_list|(
name|numberOfReducers
argument_list|,
name|parent
operator|.
name|getConf
argument_list|()
operator|.
name|getNumReducers
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|desc
operator|.
name|setNumReducers
argument_list|(
name|numberOfReducers
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Set parallelism for sink "
operator|+
name|sink
operator|+
literal|" to "
operator|+
name|numberOfReducers
operator|+
literal|" based on its parents"
argument_list|)
expr_stmt|;
block|}
specifier|final
name|Collection
argument_list|<
name|ExprNodeDesc
operator|.
name|ExprNodeDescEqualityWrapper
argument_list|>
name|keyCols
init|=
name|ExprNodeDesc
operator|.
name|ExprNodeDescEqualityWrapper
operator|.
name|transform
argument_list|(
name|desc
operator|.
name|getKeyCols
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Collection
argument_list|<
name|ExprNodeDesc
operator|.
name|ExprNodeDescEqualityWrapper
argument_list|>
name|partCols
init|=
name|ExprNodeDesc
operator|.
name|ExprNodeDescEqualityWrapper
operator|.
name|transform
argument_list|(
name|desc
operator|.
name|getPartitionCols
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyCols
operator|!=
literal|null
operator|&&
name|keyCols
operator|.
name|equals
argument_list|(
name|partCols
argument_list|)
condition|)
block|{
name|desc
operator|.
name|setReducerTraits
argument_list|(
name|EnumSet
operator|.
name|of
argument_list|(
name|UNIFORM
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Number of reducers for sink "
operator|+
name|sink
operator|+
literal|" was already determined to be: "
operator|+
name|desc
operator|.
name|getNumReducers
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
comment|// tests whether the RS needs automatic setting parallelism
specifier|private
name|boolean
name|needSetParallelism
parameter_list|(
name|ReduceSinkOperator
name|reduceSink
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|ReduceSinkDesc
name|desc
init|=
name|reduceSink
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|getNumReducers
argument_list|()
operator|<=
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|desc
operator|.
name|getNumReducers
argument_list|()
operator|==
literal|1
operator|&&
name|desc
operator|.
name|hasOrderBy
argument_list|()
operator|&&
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESAMPLINGFORORDERBY
argument_list|)
operator|&&
operator|!
name|desc
operator|.
name|isDeduplicated
argument_list|()
condition|)
block|{
name|Stack
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|descendants
init|=
operator|new
name|Stack
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|children
init|=
name|reduceSink
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|!=
literal|null
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
name|child
range|:
name|children
control|)
block|{
name|descendants
operator|.
name|push
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
block|}
while|while
condition|(
name|descendants
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|descendant
init|=
name|descendants
operator|.
name|pop
argument_list|()
decl_stmt|;
comment|//If the decendants contains LimitOperator,return false
if|if
condition|(
name|descendant
operator|instanceof
name|LimitOperator
condition|)
block|{
return|return
literal|false
return|;
block|}
name|boolean
name|reachTerminalOperator
init|=
operator|(
name|descendant
operator|instanceof
name|TerminalOperator
operator|)
decl_stmt|;
if|if
condition|(
operator|!
name|reachTerminalOperator
condition|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|childrenOfDescendant
init|=
name|descendant
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
if|if
condition|(
name|childrenOfDescendant
operator|!=
literal|null
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
name|childOfDescendant
range|:
name|childrenOfDescendant
control|)
block|{
name|descendants
operator|.
name|push
argument_list|(
name|childOfDescendant
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|void
name|getSparkMemoryAndCores
parameter_list|(
name|OptimizeSparkProcContext
name|context
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|sparkMemoryAndCores
operator|!=
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolean
argument_list|(
name|SPARK_DYNAMIC_ALLOCATION_ENABLED
argument_list|,
literal|false
argument_list|)
condition|)
block|{
comment|// If dynamic allocation is enabled, numbers for memory and cores are meaningless. So, we don't
comment|// try to get it.
name|sparkMemoryAndCores
operator|=
literal|null
expr_stmt|;
return|return;
block|}
name|SparkSessionManager
name|sparkSessionManager
init|=
literal|null
decl_stmt|;
name|SparkSession
name|sparkSession
init|=
literal|null
decl_stmt|;
try|try
block|{
name|sparkSessionManager
operator|=
name|SparkSessionManagerImpl
operator|.
name|getInstance
argument_list|()
expr_stmt|;
name|sparkSession
operator|=
name|SparkUtilities
operator|.
name|getSparkSession
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|sparkSessionManager
argument_list|)
expr_stmt|;
name|sparkMemoryAndCores
operator|=
name|sparkSession
operator|.
name|getMemoryAndCores
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Failed to get a Hive on Spark session"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get spark memory/core info, reducer parallelism may be inaccurate"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|sparkSession
operator|!=
literal|null
operator|&&
name|sparkSessionManager
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|sparkSessionManager
operator|.
name|returnSession
argument_list|(
name|sparkSession
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to return the session to SessionManager: "
operator|+
name|ex
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

