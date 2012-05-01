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
name|hooks
package|;
end_package

begin_comment
comment|/**  *  * Classes implementing this interface create JDBC connection URL's.  * This can also be used to store a parameters array  */
end_comment

begin_interface
specifier|public
interface|interface
name|ConnectionUrlFactory
block|{
specifier|public
name|void
name|init
parameter_list|(
name|String
name|param1Name
parameter_list|,
name|String
name|param2Name
parameter_list|)
function_decl|;
comment|/**    * @return the JDBC connection URL    * @throws Exception    */
name|String
name|getUrl
parameter_list|()
throws|throws
name|Exception
function_decl|;
name|String
name|getUrl
parameter_list|(
name|boolean
name|isWrite
parameter_list|)
throws|throws
name|Exception
function_decl|;
name|String
name|getValue
parameter_list|(
name|String
name|param1
parameter_list|,
name|String
name|param2
parameter_list|)
throws|throws
name|Exception
function_decl|;
name|void
name|updateProps
parameter_list|(
name|String
name|param1
parameter_list|,
name|String
name|param2
parameter_list|,
name|String
name|param3
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

