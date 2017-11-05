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
name|metastore
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
name|ConcurrentHashMap
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
name|atomic
operator|.
name|AtomicInteger
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
name|security
operator|.
name|token
operator|.
name|delegation
operator|.
name|AbstractDelegationTokenSecretManager
operator|.
name|DelegationTokenInformation
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

begin_comment
comment|/**  * Default in-memory token store implementation.  */
end_comment

begin_class
specifier|public
class|class
name|MemoryTokenStore
implements|implements
name|DelegationTokenStore
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
name|MemoryTokenStore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|masterKeys
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentHashMap
argument_list|<
name|DelegationTokenIdentifier
argument_list|,
name|DelegationTokenInformation
argument_list|>
name|tokens
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|masterKeySeq
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setConf
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
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|this
operator|.
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|addMasterKey
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|int
name|keySeq
init|=
name|masterKeySeq
operator|.
name|getAndIncrement
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"addMasterKey: s = {}, keySeq = {}"
argument_list|,
name|s
argument_list|,
name|keySeq
argument_list|)
expr_stmt|;
name|masterKeys
operator|.
name|put
argument_list|(
name|keySeq
argument_list|,
name|s
argument_list|)
expr_stmt|;
return|return
name|keySeq
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateMasterKey
parameter_list|(
name|int
name|keySeq
parameter_list|,
name|String
name|s
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"updateMasterKey: s = {}, keySeq = {}"
argument_list|,
name|s
argument_list|,
name|keySeq
argument_list|)
expr_stmt|;
name|masterKeys
operator|.
name|put
argument_list|(
name|keySeq
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|removeMasterKey
parameter_list|(
name|int
name|keySeq
parameter_list|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"removeMasterKey: keySeq = {}"
argument_list|,
name|keySeq
argument_list|)
expr_stmt|;
return|return
name|masterKeys
operator|.
name|remove
argument_list|(
name|keySeq
argument_list|)
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getMasterKeys
parameter_list|()
block|{
return|return
name|masterKeys
operator|.
name|values
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|addToken
parameter_list|(
name|DelegationTokenIdentifier
name|tokenIdentifier
parameter_list|,
name|DelegationTokenInformation
name|token
parameter_list|)
block|{
name|DelegationTokenInformation
name|tokenInfo
init|=
name|tokens
operator|.
name|putIfAbsent
argument_list|(
name|tokenIdentifier
argument_list|,
name|token
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"addToken: tokenIdentifier = {}, added = {}"
argument_list|,
name|tokenIdentifier
argument_list|,
operator|(
name|tokenInfo
operator|==
literal|null
operator|)
argument_list|)
expr_stmt|;
return|return
operator|(
name|tokenInfo
operator|==
literal|null
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|removeToken
parameter_list|(
name|DelegationTokenIdentifier
name|tokenIdentifier
parameter_list|)
block|{
name|DelegationTokenInformation
name|tokenInfo
init|=
name|tokens
operator|.
name|remove
argument_list|(
name|tokenIdentifier
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"removeToken: tokenIdentifier = {}, removed = "
argument_list|,
name|tokenIdentifier
argument_list|,
operator|(
name|tokenInfo
operator|!=
literal|null
operator|)
argument_list|)
expr_stmt|;
return|return
name|tokenInfo
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|DelegationTokenInformation
name|getToken
parameter_list|(
name|DelegationTokenIdentifier
name|tokenIdentifier
parameter_list|)
block|{
name|DelegationTokenInformation
name|result
init|=
name|tokens
operator|.
name|get
argument_list|(
name|tokenIdentifier
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"getToken: tokenIdentifier = {}, result = {}"
argument_list|,
name|tokenIdentifier
argument_list|,
name|result
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
name|getAllDelegationTokenIdentifiers
parameter_list|()
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|tokens
operator|.
name|keySet
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|//no-op
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Object
name|hmsHandler
parameter_list|,
name|HadoopThriftAuthBridge
operator|.
name|Server
operator|.
name|ServerMode
name|smode
parameter_list|)
throws|throws
name|TokenStoreException
block|{
comment|// no-op
block|}
block|}
end_class

end_unit

