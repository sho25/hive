begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ql
operator|.
name|exec
operator|.
name|tez
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|EnumSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|ql
operator|.
name|Context
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
name|DriverContext
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
name|exec
operator|.
name|Operator
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
name|exec
operator|.
name|Task
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
name|exec
operator|.
name|Utilities
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
name|log
operator|.
name|PerfLogger
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
name|plan
operator|.
name|BaseWork
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
name|plan
operator|.
name|MapWork
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
name|plan
operator|.
name|OperatorDesc
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
name|plan
operator|.
name|ReduceWork
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
name|plan
operator|.
name|TezEdgeProperty
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
name|plan
operator|.
name|TezEdgeProperty
operator|.
name|EdgeType
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
name|plan
operator|.
name|TezWork
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
name|plan
operator|.
name|UnionWork
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
name|plan
operator|.
name|api
operator|.
name|StageType
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
name|mapred
operator|.
name|JobConf
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|LocalResource
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
name|counters
operator|.
name|CounterGroup
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
name|counters
operator|.
name|TezCounter
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
name|counters
operator|.
name|TezCounters
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
name|dag
operator|.
name|api
operator|.
name|DAG
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
name|dag
operator|.
name|api
operator|.
name|Edge
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
name|dag
operator|.
name|api
operator|.
name|GroupInputEdge
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
name|dag
operator|.
name|api
operator|.
name|SessionNotRunning
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
name|dag
operator|.
name|api
operator|.
name|Vertex
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
name|dag
operator|.
name|api
operator|.
name|VertexGroup
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
name|dag
operator|.
name|api
operator|.
name|client
operator|.
name|DAGClient
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
name|dag
operator|.
name|api
operator|.
name|client
operator|.
name|StatusGetOpts
import|;
end_import

