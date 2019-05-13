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
name|events
operator|.
name|filesystem
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
name|ql
operator|.
name|ddl
operator|.
name|table
operator|.
name|partition
operator|.
name|AlterTableAddPartitionDesc
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
name|PartitionEvent
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
name|plan
operator|.
name|ImportTableDesc
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

begin_class
specifier|public
class|class
name|FSPartitionEvent
implements|implements
name|PartitionEvent
block|{
specifier|private
specifier|final
name|ReplicationState
name|replicationState
decl_stmt|;
specifier|private
specifier|final
name|TableEvent
name|tableEvent
decl_stmt|;
name|FSPartitionEvent
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|metadataDir
parameter_list|,
name|ReplicationState
name|replicationState
parameter_list|)
block|{
name|tableEvent
operator|=
operator|new
name|FSTableEvent
argument_list|(
name|hiveConf
argument_list|,
name|metadataDir
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationState
operator|=
name|replicationState
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|EventType
name|eventType
parameter_list|()
block|{
return|return
name|EventType
operator|.
name|Partition
return|;
block|}
annotation|@
name|Override
specifier|public
name|AlterTableAddPartitionDesc
name|lastPartitionReplicated
parameter_list|()
block|{
assert|assert
name|replicationState
operator|!=
literal|null
operator|&&
name|replicationState
operator|.
name|partitionState
operator|!=
literal|null
assert|;
return|return
name|replicationState
operator|.
name|partitionState
operator|.
name|lastReplicatedPartition
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableEvent
name|asTableEvent
parameter_list|()
block|{
return|return
name|tableEvent
return|;
block|}
annotation|@
name|Override
specifier|public
name|ImportTableDesc
name|tableDesc
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|tableEvent
operator|.
name|tableDesc
argument_list|(
name|dbName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|AlterTableAddPartitionDesc
argument_list|>
name|partitionDescriptions
parameter_list|(
name|ImportTableDesc
name|tblDesc
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|tableEvent
operator|.
name|partitionDescriptions
argument_list|(
name|tblDesc
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|partitions
parameter_list|(
name|ImportTableDesc
name|tblDesc
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|tableEvent
operator|.
name|partitions
argument_list|(
name|tblDesc
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationSpec
name|replicationSpec
parameter_list|()
block|{
return|return
name|tableEvent
operator|.
name|replicationSpec
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldNotReplicate
parameter_list|()
block|{
return|return
name|tableEvent
operator|.
name|shouldNotReplicate
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|metadataPath
parameter_list|()
block|{
return|return
name|tableEvent
operator|.
name|metadataPath
argument_list|()
return|;
block|}
block|}
end_class

end_unit

