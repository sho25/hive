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
name|metastore
operator|.
name|Warehouse
operator|.
name|DEFAULT_CATALOG_NAME
import|;
end_import

begin_comment
comment|/**  * Implementation of PartitionSpecProxy that composes a list of PartitionSpecProxy.  */
end_comment

begin_class
specifier|public
class|class
name|CompositePartitionSpecProxy
extends|extends
name|PartitionSpecProxy
block|{
specifier|private
name|String
name|catName
decl_stmt|;
specifier|private
name|String
name|dbName
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|List
argument_list|<
name|PartitionSpec
argument_list|>
name|partitionSpecs
decl_stmt|;
specifier|private
name|List
argument_list|<
name|PartitionSpecProxy
argument_list|>
name|partitionSpecProxies
decl_stmt|;
specifier|private
name|int
name|size
init|=
literal|0
decl_stmt|;
specifier|protected
name|CompositePartitionSpecProxy
parameter_list|(
name|List
argument_list|<
name|PartitionSpec
argument_list|>
name|partitionSpecs
parameter_list|)
block|{
name|this
operator|.
name|partitionSpecs
operator|=
name|partitionSpecs
expr_stmt|;
if|if
condition|(
name|partitionSpecs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|catName
operator|=
literal|null
expr_stmt|;
name|dbName
operator|=
literal|null
expr_stmt|;
name|tableName
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|catName
operator|=
name|partitionSpecs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getCatName
argument_list|()
expr_stmt|;
name|dbName
operator|=
name|partitionSpecs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getDbName
argument_list|()
expr_stmt|;
name|tableName
operator|=
name|partitionSpecs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTableName
argument_list|()
expr_stmt|;
name|this
operator|.
name|partitionSpecProxies
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|partitionSpecs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|PartitionSpec
name|partitionSpec
range|:
name|partitionSpecs
control|)
block|{
name|PartitionSpecProxy
name|partitionSpecProxy
init|=
name|Factory
operator|.
name|get
argument_list|(
name|partitionSpec
argument_list|)
decl_stmt|;
name|this
operator|.
name|partitionSpecProxies
operator|.
name|add
argument_list|(
name|partitionSpecProxy
argument_list|)
expr_stmt|;
name|size
operator|+=
name|partitionSpecProxy
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Assert class-invariant.
assert|assert
name|isValid
argument_list|()
operator|:
literal|"Invalid CompositePartitionSpecProxy!"
assert|;
block|}
annotation|@
name|Deprecated
specifier|protected
name|CompositePartitionSpecProxy
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|PartitionSpec
argument_list|>
name|partitionSpecs
parameter_list|)
block|{
name|this
argument_list|(
name|DEFAULT_CATALOG_NAME
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partitionSpecs
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|CompositePartitionSpecProxy
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|PartitionSpec
argument_list|>
name|partitionSpecs
parameter_list|)
block|{
name|this
operator|.
name|catName
operator|=
name|catName
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|partitionSpecs
operator|=
name|partitionSpecs
expr_stmt|;
name|this
operator|.
name|partitionSpecProxies
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|partitionSpecs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|PartitionSpec
name|partitionSpec
range|:
name|partitionSpecs
control|)
block|{
name|this
operator|.
name|partitionSpecProxies
operator|.
name|add
argument_list|(
name|PartitionSpecProxy
operator|.
name|Factory
operator|.
name|get
argument_list|(
name|partitionSpec
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Assert class-invariant.
assert|assert
name|isValid
argument_list|()
operator|:
literal|"Invalid CompositePartitionSpecProxy!"
assert|;
block|}
specifier|private
name|boolean
name|isValid
parameter_list|()
block|{
for|for
control|(
name|PartitionSpecProxy
name|partitionSpecProxy
range|:
name|partitionSpecProxies
control|)
block|{
if|if
condition|(
name|partitionSpecProxy
operator|instanceof
name|CompositePartitionSpecProxy
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
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|size
return|;
block|}
comment|/**    * Iterator to iterate over all Partitions, across all PartitionSpecProxy instances within the Composite.    */
specifier|public
specifier|static
class|class
name|Iterator
implements|implements
name|PartitionIterator
block|{
specifier|private
name|CompositePartitionSpecProxy
name|composite
decl_stmt|;
specifier|private
name|List
argument_list|<
name|PartitionSpecProxy
argument_list|>
name|partitionSpecProxies
decl_stmt|;
specifier|private
name|int
name|index
init|=
operator|-
literal|1
decl_stmt|;
comment|// Index into partitionSpecs.
specifier|private
name|PartitionIterator
name|iterator
init|=
literal|null
decl_stmt|;
specifier|public
name|Iterator
parameter_list|(
name|CompositePartitionSpecProxy
name|composite
parameter_list|)
block|{
name|this
operator|.
name|composite
operator|=
name|composite
expr_stmt|;
name|this
operator|.
name|partitionSpecProxies
operator|=
name|composite
operator|.
name|partitionSpecProxies
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|partitionSpecProxies
operator|!=
literal|null
operator|&&
operator|!
name|this
operator|.
name|partitionSpecProxies
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|index
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|iterator
operator|=
name|this
operator|.
name|partitionSpecProxies
operator|.
name|get
argument_list|(
name|this
operator|.
name|index
argument_list|)
operator|.
name|getPartitionIterator
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
if|if
condition|(
name|iterator
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
while|while
condition|(
operator|++
name|index
operator|<
name|partitionSpecProxies
operator|.
name|size
argument_list|()
operator|&&
operator|!
operator|(
name|iterator
operator|=
name|partitionSpecProxies
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|getPartitionIterator
argument_list|()
operator|)
operator|.
name|hasNext
argument_list|()
condition|)
empty_stmt|;
return|return
name|index
operator|<
name|partitionSpecProxies
operator|.
name|size
argument_list|()
operator|&&
name|iterator
operator|.
name|hasNext
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
if|if
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
return|return
name|iterator
operator|.
name|next
argument_list|()
return|;
while|while
condition|(
operator|++
name|index
operator|<
name|partitionSpecProxies
operator|.
name|size
argument_list|()
operator|&&
operator|!
operator|(
name|iterator
operator|=
name|partitionSpecProxies
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|getPartitionIterator
argument_list|()
operator|)
operator|.
name|hasNext
argument_list|()
condition|)
empty_stmt|;
return|return
name|index
operator|==
name|partitionSpecProxies
operator|.
name|size
argument_list|()
condition|?
literal|null
else|:
name|iterator
operator|.
name|next
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
name|iterator
operator|.
name|remove
argument_list|()
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
name|iterator
operator|.
name|getCurrent
argument_list|()
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
name|composite
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
name|composite
operator|.
name|dbName
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
name|composite
operator|.
name|tableName
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
name|iterator
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
name|iterator
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
name|iterator
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
name|iterator
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
name|iterator
operator|.
name|setCreateTime
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
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
name|this
operator|.
name|catName
operator|=
name|catName
expr_stmt|;
for|for
control|(
name|PartitionSpecProxy
name|partSpecProxy
range|:
name|partitionSpecProxies
control|)
block|{
name|partSpecProxy
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
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
for|for
control|(
name|PartitionSpecProxy
name|partSpecProxy
range|:
name|partitionSpecProxies
control|)
block|{
name|partSpecProxy
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
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
for|for
control|(
name|PartitionSpecProxy
name|partSpecProxy
range|:
name|partitionSpecProxies
control|)
block|{
name|partSpecProxy
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
name|String
name|getCatName
parameter_list|()
block|{
return|return
name|catName
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
name|dbName
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
name|tableName
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
name|partitionSpecs
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setRootLocation
parameter_list|(
name|String
name|rootLocation
parameter_list|)
throws|throws
name|MetaException
block|{
for|for
control|(
name|PartitionSpecProxy
name|partSpecProxy
range|:
name|partitionSpecProxies
control|)
block|{
name|partSpecProxy
operator|.
name|setRootLocation
argument_list|(
name|rootLocation
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

