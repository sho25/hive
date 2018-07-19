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
name|ql
operator|.
name|exec
operator|.
name|repl
operator|.
name|incremental
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
name|FileStatus
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
name|metastore
operator|.
name|api
operator|.
name|Database
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
name|TaskFactory
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
name|repl
operator|.
name|ReplLoadWork
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
name|repl
operator|.
name|ReplStateLogWork
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
name|repl
operator|.
name|util
operator|.
name|AddDependencyToLeaves
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
name|repl
operator|.
name|util
operator|.
name|TaskTracker
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
name|util
operator|.
name|DAGTraversal
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|metadata
operator|.
name|Hive
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
name|metadata
operator|.
name|HiveException
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
name|metadata
operator|.
name|Table
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
name|parse
operator|.
name|ReplicationSpec
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
name|parse
operator|.
name|SemanticException
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
name|parse
operator|.
name|repl
operator|.
name|DumpType
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
name|parse
operator|.
name|repl
operator|.
name|ReplLogger
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|DumpMetaData
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|UpdatedMetaDataTracker
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|log
operator|.
name|IncrementalLoadLogger
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|MessageHandler
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
name|AlterDatabaseDesc
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
name|AlterTableDesc
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
name|DDLWork
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
name|DependencyCollectionWork
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
name|stats
operator|.
name|StatsUtils
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
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|HashMap
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
name|HashSet
import|;
end_import

begin_comment
comment|/**  * IncrementalLoad  * Iterate through the dump directory and create tasks to load the events.  */
end_comment

