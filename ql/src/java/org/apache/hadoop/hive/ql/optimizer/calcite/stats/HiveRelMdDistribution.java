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
name|BuiltInMetadata
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
name|MetadataDef
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
name|MetadataHandler
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
name|rel
operator|.
name|metadata
operator|.
name|RelMetadataQuery
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
implements|implements
name|MetadataHandler
argument_list|<
name|BuiltInMetadata
operator|.
name|Distribution
argument_list|>
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
name|MetadataDef
argument_list|<
name|BuiltInMetadata
operator|.
name|Distribution
argument_list|>
name|getDef
parameter_list|()
block|{
return|return
name|BuiltInMetadata
operator|.
name|Distribution
operator|.
name|DEF
return|;
block|}
specifier|public
name|RelDistribution
name|distribution
parameter_list|(
name|HiveAggregate
name|aggregate
parameter_list|,
name|RelMetadataQuery
name|mq
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
parameter_list|,
name|RelMetadataQuery
name|mq
parameter_list|)
block|{
return|return
name|join
operator|.
name|getDistribution
argument_list|()
return|;
block|}
block|}
end_class

end_unit

