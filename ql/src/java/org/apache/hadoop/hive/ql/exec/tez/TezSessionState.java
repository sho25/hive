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
name|HashMap
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
comment|/**    * Constructor. We do not automatically connect, because we only want to    * load tez classes when the user has tez installed.    */
specifier|public
name|void
name|TezSessionContext
parameter_list|()
block|{   }
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
name|createHiveExecLocalResource
argument_list|()
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
name|DagUtils
operator|.
name|getBaseName
argument_list|(
name|appJarLr
argument_list|)
argument_list|,
name|appJarLr
argument_list|)
expr_stmt|;
name|AMConfiguration
name|amConfig
init|=
operator|new
name|AMConfiguration
argument_list|(
literal|null
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
block|}
comment|/**    * Close a tez session. Will cleanup any tez/am related resources. After closing a session    * no further DAGs can be executed against it.    * @throws IOException    * @throws TezException    */
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
comment|/**    * Returns a local resource representing the hive-exec jar. This resource will    * be used to execute the plan on the cluster.    * @param conf    * @return LocalResource corresponding to the localized hive exec resource.    * @throws IOException when any file system related call fails.    * @throws LoginException when we are unable to determine the user.    * @throws URISyntaxException when current jar location cannot be determined.    */
specifier|private
name|LocalResource
name|createHiveExecLocalResource
parameter_list|()
throws|throws
name|IOException
throws|,
name|LoginException
throws|,
name|URISyntaxException
block|{
name|String
name|hiveJarDir
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_JAR_DIRECTORY
argument_list|)
decl_stmt|;
name|String
name|currentVersionPathStr
init|=
name|DagUtils
operator|.
name|getExecJarPathLocal
argument_list|()
decl_stmt|;
name|String
name|currentJarName
init|=
name|DagUtils
operator|.
name|getResourceBaseName
argument_list|(
name|currentVersionPathStr
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
name|Path
name|jarPath
init|=
literal|null
decl_stmt|;
name|FileStatus
name|dirStatus
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|hiveJarDir
operator|!=
literal|null
condition|)
block|{
comment|// check if it is a valid directory in HDFS
name|Path
name|hiveJarDirPath
init|=
operator|new
name|Path
argument_list|(
name|hiveJarDir
argument_list|)
decl_stmt|;
name|fs
operator|=
name|hiveJarDirPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
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
name|hiveJarDir
argument_list|)
argument_list|)
throw|;
block|}
try|try
block|{
name|dirStatus
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|hiveJarDirPath
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|fe
parameter_list|)
block|{
comment|// do nothing
block|}
if|if
condition|(
operator|(
name|dirStatus
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|dirStatus
operator|.
name|isDir
argument_list|()
operator|)
condition|)
block|{
name|FileStatus
index|[]
name|listFileStatus
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|hiveJarDirPath
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
name|DagUtils
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
if|if
condition|(
name|jarName
operator|.
name|equals
argument_list|(
name|currentJarName
argument_list|)
condition|)
block|{
comment|// we have found the jar we need.
name|jarPath
operator|=
name|fstatus
operator|.
name|getPath
argument_list|()
expr_stmt|;
return|return
name|DagUtils
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
comment|// jar wasn't in the directory, copy the one in current use
if|if
condition|(
name|jarPath
operator|==
literal|null
condition|)
block|{
name|Path
name|dest
init|=
operator|new
name|Path
argument_list|(
name|hiveJarDir
operator|+
literal|"/"
operator|+
name|currentJarName
argument_list|)
decl_stmt|;
return|return
name|DagUtils
operator|.
name|localizeResource
argument_list|(
operator|new
name|Path
argument_list|(
name|currentVersionPathStr
argument_list|)
argument_list|,
name|dest
argument_list|,
name|conf
argument_list|)
return|;
block|}
block|}
block|}
comment|/*      * specified location does not exist or is not a directory      * try to push the jar to the hdfs location pointed by      * config variable HIVE_INSTALL_DIR. Path will be      * HIVE_INSTALL_DIR/{username}/.hiveJars/      */
if|if
condition|(
operator|(
name|hiveJarDir
operator|==
literal|null
operator|)
operator|||
operator|(
name|dirStatus
operator|==
literal|null
operator|)
operator|||
operator|(
operator|(
name|dirStatus
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|dirStatus
operator|.
name|isDir
argument_list|()
operator|)
operator|)
condition|)
block|{
name|Path
name|dest
init|=
name|DagUtils
operator|.
name|getDefaultDestDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|String
name|destPathStr
init|=
name|dest
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|jarPathStr
init|=
name|destPathStr
operator|+
literal|"/"
operator|+
name|currentJarName
decl_stmt|;
name|dirStatus
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|dest
argument_list|)
expr_stmt|;
if|if
condition|(
name|dirStatus
operator|.
name|isDir
argument_list|()
condition|)
block|{
return|return
name|DagUtils
operator|.
name|localizeResource
argument_list|(
operator|new
name|Path
argument_list|(
name|currentVersionPathStr
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|jarPathStr
argument_list|)
argument_list|,
name|conf
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_DIR
operator|.
name|format
argument_list|(
name|dest
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|// we couldn't find any valid locations. Throw exception
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
block|}
end_class

end_unit

