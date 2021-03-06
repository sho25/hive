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
name|Properties
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
name|Partition
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
comment|/**  * The Class which handles querying the metadata server using the MetaStoreClient. The list of  * partitions matching the partition filter is fetched from the server and the information is  * serialized and written into the JobContext configuration. The inputInfo is also updated with  * info required in the client process context.  */
end_comment

begin_class
class|class
name|InitializeInput
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
name|InitializeInput
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * @see org.apache.hive.hcatalog.mapreduce.InitializeInput#setInput(org.apache.hadoop.conf.Configuration, InputJobInfo)    */
specifier|public
specifier|static
name|void
name|setInput
parameter_list|(
name|Job
name|job
parameter_list|,
name|InputJobInfo
name|theirInputJobInfo
parameter_list|)
throws|throws
name|Exception
block|{
name|setInput
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|theirInputJobInfo
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the input to use for the Job. This queries the metadata server with the specified    * partition predicates, gets the matching partitions, and puts the information in the job    * configuration object.    *    * To ensure a known InputJobInfo state, only the database name, table name, filter, and    * properties are preserved. All other modification from the given InputJobInfo are discarded.    *    * After calling setInput, InputJobInfo can be retrieved from the job configuration as follows:    * {code}    * LinkedList&lt;InputJobInfo&gt; inputInfo = (LinkedList&lt;InputJobInfo&gt;) HCatUtil    * .deserialize(job.getConfiguration().get(HCatConstants.HCAT_KEY_JOB_INFO));    * {code}    *    * @param conf the job Configuration object    * @param theirInputJobInfo information on the Input to read    * @throws Exception    */
specifier|public
specifier|static
name|void
name|setInput
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|InputJobInfo
name|theirInputJobInfo
parameter_list|)
throws|throws
name|Exception
block|{
name|InputJobInfo
name|inputJobInfo
init|=
name|InputJobInfo
operator|.
name|create
argument_list|(
name|theirInputJobInfo
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|theirInputJobInfo
operator|.
name|getTableName
argument_list|()
argument_list|,
name|theirInputJobInfo
operator|.
name|getFilter
argument_list|()
argument_list|,
name|theirInputJobInfo
operator|.
name|getProperties
argument_list|()
argument_list|)
decl_stmt|;
name|populateInputJobInfo
argument_list|(
name|conf
argument_list|,
name|inputJobInfo
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|HCatUtil
operator|.
name|putInputJobInfoToConf
argument_list|(
name|inputJobInfo
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the given InputJobInfo after populating with data queried from the metadata service.    */
specifier|private
specifier|static
name|void
name|populateInputJobInfo
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|InputJobInfo
name|inputJobInfo
parameter_list|,
name|String
name|locationFilter
parameter_list|)
throws|throws
name|Exception
block|{
name|IMetaStoreClient
name|client
init|=
literal|null
decl_stmt|;
name|HiveConf
name|hiveConf
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|conf
operator|!=
literal|null
condition|)
block|{
name|hiveConf
operator|=
name|HCatUtil
operator|.
name|getHiveConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|HCatInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
name|client
operator|=
name|HCatUtil
operator|.
name|getHiveMetastoreClient
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
name|inputJobInfo
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|inputJobInfo
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|PartInfo
argument_list|>
name|partInfoList
init|=
operator|new
name|ArrayList
argument_list|<
name|PartInfo
argument_list|>
argument_list|()
decl_stmt|;
name|inputJobInfo
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
if|if
condition|(
name|table
operator|.
name|getPartitionKeys
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
comment|//Partitioned table
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
init|=
name|client
operator|.
name|listPartitionsByFilter
argument_list|(
name|inputJobInfo
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|inputJobInfo
operator|.
name|getTableName
argument_list|()
argument_list|,
name|inputJobInfo
operator|.
name|getFilter
argument_list|()
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
comment|// Default to 100,000 partitions if hive.metastore.maxpartition is not defined
name|int
name|maxPart
init|=
name|hiveConf
operator|.
name|getInt
argument_list|(
literal|"hcat.metastore.maxpartitions"
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
if|if
condition|(
name|parts
operator|!=
literal|null
operator|&&
name|parts
operator|.
name|size
argument_list|()
operator|>
name|maxPart
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_EXCEED_MAXPART
argument_list|,
literal|"total number of partitions is "
operator|+
name|parts
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
comment|// populate partition info
if|if
condition|(
name|parts
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Partition
name|ptn
range|:
name|parts
control|)
block|{
name|HCatSchema
name|schema
init|=
name|HCatUtil
operator|.
name|extractSchema
argument_list|(
operator|new
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
name|Partition
argument_list|(
name|table
argument_list|,
name|ptn
argument_list|)
argument_list|)
decl_stmt|;
name|PartInfo
name|partInfo
init|=
name|extractPartInfo
argument_list|(
name|schema
argument_list|,
name|ptn
operator|.
name|getSd
argument_list|()
argument_list|,
name|ptn
operator|.
name|getParameters
argument_list|()
argument_list|,
name|conf
argument_list|,
name|inputJobInfo
argument_list|)
decl_stmt|;
name|partInfo
operator|.
name|setPartitionValues
argument_list|(
name|InternalUtil
operator|.
name|createPtnKeyValueMap
argument_list|(
name|table
argument_list|,
name|ptn
argument_list|)
argument_list|)
expr_stmt|;
name|partInfoList
operator|.
name|add
argument_list|(
name|partInfo
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|//Non partitioned table
name|HCatSchema
name|schema
init|=
name|HCatUtil
operator|.
name|extractSchema
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|PartInfo
name|partInfo
init|=
name|extractPartInfo
argument_list|(
name|schema
argument_list|,
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
argument_list|,
name|conf
argument_list|,
name|inputJobInfo
argument_list|)
decl_stmt|;
name|partInfo
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
name|partInfoList
operator|.
name|add
argument_list|(
name|partInfo
argument_list|)
expr_stmt|;
block|}
name|inputJobInfo
operator|.
name|setPartitions
argument_list|(
name|partInfoList
argument_list|)
expr_stmt|;
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
specifier|private
specifier|static
name|PartInfo
name|extractPartInfo
parameter_list|(
name|HCatSchema
name|schema
parameter_list|,
name|StorageDescriptor
name|sd
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|InputJobInfo
name|inputJobInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|StorerInfo
name|storerInfo
init|=
name|InternalUtil
operator|.
name|extractStorerInfo
argument_list|(
name|sd
argument_list|,
name|parameters
argument_list|)
decl_stmt|;
name|Properties
name|hcatProperties
init|=
operator|new
name|Properties
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
name|storerInfo
argument_list|)
decl_stmt|;
comment|// copy the properties from storageHandler to jobProperties
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
init|=
name|HCatUtil
operator|.
name|getInputJobProperties
argument_list|(
name|storageHandler
argument_list|,
name|inputJobInfo
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|parameters
operator|.
name|keySet
argument_list|()
control|)
block|{
name|hcatProperties
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|parameters
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// FIXME
comment|// Bloating partinfo with inputJobInfo is not good
return|return
operator|new
name|PartInfo
argument_list|(
name|schema
argument_list|,
name|storageHandler
argument_list|,
name|sd
operator|.
name|getLocation
argument_list|()
argument_list|,
name|hcatProperties
argument_list|,
name|jobProperties
argument_list|,
name|inputJobInfo
operator|.
name|getTableInfo
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

