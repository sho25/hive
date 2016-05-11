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
name|templeton
package|;
end_package

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
name|URISyntaxException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
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
name|lang3
operator|.
name|ArrayUtils
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
name|metastore
operator|.
name|IMetaStoreClient
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_comment
comment|/**  * Helper class to run jobs using Kerberos security.  Always safe to  * use these methods, it's a no-op if security is not enabled.  */
end_comment

begin_class
specifier|public
class|class
name|SecureProxySupport
block|{
specifier|private
name|Path
name|tokenPath
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_SERVICE
init|=
literal|"hcat"
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isEnabled
decl_stmt|;
specifier|private
name|String
name|user
decl_stmt|;
specifier|public
name|SecureProxySupport
parameter_list|()
block|{
name|isEnabled
operator|=
name|UserGroupInformation
operator|.
name|isSecurityEnabled
argument_list|()
expr_stmt|;
block|}
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
name|SecureProxySupport
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * The file where we store the auth token    */
specifier|public
name|Path
name|getTokenPath
parameter_list|()
block|{
return|return
operator|(
name|tokenPath
operator|)
return|;
block|}
comment|/**    * The token to pass to hcat.    */
specifier|public
name|String
name|getHcatServiceStr
parameter_list|()
block|{
return|return
operator|(
name|HCAT_SERVICE
operator|)
return|;
block|}
comment|/**    * Create the delegation token.    */
specifier|public
name|Path
name|open
parameter_list|(
name|String
name|user
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|isEnabled
condition|)
block|{
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
name|File
name|t
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"templeton"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|tokenPath
operator|=
operator|new
name|Path
argument_list|(
name|t
operator|.
name|toURI
argument_list|()
argument_list|)
expr_stmt|;
name|Token
index|[]
name|fsToken
init|=
name|getFSDelegationToken
argument_list|(
name|user
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|String
name|hcatTokenStr
decl_stmt|;
try|try
block|{
name|hcatTokenStr
operator|=
name|buildHcatDelegationToken
argument_list|(
name|user
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
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|hcatTokenStr
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"open("
operator|+
name|user
operator|+
literal|") token=null"
argument_list|)
expr_stmt|;
block|}
name|Token
argument_list|<
name|?
argument_list|>
name|msToken
init|=
operator|new
name|Token
argument_list|()
decl_stmt|;
name|msToken
operator|.
name|decodeFromUrlString
argument_list|(
name|hcatTokenStr
argument_list|)
expr_stmt|;
name|msToken
operator|.
name|setService
argument_list|(
operator|new
name|Text
argument_list|(
name|HCAT_SERVICE
argument_list|)
argument_list|)
expr_stmt|;
name|writeProxyDelegationTokens
argument_list|(
name|fsToken
argument_list|,
name|msToken
argument_list|,
name|conf
argument_list|,
name|user
argument_list|,
name|tokenPath
argument_list|)
expr_stmt|;
block|}
return|return
name|tokenPath
return|;
block|}
comment|/**    * Cleanup    */
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|tokenPath
operator|!=
literal|null
condition|)
block|{
operator|new
name|File
argument_list|(
name|tokenPath
operator|.
name|toUri
argument_list|()
argument_list|)
operator|.
name|delete
argument_list|()
expr_stmt|;
name|tokenPath
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**    * Add Hadoop env variables.    */
specifier|public
name|void
name|addEnv
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
parameter_list|)
block|{
if|if
condition|(
name|isEnabled
condition|)
block|{
name|env
operator|.
name|put
argument_list|(
name|UserGroupInformation
operator|.
name|HADOOP_TOKEN_FILE_LOCATION
argument_list|,
name|getTokenPath
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Add hcat args.    */
specifier|public
name|void
name|addArgs
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|)
block|{
if|if
condition|(
name|isEnabled
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-D"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_TOKEN_SIGNATURE
operator|+
literal|"="
operator|+
name|getHcatServiceStr
argument_list|()
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-D"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"proxy.user.name="
operator|+
name|user
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TokenWrapper
block|{
name|Token
argument_list|<
name|?
argument_list|>
index|[]
name|tokens
init|=
operator|new
name|Token
argument_list|<
name|?
argument_list|>
index|[
literal|0
index|]
decl_stmt|;
block|}
specifier|private
name|Token
argument_list|<
name|?
argument_list|>
index|[]
name|getFSDelegationToken
parameter_list|(
name|String
name|user
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"user: "
operator|+
name|user
operator|+
literal|" loginUser: "
operator|+
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|UserGroupInformation
name|ugi
init|=
name|UgiFactory
operator|.
name|getUgi
argument_list|(
name|user
argument_list|)
decl_stmt|;
specifier|final
name|TokenWrapper
name|twrapper
init|=
operator|new
name|TokenWrapper
argument_list|()
decl_stmt|;
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|IOException
throws|,
name|URISyntaxException
block|{
name|Credentials
name|creds
init|=
operator|new
name|Credentials
argument_list|()
decl_stmt|;
comment|//get Tokens for default FS.  Not all FSs support delegation tokens, e.g. WASB
name|collectTokens
argument_list|(
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
argument_list|,
name|twrapper
argument_list|,
name|creds
argument_list|,
name|ugi
operator|.
name|getShortUserName
argument_list|()
argument_list|)
expr_stmt|;
comment|//get tokens for all other known FSs since Hive tables may result in different ones
comment|//passing "creds" prevents duplicate tokens from being added
name|Collection
argument_list|<
name|String
argument_list|>
name|URIs
init|=
name|conf
operator|.
name|getStringCollection
argument_list|(
literal|"mapreduce.job.hdfs-servers"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|uri
range|:
name|URIs
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Getting tokens for "
operator|+
name|uri
argument_list|)
expr_stmt|;
name|collectTokens
argument_list|(
name|FileSystem
operator|.
name|get
argument_list|(
operator|new
name|URI
argument_list|(
name|uri
argument_list|)
argument_list|,
name|conf
argument_list|)
argument_list|,
name|twrapper
argument_list|,
name|creds
argument_list|,
name|ugi
operator|.
name|getShortUserName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|twrapper
operator|.
name|tokens
return|;
block|}
specifier|private
specifier|static
name|void
name|collectTokens
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|TokenWrapper
name|twrapper
parameter_list|,
name|Credentials
name|creds
parameter_list|,
name|String
name|userName
parameter_list|)
throws|throws
name|IOException
block|{
name|Token
index|[]
name|tokens
init|=
name|fs
operator|.
name|addDelegationTokens
argument_list|(
name|userName
argument_list|,
name|creds
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokens
operator|!=
literal|null
operator|&&
name|tokens
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|twrapper
operator|.
name|tokens
operator|=
name|ArrayUtils
operator|.
name|addAll
argument_list|(
name|twrapper
operator|.
name|tokens
argument_list|,
name|tokens
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @param fsTokens not null    */
specifier|private
name|void
name|writeProxyDelegationTokens
parameter_list|(
specifier|final
name|Token
argument_list|<
name|?
argument_list|>
name|fsTokens
index|[]
parameter_list|,
specifier|final
name|Token
argument_list|<
name|?
argument_list|>
name|msToken
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
name|String
name|user
parameter_list|,
specifier|final
name|Path
name|tokenPath
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"user: "
operator|+
name|user
operator|+
literal|" loginUser: "
operator|+
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|UserGroupInformation
name|ugi
init|=
name|UgiFactory
operator|.
name|getUgi
argument_list|(
name|user
argument_list|)
decl_stmt|;
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|IOException
block|{
name|Credentials
name|cred
init|=
operator|new
name|Credentials
argument_list|()
decl_stmt|;
for|for
control|(
name|Token
argument_list|<
name|?
argument_list|>
name|fsToken
range|:
name|fsTokens
control|)
block|{
name|cred
operator|.
name|addToken
argument_list|(
name|fsToken
operator|.
name|getService
argument_list|()
argument_list|,
name|fsToken
argument_list|)
expr_stmt|;
block|}
name|cred
operator|.
name|addToken
argument_list|(
name|msToken
operator|.
name|getService
argument_list|()
argument_list|,
name|msToken
argument_list|)
expr_stmt|;
name|cred
operator|.
name|writeTokenStorageFile
argument_list|(
name|tokenPath
argument_list|,
name|conf
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|buildHcatDelegationToken
parameter_list|(
name|String
name|user
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|TException
block|{
specifier|final
name|HiveConf
name|c
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
specifier|final
name|IMetaStoreClient
name|client
init|=
name|HCatUtil
operator|.
name|getHiveMetastoreClient
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"user: "
operator|+
name|user
operator|+
literal|" loginUser: "
operator|+
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|UserGroupInformation
name|ugi
init|=
name|UgiFactory
operator|.
name|getUgi
argument_list|(
name|user
argument_list|)
decl_stmt|;
name|String
name|s
init|=
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|run
parameter_list|()
throws|throws
name|IOException
throws|,
name|MetaException
throws|,
name|TException
block|{
name|String
name|u
init|=
name|ugi
operator|.
name|getUserName
argument_list|()
decl_stmt|;
return|return
name|client
operator|.
name|getDelegationToken
argument_list|(
name|c
operator|.
name|getUser
argument_list|()
argument_list|,
name|u
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
return|return
name|s
return|;
block|}
block|}
end_class

end_unit

