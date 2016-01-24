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
name|HashMap
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|HashMultimap
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
name|collect
operator|.
name|Lists
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
name|collect
operator|.
name|Multimap
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
name|llap
operator|.
name|daemon
operator|.
name|FinishableStateUpdateHandler
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

begin_class
specifier|public
class|class
name|QueryInfo
block|{
specifier|private
specifier|final
name|QueryIdentifier
name|queryIdentifier
decl_stmt|;
specifier|private
specifier|final
name|String
name|appIdString
decl_stmt|;
specifier|private
specifier|final
name|String
name|dagName
decl_stmt|;
specifier|private
specifier|final
name|int
name|dagIdentifier
decl_stmt|;
specifier|private
specifier|final
name|String
name|user
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
specifier|private
name|String
index|[]
name|localDirs
decl_stmt|;
comment|// Map of states for different vertices.
specifier|private
specifier|final
name|Set
argument_list|<
name|QueryFragmentInfo
argument_list|>
name|knownFragments
init|=
name|Collections
operator|.
name|newSetFromMap
argument_list|(
operator|new
name|ConcurrentHashMap
argument_list|<
name|QueryFragmentInfo
argument_list|,
name|Boolean
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|SourceStateProto
argument_list|>
name|sourceStateMap
decl_stmt|;
specifier|private
specifier|final
name|FinishableStateTracker
name|finishableStateTracker
init|=
operator|new
name|FinishableStateTracker
argument_list|()
decl_stmt|;
specifier|public
name|QueryInfo
parameter_list|(
name|QueryIdentifier
name|queryIdentifier
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
name|user
parameter_list|,
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|SourceStateProto
argument_list|>
name|sourceStateMap
parameter_list|,
name|String
index|[]
name|localDirsBase
parameter_list|,
name|FileSystem
name|localFs
parameter_list|)
block|{
name|this
operator|.
name|queryIdentifier
operator|=
name|queryIdentifier
expr_stmt|;
name|this
operator|.
name|appIdString
operator|=
name|appIdString
expr_stmt|;
name|this
operator|.
name|dagName
operator|=
name|dagName
expr_stmt|;
name|this
operator|.
name|dagIdentifier
operator|=
name|dagIdentifier
expr_stmt|;
name|this
operator|.
name|sourceStateMap
operator|=
name|sourceStateMap
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
name|this
operator|.
name|localDirsBase
operator|=
name|localDirsBase
expr_stmt|;
name|this
operator|.
name|localFs
operator|=
name|localFs
expr_stmt|;
block|}
specifier|public
name|QueryIdentifier
name|getQueryIdentifier
parameter_list|()
block|{
return|return
name|queryIdentifier
return|;
block|}
specifier|public
name|String
name|getAppIdString
parameter_list|()
block|{
return|return
name|appIdString
return|;
block|}
specifier|public
name|int
name|getDagIdentifier
parameter_list|()
block|{
return|return
name|dagIdentifier
return|;
block|}
specifier|public
name|String
name|getUser
parameter_list|()
block|{
return|return
name|user
return|;
block|}
specifier|public
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|SourceStateProto
argument_list|>
name|getSourceStateMap
parameter_list|()
block|{
return|return
name|sourceStateMap
return|;
block|}
specifier|public
name|QueryFragmentInfo
name|registerFragment
parameter_list|(
name|String
name|vertexName
parameter_list|,
name|int
name|fragmentNumber
parameter_list|,
name|int
name|attemptNumber
parameter_list|,
name|FragmentSpecProto
name|fragmentSpec
parameter_list|)
block|{
name|QueryFragmentInfo
name|fragmentInfo
init|=
operator|new
name|QueryFragmentInfo
argument_list|(
name|this
argument_list|,
name|vertexName
argument_list|,
name|fragmentNumber
argument_list|,
name|attemptNumber
argument_list|,
name|fragmentSpec
argument_list|)
decl_stmt|;
name|knownFragments
operator|.
name|add
argument_list|(
name|fragmentInfo
argument_list|)
expr_stmt|;
return|return
name|fragmentInfo
return|;
block|}
specifier|public
name|void
name|unregisterFragment
parameter_list|(
name|QueryFragmentInfo
name|fragmentInfo
parameter_list|)
block|{
name|knownFragments
operator|.
name|remove
argument_list|(
name|fragmentInfo
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|QueryFragmentInfo
argument_list|>
name|getRegisteredFragments
parameter_list|()
block|{
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
name|knownFragments
argument_list|)
return|;
block|}
specifier|private
specifier|synchronized
name|void
name|createLocalDirs
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|localDirs
operator|==
literal|null
condition|)
block|{
name|localDirs
operator|=
operator|new
name|String
index|[
name|localDirsBase
operator|.
name|length
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
name|localDirsBase
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|localDirs
index|[
name|i
index|]
operator|=
name|createAppSpecificLocalDir
argument_list|(
name|localDirsBase
index|[
name|i
index|]
argument_list|,
name|appIdString
argument_list|,
name|user
argument_list|,
name|dagIdentifier
argument_list|)
expr_stmt|;
name|localFs
operator|.
name|mkdirs
argument_list|(
operator|new
name|Path
argument_list|(
name|localDirs
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Get, and create if required, local-dirs for a query    * @return    * @throws IOException    */
specifier|public
specifier|synchronized
name|String
index|[]
name|getLocalDirs
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|localDirs
operator|==
literal|null
condition|)
block|{
name|createLocalDirs
argument_list|()
expr_stmt|;
block|}
return|return
name|localDirs
return|;
block|}
specifier|public
specifier|synchronized
name|String
index|[]
name|getLocalDirsNoCreate
parameter_list|()
block|{
return|return
name|this
operator|.
name|localDirs
return|;
block|}
specifier|private
specifier|static
name|String
name|createAppSpecificLocalDir
parameter_list|(
name|String
name|baseDir
parameter_list|,
name|String
name|applicationIdString
parameter_list|,
name|String
name|user
parameter_list|,
name|int
name|dagIdentifier
parameter_list|)
block|{
comment|// TODO This is broken for secure clusters. The app will not have permission to create these directories.
comment|// May work via Slider - since the directory would already exist. Otherwise may need a custom shuffle handler.
comment|// TODO This should be the process user - and not the user on behalf of whom the query is being submitted.
return|return
name|baseDir
operator|+
name|File
operator|.
name|separator
operator|+
literal|"usercache"
operator|+
name|File
operator|.
name|separator
operator|+
name|user
operator|+
name|File
operator|.
name|separator
operator|+
literal|"appcache"
operator|+
name|File
operator|.
name|separator
operator|+
name|applicationIdString
operator|+
name|File
operator|.
name|separator
operator|+
name|dagIdentifier
return|;
block|}
comment|/**    *    * @param handler    * @param sources    * @param fragmentInfo    * @param lastFinishableState    * @return true if the current state is the same as the lastFinishableState. false if the state has already changed.    */
name|boolean
name|registerForFinishableStateUpdates
parameter_list|(
name|FinishableStateUpdateHandler
name|handler
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|sources
parameter_list|,
name|QueryFragmentInfo
name|fragmentInfo
parameter_list|,
name|boolean
name|lastFinishableState
parameter_list|)
block|{
return|return
name|finishableStateTracker
operator|.
name|registerForUpdates
argument_list|(
name|handler
argument_list|,
name|sources
argument_list|,
name|fragmentInfo
argument_list|,
name|lastFinishableState
argument_list|)
return|;
block|}
name|void
name|unregisterFinishableStateUpdate
parameter_list|(
name|FinishableStateUpdateHandler
name|handler
parameter_list|)
block|{
name|finishableStateTracker
operator|.
name|unregisterForUpdates
argument_list|(
name|handler
argument_list|)
expr_stmt|;
block|}
name|void
name|sourceStateUpdated
parameter_list|(
name|String
name|sourceName
parameter_list|)
block|{
name|finishableStateTracker
operator|.
name|sourceStateUpdated
argument_list|(
name|sourceName
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|FinishableStateTracker
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|FinishableStateUpdateHandler
argument_list|,
name|EntityInfo
argument_list|>
name|trackedEntities
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Multimap
argument_list|<
name|String
argument_list|,
name|EntityInfo
argument_list|>
name|sourceToEntity
init|=
name|HashMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
specifier|synchronized
name|boolean
name|registerForUpdates
parameter_list|(
name|FinishableStateUpdateHandler
name|handler
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|sources
parameter_list|,
name|QueryFragmentInfo
name|fragmentInfo
parameter_list|,
name|boolean
name|lastFinishableState
parameter_list|)
block|{
name|EntityInfo
name|entityInfo
init|=
operator|new
name|EntityInfo
argument_list|(
name|handler
argument_list|,
name|sources
argument_list|,
name|fragmentInfo
argument_list|,
name|lastFinishableState
argument_list|)
decl_stmt|;
if|if
condition|(
name|trackedEntities
operator|.
name|put
argument_list|(
name|handler
argument_list|,
name|entityInfo
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Only a single registration allowed per entity. Duplicate for "
operator|+
name|handler
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
for|for
control|(
name|String
name|source
range|:
name|sources
control|)
block|{
name|sourceToEntity
operator|.
name|put
argument_list|(
name|source
argument_list|,
name|entityInfo
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lastFinishableState
operator|!=
name|fragmentInfo
operator|.
name|canFinish
argument_list|()
condition|)
block|{
name|entityInfo
operator|.
name|setLastFinishableState
argument_list|(
name|fragmentInfo
operator|.
name|canFinish
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
else|else
block|{
return|return
literal|true
return|;
block|}
block|}
specifier|synchronized
name|void
name|unregisterForUpdates
parameter_list|(
name|FinishableStateUpdateHandler
name|handler
parameter_list|)
block|{
name|EntityInfo
name|info
init|=
name|trackedEntities
operator|.
name|remove
argument_list|(
name|handler
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|info
operator|!=
literal|null
argument_list|,
literal|"Cannot invoke unregister on an entity which has not been registered"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|source
range|:
name|info
operator|.
name|getSources
argument_list|()
control|)
block|{
name|sourceToEntity
operator|.
name|remove
argument_list|(
name|source
argument_list|,
name|info
argument_list|)
expr_stmt|;
block|}
block|}
specifier|synchronized
name|void
name|sourceStateUpdated
parameter_list|(
name|String
name|sourceName
parameter_list|)
block|{
name|Collection
argument_list|<
name|EntityInfo
argument_list|>
name|interestedEntityInfos
init|=
name|sourceToEntity
operator|.
name|get
argument_list|(
name|sourceName
argument_list|)
decl_stmt|;
if|if
condition|(
name|interestedEntityInfos
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|EntityInfo
name|entityInfo
range|:
name|interestedEntityInfos
control|)
block|{
name|boolean
name|newFinishState
init|=
name|entityInfo
operator|.
name|getFragmentInfo
argument_list|()
operator|.
name|canFinish
argument_list|()
decl_stmt|;
if|if
condition|(
name|newFinishState
operator|!=
name|entityInfo
operator|.
name|getLastFinishableState
argument_list|()
condition|)
block|{
comment|// State changed. Callback
name|entityInfo
operator|.
name|setLastFinishableState
argument_list|(
name|newFinishState
argument_list|)
expr_stmt|;
name|entityInfo
operator|.
name|getHandler
argument_list|()
operator|.
name|finishableStateUpdated
argument_list|(
name|newFinishState
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|EntityInfo
block|{
specifier|final
name|FinishableStateUpdateHandler
name|handler
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|sources
decl_stmt|;
specifier|final
name|QueryFragmentInfo
name|fragmentInfo
decl_stmt|;
name|boolean
name|lastFinishableState
decl_stmt|;
specifier|public
name|EntityInfo
parameter_list|(
name|FinishableStateUpdateHandler
name|handler
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|sources
parameter_list|,
name|QueryFragmentInfo
name|fragmentInfo
parameter_list|,
name|boolean
name|lastFinishableState
parameter_list|)
block|{
name|this
operator|.
name|handler
operator|=
name|handler
expr_stmt|;
name|this
operator|.
name|sources
operator|=
name|sources
expr_stmt|;
name|this
operator|.
name|fragmentInfo
operator|=
name|fragmentInfo
expr_stmt|;
name|this
operator|.
name|lastFinishableState
operator|=
name|lastFinishableState
expr_stmt|;
block|}
specifier|public
name|FinishableStateUpdateHandler
name|getHandler
parameter_list|()
block|{
return|return
name|handler
return|;
block|}
specifier|public
name|QueryFragmentInfo
name|getFragmentInfo
parameter_list|()
block|{
return|return
name|fragmentInfo
return|;
block|}
specifier|public
name|boolean
name|getLastFinishableState
parameter_list|()
block|{
return|return
name|lastFinishableState
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getSources
parameter_list|()
block|{
return|return
name|sources
return|;
block|}
specifier|public
name|void
name|setLastFinishableState
parameter_list|(
name|boolean
name|lastFinishableState
parameter_list|)
block|{
name|this
operator|.
name|lastFinishableState
operator|=
name|lastFinishableState
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

