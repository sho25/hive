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
name|security
operator|.
name|authorization
operator|.
name|plugin
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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
operator|.
name|Public
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
name|common
operator|.
name|classification
operator|.
name|InterfaceStability
operator|.
name|Evolving
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

begin_comment
comment|/**  * Implementation of this interface specified through hive configuration will be used to  * create  {@link HiveAuthorizer} instance used for hive authorization.  *  */
end_comment

begin_interface
annotation|@
name|Public
annotation|@
name|Evolving
specifier|public
interface|interface
name|HiveAuthorizerFactory
block|{
comment|/**    * Create a new instance of HiveAuthorizer, initialized with the given objects.    * @param metastoreClientFactory - Use this to get the valid meta store client (IMetaStoreClient)    *  for the current thread. Each invocation of method in HiveAuthorizer can happen in    *  different thread, so get the current instance in each method invocation.    * @param conf - current HiveConf    * @param hiveCurrentUser - user for current session    * @return new instance of HiveAuthorizer    */
name|HiveAuthorizer
name|createHiveAuthorizer
parameter_list|(
name|HiveMetastoreClientFactory
name|metastoreClientFactory
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|String
name|hiveCurrentUser
parameter_list|)
throws|throws
name|HiveAuthorizationPluginException
function_decl|;
block|}
end_interface

end_unit

