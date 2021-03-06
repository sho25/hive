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
name|conf
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
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
name|common
operator|.
name|FileUtils
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
name|ZooKeeperHiveHelper
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
name|InterfaceAudience
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
name|InterfaceAudience
operator|.
name|LimitedPrivate
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
name|type
operator|.
name|TimestampTZUtil
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
name|Validator
operator|.
name|PatternSet
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
name|Validator
operator|.
name|RangeValidator
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
name|Validator
operator|.
name|RatioValidator
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
name|Validator
operator|.
name|SizeValidator
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
name|Validator
operator|.
name|StringSet
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
name|Validator
operator|.
name|TimeValidator
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
name|Validator
operator|.
name|WritableDirectoryValidator
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
name|shims
operator|.
name|Utils
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
name|mapred
operator|.
name|JobConf
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
name|mapreduce
operator|.
name|lib
operator|.
name|input
operator|.
name|CombineFileInputFormat
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
name|mapreduce
operator|.
name|lib
operator|.
name|input
operator|.
name|FileInputFormat
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
name|hive
operator|.
name|common
operator|.
name|HiveCompat
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
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
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
name|File
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLDecoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLEncoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|ZoneId
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
name|Collections
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
name|HashSet
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
name|LinkedHashSet
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
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
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

begin_comment
comment|/**  * Hive Configuration.  */
end_comment

begin_class
specifier|public
class|class
name|HiveConf
extends|extends
name|Configuration
block|{
specifier|protected
name|String
name|hiveJar
decl_stmt|;
specifier|protected
name|Properties
name|origProp
decl_stmt|;
specifier|protected
name|String
name|auxJars
decl_stmt|;
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
name|HiveConf
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|loadMetastoreConfig
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|loadHiveServer2Config
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|URL
name|hiveDefaultURL
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|URL
name|hiveSiteURL
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|URL
name|hivemetastoreSiteUrl
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|URL
name|hiveServer2SiteUrl
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|confVarByteArray
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ConfVars
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ConfVars
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ConfVars
argument_list|>
name|metaConfs
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ConfVars
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|restrictList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|hiddenSet
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|rscList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|Pattern
name|modWhiteListPattern
init|=
literal|null
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|isSparkConfigUpdated
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|LOG_PREFIX_LENGTH
init|=
literal|64
decl_stmt|;
specifier|public
name|boolean
name|getSparkConfigUpdated
parameter_list|()
block|{
return|return
name|isSparkConfigUpdated
return|;
block|}
specifier|public
name|void
name|setSparkConfigUpdated
parameter_list|(
name|boolean
name|isSparkConfigUpdated
parameter_list|)
block|{
name|this
operator|.
name|isSparkConfigUpdated
operator|=
name|isSparkConfigUpdated
expr_stmt|;
block|}
specifier|public
interface|interface
name|EncoderDecoder
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
block|{
name|V
name|encode
parameter_list|(
name|K
name|key
parameter_list|)
function_decl|;
name|K
name|decode
parameter_list|(
name|V
name|value
parameter_list|)
function_decl|;
block|}
specifier|public
specifier|static
class|class
name|URLEncoderDecoder
implements|implements
name|EncoderDecoder
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|String
name|encode
parameter_list|(
name|String
name|key
parameter_list|)
block|{
try|try
block|{
return|return
name|URLEncoder
operator|.
name|encode
argument_list|(
name|key
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
operator|.
name|name
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
return|return
name|key
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|decode
parameter_list|(
name|String
name|value
parameter_list|)
block|{
try|try
block|{
return|return
name|URLDecoder
operator|.
name|decode
argument_list|(
name|value
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
operator|.
name|name
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
return|return
name|value
return|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|EncoderDecoderFactory
block|{
specifier|public
specifier|static
specifier|final
name|URLEncoderDecoder
name|URL_ENCODER_DECODER
init|=
operator|new
name|URLEncoderDecoder
argument_list|()
decl_stmt|;
block|}
static|static
block|{
name|ClassLoader
name|classLoader
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
decl_stmt|;
if|if
condition|(
name|classLoader
operator|==
literal|null
condition|)
block|{
name|classLoader
operator|=
name|HiveConf
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
expr_stmt|;
block|}
name|hiveDefaultURL
operator|=
name|classLoader
operator|.
name|getResource
argument_list|(
literal|"hive-default.xml"
argument_list|)
expr_stmt|;
comment|// Look for hive-site.xml on the CLASSPATH and log its location if found.
name|hiveSiteURL
operator|=
name|findConfigFile
argument_list|(
name|classLoader
argument_list|,
literal|"hive-site.xml"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|hivemetastoreSiteUrl
operator|=
name|findConfigFile
argument_list|(
name|classLoader
argument_list|,
literal|"hivemetastore-site.xml"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|hiveServer2SiteUrl
operator|=
name|findConfigFile
argument_list|(
name|classLoader
argument_list|,
literal|"hiveserver2-site.xml"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
for|for
control|(
name|ConfVars
name|confVar
range|:
name|ConfVars
operator|.
name|values
argument_list|()
control|)
block|{
name|vars
operator|.
name|put
argument_list|(
name|confVar
operator|.
name|varname
argument_list|,
name|confVar
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|llapDaemonConfVarsSetLocal
init|=
operator|new
name|LinkedHashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|populateLlapDaemonVarsSet
argument_list|(
name|llapDaemonConfVarsSetLocal
argument_list|)
expr_stmt|;
name|llapDaemonVarsSet
operator|=
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|llapDaemonConfVarsSetLocal
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|URL
name|findConfigFile
parameter_list|(
name|ClassLoader
name|classLoader
parameter_list|,
name|String
name|name
parameter_list|,
name|boolean
name|doLog
parameter_list|)
block|{
name|URL
name|result
init|=
name|classLoader
operator|.
name|getResource
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|String
name|confPath
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_CONF_DIR"
argument_list|)
decl_stmt|;
name|result
operator|=
name|checkConfigFile
argument_list|(
operator|new
name|File
argument_list|(
name|confPath
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|String
name|homePath
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_HOME"
argument_list|)
decl_stmt|;
name|String
name|nameInConf
init|=
literal|"conf"
operator|+
name|File
operator|.
name|separator
operator|+
name|name
decl_stmt|;
name|result
operator|=
name|checkConfigFile
argument_list|(
operator|new
name|File
argument_list|(
name|homePath
argument_list|,
name|nameInConf
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|URI
name|jarUri
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Handle both file:// and jar:<url>!{entry} in the case of shaded hive libs
name|URL
name|sourceUrl
init|=
name|HiveConf
operator|.
name|class
operator|.
name|getProtectionDomain
argument_list|()
operator|.
name|getCodeSource
argument_list|()
operator|.
name|getLocation
argument_list|()
decl_stmt|;
name|jarUri
operator|=
name|sourceUrl
operator|.
name|getProtocol
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"jar"
argument_list|)
condition|?
operator|new
name|URI
argument_list|(
name|sourceUrl
operator|.
name|getPath
argument_list|()
argument_list|)
else|:
name|sourceUrl
operator|.
name|toURI
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cannot get jar URI"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Cannot get jar URI: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// From the jar file, the parent is /lib folder
name|File
name|parent
init|=
operator|new
name|File
argument_list|(
name|jarUri
argument_list|)
operator|.
name|getParentFile
argument_list|()
decl_stmt|;
if|if
condition|(
name|parent
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|checkConfigFile
argument_list|(
operator|new
name|File
argument_list|(
name|parent
operator|.
name|getParentFile
argument_list|()
argument_list|,
name|nameInConf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|doLog
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found configuration file {}"
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
specifier|static
name|URL
name|checkConfigFile
parameter_list|(
name|File
name|f
parameter_list|)
block|{
try|try
block|{
return|return
operator|(
name|f
operator|.
name|exists
argument_list|()
operator|&&
name|f
operator|.
name|isFile
argument_list|()
operator|)
condition|?
name|f
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
else|:
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Error looking for config {}"
argument_list|,
name|f
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Error looking for config "
operator|+
name|f
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
specifier|final
name|String
name|PREFIX_LLAP
init|=
literal|"llap."
decl_stmt|;
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
specifier|final
name|String
name|PREFIX_HIVE_LLAP
init|=
literal|"hive.llap."
decl_stmt|;
comment|/**    * Metastore related options that the db is initialized against. When a conf    * var in this is list is changed, the metastore instance for the CLI will    * be recreated so that the change will take effect.    */
specifier|public
specifier|static
specifier|final
name|HiveConf
operator|.
name|ConfVars
index|[]
name|metaVars
init|=
block|{
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPLDIR
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORESELECTION
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SERVER_PORT
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORETHRIFTCONNECTIONRETRIES
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORETHRIFTFAILURERETRIES
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CLIENT_CONNECT_RETRY_DELAY
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CLIENT_SOCKET_TIMEOUT
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CLIENT_SOCKET_LIFETIME
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREPWD
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORECONNECTURLHOOK
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORECONNECTURLKEY
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORESERVERMINTHREADS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORESERVERMAXTHREADS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_TCP_KEEP_ALIVE
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_INT_ORIGINAL
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_INT_ARCHIVED
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_INT_EXTRACTED
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_KERBEROS_KEYTAB_FILE
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_KERBEROS_PRINCIPAL
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_USE_THRIFT_SASL
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_TOKEN_SIGNATURE
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CACHE_PINOBJTYPES
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_POOLING_TYPE
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_VALIDATE_TABLES
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_DATANUCLEUS_INIT_COL_INFO
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_VALIDATE_COLUMNS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_VALIDATE_CONSTRAINTS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_STORE_MANAGER_TYPE
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AUTO_CREATE_ALL
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_TRANSACTION_ISOLATION
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CACHE_LEVEL2
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CACHE_LEVEL2_TYPE
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_IDENTIFIER_FACTORY
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PLUGIN_REGISTRY_BUNDLE_CHECK
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AUTHORIZATION_STORAGE_AUTH_CHECKS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_BATCH_RETRIEVE_MAX
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EVENT_LISTENERS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_TRANSACTIONAL_EVENT_LISTENERS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EVENT_CLEAN_FREQ
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EVENT_EXPIRY_DURATION
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EVENT_MESSAGE_FACTORY
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_FILTER_HOOK
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_RAW_STORE_IMPL
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_END_FUNCTION_LISTENERS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PART_INHERIT_TBL_PROPS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_BATCH_RETRIEVE_OBJECTS_MAX
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_INIT_HOOKS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PRE_EVENT_LISTENERS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HMSHANDLERATTEMPTS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HMSHANDLERINTERVAL
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HMSHANDLERFORCERELOADCONF
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PARTITION_NAME_WHITELIST_PATTERN
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_ORM_RETRIEVE_MAPNULLS_AS_EMPTY_STRINGS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_DISALLOW_INCOMPATIBLE_COL_TYPE_CHANGES
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|USERS_IN_ADMIN_ROLE
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_MANAGER
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_TIMEOUT
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_OPERATIONAL_PROPERTIES
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_HEARTBEAT_THREADPOOL_SIZE
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_MAX_OPEN_BATCH
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_RETRYABLE_SQLEX_REGEX
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_STATS_NDV_TUNER
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_STATS_NDV_DENSITY_FUNCTION
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_ENABLED
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_SIZE
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_PARTITIONS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_FPP
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_VARIANCE
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_TTL
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_WRITER_WAIT
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_READER_WAIT
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_FULL
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_CLEAN_UNTIL
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_FASTPATH
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_HBASE_FILE_METADATA_THREADS
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_WM_DEFAULT_POOL_SIZE
block|}
decl_stmt|;
comment|/**    * User configurable Metastore vars    */
specifier|public
specifier|static
specifier|final
name|HiveConf
operator|.
name|ConfVars
index|[]
name|metaConfVars
init|=
block|{
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_TRY_DIRECT_SQL
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_TRY_DIRECT_SQL_DDL
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CLIENT_SOCKET_TIMEOUT
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PARTITION_NAME_WHITELIST_PATTERN
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CAPABILITY_CHECK
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_DISALLOW_INCOMPATIBLE_COL_TYPE_CHANGES
block|}
decl_stmt|;
static|static
block|{
for|for
control|(
name|ConfVars
name|confVar
range|:
name|metaConfVars
control|)
block|{
name|metaConfs
operator|.
name|put
argument_list|(
name|confVar
operator|.
name|varname
argument_list|,
name|confVar
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_LLAP_DAEMON_SERVICE_PRINCIPAL_NAME
init|=
literal|"hive.llap.daemon.service.principal"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_SERVER2_AUTHENTICATION_LDAP_USERMEMBERSHIPKEY_NAME
init|=
literal|"hive.server2.authentication.ldap.userMembershipKey"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_SPARK_SUBMIT_CLIENT
init|=
literal|"spark-submit"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_SPARK_LAUNCHER_CLIENT
init|=
literal|"spark-launcher"
decl_stmt|;
comment|/**    * dbVars are the parameters can be set per database. If these    * parameters are set as a database property, when switching to that    * database, the HiveConf variable will be changed. The change of these    * parameters will effectively change the DFS and MapReduce clusters    * for different databases.    */
specifier|public
specifier|static
specifier|final
name|HiveConf
operator|.
name|ConfVars
index|[]
name|dbVars
init|=
block|{
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPBIN
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
block|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SCRATCHDIR
block|}
decl_stmt|;
comment|/**    * encoded parameter values are ;-) encoded.  Use decoder to get ;-) decoded string    */
specifier|public
specifier|static
specifier|final
name|HiveConf
operator|.
name|ConfVars
index|[]
name|ENCODED_CONF
init|=
block|{
name|ConfVars
operator|.
name|HIVEQUERYSTRING
block|}
decl_stmt|;
comment|/**    * Variables used by LLAP daemons.    * TODO: Eventually auto-populate this based on prefixes. The conf variables    * will need to be renamed for this.    */
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|llapDaemonVarsSet
decl_stmt|;
specifier|private
specifier|static
name|void
name|populateLlapDaemonVarsSet
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|llapDaemonVarsSetLocal
parameter_list|)
block|{
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_IO_ENABLED
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_IO_MEMORY_MODE
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_ALLOCATOR_MIN_ALLOC
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_ALLOCATOR_MAX_ALLOC
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_ALLOCATOR_ARENA_COUNT
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_IO_MEMORY_MAX_SIZE
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_ALLOCATOR_DIRECT
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_USE_LRFU
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_LRFU_LAMBDA
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_LRFU_BP_WRAPPER_SIZE
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_CACHE_ALLOW_SYNTHETIC_FILEID
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_IO_USE_FILEID_PATH
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_IO_DECODING_METRICS_PERCENTILE_INTERVALS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_ORC_ENABLE_TIME_COUNTERS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_IO_THREADPOOL_SIZE
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_KERBEROS_PRINCIPAL
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_KERBEROS_KEYTAB_FILE
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_ZKSM_ZK_CONNECTION_STRING
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_SECURITY_ACL
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_MANAGEMENT_ACL
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_SECURITY_ACL_DENY
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_MANAGEMENT_ACL_DENY
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DELEGATION_TOKEN_LIFETIME
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_MANAGEMENT_RPC_PORT
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_WEB_AUTO_AUTH
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_RPC_NUM_HANDLERS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_WORK_DIRS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_YARN_SHUFFLE_PORT
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_YARN_CONTAINER_MB
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_SHUFFLE_DIR_WATCHER_ENABLED
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_AM_LIVENESS_HEARTBEAT_INTERVAL_MS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_AM_LIVENESS_CONNECTION_TIMEOUT_MS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_AM_LIVENESS_CONNECTION_SLEEP_BETWEEN_RETRIES_MS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_NUM_EXECUTORS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_RPC_PORT
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_XMX_HEADROOM
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_VCPUS_PER_INSTANCE
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_NUM_FILE_CLEANER_THREADS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_FILE_CLEANUP_DELAY_SECONDS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_SERVICE_HOSTS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_SERVICE_REFRESH_INTERVAL
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_ALLOW_PERMANENT_FNS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_DOWNLOAD_PERMANENT_FNS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_TASK_SCHEDULER_WAIT_QUEUE_SIZE
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_WAIT_QUEUE_COMPARATOR_CLASS_NAME
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_TASK_SCHEDULER_ENABLE_PREEMPTION
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_TASK_PREEMPTION_METRICS_INTERVALS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_WEB_PORT
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_WEB_SSL
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_CONTAINER_ID
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_VALIDATE_ACLS
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_LOGGER
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_AM_USE_FQDN
operator|.
name|varname
argument_list|)
expr_stmt|;
name|llapDaemonVarsSetLocal
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|LLAP_OUTPUT_FORMAT_ARROW
operator|.
name|varname
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get a set containing configuration parameter names used by LLAP Server isntances    * @return an unmodifiable set containing llap ConfVars    */
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|getLlapDaemonConfVars
parameter_list|()
block|{
return|return
name|llapDaemonVarsSet
return|;
block|}
comment|/**    * ConfVars.    *    * These are the default configuration properties for Hive. Each HiveConf    * object is initialized as follows:    *    * 1) Hadoop configuration properties are applied.    * 2) ConfVar properties with non-null values are overlayed.    * 3) hive-site.xml properties are overlayed.    * 4) System Properties and Manual Overrides are overlayed.    *    * WARNING: think twice before adding any Hadoop configuration properties    * with non-null values to this list as they will override any values defined    * in the underlying Hadoop configuration.    */
specifier|public
specifier|static
enum|enum
name|ConfVars
block|{
comment|// QL execution stuff
name|SCRIPTWRAPPER
argument_list|(
literal|"hive.exec.script.wrapper"
argument_list|,
literal|null
argument_list|,
literal|""
argument_list|)
block|,
name|PLAN
argument_list|(
literal|"hive.exec.plan"
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|)
block|,
name|STAGINGDIR
argument_list|(
literal|"hive.exec.stagingdir"
argument_list|,
literal|".hive-staging"
argument_list|,
literal|"Directory name that will be created inside table locations in order to support HDFS encryption. "
operator|+
literal|"This is replaces ${hive.exec.scratchdir} for query results with the exception of read-only tables. "
operator|+
literal|"In all cases ${hive.exec.scratchdir} is still used for other temporary files, such as job plans."
argument_list|)
block|,
name|SCRATCHDIR
argument_list|(
literal|"hive.exec.scratchdir"
argument_list|,
literal|"/tmp/hive"
argument_list|,
literal|"HDFS root scratch dir for Hive jobs which gets created with write all (733) permission. "
operator|+
literal|"For each connecting user, an HDFS scratch dir: ${hive.exec.scratchdir}/<username> is created, "
operator|+
literal|"with ${hive.scratch.dir.permission}."
argument_list|)
block|,
name|REPLDIR
argument_list|(
literal|"hive.repl.rootdir"
argument_list|,
literal|"/user/${system:user.name}/repl/"
argument_list|,
literal|"HDFS root dir for all replication dumps."
argument_list|)
block|,
name|REPLCMENABLED
argument_list|(
literal|"hive.repl.cm.enabled"
argument_list|,
literal|false
argument_list|,
literal|"Turn on ChangeManager, so delete files will go to cmrootdir."
argument_list|)
block|,
name|REPLCMDIR
argument_list|(
literal|"hive.repl.cmrootdir"
argument_list|,
literal|"/user/${system:user.name}/cmroot/"
argument_list|,
literal|"Root dir for ChangeManager, used for deleted files."
argument_list|)
block|,
name|REPLCMRETIAN
argument_list|(
literal|"hive.repl.cm.retain"
argument_list|,
literal|"24h"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|HOURS
argument_list|)
argument_list|,
literal|"Time to retain removed files in cmrootdir."
argument_list|)
block|,
name|REPLCMENCRYPTEDDIR
argument_list|(
literal|"hive.repl.cm.encryptionzone.rootdir"
argument_list|,
literal|".cmroot"
argument_list|,
literal|"Root dir for ChangeManager if encryption zones are enabled, used for deleted files."
argument_list|)
block|,
name|REPLCMFALLBACKNONENCRYPTEDDIR
argument_list|(
literal|"hive.repl.cm.nonencryptionzone.rootdir"
argument_list|,
literal|""
argument_list|,
literal|"Root dir for ChangeManager for non encrypted paths if hive.repl.cmrootdir is encrypted."
argument_list|)
block|,
name|REPLCMINTERVAL
argument_list|(
literal|"hive.repl.cm.interval"
argument_list|,
literal|"3600s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Inteval for cmroot cleanup thread."
argument_list|)
block|,
name|REPL_FUNCTIONS_ROOT_DIR
argument_list|(
literal|"hive.repl.replica.functions.root.dir"
argument_list|,
literal|"/user/${system:user.name}/repl/functions/"
argument_list|,
literal|"Root directory on the replica warehouse where the repl sub-system will store jars from the primary warehouse"
argument_list|)
block|,
name|REPL_APPROX_MAX_LOAD_TASKS
argument_list|(
literal|"hive.repl.approx.max.load.tasks"
argument_list|,
literal|10000
argument_list|,
literal|"Provide an approximation of the maximum number of tasks that should be executed before \n"
operator|+
literal|"dynamically generating the next set of tasks. The number is approximate as Hive \n"
operator|+
literal|"will stop at a slightly higher number, the reason being some events might lead to a \n"
operator|+
literal|"task increment that would cross the specified limit."
argument_list|)
block|,
name|REPL_PARTITIONS_DUMP_PARALLELISM
argument_list|(
literal|"hive.repl.partitions.dump.parallelism"
argument_list|,
literal|100
argument_list|,
literal|"Number of threads that will be used to dump partition data information during repl dump."
argument_list|)
block|,
name|REPL_DUMPDIR_CLEAN_FREQ
argument_list|(
literal|"hive.repl.dumpdir.clean.freq"
argument_list|,
literal|"0s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Frequency at which timer task runs to purge expired dump dirs."
argument_list|)
block|,
name|REPL_DUMPDIR_TTL
argument_list|(
literal|"hive.repl.dumpdir.ttl"
argument_list|,
literal|"7d"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|DAYS
argument_list|)
argument_list|,
literal|"TTL of dump dirs before cleanup."
argument_list|)
block|,
name|REPL_DUMP_METADATA_ONLY
argument_list|(
literal|"hive.repl.dump.metadata.only"
argument_list|,
literal|false
argument_list|,
literal|"Indicates whether replication dump only metadata information or data + metadata. \n"
operator|+
literal|"This config makes hive.repl.include.external.tables config ineffective."
argument_list|)
block|,
name|REPL_DUMP_METADATA_ONLY_FOR_EXTERNAL_TABLE
argument_list|(
literal|"hive.repl.dump.metadata.only.for.external.table"
argument_list|,
literal|false
argument_list|,
literal|"Indicates whether external table replication dump only metadata information or data + metadata"
argument_list|)
block|,
name|REPL_BOOTSTRAP_ACID_TABLES
argument_list|(
literal|"hive.repl.bootstrap.acid.tables"
argument_list|,
literal|false
argument_list|,
literal|"Indicates if repl dump should bootstrap the information about ACID tables along with \n"
operator|+
literal|"incremental dump for replication. It is recommended to keep this config parameter \n"
operator|+
literal|"as false always and should be set to true only via WITH clause of REPL DUMP \n"
operator|+
literal|"command. It should be set to true only once for incremental repl dump on \n"
operator|+
literal|"each of the existing replication policies after enabling acid tables replication."
argument_list|)
block|,
name|REPL_BOOTSTRAP_DUMP_OPEN_TXN_TIMEOUT
argument_list|(
literal|"hive.repl.bootstrap.dump.open.txn.timeout"
argument_list|,
literal|"1h"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|HOURS
argument_list|)
argument_list|,
literal|"Indicates the timeout for all transactions which are opened before triggering bootstrap REPL DUMP. "
operator|+
literal|"If these open transactions are not closed within the timeout value, then REPL DUMP will "
operator|+
literal|"forcefully abort those transactions and continue with bootstrap dump."
argument_list|)
block|,
comment|//https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/TransparentEncryption.html#Running_as_the_superuser
name|REPL_ADD_RAW_RESERVED_NAMESPACE
argument_list|(
literal|"hive.repl.add.raw.reserved.namespace"
argument_list|,
literal|false
argument_list|,
literal|"For TDE with same encryption keys on source and target, allow Distcp super user to access \n"
operator|+
literal|"the raw bytes from filesystem without decrypting on source and then encrypting on target."
argument_list|)
block|,
name|REPL_INCLUDE_EXTERNAL_TABLES
argument_list|(
literal|"hive.repl.include.external.tables"
argument_list|,
literal|false
argument_list|,
literal|"Indicates if repl dump should include information about external tables. It should be \n"
operator|+
literal|"used in conjunction with 'hive.repl.dump.metadata.only' set to false. if 'hive.repl.dump.metadata.only' \n"
operator|+
literal|" is set to true then this config parameter has no effect as external table meta data is flushed \n"
operator|+
literal|" always by default. If this config parameter is enabled on an on-going replication policy which is in\n"
operator|+
literal|" incremental phase, then need to set 'hive.repl.bootstrap.external.tables' to true for the first \n"
operator|+
literal|" repl dump to bootstrap all external tables."
argument_list|)
block|,
name|REPL_BOOTSTRAP_EXTERNAL_TABLES
argument_list|(
literal|"hive.repl.bootstrap.external.tables"
argument_list|,
literal|false
argument_list|,
literal|"Indicates if repl dump should bootstrap the information about external tables along with incremental \n"
operator|+
literal|"dump for replication. It is recommended to keep this config parameter as false always and should be \n"
operator|+
literal|"set to true only via WITH clause of REPL DUMP command. It should be used in conjunction with \n"
operator|+
literal|"'hive.repl.include.external.tables' when sets to true. If 'hive.repl.include.external.tables' is \n"
operator|+
literal|"set to false, then this config parameter has no effect. It should be set to true only once for \n"
operator|+
literal|"incremental repl dump on each existing replication policy after enabling external tables replication."
argument_list|)
block|,
name|REPL_ENABLE_MOVE_OPTIMIZATION
argument_list|(
literal|"hive.repl.enable.move.optimization"
argument_list|,
literal|false
argument_list|,
literal|"If its set to true, REPL LOAD copies data files directly to the target table/partition location \n"
operator|+
literal|"instead of copying to staging directory first and then move to target location. This optimizes \n"
operator|+
literal|" the REPL LOAD on object data stores such as S3 or WASB where creating a directory and move \n"
operator|+
literal|" files are costly operations. In file system like HDFS where move operation is atomic, this \n"
operator|+
literal|" optimization should not be enabled as it may lead to inconsistent data read for non acid tables."
argument_list|)
block|,
name|REPL_MOVE_OPTIMIZED_FILE_SCHEMES
argument_list|(
literal|"hive.repl.move.optimized.scheme"
argument_list|,
literal|"s3a, wasb"
argument_list|,
literal|"Comma separated list of schemes for which move optimization will be enabled during repl load. \n"
operator|+
literal|"This configuration overrides the value set using REPL_ENABLE_MOVE_OPTIMIZATION for the given schemes. \n"
operator|+
literal|" Schemes of the file system which does not support atomic move (rename) can be specified here to \n "
operator|+
literal|" speed up the repl load operation. In file system like HDFS where move operation is atomic, this \n"
operator|+
literal|" optimization should not be enabled as it may lead to inconsistent data read for non acid tables."
argument_list|)
block|,
name|REPL_EXTERNAL_TABLE_BASE_DIR
argument_list|(
literal|"hive.repl.replica.external.table.base.dir"
argument_list|,
literal|"/"
argument_list|,
literal|"This is the base directory on the target/replica warehouse under which data for "
operator|+
literal|"external tables is stored. This is relative base path and hence prefixed to the source "
operator|+
literal|"external table path on target cluster."
argument_list|)
block|,
name|LOCALSCRATCHDIR
argument_list|(
literal|"hive.exec.local.scratchdir"
argument_list|,
literal|"${system:java.io.tmpdir}"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"${system:user.name}"
argument_list|,
literal|"Local scratch space for Hive jobs"
argument_list|)
block|,
name|DOWNLOADED_RESOURCES_DIR
argument_list|(
literal|"hive.downloaded.resources.dir"
argument_list|,
literal|"${system:java.io.tmpdir}"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"${hive.session.id}_resources"
argument_list|,
literal|"Temporary local directory for added resources in the remote file system."
argument_list|)
block|,
name|SCRATCHDIRPERMISSION
argument_list|(
literal|"hive.scratch.dir.permission"
argument_list|,
literal|"700"
argument_list|,
literal|"The permission for the user specific scratch directories that get created."
argument_list|)
block|,
name|SUBMITVIACHILD
argument_list|(
literal|"hive.exec.submitviachild"
argument_list|,
literal|false
argument_list|,
literal|""
argument_list|)
block|,
name|SUBMITLOCALTASKVIACHILD
argument_list|(
literal|"hive.exec.submit.local.task.via.child"
argument_list|,
literal|true
argument_list|,
literal|"Determines whether local tasks (typically mapjoin hashtable generation phase) runs in \n"
operator|+
literal|"separate JVM (true recommended) or not. \n"
operator|+
literal|"Avoids the overhead of spawning new JVM, but can lead to out-of-memory issues."
argument_list|)
block|,
name|SCRIPTERRORLIMIT
argument_list|(
literal|"hive.exec.script.maxerrsize"
argument_list|,
literal|100000
argument_list|,
literal|"Maximum number of bytes a script is allowed to emit to standard error (per map-reduce task). \n"
operator|+
literal|"This prevents runaway scripts from filling logs partitions to capacity"
argument_list|)
block|,
name|ALLOWPARTIALCONSUMP
argument_list|(
literal|"hive.exec.script.allow.partial.consumption"
argument_list|,
literal|false
argument_list|,
literal|"When enabled, this option allows a user script to exit successfully without consuming \n"
operator|+
literal|"all the data from the standard input."
argument_list|)
block|,
name|STREAMREPORTERPERFIX
argument_list|(
literal|"stream.stderr.reporter.prefix"
argument_list|,
literal|"reporter:"
argument_list|,
literal|"Streaming jobs that log to standard error with this prefix can log counter or status information."
argument_list|)
block|,
name|STREAMREPORTERENABLED
argument_list|(
literal|"stream.stderr.reporter.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Enable consumption of status and counter messages for streaming jobs."
argument_list|)
block|,
name|COMPRESSRESULT
argument_list|(
literal|"hive.exec.compress.output"
argument_list|,
literal|false
argument_list|,
literal|"This controls whether the final outputs of a query (to a local/HDFS file or a Hive table) is compressed. \n"
operator|+
literal|"The compression codec and other options are determined from Hadoop config variables mapred.output.compress*"
argument_list|)
block|,
name|COMPRESSINTERMEDIATE
argument_list|(
literal|"hive.exec.compress.intermediate"
argument_list|,
literal|false
argument_list|,
literal|"This controls whether intermediate files produced by Hive between multiple map-reduce jobs are compressed. \n"
operator|+
literal|"The compression codec and other options are determined from Hadoop config variables mapred.output.compress*"
argument_list|)
block|,
name|COMPRESSINTERMEDIATECODEC
argument_list|(
literal|"hive.intermediate.compression.codec"
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|)
block|,
name|COMPRESSINTERMEDIATETYPE
argument_list|(
literal|"hive.intermediate.compression.type"
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|)
block|,
name|BYTESPERREDUCER
argument_list|(
literal|"hive.exec.reducers.bytes.per.reducer"
argument_list|,
call|(
name|long
call|)
argument_list|(
literal|256
operator|*
literal|1000
operator|*
literal|1000
argument_list|)
argument_list|,
literal|"size per reducer.The default is 256Mb, i.e if the input size is 1G, it will use 4 reducers."
argument_list|)
block|,
name|MAXREDUCERS
argument_list|(
literal|"hive.exec.reducers.max"
argument_list|,
literal|1009
argument_list|,
literal|"max number of reducers will be used. If the one specified in the configuration parameter mapred.reduce.tasks is\n"
operator|+
literal|"negative, Hive will use this one as the max number of reducers when automatically determine number of reducers."
argument_list|)
block|,
name|PREEXECHOOKS
argument_list|(
literal|"hive.exec.pre.hooks"
argument_list|,
literal|""
argument_list|,
literal|"Comma-separated list of pre-execution hooks to be invoked for each statement. \n"
operator|+
literal|"A pre-execution hook is specified as the name of a Java class which implements the \n"
operator|+
literal|"org.apache.hadoop.hive.ql.hooks.ExecuteWithHookContext interface."
argument_list|)
block|,
name|POSTEXECHOOKS
argument_list|(
literal|"hive.exec.post.hooks"
argument_list|,
literal|""
argument_list|,
literal|"Comma-separated list of post-execution hooks to be invoked for each statement. \n"
operator|+
literal|"A post-execution hook is specified as the name of a Java class which implements the \n"
operator|+
literal|"org.apache.hadoop.hive.ql.hooks.ExecuteWithHookContext interface."
argument_list|)
block|,
name|ONFAILUREHOOKS
argument_list|(
literal|"hive.exec.failure.hooks"
argument_list|,
literal|""
argument_list|,
literal|"Comma-separated list of on-failure hooks to be invoked for each statement. \n"
operator|+
literal|"An on-failure hook is specified as the name of Java class which implements the \n"
operator|+
literal|"org.apache.hadoop.hive.ql.hooks.ExecuteWithHookContext interface."
argument_list|)
block|,
name|QUERYREDACTORHOOKS
argument_list|(
literal|"hive.exec.query.redactor.hooks"
argument_list|,
literal|""
argument_list|,
literal|"Comma-separated list of hooks to be invoked for each query which can \n"
operator|+
literal|"tranform the query before it's placed in the job.xml file. Must be a Java class which \n"
operator|+
literal|"extends from the org.apache.hadoop.hive.ql.hooks.Redactor abstract class."
argument_list|)
block|,
name|CLIENTSTATSPUBLISHERS
argument_list|(
literal|"hive.client.stats.publishers"
argument_list|,
literal|""
argument_list|,
literal|"Comma-separated list of statistics publishers to be invoked on counters on each job. \n"
operator|+
literal|"A client stats publisher is specified as the name of a Java class which implements the \n"
operator|+
literal|"org.apache.hadoop.hive.ql.stats.ClientStatsPublisher interface."
argument_list|)
block|,
name|ATSHOOKQUEUECAPACITY
argument_list|(
literal|"hive.ats.hook.queue.capacity"
argument_list|,
literal|64
argument_list|,
literal|"Queue size for the ATS Hook executor. If the number of outstanding submissions \n"
operator|+
literal|"to the ATS executor exceed this amount, the Hive ATS Hook will not try to log queries to ATS."
argument_list|)
block|,
name|EXECPARALLEL
argument_list|(
literal|"hive.exec.parallel"
argument_list|,
literal|false
argument_list|,
literal|"Whether to execute jobs in parallel"
argument_list|)
block|,
name|EXECPARALLETHREADNUMBER
argument_list|(
literal|"hive.exec.parallel.thread.number"
argument_list|,
literal|8
argument_list|,
literal|"How many jobs at most can be executed in parallel"
argument_list|)
block|,
name|HIVESPECULATIVEEXECREDUCERS
argument_list|(
literal|"hive.mapred.reduce.tasks.speculative.execution"
argument_list|,
literal|true
argument_list|,
literal|"Whether speculative execution for reducers should be turned on. "
argument_list|)
block|,
name|HIVECOUNTERSPULLINTERVAL
argument_list|(
literal|"hive.exec.counters.pull.interval"
argument_list|,
literal|1000L
argument_list|,
literal|"The interval with which to poll the JobTracker for the counters the running job. \n"
operator|+
literal|"The smaller it is the more load there will be on the jobtracker, the higher it is the less granular the caught will be."
argument_list|)
block|,
name|DYNAMICPARTITIONING
argument_list|(
literal|"hive.exec.dynamic.partition"
argument_list|,
literal|true
argument_list|,
literal|"Whether or not to allow dynamic partitions in DML/DDL."
argument_list|)
block|,
name|DYNAMICPARTITIONINGMODE
argument_list|(
literal|"hive.exec.dynamic.partition.mode"
argument_list|,
literal|"nonstrict"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"strict"
argument_list|,
literal|"nonstrict"
argument_list|)
argument_list|,
literal|"In strict mode, the user must specify at least one static partition\n"
operator|+
literal|"in case the user accidentally overwrites all partitions.\n"
operator|+
literal|"In nonstrict mode all partitions are allowed to be dynamic."
argument_list|)
block|,
name|DYNAMICPARTITIONMAXPARTS
argument_list|(
literal|"hive.exec.max.dynamic.partitions"
argument_list|,
literal|1000
argument_list|,
literal|"Maximum number of dynamic partitions allowed to be created in total."
argument_list|)
block|,
name|DYNAMICPARTITIONMAXPARTSPERNODE
argument_list|(
literal|"hive.exec.max.dynamic.partitions.pernode"
argument_list|,
literal|100
argument_list|,
literal|"Maximum number of dynamic partitions allowed to be created in each mapper/reducer node."
argument_list|)
block|,
name|DYNAMICPARTITIONCONVERT
argument_list|(
literal|"hive.exec.dynamic.partition.type.conversion"
argument_list|,
literal|true
argument_list|,
literal|"Whether to check and cast a dynamic partition column before creating the partition "
operator|+
literal|"directory. For example, if partition p is type int and we insert string '001', then if "
operator|+
literal|"this value is true, directory p=1 will be created; if false, p=001"
argument_list|)
block|,
name|MAXCREATEDFILES
argument_list|(
literal|"hive.exec.max.created.files"
argument_list|,
literal|100000L
argument_list|,
literal|"Maximum number of HDFS files created by all mappers/reducers in a MapReduce job."
argument_list|)
block|,
name|DEFAULTPARTITIONNAME
argument_list|(
literal|"hive.exec.default.partition.name"
argument_list|,
literal|"__HIVE_DEFAULT_PARTITION__"
argument_list|,
literal|"The default partition name in case the dynamic partition column value is null/empty string or any other values that cannot be escaped. \n"
operator|+
literal|"This value must not contain any special character used in HDFS URI (e.g., ':', '%', '/' etc). \n"
operator|+
literal|"The user has to be aware that the dynamic partition value should not contain this value to avoid confusions."
argument_list|)
block|,
name|DEFAULT_ZOOKEEPER_PARTITION_NAME
argument_list|(
literal|"hive.lockmgr.zookeeper.default.partition.name"
argument_list|,
literal|"__HIVE_DEFAULT_ZOOKEEPER_PARTITION__"
argument_list|,
literal|""
argument_list|)
block|,
comment|// Whether to show a link to the most failed task + debugging tips
name|SHOW_JOB_FAIL_DEBUG_INFO
argument_list|(
literal|"hive.exec.show.job.failure.debug.info"
argument_list|,
literal|true
argument_list|,
literal|"If a job fails, whether to provide a link in the CLI to the task with the\n"
operator|+
literal|"most failures, along with debugging hints if applicable."
argument_list|)
block|,
name|JOB_DEBUG_CAPTURE_STACKTRACES
argument_list|(
literal|"hive.exec.job.debug.capture.stacktraces"
argument_list|,
literal|true
argument_list|,
literal|"Whether or not stack traces parsed from the task logs of a sampled failed task \n"
operator|+
literal|"for each failed job should be stored in the SessionState"
argument_list|)
block|,
name|JOB_DEBUG_TIMEOUT
argument_list|(
literal|"hive.exec.job.debug.timeout"
argument_list|,
literal|30000
argument_list|,
literal|""
argument_list|)
block|,
name|TASKLOG_DEBUG_TIMEOUT
argument_list|(
literal|"hive.exec.tasklog.debug.timeout"
argument_list|,
literal|20000
argument_list|,
literal|""
argument_list|)
block|,
name|OUTPUT_FILE_EXTENSION
argument_list|(
literal|"hive.output.file.extension"
argument_list|,
literal|null
argument_list|,
literal|"String used as a file extension for output files. \n"
operator|+
literal|"If not set, defaults to the codec extension for text files (e.g. \".gz\"), or no extension otherwise."
argument_list|)
block|,
name|HIVE_IN_TEST
argument_list|(
literal|"hive.in.test"
argument_list|,
literal|false
argument_list|,
literal|"internal usage only, true in test mode"
argument_list|,
literal|true
argument_list|)
block|,
name|HIVE_IN_TEST_SSL
argument_list|(
literal|"hive.in.ssl.test"
argument_list|,
literal|false
argument_list|,
literal|"internal usage only, true in SSL test mode"
argument_list|,
literal|true
argument_list|)
block|,
comment|// TODO: this needs to be removed; see TestReplicationScenarios* comments.
name|HIVE_IN_TEST_REPL
argument_list|(
literal|"hive.in.repl.test"
argument_list|,
literal|false
argument_list|,
literal|"internal usage only, true in replication test mode"
argument_list|,
literal|true
argument_list|)
block|,
name|HIVE_IN_TEST_IDE
argument_list|(
literal|"hive.in.ide.test"
argument_list|,
literal|false
argument_list|,
literal|"internal usage only, true if test running in ide"
argument_list|,
literal|true
argument_list|)
block|,
name|HIVE_TESTING_SHORT_LOGS
argument_list|(
literal|"hive.testing.short.logs"
argument_list|,
literal|false
argument_list|,
literal|"internal usage only, used only in test mode. If set true, when requesting the "
operator|+
literal|"operation logs the short version (generated by LogDivertAppenderForTest) will be "
operator|+
literal|"returned"
argument_list|)
block|,
name|HIVE_TESTING_REMOVE_LOGS
argument_list|(
literal|"hive.testing.remove.logs"
argument_list|,
literal|true
argument_list|,
literal|"internal usage only, used only in test mode. If set false, the operation logs, and the "
operator|+
literal|"operation log directory will not be removed, so they can be found after the test runs."
argument_list|)
block|,
name|HIVE_TEST_LOAD_HOSTNAMES
argument_list|(
literal|"hive.test.load.hostnames"
argument_list|,
literal|""
argument_list|,
literal|"Specify host names for load testing. (e.g., \"host1,host2,host3\"). Leave it empty if no "
operator|+
literal|"load generation is needed (eg. for production)."
argument_list|)
block|,
name|HIVE_TEST_LOAD_INTERVAL
argument_list|(
literal|"hive.test.load.interval"
argument_list|,
literal|"10ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"The interval length used for load and idle periods in milliseconds."
argument_list|)
block|,
name|HIVE_TEST_LOAD_UTILIZATION
argument_list|(
literal|"hive.test.load.utilization"
argument_list|,
literal|0.2f
argument_list|,
literal|"Specify processor load utilization between 0.0 (not loaded on all threads) and 1.0 "
operator|+
literal|"(fully loaded on all threads). Comparing this with a random value the load generator creates "
operator|+
literal|"hive.test.load.interval length active loops or idle periods"
argument_list|)
block|,
name|HIVE_IN_TEZ_TEST
argument_list|(
literal|"hive.in.tez.test"
argument_list|,
literal|false
argument_list|,
literal|"internal use only, true when in testing tez"
argument_list|,
literal|true
argument_list|)
block|,
name|HIVE_MAPJOIN_TESTING_NO_HASH_TABLE_LOAD
argument_list|(
literal|"hive.mapjoin.testing.no.hash.table.load"
argument_list|,
literal|false
argument_list|,
literal|"internal use only, true when in testing map join"
argument_list|,
literal|true
argument_list|)
block|,
name|HIVE_ADDITIONAL_PARTIAL_MASKS_PATTERN
argument_list|(
literal|"hive.qtest.additional.partial.mask.pattern"
argument_list|,
literal|""
argument_list|,
literal|"internal use only, used in only qtests. Provide additional partial masks pattern"
operator|+
literal|"for qtests as a ',' separated list"
argument_list|)
block|,
name|HIVE_ADDITIONAL_PARTIAL_MASKS_REPLACEMENT_TEXT
argument_list|(
literal|"hive.qtest.additional.partial.mask.replacement.text"
argument_list|,
literal|""
argument_list|,
literal|"internal use only, used in only qtests. Provide additional partial masks replacement"
operator|+
literal|"text for qtests as a ',' separated list"
argument_list|)
block|,
name|HIVE_IN_REPL_TEST_FILES_SORTED
argument_list|(
literal|"hive.in.repl.test.files.sorted"
argument_list|,
literal|false
argument_list|,
literal|"internal usage only, set to true if the file listing is required in sorted order during bootstrap load"
argument_list|,
literal|true
argument_list|)
block|,
name|LOCALMODEAUTO
argument_list|(
literal|"hive.exec.mode.local.auto"
argument_list|,
literal|false
argument_list|,
literal|"Let Hive determine whether to run in local mode automatically"
argument_list|)
block|,
name|LOCALMODEMAXBYTES
argument_list|(
literal|"hive.exec.mode.local.auto.inputbytes.max"
argument_list|,
literal|134217728L
argument_list|,
literal|"When hive.exec.mode.local.auto is true, input bytes should less than this for local mode."
argument_list|)
block|,
name|LOCALMODEMAXINPUTFILES
argument_list|(
literal|"hive.exec.mode.local.auto.input.files.max"
argument_list|,
literal|4
argument_list|,
literal|"When hive.exec.mode.local.auto is true, the number of tasks should less than this for local mode."
argument_list|)
block|,
name|DROP_IGNORES_NON_EXISTENT
argument_list|(
literal|"hive.exec.drop.ignorenonexistent"
argument_list|,
literal|true
argument_list|,
literal|"Do not report an error if DROP TABLE/VIEW/Index/Function specifies a non-existent table/view/function"
argument_list|)
block|,
name|HIVEIGNOREMAPJOINHINT
argument_list|(
literal|"hive.ignore.mapjoin.hint"
argument_list|,
literal|true
argument_list|,
literal|"Ignore the mapjoin hint"
argument_list|)
block|,
name|HIVE_FILE_MAX_FOOTER
argument_list|(
literal|"hive.file.max.footer"
argument_list|,
literal|100
argument_list|,
literal|"maximum number of lines for footer user can define for a table file"
argument_list|)
block|,
name|HIVE_RESULTSET_USE_UNIQUE_COLUMN_NAMES
argument_list|(
literal|"hive.resultset.use.unique.column.names"
argument_list|,
literal|true
argument_list|,
literal|"Make column names unique in the result set by qualifying column names with table alias if needed.\n"
operator|+
literal|"Table alias will be added to column names for queries of type \"select *\" or \n"
operator|+
literal|"if query explicitly uses table alias \"select r1.x..\"."
argument_list|)
block|,
name|HIVE_PROTO_EVENTS_QUEUE_CAPACITY
argument_list|(
literal|"hive.hook.proto.queue.capacity"
argument_list|,
literal|64
argument_list|,
literal|"Queue capacity for the proto events logging threads."
argument_list|)
block|,
name|HIVE_PROTO_EVENTS_BASE_PATH
argument_list|(
literal|"hive.hook.proto.base-directory"
argument_list|,
literal|""
argument_list|,
literal|"Base directory into which the proto event messages are written by HiveProtoLoggingHook."
argument_list|)
block|,
name|HIVE_PROTO_EVENTS_ROLLOVER_CHECK_INTERVAL
argument_list|(
literal|"hive.hook.proto.rollover-interval"
argument_list|,
literal|"600s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
literal|0L
argument_list|,
literal|true
argument_list|,
literal|3600
operator|*
literal|24L
argument_list|,
literal|true
argument_list|)
argument_list|,
literal|"Frequency at which the file rollover check is triggered."
argument_list|)
block|,
name|HIVE_PROTO_EVENTS_CLEAN_FREQ
argument_list|(
literal|"hive.hook.proto.events.clean.freq"
argument_list|,
literal|"1d"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|DAYS
argument_list|)
argument_list|,
literal|"Frequency at which timer task runs to purge expired proto event files."
argument_list|)
block|,
name|HIVE_PROTO_EVENTS_TTL
argument_list|(
literal|"hive.hook.proto.events.ttl"
argument_list|,
literal|"7d"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|DAYS
argument_list|)
argument_list|,
literal|"Time-To-Live (TTL) of proto event files before cleanup."
argument_list|)
block|,
name|HIVE_PROTO_FILE_PER_EVENT
argument_list|(
literal|"hive.hook.proto.file.per.event"
argument_list|,
literal|false
argument_list|,
literal|"Whether each proto event has to be written to separate file. "
operator|+
literal|"(Use this for FS that does not hflush immediately like S3A)"
argument_list|)
block|,
comment|// Hadoop Configuration Properties
comment|// Properties with null values are ignored and exist only for the purpose of giving us
comment|// a symbolic name to reference in the Hive source code. Properties with non-null
comment|// values will override any values set in the underlying Hadoop configuration.
name|HADOOPBIN
argument_list|(
literal|"hadoop.bin.path"
argument_list|,
name|findHadoopBinary
argument_list|()
argument_list|,
literal|""
argument_list|,
literal|true
argument_list|)
block|,
name|YARNBIN
argument_list|(
literal|"yarn.bin.path"
argument_list|,
name|findYarnBinary
argument_list|()
argument_list|,
literal|""
argument_list|,
literal|true
argument_list|)
block|,
name|MAPREDBIN
argument_list|(
literal|"mapred.bin.path"
argument_list|,
name|findMapRedBinary
argument_list|()
argument_list|,
literal|""
argument_list|,
literal|true
argument_list|)
block|,
name|HIVE_FS_HAR_IMPL
argument_list|(
literal|"fs.har.impl"
argument_list|,
literal|"org.apache.hadoop.hive.shims.HiveHarFileSystem"
argument_list|,
literal|"The implementation for accessing Hadoop Archives. Note that this won't be applicable to Hadoop versions less than 0.20"
argument_list|)
block|,
name|MAPREDMAXSPLITSIZE
argument_list|(
name|FileInputFormat
operator|.
name|SPLIT_MAXSIZE
argument_list|,
literal|256000000L
argument_list|,
literal|""
argument_list|,
literal|true
argument_list|)
block|,
name|MAPREDMINSPLITSIZE
argument_list|(
name|FileInputFormat
operator|.
name|SPLIT_MINSIZE
argument_list|,
literal|1L
argument_list|,
literal|""
argument_list|,
literal|true
argument_list|)
block|,
name|MAPREDMINSPLITSIZEPERNODE
argument_list|(
name|CombineFileInputFormat
operator|.
name|SPLIT_MINSIZE_PERNODE
argument_list|,
literal|1L
argument_list|,
literal|""
argument_list|,
literal|true
argument_list|)
block|,
name|MAPREDMINSPLITSIZEPERRACK
argument_list|(
name|CombineFileInputFormat
operator|.
name|SPLIT_MINSIZE_PERRACK
argument_list|,
literal|1L
argument_list|,
literal|""
argument_list|,
literal|true
argument_list|)
block|,
comment|// The number of reduce tasks per job. Hadoop sets this value to 1 by default
comment|// By setting this property to -1, Hive will automatically determine the correct
comment|// number of reducers.
name|HADOOPNUMREDUCERS
argument_list|(
literal|"mapreduce.job.reduces"
argument_list|,
operator|-
literal|1
argument_list|,
literal|""
argument_list|,
literal|true
argument_list|)
block|,
comment|// Metastore stuff. Be sure to update HiveConf.metaVars when you add something here!
name|METASTOREDBTYPE
argument_list|(
literal|"hive.metastore.db.type"
argument_list|,
literal|"DERBY"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"DERBY"
argument_list|,
literal|"ORACLE"
argument_list|,
literal|"MYSQL"
argument_list|,
literal|"MSSQL"
argument_list|,
literal|"POSTGRES"
argument_list|)
argument_list|,
literal|"Type of database used by the metastore. Information schema& JDBCStorageHandler depend on it."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.WAREHOUSE      */
annotation|@
name|Deprecated
name|METASTOREWAREHOUSE
argument_list|(
literal|"hive.metastore.warehouse.dir"
argument_list|,
literal|"/user/hive/warehouse"
argument_list|,
literal|"location of default database for the warehouse"
argument_list|)
block|,
name|HIVE_METASTORE_WAREHOUSE_EXTERNAL
argument_list|(
literal|"hive.metastore.warehouse.external.dir"
argument_list|,
literal|null
argument_list|,
literal|"Default location for external tables created in the warehouse. "
operator|+
literal|"If not set or null, then the normal warehouse location will be used as the default location."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.THRIFT_URIS      */
annotation|@
name|Deprecated
name|METASTOREURIS
argument_list|(
literal|"hive.metastore.uris"
argument_list|,
literal|""
argument_list|,
literal|"Thrift URI for the remote metastore. Used by metastore client to connect to remote metastore."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.THRIFT_URI_SELECTION      */
annotation|@
name|Deprecated
name|METASTORESELECTION
argument_list|(
literal|"hive.metastore.uri.selection"
argument_list|,
literal|"RANDOM"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"SEQUENTIAL"
argument_list|,
literal|"RANDOM"
argument_list|)
argument_list|,
literal|"Determines the selection mechanism used by metastore client to connect to remote "
operator|+
literal|"metastore.  SEQUENTIAL implies that the first valid metastore from the URIs specified "
operator|+
literal|"as part of hive.metastore.uris will be picked.  RANDOM implies that the metastore "
operator|+
literal|"will be picked randomly"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.CAPABILITY_CHECK      */
annotation|@
name|Deprecated
name|METASTORE_CAPABILITY_CHECK
argument_list|(
literal|"hive.metastore.client.capability.check"
argument_list|,
literal|true
argument_list|,
literal|"Whether to check client capabilities for potentially breaking API usage."
argument_list|)
block|,
name|METASTORE_CLIENT_CAPABILITIES
argument_list|(
literal|"hive.metastore.client.capabilities"
argument_list|,
literal|""
argument_list|,
literal|"Capabilities possessed by HiveServer"
argument_list|)
block|,
name|METASTORE_CLIENT_CACHE_ENABLED
argument_list|(
literal|"hive.metastore.client.cache.enabled"
argument_list|,
literal|false
argument_list|,
literal|"Whether to enable metastore client cache"
argument_list|)
block|,
name|METASTORE_CLIENT_CACHE_EXPIRY_TIME
argument_list|(
literal|"hive.metastore.client.cache.expiry.time"
argument_list|,
literal|"120s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Expiry time for metastore client cache"
argument_list|)
block|,
name|METASTORE_CLIENT_CACHE_INITIAL_CAPACITY
argument_list|(
literal|"hive.metastore.client.cache.initial.capacity"
argument_list|,
literal|50
argument_list|,
literal|"Initial capacity for metastore client cache"
argument_list|)
block|,
name|METASTORE_CLIENT_CACHE_MAX_CAPACITY
argument_list|(
literal|"hive.metastore.client.cache.max.capacity"
argument_list|,
literal|50
argument_list|,
literal|"Max capacity for metastore client cache"
argument_list|)
block|,
name|METASTORE_CLIENT_CACHE_STATS_ENABLED
argument_list|(
literal|"hive.metastore.client.cache.stats.enabled"
argument_list|,
literal|false
argument_list|,
literal|"Whether to enable metastore client cache stats"
argument_list|)
block|,
name|METASTORE_FASTPATH
argument_list|(
literal|"hive.metastore.fastpath"
argument_list|,
literal|false
argument_list|,
literal|"Used to avoid all of the proxies and object copies in the metastore.  Note, if this is "
operator|+
literal|"set, you MUST use a local metastore (hive.metastore.uris must be empty) otherwise "
operator|+
literal|"undefined and most likely undesired behavior will result"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.FS_HANDLER_THREADS_COUNT      */
annotation|@
name|Deprecated
name|METASTORE_FS_HANDLER_THREADS_COUNT
argument_list|(
literal|"hive.metastore.fshandler.threads"
argument_list|,
literal|15
argument_list|,
literal|"Number of threads to be allocated for metastore handler for fs operations."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.FILE_METADATA_THREADS      */
annotation|@
name|Deprecated
name|METASTORE_HBASE_FILE_METADATA_THREADS
argument_list|(
literal|"hive.metastore.hbase.file.metadata.threads"
argument_list|,
literal|1
argument_list|,
literal|"Number of threads to use to read file metadata in background to cache it."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.URI_RESOLVER      */
annotation|@
name|Deprecated
name|METASTORE_URI_RESOLVER
argument_list|(
literal|"hive.metastore.uri.resolver"
argument_list|,
literal|""
argument_list|,
literal|"If set, fully qualified class name of resolver for hive metastore uri's"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.THRIFT_CONNECTION_RETRIES      */
annotation|@
name|Deprecated
name|METASTORETHRIFTCONNECTIONRETRIES
argument_list|(
literal|"hive.metastore.connect.retries"
argument_list|,
literal|3
argument_list|,
literal|"Number of retries while opening a connection to metastore"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.THRIFT_FAILURE_RETRIES      */
annotation|@
name|Deprecated
name|METASTORETHRIFTFAILURERETRIES
argument_list|(
literal|"hive.metastore.failure.retries"
argument_list|,
literal|1
argument_list|,
literal|"Number of retries upon failure of Thrift metastore calls"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.SERVER_PORT      */
annotation|@
name|Deprecated
name|METASTORE_SERVER_PORT
argument_list|(
literal|"hive.metastore.port"
argument_list|,
literal|9083
argument_list|,
literal|"Hive metastore listener port"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.CLIENT_CONNECT_RETRY_DELAY      */
annotation|@
name|Deprecated
name|METASTORE_CLIENT_CONNECT_RETRY_DELAY
argument_list|(
literal|"hive.metastore.client.connect.retry.delay"
argument_list|,
literal|"1s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Number of seconds for the client to wait between consecutive connection attempts"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.CLIENT_SOCKET_TIMEOUT      */
annotation|@
name|Deprecated
name|METASTORE_CLIENT_SOCKET_TIMEOUT
argument_list|(
literal|"hive.metastore.client.socket.timeout"
argument_list|,
literal|"600s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"MetaStore Client socket timeout in seconds"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.CLIENT_SOCKET_LIFETIME      */
annotation|@
name|Deprecated
name|METASTORE_CLIENT_SOCKET_LIFETIME
argument_list|(
literal|"hive.metastore.client.socket.lifetime"
argument_list|,
literal|"0s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"MetaStore Client socket lifetime in seconds. After this time is exceeded, client\n"
operator|+
literal|"reconnects on the next MetaStore operation. A value of 0s means the connection\n"
operator|+
literal|"has an infinite lifetime."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.PWD      */
annotation|@
name|Deprecated
name|METASTOREPWD
argument_list|(
literal|"javax.jdo.option.ConnectionPassword"
argument_list|,
literal|"mine"
argument_list|,
literal|"password to use against metastore database"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.CONNECT_URL_HOOK      */
annotation|@
name|Deprecated
name|METASTORECONNECTURLHOOK
argument_list|(
literal|"hive.metastore.ds.connection.url.hook"
argument_list|,
literal|""
argument_list|,
literal|"Name of the hook to use for retrieving the JDO connection URL. If empty, the value in javax.jdo.option.ConnectionURL is used"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.MULTITHREADED      */
annotation|@
name|Deprecated
name|METASTOREMULTITHREADED
argument_list|(
literal|"javax.jdo.option.Multithreaded"
argument_list|,
literal|true
argument_list|,
literal|"Set this to true if multiple threads access metastore through JDO concurrently."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.CONNECT_URL_KEY      */
annotation|@
name|Deprecated
name|METASTORECONNECTURLKEY
argument_list|(
literal|"javax.jdo.option.ConnectionURL"
argument_list|,
literal|"jdbc:derby:;databaseName=metastore_db;create=true"
argument_list|,
literal|"JDBC connect string for a JDBC metastore.\n"
operator|+
literal|"To use SSL to encrypt/authenticate the connection, provide database-specific SSL flag in the connection URL.\n"
operator|+
literal|"For example, jdbc:postgresql://myhost/db?ssl=true for postgres database."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.DBACCESS_SSL_PROPS      */
annotation|@
name|Deprecated
name|METASTORE_DBACCESS_SSL_PROPS
argument_list|(
literal|"hive.metastore.dbaccess.ssl.properties"
argument_list|,
literal|""
argument_list|,
literal|"Comma-separated SSL properties for metastore to access database when JDO connection URL\n"
operator|+
literal|"enables SSL access. e.g. javax.net.ssl.trustStore=/tmp/truststore,javax.net.ssl.trustStorePassword=pwd."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.HMS_HANDLER_ATTEMPTS      */
annotation|@
name|Deprecated
name|HMSHANDLERATTEMPTS
argument_list|(
literal|"hive.hmshandler.retry.attempts"
argument_list|,
literal|10
argument_list|,
literal|"The number of times to retry a HMSHandler call if there were a connection error."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.HMS_HANDLER_INTERVAL      */
annotation|@
name|Deprecated
name|HMSHANDLERINTERVAL
argument_list|(
literal|"hive.hmshandler.retry.interval"
argument_list|,
literal|"2000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"The time between HMSHandler retry attempts on failure."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.HMS_HANDLER_FORCE_RELOAD_CONF      */
annotation|@
name|Deprecated
name|HMSHANDLERFORCERELOADCONF
argument_list|(
literal|"hive.hmshandler.force.reload.conf"
argument_list|,
literal|false
argument_list|,
literal|"Whether to force reloading of the HMSHandler configuration (including\n"
operator|+
literal|"the connection URL, before the next metastore query that accesses the\n"
operator|+
literal|"datastore. Once reloaded, this value is reset to false. Used for\n"
operator|+
literal|"testing only."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.SERVER_MAX_MESSAGE_SIZE      */
annotation|@
name|Deprecated
name|METASTORESERVERMAXMESSAGESIZE
argument_list|(
literal|"hive.metastore.server.max.message.size"
argument_list|,
literal|100
operator|*
literal|1024
operator|*
literal|1024L
argument_list|,
literal|"Maximum message size in bytes a HMS will accept."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.SERVER_MIN_THREADS      */
annotation|@
name|Deprecated
name|METASTORESERVERMINTHREADS
argument_list|(
literal|"hive.metastore.server.min.threads"
argument_list|,
literal|200
argument_list|,
literal|"Minimum number of worker threads in the Thrift server's pool."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.SERVER_MAX_THREADS      */
annotation|@
name|Deprecated
name|METASTORESERVERMAXTHREADS
argument_list|(
literal|"hive.metastore.server.max.threads"
argument_list|,
literal|1000
argument_list|,
literal|"Maximum number of worker threads in the Thrift server's pool."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.TCP_KEEP_ALIVE      */
annotation|@
name|Deprecated
name|METASTORE_TCP_KEEP_ALIVE
argument_list|(
literal|"hive.metastore.server.tcp.keepalive"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable TCP keepalive for the metastore server. Keepalive will prevent accumulation of half-open connections."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.WM_DEFAULT_POOL_SIZE      */
annotation|@
name|Deprecated
name|METASTORE_WM_DEFAULT_POOL_SIZE
argument_list|(
literal|"hive.metastore.wm.default.pool.size"
argument_list|,
literal|4
argument_list|,
literal|"The size of a default pool to create when creating an empty resource plan;\n"
operator|+
literal|"If not positive, no default pool will be created."
argument_list|)
block|,
name|METASTORE_INT_ORIGINAL
argument_list|(
literal|"hive.metastore.archive.intermediate.original"
argument_list|,
literal|"_INTERMEDIATE_ORIGINAL"
argument_list|,
literal|"Intermediate dir suffixes used for archiving. Not important what they\n"
operator|+
literal|"are, as long as collisions are avoided"
argument_list|)
block|,
name|METASTORE_INT_ARCHIVED
argument_list|(
literal|"hive.metastore.archive.intermediate.archived"
argument_list|,
literal|"_INTERMEDIATE_ARCHIVED"
argument_list|,
literal|""
argument_list|)
block|,
name|METASTORE_INT_EXTRACTED
argument_list|(
literal|"hive.metastore.archive.intermediate.extracted"
argument_list|,
literal|"_INTERMEDIATE_EXTRACTED"
argument_list|,
literal|""
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.KERBEROS_KEYTAB_FILE      */
annotation|@
name|Deprecated
name|METASTORE_KERBEROS_KEYTAB_FILE
argument_list|(
literal|"hive.metastore.kerberos.keytab.file"
argument_list|,
literal|""
argument_list|,
literal|"The path to the Kerberos Keytab file containing the metastore Thrift server's service principal."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.KERBEROS_PRINCIPAL      */
annotation|@
name|Deprecated
name|METASTORE_KERBEROS_PRINCIPAL
argument_list|(
literal|"hive.metastore.kerberos.principal"
argument_list|,
literal|"hive-metastore/_HOST@EXAMPLE.COM"
argument_list|,
literal|"The service principal for the metastore Thrift server. \n"
operator|+
literal|"The special string _HOST will be replaced automatically with the correct host name."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.CLIENT_KERBEROS_PRINCIPAL      */
annotation|@
name|Deprecated
name|METASTORE_CLIENT_KERBEROS_PRINCIPAL
argument_list|(
literal|"hive.metastore.client.kerberos.principal"
argument_list|,
literal|""
argument_list|,
comment|// E.g. "hive-metastore/_HOST@EXAMPLE.COM".
literal|"The Kerberos principal associated with the HA cluster of hcat_servers."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.USE_THRIFT_SASL      */
annotation|@
name|Deprecated
name|METASTORE_USE_THRIFT_SASL
argument_list|(
literal|"hive.metastore.sasl.enabled"
argument_list|,
literal|false
argument_list|,
literal|"If true, the metastore Thrift interface will be secured with SASL. Clients must authenticate with Kerberos."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.USE_THRIFT_FRAMED_TRANSPORT      */
annotation|@
name|Deprecated
name|METASTORE_USE_THRIFT_FRAMED_TRANSPORT
argument_list|(
literal|"hive.metastore.thrift.framed.transport.enabled"
argument_list|,
literal|false
argument_list|,
literal|"If true, the metastore Thrift interface will use TFramedTransport. When false (default) a standard TTransport is used."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.USE_THRIFT_COMPACT_PROTOCOL      */
annotation|@
name|Deprecated
name|METASTORE_USE_THRIFT_COMPACT_PROTOCOL
argument_list|(
literal|"hive.metastore.thrift.compact.protocol.enabled"
argument_list|,
literal|false
argument_list|,
literal|"If true, the metastore Thrift interface will use TCompactProtocol. When false (default) TBinaryProtocol will be used.\n"
operator|+
literal|"Setting it to true will break compatibility with older clients running TBinaryProtocol."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.TOKEN_SIGNATURE      */
annotation|@
name|Deprecated
name|METASTORE_TOKEN_SIGNATURE
argument_list|(
literal|"hive.metastore.token.signature"
argument_list|,
literal|""
argument_list|,
literal|"The delegation token service name to match when selecting a token from the current user's tokens."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.DELEGATION_TOKEN_STORE_CLS      */
annotation|@
name|Deprecated
name|METASTORE_CLUSTER_DELEGATION_TOKEN_STORE_CLS
argument_list|(
literal|"hive.cluster.delegation.token.store.class"
argument_list|,
literal|"org.apache.hadoop.hive.thrift.MemoryTokenStore"
argument_list|,
literal|"The delegation token store implementation. Set to org.apache.hadoop.hive.thrift.ZooKeeperTokenStore for load-balanced cluster."
argument_list|)
block|,
name|METASTORE_CLUSTER_DELEGATION_TOKEN_STORE_ZK_CONNECTSTR
argument_list|(
literal|"hive.cluster.delegation.token.store.zookeeper.connectString"
argument_list|,
literal|""
argument_list|,
literal|"The ZooKeeper token store connect string. You can re-use the configuration value\n"
operator|+
literal|"set in hive.zookeeper.quorum, by leaving this parameter unset."
argument_list|)
block|,
name|METASTORE_CLUSTER_DELEGATION_TOKEN_STORE_ZK_ZNODE
argument_list|(
literal|"hive.cluster.delegation.token.store.zookeeper.znode"
argument_list|,
literal|"/hivedelegation"
argument_list|,
literal|"The root path for token store data. Note that this is used by both HiveServer2 and\n"
operator|+
literal|"MetaStore to store delegation Token. One directory gets created for each of them.\n"
operator|+
literal|"The final directory names would have the servername appended to it (HIVESERVER2,\n"
operator|+
literal|"METASTORE)."
argument_list|)
block|,
name|METASTORE_CLUSTER_DELEGATION_TOKEN_STORE_ZK_ACL
argument_list|(
literal|"hive.cluster.delegation.token.store.zookeeper.acl"
argument_list|,
literal|""
argument_list|,
literal|"ACL for token store entries. Comma separated list of ACL entries. For example:\n"
operator|+
literal|"sasl:hive/host1@MY.DOMAIN:cdrwa,sasl:hive/host2@MY.DOMAIN:cdrwa\n"
operator|+
literal|"Defaults to all permissions for the hiveserver2/metastore process user."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.CACHE_PINOBJTYPES      */
annotation|@
name|Deprecated
name|METASTORE_CACHE_PINOBJTYPES
argument_list|(
literal|"hive.metastore.cache.pinobjtypes"
argument_list|,
literal|"Table,StorageDescriptor,SerDeInfo,Partition,Database,Type,FieldSchema,Order"
argument_list|,
literal|"List of comma separated metastore object types that should be pinned in the cache"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.CONNECTION_POOLING_TYPE      */
annotation|@
name|Deprecated
name|METASTORE_CONNECTION_POOLING_TYPE
argument_list|(
literal|"datanucleus.connectionPoolingType"
argument_list|,
literal|"HikariCP"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"BONECP"
argument_list|,
literal|"DBCP"
argument_list|,
literal|"HikariCP"
argument_list|,
literal|"NONE"
argument_list|)
argument_list|,
literal|"Specify connection pool library for datanucleus"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.CONNECTION_POOLING_MAX_CONNECTIONS      */
annotation|@
name|Deprecated
name|METASTORE_CONNECTION_POOLING_MAX_CONNECTIONS
argument_list|(
literal|"datanucleus.connectionPool.maxPoolSize"
argument_list|,
literal|10
argument_list|,
literal|"Specify the maximum number of connections in the connection pool. Note: The configured size will be used by\n"
operator|+
literal|"2 connection pools (TxnHandler and ObjectStore). When configuring the max connection pool size, it is\n"
operator|+
literal|"recommended to take into account the number of metastore instances and the number of HiveServer2 instances\n"
operator|+
literal|"configured with embedded metastore. To get optimal performance, set config to meet the following condition\n"
operator|+
literal|"(2 * pool_size * metastore_instances + 2 * pool_size * HS2_instances_with_embedded_metastore) = \n"
operator|+
literal|"(2 * physical_core_count + hard_disk_count)."
argument_list|)
block|,
comment|// Workaround for DN bug on Postgres:
comment|// http://www.datanucleus.org/servlet/forum/viewthread_thread,7985_offset
comment|/**      * @deprecated Use MetastoreConf.DATANUCLEUS_INIT_COL_INFO      */
annotation|@
name|Deprecated
name|METASTORE_DATANUCLEUS_INIT_COL_INFO
argument_list|(
literal|"datanucleus.rdbms.initializeColumnInfo"
argument_list|,
literal|"NONE"
argument_list|,
literal|"initializeColumnInfo setting for DataNucleus; set to NONE at least on Postgres."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.VALIDATE_TABLES      */
annotation|@
name|Deprecated
name|METASTORE_VALIDATE_TABLES
argument_list|(
literal|"datanucleus.schema.validateTables"
argument_list|,
literal|false
argument_list|,
literal|"validates existing schema against code. turn this on if you want to verify existing schema"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.VALIDATE_COLUMNS      */
annotation|@
name|Deprecated
name|METASTORE_VALIDATE_COLUMNS
argument_list|(
literal|"datanucleus.schema.validateColumns"
argument_list|,
literal|false
argument_list|,
literal|"validates existing schema against code. turn this on if you want to verify existing schema"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.VALIDATE_CONSTRAINTS      */
annotation|@
name|Deprecated
name|METASTORE_VALIDATE_CONSTRAINTS
argument_list|(
literal|"datanucleus.schema.validateConstraints"
argument_list|,
literal|false
argument_list|,
literal|"validates existing schema against code. turn this on if you want to verify existing schema"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.STORE_MANAGER_TYPE      */
annotation|@
name|Deprecated
name|METASTORE_STORE_MANAGER_TYPE
argument_list|(
literal|"datanucleus.storeManagerType"
argument_list|,
literal|"rdbms"
argument_list|,
literal|"metadata store type"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.AUTO_CREATE_ALL      */
annotation|@
name|Deprecated
name|METASTORE_AUTO_CREATE_ALL
argument_list|(
literal|"datanucleus.schema.autoCreateAll"
argument_list|,
literal|false
argument_list|,
literal|"Auto creates necessary schema on a startup if one doesn't exist. Set this to false, after creating it once."
operator|+
literal|"To enable auto create also set hive.metastore.schema.verification=false. Auto creation is not "
operator|+
literal|"recommended for production use cases, run schematool command instead."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.SCHEMA_VERIFICATION      */
annotation|@
name|Deprecated
name|METASTORE_SCHEMA_VERIFICATION
argument_list|(
literal|"hive.metastore.schema.verification"
argument_list|,
literal|true
argument_list|,
literal|"Enforce metastore schema version consistency.\n"
operator|+
literal|"True: Verify that version information stored in is compatible with one from Hive jars.  Also disable automatic\n"
operator|+
literal|"      schema migration attempt. Users are required to manually migrate schema after Hive upgrade which ensures\n"
operator|+
literal|"      proper metastore schema migration. (Default)\n"
operator|+
literal|"False: Warn if the version information stored in metastore doesn't match with one from in Hive jars."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.SCHEMA_VERIFICATION_RECORD_VERSION      */
annotation|@
name|Deprecated
name|METASTORE_SCHEMA_VERIFICATION_RECORD_VERSION
argument_list|(
literal|"hive.metastore.schema.verification.record.version"
argument_list|,
literal|false
argument_list|,
literal|"When true the current MS version is recorded in the VERSION table. If this is disabled and verification is\n"
operator|+
literal|" enabled the MS will be unusable."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.SCHEMA_INFO_CLASS      */
annotation|@
name|Deprecated
name|METASTORE_SCHEMA_INFO_CLASS
argument_list|(
literal|"hive.metastore.schema.info.class"
argument_list|,
literal|"org.apache.hadoop.hive.metastore.MetaStoreSchemaInfo"
argument_list|,
literal|"Fully qualified class name for the metastore schema information class \n"
operator|+
literal|"which is used by schematool to fetch the schema information.\n"
operator|+
literal|" This class should implement the IMetaStoreSchemaInfo interface"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.DATANUCLEUS_TRANSACTION_ISOLATION      */
annotation|@
name|Deprecated
name|METASTORE_TRANSACTION_ISOLATION
argument_list|(
literal|"datanucleus.transactionIsolation"
argument_list|,
literal|"read-committed"
argument_list|,
literal|"Default transaction isolation level for identity generation."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.DATANUCLEUS_CACHE_LEVEL2      */
annotation|@
name|Deprecated
name|METASTORE_CACHE_LEVEL2
argument_list|(
literal|"datanucleus.cache.level2"
argument_list|,
literal|false
argument_list|,
literal|"Use a level 2 cache. Turn this off if metadata is changed independently of Hive metastore server"
argument_list|)
block|,
name|METASTORE_CACHE_LEVEL2_TYPE
argument_list|(
literal|"datanucleus.cache.level2.type"
argument_list|,
literal|"none"
argument_list|,
literal|""
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.IDENTIFIER_FACTORY      */
annotation|@
name|Deprecated
name|METASTORE_IDENTIFIER_FACTORY
argument_list|(
literal|"datanucleus.identifierFactory"
argument_list|,
literal|"datanucleus1"
argument_list|,
literal|"Name of the identifier factory to use when generating table/column names etc. \n"
operator|+
literal|"'datanucleus1' is used for backward compatibility with DataNucleus v1"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.DATANUCLEUS_USE_LEGACY_VALUE_STRATEGY      */
annotation|@
name|Deprecated
name|METASTORE_USE_LEGACY_VALUE_STRATEGY
argument_list|(
literal|"datanucleus.rdbms.useLegacyNativeValueStrategy"
argument_list|,
literal|true
argument_list|,
literal|""
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.DATANUCLEUS_PLUGIN_REGISTRY_BUNDLE_CHECK      */
annotation|@
name|Deprecated
name|METASTORE_PLUGIN_REGISTRY_BUNDLE_CHECK
argument_list|(
literal|"datanucleus.plugin.pluginRegistryBundleCheck"
argument_list|,
literal|"LOG"
argument_list|,
literal|"Defines what happens when plugin bundles are found and are duplicated [EXCEPTION|LOG|NONE]"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.BATCH_RETRIEVE_MAX      */
annotation|@
name|Deprecated
name|METASTORE_BATCH_RETRIEVE_MAX
argument_list|(
literal|"hive.metastore.batch.retrieve.max"
argument_list|,
literal|300
argument_list|,
literal|"Maximum number of objects (tables/partitions) can be retrieved from metastore in one batch. \n"
operator|+
literal|"The higher the number, the less the number of round trips is needed to the Hive metastore server, \n"
operator|+
literal|"but it may also cause higher memory requirement at the client side."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.BATCH_RETRIEVE_OBJECTS_MAX      */
annotation|@
name|Deprecated
name|METASTORE_BATCH_RETRIEVE_OBJECTS_MAX
argument_list|(
literal|"hive.metastore.batch.retrieve.table.partition.max"
argument_list|,
literal|1000
argument_list|,
literal|"Maximum number of objects that metastore internally retrieves in one batch."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.INIT_HOOKS      */
annotation|@
name|Deprecated
name|METASTORE_INIT_HOOKS
argument_list|(
literal|"hive.metastore.init.hooks"
argument_list|,
literal|""
argument_list|,
literal|"A comma separated list of hooks to be invoked at the beginning of HMSHandler initialization. \n"
operator|+
literal|"An init hook is specified as the name of Java class which extends org.apache.hadoop.hive.metastore.MetaStoreInitListener."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.PRE_EVENT_LISTENERS      */
annotation|@
name|Deprecated
name|METASTORE_PRE_EVENT_LISTENERS
argument_list|(
literal|"hive.metastore.pre.event.listeners"
argument_list|,
literal|""
argument_list|,
literal|"List of comma separated listeners for metastore events."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.EVENT_LISTENERS      */
annotation|@
name|Deprecated
name|METASTORE_EVENT_LISTENERS
argument_list|(
literal|"hive.metastore.event.listeners"
argument_list|,
literal|""
argument_list|,
literal|"A comma separated list of Java classes that implement the org.apache.hadoop.hive.metastore.MetaStoreEventListener"
operator|+
literal|" interface. The metastore event and corresponding listener method will be invoked in separate JDO transactions. "
operator|+
literal|"Alternatively, configure hive.metastore.transactional.event.listeners to ensure both are invoked in same JDO transaction."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.TRANSACTIONAL_EVENT_LISTENERS      */
annotation|@
name|Deprecated
name|METASTORE_TRANSACTIONAL_EVENT_LISTENERS
argument_list|(
literal|"hive.metastore.transactional.event.listeners"
argument_list|,
literal|""
argument_list|,
literal|"A comma separated list of Java classes that implement the org.apache.hadoop.hive.metastore.MetaStoreEventListener"
operator|+
literal|" interface. Both the metastore event and corresponding listener method will be invoked in the same JDO transaction."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.NOTIFICATION_SEQUENCE_LOCK_MAX_RETRIES      */
annotation|@
name|Deprecated
name|NOTIFICATION_SEQUENCE_LOCK_MAX_RETRIES
argument_list|(
literal|"hive.notification.sequence.lock.max.retries"
argument_list|,
literal|10
argument_list|,
literal|"Number of retries required to acquire a lock when getting the next notification sequential ID for entries "
operator|+
literal|"in the NOTIFICATION_LOG table."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.NOTIFICATION_SEQUENCE_LOCK_RETRY_SLEEP_INTERVAL      */
annotation|@
name|Deprecated
name|NOTIFICATION_SEQUENCE_LOCK_RETRY_SLEEP_INTERVAL
argument_list|(
literal|"hive.notification.sequence.lock.retry.sleep.interval"
argument_list|,
literal|10L
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Sleep interval between retries to acquire a notification lock as described part of property "
operator|+
name|NOTIFICATION_SEQUENCE_LOCK_MAX_RETRIES
operator|.
name|name
argument_list|()
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.EVENT_DB_LISTENER_TTL      */
annotation|@
name|Deprecated
name|METASTORE_EVENT_DB_LISTENER_TTL
argument_list|(
literal|"hive.metastore.event.db.listener.timetolive"
argument_list|,
literal|"86400s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"time after which events will be removed from the database listener queue"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.EVENT_DB_NOTIFICATION_API_AUTH      */
annotation|@
name|Deprecated
name|METASTORE_EVENT_DB_NOTIFICATION_API_AUTH
argument_list|(
literal|"hive.metastore.event.db.notification.api.auth"
argument_list|,
literal|true
argument_list|,
literal|"Should metastore do authorization against database notification related APIs such as get_next_notification.\n"
operator|+
literal|"If set to true, then only the superusers in proxy settings have the permission"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.AUTHORIZATION_STORAGE_AUTH_CHECKS      */
annotation|@
name|Deprecated
name|METASTORE_AUTHORIZATION_STORAGE_AUTH_CHECKS
argument_list|(
literal|"hive.metastore.authorization.storage.checks"
argument_list|,
literal|false
argument_list|,
literal|"Should the metastore do authorization checks against the underlying storage (usually hdfs) \n"
operator|+
literal|"for operations like drop-partition (disallow the drop-partition if the user in\n"
operator|+
literal|"question doesn't have permissions to delete the corresponding directory\n"
operator|+
literal|"on the storage)."
argument_list|)
block|,
name|METASTORE_AUTHORIZATION_EXTERNALTABLE_DROP_CHECK
argument_list|(
literal|"hive.metastore.authorization.storage.check.externaltable.drop"
argument_list|,
literal|true
argument_list|,
literal|"Should StorageBasedAuthorization check permission of the storage before dropping external table.\n"
operator|+
literal|"StorageBasedAuthorization already does this check for managed table. For external table however,\n"
operator|+
literal|"anyone who has read permission of the directory could drop external table, which is surprising.\n"
operator|+
literal|"The flag is set to false by default to maintain backward compatibility."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.EVENT_CLEAN_FREQ      */
annotation|@
name|Deprecated
name|METASTORE_EVENT_CLEAN_FREQ
argument_list|(
literal|"hive.metastore.event.clean.freq"
argument_list|,
literal|"0s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Frequency at which timer task runs to purge expired events in metastore."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.EVENT_EXPIRY_DURATION      */
annotation|@
name|Deprecated
name|METASTORE_EVENT_EXPIRY_DURATION
argument_list|(
literal|"hive.metastore.event.expiry.duration"
argument_list|,
literal|"0s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Duration after which events expire from events table"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.EVENT_MESSAGE_FACTORY      */
annotation|@
name|Deprecated
name|METASTORE_EVENT_MESSAGE_FACTORY
argument_list|(
literal|"hive.metastore.event.message.factory"
argument_list|,
literal|"org.apache.hadoop.hive.metastore.messaging.json.gzip.GzipJSONMessageEncoder"
argument_list|,
literal|"Factory class for making encoding and decoding messages in the events generated."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.EXECUTE_SET_UGI      */
annotation|@
name|Deprecated
name|METASTORE_EXECUTE_SET_UGI
argument_list|(
literal|"hive.metastore.execute.setugi"
argument_list|,
literal|true
argument_list|,
literal|"In unsecure mode, setting this property to true will cause the metastore to execute DFS operations using \n"
operator|+
literal|"the client's reported user and group permissions. Note that this property must be set on \n"
operator|+
literal|"both the client and server sides. Further note that its best effort. \n"
operator|+
literal|"If client sets its to true and server sets it to false, client setting will be ignored."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.PARTITION_NAME_WHITELIST_PATTERN      */
annotation|@
name|Deprecated
name|METASTORE_PARTITION_NAME_WHITELIST_PATTERN
argument_list|(
literal|"hive.metastore.partition.name.whitelist.pattern"
argument_list|,
literal|""
argument_list|,
literal|"Partition names will be checked against this regex pattern and rejected if not matched."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.INTEGER_JDO_PUSHDOWN      */
annotation|@
name|Deprecated
name|METASTORE_INTEGER_JDO_PUSHDOWN
argument_list|(
literal|"hive.metastore.integral.jdo.pushdown"
argument_list|,
literal|false
argument_list|,
literal|"Allow JDO query pushdown for integral partition columns in metastore. Off by default. This\n"
operator|+
literal|"improves metastore perf for integral columns, especially if there's a large number of partitions.\n"
operator|+
literal|"However, it doesn't work correctly with integral values that are not normalized (e.g. have\n"
operator|+
literal|"leading zeroes, like 0012). If metastore direct SQL is enabled and works, this optimization\n"
operator|+
literal|"is also irrelevant."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.TRY_DIRECT_SQL      */
annotation|@
name|Deprecated
name|METASTORE_TRY_DIRECT_SQL
argument_list|(
literal|"hive.metastore.try.direct.sql"
argument_list|,
literal|true
argument_list|,
literal|"Whether the Hive metastore should try to use direct SQL queries instead of the\n"
operator|+
literal|"DataNucleus for certain read paths. This can improve metastore performance when\n"
operator|+
literal|"fetching many partitions or column statistics by orders of magnitude; however, it\n"
operator|+
literal|"is not guaranteed to work on all RDBMS-es and all versions. In case of SQL failures,\n"
operator|+
literal|"the metastore will fall back to the DataNucleus, so it's safe even if SQL doesn't\n"
operator|+
literal|"work for all queries on your datastore. If all SQL queries fail (for example, your\n"
operator|+
literal|"metastore is backed by MongoDB), you might want to disable this to save the\n"
operator|+
literal|"try-and-fall-back cost."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.DIRECT_SQL_PARTITION_BATCH_SIZE      */
annotation|@
name|Deprecated
name|METASTORE_DIRECT_SQL_PARTITION_BATCH_SIZE
argument_list|(
literal|"hive.metastore.direct.sql.batch.size"
argument_list|,
literal|0
argument_list|,
literal|"Batch size for partition and other object retrieval from the underlying DB in direct\n"
operator|+
literal|"SQL. For some DBs like Oracle and MSSQL, there are hardcoded or perf-based limitations\n"
operator|+
literal|"that necessitate this. For DBs that can handle the queries, this isn't necessary and\n"
operator|+
literal|"may impede performance. -1 means no batching, 0 means automatic batching."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.TRY_DIRECT_SQL_DDL      */
annotation|@
name|Deprecated
name|METASTORE_TRY_DIRECT_SQL_DDL
argument_list|(
literal|"hive.metastore.try.direct.sql.ddl"
argument_list|,
literal|true
argument_list|,
literal|"Same as hive.metastore.try.direct.sql, for read statements within a transaction that\n"
operator|+
literal|"modifies metastore data. Due to non-standard behavior in Postgres, if a direct SQL\n"
operator|+
literal|"select query has incorrect syntax or something similar inside a transaction, the\n"
operator|+
literal|"entire transaction will fail and fall-back to DataNucleus will not be possible. You\n"
operator|+
literal|"should disable the usage of direct SQL inside transactions if that happens in your case."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.DIRECT_SQL_MAX_QUERY_LENGTH      */
annotation|@
name|Deprecated
name|METASTORE_DIRECT_SQL_MAX_QUERY_LENGTH
argument_list|(
literal|"hive.direct.sql.max.query.length"
argument_list|,
literal|100
argument_list|,
literal|"The maximum\n"
operator|+
literal|" size of a query string (in KB)."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.DIRECT_SQL_MAX_ELEMENTS_IN_CLAUSE      */
annotation|@
name|Deprecated
name|METASTORE_DIRECT_SQL_MAX_ELEMENTS_IN_CLAUSE
argument_list|(
literal|"hive.direct.sql.max.elements.in.clause"
argument_list|,
literal|1000
argument_list|,
literal|"The maximum number of values in a IN clause. Once exceeded, it will be broken into\n"
operator|+
literal|" multiple OR separated IN clauses."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.DIRECT_SQL_MAX_ELEMENTS_VALUES_CLAUSE      */
annotation|@
name|Deprecated
name|METASTORE_DIRECT_SQL_MAX_ELEMENTS_VALUES_CLAUSE
argument_list|(
literal|"hive.direct.sql.max.elements.values.clause"
argument_list|,
literal|1000
argument_list|,
literal|"The maximum number of values in a VALUES clause for INSERT statement."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.ORM_RETRIEVE_MAPNULLS_AS_EMPTY_STRINGS      */
annotation|@
name|Deprecated
name|METASTORE_ORM_RETRIEVE_MAPNULLS_AS_EMPTY_STRINGS
argument_list|(
literal|"hive.metastore.orm.retrieveMapNullsAsEmptyStrings"
argument_list|,
literal|false
argument_list|,
literal|"Thrift does not support nulls in maps, so any nulls present in maps retrieved from ORM must "
operator|+
literal|"either be pruned or converted to empty strings. Some backing dbs such as Oracle persist empty strings "
operator|+
literal|"as nulls, so we should set this parameter if we wish to reverse that behaviour. For others, "
operator|+
literal|"pruning is the correct behaviour"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.DISALLOW_INCOMPATIBLE_COL_TYPE_CHANGES      */
annotation|@
name|Deprecated
name|METASTORE_DISALLOW_INCOMPATIBLE_COL_TYPE_CHANGES
argument_list|(
literal|"hive.metastore.disallow.incompatible.col.type.changes"
argument_list|,
literal|true
argument_list|,
literal|"If true (default is false), ALTER TABLE operations which change the type of a\n"
operator|+
literal|"column (say STRING) to an incompatible type (say MAP) are disallowed.\n"
operator|+
literal|"RCFile default SerDe (ColumnarSerDe) serializes the values in such a way that the\n"
operator|+
literal|"datatypes can be converted from string to any type. The map is also serialized as\n"
operator|+
literal|"a string, which can be read as a string as well. However, with any binary\n"
operator|+
literal|"serialization, this is not true. Blocking the ALTER TABLE prevents ClassCastExceptions\n"
operator|+
literal|"when subsequently trying to access old partitions.\n"
operator|+
literal|"\n"
operator|+
literal|"Primitive types like INT, STRING, BIGINT, etc., are compatible with each other and are\n"
operator|+
literal|"not blocked.\n"
operator|+
literal|"\n"
operator|+
literal|"See HIVE-4409 for more details."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.LIMIT_PARTITION_REQUEST      */
annotation|@
name|Deprecated
name|METASTORE_LIMIT_PARTITION_REQUEST
argument_list|(
literal|"hive.metastore.limit.partition.request"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"This limits the number of partitions that can be requested from the metastore for a given table.\n"
operator|+
literal|"The default value \"-1\" means no limit."
argument_list|)
block|,
name|NEWTABLEDEFAULTPARA
argument_list|(
literal|"hive.table.parameters.default"
argument_list|,
literal|""
argument_list|,
literal|"Default property values for newly created tables"
argument_list|)
block|,
name|DDL_CTL_PARAMETERS_WHITELIST
argument_list|(
literal|"hive.ddl.createtablelike.properties.whitelist"
argument_list|,
literal|""
argument_list|,
literal|"Table Properties to copy over when executing a Create Table Like."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.RAW_STORE_IMPL      */
annotation|@
name|Deprecated
name|METASTORE_RAW_STORE_IMPL
argument_list|(
literal|"hive.metastore.rawstore.impl"
argument_list|,
literal|"org.apache.hadoop.hive.metastore.ObjectStore"
argument_list|,
literal|"Name of the class that implements org.apache.hadoop.hive.metastore.rawstore interface. \n"
operator|+
literal|"This class is used to store and retrieval of raw metadata objects such as table, database"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.TXN_STORE_IMPL      */
annotation|@
name|Deprecated
name|METASTORE_TXN_STORE_IMPL
argument_list|(
literal|"hive.metastore.txn.store.impl"
argument_list|,
literal|"org.apache.hadoop.hive.metastore.txn.CompactionTxnHandler"
argument_list|,
literal|"Name of class that implements org.apache.hadoop.hive.metastore.txn.TxnStore.  This "
operator|+
literal|"class is used to store and retrieve transactions and locks"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.CONNECTION_DRIVER      */
annotation|@
name|Deprecated
name|METASTORE_CONNECTION_DRIVER
argument_list|(
literal|"javax.jdo.option.ConnectionDriverName"
argument_list|,
literal|"org.apache.derby.jdbc.EmbeddedDriver"
argument_list|,
literal|"Driver class name for a JDBC metastore"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.MANAGER_FACTORY_CLASS      */
annotation|@
name|Deprecated
name|METASTORE_MANAGER_FACTORY_CLASS
argument_list|(
literal|"javax.jdo.PersistenceManagerFactoryClass"
argument_list|,
literal|"org.datanucleus.api.jdo.JDOPersistenceManagerFactory"
argument_list|,
literal|"class implementing the jdo persistence"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.EXPRESSION_PROXY_CLASS      */
annotation|@
name|Deprecated
name|METASTORE_EXPRESSION_PROXY_CLASS
argument_list|(
literal|"hive.metastore.expression.proxy"
argument_list|,
literal|"org.apache.hadoop.hive.ql.optimizer.ppr.PartitionExpressionForMetastore"
argument_list|,
literal|""
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.DETACH_ALL_ON_COMMIT      */
annotation|@
name|Deprecated
name|METASTORE_DETACH_ALL_ON_COMMIT
argument_list|(
literal|"javax.jdo.option.DetachAllOnCommit"
argument_list|,
literal|true
argument_list|,
literal|"Detaches all objects from session so that they can be used after transaction is committed"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.NON_TRANSACTIONAL_READ      */
annotation|@
name|Deprecated
name|METASTORE_NON_TRANSACTIONAL_READ
argument_list|(
literal|"javax.jdo.option.NonTransactionalRead"
argument_list|,
literal|true
argument_list|,
literal|"Reads outside of transactions"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.CONNECTION_USER_NAME      */
annotation|@
name|Deprecated
name|METASTORE_CONNECTION_USER_NAME
argument_list|(
literal|"javax.jdo.option.ConnectionUserName"
argument_list|,
literal|"APP"
argument_list|,
literal|"Username to use against metastore database"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.END_FUNCTION_LISTENERS      */
annotation|@
name|Deprecated
name|METASTORE_END_FUNCTION_LISTENERS
argument_list|(
literal|"hive.metastore.end.function.listeners"
argument_list|,
literal|""
argument_list|,
literal|"List of comma separated listeners for the end of metastore functions."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.PART_INHERIT_TBL_PROPS      */
annotation|@
name|Deprecated
name|METASTORE_PART_INHERIT_TBL_PROPS
argument_list|(
literal|"hive.metastore.partition.inherit.table.properties"
argument_list|,
literal|""
argument_list|,
literal|"List of comma separated keys occurring in table properties which will get inherited to newly created partitions. \n"
operator|+
literal|"* implies all the keys will get inherited."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.FILTER_HOOK      */
annotation|@
name|Deprecated
name|METASTORE_FILTER_HOOK
argument_list|(
literal|"hive.metastore.filter.hook"
argument_list|,
literal|"org.apache.hadoop.hive.metastore.DefaultMetaStoreFilterHookImpl"
argument_list|,
literal|"Metastore hook class for filtering the metadata read results. If hive.security.authorization.manager"
operator|+
literal|"is set to instance of HiveAuthorizerFactory, then this value is ignored."
argument_list|)
block|,
name|FIRE_EVENTS_FOR_DML
argument_list|(
literal|"hive.metastore.dml.events"
argument_list|,
literal|false
argument_list|,
literal|"If true, the metastore will be asked"
operator|+
literal|" to fire events for DML operations"
argument_list|)
block|,
name|METASTORE_CLIENT_DROP_PARTITIONS_WITH_EXPRESSIONS
argument_list|(
literal|"hive.metastore.client.drop.partitions.using.expressions"
argument_list|,
literal|true
argument_list|,
literal|"Choose whether dropping partitions with HCatClient pushes the partition-predicate to the metastore, "
operator|+
literal|"or drops partitions iteratively"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.AGGREGATE_STATS_CACHE_ENABLED      */
annotation|@
name|Deprecated
name|METASTORE_AGGREGATE_STATS_CACHE_ENABLED
argument_list|(
literal|"hive.metastore.aggregate.stats.cache.enabled"
argument_list|,
literal|false
argument_list|,
literal|"Whether aggregate stats caching is enabled or not."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.AGGREGATE_STATS_CACHE_SIZE      */
annotation|@
name|Deprecated
name|METASTORE_AGGREGATE_STATS_CACHE_SIZE
argument_list|(
literal|"hive.metastore.aggregate.stats.cache.size"
argument_list|,
literal|10000
argument_list|,
literal|"Maximum number of aggregate stats nodes that we will place in the metastore aggregate stats cache."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.AGGREGATE_STATS_CACHE_MAX_PARTITIONS      */
annotation|@
name|Deprecated
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_PARTITIONS
argument_list|(
literal|"hive.metastore.aggregate.stats.cache.max.partitions"
argument_list|,
literal|10000
argument_list|,
literal|"Maximum number of partitions that are aggregated per cache node."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.AGGREGATE_STATS_CACHE_FPP      */
annotation|@
name|Deprecated
name|METASTORE_AGGREGATE_STATS_CACHE_FPP
argument_list|(
literal|"hive.metastore.aggregate.stats.cache.fpp"
argument_list|,
operator|(
name|float
operator|)
literal|0.01
argument_list|,
literal|"Maximum false positive probability for the Bloom Filter used in each aggregate stats cache node (default 1%)."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.AGGREGATE_STATS_CACHE_MAX_VARIANCE      */
annotation|@
name|Deprecated
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_VARIANCE
argument_list|(
literal|"hive.metastore.aggregate.stats.cache.max.variance"
argument_list|,
operator|(
name|float
operator|)
literal|0.01
argument_list|,
literal|"Maximum tolerable variance in number of partitions between a cached node and our request (default 1%)."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.AGGREGATE_STATS_CACHE_TTL      */
annotation|@
name|Deprecated
name|METASTORE_AGGREGATE_STATS_CACHE_TTL
argument_list|(
literal|"hive.metastore.aggregate.stats.cache.ttl"
argument_list|,
literal|"600s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Number of seconds for a cached node to be active in the cache before they become stale."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.AGGREGATE_STATS_CACHE_MAX_WRITER_WAIT      */
annotation|@
name|Deprecated
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_WRITER_WAIT
argument_list|(
literal|"hive.metastore.aggregate.stats.cache.max.writer.wait"
argument_list|,
literal|"5000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Number of milliseconds a writer will wait to acquire the writelock before giving up."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.AGGREGATE_STATS_CACHE_MAX_READER_WAIT      */
annotation|@
name|Deprecated
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_READER_WAIT
argument_list|(
literal|"hive.metastore.aggregate.stats.cache.max.reader.wait"
argument_list|,
literal|"1000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Number of milliseconds a reader will wait to acquire the readlock before giving up."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.AGGREGATE_STATS_CACHE_MAX_FULL      */
annotation|@
name|Deprecated
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_FULL
argument_list|(
literal|"hive.metastore.aggregate.stats.cache.max.full"
argument_list|,
operator|(
name|float
operator|)
literal|0.9
argument_list|,
literal|"Maximum cache full % after which the cache cleaner thread kicks in."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.AGGREGATE_STATS_CACHE_CLEAN_UNTIL      */
annotation|@
name|Deprecated
name|METASTORE_AGGREGATE_STATS_CACHE_CLEAN_UNTIL
argument_list|(
literal|"hive.metastore.aggregate.stats.cache.clean.until"
argument_list|,
operator|(
name|float
operator|)
literal|0.8
argument_list|,
literal|"The cleaner thread cleans until cache reaches this % full size."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.METRICS_ENABLED      */
annotation|@
name|Deprecated
name|METASTORE_METRICS
argument_list|(
literal|"hive.metastore.metrics.enabled"
argument_list|,
literal|false
argument_list|,
literal|"Enable metrics on the metastore."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.INIT_METADATA_COUNT_ENABLED      */
annotation|@
name|Deprecated
name|METASTORE_INIT_METADATA_COUNT_ENABLED
argument_list|(
literal|"hive.metastore.initial.metadata.count.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Enable a metadata count at metastore startup for metrics."
argument_list|)
block|,
comment|// Metastore SSL settings
comment|/**      * @deprecated Use MetastoreConf.USE_SSL      */
annotation|@
name|Deprecated
name|HIVE_METASTORE_USE_SSL
argument_list|(
literal|"hive.metastore.use.SSL"
argument_list|,
literal|false
argument_list|,
literal|"Set this to true for using SSL encryption in HMS server."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.SSL_KEYSTORE_PATH      */
annotation|@
name|Deprecated
name|HIVE_METASTORE_SSL_KEYSTORE_PATH
argument_list|(
literal|"hive.metastore.keystore.path"
argument_list|,
literal|""
argument_list|,
literal|"Metastore SSL certificate keystore location."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.SSL_KEYSTORE_PASSWORD      */
annotation|@
name|Deprecated
name|HIVE_METASTORE_SSL_KEYSTORE_PASSWORD
argument_list|(
literal|"hive.metastore.keystore.password"
argument_list|,
literal|""
argument_list|,
literal|"Metastore SSL certificate keystore password."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.SSL_TRUSTSTORE_PATH      */
annotation|@
name|Deprecated
name|HIVE_METASTORE_SSL_TRUSTSTORE_PATH
argument_list|(
literal|"hive.metastore.truststore.path"
argument_list|,
literal|""
argument_list|,
literal|"Metastore SSL certificate truststore location."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.SSL_TRUSTSTORE_PASSWORD      */
annotation|@
name|Deprecated
name|HIVE_METASTORE_SSL_TRUSTSTORE_PASSWORD
argument_list|(
literal|"hive.metastore.truststore.password"
argument_list|,
literal|""
argument_list|,
literal|"Metastore SSL certificate truststore password."
argument_list|)
block|,
comment|// Parameters for exporting metadata on table drop (requires the use of the)
comment|// org.apache.hadoop.hive.ql.parse.MetaDataExportListener preevent listener
comment|/**      * @deprecated Use MetastoreConf.METADATA_EXPORT_LOCATION      */
annotation|@
name|Deprecated
name|METADATA_EXPORT_LOCATION
argument_list|(
literal|"hive.metadata.export.location"
argument_list|,
literal|""
argument_list|,
literal|"When used in conjunction with the org.apache.hadoop.hive.ql.parse.MetaDataExportListener pre event listener, \n"
operator|+
literal|"it is the location to which the metadata will be exported. The default is an empty string, which results in the \n"
operator|+
literal|"metadata being exported to the current user's home directory on HDFS."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.MOVE_EXPORTED_METADATA_TO_TRASH      */
annotation|@
name|Deprecated
name|MOVE_EXPORTED_METADATA_TO_TRASH
argument_list|(
literal|"hive.metadata.move.exported.metadata.to.trash"
argument_list|,
literal|true
argument_list|,
literal|"When used in conjunction with the org.apache.hadoop.hive.ql.parse.MetaDataExportListener pre event listener, \n"
operator|+
literal|"this setting determines if the metadata that is exported will subsequently be moved to the user's trash directory \n"
operator|+
literal|"alongside the dropped table data. This ensures that the metadata will be cleaned up along with the dropped table data."
argument_list|)
block|,
comment|// CLI
name|CLIIGNOREERRORS
argument_list|(
literal|"hive.cli.errors.ignore"
argument_list|,
literal|false
argument_list|,
literal|""
argument_list|)
block|,
name|CLIPRINTCURRENTDB
argument_list|(
literal|"hive.cli.print.current.db"
argument_list|,
literal|false
argument_list|,
literal|"Whether to include the current database in the Hive prompt."
argument_list|)
block|,
name|CLIPROMPT
argument_list|(
literal|"hive.cli.prompt"
argument_list|,
literal|"hive"
argument_list|,
literal|"Command line prompt configuration value. Other hiveconf can be used in this configuration value. \n"
operator|+
literal|"Variable substitution will only be invoked at the Hive CLI startup."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.FS_HANDLER_CLS      */
annotation|@
name|Deprecated
name|HIVE_METASTORE_FS_HANDLER_CLS
argument_list|(
literal|"hive.metastore.fs.handler.class"
argument_list|,
literal|"org.apache.hadoop.hive.metastore.HiveMetaStoreFsImpl"
argument_list|,
literal|""
argument_list|)
block|,
comment|// Things we log in the jobconf
comment|// session identifier
name|HIVESESSIONID
argument_list|(
literal|"hive.session.id"
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|)
block|,
comment|// whether session is running in silent mode or not
name|HIVESESSIONSILENT
argument_list|(
literal|"hive.session.silent"
argument_list|,
literal|false
argument_list|,
literal|""
argument_list|)
block|,
name|HIVE_LOCAL_TIME_ZONE
argument_list|(
literal|"hive.local.time.zone"
argument_list|,
literal|"LOCAL"
argument_list|,
literal|"Sets the time-zone for displaying and interpreting time stamps. If this property value is set to\n"
operator|+
literal|"LOCAL, it is not specified, or it is not a correct time-zone, the system default time-zone will be\n "
operator|+
literal|"used instead. Time-zone IDs can be specified as region-based zone IDs (based on IANA time-zone data),\n"
operator|+
literal|"abbreviated zone IDs, or offset IDs."
argument_list|)
block|,
name|HIVE_SESSION_HISTORY_ENABLED
argument_list|(
literal|"hive.session.history.enabled"
argument_list|,
literal|false
argument_list|,
literal|"Whether to log Hive query, query plan, runtime statistics etc."
argument_list|)
block|,
name|HIVEQUERYSTRING
argument_list|(
literal|"hive.query.string"
argument_list|,
literal|""
argument_list|,
literal|"Query being executed (might be multiple per a session)"
argument_list|)
block|,
name|HIVEQUERYID
argument_list|(
literal|"hive.query.id"
argument_list|,
literal|""
argument_list|,
literal|"ID for query being executed (might be multiple per a session)"
argument_list|)
block|,
name|HIVEQUERYTAG
argument_list|(
literal|"hive.query.tag"
argument_list|,
literal|null
argument_list|,
literal|"Tag for the queries in the session. User can kill the queries with the tag "
operator|+
literal|"in another session. Currently there is no tag duplication check, user need to make sure his tag is unique. "
operator|+
literal|"Also 'kill query' needs to be issued to all HiveServer2 instances to proper kill the queries"
argument_list|)
block|,
name|HIVESPARKJOBNAMELENGTH
argument_list|(
literal|"hive.spark.jobname.length"
argument_list|,
literal|100000
argument_list|,
literal|"max jobname length for Hive on "
operator|+
literal|"Spark queries"
argument_list|)
block|,
name|HIVEJOBNAMELENGTH
argument_list|(
literal|"hive.jobname.length"
argument_list|,
literal|50
argument_list|,
literal|"max jobname length"
argument_list|)
block|,
comment|// hive jar
name|HIVEJAR
argument_list|(
literal|"hive.jar.path"
argument_list|,
literal|""
argument_list|,
literal|"The location of hive_cli.jar that is used when submitting jobs in a separate jvm."
argument_list|)
block|,
name|HIVEAUXJARS
argument_list|(
literal|"hive.aux.jars.path"
argument_list|,
literal|""
argument_list|,
literal|"The location of the plugin jars that contain implementations of user defined functions and serdes."
argument_list|)
block|,
comment|// reloadable jars
name|HIVERELOADABLEJARS
argument_list|(
literal|"hive.reloadable.aux.jars.path"
argument_list|,
literal|""
argument_list|,
literal|"The locations of the plugin jars, which can be a comma-separated folders or jars. Jars can be renewed\n"
operator|+
literal|"by executing reload command. And these jars can be "
operator|+
literal|"used as the auxiliary classes like creating a UDF or SerDe."
argument_list|)
block|,
comment|// hive added files and jars
name|HIVEADDEDFILES
argument_list|(
literal|"hive.added.files.path"
argument_list|,
literal|""
argument_list|,
literal|"This an internal parameter."
argument_list|)
block|,
name|HIVEADDEDJARS
argument_list|(
literal|"hive.added.jars.path"
argument_list|,
literal|""
argument_list|,
literal|"This an internal parameter."
argument_list|)
block|,
name|HIVEADDEDARCHIVES
argument_list|(
literal|"hive.added.archives.path"
argument_list|,
literal|""
argument_list|,
literal|"This an internal parameter."
argument_list|)
block|,
name|HIVEADDFILESUSEHDFSLOCATION
argument_list|(
literal|"hive.resource.use.hdfs.location"
argument_list|,
literal|true
argument_list|,
literal|"Reference HDFS based files/jars directly instead of "
operator|+
literal|"copy to session based HDFS scratch directory, to make distributed cache more useful."
argument_list|)
block|,
name|HIVE_CURRENT_DATABASE
argument_list|(
literal|"hive.current.database"
argument_list|,
literal|""
argument_list|,
literal|"Database name used by current session. Internal usage only."
argument_list|,
literal|true
argument_list|)
block|,
comment|// for hive script operator
name|HIVES_AUTO_PROGRESS_TIMEOUT
argument_list|(
literal|"hive.auto.progress.timeout"
argument_list|,
literal|"0s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"How long to run autoprogressor for the script/UDTF operators.\n"
operator|+
literal|"Set to 0 for forever."
argument_list|)
block|,
name|HIVESCRIPTAUTOPROGRESS
argument_list|(
literal|"hive.script.auto.progress"
argument_list|,
literal|false
argument_list|,
literal|"Whether Hive Transform/Map/Reduce Clause should automatically send progress information to TaskTracker \n"
operator|+
literal|"to avoid the task getting killed because of inactivity.  Hive sends progress information when the script is \n"
operator|+
literal|"outputting to stderr.  This option removes the need of periodically producing stderr messages, \n"
operator|+
literal|"but users should be cautious because this may prevent infinite loops in the scripts to be killed by TaskTracker."
argument_list|)
block|,
name|HIVESCRIPTIDENVVAR
argument_list|(
literal|"hive.script.operator.id.env.var"
argument_list|,
literal|"HIVE_SCRIPT_OPERATOR_ID"
argument_list|,
literal|"Name of the environment variable that holds the unique script operator ID in the user's \n"
operator|+
literal|"transform function (the custom mapper/reducer that the user has specified in the query)"
argument_list|)
block|,
name|HIVESCRIPTTRUNCATEENV
argument_list|(
literal|"hive.script.operator.truncate.env"
argument_list|,
literal|false
argument_list|,
literal|"Truncate each environment variable for external script in scripts operator to 20KB (to fit system limits)"
argument_list|)
block|,
name|HIVESCRIPT_ENV_BLACKLIST
argument_list|(
literal|"hive.script.operator.env.blacklist"
argument_list|,
literal|"hive.txn.valid.txns,hive.txn.tables.valid.writeids,hive.txn.valid.writeids,hive.script.operator.env.blacklist,hive.repl.current.table.write.id"
argument_list|,
literal|"Comma separated list of keys from the configuration file not to convert to environment "
operator|+
literal|"variables when invoking the script operator"
argument_list|)
block|,
name|HIVE_STRICT_CHECKS_ORDERBY_NO_LIMIT
argument_list|(
literal|"hive.strict.checks.orderby.no.limit"
argument_list|,
literal|false
argument_list|,
literal|"Enabling strict large query checks disallows the following:\n"
operator|+
literal|"  Orderby without limit.\n"
operator|+
literal|"Note that this check currently does not consider data size, only the query pattern."
argument_list|)
block|,
name|HIVE_STRICT_CHECKS_NO_PARTITION_FILTER
argument_list|(
literal|"hive.strict.checks.no.partition.filter"
argument_list|,
literal|false
argument_list|,
literal|"Enabling strict large query checks disallows the following:\n"
operator|+
literal|"  No partition being picked up for a query against partitioned table.\n"
operator|+
literal|"Note that this check currently does not consider data size, only the query pattern."
argument_list|)
block|,
name|HIVE_STRICT_CHECKS_TYPE_SAFETY
argument_list|(
literal|"hive.strict.checks.type.safety"
argument_list|,
literal|true
argument_list|,
literal|"Enabling strict type safety checks disallows the following:\n"
operator|+
literal|"  Comparing bigints and strings.\n"
operator|+
literal|"  Comparing bigints and doubles."
argument_list|)
block|,
name|HIVE_STRICT_CHECKS_CARTESIAN
argument_list|(
literal|"hive.strict.checks.cartesian.product"
argument_list|,
literal|false
argument_list|,
literal|"Enabling strict Cartesian join checks disallows the following:\n"
operator|+
literal|"  Cartesian product (cross join)."
argument_list|)
block|,
name|HIVE_STRICT_CHECKS_BUCKETING
argument_list|(
literal|"hive.strict.checks.bucketing"
argument_list|,
literal|true
argument_list|,
literal|"Enabling strict bucketing checks disallows the following:\n"
operator|+
literal|"  Load into bucketed tables."
argument_list|)
block|,
name|HIVE_LOAD_DATA_OWNER
argument_list|(
literal|"hive.load.data.owner"
argument_list|,
literal|""
argument_list|,
literal|"Set the owner of files loaded using load data in managed tables."
argument_list|)
block|,
annotation|@
name|Deprecated
name|HIVEMAPREDMODE
argument_list|(
literal|"hive.mapred.mode"
argument_list|,
literal|null
argument_list|,
literal|"Deprecated; use hive.strict.checks.* settings instead."
argument_list|)
block|,
name|HIVEALIAS
argument_list|(
literal|"hive.alias"
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEMAPSIDEAGGREGATE
argument_list|(
literal|"hive.map.aggr"
argument_list|,
literal|true
argument_list|,
literal|"Whether to use map-side aggregation in Hive Group By queries"
argument_list|)
block|,
name|HIVEGROUPBYSKEW
argument_list|(
literal|"hive.groupby.skewindata"
argument_list|,
literal|false
argument_list|,
literal|"Whether there is skew in data to optimize group by queries"
argument_list|)
block|,
name|HIVEJOINEMITINTERVAL
argument_list|(
literal|"hive.join.emit.interval"
argument_list|,
literal|1000
argument_list|,
literal|"How many rows in the right-most join operand Hive should buffer before emitting the join result."
argument_list|)
block|,
name|HIVEJOINCACHESIZE
argument_list|(
literal|"hive.join.cache.size"
argument_list|,
literal|25000
argument_list|,
literal|"How many rows in the joining tables (except the streaming table) should be cached in memory."
argument_list|)
block|,
name|HIVE_PUSH_RESIDUAL_INNER
argument_list|(
literal|"hive.join.inner.residual"
argument_list|,
literal|false
argument_list|,
literal|"Whether to push non-equi filter predicates within inner joins. This can improve efficiency in "
operator|+
literal|"the evaluation of certain joins, since we will not be emitting rows which are thrown away by "
operator|+
literal|"a Filter operator straight away. However, currently vectorization does not support them, thus "
operator|+
literal|"enabling it is only recommended when vectorization is disabled."
argument_list|)
block|,
name|HIVE_PTF_RANGECACHE_SIZE
argument_list|(
literal|"hive.ptf.rangecache.size"
argument_list|,
literal|10000
argument_list|,
literal|"Size of the cache used on reducer side, that stores boundaries of ranges within a PTF "
operator|+
literal|"partition. Used if a query specifies a RANGE type window including an orderby clause."
operator|+
literal|"Set this to 0 to disable this cache."
argument_list|)
block|,
comment|// CBO related
name|HIVE_CBO_ENABLED
argument_list|(
literal|"hive.cbo.enable"
argument_list|,
literal|true
argument_list|,
literal|"Flag to control enabling Cost Based Optimizations using Calcite framework."
argument_list|)
block|,
name|HIVE_CBO_CNF_NODES_LIMIT
argument_list|(
literal|"hive.cbo.cnf.maxnodes"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"When converting to conjunctive normal form (CNF), fail if"
operator|+
literal|"the expression exceeds this threshold; the threshold is expressed in terms of number of nodes (leaves and"
operator|+
literal|"interior nodes). -1 to not set up a threshold."
argument_list|)
block|,
name|HIVE_CBO_RETPATH_HIVEOP
argument_list|(
literal|"hive.cbo.returnpath.hiveop"
argument_list|,
literal|false
argument_list|,
literal|"Flag to control calcite plan to hive operator conversion"
argument_list|)
block|,
name|HIVE_CBO_EXTENDED_COST_MODEL
argument_list|(
literal|"hive.cbo.costmodel.extended"
argument_list|,
literal|false
argument_list|,
literal|"Flag to control enabling the extended cost model based on"
operator|+
literal|"CPU, IO and cardinality. Otherwise, the cost model is based on cardinality."
argument_list|)
block|,
name|HIVE_CBO_COST_MODEL_CPU
argument_list|(
literal|"hive.cbo.costmodel.cpu"
argument_list|,
literal|"0.000001"
argument_list|,
literal|"Default cost of a comparison"
argument_list|)
block|,
name|HIVE_CBO_COST_MODEL_NET
argument_list|(
literal|"hive.cbo.costmodel.network"
argument_list|,
literal|"150.0"
argument_list|,
literal|"Default cost of a transferring a byte over network;"
operator|+
literal|" expressed as multiple of CPU cost"
argument_list|)
block|,
name|HIVE_CBO_COST_MODEL_LFS_WRITE
argument_list|(
literal|"hive.cbo.costmodel.local.fs.write"
argument_list|,
literal|"4.0"
argument_list|,
literal|"Default cost of writing a byte to local FS;"
operator|+
literal|" expressed as multiple of NETWORK cost"
argument_list|)
block|,
name|HIVE_CBO_COST_MODEL_LFS_READ
argument_list|(
literal|"hive.cbo.costmodel.local.fs.read"
argument_list|,
literal|"4.0"
argument_list|,
literal|"Default cost of reading a byte from local FS;"
operator|+
literal|" expressed as multiple of NETWORK cost"
argument_list|)
block|,
name|HIVE_CBO_COST_MODEL_HDFS_WRITE
argument_list|(
literal|"hive.cbo.costmodel.hdfs.write"
argument_list|,
literal|"10.0"
argument_list|,
literal|"Default cost of writing a byte to HDFS;"
operator|+
literal|" expressed as multiple of Local FS write cost"
argument_list|)
block|,
name|HIVE_CBO_COST_MODEL_HDFS_READ
argument_list|(
literal|"hive.cbo.costmodel.hdfs.read"
argument_list|,
literal|"1.5"
argument_list|,
literal|"Default cost of reading a byte from HDFS;"
operator|+
literal|" expressed as multiple of Local FS read cost"
argument_list|)
block|,
name|HIVE_CBO_SHOW_WARNINGS
argument_list|(
literal|"hive.cbo.show.warnings"
argument_list|,
literal|true
argument_list|,
literal|"Toggle display of CBO warnings like missing column stats"
argument_list|)
block|,
name|HIVE_CBO_STATS_CORRELATED_MULTI_KEY_JOINS
argument_list|(
literal|"hive.cbo.stats.correlated.multi.key.joins"
argument_list|,
literal|true
argument_list|,
literal|"When CBO estimates output rows for a join involving multiple columns, the default behavior assumes"
operator|+
literal|"the columns are independent. Setting this flag to true will cause the estimator to assume"
operator|+
literal|"the columns are correlated."
argument_list|)
block|,
name|AGGR_JOIN_TRANSPOSE
argument_list|(
literal|"hive.transpose.aggr.join"
argument_list|,
literal|false
argument_list|,
literal|"push aggregates through join"
argument_list|)
block|,
name|SEMIJOIN_CONVERSION
argument_list|(
literal|"hive.optimize.semijoin.conversion"
argument_list|,
literal|true
argument_list|,
literal|"convert group by followed by inner equi join into semijoin"
argument_list|)
block|,
name|HIVE_COLUMN_ALIGNMENT
argument_list|(
literal|"hive.order.columnalignment"
argument_list|,
literal|true
argument_list|,
literal|"Flag to control whether we want to try to align"
operator|+
literal|"columns in operators such as Aggregate or Join so that we try to reduce the number of shuffling stages"
argument_list|)
block|,
comment|// materialized views
name|HIVE_MATERIALIZED_VIEW_ENABLE_AUTO_REWRITING
argument_list|(
literal|"hive.materializedview.rewriting"
argument_list|,
literal|true
argument_list|,
literal|"Whether to try to rewrite queries using the materialized views enabled for rewriting"
argument_list|)
block|,
name|HIVE_MATERIALIZED_VIEW_REWRITING_SELECTION_STRATEGY
argument_list|(
literal|"hive.materializedview.rewriting.strategy"
argument_list|,
literal|"heuristic"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"heuristic"
argument_list|,
literal|"costbased"
argument_list|)
argument_list|,
literal|"The strategy that should be used to cost and select the materialized view rewriting. \n"
operator|+
literal|"  heuristic: Always try to select the plan using the materialized view if rewriting produced one,"
operator|+
literal|"choosing the plan with lower cost among possible plans containing a materialized view\n"
operator|+
literal|"  costbased: Fully cost-based strategy, always use plan with lower cost, independently on whether "
operator|+
literal|"it uses a materialized view or not"
argument_list|)
block|,
name|HIVE_MATERIALIZED_VIEW_REWRITING_TIME_WINDOW
argument_list|(
literal|"hive.materializedview.rewriting.time.window"
argument_list|,
literal|"0min"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|,
literal|"Time window, specified in seconds, after which outdated materialized views become invalid for automatic query rewriting.\n"
operator|+
literal|"For instance, if more time than the value assigned to the property has passed since the materialized view "
operator|+
literal|"was created or rebuilt, and one of its source tables has changed since, the materialized view will not be "
operator|+
literal|"considered for rewriting. Default value 0 means that the materialized view cannot be "
operator|+
literal|"outdated to be used automatically in query rewriting. Value -1 means to skip this check."
argument_list|)
block|,
name|HIVE_MATERIALIZED_VIEW_REWRITING_INCREMENTAL
argument_list|(
literal|"hive.materializedview.rewriting.incremental"
argument_list|,
literal|false
argument_list|,
literal|"Whether to try to execute incremental rewritings based on outdated materializations and\n"
operator|+
literal|"current content of tables. Default value of true effectively amounts to enabling incremental\n"
operator|+
literal|"rebuild for the materializations too."
argument_list|)
block|,
name|HIVE_MATERIALIZED_VIEW_REBUILD_INCREMENTAL
argument_list|(
literal|"hive.materializedview.rebuild.incremental"
argument_list|,
literal|true
argument_list|,
literal|"Whether to try to execute incremental rebuild for the materialized views. Incremental rebuild\n"
operator|+
literal|"tries to modify the original materialization contents to reflect the latest changes to the\n"
operator|+
literal|"materialized view source tables, instead of rebuilding the contents fully. Incremental rebuild\n"
operator|+
literal|"is based on the materialized view algebraic incremental rewriting."
argument_list|)
block|,
name|HIVE_MATERIALIZED_VIEW_REBUILD_INCREMENTAL_FACTOR
argument_list|(
literal|"hive.materializedview.rebuild.incremental.factor"
argument_list|,
literal|0.1f
argument_list|,
literal|"The estimated cost of the resulting plan for incremental maintenance of materialization\n"
operator|+
literal|"with aggregations will be multiplied by this value. Reducing the value can be useful to\n"
operator|+
literal|"favour incremental rebuild over full rebuild."
argument_list|)
block|,
name|HIVE_MATERIALIZED_VIEW_FILE_FORMAT
argument_list|(
literal|"hive.materializedview.fileformat"
argument_list|,
literal|"ORC"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"none"
argument_list|,
literal|"TextFile"
argument_list|,
literal|"SequenceFile"
argument_list|,
literal|"RCfile"
argument_list|,
literal|"ORC"
argument_list|)
argument_list|,
literal|"Default file format for CREATE MATERIALIZED VIEW statement"
argument_list|)
block|,
name|HIVE_MATERIALIZED_VIEW_SERDE
argument_list|(
literal|"hive.materializedview.serde"
argument_list|,
literal|"org.apache.hadoop.hive.ql.io.orc.OrcSerde"
argument_list|,
literal|"Default SerDe used for materialized views"
argument_list|)
block|,
name|HIVE_ENABLE_JDBC_PUSHDOWN
argument_list|(
literal|"hive.jdbc.pushdown.enable"
argument_list|,
literal|true
argument_list|,
literal|"Flag to control enabling pushdown of operators into JDBC connection and subsequent SQL generation\n"
operator|+
literal|"using Calcite"
argument_list|)
block|,
name|HIVE_ENABLE_JDBC_SAFE_PUSHDOWN
argument_list|(
literal|"hive.jdbc.pushdown.safe.enable"
argument_list|,
literal|false
argument_list|,
literal|"Flag to control enabling pushdown of operators using Calcite that prevent splitting results\n"
operator|+
literal|"retrieval in the JDBC storage handler"
argument_list|)
block|,
comment|// hive.mapjoin.bucket.cache.size has been replaced by hive.smbjoin.cache.row,
comment|// need to remove by hive .13. Also, do not change default (see SMB operator)
name|HIVEMAPJOINBUCKETCACHESIZE
argument_list|(
literal|"hive.mapjoin.bucket.cache.size"
argument_list|,
literal|100
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEMAPJOINUSEOPTIMIZEDTABLE
argument_list|(
literal|"hive.mapjoin.optimized.hashtable"
argument_list|,
literal|true
argument_list|,
literal|"Whether Hive should use memory-optimized hash table for MapJoin.\n"
operator|+
literal|"Only works on Tez and Spark, because memory-optimized hashtable cannot be serialized."
argument_list|)
block|,
name|HIVEMAPJOINOPTIMIZEDTABLEPROBEPERCENT
argument_list|(
literal|"hive.mapjoin.optimized.hashtable.probe.percent"
argument_list|,
operator|(
name|float
operator|)
literal|0.5
argument_list|,
literal|"Probing space percentage of the optimized hashtable"
argument_list|)
block|,
name|HIVEUSEHYBRIDGRACEHASHJOIN
argument_list|(
literal|"hive.mapjoin.hybridgrace.hashtable"
argument_list|,
literal|false
argument_list|,
literal|"Whether to use hybrid"
operator|+
literal|"grace hash join as the join method for mapjoin. Tez only."
argument_list|)
block|,
name|HIVEHYBRIDGRACEHASHJOINMEMCHECKFREQ
argument_list|(
literal|"hive.mapjoin.hybridgrace.memcheckfrequency"
argument_list|,
literal|1024
argument_list|,
literal|"For "
operator|+
literal|"hybrid grace hash join, how often (how many rows apart) we check if memory is full. "
operator|+
literal|"This number should be power of 2."
argument_list|)
block|,
name|HIVEHYBRIDGRACEHASHJOINMINWBSIZE
argument_list|(
literal|"hive.mapjoin.hybridgrace.minwbsize"
argument_list|,
literal|524288
argument_list|,
literal|"For hybrid grace"
operator|+
literal|"Hash join, the minimum write buffer size used by optimized hashtable. Default is 512 KB."
argument_list|)
block|,
name|HIVEHYBRIDGRACEHASHJOINMINNUMPARTITIONS
argument_list|(
literal|"hive.mapjoin.hybridgrace.minnumpartitions"
argument_list|,
literal|16
argument_list|,
literal|"For"
operator|+
literal|"Hybrid grace hash join, the minimum number of partitions to create."
argument_list|)
block|,
name|HIVEHASHTABLEWBSIZE
argument_list|(
literal|"hive.mapjoin.optimized.hashtable.wbsize"
argument_list|,
literal|8
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
literal|"Optimized hashtable (see hive.mapjoin.optimized.hashtable) uses a chain of buffers to\n"
operator|+
literal|"store data. This is one buffer size. HT may be slightly faster if this is larger, but for small\n"
operator|+
literal|"joins unnecessary memory will be allocated and then trimmed."
argument_list|)
block|,
name|HIVEHYBRIDGRACEHASHJOINBLOOMFILTER
argument_list|(
literal|"hive.mapjoin.hybridgrace.bloomfilter"
argument_list|,
literal|true
argument_list|,
literal|"Whether to "
operator|+
literal|"use BloomFilter in Hybrid grace hash join to minimize unnecessary spilling."
argument_list|)
block|,
name|HIVEMAPJOINFULLOUTER
argument_list|(
literal|"hive.mapjoin.full.outer"
argument_list|,
literal|true
argument_list|,
literal|"Whether to use MapJoin for FULL OUTER JOINs."
argument_list|)
block|,
name|HIVE_TEST_MAPJOINFULLOUTER_OVERRIDE
argument_list|(
literal|"hive.test.mapjoin.full.outer.override"
argument_list|,
literal|"none"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"none"
argument_list|,
literal|"enable"
argument_list|,
literal|"disable"
argument_list|)
argument_list|,
literal|"internal use only, used to override the hive.mapjoin.full.outer\n"
operator|+
literal|"setting.  Using enable will force it on and disable will force it off.\n"
operator|+
literal|"The default none is do nothing, of course"
argument_list|,
literal|true
argument_list|)
block|,
name|HIVESMBJOINCACHEROWS
argument_list|(
literal|"hive.smbjoin.cache.rows"
argument_list|,
literal|10000
argument_list|,
literal|"How many rows with the same key value should be cached in memory per smb joined table."
argument_list|)
block|,
name|HIVEGROUPBYMAPINTERVAL
argument_list|(
literal|"hive.groupby.mapaggr.checkinterval"
argument_list|,
literal|100000
argument_list|,
literal|"Number of rows after which size of the grouping keys/aggregation classes is performed"
argument_list|)
block|,
name|HIVEMAPAGGRHASHMEMORY
argument_list|(
literal|"hive.map.aggr.hash.percentmemory"
argument_list|,
operator|(
name|float
operator|)
literal|0.5
argument_list|,
literal|"Portion of total memory to be used by map-side group aggregation hash table"
argument_list|)
block|,
name|HIVEMAPJOINFOLLOWEDBYMAPAGGRHASHMEMORY
argument_list|(
literal|"hive.mapjoin.followby.map.aggr.hash.percentmemory"
argument_list|,
operator|(
name|float
operator|)
literal|0.3
argument_list|,
literal|"Portion of total memory to be used by map-side group aggregation hash table, when this group by is followed by map join"
argument_list|)
block|,
name|HIVEMAPAGGRMEMORYTHRESHOLD
argument_list|(
literal|"hive.map.aggr.hash.force.flush.memory.threshold"
argument_list|,
operator|(
name|float
operator|)
literal|0.9
argument_list|,
literal|"The max memory to be used by map-side group aggregation hash table.\n"
operator|+
literal|"If the memory usage is higher than this number, force to flush data"
argument_list|)
block|,
name|HIVEMAPAGGRHASHMINREDUCTION
argument_list|(
literal|"hive.map.aggr.hash.min.reduction"
argument_list|,
operator|(
name|float
operator|)
literal|0.99
argument_list|,
literal|"Hash aggregation will be turned off if the ratio between hash  table size and input rows is bigger than this number. \n"
operator|+
literal|"Set to 1 to make sure hash aggregation is never turned off."
argument_list|)
block|,
name|HIVEMAPAGGRHASHMINREDUCTIONSTATSADJUST
argument_list|(
literal|"hive.map.aggr.hash.min.reduction.stats"
argument_list|,
literal|true
argument_list|,
literal|"Whether the value for hive.map.aggr.hash.min.reduction should be set statically using stats estimates. \n"
operator|+
literal|"If this is enabled, the default value for hive.map.aggr.hash.min.reduction is only used as an upper-bound\n"
operator|+
literal|"for the value set in the map-side group by operators."
argument_list|)
block|,
name|HIVEMULTIGROUPBYSINGLEREDUCER
argument_list|(
literal|"hive.multigroupby.singlereducer"
argument_list|,
literal|true
argument_list|,
literal|"Whether to optimize multi group by query to generate single M/R  job plan. If the multi group by query has \n"
operator|+
literal|"common group by keys, it will be optimized to generate single M/R job."
argument_list|)
block|,
name|HIVE_MAP_GROUPBY_SORT
argument_list|(
literal|"hive.map.groupby.sorted"
argument_list|,
literal|true
argument_list|,
literal|"If the bucketing/sorting properties of the table exactly match the grouping key, whether to perform \n"
operator|+
literal|"the group by in the mapper by using BucketizedHiveInputFormat. The only downside to this\n"
operator|+
literal|"is that it limits the number of mappers to the number of files."
argument_list|)
block|,
name|HIVE_DEFAULT_NULLS_LAST
argument_list|(
literal|"hive.default.nulls.last"
argument_list|,
literal|true
argument_list|,
literal|"Whether to set NULLS LAST as the default null ordering"
argument_list|)
block|,
name|HIVE_GROUPBY_POSITION_ALIAS
argument_list|(
literal|"hive.groupby.position.alias"
argument_list|,
literal|false
argument_list|,
literal|"Whether to enable using Column Position Alias in Group By"
argument_list|)
block|,
name|HIVE_ORDERBY_POSITION_ALIAS
argument_list|(
literal|"hive.orderby.position.alias"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable using Column Position Alias in Order By"
argument_list|)
block|,
annotation|@
name|Deprecated
name|HIVE_GROUPBY_ORDERBY_POSITION_ALIAS
argument_list|(
literal|"hive.groupby.orderby.position.alias"
argument_list|,
literal|false
argument_list|,
literal|"Whether to enable using Column Position Alias in Group By or Order By (deprecated).\n"
operator|+
literal|"Use "
operator|+
name|HIVE_ORDERBY_POSITION_ALIAS
operator|.
name|varname
operator|+
literal|" or "
operator|+
name|HIVE_GROUPBY_POSITION_ALIAS
operator|.
name|varname
operator|+
literal|" instead"
argument_list|)
block|,
name|HIVE_NEW_JOB_GROUPING_SET_CARDINALITY
argument_list|(
literal|"hive.new.job.grouping.set.cardinality"
argument_list|,
literal|30
argument_list|,
literal|"Whether a new map-reduce job should be launched for grouping sets/rollups/cubes.\n"
operator|+
literal|"For a query like: select a, b, c, count(1) from T group by a, b, c with rollup;\n"
operator|+
literal|"4 rows are created per row: (a, b, c), (a, b, null), (a, null, null), (null, null, null).\n"
operator|+
literal|"This can lead to explosion across map-reduce boundary if the cardinality of T is very high,\n"
operator|+
literal|"and map-side aggregation does not do a very good job. \n"
operator|+
literal|"\n"
operator|+
literal|"This parameter decides if Hive should add an additional map-reduce job. If the grouping set\n"
operator|+
literal|"cardinality (4 in the example above), is more than this value, a new MR job is added under the\n"
operator|+
literal|"assumption that the original group by will reduce the data size."
argument_list|)
block|,
name|HIVE_GROUPBY_LIMIT_EXTRASTEP
argument_list|(
literal|"hive.groupby.limit.extrastep"
argument_list|,
literal|true
argument_list|,
literal|"This parameter decides if Hive should \n"
operator|+
literal|"create new MR job for sorting final output"
argument_list|)
block|,
comment|// Max file num and size used to do a single copy (after that, distcp is used)
name|HIVE_EXEC_COPYFILE_MAXNUMFILES
argument_list|(
literal|"hive.exec.copyfile.maxnumfiles"
argument_list|,
literal|1L
argument_list|,
literal|"Maximum number of files Hive uses to do sequential HDFS copies between directories."
operator|+
literal|"Distributed copies (distcp) will be used instead for larger numbers of files so that copies can be done faster."
argument_list|)
block|,
name|HIVE_EXEC_COPYFILE_MAXSIZE
argument_list|(
literal|"hive.exec.copyfile.maxsize"
argument_list|,
literal|32L
operator|*
literal|1024
operator|*
literal|1024
comment|/*32M*/
argument_list|,
literal|"Maximum file size (in bytes) that Hive uses to do single HDFS copies between directories."
operator|+
literal|"Distributed copies (distcp) will be used instead for bigger files so that copies can be done faster."
argument_list|)
block|,
comment|// for hive udtf operator
name|HIVEUDTFAUTOPROGRESS
argument_list|(
literal|"hive.udtf.auto.progress"
argument_list|,
literal|false
argument_list|,
literal|"Whether Hive should automatically send progress information to TaskTracker \n"
operator|+
literal|"when using UDTF's to prevent the task getting killed because of inactivity.  Users should be cautious \n"
operator|+
literal|"because this may prevent TaskTracker from killing tasks with infinite loops."
argument_list|)
block|,
name|HIVEDEFAULTFILEFORMAT
argument_list|(
literal|"hive.default.fileformat"
argument_list|,
literal|"TextFile"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"TextFile"
argument_list|,
literal|"SequenceFile"
argument_list|,
literal|"RCfile"
argument_list|,
literal|"ORC"
argument_list|,
literal|"parquet"
argument_list|)
argument_list|,
literal|"Default file format for CREATE TABLE statement. Users can explicitly override it by CREATE TABLE ... STORED AS [FORMAT]"
argument_list|)
block|,
name|HIVEDEFAULTMANAGEDFILEFORMAT
argument_list|(
literal|"hive.default.fileformat.managed"
argument_list|,
literal|"none"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"none"
argument_list|,
literal|"TextFile"
argument_list|,
literal|"SequenceFile"
argument_list|,
literal|"RCfile"
argument_list|,
literal|"ORC"
argument_list|,
literal|"parquet"
argument_list|)
argument_list|,
literal|"Default file format for CREATE TABLE statement applied to managed tables only. External tables will be \n"
operator|+
literal|"created with format specified by hive.default.fileformat. Leaving this null will result in using hive.default.fileformat \n"
operator|+
literal|"for all tables."
argument_list|)
block|,
name|HIVEQUERYRESULTFILEFORMAT
argument_list|(
literal|"hive.query.result.fileformat"
argument_list|,
literal|"SequenceFile"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"TextFile"
argument_list|,
literal|"SequenceFile"
argument_list|,
literal|"RCfile"
argument_list|,
literal|"Llap"
argument_list|)
argument_list|,
literal|"Default file format for storing result of the query."
argument_list|)
block|,
name|HIVECHECKFILEFORMAT
argument_list|(
literal|"hive.fileformat.check"
argument_list|,
literal|true
argument_list|,
literal|"Whether to check file format or not when loading data files"
argument_list|)
block|,
comment|// default serde for rcfile
name|HIVEDEFAULTRCFILESERDE
argument_list|(
literal|"hive.default.rcfile.serde"
argument_list|,
literal|"org.apache.hadoop.hive.serde2.columnar.LazyBinaryColumnarSerDe"
argument_list|,
literal|"The default SerDe Hive will use for the RCFile format"
argument_list|)
block|,
name|HIVEDEFAULTSERDE
argument_list|(
literal|"hive.default.serde"
argument_list|,
literal|"org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe"
argument_list|,
literal|"The default SerDe Hive will use for storage formats that do not specify a SerDe."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.SERDES_USING_METASTORE_FOR_SCHEMA      */
annotation|@
name|Deprecated
name|SERDESUSINGMETASTOREFORSCHEMA
argument_list|(
literal|"hive.serdes.using.metastore.for.schema"
argument_list|,
literal|"org.apache.hadoop.hive.ql.io.orc.OrcSerde,"
operator|+
literal|"org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe,"
operator|+
literal|"org.apache.hadoop.hive.serde2.columnar.ColumnarSerDe,"
operator|+
literal|"org.apache.hadoop.hive.serde2.dynamic_type.DynamicSerDe,"
operator|+
literal|"org.apache.hadoop.hive.serde2.MetadataTypedColumnsetSerDe,"
operator|+
literal|"org.apache.hadoop.hive.serde2.columnar.LazyBinaryColumnarSerDe,"
operator|+
literal|"org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe,"
operator|+
literal|"org.apache.hadoop.hive.serde2.lazybinary.LazyBinarySerDe,"
operator|+
literal|"org.apache.hadoop.hive.serde2.OpenCSVSerde"
argument_list|,
literal|"SerDes retrieving schema from metastore. This is an internal parameter."
argument_list|)
block|,
annotation|@
name|Deprecated
name|HIVE_LEGACY_SCHEMA_FOR_ALL_SERDES
argument_list|(
literal|"hive.legacy.schema.for.all.serdes"
argument_list|,
literal|false
argument_list|,
literal|"A backward compatibility setting for external metastore users that do not handle \n"
operator|+
name|SERDESUSINGMETASTOREFORSCHEMA
operator|.
name|varname
operator|+
literal|" correctly. This may be removed at any time."
argument_list|)
block|,
name|HIVEHISTORYFILELOC
argument_list|(
literal|"hive.querylog.location"
argument_list|,
literal|"${system:java.io.tmpdir}"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"${system:user.name}"
argument_list|,
literal|"Location of Hive run time structured log file"
argument_list|)
block|,
name|HIVE_LOG_INCREMENTAL_PLAN_PROGRESS
argument_list|(
literal|"hive.querylog.enable.plan.progress"
argument_list|,
literal|true
argument_list|,
literal|"Whether to log the plan's progress every time a job's progress is checked.\n"
operator|+
literal|"These logs are written to the location specified by hive.querylog.location"
argument_list|)
block|,
name|HIVE_LOG_INCREMENTAL_PLAN_PROGRESS_INTERVAL
argument_list|(
literal|"hive.querylog.plan.progress.interval"
argument_list|,
literal|"60000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"The interval to wait between logging the plan's progress.\n"
operator|+
literal|"If there is a whole number percentage change in the progress of the mappers or the reducers,\n"
operator|+
literal|"the progress is logged regardless of this value.\n"
operator|+
literal|"The actual interval will be the ceiling of (this value divided by the value of\n"
operator|+
literal|"hive.exec.counters.pull.interval) multiplied by the value of hive.exec.counters.pull.interval\n"
operator|+
literal|"I.e. if it is not divide evenly by the value of hive.exec.counters.pull.interval it will be\n"
operator|+
literal|"logged less frequently than specified.\n"
operator|+
literal|"This only has an effect if hive.querylog.enable.plan.progress is set to true."
argument_list|)
block|,
name|HIVESCRIPTSERDE
argument_list|(
literal|"hive.script.serde"
argument_list|,
literal|"org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe"
argument_list|,
literal|"The default SerDe for transmitting input data to and reading output data from the user scripts. "
argument_list|)
block|,
name|HIVESCRIPTRECORDREADER
argument_list|(
literal|"hive.script.recordreader"
argument_list|,
literal|"org.apache.hadoop.hive.ql.exec.TextRecordReader"
argument_list|,
literal|"The default record reader for reading data from the user scripts. "
argument_list|)
block|,
name|HIVESCRIPTRECORDWRITER
argument_list|(
literal|"hive.script.recordwriter"
argument_list|,
literal|"org.apache.hadoop.hive.ql.exec.TextRecordWriter"
argument_list|,
literal|"The default record writer for writing data to the user scripts. "
argument_list|)
block|,
name|HIVESCRIPTESCAPE
argument_list|(
literal|"hive.transform.escape.input"
argument_list|,
literal|false
argument_list|,
literal|"This adds an option to escape special chars (newlines, carriage returns and\n"
operator|+
literal|"tabs) when they are passed to the user script. This is useful if the Hive tables\n"
operator|+
literal|"can contain data that contains special characters."
argument_list|)
block|,
name|HIVEBINARYRECORDMAX
argument_list|(
literal|"hive.binary.record.max.length"
argument_list|,
literal|1000
argument_list|,
literal|"Read from a binary stream and treat each hive.binary.record.max.length bytes as a record. \n"
operator|+
literal|"The last record before the end of stream can have less than hive.binary.record.max.length bytes"
argument_list|)
block|,
name|HIVEHADOOPMAXMEM
argument_list|(
literal|"hive.mapred.local.mem"
argument_list|,
literal|0
argument_list|,
literal|"mapper/reducer memory in local mode"
argument_list|)
block|,
comment|//small table file size
name|HIVESMALLTABLESFILESIZE
argument_list|(
literal|"hive.mapjoin.smalltable.filesize"
argument_list|,
literal|25000000L
argument_list|,
literal|"The threshold for the input file size of the small tables; if the file size is smaller \n"
operator|+
literal|"than this threshold, it will try to convert the common join into map join"
argument_list|)
block|,
name|HIVE_SCHEMA_EVOLUTION
argument_list|(
literal|"hive.exec.schema.evolution"
argument_list|,
literal|true
argument_list|,
literal|"Use schema evolution to convert self-describing file format's data to the schema desired by the reader."
argument_list|)
block|,
name|HIVE_ORC_FORCE_POSITIONAL_SCHEMA_EVOLUTION
argument_list|(
literal|"orc.force.positional.evolution"
argument_list|,
literal|true
argument_list|,
literal|"Whether to use column position based schema evolution or not (as opposed to column name based evolution)"
argument_list|)
block|,
comment|/** Don't use this directly - use AcidUtils! */
name|HIVE_TRANSACTIONAL_TABLE_SCAN
argument_list|(
literal|"hive.transactional.table.scan"
argument_list|,
literal|false
argument_list|,
literal|"internal usage only -- do transaction (ACID or insert-only) table scan."
argument_list|,
literal|true
argument_list|)
block|,
name|HIVE_TRANSACTIONAL_NUM_EVENTS_IN_MEMORY
argument_list|(
literal|"hive.transactional.events.mem"
argument_list|,
literal|10000000
argument_list|,
literal|"Vectorized ACID readers can often load all the delete events from all the delete deltas\n"
operator|+
literal|"into memory to optimize for performance. To prevent out-of-memory errors, this is a rough heuristic\n"
operator|+
literal|"that limits the total number of delete events that can be loaded into memory at once.\n"
operator|+
literal|"Roughly it has been set to 10 million delete events per bucket (~160 MB).\n"
argument_list|)
block|,
name|FILTER_DELETE_EVENTS
argument_list|(
literal|"hive.txn.filter.delete.events"
argument_list|,
literal|true
argument_list|,
literal|"If true, VectorizedOrcAcidRowBatchReader will compute min/max "
operator|+
literal|"ROW__ID for the split and only load delete events in that range.\n"
argument_list|)
block|,
name|HIVESAMPLERANDOMNUM
argument_list|(
literal|"hive.sample.seednumber"
argument_list|,
literal|0
argument_list|,
literal|"A number used to percentage sampling. By changing this number, user will change the subsets of data sampled."
argument_list|)
block|,
comment|// test mode in hive mode
name|HIVETESTMODE
argument_list|(
literal|"hive.test.mode"
argument_list|,
literal|false
argument_list|,
literal|"Whether Hive is running in test mode. If yes, it turns on sampling and prefixes the output tablename."
argument_list|,
literal|false
argument_list|)
block|,
name|HIVEEXIMTESTMODE
argument_list|(
literal|"hive.exim.test.mode"
argument_list|,
literal|false
argument_list|,
literal|"The subset of test mode that only enables custom path handling for ExIm."
argument_list|,
literal|false
argument_list|)
block|,
name|HIVETESTMODEPREFIX
argument_list|(
literal|"hive.test.mode.prefix"
argument_list|,
literal|"test_"
argument_list|,
literal|"In test mode, specifies prefixes for the output table"
argument_list|,
literal|false
argument_list|)
block|,
name|HIVETESTMODESAMPLEFREQ
argument_list|(
literal|"hive.test.mode.samplefreq"
argument_list|,
literal|32
argument_list|,
literal|"In test mode, specifies sampling frequency for table, which is not bucketed,\n"
operator|+
literal|"For example, the following query:\n"
operator|+
literal|"  INSERT OVERWRITE TABLE dest SELECT col1 from src\n"
operator|+
literal|"would be converted to\n"
operator|+
literal|"  INSERT OVERWRITE TABLE test_dest\n"
operator|+
literal|"  SELECT col1 from src TABLESAMPLE (BUCKET 1 out of 32 on rand(1))"
argument_list|,
literal|false
argument_list|)
block|,
name|HIVETESTMODENOSAMPLE
argument_list|(
literal|"hive.test.mode.nosamplelist"
argument_list|,
literal|""
argument_list|,
literal|"In test mode, specifies comma separated table names which would not apply sampling"
argument_list|,
literal|false
argument_list|)
block|,
name|HIVETESTMODEDUMMYSTATAGGR
argument_list|(
literal|"hive.test.dummystats.aggregator"
argument_list|,
literal|""
argument_list|,
literal|"internal variable for test"
argument_list|,
literal|false
argument_list|)
block|,
name|HIVETESTMODEDUMMYSTATPUB
argument_list|(
literal|"hive.test.dummystats.publisher"
argument_list|,
literal|""
argument_list|,
literal|"internal variable for test"
argument_list|,
literal|false
argument_list|)
block|,
name|HIVETESTCURRENTTIMESTAMP
argument_list|(
literal|"hive.test.currenttimestamp"
argument_list|,
literal|null
argument_list|,
literal|"current timestamp for test"
argument_list|,
literal|false
argument_list|)
block|,
name|HIVETESTMODEROLLBACKTXN
argument_list|(
literal|"hive.test.rollbacktxn"
argument_list|,
literal|false
argument_list|,
literal|"For testing only.  Will mark every ACID transaction aborted"
argument_list|,
literal|false
argument_list|)
block|,
name|HIVETESTMODEFAILCOMPACTION
argument_list|(
literal|"hive.test.fail.compaction"
argument_list|,
literal|false
argument_list|,
literal|"For testing only.  Will cause CompactorMR to fail."
argument_list|,
literal|false
argument_list|)
block|,
name|HIVETESTMODEFAILHEARTBEATER
argument_list|(
literal|"hive.test.fail.heartbeater"
argument_list|,
literal|false
argument_list|,
literal|"For testing only.  Will cause Heartbeater to fail."
argument_list|,
literal|false
argument_list|)
block|,
name|TESTMODE_BUCKET_CODEC_VERSION
argument_list|(
literal|"hive.test.bucketcodec.version"
argument_list|,
literal|1
argument_list|,
literal|"For testing only.  Will make ACID subsystem write RecordIdentifier.bucketId in specified\n"
operator|+
literal|"format"
argument_list|,
literal|false
argument_list|)
block|,
name|HIVETESTMODEACIDKEYIDXSKIP
argument_list|(
literal|"hive.test.acid.key.index.skip"
argument_list|,
literal|false
argument_list|,
literal|"For testing only. OrcRecordUpdater will skip "
operator|+
literal|"generation of the hive.acid.key.index"
argument_list|,
literal|false
argument_list|)
block|,
name|HIVEMERGEMAPFILES
argument_list|(
literal|"hive.merge.mapfiles"
argument_list|,
literal|true
argument_list|,
literal|"Merge small files at the end of a map-only job"
argument_list|)
block|,
name|HIVEMERGEMAPREDFILES
argument_list|(
literal|"hive.merge.mapredfiles"
argument_list|,
literal|false
argument_list|,
literal|"Merge small files at the end of a map-reduce job"
argument_list|)
block|,
name|HIVEMERGETEZFILES
argument_list|(
literal|"hive.merge.tezfiles"
argument_list|,
literal|false
argument_list|,
literal|"Merge small files at the end of a Tez DAG"
argument_list|)
block|,
name|HIVEMERGESPARKFILES
argument_list|(
literal|"hive.merge.sparkfiles"
argument_list|,
literal|false
argument_list|,
literal|"Merge small files at the end of a Spark DAG Transformation"
argument_list|)
block|,
name|HIVEMERGEMAPFILESSIZE
argument_list|(
literal|"hive.merge.size.per.task"
argument_list|,
call|(
name|long
call|)
argument_list|(
literal|256
operator|*
literal|1000
operator|*
literal|1000
argument_list|)
argument_list|,
literal|"Size of merged files at the end of the job"
argument_list|)
block|,
name|HIVEMERGEMAPFILESAVGSIZE
argument_list|(
literal|"hive.merge.smallfiles.avgsize"
argument_list|,
call|(
name|long
call|)
argument_list|(
literal|16
operator|*
literal|1000
operator|*
literal|1000
argument_list|)
argument_list|,
literal|"When the average output file size of a job is less than this number, Hive will start an additional \n"
operator|+
literal|"map-reduce job to merge the output files into bigger files. This is only done for map-only jobs \n"
operator|+
literal|"if hive.merge.mapfiles is true, and for map-reduce jobs if hive.merge.mapredfiles is true."
argument_list|)
block|,
name|HIVEMERGERCFILEBLOCKLEVEL
argument_list|(
literal|"hive.merge.rcfile.block.level"
argument_list|,
literal|true
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEMERGEORCFILESTRIPELEVEL
argument_list|(
literal|"hive.merge.orcfile.stripe.level"
argument_list|,
literal|true
argument_list|,
literal|"When hive.merge.mapfiles, hive.merge.mapredfiles or hive.merge.tezfiles is enabled\n"
operator|+
literal|"while writing a table with ORC file format, enabling this config will do stripe-level\n"
operator|+
literal|"fast merge for small ORC files. Note that enabling this config will not honor the\n"
operator|+
literal|"padding tolerance config (hive.exec.orc.block.padding.tolerance)."
argument_list|)
block|,
name|HIVE_ORC_CODEC_POOL
argument_list|(
literal|"hive.use.orc.codec.pool"
argument_list|,
literal|false
argument_list|,
literal|"Whether to use codec pool in ORC. Disable if there are bugs with codec reuse."
argument_list|)
block|,
name|HIVEUSEEXPLICITRCFILEHEADER
argument_list|(
literal|"hive.exec.rcfile.use.explicit.header"
argument_list|,
literal|true
argument_list|,
literal|"If this is set the header for RCFiles will simply be RCF.  If this is not\n"
operator|+
literal|"set the header will be that borrowed from sequence files, e.g. SEQ- followed\n"
operator|+
literal|"by the input and output RCFile formats."
argument_list|)
block|,
name|HIVEUSERCFILESYNCCACHE
argument_list|(
literal|"hive.exec.rcfile.use.sync.cache"
argument_list|,
literal|true
argument_list|,
literal|""
argument_list|)
block|,
name|HIVE_RCFILE_RECORD_INTERVAL
argument_list|(
literal|"hive.io.rcfile.record.interval"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|""
argument_list|)
block|,
name|HIVE_RCFILE_COLUMN_NUMBER_CONF
argument_list|(
literal|"hive.io.rcfile.column.number.conf"
argument_list|,
literal|0
argument_list|,
literal|""
argument_list|)
block|,
name|HIVE_RCFILE_TOLERATE_CORRUPTIONS
argument_list|(
literal|"hive.io.rcfile.tolerate.corruptions"
argument_list|,
literal|false
argument_list|,
literal|""
argument_list|)
block|,
name|HIVE_RCFILE_RECORD_BUFFER_SIZE
argument_list|(
literal|"hive.io.rcfile.record.buffer.size"
argument_list|,
literal|4194304
argument_list|,
literal|""
argument_list|)
block|,
comment|// 4M
name|PARQUET_MEMORY_POOL_RATIO
argument_list|(
literal|"parquet.memory.pool.ratio"
argument_list|,
literal|0.5f
argument_list|,
literal|"Maximum fraction of heap that can be used by Parquet file writers in one task.\n"
operator|+
literal|"It is for avoiding OutOfMemory error in tasks. Work with Parquet 1.6.0 and above.\n"
operator|+
literal|"This config parameter is defined in Parquet, so that it does not start with 'hive.'."
argument_list|)
block|,
name|HIVE_PARQUET_TIMESTAMP_SKIP_CONVERSION
argument_list|(
literal|"hive.parquet.timestamp.skip.conversion"
argument_list|,
literal|true
argument_list|,
literal|"Current Hive implementation of parquet stores timestamps to UTC, this flag allows skipping of the conversion"
operator|+
literal|"on reading parquet files from other tools"
argument_list|)
block|,
name|HIVE_PARQUET_DATE_PROLEPTIC_GREGORIAN
argument_list|(
literal|"hive.parquet.date.proleptic.gregorian"
argument_list|,
literal|false
argument_list|,
literal|"Should we write date using the proleptic Gregorian calendar instead of the hybrid Julian Gregorian?\n"
operator|+
literal|"Hybrid is the default."
argument_list|)
block|,
name|HIVE_PARQUET_DATE_PROLEPTIC_GREGORIAN_DEFAULT
argument_list|(
literal|"hive.parquet.date.proleptic.gregorian.default"
argument_list|,
literal|false
argument_list|,
literal|"This value controls whether date type in Parquet files was written using the hybrid or proleptic\n"
operator|+
literal|"calendar. Hybrid is the default."
argument_list|)
block|,
name|HIVE_AVRO_TIMESTAMP_SKIP_CONVERSION
argument_list|(
literal|"hive.avro.timestamp.skip.conversion"
argument_list|,
literal|false
argument_list|,
literal|"Some older Hive implementations (pre-3.1) wrote Avro timestamps in a UTC-normalized"
operator|+
literal|"manner, while from version 3.1 until now Hive wrote time zone agnostic timestamps. "
operator|+
literal|"Setting this flag to true will treat legacy timestamps as time zone agnostic. Setting "
operator|+
literal|"it to false will treat legacy timestamps as UTC-normalized. This flag will not affect "
operator|+
literal|"timestamps written after this change."
argument_list|)
block|,
name|HIVE_AVRO_PROLEPTIC_GREGORIAN
argument_list|(
literal|"hive.avro.proleptic.gregorian"
argument_list|,
literal|false
argument_list|,
literal|"Should we write date and timestamp using the proleptic Gregorian calendar instead of the hybrid Julian Gregorian?\n"
operator|+
literal|"Hybrid is the default."
argument_list|)
block|,
name|HIVE_AVRO_PROLEPTIC_GREGORIAN_DEFAULT
argument_list|(
literal|"hive.avro.proleptic.gregorian.default"
argument_list|,
literal|false
argument_list|,
literal|"This value controls whether date and timestamp type in Avro files was written using the hybrid or proleptic\n"
operator|+
literal|"calendar. Hybrid is the default."
argument_list|)
block|,
name|HIVE_INT_TIMESTAMP_CONVERSION_IN_SECONDS
argument_list|(
literal|"hive.int.timestamp.conversion.in.seconds"
argument_list|,
literal|false
argument_list|,
literal|"Boolean/tinyint/smallint/int/bigint value is interpreted as milliseconds during the timestamp conversion.\n"
operator|+
literal|"Set this flag to true to interpret the value as seconds to be consistent with float/double."
argument_list|)
block|,
name|HIVE_PARQUET_WRITE_INT64_TIMESTAMP
argument_list|(
literal|"hive.parquet.write.int64.timestamp"
argument_list|,
literal|false
argument_list|,
literal|"Write parquet timestamps as int64/LogicalTypes instead of int96/OriginalTypes. Note:"
operator|+
literal|"Timestamps will be time zone agnostic (NEVER converted to a different time zone)."
argument_list|)
block|,
name|HIVE_PARQUET_TIMESTAMP_TIME_UNIT
argument_list|(
literal|"hive.parquet.timestamp.time.unit"
argument_list|,
literal|"micros"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"nanos"
argument_list|,
literal|"micros"
argument_list|,
literal|"millis"
argument_list|)
argument_list|,
literal|"Store parquet int64/LogicalTypes timestamps in this time unit."
argument_list|)
block|,
name|HIVE_ORC_BASE_DELTA_RATIO
argument_list|(
literal|"hive.exec.orc.base.delta.ratio"
argument_list|,
literal|8
argument_list|,
literal|"The ratio of base writer and\n"
operator|+
literal|"delta writer in terms of STRIPE_SIZE and BUFFER_SIZE."
argument_list|)
block|,
name|HIVE_ORC_DELTA_STREAMING_OPTIMIZATIONS_ENABLED
argument_list|(
literal|"hive.exec.orc.delta.streaming.optimizations.enabled"
argument_list|,
literal|false
argument_list|,
literal|"Whether to enable streaming optimizations for ORC delta files. This will disable ORC's internal indexes,\n"
operator|+
literal|"disable compression, enable fast encoding and disable dictionary encoding."
argument_list|)
block|,
name|HIVE_ORC_SPLIT_STRATEGY
argument_list|(
literal|"hive.exec.orc.split.strategy"
argument_list|,
literal|"HYBRID"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"HYBRID"
argument_list|,
literal|"BI"
argument_list|,
literal|"ETL"
argument_list|)
argument_list|,
literal|"This is not a user level config. BI strategy is used when the requirement is to spend less time in split generation"
operator|+
literal|" as opposed to query execution (split generation does not read or cache file footers)."
operator|+
literal|" ETL strategy is used when spending little more time in split generation is acceptable"
operator|+
literal|" (split generation reads and caches file footers). HYBRID chooses between the above strategies"
operator|+
literal|" based on heuristics."
argument_list|)
block|,
name|HIVE_ORC_BLOB_STORAGE_SPLIT_SIZE
argument_list|(
literal|"hive.exec.orc.blob.storage.split.size"
argument_list|,
literal|128L
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
literal|"When blob storage is used, BI split strategy does not have block locations for splitting orc files.\n"
operator|+
literal|"In such cases, split generation will use this config to split orc file"
argument_list|)
block|,
name|HIVE_ORC_WRITER_LLAP_MEMORY_MANAGER_ENABLED
argument_list|(
literal|"hive.exec.orc.writer.llap.memory.manager.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Whether orc writers should use llap-aware memory manager. LLAP aware memory manager will use memory\n"
operator|+
literal|"per executor instead of entire heap memory when concurrent orc writers are involved. This will let\n"
operator|+
literal|"task fragments to use memory within its limit (memory per executor) when performing ETL in LLAP."
argument_list|)
block|,
comment|// hive streaming ingest settings
name|HIVE_STREAMING_AUTO_FLUSH_ENABLED
argument_list|(
literal|"hive.streaming.auto.flush.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable memory \n"
operator|+
literal|"monitoring and automatic flushing of open record updaters during streaming ingest. This is an expert level \n"
operator|+
literal|"setting and disabling this may have severe performance impact under memory pressure."
argument_list|)
block|,
name|HIVE_HEAP_MEMORY_MONITOR_USAGE_THRESHOLD
argument_list|(
literal|"hive.heap.memory.monitor.usage.threshold"
argument_list|,
literal|0.7f
argument_list|,
literal|"Hive streaming does automatic memory management across all open record writers. This threshold will let the \n"
operator|+
literal|"memory monitor take an action (flush open files) when heap memory usage exceeded this threshold."
argument_list|)
block|,
name|HIVE_STREAMING_AUTO_FLUSH_CHECK_INTERVAL_SIZE
argument_list|(
literal|"hive.streaming.auto.flush.check.interval.size"
argument_list|,
literal|"100Mb"
argument_list|,
operator|new
name|SizeValidator
argument_list|()
argument_list|,
literal|"Hive streaming ingest has auto flush mechanism to flush all open record updaters under memory pressure.\n"
operator|+
literal|"When memory usage exceed hive.heap.memory.monitor.default.usage.threshold, the auto-flush mechanism will \n"
operator|+
literal|"wait until this size (default 100Mb) of records are ingested before triggering flush."
argument_list|)
block|,
name|HIVE_CLASSLOADER_SHADE_PREFIX
argument_list|(
literal|"hive.classloader.shade.prefix"
argument_list|,
literal|""
argument_list|,
literal|"During reflective instantiation of a class\n"
operator|+
literal|"(input, output formats, serde etc.), when classloader throws ClassNotFoundException, as a fallback this\n"
operator|+
literal|"shade prefix will be used before class reference and retried."
argument_list|)
block|,
name|HIVE_ORC_MS_FOOTER_CACHE_ENABLED
argument_list|(
literal|"hive.orc.splits.ms.footer.cache.enabled"
argument_list|,
literal|false
argument_list|,
literal|"Whether to enable using file metadata cache in metastore for ORC file footers."
argument_list|)
block|,
name|HIVE_ORC_MS_FOOTER_CACHE_PPD
argument_list|(
literal|"hive.orc.splits.ms.footer.cache.ppd.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable file footer cache PPD (hive.orc.splits.ms.footer.cache.enabled\n"
operator|+
literal|"must also be set to true for this to work)."
argument_list|)
block|,
name|HIVE_ORC_INCLUDE_FILE_FOOTER_IN_SPLITS
argument_list|(
literal|"hive.orc.splits.include.file.footer"
argument_list|,
literal|false
argument_list|,
literal|"If turned on splits generated by orc will include metadata about the stripes in the file. This\n"
operator|+
literal|"data is read remotely (from the client or HS2 machine) and sent to all the tasks."
argument_list|)
block|,
name|HIVE_ORC_SPLIT_DIRECTORY_BATCH_MS
argument_list|(
literal|"hive.orc.splits.directory.batch.ms"
argument_list|,
literal|0
argument_list|,
literal|"How long, in ms, to wait to batch input directories for processing during ORC split\n"
operator|+
literal|"generation. 0 means process directories individually. This can increase the number of\n"
operator|+
literal|"metastore calls if metastore metadata cache is used."
argument_list|)
block|,
name|HIVE_ORC_INCLUDE_FILE_ID_IN_SPLITS
argument_list|(
literal|"hive.orc.splits.include.fileid"
argument_list|,
literal|true
argument_list|,
literal|"Include file ID in splits on file systems that support it."
argument_list|)
block|,
name|HIVE_ORC_ALLOW_SYNTHETIC_FILE_ID_IN_SPLITS
argument_list|(
literal|"hive.orc.splits.allow.synthetic.fileid"
argument_list|,
literal|true
argument_list|,
literal|"Allow synthetic file ID in splits on file systems that don't have a native one."
argument_list|)
block|,
name|HIVE_ORC_CACHE_STRIPE_DETAILS_MEMORY_SIZE
argument_list|(
literal|"hive.orc.cache.stripe.details.mem.size"
argument_list|,
literal|"256Mb"
argument_list|,
operator|new
name|SizeValidator
argument_list|()
argument_list|,
literal|"Maximum size of orc splits cached in the client."
argument_list|)
block|,
comment|/**      * @deprecated Use HiveConf.HIVE_COMPUTE_SPLITS_NUM_THREADS      */
annotation|@
name|Deprecated
name|HIVE_ORC_COMPUTE_SPLITS_NUM_THREADS
argument_list|(
literal|"hive.orc.compute.splits.num.threads"
argument_list|,
literal|10
argument_list|,
literal|"How many threads orc should use to create splits in parallel."
argument_list|)
block|,
name|HIVE_ORC_CACHE_USE_SOFT_REFERENCES
argument_list|(
literal|"hive.orc.cache.use.soft.references"
argument_list|,
literal|false
argument_list|,
literal|"By default, the cache that ORC input format uses to store orc file footer use hard\n"
operator|+
literal|"references for the cached object. Setting this to true can help avoid out of memory\n"
operator|+
literal|"issues under memory pressure (in some cases) at the cost of slight unpredictability in\n"
operator|+
literal|"overall query performance."
argument_list|)
block|,
name|HIVE_IO_SARG_CACHE_MAX_WEIGHT_MB
argument_list|(
literal|"hive.io.sarg.cache.max.weight.mb"
argument_list|,
literal|10
argument_list|,
literal|"The max weight allowed for the SearchArgument Cache. By default, the cache allows a max-weight of 10MB, "
operator|+
literal|"after which entries will be evicted."
argument_list|)
block|,
name|HIVE_LAZYSIMPLE_EXTENDED_BOOLEAN_LITERAL
argument_list|(
literal|"hive.lazysimple.extended_boolean_literal"
argument_list|,
literal|false
argument_list|,
literal|"LazySimpleSerde uses this property to determine if it treats 'T', 't', 'F', 'f',\n"
operator|+
literal|"'1', and '0' as extended, legal boolean literal, in addition to 'TRUE' and 'FALSE'.\n"
operator|+
literal|"The default is false, which means only 'TRUE' and 'FALSE' are treated as legal\n"
operator|+
literal|"boolean literal."
argument_list|)
block|,
name|HIVESKEWJOIN
argument_list|(
literal|"hive.optimize.skewjoin"
argument_list|,
literal|false
argument_list|,
literal|"Whether to enable skew join optimization. \n"
operator|+
literal|"The algorithm is as follows: At runtime, detect the keys with a large skew. Instead of\n"
operator|+
literal|"processing those keys, store them temporarily in an HDFS directory. In a follow-up map-reduce\n"
operator|+
literal|"job, process those skewed keys. The same key need not be skewed for all the tables, and so,\n"
operator|+
literal|"the follow-up map-reduce job (for the skewed keys) would be much faster, since it would be a\n"
operator|+
literal|"map-join."
argument_list|)
block|,
name|HIVEDYNAMICPARTITIONHASHJOIN
argument_list|(
literal|"hive.optimize.dynamic.partition.hashjoin"
argument_list|,
literal|false
argument_list|,
literal|"Whether to enable dynamically partitioned hash join optimization. \n"
operator|+
literal|"This setting is also dependent on enabling hive.auto.convert.join"
argument_list|)
block|,
name|HIVECONVERTJOIN
argument_list|(
literal|"hive.auto.convert.join"
argument_list|,
literal|true
argument_list|,
literal|"Whether Hive enables the optimization about converting common join into mapjoin based on the input file size"
argument_list|)
block|,
name|HIVECONVERTJOINNOCONDITIONALTASK
argument_list|(
literal|"hive.auto.convert.join.noconditionaltask"
argument_list|,
literal|true
argument_list|,
literal|"Whether Hive enables the optimization about converting common join into mapjoin based on the input file size. \n"
operator|+
literal|"If this parameter is on, and the sum of size for n-1 of the tables/partitions for a n-way join is smaller than the\n"
operator|+
literal|"specified size, the join is directly converted to a mapjoin (there is no conditional task)."
argument_list|)
block|,
name|HIVECONVERTJOINNOCONDITIONALTASKTHRESHOLD
argument_list|(
literal|"hive.auto.convert.join.noconditionaltask.size"
argument_list|,
literal|10000000L
argument_list|,
literal|"If hive.auto.convert.join.noconditionaltask is off, this parameter does not take affect. \n"
operator|+
literal|"However, if it is on, and the sum of size for n-1 of the tables/partitions for a n-way join is smaller than this size, \n"
operator|+
literal|"the join is directly converted to a mapjoin(there is no conditional task). The default is 10MB"
argument_list|)
block|,
name|HIVECONVERTJOINUSENONSTAGED
argument_list|(
literal|"hive.auto.convert.join.use.nonstaged"
argument_list|,
literal|false
argument_list|,
literal|"For conditional joins, if input stream from a small alias can be directly applied to join operator without \n"
operator|+
literal|"filtering or projection, the alias need not to be pre-staged in distributed cache via mapred local task.\n"
operator|+
literal|"Currently, this is not working with vectorization or tez execution engine."
argument_list|)
block|,
name|HIVESKEWJOINKEY
argument_list|(
literal|"hive.skewjoin.key"
argument_list|,
literal|100000
argument_list|,
literal|"Determine if we get a skew key in join. If we see more than the specified number of rows with the same key in join operator,\n"
operator|+
literal|"we think the key as a skew join key. "
argument_list|)
block|,
name|HIVESKEWJOINMAPJOINNUMMAPTASK
argument_list|(
literal|"hive.skewjoin.mapjoin.map.tasks"
argument_list|,
literal|10000
argument_list|,
literal|"Determine the number of map task used in the follow up map join job for a skew join.\n"
operator|+
literal|"It should be used together with hive.skewjoin.mapjoin.min.split to perform a fine grained control."
argument_list|)
block|,
name|HIVESKEWJOINMAPJOINMINSPLIT
argument_list|(
literal|"hive.skewjoin.mapjoin.min.split"
argument_list|,
literal|33554432L
argument_list|,
literal|"Determine the number of map task at most used in the follow up map join job for a skew join by specifying \n"
operator|+
literal|"the minimum split size. It should be used together with hive.skewjoin.mapjoin.map.tasks to perform a fine grained control."
argument_list|)
block|,
name|HIVESENDHEARTBEAT
argument_list|(
literal|"hive.heartbeat.interval"
argument_list|,
literal|1000
argument_list|,
literal|"Send a heartbeat after this interval - used by mapjoin and filter operators"
argument_list|)
block|,
name|HIVELIMITMAXROWSIZE
argument_list|(
literal|"hive.limit.row.max.size"
argument_list|,
literal|100000L
argument_list|,
literal|"When trying a smaller subset of data for simple LIMIT, how much size we need to guarantee each row to have at least."
argument_list|)
block|,
name|HIVELIMITOPTLIMITFILE
argument_list|(
literal|"hive.limit.optimize.limit.file"
argument_list|,
literal|10
argument_list|,
literal|"When trying a smaller subset of data for simple LIMIT, maximum number of files we can sample."
argument_list|)
block|,
name|HIVELIMITOPTENABLE
argument_list|(
literal|"hive.limit.optimize.enable"
argument_list|,
literal|false
argument_list|,
literal|"Whether to enable to optimization to trying a smaller subset of data for simple LIMIT first."
argument_list|)
block|,
name|HIVELIMITOPTMAXFETCH
argument_list|(
literal|"hive.limit.optimize.fetch.max"
argument_list|,
literal|50000
argument_list|,
literal|"Maximum number of rows allowed for a smaller subset of data for simple LIMIT, if it is a fetch query. \n"
operator|+
literal|"Insert queries are not restricted by this limit."
argument_list|)
block|,
name|HIVELIMITPUSHDOWNMEMORYUSAGE
argument_list|(
literal|"hive.limit.pushdown.memory.usage"
argument_list|,
literal|0.1f
argument_list|,
operator|new
name|RatioValidator
argument_list|()
argument_list|,
literal|"The fraction of available memory to be used for buffering rows in Reducesink operator for limit pushdown optimization."
argument_list|)
block|,
name|HIVECONVERTJOINMAXENTRIESHASHTABLE
argument_list|(
literal|"hive.auto.convert.join.hashtable.max.entries"
argument_list|,
literal|21000000L
argument_list|,
literal|"If hive.auto.convert.join.noconditionaltask is off, this parameter does not take affect. \n"
operator|+
literal|"However, if it is on, and the predicted number of entries in hashtable for a given join \n"
operator|+
literal|"input is larger than this number, the join will not be converted to a mapjoin. \n"
operator|+
literal|"The value \"-1\" means no limit."
argument_list|)
block|,
name|XPRODSMALLTABLEROWSTHRESHOLD
argument_list|(
literal|"hive.xprod.mapjoin.small.table.rows"
argument_list|,
literal|1
argument_list|,
literal|"Maximum number of rows on build side"
operator|+
literal|" of map join before it switches over to cross product edge"
argument_list|)
block|,
name|HIVECONVERTJOINMAXSHUFFLESIZE
argument_list|(
literal|"hive.auto.convert.join.shuffle.max.size"
argument_list|,
literal|10000000000L
argument_list|,
literal|"If hive.auto.convert.join.noconditionaltask is off, this parameter does not take affect. \n"
operator|+
literal|"However, if it is on, and the predicted size of the larger input for a given join is greater \n"
operator|+
literal|"than this number, the join will not be converted to a dynamically partitioned hash join. \n"
operator|+
literal|"The value \"-1\" means no limit."
argument_list|)
block|,
name|HIVEHASHTABLEKEYCOUNTADJUSTMENT
argument_list|(
literal|"hive.hashtable.key.count.adjustment"
argument_list|,
literal|0.99f
argument_list|,
literal|"Adjustment to mapjoin hashtable size derived from table and column statistics; the estimate"
operator|+
literal|" of the number of keys is divided by this value. If the value is 0, statistics are not used"
operator|+
literal|"and hive.hashtable.initialCapacity is used instead."
argument_list|)
block|,
name|HIVEHASHTABLETHRESHOLD
argument_list|(
literal|"hive.hashtable.initialCapacity"
argument_list|,
literal|100000
argument_list|,
literal|"Initial capacity of "
operator|+
literal|"mapjoin hashtable if statistics are absent, or if hive.hashtable.key.count.adjustment is set to 0"
argument_list|)
block|,
name|HIVEHASHTABLELOADFACTOR
argument_list|(
literal|"hive.hashtable.loadfactor"
argument_list|,
operator|(
name|float
operator|)
literal|0.75
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEHASHTABLEFOLLOWBYGBYMAXMEMORYUSAGE
argument_list|(
literal|"hive.mapjoin.followby.gby.localtask.max.memory.usage"
argument_list|,
operator|(
name|float
operator|)
literal|0.55
argument_list|,
literal|"This number means how much memory the local task can take to hold the key/value into an in-memory hash table \n"
operator|+
literal|"when this map join is followed by a group by. If the local task's memory usage is more than this number, \n"
operator|+
literal|"the local task will abort by itself. It means the data of the small table is too large "
operator|+
literal|"to be held in memory. Does not apply to Hive-on-Spark (replaced by "
operator|+
literal|"hive.mapjoin.max.gc.time.percentage)"
argument_list|)
block|,
name|HIVEHASHTABLEMAXMEMORYUSAGE
argument_list|(
literal|"hive.mapjoin.localtask.max.memory.usage"
argument_list|,
operator|(
name|float
operator|)
literal|0.90
argument_list|,
literal|"This number means how much memory the local task can take to hold the key/value into an in-memory hash table. \n"
operator|+
literal|"If the local task's memory usage is more than this number, the local task will abort by itself. \n"
operator|+
literal|"It means the data of the small table is too large to be held in memory. Does not apply to "
operator|+
literal|"Hive-on-Spark (replaced by hive.mapjoin.max.gc.time.percentage)"
argument_list|)
block|,
name|HIVEHASHTABLESCALE
argument_list|(
literal|"hive.mapjoin.check.memory.rows"
argument_list|,
operator|(
name|long
operator|)
literal|100000
argument_list|,
literal|"The number means after how many rows processed it needs to check the memory usage"
argument_list|)
block|,
name|HIVEHASHTABLEMAXGCTIMEPERCENTAGE
argument_list|(
literal|"hive.mapjoin.max.gc.time.percentage"
argument_list|,
operator|(
name|float
operator|)
literal|0.60
argument_list|,
operator|new
name|RangeValidator
argument_list|(
literal|0.0f
argument_list|,
literal|1.0f
argument_list|)
argument_list|,
literal|"This number means how much time (what percentage, "
operator|+
literal|"0..1, of wallclock time) the JVM is allowed to spend in garbage collection when running "
operator|+
literal|"the local task. If GC time percentage exceeds this number, the local task will abort by "
operator|+
literal|"itself. Applies to Hive-on-Spark only"
argument_list|)
block|,
name|HIVEDEBUGLOCALTASK
argument_list|(
literal|"hive.debug.localtask"
argument_list|,
literal|false
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEINPUTFORMAT
argument_list|(
literal|"hive.input.format"
argument_list|,
literal|"org.apache.hadoop.hive.ql.io.CombineHiveInputFormat"
argument_list|,
literal|"The default input format. Set this to HiveInputFormat if you encounter problems with CombineHiveInputFormat."
argument_list|)
block|,
name|HIVETEZINPUTFORMAT
argument_list|(
literal|"hive.tez.input.format"
argument_list|,
literal|"org.apache.hadoop.hive.ql.io.HiveInputFormat"
argument_list|,
literal|"The default input format for tez. Tez groups splits in the AM."
argument_list|)
block|,
name|HIVETEZCONTAINERSIZE
argument_list|(
literal|"hive.tez.container.size"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"By default Tez will spawn containers of the size of a mapper. This can be used to overwrite."
argument_list|)
block|,
name|HIVETEZCPUVCORES
argument_list|(
literal|"hive.tez.cpu.vcores"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"By default Tez will ask for however many cpus map-reduce is configured to use per container.\n"
operator|+
literal|"This can be used to overwrite."
argument_list|)
block|,
name|HIVETEZJAVAOPTS
argument_list|(
literal|"hive.tez.java.opts"
argument_list|,
literal|null
argument_list|,
literal|"By default Tez will use the Java options from map tasks. This can be used to overwrite."
argument_list|)
block|,
name|HIVETEZLOGLEVEL
argument_list|(
literal|"hive.tez.log.level"
argument_list|,
literal|"INFO"
argument_list|,
literal|"The log level to use for tasks executing as part of the DAG.\n"
operator|+
literal|"Used only if hive.tez.java.opts is used to configure Java options."
argument_list|)
block|,
name|HIVETEZHS2USERACCESS
argument_list|(
literal|"hive.tez.hs2.user.access"
argument_list|,
literal|true
argument_list|,
literal|"Whether to grant access to the hs2/hive user for queries"
argument_list|)
block|,
name|HIVEQUERYNAME
argument_list|(
literal|"hive.query.name"
argument_list|,
literal|null
argument_list|,
literal|"This named is used by Tez to set the dag name. This name in turn will appear on \n"
operator|+
literal|"the Tez UI representing the work that was done. Used by Spark to set the query name, will show up in the\n"
operator|+
literal|"Spark UI."
argument_list|)
block|,
name|SYSLOG_INPUT_FORMAT_FILE_PRUNING
argument_list|(
literal|"hive.syslog.input.format.file.pruning"
argument_list|,
literal|true
argument_list|,
literal|"Whether syslog input format should prune files based on timestamp (ts) column in sys.logs table."
argument_list|)
block|,
name|SYSLOG_INPUT_FORMAT_FILE_TIME_SLICE
argument_list|(
literal|"hive.syslog.input.format.file.time.slice"
argument_list|,
literal|"300s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
literal|0L
argument_list|,
literal|false
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
literal|false
argument_list|)
argument_list|,
literal|"Files stored in sys.logs typically are chunked with time interval. For example: depending on the\n"
operator|+
literal|"logging library used this represents the flush interval/time slice. \n"
operator|+
literal|"If time slice/flust interval is set to 5 minutes, then the expectation is that the filename \n"
operator|+
literal|"2019-01-02-10-00_0.log represent time range from 10:00 to 10:05.\n"
operator|+
literal|"This time slice should align with the flush interval of the logging library else file pruning may\n"
operator|+
literal|"incorrectly prune files leading to incorrect results from sys.logs table."
argument_list|)
block|,
name|HIVEOPTIMIZEBUCKETINGSORTING
argument_list|(
literal|"hive.optimize.bucketingsorting"
argument_list|,
literal|true
argument_list|,
literal|"Don't create a reducer for enforcing \n"
operator|+
literal|"bucketing/sorting for queries of the form: \n"
operator|+
literal|"insert overwrite table T2 select * from T1;\n"
operator|+
literal|"where T1 and T2 are bucketed/sorted by the same keys into the same number of buckets."
argument_list|)
block|,
name|HIVEPARTITIONER
argument_list|(
literal|"hive.mapred.partitioner"
argument_list|,
literal|"org.apache.hadoop.hive.ql.io.DefaultHivePartitioner"
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEENFORCESORTMERGEBUCKETMAPJOIN
argument_list|(
literal|"hive.enforce.sortmergebucketmapjoin"
argument_list|,
literal|false
argument_list|,
literal|"If the user asked for sort-merge bucketed map-side join, and it cannot be performed, should the query fail or not ?"
argument_list|)
block|,
name|HIVEENFORCEBUCKETMAPJOIN
argument_list|(
literal|"hive.enforce.bucketmapjoin"
argument_list|,
literal|false
argument_list|,
literal|"If the user asked for bucketed map-side join, and it cannot be performed, \n"
operator|+
literal|"should the query fail or not ? For example, if the buckets in the tables being joined are\n"
operator|+
literal|"not a multiple of each other, bucketed map-side join cannot be performed, and the\n"
operator|+
literal|"query will fail if hive.enforce.bucketmapjoin is set to true."
argument_list|)
block|,
name|HIVE_SORT_WHEN_BUCKETING
argument_list|(
literal|"hive.optimize.clustered.sort"
argument_list|,
literal|true
argument_list|,
literal|"When this option is true, when a Hive table was created with a clustered by clause, we will also\n"
operator|+
literal|"sort by same value (if sort columns were not specified)"
argument_list|)
block|,
name|HIVE_ENFORCE_NOT_NULL_CONSTRAINT
argument_list|(
literal|"hive.constraint.notnull.enforce"
argument_list|,
literal|true
argument_list|,
literal|"Should \"IS NOT NULL \" constraint be enforced?"
argument_list|)
block|,
name|HIVE_AUTO_SORTMERGE_JOIN
argument_list|(
literal|"hive.auto.convert.sortmerge.join"
argument_list|,
literal|true
argument_list|,
literal|"Will the join be automatically converted to a sort-merge join, if the joined tables pass the criteria for sort-merge join."
argument_list|)
block|,
name|HIVE_AUTO_SORTMERGE_JOIN_REDUCE
argument_list|(
literal|"hive.auto.convert.sortmerge.join.reduce.side"
argument_list|,
literal|true
argument_list|,
literal|"Whether hive.auto.convert.sortmerge.join (if enabled) should be applied to reduce side."
argument_list|)
block|,
name|HIVE_AUTO_SORTMERGE_JOIN_BIGTABLE_SELECTOR
argument_list|(
literal|"hive.auto.convert.sortmerge.join.bigtable.selection.policy"
argument_list|,
literal|"org.apache.hadoop.hive.ql.optimizer.AvgPartitionSizeBasedBigTableSelectorForAutoSMJ"
argument_list|,
literal|"The policy to choose the big table for automatic conversion to sort-merge join. \n"
operator|+
literal|"By default, the table with the largest partitions is assigned the big table. All policies are:\n"
operator|+
literal|". based on position of the table - the leftmost table is selected\n"
operator|+
literal|"org.apache.hadoop.hive.ql.optimizer.LeftmostBigTableSMJ.\n"
operator|+
literal|". based on total size (all the partitions selected in the query) of the table \n"
operator|+
literal|"org.apache.hadoop.hive.ql.optimizer.TableSizeBasedBigTableSelectorForAutoSMJ.\n"
operator|+
literal|". based on average size (all the partitions selected in the query) of the table \n"
operator|+
literal|"org.apache.hadoop.hive.ql.optimizer.AvgPartitionSizeBasedBigTableSelectorForAutoSMJ.\n"
operator|+
literal|"New policies can be added in future."
argument_list|)
block|,
name|HIVE_AUTO_SORTMERGE_JOIN_TOMAPJOIN
argument_list|(
literal|"hive.auto.convert.sortmerge.join.to.mapjoin"
argument_list|,
literal|false
argument_list|,
literal|"If hive.auto.convert.sortmerge.join is set to true, and a join was converted to a sort-merge join, \n"
operator|+
literal|"this parameter decides whether each table should be tried as a big table, and effectively a map-join should be\n"
operator|+
literal|"tried. That would create a conditional task with n+1 children for a n-way join (1 child for each table as the\n"
operator|+
literal|"big table), and the backup task will be the sort-merge join. In some cases, a map-join would be faster than a\n"
operator|+
literal|"sort-merge join, if there is no advantage of having the output bucketed and sorted. For example, if a very big sorted\n"
operator|+
literal|"and bucketed table with few files (say 10 files) are being joined with a very small sorter and bucketed table\n"
operator|+
literal|"with few files (10 files), the sort-merge join will only use 10 mappers, and a simple map-only join might be faster\n"
operator|+
literal|"if the complete small table can fit in memory, and a map-join can be performed."
argument_list|)
block|,
name|HIVESCRIPTOPERATORTRUST
argument_list|(
literal|"hive.exec.script.trust"
argument_list|,
literal|false
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEROWOFFSET
argument_list|(
literal|"hive.exec.rowoffset"
argument_list|,
literal|false
argument_list|,
literal|"Whether to provide the row offset virtual column"
argument_list|)
block|,
comment|// Optimizer
name|HIVEOPTINDEXFILTER
argument_list|(
literal|"hive.optimize.index.filter"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable automatic use of indexes"
argument_list|)
block|,
name|HIVEOPTPPD
argument_list|(
literal|"hive.optimize.ppd"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable predicate pushdown"
argument_list|)
block|,
name|HIVEOPTPPD_WINDOWING
argument_list|(
literal|"hive.optimize.ppd.windowing"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable predicate pushdown through windowing"
argument_list|)
block|,
name|HIVEPPDRECOGNIZETRANSITIVITY
argument_list|(
literal|"hive.ppd.recognizetransivity"
argument_list|,
literal|true
argument_list|,
literal|"Whether to transitively replicate predicate filters over equijoin conditions."
argument_list|)
block|,
name|HIVEPPDREMOVEDUPLICATEFILTERS
argument_list|(
literal|"hive.ppd.remove.duplicatefilters"
argument_list|,
literal|true
argument_list|,
literal|"During query optimization, filters may be pushed down in the operator tree. \n"
operator|+
literal|"If this config is true only pushed down filters remain in the operator tree, \n"
operator|+
literal|"and the original filter is removed. If this config is false, the original filter \n"
operator|+
literal|"is also left in the operator tree at the original place."
argument_list|)
block|,
name|HIVEPOINTLOOKUPOPTIMIZER
argument_list|(
literal|"hive.optimize.point.lookup"
argument_list|,
literal|true
argument_list|,
literal|"Whether to transform OR clauses in Filter operators into IN clauses"
argument_list|)
block|,
name|HIVEPOINTLOOKUPOPTIMIZERMIN
argument_list|(
literal|"hive.optimize.point.lookup.min"
argument_list|,
literal|2
argument_list|,
literal|"Minimum number of OR clauses needed to transform into IN clauses"
argument_list|)
block|,
name|HIVEOPT_TRANSFORM_IN_MAXNODES
argument_list|(
literal|"hive.optimize.transform.in.maxnodes"
argument_list|,
literal|16
argument_list|,
literal|"Maximum number of IN expressions beyond which IN will not be transformed into OR clause"
argument_list|)
block|,
name|HIVECOUNTDISTINCTOPTIMIZER
argument_list|(
literal|"hive.optimize.countdistinct"
argument_list|,
literal|true
argument_list|,
literal|"Whether to transform count distinct into two stages"
argument_list|)
block|,
name|HIVEPARTITIONCOLUMNSEPARATOR
argument_list|(
literal|"hive.optimize.partition.columns.separate"
argument_list|,
literal|true
argument_list|,
literal|"Extract partition columns from IN clauses"
argument_list|)
block|,
comment|// Constant propagation optimizer
name|HIVEOPTCONSTANTPROPAGATION
argument_list|(
literal|"hive.optimize.constant.propagation"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable constant propagation optimizer"
argument_list|)
block|,
name|HIVEIDENTITYPROJECTREMOVER
argument_list|(
literal|"hive.optimize.remove.identity.project"
argument_list|,
literal|true
argument_list|,
literal|"Removes identity project from operator tree"
argument_list|)
block|,
name|HIVEMETADATAONLYQUERIES
argument_list|(
literal|"hive.optimize.metadataonly"
argument_list|,
literal|false
argument_list|,
literal|"Whether to eliminate scans of the tables from which no columns are selected. Note\n"
operator|+
literal|"that, when selecting from empty tables with data files, this can produce incorrect\n"
operator|+
literal|"results, so it's disabled by default. It works correctly for normal tables."
argument_list|)
block|,
name|HIVENULLSCANOPTIMIZE
argument_list|(
literal|"hive.optimize.null.scan"
argument_list|,
literal|true
argument_list|,
literal|"Dont scan relations which are guaranteed to not generate any rows"
argument_list|)
block|,
name|HIVEOPTPPD_STORAGE
argument_list|(
literal|"hive.optimize.ppd.storage"
argument_list|,
literal|true
argument_list|,
literal|"Whether to push predicates down to storage handlers"
argument_list|)
block|,
name|HIVEOPTGROUPBY
argument_list|(
literal|"hive.optimize.groupby"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable the bucketed group by from bucketed partitions/tables."
argument_list|)
block|,
name|HIVEOPTBUCKETMAPJOIN
argument_list|(
literal|"hive.optimize.bucketmapjoin"
argument_list|,
literal|false
argument_list|,
literal|"Whether to try bucket mapjoin"
argument_list|)
block|,
name|HIVEOPTSORTMERGEBUCKETMAPJOIN
argument_list|(
literal|"hive.optimize.bucketmapjoin.sortedmerge"
argument_list|,
literal|false
argument_list|,
literal|"Whether to try sorted bucket merge map join"
argument_list|)
block|,
name|HIVEOPTREDUCEDEDUPLICATION
argument_list|(
literal|"hive.optimize.reducededuplication"
argument_list|,
literal|true
argument_list|,
literal|"Remove extra map-reduce jobs if the data is already clustered by the same key which needs to be used again. \n"
operator|+
literal|"This should always be set to true. Since it is a new feature, it has been made configurable."
argument_list|)
block|,
name|HIVEOPTREDUCEDEDUPLICATIONMINREDUCER
argument_list|(
literal|"hive.optimize.reducededuplication.min.reducer"
argument_list|,
literal|4
argument_list|,
literal|"Reduce deduplication merges two RSs by moving key/parts/reducer-num of the child RS to parent RS. \n"
operator|+
literal|"That means if reducer-num of the child RS is fixed (order by or forced bucketing) and small, it can make very slow, single MR.\n"
operator|+
literal|"The optimization will be automatically disabled if number of reducers would be less than specified value."
argument_list|)
block|,
name|HIVEOPTJOINREDUCEDEDUPLICATION
argument_list|(
literal|"hive.optimize.joinreducededuplication"
argument_list|,
literal|true
argument_list|,
literal|"Remove extra shuffle/sorting operations after join algorithm selection has been executed. \n"
operator|+
literal|"Currently it only works with Apache Tez. This should always be set to true. \n"
operator|+
literal|"Since it is a new feature, it has been made configurable."
argument_list|)
block|,
annotation|@
name|Deprecated
name|HIVEOPTSORTDYNAMICPARTITION
argument_list|(
literal|"hive.optimize.sort.dynamic.partition"
argument_list|,
literal|false
argument_list|,
literal|"Deprecated. Use hive.optimize.sort.dynamic.partition.threshold instead."
argument_list|)
block|,
name|HIVEOPTSORTDYNAMICPARTITIONTHRESHOLD
argument_list|(
literal|"hive.optimize.sort.dynamic.partition.threshold"
argument_list|,
literal|0
argument_list|,
literal|"When enabled dynamic partitioning column will be globally sorted.\n"
operator|+
literal|"This way we can keep only one record writer open for each partition value\n"
operator|+
literal|"in the reducer thereby reducing the memory pressure on reducers.\n"
operator|+
literal|"This config has following possible values: \n"
operator|+
literal|"\t-1 - This completely disables the optimization. \n"
operator|+
literal|"\t1 - This always enable the optimization. \n"
operator|+
literal|"\t0 - This makes the optimization a cost based decision. \n"
operator|+
literal|"Setting it to any other positive integer will make Hive use this as threshold for number of writers."
argument_list|)
block|,
name|HIVESAMPLINGFORORDERBY
argument_list|(
literal|"hive.optimize.sampling.orderby"
argument_list|,
literal|false
argument_list|,
literal|"Uses sampling on order-by clause for parallel execution."
argument_list|)
block|,
name|HIVESAMPLINGNUMBERFORORDERBY
argument_list|(
literal|"hive.optimize.sampling.orderby.number"
argument_list|,
literal|1000
argument_list|,
literal|"Total number of samples to be obtained."
argument_list|)
block|,
name|HIVESAMPLINGPERCENTFORORDERBY
argument_list|(
literal|"hive.optimize.sampling.orderby.percent"
argument_list|,
literal|0.1f
argument_list|,
operator|new
name|RatioValidator
argument_list|()
argument_list|,
literal|"Probability with which a row will be chosen."
argument_list|)
block|,
name|HIVE_REMOVE_ORDERBY_IN_SUBQUERY
argument_list|(
literal|"hive.remove.orderby.in.subquery"
argument_list|,
literal|true
argument_list|,
literal|"If set to true, order/sort by without limit in sub queries will be removed."
argument_list|)
block|,
name|HIVEOPTIMIZEDISTINCTREWRITE
argument_list|(
literal|"hive.optimize.distinct.rewrite"
argument_list|,
literal|true
argument_list|,
literal|"When applicable this "
operator|+
literal|"optimization rewrites distinct aggregates from a single stage to multi-stage "
operator|+
literal|"aggregation. This may not be optimal in all cases. Ideally, whether to trigger it or "
operator|+
literal|"not should be cost based decision. Until Hive formalizes cost model for this, this is config driven."
argument_list|)
block|,
comment|// whether to optimize union followed by select followed by filesink
comment|// It creates sub-directories in the final output, so should not be turned on in systems
comment|// where MAPREDUCE-1501 is not present
name|HIVE_OPTIMIZE_UNION_REMOVE
argument_list|(
literal|"hive.optimize.union.remove"
argument_list|,
literal|false
argument_list|,
literal|"Whether to remove the union and push the operators between union and the filesink above union. \n"
operator|+
literal|"This avoids an extra scan of the output by union. This is independently useful for union\n"
operator|+
literal|"queries, and specially useful when hive.optimize.skewjoin.compiletime is set to true, since an\n"
operator|+
literal|"extra union is inserted.\n"
operator|+
literal|"\n"
operator|+
literal|"The merge is triggered if either of hive.merge.mapfiles or hive.merge.mapredfiles is set to true.\n"
operator|+
literal|"If the user has set hive.merge.mapfiles to true and hive.merge.mapredfiles to false, the idea was the\n"
operator|+
literal|"number of reducers are few, so the number of files anyway are small. However, with this optimization,\n"
operator|+
literal|"we are increasing the number of files possibly by a big margin. So, we merge aggressively."
argument_list|)
block|,
name|HIVEOPTCORRELATION
argument_list|(
literal|"hive.optimize.correlation"
argument_list|,
literal|false
argument_list|,
literal|"exploit intra-query correlations."
argument_list|)
block|,
name|HIVE_OPTIMIZE_LIMIT_TRANSPOSE
argument_list|(
literal|"hive.optimize.limittranspose"
argument_list|,
literal|false
argument_list|,
literal|"Whether to push a limit through left/right outer join or union. If the value is true and the size of the outer\n"
operator|+
literal|"input is reduced enough (as specified in hive.optimize.limittranspose.reduction), the limit is pushed\n"
operator|+
literal|"to the outer input or union; to remain semantically correct, the limit is kept on top of the join or the union too."
argument_list|)
block|,
name|HIVE_OPTIMIZE_LIMIT_TRANSPOSE_REDUCTION_PERCENTAGE
argument_list|(
literal|"hive.optimize.limittranspose.reductionpercentage"
argument_list|,
literal|1.0f
argument_list|,
literal|"When hive.optimize.limittranspose is true, this variable specifies the minimal reduction of the\n"
operator|+
literal|"size of the outer input of the join or input of the union that we should get in order to apply the rule."
argument_list|)
block|,
name|HIVE_OPTIMIZE_LIMIT_TRANSPOSE_REDUCTION_TUPLES
argument_list|(
literal|"hive.optimize.limittranspose.reductiontuples"
argument_list|,
operator|(
name|long
operator|)
literal|0
argument_list|,
literal|"When hive.optimize.limittranspose is true, this variable specifies the minimal reduction in the\n"
operator|+
literal|"number of tuples of the outer input of the join or the input of the union that you should get in order to apply the rule."
argument_list|)
block|,
name|HIVE_OPTIMIZE_CONSTRAINTS_JOIN
argument_list|(
literal|"hive.optimize.constraints.join"
argument_list|,
literal|true
argument_list|,
literal|"Whether to use referential constraints\n"
operator|+
literal|"to optimize (remove or transform) join operators"
argument_list|)
block|,
name|HIVE_OPTIMIZE_SORT_PREDS_WITH_STATS
argument_list|(
literal|"hive.optimize.filter.preds.sort"
argument_list|,
literal|true
argument_list|,
literal|"Whether to sort conditions in filters\n"
operator|+
literal|"based on estimated selectivity and compute cost"
argument_list|)
block|,
name|HIVE_OPTIMIZE_REDUCE_WITH_STATS
argument_list|(
literal|"hive.optimize.filter.stats.reduction"
argument_list|,
literal|false
argument_list|,
literal|"Whether to simplify comparison\n"
operator|+
literal|"expressions in filter operators using column stats"
argument_list|)
block|,
name|HIVE_OPTIMIZE_SKEWJOIN_COMPILETIME
argument_list|(
literal|"hive.optimize.skewjoin.compiletime"
argument_list|,
literal|false
argument_list|,
literal|"Whether to create a separate plan for skewed keys for the tables in the join.\n"
operator|+
literal|"This is based on the skewed keys stored in the metadata. At compile time, the plan is broken\n"
operator|+
literal|"into different joins: one for the skewed keys, and the other for the remaining keys. And then,\n"
operator|+
literal|"a union is performed for the 2 joins generated above. So unless the same skewed key is present\n"
operator|+
literal|"in both the joined tables, the join for the skewed key will be performed as a map-side join.\n"
operator|+
literal|"\n"
operator|+
literal|"The main difference between this parameter and hive.optimize.skewjoin is that this parameter\n"
operator|+
literal|"uses the skew information stored in the metastore to optimize the plan at compile time itself.\n"
operator|+
literal|"If there is no skew information in the metadata, this parameter will not have any affect.\n"
operator|+
literal|"Both hive.optimize.skewjoin.compiletime and hive.optimize.skewjoin should be set to true.\n"
operator|+
literal|"Ideally, hive.optimize.skewjoin should be renamed as hive.optimize.skewjoin.runtime, but not doing\n"
operator|+
literal|"so for backward compatibility.\n"
operator|+
literal|"\n"
operator|+
literal|"If the skew information is correctly stored in the metadata, hive.optimize.skewjoin.compiletime\n"
operator|+
literal|"would change the query plan to take care of it, and hive.optimize.skewjoin will be a no-op."
argument_list|)
block|,
name|HIVE_OPTIMIZE_TOPNKEY
argument_list|(
literal|"hive.optimize.topnkey"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable top n key optimizer."
argument_list|)
block|,
name|HIVE_MAX_TOPN_ALLOWED
argument_list|(
literal|"hive.optimize.topnkey.max"
argument_list|,
literal|128
argument_list|,
literal|"Maximum topN value allowed by top n key optimizer.\n"
operator|+
literal|"If the LIMIT is greater than this value then top n key optimization won't be used."
argument_list|)
block|,
name|HIVE_TOPN_EFFICIENCY_THRESHOLD
argument_list|(
literal|"hive.optimize.topnkey.efficiency.threshold"
argument_list|,
literal|0.8f
argument_list|,
literal|"Disable topN key filter if the ratio between forwarded and total rows reaches this limit."
argument_list|)
block|,
name|HIVE_TOPN_EFFICIENCY_CHECK_BATCHES
argument_list|(
literal|"hive.optimize.topnkey.efficiency.check.nbatches"
argument_list|,
literal|10000
argument_list|,
literal|"Check topN key filter efficiency after a specific number of batches."
argument_list|)
block|,
name|HIVE_TOPN_MAX_NUMBER_OF_PARTITIONS
argument_list|(
literal|"hive.optimize.topnkey.partitions.max"
argument_list|,
literal|64
argument_list|,
literal|"Limit the maximum number of partitions used by the top N key operator."
argument_list|)
block|,
name|HIVE_SHARED_WORK_OPTIMIZATION
argument_list|(
literal|"hive.optimize.shared.work"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable shared work optimizer. The optimizer finds scan operator over the same table\n"
operator|+
literal|"and follow-up operators in the query plan and merges them if they meet some preconditions. Tez only."
argument_list|)
block|,
name|HIVE_SHARED_WORK_EXTENDED_OPTIMIZATION
argument_list|(
literal|"hive.optimize.shared.work.extended"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable shared work extended optimizer. The optimizer tries to merge equal operators\n"
operator|+
literal|"after a work boundary after shared work optimizer has been executed. Requires hive.optimize.shared.work\n"
operator|+
literal|"to be set to true. Tez only."
argument_list|)
block|,
name|HIVE_SHARED_WORK_SEMIJOIN_OPTIMIZATION
argument_list|(
literal|"hive.optimize.shared.work.semijoin"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable shared work extended optimizer for semijoins. The optimizer tries to merge\n"
operator|+
literal|"scan operators if one of them reads the full table, even if the other one is the target for\n"
operator|+
literal|"one or more semijoin edges. Tez only."
argument_list|)
block|,
name|HIVE_SHARED_WORK_REUSE_MAPJOIN_CACHE
argument_list|(
literal|"hive.optimize.shared.work.mapjoin.cache.reuse"
argument_list|,
literal|true
argument_list|,
literal|"When shared work optimizer is enabled, whether we should reuse the cache for the broadcast side\n"
operator|+
literal|"of mapjoin operators that share same broadcast input. Requires hive.optimize.shared.work\n"
operator|+
literal|"to be set to true. Tez only."
argument_list|)
block|,
name|HIVE_COMBINE_EQUIVALENT_WORK_OPTIMIZATION
argument_list|(
literal|"hive.combine.equivalent.work.optimization"
argument_list|,
literal|true
argument_list|,
literal|"Whether to "
operator|+
literal|"combine equivalent work objects during physical optimization.\n This optimization looks for equivalent "
operator|+
literal|"work objects and combines them if they meet certain preconditions. Spark only."
argument_list|)
block|,
name|HIVE_REMOVE_SQ_COUNT_CHECK
argument_list|(
literal|"hive.optimize.remove.sq_count_check"
argument_list|,
literal|true
argument_list|,
literal|"Whether to remove an extra join with sq_count_check for scalar subqueries "
operator|+
literal|"with constant group by keys."
argument_list|)
block|,
name|HIVE_OPTIMIZE_TABLE_PROPERTIES_FROM_SERDE
argument_list|(
literal|"hive.optimize.update.table.properties.from.serde"
argument_list|,
literal|false
argument_list|,
literal|"Whether to update table-properties by initializing tables' SerDe instances during logical-optimization. \n"
operator|+
literal|"By doing so, certain SerDe classes (like AvroSerDe) can pre-calculate table-specific information, and \n"
operator|+
literal|"store it in table-properties, to be used later in the SerDe, while running the job."
argument_list|)
block|,
name|HIVE_OPTIMIZE_TABLE_PROPERTIES_FROM_SERDE_LIST
argument_list|(
literal|"hive.optimize.update.table.properties.from.serde.list"
argument_list|,
literal|"org.apache.hadoop.hive.serde2.avro.AvroSerDe"
argument_list|,
literal|"The comma-separated list of SerDe classes that are considered when enhancing table-properties \n"
operator|+
literal|"during logical optimization."
argument_list|)
block|,
comment|// CTE
name|HIVE_CTE_MATERIALIZE_THRESHOLD
argument_list|(
literal|"hive.optimize.cte.materialize.threshold"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"If the number of references to a CTE clause exceeds this threshold, Hive will materialize it\n"
operator|+
literal|"before executing the main query block. -1 will disable this feature."
argument_list|)
block|,
comment|// Statistics
name|HIVE_STATS_ESTIMATE_STATS
argument_list|(
literal|"hive.stats.estimate"
argument_list|,
literal|true
argument_list|,
literal|"Estimate statistics in absence of statistics."
argument_list|)
block|,
name|HIVE_STATS_NDV_ESTIMATE_PERC
argument_list|(
literal|"hive.stats.ndv.estimate.percent"
argument_list|,
operator|(
name|float
operator|)
literal|20
argument_list|,
literal|"This many percentage of rows will be estimated as count distinct in absence of statistics."
argument_list|)
block|,
name|HIVE_STATS_NUM_NULLS_ESTIMATE_PERC
argument_list|(
literal|"hive.stats.num.nulls.estimate.percent"
argument_list|,
operator|(
name|float
operator|)
literal|5
argument_list|,
literal|"This many percentage of rows will be estimated as number of nulls in absence of statistics."
argument_list|)
block|,
name|HIVESTATSAUTOGATHER
argument_list|(
literal|"hive.stats.autogather"
argument_list|,
literal|true
argument_list|,
literal|"A flag to gather statistics (only basic) automatically during the INSERT OVERWRITE command."
argument_list|)
block|,
name|HIVESTATSCOLAUTOGATHER
argument_list|(
literal|"hive.stats.column.autogather"
argument_list|,
literal|true
argument_list|,
literal|"A flag to gather column statistics automatically."
argument_list|)
block|,
name|HIVESTATSDBCLASS
argument_list|(
literal|"hive.stats.dbclass"
argument_list|,
literal|"fs"
argument_list|,
operator|new
name|PatternSet
argument_list|(
literal|"custom"
argument_list|,
literal|"fs"
argument_list|)
argument_list|,
literal|"The storage that stores temporary Hive statistics. In filesystem based statistics collection ('fs'), \n"
operator|+
literal|"each task writes statistics it has collected in a file on the filesystem, which will be aggregated \n"
operator|+
literal|"after the job has finished. Supported values are fs (filesystem) and custom as defined in StatsSetupConst.java."
argument_list|)
block|,
comment|// StatsSetupConst.StatDB
comment|/**      * @deprecated Use MetastoreConf.STATS_DEFAULT_PUBLISHER      */
annotation|@
name|Deprecated
name|HIVE_STATS_DEFAULT_PUBLISHER
argument_list|(
literal|"hive.stats.default.publisher"
argument_list|,
literal|""
argument_list|,
literal|"The Java class (implementing the StatsPublisher interface) that is used by default if hive.stats.dbclass is custom type."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.STATS_DEFAULT_AGGRETATOR      */
annotation|@
name|Deprecated
name|HIVE_STATS_DEFAULT_AGGREGATOR
argument_list|(
literal|"hive.stats.default.aggregator"
argument_list|,
literal|""
argument_list|,
literal|"The Java class (implementing the StatsAggregator interface) that is used by default if hive.stats.dbclass is custom type."
argument_list|)
block|,
name|CLIENT_STATS_COUNTERS
argument_list|(
literal|"hive.client.stats.counters"
argument_list|,
literal|""
argument_list|,
literal|"Subset of counters that should be of interest for hive.client.stats.publishers (when one wants to limit their publishing). \n"
operator|+
literal|"Non-display names should be used"
argument_list|)
block|,
comment|//Subset of counters that should be of interest for hive.client.stats.publishers (when one wants to limit their publishing). Non-display names should be used".
name|HIVE_STATS_RELIABLE
argument_list|(
literal|"hive.stats.reliable"
argument_list|,
literal|false
argument_list|,
literal|"Whether queries will fail because stats cannot be collected completely accurately. \n"
operator|+
literal|"If this is set to true, reading/writing from/into a partition may fail because the stats\n"
operator|+
literal|"could not be computed accurately."
argument_list|)
block|,
name|HIVE_STATS_COLLECT_PART_LEVEL_STATS
argument_list|(
literal|"hive.analyze.stmt.collect.partlevel.stats"
argument_list|,
literal|true
argument_list|,
literal|"analyze table T compute statistics for columns. Queries like these should compute partition"
operator|+
literal|"level stats for partitioned table even when no part spec is specified."
argument_list|)
block|,
name|HIVE_STATS_GATHER_NUM_THREADS
argument_list|(
literal|"hive.stats.gather.num.threads"
argument_list|,
literal|10
argument_list|,
literal|"Number of threads used by noscan analyze command for partitioned tables.\n"
operator|+
literal|"This is applicable only for file formats that implement StatsProvidingRecordReader (like ORC)."
argument_list|)
block|,
comment|// Collect table access keys information for operators that can benefit from bucketing
name|HIVE_STATS_COLLECT_TABLEKEYS
argument_list|(
literal|"hive.stats.collect.tablekeys"
argument_list|,
literal|false
argument_list|,
literal|"Whether join and group by keys on tables are derived and maintained in the QueryPlan.\n"
operator|+
literal|"This is useful to identify how tables are accessed and to determine if they should be bucketed."
argument_list|)
block|,
comment|// Collect column access information
name|HIVE_STATS_COLLECT_SCANCOLS
argument_list|(
literal|"hive.stats.collect.scancols"
argument_list|,
literal|false
argument_list|,
literal|"Whether column accesses are tracked in the QueryPlan.\n"
operator|+
literal|"This is useful to identify how tables are accessed and to determine if there are wasted columns that can be trimmed."
argument_list|)
block|,
name|HIVE_STATS_NDV_ALGO
argument_list|(
literal|"hive.stats.ndv.algo"
argument_list|,
literal|"hll"
argument_list|,
operator|new
name|PatternSet
argument_list|(
literal|"hll"
argument_list|,
literal|"fm"
argument_list|)
argument_list|,
literal|"hll and fm stand for HyperLogLog and FM-sketch, respectively for computing ndv."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.STATS_FETCH_BITVECTOR      */
annotation|@
name|Deprecated
name|HIVE_STATS_FETCH_BITVECTOR
argument_list|(
literal|"hive.stats.fetch.bitvector"
argument_list|,
literal|false
argument_list|,
literal|"Whether we fetch bitvector when we compute ndv. Users can turn it off if they want to use old schema"
argument_list|)
block|,
comment|// standard error allowed for ndv estimates for FM-sketch. A lower value indicates higher accuracy and a
comment|// higher compute cost.
name|HIVE_STATS_NDV_ERROR
argument_list|(
literal|"hive.stats.ndv.error"
argument_list|,
operator|(
name|float
operator|)
literal|20.0
argument_list|,
literal|"The standard error allowed for NDV estimates, expressed in percentage. This provides a tradeoff \n"
operator|+
literal|"between accuracy and compute cost. A lower value for the error indicates higher accuracy and a \n"
operator|+
literal|"higher compute cost. (NDV means the number of distinct values.). It only affects the FM-Sketch \n"
operator|+
literal|"(not the HLL algorithm which is the default), where it computes the number of necessary\n"
operator|+
literal|" bitvectors to achieve the accuracy."
argument_list|)
block|,
name|HIVE_STATS_ESTIMATORS_ENABLE
argument_list|(
literal|"hive.stats.estimators.enable"
argument_list|,
literal|true
argument_list|,
literal|"Estimators are able to provide more accurate column statistic infos for UDF results."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.STATS_NDV_TUNER      */
annotation|@
name|Deprecated
name|HIVE_METASTORE_STATS_NDV_TUNER
argument_list|(
literal|"hive.metastore.stats.ndv.tuner"
argument_list|,
operator|(
name|float
operator|)
literal|0.0
argument_list|,
literal|"Provides a tunable parameter between the lower bound and the higher bound of ndv for aggregate ndv across all the partitions. \n"
operator|+
literal|"The lower bound is equal to the maximum of ndv of all the partitions. The higher bound is equal to the sum of ndv of all the partitions.\n"
operator|+
literal|"Its value should be between 0.0 (i.e., choose lower bound) and 1.0 (i.e., choose higher bound)"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.STATS_NDV_DENSITY_FUNCTION      */
annotation|@
name|Deprecated
name|HIVE_METASTORE_STATS_NDV_DENSITY_FUNCTION
argument_list|(
literal|"hive.metastore.stats.ndv.densityfunction"
argument_list|,
literal|false
argument_list|,
literal|"Whether to use density function to estimate the NDV for the whole table based on the NDV of partitions"
argument_list|)
block|,
name|HIVE_STATS_KEY_PREFIX
argument_list|(
literal|"hive.stats.key.prefix"
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
literal|true
argument_list|)
block|,
comment|// internal usage only
comment|// if length of variable length data type cannot be determined this length will be used.
name|HIVE_STATS_MAX_VARIABLE_LENGTH
argument_list|(
literal|"hive.stats.max.variable.length"
argument_list|,
literal|100
argument_list|,
literal|"To estimate the size of data flowing through operators in Hive/Tez(for reducer estimation etc.),\n"
operator|+
literal|"average row size is multiplied with the total number of rows coming out of each operator.\n"
operator|+
literal|"Average row size is computed from average column size of all columns in the row. In the absence\n"
operator|+
literal|"of column statistics, for variable length columns (like string, bytes etc.), this value will be\n"
operator|+
literal|"used. For fixed length columns their corresponding Java equivalent sizes are used\n"
operator|+
literal|"(float - 4 bytes, double - 8 bytes etc.)."
argument_list|)
block|,
comment|// if number of elements in list cannot be determined, this value will be used
name|HIVE_STATS_LIST_NUM_ENTRIES
argument_list|(
literal|"hive.stats.list.num.entries"
argument_list|,
literal|10
argument_list|,
literal|"To estimate the size of data flowing through operators in Hive/Tez(for reducer estimation etc.),\n"
operator|+
literal|"average row size is multiplied with the total number of rows coming out of each operator.\n"
operator|+
literal|"Average row size is computed from average column size of all columns in the row. In the absence\n"
operator|+
literal|"of column statistics and for variable length complex columns like list, the average number of\n"
operator|+
literal|"entries/values can be specified using this config."
argument_list|)
block|,
comment|// if number of elements in map cannot be determined, this value will be used
name|HIVE_STATS_MAP_NUM_ENTRIES
argument_list|(
literal|"hive.stats.map.num.entries"
argument_list|,
literal|10
argument_list|,
literal|"To estimate the size of data flowing through operators in Hive/Tez(for reducer estimation etc.),\n"
operator|+
literal|"average row size is multiplied with the total number of rows coming out of each operator.\n"
operator|+
literal|"Average row size is computed from average column size of all columns in the row. In the absence\n"
operator|+
literal|"of column statistics and for variable length complex columns like map, the average number of\n"
operator|+
literal|"entries/values can be specified using this config."
argument_list|)
block|,
comment|// statistics annotation fetches column statistics for all required columns which can
comment|// be very expensive sometimes
name|HIVE_STATS_FETCH_COLUMN_STATS
argument_list|(
literal|"hive.stats.fetch.column.stats"
argument_list|,
literal|true
argument_list|,
literal|"Annotation of operator tree with statistics information requires column statistics.\n"
operator|+
literal|"Column statistics are fetched from metastore. Fetching column statistics for each needed column\n"
operator|+
literal|"can be expensive when the number of columns is high. This flag can be used to disable fetching\n"
operator|+
literal|"of column statistics from metastore."
argument_list|)
block|,
comment|// in the absence of column statistics, the estimated number of rows/data size that will
comment|// be emitted from join operator will depend on this factor
name|HIVE_STATS_JOIN_FACTOR
argument_list|(
literal|"hive.stats.join.factor"
argument_list|,
operator|(
name|float
operator|)
literal|1.1
argument_list|,
literal|"Hive/Tez optimizer estimates the data size flowing through each of the operators. JOIN operator\n"
operator|+
literal|"uses column statistics to estimate the number of rows flowing out of it and hence the data size.\n"
operator|+
literal|"In the absence of column statistics, this factor determines the amount of rows that flows out\n"
operator|+
literal|"of JOIN operator."
argument_list|)
block|,
name|HIVE_STATS_CORRELATED_MULTI_KEY_JOINS
argument_list|(
literal|"hive.stats.correlated.multi.key.joins"
argument_list|,
literal|true
argument_list|,
literal|"When estimating output rows for a join involving multiple columns, the default behavior assumes"
operator|+
literal|"the columns are independent. Setting this flag to true will cause the estimator to assume"
operator|+
literal|"the columns are correlated."
argument_list|)
block|,
name|HIVE_STATS_RANGE_SELECTIVITY_UNIFORM_DISTRIBUTION
argument_list|(
literal|"hive.stats.filter.range.uniform"
argument_list|,
literal|true
argument_list|,
literal|"When estimating output rows from a condition, if a range predicate is applied over a column and the\n"
operator|+
literal|"minimum and maximum values for that column are available, assume uniform distribution of values\n"
operator|+
literal|"across that range and scales number of rows proportionally. If this is set to false, default\n"
operator|+
literal|"selectivity value is used."
argument_list|)
block|,
comment|// in the absence of uncompressed/raw data size, total file size will be used for statistics
comment|// annotation. But the file may be compressed, encoded and serialized which may be lesser in size
comment|// than the actual uncompressed/raw data size. This factor will be multiplied to file size to estimate
comment|// the raw data size.
name|HIVE_STATS_DESERIALIZATION_FACTOR
argument_list|(
literal|"hive.stats.deserialization.factor"
argument_list|,
operator|(
name|float
operator|)
literal|10.0
argument_list|,
literal|"Hive/Tez optimizer estimates the data size flowing through each of the operators. In the absence\n"
operator|+
literal|"of basic statistics like number of rows and data size, file size is used to estimate the number\n"
operator|+
literal|"of rows and data size. Since files in tables/partitions are serialized (and optionally\n"
operator|+
literal|"compressed) the estimates of number of rows and data size cannot be reliably determined.\n"
operator|+
literal|"This factor is multiplied with the file size to account for serialization and compression."
argument_list|)
block|,
name|HIVE_STATS_IN_CLAUSE_FACTOR
argument_list|(
literal|"hive.stats.filter.in.factor"
argument_list|,
operator|(
name|float
operator|)
literal|1.0
argument_list|,
literal|"Currently column distribution is assumed to be uniform. This can lead to overestimation/underestimation\n"
operator|+
literal|"in the number of rows filtered by a certain operator, which in turn might lead to overprovision or\n"
operator|+
literal|"underprovision of resources. This factor is applied to the cardinality estimation of IN clauses in\n"
operator|+
literal|"filter operators."
argument_list|)
block|,
name|HIVE_STATS_IN_MIN_RATIO
argument_list|(
literal|"hive.stats.filter.in.min.ratio"
argument_list|,
literal|0.0f
argument_list|,
literal|"Output estimation of an IN filter can't be lower than this ratio"
argument_list|)
block|,
name|HIVE_STATS_UDTF_FACTOR
argument_list|(
literal|"hive.stats.udtf.factor"
argument_list|,
operator|(
name|float
operator|)
literal|1.0
argument_list|,
literal|"UDTFs change the number of rows of the output. A common UDTF is the explode() method that creates\n"
operator|+
literal|"multiple rows for each element in the input array. This factor is applied to the number of\n"
operator|+
literal|"output rows and output size."
argument_list|)
block|,
comment|// Concurrency
name|HIVE_SUPPORT_CONCURRENCY
argument_list|(
literal|"hive.support.concurrency"
argument_list|,
literal|false
argument_list|,
literal|"Whether Hive supports concurrency control or not. \n"
operator|+
literal|"A ZooKeeper instance must be up and running when using zookeeper Hive lock manager "
argument_list|)
block|,
name|HIVE_LOCK_MANAGER
argument_list|(
literal|"hive.lock.manager"
argument_list|,
literal|"org.apache.hadoop.hive.ql.lockmgr.zookeeper.ZooKeeperHiveLockManager"
argument_list|,
literal|""
argument_list|)
block|,
name|HIVE_LOCK_NUMRETRIES
argument_list|(
literal|"hive.lock.numretries"
argument_list|,
literal|100
argument_list|,
literal|"The number of times you want to try to get all the locks"
argument_list|)
block|,
name|HIVE_UNLOCK_NUMRETRIES
argument_list|(
literal|"hive.unlock.numretries"
argument_list|,
literal|10
argument_list|,
literal|"The number of times you want to retry to do one unlock"
argument_list|)
block|,
name|HIVE_LOCK_SLEEP_BETWEEN_RETRIES
argument_list|(
literal|"hive.lock.sleep.between.retries"
argument_list|,
literal|"60s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
literal|0L
argument_list|,
literal|false
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
literal|false
argument_list|)
argument_list|,
literal|"The maximum sleep time between various retries"
argument_list|)
block|,
name|HIVE_LOCK_MAPRED_ONLY
argument_list|(
literal|"hive.lock.mapred.only.operation"
argument_list|,
literal|false
argument_list|,
literal|"This param is to control whether or not only do lock on queries\n"
operator|+
literal|"that need to execute at least one mapred job."
argument_list|)
block|,
name|HIVE_LOCK_QUERY_STRING_MAX_LENGTH
argument_list|(
literal|"hive.lock.query.string.max.length"
argument_list|,
literal|1000000
argument_list|,
literal|"The maximum length of the query string to store in the lock.\n"
operator|+
literal|"The default value is 1000000, since the data limit of a znode is 1MB"
argument_list|)
block|,
name|HIVE_MM_ALLOW_ORIGINALS
argument_list|(
literal|"hive.mm.allow.originals"
argument_list|,
literal|false
argument_list|,
literal|"Whether to allow original files in MM tables. Conversion to MM may be expensive if\n"
operator|+
literal|"this is set to false, however unless MAPREDUCE-7086 fix is present, queries that\n"
operator|+
literal|"read MM tables with original files will fail. The default in Hive 3.0 is false."
argument_list|)
block|,
comment|// Zookeeper related configs
name|HIVE_ZOOKEEPER_USE_KERBEROS
argument_list|(
literal|"hive.zookeeper.kerberos.enabled"
argument_list|,
literal|true
argument_list|,
literal|"If ZooKeeper is configured for Kerberos authentication. This could be useful when cluster\n"
operator|+
literal|"is kerberized, but Zookeeper is not."
argument_list|)
block|,
name|HIVE_ZOOKEEPER_QUORUM
argument_list|(
literal|"hive.zookeeper.quorum"
argument_list|,
literal|""
argument_list|,
literal|"List of ZooKeeper servers to talk to. This is needed for: \n"
operator|+
literal|"1. Read/write locks - when hive.lock.manager is set to \n"
operator|+
literal|"org.apache.hadoop.hive.ql.lockmgr.zookeeper.ZooKeeperHiveLockManager, \n"
operator|+
literal|"2. When HiveServer2 supports service discovery via Zookeeper.\n"
operator|+
literal|"3. For delegation token storage if zookeeper store is used, if\n"
operator|+
literal|"hive.cluster.delegation.token.store.zookeeper.connectString is not set\n"
operator|+
literal|"4. LLAP daemon registry service\n"
operator|+
literal|"5. Leader selection for privilege synchronizer"
argument_list|)
block|,
name|HIVE_ZOOKEEPER_CLIENT_PORT
argument_list|(
literal|"hive.zookeeper.client.port"
argument_list|,
literal|"2181"
argument_list|,
literal|"The port of ZooKeeper servers to talk to.\n"
operator|+
literal|"If the list of Zookeeper servers specified in hive.zookeeper.quorum\n"
operator|+
literal|"does not contain port numbers, this value is used."
argument_list|)
block|,
name|HIVE_ZOOKEEPER_SESSION_TIMEOUT
argument_list|(
literal|"hive.zookeeper.session.timeout"
argument_list|,
literal|"120000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"ZooKeeper client's session timeout (in milliseconds). The client is disconnected, and as a result, all locks released, \n"
operator|+
literal|"if a heartbeat is not sent in the timeout."
argument_list|)
block|,
name|HIVE_ZOOKEEPER_CONNECTION_TIMEOUT
argument_list|(
literal|"hive.zookeeper.connection.timeout"
argument_list|,
literal|"15s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"ZooKeeper client's connection timeout in seconds. Connection timeout * hive.zookeeper.connection.max.retries\n"
operator|+
literal|"with exponential backoff is when curator client deems connection is lost to zookeeper."
argument_list|)
block|,
name|HIVE_ZOOKEEPER_NAMESPACE
argument_list|(
literal|"hive.zookeeper.namespace"
argument_list|,
literal|"hive_zookeeper_namespace"
argument_list|,
literal|"The parent node under which all ZooKeeper nodes are created."
argument_list|)
block|,
name|HIVE_ZOOKEEPER_CLEAN_EXTRA_NODES
argument_list|(
literal|"hive.zookeeper.clean.extra.nodes"
argument_list|,
literal|false
argument_list|,
literal|"Clean extra nodes at the end of the session."
argument_list|)
block|,
name|HIVE_ZOOKEEPER_CONNECTION_MAX_RETRIES
argument_list|(
literal|"hive.zookeeper.connection.max.retries"
argument_list|,
literal|3
argument_list|,
literal|"Max number of times to retry when connecting to the ZooKeeper server."
argument_list|)
block|,
name|HIVE_ZOOKEEPER_CONNECTION_BASESLEEPTIME
argument_list|(
literal|"hive.zookeeper.connection.basesleeptime"
argument_list|,
literal|"1000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Initial amount of time (in milliseconds) to wait between retries\n"
operator|+
literal|"when connecting to the ZooKeeper server when using ExponentialBackoffRetry policy."
argument_list|)
block|,
comment|// Transactions
name|HIVE_TXN_MANAGER
argument_list|(
literal|"hive.txn.manager"
argument_list|,
literal|"org.apache.hadoop.hive.ql.lockmgr.DummyTxnManager"
argument_list|,
literal|"Set to org.apache.hadoop.hive.ql.lockmgr.DbTxnManager as part of turning on Hive\n"
operator|+
literal|"transactions, which also requires appropriate settings for hive.compactor.initiator.on,\n"
operator|+
literal|"hive.compactor.worker.threads, hive.support.concurrency (true),\n"
operator|+
literal|"and hive.exec.dynamic.partition.mode (nonstrict).\n"
operator|+
literal|"The default DummyTxnManager replicates pre-Hive-0.13 behavior and provides\n"
operator|+
literal|"no transactions."
argument_list|)
block|,
name|HIVE_TXN_STRICT_LOCKING_MODE
argument_list|(
literal|"hive.txn.strict.locking.mode"
argument_list|,
literal|true
argument_list|,
literal|"In strict mode non-ACID\n"
operator|+
literal|"resources use standard R/W lock semantics, e.g. INSERT will acquire exclusive lock.\n"
operator|+
literal|"In nonstrict mode, for non-ACID resources, INSERT will only acquire shared lock, which\n"
operator|+
literal|"allows two concurrent writes to the same partition but still lets lock manager prevent\n"
operator|+
literal|"DROP TABLE etc. when the table is being written to"
argument_list|)
block|,
name|HIVE_TXN_NONACID_READ_LOCKS
argument_list|(
literal|"hive.txn.nonacid.read.locks"
argument_list|,
literal|true
argument_list|,
literal|"Flag to turn off the read locks for non-ACID tables, when set to false.\n"
operator|+
literal|"Could be exercised to improve the performance of non-ACID tables in clusters where read locking "
operator|+
literal|"is enabled globally to support ACID. Can cause issues with concurrent DDL operations, or slow S3 writes."
argument_list|)
block|,
name|HIVE_TXN_READ_LOCKS
argument_list|(
literal|"hive.txn.read.locks"
argument_list|,
literal|true
argument_list|,
literal|"Flag to turn off the read locks, when set to false. Although its not recommended, \n"
operator|+
literal|"but in performance critical scenarios this option may be exercised."
argument_list|)
block|,
name|TXN_OVERWRITE_X_LOCK
argument_list|(
literal|"hive.txn.xlock.iow"
argument_list|,
literal|true
argument_list|,
literal|"Ensures commands with OVERWRITE (such as INSERT OVERWRITE) acquire Exclusive locks for\n"
operator|+
literal|"transactional tables. This ensures that inserts (w/o overwrite) running concurrently\n"
operator|+
literal|"are not hidden by the INSERT OVERWRITE."
argument_list|)
block|,
name|HIVE_TXN_STATS_ENABLED
argument_list|(
literal|"hive.txn.stats.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Whether Hive supports transactional stats (accurate stats for transactional tables)"
argument_list|)
block|,
name|HIVE_TXN_ACID_DIR_CACHE_DURATION
argument_list|(
literal|"hive.txn.acid.dir.cache.duration"
argument_list|,
literal|120
argument_list|,
literal|"Enable dir cache for ACID tables specified in minutes."
operator|+
literal|"0 indicates cache is disabled. "
argument_list|)
block|,
name|HIVE_TXN_READONLY_ENABLED
argument_list|(
literal|"hive.txn.readonly.enabled"
argument_list|,
literal|false
argument_list|,
literal|"Enables read-only transaction classification and related optimizations"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.TXN_TIMEOUT      */
annotation|@
name|Deprecated
name|HIVE_TXN_TIMEOUT
argument_list|(
literal|"hive.txn.timeout"
argument_list|,
literal|"300s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"time after which transactions are declared aborted if the client has not sent a heartbeat."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.TXN_HEARTBEAT_THREADPOOL_SIZE      */
annotation|@
name|Deprecated
name|HIVE_TXN_HEARTBEAT_THREADPOOL_SIZE
argument_list|(
literal|"hive.txn.heartbeat.threadpool.size"
argument_list|,
literal|5
argument_list|,
literal|"The number of "
operator|+
literal|"threads to use for heartbeating. For Hive CLI, 1 is enough. For HiveServer2, we need a few"
argument_list|)
block|,
name|TXN_MGR_DUMP_LOCK_STATE_ON_ACQUIRE_TIMEOUT
argument_list|(
literal|"hive.txn.manager.dump.lock.state.on.acquire.timeout"
argument_list|,
literal|false
argument_list|,
literal|"Set this to true so that when attempt to acquire a lock on resource times out, the current state"
operator|+
literal|" of the lock manager is dumped to log file.  This is for debugging.  See also "
operator|+
literal|"hive.lock.numretries and hive.lock.sleep.between.retries."
argument_list|)
block|,
name|HIVE_TXN_OPERATIONAL_PROPERTIES
argument_list|(
literal|"hive.txn.operational.properties"
argument_list|,
literal|1
argument_list|,
literal|"1: Enable split-update feature found in the newer version of Hive ACID subsystem\n"
operator|+
literal|"4: Make the table 'quarter-acid' as it only supports insert. But it doesn't require ORC or bucketing.\n"
operator|+
literal|"This is intended to be used as an internal property for future versions of ACID. (See\n"
operator|+
literal|"HIVE-14035 for details.  User sets it tblproperites via transactional_properties.)"
argument_list|,
literal|true
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.MAX_OPEN_TXNS      */
annotation|@
name|Deprecated
name|HIVE_MAX_OPEN_TXNS
argument_list|(
literal|"hive.max.open.txns"
argument_list|,
literal|100000
argument_list|,
literal|"Maximum number of open transactions. If \n"
operator|+
literal|"current open transactions reach this limit, future open transaction requests will be \n"
operator|+
literal|"rejected, until this number goes below the limit."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.COUNT_OPEN_TXNS_INTERVAL      */
annotation|@
name|Deprecated
name|HIVE_COUNT_OPEN_TXNS_INTERVAL
argument_list|(
literal|"hive.count.open.txns.interval"
argument_list|,
literal|"1s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Time in seconds between checks to count open transactions."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.TXN_MAX_OPEN_BATCH      */
annotation|@
name|Deprecated
name|HIVE_TXN_MAX_OPEN_BATCH
argument_list|(
literal|"hive.txn.max.open.batch"
argument_list|,
literal|1000
argument_list|,
literal|"Maximum number of transactions that can be fetched in one call to open_txns().\n"
operator|+
literal|"This controls how many transactions streaming agents such as Flume or Storm open\n"
operator|+
literal|"simultaneously. The streaming agent then writes that number of entries into a single\n"
operator|+
literal|"file (per Flume agent or Storm bolt). Thus increasing this value decreases the number\n"
operator|+
literal|"of delta files created by streaming agents. But it also increases the number of open\n"
operator|+
literal|"transactions that Hive has to track at any given time, which may negatively affect\n"
operator|+
literal|"read performance."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.TXN_RETRYABLE_SQLEX_REGEX      */
annotation|@
name|Deprecated
name|HIVE_TXN_RETRYABLE_SQLEX_REGEX
argument_list|(
literal|"hive.txn.retryable.sqlex.regex"
argument_list|,
literal|""
argument_list|,
literal|"Comma separated list\n"
operator|+
literal|"of regular expression patterns for SQL state, error code, and error message of\n"
operator|+
literal|"retryable SQLExceptions, that's suitable for the metastore DB.\n"
operator|+
literal|"For example: Can't serialize.*,40001$,^Deadlock,.*ORA-08176.*\n"
operator|+
literal|"The string that the regex will be matched against is of the following form, where ex is a SQLException:\n"
operator|+
literal|"ex.getMessage() + \" (SQLState=\" + ex.getSQLState() + \", ErrorCode=\" + ex.getErrorCode() + \")\""
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.COMPACTOR_INITIATOR_ON      */
annotation|@
name|Deprecated
name|HIVE_COMPACTOR_INITIATOR_ON
argument_list|(
literal|"hive.compactor.initiator.on"
argument_list|,
literal|false
argument_list|,
literal|"Whether to run the initiator and cleaner threads on this metastore instance or not.\n"
operator|+
literal|"Set this to true on one instance of the Thrift metastore service as part of turning\n"
operator|+
literal|"on Hive transactions. For a complete list of parameters required for turning on\n"
operator|+
literal|"transactions, see hive.txn.manager."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.COMPACTOR_WORKER_THREADS      */
annotation|@
name|Deprecated
name|HIVE_COMPACTOR_WORKER_THREADS
argument_list|(
literal|"hive.compactor.worker.threads"
argument_list|,
literal|0
argument_list|,
literal|"How many compactor worker threads to run on this metastore instance. Set this to a\n"
operator|+
literal|"positive number on one or more instances of the Thrift metastore service as part of\n"
operator|+
literal|"turning on Hive transactions. For a complete list of parameters required for turning\n"
operator|+
literal|"on transactions, see hive.txn.manager.\n"
operator|+
literal|"Worker threads spawn MapReduce jobs to do compactions. They do not do the compactions\n"
operator|+
literal|"themselves. Increasing the number of worker threads will decrease the time it takes\n"
operator|+
literal|"tables or partitions to be compacted once they are determined to need compaction.\n"
operator|+
literal|"It will also increase the background load on the Hadoop cluster as more MapReduce jobs\n"
operator|+
literal|"will be running in the background."
argument_list|)
block|,
name|HIVE_COMPACTOR_WORKER_TIMEOUT
argument_list|(
literal|"hive.compactor.worker.timeout"
argument_list|,
literal|"86400s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Time in seconds after which a compaction job will be declared failed and the\n"
operator|+
literal|"compaction re-queued."
argument_list|)
block|,
name|HIVE_COMPACTOR_CHECK_INTERVAL
argument_list|(
literal|"hive.compactor.check.interval"
argument_list|,
literal|"300s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Time in seconds between checks to see if any tables or partitions need to be\n"
operator|+
literal|"compacted. This should be kept high because each check for compaction requires\n"
operator|+
literal|"many calls against the NameNode.\n"
operator|+
literal|"Decreasing this value will reduce the time it takes for compaction to be started\n"
operator|+
literal|"for a table or partition that requires compaction. However, checking if compaction\n"
operator|+
literal|"is needed requires several calls to the NameNode for each table or partition that\n"
operator|+
literal|"has had a transaction done on it since the last major compaction. So decreasing this\n"
operator|+
literal|"value will increase the load on the NameNode."
argument_list|)
block|,
name|HIVE_COMPACTOR_REQUEST_QUEUE
argument_list|(
literal|"hive.compactor.request.queue"
argument_list|,
literal|1
argument_list|,
literal|"Enables parallelization of the checkForCompaction operation, that includes many file metadata checks\n"
operator|+
literal|"and may be expensive"
argument_list|)
block|,
name|HIVE_COMPACTOR_DELTA_NUM_THRESHOLD
argument_list|(
literal|"hive.compactor.delta.num.threshold"
argument_list|,
literal|10
argument_list|,
literal|"Number of delta directories in a table or partition that will trigger a minor\n"
operator|+
literal|"compaction."
argument_list|)
block|,
name|HIVE_COMPACTOR_DELTA_PCT_THRESHOLD
argument_list|(
literal|"hive.compactor.delta.pct.threshold"
argument_list|,
literal|0.1f
argument_list|,
literal|"Percentage (fractional) size of the delta files relative to the base that will trigger\n"
operator|+
literal|"a major compaction. (1.0 = 100%, so the default 0.1 = 10%.)"
argument_list|)
block|,
name|COMPACTOR_MAX_NUM_DELTA
argument_list|(
literal|"hive.compactor.max.num.delta"
argument_list|,
literal|500
argument_list|,
literal|"Maximum number of delta files that "
operator|+
literal|"the compactor will attempt to handle in a single job."
argument_list|)
block|,
name|HIVE_COMPACTOR_ABORTEDTXN_THRESHOLD
argument_list|(
literal|"hive.compactor.abortedtxn.threshold"
argument_list|,
literal|1000
argument_list|,
literal|"Number of aborted transactions involving a given table or partition that will trigger\n"
operator|+
literal|"a major compaction."
argument_list|)
block|,
name|HIVE_COMPACTOR_WAIT_TIMEOUT
argument_list|(
literal|"hive.compactor.wait.timeout"
argument_list|,
literal|300000L
argument_list|,
literal|"Time out in "
operator|+
literal|"milliseconds for blocking compaction. It's value has to be higher than 2000 milliseconds. "
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.COMPACTOR_INITIATOR_FAILED_THRESHOLD      */
annotation|@
name|Deprecated
name|COMPACTOR_INITIATOR_FAILED_THRESHOLD
argument_list|(
literal|"hive.compactor.initiator.failed.compacts.threshold"
argument_list|,
literal|2
argument_list|,
operator|new
name|RangeValidator
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|,
literal|"Number of consecutive compaction failures (per table/partition) "
operator|+
literal|"after which automatic compactions will not be scheduled any more.  Note that this must be less "
operator|+
literal|"than hive.compactor.history.retention.failed."
argument_list|)
block|,
name|HIVE_COMPACTOR_CLEANER_RUN_INTERVAL
argument_list|(
literal|"hive.compactor.cleaner.run.interval"
argument_list|,
literal|"5000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Time between runs of the cleaner thread"
argument_list|)
block|,
name|COMPACTOR_JOB_QUEUE
argument_list|(
literal|"hive.compactor.job.queue"
argument_list|,
literal|""
argument_list|,
literal|"Used to specify name of Hadoop queue to which\n"
operator|+
literal|"Compaction jobs will be submitted.  Set to empty string to let Hadoop choose the queue."
argument_list|)
block|,
name|TRANSACTIONAL_CONCATENATE_NOBLOCK
argument_list|(
literal|"hive.transactional.concatenate.noblock"
argument_list|,
literal|false
argument_list|,
literal|"Will cause 'alter table T concatenate' to be non-blocking"
argument_list|)
block|,
name|HIVE_COMPACTOR_COMPACT_MM
argument_list|(
literal|"hive.compactor.compact.insert.only"
argument_list|,
literal|true
argument_list|,
literal|"Whether the compactor should compact insert-only tables. A safety switch."
argument_list|)
block|,
name|COMPACTOR_CRUD_QUERY_BASED
argument_list|(
literal|"hive.compactor.crud.query.based"
argument_list|,
literal|false
argument_list|,
literal|"Means Major compaction on full CRUD tables is done as a query, "
operator|+
literal|"and minor compaction will be disabled."
argument_list|)
block|,
name|SPLIT_GROUPING_MODE
argument_list|(
literal|"hive.split.grouping.mode"
argument_list|,
literal|"query"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"query"
argument_list|,
literal|"compactor"
argument_list|)
argument_list|,
literal|"This is set to compactor from within the query based compactor. This enables the Tez SplitGrouper "
operator|+
literal|"to group splits based on their bucket number, so that all rows from different bucket files "
operator|+
literal|" for the same bucket number can end up in the same bucket file after the compaction."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.COMPACTOR_HISTORY_RETENTION_SUCCEEDED      */
annotation|@
name|Deprecated
name|COMPACTOR_HISTORY_RETENTION_SUCCEEDED
argument_list|(
literal|"hive.compactor.history.retention.succeeded"
argument_list|,
literal|3
argument_list|,
operator|new
name|RangeValidator
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
argument_list|,
literal|"Determines how many successful compaction records will be "
operator|+
literal|"retained in compaction history for a given table/partition."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.COMPACTOR_HISTORY_RETENTION_FAILED      */
annotation|@
name|Deprecated
name|COMPACTOR_HISTORY_RETENTION_FAILED
argument_list|(
literal|"hive.compactor.history.retention.failed"
argument_list|,
literal|3
argument_list|,
operator|new
name|RangeValidator
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
argument_list|,
literal|"Determines how many failed compaction records will be "
operator|+
literal|"retained in compaction history for a given table/partition."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.COMPACTOR_HISTORY_RETENTION_ATTEMPTED      */
annotation|@
name|Deprecated
name|COMPACTOR_HISTORY_RETENTION_ATTEMPTED
argument_list|(
literal|"hive.compactor.history.retention.attempted"
argument_list|,
literal|2
argument_list|,
operator|new
name|RangeValidator
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
argument_list|,
literal|"Determines how many attempted compaction records will be "
operator|+
literal|"retained in compaction history for a given table/partition."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.COMPACTOR_HISTORY_REAPER_INTERVAL      */
annotation|@
name|Deprecated
name|COMPACTOR_HISTORY_REAPER_INTERVAL
argument_list|(
literal|"hive.compactor.history.reaper.interval"
argument_list|,
literal|"2m"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Determines how often compaction history reaper runs"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.TIMEDOUT_TXN_REAPER_START      */
annotation|@
name|Deprecated
name|HIVE_TIMEDOUT_TXN_REAPER_START
argument_list|(
literal|"hive.timedout.txn.reaper.start"
argument_list|,
literal|"100s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Time delay of 1st reaper run after metastore start"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.TIMEDOUT_TXN_REAPER_INTERVAL      */
annotation|@
name|Deprecated
name|HIVE_TIMEDOUT_TXN_REAPER_INTERVAL
argument_list|(
literal|"hive.timedout.txn.reaper.interval"
argument_list|,
literal|"180s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Time interval describing how often the reaper runs"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.WRITE_SET_REAPER_INTERVAL      */
annotation|@
name|Deprecated
name|WRITE_SET_REAPER_INTERVAL
argument_list|(
literal|"hive.writeset.reaper.interval"
argument_list|,
literal|"60s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Frequency of WriteSet reaper runs"
argument_list|)
block|,
name|MERGE_CARDINALITY_VIOLATION_CHECK
argument_list|(
literal|"hive.merge.cardinality.check"
argument_list|,
literal|true
argument_list|,
literal|"Set to true to ensure that each SQL Merge statement ensures that for each row in the target\n"
operator|+
literal|"table there is at most 1 matching row in the source table per SQL Specification."
argument_list|)
block|,
name|MERGE_SPLIT_UPDATE
argument_list|(
literal|"hive.merge.split.update"
argument_list|,
literal|false
argument_list|,
literal|"If true, SQL Merge statement will handle WHEN MATCHED UPDATE by splitting it into 2\n"
operator|+
literal|"branches of a multi-insert, representing delete of existing row and an insert of\n"
operator|+
literal|"the new version of the row.  Updating bucketing and partitioning columns should\n"
operator|+
literal|"only be permitted if this is true."
argument_list|)
block|,
name|OPTIMIZE_ACID_META_COLUMNS
argument_list|(
literal|"hive.optimize.acid.meta.columns"
argument_list|,
literal|true
argument_list|,
literal|"If true, don't decode Acid metadata columns from storage unless"
operator|+
literal|" they are needed."
argument_list|)
block|,
comment|// For Arrow SerDe
name|HIVE_ARROW_ROOT_ALLOCATOR_LIMIT
argument_list|(
literal|"hive.arrow.root.allocator.limit"
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
literal|"Arrow root allocator memory size limitation in bytes."
argument_list|)
block|,
name|HIVE_ARROW_BATCH_ALLOCATOR_LIMIT
argument_list|(
literal|"hive.arrow.batch.allocator.limit"
argument_list|,
literal|10_000_000_000L
argument_list|,
literal|"Max bytes per arrow batch. This is a threshold, the memory is not pre-allocated."
argument_list|)
block|,
name|HIVE_ARROW_BATCH_SIZE
argument_list|(
literal|"hive.arrow.batch.size"
argument_list|,
literal|1000
argument_list|,
literal|"The number of rows sent in one Arrow batch."
argument_list|)
block|,
comment|// For Druid storage handler
name|HIVE_DRUID_INDEXING_GRANULARITY
argument_list|(
literal|"hive.druid.indexer.segments.granularity"
argument_list|,
literal|"DAY"
argument_list|,
operator|new
name|PatternSet
argument_list|(
literal|"YEAR"
argument_list|,
literal|"MONTH"
argument_list|,
literal|"WEEK"
argument_list|,
literal|"DAY"
argument_list|,
literal|"HOUR"
argument_list|,
literal|"MINUTE"
argument_list|,
literal|"SECOND"
argument_list|)
argument_list|,
literal|"Granularity for the segments created by the Druid storage handler"
argument_list|)
block|,
name|HIVE_DRUID_MAX_PARTITION_SIZE
argument_list|(
literal|"hive.druid.indexer.partition.size.max"
argument_list|,
literal|5000000
argument_list|,
literal|"Maximum number of records per segment partition"
argument_list|)
block|,
name|HIVE_DRUID_MAX_ROW_IN_MEMORY
argument_list|(
literal|"hive.druid.indexer.memory.rownum.max"
argument_list|,
literal|75000
argument_list|,
literal|"Maximum number of records in memory while storing data in Druid"
argument_list|)
block|,
name|HIVE_DRUID_BROKER_DEFAULT_ADDRESS
argument_list|(
literal|"hive.druid.broker.address.default"
argument_list|,
literal|"localhost:8082"
argument_list|,
literal|"Address of the Druid broker. If we are querying Druid from Hive, this address needs to be\n"
operator|+
literal|"declared"
argument_list|)
block|,
name|HIVE_DRUID_COORDINATOR_DEFAULT_ADDRESS
argument_list|(
literal|"hive.druid.coordinator.address.default"
argument_list|,
literal|"localhost:8081"
argument_list|,
literal|"Address of the Druid coordinator. It is used to check the load status of newly created segments"
argument_list|)
block|,
name|HIVE_DRUID_OVERLORD_DEFAULT_ADDRESS
argument_list|(
literal|"hive.druid.overlord.address.default"
argument_list|,
literal|"localhost:8090"
argument_list|,
literal|"Address of the Druid overlord. It is used to submit indexing tasks to druid."
argument_list|)
block|,
name|HIVE_DRUID_SELECT_THRESHOLD
argument_list|(
literal|"hive.druid.select.threshold"
argument_list|,
literal|10000
argument_list|,
literal|"Takes only effect when hive.druid.select.distribute is set to false. \n"
operator|+
literal|"When we can split a Select query, this is the maximum number of rows that we try to retrieve\n"
operator|+
literal|"per query. In order to do that, we obtain the estimated size for the complete result. If the\n"
operator|+
literal|"number of records of the query results is larger than this threshold, we split the query in\n"
operator|+
literal|"total number of rows/threshold parts across the time dimension. Note that we assume the\n"
operator|+
literal|"records to be split uniformly across the time dimension."
argument_list|)
block|,
name|HIVE_DRUID_NUM_HTTP_CONNECTION
argument_list|(
literal|"hive.druid.http.numConnection"
argument_list|,
literal|20
argument_list|,
literal|"Number of connections used by\n"
operator|+
literal|"the HTTP client."
argument_list|)
block|,
name|HIVE_DRUID_HTTP_READ_TIMEOUT
argument_list|(
literal|"hive.druid.http.read.timeout"
argument_list|,
literal|"PT1M"
argument_list|,
literal|"Read timeout period for the HTTP\n"
operator|+
literal|"client in ISO8601 format (for example P2W, P3M, PT1H30M, PT0.750S), default is period of 1 minute."
argument_list|)
block|,
name|HIVE_DRUID_SLEEP_TIME
argument_list|(
literal|"hive.druid.sleep.time"
argument_list|,
literal|"PT10S"
argument_list|,
literal|"Sleep time between retries in ISO8601 format (for example P2W, P3M, PT1H30M, PT0.750S), default is period of 10 seconds."
argument_list|)
block|,
name|HIVE_DRUID_BASE_PERSIST_DIRECTORY
argument_list|(
literal|"hive.druid.basePersistDirectory"
argument_list|,
literal|""
argument_list|,
literal|"Local temporary directory used to persist intermediate indexing state, will default to JVM system property java.io.tmpdir."
argument_list|)
block|,
name|HIVE_DRUID_ROLLUP
argument_list|(
literal|"hive.druid.rollup"
argument_list|,
literal|true
argument_list|,
literal|"Whether to rollup druid rows or not."
argument_list|)
block|,
name|DRUID_SEGMENT_DIRECTORY
argument_list|(
literal|"hive.druid.storage.storageDirectory"
argument_list|,
literal|"/druid/segments"
argument_list|,
literal|"druid deep storage location."
argument_list|)
block|,
name|DRUID_METADATA_BASE
argument_list|(
literal|"hive.druid.metadata.base"
argument_list|,
literal|"druid"
argument_list|,
literal|"Default prefix for metadata tables"
argument_list|)
block|,
name|DRUID_METADATA_DB_TYPE
argument_list|(
literal|"hive.druid.metadata.db.type"
argument_list|,
literal|"mysql"
argument_list|,
operator|new
name|PatternSet
argument_list|(
literal|"mysql"
argument_list|,
literal|"postgresql"
argument_list|,
literal|"derby"
argument_list|)
argument_list|,
literal|"Type of the metadata database."
argument_list|)
block|,
name|DRUID_METADATA_DB_USERNAME
argument_list|(
literal|"hive.druid.metadata.username"
argument_list|,
literal|""
argument_list|,
literal|"Username to connect to Type of the metadata DB."
argument_list|)
block|,
name|DRUID_METADATA_DB_PASSWORD
argument_list|(
literal|"hive.druid.metadata.password"
argument_list|,
literal|""
argument_list|,
literal|"Password to connect to Type of the metadata DB."
argument_list|)
block|,
name|DRUID_METADATA_DB_URI
argument_list|(
literal|"hive.druid.metadata.uri"
argument_list|,
literal|""
argument_list|,
literal|"URI to connect to the database (for example jdbc:mysql://hostname:port/DBName)."
argument_list|)
block|,
name|DRUID_WORKING_DIR
argument_list|(
literal|"hive.druid.working.directory"
argument_list|,
literal|"/tmp/workingDirectory"
argument_list|,
literal|"Default hdfs working directory used to store some intermediate metadata"
argument_list|)
block|,
name|HIVE_DRUID_MAX_TRIES
argument_list|(
literal|"hive.druid.maxTries"
argument_list|,
literal|5
argument_list|,
literal|"Maximum number of retries before giving up"
argument_list|)
block|,
name|HIVE_DRUID_PASSIVE_WAIT_TIME
argument_list|(
literal|"hive.druid.passiveWaitTimeMs"
argument_list|,
literal|30000L
argument_list|,
literal|"Wait time in ms default to 30 seconds."
argument_list|)
block|,
name|HIVE_DRUID_BITMAP_FACTORY_TYPE
argument_list|(
literal|"hive.druid.bitmap.type"
argument_list|,
literal|"roaring"
argument_list|,
operator|new
name|PatternSet
argument_list|(
literal|"roaring"
argument_list|,
literal|"concise"
argument_list|)
argument_list|,
literal|"Coding algorithm use to encode the bitmaps"
argument_list|)
block|,
comment|// For HBase storage handler
name|HIVE_HBASE_WAL_ENABLED
argument_list|(
literal|"hive.hbase.wal.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Whether writes to HBase should be forced to the write-ahead log. \n"
operator|+
literal|"Disabling this improves HBase write performance at the risk of lost writes in case of a crash."
argument_list|)
block|,
name|HIVE_HBASE_GENERATE_HFILES
argument_list|(
literal|"hive.hbase.generatehfiles"
argument_list|,
literal|false
argument_list|,
literal|"True when HBaseStorageHandler should generate hfiles instead of operate against the online table."
argument_list|)
block|,
name|HIVE_HBASE_SNAPSHOT_NAME
argument_list|(
literal|"hive.hbase.snapshot.name"
argument_list|,
literal|null
argument_list|,
literal|"The HBase table snapshot name to use."
argument_list|)
block|,
name|HIVE_HBASE_SNAPSHOT_RESTORE_DIR
argument_list|(
literal|"hive.hbase.snapshot.restoredir"
argument_list|,
literal|"/tmp"
argument_list|,
literal|"The directory in which to "
operator|+
literal|"restore the HBase table snapshot."
argument_list|)
block|,
comment|// For Kudu storage handler
name|HIVE_KUDU_MASTER_ADDRESSES_DEFAULT
argument_list|(
literal|"hive.kudu.master.addresses.default"
argument_list|,
literal|"localhost:7050"
argument_list|,
literal|"Comma-separated list of all of the Kudu master addresses.\n"
operator|+
literal|"This value is only used for a given table if the kudu.master_addresses table property is not set."
argument_list|)
block|,
comment|// For har files
name|HIVEARCHIVEENABLED
argument_list|(
literal|"hive.archive.enabled"
argument_list|,
literal|false
argument_list|,
literal|"Whether archiving operations are permitted"
argument_list|)
block|,
name|HIVEFETCHTASKCONVERSION
argument_list|(
literal|"hive.fetch.task.conversion"
argument_list|,
literal|"more"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"none"
argument_list|,
literal|"minimal"
argument_list|,
literal|"more"
argument_list|)
argument_list|,
literal|"Some select queries can be converted to single FETCH task minimizing latency.\n"
operator|+
literal|"Currently the query should be single sourced not having any subquery and should not have\n"
operator|+
literal|"any aggregations or distincts (which incurs RS), lateral views and joins.\n"
operator|+
literal|"0. none : disable hive.fetch.task.conversion\n"
operator|+
literal|"1. minimal : SELECT STAR, FILTER on partition columns, LIMIT only\n"
operator|+
literal|"2. more    : SELECT, FILTER, LIMIT only (support TABLESAMPLE and virtual columns)"
argument_list|)
block|,
name|HIVEFETCHTASKCONVERSIONTHRESHOLD
argument_list|(
literal|"hive.fetch.task.conversion.threshold"
argument_list|,
literal|1073741824L
argument_list|,
literal|"Input threshold for applying hive.fetch.task.conversion. If target table is native, input length\n"
operator|+
literal|"is calculated by summation of file lengths. If it's not native, storage handler for the table\n"
operator|+
literal|"can optionally implement org.apache.hadoop.hive.ql.metadata.InputEstimator interface."
argument_list|)
block|,
name|HIVEFETCHTASKAGGR
argument_list|(
literal|"hive.fetch.task.aggr"
argument_list|,
literal|false
argument_list|,
literal|"Aggregation queries with no group-by clause (for example, select count(*) from src) execute\n"
operator|+
literal|"final aggregations in single reduce task. If this is set true, Hive delegates final aggregation\n"
operator|+
literal|"stage to fetch task, possibly decreasing the query time."
argument_list|)
block|,
name|HIVEOPTIMIZEMETADATAQUERIES
argument_list|(
literal|"hive.compute.query.using.stats"
argument_list|,
literal|true
argument_list|,
literal|"When set to true Hive will answer a few queries like count(1) purely using stats\n"
operator|+
literal|"stored in metastore. For basic stats collection turn on the config hive.stats.autogather to true.\n"
operator|+
literal|"For more advanced stats collection need to run analyze table queries."
argument_list|)
block|,
comment|// Serde for FetchTask
name|HIVEFETCHOUTPUTSERDE
argument_list|(
literal|"hive.fetch.output.serde"
argument_list|,
literal|"org.apache.hadoop.hive.serde2.DelimitedJSONSerDe"
argument_list|,
literal|"The SerDe used by FetchTask to serialize the fetch output."
argument_list|)
block|,
name|HIVEEXPREVALUATIONCACHE
argument_list|(
literal|"hive.cache.expr.evaluation"
argument_list|,
literal|true
argument_list|,
literal|"If true, the evaluation result of a deterministic expression referenced twice or more\n"
operator|+
literal|"will be cached.\n"
operator|+
literal|"For example, in a filter condition like '.. where key + 10 = 100 or key + 10 = 0'\n"
operator|+
literal|"the expression 'key + 10' will be evaluated/cached once and reused for the following\n"
operator|+
literal|"expression ('key + 10 = 0'). Currently, this is applied only to expressions in select\n"
operator|+
literal|"or filter operators."
argument_list|)
block|,
comment|// Hive Variables
name|HIVEVARIABLESUBSTITUTE
argument_list|(
literal|"hive.variable.substitute"
argument_list|,
literal|true
argument_list|,
literal|"This enables substitution using syntax like ${var} ${system:var} and ${env:var}."
argument_list|)
block|,
name|HIVEVARIABLESUBSTITUTEDEPTH
argument_list|(
literal|"hive.variable.substitute.depth"
argument_list|,
literal|40
argument_list|,
literal|"The maximum replacements the substitution engine will do."
argument_list|)
block|,
name|HIVECONFVALIDATION
argument_list|(
literal|"hive.conf.validation"
argument_list|,
literal|true
argument_list|,
literal|"Enables type checking for registered Hive configurations"
argument_list|)
block|,
name|SEMANTIC_ANALYZER_HOOK
argument_list|(
literal|"hive.semantic.analyzer.hook"
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|)
block|,
name|HIVE_TEST_AUTHORIZATION_SQLSTD_HS2_MODE
argument_list|(
literal|"hive.test.authz.sstd.hs2.mode"
argument_list|,
literal|false
argument_list|,
literal|"test hs2 mode from .q tests"
argument_list|,
literal|true
argument_list|)
block|,
name|HIVE_AUTHORIZATION_ENABLED
argument_list|(
literal|"hive.security.authorization.enabled"
argument_list|,
literal|false
argument_list|,
literal|"enable or disable the Hive client authorization"
argument_list|)
block|,
name|HIVE_AUTHORIZATION_KERBEROS_USE_SHORTNAME
argument_list|(
literal|"hive.security.authorization.kerberos.use.shortname"
argument_list|,
literal|true
argument_list|,
literal|"use short name in Kerberos cluster"
argument_list|)
block|,
name|HIVE_AUTHORIZATION_MANAGER
argument_list|(
literal|"hive.security.authorization.manager"
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|,
literal|"The Hive client authorization manager class name. The user defined authorization class should implement \n"
operator|+
literal|"interface org.apache.hadoop.hive.ql.security.authorization.HiveAuthorizationProvider."
argument_list|)
block|,
name|HIVE_AUTHENTICATOR_MANAGER
argument_list|(
literal|"hive.security.authenticator.manager"
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.HadoopDefaultAuthenticator"
argument_list|,
literal|"hive client authenticator manager class name. The user defined authenticator should implement \n"
operator|+
literal|"interface org.apache.hadoop.hive.ql.security.HiveAuthenticationProvider."
argument_list|)
block|,
name|HIVE_METASTORE_AUTHORIZATION_MANAGER
argument_list|(
literal|"hive.security.metastore.authorization.manager"
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.DefaultHiveMetastoreAuthorizationProvider"
argument_list|,
literal|"Names of authorization manager classes (comma separated) to be used in the metastore\n"
operator|+
literal|"for authorization. The user defined authorization class should implement interface\n"
operator|+
literal|"org.apache.hadoop.hive.ql.security.authorization.HiveMetastoreAuthorizationProvider.\n"
operator|+
literal|"All authorization manager classes have to successfully authorize the metastore API\n"
operator|+
literal|"call for the command execution to be allowed."
argument_list|)
block|,
name|HIVE_METASTORE_AUTHORIZATION_AUTH_READS
argument_list|(
literal|"hive.security.metastore.authorization.auth.reads"
argument_list|,
literal|true
argument_list|,
literal|"If this is true, metastore authorizer authorizes read actions on database, table"
argument_list|)
block|,
name|HIVE_METASTORE_AUTHENTICATOR_MANAGER
argument_list|(
literal|"hive.security.metastore.authenticator.manager"
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.HadoopDefaultMetastoreAuthenticator"
argument_list|,
literal|"authenticator manager class name to be used in the metastore for authentication. \n"
operator|+
literal|"The user defined authenticator should implement interface org.apache.hadoop.hive.ql.security.HiveAuthenticationProvider."
argument_list|)
block|,
name|HIVE_AUTHORIZATION_TABLE_USER_GRANTS
argument_list|(
literal|"hive.security.authorization.createtable.user.grants"
argument_list|,
literal|""
argument_list|,
literal|"the privileges automatically granted to some users whenever a table gets created.\n"
operator|+
literal|"An example like \"userX,userY:select;userZ:create\" will grant select privilege to userX and userY,\n"
operator|+
literal|"and grant create privilege to userZ whenever a new table created."
argument_list|)
block|,
name|HIVE_AUTHORIZATION_TABLE_GROUP_GRANTS
argument_list|(
literal|"hive.security.authorization.createtable.group.grants"
argument_list|,
literal|""
argument_list|,
literal|"the privileges automatically granted to some groups whenever a table gets created.\n"
operator|+
literal|"An example like \"groupX,groupY:select;groupZ:create\" will grant select privilege to groupX and groupY,\n"
operator|+
literal|"and grant create privilege to groupZ whenever a new table created."
argument_list|)
block|,
name|HIVE_AUTHORIZATION_TABLE_ROLE_GRANTS
argument_list|(
literal|"hive.security.authorization.createtable.role.grants"
argument_list|,
literal|""
argument_list|,
literal|"the privileges automatically granted to some roles whenever a table gets created.\n"
operator|+
literal|"An example like \"roleX,roleY:select;roleZ:create\" will grant select privilege to roleX and roleY,\n"
operator|+
literal|"and grant create privilege to roleZ whenever a new table created."
argument_list|)
block|,
name|HIVE_AUTHORIZATION_TABLE_OWNER_GRANTS
argument_list|(
literal|"hive.security.authorization.createtable.owner.grants"
argument_list|,
literal|""
argument_list|,
literal|"The privileges automatically granted to the owner whenever a table gets created.\n"
operator|+
literal|"An example like \"select,drop\" will grant select and drop privilege to the owner\n"
operator|+
literal|"of the table. Note that the default gives the creator of a table no access to the\n"
operator|+
literal|"table (but see HIVE-8067)."
argument_list|)
block|,
name|HIVE_AUTHORIZATION_TASK_FACTORY
argument_list|(
literal|"hive.security.authorization.task.factory"
argument_list|,
literal|"org.apache.hadoop.hive.ql.parse.authorization.HiveAuthorizationTaskFactoryImpl"
argument_list|,
literal|"Authorization DDL task factory implementation"
argument_list|)
block|,
comment|// if this is not set default value is set during config initialization
comment|// Default value can't be set in this constructor as it would refer names in other ConfVars
comment|// whose constructor would not have been called
name|HIVE_AUTHORIZATION_SQL_STD_AUTH_CONFIG_WHITELIST
argument_list|(
literal|"hive.security.authorization.sqlstd.confwhitelist"
argument_list|,
literal|""
argument_list|,
literal|"A Java regex. Configurations parameters that match this\n"
operator|+
literal|"regex can be modified by user when SQL standard authorization is enabled.\n"
operator|+
literal|"To get the default value, use the 'set<param>' command.\n"
operator|+
literal|"Note that the hive.conf.restricted.list checks are still enforced after the white list\n"
operator|+
literal|"check"
argument_list|)
block|,
name|HIVE_AUTHORIZATION_SQL_STD_AUTH_CONFIG_WHITELIST_APPEND
argument_list|(
literal|"hive.security.authorization.sqlstd.confwhitelist.append"
argument_list|,
literal|""
argument_list|,
literal|"2nd Java regex that it would match in addition to\n"
operator|+
literal|"hive.security.authorization.sqlstd.confwhitelist.\n"
operator|+
literal|"Do not include a starting \"|\" in the value. Using this regex instead\n"
operator|+
literal|"of updating the original regex means that you can append to the default\n"
operator|+
literal|"set by SQL standard authorization instead of replacing it entirely."
argument_list|)
block|,
name|HIVE_CLI_PRINT_HEADER
argument_list|(
literal|"hive.cli.print.header"
argument_list|,
literal|false
argument_list|,
literal|"Whether to print the names of the columns in query output."
argument_list|)
block|,
name|HIVE_CLI_PRINT_ESCAPE_CRLF
argument_list|(
literal|"hive.cli.print.escape.crlf"
argument_list|,
literal|false
argument_list|,
literal|"Whether to print carriage returns and line feeds in row output as escaped \\r and \\n"
argument_list|)
block|,
name|HIVE_CLI_TEZ_SESSION_ASYNC
argument_list|(
literal|"hive.cli.tez.session.async"
argument_list|,
literal|true
argument_list|,
literal|"Whether to start Tez\n"
operator|+
literal|"session in background when running CLI with Tez, allowing CLI to be available earlier."
argument_list|)
block|,
name|HIVE_DISABLE_UNSAFE_EXTERNALTABLE_OPERATIONS
argument_list|(
literal|"hive.disable.unsafe.external.table.operations"
argument_list|,
literal|true
argument_list|,
literal|"Whether to disable certain optimizations and operations on external tables,"
operator|+
literal|" on the assumption that data changes by external applications may have negative effects"
operator|+
literal|" on these operations."
argument_list|)
block|,
name|HIVE_STRICT_MANAGED_TABLES
argument_list|(
literal|"hive.strict.managed.tables"
argument_list|,
literal|false
argument_list|,
literal|"Whether strict managed tables mode is enabled. With this mode enabled, "
operator|+
literal|"only transactional tables (both full and insert-only) are allowed to be created as managed tables"
argument_list|)
block|,
name|HIVE_EXTERNALTABLE_PURGE_DEFAULT
argument_list|(
literal|"hive.external.table.purge.default"
argument_list|,
literal|false
argument_list|,
literal|"Set to true to set external.table.purge=true on newly created external tables,"
operator|+
literal|" which will specify that the table data should be deleted when the table is dropped."
operator|+
literal|" Set to false maintain existing behavior that external tables do not delete data"
operator|+
literal|" when the table is dropped."
argument_list|)
block|,
name|HIVE_ERROR_ON_EMPTY_PARTITION
argument_list|(
literal|"hive.error.on.empty.partition"
argument_list|,
literal|false
argument_list|,
literal|"Whether to throw an exception if dynamic partition insert generates empty results."
argument_list|)
block|,
name|HIVE_EXIM_URI_SCHEME_WL
argument_list|(
literal|"hive.exim.uri.scheme.whitelist"
argument_list|,
literal|"hdfs,pfile,file,s3,s3a,gs"
argument_list|,
literal|"A comma separated list of acceptable URI schemes for import and export."
argument_list|)
block|,
comment|// temporary variable for testing. This is added just to turn off this feature in case of a bug in
comment|// deployment. It has not been documented in hive-default.xml intentionally, this should be removed
comment|// once the feature is stable
name|HIVE_EXIM_RESTRICT_IMPORTS_INTO_REPLICATED_TABLES
argument_list|(
literal|"hive.exim.strict.repl.tables"
argument_list|,
literal|true
argument_list|,
literal|"Parameter that determines if 'regular' (non-replication) export dumps can be\n"
operator|+
literal|"imported on to tables that are the target of replication. If this parameter is\n"
operator|+
literal|"set, regular imports will check if the destination table(if it exists) has a "
operator|+
literal|"'repl.last.id' set on it. If so, it will fail."
argument_list|)
block|,
name|HIVE_REPL_TASK_FACTORY
argument_list|(
literal|"hive.repl.task.factory"
argument_list|,
literal|"org.apache.hive.hcatalog.api.repl.exim.EximReplicationTaskFactory"
argument_list|,
literal|"Parameter that can be used to override which ReplicationTaskFactory will be\n"
operator|+
literal|"used to instantiate ReplicationTask events. Override for third party repl plugins"
argument_list|)
block|,
name|HIVE_MAPPER_CANNOT_SPAN_MULTIPLE_PARTITIONS
argument_list|(
literal|"hive.mapper.cannot.span.multiple.partitions"
argument_list|,
literal|false
argument_list|,
literal|""
argument_list|)
block|,
name|HIVE_REWORK_MAPREDWORK
argument_list|(
literal|"hive.rework.mapredwork"
argument_list|,
literal|false
argument_list|,
literal|"should rework the mapred work or not.\n"
operator|+
literal|"This is first introduced by SymlinkTextInputFormat to replace symlink files with real paths at compile time."
argument_list|)
block|,
name|HIVE_IO_EXCEPTION_HANDLERS
argument_list|(
literal|"hive.io.exception.handlers"
argument_list|,
literal|""
argument_list|,
literal|"A list of io exception handler class names. This is used\n"
operator|+
literal|"to construct a list exception handlers to handle exceptions thrown\n"
operator|+
literal|"by record readers"
argument_list|)
block|,
comment|// logging configuration
name|HIVE_LOG4J_FILE
argument_list|(
literal|"hive.log4j.file"
argument_list|,
literal|""
argument_list|,
literal|"Hive log4j configuration file.\n"
operator|+
literal|"If the property is not set, then logging will be initialized using hive-log4j2.properties found on the classpath.\n"
operator|+
literal|"If the property is set, the value must be a valid URI (java.net.URI, e.g. \"file:///tmp/my-logging.xml\"), \n"
operator|+
literal|"which you can then extract a URL from and pass to PropertyConfigurator.configure(URL)."
argument_list|)
block|,
name|HIVE_EXEC_LOG4J_FILE
argument_list|(
literal|"hive.exec.log4j.file"
argument_list|,
literal|""
argument_list|,
literal|"Hive log4j configuration file for execution mode(sub command).\n"
operator|+
literal|"If the property is not set, then logging will be initialized using hive-exec-log4j2.properties found on the classpath.\n"
operator|+
literal|"If the property is set, the value must be a valid URI (java.net.URI, e.g. \"file:///tmp/my-logging.xml\"), \n"
operator|+
literal|"which you can then extract a URL from and pass to PropertyConfigurator.configure(URL)."
argument_list|)
block|,
name|HIVE_ASYNC_LOG_ENABLED
argument_list|(
literal|"hive.async.log.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable Log4j2's asynchronous logging. Asynchronous logging can give\n"
operator|+
literal|" significant performance improvement as logging will be handled in separate thread\n"
operator|+
literal|" that uses LMAX disruptor queue for buffering log messages.\n"
operator|+
literal|" Refer https://logging.apache.org/log4j/2.x/manual/async.html for benefits and\n"
operator|+
literal|" drawbacks."
argument_list|)
block|,
name|HIVE_LOG_EXPLAIN_OUTPUT
argument_list|(
literal|"hive.log.explain.output"
argument_list|,
literal|false
argument_list|,
literal|"Whether to log explain output for every query.\n"
operator|+
literal|"When enabled, will log EXPLAIN EXTENDED output for the query at INFO log4j log level."
argument_list|)
block|,
name|HIVE_EXPLAIN_USER
argument_list|(
literal|"hive.explain.user"
argument_list|,
literal|true
argument_list|,
literal|"Whether to show explain result at user level.\n"
operator|+
literal|"When enabled, will log EXPLAIN output for the query at user level. Tez only."
argument_list|)
block|,
name|HIVE_SPARK_EXPLAIN_USER
argument_list|(
literal|"hive.spark.explain.user"
argument_list|,
literal|false
argument_list|,
literal|"Whether to show explain result at user level.\n"
operator|+
literal|"When enabled, will log EXPLAIN output for the query at user level. Spark only."
argument_list|)
block|,
name|HIVE_SPARK_LOG_EXPLAIN_WEBUI
argument_list|(
literal|"hive.spark.log.explain.webui"
argument_list|,
literal|true
argument_list|,
literal|"Whether to show the "
operator|+
literal|"explain plan in the Spark Web UI. Only shows the regular EXPLAIN plan, and ignores "
operator|+
literal|"any extra EXPLAIN configuration (e.g. hive.spark.explain.user, etc.). The explain "
operator|+
literal|"plan for each stage is truncated at 100,000 characters."
argument_list|)
block|,
comment|// prefix used to auto generated column aliases (this should be s,tarted with '_')
name|HIVE_AUTOGEN_COLUMNALIAS_PREFIX_LABEL
argument_list|(
literal|"hive.autogen.columnalias.prefix.label"
argument_list|,
literal|"_c"
argument_list|,
literal|"String used as a prefix when auto generating column alias.\n"
operator|+
literal|"By default the prefix label will be appended with a column position number to form the column alias. \n"
operator|+
literal|"Auto generation would happen if an aggregate function is used in a select clause without an explicit alias."
argument_list|)
block|,
name|HIVE_AUTOGEN_COLUMNALIAS_PREFIX_INCLUDEFUNCNAME
argument_list|(
literal|"hive.autogen.columnalias.prefix.includefuncname"
argument_list|,
literal|false
argument_list|,
literal|"Whether to include function name in the column alias auto generated by Hive."
argument_list|)
block|,
name|HIVE_METRICS_CLASS
argument_list|(
literal|"hive.service.metrics.class"
argument_list|,
literal|"org.apache.hadoop.hive.common.metrics.metrics2.CodahaleMetrics"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"org.apache.hadoop.hive.common.metrics.metrics2.CodahaleMetrics"
argument_list|,
literal|"org.apache.hadoop.hive.common.metrics.LegacyMetrics"
argument_list|)
argument_list|,
literal|"Hive metrics subsystem implementation class."
argument_list|)
block|,
name|HIVE_CODAHALE_METRICS_REPORTER_CLASSES
argument_list|(
literal|"hive.service.metrics.codahale.reporter.classes"
argument_list|,
literal|"org.apache.hadoop.hive.common.metrics.metrics2.JsonFileMetricsReporter, "
operator|+
literal|"org.apache.hadoop.hive.common.metrics.metrics2.JmxMetricsReporter"
argument_list|,
literal|"Comma separated list of reporter implementation classes for metric class "
operator|+
literal|"org.apache.hadoop.hive.common.metrics.metrics2.CodahaleMetrics. Overrides "
operator|+
literal|"HIVE_METRICS_REPORTER conf if present"
argument_list|)
block|,
annotation|@
name|Deprecated
name|HIVE_METRICS_REPORTER
argument_list|(
literal|"hive.service.metrics.reporter"
argument_list|,
literal|""
argument_list|,
literal|"Reporter implementations for metric class "
operator|+
literal|"org.apache.hadoop.hive.common.metrics.metrics2.CodahaleMetrics;"
operator|+
literal|"Deprecated, use HIVE_CODAHALE_METRICS_REPORTER_CLASSES instead. This configuraiton will be"
operator|+
literal|" overridden by HIVE_CODAHALE_METRICS_REPORTER_CLASSES if present. "
operator|+
literal|"Comma separated list of JMX, CONSOLE, JSON_FILE, HADOOP2"
argument_list|)
block|,
name|HIVE_METRICS_JSON_FILE_LOCATION
argument_list|(
literal|"hive.service.metrics.file.location"
argument_list|,
literal|"/tmp/report.json"
argument_list|,
literal|"For metric class org.apache.hadoop.hive.common.metrics.metrics2.CodahaleMetrics JSON_FILE reporter, the location of local JSON metrics file.  "
operator|+
literal|"This file will get overwritten at every interval."
argument_list|)
block|,
name|HIVE_METRICS_JSON_FILE_INTERVAL
argument_list|(
literal|"hive.service.metrics.file.frequency"
argument_list|,
literal|"5000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"For metric class org.apache.hadoop.hive.common.metrics.metrics2.JsonFileMetricsReporter, "
operator|+
literal|"the frequency of updating JSON metrics file."
argument_list|)
block|,
name|HIVE_METRICS_HADOOP2_INTERVAL
argument_list|(
literal|"hive.service.metrics.hadoop2.frequency"
argument_list|,
literal|"30s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"For metric class org.apache.hadoop.hive.common.metrics.metrics2.Metrics2Reporter, "
operator|+
literal|"the frequency of updating the HADOOP2 metrics system."
argument_list|)
block|,
name|HIVE_METRICS_HADOOP2_COMPONENT_NAME
argument_list|(
literal|"hive.service.metrics.hadoop2.component"
argument_list|,
literal|"hive"
argument_list|,
literal|"Component name to provide to Hadoop2 Metrics system. Ideally 'hivemetastore' for the MetaStore "
operator|+
literal|" and and 'hiveserver2' for HiveServer2."
argument_list|)
block|,
name|HIVE_PERF_LOGGER
argument_list|(
literal|"hive.exec.perf.logger"
argument_list|,
literal|"org.apache.hadoop.hive.ql.log.PerfLogger"
argument_list|,
literal|"The class responsible for logging client side performance metrics. \n"
operator|+
literal|"Must be a subclass of org.apache.hadoop.hive.ql.log.PerfLogger"
argument_list|)
block|,
name|HIVE_START_CLEANUP_SCRATCHDIR
argument_list|(
literal|"hive.start.cleanup.scratchdir"
argument_list|,
literal|false
argument_list|,
literal|"To cleanup the Hive scratchdir when starting the Hive Server"
argument_list|)
block|,
name|HIVE_SCRATCH_DIR_LOCK
argument_list|(
literal|"hive.scratchdir.lock"
argument_list|,
literal|false
argument_list|,
literal|"To hold a lock file in scratchdir to prevent to be removed by cleardanglingscratchdir"
argument_list|)
block|,
name|HIVE_INSERT_INTO_MULTILEVEL_DIRS
argument_list|(
literal|"hive.insert.into.multilevel.dirs"
argument_list|,
literal|false
argument_list|,
literal|"Where to insert into multilevel directories like\n"
operator|+
literal|"\"insert directory '/HIVEFT25686/chinna/' from table\""
argument_list|)
block|,
name|HIVE_CTAS_EXTERNAL_TABLES
argument_list|(
literal|"hive.ctas.external.tables"
argument_list|,
literal|true
argument_list|,
literal|"whether CTAS for external tables is allowed"
argument_list|)
block|,
name|HIVE_INSERT_INTO_EXTERNAL_TABLES
argument_list|(
literal|"hive.insert.into.external.tables"
argument_list|,
literal|true
argument_list|,
literal|"whether insert into external tables is allowed"
argument_list|)
block|,
name|HIVE_TEMPORARY_TABLE_STORAGE
argument_list|(
literal|"hive.exec.temporary.table.storage"
argument_list|,
literal|"default"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"memory"
argument_list|,
literal|"ssd"
argument_list|,
literal|"default"
argument_list|)
argument_list|,
literal|"Define the storage policy for temporary tables."
operator|+
literal|"Choices between memory, ssd and default"
argument_list|)
block|,
name|HIVE_QUERY_LIFETIME_HOOKS
argument_list|(
literal|"hive.query.lifetime.hooks"
argument_list|,
literal|""
argument_list|,
literal|"A comma separated list of hooks which implement QueryLifeTimeHook. These will be triggered"
operator|+
literal|" before/after query compilation and before/after query execution, in the order specified."
operator|+
literal|"Implementations of QueryLifeTimeHookWithParseHooks can also be specified in this list. If they are"
operator|+
literal|"specified then they will be invoked in the same places as QueryLifeTimeHooks and will be invoked during pre "
operator|+
literal|"and post query parsing"
argument_list|)
block|,
name|HIVE_DRIVER_RUN_HOOKS
argument_list|(
literal|"hive.exec.driver.run.hooks"
argument_list|,
literal|""
argument_list|,
literal|"A comma separated list of hooks which implement HiveDriverRunHook. Will be run at the beginning "
operator|+
literal|"and end of Driver.run, these will be run in the order specified."
argument_list|)
block|,
name|HIVE_DDL_OUTPUT_FORMAT
argument_list|(
literal|"hive.ddl.output.format"
argument_list|,
literal|null
argument_list|,
literal|"The data format to use for DDL output.  One of \"text\" (for human\n"
operator|+
literal|"readable text) or \"json\" (for a json object)."
argument_list|)
block|,
name|HIVE_ENTITY_SEPARATOR
argument_list|(
literal|"hive.entity.separator"
argument_list|,
literal|"@"
argument_list|,
literal|"Separator used to construct names of tables and partitions. For example, dbname@tablename@partitionname"
argument_list|)
block|,
name|HIVE_CAPTURE_TRANSFORM_ENTITY
argument_list|(
literal|"hive.entity.capture.transform"
argument_list|,
literal|false
argument_list|,
literal|"Compiler to capture transform URI referred in the query"
argument_list|)
block|,
name|HIVE_DISPLAY_PARTITION_COLUMNS_SEPARATELY
argument_list|(
literal|"hive.display.partition.cols.separately"
argument_list|,
literal|true
argument_list|,
literal|"In older Hive version (0.10 and earlier) no distinction was made between\n"
operator|+
literal|"partition columns or non-partition columns while displaying columns in describe\n"
operator|+
literal|"table. From 0.12 onwards, they are displayed separately. This flag will let you\n"
operator|+
literal|"get old behavior, if desired. See, test-case in patch for HIVE-6689."
argument_list|)
block|,
name|HIVE_SSL_PROTOCOL_BLACKLIST
argument_list|(
literal|"hive.ssl.protocol.blacklist"
argument_list|,
literal|"SSLv2,SSLv3"
argument_list|,
literal|"SSL Versions to disable for all Hive Servers"
argument_list|)
block|,
name|HIVE_PRIVILEGE_SYNCHRONIZER
argument_list|(
literal|"hive.privilege.synchronizer"
argument_list|,
literal|true
argument_list|,
literal|"Whether to synchronize privileges from external authorizer periodically in HS2"
argument_list|)
block|,
name|HIVE_PRIVILEGE_SYNCHRONIZER_INTERVAL
argument_list|(
literal|"hive.privilege.synchronizer.interval"
argument_list|,
literal|"1800s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Interval to synchronize privileges from external authorizer periodically in HS2"
argument_list|)
block|,
comment|// HiveServer2 specific configs
name|HIVE_SERVER2_CLEAR_DANGLING_SCRATCH_DIR
argument_list|(
literal|"hive.server2.clear.dangling.scratchdir"
argument_list|,
literal|false
argument_list|,
literal|"Clear dangling scratch dir periodically in HS2"
argument_list|)
block|,
name|HIVE_SERVER2_CLEAR_DANGLING_SCRATCH_DIR_INTERVAL
argument_list|(
literal|"hive.server2.clear.dangling.scratchdir.interval"
argument_list|,
literal|"1800s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Interval to clear dangling scratch dir periodically in HS2"
argument_list|)
block|,
name|HIVE_SERVER2_SLEEP_INTERVAL_BETWEEN_START_ATTEMPTS
argument_list|(
literal|"hive.server2.sleep.interval.between.start.attempts"
argument_list|,
literal|"60s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
literal|0l
argument_list|,
literal|true
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
literal|true
argument_list|)
argument_list|,
literal|"Amount of time to sleep between HiveServer2 start attempts. Primarily meant for tests"
argument_list|)
block|,
name|HIVE_SERVER2_MAX_START_ATTEMPTS
argument_list|(
literal|"hive.server2.max.start.attempts"
argument_list|,
literal|30L
argument_list|,
operator|new
name|RangeValidator
argument_list|(
literal|0L
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|"Number of times HiveServer2 will attempt to start before exiting. The sleep interval between retries"
operator|+
literal|" is determined by "
operator|+
name|ConfVars
operator|.
name|HIVE_SERVER2_SLEEP_INTERVAL_BETWEEN_START_ATTEMPTS
operator|.
name|varname
operator|+
literal|"\n The default of 30 will keep trying for 30 minutes."
argument_list|)
block|,
name|HIVE_SERVER2_SUPPORT_DYNAMIC_SERVICE_DISCOVERY
argument_list|(
literal|"hive.server2.support.dynamic.service.discovery"
argument_list|,
literal|false
argument_list|,
literal|"Whether HiveServer2 supports dynamic service discovery for its clients. "
operator|+
literal|"To support this, each instance of HiveServer2 currently uses ZooKeeper to register itself, "
operator|+
literal|"when it is brought up. JDBC/ODBC clients should use the ZooKeeper ensemble: "
operator|+
literal|"hive.zookeeper.quorum in their connection string."
argument_list|)
block|,
name|HIVE_SERVER2_ZOOKEEPER_NAMESPACE
argument_list|(
literal|"hive.server2.zookeeper.namespace"
argument_list|,
literal|"hiveserver2"
argument_list|,
literal|"The parent node in ZooKeeper used by HiveServer2 when supporting dynamic service discovery."
argument_list|)
block|,
name|HIVE_SERVER2_ZOOKEEPER_PUBLISH_CONFIGS
argument_list|(
literal|"hive.server2.zookeeper.publish.configs"
argument_list|,
literal|true
argument_list|,
literal|"Whether we should publish HiveServer2's configs to ZooKeeper."
argument_list|)
block|,
comment|// HiveServer2 global init file location
name|HIVE_SERVER2_GLOBAL_INIT_FILE_LOCATION
argument_list|(
literal|"hive.server2.global.init.file.location"
argument_list|,
literal|"${env:HIVE_CONF_DIR}"
argument_list|,
literal|"Either the location of a HS2 global init file or a directory containing a .hiverc file. If the \n"
operator|+
literal|"property is set, the value must be a valid path to an init file or directory where the init file is located."
argument_list|)
block|,
name|HIVE_SERVER2_TRANSPORT_MODE
argument_list|(
literal|"hive.server2.transport.mode"
argument_list|,
literal|"binary"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"binary"
argument_list|,
literal|"http"
argument_list|)
argument_list|,
literal|"Transport mode of HiveServer2."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_BIND_HOST
argument_list|(
literal|"hive.server2.thrift.bind.host"
argument_list|,
literal|""
argument_list|,
literal|"Bind host on which to run the HiveServer2 Thrift service."
argument_list|)
block|,
name|HIVE_SERVER2_PARALLEL_COMPILATION
argument_list|(
literal|"hive.driver.parallel.compilation"
argument_list|,
literal|false
argument_list|,
literal|"Whether to\n"
operator|+
literal|"enable parallel compilation of the queries between sessions and within the same session on HiveServer2. The default is false."
argument_list|)
block|,
name|HIVE_SERVER2_PARALLEL_COMPILATION_LIMIT
argument_list|(
literal|"hive.driver.parallel.compilation.global.limit"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"Determines the "
operator|+
literal|"degree of parallelism for queries compilation between sessions on HiveServer2. The default is -1."
argument_list|)
block|,
name|HIVE_SERVER2_COMPILE_LOCK_TIMEOUT
argument_list|(
literal|"hive.server2.compile.lock.timeout"
argument_list|,
literal|"0s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Number of seconds a request will wait to acquire the compile lock before giving up. "
operator|+
literal|"Setting it to 0s disables the timeout."
argument_list|)
block|,
name|HIVE_SERVER2_PARALLEL_OPS_IN_SESSION
argument_list|(
literal|"hive.server2.parallel.ops.in.session"
argument_list|,
literal|true
argument_list|,
literal|"Whether to allow several parallel operations (such as SQL statements) in one session."
argument_list|)
block|,
name|HIVE_SERVER2_MATERIALIZED_VIEWS_REGISTRY_IMPL
argument_list|(
literal|"hive.server2.materializedviews.registry.impl"
argument_list|,
literal|"DEFAULT"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"DEFAULT"
argument_list|,
literal|"DUMMY"
argument_list|)
argument_list|,
literal|"The implementation that we should use for the materialized views registry. \n"
operator|+
literal|"  DEFAULT: Default cache for materialized views\n"
operator|+
literal|"  DUMMY: Do not cache materialized views and hence forward requests to metastore"
argument_list|)
block|,
name|HIVE_SERVER2_MATERIALIZED_VIEWS_REGISTRY_REFRESH
argument_list|(
literal|"hive.server2.materializedviews.registry.refresh.period"
argument_list|,
literal|"1500s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Period, specified in seconds, between successive refreshes of the registry to pull new materializations "
operator|+
literal|"from the metastore that may have been created by other HS2 instances."
argument_list|)
block|,
comment|// HiveServer2 WebUI
name|HIVE_SERVER2_WEBUI_BIND_HOST
argument_list|(
literal|"hive.server2.webui.host"
argument_list|,
literal|"0.0.0.0"
argument_list|,
literal|"The host address the HiveServer2 WebUI will listen on"
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_PORT
argument_list|(
literal|"hive.server2.webui.port"
argument_list|,
literal|10002
argument_list|,
literal|"The port the HiveServer2 WebUI will listen on. This can be"
operator|+
literal|"set to 0 or a negative integer to disable the web UI"
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_MAX_THREADS
argument_list|(
literal|"hive.server2.webui.max.threads"
argument_list|,
literal|50
argument_list|,
literal|"The max HiveServer2 WebUI threads"
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_USE_SSL
argument_list|(
literal|"hive.server2.webui.use.ssl"
argument_list|,
literal|false
argument_list|,
literal|"Set this to true for using SSL encryption for HiveServer2 WebUI."
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_SSL_KEYSTORE_PATH
argument_list|(
literal|"hive.server2.webui.keystore.path"
argument_list|,
literal|""
argument_list|,
literal|"SSL certificate keystore location for HiveServer2 WebUI."
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_SSL_KEYSTORE_PASSWORD
argument_list|(
literal|"hive.server2.webui.keystore.password"
argument_list|,
literal|""
argument_list|,
literal|"SSL certificate keystore password for HiveServer2 WebUI."
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_USE_SPNEGO
argument_list|(
literal|"hive.server2.webui.use.spnego"
argument_list|,
literal|false
argument_list|,
literal|"If true, the HiveServer2 WebUI will be secured with SPNEGO. Clients must authenticate with Kerberos."
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_SPNEGO_KEYTAB
argument_list|(
literal|"hive.server2.webui.spnego.keytab"
argument_list|,
literal|""
argument_list|,
literal|"The path to the Kerberos Keytab file containing the HiveServer2 WebUI SPNEGO service principal."
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_SPNEGO_PRINCIPAL
argument_list|(
literal|"hive.server2.webui.spnego.principal"
argument_list|,
literal|"HTTP/_HOST@EXAMPLE.COM"
argument_list|,
literal|"The HiveServer2 WebUI SPNEGO service principal.\n"
operator|+
literal|"The special string _HOST will be replaced automatically with \n"
operator|+
literal|"the value of hive.server2.webui.host or the correct host name."
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_MAX_HISTORIC_QUERIES
argument_list|(
literal|"hive.server2.webui.max.historic.queries"
argument_list|,
literal|25
argument_list|,
literal|"The maximum number of past queries to show in HiverSever2 WebUI."
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_USE_PAM
argument_list|(
literal|"hive.server2.webui.use.pam"
argument_list|,
literal|false
argument_list|,
literal|"If true, the HiveServer2 WebUI will be secured with PAM."
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_EXPLAIN_OUTPUT
argument_list|(
literal|"hive.server2.webui.explain.output"
argument_list|,
literal|false
argument_list|,
literal|"When set to true, the EXPLAIN output for every query is displayed"
operator|+
literal|" in the HS2 WebUI / Drilldown / Query Plan tab.\n"
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_SHOW_GRAPH
argument_list|(
literal|"hive.server2.webui.show.graph"
argument_list|,
literal|false
argument_list|,
literal|"Set this to true to to display query plan as a graph instead of text in the WebUI. "
operator|+
literal|"Only works with hive.server2.webui.explain.output set to true."
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_MAX_GRAPH_SIZE
argument_list|(
literal|"hive.server2.webui.max.graph.size"
argument_list|,
literal|25
argument_list|,
literal|"Max number of stages graph can display. If number of stages exceeds this, no query"
operator|+
literal|"plan will be shown. Only works when hive.server2.webui.show.graph and "
operator|+
literal|"hive.server2.webui.explain.output set to true."
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_SHOW_STATS
argument_list|(
literal|"hive.server2.webui.show.stats"
argument_list|,
literal|false
argument_list|,
literal|"Set this to true to to display statistics for MapReduce tasks in the WebUI. "
operator|+
literal|"Only works when hive.server2.webui.show.graph and hive.server2.webui.explain.output "
operator|+
literal|"set to true."
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_ENABLE_CORS
argument_list|(
literal|"hive.server2.webui.enable.cors"
argument_list|,
literal|false
argument_list|,
literal|"Whether to enable cross origin requests (CORS)\n"
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_CORS_ALLOWED_ORIGINS
argument_list|(
literal|"hive.server2.webui.cors.allowed.origins"
argument_list|,
literal|"*"
argument_list|,
literal|"Comma separated list of origins that are allowed when CORS is enabled.\n"
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_CORS_ALLOWED_METHODS
argument_list|(
literal|"hive.server2.webui.cors.allowed.methods"
argument_list|,
literal|"GET,POST,DELETE,HEAD"
argument_list|,
literal|"Comma separated list of http methods that are allowed when CORS is enabled.\n"
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_CORS_ALLOWED_HEADERS
argument_list|(
literal|"hive.server2.webui.cors.allowed.headers"
argument_list|,
literal|"X-Requested-With,Content-Type,Accept,Origin"
argument_list|,
literal|"Comma separated list of http headers that are allowed when CORS is enabled.\n"
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_XFRAME_ENABLED
argument_list|(
literal|"hive.server2.webui.xframe.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable xframe\n"
argument_list|)
block|,
name|HIVE_SERVER2_WEBUI_XFRAME_VALUE
argument_list|(
literal|"hive.server2.webui.xframe.value"
argument_list|,
literal|"SAMEORIGIN"
argument_list|,
literal|"Configuration to allow the user to set the x_frame-options value\n"
argument_list|)
block|,
comment|// Tez session settings
name|HIVE_SERVER2_ACTIVE_PASSIVE_HA_ENABLE
argument_list|(
literal|"hive.server2.active.passive.ha.enable"
argument_list|,
literal|false
argument_list|,
literal|"Whether HiveServer2 Active/Passive High Availability be enabled when Hive Interactive sessions are enabled."
operator|+
literal|"This will also require hive.server2.support.dynamic.service.discovery to be enabled."
argument_list|)
block|,
name|HIVE_SERVER2_ACTIVE_PASSIVE_HA_REGISTRY_NAMESPACE
argument_list|(
literal|"hive.server2.active.passive.ha.registry.namespace"
argument_list|,
literal|"hs2ActivePassiveHA"
argument_list|,
literal|"When HiveServer2 Active/Passive High Availability is enabled, uses this namespace for registering HS2\n"
operator|+
literal|"instances with zookeeper"
argument_list|)
block|,
name|HIVE_SERVER2_TEZ_INTERACTIVE_QUEUE
argument_list|(
literal|"hive.server2.tez.interactive.queue"
argument_list|,
literal|""
argument_list|,
literal|"A single YARN queues to use for Hive Interactive sessions. When this is specified,\n"
operator|+
literal|"workload management is enabled and used for these sessions."
argument_list|)
block|,
name|HIVE_SERVER2_WM_NAMESPACE
argument_list|(
literal|"hive.server2.wm.namespace"
argument_list|,
literal|"default"
argument_list|,
literal|"The WM namespace to use when one metastore is used by multiple compute clusters each \n"
operator|+
literal|"with their own workload management. The special value 'default' (the default) will \n"
operator|+
literal|"also include any resource plans created before the namespaces were introduced."
argument_list|)
block|,
name|HIVE_SERVER2_WM_WORKER_THREADS
argument_list|(
literal|"hive.server2.wm.worker.threads"
argument_list|,
literal|4
argument_list|,
literal|"Number of worker threads to use to perform the synchronous operations with Tez\n"
operator|+
literal|"sessions for workload management (e.g. opening, closing, etc.)"
argument_list|)
block|,
name|HIVE_SERVER2_WM_ALLOW_ANY_POOL_VIA_JDBC
argument_list|(
literal|"hive.server2.wm.allow.any.pool.via.jdbc"
argument_list|,
literal|false
argument_list|,
literal|"Applies when a user specifies a target WM pool in the JDBC connection string. If\n"
operator|+
literal|"false, the user can only specify a pool he is mapped to (e.g. make a choice among\n"
operator|+
literal|"multiple group mappings); if true, the user can specify any existing pool."
argument_list|)
block|,
name|HIVE_SERVER2_WM_POOL_METRICS
argument_list|(
literal|"hive.server2.wm.pool.metrics"
argument_list|,
literal|true
argument_list|,
literal|"Whether per-pool WM metrics should be enabled."
argument_list|)
block|,
name|HIVE_SERVER2_TEZ_WM_AM_REGISTRY_TIMEOUT
argument_list|(
literal|"hive.server2.tez.wm.am.registry.timeout"
argument_list|,
literal|"30s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"The timeout for AM registry registration, after which (on attempting to use the\n"
operator|+
literal|"session), we kill it and try to get another one."
argument_list|)
block|,
name|HIVE_SERVER2_TEZ_DEFAULT_QUEUES
argument_list|(
literal|"hive.server2.tez.default.queues"
argument_list|,
literal|""
argument_list|,
literal|"A list of comma separated values corresponding to YARN queues of the same name.\n"
operator|+
literal|"When HiveServer2 is launched in Tez mode, this configuration needs to be set\n"
operator|+
literal|"for multiple Tez sessions to run in parallel on the cluster."
argument_list|)
block|,
name|HIVE_SERVER2_TEZ_SESSIONS_PER_DEFAULT_QUEUE
argument_list|(
literal|"hive.server2.tez.sessions.per.default.queue"
argument_list|,
literal|1
argument_list|,
literal|"A positive integer that determines the number of Tez sessions that should be\n"
operator|+
literal|"launched on each of the queues specified by \"hive.server2.tez.default.queues\".\n"
operator|+
literal|"Determines the parallelism on each queue."
argument_list|)
block|,
name|HIVE_SERVER2_TEZ_INITIALIZE_DEFAULT_SESSIONS
argument_list|(
literal|"hive.server2.tez.initialize.default.sessions"
argument_list|,
literal|true
argument_list|,
literal|"This flag is used in HiveServer2 to enable a user to use HiveServer2 without\n"
operator|+
literal|"turning on Tez for HiveServer2. The user could potentially want to run queries\n"
operator|+
literal|"over Tez without the pool of sessions."
argument_list|)
block|,
name|HIVE_SERVER2_TEZ_QUEUE_ACCESS_CHECK
argument_list|(
literal|"hive.server2.tez.queue.access.check"
argument_list|,
literal|false
argument_list|,
literal|"Whether to check user access to explicitly specified YARN queues. "
operator|+
literal|"yarn.resourcemanager.webapp.address must be configured to use this."
argument_list|)
block|,
name|HIVE_SERVER2_TEZ_SESSION_LIFETIME
argument_list|(
literal|"hive.server2.tez.session.lifetime"
argument_list|,
literal|"162h"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|HOURS
argument_list|)
argument_list|,
literal|"The lifetime of the Tez sessions launched by HS2 when default sessions are enabled.\n"
operator|+
literal|"Set to 0 to disable session expiration."
argument_list|)
block|,
name|HIVE_SERVER2_TEZ_SESSION_LIFETIME_JITTER
argument_list|(
literal|"hive.server2.tez.session.lifetime.jitter"
argument_list|,
literal|"3h"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|HOURS
argument_list|)
argument_list|,
literal|"The jitter for Tez session lifetime; prevents all the sessions from restarting at once."
argument_list|)
block|,
name|HIVE_SERVER2_TEZ_SESSION_MAX_INIT_THREADS
argument_list|(
literal|"hive.server2.tez.sessions.init.threads"
argument_list|,
literal|16
argument_list|,
literal|"If hive.server2.tez.initialize.default.sessions is enabled, the maximum number of\n"
operator|+
literal|"threads to use to initialize the default sessions."
argument_list|)
block|,
name|HIVE_SERVER2_TEZ_SESSION_RESTRICTED_CONFIGS
argument_list|(
literal|"hive.server2.tez.sessions.restricted.configs"
argument_list|,
literal|""
argument_list|,
literal|"The configuration settings that cannot be set when submitting jobs to HiveServer2. If\n"
operator|+
literal|"any of these are set to values different from those in the server configuration, an\n"
operator|+
literal|"exception will be thrown."
argument_list|)
block|,
name|HIVE_SERVER2_TEZ_SESSION_CUSTOM_QUEUE_ALLOWED
argument_list|(
literal|"hive.server2.tez.sessions.custom.queue.allowed"
argument_list|,
literal|"true"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"true"
argument_list|,
literal|"false"
argument_list|,
literal|"ignore"
argument_list|)
argument_list|,
literal|"Whether Tez session pool should allow submitting queries to custom queues. The options\n"
operator|+
literal|"are true, false (error out), ignore (accept the query but ignore the queue setting)."
argument_list|)
block|,
comment|// Operation log configuration
name|HIVE_SERVER2_LOGGING_OPERATION_ENABLED
argument_list|(
literal|"hive.server2.logging.operation.enabled"
argument_list|,
literal|true
argument_list|,
literal|"When true, HS2 will save operation logs and make them available for clients"
argument_list|)
block|,
name|HIVE_SERVER2_LOGGING_OPERATION_LOG_LOCATION
argument_list|(
literal|"hive.server2.logging.operation.log.location"
argument_list|,
literal|"${system:java.io.tmpdir}"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"${system:user.name}"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"operation_logs"
argument_list|,
literal|"Top level directory where operation logs are stored if logging functionality is enabled"
argument_list|)
block|,
name|HIVE_SERVER2_LOGGING_OPERATION_LEVEL
argument_list|(
literal|"hive.server2.logging.operation.level"
argument_list|,
literal|"EXECUTION"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"NONE"
argument_list|,
literal|"EXECUTION"
argument_list|,
literal|"PERFORMANCE"
argument_list|,
literal|"VERBOSE"
argument_list|)
argument_list|,
literal|"HS2 operation logging mode available to clients to be set at session level.\n"
operator|+
literal|"For this to work, hive.server2.logging.operation.enabled should be set to true.\n"
operator|+
literal|"  NONE: Ignore any logging\n"
operator|+
literal|"  EXECUTION: Log completion of tasks\n"
operator|+
literal|"  PERFORMANCE: Execution + Performance logs \n"
operator|+
literal|"  VERBOSE: All logs"
argument_list|)
block|,
name|HIVE_SERVER2_OPERATION_LOG_CLEANUP_DELAY
argument_list|(
literal|"hive.server2.operation.log.cleanup.delay"
argument_list|,
literal|"300s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"When a query is cancelled (via kill query, query timeout or triggers),\n"
operator|+
literal|" operation logs gets cleaned up after this delay"
argument_list|)
block|,
comment|// HS2 connections guard rails
name|HIVE_SERVER2_LIMIT_CONNECTIONS_PER_USER
argument_list|(
literal|"hive.server2.limit.connections.per.user"
argument_list|,
literal|0
argument_list|,
literal|"Maximum hive server2 connections per user. Any user exceeding this limit will not be allowed to connect. "
operator|+
literal|"Default=0 does not enforce limits."
argument_list|)
block|,
name|HIVE_SERVER2_LIMIT_CONNECTIONS_PER_IPADDRESS
argument_list|(
literal|"hive.server2.limit.connections.per.ipaddress"
argument_list|,
literal|0
argument_list|,
literal|"Maximum hive server2 connections per ipaddress. Any ipaddress exceeding this limit will not be allowed "
operator|+
literal|"to connect. Default=0 does not enforce limits."
argument_list|)
block|,
name|HIVE_SERVER2_LIMIT_CONNECTIONS_PER_USER_IPADDRESS
argument_list|(
literal|"hive.server2.limit.connections.per.user.ipaddress"
argument_list|,
literal|0
argument_list|,
literal|"Maximum hive server2 connections per user:ipaddress combination. Any user-ipaddress exceeding this limit will "
operator|+
literal|"not be allowed to connect. Default=0 does not enforce limits."
argument_list|)
block|,
comment|// Enable metric collection for HiveServer2
name|HIVE_SERVER2_METRICS_ENABLED
argument_list|(
literal|"hive.server2.metrics.enabled"
argument_list|,
literal|false
argument_list|,
literal|"Enable metrics on the HiveServer2."
argument_list|)
block|,
comment|// http (over thrift) transport settings
name|HIVE_SERVER2_THRIFT_HTTP_PORT
argument_list|(
literal|"hive.server2.thrift.http.port"
argument_list|,
literal|10001
argument_list|,
literal|"Port number of HiveServer2 Thrift interface when hive.server2.transport.mode is 'http'."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_HTTP_PATH
argument_list|(
literal|"hive.server2.thrift.http.path"
argument_list|,
literal|"cliservice"
argument_list|,
literal|"Path component of URL endpoint when in HTTP mode."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_MAX_MESSAGE_SIZE
argument_list|(
literal|"hive.server2.thrift.max.message.size"
argument_list|,
literal|100
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
literal|"Maximum message size in bytes a HS2 server will accept."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_HTTP_MAX_IDLE_TIME
argument_list|(
literal|"hive.server2.thrift.http.max.idle.time"
argument_list|,
literal|"1800s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Maximum idle time for a connection on the server when in HTTP mode."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_HTTP_WORKER_KEEPALIVE_TIME
argument_list|(
literal|"hive.server2.thrift.http.worker.keepalive.time"
argument_list|,
literal|"60s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Keepalive time for an idle http worker thread. When the number of workers exceeds min workers, "
operator|+
literal|"excessive threads are killed after this time interval."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_HTTP_REQUEST_HEADER_SIZE
argument_list|(
literal|"hive.server2.thrift.http.request.header.size"
argument_list|,
literal|6
operator|*
literal|1024
argument_list|,
literal|"Request header size in bytes, when using HTTP transport mode. Jetty defaults used."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_HTTP_RESPONSE_HEADER_SIZE
argument_list|(
literal|"hive.server2.thrift.http.response.header.size"
argument_list|,
literal|6
operator|*
literal|1024
argument_list|,
literal|"Response header size in bytes, when using HTTP transport mode. Jetty defaults used."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_HTTP_COMPRESSION_ENABLED
argument_list|(
literal|"hive.server2.thrift.http.compression.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Enable thrift http compression via Jetty compression support"
argument_list|)
block|,
comment|// Cookie based authentication when using HTTP Transport
name|HIVE_SERVER2_THRIFT_HTTP_COOKIE_AUTH_ENABLED
argument_list|(
literal|"hive.server2.thrift.http.cookie.auth.enabled"
argument_list|,
literal|true
argument_list|,
literal|"When true, HiveServer2 in HTTP transport mode, will use cookie based authentication mechanism."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_HTTP_COOKIE_MAX_AGE
argument_list|(
literal|"hive.server2.thrift.http.cookie.max.age"
argument_list|,
literal|"86400s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Maximum age in seconds for server side cookie used by HS2 in HTTP mode."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_HTTP_COOKIE_DOMAIN
argument_list|(
literal|"hive.server2.thrift.http.cookie.domain"
argument_list|,
literal|null
argument_list|,
literal|"Domain for the HS2 generated cookies"
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_HTTP_COOKIE_PATH
argument_list|(
literal|"hive.server2.thrift.http.cookie.path"
argument_list|,
literal|null
argument_list|,
literal|"Path for the HS2 generated cookies"
argument_list|)
block|,
annotation|@
name|Deprecated
name|HIVE_SERVER2_THRIFT_HTTP_COOKIE_IS_SECURE
argument_list|(
literal|"hive.server2.thrift.http.cookie.is.secure"
argument_list|,
literal|true
argument_list|,
literal|"Deprecated: Secure attribute of the HS2 generated cookie (this is automatically enabled for SSL enabled HiveServer2)."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_HTTP_COOKIE_IS_HTTPONLY
argument_list|(
literal|"hive.server2.thrift.http.cookie.is.httponly"
argument_list|,
literal|true
argument_list|,
literal|"HttpOnly attribute of the HS2 generated cookie."
argument_list|)
block|,
comment|// binary transport settings
name|HIVE_SERVER2_THRIFT_PORT
argument_list|(
literal|"hive.server2.thrift.port"
argument_list|,
literal|10000
argument_list|,
literal|"Port number of HiveServer2 Thrift interface when hive.server2.transport.mode is 'binary'."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_SASL_QOP
argument_list|(
literal|"hive.server2.thrift.sasl.qop"
argument_list|,
literal|"auth"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"auth"
argument_list|,
literal|"auth-int"
argument_list|,
literal|"auth-conf"
argument_list|)
argument_list|,
literal|"Sasl QOP value; set it to one of following values to enable higher levels of\n"
operator|+
literal|"protection for HiveServer2 communication with clients.\n"
operator|+
literal|"Setting hadoop.rpc.protection to a higher level than HiveServer2 does not\n"
operator|+
literal|"make sense in most situations. HiveServer2 ignores hadoop.rpc.protection in favor\n"
operator|+
literal|"of hive.server2.thrift.sasl.qop.\n"
operator|+
literal|"  \"auth\" - authentication only (default)\n"
operator|+
literal|"  \"auth-int\" - authentication plus integrity protection\n"
operator|+
literal|"  \"auth-conf\" - authentication plus integrity and confidentiality protection\n"
operator|+
literal|"This is applicable only if HiveServer2 is configured to use Kerberos authentication."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_MIN_WORKER_THREADS
argument_list|(
literal|"hive.server2.thrift.min.worker.threads"
argument_list|,
literal|5
argument_list|,
literal|"Minimum number of Thrift worker threads"
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_MAX_WORKER_THREADS
argument_list|(
literal|"hive.server2.thrift.max.worker.threads"
argument_list|,
literal|500
argument_list|,
literal|"Maximum number of Thrift worker threads"
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_LOGIN_BEBACKOFF_SLOT_LENGTH
argument_list|(
literal|"hive.server2.thrift.exponential.backoff.slot.length"
argument_list|,
literal|"100ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Binary exponential backoff slot time for Thrift clients during login to HiveServer2,\n"
operator|+
literal|"for retries until hitting Thrift client timeout"
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_LOGIN_TIMEOUT
argument_list|(
literal|"hive.server2.thrift.login.timeout"
argument_list|,
literal|"20s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Timeout for Thrift clients during login to HiveServer2"
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_WORKER_KEEPALIVE_TIME
argument_list|(
literal|"hive.server2.thrift.worker.keepalive.time"
argument_list|,
literal|"60s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Keepalive time (in seconds) for an idle worker thread. When the number of workers exceeds min workers, "
operator|+
literal|"excessive threads are killed after this time interval."
argument_list|)
block|,
comment|// Configuration for async thread pool in SessionManager
name|HIVE_SERVER2_ASYNC_EXEC_THREADS
argument_list|(
literal|"hive.server2.async.exec.threads"
argument_list|,
literal|100
argument_list|,
literal|"Number of threads in the async thread pool for HiveServer2"
argument_list|)
block|,
name|HIVE_SERVER2_ASYNC_EXEC_SHUTDOWN_TIMEOUT
argument_list|(
literal|"hive.server2.async.exec.shutdown.timeout"
argument_list|,
literal|"10s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"How long HiveServer2 shutdown will wait for async threads to terminate."
argument_list|)
block|,
name|HIVE_SERVER2_ASYNC_EXEC_WAIT_QUEUE_SIZE
argument_list|(
literal|"hive.server2.async.exec.wait.queue.size"
argument_list|,
literal|100
argument_list|,
literal|"Size of the wait queue for async thread pool in HiveServer2.\n"
operator|+
literal|"After hitting this limit, the async thread pool will reject new requests."
argument_list|)
block|,
name|HIVE_SERVER2_ASYNC_EXEC_KEEPALIVE_TIME
argument_list|(
literal|"hive.server2.async.exec.keepalive.time"
argument_list|,
literal|"10s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Time that an idle HiveServer2 async thread (from the thread pool) will wait for a new task\n"
operator|+
literal|"to arrive before terminating"
argument_list|)
block|,
name|HIVE_SERVER2_ASYNC_EXEC_ASYNC_COMPILE
argument_list|(
literal|"hive.server2.async.exec.async.compile"
argument_list|,
literal|false
argument_list|,
literal|"Whether to enable compiling async query asynchronously. If enabled, it is unknown if the query will have any resultset before compilation completed."
argument_list|)
block|,
name|HIVE_SERVER2_LONG_POLLING_TIMEOUT
argument_list|(
literal|"hive.server2.long.polling.timeout"
argument_list|,
literal|"5000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Time that HiveServer2 will wait before responding to asynchronous calls that use long polling"
argument_list|)
block|,
name|HIVE_SESSION_IMPL_CLASSNAME
argument_list|(
literal|"hive.session.impl.classname"
argument_list|,
literal|null
argument_list|,
literal|"Classname for custom implementation of hive session"
argument_list|)
block|,
name|HIVE_SESSION_IMPL_WITH_UGI_CLASSNAME
argument_list|(
literal|"hive.session.impl.withugi.classname"
argument_list|,
literal|null
argument_list|,
literal|"Classname for custom implementation of hive session with UGI"
argument_list|)
block|,
comment|// HiveServer2 auth configuration
name|HIVE_SERVER2_AUTHENTICATION
argument_list|(
literal|"hive.server2.authentication"
argument_list|,
literal|"NONE"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"NOSASL"
argument_list|,
literal|"NONE"
argument_list|,
literal|"LDAP"
argument_list|,
literal|"KERBEROS"
argument_list|,
literal|"PAM"
argument_list|,
literal|"CUSTOM"
argument_list|)
argument_list|,
literal|"Client authentication types.\n"
operator|+
literal|"  NONE: no authentication check\n"
operator|+
literal|"  LDAP: LDAP/AD based authentication\n"
operator|+
literal|"  KERBEROS: Kerberos/GSSAPI authentication\n"
operator|+
literal|"  CUSTOM: Custom authentication provider\n"
operator|+
literal|"          (Use with property hive.server2.custom.authentication.class)\n"
operator|+
literal|"  PAM: Pluggable authentication module\n"
operator|+
literal|"  NOSASL:  Raw transport"
argument_list|)
block|,
name|HIVE_SERVER2_TRUSTED_DOMAIN
argument_list|(
literal|"hive.server2.trusted.domain"
argument_list|,
literal|""
argument_list|,
literal|"Specifies the host or a domain to trust connections from. Authentication is skipped "
operator|+
literal|"for any connection coming from a host whose hostname ends with the value of this"
operator|+
literal|" property. If authentication is expected to be skipped for connections from "
operator|+
literal|"only a given host, fully qualified hostname of that host should be specified. By default"
operator|+
literal|" it is empty, which means that all the connections to HiveServer2 are authenticated. "
operator|+
literal|"When it is non-empty, the client has to provide a Hive user name. Any password, if "
operator|+
literal|"provided, will not be used when authentication is skipped."
argument_list|)
block|,
name|HIVE_SERVER2_TRUSTED_DOMAIN_USE_XFF_HEADER
argument_list|(
literal|"hive.server2.trusted.domain.use.xff.header"
argument_list|,
literal|false
argument_list|,
literal|"When trusted domain authentication is enabled, the clients connecting to the HS2 could pass"
operator|+
literal|"through many layers of proxy. Some proxies append its own ip address to 'X-Forwarded-For' header"
operator|+
literal|"before passing on the request to another proxy or HS2. Some proxies also connect on behalf of client"
operator|+
literal|"and may create a separate connection to HS2 without binding using client IP. For such environments, instead"
operator|+
literal|"of looking at client IP from the request, if this config is set and if 'X-Forwarded-For' is present,"
operator|+
literal|"trusted domain authentication will use left most ip address from X-Forwarded-For header."
argument_list|)
block|,
name|HIVE_SERVER2_ALLOW_USER_SUBSTITUTION
argument_list|(
literal|"hive.server2.allow.user.substitution"
argument_list|,
literal|true
argument_list|,
literal|"Allow alternate user to be specified as part of HiveServer2 open connection request."
argument_list|)
block|,
name|HIVE_SERVER2_KERBEROS_KEYTAB
argument_list|(
literal|"hive.server2.authentication.kerberos.keytab"
argument_list|,
literal|""
argument_list|,
literal|"Kerberos keytab file for server principal"
argument_list|)
block|,
name|HIVE_SERVER2_KERBEROS_PRINCIPAL
argument_list|(
literal|"hive.server2.authentication.kerberos.principal"
argument_list|,
literal|""
argument_list|,
literal|"Kerberos server principal"
argument_list|)
block|,
name|HIVE_SERVER2_CLIENT_KERBEROS_PRINCIPAL
argument_list|(
literal|"hive.server2.authentication.client.kerberos.principal"
argument_list|,
literal|""
argument_list|,
literal|"Kerberos principal used by the HA hive_server2s."
argument_list|)
block|,
name|HIVE_SERVER2_SPNEGO_KEYTAB
argument_list|(
literal|"hive.server2.authentication.spnego.keytab"
argument_list|,
literal|""
argument_list|,
literal|"keytab file for SPNego principal, optional,\n"
operator|+
literal|"typical value would look like /etc/security/keytabs/spnego.service.keytab,\n"
operator|+
literal|"This keytab would be used by HiveServer2 when Kerberos security is enabled and \n"
operator|+
literal|"HTTP transport mode is used.\n"
operator|+
literal|"This needs to be set only if SPNEGO is to be used in authentication.\n"
operator|+
literal|"SPNego authentication would be honored only if valid\n"
operator|+
literal|"  hive.server2.authentication.spnego.principal\n"
operator|+
literal|"and\n"
operator|+
literal|"  hive.server2.authentication.spnego.keytab\n"
operator|+
literal|"are specified."
argument_list|)
block|,
name|HIVE_SERVER2_SPNEGO_PRINCIPAL
argument_list|(
literal|"hive.server2.authentication.spnego.principal"
argument_list|,
literal|""
argument_list|,
literal|"SPNego service principal, optional,\n"
operator|+
literal|"typical value would look like HTTP/_HOST@EXAMPLE.COM\n"
operator|+
literal|"SPNego service principal would be used by HiveServer2 when Kerberos security is enabled\n"
operator|+
literal|"and HTTP transport mode is used.\n"
operator|+
literal|"This needs to be set only if SPNEGO is to be used in authentication."
argument_list|)
block|,
name|HIVE_SERVER2_PLAIN_LDAP_URL
argument_list|(
literal|"hive.server2.authentication.ldap.url"
argument_list|,
literal|null
argument_list|,
literal|"LDAP connection URL(s),\n"
operator|+
literal|"this value could contain URLs to multiple LDAP servers instances for HA,\n"
operator|+
literal|"each LDAP URL is separated by a SPACE character. URLs are used in the \n"
operator|+
literal|" order specified until a connection is successful."
argument_list|)
block|,
name|HIVE_SERVER2_PLAIN_LDAP_BASEDN
argument_list|(
literal|"hive.server2.authentication.ldap.baseDN"
argument_list|,
literal|null
argument_list|,
literal|"LDAP base DN"
argument_list|)
block|,
name|HIVE_SERVER2_PLAIN_LDAP_DOMAIN
argument_list|(
literal|"hive.server2.authentication.ldap.Domain"
argument_list|,
literal|null
argument_list|,
literal|""
argument_list|)
block|,
name|HIVE_SERVER2_PLAIN_LDAP_GROUPDNPATTERN
argument_list|(
literal|"hive.server2.authentication.ldap.groupDNPattern"
argument_list|,
literal|null
argument_list|,
literal|"COLON-separated list of patterns to use to find DNs for group entities in this directory.\n"
operator|+
literal|"Use %s where the actual group name is to be substituted for.\n"
operator|+
literal|"For example: CN=%s,CN=Groups,DC=subdomain,DC=domain,DC=com."
argument_list|)
block|,
name|HIVE_SERVER2_PLAIN_LDAP_GROUPFILTER
argument_list|(
literal|"hive.server2.authentication.ldap.groupFilter"
argument_list|,
literal|null
argument_list|,
literal|"COMMA-separated list of LDAP Group names (short name not full DNs).\n"
operator|+
literal|"For example: HiveAdmins,HadoopAdmins,Administrators"
argument_list|)
block|,
name|HIVE_SERVER2_PLAIN_LDAP_USERDNPATTERN
argument_list|(
literal|"hive.server2.authentication.ldap.userDNPattern"
argument_list|,
literal|null
argument_list|,
literal|"COLON-separated list of patterns to use to find DNs for users in this directory.\n"
operator|+
literal|"Use %s where the actual group name is to be substituted for.\n"
operator|+
literal|"For example: CN=%s,CN=Users,DC=subdomain,DC=domain,DC=com."
argument_list|)
block|,
name|HIVE_SERVER2_PLAIN_LDAP_USERFILTER
argument_list|(
literal|"hive.server2.authentication.ldap.userFilter"
argument_list|,
literal|null
argument_list|,
literal|"COMMA-separated list of LDAP usernames (just short names, not full DNs).\n"
operator|+
literal|"For example: hiveuser,impalauser,hiveadmin,hadoopadmin"
argument_list|)
block|,
name|HIVE_SERVER2_PLAIN_LDAP_GUIDKEY
argument_list|(
literal|"hive.server2.authentication.ldap.guidKey"
argument_list|,
literal|"uid"
argument_list|,
literal|"LDAP attribute name whose values are unique in this LDAP server.\n"
operator|+
literal|"For example: uid or CN."
argument_list|)
block|,
name|HIVE_SERVER2_PLAIN_LDAP_GROUPMEMBERSHIP_KEY
argument_list|(
literal|"hive.server2.authentication.ldap.groupMembershipKey"
argument_list|,
literal|"member"
argument_list|,
literal|"LDAP attribute name on the group object that contains the list of distinguished names\n"
operator|+
literal|"for the user, group, and contact objects that are members of the group.\n"
operator|+
literal|"For example: member, uniqueMember or memberUid"
argument_list|)
block|,
name|HIVE_SERVER2_PLAIN_LDAP_USERMEMBERSHIP_KEY
argument_list|(
name|HIVE_SERVER2_AUTHENTICATION_LDAP_USERMEMBERSHIPKEY_NAME
argument_list|,
literal|null
argument_list|,
literal|"LDAP attribute name on the user object that contains groups of which the user is\n"
operator|+
literal|"a direct member, except for the primary group, which is represented by the\n"
operator|+
literal|"primaryGroupId.\n"
operator|+
literal|"For example: memberOf"
argument_list|)
block|,
name|HIVE_SERVER2_PLAIN_LDAP_GROUPCLASS_KEY
argument_list|(
literal|"hive.server2.authentication.ldap.groupClassKey"
argument_list|,
literal|"groupOfNames"
argument_list|,
literal|"LDAP attribute name on the group entry that is to be used in LDAP group searches.\n"
operator|+
literal|"For example: group, groupOfNames or groupOfUniqueNames."
argument_list|)
block|,
name|HIVE_SERVER2_PLAIN_LDAP_CUSTOMLDAPQUERY
argument_list|(
literal|"hive.server2.authentication.ldap.customLDAPQuery"
argument_list|,
literal|null
argument_list|,
literal|"A full LDAP query that LDAP Atn provider uses to execute against LDAP Server.\n"
operator|+
literal|"If this query returns a null resultset, the LDAP Provider fails the Authentication\n"
operator|+
literal|"request, succeeds if the user is part of the resultset."
operator|+
literal|"For example: (&(objectClass=group)(objectClass=top)(instanceType=4)(cn=Domain*)) \n"
operator|+
literal|"(&(objectClass=person)(|(sAMAccountName=admin)(|(memberOf=CN=Domain Admins,CN=Users,DC=domain,DC=com)"
operator|+
literal|"(memberOf=CN=Administrators,CN=Builtin,DC=domain,DC=com))))"
argument_list|)
block|,
name|HIVE_SERVER2_PLAIN_LDAP_BIND_USER
argument_list|(
literal|"hive.server2.authentication.ldap.binddn"
argument_list|,
literal|null
argument_list|,
literal|"The user with which to bind to the LDAP server, and search for the full domain name "
operator|+
literal|"of the user being authenticated.\n"
operator|+
literal|"This should be the full domain name of the user, and should have search access across all "
operator|+
literal|"users in the LDAP tree.\n"
operator|+
literal|"If not specified, then the user being authenticated will be used as the bind user.\n"
operator|+
literal|"For example: CN=bindUser,CN=Users,DC=subdomain,DC=domain,DC=com"
argument_list|)
block|,
name|HIVE_SERVER2_PLAIN_LDAP_BIND_PASSWORD
argument_list|(
literal|"hive.server2.authentication.ldap.bindpw"
argument_list|,
literal|null
argument_list|,
literal|"The password for the bind user, to be used to search for the full name of the user being authenticated.\n"
operator|+
literal|"If the username is specified, this parameter must also be specified."
argument_list|)
block|,
name|HIVE_SERVER2_CUSTOM_AUTHENTICATION_CLASS
argument_list|(
literal|"hive.server2.custom.authentication.class"
argument_list|,
literal|null
argument_list|,
literal|"Custom authentication class. Used when property\n"
operator|+
literal|"'hive.server2.authentication' is set to 'CUSTOM'. Provided class\n"
operator|+
literal|"must be a proper implementation of the interface\n"
operator|+
literal|"org.apache.hive.service.auth.PasswdAuthenticationProvider. HiveServer2\n"
operator|+
literal|"will call its Authenticate(user, passed) method to authenticate requests.\n"
operator|+
literal|"The implementation may optionally implement Hadoop's\n"
operator|+
literal|"org.apache.hadoop.conf.Configurable class to grab Hive's Configuration object."
argument_list|)
block|,
name|HIVE_SERVER2_PAM_SERVICES
argument_list|(
literal|"hive.server2.authentication.pam.services"
argument_list|,
literal|null
argument_list|,
literal|"List of the underlying pam services that should be used when auth type is PAM\n"
operator|+
literal|"A file with the same name must exist in /etc/pam.d"
argument_list|)
block|,
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|(
literal|"hive.server2.enable.doAs"
argument_list|,
literal|true
argument_list|,
literal|"Setting this property to true will have HiveServer2 execute\n"
operator|+
literal|"Hive operations as the user making the calls to it."
argument_list|)
block|,
name|HIVE_DISTCP_DOAS_USER
argument_list|(
literal|"hive.distcp.privileged.doAs"
argument_list|,
literal|"hive"
argument_list|,
literal|"This property allows privileged distcp executions done by hive\n"
operator|+
literal|"to run as this user."
argument_list|)
block|,
name|HIVE_SERVER2_TABLE_TYPE_MAPPING
argument_list|(
literal|"hive.server2.table.type.mapping"
argument_list|,
literal|"CLASSIC"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"CLASSIC"
argument_list|,
literal|"HIVE"
argument_list|)
argument_list|,
literal|"This setting reflects how HiveServer2 will report the table types for JDBC and other\n"
operator|+
literal|"client implementations that retrieve the available tables and supported table types\n"
operator|+
literal|"  HIVE : Exposes Hive's native table types like MANAGED_TABLE, EXTERNAL_TABLE, VIRTUAL_VIEW\n"
operator|+
literal|"  CLASSIC : More generic types like TABLE and VIEW"
argument_list|)
block|,
name|HIVE_SERVER2_SESSION_HOOK
argument_list|(
literal|"hive.server2.session.hook"
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|)
block|,
comment|// SSL settings
name|HIVE_SERVER2_USE_SSL
argument_list|(
literal|"hive.server2.use.SSL"
argument_list|,
literal|false
argument_list|,
literal|"Set this to true for using SSL encryption in HiveServer2."
argument_list|)
block|,
name|HIVE_SERVER2_SSL_KEYSTORE_PATH
argument_list|(
literal|"hive.server2.keystore.path"
argument_list|,
literal|""
argument_list|,
literal|"SSL certificate keystore location."
argument_list|)
block|,
name|HIVE_SERVER2_SSL_KEYSTORE_PASSWORD
argument_list|(
literal|"hive.server2.keystore.password"
argument_list|,
literal|""
argument_list|,
literal|"SSL certificate keystore password."
argument_list|)
block|,
name|HIVE_SERVER2_BUILTIN_UDF_WHITELIST
argument_list|(
literal|"hive.server2.builtin.udf.whitelist"
argument_list|,
literal|""
argument_list|,
literal|"Comma separated list of builtin udf names allowed in queries.\n"
operator|+
literal|"An empty whitelist allows all builtin udfs to be executed. "
operator|+
literal|" The udf black list takes precedence over udf white list"
argument_list|)
block|,
name|HIVE_SERVER2_BUILTIN_UDF_BLACKLIST
argument_list|(
literal|"hive.server2.builtin.udf.blacklist"
argument_list|,
literal|""
argument_list|,
literal|"Comma separated list of udfs names. These udfs will not be allowed in queries."
operator|+
literal|" The udf black list takes precedence over udf white list"
argument_list|)
block|,
name|HIVE_ALLOW_UDF_LOAD_ON_DEMAND
argument_list|(
literal|"hive.allow.udf.load.on.demand"
argument_list|,
literal|false
argument_list|,
literal|"Whether enable loading UDFs from metastore on demand; this is mostly relevant for\n"
operator|+
literal|"HS2 and was the default behavior before Hive 1.2. Off by default."
argument_list|)
block|,
name|HIVE_SERVER2_SESSION_CHECK_INTERVAL
argument_list|(
literal|"hive.server2.session.check.interval"
argument_list|,
literal|"15m"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
literal|3000l
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
argument_list|,
literal|"The check interval for session/operation timeout, which can be disabled by setting to zero or negative value."
argument_list|)
block|,
name|HIVE_SERVER2_CLOSE_SESSION_ON_DISCONNECT
argument_list|(
literal|"hive.server2.close.session.on.disconnect"
argument_list|,
literal|true
argument_list|,
literal|"Session will be closed when connection is closed. Set this to false to have session outlive its parent connection."
argument_list|)
block|,
name|HIVE_SERVER2_IDLE_SESSION_TIMEOUT
argument_list|(
literal|"hive.server2.idle.session.timeout"
argument_list|,
literal|"4h"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Session will be closed when it's not accessed for this duration, which can be disabled by setting to zero or negative value."
argument_list|)
block|,
name|HIVE_SERVER2_IDLE_OPERATION_TIMEOUT
argument_list|(
literal|"hive.server2.idle.operation.timeout"
argument_list|,
literal|"2h"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Operation will be closed when it's not accessed for this duration of time, which can be disabled by setting to zero value.\n"
operator|+
literal|"  With positive value, it's checked for operations in terminal state only (FINISHED, CANCELED, CLOSED, ERROR).\n"
operator|+
literal|"  With negative value, it's checked for all of the operations regardless of state."
argument_list|)
block|,
name|HIVE_SERVER2_IDLE_SESSION_CHECK_OPERATION
argument_list|(
literal|"hive.server2.idle.session.check.operation"
argument_list|,
literal|true
argument_list|,
literal|"Session will be considered to be idle only if there is no activity, and there is no pending operation.\n"
operator|+
literal|" This setting takes effect only if session idle timeout (hive.server2.idle.session.timeout) and checking\n"
operator|+
literal|"(hive.server2.session.check.interval) are enabled."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_CLIENT_RETRY_LIMIT
argument_list|(
literal|"hive.server2.thrift.client.retry.limit"
argument_list|,
literal|1
argument_list|,
literal|"Number of retries upon "
operator|+
literal|"failure of Thrift HiveServer2 calls"
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_CLIENT_CONNECTION_RETRY_LIMIT
argument_list|(
literal|"hive.server2.thrift.client.connect.retry.limit"
argument_list|,
literal|1
argument_list|,
literal|"Number of "
operator|+
literal|"retries while opening a connection to HiveServe2"
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_CLIENT_RETRY_DELAY_SECONDS
argument_list|(
literal|"hive.server2.thrift.client.retry.delay.seconds"
argument_list|,
literal|"1s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Number of seconds for the HiveServer2 thrift client to wait between "
operator|+
literal|"consecutive connection attempts. Also specifies the time to wait between retrying thrift calls upon failures"
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_CLIENT_USER
argument_list|(
literal|"hive.server2.thrift.client.user"
argument_list|,
literal|"anonymous"
argument_list|,
literal|"Username to use against thrift"
operator|+
literal|" client"
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_CLIENT_PASSWORD
argument_list|(
literal|"hive.server2.thrift.client.password"
argument_list|,
literal|"anonymous"
argument_list|,
literal|"Password to use against "
operator|+
literal|"thrift client"
argument_list|)
block|,
comment|// ResultSet serialization settings
name|HIVE_SERVER2_THRIFT_RESULTSET_SERIALIZE_IN_TASKS
argument_list|(
literal|"hive.server2.thrift.resultset.serialize.in.tasks"
argument_list|,
literal|false
argument_list|,
literal|"Whether we should serialize the Thrift structures used in JDBC ResultSet RPC in task nodes.\n "
operator|+
literal|"We use SequenceFile and ThriftJDBCBinarySerDe to read and write the final results if this is true."
argument_list|)
block|,
comment|// TODO: Make use of this config to configure fetch size
name|HIVE_SERVER2_THRIFT_RESULTSET_MAX_FETCH_SIZE
argument_list|(
literal|"hive.server2.thrift.resultset.max.fetch.size"
argument_list|,
literal|10000
argument_list|,
literal|"Max number of rows sent in one Fetch RPC call by the server to the client."
argument_list|)
block|,
name|HIVE_SERVER2_THRIFT_RESULTSET_DEFAULT_FETCH_SIZE
argument_list|(
literal|"hive.server2.thrift.resultset.default.fetch.size"
argument_list|,
literal|1000
argument_list|,
literal|"The number of rows sent in one Fetch RPC call by the server to the client, if not\n"
operator|+
literal|"specified by the client."
argument_list|)
block|,
name|HIVE_SERVER2_XSRF_FILTER_ENABLED
argument_list|(
literal|"hive.server2.xsrf.filter.enabled"
argument_list|,
literal|false
argument_list|,
literal|"If enabled, HiveServer2 will block any requests made to it over http "
operator|+
literal|"if an X-XSRF-HEADER header is not present"
argument_list|)
block|,
name|HIVE_SECURITY_COMMAND_WHITELIST
argument_list|(
literal|"hive.security.command.whitelist"
argument_list|,
literal|"set,reset,dfs,add,list,delete,reload,compile,llap"
argument_list|,
literal|"Comma separated list of non-SQL Hive commands users are authorized to execute"
argument_list|)
block|,
name|HIVE_SERVER2_JOB_CREDENTIAL_PROVIDER_PATH
argument_list|(
literal|"hive.server2.job.credential.provider.path"
argument_list|,
literal|""
argument_list|,
literal|"If set, this configuration property should provide a comma-separated list of URLs that indicates the type and "
operator|+
literal|"location of providers to be used by hadoop credential provider API. It provides HiveServer2 the ability to provide job-specific "
operator|+
literal|"credential providers for jobs run using MR and Spark execution engines. This functionality has not been tested against Tez."
argument_list|)
block|,
name|HIVE_MOVE_FILES_THREAD_COUNT
argument_list|(
literal|"hive.mv.files.thread"
argument_list|,
literal|15
argument_list|,
operator|new
name|SizeValidator
argument_list|(
literal|0L
argument_list|,
literal|true
argument_list|,
literal|1024L
argument_list|,
literal|true
argument_list|)
argument_list|,
literal|"Number of threads"
operator|+
literal|" used to move files in move task. Set it to 0 to disable multi-threaded file moves. This parameter is also used by"
operator|+
literal|" MSCK to check tables."
argument_list|)
block|,
name|HIVE_LOAD_DYNAMIC_PARTITIONS_THREAD_COUNT
argument_list|(
literal|"hive.load.dynamic.partitions.thread"
argument_list|,
literal|15
argument_list|,
operator|new
name|SizeValidator
argument_list|(
literal|1L
argument_list|,
literal|true
argument_list|,
literal|1024L
argument_list|,
literal|true
argument_list|)
argument_list|,
literal|"Number of threads used to load dynamic partitions."
argument_list|)
block|,
comment|// If this is set all move tasks at the end of a multi-insert query will only begin once all
comment|// outputs are ready
name|HIVE_MULTI_INSERT_MOVE_TASKS_SHARE_DEPENDENCIES
argument_list|(
literal|"hive.multi.insert.move.tasks.share.dependencies"
argument_list|,
literal|false
argument_list|,
literal|"If this is set all move tasks for tables/partitions (not directories) at the end of a\n"
operator|+
literal|"multi-insert query will only begin once the dependencies for all these move tasks have been\n"
operator|+
literal|"met.\n"
operator|+
literal|"Advantages: If concurrency is enabled, the locks will only be released once the query has\n"
operator|+
literal|"            finished, so with this config enabled, the time when the table/partition is\n"
operator|+
literal|"            generated will be much closer to when the lock on it is released.\n"
operator|+
literal|"Disadvantages: If concurrency is not enabled, with this disabled, the tables/partitions which\n"
operator|+
literal|"               are produced by this query and finish earlier will be available for querying\n"
operator|+
literal|"               much earlier.  Since the locks are only released once the query finishes, this\n"
operator|+
literal|"               does not apply if concurrency is enabled."
argument_list|)
block|,
name|HIVE_INFER_BUCKET_SORT
argument_list|(
literal|"hive.exec.infer.bucket.sort"
argument_list|,
literal|false
argument_list|,
literal|"If this is set, when writing partitions, the metadata will include the bucketing/sorting\n"
operator|+
literal|"properties with which the data was written if any (this will not overwrite the metadata\n"
operator|+
literal|"inherited from the table if the table is bucketed/sorted)"
argument_list|)
block|,
name|HIVE_INFER_BUCKET_SORT_NUM_BUCKETS_POWER_TWO
argument_list|(
literal|"hive.exec.infer.bucket.sort.num.buckets.power.two"
argument_list|,
literal|false
argument_list|,
literal|"If this is set, when setting the number of reducers for the map reduce task which writes the\n"
operator|+
literal|"final output files, it will choose a number which is a power of two, unless the user specifies\n"
operator|+
literal|"the number of reducers to use using mapred.reduce.tasks.  The number of reducers\n"
operator|+
literal|"may be set to a power of two, only to be followed by a merge task meaning preventing\n"
operator|+
literal|"anything from being inferred.\n"
operator|+
literal|"With hive.exec.infer.bucket.sort set to true:\n"
operator|+
literal|"Advantages:  If this is not set, the number of buckets for partitions will seem arbitrary,\n"
operator|+
literal|"             which means that the number of mappers used for optimized joins, for example, will\n"
operator|+
literal|"             be very low.  With this set, since the number of buckets used for any partition is\n"
operator|+
literal|"             a power of two, the number of mappers used for optimized joins will be the least\n"
operator|+
literal|"             number of buckets used by any partition being joined.\n"
operator|+
literal|"Disadvantages: This may mean a much larger or much smaller number of reducers being used in the\n"
operator|+
literal|"               final map reduce job, e.g. if a job was originally going to take 257 reducers,\n"
operator|+
literal|"               it will now take 512 reducers, similarly if the max number of reducers is 511,\n"
operator|+
literal|"               and a job was going to use this many, it will now use 256 reducers."
argument_list|)
block|,
name|HIVEOPTLISTBUCKETING
argument_list|(
literal|"hive.optimize.listbucketing"
argument_list|,
literal|false
argument_list|,
literal|"Enable list bucketing optimizer. Default value is false so that we disable it by default."
argument_list|)
block|,
comment|// Allow TCP Keep alive socket option for for HiveServer or a maximum timeout for the socket.
name|SERVER_READ_SOCKET_TIMEOUT
argument_list|(
literal|"hive.server.read.socket.timeout"
argument_list|,
literal|"10s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Timeout for the HiveServer to close the connection if no response from the client. By default, 10 seconds."
argument_list|)
block|,
name|SERVER_TCP_KEEP_ALIVE
argument_list|(
literal|"hive.server.tcp.keepalive"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable TCP keepalive for the Hive Server. Keepalive will prevent accumulation of half-open connections."
argument_list|)
block|,
name|HIVE_DECODE_PARTITION_NAME
argument_list|(
literal|"hive.decode.partition.name"
argument_list|,
literal|false
argument_list|,
literal|"Whether to show the unquoted partition names in query results."
argument_list|)
block|,
name|HIVE_EXECUTION_ENGINE
argument_list|(
literal|"hive.execution.engine"
argument_list|,
literal|"mr"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|true
argument_list|,
literal|"mr"
argument_list|,
literal|"tez"
argument_list|,
literal|"spark"
argument_list|)
argument_list|,
literal|"Chooses execution engine. Options are: mr (Map reduce, default), tez, spark. While MR\n"
operator|+
literal|"remains the default engine for historical reasons, it is itself a historical engine\n"
operator|+
literal|"and is deprecated in Hive 2 line. It may be removed without further warning."
argument_list|)
block|,
name|HIVE_EXECUTION_MODE
argument_list|(
literal|"hive.execution.mode"
argument_list|,
literal|"container"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"container"
argument_list|,
literal|"llap"
argument_list|)
argument_list|,
literal|"Chooses whether query fragments will run in container or in llap"
argument_list|)
block|,
name|HIVE_JAR_DIRECTORY
argument_list|(
literal|"hive.jar.directory"
argument_list|,
literal|null
argument_list|,
literal|"This is the location hive in tez mode will look for to find a site wide \n"
operator|+
literal|"installed hive instance."
argument_list|)
block|,
name|HIVE_USER_INSTALL_DIR
argument_list|(
literal|"hive.user.install.directory"
argument_list|,
literal|"/user/"
argument_list|,
literal|"If hive (in tez mode only) cannot find a usable hive jar in \"hive.jar.directory\", \n"
operator|+
literal|"it will upload the hive jar to \"hive.user.install.directory/user.name\"\n"
operator|+
literal|"and use it to run queries."
argument_list|)
block|,
comment|// Vectorization enabled
name|HIVE_VECTORIZATION_ENABLED
argument_list|(
literal|"hive.vectorized.execution.enabled"
argument_list|,
literal|true
argument_list|,
literal|"This flag should be set to true to enable vectorized mode of query execution.\n"
operator|+
literal|"The default value is true to reflect that our most expected Hive deployment will be using vectorization."
argument_list|)
block|,
name|HIVE_VECTORIZATION_REDUCE_ENABLED
argument_list|(
literal|"hive.vectorized.execution.reduce.enabled"
argument_list|,
literal|true
argument_list|,
literal|"This flag should be set to true to enable vectorized mode of the reduce-side of query execution.\n"
operator|+
literal|"The default value is true."
argument_list|)
block|,
name|HIVE_VECTORIZATION_REDUCE_GROUPBY_ENABLED
argument_list|(
literal|"hive.vectorized.execution.reduce.groupby.enabled"
argument_list|,
literal|true
argument_list|,
literal|"This flag should be set to true to enable vectorized mode of the reduce-side GROUP BY query execution.\n"
operator|+
literal|"The default value is true."
argument_list|)
block|,
name|HIVE_VECTORIZATION_MAPJOIN_NATIVE_ENABLED
argument_list|(
literal|"hive.vectorized.execution.mapjoin.native.enabled"
argument_list|,
literal|true
argument_list|,
literal|"This flag should be set to true to enable native (i.e. non-pass through) vectorization\n"
operator|+
literal|"of queries using MapJoin.\n"
operator|+
literal|"The default value is true."
argument_list|)
block|,
name|HIVE_VECTORIZATION_MAPJOIN_NATIVE_MULTIKEY_ONLY_ENABLED
argument_list|(
literal|"hive.vectorized.execution.mapjoin.native.multikey.only.enabled"
argument_list|,
literal|false
argument_list|,
literal|"This flag should be set to true to restrict use of native vector map join hash tables to\n"
operator|+
literal|"the MultiKey in queries using MapJoin.\n"
operator|+
literal|"The default value is false."
argument_list|)
block|,
name|HIVE_VECTORIZATION_MAPJOIN_NATIVE_MINMAX_ENABLED
argument_list|(
literal|"hive.vectorized.execution.mapjoin.minmax.enabled"
argument_list|,
literal|false
argument_list|,
literal|"This flag should be set to true to enable vector map join hash tables to\n"
operator|+
literal|"use max / max filtering for integer join queries using MapJoin.\n"
operator|+
literal|"The default value is false."
argument_list|)
block|,
name|HIVE_VECTORIZATION_MAPJOIN_NATIVE_OVERFLOW_REPEATED_THRESHOLD
argument_list|(
literal|"hive.vectorized.execution.mapjoin.overflow.repeated.threshold"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"The number of small table rows for a match in vector map join hash tables\n"
operator|+
literal|"where we use the repeated field optimization in overflow vectorized row batch for join queries using MapJoin.\n"
operator|+
literal|"A value of -1 means do use the join result optimization.  Otherwise, threshold value can be 0 to maximum integer."
argument_list|)
block|,
name|HIVE_VECTORIZATION_MAPJOIN_NATIVE_FAST_HASHTABLE_ENABLED
argument_list|(
literal|"hive.vectorized.execution.mapjoin.native.fast.hashtable.enabled"
argument_list|,
literal|false
argument_list|,
literal|"This flag should be set to true to enable use of native fast vector map join hash tables in\n"
operator|+
literal|"queries using MapJoin.\n"
operator|+
literal|"The default value is false."
argument_list|)
block|,
name|HIVE_VECTORIZATION_GROUPBY_CHECKINTERVAL
argument_list|(
literal|"hive.vectorized.groupby.checkinterval"
argument_list|,
literal|100000
argument_list|,
literal|"Number of entries added to the group by aggregation hash before a recomputation of average entry size is performed."
argument_list|)
block|,
name|HIVE_VECTORIZATION_GROUPBY_MAXENTRIES
argument_list|(
literal|"hive.vectorized.groupby.maxentries"
argument_list|,
literal|1000000
argument_list|,
literal|"Max number of entries in the vector group by aggregation hashtables. \n"
operator|+
literal|"Exceeding this will trigger a flush irrelevant of memory pressure condition."
argument_list|)
block|,
name|HIVE_VECTORIZATION_GROUPBY_FLUSH_PERCENT
argument_list|(
literal|"hive.vectorized.groupby.flush.percent"
argument_list|,
operator|(
name|float
operator|)
literal|0.1
argument_list|,
literal|"Percent of entries in the group by aggregation hash flushed when the memory threshold is exceeded."
argument_list|)
block|,
name|HIVE_VECTORIZATION_REDUCESINK_NEW_ENABLED
argument_list|(
literal|"hive.vectorized.execution.reducesink.new.enabled"
argument_list|,
literal|true
argument_list|,
literal|"This flag should be set to true to enable the new vectorization\n"
operator|+
literal|"of queries using ReduceSink.\ni"
operator|+
literal|"The default value is true."
argument_list|)
block|,
name|HIVE_VECTORIZATION_USE_VECTORIZED_INPUT_FILE_FORMAT
argument_list|(
literal|"hive.vectorized.use.vectorized.input.format"
argument_list|,
literal|true
argument_list|,
literal|"This flag should be set to true to enable vectorizing with vectorized input file format capable SerDe.\n"
operator|+
literal|"The default value is true."
argument_list|)
block|,
name|HIVE_VECTORIZATION_VECTORIZED_INPUT_FILE_FORMAT_EXCLUDES
argument_list|(
literal|"hive.vectorized.input.format.excludes"
argument_list|,
literal|""
argument_list|,
literal|"This configuration should be set to fully described input format class names for which \n"
operator|+
literal|" vectorized input format should not be used for vectorized execution."
argument_list|)
block|,
name|HIVE_VECTORIZATION_USE_VECTOR_DESERIALIZE
argument_list|(
literal|"hive.vectorized.use.vector.serde.deserialize"
argument_list|,
literal|true
argument_list|,
literal|"This flag should be set to true to enable vectorizing rows using vector deserialize.\n"
operator|+
literal|"The default value is true."
argument_list|)
block|,
name|HIVE_VECTORIZATION_USE_ROW_DESERIALIZE
argument_list|(
literal|"hive.vectorized.use.row.serde.deserialize"
argument_list|,
literal|true
argument_list|,
literal|"This flag should be set to true to enable vectorizing using row deserialize.\n"
operator|+
literal|"The default value is false."
argument_list|)
block|,
name|HIVE_VECTORIZATION_ROW_DESERIALIZE_INPUTFORMAT_EXCLUDES
argument_list|(
literal|"hive.vectorized.row.serde.inputformat.excludes"
argument_list|,
literal|"org.apache.parquet.hadoop.ParquetInputFormat,org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat"
argument_list|,
literal|"The input formats not supported by row deserialize vectorization."
argument_list|)
block|,
name|HIVE_VECTOR_ADAPTOR_USAGE_MODE
argument_list|(
literal|"hive.vectorized.adaptor.usage.mode"
argument_list|,
literal|"all"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"none"
argument_list|,
literal|"chosen"
argument_list|,
literal|"all"
argument_list|)
argument_list|,
literal|"Specifies the extent to which the VectorUDFAdaptor will be used for UDFs that do not have a corresponding vectorized class.\n"
operator|+
literal|"0. none   : disable any usage of VectorUDFAdaptor\n"
operator|+
literal|"1. chosen : use VectorUDFAdaptor for a small set of UDFs that were chosen for good performance\n"
operator|+
literal|"2. all    : use VectorUDFAdaptor for all UDFs"
argument_list|)
block|,
name|HIVE_TEST_VECTOR_ADAPTOR_OVERRIDE
argument_list|(
literal|"hive.test.vectorized.adaptor.override"
argument_list|,
literal|false
argument_list|,
literal|"internal use only, used to force always using the VectorUDFAdaptor.\n"
operator|+
literal|"The default is false, of course"
argument_list|,
literal|true
argument_list|)
block|,
name|HIVE_VECTORIZATION_PTF_ENABLED
argument_list|(
literal|"hive.vectorized.execution.ptf.enabled"
argument_list|,
literal|true
argument_list|,
literal|"This flag should be set to true to enable vectorized mode of the PTF of query execution.\n"
operator|+
literal|"The default value is true."
argument_list|)
block|,
name|HIVE_VECTORIZATION_PTF_MAX_MEMORY_BUFFERING_BATCH_COUNT
argument_list|(
literal|"hive.vectorized.ptf.max.memory.buffering.batch.count"
argument_list|,
literal|25
argument_list|,
literal|"Maximum number of vectorized row batches to buffer in memory for PTF\n"
operator|+
literal|"The default value is 25"
argument_list|)
block|,
name|HIVE_VECTORIZATION_TESTING_REDUCER_BATCH_SIZE
argument_list|(
literal|"hive.vectorized.testing.reducer.batch.size"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"internal use only, used for creating small group key vectorized row batches to exercise more logic\n"
operator|+
literal|"The default value is -1 which means don't restrict for testing"
argument_list|,
literal|true
argument_list|)
block|,
name|HIVE_VECTORIZATION_TESTING_REUSE_SCRATCH_COLUMNS
argument_list|(
literal|"hive.vectorized.reuse.scratch.columns"
argument_list|,
literal|true
argument_list|,
literal|"internal use only. Disable this to debug scratch column state issues"
argument_list|,
literal|true
argument_list|)
block|,
name|HIVE_VECTORIZATION_COMPLEX_TYPES_ENABLED
argument_list|(
literal|"hive.vectorized.complex.types.enabled"
argument_list|,
literal|true
argument_list|,
literal|"This flag should be set to true to enable vectorization\n"
operator|+
literal|"of expressions with complex types.\n"
operator|+
literal|"The default value is true."
argument_list|)
block|,
name|HIVE_VECTORIZATION_GROUPBY_COMPLEX_TYPES_ENABLED
argument_list|(
literal|"hive.vectorized.groupby.complex.types.enabled"
argument_list|,
literal|true
argument_list|,
literal|"This flag should be set to true to enable group by vectorization\n"
operator|+
literal|"of aggregations that use complex types.\n"
operator|+
literal|"For example, AVG uses a complex type (STRUCT) for partial aggregation results"
operator|+
literal|"The default value is true."
argument_list|)
block|,
name|HIVE_VECTORIZATION_ROW_IDENTIFIER_ENABLED
argument_list|(
literal|"hive.vectorized.row.identifier.enabled"
argument_list|,
literal|true
argument_list|,
literal|"This flag should be set to true to enable vectorization of ROW__ID."
argument_list|)
block|,
name|HIVE_VECTORIZATION_USE_CHECKED_EXPRESSIONS
argument_list|(
literal|"hive.vectorized.use.checked.expressions"
argument_list|,
literal|false
argument_list|,
literal|"This flag should be set to true to use overflow checked vector expressions when available.\n"
operator|+
literal|"For example, arithmetic expressions which can overflow the output data type can be evaluated using\n"
operator|+
literal|" checked vector expressions so that they produce same result as non-vectorized evaluation."
argument_list|)
block|,
name|HIVE_VECTORIZED_ADAPTOR_SUPPRESS_EVALUATE_EXCEPTIONS
argument_list|(
literal|"hive.vectorized.adaptor.suppress.evaluate.exceptions"
argument_list|,
literal|false
argument_list|,
literal|"This flag should be set to true to suppress HiveException from the generic UDF function\n"
operator|+
literal|"evaluate call and turn them into NULLs. Assume, by default, this is not needed"
argument_list|)
block|,
name|HIVE_VECTORIZED_INPUT_FORMAT_SUPPORTS_ENABLED
argument_list|(
literal|"hive.vectorized.input.format.supports.enabled"
argument_list|,
literal|"decimal_64"
argument_list|,
literal|"Which vectorized input format support features are enabled for vectorization.\n"
operator|+
literal|"That is, if a VectorizedInputFormat input format does support \"decimal_64\" for example\n"
operator|+
literal|"this variable must enable that to be used in vectorization"
argument_list|)
block|,
name|HIVE_VECTORIZED_IF_EXPR_MODE
argument_list|(
literal|"hive.vectorized.if.expr.mode"
argument_list|,
literal|"better"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"adaptor"
argument_list|,
literal|"good"
argument_list|,
literal|"better"
argument_list|)
argument_list|,
literal|"Specifies the extent to which SQL IF statements will be vectorized.\n"
operator|+
literal|"0. adaptor: only use the VectorUDFAdaptor to vectorize IF statements\n"
operator|+
literal|"1. good   : use regular vectorized IF expression classes that get good performance\n"
operator|+
literal|"2. better : use vectorized IF expression classes that conditionally execute THEN/ELSE\n"
operator|+
literal|"            expressions for better performance.\n"
argument_list|)
block|,
name|HIVE_TEST_VECTORIZATION_ENABLED_OVERRIDE
argument_list|(
literal|"hive.test.vectorized.execution.enabled.override"
argument_list|,
literal|"none"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"none"
argument_list|,
literal|"enable"
argument_list|,
literal|"disable"
argument_list|)
argument_list|,
literal|"internal use only, used to override the hive.vectorized.execution.enabled setting and\n"
operator|+
literal|"turn off vectorization.  The default is false, of course"
argument_list|,
literal|true
argument_list|)
block|,
name|HIVE_TEST_VECTORIZATION_SUPPRESS_EXPLAIN_EXECUTION_MODE
argument_list|(
literal|"hive.test.vectorization.suppress.explain.execution.mode"
argument_list|,
literal|false
argument_list|,
literal|"internal use only, used to suppress \"Execution mode: vectorized\" EXPLAIN display.\n"
operator|+
literal|"The default is false, of course"
argument_list|,
literal|true
argument_list|)
block|,
name|HIVE_TEST_VECTORIZER_SUPPRESS_FATAL_EXCEPTIONS
argument_list|(
literal|"hive.test.vectorizer.suppress.fatal.exceptions"
argument_list|,
literal|true
argument_list|,
literal|"internal use only. When false, don't suppress fatal exceptions like\n"
operator|+
literal|"NullPointerException, etc so the query will fail and assure it will be noticed"
argument_list|,
literal|true
argument_list|)
block|,
name|HIVE_VECTORIZATION_FILESINK_ARROW_NATIVE_ENABLED
argument_list|(
literal|"hive.vectorized.execution.filesink.arrow.native.enabled"
argument_list|,
literal|false
argument_list|,
literal|"This flag should be set to true to enable the native vectorization\n"
operator|+
literal|"of queries using the Arrow SerDe and FileSink.\n"
operator|+
literal|"The default value is false."
argument_list|)
block|,
name|HIVE_TYPE_CHECK_ON_INSERT
argument_list|(
literal|"hive.typecheck.on.insert"
argument_list|,
literal|true
argument_list|,
literal|"This property has been extended to control "
operator|+
literal|"whether to check, convert, and normalize partition value to conform to its column type in "
operator|+
literal|"partition operations including but not limited to insert, such as alter, describe etc."
argument_list|)
block|,
name|HIVE_HADOOP_CLASSPATH
argument_list|(
literal|"hive.hadoop.classpath"
argument_list|,
literal|null
argument_list|,
literal|"For Windows OS, we need to pass HIVE_HADOOP_CLASSPATH Java parameter while starting HiveServer2 \n"
operator|+
literal|"using \"-hiveconf hive.hadoop.classpath=%HIVE_LIB%\"."
argument_list|)
block|,
name|HIVE_RPC_QUERY_PLAN
argument_list|(
literal|"hive.rpc.query.plan"
argument_list|,
literal|false
argument_list|,
literal|"Whether to send the query plan via local resource or RPC"
argument_list|)
block|,
name|HIVE_AM_SPLIT_GENERATION
argument_list|(
literal|"hive.compute.splits.in.am"
argument_list|,
literal|true
argument_list|,
literal|"Whether to generate the splits locally or in the AM (tez only)"
argument_list|)
block|,
name|HIVE_TEZ_GENERATE_CONSISTENT_SPLITS
argument_list|(
literal|"hive.tez.input.generate.consistent.splits"
argument_list|,
literal|true
argument_list|,
literal|"Whether to generate consistent split locations when generating splits in the AM"
argument_list|)
block|,
name|HIVE_PREWARM_ENABLED
argument_list|(
literal|"hive.prewarm.enabled"
argument_list|,
literal|false
argument_list|,
literal|"Enables container prewarm for Tez/Spark (Hadoop 2 only)"
argument_list|)
block|,
name|HIVE_PREWARM_NUM_CONTAINERS
argument_list|(
literal|"hive.prewarm.numcontainers"
argument_list|,
literal|10
argument_list|,
literal|"Controls the number of containers to prewarm for Tez/Spark (Hadoop 2 only)"
argument_list|)
block|,
name|HIVE_PREWARM_SPARK_TIMEOUT
argument_list|(
literal|"hive.prewarm.spark.timeout"
argument_list|,
literal|"5000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Time to wait to finish prewarming spark executors"
argument_list|)
block|,
name|HIVESTAGEIDREARRANGE
argument_list|(
literal|"hive.stageid.rearrange"
argument_list|,
literal|"none"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"none"
argument_list|,
literal|"idonly"
argument_list|,
literal|"traverse"
argument_list|,
literal|"execution"
argument_list|)
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEEXPLAINDEPENDENCYAPPENDTASKTYPES
argument_list|(
literal|"hive.explain.dependency.append.tasktype"
argument_list|,
literal|false
argument_list|,
literal|""
argument_list|)
block|,
name|HIVEUSEGOOGLEREGEXENGINE
argument_list|(
literal|"hive.use.googleregex.engine"
argument_list|,
literal|false
argument_list|,
literal|"whether to use google regex engine or not, default regex engine is java.util.regex"
argument_list|)
block|,
name|HIVECOUNTERGROUP
argument_list|(
literal|"hive.counters.group.name"
argument_list|,
literal|"HIVE"
argument_list|,
literal|"The name of counter group for internal Hive variables (CREATED_FILE, FATAL_ERROR, etc.)"
argument_list|)
block|,
name|HIVE_QUOTEDID_SUPPORT
argument_list|(
literal|"hive.support.quoted.identifiers"
argument_list|,
literal|"column"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"none"
argument_list|,
literal|"column"
argument_list|)
argument_list|,
literal|"Whether to use quoted identifier. 'none' or 'column' can be used. \n"
operator|+
literal|"  none: default(past) behavior. Implies only alphaNumeric and underscore are valid characters in identifiers.\n"
operator|+
literal|"  column: implies column names can contain any character."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.SUPPORT_SPECIAL_CHARACTERS_IN_TABLE_NAMES      */
annotation|@
name|Deprecated
name|HIVE_SUPPORT_SPECICAL_CHARACTERS_IN_TABLE_NAMES
argument_list|(
literal|"hive.support.special.characters.tablename"
argument_list|,
literal|true
argument_list|,
literal|"This flag should be set to true to enable support for special characters in table names.\n"
operator|+
literal|"When it is set to false, only [a-zA-Z_0-9]+ are supported.\n"
operator|+
literal|"The only supported special character right now is '/'. This flag applies only to quoted table names.\n"
operator|+
literal|"The default value is true."
argument_list|)
block|,
name|HIVE_CREATE_TABLES_AS_INSERT_ONLY
argument_list|(
literal|"hive.create.as.insert.only"
argument_list|,
literal|false
argument_list|,
literal|"Whether the eligible tables should be created as ACID insert-only by default. Does \n"
operator|+
literal|"not apply to external tables, the ones using storage handlers, etc."
argument_list|)
block|,
name|HIVE_ACID_DIRECT_INSERT_ENABLED
argument_list|(
literal|"hive.acid.direct.insert.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Enable writing the data files directly to the table's final destination instead of the staging directory."
operator|+
literal|"This optimization only applies on INSERT operations on ACID tables."
argument_list|)
block|,
comment|// role names are case-insensitive
name|USERS_IN_ADMIN_ROLE
argument_list|(
literal|"hive.users.in.admin.role"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|,
literal|"Comma separated list of users who are in admin role for bootstrapping.\n"
operator|+
literal|"More users can be added in ADMIN role later."
argument_list|)
block|,
name|HIVE_COMPAT
argument_list|(
literal|"hive.compat"
argument_list|,
name|HiveCompat
operator|.
name|DEFAULT_COMPAT_LEVEL
argument_list|,
literal|"Enable (configurable) deprecated behaviors by setting desired level of backward compatibility.\n"
operator|+
literal|"Setting to 0.12:\n"
operator|+
literal|"  Maintains division behavior: int / int = double"
argument_list|)
block|,
name|HIVE_CONVERT_JOIN_BUCKET_MAPJOIN_TEZ
argument_list|(
literal|"hive.convert.join.bucket.mapjoin.tez"
argument_list|,
literal|true
argument_list|,
literal|"Whether joins can be automatically converted to bucket map joins in hive \n"
operator|+
literal|"when tez is used as the execution engine."
argument_list|)
block|,
name|HIVE_TEZ_BMJ_USE_SUBCACHE
argument_list|(
literal|"hive.tez.bmj.use.subcache"
argument_list|,
literal|true
argument_list|,
literal|"Use subcache to reuse hashtable across multiple tasks"
argument_list|)
block|,
name|HIVE_CHECK_CROSS_PRODUCT
argument_list|(
literal|"hive.exec.check.crossproducts"
argument_list|,
literal|true
argument_list|,
literal|"Check if a plan contains a Cross Product. If there is one, output a warning to the Session's console."
argument_list|)
block|,
name|HIVE_LOCALIZE_RESOURCE_WAIT_INTERVAL
argument_list|(
literal|"hive.localize.resource.wait.interval"
argument_list|,
literal|"5000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Time to wait for another thread to localize the same resource for hive-tez."
argument_list|)
block|,
name|HIVE_LOCALIZE_RESOURCE_NUM_WAIT_ATTEMPTS
argument_list|(
literal|"hive.localize.resource.num.wait.attempts"
argument_list|,
literal|5
argument_list|,
literal|"The number of attempts waiting for localizing a resource in hive-tez."
argument_list|)
block|,
name|TEZ_AUTO_REDUCER_PARALLELISM
argument_list|(
literal|"hive.tez.auto.reducer.parallelism"
argument_list|,
literal|false
argument_list|,
literal|"Turn on Tez' auto reducer parallelism feature. When enabled, Hive will still estimate data sizes\n"
operator|+
literal|"and set parallelism estimates. Tez will sample source vertices' output sizes and adjust the estimates at runtime as\n"
operator|+
literal|"necessary."
argument_list|)
block|,
name|TEZ_LLAP_MIN_REDUCER_PER_EXECUTOR
argument_list|(
literal|"hive.tez.llap.min.reducer.per.executor"
argument_list|,
literal|0.33f
argument_list|,
literal|"If above 0, the min number of reducers for auto-parallelism for LLAP scheduling will\n"
operator|+
literal|"be set to this fraction of the number of executors."
argument_list|)
block|,
name|TEZ_MAX_PARTITION_FACTOR
argument_list|(
literal|"hive.tez.max.partition.factor"
argument_list|,
literal|2f
argument_list|,
literal|"When auto reducer parallelism is enabled this factor will be used to over-partition data in shuffle edges."
argument_list|)
block|,
name|TEZ_MIN_PARTITION_FACTOR
argument_list|(
literal|"hive.tez.min.partition.factor"
argument_list|,
literal|0.25f
argument_list|,
literal|"When auto reducer parallelism is enabled this factor will be used to put a lower limit to the number\n"
operator|+
literal|"of reducers that tez specifies."
argument_list|)
block|,
name|TEZ_OPTIMIZE_BUCKET_PRUNING
argument_list|(
literal|"hive.tez.bucket.pruning"
argument_list|,
literal|true
argument_list|,
literal|"When pruning is enabled, filters on bucket columns will be processed by \n"
operator|+
literal|"filtering the splits against a bitset of included buckets. This needs predicates \n"
operator|+
literal|"produced by hive.optimize.ppd and hive.optimize.index.filters."
argument_list|)
block|,
name|TEZ_OPTIMIZE_BUCKET_PRUNING_COMPAT
argument_list|(
literal|"hive.tez.bucket.pruning.compat"
argument_list|,
literal|true
argument_list|,
literal|"When pruning is enabled, handle possibly broken inserts due to negative hashcodes.\n"
operator|+
literal|"This occasionally doubles the data scan cost, but is default enabled for safety"
argument_list|)
block|,
name|TEZ_DYNAMIC_PARTITION_PRUNING
argument_list|(
literal|"hive.tez.dynamic.partition.pruning"
argument_list|,
literal|true
argument_list|,
literal|"When dynamic pruning is enabled, joins on partition keys will be processed by sending\n"
operator|+
literal|"events from the processing vertices to the Tez application master. These events will be\n"
operator|+
literal|"used to prune unnecessary partitions."
argument_list|)
block|,
name|TEZ_DYNAMIC_PARTITION_PRUNING_EXTENDED
argument_list|(
literal|"hive.tez.dynamic.partition.pruning.extended"
argument_list|,
literal|true
argument_list|,
literal|"Whether we should try to create additional opportunities for dynamic pruning, e.g., considering\n"
operator|+
literal|"siblings that may not be created by normal dynamic pruning logic.\n"
operator|+
literal|"Only works when dynamic pruning is enabled."
argument_list|)
block|,
name|TEZ_DYNAMIC_PARTITION_PRUNING_MAX_EVENT_SIZE
argument_list|(
literal|"hive.tez.dynamic.partition.pruning.max.event.size"
argument_list|,
literal|1
operator|*
literal|1024
operator|*
literal|1024L
argument_list|,
literal|"Maximum size of events sent by processors in dynamic pruning. If this size is crossed no pruning will take place."
argument_list|)
block|,
name|TEZ_DYNAMIC_PARTITION_PRUNING_MAX_DATA_SIZE
argument_list|(
literal|"hive.tez.dynamic.partition.pruning.max.data.size"
argument_list|,
literal|100
operator|*
literal|1024
operator|*
literal|1024L
argument_list|,
literal|"Maximum total data size of events in dynamic pruning."
argument_list|)
block|,
name|TEZ_DYNAMIC_SEMIJOIN_REDUCTION
argument_list|(
literal|"hive.tez.dynamic.semijoin.reduction"
argument_list|,
literal|true
argument_list|,
literal|"When dynamic semijoin is enabled, shuffle joins will perform a leaky semijoin before shuffle. This "
operator|+
literal|"requires hive.tez.dynamic.partition.pruning to be enabled."
argument_list|)
block|,
name|TEZ_MIN_BLOOM_FILTER_ENTRIES
argument_list|(
literal|"hive.tez.min.bloom.filter.entries"
argument_list|,
literal|1000000L
argument_list|,
literal|"Bloom filter should be of at min certain size to be effective"
argument_list|)
block|,
name|TEZ_MAX_BLOOM_FILTER_ENTRIES
argument_list|(
literal|"hive.tez.max.bloom.filter.entries"
argument_list|,
literal|100000000L
argument_list|,
literal|"Bloom filter should be of at max certain size to be effective"
argument_list|)
block|,
name|TEZ_BLOOM_FILTER_FACTOR
argument_list|(
literal|"hive.tez.bloom.filter.factor"
argument_list|,
operator|(
name|float
operator|)
literal|1.0
argument_list|,
literal|"Bloom filter should be a multiple of this factor with nDV"
argument_list|)
block|,
name|TEZ_BIGTABLE_MIN_SIZE_SEMIJOIN_REDUCTION
argument_list|(
literal|"hive.tez.bigtable.minsize.semijoin.reduction"
argument_list|,
literal|100000000L
argument_list|,
literal|"Big table for runtime filteting should be of atleast this size"
argument_list|)
block|,
name|TEZ_DYNAMIC_SEMIJOIN_REDUCTION_THRESHOLD
argument_list|(
literal|"hive.tez.dynamic.semijoin.reduction.threshold"
argument_list|,
operator|(
name|float
operator|)
literal|0.50
argument_list|,
literal|"Only perform semijoin optimization if the estimated benefit at or above this fraction of the target table"
argument_list|)
block|,
name|TEZ_DYNAMIC_SEMIJOIN_REDUCTION_FOR_MAPJOIN
argument_list|(
literal|"hive.tez.dynamic.semijoin.reduction.for.mapjoin"
argument_list|,
literal|false
argument_list|,
literal|"Use a semi-join branch for map-joins. This may not make it faster, but is helpful in certain join patterns."
argument_list|)
block|,
name|TEZ_DYNAMIC_SEMIJOIN_REDUCTION_FOR_DPP_FACTOR
argument_list|(
literal|"hive.tez.dynamic.semijoin.reduction.for.dpp.factor"
argument_list|,
operator|(
name|float
operator|)
literal|1.0
argument_list|,
literal|"The factor to decide if semijoin branch feeds into a TableScan\n"
operator|+
literal|"which has an outgoing Dynamic Partition Pruning (DPP) branch based on number of distinct values."
argument_list|)
block|,
name|TEZ_SMB_NUMBER_WAVES
argument_list|(
literal|"hive.tez.smb.number.waves"
argument_list|,
operator|(
name|float
operator|)
literal|0.5
argument_list|,
literal|"The number of waves in which to run the SMB join. Account for cluster being occupied. Ideally should be 1 wave."
argument_list|)
block|,
name|TEZ_EXEC_SUMMARY
argument_list|(
literal|"hive.tez.exec.print.summary"
argument_list|,
literal|false
argument_list|,
literal|"Display breakdown of execution steps, for every query executed by the shell."
argument_list|)
block|,
name|TEZ_SESSION_EVENTS_SUMMARY
argument_list|(
literal|"hive.tez.session.events.print.summary"
argument_list|,
literal|"none"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"none"
argument_list|,
literal|"text"
argument_list|,
literal|"json"
argument_list|)
argument_list|,
literal|"Display summary of all tez sessions related events in text or json format"
argument_list|)
block|,
name|TEZ_EXEC_INPLACE_PROGRESS
argument_list|(
literal|"hive.tez.exec.inplace.progress"
argument_list|,
literal|true
argument_list|,
literal|"Updates tez job execution progress in-place in the terminal when hive-cli is used."
argument_list|)
block|,
name|HIVE_SERVER2_INPLACE_PROGRESS
argument_list|(
literal|"hive.server2.in.place.progress"
argument_list|,
literal|true
argument_list|,
literal|"Allows hive server 2 to send progress bar update information. This is currently available"
operator|+
literal|" only if the execution engine is tez or Spark."
argument_list|)
block|,
name|TEZ_DAG_STATUS_CHECK_INTERVAL
argument_list|(
literal|"hive.tez.dag.status.check.interval"
argument_list|,
literal|"500ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Interval between subsequent DAG status invocation."
argument_list|)
block|,
name|SPARK_EXEC_INPLACE_PROGRESS
argument_list|(
literal|"hive.spark.exec.inplace.progress"
argument_list|,
literal|true
argument_list|,
literal|"Updates spark job execution progress in-place in the terminal."
argument_list|)
block|,
name|TEZ_CONTAINER_MAX_JAVA_HEAP_FRACTION
argument_list|(
literal|"hive.tez.container.max.java.heap.fraction"
argument_list|,
literal|0.8f
argument_list|,
literal|"This is to override the tez setting with the same name"
argument_list|)
block|,
name|TEZ_TASK_SCALE_MEMORY_RESERVE_FRACTION_MIN
argument_list|(
literal|"hive.tez.task.scale.memory.reserve-fraction.min"
argument_list|,
literal|0.3f
argument_list|,
literal|"This is to override the tez setting tez.task.scale.memory.reserve-fraction"
argument_list|)
block|,
name|TEZ_TASK_SCALE_MEMORY_RESERVE_FRACTION_MAX
argument_list|(
literal|"hive.tez.task.scale.memory.reserve.fraction.max"
argument_list|,
literal|0.5f
argument_list|,
literal|"The maximum fraction of JVM memory which Tez will reserve for the processor"
argument_list|)
block|,
name|TEZ_TASK_SCALE_MEMORY_RESERVE_FRACTION
argument_list|(
literal|"hive.tez.task.scale.memory.reserve.fraction"
argument_list|,
operator|-
literal|1f
argument_list|,
literal|"The customized fraction of JVM memory which Tez will reserve for the processor"
argument_list|)
block|,
name|TEZ_CARTESIAN_PRODUCT_EDGE_ENABLED
argument_list|(
literal|"hive.tez.cartesian-product.enabled"
argument_list|,
literal|false
argument_list|,
literal|"Use Tez cartesian product edge to speed up cross product"
argument_list|)
block|,
name|TEZ_SIMPLE_CUSTOM_EDGE_TINY_BUFFER_SIZE_MB
argument_list|(
literal|"hive.tez.unordered.output.buffer.size.mb"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"When we have an operation that does not need a large buffer, we use this buffer size for simple custom edge.\n"
operator|+
literal|"Value is an integer. Default value is -1, which means that we will estimate this value from operators in the plan."
argument_list|)
block|,
comment|// The default is different on the client and server, so it's null here.
name|LLAP_IO_ENABLED
argument_list|(
literal|"hive.llap.io.enabled"
argument_list|,
literal|null
argument_list|,
literal|"Whether the LLAP IO layer is enabled."
argument_list|)
block|,
name|LLAP_IO_CACHE_ONLY
argument_list|(
literal|"hive.llap.io.cache.only"
argument_list|,
literal|false
argument_list|,
literal|"Whether the query should read from cache only. If set to "
operator|+
literal|"true and a cache miss happens during the read an exception will occur. Primarily used for testing."
argument_list|)
block|,
name|LLAP_IO_ROW_WRAPPER_ENABLED
argument_list|(
literal|"hive.llap.io.row.wrapper.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Whether the LLAP IO row wrapper is enabled for non-vectorized queries."
argument_list|)
block|,
name|LLAP_IO_ACID_ENABLED
argument_list|(
literal|"hive.llap.io.acid"
argument_list|,
literal|true
argument_list|,
literal|"Whether the LLAP IO layer is enabled for ACID."
argument_list|)
block|,
name|LLAP_IO_TRACE_SIZE
argument_list|(
literal|"hive.llap.io.trace.size"
argument_list|,
literal|"2Mb"
argument_list|,
operator|new
name|SizeValidator
argument_list|(
literal|0L
argument_list|,
literal|true
argument_list|,
operator|(
name|long
operator|)
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|false
argument_list|)
argument_list|,
literal|"The buffer size for a per-fragment LLAP debug trace. 0 to disable."
argument_list|)
block|,
name|LLAP_IO_TRACE_ALWAYS_DUMP
argument_list|(
literal|"hive.llap.io.trace.always.dump"
argument_list|,
literal|false
argument_list|,
literal|"Whether to always dump the LLAP IO trace (if enabled); the default is on error."
argument_list|)
block|,
name|LLAP_IO_NONVECTOR_WRAPPER_ENABLED
argument_list|(
literal|"hive.llap.io.nonvector.wrapper.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Whether the LLAP IO layer is enabled for non-vectorized queries that read inputs\n"
operator|+
literal|"that can be vectorized"
argument_list|)
block|,
name|LLAP_IO_MEMORY_MODE
argument_list|(
literal|"hive.llap.io.memory.mode"
argument_list|,
literal|"cache"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"cache"
argument_list|,
literal|"none"
argument_list|)
argument_list|,
literal|"LLAP IO memory usage; 'cache' (the default) uses data and metadata cache with a\n"
operator|+
literal|"custom off-heap allocator, 'none' doesn't use either (this mode may result in\n"
operator|+
literal|"significant performance degradation)"
argument_list|)
block|,
name|LLAP_ALLOCATOR_MIN_ALLOC
argument_list|(
literal|"hive.llap.io.allocator.alloc.min"
argument_list|,
literal|"4Kb"
argument_list|,
operator|new
name|SizeValidator
argument_list|()
argument_list|,
literal|"Minimum allocation possible from LLAP buddy allocator. Allocations below that are\n"
operator|+
literal|"padded to minimum allocation. For ORC, should generally be the same as the expected\n"
operator|+
literal|"compression buffer size, or next lowest power of 2. Must be a power of 2."
argument_list|)
block|,
name|LLAP_ALLOCATOR_MAX_ALLOC
argument_list|(
literal|"hive.llap.io.allocator.alloc.max"
argument_list|,
literal|"16Mb"
argument_list|,
operator|new
name|SizeValidator
argument_list|()
argument_list|,
literal|"Maximum allocation possible from LLAP buddy allocator. For ORC, should be as large as\n"
operator|+
literal|"the largest expected ORC compression buffer size. Must be a power of 2."
argument_list|)
block|,
name|LLAP_ALLOCATOR_ARENA_COUNT
argument_list|(
literal|"hive.llap.io.allocator.arena.count"
argument_list|,
literal|8
argument_list|,
literal|"Arena count for LLAP low-level cache; cache will be allocated in the steps of\n"
operator|+
literal|"(size/arena_count) bytes. This size must be<= 1Gb and>= max allocation; if it is\n"
operator|+
literal|"not the case, an adjusted size will be used. Using powers of 2 is recommended."
argument_list|)
block|,
name|LLAP_IO_MEMORY_MAX_SIZE
argument_list|(
literal|"hive.llap.io.memory.size"
argument_list|,
literal|"1Gb"
argument_list|,
operator|new
name|SizeValidator
argument_list|()
argument_list|,
literal|"Maximum size for IO allocator or ORC low-level cache."
argument_list|,
literal|"hive.llap.io.cache.orc.size"
argument_list|)
block|,
name|LLAP_ALLOCATOR_DIRECT
argument_list|(
literal|"hive.llap.io.allocator.direct"
argument_list|,
literal|true
argument_list|,
literal|"Whether ORC low-level cache should use direct allocation."
argument_list|)
block|,
name|LLAP_ALLOCATOR_PREALLOCATE
argument_list|(
literal|"hive.llap.io.allocator.preallocate"
argument_list|,
literal|true
argument_list|,
literal|"Whether to preallocate the entire IO memory at init time."
argument_list|)
block|,
name|LLAP_ALLOCATOR_MAPPED
argument_list|(
literal|"hive.llap.io.allocator.mmap"
argument_list|,
literal|false
argument_list|,
literal|"Whether ORC low-level cache should use memory mapped allocation (direct I/O). \n"
operator|+
literal|"This is recommended to be used along-side NVDIMM (DAX) or NVMe flash storage."
argument_list|)
block|,
name|LLAP_ALLOCATOR_MAPPED_PATH
argument_list|(
literal|"hive.llap.io.allocator.mmap.path"
argument_list|,
literal|"/tmp"
argument_list|,
operator|new
name|WritableDirectoryValidator
argument_list|()
argument_list|,
literal|"The directory location for mapping NVDIMM/NVMe flash storage into the ORC low-level cache."
argument_list|)
block|,
name|LLAP_ALLOCATOR_DISCARD_METHOD
argument_list|(
literal|"hive.llap.io.allocator.discard.method"
argument_list|,
literal|"both"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"freelist"
argument_list|,
literal|"brute"
argument_list|,
literal|"both"
argument_list|)
argument_list|,
literal|"Which method to use to force-evict blocks to deal with fragmentation:\n"
operator|+
literal|"freelist - use half-size free list (discards less, but also less reliable); brute -\n"
operator|+
literal|"brute force, discard whatever we can; both - first try free list, then brute force."
argument_list|)
block|,
name|LLAP_ALLOCATOR_DEFRAG_HEADROOM
argument_list|(
literal|"hive.llap.io.allocator.defrag.headroom"
argument_list|,
literal|"1Mb"
argument_list|,
literal|"How much of a headroom to leave to allow allocator more flexibility to defragment.\n"
operator|+
literal|"The allocator would further cap it to a fraction of total memory."
argument_list|)
block|,
name|LLAP_ALLOCATOR_MAX_FORCE_EVICTED
argument_list|(
literal|"hive.llap.io.allocator.max.force.eviction"
argument_list|,
literal|"16Mb"
argument_list|,
literal|"Fragmentation can lead to some cases where more eviction has to happen to accommodate allocations\n"
operator|+
literal|" This configuration puts a limit on how many bytes to force evict before using Allocator Discard method."
operator|+
literal|" Higher values will allow allocator more flexibility and will lead to better caching."
argument_list|)
block|,
name|LLAP_TRACK_CACHE_USAGE
argument_list|(
literal|"hive.llap.io.track.cache.usage"
argument_list|,
literal|true
argument_list|,
literal|"Whether to tag LLAP cache contents, mapping them to Hive entities (paths for\n"
operator|+
literal|"partitions and tables) for reporting."
argument_list|)
block|,
name|LLAP_USE_LRFU
argument_list|(
literal|"hive.llap.io.use.lrfu"
argument_list|,
literal|true
argument_list|,
literal|"Whether ORC low-level cache should use LRFU cache policy instead of default (FIFO)."
argument_list|)
block|,
name|LLAP_LRFU_LAMBDA
argument_list|(
literal|"hive.llap.io.lrfu.lambda"
argument_list|,
literal|0.1f
argument_list|,
literal|"Lambda for ORC low-level cache LRFU cache policy. Must be in [0, 1]. 0 makes LRFU\n"
operator|+
literal|"behave like LFU, 1 makes it behave like LRU, values in between balance accordingly.\n"
operator|+
literal|"The meaning of this parameter is the inverse of the number of time ticks (cache\n"
operator|+
literal|" operations, currently) that cause the combined recency-frequency of a block in cache\n"
operator|+
literal|" to be halved."
argument_list|)
block|,
name|LLAP_LRFU_BP_WRAPPER_SIZE
argument_list|(
literal|"hive.llap.io.lrfu.bp.wrapper.size"
argument_list|,
literal|64
argument_list|,
literal|"thread local queue "
operator|+
literal|"used to amortize the lock contention, the idea hear is to try locking as soon we reach max size / 2 "
operator|+
literal|"and block when max queue size reached"
argument_list|)
block|,
name|LLAP_CACHE_ALLOW_SYNTHETIC_FILEID
argument_list|(
literal|"hive.llap.cache.allow.synthetic.fileid"
argument_list|,
literal|true
argument_list|,
literal|"Whether LLAP cache should use synthetic file ID if real one is not available. Systems\n"
operator|+
literal|"like HDFS, Isilon, etc. provide a unique file/inode ID. On other FSes (e.g. local\n"
operator|+
literal|"FS), the cache would not work by default because LLAP is unable to uniquely track the\n"
operator|+
literal|"files; enabling this setting allows LLAP to generate file ID from the path, size and\n"
operator|+
literal|"modification time, which is almost certain to identify file uniquely. However, if you\n"
operator|+
literal|"use a FS without file IDs and rewrite files a lot (or are paranoid), you might want\n"
operator|+
literal|"to avoid this setting."
argument_list|)
block|,
name|LLAP_CACHE_DEFAULT_FS_FILE_ID
argument_list|(
literal|"hive.llap.cache.defaultfs.only.native.fileid"
argument_list|,
literal|true
argument_list|,
literal|"Whether LLAP cache should use native file IDs from the default FS only. This is to\n"
operator|+
literal|"avoid file ID collisions when several different DFS instances are in use at the same\n"
operator|+
literal|"time. Disable this check to allow native file IDs from non-default DFS."
argument_list|)
block|,
name|LLAP_CACHE_ENABLE_ORC_GAP_CACHE
argument_list|(
literal|"hive.llap.orc.gap.cache"
argument_list|,
literal|true
argument_list|,
literal|"Whether LLAP cache for ORC should remember gaps in ORC compression buffer read\n"
operator|+
literal|"estimates, to avoid re-reading the data that was read once and discarded because it\n"
operator|+
literal|"is unneeded. This is only necessary for ORC files written before HIVE-9660."
argument_list|)
block|,
name|LLAP_IO_USE_FILEID_PATH
argument_list|(
literal|"hive.llap.io.use.fileid.path"
argument_list|,
literal|true
argument_list|,
literal|"Whether LLAP should use fileId (inode)-based path to ensure better consistency for the\n"
operator|+
literal|"cases of file overwrites. This is supported on HDFS. Disabling this also turns off any\n"
operator|+
literal|"cache consistency checks based on fileid comparisons."
argument_list|)
block|,
comment|// Restricted to text for now as this is a new feature; only text files can be sliced.
name|LLAP_IO_ENCODE_ENABLED
argument_list|(
literal|"hive.llap.io.encode.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Whether LLAP should try to re-encode and cache data for non-ORC formats. This is used\n"
operator|+
literal|"on LLAP Server side to determine if the infrastructure for that is initialized."
argument_list|)
block|,
name|LLAP_IO_ENCODE_FORMATS
argument_list|(
literal|"hive.llap.io.encode.formats"
argument_list|,
literal|"org.apache.hadoop.mapred.TextInputFormat,"
argument_list|,
literal|"The table input formats for which LLAP IO should re-encode and cache data.\n"
operator|+
literal|"Comma-separated list."
argument_list|)
block|,
name|LLAP_IO_ENCODE_ALLOC_SIZE
argument_list|(
literal|"hive.llap.io.encode.alloc.size"
argument_list|,
literal|"256Kb"
argument_list|,
operator|new
name|SizeValidator
argument_list|()
argument_list|,
literal|"Allocation size for the buffers used to cache encoded data from non-ORC files. Must\n"
operator|+
literal|"be a power of two between "
operator|+
name|LLAP_ALLOCATOR_MIN_ALLOC
operator|+
literal|" and\n"
operator|+
name|LLAP_ALLOCATOR_MAX_ALLOC
operator|+
literal|"."
argument_list|)
block|,
name|LLAP_IO_ENCODE_VECTOR_SERDE_ENABLED
argument_list|(
literal|"hive.llap.io.encode.vector.serde.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Whether LLAP should use vectorized SerDe reader to read text data when re-encoding."
argument_list|)
block|,
name|LLAP_IO_ENCODE_VECTOR_SERDE_ASYNC_ENABLED
argument_list|(
literal|"hive.llap.io.encode.vector.serde.async.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Whether LLAP should use async mode in vectorized SerDe reader to read text data."
argument_list|)
block|,
name|LLAP_IO_ENCODE_SLICE_ROW_COUNT
argument_list|(
literal|"hive.llap.io.encode.slice.row.count"
argument_list|,
literal|100000
argument_list|,
literal|"Row count to use to separate cache slices when reading encoded data from row-based\n"
operator|+
literal|"inputs into LLAP cache, if this feature is enabled."
argument_list|)
block|,
name|LLAP_IO_ENCODE_SLICE_LRR
argument_list|(
literal|"hive.llap.io.encode.slice.lrr"
argument_list|,
literal|true
argument_list|,
literal|"Whether to separate cache slices when reading encoded data from text inputs via MR\n"
operator|+
literal|"MR LineRecordRedader into LLAP cache, if this feature is enabled. Safety flag."
argument_list|)
block|,
name|LLAP_ORC_ENABLE_TIME_COUNTERS
argument_list|(
literal|"hive.llap.io.orc.time.counters"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable time counters for LLAP IO layer (time spent in HDFS, etc.)"
argument_list|)
block|,
name|LLAP_IO_VRB_QUEUE_LIMIT_MAX
argument_list|(
literal|"hive.llap.io.vrb.queue.limit.max"
argument_list|,
literal|50000
argument_list|,
literal|"The maximum queue size for VRBs produced by a LLAP IO thread when the processing is\n"
operator|+
literal|"slower than the IO. The actual queue size is set per fragment, and is adjusted down\n"
operator|+
literal|"from the base, depending on the schema see LLAP_IO_CVB_BUFFERED_SIZE."
argument_list|)
block|,
name|LLAP_IO_VRB_QUEUE_LIMIT_MIN
argument_list|(
literal|"hive.llap.io.vrb.queue.limit.min"
argument_list|,
literal|1
argument_list|,
literal|"The minimum queue size for VRBs produced by a LLAP IO thread when the processing is\n"
operator|+
literal|"slower than the IO (used when determining the size from base size)."
argument_list|)
block|,
name|LLAP_IO_CVB_BUFFERED_SIZE
argument_list|(
literal|"hive.llap.io.cvb.memory.consumption."
argument_list|,
literal|1L
operator|<<
literal|30
argument_list|,
literal|"The amount of bytes used to buffer CVB between IO and Processor Threads default to 1GB, "
operator|+
literal|"this will be used to compute a best effort queue size for VRBs produced by a LLAP IO thread."
argument_list|)
block|,
name|LLAP_IO_SHARE_OBJECT_POOLS
argument_list|(
literal|"hive.llap.io.share.object.pools"
argument_list|,
literal|false
argument_list|,
literal|"Whether to used shared object pools in LLAP IO. A safety flag."
argument_list|)
block|,
name|LLAP_AUTO_ALLOW_UBER
argument_list|(
literal|"hive.llap.auto.allow.uber"
argument_list|,
literal|false
argument_list|,
literal|"Whether or not to allow the planner to run vertices in the AM."
argument_list|)
block|,
name|LLAP_AUTO_ENFORCE_TREE
argument_list|(
literal|"hive.llap.auto.enforce.tree"
argument_list|,
literal|true
argument_list|,
literal|"Enforce that all parents are in llap, before considering vertex"
argument_list|)
block|,
name|LLAP_AUTO_ENFORCE_VECTORIZED
argument_list|(
literal|"hive.llap.auto.enforce.vectorized"
argument_list|,
literal|true
argument_list|,
literal|"Enforce that inputs are vectorized, before considering vertex"
argument_list|)
block|,
name|LLAP_AUTO_ENFORCE_STATS
argument_list|(
literal|"hive.llap.auto.enforce.stats"
argument_list|,
literal|true
argument_list|,
literal|"Enforce that col stats are available, before considering vertex"
argument_list|)
block|,
name|LLAP_AUTO_MAX_INPUT
argument_list|(
literal|"hive.llap.auto.max.input.size"
argument_list|,
literal|10
operator|*
literal|1024
operator|*
literal|1024
operator|*
literal|1024L
argument_list|,
literal|"Check input size, before considering vertex (-1 disables check)"
argument_list|)
block|,
name|LLAP_AUTO_MAX_OUTPUT
argument_list|(
literal|"hive.llap.auto.max.output.size"
argument_list|,
literal|1
operator|*
literal|1024
operator|*
literal|1024
operator|*
literal|1024L
argument_list|,
literal|"Check output size, before considering vertex (-1 disables check)"
argument_list|)
block|,
name|LLAP_SKIP_COMPILE_UDF_CHECK
argument_list|(
literal|"hive.llap.skip.compile.udf.check"
argument_list|,
literal|false
argument_list|,
literal|"Whether to skip the compile-time check for non-built-in UDFs when deciding whether to\n"
operator|+
literal|"execute tasks in LLAP. Skipping the check allows executing UDFs from pre-localized\n"
operator|+
literal|"jars in LLAP; if the jars are not pre-localized, the UDFs will simply fail to load."
argument_list|)
block|,
name|LLAP_ALLOW_PERMANENT_FNS
argument_list|(
literal|"hive.llap.allow.permanent.fns"
argument_list|,
literal|true
argument_list|,
literal|"Whether LLAP decider should allow permanent UDFs."
argument_list|)
block|,
name|LLAP_EXECUTION_MODE
argument_list|(
literal|"hive.llap.execution.mode"
argument_list|,
literal|"none"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"auto"
argument_list|,
literal|"none"
argument_list|,
literal|"all"
argument_list|,
literal|"map"
argument_list|,
literal|"only"
argument_list|)
argument_list|,
literal|"Chooses whether query fragments will run in container or in llap"
argument_list|)
block|,
name|LLAP_IO_ETL_SKIP_FORMAT
argument_list|(
literal|"hive.llap.io.etl.skip.format"
argument_list|,
literal|"encode"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"none"
argument_list|,
literal|"encode"
argument_list|,
literal|"all"
argument_list|)
argument_list|,
literal|"For ETL queries, determines whether to skip llap io cache. By default, hive.llap.io.encode.enabled "
operator|+
literal|"will be set to false which disables LLAP IO for text formats. Setting it to 'all' will disable LLAP IO for all"
operator|+
literal|" formats. 'none' will not disable LLAP IO for any formats."
argument_list|)
block|,
name|LLAP_OBJECT_CACHE_ENABLED
argument_list|(
literal|"hive.llap.object.cache.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Cache objects (plans, hashtables, etc) in llap"
argument_list|)
block|,
name|LLAP_IO_DECODING_METRICS_PERCENTILE_INTERVALS
argument_list|(
literal|"hive.llap.io.decoding.metrics.percentiles.intervals"
argument_list|,
literal|"30"
argument_list|,
literal|"Comma-delimited set of integers denoting the desired rollover intervals (in seconds)\n"
operator|+
literal|"for percentile latency metrics on the LLAP daemon IO decoding time.\n"
operator|+
literal|"hive.llap.queue.metrics.percentiles.intervals"
argument_list|)
block|,
name|LLAP_IO_THREADPOOL_SIZE
argument_list|(
literal|"hive.llap.io.threadpool.size"
argument_list|,
literal|10
argument_list|,
literal|"Specify the number of threads to use for low-level IO thread pool."
argument_list|)
block|,
name|LLAP_USE_KERBEROS
argument_list|(
literal|"hive.llap.kerberos.enabled"
argument_list|,
literal|true
argument_list|,
literal|"If LLAP is configured for Kerberos authentication. This could be useful when cluster\n"
operator|+
literal|"is kerberized, but LLAP is not."
argument_list|)
block|,
name|LLAP_KERBEROS_PRINCIPAL
argument_list|(
name|HIVE_LLAP_DAEMON_SERVICE_PRINCIPAL_NAME
argument_list|,
literal|""
argument_list|,
literal|"The name of the LLAP daemon's service principal."
argument_list|)
block|,
name|LLAP_KERBEROS_KEYTAB_FILE
argument_list|(
literal|"hive.llap.daemon.keytab.file"
argument_list|,
literal|""
argument_list|,
literal|"The path to the Kerberos Keytab file containing the LLAP daemon's service principal."
argument_list|)
block|,
name|LLAP_WEBUI_SPNEGO_KEYTAB_FILE
argument_list|(
literal|"hive.llap.webui.spnego.keytab"
argument_list|,
literal|""
argument_list|,
literal|"The path to the Kerberos Keytab file containing the LLAP WebUI SPNEGO principal.\n"
operator|+
literal|"Typical value would look like /etc/security/keytabs/spnego.service.keytab."
argument_list|)
block|,
name|LLAP_WEBUI_SPNEGO_PRINCIPAL
argument_list|(
literal|"hive.llap.webui.spnego.principal"
argument_list|,
literal|""
argument_list|,
literal|"The LLAP WebUI SPNEGO service principal. Configured similarly to\n"
operator|+
literal|"hive.server2.webui.spnego.principal"
argument_list|)
block|,
name|LLAP_FS_KERBEROS_PRINCIPAL
argument_list|(
literal|"hive.llap.task.principal"
argument_list|,
literal|""
argument_list|,
literal|"The name of the principal to use to run tasks. By default, the clients are required\n"
operator|+
literal|"to provide tokens to access HDFS/etc."
argument_list|)
block|,
name|LLAP_FS_KERBEROS_KEYTAB_FILE
argument_list|(
literal|"hive.llap.task.keytab.file"
argument_list|,
literal|""
argument_list|,
literal|"The path to the Kerberos Keytab file containing the principal to use to run tasks.\n"
operator|+
literal|"By default, the clients are required to provide tokens to access HDFS/etc."
argument_list|)
block|,
name|LLAP_ZKSM_ZK_CONNECTION_STRING
argument_list|(
literal|"hive.llap.zk.sm.connectionString"
argument_list|,
literal|""
argument_list|,
literal|"ZooKeeper connection string for ZooKeeper SecretManager."
argument_list|)
block|,
name|LLAP_ZKSM_ZK_SESSION_TIMEOUT
argument_list|(
literal|"hive.llap.zk.sm.session.timeout"
argument_list|,
literal|"40s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"ZooKeeper session timeout for ZK SecretManager."
argument_list|)
block|,
name|LLAP_ZK_REGISTRY_USER
argument_list|(
literal|"hive.llap.zk.registry.user"
argument_list|,
literal|""
argument_list|,
literal|"In the LLAP ZooKeeper-based registry, specifies the username in the Zookeeper path.\n"
operator|+
literal|"This should be the hive user or whichever user is running the LLAP daemon."
argument_list|)
block|,
name|LLAP_ZK_REGISTRY_NAMESPACE
argument_list|(
literal|"hive.llap.zk.registry.namespace"
argument_list|,
literal|null
argument_list|,
literal|"In the LLAP ZooKeeper-based registry, overrides the ZK path namespace. Note that\n"
operator|+
literal|"using this makes the path management (e.g. setting correct ACLs) your responsibility."
argument_list|)
block|,
comment|// Note: do not rename to ..service.acl; Hadoop generates .hosts setting name from this,
comment|// resulting in a collision with existing hive.llap.daemon.service.hosts and bizarre errors.
comment|// These are read by Hadoop IPC, so you should check the usage and naming conventions (e.g.
comment|// ".blocked" is a string hardcoded by Hadoop, and defaults are enforced elsewhere in Hive)
comment|// before making changes or copy-pasting these.
name|LLAP_SECURITY_ACL
argument_list|(
literal|"hive.llap.daemon.acl"
argument_list|,
literal|"*"
argument_list|,
literal|"The ACL for LLAP daemon."
argument_list|)
block|,
name|LLAP_SECURITY_ACL_DENY
argument_list|(
literal|"hive.llap.daemon.acl.blocked"
argument_list|,
literal|""
argument_list|,
literal|"The deny ACL for LLAP daemon."
argument_list|)
block|,
name|LLAP_MANAGEMENT_ACL
argument_list|(
literal|"hive.llap.management.acl"
argument_list|,
literal|"*"
argument_list|,
literal|"The ACL for LLAP daemon management."
argument_list|)
block|,
name|LLAP_MANAGEMENT_ACL_DENY
argument_list|(
literal|"hive.llap.management.acl.blocked"
argument_list|,
literal|""
argument_list|,
literal|"The deny ACL for LLAP daemon management."
argument_list|)
block|,
name|LLAP_PLUGIN_ACL
argument_list|(
literal|"hive.llap.plugin.acl"
argument_list|,
literal|"*"
argument_list|,
literal|"The ACL for LLAP plugin AM endpoint."
argument_list|)
block|,
name|LLAP_PLUGIN_ACL_DENY
argument_list|(
literal|"hive.llap.plugin.acl.blocked"
argument_list|,
literal|""
argument_list|,
literal|"The deny ACL for LLAP plugin AM endpoint."
argument_list|)
block|,
name|LLAP_REMOTE_TOKEN_REQUIRES_SIGNING
argument_list|(
literal|"hive.llap.remote.token.requires.signing"
argument_list|,
literal|"true"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"false"
argument_list|,
literal|"except_llap_owner"
argument_list|,
literal|"true"
argument_list|)
argument_list|,
literal|"Whether the token returned from LLAP management API should require fragment signing.\n"
operator|+
literal|"True by default; can be disabled to allow CLI to get tokens from LLAP in a secure\n"
operator|+
literal|"cluster by setting it to true or 'except_llap_owner' (the latter returns such tokens\n"
operator|+
literal|"to everyone except the user LLAP cluster is authenticating under)."
argument_list|)
block|,
comment|// Hadoop DelegationTokenManager default is 1 week.
name|LLAP_DELEGATION_TOKEN_LIFETIME
argument_list|(
literal|"hive.llap.daemon.delegation.token.lifetime"
argument_list|,
literal|"14d"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"LLAP delegation token lifetime, in seconds if specified without a unit."
argument_list|)
block|,
name|LLAP_MANAGEMENT_RPC_PORT
argument_list|(
literal|"hive.llap.management.rpc.port"
argument_list|,
literal|15004
argument_list|,
literal|"RPC port for LLAP daemon management service."
argument_list|)
block|,
name|LLAP_WEB_AUTO_AUTH
argument_list|(
literal|"hive.llap.auto.auth"
argument_list|,
literal|false
argument_list|,
literal|"Whether or not to set Hadoop configs to enable auth in LLAP web app."
argument_list|)
block|,
name|LLAP_DAEMON_RPC_NUM_HANDLERS
argument_list|(
literal|"hive.llap.daemon.rpc.num.handlers"
argument_list|,
literal|5
argument_list|,
literal|"Number of RPC handlers for LLAP daemon."
argument_list|,
literal|"llap.daemon.rpc.num.handlers"
argument_list|)
block|,
name|LLAP_PLUGIN_RPC_PORT
argument_list|(
literal|"hive.llap.plugin.rpc.port"
argument_list|,
literal|0
argument_list|,
literal|"Port to use for LLAP plugin rpc server"
argument_list|)
block|,
name|LLAP_PLUGIN_RPC_NUM_HANDLERS
argument_list|(
literal|"hive.llap.plugin.rpc.num.handlers"
argument_list|,
literal|1
argument_list|,
literal|"Number of RPC handlers for AM LLAP plugin endpoint."
argument_list|)
block|,
name|LLAP_HDFS_PACKAGE_DIR
argument_list|(
literal|"hive.llap.hdfs.package.dir"
argument_list|,
literal|".yarn"
argument_list|,
literal|"Package directory on HDFS used for holding collected configuration and libraries"
operator|+
literal|" required for YARN launch. Note: this should be set to the same as yarn.service.base.path"
argument_list|)
block|,
name|LLAP_DAEMON_WORK_DIRS
argument_list|(
literal|"hive.llap.daemon.work.dirs"
argument_list|,
literal|""
argument_list|,
literal|"Working directories for the daemon. This should not be set if running as a YARN\n"
operator|+
literal|"Service. It must be set when not running on YARN. If the value is set when\n"
operator|+
literal|"running as a YARN Service, the specified value will be used."
argument_list|,
literal|"llap.daemon.work.dirs"
argument_list|)
block|,
name|LLAP_DAEMON_YARN_SHUFFLE_PORT
argument_list|(
literal|"hive.llap.daemon.yarn.shuffle.port"
argument_list|,
literal|15551
argument_list|,
literal|"YARN shuffle port for LLAP-daemon-hosted shuffle."
argument_list|,
literal|"llap.daemon.yarn.shuffle.port"
argument_list|)
block|,
name|LLAP_DAEMON_YARN_CONTAINER_MB
argument_list|(
literal|"hive.llap.daemon.yarn.container.mb"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"llap server yarn container size in MB. Used in LlapServiceDriver and package.py"
argument_list|,
literal|"llap.daemon.yarn.container.mb"
argument_list|)
block|,
name|LLAP_DAEMON_QUEUE_NAME
argument_list|(
literal|"hive.llap.daemon.queue.name"
argument_list|,
literal|null
argument_list|,
literal|"Queue name within which the llap application will run."
operator|+
literal|" Used in LlapServiceDriver and package.py"
argument_list|)
block|,
comment|// TODO Move the following 2 properties out of Configuration to a constant.
name|LLAP_DAEMON_CONTAINER_ID
argument_list|(
literal|"hive.llap.daemon.container.id"
argument_list|,
literal|null
argument_list|,
literal|"ContainerId of a running LlapDaemon. Used to publish to the registry"
argument_list|)
block|,
name|LLAP_DAEMON_NM_ADDRESS
argument_list|(
literal|"hive.llap.daemon.nm.address"
argument_list|,
literal|null
argument_list|,
literal|"NM Address host:rpcPort for the NodeManager on which the instance of the daemon is running.\n"
operator|+
literal|"Published to the llap registry. Should never be set by users"
argument_list|)
block|,
name|LLAP_DAEMON_SHUFFLE_DIR_WATCHER_ENABLED
argument_list|(
literal|"hive.llap.daemon.shuffle.dir.watcher.enabled"
argument_list|,
literal|false
argument_list|,
literal|"TODO doc"
argument_list|,
literal|"llap.daemon.shuffle.dir-watcher.enabled"
argument_list|)
block|,
name|LLAP_DAEMON_AM_LIVENESS_HEARTBEAT_INTERVAL_MS
argument_list|(
literal|"hive.llap.daemon.am.liveness.heartbeat.interval.ms"
argument_list|,
literal|"10000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Tez AM-LLAP heartbeat interval (milliseconds). This needs to be below the task timeout\n"
operator|+
literal|"interval, but otherwise as high as possible to avoid unnecessary traffic."
argument_list|,
literal|"llap.daemon.am.liveness.heartbeat.interval-ms"
argument_list|)
block|,
name|LLAP_DAEMON_AM_LIVENESS_CONNECTION_TIMEOUT_MS
argument_list|(
literal|"hive.llap.am.liveness.connection.timeout.ms"
argument_list|,
literal|"10000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Amount of time to wait on connection failures to the AM from an LLAP daemon before\n"
operator|+
literal|"considering the AM to be dead."
argument_list|,
literal|"llap.am.liveness.connection.timeout-millis"
argument_list|)
block|,
name|LLAP_DAEMON_AM_USE_FQDN
argument_list|(
literal|"hive.llap.am.use.fqdn"
argument_list|,
literal|true
argument_list|,
literal|"Whether to use FQDN of the AM machine when submitting work to LLAP."
argument_list|)
block|,
name|LLAP_DAEMON_EXEC_USE_FQDN
argument_list|(
literal|"hive.llap.exec.use.fqdn"
argument_list|,
literal|true
argument_list|,
literal|"On non-kerberized clusters, where the hostnames are stable but ip address changes, setting this config\n"
operator|+
literal|" to false will use ip address of llap daemon in execution context instead of FQDN"
argument_list|)
block|,
comment|// Not used yet - since the Writable RPC engine does not support this policy.
name|LLAP_DAEMON_AM_LIVENESS_CONNECTION_SLEEP_BETWEEN_RETRIES_MS
argument_list|(
literal|"hive.llap.am.liveness.connection.sleep.between.retries.ms"
argument_list|,
literal|"2000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Sleep duration while waiting to retry connection failures to the AM from the daemon for\n"
operator|+
literal|"the general keep-alive thread (milliseconds)."
argument_list|,
literal|"llap.am.liveness.connection.sleep-between-retries-millis"
argument_list|)
block|,
name|LLAP_DAEMON_TASK_SCHEDULER_TIMEOUT_SECONDS
argument_list|(
literal|"hive.llap.task.scheduler.timeout.seconds"
argument_list|,
literal|"60s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Amount of time to wait before failing the query when there are no llap daemons running\n"
operator|+
literal|"(alive) in the cluster."
argument_list|,
literal|"llap.daemon.scheduler.timeout.seconds"
argument_list|)
block|,
name|LLAP_DAEMON_NUM_EXECUTORS
argument_list|(
literal|"hive.llap.daemon.num.executors"
argument_list|,
literal|4
argument_list|,
literal|"Number of executors to use in LLAP daemon; essentially, the number of tasks that can be\n"
operator|+
literal|"executed in parallel."
argument_list|,
literal|"llap.daemon.num.executors"
argument_list|)
block|,
name|LLAP_MAPJOIN_MEMORY_OVERSUBSCRIBE_FACTOR
argument_list|(
literal|"hive.llap.mapjoin.memory.oversubscribe.factor"
argument_list|,
literal|0.2f
argument_list|,
literal|"Fraction of memory from hive.auto.convert.join.noconditionaltask.size that can be over subscribed\n"
operator|+
literal|"by queries running in LLAP mode. This factor has to be from 0.0 to 1.0. Default is 20% over subscription.\n"
argument_list|)
block|,
name|LLAP_MEMORY_OVERSUBSCRIPTION_MAX_EXECUTORS_PER_QUERY
argument_list|(
literal|"hive.llap.memory.oversubscription.max.executors.per.query"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"Used along with hive.llap.mapjoin.memory.oversubscribe.factor to limit the number of executors from\n"
operator|+
literal|"which memory for mapjoin can be borrowed. Default 3 (from 3 other executors\n"
operator|+
literal|"hive.llap.mapjoin.memory.oversubscribe.factor amount of memory can be borrowed based on which mapjoin\n"
operator|+
literal|"conversion decision will be made). This is only an upper bound. Lower bound is determined by number of\n"
operator|+
literal|"executors and configured max concurrency."
argument_list|)
block|,
name|LLAP_MAPJOIN_MEMORY_MONITOR_CHECK_INTERVAL
argument_list|(
literal|"hive.llap.mapjoin.memory.monitor.check.interval"
argument_list|,
literal|100000L
argument_list|,
literal|"Check memory usage of mapjoin hash tables after every interval of this many rows. If map join hash table\n"
operator|+
literal|"memory usage exceeds (hive.auto.convert.join.noconditionaltask.size * hive.hash.table.inflation.factor)\n"
operator|+
literal|"when running in LLAP, tasks will get killed and not retried. Set the value to 0 to disable this feature."
argument_list|)
block|,
name|LLAP_DAEMON_AM_REPORTER_MAX_THREADS
argument_list|(
literal|"hive.llap.daemon.am-reporter.max.threads"
argument_list|,
literal|4
argument_list|,
literal|"Maximum number of threads to be used for AM reporter. If this is lower than number of\n"
operator|+
literal|"executors in llap daemon, it would be set to number of executors at runtime."
argument_list|,
literal|"llap.daemon.am-reporter.max.threads"
argument_list|)
block|,
name|LLAP_DAEMON_RPC_PORT
argument_list|(
literal|"hive.llap.daemon.rpc.port"
argument_list|,
literal|0
argument_list|,
literal|"The LLAP daemon RPC port."
argument_list|,
literal|"llap.daemon.rpc.port. A value of 0 indicates a dynamic port"
argument_list|)
block|,
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB
argument_list|(
literal|"hive.llap.daemon.memory.per.instance.mb"
argument_list|,
literal|4096
argument_list|,
literal|"The total amount of memory to use for the executors inside LLAP (in megabytes)."
argument_list|,
literal|"llap.daemon.memory.per.instance.mb"
argument_list|)
block|,
name|LLAP_DAEMON_XMX_HEADROOM
argument_list|(
literal|"hive.llap.daemon.xmx.headroom"
argument_list|,
literal|"5%"
argument_list|,
literal|"The total amount of heap memory set aside by LLAP and not used by the executors. Can\n"
operator|+
literal|"be specified as size (e.g. '512Mb'), or percentage (e.g. '5%'). Note that the latter is\n"
operator|+
literal|"derived from the total daemon XMX, which can be different from the total executor\n"
operator|+
literal|"memory if the cache is on-heap; although that's not the default configuration."
argument_list|)
block|,
name|LLAP_DAEMON_VCPUS_PER_INSTANCE
argument_list|(
literal|"hive.llap.daemon.vcpus.per.instance"
argument_list|,
literal|4
argument_list|,
literal|"The total number of vcpus to use for the executors inside LLAP."
argument_list|,
literal|"llap.daemon.vcpus.per.instance"
argument_list|)
block|,
name|LLAP_DAEMON_NUM_FILE_CLEANER_THREADS
argument_list|(
literal|"hive.llap.daemon.num.file.cleaner.threads"
argument_list|,
literal|1
argument_list|,
literal|"Number of file cleaner threads in LLAP."
argument_list|,
literal|"llap.daemon.num.file.cleaner.threads"
argument_list|)
block|,
name|LLAP_FILE_CLEANUP_DELAY_SECONDS
argument_list|(
literal|"hive.llap.file.cleanup.delay.seconds"
argument_list|,
literal|"300s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"How long to delay before cleaning up query files in LLAP (in seconds, for debugging)."
argument_list|,
literal|"llap.file.cleanup.delay-seconds"
argument_list|)
block|,
name|LLAP_DAEMON_SERVICE_HOSTS
argument_list|(
literal|"hive.llap.daemon.service.hosts"
argument_list|,
literal|null
argument_list|,
literal|"Explicitly specified hosts to use for LLAP scheduling. Useful for testing. By default,\n"
operator|+
literal|"YARN registry is used."
argument_list|,
literal|"llap.daemon.service.hosts"
argument_list|)
block|,
name|LLAP_DAEMON_SERVICE_REFRESH_INTERVAL
argument_list|(
literal|"hive.llap.daemon.service.refresh.interval.sec"
argument_list|,
literal|"60s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"LLAP YARN registry service list refresh delay, in seconds."
argument_list|,
literal|"llap.daemon.service.refresh.interval"
argument_list|)
block|,
name|LLAP_DAEMON_COMMUNICATOR_NUM_THREADS
argument_list|(
literal|"hive.llap.daemon.communicator.num.threads"
argument_list|,
literal|10
argument_list|,
literal|"Number of threads to use in LLAP task communicator in Tez AM."
argument_list|,
literal|"llap.daemon.communicator.num.threads"
argument_list|)
block|,
name|LLAP_PLUGIN_CLIENT_NUM_THREADS
argument_list|(
literal|"hive.llap.plugin.client.num.threads"
argument_list|,
literal|10
argument_list|,
literal|"Number of threads to use in LLAP task plugin client."
argument_list|)
block|,
name|LLAP_DAEMON_DOWNLOAD_PERMANENT_FNS
argument_list|(
literal|"hive.llap.daemon.download.permanent.fns"
argument_list|,
literal|false
argument_list|,
literal|"Whether LLAP daemon should localize the resources for permanent UDFs."
argument_list|)
block|,
name|LLAP_TASK_SCHEDULER_AM_COLLECT_DAEMON_METRICS_MS
argument_list|(
literal|"hive.llap.task.scheduler.am.collect.daemon.metrics.ms"
argument_list|,
literal|"0ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Collect llap daemon metrics in the AM every given milliseconds,\n"
operator|+
literal|"so that the AM can use this information, to make better scheduling decisions.\n"
operator|+
literal|"If it's set to 0, then the feature is disabled."
argument_list|)
block|,
name|LLAP_TASK_SCHEDULER_AM_COLLECT_DAEMON_METRICS_LISTENER
argument_list|(
literal|"hive.llap.task.scheduler.am.collect.daemon.metrics.listener"
argument_list|,
literal|""
argument_list|,
literal|"The listener which is called when new Llap Daemon statistics is received on AM side.\n"
operator|+
literal|"The listener should implement the "
operator|+
literal|"org.apache.hadoop.hive.llap.tezplugins.metrics.LlapMetricsListener interface."
argument_list|)
block|,
name|LLAP_NODEHEALTHCHECKS_MINTASKS
argument_list|(
literal|"hive.llap.nodehealthchecks.mintasks"
argument_list|,
literal|2000
argument_list|,
literal|"Specifies the minimum amount of tasks, executed by a particular LLAP daemon, before the health\n"
operator|+
literal|"status of the node is examined."
argument_list|)
block|,
name|LLAP_NODEHEALTHCHECKS_MININTERVALDURATION
argument_list|(
literal|"hive.llap.nodehealthckecks.minintervalduration"
argument_list|,
literal|"300s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"The minimum time that needs to elapse between two actions that are the correcting results of identifying\n"
operator|+
literal|"an unhealthy node. Even if additional nodes are considered to be unhealthy, no action is performed until\n"
operator|+
literal|"this time interval has passed since the last corrective action."
argument_list|)
block|,
name|LLAP_NODEHEALTHCHECKS_TASKTIMERATIO
argument_list|(
literal|"hive.llap.nodehealthckecks.tasktimeratio"
argument_list|,
literal|1.5f
argument_list|,
literal|"LLAP daemons are considered unhealthy, if their average (Map-) task execution time is significantly larger\n"
operator|+
literal|"than the average task execution time of other nodes. This value specifies the ratio of a node to other\n"
operator|+
literal|"nodes, which is considered as threshold for unhealthy. A value of 1.5 for example considers a node to be\n"
operator|+
literal|"unhealthy if its average task execution time is 50% larger than the average of other nodes."
argument_list|)
block|,
name|LLAP_NODEHEALTHCHECKS_EXECUTORRATIO
argument_list|(
literal|"hive.llap.nodehealthckecks.executorratio"
argument_list|,
literal|2.0f
argument_list|,
literal|"If an unhealthy node is identified, it is blacklisted only where there is enough free executors to execute\n"
operator|+
literal|"the tasks. This value specifies the ratio of the free executors compared to the blacklisted ones.\n"
operator|+
literal|"A value of 2.0 for example defines that we blacklist an unhealthy node only if we have 2 times more\n"
operator|+
literal|"free executors on the remaining nodes than the unhealthy node."
argument_list|)
block|,
name|LLAP_NODEHEALTHCHECKS_MAXNODES
argument_list|(
literal|"hive.llap.nodehealthckecks.maxnodes"
argument_list|,
literal|1
argument_list|,
literal|"The maximum number of blacklisted nodes. If there are at least this number of blacklisted nodes\n"
operator|+
literal|"the listener will not blacklist further nodes even if all the conditions are met."
argument_list|)
block|,
name|LLAP_TASK_SCHEDULER_AM_REGISTRY_NAME
argument_list|(
literal|"hive.llap.task.scheduler.am.registry"
argument_list|,
literal|"llap"
argument_list|,
literal|"AM registry name for LLAP task scheduler plugin to register with."
argument_list|)
block|,
name|LLAP_TASK_SCHEDULER_AM_REGISTRY_PRINCIPAL
argument_list|(
literal|"hive.llap.task.scheduler.am.registry.principal"
argument_list|,
literal|""
argument_list|,
literal|"The name of the principal used to access ZK AM registry securely."
argument_list|)
block|,
name|LLAP_TASK_SCHEDULER_AM_REGISTRY_KEYTAB_FILE
argument_list|(
literal|"hive.llap.task.scheduler.am.registry.keytab.file"
argument_list|,
literal|""
argument_list|,
literal|"The path to the Kerberos keytab file used to access ZK AM registry securely."
argument_list|)
block|,
name|LLAP_TASK_SCHEDULER_NODE_REENABLE_MIN_TIMEOUT_MS
argument_list|(
literal|"hive.llap.task.scheduler.node.reenable.min.timeout.ms"
argument_list|,
literal|"200ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Minimum time after which a previously disabled node will be re-enabled for scheduling,\n"
operator|+
literal|"in milliseconds. This may be modified by an exponential back-off if failures persist."
argument_list|,
literal|"llap.task.scheduler.node.re-enable.min.timeout.ms"
argument_list|)
block|,
name|LLAP_TASK_SCHEDULER_NODE_REENABLE_MAX_TIMEOUT_MS
argument_list|(
literal|"hive.llap.task.scheduler.node.reenable.max.timeout.ms"
argument_list|,
literal|"10000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Maximum time after which a previously disabled node will be re-enabled for scheduling,\n"
operator|+
literal|"in milliseconds. This may be modified by an exponential back-off if failures persist."
argument_list|,
literal|"llap.task.scheduler.node.re-enable.max.timeout.ms"
argument_list|)
block|,
name|LLAP_TASK_SCHEDULER_NODE_DISABLE_BACK_OFF_FACTOR
argument_list|(
literal|"hive.llap.task.scheduler.node.disable.backoff.factor"
argument_list|,
literal|1.5f
argument_list|,
literal|"Backoff factor on successive blacklists of a node due to some failures. Blacklist times\n"
operator|+
literal|"start at the min timeout and go up to the max timeout based on this backoff factor."
argument_list|,
literal|"llap.task.scheduler.node.disable.backoff.factor"
argument_list|)
block|,
name|LLAP_TASK_SCHEDULER_PREEMPT_INDEPENDENT
argument_list|(
literal|"hive.llap.task.scheduler.preempt.independent"
argument_list|,
literal|false
argument_list|,
literal|"Whether the AM LLAP scheduler should preempt a lower priority task for a higher pri one\n"
operator|+
literal|"even if the former doesn't depend on the latter (e.g. for two parallel sides of a union)."
argument_list|)
block|,
name|LLAP_TASK_SCHEDULER_NUM_SCHEDULABLE_TASKS_PER_NODE
argument_list|(
literal|"hive.llap.task.scheduler.num.schedulable.tasks.per.node"
argument_list|,
literal|0
argument_list|,
literal|"The number of tasks the AM TaskScheduler will try allocating per node. 0 indicates that\n"
operator|+
literal|"this should be picked up from the Registry. -1 indicates unlimited capacity; positive\n"
operator|+
literal|"values indicate a specific bound."
argument_list|,
literal|"llap.task.scheduler.num.schedulable.tasks.per.node"
argument_list|)
block|,
name|LLAP_TASK_SCHEDULER_LOCALITY_DELAY
argument_list|(
literal|"hive.llap.task.scheduler.locality.delay"
argument_list|,
literal|"0ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
operator|-
literal|1l
argument_list|,
literal|true
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
literal|true
argument_list|)
argument_list|,
literal|"Amount of time to wait before allocating a request which contains location information,"
operator|+
literal|" to a location other than the ones requested. Set to -1 for an infinite delay, 0"
operator|+
literal|"for no delay."
argument_list|)
block|,
name|LLAP_DAEMON_TASK_PREEMPTION_METRICS_INTERVALS
argument_list|(
literal|"hive.llap.daemon.task.preemption.metrics.intervals"
argument_list|,
literal|"30,60,300"
argument_list|,
literal|"Comma-delimited set of integers denoting the desired rollover intervals (in seconds)\n"
operator|+
literal|" for percentile latency metrics. Used by LLAP daemon task scheduler metrics for\n"
operator|+
literal|" time taken to kill task (due to pre-emption) and useful time wasted by the task that\n"
operator|+
literal|" is about to be preempted."
argument_list|)
block|,
name|LLAP_DAEMON_TASK_SCHEDULER_WAIT_QUEUE_SIZE
argument_list|(
literal|"hive.llap.daemon.task.scheduler.wait.queue.size"
argument_list|,
literal|10
argument_list|,
literal|"LLAP scheduler maximum queue size."
argument_list|,
literal|"llap.daemon.task.scheduler.wait.queue.size"
argument_list|)
block|,
name|LLAP_DAEMON_WAIT_QUEUE_COMPARATOR_CLASS_NAME
argument_list|(
literal|"hive.llap.daemon.wait.queue.comparator.class.name"
argument_list|,
literal|"org.apache.hadoop.hive.llap.daemon.impl.comparator.ShortestJobFirstComparator"
argument_list|,
literal|"The priority comparator to use for LLAP scheduler priority queue. The built-in options\n"
operator|+
literal|"are org.apache.hadoop.hive.llap.daemon.impl.comparator.ShortestJobFirstComparator and\n"
operator|+
literal|".....FirstInFirstOutComparator"
argument_list|,
literal|"llap.daemon.wait.queue.comparator.class.name"
argument_list|)
block|,
name|LLAP_DAEMON_TASK_SCHEDULER_ENABLE_PREEMPTION
argument_list|(
literal|"hive.llap.daemon.task.scheduler.enable.preemption"
argument_list|,
literal|true
argument_list|,
literal|"Whether non-finishable running tasks (e.g. a reducer waiting for inputs) should be\n"
operator|+
literal|"preempted by finishable tasks inside LLAP scheduler."
argument_list|,
literal|"llap.daemon.task.scheduler.enable.preemption"
argument_list|)
block|,
name|LLAP_DAEMON_METRICS_TIMED_WINDOW_AVERAGE_DATA_POINTS
argument_list|(
literal|"hive.llap.daemon.metrics.timed.window.average.data.points"
argument_list|,
literal|0
argument_list|,
literal|"The number of data points stored for calculating executor metrics timed averages.\n"
operator|+
literal|"Currently used for ExecutorNumExecutorsAvailableAverage and ExecutorNumQueuedRequestsAverage\n"
operator|+
literal|"0 means that average calculation is turned off"
argument_list|)
block|,
name|LLAP_DAEMON_METRICS_TIMED_WINDOW_AVERAGE_WINDOW_LENGTH
argument_list|(
literal|"hive.llap.daemon.metrics.timed.window.average.window.length"
argument_list|,
literal|"1m"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
argument_list|,
literal|"The length of the time window used for calculating executor metrics timed averages.\n"
operator|+
literal|"Currently used for ExecutorNumExecutorsAvailableAverage and ExecutorNumQueuedRequestsAverage\n"
argument_list|)
block|,
name|LLAP_DAEMON_METRICS_SIMPLE_AVERAGE_DATA_POINTS
argument_list|(
literal|"hive.llap.daemon.metrics.simple.average.data.points"
argument_list|,
literal|0
argument_list|,
literal|"The number of data points stored for calculating executor metrics simple averages.\n"
operator|+
literal|"Currently used for AverageQueueTime and AverageResponseTime\n"
operator|+
literal|"0 means that average calculation is turned off"
argument_list|)
block|,
name|LLAP_TASK_COMMUNICATOR_CONNECTION_TIMEOUT_MS
argument_list|(
literal|"hive.llap.task.communicator.connection.timeout.ms"
argument_list|,
literal|"16000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Connection timeout (in milliseconds) before a failure to an LLAP daemon from Tez AM."
argument_list|,
literal|"llap.task.communicator.connection.timeout-millis"
argument_list|)
block|,
name|LLAP_TASK_COMMUNICATOR_LISTENER_THREAD_COUNT
argument_list|(
literal|"hive.llap.task.communicator.listener.thread-count"
argument_list|,
literal|30
argument_list|,
literal|"The number of task communicator listener threads."
argument_list|)
block|,
name|LLAP_TASK_COMMUNICATOR_CONNECTION_SLEEP_BETWEEN_RETRIES_MS
argument_list|(
literal|"hive.llap.task.communicator.connection.sleep.between.retries.ms"
argument_list|,
literal|"2000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Sleep duration (in milliseconds) to wait before retrying on error when obtaining a\n"
operator|+
literal|"connection to LLAP daemon from Tez AM."
argument_list|,
literal|"llap.task.communicator.connection.sleep-between-retries-millis"
argument_list|)
block|,
name|LLAP_TASK_UMBILICAL_SERVER_PORT
argument_list|(
literal|"hive.llap.daemon.umbilical.port"
argument_list|,
literal|0
argument_list|,
literal|"LLAP task umbilical server RPC port"
argument_list|)
block|,
name|LLAP_DAEMON_WEB_PORT
argument_list|(
literal|"hive.llap.daemon.web.port"
argument_list|,
literal|15002
argument_list|,
literal|"LLAP daemon web UI port."
argument_list|,
literal|"llap.daemon.service.port"
argument_list|)
block|,
name|LLAP_DAEMON_WEB_SSL
argument_list|(
literal|"hive.llap.daemon.web.ssl"
argument_list|,
literal|false
argument_list|,
literal|"Whether LLAP daemon web UI should use SSL."
argument_list|,
literal|"llap.daemon.service.ssl"
argument_list|)
block|,
name|LLAP_DAEMON_WEB_XFRAME_ENABLED
argument_list|(
literal|"hive.llap.daemon.web.xframe.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Whether to enable xframe on LLAP daemon webUI\n"
argument_list|)
block|,
name|LLAP_DAEMON_WEB_XFRAME_VALUE
argument_list|(
literal|"hive.llap.daemon.web.xframe.value"
argument_list|,
literal|"SAMEORIGIN"
argument_list|,
literal|"Configuration to allow the user to set the x_frame-options value\n"
argument_list|)
block|,
name|LLAP_CLIENT_CONSISTENT_SPLITS
argument_list|(
literal|"hive.llap.client.consistent.splits"
argument_list|,
literal|true
argument_list|,
literal|"Whether to setup split locations to match nodes on which llap daemons are running, "
operator|+
literal|"instead of using the locations provided by the split itself. If there is no llap daemon "
operator|+
literal|"running, fall back to locations provided by the split. This is effective only if "
operator|+
literal|"hive.execution.mode is llap"
argument_list|)
block|,
name|LLAP_VALIDATE_ACLS
argument_list|(
literal|"hive.llap.validate.acls"
argument_list|,
literal|true
argument_list|,
literal|"Whether LLAP should reject permissive ACLs in some cases (e.g. its own management\n"
operator|+
literal|"protocol or ZK paths), similar to how ssh refuses a key with bad access permissions."
argument_list|)
block|,
name|LLAP_DAEMON_OUTPUT_SERVICE_PORT
argument_list|(
literal|"hive.llap.daemon.output.service.port"
argument_list|,
literal|15003
argument_list|,
literal|"LLAP daemon output service port"
argument_list|)
block|,
name|LLAP_DAEMON_OUTPUT_STREAM_TIMEOUT
argument_list|(
literal|"hive.llap.daemon.output.stream.timeout"
argument_list|,
literal|"120s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"The timeout for the client to connect to LLAP output service and start the fragment\n"
operator|+
literal|"output after sending the fragment. The fragment will fail if its output is not claimed."
argument_list|)
block|,
name|LLAP_DAEMON_OUTPUT_SERVICE_SEND_BUFFER_SIZE
argument_list|(
literal|"hive.llap.daemon.output.service.send.buffer.size"
argument_list|,
literal|128
operator|*
literal|1024
argument_list|,
literal|"Send buffer size to be used by LLAP daemon output service"
argument_list|)
block|,
name|LLAP_DAEMON_OUTPUT_SERVICE_MAX_PENDING_WRITES
argument_list|(
literal|"hive.llap.daemon.output.service.max.pending.writes"
argument_list|,
literal|8
argument_list|,
literal|"Maximum number of queued writes allowed per connection when sending data\n"
operator|+
literal|" via the LLAP output service to external clients."
argument_list|)
block|,
name|LLAP_EXTERNAL_SPLITS_TEMP_TABLE_STORAGE_FORMAT
argument_list|(
literal|"hive.llap.external.splits.temp.table.storage.format"
argument_list|,
literal|"orc"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"default"
argument_list|,
literal|"text"
argument_list|,
literal|"orc"
argument_list|)
argument_list|,
literal|"Storage format for temp tables created using LLAP external client"
argument_list|)
block|,
name|LLAP_EXTERNAL_SPLITS_ORDER_BY_FORCE_SINGLE_SPLIT
argument_list|(
literal|"hive.llap.external.splits.order.by.force.single.split"
argument_list|,
literal|true
argument_list|,
literal|"If LLAP external clients submits ORDER BY queries, force return a single split to guarantee reading\n"
operator|+
literal|"data out in ordered way. Setting this to false will let external clients read data out in parallel\n"
operator|+
literal|"losing the ordering (external clients are responsible for guaranteeing the ordering)"
argument_list|)
block|,
name|LLAP_ENABLE_GRACE_JOIN_IN_LLAP
argument_list|(
literal|"hive.llap.enable.grace.join.in.llap"
argument_list|,
literal|false
argument_list|,
literal|"Override if grace join should be allowed to run in llap."
argument_list|)
block|,
name|LLAP_HS2_ENABLE_COORDINATOR
argument_list|(
literal|"hive.llap.hs2.coordinator.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Whether to create the LLAP coordinator; since execution engine and container vs llap\n"
operator|+
literal|"settings are both coming from job configs, we don't know at start whether this should\n"
operator|+
literal|"be created. Default true."
argument_list|)
block|,
name|LLAP_DAEMON_LOGGER
argument_list|(
literal|"hive.llap.daemon.logger"
argument_list|,
name|Constants
operator|.
name|LLAP_LOGGER_NAME_QUERY_ROUTING
argument_list|,
operator|new
name|StringSet
argument_list|(
name|Constants
operator|.
name|LLAP_LOGGER_NAME_QUERY_ROUTING
argument_list|,
name|Constants
operator|.
name|LLAP_LOGGER_NAME_RFA
argument_list|,
name|Constants
operator|.
name|LLAP_LOGGER_NAME_CONSOLE
argument_list|)
argument_list|,
literal|"logger used for llap-daemons."
argument_list|)
block|,
name|LLAP_OUTPUT_FORMAT_ARROW
argument_list|(
literal|"hive.llap.output.format.arrow"
argument_list|,
literal|true
argument_list|,
literal|"Whether LLapOutputFormatService should output arrow batches"
argument_list|)
block|,
name|LLAP_COLLECT_LOCK_METRICS
argument_list|(
literal|"hive.llap.lockmetrics.collect"
argument_list|,
literal|false
argument_list|,
literal|"Whether lock metrics (wait times, counts) are collected for LLAP "
operator|+
literal|"related locks"
argument_list|)
block|,
name|LLAP_TASK_TIME_SUMMARY
argument_list|(
literal|"hive.llap.task.time.print.summary"
argument_list|,
literal|false
argument_list|,
literal|"Display queue and runtime of tasks by host for every query executed by the shell."
argument_list|)
block|,
name|HIVE_TRIGGER_VALIDATION_INTERVAL
argument_list|(
literal|"hive.trigger.validation.interval"
argument_list|,
literal|"500ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Interval for validating triggers during execution of a query. Triggers defined in resource plan will get\n"
operator|+
literal|"validated for all SQL operations after every defined interval (default: 500ms) and corresponding action\n"
operator|+
literal|"defined in the trigger will be taken"
argument_list|)
block|,
name|SPARK_USE_OP_STATS
argument_list|(
literal|"hive.spark.use.op.stats"
argument_list|,
literal|true
argument_list|,
literal|"Whether to use operator stats to determine reducer parallelism for Hive on Spark.\n"
operator|+
literal|"If this is false, Hive will use source table stats to determine reducer\n"
operator|+
literal|"parallelism for all first level reduce tasks, and the maximum reducer parallelism\n"
operator|+
literal|"from all parents for all the rest (second level and onward) reducer tasks."
argument_list|)
block|,
name|SPARK_USE_TS_STATS_FOR_MAPJOIN
argument_list|(
literal|"hive.spark.use.ts.stats.for.mapjoin"
argument_list|,
literal|false
argument_list|,
literal|"If this is set to true, mapjoin optimization in Hive/Spark will use statistics from\n"
operator|+
literal|"TableScan operators at the root of operator tree, instead of parent ReduceSink\n"
operator|+
literal|"operators of the Join operator."
argument_list|)
block|,
name|SPARK_OPTIMIZE_SHUFFLE_SERDE
argument_list|(
literal|"hive.spark.optimize.shuffle.serde"
argument_list|,
literal|true
argument_list|,
literal|"If this is set to true, Hive on Spark will register custom serializers for data types\n"
operator|+
literal|"in shuffle. This should result in less shuffled data."
argument_list|)
block|,
name|SPARK_CLIENT_FUTURE_TIMEOUT
argument_list|(
literal|"hive.spark.client.future.timeout"
argument_list|,
literal|"60s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Timeout for requests between Hive client and remote Spark driver."
argument_list|)
block|,
name|SPARK_JOB_MONITOR_TIMEOUT
argument_list|(
literal|"hive.spark.job.monitor.timeout"
argument_list|,
literal|"60s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Timeout for job monitor to get Spark job state."
argument_list|)
block|,
name|SPARK_RPC_CLIENT_CONNECT_TIMEOUT
argument_list|(
literal|"hive.spark.client.connect.timeout"
argument_list|,
literal|"1000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Timeout for remote Spark driver in connecting back to Hive client."
argument_list|)
block|,
name|SPARK_RPC_CLIENT_HANDSHAKE_TIMEOUT
argument_list|(
literal|"hive.spark.client.server.connect.timeout"
argument_list|,
literal|"90000ms"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
literal|"Timeout for handshake between Hive client and remote Spark driver.  Checked by both processes."
argument_list|)
block|,
name|SPARK_RPC_SECRET_RANDOM_BITS
argument_list|(
literal|"hive.spark.client.secret.bits"
argument_list|,
literal|"256"
argument_list|,
literal|"Number of bits of randomness in the generated secret for communication between Hive client and remote Spark driver. "
operator|+
literal|"Rounded down to the nearest multiple of 8."
argument_list|)
block|,
name|SPARK_RPC_MAX_THREADS
argument_list|(
literal|"hive.spark.client.rpc.threads"
argument_list|,
literal|8
argument_list|,
literal|"Maximum number of threads for remote Spark driver's RPC event loop."
argument_list|)
block|,
name|SPARK_RPC_MAX_MESSAGE_SIZE
argument_list|(
literal|"hive.spark.client.rpc.max.size"
argument_list|,
literal|50
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
literal|"Maximum message size in bytes for communication between Hive client and remote Spark driver. Default is 50MB."
argument_list|)
block|,
name|SPARK_RPC_CHANNEL_LOG_LEVEL
argument_list|(
literal|"hive.spark.client.channel.log.level"
argument_list|,
literal|null
argument_list|,
literal|"Channel logging level for remote Spark driver.  One of {DEBUG, ERROR, INFO, TRACE, WARN}."
argument_list|)
block|,
name|SPARK_RPC_SASL_MECHANISM
argument_list|(
literal|"hive.spark.client.rpc.sasl.mechanisms"
argument_list|,
literal|"DIGEST-MD5"
argument_list|,
literal|"Name of the SASL mechanism to use for authentication."
argument_list|)
block|,
name|SPARK_RPC_SERVER_ADDRESS
argument_list|(
literal|"hive.spark.client.rpc.server.address"
argument_list|,
literal|""
argument_list|,
literal|"The server address of HiverServer2 host to be used for communication between Hive client and remote Spark driver. "
operator|+
literal|"Default is empty, which means the address will be determined in the same way as for hive.server2.thrift.bind.host."
operator|+
literal|"This is only necessary if the host has multiple network addresses and if a different network address other than "
operator|+
literal|"hive.server2.thrift.bind.host is to be used."
argument_list|)
block|,
name|SPARK_RPC_SERVER_PORT
argument_list|(
literal|"hive.spark.client.rpc.server.port"
argument_list|,
literal|""
argument_list|,
literal|"A list of port ranges which can be used by RPC server "
operator|+
literal|"with the format of 49152-49222,49228 and a random one is selected from the list. Default is empty, which randomly "
operator|+
literal|"selects one port from all available ones."
argument_list|)
block|,
name|SPARK_DYNAMIC_PARTITION_PRUNING
argument_list|(
literal|"hive.spark.dynamic.partition.pruning"
argument_list|,
literal|false
argument_list|,
literal|"When dynamic pruning is enabled, joins on partition keys will be processed by writing\n"
operator|+
literal|"to a temporary HDFS file, and read later for removing unnecessary partitions."
argument_list|)
block|,
name|SPARK_DYNAMIC_PARTITION_PRUNING_MAX_DATA_SIZE
argument_list|(
literal|"hive.spark.dynamic.partition.pruning.max.data.size"
argument_list|,
literal|100
operator|*
literal|1024
operator|*
literal|1024L
argument_list|,
literal|"Maximum total data size in dynamic pruning."
argument_list|)
block|,
name|SPARK_DYNAMIC_PARTITION_PRUNING_MAP_JOIN_ONLY
argument_list|(
literal|"hive.spark.dynamic.partition.pruning.map.join.only"
argument_list|,
literal|false
argument_list|,
literal|"Turn on dynamic partition pruning only for map joins.\n"
operator|+
literal|"If hive.spark.dynamic.partition.pruning is set to true, this parameter value is ignored."
argument_list|)
block|,
name|SPARK_USE_GROUPBY_SHUFFLE
argument_list|(
literal|"hive.spark.use.groupby.shuffle"
argument_list|,
literal|true
argument_list|,
literal|"Spark groupByKey transformation has better performance but uses unbounded memory."
operator|+
literal|"Turn this off when there is a memory issue."
argument_list|)
block|,
name|SPARK_JOB_MAX_TASKS
argument_list|(
literal|"hive.spark.job.max.tasks"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"The maximum number of tasks a Spark job may have.\n"
operator|+
literal|"If a Spark job contains more tasks than the maximum, it will be cancelled. A value of -1 means no limit."
argument_list|)
block|,
name|SPARK_STAGE_MAX_TASKS
argument_list|(
literal|"hive.spark.stage.max.tasks"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"The maximum number of tasks a stage in a Spark job may have.\n"
operator|+
literal|"If a Spark job stage contains more tasks than the maximum, the job will be cancelled. A value of -1 means no limit."
argument_list|)
block|,
name|SPARK_CLIENT_TYPE
argument_list|(
literal|"hive.spark.client.type"
argument_list|,
name|HIVE_SPARK_SUBMIT_CLIENT
argument_list|,
literal|"Controls how the Spark application is launched. If "
operator|+
name|HIVE_SPARK_SUBMIT_CLIENT
operator|+
literal|" is "
operator|+
literal|"specified (default) then the spark-submit shell script is used to launch the Spark "
operator|+
literal|"app. If "
operator|+
name|HIVE_SPARK_LAUNCHER_CLIENT
operator|+
literal|" is specified then Spark's "
operator|+
literal|"InProcessLauncher is used to programmatically launch the app."
argument_list|)
block|,
name|SPARK_SESSION_TIMEOUT
argument_list|(
literal|"hive.spark.session.timeout"
argument_list|,
literal|"30m"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|MINUTES
argument_list|,
literal|30L
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
argument_list|,
literal|"Amount of time the Spark Remote Driver should wait for "
operator|+
literal|" a Spark job to be submitted before shutting down. Minimum value is 30 minutes"
argument_list|)
block|,
name|SPARK_SESSION_TIMEOUT_PERIOD
argument_list|(
literal|"hive.spark.session.timeout.period"
argument_list|,
literal|"60s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
literal|60L
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
argument_list|,
literal|"How frequently to check for idle Spark sessions. Minimum value is 60 seconds."
argument_list|)
block|,
name|NWAYJOINREORDER
argument_list|(
literal|"hive.reorder.nway.joins"
argument_list|,
literal|true
argument_list|,
literal|"Runs reordering of tables within single n-way join (i.e.: picks streamtable)"
argument_list|)
block|,
name|HIVE_MERGE_NWAY_JOINS
argument_list|(
literal|"hive.merge.nway.joins"
argument_list|,
literal|false
argument_list|,
literal|"Merge adjacent joins into a single n-way join"
argument_list|)
block|,
name|HIVE_LOG_N_RECORDS
argument_list|(
literal|"hive.log.every.n.records"
argument_list|,
literal|0L
argument_list|,
operator|new
name|RangeValidator
argument_list|(
literal|0L
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|"If value is greater than 0 logs in fixed intervals of size n rather than exponentially."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.MSCK_PATH_VALIDATION      */
annotation|@
name|Deprecated
name|HIVE_MSCK_PATH_VALIDATION
argument_list|(
literal|"hive.msck.path.validation"
argument_list|,
literal|"throw"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"throw"
argument_list|,
literal|"skip"
argument_list|,
literal|"ignore"
argument_list|)
argument_list|,
literal|"The approach msck should take with HDFS "
operator|+
literal|"directories that are partition-like but contain unsupported characters. 'throw' (an "
operator|+
literal|"exception) is the default; 'skip' will skip the invalid directories and still repair the"
operator|+
literal|" others; 'ignore' will skip the validation (legacy behavior, causes bugs in many cases)"
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.MSCK_REPAIR_BATCH_SIZE      */
annotation|@
name|Deprecated
name|HIVE_MSCK_REPAIR_BATCH_SIZE
argument_list|(
literal|"hive.msck.repair.batch.size"
argument_list|,
literal|3000
argument_list|,
literal|"Batch size for the msck repair command. If the value is greater than zero,\n "
operator|+
literal|"it will execute batch wise with the configured batch size. In case of errors while\n"
operator|+
literal|"adding unknown partitions the batch size is automatically reduced by half in the subsequent\n"
operator|+
literal|"retry attempt. The default value is 3000 which means it will execute in the batches of 3000."
argument_list|)
block|,
comment|/**      * @deprecated Use MetastoreConf.MSCK_REPAIR_BATCH_MAX_RETRIES      */
annotation|@
name|Deprecated
name|HIVE_MSCK_REPAIR_BATCH_MAX_RETRIES
argument_list|(
literal|"hive.msck.repair.batch.max.retries"
argument_list|,
literal|4
argument_list|,
literal|"Maximum number of retries for the msck repair command when adding unknown partitions.\n "
operator|+
literal|"If the value is greater than zero it will retry adding unknown partitions until the maximum\n"
operator|+
literal|"number of attempts is reached or batch size is reduced to 0, whichever is earlier.\n"
operator|+
literal|"In each retry attempt it will reduce the batch size by a factor of 2 until it reaches zero.\n"
operator|+
literal|"If the value is set to zero it will retry until the batch size becomes zero as described above."
argument_list|)
block|,
name|HIVE_SERVER2_LLAP_CONCURRENT_QUERIES
argument_list|(
literal|"hive.server2.llap.concurrent.queries"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"The number of queries allowed in parallel via llap. Negative number implies 'infinite'."
argument_list|)
block|,
name|HIVE_TEZ_ENABLE_MEMORY_MANAGER
argument_list|(
literal|"hive.tez.enable.memory.manager"
argument_list|,
literal|true
argument_list|,
literal|"Enable memory manager for tez"
argument_list|)
block|,
name|HIVE_HASH_TABLE_INFLATION_FACTOR
argument_list|(
literal|"hive.hash.table.inflation.factor"
argument_list|,
operator|(
name|float
operator|)
literal|2.0
argument_list|,
literal|"Expected inflation factor between disk/in memory representation of hash tables"
argument_list|)
block|,
name|HIVE_LOG_TRACE_ID
argument_list|(
literal|"hive.log.trace.id"
argument_list|,
literal|""
argument_list|,
literal|"Log tracing id that can be used by upstream clients for tracking respective logs. "
operator|+
literal|"Truncated to "
operator|+
name|LOG_PREFIX_LENGTH
operator|+
literal|" characters. Defaults to use auto-generated session id."
argument_list|)
block|,
name|HIVE_MM_AVOID_GLOBSTATUS_ON_S3
argument_list|(
literal|"hive.mm.avoid.s3.globstatus"
argument_list|,
literal|true
argument_list|,
literal|"Whether to use listFiles (optimized on S3) instead of globStatus when on S3."
argument_list|)
block|,
comment|// If a parameter is added to the restricted list, add a test in TestRestrictedList.Java
name|HIVE_CONF_RESTRICTED_LIST
argument_list|(
literal|"hive.conf.restricted.list"
argument_list|,
literal|"hive.security.authenticator.manager,hive.security.authorization.manager,"
operator|+
literal|"hive.security.metastore.authorization.manager,hive.security.metastore.authenticator.manager,"
operator|+
literal|"hive.users.in.admin.role,hive.server2.xsrf.filter.enabled,hive.security.authorization.enabled,"
operator|+
literal|"hive.distcp.privileged.doAs,"
operator|+
literal|"hive.server2.authentication.ldap.baseDN,"
operator|+
literal|"hive.server2.authentication.ldap.url,"
operator|+
literal|"hive.server2.authentication.ldap.Domain,"
operator|+
literal|"hive.server2.authentication.ldap.groupDNPattern,"
operator|+
literal|"hive.server2.authentication.ldap.groupFilter,"
operator|+
literal|"hive.server2.authentication.ldap.userDNPattern,"
operator|+
literal|"hive.server2.authentication.ldap.userFilter,"
operator|+
literal|"hive.server2.authentication.ldap.groupMembershipKey,"
operator|+
literal|"hive.server2.authentication.ldap.userMembershipKey,"
operator|+
literal|"hive.server2.authentication.ldap.groupClassKey,"
operator|+
literal|"hive.server2.authentication.ldap.customLDAPQuery,"
operator|+
literal|"hive.privilege.synchronizer,"
operator|+
literal|"hive.privilege.synchronizer.interval,"
operator|+
literal|"hive.spark.client.connect.timeout,"
operator|+
literal|"hive.spark.client.server.connect.timeout,"
operator|+
literal|"hive.spark.client.channel.log.level,"
operator|+
literal|"hive.spark.client.rpc.max.size,"
operator|+
literal|"hive.spark.client.rpc.threads,"
operator|+
literal|"hive.spark.client.secret.bits,"
operator|+
literal|"hive.query.max.length,"
operator|+
literal|"hive.spark.client.rpc.server.address,"
operator|+
literal|"hive.spark.client.rpc.server.port,"
operator|+
literal|"hive.spark.client.rpc.sasl.mechanisms,"
operator|+
literal|"bonecp.,"
operator|+
literal|"hive.druid.broker.address.default,"
operator|+
literal|"hive.druid.coordinator.address.default,"
operator|+
literal|"hikaricp.,"
operator|+
literal|"hadoop.bin.path,"
operator|+
literal|"yarn.bin.path,"
operator|+
literal|"spark.home,"
operator|+
literal|"hive.driver.parallel.compilation.global.limit"
argument_list|,
literal|"Comma separated list of configuration options which are immutable at runtime"
argument_list|)
block|,
name|HIVE_CONF_HIDDEN_LIST
argument_list|(
literal|"hive.conf.hidden.list"
argument_list|,
name|METASTOREPWD
operator|.
name|varname
operator|+
literal|","
operator|+
name|HIVE_SERVER2_SSL_KEYSTORE_PASSWORD
operator|.
name|varname
operator|+
literal|","
operator|+
name|DRUID_METADATA_DB_PASSWORD
operator|.
name|varname
comment|// Adding the S3 credentials from Hadoop config to be hidden
operator|+
literal|",fs.s3.awsAccessKeyId"
operator|+
literal|",fs.s3.awsSecretAccessKey"
operator|+
literal|",fs.s3n.awsAccessKeyId"
operator|+
literal|",fs.s3n.awsSecretAccessKey"
operator|+
literal|",fs.s3a.access.key"
operator|+
literal|",fs.s3a.secret.key"
operator|+
literal|",fs.s3a.proxy.password"
operator|+
literal|",dfs.adls.oauth2.credential"
operator|+
literal|",fs.adl.oauth2.credential"
operator|+
literal|",fs.azure.account.oauth2.client.secret"
argument_list|,
literal|"Comma separated list of configuration options which should not be read by normal user like passwords"
argument_list|)
block|,
name|HIVE_CONF_INTERNAL_VARIABLE_LIST
argument_list|(
literal|"hive.conf.internal.variable.list"
argument_list|,
literal|"hive.added.files.path,hive.added.jars.path,hive.added.archives.path"
argument_list|,
literal|"Comma separated list of variables which are used internally and should not be configurable."
argument_list|)
block|,
name|HIVE_SPARK_RSC_CONF_LIST
argument_list|(
literal|"hive.spark.rsc.conf.list"
argument_list|,
name|SPARK_OPTIMIZE_SHUFFLE_SERDE
operator|.
name|varname
operator|+
literal|","
operator|+
name|SPARK_CLIENT_FUTURE_TIMEOUT
operator|.
name|varname
operator|+
literal|","
operator|+
name|SPARK_CLIENT_TYPE
operator|.
name|varname
argument_list|,
literal|"Comma separated list of variables which are related to remote spark context.\n"
operator|+
literal|"Changing these variables will result in re-creating the spark session."
argument_list|)
block|,
name|HIVE_QUERY_MAX_LENGTH
argument_list|(
literal|"hive.query.max.length"
argument_list|,
literal|"10Mb"
argument_list|,
operator|new
name|SizeValidator
argument_list|()
argument_list|,
literal|"The maximum"
operator|+
literal|" size of a query string. Enforced after variable substitutions."
argument_list|)
block|,
name|HIVE_QUERY_TIMEOUT_SECONDS
argument_list|(
literal|"hive.query.timeout.seconds"
argument_list|,
literal|"0s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Timeout for Running Query in seconds. A nonpositive value means infinite. "
operator|+
literal|"If the query timeout is also set by thrift API call, the smaller one will be taken."
argument_list|)
block|,
name|HIVE_COMPUTE_SPLITS_NUM_THREADS
argument_list|(
literal|"hive.compute.splits.num.threads"
argument_list|,
literal|10
argument_list|,
literal|"How many threads Input Format should use to create splits in parallel."
argument_list|,
name|HIVE_ORC_COMPUTE_SPLITS_NUM_THREADS
operator|.
name|varname
argument_list|)
block|,
name|HIVE_EXEC_INPUT_LISTING_MAX_THREADS
argument_list|(
literal|"hive.exec.input.listing.max.threads"
argument_list|,
literal|0
argument_list|,
operator|new
name|SizeValidator
argument_list|(
literal|0L
argument_list|,
literal|true
argument_list|,
literal|1024L
argument_list|,
literal|true
argument_list|)
argument_list|,
literal|"Maximum number of threads that Hive uses to list file information from file systems (recommended> 1 for blobstore)."
argument_list|)
block|,
name|HIVE_QUERY_REEXECUTION_ENABLED
argument_list|(
literal|"hive.query.reexecution.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Enable query reexecutions"
argument_list|)
block|,
name|HIVE_QUERY_REEXECUTION_STRATEGIES
argument_list|(
literal|"hive.query.reexecution.strategies"
argument_list|,
literal|"overlay,reoptimize"
argument_list|,
literal|"comma separated list of plugin can be used:\n"
operator|+
literal|"  overlay: hiveconf subtree 'reexec.overlay' is used as an overlay in case of an execution errors out\n"
operator|+
literal|"  reoptimize: collects operator statistics during execution and recompile the query after a failure"
argument_list|)
block|,
name|HIVE_QUERY_REEXECUTION_STATS_PERSISTENCE
argument_list|(
literal|"hive.query.reexecution.stats.persist.scope"
argument_list|,
literal|"metastore"
argument_list|,
operator|new
name|StringSet
argument_list|(
literal|"query"
argument_list|,
literal|"hiveserver"
argument_list|,
literal|"metastore"
argument_list|)
argument_list|,
literal|"Sets the persistence scope of runtime statistics\n"
operator|+
literal|"  query: runtime statistics are only used during re-execution\n"
operator|+
literal|"  hiveserver: runtime statistics are persisted in the hiveserver - all sessions share it\n"
operator|+
literal|"  metastore: runtime statistics are persisted in the metastore as well"
argument_list|)
block|,
name|HIVE_QUERY_MAX_REEXECUTION_COUNT
argument_list|(
literal|"hive.query.reexecution.max.count"
argument_list|,
literal|1
argument_list|,
literal|"Maximum number of re-executions for a single query."
argument_list|)
block|,
name|HIVE_QUERY_REEXECUTION_ALWAYS_COLLECT_OPERATOR_STATS
argument_list|(
literal|"hive.query.reexecution.always.collect.operator.stats"
argument_list|,
literal|false
argument_list|,
literal|"If sessionstats are enabled; this option can be used to collect statistics all the time"
argument_list|)
block|,
name|HIVE_QUERY_REEXECUTION_STATS_CACHE_BATCH_SIZE
argument_list|(
literal|"hive.query.reexecution.stats.cache.batch.size"
argument_list|,
operator|-
literal|1
argument_list|,
literal|"If runtime stats are stored in metastore; the maximal batch size per round during load."
argument_list|)
block|,
name|HIVE_QUERY_REEXECUTION_STATS_CACHE_SIZE
argument_list|(
literal|"hive.query.reexecution.stats.cache.size"
argument_list|,
literal|100_000
argument_list|,
literal|"Size of the runtime statistics cache. Unit is: OperatorStat entry; a query plan consist ~100."
argument_list|)
block|,
name|HIVE_QUERY_PLANMAPPER_LINK_RELNODES
argument_list|(
literal|"hive.query.planmapper.link.relnodes"
argument_list|,
literal|true
argument_list|,
literal|"Whether to link Calcite nodes to runtime statistics."
argument_list|)
block|,
name|HIVE_SCHEDULED_QUERIES_EXECUTOR_ENABLED
argument_list|(
literal|"hive.scheduled.queries.executor.enabled"
argument_list|,
literal|true
argument_list|,
literal|"Controls whether HS2 will run scheduled query executor."
argument_list|)
block|,
name|HIVE_SCHEDULED_QUERIES_NAMESPACE
argument_list|(
literal|"hive.scheduled.queries.namespace"
argument_list|,
literal|"hive"
argument_list|,
literal|"Sets the scheduled query namespace to be used. New scheduled queries are created in this namespace;"
operator|+
literal|"and execution is also bound to the namespace"
argument_list|)
block|,
name|HIVE_SCHEDULED_QUERIES_EXECUTOR_IDLE_SLEEP_TIME
argument_list|(
literal|"hive.scheduled.queries.executor.idle.sleep.time"
argument_list|,
literal|"60s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Time to sleep between quering for the presence of a scheduled query."
argument_list|)
block|,
name|HIVE_SCHEDULED_QUERIES_EXECUTOR_PROGRESS_REPORT_INTERVAL
argument_list|(
literal|"hive.scheduled.queries.executor.progress.report.interval"
argument_list|,
literal|"60s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"While scheduled queries are in flight; "
operator|+
literal|"a background update happens periodically to report the actual state of the query"
argument_list|)
block|,
name|HIVE_SCHEDULED_QUERIES_CREATE_AS_ENABLED
argument_list|(
literal|"hive.scheduled.queries.create.as.enabled"
argument_list|,
literal|true
argument_list|,
literal|"This option sets the default behaviour of newly created scheduled queries."
argument_list|)
block|,
name|HIVE_SECURITY_AUTHORIZATION_SCHEDULED_QUERIES_SUPPORTED
argument_list|(
literal|"hive.security.authorization.scheduled.queries.supported"
argument_list|,
literal|false
argument_list|,
literal|"Enable this if the configured authorizer is able to handle scheduled query related calls."
argument_list|)
block|,
name|HIVE_SCHEDULED_QUERIES_MAX_EXECUTORS
argument_list|(
literal|"hive.scheduled.queries.max.executors"
argument_list|,
literal|4
argument_list|,
operator|new
name|RangeValidator
argument_list|(
literal|1
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|"Maximal number of scheduled query executors to allow."
argument_list|)
block|,
name|HIVE_QUERY_RESULTS_CACHE_ENABLED
argument_list|(
literal|"hive.query.results.cache.enabled"
argument_list|,
literal|true
argument_list|,
literal|"If the query results cache is enabled. This will keep results of previously executed queries "
operator|+
literal|"to be reused if the same query is executed again."
argument_list|)
block|,
name|HIVE_QUERY_RESULTS_CACHE_NONTRANSACTIONAL_TABLES_ENABLED
argument_list|(
literal|"hive.query.results.cache.nontransactional.tables.enabled"
argument_list|,
literal|false
argument_list|,
literal|"If the query results cache is enabled for queries involving non-transactional tables."
operator|+
literal|"Users who enable this setting should be willing to tolerate some amount of stale results in the cache."
argument_list|)
block|,
name|HIVE_QUERY_RESULTS_CACHE_WAIT_FOR_PENDING_RESULTS
argument_list|(
literal|"hive.query.results.cache.wait.for.pending.results"
argument_list|,
literal|true
argument_list|,
literal|"Should a query wait for the pending results of an already running query, "
operator|+
literal|"in order to use the cached result when it becomes ready"
argument_list|)
block|,
name|HIVE_QUERY_RESULTS_CACHE_DIRECTORY
argument_list|(
literal|"hive.query.results.cache.directory"
argument_list|,
literal|"/tmp/hive/_resultscache_"
argument_list|,
literal|"Location of the query results cache directory. Temporary results from queries "
operator|+
literal|"will be moved to this location."
argument_list|)
block|,
name|HIVE_QUERY_RESULTS_CACHE_MAX_ENTRY_LIFETIME
argument_list|(
literal|"hive.query.results.cache.max.entry.lifetime"
argument_list|,
literal|"3600s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"Maximum lifetime in seconds for an entry in the query results cache. A nonpositive value means infinite."
argument_list|)
block|,
name|HIVE_QUERY_RESULTS_CACHE_MAX_SIZE
argument_list|(
literal|"hive.query.results.cache.max.size"
argument_list|,
operator|(
name|long
operator|)
literal|2
operator|*
literal|1024
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
literal|"Maximum total size in bytes that the query results cache directory is allowed to use on the filesystem."
argument_list|)
block|,
name|HIVE_QUERY_RESULTS_CACHE_MAX_ENTRY_SIZE
argument_list|(
literal|"hive.query.results.cache.max.entry.size"
argument_list|,
operator|(
name|long
operator|)
literal|10
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
literal|"Maximum size in bytes that a single query result is allowed to use in the results cache directory"
argument_list|)
block|,
name|HIVE_NOTFICATION_EVENT_POLL_INTERVAL
argument_list|(
literal|"hive.notification.event.poll.interval"
argument_list|,
literal|"60s"
argument_list|,
operator|new
name|TimeValidator
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
literal|"How often the notification log is polled for new NotificationEvents from the metastore."
operator|+
literal|"A nonpositive value means the notification log is never polled."
argument_list|)
block|,
name|HIVE_NOTFICATION_EVENT_CONSUMERS
argument_list|(
literal|"hive.notification.event.consumers"
argument_list|,
literal|"org.apache.hadoop.hive.ql.cache.results.QueryResultsCache$InvalidationEventConsumer"
argument_list|,
literal|"Comma-separated list of class names extending EventConsumer,"
operator|+
literal|"to handle the NotificationEvents retreived by the notification event poll."
argument_list|)
block|,
comment|/* BLOBSTORE section */
name|HIVE_BLOBSTORE_SUPPORTED_SCHEMES
argument_list|(
literal|"hive.blobstore.supported.schemes"
argument_list|,
literal|"s3,s3a,s3n"
argument_list|,
literal|"Comma-separated list of supported blobstore schemes."
argument_list|)
block|,
name|HIVE_BLOBSTORE_USE_BLOBSTORE_AS_SCRATCHDIR
argument_list|(
literal|"hive.blobstore.use.blobstore.as.scratchdir"
argument_list|,
literal|false
argument_list|,
literal|"Enable the use of scratch directories directly on blob storage systems (it may cause performance penalties)."
argument_list|)
block|,
name|HIVE_BLOBSTORE_OPTIMIZATIONS_ENABLED
argument_list|(
literal|"hive.blobstore.optimizations.enabled"
argument_list|,
literal|true
argument_list|,
literal|"This parameter enables a number of optimizations when running on blobstores:\n"
operator|+
literal|"(1) If hive.blobstore.use.blobstore.as.scratchdir is false, force the last Hive job to write to the blobstore.\n"
operator|+
literal|"This is a performance optimization that forces the final FileSinkOperator to write to the blobstore.\n"
operator|+
literal|"See HIVE-15121 for details."
argument_list|)
block|,
name|HIVE_ADDITIONAL_CONFIG_FILES
argument_list|(
literal|"hive.additional.config.files"
argument_list|,
literal|""
argument_list|,
literal|"The names of additional config files, such as ldap-site.xml,"
operator|+
literal|"spark-site.xml, etc in comma separated list."
argument_list|)
block|;
specifier|public
specifier|final
name|String
name|varname
decl_stmt|;
specifier|public
specifier|final
name|String
name|altName
decl_stmt|;
specifier|private
specifier|final
name|String
name|defaultExpr
decl_stmt|;
specifier|public
specifier|final
name|String
name|defaultStrVal
decl_stmt|;
specifier|public
specifier|final
name|int
name|defaultIntVal
decl_stmt|;
specifier|public
specifier|final
name|long
name|defaultLongVal
decl_stmt|;
specifier|public
specifier|final
name|float
name|defaultFloatVal
decl_stmt|;
specifier|public
specifier|final
name|boolean
name|defaultBoolVal
decl_stmt|;
specifier|private
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|valClass
decl_stmt|;
specifier|private
specifier|final
name|VarType
name|valType
decl_stmt|;
specifier|private
specifier|final
name|Validator
name|validator
decl_stmt|;
specifier|private
specifier|final
name|String
name|description
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|excluded
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|caseSensitive
decl_stmt|;
name|ConfVars
parameter_list|(
name|String
name|varname
parameter_list|,
name|Object
name|defaultVal
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|this
argument_list|(
name|varname
argument_list|,
name|defaultVal
argument_list|,
literal|null
argument_list|,
name|description
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|ConfVars
parameter_list|(
name|String
name|varname
parameter_list|,
name|Object
name|defaultVal
parameter_list|,
name|String
name|description
parameter_list|,
name|String
name|altName
parameter_list|)
block|{
name|this
argument_list|(
name|varname
argument_list|,
name|defaultVal
argument_list|,
literal|null
argument_list|,
name|description
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
name|altName
argument_list|)
expr_stmt|;
block|}
name|ConfVars
parameter_list|(
name|String
name|varname
parameter_list|,
name|Object
name|defaultVal
parameter_list|,
name|Validator
name|validator
parameter_list|,
name|String
name|description
parameter_list|,
name|String
name|altName
parameter_list|)
block|{
name|this
argument_list|(
name|varname
argument_list|,
name|defaultVal
argument_list|,
name|validator
argument_list|,
name|description
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
name|altName
argument_list|)
expr_stmt|;
block|}
name|ConfVars
parameter_list|(
name|String
name|varname
parameter_list|,
name|Object
name|defaultVal
parameter_list|,
name|String
name|description
parameter_list|,
name|boolean
name|excluded
parameter_list|)
block|{
name|this
argument_list|(
name|varname
argument_list|,
name|defaultVal
argument_list|,
literal|null
argument_list|,
name|description
argument_list|,
literal|true
argument_list|,
name|excluded
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|ConfVars
parameter_list|(
name|String
name|varname
parameter_list|,
name|String
name|defaultVal
parameter_list|,
name|boolean
name|caseSensitive
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|this
argument_list|(
name|varname
argument_list|,
name|defaultVal
argument_list|,
literal|null
argument_list|,
name|description
argument_list|,
name|caseSensitive
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|ConfVars
parameter_list|(
name|String
name|varname
parameter_list|,
name|Object
name|defaultVal
parameter_list|,
name|Validator
name|validator
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|this
argument_list|(
name|varname
argument_list|,
name|defaultVal
argument_list|,
name|validator
argument_list|,
name|description
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|ConfVars
parameter_list|(
name|String
name|varname
parameter_list|,
name|Object
name|defaultVal
parameter_list|,
name|Validator
name|validator
parameter_list|,
name|String
name|description
parameter_list|,
name|boolean
name|excluded
parameter_list|)
block|{
name|this
argument_list|(
name|varname
argument_list|,
name|defaultVal
argument_list|,
name|validator
argument_list|,
name|description
argument_list|,
literal|true
argument_list|,
name|excluded
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|ConfVars
parameter_list|(
name|String
name|varname
parameter_list|,
name|Object
name|defaultVal
parameter_list|,
name|Validator
name|validator
parameter_list|,
name|String
name|description
parameter_list|,
name|boolean
name|caseSensitive
parameter_list|,
name|boolean
name|excluded
parameter_list|,
name|String
name|altName
parameter_list|)
block|{
name|this
operator|.
name|varname
operator|=
name|varname
expr_stmt|;
name|this
operator|.
name|validator
operator|=
name|validator
expr_stmt|;
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
name|this
operator|.
name|defaultExpr
operator|=
name|defaultVal
operator|==
literal|null
condition|?
literal|null
else|:
name|String
operator|.
name|valueOf
argument_list|(
name|defaultVal
argument_list|)
expr_stmt|;
name|this
operator|.
name|excluded
operator|=
name|excluded
expr_stmt|;
name|this
operator|.
name|caseSensitive
operator|=
name|caseSensitive
expr_stmt|;
name|this
operator|.
name|altName
operator|=
name|altName
expr_stmt|;
if|if
condition|(
name|defaultVal
operator|==
literal|null
operator|||
name|defaultVal
operator|instanceof
name|String
condition|)
block|{
name|this
operator|.
name|valClass
operator|=
name|String
operator|.
name|class
expr_stmt|;
name|this
operator|.
name|valType
operator|=
name|VarType
operator|.
name|STRING
expr_stmt|;
name|this
operator|.
name|defaultStrVal
operator|=
name|SystemVariables
operator|.
name|substitute
argument_list|(
operator|(
name|String
operator|)
name|defaultVal
argument_list|)
expr_stmt|;
name|this
operator|.
name|defaultIntVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultLongVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultFloatVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultBoolVal
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|defaultVal
operator|instanceof
name|Integer
condition|)
block|{
name|this
operator|.
name|valClass
operator|=
name|Integer
operator|.
name|class
expr_stmt|;
name|this
operator|.
name|valType
operator|=
name|VarType
operator|.
name|INT
expr_stmt|;
name|this
operator|.
name|defaultStrVal
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|defaultIntVal
operator|=
operator|(
name|Integer
operator|)
name|defaultVal
expr_stmt|;
name|this
operator|.
name|defaultLongVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultFloatVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultBoolVal
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|defaultVal
operator|instanceof
name|Long
condition|)
block|{
name|this
operator|.
name|valClass
operator|=
name|Long
operator|.
name|class
expr_stmt|;
name|this
operator|.
name|valType
operator|=
name|VarType
operator|.
name|LONG
expr_stmt|;
name|this
operator|.
name|defaultStrVal
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|defaultIntVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultLongVal
operator|=
operator|(
name|Long
operator|)
name|defaultVal
expr_stmt|;
name|this
operator|.
name|defaultFloatVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultBoolVal
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|defaultVal
operator|instanceof
name|Float
condition|)
block|{
name|this
operator|.
name|valClass
operator|=
name|Float
operator|.
name|class
expr_stmt|;
name|this
operator|.
name|valType
operator|=
name|VarType
operator|.
name|FLOAT
expr_stmt|;
name|this
operator|.
name|defaultStrVal
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|defaultIntVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultLongVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultFloatVal
operator|=
operator|(
name|Float
operator|)
name|defaultVal
expr_stmt|;
name|this
operator|.
name|defaultBoolVal
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|defaultVal
operator|instanceof
name|Boolean
condition|)
block|{
name|this
operator|.
name|valClass
operator|=
name|Boolean
operator|.
name|class
expr_stmt|;
name|this
operator|.
name|valType
operator|=
name|VarType
operator|.
name|BOOLEAN
expr_stmt|;
name|this
operator|.
name|defaultStrVal
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|defaultIntVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultLongVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultFloatVal
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|defaultBoolVal
operator|=
operator|(
name|Boolean
operator|)
name|defaultVal
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Not supported type value "
operator|+
name|defaultVal
operator|.
name|getClass
argument_list|()
operator|+
literal|" for name "
operator|+
name|varname
argument_list|)
throw|;
block|}
block|}
specifier|public
name|boolean
name|isType
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|valType
operator|.
name|isType
argument_list|(
name|value
argument_list|)
return|;
block|}
specifier|public
name|Validator
name|getValidator
parameter_list|()
block|{
return|return
name|validator
return|;
block|}
specifier|public
name|String
name|validate
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|validator
operator|==
literal|null
condition|?
literal|null
else|:
name|validator
operator|.
name|validate
argument_list|(
name|value
argument_list|)
return|;
block|}
specifier|public
name|String
name|validatorDescription
parameter_list|()
block|{
return|return
name|validator
operator|==
literal|null
condition|?
literal|null
else|:
name|validator
operator|.
name|toDescription
argument_list|()
return|;
block|}
specifier|public
name|String
name|typeString
parameter_list|()
block|{
name|String
name|type
init|=
name|valType
operator|.
name|typeString
argument_list|()
decl_stmt|;
if|if
condition|(
name|valType
operator|==
name|VarType
operator|.
name|STRING
operator|&&
name|validator
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|validator
operator|instanceof
name|TimeValidator
condition|)
block|{
name|type
operator|+=
literal|"(TIME)"
expr_stmt|;
block|}
block|}
return|return
name|type
return|;
block|}
specifier|public
name|String
name|getRawDescription
parameter_list|()
block|{
return|return
name|description
return|;
block|}
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
name|String
name|validator
init|=
name|validatorDescription
argument_list|()
decl_stmt|;
if|if
condition|(
name|validator
operator|!=
literal|null
condition|)
block|{
return|return
name|validator
operator|+
literal|".\n"
operator|+
name|description
return|;
block|}
return|return
name|description
return|;
block|}
specifier|public
name|boolean
name|isExcluded
parameter_list|()
block|{
return|return
name|excluded
return|;
block|}
specifier|public
name|boolean
name|isCaseSensitive
parameter_list|()
block|{
return|return
name|caseSensitive
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|varname
return|;
block|}
specifier|private
specifier|static
name|String
name|findHadoopBinary
parameter_list|()
block|{
name|String
name|val
init|=
name|findHadoopHome
argument_list|()
decl_stmt|;
comment|// if can't find hadoop home we can at least try /usr/bin/hadoop
name|val
operator|=
operator|(
name|val
operator|==
literal|null
condition|?
name|File
operator|.
name|separator
operator|+
literal|"usr"
else|:
name|val
operator|)
operator|+
name|File
operator|.
name|separator
operator|+
literal|"bin"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"hadoop"
expr_stmt|;
comment|// Launch hadoop command file on windows.
return|return
name|val
return|;
block|}
specifier|private
specifier|static
name|String
name|findYarnBinary
parameter_list|()
block|{
name|String
name|val
init|=
name|findHadoopHome
argument_list|()
decl_stmt|;
name|val
operator|=
operator|(
name|val
operator|==
literal|null
condition|?
literal|"yarn"
else|:
name|val
operator|+
name|File
operator|.
name|separator
operator|+
literal|"bin"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"yarn"
operator|)
expr_stmt|;
return|return
name|val
return|;
block|}
specifier|private
specifier|static
name|String
name|findMapRedBinary
parameter_list|()
block|{
name|String
name|val
init|=
name|findHadoopHome
argument_list|()
decl_stmt|;
name|val
operator|=
operator|(
name|val
operator|==
literal|null
condition|?
literal|"mapred"
else|:
name|val
operator|+
name|File
operator|.
name|separator
operator|+
literal|"bin"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"mapred"
operator|)
expr_stmt|;
return|return
name|val
return|;
block|}
specifier|private
specifier|static
name|String
name|findHadoopHome
parameter_list|()
block|{
name|String
name|val
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HADOOP_HOME"
argument_list|)
decl_stmt|;
comment|// In Hadoop 1.X and Hadoop 2.X HADOOP_HOME is gone and replaced with HADOOP_PREFIX
if|if
condition|(
name|val
operator|==
literal|null
condition|)
block|{
name|val
operator|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HADOOP_PREFIX"
argument_list|)
expr_stmt|;
block|}
return|return
name|val
return|;
block|}
specifier|public
name|String
name|getDefaultValue
parameter_list|()
block|{
return|return
name|valType
operator|.
name|defaultValueString
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|public
name|String
name|getDefaultExpr
parameter_list|()
block|{
return|return
name|defaultExpr
return|;
block|}
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|getValidStringValues
parameter_list|()
block|{
if|if
condition|(
name|validator
operator|==
literal|null
operator|||
operator|!
operator|(
name|validator
operator|instanceof
name|StringSet
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|varname
operator|+
literal|" does not specify a list of valid values"
argument_list|)
throw|;
block|}
return|return
operator|(
operator|(
name|StringSet
operator|)
name|validator
operator|)
operator|.
name|getExpected
argument_list|()
return|;
block|}
enum|enum
name|VarType
block|{
name|STRING
block|{
annotation|@
name|Override
name|void
name|checkType
parameter_list|(
name|String
name|value
parameter_list|)
throws|throws
name|Exception
block|{ }
annotation|@
name|Override
name|String
name|defaultValueString
parameter_list|(
name|ConfVars
name|confVar
parameter_list|)
block|{
return|return
name|confVar
operator|.
name|defaultStrVal
return|;
block|}
block|}
block|,
name|INT
block|{
annotation|@
name|Override
name|void
name|checkType
parameter_list|(
name|String
name|value
parameter_list|)
throws|throws
name|Exception
block|{
name|Integer
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|,
name|LONG
block|{
annotation|@
name|Override
name|void
name|checkType
parameter_list|(
name|String
name|value
parameter_list|)
throws|throws
name|Exception
block|{
name|Long
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|,
name|FLOAT
block|{
annotation|@
name|Override
name|void
name|checkType
parameter_list|(
name|String
name|value
parameter_list|)
throws|throws
name|Exception
block|{
name|Float
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|,
name|BOOLEAN
block|{
annotation|@
name|Override
name|void
name|checkType
parameter_list|(
name|String
name|value
parameter_list|)
throws|throws
name|Exception
block|{
name|Boolean
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|;
name|boolean
name|isType
parameter_list|(
name|String
name|value
parameter_list|)
block|{
try|try
block|{
name|checkType
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
name|String
name|typeString
parameter_list|()
block|{
return|return
name|name
argument_list|()
operator|.
name|toUpperCase
argument_list|()
return|;
block|}
name|String
name|defaultValueString
parameter_list|(
name|ConfVars
name|confVar
parameter_list|)
block|{
return|return
name|confVar
operator|.
name|defaultExpr
return|;
block|}
specifier|abstract
name|void
name|checkType
parameter_list|(
name|String
name|value
parameter_list|)
throws|throws
name|Exception
function_decl|;
block|}
block|}
comment|/**    * Writes the default ConfVars out to a byte array and returns an input    * stream wrapping that byte array.    *    * We need this in order to initialize the ConfVar properties    * in the underling Configuration object using the addResource(InputStream)    * method.    *    * It is important to use a LoopingByteArrayInputStream because it turns out    * addResource(InputStream) is broken since Configuration tries to read the    * entire contents of the same InputStream repeatedly without resetting it.    * LoopingByteArrayInputStream has special logic to handle this.    */
specifier|private
specifier|static
specifier|synchronized
name|InputStream
name|getConfVarInputStream
parameter_list|()
block|{
if|if
condition|(
name|confVarByteArray
operator|==
literal|null
condition|)
block|{
try|try
block|{
comment|// Create a Hadoop configuration without inheriting default settings.
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|applyDefaultNonNullConfVars
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ByteArrayOutputStream
name|confVarBaos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|conf
operator|.
name|writeXml
argument_list|(
name|confVarBaos
argument_list|)
expr_stmt|;
name|confVarByteArray
operator|=
name|confVarBaos
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// We're pretty screwed if we can't load the default conf vars
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to initialize default Hive configuration variables!"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
operator|new
name|LoopingByteArrayInputStream
argument_list|(
name|confVarByteArray
argument_list|)
return|;
block|}
specifier|public
name|void
name|verifyAndSet
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
if|if
condition|(
name|modWhiteListPattern
operator|!=
literal|null
condition|)
block|{
name|Matcher
name|wlMatcher
init|=
name|modWhiteListPattern
operator|.
name|matcher
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|wlMatcher
operator|.
name|matches
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot modify "
operator|+
name|name
operator|+
literal|" at runtime. "
operator|+
literal|"It is not in list of params that are allowed to be modified at runtime"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|Iterables
operator|.
name|any
argument_list|(
name|restrictList
argument_list|,
name|restrictedVar
lambda|->
name|name
operator|!=
literal|null
operator|&&
name|name
operator|.
name|startsWith
argument_list|(
name|restrictedVar
argument_list|)
argument_list|)
condition|)
block|{
throw|throw
argument_list|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot modify "
operator|+
name|name
operator|+
literal|" at runtime. It is in the list"
operator|+
literal|" of parameters that can't be modified at runtime or is prefixed by a restricted variable"
argument_list|)
block|;     }
name|String
name|oldValue
init|=
name|name
operator|!=
literal|null
condition|?
name|get
argument_list|(
name|name
argument_list|)
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|name
operator|==
literal|null
operator|||
name|value
operator|==
literal|null
operator|||
operator|!
name|value
operator|.
name|equals
argument_list|(
name|oldValue
argument_list|)
condition|)
block|{
comment|// When either name or value is null, the set method below will fail,
comment|// and throw IllegalArgumentException
name|set
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|isSparkRelatedConfig
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|isSparkConfigUpdated
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|boolean
name|isHiddenConfig
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|Iterables
operator|.
name|any
argument_list|(
name|hiddenSet
argument_list|,
name|hiddenVar
lambda|->
name|name
operator|.
name|startsWith
argument_list|(
name|hiddenVar
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isEncodedPar
parameter_list|(
name|String
name|name
parameter_list|)
block|{
for|for
control|(
name|ConfVars
name|confVar
range|:
name|HiveConf
operator|.
name|ENCODED_CONF
control|)
block|{
name|ConfVars
name|confVar1
init|=
name|confVar
decl_stmt|;
if|if
condition|(
name|confVar1
operator|.
name|varname
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * check whether spark related property is updated, which includes spark configurations,    * RSC configurations and yarn configuration in Spark on YARN mode.    * @param name    * @return    */
specifier|private
name|boolean
name|isSparkRelatedConfig
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|startsWith
argument_list|(
literal|"spark"
argument_list|)
condition|)
block|{
comment|// Spark property.
comment|// for now we don't support changing spark app name on the fly
name|result
operator|=
operator|!
name|name
operator|.
name|equals
argument_list|(
literal|"spark.app.name"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|name
operator|.
name|startsWith
argument_list|(
literal|"yarn"
argument_list|)
condition|)
block|{
comment|// YARN property in Spark on YARN mode.
name|String
name|sparkMaster
init|=
name|get
argument_list|(
literal|"spark.master"
argument_list|)
decl_stmt|;
if|if
condition|(
name|sparkMaster
operator|!=
literal|null
operator|&&
name|sparkMaster
operator|.
name|startsWith
argument_list|(
literal|"yarn"
argument_list|)
condition|)
block|{
name|result
operator|=
literal|true
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|rscList
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|rscVar
lambda|->
name|rscVar
operator|.
name|equals
argument_list|(
name|name
argument_list|)
argument_list|)
condition|)
block|{
comment|// Remote Spark Context property.
name|result
operator|=
literal|true
block|;     }
elseif|else
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"mapreduce.job.queuename"
argument_list|)
condition|)
block|{
comment|// a special property starting with mapreduce that we would also like to effect if it changes
name|result
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|int
name|getIntVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|Integer
operator|.
name|class
operator|)
operator|:
name|var
operator|.
name|varname
assert|;
if|if
condition|(
name|var
operator|.
name|altName
operator|!=
literal|null
condition|)
block|{
return|return
name|conf
operator|.
name|getInt
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|var
operator|.
name|altName
argument_list|,
name|var
operator|.
name|defaultIntVal
argument_list|)
argument_list|)
return|;
block|}
return|return
name|conf
operator|.
name|getInt
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|var
operator|.
name|defaultIntVal
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|setIntVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|,
name|int
name|val
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|Integer
operator|.
name|class
operator|)
operator|:
name|var
operator|.
name|varname
assert|;
name|conf
operator|.
name|setInt
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getIntVar
parameter_list|(
name|ConfVars
name|var
parameter_list|)
block|{
return|return
name|getIntVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|)
return|;
block|}
specifier|public
name|void
name|setIntVar
parameter_list|(
name|ConfVars
name|var
parameter_list|,
name|int
name|val
parameter_list|)
block|{
name|setIntVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|long
name|getTimeVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|,
name|TimeUnit
name|outUnit
parameter_list|)
block|{
return|return
name|toTime
argument_list|(
name|getVar
argument_list|(
name|conf
argument_list|,
name|var
argument_list|)
argument_list|,
name|getDefaultTimeUnit
argument_list|(
name|var
argument_list|)
argument_list|,
name|outUnit
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|setTimeVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|,
name|long
name|time
parameter_list|,
name|TimeUnit
name|timeunit
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|String
operator|.
name|class
operator|)
operator|:
name|var
operator|.
name|varname
assert|;
name|conf
operator|.
name|set
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|time
operator|+
name|stringFor
argument_list|(
name|timeunit
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|getTimeVar
parameter_list|(
name|ConfVars
name|var
parameter_list|,
name|TimeUnit
name|outUnit
parameter_list|)
block|{
return|return
name|getTimeVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|,
name|outUnit
argument_list|)
return|;
block|}
specifier|public
name|void
name|setTimeVar
parameter_list|(
name|ConfVars
name|var
parameter_list|,
name|long
name|time
parameter_list|,
name|TimeUnit
name|outUnit
parameter_list|)
block|{
name|setTimeVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|,
name|time
argument_list|,
name|outUnit
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|long
name|getSizeVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|)
block|{
return|return
name|toSizeBytes
argument_list|(
name|getVar
argument_list|(
name|conf
argument_list|,
name|var
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|long
name|getSizeVar
parameter_list|(
name|ConfVars
name|var
parameter_list|)
block|{
return|return
name|getSizeVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TimeUnit
name|getDefaultTimeUnit
parameter_list|(
name|ConfVars
name|var
parameter_list|)
block|{
name|TimeUnit
name|inputUnit
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|var
operator|.
name|validator
operator|instanceof
name|TimeValidator
condition|)
block|{
name|inputUnit
operator|=
operator|(
operator|(
name|TimeValidator
operator|)
name|var
operator|.
name|validator
operator|)
operator|.
name|getTimeUnit
argument_list|()
expr_stmt|;
block|}
return|return
name|inputUnit
return|;
block|}
specifier|public
specifier|static
name|long
name|toTime
parameter_list|(
name|String
name|value
parameter_list|,
name|TimeUnit
name|inputUnit
parameter_list|,
name|TimeUnit
name|outUnit
parameter_list|)
block|{
name|String
index|[]
name|parsed
init|=
name|parseNumberFollowedByUnit
argument_list|(
name|value
operator|.
name|trim
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|outUnit
operator|.
name|convert
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|parsed
index|[
literal|0
index|]
operator|.
name|trim
argument_list|()
argument_list|)
argument_list|,
name|unitFor
argument_list|(
name|parsed
index|[
literal|1
index|]
operator|.
name|trim
argument_list|()
argument_list|,
name|inputUnit
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|long
name|toSizeBytes
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|String
index|[]
name|parsed
init|=
name|parseNumberFollowedByUnit
argument_list|(
name|value
operator|.
name|trim
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|parsed
index|[
literal|0
index|]
operator|.
name|trim
argument_list|()
argument_list|)
operator|*
name|multiplierFor
argument_list|(
name|parsed
index|[
literal|1
index|]
operator|.
name|trim
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
index|[]
name|parseNumberFollowedByUnit
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|char
index|[]
name|chars
init|=
name|value
operator|.
name|toCharArray
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
init|;
name|i
operator|<
name|chars
operator|.
name|length
operator|&&
operator|(
name|chars
index|[
name|i
index|]
operator|==
literal|'-'
operator|||
name|Character
operator|.
name|isDigit
argument_list|(
name|chars
index|[
name|i
index|]
argument_list|)
operator|)
condition|;
name|i
operator|++
control|)
block|{     }
return|return
operator|new
name|String
index|[]
block|{
name|value
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|i
argument_list|)
block|,
name|value
operator|.
name|substring
argument_list|(
name|i
argument_list|)
block|}
return|;
block|}
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|daysSet
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
literal|"d"
argument_list|,
literal|"D"
argument_list|,
literal|"day"
argument_list|,
literal|"DAY"
argument_list|,
literal|"days"
argument_list|,
literal|"DAYS"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|hoursSet
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
literal|"h"
argument_list|,
literal|"H"
argument_list|,
literal|"hour"
argument_list|,
literal|"HOUR"
argument_list|,
literal|"hours"
argument_list|,
literal|"HOURS"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|minutesSet
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
literal|"m"
argument_list|,
literal|"M"
argument_list|,
literal|"min"
argument_list|,
literal|"MIN"
argument_list|,
literal|"mins"
argument_list|,
literal|"MINS"
argument_list|,
literal|"minute"
argument_list|,
literal|"MINUTE"
argument_list|,
literal|"minutes"
argument_list|,
literal|"MINUTES"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|secondsSet
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
literal|"s"
argument_list|,
literal|"S"
argument_list|,
literal|"sec"
argument_list|,
literal|"SEC"
argument_list|,
literal|"secs"
argument_list|,
literal|"SECS"
argument_list|,
literal|"second"
argument_list|,
literal|"SECOND"
argument_list|,
literal|"seconds"
argument_list|,
literal|"SECONDS"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|millisSet
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
literal|"ms"
argument_list|,
literal|"MS"
argument_list|,
literal|"msec"
argument_list|,
literal|"MSEC"
argument_list|,
literal|"msecs"
argument_list|,
literal|"MSECS"
argument_list|,
literal|"millisecond"
argument_list|,
literal|"MILLISECOND"
argument_list|,
literal|"milliseconds"
argument_list|,
literal|"MILLISECONDS"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|microsSet
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
literal|"us"
argument_list|,
literal|"US"
argument_list|,
literal|"usec"
argument_list|,
literal|"USEC"
argument_list|,
literal|"usecs"
argument_list|,
literal|"USECS"
argument_list|,
literal|"microsecond"
argument_list|,
literal|"MICROSECOND"
argument_list|,
literal|"microseconds"
argument_list|,
literal|"MICROSECONDS"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|nanosSet
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
literal|"ns"
argument_list|,
literal|"NS"
argument_list|,
literal|"nsec"
argument_list|,
literal|"NSEC"
argument_list|,
literal|"nsecs"
argument_list|,
literal|"NSECS"
argument_list|,
literal|"nanosecond"
argument_list|,
literal|"NANOSECOND"
argument_list|,
literal|"nanoseconds"
argument_list|,
literal|"NANOSECONDS"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|TimeUnit
name|unitFor
parameter_list|(
name|String
name|unit
parameter_list|,
name|TimeUnit
name|defaultUnit
parameter_list|)
block|{
name|unit
operator|=
name|unit
operator|.
name|trim
argument_list|()
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
if|if
condition|(
name|unit
operator|.
name|isEmpty
argument_list|()
operator|||
name|unit
operator|.
name|equals
argument_list|(
literal|"l"
argument_list|)
condition|)
block|{
if|if
condition|(
name|defaultUnit
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Time unit is not specified"
argument_list|)
throw|;
block|}
return|return
name|defaultUnit
return|;
block|}
elseif|else
if|if
condition|(
name|daysSet
operator|.
name|contains
argument_list|(
name|unit
argument_list|)
condition|)
block|{
return|return
name|TimeUnit
operator|.
name|DAYS
return|;
block|}
elseif|else
if|if
condition|(
name|hoursSet
operator|.
name|contains
argument_list|(
name|unit
argument_list|)
condition|)
block|{
return|return
name|TimeUnit
operator|.
name|HOURS
return|;
block|}
elseif|else
if|if
condition|(
name|minutesSet
operator|.
name|contains
argument_list|(
name|unit
argument_list|)
condition|)
block|{
return|return
name|TimeUnit
operator|.
name|MINUTES
return|;
block|}
elseif|else
if|if
condition|(
name|secondsSet
operator|.
name|contains
argument_list|(
name|unit
argument_list|)
condition|)
block|{
return|return
name|TimeUnit
operator|.
name|SECONDS
return|;
block|}
elseif|else
if|if
condition|(
name|millisSet
operator|.
name|contains
argument_list|(
name|unit
argument_list|)
condition|)
block|{
return|return
name|TimeUnit
operator|.
name|MILLISECONDS
return|;
block|}
elseif|else
if|if
condition|(
name|microsSet
operator|.
name|contains
argument_list|(
name|unit
argument_list|)
condition|)
block|{
return|return
name|TimeUnit
operator|.
name|MICROSECONDS
return|;
block|}
elseif|else
if|if
condition|(
name|nanosSet
operator|.
name|contains
argument_list|(
name|unit
argument_list|)
condition|)
block|{
return|return
name|TimeUnit
operator|.
name|NANOSECONDS
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid time unit "
operator|+
name|unit
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|long
name|multiplierFor
parameter_list|(
name|String
name|unit
parameter_list|)
block|{
name|unit
operator|=
name|unit
operator|.
name|trim
argument_list|()
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
if|if
condition|(
name|unit
operator|.
name|isEmpty
argument_list|()
operator|||
name|unit
operator|.
name|equals
argument_list|(
literal|"b"
argument_list|)
operator|||
name|unit
operator|.
name|equals
argument_list|(
literal|"bytes"
argument_list|)
condition|)
block|{
return|return
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|unit
operator|.
name|equals
argument_list|(
literal|"kb"
argument_list|)
condition|)
block|{
return|return
literal|1024
return|;
block|}
elseif|else
if|if
condition|(
name|unit
operator|.
name|equals
argument_list|(
literal|"mb"
argument_list|)
condition|)
block|{
return|return
literal|1024
operator|*
literal|1024
return|;
block|}
elseif|else
if|if
condition|(
name|unit
operator|.
name|equals
argument_list|(
literal|"gb"
argument_list|)
condition|)
block|{
return|return
literal|1024
operator|*
literal|1024
operator|*
literal|1024
return|;
block|}
elseif|else
if|if
condition|(
name|unit
operator|.
name|equals
argument_list|(
literal|"tb"
argument_list|)
condition|)
block|{
return|return
literal|1024L
operator|*
literal|1024
operator|*
literal|1024
operator|*
literal|1024
return|;
block|}
elseif|else
if|if
condition|(
name|unit
operator|.
name|equals
argument_list|(
literal|"pb"
argument_list|)
condition|)
block|{
return|return
literal|1024L
operator|*
literal|1024
operator|*
literal|1024
operator|*
literal|1024
operator|*
literal|1024
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid size unit "
operator|+
name|unit
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|String
name|stringFor
parameter_list|(
name|TimeUnit
name|timeunit
parameter_list|)
block|{
switch|switch
condition|(
name|timeunit
condition|)
block|{
case|case
name|DAYS
case|:
return|return
literal|"day"
return|;
case|case
name|HOURS
case|:
return|return
literal|"hour"
return|;
case|case
name|MINUTES
case|:
return|return
literal|"min"
return|;
case|case
name|SECONDS
case|:
return|return
literal|"sec"
return|;
case|case
name|MILLISECONDS
case|:
return|return
literal|"msec"
return|;
case|case
name|MICROSECONDS
case|:
return|return
literal|"usec"
return|;
case|case
name|NANOSECONDS
case|:
return|return
literal|"nsec"
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid timeunit "
operator|+
name|timeunit
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|long
name|getLongVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|Long
operator|.
name|class
operator|)
operator|:
name|var
operator|.
name|varname
assert|;
if|if
condition|(
name|var
operator|.
name|altName
operator|!=
literal|null
condition|)
block|{
return|return
name|conf
operator|.
name|getLong
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
name|var
operator|.
name|altName
argument_list|,
name|var
operator|.
name|defaultLongVal
argument_list|)
argument_list|)
return|;
block|}
return|return
name|conf
operator|.
name|getLong
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|var
operator|.
name|defaultLongVal
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|long
name|getLongVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|,
name|long
name|defaultVal
parameter_list|)
block|{
if|if
condition|(
name|var
operator|.
name|altName
operator|!=
literal|null
condition|)
block|{
return|return
name|conf
operator|.
name|getLong
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
name|var
operator|.
name|altName
argument_list|,
name|defaultVal
argument_list|)
argument_list|)
return|;
block|}
return|return
name|conf
operator|.
name|getLong
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|defaultVal
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|setLongVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|,
name|long
name|val
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|Long
operator|.
name|class
operator|)
operator|:
name|var
operator|.
name|varname
assert|;
name|conf
operator|.
name|setLong
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|getLongVar
parameter_list|(
name|ConfVars
name|var
parameter_list|)
block|{
return|return
name|getLongVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|)
return|;
block|}
specifier|public
name|void
name|setLongVar
parameter_list|(
name|ConfVars
name|var
parameter_list|,
name|long
name|val
parameter_list|)
block|{
name|setLongVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|float
name|getFloatVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|Float
operator|.
name|class
operator|)
operator|:
name|var
operator|.
name|varname
assert|;
if|if
condition|(
name|var
operator|.
name|altName
operator|!=
literal|null
condition|)
block|{
return|return
name|conf
operator|.
name|getFloat
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getFloat
argument_list|(
name|var
operator|.
name|altName
argument_list|,
name|var
operator|.
name|defaultFloatVal
argument_list|)
argument_list|)
return|;
block|}
return|return
name|conf
operator|.
name|getFloat
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|var
operator|.
name|defaultFloatVal
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|float
name|getFloatVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|,
name|float
name|defaultVal
parameter_list|)
block|{
if|if
condition|(
name|var
operator|.
name|altName
operator|!=
literal|null
condition|)
block|{
return|return
name|conf
operator|.
name|getFloat
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getFloat
argument_list|(
name|var
operator|.
name|altName
argument_list|,
name|defaultVal
argument_list|)
argument_list|)
return|;
block|}
return|return
name|conf
operator|.
name|getFloat
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|defaultVal
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|setFloatVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|,
name|float
name|val
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|Float
operator|.
name|class
operator|)
operator|:
name|var
operator|.
name|varname
assert|;
name|conf
operator|.
name|setFloat
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
name|float
name|getFloatVar
parameter_list|(
name|ConfVars
name|var
parameter_list|)
block|{
return|return
name|getFloatVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|)
return|;
block|}
specifier|public
name|void
name|setFloatVar
parameter_list|(
name|ConfVars
name|var
parameter_list|,
name|float
name|val
parameter_list|)
block|{
name|setFloatVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|boolean
name|getBoolVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|Boolean
operator|.
name|class
operator|)
operator|:
name|var
operator|.
name|varname
assert|;
if|if
condition|(
name|var
operator|.
name|altName
operator|!=
literal|null
condition|)
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|var
operator|.
name|altName
argument_list|,
name|var
operator|.
name|defaultBoolVal
argument_list|)
argument_list|)
return|;
block|}
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|var
operator|.
name|defaultBoolVal
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|getBoolVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|,
name|boolean
name|defaultVal
parameter_list|)
block|{
if|if
condition|(
name|var
operator|.
name|altName
operator|!=
literal|null
condition|)
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getBoolean
argument_list|(
name|var
operator|.
name|altName
argument_list|,
name|defaultVal
argument_list|)
argument_list|)
return|;
block|}
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|defaultVal
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|setBoolVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|,
name|boolean
name|val
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|Boolean
operator|.
name|class
operator|)
operator|:
name|var
operator|.
name|varname
assert|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
comment|/* Dynamic partition pruning is enabled in some or all cases if either    * hive.spark.dynamic.partition.pruning is true or    * hive.spark.dynamic.partition.pruning.map.join.only is true    */
specifier|public
specifier|static
name|boolean
name|isSparkDPPAny
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|ConfVars
operator|.
name|SPARK_DYNAMIC_PARTITION_PRUNING
operator|.
name|varname
argument_list|,
name|ConfVars
operator|.
name|SPARK_DYNAMIC_PARTITION_PRUNING
operator|.
name|defaultBoolVal
argument_list|)
operator|||
name|conf
operator|.
name|getBoolean
argument_list|(
name|ConfVars
operator|.
name|SPARK_DYNAMIC_PARTITION_PRUNING_MAP_JOIN_ONLY
operator|.
name|varname
argument_list|,
name|ConfVars
operator|.
name|SPARK_DYNAMIC_PARTITION_PRUNING_MAP_JOIN_ONLY
operator|.
name|defaultBoolVal
argument_list|)
operator|)
return|;
block|}
specifier|public
name|boolean
name|getBoolVar
parameter_list|(
name|ConfVars
name|var
parameter_list|)
block|{
return|return
name|getBoolVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|)
return|;
block|}
specifier|public
name|void
name|setBoolVar
parameter_list|(
name|ConfVars
name|var
parameter_list|,
name|boolean
name|val
parameter_list|)
block|{
name|setBoolVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|String
name|getVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|String
operator|.
name|class
operator|)
operator|:
name|var
operator|.
name|varname
assert|;
return|return
name|var
operator|.
name|altName
operator|!=
literal|null
condition|?
name|conf
operator|.
name|get
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|var
operator|.
name|altName
argument_list|,
name|var
operator|.
name|defaultStrVal
argument_list|)
argument_list|)
else|:
name|conf
operator|.
name|get
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|var
operator|.
name|defaultStrVal
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getVarWithoutType
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|)
block|{
return|return
name|var
operator|.
name|altName
operator|!=
literal|null
condition|?
name|conf
operator|.
name|get
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|var
operator|.
name|altName
argument_list|,
name|var
operator|.
name|defaultExpr
argument_list|)
argument_list|)
else|:
name|conf
operator|.
name|get
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|var
operator|.
name|defaultExpr
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getTrimmedVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|String
operator|.
name|class
operator|)
operator|:
name|var
operator|.
name|varname
assert|;
if|if
condition|(
name|var
operator|.
name|altName
operator|!=
literal|null
condition|)
block|{
return|return
name|conf
operator|.
name|getTrimmed
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getTrimmed
argument_list|(
name|var
operator|.
name|altName
argument_list|,
name|var
operator|.
name|defaultStrVal
argument_list|)
argument_list|)
return|;
block|}
return|return
name|conf
operator|.
name|getTrimmed
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|var
operator|.
name|defaultStrVal
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
index|[]
name|getTrimmedStringsVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|String
operator|.
name|class
operator|)
operator|:
name|var
operator|.
name|varname
assert|;
name|String
index|[]
name|result
init|=
name|conf
operator|.
name|getTrimmedStrings
argument_list|(
name|var
operator|.
name|varname
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
return|return
name|result
return|;
block|}
if|if
condition|(
name|var
operator|.
name|altName
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|conf
operator|.
name|getTrimmedStrings
argument_list|(
name|var
operator|.
name|altName
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
return|return
name|result
return|;
block|}
block|}
return|return
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|getTrimmedStrings
argument_list|(
name|var
operator|.
name|defaultStrVal
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|,
name|String
name|defaultVal
parameter_list|)
block|{
name|String
name|ret
init|=
name|var
operator|.
name|altName
operator|!=
literal|null
condition|?
name|conf
operator|.
name|get
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|var
operator|.
name|altName
argument_list|,
name|defaultVal
argument_list|)
argument_list|)
else|:
name|conf
operator|.
name|get
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|defaultVal
argument_list|)
decl_stmt|;
return|return
name|ret
return|;
block|}
specifier|public
specifier|static
name|String
name|getVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|,
name|EncoderDecoder
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|encoderDecoder
parameter_list|)
block|{
return|return
name|encoderDecoder
operator|.
name|decode
argument_list|(
name|getVar
argument_list|(
name|conf
argument_list|,
name|var
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|String
name|getLogIdVar
parameter_list|(
name|String
name|defaultValue
parameter_list|)
block|{
name|String
name|retval
init|=
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_LOG_TRACE_ID
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|EMPTY
operator|.
name|equals
argument_list|(
name|retval
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Using the default value passed in for log id: {}"
argument_list|,
name|defaultValue
argument_list|)
expr_stmt|;
block|}
name|retval
operator|=
name|defaultValue
expr_stmt|;
block|}
if|if
condition|(
name|retval
operator|.
name|length
argument_list|()
operator|>
name|LOG_PREFIX_LENGTH
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"The original log id prefix is {} has been truncated to {}"
argument_list|,
name|retval
argument_list|,
name|retval
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|LOG_PREFIX_LENGTH
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|retval
operator|=
name|retval
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|LOG_PREFIX_LENGTH
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|retval
return|;
block|}
specifier|public
specifier|static
name|void
name|setVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|,
name|String
name|val
parameter_list|)
block|{
assert|assert
operator|(
name|var
operator|.
name|valClass
operator|==
name|String
operator|.
name|class
operator|)
operator|:
name|var
operator|.
name|varname
assert|;
name|conf
operator|.
name|set
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|val
argument_list|,
literal|"setVar"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|setVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|var
parameter_list|,
name|String
name|val
parameter_list|,
name|EncoderDecoder
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|encoderDecoder
parameter_list|)
block|{
name|setVar
argument_list|(
name|conf
argument_list|,
name|var
argument_list|,
name|encoderDecoder
operator|.
name|encode
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|ConfVars
name|getConfVars
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|vars
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ConfVars
name|getMetaConf
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|metaConfs
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|public
name|String
name|getVar
parameter_list|(
name|ConfVars
name|var
parameter_list|)
block|{
return|return
name|getVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|)
return|;
block|}
specifier|public
name|void
name|setVar
parameter_list|(
name|ConfVars
name|var
parameter_list|,
name|String
name|val
parameter_list|)
block|{
name|setVar
argument_list|(
name|this
argument_list|,
name|var
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getQueryString
parameter_list|()
block|{
return|return
name|getQueryString
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getQueryString
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVEQUERYSTRING
argument_list|,
name|EncoderDecoderFactory
operator|.
name|URL_ENCODER_DECODER
argument_list|)
return|;
block|}
specifier|public
name|void
name|setQueryString
parameter_list|(
name|String
name|query
parameter_list|)
block|{
name|setQueryString
argument_list|(
name|this
argument_list|,
name|query
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|setQueryString
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|query
parameter_list|)
block|{
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVEQUERYSTRING
argument_list|,
name|query
argument_list|,
name|EncoderDecoderFactory
operator|.
name|URL_ENCODER_DECODER
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|logVars
parameter_list|(
name|PrintStream
name|ps
parameter_list|)
block|{
for|for
control|(
name|ConfVars
name|one
range|:
name|ConfVars
operator|.
name|values
argument_list|()
control|)
block|{
name|ps
operator|.
name|println
argument_list|(
name|one
operator|.
name|varname
operator|+
literal|"="
operator|+
operator|(
operator|(
name|get
argument_list|(
name|one
operator|.
name|varname
argument_list|)
operator|!=
literal|null
operator|)
condition|?
name|get
argument_list|(
name|one
operator|.
name|varname
argument_list|)
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @return a ZooKeeperHiveHelper instance containing the ZooKeeper specifications from the    * given HiveConf.    */
specifier|public
name|ZooKeeperHiveHelper
name|getZKConfig
parameter_list|()
block|{
return|return
operator|new
name|ZooKeeperHiveHelper
argument_list|(
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_QUORUM
argument_list|)
argument_list|,
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_CLIENT_PORT
argument_list|)
argument_list|,
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ZOOKEEPER_NAMESPACE
argument_list|)
argument_list|,
operator|(
name|int
operator|)
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_SESSION_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
operator|(
name|int
operator|)
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_CONNECTION_BASESLEEPTIME
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|,
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_CONNECTION_MAX_RETRIES
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|HiveConf
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|initialize
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveConf
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|cls
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|initialize
argument_list|(
name|cls
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveConf
parameter_list|(
name|Configuration
name|other
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|cls
parameter_list|)
block|{
name|super
argument_list|(
name|other
argument_list|)
expr_stmt|;
name|initialize
argument_list|(
name|cls
argument_list|)
expr_stmt|;
block|}
comment|/**    * Copy constructor    */
specifier|public
name|HiveConf
parameter_list|(
name|HiveConf
name|other
parameter_list|)
block|{
name|super
argument_list|(
name|other
argument_list|)
expr_stmt|;
name|hiveJar
operator|=
name|other
operator|.
name|hiveJar
expr_stmt|;
name|auxJars
operator|=
name|other
operator|.
name|auxJars
expr_stmt|;
name|isSparkConfigUpdated
operator|=
name|other
operator|.
name|isSparkConfigUpdated
expr_stmt|;
name|origProp
operator|=
operator|(
name|Properties
operator|)
name|other
operator|.
name|origProp
operator|.
name|clone
argument_list|()
expr_stmt|;
name|restrictList
operator|.
name|addAll
argument_list|(
name|other
operator|.
name|restrictList
argument_list|)
expr_stmt|;
name|hiddenSet
operator|.
name|addAll
argument_list|(
name|other
operator|.
name|hiddenSet
argument_list|)
expr_stmt|;
name|modWhiteListPattern
operator|=
name|other
operator|.
name|modWhiteListPattern
expr_stmt|;
block|}
specifier|public
name|Properties
name|getAllProperties
parameter_list|()
block|{
return|return
name|getProperties
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Properties
name|getProperties
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|iter
init|=
name|conf
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Properties
name|p
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|p
operator|.
name|setProperty
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|p
return|;
block|}
specifier|private
name|void
name|initialize
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|cls
parameter_list|)
block|{
name|hiveJar
operator|=
operator|(
operator|new
name|JobConf
argument_list|(
name|cls
argument_list|)
operator|)
operator|.
name|getJar
argument_list|()
expr_stmt|;
comment|// preserve the original configuration
name|origProp
operator|=
name|getAllProperties
argument_list|()
expr_stmt|;
comment|// Overlay the ConfVars. Note that this ignores ConfVars with null values
name|addResource
argument_list|(
name|getConfVarInputStream
argument_list|()
argument_list|,
literal|"HiveConf.java"
argument_list|)
expr_stmt|;
comment|// Overlay hive-site.xml if it exists
if|if
condition|(
name|hiveSiteURL
operator|!=
literal|null
condition|)
block|{
name|addResource
argument_list|(
name|hiveSiteURL
argument_list|)
expr_stmt|;
block|}
comment|// if embedded metastore is to be used as per config so far
comment|// then this is considered like the metastore server case
name|String
name|msUri
init|=
name|this
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|)
decl_stmt|;
comment|// This is hackery, but having hive-common depend on standalone-metastore is really bad
comment|// because it will pull all of the metastore code into every module.  We need to check that
comment|// we aren't using the standalone metastore.  If we are, we should treat it the same as a
comment|// remote metastore situation.
if|if
condition|(
name|msUri
operator|==
literal|null
operator|||
name|msUri
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|msUri
operator|=
name|this
operator|.
name|get
argument_list|(
literal|"metastore.thrift.uris"
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Found metastore URI of "
operator|+
name|msUri
argument_list|)
expr_stmt|;
if|if
condition|(
name|HiveConfUtil
operator|.
name|isEmbeddedMetaStore
argument_list|(
name|msUri
argument_list|)
condition|)
block|{
name|setLoadMetastoreConfig
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// load hivemetastore-site.xml if this is metastore and file exists
if|if
condition|(
name|isLoadMetastoreConfig
argument_list|()
operator|&&
name|hivemetastoreSiteUrl
operator|!=
literal|null
condition|)
block|{
name|addResource
argument_list|(
name|hivemetastoreSiteUrl
argument_list|)
expr_stmt|;
block|}
comment|// load hiveserver2-site.xml if this is hiveserver2 and file exists
comment|// metastore can be embedded within hiveserver2, in such cases
comment|// the conf params in hiveserver2-site.xml will override whats defined
comment|// in hivemetastore-site.xml
if|if
condition|(
name|isLoadHiveServer2Config
argument_list|()
condition|)
block|{
comment|// set the hardcoded value first, so anything in hiveserver2-site.xml can override it
name|set
argument_list|(
name|ConfVars
operator|.
name|METASTORE_CLIENT_CAPABILITIES
operator|.
name|varname
argument_list|,
literal|"EXTWRITE,EXTREAD,HIVEBUCKET2,HIVEFULLACIDREAD,"
operator|+
literal|"HIVEFULLACIDWRITE,HIVECACHEINVALIDATE,HIVEMANAGESTATS,HIVEMANAGEDINSERTWRITE,HIVEMANAGEDINSERTREAD,"
operator|+
literal|"HIVESQL,HIVEMQT,HIVEONLYMQTWRITE"
argument_list|)
expr_stmt|;
if|if
condition|(
name|hiveServer2SiteUrl
operator|!=
literal|null
condition|)
block|{
name|addResource
argument_list|(
name|hiveServer2SiteUrl
argument_list|)
expr_stmt|;
block|}
block|}
name|String
name|val
init|=
name|this
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ADDITIONAL_CONFIG_FILES
argument_list|)
decl_stmt|;
name|ClassLoader
name|classLoader
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
decl_stmt|;
if|if
condition|(
name|val
operator|!=
literal|null
operator|&&
operator|!
name|val
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|String
index|[]
name|configFiles
init|=
name|val
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|config
range|:
name|configFiles
control|)
block|{
name|URL
name|configURL
init|=
name|findConfigFile
argument_list|(
name|classLoader
argument_list|,
name|config
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|configURL
operator|!=
literal|null
condition|)
block|{
name|addResource
argument_list|(
name|configURL
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Overlay the values of any system properties and manual overrides
name|applySystemProperties
argument_list|()
expr_stmt|;
if|if
condition|(
operator|(
name|this
operator|.
name|get
argument_list|(
literal|"hive.metastore.ds.retry.attempts"
argument_list|)
operator|!=
literal|null
operator|)
operator|||
name|this
operator|.
name|get
argument_list|(
literal|"hive.metastore.ds.retry.interval"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"DEPRECATED: hive.metastore.ds.retry.* no longer has any effect.  "
operator|+
literal|"Use hive.hmshandler.retry.* instead"
argument_list|)
expr_stmt|;
block|}
comment|// if the running class was loaded directly (through eclipse) rather than through a
comment|// jar then this would be needed
if|if
condition|(
name|hiveJar
operator|==
literal|null
condition|)
block|{
name|hiveJar
operator|=
name|this
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVEJAR
operator|.
name|varname
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|auxJars
operator|==
literal|null
condition|)
block|{
name|auxJars
operator|=
name|StringUtils
operator|.
name|join
argument_list|(
name|FileUtils
operator|.
name|getJarFilesByPath
argument_list|(
name|this
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVEAUXJARS
operator|.
name|varname
argument_list|)
argument_list|,
name|this
argument_list|)
argument_list|,
literal|','
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION
argument_list|)
condition|)
block|{
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|METASTORE_AUTO_CREATE_ALL
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECONFVALIDATION
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|trimmed
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|this
control|)
block|{
name|String
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|==
literal|null
operator|||
operator|!
name|key
operator|.
name|startsWith
argument_list|(
literal|"hive."
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|ConfVars
name|var
init|=
name|HiveConf
operator|.
name|getConfVars
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|var
operator|==
literal|null
condition|)
block|{
name|var
operator|=
name|HiveConf
operator|.
name|getConfVars
argument_list|(
name|key
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|var
operator|!=
literal|null
condition|)
block|{
name|trimmed
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|var
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"HiveConf of name {} does not exist"
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|var
operator|.
name|isType
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"HiveConf {} expects {} type value"
argument_list|,
name|var
operator|.
name|varname
argument_list|,
name|var
operator|.
name|typeString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|String
name|key
range|:
name|trimmed
control|)
block|{
name|set
argument_list|(
name|key
operator|.
name|trim
argument_list|()
argument_list|,
name|getRaw
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|unset
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
name|setupSQLStdAuthWhiteList
argument_list|()
expr_stmt|;
comment|// setup list of conf vars that are not allowed to change runtime
name|setupRestrictList
argument_list|()
expr_stmt|;
name|hiddenSet
operator|.
name|clear
argument_list|()
expr_stmt|;
name|hiddenSet
operator|.
name|addAll
argument_list|(
name|HiveConfUtil
operator|.
name|getHiddenSet
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
name|setupRSCList
argument_list|()
expr_stmt|;
block|}
comment|/**    * If the config whitelist param for sql standard authorization is not set, set it up here.    */
specifier|private
name|void
name|setupSQLStdAuthWhiteList
parameter_list|()
block|{
name|String
name|whiteListParamsStr
init|=
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_SQL_STD_AUTH_CONFIG_WHITELIST
argument_list|)
decl_stmt|;
if|if
condition|(
name|whiteListParamsStr
operator|==
literal|null
operator|||
name|whiteListParamsStr
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// set the default configs in whitelist
name|whiteListParamsStr
operator|=
name|getSQLStdAuthDefaultWhiteListPattern
argument_list|()
expr_stmt|;
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_SQL_STD_AUTH_CONFIG_WHITELIST
argument_list|,
name|whiteListParamsStr
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|String
name|getSQLStdAuthDefaultWhiteListPattern
parameter_list|()
block|{
comment|// create the default white list from list of safe config params
comment|// and regex list
name|String
name|confVarPatternStr
init|=
name|Joiner
operator|.
name|on
argument_list|(
literal|"|"
argument_list|)
operator|.
name|join
argument_list|(
name|convertVarsToRegex
argument_list|(
name|SQL_STD_AUTH_SAFE_VAR_NAMES
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|regexPatternStr
init|=
name|Joiner
operator|.
name|on
argument_list|(
literal|"|"
argument_list|)
operator|.
name|join
argument_list|(
name|sqlStdAuthSafeVarNameRegexes
argument_list|)
decl_stmt|;
return|return
name|regexPatternStr
operator|+
literal|"|"
operator|+
name|confVarPatternStr
return|;
block|}
comment|/**    * Obtains the local time-zone ID.    */
specifier|public
name|ZoneId
name|getLocalTimeZone
parameter_list|()
block|{
name|String
name|timeZoneStr
init|=
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_LOCAL_TIME_ZONE
argument_list|)
decl_stmt|;
return|return
name|TimestampTZUtil
operator|.
name|parseTimeZone
argument_list|(
name|timeZoneStr
argument_list|)
return|;
block|}
comment|/**    * @param paramList  list of parameter strings    * @return list of parameter strings with "." replaced by "\."    */
specifier|private
specifier|static
name|String
index|[]
name|convertVarsToRegex
parameter_list|(
name|String
index|[]
name|paramList
parameter_list|)
block|{
name|String
index|[]
name|regexes
init|=
operator|new
name|String
index|[
name|paramList
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|paramList
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|regexes
index|[
name|i
index|]
operator|=
name|paramList
index|[
name|i
index|]
operator|.
name|replace
argument_list|(
literal|"."
argument_list|,
literal|"\\."
argument_list|)
expr_stmt|;
block|}
return|return
name|regexes
return|;
block|}
comment|/**    * Default list of modifiable config parameters for sql standard authorization    * For internal use only.    */
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|SQL_STD_AUTH_SAFE_VAR_NAMES
init|=
operator|new
name|String
index|[]
block|{
name|ConfVars
operator|.
name|AGGR_JOIN_TRANSPOSE
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|BYTESPERREDUCER
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|CLIENT_STATS_COUNTERS
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|DEFAULTPARTITIONNAME
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|DROP_IGNORES_NON_EXISTENT
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVECOUNTERGROUP
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEDEFAULTMANAGEDFILEFORMAT
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEENFORCEBUCKETMAPJOIN
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEENFORCESORTMERGEBUCKETMAPJOIN
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEEXPREVALUATIONCACHE
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEQUERYRESULTFILEFORMAT
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEHASHTABLELOADFACTOR
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEHASHTABLETHRESHOLD
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEIGNOREMAPJOINHINT
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVELIMITMAXROWSIZE
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEMAPREDMODE
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEMAPSIDEAGGREGATE
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEOPTIMIZEMETADATAQUERIES
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEROWOFFSET
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEVARIABLESUBSTITUTE
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEVARIABLESUBSTITUTEDEPTH
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_AUTOGEN_COLUMNALIAS_PREFIX_INCLUDEFUNCNAME
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_AUTOGEN_COLUMNALIAS_PREFIX_LABEL
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_CHECK_CROSS_PRODUCT
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_CLI_TEZ_SESSION_ASYNC
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_COMPAT
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_DISPLAY_PARTITION_COLUMNS_SEPARATELY
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_ERROR_ON_EMPTY_PARTITION
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_EXEC_COPYFILE_MAXSIZE
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_EXIM_URI_SCHEME_WL
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_FILE_MAX_FOOTER
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_INSERT_INTO_MULTILEVEL_DIRS
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_LOCALIZE_RESOURCE_NUM_WAIT_ATTEMPTS
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_MULTI_INSERT_MOVE_TASKS_SHARE_DEPENDENCIES
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_QUERY_RESULTS_CACHE_ENABLED
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_QUERY_RESULTS_CACHE_WAIT_FOR_PENDING_RESULTS
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_QUOTEDID_SUPPORT
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_RESULTSET_USE_UNIQUE_COLUMN_NAMES
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_STATS_COLLECT_PART_LEVEL_STATS
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_SCHEMA_EVOLUTION
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_SERVER2_LOGGING_OPERATION_LEVEL
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_RESULTSET_SERIALIZE_IN_TASKS
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVE_SUPPORT_SPECICAL_CHARACTERS_IN_TABLE_NAMES
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|JOB_DEBUG_CAPTURE_STACKTRACES
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|JOB_DEBUG_TIMEOUT
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|LLAP_IO_ENABLED
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|LLAP_IO_USE_FILEID_PATH
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|LLAP_DAEMON_SERVICE_HOSTS
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|LLAP_EXECUTION_MODE
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|LLAP_AUTO_ALLOW_UBER
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|LLAP_AUTO_ENFORCE_TREE
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|LLAP_AUTO_ENFORCE_VECTORIZED
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|LLAP_AUTO_ENFORCE_STATS
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|LLAP_AUTO_MAX_INPUT
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|LLAP_AUTO_MAX_OUTPUT
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|LLAP_SKIP_COMPILE_UDF_CHECK
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|LLAP_CLIENT_CONSISTENT_SPLITS
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|LLAP_ENABLE_GRACE_JOIN_IN_LLAP
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|LLAP_ALLOW_PERMANENT_FNS
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|MAXCREATEDFILES
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|MAXREDUCERS
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|NWAYJOINREORDER
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|OUTPUT_FILE_EXTENSION
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|SHOW_JOB_FAIL_DEBUG_INFO
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|TASKLOG_DEBUG_TIMEOUT
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEQUERYID
operator|.
name|varname
block|,
name|ConfVars
operator|.
name|HIVEQUERYTAG
operator|.
name|varname
block|,   }
decl_stmt|;
comment|/**    * Default list of regexes for config parameters that are modifiable with    * sql standard authorization enabled    */
specifier|static
specifier|final
name|String
index|[]
name|sqlStdAuthSafeVarNameRegexes
init|=
operator|new
name|String
index|[]
block|{
literal|"hive\\.auto\\..*"
block|,
literal|"hive\\.cbo\\..*"
block|,
literal|"hive\\.convert\\..*"
block|,
literal|"hive\\.druid\\..*"
block|,
literal|"hive\\.exec\\.dynamic\\.partition.*"
block|,
literal|"hive\\.exec\\.max\\.dynamic\\.partitions.*"
block|,
literal|"hive\\.exec\\.compress\\..*"
block|,
literal|"hive\\.exec\\.infer\\..*"
block|,
literal|"hive\\.exec\\.mode.local\\..*"
block|,
literal|"hive\\.exec\\.orc\\..*"
block|,
literal|"hive\\.exec\\.parallel.*"
block|,
literal|"hive\\.explain\\..*"
block|,
literal|"hive\\.fetch.task\\..*"
block|,
literal|"hive\\.groupby\\..*"
block|,
literal|"hive\\.hbase\\..*"
block|,
literal|"hive\\.index\\..*"
block|,
literal|"hive\\.index\\..*"
block|,
literal|"hive\\.intermediate\\..*"
block|,
literal|"hive\\.jdbc\\..*"
block|,
literal|"hive\\.join\\..*"
block|,
literal|"hive\\.limit\\..*"
block|,
literal|"hive\\.log\\..*"
block|,
literal|"hive\\.mapjoin\\..*"
block|,
literal|"hive\\.merge\\..*"
block|,
literal|"hive\\.optimize\\..*"
block|,
literal|"hive\\.materializedview\\..*"
block|,
literal|"hive\\.orc\\..*"
block|,
literal|"hive\\.outerjoin\\..*"
block|,
literal|"hive\\.parquet\\..*"
block|,
literal|"hive\\.ppd\\..*"
block|,
literal|"hive\\.prewarm\\..*"
block|,
literal|"hive\\.server2\\.thrift\\.resultset\\.default\\.fetch\\.size"
block|,
literal|"hive\\.server2\\.proxy\\.user"
block|,
literal|"hive\\.skewjoin\\..*"
block|,
literal|"hive\\.smbjoin\\..*"
block|,
literal|"hive\\.stats\\..*"
block|,
literal|"hive\\.strict\\..*"
block|,
literal|"hive\\.tez\\..*"
block|,
literal|"hive\\.vectorized\\..*"
block|,
literal|"hive\\.query\\.reexecution\\..*"
block|,
literal|"hive\\.query\\.exclusive\\.lock"
block|,
literal|"reexec\\.overlay\\..*"
block|,
literal|"fs\\.defaultFS"
block|,
literal|"ssl\\.client\\.truststore\\.location"
block|,
literal|"distcp\\.atomic"
block|,
literal|"distcp\\.ignore\\.failures"
block|,
literal|"distcp\\.preserve\\.status"
block|,
literal|"distcp\\.preserve\\.rawxattrs"
block|,
literal|"distcp\\.sync\\.folders"
block|,
literal|"distcp\\.delete\\.missing\\.source"
block|,
literal|"distcp\\.keystore\\.resource"
block|,
literal|"distcp\\.liststatus\\.threads"
block|,
literal|"distcp\\.max\\.maps"
block|,
literal|"distcp\\.copy\\.strategy"
block|,
literal|"distcp\\.skip\\.crc"
block|,
literal|"distcp\\.copy\\.overwrite"
block|,
literal|"distcp\\.copy\\.append"
block|,
literal|"distcp\\.map\\.bandwidth\\.mb"
block|,
literal|"distcp\\.dynamic\\..*"
block|,
literal|"distcp\\.meta\\.folder"
block|,
literal|"distcp\\.copy\\.listing\\.class"
block|,
literal|"distcp\\.filters\\.class"
block|,
literal|"distcp\\.options\\.skipcrccheck"
block|,
literal|"distcp\\.options\\.m"
block|,
literal|"distcp\\.options\\.numListstatusThreads"
block|,
literal|"distcp\\.options\\.mapredSslConf"
block|,
literal|"distcp\\.options\\.bandwidth"
block|,
literal|"distcp\\.options\\.overwrite"
block|,
literal|"distcp\\.options\\.strategy"
block|,
literal|"distcp\\.options\\.i"
block|,
literal|"distcp\\.options\\.p.*"
block|,
literal|"distcp\\.options\\.update"
block|,
literal|"distcp\\.options\\.delete"
block|,
literal|"mapred\\.map\\..*"
block|,
literal|"mapred\\.reduce\\..*"
block|,
literal|"mapred\\.output\\.compression\\.codec"
block|,
literal|"mapred\\.job\\.queue\\.name"
block|,
literal|"mapred\\.output\\.compression\\.type"
block|,
literal|"mapred\\.min\\.split\\.size"
block|,
literal|"mapreduce\\.job\\.reduce\\.slowstart\\.completedmaps"
block|,
literal|"mapreduce\\.job\\.queuename"
block|,
literal|"mapreduce\\.job\\.tags"
block|,
literal|"mapreduce\\.input\\.fileinputformat\\.split\\.minsize"
block|,
literal|"mapreduce\\.map\\..*"
block|,
literal|"mapreduce\\.reduce\\..*"
block|,
literal|"mapreduce\\.output\\.fileoutputformat\\.compress\\.codec"
block|,
literal|"mapreduce\\.output\\.fileoutputformat\\.compress\\.type"
block|,
literal|"oozie\\..*"
block|,
literal|"tez\\.am\\..*"
block|,
literal|"tez\\.task\\..*"
block|,
literal|"tez\\.runtime\\..*"
block|,
literal|"tez\\.queue\\.name"
block|,    }
decl_stmt|;
comment|//Take care of conf overrides.
comment|//Includes values in ConfVars as well as underlying configuration properties (ie, hadoop)
specifier|public
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|overrides
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Apply system properties to this object if the property name is defined in ConfVars    * and the value is non-null and not an empty string.    */
specifier|private
name|void
name|applySystemProperties
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|systemProperties
init|=
name|getConfSystemProperties
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|systemProperty
range|:
name|systemProperties
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|this
operator|.
name|set
argument_list|(
name|systemProperty
operator|.
name|getKey
argument_list|()
argument_list|,
name|systemProperty
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This method returns a mapping from config variable name to its value for all config variables    * which have been set using System properties    */
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getConfSystemProperties
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|systemProperties
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ConfVars
name|oneVar
range|:
name|ConfVars
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
name|oneVar
operator|.
name|varname
argument_list|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
name|oneVar
operator|.
name|varname
argument_list|)
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|systemProperties
operator|.
name|put
argument_list|(
name|oneVar
operator|.
name|varname
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
name|oneVar
operator|.
name|varname
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|oneVar
range|:
name|overrides
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|overrides
operator|.
name|get
argument_list|(
name|oneVar
operator|.
name|getKey
argument_list|()
argument_list|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|overrides
operator|.
name|get
argument_list|(
name|oneVar
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|systemProperties
operator|.
name|put
argument_list|(
name|oneVar
operator|.
name|getKey
argument_list|()
argument_list|,
name|oneVar
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|systemProperties
return|;
block|}
comment|/**    * Overlays ConfVar properties with non-null values    */
specifier|private
specifier|static
name|void
name|applyDefaultNonNullConfVars
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
for|for
control|(
name|ConfVars
name|var
range|:
name|ConfVars
operator|.
name|values
argument_list|()
control|)
block|{
name|String
name|defaultValue
init|=
name|var
operator|.
name|getDefaultValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|defaultValue
operator|==
literal|null
condition|)
block|{
comment|// Don't override ConfVars with null values
continue|continue;
block|}
name|conf
operator|.
name|set
argument_list|(
name|var
operator|.
name|varname
argument_list|,
name|defaultValue
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Properties
name|getChangedProperties
parameter_list|()
block|{
name|Properties
name|ret
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|Properties
name|newProp
init|=
name|getAllProperties
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|one
range|:
name|newProp
operator|.
name|keySet
argument_list|()
control|)
block|{
name|String
name|oneProp
init|=
operator|(
name|String
operator|)
name|one
decl_stmt|;
name|String
name|oldValue
init|=
name|origProp
operator|.
name|getProperty
argument_list|(
name|oneProp
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|StringUtils
operator|.
name|equals
argument_list|(
name|oldValue
argument_list|,
name|newProp
operator|.
name|getProperty
argument_list|(
name|oneProp
argument_list|)
argument_list|)
condition|)
block|{
name|ret
operator|.
name|setProperty
argument_list|(
name|oneProp
argument_list|,
name|newProp
operator|.
name|getProperty
argument_list|(
name|oneProp
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|(
name|ret
operator|)
return|;
block|}
specifier|public
name|String
name|getJar
parameter_list|()
block|{
return|return
name|hiveJar
return|;
block|}
comment|/**    * @return the auxJars    */
specifier|public
name|String
name|getAuxJars
parameter_list|()
block|{
return|return
name|auxJars
return|;
block|}
comment|/**    * Set the auxiliary jars. Used for unit tests only.    * @param auxJars the auxJars to set.    */
specifier|public
name|void
name|setAuxJars
parameter_list|(
name|String
name|auxJars
parameter_list|)
block|{
name|this
operator|.
name|auxJars
operator|=
name|auxJars
expr_stmt|;
name|setVar
argument_list|(
name|this
argument_list|,
name|ConfVars
operator|.
name|HIVEAUXJARS
argument_list|,
name|auxJars
argument_list|)
expr_stmt|;
block|}
specifier|public
name|URL
name|getHiveDefaultLocation
parameter_list|()
block|{
return|return
name|hiveDefaultURL
return|;
block|}
specifier|public
specifier|static
name|void
name|setHiveSiteLocation
parameter_list|(
name|URL
name|location
parameter_list|)
block|{
name|hiveSiteURL
operator|=
name|location
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|setHivemetastoreSiteUrl
parameter_list|(
name|URL
name|location
parameter_list|)
block|{
name|hivemetastoreSiteUrl
operator|=
name|location
expr_stmt|;
block|}
specifier|public
specifier|static
name|URL
name|getHiveSiteLocation
parameter_list|()
block|{
return|return
name|hiveSiteURL
return|;
block|}
specifier|public
specifier|static
name|URL
name|getMetastoreSiteLocation
parameter_list|()
block|{
return|return
name|hivemetastoreSiteUrl
return|;
block|}
specifier|public
specifier|static
name|URL
name|getHiveServer2SiteLocation
parameter_list|()
block|{
return|return
name|hiveServer2SiteUrl
return|;
block|}
comment|/**    * @return the user name set in hadoop.job.ugi param or the current user from System    * @throws IOException    */
specifier|public
name|String
name|getUser
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|UserGroupInformation
name|ugi
init|=
name|Utils
operator|.
name|getUGI
argument_list|()
decl_stmt|;
return|return
name|ugi
operator|.
name|getUserName
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|LoginException
name|le
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|le
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|String
name|getColumnInternalName
parameter_list|(
name|int
name|pos
parameter_list|)
block|{
return|return
literal|"_col"
operator|+
name|pos
return|;
block|}
specifier|public
specifier|static
name|int
name|getPositionFromInternalName
parameter_list|(
name|String
name|internalName
parameter_list|)
block|{
name|Pattern
name|internalPattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"_col([0-9]+)"
argument_list|)
decl_stmt|;
name|Matcher
name|m
init|=
name|internalPattern
operator|.
name|matcher
argument_list|(
name|internalName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
else|else
block|{
return|return
name|Integer
operator|.
name|parseInt
argument_list|(
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|/**    * Append comma separated list of config vars to the restrict List    * @param restrictListStr    */
specifier|public
name|void
name|addToRestrictList
parameter_list|(
name|String
name|restrictListStr
parameter_list|)
block|{
if|if
condition|(
name|restrictListStr
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|String
name|oldList
init|=
name|this
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_CONF_RESTRICTED_LIST
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldList
operator|==
literal|null
operator|||
name|oldList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_CONF_RESTRICTED_LIST
argument_list|,
name|restrictListStr
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_CONF_RESTRICTED_LIST
argument_list|,
name|oldList
operator|+
literal|","
operator|+
name|restrictListStr
argument_list|)
expr_stmt|;
block|}
name|setupRestrictList
argument_list|()
expr_stmt|;
block|}
comment|/**    * Set white list of parameters that are allowed to be modified    *    * @param paramNameRegex    */
annotation|@
name|LimitedPrivate
argument_list|(
name|value
operator|=
block|{
literal|"Currently only for use by HiveAuthorizer"
block|}
argument_list|)
specifier|public
name|void
name|setModifiableWhiteListRegex
parameter_list|(
name|String
name|paramNameRegex
parameter_list|)
block|{
if|if
condition|(
name|paramNameRegex
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|modWhiteListPattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|paramNameRegex
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add the HIVE_CONF_RESTRICTED_LIST values to restrictList,    * including HIVE_CONF_RESTRICTED_LIST itself    */
specifier|private
name|void
name|setupRestrictList
parameter_list|()
block|{
name|String
name|restrictListStr
init|=
name|this
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_CONF_RESTRICTED_LIST
argument_list|)
decl_stmt|;
name|restrictList
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|restrictListStr
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|entry
range|:
name|restrictListStr
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
name|restrictList
operator|.
name|add
argument_list|(
name|entry
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|String
name|internalVariableListStr
init|=
name|this
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_CONF_INTERNAL_VARIABLE_LIST
argument_list|)
decl_stmt|;
if|if
condition|(
name|internalVariableListStr
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|entry
range|:
name|internalVariableListStr
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
name|restrictList
operator|.
name|add
argument_list|(
name|entry
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|restrictList
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|HIVE_IN_TEST
operator|.
name|varname
argument_list|)
expr_stmt|;
name|restrictList
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|HIVE_CONF_RESTRICTED_LIST
operator|.
name|varname
argument_list|)
expr_stmt|;
name|restrictList
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|HIVE_CONF_HIDDEN_LIST
operator|.
name|varname
argument_list|)
expr_stmt|;
name|restrictList
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|HIVE_CONF_INTERNAL_VARIABLE_LIST
operator|.
name|varname
argument_list|)
expr_stmt|;
name|restrictList
operator|.
name|add
argument_list|(
name|ConfVars
operator|.
name|HIVE_SPARK_RSC_CONF_LIST
operator|.
name|varname
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setupRSCList
parameter_list|()
block|{
name|rscList
operator|.
name|clear
argument_list|()
expr_stmt|;
name|String
name|vars
init|=
name|this
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SPARK_RSC_CONF_LIST
argument_list|)
decl_stmt|;
if|if
condition|(
name|vars
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|var
range|:
name|vars
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
name|rscList
operator|.
name|add
argument_list|(
name|var
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Strips hidden config entries from configuration    */
specifier|public
name|void
name|stripHiddenConfigurations
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|HiveConfUtil
operator|.
name|stripConfigurations
argument_list|(
name|conf
argument_list|,
name|hiddenSet
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return true if HS2 webui is enabled    */
specifier|public
name|boolean
name|isWebUiEnabled
parameter_list|()
block|{
return|return
name|this
operator|.
name|getIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
argument_list|)
operator|!=
literal|0
return|;
block|}
comment|/**    * @return true if HS2 webui query-info cache is enabled    */
specifier|public
name|boolean
name|isWebUiQueryInfoCacheEnabled
parameter_list|()
block|{
return|return
name|isWebUiEnabled
argument_list|()
operator|&&
name|this
operator|.
name|getIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_MAX_HISTORIC_QUERIES
argument_list|)
operator|>
literal|0
return|;
block|}
comment|/* Dynamic partition pruning is enabled in some or all cases    */
specifier|public
name|boolean
name|isSparkDPPAny
parameter_list|()
block|{
return|return
name|isSparkDPPAny
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/* Dynamic partition pruning is enabled only for map join    * hive.spark.dynamic.partition.pruning is false and    * hive.spark.dynamic.partition.pruning.map.join.only is true    */
specifier|public
name|boolean
name|isSparkDPPOnlyMapjoin
parameter_list|()
block|{
return|return
operator|(
operator|!
name|this
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|SPARK_DYNAMIC_PARTITION_PRUNING
argument_list|)
operator|&&
name|this
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|SPARK_DYNAMIC_PARTITION_PRUNING_MAP_JOIN_ONLY
argument_list|)
operator|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isLoadMetastoreConfig
parameter_list|()
block|{
return|return
name|loadMetastoreConfig
return|;
block|}
specifier|public
specifier|static
name|void
name|setLoadMetastoreConfig
parameter_list|(
name|boolean
name|loadMetastoreConfig
parameter_list|)
block|{
name|HiveConf
operator|.
name|loadMetastoreConfig
operator|=
name|loadMetastoreConfig
expr_stmt|;
block|}
specifier|public
specifier|static
name|boolean
name|isLoadHiveServer2Config
parameter_list|()
block|{
return|return
name|loadHiveServer2Config
return|;
block|}
specifier|public
specifier|static
name|void
name|setLoadHiveServer2Config
parameter_list|(
name|boolean
name|loadHiveServer2Config
parameter_list|)
block|{
name|HiveConf
operator|.
name|loadHiveServer2Config
operator|=
name|loadHiveServer2Config
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|StrictChecks
block|{
specifier|private
specifier|static
specifier|final
name|String
name|NO_LIMIT_MSG
init|=
name|makeMessage
argument_list|(
literal|"Order by-s without limit"
argument_list|,
name|ConfVars
operator|.
name|HIVE_STRICT_CHECKS_ORDERBY_NO_LIMIT
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NO_PARTITIONLESS_MSG
init|=
name|makeMessage
argument_list|(
literal|"Queries against partitioned tables without a partition filter"
argument_list|,
name|ConfVars
operator|.
name|HIVE_STRICT_CHECKS_NO_PARTITION_FILTER
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|NO_COMPARES_MSG
init|=
name|makeMessage
argument_list|(
literal|"Unsafe compares between different types"
argument_list|,
name|ConfVars
operator|.
name|HIVE_STRICT_CHECKS_TYPE_SAFETY
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|NO_CARTESIAN_MSG
init|=
name|makeMessage
argument_list|(
literal|"Cartesian products"
argument_list|,
name|ConfVars
operator|.
name|HIVE_STRICT_CHECKS_CARTESIAN
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|NO_BUCKETING_MSG
init|=
name|makeMessage
argument_list|(
literal|"Load into bucketed tables"
argument_list|,
name|ConfVars
operator|.
name|HIVE_STRICT_CHECKS_BUCKETING
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|makeMessage
parameter_list|(
name|String
name|what
parameter_list|,
name|ConfVars
name|setting
parameter_list|)
block|{
return|return
name|what
operator|+
literal|" are disabled for safety reasons. If you know what you are doing, please set "
operator|+
name|setting
operator|.
name|varname
operator|+
literal|" to false and make sure that "
operator|+
name|ConfVars
operator|.
name|HIVEMAPREDMODE
operator|.
name|varname
operator|+
literal|" is not set to 'strict' to proceed. Note that you may get errors or incorrect "
operator|+
literal|"results if you make a mistake while using some of the unsafe features."
return|;
block|}
specifier|public
specifier|static
name|String
name|checkNoLimit
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|isAllowed
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_STRICT_CHECKS_ORDERBY_NO_LIMIT
argument_list|)
condition|?
literal|null
else|:
name|NO_LIMIT_MSG
return|;
block|}
specifier|public
specifier|static
name|String
name|checkNoPartitionFilter
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|isAllowed
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_STRICT_CHECKS_NO_PARTITION_FILTER
argument_list|)
condition|?
literal|null
else|:
name|NO_PARTITIONLESS_MSG
return|;
block|}
specifier|public
specifier|static
name|String
name|checkTypeSafety
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|isAllowed
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_STRICT_CHECKS_TYPE_SAFETY
argument_list|)
condition|?
literal|null
else|:
name|NO_COMPARES_MSG
return|;
block|}
specifier|public
specifier|static
name|String
name|checkCartesian
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|isAllowed
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_STRICT_CHECKS_CARTESIAN
argument_list|)
condition|?
literal|null
else|:
name|NO_CARTESIAN_MSG
return|;
block|}
specifier|public
specifier|static
name|String
name|checkBucketing
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|isAllowed
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_STRICT_CHECKS_BUCKETING
argument_list|)
condition|?
literal|null
else|:
name|NO_BUCKETING_MSG
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isAllowed
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ConfVars
name|setting
parameter_list|)
block|{
name|String
name|mode
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVEMAPREDMODE
argument_list|,
operator|(
name|String
operator|)
literal|null
argument_list|)
decl_stmt|;
return|return
operator|(
name|mode
operator|!=
literal|null
operator|)
condition|?
operator|!
literal|"strict"
operator|.
name|equals
argument_list|(
name|mode
argument_list|)
else|:
operator|!
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|setting
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|String
name|getNonMrEngines
parameter_list|()
block|{
name|String
name|result
init|=
name|StringUtils
operator|.
name|EMPTY
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
operator|.
name|getValidStringValues
argument_list|()
control|)
block|{
if|if
condition|(
literal|"mr"
operator|.
name|equals
argument_list|(
name|s
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|!
name|result
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|result
operator|+=
literal|", "
expr_stmt|;
block|}
name|result
operator|+=
name|s
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|String
name|generateMrDeprecationWarning
parameter_list|()
block|{
return|return
literal|"Hive-on-MR is deprecated in Hive 2 and may not be available in the future versions. "
operator|+
literal|"Consider using a different execution engine (i.e. "
operator|+
name|HiveConf
operator|.
name|getNonMrEngines
argument_list|()
operator|+
literal|") or using Hive 1.X releases."
return|;
block|}
specifier|private
specifier|static
specifier|final
name|Object
name|reverseMapLock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|HashMap
argument_list|<
name|String
argument_list|,
name|ConfVars
argument_list|>
name|reverseMap
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
name|HashMap
argument_list|<
name|String
argument_list|,
name|ConfVars
argument_list|>
name|getOrCreateReverseMap
parameter_list|()
block|{
comment|// This should be called rarely enough; for now it's ok to just lock every time.
synchronized|synchronized
init|(
name|reverseMapLock
init|)
block|{
if|if
condition|(
name|reverseMap
operator|!=
literal|null
condition|)
block|{
return|return
name|reverseMap
return|;
block|}
block|}
name|HashMap
argument_list|<
name|String
argument_list|,
name|ConfVars
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ConfVars
name|val
range|:
name|ConfVars
operator|.
name|values
argument_list|()
control|)
block|{
name|vars
operator|.
name|put
argument_list|(
name|val
operator|.
name|varname
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|val
argument_list|)
expr_stmt|;
if|if
condition|(
name|val
operator|.
name|altName
operator|!=
literal|null
operator|&&
operator|!
name|val
operator|.
name|altName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|vars
operator|.
name|put
argument_list|(
name|val
operator|.
name|altName
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
synchronized|synchronized
init|(
name|reverseMapLock
init|)
block|{
if|if
condition|(
name|reverseMap
operator|!=
literal|null
condition|)
block|{
return|return
name|reverseMap
return|;
block|}
name|reverseMap
operator|=
name|vars
expr_stmt|;
return|return
name|reverseMap
return|;
block|}
block|}
specifier|public
name|void
name|verifyAndSetAll
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|overlay
parameter_list|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|overlay
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|verifyAndSet
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|subtree
parameter_list|(
name|String
name|string
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ret
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|getProps
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|key
init|=
operator|(
name|String
operator|)
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|value
init|=
operator|(
name|String
operator|)
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|.
name|startsWith
argument_list|(
name|string
argument_list|)
condition|)
block|{
name|ret
operator|.
name|put
argument_list|(
name|key
operator|.
name|substring
argument_list|(
name|string
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit

