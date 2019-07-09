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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|primitives
operator|.
name|Ints
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
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
name|DaemonId
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
name|LlapDaemonTestUtils
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
name|RegisterDagRequestProto
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
name|QueryIdentifierProto
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
name|security
operator|.
name|LlapTokenIdentifier
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
name|security
operator|.
name|LlapUgiFactoryFactory
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
name|hive
operator|.
name|llap
operator|.
name|tezplugins
operator|.
name|LlapTezUtils
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
name|io
operator|.
name|Text
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
name|net
operator|.
name|NetUtils
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
name|token
operator|.
name|Token
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|common
operator|.
name|security
operator|.
name|TokenCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|net
operator|.
name|SocketFactory
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_comment
comment|/**  * Test ContainerRunnerImpl.  */
end_comment

begin_class
specifier|public
class|class
name|TestContainerRunnerImpl
block|{
name|ContainerRunnerImpl
name|containerRunner
decl_stmt|;
name|LlapDaemonConfiguration
name|daemonConf
init|=
operator|new
name|LlapDaemonConfiguration
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|numExecutors
init|=
literal|1
decl_stmt|;
specifier|private
specifier|final
name|int
name|waitQueueSize
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|daemonConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_TASK_SCHEDULER_WAIT_QUEUE_SIZE
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|enablePreemption
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|int
name|numLocalDirs
init|=
literal|1
decl_stmt|;
specifier|private
specifier|final
name|String
index|[]
name|localDirs
init|=
operator|new
name|String
index|[
name|numLocalDirs
index|]
decl_stmt|;
specifier|private
specifier|final
name|File
name|testWorkDir
init|=
operator|new
name|File
argument_list|(
literal|"target"
argument_list|,
literal|"container-runner-tests"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|Integer
argument_list|>
name|shufflePort
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|InetSocketAddress
argument_list|>
name|srvAddress
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|executorMemoryPerInstance
init|=
literal|1024
decl_stmt|;
specifier|private
name|LlapDaemonExecutorMetrics
name|metrics
decl_stmt|;
specifier|private
name|AMReporter
name|amReporter
decl_stmt|;
specifier|private
specifier|final
name|String
name|testUser
init|=
literal|"testUser"
decl_stmt|;
specifier|private
specifier|final
name|String
name|appId
init|=
literal|"application_1540489363818_0021"
decl_stmt|;
specifier|private
specifier|final
name|int
name|dagId
init|=
literal|1234
decl_stmt|;
specifier|private
specifier|final
name|int
name|vId
init|=
literal|12345
decl_stmt|;
specifier|private
specifier|final
name|String
name|hostname
init|=
literal|"test.cluster"
decl_stmt|;
specifier|private
specifier|final
name|DaemonId
name|daemonId
init|=
operator|new
name|DaemonId
argument_list|(
name|testUser
argument_list|,
literal|"ContainerTests"
argument_list|,
name|hostname
argument_list|,
name|appId
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|SocketFactory
name|socketFactory
init|=
name|NetUtils
operator|.
name|getDefaultSocketFactory
argument_list|(
name|daemonConf
argument_list|)
decl_stmt|;
specifier|private
name|QueryTracker
name|queryTracker
decl_stmt|;
specifier|private
name|TaskExecutorService
name|executorService
decl_stmt|;
specifier|private
name|InetSocketAddress
name|serverSocket
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|strIntervals
init|=
name|HiveConf
operator|.
name|getTrimmedStringsVar
argument_list|(
name|daemonConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_TASK_PREEMPTION_METRICS_INTERVALS
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|intervalList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|strIntervals
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|strInterval
range|:
name|strIntervals
control|)
block|{
name|intervalList
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|strInterval
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|amReporter
operator|=
name|mock
argument_list|(
name|AMReporter
operator|.
name|class
argument_list|)
expr_stmt|;
name|serverSocket
operator|=
operator|new
name|InetSocketAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|srvAddress
operator|.
name|set
argument_list|(
name|serverSocket
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
literal|"ContinerRunerTests"
argument_list|,
name|MetricsUtils
operator|.
name|getUUID
argument_list|()
argument_list|,
name|numExecutors
argument_list|,
name|waitQueueSize
argument_list|,
name|Ints
operator|.
name|toArray
argument_list|(
name|intervalList
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|0L
argument_list|,
literal|0
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numLocalDirs
condition|;
name|i
operator|++
control|)
block|{
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|testWorkDir
argument_list|,
literal|"localDir"
argument_list|)
decl_stmt|;
name|f
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|localDirs
index|[
name|i
index|]
operator|=
name|f
operator|.
name|getAbsolutePath
argument_list|()
expr_stmt|;
block|}
name|String
name|waitQueueSchedulerClassName
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|daemonConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_WAIT_QUEUE_COMPARATOR_CLASS_NAME
argument_list|)
decl_stmt|;
name|queryTracker
operator|=
operator|new
name|QueryTracker
argument_list|(
name|daemonConf
argument_list|,
name|localDirs
argument_list|,
name|daemonId
operator|.
name|getClusterString
argument_list|()
argument_list|)
expr_stmt|;
name|executorService
operator|=
operator|new
name|TaskExecutorService
argument_list|(
name|numExecutors
argument_list|,
name|waitQueueSize
argument_list|,
name|waitQueueSchedulerClassName
argument_list|,
name|enablePreemption
argument_list|,
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
argument_list|,
name|metrics
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|shufflePort
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|daemonConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_RPC_PORT
argument_list|)
argument_list|)
expr_stmt|;
name|containerRunner
operator|=
operator|new
name|ContainerRunnerImpl
argument_list|(
name|daemonConf
argument_list|,
name|numExecutors
argument_list|,
name|this
operator|.
name|shufflePort
argument_list|,
name|srvAddress
argument_list|,
name|executorMemoryPerInstance
argument_list|,
name|metrics
argument_list|,
name|amReporter
argument_list|,
name|queryTracker
argument_list|,
name|executorService
argument_list|,
name|daemonId
argument_list|,
name|LlapUgiFactoryFactory
operator|.
name|createFsUgiFactory
argument_list|(
name|daemonConf
argument_list|)
argument_list|,
name|socketFactory
argument_list|)
expr_stmt|;
name|ShuffleHandler
operator|.
name|initializeAndStart
argument_list|(
name|daemonConf
argument_list|)
expr_stmt|;
name|executorService
operator|.
name|init
argument_list|(
name|daemonConf
argument_list|)
expr_stmt|;
name|executorService
operator|.
name|start
argument_list|()
expr_stmt|;
name|queryTracker
operator|.
name|init
argument_list|(
name|daemonConf
argument_list|)
expr_stmt|;
name|queryTracker
operator|.
name|start
argument_list|()
expr_stmt|;
name|containerRunner
operator|.
name|init
argument_list|(
name|daemonConf
argument_list|)
expr_stmt|;
name|containerRunner
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|cleanup
parameter_list|()
throws|throws
name|Exception
block|{
name|containerRunner
operator|.
name|serviceStop
argument_list|()
expr_stmt|;
name|queryTracker
operator|.
name|serviceStop
argument_list|()
expr_stmt|;
name|executorService
operator|.
name|serviceStop
argument_list|()
expr_stmt|;
name|executorService
operator|.
name|serviceStop
argument_list|()
expr_stmt|;
name|LlapMetricsSystem
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10000
argument_list|)
specifier|public
name|void
name|testRegisterDag
parameter_list|()
throws|throws
name|Exception
block|{
name|Credentials
name|credentials
init|=
operator|new
name|Credentials
argument_list|()
decl_stmt|;
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|sessionToken
init|=
operator|new
name|Token
argument_list|<>
argument_list|(
literal|"identifier"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"testPassword"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Text
argument_list|(
literal|"kind"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"service"
argument_list|)
argument_list|)
decl_stmt|;
name|TokenCache
operator|.
name|setSessionToken
argument_list|(
name|sessionToken
argument_list|,
name|credentials
argument_list|)
expr_stmt|;
name|RegisterDagRequestProto
name|request
init|=
name|RegisterDagRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setUser
argument_list|(
name|testUser
argument_list|)
operator|.
name|setCredentialsBinary
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|LlapTezUtils
operator|.
name|serializeCredentials
argument_list|(
name|credentials
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setQueryIdentifier
argument_list|(
name|QueryIdentifierProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|setApplicationIdString
argument_list|(
name|appId
argument_list|)
operator|.
name|setDagIndex
argument_list|(
name|dagId
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|containerRunner
operator|.
name|registerDag
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ShuffleHandler
operator|.
name|get
argument_list|()
operator|.
name|getRegisteredApps
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ShuffleHandler
operator|.
name|get
argument_list|()
operator|.
name|getRegisteredApps
argument_list|()
operator|.
name|get
argument_list|(
name|appId
argument_list|)
argument_list|,
name|dagId
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ShuffleHandler
operator|.
name|get
argument_list|()
operator|.
name|getRegisteredDirectories
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|containerRunner
operator|.
name|registerDag
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ShuffleHandler
operator|.
name|get
argument_list|()
operator|.
name|getRegisteredApps
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ShuffleHandler
operator|.
name|get
argument_list|()
operator|.
name|getRegisteredApps
argument_list|()
operator|.
name|get
argument_list|(
name|appId
argument_list|)
argument_list|,
name|dagId
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ShuffleHandler
operator|.
name|get
argument_list|()
operator|.
name|getRegisteredDirectories
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|SubmitWorkRequestProto
name|sRequest
init|=
name|LlapDaemonTestUtils
operator|.
name|buildSubmitProtoRequest
argument_list|(
literal|1
argument_list|,
name|appId
argument_list|,
name|dagId
argument_list|,
name|vId
argument_list|,
literal|"dagName"
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
name|credentials
argument_list|)
decl_stmt|;
name|containerRunner
operator|.
name|submitWork
argument_list|(
name|sRequest
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ShuffleHandler
operator|.
name|get
argument_list|()
operator|.
name|getRegisteredApps
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ShuffleHandler
operator|.
name|get
argument_list|()
operator|.
name|getRegisteredApps
argument_list|()
operator|.
name|get
argument_list|(
name|appId
argument_list|)
argument_list|,
name|dagId
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ShuffleHandler
operator|.
name|get
argument_list|()
operator|.
name|getRegisteredDirectories
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ShuffleHandler
operator|.
name|get
argument_list|()
operator|.
name|getRegisteredDirectories
argument_list|()
operator|.
name|get
argument_list|(
name|appId
argument_list|)
argument_list|,
name|dagId
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

