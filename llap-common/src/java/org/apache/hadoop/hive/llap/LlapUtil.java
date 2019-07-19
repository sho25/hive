begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  *  you may not use this file except in compliance with the License.  *  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ThreadMXBean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
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
name|HashMap
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
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|fs
operator|.
name|CommonConfigurationKeysPublic
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|fs
operator|.
name|PathFilter
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
name|io
operator|.
name|DataInputBuffer
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
name|ipc
operator|.
name|ProtobufRpcEngine
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
name|ipc
operator|.
name|RPC
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
name|Credentials
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
name|authorize
operator|.
name|PolicyProvider
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
name|SecretManager
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
name|BlockingService
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_class
specifier|public
class|class
name|LlapUtil
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
name|LlapUtil
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|String
name|getDaemonLocalDirString
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|workDirsEnvString
parameter_list|)
block|{
name|String
name|localDirList
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_WORK_DIRS
argument_list|)
decl_stmt|;
if|if
condition|(
name|localDirList
operator|!=
literal|null
operator|&&
operator|!
name|localDirList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Local dirs from Configuration: {}"
argument_list|,
name|localDirList
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|localDirList
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"useYarnEnvDirs"
argument_list|)
operator|&&
operator|!
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|localDirList
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Using local dirs from Configuration"
argument_list|)
expr_stmt|;
return|return
name|localDirList
return|;
block|}
block|}
comment|// Fallback to picking up the value from environment.
if|if
condition|(
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|workDirsEnvString
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Using local dirs from environment: {}"
argument_list|,
name|workDirsEnvString
argument_list|)
expr_stmt|;
return|return
name|workDirsEnvString
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot determined local dirs from specified configuration and env. ValueFromConf="
operator|+
name|localDirList
operator|+
literal|", ValueFromEnv="
operator|+
name|workDirsEnvString
argument_list|)
throw|;
block|}
block|}
comment|/**    * Login using kerberos. But does not change the current logged in user.    *    * @param principal  - kerberos principal    * @param keytabFile - keytab file    * @return UGI    * @throws IOException - if keytab file cannot be found    */
specifier|public
specifier|static
name|UserGroupInformation
name|loginWithKerberos
parameter_list|(
name|String
name|principal
parameter_list|,
name|String
name|keytabFile
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
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|principal
operator|==
literal|null
operator|||
name|principal
operator|.
name|isEmpty
argument_list|()
operator|||
name|keytabFile
operator|==
literal|null
operator|||
name|keytabFile
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Kerberos principal and/or keytab are null or empty"
argument_list|)
throw|;
block|}
specifier|final
name|String
name|serverPrincipal
init|=
name|SecurityUtil
operator|.
name|getServerPrincipal
argument_list|(
name|principal
argument_list|,
literal|"0.0.0.0"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Logging in as "
operator|+
name|serverPrincipal
operator|+
literal|" via "
operator|+
name|keytabFile
argument_list|)
expr_stmt|;
return|return
name|UserGroupInformation
operator|.
name|loginUserFromKeytabAndReturnUGI
argument_list|(
name|serverPrincipal
argument_list|,
name|keytabFile
argument_list|)
return|;
block|}
comment|/**    * Login using kerberos and also updates the current logged in user    *    * @param principal  - kerberos principal    * @param keytabFile - keytab file    * @throws IOException - if keytab file cannot be found    */
specifier|public
specifier|static
name|void
name|loginWithKerberosAndUpdateCurrentUser
parameter_list|(
name|String
name|principal
parameter_list|,
name|String
name|keytabFile
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
block|{
return|return;
block|}
if|if
condition|(
name|principal
operator|==
literal|null
operator|||
name|principal
operator|.
name|isEmpty
argument_list|()
operator|||
name|keytabFile
operator|==
literal|null
operator|||
name|keytabFile
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Kerberos principal and/or keytab is null or empty"
argument_list|)
throw|;
block|}
specifier|final
name|String
name|serverPrincipal
init|=
name|SecurityUtil
operator|.
name|getServerPrincipal
argument_list|(
name|principal
argument_list|,
literal|"0.0.0.0"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Logging in as "
operator|+
name|serverPrincipal
operator|+
literal|" via "
operator|+
name|keytabFile
operator|+
literal|" and updating current logged in user"
argument_list|)
expr_stmt|;
name|UserGroupInformation
operator|.
name|loginUserFromKeytab
argument_list|(
name|serverPrincipal
argument_list|,
name|keytabFile
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|final
specifier|static
name|Pattern
name|hostsRe
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"[^A-Za-z0-9_-]"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|String
name|generateClusterName
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|hosts
init|=
name|HiveConf
operator|.
name|getTrimmedVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_SERVICE_HOSTS
argument_list|)
decl_stmt|;
return|return
name|hostsRe
operator|.
name|matcher
argument_list|(
name|hosts
operator|.
name|startsWith
argument_list|(
literal|"@"
argument_list|)
condition|?
name|hosts
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
else|:
name|hosts
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"_"
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getUserNameFromPrincipal
parameter_list|(
name|String
name|principal
parameter_list|)
block|{
comment|// Based on SecurityUtil.
if|if
condition|(
name|principal
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|String
index|[]
name|components
init|=
name|principal
operator|.
name|split
argument_list|(
literal|"[/@]"
argument_list|)
decl_stmt|;
return|return
operator|(
name|components
operator|==
literal|null
operator|||
name|components
operator|.
name|length
operator|!=
literal|3
operator|)
condition|?
name|principal
else|:
name|components
index|[
literal|0
index|]
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|StatisticsData
argument_list|>
name|getStatisticsForScheme
parameter_list|(
specifier|final
name|String
name|scheme
parameter_list|,
specifier|final
name|List
argument_list|<
name|StatisticsData
argument_list|>
name|stats
parameter_list|)
block|{
name|List
argument_list|<
name|StatisticsData
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|stats
operator|!=
literal|null
operator|&&
name|scheme
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|StatisticsData
name|s
range|:
name|stats
control|)
block|{
if|if
condition|(
name|s
operator|.
name|getScheme
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|scheme
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|FileSystem
operator|.
name|Statistics
argument_list|>
name|getCombinedFileSystemStatistics
parameter_list|()
block|{
specifier|final
name|List
argument_list|<
name|FileSystem
operator|.
name|Statistics
argument_list|>
name|allStats
init|=
name|FileSystem
operator|.
name|getAllStatistics
argument_list|()
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|FileSystem
operator|.
name|Statistics
argument_list|>
name|result
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileSystem
operator|.
name|Statistics
name|statistics
range|:
name|allStats
control|)
block|{
specifier|final
name|String
name|scheme
init|=
name|statistics
operator|.
name|getScheme
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|.
name|containsKey
argument_list|(
name|scheme
argument_list|)
condition|)
block|{
name|FileSystem
operator|.
name|Statistics
name|existing
init|=
name|result
operator|.
name|get
argument_list|(
name|scheme
argument_list|)
decl_stmt|;
name|FileSystem
operator|.
name|Statistics
name|combined
init|=
name|combineFileSystemStatistics
argument_list|(
name|existing
argument_list|,
name|statistics
argument_list|)
decl_stmt|;
name|result
operator|.
name|put
argument_list|(
name|scheme
argument_list|,
name|combined
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|put
argument_list|(
name|scheme
argument_list|,
name|statistics
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|private
specifier|static
name|FileSystem
operator|.
name|Statistics
name|combineFileSystemStatistics
parameter_list|(
specifier|final
name|FileSystem
operator|.
name|Statistics
name|s1
parameter_list|,
specifier|final
name|FileSystem
operator|.
name|Statistics
name|s2
parameter_list|)
block|{
name|FileSystem
operator|.
name|Statistics
name|result
init|=
operator|new
name|FileSystem
operator|.
name|Statistics
argument_list|(
name|s1
argument_list|)
decl_stmt|;
name|result
operator|.
name|incrementReadOps
argument_list|(
name|s2
operator|.
name|getReadOps
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|incrementLargeReadOps
argument_list|(
name|s2
operator|.
name|getLargeReadOps
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|incrementWriteOps
argument_list|(
name|s2
operator|.
name|getWriteOps
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|incrementBytesRead
argument_list|(
name|s2
operator|.
name|getBytesRead
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|incrementBytesWritten
argument_list|(
name|s2
operator|.
name|getBytesWritten
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|StatisticsData
argument_list|>
name|cloneThreadLocalFileSystemStatistics
parameter_list|()
block|{
name|List
argument_list|<
name|StatisticsData
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// thread local filesystem stats is private and cannot be cloned. So make a copy to new class
for|for
control|(
name|FileSystem
operator|.
name|Statistics
name|statistics
range|:
name|FileSystem
operator|.
name|getAllStatistics
argument_list|()
control|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|StatisticsData
argument_list|(
name|statistics
operator|.
name|getScheme
argument_list|()
argument_list|,
name|statistics
operator|.
name|getThreadStatistics
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
class|class
name|StatisticsData
block|{
name|long
name|bytesRead
decl_stmt|;
name|long
name|bytesWritten
decl_stmt|;
name|int
name|readOps
decl_stmt|;
name|int
name|largeReadOps
decl_stmt|;
name|int
name|writeOps
decl_stmt|;
name|String
name|scheme
decl_stmt|;
specifier|public
name|StatisticsData
parameter_list|(
name|String
name|scheme
parameter_list|,
name|FileSystem
operator|.
name|Statistics
operator|.
name|StatisticsData
name|fsStats
parameter_list|)
block|{
name|this
operator|.
name|scheme
operator|=
name|scheme
expr_stmt|;
name|this
operator|.
name|bytesRead
operator|=
name|fsStats
operator|.
name|getBytesRead
argument_list|()
expr_stmt|;
name|this
operator|.
name|bytesWritten
operator|=
name|fsStats
operator|.
name|getBytesWritten
argument_list|()
expr_stmt|;
name|this
operator|.
name|readOps
operator|=
name|fsStats
operator|.
name|getReadOps
argument_list|()
expr_stmt|;
name|this
operator|.
name|largeReadOps
operator|=
name|fsStats
operator|.
name|getLargeReadOps
argument_list|()
expr_stmt|;
name|this
operator|.
name|writeOps
operator|=
name|fsStats
operator|.
name|getWriteOps
argument_list|()
expr_stmt|;
block|}
specifier|public
name|long
name|getBytesRead
parameter_list|()
block|{
return|return
name|bytesRead
return|;
block|}
specifier|public
name|long
name|getBytesWritten
parameter_list|()
block|{
return|return
name|bytesWritten
return|;
block|}
specifier|public
name|int
name|getReadOps
parameter_list|()
block|{
return|return
name|readOps
return|;
block|}
specifier|public
name|int
name|getLargeReadOps
parameter_list|()
block|{
return|return
name|largeReadOps
return|;
block|}
specifier|public
name|int
name|getWriteOps
parameter_list|()
block|{
return|return
name|writeOps
return|;
block|}
specifier|public
name|String
name|getScheme
parameter_list|()
block|{
return|return
name|scheme
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" scheme: "
argument_list|)
operator|.
name|append
argument_list|(
name|scheme
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" bytesRead: "
argument_list|)
operator|.
name|append
argument_list|(
name|bytesRead
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" bytesWritten: "
argument_list|)
operator|.
name|append
argument_list|(
name|bytesWritten
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" readOps: "
argument_list|)
operator|.
name|append
argument_list|(
name|readOps
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" largeReadOps: "
argument_list|)
operator|.
name|append
argument_list|(
name|largeReadOps
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" writeOps: "
argument_list|)
operator|.
name|append
argument_list|(
name|writeOps
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
specifier|public
specifier|static
name|String
name|getAmHostNameFromAddress
parameter_list|(
name|InetSocketAddress
name|address
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
operator|!
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
name|LLAP_DAEMON_AM_USE_FQDN
argument_list|)
condition|)
block|{
return|return
name|address
operator|.
name|getHostName
argument_list|()
return|;
block|}
name|InetAddress
name|ia
init|=
name|address
operator|.
name|getAddress
argument_list|()
decl_stmt|;
comment|// getCanonicalHostName would either return FQDN, or an IP.
return|return
operator|(
name|ia
operator|==
literal|null
operator|)
condition|?
name|address
operator|.
name|getHostName
argument_list|()
else|:
name|ia
operator|.
name|getCanonicalHostName
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|String
name|humanReadableByteCount
parameter_list|(
name|long
name|bytes
parameter_list|)
block|{
name|int
name|unit
init|=
literal|1024
decl_stmt|;
if|if
condition|(
name|bytes
operator|<
name|unit
condition|)
block|{
return|return
name|bytes
operator|+
literal|"B"
return|;
block|}
name|int
name|exp
init|=
call|(
name|int
call|)
argument_list|(
name|Math
operator|.
name|log
argument_list|(
name|bytes
argument_list|)
operator|/
name|Math
operator|.
name|log
argument_list|(
name|unit
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|suffix
init|=
literal|"KMGTPE"
operator|.
name|charAt
argument_list|(
name|exp
operator|-
literal|1
argument_list|)
operator|+
literal|""
decl_stmt|;
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%.2f%sB"
argument_list|,
name|bytes
operator|/
name|Math
operator|.
name|pow
argument_list|(
name|unit
argument_list|,
name|exp
argument_list|)
argument_list|,
name|suffix
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|RPC
operator|.
name|Server
name|createRpcServer
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|pbProtocol
parameter_list|,
name|InetSocketAddress
name|addr
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|int
name|numHandlers
parameter_list|,
name|BlockingService
name|blockingService
parameter_list|,
name|SecretManager
argument_list|<
name|?
argument_list|>
name|secretManager
parameter_list|,
name|PolicyProvider
name|provider
parameter_list|,
name|ConfVars
modifier|...
name|aclVars
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|serverConf
init|=
name|conf
decl_stmt|;
name|boolean
name|isSecurityEnabled
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|CommonConfigurationKeysPublic
operator|.
name|HADOOP_SECURITY_AUTHORIZATION
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|isSecurityEnabled
condition|)
block|{
comment|// Enforce Hive defaults.
for|for
control|(
name|ConfVars
name|acl
range|:
name|aclVars
control|)
block|{
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|acl
operator|.
name|varname
argument_list|)
operator|!=
literal|null
condition|)
continue|continue;
comment|// Some value is set.
if|if
condition|(
name|serverConf
operator|==
name|conf
condition|)
block|{
name|serverConf
operator|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|serverConf
operator|.
name|set
argument_list|(
name|acl
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|getVar
argument_list|(
name|serverConf
argument_list|,
name|acl
argument_list|)
argument_list|)
expr_stmt|;
comment|// Set the default.
block|}
block|}
name|RPC
operator|.
name|setProtocolEngine
argument_list|(
name|serverConf
argument_list|,
name|pbProtocol
argument_list|,
name|ProtobufRpcEngine
operator|.
name|class
argument_list|)
expr_stmt|;
name|RPC
operator|.
name|Builder
name|builder
init|=
operator|new
name|RPC
operator|.
name|Builder
argument_list|(
name|serverConf
argument_list|)
operator|.
name|setProtocol
argument_list|(
name|pbProtocol
argument_list|)
operator|.
name|setInstance
argument_list|(
name|blockingService
argument_list|)
operator|.
name|setBindAddress
argument_list|(
name|addr
operator|.
name|getHostName
argument_list|()
argument_list|)
operator|.
name|setPort
argument_list|(
name|addr
operator|.
name|getPort
argument_list|()
argument_list|)
operator|.
name|setNumHandlers
argument_list|(
name|numHandlers
argument_list|)
decl_stmt|;
if|if
condition|(
name|secretManager
operator|!=
literal|null
condition|)
block|{
name|builder
operator|=
name|builder
operator|.
name|setSecretManager
argument_list|(
name|secretManager
argument_list|)
expr_stmt|;
block|}
name|RPC
operator|.
name|Server
name|server
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
if|if
condition|(
name|isSecurityEnabled
condition|)
block|{
name|server
operator|.
name|refreshServiceAcl
argument_list|(
name|serverConf
argument_list|,
name|provider
argument_list|)
expr_stmt|;
block|}
return|return
name|server
return|;
block|}
specifier|public
specifier|static
name|RPC
operator|.
name|Server
name|startProtocolServer
parameter_list|(
name|int
name|srvPort
parameter_list|,
name|int
name|numHandlers
parameter_list|,
name|AtomicReference
argument_list|<
name|InetSocketAddress
argument_list|>
name|bindAddress
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|BlockingService
name|impl
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|protocolClass
parameter_list|,
name|SecretManager
argument_list|<
name|?
argument_list|>
name|secretManager
parameter_list|,
name|PolicyProvider
name|provider
parameter_list|,
name|ConfVars
modifier|...
name|aclVars
parameter_list|)
block|{
name|InetSocketAddress
name|addr
init|=
operator|new
name|InetSocketAddress
argument_list|(
name|srvPort
argument_list|)
decl_stmt|;
name|RPC
operator|.
name|Server
name|server
decl_stmt|;
try|try
block|{
name|server
operator|=
name|createRpcServer
argument_list|(
name|protocolClass
argument_list|,
name|addr
argument_list|,
name|conf
argument_list|,
name|numHandlers
argument_list|,
name|impl
argument_list|,
name|secretManager
argument_list|,
name|provider
argument_list|,
name|aclVars
argument_list|)
expr_stmt|;
name|server
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to run RPC Server on port: "
operator|+
name|srvPort
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|InetSocketAddress
name|serverBindAddress
init|=
name|NetUtils
operator|.
name|getConnectAddress
argument_list|(
name|server
argument_list|)
decl_stmt|;
name|InetSocketAddress
name|bindAddressVal
init|=
name|NetUtils
operator|.
name|createSocketAddrForHost
argument_list|(
name|serverBindAddress
operator|.
name|getAddress
argument_list|()
operator|.
name|getCanonicalHostName
argument_list|()
argument_list|,
name|serverBindAddress
operator|.
name|getPort
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|bindAddress
operator|!=
literal|null
condition|)
block|{
name|bindAddress
operator|.
name|set
argument_list|(
name|bindAddressVal
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Instantiated "
operator|+
name|protocolClass
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" at "
operator|+
name|bindAddressVal
argument_list|)
expr_stmt|;
return|return
name|server
return|;
block|}
comment|// Copied from AcidUtils so we don't have to put the code using this into ql.
comment|// TODO: Ideally, AcidUtils class and various constants should be in common.
specifier|private
specifier|static
specifier|final
name|String
name|BASE_PREFIX
init|=
literal|"base_"
decl_stmt|,
name|DELTA_PREFIX
init|=
literal|"delta_"
decl_stmt|,
name|DELETE_DELTA_PREFIX
init|=
literal|"delete_delta_"
decl_stmt|,
name|BUCKET_PREFIX
init|=
literal|"bucket_"
decl_stmt|,
name|DATABASE_PATH_SUFFIX
init|=
literal|".db"
decl_stmt|,
name|UNION_SUDBIR_PREFIX
init|=
literal|"HIVE_UNION_SUBDIR_"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|char
name|DERIVED_ENTITY_PARTITION_SEPARATOR
init|=
literal|'/'
decl_stmt|;
specifier|public
specifier|static
name|String
name|getDbAndTableNameForMetrics
parameter_list|(
name|Path
name|path
parameter_list|,
name|boolean
name|includeParts
parameter_list|)
block|{
name|String
index|[]
name|parts
init|=
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|split
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
decl_stmt|;
name|int
name|dbIx
init|=
operator|-
literal|1
decl_stmt|;
comment|// Try to find the default db postfix; don't check two last components - at least there
comment|// should be a table and file (we could also try to throw away partition/bucket/acid stuff).
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|parts
operator|.
name|length
operator|-
literal|2
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|!
name|parts
index|[
name|i
index|]
operator|.
name|endsWith
argument_list|(
name|DATABASE_PATH_SUFFIX
argument_list|)
condition|)
continue|continue;
if|if
condition|(
name|dbIx
operator|>=
literal|0
condition|)
block|{
name|dbIx
operator|=
operator|-
literal|1
expr_stmt|;
comment|// Let's not guess which one is correct.
break|break;
block|}
name|dbIx
operator|=
name|i
expr_stmt|;
block|}
if|if
condition|(
name|dbIx
operator|>=
literal|0
condition|)
block|{
name|String
name|dbAndTable
init|=
name|parts
index|[
name|dbIx
index|]
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|parts
index|[
name|dbIx
index|]
operator|.
name|length
argument_list|()
operator|-
literal|3
argument_list|)
operator|+
literal|"."
operator|+
name|parts
index|[
name|dbIx
operator|+
literal|1
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|includeParts
condition|)
return|return
name|dbAndTable
return|;
for|for
control|(
name|int
name|i
init|=
name|dbIx
operator|+
literal|2
init|;
name|i
operator|<
name|parts
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|!
name|parts
index|[
name|i
index|]
operator|.
name|contains
argument_list|(
literal|"="
argument_list|)
condition|)
break|break;
name|dbAndTable
operator|+=
literal|"/"
operator|+
name|parts
index|[
name|i
index|]
expr_stmt|;
block|}
return|return
name|dbAndTable
return|;
block|}
comment|// Just go from the back and throw away everything we think is wrong; skip last item, the file.
name|boolean
name|isInPartFields
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|parts
operator|.
name|length
operator|-
literal|2
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
name|String
name|p
init|=
name|parts
index|[
name|i
index|]
decl_stmt|;
name|boolean
name|isPartField
init|=
name|p
operator|.
name|contains
argument_list|(
literal|"="
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|isInPartFields
operator|&&
operator|!
name|isPartField
operator|)
operator|||
operator|(
operator|!
name|isPartField
operator|&&
operator|!
name|isSomeHiveDir
argument_list|(
name|p
argument_list|)
operator|)
condition|)
block|{
name|dbIx
operator|=
name|i
operator|-
literal|1
expr_stmt|;
comment|// Assume this is the table we are at now.
break|break;
block|}
name|isInPartFields
operator|=
name|isPartField
expr_stmt|;
block|}
comment|// If we found something before we ran out of components, use it.
if|if
condition|(
name|dbIx
operator|>=
literal|0
condition|)
block|{
name|String
name|dbName
init|=
name|parts
index|[
name|dbIx
index|]
decl_stmt|;
if|if
condition|(
name|dbName
operator|.
name|endsWith
argument_list|(
name|DATABASE_PATH_SUFFIX
argument_list|)
condition|)
block|{
name|dbName
operator|=
name|dbName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|dbName
operator|.
name|length
argument_list|()
operator|-
literal|3
argument_list|)
expr_stmt|;
block|}
name|String
name|dbAndTable
init|=
name|dbName
operator|+
literal|"."
operator|+
name|parts
index|[
name|dbIx
operator|+
literal|1
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|includeParts
condition|)
return|return
name|dbAndTable
return|;
for|for
control|(
name|int
name|i
init|=
name|dbIx
operator|+
literal|2
init|;
name|i
operator|<
name|parts
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|!
name|parts
index|[
name|i
index|]
operator|.
name|contains
argument_list|(
literal|"="
argument_list|)
condition|)
break|break;
name|dbAndTable
operator|+=
literal|"/"
operator|+
name|parts
index|[
name|i
index|]
expr_stmt|;
block|}
return|return
name|dbAndTable
return|;
block|}
return|return
literal|"unknown"
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isSomeHiveDir
parameter_list|(
name|String
name|p
parameter_list|)
block|{
return|return
name|p
operator|.
name|startsWith
argument_list|(
name|BASE_PREFIX
argument_list|)
operator|||
name|p
operator|.
name|startsWith
argument_list|(
name|DELTA_PREFIX
argument_list|)
operator|||
name|p
operator|.
name|startsWith
argument_list|(
name|BUCKET_PREFIX
argument_list|)
operator|||
name|p
operator|.
name|startsWith
argument_list|(
name|UNION_SUDBIR_PREFIX
argument_list|)
operator|||
name|p
operator|.
name|startsWith
argument_list|(
name|DELETE_DELTA_PREFIX
argument_list|)
return|;
block|}
annotation|@
name|Nullable
specifier|public
specifier|static
name|ThreadMXBean
name|initThreadMxBean
parameter_list|()
block|{
name|ThreadMXBean
name|mxBean
init|=
name|ManagementFactory
operator|.
name|getThreadMXBean
argument_list|()
decl_stmt|;
if|if
condition|(
name|mxBean
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|mxBean
operator|.
name|isCurrentThreadCpuTimeSupported
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Thread CPU monitoring is not supported"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|mxBean
operator|.
name|isThreadCpuTimeEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Thread CPU monitoring is not enabled"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
return|return
name|mxBean
return|;
block|}
comment|/**    * transform a byte of crendetials to a hadoop Credentials object.    * @param binaryCredentials credentials in byte format as they would    *                          usually be when received from protobuffers    * @return a hadoop Credentials object    */
specifier|public
specifier|static
name|Credentials
name|credentialsFromByteArray
parameter_list|(
name|byte
index|[]
name|binaryCredentials
parameter_list|)
throws|throws
name|IOException
block|{
name|Credentials
name|credentials
init|=
operator|new
name|Credentials
argument_list|()
decl_stmt|;
name|DataInputBuffer
name|dib
init|=
operator|new
name|DataInputBuffer
argument_list|()
decl_stmt|;
name|dib
operator|.
name|reset
argument_list|(
name|binaryCredentials
argument_list|,
name|binaryCredentials
operator|.
name|length
argument_list|)
expr_stmt|;
name|credentials
operator|.
name|readTokenStorageStream
argument_list|(
name|dib
argument_list|)
expr_stmt|;
return|return
name|credentials
return|;
block|}
block|}
end_class

end_unit