begin_class
specifier|public
class|class
name|IncrementalLoadTasksBuilder
block|{
specifier|private
specifier|final
name|String
name|dbName
decl_stmt|,
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|IncrementalLoadEventsIterator
name|iterator
decl_stmt|;
specifier|private
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
decl_stmt|;
specifier|private
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
decl_stmt|;
specifier|private
name|Logger
name|log
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|final
name|ReplLogger
name|replLogger
decl_stmt|;
specifier|private
specifier|static
name|long
name|numIteration
decl_stmt|;
specifier|public
name|IncrementalLoadTasksBuilder
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|loadPath
parameter_list|,
name|IncrementalLoadEventsIterator
name|iterator
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|iterator
operator|=
name|iterator
expr_stmt|;
name|inputs
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|outputs
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|log
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|replLogger
operator|=
operator|new
name|IncrementalLoadLogger
argument_list|(
name|dbName
argument_list|,
name|loadPath
argument_list|,
name|iterator
operator|.
name|getNumEvents
argument_list|()
argument_list|)
expr_stmt|;
name|numIteration
operator|=
literal|0
expr_stmt|;
name|replLogger
operator|.
name|startLog
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|build
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|,
name|Hive
name|hive
parameter_list|,
name|Logger
name|log
parameter_list|,
name|ReplLoadWork
name|loadWork
parameter_list|)
throws|throws
name|Exception
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|evTaskRoot
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DependencyCollectionWork
argument_list|()
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|taskChainTail
init|=
name|evTaskRoot
decl_stmt|;
name|Long
name|lastReplayedEvent
init|=
literal|null
decl_stmt|;
name|this
operator|.
name|log
operator|=
name|log
expr_stmt|;
name|numIteration
operator|++
expr_stmt|;
name|this
operator|.
name|log
operator|.
name|debug
argument_list|(
literal|"Iteration num "
operator|+
name|numIteration
argument_list|)
expr_stmt|;
name|TaskTracker
name|tracker
init|=
operator|new
name|TaskTracker
argument_list|(
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPL_APPROX_MAX_LOAD_TASKS
argument_list|)
argument_list|)
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
operator|&&
name|tracker
operator|.
name|canAddMoreTasks
argument_list|()
condition|)
block|{
name|FileStatus
name|dir
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|location
init|=
name|dir
operator|.
name|getPath
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|DumpMetaData
name|eventDmd
init|=
operator|new
name|DumpMetaData
argument_list|(
operator|new
name|Path
argument_list|(
name|location
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|shouldReplayEvent
argument_list|(
name|dir
argument_list|,
name|eventDmd
operator|.
name|getDumpType
argument_list|()
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|)
condition|)
block|{
name|this
operator|.
name|log
operator|.
name|debug
argument_list|(
literal|"Skipping event {} from {} for table {}.{} maxTasks: {}"
argument_list|,
name|eventDmd
operator|.
name|getDumpType
argument_list|()
argument_list|,
name|dir
operator|.
name|getPath
argument_list|()
operator|.
name|toUri
argument_list|()
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|tracker
operator|.
name|numberOfTasks
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|this
operator|.
name|log
operator|.
name|debug
argument_list|(
literal|"Loading event {} from {} for table {}.{} maxTasks: {}"
argument_list|,
name|eventDmd
operator|.
name|getDumpType
argument_list|()
argument_list|,
name|dir
operator|.
name|getPath
argument_list|()
operator|.
name|toUri
argument_list|()
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|tracker
operator|.
name|numberOfTasks
argument_list|()
argument_list|)
expr_stmt|;
comment|// event loads will behave similar to table loads, with one crucial difference
comment|// precursor order is strict, and each event must be processed after the previous one.
comment|// The way we handle this strict order is as follows:
comment|// First, we start with a taskChainTail which is a dummy noop task (a DependecyCollectionTask)
comment|// at the head of our event chain. For each event we process, we tell analyzeTableLoad to
comment|// create tasks that use the taskChainTail as a dependency. Then, we collect all those tasks
comment|// and introduce a new barrier task(also a DependencyCollectionTask) which depends on all
comment|// these tasks. Then, this barrier task becomes our new taskChainTail. Thus, we get a set of
comment|// tasks as follows:
comment|//
comment|//                 --->ev1.task1--                          --->ev2.task1--
comment|//                /               \                        /               \
comment|//  evTaskRoot-->*---->ev1.task2---*--> ev1.barrierTask-->*---->ev2.task2---*->evTaskChainTail
comment|//                \               /
comment|//                 --->ev1.task3--
comment|//
comment|// Once this entire chain is generated, we add evTaskRoot to rootTasks, so as to execute the
comment|// entire chain
name|MessageHandler
operator|.
name|Context
name|context
init|=
operator|new
name|MessageHandler
operator|.
name|Context
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|location
argument_list|,
name|taskChainTail
argument_list|,
name|eventDmd
argument_list|,
name|conf
argument_list|,
name|hive
argument_list|,
name|driverContext
operator|.
name|getCtx
argument_list|()
argument_list|,
name|this
operator|.
name|log
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|evTasks
init|=
name|analyzeEventLoad
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|evTasks
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|evTasks
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|ReplStateLogWork
name|replStateLogWork
init|=
operator|new
name|ReplStateLogWork
argument_list|(
name|replLogger
argument_list|,
name|dir
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|eventDmd
operator|.
name|getDumpType
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|barrierTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|replStateLogWork
argument_list|)
decl_stmt|;
name|AddDependencyToLeaves
name|function
init|=
operator|new
name|AddDependencyToLeaves
argument_list|(
name|barrierTask
argument_list|)
decl_stmt|;
name|DAGTraversal
operator|.
name|traverse
argument_list|(
name|evTasks
argument_list|,
name|function
argument_list|)
expr_stmt|;
name|this
operator|.
name|log
operator|.
name|debug
argument_list|(
literal|"Updated taskChainTail from {}:{} to {}:{}"
argument_list|,
name|taskChainTail
operator|.
name|getClass
argument_list|()
argument_list|,
name|taskChainTail
operator|.
name|getId
argument_list|()
argument_list|,
name|barrierTask
operator|.
name|getClass
argument_list|()
argument_list|,
name|barrierTask
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|addTaskList
argument_list|(
name|taskChainTail
operator|.
name|getChildTasks
argument_list|()
argument_list|)
expr_stmt|;
name|taskChainTail
operator|=
name|barrierTask
expr_stmt|;
block|}
name|lastReplayedEvent
operator|=
name|eventDmd
operator|.
name|getEventTo
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
comment|// add load task to start the next iteration
name|taskChainTail
operator|.
name|addDependentTask
argument_list|(
name|TaskFactory
operator|.
name|get
argument_list|(
name|loadWork
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dbProps
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|dbProps
operator|.
name|put
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|CURR_STATE_ID
operator|.
name|toString
argument_list|()
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|lastReplayedEvent
argument_list|)
argument_list|)
expr_stmt|;
name|ReplStateLogWork
name|replStateLogWork
init|=
operator|new
name|ReplStateLogWork
argument_list|(
name|replLogger
argument_list|,
name|dbProps
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|barrierTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|replStateLogWork
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|taskChainTail
operator|.
name|addDependentTask
argument_list|(
name|barrierTask
argument_list|)
expr_stmt|;
name|this
operator|.
name|log
operator|.
name|debug
argument_list|(
literal|"Added {}:{} as a precursor of barrier task {}:{}"
argument_list|,
name|taskChainTail
operator|.
name|getClass
argument_list|()
argument_list|,
name|taskChainTail
operator|.
name|getId
argument_list|()
argument_list|,
name|barrierTask
operator|.
name|getClass
argument_list|()
argument_list|,
name|barrierTask
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|evTaskRoot
return|;
block|}
specifier|private
name|boolean
name|isEventNotReplayed
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|,
name|FileStatus
name|dir
parameter_list|,
name|DumpType
name|dumpType
parameter_list|)
block|{
if|if
condition|(
name|params
operator|!=
literal|null
operator|&&
operator|(
name|params
operator|.
name|containsKey
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|CURR_STATE_ID
operator|.
name|toString
argument_list|()
argument_list|)
operator|)
condition|)
block|{
name|String
name|replLastId
init|=
name|params
operator|.
name|get
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|CURR_STATE_ID
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|Long
operator|.
name|parseLong
argument_list|(
name|replLastId
argument_list|)
operator|>=
name|Long
operator|.
name|parseLong
argument_list|(
name|dir
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|log
operator|.
name|debug
argument_list|(
literal|"Event "
operator|+
name|dumpType
operator|+
literal|" with replId "
operator|+
name|Long
operator|.
name|parseLong
argument_list|(
name|dir
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|+
literal|" is already replayed. LastReplId - "
operator|+
name|Long
operator|.
name|parseLong
argument_list|(
name|replLastId
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|boolean
name|shouldReplayEvent
parameter_list|(
name|FileStatus
name|dir
parameter_list|,
name|DumpType
name|dumpType
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
comment|// if database itself is null then we can not filter out anything.
if|if
condition|(
name|dbName
operator|==
literal|null
operator|||
name|dbName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
operator|(
name|tableName
operator|==
literal|null
operator|)
operator|||
operator|(
name|tableName
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|Database
name|database
decl_stmt|;
try|try
block|{
name|database
operator|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
return|return
name|database
operator|==
literal|null
condition|?
literal|true
else|:
name|isEventNotReplayed
argument_list|(
name|database
operator|.
name|getParameters
argument_list|()
argument_list|,
name|dir
argument_list|,
name|dumpType
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
comment|//may be the db is getting created in this load
name|log
operator|.
name|debug
argument_list|(
literal|"failed to get the database "
operator|+
name|dbName
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
else|else
block|{
name|Table
name|tbl
decl_stmt|;
try|try
block|{
name|tbl
operator|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
return|return
name|isEventNotReplayed
argument_list|(
name|tbl
operator|.
name|getParameters
argument_list|()
argument_list|,
name|dir
argument_list|,
name|dumpType
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
comment|// may be the table is getting created in this load
name|log
operator|.
name|debug
argument_list|(
literal|"failed to get the table "
operator|+
name|dbName
operator|+
literal|"."
operator|+
name|tableName
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
specifier|private
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|analyzeEventLoad
parameter_list|(
name|MessageHandler
operator|.
name|Context
name|context
parameter_list|)
throws|throws
name|SemanticException
block|{
name|MessageHandler
name|messageHandler
init|=
name|context
operator|.
name|dmd
operator|.
name|getDumpType
argument_list|()
operator|.
name|handler
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|tasks
init|=
name|messageHandler
operator|.
name|handle
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|precursor
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|t
range|:
name|tasks
control|)
block|{
name|context
operator|.
name|precursor
operator|.
name|addDependentTask
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|log
operator|.
name|debug
argument_list|(
literal|"Added {}:{} as a precursor of {}:{}"
argument_list|,
name|context
operator|.
name|precursor
operator|.
name|getClass
argument_list|()
argument_list|,
name|context
operator|.
name|precursor
operator|.
name|getId
argument_list|()
argument_list|,
name|t
operator|.
name|getClass
argument_list|()
argument_list|,
name|t
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|inputs
operator|.
name|addAll
argument_list|(
name|messageHandler
operator|.
name|readEntities
argument_list|()
argument_list|)
expr_stmt|;
name|outputs
operator|.
name|addAll
argument_list|(
name|messageHandler
operator|.
name|writeEntities
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|addUpdateReplStateTasks
argument_list|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|context
operator|.
name|tableName
argument_list|)
argument_list|,
name|messageHandler
operator|.
name|getUpdatedMetadata
argument_list|()
argument_list|,
name|tasks
argument_list|)
return|;
block|}
specifier|private
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tableUpdateReplStateTask
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|,
name|String
name|replState
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|preCursor
parameter_list|)
throws|throws
name|SemanticException
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapProp
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|mapProp
operator|.
name|put
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|CURR_STATE_ID
operator|.
name|toString
argument_list|()
argument_list|,
name|replState
argument_list|)
expr_stmt|;
name|AlterTableDesc
name|alterTblDesc
init|=
operator|new
name|AlterTableDesc
argument_list|(
name|AlterTableDesc
operator|.
name|AlterTableTypes
operator|.
name|ADDPROPS
argument_list|,
operator|new
name|ReplicationSpec
argument_list|(
name|replState
argument_list|,
name|replState
argument_list|)
argument_list|)
decl_stmt|;
name|alterTblDesc
operator|.
name|setProps
argument_list|(
name|mapProp
argument_list|)
expr_stmt|;
name|alterTblDesc
operator|.
name|setOldName
argument_list|(
name|StatsUtils
operator|.
name|getFullyQualifiedTableName
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|alterTblDesc
operator|.
name|setPartSpec
argument_list|(
operator|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|partSpec
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|updateReplIdTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|,
name|alterTblDesc
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
comment|// Link the update repl state task with dependency collection task
if|if
condition|(
name|preCursor
operator|!=
literal|null
condition|)
block|{
name|preCursor
operator|.
name|addDependentTask
argument_list|(
name|updateReplIdTask
argument_list|)
expr_stmt|;
name|log
operator|.
name|debug
argument_list|(
literal|"Added {}:{} as a precursor of {}:{}"
argument_list|,
name|preCursor
operator|.
name|getClass
argument_list|()
argument_list|,
name|preCursor
operator|.
name|getId
argument_list|()
argument_list|,
name|updateReplIdTask
operator|.
name|getClass
argument_list|()
argument_list|,
name|updateReplIdTask
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|updateReplIdTask
return|;
block|}
specifier|private
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|dbUpdateReplStateTask
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|replState
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|preCursor
parameter_list|)
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapProp
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|mapProp
operator|.
name|put
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|CURR_STATE_ID
operator|.
name|toString
argument_list|()
argument_list|,
name|replState
argument_list|)
expr_stmt|;
name|AlterDatabaseDesc
name|alterDbDesc
init|=
operator|new
name|AlterDatabaseDesc
argument_list|(
name|dbName
argument_list|,
name|mapProp
argument_list|,
operator|new
name|ReplicationSpec
argument_list|(
name|replState
argument_list|,
name|replState
argument_list|)
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|updateReplIdTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|,
name|alterDbDesc
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
comment|// Link the update repl state task with dependency collection task
if|if
condition|(
name|preCursor
operator|!=
literal|null
condition|)
block|{
name|preCursor
operator|.
name|addDependentTask
argument_list|(
name|updateReplIdTask
argument_list|)
expr_stmt|;
name|log
operator|.
name|debug
argument_list|(
literal|"Added {}:{} as a precursor of {}:{}"
argument_list|,
name|preCursor
operator|.
name|getClass
argument_list|()
argument_list|,
name|preCursor
operator|.
name|getId
argument_list|()
argument_list|,
name|updateReplIdTask
operator|.
name|getClass
argument_list|()
argument_list|,
name|updateReplIdTask
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|updateReplIdTask
return|;
block|}
specifier|private
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|addUpdateReplStateTasks
parameter_list|(
name|boolean
name|isDatabaseLoad
parameter_list|,
name|UpdatedMetaDataTracker
name|updatedMetaDataTracker
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|importTasks
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// If no import tasks generated by the event then no need to update the repl state to any object.
if|if
condition|(
name|importTasks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|log
operator|.
name|debug
argument_list|(
literal|"No objects need update of repl state: 0 import tasks"
argument_list|)
expr_stmt|;
return|return
name|importTasks
return|;
block|}
comment|// Create a barrier task for dependency collection of import tasks
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|barrierTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DependencyCollectionWork
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|tasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|updateReplIdTask
decl_stmt|;
for|for
control|(
name|UpdatedMetaDataTracker
operator|.
name|UpdateMetaData
name|updateMetaData
range|:
name|updatedMetaDataTracker
operator|.
name|getUpdateMetaDataList
argument_list|()
control|)
block|{
name|String
name|replState
init|=
name|updateMetaData
operator|.
name|getReplState
argument_list|()
decl_stmt|;
name|String
name|dbName
init|=
name|updateMetaData
operator|.
name|getDbName
argument_list|()
decl_stmt|;
name|String
name|tableName
init|=
name|updateMetaData
operator|.
name|getTableName
argument_list|()
decl_stmt|;
comment|// If any partition is updated, then update repl state in partition object
for|for
control|(
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
range|:
name|updateMetaData
operator|.
name|getPartitionsList
argument_list|()
control|)
block|{
name|updateReplIdTask
operator|=
name|tableUpdateReplStateTask
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partSpec
argument_list|,
name|replState
argument_list|,
name|barrierTask
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|add
argument_list|(
name|updateReplIdTask
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tableName
operator|!=
literal|null
condition|)
block|{
comment|// If any table/partition is updated, then update repl state in table object
name|updateReplIdTask
operator|=
name|tableUpdateReplStateTask
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
name|replState
argument_list|,
name|barrierTask
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|add
argument_list|(
name|updateReplIdTask
argument_list|)
expr_stmt|;
block|}
comment|// For table level load, need not update replication state for the database
if|if
condition|(
name|isDatabaseLoad
condition|)
block|{
comment|// If any table/partition is updated, then update repl state in db object
name|updateReplIdTask
operator|=
name|dbUpdateReplStateTask
argument_list|(
name|dbName
argument_list|,
name|replState
argument_list|,
name|barrierTask
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|add
argument_list|(
name|updateReplIdTask
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|tasks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|log
operator|.
name|debug
argument_list|(
literal|"No objects need update of repl state: 0 update tracker tasks"
argument_list|)
expr_stmt|;
return|return
name|importTasks
return|;
block|}
comment|// Link import tasks to the barrier task which will in-turn linked with repl state update tasks
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|t
range|:
name|importTasks
control|)
block|{
name|t
operator|.
name|addDependentTask
argument_list|(
name|barrierTask
argument_list|)
expr_stmt|;
name|log
operator|.
name|debug
argument_list|(
literal|"Added {}:{} as a precursor of barrier task {}:{}"
argument_list|,
name|t
operator|.
name|getClass
argument_list|()
argument_list|,
name|t
operator|.
name|getId
argument_list|()
argument_list|,
name|barrierTask
operator|.
name|getClass
argument_list|()
argument_list|,
name|barrierTask
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// At least one task would have been added to update the repl state
return|return
name|tasks
return|;
block|}
specifier|public
specifier|static
name|long
name|getNumIteration
parameter_list|()
block|{
return|return
name|numIteration
return|;
block|}
block|}
end_class

end_unit

