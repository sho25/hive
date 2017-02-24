begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
comment|/**  * Submit a job to the MapReduce queue.  *  * This is the backend of the mapreduce/jar web service.  */
end_comment

begin_class
specifier|public
class|class
name|JarDelegator
extends|extends
name|LauncherDelegator
block|{
specifier|public
name|JarDelegator
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
name|jar
parameter_list|,
name|String
name|mainClass
parameter_list|,
name|String
name|libjars
parameter_list|,
name|String
name|files
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|jarArgs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|defines
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
parameter_list|,
name|JobType
name|jobType
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
name|jar
argument_list|,
name|mainClass
argument_list|,
name|libjars
argument_list|,
name|files
argument_list|,
name|jarArgs
argument_list|,
name|defines
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
argument_list|,
name|jobType
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
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|makeArgs
parameter_list|(
name|String
name|jar
parameter_list|,
name|String
name|mainClass
parameter_list|,
name|String
name|libjars
parameter_list|,
name|String
name|files
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|jarArgs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|defines
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
parameter_list|,
name|JobType
name|jobType
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
name|allFiles
operator|.
name|add
argument_list|(
name|TempletonUtils
operator|.
name|hadoopFsFilename
argument_list|(
name|jar
argument_list|,
name|appConf
argument_list|,
name|runAs
argument_list|)
argument_list|)
expr_stmt|;
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
name|jobType
argument_list|)
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"--"
argument_list|)
expr_stmt|;
comment|//check if the rest command specified explicitly to use hcatalog
if|if
condition|(
name|usesHcatalog
condition|)
block|{
name|addHiveMetaStoreTokenArg
argument_list|()
expr_stmt|;
block|}
name|args
operator|.
name|add
argument_list|(
name|appConf
operator|.
name|clusterHadoop
argument_list|()
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"jar"
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
name|jar
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
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|mainClass
argument_list|)
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
name|mainClass
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|libjars
argument_list|)
condition|)
block|{
name|String
name|libjarsListAsString
init|=
name|TempletonUtils
operator|.
name|hadoopFsListAsString
argument_list|(
name|libjars
argument_list|,
name|appConf
argument_list|,
name|runAs
argument_list|)
decl_stmt|;
comment|//This will work only if the files are local files on webhcat server
comment|// (which is not very useful since users might not have access to that file system).
comment|//This is likely the HIVE-5188 issue
name|args
operator|.
name|add
argument_list|(
literal|"-libjars"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|libjarsListAsString
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|files
argument_list|)
condition|)
block|{
name|String
name|filesListAsString
init|=
name|TempletonUtils
operator|.
name|hadoopFsListAsString
argument_list|(
name|files
argument_list|,
name|appConf
argument_list|,
name|runAs
argument_list|)
decl_stmt|;
name|args
operator|.
name|add
argument_list|(
literal|"-files"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|filesListAsString
argument_list|)
expr_stmt|;
block|}
comment|//the token file location comes after mainClass, as a -D prop=val
name|args
operator|.
name|add
argument_list|(
literal|"-D"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
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
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|TempletonControllerJob
operator|.
name|MAPREDUCE_JOB_TAGS_ARG_PLACEHOLDER
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|d
range|:
name|defines
control|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-D"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|arg
range|:
name|jarArgs
control|)
block|{
name|args
operator|.
name|add
argument_list|(
name|arg
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
block|}
end_class

end_unit

