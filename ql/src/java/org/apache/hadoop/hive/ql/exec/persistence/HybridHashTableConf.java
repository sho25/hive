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
name|persistence
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

begin_comment
comment|/**  * This conf class is a wrapper of a list of HybridHashTableContainers and some common info shared  * among them, which is used in n-way join (multiple small tables are involved).  */
end_comment

begin_class
specifier|public
class|class
name|HybridHashTableConf
block|{
specifier|private
name|List
argument_list|<
name|HybridHashTableContainer
argument_list|>
name|loadedContainerList
decl_stmt|;
comment|// A list of alrady loaded containers
specifier|private
name|int
name|numberOfPartitions
init|=
literal|0
decl_stmt|;
comment|// Number of partitions each table should have
specifier|private
name|int
name|nextSpillPartition
init|=
operator|-
literal|1
decl_stmt|;
comment|// The partition to be spilled next
specifier|public
name|HybridHashTableConf
parameter_list|()
block|{
name|loadedContainerList
operator|=
operator|new
name|ArrayList
argument_list|<
name|HybridHashTableContainer
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|int
name|getNumberOfPartitions
parameter_list|()
block|{
return|return
name|numberOfPartitions
return|;
block|}
specifier|public
name|void
name|setNumberOfPartitions
parameter_list|(
name|int
name|numberOfPartitions
parameter_list|)
block|{
name|this
operator|.
name|numberOfPartitions
operator|=
name|numberOfPartitions
expr_stmt|;
name|this
operator|.
name|nextSpillPartition
operator|=
name|numberOfPartitions
operator|-
literal|1
expr_stmt|;
block|}
specifier|public
name|int
name|getNextSpillPartition
parameter_list|()
block|{
return|return
name|this
operator|.
name|nextSpillPartition
return|;
block|}
specifier|public
name|void
name|setNextSpillPartition
parameter_list|(
name|int
name|nextSpillPartition
parameter_list|)
block|{
name|this
operator|.
name|nextSpillPartition
operator|=
name|nextSpillPartition
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|HybridHashTableContainer
argument_list|>
name|getLoadedContainerList
parameter_list|()
block|{
return|return
name|loadedContainerList
return|;
block|}
comment|/**    * Spill one in-memory partition from tail for all previously loaded HybridHashTableContainers.    * Also mark that partition number as spill-on-creation for future created containers.    * @return amount of memory freed; 0 if only one last partition is in memory for each container    */
specifier|public
name|long
name|spill
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|nextSpillPartition
operator|==
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
name|long
name|memFreed
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HybridHashTableContainer
name|container
range|:
name|loadedContainerList
control|)
block|{
name|memFreed
operator|+=
name|container
operator|.
name|spillPartition
argument_list|(
name|nextSpillPartition
argument_list|)
expr_stmt|;
name|container
operator|.
name|setSpill
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|nextSpillPartition
operator|--
expr_stmt|;
return|return
name|memFreed
return|;
block|}
comment|/**    * Check if a partition should be spilled directly on creation    * @param partitionId the partition to create    * @return true if it should be spilled directly, false otherwise    */
specifier|public
name|boolean
name|doSpillOnCreation
parameter_list|(
name|int
name|partitionId
parameter_list|)
block|{
return|return
name|nextSpillPartition
operator|!=
operator|-
literal|1
operator|&&
name|partitionId
operator|>
name|nextSpillPartition
return|;
block|}
block|}
end_class

end_unit

