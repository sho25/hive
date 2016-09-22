begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
operator|.
name|rpc
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
name|HashMap
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
name|security
operator|.
name|sasl
operator|.
name|Sasl
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
name|ImmutableMap
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
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
name|hive
operator|.
name|common
operator|.
name|ServerUtils
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
name|conf
operator|.
name|HiveConf
import|;
end_import

begin_comment
comment|/**  * Definitions of configuration keys and default values for the RPC layer.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|RpcConfiguration
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
name|RpcConfiguration
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|HIVE_SPARK_RSC_CONFIGS
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_CLIENT_CONNECT_TIMEOUT
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_CLIENT_HANDSHAKE_TIMEOUT
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_CHANNEL_LOG_LEVEL
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_MAX_MESSAGE_SIZE
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_MAX_THREADS
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_SECRET_RANDOM_BITS
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_SERVER_ADDRESS
operator|.
name|varname
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|HIVE_SPARK_TIME_CONFIGS
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_CLIENT_CONNECT_TIMEOUT
operator|.
name|varname
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_CLIENT_HANDSHAKE_TIMEOUT
operator|.
name|varname
argument_list|)
decl_stmt|;
comment|/** Prefix for other SASL options. */
specifier|public
specifier|static
specifier|final
name|String
name|RPC_SASL_OPT_PREFIX
init|=
literal|"hive.spark.client.rpc.sasl."
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|config
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HiveConf
name|DEFAULT_CONF
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
specifier|public
name|RpcConfiguration
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|config
parameter_list|)
block|{
name|this
operator|.
name|config
operator|=
name|config
expr_stmt|;
block|}
name|long
name|getConnectTimeoutMs
parameter_list|()
block|{
name|String
name|value
init|=
name|config
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_CLIENT_CONNECT_TIMEOUT
operator|.
name|varname
argument_list|)
decl_stmt|;
return|return
name|value
operator|!=
literal|null
condition|?
name|Integer
operator|.
name|parseInt
argument_list|(
name|value
argument_list|)
else|:
name|DEFAULT_CONF
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_CLIENT_CONNECT_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
name|int
name|getMaxMessageSize
parameter_list|()
block|{
name|String
name|value
init|=
name|config
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_MAX_MESSAGE_SIZE
operator|.
name|varname
argument_list|)
decl_stmt|;
return|return
name|value
operator|!=
literal|null
condition|?
name|Integer
operator|.
name|parseInt
argument_list|(
name|value
argument_list|)
else|:
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_MAX_MESSAGE_SIZE
operator|.
name|defaultIntVal
return|;
block|}
name|long
name|getServerConnectTimeoutMs
parameter_list|()
block|{
name|String
name|value
init|=
name|config
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_CLIENT_HANDSHAKE_TIMEOUT
operator|.
name|varname
argument_list|)
decl_stmt|;
return|return
name|value
operator|!=
literal|null
condition|?
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
else|:
name|DEFAULT_CONF
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_CLIENT_HANDSHAKE_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
name|int
name|getSecretBits
parameter_list|()
block|{
name|String
name|value
init|=
name|config
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_SECRET_RANDOM_BITS
operator|.
name|varname
argument_list|)
decl_stmt|;
return|return
name|value
operator|!=
literal|null
condition|?
name|Integer
operator|.
name|parseInt
argument_list|(
name|value
argument_list|)
else|:
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_SECRET_RANDOM_BITS
operator|.
name|defaultIntVal
return|;
block|}
comment|/**    * Here we assume that the remote driver will connect back to HS2 using the same network interface    * as if it were just a HS2 client. If this isn't true, we can have a separate configuration for that.    * For now, I think we are okay.    * @return server host name in the network    * @throws IOException    */
name|String
name|getServerAddress
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|hiveHost
init|=
name|config
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_SERVER_ADDRESS
operator|.
name|varname
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|hiveHost
argument_list|)
condition|)
block|{
name|hiveHost
operator|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_SERVER2_THRIFT_BIND_HOST"
argument_list|)
expr_stmt|;
if|if
condition|(
name|hiveHost
operator|==
literal|null
condition|)
block|{
name|hiveHost
operator|=
name|config
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_BIND_HOST
operator|.
name|varname
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ServerUtils
operator|.
name|getHostAddress
argument_list|(
name|hiveHost
argument_list|)
operator|.
name|getHostName
argument_list|()
return|;
block|}
name|String
name|getRpcChannelLogLevel
parameter_list|()
block|{
return|return
name|config
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_CHANNEL_LOG_LEVEL
operator|.
name|varname
argument_list|)
return|;
block|}
specifier|public
name|int
name|getRpcThreadCount
parameter_list|()
block|{
name|String
name|value
init|=
name|config
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_MAX_THREADS
operator|.
name|varname
argument_list|)
decl_stmt|;
return|return
name|value
operator|!=
literal|null
condition|?
name|Integer
operator|.
name|parseInt
argument_list|(
name|value
argument_list|)
else|:
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_MAX_THREADS
operator|.
name|defaultIntVal
return|;
block|}
comment|/**    * Utility method for a given RpcConfiguration key, to convert value to millisecond if it is a time value,    * and return as string in either case.    * @param conf hive configuration    * @param key Rpc configuration to lookup (hive.spark.*)    * @return string form of the value    */
specifier|public
specifier|static
name|String
name|getValue
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|key
parameter_list|)
block|{
if|if
condition|(
name|HIVE_SPARK_TIME_CONFIGS
operator|.
name|contains
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|HiveConf
operator|.
name|ConfVars
name|confVar
init|=
name|HiveConf
operator|.
name|getConfVars
argument_list|(
name|key
argument_list|)
decl_stmt|;
return|return
name|String
operator|.
name|valueOf
argument_list|(
name|conf
operator|.
name|getTimeVar
argument_list|(
name|confVar
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|conf
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
block|}
name|String
name|getSaslMechanism
parameter_list|()
block|{
name|String
name|value
init|=
name|config
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_SASL_MECHANISM
operator|.
name|varname
argument_list|)
decl_stmt|;
return|return
name|value
operator|!=
literal|null
condition|?
name|value
else|:
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_RPC_SASL_MECHANISM
operator|.
name|defaultStrVal
return|;
block|}
comment|/**    * SASL options are namespaced under "hive.spark.client.rpc.sasl.*"; each option is the    * lower-case version of the constant in the "javax.security.sasl.Sasl" class (e.g. "strength"    * for cipher strength).    */
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getSaslOptions
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|opts
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
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|saslOpts
init|=
name|ImmutableMap
operator|.
expr|<
name|String
decl_stmt|,
name|String
decl|>
name|builder
argument_list|()
decl|.
name|put
argument_list|(
name|Sasl
operator|.
name|CREDENTIALS
argument_list|,
literal|"credentials"
argument_list|)
decl|.
name|put
argument_list|(
name|Sasl
operator|.
name|MAX_BUFFER
argument_list|,
literal|"max_buffer"
argument_list|)
decl|.
name|put
argument_list|(
name|Sasl
operator|.
name|POLICY_FORWARD_SECRECY
argument_list|,
literal|"policy_forward_secrecy"
argument_list|)
decl|.
name|put
argument_list|(
name|Sasl
operator|.
name|POLICY_NOACTIVE
argument_list|,
literal|"policy_noactive"
argument_list|)
decl|.
name|put
argument_list|(
name|Sasl
operator|.
name|POLICY_NOANONYMOUS
argument_list|,
literal|"policy_noanonymous"
argument_list|)
decl|.
name|put
argument_list|(
name|Sasl
operator|.
name|POLICY_NODICTIONARY
argument_list|,
literal|"policy_nodictionary"
argument_list|)
decl|.
name|put
argument_list|(
name|Sasl
operator|.
name|POLICY_NOPLAINTEXT
argument_list|,
literal|"policy_noplaintext"
argument_list|)
decl|.
name|put
argument_list|(
name|Sasl
operator|.
name|POLICY_PASS_CREDENTIALS
argument_list|,
literal|"policy_pass_credentials"
argument_list|)
decl|.
name|put
argument_list|(
name|Sasl
operator|.
name|QOP
argument_list|,
literal|"qop"
argument_list|)
decl|.
name|put
argument_list|(
name|Sasl
operator|.
name|RAW_SEND_SIZE
argument_list|,
literal|"raw_send_size"
argument_list|)
decl|.
name|put
argument_list|(
name|Sasl
operator|.
name|REUSE
argument_list|,
literal|"reuse"
argument_list|)
decl|.
name|put
argument_list|(
name|Sasl
operator|.
name|SERVER_AUTH
argument_list|,
literal|"server_auth"
argument_list|)
decl|.
name|put
argument_list|(
name|Sasl
operator|.
name|STRENGTH
argument_list|,
literal|"strength"
argument_list|)
decl|.
name|build
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
name|e
range|:
name|saslOpts
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|value
init|=
name|config
operator|.
name|get
argument_list|(
name|RPC_SASL_OPT_PREFIX
operator|+
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|opts
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|opts
return|;
block|}
block|}
end_class

end_unit

