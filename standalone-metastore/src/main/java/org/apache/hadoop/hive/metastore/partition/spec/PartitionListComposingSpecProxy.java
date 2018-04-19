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
name|metastore
operator|.
name|partition
operator|.
name|spec
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
name|PartitionListComposingSpec
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
name|PartitionSpec
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
comment|/**  * PartitionSpecProxy implementation that composes a List of Partitions.  */
end_comment

begin_class
specifier|public
class|class
name|PartitionListComposingSpecProxy
extends|extends
name|PartitionSpecProxy
block|{
specifier|private
name|PartitionSpec
name|partitionSpec
decl_stmt|;
specifier|protected
name|PartitionListComposingSpecProxy
parameter_list|(
name|PartitionSpec
name|partitionSpec
parameter_list|)
throws|throws
name|MetaException
block|{
assert|assert
name|partitionSpec
operator|.
name|isSetPartitionList
argument_list|()
operator|:
literal|"Partition-list should have been set."
assert|;
name|PartitionListComposingSpec
name|partitionList
init|=
name|partitionSpec
operator|.
name|getPartitionList
argument_list|()
decl_stmt|;
if|if
condition|(
name|partitionList
operator|==
literal|null
operator|||
name|partitionList
operator|.
name|getPartitions
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"The partition list cannot be null."
argument_list|)
throw|;
block|}
for|for
control|(
name|Partition
name|partition
range|:
name|partitionList
operator|.
name|getPartitions
argument_list|()
control|)
block|{
if|if
condition|(
name|partition
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Partition cannot be null."
argument_list|)
throw|;
block|}
if|if
condition|(
name|partition
operator|.
name|getValues
argument_list|()
operator|==
literal|null
operator|||
name|partition
operator|.
name|getValues
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"The partition value list cannot be null or empty."
argument_list|)
throw|;
block|}
if|if
condition|(
name|partition
operator|.
name|getValues
argument_list|()
operator|.
name|contains
argument_list|(
literal|null
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Partition value cannot be null."
argument_list|)
throw|;
block|}
block|}
name|this
operator|.
name|partitionSpec
operator|=
name|partitionSpec
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getCatName
parameter_list|()
block|{
return|return
name|partitionSpec
operator|.
name|getCatName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDbName
parameter_list|()
block|{
return|return
name|partitionSpec
operator|.
name|getDbName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|partitionSpec
operator|.
name|getTableName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|PartitionIterator
name|getPartitionIterator
parameter_list|()
block|{
return|return
operator|new
name|Iterator
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|PartitionSpec
argument_list|>
name|toPartitionSpec
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|partitionSpec
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|partitionSpec
operator|.
name|getPartitionList
argument_list|()
operator|.
name|getPartitionsSize
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setCatName
parameter_list|(
name|String
name|catName
parameter_list|)
block|{
name|partitionSpec
operator|.
name|setCatName
argument_list|(
name|catName
argument_list|)
expr_stmt|;
for|for
control|(
name|Partition
name|partition
range|:
name|partitionSpec
operator|.
name|getPartitionList
argument_list|()
operator|.
name|getPartitions
argument_list|()
control|)
block|{
name|partition
operator|.
name|setCatName
argument_list|(
name|catName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setDbName
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
name|partitionSpec
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
for|for
control|(
name|Partition
name|partition
range|:
name|partitionSpec
operator|.
name|getPartitionList
argument_list|()
operator|.
name|getPartitions
argument_list|()
control|)
block|{
name|partition
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setTableName
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|partitionSpec
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
for|for
control|(
name|Partition
name|partition
range|:
name|partitionSpec
operator|.
name|getPartitionList
argument_list|()
operator|.
name|getPartitions
argument_list|()
control|)
block|{
name|partition
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setRootLocation
parameter_list|(
name|String
name|newRootPath
parameter_list|)
throws|throws
name|MetaException
block|{
name|String
name|oldRootPath
init|=
name|partitionSpec
operator|.
name|getRootPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|oldRootPath
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"No common root-path. Can't replace root-path!"
argument_list|)
throw|;
block|}
if|if
condition|(
name|newRootPath
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Root path cannot be null."
argument_list|)
throw|;
block|}
for|for
control|(
name|Partition
name|partition
range|:
name|partitionSpec
operator|.
name|getPartitionList
argument_list|()
operator|.
name|getPartitions
argument_list|()
control|)
block|{
name|String
name|location
init|=
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
decl_stmt|;
if|if
condition|(
name|location
operator|.
name|startsWith
argument_list|(
name|oldRootPath
argument_list|)
condition|)
block|{
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|setLocation
argument_list|(
name|location
operator|.
name|replace
argument_list|(
name|oldRootPath
argument_list|,
name|newRootPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Common root-path not found. Can't replace root-path!"
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|Iterator
implements|implements
name|PartitionIterator
block|{
name|PartitionListComposingSpecProxy
name|partitionSpecProxy
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|partitionList
decl_stmt|;
name|int
name|index
decl_stmt|;
specifier|public
name|Iterator
parameter_list|(
name|PartitionListComposingSpecProxy
name|partitionSpecProxy
parameter_list|)
block|{
name|this
operator|.
name|partitionSpecProxy
operator|=
name|partitionSpecProxy
expr_stmt|;
name|this
operator|.
name|partitionList
operator|=
name|partitionSpecProxy
operator|.
name|partitionSpec
operator|.
name|getPartitionList
argument_list|()
operator|.
name|getPartitions
argument_list|()
expr_stmt|;
name|this
operator|.
name|index
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Partition
name|getCurrent
parameter_list|()
block|{
return|return
name|partitionList
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getCatName
parameter_list|()
block|{
return|return
name|partitionSpecProxy
operator|.
name|getCatName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDbName
parameter_list|()
block|{
return|return
name|partitionSpecProxy
operator|.
name|getDbName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|partitionSpecProxy
operator|.
name|getTableName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getParameters
parameter_list|()
block|{
return|return
name|partitionList
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|getParameters
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setParameters
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|)
block|{
name|partitionList
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|setParameters
argument_list|(
name|parameters
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|partitionList
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|putToParameters
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|partitionList
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|putToParameters
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setCreateTime
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|partitionList
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|setCreateTime
argument_list|(
operator|(
name|int
operator|)
name|time
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|index
operator|<
name|partitionList
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Partition
name|next
parameter_list|()
block|{
return|return
name|partitionList
operator|.
name|get
argument_list|(
name|index
operator|++
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
name|partitionList
operator|.
name|remove
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
block|}
comment|// class Iterator;
block|}
end_class

begin_comment
comment|// class PartitionListComposingSpecProxy;
end_comment

end_unit

