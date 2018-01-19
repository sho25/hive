begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|Iterator
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
name|Map
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
name|ql
operator|.
name|metadata
operator|.
name|HiveStorageHandler
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorConverters
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|typeinfo
operator|.
name|TypeInfoUtils
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
name|hadoop
operator|.
name|util
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
name|hive
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
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatFieldSchema
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
name|schema
operator|.
name|HCatSchema
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|HCatBaseInputFormat
extends|extends
name|InputFormat
argument_list|<
name|WritableComparable
argument_list|,
name|HCatRecord
argument_list|>
block|{
comment|/**    * get the schema for the HCatRecord data returned by HCatInputFormat.    *    * @param context the jobContext    * @throws IllegalArgumentException    */
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|inputFileFormatClass
decl_stmt|;
comment|// TODO needs to go in InitializeInput? as part of InputJobInfo
specifier|private
specifier|static
name|HCatSchema
name|getOutputSchema
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|os
init|=
name|conf
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
name|os
operator|==
literal|null
condition|)
block|{
return|return
name|getTableSchema
argument_list|(
name|conf
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|(
name|HCatSchema
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|os
argument_list|)
return|;
block|}
block|}
comment|/**    * Set the schema for the HCatRecord data returned by HCatInputFormat.    * @param job the job object    * @param hcatSchema the schema to use as the consolidated schema    */
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
name|IOException
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
specifier|protected
specifier|static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputFormat
argument_list|<
name|WritableComparable
argument_list|,
name|Writable
argument_list|>
name|getMapRedInputFormat
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|Class
name|inputFormatClass
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputFormat
argument_list|<
name|WritableComparable
argument_list|,
name|Writable
argument_list|>
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|inputFormatClass
argument_list|,
name|job
argument_list|)
return|;
block|}
comment|/**    * Logically split the set of input files for the job. Returns the    * underlying InputFormat's splits    * @param jobContext the job context object    * @return the splits, an HCatInputSplit wrapper over the storage    *         handler InputSplits    * @throws IOException or InterruptedException    */
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
name|Configuration
name|conf
init|=
name|jobContext
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// Set up recursive reads for sub-directories.
comment|// (Otherwise, sub-directories produced by Hive UNION operations won't be readable.)
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"mapred.input.dir.recursive"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|//Get the job info from the configuration,
comment|//throws exception if not initialized
name|InputJobInfo
name|inputJobInfo
decl_stmt|;
try|try
block|{
name|inputJobInfo
operator|=
name|getJobInfo
argument_list|(
name|conf
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
name|inputJobInfo
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
name|HiveStorageHandler
name|storageHandler
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveProps
init|=
literal|null
decl_stmt|;
comment|//For each matching partition, call getSplits on the underlying InputFormat
for|for
control|(
name|PartInfo
name|partitionInfo
range|:
name|partitionInfoList
control|)
block|{
name|JobConf
name|jobConf
init|=
name|HCatUtil
operator|.
name|getJobConfFromContext
argument_list|(
name|jobContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|hiveProps
operator|==
literal|null
condition|)
block|{
name|hiveProps
operator|=
name|HCatUtil
operator|.
name|getHCatKeyHiveConf
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|setInputPath
init|=
name|setInputPath
argument_list|(
name|jobConf
argument_list|,
name|partitionInfo
operator|.
name|getLocation
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|setInputPath
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
init|=
name|partitionInfo
operator|.
name|getJobProperties
argument_list|()
decl_stmt|;
name|HCatUtil
operator|.
name|copyJobPropertiesToJobConf
argument_list|(
name|hiveProps
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
name|HCatUtil
operator|.
name|copyJobPropertiesToJobConf
argument_list|(
name|jobProperties
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
name|storageHandler
operator|=
name|HCatUtil
operator|.
name|getStorageHandler
argument_list|(
name|jobConf
argument_list|,
name|partitionInfo
argument_list|)
expr_stmt|;
comment|//Get the input format
name|Class
name|inputFormatClass
init|=
name|storageHandler
operator|.
name|getInputFormatClass
argument_list|()
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputFormat
name|inputFormat
init|=
name|getMapRedInputFormat
argument_list|(
name|jobConf
argument_list|,
name|inputFormatClass
argument_list|)
decl_stmt|;
comment|//Call getSplit on the InputFormat, create an HCatSplit for each
comment|//underlying split. When the desired number of input splits is missing,
comment|//use a default number (denoted by zero).
comment|//TODO(malewicz): Currently each partition is split independently into
comment|//a desired number. However, we want the union of all partitions to be
comment|//split into a desired number while maintaining balanced sizes of input
comment|//splits.
name|int
name|desiredNumSplits
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DESIRED_PARTITION_NUM_SPLITS
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
index|[]
name|baseSplits
init|=
name|inputFormat
operator|.
name|getSplits
argument_list|(
name|jobConf
argument_list|,
name|desiredNumSplits
argument_list|)
decl_stmt|;
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
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|splits
return|;
block|}
comment|/**    * Create the RecordReader for the given InputSplit. Returns the underlying    * RecordReader if the required operations are supported and schema matches    * with HCatTable schema. Returns an HCatRecordReader if operations need to    * be implemented in HCat.    * @param split the split    * @param taskContext the task attempt context    * @return the record reader instance, either an HCatRecordReader(later) or    *         the underlying storage handler's RecordReader    * @throws IOException or InterruptedException    */
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
name|hcatSplit
init|=
name|InternalUtil
operator|.
name|castToHCatSplit
argument_list|(
name|split
argument_list|)
decl_stmt|;
name|PartInfo
name|partitionInfo
init|=
name|hcatSplit
operator|.
name|getPartitionInfo
argument_list|()
decl_stmt|;
comment|// Ensure PartInfo's TableInfo is initialized.
if|if
condition|(
name|partitionInfo
operator|.
name|getTableInfo
argument_list|()
operator|==
literal|null
condition|)
block|{
name|partitionInfo
operator|.
name|setTableInfo
argument_list|(
operator|(
operator|(
name|InputJobInfo
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|taskContext
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
argument_list|)
operator|)
operator|.
name|getTableInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|JobContext
name|jobContext
init|=
name|taskContext
decl_stmt|;
name|Configuration
name|conf
init|=
name|jobContext
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|HiveStorageHandler
name|storageHandler
init|=
name|HCatUtil
operator|.
name|getStorageHandler
argument_list|(
name|conf
argument_list|,
name|partitionInfo
argument_list|)
decl_stmt|;
name|JobConf
name|jobConf
init|=
name|HCatUtil
operator|.
name|getJobConfFromContext
argument_list|(
name|jobContext
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
init|=
name|partitionInfo
operator|.
name|getJobProperties
argument_list|()
decl_stmt|;
name|HCatUtil
operator|.
name|copyJobPropertiesToJobConf
argument_list|(
name|jobProperties
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|valuesNotInDataCols
init|=
name|getColValsNotInDataColumns
argument_list|(
name|getOutputSchema
argument_list|(
name|conf
argument_list|)
argument_list|,
name|partitionInfo
argument_list|)
decl_stmt|;
return|return
operator|new
name|HCatRecordReader
argument_list|(
name|storageHandler
argument_list|,
name|valuesNotInDataCols
argument_list|)
return|;
block|}
comment|/**    * gets values for fields requested by output schema which will not be in the data    */
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getColValsNotInDataColumns
parameter_list|(
name|HCatSchema
name|outputSchema
parameter_list|,
name|PartInfo
name|partInfo
parameter_list|)
throws|throws
name|HCatException
block|{
name|HCatSchema
name|dataSchema
init|=
name|partInfo
operator|.
name|getPartitionSchema
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vals
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|fieldName
range|:
name|outputSchema
operator|.
name|getFieldNames
argument_list|()
control|)
block|{
if|if
condition|(
name|dataSchema
operator|.
name|getPosition
argument_list|(
name|fieldName
argument_list|)
operator|==
literal|null
condition|)
block|{
comment|// this entry of output is not present in the output schema
comment|// so, we first check the table schema to see if it is a part col
if|if
condition|(
name|partInfo
operator|.
name|getPartitionValues
argument_list|()
operator|.
name|containsKey
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
comment|// First, get the appropriate field schema for this field
name|HCatFieldSchema
name|fschema
init|=
name|outputSchema
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
comment|// For a partition key type, this will be a primitive typeinfo.
comment|// Obtain relevant object inspector for this typeinfo
name|ObjectInspector
name|oi
init|=
name|TypeInfoUtils
operator|.
name|getStandardJavaObjectInspectorFromTypeInfo
argument_list|(
name|fschema
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
decl_stmt|;
comment|// get appropriate object from the string representation of the value in partInfo.getPartitionValues()
comment|// Essentially, partition values are represented as strings, but we want the actual object type associated
name|Object
name|objVal
init|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|oi
argument_list|)
operator|.
name|convert
argument_list|(
name|partInfo
operator|.
name|getPartitionValues
argument_list|()
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
argument_list|)
decl_stmt|;
name|vals
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|objVal
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|vals
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|vals
return|;
block|}
comment|/**    * Gets the HCatTable schema for the table specified in the HCatInputFormat.setInput call    * on the specified job context. This information is available only after HCatInputFormat.setInput    * has been called for a JobContext.    * @param conf the Configuration object    * @return the table schema    * @throws IOException if HCatInputFormat.setInput has not been called    *                     for the current context    */
specifier|public
specifier|static
name|HCatSchema
name|getTableSchema
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|InputJobInfo
name|inputJobInfo
init|=
name|getJobInfo
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HCatSchema
name|allCols
init|=
operator|new
name|HCatSchema
argument_list|(
operator|new
name|LinkedList
argument_list|<
name|HCatFieldSchema
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HCatFieldSchema
name|field
range|:
name|inputJobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getDataColumns
argument_list|()
operator|.
name|getFields
argument_list|()
control|)
block|{
name|allCols
operator|.
name|append
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|HCatFieldSchema
name|field
range|:
name|inputJobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getPartitionColumns
argument_list|()
operator|.
name|getFields
argument_list|()
control|)
block|{
name|allCols
operator|.
name|append
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
return|return
name|allCols
return|;
block|}
comment|/**    * Gets the InputJobInfo object by reading the Configuration and deserializing    * the string. If InputJobInfo is not present in the configuration, throws an    * exception since that means HCatInputFormat.setInput has not been called.    * @param conf the Configuration object    * @return the InputJobInfo object    * @throws IOException the exception    */
specifier|private
specifier|static
name|InputJobInfo
name|getJobInfo
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|jobString
init|=
name|conf
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
name|IOException
argument_list|(
literal|"job information not found in JobContext."
operator|+
literal|" HCatInputFormat.setInput() not called?"
argument_list|)
throw|;
block|}
return|return
operator|(
name|InputJobInfo
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|jobString
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|setInputPath
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|IOException
block|{
comment|// ideally we should just call FileInputFormat.setInputPaths() here - but
comment|// that won't work since FileInputFormat.setInputPaths() needs
comment|// a Job object instead of a JobContext which we are handed here
name|int
name|length
init|=
name|location
operator|.
name|length
argument_list|()
decl_stmt|;
name|int
name|curlyOpen
init|=
literal|0
decl_stmt|;
name|int
name|pathStart
init|=
literal|0
decl_stmt|;
name|boolean
name|globPattern
init|=
literal|false
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|pathStrings
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
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
name|char
name|ch
init|=
name|location
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|ch
condition|)
block|{
case|case
literal|'{'
case|:
block|{
name|curlyOpen
operator|++
expr_stmt|;
if|if
condition|(
operator|!
name|globPattern
condition|)
block|{
name|globPattern
operator|=
literal|true
expr_stmt|;
block|}
break|break;
block|}
case|case
literal|'}'
case|:
block|{
name|curlyOpen
operator|--
expr_stmt|;
if|if
condition|(
name|curlyOpen
operator|==
literal|0
operator|&&
name|globPattern
condition|)
block|{
name|globPattern
operator|=
literal|false
expr_stmt|;
block|}
break|break;
block|}
case|case
literal|','
case|:
block|{
if|if
condition|(
operator|!
name|globPattern
condition|)
block|{
name|pathStrings
operator|.
name|add
argument_list|(
name|location
operator|.
name|substring
argument_list|(
name|pathStart
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|pathStart
operator|=
name|i
operator|+
literal|1
expr_stmt|;
block|}
break|break;
block|}
block|}
block|}
name|pathStrings
operator|.
name|add
argument_list|(
name|location
operator|.
name|substring
argument_list|(
name|pathStart
argument_list|,
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|separator
init|=
literal|""
decl_stmt|;
name|StringBuilder
name|str
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|boolean
name|ignoreInvalidPath
init|=
name|jobConf
operator|.
name|getBoolean
argument_list|(
name|HCatConstants
operator|.
name|HCAT_INPUT_IGNORE_INVALID_PATH_KEY
argument_list|,
name|HCatConstants
operator|.
name|HCAT_INPUT_IGNORE_INVALID_PATH_DEFAULT
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|pathIterator
init|=
name|pathStrings
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|pathIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|pathString
init|=
name|pathIterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|ignoreInvalidPath
operator|&&
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|pathString
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|pathString
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
if|if
condition|(
name|ignoreInvalidPath
operator|&&
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|pathIterator
operator|.
name|remove
argument_list|()
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|qualifiedPath
init|=
name|fs
operator|.
name|makeQualified
argument_list|(
name|path
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|str
operator|.
name|append
argument_list|(
name|separator
argument_list|)
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|escapeString
argument_list|(
name|qualifiedPath
argument_list|)
argument_list|)
expr_stmt|;
name|separator
operator|=
name|StringUtils
operator|.
name|COMMA_STR
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|ignoreInvalidPath
operator|||
operator|!
name|pathStrings
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|jobConf
operator|.
name|set
argument_list|(
literal|"mapred.input.dir"
argument_list|,
name|str
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|pathStrings
return|;
block|}
block|}
end_class

end_unit

