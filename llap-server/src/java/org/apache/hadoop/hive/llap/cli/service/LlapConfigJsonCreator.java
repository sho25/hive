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
operator|.
name|service
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStreamWriter
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
name|Charset
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
name|fs
operator|.
name|FSDataOutputStream
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
name|yarn
operator|.
name|conf
operator|.
name|YarnConfiguration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jettison
operator|.
name|json
operator|.
name|JSONException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jettison
operator|.
name|json
operator|.
name|JSONObject
import|;
end_import

begin_comment
comment|/**  * Creates the config json for llap start.  */
end_comment

begin_class
class|class
name|LlapConfigJsonCreator
block|{
comment|// This is not a config that users set in hive-site. It's only use is to share information
comment|// between the java component of the service driver and the python component.
specifier|private
specifier|static
specifier|final
name|String
name|CONFIG_CLUSTER_NAME
init|=
literal|"private.hive.llap.servicedriver.cluster.name"
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|final
name|Path
name|tmpDir
decl_stmt|;
specifier|private
specifier|final
name|long
name|cache
decl_stmt|;
specifier|private
specifier|final
name|long
name|xmx
decl_stmt|;
specifier|private
specifier|final
name|String
name|javaHome
decl_stmt|;
name|LlapConfigJsonCreator
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|tmpDir
parameter_list|,
name|long
name|cache
parameter_list|,
name|long
name|xmx
parameter_list|,
name|String
name|javaHome
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|tmpDir
operator|=
name|tmpDir
expr_stmt|;
name|this
operator|.
name|cache
operator|=
name|cache
expr_stmt|;
name|this
operator|.
name|xmx
operator|=
name|xmx
expr_stmt|;
name|this
operator|.
name|javaHome
operator|=
name|javaHome
expr_stmt|;
block|}
name|void
name|createLlapConfigJson
parameter_list|()
throws|throws
name|Exception
block|{
name|JSONObject
name|configs
init|=
name|createConfigJson
argument_list|()
decl_stmt|;
name|writeConfigJson
argument_list|(
name|configs
argument_list|)
expr_stmt|;
block|}
specifier|private
name|JSONObject
name|createConfigJson
parameter_list|()
throws|throws
name|JSONException
block|{
comment|// extract configs for processing by the python fragments in YARN Service
name|JSONObject
name|configs
init|=
operator|new
name|JSONObject
argument_list|()
decl_stmt|;
name|configs
operator|.
name|put
argument_list|(
literal|"java.home"
argument_list|,
name|javaHome
argument_list|)
expr_stmt|;
name|configs
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_YARN_CONTAINER_MB
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getLongVar
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_YARN_CONTAINER_MB
argument_list|)
argument_list|)
expr_stmt|;
name|configs
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|LLAP_IO_MEMORY_MAX_SIZE
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getSizeVar
argument_list|(
name|ConfVars
operator|.
name|LLAP_IO_MEMORY_MAX_SIZE
argument_list|)
argument_list|)
expr_stmt|;
name|configs
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|LLAP_ALLOCATOR_DIRECT
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_ALLOCATOR_DIRECT
argument_list|)
argument_list|)
expr_stmt|;
name|configs
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getIntVar
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB
argument_list|)
argument_list|)
expr_stmt|;
name|configs
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_VCPUS_PER_INSTANCE
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getIntVar
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_VCPUS_PER_INSTANCE
argument_list|)
argument_list|)
expr_stmt|;
name|configs
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_NUM_EXECUTORS
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getIntVar
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_NUM_EXECUTORS
argument_list|)
argument_list|)
expr_stmt|;
comment|// Let YARN pick the queue name, if it isn't provided in hive-site, or via the command-line
if|if
condition|(
name|conf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_QUEUE_NAME
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|configs
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_QUEUE_NAME
operator|.
name|varname
argument_list|,
name|conf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_QUEUE_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Propagate the cluster name to the script.
name|String
name|clusterHosts
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_SERVICE_HOSTS
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|clusterHosts
argument_list|)
operator|&&
name|clusterHosts
operator|.
name|startsWith
argument_list|(
literal|"@"
argument_list|)
operator|&&
name|clusterHosts
operator|.
name|length
argument_list|()
operator|>
literal|1
condition|)
block|{
name|configs
operator|.
name|put
argument_list|(
name|CONFIG_CLUSTER_NAME
argument_list|,
name|clusterHosts
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|configs
operator|.
name|put
argument_list|(
name|YarnConfiguration
operator|.
name|RM_SCHEDULER_MINIMUM_ALLOCATION_MB
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|YarnConfiguration
operator|.
name|RM_SCHEDULER_MINIMUM_ALLOCATION_MB
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|configs
operator|.
name|put
argument_list|(
name|YarnConfiguration
operator|.
name|RM_SCHEDULER_MINIMUM_ALLOCATION_VCORES
argument_list|,
name|conf
operator|.
name|getInt
argument_list|(
name|YarnConfiguration
operator|.
name|RM_SCHEDULER_MINIMUM_ALLOCATION_VCORES
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|maxDirect
init|=
operator|(
name|xmx
operator|>
literal|0
operator|&&
name|cache
operator|>
literal|0
operator|&&
name|xmx
operator|<
name|cache
operator|*
literal|1.25
operator|)
condition|?
call|(
name|long
call|)
argument_list|(
name|cache
operator|*
literal|1.25
argument_list|)
else|:
operator|-
literal|1
decl_stmt|;
name|configs
operator|.
name|put
argument_list|(
literal|"max_direct_memory"
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|maxDirect
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|configs
return|;
block|}
specifier|private
name|void
name|writeConfigJson
parameter_list|(
name|JSONObject
name|configs
parameter_list|)
throws|throws
name|Exception
block|{
try|try
init|(
name|FSDataOutputStream
name|fsdos
init|=
name|fs
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|tmpDir
argument_list|,
literal|"config.json"
argument_list|)
argument_list|)
init|;
name|OutputStreamWriter
name|w
operator|=
operator|new
name|OutputStreamWriter
argument_list|(
name|fsdos
argument_list|,
name|Charset
operator|.
name|defaultCharset
argument_list|()
argument_list|)
init|)
block|{
name|configs
operator|.
name|write
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

