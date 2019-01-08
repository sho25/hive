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
package|;
end_package

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
name|ErrorMsg
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
name|bootstrap
operator|.
name|events
operator|.
name|BootstrapEvent
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
name|bootstrap
operator|.
name|events
operator|.
name|ConstraintEvent
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
name|bootstrap
operator|.
name|events
operator|.
name|DatabaseEvent
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
name|bootstrap
operator|.
name|events
operator|.
name|FunctionEvent
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
name|bootstrap
operator|.
name|events
operator|.
name|PartitionEvent
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
name|bootstrap
operator|.
name|events
operator|.
name|TableEvent
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
name|bootstrap
operator|.
name|events
operator|.
name|filesystem
operator|.
name|BootstrapEventsIterator
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
name|bootstrap
operator|.
name|events
operator|.
name|filesystem
operator|.
name|ConstraintEventsIterator
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
name|bootstrap
operator|.
name|load
operator|.
name|LoadConstraint
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
name|bootstrap
operator|.
name|load
operator|.
name|LoadDatabase
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
name|bootstrap
operator|.
name|load
operator|.
name|LoadFunction
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
name|incremental
operator|.
name|IncrementalLoadTasksBuilder
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
name|repl
operator|.
name|bootstrap
operator|.
name|load
operator|.
name|table
operator|.
name|LoadPartitions
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
name|bootstrap
operator|.
name|load
operator|.
name|table
operator|.
name|LoadTable
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
name|bootstrap
operator|.
name|load
operator|.
name|table
operator|.
name|TableContext
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
name|bootstrap
operator|.
name|load
operator|.
name|util
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
name|plan
operator|.
name|api
operator|.
name|StageType
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
import|import static
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
name|bootstrap
operator|.
name|load
operator|.
name|LoadDatabase
operator|.
name|AlterDatabase
import|;
end_import

