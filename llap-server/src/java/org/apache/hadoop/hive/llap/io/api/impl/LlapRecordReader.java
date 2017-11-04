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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|impl
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
name|concurrent
operator|.
name|ExecutorService
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|llap
operator|.
name|ConsumerFeedback
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
name|llap
operator|.
name|counters
operator|.
name|FragmentCountersMap
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
name|llap
operator|.
name|counters
operator|.
name|LlapIOCounters
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
name|llap
operator|.
name|counters
operator|.
name|QueryFragmentCounters
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
name|llap
operator|.
name|daemon
operator|.
name|impl
operator|.
name|StatsRecordingThreadPool
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
name|llap
operator|.
name|io
operator|.
name|decode
operator|.
name|ColumnVectorProducer
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
name|llap
operator|.
name|io
operator|.
name|decode
operator|.
name|ReadPipeline
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
name|llap
operator|.
name|tezplugins
operator|.
name|LlapTezUtils
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
name|DagUtils
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
name|vector
operator|.
name|VectorizedRowBatch
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
name|vector
operator|.
name|VectorizedRowBatchCtx
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
name|orc
operator|.
name|OrcInputFormat
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
name|orc
operator|.
name|OrcSplit
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
name|orc
operator|.
name|VectorizedOrcAcidRowBatchReader
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
name|orc
operator|.
name|encoded
operator|.
name|Consumer
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
name|sarg
operator|.
name|ConvertAstToSearchArg
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
name|sarg
operator|.
name|SearchArgument
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
name|serde2
operator|.
name|ColumnProjectionUtils
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
name|Deserializer
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
name|NullWritable
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
name|FileSplit
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
name|InputFormat
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
name|RecordReader
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
name|Reporter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|TypeDescription
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|impl
operator|.
name|SchemaEvolution
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
name|counters
operator|.
name|TezCounters
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
name|slf4j
operator|.
name|MDC
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
name|Lists
import|;
end_import

