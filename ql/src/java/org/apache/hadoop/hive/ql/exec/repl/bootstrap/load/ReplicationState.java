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
package|;
end_package

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
name|add
operator|.
name|AlterTableAddPartitionDesc
import|;
end_import

begin_class
specifier|public
class|class
name|ReplicationState
implements|implements
name|Serializable
block|{
specifier|public
specifier|static
class|class
name|PartitionState
block|{
specifier|final
name|String
name|tableName
decl_stmt|;
specifier|public
specifier|final
name|AlterTableAddPartitionDesc
name|lastReplicatedPartition
decl_stmt|;
specifier|public
name|PartitionState
parameter_list|(
name|String
name|tableName
parameter_list|,
name|AlterTableAddPartitionDesc
name|lastReplicatedPartition
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|lastReplicatedPartition
operator|=
name|lastReplicatedPartition
expr_stmt|;
block|}
block|}
comment|// null :: for non - partitioned table.
specifier|public
specifier|final
name|PartitionState
name|partitionState
decl_stmt|;
comment|// for non partitioned table this will represent the last tableName replicated, else its the name of the
comment|// current partitioned table with last partition replicated denoted by "lastPartitionReplicated"
specifier|public
specifier|final
name|String
name|lastTableReplicated
decl_stmt|;
comment|// last function name is replicated, null if function replication was in progress when we created this state.
specifier|public
specifier|final
name|String
name|functionName
decl_stmt|;
specifier|public
name|ReplicationState
parameter_list|(
name|PartitionState
name|partitionState
parameter_list|)
block|{
name|this
operator|.
name|partitionState
operator|=
name|partitionState
expr_stmt|;
name|this
operator|.
name|functionName
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|lastTableReplicated
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"ReplicationState{"
operator|+
literal|", partitionState="
operator|+
name|partitionState
operator|+
literal|", lastTableReplicated='"
operator|+
name|lastTableReplicated
operator|+
literal|'\''
operator|+
literal|", functionName='"
operator|+
name|functionName
operator|+
literal|'\''
operator|+
literal|'}'
return|;
block|}
block|}
end_class

end_unit

