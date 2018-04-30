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
name|cli
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
name|permission
operator|.
name|FsPermission
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationId
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationReport
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
name|yarn
operator|.
name|exceptions
operator|.
name|YarnException
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
name|yarn
operator|.
name|service
operator|.
name|api
operator|.
name|records
operator|.
name|Service
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
name|yarn
operator|.
name|service
operator|.
name|client
operator|.
name|ServiceClient
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
name|yarn
operator|.
name|service
operator|.
name|utils
operator|.
name|CoreFileSystem
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
name|yarn
operator|.
name|util
operator|.
name|Clock
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
name|yarn
operator|.
name|util
operator|.
name|SystemClock
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
name|LlapSliderUtils
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
name|LlapSliderUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LLAP_PACKAGE_DIR
init|=
literal|".yarn/package/LLAP/"
decl_stmt|;
specifier|public
specifier|static
name|ServiceClient
name|createServiceClient
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|ServiceClient
name|serviceClient
init|=
operator|new
name|ServiceClient
argument_list|()
decl_stmt|;
name|serviceClient
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|serviceClient
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|serviceClient
return|;
block|}
specifier|public
specifier|static
name|ApplicationReport
name|getAppReport
parameter_list|(
name|String
name|appName
parameter_list|,
name|ServiceClient
name|serviceClient
parameter_list|,
name|long
name|timeoutMs
parameter_list|)
throws|throws
name|LlapStatusServiceDriver
operator|.
name|LlapStatusCliException
block|{
name|Clock
name|clock
init|=
name|SystemClock
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|long
name|startTime
init|=
name|clock
operator|.
name|getTime
argument_list|()
decl_stmt|;
name|long
name|timeoutTime
init|=
name|timeoutMs
operator|<
literal|0
condition|?
name|Long
operator|.
name|MAX_VALUE
else|:
operator|(
name|startTime
operator|+
name|timeoutMs
operator|)
decl_stmt|;
name|ApplicationReport
name|appReport
init|=
literal|null
decl_stmt|;
name|ApplicationId
name|appId
decl_stmt|;
try|try
block|{
name|appId
operator|=
name|serviceClient
operator|.
name|getAppId
argument_list|(
name|appName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|YarnException
decl||
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
while|while
condition|(
name|appReport
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|appReport
operator|=
name|serviceClient
operator|.
name|getYarnClient
argument_list|()
operator|.
name|getApplicationReport
argument_list|(
name|appId
argument_list|)
expr_stmt|;
if|if
condition|(
name|timeoutMs
operator|==
literal|0
condition|)
block|{
comment|// break immediately if timeout is 0
break|break;
block|}
comment|// Otherwise sleep, and try again.
if|if
condition|(
name|appReport
operator|==
literal|null
condition|)
block|{
name|long
name|remainingTime
init|=
name|Math
operator|.
name|min
argument_list|(
name|timeoutTime
operator|-
name|clock
operator|.
name|getTime
argument_list|()
argument_list|,
literal|500l
argument_list|)
decl_stmt|;
if|if
condition|(
name|remainingTime
operator|>
literal|0
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|remainingTime
argument_list|)
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// No point separating IOException vs YarnException vs others
throw|throw
operator|new
name|LlapStatusServiceDriver
operator|.
name|LlapStatusCliException
argument_list|(
name|LlapStatusServiceDriver
operator|.
name|ExitCode
operator|.
name|YARN_ERROR
argument_list|,
literal|"Failed to get Yarn AppReport"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|appReport
return|;
block|}
specifier|public
specifier|static
name|Service
name|getService
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Get service details for "
operator|+
name|name
argument_list|)
expr_stmt|;
name|ServiceClient
name|sc
decl_stmt|;
try|try
block|{
name|sc
operator|=
name|createServiceClient
argument_list|(
name|conf
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|Service
name|service
init|=
literal|null
decl_stmt|;
try|try
block|{
name|service
operator|=
name|sc
operator|.
name|getStatus
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|YarnException
decl||
name|IOException
name|e
parameter_list|)
block|{
comment|// Probably the app does not exist
name|LOG
operator|.
name|info
argument_list|(
name|e
operator|.
name|getLocalizedMessage
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
finally|finally
block|{
try|try
block|{
name|sc
operator|.
name|close
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
name|info
argument_list|(
literal|"Failed to close service client"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|service
return|;
block|}
specifier|public
specifier|static
name|void
name|startCluster
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|packageName
parameter_list|,
name|Path
name|packageDir
parameter_list|,
name|String
name|queue
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting cluster with "
operator|+
name|name
operator|+
literal|", "
operator|+
name|packageName
operator|+
literal|", "
operator|+
name|queue
operator|+
literal|", "
operator|+
name|packageDir
argument_list|)
expr_stmt|;
name|ServiceClient
name|sc
decl_stmt|;
try|try
block|{
name|sc
operator|=
name|createServiceClient
argument_list|(
name|conf
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
try|try
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing the stop command"
argument_list|)
expr_stmt|;
name|sc
operator|.
name|actionStop
argument_list|(
name|name
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
comment|// Ignore exceptions from stop
name|LOG
operator|.
name|info
argument_list|(
name|ex
operator|.
name|getLocalizedMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing the destroy command"
argument_list|)
expr_stmt|;
name|sc
operator|.
name|actionDestroy
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
comment|// Ignore exceptions from destroy
name|LOG
operator|.
name|info
argument_list|(
name|ex
operator|.
name|getLocalizedMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Uploading the app tarball"
argument_list|)
expr_stmt|;
name|CoreFileSystem
name|fs
init|=
operator|new
name|CoreFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|copyLocalFileToHdfs
argument_list|(
operator|new
name|File
argument_list|(
name|packageDir
operator|.
name|toString
argument_list|()
argument_list|,
name|packageName
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|LLAP_PACKAGE_DIR
argument_list|)
argument_list|,
operator|new
name|FsPermission
argument_list|(
literal|"755"
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing the launch command"
argument_list|)
expr_stmt|;
name|File
name|yarnfile
init|=
operator|new
name|File
argument_list|(
operator|new
name|Path
argument_list|(
name|packageDir
argument_list|,
literal|"Yarnfile"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|Long
name|lifetime
init|=
literal|null
decl_stmt|;
comment|// unlimited lifetime
try|try
block|{
name|sc
operator|.
name|actionLaunch
argument_list|(
name|yarnfile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
name|name
argument_list|,
name|lifetime
argument_list|,
name|queue
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{       }
name|LOG
operator|.
name|debug
argument_list|(
literal|"Started the cluster via service API"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|YarnException
decl||
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
finally|finally
block|{
try|try
block|{
name|sc
operator|.
name|close
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
name|info
argument_list|(
literal|"Failed to close service client"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

