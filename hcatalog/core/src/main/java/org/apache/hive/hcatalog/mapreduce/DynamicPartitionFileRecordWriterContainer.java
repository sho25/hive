begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|mapreduce
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
name|serde2
operator|.
name|SerDe
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
name|SerDeException
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
name|objectinspector
operator|.
name|ObjectInspector
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
name|Reporter
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
name|RecordWriter
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
name|mapreduce
operator|.
name|JobContext
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
name|mapreduce
operator|.
name|OutputCommitter
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
name|mapreduce
operator|.
name|TaskAttemptContext
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
name|mapreduce
operator|.
name|lib
operator|.
name|output
operator|.
name|FileOutputCommitter
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
name|mapreduce
operator|.
name|lib
operator|.
name|output
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
name|util
operator|.
name|ReflectionUtils
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
name|hcatalog
operator|.
name|common
operator|.
name|ErrorType
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
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
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
name|hcatalog
operator|.
name|data
operator|.
name|HCatRecord
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

begin_comment
comment|/**  * Record writer container for tables using dynamic partitioning. See  * {@link FileOutputFormatContainer} for more information  */
end_comment

begin_class
class|class
name|DynamicPartitionFileRecordWriterContainer
extends|extends
name|FileRecordWriterContainer
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
name|DynamicPartitionFileRecordWriterContainer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|dynamicPartCols
decl_stmt|;
specifier|private
name|int
name|maxDynamicPartitions
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|RecordWriter
argument_list|<
name|?
super|super
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|?
super|super
name|Writable
argument_list|>
argument_list|>
name|baseDynamicWriters
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|SerDe
argument_list|>
name|baseDynamicSerDe
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|OutputCommitter
argument_list|>
name|baseDynamicCommitters
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|TaskAttemptContext
argument_list|>
name|dynamicContexts
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ObjectInspector
argument_list|>
name|dynamicObjectInspectors
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|OutputJobInfo
argument_list|>
name|dynamicOutputJobInfo
decl_stmt|;
comment|/**    * @param baseWriter RecordWriter to contain    * @param context current TaskAttemptContext    * @throws IOException    * @throws InterruptedException    */
specifier|public
name|DynamicPartitionFileRecordWriterContainer
parameter_list|(
name|RecordWriter
argument_list|<
name|?
super|super
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|?
super|super
name|Writable
argument_list|>
name|baseWriter
parameter_list|,
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|super
argument_list|(
name|baseWriter
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|maxDynamicPartitions
operator|=
name|jobInfo
operator|.
name|getMaxDynamicPartitions
argument_list|()
expr_stmt|;
name|dynamicPartCols
operator|=
name|jobInfo
operator|.
name|getPosOfDynPartCols
argument_list|()
expr_stmt|;
if|if
condition|(
name|dynamicPartCols
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"It seems that setSchema() is not called on "
operator|+
literal|"HCatOutputFormat. Please make sure that method is called."
argument_list|)
throw|;
block|}
name|this
operator|.
name|baseDynamicSerDe
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|SerDe
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|baseDynamicWriters
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|RecordWriter
argument_list|<
name|?
super|super
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|?
super|super
name|Writable
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|baseDynamicCommitters
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|OutputCommitter
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|dynamicContexts
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|TaskAttemptContext
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|dynamicObjectInspectors
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ObjectInspector
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|dynamicOutputJobInfo
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|OutputJobInfo
argument_list|>
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Reporter
name|reporter
init|=
name|InternalUtil
operator|.
name|createReporter
argument_list|(
name|context
argument_list|)
decl_stmt|;
for|for
control|(
name|RecordWriter
argument_list|<
name|?
super|super
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|?
super|super
name|Writable
argument_list|>
name|bwriter
range|:
name|baseDynamicWriters
operator|.
name|values
argument_list|()
control|)
block|{
comment|// We are in RecordWriter.close() make sense that the context would be
comment|// TaskInputOutput.
name|bwriter
operator|.
name|close
argument_list|(
name|reporter
argument_list|)
expr_stmt|;
block|}
name|TaskCommitContextRegistry
operator|.
name|getInstance
argument_list|()
operator|.
name|register
argument_list|(
name|context
argument_list|,
operator|new
name|TaskCommitContextRegistry
operator|.
name|TaskCommitterProxy
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|abortTask
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|OutputJobInfo
argument_list|>
name|outputJobInfoEntry
range|:
name|dynamicOutputJobInfo
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|dynKey
init|=
name|outputJobInfoEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|OutputJobInfo
name|outputJobInfo
init|=
name|outputJobInfoEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Aborting task-attempt for "
operator|+
name|outputJobInfo
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|baseDynamicCommitters
operator|.
name|get
argument_list|(
name|dynKey
argument_list|)
operator|.
name|abortTask
argument_list|(
name|dynamicContexts
operator|.
name|get
argument_list|(
name|dynKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitTask
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|OutputJobInfo
argument_list|>
name|outputJobInfoEntry
range|:
name|dynamicOutputJobInfo
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|dynKey
init|=
name|outputJobInfoEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|OutputJobInfo
name|outputJobInfo
init|=
name|outputJobInfoEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Committing task-attempt for "
operator|+
name|outputJobInfo
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|TaskAttemptContext
name|dynContext
init|=
name|dynamicContexts
operator|.
name|get
argument_list|(
name|dynKey
argument_list|)
decl_stmt|;
name|OutputCommitter
name|dynCommitter
init|=
name|baseDynamicCommitters
operator|.
name|get
argument_list|(
name|dynKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|dynCommitter
operator|.
name|needsTaskCommit
argument_list|(
name|dynContext
argument_list|)
condition|)
block|{
name|dynCommitter
operator|.
name|commitTask
argument_list|(
name|dynContext
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipping commitTask() for "
operator|+
name|outputJobInfo
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|LocalFileWriter
name|getLocalFileWriter
parameter_list|(
name|HCatRecord
name|value
parameter_list|)
throws|throws
name|IOException
throws|,
name|HCatException
block|{
name|OutputJobInfo
name|localJobInfo
init|=
literal|null
decl_stmt|;
comment|// Calculate which writer to use from the remaining values - this needs to
comment|// be done before we delete cols.
name|List
argument_list|<
name|String
argument_list|>
name|dynamicPartValues
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Integer
name|colToAppend
range|:
name|dynamicPartCols
control|)
block|{
name|dynamicPartValues
operator|.
name|add
argument_list|(
name|value
operator|.
name|get
argument_list|(
name|colToAppend
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|dynKey
init|=
name|dynamicPartValues
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|baseDynamicWriters
operator|.
name|containsKey
argument_list|(
name|dynKey
argument_list|)
condition|)
block|{
if|if
condition|(
operator|(
name|maxDynamicPartitions
operator|!=
operator|-
literal|1
operator|)
operator|&&
operator|(
name|baseDynamicWriters
operator|.
name|size
argument_list|()
operator|>
name|maxDynamicPartitions
operator|)
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_TOO_MANY_DYNAMIC_PTNS
argument_list|,
literal|"Number of dynamic partitions being created "
operator|+
literal|"exceeds configured max allowable partitions["
operator|+
name|maxDynamicPartitions
operator|+
literal|"], increase parameter ["
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DYNAMICPARTITIONMAXPARTS
operator|.
name|varname
operator|+
literal|"] if needed."
argument_list|)
throw|;
block|}
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|TaskAttemptContext
name|currTaskContext
init|=
name|HCatMapRedUtil
operator|.
name|createTaskAttemptContext
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|configureDynamicStorageHandler
argument_list|(
name|currTaskContext
argument_list|,
name|dynamicPartValues
argument_list|)
expr_stmt|;
name|localJobInfo
operator|=
name|HCatBaseOutputFormat
operator|.
name|getJobInfo
argument_list|(
name|currTaskContext
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
comment|// Setup serDe.
name|SerDe
name|currSerDe
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|storageHandler
operator|.
name|getSerDeClass
argument_list|()
argument_list|,
name|currTaskContext
operator|.
name|getJobConf
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|InternalUtil
operator|.
name|initializeOutputSerDe
argument_list|(
name|currSerDe
argument_list|,
name|currTaskContext
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|localJobInfo
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to initialize SerDe"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// create base OutputFormat
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|OutputFormat
name|baseOF
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|storageHandler
operator|.
name|getOutputFormatClass
argument_list|()
argument_list|,
name|currTaskContext
operator|.
name|getJobConf
argument_list|()
argument_list|)
decl_stmt|;
comment|// We are skipping calling checkOutputSpecs() for each partition
comment|// As it can throw a FileAlreadyExistsException when more than one
comment|// mapper is writing to a partition.
comment|// See HCATALOG-490, also to avoid contacting the namenode for each new
comment|// FileOutputFormat instance.
comment|// In general this should be ok for most FileOutputFormat implementations
comment|// but may become an issue for cases when the method is used to perform
comment|// other setup tasks.
comment|// Get Output Committer
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|OutputCommitter
name|baseOutputCommitter
init|=
name|currTaskContext
operator|.
name|getJobConf
argument_list|()
operator|.
name|getOutputCommitter
argument_list|()
decl_stmt|;
comment|// Create currJobContext the latest so it gets all the config changes
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobContext
name|currJobContext
init|=
name|HCatMapRedUtil
operator|.
name|createJobContext
argument_list|(
name|currTaskContext
argument_list|)
decl_stmt|;
comment|// Set up job.
name|baseOutputCommitter
operator|.
name|setupJob
argument_list|(
name|currJobContext
argument_list|)
expr_stmt|;
comment|// Recreate to refresh jobConf of currTask context.
name|currTaskContext
operator|=
name|HCatMapRedUtil
operator|.
name|createTaskAttemptContext
argument_list|(
name|currJobContext
operator|.
name|getJobConf
argument_list|()
argument_list|,
name|currTaskContext
operator|.
name|getTaskAttemptID
argument_list|()
argument_list|,
name|currTaskContext
operator|.
name|getProgressible
argument_list|()
argument_list|)
expr_stmt|;
comment|// Set temp location.
name|currTaskContext
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
literal|"mapred.work.output.dir"
argument_list|,
operator|new
name|FileOutputCommitter
argument_list|(
operator|new
name|Path
argument_list|(
name|localJobInfo
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|,
name|currTaskContext
argument_list|)
operator|.
name|getWorkPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Set up task.
name|baseOutputCommitter
operator|.
name|setupTask
argument_list|(
name|currTaskContext
argument_list|)
expr_stmt|;
name|Path
name|parentDir
init|=
operator|new
name|Path
argument_list|(
name|currTaskContext
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
literal|"mapred.work.output.dir"
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|childPath
init|=
operator|new
name|Path
argument_list|(
name|parentDir
argument_list|,
name|FileOutputFormat
operator|.
name|getUniqueFile
argument_list|(
name|currTaskContext
argument_list|,
name|currTaskContext
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
literal|"mapreduce.output.basename"
argument_list|,
literal|"part"
argument_list|)
argument_list|,
literal|""
argument_list|)
argument_list|)
decl_stmt|;
name|RecordWriter
name|baseRecordWriter
init|=
name|baseOF
operator|.
name|getRecordWriter
argument_list|(
name|parentDir
operator|.
name|getFileSystem
argument_list|(
name|currTaskContext
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|currTaskContext
operator|.
name|getJobConf
argument_list|()
argument_list|,
name|childPath
operator|.
name|toString
argument_list|()
argument_list|,
name|InternalUtil
operator|.
name|createReporter
argument_list|(
name|currTaskContext
argument_list|)
argument_list|)
decl_stmt|;
name|baseDynamicWriters
operator|.
name|put
argument_list|(
name|dynKey
argument_list|,
name|baseRecordWriter
argument_list|)
expr_stmt|;
name|baseDynamicSerDe
operator|.
name|put
argument_list|(
name|dynKey
argument_list|,
name|currSerDe
argument_list|)
expr_stmt|;
name|baseDynamicCommitters
operator|.
name|put
argument_list|(
name|dynKey
argument_list|,
name|baseOutputCommitter
argument_list|)
expr_stmt|;
name|dynamicContexts
operator|.
name|put
argument_list|(
name|dynKey
argument_list|,
name|currTaskContext
argument_list|)
expr_stmt|;
name|dynamicObjectInspectors
operator|.
name|put
argument_list|(
name|dynKey
argument_list|,
name|InternalUtil
operator|.
name|createStructObjectInspector
argument_list|(
name|jobInfo
operator|.
name|getOutputSchema
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|dynamicOutputJobInfo
operator|.
name|put
argument_list|(
name|dynKey
argument_list|,
name|HCatOutputFormat
operator|.
name|getJobInfo
argument_list|(
name|dynamicContexts
operator|.
name|get
argument_list|(
name|dynKey
argument_list|)
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|LocalFileWriter
argument_list|(
name|baseDynamicWriters
operator|.
name|get
argument_list|(
name|dynKey
argument_list|)
argument_list|,
name|dynamicObjectInspectors
operator|.
name|get
argument_list|(
name|dynKey
argument_list|)
argument_list|,
name|baseDynamicSerDe
operator|.
name|get
argument_list|(
name|dynKey
argument_list|)
argument_list|,
name|dynamicOutputJobInfo
operator|.
name|get
argument_list|(
name|dynKey
argument_list|)
argument_list|)
return|;
block|}
specifier|protected
name|void
name|configureDynamicStorageHandler
parameter_list|(
name|JobContext
name|context
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|dynamicPartVals
parameter_list|)
throws|throws
name|IOException
block|{
name|HCatOutputFormat
operator|.
name|configureOutputStorageHandler
argument_list|(
name|context
argument_list|,
name|dynamicPartVals
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

