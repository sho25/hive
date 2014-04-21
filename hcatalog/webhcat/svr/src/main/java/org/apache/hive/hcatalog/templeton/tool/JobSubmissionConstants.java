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
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
package|;
end_package

begin_interface
specifier|public
interface|interface
name|JobSubmissionConstants
block|{
specifier|public
specifier|static
specifier|final
name|String
name|COPY_NAME
init|=
literal|"templeton.copy"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STATUSDIR_NAME
init|=
literal|"templeton.statusdir"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ENABLE_LOG
init|=
literal|"templeton.enablelog"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JOB_TYPE
init|=
literal|"templeton.jobtype"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JAR_ARGS_NAME
init|=
literal|"templeton.args"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OVERRIDE_CLASSPATH
init|=
literal|"templeton.override-classpath"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OVERRIDE_CONTAINER_LOG4J_PROPS
init|=
literal|"override.containerLog4j"
decl_stmt|;
comment|//name of file
specifier|static
specifier|final
name|String
name|CONTAINER_LOG4J_PROPS
init|=
literal|"override-container-log4j.properties"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STDOUT_FNAME
init|=
literal|"stdout"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STDERR_FNAME
init|=
literal|"stderr"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXIT_FNAME
init|=
literal|"exit"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|WATCHER_TIMEOUT_SECS
init|=
literal|10
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|KEEP_ALIVE_MSEC
init|=
literal|60
operator|*
literal|1000
decl_stmt|;
comment|/*    * The = sign in the string for TOKEN_FILE_ARG_PLACEHOLDER is required because    * org.apache.hadoop.util.GenericOptionsParser.preProcessForWindows() prepares    * arguments expecting an = sign. It will fail to prepare the arguments correctly    * without the = sign present.    */
specifier|public
specifier|static
specifier|final
name|String
name|TOKEN_FILE_ARG_PLACEHOLDER
init|=
literal|"__MR_JOB_CREDENTIALS_OPTION=WEBHCAT_TOKEN_FILE_LOCATION__"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TOKEN_FILE_ARG_PLACEHOLDER_TEZ
init|=
literal|"__TEZ_CREDENTIALS_OPTION=WEBHCAT_TOKEN_FILE_LOCATION_TEZ__"
decl_stmt|;
comment|/**    * constants needed for Pig job submission    * The string values here are what Pig expects to see in it's environment    */
specifier|public
specifier|static
interface|interface
name|PigConstants
block|{
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_HOME
init|=
literal|"HIVE_HOME"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_HOME
init|=
literal|"HCAT_HOME"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PIG_OPTS
init|=
literal|"PIG_OPTS"
decl_stmt|;
block|}
block|}
end_interface

end_unit

