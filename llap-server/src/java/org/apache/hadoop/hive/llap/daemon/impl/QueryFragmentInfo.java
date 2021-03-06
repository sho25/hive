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
name|IOException
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
name|IOSpecProto
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
name|SignableVertexSpec
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
name|tezplugins
operator|.
name|LlapTezUtils
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

begin_class
specifier|public
class|class
name|QueryFragmentInfo
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
name|QueryFragmentInfo
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|QueryInfo
name|queryInfo
decl_stmt|;
specifier|private
specifier|final
name|String
name|vertexName
decl_stmt|;
specifier|private
specifier|final
name|int
name|fragmentNumber
decl_stmt|;
specifier|private
specifier|final
name|int
name|attemptNumber
decl_stmt|;
specifier|private
specifier|final
name|SignableVertexSpec
name|vertexSpec
decl_stmt|;
specifier|private
specifier|final
name|String
name|fragmentIdString
decl_stmt|;
specifier|private
name|boolean
name|canFinishForPriority
decl_stmt|;
specifier|public
name|QueryFragmentInfo
parameter_list|(
name|QueryInfo
name|queryInfo
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
name|SignableVertexSpec
name|vertexSpec
parameter_list|,
name|String
name|fragmentIdString
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|queryInfo
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|vertexName
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|vertexSpec
argument_list|)
expr_stmt|;
name|this
operator|.
name|queryInfo
operator|=
name|queryInfo
expr_stmt|;
name|this
operator|.
name|vertexName
operator|=
name|vertexName
expr_stmt|;
name|this
operator|.
name|fragmentNumber
operator|=
name|fragmentNumber
expr_stmt|;
name|this
operator|.
name|attemptNumber
operator|=
name|attemptNumber
expr_stmt|;
name|this
operator|.
name|vertexSpec
operator|=
name|vertexSpec
expr_stmt|;
name|this
operator|.
name|fragmentIdString
operator|=
name|fragmentIdString
expr_stmt|;
name|this
operator|.
name|canFinishForPriority
operator|=
literal|false
expr_stmt|;
comment|// Updated when we add this to the queue.
block|}
comment|// Only meant for use by the QueryTracker
name|QueryInfo
name|getQueryInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|queryInfo
return|;
block|}
specifier|public
name|SignableVertexSpec
name|getVertexSpec
parameter_list|()
block|{
return|return
name|vertexSpec
return|;
block|}
specifier|public
name|String
name|getVertexName
parameter_list|()
block|{
return|return
name|vertexName
return|;
block|}
specifier|public
name|int
name|getFragmentNumber
parameter_list|()
block|{
return|return
name|fragmentNumber
return|;
block|}
specifier|public
name|int
name|getAttemptNumber
parameter_list|()
block|{
return|return
name|attemptNumber
return|;
block|}
specifier|public
name|String
name|getFragmentIdentifierString
parameter_list|()
block|{
return|return
name|fragmentIdString
return|;
block|}
comment|/**    * Unlike canFinish, this CANNOT be derived dynamically; a change without a reinsert will    * cause the queue order to become incorrect.    */
specifier|public
name|boolean
name|canFinishForPriority
parameter_list|()
block|{
return|return
name|canFinishForPriority
return|;
block|}
comment|/**    * This MUST be called when the fragment is NOT in wait queue.    */
specifier|public
name|void
name|setCanFinishForPriority
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|canFinishForPriority
operator|=
name|value
expr_stmt|;
block|}
comment|/**    * Check whether a task can run to completion or may end up blocking on it's sources.    * This currently happens via looking up source state.    * TODO: Eventually, this should lookup the Hive Processor to figure out whether    * it's reached a state where it can finish - especially in cases of failures    * after data has been fetched.    *    * @return true if the task can finish, false otherwise    */
specifier|public
specifier|static
name|boolean
name|canFinish
parameter_list|(
name|QueryFragmentInfo
name|fragment
parameter_list|)
block|{
return|return
name|fragment
operator|.
name|canFinish
argument_list|()
return|;
block|}
comment|// Hide this so it doesn't look like a simple property.
specifier|private
name|boolean
name|canFinish
parameter_list|()
block|{
name|List
argument_list|<
name|IOSpecProto
argument_list|>
name|inputSpecList
init|=
name|vertexSpec
operator|.
name|getInputSpecsList
argument_list|()
decl_stmt|;
name|boolean
name|canFinish
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|inputSpecList
operator|!=
literal|null
operator|&&
operator|!
name|inputSpecList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|IOSpecProto
name|inputSpec
range|:
name|inputSpecList
control|)
block|{
if|if
condition|(
name|LlapTezUtils
operator|.
name|isSourceOfInterest
argument_list|(
name|inputSpec
operator|.
name|getIoDescriptor
argument_list|()
operator|.
name|getClassName
argument_list|()
argument_list|)
condition|)
block|{
comment|// Lookup the state in the map.
name|LlapDaemonProtocolProtos
operator|.
name|SourceStateProto
name|state
init|=
name|queryInfo
operator|.
name|getSourceStateMap
argument_list|()
operator|.
name|get
argument_list|(
name|inputSpec
operator|.
name|getConnectedVertexName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|state
operator|!=
literal|null
operator|&&
name|state
operator|==
name|LlapDaemonProtocolProtos
operator|.
name|SourceStateProto
operator|.
name|S_SUCCEEDED
condition|)
block|{
continue|continue;
block|}
else|else
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cannot finish due to source: "
operator|+
name|inputSpec
operator|.
name|getConnectedVertexName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|canFinish
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
return|return
name|canFinish
return|;
block|}
comment|/**    * Get, and create if required, local-dirs for a fragment    * @return    * @throws IOException    */
specifier|public
name|String
index|[]
name|getLocalDirs
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|queryInfo
operator|.
name|getLocalDirs
argument_list|()
return|;
block|}
comment|/**    *    * @param handler    * @param lastFinishableState    * @return true if the current state is the same as the lastFinishableState. false if the state has already changed.    */
specifier|public
name|boolean
name|registerForFinishableStateUpdates
parameter_list|(
name|FinishableStateUpdateHandler
name|handler
parameter_list|,
name|boolean
name|lastFinishableState
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|sourcesOfInterest
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|IOSpecProto
argument_list|>
name|inputSpecList
init|=
name|vertexSpec
operator|.
name|getInputSpecsList
argument_list|()
decl_stmt|;
if|if
condition|(
name|inputSpecList
operator|!=
literal|null
operator|&&
operator|!
name|inputSpecList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|IOSpecProto
name|inputSpec
range|:
name|inputSpecList
control|)
block|{
if|if
condition|(
name|LlapTezUtils
operator|.
name|isSourceOfInterest
argument_list|(
name|inputSpec
operator|.
name|getIoDescriptor
argument_list|()
operator|.
name|getClassName
argument_list|()
argument_list|)
condition|)
block|{
name|sourcesOfInterest
operator|.
name|add
argument_list|(
name|inputSpec
operator|.
name|getConnectedVertexName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|queryInfo
operator|.
name|registerForFinishableStateUpdates
argument_list|(
name|handler
argument_list|,
name|sourcesOfInterest
argument_list|,
name|this
argument_list|,
name|lastFinishableState
argument_list|)
return|;
block|}
specifier|public
name|void
name|unregisterForFinishableStateUpdates
parameter_list|(
name|FinishableStateUpdateHandler
name|handler
parameter_list|)
block|{
name|queryInfo
operator|.
name|unregisterFinishableStateUpdate
argument_list|(
name|handler
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|QueryFragmentInfo
name|that
init|=
operator|(
name|QueryFragmentInfo
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|fragmentNumber
operator|!=
name|that
operator|.
name|fragmentNumber
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|attemptNumber
operator|!=
name|that
operator|.
name|attemptNumber
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|vertexName
operator|.
name|equals
argument_list|(
name|that
operator|.
name|vertexName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|vertexName
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|fragmentNumber
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|attemptNumber
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

