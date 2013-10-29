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
operator|.
name|tool
package|;
end_package

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

begin_comment
comment|/**  * Execute a local program.  This is a singleton service that will  * execute a programs on the local box.  *   * Note that is is executed from LaunchMapper which is executed in   * different JVM from WebHCat (Templeton) server.  Thus it should not call any classes  * not available on every node in the cluster (outside webhcat jar)  */
end_comment

begin_class
specifier|final
class|class
name|TrivialExecService
block|{
comment|//with default log4j config, this output ends up in 'syslog' of the LaunchMapper task
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
name|TrivialExecService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|volatile
name|TrivialExecService
name|theSingleton
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HADOOP_CLIENT_OPTS
init|=
literal|"HADOOP_CLIENT_OPTS"
decl_stmt|;
comment|/**    * Retrieve the singleton.    */
specifier|public
specifier|static
specifier|synchronized
name|TrivialExecService
name|getInstance
parameter_list|()
block|{
if|if
condition|(
name|theSingleton
operator|==
literal|null
condition|)
name|theSingleton
operator|=
operator|new
name|TrivialExecService
argument_list|()
expr_stmt|;
return|return
name|theSingleton
return|;
block|}
comment|/**    * See {@link JobSubmissionConstants#CONTAINER_LOG4J_PROPS} file for details.    */
specifier|private
specifier|static
name|void
name|hadoop2LogRedirect
parameter_list|(
name|ProcessBuilder
name|processBuilder
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
init|=
name|processBuilder
operator|.
name|environment
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|env
operator|.
name|containsKey
argument_list|(
name|HADOOP_CLIENT_OPTS
argument_list|)
condition|)
block|{
return|return;
block|}
name|String
name|hcopts
init|=
name|env
operator|.
name|get
argument_list|(
name|HADOOP_CLIENT_OPTS
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|hcopts
operator|.
name|contains
argument_list|(
literal|"log4j.configuration=container-log4j.properties"
argument_list|)
condition|)
block|{
return|return;
block|}
comment|//TempletonControllerJob ensures that this file is in DistributedCache
name|File
name|log4jProps
init|=
operator|new
name|File
argument_list|(
name|JobSubmissionConstants
operator|.
name|CONTAINER_LOG4J_PROPS
argument_list|)
decl_stmt|;
name|hcopts
operator|=
name|hcopts
operator|.
name|replace
argument_list|(
literal|"log4j.configuration=container-log4j.properties"
argument_list|,
literal|"log4j.configuration=file://"
operator|+
name|log4jProps
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
comment|//helps figure out what log4j is doing, but may confuse
comment|//some jobs due to extra output to stdout
comment|//hcopts = hcopts + " -Dlog4j.debug=true";
name|env
operator|.
name|put
argument_list|(
name|HADOOP_CLIENT_OPTS
argument_list|,
name|hcopts
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Process
name|run
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|cmd
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|removeEnv
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|environmentVariables
parameter_list|,
name|boolean
name|overrideContainerLog4jProps
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"run(cmd, removeEnv, environmentVariables, "
operator|+
name|overrideContainerLog4jProps
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting cmd: "
operator|+
name|cmd
argument_list|)
expr_stmt|;
name|ProcessBuilder
name|pb
init|=
operator|new
name|ProcessBuilder
argument_list|(
name|cmd
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|removeEnv
control|)
block|{
if|if
condition|(
name|pb
operator|.
name|environment
argument_list|()
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Removing env var: "
operator|+
name|key
operator|+
literal|"="
operator|+
name|pb
operator|.
name|environment
argument_list|()
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|pb
operator|.
name|environment
argument_list|()
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
name|pb
operator|.
name|environment
argument_list|()
operator|.
name|putAll
argument_list|(
name|environmentVariables
argument_list|)
expr_stmt|;
if|if
condition|(
name|overrideContainerLog4jProps
condition|)
block|{
name|hadoop2LogRedirect
argument_list|(
name|pb
argument_list|)
expr_stmt|;
block|}
name|logDebugInfo
argument_list|(
literal|"Starting process with env:"
argument_list|,
name|pb
operator|.
name|environment
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|pb
operator|.
name|start
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|void
name|logDebugInfo
parameter_list|(
name|String
name|msg
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|keys
operator|.
name|addAll
argument_list|(
name|props
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|keys
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|key
range|:
name|keys
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|key
operator|+
literal|"="
operator|+
name|props
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

