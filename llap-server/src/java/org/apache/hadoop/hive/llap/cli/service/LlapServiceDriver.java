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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
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
name|hive
operator|.
name|llap
operator|.
name|LlapUtil
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
name|llap
operator|.
name|cli
operator|.
name|LlapSliderUtils
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
name|llap
operator|.
name|configuration
operator|.
name|LlapDaemonConfiguration
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
name|llap
operator|.
name|daemon
operator|.
name|impl
operator|.
name|LlapConstants
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
name|session
operator|.
name|SessionState
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
name|joda
operator|.
name|time
operator|.
name|DateTime
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
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Paths
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
name|Arrays
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
name|concurrent
operator|.
name|ExecutorService
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
name|Executors
import|;
end_import

begin_comment
comment|/** Starts the llap daemon. */
end_comment

begin_class
specifier|public
class|class
name|LlapServiceDriver
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
name|LlapServiceDriver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LLAP_RELATIVE_PACKAGE_DIR
init|=
literal|"/package/LLAP/"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OUTPUT_DIR_PREFIX
init|=
literal|"llap-yarn-"
decl_stmt|;
comment|/**    * This is a working configuration for the instance to merge various variables.    * It is not written out for llap server usage    */
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|final
name|LlapServiceCommandLine
name|cl
decl_stmt|;
specifier|public
name|LlapServiceDriver
parameter_list|(
name|LlapServiceCommandLine
name|cl
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|cl
operator|=
name|cl
expr_stmt|;
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|this
operator|.
name|conf
operator|=
operator|(
name|ss
operator|!=
literal|null
operator|)
condition|?
name|ss
operator|.
name|getConf
argument_list|()
else|:
operator|new
name|HiveConf
argument_list|(
name|SessionState
operator|.
name|class
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Cannot load any configuration to run command"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|int
name|run
parameter_list|()
throws|throws
name|Exception
block|{
name|Properties
name|propsDirectOptions
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
comment|// Working directory.
name|Path
name|tmpDir
init|=
operator|new
name|Path
argument_list|(
name|cl
operator|.
name|getDirectory
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|t0
init|=
name|System
operator|.
name|nanoTime
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
name|FileSystem
name|rawFs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
operator|.
name|getRawFileSystem
argument_list|()
decl_stmt|;
name|int
name|threadCount
init|=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
operator|/
literal|2
argument_list|)
decl_stmt|;
name|ExecutorService
name|executor
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|threadCount
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setNameFormat
argument_list|(
literal|"llap-pkg-%d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|rc
init|=
literal|0
decl_stmt|;
try|try
block|{
name|setupConf
argument_list|(
name|propsDirectOptions
argument_list|)
expr_stmt|;
name|URL
name|logger
init|=
name|conf
operator|.
name|getResource
argument_list|(
name|LlapConstants
operator|.
name|LOG4j2_PROPERTIES_FILE
argument_list|)
decl_stmt|;
if|if
condition|(
name|logger
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Unable to find required config file: llap-daemon-log4j2.properties"
argument_list|)
throw|;
block|}
name|Path
name|home
init|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_HOME"
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|scriptParent
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|home
argument_list|,
literal|"scripts"
argument_list|)
argument_list|,
literal|"llap"
argument_list|)
decl_stmt|;
name|Path
name|scripts
init|=
operator|new
name|Path
argument_list|(
name|scriptParent
argument_list|,
literal|"bin"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|rawFs
operator|.
name|exists
argument_list|(
name|home
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Unable to find HIVE_HOME:"
operator|+
name|home
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
operator|!
name|rawFs
operator|.
name|exists
argument_list|(
name|scripts
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to find llap scripts:"
operator|+
name|scripts
argument_list|)
expr_stmt|;
block|}
name|String
name|javaHome
init|=
name|getJavaHome
argument_list|()
decl_stmt|;
name|LlapTarComponentGatherer
name|tarComponentGatherer
init|=
operator|new
name|LlapTarComponentGatherer
argument_list|(
name|cl
argument_list|,
name|conf
argument_list|,
name|propsDirectOptions
argument_list|,
name|fs
argument_list|,
name|rawFs
argument_list|,
name|executor
argument_list|,
name|tmpDir
argument_list|)
decl_stmt|;
name|tarComponentGatherer
operator|.
name|createDirs
argument_list|()
expr_stmt|;
name|tarComponentGatherer
operator|.
name|submitTarComponentGatherTasks
argument_list|()
expr_stmt|;
comment|// TODO: need to move from Python to Java for the rest of the script.
name|LlapConfigJsonCreator
name|lcjCreator
init|=
operator|new
name|LlapConfigJsonCreator
argument_list|(
name|conf
argument_list|,
name|rawFs
argument_list|,
name|tmpDir
argument_list|,
name|cl
operator|.
name|getCache
argument_list|()
argument_list|,
name|cl
operator|.
name|getXmx
argument_list|()
argument_list|,
name|javaHome
argument_list|)
decl_stmt|;
name|lcjCreator
operator|.
name|createLlapConfigJson
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Config Json generation took "
operator|+
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|t0
operator|)
operator|+
literal|" ns"
argument_list|)
expr_stmt|;
name|tarComponentGatherer
operator|.
name|waitForFinish
argument_list|()
expr_stmt|;
if|if
condition|(
name|cl
operator|.
name|isStarting
argument_list|()
condition|)
block|{
name|rc
operator|=
name|startLlap
argument_list|(
name|tmpDir
argument_list|,
name|scriptParent
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rc
operator|=
literal|0
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|rawFs
operator|.
name|close
argument_list|()
expr_stmt|;
name|fs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|rc
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Exiting successfully"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Exiting with rc = "
operator|+
name|rc
argument_list|)
expr_stmt|;
block|}
return|return
name|rc
return|;
block|}
specifier|private
name|void
name|setupConf
parameter_list|(
name|Properties
name|propsDirectOptions
parameter_list|)
throws|throws
name|Exception
block|{
comment|// needed so that the file is actually loaded into configuration.
for|for
control|(
name|String
name|f
range|:
name|LlapDaemonConfiguration
operator|.
name|DAEMON_CONFIGS
control|)
block|{
name|conf
operator|.
name|addResource
argument_list|(
name|f
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|getResource
argument_list|(
name|f
argument_list|)
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Unable to find required config file: "
operator|+
name|f
argument_list|)
throw|;
block|}
block|}
for|for
control|(
name|String
name|f
range|:
name|LlapDaemonConfiguration
operator|.
name|SSL_DAEMON_CONFIGS
control|)
block|{
name|conf
operator|.
name|addResource
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|reloadConfiguration
argument_list|()
expr_stmt|;
name|populateConfWithLlapProperties
argument_list|(
name|conf
argument_list|,
name|cl
operator|.
name|getConfig
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|cl
operator|.
name|getName
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// update service registry configs - caveat: this has nothing to do with the actual settings as read by the AM
comment|// if needed, use --hiveconf llap.daemon.service.hosts=@llap0 to dynamically switch between instances
name|conf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_SERVICE_HOSTS
operator|.
name|varname
argument_list|,
literal|"@"
operator|+
name|cl
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|propsDirectOptions
operator|.
name|setProperty
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_SERVICE_HOSTS
operator|.
name|varname
argument_list|,
literal|"@"
operator|+
name|cl
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cl
operator|.
name|getLogger
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_LOGGER
argument_list|,
name|cl
operator|.
name|getLogger
argument_list|()
argument_list|)
expr_stmt|;
name|propsDirectOptions
operator|.
name|setProperty
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_LOGGER
operator|.
name|varname
argument_list|,
name|cl
operator|.
name|getLogger
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|boolean
name|isDirect
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_ALLOCATOR_DIRECT
argument_list|)
decl_stmt|;
name|String
name|cacheStr
init|=
name|LlapUtil
operator|.
name|humanReadableByteCount
argument_list|(
name|cl
operator|.
name|getCache
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|sizeStr
init|=
name|LlapUtil
operator|.
name|humanReadableByteCount
argument_list|(
name|cl
operator|.
name|getSize
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|xmxStr
init|=
name|LlapUtil
operator|.
name|humanReadableByteCount
argument_list|(
name|cl
operator|.
name|getXmx
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|cl
operator|.
name|getSize
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|cl
operator|.
name|getCache
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
operator|!
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_ALLOCATOR_MAPPED
argument_list|)
condition|)
block|{
comment|// direct heap allocations need to be safer
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|cl
operator|.
name|getCache
argument_list|()
operator|<
name|cl
operator|.
name|getSize
argument_list|()
argument_list|,
literal|"Cache size ("
operator|+
name|cacheStr
operator|+
literal|") has to be smaller"
operator|+
literal|" than the container sizing ("
operator|+
name|sizeStr
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cl
operator|.
name|getCache
argument_list|()
operator|<
name|cl
operator|.
name|getSize
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Note that this might need YARN physical memory monitoring to be turned off "
operator|+
literal|"(yarn.nodemanager.pmem-check-enabled=false)"
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|cl
operator|.
name|getXmx
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|cl
operator|.
name|getXmx
argument_list|()
operator|<
name|cl
operator|.
name|getSize
argument_list|()
argument_list|,
literal|"Working memory (Xmx="
operator|+
name|xmxStr
operator|+
literal|") has to be"
operator|+
literal|" smaller than the container sizing ("
operator|+
name|sizeStr
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isDirect
operator|&&
operator|!
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_ALLOCATOR_MAPPED
argument_list|)
condition|)
block|{
comment|// direct and not memory mapped
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|cl
operator|.
name|getXmx
argument_list|()
operator|+
name|cl
operator|.
name|getCache
argument_list|()
operator|<=
name|cl
operator|.
name|getSize
argument_list|()
argument_list|,
literal|"Working memory (Xmx="
operator|+
name|xmxStr
operator|+
literal|") + cache size ("
operator|+
name|cacheStr
operator|+
literal|") has to be smaller than the container sizing ("
operator|+
name|sizeStr
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|cl
operator|.
name|getExecutors
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
name|conf
operator|.
name|setLong
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_NUM_EXECUTORS
operator|.
name|varname
argument_list|,
name|cl
operator|.
name|getExecutors
argument_list|()
argument_list|)
expr_stmt|;
name|propsDirectOptions
operator|.
name|setProperty
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_NUM_EXECUTORS
operator|.
name|varname
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|cl
operator|.
name|getExecutors
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// TODO: vcpu settings - possibly when DRFA works right
block|}
if|if
condition|(
name|cl
operator|.
name|getIoThreads
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
name|conf
operator|.
name|setLong
argument_list|(
name|ConfVars
operator|.
name|LLAP_IO_THREADPOOL_SIZE
operator|.
name|varname
argument_list|,
name|cl
operator|.
name|getIoThreads
argument_list|()
argument_list|)
expr_stmt|;
name|propsDirectOptions
operator|.
name|setProperty
argument_list|(
name|ConfVars
operator|.
name|LLAP_IO_THREADPOOL_SIZE
operator|.
name|varname
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|cl
operator|.
name|getIoThreads
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|long
name|cache
init|=
name|cl
operator|.
name|getCache
argument_list|()
decl_stmt|;
if|if
condition|(
name|cache
operator|!=
operator|-
literal|1
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_IO_MEMORY_MAX_SIZE
operator|.
name|varname
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|cache
argument_list|)
argument_list|)
expr_stmt|;
name|propsDirectOptions
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_IO_MEMORY_MAX_SIZE
operator|.
name|varname
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|cache
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|long
name|xmx
init|=
name|cl
operator|.
name|getXmx
argument_list|()
decl_stmt|;
if|if
condition|(
name|xmx
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// Needs more explanation here
comment|// Xmx is not the max heap value in JDK8. You need to subtract 50% of the survivor fraction
comment|// from this, to get actual usable memory before it goes into GC
name|long
name|xmxMb
init|=
operator|(
name|xmx
operator|/
operator|(
literal|1024L
operator|*
literal|1024L
operator|)
operator|)
decl_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB
operator|.
name|varname
argument_list|,
name|xmxMb
argument_list|)
expr_stmt|;
name|propsDirectOptions
operator|.
name|setProperty
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB
operator|.
name|varname
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|xmxMb
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|long
name|containerSize
init|=
name|cl
operator|.
name|getSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|containerSize
operator|==
operator|-
literal|1
condition|)
block|{
name|long
name|heapSize
init|=
name|xmx
decl_stmt|;
if|if
condition|(
operator|!
name|isDirect
condition|)
block|{
name|heapSize
operator|+=
name|cache
expr_stmt|;
block|}
name|containerSize
operator|=
name|Math
operator|.
name|min
argument_list|(
call|(
name|long
call|)
argument_list|(
name|heapSize
operator|*
literal|1.2
argument_list|)
argument_list|,
name|heapSize
operator|+
literal|1024L
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
if|if
condition|(
name|isDirect
condition|)
block|{
name|containerSize
operator|+=
name|cache
expr_stmt|;
block|}
block|}
name|long
name|containerSizeMB
init|=
name|containerSize
operator|/
operator|(
literal|1024
operator|*
literal|1024
operator|)
decl_stmt|;
name|long
name|minAllocMB
init|=
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
decl_stmt|;
name|String
name|containerSizeStr
init|=
name|LlapUtil
operator|.
name|humanReadableByteCount
argument_list|(
name|containerSize
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|containerSizeMB
operator|>=
name|minAllocMB
argument_list|,
literal|"Container size ("
operator|+
name|containerSizeStr
operator|+
literal|") should be "
operator|+
literal|"greater than minimum allocation("
operator|+
name|LlapUtil
operator|.
name|humanReadableByteCount
argument_list|(
name|minAllocMB
operator|*
literal|1024L
operator|*
literal|1024L
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_YARN_CONTAINER_MB
operator|.
name|varname
argument_list|,
name|containerSizeMB
argument_list|)
expr_stmt|;
name|propsDirectOptions
operator|.
name|setProperty
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_YARN_CONTAINER_MB
operator|.
name|varname
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|containerSizeMB
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Memory settings: container memory: {} executor memory: {} cache memory: {}"
argument_list|,
name|containerSizeStr
argument_list|,
name|xmxStr
argument_list|,
name|cacheStr
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|cl
operator|.
name|getLlapQueueName
argument_list|()
argument_list|)
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_QUEUE_NAME
operator|.
name|varname
argument_list|,
name|cl
operator|.
name|getLlapQueueName
argument_list|()
argument_list|)
expr_stmt|;
name|propsDirectOptions
operator|.
name|setProperty
argument_list|(
name|ConfVars
operator|.
name|LLAP_DAEMON_QUEUE_NAME
operator|.
name|varname
argument_list|,
name|cl
operator|.
name|getLlapQueueName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|getJavaHome
parameter_list|()
block|{
name|String
name|javaHome
init|=
name|cl
operator|.
name|getJavaPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|javaHome
argument_list|)
condition|)
block|{
name|javaHome
operator|=
name|System
operator|.
name|getenv
argument_list|(
literal|"JAVA_HOME"
argument_list|)
expr_stmt|;
name|String
name|jreHome
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.home"
argument_list|)
decl_stmt|;
if|if
condition|(
name|javaHome
operator|==
literal|null
condition|)
block|{
name|javaHome
operator|=
name|jreHome
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|javaHome
operator|.
name|equals
argument_list|(
name|jreHome
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Java versions might not match : JAVA_HOME=[{}],process jre=[{}]"
argument_list|,
name|javaHome
argument_list|,
name|jreHome
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|javaHome
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Could not determine JAVA_HOME from command line parameters, environment or system properties"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Using [{}] for JAVA_HOME"
argument_list|,
name|javaHome
argument_list|)
expr_stmt|;
return|return
name|javaHome
return|;
block|}
specifier|private
specifier|static
name|void
name|populateConfWithLlapProperties
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|properties
parameter_list|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|props
range|:
name|properties
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
name|props
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getLlapDaemonConfVars
argument_list|()
operator|.
name|contains
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|key
argument_list|,
operator|(
name|String
operator|)
name|props
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|key
operator|.
name|startsWith
argument_list|(
name|HiveConf
operator|.
name|PREFIX_LLAP
argument_list|)
operator|||
name|key
operator|.
name|startsWith
argument_list|(
name|HiveConf
operator|.
name|PREFIX_HIVE_LLAP
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Adding key [{}] even though it is not in the set of known llap-server keys"
argument_list|,
name|key
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|key
argument_list|,
operator|(
name|String
operator|)
name|props
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Ignoring unknown llap server parameter: [{}]"
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|int
name|startLlap
parameter_list|(
name|Path
name|tmpDir
parameter_list|,
name|Path
name|scriptParent
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|int
name|rc
decl_stmt|;
name|String
name|version
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_VERSION"
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|version
argument_list|)
condition|)
block|{
name|version
operator|=
name|DateTime
operator|.
name|now
argument_list|()
operator|.
name|toString
argument_list|(
literal|"ddMMMyyyy"
argument_list|)
expr_stmt|;
block|}
name|String
name|outputDir
init|=
name|cl
operator|.
name|getOutput
argument_list|()
decl_stmt|;
name|Path
name|packageDir
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|outputDir
operator|==
literal|null
condition|)
block|{
name|outputDir
operator|=
name|OUTPUT_DIR_PREFIX
operator|+
name|version
expr_stmt|;
name|packageDir
operator|=
operator|new
name|Path
argument_list|(
name|Paths
operator|.
name|get
argument_list|(
literal|"."
argument_list|)
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|OUTPUT_DIR_PREFIX
operator|+
name|version
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|packageDir
operator|=
operator|new
name|Path
argument_list|(
name|outputDir
argument_list|)
expr_stmt|;
block|}
name|rc
operator|=
name|runPackagePy
argument_list|(
name|tmpDir
argument_list|,
name|scriptParent
argument_list|,
name|version
argument_list|,
name|outputDir
argument_list|)
expr_stmt|;
if|if
condition|(
name|rc
operator|==
literal|0
condition|)
block|{
name|String
name|tarballName
init|=
literal|"llap-"
operator|+
name|version
operator|+
literal|".tar.gz"
decl_stmt|;
name|startCluster
argument_list|(
name|conf
argument_list|,
name|cl
operator|.
name|getName
argument_list|()
argument_list|,
name|tarballName
argument_list|,
name|packageDir
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
return|return
name|rc
return|;
block|}
specifier|private
name|int
name|runPackagePy
parameter_list|(
name|Path
name|tmpDir
parameter_list|,
name|Path
name|scriptParent
parameter_list|,
name|String
name|version
parameter_list|,
name|String
name|outputDir
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Path
name|scriptPath
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|scriptParent
argument_list|,
literal|"yarn"
argument_list|)
argument_list|,
literal|"package.py"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|scriptArgs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|cl
operator|.
name|getArgs
argument_list|()
operator|.
name|length
operator|+
literal|7
argument_list|)
decl_stmt|;
name|scriptArgs
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"python"
argument_list|,
name|scriptPath
operator|.
name|toString
argument_list|()
argument_list|,
literal|"--input"
argument_list|,
name|tmpDir
operator|.
name|toString
argument_list|()
argument_list|,
literal|"--output"
argument_list|,
name|outputDir
argument_list|,
literal|"--javaChild"
argument_list|)
argument_list|)
expr_stmt|;
name|scriptArgs
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|cl
operator|.
name|getArgs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Calling package.py via: "
operator|+
name|scriptArgs
argument_list|)
expr_stmt|;
name|ProcessBuilder
name|builder
init|=
operator|new
name|ProcessBuilder
argument_list|(
name|scriptArgs
argument_list|)
decl_stmt|;
name|builder
operator|.
name|redirectError
argument_list|(
name|ProcessBuilder
operator|.
name|Redirect
operator|.
name|INHERIT
argument_list|)
expr_stmt|;
name|builder
operator|.
name|redirectOutput
argument_list|(
name|ProcessBuilder
operator|.
name|Redirect
operator|.
name|INHERIT
argument_list|)
expr_stmt|;
name|builder
operator|.
name|environment
argument_list|()
operator|.
name|put
argument_list|(
literal|"HIVE_VERSION"
argument_list|,
name|version
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|start
argument_list|()
operator|.
name|waitFor
argument_list|()
return|;
block|}
specifier|private
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
name|LlapSliderUtils
operator|.
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
name|String
name|llapPackageDir
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_HDFS_PACKAGE_DIR
argument_list|)
operator|+
name|LLAP_RELATIVE_PACKAGE_DIR
decl_stmt|;
name|fs
operator|.
name|createWithPermissions
argument_list|(
operator|new
name|Path
argument_list|(
name|llapPackageDir
argument_list|)
argument_list|,
name|FsPermission
operator|.
name|getDirDefault
argument_list|()
argument_list|)
expr_stmt|;
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
name|llapPackageDir
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
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|LlapServiceCommandLine
name|cl
init|=
operator|new
name|LlapServiceCommandLine
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|int
name|ret
init|=
literal|0
decl_stmt|;
try|try
block|{
name|ret
operator|=
operator|new
name|LlapServiceDriver
argument_list|(
name|cl
argument_list|)
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Failed: "
operator|+
name|t
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|ret
operator|=
literal|3
expr_stmt|;
block|}
finally|finally
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"LLAP service driver finished"
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Completed processing - exiting with "
operator|+
name|ret
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

