begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
operator|.
name|metrics
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|executor
operator|.
name|TaskMetrics
import|;
end_import

begin_comment
comment|/**  * Metrics pertaining to reading shuffle data.  */
end_comment

begin_class
specifier|public
class|class
name|ShuffleReadMetrics
implements|implements
name|Serializable
block|{
comment|/** Number of remote blocks fetched in shuffles by tasks. */
specifier|public
specifier|final
name|int
name|remoteBlocksFetched
decl_stmt|;
comment|/** Number of local blocks fetched in shuffles by tasks. */
specifier|public
specifier|final
name|int
name|localBlocksFetched
decl_stmt|;
comment|/**    * Time tasks spent waiting for remote shuffle blocks. This only includes the    * time blocking on shuffle input data. For instance if block B is being    * fetched while the task is still not finished processing block A, it is not    * considered to be blocking on block B.    */
specifier|public
specifier|final
name|long
name|fetchWaitTime
decl_stmt|;
comment|/** Total number of remote bytes read from the shuffle by tasks. */
specifier|public
specifier|final
name|long
name|remoteBytesRead
decl_stmt|;
specifier|public
name|ShuffleReadMetrics
parameter_list|(
name|int
name|remoteBlocksFetched
parameter_list|,
name|int
name|localBlocksFetched
parameter_list|,
name|long
name|fetchWaitTime
parameter_list|,
name|long
name|remoteBytesRead
parameter_list|)
block|{
name|this
operator|.
name|remoteBlocksFetched
operator|=
name|remoteBlocksFetched
expr_stmt|;
name|this
operator|.
name|localBlocksFetched
operator|=
name|localBlocksFetched
expr_stmt|;
name|this
operator|.
name|fetchWaitTime
operator|=
name|fetchWaitTime
expr_stmt|;
name|this
operator|.
name|remoteBytesRead
operator|=
name|remoteBytesRead
expr_stmt|;
block|}
specifier|public
name|ShuffleReadMetrics
parameter_list|(
name|TaskMetrics
name|metrics
parameter_list|)
block|{
name|this
argument_list|(
name|metrics
operator|.
name|shuffleReadMetrics
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|remoteBlocksFetched
argument_list|()
argument_list|,
name|metrics
operator|.
name|shuffleReadMetrics
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|localBlocksFetched
argument_list|()
argument_list|,
name|metrics
operator|.
name|shuffleReadMetrics
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|fetchWaitTime
argument_list|()
argument_list|,
name|metrics
operator|.
name|shuffleReadMetrics
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|remoteBytesRead
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Number of blocks fetched in shuffle by tasks (remote or local).    */
specifier|public
name|int
name|getTotalBlocksFetched
parameter_list|()
block|{
return|return
name|remoteBlocksFetched
operator|+
name|localBlocksFetched
return|;
block|}
block|}
end_class

end_unit

