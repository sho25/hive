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
name|qoption
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
name|ql
operator|.
name|QTestUtil
import|;
end_import

begin_comment
comment|/**  * Qtest options might be usefull to prepare the test environment or do some extra checks/cleanup.  */
end_comment

begin_interface
specifier|public
interface|interface
name|QTestOptionHandler
block|{
comment|/**    * For a matching option; the arguments are supplied to the handler by this method.     */
name|void
name|processArguments
parameter_list|(
name|String
name|arguments
parameter_list|)
function_decl|;
comment|/**    * Invoked before the actual test is executed.    *     * At the time of this call all the options for the actual test is already processed.    */
name|void
name|beforeTest
parameter_list|(
name|QTestUtil
name|qt
parameter_list|)
throws|throws
name|Exception
function_decl|;
comment|/**    * Invoked right after the test is executed.    *     * Can be used to cleanup things and/or clear internal state of the handler.    */
name|void
name|afterTest
parameter_list|(
name|QTestUtil
name|qt
parameter_list|)
throws|throws
name|Exception
function_decl|;
block|}
end_interface

end_unit

