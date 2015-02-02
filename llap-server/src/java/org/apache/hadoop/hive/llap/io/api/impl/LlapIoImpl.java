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
name|api
operator|.
name|impl
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
name|List
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
name|ExecutorService
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
name|Executors
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
name|conf
operator|.
name|Configuration
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
name|conf
operator|.
name|HiveConf
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
name|Allocator
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
name|BuddyAllocator
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
name|Cache
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
name|LowLevelCacheImpl
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
name|LowLevelCachePolicyBase
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
name|LowLevelFifoCachePolicy
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
name|LowLevelLrfuCachePolicy
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
name|NoopCache
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
name|LlapIo
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
name|OrcCacheKey
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
name|decode
operator|.
name|OrcColumnVectorProducer
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
name|encoded
operator|.
name|OrcEncodedDataProducer
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
name|VectorizedRowBatch
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
name|sarg
operator|.
name|SearchArgument
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
name|NullWritable
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
name|mapred
operator|.
name|InputFormat
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
name|mapred
operator|.
name|InputSplit
import|;
end_import

begin_class
specifier|public
class|class
name|LlapIoImpl
implements|implements
name|LlapIo
argument_list|<
name|VectorizedRowBatch
argument_list|>
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
name|LlapIoImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|OrcColumnVectorProducer
name|cvp
decl_stmt|;
specifier|private
specifier|final
name|OrcEncodedDataProducer
name|edp
decl_stmt|;
specifier|private
name|LlapIoImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|useLowLevelCache
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_LOW_LEVEL_CACHE
argument_list|)
decl_stmt|;
comment|// High-level cache not supported yet.
name|Cache
argument_list|<
name|OrcCacheKey
argument_list|>
name|cache
init|=
name|useLowLevelCache
condition|?
literal|null
else|:
operator|new
name|NoopCache
argument_list|<
name|OrcCacheKey
argument_list|>
argument_list|()
decl_stmt|;
name|LowLevelCacheImpl
name|orcCache
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|useLowLevelCache
condition|)
block|{
name|boolean
name|useLrfu
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_USE_LRFU
argument_list|)
decl_stmt|;
name|LowLevelCachePolicyBase
name|cachePolicy
init|=
name|useLrfu
condition|?
operator|new
name|LowLevelLrfuCachePolicy
argument_list|(
name|conf
argument_list|)
else|:
operator|new
name|LowLevelFifoCachePolicy
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Allocator
name|allocator
init|=
operator|new
name|BuddyAllocator
argument_list|(
name|conf
argument_list|,
name|cachePolicy
argument_list|)
decl_stmt|;
name|orcCache
operator|=
operator|new
name|LowLevelCacheImpl
argument_list|(
name|conf
argument_list|,
name|cachePolicy
argument_list|,
name|allocator
argument_list|)
expr_stmt|;
block|}
comment|// TODO: arbitrary thread pool
name|ExecutorService
name|threadPool
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
literal|10
argument_list|)
decl_stmt|;
comment|// TODO: this should depends on input format and be in a map, or something.
name|this
operator|.
name|edp
operator|=
operator|new
name|OrcEncodedDataProducer
argument_list|(
name|orcCache
argument_list|,
name|cache
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|cvp
operator|=
operator|new
name|OrcColumnVectorProducer
argument_list|(
name|threadPool
argument_list|,
name|edp
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
name|VectorReader
name|getReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|columnIds
parameter_list|,
name|SearchArgument
name|sarg
parameter_list|,
name|String
index|[]
name|columnNames
parameter_list|)
block|{
return|return
operator|new
name|VectorReader
argument_list|(
name|split
argument_list|,
name|columnIds
argument_list|,
name|sarg
argument_list|,
name|columnNames
argument_list|,
name|cvp
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InputFormat
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
name|getInputFormat
parameter_list|(
name|InputFormat
name|sourceInputFormat
parameter_list|)
block|{
return|return
operator|new
name|LlapInputFormat
argument_list|(
name|this
argument_list|,
name|sourceInputFormat
argument_list|)
return|;
block|}
block|}
end_class

end_unit

