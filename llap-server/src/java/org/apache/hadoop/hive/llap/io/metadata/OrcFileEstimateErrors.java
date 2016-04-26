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
name|llap
operator|.
name|io
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|fs
operator|.
name|Path
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
name|common
operator|.
name|io
operator|.
name|DiskRangeList
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
name|common
operator|.
name|io
operator|.
name|DataCache
operator|.
name|BooleanRef
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
name|common
operator|.
name|io
operator|.
name|DataCache
operator|.
name|DiskRangeListFactory
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
name|common
operator|.
name|io
operator|.
name|DiskRangeList
operator|.
name|MutateHelper
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
name|llap
operator|.
name|IncrementalObjectSizeEstimator
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
name|llap
operator|.
name|IncrementalObjectSizeEstimator
operator|.
name|ObjectEstimator
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
name|llap
operator|.
name|cache
operator|.
name|EvictionDispatcher
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
name|llap
operator|.
name|cache
operator|.
name|LlapCacheableBuffer
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
name|io
operator|.
name|SyntheticFileId
import|;
end_import

begin_class
specifier|public
class|class
name|OrcFileEstimateErrors
extends|extends
name|LlapCacheableBuffer
block|{
specifier|private
specifier|final
name|Object
name|fileKey
decl_stmt|;
specifier|private
name|int
name|estimatedMemUsage
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentHashMap
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
name|cache
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|ObjectEstimator
argument_list|>
name|SIZE_ESTIMATORS
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|ObjectEstimator
name|SIZE_ESTIMATOR
decl_stmt|;
static|static
block|{
name|SIZE_ESTIMATORS
operator|=
name|IncrementalObjectSizeEstimator
operator|.
name|createEstimators
argument_list|(
name|createDummy
argument_list|(
operator|new
name|SyntheticFileId
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|SIZE_ESTIMATOR
operator|=
name|SIZE_ESTIMATORS
operator|.
name|get
argument_list|(
name|OrcFileEstimateErrors
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|public
name|OrcFileEstimateErrors
parameter_list|(
name|Object
name|fileKey
parameter_list|)
block|{
name|this
operator|.
name|fileKey
operator|=
name|fileKey
expr_stmt|;
block|}
specifier|public
name|void
name|addError
parameter_list|(
name|long
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|long
name|baseOffset
parameter_list|)
block|{
name|Long
name|key
init|=
name|Long
operator|.
name|valueOf
argument_list|(
name|offset
operator|+
name|baseOffset
argument_list|)
decl_stmt|;
name|Integer
name|existingLength
init|=
name|cache
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|existingLength
operator|!=
literal|null
operator|&&
name|existingLength
operator|>=
name|length
condition|)
return|return;
name|Integer
name|value
init|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|length
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|existingLength
operator|=
name|cache
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|existingLength
operator|==
literal|null
operator|||
name|existingLength
operator|>=
name|length
condition|)
return|return;
name|cache
operator|.
name|remove
argument_list|(
name|key
argument_list|,
name|existingLength
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|DiskRangeList
name|getIncompleteCbs
parameter_list|(
name|DiskRangeList
name|ranges
parameter_list|,
name|long
name|baseOffset
parameter_list|,
name|DiskRangeListFactory
name|factory
parameter_list|,
name|BooleanRef
name|gotAllData
parameter_list|)
block|{
name|DiskRangeList
name|prev
init|=
name|ranges
operator|.
name|prev
decl_stmt|;
if|if
condition|(
name|prev
operator|==
literal|null
condition|)
block|{
name|prev
operator|=
operator|new
name|MutateHelper
argument_list|(
name|ranges
argument_list|)
expr_stmt|;
block|}
name|DiskRangeList
name|current
init|=
name|ranges
decl_stmt|;
while|while
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
comment|// We assume ranges in "ranges" are non-overlapping; thus, we will save next in advance.
name|DiskRangeList
name|check
init|=
name|current
decl_stmt|;
name|current
operator|=
name|current
operator|.
name|next
expr_stmt|;
if|if
condition|(
name|check
operator|.
name|hasData
argument_list|()
condition|)
continue|continue;
name|Integer
name|badLength
init|=
name|cache
operator|.
name|get
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|check
operator|.
name|getOffset
argument_list|()
operator|+
name|baseOffset
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|badLength
operator|==
literal|null
operator|||
name|badLength
operator|<
name|check
operator|.
name|getLength
argument_list|()
condition|)
block|{
name|gotAllData
operator|.
name|value
operator|=
literal|false
expr_stmt|;
continue|continue;
block|}
name|check
operator|.
name|removeSelf
argument_list|()
expr_stmt|;
block|}
return|return
name|prev
operator|.
name|next
return|;
block|}
specifier|public
name|Object
name|getFileKey
parameter_list|()
block|{
return|return
name|fileKey
return|;
block|}
specifier|public
name|long
name|estimateMemoryUsage
parameter_list|()
block|{
comment|// Since we won't be able to update this as we add, for now, estimate 10x usage.
comment|// This shouldn't be much and this cache should be remove later anyway.
name|estimatedMemUsage
operator|=
literal|10
operator|*
name|SIZE_ESTIMATOR
operator|.
name|estimate
argument_list|(
name|this
argument_list|,
name|SIZE_ESTIMATORS
argument_list|)
expr_stmt|;
return|return
name|estimatedMemUsage
return|;
block|}
specifier|private
specifier|static
name|OrcFileEstimateErrors
name|createDummy
parameter_list|(
name|Object
name|fileKey
parameter_list|)
block|{
name|OrcFileEstimateErrors
name|dummy
init|=
operator|new
name|OrcFileEstimateErrors
argument_list|(
name|fileKey
argument_list|)
decl_stmt|;
name|dummy
operator|.
name|addError
argument_list|(
literal|0L
argument_list|,
literal|0
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
return|return
name|dummy
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|invalidate
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMemoryUsage
parameter_list|()
block|{
return|return
name|estimatedMemUsage
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|notifyEvicted
parameter_list|(
name|EvictionDispatcher
name|evictionDispatcher
parameter_list|)
block|{
name|evictionDispatcher
operator|.
name|notifyEvicted
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|isLocked
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

