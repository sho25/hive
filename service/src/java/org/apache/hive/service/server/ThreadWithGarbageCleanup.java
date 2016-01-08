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
name|hive
operator|.
name|service
operator|.
name|server
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|HiveMetaStore
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
name|RawStore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * A HiveServer2 thread used to construct new server threads.  * In particular, this thread ensures an orderly cleanup,  * when killed by its corresponding ExecutorService.  */
end_comment

begin_class
specifier|public
class|class
name|ThreadWithGarbageCleanup
extends|extends
name|Thread
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ThreadWithGarbageCleanup
operator|.
name|class
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Long
argument_list|,
name|RawStore
argument_list|>
name|threadRawStoreMap
init|=
name|ThreadFactoryWithGarbageCleanup
operator|.
name|getThreadRawStoreMap
argument_list|()
decl_stmt|;
specifier|public
name|ThreadWithGarbageCleanup
parameter_list|(
name|Runnable
name|runnable
parameter_list|)
block|{
name|super
argument_list|(
name|runnable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add any Thread specific garbage cleanup code here.    * Currently, it shuts down the RawStore object for this thread if it is not null.    */
annotation|@
name|Override
specifier|public
name|void
name|finalize
parameter_list|()
throws|throws
name|Throwable
block|{
name|cleanRawStore
argument_list|()
expr_stmt|;
name|super
operator|.
name|finalize
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|cleanRawStore
parameter_list|()
block|{
name|Long
name|threadId
init|=
name|this
operator|.
name|getId
argument_list|()
decl_stmt|;
name|RawStore
name|threadLocalRawStore
init|=
name|threadRawStoreMap
operator|.
name|get
argument_list|(
name|threadId
argument_list|)
decl_stmt|;
if|if
condition|(
name|threadLocalRawStore
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"RawStore: "
operator|+
name|threadLocalRawStore
operator|+
literal|", for the thread: "
operator|+
name|this
operator|.
name|getName
argument_list|()
operator|+
literal|" will be closed now."
argument_list|)
expr_stmt|;
name|threadLocalRawStore
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|threadRawStoreMap
operator|.
name|remove
argument_list|(
name|threadId
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Cache the ThreadLocal RawStore object. Called from the corresponding thread.    */
specifier|public
name|void
name|cacheThreadLocalRawStore
parameter_list|()
block|{
name|Long
name|threadId
init|=
name|this
operator|.
name|getId
argument_list|()
decl_stmt|;
name|RawStore
name|threadLocalRawStore
init|=
name|HiveMetaStore
operator|.
name|HMSHandler
operator|.
name|getRawStore
argument_list|()
decl_stmt|;
if|if
condition|(
name|threadLocalRawStore
operator|!=
literal|null
operator|&&
operator|!
name|threadRawStoreMap
operator|.
name|containsKey
argument_list|(
name|threadId
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding RawStore: "
operator|+
name|threadLocalRawStore
operator|+
literal|", for the thread: "
operator|+
name|this
operator|.
name|getName
argument_list|()
operator|+
literal|" to threadRawStoreMap for future cleanup."
argument_list|)
expr_stmt|;
name|threadRawStoreMap
operator|.
name|put
argument_list|(
name|threadId
argument_list|,
name|threadLocalRawStore
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

