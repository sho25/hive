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
name|File
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
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryMXBean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|SimpleDateFormat
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
name|Calendar
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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|FileUtil
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
name|exec
operator|.
name|persistence
operator|.
name|AbstractMapJoinKey
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
name|persistence
operator|.
name|HashMapWrapper
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
name|persistence
operator|.
name|MapJoinObjectValue
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
name|FetchWork
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
name|MapredLocalWork
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
name|MapredLocalWork
operator|.
name|BucketMapJoinContext
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
name|serde2
operator|.
name|ColumnProjectionUtils
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
name|serde2
operator|.
name|objectinspector
operator|.
name|InspectableObject
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_class
specifier|public
class|class
name|MapredLocalTask
extends|extends
name|Task
argument_list|<
name|MapredLocalWork
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|FetchOperator
argument_list|>
name|fetchOperators
decl_stmt|;
specifier|private
name|JobConf
name|job
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Log
name|l4j
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"MapredLocalTask"
argument_list|)
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
specifier|public
specifier|static
name|MemoryMXBean
name|memoryMXBean
decl_stmt|;
comment|// not sure we need this exec context; but all the operators in the work
comment|// will pass this context throught
specifier|private
specifier|final
name|ExecMapperContext
name|execContext
init|=
operator|new
name|ExecMapperContext
argument_list|()
decl_stmt|;
specifier|public
name|MapredLocalTask
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|MapredLocalTask
parameter_list|(
name|MapredLocalWork
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
name|ExecDriver
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|String
name|now
parameter_list|()
block|{
name|Calendar
name|cal
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|SimpleDateFormat
name|sdf
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-mm-dd hh:mm:ss"
argument_list|)
decl_stmt|;
return|return
name|sdf
operator|.
name|format
argument_list|(
name|cal
operator|.
name|getTime
argument_list|()
argument_list|)
return|;
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
try|try
block|{
comment|// generate the cmd line to run in the child jvm
name|Context
name|ctx
init|=
name|driverContext
operator|.
name|getCtx
argument_list|()
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
name|libJarsOption
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
name|MapredLocalWork
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
name|serializeMapRedLocalWork
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
expr_stmt|;
name|String
name|hiveConfArgs
init|=
name|ExecDriver
operator|.
name|generateCmdLine
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|String
name|cmdLine
init|=
name|hadoopExec
operator|+
literal|" jar "
operator|+
name|jarCmd
operator|+
literal|" -localtask -plan "
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
name|workDir
init|=
operator|(
operator|new
name|File
argument_list|(
literal|"."
argument_list|)
operator|)
operator|.
name|getCanonicalPath
argument_list|()
decl_stmt|;
name|String
name|files
init|=
name|ExecDriver
operator|.
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
name|workDir
operator|=
operator|(
operator|new
name|Path
argument_list|(
name|ctx
operator|.
name|getLocalTmpFileURI
argument_list|()
argument_list|)
operator|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
operator|(
operator|new
name|File
argument_list|(
name|workDir
argument_list|)
operator|)
operator|.
name|mkdir
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot create tmp working dir: "
operator|+
name|workDir
argument_list|)
throw|;
block|}
for|for
control|(
name|String
name|f
range|:
name|StringUtils
operator|.
name|split
argument_list|(
name|files
argument_list|,
literal|','
argument_list|)
control|)
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|f
argument_list|)
decl_stmt|;
name|String
name|target
init|=
name|p
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|String
name|link
init|=
name|workDir
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|p
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|FileUtil
operator|.
name|symLink
argument_list|(
name|target
argument_list|,
name|link
argument_list|)
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot link to added file: "
operator|+
name|target
operator|+
literal|" from: "
operator|+
name|link
argument_list|)
throw|;
block|}
block|}
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
comment|// if ("local".equals(conf.getVar(HiveConf.ConfVars.HADOOPJT))) {
comment|// if we are running in local mode - then the amount of memory used
comment|// by the child jvm can no longer default to the memory used by the
comment|// parent jvm
comment|// int hadoopMem = conf.getIntVar(HiveConf.ConfVars.HIVEHADOOPMAXMEM);
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
comment|// remove env var that would default child jvm to use parent's memory
comment|// as default. child jvm would use default memory for a hadoop client
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
comment|// user specified the memory for local mode hadoop run
name|console
operator|.
name|printInfo
argument_list|(
literal|" set heap size\t"
operator|+
name|hadoopMem
operator|+
literal|"MB"
argument_list|)
expr_stmt|;
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
comment|// } else {
comment|// nothing to do - we are not running in local mode - only submitting
comment|// the job via a child process. in this case it's appropriate that the
comment|// child jvm use the same memory as the parent jvm
comment|// }
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
argument_list|,
operator|new
name|File
argument_list|(
name|workDir
argument_list|)
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
name|console
operator|.
name|printInfo
argument_list|(
literal|"Mapred Local Task Succeeded . Convert the Join into MapJoin"
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
block|}
specifier|public
name|int
name|executeFromChildJVM
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
comment|// check the local work
if|if
condition|(
name|work
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|memoryMXBean
operator|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|Utilities
operator|.
name|now
argument_list|()
operator|+
literal|"\tStarting to launch local task to process map join;\tmaximum memory = "
operator|+
name|memoryMXBean
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
name|fetchOperators
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|FetchOperator
argument_list|>
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|FetchOperator
argument_list|,
name|JobConf
argument_list|>
name|fetchOpJobConfMap
init|=
operator|new
name|HashMap
argument_list|<
name|FetchOperator
argument_list|,
name|JobConf
argument_list|>
argument_list|()
decl_stmt|;
name|execContext
operator|.
name|setJc
argument_list|(
name|job
argument_list|)
expr_stmt|;
comment|// set the local work, so all the operator can get this context
name|execContext
operator|.
name|setLocalWork
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|boolean
name|inputFileChangeSenstive
init|=
name|work
operator|.
name|getInputFileChangeSensitive
argument_list|()
decl_stmt|;
try|try
block|{
name|initializeOperators
argument_list|(
name|fetchOpJobConfMap
argument_list|)
expr_stmt|;
comment|// for each big table's bucket, call the start forward
if|if
condition|(
name|inputFileChangeSenstive
condition|)
block|{
for|for
control|(
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|bigTableBucketFiles
range|:
name|work
operator|.
name|getBucketMapjoinContext
argument_list|()
operator|.
name|getAliasBucketFileNameMapping
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|bigTableBucket
range|:
name|bigTableBucketFiles
operator|.
name|keySet
argument_list|()
control|)
block|{
name|startForward
argument_list|(
name|inputFileChangeSenstive
argument_list|,
name|bigTableBucket
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|startForward
argument_list|(
name|inputFileChangeSenstive
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|long
name|currentTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|elapsed
init|=
name|currentTime
operator|-
name|startTime
decl_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|Utilities
operator|.
name|now
argument_list|()
operator|+
literal|"\tEnd of local task; Time Taken: "
operator|+
name|Utilities
operator|.
name|showTime
argument_list|(
name|elapsed
argument_list|)
operator|+
literal|" sec."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|OutOfMemoryError
operator|||
operator|(
name|e
operator|instanceof
name|HiveException
operator|&&
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|equals
argument_list|(
literal|"RunOutOfMeomoryUsage"
argument_list|)
operator|)
condition|)
block|{
comment|// Don't create a new object if we are already out of memory
return|return
literal|3
return|;
block|}
else|else
block|{
name|l4j
operator|.
name|error
argument_list|(
literal|"Hive Runtime Error: Map local work failed"
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
return|return
literal|2
return|;
block|}
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|void
name|startForward
parameter_list|(
name|boolean
name|inputFileChangeSenstive
parameter_list|,
name|String
name|bigTableBucket
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|FetchOperator
argument_list|>
name|entry
range|:
name|fetchOperators
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|int
name|fetchOpRows
init|=
literal|0
decl_stmt|;
name|String
name|alias
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|FetchOperator
name|fetchOp
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|inputFileChangeSenstive
condition|)
block|{
name|fetchOp
operator|.
name|clearFetchContext
argument_list|()
expr_stmt|;
name|setUpFetchOpContext
argument_list|(
name|fetchOp
argument_list|,
name|alias
argument_list|,
name|bigTableBucket
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fetchOp
operator|.
name|isEmptyTable
argument_list|()
condition|)
block|{
comment|//generate empty hashtable for empty table
name|this
operator|.
name|generateDummyHashTable
argument_list|(
name|alias
argument_list|,
name|bigTableBucket
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// get the root operator
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|forwardOp
init|=
name|work
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
comment|// walk through the operator tree
while|while
condition|(
literal|true
condition|)
block|{
name|InspectableObject
name|row
init|=
name|fetchOp
operator|.
name|getNextRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|row
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|inputFileChangeSenstive
condition|)
block|{
name|String
name|fileName
init|=
name|this
operator|.
name|getFileName
argument_list|(
name|bigTableBucket
argument_list|)
decl_stmt|;
name|execContext
operator|.
name|setCurrentBigBucketFile
argument_list|(
name|fileName
argument_list|)
expr_stmt|;
name|forwardOp
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
name|forwardOp
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
break|break;
block|}
name|fetchOpRows
operator|++
expr_stmt|;
name|forwardOp
operator|.
name|process
argument_list|(
name|row
operator|.
name|o
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// check if any operator had a fatal error or early exit during
comment|// execution
if|if
condition|(
name|forwardOp
operator|.
name|getDone
argument_list|()
condition|)
block|{
comment|// ExecMapper.setDone(true);
break|break;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|initializeOperators
parameter_list|(
name|Map
argument_list|<
name|FetchOperator
argument_list|,
name|JobConf
argument_list|>
name|fetchOpJobConfMap
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// this mapper operator is used to initialize all the operators
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|FetchWork
argument_list|>
name|entry
range|:
name|work
operator|.
name|getAliasToFetchWork
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|JobConf
name|jobClone
init|=
operator|new
name|JobConf
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tableScan
init|=
name|work
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|setColumnsNeeded
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|tableScan
operator|instanceof
name|TableScanOperator
condition|)
block|{
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|list
init|=
operator|(
operator|(
name|TableScanOperator
operator|)
name|tableScan
operator|)
operator|.
name|getNeededColumnIDs
argument_list|()
decl_stmt|;
if|if
condition|(
name|list
operator|!=
literal|null
condition|)
block|{
name|ColumnProjectionUtils
operator|.
name|appendReadColumnIDs
argument_list|(
name|jobClone
argument_list|,
name|list
argument_list|)
expr_stmt|;
name|setColumnsNeeded
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|setColumnsNeeded
condition|)
block|{
name|ColumnProjectionUtils
operator|.
name|setFullyReadColumns
argument_list|(
name|jobClone
argument_list|)
expr_stmt|;
block|}
comment|// create a fetch operator
name|FetchOperator
name|fetchOp
init|=
operator|new
name|FetchOperator
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|jobClone
argument_list|)
decl_stmt|;
name|fetchOpJobConfMap
operator|.
name|put
argument_list|(
name|fetchOp
argument_list|,
name|jobClone
argument_list|)
expr_stmt|;
name|fetchOperators
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|fetchOp
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"fetchoperator for "
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|" created"
argument_list|)
expr_stmt|;
block|}
comment|// initilize all forward operator
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|FetchOperator
argument_list|>
name|entry
range|:
name|fetchOperators
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// get the forward op
name|String
name|alias
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|forwardOp
init|=
name|work
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
comment|// put the exe context into all the operators
name|forwardOp
operator|.
name|setExecContext
argument_list|(
name|execContext
argument_list|)
expr_stmt|;
comment|// All the operators need to be initialized before process
name|FetchOperator
name|fetchOp
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|JobConf
name|jobConf
init|=
name|fetchOpJobConfMap
operator|.
name|get
argument_list|(
name|fetchOp
argument_list|)
decl_stmt|;
if|if
condition|(
name|jobConf
operator|==
literal|null
condition|)
block|{
name|jobConf
operator|=
name|job
expr_stmt|;
block|}
comment|// initialize the forward operator
name|ObjectInspector
name|objectInspector
init|=
name|fetchOp
operator|.
name|getOutputObjectInspector
argument_list|()
decl_stmt|;
if|if
condition|(
name|objectInspector
operator|!=
literal|null
condition|)
block|{
name|forwardOp
operator|.
name|initialize
argument_list|(
name|jobConf
argument_list|,
operator|new
name|ObjectInspector
index|[]
block|{
name|objectInspector
block|}
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"fetchoperator for "
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|" initialized"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fetchOp
operator|.
name|setEmptyTable
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|generateDummyHashTable
parameter_list|(
name|String
name|alias
parameter_list|,
name|String
name|bigBucketFileName
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
comment|// find the (byte)tag for the map join(HashTableSinkOperator)
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|parentOp
init|=
name|work
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|childOp
init|=
name|parentOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
while|while
condition|(
operator|(
name|childOp
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
operator|(
name|childOp
operator|instanceof
name|HashTableSinkOperator
operator|)
operator|)
condition|)
block|{
name|parentOp
operator|=
name|childOp
expr_stmt|;
assert|assert
name|parentOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|childOp
operator|=
name|parentOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|childOp
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Cannot find HashTableSink op by tracing down the table scan operator tree"
argument_list|)
throw|;
block|}
name|byte
name|tag
init|=
operator|(
name|byte
operator|)
name|childOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|indexOf
argument_list|(
name|parentOp
argument_list|)
decl_stmt|;
comment|// generate empty hashtable for this (byte)tag
name|String
name|tmpURI
init|=
name|this
operator|.
name|getWork
argument_list|()
operator|.
name|getTmpFileURI
argument_list|()
decl_stmt|;
name|HashMapWrapper
argument_list|<
name|AbstractMapJoinKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
name|hashTable
init|=
operator|new
name|HashMapWrapper
argument_list|<
name|AbstractMapJoinKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|bigBucketFileName
operator|==
literal|null
operator|||
name|bigBucketFileName
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|bigBucketFileName
operator|=
literal|"-"
expr_stmt|;
block|}
name|String
name|tmpURIPath
init|=
name|Utilities
operator|.
name|generatePath
argument_list|(
name|tmpURI
argument_list|,
name|tag
argument_list|,
name|bigBucketFileName
argument_list|)
decl_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|Utilities
operator|.
name|now
argument_list|()
operator|+
literal|"\tDump the hashtable into file: "
operator|+
name|tmpURIPath
argument_list|)
expr_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|tmpURIPath
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|create
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|long
name|fileLength
init|=
name|hashTable
operator|.
name|flushMemoryCacheToPersistent
argument_list|(
name|file
argument_list|)
decl_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|Utilities
operator|.
name|now
argument_list|()
operator|+
literal|"\tUpload 1 File to: "
operator|+
name|tmpURIPath
operator|+
literal|" File size: "
operator|+
name|fileLength
argument_list|)
expr_stmt|;
name|hashTable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|setUpFetchOpContext
parameter_list|(
name|FetchOperator
name|fetchOp
parameter_list|,
name|String
name|alias
parameter_list|,
name|String
name|currentInputFile
parameter_list|)
throws|throws
name|Exception
block|{
name|BucketMapJoinContext
name|bucketMatcherCxt
init|=
name|this
operator|.
name|work
operator|.
name|getBucketMapjoinContext
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|BucketMatcher
argument_list|>
name|bucketMatcherCls
init|=
name|bucketMatcherCxt
operator|.
name|getBucketMatcherClass
argument_list|()
decl_stmt|;
name|BucketMatcher
name|bucketMatcher
init|=
operator|(
name|BucketMatcher
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|bucketMatcherCls
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|bucketMatcher
operator|.
name|setAliasBucketFileNameMapping
argument_list|(
name|bucketMatcherCxt
operator|.
name|getAliasBucketFileNameMapping
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|aliasFiles
init|=
name|bucketMatcher
operator|.
name|getAliasBucketFiles
argument_list|(
name|currentInputFile
argument_list|,
name|bucketMatcherCxt
operator|.
name|getMapJoinBigTableAlias
argument_list|()
argument_list|,
name|alias
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|Path
argument_list|>
name|iter
init|=
name|aliasFiles
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|fetchOp
operator|.
name|setupContext
argument_list|(
name|iter
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|getFileName
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|==
literal|null
operator|||
name|path
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|last_separator
init|=
name|path
operator|.
name|lastIndexOf
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
operator|+
literal|1
decl_stmt|;
name|String
name|fileName
init|=
name|path
operator|.
name|substring
argument_list|(
name|last_separator
argument_list|)
decl_stmt|;
return|return
name|fileName
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|localizeMRTmpFilesImpl
parameter_list|(
name|Context
name|ctx
parameter_list|)
block|{    }
annotation|@
name|Override
specifier|public
name|boolean
name|isMapRedLocalTask
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"MAPREDLOCAL"
return|;
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
comment|//assert false;
return|return
name|StageType
operator|.
name|MAPREDLOCAL
return|;
block|}
block|}
end_class

end_unit

