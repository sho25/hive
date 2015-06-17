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
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryMXBean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryPoolMXBean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryType
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
name|Set
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
name|configuration
operator|.
name|LlapConfiguration
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
name|QueryFailedHandler
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
operator|.
name|QueryCompleteRequestProto
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
operator|.
name|SourceStateUpdatedRequestProto
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
operator|.
name|SubmitWorkRequestProto
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
operator|.
name|TerminateFragmentRequestProto
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
name|services
operator|.
name|impl
operator|.
name|LlapWebServices
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
name|CompositeService
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
name|ExitUtil
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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|ShutdownHookManager
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
name|LlapDaemon
extends|extends
name|CompositeService
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
name|LoggerFactory
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
name|AMReporter
name|amReporter
decl_stmt|;
specifier|private
specifier|final
name|LlapRegistryService
name|registry
decl_stmt|;
specifier|private
specifier|final
name|LlapWebServices
name|webServices
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
specifier|final
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
name|boolean
name|isDirectCache
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
name|getTotalHeapSize
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
name|int
name|waitQueueSize
init|=
name|daemonConf
operator|.
name|getInt
argument_list|(
name|LlapConfiguration
operator|.
name|LLAP_DAEMON_TASK_SCHEDULER_WAIT_QUEUE_SIZE
argument_list|,
name|LlapConfiguration
operator|.
name|LLAP_DAEMON_TASK_SCHEDULER_WAIT_QUEUE_SIZE_DEFAULT
argument_list|)
decl_stmt|;
name|boolean
name|enablePreemption
init|=
name|daemonConf
operator|.
name|getBoolean
argument_list|(
name|LlapConfiguration
operator|.
name|LLAP_DAEMON_TASK_SCHEDULER_ENABLE_PREEMPTION
argument_list|,
name|LlapConfiguration
operator|.
name|LLAP_DAEMON_TASK_SCHEDULER_ENABLE_PREEMPTION_DEFAULT
argument_list|)
decl_stmt|;
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
literal|", llapIoCacheIsDirect="
operator|+
name|isDirectCache
operator|+
literal|", llapIoCacheSize="
operator|+
name|ioMemoryBytes
operator|+
literal|", jvmAvailableMemory="
operator|+
name|maxJvmMemory
operator|+
literal|", waitQueueSize= "
operator|+
name|waitQueueSize
operator|+
literal|", enablePreemption= "
operator|+
name|enablePreemption
argument_list|)
expr_stmt|;
name|long
name|memRequired
init|=
name|executorMemoryBytes
operator|+
operator|(
name|ioEnabled
operator|&&
name|isDirectCache
operator|==
literal|false
condition|?
name|ioMemoryBytes
else|:
literal|0
operator|)
decl_stmt|;
comment|// TODO: this check is somewhat bogus as the maxJvmMemory != Xmx parameters (see annotation in LlapServiceDriver)
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
name|this
operator|.
name|shuffleHandlerConf
operator|.
name|setBoolean
argument_list|(
name|ShuffleHandler
operator|.
name|SHUFFLE_DIR_WATCHER_ENABLED
argument_list|,
name|daemonConf
operator|.
name|getBoolean
argument_list|(
name|LlapConfiguration
operator|.
name|LLAP_DAEMON_SHUFFLE_DIR_WATCHER_ENABLED
argument_list|,
name|LlapConfiguration
operator|.
name|LLAP_DAEMON_SHUFFLE_DIR_WATCHER_ENABLED_DEFAULT
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
name|LlapConfiguration
operator|.
name|LLAP_DAEMON_RPC_NUM_HANDLERS
argument_list|,
name|LlapConfiguration
operator|.
name|LLAP_DAEMON_RPC_NUM_HANDLERS_DEFAULT
argument_list|)
decl_stmt|;
comment|// Initialize the metrics system
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
name|amReporter
operator|=
operator|new
name|AMReporter
argument_list|(
name|address
argument_list|,
operator|new
name|QueryFailedHandlerProxy
argument_list|()
argument_list|,
name|daemonConf
argument_list|)
expr_stmt|;
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
name|this
operator|.
name|containerRunner
operator|=
operator|new
name|ContainerRunnerImpl
argument_list|(
name|daemonConf
argument_list|,
name|numExecutors
argument_list|,
name|waitQueueSize
argument_list|,
name|enablePreemption
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
argument_list|,
name|amReporter
argument_list|)
expr_stmt|;
name|addIfService
argument_list|(
name|containerRunner
argument_list|)
expr_stmt|;
name|this
operator|.
name|registry
operator|=
operator|new
name|LlapRegistryService
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|addIfService
argument_list|(
name|registry
argument_list|)
expr_stmt|;
name|this
operator|.
name|webServices
operator|=
operator|new
name|LlapWebServices
argument_list|()
expr_stmt|;
name|addIfService
argument_list|(
name|webServices
argument_list|)
expr_stmt|;
comment|// Bring up the server only after all other components have started.
name|addIfService
argument_list|(
name|server
argument_list|)
expr_stmt|;
comment|// AMReporter after the server so that it gets the correct address. It knows how to deal with
comment|// requests before it is started.
name|addIfService
argument_list|(
name|amReporter
argument_list|)
expr_stmt|;
block|}
specifier|private
name|long
name|getTotalHeapSize
parameter_list|()
block|{
comment|// runtime.getMax() gives a very different number from the actual Xmx sizing.
comment|// you can iterate through the
comment|// http://docs.oracle.com/javase/7/docs/api/java/lang/management/MemoryPoolMXBean.html
comment|// from java.lang.management to figure this out, but the hard-coded params in the llap run.sh
comment|// result in 89% usable heap (-XX:NewRatio=8) + a survivor region which is technically not
comment|// in the usable space.
name|long
name|total
init|=
literal|0
decl_stmt|;
for|for
control|(
name|MemoryPoolMXBean
name|mp
range|:
name|ManagementFactory
operator|.
name|getMemoryPoolMXBeans
argument_list|()
control|)
block|{
name|long
name|sz
init|=
name|mp
operator|.
name|getUsage
argument_list|()
operator|.
name|getMax
argument_list|()
decl_stmt|;
if|if
condition|(
name|mp
operator|.
name|getName
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Survivor"
argument_list|)
condition|)
block|{
name|sz
operator|*=
literal|2
expr_stmt|;
comment|// there are 2 survivor spaces
block|}
if|if
condition|(
name|mp
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|MemoryType
operator|.
name|HEAP
argument_list|)
condition|)
block|{
name|total
operator|+=
name|sz
expr_stmt|;
block|}
block|}
comment|// round up to the next MB
name|total
operator|+=
operator|(
name|total
operator|%
operator|(
literal|1024
operator|*
literal|1024
operator|)
operator|)
expr_stmt|;
return|return
name|total
return|;
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
throws|throws
name|Exception
block|{
name|super
operator|.
name|serviceInit
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
comment|// Start the Shuffle service before the listener - until it's a service as well.
name|ShuffleHandler
operator|.
name|initializeAndStart
argument_list|(
name|shuffleHandlerConf
argument_list|)
expr_stmt|;
name|super
operator|.
name|serviceStart
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"LlapDaemon serviceStart complete"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|serviceStop
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|serviceStop
argument_list|()
expr_stmt|;
name|ShuffleHandler
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|shutdown
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"LlapDaemon shutdown complete"
argument_list|)
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
name|Thread
operator|.
name|setDefaultUncaughtExceptionHandler
argument_list|(
operator|new
name|LlapDaemonUncaughtExceptionHandler
argument_list|()
argument_list|)
expr_stmt|;
name|LlapDaemon
name|llapDaemon
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Cache settings will need to be setup in llap-daemon-site.xml - since the daemons don't read hive-site.xml
comment|// Ideally, these properties should be part of LlapDameonConf rather than HiveConf
name|LlapConfiguration
name|daemonConf
init|=
operator|new
name|LlapConfiguration
argument_list|()
decl_stmt|;
name|int
name|numExecutors
init|=
name|daemonConf
operator|.
name|getInt
argument_list|(
name|LlapConfiguration
operator|.
name|LLAP_DAEMON_NUM_EXECUTORS
argument_list|,
name|LlapConfiguration
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
name|LlapConfiguration
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
name|LlapConfiguration
operator|.
name|LLAP_DAEMON_RPC_PORT
argument_list|,
name|LlapConfiguration
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
name|LlapConfiguration
operator|.
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB
argument_list|,
name|LlapConfiguration
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
name|isDirectCache
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
name|LLAP_ORC_CACHE_ALLOCATE_DIRECT
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
name|isDirectCache
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding shutdown hook for LlapDaemon"
argument_list|)
expr_stmt|;
name|ShutdownHookManager
operator|.
name|addShutdownHook
argument_list|(
operator|new
name|CompositeServiceShutdownHook
argument_list|(
name|llapDaemon
argument_list|)
argument_list|,
literal|1
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
name|Override
specifier|public
name|void
name|sourceStateUpdated
parameter_list|(
name|SourceStateUpdatedRequestProto
name|request
parameter_list|)
block|{
name|containerRunner
operator|.
name|sourceStateUpdated
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|queryComplete
parameter_list|(
name|QueryCompleteRequestProto
name|request
parameter_list|)
block|{
name|containerRunner
operator|.
name|queryComplete
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|terminateFragment
parameter_list|(
name|TerminateFragmentRequestProto
name|request
parameter_list|)
block|{
name|containerRunner
operator|.
name|terminateFragment
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
name|Set
argument_list|<
name|String
argument_list|>
name|getExecutorsStatus
parameter_list|()
block|{
return|return
name|containerRunner
operator|.
name|getExecutorStatus
argument_list|()
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
specifier|private
specifier|static
class|class
name|LlapDaemonUncaughtExceptionHandler
implements|implements
name|Thread
operator|.
name|UncaughtExceptionHandler
block|{
annotation|@
name|Override
specifier|public
name|void
name|uncaughtException
parameter_list|(
name|Thread
name|t
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"UncaughtExceptionHandler invoked"
argument_list|)
expr_stmt|;
if|if
condition|(
name|ShutdownHookManager
operator|.
name|isShutdownInProgress
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Thread {} threw a Throwable, but we are shutting down, so ignoring this"
argument_list|,
name|t
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|e
operator|instanceof
name|Error
condition|)
block|{
try|try
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Thread {} threw an Error.  Shutting down now..."
argument_list|,
name|t
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|err
parameter_list|)
block|{
comment|//We don't want to not exit because of an issue with logging
block|}
if|if
condition|(
name|e
operator|instanceof
name|OutOfMemoryError
condition|)
block|{
comment|//After catching an OOM java says it is undefined behavior, so don't
comment|//even try to clean up or we can get stuck on shutdown.
try|try
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Halting due to Out Of Memory Error..."
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|err
parameter_list|)
block|{
comment|//Again we done want to exit because of logging issues.
block|}
name|ExitUtil
operator|.
name|halt
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ExitUtil
operator|.
name|terminate
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Thread {} threw an Exception. Shutting down now..."
argument_list|,
name|t
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|ExitUtil
operator|.
name|terminate
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
class|class
name|QueryFailedHandlerProxy
implements|implements
name|QueryFailedHandler
block|{
annotation|@
name|Override
specifier|public
name|void
name|queryFailed
parameter_list|(
name|String
name|queryId
parameter_list|,
name|String
name|dagName
parameter_list|)
block|{
name|containerRunner
operator|.
name|queryFailed
argument_list|(
name|queryId
argument_list|,
name|dagName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

