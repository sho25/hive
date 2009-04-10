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
name|session
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
name|log4j
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
name|URL
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
name|ql
operator|.
name|metadata
operator|.
name|Hive
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
name|history
operator|.
name|HiveHistory
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
comment|/**  * SessionState encapsulates common data associated with a session  *   * Also provides support for a thread static session object that can  * be accessed from any point in the code to interact with the user  * and to retrieve configuration information  */
end_comment

begin_class
specifier|public
class|class
name|SessionState
block|{
comment|/**    * current configuration    */
specifier|protected
name|HiveConf
name|conf
decl_stmt|;
comment|/**    * silent mode    */
specifier|protected
name|boolean
name|isSilent
decl_stmt|;
comment|/**    * cached current connection to Hive MetaStore    */
specifier|protected
name|Hive
name|db
decl_stmt|;
comment|/*    *  HiveHistory Object     */
specifier|protected
name|HiveHistory
name|hiveHist
decl_stmt|;
comment|/**    * Streams to read/write from    */
specifier|public
name|PrintStream
name|out
decl_stmt|;
specifier|public
name|InputStream
name|in
decl_stmt|;
specifier|public
name|PrintStream
name|err
decl_stmt|;
specifier|public
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|public
name|void
name|setConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIsSilent
parameter_list|()
block|{
return|return
name|isSilent
return|;
block|}
specifier|public
name|void
name|setIsSilent
parameter_list|(
name|boolean
name|isSilent
parameter_list|)
block|{
name|this
operator|.
name|isSilent
operator|=
name|isSilent
expr_stmt|;
block|}
specifier|public
name|SessionState
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SessionState
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SessionState
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Hive
name|db
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|db
operator|=
name|db
expr_stmt|;
for|for
control|(
name|HiveConf
operator|.
name|ConfVars
name|oneVar
range|:
name|HiveConf
operator|.
name|metaVars
control|)
block|{
name|dbOptions
operator|.
name|put
argument_list|(
name|oneVar
argument_list|,
name|conf
operator|.
name|getVar
argument_list|(
name|oneVar
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * cached values of such options    */
specifier|private
specifier|final
name|HashMap
argument_list|<
name|HiveConf
operator|.
name|ConfVars
argument_list|,
name|String
argument_list|>
name|dbOptions
init|=
operator|new
name|HashMap
argument_list|<
name|HiveConf
operator|.
name|ConfVars
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|Hive
name|getDb
parameter_list|()
throws|throws
name|HiveException
block|{
name|boolean
name|needsRefresh
init|=
literal|false
decl_stmt|;
for|for
control|(
name|HiveConf
operator|.
name|ConfVars
name|oneVar
range|:
name|HiveConf
operator|.
name|metaVars
control|)
block|{
if|if
condition|(
operator|!
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|StringUtils
operator|.
name|difference
argument_list|(
name|dbOptions
operator|.
name|get
argument_list|(
name|oneVar
argument_list|)
argument_list|,
name|conf
operator|.
name|getVar
argument_list|(
name|oneVar
argument_list|)
argument_list|)
argument_list|)
condition|)
block|{
name|needsRefresh
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|(
name|db
operator|==
literal|null
operator|)
operator|||
name|needsRefresh
condition|)
block|{
name|db
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|,
name|needsRefresh
argument_list|)
expr_stmt|;
block|}
return|return
name|db
return|;
block|}
specifier|public
name|void
name|setCmd
parameter_list|(
name|String
name|cmdString
parameter_list|)
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYSTRING
argument_list|,
name|cmdString
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getCmd
parameter_list|()
block|{
return|return
operator|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYSTRING
argument_list|)
operator|)
return|;
block|}
specifier|public
name|String
name|getQueryId
parameter_list|()
block|{
return|return
operator|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
operator|)
return|;
block|}
specifier|public
name|String
name|getSessionId
parameter_list|()
block|{
return|return
operator|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESESSIONID
argument_list|)
operator|)
return|;
block|}
comment|/**    * Singleton Session object per thread.    *    **/
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|SessionState
argument_list|>
name|tss
init|=
operator|new
name|ThreadLocal
argument_list|<
name|SessionState
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * start a new session and set it to current session    */
specifier|public
specifier|static
name|SessionState
name|start
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|SessionState
name|ss
init|=
operator|new
name|SessionState
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ss
operator|.
name|getConf
argument_list|()
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESESSIONID
argument_list|,
name|makeSessionId
argument_list|()
argument_list|)
expr_stmt|;
name|ss
operator|.
name|hiveHist
operator|=
operator|new
name|HiveHistory
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|tss
operator|.
name|set
argument_list|(
name|ss
argument_list|)
expr_stmt|;
return|return
operator|(
name|ss
operator|)
return|;
block|}
comment|/**    * set current session to existing session object    * if a thread is running multiple sessions - it must call this method with the new    * session object when switching from one session to another    */
specifier|public
specifier|static
name|SessionState
name|start
parameter_list|(
name|SessionState
name|startSs
parameter_list|)
block|{
name|tss
operator|.
name|set
argument_list|(
name|startSs
argument_list|)
expr_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|startSs
operator|.
name|getConf
argument_list|()
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESESSIONID
argument_list|)
argument_list|)
condition|)
block|{
name|startSs
operator|.
name|getConf
argument_list|()
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESESSIONID
argument_list|,
name|makeSessionId
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|startSs
operator|.
name|hiveHist
operator|==
literal|null
condition|)
block|{
name|startSs
operator|.
name|hiveHist
operator|=
operator|new
name|HiveHistory
argument_list|(
name|startSs
argument_list|)
expr_stmt|;
block|}
return|return
name|startSs
return|;
block|}
comment|/**    * get the current session    */
specifier|public
specifier|static
name|SessionState
name|get
parameter_list|()
block|{
return|return
name|tss
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * get hiveHitsory object which does structured logging    * @return The hive history object    */
specifier|public
name|HiveHistory
name|getHiveHistory
parameter_list|()
block|{
return|return
name|hiveHist
return|;
block|}
specifier|private
specifier|static
name|String
name|makeSessionId
parameter_list|()
block|{
name|GregorianCalendar
name|gc
init|=
operator|new
name|GregorianCalendar
argument_list|()
decl_stmt|;
name|String
name|userid
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
decl_stmt|;
return|return
name|userid
operator|+
literal|"_"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$4d%2$02d%3$02d%4$02d%5$02d"
argument_list|,
name|gc
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|YEAR
argument_list|)
argument_list|,
name|gc
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|MONTH
argument_list|)
operator|+
literal|1
argument_list|,
name|gc
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|DAY_OF_MONTH
argument_list|)
argument_list|,
name|gc
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|HOUR_OF_DAY
argument_list|)
argument_list|,
name|gc
operator|.
name|get
argument_list|(
name|Calendar
operator|.
name|MINUTE
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_L4J
init|=
literal|"hive-log4j.properties"
decl_stmt|;
specifier|public
specifier|static
name|void
name|initHiveLog4j
parameter_list|()
block|{
comment|// allow hive log4j to override any normal initialized one
name|URL
name|hive_l4j
init|=
name|SessionState
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
operator|.
name|getResource
argument_list|(
name|HIVE_L4J
argument_list|)
decl_stmt|;
if|if
condition|(
name|hive_l4j
operator|==
literal|null
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|HIVE_L4J
operator|+
literal|" not found"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LogManager
operator|.
name|resetConfiguration
argument_list|()
expr_stmt|;
name|PropertyConfigurator
operator|.
name|configure
argument_list|(
name|hive_l4j
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This class provides helper routines to emit informational and error messages to the user    * and log4j files while obeying the current session's verbosity levels.    *     * NEVER write directly to the SessionStates standard output other than to emit result data    * DO use printInfo and printError provided by LogHelper to emit non result data strings    *     * It is perfectly acceptable to have global static LogHelper objects (for example - once per module)    * LogHelper always emits info/error to current session as required.    */
specifier|public
specifier|static
class|class
name|LogHelper
block|{
specifier|protected
name|Log
name|LOG
decl_stmt|;
specifier|protected
name|boolean
name|isSilent
decl_stmt|;
specifier|public
name|LogHelper
parameter_list|(
name|Log
name|LOG
parameter_list|)
block|{
name|this
argument_list|(
name|LOG
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|LogHelper
parameter_list|(
name|Log
name|LOG
parameter_list|,
name|boolean
name|isSilent
parameter_list|)
block|{
name|this
operator|.
name|LOG
operator|=
name|LOG
expr_stmt|;
name|this
operator|.
name|isSilent
operator|=
name|isSilent
expr_stmt|;
block|}
specifier|public
name|PrintStream
name|getOutStream
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
return|return
operator|(
operator|(
name|ss
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|ss
operator|.
name|out
operator|!=
literal|null
operator|)
operator|)
condition|?
name|ss
operator|.
name|out
else|:
name|System
operator|.
name|out
return|;
block|}
specifier|public
name|PrintStream
name|getErrStream
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
return|return
operator|(
operator|(
name|ss
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|ss
operator|.
name|err
operator|!=
literal|null
operator|)
operator|)
condition|?
name|ss
operator|.
name|err
else|:
name|System
operator|.
name|err
return|;
block|}
specifier|public
name|boolean
name|getIsSilent
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
comment|// use the session or the one supplied in constructor
return|return
operator|(
name|ss
operator|!=
literal|null
operator|)
condition|?
name|ss
operator|.
name|getIsSilent
argument_list|()
else|:
name|isSilent
return|;
block|}
specifier|public
name|void
name|printInfo
parameter_list|(
name|String
name|info
parameter_list|)
block|{
name|printInfo
argument_list|(
name|info
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|printInfo
parameter_list|(
name|String
name|info
parameter_list|,
name|String
name|detail
parameter_list|)
block|{
if|if
condition|(
operator|!
name|getIsSilent
argument_list|()
condition|)
block|{
name|getOutStream
argument_list|()
operator|.
name|println
argument_list|(
name|info
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|info
operator|+
name|StringUtils
operator|.
name|defaultString
argument_list|(
name|detail
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|printError
parameter_list|(
name|String
name|error
parameter_list|)
block|{
name|printError
argument_list|(
name|error
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|printError
parameter_list|(
name|String
name|error
parameter_list|,
name|String
name|detail
parameter_list|)
block|{
name|getErrStream
argument_list|()
operator|.
name|println
argument_list|(
name|error
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|error
operator|+
name|StringUtils
operator|.
name|defaultString
argument_list|(
name|detail
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|LogHelper
name|_console
decl_stmt|;
comment|/**    * initialize or retrieve console object for SessionState    */
specifier|private
specifier|static
name|LogHelper
name|getConsole
parameter_list|()
block|{
if|if
condition|(
name|_console
operator|==
literal|null
condition|)
block|{
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"SessionState"
argument_list|)
decl_stmt|;
name|_console
operator|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|)
expr_stmt|;
block|}
return|return
name|_console
return|;
block|}
specifier|public
specifier|static
name|String
name|validateFile
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|curFiles
parameter_list|,
name|String
name|newFile
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
name|LogHelper
name|console
init|=
name|getConsole
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|(
name|ss
operator|==
literal|null
operator|)
condition|?
operator|new
name|Configuration
argument_list|()
else|:
name|ss
operator|.
name|getConf
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|Utilities
operator|.
name|realFile
argument_list|(
name|newFile
argument_list|,
name|conf
argument_list|)
operator|!=
literal|null
condition|)
return|return
name|newFile
return|;
else|else
block|{
name|console
operator|.
name|printError
argument_list|(
name|newFile
operator|+
literal|" does not exist"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
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
literal|"Unable to validate "
operator|+
name|newFile
operator|+
literal|"\nException: "
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
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
interface|interface
name|ResourceHook
block|{
specifier|public
name|String
name|preHook
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|cur
parameter_list|,
name|String
name|s
parameter_list|)
function_decl|;
block|}
specifier|public
specifier|static
enum|enum
name|ResourceType
block|{
name|FILE
argument_list|(
operator|new
name|ResourceHook
argument_list|()
block|{
specifier|public
name|String
name|preHook
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|cur
parameter_list|,
name|String
name|s
parameter_list|)
block|{
return|return
name|validateFile
argument_list|(
name|cur
argument_list|,
name|s
argument_list|)
return|;
block|}
block|}
argument_list|)
block|;
specifier|public
name|ResourceHook
name|hook
decl_stmt|;
name|ResourceType
parameter_list|(
name|ResourceHook
name|hook
parameter_list|)
block|{
name|this
operator|.
name|hook
operator|=
name|hook
expr_stmt|;
block|}
block|}
empty_stmt|;
specifier|public
specifier|static
name|ResourceType
name|find_resource_type
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|s
operator|=
name|s
operator|.
name|trim
argument_list|()
operator|.
name|toUpperCase
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|ResourceType
operator|.
name|valueOf
argument_list|(
name|s
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{     }
comment|// try singular
if|if
condition|(
name|s
operator|.
name|endsWith
argument_list|(
literal|"S"
argument_list|)
condition|)
block|{
name|s
operator|=
name|s
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|s
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
return|return
name|ResourceType
operator|.
name|valueOf
argument_list|(
name|s
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{     }
return|return
literal|null
return|;
block|}
specifier|private
name|HashMap
argument_list|<
name|ResourceType
argument_list|,
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|>
name|resource_map
init|=
operator|new
name|HashMap
argument_list|<
name|ResourceType
argument_list|,
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|void
name|add_resource
parameter_list|(
name|ResourceType
name|t
parameter_list|,
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|resource_map
operator|.
name|get
argument_list|(
name|t
argument_list|)
operator|==
literal|null
condition|)
block|{
name|resource_map
operator|.
name|put
argument_list|(
name|t
argument_list|,
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|fnlVal
init|=
name|value
decl_stmt|;
if|if
condition|(
name|t
operator|.
name|hook
operator|!=
literal|null
condition|)
block|{
name|fnlVal
operator|=
name|t
operator|.
name|hook
operator|.
name|preHook
argument_list|(
name|resource_map
operator|.
name|get
argument_list|(
name|t
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|fnlVal
operator|==
literal|null
condition|)
return|return;
block|}
name|resource_map
operator|.
name|get
argument_list|(
name|t
argument_list|)
operator|.
name|add
argument_list|(
name|fnlVal
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|delete_resource
parameter_list|(
name|ResourceType
name|t
parameter_list|,
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|resource_map
operator|.
name|get
argument_list|(
name|t
argument_list|)
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|(
name|resource_map
operator|.
name|get
argument_list|(
name|t
argument_list|)
operator|.
name|remove
argument_list|(
name|value
argument_list|)
operator|)
return|;
block|}
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|list_resource
parameter_list|(
name|ResourceType
name|t
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|filter
parameter_list|)
block|{
if|if
condition|(
name|resource_map
operator|.
name|get
argument_list|(
name|t
argument_list|)
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|orig
init|=
name|resource_map
operator|.
name|get
argument_list|(
name|t
argument_list|)
decl_stmt|;
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
return|return
name|orig
return|;
block|}
else|else
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|fnl
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|one
range|:
name|orig
control|)
block|{
if|if
condition|(
name|filter
operator|.
name|contains
argument_list|(
name|one
argument_list|)
condition|)
block|{
name|fnl
operator|.
name|add
argument_list|(
name|one
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|fnl
return|;
block|}
block|}
specifier|public
name|void
name|delete_resource
parameter_list|(
name|ResourceType
name|t
parameter_list|)
block|{
name|resource_map
operator|.
name|remove
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

