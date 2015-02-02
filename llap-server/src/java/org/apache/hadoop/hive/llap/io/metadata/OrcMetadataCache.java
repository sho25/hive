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
name|concurrent
operator|.
name|ExecutionException
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
name|io
operator|.
name|api
operator|.
name|orc
operator|.
name|OrcBatchKey
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
name|cache
operator|.
name|Cache
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
name|cache
operator|.
name|CacheBuilder
import|;
end_import

begin_comment
comment|/**  * ORC-specific metadata cache.  * TODO: should be merged with main cache somehow if we find this takes too much memory  */
end_comment

begin_class
specifier|public
class|class
name|OrcMetadataCache
block|{
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_CACHE_ACCESS_CONCURRENCY
init|=
literal|10
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_FILE_ENTRIES
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_STRIPE_ENTRIES
init|=
literal|10000
decl_stmt|;
specifier|private
specifier|static
name|Cache
argument_list|<
name|String
argument_list|,
name|OrcFileMetadata
argument_list|>
name|METADATA
decl_stmt|;
specifier|private
specifier|static
name|Cache
argument_list|<
name|OrcBatchKey
argument_list|,
name|OrcStripeMetadata
argument_list|>
name|STRIPE_METADATA
decl_stmt|;
static|static
block|{
name|METADATA
operator|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|concurrencyLevel
argument_list|(
name|DEFAULT_CACHE_ACCESS_CONCURRENCY
argument_list|)
operator|.
name|maximumSize
argument_list|(
name|DEFAULT_MAX_FILE_ENTRIES
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|STRIPE_METADATA
operator|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|concurrencyLevel
argument_list|(
name|DEFAULT_CACHE_ACCESS_CONCURRENCY
argument_list|)
operator|.
name|maximumSize
argument_list|(
name|DEFAULT_MAX_STRIPE_ENTRIES
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|putFileMetadata
parameter_list|(
name|String
name|filePath
parameter_list|,
name|OrcFileMetadata
name|metaData
parameter_list|)
block|{
name|METADATA
operator|.
name|put
argument_list|(
name|filePath
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|putStripeMetadata
parameter_list|(
name|OrcBatchKey
name|stripeKey
parameter_list|,
name|OrcStripeMetadata
name|metaData
parameter_list|)
block|{
name|STRIPE_METADATA
operator|.
name|put
argument_list|(
name|stripeKey
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
block|}
specifier|public
name|OrcStripeMetadata
name|getStripeMetadata
parameter_list|(
name|OrcBatchKey
name|stripeKey
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|STRIPE_METADATA
operator|.
name|getIfPresent
argument_list|(
name|stripeKey
argument_list|)
return|;
block|}
specifier|public
name|OrcFileMetadata
name|getFileMetadata
parameter_list|(
name|String
name|pathString
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|METADATA
operator|.
name|getIfPresent
argument_list|(
name|pathString
argument_list|)
return|;
block|}
block|}
end_class

end_unit

