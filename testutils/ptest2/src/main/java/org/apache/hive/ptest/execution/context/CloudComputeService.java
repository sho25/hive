begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
operator|.
name|context
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|ContextBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|aws
operator|.
name|ec2
operator|.
name|compute
operator|.
name|AWSEC2TemplateOptions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|compute
operator|.
name|ComputeService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|compute
operator|.
name|ComputeServiceContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|compute
operator|.
name|RunNodesException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|compute
operator|.
name|domain
operator|.
name|ComputeMetadata
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|compute
operator|.
name|domain
operator|.
name|NodeMetadata
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|compute
operator|.
name|domain
operator|.
name|NodeMetadata
operator|.
name|Status
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|compute
operator|.
name|domain
operator|.
name|Template
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|ec2
operator|.
name|domain
operator|.
name|InstanceType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|logging
operator|.
name|log4j
operator|.
name|config
operator|.
name|Log4JLoggingModule
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
name|base
operator|.
name|Predicate
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
name|ImmutableSet
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
name|Sets
import|;
end_import

begin_class
specifier|public
class|class
name|CloudComputeService
block|{
specifier|private
specifier|final
name|ComputeServiceContext
name|mComputeServiceContext
decl_stmt|;
specifier|private
specifier|final
name|ComputeService
name|mComputeService
decl_stmt|;
specifier|private
specifier|final
name|String
name|mGroupName
decl_stmt|;
specifier|private
specifier|final
name|String
name|mImageId
decl_stmt|;
specifier|private
specifier|final
name|String
name|mkeyPair
decl_stmt|;
specifier|private
specifier|final
name|String
name|mSecurityGroup
decl_stmt|;
specifier|private
specifier|final
name|float
name|mMaxBid
decl_stmt|;
specifier|public
name|CloudComputeService
parameter_list|(
name|String
name|apiKey
parameter_list|,
name|String
name|accessKey
parameter_list|,
name|String
name|groupName
parameter_list|,
name|String
name|imageId
parameter_list|,
name|String
name|keyPair
parameter_list|,
name|String
name|securityGroup
parameter_list|,
name|float
name|maxBid
parameter_list|)
block|{
name|mGroupName
operator|=
name|groupName
expr_stmt|;
name|mImageId
operator|=
name|imageId
expr_stmt|;
name|mkeyPair
operator|=
name|keyPair
expr_stmt|;
name|mSecurityGroup
operator|=
name|securityGroup
expr_stmt|;
name|mMaxBid
operator|=
name|maxBid
expr_stmt|;
name|mComputeServiceContext
operator|=
name|ContextBuilder
operator|.
name|newBuilder
argument_list|(
literal|"aws-ec2"
argument_list|)
operator|.
name|credentials
argument_list|(
name|apiKey
argument_list|,
name|accessKey
argument_list|)
operator|.
name|modules
argument_list|(
name|ImmutableSet
operator|.
name|of
argument_list|(
operator|new
name|Log4JLoggingModule
argument_list|()
argument_list|)
argument_list|)
operator|.
name|buildView
argument_list|(
name|ComputeServiceContext
operator|.
name|class
argument_list|)
expr_stmt|;
name|mComputeService
operator|=
name|mComputeServiceContext
operator|.
name|getComputeService
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|NodeMetadata
argument_list|>
name|createNodes
parameter_list|(
name|int
name|count
parameter_list|)
throws|throws
name|RunNodesException
block|{
name|Set
argument_list|<
name|NodeMetadata
argument_list|>
name|result
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|Template
name|template
init|=
name|mComputeService
operator|.
name|templateBuilder
argument_list|()
operator|.
name|hardwareId
argument_list|(
name|InstanceType
operator|.
name|M1_XLARGE
argument_list|)
operator|.
name|imageId
argument_list|(
name|mImageId
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|template
operator|.
name|getOptions
argument_list|()
operator|.
name|as
argument_list|(
name|AWSEC2TemplateOptions
operator|.
name|class
argument_list|)
operator|.
name|keyPair
argument_list|(
name|mkeyPair
argument_list|)
operator|.
name|securityGroupIds
argument_list|(
name|mSecurityGroup
argument_list|)
operator|.
name|blockOnPort
argument_list|(
literal|22
argument_list|,
literal|60
argument_list|)
operator|.
name|spotPrice
argument_list|(
name|mMaxBid
argument_list|)
expr_stmt|;
name|result
operator|.
name|addAll
argument_list|(
name|mComputeService
operator|.
name|createNodesInGroup
argument_list|(
name|mGroupName
argument_list|,
name|count
argument_list|,
name|template
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|Set
argument_list|<
name|NodeMetadata
argument_list|>
name|listRunningNodes
parameter_list|()
block|{
name|Set
argument_list|<
name|NodeMetadata
argument_list|>
name|result
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|result
operator|.
name|addAll
argument_list|(
name|mComputeService
operator|.
name|listNodesDetailsMatching
argument_list|(
operator|new
name|Predicate
argument_list|<
name|ComputeMetadata
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|ComputeMetadata
name|computeMetadata
parameter_list|)
block|{
name|NodeMetadata
name|nodeMetadata
init|=
operator|(
name|NodeMetadata
operator|)
name|computeMetadata
decl_stmt|;
return|return
name|nodeMetadata
operator|.
name|getStatus
argument_list|()
operator|==
name|Status
operator|.
name|RUNNING
operator|&&
name|mGroupName
operator|.
name|equalsIgnoreCase
argument_list|(
name|nodeMetadata
operator|.
name|getGroup
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|void
name|destroyNode
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
name|mComputeService
operator|.
name|destroyNode
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
name|mComputeServiceContext
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