begin_class
class|class
name|LlapRecordReader
implements|implements
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
implements|,
name|Consumer
argument_list|<
name|ColumnVectorBatch
argument_list|>
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
name|LlapRecordReader
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|FileSplit
name|split
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|columnIds
decl_stmt|;
specifier|private
specifier|final
name|SearchArgument
name|sarg
decl_stmt|;
specifier|private
specifier|final
name|String
index|[]
name|columnNames
decl_stmt|;
specifier|private
specifier|final
name|VectorizedRowBatchCtx
name|rbCtx
decl_stmt|;
specifier|private
specifier|final
name|Object
index|[]
name|partitionValues
decl_stmt|;
specifier|private
specifier|final
name|LinkedList
argument_list|<
name|ColumnVectorBatch
argument_list|>
name|pendingData
init|=
operator|new
name|LinkedList
argument_list|<
name|ColumnVectorBatch
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|ColumnVectorBatch
name|lastCvb
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|isFirst
init|=
literal|true
decl_stmt|;
specifier|private
name|Throwable
name|pendingError
init|=
literal|null
decl_stmt|;
comment|/** Vector that is currently being processed by our user. */
specifier|private
name|boolean
name|isDone
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isClosed
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|ConsumerFeedback
argument_list|<
name|ColumnVectorBatch
argument_list|>
name|feedback
decl_stmt|;
specifier|private
specifier|final
name|QueryFragmentCounters
name|counters
decl_stmt|;
specifier|private
name|long
name|firstReturnTime
decl_stmt|;
specifier|private
specifier|final
name|JobConf
name|jobConf
decl_stmt|;
specifier|private
specifier|final
name|ReadPipeline
name|rp
decl_stmt|;
specifier|private
specifier|final
name|ExecutorService
name|executor
decl_stmt|;
specifier|private
specifier|final
name|int
name|columnCount
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isAcidScan
decl_stmt|;
comment|/**    * Creates the record reader and checks the input-specific compatibility.    * @return The reader if the split can be read, null otherwise.    */
specifier|public
specifier|static
name|LlapRecordReader
name|create
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|FileSplit
name|split
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|includedCols
parameter_list|,
name|String
name|hostName
parameter_list|,
name|ColumnVectorProducer
name|cvp
parameter_list|,
name|ExecutorService
name|executor
parameter_list|,
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|sourceInputFormat
parameter_list|,
name|Deserializer
name|sourceSerDe
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
name|MapWork
name|mapWork
init|=
name|findMapWork
argument_list|(
name|job
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapWork
operator|==
literal|null
condition|)
return|return
literal|null
return|;
comment|// No compatible MapWork.
name|LlapRecordReader
name|rr
init|=
operator|new
name|LlapRecordReader
argument_list|(
name|mapWork
argument_list|,
name|job
argument_list|,
name|split
argument_list|,
name|includedCols
argument_list|,
name|hostName
argument_list|,
name|cvp
argument_list|,
name|executor
argument_list|,
name|sourceInputFormat
argument_list|,
name|sourceSerDe
argument_list|,
name|reporter
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|rr
operator|.
name|checkOrcSchemaEvolution
argument_list|()
condition|)
block|{
name|rr
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|rr
return|;
block|}
specifier|private
name|LlapRecordReader
parameter_list|(
name|MapWork
name|mapWork
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|FileSplit
name|split
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|includedCols
parameter_list|,
name|String
name|hostName
parameter_list|,
name|ColumnVectorProducer
name|cvp
parameter_list|,
name|ExecutorService
name|executor
parameter_list|,
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|sourceInputFormat
parameter_list|,
name|Deserializer
name|sourceSerDe
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
name|this
operator|.
name|executor
operator|=
name|executor
expr_stmt|;
name|this
operator|.
name|jobConf
operator|=
name|job
expr_stmt|;
name|this
operator|.
name|split
operator|=
name|split
expr_stmt|;
name|this
operator|.
name|sarg
operator|=
name|ConvertAstToSearchArg
operator|.
name|createFromConf
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnNames
operator|=
name|ColumnProjectionUtils
operator|.
name|getReadColumnNames
argument_list|(
name|job
argument_list|)
expr_stmt|;
specifier|final
name|String
name|fragmentId
init|=
name|LlapTezUtils
operator|.
name|getFragmentId
argument_list|(
name|job
argument_list|)
decl_stmt|;
specifier|final
name|String
name|dagId
init|=
name|LlapTezUtils
operator|.
name|getDagId
argument_list|(
name|job
argument_list|)
decl_stmt|;
specifier|final
name|String
name|queryId
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
decl_stmt|;
name|MDC
operator|.
name|put
argument_list|(
literal|"dagId"
argument_list|,
name|dagId
argument_list|)
expr_stmt|;
name|MDC
operator|.
name|put
argument_list|(
literal|"queryId"
argument_list|,
name|queryId
argument_list|)
expr_stmt|;
name|TezCounters
name|taskCounters
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|fragmentId
operator|!=
literal|null
condition|)
block|{
name|MDC
operator|.
name|put
argument_list|(
literal|"fragmentId"
argument_list|,
name|fragmentId
argument_list|)
expr_stmt|;
name|taskCounters
operator|=
name|FragmentCountersMap
operator|.
name|getCountersForFragment
argument_list|(
name|fragmentId
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Received fragment id: {}"
argument_list|,
name|fragmentId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Not using tez counters as fragment id string is null"
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|counters
operator|=
operator|new
name|QueryFragmentCounters
argument_list|(
name|job
argument_list|,
name|taskCounters
argument_list|)
expr_stmt|;
name|this
operator|.
name|counters
operator|.
name|setDesc
argument_list|(
name|QueryFragmentCounters
operator|.
name|Desc
operator|.
name|MACHINE
argument_list|,
name|hostName
argument_list|)
expr_stmt|;
name|isAcidScan
operator|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|jobConf
argument_list|,
name|ConfVars
operator|.
name|HIVE_TRANSACTIONAL_TABLE_SCAN
argument_list|)
expr_stmt|;
name|TypeDescription
name|schema
init|=
name|OrcInputFormat
operator|.
name|getDesiredRowTypeDescr
argument_list|(
name|job
argument_list|,
name|isAcidScan
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
if|if
condition|(
name|isAcidScan
condition|)
block|{
name|this
operator|.
name|columnIds
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
specifier|final
name|int
name|ACID_FIELDS
init|=
name|OrcInputFormat
operator|.
name|getRootColumn
argument_list|(
literal|false
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ACID_FIELDS
condition|;
name|i
operator|++
control|)
block|{
name|columnIds
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|includedCols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|columnIds
operator|.
name|add
argument_list|(
name|i
operator|+
name|ACID_FIELDS
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|columnCount
operator|=
name|columnIds
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|columnIds
operator|=
name|includedCols
expr_stmt|;
name|this
operator|.
name|columnCount
operator|=
name|columnIds
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
name|VectorizedRowBatchCtx
name|ctx
init|=
name|mapWork
operator|.
name|getVectorizedRowBatchCtx
argument_list|()
decl_stmt|;
name|rbCtx
operator|=
name|ctx
operator|!=
literal|null
condition|?
name|ctx
else|:
name|LlapInputFormat
operator|.
name|createFakeVrbCtx
argument_list|(
name|mapWork
argument_list|)
expr_stmt|;
if|if
condition|(
name|includedCols
operator|==
literal|null
condition|)
block|{
comment|// Assume including everything means the VRB will have everything.
name|includedCols
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|rbCtx
operator|.
name|getRowColumnTypeInfos
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rbCtx
operator|.
name|getRowColumnTypeInfos
argument_list|()
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|includedCols
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|partitionColumnCount
init|=
name|rbCtx
operator|.
name|getPartitionColumnCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|partitionColumnCount
operator|>
literal|0
condition|)
block|{
name|partitionValues
operator|=
operator|new
name|Object
index|[
name|partitionColumnCount
index|]
expr_stmt|;
name|VectorizedRowBatchCtx
operator|.
name|getPartitionValues
argument_list|(
name|rbCtx
argument_list|,
name|mapWork
argument_list|,
name|split
argument_list|,
name|partitionValues
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|partitionValues
operator|=
literal|null
expr_stmt|;
block|}
comment|// Create the consumer of encoded data; it will coordinate decoding to CVBs.
name|feedback
operator|=
name|rp
operator|=
name|cvp
operator|.
name|createReadPipeline
argument_list|(
name|this
argument_list|,
name|split
argument_list|,
name|columnIds
argument_list|,
name|sarg
argument_list|,
name|columnNames
argument_list|,
name|counters
argument_list|,
name|schema
argument_list|,
name|sourceInputFormat
argument_list|,
name|sourceSerDe
argument_list|,
name|reporter
argument_list|,
name|job
argument_list|,
name|mapWork
operator|.
name|getPathToPartitionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|MapWork
name|findMapWork
parameter_list|(
name|JobConf
name|job
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
name|inputName
init|=
name|job
operator|.
name|get
argument_list|(
name|Utilities
operator|.
name|INPUT_NAME
argument_list|,
literal|null
argument_list|)
decl_stmt|;
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
literal|"Initializing for input "
operator|+
name|inputName
argument_list|)
expr_stmt|;
block|}
name|String
name|prefixes
init|=
name|job
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
operator|&&
operator|!
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|prefixes
argument_list|)
condition|)
block|{
comment|// Currently SMB is broken, so we cannot check if it's  compatible with IO elevator.
comment|// So, we don't use the below code that would get the correct MapWork. See HIVE-16985.
return|return
literal|null
return|;
block|}
name|BaseWork
name|work
init|=
literal|null
decl_stmt|;
comment|// HIVE-16985: try to find the fake merge work for SMB join, that is really another MapWork.
if|if
condition|(
name|inputName
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|prefixes
operator|==
literal|null
operator|||
operator|!
name|Lists
operator|.
name|newArrayList
argument_list|(
name|prefixes
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
operator|.
name|contains
argument_list|(
name|inputName
argument_list|)
condition|)
block|{
name|inputName
operator|=
literal|null
expr_stmt|;
block|}
block|}
if|if
condition|(
name|inputName
operator|!=
literal|null
condition|)
block|{
name|work
operator|=
name|Utilities
operator|.
name|getMergeWork
argument_list|(
name|job
argument_list|,
name|inputName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|work
operator|==
literal|null
operator|||
operator|!
operator|(
name|work
operator|instanceof
name|MapWork
operator|)
condition|)
block|{
name|work
operator|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|MapWork
operator|)
name|work
return|;
block|}
comment|/**    * Starts the data read pipeline    */
specifier|public
name|void
name|start
parameter_list|()
block|{
comment|// perform the data read asynchronously
if|if
condition|(
name|executor
operator|instanceof
name|StatsRecordingThreadPool
condition|)
block|{
comment|// Every thread created by this thread pool will use the same handler
operator|(
operator|(
name|StatsRecordingThreadPool
operator|)
name|executor
operator|)
operator|.
name|setUncaughtExceptionHandler
argument_list|(
operator|new
name|IOUncaughtExceptionHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|executor
operator|.
name|submit
argument_list|(
name|rp
operator|.
name|getReadCallable
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|checkOrcSchemaEvolution
parameter_list|()
block|{
name|SchemaEvolution
name|evolution
init|=
name|rp
operator|.
name|getSchemaEvolution
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|columnCount
condition|;
operator|++
name|i
control|)
block|{
name|int
name|projectedColId
init|=
name|columnIds
operator|==
literal|null
condition|?
name|i
else|:
name|columnIds
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
comment|// Adjust file column index for ORC struct.
comment|// LLAP IO does not support ACID. When it supports, this would be auto adjusted.
name|int
name|fileColId
init|=
name|OrcInputFormat
operator|.
name|getRootColumn
argument_list|(
operator|!
name|isAcidScan
argument_list|)
operator|+
name|projectedColId
operator|+
literal|1
decl_stmt|;
if|if
condition|(
operator|!
name|evolution
operator|.
name|isPPDSafeConversion
argument_list|(
name|fileColId
argument_list|)
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unsupported schema evolution! Disabling Llap IO for {}"
argument_list|,
name|split
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|NullWritable
name|key
parameter_list|,
name|VectorizedRowBatch
name|value
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|value
operator|!=
literal|null
assert|;
if|if
condition|(
name|isClosed
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"next called after close"
argument_list|)
throw|;
block|}
comment|// Add partition cols if necessary (see VectorizedOrcInputFormat for details).
name|boolean
name|wasFirst
init|=
name|isFirst
decl_stmt|;
if|if
condition|(
name|isFirst
condition|)
block|{
if|if
condition|(
name|partitionValues
operator|!=
literal|null
condition|)
block|{
name|rbCtx
operator|.
name|addPartitionColsToBatch
argument_list|(
name|value
argument_list|,
name|partitionValues
argument_list|)
expr_stmt|;
block|}
name|isFirst
operator|=
literal|false
expr_stmt|;
block|}
name|ColumnVectorBatch
name|cvb
init|=
literal|null
decl_stmt|;
try|try
block|{
name|cvb
operator|=
name|nextCvb
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// Query might have been canceled. Stop the background processing.
name|feedback
operator|.
name|stop
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|cvb
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|wasFirst
condition|)
block|{
name|firstReturnTime
operator|=
name|counters
operator|.
name|startTimeCounter
argument_list|()
expr_stmt|;
block|}
name|counters
operator|.
name|incrTimeCounter
argument_list|(
name|LlapIOCounters
operator|.
name|CONSUMER_TIME_NS
argument_list|,
name|firstReturnTime
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
specifier|final
name|boolean
name|isVectorized
init|=
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
name|HIVE_VECTORIZATION_ENABLED
argument_list|)
decl_stmt|;
if|if
condition|(
name|isAcidScan
condition|)
block|{
name|value
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|isVectorized
condition|)
block|{
specifier|final
name|VectorizedRowBatch
name|acidVrb
init|=
operator|new
name|VectorizedRowBatch
argument_list|(
name|cvb
operator|.
name|cols
operator|.
name|length
argument_list|)
decl_stmt|;
name|acidVrb
operator|.
name|cols
operator|=
name|cvb
operator|.
name|cols
expr_stmt|;
name|acidVrb
operator|.
name|size
operator|=
name|cvb
operator|.
name|size
expr_stmt|;
specifier|final
name|VectorizedOrcAcidRowBatchReader
name|acidReader
init|=
operator|new
name|VectorizedOrcAcidRowBatchReader
argument_list|(
operator|(
name|OrcSplit
operator|)
name|split
argument_list|,
name|jobConf
argument_list|,
name|Reporter
operator|.
name|NULL
argument_list|,
operator|new
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|NullWritable
name|key
parameter_list|,
name|VectorizedRowBatch
name|value
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|NullWritable
name|createKey
parameter_list|()
block|{
return|return
name|NullWritable
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorizedRowBatch
name|createValue
parameter_list|()
block|{
return|return
name|acidVrb
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPos
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{                   }
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
block|}
argument_list|,
name|rbCtx
argument_list|)
decl_stmt|;
name|acidReader
operator|.
name|next
argument_list|(
name|NullWritable
operator|.
name|get
argument_list|()
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|columnCount
operator|!=
name|cvb
operator|.
name|cols
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected number of columns, VRB has "
operator|+
name|columnCount
operator|+
literal|" included, but the reader returned "
operator|+
name|cvb
operator|.
name|cols
operator|.
name|length
argument_list|)
throw|;
block|}
comment|// VRB was created from VrbCtx, so we already have pre-allocated column vectors
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|cvb
operator|.
name|cols
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
comment|// Return old CVs (if any) to caller. We assume these things all have the same schema.
name|cvb
operator|.
name|swapColumnVector
argument_list|(
name|i
argument_list|,
name|value
operator|.
name|cols
argument_list|,
name|columnIds
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|value
operator|.
name|selectedInUse
operator|=
literal|false
expr_stmt|;
name|value
operator|.
name|size
operator|=
name|cvb
operator|.
name|size
expr_stmt|;
block|}
if|if
condition|(
name|wasFirst
condition|)
block|{
name|firstReturnTime
operator|=
name|counters
operator|.
name|startTimeCounter
argument_list|()
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|VectorizedRowBatchCtx
name|getVectorizedRowBatchCtx
parameter_list|()
block|{
return|return
name|rbCtx
return|;
block|}
specifier|private
specifier|final
class|class
name|IOUncaughtExceptionHandler
implements|implements
name|Thread
operator|.
name|UncaughtExceptionHandler
block|{
annotation|@
name|Override
specifier|public
name|void
name|uncaughtException
parameter_list|(
specifier|final
name|Thread
name|t
parameter_list|,
specifier|final
name|Throwable
name|e
parameter_list|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|error
argument_list|(
literal|"Unhandled error from reader thread. threadName: {} threadId: {}"
operator|+
literal|" Message: {}"
argument_list|,
name|t
operator|.
name|getName
argument_list|()
argument_list|,
name|t
operator|.
name|getId
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|setError
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|ColumnVectorBatch
name|nextCvb
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|boolean
name|isFirst
init|=
operator|(
name|lastCvb
operator|==
literal|null
operator|)
decl_stmt|;
if|if
condition|(
operator|!
name|isFirst
condition|)
block|{
name|feedback
operator|.
name|returnData
argument_list|(
name|lastCvb
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|pendingData
init|)
block|{
comment|// We are waiting for next block. Either we will get it, or be told we are done.
name|boolean
name|doLogBlocking
init|=
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
operator|&&
name|isNothingToReport
argument_list|()
decl_stmt|;
if|if
condition|(
name|doLogBlocking
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|trace
argument_list|(
literal|"next will block"
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
name|isNothingToReport
argument_list|()
condition|)
block|{
name|pendingData
operator|.
name|wait
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|doLogBlocking
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|trace
argument_list|(
literal|"next is unblocked"
argument_list|)
expr_stmt|;
block|}
name|rethrowErrorIfAny
argument_list|()
expr_stmt|;
name|lastCvb
operator|=
name|pendingData
operator|.
name|poll
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
operator|&&
name|lastCvb
operator|!=
literal|null
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|trace
argument_list|(
literal|"Processing will receive vector {}"
argument_list|,
name|lastCvb
argument_list|)
expr_stmt|;
block|}
return|return
name|lastCvb
return|;
block|}
specifier|private
name|boolean
name|isNothingToReport
parameter_list|()
block|{
return|return
operator|!
name|isDone
operator|&&
name|pendingData
operator|.
name|isEmpty
argument_list|()
operator|&&
name|pendingError
operator|==
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|NullWritable
name|createKey
parameter_list|()
block|{
return|return
name|NullWritable
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorizedRowBatch
name|createValue
parameter_list|()
block|{
return|return
name|rbCtx
operator|.
name|createVectorizedRowBatch
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPos
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|-
literal|1
return|;
comment|// Position doesn't make sense for async reader, chunk order is arbitrary.
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
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|trace
argument_list|(
literal|"close called; closed {}, done {}, err {}, pending {}"
argument_list|,
name|isClosed
argument_list|,
name|isDone
argument_list|,
name|pendingError
argument_list|,
name|pendingData
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Llap counters: {}"
argument_list|,
name|counters
argument_list|)
expr_stmt|;
comment|// This is where counters are logged!
name|feedback
operator|.
name|stop
argument_list|()
expr_stmt|;
name|rethrowErrorIfAny
argument_list|()
expr_stmt|;
name|MDC
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|rethrowErrorIfAny
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|pendingError
operator|==
literal|null
condition|)
return|return;
if|if
condition|(
name|pendingError
operator|instanceof
name|IOException
condition|)
block|{
throw|throw
operator|(
name|IOException
operator|)
name|pendingError
throw|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
name|pendingError
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setDone
parameter_list|()
block|{
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|debug
argument_list|(
literal|"setDone called; closed {}, done {}, err {}, pending {}"
argument_list|,
name|isClosed
argument_list|,
name|isDone
argument_list|,
name|pendingError
argument_list|,
name|pendingData
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|pendingData
init|)
block|{
name|isDone
operator|=
literal|true
expr_stmt|;
name|pendingData
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|consumeData
parameter_list|(
name|ColumnVectorBatch
name|data
parameter_list|)
block|{
if|if
condition|(
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|trace
argument_list|(
literal|"consume called; closed {}, done {}, err {}, pending {}"
argument_list|,
name|isClosed
argument_list|,
name|isDone
argument_list|,
name|pendingError
argument_list|,
name|pendingData
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|pendingData
init|)
block|{
if|if
condition|(
name|isClosed
condition|)
block|{
return|return;
block|}
name|pendingData
operator|.
name|add
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|pendingData
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setError
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|counters
operator|.
name|incrCounter
argument_list|(
name|LlapIOCounters
operator|.
name|NUM_ERRORS
argument_list|)
expr_stmt|;
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|debug
argument_list|(
literal|"setError called; current state closed {}, done {}, err {}, pending {}"
argument_list|,
name|isClosed
argument_list|,
name|isDone
argument_list|,
name|pendingError
argument_list|,
name|pendingData
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|warn
argument_list|(
literal|"setError called with an error"
argument_list|,
name|t
argument_list|)
expr_stmt|;
assert|assert
name|t
operator|!=
literal|null
assert|;
synchronized|synchronized
init|(
name|pendingData
init|)
block|{
name|pendingError
operator|=
name|t
expr_stmt|;
name|pendingData
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
block|{
comment|// TODO: plumb progress info thru the reader if we can get metadata from loader first.
return|return
literal|0.0f
return|;
block|}
block|}
end_class

end_unit

