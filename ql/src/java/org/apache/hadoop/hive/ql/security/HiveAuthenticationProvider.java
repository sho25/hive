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
name|security
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|conf
operator|.
name|Configurable
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
name|session
operator|.
name|ISessionAuthState
import|;
end_import

begin_comment
comment|/**  * HiveAuthenticationProvider is an interface for authentication. The  * implementation should return userNames and groupNames.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveAuthenticationProvider
extends|extends
name|Configurable
block|{
specifier|public
name|String
name|getUserName
parameter_list|()
function_decl|;
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getGroupNames
parameter_list|()
function_decl|;
specifier|public
name|void
name|destroy
parameter_list|()
throws|throws
name|HiveException
function_decl|;
comment|/**    * This function is meant to be used only for hive internal implementations of this interface.    * SessionState is not a public interface.    * @param ss SessionState that created this instance    */
specifier|public
name|void
name|setSessionState
parameter_list|(
name|ISessionAuthState
name|ss
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

