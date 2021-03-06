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
name|truncate
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadLocalRandom
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
name|lang3
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
name|common
operator|.
name|JavaUtils
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
name|TaskQueue
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
name|QueryState
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
name|exec
operator|.
name|mr
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
name|mr
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
name|mr
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
name|io
operator|.
name|BucketizedHiveInputFormat
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
name|HiveFileFormatUtils
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
name|HiveOutputFormatImpl
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
name|MapredWork
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
name|hadoop
operator|.
name|mapreduce
operator|.
name|MRJobConfig
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
name|ColumnTruncateTask
extends|extends
name|Task
argument_list|<
name|ColumnTruncateWork
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
name|QueryState
name|queryState
parameter_list|,
name|QueryPlan
name|queryPlan
parameter_list|,
name|TaskQueue
name|taskQueue
parameter_list|,
name|Context
name|context
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|queryState
argument_list|,
name|queryPlan
argument_list|,
name|taskQueue
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|job
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|,
name|ColumnTruncateTask
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
comment|/**    * start a new map-reduce job to do the truncation, almost the same as ExecDriver.    */
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
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
name|BucketizedHiveInputFormat
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
name|HiveFileFormatUtils
operator|.
name|prepareJobOutput
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormat
argument_list|(
name|HiveOutputFormatImpl
operator|.
name|class
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
name|context
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
name|LOG
operator|.
name|error
argument_list|(
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
name|setException
argument_list|(
name|e
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
name|JavaUtils
operator|.
name|loadClass
argument_list|(
name|inpFormat
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
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|Path
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
name|outputPath
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
name|setException
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Can't make path "
operator|+
name|outputPath
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|6
return|;
block|}
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
name|job
operator|.
name|get
argument_list|(
name|MRJobConfig
operator|.
name|JOB_NAME
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
name|job
operator|.
name|set
argument_list|(
name|MRJobConfig
operator|.
name|JOB_NAME
argument_list|,
name|jobName
operator|!=
literal|null
condition|?
name|jobName
else|:
literal|"JOB"
operator|+
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
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
name|MapredWork
name|mrWork
init|=
operator|new
name|MapredWork
argument_list|()
decl_stmt|;
name|mrWork
operator|.
name|setMapWork
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|setMapRedWork
argument_list|(
name|job
argument_list|,
name|mrWork
argument_list|,
name|ctx
operator|.
name|getMRTmpPath
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
name|this
operator|.
name|jobID
operator|=
name|rj
operator|.
name|getJobID
argument_list|()
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
argument_list|,
name|ctx
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
name|String
name|mesg
init|=
name|rj
operator|!=
literal|null
condition|?
operator|(
literal|"Ended Job = "
operator|+
name|rj
operator|.
name|getJobID
argument_list|()
operator|)
else|:
literal|"Job Submission failed"
decl_stmt|;
comment|// Has to use full name to make sure it does not conflict with
comment|// org.apache.commons.lang3.StringUtils
name|LOG
operator|.
name|error
argument_list|(
name|mesg
argument_list|,
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
name|setException
argument_list|(
name|e
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
block|}
name|ColumnTruncateMapper
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
argument_list|,
name|work
operator|.
name|getDynPartCtx
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed while cleaning up "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HadoopJobExecHelper
operator|.
name|runningJobs
operator|.
name|remove
argument_list|(
name|rj
argument_list|)
expr_stmt|;
block|}
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
name|ColumnTruncateWork
name|work
parameter_list|)
block|{
name|FileInputFormat
operator|.
name|addInputPath
argument_list|(
name|job
argument_list|,
name|work
operator|.
name|getInputDir
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"RCFile ColumnTruncate"
return|;
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
block|}
end_class

end_unit

