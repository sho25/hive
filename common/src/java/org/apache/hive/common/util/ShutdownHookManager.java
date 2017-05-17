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
name|hive
operator|.
name|common
operator|.
name|util
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
name|HashSet
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * This is just a wrapper around hadoop's ShutdownHookManager but also manages delete on exit hook for temp files.  */
end_comment

begin_class
specifier|public
class|class
name|ShutdownHookManager
block|{
specifier|private
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|ShutdownHookManager
name|MGR
init|=
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|ShutdownHookManager
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|DeleteOnExitHook
name|DELETE_ON_EXIT_HOOK
init|=
operator|new
name|DeleteOnExitHook
argument_list|()
decl_stmt|;
static|static
block|{
name|MGR
operator|.
name|addShutdownHook
argument_list|(
name|DELETE_ON_EXIT_HOOK
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Adds shutdown hook with default priority (10)    * @param shutdownHook - shutdown hook    */
specifier|public
specifier|static
name|void
name|addShutdownHook
parameter_list|(
name|Runnable
name|shutdownHook
parameter_list|)
block|{
name|addShutdownHook
argument_list|(
name|shutdownHook
argument_list|,
name|FileSystem
operator|.
name|SHUTDOWN_HOOK_PRIORITY
argument_list|)
expr_stmt|;
block|}
comment|/**    * Adds a shutdownHook with a priority, the higher the priority    * the earlier will run. ShutdownHooks with same priority run    * in a non-deterministic order.    *    * @param shutdownHook shutdownHook<code>Runnable</code>    * @param priority priority of the shutdownHook.    */
specifier|public
specifier|static
name|void
name|addShutdownHook
parameter_list|(
name|Runnable
name|shutdownHook
parameter_list|,
name|int
name|priority
parameter_list|)
block|{
if|if
condition|(
name|priority
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Priority should be greater than or equal to zero"
argument_list|)
throw|;
block|}
name|MGR
operator|.
name|addShutdownHook
argument_list|(
name|shutdownHook
argument_list|,
name|priority
argument_list|)
expr_stmt|;
block|}
comment|/**    * Indicates if shutdown is in progress or not.    *    * @return TRUE if the shutdown is in progress, otherwise FALSE.    */
specifier|public
specifier|static
name|boolean
name|isShutdownInProgress
parameter_list|()
block|{
return|return
name|MGR
operator|.
name|isShutdownInProgress
argument_list|()
return|;
block|}
comment|/**    * Removes a shutdownHook.    *    * @param shutdownHook shutdownHook to remove.    * @return TRUE if the shutdownHook was registered and removed,    * FALSE otherwise (including when shutdownHook == null)    */
specifier|public
specifier|static
name|boolean
name|removeShutdownHook
parameter_list|(
name|Runnable
name|shutdownHook
parameter_list|)
block|{
if|if
condition|(
name|shutdownHook
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|MGR
operator|.
name|removeShutdownHook
argument_list|(
name|shutdownHook
argument_list|)
return|;
block|}
comment|/**    * register file to delete-on-exit hook    *    * {@link org.apache.hadoop.hive.common.FileUtils#createTempFile}    */
specifier|public
specifier|static
name|void
name|deleteOnExit
parameter_list|(
name|File
name|file
parameter_list|)
block|{
if|if
condition|(
name|MGR
operator|.
name|isShutdownInProgress
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Shutdown in progress, cannot add a deleteOnExit"
argument_list|)
throw|;
block|}
name|DELETE_ON_EXIT_HOOK
operator|.
name|deleteTargets
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
comment|/**    * deregister file from delete-on-exit hook    */
specifier|public
specifier|static
name|void
name|cancelDeleteOnExit
parameter_list|(
name|File
name|file
parameter_list|)
block|{
if|if
condition|(
name|MGR
operator|.
name|isShutdownInProgress
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Shutdown in progress, cannot cancel a deleteOnExit"
argument_list|)
throw|;
block|}
name|DELETE_ON_EXIT_HOOK
operator|.
name|deleteTargets
operator|.
name|remove
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|static
name|boolean
name|isRegisteredToDeleteOnExit
parameter_list|(
name|File
name|file
parameter_list|)
block|{
return|return
name|DELETE_ON_EXIT_HOOK
operator|.
name|deleteTargets
operator|.
name|contains
argument_list|(
name|file
argument_list|)
return|;
block|}
specifier|private
specifier|static
class|class
name|DeleteOnExitHook
implements|implements
name|Runnable
block|{
specifier|private
specifier|final
name|Set
argument_list|<
name|File
argument_list|>
name|deleteTargets
init|=
name|Collections
operator|.
name|synchronizedSet
argument_list|(
operator|new
name|HashSet
argument_list|<
name|File
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
for|for
control|(
name|File
name|deleteTarget
range|:
name|deleteTargets
control|)
block|{
name|deleteTarget
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
name|deleteTargets
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

