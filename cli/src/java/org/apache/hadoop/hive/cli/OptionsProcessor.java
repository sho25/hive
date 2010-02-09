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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ListIterator
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
name|cli2
operator|.
name|Argument
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
name|cli2
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
name|cli2
operator|.
name|Group
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
name|cli2
operator|.
name|Option
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
name|cli2
operator|.
name|OptionException
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
name|cli2
operator|.
name|WriteableCommandLine
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
name|cli2
operator|.
name|builder
operator|.
name|ArgumentBuilder
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
name|cli2
operator|.
name|builder
operator|.
name|DefaultOptionBuilder
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
name|cli2
operator|.
name|builder
operator|.
name|GroupBuilder
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
name|cli2
operator|.
name|commandline
operator|.
name|Parser
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
name|cli2
operator|.
name|option
operator|.
name|PropertyOption
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
name|cli2
operator|.
name|resource
operator|.
name|ResourceConstants
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
comment|/**  * OptionsProcessor.  *   */
end_comment

begin_class
specifier|public
class|class
name|OptionsProcessor
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|l4j
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|OptionsProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Parser
name|parser
init|=
operator|new
name|Parser
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Option
name|confOptions
decl_stmt|,
name|isSilentOption
decl_stmt|,
name|execOption
decl_stmt|,
name|fileOption
decl_stmt|,
name|isHelpOption
decl_stmt|;
comment|/**    * Shamelessly cloned from Hadoop streaming take in multiple -hiveconf x=y parameters.    */
class|class
name|MultiPropertyOption
extends|extends
name|PropertyOption
block|{
specifier|private
name|String
name|optionString
decl_stmt|;
name|MultiPropertyOption
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
name|MultiPropertyOption
parameter_list|(
specifier|final
name|String
name|optionString
parameter_list|,
specifier|final
name|String
name|description
parameter_list|,
specifier|final
name|int
name|id
parameter_list|)
block|{
name|super
argument_list|(
name|optionString
argument_list|,
name|description
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|this
operator|.
name|optionString
operator|=
name|optionString
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|canProcess
parameter_list|(
specifier|final
name|WriteableCommandLine
name|commandLine
parameter_list|,
specifier|final
name|String
name|argument
parameter_list|)
block|{
name|boolean
name|ret
init|=
operator|(
name|argument
operator|!=
literal|null
operator|)
operator|&&
name|argument
operator|.
name|startsWith
argument_list|(
name|optionString
argument_list|)
decl_stmt|;
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
specifier|final
name|WriteableCommandLine
name|commandLine
parameter_list|,
specifier|final
name|ListIterator
name|arguments
parameter_list|)
throws|throws
name|OptionException
block|{
specifier|final
name|String
name|arg
init|=
operator|(
name|String
operator|)
name|arguments
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|canProcess
argument_list|(
name|commandLine
argument_list|,
name|arg
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|OptionException
argument_list|(
name|this
argument_list|,
name|ResourceConstants
operator|.
name|UNEXPECTED_TOKEN
argument_list|,
name|arg
argument_list|)
throw|;
block|}
name|ArrayList
name|properties
init|=
operator|new
name|ArrayList
argument_list|()
decl_stmt|;
name|String
name|next
init|=
literal|""
decl_stmt|;
while|while
condition|(
name|arguments
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|next
operator|=
operator|(
name|String
operator|)
name|arguments
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|next
operator|.
name|startsWith
argument_list|(
literal|"-"
argument_list|)
condition|)
block|{
if|if
condition|(
name|next
operator|.
name|indexOf
argument_list|(
literal|"="
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|OptionException
argument_list|(
name|this
argument_list|,
name|ResourceConstants
operator|.
name|UNEXPECTED_TOKEN
argument_list|,
literal|"argument: '"
operator|+
name|next
operator|+
literal|"' is not of the form x=y"
argument_list|)
throw|;
block|}
name|properties
operator|.
name|add
argument_list|(
name|next
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|arguments
operator|.
name|previous
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
comment|// add to any existing values (support specifying args multiple times)
name|List
argument_list|<
name|String
argument_list|>
name|oldVal
init|=
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|commandLine
operator|.
name|getValue
argument_list|(
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldVal
operator|==
literal|null
condition|)
block|{
name|commandLine
operator|.
name|addValue
argument_list|(
name|this
argument_list|,
name|properties
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|oldVal
operator|.
name|addAll
argument_list|(
name|properties
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|Option
name|createBoolOption
parameter_list|(
name|DefaultOptionBuilder
name|builder
parameter_list|,
name|String
name|longName
parameter_list|,
name|String
name|shortName
parameter_list|,
name|String
name|desc
parameter_list|)
block|{
name|builder
operator|.
name|reset
argument_list|()
expr_stmt|;
if|if
condition|(
name|longName
operator|==
literal|null
condition|)
block|{
return|return
name|builder
operator|.
name|withShortName
argument_list|(
name|shortName
argument_list|)
operator|.
name|withDescription
argument_list|(
name|desc
argument_list|)
operator|.
name|create
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|builder
operator|.
name|withShortName
argument_list|(
name|shortName
argument_list|)
operator|.
name|withLongName
argument_list|(
name|longName
argument_list|)
operator|.
name|withDescription
argument_list|(
name|desc
argument_list|)
operator|.
name|create
argument_list|()
return|;
block|}
block|}
specifier|private
name|Option
name|createOptionWithArg
parameter_list|(
name|DefaultOptionBuilder
name|builder
parameter_list|,
name|String
name|longName
parameter_list|,
name|String
name|shortName
parameter_list|,
name|String
name|desc
parameter_list|,
name|Argument
name|arg
parameter_list|)
block|{
name|builder
operator|.
name|reset
argument_list|()
expr_stmt|;
name|DefaultOptionBuilder
name|dob
init|=
name|builder
operator|.
name|withShortName
argument_list|(
name|shortName
argument_list|)
operator|.
name|withArgument
argument_list|(
name|arg
argument_list|)
operator|.
name|withDescription
argument_list|(
name|desc
argument_list|)
decl_stmt|;
if|if
condition|(
name|longName
operator|!=
literal|null
condition|)
block|{
name|dob
operator|=
name|dob
operator|.
name|withLongName
argument_list|(
name|longName
argument_list|)
expr_stmt|;
block|}
return|return
name|dob
operator|.
name|create
argument_list|()
return|;
block|}
specifier|public
name|OptionsProcessor
parameter_list|()
block|{
name|DefaultOptionBuilder
name|builder
init|=
operator|new
name|DefaultOptionBuilder
argument_list|(
literal|"-"
argument_list|,
literal|"-"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ArgumentBuilder
name|argBuilder
init|=
operator|new
name|ArgumentBuilder
argument_list|()
decl_stmt|;
comment|// -e
name|execOption
operator|=
name|createOptionWithArg
argument_list|(
name|builder
argument_list|,
literal|"exec"
argument_list|,
literal|"e"
argument_list|,
literal|"execute the following command"
argument_list|,
name|argBuilder
operator|.
name|withMinimum
argument_list|(
literal|1
argument_list|)
operator|.
name|withMaximum
argument_list|(
literal|1
argument_list|)
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
comment|// -f
name|fileOption
operator|=
name|createOptionWithArg
argument_list|(
name|builder
argument_list|,
literal|"file"
argument_list|,
literal|"f"
argument_list|,
literal|"execute commands from the following file"
argument_list|,
name|argBuilder
operator|.
name|withMinimum
argument_list|(
literal|1
argument_list|)
operator|.
name|withMaximum
argument_list|(
literal|1
argument_list|)
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
comment|// -S
name|isSilentOption
operator|=
name|createBoolOption
argument_list|(
name|builder
argument_list|,
literal|"silent"
argument_list|,
literal|"S"
argument_list|,
literal|"silent mode"
argument_list|)
expr_stmt|;
comment|// -help
name|isHelpOption
operator|=
name|createBoolOption
argument_list|(
name|builder
argument_list|,
literal|"help"
argument_list|,
literal|"h"
argument_list|,
literal|"help"
argument_list|)
expr_stmt|;
comment|// -hiveconf var=val
name|confOptions
operator|=
operator|new
name|MultiPropertyOption
argument_list|(
literal|"-hiveconf"
argument_list|,
literal|"(n=v) Optional. Add or override Hive/Hadoop properties."
argument_list|,
literal|'D'
argument_list|)
expr_stmt|;
operator|new
name|PropertyOption
argument_list|()
expr_stmt|;
name|Group
name|allOptions
init|=
operator|new
name|GroupBuilder
argument_list|()
operator|.
name|withOption
argument_list|(
name|confOptions
argument_list|)
operator|.
name|withOption
argument_list|(
name|isSilentOption
argument_list|)
operator|.
name|withOption
argument_list|(
name|isHelpOption
argument_list|)
operator|.
name|withOption
argument_list|(
name|execOption
argument_list|)
operator|.
name|withOption
argument_list|(
name|fileOption
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
name|parser
operator|.
name|setGroup
argument_list|(
name|allOptions
argument_list|)
expr_stmt|;
block|}
specifier|private
name|CommandLine
name|cmdLine
decl_stmt|;
specifier|public
name|boolean
name|process_stage1
parameter_list|(
name|String
index|[]
name|argv
parameter_list|)
block|{
try|try
block|{
name|cmdLine
operator|=
name|parser
operator|.
name|parse
argument_list|(
name|argv
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|hiveConfArgs
init|=
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|cmdLine
operator|.
name|getValue
argument_list|(
name|confOptions
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|hiveConfArgs
condition|)
block|{
for|for
control|(
name|String
name|s
range|:
name|hiveConfArgs
control|)
block|{
name|String
index|[]
name|parts
init|=
name|s
operator|.
name|split
argument_list|(
literal|"="
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|,
name|parts
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|OptionException
name|oe
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|oe
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|boolean
name|process_stage2
parameter_list|(
name|CliSessionState
name|ss
parameter_list|)
block|{
name|ss
operator|.
name|getConf
argument_list|()
expr_stmt|;
comment|// -S
name|ss
operator|.
name|setIsSilent
argument_list|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
name|isSilentOption
argument_list|)
argument_list|)
expr_stmt|;
comment|// -e
name|ss
operator|.
name|execString
operator|=
operator|(
name|String
operator|)
name|cmdLine
operator|.
name|getValue
argument_list|(
name|execOption
argument_list|)
expr_stmt|;
comment|// -f
name|ss
operator|.
name|fileName
operator|=
operator|(
name|String
operator|)
name|cmdLine
operator|.
name|getValue
argument_list|(
name|fileOption
argument_list|)
expr_stmt|;
comment|// -h
if|if
condition|(
name|cmdLine
operator|.
name|hasOption
argument_list|(
name|isHelpOption
argument_list|)
condition|)
block|{
name|printUsage
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|ss
operator|.
name|execString
operator|!=
literal|null
operator|&&
name|ss
operator|.
name|fileName
operator|!=
literal|null
condition|)
block|{
name|printUsage
argument_list|(
literal|"-e and -f option cannot be specified simultaneously"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|hiveConfArgs
init|=
operator|(
name|List
argument_list|<
name|String
argument_list|>
operator|)
name|cmdLine
operator|.
name|getValue
argument_list|(
name|confOptions
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|hiveConfArgs
condition|)
block|{
for|for
control|(
name|String
name|s
range|:
name|hiveConfArgs
control|)
block|{
name|String
index|[]
name|parts
init|=
name|s
operator|.
name|split
argument_list|(
literal|"="
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|ss
operator|.
name|cmdProperties
operator|.
name|setProperty
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|,
name|parts
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|void
name|printUsage
parameter_list|(
name|String
name|error
parameter_list|)
block|{
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Invalid arguments: "
operator|+
name|error
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage: hive [--config confdir] [-hiveconf x=y]* [<-f filename>|<-e query-string>] [-S]"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -e 'quoted query string'  Sql from command line"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -f<filename>             Sql from files"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -S                        Silent mode in interactive shell"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"-e and -f cannot be specified together. In the absence of these"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"options, interactive shell is started"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

