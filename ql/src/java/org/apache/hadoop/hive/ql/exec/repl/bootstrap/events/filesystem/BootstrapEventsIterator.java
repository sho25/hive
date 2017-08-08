begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*   Licensed to the Apache Software Foundation (ASF) under one   or more contributor license agreements.  See the NOTICE file   distributed with this work for additional information   regarding copyright ownership.  The ASF licenses this file   to you under the Apache License, Version 2.0 (the   "License"); you may not use this file except in compliance   with the License.  You may obtain a copy of the License at        http://www.apache.org/licenses/LICENSE-2.0    Unless required by applicable law or agreed to in writing, software   distributed under the License is distributed on an "AS IS" BASIS,   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   See the License for the specific language governing permissions and   limitations under the License.  */
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
name|parse
operator|.
name|EximUtil
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
comment|/**  * Replication layout is from the root directory of replication Dump is  * db  *    table1  *        _metadata  *        data  *          _files  *    table2  *        _metadata  *        data  *          _files  *    _functions  *        functionName1  *          _metadata  *        functionName2  *          _metadata  * this class understands this layout and hence will help in identifying for subsequent bootstrap tasks  * as to where the last set of tasks left execution and from where this task should pick up replication.  * Since for replication we have the need for hierarchy of tasks we need to make sure that db level are  * processed first before table, table level are processed first before partitions etc.  *  * Based on how the metadata is being exported on the file we have to currently take care of the following:  * 1. Make sure db level are processed first as this will be required before table / functions processing.  * 2. Table before partition is not explicitly required as table and partition metadata are in the same file.  *  *  * For future integrations other sources of events like kafka, would require to implement an Iterator<BootstrapEvent>  *  */
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
specifier|public
name|BootstrapEventsIterator
parameter_list|(
name|String
name|dumpDirectory
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
name|EximUtil
operator|.
name|getDirectoryFilter
argument_list|(
name|fileSystem
argument_list|)
argument_list|)
decl_stmt|;
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
block|{
name|Path
name|metadataPath
init|=
operator|new
name|Path
argument_list|(
name|f
operator|.
name|getPath
argument_list|()
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|EximUtil
operator|.
name|METADATA_NAME
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|fileSystem
operator|.
name|exists
argument_list|(
name|metadataPath
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
literal|"could not determine if exists : "
operator|+
name|metadataPath
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
block|}
end_class

end_unit

