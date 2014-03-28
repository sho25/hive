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
name|text
operator|.
name|NumberFormat
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
name|Map
operator|.
name|Entry
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
name|conf
operator|.
name|Configuration
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
name|common
operator|.
name|TezUtils
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
name|input
operator|.
name|MRInputLegacy
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
name|Event
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
name|LogicalIOProcessor
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
name|TezProcessorContext
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
name|library
operator|.
name|api
operator|.
name|KeyValueWriter
import|;
end_import

begin_comment
comment|/**  * Hive processor for Tez that forms the vertices in Tez and processes the data.  * Does what ExecMapper and ExecReducer does for hive in MR framework.  */
end_comment

begin_class
specifier|public
class|class
name|TezProcessor
implements|implements
name|LogicalIOProcessor
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
name|TezProcessor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|isMap
init|=
literal|false
decl_stmt|;
name|RecordProcessor
name|rproc
init|=
literal|null
decl_stmt|;
specifier|private
name|JobConf
name|jobConf
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|TezProcessor
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
name|PerfLogger
operator|.
name|getPerfLogger
argument_list|()
decl_stmt|;
specifier|private
name|TezProcessorContext
name|processorContext
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|NumberFormat
name|taskIdFormat
init|=
name|NumberFormat
operator|.
name|getInstance
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|NumberFormat
name|jobIdFormat
init|=
name|NumberFormat
operator|.
name|getInstance
argument_list|()
decl_stmt|;
static|static
block|{
name|taskIdFormat
operator|.
name|setGroupingUsed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|taskIdFormat
operator|.
name|setMinimumIntegerDigits
argument_list|(
literal|6
argument_list|)
expr_stmt|;
name|jobIdFormat
operator|.
name|setGroupingUsed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|jobIdFormat
operator|.
name|setMinimumIntegerDigits
argument_list|(
literal|4
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TezProcessor
parameter_list|(
name|boolean
name|isMap
parameter_list|)
block|{
name|this
operator|.
name|isMap
operator|=
name|isMap
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|// we have to close in the processor's run method, because tez closes inputs
comment|// before calling close (TEZ-955) and we might need to read inputs
comment|// when we flush the pipeline.
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleEvents
parameter_list|(
name|List
argument_list|<
name|Event
argument_list|>
name|arg0
parameter_list|)
block|{
comment|//this is not called by tez, so nothing to be done here
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|TezProcessorContext
name|processorContext
parameter_list|)
throws|throws
name|IOException
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_INITIALIZE_PROCESSOR
argument_list|)
expr_stmt|;
name|this
operator|.
name|processorContext
operator|=
name|processorContext
expr_stmt|;
comment|//get the jobconf
name|byte
index|[]
name|userPayload
init|=
name|processorContext
operator|.
name|getUserPayload
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|TezUtils
operator|.
name|createConfFromUserPayload
argument_list|(
name|userPayload
argument_list|)
decl_stmt|;
name|this
operator|.
name|jobConf
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|setupMRLegacyConfigs
argument_list|(
name|processorContext
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
name|TEZ_INITIALIZE_PROCESSOR
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setupMRLegacyConfigs
parameter_list|(
name|TezProcessorContext
name|processorContext
parameter_list|)
block|{
comment|// Hive "insert overwrite local directory" uses task id as dir name
comment|// Setting the id in jobconf helps to have the similar dir name as MR
name|StringBuilder
name|taskAttemptIdBuilder
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"task"
argument_list|)
decl_stmt|;
name|taskAttemptIdBuilder
operator|.
name|append
argument_list|(
name|processorContext
operator|.
name|getApplicationId
argument_list|()
operator|.
name|getClusterTimestamp
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"_"
argument_list|)
operator|.
name|append
argument_list|(
name|jobIdFormat
operator|.
name|format
argument_list|(
name|processorContext
operator|.
name|getApplicationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"_"
argument_list|)
expr_stmt|;
if|if
condition|(
name|isMap
condition|)
block|{
name|taskAttemptIdBuilder
operator|.
name|append
argument_list|(
literal|"m_"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|taskAttemptIdBuilder
operator|.
name|append
argument_list|(
literal|"r_"
argument_list|)
expr_stmt|;
block|}
name|taskAttemptIdBuilder
operator|.
name|append
argument_list|(
name|taskIdFormat
operator|.
name|format
argument_list|(
name|processorContext
operator|.
name|getTaskIndex
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"_"
argument_list|)
operator|.
name|append
argument_list|(
name|processorContext
operator|.
name|getTaskAttemptNumber
argument_list|()
argument_list|)
expr_stmt|;
comment|// In MR, mapreduce.task.attempt.id is same as mapred.task.id. Go figure.
name|String
name|taskAttemptIdStr
init|=
name|taskAttemptIdBuilder
operator|.
name|toString
argument_list|()
decl_stmt|;
name|this
operator|.
name|jobConf
operator|.
name|set
argument_list|(
literal|"mapred.task.id"
argument_list|,
name|taskAttemptIdStr
argument_list|)
expr_stmt|;
name|this
operator|.
name|jobConf
operator|.
name|set
argument_list|(
literal|"mapreduce.task.attempt.id"
argument_list|,
name|taskAttemptIdStr
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
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
name|Exception
name|processingException
init|=
literal|null
decl_stmt|;
try|try
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_RUN_PROCESSOR
argument_list|)
expr_stmt|;
comment|// in case of broadcast-join read the broadcast edge inputs
comment|// (possibly asynchronously)
name|LOG
operator|.
name|info
argument_list|(
literal|"Running task: "
operator|+
name|processorContext
operator|.
name|getUniqueIdentifier
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|isMap
condition|)
block|{
name|rproc
operator|=
operator|new
name|MapRecordProcessor
argument_list|()
expr_stmt|;
name|MRInputLegacy
name|mrInput
init|=
name|getMRInput
argument_list|(
name|inputs
argument_list|)
decl_stmt|;
try|try
block|{
name|mrInput
operator|.
name|init
argument_list|()
expr_stmt|;
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
literal|"Failed while initializing MRInput"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|rproc
operator|=
operator|new
name|ReduceRecordProcessor
argument_list|()
expr_stmt|;
block|}
name|TezCacheAccess
name|cacheAccess
init|=
name|TezCacheAccess
operator|.
name|createInstance
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
comment|// Start the actual Inputs. After MRInput initialization.
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|LogicalInput
argument_list|>
name|inputEntry
range|:
name|inputs
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|cacheAccess
operator|.
name|isInputCached
argument_list|(
name|inputEntry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Input: "
operator|+
name|inputEntry
operator|.
name|getKey
argument_list|()
operator|+
literal|" is not cached"
argument_list|)
expr_stmt|;
name|inputEntry
operator|.
name|getValue
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Input: "
operator|+
name|inputEntry
operator|.
name|getKey
argument_list|()
operator|+
literal|" is already cached. Skipping start"
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Outputs will be started later by the individual Processors.
name|MRTaskReporter
name|mrReporter
init|=
operator|new
name|MRTaskReporter
argument_list|(
name|processorContext
argument_list|)
decl_stmt|;
name|rproc
operator|.
name|init
argument_list|(
name|jobConf
argument_list|,
name|processorContext
argument_list|,
name|mrReporter
argument_list|,
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|rproc
operator|.
name|run
argument_list|()
expr_stmt|;
comment|//done - output does not need to be committed as hive does not use outputcommitter
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_RUN_PROCESSOR
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|processingException
operator|=
name|e
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
if|if
condition|(
name|rproc
operator|!=
literal|null
condition|)
block|{
name|rproc
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|processingException
operator|==
literal|null
condition|)
block|{
name|processingException
operator|=
name|e
expr_stmt|;
block|}
block|}
if|if
condition|(
name|processingException
operator|!=
literal|null
condition|)
block|{
throw|throw
name|processingException
throw|;
block|}
block|}
block|}
comment|/**    * KVOutputCollector. OutputCollector that writes using KVWriter.    * Must be initialized before it is used.    *     */
specifier|static
class|class
name|TezKVOutputCollector
implements|implements
name|OutputCollector
block|{
specifier|private
name|KeyValueWriter
name|writer
decl_stmt|;
specifier|private
specifier|final
name|LogicalOutput
name|output
decl_stmt|;
name|TezKVOutputCollector
parameter_list|(
name|LogicalOutput
name|logicalOutput
parameter_list|)
block|{
name|this
operator|.
name|output
operator|=
name|logicalOutput
expr_stmt|;
block|}
name|void
name|initialize
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|writer
operator|=
operator|(
name|KeyValueWriter
operator|)
name|output
operator|.
name|getWriter
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|collect
parameter_list|(
name|Object
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|write
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|MRInputLegacy
name|getMRInput
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalInput
argument_list|>
name|inputs
parameter_list|)
block|{
comment|//there should be only one MRInput
name|MRInputLegacy
name|theMRInput
init|=
literal|null
decl_stmt|;
for|for
control|(
name|LogicalInput
name|inp
range|:
name|inputs
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|inp
operator|instanceof
name|MRInputLegacy
condition|)
block|{
if|if
condition|(
name|theMRInput
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Only one MRInput is expected"
argument_list|)
throw|;
block|}
comment|//a better logic would be to find the alias
name|theMRInput
operator|=
operator|(
name|MRInputLegacy
operator|)
name|inp
expr_stmt|;
block|}
block|}
return|return
name|theMRInput
return|;
block|}
block|}
end_class

end_unit

