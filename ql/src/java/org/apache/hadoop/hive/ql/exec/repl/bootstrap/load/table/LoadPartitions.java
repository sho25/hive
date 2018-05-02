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
name|exec
operator|.
name|repl
operator|.
name|bootstrap
operator|.
name|load
operator|.
name|table
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
name|Database
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
name|utils
operator|.
name|MetaStoreUtils
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
name|ReplCopyTask
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
name|exec
operator|.
name|repl
operator|.
name|ReplStateLogWork
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
name|repl
operator|.
name|bootstrap
operator|.
name|AddDependencyToLeaves
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
name|repl
operator|.
name|bootstrap
operator|.
name|events
operator|.
name|TableEvent
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
name|repl
operator|.
name|bootstrap
operator|.
name|load
operator|.
name|ReplicationState
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
name|repl
operator|.
name|bootstrap
operator|.
name|load
operator|.
name|TaskTracker
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
name|repl
operator|.
name|bootstrap
operator|.
name|load
operator|.
name|util
operator|.
name|Context
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
name|repl
operator|.
name|bootstrap
operator|.
name|load
operator|.
name|util
operator|.
name|PathUtils
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
name|util
operator|.
name|DAGTraversal
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
name|parse
operator|.
name|ImportSemanticAnalyzer
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
name|repl
operator|.
name|ReplLogger
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
name|AddPartitionDesc
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
name|DDLWork
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
name|ImportTableDesc
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
name|LoadMultiFilesDesc
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
name|LoadTableDesc
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
name|LoadTableDesc
operator|.
name|LoadFileType
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
name|MoveWork
import|;
end_import

begin_import
import|import
name|org
operator|.
name|datanucleus
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
name|io
operator|.
name|Serializable
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
import|import static
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
name|repl
operator|.
name|bootstrap
operator|.
name|load
operator|.
name|ReplicationState
operator|.
name|PartitionState
import|;
end_import

begin_import
import|import static
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
name|ImportSemanticAnalyzer
operator|.
name|isPartitioned
import|;
end_import

begin_import
import|import static
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
name|ImportSemanticAnalyzer
operator|.
name|partSpecToString
import|;
end_import

