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
name|fs
operator|.
name|permission
operator|.
name|FsPermission
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
name|metastore
operator|.
name|HiveMetaStoreClient
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|metastore
operator|.
name|api
operator|.
name|Index
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
name|metastore
operator|.
name|api
operator|.
name|StorageDescriptor
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
name|Table
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
name|security
operator|.
name|Credentials
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
name|HCatSchema
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
comment|/** The OutputFormat to use to write data to HCatalog. The key value is ignored and  *  should be given as null. The value is the HCatRecord to write.*/
end_comment

begin_class
specifier|public
class|class
name|HCatOutputFormat
extends|extends
name|HCatBaseOutputFormat
block|{
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HCatOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|int
name|maxDynamicPartitions
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|harRequested
decl_stmt|;
comment|/**    * @see org.apache.hive.hcatalog.mapreduce.HCatOutputFormat#setOutput(org.apache.hadoop.conf.Configuration, Credentials, OutputJobInfo)    */
specifier|public
specifier|static
name|void
name|setOutput
parameter_list|(
name|Job
name|job
parameter_list|,
name|OutputJobInfo
name|outputJobInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|setOutput
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|job
operator|.
name|getCredentials
argument_list|()
argument_list|,
name|outputJobInfo
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the information about the output to write for the job. This queries the metadata server    * to find the StorageHandler to use for the table.  It throws an error if the    * partition is already published.    * @param conf the Configuration object    * @param credentials the Credentials object    * @param outputJobInfo the table output information for the job    * @throws IOException the exception in communicating with the metadata server    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
name|void
name|setOutput
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Credentials
name|credentials
parameter_list|,
name|OutputJobInfo
name|outputJobInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|HiveMetaStoreClient
name|client
init|=
literal|null
decl_stmt|;
try|try
block|{
name|HiveConf
name|hiveConf
init|=
name|HCatUtil
operator|.
name|getHiveConf
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|client
operator|=
name|HCatUtil
operator|.
name|getHiveClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|HCatUtil
operator|.
name|getTable
argument_list|(
name|client
argument_list|,
name|outputJobInfo
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|outputJobInfo
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|indexList
init|=
name|client
operator|.
name|listIndexNames
argument_list|(
name|outputJobInfo
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|outputJobInfo
operator|.
name|getTableName
argument_list|()
argument_list|,
name|Short
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|indexName
range|:
name|indexList
control|)
block|{
name|Index
name|index
init|=
name|client
operator|.
name|getIndex
argument_list|(
name|outputJobInfo
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|outputJobInfo
operator|.
name|getTableName
argument_list|()
argument_list|,
name|indexName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|index
operator|.
name|isDeferredRebuild
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_SUPPORTED
argument_list|,
literal|"Store into a table with an automatic index from Pig/Mapreduce is not supported"
argument_list|)
throw|;
block|}
block|}
name|StorageDescriptor
name|sd
init|=
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getSd
argument_list|()
decl_stmt|;
if|if
condition|(
name|sd
operator|.
name|isCompressed
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_SUPPORTED
argument_list|,
literal|"Store into a compressed partition from Pig/Mapreduce is not supported"
argument_list|)
throw|;
block|}
if|if
condition|(
name|sd
operator|.
name|getBucketCols
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|sd
operator|.
name|getBucketCols
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_SUPPORTED
argument_list|,
literal|"Store into a partition with bucket definition from Pig/Mapreduce is not supported"
argument_list|)
throw|;
block|}
if|if
condition|(
name|sd
operator|.
name|getSortCols
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|sd
operator|.
name|getSortCols
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_SUPPORTED
argument_list|,
literal|"Store into a partition with sorted column definition from Pig/Mapreduce is not supported"
argument_list|)
throw|;
block|}
if|if
condition|(
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getPartitionKeysSize
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
operator|(
name|outputJobInfo
operator|.
name|getPartitionValues
argument_list|()
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|outputJobInfo
operator|.
name|getPartitionValues
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
comment|// attempt made to save partition values in non-partitioned table - throw error.
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_INVALID_PARTITION_VALUES
argument_list|,
literal|"Partition values specified for non-partitioned table"
argument_list|)
throw|;
block|}
comment|// non-partitioned table
name|outputJobInfo
operator|.
name|setPartitionValues
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// partitioned table, we expect partition values
comment|// convert user specified map to have lower case key names
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|valueMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|outputJobInfo
operator|.
name|getPartitionValues
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|outputJobInfo
operator|.
name|getPartitionValues
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|valueMap
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|(
name|outputJobInfo
operator|.
name|getPartitionValues
argument_list|()
operator|==
literal|null
operator|)
operator|||
operator|(
name|outputJobInfo
operator|.
name|getPartitionValues
argument_list|()
operator|.
name|size
argument_list|()
operator|<
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getPartitionKeysSize
argument_list|()
operator|)
condition|)
block|{
comment|// dynamic partition usecase - partition values were null, or not all were specified
comment|// need to figure out which keys are not specified.
name|List
argument_list|<
name|String
argument_list|>
name|dynamicPartitioningKeys
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|firstItem
init|=
literal|true
decl_stmt|;
for|for
control|(
name|FieldSchema
name|fs
range|:
name|table
operator|.
name|getPartitionKeys
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|valueMap
operator|.
name|containsKey
argument_list|(
name|fs
operator|.
name|getName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
condition|)
block|{
name|dynamicPartitioningKeys
operator|.
name|add
argument_list|(
name|fs
operator|.
name|getName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|valueMap
operator|.
name|size
argument_list|()
operator|+
name|dynamicPartitioningKeys
operator|.
name|size
argument_list|()
operator|!=
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getPartitionKeysSize
argument_list|()
condition|)
block|{
comment|// If this isn't equal, then bogus key values have been inserted, error out.
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_INVALID_PARTITION_VALUES
argument_list|,
literal|"Invalid partition keys specified"
argument_list|)
throw|;
block|}
name|outputJobInfo
operator|.
name|setDynamicPartitioningKeys
argument_list|(
name|dynamicPartitioningKeys
argument_list|)
expr_stmt|;
name|String
name|dynHash
decl_stmt|;
if|if
condition|(
operator|(
name|dynHash
operator|=
name|conf
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DYNAMIC_PTN_JOBID
argument_list|)
operator|)
operator|==
literal|null
condition|)
block|{
name|dynHash
operator|=
name|String
operator|.
name|valueOf
argument_list|(
name|Math
operator|.
name|random
argument_list|()
argument_list|)
expr_stmt|;
comment|//              LOG.info("New dynHash : ["+dynHash+"]");
comment|//            }else{
comment|//              LOG.info("Old dynHash : ["+dynHash+"]");
block|}
name|conf
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DYNAMIC_PTN_JOBID
argument_list|,
name|dynHash
argument_list|)
expr_stmt|;
block|}
name|outputJobInfo
operator|.
name|setPartitionValues
argument_list|(
name|valueMap
argument_list|)
expr_stmt|;
block|}
comment|// To get around hbase failure on single node, see BUG-4383
name|conf
operator|.
name|set
argument_list|(
literal|"dfs.client.read.shortcircuit"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|HCatSchema
name|tableSchema
init|=
name|HCatUtil
operator|.
name|extractSchema
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|StorerInfo
name|storerInfo
init|=
name|InternalUtil
operator|.
name|extractStorerInfo
argument_list|(
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getSd
argument_list|()
argument_list|,
name|table
operator|.
name|getParameters
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partitionCols
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
name|FieldSchema
name|schema
range|:
name|table
operator|.
name|getPartitionKeys
argument_list|()
control|)
block|{
name|partitionCols
operator|.
name|add
argument_list|(
name|schema
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|HCatStorageHandler
name|storageHandler
init|=
name|HCatUtil
operator|.
name|getStorageHandler
argument_list|(
name|conf
argument_list|,
name|storerInfo
argument_list|)
decl_stmt|;
comment|//Serialize the output info into the configuration
name|outputJobInfo
operator|.
name|setTableInfo
argument_list|(
name|HCatTableInfo
operator|.
name|valueOf
argument_list|(
name|table
operator|.
name|getTTable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|outputJobInfo
operator|.
name|setOutputSchema
argument_list|(
name|tableSchema
argument_list|)
expr_stmt|;
name|harRequested
operator|=
name|getHarRequested
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|outputJobInfo
operator|.
name|setHarRequested
argument_list|(
name|harRequested
argument_list|)
expr_stmt|;
name|maxDynamicPartitions
operator|=
name|getMaxDynamicPartitions
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|outputJobInfo
operator|.
name|setMaximumDynamicPartitions
argument_list|(
name|maxDynamicPartitions
argument_list|)
expr_stmt|;
name|HCatUtil
operator|.
name|configureOutputStorageHandler
argument_list|(
name|storageHandler
argument_list|,
name|conf
argument_list|,
name|outputJobInfo
argument_list|)
expr_stmt|;
name|Path
name|tblPath
init|=
operator|new
name|Path
argument_list|(
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
decl_stmt|;
comment|/*  Set the umask in conf such that files/dirs get created with table-dir       * permissions. Following three assumptions are made:       * 1. Actual files/dirs creation is done by RecordWriter of underlying       * output format. It is assumed that they use default permissions while creation.       * 2. Default Permissions = FsPermission.getDefault() = 777.       * 3. UMask is honored by underlying filesystem.       */
name|FsPermission
operator|.
name|setUMask
argument_list|(
name|conf
argument_list|,
name|FsPermission
operator|.
name|getDefault
argument_list|()
operator|.
name|applyUMask
argument_list|(
name|tblPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|getFileStatus
argument_list|(
name|tblPath
argument_list|)
operator|.
name|getPermission
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|Security
operator|.
name|getInstance
argument_list|()
operator|.
name|isSecurityEnabled
argument_list|()
condition|)
block|{
name|Security
operator|.
name|getInstance
argument_list|()
operator|.
name|handleSecurity
argument_list|(
name|credentials
argument_list|,
name|outputJobInfo
argument_list|,
name|client
argument_list|,
name|conf
argument_list|,
name|harRequested
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
if|if
condition|(
name|e
operator|instanceof
name|HCatException
condition|)
block|{
throw|throw
operator|(
name|HCatException
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_SET_OUTPUT
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
finally|finally
block|{
name|HCatUtil
operator|.
name|closeHiveClientQuietly
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @see org.apache.hive.hcatalog.mapreduce.HCatOutputFormat#setSchema(org.apache.hadoop.conf.Configuration, org.apache.hive.hcatalog.data.schema.HCatSchema)    */
specifier|public
specifier|static
name|void
name|setSchema
parameter_list|(
specifier|final
name|Job
name|job
parameter_list|,
specifier|final
name|HCatSchema
name|schema
parameter_list|)
throws|throws
name|IOException
block|{
name|setSchema
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|schema
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the schema for the data being written out to the partition. The    * table schema is used by default for the partition if this is not called.    * @param conf the job Configuration object    * @param schema the schema for the data    * @throws IOException    */
specifier|public
specifier|static
name|void
name|setSchema
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|HCatSchema
name|schema
parameter_list|)
throws|throws
name|IOException
block|{
name|OutputJobInfo
name|jobInfo
init|=
name|getJobInfo
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partMap
init|=
name|jobInfo
operator|.
name|getPartitionValues
argument_list|()
decl_stmt|;
name|setPartDetails
argument_list|(
name|jobInfo
argument_list|,
name|schema
argument_list|,
name|partMap
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_OUTPUT_INFO
argument_list|,
name|HCatUtil
operator|.
name|serialize
argument_list|(
name|jobInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the record writer for the job. This uses the StorageHandler's default     * OutputFormat to get the record writer.    * @param context the information about the current task    * @return a RecordWriter to write the output for the job    * @throws IOException    * @throws InterruptedException    */
annotation|@
name|Override
specifier|public
name|RecordWriter
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|HCatRecord
argument_list|>
name|getRecordWriter
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|getOutputFormat
argument_list|(
name|context
argument_list|)
operator|.
name|getRecordWriter
argument_list|(
name|context
argument_list|)
return|;
block|}
comment|/**    * Get the output committer for this output format. This is responsible    * for ensuring the output is committed correctly.    * @param context the task context    * @return an output committer    * @throws IOException    * @throws InterruptedException    */
annotation|@
name|Override
specifier|public
name|OutputCommitter
name|getOutputCommitter
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|getOutputFormat
argument_list|(
name|context
argument_list|)
operator|.
name|getOutputCommitter
argument_list|(
name|context
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|int
name|getMaxDynamicPartitions
parameter_list|(
name|HiveConf
name|hConf
parameter_list|)
block|{
comment|// by default the bounds checking for maximum number of
comment|// dynamic partitions is disabled (-1)
name|int
name|maxDynamicPartitions
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|HCatConstants
operator|.
name|HCAT_IS_DYNAMIC_MAX_PTN_CHECK_ENABLED
condition|)
block|{
name|maxDynamicPartitions
operator|=
name|hConf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DYNAMICPARTITIONMAXPARTS
argument_list|)
expr_stmt|;
block|}
return|return
name|maxDynamicPartitions
return|;
block|}
specifier|private
specifier|static
name|boolean
name|getHarRequested
parameter_list|(
name|HiveConf
name|hConf
parameter_list|)
block|{
return|return
name|hConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEARCHIVEENABLED
argument_list|)
return|;
block|}
block|}
end_class

end_unit

