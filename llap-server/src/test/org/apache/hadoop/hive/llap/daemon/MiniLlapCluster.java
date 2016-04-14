begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  *  you may not use this file except in compliance with the License.  *  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|zookeeper
operator|.
name|MiniZooKeeperCluster
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
name|FileContext
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
name|hive
operator|.
name|llap
operator|.
name|daemon
operator|.
name|impl
operator|.
name|LlapDaemon
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
name|service
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
name|util
operator|.
name|Shell
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
name|YarnRuntimeException
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
name|runtime
operator|.
name|library
operator|.
name|api
operator|.
name|TezRuntimeConfiguration
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
name|MiniLlapCluster
extends|extends
name|AbstractService
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
name|MiniLlapCluster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|File
name|testWorkDir
decl_stmt|;
specifier|private
specifier|final
name|String
name|clusterNameTrimmed
decl_stmt|;
specifier|private
specifier|final
name|long
name|numInstances
decl_stmt|;
specifier|private
specifier|final
name|long
name|execBytesPerService
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|llapIoEnabled
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|ioIsDirect
decl_stmt|;
specifier|private
specifier|final
name|long
name|ioBytesPerService
decl_stmt|;
specifier|private
specifier|final
name|int
name|numExecutorsPerService
decl_stmt|;
specifier|private
specifier|final
name|File
name|zkWorkDir
decl_stmt|;
specifier|private
specifier|final
name|String
index|[]
name|localDirs
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|clusterSpecificConfiguration
init|=
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|LlapDaemon
index|[]
name|llapDaemons
decl_stmt|;
specifier|private
name|MiniZooKeeperCluster
name|miniZooKeeperCluster
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|ownZkCluster
decl_stmt|;
specifier|public
specifier|static
name|MiniLlapCluster
name|create
parameter_list|(
name|String
name|clusterName
parameter_list|,
annotation|@
name|Nullable
name|MiniZooKeeperCluster
name|miniZkCluster
parameter_list|,
name|int
name|numInstances
parameter_list|,
name|int
name|numExecutorsPerService
parameter_list|,
name|long
name|execBytePerService
parameter_list|,
name|boolean
name|llapIoEnabled
parameter_list|,
name|boolean
name|ioIsDirect
parameter_list|,
name|long
name|ioBytesPerService
parameter_list|,
name|int
name|numLocalDirs
parameter_list|)
block|{
return|return
operator|new
name|MiniLlapCluster
argument_list|(
name|clusterName
argument_list|,
name|miniZkCluster
argument_list|,
name|numInstances
argument_list|,
name|numExecutorsPerService
argument_list|,
name|execBytePerService
argument_list|,
name|llapIoEnabled
argument_list|,
name|ioIsDirect
argument_list|,
name|ioBytesPerService
argument_list|,
name|numLocalDirs
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|MiniLlapCluster
name|create
parameter_list|(
name|String
name|clusterName
parameter_list|,
annotation|@
name|Nullable
name|MiniZooKeeperCluster
name|miniZkCluster
parameter_list|,
name|int
name|numExecutorsPerService
parameter_list|,
name|long
name|execBytePerService
parameter_list|,
name|boolean
name|llapIoEnabled
parameter_list|,
name|boolean
name|ioIsDirect
parameter_list|,
name|long
name|ioBytesPerService
parameter_list|,
name|int
name|numLocalDirs
parameter_list|)
block|{
return|return
name|create
argument_list|(
name|clusterName
argument_list|,
name|miniZkCluster
argument_list|,
literal|1
argument_list|,
name|numExecutorsPerService
argument_list|,
name|execBytePerService
argument_list|,
name|llapIoEnabled
argument_list|,
name|ioIsDirect
argument_list|,
name|ioBytesPerService
argument_list|,
name|numLocalDirs
argument_list|)
return|;
block|}
specifier|private
name|MiniLlapCluster
parameter_list|(
name|String
name|clusterName
parameter_list|,
annotation|@
name|Nullable
name|MiniZooKeeperCluster
name|miniZkCluster
parameter_list|,
name|int
name|numInstances
parameter_list|,
name|int
name|numExecutorsPerService
parameter_list|,
name|long
name|execMemoryPerService
parameter_list|,
name|boolean
name|llapIoEnabled
parameter_list|,
name|boolean
name|ioIsDirect
parameter_list|,
name|long
name|ioBytesPerService
parameter_list|,
name|int
name|numLocalDirs
parameter_list|)
block|{
name|super
argument_list|(
name|clusterName
operator|+
literal|"_"
operator|+
name|MiniLlapCluster
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|numExecutorsPerService
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|execMemoryPerService
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|numLocalDirs
operator|>
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|numInstances
operator|=
name|numInstances
expr_stmt|;
name|this
operator|.
name|clusterNameTrimmed
operator|=
name|clusterName
operator|.
name|replace
argument_list|(
literal|"$"
argument_list|,
literal|""
argument_list|)
operator|+
literal|"_"
operator|+
name|MiniLlapCluster
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
expr_stmt|;
name|this
operator|.
name|llapDaemons
operator|=
operator|new
name|LlapDaemon
index|[
name|numInstances
index|]
expr_stmt|;
name|File
name|targetWorkDir
init|=
operator|new
name|File
argument_list|(
literal|"target"
argument_list|,
name|clusterNameTrimmed
argument_list|)
decl_stmt|;
try|try
block|{
name|FileContext
operator|.
name|getLocalFSFileContext
argument_list|()
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|targetWorkDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not cleanup test workDir: "
operator|+
name|targetWorkDir
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Could not cleanup test workDir: "
operator|+
name|targetWorkDir
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|Shell
operator|.
name|WINDOWS
condition|)
block|{
comment|// The test working directory can exceed the maximum path length supported
comment|// by some Windows APIs and cmd.exe (260 characters).  To work around this,
comment|// create a symlink in temporary storage with a much shorter path,
comment|// targeting the full path to the test working directory.  Then, use the
comment|// symlink as the test working directory.
name|String
name|targetPath
init|=
name|targetWorkDir
operator|.
name|getAbsolutePath
argument_list|()
decl_stmt|;
name|File
name|link
init|=
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|linkPath
init|=
name|link
operator|.
name|getAbsolutePath
argument_list|()
decl_stmt|;
try|try
block|{
name|FileContext
operator|.
name|getLocalFSFileContext
argument_list|()
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|linkPath
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|YarnRuntimeException
argument_list|(
literal|"could not cleanup symlink: "
operator|+
name|linkPath
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// Guarantee target exists before creating symlink.
name|targetWorkDir
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|Shell
operator|.
name|ShellCommandExecutor
name|shexec
init|=
operator|new
name|Shell
operator|.
name|ShellCommandExecutor
argument_list|(
name|Shell
operator|.
name|getSymlinkCommand
argument_list|(
name|targetPath
argument_list|,
name|linkPath
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|shexec
operator|.
name|execute
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|YarnRuntimeException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"failed to create symlink from %s to %s, shell output: %s"
argument_list|,
name|linkPath
argument_list|,
name|targetPath
argument_list|,
name|shexec
operator|.
name|getOutput
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|this
operator|.
name|testWorkDir
operator|=
name|link
expr_stmt|;
block|}
else|else
block|{
name|targetWorkDir
operator|.
name|mkdir
argument_list|()
expr_stmt|;
name|this
operator|.
name|testWorkDir
operator|=
name|targetWorkDir
expr_stmt|;
block|}
if|if
condition|(
name|miniZkCluster
operator|==
literal|null
condition|)
block|{
name|ownZkCluster
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|zkWorkDir
operator|=
operator|new
name|File
argument_list|(
name|testWorkDir
argument_list|,
literal|"mini-zk-cluster"
argument_list|)
expr_stmt|;
name|zkWorkDir
operator|.
name|mkdir
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|miniZooKeeperCluster
operator|=
name|miniZkCluster
expr_stmt|;
name|ownZkCluster
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|zkWorkDir
operator|=
literal|null
expr_stmt|;
block|}
name|this
operator|.
name|numExecutorsPerService
operator|=
name|numExecutorsPerService
expr_stmt|;
name|this
operator|.
name|execBytesPerService
operator|=
name|execMemoryPerService
expr_stmt|;
name|this
operator|.
name|ioIsDirect
operator|=
name|ioIsDirect
expr_stmt|;
name|this
operator|.
name|llapIoEnabled
operator|=
name|llapIoEnabled
expr_stmt|;
name|this
operator|.
name|ioBytesPerService
operator|=
name|ioBytesPerService
expr_stmt|;
comment|// Setup Local Dirs
name|localDirs
operator|=
operator|new
name|String
index|[
name|numLocalDirs
index|]
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Created localDir: "
operator|+
name|f
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
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
name|IOException
throws|,
name|InterruptedException
block|{
name|int
name|rpcPort
init|=
literal|0
decl_stmt|;
name|int
name|mngPort
init|=
literal|0
decl_stmt|;
name|int
name|shufflePort
init|=
literal|0
decl_stmt|;
name|int
name|webPort
init|=
literal|0
decl_stmt|;
name|boolean
name|usePortsFromConf
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"minillap.usePortsFromConf"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"MiniLlap configured to use ports from conf: {}"
argument_list|,
name|usePortsFromConf
argument_list|)
expr_stmt|;
if|if
condition|(
name|usePortsFromConf
condition|)
block|{
name|rpcPort
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_RPC_PORT
argument_list|)
expr_stmt|;
name|mngPort
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_MANAGEMENT_RPC_PORT
argument_list|)
expr_stmt|;
name|shufflePort
operator|=
name|conf
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
expr_stmt|;
name|webPort
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_WEB_PORT
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ownZkCluster
condition|)
block|{
name|miniZooKeeperCluster
operator|=
operator|new
name|MiniZooKeeperCluster
argument_list|()
expr_stmt|;
name|miniZooKeeperCluster
operator|.
name|startup
argument_list|(
name|zkWorkDir
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Already setup in the create method
block|}
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
name|clusterNameTrimmed
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_QUORUM
operator|.
name|varname
argument_list|,
literal|"localhost"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_CLIENT_PORT
operator|.
name|varname
argument_list|,
name|miniZooKeeperCluster
operator|.
name|getClientPort
argument_list|()
argument_list|)
expr_stmt|;
comment|// Also add ZK settings to clusterSpecificConf to make sure these get picked up by whoever started this.
name|clusterSpecificConfiguration
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
name|clusterNameTrimmed
argument_list|)
expr_stmt|;
name|clusterSpecificConfiguration
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_QUORUM
operator|.
name|varname
argument_list|,
literal|"localhost"
argument_list|)
expr_stmt|;
name|clusterSpecificConfiguration
operator|.
name|setInt
argument_list|(
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_CLIENT_PORT
operator|.
name|varname
argument_list|,
name|miniZooKeeperCluster
operator|.
name|getClientPort
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Initializing {} llap instances for MiniLlapCluster with name={}"
argument_list|,
name|numInstances
argument_list|,
name|clusterNameTrimmed
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
name|numInstances
condition|;
name|i
operator|++
control|)
block|{
name|llapDaemons
index|[
name|i
index|]
operator|=
operator|new
name|LlapDaemon
argument_list|(
name|conf
argument_list|,
name|numExecutorsPerService
argument_list|,
name|execBytesPerService
argument_list|,
name|llapIoEnabled
argument_list|,
name|ioIsDirect
argument_list|,
name|ioBytesPerService
argument_list|,
name|localDirs
argument_list|,
name|rpcPort
argument_list|,
name|mngPort
argument_list|,
name|shufflePort
argument_list|,
name|webPort
argument_list|)
expr_stmt|;
name|llapDaemons
index|[
name|i
index|]
operator|.
name|init
argument_list|(
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Initialized {} llap instances for MiniLlapCluster with name={}"
argument_list|,
name|numInstances
argument_list|,
name|clusterNameTrimmed
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceStart
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting {} llap instances for MiniLlapCluster with name={}"
argument_list|,
name|numInstances
argument_list|,
name|clusterNameTrimmed
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
name|numInstances
condition|;
name|i
operator|++
control|)
block|{
name|llapDaemons
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Started {} llap instances for MiniLlapCluster with name={}"
argument_list|,
name|numInstances
argument_list|,
name|clusterNameTrimmed
argument_list|)
expr_stmt|;
comment|// Optimize local fetch does not work with LLAP due to different local directories
comment|// used by containers and LLAP
name|clusterSpecificConfiguration
operator|.
name|setBoolean
argument_list|(
name|TezRuntimeConfiguration
operator|.
name|TEZ_RUNTIME_OPTIMIZE_LOCAL_FETCH
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceStop
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numInstances
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|llapDaemons
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
name|llapDaemons
index|[
name|i
index|]
operator|.
name|stop
argument_list|()
expr_stmt|;
name|llapDaemons
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
if|if
condition|(
name|ownZkCluster
condition|)
block|{
if|if
condition|(
name|miniZooKeeperCluster
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping MiniZooKeeper cluster"
argument_list|)
expr_stmt|;
name|miniZooKeeperCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|miniZooKeeperCluster
operator|=
literal|null
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopped MiniZooKeeper cluster"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Not stopping MiniZK cluster since it is now owned by us"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Configuration
name|getClusterSpecificConfiguration
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|getServiceState
argument_list|()
operator|==
name|Service
operator|.
name|STATE
operator|.
name|STARTED
argument_list|)
expr_stmt|;
return|return
name|clusterSpecificConfiguration
return|;
block|}
comment|// Mainly for verification
specifier|public
name|long
name|getNumSubmissions
parameter_list|()
block|{
name|int
name|numSubmissions
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numInstances
condition|;
name|i
operator|++
control|)
block|{
name|numSubmissions
operator|+=
name|llapDaemons
index|[
name|i
index|]
operator|.
name|getNumSubmissions
argument_list|()
expr_stmt|;
block|}
return|return
name|numSubmissions
return|;
block|}
block|}
end_class

end_unit

