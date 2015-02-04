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
name|cache
package|;
end_package

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
name|EncodedColumnBatch
operator|.
name|StreamBuffer
import|;
end_import

begin_class
specifier|public
class|class
name|NoopCache
parameter_list|<
name|CacheKey
parameter_list|>
implements|implements
name|Cache
argument_list|<
name|CacheKey
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|StreamBuffer
index|[]
name|cacheOrGet
parameter_list|(
name|CacheKey
name|key
parameter_list|,
name|StreamBuffer
index|[]
name|value
parameter_list|)
block|{
return|return
name|value
return|;
block|}
annotation|@
name|Override
specifier|public
name|StreamBuffer
index|[]
name|get
parameter_list|(
name|CacheKey
name|key
parameter_list|)
block|{
return|return
literal|null
return|;
comment|// TODO: ensure real implementation increases refcount
block|}
block|}
end_class

end_unit

