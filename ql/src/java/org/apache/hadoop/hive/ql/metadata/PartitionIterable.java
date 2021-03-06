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
name|metadata
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
name|Collection
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

begin_comment
comment|/**  * PartitionIterable - effectively a lazy Iterable&lt;Partition&gt;  *  * Sometimes, we have a need for iterating through a list of partitions,  * but the list of partitions can be too big to fetch as a single object.  * Thus, the goal of PartitionIterable is to act as an Iterable&lt;Partition&gt;  * while lazily fetching each relevant partition, one after the other as  * independent metadata calls.  *  * It is very likely that any calls to PartitionIterable are going to result  * in a large number of calls, so use sparingly only when the memory cost  * of fetching all the partitions in one shot is too prohibitive.  *  * This is still pretty costly in that it would retain a list of partition  * names, but that should be far less expensive than the entire partition  * objects.  *  * Note that remove() is an illegal call on this, and will result in an  * IllegalStateException.  */
end_comment

begin_class
specifier|public
class|class
name|PartitionIterable
implements|implements
name|Iterable
argument_list|<
name|Partition
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Partition
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|Iterator
argument_list|<
name|Partition
argument_list|>
argument_list|()
block|{
specifier|private
name|boolean
name|initialized
init|=
literal|false
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|Partition
argument_list|>
name|ptnsIterator
init|=
literal|null
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|String
argument_list|>
name|partitionNamesIter
init|=
literal|null
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|Partition
argument_list|>
name|batchIter
init|=
literal|null
decl_stmt|;
specifier|private
name|void
name|initialize
parameter_list|()
block|{
if|if
condition|(
operator|!
name|initialized
condition|)
block|{
if|if
condition|(
name|currType
operator|==
name|Type
operator|.
name|LIST_PROVIDED
condition|)
block|{
name|ptnsIterator
operator|=
name|ptnsProvided
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|partitionNamesIter
operator|=
name|partitionNames
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
name|initialized
operator|=
literal|true
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
name|initialize
argument_list|()
expr_stmt|;
if|if
condition|(
name|currType
operator|==
name|Type
operator|.
name|LIST_PROVIDED
condition|)
block|{
return|return
name|ptnsIterator
operator|.
name|hasNext
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|(
operator|(
name|batchIter
operator|!=
literal|null
operator|)
operator|&&
name|batchIter
operator|.
name|hasNext
argument_list|()
operator|)
operator|||
name|partitionNamesIter
operator|.
name|hasNext
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Partition
name|next
parameter_list|()
block|{
name|initialize
argument_list|()
expr_stmt|;
if|if
condition|(
name|currType
operator|==
name|Type
operator|.
name|LIST_PROVIDED
condition|)
block|{
return|return
name|ptnsIterator
operator|.
name|next
argument_list|()
return|;
block|}
if|if
condition|(
operator|(
name|batchIter
operator|==
literal|null
operator|)
operator|||
operator|!
name|batchIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|getNextBatch
argument_list|()
expr_stmt|;
block|}
return|return
name|batchIter
operator|.
name|next
argument_list|()
return|;
block|}
specifier|private
name|void
name|getNextBatch
parameter_list|()
block|{
name|int
name|batchCounter
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|nameBatch
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|batchCounter
operator|<
name|batchSize
operator|&&
name|partitionNamesIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|nameBatch
operator|.
name|add
argument_list|(
name|partitionNamesIter
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
name|batchCounter
operator|++
expr_stmt|;
block|}
try|try
block|{
name|batchIter
operator|=
name|db
operator|.
name|getPartitionsByNames
argument_list|(
name|table
argument_list|,
name|nameBatch
argument_list|,
name|getColStats
argument_list|)
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"PartitionIterable is a read-only iterable and remove() is unsupported"
argument_list|)
throw|;
block|}
block|}
return|;
block|}
enum|enum
name|Type
block|{
name|LIST_PROVIDED
block|,
comment|// Where a List<Partitions is already provided
name|LAZY_FETCH_PARTITIONS
comment|// Where we want to fetch Partitions lazily when they're needed.
block|}
empty_stmt|;
specifier|final
name|Type
name|currType
decl_stmt|;
comment|// used for LIST_PROVIDED cases
specifier|private
name|Collection
argument_list|<
name|Partition
argument_list|>
name|ptnsProvided
init|=
literal|null
decl_stmt|;
comment|// used for LAZY_FETCH_PARTITIONS cases
specifier|private
name|Hive
name|db
init|=
literal|null
decl_stmt|;
comment|// Assumes one instance of this + single-threaded compilation for each query.
specifier|private
name|Table
name|table
init|=
literal|null
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partialPartitionSpec
init|=
literal|null
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|partitionNames
init|=
literal|null
decl_stmt|;
specifier|private
name|int
name|batchSize
decl_stmt|;
specifier|private
name|boolean
name|getColStats
init|=
literal|false
decl_stmt|;
comment|/**    * Dummy constructor, which simply acts as an iterator on an already-present    * list of partitions, allows for easy drop-in replacement for other methods    * that already have a List&lt;Partition&gt;    */
specifier|public
name|PartitionIterable
parameter_list|(
name|Collection
argument_list|<
name|Partition
argument_list|>
name|ptnsProvided
parameter_list|)
block|{
name|this
operator|.
name|currType
operator|=
name|Type
operator|.
name|LIST_PROVIDED
expr_stmt|;
name|this
operator|.
name|ptnsProvided
operator|=
name|ptnsProvided
expr_stmt|;
block|}
comment|/**    * Primary constructor that fetches all partitions in a given table, given    * a Hive object and a table object, and a partial partition spec.    */
specifier|public
name|PartitionIterable
parameter_list|(
name|Hive
name|db
parameter_list|,
name|Table
name|table
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partialPartitionSpec
parameter_list|,
name|int
name|batchSize
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
argument_list|(
name|db
argument_list|,
name|table
argument_list|,
name|partialPartitionSpec
argument_list|,
name|batchSize
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Primary constructor that fetches all partitions in a given table, given    * a Hive object and a table object, and a partial partition spec.    */
specifier|public
name|PartitionIterable
parameter_list|(
name|Hive
name|db
parameter_list|,
name|Table
name|table
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partialPartitionSpec
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|boolean
name|getColStats
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
operator|.
name|currType
operator|=
name|Type
operator|.
name|LAZY_FETCH_PARTITIONS
expr_stmt|;
name|this
operator|.
name|db
operator|=
name|db
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|partialPartitionSpec
operator|=
name|partialPartitionSpec
expr_stmt|;
name|this
operator|.
name|batchSize
operator|=
name|batchSize
expr_stmt|;
name|this
operator|.
name|getColStats
operator|=
name|getColStats
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|partialPartitionSpec
operator|==
literal|null
condition|)
block|{
name|partitionNames
operator|=
name|db
operator|.
name|getPartitionNames
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
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|partitionNames
operator|=
name|db
operator|.
name|getPartitionNames
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
argument_list|,
name|partialPartitionSpec
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

