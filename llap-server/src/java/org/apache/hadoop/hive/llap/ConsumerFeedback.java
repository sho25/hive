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
name|llap
package|;
end_package

begin_comment
comment|/**  * Consumer feedback typically used by Consumer&lt;T&gt;;  * allows consumer to influence production of data.  */
end_comment

begin_interface
specifier|public
interface|interface
name|ConsumerFeedback
parameter_list|<
name|T
parameter_list|>
block|{
comment|/** Pause data production. */
specifier|public
name|void
name|pause
parameter_list|()
function_decl|;
comment|/** Unpause data production. */
specifier|public
name|void
name|unpause
parameter_list|()
function_decl|;
comment|/** Stop data production, the external operation has been cancelled. */
specifier|public
name|void
name|stop
parameter_list|()
function_decl|;
comment|/** Returns processed data back to producer; necessary if e.g. data is locked in cache. */
specifier|public
name|void
name|returnData
parameter_list|(
name|T
name|data
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

