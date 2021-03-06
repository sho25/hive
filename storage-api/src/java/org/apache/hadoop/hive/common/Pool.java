begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|common
package|;
end_package

begin_comment
comment|/**  * Simple object pool to prevent GC on small objects passed between threads.  */
end_comment

begin_interface
specifier|public
interface|interface
name|Pool
parameter_list|<
name|T
parameter_list|>
block|{
comment|/**    * Object helper for objects stored in the pool.    */
specifier|public
interface|interface
name|PoolObjectHelper
parameter_list|<
name|T
parameter_list|>
block|{
comment|/**      * Called to create an object when one cannot be provided.      *      * @return a newly allocated object      */
name|T
name|create
parameter_list|()
function_decl|;
comment|/**      * Called before the object is put in the pool (regardless of whether put succeeds).      *      * @param t the object to reset      */
name|void
name|resetBeforeOffer
parameter_list|(
name|T
name|t
parameter_list|)
function_decl|;
block|}
name|T
name|take
parameter_list|()
function_decl|;
name|void
name|offer
parameter_list|(
name|T
name|t
parameter_list|)
function_decl|;
name|int
name|size
parameter_list|()
function_decl|;
specifier|default
name|void
name|clear
parameter_list|()
block|{
comment|//no op
block|}
block|}
end_interface

end_unit

