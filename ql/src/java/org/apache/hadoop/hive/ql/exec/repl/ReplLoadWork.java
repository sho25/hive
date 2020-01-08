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
name|common
operator|.
name|repl
operator|.
name|ReplScope
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
name|incremental
operator|.
name|IncrementalLoadEventsIterator
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
name|ReplUtils
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
name|Explain
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
name|LineageState
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
name|Iterator
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
name|ExternalTableCopyTaskBuilder
operator|.
name|DirCopyWork
import|;
end_import

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Replication Load Operator"
argument_list|,
name|explainLevels
operator|=
block|{
name|Explain
operator|.
name|Level
operator|.
name|USER
block|,
name|Explain
operator|.
name|Level
operator|.
name|DEFAULT
block|,
name|Explain
operator|.
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|ReplLoadWork
implements|implements
name|Serializable
block|{
specifier|final
name|String
name|dbNameToLoadIn
decl_stmt|;
specifier|final
name|ReplScope
name|currentReplScope
decl_stmt|;
specifier|final
name|String
name|dumpDirectory
decl_stmt|;
specifier|final
name|String
name|bootstrapDumpToCleanTables
decl_stmt|;
name|boolean
name|needCleanTablesFromBootstrap
decl_stmt|;
specifier|private
specifier|final
name|ConstraintEventsIterator
name|constraintsIterator
decl_stmt|;
specifier|private
name|int
name|loadTaskRunCount
init|=
literal|0
decl_stmt|;
specifier|private
name|DatabaseEvent
operator|.
name|State
name|state
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
specifier|transient
name|BootstrapEventsIterator
name|bootstrapIterator
decl_stmt|;
specifier|private
specifier|transient
name|IncrementalLoadTasksBuilder
name|incrementalLoadTasksBuilder
decl_stmt|;
specifier|private
specifier|transient
name|Task
argument_list|<
name|?
argument_list|>
name|rootTask
decl_stmt|;
specifier|private
specifier|final
specifier|transient
name|Iterator
argument_list|<
name|DirCopyWork
argument_list|>
name|pathsToCopyIterator
decl_stmt|;
comment|/*   these are sessionState objects that are copied over to work to allow for parallel execution.   based on the current use case the methods are selectively synchronized, which might need to be   taken care when using other methods.   */
specifier|final
name|LineageState
name|sessionStateLineageState
decl_stmt|;
specifier|public
name|ReplLoadWork
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|dumpDirectory
parameter_list|,
name|String
name|dbNameToLoadIn
parameter_list|,
name|ReplScope
name|currentReplScope
parameter_list|,
name|LineageState
name|lineageState
parameter_list|,
name|boolean
name|isIncrementalDump
parameter_list|,
name|Long
name|eventTo
parameter_list|,
name|List
argument_list|<
name|DirCopyWork
argument_list|>
name|pathsToCopyIterator
parameter_list|)
throws|throws
name|IOException
block|{
name|sessionStateLineageState
operator|=
name|lineageState
expr_stmt|;
name|this
operator|.
name|dumpDirectory
operator|=
name|dumpDirectory
expr_stmt|;
name|this
operator|.
name|dbNameToLoadIn
operator|=
name|dbNameToLoadIn
expr_stmt|;
name|this
operator|.
name|currentReplScope
operator|=
name|currentReplScope
expr_stmt|;
comment|// If DB name is changed during REPL LOAD, then set it instead of referring to source DB name.
if|if
condition|(
operator|(
name|currentReplScope
operator|!=
literal|null
operator|)
operator|&&
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|dbNameToLoadIn
argument_list|)
condition|)
block|{
name|currentReplScope
operator|.
name|setDbName
argument_list|(
name|dbNameToLoadIn
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|bootstrapDumpToCleanTables
operator|=
name|hiveConf
operator|.
name|get
argument_list|(
name|ReplUtils
operator|.
name|REPL_CLEAN_TABLES_FROM_BOOTSTRAP_CONFIG
argument_list|)
expr_stmt|;
name|this
operator|.
name|needCleanTablesFromBootstrap
operator|=
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|this
operator|.
name|bootstrapDumpToCleanTables
argument_list|)
expr_stmt|;
name|rootTask
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|isIncrementalDump
condition|)
block|{
name|incrementalLoadTasksBuilder
operator|=
operator|new
name|IncrementalLoadTasksBuilder
argument_list|(
name|dbNameToLoadIn
argument_list|,
name|dumpDirectory
argument_list|,
operator|new
name|IncrementalLoadEventsIterator
argument_list|(
name|dumpDirectory
argument_list|,
name|hiveConf
argument_list|)
argument_list|,
name|hiveConf
argument_list|,
name|eventTo
argument_list|)
expr_stmt|;
comment|/*        * If the current incremental dump also includes bootstrap for some tables, then create iterator        * for the same.        */
name|Path
name|incBootstrapDir
init|=
operator|new
name|Path
argument_list|(
name|dumpDirectory
argument_list|,
name|ReplUtils
operator|.
name|INC_BOOTSTRAP_ROOT_DIR_NAME
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|incBootstrapDir
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|incBootstrapDir
argument_list|)
condition|)
block|{
name|this
operator|.
name|bootstrapIterator
operator|=
operator|new
name|BootstrapEventsIterator
argument_list|(
name|incBootstrapDir
operator|.
name|toString
argument_list|()
argument_list|,
name|dbNameToLoadIn
argument_list|,
literal|true
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|this
operator|.
name|constraintsIterator
operator|=
operator|new
name|ConstraintEventsIterator
argument_list|(
name|dumpDirectory
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|bootstrapIterator
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|constraintsIterator
operator|=
literal|null
expr_stmt|;
block|}
block|}
else|else
block|{
name|this
operator|.
name|bootstrapIterator
operator|=
operator|new
name|BootstrapEventsIterator
argument_list|(
name|dumpDirectory
argument_list|,
name|dbNameToLoadIn
argument_list|,
literal|true
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|this
operator|.
name|constraintsIterator
operator|=
operator|new
name|ConstraintEventsIterator
argument_list|(
name|dumpDirectory
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|incrementalLoadTasksBuilder
operator|=
literal|null
expr_stmt|;
block|}
name|this
operator|.
name|pathsToCopyIterator
operator|=
name|pathsToCopyIterator
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
name|BootstrapEventsIterator
name|bootstrapIterator
parameter_list|()
block|{
return|return
name|bootstrapIterator
return|;
block|}
name|ConstraintEventsIterator
name|constraintsIterator
parameter_list|()
block|{
return|return
name|constraintsIterator
return|;
block|}
name|int
name|executedLoadTask
parameter_list|()
block|{
return|return
operator|++
name|loadTaskRunCount
return|;
block|}
name|void
name|updateDbEventState
parameter_list|(
name|DatabaseEvent
operator|.
name|State
name|state
parameter_list|)
block|{
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
block|}
name|DatabaseEvent
name|databaseEvent
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
return|return
name|state
operator|.
name|toEvent
argument_list|(
name|hiveConf
argument_list|)
return|;
block|}
name|boolean
name|hasDbState
parameter_list|()
block|{
return|return
name|state
operator|!=
literal|null
return|;
block|}
name|boolean
name|isIncrementalLoad
parameter_list|()
block|{
return|return
name|incrementalLoadTasksBuilder
operator|!=
literal|null
return|;
block|}
name|boolean
name|hasBootstrapLoadTasks
parameter_list|()
block|{
return|return
operator|(
operator|(
operator|(
name|bootstrapIterator
operator|!=
literal|null
operator|)
operator|&&
name|bootstrapIterator
operator|.
name|hasNext
argument_list|()
operator|)
operator|||
operator|(
operator|(
name|constraintsIterator
operator|!=
literal|null
operator|)
operator|&&
name|constraintsIterator
operator|.
name|hasNext
argument_list|()
operator|)
operator|)
return|;
block|}
name|IncrementalLoadTasksBuilder
name|incrementalLoadTasksBuilder
parameter_list|()
block|{
return|return
name|incrementalLoadTasksBuilder
return|;
block|}
specifier|public
name|Task
argument_list|<
name|?
argument_list|>
name|getRootTask
parameter_list|()
block|{
return|return
name|rootTask
return|;
block|}
specifier|public
name|void
name|setRootTask
parameter_list|(
name|Task
argument_list|<
name|?
argument_list|>
name|rootTask
parameter_list|)
block|{
name|this
operator|.
name|rootTask
operator|=
name|rootTask
expr_stmt|;
block|}
specifier|public
name|Iterator
argument_list|<
name|DirCopyWork
argument_list|>
name|getPathsToCopyIterator
parameter_list|()
block|{
return|return
name|pathsToCopyIterator
return|;
block|}
block|}
end_class

end_unit

