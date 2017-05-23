begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
operator|.
name|mutate
operator|.
name|worker
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * Tracks the (partition, bucket) combinations that have been encountered, checking that a group is not revisited.  * Potentially memory intensive.  */
end_comment

begin_class
class|class
name|GroupingValidator
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|visited
decl_stmt|;
specifier|private
specifier|final
name|StringBuilder
name|partitionKeyBuilder
decl_stmt|;
specifier|private
name|long
name|groups
decl_stmt|;
specifier|private
name|String
name|lastPartitionKey
decl_stmt|;
specifier|private
name|int
name|lastBucketId
init|=
operator|-
literal|1
decl_stmt|;
name|GroupingValidator
parameter_list|()
block|{
name|visited
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|partitionKeyBuilder
operator|=
operator|new
name|StringBuilder
argument_list|(
literal|64
argument_list|)
expr_stmt|;
block|}
comment|/**    * Checks that this group is either the same as the last or is a new group.    */
name|boolean
name|isInSequence
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partitionValues
parameter_list|,
name|int
name|bucketId
parameter_list|)
block|{
name|String
name|partitionKey
init|=
name|getPartitionKey
argument_list|(
name|partitionValues
argument_list|)
decl_stmt|;
if|if
condition|(
name|Objects
operator|.
name|equals
argument_list|(
name|lastPartitionKey
argument_list|,
name|partitionKey
argument_list|)
operator|&&
name|lastBucketId
operator|==
name|bucketId
condition|)
block|{
return|return
literal|true
return|;
block|}
name|lastPartitionKey
operator|=
name|partitionKey
expr_stmt|;
name|lastBucketId
operator|=
name|bucketId
expr_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|bucketIdSet
init|=
name|visited
operator|.
name|get
argument_list|(
name|partitionKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketIdSet
operator|==
literal|null
condition|)
block|{
comment|// If the bucket id set component of this data structure proves to be too large there is the
comment|// option of moving it to Trove or HPPC in an effort to reduce size.
name|bucketIdSet
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|visited
operator|.
name|put
argument_list|(
name|partitionKey
argument_list|,
name|bucketIdSet
argument_list|)
expr_stmt|;
block|}
name|boolean
name|newGroup
init|=
name|bucketIdSet
operator|.
name|add
argument_list|(
name|bucketId
argument_list|)
decl_stmt|;
if|if
condition|(
name|newGroup
condition|)
block|{
name|groups
operator|++
expr_stmt|;
block|}
return|return
name|newGroup
return|;
block|}
specifier|private
name|String
name|getPartitionKey
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partitionValues
parameter_list|)
block|{
name|partitionKeyBuilder
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
for|for
control|(
name|String
name|element
range|:
name|partitionValues
control|)
block|{
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|partitionKeyBuilder
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
block|}
name|partitionKeyBuilder
operator|.
name|append
argument_list|(
name|element
argument_list|)
expr_stmt|;
block|}
name|String
name|partitionKey
init|=
name|partitionKeyBuilder
operator|.
name|toString
argument_list|()
decl_stmt|;
return|return
name|partitionKey
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"GroupingValidator [groups="
operator|+
name|groups
operator|+
literal|",lastPartitionKey="
operator|+
name|lastPartitionKey
operator|+
literal|",lastBucketId="
operator|+
name|lastBucketId
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

