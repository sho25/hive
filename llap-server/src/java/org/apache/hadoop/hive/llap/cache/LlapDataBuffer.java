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

begin_class
specifier|public
specifier|final
class|class
name|LlapDataBuffer
extends|extends
name|LlapAllocatorBuffer
block|{
specifier|public
specifier|static
specifier|final
name|int
name|UNKNOWN_CACHED_LENGTH
init|=
operator|-
literal|1
decl_stmt|;
comment|/** ORC cache uses this to store compressed length; buffer is cached uncompressed, but    * the lookup is on compressed ranges, so we need to know this. */
specifier|public
name|int
name|declaredCachedLength
init|=
name|UNKNOWN_CACHED_LENGTH
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|notifyEvicted
parameter_list|(
name|EvictionDispatcher
name|evictionDispatcher
parameter_list|)
block|{
name|evictionDispatcher
operator|.
name|notifyEvicted
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

