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
name|lang
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
name|*
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
name|log
operator|.
name|BootstrapLoadLogger
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
name|function
operator|.
name|Consumer
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

begin_comment
comment|/**  * Replication layout is from the root directory of replication Dump is  * db  *    _external_tables_info  *    table1  *        _metadata  *        data  *          _files  *    table2  *        _metadata  *        data  *          _files  *    _functions  *        functionName1  *          _metadata  *        functionName2  *          _metadata  * this class understands this layout and hence will help in identifying for subsequent bootstrap tasks  * as to where the last set of tasks left execution and from where this task should pick up replication.  * Since for replication we have the need for hierarchy of tasks we need to make sure that db level are  * processed first before table, table level are processed first before partitions etc.  *  * Based on how the metadata is being exported on the file we have to currently take care of the following:  * 1. Make sure db level are processed first as this will be required before table / functions processing.  * 2. Table before partition is not explicitly required as table and partition metadata are in the same file.  *  *  * For future integrations other sources of events like kafka, would require to implement an Iterator&lt;BootstrapEvent&gt;  *  */
end_comment

begin_class
specifier|public
class|class
name|BootstrapEventsIterator
implements|implements
name|Iterator
argument_list|<
name|BootstrapEvent
argument_list|>
block|{
specifier|private
name|DatabaseEventsIterator
name|currentDatabaseIterator
init|=
literal|null
decl_stmt|;
comment|/*       This denotes listing of any directories where during replication we want to take care of       db level operations first, namely in our case its only during db creation on the replica       warehouse.    */
specifier|private
name|Iterator
argument_list|<
name|DatabaseEventsIterator
argument_list|>
name|dbEventsIterator
decl_stmt|;
specifier|private
specifier|final
name|String
name|dumpDirectory
decl_stmt|;
specifier|private
specifier|final
name|String
name|dbNameToLoadIn
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|needLogger
decl_stmt|;
specifier|private
name|ReplLogger
name|replLogger
decl_stmt|;
specifier|public
name|BootstrapEventsIterator
parameter_list|(
name|String
name|dumpDirectory
parameter_list|,
name|String
name|dbNameToLoadIn
parameter_list|,
name|boolean
name|needLogger
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|dumpDirectory
argument_list|)
decl_stmt|;
name|FileSystem
name|fileSystem
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|fileStatuses
init|=
name|fileSystem
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|dumpDirectory
argument_list|)
argument_list|,
name|ReplUtils
operator|.
name|getBootstrapDirectoryFilter
argument_list|(
name|fileSystem
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|fileStatuses
operator|==
literal|null
operator|)
operator|||
operator|(
name|fileStatuses
operator|.
name|length
operator|==
literal|0
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No data to load in path "
operator|+
name|dumpDirectory
argument_list|)
throw|;
block|}
if|if
condition|(
operator|(
name|dbNameToLoadIn
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|fileStatuses
operator|.
name|length
operator|>
literal|1
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Multiple dirs in "
operator|+
name|dumpDirectory
operator|+
literal|" does not correspond to REPL LOAD expecting to load to a singular destination point."
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|FileStatus
argument_list|>
name|dbsToCreate
init|=
name|Arrays
operator|.
name|stream
argument_list|(
name|fileStatuses
argument_list|)
operator|.
name|filter
argument_list|(
name|f
lambda|->
operator|!
name|f
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|ReplUtils
operator|.
name|CONSTRAINTS_ROOT_DIR_NAME
argument_list|)
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
name|dbEventsIterator
operator|=
name|dbsToCreate
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|f
lambda|->
block|{
try|try
block|{
return|return
operator|new
name|DatabaseEventsIterator
argument_list|(
name|f
operator|.
name|getPath
argument_list|()
argument_list|,
name|hiveConf
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error while creating event iterator for db at path"
operator|+
name|f
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
operator|.
name|iterator
argument_list|()
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
name|needLogger
operator|=
name|needLogger
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|currentDatabaseIterator
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|dbEventsIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|currentDatabaseIterator
operator|=
name|dbEventsIterator
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|needLogger
condition|)
block|{
name|initReplLogger
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|currentDatabaseIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
name|currentDatabaseIterator
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|BootstrapEvent
name|next
parameter_list|()
block|{
return|return
name|currentDatabaseIterator
operator|.
name|next
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"This operation is not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|forEachRemaining
parameter_list|(
name|Consumer
argument_list|<
name|?
super|super
name|BootstrapEvent
argument_list|>
name|action
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"This operation is not supported"
argument_list|)
throw|;
block|}
specifier|public
name|boolean
name|currentDbHasNext
parameter_list|()
block|{
return|return
operator|(
operator|(
name|currentDatabaseIterator
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|currentDatabaseIterator
operator|.
name|hasNext
argument_list|()
operator|)
operator|)
return|;
block|}
specifier|public
name|void
name|setReplicationState
parameter_list|(
name|ReplicationState
name|replicationState
parameter_list|)
block|{
name|this
operator|.
name|currentDatabaseIterator
operator|.
name|replicationState
operator|=
name|replicationState
expr_stmt|;
block|}
specifier|public
name|ReplLogger
name|replLogger
parameter_list|()
block|{
return|return
name|replLogger
return|;
block|}
specifier|private
name|void
name|initReplLogger
parameter_list|()
block|{
try|try
block|{
name|Path
name|dbDumpPath
init|=
name|currentDatabaseIterator
operator|.
name|dbLevelPath
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|dbDumpPath
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|long
name|numTables
init|=
name|getSubDirs
argument_list|(
name|fs
argument_list|,
name|dbDumpPath
argument_list|)
operator|.
name|length
decl_stmt|;
name|long
name|numFunctions
init|=
literal|0
decl_stmt|;
name|Path
name|funcPath
init|=
operator|new
name|Path
argument_list|(
name|dbDumpPath
argument_list|,
name|ReplUtils
operator|.
name|FUNCTIONS_ROOT_DIR_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|funcPath
argument_list|)
condition|)
block|{
name|numFunctions
operator|=
name|getSubDirs
argument_list|(
name|fs
argument_list|,
name|funcPath
argument_list|)
operator|.
name|length
expr_stmt|;
block|}
name|String
name|dbName
init|=
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|dbNameToLoadIn
argument_list|)
condition|?
name|dbDumpPath
operator|.
name|getName
argument_list|()
else|:
name|dbNameToLoadIn
decl_stmt|;
name|replLogger
operator|=
operator|new
name|BootstrapLoadLogger
argument_list|(
name|dbName
argument_list|,
name|dumpDirectory
argument_list|,
name|numTables
argument_list|,
name|numFunctions
argument_list|)
expr_stmt|;
name|replLogger
operator|.
name|startLog
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Ignore the exception
block|}
block|}
name|FileStatus
index|[]
name|getSubDirs
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|dirPath
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fs
operator|.
name|listStatus
argument_list|(
name|dirPath
argument_list|,
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
name|String
name|name
init|=
name|p
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
operator|&&
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"."
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
block|}
end_class

end_unit

