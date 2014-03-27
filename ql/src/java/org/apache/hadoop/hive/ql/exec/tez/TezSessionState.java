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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|tez
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|UUID
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|digest
operator|.
name|DigestUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|FilenameUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|hdfs
operator|.
name|DistributedFileSystem
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|LocalResource
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|client
operator|.
name|AMConfiguration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|client
operator|.
name|PreWarmContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|client
operator|.
name|TezSession
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|client
operator|.
name|TezSessionConfiguration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|SessionNotRunning
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|TezConfiguration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|TezException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|mapreduce
operator|.
name|hadoop
operator|.
name|MRHelpers
import|;
end_import

begin_comment
comment|/**  * Holds session state related to Tez  */
end_comment

begin_class
specifier|public
class|class
name|TezSessionState
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TezSessionState
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEZ_DIR
init|=
literal|"_tez_session_dir"
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|Path
name|tezScratchDir
decl_stmt|;
specifier|private
name|LocalResource
name|appJarLr
decl_stmt|;
specifier|private
name|TezSession
name|session
decl_stmt|;
specifier|private
name|String
name|sessionId
decl_stmt|;
specifier|private
name|DagUtils
name|utils
decl_stmt|;
specifier|private
name|String
name|queueName
decl_stmt|;
specifier|private
name|boolean
name|defaultQueue
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|List
argument_list|<
name|TezSessionState
argument_list|>
name|openSessions
init|=
name|Collections
operator|.
name|synchronizedList
argument_list|(
operator|new
name|LinkedList
argument_list|<
name|TezSessionState
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * Constructor. We do not automatically connect, because we only want to    * load tez classes when the user has tez installed.    */
specifier|public
name|TezSessionState
parameter_list|(
name|DagUtils
name|utils
parameter_list|)
block|{
name|this
operator|.
name|utils
operator|=
name|utils
expr_stmt|;
block|}
comment|/**    * Constructor. We do not automatically connect, because we only want to    * load tez classes when the user has tez installed.    */
specifier|public
name|TezSessionState
parameter_list|()
block|{
name|this
argument_list|(
name|DagUtils
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns whether a session has been established    */
specifier|public
name|boolean
name|isOpen
parameter_list|()
block|{
return|return
name|session
operator|!=
literal|null
return|;
block|}
comment|/**    * Get all open sessions. Only used to clean up at shutdown.    * @return List<TezSessionState>    */
specifier|public
specifier|static
name|List
argument_list|<
name|TezSessionState
argument_list|>
name|getOpenSessions
parameter_list|()
block|{
return|return
name|openSessions
return|;
block|}
specifier|public
specifier|static
name|String
name|makeSessionId
parameter_list|()
block|{
return|return
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Creates a tez session. A session is tied to either a cli/hs2 session. You can    * submit multiple DAGs against a session (as long as they are executed serially).    * @throws IOException    * @throws URISyntaxException    * @throws LoginException    * @throws TezException    */
specifier|public
name|void
name|open
parameter_list|(
name|String
name|sessionId
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|LoginException
throws|,
name|URISyntaxException
throws|,
name|TezException
block|{
name|this
operator|.
name|sessionId
operator|=
name|sessionId
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
comment|// create the tez tmp dir
name|tezScratchDir
operator|=
name|createTezDir
argument_list|(
name|sessionId
argument_list|)
expr_stmt|;
comment|// generate basic tez config
name|TezConfiguration
name|tezConfig
init|=
operator|new
name|TezConfiguration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|tezConfig
operator|.
name|set
argument_list|(
name|TezConfiguration
operator|.
name|TEZ_AM_STAGING_DIR
argument_list|,
name|tezScratchDir
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// unless already installed on all the cluster nodes, we'll have to
comment|// localize hive-exec.jar as well.
name|appJarLr
operator|=
name|createJarLocalResource
argument_list|(
name|utils
operator|.
name|getExecJarPathLocal
argument_list|()
argument_list|)
expr_stmt|;
comment|// configuration for the application master
name|Map
argument_list|<
name|String
argument_list|,
name|LocalResource
argument_list|>
name|commonLocalResources
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|LocalResource
argument_list|>
argument_list|()
decl_stmt|;
name|commonLocalResources
operator|.
name|put
argument_list|(
name|utils
operator|.
name|getBaseName
argument_list|(
name|appJarLr
argument_list|)
argument_list|,
name|appJarLr
argument_list|)
expr_stmt|;
comment|// Create environment for AM.
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|amEnv
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|MRHelpers
operator|.
name|updateEnvironmentForMRAM
argument_list|(
name|conf
argument_list|,
name|amEnv
argument_list|)
expr_stmt|;
name|AMConfiguration
name|amConfig
init|=
operator|new
name|AMConfiguration
argument_list|(
name|amEnv
argument_list|,
name|commonLocalResources
argument_list|,
name|tezConfig
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// configuration for the session
name|TezSessionConfiguration
name|sessionConfig
init|=
operator|new
name|TezSessionConfiguration
argument_list|(
name|amConfig
argument_list|,
name|tezConfig
argument_list|)
decl_stmt|;
comment|// and finally we're ready to create and start the session
name|session
operator|=
operator|new
name|TezSession
argument_list|(
literal|"HIVE-"
operator|+
name|sessionId
argument_list|,
name|sessionConfig
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Opening new Tez Session (id: "
operator|+
name|sessionId
operator|+
literal|", scratch dir: "
operator|+
name|tezScratchDir
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|session
operator|.
name|start
argument_list|()
expr_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_PREWARM_ENABLED
argument_list|)
condition|)
block|{
name|int
name|n
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_PREWARM_NUM_CONTAINERS
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Prewarming "
operator|+
name|n
operator|+
literal|" containers  (id: "
operator|+
name|sessionId
operator|+
literal|", scratch dir: "
operator|+
name|tezScratchDir
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|PreWarmContext
name|context
init|=
name|utils
operator|.
name|createPreWarmContext
argument_list|(
name|sessionConfig
argument_list|,
name|n
argument_list|,
name|commonLocalResources
argument_list|)
decl_stmt|;
try|try
block|{
name|session
operator|.
name|preWarm
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
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
literal|"Hive Prewarm threw an exception "
argument_list|,
name|ie
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// In case we need to run some MR jobs, we'll run them under tez MR emulation. The session
comment|// id is used for tez to reuse the current session rather than start a new one.
name|conf
operator|.
name|set
argument_list|(
literal|"mapreduce.framework.name"
argument_list|,
literal|"yarn-tez"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"mapreduce.tez.session.tokill-application-id"
argument_list|,
name|session
operator|.
name|getApplicationId
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|openSessions
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
comment|/**    * Close a tez session. Will cleanup any tez/am related resources. After closing a session    * no further DAGs can be executed against it.    * @param keepTmpDir whether or not to remove the scratch dir at the same time.    * @throws IOException    * @throws TezException    */
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|keepTmpDir
parameter_list|)
throws|throws
name|TezException
throws|,
name|IOException
block|{
if|if
condition|(
operator|!
name|isOpen
argument_list|()
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Closing Tez Session"
argument_list|)
expr_stmt|;
try|try
block|{
name|session
operator|.
name|stop
argument_list|()
expr_stmt|;
name|openSessions
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SessionNotRunning
name|nr
parameter_list|)
block|{
comment|// ignore
block|}
if|if
condition|(
operator|!
name|keepTmpDir
condition|)
block|{
name|FileSystem
name|fs
init|=
name|tezScratchDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|tezScratchDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|session
operator|=
literal|null
expr_stmt|;
name|tezScratchDir
operator|=
literal|null
expr_stmt|;
name|conf
operator|=
literal|null
expr_stmt|;
name|appJarLr
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|String
name|getSessionId
parameter_list|()
block|{
return|return
name|sessionId
return|;
block|}
specifier|public
name|TezSession
name|getSession
parameter_list|()
block|{
return|return
name|session
return|;
block|}
specifier|public
name|Path
name|getTezScratchDir
parameter_list|()
block|{
return|return
name|tezScratchDir
return|;
block|}
specifier|public
name|LocalResource
name|getAppJarLr
parameter_list|()
block|{
return|return
name|appJarLr
return|;
block|}
comment|/**    * createTezDir creates a temporary directory in the scratchDir folder to    * be used with Tez. Assumes scratchDir exists.    */
specifier|private
name|Path
name|createTezDir
parameter_list|(
name|String
name|sessionId
parameter_list|)
throws|throws
name|IOException
block|{
comment|// tez needs its own scratch dir (per session)
name|Path
name|tezDir
init|=
operator|new
name|Path
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SCRATCHDIR
argument_list|)
argument_list|,
name|TEZ_DIR
argument_list|)
decl_stmt|;
name|tezDir
operator|=
operator|new
name|Path
argument_list|(
name|tezDir
argument_list|,
name|sessionId
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|tezDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|tezDir
argument_list|)
expr_stmt|;
comment|// don't keep the directory around on non-clean exit
name|fs
operator|.
name|deleteOnExit
argument_list|(
name|tezDir
argument_list|)
expr_stmt|;
return|return
name|tezDir
return|;
block|}
comment|/**    * Returns a local resource representing a jar.    * This resource will be used to execute the plan on the cluster.    * @param localJarPath Local path to the jar to be localized.    * @return LocalResource corresponding to the localized hive exec resource.    * @throws IOException when any file system related call fails.    * @throws LoginException when we are unable to determine the user.    * @throws URISyntaxException when current jar location cannot be determined.    */
specifier|private
name|LocalResource
name|createJarLocalResource
parameter_list|(
name|String
name|localJarPath
parameter_list|)
throws|throws
name|IOException
throws|,
name|LoginException
throws|,
name|IllegalArgumentException
throws|,
name|FileNotFoundException
block|{
name|Path
name|destDirPath
init|=
literal|null
decl_stmt|;
name|FileSystem
name|destFs
init|=
literal|null
decl_stmt|;
name|FileStatus
name|destDirStatus
init|=
literal|null
decl_stmt|;
block|{
name|String
name|hiveJarDir
init|=
name|utils
operator|.
name|getHiveJarDirectory
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|hiveJarDir
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Hive jar directory is "
operator|+
name|hiveJarDir
argument_list|)
expr_stmt|;
comment|// check if it is a valid directory in HDFS
name|destDirPath
operator|=
operator|new
name|Path
argument_list|(
name|hiveJarDir
argument_list|)
expr_stmt|;
name|destFs
operator|=
name|destDirPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|destDirStatus
operator|=
name|validateTargetDir
argument_list|(
name|destDirPath
argument_list|,
name|destFs
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*      * Specified location does not exist or is not a directory.      * Try to push the jar to the hdfs location pointed by config variable HIVE_INSTALL_DIR.      * Path will be HIVE_INSTALL_DIR/{username}/.hiveJars/      * This will probably never ever happen.      */
if|if
condition|(
name|destDirStatus
operator|==
literal|null
operator|||
operator|!
name|destDirStatus
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|destDirPath
operator|=
name|utils
operator|.
name|getDefaultDestDir
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Jar dir is null/directory doesn't exist. Choosing HIVE_INSTALL_DIR - "
operator|+
name|destDirPath
argument_list|)
expr_stmt|;
name|destFs
operator|=
name|destDirPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|destDirStatus
operator|=
name|validateTargetDir
argument_list|(
name|destDirPath
argument_list|,
name|destFs
argument_list|)
expr_stmt|;
block|}
comment|// we couldn't find any valid locations. Throw exception
if|if
condition|(
name|destDirStatus
operator|==
literal|null
operator|||
operator|!
name|destDirStatus
operator|.
name|isDir
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ErrorMsg
operator|.
name|NO_VALID_LOCATIONS
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
name|Path
name|localFile
init|=
operator|new
name|Path
argument_list|(
name|localJarPath
argument_list|)
decl_stmt|;
name|String
name|sha
init|=
name|getSha
argument_list|(
name|localFile
argument_list|)
decl_stmt|;
name|String
name|destFileName
init|=
name|localFile
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// Now, try to find the file based on SHA and name. Currently we require exact name match.
comment|// We could also allow cutting off versions and other stuff provided that SHA matches...
name|destFileName
operator|=
name|FilenameUtils
operator|.
name|removeExtension
argument_list|(
name|destFileName
argument_list|)
operator|+
literal|"-"
operator|+
name|sha
operator|+
name|FilenameUtils
operator|.
name|EXTENSION_SEPARATOR
operator|+
name|FilenameUtils
operator|.
name|getExtension
argument_list|(
name|destFileName
argument_list|)
expr_stmt|;
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
literal|"The destination file name for ["
operator|+
name|localJarPath
operator|+
literal|"] is "
operator|+
name|destFileName
argument_list|)
expr_stmt|;
block|}
comment|// TODO: if this method is ever called on more than one jar, getting the dir and the
comment|//       list need to be refactored out to be done only once.
name|Path
name|jarPath
init|=
literal|null
decl_stmt|;
name|FileStatus
index|[]
name|listFileStatus
init|=
name|destFs
operator|.
name|listStatus
argument_list|(
name|destDirPath
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|fstatus
range|:
name|listFileStatus
control|)
block|{
name|String
name|jarName
init|=
name|utils
operator|.
name|getResourceBaseName
argument_list|(
name|fstatus
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
comment|// ...
if|if
condition|(
name|jarName
operator|.
name|equals
argument_list|(
name|destFileName
argument_list|)
condition|)
block|{
comment|// We have found the jar we need.
name|jarPath
operator|=
name|fstatus
operator|.
name|getPath
argument_list|()
expr_stmt|;
return|return
name|utils
operator|.
name|localizeResource
argument_list|(
literal|null
argument_list|,
name|jarPath
argument_list|,
name|conf
argument_list|)
return|;
block|}
block|}
comment|// Jar wasn't in the directory, copy the one in current use.
assert|assert
name|jarPath
operator|==
literal|null
assert|;
name|Path
name|destFile
init|=
operator|new
name|Path
argument_list|(
name|destDirPath
operator|.
name|toString
argument_list|()
operator|+
literal|"/"
operator|+
name|destFileName
argument_list|)
decl_stmt|;
return|return
name|utils
operator|.
name|localizeResource
argument_list|(
name|localFile
argument_list|,
name|destFile
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|private
name|FileStatus
name|validateTargetDir
parameter_list|(
name|Path
name|hiveJarDirPath
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
operator|(
name|fs
operator|instanceof
name|DistributedFileSystem
operator|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_HDFS_URI
operator|.
name|format
argument_list|(
name|hiveJarDirPath
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
try|try
block|{
return|return
name|fs
operator|.
name|getFileStatus
argument_list|(
name|hiveJarDirPath
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|fe
parameter_list|)
block|{
comment|// do nothing
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|String
name|getSha
parameter_list|(
name|Path
name|localFile
parameter_list|)
throws|throws
name|IOException
throws|,
name|IllegalArgumentException
block|{
name|InputStream
name|is
init|=
literal|null
decl_stmt|;
try|try
block|{
name|FileSystem
name|localFs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|is
operator|=
name|localFs
operator|.
name|open
argument_list|(
name|localFile
argument_list|)
expr_stmt|;
return|return
name|DigestUtils
operator|.
name|sha256Hex
argument_list|(
name|is
argument_list|)
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|is
operator|!=
literal|null
condition|)
block|{
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|setQueueName
parameter_list|(
name|String
name|queueName
parameter_list|)
block|{
name|this
operator|.
name|queueName
operator|=
name|queueName
expr_stmt|;
block|}
specifier|public
name|String
name|getQueueName
parameter_list|()
block|{
return|return
name|queueName
return|;
block|}
specifier|public
name|void
name|setDefault
parameter_list|()
block|{
name|defaultQueue
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|isDefault
parameter_list|()
block|{
return|return
name|defaultQueue
return|;
block|}
specifier|public
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
block|}
end_class

end_unit

