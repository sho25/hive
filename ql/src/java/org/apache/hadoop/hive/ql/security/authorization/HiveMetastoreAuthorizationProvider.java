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
operator|.
name|authorization
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
name|metastore
operator|.
name|IHMSHandler
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
name|AuthorizationException
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

begin_comment
comment|/**  * HiveMetastoreAuthorizationProvider : An extension of HiveAuthorizaytionProvider  * that is intended to be called from the metastore-side. It will be invoked  * by AuthorizationPreEventListener.  *  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveMetastoreAuthorizationProvider
extends|extends
name|HiveAuthorizationProvider
block|{
comment|/**    * Allows invoker of HiveMetaStoreAuthorizationProvider to send in a    * hive metastore handler that can be used to make calls to test    * whether or not authorizations can/will succeed. Intended to be called    * before any of the authorize methods are called.    * @param handler    */
name|void
name|setMetaStoreHandler
parameter_list|(
name|IHMSHandler
name|handler
parameter_list|)
function_decl|;
comment|/**    * Authorize metastore authorization api call.    */
name|void
name|authorizeAuthorizationApiInvocation
parameter_list|()
throws|throws
name|HiveException
throws|,
name|AuthorizationException
function_decl|;
block|}
end_interface

end_unit