begin_class
specifier|public
class|class
name|ReplLoadTask
extends|extends
name|Task
argument_list|<
name|ReplLoadWork
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|final
specifier|static
name|int
name|ZERO_TASKS
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
operator|(
name|work
operator|.
name|isIncrementalLoad
argument_list|()
condition|?
literal|"REPL_INCREMENTAL_LOAD"
else|:
literal|"REPL_BOOTSTRAP_LOAD"
operator|)
return|;
block|}
comment|/**    * Provides the root Tasks created as a result of this loadTask run which will be executed    * by the driver. It does not track details across multiple runs of LoadTask.    */
specifier|private
specifier|static
class|class
name|Scope
block|{
name|boolean
name|database
init|=
literal|false
decl_stmt|,
name|table
init|=
literal|false
decl_stmt|,
name|partition
init|=
literal|false
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
name|rootTasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|rootTask
init|=
name|work
operator|.
name|getRootTask
argument_list|()
decl_stmt|;
if|if
condition|(
name|rootTask
operator|!=
literal|null
condition|)
block|{
name|rootTask
operator|.
name|setChildTasks
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|work
operator|.
name|setRootTask
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentTasks
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|work
operator|.
name|isIncrementalLoad
argument_list|()
condition|)
block|{
return|return
name|executeIncrementalLoad
argument_list|(
name|driverContext
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|executeBootStrapLoad
argument_list|(
name|driverContext
argument_list|)
return|;
block|}
block|}
specifier|private
name|int
name|executeBootStrapLoad
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
try|try
block|{
name|int
name|maxTasks
init|=
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
decl_stmt|;
name|Context
name|context
init|=
operator|new
name|Context
argument_list|(
name|work
operator|.
name|dumpDirectory
argument_list|,
name|conf
argument_list|,
name|getHive
argument_list|()
argument_list|,
name|work
operator|.
name|sessionStateLineageState
argument_list|,
name|driverContext
operator|.
name|getCtx
argument_list|()
argument_list|)
decl_stmt|;
name|TaskTracker
name|loadTaskTracker
init|=
operator|new
name|TaskTracker
argument_list|(
name|maxTasks
argument_list|)
decl_stmt|;
comment|/*           for now for simplicity we are doing just one directory ( one database ), come back to use           of multiple databases once we have the basic flow to chain creating of tasks in place for           a database ( directory )       */
name|BootstrapEventsIterator
name|iterator
init|=
name|work
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|ConstraintEventsIterator
name|constraintIterator
init|=
name|work
operator|.
name|constraintIterator
argument_list|()
decl_stmt|;
comment|/*       This is used to get hold of a reference during the current creation of tasks and is initialized       with "0" tasks such that it will be non consequential in any operations done with task tracker       compositions.        */
name|TaskTracker
name|dbTracker
init|=
operator|new
name|TaskTracker
argument_list|(
name|ZERO_TASKS
argument_list|)
decl_stmt|;
name|TaskTracker
name|tableTracker
init|=
operator|new
name|TaskTracker
argument_list|(
name|ZERO_TASKS
argument_list|)
decl_stmt|;
name|Scope
name|scope
init|=
operator|new
name|Scope
argument_list|()
decl_stmt|;
name|boolean
name|loadingConstraint
init|=
literal|false
decl_stmt|;
if|if
condition|(
operator|!
name|iterator
operator|.
name|hasNext
argument_list|()
operator|&&
name|constraintIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|loadingConstraint
operator|=
literal|true
expr_stmt|;
block|}
while|while
condition|(
operator|(
name|iterator
operator|.
name|hasNext
argument_list|()
operator|||
operator|(
name|loadingConstraint
operator|&&
name|constraintIterator
operator|.
name|hasNext
argument_list|()
operator|)
operator|)
operator|&&
name|loadTaskTracker
operator|.
name|canAddMoreTasks
argument_list|()
condition|)
block|{
name|BootstrapEvent
name|next
decl_stmt|;
if|if
condition|(
operator|!
name|loadingConstraint
condition|)
block|{
name|next
operator|=
name|iterator
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|next
operator|=
name|constraintIterator
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
switch|switch
condition|(
name|next
operator|.
name|eventType
argument_list|()
condition|)
block|{
case|case
name|Database
case|:
name|DatabaseEvent
name|dbEvent
init|=
operator|(
name|DatabaseEvent
operator|)
name|next
decl_stmt|;
name|dbTracker
operator|=
operator|new
name|LoadDatabase
argument_list|(
name|context
argument_list|,
name|dbEvent
argument_list|,
name|work
operator|.
name|dbNameToLoadIn
argument_list|,
name|loadTaskTracker
argument_list|)
operator|.
name|tasks
argument_list|()
expr_stmt|;
name|loadTaskTracker
operator|.
name|update
argument_list|(
name|dbTracker
argument_list|)
expr_stmt|;
if|if
condition|(
name|work
operator|.
name|hasDbState
argument_list|()
condition|)
block|{
name|loadTaskTracker
operator|.
name|update
argument_list|(
name|updateDatabaseLastReplID
argument_list|(
name|maxTasks
argument_list|,
name|context
argument_list|,
name|scope
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Scope might have set to database in some previous iteration of loop, so reset it to false if database
comment|// tracker has no tasks.
name|scope
operator|.
name|database
operator|=
literal|false
expr_stmt|;
block|}
name|work
operator|.
name|updateDbEventState
argument_list|(
name|dbEvent
operator|.
name|toState
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|dbTracker
operator|.
name|hasTasks
argument_list|()
condition|)
block|{
name|scope
operator|.
name|rootTasks
operator|.
name|addAll
argument_list|(
name|dbTracker
operator|.
name|tasks
argument_list|()
argument_list|)
expr_stmt|;
name|scope
operator|.
name|database
operator|=
literal|true
expr_stmt|;
block|}
name|dbTracker
operator|.
name|debugLog
argument_list|(
literal|"database"
argument_list|)
expr_stmt|;
break|break;
case|case
name|Table
case|:
block|{
comment|/*               Implicit assumption here is that database level is processed first before table level,               which will depend on the iterator used since it should provide the higher level directory               listing before providing the lower level listing. This is also required such that               the dbTracker /  tableTracker are setup correctly always.            */
name|TableContext
name|tableContext
init|=
operator|new
name|TableContext
argument_list|(
name|dbTracker
argument_list|,
name|work
operator|.
name|dbNameToLoadIn
argument_list|,
name|work
operator|.
name|tableNameToLoadIn
argument_list|)
decl_stmt|;
name|TableEvent
name|tableEvent
init|=
operator|(
name|TableEvent
operator|)
name|next
decl_stmt|;
name|LoadTable
name|loadTable
init|=
operator|new
name|LoadTable
argument_list|(
name|tableEvent
argument_list|,
name|context
argument_list|,
name|iterator
operator|.
name|replLogger
argument_list|()
argument_list|,
name|tableContext
argument_list|,
name|loadTaskTracker
argument_list|)
decl_stmt|;
name|tableTracker
operator|=
name|loadTable
operator|.
name|tasks
argument_list|()
expr_stmt|;
name|setUpDependencies
argument_list|(
name|dbTracker
argument_list|,
name|tableTracker
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|scope
operator|.
name|database
operator|&&
name|tableTracker
operator|.
name|hasTasks
argument_list|()
condition|)
block|{
name|scope
operator|.
name|rootTasks
operator|.
name|addAll
argument_list|(
name|tableTracker
operator|.
name|tasks
argument_list|()
argument_list|)
expr_stmt|;
name|scope
operator|.
name|table
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
comment|// Scope might have set to table in some previous iteration of loop, so reset it to false if table
comment|// tracker has no tasks.
name|scope
operator|.
name|table
operator|=
literal|false
expr_stmt|;
block|}
comment|/*             for table replication if we reach the max number of tasks then for the next run we will             try to reload the same table again, this is mainly for ease of understanding the code             as then we can avoid handling ==> loading partitions for the table given that             the creation of table lead to reaching max tasks vs,  loading next table since current             one does not have partitions.            */
comment|// for a table we explicitly try to load partitions as there is no separate partitions events.
name|LoadPartitions
name|loadPartitions
init|=
operator|new
name|LoadPartitions
argument_list|(
name|context
argument_list|,
name|iterator
operator|.
name|replLogger
argument_list|()
argument_list|,
name|loadTaskTracker
argument_list|,
name|tableEvent
argument_list|,
name|work
operator|.
name|dbNameToLoadIn
argument_list|,
name|tableContext
argument_list|)
decl_stmt|;
name|TaskTracker
name|partitionsTracker
init|=
name|loadPartitions
operator|.
name|tasks
argument_list|()
decl_stmt|;
name|partitionsPostProcessing
argument_list|(
name|iterator
argument_list|,
name|scope
argument_list|,
name|loadTaskTracker
argument_list|,
name|tableTracker
argument_list|,
name|partitionsTracker
argument_list|)
expr_stmt|;
name|tableTracker
operator|.
name|debugLog
argument_list|(
literal|"table"
argument_list|)
expr_stmt|;
name|partitionsTracker
operator|.
name|debugLog
argument_list|(
literal|"partitions for table"
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|Partition
case|:
block|{
comment|/*               This will happen only when loading tables and we reach the limit of number of tasks we can create;               hence we know here that the table should exist and there should be a lastPartitionName           */
name|PartitionEvent
name|event
init|=
operator|(
name|PartitionEvent
operator|)
name|next
decl_stmt|;
name|TableContext
name|tableContext
init|=
operator|new
name|TableContext
argument_list|(
name|dbTracker
argument_list|,
name|work
operator|.
name|dbNameToLoadIn
argument_list|,
name|work
operator|.
name|tableNameToLoadIn
argument_list|)
decl_stmt|;
name|LoadPartitions
name|loadPartitions
init|=
operator|new
name|LoadPartitions
argument_list|(
name|context
argument_list|,
name|iterator
operator|.
name|replLogger
argument_list|()
argument_list|,
name|tableContext
argument_list|,
name|loadTaskTracker
argument_list|,
name|event
operator|.
name|asTableEvent
argument_list|()
argument_list|,
name|work
operator|.
name|dbNameToLoadIn
argument_list|,
name|event
operator|.
name|lastPartitionReplicated
argument_list|()
argument_list|)
decl_stmt|;
comment|/*                the tableTracker here should be a new instance and not an existing one as this can                only happen when we break in between loading partitions.            */
name|TaskTracker
name|partitionsTracker
init|=
name|loadPartitions
operator|.
name|tasks
argument_list|()
decl_stmt|;
name|partitionsPostProcessing
argument_list|(
name|iterator
argument_list|,
name|scope
argument_list|,
name|loadTaskTracker
argument_list|,
name|tableTracker
argument_list|,
name|partitionsTracker
argument_list|)
expr_stmt|;
name|partitionsTracker
operator|.
name|debugLog
argument_list|(
literal|"partitions"
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|Function
case|:
block|{
name|LoadFunction
name|loadFunction
init|=
operator|new
name|LoadFunction
argument_list|(
name|context
argument_list|,
name|iterator
operator|.
name|replLogger
argument_list|()
argument_list|,
operator|(
name|FunctionEvent
operator|)
name|next
argument_list|,
name|work
operator|.
name|dbNameToLoadIn
argument_list|,
name|dbTracker
argument_list|)
decl_stmt|;
name|TaskTracker
name|functionsTracker
init|=
name|loadFunction
operator|.
name|tasks
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|scope
operator|.
name|database
condition|)
block|{
name|scope
operator|.
name|rootTasks
operator|.
name|addAll
argument_list|(
name|functionsTracker
operator|.
name|tasks
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setUpDependencies
argument_list|(
name|dbTracker
argument_list|,
name|functionsTracker
argument_list|)
expr_stmt|;
block|}
name|loadTaskTracker
operator|.
name|update
argument_list|(
name|functionsTracker
argument_list|)
expr_stmt|;
name|functionsTracker
operator|.
name|debugLog
argument_list|(
literal|"functions"
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|Constraint
case|:
block|{
name|LoadConstraint
name|loadConstraint
init|=
operator|new
name|LoadConstraint
argument_list|(
name|context
argument_list|,
operator|(
name|ConstraintEvent
operator|)
name|next
argument_list|,
name|work
operator|.
name|dbNameToLoadIn
argument_list|,
name|dbTracker
argument_list|)
decl_stmt|;
name|TaskTracker
name|constraintTracker
init|=
name|loadConstraint
operator|.
name|tasks
argument_list|()
decl_stmt|;
name|scope
operator|.
name|rootTasks
operator|.
name|addAll
argument_list|(
name|constraintTracker
operator|.
name|tasks
argument_list|()
argument_list|)
expr_stmt|;
name|loadTaskTracker
operator|.
name|update
argument_list|(
name|constraintTracker
argument_list|)
expr_stmt|;
name|constraintTracker
operator|.
name|debugLog
argument_list|(
literal|"constraints"
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|loadingConstraint
operator|&&
operator|!
name|iterator
operator|.
name|currentDbHasNext
argument_list|()
condition|)
block|{
name|createEndReplLogTask
argument_list|(
name|context
argument_list|,
name|scope
argument_list|,
name|iterator
operator|.
name|replLogger
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|loadTaskTracker
operator|.
name|canAddMoreTasks
argument_list|()
condition|)
block|{
name|scope
operator|.
name|rootTasks
operator|.
name|addAll
argument_list|(
operator|new
name|ExternalTableCopyTaskBuilder
argument_list|(
name|work
argument_list|,
name|conf
argument_list|)
operator|.
name|tasks
argument_list|(
name|loadTaskTracker
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|boolean
name|addAnotherLoadTask
init|=
name|iterator
operator|.
name|hasNext
argument_list|()
operator|||
name|loadTaskTracker
operator|.
name|hasReplicationState
argument_list|()
operator|||
name|constraintIterator
operator|.
name|hasNext
argument_list|()
operator|||
name|work
operator|.
name|getPathsToCopyIterator
argument_list|()
operator|.
name|hasNext
argument_list|()
decl_stmt|;
if|if
condition|(
name|addAnotherLoadTask
condition|)
block|{
name|createBuilderTask
argument_list|(
name|scope
operator|.
name|rootTasks
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|iterator
operator|.
name|hasNext
argument_list|()
operator|&&
operator|!
name|constraintIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|loadTaskTracker
operator|.
name|update
argument_list|(
name|updateDatabaseLastReplID
argument_list|(
name|maxTasks
argument_list|,
name|context
argument_list|,
name|scope
argument_list|)
argument_list|)
expr_stmt|;
name|work
operator|.
name|updateDbEventState
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|childTasks
operator|=
name|scope
operator|.
name|rootTasks
expr_stmt|;
comment|/*       Since there can be multiple rounds of this run all of which will be tied to the same       query id -- generated in compile phase , adding a additional UUID to the end to print each run       in separate files.        */
name|LOG
operator|.
name|info
argument_list|(
literal|"Root Tasks / Total Tasks : {} / {} "
argument_list|,
name|childTasks
operator|.
name|size
argument_list|()
argument_list|,
name|loadTaskTracker
operator|.
name|numberOfTasks
argument_list|()
argument_list|)
expr_stmt|;
comment|// Populate the driver context with the scratch dir info from the repl context, so that the temp dirs will be cleaned up later
name|driverContext
operator|.
name|getCtx
argument_list|()
operator|.
name|getFsScratchDirs
argument_list|()
operator|.
name|putAll
argument_list|(
name|context
operator|.
name|pathInfo
operator|.
name|getFsScratchDirs
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"replication failed with run time exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
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
literal|"replication failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|setException
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
name|ErrorMsg
operator|.
name|getErrorMsg
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
operator|.
name|getErrorCode
argument_list|()
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"completed load task run : {}"
argument_list|,
name|work
operator|.
name|executedLoadTask
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|private
name|void
name|createEndReplLogTask
parameter_list|(
name|Context
name|context
parameter_list|,
name|Scope
name|scope
parameter_list|,
name|ReplLogger
name|replLogger
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Database
name|dbInMetadata
init|=
name|work
operator|.
name|databaseEvent
argument_list|(
name|context
operator|.
name|hiveConf
argument_list|)
operator|.
name|dbInMetadata
argument_list|(
name|work
operator|.
name|dbNameToLoadIn
argument_list|)
decl_stmt|;
name|ReplStateLogWork
name|replLogWork
init|=
operator|new
name|ReplStateLogWork
argument_list|(
name|replLogger
argument_list|,
name|dbInMetadata
operator|.
name|getParameters
argument_list|()
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|ReplStateLogWork
argument_list|>
name|replLogTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|replLogWork
argument_list|)
decl_stmt|;
if|if
condition|(
name|scope
operator|.
name|rootTasks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|scope
operator|.
name|rootTasks
operator|.
name|add
argument_list|(
name|replLogTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|DAGTraversal
operator|.
name|traverse
argument_list|(
name|scope
operator|.
name|rootTasks
argument_list|,
operator|new
name|AddDependencyToLeaves
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|replLogTask
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * There was a database update done before and we want to make sure we update the last repl    * id on this database as we are now going to switch to processing a new database.    *    * This has to be last task in the graph since if there are intermediate tasks and the last.repl.id    * is a root level task then in the execution phase the root level tasks will get executed first,    * however if any of the child tasks of the bootstrap load failed then even though the bootstrap has failed    * the last repl status of the target database will return a valid value, which will not represent    * the state of the database.    */
specifier|private
name|TaskTracker
name|updateDatabaseLastReplID
parameter_list|(
name|int
name|maxTasks
parameter_list|,
name|Context
name|context
parameter_list|,
name|Scope
name|scope
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|/*     we don't want to put any limits on this task as this is essential before we start     processing new database events.    */
name|TaskTracker
name|taskTracker
init|=
operator|new
name|AlterDatabase
argument_list|(
name|context
argument_list|,
name|work
operator|.
name|databaseEvent
argument_list|(
name|context
operator|.
name|hiveConf
argument_list|)
argument_list|,
name|work
operator|.
name|dbNameToLoadIn
argument_list|,
operator|new
name|TaskTracker
argument_list|(
name|maxTasks
argument_list|)
argument_list|)
operator|.
name|tasks
argument_list|()
decl_stmt|;
name|AddDependencyToLeaves
name|function
init|=
operator|new
name|AddDependencyToLeaves
argument_list|(
name|taskTracker
operator|.
name|tasks
argument_list|()
argument_list|)
decl_stmt|;
name|DAGTraversal
operator|.
name|traverse
argument_list|(
name|scope
operator|.
name|rootTasks
argument_list|,
name|function
argument_list|)
expr_stmt|;
return|return
name|taskTracker
return|;
block|}
specifier|private
name|void
name|partitionsPostProcessing
parameter_list|(
name|BootstrapEventsIterator
name|iterator
parameter_list|,
name|Scope
name|scope
parameter_list|,
name|TaskTracker
name|loadTaskTracker
parameter_list|,
name|TaskTracker
name|tableTracker
parameter_list|,
name|TaskTracker
name|partitionsTracker
parameter_list|)
block|{
name|setUpDependencies
argument_list|(
name|tableTracker
argument_list|,
name|partitionsTracker
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|scope
operator|.
name|database
operator|&&
operator|!
name|scope
operator|.
name|table
condition|)
block|{
name|scope
operator|.
name|rootTasks
operator|.
name|addAll
argument_list|(
name|partitionsTracker
operator|.
name|tasks
argument_list|()
argument_list|)
expr_stmt|;
name|scope
operator|.
name|partition
operator|=
literal|true
expr_stmt|;
block|}
name|loadTaskTracker
operator|.
name|update
argument_list|(
name|tableTracker
argument_list|)
expr_stmt|;
name|loadTaskTracker
operator|.
name|update
argument_list|(
name|partitionsTracker
argument_list|)
expr_stmt|;
if|if
condition|(
name|partitionsTracker
operator|.
name|hasReplicationState
argument_list|()
condition|)
block|{
name|iterator
operator|.
name|setReplicationState
argument_list|(
name|partitionsTracker
operator|.
name|replicationState
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*       This sets up dependencies such that a child task is dependant on the parent to be complete.    */
specifier|private
name|void
name|setUpDependencies
parameter_list|(
name|TaskTracker
name|parentTasks
parameter_list|,
name|TaskTracker
name|childTasks
parameter_list|)
block|{
if|if
condition|(
name|parentTasks
operator|.
name|hasTasks
argument_list|()
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
name|parentTask
range|:
name|parentTasks
operator|.
name|tasks
argument_list|()
control|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|childTask
range|:
name|childTasks
operator|.
name|tasks
argument_list|()
control|)
block|{
name|parentTask
operator|.
name|addDependentTask
argument_list|(
name|childTask
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|childTask
range|:
name|childTasks
operator|.
name|tasks
argument_list|()
control|)
block|{
name|parentTasks
operator|.
name|addTask
argument_list|(
name|childTask
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|createBuilderTask
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|)
block|{
comment|// Use loadTask as dependencyCollection
name|Task
argument_list|<
name|ReplLoadWork
argument_list|>
name|loadTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|work
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|DAGTraversal
operator|.
name|traverse
argument_list|(
name|rootTasks
argument_list|,
operator|new
name|AddDependencyToLeaves
argument_list|(
name|loadTask
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|work
operator|.
name|isIncrementalLoad
argument_list|()
condition|?
name|StageType
operator|.
name|REPL_INCREMENTAL_LOAD
else|:
name|StageType
operator|.
name|REPL_BOOTSTRAP_LOAD
return|;
block|}
specifier|private
name|int
name|executeIncrementalLoad
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
try|try
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|childTasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|parallelism
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|EXECPARALLETHREADNUMBER
argument_list|)
decl_stmt|;
comment|// during incremental we will have no parallelism from replication tasks since they are event based
comment|// and hence are linear. To achieve prallelism we have to use copy tasks(which have no DAG) for
comment|// all threads except one, in execution phase.
name|int
name|maxTasks
init|=
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
decl_stmt|;
name|IncrementalLoadTasksBuilder
name|builder
init|=
name|work
operator|.
name|getIncrementalLoadTaskBuilder
argument_list|()
decl_stmt|;
comment|// If the total number of tasks that can be created are less than the parallelism we can achieve
comment|// do nothing since someone is working on 1950's machine. else try to achieve max parallelism
name|int
name|calculatedMaxNumOfTasks
init|=
literal|0
decl_stmt|,
name|maxNumOfHDFSTasks
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|maxTasks
operator|<=
name|parallelism
condition|)
block|{
if|if
condition|(
name|builder
operator|.
name|hasMoreWork
argument_list|()
condition|)
block|{
name|calculatedMaxNumOfTasks
operator|=
name|maxTasks
expr_stmt|;
block|}
else|else
block|{
name|maxNumOfHDFSTasks
operator|=
name|maxTasks
expr_stmt|;
block|}
block|}
else|else
block|{
name|calculatedMaxNumOfTasks
operator|=
name|maxTasks
operator|-
name|parallelism
operator|+
literal|1
expr_stmt|;
name|maxNumOfHDFSTasks
operator|=
name|parallelism
operator|-
literal|1
expr_stmt|;
block|}
name|TaskTracker
name|trackerForReplIncremental
init|=
operator|new
name|TaskTracker
argument_list|(
name|calculatedMaxNumOfTasks
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|incrementalLoadTaskRoot
init|=
name|builder
operator|.
name|build
argument_list|(
name|driverContext
argument_list|,
name|getHive
argument_list|()
argument_list|,
name|LOG
argument_list|,
name|work
argument_list|,
name|trackerForReplIncremental
argument_list|)
decl_stmt|;
comment|// we are adding the incremental task first so that its always processed first,
comment|// followed by dir copy tasks if capacity allows.
name|childTasks
operator|.
name|add
argument_list|(
name|incrementalLoadTaskRoot
argument_list|)
expr_stmt|;
name|TaskTracker
name|trackerForCopy
init|=
operator|new
name|TaskTracker
argument_list|(
name|maxNumOfHDFSTasks
argument_list|)
decl_stmt|;
name|childTasks
operator|.
name|addAll
argument_list|(
operator|new
name|ExternalTableCopyTaskBuilder
argument_list|(
name|work
argument_list|,
name|conf
argument_list|)
operator|.
name|tasks
argument_list|(
name|trackerForCopy
argument_list|)
argument_list|)
expr_stmt|;
comment|// either the incremental has more work or the external table file copy has more paths to process
if|if
condition|(
name|builder
operator|.
name|hasMoreWork
argument_list|()
operator|||
name|work
operator|.
name|getPathsToCopyIterator
argument_list|()
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|DAGTraversal
operator|.
name|traverse
argument_list|(
name|childTasks
argument_list|,
operator|new
name|AddDependencyToLeaves
argument_list|(
name|TaskFactory
operator|.
name|get
argument_list|(
name|work
argument_list|,
name|conf
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|childTasks
operator|=
name|childTasks
expr_stmt|;
return|return
literal|0
return|;
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
literal|"failed replication"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|setException
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
block|}
end_class

end_unit

