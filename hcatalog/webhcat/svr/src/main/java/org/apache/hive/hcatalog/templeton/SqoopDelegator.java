begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Path
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
comment|/**  * Submit a Sqoop job.  *  * This is the backend of the Sqoop web service.  */
end_comment

begin_class
specifier|public
class|class
name|SqoopDelegator
extends|extends
name|LauncherDelegator
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
name|SqoopDelegator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|SqoopDelegator
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
name|command
parameter_list|,
name|String
name|optionsFile
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
name|String
name|completedUrl
parameter_list|,
name|boolean
name|enablelog
parameter_list|,
name|Boolean
name|enableJobReconnect
parameter_list|,
name|String
name|libdir
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
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|appConf
operator|.
name|sqoopArchive
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|appConf
operator|.
name|sqoopPath
argument_list|()
argument_list|)
operator|&&
operator|!
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|appConf
operator|.
name|sqoopHome
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"If '"
operator|+
name|AppConfig
operator|.
name|SQOOP_ARCHIVE_NAME
operator|+
literal|"' is defined, '"
operator|+
name|AppConfig
operator|.
name|SQOOP_PATH_NAME
operator|+
literal|"' and '"
operator|+
name|AppConfig
operator|.
name|SQOOP_HOME_PATH
operator|+
literal|"' must be defined"
argument_list|)
throw|;
block|}
block|}
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
name|command
argument_list|,
name|optionsFile
argument_list|,
name|otherFiles
argument_list|,
name|statusdir
argument_list|,
name|completedUrl
argument_list|,
name|enablelog
argument_list|,
name|enableJobReconnect
argument_list|,
name|libdir
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
name|command
parameter_list|,
name|String
name|optionsFile
parameter_list|,
name|String
name|otherFiles
parameter_list|,
name|String
name|statusdir
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
name|String
name|libdir
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
name|args
operator|.
name|addAll
argument_list|(
name|makeBasicArgs
argument_list|(
name|optionsFile
argument_list|,
name|otherFiles
argument_list|,
name|statusdir
argument_list|,
name|completedUrl
argument_list|,
name|enablelog
argument_list|,
name|enableJobReconnect
argument_list|,
name|libdir
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
name|TempletonUtils
operator|.
name|addCmdForWindows
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|appConf
operator|.
name|sqoopPath
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|command
argument_list|)
condition|)
block|{
name|String
index|[]
name|temArgs
init|=
name|command
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|temArgs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|args
operator|.
name|add
argument_list|(
name|TempletonUtils
operator|.
name|quoteForWindows
argument_list|(
name|temArgs
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
comment|// The token file location and mapreduce job tag should be right after the tool argument
if|if
condition|(
name|i
operator|==
literal|0
operator|&&
operator|!
name|temArgs
index|[
name|i
index|]
operator|.
name|startsWith
argument_list|(
literal|"--"
argument_list|)
condition|)
block|{
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
block|}
if|if
condition|(
name|i
operator|==
literal|0
operator|&&
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|libdir
argument_list|)
operator|&&
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|appConf
operator|.
name|sqoopArchive
argument_list|()
argument_list|)
condition|)
block|{
comment|//http://sqoop.apache.org/docs/1.4.5/SqoopUserGuide.html#_using_generic_and_specific_arguments
name|String
name|libJars
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|args
control|)
block|{
if|if
condition|(
name|s
operator|.
name|startsWith
argument_list|(
name|JobSubmissionConstants
operator|.
name|Sqoop
operator|.
name|LIB_JARS
argument_list|)
condition|)
block|{
name|libJars
operator|=
name|s
operator|.
name|substring
argument_list|(
name|s
operator|.
name|indexOf
argument_list|(
literal|"="
argument_list|)
operator|+
literal|1
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
comment|//the jars in libJars will be localized to CWD of the launcher task; then -libjars will
comment|//cause them to be localized for the Sqoop MR job tasks
name|args
operator|.
name|add
argument_list|(
name|TempletonUtils
operator|.
name|quoteForWindows
argument_list|(
literal|"-libjars"
argument_list|)
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|TempletonUtils
operator|.
name|quoteForWindows
argument_list|(
name|libJars
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|optionsFile
argument_list|)
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"--options-file"
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
name|optionsFile
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
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|makeBasicArgs
parameter_list|(
name|String
name|optionsFile
parameter_list|,
name|String
name|otherFiles
parameter_list|,
name|String
name|statusdir
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
name|String
name|libdir
parameter_list|)
throws|throws
name|URISyntaxException
throws|,
name|FileNotFoundException
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
name|optionsFile
argument_list|)
condition|)
name|allFiles
operator|.
name|add
argument_list|(
name|TempletonUtils
operator|.
name|hadoopFsFilename
argument_list|(
name|optionsFile
argument_list|,
name|appConf
argument_list|,
name|runAs
argument_list|)
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|libdir
argument_list|)
operator|&&
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|appConf
operator|.
name|sqoopArchive
argument_list|()
argument_list|)
condition|)
block|{
comment|/**Sqoop accesses databases via JDBC.  This means it needs to have appropriate JDBC       drivers available.  Normally, the user would install Sqoop and place these jars       into SQOOP_HOME/lib.  When WebHCat is configured to auto-ship the Sqoop tar file, we       need to make sure that relevant JDBC jars are available on target node but we cannot modify       lib/ of exploded tar because Dist Cache intentionally prevents this.       The user is expected to place any JDBC jars into an HDFS directory and specify this       dir in "libdir" parameter.  WebHCat then ensures that these jars are localized for the launcher task       and made available to Sqoop.       {@link org.apache.hive.hcatalog.templeton.tool.LaunchMapper#handleSqoop(org.apache.hadoop.conf.Configuration, java.util.Map)}       {@link #makeArgs(String, String, String, String, String, boolean, String)}       */
name|LOG
operator|.
name|debug
argument_list|(
literal|"libdir="
operator|+
name|libdir
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|jarList
init|=
name|TempletonUtils
operator|.
name|hadoopFsListChildren
argument_list|(
name|libdir
argument_list|,
name|appConf
argument_list|,
name|runAs
argument_list|)
decl_stmt|;
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|jarList
argument_list|)
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Path
name|jar
range|:
name|jarList
control|)
block|{
name|allFiles
operator|.
name|add
argument_list|(
name|jar
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|jar
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|setLength
argument_list|(
name|sb
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|//we use the same mechanism to copy "files"/"otherFiles" and "libdir", but we only want to put
comment|//contents of "libdir" in Sqoop/lib, thus we pass the list of names here
name|addDef
argument_list|(
name|args
argument_list|,
name|JobSubmissionConstants
operator|.
name|Sqoop
operator|.
name|LIB_JARS
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|addDef
argument_list|(
name|args
argument_list|,
name|AppConfig
operator|.
name|SQOOP_HOME_PATH
argument_list|,
name|appConf
operator|.
name|get
argument_list|(
name|AppConfig
operator|.
name|SQOOP_HOME_PATH
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|SQOOP
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|TempletonUtils
operator|.
name|isset
argument_list|(
name|appConf
operator|.
name|sqoopArchive
argument_list|()
argument_list|)
condition|)
block|{
name|args
operator|.
name|add
argument_list|(
literal|"-archives"
argument_list|)
expr_stmt|;
name|args
operator|.
name|add
argument_list|(
name|appConf
operator|.
name|sqoopArchive
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|args
return|;
block|}
block|}
end_class

end_unit

