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
name|DebugUtils
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
name|vector
operator|.
name|VectorizedInputFormatInterface
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
name|CombineHiveInputFormat
operator|.
name|AvoidSplitCombination
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
name|LlapAwareSplit
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
name|SelfDescribingInputFormatInterface
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
name|InputSplit
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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|HiveStringUtils
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|FutureCallback
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
name|util
operator|.
name|concurrent
operator|.
name|Futures
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
name|util
operator|.
name|concurrent
operator|.
name|ListenableFuture
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
name|util
operator|.
name|concurrent
operator|.
name|ListeningExecutorService
import|;
end_import

begin_class
specifier|public
class|class
name|LlapInputFormat
implements|implements
name|InputFormat
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
implements|,
name|VectorizedInputFormatInterface
implements|,
name|SelfDescribingInputFormatInterface
implements|,
name|AvoidSplitCombination
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
specifier|private
specifier|final
name|InputFormat
name|sourceInputFormat
decl_stmt|;
specifier|private
specifier|final
name|AvoidSplitCombination
name|sourceASC
decl_stmt|;
specifier|private
specifier|final
name|ColumnVectorProducer
name|cvp
decl_stmt|;
specifier|private
specifier|final
name|ListeningExecutorService
name|executor
decl_stmt|;
specifier|private
specifier|final
name|String
name|hostName
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|LlapInputFormat
parameter_list|(
name|InputFormat
name|sourceInputFormat
parameter_list|,
name|ColumnVectorProducer
name|cvp
parameter_list|,
name|ListeningExecutorService
name|executor
parameter_list|)
block|{
comment|// TODO: right now, we do nothing with source input format, ORC-only in the first cut.
comment|//       We'd need to plumb it thru and use it to get data to cache/etc.
assert|assert
name|sourceInputFormat
operator|instanceof
name|OrcInputFormat
assert|;
name|this
operator|.
name|executor
operator|=
name|executor
expr_stmt|;
name|this
operator|.
name|cvp
operator|=
name|cvp
expr_stmt|;
name|this
operator|.
name|sourceInputFormat
operator|=
name|sourceInputFormat
expr_stmt|;
name|this
operator|.
name|sourceASC
operator|=
operator|(
name|sourceInputFormat
operator|instanceof
name|AvoidSplitCombination
operator|)
condition|?
operator|(
name|AvoidSplitCombination
operator|)
name|sourceInputFormat
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|hostName
operator|=
name|HiveStringUtils
operator|.
name|getHostname
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
name|getRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|useLlapIo
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|split
operator|instanceof
name|LlapAwareSplit
condition|)
block|{
name|useLlapIo
operator|=
operator|(
operator|(
name|LlapAwareSplit
operator|)
name|split
operator|)
operator|.
name|canUseLlapIo
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|useLlapIo
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Not using LLAP IO for an unsupported split: "
operator|+
name|split
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
name|rr
init|=
name|sourceInputFormat
operator|.
name|getRecordReader
argument_list|(
name|split
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
decl_stmt|;
return|return
name|rr
return|;
block|}
name|boolean
name|isVectorMode
init|=
name|Utilities
operator|.
name|getUseVectorizedInputFileFormat
argument_list|(
name|job
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isVectorMode
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|error
argument_list|(
literal|"No LLAP IO in non-vectorized mode"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"No LLAP IO in non-vectorized mode"
argument_list|)
throw|;
block|}
name|FileSplit
name|fileSplit
init|=
operator|(
name|FileSplit
operator|)
name|split
decl_stmt|;
name|reporter
operator|.
name|setStatus
argument_list|(
name|fileSplit
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|List
argument_list|<
name|Integer
argument_list|>
name|includedCols
init|=
name|ColumnProjectionUtils
operator|.
name|isReadAllColumns
argument_list|(
name|job
argument_list|)
condition|?
literal|null
else|:
name|ColumnProjectionUtils
operator|.
name|getReadColumnIDs
argument_list|(
name|job
argument_list|)
decl_stmt|;
return|return
operator|new
name|LlapRecordReader
argument_list|(
name|job
argument_list|,
name|fileSplit
argument_list|,
name|includedCols
argument_list|,
name|hostName
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|InputSplit
index|[]
name|getSplits
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|int
name|numSplits
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|sourceInputFormat
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
name|numSplits
argument_list|)
return|;
block|}
specifier|private
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
specifier|final
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
name|boolean
index|[]
name|columnsToIncludeTruncated
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
specifier|public
name|LlapRecordReader
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
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|split
operator|=
name|split
expr_stmt|;
name|this
operator|.
name|columnIds
operator|=
name|includedCols
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
name|String
name|dagId
init|=
name|job
operator|.
name|get
argument_list|(
literal|"tez.mapreduce.dag.index"
argument_list|)
decl_stmt|;
name|String
name|vertexId
init|=
name|job
operator|.
name|get
argument_list|(
literal|"tez.mapreduce.vertex.index"
argument_list|)
decl_stmt|;
name|String
name|taskId
init|=
name|job
operator|.
name|get
argument_list|(
literal|"tez.mapreduce.task.index"
argument_list|)
decl_stmt|;
name|String
name|taskAttemptId
init|=
name|job
operator|.
name|get
argument_list|(
literal|"tez.mapreduce.task.attempt.index"
argument_list|)
decl_stmt|;
name|TezCounters
name|taskCounters
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|dagId
operator|!=
literal|null
operator|&&
name|vertexId
operator|!=
literal|null
operator|&&
name|taskId
operator|!=
literal|null
operator|&&
name|taskAttemptId
operator|!=
literal|null
condition|)
block|{
name|String
name|fullId
init|=
name|Joiner
operator|.
name|on
argument_list|(
literal|'_'
argument_list|)
operator|.
name|join
argument_list|(
name|dagId
argument_list|,
name|vertexId
argument_list|,
name|taskId
argument_list|,
name|taskAttemptId
argument_list|)
decl_stmt|;
name|taskCounters
operator|=
name|FragmentCountersMap
operator|.
name|getCountersForFragment
argument_list|(
name|fullId
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Received dagid_vertexid_taskid_attempid: {}"
argument_list|,
name|fullId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Not using tez counters as some identifier is null."
operator|+
literal|" dagId: {} vertexId: {} taskId: {} taskAttempId: {}"
argument_list|,
name|dagId
argument_list|,
name|vertexId
argument_list|,
name|taskId
argument_list|,
name|taskAttemptId
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
name|MapWork
name|mapWork
init|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|rbCtx
operator|=
name|mapWork
operator|.
name|getVectorizedRowBatchCtx
argument_list|()
expr_stmt|;
name|columnsToIncludeTruncated
operator|=
name|rbCtx
operator|.
name|getColumnsToIncludeTruncated
argument_list|(
name|job
argument_list|)
expr_stmt|;
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
name|job
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
name|startRead
argument_list|()
expr_stmt|;
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
if|if
condition|(
name|columnIds
operator|.
name|size
argument_list|()
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
name|columnIds
operator|.
name|size
argument_list|()
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
specifier|private
specifier|final
class|class
name|UncaughtErrorHandler
implements|implements
name|FutureCallback
argument_list|<
name|Void
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|(
name|Void
name|result
parameter_list|)
block|{
comment|// Successful execution of reader is supposed to call setDone.
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// Reader is not supposed to throw AFTER calling setError.
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|error
argument_list|(
literal|"Unhandled error from reader thread "
operator|+
name|t
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|setError
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|startRead
parameter_list|()
block|{
comment|// Create the consumer of encoded data; it will coordinate decoding to CVBs.
name|ReadPipeline
name|rp
init|=
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
argument_list|)
decl_stmt|;
name|feedback
operator|=
name|rp
expr_stmt|;
name|ListenableFuture
argument_list|<
name|Void
argument_list|>
name|future
init|=
name|executor
operator|.
name|submit
argument_list|(
name|rp
operator|.
name|getReadCallable
argument_list|()
argument_list|)
decl_stmt|;
comment|// TODO: we should NOT do this thing with handler. Reader needs to do cleanup in most cases.
name|Futures
operator|.
name|addCallback
argument_list|(
name|future
argument_list|,
operator|new
name|UncaughtErrorHandler
argument_list|()
argument_list|)
expr_stmt|;
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
argument_list|(
name|columnsToIncludeTruncated
argument_list|)
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
name|info
argument_list|(
literal|"setError called; closed {}, done {}, err {}, pending {}"
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
annotation|@
name|Override
specifier|public
name|boolean
name|shouldSkipCombine
parameter_list|(
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|sourceASC
operator|==
literal|null
condition|?
literal|false
else|:
name|sourceASC
operator|.
name|shouldSkipCombine
argument_list|(
name|path
argument_list|,
name|conf
argument_list|)
return|;
block|}
block|}
end_class

end_unit

