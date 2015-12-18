begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|cli
operator|.
name|SemanticAnalysis
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
name|conf
operator|.
name|Configuration
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

begin_class
specifier|final
class|class
name|HCatAuthUtil
block|{
specifier|public
specifier|static
name|boolean
name|isAuthorizationEnabled
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// the session state getAuthorizer can return null even if authorization is
comment|// enabled if the V2 api of authorizer in use.
comment|// The additional authorization checks happening in hcatalog are designed to
comment|// work with  storage based authorization (on client side). It should not try doing
comment|// additional checks if a V2 authorizer is in use. The reccomended configuration is to
comment|// use storage based authorization in metastore server
return|return
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_ENABLED
argument_list|)
operator|&&
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getAuthorizer
argument_list|()
operator|!=
literal|null
return|;
block|}
block|}
end_class

end_unit

