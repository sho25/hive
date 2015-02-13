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
name|optimizer
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
name|LinkedHashMap
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
name|Set
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
name|fs
operator|.
name|FileStatus
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
name|common
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
name|ql
operator|.
name|Driver
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
name|TableScanOperator
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
name|Task
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
name|TaskFactory
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|index
operator|.
name|IndexMetadataChangeTask
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
name|index
operator|.
name|IndexMetadataChangeWork
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
name|Hive
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
name|metadata
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
name|ql
operator|.
name|optimizer
operator|.
name|physical
operator|.
name|index
operator|.
name|IndexWhereProcessor
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
name|parse
operator|.
name|ParseContext
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
name|parse
operator|.
name|PrunedPartitionList
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
name|parse
operator|.
name|SemanticException
import|;
end_import

begin_comment
comment|/**  * Utility class for index support.  * Currently used for BITMAP and AGGREGATE index  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|IndexUtils
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
name|IndexWhereProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|IndexUtils
parameter_list|()
block|{   }
comment|/**    * Check the partitions used by the table scan to make sure they also exist in the    * index table.    * @param pctx    * @param indexes    * @return partitions used by query.  null if they do not exist in index table    * @throws HiveException    */
specifier|public
specifier|static
name|Set
argument_list|<
name|Partition
argument_list|>
name|checkPartitionsCoveredByIndex
parameter_list|(
name|TableScanOperator
name|tableScan
parameter_list|,
name|ParseContext
name|pctx
parameter_list|,
name|List
argument_list|<
name|Index
argument_list|>
name|indexes
parameter_list|)
throws|throws
name|HiveException
block|{
name|Hive
name|hive
init|=
name|Hive
operator|.
name|get
argument_list|(
name|pctx
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
comment|// make sure each partition exists on the index table
name|PrunedPartitionList
name|queryPartitionList
init|=
name|pctx
operator|.
name|getOpToPartList
argument_list|()
operator|.
name|get
argument_list|(
name|tableScan
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Partition
argument_list|>
name|queryPartitions
init|=
name|queryPartitionList
operator|.
name|getPartitions
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryPartitions
operator|==
literal|null
operator|||
name|queryPartitions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|Partition
name|part
range|:
name|queryPartitions
control|)
block|{
if|if
condition|(
operator|!
name|containsPartition
argument_list|(
name|hive
argument_list|,
name|part
argument_list|,
name|indexes
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
comment|// problem if it doesn't contain the partition
block|}
block|}
return|return
name|queryPartitions
return|;
block|}
comment|/**    * check that every index table contains the given partition and is fresh    */
specifier|private
specifier|static
name|boolean
name|containsPartition
parameter_list|(
name|Hive
name|hive
parameter_list|,
name|Partition
name|part
parameter_list|,
name|List
argument_list|<
name|Index
argument_list|>
name|indexes
parameter_list|)
throws|throws
name|HiveException
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|part
operator|.
name|getSpec
argument_list|()
decl_stmt|;
if|if
condition|(
name|partSpec
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// empty specs come from non-partitioned tables
return|return
name|isIndexTableFresh
argument_list|(
name|hive
argument_list|,
name|indexes
argument_list|,
name|part
operator|.
name|getTable
argument_list|()
argument_list|)
return|;
block|}
for|for
control|(
name|Index
name|index
range|:
name|indexes
control|)
block|{
comment|// index.getDbName() is used as a default database, which is database of target table,
comment|// if index.getIndexTableName() does not contain database name
name|String
index|[]
name|qualified
init|=
name|Utilities
operator|.
name|getDbTableName
argument_list|(
name|index
operator|.
name|getDbName
argument_list|()
argument_list|,
name|index
operator|.
name|getIndexTableName
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|indexTable
init|=
name|hive
operator|.
name|getTable
argument_list|(
name|qualified
index|[
literal|0
index|]
argument_list|,
name|qualified
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
comment|// get partitions that match the spec
name|Partition
name|matchingPartition
init|=
name|hive
operator|.
name|getPartition
argument_list|(
name|indexTable
argument_list|,
name|partSpec
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|matchingPartition
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Index table "
operator|+
name|indexTable
operator|+
literal|"did not contain built partition that matched "
operator|+
name|partSpec
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|isIndexPartitionFresh
argument_list|(
name|hive
argument_list|,
name|index
argument_list|,
name|part
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Check the index partitions on a partitioned table exist and are fresh    */
specifier|private
specifier|static
name|boolean
name|isIndexPartitionFresh
parameter_list|(
name|Hive
name|hive
parameter_list|,
name|Index
name|index
parameter_list|,
name|Partition
name|part
parameter_list|)
throws|throws
name|HiveException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"checking index staleness..."
argument_list|)
expr_stmt|;
try|try
block|{
name|String
name|indexTs
init|=
name|index
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|part
operator|.
name|getSpec
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexTs
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|FileSystem
name|partFs
init|=
name|part
operator|.
name|getDataLocation
argument_list|()
operator|.
name|getFileSystem
argument_list|(
name|hive
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|parts
init|=
name|partFs
operator|.
name|listStatus
argument_list|(
name|part
operator|.
name|getDataLocation
argument_list|()
argument_list|,
name|FileUtils
operator|.
name|HIDDEN_FILES_PATH_FILTER
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|parts
control|)
block|{
if|if
condition|(
name|status
operator|.
name|getModificationTime
argument_list|()
operator|>
name|Long
operator|.
name|parseLong
argument_list|(
name|indexTs
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Index is stale on partition '"
operator|+
name|part
operator|.
name|getName
argument_list|()
operator|+
literal|"'. Modified time ("
operator|+
name|status
operator|.
name|getModificationTime
argument_list|()
operator|+
literal|") for '"
operator|+
name|status
operator|.
name|getPath
argument_list|()
operator|+
literal|"' is higher than index creation time ("
operator|+
name|indexTs
operator|+
literal|")."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Failed to grab timestamp information from partition '"
operator|+
name|part
operator|.
name|getName
argument_list|()
operator|+
literal|"': "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Check that the indexes on the un-partitioned table exist and are fresh    */
specifier|private
specifier|static
name|boolean
name|isIndexTableFresh
parameter_list|(
name|Hive
name|hive
parameter_list|,
name|List
argument_list|<
name|Index
argument_list|>
name|indexes
parameter_list|,
name|Table
name|src
parameter_list|)
throws|throws
name|HiveException
block|{
comment|//check that they exist
if|if
condition|(
name|indexes
operator|==
literal|null
operator|||
name|indexes
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|//check that they are not stale
for|for
control|(
name|Index
name|index
range|:
name|indexes
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"checking index staleness..."
argument_list|)
expr_stmt|;
try|try
block|{
name|String
name|indexTs
init|=
name|index
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
literal|"base_timestamp"
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexTs
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|FileSystem
name|srcFs
init|=
name|src
operator|.
name|getPath
argument_list|()
operator|.
name|getFileSystem
argument_list|(
name|hive
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|srcs
init|=
name|srcFs
operator|.
name|listStatus
argument_list|(
name|src
operator|.
name|getPath
argument_list|()
argument_list|,
name|FileUtils
operator|.
name|HIDDEN_FILES_PATH_FILTER
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|srcs
control|)
block|{
if|if
condition|(
name|status
operator|.
name|getModificationTime
argument_list|()
operator|>
name|Long
operator|.
name|parseLong
argument_list|(
name|indexTs
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Index is stale on table '"
operator|+
name|src
operator|.
name|getTableName
argument_list|()
operator|+
literal|"'. Modified time ("
operator|+
name|status
operator|.
name|getModificationTime
argument_list|()
operator|+
literal|") for '"
operator|+
name|status
operator|.
name|getPath
argument_list|()
operator|+
literal|"' is higher than index creation time ("
operator|+
name|indexTs
operator|+
literal|")."
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Failed to grab timestamp information from table '"
operator|+
name|src
operator|.
name|getTableName
argument_list|()
operator|+
literal|"': "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Get a list of indexes on a table that match given types.    */
specifier|public
specifier|static
name|List
argument_list|<
name|Index
argument_list|>
name|getIndexes
parameter_list|(
name|Table
name|baseTableMetaData
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|matchIndexTypes
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|Index
argument_list|>
name|matchingIndexes
init|=
operator|new
name|ArrayList
argument_list|<
name|Index
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Index
argument_list|>
name|indexesOnTable
decl_stmt|;
try|try
block|{
name|indexesOnTable
operator|=
name|getAllIndexes
argument_list|(
name|baseTableMetaData
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// get all indexes
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Error accessing metastore"
argument_list|,
name|e
argument_list|)
throw|;
block|}
for|for
control|(
name|Index
name|index
range|:
name|indexesOnTable
control|)
block|{
name|String
name|indexType
init|=
name|index
operator|.
name|getIndexHandlerClass
argument_list|()
decl_stmt|;
if|if
condition|(
name|matchIndexTypes
operator|.
name|contains
argument_list|(
name|indexType
argument_list|)
condition|)
block|{
name|matchingIndexes
operator|.
name|add
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|matchingIndexes
return|;
block|}
comment|/**    * @return List containing Indexes names if there are indexes on this table    * @throws HiveException    **/
specifier|public
specifier|static
name|List
argument_list|<
name|Index
argument_list|>
name|getAllIndexes
parameter_list|(
name|Table
name|table
parameter_list|,
name|short
name|max
parameter_list|)
throws|throws
name|HiveException
block|{
name|Hive
name|hive
init|=
name|Hive
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
name|hive
operator|.
name|getIndexes
argument_list|(
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|,
name|max
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Task
argument_list|<
name|?
argument_list|>
name|createRootTask
parameter_list|(
name|HiveConf
name|builderConf
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|StringBuilder
name|command
parameter_list|,
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|,
name|String
name|indexTableName
parameter_list|,
name|String
name|dbName
parameter_list|)
block|{
comment|// Don't try to index optimize the query to build the index
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|builderConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTINDEXFILTER
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Driver
name|driver
init|=
operator|new
name|Driver
argument_list|(
name|builderConf
argument_list|)
decl_stmt|;
name|driver
operator|.
name|compile
argument_list|(
name|command
operator|.
name|toString
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|rootTask
init|=
name|driver
operator|.
name|getPlan
argument_list|()
operator|.
name|getRootTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|inputs
operator|.
name|addAll
argument_list|(
name|driver
operator|.
name|getPlan
argument_list|()
operator|.
name|getInputs
argument_list|()
argument_list|)
expr_stmt|;
name|outputs
operator|.
name|addAll
argument_list|(
name|driver
operator|.
name|getPlan
argument_list|()
operator|.
name|getOutputs
argument_list|()
argument_list|)
expr_stmt|;
name|IndexMetadataChangeWork
name|indexMetaChange
init|=
operator|new
name|IndexMetadataChangeWork
argument_list|(
name|partSpec
argument_list|,
name|indexTableName
argument_list|,
name|dbName
argument_list|)
decl_stmt|;
name|IndexMetadataChangeTask
name|indexMetaChangeTsk
init|=
operator|(
name|IndexMetadataChangeTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
name|indexMetaChange
argument_list|,
name|builderConf
argument_list|)
decl_stmt|;
name|indexMetaChangeTsk
operator|.
name|setWork
argument_list|(
name|indexMetaChange
argument_list|)
expr_stmt|;
name|rootTask
operator|.
name|addDependentTask
argument_list|(
name|indexMetaChangeTsk
argument_list|)
expr_stmt|;
return|return
name|rootTask
return|;
block|}
block|}
end_class

end_unit

