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
name|columnstats
operator|.
name|cache
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|ndv
operator|.
name|NumDistinctValueEstimator
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
name|ndv
operator|.
name|NumDistinctValueEstimatorFactory
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
name|StringColumnStatsData
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|public
class|class
name|StringColumnStatsDataInspector
extends|extends
name|StringColumnStatsData
block|{
specifier|private
name|NumDistinctValueEstimator
name|ndvEstimator
decl_stmt|;
specifier|public
name|StringColumnStatsDataInspector
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|StringColumnStatsDataInspector
parameter_list|(
name|long
name|maxColLen
parameter_list|,
name|double
name|avgColLen
parameter_list|,
name|long
name|numNulls
parameter_list|,
name|long
name|numDVs
parameter_list|)
block|{
name|super
argument_list|(
name|maxColLen
argument_list|,
name|avgColLen
argument_list|,
name|numNulls
argument_list|,
name|numDVs
argument_list|)
expr_stmt|;
block|}
specifier|public
name|StringColumnStatsDataInspector
parameter_list|(
name|StringColumnStatsDataInspector
name|other
parameter_list|)
block|{
name|super
argument_list|(
name|other
argument_list|)
expr_stmt|;
if|if
condition|(
name|other
operator|.
name|ndvEstimator
operator|!=
literal|null
condition|)
block|{
name|super
operator|.
name|setBitVectors
argument_list|(
name|ndvEstimator
operator|.
name|serialize
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|StringColumnStatsDataInspector
parameter_list|(
name|StringColumnStatsData
name|other
parameter_list|)
block|{
name|super
argument_list|(
name|other
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|StringColumnStatsDataInspector
name|deepCopy
parameter_list|()
block|{
return|return
operator|new
name|StringColumnStatsDataInspector
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getBitVectors
parameter_list|()
block|{
if|if
condition|(
name|ndvEstimator
operator|!=
literal|null
condition|)
block|{
name|updateBitVectors
argument_list|()
expr_stmt|;
block|}
return|return
name|super
operator|.
name|getBitVectors
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|bufferForBitVectors
parameter_list|()
block|{
if|if
condition|(
name|ndvEstimator
operator|!=
literal|null
condition|)
block|{
name|updateBitVectors
argument_list|()
expr_stmt|;
block|}
return|return
name|super
operator|.
name|bufferForBitVectors
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setBitVectors
parameter_list|(
name|byte
index|[]
name|bitVectors
parameter_list|)
block|{
name|super
operator|.
name|setBitVectors
argument_list|(
name|bitVectors
argument_list|)
expr_stmt|;
name|this
operator|.
name|ndvEstimator
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setBitVectors
parameter_list|(
name|ByteBuffer
name|bitVectors
parameter_list|)
block|{
name|super
operator|.
name|setBitVectors
argument_list|(
name|bitVectors
argument_list|)
expr_stmt|;
name|this
operator|.
name|ndvEstimator
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|unsetBitVectors
parameter_list|()
block|{
name|super
operator|.
name|unsetBitVectors
argument_list|()
expr_stmt|;
name|this
operator|.
name|ndvEstimator
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSetBitVectors
parameter_list|()
block|{
if|if
condition|(
name|ndvEstimator
operator|!=
literal|null
condition|)
block|{
name|updateBitVectors
argument_list|()
expr_stmt|;
block|}
return|return
name|super
operator|.
name|isSetBitVectors
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setBitVectorsIsSet
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
if|if
condition|(
name|ndvEstimator
operator|!=
literal|null
condition|)
block|{
name|updateBitVectors
argument_list|()
expr_stmt|;
block|}
name|super
operator|.
name|setBitVectorsIsSet
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|NumDistinctValueEstimator
name|getNdvEstimator
parameter_list|()
block|{
if|if
condition|(
name|isSetBitVectors
argument_list|()
operator|&&
name|getBitVectors
argument_list|()
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
name|updateNdvEstimator
argument_list|()
expr_stmt|;
block|}
return|return
name|ndvEstimator
return|;
block|}
specifier|public
name|void
name|setNdvEstimator
parameter_list|(
name|NumDistinctValueEstimator
name|ndvEstimator
parameter_list|)
block|{
name|super
operator|.
name|unsetBitVectors
argument_list|()
expr_stmt|;
name|this
operator|.
name|ndvEstimator
operator|=
name|ndvEstimator
expr_stmt|;
block|}
specifier|private
name|void
name|updateBitVectors
parameter_list|()
block|{
name|super
operator|.
name|setBitVectors
argument_list|(
name|ndvEstimator
operator|.
name|serialize
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|ndvEstimator
operator|=
literal|null
expr_stmt|;
block|}
specifier|private
name|void
name|updateNdvEstimator
parameter_list|()
block|{
name|this
operator|.
name|ndvEstimator
operator|=
name|NumDistinctValueEstimatorFactory
operator|.
name|getNumDistinctValueEstimator
argument_list|(
name|super
operator|.
name|getBitVectors
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|unsetBitVectors
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

