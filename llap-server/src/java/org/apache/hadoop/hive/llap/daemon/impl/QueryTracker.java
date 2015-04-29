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
name|shufflehandler
operator|.
name|ShuffleHandler
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
name|concurrent
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_comment
comment|/**  * Tracks queries running within a daemon  */
end_comment

begin_class
specifier|public
class|class
name|QueryTracker
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
name|queryFileCleaner
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|queryFileCleaner
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|void
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
parameter_list|)
throws|throws
name|IOException
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
block|}
name|String
index|[]
name|getLocalDirs
parameter_list|(
name|String
name|queryId
parameter_list|,
name|String
name|dagName
parameter_list|,
name|String
name|user
parameter_list|)
throws|throws
name|IOException
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
return|return
name|queryInfo
operator|.
name|getLocalDirs
argument_list|()
return|;
block|}
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
name|dagIdentifier
argument_list|)
expr_stmt|;
block|}
block|}
comment|// TODO HIVE-10535 Cleanup map join cache
block|}
name|void
name|shutdown
parameter_list|()
block|{
name|queryFileCleaner
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
specifier|private
class|class
name|QueryInfo
block|{
specifier|private
specifier|final
name|String
name|queryId
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
name|String
index|[]
name|localDirs
decl_stmt|;
specifier|public
name|QueryInfo
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
name|user
parameter_list|)
block|{
name|this
operator|.
name|queryId
operator|=
name|queryId
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
name|user
operator|=
name|user
expr_stmt|;
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
specifier|private
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
specifier|private
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
block|}
end_class

end_unit

