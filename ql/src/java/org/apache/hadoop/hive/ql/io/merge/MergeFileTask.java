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
name|merge
package|;
end_package

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
name|Operator
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
name|OperatorDesc
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

begin_comment
comment|/**  * Task for fast merging of ORC and RC files.  */
end_comment

begin_class
specifier|public
class|class
name|MergeFileTask
extends|extends
name|Task
argument_list|<
name|MergeFileWork
argument_list|>
implements|implements
name|Serializable
implements|,
name|HadoopJobExecHook
block|{
specifier|private
specifier|transient
name|JobConf
name|job
decl_stmt|;
specifier|private
name|HadoopJobExecHelper
name|jobExecHelper
decl_stmt|;
specifier|private
name|boolean
name|success
init|=
literal|true
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
name|MergeFileTask
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
comment|/**    * start a new map-reduce job to do the merge, almost the same as ExecDriver.    */
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
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
name|RunningJob
name|rj
init|=
literal|null
decl_stmt|;
name|int
name|returnVal
init|=
literal|0
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
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|prepareJobOutput
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|job
operator|.
name|setInputFormat
argument_list|(
name|work
operator|.
name|getInputformatClass
argument_list|()
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
name|MergeFileMapper
operator|.
name|class
argument_list|)
expr_stmt|;
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
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// create the temp directories
name|Path
name|outputPath
init|=
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
comment|// set job name
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
comment|// add input path
name|addInputPaths
argument_list|(
name|job
argument_list|,
name|work
argument_list|)
expr_stmt|;
comment|// serialize work
name|Utilities
operator|.
name|setMapWork
argument_list|(
name|job
argument_list|,
name|work
argument_list|,
name|ctx
operator|.
name|getMRTmpPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// remove pwd from conf file so that job tracker doesn't show this logs
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
comment|// submit the job
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
argument_list|,
literal|null
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
name|runningJobs
operator|.
name|remove
argument_list|(
name|rj
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
comment|// get the list of Dynamic partition paths
if|if
condition|(
name|rj
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|work
operator|.
name|getAliasToWork
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|work
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|op
operator|.
name|jobClose
argument_list|(
name|job
argument_list|,
name|success
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// jobClose needs to execute successfully otherwise fail task
if|if
condition|(
name|success
condition|)
block|{
name|success
operator|=
literal|false
expr_stmt|;
name|returnVal
operator|=
literal|3
expr_stmt|;
name|String
name|mesg
init|=
literal|"Job Commit failed with exception '"
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
block|}
block|}
block|}
return|return
name|returnVal
return|;
block|}
specifier|private
name|void
name|addInputPaths
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|MergeFileWork
name|work
parameter_list|)
block|{
for|for
control|(
name|Path
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
name|path
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
literal|"MergeFileTask"
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

