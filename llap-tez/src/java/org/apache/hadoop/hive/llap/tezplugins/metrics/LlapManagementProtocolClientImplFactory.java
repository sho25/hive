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
operator|.
name|tezplugins
operator|.
name|metrics
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
name|llap
operator|.
name|impl
operator|.
name|LlapManagementProtocolClientImpl
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
name|llap
operator|.
name|registry
operator|.
name|LlapServiceInstance
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
name|io
operator|.
name|retry
operator|.
name|RetryPolicies
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
name|io
operator|.
name|retry
operator|.
name|RetryPolicy
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
name|net
operator|.
name|NetUtils
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|net
operator|.
name|SocketFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * Creates a LlapManagementProtocolClientImpl from a given LlapServiceInstance.  */
end_comment

begin_class
specifier|public
class|class
name|LlapManagementProtocolClientImplFactory
block|{
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|RetryPolicy
name|retryPolicy
decl_stmt|;
specifier|private
specifier|final
name|SocketFactory
name|socketFactory
decl_stmt|;
specifier|public
name|LlapManagementProtocolClientImplFactory
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|RetryPolicy
name|retryPolicy
parameter_list|,
name|SocketFactory
name|socketFactory
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|retryPolicy
operator|=
name|retryPolicy
expr_stmt|;
name|this
operator|.
name|socketFactory
operator|=
name|socketFactory
expr_stmt|;
block|}
specifier|public
specifier|static
name|LlapManagementProtocolClientImplFactory
name|basicInstance
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|LlapManagementProtocolClientImplFactory
argument_list|(
name|conf
argument_list|,
name|RetryPolicies
operator|.
name|retryUpToMaximumCountWithFixedSleep
argument_list|(
literal|5
argument_list|,
literal|3000L
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
name|NetUtils
operator|.
name|getDefaultSocketFactory
argument_list|(
name|conf
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|LlapManagementProtocolClientImpl
name|create
parameter_list|(
name|LlapServiceInstance
name|serviceInstance
parameter_list|)
block|{
return|return
operator|new
name|LlapManagementProtocolClientImpl
argument_list|(
name|conf
argument_list|,
name|serviceInstance
operator|.
name|getHost
argument_list|()
argument_list|,
name|serviceInstance
operator|.
name|getManagementPort
argument_list|()
argument_list|,
name|retryPolicy
argument_list|,
name|socketFactory
argument_list|)
return|;
block|}
block|}
end_class

end_unit

