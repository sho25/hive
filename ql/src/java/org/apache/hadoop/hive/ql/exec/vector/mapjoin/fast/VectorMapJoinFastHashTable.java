begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|vector
operator|.
name|mapjoin
operator|.
name|fast
package|;
end_package

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|vector
operator|.
name|mapjoin
operator|.
name|hashtable
operator|.
name|VectorMapJoinHashTable
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|VectorMapJoinFastHashTable
implements|implements
name|VectorMapJoinHashTable
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|VectorMapJoinFastHashTable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|int
name|logicalHashBucketCount
decl_stmt|;
specifier|protected
name|int
name|logicalHashBucketMask
decl_stmt|;
specifier|protected
name|float
name|loadFactor
decl_stmt|;
specifier|protected
specifier|final
name|int
name|writeBuffersSize
decl_stmt|;
specifier|protected
name|long
name|estimatedKeyCount
decl_stmt|;
specifier|protected
name|int
name|metricPutConflict
decl_stmt|;
specifier|protected
name|int
name|largestNumberOfSteps
decl_stmt|;
specifier|protected
name|int
name|keysAssigned
decl_stmt|;
specifier|protected
name|int
name|resizeThreshold
decl_stmt|;
specifier|protected
name|int
name|metricExpands
decl_stmt|;
comment|// 2^30 (we cannot use Integer.MAX_VALUE which is 2^31-1).
specifier|public
specifier|static
specifier|final
name|int
name|HIGHEST_INT_POWER_OF_2
init|=
literal|1073741824
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|ONE_QUARTER_LIMIT
init|=
name|HIGHEST_INT_POWER_OF_2
operator|/
literal|4
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|ONE_SIXTH_LIMIT
init|=
name|HIGHEST_INT_POWER_OF_2
operator|/
literal|6
decl_stmt|;
specifier|public
name|void
name|throwExpandError
parameter_list|(
name|int
name|limit
parameter_list|,
name|String
name|dataTypeName
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Vector MapJoin "
operator|+
name|dataTypeName
operator|+
literal|" Hash Table cannot grow any more -- use a smaller container size. "
operator|+
literal|"Current logical size is "
operator|+
name|logicalHashBucketCount
operator|+
literal|" and "
operator|+
literal|"the limit is "
operator|+
name|limit
operator|+
literal|". "
operator|+
literal|"Estimated key count was "
operator|+
operator|(
name|estimatedKeyCount
operator|==
operator|-
literal|1
condition|?
literal|"not available"
else|:
name|estimatedKeyCount
operator|)
operator|+
literal|"."
argument_list|)
throw|;
block|}
specifier|private
specifier|static
name|void
name|validateCapacity
parameter_list|(
name|long
name|capacity
parameter_list|)
block|{
if|if
condition|(
name|Long
operator|.
name|bitCount
argument_list|(
name|capacity
argument_list|)
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Capacity must be a power of two"
argument_list|)
throw|;
block|}
if|if
condition|(
name|capacity
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Invalid capacity "
operator|+
name|capacity
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|int
name|nextHighestPowerOfTwo
parameter_list|(
name|int
name|v
parameter_list|)
block|{
return|return
name|Integer
operator|.
name|highestOneBit
argument_list|(
name|v
argument_list|)
operator|<<
literal|1
return|;
block|}
specifier|public
name|VectorMapJoinFastHashTable
parameter_list|(
name|int
name|initialCapacity
parameter_list|,
name|float
name|loadFactor
parameter_list|,
name|int
name|writeBuffersSize
parameter_list|,
name|long
name|estimatedKeyCount
parameter_list|)
block|{
name|initialCapacity
operator|=
operator|(
name|Long
operator|.
name|bitCount
argument_list|(
name|initialCapacity
argument_list|)
operator|==
literal|1
operator|)
condition|?
name|initialCapacity
else|:
name|nextHighestPowerOfTwo
argument_list|(
name|initialCapacity
argument_list|)
expr_stmt|;
name|validateCapacity
argument_list|(
name|initialCapacity
argument_list|)
expr_stmt|;
name|this
operator|.
name|estimatedKeyCount
operator|=
name|estimatedKeyCount
expr_stmt|;
name|logicalHashBucketCount
operator|=
name|initialCapacity
expr_stmt|;
name|logicalHashBucketMask
operator|=
name|logicalHashBucketCount
operator|-
literal|1
expr_stmt|;
name|resizeThreshold
operator|=
call|(
name|int
call|)
argument_list|(
name|logicalHashBucketCount
operator|*
name|loadFactor
argument_list|)
expr_stmt|;
name|this
operator|.
name|loadFactor
operator|=
name|loadFactor
expr_stmt|;
name|this
operator|.
name|writeBuffersSize
operator|=
name|writeBuffersSize
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|keysAssigned
return|;
block|}
block|}
end_class

end_unit

