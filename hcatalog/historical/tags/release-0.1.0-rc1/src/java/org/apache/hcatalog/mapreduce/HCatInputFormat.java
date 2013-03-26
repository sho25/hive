begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
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
name|mapreduce
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
name|mapreduce
operator|.
name|Job
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
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
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
name|schema
operator|.
name|HCatSchema
import|;
end_import

begin_comment
comment|/** The InputFormat to use to read data from Howl */
end_comment

begin_class
specifier|public
class|class
name|HCatInputFormat
extends|extends
name|InputFormat
argument_list|<
name|WritableComparable
argument_list|,
name|HCatRecord
argument_list|>
block|{
comment|/**    * Set the input to use for the Job. This queries the metadata server with    * the specified partition predicates, gets the matching partitions, puts    * the information in the conf object. The inputInfo object is updated with    * information needed in the client context    * @param job the job object    * @param inputInfo the table input info    * @throws IOException the exception in communicating with the metadata server    */
specifier|public
specifier|static
name|void
name|setInput
parameter_list|(
name|Job
name|job
parameter_list|,
name|HCatTableInfo
name|inputInfo
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|InitializeInput
operator|.
name|setInput
argument_list|(
name|job
argument_list|,
name|inputInfo
argument_list|)
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Set the schema for the HowlRecord data returned by HowlInputFormat.    * @param job the job object    * @param hcatSchema the schema to use as the consolidated schema    */
specifier|public
specifier|static
name|void
name|setOutputSchema
parameter_list|(
name|Job
name|job
parameter_list|,
name|HCatSchema
name|hcatSchema
parameter_list|)
throws|throws
name|Exception
block|{
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_OUTPUT_SCHEMA
argument_list|,
name|HCatUtil
operator|.
name|serialize
argument_list|(
name|hcatSchema
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Logically split the set of input files for the job. Returns the    * underlying InputFormat's splits    * @param jobContext the job context object    * @return the splits, an HowlInputSplit wrapper over the storage    *         driver InputSplits    * @throws IOException or InterruptedException    */
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|InputSplit
argument_list|>
name|getSplits
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|//Get the job info from the configuration,
comment|//throws exception if not initialized
name|JobInfo
name|jobInfo
decl_stmt|;
try|try
block|{
name|jobInfo
operator|=
name|getJobInfo
argument_list|(
name|jobContext
argument_list|)
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
init|=
operator|new
name|ArrayList
argument_list|<
name|InputSplit
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|PartInfo
argument_list|>
name|partitionInfoList
init|=
name|jobInfo
operator|.
name|getPartitions
argument_list|()
decl_stmt|;
if|if
condition|(
name|partitionInfoList
operator|==
literal|null
condition|)
block|{
comment|//No partitions match the specified partition filter
return|return
name|splits
return|;
block|}
comment|//For each matching partition, call getSplits on the underlying InputFormat
for|for
control|(
name|PartInfo
name|partitionInfo
range|:
name|partitionInfoList
control|)
block|{
name|Job
name|localJob
init|=
operator|new
name|Job
argument_list|(
name|jobContext
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HCatInputStorageDriver
name|storageDriver
decl_stmt|;
try|try
block|{
name|storageDriver
operator|=
name|getInputDriverInstance
argument_list|(
name|partitionInfo
operator|.
name|getInputStorageDriverClass
argument_list|()
argument_list|)
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|//Pass all required information to the storage driver
name|initStorageDriver
argument_list|(
name|storageDriver
argument_list|,
name|localJob
argument_list|,
name|partitionInfo
argument_list|,
name|jobInfo
operator|.
name|getTableSchema
argument_list|()
argument_list|)
expr_stmt|;
comment|//Get the input format for the storage driver
name|InputFormat
name|inputFormat
init|=
name|storageDriver
operator|.
name|getInputFormat
argument_list|(
name|partitionInfo
operator|.
name|getInputStorageDriverProperties
argument_list|()
argument_list|)
decl_stmt|;
comment|//Call getSplit on the storage drivers InputFormat, create an
comment|//HCatSplit for each underlying split
name|List
argument_list|<
name|InputSplit
argument_list|>
name|baseSplits
init|=
name|inputFormat
operator|.
name|getSplits
argument_list|(
name|localJob
argument_list|)
decl_stmt|;
for|for
control|(
name|InputSplit
name|split
range|:
name|baseSplits
control|)
block|{
name|splits
operator|.
name|add
argument_list|(
operator|new
name|HCatSplit
argument_list|(
name|partitionInfo
argument_list|,
name|split
argument_list|,
name|jobInfo
operator|.
name|getTableSchema
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|splits
return|;
block|}
comment|/**    * Create the RecordReader for the given InputSplit. Returns the underlying    * RecordReader if the required operations are supported and schema matches    * with HowlTable schema. Returns an HowlRecordReader if operations need to    * be implemented in Howl.    * @param split the split    * @param taskContext the task attempt context    * @return the record reader instance, either an HowlRecordReader(later) or    *         the underlying storage driver's RecordReader    * @throws IOException or InterruptedException    */
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|WritableComparable
argument_list|,
name|HCatRecord
argument_list|>
name|createRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|TaskAttemptContext
name|taskContext
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|HCatSplit
name|howlSplit
init|=
operator|(
name|HCatSplit
operator|)
name|split
decl_stmt|;
name|PartInfo
name|partitionInfo
init|=
name|howlSplit
operator|.
name|getPartitionInfo
argument_list|()
decl_stmt|;
comment|//If running through a Pig job, the JobInfo will not be available in the
comment|//backend process context (since HowlLoader works on a copy of the JobContext and does
comment|//not call HowlInputFormat.setInput in the backend process).
comment|//So this function should NOT attempt to read the JobInfo.
name|HCatInputStorageDriver
name|storageDriver
decl_stmt|;
try|try
block|{
name|storageDriver
operator|=
name|getInputDriverInstance
argument_list|(
name|partitionInfo
operator|.
name|getInputStorageDriverClass
argument_list|()
argument_list|)
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|//Pass all required information to the storage driver
name|initStorageDriver
argument_list|(
name|storageDriver
argument_list|,
name|taskContext
argument_list|,
name|partitionInfo
argument_list|,
name|howlSplit
operator|.
name|getTableSchema
argument_list|()
argument_list|)
expr_stmt|;
comment|//Get the input format for the storage driver
name|InputFormat
name|inputFormat
init|=
name|storageDriver
operator|.
name|getInputFormat
argument_list|(
name|partitionInfo
operator|.
name|getInputStorageDriverProperties
argument_list|()
argument_list|)
decl_stmt|;
comment|//Create the underlying input formats record record and an Howl wrapper
name|RecordReader
name|recordReader
init|=
name|inputFormat
operator|.
name|createRecordReader
argument_list|(
name|howlSplit
operator|.
name|getBaseSplit
argument_list|()
argument_list|,
name|taskContext
argument_list|)
decl_stmt|;
return|return
operator|new
name|HCatRecordReader
argument_list|(
name|storageDriver
argument_list|,
name|recordReader
argument_list|)
return|;
block|}
comment|/**    * Gets the HowlTable schema for the table specified in the HowlInputFormat.setInput call    * on the specified job context. This information is available only after HowlInputFormat.setInput    * has been called for a JobContext.    * @param context the context    * @return the table schema    * @throws Exception if HowlInputFromat.setInput has not been called for the current context    */
specifier|public
specifier|static
name|HCatSchema
name|getTableSchema
parameter_list|(
name|JobContext
name|context
parameter_list|)
throws|throws
name|Exception
block|{
name|JobInfo
name|jobInfo
init|=
name|getJobInfo
argument_list|(
name|context
argument_list|)
decl_stmt|;
return|return
name|jobInfo
operator|.
name|getTableSchema
argument_list|()
return|;
block|}
comment|/**    * Gets the JobInfo object by reading the Configuration and deserializing    * the string. If JobInfo is not present in the configuration, throws an    * exception since that means HowlInputFormat.setInput has not been called.    * @param jobContext the job context    * @return the JobInfo object    * @throws Exception the exception    */
specifier|private
specifier|static
name|JobInfo
name|getJobInfo
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|jobString
init|=
name|jobContext
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_JOB_INFO
argument_list|)
decl_stmt|;
if|if
condition|(
name|jobString
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"job information not found in JobContext. HowlInputFormat.setInput() not called?"
argument_list|)
throw|;
block|}
return|return
operator|(
name|JobInfo
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|jobString
argument_list|)
return|;
block|}
comment|/**    * Initializes the storage driver instance. Passes on the required    * schema information, path info and arguments for the supported    * features to the storage driver.    * @param storageDriver the storage driver    * @param context the job context    * @param partitionInfo the partition info    * @param tableSchema the table level schema    * @throws IOException Signals that an I/O exception has occurred.    */
specifier|private
name|void
name|initStorageDriver
parameter_list|(
name|HCatInputStorageDriver
name|storageDriver
parameter_list|,
name|JobContext
name|context
parameter_list|,
name|PartInfo
name|partitionInfo
parameter_list|,
name|HCatSchema
name|tableSchema
parameter_list|)
throws|throws
name|IOException
block|{
name|storageDriver
operator|.
name|setInputPath
argument_list|(
name|context
argument_list|,
name|partitionInfo
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|partitionInfo
operator|.
name|getPartitionSchema
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|storageDriver
operator|.
name|setOriginalSchema
argument_list|(
name|context
argument_list|,
name|partitionInfo
operator|.
name|getPartitionSchema
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|storageDriver
operator|.
name|setPartitionValues
argument_list|(
name|context
argument_list|,
name|partitionInfo
operator|.
name|getPartitionValues
argument_list|()
argument_list|)
expr_stmt|;
comment|//Set the output schema. Use the schema given by user if set, otherwise use the
comment|//table level schema
name|HCatSchema
name|outputSchema
init|=
literal|null
decl_stmt|;
name|String
name|outputSchemaString
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_OUTPUT_SCHEMA
argument_list|)
decl_stmt|;
if|if
condition|(
name|outputSchemaString
operator|!=
literal|null
condition|)
block|{
name|outputSchema
operator|=
operator|(
name|HCatSchema
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|outputSchemaString
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputSchema
operator|=
name|tableSchema
expr_stmt|;
block|}
name|storageDriver
operator|.
name|setOutputSchema
argument_list|(
name|context
argument_list|,
name|outputSchema
argument_list|)
expr_stmt|;
name|storageDriver
operator|.
name|initialize
argument_list|(
name|context
argument_list|,
name|partitionInfo
operator|.
name|getInputStorageDriverProperties
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Gets the input driver instance.    * @param inputStorageDriverClass the input storage driver classname    * @return the input driver instance    * @throws Exception    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|HCatInputStorageDriver
name|getInputDriverInstance
parameter_list|(
name|String
name|inputStorageDriverClass
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|HCatInputStorageDriver
argument_list|>
name|driverClass
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HCatInputStorageDriver
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|inputStorageDriverClass
argument_list|)
decl_stmt|;
return|return
name|driverClass
operator|.
name|newInstance
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"error creating storage driver "
operator|+
name|inputStorageDriverClass
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

