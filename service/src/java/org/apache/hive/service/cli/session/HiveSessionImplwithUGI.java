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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|session
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
name|Map
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
name|ql
operator|.
name|metadata
operator|.
name|Hive
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|ShimLoader
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
name|service
operator|.
name|auth
operator|.
name|HiveAuthFactory
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
name|service
operator|.
name|cli
operator|.
name|HiveSQLException
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
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TProtocolVersion
import|;
end_import

begin_comment
comment|/**  *  * HiveSessionImplwithUGI.  * HiveSession with connecting user's UGI and delegation token if required  */
end_comment

begin_class
specifier|public
class|class
name|HiveSessionImplwithUGI
extends|extends
name|HiveSessionImpl
block|{
specifier|public
specifier|static
specifier|final
name|String
name|HS2TOKEN
init|=
literal|"HiveServer2ImpersonationToken"
decl_stmt|;
specifier|private
name|UserGroupInformation
name|sessionUgi
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|delegationTokenStr
init|=
literal|null
decl_stmt|;
specifier|private
name|Hive
name|sessionHive
init|=
literal|null
decl_stmt|;
specifier|private
name|HiveSession
name|proxySession
init|=
literal|null
decl_stmt|;
specifier|public
name|HiveSessionImplwithUGI
parameter_list|(
name|TProtocolVersion
name|protocol
parameter_list|,
name|String
name|username
parameter_list|,
name|String
name|password
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|ipAddress
parameter_list|,
name|String
name|delegationToken
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|super
argument_list|(
name|protocol
argument_list|,
name|username
argument_list|,
name|password
argument_list|,
name|hiveConf
argument_list|,
name|ipAddress
argument_list|)
expr_stmt|;
name|setSessionUGI
argument_list|(
name|username
argument_list|)
expr_stmt|;
name|setDelegationToken
argument_list|(
name|delegationToken
argument_list|)
expr_stmt|;
block|}
comment|// setup appropriate UGI for the session
specifier|public
name|void
name|setSessionUGI
parameter_list|(
name|String
name|owner
parameter_list|)
throws|throws
name|HiveSQLException
block|{
if|if
condition|(
name|owner
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"No username provided for impersonation"
argument_list|)
throw|;
block|}
if|if
condition|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|isSecurityEnabled
argument_list|()
condition|)
block|{
try|try
block|{
name|sessionUgi
operator|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|createProxyUser
argument_list|(
name|owner
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
name|HiveSQLException
argument_list|(
literal|"Couldn't setup proxy user"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|sessionUgi
operator|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|createRemoteUser
argument_list|(
name|owner
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|UserGroupInformation
name|getSessionUgi
parameter_list|()
block|{
return|return
name|this
operator|.
name|sessionUgi
return|;
block|}
specifier|public
name|String
name|getDelegationToken
parameter_list|()
block|{
return|return
name|this
operator|.
name|delegationTokenStr
return|;
block|}
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|void
name|acquire
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|super
operator|.
name|acquire
argument_list|()
expr_stmt|;
comment|// if we have a metastore connection with impersonation, then set it first
if|if
condition|(
name|sessionHive
operator|!=
literal|null
condition|)
block|{
name|Hive
operator|.
name|set
argument_list|(
name|sessionHive
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * close the file systems for the session    * cancel the session's delegation token and close the metastore connection    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|HiveSQLException
block|{
try|try
block|{
name|acquire
argument_list|()
expr_stmt|;
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|closeAllForUGI
argument_list|(
name|sessionUgi
argument_list|)
expr_stmt|;
name|cancelDelegationToken
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|release
argument_list|()
expr_stmt|;
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Enable delegation token for the session    * save the token string and set the token.signature in hive conf. The metastore client uses    * this token.signature to determine where to use kerberos or delegation token    * @throws HiveException    * @throws IOException    */
specifier|private
name|void
name|setDelegationToken
parameter_list|(
name|String
name|delegationTokenStr
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|this
operator|.
name|delegationTokenStr
operator|=
name|delegationTokenStr
expr_stmt|;
if|if
condition|(
name|delegationTokenStr
operator|!=
literal|null
condition|)
block|{
name|getHiveConf
argument_list|()
operator|.
name|set
argument_list|(
literal|"hive.metastore.token.signature"
argument_list|,
name|HS2TOKEN
argument_list|)
expr_stmt|;
try|try
block|{
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|setTokenStr
argument_list|(
name|sessionUgi
argument_list|,
name|delegationTokenStr
argument_list|,
name|HS2TOKEN
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
name|HiveSQLException
argument_list|(
literal|"Couldn't setup delegation token in the ugi"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// create a new metastore connection using the delegation token
name|Hive
operator|.
name|set
argument_list|(
literal|null
argument_list|)
expr_stmt|;
try|try
block|{
name|sessionHive
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|getHiveConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Failed to setup metastore connection"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|// If the session has a delegation token obtained from the metastore, then cancel it
specifier|private
name|void
name|cancelDelegationToken
parameter_list|()
throws|throws
name|HiveSQLException
block|{
if|if
condition|(
name|delegationTokenStr
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|Hive
operator|.
name|get
argument_list|(
name|getHiveConf
argument_list|()
argument_list|)
operator|.
name|cancelDelegationToken
argument_list|(
name|delegationTokenStr
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Couldn't cancel delegation token"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// close the metastore connection created with this delegation token
name|Hive
operator|.
name|closeCurrent
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|HiveSession
name|getSession
parameter_list|()
block|{
assert|assert
name|proxySession
operator|!=
literal|null
assert|;
return|return
name|proxySession
return|;
block|}
specifier|public
name|void
name|setProxySession
parameter_list|(
name|HiveSession
name|proxySession
parameter_list|)
block|{
name|this
operator|.
name|proxySession
operator|=
name|proxySession
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDelegationToken
parameter_list|(
name|HiveAuthFactory
name|authFactory
parameter_list|,
name|String
name|owner
parameter_list|,
name|String
name|renewer
parameter_list|)
throws|throws
name|HiveSQLException
block|{
return|return
name|authFactory
operator|.
name|getDelegationToken
argument_list|(
name|owner
argument_list|,
name|renewer
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cancelDelegationToken
parameter_list|(
name|HiveAuthFactory
name|authFactory
parameter_list|,
name|String
name|tokenStr
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|authFactory
operator|.
name|cancelDelegationToken
argument_list|(
name|tokenStr
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|renewDelegationToken
parameter_list|(
name|HiveAuthFactory
name|authFactory
parameter_list|,
name|String
name|tokenStr
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|authFactory
operator|.
name|renewDelegationToken
argument_list|(
name|tokenStr
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

