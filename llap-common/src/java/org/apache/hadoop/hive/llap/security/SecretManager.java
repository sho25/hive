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
name|ByteArrayInputStream
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
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
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|ensemble
operator|.
name|fixed
operator|.
name|FixedEnsembleProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|framework
operator|.
name|CuratorFramework
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|framework
operator|.
name|CuratorFrameworkFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|retry
operator|.
name|RetryOneTime
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|LlapUtil
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
name|Text
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
name|SecurityUtil
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
name|security
operator|.
name|token
operator|.
name|delegation
operator|.
name|ZKDelegationTokenSecretManager
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
name|web
operator|.
name|DelegationTokenManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|ACL
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|Id
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

begin_class
specifier|public
class|class
name|SecretManager
extends|extends
name|ZKDelegationTokenSecretManager
argument_list|<
name|LlapTokenIdentifier
argument_list|>
implements|implements
name|SigningSecretManager
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
name|SecretManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DISABLE_MESSAGE
init|=
literal|"Set "
operator|+
name|ConfVars
operator|.
name|LLAP_VALIDATE_ACLS
operator|.
name|varname
operator|+
literal|" to false to disable ACL validation (note"
operator|+
literal|" that invalid ACLs on secret key paths would mean that security is compromised)"
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|String
name|clusterId
decl_stmt|;
specifier|public
name|SecretManager
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|clusterId
parameter_list|)
block|{
name|super
argument_list|(
name|validateConfigBeforeCtor
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterId
operator|=
name|clusterId
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|checkForZKDTSMBug
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|Configuration
name|validateConfigBeforeCtor
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|setCurator
argument_list|(
literal|null
argument_list|)
expr_stmt|;
comment|// Ensure there's no threadlocal. We don't expect one.
comment|// We don't ever want to create key paths with world visibility. Why is that even an option?!!
name|String
name|authType
init|=
name|conf
operator|.
name|get
argument_list|(
name|ZK_DTSM_ZK_AUTH_TYPE
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
literal|"sasl"
operator|.
name|equals
argument_list|(
name|authType
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Inconsistent configuration: secure cluster, but ZK auth is "
operator|+
name|authType
operator|+
literal|" instead of sasl"
argument_list|)
throw|;
block|}
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|startThreads
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|principalUser
init|=
name|LlapUtil
operator|.
name|getUserNameFromPrincipal
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|SecretManager
operator|.
name|ZK_DTSM_ZK_KERBEROS_PRINCIPAL
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting ZK threads as user "
operator|+
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
operator|+
literal|"; kerberos principal is configured for user (short user name) "
operator|+
name|principalUser
argument_list|)
expr_stmt|;
name|super
operator|.
name|startThreads
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_VALIDATE_ACLS
argument_list|)
operator|||
operator|!
name|UserGroupInformation
operator|.
name|isSecurityEnabled
argument_list|()
condition|)
return|return;
name|String
name|path
init|=
name|conf
operator|.
name|get
argument_list|(
name|ZK_DTSM_ZNODE_WORKING_PATH
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|path
operator|==
literal|null
condition|)
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Path was not set in config"
argument_list|)
throw|;
name|checkRootAcls
argument_list|(
name|conf
argument_list|,
name|path
argument_list|,
name|principalUser
argument_list|)
expr_stmt|;
block|}
comment|// Workaround for HADOOP-12659 - remove when Hadoop 2.7.X is no longer supported.
specifier|private
name|void
name|checkForZKDTSMBug
parameter_list|()
block|{
comment|// There's a bug in ZKDelegationTokenSecretManager ctor where seconds are not converted to ms.
name|long
name|expectedRenewTimeSec
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|DelegationTokenManager
operator|.
name|RENEW_INTERVAL
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Checking for tokenRenewInterval bug: "
operator|+
name|expectedRenewTimeSec
argument_list|)
expr_stmt|;
if|if
condition|(
name|expectedRenewTimeSec
operator|==
operator|-
literal|1
condition|)
return|return;
comment|// The default works, no bug.
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
name|f
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|c
init|=
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
name|class
decl_stmt|;
name|f
operator|=
name|c
operator|.
name|getDeclaredField
argument_list|(
literal|"tokenRenewInterval"
argument_list|)
expr_stmt|;
name|f
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// Maybe someone removed the field; probably ok to ignore.
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to check for tokenRenewInterval bug, hoping for the best"
argument_list|,
name|t
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|long
name|realValue
init|=
name|f
operator|.
name|getLong
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|long
name|expectedValue
init|=
name|expectedRenewTimeSec
operator|*
literal|1000
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"tokenRenewInterval is: "
operator|+
name|realValue
operator|+
literal|" (expected "
operator|+
name|expectedValue
operator|+
literal|")"
argument_list|)
expr_stmt|;
if|if
condition|(
name|realValue
operator|==
name|expectedRenewTimeSec
condition|)
block|{
comment|// Bug - the field has to be in ms, not sec. Override only if set precisely to sec.
name|f
operator|.
name|setLong
argument_list|(
name|this
argument_list|,
name|expectedValue
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to address tokenRenewInterval bug"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|LlapTokenIdentifier
name|createIdentifier
parameter_list|()
block|{
return|return
operator|new
name|LlapTokenIdentifier
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|LlapTokenIdentifier
name|decodeTokenIdentifier
parameter_list|(
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|token
parameter_list|)
throws|throws
name|IOException
block|{
name|DataInputStream
name|dis
init|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|token
operator|.
name|getIdentifier
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|LlapTokenIdentifier
name|id
init|=
operator|new
name|LlapTokenIdentifier
argument_list|()
decl_stmt|;
name|id
operator|.
name|readFields
argument_list|(
name|dis
argument_list|)
expr_stmt|;
name|dis
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|id
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|DelegationKey
name|getCurrentKey
parameter_list|()
throws|throws
name|IOException
block|{
name|DelegationKey
name|currentKey
init|=
name|getDelegationKey
argument_list|(
name|getCurrentKeyId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentKey
operator|!=
literal|null
condition|)
return|return
name|currentKey
return|;
comment|// Try to roll the key if none is found.
name|HiveDelegationTokenSupport
operator|.
name|rollMasterKey
argument_list|(
name|this
argument_list|)
expr_stmt|;
return|return
name|getDelegationKey
argument_list|(
name|getCurrentKeyId
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|signWithKey
parameter_list|(
name|byte
index|[]
name|message
parameter_list|,
name|DelegationKey
name|key
parameter_list|)
block|{
return|return
name|createPassword
argument_list|(
name|message
argument_list|,
name|key
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|signWithKey
parameter_list|(
name|byte
index|[]
name|message
parameter_list|,
name|int
name|keyId
parameter_list|)
throws|throws
name|SecurityException
block|{
name|DelegationKey
name|key
init|=
name|getDelegationKey
argument_list|(
name|keyId
argument_list|)
decl_stmt|;
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SecurityException
argument_list|(
literal|"The key ID "
operator|+
name|keyId
operator|+
literal|" was not found"
argument_list|)
throw|;
block|}
return|return
name|createPassword
argument_list|(
name|message
argument_list|,
name|key
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
specifier|static
specifier|final
class|class
name|LlapZkConf
block|{
specifier|public
name|Configuration
name|zkConf
decl_stmt|;
specifier|public
name|UserGroupInformation
name|zkUgi
decl_stmt|;
specifier|public
name|LlapZkConf
parameter_list|(
name|Configuration
name|zkConf
parameter_list|,
name|UserGroupInformation
name|zkUgi
parameter_list|)
block|{
name|this
operator|.
name|zkConf
operator|=
name|zkConf
expr_stmt|;
name|this
operator|.
name|zkUgi
operator|=
name|zkUgi
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|LlapZkConf
name|createLlapZkConf
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|llapPrincipal
parameter_list|,
name|String
name|llapKeytab
parameter_list|,
name|String
name|clusterId
parameter_list|)
block|{
comment|// Override the default delegation token lifetime for LLAP.
comment|// Also set all the necessary ZK settings to defaults and LLAP configs, if not set.
specifier|final
name|Configuration
name|zkConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|long
name|tokenLifetime
init|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DELEGATION_TOKEN_LIFETIME
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|zkConf
operator|.
name|setLong
argument_list|(
name|DelegationTokenManager
operator|.
name|MAX_LIFETIME
argument_list|,
name|tokenLifetime
argument_list|)
expr_stmt|;
name|zkConf
operator|.
name|setLong
argument_list|(
name|DelegationTokenManager
operator|.
name|RENEW_INTERVAL
argument_list|,
name|tokenLifetime
argument_list|)
expr_stmt|;
try|try
block|{
name|zkConf
operator|.
name|set
argument_list|(
name|ZK_DTSM_ZK_KERBEROS_PRINCIPAL
argument_list|,
name|SecurityUtil
operator|.
name|getServerPrincipal
argument_list|(
name|llapPrincipal
argument_list|,
literal|"0.0.0.0"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|zkConf
operator|.
name|set
argument_list|(
name|ZK_DTSM_ZK_KERBEROS_KEYTAB
argument_list|,
name|llapKeytab
argument_list|)
expr_stmt|;
name|String
name|zkPath
init|=
literal|"zkdtsm_"
operator|+
name|clusterId
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Using {} as ZK secret manager path"
argument_list|,
name|zkPath
argument_list|)
expr_stmt|;
name|zkConf
operator|.
name|set
argument_list|(
name|ZK_DTSM_ZNODE_WORKING_PATH
argument_list|,
name|zkPath
argument_list|)
expr_stmt|;
comment|// Hardcode SASL here. ZKDTSM only supports none or sasl and we never want none.
name|zkConf
operator|.
name|set
argument_list|(
name|ZK_DTSM_ZK_AUTH_TYPE
argument_list|,
literal|"sasl"
argument_list|)
expr_stmt|;
name|long
name|sessionTimeoutMs
init|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|zkConf
argument_list|,
name|ConfVars
operator|.
name|LLAP_ZKSM_ZK_SESSION_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|long
name|newRetryCount
init|=
operator|(
name|ZK_DTSM_ZK_NUM_RETRIES_DEFAULT
operator|*
name|sessionTimeoutMs
operator|)
operator|/
name|ZK_DTSM_ZK_SESSION_TIMEOUT_DEFAULT
decl_stmt|;
name|long
name|connTimeoutMs
init|=
name|Math
operator|.
name|max
argument_list|(
name|sessionTimeoutMs
argument_list|,
name|ZK_DTSM_ZK_CONNECTION_TIMEOUT_DEFAULT
argument_list|)
decl_stmt|;
name|zkConf
operator|.
name|set
argument_list|(
name|ZK_DTSM_ZK_SESSION_TIMEOUT
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|sessionTimeoutMs
argument_list|)
argument_list|)
expr_stmt|;
name|zkConf
operator|.
name|set
argument_list|(
name|ZK_DTSM_ZK_CONNECTION_TIMEOUT
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|connTimeoutMs
argument_list|)
argument_list|)
expr_stmt|;
name|zkConf
operator|.
name|set
argument_list|(
name|ZK_DTSM_ZK_NUM_RETRIES
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|newRetryCount
argument_list|)
argument_list|)
expr_stmt|;
name|setZkConfIfNotSet
argument_list|(
name|zkConf
argument_list|,
name|ZK_DTSM_ZK_CONNECTION_STRING
argument_list|,
name|HiveConf
operator|.
name|getVar
argument_list|(
name|zkConf
argument_list|,
name|ConfVars
operator|.
name|LLAP_ZKSM_ZK_CONNECTION_STRING
argument_list|)
argument_list|)
expr_stmt|;
name|UserGroupInformation
name|zkUgi
init|=
literal|null
decl_stmt|;
try|try
block|{
name|zkUgi
operator|=
name|LlapUtil
operator|.
name|loginWithKerberos
argument_list|(
name|llapPrincipal
argument_list|,
name|llapKeytab
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
operator|new
name|LlapZkConf
argument_list|(
name|zkConf
argument_list|,
name|zkUgi
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|SecretManager
name|createSecretManager
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|clusterId
parameter_list|)
block|{
name|String
name|llapPrincipal
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_KERBEROS_PRINCIPAL
argument_list|)
decl_stmt|,
name|llapKeytab
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_KERBEROS_KEYTAB_FILE
argument_list|)
decl_stmt|;
return|return
name|createSecretManager
argument_list|(
name|conf
argument_list|,
name|llapPrincipal
argument_list|,
name|llapKeytab
argument_list|,
name|clusterId
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|SecretManager
name|createSecretManager
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|llapPrincipal
parameter_list|,
name|String
name|llapKeytab
parameter_list|,
specifier|final
name|String
name|clusterId
parameter_list|)
block|{
assert|assert
name|UserGroupInformation
operator|.
name|isSecurityEnabled
argument_list|()
assert|;
specifier|final
name|LlapZkConf
name|c
init|=
name|createLlapZkConf
argument_list|(
name|conf
argument_list|,
name|llapPrincipal
argument_list|,
name|llapKeytab
argument_list|,
name|clusterId
argument_list|)
decl_stmt|;
return|return
name|c
operator|.
name|zkUgi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|SecretManager
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|SecretManager
name|run
parameter_list|()
block|{
name|SecretManager
name|zkSecretManager
init|=
operator|new
name|SecretManager
argument_list|(
name|c
operator|.
name|zkConf
argument_list|,
name|clusterId
argument_list|)
decl_stmt|;
try|try
block|{
name|zkSecretManager
operator|.
name|startThreads
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|zkSecretManager
return|;
block|}
block|}
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|void
name|setZkConfIfNotSet
parameter_list|(
name|Configuration
name|zkConf
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|zkConf
operator|.
name|get
argument_list|(
name|name
argument_list|)
operator|!=
literal|null
condition|)
return|return;
name|zkConf
operator|.
name|set
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|createLlapToken
parameter_list|(
name|String
name|appId
parameter_list|,
name|String
name|user
parameter_list|,
name|boolean
name|isSignatureRequired
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
name|realUser
init|=
literal|null
decl_stmt|,
name|renewer
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|user
operator|==
literal|null
condition|)
block|{
name|UserGroupInformation
name|ugi
init|=
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
decl_stmt|;
name|user
operator|=
name|ugi
operator|.
name|getUserName
argument_list|()
expr_stmt|;
if|if
condition|(
name|ugi
operator|.
name|getRealUser
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|realUser
operator|=
operator|new
name|Text
argument_list|(
name|ugi
operator|.
name|getRealUser
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|renewer
operator|=
operator|new
name|Text
argument_list|(
name|ugi
operator|.
name|getShortUserName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|renewer
operator|=
operator|new
name|Text
argument_list|(
name|user
argument_list|)
expr_stmt|;
block|}
name|LlapTokenIdentifier
name|llapId
init|=
operator|new
name|LlapTokenIdentifier
argument_list|(
operator|new
name|Text
argument_list|(
name|user
argument_list|)
argument_list|,
name|renewer
argument_list|,
name|realUser
argument_list|,
name|clusterId
argument_list|,
name|appId
argument_list|,
name|isSignatureRequired
argument_list|)
decl_stmt|;
comment|// TODO: note that the token is not renewable right now and will last for 2 weeks by default.
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|token
init|=
operator|new
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
argument_list|(
name|llapId
argument_list|,
name|this
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
literal|"Created LLAP token {}"
argument_list|,
name|token
argument_list|)
expr_stmt|;
block|}
return|return
name|token
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|stopThreads
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|checkRootAcls
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|path
parameter_list|,
name|String
name|user
parameter_list|)
block|{
name|int
name|stime
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|ZK_DTSM_ZK_SESSION_TIMEOUT
argument_list|,
name|ZK_DTSM_ZK_SESSION_TIMEOUT_DEFAULT
argument_list|)
decl_stmt|,
name|ctime
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|ZK_DTSM_ZK_CONNECTION_TIMEOUT
argument_list|,
name|ZK_DTSM_ZK_CONNECTION_TIMEOUT_DEFAULT
argument_list|)
decl_stmt|;
name|CuratorFramework
name|zkClient
init|=
name|CuratorFrameworkFactory
operator|.
name|builder
argument_list|()
operator|.
name|namespace
argument_list|(
literal|null
argument_list|)
operator|.
name|retryPolicy
argument_list|(
operator|new
name|RetryOneTime
argument_list|(
literal|10
argument_list|)
argument_list|)
operator|.
name|sessionTimeoutMs
argument_list|(
name|stime
argument_list|)
operator|.
name|connectionTimeoutMs
argument_list|(
name|ctime
argument_list|)
operator|.
name|ensembleProvider
argument_list|(
operator|new
name|FixedEnsembleProvider
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|ZK_DTSM_ZK_CONNECTION_STRING
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// Hardcoded from a private field in ZKDelegationTokenSecretManager.
comment|// We need to check the path under what it sets for namespace, since the namespace is
comment|// created with world ACLs.
name|String
name|nsPath
init|=
literal|"/"
operator|+
name|path
operator|+
literal|"/ZKDTSMRoot"
decl_stmt|;
name|Id
name|currentUser
init|=
operator|new
name|Id
argument_list|(
literal|"sasl"
argument_list|,
name|user
argument_list|)
decl_stmt|;
try|try
block|{
name|zkClient
operator|.
name|start
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|children
init|=
name|zkClient
operator|.
name|getChildren
argument_list|()
operator|.
name|forPath
argument_list|(
name|nsPath
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|child
range|:
name|children
control|)
block|{
name|String
name|childPath
init|=
name|nsPath
operator|+
literal|"/"
operator|+
name|child
decl_stmt|;
name|checkAcls
argument_list|(
name|zkClient
argument_list|,
name|currentUser
argument_list|,
name|childPath
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|zkClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|checkAcls
parameter_list|(
name|CuratorFramework
name|zkClient
parameter_list|,
name|Id
name|user
parameter_list|,
name|String
name|path
parameter_list|)
block|{
name|List
argument_list|<
name|ACL
argument_list|>
name|acls
init|=
literal|null
decl_stmt|;
try|try
block|{
name|acls
operator|=
name|zkClient
operator|.
name|getACL
argument_list|()
operator|.
name|forPath
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error during the ACL check. "
operator|+
name|DISABLE_MESSAGE
argument_list|,
name|ex
argument_list|)
throw|;
block|}
if|if
condition|(
name|acls
operator|==
literal|null
operator|||
name|acls
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// There's some access (to get ACLs), so assume it means free for all.
throw|throw
operator|new
name|SecurityException
argument_list|(
literal|"No ACLs on "
operator|+
name|path
operator|+
literal|". "
operator|+
name|DISABLE_MESSAGE
argument_list|)
throw|;
block|}
for|for
control|(
name|ACL
name|acl
range|:
name|acls
control|)
block|{
if|if
condition|(
operator|!
name|user
operator|.
name|equals
argument_list|(
name|acl
operator|.
name|getId
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SecurityException
argument_list|(
literal|"The ACL "
operator|+
name|acl
operator|+
literal|" is unnacceptable for "
operator|+
name|path
operator|+
literal|"; only "
operator|+
name|user
operator|+
literal|" is allowed. "
operator|+
name|DISABLE_MESSAGE
argument_list|)
throw|;
block|}
block|}
block|}
comment|/** Verifies the token available as serialized bytes. */
specifier|public
name|void
name|verifyToken
parameter_list|(
name|byte
index|[]
name|tokenBytes
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
return|return;
if|if
condition|(
name|tokenBytes
operator|==
literal|null
condition|)
throw|throw
operator|new
name|SecurityException
argument_list|(
literal|"Token required for authentication"
argument_list|)
throw|;
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
name|token
operator|.
name|readFields
argument_list|(
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|tokenBytes
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|verifyToken
argument_list|(
name|token
operator|.
name|decodeIdentifier
argument_list|()
argument_list|,
name|token
operator|.
name|getPassword
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

