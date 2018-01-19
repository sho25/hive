begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  Licensed to the Apache Software Foundation (ASF) under one  *  or more contributor license agreements.  See the NOTICE file  *  distributed with this work for additional information  *  regarding copyright ownership.  The ASF licenses this file  *  to you under the Apache License, Version 2.0 (the  *  "License"); you may not use this file except in compliance  *  with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|exec
operator|.
name|spark
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|common
operator|.
name|JavaUtils
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
name|io
operator|.
name|merge
operator|.
name|MergeFileMapper
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
name|io
operator|.
name|merge
operator|.
name|MergeFileOutputFormat
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
name|io
operator|.
name|merge
operator|.
name|MergeFileWork
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
name|log
operator|.
name|PerfLogger
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
name|SessionState
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
name|mapred
operator|.
name|FileOutputFormat
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
name|ErrorMsg
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
name|mr
operator|.
name|ExecMapper
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
name|mr
operator|.
name|ExecReducer
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
name|io
operator|.
name|BucketizedHiveInputFormat
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
name|plan
operator|.
name|BaseWork
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
name|MapWork
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
name|ReduceWork
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
name|SparkEdgeProperty
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
name|SparkWork
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
name|StatsCollectionContext
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
name|StatsFactory
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
name|StatsPublisher
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
name|io
operator|.
name|Writable
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
name|io
operator|.
name|WritableComparable
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaPairRDD
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaSparkContext
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
name|base
operator|.
name|Preconditions
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|public
class|class
name|SparkPlanGenerator
block|{
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|SparkPlanGenerator
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|PerfLogger
name|perfLogger
init|=
name|SessionState
operator|.
name|getPerfLogger
argument_list|()
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
name|SparkPlanGenerator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|JavaSparkContext
name|sc
decl_stmt|;
specifier|private
specifier|final
name|JobConf
name|jobConf
decl_stmt|;
specifier|private
specifier|final
name|Context
name|context
decl_stmt|;
specifier|private
specifier|final
name|Path
name|scratchDir
decl_stmt|;
specifier|private
specifier|final
name|SparkReporter
name|sparkReporter
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|BaseWork
argument_list|>
name|cloneToWork
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|SparkTran
argument_list|>
name|workToTranMap
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|SparkTran
argument_list|>
name|workToParentWorkTranMap
decl_stmt|;
comment|// a map from each BaseWork to its cloned JobConf
specifier|private
specifier|final
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|JobConf
argument_list|>
name|workToJobConf
decl_stmt|;
specifier|public
name|SparkPlanGenerator
parameter_list|(
name|JavaSparkContext
name|sc
parameter_list|,
name|Context
name|context
parameter_list|,
name|JobConf
name|jobConf
parameter_list|,
name|Path
name|scratchDir
parameter_list|,
name|SparkReporter
name|sparkReporter
parameter_list|)
block|{
name|this
operator|.
name|sc
operator|=
name|sc
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|jobConf
operator|=
name|jobConf
expr_stmt|;
name|this
operator|.
name|scratchDir
operator|=
name|scratchDir
expr_stmt|;
name|this
operator|.
name|workToTranMap
operator|=
operator|new
name|HashMap
argument_list|<
name|BaseWork
argument_list|,
name|SparkTran
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|workToParentWorkTranMap
operator|=
operator|new
name|HashMap
argument_list|<
name|BaseWork
argument_list|,
name|SparkTran
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|sparkReporter
operator|=
name|sparkReporter
expr_stmt|;
name|this
operator|.
name|workToJobConf
operator|=
operator|new
name|HashMap
argument_list|<
name|BaseWork
argument_list|,
name|JobConf
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|SparkPlan
name|generate
parameter_list|(
name|SparkWork
name|sparkWork
parameter_list|)
throws|throws
name|Exception
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_BUILD_PLAN
argument_list|)
expr_stmt|;
name|SparkPlan
name|sparkPlan
init|=
operator|new
name|SparkPlan
argument_list|()
decl_stmt|;
name|cloneToWork
operator|=
name|sparkWork
operator|.
name|getCloneToWork
argument_list|()
expr_stmt|;
name|workToTranMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|workToParentWorkTranMap
operator|.
name|clear
argument_list|()
expr_stmt|;
try|try
block|{
for|for
control|(
name|BaseWork
name|work
range|:
name|sparkWork
operator|.
name|getAllWork
argument_list|()
control|)
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_CREATE_TRAN
operator|+
name|work
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|SparkTran
name|tran
init|=
name|generate
argument_list|(
name|work
argument_list|,
name|sparkWork
argument_list|)
decl_stmt|;
name|SparkTran
name|parentTran
init|=
name|generateParentTran
argument_list|(
name|sparkPlan
argument_list|,
name|sparkWork
argument_list|,
name|work
argument_list|)
decl_stmt|;
name|sparkPlan
operator|.
name|addTran
argument_list|(
name|tran
argument_list|)
expr_stmt|;
name|sparkPlan
operator|.
name|connect
argument_list|(
name|parentTran
argument_list|,
name|tran
argument_list|)
expr_stmt|;
name|workToTranMap
operator|.
name|put
argument_list|(
name|work
argument_list|,
name|tran
argument_list|)
expr_stmt|;
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_CREATE_TRAN
operator|+
name|work
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
comment|// clear all ThreadLocal cached MapWork/ReduceWork after plan generation
comment|// as this may executed in a pool thread.
name|Utilities
operator|.
name|clearWorkMap
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
block|}
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_BUILD_PLAN
argument_list|)
expr_stmt|;
return|return
name|sparkPlan
return|;
block|}
comment|// Generate (possibly get from a cached result) parent SparkTran
specifier|private
name|SparkTran
name|generateParentTran
parameter_list|(
name|SparkPlan
name|sparkPlan
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|,
name|BaseWork
name|work
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|cloneToWork
operator|.
name|containsKey
argument_list|(
name|work
argument_list|)
condition|)
block|{
name|BaseWork
name|originalWork
init|=
name|cloneToWork
operator|.
name|get
argument_list|(
name|work
argument_list|)
decl_stmt|;
if|if
condition|(
name|workToParentWorkTranMap
operator|.
name|containsKey
argument_list|(
name|originalWork
argument_list|)
condition|)
block|{
return|return
name|workToParentWorkTranMap
operator|.
name|get
argument_list|(
name|originalWork
argument_list|)
return|;
block|}
block|}
name|SparkTran
name|result
decl_stmt|;
if|if
condition|(
name|work
operator|instanceof
name|MapWork
condition|)
block|{
name|result
operator|=
name|generateMapInput
argument_list|(
name|sparkPlan
argument_list|,
operator|(
name|MapWork
operator|)
name|work
argument_list|)
expr_stmt|;
name|sparkPlan
operator|.
name|addTran
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|work
operator|instanceof
name|ReduceWork
condition|)
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|parentWorks
init|=
name|sparkWork
operator|.
name|getParents
argument_list|(
name|work
argument_list|)
decl_stmt|;
name|result
operator|=
name|generate
argument_list|(
name|sparkPlan
argument_list|,
name|sparkWork
operator|.
name|getEdgeProperty
argument_list|(
name|parentWorks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|work
argument_list|)
argument_list|,
name|cloneToWork
operator|.
name|containsKey
argument_list|(
name|work
argument_list|)
argument_list|)
expr_stmt|;
name|sparkPlan
operator|.
name|addTran
argument_list|(
name|result
argument_list|)
expr_stmt|;
for|for
control|(
name|BaseWork
name|parentWork
range|:
name|parentWorks
control|)
block|{
name|sparkPlan
operator|.
name|connect
argument_list|(
name|workToTranMap
operator|.
name|get
argument_list|(
name|parentWork
argument_list|)
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"AssertionError: expected either MapWork or ReduceWork, "
operator|+
literal|"but found "
operator|+
name|work
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|cloneToWork
operator|.
name|containsKey
argument_list|(
name|work
argument_list|)
condition|)
block|{
name|workToParentWorkTranMap
operator|.
name|put
argument_list|(
name|cloneToWork
operator|.
name|get
argument_list|(
name|work
argument_list|)
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|Class
argument_list|<
name|?
argument_list|>
name|getInputFormat
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|MapWork
name|mWork
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// MergeFileWork is sub-class of MapWork, we don't need to distinguish here
if|if
condition|(
name|mWork
operator|.
name|getInputformat
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|HiveConf
operator|.
name|setVar
argument_list|(
name|jobConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEINPUTFORMAT
argument_list|,
name|mWork
operator|.
name|getInputformat
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|inpFormat
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|jobConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEINPUTFORMAT
argument_list|)
decl_stmt|;
if|if
condition|(
name|mWork
operator|.
name|isUseBucketizedHiveInputFormat
argument_list|()
condition|)
block|{
name|inpFormat
operator|=
name|BucketizedHiveInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
name|Class
name|inputFormatClass
decl_stmt|;
try|try
block|{
name|inputFormatClass
operator|=
name|JavaUtils
operator|.
name|loadClass
argument_list|(
name|inpFormat
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|String
name|message
init|=
literal|"Failed to load specified input format class:"
operator|+
name|inpFormat
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|message
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|message
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|inputFormatClass
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|MapInput
name|generateMapInput
parameter_list|(
name|SparkPlan
name|sparkPlan
parameter_list|,
name|MapWork
name|mapWork
parameter_list|)
throws|throws
name|Exception
block|{
name|JobConf
name|jobConf
init|=
name|cloneJobConf
argument_list|(
name|mapWork
argument_list|)
decl_stmt|;
name|Class
name|ifClass
init|=
name|getInputFormat
argument_list|(
name|jobConf
argument_list|,
name|mapWork
argument_list|)
decl_stmt|;
name|JavaPairRDD
argument_list|<
name|WritableComparable
argument_list|,
name|Writable
argument_list|>
name|hadoopRDD
decl_stmt|;
if|if
condition|(
name|mapWork
operator|.
name|getNumMapTasks
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|jobConf
operator|.
name|setNumMapTasks
argument_list|(
name|mapWork
operator|.
name|getNumMapTasks
argument_list|()
argument_list|)
expr_stmt|;
name|hadoopRDD
operator|=
name|sc
operator|.
name|hadoopRDD
argument_list|(
name|jobConf
argument_list|,
name|ifClass
argument_list|,
name|WritableComparable
operator|.
name|class
argument_list|,
name|Writable
operator|.
name|class
argument_list|,
name|mapWork
operator|.
name|getNumMapTasks
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hadoopRDD
operator|=
name|sc
operator|.
name|hadoopRDD
argument_list|(
name|jobConf
argument_list|,
name|ifClass
argument_list|,
name|WritableComparable
operator|.
name|class
argument_list|,
name|Writable
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|// Caching is disabled for MapInput due to HIVE-8920
name|MapInput
name|result
init|=
operator|new
name|MapInput
argument_list|(
name|sparkPlan
argument_list|,
name|hadoopRDD
argument_list|,
literal|false
comment|/*cloneToWork.containsKey(mapWork)*/
argument_list|)
decl_stmt|;
return|return
name|result
return|;
block|}
specifier|private
name|ShuffleTran
name|generate
parameter_list|(
name|SparkPlan
name|sparkPlan
parameter_list|,
name|SparkEdgeProperty
name|edge
parameter_list|,
name|boolean
name|toCache
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
operator|!
name|edge
operator|.
name|isShuffleNone
argument_list|()
argument_list|,
literal|"AssertionError: SHUFFLE_NONE should only be used for UnionWork."
argument_list|)
expr_stmt|;
name|SparkShuffler
name|shuffler
decl_stmt|;
if|if
condition|(
name|edge
operator|.
name|isMRShuffle
argument_list|()
condition|)
block|{
name|shuffler
operator|=
operator|new
name|SortByShuffler
argument_list|(
literal|false
argument_list|,
name|sparkPlan
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|edge
operator|.
name|isShuffleSort
argument_list|()
condition|)
block|{
name|shuffler
operator|=
operator|new
name|SortByShuffler
argument_list|(
literal|true
argument_list|,
name|sparkPlan
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|shuffler
operator|=
operator|new
name|GroupByShuffler
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|ShuffleTran
argument_list|(
name|sparkPlan
argument_list|,
name|shuffler
argument_list|,
name|edge
operator|.
name|getNumPartitions
argument_list|()
argument_list|,
name|toCache
argument_list|)
return|;
block|}
specifier|private
name|SparkTran
name|generate
parameter_list|(
name|BaseWork
name|work
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
throws|throws
name|Exception
block|{
name|initStatsPublisher
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|JobConf
name|newJobConf
init|=
name|cloneJobConf
argument_list|(
name|work
argument_list|)
decl_stmt|;
name|checkSpecs
argument_list|(
name|work
argument_list|,
name|newJobConf
argument_list|)
expr_stmt|;
name|byte
index|[]
name|confBytes
init|=
name|KryoSerializer
operator|.
name|serializeJobConf
argument_list|(
name|newJobConf
argument_list|)
decl_stmt|;
name|boolean
name|caching
init|=
name|isCachingWork
argument_list|(
name|work
argument_list|,
name|sparkWork
argument_list|)
decl_stmt|;
if|if
condition|(
name|work
operator|instanceof
name|MapWork
condition|)
block|{
comment|// Create tmp dir for MergeFileWork
if|if
condition|(
name|work
operator|instanceof
name|MergeFileWork
condition|)
block|{
name|Path
name|outputPath
init|=
operator|(
operator|(
name|MergeFileWork
operator|)
name|work
operator|)
operator|.
name|getOutputDir
argument_list|()
decl_stmt|;
name|Path
name|tempOutPath
init|=
name|Utilities
operator|.
name|toTempPath
argument_list|(
name|outputPath
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|outputPath
operator|.
name|getFileSystem
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|tempOutPath
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|tempOutPath
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Can't make path "
operator|+
name|outputPath
operator|+
literal|" : "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|MapTran
name|mapTran
init|=
operator|new
name|MapTran
argument_list|(
name|caching
argument_list|)
decl_stmt|;
name|HiveMapFunction
name|mapFunc
init|=
operator|new
name|HiveMapFunction
argument_list|(
name|confBytes
argument_list|,
name|sparkReporter
argument_list|)
decl_stmt|;
name|mapTran
operator|.
name|setMapFunction
argument_list|(
name|mapFunc
argument_list|)
expr_stmt|;
return|return
name|mapTran
return|;
block|}
elseif|else
if|if
condition|(
name|work
operator|instanceof
name|ReduceWork
condition|)
block|{
name|ReduceTran
name|reduceTran
init|=
operator|new
name|ReduceTran
argument_list|(
name|caching
argument_list|)
decl_stmt|;
name|HiveReduceFunction
name|reduceFunc
init|=
operator|new
name|HiveReduceFunction
argument_list|(
name|confBytes
argument_list|,
name|sparkReporter
argument_list|)
decl_stmt|;
name|reduceTran
operator|.
name|setReduceFunction
argument_list|(
name|reduceFunc
argument_list|)
expr_stmt|;
return|return
name|reduceTran
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"AssertionError: expected either MapWork or ReduceWork, "
operator|+
literal|"but found "
operator|+
name|work
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
name|boolean
name|isCachingWork
parameter_list|(
name|BaseWork
name|work
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
block|{
name|boolean
name|caching
init|=
literal|true
decl_stmt|;
name|List
argument_list|<
name|BaseWork
argument_list|>
name|children
init|=
name|sparkWork
operator|.
name|getChildren
argument_list|(
name|work
argument_list|)
decl_stmt|;
if|if
condition|(
name|children
operator|.
name|size
argument_list|()
operator|<
literal|2
condition|)
block|{
name|caching
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
comment|// do not cache this if its child RDD is intend to be cached.
for|for
control|(
name|BaseWork
name|child
range|:
name|children
control|)
block|{
if|if
condition|(
name|cloneToWork
operator|.
name|containsKey
argument_list|(
name|child
argument_list|)
condition|)
block|{
name|caching
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
return|return
name|caching
return|;
block|}
specifier|private
name|void
name|checkSpecs
parameter_list|(
name|BaseWork
name|work
parameter_list|,
name|JobConf
name|jc
parameter_list|)
throws|throws
name|Exception
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opList
init|=
name|work
operator|.
name|getAllOperators
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|opList
control|)
block|{
if|if
condition|(
name|op
operator|instanceof
name|FileSinkOperator
condition|)
block|{
operator|(
operator|(
name|FileSinkOperator
operator|)
name|op
operator|)
operator|.
name|checkOutputSpecs
argument_list|(
literal|null
argument_list|,
name|jc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
specifier|private
name|JobConf
name|cloneJobConf
parameter_list|(
name|BaseWork
name|work
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|workToJobConf
operator|.
name|containsKey
argument_list|(
name|work
argument_list|)
condition|)
block|{
return|return
name|workToJobConf
operator|.
name|get
argument_list|(
name|work
argument_list|)
return|;
block|}
name|JobConf
name|cloned
init|=
operator|new
name|JobConf
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
comment|// Make sure we'll use a different plan path from the original one
name|HiveConf
operator|.
name|setVar
argument_list|(
name|cloned
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PLAN
argument_list|,
literal|""
argument_list|)
expr_stmt|;
try|try
block|{
name|cloned
operator|.
name|setPartitionerClass
argument_list|(
name|JavaUtils
operator|.
name|loadClass
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|cloned
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEPARTITIONER
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Could not find partitioner class: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|" which is specified by: "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEPARTITIONER
operator|.
name|varname
decl_stmt|;
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|work
operator|instanceof
name|MapWork
condition|)
block|{
name|MapWork
name|mapWork
init|=
operator|(
name|MapWork
operator|)
name|work
decl_stmt|;
name|cloned
operator|.
name|setBoolean
argument_list|(
literal|"mapred.task.is.map"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|inputPaths
init|=
name|Utilities
operator|.
name|getInputPaths
argument_list|(
name|cloned
argument_list|,
name|mapWork
argument_list|,
name|scratchDir
argument_list|,
name|context
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|setInputPaths
argument_list|(
name|cloned
argument_list|,
name|inputPaths
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|setMapWork
argument_list|(
name|cloned
argument_list|,
name|mapWork
argument_list|,
name|scratchDir
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|createTmpDirs
argument_list|(
name|cloned
argument_list|,
name|mapWork
argument_list|)
expr_stmt|;
if|if
condition|(
name|work
operator|instanceof
name|MergeFileWork
condition|)
block|{
name|MergeFileWork
name|mergeFileWork
init|=
operator|(
name|MergeFileWork
operator|)
name|work
decl_stmt|;
name|cloned
operator|.
name|set
argument_list|(
name|Utilities
operator|.
name|MAPRED_MAPPER_CLASS
argument_list|,
name|MergeFileMapper
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|cloned
operator|.
name|set
argument_list|(
literal|"mapred.input.format.class"
argument_list|,
name|mergeFileWork
operator|.
name|getInputformat
argument_list|()
argument_list|)
expr_stmt|;
name|cloned
operator|.
name|setClass
argument_list|(
literal|"mapred.output.format.class"
argument_list|,
name|MergeFileOutputFormat
operator|.
name|class
argument_list|,
name|FileOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cloned
operator|.
name|set
argument_list|(
name|Utilities
operator|.
name|MAPRED_MAPPER_CLASS
argument_list|,
name|ExecMapper
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
name|mapWork
operator|.
name|getMaxSplitSize
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|HiveConf
operator|.
name|setLongVar
argument_list|(
name|cloned
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAPREDMAXSPLITSIZE
argument_list|,
name|mapWork
operator|.
name|getMaxSplitSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|mapWork
operator|.
name|getMinSplitSize
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|HiveConf
operator|.
name|setLongVar
argument_list|(
name|cloned
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAPREDMINSPLITSIZE
argument_list|,
name|mapWork
operator|.
name|getMinSplitSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|mapWork
operator|.
name|getMinSplitSizePerNode
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|HiveConf
operator|.
name|setLongVar
argument_list|(
name|cloned
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAPREDMINSPLITSIZEPERNODE
argument_list|,
name|mapWork
operator|.
name|getMinSplitSizePerNode
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|mapWork
operator|.
name|getMinSplitSizePerRack
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|HiveConf
operator|.
name|setLongVar
argument_list|(
name|cloned
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAPREDMINSPLITSIZEPERRACK
argument_list|,
name|mapWork
operator|.
name|getMinSplitSizePerRack
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// remember the JobConf cloned for each MapWork, so we won't clone for it again
name|workToJobConf
operator|.
name|put
argument_list|(
name|work
argument_list|,
name|cloned
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|work
operator|instanceof
name|ReduceWork
condition|)
block|{
name|cloned
operator|.
name|setBoolean
argument_list|(
literal|"mapred.task.is.map"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|setReduceWork
argument_list|(
name|cloned
argument_list|,
operator|(
name|ReduceWork
operator|)
name|work
argument_list|,
name|scratchDir
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|createTmpDirs
argument_list|(
name|cloned
argument_list|,
operator|(
name|ReduceWork
operator|)
name|work
argument_list|)
expr_stmt|;
name|cloned
operator|.
name|set
argument_list|(
name|Utilities
operator|.
name|MAPRED_REDUCER_CLASS
argument_list|,
name|ExecReducer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|cloned
return|;
block|}
specifier|private
name|void
name|initStatsPublisher
parameter_list|(
name|BaseWork
name|work
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// initialize stats publisher if necessary
if|if
condition|(
name|work
operator|.
name|isGatheringStats
argument_list|()
condition|)
block|{
name|StatsPublisher
name|statsPublisher
decl_stmt|;
name|StatsFactory
name|factory
init|=
name|StatsFactory
operator|.
name|newFactory
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
if|if
condition|(
name|factory
operator|!=
literal|null
condition|)
block|{
name|statsPublisher
operator|=
name|factory
operator|.
name|getStatsPublisher
argument_list|()
expr_stmt|;
name|StatsCollectionContext
name|sc
init|=
operator|new
name|StatsCollectionContext
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|sc
operator|.
name|setStatsTmpDirs
argument_list|(
name|Utilities
operator|.
name|getStatsTmpDirs
argument_list|(
name|work
argument_list|,
name|jobConf
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|statsPublisher
operator|.
name|init
argument_list|(
name|sc
argument_list|)
condition|)
block|{
comment|// creating stats table if not exists
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|jobConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_STATS_RELIABLE
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|STATSPUBLISHER_INITIALIZATION_ERROR
operator|.
name|getErrorCodedMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

