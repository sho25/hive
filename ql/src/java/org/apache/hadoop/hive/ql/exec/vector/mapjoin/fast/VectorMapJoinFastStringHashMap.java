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
name|vector
operator|.
name|mapjoin
operator|.
name|fast
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
operator|.
name|HiveException
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

begin_comment
comment|/*  * An single STRING key hash map optimized for vector map join.  *  * The key will be deserialized and just the bytes will be stored.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinFastStringHashMap
extends|extends
name|VectorMapJoinFastBytesHashMap
block|{
specifier|private
name|VectorMapJoinFastStringCommon
name|stringCommon
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|putRow
parameter_list|(
name|BytesWritable
name|currentKey
parameter_list|,
name|BytesWritable
name|currentValue
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
if|if
condition|(
operator|!
name|stringCommon
operator|.
name|adaptPutRow
argument_list|(
name|this
argument_list|,
name|currentKey
argument_list|,
name|currentValue
argument_list|)
condition|)
block|{
comment|// Ignore NULL keys, except for FULL OUTER.
if|if
condition|(
name|isFullOuter
condition|)
block|{
name|addFullOuterNullKeyValue
argument_list|(
name|currentValue
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|VectorMapJoinFastStringHashMap
parameter_list|(
name|boolean
name|isFullOuter
parameter_list|,
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
name|super
argument_list|(
name|isFullOuter
argument_list|,
name|initialCapacity
argument_list|,
name|loadFactor
argument_list|,
name|writeBuffersSize
argument_list|,
name|estimatedKeyCount
argument_list|)
expr_stmt|;
name|stringCommon
operator|=
operator|new
name|VectorMapJoinFastStringCommon
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getEstimatedMemorySize
parameter_list|()
block|{
name|long
name|size
init|=
literal|0
decl_stmt|;
comment|// adding 16KB constant memory for stringCommon as the rabit hole is deep to implement
comment|// MemoryEstimate interface, also it is constant overhead
name|size
operator|+=
operator|(
literal|16
operator|*
literal|1024L
operator|)
expr_stmt|;
return|return
name|super
operator|.
name|getEstimatedMemorySize
argument_list|()
operator|+
name|size
return|;
block|}
block|}
end_class

end_unit

