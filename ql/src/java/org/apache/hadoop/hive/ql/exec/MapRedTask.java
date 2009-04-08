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
name|FileOutputStream
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
name|Map
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
name|exec
operator|.
name|Utilities
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
name|commons
operator|.
name|lang
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Alternate implementation (to ExecDriver) of spawning a mapreduce task that runs it from  * a separate jvm. The primary issue with this is the inability to control logging from  * a separate jvm in a consistent manner  **/
end_comment

begin_class
specifier|public
class|class
name|MapRedTask
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
name|int
name|execute
parameter_list|()
block|{
try|try
block|{
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
name|auxJars
init|=
name|conf
operator|.
name|getAuxJars
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|auxJars
argument_list|)
condition|)
block|{
name|auxJars
operator|=
literal|" -libjars "
operator|+
name|auxJars
operator|+
literal|" "
expr_stmt|;
block|}
else|else
block|{
name|auxJars
operator|=
literal|" "
expr_stmt|;
block|}
name|mapredWork
name|plan
init|=
name|getWork
argument_list|()
decl_stmt|;
name|File
name|planFile
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"plan"
argument_list|,
literal|".xml"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Generating plan file "
operator|+
name|planFile
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|FileOutputStream
name|out
init|=
operator|new
name|FileOutputStream
argument_list|(
name|planFile
argument_list|)
decl_stmt|;
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
name|cmdLine
init|=
name|hadoopExec
operator|+
literal|" jar "
operator|+
name|auxJars
operator|+
literal|" "
operator|+
name|hiveJar
operator|+
literal|" org.apache.hadoop.hive.ql.exec.ExecDriver -plan "
operator|+
name|planFile
operator|.
name|toString
argument_list|()
operator|+
literal|" "
operator|+
name|hiveConfArgs
decl_stmt|;
name|String
name|files
init|=
name|ExecDriver
operator|.
name|getRealFiles
argument_list|(
name|conf
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
argument_list|)
expr_stmt|;
comment|// user specified the memory - only applicable for local mode
else|else
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|variables
init|=
name|System
operator|.
name|getenv
argument_list|()
decl_stmt|;
name|String
index|[]
name|env
init|=
operator|new
name|String
index|[
name|variables
operator|.
name|size
argument_list|()
operator|+
literal|1
index|]
decl_stmt|;
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
name|env
index|[
name|pos
index|]
operator|=
operator|new
name|String
argument_list|(
literal|"HADOOP_HEAPSIZE="
operator|+
name|hadoopMem
argument_list|)
expr_stmt|;
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
block|}
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

