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
name|cli
package|;
end_package

begin_import
import|import
name|jline
operator|.
name|*
import|;
end_import

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FsShell
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
name|conf
operator|.
name|Configuration
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
name|Driver
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
name|ql
operator|.
name|processors
operator|.
name|CommandProcessor
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
name|processors
operator|.
name|CommandProcessorFactory
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
name|hive
operator|.
name|shims
operator|.
name|ShimLoader
import|;
end_import

begin_class
specifier|public
class|class
name|CliDriver
block|{
specifier|public
specifier|final
specifier|static
name|String
name|prompt
init|=
literal|"hive"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|String
name|prompt2
init|=
literal|"    "
decl_stmt|;
comment|// when ';' is not yet seen
specifier|private
name|LogHelper
name|console
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|CliDriver
parameter_list|()
block|{
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|conf
operator|=
operator|(
name|ss
operator|!=
literal|null
operator|)
condition|?
name|ss
operator|.
name|getConf
argument_list|()
else|:
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"CliDriver"
argument_list|)
decl_stmt|;
name|console
operator|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|processCmd
parameter_list|(
name|String
name|cmd
parameter_list|)
block|{
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|String
name|cmd_trimmed
init|=
name|cmd
operator|.
name|trim
argument_list|()
decl_stmt|;
name|String
index|[]
name|tokens
init|=
name|cmd_trimmed
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|)
decl_stmt|;
name|String
name|cmd_1
init|=
name|cmd_trimmed
operator|.
name|substring
argument_list|(
name|tokens
index|[
literal|0
index|]
operator|.
name|length
argument_list|()
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|int
name|ret
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|cmd_trimmed
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"quit"
argument_list|)
operator|||
name|cmd_trimmed
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"exit"
argument_list|)
condition|)
block|{
comment|// if we have come this far - either the previous commands
comment|// are all successful or this is command line. in either case
comment|// this counts as a successful run
name|System
operator|.
name|exit
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd_trimmed
operator|.
name|startsWith
argument_list|(
literal|"!"
argument_list|)
condition|)
block|{
name|String
name|shell_cmd
init|=
name|cmd_trimmed
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|//shell_cmd = "/bin/bash -c \'" + shell_cmd + "\'";
try|try
block|{
name|Process
name|executor
init|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|exec
argument_list|(
name|shell_cmd
argument_list|)
decl_stmt|;
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
name|ss
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
name|ss
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
name|ret
operator|=
name|executor
operator|.
name|waitFor
argument_list|()
expr_stmt|;
if|if
condition|(
name|ret
operator|!=
literal|0
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Command failed with exit code = "
operator|+
name|ret
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
name|console
operator|.
name|printError
argument_list|(
literal|"Exception raised from Shell command "
operator|+
name|e
operator|.
name|getLocalizedMessage
argument_list|()
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
name|ret
operator|=
literal|1
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|tokens
index|[
literal|0
index|]
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"list"
argument_list|)
condition|)
block|{
name|SessionState
operator|.
name|ResourceType
name|t
decl_stmt|;
if|if
condition|(
name|tokens
operator|.
name|length
operator|<
literal|2
operator|||
operator|(
name|t
operator|=
name|SessionState
operator|.
name|find_resource_type
argument_list|(
name|tokens
index|[
literal|1
index|]
argument_list|)
operator|)
operator|==
literal|null
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Usage: list ["
operator|+
name|StringUtils
operator|.
name|join
argument_list|(
name|SessionState
operator|.
name|ResourceType
operator|.
name|values
argument_list|()
argument_list|,
literal|"|"
argument_list|)
operator|+
literal|"] [<value> [<value>]*]"
argument_list|)
expr_stmt|;
name|ret
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|String
argument_list|>
name|filter
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|tokens
operator|.
name|length
operator|>=
literal|3
condition|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|tokens
argument_list|,
literal|2
argument_list|,
name|tokens
argument_list|,
literal|0
argument_list|,
name|tokens
operator|.
name|length
operator|-
literal|2
argument_list|)
expr_stmt|;
name|filter
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|tokens
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|s
init|=
name|ss
operator|.
name|list_resource
argument_list|(
name|t
argument_list|,
name|filter
argument_list|)
decl_stmt|;
if|if
condition|(
name|s
operator|!=
literal|null
operator|&&
operator|!
name|s
operator|.
name|isEmpty
argument_list|()
condition|)
name|ss
operator|.
name|out
operator|.
name|println
argument_list|(
name|StringUtils
operator|.
name|join
argument_list|(
name|s
argument_list|,
literal|"\n"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|CommandProcessor
name|proc
init|=
name|CommandProcessorFactory
operator|.
name|get
argument_list|(
name|tokens
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|proc
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|proc
operator|instanceof
name|Driver
condition|)
block|{
name|Driver
name|qp
init|=
operator|(
name|Driver
operator|)
name|proc
decl_stmt|;
name|PrintStream
name|out
init|=
name|ss
operator|.
name|out
decl_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|ret
operator|=
name|qp
operator|.
name|run
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
if|if
condition|(
name|ret
operator|!=
literal|0
condition|)
block|{
name|qp
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|ret
return|;
block|}
name|Vector
argument_list|<
name|String
argument_list|>
name|res
init|=
operator|new
name|Vector
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
while|while
condition|(
name|qp
operator|.
name|getResults
argument_list|(
name|res
argument_list|)
condition|)
block|{
for|for
control|(
name|String
name|r
range|:
name|res
control|)
block|{
name|out
operator|.
name|println
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
name|res
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|checkError
argument_list|()
condition|)
block|{
break|break;
block|}
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
literal|"Failed with exception "
operator|+
name|e
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|":"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
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
name|ret
operator|=
literal|1
expr_stmt|;
block|}
name|int
name|cret
init|=
name|qp
operator|.
name|close
argument_list|()
decl_stmt|;
if|if
condition|(
name|ret
operator|==
literal|0
condition|)
block|{
name|ret
operator|=
name|cret
expr_stmt|;
block|}
name|long
name|end
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|end
operator|>
name|start
condition|)
block|{
name|double
name|timeTaken
init|=
call|(
name|double
call|)
argument_list|(
name|end
operator|-
name|start
argument_list|)
operator|/
literal|1000.0
decl_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Time taken: "
operator|+
name|timeTaken
operator|+
literal|" seconds"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|ret
operator|=
name|proc
operator|.
name|run
argument_list|(
name|cmd_1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|ret
return|;
block|}
specifier|public
name|int
name|processLine
parameter_list|(
name|String
name|line
parameter_list|)
block|{
name|int
name|lastRet
init|=
literal|0
decl_stmt|,
name|ret
init|=
literal|0
decl_stmt|;
name|String
name|command
init|=
literal|""
decl_stmt|;
for|for
control|(
name|String
name|oneCmd
range|:
name|line
operator|.
name|split
argument_list|(
literal|";"
argument_list|)
control|)
block|{
if|if
condition|(
name|StringUtils
operator|.
name|endsWith
argument_list|(
name|oneCmd
argument_list|,
literal|"\\"
argument_list|)
condition|)
block|{
name|command
operator|+=
name|StringUtils
operator|.
name|chop
argument_list|(
name|oneCmd
argument_list|)
operator|+
literal|";"
expr_stmt|;
continue|continue;
block|}
else|else
block|{
name|command
operator|+=
name|oneCmd
expr_stmt|;
block|}
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|command
argument_list|)
condition|)
continue|continue;
name|ret
operator|=
name|processCmd
argument_list|(
name|command
argument_list|)
expr_stmt|;
name|command
operator|=
literal|""
expr_stmt|;
name|lastRet
operator|=
name|ret
expr_stmt|;
name|boolean
name|ignoreErrors
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
name|CLIIGNOREERRORS
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|!=
literal|0
operator|&&
operator|!
name|ignoreErrors
condition|)
block|{
return|return
name|ret
return|;
block|}
block|}
return|return
name|lastRet
return|;
block|}
specifier|public
name|int
name|processReader
parameter_list|(
name|BufferedReader
name|r
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|line
decl_stmt|;
name|StringBuffer
name|qsb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|r
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|qsb
operator|.
name|append
argument_list|(
name|line
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|processLine
argument_list|(
name|qsb
operator|.
name|toString
argument_list|()
argument_list|)
operator|)
return|;
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
name|Exception
block|{
name|OptionsProcessor
name|oproc
init|=
operator|new
name|OptionsProcessor
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|oproc
operator|.
name|process_stage1
argument_list|(
name|args
argument_list|)
condition|)
block|{
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// NOTE: It is critical to do this here so that log4j is reinitialized before
comment|// any of the other core hive classes are loaded
name|SessionState
operator|.
name|initHiveLog4j
argument_list|()
expr_stmt|;
name|CliSessionState
name|ss
init|=
operator|new
name|CliSessionState
argument_list|(
operator|new
name|HiveConf
argument_list|(
name|SessionState
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|ss
operator|.
name|in
operator|=
name|System
operator|.
name|in
expr_stmt|;
try|try
block|{
name|ss
operator|.
name|out
operator|=
operator|new
name|PrintStream
argument_list|(
name|System
operator|.
name|out
argument_list|,
literal|true
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
name|ss
operator|.
name|err
operator|=
operator|new
name|PrintStream
argument_list|(
name|System
operator|.
name|err
argument_list|,
literal|true
argument_list|,
literal|"UTF-8"
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
name|exit
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|oproc
operator|.
name|process_stage2
argument_list|(
name|ss
argument_list|)
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
comment|// set all properties specified via command line
name|HiveConf
name|conf
init|=
name|ss
operator|.
name|getConf
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|item
range|:
name|ss
operator|.
name|cmdProperties
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|conf
operator|.
name|set
argument_list|(
operator|(
name|String
operator|)
name|item
operator|.
name|getKey
argument_list|()
argument_list|,
operator|(
name|String
operator|)
name|item
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|usesJobShell
argument_list|()
condition|)
block|{
comment|// hadoop-20 and above - we need to augment classpath using hiveconf components
comment|// see also: code in ExecDriver.java
name|ClassLoader
name|loader
init|=
name|conf
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
name|String
name|auxJars
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
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
name|loader
operator|=
name|Utilities
operator|.
name|addToClassPath
argument_list|(
name|loader
argument_list|,
name|StringUtils
operator|.
name|split
argument_list|(
name|auxJars
argument_list|,
literal|","
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|setClassLoader
argument_list|(
name|loader
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|setContextClassLoader
argument_list|(
name|loader
argument_list|)
expr_stmt|;
block|}
name|SessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|CliDriver
name|cli
init|=
operator|new
name|CliDriver
argument_list|()
decl_stmt|;
if|if
condition|(
name|ss
operator|.
name|execString
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|exit
argument_list|(
name|cli
operator|.
name|processLine
argument_list|(
name|ss
operator|.
name|execString
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|ss
operator|.
name|fileName
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|exit
argument_list|(
name|cli
operator|.
name|processReader
argument_list|(
operator|new
name|BufferedReader
argument_list|(
operator|new
name|FileReader
argument_list|(
name|ss
operator|.
name|fileName
argument_list|)
argument_list|)
argument_list|)
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
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Could not open input file for reading. ("
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|")"
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
name|ConsoleReader
name|reader
init|=
operator|new
name|ConsoleReader
argument_list|()
decl_stmt|;
name|reader
operator|.
name|setBellEnabled
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|//reader.setDebug(new PrintWriter(new FileWriter("writer.debug", true)));
name|List
argument_list|<
name|SimpleCompletor
argument_list|>
name|completors
init|=
operator|new
name|LinkedList
argument_list|<
name|SimpleCompletor
argument_list|>
argument_list|()
decl_stmt|;
name|completors
operator|.
name|add
argument_list|(
operator|new
name|SimpleCompletor
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"set"
block|,
literal|"from"
block|,
literal|"create"
block|,
literal|"load"
block|,
literal|"describe"
block|,
literal|"quit"
block|,
literal|"exit"
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|reader
operator|.
name|addCompletor
argument_list|(
operator|new
name|ArgumentCompletor
argument_list|(
name|completors
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|line
decl_stmt|;
specifier|final
name|String
name|HISTORYFILE
init|=
literal|".hivehistory"
decl_stmt|;
name|String
name|historyFile
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.home"
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
name|HISTORYFILE
decl_stmt|;
name|reader
operator|.
name|setHistory
argument_list|(
operator|new
name|History
argument_list|(
operator|new
name|File
argument_list|(
name|historyFile
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
literal|0
decl_stmt|;
name|String
name|prefix
init|=
literal|""
decl_stmt|;
name|String
name|curPrompt
init|=
name|prompt
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|(
name|curPrompt
operator|+
literal|"> "
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|prefix
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|prefix
operator|+=
literal|'\n'
expr_stmt|;
block|}
if|if
condition|(
name|line
operator|.
name|trim
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|";"
argument_list|)
operator|&&
operator|!
name|line
operator|.
name|trim
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"\\;"
argument_list|)
condition|)
block|{
name|line
operator|=
name|prefix
operator|+
name|line
expr_stmt|;
name|ret
operator|=
name|cli
operator|.
name|processLine
argument_list|(
name|line
argument_list|)
expr_stmt|;
name|prefix
operator|=
literal|""
expr_stmt|;
name|curPrompt
operator|=
name|prompt
expr_stmt|;
block|}
else|else
block|{
name|prefix
operator|=
name|prefix
operator|+
name|line
expr_stmt|;
name|curPrompt
operator|=
name|prompt2
expr_stmt|;
continue|continue;
block|}
block|}
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

