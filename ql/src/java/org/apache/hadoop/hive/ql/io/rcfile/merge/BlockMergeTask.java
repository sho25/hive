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
name|io
operator|.
name|rcfile
operator|.
name|merge
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
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLDecoder
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Enumeration
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
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
name|Context
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
name|DriverContext
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
name|QueryPlan
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
name|exec
operator|.
name|ExecDriver
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
name|exec
operator|.
name|HadoopJobExecHelper
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
name|exec
operator|.
name|HadoopJobExecHook
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
name|exec
operator|.
name|Task
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
name|exec
operator|.
name|Throttle
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
name|exec
operator|.
name|Utilities
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
name|io
operator|.
name|CombineHiveInputFormat
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
name|plan
operator|.
name|api
operator|.
name|StageType
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
name|session
operator|.
name|SessionState
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
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
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
name|shims
operator|.
name|ShimLoader
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
name|io
operator|.
name|NullWritable
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
name|mapred
operator|.
name|Counters
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
name|mapred
operator|.
name|FileInputFormat
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
name|mapred
operator|.
name|InputFormat
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
name|mapred
operator|.
name|JobClient
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|RunningJob
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Appender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|FileAppender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|LogManager
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"deprecation"
block|,
literal|"unchecked"
block|}
argument_list|)
specifier|public
class|class
name|BlockMergeTask
extends|extends
name|Task
argument_list|<
name|MergeWork
argument_list|>
implements|implements
name|Serializable
implements|,
name|HadoopJobExecHook
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|protected
specifier|transient
name|JobConf
name|job
decl_stmt|;
specifier|protected
name|HadoopJobExecHelper
name|jobExecHelper
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|QueryPlan
name|queryPlan
parameter_list|,
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|queryPlan
argument_list|,
name|driverContext
argument_list|)
expr_stmt|;
name|job
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|,
name|BlockMergeTask
operator|.
name|class
argument_list|)
expr_stmt|;
name|jobExecHelper
operator|=
operator|new
name|HadoopJobExecHelper
argument_list|(
name|job
argument_list|,
name|this
operator|.
name|console
argument_list|,
name|this
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|requireLock
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
name|boolean
name|success
init|=
literal|true
decl_stmt|;
annotation|@
name|Override
comment|/**    * start a new map-reduce job to do the merge, almost the same as ExecDriver.    */
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|HiveConf
operator|.
name|setVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEINPUTFORMAT
argument_list|,
name|CombineHiveInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|success
operator|=
literal|true
expr_stmt|;
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|setNullOutputFormat
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|work
operator|.
name|getMapperClass
argument_list|()
argument_list|)
expr_stmt|;
name|Context
name|ctx
init|=
name|driverContext
operator|.
name|getCtx
argument_list|()
decl_stmt|;
name|boolean
name|ctxCreated
init|=
literal|false
decl_stmt|;
try|try
block|{
if|if
condition|(
name|ctx
operator|==
literal|null
condition|)
block|{
name|ctx
operator|=
operator|new
name|Context
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|ctxCreated
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"Error launching map-reduce job"
argument_list|,
literal|"\n"
operator|+
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|5
return|;
block|}
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|NullWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|NullWritable
operator|.
name|class
argument_list|)
expr_stmt|;
if|if
condition|(
name|work
operator|.
name|getNumMapTasks
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|job
operator|.
name|setNumMapTasks
argument_list|(
name|work
operator|.
name|getNumMapTasks
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// zero reducers
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|work
operator|.
name|getMinSplitSize
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|HiveConf
operator|.
name|setLongVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAPREDMINSPLITSIZE
argument_list|,
name|work
operator|.
name|getMinSplitSize
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|work
operator|.
name|getInputformat
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|HiveConf
operator|.
name|setVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEINPUTFORMAT
argument_list|,
name|work
operator|.
name|getInputformat
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|inpFormat
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEINPUTFORMAT
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|inpFormat
operator|==
literal|null
operator|)
operator|||
operator|(
operator|!
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|inpFormat
argument_list|)
operator|)
condition|)
block|{
name|inpFormat
operator|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getInputFormatClassName
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Using "
operator|+
name|inpFormat
argument_list|)
expr_stmt|;
try|try
block|{
name|job
operator|.
name|setInputFormat
argument_list|(
call|(
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
call|)
argument_list|(
name|Class
operator|.
name|forName
argument_list|(
name|inpFormat
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
name|String
name|outputPath
init|=
name|this
operator|.
name|work
operator|.
name|getOutputDir
argument_list|()
decl_stmt|;
name|Path
name|tempOutPath
init|=
name|Utilities
operator|.
name|toTempPath
argument_list|(
operator|new
name|Path
argument_list|(
name|outputPath
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|FileSystem
name|fs
init|=
name|tempOutPath
operator|.
name|getFileSystem
argument_list|(
name|job
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|tempOutPath
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|tempOutPath
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Can't make path "
operator|+
name|outputPath
operator|+
literal|" : "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|6
return|;
block|}
name|RCFileBlockMergeOutputFormat
operator|.
name|setMergeOutputPath
argument_list|(
name|job
argument_list|,
operator|new
name|Path
argument_list|(
name|outputPath
argument_list|)
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputKeyClass
argument_list|(
name|NullWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputValueClass
argument_list|(
name|NullWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMERGECURRENTJOBHASDYNAMICPARTITIONS
argument_list|,
name|work
operator|.
name|hasDynamicPartitions
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|returnVal
init|=
literal|0
decl_stmt|;
name|RunningJob
name|rj
init|=
literal|null
decl_stmt|;
name|boolean
name|noName
init|=
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPJOBNAME
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|jobName
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|noName
operator|&&
name|this
operator|.
name|getQueryPlan
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|int
name|maxlen
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEJOBNAMELENGTH
argument_list|)
decl_stmt|;
name|jobName
operator|=
name|Utilities
operator|.
name|abbreviate
argument_list|(
name|this
operator|.
name|getQueryPlan
argument_list|()
operator|.
name|getQueryStr
argument_list|()
argument_list|,
name|maxlen
operator|-
literal|6
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|noName
condition|)
block|{
comment|// This is for a special case to ensure unit tests pass
name|HiveConf
operator|.
name|setVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPJOBNAME
argument_list|,
name|jobName
operator|!=
literal|null
condition|?
name|jobName
else|:
literal|"JOB"
operator|+
name|Utilities
operator|.
name|randGen
operator|.
name|nextInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|addInputPaths
argument_list|(
name|job
argument_list|,
name|work
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|setMapRedWork
argument_list|(
name|job
argument_list|,
name|work
argument_list|,
name|ctx
operator|.
name|getMRTmpFileURI
argument_list|()
argument_list|)
expr_stmt|;
comment|// remove the pwd from conf file so that job tracker doesn't show this
comment|// logs
name|String
name|pwd
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREPWD
argument_list|)
decl_stmt|;
if|if
condition|(
name|pwd
operator|!=
literal|null
condition|)
block|{
name|HiveConf
operator|.
name|setVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREPWD
argument_list|,
literal|"HIVE"
argument_list|)
expr_stmt|;
block|}
name|JobClient
name|jc
init|=
operator|new
name|JobClient
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|String
name|addedJars
init|=
name|Utilities
operator|.
name|getResourceFiles
argument_list|(
name|job
argument_list|,
name|SessionState
operator|.
name|ResourceType
operator|.
name|JAR
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|addedJars
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|job
operator|.
name|set
argument_list|(
literal|"tmpjars"
argument_list|,
name|addedJars
argument_list|)
expr_stmt|;
block|}
comment|// make this client wait if job trcker is not behaving well.
name|Throttle
operator|.
name|checkJobTracker
argument_list|(
name|job
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|// Finally SUBMIT the JOB!
name|rj
operator|=
name|jc
operator|.
name|submitJob
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|returnVal
operator|=
name|jobExecHelper
operator|.
name|progress
argument_list|(
name|rj
argument_list|,
name|jc
argument_list|)
expr_stmt|;
name|success
operator|=
operator|(
name|returnVal
operator|==
literal|0
operator|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|String
name|mesg
init|=
literal|" with exception '"
operator|+
name|Utilities
operator|.
name|getNameMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"'"
decl_stmt|;
if|if
condition|(
name|rj
operator|!=
literal|null
condition|)
block|{
name|mesg
operator|=
literal|"Ended Job = "
operator|+
name|rj
operator|.
name|getJobID
argument_list|()
operator|+
name|mesg
expr_stmt|;
block|}
else|else
block|{
name|mesg
operator|=
literal|"Job Submission failed"
operator|+
name|mesg
expr_stmt|;
block|}
comment|// Has to use full name to make sure it does not conflict with
comment|// org.apache.commons.lang.StringUtils
name|console
operator|.
name|printError
argument_list|(
name|mesg
argument_list|,
literal|"\n"
operator|+
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|success
operator|=
literal|false
expr_stmt|;
name|returnVal
operator|=
literal|1
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
if|if
condition|(
name|ctxCreated
condition|)
block|{
name|ctx
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|rj
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|returnVal
operator|!=
literal|0
condition|)
block|{
name|rj
operator|.
name|killJob
argument_list|()
expr_stmt|;
block|}
name|HadoopJobExecHelper
operator|.
name|runningJobKillURIs
operator|.
name|remove
argument_list|(
name|rj
operator|.
name|getJobID
argument_list|()
argument_list|)
expr_stmt|;
name|jobID
operator|=
name|rj
operator|.
name|getID
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|RCFileMergeMapper
operator|.
name|jobClose
argument_list|(
name|outputPath
argument_list|,
name|success
argument_list|,
name|job
argument_list|,
name|console
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{       }
block|}
return|return
operator|(
name|returnVal
operator|)
return|;
block|}
specifier|private
name|void
name|addInputPaths
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|MergeWork
name|work
parameter_list|)
block|{
for|for
control|(
name|String
name|path
range|:
name|work
operator|.
name|getInputPaths
argument_list|()
control|)
block|{
name|FileInputFormat
operator|.
name|addInputPath
argument_list|(
name|job
argument_list|,
operator|new
name|Path
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"RCFile Merge"
return|;
block|}
specifier|public
specifier|static
name|String
name|INPUT_SEPERATOR
init|=
literal|":"
decl_stmt|;
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|jobConfArgs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|inputPathStr
init|=
literal|null
decl_stmt|;
name|String
name|outputDir
init|=
literal|null
decl_stmt|;
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|args
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
literal|"-input"
argument_list|)
condition|)
block|{
name|inputPathStr
operator|=
name|args
index|[
operator|++
name|i
index|]
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|args
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
literal|"-jobconf"
argument_list|)
condition|)
block|{
name|jobConfArgs
operator|.
name|add
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|args
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
literal|"-outputDir"
argument_list|)
condition|)
block|{
name|outputDir
operator|=
name|args
index|[
operator|++
name|i
index|]
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IndexOutOfBoundsException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Missing argument to option"
argument_list|)
expr_stmt|;
name|printUsage
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|inputPathStr
operator|==
literal|null
operator|||
name|outputDir
operator|==
literal|null
operator|||
name|outputDir
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|printUsage
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|inputPaths
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|String
index|[]
name|paths
init|=
name|inputPathStr
operator|.
name|split
argument_list|(
name|INPUT_SEPERATOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|paths
operator|==
literal|null
operator|||
name|paths
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|printUsage
argument_list|()
expr_stmt|;
block|}
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
name|JobConf
name|conf
init|=
operator|new
name|JobConf
argument_list|(
name|BlockMergeTask
operator|.
name|class
argument_list|)
decl_stmt|;
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|(
name|conf
argument_list|,
name|BlockMergeTask
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|path
range|:
name|paths
control|)
block|{
try|try
block|{
name|Path
name|pathObj
init|=
operator|new
name|Path
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|==
literal|null
condition|)
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|pathObj
operator|.
name|toUri
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
name|FileStatus
name|fstatus
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|pathObj
argument_list|)
decl_stmt|;
if|if
condition|(
name|fstatus
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|FileStatus
index|[]
name|fileStatus
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|pathObj
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|st
range|:
name|fileStatus
control|)
block|{
name|inputPaths
operator|.
name|add
argument_list|(
name|st
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|inputPaths
operator|.
name|add
argument_list|(
name|fstatus
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|(
name|System
operator|.
name|err
argument_list|)
expr_stmt|;
block|}
block|}
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"JobConf:\n"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|one
range|:
name|jobConfArgs
control|)
block|{
name|int
name|eqIndex
init|=
name|one
operator|.
name|indexOf
argument_list|(
literal|'='
argument_list|)
decl_stmt|;
if|if
condition|(
name|eqIndex
operator|!=
operator|-
literal|1
condition|)
block|{
try|try
block|{
name|String
name|key
init|=
name|one
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|eqIndex
argument_list|)
decl_stmt|;
name|String
name|value
init|=
name|URLDecoder
operator|.
name|decode
argument_list|(
name|one
operator|.
name|substring
argument_list|(
name|eqIndex
operator|+
literal|1
argument_list|)
argument_list|,
literal|"UTF-8"
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|key
argument_list|)
operator|.
name|append
argument_list|(
literal|"="
argument_list|)
operator|.
name|append
argument_list|(
name|value
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Unexpected error "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|" while encoding "
operator|+
name|one
operator|.
name|substring
argument_list|(
name|eqIndex
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|BlockMergeTask
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|isSilent
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESESSIONSILENT
argument_list|)
decl_stmt|;
name|LogHelper
name|console
init|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|,
name|isSilent
argument_list|)
decl_stmt|;
comment|// print out the location of the log file for the user so
comment|// that it's easy to find reason for local mode execution failures
for|for
control|(
name|Appender
name|appender
range|:
name|Collections
operator|.
name|list
argument_list|(
operator|(
name|Enumeration
argument_list|<
name|Appender
argument_list|>
operator|)
name|LogManager
operator|.
name|getRootLogger
argument_list|()
operator|.
name|getAllAppenders
argument_list|()
argument_list|)
control|)
block|{
if|if
condition|(
name|appender
operator|instanceof
name|FileAppender
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Execution log at: "
operator|+
operator|(
operator|(
name|FileAppender
operator|)
name|appender
operator|)
operator|.
name|getFile
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// log the list of job conf parameters for reference
name|LOG
operator|.
name|info
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|MergeWork
name|mergeWork
init|=
operator|new
name|MergeWork
argument_list|(
name|inputPaths
argument_list|,
name|outputDir
argument_list|)
decl_stmt|;
name|DriverContext
name|driverCxt
init|=
operator|new
name|DriverContext
argument_list|()
decl_stmt|;
name|BlockMergeTask
name|taskExec
init|=
operator|new
name|BlockMergeTask
argument_list|()
decl_stmt|;
name|taskExec
operator|.
name|initialize
argument_list|(
name|hiveConf
argument_list|,
literal|null
argument_list|,
name|driverCxt
argument_list|)
expr_stmt|;
name|taskExec
operator|.
name|setWork
argument_list|(
name|mergeWork
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
name|taskExec
operator|.
name|execute
argument_list|(
name|driverCxt
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|!=
literal|0
condition|)
block|{
name|System
operator|.
name|exit
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|printUsage
parameter_list|()
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"BlockMergeTask -input<colon seperated input paths>  "
operator|+
literal|"-outputDir outputDir [-jobconf k1=v1 [-jobconf k2=v2] ...] "
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|MAPRED
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|checkFatalErrors
parameter_list|(
name|Counters
name|ctrs
parameter_list|,
name|StringBuilder
name|errMsg
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|logPlanProgress
parameter_list|(
name|SessionState
name|ss
parameter_list|)
throws|throws
name|IOException
block|{
comment|// no op
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateCounters
parameter_list|(
name|Counters
name|ctrs
parameter_list|,
name|RunningJob
name|rj
parameter_list|)
throws|throws
name|IOException
block|{
comment|// no op
block|}
annotation|@
name|Override
specifier|protected
name|void
name|localizeMRTmpFilesImpl
parameter_list|(
name|Context
name|ctx
parameter_list|)
block|{
comment|// no op
block|}
block|}
end_class

end_unit

