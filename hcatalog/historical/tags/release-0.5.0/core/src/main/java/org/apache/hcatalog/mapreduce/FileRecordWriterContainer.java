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
name|HCatMapRedUtil
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
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|HCatRecord
import|;
end_import

begin_comment
comment|/**  * Part of the FileOutput*Container classes  * See {@link FileOutputFormatContainer} for more information  */
end_comment

begin_class
class|class
name|FileRecordWriterContainer
extends|extends
name|RecordWriterContainer
block|{
specifier|private
specifier|final
name|HCatStorageHandler
name|storageHandler
decl_stmt|;
specifier|private
specifier|final
name|SerDe
name|serDe
decl_stmt|;
specifier|private
specifier|final
name|ObjectInspector
name|objectInspector
decl_stmt|;
specifier|private
name|boolean
name|dynamicPartitioningUsed
init|=
literal|false
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
specifier|private
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|partColsToDel
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
name|OutputJobInfo
name|jobInfo
decl_stmt|;
specifier|private
name|TaskAttemptContext
name|context
decl_stmt|;
comment|/**      * @param baseWriter RecordWriter to contain      * @param context current TaskAttemptContext      * @throws IOException      * @throws InterruptedException      */
specifier|public
name|FileRecordWriterContainer
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
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
name|context
argument_list|,
name|baseWriter
argument_list|)
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|jobInfo
operator|=
name|HCatOutputFormat
operator|.
name|getJobInfo
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|storageHandler
operator|=
name|HCatUtil
operator|.
name|getStorageHandler
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getStorerInfo
argument_list|()
argument_list|)
expr_stmt|;
name|serDe
operator|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|storageHandler
operator|.
name|getSerDeClass
argument_list|()
argument_list|,
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|objectInspector
operator|=
name|InternalUtil
operator|.
name|createStructObjectInspector
argument_list|(
name|jobInfo
operator|.
name|getOutputSchema
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|InternalUtil
operator|.
name|initializeOutputSerDe
argument_list|(
name|serDe
argument_list|,
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|jobInfo
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
literal|"Failed to inialize SerDe"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// If partition columns occur in data, we want to remove them.
name|partColsToDel
operator|=
name|jobInfo
operator|.
name|getPosOfPartCols
argument_list|()
expr_stmt|;
name|dynamicPartitioningUsed
operator|=
name|jobInfo
operator|.
name|isDynamicPartitioningUsed
argument_list|()
expr_stmt|;
name|dynamicPartCols
operator|=
name|jobInfo
operator|.
name|getPosOfDynPartCols
argument_list|()
expr_stmt|;
name|maxDynamicPartitions
operator|=
name|jobInfo
operator|.
name|getMaxDynamicPartitions
argument_list|()
expr_stmt|;
if|if
condition|(
operator|(
name|partColsToDel
operator|==
literal|null
operator|)
operator|||
operator|(
name|dynamicPartitioningUsed
operator|&&
operator|(
name|dynamicPartCols
operator|==
literal|null
operator|)
operator|)
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
if|if
condition|(
operator|!
name|dynamicPartitioningUsed
condition|)
block|{
name|this
operator|.
name|baseDynamicSerDe
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|baseDynamicWriters
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|baseDynamicCommitters
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|dynamicContexts
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|dynamicObjectInspectors
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|dynamicOutputJobInfo
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
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
block|}
comment|/**      * @return the storagehandler      */
specifier|public
name|HCatStorageHandler
name|getStorageHandler
parameter_list|()
block|{
return|return
name|storageHandler
return|;
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
if|if
condition|(
name|dynamicPartitioningUsed
condition|)
block|{
for|for
control|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
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
comment|//We are in RecordWriter.close() make sense that the context would be TaskInputOutput
name|bwriter
operator|.
name|close
argument_list|(
name|reporter
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
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
name|entry
range|:
name|baseDynamicCommitters
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|TaskAttemptContext
name|currContext
init|=
name|dynamicContexts
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|OutputCommitter
name|baseOutputCommitter
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|baseOutputCommitter
operator|.
name|needsTaskCommit
argument_list|(
name|currContext
argument_list|)
condition|)
block|{
name|baseOutputCommitter
operator|.
name|commitTask
argument_list|(
name|currContext
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|getBaseRecordWriter
argument_list|()
operator|.
name|close
argument_list|(
name|reporter
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|WritableComparable
argument_list|<
name|?
argument_list|>
name|key
parameter_list|,
name|HCatRecord
name|value
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|RecordWriter
name|localWriter
decl_stmt|;
name|ObjectInspector
name|localObjectInspector
decl_stmt|;
name|SerDe
name|localSerDe
decl_stmt|;
name|OutputJobInfo
name|localJobInfo
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|dynamicPartitioningUsed
condition|)
block|{
comment|// calculate which writer to use from the remaining values - this needs to be done before we delete cols
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
argument_list|)
expr_stmt|;
comment|//setup serDe
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
comment|//create base OutputFormat
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
comment|//We are skipping calling checkOutputSpecs() for each partition
comment|//As it can throw a FileAlreadyExistsException when more than one mapper is writing to a partition
comment|//See HCATALOG-490, also to avoid contacting the namenode for each new FileOutputFormat instance
comment|//In general this should be ok for most FileOutputFormat implementations
comment|//but may become an issue for cases when the method is used to perform other setup tasks
comment|//get Output Committer
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
comment|//create currJobContext the latest so it gets all the config changes
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
comment|//setupJob()
name|baseOutputCommitter
operator|.
name|setupJob
argument_list|(
name|currJobContext
argument_list|)
expr_stmt|;
comment|//recreate to refresh jobConf of currTask context
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
comment|//set temp location
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
comment|//setupTask()
name|baseOutputCommitter
operator|.
name|setupTask
argument_list|(
name|currTaskContext
argument_list|)
expr_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|RecordWriter
name|baseRecordWriter
init|=
name|baseOF
operator|.
name|getRecordWriter
argument_list|(
literal|null
argument_list|,
name|currTaskContext
operator|.
name|getJobConf
argument_list|()
argument_list|,
name|FileOutputFormat
operator|.
name|getUniqueFile
argument_list|(
name|currTaskContext
argument_list|,
literal|"part"
argument_list|,
literal|""
argument_list|)
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
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|localJobInfo
operator|=
name|dynamicOutputJobInfo
operator|.
name|get
argument_list|(
name|dynKey
argument_list|)
expr_stmt|;
name|localWriter
operator|=
name|baseDynamicWriters
operator|.
name|get
argument_list|(
name|dynKey
argument_list|)
expr_stmt|;
name|localSerDe
operator|=
name|baseDynamicSerDe
operator|.
name|get
argument_list|(
name|dynKey
argument_list|)
expr_stmt|;
name|localObjectInspector
operator|=
name|dynamicObjectInspectors
operator|.
name|get
argument_list|(
name|dynKey
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|localJobInfo
operator|=
name|jobInfo
expr_stmt|;
name|localWriter
operator|=
name|getBaseRecordWriter
argument_list|()
expr_stmt|;
name|localSerDe
operator|=
name|serDe
expr_stmt|;
name|localObjectInspector
operator|=
name|objectInspector
expr_stmt|;
block|}
for|for
control|(
name|Integer
name|colToDel
range|:
name|partColsToDel
control|)
block|{
name|value
operator|.
name|remove
argument_list|(
name|colToDel
argument_list|)
expr_stmt|;
block|}
comment|//The key given by user is ignored
try|try
block|{
name|localWriter
operator|.
name|write
argument_list|(
name|NullWritable
operator|.
name|get
argument_list|()
argument_list|,
name|localSerDe
operator|.
name|serialize
argument_list|(
name|value
operator|.
name|getAll
argument_list|()
argument_list|,
name|localObjectInspector
argument_list|)
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
literal|"Failed to serialize object"
argument_list|,
name|e
argument_list|)
throw|;
block|}
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

