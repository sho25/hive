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
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
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
name|int
name|writeBuffersSize
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
block|}
end_class

end_unit

