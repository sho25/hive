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
name|ByteArrayOutputStream
import|;
end_import

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
name|concurrent
operator|.
name|Semaphore
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
name|CommandLine
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
name|DefaultExecutor
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
name|commons
operator|.
name|exec
operator|.
name|ExecuteWatchdog
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
name|PumpStreamHandler
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

begin_comment
comment|/**  * Execute a local program.  This is a singleton service that will  * execute programs as non-privileged users on the local box.  See  * ExecService.run and ExecService.runUnlimited for details.  */
end_comment

begin_class
specifier|public
class|class
name|ExecServiceImpl
implements|implements
name|ExecService
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
name|ExecServiceImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|AppConfig
name|appConf
init|=
name|Main
operator|.
name|getAppConfigInstance
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|volatile
name|ExecServiceImpl
name|theSingleton
decl_stmt|;
comment|/**      * Retrieve the singleton.      */
specifier|public
specifier|static
specifier|synchronized
name|ExecServiceImpl
name|getInstance
parameter_list|()
block|{
if|if
condition|(
name|theSingleton
operator|==
literal|null
condition|)
block|{
name|theSingleton
operator|=
operator|new
name|ExecServiceImpl
argument_list|()
expr_stmt|;
block|}
return|return
name|theSingleton
return|;
block|}
specifier|private
name|Semaphore
name|avail
decl_stmt|;
specifier|private
name|ExecServiceImpl
parameter_list|()
block|{
name|avail
operator|=
operator|new
name|Semaphore
argument_list|(
name|appConf
operator|.
name|getInt
argument_list|(
name|AppConfig
operator|.
name|EXEC_MAX_PROCS_NAME
argument_list|,
literal|16
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Run the program synchronously as the given user. We rate limit      * the number of processes that can simultaneously created for      * this instance.      *      * @param program   The program to run      * @param args      Arguments to pass to the program      * @param env       Any extra environment variables to set      * @return The result of the run.      */
specifier|public
name|ExecBean
name|run
parameter_list|(
name|String
name|program
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
parameter_list|)
throws|throws
name|NotAuthorizedException
throws|,
name|BusyException
throws|,
name|ExecuteException
throws|,
name|IOException
block|{
name|boolean
name|aquired
init|=
literal|false
decl_stmt|;
try|try
block|{
name|aquired
operator|=
name|avail
operator|.
name|tryAcquire
argument_list|()
expr_stmt|;
if|if
condition|(
name|aquired
condition|)
block|{
return|return
name|runUnlimited
argument_list|(
name|program
argument_list|,
name|args
argument_list|,
name|env
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|BusyException
argument_list|()
throw|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|aquired
condition|)
block|{
name|avail
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Run the program synchronously as the given user.  Warning:      * CommandLine will trim the argument strings.      *      * @param program   The program to run.      * @param args      Arguments to pass to the program      * @param env       Any extra environment variables to set      * @return The result of the run.      */
specifier|public
name|ExecBean
name|runUnlimited
parameter_list|(
name|String
name|program
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
parameter_list|)
throws|throws
name|NotAuthorizedException
throws|,
name|ExecuteException
throws|,
name|IOException
block|{
try|try
block|{
return|return
name|auxRun
argument_list|(
name|program
argument_list|,
name|args
argument_list|,
name|env
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|File
name|cwd
init|=
operator|new
name|java
operator|.
name|io
operator|.
name|File
argument_list|(
literal|"."
argument_list|)
decl_stmt|;
if|if
condition|(
name|cwd
operator|.
name|canRead
argument_list|()
operator|&&
name|cwd
operator|.
name|canWrite
argument_list|()
condition|)
throw|throw
name|e
throw|;
else|else
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Invalid permissions on Templeton directory: "
operator|+
name|cwd
operator|.
name|getCanonicalPath
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
name|ExecBean
name|auxRun
parameter_list|(
name|String
name|program
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
parameter_list|)
throws|throws
name|NotAuthorizedException
throws|,
name|ExecuteException
throws|,
name|IOException
block|{
name|DefaultExecutor
name|executor
init|=
operator|new
name|DefaultExecutor
argument_list|()
decl_stmt|;
name|executor
operator|.
name|setExitValues
argument_list|(
literal|null
argument_list|)
expr_stmt|;
comment|// Setup stdout and stderr
name|int
name|nbytes
init|=
name|appConf
operator|.
name|getInt
argument_list|(
name|AppConfig
operator|.
name|EXEC_MAX_BYTES_NAME
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|outStream
init|=
operator|new
name|MaxByteArrayOutputStream
argument_list|(
name|nbytes
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|errStream
init|=
operator|new
name|MaxByteArrayOutputStream
argument_list|(
name|nbytes
argument_list|)
decl_stmt|;
name|executor
operator|.
name|setStreamHandler
argument_list|(
operator|new
name|PumpStreamHandler
argument_list|(
name|outStream
argument_list|,
name|errStream
argument_list|)
argument_list|)
expr_stmt|;
comment|// Only run for N milliseconds
name|int
name|timeout
init|=
name|appConf
operator|.
name|getInt
argument_list|(
name|AppConfig
operator|.
name|EXEC_TIMEOUT_NAME
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|ExecuteWatchdog
name|watchdog
init|=
operator|new
name|ExecuteWatchdog
argument_list|(
name|timeout
argument_list|)
decl_stmt|;
name|executor
operator|.
name|setWatchdog
argument_list|(
name|watchdog
argument_list|)
expr_stmt|;
name|CommandLine
name|cmd
init|=
name|makeCommandLine
argument_list|(
name|program
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Running: "
operator|+
name|cmd
argument_list|)
expr_stmt|;
name|ExecBean
name|res
init|=
operator|new
name|ExecBean
argument_list|()
decl_stmt|;
name|res
operator|.
name|exitcode
operator|=
name|executor
operator|.
name|execute
argument_list|(
name|cmd
argument_list|,
name|execEnv
argument_list|(
name|env
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|enc
init|=
name|appConf
operator|.
name|get
argument_list|(
name|AppConfig
operator|.
name|EXEC_ENCODING_NAME
argument_list|)
decl_stmt|;
name|res
operator|.
name|stdout
operator|=
name|outStream
operator|.
name|toString
argument_list|(
name|enc
argument_list|)
expr_stmt|;
name|res
operator|.
name|stderr
operator|=
name|errStream
operator|.
name|toString
argument_list|(
name|enc
argument_list|)
expr_stmt|;
return|return
name|res
return|;
block|}
specifier|private
name|CommandLine
name|makeCommandLine
parameter_list|(
name|String
name|program
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|)
throws|throws
name|NotAuthorizedException
throws|,
name|IOException
block|{
name|String
name|path
init|=
name|validateProgram
argument_list|(
name|program
argument_list|)
decl_stmt|;
name|CommandLine
name|cmd
init|=
operator|new
name|CommandLine
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|args
operator|!=
literal|null
condition|)
for|for
control|(
name|String
name|arg
range|:
name|args
control|)
name|cmd
operator|.
name|addArgument
argument_list|(
name|arg
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
name|cmd
return|;
block|}
comment|/**      * Build the environment used for all exec calls.      *      * @return The environment variables.      */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|execEnv
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
parameter_list|)
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|res
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
for|for
control|(
name|String
name|key
range|:
name|appConf
operator|.
name|getStrings
argument_list|(
name|AppConfig
operator|.
name|EXEC_ENVS_NAME
argument_list|)
control|)
block|{
name|String
name|val
init|=
name|System
operator|.
name|getenv
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|!=
literal|null
condition|)
block|{
name|res
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|env
operator|!=
literal|null
condition|)
name|res
operator|.
name|putAll
argument_list|(
name|env
argument_list|)
expr_stmt|;
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
name|envs
range|:
name|res
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Env "
operator|+
name|envs
operator|.
name|getKey
argument_list|()
operator|+
literal|"="
operator|+
name|envs
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
comment|/**      * Given a program name, lookup the fully qualified path.  Throws      * an exception if the program is missing or not authorized.      *      * @param path      The path of the program.      * @return The path of the validated program.      */
specifier|public
name|String
name|validateProgram
parameter_list|(
name|String
name|path
parameter_list|)
throws|throws
name|NotAuthorizedException
throws|,
name|IOException
block|{
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|f
operator|.
name|canExecute
argument_list|()
condition|)
block|{
return|return
name|f
operator|.
name|getCanonicalPath
argument_list|()
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|NotAuthorizedException
argument_list|(
literal|"Unable to access program: "
operator|+
name|path
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

