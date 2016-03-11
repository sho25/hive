begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
specifier|public
name|SecretManager
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|checkForZKDTSMBug
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|// Workaround for HADOOP-12659 - remove when Hadoop 2.7.X is no longer supported.
specifier|private
name|void
name|checkForZKDTSMBug
parameter_list|(
name|Configuration
name|conf
parameter_list|)
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
specifier|public
specifier|static
name|SecretManager
name|createSecretManager
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
name|String
name|llapPrincipal
parameter_list|,
name|String
name|llapKeytab
parameter_list|)
block|{
comment|// Create ZK connection under a separate ugi (if specified) - ZK works in mysterious ways.
name|UserGroupInformation
name|zkUgi
init|=
literal|null
decl_stmt|;
name|String
name|principal
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_ZKSM_KERBEROS_PRINCIPAL
argument_list|,
name|llapPrincipal
argument_list|)
decl_stmt|;
name|String
name|keyTab
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_ZKSM_KERBEROS_KEYTAB_FILE
argument_list|,
name|llapKeytab
argument_list|)
decl_stmt|;
try|try
block|{
name|zkUgi
operator|=
name|LlapSecurityHelper
operator|.
name|loginWithKerberos
argument_list|(
name|principal
argument_list|,
name|keyTab
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
name|zkConf
operator|.
name|set
argument_list|(
name|SecretManager
operator|.
name|ZK_DTSM_ZK_KERBEROS_PRINCIPAL
argument_list|,
name|principal
argument_list|)
expr_stmt|;
name|zkConf
operator|.
name|set
argument_list|(
name|SecretManager
operator|.
name|ZK_DTSM_ZK_KERBEROS_KEYTAB
argument_list|,
name|keyTab
argument_list|)
expr_stmt|;
name|setZkConfIfNotSet
argument_list|(
name|zkConf
argument_list|,
name|SecretManager
operator|.
name|ZK_DTSM_ZNODE_WORKING_PATH
argument_list|,
literal|"llapzkdtsm"
argument_list|)
expr_stmt|;
name|setZkConfIfNotSet
argument_list|(
name|zkConf
argument_list|,
name|SecretManager
operator|.
name|ZK_DTSM_ZK_AUTH_TYPE
argument_list|,
literal|"sasl"
argument_list|)
expr_stmt|;
name|setZkConfIfNotSet
argument_list|(
name|zkConf
argument_list|,
name|SecretManager
operator|.
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
return|return
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
name|zkConf
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
block|}
end_class

end_unit

