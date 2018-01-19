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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|session
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

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
name|hive
operator|.
name|conf
operator|.
name|HiveConf
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
name|SessionState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|SessionHandle
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|operation
operator|.
name|OperationManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TProtocolVersion
import|;
end_import

begin_comment
comment|/**  * Methods that don't need to be executed under a doAs  * context are here. Rest of them in HiveSession interface  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveSessionBase
block|{
name|TProtocolVersion
name|getProtocolVersion
parameter_list|()
function_decl|;
comment|/**    * Set the session manager for the session    * @param sessionManager    */
name|void
name|setSessionManager
parameter_list|(
name|SessionManager
name|sessionManager
parameter_list|)
function_decl|;
comment|/**    * Get the session manager for the session    */
name|SessionManager
name|getSessionManager
parameter_list|()
function_decl|;
comment|/**    * Set operation manager for the session    * @param operationManager    */
name|void
name|setOperationManager
parameter_list|(
name|OperationManager
name|operationManager
parameter_list|)
function_decl|;
comment|/**    * Check whether operation logging is enabled and session dir is created successfully    */
name|boolean
name|isOperationLogEnabled
parameter_list|()
function_decl|;
comment|/**    * Get the session dir, which is the parent dir of operation logs    * @return a file representing the parent directory of operation logs    */
name|File
name|getOperationLogSessionDir
parameter_list|()
function_decl|;
comment|/**    * Set the session dir, which is the parent dir of operation logs    * @param operationLogRootDir the parent dir of the session dir    */
name|void
name|setOperationLogSessionDir
parameter_list|(
name|File
name|operationLogRootDir
parameter_list|)
function_decl|;
name|SessionHandle
name|getSessionHandle
parameter_list|()
function_decl|;
name|String
name|getPassword
parameter_list|()
function_decl|;
name|HiveConf
name|getHiveConf
parameter_list|()
function_decl|;
name|SessionState
name|getSessionState
parameter_list|()
function_decl|;
name|String
name|getUserName
parameter_list|()
function_decl|;
name|void
name|setUserName
parameter_list|(
name|String
name|userName
parameter_list|)
function_decl|;
name|String
name|getIpAddress
parameter_list|()
function_decl|;
name|void
name|setIpAddress
parameter_list|(
name|String
name|ipAddress
parameter_list|)
function_decl|;
name|List
argument_list|<
name|String
argument_list|>
name|getForwardedAddresses
parameter_list|()
function_decl|;
name|void
name|setForwardedAddresses
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|forwardedAddresses
parameter_list|)
function_decl|;
name|long
name|getLastAccessTime
parameter_list|()
function_decl|;
name|long
name|getCreationTime
parameter_list|()
function_decl|;
name|int
name|getOpenOperationCount
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

