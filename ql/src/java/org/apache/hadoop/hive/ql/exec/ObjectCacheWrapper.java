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
name|exec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
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
name|Future
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
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_class
specifier|public
class|class
name|ObjectCacheWrapper
implements|implements
name|ObjectCache
block|{
specifier|private
specifier|final
name|String
name|queryId
decl_stmt|;
specifier|private
specifier|final
name|ObjectCache
name|globalCache
decl_stmt|;
specifier|public
name|ObjectCacheWrapper
parameter_list|(
name|ObjectCache
name|globalCache
parameter_list|,
name|String
name|queryId
parameter_list|)
block|{
name|this
operator|.
name|queryId
operator|=
name|queryId
expr_stmt|;
name|this
operator|.
name|globalCache
operator|=
name|globalCache
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|release
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|globalCache
operator|.
name|release
argument_list|(
name|makeKey
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|retrieve
parameter_list|(
name|String
name|key
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|globalCache
operator|.
name|retrieve
argument_list|(
name|makeKey
argument_list|(
name|key
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|retrieve
parameter_list|(
name|String
name|key
parameter_list|,
name|Callable
argument_list|<
name|T
argument_list|>
name|fn
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|globalCache
operator|.
name|retrieve
argument_list|(
name|makeKey
argument_list|(
name|key
argument_list|)
argument_list|,
name|fn
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|Future
argument_list|<
name|T
argument_list|>
name|retrieveAsync
parameter_list|(
name|String
name|key
parameter_list|,
name|Callable
argument_list|<
name|T
argument_list|>
name|fn
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|globalCache
operator|.
name|retrieveAsync
argument_list|(
name|makeKey
argument_list|(
name|key
argument_list|)
argument_list|,
name|fn
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|globalCache
operator|.
name|remove
argument_list|(
name|makeKey
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|makeKey
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
name|queryId
operator|+
literal|"_"
operator|+
name|key
return|;
block|}
block|}
end_class

end_unit

