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
name|io
operator|.
name|orc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|List
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
name|WritableComparable
import|;
end_import

begin_comment
comment|/**  * Value for OrcFileMergeMapper. Contains stripe related information for the  * current orc file that is being merged.  */
end_comment

begin_class
specifier|public
class|class
name|OrcFileValueWrapper
implements|implements
name|WritableComparable
argument_list|<
name|OrcFileValueWrapper
argument_list|>
block|{
specifier|protected
name|StripeInformation
name|stripeInformation
decl_stmt|;
specifier|protected
name|OrcProto
operator|.
name|StripeStatistics
name|stripeStatistics
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|OrcProto
operator|.
name|UserMetadataItem
argument_list|>
name|userMetadata
decl_stmt|;
specifier|protected
name|boolean
name|lastStripeInFile
decl_stmt|;
specifier|public
name|List
argument_list|<
name|OrcProto
operator|.
name|UserMetadataItem
argument_list|>
name|getUserMetadata
parameter_list|()
block|{
return|return
name|userMetadata
return|;
block|}
specifier|public
name|void
name|setUserMetadata
parameter_list|(
name|List
argument_list|<
name|OrcProto
operator|.
name|UserMetadataItem
argument_list|>
name|userMetadata
parameter_list|)
block|{
name|this
operator|.
name|userMetadata
operator|=
name|userMetadata
expr_stmt|;
block|}
specifier|public
name|boolean
name|isLastStripeInFile
parameter_list|()
block|{
return|return
name|lastStripeInFile
return|;
block|}
specifier|public
name|void
name|setLastStripeInFile
parameter_list|(
name|boolean
name|lastStripeInFile
parameter_list|)
block|{
name|this
operator|.
name|lastStripeInFile
operator|=
name|lastStripeInFile
expr_stmt|;
block|}
specifier|public
name|OrcProto
operator|.
name|StripeStatistics
name|getStripeStatistics
parameter_list|()
block|{
return|return
name|stripeStatistics
return|;
block|}
specifier|public
name|void
name|setStripeStatistics
parameter_list|(
name|OrcProto
operator|.
name|StripeStatistics
name|stripeStatistics
parameter_list|)
block|{
name|this
operator|.
name|stripeStatistics
operator|=
name|stripeStatistics
expr_stmt|;
block|}
specifier|public
name|StripeInformation
name|getStripeInformation
parameter_list|()
block|{
return|return
name|stripeInformation
return|;
block|}
specifier|public
name|void
name|setStripeInformation
parameter_list|(
name|StripeInformation
name|stripeInformation
parameter_list|)
block|{
name|this
operator|.
name|stripeInformation
operator|=
name|stripeInformation
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not supported."
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not supported."
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|OrcFileValueWrapper
name|o
parameter_list|)
block|{
if|if
condition|(
name|stripeInformation
operator|.
name|getOffset
argument_list|()
operator|<
name|o
operator|.
name|getStripeInformation
argument_list|()
operator|.
name|getOffset
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|stripeInformation
operator|.
name|getOffset
argument_list|()
operator|>
name|o
operator|.
name|getStripeInformation
argument_list|()
operator|.
name|getOffset
argument_list|()
condition|)
block|{
return|return
literal|1
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
block|}
end_class

end_unit

