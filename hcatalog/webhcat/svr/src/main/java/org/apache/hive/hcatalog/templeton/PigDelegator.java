begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
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
name|ArrayList
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|exec
operator|.
name|ExecuteException
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
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|JobSubmissionConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|TempletonControllerJob
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|TempletonUtils
import|;
end_import

begin_comment
comment|/**  * Submit a Pig job.  *  * This is the backend of the pig web service.  */
end_comment

begin_class
specifier|public
class|class
name|PigDelegator
extends|extends
name|LauncherDelegator
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
name|PigDelegator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|PigDelegator
parameter_list|(
name|AppConfig
name|appConf
parameter_list|)
block|{
name|super
argument_list|(
name|appConf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|EnqueueBean
name|run
parameter_list|(
name|String
name|user
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|userArgs
parameter_list|,
name|String
name|execute
parameter_list|,
name|String
name|srcFile
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|pigArgs
parameter_list|,
name|String
name|otherFiles
parameter_list|,
name|String
name|statusdir
parameter_list|,
name|String
name|callback
parameter_list|,
name|boolean
name|usesHcatalog
parameter_list|,
name|String
name|completedUrl
parameter_list|,
name|boolean
name|enablelog
parameter_list|,
name|Boolean
name|enableJobReconnect
parameter_list|)
throws|throws
name|NotAuthorizedException
throws|,
name|BadParam
throws|,
name|BusyException
throws|,
name|QueueException
throws|,
name|ExecuteException
throws|,
name|IOException
throws|,
name|InterruptedException
throws|,
name|TooManyRequestsException
block|{
name|runAs
operator|=
name|user
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|args
init|=
name|makeArgs
argument_list|(
name|execute
argument_list|,
name|srcFile
argument_list|,
name|pigArgs
argument_list|,
name|otherFiles
argument_list|,
name|statusdir
argument_list|,
name|usesHcatalog
argument_list|,
name|completedUrl
argument_list|,
name|enablelog
argument_list|,
name|enableJobReconnect
argument_list|)
decl_stmt|;
return|return
name|enqueueController
argument_list|(
name|user
argument_list|,
name|userArgs
argument_list|,
name|callback
argument_list|,
name|args
argument_list|)
return|;
block|}
comment|/**    * @param execute pig query string to be executed    * @param srcFile pig query file to be executed    * @param pigArgs pig command line arguments    * @param otherFiles  files to be copied to the map reduce cluster    * @param statusdir status dir location    * @param usesHcatalog whether the command uses hcatalog/needs to connect    *         to hive metastore server    * @param completedUrl call back url    * @param enablelog    * @param enableJobReconnect    * @return list of arguments    * @throws BadParam    * @throws IOException    * @throws InterruptedException    */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|makeArgs
parameter_list|(
name|String
name|execute
parameter_list|,
name|String
name|srcFile
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|pigArgs
parameter_list|,
name|String
name|otherFiles
parameter_list|,
name|String
name|statusdir
parameter_list|,
name|boolean
name|usesHcatalog
parameter_list|,
name|String
name|completedUrl
parameter_list|,
name|boolean
name|enablelog
parameter_list|,
name|Boolean
name|enableJobReconnect
parameter_list|)
throws|throws
name|BadParam
throws|,
name|IOException
throws|,
name|InterruptedException
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|args
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|//check if the REST command specified explicitly to use hcatalog
comment|// or if it says that implicitly using the pig -useHCatalog arg
name|boolean
name|needsMetastoreAccess
init|=
name|usesHcatalog
operator|||
name|hasPigArgUseHcat
argument_list|(
name|pigArgs
argument_list|)
decl_stmt|;
try|try
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|allFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|srcFile
argument_list|)
condition|)
block|{
name|allFiles
operator|.
name|add
argument_list|(
name|TempletonUtils
operator|.
name|hadoopFsFilename
argument_list|(
name|srcFile
argument_list|,
name|appConf
argument_list|,
name|runAs
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|otherFiles
argument_list|)
condition|)
block|{
name|String
index|[]
name|ofs
init|=
name|TempletonUtils
operator|.
name|hadoopFsListAsArray
argument_list|(
name|otherFiles
argument_list|,
name|appConf
argument_list|,
name|runAs
argument_list|)
decl_stmt|;
name|allFiles
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|ofs
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|args
operator|.
name|addAll
argument_list|(
name|makeLauncherArgs
argument_list|(
name|appConf
argument_list|,
name|statusdir
argument_list|,
name|completedUrl
argument_list|,
name|allFiles
argument_list|,
name|enablelog
argument_list|,
name|enableJobReconnect
argument_list|,
name|JobType
operator|.
name|PIG
argument_list|)
argument_list|)
expr_stmt|;
name|boolean
name|shipPigTar
init|=
name|appConf
operator|.
name|pigArchive
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|appConf
operator|.
name|pigArchive
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
decl_stmt|;
name|boolean
name|shipHiveTar
init|=
name|needsMetastoreAccess
operator|&&
name|appConf
operator|.
name|hiveArchive
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|appConf
operator|.
name|hiveArchive
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
decl_stmt|;
if|if
condition|(
name|shipPigTar
operator|||
name|shipHiveTar
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
name|ARCHIVES
argument_list|)
expr_stmt|;
name|StringBuilder
name|archives
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|shipPigTar
condition|)
block|{
name|archives
operator|.
name|append
argument_list|(
name|appConf
operator|.
name|pigArchive
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shipPigTar
operator|&&
name|shipHiveTar
condition|)
block|{
name|archives
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shipHiveTar
condition|)
block|{
name|archives
operator|.
name|append
argument_list|(
name|appConf
operator|.
name|hiveArchive
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|args
operator|.
name|add
argument_list|(
name|archives
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shipHiveTar
condition|)
block|{
name|addDef
argument_list|(
name|args
argument_list|,
name|JobSubmissionConstants
operator|.
name|PigConstants
operator|.
name|HIVE_HOME
argument_list|,
name|appConf
operator|.
name|get
argument_list|(
name|AppConfig
operator|.
name|HIVE_HOME_PATH
argument_list|)
argument_list|)
expr_stmt|;
name|addDef
argument_list|(
name|args
argument_list|,
name|JobSubmissionConstants
operator|.
name|PigConstants
operator|.
name|HCAT_HOME
argument_list|,
name|appConf
operator|.
name|get
argument_list|(
name|AppConfig
operator|.
name|HCAT_HOME_PATH
argument_list|)
argument_list|)
expr_stmt|;
comment|//Pig which uses HCat will pass this to HCat so that it can find the metastore
name|addDef
argument_list|(
name|args
argument_list|,
name|JobSubmissionConstants
operator|.
name|PigConstants
operator|.
name|PIG_OPTS
argument_list|,
name|appConf
operator|.
name|get
argument_list|(
name|AppConfig
operator|.
name|HIVE_PROPS_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|args
operator|.
name|add
argument_list|(
literal|"--"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|appConf
operator|.
name|pigPath
argument_list|()
argument_list|)
expr_stmt|;
comment|//the token file location should be first argument of pig
name|args
operator|.
name|add
argument_list|(
literal|"-D"
operator|+
name|TempletonControllerJob
operator|.
name|TOKEN_FILE_ARG_PLACEHOLDER
argument_list|)
expr_stmt|;
comment|//add mapreduce job tag placeholder
name|args
operator|.
name|add
argument_list|(
literal|"-D"
operator|+
name|TempletonControllerJob
operator|.
name|MAPREDUCE_JOB_TAGS_ARG_PLACEHOLDER
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|pigArg
range|:
name|pigArgs
control|)
block|{
name|args
operator|.
name|add
argument_list|(
name|pigArg
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|needsMetastoreAccess
condition|)
block|{
name|addHiveMetaStoreTokenArg
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|execute
argument_list|)
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-execute"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|execute
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|srcFile
argument_list|)
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-file"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|TempletonUtils
operator|.
name|hadoopFsPath
argument_list|(
name|srcFile
argument_list|,
name|appConf
argument_list|,
name|runAs
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BadParam
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BadParam
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|args
return|;
block|}
comment|/**    * Check if the pig arguments has -useHCatalog set    * see http://hive.apache.org/docs/hcat_r0.5.0/loadstore.pdf    */
specifier|private
name|boolean
name|hasPigArgUseHcat
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|pigArgs
parameter_list|)
block|{
return|return
name|pigArgs
operator|.
name|contains
argument_list|(
literal|"-useHCatalog"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

