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
name|PartitionSpec
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
comment|/**  * Polymorphic proxy class, equivalent to org.apache.hadoop.hive.metastore.api.PartitionSpec.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|PartitionSpecProxy
block|{
comment|/**    * The number of Partition instances represented by the PartitionSpec.    * @return Number of partitions.    */
specifier|public
specifier|abstract
name|int
name|size
parameter_list|()
function_decl|;
comment|/**    * Set catalog name.    * @param catName catalog name.    */
specifier|public
specifier|abstract
name|void
name|setCatName
parameter_list|(
name|String
name|catName
parameter_list|)
function_decl|;
comment|/**    * Setter for name of the DB.    * @param dbName The name of the DB.    */
specifier|public
specifier|abstract
name|void
name|setDbName
parameter_list|(
name|String
name|dbName
parameter_list|)
function_decl|;
comment|/**    * Setter for name of the table.    * @param tableName The name of the table.    */
specifier|public
specifier|abstract
name|void
name|setTableName
parameter_list|(
name|String
name|tableName
parameter_list|)
function_decl|;
comment|/**    * Get catalog name.    * @return catalog name.    */
specifier|public
specifier|abstract
name|String
name|getCatName
parameter_list|()
function_decl|;
comment|/**    * Getter for name of the DB.    * @return The name of the DB.    */
specifier|public
specifier|abstract
name|String
name|getDbName
parameter_list|()
function_decl|;
comment|/**    * Getter for name of the table.    * @return The name of the table.    */
specifier|public
specifier|abstract
name|String
name|getTableName
parameter_list|()
function_decl|;
comment|/**    * Iterator to the (virtual) sequence of Partitions represented by the PartitionSpec.    * @return A PartitionIterator to the beginning of the Partition sequence.    */
specifier|public
specifier|abstract
name|PartitionIterator
name|getPartitionIterator
parameter_list|()
function_decl|;
comment|/**    * Conversion to a org.apache.hadoop.hive.metastore.api.PartitionSpec sequence.    * @return A list of org.apache.hadoop.hive.metastore.api.PartitionSpec instances.    */
specifier|public
specifier|abstract
name|List
argument_list|<
name|PartitionSpec
argument_list|>
name|toPartitionSpec
parameter_list|()
function_decl|;
comment|/**    * Setter for the common root-location for all partitions in the PartitionSet.    * @param rootLocation The new common root-location.    * @throws MetaException    */
specifier|public
specifier|abstract
name|void
name|setRootLocation
parameter_list|(
name|String
name|rootLocation
parameter_list|)
throws|throws
name|MetaException
function_decl|;
comment|/**    * Factory to construct PartitionSetProxy instances, from PartitionSets.    */
specifier|public
specifier|static
class|class
name|Factory
block|{
comment|/**      * Factory method. Construct PartitionSpecProxy from raw PartitionSpec.      * @param partSpec Raw PartitionSpec from the Thrift API.      * @return PartitionSpecProxy instance.      * @throws MetaException      */
specifier|public
specifier|static
name|PartitionSpecProxy
name|get
parameter_list|(
name|PartitionSpec
name|partSpec
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|partSpec
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|partSpec
operator|.
name|isSetPartitionList
argument_list|()
condition|)
block|{
return|return
operator|new
name|PartitionListComposingSpecProxy
argument_list|(
name|partSpec
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|partSpec
operator|.
name|isSetSharedSDPartitionSpec
argument_list|()
condition|)
block|{
return|return
operator|new
name|PartitionSpecWithSharedSDProxy
argument_list|(
name|partSpec
argument_list|)
return|;
block|}
assert|assert
literal|false
operator|:
literal|"Unsupported type of PartitionSpec!"
assert|;
return|return
literal|null
return|;
block|}
comment|/**      * Factory method to construct CompositePartitionSpecProxy.      * @param partitionSpecs List of raw PartitionSpecs.      * @return A CompositePartitionSpecProxy instance.      * @throws MetaException      */
specifier|public
specifier|static
name|PartitionSpecProxy
name|get
parameter_list|(
name|List
argument_list|<
name|PartitionSpec
argument_list|>
name|partitionSpecs
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
operator|new
name|CompositePartitionSpecProxy
argument_list|(
name|partitionSpecs
argument_list|)
return|;
block|}
block|}
comment|// class Factory;
comment|/**    * Iterator to iterate over Partitions corresponding to a PartitionSpec.    */
specifier|public
interface|interface
name|PartitionIterator
extends|extends
name|java
operator|.
name|util
operator|.
name|Iterator
argument_list|<
name|Partition
argument_list|>
block|{
comment|/**      * Getter for the Partition "pointed to" by the iterator.      * Like next(), but without advancing the iterator.      * @return The "current" partition object.      */
name|Partition
name|getCurrent
parameter_list|()
function_decl|;
comment|/**      * Get the catalog name.      * @return catalog name.      */
name|String
name|getCatName
parameter_list|()
function_decl|;
comment|/**      * Getter for the name of the DB.      * @return Name of the DB.      */
name|String
name|getDbName
parameter_list|()
function_decl|;
comment|/**      * Getter for the name of the table.      * @return Name of the table.      */
name|String
name|getTableName
parameter_list|()
function_decl|;
comment|/**      * Getter for the Partition parameters.      * @return Key-value map for Partition-level parameters.      */
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getParameters
parameter_list|()
function_decl|;
comment|/**      * Setter for Partition parameters.      * @param parameters Key-value map fo Partition-level parameters.      */
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
function_decl|;
comment|/**      * Insert an individual parameter to a Partition's parameter-set.      * @param key      * @param value      */
name|void
name|putToParameters
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
function_decl|;
comment|/**      * Getter for Partition-location.      * @return Partition's location.      */
name|String
name|getLocation
parameter_list|()
function_decl|;
comment|/**      * Setter for creation-time of a Partition.      * @param time Timestamp indicating the time of creation of the Partition.      */
name|void
name|setCreateTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
block|}
comment|// class PartitionIterator;
comment|/**    * Simple wrapper class for pre-constructed Partitions, to expose a PartitionIterator interface,    * where the iterator-sequence consists of just one Partition.    */
specifier|public
specifier|static
class|class
name|SimplePartitionWrapperIterator
implements|implements
name|PartitionIterator
block|{
specifier|private
name|Partition
name|partition
decl_stmt|;
specifier|public
name|SimplePartitionWrapperIterator
parameter_list|(
name|Partition
name|partition
parameter_list|)
block|{
name|this
operator|.
name|partition
operator|=
name|partition
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
name|partition
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
name|partition
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
name|partition
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
name|partition
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
name|partition
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
name|partition
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
name|partition
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
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|partition
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
name|setCreateTime
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|partition
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
literal|false
return|;
block|}
comment|// No next partition.
annotation|@
name|Override
specifier|public
name|Partition
name|next
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|// No next partition.
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{}
comment|// Do nothing.
block|}
comment|// P
block|}
end_class

begin_comment
comment|// class PartitionSpecProxy;
end_comment

end_unit

