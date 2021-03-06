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
name|IMetaStoreClient
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
name|utils
operator|.
name|FileUtils
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
name|MetaException
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
name|NoSuchObjectException
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
name|hive
operator|.
name|serde2
operator|.
name|AbstractSerDe
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
name|thrift
operator|.
name|TException
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * File-based storage (ie RCFile, Text, etc) implementation of OutputFormatContainer.  * This implementation supports the following HCatalog features: partitioning, dynamic partitioning, Hadoop Archiving, etc.  */
end_comment

begin_class
class|class
name|FileOutputFormatContainer
extends|extends
name|OutputFormatContainer
block|{
comment|/**    * @param of base OutputFormat to contain    */
specifier|public
name|FileOutputFormatContainer
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|OutputFormat
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
name|of
parameter_list|)
block|{
name|super
argument_list|(
name|of
argument_list|)
expr_stmt|;
block|}
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
comment|//this needs to be manually set, under normal circumstances MR Task does this
name|setWorkOutputPath
argument_list|(
name|context
argument_list|)
expr_stmt|;
comment|//Configure the output key and value classes.
comment|// This is required for writing null as key for file based tables.
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
literal|"mapred.output.key.class"
argument_list|,
name|NullWritable
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|jobInfoString
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
name|HCAT_KEY_OUTPUT_INFO
argument_list|)
decl_stmt|;
name|OutputJobInfo
name|jobInfo
init|=
operator|(
name|OutputJobInfo
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|jobInfoString
argument_list|)
decl_stmt|;
name|StorerInfo
name|storeInfo
init|=
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getStorerInfo
argument_list|()
decl_stmt|;
name|HiveStorageHandler
name|storageHandler
init|=
name|HCatUtil
operator|.
name|getStorageHandler
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|storeInfo
argument_list|)
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|AbstractSerDe
argument_list|>
name|serde
init|=
name|storageHandler
operator|.
name|getSerDeClass
argument_list|()
decl_stmt|;
name|AbstractSerDe
name|sd
init|=
operator|(
name|AbstractSerDe
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|serde
argument_list|,
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
literal|"mapred.output.value.class"
argument_list|,
name|sd
operator|.
name|getSerializedClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|RecordWriter
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|HCatRecord
argument_list|>
name|rw
decl_stmt|;
if|if
condition|(
name|HCatBaseOutputFormat
operator|.
name|getJobInfo
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|isDynamicPartitioningUsed
argument_list|()
condition|)
block|{
comment|// When Dynamic partitioning is used, the RecordWriter instance initialized here isn't used. Can use null.
comment|// (That's because records can't be written until the values of the dynamic partitions are deduced.
comment|// By that time, a new local instance of RecordWriter, with the correct output-path, will be constructed.)
name|rw
operator|=
operator|new
name|DynamicPartitionFileRecordWriterContainer
argument_list|(
operator|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|RecordWriter
operator|)
literal|null
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Path
name|parentDir
init|=
operator|new
name|Path
argument_list|(
name|context
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
name|getUniqueName
argument_list|(
operator|new
name|JobConf
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|context
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
argument_list|)
argument_list|)
decl_stmt|;
name|rw
operator|=
operator|new
name|StaticPartitionFileRecordWriterContainer
argument_list|(
name|getBaseOutputFormat
argument_list|()
operator|.
name|getRecordWriter
argument_list|(
name|parentDir
operator|.
name|getFileSystem
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
operator|new
name|JobConf
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
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
name|context
argument_list|)
argument_list|)
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
return|return
name|rw
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkOutputSpecs
parameter_list|(
name|JobContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|OutputJobInfo
name|jobInfo
init|=
name|HCatOutputFormat
operator|.
name|getJobInfo
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|IMetaStoreClient
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
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|client
operator|=
name|HCatUtil
operator|.
name|getHiveMetastoreClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|handleDuplicatePublish
argument_list|(
name|context
argument_list|,
name|jobInfo
argument_list|,
name|client
argument_list|,
operator|new
name|Table
argument_list|(
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
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
catch|catch
parameter_list|(
name|TException
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
if|if
condition|(
operator|!
name|jobInfo
operator|.
name|isDynamicPartitioningUsed
argument_list|()
condition|)
block|{
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|getBaseOutputFormat
argument_list|()
operator|.
name|checkOutputSpecs
argument_list|(
literal|null
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
comment|//checkoutputspecs might've set some properties we need to have context reflect that
name|HCatUtil
operator|.
name|copyConf
argument_list|(
name|jobConf
argument_list|,
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
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
comment|//this needs to be manually set, under normal circumstances MR Task does this
name|setWorkOutputPath
argument_list|(
name|context
argument_list|)
expr_stmt|;
return|return
operator|new
name|FileOutputCommitterContainer
argument_list|(
name|context
argument_list|,
name|HCatBaseOutputFormat
operator|.
name|getJobInfo
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|isDynamicPartitioningUsed
argument_list|()
condition|?
literal|null
else|:
operator|new
name|JobConf
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|getOutputCommitter
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Handles duplicate publish of partition or data into an unpartitioned table    * if the table is immutable    *    * For partitioned tables, fails if partition already exists.    * For non partitioned tables, fails if files are present in table directory.    * For dynamic partitioned publish, does nothing - check would need to be done at recordwriter time    * @param context the job    * @param outputInfo the output info    * @param client the metastore client    * @param table the table being written to    * @throws IOException    * @throws org.apache.hadoop.hive.metastore.api.MetaException    * @throws org.apache.thrift.TException    */
specifier|private
specifier|static
name|void
name|handleDuplicatePublish
parameter_list|(
name|JobContext
name|context
parameter_list|,
name|OutputJobInfo
name|outputInfo
parameter_list|,
name|IMetaStoreClient
name|client
parameter_list|,
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
throws|,
name|MetaException
throws|,
name|TException
throws|,
name|NoSuchObjectException
block|{
comment|/*      * For fully specified ptn, follow strict checks for existence of partitions in metadata      * For unpartitioned tables, follow filechecks      * For partially specified tables:      *    This would then need filechecks at the start of a ptn write,      *    Doing metadata checks can get potentially very expensive (fat conf) if      *    there are a large number of partitions that match the partial specifications      */
if|if
condition|(
operator|!
name|table
operator|.
name|isImmutable
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|table
operator|.
name|getPartitionKeys
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
if|if
condition|(
operator|!
name|outputInfo
operator|.
name|isDynamicPartitioningUsed
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|partitionValues
init|=
name|getPartitionValueList
argument_list|(
name|table
argument_list|,
name|outputInfo
operator|.
name|getPartitionValues
argument_list|()
argument_list|)
decl_stmt|;
comment|// fully-specified partition
name|List
argument_list|<
name|String
argument_list|>
name|currentParts
init|=
name|client
operator|.
name|listPartitionNames
argument_list|(
name|outputInfo
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|outputInfo
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partitionValues
argument_list|,
operator|(
name|short
operator|)
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentParts
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// If a table is partitioned and immutable, then the presence
comment|// of the partition alone is enough to throw an error - we do
comment|// not need to check for emptiness to decide to throw an error
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_DUPLICATE_PARTITION
argument_list|)
throw|;
block|}
block|}
block|}
else|else
block|{
name|List
argument_list|<
name|String
argument_list|>
name|partitionValues
init|=
name|getPartitionValueList
argument_list|(
name|table
argument_list|,
name|outputInfo
operator|.
name|getPartitionValues
argument_list|()
argument_list|)
decl_stmt|;
comment|// non-partitioned table
name|Path
name|tablePath
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
name|FileSystem
name|fs
init|=
name|tablePath
operator|.
name|getFileSystem
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|FileUtils
operator|.
name|isDirEmpty
argument_list|(
name|fs
argument_list|,
name|tablePath
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NON_EMPTY_TABLE
argument_list|,
name|table
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * Convert the partition value map to a value list in the partition key order.    * @param table the table being written to    * @param valueMap the partition value map    * @return the partition value list    * @throws java.io.IOException    */
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getPartitionValueList
parameter_list|(
name|Table
name|table
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|valueMap
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|valueMap
operator|.
name|size
argument_list|()
operator|!=
name|table
operator|.
name|getPartitionKeys
argument_list|()
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_INVALID_PARTITION_VALUES
argument_list|,
literal|"Table "
operator|+
name|table
operator|.
name|getTableName
argument_list|()
operator|+
literal|" has "
operator|+
name|table
operator|.
name|getPartitionKeys
argument_list|()
operator|.
name|size
argument_list|()
operator|+
literal|" partition keys, got "
operator|+
name|valueMap
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|values
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
name|String
name|value
init|=
name|valueMap
operator|.
name|get
argument_list|(
name|schema
operator|.
name|getName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_MISSING_PARTITION_KEY
argument_list|,
literal|"Key "
operator|+
name|schema
operator|.
name|getName
argument_list|()
operator|+
literal|" of table "
operator|+
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
throw|;
block|}
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|values
return|;
block|}
specifier|static
name|void
name|setWorkOutputPath
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|outputPath
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
literal|"mapred.output.dir"
argument_list|)
decl_stmt|;
comment|//we need to do this to get the task path and set it for mapred implementation
comment|//since it can't be done automatically because of mapreduce->mapred abstraction
if|if
condition|(
name|outputPath
operator|!=
literal|null
condition|)
name|context
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
name|outputPath
argument_list|)
argument_list|,
name|context
argument_list|)
operator|.
name|getWorkPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

