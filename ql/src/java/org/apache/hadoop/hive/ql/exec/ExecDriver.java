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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLEncoder
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
name|io
operator|.
name|*
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
name|*
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
name|plan
operator|.
name|mapredWork
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
name|metadata
operator|.
name|HiveException
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
name|*
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

begin_class
specifier|public
class|class
name|ExecDriver
extends|extends
name|Task
argument_list|<
name|mapredWork
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|LOAD_PER_REDUCER
init|=
literal|1024
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
specifier|transient
specifier|protected
name|JobConf
name|job
decl_stmt|;
comment|/**    * Constructor when invoked from QL    */
specifier|public
name|ExecDriver
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Initialization when invoked from QL    */
specifier|public
name|void
name|initialize
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|job
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|,
name|ExecDriver
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor/Initialization for invocation as independent utility    */
specifier|public
name|ExecDriver
parameter_list|(
name|mapredWork
name|plan
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|boolean
name|isSilent
parameter_list|)
block|{
name|setWork
argument_list|(
name|plan
argument_list|)
expr_stmt|;
name|this
operator|.
name|job
operator|=
name|job
expr_stmt|;
name|LOG
operator|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|console
operator|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|,
name|isSilent
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|fillInDefaults
parameter_list|()
block|{
comment|// this is a temporary hack to fix things that are not fixed in the compiler
if|if
condition|(
name|work
operator|.
name|getNumReduceTasks
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|work
operator|.
name|getReducer
argument_list|()
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Number of reduce tasks not specified. Defaulting to 0 since there's no reduce operator"
argument_list|)
expr_stmt|;
name|work
operator|.
name|setNumReduceTasks
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Number of reduce tasks not specified. Defaulting to jobconf value of: "
operator|+
name|job
operator|.
name|getNumReduceTasks
argument_list|()
argument_list|)
expr_stmt|;
name|work
operator|.
name|setNumReduceTasks
argument_list|(
name|job
operator|.
name|getNumReduceTasks
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
name|LOG
operator|.
name|info
argument_list|(
literal|"Number of reduce tasks determined at compile : "
operator|+
name|work
operator|.
name|getNumReduceTasks
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * A list of the currently running jobs spawned in this Hive instance that is used    * to kill all running jobs in the event of an unexpected shutdown - i.e., the JVM shuts    * down while there are still jobs running.    */
specifier|public
specifier|static
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|runningJobKillURIs
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
comment|/**    * In Hive, when the user control-c's the command line, any running jobs spawned from that command     * line are best-effort killed.    *    * This static constructor registers a shutdown thread to iterate over all the running job    * kill URLs and do a get on them.    *    */
static|static
block|{
if|if
condition|(
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
argument_list|()
operator|.
name|getBoolean
argument_list|(
literal|"webinterface.private.actions"
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|addShutdownHook
argument_list|(
operator|new
name|Thread
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
for|for
control|(
name|Iterator
argument_list|<
name|String
argument_list|>
name|elems
init|=
name|runningJobKillURIs
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|elems
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|String
name|uri
init|=
name|elems
operator|.
name|next
argument_list|()
decl_stmt|;
try|try
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"killing job with: "
operator|+
name|uri
argument_list|)
expr_stmt|;
name|int
name|retCode
init|=
operator|(
operator|(
name|java
operator|.
name|net
operator|.
name|HttpURLConnection
operator|)
operator|new
name|java
operator|.
name|net
operator|.
name|URL
argument_list|(
name|uri
argument_list|)
operator|.
name|openConnection
argument_list|()
operator|)
operator|.
name|getResponseCode
argument_list|()
decl_stmt|;
if|if
condition|(
name|retCode
operator|!=
literal|200
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Got an error trying to kill job with URI: "
operator|+
name|uri
operator|+
literal|" = "
operator|+
name|retCode
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"trying to kill job, caught: "
operator|+
name|e
argument_list|)
expr_stmt|;
comment|// do nothing
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * from StreamJob.java    */
specifier|public
name|void
name|jobInfo
parameter_list|(
name|RunningJob
name|rj
parameter_list|)
block|{
if|if
condition|(
name|job
operator|.
name|get
argument_list|(
literal|"mapred.job.tracker"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"local"
argument_list|)
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Job running in-process (local Hadoop)"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|hp
init|=
name|job
operator|.
name|get
argument_list|(
literal|"mapred.job.tracker"
argument_list|)
decl_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Starting Job = "
operator|+
name|rj
operator|.
name|getJobID
argument_list|()
operator|+
literal|", Tracking URL = "
operator|+
name|rj
operator|.
name|getTrackingURL
argument_list|()
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Kill Command = "
operator|+
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
name|HADOOPBIN
argument_list|)
operator|+
literal|" job  -Dmapred.job.tracker="
operator|+
name|hp
operator|+
literal|" -kill "
operator|+
name|rj
operator|.
name|getJobID
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * from StreamJob.java    */
specifier|public
name|RunningJob
name|jobProgress
parameter_list|(
name|JobClient
name|jc
parameter_list|,
name|RunningJob
name|rj
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|lastReport
init|=
literal|""
decl_stmt|;
while|while
condition|(
operator|!
name|rj
operator|.
name|isComplete
argument_list|()
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{       }
name|rj
operator|=
name|jc
operator|.
name|getJob
argument_list|(
name|rj
operator|.
name|getJobID
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|report
init|=
literal|null
decl_stmt|;
name|report
operator|=
literal|" map = "
operator|+
name|Math
operator|.
name|round
argument_list|(
name|rj
operator|.
name|mapProgress
argument_list|()
operator|*
literal|100
argument_list|)
operator|+
literal|"%,  reduce ="
operator|+
name|Math
operator|.
name|round
argument_list|(
name|rj
operator|.
name|reduceProgress
argument_list|()
operator|*
literal|100
argument_list|)
operator|+
literal|"%"
expr_stmt|;
if|if
condition|(
operator|!
name|report
operator|.
name|equals
argument_list|(
name|lastReport
argument_list|)
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|report
argument_list|)
expr_stmt|;
name|lastReport
operator|=
name|report
expr_stmt|;
block|}
block|}
return|return
name|rj
return|;
block|}
specifier|private
name|void
name|inferNumReducers
parameter_list|()
throws|throws
name|Exception
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|job
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|work
operator|.
name|getReducer
argument_list|()
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|work
operator|.
name|getInferNumReducers
argument_list|()
operator|==
literal|true
operator|)
condition|)
block|{
name|long
name|inpSz
init|=
literal|0
decl_stmt|;
comment|// based on the input size - estimate the number of reducers
name|Path
index|[]
name|inputPaths
init|=
name|FileInputFormat
operator|.
name|getInputPaths
argument_list|(
name|job
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|inputP
range|:
name|inputPaths
control|)
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|inputP
argument_list|)
condition|)
block|{
name|FileStatus
index|[]
name|fStats
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|inputP
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|fStat
range|:
name|fStats
control|)
name|inpSz
operator|+=
name|fStat
operator|.
name|getLen
argument_list|()
expr_stmt|;
block|}
block|}
name|int
name|newRed
init|=
call|(
name|int
call|)
argument_list|(
name|inpSz
operator|/
name|LOAD_PER_REDUCER
argument_list|)
operator|+
literal|1
decl_stmt|;
if|if
condition|(
name|newRed
operator|<
name|work
operator|.
name|getNumReduceTasks
argument_list|()
operator|.
name|intValue
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Number of reduce tasks inferred based on input size to : "
operator|+
name|newRed
argument_list|)
expr_stmt|;
name|work
operator|.
name|setNumReduceTasks
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|newRed
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Execute a query plan using Hadoop    */
specifier|public
name|int
name|execute
parameter_list|()
block|{
name|fillInDefaults
argument_list|()
expr_stmt|;
name|String
name|invalidReason
init|=
name|work
operator|.
name|isInvalid
argument_list|()
decl_stmt|;
if|if
condition|(
name|invalidReason
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Plan invalid, Reason: "
operator|+
name|invalidReason
argument_list|)
throw|;
block|}
name|Utilities
operator|.
name|setMapRedWork
argument_list|(
name|job
argument_list|,
name|work
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|onefile
range|:
name|work
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding input file "
operator|+
name|onefile
argument_list|)
expr_stmt|;
name|FileInputFormat
operator|.
name|addInputPaths
argument_list|(
name|job
argument_list|,
name|onefile
argument_list|)
expr_stmt|;
block|}
name|String
name|hiveScratchDir
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
name|SCRATCHDIR
argument_list|)
decl_stmt|;
name|String
name|jobScratchDir
init|=
name|hiveScratchDir
operator|+
name|Utilities
operator|.
name|randGen
operator|.
name|nextInt
argument_list|()
decl_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|job
argument_list|,
operator|new
name|Path
argument_list|(
name|jobScratchDir
argument_list|)
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|ExecMapper
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|HiveKey
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
name|work
operator|.
name|getNumReduceTasks
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|job
operator|.
name|setReducerClass
argument_list|(
name|ExecReducer
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setInputFormat
argument_list|(
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
name|HiveInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// No-Op - we don't really write anything here ..
name|job
operator|.
name|setOutputKeyClass
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputValueClass
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|String
name|auxJars
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
name|HIVEAUXJARS
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|auxJars
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"adding libjars: "
operator|+
name|auxJars
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
literal|"tmpjars"
argument_list|,
name|auxJars
argument_list|)
expr_stmt|;
block|}
name|int
name|returnVal
init|=
literal|0
decl_stmt|;
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
name|RunningJob
name|rj
init|=
literal|null
decl_stmt|;
try|try
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|job
argument_list|)
expr_stmt|;
comment|// if the input is empty exit gracefully
name|Path
index|[]
name|inputPaths
init|=
name|FileInputFormat
operator|.
name|getInputPaths
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|boolean
name|emptyInput
init|=
literal|true
decl_stmt|;
for|for
control|(
name|Path
name|inputP
range|:
name|inputPaths
control|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|inputP
argument_list|)
condition|)
continue|continue;
name|FileStatus
index|[]
name|fStats
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|inputP
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|fStat
range|:
name|fStats
control|)
block|{
if|if
condition|(
name|fStat
operator|.
name|getLen
argument_list|()
operator|>
literal|0
condition|)
block|{
name|emptyInput
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
block|}
if|if
condition|(
name|emptyInput
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Job need not be submitted: no output: Success"
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
name|inferNumReducers
argument_list|()
expr_stmt|;
name|JobClient
name|jc
init|=
operator|new
name|JobClient
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|rj
operator|=
name|jc
operator|.
name|submitJob
argument_list|(
name|job
argument_list|)
expr_stmt|;
comment|// add to list of running jobs so in case of abnormal shutdown can kill it.
name|runningJobKillURIs
operator|.
name|put
argument_list|(
name|rj
operator|.
name|getJobID
argument_list|()
argument_list|,
name|rj
operator|.
name|getTrackingURL
argument_list|()
operator|+
literal|"&action=kill"
argument_list|)
expr_stmt|;
name|jobInfo
argument_list|(
name|rj
argument_list|)
expr_stmt|;
name|rj
operator|=
name|jobProgress
argument_list|(
name|jc
argument_list|,
name|rj
argument_list|)
expr_stmt|;
name|String
name|statusMesg
init|=
literal|"Ended Job = "
operator|+
name|rj
operator|.
name|getJobID
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|rj
operator|.
name|isSuccessful
argument_list|()
condition|)
block|{
name|statusMesg
operator|+=
literal|" with errors"
expr_stmt|;
name|returnVal
operator|=
literal|2
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
name|statusMesg
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|statusMesg
argument_list|)
expr_stmt|;
block|}
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
name|e
operator|.
name|getMessage
argument_list|()
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
comment|// Has to use full name to make sure it does not conflict with org.apache.commons.lang.StringUtils
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
name|returnVal
operator|=
literal|1
expr_stmt|;
block|}
finally|finally
block|{
name|Utilities
operator|.
name|clearMapRedWork
argument_list|(
name|job
argument_list|)
expr_stmt|;
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|jobScratchDir
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|returnVal
operator|!=
literal|0
operator|&&
name|rj
operator|!=
literal|null
condition|)
block|{
name|rj
operator|.
name|killJob
argument_list|()
expr_stmt|;
block|}
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
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{}
block|}
return|return
operator|(
name|returnVal
operator|)
return|;
block|}
specifier|private
specifier|static
name|void
name|printUsage
parameter_list|()
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"ExecDriver -plan<plan-file> [-jobconf k1=v1 [-jobconf k2=v2] ...]"
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
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
name|String
name|planFileName
init|=
literal|null
decl_stmt|;
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
name|boolean
name|isSilent
init|=
literal|false
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
literal|"-plan"
argument_list|)
condition|)
block|{
name|planFileName
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
literal|"-silent"
argument_list|)
condition|)
block|{
name|isSilent
operator|=
literal|true
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
name|planFileName
operator|==
literal|null
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Must specify Plan File Name"
argument_list|)
expr_stmt|;
name|printUsage
argument_list|()
expr_stmt|;
block|}
name|JobConf
name|conf
init|=
operator|new
name|JobConf
argument_list|(
name|ExecDriver
operator|.
name|class
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
name|conf
operator|.
name|set
argument_list|(
name|one
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|eqIndex
argument_list|)
argument_list|,
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
name|URI
name|pathURI
init|=
operator|(
operator|new
name|Path
argument_list|(
name|planFileName
argument_list|)
operator|)
operator|.
name|toUri
argument_list|()
decl_stmt|;
name|InputStream
name|pathData
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|pathURI
operator|.
name|getScheme
argument_list|()
argument_list|)
condition|)
block|{
comment|// default to local file system
name|pathData
operator|=
operator|new
name|FileInputStream
argument_list|(
name|planFileName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// otherwise may be in hadoop ..
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|pathData
operator|=
name|fs
operator|.
name|open
argument_list|(
operator|new
name|Path
argument_list|(
name|planFileName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|mapredWork
name|plan
init|=
name|Utilities
operator|.
name|deserializeMapRedWork
argument_list|(
name|pathData
argument_list|)
decl_stmt|;
name|ExecDriver
name|ed
init|=
operator|new
name|ExecDriver
argument_list|(
name|plan
argument_list|,
name|conf
argument_list|,
name|isSilent
argument_list|)
decl_stmt|;
name|int
name|ret
init|=
name|ed
operator|.
name|execute
argument_list|()
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
name|out
operator|.
name|println
argument_list|(
literal|"Job Failed"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Given a Hive Configuration object - generate a command line    * fragment for passing such configuration information to ExecDriver    */
specifier|public
specifier|static
name|String
name|generateCmdLine
parameter_list|(
name|HiveConf
name|hconf
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|Properties
name|deltaP
init|=
name|hconf
operator|.
name|getChangedProperties
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|one
range|:
name|deltaP
operator|.
name|keySet
argument_list|()
control|)
block|{
name|String
name|oneProp
init|=
operator|(
name|String
operator|)
name|one
decl_stmt|;
name|String
name|oneValue
init|=
name|deltaP
operator|.
name|getProperty
argument_list|(
name|oneProp
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"-jobconf "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|oneProp
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"="
argument_list|)
expr_stmt|;
try|try
block|{
name|sb
operator|.
name|append
argument_list|(
name|URLEncoder
operator|.
name|encode
argument_list|(
name|oneValue
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isMapRedTask
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasReduce
parameter_list|()
block|{
name|mapredWork
name|w
init|=
name|getWork
argument_list|()
decl_stmt|;
return|return
name|w
operator|.
name|getReducer
argument_list|()
operator|!=
literal|null
return|;
block|}
block|}
end_class

end_unit