begin_class
specifier|public
class|class
name|LoadPartitions
block|{
specifier|private
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LoadPartitions
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Context
name|context
decl_stmt|;
specifier|private
specifier|final
name|ReplLogger
name|replLogger
decl_stmt|;
specifier|private
specifier|final
name|TableContext
name|tableContext
decl_stmt|;
specifier|private
specifier|final
name|TableEvent
name|event
decl_stmt|;
specifier|private
specifier|final
name|TaskTracker
name|tracker
decl_stmt|;
specifier|private
specifier|final
name|AddPartitionDesc
name|lastReplicatedPartition
decl_stmt|;
specifier|private
specifier|final
name|ImportTableDesc
name|tableDesc
decl_stmt|;
specifier|private
name|Table
name|table
decl_stmt|;
specifier|public
name|LoadPartitions
parameter_list|(
name|Context
name|context
parameter_list|,
name|ReplLogger
name|replLogger
parameter_list|,
name|TaskTracker
name|tableTracker
parameter_list|,
name|TableEvent
name|event
parameter_list|,
name|String
name|dbNameToLoadIn
parameter_list|,
name|TableContext
name|tableContext
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|this
argument_list|(
name|context
argument_list|,
name|replLogger
argument_list|,
name|tableContext
argument_list|,
name|tableTracker
argument_list|,
name|event
argument_list|,
name|dbNameToLoadIn
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|LoadPartitions
parameter_list|(
name|Context
name|context
parameter_list|,
name|ReplLogger
name|replLogger
parameter_list|,
name|TableContext
name|tableContext
parameter_list|,
name|TaskTracker
name|limiter
parameter_list|,
name|TableEvent
name|event
parameter_list|,
name|String
name|dbNameToLoadIn
parameter_list|,
name|AddPartitionDesc
name|lastReplicatedPartition
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|this
operator|.
name|tracker
operator|=
operator|new
name|TaskTracker
argument_list|(
name|limiter
argument_list|)
expr_stmt|;
name|this
operator|.
name|event
operator|=
name|event
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|replLogger
operator|=
name|replLogger
expr_stmt|;
name|this
operator|.
name|lastReplicatedPartition
operator|=
name|lastReplicatedPartition
expr_stmt|;
name|this
operator|.
name|tableContext
operator|=
name|tableContext
expr_stmt|;
name|this
operator|.
name|tableDesc
operator|=
name|tableContext
operator|.
name|overrideProperties
argument_list|(
name|event
operator|.
name|tableDesc
argument_list|(
name|dbNameToLoadIn
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|ImportSemanticAnalyzer
operator|.
name|tableIfExists
argument_list|(
name|tableDesc
argument_list|,
name|context
operator|.
name|hiveDb
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|location
parameter_list|()
throws|throws
name|MetaException
throws|,
name|HiveException
block|{
name|Database
name|parentDb
init|=
name|context
operator|.
name|hiveDb
operator|.
name|getDatabase
argument_list|(
name|tableDesc
operator|.
name|getDatabaseName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|tableContext
operator|.
name|waitOnPrecursor
argument_list|()
condition|)
block|{
return|return
name|context
operator|.
name|warehouse
operator|.
name|getDefaultTablePath
argument_list|(
name|parentDb
argument_list|,
name|tableDesc
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
else|else
block|{
name|Path
name|tablePath
init|=
operator|new
name|Path
argument_list|(
name|context
operator|.
name|warehouse
operator|.
name|getDefaultDatabasePath
argument_list|(
name|tableDesc
operator|.
name|getDatabaseName
argument_list|()
argument_list|)
argument_list|,
name|MetaStoreUtils
operator|.
name|encodeTableName
argument_list|(
name|tableDesc
operator|.
name|getTableName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|context
operator|.
name|warehouse
operator|.
name|getDnsPath
argument_list|(
name|tablePath
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
specifier|private
name|void
name|createTableReplLogTask
parameter_list|()
throws|throws
name|SemanticException
block|{
name|ReplStateLogWork
name|replLogWork
init|=
operator|new
name|ReplStateLogWork
argument_list|(
name|replLogger
argument_list|,
name|tableDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|tableDesc
operator|.
name|tableType
argument_list|()
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|ReplStateLogWork
argument_list|>
name|replLogTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|replLogWork
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
if|if
condition|(
name|tracker
operator|.
name|tasks
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tracker
operator|.
name|addTask
argument_list|(
name|replLogTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|DAGTraversal
operator|.
name|traverse
argument_list|(
name|tracker
operator|.
name|tasks
argument_list|()
argument_list|,
operator|new
name|AddDependencyToLeaves
argument_list|(
name|replLogTask
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|visited
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|tracker
operator|.
name|updateTaskCount
argument_list|(
name|replLogTask
argument_list|,
name|visited
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|TaskTracker
name|tasks
parameter_list|()
throws|throws
name|SemanticException
block|{
try|try
block|{
comment|/*       We are doing this both in load table and load partitions        */
if|if
condition|(
name|tableDesc
operator|.
name|getLocation
argument_list|()
operator|==
literal|null
condition|)
block|{
name|tableDesc
operator|.
name|setLocation
argument_list|(
name|location
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|table
operator|==
literal|null
condition|)
block|{
comment|//new table
name|table
operator|=
name|tableDesc
operator|.
name|toTable
argument_list|(
name|context
operator|.
name|hiveConf
argument_list|)
expr_stmt|;
if|if
condition|(
name|isPartitioned
argument_list|(
name|tableDesc
argument_list|)
condition|)
block|{
name|updateReplicationState
argument_list|(
name|initialReplicationState
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|forNewTable
argument_list|()
operator|.
name|hasReplicationState
argument_list|()
condition|)
block|{
comment|// Add ReplStateLogTask only if no pending table load tasks left for next cycle
name|createTableReplLogTask
argument_list|()
expr_stmt|;
block|}
return|return
name|tracker
return|;
block|}
block|}
else|else
block|{
comment|// existing
if|if
condition|(
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|AddPartitionDesc
argument_list|>
name|partitionDescs
init|=
name|event
operator|.
name|partitionDescriptions
argument_list|(
name|tableDesc
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|event
operator|.
name|replicationSpec
argument_list|()
operator|.
name|isMetadataOnly
argument_list|()
operator|&&
operator|!
name|partitionDescs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|updateReplicationState
argument_list|(
name|initialReplicationState
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|forExistingTable
argument_list|(
name|lastReplicatedPartition
argument_list|)
operator|.
name|hasReplicationState
argument_list|()
condition|)
block|{
comment|// Add ReplStateLogTask only if no pending table load tasks left for next cycle
name|createTableReplLogTask
argument_list|()
expr_stmt|;
block|}
return|return
name|tracker
return|;
block|}
block|}
block|}
return|return
name|tracker
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
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|updateReplicationState
parameter_list|(
name|ReplicationState
name|replicationState
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
operator|!
name|tracker
operator|.
name|canAddMoreTasks
argument_list|()
condition|)
block|{
name|tracker
operator|.
name|setReplicationState
argument_list|(
name|replicationState
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|ReplicationState
name|initialReplicationState
parameter_list|()
throws|throws
name|SemanticException
block|{
return|return
operator|new
name|ReplicationState
argument_list|(
operator|new
name|PartitionState
argument_list|(
name|tableDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|lastReplicatedPartition
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|TaskTracker
name|forNewTable
parameter_list|()
throws|throws
name|Exception
block|{
name|Iterator
argument_list|<
name|AddPartitionDesc
argument_list|>
name|iterator
init|=
name|event
operator|.
name|partitionDescriptions
argument_list|(
name|tableDesc
argument_list|)
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
operator|&&
name|tracker
operator|.
name|canAddMoreTasks
argument_list|()
condition|)
block|{
name|AddPartitionDesc
name|currentPartitionDesc
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
comment|/*        the currentPartitionDesc cannot be inlined as we need the hasNext() to be evaluated post the        current retrieved lastReplicatedPartition       */
name|addPartition
argument_list|(
name|iterator
operator|.
name|hasNext
argument_list|()
argument_list|,
name|currentPartitionDesc
argument_list|)
expr_stmt|;
block|}
return|return
name|tracker
return|;
block|}
specifier|private
name|void
name|addPartition
parameter_list|(
name|boolean
name|hasMorePartitions
parameter_list|,
name|AddPartitionDesc
name|addPartitionDesc
parameter_list|)
throws|throws
name|Exception
block|{
name|tracker
operator|.
name|addTask
argument_list|(
name|tasksForAddPartition
argument_list|(
name|table
argument_list|,
name|addPartitionDesc
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasMorePartitions
operator|&&
operator|!
name|tracker
operator|.
name|canAddMoreTasks
argument_list|()
condition|)
block|{
name|ReplicationState
name|currentReplicationState
init|=
operator|new
name|ReplicationState
argument_list|(
operator|new
name|PartitionState
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|addPartitionDesc
argument_list|)
argument_list|)
decl_stmt|;
name|updateReplicationState
argument_list|(
name|currentReplicationState
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * returns the root task for adding a partition    */
specifier|private
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tasksForAddPartition
parameter_list|(
name|Table
name|table
parameter_list|,
name|AddPartitionDesc
name|addPartitionDesc
parameter_list|)
throws|throws
name|MetaException
throws|,
name|IOException
throws|,
name|HiveException
block|{
name|AddPartitionDesc
operator|.
name|OnePartitionDesc
name|partSpec
init|=
name|addPartitionDesc
operator|.
name|getPartition
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Path
name|sourceWarehousePartitionLocation
init|=
operator|new
name|Path
argument_list|(
name|partSpec
operator|.
name|getLocation
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|replicaWarehousePartitionLocation
init|=
name|locationOnReplicaWarehouse
argument_list|(
name|table
argument_list|,
name|partSpec
argument_list|)
decl_stmt|;
name|partSpec
operator|.
name|setLocation
argument_list|(
name|replicaWarehousePartitionLocation
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"adding dependent CopyWork/AddPart/MoveWork for partition "
operator|+
name|partSpecToString
argument_list|(
name|partSpec
operator|.
name|getPartSpec
argument_list|()
argument_list|)
operator|+
literal|" with source location: "
operator|+
name|partSpec
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|tmpPath
init|=
name|PathUtils
operator|.
name|getExternalTmpPath
argument_list|(
name|replicaWarehousePartitionLocation
argument_list|,
name|context
operator|.
name|pathInfo
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|copyTask
init|=
name|ReplCopyTask
operator|.
name|getLoadCopyTask
argument_list|(
name|event
operator|.
name|replicationSpec
argument_list|()
argument_list|,
name|sourceWarehousePartitionLocation
argument_list|,
name|tmpPath
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|addPartTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
name|addPartitionDesc
argument_list|)
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|movePartitionTask
init|=
name|movePartitionTask
argument_list|(
name|table
argument_list|,
name|partSpec
argument_list|,
name|tmpPath
argument_list|)
decl_stmt|;
name|copyTask
operator|.
name|addDependentTask
argument_list|(
name|addPartTask
argument_list|)
expr_stmt|;
name|addPartTask
operator|.
name|addDependentTask
argument_list|(
name|movePartitionTask
argument_list|)
expr_stmt|;
return|return
name|copyTask
return|;
block|}
comment|/**    * This will create the move of partition data from temp path to actual path    */
specifier|private
name|Task
argument_list|<
name|?
argument_list|>
name|movePartitionTask
parameter_list|(
name|Table
name|table
parameter_list|,
name|AddPartitionDesc
operator|.
name|OnePartitionDesc
name|partSpec
parameter_list|,
name|Path
name|tmpPath
parameter_list|)
block|{
name|MoveWork
name|moveWork
init|=
operator|new
name|MoveWork
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|AcidUtils
operator|.
name|isTransactionalTable
argument_list|(
name|table
argument_list|)
condition|)
block|{
name|LoadMultiFilesDesc
name|loadFilesWork
init|=
operator|new
name|LoadMultiFilesDesc
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|tmpPath
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|Path
argument_list|(
name|partSpec
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|moveWork
operator|.
name|setMultiFilesDesc
argument_list|(
name|loadFilesWork
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LoadTableDesc
name|loadTableWork
init|=
operator|new
name|LoadTableDesc
argument_list|(
name|tmpPath
argument_list|,
name|Utilities
operator|.
name|getTableDesc
argument_list|(
name|table
argument_list|)
argument_list|,
name|partSpec
operator|.
name|getPartSpec
argument_list|()
argument_list|,
name|event
operator|.
name|replicationSpec
argument_list|()
operator|.
name|isReplace
argument_list|()
condition|?
name|LoadFileType
operator|.
name|REPLACE_ALL
else|:
name|LoadFileType
operator|.
name|OVERWRITE_EXISTING
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|loadTableWork
operator|.
name|setInheritTableSpecs
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|moveWork
operator|.
name|setLoadTableWork
argument_list|(
name|loadTableWork
argument_list|)
expr_stmt|;
block|}
return|return
name|TaskFactory
operator|.
name|get
argument_list|(
name|moveWork
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
return|;
block|}
specifier|private
name|Path
name|locationOnReplicaWarehouse
parameter_list|(
name|Table
name|table
parameter_list|,
name|AddPartitionDesc
operator|.
name|OnePartitionDesc
name|partSpec
parameter_list|)
throws|throws
name|MetaException
throws|,
name|HiveException
throws|,
name|IOException
block|{
name|String
name|child
init|=
name|Warehouse
operator|.
name|makePartPath
argument_list|(
name|partSpec
operator|.
name|getPartSpec
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableDesc
operator|.
name|getLocation
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|table
operator|.
name|getDataLocation
argument_list|()
operator|==
literal|null
condition|)
block|{
name|Database
name|parentDb
init|=
name|context
operator|.
name|hiveDb
operator|.
name|getDatabase
argument_list|(
name|tableDesc
operator|.
name|getDatabaseName
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|context
operator|.
name|warehouse
operator|.
name|getDefaultTablePath
argument_list|(
name|parentDb
argument_list|,
name|tableDesc
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|,
name|child
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|Path
argument_list|(
name|table
operator|.
name|getDataLocation
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|child
argument_list|)
return|;
block|}
block|}
else|else
block|{
return|return
operator|new
name|Path
argument_list|(
name|tableDesc
operator|.
name|getLocation
argument_list|()
argument_list|,
name|child
argument_list|)
return|;
block|}
block|}
specifier|private
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|alterSinglePartition
parameter_list|(
name|AddPartitionDesc
name|desc
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|,
name|Partition
name|ptn
parameter_list|)
block|{
name|desc
operator|.
name|setReplaceMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|replicationSpec
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
operator|)
condition|)
block|{
name|desc
operator|.
name|setReplicationSpec
argument_list|(
name|replicationSpec
argument_list|)
expr_stmt|;
block|}
name|desc
operator|.
name|getPartition
argument_list|(
literal|0
argument_list|)
operator|.
name|setLocation
argument_list|(
name|ptn
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
comment|// use existing location
return|return
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
name|desc
argument_list|)
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
return|;
block|}
specifier|private
name|TaskTracker
name|forExistingTable
parameter_list|(
name|AddPartitionDesc
name|lastPartitionReplicated
parameter_list|)
throws|throws
name|Exception
block|{
name|boolean
name|encounteredTheLastReplicatedPartition
init|=
operator|(
name|lastPartitionReplicated
operator|==
literal|null
operator|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|lastReplicatedPartSpec
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|encounteredTheLastReplicatedPartition
condition|)
block|{
name|lastReplicatedPartSpec
operator|=
name|lastPartitionReplicated
operator|.
name|getPartition
argument_list|(
literal|0
argument_list|)
operator|.
name|getPartSpec
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Start processing from partition info spec : {}"
argument_list|,
name|StringUtils
operator|.
name|mapToString
argument_list|(
name|lastReplicatedPartSpec
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ReplicationSpec
name|replicationSpec
init|=
name|event
operator|.
name|replicationSpec
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|AddPartitionDesc
argument_list|>
name|partitionIterator
init|=
name|event
operator|.
name|partitionDescriptions
argument_list|(
name|tableDesc
argument_list|)
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|encounteredTheLastReplicatedPartition
operator|&&
name|partitionIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|AddPartitionDesc
name|addPartitionDesc
init|=
name|partitionIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|currentSpec
init|=
name|addPartitionDesc
operator|.
name|getPartition
argument_list|(
literal|0
argument_list|)
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
name|encounteredTheLastReplicatedPartition
operator|=
name|lastReplicatedPartSpec
operator|.
name|equals
argument_list|(
name|currentSpec
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
name|partitionIterator
operator|.
name|hasNext
argument_list|()
operator|&&
name|tracker
operator|.
name|canAddMoreTasks
argument_list|()
condition|)
block|{
name|AddPartitionDesc
name|addPartitionDesc
init|=
name|partitionIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|addPartitionDesc
operator|.
name|getPartition
argument_list|(
literal|0
argument_list|)
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
name|Partition
name|ptn
init|=
name|context
operator|.
name|hiveDb
operator|.
name|getPartition
argument_list|(
name|table
argument_list|,
name|partSpec
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|ptn
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|replicationSpec
operator|.
name|isMetadataOnly
argument_list|()
condition|)
block|{
name|addPartition
argument_list|(
name|partitionIterator
operator|.
name|hasNext
argument_list|()
argument_list|,
name|addPartitionDesc
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// If replicating, then the partition already existing means we need to replace, maybe, if
comment|// the destination ptn's repl.last.id is older than the replacement's.
if|if
condition|(
name|replicationSpec
operator|.
name|allowReplacementInto
argument_list|(
name|ptn
operator|.
name|getParameters
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|replicationSpec
operator|.
name|isMetadataOnly
argument_list|()
condition|)
block|{
name|tracker
operator|.
name|addTask
argument_list|(
name|alterSinglePartition
argument_list|(
name|addPartitionDesc
argument_list|,
name|replicationSpec
argument_list|,
name|ptn
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|tracker
operator|.
name|canAddMoreTasks
argument_list|()
condition|)
block|{
name|tracker
operator|.
name|setReplicationState
argument_list|(
operator|new
name|ReplicationState
argument_list|(
operator|new
name|PartitionState
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|addPartitionDesc
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|addPartition
argument_list|(
name|partitionIterator
operator|.
name|hasNext
argument_list|()
argument_list|,
name|addPartitionDesc
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// ignore this ptn, do nothing, not an error.
block|}
block|}
block|}
return|return
name|tracker
return|;
block|}
block|}
end_class

end_unit

