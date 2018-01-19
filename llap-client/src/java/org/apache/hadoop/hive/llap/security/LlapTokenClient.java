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
name|security
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|java
operator|.
name|util
operator|.
name|Map
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|daemon
operator|.
name|rpc
operator|.
name|LlapDaemonProtocolProtos
operator|.
name|GetTokenRequestProto
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
name|impl
operator|.
name|LlapRegistryService
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
name|hive
operator|.
name|llap
operator|.
name|registry
operator|.
name|LlapServiceInstanceSet
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
name|security
operator|.
name|LlapTokenIdentifier
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
name|DataInputByteBuffer
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|security
operator|.
name|UserGroupInformation
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
name|security
operator|.
name|token
operator|.
name|Token
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
import|;
end_import

begin_class
specifier|public
class|class
name|LlapTokenClient
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LlapTokenClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|LlapRegistryService
name|registry
decl_stmt|;
specifier|private
specifier|final
name|SocketFactory
name|socketFactory
decl_stmt|;
specifier|private
specifier|final
name|RetryPolicy
name|retryPolicy
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|LlapServiceInstanceSet
name|activeInstances
decl_stmt|;
specifier|private
name|Collection
argument_list|<
name|LlapServiceInstance
argument_list|>
name|lastKnownInstances
decl_stmt|;
specifier|private
name|LlapManagementProtocolClientImpl
name|client
decl_stmt|;
specifier|private
name|LlapServiceInstance
name|clientInstance
decl_stmt|;
specifier|public
name|LlapTokenClient
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|registry
operator|=
operator|new
name|LlapRegistryService
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|registry
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|socketFactory
operator|=
name|NetUtils
operator|.
name|getDefaultSocketFactory
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|retryPolicy
operator|=
name|RetryPolicies
operator|.
name|retryUpToMaximumTimeWithFixedSleep
argument_list|(
literal|16000
argument_list|,
literal|2000l
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|getDelegationToken
parameter_list|(
name|String
name|appId
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|UserGroupInformation
operator|.
name|isSecurityEnabled
argument_list|()
condition|)
return|return
literal|null
return|;
name|Iterator
argument_list|<
name|LlapServiceInstance
argument_list|>
name|llaps
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|clientInstance
operator|==
literal|null
condition|)
block|{
assert|assert
name|client
operator|==
literal|null
assert|;
name|llaps
operator|=
name|getLlapServices
argument_list|(
literal|false
argument_list|)
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|clientInstance
operator|=
name|llaps
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
name|ByteString
name|tokenBytes
init|=
literal|null
decl_stmt|;
name|boolean
name|hasRefreshed
init|=
literal|false
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|tokenBytes
operator|=
name|getTokenBytes
argument_list|(
name|appId
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|ServiceException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Cannot get a token, trying a different instance"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|client
operator|=
literal|null
expr_stmt|;
name|clientInstance
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|llaps
operator|==
literal|null
operator|||
operator|!
name|llaps
operator|.
name|hasNext
argument_list|()
condition|)
block|{
if|if
condition|(
name|hasRefreshed
condition|)
block|{
comment|// Only refresh once.
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot find any LLAPs to get the token from"
argument_list|)
throw|;
block|}
name|llaps
operator|=
name|getLlapServices
argument_list|(
literal|true
argument_list|)
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|hasRefreshed
operator|=
literal|true
expr_stmt|;
block|}
name|clientInstance
operator|=
name|llaps
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|token
init|=
name|extractToken
argument_list|(
name|tokenBytes
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Obtained a LLAP delegation token from "
operator|+
name|clientInstance
operator|+
literal|": "
operator|+
name|token
argument_list|)
expr_stmt|;
block|}
return|return
name|token
return|;
block|}
specifier|private
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|extractToken
parameter_list|(
name|ByteString
name|tokenBytes
parameter_list|)
throws|throws
name|IOException
block|{
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|token
init|=
operator|new
name|Token
argument_list|<>
argument_list|()
decl_stmt|;
name|DataInputByteBuffer
name|in
init|=
operator|new
name|DataInputByteBuffer
argument_list|()
decl_stmt|;
name|in
operator|.
name|reset
argument_list|(
name|tokenBytes
operator|.
name|asReadOnlyByteBuffer
argument_list|()
argument_list|)
expr_stmt|;
name|token
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|token
return|;
block|}
specifier|private
name|ByteString
name|getTokenBytes
parameter_list|(
specifier|final
name|String
name|appId
parameter_list|)
throws|throws
name|IOException
throws|,
name|ServiceException
block|{
assert|assert
name|clientInstance
operator|!=
literal|null
assert|;
if|if
condition|(
name|client
operator|==
literal|null
condition|)
block|{
name|client
operator|=
operator|new
name|LlapManagementProtocolClientImpl
argument_list|(
name|conf
argument_list|,
name|clientInstance
operator|.
name|getHost
argument_list|()
argument_list|,
name|clientInstance
operator|.
name|getManagementPort
argument_list|()
argument_list|,
name|retryPolicy
argument_list|,
name|socketFactory
argument_list|)
expr_stmt|;
block|}
name|GetTokenRequestProto
operator|.
name|Builder
name|req
init|=
name|GetTokenRequestProto
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|appId
argument_list|)
condition|)
block|{
name|req
operator|.
name|setAppId
argument_list|(
name|appId
argument_list|)
expr_stmt|;
block|}
return|return
name|client
operator|.
name|getDelegationToken
argument_list|(
literal|null
argument_list|,
name|req
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|getToken
argument_list|()
return|;
block|}
comment|/** Synchronized - LLAP registry and instance set are not thread safe. */
specifier|private
specifier|synchronized
name|List
argument_list|<
name|LlapServiceInstance
argument_list|>
name|getLlapServices
parameter_list|(
name|boolean
name|doForceRefresh
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|doForceRefresh
operator|&&
name|lastKnownInstances
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|lastKnownInstances
argument_list|)
return|;
block|}
if|if
condition|(
name|activeInstances
operator|==
literal|null
condition|)
block|{
name|registry
operator|.
name|start
argument_list|()
expr_stmt|;
name|activeInstances
operator|=
name|registry
operator|.
name|getInstances
argument_list|()
expr_stmt|;
block|}
name|Collection
argument_list|<
name|LlapServiceInstance
argument_list|>
name|daemons
init|=
name|activeInstances
operator|.
name|getAll
argument_list|()
decl_stmt|;
if|if
condition|(
name|daemons
operator|==
literal|null
operator|||
name|daemons
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"No LLAPs found"
argument_list|)
throw|;
block|}
name|lastKnownInstances
operator|=
name|daemons
expr_stmt|;
return|return
operator|new
name|ArrayList
argument_list|<
name|LlapServiceInstance
argument_list|>
argument_list|(
name|lastKnownInstances
argument_list|)
return|;
block|}
block|}
end_class

end_unit

