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
name|optimizer
operator|.
name|calcite
operator|.
name|stats
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelDistribution
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|metadata
operator|.
name|ChainedRelMetadataProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|metadata
operator|.
name|ReflectiveRelMetadataProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|metadata
operator|.
name|RelMdDistribution
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|metadata
operator|.
name|RelMetadataProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|util
operator|.
name|BuiltInMethod
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
name|optimizer
operator|.
name|calcite
operator|.
name|HiveCalciteUtil
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
name|optimizer
operator|.
name|calcite
operator|.
name|HiveCalciteUtil
operator|.
name|JoinLeafPredicateInfo
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
name|optimizer
operator|.
name|calcite
operator|.
name|HiveCalciteUtil
operator|.
name|JoinPredicateInfo
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
name|optimizer
operator|.
name|calcite
operator|.
name|HiveRelDistribution
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveAggregate
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveJoin
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveJoin
operator|.
name|MapJoinStreamingRelation
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_class
specifier|public
class|class
name|HiveRelMdDistribution
block|{
specifier|public
specifier|static
specifier|final
name|RelMetadataProvider
name|SOURCE
init|=
name|ChainedRelMetadataProvider
operator|.
name|of
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
name|ReflectiveRelMetadataProvider
operator|.
name|reflectiveSource
argument_list|(
name|BuiltInMethod
operator|.
name|DISTRIBUTION
operator|.
name|method
argument_list|,
operator|new
name|HiveRelMdDistribution
argument_list|()
argument_list|)
argument_list|,
name|RelMdDistribution
operator|.
name|SOURCE
argument_list|)
argument_list|)
decl_stmt|;
comment|//~ Constructors -----------------------------------------------------------
specifier|private
name|HiveRelMdDistribution
parameter_list|()
block|{}
comment|//~ Methods ----------------------------------------------------------------
specifier|public
name|RelDistribution
name|distribution
parameter_list|(
name|HiveAggregate
name|aggregate
parameter_list|)
block|{
return|return
operator|new
name|HiveRelDistribution
argument_list|(
name|RelDistribution
operator|.
name|Type
operator|.
name|HASH_DISTRIBUTED
argument_list|,
name|aggregate
operator|.
name|getGroupSet
argument_list|()
operator|.
name|asList
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|RelDistribution
name|distribution
parameter_list|(
name|HiveJoin
name|join
parameter_list|)
block|{
comment|// Compute distribution
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|Integer
argument_list|>
name|keysListBuilder
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|Integer
argument_list|>
name|leftKeysListBuilder
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|Integer
argument_list|>
name|rightKeysListBuilder
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|JoinPredicateInfo
name|joinPredInfo
init|=
name|HiveCalciteUtil
operator|.
name|JoinPredicateInfo
operator|.
name|constructJoinPredicateInfo
argument_list|(
name|join
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|joinPredInfo
operator|.
name|getEquiJoinPredicateElements
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|JoinLeafPredicateInfo
name|joinLeafPredInfo
init|=
name|joinPredInfo
operator|.
name|getEquiJoinPredicateElements
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|leftPos
range|:
name|joinLeafPredInfo
operator|.
name|getProjsFromLeftPartOfJoinKeysInJoinSchema
argument_list|()
control|)
block|{
name|keysListBuilder
operator|.
name|add
argument_list|(
name|leftPos
argument_list|)
expr_stmt|;
name|leftKeysListBuilder
operator|.
name|add
argument_list|(
name|leftPos
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|rightPos
range|:
name|joinLeafPredInfo
operator|.
name|getProjsFromRightPartOfJoinKeysInJoinSchema
argument_list|()
control|)
block|{
name|keysListBuilder
operator|.
name|add
argument_list|(
name|rightPos
argument_list|)
expr_stmt|;
name|rightKeysListBuilder
operator|.
name|add
argument_list|(
name|rightPos
argument_list|)
expr_stmt|;
block|}
block|}
name|RelDistribution
name|distribution
decl_stmt|;
switch|switch
condition|(
name|join
operator|.
name|getJoinAlgorithm
argument_list|()
condition|)
block|{
case|case
name|SMB_JOIN
case|:
case|case
name|BUCKET_JOIN
case|:
case|case
name|COMMON_JOIN
case|:
name|distribution
operator|=
operator|new
name|HiveRelDistribution
argument_list|(
name|RelDistribution
operator|.
name|Type
operator|.
name|HASH_DISTRIBUTED
argument_list|,
name|keysListBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|MAP_JOIN
case|:
comment|// Keep buckets from the streaming relation
if|if
condition|(
name|join
operator|.
name|getMapJoinStreamingSide
argument_list|()
operator|==
name|MapJoinStreamingRelation
operator|.
name|LEFT_RELATION
condition|)
block|{
name|distribution
operator|=
operator|new
name|HiveRelDistribution
argument_list|(
name|RelDistribution
operator|.
name|Type
operator|.
name|HASH_DISTRIBUTED
argument_list|,
name|leftKeysListBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|join
operator|.
name|getMapJoinStreamingSide
argument_list|()
operator|==
name|MapJoinStreamingRelation
operator|.
name|RIGHT_RELATION
condition|)
block|{
name|distribution
operator|=
operator|new
name|HiveRelDistribution
argument_list|(
name|RelDistribution
operator|.
name|Type
operator|.
name|HASH_DISTRIBUTED
argument_list|,
name|rightKeysListBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|distribution
operator|=
literal|null
expr_stmt|;
block|}
break|break;
default|default:
name|distribution
operator|=
literal|null
expr_stmt|;
block|}
return|return
name|distribution
return|;
block|}
block|}
end_class

end_unit

