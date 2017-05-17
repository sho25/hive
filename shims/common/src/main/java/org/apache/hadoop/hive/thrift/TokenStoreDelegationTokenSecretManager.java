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
name|thrift
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
import|;
end_import

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
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|binary
operator|.
name|Base64
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
name|Writable
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
name|DelegationKey
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
name|HiveDelegationTokenSupport
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
name|util
operator|.
name|Daemon
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
name|util
operator|.
name|StringUtils
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
comment|/**  * Extension of {@link DelegationTokenSecretManager} to support alternative to default in-memory  * token management for fail-over and clustering through plug-able token store (ZooKeeper etc.).  * Delegation tokens will be retrieved from the store on-demand and (unlike base class behavior) not  * cached in memory. This avoids complexities related to token expiration. The security token is  * needed only at the time the transport is opened (as opposed to per interface operation). The  * assumption therefore is low cost of interprocess token retrieval (for random read efficient store  * such as ZooKeeper) compared to overhead of synchronizing per-process in-memory token caches.  * The wrapper incorporates the token store abstraction within the limitations of current  * Hive/Hadoop dependency (.20S) with minimum code duplication.  * Eventually this should be supported by Hadoop security directly.  */
end_comment

begin_class
specifier|public
class|class
name|TokenStoreDelegationTokenSecretManager
extends|extends
name|DelegationTokenSecretManager
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TokenStoreDelegationTokenSecretManager
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
specifier|private
name|long
name|keyUpdateInterval
decl_stmt|;
specifier|final
specifier|private
name|long
name|tokenRemoverScanInterval
decl_stmt|;
specifier|private
name|Thread
name|tokenRemoverThread
decl_stmt|;
specifier|final
specifier|private
name|DelegationTokenStore
name|tokenStore
decl_stmt|;
specifier|public
name|TokenStoreDelegationTokenSecretManager
parameter_list|(
name|long
name|delegationKeyUpdateInterval
parameter_list|,
name|long
name|delegationTokenMaxLifetime
parameter_list|,
name|long
name|delegationTokenRenewInterval
parameter_list|,
name|long
name|delegationTokenRemoverScanInterval
parameter_list|,
name|DelegationTokenStore
name|sharedStore
parameter_list|)
block|{
name|super
argument_list|(
name|delegationKeyUpdateInterval
argument_list|,
name|delegationTokenMaxLifetime
argument_list|,
name|delegationTokenRenewInterval
argument_list|,
name|delegationTokenRemoverScanInterval
argument_list|)
expr_stmt|;
name|this
operator|.
name|keyUpdateInterval
operator|=
name|delegationKeyUpdateInterval
expr_stmt|;
name|this
operator|.
name|tokenRemoverScanInterval
operator|=
name|delegationTokenRemoverScanInterval
expr_stmt|;
name|this
operator|.
name|tokenStore
operator|=
name|sharedStore
expr_stmt|;
block|}
specifier|protected
name|Map
argument_list|<
name|Integer
argument_list|,
name|DelegationKey
argument_list|>
name|reloadKeys
parameter_list|()
block|{
comment|// read keys from token store
name|String
index|[]
name|allKeys
init|=
name|tokenStore
operator|.
name|getMasterKeys
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|DelegationKey
argument_list|>
name|keys
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|DelegationKey
argument_list|>
argument_list|(
name|allKeys
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|keyStr
range|:
name|allKeys
control|)
block|{
name|DelegationKey
name|key
init|=
operator|new
name|DelegationKey
argument_list|()
decl_stmt|;
try|try
block|{
name|decodeWritable
argument_list|(
name|key
argument_list|,
name|keyStr
argument_list|)
expr_stmt|;
name|keys
operator|.
name|put
argument_list|(
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
literal|"Failed to load master key."
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
synchronized|synchronized
init|(
name|this
init|)
block|{
name|super
operator|.
name|allKeys
operator|.
name|clear
argument_list|()
expr_stmt|;
name|super
operator|.
name|allKeys
operator|.
name|putAll
argument_list|(
name|keys
argument_list|)
expr_stmt|;
block|}
return|return
name|keys
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|retrievePassword
parameter_list|(
name|DelegationTokenIdentifier
name|identifier
parameter_list|)
throws|throws
name|InvalidToken
block|{
name|DelegationTokenInformation
name|info
init|=
name|this
operator|.
name|tokenStore
operator|.
name|getToken
argument_list|(
name|identifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|InvalidToken
argument_list|(
literal|"token expired or does not exist: "
operator|+
name|identifier
argument_list|)
throw|;
block|}
comment|// must reuse super as info.getPassword is not accessible
synchronized|synchronized
init|(
name|this
init|)
block|{
try|try
block|{
name|super
operator|.
name|currentTokens
operator|.
name|put
argument_list|(
name|identifier
argument_list|,
name|info
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|retrievePassword
argument_list|(
name|identifier
argument_list|)
return|;
block|}
finally|finally
block|{
name|super
operator|.
name|currentTokens
operator|.
name|remove
argument_list|(
name|identifier
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|DelegationTokenIdentifier
name|cancelToken
parameter_list|(
name|Token
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
name|token
parameter_list|,
name|String
name|canceller
parameter_list|)
throws|throws
name|IOException
block|{
name|DelegationTokenIdentifier
name|id
init|=
name|getTokenIdentifier
argument_list|(
name|token
argument_list|)
decl_stmt|;
name|LOGGER
operator|.
name|info
argument_list|(
literal|"Token cancelation requested for identifier: "
operator|+
name|id
argument_list|)
expr_stmt|;
name|this
operator|.
name|tokenStore
operator|.
name|removeToken
argument_list|(
name|id
argument_list|)
expr_stmt|;
return|return
name|id
return|;
block|}
comment|/**    * Create the password and add it to shared store.    */
annotation|@
name|Override
specifier|protected
name|byte
index|[]
name|createPassword
parameter_list|(
name|DelegationTokenIdentifier
name|id
parameter_list|)
block|{
name|byte
index|[]
name|password
decl_stmt|;
name|DelegationTokenInformation
name|info
decl_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
name|password
operator|=
name|super
operator|.
name|createPassword
argument_list|(
name|id
argument_list|)
expr_stmt|;
comment|// add new token to shared store
comment|// need to persist expiration along with password
name|info
operator|=
name|super
operator|.
name|currentTokens
operator|.
name|remove
argument_list|(
name|id
argument_list|)
expr_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to retrieve token after creation"
argument_list|)
throw|;
block|}
block|}
name|this
operator|.
name|tokenStore
operator|.
name|addToken
argument_list|(
name|id
argument_list|,
name|info
argument_list|)
expr_stmt|;
return|return
name|password
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|renewToken
parameter_list|(
name|Token
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
name|token
parameter_list|,
name|String
name|renewer
parameter_list|)
throws|throws
name|InvalidToken
throws|,
name|IOException
block|{
comment|// since renewal is KERBEROS authenticated token may not be cached
specifier|final
name|DelegationTokenIdentifier
name|id
init|=
name|getTokenIdentifier
argument_list|(
name|token
argument_list|)
decl_stmt|;
name|DelegationTokenInformation
name|tokenInfo
init|=
name|this
operator|.
name|tokenStore
operator|.
name|getToken
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokenInfo
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|InvalidToken
argument_list|(
literal|"token does not exist: "
operator|+
name|id
argument_list|)
throw|;
comment|// no token found
block|}
comment|// ensure associated master key is available
if|if
condition|(
operator|!
name|super
operator|.
name|allKeys
operator|.
name|containsKey
argument_list|(
name|id
operator|.
name|getMasterKeyId
argument_list|()
argument_list|)
condition|)
block|{
name|LOGGER
operator|.
name|info
argument_list|(
literal|"Unknown master key (id={}), (re)loading keys from token store."
argument_list|,
name|id
operator|.
name|getMasterKeyId
argument_list|()
argument_list|)
expr_stmt|;
name|reloadKeys
argument_list|()
expr_stmt|;
block|}
comment|// reuse super renewal logic
synchronized|synchronized
init|(
name|this
init|)
block|{
name|super
operator|.
name|currentTokens
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|tokenInfo
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|super
operator|.
name|renewToken
argument_list|(
name|token
argument_list|,
name|renewer
argument_list|)
return|;
block|}
finally|finally
block|{
name|super
operator|.
name|currentTokens
operator|.
name|remove
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|String
name|encodeWritable
parameter_list|(
name|Writable
name|key
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteArrayOutputStream
name|bos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|dos
init|=
operator|new
name|DataOutputStream
argument_list|(
name|bos
argument_list|)
decl_stmt|;
name|key
operator|.
name|write
argument_list|(
name|dos
argument_list|)
expr_stmt|;
name|dos
operator|.
name|flush
argument_list|()
expr_stmt|;
return|return
name|Base64
operator|.
name|encodeBase64URLSafeString
argument_list|(
name|bos
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|decodeWritable
parameter_list|(
name|Writable
name|w
parameter_list|,
name|String
name|idStr
parameter_list|)
throws|throws
name|IOException
block|{
name|DataInputStream
name|in
init|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|Base64
operator|.
name|decodeBase64
argument_list|(
name|idStr
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|w
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
comment|/**    * Synchronize master key updates / sequence generation for multiple nodes.    * NOTE: {@link AbstractDelegationTokenSecretManager} keeps currentKey private, so we need    * to utilize this "hook" to manipulate the key through the object reference.    * This .20S workaround should cease to exist when Hadoop supports token store.    */
annotation|@
name|Override
specifier|protected
name|void
name|logUpdateMasterKey
parameter_list|(
name|DelegationKey
name|key
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|keySeq
init|=
name|this
operator|.
name|tokenStore
operator|.
name|addMasterKey
argument_list|(
name|encodeWritable
argument_list|(
name|key
argument_list|)
argument_list|)
decl_stmt|;
comment|// update key with assigned identifier
name|DelegationKey
name|keyWithSeq
init|=
operator|new
name|DelegationKey
argument_list|(
name|keySeq
argument_list|,
name|key
operator|.
name|getExpiryDate
argument_list|()
argument_list|,
name|key
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|keyStr
init|=
name|encodeWritable
argument_list|(
name|keyWithSeq
argument_list|)
decl_stmt|;
name|this
operator|.
name|tokenStore
operator|.
name|updateMasterKey
argument_list|(
name|keySeq
argument_list|,
name|keyStr
argument_list|)
expr_stmt|;
name|decodeWritable
argument_list|(
name|key
argument_list|,
name|keyStr
argument_list|)
expr_stmt|;
name|LOGGER
operator|.
name|info
argument_list|(
literal|"New master key with key id={}"
argument_list|,
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|logUpdateMasterKey
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|startThreads
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
comment|// updateCurrentKey needs to be called to initialize the master key
comment|// (there should be a null check added in the future in rollMasterKey)
comment|// updateCurrentKey();
name|Method
name|m
init|=
name|AbstractDelegationTokenSecretManager
operator|.
name|class
operator|.
name|getDeclaredMethod
argument_list|(
literal|"updateCurrentKey"
argument_list|)
decl_stmt|;
name|m
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|m
operator|.
name|invoke
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to initialize master key"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|running
operator|=
literal|true
expr_stmt|;
name|tokenRemoverThread
operator|=
operator|new
name|Daemon
argument_list|(
operator|new
name|ExpiredTokenRemover
argument_list|()
argument_list|)
expr_stmt|;
name|tokenRemoverThread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|stopThreads
parameter_list|()
block|{
if|if
condition|(
name|LOGGER
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOGGER
operator|.
name|debug
argument_list|(
literal|"Stopping expired delegation token remover thread"
argument_list|)
expr_stmt|;
block|}
name|running
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|tokenRemoverThread
operator|!=
literal|null
condition|)
block|{
name|tokenRemoverThread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Remove expired tokens. Replaces logic in {@link AbstractDelegationTokenSecretManager}    * that cannot be reused due to private method access. Logic here can more efficiently    * deal with external token store by only loading into memory the minimum data needed.    */
specifier|protected
name|void
name|removeExpiredTokens
parameter_list|()
block|{
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
name|i
init|=
name|tokenStore
operator|.
name|getAllDelegationTokenIdentifiers
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|i
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|DelegationTokenIdentifier
name|id
init|=
name|i
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|now
operator|>
name|id
operator|.
name|getMaxDate
argument_list|()
condition|)
block|{
name|this
operator|.
name|tokenStore
operator|.
name|removeToken
argument_list|(
name|id
argument_list|)
expr_stmt|;
comment|// no need to look at token info
block|}
else|else
block|{
comment|// get token info to check renew date
name|DelegationTokenInformation
name|tokenInfo
init|=
name|tokenStore
operator|.
name|getToken
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokenInfo
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|now
operator|>
name|tokenInfo
operator|.
name|getRenewDate
argument_list|()
condition|)
block|{
name|this
operator|.
name|tokenStore
operator|.
name|removeToken
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|/**    * Extension of rollMasterKey to remove expired keys from store.    *    * @throws IOException    */
specifier|protected
name|void
name|rollMasterKeyExt
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|Integer
argument_list|,
name|DelegationKey
argument_list|>
name|keys
init|=
name|reloadKeys
argument_list|()
decl_stmt|;
name|int
name|currentKeyId
init|=
name|super
operator|.
name|currentId
decl_stmt|;
name|HiveDelegationTokenSupport
operator|.
name|rollMasterKey
argument_list|(
name|TokenStoreDelegationTokenSecretManager
operator|.
name|this
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|DelegationKey
argument_list|>
name|keysAfterRoll
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|getAllKeys
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|DelegationKey
name|key
range|:
name|keysAfterRoll
control|)
block|{
name|keys
operator|.
name|remove
argument_list|(
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|key
operator|.
name|getKeyId
argument_list|()
operator|==
name|currentKeyId
condition|)
block|{
name|tokenStore
operator|.
name|updateMasterKey
argument_list|(
name|currentKeyId
argument_list|,
name|encodeWritable
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|DelegationKey
name|expiredKey
range|:
name|keys
operator|.
name|values
argument_list|()
control|)
block|{
name|LOGGER
operator|.
name|info
argument_list|(
literal|"Removing expired key id={}"
argument_list|,
name|expiredKey
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|tokenStore
operator|.
name|removeMasterKey
argument_list|(
name|expiredKey
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
literal|"Error removing expired key id={}"
argument_list|,
name|expiredKey
operator|.
name|getKeyId
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Cloned from {@link AbstractDelegationTokenSecretManager} to deal with private access    * restriction (there would not be an need to clone the remove thread if the remove logic was    * protected/extensible).    */
specifier|protected
class|class
name|ExpiredTokenRemover
extends|extends
name|Thread
block|{
specifier|private
name|long
name|lastMasterKeyUpdate
decl_stmt|;
specifier|private
name|long
name|lastTokenCacheCleanup
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|LOGGER
operator|.
name|info
argument_list|(
literal|"Starting expired delegation token remover thread, "
operator|+
literal|"tokenRemoverScanInterval="
operator|+
name|tokenRemoverScanInterval
operator|/
operator|(
literal|60
operator|*
literal|1000
operator|)
operator|+
literal|" min(s)"
argument_list|)
expr_stmt|;
while|while
condition|(
name|running
condition|)
block|{
try|try
block|{
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastMasterKeyUpdate
operator|+
name|keyUpdateInterval
operator|<
name|now
condition|)
block|{
try|try
block|{
name|rollMasterKeyExt
argument_list|()
expr_stmt|;
name|lastMasterKeyUpdate
operator|=
name|now
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
literal|"Master key updating failed. "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|lastTokenCacheCleanup
operator|+
name|tokenRemoverScanInterval
operator|<
name|now
condition|)
block|{
name|removeExpiredTokens
argument_list|()
expr_stmt|;
name|lastTokenCacheCleanup
operator|=
name|now
expr_stmt|;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
comment|// 5 seconds
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
literal|"InterruptedException received for ExpiredTokenRemover thread "
operator|+
name|ie
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
literal|"ExpiredTokenRemover thread received unexpected exception. "
operator|+
name|t
argument_list|,
name|t
argument_list|)
expr_stmt|;
comment|// Wait 5 seconds too in case of an exception, so we do not end up in busy waiting for
comment|// the solution for this exception
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
comment|// 5 seconds
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
literal|"InterruptedException received for ExpiredTokenRemover thread during "
operator|+
literal|"wait in exception sleep "
operator|+
name|ie
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

