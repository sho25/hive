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
name|ql
operator|.
name|io
operator|.
name|orc
operator|.
name|encoded
package|;
end_package

begin_comment
comment|/**  * Data consumer; an equivalent of a data queue for an asynchronous data producer.  */
end_comment

begin_interface
specifier|public
interface|interface
name|Consumer
parameter_list|<
name|T
parameter_list|>
block|{
comment|/** Some data has been produced. */
specifier|public
name|void
name|consumeData
parameter_list|(
name|T
name|data
parameter_list|)
throws|throws
name|InterruptedException
function_decl|;
comment|/** No more data will be produced; done. */
specifier|public
name|void
name|setDone
parameter_list|()
throws|throws
name|InterruptedException
function_decl|;
comment|/** No more data will be produced; error during production. */
specifier|public
name|void
name|setError
parameter_list|(
name|Throwable
name|t
parameter_list|)
throws|throws
name|InterruptedException
function_decl|;
block|}
end_interface

end_unit

