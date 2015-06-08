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
name|llap
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|GnuParser
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
name|cli
operator|.
name|HelpFormatter
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
name|cli
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
name|cli
operator|.
name|OptionBuilder
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
name|cli
operator|.
name|Options
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
name|cli
operator|.
name|ParseException
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_class
specifier|public
class|class
name|LlapOptionsProcessor
block|{
specifier|public
class|class
name|LlapOptions
block|{
specifier|private
name|int
name|instances
init|=
literal|0
decl_stmt|;
specifier|private
name|String
name|directory
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|int
name|executors
decl_stmt|;
specifier|private
name|long
name|cache
decl_stmt|;
specifier|private
name|long
name|size
decl_stmt|;
specifier|private
name|long
name|xmx
decl_stmt|;
specifier|public
name|LlapOptions
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|instances
parameter_list|,
name|String
name|directory
parameter_list|,
name|int
name|executors
parameter_list|,
name|long
name|cache
parameter_list|,
name|long
name|size
parameter_list|,
name|long
name|xmx
parameter_list|)
throws|throws
name|ParseException
block|{
if|if
condition|(
name|instances
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Invalid configuration: "
operator|+
name|instances
operator|+
literal|" (should be greater than 0)"
argument_list|)
throw|;
block|}
name|this
operator|.
name|instances
operator|=
name|instances
expr_stmt|;
name|this
operator|.
name|directory
operator|=
name|directory
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|executors
operator|=
name|executors
expr_stmt|;
name|this
operator|.
name|cache
operator|=
name|cache
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|xmx
operator|=
name|xmx
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
name|int
name|getInstances
parameter_list|()
block|{
return|return
name|instances
return|;
block|}
specifier|public
name|String
name|getDirectory
parameter_list|()
block|{
return|return
name|directory
return|;
block|}
specifier|public
name|int
name|getExecutors
parameter_list|()
block|{
return|return
name|executors
return|;
block|}
specifier|public
name|long
name|getCache
parameter_list|()
block|{
return|return
name|cache
return|;
block|}
specifier|public
name|long
name|getSize
parameter_list|()
block|{
return|return
name|size
return|;
block|}
specifier|public
name|long
name|getXmx
parameter_list|()
block|{
return|return
name|xmx
return|;
block|}
block|}
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
name|LlapOptionsProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Options
name|options
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveVariables
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
specifier|private
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
name|commandLine
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"static-access"
argument_list|)
specifier|public
name|LlapOptionsProcessor
parameter_list|()
block|{
comment|// set the number of instances on which llap should run
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"instances"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"instances"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"Specify the number of instances to run this on"
argument_list|)
operator|.
name|create
argument_list|(
literal|'i'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"name"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"name"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"Cluster name for YARN registry"
argument_list|)
operator|.
name|create
argument_list|(
literal|'n'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"directory"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"directory"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"Temp directory for jars etc."
argument_list|)
operator|.
name|create
argument_list|(
literal|'d'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"args"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"args"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"java arguments to the llap instance"
argument_list|)
operator|.
name|create
argument_list|(
literal|'a'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"loglevel"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"loglevel"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"log levels for the llap instance"
argument_list|)
operator|.
name|create
argument_list|(
literal|'l'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"chaosmonkey"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"chaosmonkey"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"chaosmonkey interval"
argument_list|)
operator|.
name|create
argument_list|(
literal|'m'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"executors"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"executors"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"executor per instance"
argument_list|)
operator|.
name|create
argument_list|(
literal|'e'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"cache"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"cache"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"cache size per instance"
argument_list|)
operator|.
name|create
argument_list|(
literal|'c'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"size"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"size"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"container size per instance"
argument_list|)
operator|.
name|create
argument_list|(
literal|'s'
argument_list|)
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|hasArg
argument_list|()
operator|.
name|withArgName
argument_list|(
literal|"xmx"
argument_list|)
operator|.
name|withLongOpt
argument_list|(
literal|"xmx"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"working memory size"
argument_list|)
operator|.
name|create
argument_list|(
literal|'w'
argument_list|)
argument_list|)
expr_stmt|;
comment|// [-H|--help]
name|options
operator|.
name|addOption
argument_list|(
operator|new
name|Option
argument_list|(
literal|"H"
argument_list|,
literal|"help"
argument_list|,
literal|false
argument_list|,
literal|"Print help information"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|long
name|parseSuffixed
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|StringUtils
operator|.
name|TraditionalBinaryPrefix
operator|.
name|string2long
argument_list|(
name|value
argument_list|)
return|;
block|}
specifier|public
name|LlapOptions
name|processOptions
parameter_list|(
name|String
name|argv
index|[]
parameter_list|)
throws|throws
name|ParseException
block|{
name|commandLine
operator|=
operator|new
name|GnuParser
argument_list|()
operator|.
name|parse
argument_list|(
name|options
argument_list|,
name|argv
argument_list|)
expr_stmt|;
if|if
condition|(
name|commandLine
operator|.
name|hasOption
argument_list|(
literal|'H'
argument_list|)
operator|||
literal|false
operator|==
name|commandLine
operator|.
name|hasOption
argument_list|(
literal|"instances"
argument_list|)
condition|)
block|{
comment|// needs at least --instances
name|printUsage
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
name|int
name|instances
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"instances"
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|directory
init|=
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"directory"
argument_list|)
decl_stmt|;
name|String
name|name
init|=
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"name"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|int
name|executors
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"executors"
argument_list|,
literal|"-1"
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|cache
init|=
name|parseSuffixed
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"cache"
argument_list|,
literal|"-1"
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|size
init|=
name|parseSuffixed
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"size"
argument_list|,
literal|"-1"
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|xmx
init|=
name|parseSuffixed
argument_list|(
name|commandLine
operator|.
name|getOptionValue
argument_list|(
literal|"xmx"
argument_list|,
literal|"-1"
argument_list|)
argument_list|)
decl_stmt|;
comment|// loglevel, chaosmonkey& args are parsed by the python processor
return|return
operator|new
name|LlapOptions
argument_list|(
name|name
argument_list|,
name|instances
argument_list|,
name|directory
argument_list|,
name|executors
argument_list|,
name|cache
argument_list|,
name|size
argument_list|,
name|xmx
argument_list|)
return|;
block|}
specifier|private
name|void
name|printUsage
parameter_list|()
block|{
operator|new
name|HelpFormatter
argument_list|()
operator|.
name|printHelp
argument_list|(
literal|"llap"
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

