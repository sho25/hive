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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|java
operator|.
name|util
operator|.
name|Properties
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
name|ContentSummary
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
name|exec
operator|.
name|Utilities
operator|.
name|StreamPrinter
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_comment
comment|/**  * Extension of ExecDriver:  * - can optionally spawn a map-reduce task from a separate jvm  * - will make last minute adjustments to map-reduce job parameters, viz:  *   * estimating number of reducers  *   * estimating whether job should run locally  **/
end_comment

begin_class
specifier|public
class|class
name|MapRedTask
extends|extends
name|ExecDriver
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
specifier|static
specifier|final
name|String
name|HADOOP_MEM_KEY
init|=
literal|"HADOOP_HEAPSIZE"
decl_stmt|;
specifier|static
specifier|final
name|String
name|HADOOP_OPTS_KEY
init|=
literal|"HADOOP_OPTS"
decl_stmt|;
specifier|static
specifier|final
name|String
index|[]
name|HIVE_SYS_PROP
init|=
block|{
literal|"build.dir"
block|,
literal|"build.dir.hive"
block|}
decl_stmt|;
specifier|private
specifier|transient
name|ContentSummary
name|inputSummary
init|=
literal|null
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|runningViaChild
init|=
literal|false
decl_stmt|;
specifier|public
name|MapRedTask
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|MapRedTask
parameter_list|(
name|MapredWork
name|plan
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|boolean
name|isSilent
parameter_list|)
throws|throws
name|HiveException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Illegal Constructor call"
argument_list|)
throw|;
block|}
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
name|conf
argument_list|)
expr_stmt|;
name|ctxCreated
operator|=
literal|true
expr_stmt|;
block|}
comment|// estimate number of reducers
name|setNumberOfReducers
argument_list|()
expr_stmt|;
comment|// auto-determine local mode if allowed
if|if
condition|(
operator|!
name|ctx
operator|.
name|isLocalOnlyExecutionMode
argument_list|()
operator|&&
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LOCALMODEAUTO
argument_list|)
condition|)
block|{
if|if
condition|(
name|inputSummary
operator|==
literal|null
condition|)
block|{
name|inputSummary
operator|=
name|Utilities
operator|.
name|getInputSummary
argument_list|(
name|driverContext
operator|.
name|getCtx
argument_list|()
argument_list|,
name|work
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// at this point the number of reducers is precisely defined in the plan
name|int
name|numReducers
init|=
name|work
operator|.
name|getNumReduceTasks
argument_list|()
decl_stmt|;
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
literal|"Task: "
operator|+
name|getId
argument_list|()
operator|+
literal|", Summary: "
operator|+
name|inputSummary
operator|.
name|getLength
argument_list|()
operator|+
literal|","
operator|+
name|inputSummary
operator|.
name|getFileCount
argument_list|()
operator|+
literal|","
operator|+
name|numReducers
argument_list|)
expr_stmt|;
block|}
name|String
name|reason
init|=
name|MapRedTask
operator|.
name|isEligibleForLocalMode
argument_list|(
name|conf
argument_list|,
name|inputSummary
argument_list|,
name|numReducers
argument_list|)
decl_stmt|;
if|if
condition|(
name|reason
operator|==
literal|null
condition|)
block|{
comment|// set the JT to local for the duration of this job
name|ctx
operator|.
name|setOriginalTracker
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPJT
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPJT
argument_list|,
literal|"local"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Selecting local mode for task: "
operator|+
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Cannot run job locally: "
operator|+
name|reason
argument_list|)
expr_stmt|;
block|}
block|}
name|runningViaChild
operator|=
literal|"local"
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPJT
argument_list|)
argument_list|)
operator|||
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SUBMITVIACHILD
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|runningViaChild
condition|)
block|{
comment|// we are not running this mapred task via child jvm
comment|// so directly invoke ExecDriver
return|return
name|super
operator|.
name|execute
argument_list|(
name|driverContext
argument_list|)
return|;
block|}
comment|// enable assertion
name|String
name|hadoopExec
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPBIN
argument_list|)
decl_stmt|;
name|String
name|hiveJar
init|=
name|conf
operator|.
name|getJar
argument_list|()
decl_stmt|;
name|String
name|libJarsOption
decl_stmt|;
name|String
name|addedJars
init|=
name|getResourceFiles
argument_list|(
name|conf
argument_list|,
name|SessionState
operator|.
name|ResourceType
operator|.
name|JAR
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVEADDEDJARS
argument_list|,
name|addedJars
argument_list|)
expr_stmt|;
name|String
name|auxJars
init|=
name|conf
operator|.
name|getAuxJars
argument_list|()
decl_stmt|;
comment|// Put auxjars and addedjars together into libjars
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|addedJars
argument_list|)
condition|)
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|auxJars
argument_list|)
condition|)
block|{
name|libJarsOption
operator|=
literal|" "
expr_stmt|;
block|}
else|else
block|{
name|libJarsOption
operator|=
literal|" -libjars "
operator|+
name|auxJars
operator|+
literal|" "
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|auxJars
argument_list|)
condition|)
block|{
name|libJarsOption
operator|=
literal|" -libjars "
operator|+
name|addedJars
operator|+
literal|" "
expr_stmt|;
block|}
else|else
block|{
name|libJarsOption
operator|=
literal|" -libjars "
operator|+
name|addedJars
operator|+
literal|","
operator|+
name|auxJars
operator|+
literal|" "
expr_stmt|;
block|}
block|}
comment|// Generate the hiveConfArgs after potentially adding the jars
name|String
name|hiveConfArgs
init|=
name|generateCmdLine
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// write out the plan to a local file
name|Path
name|planPath
init|=
operator|new
name|Path
argument_list|(
name|ctx
operator|.
name|getLocalTmpFileURI
argument_list|()
argument_list|,
literal|"plan.xml"
argument_list|)
decl_stmt|;
name|OutputStream
name|out
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
operator|.
name|create
argument_list|(
name|planPath
argument_list|)
decl_stmt|;
name|MapredWork
name|plan
init|=
name|getWork
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Generating plan file "
operator|+
name|planPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|serializeMapRedWork
argument_list|(
name|plan
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|String
name|isSilent
init|=
literal|"true"
operator|.
name|equalsIgnoreCase
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.silent"
argument_list|)
argument_list|)
condition|?
literal|"-nolog"
else|:
literal|""
decl_stmt|;
name|String
name|jarCmd
decl_stmt|;
if|if
condition|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|usesJobShell
argument_list|()
condition|)
block|{
name|jarCmd
operator|=
name|libJarsOption
operator|+
name|hiveJar
operator|+
literal|" "
operator|+
name|ExecDriver
operator|.
name|class
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|jarCmd
operator|=
name|hiveJar
operator|+
literal|" "
operator|+
name|ExecDriver
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
name|libJarsOption
expr_stmt|;
block|}
name|String
name|cmdLine
init|=
name|hadoopExec
operator|+
literal|" jar "
operator|+
name|jarCmd
operator|+
literal|" -plan "
operator|+
name|planPath
operator|.
name|toString
argument_list|()
operator|+
literal|" "
operator|+
name|isSilent
operator|+
literal|" "
operator|+
name|hiveConfArgs
decl_stmt|;
name|String
name|files
init|=
name|getResourceFiles
argument_list|(
name|conf
argument_list|,
name|SessionState
operator|.
name|ResourceType
operator|.
name|FILE
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|files
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|cmdLine
operator|=
name|cmdLine
operator|+
literal|" -files "
operator|+
name|files
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing: "
operator|+
name|cmdLine
argument_list|)
expr_stmt|;
name|Process
name|executor
init|=
literal|null
decl_stmt|;
comment|// Inherit Java system variables
name|String
name|hadoopOpts
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|Properties
name|p
init|=
name|System
operator|.
name|getProperties
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|element
range|:
name|HIVE_SYS_PROP
control|)
block|{
if|if
condition|(
name|p
operator|.
name|containsKey
argument_list|(
name|element
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" -D"
operator|+
name|element
operator|+
literal|"="
operator|+
name|p
operator|.
name|getProperty
argument_list|(
name|element
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|hadoopOpts
operator|=
name|sb
operator|.
name|toString
argument_list|()
expr_stmt|;
comment|// Inherit the environment variables
name|String
index|[]
name|env
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|variables
init|=
operator|new
name|HashMap
argument_list|(
name|System
operator|.
name|getenv
argument_list|()
argument_list|)
decl_stmt|;
comment|// The user can specify the hadoop memory
name|int
name|hadoopMem
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHADOOPMAXMEM
argument_list|)
decl_stmt|;
if|if
condition|(
name|hadoopMem
operator|==
literal|0
condition|)
block|{
name|variables
operator|.
name|remove
argument_list|(
name|HADOOP_MEM_KEY
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// user specified the memory - only applicable for local mode
name|variables
operator|.
name|put
argument_list|(
name|HADOOP_MEM_KEY
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|hadoopMem
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|variables
operator|.
name|containsKey
argument_list|(
name|HADOOP_OPTS_KEY
argument_list|)
condition|)
block|{
name|variables
operator|.
name|put
argument_list|(
name|HADOOP_OPTS_KEY
argument_list|,
name|variables
operator|.
name|get
argument_list|(
name|HADOOP_OPTS_KEY
argument_list|)
operator|+
name|hadoopOpts
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|variables
operator|.
name|put
argument_list|(
name|HADOOP_OPTS_KEY
argument_list|,
name|hadoopOpts
argument_list|)
expr_stmt|;
block|}
name|env
operator|=
operator|new
name|String
index|[
name|variables
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|variables
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|env
index|[
name|pos
operator|++
index|]
operator|=
name|name
operator|+
literal|"="
operator|+
name|value
expr_stmt|;
block|}
comment|// Run ExecDriver in another JVM
name|executor
operator|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|exec
argument_list|(
name|cmdLine
argument_list|,
name|env
argument_list|)
expr_stmt|;
name|StreamPrinter
name|outPrinter
init|=
operator|new
name|StreamPrinter
argument_list|(
name|executor
operator|.
name|getInputStream
argument_list|()
argument_list|,
literal|null
argument_list|,
name|System
operator|.
name|out
argument_list|)
decl_stmt|;
name|StreamPrinter
name|errPrinter
init|=
operator|new
name|StreamPrinter
argument_list|(
name|executor
operator|.
name|getErrorStream
argument_list|()
argument_list|,
literal|null
argument_list|,
name|System
operator|.
name|err
argument_list|)
decl_stmt|;
name|outPrinter
operator|.
name|start
argument_list|()
expr_stmt|;
name|errPrinter
operator|.
name|start
argument_list|()
expr_stmt|;
name|int
name|exitVal
init|=
name|executor
operator|.
name|waitFor
argument_list|()
decl_stmt|;
if|if
condition|(
name|exitVal
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Execution failed with exit status: "
operator|+
name|exitVal
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Execution completed successfully"
argument_list|)
expr_stmt|;
block|}
return|return
name|exitVal
return|;
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Exception: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|(
literal|1
operator|)
return|;
block|}
finally|finally
block|{
try|try
block|{
comment|// in case we decided to run everything in local mode, restore the
comment|// the jobtracker setting to its initial value
name|ctx
operator|.
name|restoreOriginalTracker
argument_list|()
expr_stmt|;
comment|// creating the context can create a bunch of files. So make
comment|// sure to clear it out
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
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Exception: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|mapStarted
parameter_list|()
block|{
name|boolean
name|b
init|=
name|super
operator|.
name|mapStarted
argument_list|()
decl_stmt|;
return|return
name|runningViaChild
condition|?
name|isdone
else|:
name|b
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|reduceStarted
parameter_list|()
block|{
name|boolean
name|b
init|=
name|super
operator|.
name|reduceStarted
argument_list|()
decl_stmt|;
return|return
name|runningViaChild
condition|?
name|isdone
else|:
name|b
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|mapDone
parameter_list|()
block|{
name|boolean
name|b
init|=
name|super
operator|.
name|mapDone
argument_list|()
decl_stmt|;
return|return
name|runningViaChild
condition|?
name|isdone
else|:
name|b
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|reduceDone
parameter_list|()
block|{
name|boolean
name|b
init|=
name|super
operator|.
name|reduceDone
argument_list|()
decl_stmt|;
return|return
name|runningViaChild
condition|?
name|isdone
else|:
name|b
return|;
block|}
comment|/**    * Set the number of reducers for the mapred work.    */
specifier|private
name|void
name|setNumberOfReducers
parameter_list|()
throws|throws
name|IOException
block|{
comment|// this is a temporary hack to fix things that are not fixed in the compiler
name|Integer
name|numReducersFromWork
init|=
name|work
operator|.
name|getNumReduceTasks
argument_list|()
decl_stmt|;
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
name|console
operator|.
name|printInfo
argument_list|(
literal|"Number of reduce tasks is set to 0 since there's no reduce operator"
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
if|if
condition|(
name|numReducersFromWork
operator|>=
literal|0
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Number of reduce tasks determined at compile time: "
operator|+
name|work
operator|.
name|getNumReduceTasks
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|job
operator|.
name|getNumReduceTasks
argument_list|()
operator|>
literal|0
condition|)
block|{
name|int
name|reducers
init|=
name|job
operator|.
name|getNumReduceTasks
argument_list|()
decl_stmt|;
name|work
operator|.
name|setNumReduceTasks
argument_list|(
name|reducers
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Number of reduce tasks not specified. Defaulting to jobconf value of: "
operator|+
name|reducers
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|reducers
init|=
name|estimateNumberOfReducers
argument_list|()
decl_stmt|;
name|work
operator|.
name|setNumReduceTasks
argument_list|(
name|reducers
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Number of reduce tasks not specified. Estimated from input data size: "
operator|+
name|reducers
argument_list|)
expr_stmt|;
block|}
name|console
operator|.
name|printInfo
argument_list|(
literal|"In order to change the average load for a reducer (in bytes):"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"  set "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|BYTESPERREDUCER
operator|.
name|varname
operator|+
literal|"=<number>"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"In order to limit the maximum number of reducers:"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"  set "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAXREDUCERS
operator|.
name|varname
operator|+
literal|"=<number>"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"In order to set a constant number of reducers:"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"  set "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPNUMREDUCERS
operator|+
literal|"=<number>"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Estimate the number of reducers needed for this job, based on job input,    * and configuration parameters.    *    * @return the number of reducers.    */
specifier|private
name|int
name|estimateNumberOfReducers
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|bytesPerReducer
init|=
name|conf
operator|.
name|getLongVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|BYTESPERREDUCER
argument_list|)
decl_stmt|;
name|int
name|maxReducers
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAXREDUCERS
argument_list|)
decl_stmt|;
if|if
condition|(
name|inputSummary
operator|==
literal|null
condition|)
block|{
comment|// compute the summary and stash it away
name|inputSummary
operator|=
name|Utilities
operator|.
name|getInputSummary
argument_list|(
name|driverContext
operator|.
name|getCtx
argument_list|()
argument_list|,
name|work
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|long
name|totalInputFileSize
init|=
name|inputSummary
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"BytesPerReducer="
operator|+
name|bytesPerReducer
operator|+
literal|" maxReducers="
operator|+
name|maxReducers
operator|+
literal|" totalInputFileSize="
operator|+
name|totalInputFileSize
argument_list|)
expr_stmt|;
name|int
name|reducers
init|=
call|(
name|int
call|)
argument_list|(
operator|(
name|totalInputFileSize
operator|+
name|bytesPerReducer
operator|-
literal|1
operator|)
operator|/
name|bytesPerReducer
argument_list|)
decl_stmt|;
name|reducers
operator|=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|reducers
argument_list|)
expr_stmt|;
name|reducers
operator|=
name|Math
operator|.
name|min
argument_list|(
name|maxReducers
argument_list|,
name|reducers
argument_list|)
expr_stmt|;
return|return
name|reducers
return|;
block|}
comment|/**    * Find out if a job can be run in local mode based on it's characteristics    *    * @param conf Hive Configuration    * @param inputSummary summary about the input files for this job    * @param numReducers total number of reducers for this job    * @return String null if job is eligible for local mode, reason otherwise    */
specifier|public
specifier|static
name|String
name|isEligibleForLocalMode
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|ContentSummary
name|inputSummary
parameter_list|,
name|int
name|numReducers
parameter_list|)
block|{
name|long
name|maxBytes
init|=
name|conf
operator|.
name|getLongVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LOCALMODEMAXBYTES
argument_list|)
decl_stmt|;
name|long
name|maxTasks
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LOCALMODEMAXTASKS
argument_list|)
decl_stmt|;
comment|// check for max input size
if|if
condition|(
name|inputSummary
operator|.
name|getLength
argument_list|()
operator|>
name|maxBytes
condition|)
block|{
return|return
literal|"Input Size (= "
operator|+
name|inputSummary
operator|.
name|getLength
argument_list|()
operator|+
literal|") is larger than "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LOCALMODEMAXBYTES
operator|.
name|varname
operator|+
literal|" (= "
operator|+
name|maxBytes
operator|+
literal|")"
return|;
block|}
comment|// ideally we would like to do this check based on the number of splits
comment|// in the absence of an easy way to get the number of splits - do this
comment|// based on the total number of files (pessimistically assumming that
comment|// splits are equal to number of files in worst case)
if|if
condition|(
name|inputSummary
operator|.
name|getFileCount
argument_list|()
operator|>
name|maxTasks
condition|)
block|{
return|return
literal|"Number of Input Files (= "
operator|+
name|inputSummary
operator|.
name|getFileCount
argument_list|()
operator|+
literal|") is larger than "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LOCALMODEMAXTASKS
operator|.
name|varname
operator|+
literal|"(= "
operator|+
name|maxTasks
operator|+
literal|")"
return|;
block|}
comment|// since local mode only runs with 1 reducers - make sure that the
comment|// the number of reducers (set by user or inferred) is<=1
if|if
condition|(
name|numReducers
operator|>
literal|1
condition|)
block|{
return|return
literal|"Number of reducers (= "
operator|+
name|numReducers
operator|+
literal|") is more than 1"
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

