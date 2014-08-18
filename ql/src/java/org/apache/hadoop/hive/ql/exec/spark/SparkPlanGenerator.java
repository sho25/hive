begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  Licensed to the Apache Software Foundation (ASF) under one  *  or more contributor license agreements.  See the NOTICE file  *  distributed with this work for additional information  *  regarding copyright ownership.  The ASF licenses this file  *  to you under the Apache License, Version 2.0 (the  *  "License"); you may not use this file except in compliance  *  with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
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
name|io
operator|.
name|CombineHiveInputFormat
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
name|plan
operator|.
name|UnionWork
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
name|hive
operator|.
name|shims
operator|.
name|ShimLoader
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
name|BytesWritable
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

begin_class
specifier|public
class|class
name|SparkPlanGenerator
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
name|SparkPlanGenerator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|JavaSparkContext
name|sc
decl_stmt|;
specifier|private
specifier|final
name|JobConf
name|jobConf
decl_stmt|;
specifier|private
name|Context
name|context
decl_stmt|;
specifier|private
name|Path
name|scratchDir
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|SparkTran
argument_list|>
name|unionWorkTrans
init|=
operator|new
name|HashMap
argument_list|<
name|BaseWork
argument_list|,
name|SparkTran
argument_list|>
argument_list|()
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
name|SparkPlan
name|plan
init|=
operator|new
name|SparkPlan
argument_list|()
decl_stmt|;
name|GraphTran
name|trans
init|=
operator|new
name|GraphTran
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|roots
init|=
name|sparkWork
operator|.
name|getRoots
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|roots
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|w
operator|instanceof
name|MapWork
operator|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"The roots in the SparkWork must be MapWork instances!"
argument_list|)
throw|;
block|}
name|MapWork
name|mapWork
init|=
operator|(
name|MapWork
operator|)
name|w
decl_stmt|;
name|SparkTran
name|tran
init|=
name|generate
argument_list|(
name|w
argument_list|)
decl_stmt|;
name|JavaPairRDD
argument_list|<
name|BytesWritable
argument_list|,
name|BytesWritable
argument_list|>
name|input
init|=
name|generateRDD
argument_list|(
name|mapWork
argument_list|)
decl_stmt|;
name|trans
operator|.
name|addTranWithInput
argument_list|(
name|tran
argument_list|,
name|input
argument_list|)
expr_stmt|;
while|while
condition|(
name|sparkWork
operator|.
name|getChildren
argument_list|(
name|w
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|BaseWork
name|child
init|=
name|sparkWork
operator|.
name|getChildren
argument_list|(
name|w
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|child
operator|instanceof
name|ReduceWork
condition|)
block|{
name|SparkEdgeProperty
name|edge
init|=
name|sparkWork
operator|.
name|getEdgeProperty
argument_list|(
name|w
argument_list|,
name|child
argument_list|)
decl_stmt|;
name|SparkShuffler
name|st
init|=
name|generate
argument_list|(
name|edge
argument_list|)
decl_stmt|;
name|ReduceTran
name|rt
init|=
name|generate
argument_list|(
operator|(
name|ReduceWork
operator|)
name|child
argument_list|)
decl_stmt|;
name|rt
operator|.
name|setShuffler
argument_list|(
name|st
argument_list|)
expr_stmt|;
name|rt
operator|.
name|setNumPartitions
argument_list|(
name|edge
operator|.
name|getNumPartitions
argument_list|()
argument_list|)
expr_stmt|;
name|trans
operator|.
name|addTran
argument_list|(
name|rt
argument_list|)
expr_stmt|;
name|trans
operator|.
name|connect
argument_list|(
name|tran
argument_list|,
name|rt
argument_list|)
expr_stmt|;
name|w
operator|=
name|child
expr_stmt|;
name|tran
operator|=
name|rt
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|instanceof
name|UnionWork
condition|)
block|{
if|if
condition|(
name|unionWorkTrans
operator|.
name|get
argument_list|(
name|child
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|trans
operator|.
name|connect
argument_list|(
name|tran
argument_list|,
name|unionWorkTrans
operator|.
name|get
argument_list|(
name|child
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
else|else
block|{
name|SparkTran
name|ut
init|=
name|generate
argument_list|(
operator|(
name|UnionWork
operator|)
name|child
argument_list|)
decl_stmt|;
name|unionWorkTrans
operator|.
name|put
argument_list|(
name|child
argument_list|,
name|ut
argument_list|)
expr_stmt|;
name|trans
operator|.
name|addTran
argument_list|(
name|ut
argument_list|)
expr_stmt|;
name|trans
operator|.
name|connect
argument_list|(
name|tran
argument_list|,
name|ut
argument_list|)
expr_stmt|;
name|w
operator|=
name|child
expr_stmt|;
name|tran
operator|=
name|ut
expr_stmt|;
block|}
block|}
block|}
block|}
name|unionWorkTrans
operator|.
name|clear
argument_list|()
expr_stmt|;
name|plan
operator|.
name|setTran
argument_list|(
name|trans
argument_list|)
expr_stmt|;
return|return
name|plan
return|;
block|}
specifier|private
name|JavaPairRDD
argument_list|<
name|BytesWritable
argument_list|,
name|BytesWritable
argument_list|>
name|generateRDD
parameter_list|(
name|MapWork
name|mapWork
parameter_list|)
throws|throws
name|Exception
block|{
name|JobConf
name|newJobConf
init|=
operator|new
name|JobConf
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
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
name|newJobConf
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
name|newJobConf
argument_list|,
name|inputPaths
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|setMapWork
argument_list|(
name|newJobConf
argument_list|,
name|mapWork
argument_list|,
name|scratchDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Class
name|ifClass
init|=
name|getInputFormat
argument_list|(
name|mapWork
argument_list|)
decl_stmt|;
comment|// The mapper class is expected by the HiveInputFormat.
name|newJobConf
operator|.
name|set
argument_list|(
literal|"mapred.mapper.class"
argument_list|,
name|ExecMapper
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sc
operator|.
name|hadoopRDD
argument_list|(
name|newJobConf
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
return|;
block|}
specifier|private
name|Class
name|getInputFormat
parameter_list|(
name|MapWork
name|mWork
parameter_list|)
throws|throws
name|HiveException
block|{
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
operator|(
name|inpFormat
operator|==
literal|null
operator|)
operator|||
operator|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|inpFormat
argument_list|)
operator|)
condition|)
block|{
name|inpFormat
operator|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getInputFormatClassName
argument_list|()
expr_stmt|;
block|}
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
name|Class
operator|.
name|forName
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
specifier|private
name|SparkTran
name|generate
parameter_list|(
name|BaseWork
name|bw
parameter_list|)
throws|throws
name|Exception
block|{
comment|// initialize stats publisher if necessary
if|if
condition|(
name|bw
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
if|if
condition|(
operator|!
name|statsPublisher
operator|.
name|init
argument_list|(
name|jobConf
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
if|if
condition|(
name|bw
operator|instanceof
name|MapWork
condition|)
block|{
return|return
name|generate
argument_list|(
operator|(
name|MapWork
operator|)
name|bw
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|bw
operator|instanceof
name|ReduceWork
condition|)
block|{
return|return
name|generate
argument_list|(
operator|(
name|ReduceWork
operator|)
name|bw
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Only MapWork and ReduceWork are expected"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|MapTran
name|generate
parameter_list|(
name|MapWork
name|mw
parameter_list|)
throws|throws
name|Exception
block|{
name|JobConf
name|newJobConf
init|=
operator|new
name|JobConf
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|MapTran
name|result
init|=
operator|new
name|MapTran
argument_list|()
decl_stmt|;
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
name|newJobConf
argument_list|,
name|mw
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
name|newJobConf
argument_list|,
name|inputPaths
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|setMapWork
argument_list|(
name|newJobConf
argument_list|,
name|mw
argument_list|,
name|scratchDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|createTmpDirs
argument_list|(
name|newJobConf
argument_list|,
name|mw
argument_list|)
expr_stmt|;
name|newJobConf
operator|.
name|set
argument_list|(
literal|"mapred.mapper.class"
argument_list|,
name|ExecMapper
operator|.
name|class
operator|.
name|getName
argument_list|()
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
name|HiveMapFunction
name|mapFunc
init|=
operator|new
name|HiveMapFunction
argument_list|(
name|confBytes
argument_list|)
decl_stmt|;
name|result
operator|.
name|setMapFunction
argument_list|(
name|mapFunc
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|private
name|SparkShuffler
name|generate
parameter_list|(
name|SparkEdgeProperty
name|edge
parameter_list|)
block|{
if|if
condition|(
name|edge
operator|.
name|isShuffleSort
argument_list|()
condition|)
block|{
return|return
operator|new
name|SortByShuffler
argument_list|()
return|;
block|}
return|return
operator|new
name|GroupByShuffler
argument_list|()
return|;
block|}
specifier|private
name|ReduceTran
name|generate
parameter_list|(
name|ReduceWork
name|rw
parameter_list|)
throws|throws
name|IOException
block|{
name|ReduceTran
name|result
init|=
operator|new
name|ReduceTran
argument_list|()
decl_stmt|;
comment|// Clone jobConf for each ReduceWork so we can have multiple of them
name|JobConf
name|newJobConf
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
name|newJobConf
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
name|Utilities
operator|.
name|setReduceWork
argument_list|(
name|newJobConf
argument_list|,
name|rw
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
name|newJobConf
argument_list|,
name|rw
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
name|HiveReduceFunction
name|redFunc
init|=
operator|new
name|HiveReduceFunction
argument_list|(
name|confBytes
argument_list|)
decl_stmt|;
name|result
operator|.
name|setReduceFunction
argument_list|(
name|redFunc
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|private
name|UnionTran
name|generate
parameter_list|(
name|UnionWork
name|uw
parameter_list|)
block|{
name|UnionTran
name|result
init|=
operator|new
name|UnionTran
argument_list|()
decl_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

