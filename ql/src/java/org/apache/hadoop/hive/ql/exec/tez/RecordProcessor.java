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
name|exec
operator|.
name|tez
package|;
end_package

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
name|Maps
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
name|ql
operator|.
name|exec
operator|.
name|ObjectCache
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
name|tez
operator|.
name|TezProcessor
operator|.
name|TezKVOutputCollector
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
name|hadoop
operator|.
name|mapred
operator|.
name|OutputCollector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|mapreduce
operator|.
name|processor
operator|.
name|MRTaskReporter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|LogicalInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|LogicalOutput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|ProcessorContext
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryMXBean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLClassLoader
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
name|Arrays
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
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_comment
comment|/**  * Process input from tez LogicalInput and write output  * It has different subclasses for map and reduce processing  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|RecordProcessor
block|{
specifier|protected
name|JobConf
name|jconf
decl_stmt|;
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalInput
argument_list|>
name|inputs
decl_stmt|;
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalOutput
argument_list|>
name|outputs
decl_stmt|;
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|OutputCollector
argument_list|>
name|outMap
decl_stmt|;
specifier|protected
name|ProcessorContext
name|processorContext
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Log
name|l4j
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RecordProcessor
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// used to log memory usage periodically
specifier|protected
name|boolean
name|isLogInfoEnabled
init|=
literal|false
decl_stmt|;
specifier|protected
name|boolean
name|isLogTraceEnabled
init|=
literal|false
decl_stmt|;
specifier|protected
name|MRTaskReporter
name|reporter
decl_stmt|;
specifier|protected
name|PerfLogger
name|perfLogger
init|=
name|PerfLogger
operator|.
name|getPerfLogger
argument_list|()
decl_stmt|;
specifier|protected
name|String
name|CLASS_NAME
init|=
name|RecordProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|/**    * Common initialization code for RecordProcessors    * @param jconf    * @param processorContext the {@link ProcessorContext}    * @param mrReporter    * @param inputs map of Input names to {@link LogicalInput}s    * @param outputs map of Output names to {@link LogicalOutput}s    * @throws Exception    */
name|void
name|init
parameter_list|(
name|JobConf
name|jconf
parameter_list|,
name|ProcessorContext
name|processorContext
parameter_list|,
name|MRTaskReporter
name|mrReporter
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalInput
argument_list|>
name|inputs
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalOutput
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|jconf
operator|=
name|jconf
expr_stmt|;
name|this
operator|.
name|reporter
operator|=
name|mrReporter
expr_stmt|;
name|this
operator|.
name|inputs
operator|=
name|inputs
expr_stmt|;
name|this
operator|.
name|outputs
operator|=
name|outputs
expr_stmt|;
name|this
operator|.
name|processorContext
operator|=
name|processorContext
expr_stmt|;
name|isLogInfoEnabled
operator|=
name|l4j
operator|.
name|isInfoEnabled
argument_list|()
expr_stmt|;
name|isLogTraceEnabled
operator|=
name|l4j
operator|.
name|isTraceEnabled
argument_list|()
expr_stmt|;
comment|//log classpaths
try|try
block|{
if|if
condition|(
name|l4j
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|l4j
operator|.
name|debug
argument_list|(
literal|"conf classpath = "
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
operator|(
operator|(
name|URLClassLoader
operator|)
name|jconf
operator|.
name|getClassLoader
argument_list|()
operator|)
operator|.
name|getURLs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
literal|"thread classpath = "
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
operator|(
operator|(
name|URLClassLoader
operator|)
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
operator|)
operator|.
name|getURLs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|l4j
operator|.
name|info
argument_list|(
literal|"cannot get classpath: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * start processing the inputs and writing output    * @throws Exception    */
specifier|abstract
name|void
name|run
parameter_list|()
throws|throws
name|Exception
function_decl|;
specifier|abstract
name|void
name|close
parameter_list|()
function_decl|;
specifier|protected
name|void
name|createOutputMap
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|outMap
operator|==
literal|null
argument_list|,
literal|"Outputs should only be setup once"
argument_list|)
expr_stmt|;
name|outMap
operator|=
name|Maps
operator|.
name|newHashMap
argument_list|()
expr_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|LogicalOutput
argument_list|>
name|entry
range|:
name|outputs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|TezKVOutputCollector
name|collector
init|=
operator|new
name|TezKVOutputCollector
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|outMap
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|BaseWork
argument_list|>
name|getMergeWorkList
parameter_list|(
specifier|final
name|JobConf
name|jconf
parameter_list|,
name|String
name|key
parameter_list|,
name|String
name|queryId
parameter_list|,
name|ObjectCache
name|cache
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|cacheKeys
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
name|prefixes
init|=
name|jconf
operator|.
name|get
argument_list|(
name|DagUtils
operator|.
name|TEZ_MERGE_WORK_FILE_PREFIXES
argument_list|)
decl_stmt|;
if|if
condition|(
name|prefixes
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|mergeWorkList
init|=
operator|new
name|ArrayList
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|String
name|prefix
range|:
name|prefixes
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
if|if
condition|(
name|prefix
operator|==
literal|null
operator|||
name|prefix
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|key
operator|=
name|queryId
operator|+
name|prefix
expr_stmt|;
name|cacheKeys
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|mergeWorkList
operator|.
name|add
argument_list|(
operator|(
name|BaseWork
operator|)
name|cache
operator|.
name|retrieve
argument_list|(
name|key
argument_list|,
operator|new
name|Callable
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|call
parameter_list|()
block|{
return|return
name|Utilities
operator|.
name|getMergeWork
argument_list|(
name|jconf
argument_list|,
name|prefix
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|mergeWorkList
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

