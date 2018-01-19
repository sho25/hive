begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  Licensed to the Apache Software Foundation (ASF) under one  *  or more contributor license agreements.  See the NOTICE file  *  distributed with this work for additional information  *  regarding copyright ownership.  The ASF licenses this file  *  to you under the Apache License, Version 2.0 (the  *  "License"); you may not use this file except in compliance  *  with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|storage
operator|.
name|StorageLevel
import|;
end_import

begin_class
specifier|public
class|class
name|ShuffleTran
implements|implements
name|SparkTran
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|,
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
block|{
specifier|private
specifier|final
name|SparkShuffler
name|shuffler
decl_stmt|;
specifier|private
specifier|final
name|int
name|numOfPartitions
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|toCache
decl_stmt|;
specifier|private
specifier|final
name|SparkPlan
name|sparkPlan
decl_stmt|;
specifier|private
name|String
name|name
init|=
literal|"Shuffle"
decl_stmt|;
specifier|public
name|ShuffleTran
parameter_list|(
name|SparkPlan
name|sparkPlan
parameter_list|,
name|SparkShuffler
name|sf
parameter_list|,
name|int
name|n
parameter_list|)
block|{
name|this
argument_list|(
name|sparkPlan
argument_list|,
name|sf
argument_list|,
name|n
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ShuffleTran
parameter_list|(
name|SparkPlan
name|sparkPlan
parameter_list|,
name|SparkShuffler
name|sf
parameter_list|,
name|int
name|n
parameter_list|,
name|boolean
name|toCache
parameter_list|)
block|{
name|shuffler
operator|=
name|sf
expr_stmt|;
name|numOfPartitions
operator|=
name|n
expr_stmt|;
name|this
operator|.
name|toCache
operator|=
name|toCache
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
name|BytesWritable
argument_list|>
name|transform
parameter_list|(
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|input
parameter_list|)
block|{
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|result
init|=
name|shuffler
operator|.
name|shuffle
argument_list|(
name|input
argument_list|,
name|numOfPartitions
argument_list|)
decl_stmt|;
if|if
condition|(
name|toCache
condition|)
block|{
name|sparkPlan
operator|.
name|addCachedRDDId
argument_list|(
name|result
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|=
name|result
operator|.
name|persist
argument_list|(
name|StorageLevel
operator|.
name|MEMORY_AND_DISK
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|int
name|getNoOfPartitions
parameter_list|()
block|{
return|return
name|numOfPartitions
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
name|name
return|;
block|}
annotation|@
name|Override
specifier|public
name|Boolean
name|isCacheEnable
parameter_list|()
block|{
return|return
operator|new
name|Boolean
argument_list|(
name|toCache
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|SparkShuffler
name|getShuffler
parameter_list|()
block|{
return|return
name|shuffler
return|;
block|}
block|}
end_class

end_unit

