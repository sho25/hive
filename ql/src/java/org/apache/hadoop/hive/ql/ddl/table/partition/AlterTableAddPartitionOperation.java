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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|ddl
operator|.
name|table
operator|.
name|partition
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
name|util
operator|.
name|BitSet
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
name|common
operator|.
name|StatsSetupConst
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
name|TableName
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
name|ValidReaderWriteIdList
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
name|Warehouse
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
name|ColumnStatistics
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
name|EnvironmentContext
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
name|ql
operator|.
name|ddl
operator|.
name|DDLOperation
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
name|ddl
operator|.
name|DDLOperationContext
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
name|ddl
operator|.
name|DDLUtils
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
name|io
operator|.
name|AcidUtils
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
name|lockmgr
operator|.
name|LockException
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
name|parse
operator|.
name|ReplicationSpec
import|;
end_import

begin_comment
comment|/**  * Operation process of adding a partition to a table.  */
end_comment

begin_class
specifier|public
class|class
name|AlterTableAddPartitionOperation
extends|extends
name|DDLOperation
argument_list|<
name|AlterTableAddPartitionDesc
argument_list|>
block|{
specifier|public
name|AlterTableAddPartitionOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|AlterTableAddPartitionDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
comment|// TODO: catalog name everywhere in this method
name|Table
name|table
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getTable
argument_list|(
name|desc
operator|.
name|getDbName
argument_list|()
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|writeId
init|=
name|getWriteId
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
name|getPartitions
argument_list|(
name|table
argument_list|,
name|writeId
argument_list|)
decl_stmt|;
name|addPartitions
argument_list|(
name|table
argument_list|,
name|partitions
argument_list|,
name|writeId
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|private
name|long
name|getWriteId
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|LockException
block|{
comment|// In case of replication, get the writeId from the source and use valid write Id list for replication.
if|if
condition|(
name|desc
operator|.
name|getReplicationSpec
argument_list|()
operator|.
name|isInReplicationScope
argument_list|()
operator|&&
name|desc
operator|.
name|getPartitions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getWriteId
argument_list|()
operator|>
literal|0
condition|)
block|{
return|return
name|desc
operator|.
name|getPartitions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getWriteId
argument_list|()
return|;
block|}
else|else
block|{
name|AcidUtils
operator|.
name|TableSnapshot
name|tableSnapshot
init|=
name|AcidUtils
operator|.
name|getTableSnapshot
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|table
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableSnapshot
operator|!=
literal|null
operator|&&
name|tableSnapshot
operator|.
name|getWriteId
argument_list|()
operator|>
literal|0
condition|)
block|{
return|return
name|tableSnapshot
operator|.
name|getWriteId
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
block|}
specifier|private
name|List
argument_list|<
name|Partition
argument_list|>
name|getPartitions
parameter_list|(
name|Table
name|table
parameter_list|,
name|long
name|writeId
parameter_list|)
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|desc
operator|.
name|getPartitions
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|AlterTableAddPartitionDesc
operator|.
name|PartitionDesc
name|partitionDesc
range|:
name|desc
operator|.
name|getPartitions
argument_list|()
control|)
block|{
name|Partition
name|partition
init|=
name|convertPartitionSpecToMetaPartition
argument_list|(
name|table
argument_list|,
name|partitionDesc
argument_list|)
decl_stmt|;
if|if
condition|(
name|partition
operator|!=
literal|null
operator|&&
name|writeId
operator|>
literal|0
condition|)
block|{
name|partition
operator|.
name|setWriteId
argument_list|(
name|writeId
argument_list|)
expr_stmt|;
block|}
name|partitions
operator|.
name|add
argument_list|(
name|partition
argument_list|)
expr_stmt|;
block|}
return|return
name|partitions
return|;
block|}
specifier|private
name|Partition
name|convertPartitionSpecToMetaPartition
parameter_list|(
name|Table
name|table
parameter_list|,
name|AlterTableAddPartitionDesc
operator|.
name|PartitionDesc
name|partitionSpec
parameter_list|)
throws|throws
name|HiveException
block|{
name|Path
name|location
init|=
name|partitionSpec
operator|.
name|getLocation
argument_list|()
operator|!=
literal|null
condition|?
operator|new
name|Path
argument_list|(
name|table
operator|.
name|getPath
argument_list|()
argument_list|,
name|partitionSpec
operator|.
name|getLocation
argument_list|()
argument_list|)
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|location
operator|!=
literal|null
condition|)
block|{
comment|// Ensure that it is a full qualified path (in most cases it will be since tbl.getPath() is full qualified)
name|location
operator|=
operator|new
name|Path
argument_list|(
name|Utilities
operator|.
name|getQualifiedPath
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|location
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Partition
name|partition
init|=
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
operator|.
name|createMetaPartitionObject
argument_list|(
name|table
argument_list|,
name|partitionSpec
operator|.
name|getPartSpec
argument_list|()
argument_list|,
name|location
argument_list|)
decl_stmt|;
if|if
condition|(
name|partitionSpec
operator|.
name|getPartParams
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|partition
operator|.
name|setParameters
argument_list|(
name|partitionSpec
operator|.
name|getPartParams
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|partitionSpec
operator|.
name|getInputFormat
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|setInputFormat
argument_list|(
name|partitionSpec
operator|.
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|partitionSpec
operator|.
name|getOutputFormat
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|setOutputFormat
argument_list|(
name|partitionSpec
operator|.
name|getOutputFormat
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|partitionSpec
operator|.
name|getNumBuckets
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|setNumBuckets
argument_list|(
name|partitionSpec
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|partitionSpec
operator|.
name|getCols
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|setCols
argument_list|(
name|partitionSpec
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|partitionSpec
operator|.
name|getSerializationLib
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setSerializationLib
argument_list|(
name|partitionSpec
operator|.
name|getSerializationLib
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|partitionSpec
operator|.
name|getSerdeParams
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setParameters
argument_list|(
name|partitionSpec
operator|.
name|getSerdeParams
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|partitionSpec
operator|.
name|getBucketCols
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|setBucketCols
argument_list|(
name|partitionSpec
operator|.
name|getBucketCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|partitionSpec
operator|.
name|getSortCols
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|setSortCols
argument_list|(
name|partitionSpec
operator|.
name|getSortCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|partitionSpec
operator|.
name|getColStats
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|partition
operator|.
name|setColStats
argument_list|(
name|partitionSpec
operator|.
name|getColStats
argument_list|()
argument_list|)
expr_stmt|;
name|ColumnStatistics
name|statistics
init|=
name|partition
operator|.
name|getColStats
argument_list|()
decl_stmt|;
if|if
condition|(
name|statistics
operator|!=
literal|null
operator|&&
name|statistics
operator|.
name|getEngine
argument_list|()
operator|==
literal|null
condition|)
block|{
name|statistics
operator|.
name|setEngine
argument_list|(
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
name|Constants
operator|.
name|HIVE_ENGINE
argument_list|)
expr_stmt|;
block|}
comment|// Statistics will have an associated write Id for a transactional table. We need it to update column statistics.
name|partition
operator|.
name|setWriteId
argument_list|(
name|partitionSpec
operator|.
name|getWriteId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|partition
return|;
block|}
specifier|private
name|void
name|addPartitions
parameter_list|(
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|,
name|long
name|writeId
parameter_list|)
throws|throws
name|HiveException
block|{
name|List
argument_list|<
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
argument_list|>
name|outPartitions
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|desc
operator|.
name|getReplicationSpec
argument_list|()
operator|.
name|isInReplicationScope
argument_list|()
condition|)
block|{
name|outPartitions
operator|=
name|addPartitionsNoReplication
argument_list|(
name|table
argument_list|,
name|partitions
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outPartitions
operator|=
name|addPartitionsWithReplication
argument_list|(
name|table
argument_list|,
name|partitions
argument_list|,
name|writeId
argument_list|)
expr_stmt|;
block|}
for|for
control|(
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
name|outPartition
range|:
name|outPartitions
control|)
block|{
name|DDLUtils
operator|.
name|addIfAbsentByName
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|outPartition
argument_list|,
name|WriteEntity
operator|.
name|WriteType
operator|.
name|INSERT
argument_list|)
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|List
argument_list|<
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
argument_list|>
name|addPartitionsNoReplication
parameter_list|(
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// TODO: normally, the result is not necessary; might make sense to pass false
name|List
argument_list|<
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
argument_list|>
name|outPartitions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Partition
name|outPart
range|:
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|addPartition
argument_list|(
name|partitions
argument_list|,
name|desc
operator|.
name|isIfNotExists
argument_list|()
argument_list|,
literal|true
argument_list|)
control|)
block|{
name|outPartitions
operator|.
name|add
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
name|outPart
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|outPartitions
return|;
block|}
specifier|private
name|List
argument_list|<
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
argument_list|>
name|addPartitionsWithReplication
parameter_list|(
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|,
name|long
name|writeId
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// For replication add-ptns, we need to follow a insert-if-not-exist, alter-if-exists scenario.
comment|// TODO : ideally, we should push this mechanism to the metastore, because, otherwise, we have
comment|// no choice but to iterate over the partitions here.
name|List
argument_list|<
name|Partition
argument_list|>
name|partitionsToAdd
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|partitionssToAlter
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partitionNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Partition
name|partition
range|:
name|partitions
control|)
block|{
name|partitionNames
operator|.
name|add
argument_list|(
name|getPartitionName
argument_list|(
name|table
argument_list|,
name|partition
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|Partition
name|p
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getPartition
argument_list|(
name|desc
operator|.
name|getDbName
argument_list|()
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partition
operator|.
name|getValues
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|getReplicationSpec
argument_list|()
operator|.
name|allowReplacementInto
argument_list|(
name|p
operator|.
name|getParameters
argument_list|()
argument_list|)
condition|)
block|{
name|ReplicationSpec
operator|.
name|copyLastReplId
argument_list|(
name|p
operator|.
name|getParameters
argument_list|()
argument_list|,
name|partition
operator|.
name|getParameters
argument_list|()
argument_list|)
expr_stmt|;
name|partitionssToAlter
operator|.
name|add
argument_list|(
name|partition
argument_list|)
expr_stmt|;
block|}
comment|// else ptn already exists, but we do nothing with it.
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|NoSuchObjectException
condition|)
block|{
comment|// if the object does not exist, we want to add it.
name|partitionsToAdd
operator|.
name|add
argument_list|(
name|partition
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|e
throw|;
block|}
block|}
block|}
name|List
argument_list|<
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
argument_list|>
name|outPartitions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Partition
name|outPartition
range|:
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|addPartition
argument_list|(
name|partitionsToAdd
argument_list|,
name|desc
operator|.
name|isIfNotExists
argument_list|()
argument_list|,
literal|true
argument_list|)
control|)
block|{
name|outPartitions
operator|.
name|add
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
name|outPartition
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// In case of replication, statistics is obtained from the source, so do not update those on replica.
name|EnvironmentContext
name|ec
init|=
operator|new
name|EnvironmentContext
argument_list|()
decl_stmt|;
name|ec
operator|.
name|putToProperties
argument_list|(
name|StatsSetupConst
operator|.
name|DO_NOT_UPDATE_STATS
argument_list|,
name|StatsSetupConst
operator|.
name|TRUE
argument_list|)
expr_stmt|;
name|String
name|validWriteIdList
init|=
name|getValidWriteIdList
argument_list|(
name|table
argument_list|,
name|writeId
argument_list|)
decl_stmt|;
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|alterPartitions
argument_list|(
name|desc
operator|.
name|getDbName
argument_list|()
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partitionssToAlter
argument_list|,
name|ec
argument_list|,
name|validWriteIdList
argument_list|,
name|writeId
argument_list|)
expr_stmt|;
for|for
control|(
name|Partition
name|outPartition
range|:
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getPartitionsByNames
argument_list|(
name|desc
operator|.
name|getDbName
argument_list|()
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partitionNames
argument_list|)
control|)
block|{
name|outPartitions
operator|.
name|add
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
name|outPartition
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|outPartitions
return|;
block|}
specifier|private
name|String
name|getPartitionName
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|partition
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
return|return
name|Warehouse
operator|.
name|makePartName
argument_list|(
name|table
operator|.
name|getPartitionKeys
argument_list|()
argument_list|,
name|partition
operator|.
name|getValues
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|String
name|getValidWriteIdList
parameter_list|(
name|Table
name|table
parameter_list|,
name|long
name|writeId
parameter_list|)
throws|throws
name|LockException
block|{
if|if
condition|(
name|desc
operator|.
name|getReplicationSpec
argument_list|()
operator|.
name|isInReplicationScope
argument_list|()
operator|&&
name|desc
operator|.
name|getPartitions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getWriteId
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// We need a valid writeId list for a transactional change. During replication we do not
comment|// have a valid writeId list which was used for this on the source. But we know for sure
comment|// that the writeId associated with it was valid then (otherwise the change would have
comment|// failed on the source). So use a valid transaction list with only that writeId.
return|return
operator|new
name|ValidReaderWriteIdList
argument_list|(
name|TableName
operator|.
name|getDbTable
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|,
operator|new
name|long
index|[
literal|0
index|]
argument_list|,
operator|new
name|BitSet
argument_list|()
argument_list|,
name|writeId
argument_list|)
operator|.
name|writeToString
argument_list|()
return|;
block|}
else|else
block|{
name|AcidUtils
operator|.
name|TableSnapshot
name|tableSnapshot
init|=
name|AcidUtils
operator|.
name|getTableSnapshot
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|table
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableSnapshot
operator|!=
literal|null
operator|&&
name|tableSnapshot
operator|.
name|getWriteId
argument_list|()
operator|>
literal|0
condition|)
block|{
return|return
name|tableSnapshot
operator|.
name|getValidWriteIdList
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

