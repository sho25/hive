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
name|processor
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
name|api
operator|.
name|Vector
import|;
end_import

begin_comment
comment|/**  * Interface implemented by reader; allows it to receive blocks asynchronously.  */
end_comment

begin_interface
specifier|public
interface|interface
name|ChunkConsumer
block|{
specifier|public
name|void
name|init
parameter_list|(
name|ChunkProducerFeedback
name|feedback
parameter_list|)
function_decl|;
specifier|public
name|void
name|setDone
parameter_list|()
function_decl|;
comment|// For now this returns Vector, which has to have full rows.
comment|// Vectorization cannot run on non-full rows anyway so that's ok. Maybe later we can
comment|// have LazyVRB which only loads columns when needed... one can dream right?
specifier|public
name|void
name|consumeVector
parameter_list|(
name|Vector
name|vector
parameter_list|)
function_decl|;
specifier|public
name|void
name|setError
parameter_list|(
name|Throwable
name|t
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