begin_comment
comment|/**  *  * TezTask handles the execution of TezWork. Currently it executes a graph of map and reduce work  * using the Tez APIs directly.  *  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"serial"
block|}
argument_list|)
specifier|public
class|class
name|TezTask
extends|extends
name|Task
argument_list|<
name|TezWork
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|TezTask
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|PerfLogger
name|perfLogger
init|=
name|PerfLogger
operator|.
name|getPerfLogger
argument_list|()
decl_stmt|;
specifier|private
name|TezCounters
name|counters
decl_stmt|;
specifier|private
specifier|final
name|DagUtils
name|utils
decl_stmt|;
specifier|public
name|TezTask
parameter_list|()
block|{
name|this
argument_list|(
name|DagUtils
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TezTask
parameter_list|(
name|DagUtils
name|utils
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|utils
operator|=
name|utils
expr_stmt|;
block|}
specifier|public
name|TezCounters
name|getTezCounters
parameter_list|()
block|{
return|return
name|counters
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|int
name|rc
init|=
literal|1
decl_stmt|;
name|boolean
name|cleanContext
init|=
literal|false
decl_stmt|;
name|Context
name|ctx
init|=
literal|null
decl_stmt|;
name|DAGClient
name|client
init|=
literal|null
decl_stmt|;
name|TezSessionState
name|session
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Get or create Context object. If we create it we have to clean it later as well.
name|ctx
operator|=
name|driverContext
operator|.
name|getCtx
argument_list|()
expr_stmt|;
if|if
condition|(
name|ctx
operator|==
literal|null
condition|)
block|{
name|ctx
operator|=
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|cleanContext
operator|=
literal|true
expr_stmt|;
block|}
comment|// Need to remove this static hack. But this is the way currently to get a session.
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|session
operator|=
name|ss
operator|.
name|getTezSession
argument_list|()
expr_stmt|;
name|session
operator|=
name|TezSessionPoolManager
operator|.
name|getInstance
argument_list|()
operator|.
name|getSession
argument_list|(
name|session
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|ss
operator|.
name|setTezSession
argument_list|(
name|session
argument_list|)
expr_stmt|;
comment|// jobConf will hold all the configuration for hadoop, tez, and hive
name|JobConf
name|jobConf
init|=
name|utils
operator|.
name|createConfiguration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Get all user jars from work (e.g. input format stuff).
name|String
index|[]
name|inputOutputJars
init|=
name|work
operator|.
name|configureJobConfAndExtractJars
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
comment|// we will localize all the files (jars, plans, hashtables) to the
comment|// scratch dir. let's create this and tmp first.
name|Path
name|scratchDir
init|=
name|ctx
operator|.
name|getMRScratchDir
argument_list|()
decl_stmt|;
comment|// create the tez tmp dir
name|scratchDir
operator|=
name|utils
operator|.
name|createTezDir
argument_list|(
name|scratchDir
argument_list|,
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|session
operator|.
name|isOpen
argument_list|()
condition|)
block|{
comment|// can happen if the user sets the tez flag after the session was
comment|// established
name|LOG
operator|.
name|info
argument_list|(
literal|"Tez session hasn't been created yet. Opening session"
argument_list|)
expr_stmt|;
name|session
operator|.
name|open
argument_list|(
name|conf
argument_list|,
name|inputOutputJars
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|session
operator|.
name|refreshLocalResourcesFromConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|LocalResource
argument_list|>
name|additionalLr
init|=
name|session
operator|.
name|getLocalizedResources
argument_list|()
decl_stmt|;
comment|// log which resources we're adding (apart from the hive exec)
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
if|if
condition|(
name|additionalLr
operator|==
literal|null
operator|||
name|additionalLr
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No local resources to process (other than hive-exec)"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|LocalResource
name|lr
range|:
name|additionalLr
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding local resource: "
operator|+
name|lr
operator|.
name|getResource
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// unless already installed on all the cluster nodes, we'll have to
comment|// localize hive-exec.jar as well.
name|LocalResource
name|appJarLr
init|=
name|session
operator|.
name|getAppJarLr
argument_list|()
decl_stmt|;
comment|// next we translate the TezWork to a Tez DAG
name|DAG
name|dag
init|=
name|build
argument_list|(
name|jobConf
argument_list|,
name|work
argument_list|,
name|scratchDir
argument_list|,
name|appJarLr
argument_list|,
name|additionalLr
argument_list|,
name|ctx
argument_list|)
decl_stmt|;
comment|// submit will send the job to the cluster and start executing
name|client
operator|=
name|submit
argument_list|(
name|jobConf
argument_list|,
name|dag
argument_list|,
name|scratchDir
argument_list|,
name|appJarLr
argument_list|,
name|session
argument_list|,
name|additionalLr
argument_list|)
expr_stmt|;
comment|// finally monitor will print progress until the job is done
name|TezJobMonitor
name|monitor
init|=
operator|new
name|TezJobMonitor
argument_list|()
decl_stmt|;
name|rc
operator|=
name|monitor
operator|.
name|monitorExecution
argument_list|(
name|client
argument_list|,
name|ctx
operator|.
name|getHiveTxnManager
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// fetch the counters
name|Set
argument_list|<
name|StatusGetOpts
argument_list|>
name|statusGetOpts
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|StatusGetOpts
operator|.
name|GET_COUNTERS
argument_list|)
decl_stmt|;
name|counters
operator|=
name|client
operator|.
name|getDAGStatus
argument_list|(
name|statusGetOpts
argument_list|)
operator|.
name|getDAGCounters
argument_list|()
expr_stmt|;
name|TezSessionPoolManager
operator|.
name|getInstance
argument_list|()
operator|.
name|returnSession
argument_list|(
name|session
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
for|for
control|(
name|CounterGroup
name|group
range|:
name|counters
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|group
operator|.
name|getDisplayName
argument_list|()
operator|+
literal|":"
argument_list|)
expr_stmt|;
for|for
control|(
name|TezCounter
name|counter
range|:
name|group
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"   "
operator|+
name|counter
operator|.
name|getDisplayName
argument_list|()
operator|+
literal|": "
operator|+
name|counter
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to execute tez graph."
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// rc will be 1 at this point indicating failure.
block|}
finally|finally
block|{
name|Utilities
operator|.
name|clearWork
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|cleanContext
condition|)
block|{
try|try
block|{
name|ctx
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|/*best effort*/
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to clean up after tez job"
argument_list|)
expr_stmt|;
block|}
block|}
comment|// need to either move tmp files or remove them
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
comment|// rc will only be overwritten if close errors out
name|rc
operator|=
name|close
argument_list|(
name|work
argument_list|,
name|rc
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|rc
return|;
block|}
name|DAG
name|build
parameter_list|(
name|JobConf
name|conf
parameter_list|,
name|TezWork
name|work
parameter_list|,
name|Path
name|scratchDir
parameter_list|,
name|LocalResource
name|appJarLr
parameter_list|,
name|List
argument_list|<
name|LocalResource
argument_list|>
name|additionalLr
parameter_list|,
name|Context
name|ctx
parameter_list|)
throws|throws
name|Exception
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_BUILD_DAG
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|Vertex
argument_list|>
name|workToVertex
init|=
operator|new
name|HashMap
argument_list|<
name|BaseWork
argument_list|,
name|Vertex
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|JobConf
argument_list|>
name|workToConf
init|=
operator|new
name|HashMap
argument_list|<
name|BaseWork
argument_list|,
name|JobConf
argument_list|>
argument_list|()
decl_stmt|;
comment|// getAllWork returns a topologically sorted list, which we use to make
comment|// sure that vertices are created before they are used in edges.
name|List
argument_list|<
name|BaseWork
argument_list|>
name|ws
init|=
name|work
operator|.
name|getAllWork
argument_list|()
decl_stmt|;
name|Collections
operator|.
name|reverse
argument_list|(
name|ws
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|scratchDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// the name of the dag is what is displayed in the AM/Job UI
name|DAG
name|dag
init|=
operator|new
name|DAG
argument_list|(
name|work
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|ws
control|)
block|{
name|boolean
name|isFinal
init|=
name|work
operator|.
name|getLeaves
argument_list|()
operator|.
name|contains
argument_list|(
name|w
argument_list|)
decl_stmt|;
comment|// translate work to vertex
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_CREATE_VERTEX
operator|+
name|w
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|w
operator|instanceof
name|UnionWork
condition|)
block|{
comment|// Special case for unions. These items translate to VertexGroups
name|List
argument_list|<
name|BaseWork
argument_list|>
name|unionWorkItems
init|=
operator|new
name|LinkedList
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|BaseWork
argument_list|>
name|children
init|=
operator|new
name|LinkedList
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
decl_stmt|;
comment|// split the children into vertices that make up the union and vertices that are
comment|// proper children of the union
for|for
control|(
name|BaseWork
name|v
range|:
name|work
operator|.
name|getChildren
argument_list|(
name|w
argument_list|)
control|)
block|{
name|EdgeType
name|type
init|=
name|work
operator|.
name|getEdgeProperty
argument_list|(
name|w
argument_list|,
name|v
argument_list|)
operator|.
name|getEdgeType
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|==
name|EdgeType
operator|.
name|CONTAINS
condition|)
block|{
name|unionWorkItems
operator|.
name|add
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|children
operator|.
name|add
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
comment|// create VertexGroup
name|Vertex
index|[]
name|vertexArray
init|=
operator|new
name|Vertex
index|[
name|unionWorkItems
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|BaseWork
name|v
range|:
name|unionWorkItems
control|)
block|{
name|vertexArray
index|[
name|i
operator|++
index|]
operator|=
name|workToVertex
operator|.
name|get
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
name|VertexGroup
name|group
init|=
name|dag
operator|.
name|createVertexGroup
argument_list|(
name|w
operator|.
name|getName
argument_list|()
argument_list|,
name|vertexArray
argument_list|)
decl_stmt|;
comment|// For a vertex group, all Outputs use the same Key-class, Val-class and partitioner.
comment|// Pick any one source vertex to figure out the Edge configuration.
name|JobConf
name|parentConf
init|=
name|workToConf
operator|.
name|get
argument_list|(
name|unionWorkItems
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
comment|// now hook up the children
for|for
control|(
name|BaseWork
name|v
range|:
name|children
control|)
block|{
comment|// finally we can create the grouped edge
name|GroupInputEdge
name|e
init|=
name|utils
operator|.
name|createEdge
argument_list|(
name|group
argument_list|,
name|parentConf
argument_list|,
name|workToVertex
operator|.
name|get
argument_list|(
name|v
argument_list|)
argument_list|,
name|work
operator|.
name|getEdgeProperty
argument_list|(
name|w
argument_list|,
name|v
argument_list|)
argument_list|)
decl_stmt|;
name|dag
operator|.
name|addEdge
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Regular vertices
name|JobConf
name|wxConf
init|=
name|utils
operator|.
name|initializeVertexConf
argument_list|(
name|conf
argument_list|,
name|w
argument_list|)
decl_stmt|;
name|Vertex
name|wx
init|=
name|utils
operator|.
name|createVertex
argument_list|(
name|wxConf
argument_list|,
name|w
argument_list|,
name|scratchDir
argument_list|,
name|appJarLr
argument_list|,
name|additionalLr
argument_list|,
name|fs
argument_list|,
name|ctx
argument_list|,
operator|!
name|isFinal
argument_list|,
name|work
argument_list|)
decl_stmt|;
name|dag
operator|.
name|addVertex
argument_list|(
name|wx
argument_list|)
expr_stmt|;
name|utils
operator|.
name|addCredentials
argument_list|(
name|w
argument_list|,
name|dag
argument_list|)
expr_stmt|;
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_CREATE_VERTEX
operator|+
name|w
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|workToVertex
operator|.
name|put
argument_list|(
name|w
argument_list|,
name|wx
argument_list|)
expr_stmt|;
name|workToConf
operator|.
name|put
argument_list|(
name|w
argument_list|,
name|wxConf
argument_list|)
expr_stmt|;
comment|// add all dependencies (i.e.: edges) to the graph
for|for
control|(
name|BaseWork
name|v
range|:
name|work
operator|.
name|getChildren
argument_list|(
name|w
argument_list|)
control|)
block|{
assert|assert
name|workToVertex
operator|.
name|containsKey
argument_list|(
name|v
argument_list|)
assert|;
name|Edge
name|e
init|=
literal|null
decl_stmt|;
name|TezEdgeProperty
name|edgeProp
init|=
name|work
operator|.
name|getEdgeProperty
argument_list|(
name|w
argument_list|,
name|v
argument_list|)
decl_stmt|;
name|e
operator|=
name|utils
operator|.
name|createEdge
argument_list|(
name|wxConf
argument_list|,
name|wx
argument_list|,
name|workToVertex
operator|.
name|get
argument_list|(
name|v
argument_list|)
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
name|dag
operator|.
name|addEdge
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_BUILD_DAG
argument_list|)
expr_stmt|;
return|return
name|dag
return|;
block|}
name|DAGClient
name|submit
parameter_list|(
name|JobConf
name|conf
parameter_list|,
name|DAG
name|dag
parameter_list|,
name|Path
name|scratchDir
parameter_list|,
name|LocalResource
name|appJarLr
parameter_list|,
name|TezSessionState
name|sessionState
parameter_list|,
name|List
argument_list|<
name|LocalResource
argument_list|>
name|additionalLr
parameter_list|)
throws|throws
name|Exception
block|{
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_SUBMIT_DAG
argument_list|)
expr_stmt|;
name|DAGClient
name|dagClient
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|LocalResource
argument_list|>
name|resourceMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|LocalResource
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|additionalLr
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|LocalResource
name|lr
range|:
name|additionalLr
control|)
block|{
name|resourceMap
operator|.
name|put
argument_list|(
name|utils
operator|.
name|getBaseName
argument_list|(
name|lr
argument_list|)
argument_list|,
name|lr
argument_list|)
expr_stmt|;
block|}
block|}
try|try
block|{
comment|// ready to start execution on the cluster
name|sessionState
operator|.
name|getSession
argument_list|()
operator|.
name|addAppMasterLocalFiles
argument_list|(
name|resourceMap
argument_list|)
expr_stmt|;
name|dagClient
operator|=
name|sessionState
operator|.
name|getSession
argument_list|()
operator|.
name|submitDAG
argument_list|(
name|dag
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SessionNotRunning
name|nr
parameter_list|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Tez session was closed. Reopening..."
argument_list|)
expr_stmt|;
comment|// close the old one, but keep the tmp files around
name|TezSessionPoolManager
operator|.
name|getInstance
argument_list|()
operator|.
name|closeAndOpen
argument_list|(
name|sessionState
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Session re-established."
argument_list|)
expr_stmt|;
name|dagClient
operator|=
name|sessionState
operator|.
name|getSession
argument_list|()
operator|.
name|submitDAG
argument_list|(
name|dag
argument_list|)
expr_stmt|;
block|}
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|TEZ_SUBMIT_DAG
argument_list|)
expr_stmt|;
return|return
name|dagClient
return|;
block|}
comment|/*    * close will move the temp files into the right place for the fetch    * task. If the job has failed it will clean up the files.    */
name|int
name|close
parameter_list|(
name|TezWork
name|work
parameter_list|,
name|int
name|rc
parameter_list|)
block|{
try|try
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|ws
init|=
name|work
operator|.
name|getAllWork
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|ws
control|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|w
operator|.
name|getAllOperators
argument_list|()
control|)
block|{
name|op
operator|.
name|jobClose
argument_list|(
name|conf
argument_list|,
name|rc
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// jobClose needs to execute successfully otherwise fail task
if|if
condition|(
name|rc
operator|==
literal|0
condition|)
block|{
name|rc
operator|=
literal|3
expr_stmt|;
name|String
name|mesg
init|=
literal|"Job Commit failed with exception '"
operator|+
name|Utilities
operator|.
name|getNameMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"'"
decl_stmt|;
name|console
operator|.
name|printError
argument_list|(
name|mesg
argument_list|,
literal|"\n"
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|rc
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isMapRedTask
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|MAPRED
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"TEZ"
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|MapWork
argument_list|>
name|getMapWork
parameter_list|()
block|{
name|List
argument_list|<
name|MapWork
argument_list|>
name|result
init|=
operator|new
name|LinkedList
argument_list|<
name|MapWork
argument_list|>
argument_list|()
decl_stmt|;
name|TezWork
name|work
init|=
name|getWork
argument_list|()
decl_stmt|;
comment|// framework expects MapWork instances that have no physical parents (i.e.: union parent is
comment|// fine, broadcast parent isn't)
for|for
control|(
name|BaseWork
name|w
range|:
name|work
operator|.
name|getAllWorkUnsorted
argument_list|()
control|)
block|{
if|if
condition|(
name|w
operator|instanceof
name|MapWork
condition|)
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|parents
init|=
name|work
operator|.
name|getParents
argument_list|(
name|w
argument_list|)
decl_stmt|;
name|boolean
name|candidate
init|=
literal|true
decl_stmt|;
for|for
control|(
name|BaseWork
name|parent
range|:
name|parents
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|parent
operator|instanceof
name|UnionWork
operator|)
condition|)
block|{
name|candidate
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
name|candidate
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|(
name|MapWork
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|getReducer
parameter_list|(
name|MapWork
name|mapWork
parameter_list|)
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|children
init|=
name|getWork
argument_list|()
operator|.
name|getChildren
argument_list|(
name|mapWork
argument_list|)
decl_stmt|;
if|if
condition|(
name|children
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|ReduceWork
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|(
operator|(
name|ReduceWork
operator|)
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|getReducer
argument_list|()
return|;
block|}
block|}
end_class

end_unit

