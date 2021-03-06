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
name|common
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
name|ServerSocket
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
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

begin_comment
comment|/**  * ServerUtils (specific to HiveServer version 1)  */
end_comment

begin_class
specifier|public
class|class
name|ServerUtils
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ServerUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|void
name|cleanUpScratchDir
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_START_CLEANUP_SCRATCHDIR
argument_list|)
condition|)
block|{
name|String
name|hiveScratchDir
init|=
name|hiveConf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SCRATCHDIR
operator|.
name|varname
argument_list|)
decl_stmt|;
try|try
block|{
name|Path
name|jobScratchDir
init|=
operator|new
name|Path
argument_list|(
name|hiveScratchDir
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Cleaning scratchDir : "
operator|+
name|hiveScratchDir
argument_list|)
expr_stmt|;
name|FileSystem
name|fileSystem
init|=
name|jobScratchDir
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|fileSystem
operator|.
name|delete
argument_list|(
name|jobScratchDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Even if the cleanup throws some exception it will continue.
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to delete scratchDir : "
operator|+
name|hiveScratchDir
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Get the Inet address of the machine of the given host name.    * @param hostname The name of the host    * @return The network address of the the host    * @throws UnknownHostException    */
specifier|public
specifier|static
name|InetAddress
name|getHostAddress
parameter_list|(
name|String
name|hostname
parameter_list|)
throws|throws
name|UnknownHostException
block|{
name|InetAddress
name|serverIPAddress
decl_stmt|;
if|if
condition|(
name|hostname
operator|!=
literal|null
operator|&&
operator|!
name|hostname
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|serverIPAddress
operator|=
name|InetAddress
operator|.
name|getByName
argument_list|(
name|hostname
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serverIPAddress
operator|=
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
expr_stmt|;
block|}
return|return
name|serverIPAddress
return|;
block|}
comment|/**    * @return name of current host    */
specifier|public
specifier|static
name|String
name|hostname
parameter_list|()
block|{
try|try
block|{
return|return
name|InetAddress
operator|.
name|getLocalHost
argument_list|()
operator|.
name|getHostName
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to resolve my host name "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
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
block|}
specifier|public
specifier|static
name|int
name|findFreePort
parameter_list|()
throws|throws
name|IOException
block|{
name|ServerSocket
name|socket
init|=
operator|new
name|ServerSocket
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|int
name|port
init|=
name|socket
operator|.
name|getLocalPort
argument_list|()
decl_stmt|;
name|socket
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|port
return|;
block|}
block|}
end_class

end_unit

