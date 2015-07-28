begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/*  * This source file is based on code taken from SQLLine 1.0.2  * See SQLLine notice in LICENSE  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|beeline
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
name|FileInputStream
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
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
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|LinkedList
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
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import
name|jline
operator|.
name|Terminal
import|;
end_import

begin_import
import|import
name|jline
operator|.
name|TerminalFactory
import|;
end_import

begin_import
import|import
name|jline
operator|.
name|console
operator|.
name|completer
operator|.
name|Completer
import|;
end_import

begin_import
import|import
name|jline
operator|.
name|console
operator|.
name|completer
operator|.
name|StringsCompleter
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

begin_class
class|class
name|BeeLineOpts
implements|implements
name|Completer
block|{
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_WIDTH
init|=
literal|80
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_HEIGHT
init|=
literal|80
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_HEADER_INTERVAL
init|=
literal|100
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_ISOLATION_LEVEL
init|=
literal|"TRANSACTION_REPEATABLE_READ"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PROPERTY_PREFIX
init|=
literal|"beeline."
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PROPERTY_NAME_EXIT
init|=
name|PROPERTY_PREFIX
operator|+
literal|"system.exit"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_NULL_STRING
init|=
literal|"NULL"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|char
name|DEFAULT_DELIMITER_FOR_DSV
init|=
literal|'|'
decl_stmt|;
specifier|private
specifier|final
name|BeeLine
name|beeLine
decl_stmt|;
specifier|private
name|boolean
name|autosave
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|silent
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|color
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|showHeader
init|=
literal|true
decl_stmt|;
specifier|private
name|int
name|headerInterval
init|=
literal|100
decl_stmt|;
specifier|private
name|boolean
name|fastConnect
init|=
literal|true
decl_stmt|;
specifier|private
name|boolean
name|autoCommit
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|verbose
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|force
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|incremental
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|showWarnings
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|showNestedErrs
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|showElapsedTime
init|=
literal|true
decl_stmt|;
specifier|private
name|boolean
name|entireLineAsCommand
init|=
literal|false
decl_stmt|;
specifier|private
name|String
name|numberFormat
init|=
literal|"default"
decl_stmt|;
specifier|private
specifier|final
name|Terminal
name|terminal
init|=
name|TerminalFactory
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|private
name|int
name|maxWidth
init|=
name|DEFAULT_MAX_WIDTH
decl_stmt|;
specifier|private
name|int
name|maxHeight
init|=
name|DEFAULT_MAX_HEIGHT
decl_stmt|;
specifier|private
name|int
name|maxColumnWidth
init|=
literal|15
decl_stmt|;
name|int
name|timeout
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|String
name|isolation
init|=
name|DEFAULT_ISOLATION_LEVEL
decl_stmt|;
specifier|private
name|String
name|outputFormat
init|=
literal|"table"
decl_stmt|;
comment|// This configuration is used only for client side configuration.
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|boolean
name|trimScripts
init|=
literal|true
decl_stmt|;
specifier|private
name|boolean
name|allowMultiLineCommand
init|=
literal|true
decl_stmt|;
comment|//This can be set for old behavior of nulls printed as empty strings
specifier|private
name|boolean
name|nullEmptyString
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|truncateTable
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|File
name|rcFile
init|=
operator|new
name|File
argument_list|(
name|saveDir
argument_list|()
argument_list|,
literal|"beeline.properties"
argument_list|)
decl_stmt|;
specifier|private
name|String
name|historyFile
init|=
operator|new
name|File
argument_list|(
name|saveDir
argument_list|()
argument_list|,
literal|"history"
argument_list|)
operator|.
name|getAbsolutePath
argument_list|()
decl_stmt|;
specifier|private
name|String
name|scriptFile
init|=
literal|null
decl_stmt|;
specifier|private
name|String
index|[]
name|initFiles
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|authType
init|=
literal|null
decl_stmt|;
specifier|private
name|char
name|delimiterForDSV
init|=
name|DEFAULT_DELIMITER_FOR_DSV
decl_stmt|;
specifier|private
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
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveConfVariables
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
specifier|public
name|BeeLineOpts
parameter_list|(
name|BeeLine
name|beeLine
parameter_list|,
name|Properties
name|props
parameter_list|)
block|{
name|this
operator|.
name|beeLine
operator|=
name|beeLine
expr_stmt|;
if|if
condition|(
name|terminal
operator|.
name|getWidth
argument_list|()
operator|>
literal|0
condition|)
block|{
name|maxWidth
operator|=
name|terminal
operator|.
name|getWidth
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|terminal
operator|.
name|getHeight
argument_list|()
operator|>
literal|0
condition|)
block|{
name|maxHeight
operator|=
name|terminal
operator|.
name|getHeight
argument_list|()
expr_stmt|;
block|}
name|loadProperties
argument_list|(
name|props
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Completer
index|[]
name|optionCompleters
parameter_list|()
block|{
return|return
operator|new
name|Completer
index|[]
block|{
name|this
block|}
return|;
block|}
specifier|public
name|String
index|[]
name|possibleSettingValues
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|vals
init|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|vals
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"yes"
block|,
literal|"no"
block|}
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|vals
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|vals
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
comment|/**    * The save directory if HOME/.beeline/ on UNIX, and    * HOME/beeline/ on Windows.    */
specifier|public
name|File
name|saveDir
parameter_list|()
block|{
name|String
name|dir
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"beeline.rcfile"
argument_list|)
decl_stmt|;
if|if
condition|(
name|dir
operator|!=
literal|null
operator|&&
name|dir
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
return|return
operator|new
name|File
argument_list|(
name|dir
argument_list|)
return|;
block|}
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.home"
argument_list|)
argument_list|,
operator|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"os.name"
argument_list|)
operator|.
name|toLowerCase
argument_list|()
operator|.
name|indexOf
argument_list|(
literal|"windows"
argument_list|)
operator|!=
operator|-
literal|1
condition|?
literal|""
else|:
literal|"."
operator|)
operator|+
literal|"beeline"
argument_list|)
operator|.
name|getAbsoluteFile
argument_list|()
decl_stmt|;
try|try
block|{
name|f
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{     }
return|return
name|f
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|complete
parameter_list|(
name|String
name|buf
parameter_list|,
name|int
name|pos
parameter_list|,
name|List
name|cand
parameter_list|)
block|{
try|try
block|{
return|return
operator|new
name|StringsCompleter
argument_list|(
name|propertyNames
argument_list|()
argument_list|)
operator|.
name|complete
argument_list|(
name|buf
argument_list|,
name|pos
argument_list|,
name|cand
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|beeLine
operator|.
name|handleException
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
operator|-
literal|1
return|;
block|}
block|}
specifier|public
name|void
name|save
parameter_list|()
throws|throws
name|IOException
block|{
name|OutputStream
name|out
init|=
operator|new
name|FileOutputStream
argument_list|(
name|rcFile
argument_list|)
decl_stmt|;
name|save
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|save
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|Properties
name|props
init|=
name|toProperties
argument_list|()
decl_stmt|;
comment|// don't save maxwidth: it is automatically set based on
comment|// the terminal configuration
name|props
operator|.
name|remove
argument_list|(
name|PROPERTY_PREFIX
operator|+
literal|"maxwidth"
argument_list|)
expr_stmt|;
name|props
operator|.
name|store
argument_list|(
name|out
argument_list|,
name|beeLine
operator|.
name|getApplicationTitle
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|beeLine
operator|.
name|handleException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|String
index|[]
name|propertyNames
parameter_list|()
throws|throws
name|IllegalAccessException
throws|,
name|InvocationTargetException
block|{
name|TreeSet
argument_list|<
name|String
argument_list|>
name|names
init|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// get all the values from getXXX methods
name|Method
index|[]
name|m
init|=
name|getClass
argument_list|()
operator|.
name|getDeclaredMethods
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|m
operator|!=
literal|null
operator|&&
name|i
operator|<
name|m
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|m
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"get"
argument_list|)
operator|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|m
index|[
name|i
index|]
operator|.
name|getParameterTypes
argument_list|()
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
continue|continue;
block|}
name|String
name|propName
init|=
name|m
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
operator|.
name|substring
argument_list|(
literal|3
argument_list|)
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
name|names
operator|.
name|add
argument_list|(
name|propName
argument_list|)
expr_stmt|;
block|}
return|return
name|names
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|names
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
specifier|public
name|Properties
name|toProperties
parameter_list|()
throws|throws
name|IllegalAccessException
throws|,
name|InvocationTargetException
throws|,
name|ClassNotFoundException
block|{
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|String
index|[]
name|names
init|=
name|propertyNames
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|names
operator|!=
literal|null
operator|&&
name|i
operator|<
name|names
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|o
init|=
name|beeLine
operator|.
name|getReflector
argument_list|()
operator|.
name|invoke
argument_list|(
name|this
argument_list|,
literal|"get"
operator|+
name|names
index|[
name|i
index|]
argument_list|,
operator|new
name|Object
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|PROPERTY_PREFIX
operator|+
name|names
index|[
name|i
index|]
argument_list|,
name|o
operator|==
literal|null
condition|?
literal|""
else|:
name|o
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|beeLine
operator|.
name|debug
argument_list|(
literal|"properties: "
operator|+
name|props
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|props
return|;
block|}
specifier|public
name|void
name|load
parameter_list|()
throws|throws
name|IOException
block|{
name|InputStream
name|in
init|=
operator|new
name|FileInputStream
argument_list|(
name|rcFile
argument_list|)
decl_stmt|;
name|load
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|load
parameter_list|(
name|InputStream
name|fin
parameter_list|)
throws|throws
name|IOException
block|{
name|Properties
name|p
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|p
operator|.
name|load
argument_list|(
name|fin
argument_list|)
expr_stmt|;
name|loadProperties
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update the options after connection is established in CLI mode.    */
specifier|public
name|void
name|updateBeeLineOptsFromConf
parameter_list|()
block|{
if|if
condition|(
operator|!
name|beeLine
operator|.
name|isBeeLine
argument_list|()
condition|)
block|{
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
name|conf
operator|=
name|beeLine
operator|.
name|getCommands
argument_list|()
operator|.
name|getHiveConf
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|setForce
argument_list|(
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
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|setHiveConf
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
name|void
name|loadProperties
parameter_list|(
name|Properties
name|props
parameter_list|)
block|{
for|for
control|(
name|Object
name|element
range|:
name|props
operator|.
name|keySet
argument_list|()
control|)
block|{
name|String
name|key
init|=
name|element
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
name|PROPERTY_NAME_EXIT
argument_list|)
condition|)
block|{
comment|// fix for sf.net bug 879422
continue|continue;
block|}
if|if
condition|(
name|key
operator|.
name|startsWith
argument_list|(
name|PROPERTY_PREFIX
argument_list|)
condition|)
block|{
name|set
argument_list|(
name|key
operator|.
name|substring
argument_list|(
name|PROPERTY_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
argument_list|,
name|props
operator|.
name|getProperty
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|set
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|set
argument_list|(
name|key
argument_list|,
name|value
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|set
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|,
name|boolean
name|quiet
parameter_list|)
block|{
try|try
block|{
name|beeLine
operator|.
name|getReflector
argument_list|()
operator|.
name|invoke
argument_list|(
name|this
argument_list|,
literal|"set"
operator|+
name|key
argument_list|,
operator|new
name|Object
index|[]
block|{
name|value
block|}
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|quiet
condition|)
block|{
name|beeLine
operator|.
name|error
argument_list|(
name|beeLine
operator|.
name|loc
argument_list|(
literal|"error-setting"
argument_list|,
operator|new
name|Object
index|[]
block|{
name|key
block|,
name|e
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
block|}
specifier|public
name|void
name|setFastConnect
parameter_list|(
name|boolean
name|fastConnect
parameter_list|)
block|{
name|this
operator|.
name|fastConnect
operator|=
name|fastConnect
expr_stmt|;
block|}
specifier|public
name|String
name|getAuthType
parameter_list|()
block|{
return|return
name|authType
return|;
block|}
specifier|public
name|void
name|setAuthType
parameter_list|(
name|String
name|authType
parameter_list|)
block|{
name|this
operator|.
name|authType
operator|=
name|authType
expr_stmt|;
block|}
specifier|public
name|boolean
name|getFastConnect
parameter_list|()
block|{
return|return
name|fastConnect
return|;
block|}
specifier|public
name|void
name|setAutoCommit
parameter_list|(
name|boolean
name|autoCommit
parameter_list|)
block|{
name|this
operator|.
name|autoCommit
operator|=
name|autoCommit
expr_stmt|;
block|}
specifier|public
name|boolean
name|getAutoCommit
parameter_list|()
block|{
return|return
name|autoCommit
return|;
block|}
specifier|public
name|void
name|setVerbose
parameter_list|(
name|boolean
name|verbose
parameter_list|)
block|{
name|this
operator|.
name|verbose
operator|=
name|verbose
expr_stmt|;
block|}
specifier|public
name|boolean
name|getVerbose
parameter_list|()
block|{
return|return
name|verbose
return|;
block|}
specifier|public
name|void
name|setShowWarnings
parameter_list|(
name|boolean
name|showWarnings
parameter_list|)
block|{
name|this
operator|.
name|showWarnings
operator|=
name|showWarnings
expr_stmt|;
block|}
specifier|public
name|boolean
name|getShowWarnings
parameter_list|()
block|{
return|return
name|showWarnings
return|;
block|}
specifier|public
name|void
name|setShowNestedErrs
parameter_list|(
name|boolean
name|showNestedErrs
parameter_list|)
block|{
name|this
operator|.
name|showNestedErrs
operator|=
name|showNestedErrs
expr_stmt|;
block|}
specifier|public
name|boolean
name|getShowNestedErrs
parameter_list|()
block|{
return|return
name|showNestedErrs
return|;
block|}
specifier|public
name|void
name|setShowElapsedTime
parameter_list|(
name|boolean
name|showElapsedTime
parameter_list|)
block|{
name|this
operator|.
name|showElapsedTime
operator|=
name|showElapsedTime
expr_stmt|;
block|}
specifier|public
name|boolean
name|getShowElapsedTime
parameter_list|()
block|{
return|return
name|showElapsedTime
return|;
block|}
specifier|public
name|void
name|setNumberFormat
parameter_list|(
name|String
name|numberFormat
parameter_list|)
block|{
name|this
operator|.
name|numberFormat
operator|=
name|numberFormat
expr_stmt|;
block|}
specifier|public
name|String
name|getNumberFormat
parameter_list|()
block|{
return|return
name|numberFormat
return|;
block|}
specifier|public
name|void
name|setMaxWidth
parameter_list|(
name|int
name|maxWidth
parameter_list|)
block|{
name|this
operator|.
name|maxWidth
operator|=
name|maxWidth
expr_stmt|;
block|}
specifier|public
name|int
name|getMaxWidth
parameter_list|()
block|{
return|return
name|maxWidth
return|;
block|}
specifier|public
name|void
name|setMaxColumnWidth
parameter_list|(
name|int
name|maxColumnWidth
parameter_list|)
block|{
name|this
operator|.
name|maxColumnWidth
operator|=
name|maxColumnWidth
expr_stmt|;
block|}
specifier|public
name|int
name|getMaxColumnWidth
parameter_list|()
block|{
return|return
name|maxColumnWidth
return|;
block|}
specifier|public
name|void
name|setTimeout
parameter_list|(
name|int
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
block|}
specifier|public
name|int
name|getTimeout
parameter_list|()
block|{
return|return
name|timeout
return|;
block|}
specifier|public
name|void
name|setIsolation
parameter_list|(
name|String
name|isolation
parameter_list|)
block|{
name|this
operator|.
name|isolation
operator|=
name|isolation
expr_stmt|;
block|}
specifier|public
name|String
name|getIsolation
parameter_list|()
block|{
return|return
name|isolation
return|;
block|}
specifier|public
name|void
name|setEntireLineAsCommand
parameter_list|(
name|boolean
name|entireLineAsCommand
parameter_list|)
block|{
name|this
operator|.
name|entireLineAsCommand
operator|=
name|entireLineAsCommand
expr_stmt|;
block|}
specifier|public
name|boolean
name|getEntireLineAsCommand
parameter_list|()
block|{
return|return
name|entireLineAsCommand
return|;
block|}
specifier|public
name|void
name|setHistoryFile
parameter_list|(
name|String
name|historyFile
parameter_list|)
block|{
name|this
operator|.
name|historyFile
operator|=
name|historyFile
expr_stmt|;
block|}
specifier|public
name|String
name|getHistoryFile
parameter_list|()
block|{
return|return
name|historyFile
return|;
block|}
specifier|public
name|void
name|setScriptFile
parameter_list|(
name|String
name|scriptFile
parameter_list|)
block|{
name|this
operator|.
name|scriptFile
operator|=
name|scriptFile
expr_stmt|;
block|}
specifier|public
name|String
name|getScriptFile
parameter_list|()
block|{
return|return
name|scriptFile
return|;
block|}
specifier|public
name|String
index|[]
name|getInitFiles
parameter_list|()
block|{
return|return
name|initFiles
return|;
block|}
specifier|public
name|void
name|setInitFiles
parameter_list|(
name|String
index|[]
name|initFiles
parameter_list|)
block|{
name|this
operator|.
name|initFiles
operator|=
name|initFiles
expr_stmt|;
block|}
specifier|public
name|void
name|setColor
parameter_list|(
name|boolean
name|color
parameter_list|)
block|{
name|this
operator|.
name|color
operator|=
name|color
expr_stmt|;
block|}
specifier|public
name|boolean
name|getColor
parameter_list|()
block|{
return|return
name|color
return|;
block|}
specifier|public
name|void
name|setShowHeader
parameter_list|(
name|boolean
name|showHeader
parameter_list|)
block|{
name|this
operator|.
name|showHeader
operator|=
name|showHeader
expr_stmt|;
block|}
specifier|public
name|boolean
name|getShowHeader
parameter_list|()
block|{
return|return
name|showHeader
return|;
block|}
specifier|public
name|void
name|setHeaderInterval
parameter_list|(
name|int
name|headerInterval
parameter_list|)
block|{
name|this
operator|.
name|headerInterval
operator|=
name|headerInterval
expr_stmt|;
block|}
specifier|public
name|int
name|getHeaderInterval
parameter_list|()
block|{
return|return
name|headerInterval
return|;
block|}
specifier|public
name|void
name|setForce
parameter_list|(
name|boolean
name|force
parameter_list|)
block|{
name|this
operator|.
name|force
operator|=
name|force
expr_stmt|;
block|}
specifier|public
name|boolean
name|getForce
parameter_list|()
block|{
return|return
name|force
return|;
block|}
specifier|public
name|void
name|setIncremental
parameter_list|(
name|boolean
name|incremental
parameter_list|)
block|{
name|this
operator|.
name|incremental
operator|=
name|incremental
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIncremental
parameter_list|()
block|{
return|return
name|incremental
return|;
block|}
specifier|public
name|void
name|setSilent
parameter_list|(
name|boolean
name|silent
parameter_list|)
block|{
name|this
operator|.
name|silent
operator|=
name|silent
expr_stmt|;
block|}
specifier|public
name|boolean
name|isSilent
parameter_list|()
block|{
return|return
name|silent
return|;
block|}
specifier|public
name|void
name|setAutosave
parameter_list|(
name|boolean
name|autosave
parameter_list|)
block|{
name|this
operator|.
name|autosave
operator|=
name|autosave
expr_stmt|;
block|}
specifier|public
name|boolean
name|getAutosave
parameter_list|()
block|{
return|return
name|autosave
return|;
block|}
specifier|public
name|void
name|setOutputFormat
parameter_list|(
name|String
name|outputFormat
parameter_list|)
block|{
if|if
condition|(
name|outputFormat
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"csv"
argument_list|)
operator|||
name|outputFormat
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"tsv"
argument_list|)
condition|)
block|{
name|beeLine
operator|.
name|info
argument_list|(
literal|"Format "
operator|+
name|outputFormat
operator|+
literal|" is deprecated, please use "
operator|+
name|outputFormat
operator|+
literal|"2"
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|outputFormat
operator|=
name|outputFormat
expr_stmt|;
block|}
specifier|public
name|String
name|getOutputFormat
parameter_list|()
block|{
return|return
name|outputFormat
return|;
block|}
specifier|public
name|void
name|setTrimScripts
parameter_list|(
name|boolean
name|trimScripts
parameter_list|)
block|{
name|this
operator|.
name|trimScripts
operator|=
name|trimScripts
expr_stmt|;
block|}
specifier|public
name|boolean
name|getTrimScripts
parameter_list|()
block|{
return|return
name|trimScripts
return|;
block|}
specifier|public
name|void
name|setMaxHeight
parameter_list|(
name|int
name|maxHeight
parameter_list|)
block|{
name|this
operator|.
name|maxHeight
operator|=
name|maxHeight
expr_stmt|;
block|}
specifier|public
name|int
name|getMaxHeight
parameter_list|()
block|{
return|return
name|maxHeight
return|;
block|}
specifier|public
name|File
name|getPropertiesFile
parameter_list|()
block|{
return|return
name|rcFile
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getHiveVariables
parameter_list|()
block|{
return|return
name|hiveVariables
return|;
block|}
specifier|public
name|void
name|setHiveVariables
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveVariables
parameter_list|)
block|{
name|this
operator|.
name|hiveVariables
operator|=
name|hiveVariables
expr_stmt|;
block|}
specifier|public
name|boolean
name|isAllowMultiLineCommand
parameter_list|()
block|{
return|return
name|allowMultiLineCommand
return|;
block|}
specifier|public
name|void
name|setAllowMultiLineCommand
parameter_list|(
name|boolean
name|allowMultiLineCommand
parameter_list|)
block|{
name|this
operator|.
name|allowMultiLineCommand
operator|=
name|allowMultiLineCommand
expr_stmt|;
block|}
comment|/**    * Use getNullString() to get the null string to be used.    * @return true if null representation should be an empty string    */
specifier|public
name|boolean
name|getNullEmptyString
parameter_list|()
block|{
return|return
name|nullEmptyString
return|;
block|}
specifier|public
name|void
name|setNullEmptyString
parameter_list|(
name|boolean
name|nullStringEmpty
parameter_list|)
block|{
name|this
operator|.
name|nullEmptyString
operator|=
name|nullStringEmpty
expr_stmt|;
block|}
specifier|public
name|String
name|getNullString
parameter_list|()
block|{
return|return
name|nullEmptyString
condition|?
literal|""
else|:
name|DEFAULT_NULL_STRING
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getHiveConfVariables
parameter_list|()
block|{
return|return
name|hiveConfVariables
return|;
block|}
specifier|public
name|void
name|setHiveConfVariables
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveConfVariables
parameter_list|)
block|{
name|this
operator|.
name|hiveConfVariables
operator|=
name|hiveConfVariables
expr_stmt|;
block|}
specifier|public
name|boolean
name|getTruncateTable
parameter_list|()
block|{
return|return
name|truncateTable
return|;
block|}
specifier|public
name|void
name|setTruncateTable
parameter_list|(
name|boolean
name|truncateTable
parameter_list|)
block|{
name|this
operator|.
name|truncateTable
operator|=
name|truncateTable
expr_stmt|;
block|}
specifier|public
name|char
name|getDelimiterForDSV
parameter_list|()
block|{
return|return
name|delimiterForDSV
return|;
block|}
specifier|public
name|void
name|setDelimiterForDSV
parameter_list|(
name|char
name|delimiterForDSV
parameter_list|)
block|{
name|this
operator|.
name|delimiterForDSV
operator|=
name|delimiterForDSV
expr_stmt|;
block|}
specifier|public
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
block|}
end_class

end_unit

