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
name|plan
package|;
end_package

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Edge Property"
argument_list|)
specifier|public
class|class
name|SparkEdgeProperty
block|{
specifier|public
specifier|static
name|long
name|SHUFFLE_NONE
init|=
literal|0
decl_stmt|;
comment|// No shuffle is needed. For union only.
specifier|public
specifier|static
name|long
name|SHUFFLE_GROUP
init|=
literal|1
decl_stmt|;
comment|// Shuffle, keys are coming together
specifier|public
specifier|static
name|long
name|SHUFFLE_SORT
init|=
literal|2
decl_stmt|;
comment|// Shuffle, keys are sorted
specifier|private
name|long
name|edgeType
decl_stmt|;
specifier|private
name|int
name|numPartitions
decl_stmt|;
specifier|public
name|SparkEdgeProperty
parameter_list|(
name|long
name|edgeType
parameter_list|,
name|int
name|numPartitions
parameter_list|)
block|{
name|this
operator|.
name|edgeType
operator|=
name|edgeType
expr_stmt|;
name|this
operator|.
name|numPartitions
operator|=
name|numPartitions
expr_stmt|;
block|}
specifier|public
name|SparkEdgeProperty
parameter_list|(
name|long
name|edgeType
parameter_list|)
block|{
name|this
operator|.
name|edgeType
operator|=
name|edgeType
expr_stmt|;
block|}
specifier|public
name|boolean
name|isShuffleNone
parameter_list|()
block|{
return|return
name|edgeType
operator|==
name|SHUFFLE_NONE
return|;
block|}
specifier|public
name|void
name|setShuffleNone
parameter_list|()
block|{
name|edgeType
operator|=
name|SHUFFLE_NONE
expr_stmt|;
block|}
specifier|public
name|boolean
name|isShuffleGroup
parameter_list|()
block|{
return|return
operator|(
name|edgeType
operator|&
name|SHUFFLE_GROUP
operator|)
operator|!=
literal|0
return|;
block|}
specifier|public
name|void
name|setShuffleGroup
parameter_list|()
block|{
name|edgeType
operator||=
name|SHUFFLE_GROUP
expr_stmt|;
block|}
specifier|public
name|boolean
name|isShuffleSort
parameter_list|()
block|{
return|return
operator|(
name|edgeType
operator|&
name|SHUFFLE_SORT
operator|)
operator|!=
literal|0
return|;
block|}
specifier|public
name|void
name|setShuffleSort
parameter_list|()
block|{
name|edgeType
operator||=
name|SHUFFLE_SORT
expr_stmt|;
block|}
specifier|public
name|long
name|getEdgeType
parameter_list|()
block|{
return|return
name|edgeType
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Shuffle Type"
argument_list|)
specifier|public
name|String
name|getShuffleType
parameter_list|()
block|{
if|if
condition|(
name|isShuffleNone
argument_list|()
condition|)
block|{
return|return
literal|"NONE"
return|;
block|}
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|isShuffleGroup
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"GROUP"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isShuffleSort
argument_list|()
condition|)
block|{
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"SORT"
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|int
name|getNumPartitions
parameter_list|()
block|{
return|return
name|numPartitions
return|;
block|}
specifier|public
name|void
name|setNumPartitions
parameter_list|(
name|int
name|numPartitions
parameter_list|)
block|{
name|this
operator|.
name|numPartitions
operator|=
name|numPartitions
expr_stmt|;
block|}
block|}
end_class

end_unit

