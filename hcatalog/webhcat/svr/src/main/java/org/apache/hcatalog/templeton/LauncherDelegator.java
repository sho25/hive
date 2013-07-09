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
name|IOException
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
name|ArrayList
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|exec
operator|.
name|ExecuteException
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|util
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
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|JobState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|TempletonControllerJob
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|TempletonStorage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|TempletonUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|ZooKeeperStorage
import|;
end_import

begin_comment
comment|/**  * The helper class for all the Templeton delegator classes that  * launch child jobs.  */
end_comment

begin_class
specifier|public
class|class
name|LauncherDelegator
extends|extends
name|TempletonDelegator
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Server
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JAR_CLASS
init|=
name|TempletonControllerJob
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|protected
name|String
name|runAs
init|=
literal|null
decl_stmt|;
specifier|public
name|LauncherDelegator
parameter_list|(
name|AppConfig
name|appConf
parameter_list|)
block|{
name|super
argument_list|(
name|appConf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|registerJob
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|user
parameter_list|,
name|String
name|callback
parameter_list|)
throws|throws
name|IOException
block|{
name|JobState
name|state
init|=
literal|null
decl_stmt|;
try|try
block|{
name|state
operator|=
operator|new
name|JobState
argument_list|(
name|id
argument_list|,
name|Main
operator|.
name|getAppConfigInstance
argument_list|()
argument_list|)
expr_stmt|;
name|state
operator|.
name|setUser
argument_list|(
name|user
argument_list|)
expr_stmt|;
name|state
operator|.
name|setCallback
argument_list|(
name|callback
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|state
operator|!=
literal|null
condition|)
name|state
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * Enqueue the TempletonControllerJob directly calling doAs.      */
specifier|public
name|EnqueueBean
name|enqueueController
parameter_list|(
name|String
name|user
parameter_list|,
name|String
name|callback
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|)
throws|throws
name|NotAuthorizedException
throws|,
name|BusyException
throws|,
name|ExecuteException
throws|,
name|IOException
throws|,
name|QueueException
block|{
try|try
block|{
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
name|long
name|startTime
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|String
name|id
init|=
name|queueAsUser
argument_list|(
name|ugi
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|long
name|elapsed
init|=
operator|(
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|startTime
operator|)
operator|/
operator|(
operator|(
name|int
operator|)
literal|1e6
operator|)
operator|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"queued job "
operator|+
name|id
operator|+
literal|" in "
operator|+
name|elapsed
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
if|if
condition|(
name|id
operator|==
literal|null
condition|)
throw|throw
operator|new
name|QueueException
argument_list|(
literal|"Unable to get job id"
argument_list|)
throw|;
name|registerJob
argument_list|(
name|id
argument_list|,
name|user
argument_list|,
name|callback
argument_list|)
expr_stmt|;
return|return
operator|new
name|EnqueueBean
argument_list|(
name|id
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueueException
argument_list|(
literal|"Unable to launch job "
operator|+
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|String
name|queueAsUser
parameter_list|(
name|UserGroupInformation
name|ugi
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|id
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
name|Exception
block|{
name|String
index|[]
name|array
init|=
operator|new
name|String
index|[
name|args
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|TempletonControllerJob
name|ctrl
init|=
operator|new
name|TempletonControllerJob
argument_list|()
decl_stmt|;
name|ToolRunner
operator|.
name|run
argument_list|(
name|ctrl
argument_list|,
name|args
operator|.
name|toArray
argument_list|(
name|array
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|ctrl
operator|.
name|getSubmittedId
argument_list|()
return|;
block|}
block|}
argument_list|)
decl_stmt|;
return|return
name|id
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|makeLauncherArgs
parameter_list|(
name|AppConfig
name|appConf
parameter_list|,
name|String
name|statusdir
parameter_list|,
name|String
name|completedUrl
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|copyFiles
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|args
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-libjars"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|appConf
operator|.
name|libJars
argument_list|()
argument_list|)
expr_stmt|;
name|addCacheFiles
argument_list|(
name|args
argument_list|,
name|appConf
argument_list|)
expr_stmt|;
comment|// Hadoop vars
name|addDef
argument_list|(
name|args
argument_list|,
literal|"user.name"
argument_list|,
name|runAs
argument_list|)
expr_stmt|;
name|addDef
argument_list|(
name|args
argument_list|,
name|AppConfig
operator|.
name|HADOOP_SPECULATIVE_NAME
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|addDef
argument_list|(
name|args
argument_list|,
name|AppConfig
operator|.
name|HADOOP_CHILD_JAVA_OPTS
argument_list|,
name|appConf
operator|.
name|controllerMRChildOpts
argument_list|()
argument_list|)
expr_stmt|;
comment|// Internal vars
name|addDef
argument_list|(
name|args
argument_list|,
name|TempletonControllerJob
operator|.
name|STATUSDIR_NAME
argument_list|,
name|statusdir
argument_list|)
expr_stmt|;
name|addDef
argument_list|(
name|args
argument_list|,
name|TempletonControllerJob
operator|.
name|COPY_NAME
argument_list|,
name|TempletonUtils
operator|.
name|encodeArray
argument_list|(
name|copyFiles
argument_list|)
argument_list|)
expr_stmt|;
name|addDef
argument_list|(
name|args
argument_list|,
name|TempletonControllerJob
operator|.
name|OVERRIDE_CLASSPATH
argument_list|,
name|makeOverrideClasspath
argument_list|(
name|appConf
argument_list|)
argument_list|)
expr_stmt|;
comment|// Hadoop queue information
name|addDef
argument_list|(
name|args
argument_list|,
literal|"mapred.job.queue.name"
argument_list|,
name|appConf
operator|.
name|hadoopQueueName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Job vars
name|addStorageVars
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|addCompletionVars
argument_list|(
name|args
argument_list|,
name|completedUrl
argument_list|)
expr_stmt|;
return|return
name|args
return|;
block|}
comment|// Storage vars
specifier|private
name|void
name|addStorageVars
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|)
block|{
name|addDef
argument_list|(
name|args
argument_list|,
name|TempletonStorage
operator|.
name|STORAGE_CLASS
argument_list|,
name|appConf
operator|.
name|get
argument_list|(
name|TempletonStorage
operator|.
name|STORAGE_CLASS
argument_list|)
argument_list|)
expr_stmt|;
name|addDef
argument_list|(
name|args
argument_list|,
name|TempletonStorage
operator|.
name|STORAGE_ROOT
argument_list|,
name|appConf
operator|.
name|get
argument_list|(
name|TempletonStorage
operator|.
name|STORAGE_ROOT
argument_list|)
argument_list|)
expr_stmt|;
name|addDef
argument_list|(
name|args
argument_list|,
name|ZooKeeperStorage
operator|.
name|ZK_HOSTS
argument_list|,
name|appConf
operator|.
name|get
argument_list|(
name|ZooKeeperStorage
operator|.
name|ZK_HOSTS
argument_list|)
argument_list|)
expr_stmt|;
name|addDef
argument_list|(
name|args
argument_list|,
name|ZooKeeperStorage
operator|.
name|ZK_SESSION_TIMEOUT
argument_list|,
name|appConf
operator|.
name|get
argument_list|(
name|ZooKeeperStorage
operator|.
name|ZK_SESSION_TIMEOUT
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Completion notifier vars
specifier|private
name|void
name|addCompletionVars
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|,
name|String
name|completedUrl
parameter_list|)
block|{
name|addDef
argument_list|(
name|args
argument_list|,
name|AppConfig
operator|.
name|HADOOP_END_RETRY_NAME
argument_list|,
name|appConf
operator|.
name|get
argument_list|(
name|AppConfig
operator|.
name|CALLBACK_RETRY_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|addDef
argument_list|(
name|args
argument_list|,
name|AppConfig
operator|.
name|HADOOP_END_INTERVAL_NAME
argument_list|,
name|appConf
operator|.
name|get
argument_list|(
name|AppConfig
operator|.
name|CALLBACK_INTERVAL_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|addDef
argument_list|(
name|args
argument_list|,
name|AppConfig
operator|.
name|HADOOP_END_URL_NAME
argument_list|,
name|completedUrl
argument_list|)
expr_stmt|;
block|}
comment|/**      * Add files to the Distributed Cache for the controller job.      */
specifier|public
specifier|static
name|void
name|addCacheFiles
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|,
name|AppConfig
name|appConf
parameter_list|)
block|{
name|String
name|overrides
init|=
name|appConf
operator|.
name|overrideJarsString
argument_list|()
decl_stmt|;
if|if
condition|(
name|overrides
operator|!=
literal|null
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-files"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|overrides
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Create the override classpath, which will be added to      * HADOOP_CLASSPATH at runtime by the controller job.      */
specifier|public
specifier|static
name|String
name|makeOverrideClasspath
parameter_list|(
name|AppConfig
name|appConf
parameter_list|)
block|{
name|String
index|[]
name|overrides
init|=
name|appConf
operator|.
name|overrideJars
argument_list|()
decl_stmt|;
if|if
condition|(
name|overrides
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|cp
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
name|String
name|fname
range|:
name|overrides
control|)
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|fname
argument_list|)
decl_stmt|;
name|cp
operator|.
name|add
argument_list|(
name|p
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|StringUtils
operator|.
name|join
argument_list|(
literal|":"
argument_list|,
name|cp
argument_list|)
return|;
block|}
comment|/**      * Add a Hadoop command line definition to args if the value is      * not null.      */
specifier|public
specifier|static
name|void
name|addDef
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|val
parameter_list|)
block|{
if|if
condition|(
name|val
operator|!=
literal|null
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
name|name
operator|+
literal|"="
operator|+
name|val
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

