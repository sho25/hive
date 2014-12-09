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
name|Stack
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
name|scala
operator|.
name|Tuple2
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
name|NodeProcessor
block|{
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
name|SetSparkReducerParallelism
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// Spark memory per task, and total number of cores
specifier|private
name|Tuple2
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
name|sparkMemoryAndCores
decl_stmt|;
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
name|desc
operator|.
name|getNumReducers
argument_list|()
operator|<=
literal|0
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
name|long
name|numberOfBytes
init|=
literal|0
decl_stmt|;
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
operator|+=
name|sibling
operator|.
name|getStatistics
argument_list|()
operator|.
name|getDataSize
argument_list|()
expr_stmt|;
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
if|if
condition|(
name|sparkMemoryAndCores
operator|==
literal|null
condition|)
block|{
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
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Failed to get spark memory/core info"
argument_list|,
name|e
argument_list|)
throw|;
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
literal|"Failed to return the session to SessionManager"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// Divide it by 2 so that we can have more reducers
name|long
name|bytesPerReducer
init|=
name|sparkMemoryAndCores
operator|.
name|_1
operator|.
name|longValue
argument_list|()
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
comment|// If there are more cores, use the number of cores
name|int
name|cores
init|=
name|sparkMemoryAndCores
operator|.
name|_2
operator|.
name|intValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|numReducers
operator|<
name|cores
condition|)
block|{
name|numReducers
operator|=
name|cores
expr_stmt|;
block|}
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
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Number of reducers determined to be: "
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
block|}
end_class

end_unit

