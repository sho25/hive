begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  Licensed to the Apache Software Foundation (ASF) under one  *  or more contributor license agreements.  See the NOTICE file  *  distributed with this work for additional information  *  regarding copyright ownership.  The ASF licenses this file  *  to you under the Apache License, Version 2.0 (the  *  "License"); you may not use this file except in compliance  *  with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|spark
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
name|ql
operator|.
name|io
operator|.
name|HiveKey
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
name|io
operator|.
name|BytesWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|HashPartitioner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|Partitioner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaPairRDD
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|function
operator|.
name|PairFlatMapFunction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|storage
operator|.
name|StorageLevel
import|;
end_import

begin_import
import|import
name|scala
operator|.
name|Tuple2
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|SortByShuffler
implements|implements
name|SparkShuffler
block|{
specifier|private
specifier|final
name|boolean
name|totalOrder
decl_stmt|;
specifier|private
specifier|final
name|SparkPlan
name|sparkPlan
decl_stmt|;
comment|/**    * @param totalOrder whether this shuffler provides total order shuffle.    */
specifier|public
name|SortByShuffler
parameter_list|(
name|boolean
name|totalOrder
parameter_list|,
name|SparkPlan
name|sparkPlan
parameter_list|)
block|{
name|this
operator|.
name|totalOrder
operator|=
name|totalOrder
expr_stmt|;
name|this
operator|.
name|sparkPlan
operator|=
name|sparkPlan
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|Iterable
argument_list|<
name|BytesWritable
argument_list|>
argument_list|>
name|shuffle
parameter_list|(
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|input
parameter_list|,
name|int
name|numPartitions
parameter_list|)
block|{
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|rdd
decl_stmt|;
if|if
condition|(
name|totalOrder
condition|)
block|{
if|if
condition|(
name|numPartitions
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|numPartitions
operator|>
literal|1
operator|&&
name|input
operator|.
name|getStorageLevel
argument_list|()
operator|==
name|StorageLevel
operator|.
name|NONE
argument_list|()
condition|)
block|{
name|input
operator|.
name|persist
argument_list|(
name|StorageLevel
operator|.
name|DISK_ONLY
argument_list|()
argument_list|)
expr_stmt|;
name|sparkPlan
operator|.
name|addCachedRDDId
argument_list|(
name|input
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rdd
operator|=
name|input
operator|.
name|sortByKey
argument_list|(
literal|true
argument_list|,
name|numPartitions
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rdd
operator|=
name|input
operator|.
name|sortByKey
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|Partitioner
name|partitioner
init|=
operator|new
name|HashPartitioner
argument_list|(
name|numPartitions
argument_list|)
decl_stmt|;
name|rdd
operator|=
name|input
operator|.
name|repartitionAndSortWithinPartitions
argument_list|(
name|partitioner
argument_list|)
expr_stmt|;
block|}
return|return
name|rdd
operator|.
name|mapPartitionsToPair
argument_list|(
operator|new
name|ShuffleFunction
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"SortBy"
return|;
block|}
specifier|private
specifier|static
class|class
name|ShuffleFunction
implements|implements
name|PairFlatMapFunction
argument_list|<
name|Iterator
argument_list|<
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
argument_list|,
name|HiveKey
argument_list|,
name|Iterable
argument_list|<
name|BytesWritable
argument_list|>
argument_list|>
block|{
comment|// make eclipse happy
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|Iterable
argument_list|<
name|BytesWritable
argument_list|>
argument_list|>
argument_list|>
name|call
parameter_list|(
specifier|final
name|Iterator
argument_list|<
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
name|it
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Use input iterator to back returned iterable object.
return|return
operator|new
name|Iterator
argument_list|<
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|Iterable
argument_list|<
name|BytesWritable
argument_list|>
argument_list|>
argument_list|>
argument_list|()
block|{
name|HiveKey
name|curKey
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|BytesWritable
argument_list|>
name|curValues
init|=
operator|new
name|ArrayList
argument_list|<
name|BytesWritable
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|it
operator|.
name|hasNext
argument_list|()
operator|||
name|curKey
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|Iterable
argument_list|<
name|BytesWritable
argument_list|>
argument_list|>
name|next
parameter_list|()
block|{
comment|// TODO: implement this by accumulating rows with the same key into a list.
comment|// Note that this list needs to improved to prevent excessive memory usage, but this
comment|// can be done in later phase.
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|pair
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|curKey
operator|!=
literal|null
operator|&&
operator|!
name|curKey
operator|.
name|equals
argument_list|(
name|pair
operator|.
name|_1
argument_list|()
argument_list|)
condition|)
block|{
name|HiveKey
name|key
init|=
name|curKey
decl_stmt|;
name|List
argument_list|<
name|BytesWritable
argument_list|>
name|values
init|=
name|curValues
decl_stmt|;
name|curKey
operator|=
name|pair
operator|.
name|_1
argument_list|()
expr_stmt|;
name|curValues
operator|=
operator|new
name|ArrayList
argument_list|<
name|BytesWritable
argument_list|>
argument_list|()
expr_stmt|;
name|curValues
operator|.
name|add
argument_list|(
name|pair
operator|.
name|_2
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|Iterable
argument_list|<
name|BytesWritable
argument_list|>
argument_list|>
argument_list|(
name|key
argument_list|,
name|values
argument_list|)
return|;
block|}
name|curKey
operator|=
name|pair
operator|.
name|_1
argument_list|()
expr_stmt|;
name|curValues
operator|.
name|add
argument_list|(
name|pair
operator|.
name|_2
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|curKey
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|()
throw|;
block|}
comment|// if we get here, this should be the last element we have
name|HiveKey
name|key
init|=
name|curKey
decl_stmt|;
name|curKey
operator|=
literal|null
expr_stmt|;
return|return
operator|new
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|Iterable
argument_list|<
name|BytesWritable
argument_list|>
argument_list|>
argument_list|(
name|key
argument_list|,
name|curValues
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
comment|// Not implemented.
comment|// throw Unsupported Method Invocation Exception.
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
return|;
block|}
block|}
block|}
end_class

end_unit

