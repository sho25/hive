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
name|FragmentSpecProto
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
name|SourceStateProto
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
name|CompositeService
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
name|Collections
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
name|ConcurrentHashMap
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
name|ConcurrentMap
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
name|locks
operator|.
name|Lock
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
name|locks
operator|.
name|ReadWriteLock
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
name|locks
operator|.
name|ReentrantLock
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
name|locks
operator|.
name|ReentrantReadWriteLock
import|;
end_import

begin_comment
comment|/**  * Tracks queries running within a daemon  */
end_comment

begin_class
specifier|public
class|class
name|QueryTracker
extends|extends
name|CompositeService
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
name|QueryTracker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|QueryFileCleaner
name|queryFileCleaner
decl_stmt|;
comment|// TODO Make use if the query id for cachin when this is available.
specifier|private
specifier|final
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|QueryInfo
argument_list|>
name|queryInfoMap
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|String
index|[]
name|localDirsBase
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|localFs
decl_stmt|;
comment|// TODO At the moment there's no way of knowing whether a query is running or not.
comment|// A race is possible between dagComplete and registerFragment - where the registerFragment
comment|// is processed after a dagCompletes.
comment|// May need to keep track of completed dags for a certain time duration to avoid this.
comment|// Alternately - send in an explicit dag start message before any other message is processed.
comment|// Multiple threads communicating from a single AM gets in the way of this.
comment|// Keeps track of completed dags. Assumes dag names are unique across AMs.
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|completedDagMap
init|=
name|Collections
operator|.
name|newSetFromMap
argument_list|(
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Lock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|ReadWriteLock
argument_list|>
name|dagSpecificLocks
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Tracks various maps for dagCompletions. This is setup here since stateChange messages
comment|// may be processed by a thread which ends up executing before a task.
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|SourceStateProto
argument_list|>
argument_list|>
name|sourceCompletionMap
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|QueryTracker
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
index|[]
name|localDirsBase
parameter_list|)
block|{
name|super
argument_list|(
literal|"QueryTracker"
argument_list|)
expr_stmt|;
name|this
operator|.
name|localDirsBase
operator|=
name|localDirsBase
expr_stmt|;
try|try
block|{
name|localFs
operator|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
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
name|RuntimeException
argument_list|(
literal|"Failed to setup local filesystem instance"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|queryFileCleaner
operator|=
operator|new
name|QueryFileCleaner
argument_list|(
name|conf
argument_list|,
name|localFs
argument_list|)
expr_stmt|;
name|addService
argument_list|(
name|queryFileCleaner
argument_list|)
expr_stmt|;
block|}
comment|/**    * Register a new fragment for a specific query    * @param queryId    * @param appIdString    * @param dagName    * @param dagIdentifier    * @param vertexName    * @param fragmentNumber    * @param attemptNumber    * @param user    * @throws IOException    */
name|QueryFragmentInfo
name|registerFragment
parameter_list|(
name|String
name|queryId
parameter_list|,
name|String
name|appIdString
parameter_list|,
name|String
name|dagName
parameter_list|,
name|int
name|dagIdentifier
parameter_list|,
name|String
name|vertexName
parameter_list|,
name|int
name|fragmentNumber
parameter_list|,
name|int
name|attemptNumber
parameter_list|,
name|String
name|user
parameter_list|,
name|FragmentSpecProto
name|fragmentSpec
parameter_list|)
throws|throws
name|IOException
block|{
name|ReadWriteLock
name|dagLock
init|=
name|getDagLock
argument_list|(
name|dagName
argument_list|)
decl_stmt|;
name|dagLock
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|completedDagMap
operator|.
name|contains
argument_list|(
name|dagName
argument_list|)
condition|)
block|{
name|QueryInfo
name|queryInfo
init|=
name|queryInfoMap
operator|.
name|get
argument_list|(
name|dagName
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryInfo
operator|==
literal|null
condition|)
block|{
name|queryInfo
operator|=
operator|new
name|QueryInfo
argument_list|(
name|queryId
argument_list|,
name|appIdString
argument_list|,
name|dagName
argument_list|,
name|dagIdentifier
argument_list|,
name|user
argument_list|,
name|getSourceCompletionMap
argument_list|(
name|dagName
argument_list|)
argument_list|,
name|localDirsBase
argument_list|,
name|localFs
argument_list|)
expr_stmt|;
name|queryInfoMap
operator|.
name|putIfAbsent
argument_list|(
name|dagName
argument_list|,
name|queryInfo
argument_list|)
expr_stmt|;
block|}
return|return
name|queryInfo
operator|.
name|registerFragment
argument_list|(
name|vertexName
argument_list|,
name|fragmentNumber
argument_list|,
name|attemptNumber
argument_list|,
name|fragmentSpec
argument_list|)
return|;
block|}
else|else
block|{
comment|// Cleanup the dag lock here, since it may have been created after the query completed
name|dagSpecificLocks
operator|.
name|remove
argument_list|(
name|dagName
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Dag "
operator|+
name|dagName
operator|+
literal|" already complete. Rejecting fragment ["
operator|+
name|vertexName
operator|+
literal|", "
operator|+
name|fragmentNumber
operator|+
literal|", "
operator|+
name|attemptNumber
argument_list|)
throw|;
block|}
block|}
finally|finally
block|{
name|dagLock
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Indicate to the tracker that a fragment is complete. This is from internal execution within the daemon    * @param fragmentInfo    */
name|void
name|fragmentComplete
parameter_list|(
name|QueryFragmentInfo
name|fragmentInfo
parameter_list|)
block|{
name|String
name|dagName
init|=
name|fragmentInfo
operator|.
name|getQueryInfo
argument_list|()
operator|.
name|getDagName
argument_list|()
decl_stmt|;
name|QueryInfo
name|queryInfo
init|=
name|queryInfoMap
operator|.
name|get
argument_list|(
name|dagName
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryInfo
operator|==
literal|null
condition|)
block|{
comment|// Possible because a queryComplete message from the AM can come in first - KILL / SUCCESSFUL,
comment|// before the fragmentComplete is reported
name|LOG
operator|.
name|info
argument_list|(
literal|"Ignoring fragmentComplete message for unknown query"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|queryInfo
operator|.
name|unregisterFragment
argument_list|(
name|fragmentInfo
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Register completion for a query    * @param queryId    * @param dagName    * @param deleteDelay    */
name|void
name|queryComplete
parameter_list|(
name|String
name|queryId
parameter_list|,
name|String
name|dagName
parameter_list|,
name|long
name|deleteDelay
parameter_list|)
block|{
name|ReadWriteLock
name|dagLock
init|=
name|getDagLock
argument_list|(
name|dagName
argument_list|)
decl_stmt|;
name|dagLock
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|completedDagMap
operator|.
name|add
argument_list|(
name|dagName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Processing queryComplete for dagName={} with deleteDelay={} seconds"
argument_list|,
name|dagName
argument_list|,
name|deleteDelay
argument_list|)
expr_stmt|;
name|completedDagMap
operator|.
name|add
argument_list|(
name|dagName
argument_list|)
expr_stmt|;
name|QueryInfo
name|queryInfo
init|=
name|queryInfoMap
operator|.
name|remove
argument_list|(
name|dagName
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryInfo
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Ignoring query complete for unknown dag: {}"
argument_list|,
name|dagName
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|localDirs
init|=
name|queryInfo
operator|.
name|getLocalDirsNoCreate
argument_list|()
decl_stmt|;
if|if
condition|(
name|localDirs
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|localDir
range|:
name|localDirs
control|)
block|{
name|queryFileCleaner
operator|.
name|cleanupDir
argument_list|(
name|localDir
argument_list|,
name|deleteDelay
argument_list|)
expr_stmt|;
name|ShuffleHandler
operator|.
name|get
argument_list|()
operator|.
name|unregisterDag
argument_list|(
name|localDir
argument_list|,
name|dagName
argument_list|,
name|queryInfo
operator|.
name|getDagIdentifier
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|sourceCompletionMap
operator|.
name|remove
argument_list|(
name|dagName
argument_list|)
expr_stmt|;
name|dagSpecificLocks
operator|.
name|remove
argument_list|(
name|dagName
argument_list|)
expr_stmt|;
comment|// TODO HIVE-10762 Issue a kill message to all running fragments for this container.
comment|// TODO HIVE-10535 Cleanup map join cache
block|}
finally|finally
block|{
name|dagLock
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Register an update to a source within an executing dag    * @param dagName    * @param sourceName    * @param sourceState    */
name|void
name|registerSourceStateChange
parameter_list|(
name|String
name|dagName
parameter_list|,
name|String
name|sourceName
parameter_list|,
name|SourceStateProto
name|sourceState
parameter_list|)
block|{
name|getSourceCompletionMap
argument_list|(
name|dagName
argument_list|)
operator|.
name|put
argument_list|(
name|sourceName
argument_list|,
name|sourceState
argument_list|)
expr_stmt|;
comment|// TODO HIVE-10758 source completion notifications
block|}
specifier|private
name|ReadWriteLock
name|getDagLock
parameter_list|(
name|String
name|dagName
parameter_list|)
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|ReadWriteLock
name|dagLock
init|=
name|dagSpecificLocks
operator|.
name|get
argument_list|(
name|dagName
argument_list|)
decl_stmt|;
if|if
condition|(
name|dagLock
operator|==
literal|null
condition|)
block|{
name|dagLock
operator|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
expr_stmt|;
name|dagSpecificLocks
operator|.
name|put
argument_list|(
name|dagName
argument_list|,
name|dagLock
argument_list|)
expr_stmt|;
block|}
return|return
name|dagLock
return|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|SourceStateProto
argument_list|>
name|getSourceCompletionMap
parameter_list|(
name|String
name|dagName
parameter_list|)
block|{
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|SourceStateProto
argument_list|>
name|dagMap
init|=
name|sourceCompletionMap
operator|.
name|get
argument_list|(
name|dagName
argument_list|)
decl_stmt|;
if|if
condition|(
name|dagMap
operator|==
literal|null
condition|)
block|{
name|dagMap
operator|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|SourceStateProto
argument_list|>
name|old
init|=
name|sourceCompletionMap
operator|.
name|putIfAbsent
argument_list|(
name|dagName
argument_list|,
name|dagMap
argument_list|)
decl_stmt|;
name|dagMap
operator|=
operator|(
name|old
operator|!=
literal|null
operator|)
condition|?
name|old
else|:
name|dagMap
expr_stmt|;
block|}
return|return
name|dagMap
return|;
block|}
block|}
end_class

end_unit

