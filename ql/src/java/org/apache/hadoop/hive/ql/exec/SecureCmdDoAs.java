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
name|ql
operator|.
name|exec
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
comment|/**  * SecureCmdDoAs - Helper class for setting parameters and env necessary for  * being able to run child jvm as intended user.  * Used only when kerberos security is used  *  */
end_comment

begin_class
specifier|public
class|class
name|SecureCmdDoAs
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
name|SecureCmdDoAs
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Path
name|tokenPath
decl_stmt|;
specifier|private
specifier|final
name|File
name|tokenFile
decl_stmt|;
specifier|public
name|SecureCmdDoAs
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
comment|// Get delegation token for user from filesystem and write the token along with
comment|// metastore tokens into a file
name|String
name|uname
init|=
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
operator|.
name|getShortUserName
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Credentials
name|cred
init|=
operator|new
name|Credentials
argument_list|()
decl_stmt|;
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|addDelegationTokens
argument_list|(
name|fs
argument_list|,
name|cred
argument_list|,
name|uname
argument_list|)
expr_stmt|;
comment|// ask default fs first
for|for
control|(
name|String
name|uri
range|:
name|conf
operator|.
name|getStringCollection
argument_list|(
literal|"mapreduce.job.hdfs-servers"
argument_list|)
control|)
block|{
try|try
block|{
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|addDelegationTokens
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
name|cred
argument_list|,
name|uname
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Invalid URI in mapreduce.job.hdfs-servers:["
operator|+
name|uri
operator|+
literal|"], ignoring."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|tokenFile
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"hive_hadoop_delegation_token"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|tokenPath
operator|=
operator|new
name|Path
argument_list|(
name|tokenFile
operator|.
name|toURI
argument_list|()
argument_list|)
expr_stmt|;
comment|//write credential with token to file
name|cred
operator|.
name|writeTokenStorageFile
argument_list|(
name|tokenPath
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
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
name|env
operator|.
name|put
argument_list|(
name|UserGroupInformation
operator|.
name|HADOOP_TOKEN_FILE_LOCATION
argument_list|,
name|tokenPath
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
name|tokenFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

