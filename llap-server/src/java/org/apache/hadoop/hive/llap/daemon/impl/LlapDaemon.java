begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|daemon
operator|.
name|impl
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
name|InetSocketAddress
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|ObjectName
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
name|llap
operator|.
name|daemon
operator|.
name|ContainerRunner
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
name|registry
operator|.
name|impl
operator|.
name|LlapRegistryService
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
name|rpc
operator|.
name|LlapDaemonProtocolProtos
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
name|io
operator|.
name|api
operator|.
name|LlapIoProxy
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
name|metrics
operator|.
name|LlapDaemonExecutorMetrics
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
name|metrics
operator|.
name|LlapMetricsSystem
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
name|metrics
operator|.
name|MetricsUtils
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
name|shufflehandler
operator|.
name|ShuffleHandler
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
name|metrics2
operator|.
name|util
operator|.
name|MBeans
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
name|service
operator|.
name|AbstractService
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
name|JvmPauseMonitor
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
name|log4j
operator|.
name|Logger
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
name|annotations
operator|.
name|VisibleForTesting
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
name|base
operator|.
name|Joiner
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
name|base
operator|.
name|Preconditions
import|;
end_import

begin_class
specifier|public
class|class
name|LlapDaemon
extends|extends
name|AbstractService
implements|implements
name|ContainerRunner
implements|,
name|LlapDaemonMXBean
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|LlapDaemon
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|shuffleHandlerConf
decl_stmt|;
specifier|private
specifier|final
name|LlapDaemonProtocolServerImpl
name|server
decl_stmt|;
specifier|private
specifier|final
name|ContainerRunnerImpl
name|containerRunner
decl_stmt|;
specifier|private
specifier|final
name|LlapRegistryService
name|registry
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|numSubmissions
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
name|JvmPauseMonitor
name|pauseMonitor
decl_stmt|;
specifier|private
specifier|final
name|ObjectName
name|llapDaemonInfoBean
decl_stmt|;
specifier|private
specifier|final
name|LlapDaemonExecutorMetrics
name|metrics
decl_stmt|;
comment|// Parameters used for JMX
specifier|private
specifier|final
name|boolean
name|llapIoEnabled
decl_stmt|;
specifier|private
specifier|final
name|long
name|executorMemoryPerInstance
decl_stmt|;
specifier|private
specifier|final
name|long
name|ioMemoryPerInstance
decl_stmt|;
specifier|private
specifier|final
name|int
name|numExecutors
decl_stmt|;
specifier|private
specifier|final
name|long
name|maxJvmMemory
decl_stmt|;
specifier|private
specifier|final
name|String
index|[]
name|localDirs
decl_stmt|;
comment|// TODO Not the best way to share the address
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|InetSocketAddress
argument_list|>
name|address
init|=
operator|new
name|AtomicReference
argument_list|<
name|InetSocketAddress
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|LlapDaemon
parameter_list|(
name|Configuration
name|daemonConf
parameter_list|,
name|int
name|numExecutors
parameter_list|,
name|long
name|executorMemoryBytes
parameter_list|,
name|boolean
name|ioEnabled
parameter_list|,
name|long
name|ioMemoryBytes
parameter_list|,
name|String
index|[]
name|localDirs
parameter_list|,
name|int
name|rpcPort
parameter_list|,
name|int
name|shufflePort
parameter_list|)
block|{
name|super
argument_list|(
literal|"LlapDaemon"
argument_list|)
expr_stmt|;
name|printAsciiArt
argument_list|()
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|numExecutors
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|rpcPort
operator|==
literal|0
operator|||
operator|(
name|rpcPort
operator|>
literal|1024
operator|&&
name|rpcPort
operator|<
literal|65536
operator|)
argument_list|,
literal|"RPC Port must be between 1025 and 65535, or 0 automatic selection"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|localDirs
operator|!=
literal|null
operator|&&
name|localDirs
operator|.
name|length
operator|>
literal|0
argument_list|,
literal|"Work dirs must be specified"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|shufflePort
operator|==
literal|0
operator|||
operator|(
name|shufflePort
operator|>
literal|1024
operator|&&
name|shufflePort
operator|<
literal|65536
operator|)
argument_list|,
literal|"Shuffle Port must be betwee 1024 and 65535, or 0 for automatic selection"
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxJvmMemory
operator|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|maxMemory
argument_list|()
expr_stmt|;
name|this
operator|.
name|llapIoEnabled
operator|=
name|ioEnabled
expr_stmt|;
name|this
operator|.
name|executorMemoryPerInstance
operator|=
name|executorMemoryBytes
expr_stmt|;
name|this
operator|.
name|ioMemoryPerInstance
operator|=
name|ioMemoryBytes
expr_stmt|;
name|this
operator|.
name|numExecutors
operator|=
name|numExecutors
expr_stmt|;
name|this
operator|.
name|localDirs
operator|=
name|localDirs
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Attempting to start LlapDaemonConf with the following configuration: "
operator|+
literal|"numExecutors="
operator|+
name|numExecutors
operator|+
literal|", rpcListenerPort="
operator|+
name|rpcPort
operator|+
literal|", workDirs="
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|localDirs
argument_list|)
operator|+
literal|", shufflePort="
operator|+
name|shufflePort
operator|+
literal|", executorMemory="
operator|+
name|executorMemoryBytes
operator|+
literal|", llapIoEnabled="
operator|+
name|ioEnabled
operator|+
literal|", llapIoCacheSize="
operator|+
name|ioMemoryBytes
operator|+
literal|", jvmAvailableMemory="
operator|+
name|maxJvmMemory
argument_list|)
expr_stmt|;
name|long
name|memRequired
init|=
name|executorMemoryBytes
operator|+
operator|(
name|ioEnabled
condition|?
name|ioMemoryBytes
else|:
literal|0
operator|)
decl_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|maxJvmMemory
operator|>=
name|memRequired
argument_list|,
literal|"Invalid configuration. Xmx value too small. maxAvailable="
operator|+
name|maxJvmMemory
operator|+
literal|", configured(exec + io if enabled)="
operator|+
name|memRequired
argument_list|)
expr_stmt|;
name|this
operator|.
name|shuffleHandlerConf
operator|=
operator|new
name|Configuration
argument_list|(
name|daemonConf
argument_list|)
expr_stmt|;
name|this
operator|.
name|shuffleHandlerConf
operator|.
name|setInt
argument_list|(
name|ShuffleHandler
operator|.
name|SHUFFLE_PORT_CONFIG_KEY
argument_list|,
name|shufflePort
argument_list|)
expr_stmt|;
name|this
operator|.
name|shuffleHandlerConf
operator|.
name|set
argument_list|(
name|ShuffleHandler
operator|.
name|SHUFFLE_HANDLER_LOCAL_DIRS
argument_list|,
name|StringUtils
operator|.
name|arrayToString
argument_list|(
name|localDirs
argument_list|)
argument_list|)
expr_stmt|;
comment|// Less frequently set parameter, not passing in as a param.
name|int
name|numHandlers
init|=
name|daemonConf
operator|.
name|getInt
argument_list|(
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_RPC_NUM_HANDLERS
argument_list|,
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_RPC_NUM_HANDLERS_DEFAULT
argument_list|)
decl_stmt|;
name|this
operator|.
name|server
operator|=
operator|new
name|LlapDaemonProtocolServerImpl
argument_list|(
name|numHandlers
argument_list|,
name|this
argument_list|,
name|address
argument_list|,
name|rpcPort
argument_list|)
expr_stmt|;
comment|// Initialize the metric system
name|LlapMetricsSystem
operator|.
name|initialize
argument_list|(
literal|"LlapDaemon"
argument_list|)
expr_stmt|;
name|this
operator|.
name|pauseMonitor
operator|=
operator|new
name|JvmPauseMonitor
argument_list|(
name|daemonConf
argument_list|)
expr_stmt|;
name|pauseMonitor
operator|.
name|start
argument_list|()
expr_stmt|;
name|String
name|displayName
init|=
literal|"LlapDaemonExecutorMetrics-"
operator|+
name|MetricsUtils
operator|.
name|getHostName
argument_list|()
decl_stmt|;
name|String
name|sessionId
init|=
name|MetricsUtils
operator|.
name|getUUID
argument_list|()
decl_stmt|;
name|daemonConf
operator|.
name|set
argument_list|(
literal|"llap.daemon.metrics.sessionid"
argument_list|,
name|sessionId
argument_list|)
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|LlapDaemonExecutorMetrics
operator|.
name|create
argument_list|(
name|displayName
argument_list|,
name|sessionId
argument_list|,
name|numExecutors
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|getJvmMetrics
argument_list|()
operator|.
name|setPauseMonitor
argument_list|(
name|pauseMonitor
argument_list|)
expr_stmt|;
name|this
operator|.
name|llapDaemonInfoBean
operator|=
name|MBeans
operator|.
name|register
argument_list|(
literal|"LlapDaemon"
argument_list|,
literal|"LlapDaemonInfo"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started LlapMetricsSystem with displayName: "
operator|+
name|displayName
operator|+
literal|" sessionId: "
operator|+
name|sessionId
argument_list|)
expr_stmt|;
name|this
operator|.
name|containerRunner
operator|=
operator|new
name|ContainerRunnerImpl
argument_list|(
name|numExecutors
argument_list|,
name|localDirs
argument_list|,
name|shufflePort
argument_list|,
name|address
argument_list|,
name|executorMemoryBytes
argument_list|,
name|metrics
argument_list|)
expr_stmt|;
name|this
operator|.
name|registry
operator|=
operator|new
name|LlapRegistryService
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|printAsciiArt
parameter_list|()
block|{
specifier|final
name|String
name|asciiArt
init|=
literal|""
operator|+
literal|"$$\\       $$\\        $$$$$$\\  $$$$$$$\\\n"
operator|+
literal|"$$ |      $$ |      $$  __$$\\ $$  __$$\\\n"
operator|+
literal|"$$ |      $$ |      $$ /  $$ |$$ |  $$ |\n"
operator|+
literal|"$$ |      $$ |      $$$$$$$$ |$$$$$$$  |\n"
operator|+
literal|"$$ |      $$ |      $$  __$$ |$$  ____/\n"
operator|+
literal|"$$ |      $$ |      $$ |  $$ |$$ |\n"
operator|+
literal|"$$$$$$$$\\ $$$$$$$$\\ $$ |  $$ |$$ |\n"
operator|+
literal|"\\________|\\________|\\__|  \\__|\\__|\n"
operator|+
literal|"\n"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"\n\n"
operator|+
name|asciiArt
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceInit
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|server
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|containerRunner
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|registry
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|LlapIoProxy
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|LlapIoProxy
operator|.
name|initializeLlapIo
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceStart
parameter_list|()
throws|throws
name|Exception
block|{
name|ShuffleHandler
operator|.
name|initializeAndStart
argument_list|(
name|shuffleHandlerConf
argument_list|)
expr_stmt|;
name|server
operator|.
name|start
argument_list|()
expr_stmt|;
name|containerRunner
operator|.
name|start
argument_list|()
expr_stmt|;
name|registry
operator|.
name|start
argument_list|()
expr_stmt|;
name|registry
operator|.
name|registerWorker
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|serviceStop
parameter_list|()
throws|throws
name|Exception
block|{
comment|// TODO Shutdown LlapIO
name|shutdown
argument_list|()
expr_stmt|;
name|containerRunner
operator|.
name|stop
argument_list|()
expr_stmt|;
name|server
operator|.
name|stop
argument_list|()
expr_stmt|;
name|registry
operator|.
name|unregisterWorker
argument_list|()
expr_stmt|;
name|registry
operator|.
name|stop
argument_list|()
expr_stmt|;
name|ShuffleHandler
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"LlapDaemon shutdown invoked"
argument_list|)
expr_stmt|;
if|if
condition|(
name|llapDaemonInfoBean
operator|!=
literal|null
condition|)
block|{
name|MBeans
operator|.
name|unregister
argument_list|(
name|llapDaemonInfoBean
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|pauseMonitor
operator|!=
literal|null
condition|)
block|{
name|pauseMonitor
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|metrics
operator|!=
literal|null
condition|)
block|{
name|LlapMetricsSystem
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|LlapIoProxy
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|LlapDaemon
name|llapDaemon
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Cache settings will need to be setup in llap-daemon-site.xml - since the daemons don't read hive-site.xml
comment|// Ideally, these properties should be part of LlapDameonConf rather than HiveConf
name|LlapDaemonConfiguration
name|daemonConf
init|=
operator|new
name|LlapDaemonConfiguration
argument_list|()
decl_stmt|;
name|int
name|numExecutors
init|=
name|daemonConf
operator|.
name|getInt
argument_list|(
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_NUM_EXECUTORS
argument_list|,
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_NUM_EXECUTORS_DEFAULT
argument_list|)
decl_stmt|;
name|String
index|[]
name|localDirs
init|=
name|daemonConf
operator|.
name|getTrimmedStrings
argument_list|(
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_WORK_DIRS
argument_list|)
decl_stmt|;
name|int
name|rpcPort
init|=
name|daemonConf
operator|.
name|getInt
argument_list|(
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_RPC_PORT
argument_list|,
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_RPC_PORT_DEFAULT
argument_list|)
decl_stmt|;
name|int
name|shufflePort
init|=
name|daemonConf
operator|.
name|getInt
argument_list|(
name|ShuffleHandler
operator|.
name|SHUFFLE_PORT_CONFIG_KEY
argument_list|,
name|ShuffleHandler
operator|.
name|DEFAULT_SHUFFLE_PORT
argument_list|)
decl_stmt|;
name|long
name|executorMemoryBytes
init|=
name|daemonConf
operator|.
name|getInt
argument_list|(
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB
argument_list|,
name|LlapDaemonConfiguration
operator|.
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB_DEFAULT
argument_list|)
operator|*
literal|1024l
operator|*
literal|1024l
decl_stmt|;
name|long
name|cacheMemoryBytes
init|=
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|daemonConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_ORC_CACHE_MAX_SIZE
argument_list|)
decl_stmt|;
name|boolean
name|llapIoEnabled
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|daemonConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_IO_ENABLED
argument_list|)
decl_stmt|;
name|llapDaemon
operator|=
operator|new
name|LlapDaemon
argument_list|(
name|daemonConf
argument_list|,
name|numExecutors
argument_list|,
name|executorMemoryBytes
argument_list|,
name|llapIoEnabled
argument_list|,
name|cacheMemoryBytes
argument_list|,
name|localDirs
argument_list|,
name|rpcPort
argument_list|,
name|shufflePort
argument_list|)
expr_stmt|;
name|llapDaemon
operator|.
name|init
argument_list|(
name|daemonConf
argument_list|)
expr_stmt|;
name|llapDaemon
operator|.
name|start
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Started LlapDaemon"
argument_list|)
expr_stmt|;
comment|// Relying on the RPC threads to keep the service alive.
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// TODO Replace this with a ExceptionHandler / ShutdownHook
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to start LLAP Daemon with exception"
argument_list|,
name|t
argument_list|)
expr_stmt|;
if|if
condition|(
name|llapDaemon
operator|!=
literal|null
condition|)
block|{
name|llapDaemon
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|submitWork
parameter_list|(
name|LlapDaemonProtocolProtos
operator|.
name|SubmitWorkRequestProto
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|numSubmissions
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|containerRunner
operator|.
name|submitWork
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|long
name|getNumSubmissions
parameter_list|()
block|{
return|return
name|numSubmissions
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|InetSocketAddress
name|getListenerAddress
parameter_list|()
block|{
return|return
name|server
operator|.
name|getBindAddress
argument_list|()
return|;
block|}
comment|// LlapDaemonMXBean methods. Will be exposed via JMX
annotation|@
name|Override
specifier|public
name|int
name|getRpcPort
parameter_list|()
block|{
return|return
name|server
operator|.
name|getBindAddress
argument_list|()
operator|.
name|getPort
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getNumExecutors
parameter_list|()
block|{
return|return
name|numExecutors
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getShufflePort
parameter_list|()
block|{
return|return
name|ShuffleHandler
operator|.
name|get
argument_list|()
operator|.
name|getPort
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getLocalDirs
parameter_list|()
block|{
return|return
name|Joiner
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|skipNulls
argument_list|()
operator|.
name|join
argument_list|(
name|localDirs
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getExecutorMemoryPerInstance
parameter_list|()
block|{
return|return
name|executorMemoryPerInstance
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getIoMemoryPerInstance
parameter_list|()
block|{
return|return
name|ioMemoryPerInstance
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isIoEnabled
parameter_list|()
block|{
return|return
name|llapIoEnabled
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMaxJvmMemory
parameter_list|()
block|{
return|return
name|maxJvmMemory
return|;
block|}
block|}
end_class

end_unit

