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
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
import|;
end_import

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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|ExplainTask
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
name|parse
operator|.
name|ExplainConfiguration
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
name|ExplainWork
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
name|SparkContext
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
name|util
operator|.
name|CallSite
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
name|io
operator|.
name|HiveKey
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
name|SparkPlan
block|{
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|SparkPlan
operator|.
name|class
operator|.
name|getName
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
name|SparkPlan
operator|.
name|class
argument_list|)
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
specifier|final
name|Set
argument_list|<
name|SparkTran
argument_list|>
name|rootTrans
init|=
operator|new
name|HashSet
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|SparkTran
argument_list|>
name|leafTrans
init|=
operator|new
name|HashSet
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|SparkTran
argument_list|,
name|List
argument_list|<
name|SparkTran
argument_list|>
argument_list|>
name|transGraph
init|=
operator|new
name|HashMap
argument_list|<
name|SparkTran
argument_list|,
name|List
argument_list|<
name|SparkTran
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|SparkTran
argument_list|,
name|List
argument_list|<
name|SparkTran
argument_list|>
argument_list|>
name|invertedTransGraph
init|=
operator|new
name|HashMap
argument_list|<
name|SparkTran
argument_list|,
name|List
argument_list|<
name|SparkTran
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|Integer
argument_list|>
name|cachedRDDIds
init|=
operator|new
name|HashSet
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|JobConf
name|jobConf
decl_stmt|;
specifier|private
specifier|final
name|SparkContext
name|sc
decl_stmt|;
name|SparkPlan
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|SparkContext
name|sc
parameter_list|)
block|{
name|this
operator|.
name|jobConf
operator|=
name|jobConf
expr_stmt|;
name|this
operator|.
name|sc
operator|=
name|sc
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|generateGraph
parameter_list|()
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_BUILD_RDD_GRAPH
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|SparkTran
argument_list|,
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
name|tranToOutputRDDMap
init|=
operator|new
name|HashMap
argument_list|<
name|SparkTran
argument_list|,
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|SparkTran
name|tran
range|:
name|getAllTrans
argument_list|()
control|)
block|{
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|rdd
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|SparkTran
argument_list|>
name|parents
init|=
name|getParents
argument_list|(
name|tran
argument_list|)
decl_stmt|;
if|if
condition|(
name|parents
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// Root tran, it must be MapInput
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|tran
operator|instanceof
name|MapInput
argument_list|,
literal|"AssertionError: tran must be an instance of MapInput"
argument_list|)
expr_stmt|;
name|sc
operator|.
name|setCallSite
argument_list|(
name|CallSite
operator|.
name|apply
argument_list|(
name|tran
operator|.
name|getName
argument_list|()
argument_list|,
name|getLongFormCallSite
argument_list|(
name|tran
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|rdd
operator|=
name|tran
operator|.
name|transform
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|SparkTran
name|parent
range|:
name|parents
control|)
block|{
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|prevRDD
init|=
name|tranToOutputRDDMap
operator|.
name|get
argument_list|(
name|parent
argument_list|)
decl_stmt|;
if|if
condition|(
name|rdd
operator|==
literal|null
condition|)
block|{
name|rdd
operator|=
name|prevRDD
expr_stmt|;
block|}
else|else
block|{
name|sc
operator|.
name|setCallSite
argument_list|(
name|CallSite
operator|.
name|apply
argument_list|(
literal|"UnionRDD ("
operator|+
name|rdd
operator|.
name|name
argument_list|()
operator|+
literal|", "
operator|+
name|prevRDD
operator|.
name|name
argument_list|()
operator|+
literal|")"
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|rdd
operator|=
name|rdd
operator|.
name|union
argument_list|(
name|prevRDD
argument_list|)
expr_stmt|;
name|rdd
operator|.
name|setName
argument_list|(
literal|"UnionRDD ("
operator|+
name|rdd
operator|.
name|getNumPartitions
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
name|sc
operator|.
name|setCallSite
argument_list|(
name|CallSite
operator|.
name|apply
argument_list|(
name|tran
operator|.
name|getName
argument_list|()
argument_list|,
name|getLongFormCallSite
argument_list|(
name|tran
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|rdd
operator|=
name|tran
operator|.
name|transform
argument_list|(
name|rdd
argument_list|)
expr_stmt|;
block|}
name|tranToOutputRDDMap
operator|.
name|put
argument_list|(
name|tran
argument_list|,
name|rdd
argument_list|)
expr_stmt|;
block|}
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|finalRDD
init|=
literal|null
decl_stmt|;
for|for
control|(
name|SparkTran
name|leafTran
range|:
name|leafTrans
control|)
block|{
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|rdd
init|=
name|tranToOutputRDDMap
operator|.
name|get
argument_list|(
name|leafTran
argument_list|)
decl_stmt|;
if|if
condition|(
name|finalRDD
operator|==
literal|null
condition|)
block|{
name|finalRDD
operator|=
name|rdd
expr_stmt|;
block|}
else|else
block|{
name|sc
operator|.
name|setCallSite
argument_list|(
name|CallSite
operator|.
name|apply
argument_list|(
literal|"UnionRDD ("
operator|+
name|rdd
operator|.
name|name
argument_list|()
operator|+
literal|", "
operator|+
name|finalRDD
operator|.
name|name
argument_list|()
operator|+
literal|")"
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|finalRDD
operator|=
name|finalRDD
operator|.
name|union
argument_list|(
name|rdd
argument_list|)
expr_stmt|;
name|finalRDD
operator|.
name|setName
argument_list|(
literal|"UnionRDD ("
operator|+
name|finalRDD
operator|.
name|getNumPartitions
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_BUILD_RDD_GRAPH
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\nSpark RDD Graph:\n\n"
operator|+
name|finalRDD
operator|.
name|toDebugString
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
return|return
name|finalRDD
return|;
block|}
comment|/**    * Takes a {@link SparkTran} object that creates the longForm for the RDD's {@link CallSite}.    * It does this my creating an {@link ExplainTask} and running it over the    * {@link SparkTran#getBaseWork()} object. The explain output is serialized to the string,    * which is logged and returned. If any errors are encountered while creating the explain plan,    * an error message is simply logged, but no {@link Exception} is thrown.    *    * @param tran the {@link SparkTran} to create the long call site for    *    * @return a {@link String} containing the explain plan for the given {@link SparkTran}    */
specifier|private
name|String
name|getLongFormCallSite
parameter_list|(
name|SparkTran
name|tran
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|jobConf
operator|.
name|getBoolean
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SPARK_LOG_EXPLAIN_WEBUI
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SPARK_LOG_EXPLAIN_WEBUI
operator|.
name|defaultBoolVal
argument_list|)
condition|)
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_CREATE_EXPLAIN_PLAN
operator|+
name|tran
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|ExplainWork
name|explainWork
init|=
operator|new
name|ExplainWork
argument_list|()
decl_stmt|;
name|explainWork
operator|.
name|setConfig
argument_list|(
operator|new
name|ExplainConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|ExplainTask
name|explainTask
init|=
operator|new
name|ExplainTask
argument_list|()
decl_stmt|;
name|explainTask
operator|.
name|setWork
argument_list|(
name|explainWork
argument_list|)
expr_stmt|;
name|String
name|explainOutput
init|=
literal|""
decl_stmt|;
try|try
block|{
name|ByteArrayOutputStream
name|outputStream
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|explainTask
operator|.
name|outputPlan
argument_list|(
name|tran
operator|.
name|getBaseWork
argument_list|()
argument_list|,
operator|new
name|PrintStream
argument_list|(
name|outputStream
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|,
name|this
operator|.
name|jobConf
operator|.
name|getBoolean
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_IN_TEST
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_IN_TEST
operator|.
name|defaultBoolVal
argument_list|)
argument_list|)
expr_stmt|;
name|explainOutput
operator|=
name|StringUtils
operator|.
name|abbreviate
argument_list|(
name|tran
operator|.
name|getName
argument_list|()
operator|+
literal|" Explain Plan:\n\n"
operator|+
name|outputStream
operator|.
name|toString
argument_list|()
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|explainOutput
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error while generating explain plan for "
operator|+
name|tran
operator|.
name|getName
argument_list|()
argument_list|,
name|e
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
name|SPARK_CREATE_EXPLAIN_PLAN
operator|+
name|tran
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|explainOutput
return|;
block|}
return|return
literal|""
return|;
block|}
specifier|public
name|void
name|addTran
parameter_list|(
name|SparkTran
name|tran
parameter_list|)
block|{
name|rootTrans
operator|.
name|add
argument_list|(
name|tran
argument_list|)
expr_stmt|;
name|leafTrans
operator|.
name|add
argument_list|(
name|tran
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addCachedRDDId
parameter_list|(
name|int
name|rddId
parameter_list|)
block|{
name|cachedRDDIds
operator|.
name|add
argument_list|(
name|rddId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|Integer
argument_list|>
name|getCachedRDDIds
parameter_list|()
block|{
return|return
name|cachedRDDIds
return|;
block|}
comment|/**    * This method returns a topologically sorted list of SparkTran.    */
specifier|private
name|List
argument_list|<
name|SparkTran
argument_list|>
name|getAllTrans
parameter_list|()
block|{
name|List
argument_list|<
name|SparkTran
argument_list|>
name|result
init|=
operator|new
name|LinkedList
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|SparkTran
argument_list|>
name|seen
init|=
operator|new
name|HashSet
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|SparkTran
name|leaf
range|:
name|leafTrans
control|)
block|{
comment|// make sure all leaves are visited at least once
name|visit
argument_list|(
name|leaf
argument_list|,
name|seen
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
name|void
name|visit
parameter_list|(
name|SparkTran
name|child
parameter_list|,
name|Set
argument_list|<
name|SparkTran
argument_list|>
name|seen
parameter_list|,
name|List
argument_list|<
name|SparkTran
argument_list|>
name|result
parameter_list|)
block|{
if|if
condition|(
name|seen
operator|.
name|contains
argument_list|(
name|child
argument_list|)
condition|)
block|{
comment|// don't visit multiple times
return|return;
block|}
name|seen
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
for|for
control|(
name|SparkTran
name|parent
range|:
name|getParents
argument_list|(
name|child
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|seen
operator|.
name|contains
argument_list|(
name|parent
argument_list|)
condition|)
block|{
name|visit
argument_list|(
name|parent
argument_list|,
name|seen
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
name|result
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
comment|/**    * Connects the two SparkTrans in the graph.  Does not allow multiple connections    * between the same pair of SparkTrans.    * @param parent    * @param child    */
specifier|public
name|void
name|connect
parameter_list|(
name|SparkTran
name|parent
parameter_list|,
name|SparkTran
name|child
parameter_list|)
block|{
name|rootTrans
operator|.
name|remove
argument_list|(
name|child
argument_list|)
expr_stmt|;
name|leafTrans
operator|.
name|remove
argument_list|(
name|parent
argument_list|)
expr_stmt|;
if|if
condition|(
name|transGraph
operator|.
name|get
argument_list|(
name|parent
argument_list|)
operator|==
literal|null
condition|)
block|{
name|transGraph
operator|.
name|put
argument_list|(
name|parent
argument_list|,
operator|new
name|LinkedList
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|invertedTransGraph
operator|.
name|get
argument_list|(
name|child
argument_list|)
operator|==
literal|null
condition|)
block|{
name|invertedTransGraph
operator|.
name|put
argument_list|(
name|child
argument_list|,
operator|new
name|LinkedList
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|transGraph
operator|.
name|get
argument_list|(
name|parent
argument_list|)
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
name|invertedTransGraph
operator|.
name|get
argument_list|(
name|child
argument_list|)
operator|.
name|add
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|SparkTran
argument_list|>
name|getParents
parameter_list|(
name|SparkTran
name|tran
parameter_list|)
block|{
if|if
condition|(
operator|!
name|invertedTransGraph
operator|.
name|containsKey
argument_list|(
name|tran
argument_list|)
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
return|;
block|}
return|return
name|invertedTransGraph
operator|.
name|get
argument_list|(
name|tran
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|SparkTran
argument_list|>
name|getChildren
parameter_list|(
name|SparkTran
name|tran
parameter_list|)
block|{
if|if
condition|(
operator|!
name|transGraph
operator|.
name|containsKey
argument_list|(
name|tran
argument_list|)
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
return|;
block|}
return|return
name|transGraph
operator|.
name|get
argument_list|(
name|tran
argument_list|)
return|;
block|}
block|}
end_class

end_unit

