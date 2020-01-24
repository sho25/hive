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
name|bootstrap
operator|.
name|events
operator|.
name|filesystem
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
name|LocatedFileStatus
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
name|RemoteIterator
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
name|ReplExternalTables
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
name|load
operator|.
name|ReplicationState
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
name|parse
operator|.
name|EximUtil
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
name|EventDumpDirComparator
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
name|IOException
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
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|util
operator|.
name|ReplUtils
operator|.
name|FUNCTIONS_ROOT_DIR_NAME
import|;
end_import

begin_class
class|class
name|DatabaseEventsIterator
implements|implements
name|Iterator
argument_list|<
name|BootstrapEvent
argument_list|>
block|{
specifier|private
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|DatabaseEventsIterator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|RemoteIterator
argument_list|<
name|LocatedFileStatus
argument_list|>
name|remoteIterator
decl_stmt|;
specifier|private
specifier|final
name|Path
name|dbLevelPath
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
name|ReplicationState
name|replicationState
decl_stmt|;
specifier|private
name|Path
name|next
init|=
literal|null
decl_stmt|,
name|previous
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|databaseEventProcessed
init|=
literal|false
decl_stmt|;
name|DatabaseEventsIterator
parameter_list|(
name|Path
name|dbLevelPath
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|dbLevelPath
operator|=
name|dbLevelPath
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|FileSystem
name|fileSystem
init|=
name|dbLevelPath
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
comment|// this is only there for the use case where we are doing table only replication and not database level
if|if
condition|(
operator|!
name|fileSystem
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|dbLevelPath
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|EximUtil
operator|.
name|METADATA_NAME
argument_list|)
argument_list|)
condition|)
block|{
name|databaseEventProcessed
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_IN_REPL_TEST_FILES_SORTED
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|" file sorting is enabled in DatabaseEventsIterator"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|LocatedFileStatus
argument_list|>
name|fileStatuses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Sort the directories in tha path and then add the files recursively .
name|getSortedFileList
argument_list|(
name|dbLevelPath
argument_list|,
name|fileStatuses
argument_list|,
name|fileSystem
argument_list|)
expr_stmt|;
name|remoteIterator
operator|=
operator|new
name|RemoteIterator
argument_list|<
name|LocatedFileStatus
argument_list|>
argument_list|()
block|{
specifier|private
name|int
name|idx
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|int
name|numEntry
init|=
name|fileStatuses
operator|.
name|size
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|LocatedFileStatus
argument_list|>
name|fileStatusesLocal
init|=
name|fileStatuses
decl_stmt|;
specifier|public
name|boolean
name|hasNext
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|idx
operator|<
name|numEntry
return|;
block|}
specifier|public
name|LocatedFileStatus
name|next
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|" file in next is "
operator|+
name|fileStatusesLocal
operator|.
name|get
argument_list|(
name|idx
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|fileStatusesLocal
operator|.
name|get
argument_list|(
name|idx
operator|++
argument_list|)
return|;
block|}
block|}
expr_stmt|;
block|}
else|else
block|{
name|remoteIterator
operator|=
name|fileSystem
operator|.
name|listFiles
argument_list|(
name|dbLevelPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|getSortedFileList
parameter_list|(
name|Path
name|eventPath
parameter_list|,
name|List
argument_list|<
name|LocatedFileStatus
argument_list|>
name|fileStatuses
parameter_list|,
name|FileSystem
name|fileSystem
parameter_list|)
throws|throws
name|IOException
block|{
comment|//Add all the files in this directory. No need to sort.
name|RemoteIterator
argument_list|<
name|LocatedFileStatus
argument_list|>
name|iteratorNext
init|=
name|fileSystem
operator|.
name|listFiles
argument_list|(
name|eventPath
argument_list|,
literal|false
argument_list|)
decl_stmt|;
while|while
condition|(
name|iteratorNext
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|LocatedFileStatus
name|status
init|=
name|iteratorNext
operator|.
name|next
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|" files added at getSortedFileList"
operator|+
name|status
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|fileStatuses
operator|.
name|add
argument_list|(
name|status
argument_list|)
expr_stmt|;
block|}
comment|// get all the directories in this path and sort them
name|FileStatus
index|[]
name|eventDirs
init|=
name|fileSystem
operator|.
name|listStatus
argument_list|(
name|eventPath
argument_list|,
name|EximUtil
operator|.
name|getDirectoryFilter
argument_list|(
name|fileSystem
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|eventDirs
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|Arrays
operator|.
name|sort
argument_list|(
name|eventDirs
argument_list|,
operator|new
name|EventDumpDirComparator
argument_list|()
argument_list|)
expr_stmt|;
comment|// add files recursively for each directory
for|for
control|(
name|FileStatus
name|fs
range|:
name|eventDirs
control|)
block|{
name|getSortedFileList
argument_list|(
name|fs
operator|.
name|getPath
argument_list|()
argument_list|,
name|fileStatuses
argument_list|,
name|fileSystem
argument_list|)
expr_stmt|;
block|}
block|}
name|Path
name|dbLevelPath
parameter_list|()
block|{
return|return
name|this
operator|.
name|dbLevelPath
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
operator|!
name|databaseEventProcessed
condition|)
block|{
name|next
operator|=
name|dbLevelPath
expr_stmt|;
return|return
literal|true
return|;
block|}
if|if
condition|(
name|replicationState
operator|==
literal|null
operator|&&
name|next
operator|==
literal|null
condition|)
block|{
while|while
condition|(
name|remoteIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|LocatedFileStatus
name|next
init|=
name|remoteIterator
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// we want to skip this file, this also means there cant be a table with name represented
comment|// by constant ReplExternalTables.FILE_NAME or ReplUtils.REPL_TABLE_LIST_DIR_NAME (_tables)
if|if
condition|(
name|next
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|endsWith
argument_list|(
name|ReplExternalTables
operator|.
name|FILE_NAME
argument_list|)
operator|||
name|next
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|endsWith
argument_list|(
name|ReplUtils
operator|.
name|REPL_TABLE_LIST_DIR_NAME
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|next
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|endsWith
argument_list|(
name|EximUtil
operator|.
name|METADATA_NAME
argument_list|)
condition|)
block|{
name|String
name|replacedString
init|=
name|next
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|replace
argument_list|(
name|dbLevelPath
operator|.
name|toString
argument_list|()
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|filteredNames
init|=
name|Arrays
operator|.
name|stream
argument_list|(
name|replacedString
operator|.
name|split
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
argument_list|)
operator|.
name|filter
argument_list|(
name|StringUtils
operator|::
name|isNotBlank
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|filteredNames
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
comment|// this relates to db level event tracked via databaseEventProcessed
block|}
else|else
block|{
name|this
operator|.
name|next
operator|=
name|next
operator|.
name|getPath
argument_list|()
operator|.
name|getParent
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// may be do some retry logic here.
name|LOG
operator|.
name|error
argument_list|(
literal|"could not traverse the file via remote iterator "
operator|+
name|dbLevelPath
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/*   we handle three types of scenarios with special case.   1. handling of db Level _metadata   2. handling of subsequent loadTask which will start running from the previous replicationState   3. other events : these can only be either table / function _metadata.    */
annotation|@
name|Override
specifier|public
name|BootstrapEvent
name|next
parameter_list|()
block|{
if|if
condition|(
operator|!
name|databaseEventProcessed
condition|)
block|{
name|FSDatabaseEvent
name|event
init|=
operator|new
name|FSDatabaseEvent
argument_list|(
name|hiveConf
argument_list|,
name|next
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|databaseEventProcessed
operator|=
literal|true
expr_stmt|;
return|return
name|postProcessing
argument_list|(
name|event
argument_list|)
return|;
block|}
if|if
condition|(
name|replicationState
operator|!=
literal|null
condition|)
block|{
return|return
name|eventForReplicationState
argument_list|()
return|;
block|}
name|String
name|currentPath
init|=
name|next
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentPath
operator|.
name|contains
argument_list|(
name|FUNCTIONS_ROOT_DIR_NAME
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"functions directory: {}"
argument_list|,
name|next
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|postProcessing
argument_list|(
operator|new
name|FSFunctionEvent
argument_list|(
name|next
argument_list|)
argument_list|)
return|;
block|}
return|return
name|postProcessing
argument_list|(
operator|new
name|FSTableEvent
argument_list|(
name|hiveConf
argument_list|,
name|next
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|BootstrapEvent
name|postProcessing
parameter_list|(
name|BootstrapEvent
name|bootstrapEvent
parameter_list|)
block|{
name|previous
operator|=
name|next
expr_stmt|;
name|next
operator|=
literal|null
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"processing "
operator|+
name|previous
argument_list|)
expr_stmt|;
return|return
name|bootstrapEvent
return|;
block|}
specifier|private
name|BootstrapEvent
name|eventForReplicationState
parameter_list|()
block|{
if|if
condition|(
name|replicationState
operator|.
name|partitionState
operator|!=
literal|null
condition|)
block|{
name|BootstrapEvent
name|bootstrapEvent
init|=
operator|new
name|FSPartitionEvent
argument_list|(
name|hiveConf
argument_list|,
name|previous
operator|.
name|toString
argument_list|()
argument_list|,
name|replicationState
argument_list|)
decl_stmt|;
name|replicationState
operator|=
literal|null
expr_stmt|;
return|return
name|bootstrapEvent
return|;
block|}
elseif|else
if|if
condition|(
name|replicationState
operator|.
name|lastTableReplicated
operator|!=
literal|null
condition|)
block|{
name|FSTableEvent
name|event
init|=
operator|new
name|FSTableEvent
argument_list|(
name|hiveConf
argument_list|,
name|previous
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|replicationState
operator|=
literal|null
expr_stmt|;
return|return
name|event
return|;
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"for replicationState "
operator|+
name|replicationState
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

