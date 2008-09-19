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
name|util
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Text
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
name|io
operator|.
name|LongWritable
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
name|plan
operator|.
name|scriptDesc
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
name|serde2
operator|.
name|Deserializer
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
name|SerDeException
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
name|SerDe
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
name|Serializer
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
name|mapred
operator|.
name|LineRecordReader
operator|.
name|LineReader
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
name|ScriptOperator
extends|extends
name|Operator
argument_list|<
name|scriptDesc
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
specifier|static
enum|enum
name|Counter
block|{
name|DESERIALIZE_ERRORS
block|,
name|SERIALIZE_ERRORS
block|}
specifier|transient
specifier|private
name|LongWritable
name|deserialize_error_count
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
specifier|transient
specifier|private
name|LongWritable
name|serialize_error_count
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
specifier|transient
name|DataOutputStream
name|scriptOut
decl_stmt|;
specifier|transient
name|DataInputStream
name|scriptErr
decl_stmt|;
specifier|transient
name|DataInputStream
name|scriptIn
decl_stmt|;
specifier|transient
name|Thread
name|outThread
decl_stmt|;
specifier|transient
name|Thread
name|errThread
decl_stmt|;
specifier|transient
name|Process
name|scriptPid
decl_stmt|;
specifier|transient
name|Configuration
name|hconf
decl_stmt|;
comment|// Input to the script
specifier|transient
name|Serializer
name|scriptInputSerializer
decl_stmt|;
comment|// Output from the script
specifier|transient
name|Deserializer
name|scriptOutputDeserializer
decl_stmt|;
specifier|transient
specifier|volatile
name|Throwable
name|scriptError
init|=
literal|null
decl_stmt|;
comment|/**    * addJobConfToEnvironment is shamelessly copied from hadoop streaming.    */
specifier|static
name|String
name|safeEnvVarName
parameter_list|(
name|String
name|var
parameter_list|)
block|{
name|StringBuffer
name|safe
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|int
name|len
init|=
name|var
operator|.
name|length
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|len
condition|;
name|i
operator|++
control|)
block|{
name|char
name|c
init|=
name|var
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|char
name|s
decl_stmt|;
if|if
condition|(
operator|(
name|c
operator|>=
literal|'0'
operator|&&
name|c
operator|<=
literal|'9'
operator|)
operator|||
operator|(
name|c
operator|>=
literal|'A'
operator|&&
name|c
operator|<=
literal|'Z'
operator|)
operator|||
operator|(
name|c
operator|>=
literal|'a'
operator|&&
name|c
operator|<=
literal|'z'
operator|)
condition|)
block|{
name|s
operator|=
name|c
expr_stmt|;
block|}
else|else
block|{
name|s
operator|=
literal|'_'
expr_stmt|;
block|}
name|safe
operator|.
name|append
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
return|return
name|safe
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|static
name|void
name|addJobConfToEnvironment
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
parameter_list|)
block|{
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|it
init|=
name|conf
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|en
init|=
operator|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|name
init|=
operator|(
name|String
operator|)
name|en
operator|.
name|getKey
argument_list|()
decl_stmt|;
comment|//String value = (String)en.getValue(); // does not apply variable expansion
name|String
name|value
init|=
name|conf
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
comment|// does variable expansion
name|name
operator|=
name|safeEnvVarName
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|env
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|statsMap
operator|.
name|put
argument_list|(
name|Counter
operator|.
name|DESERIALIZE_ERRORS
argument_list|,
name|deserialize_error_count
argument_list|)
expr_stmt|;
name|statsMap
operator|.
name|put
argument_list|(
name|Counter
operator|.
name|SERIALIZE_ERRORS
argument_list|,
name|serialize_error_count
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|hconf
operator|=
name|hconf
expr_stmt|;
name|scriptOutputDeserializer
operator|=
name|conf
operator|.
name|getScriptOutputInfo
argument_list|()
operator|.
name|getDeserializerClass
argument_list|()
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|scriptOutputDeserializer
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
name|conf
operator|.
name|getScriptOutputInfo
argument_list|()
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
name|scriptInputSerializer
operator|=
operator|(
name|Serializer
operator|)
name|conf
operator|.
name|getScriptInputInfo
argument_list|()
operator|.
name|getDeserializerClass
argument_list|()
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|scriptInputSerializer
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
name|conf
operator|.
name|getScriptInputInfo
argument_list|()
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
name|String
index|[]
name|cmdArgs
init|=
name|splitArgs
argument_list|(
name|conf
operator|.
name|getScriptCmd
argument_list|()
argument_list|)
decl_stmt|;
name|String
index|[]
name|wrappedCmdArgs
init|=
name|addWrapper
argument_list|(
name|cmdArgs
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Executing "
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
name|wrappedCmdArgs
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"tablename="
operator|+
name|hconf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVETABLENAME
operator|.
name|varname
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"partname="
operator|+
name|hconf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEPARTITIONNAME
operator|.
name|varname
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"alias="
operator|+
name|alias
argument_list|)
expr_stmt|;
name|ProcessBuilder
name|pb
init|=
operator|new
name|ProcessBuilder
argument_list|(
name|wrappedCmdArgs
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
init|=
name|pb
operator|.
name|environment
argument_list|()
decl_stmt|;
name|addJobConfToEnvironment
argument_list|(
name|hconf
argument_list|,
name|env
argument_list|)
expr_stmt|;
name|env
operator|.
name|put
argument_list|(
name|safeEnvVarName
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEALIAS
operator|.
name|varname
argument_list|)
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|alias
argument_list|)
argument_list|)
expr_stmt|;
name|scriptPid
operator|=
name|pb
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// Runtime.getRuntime().exec(wrappedCmdArgs);
name|scriptOut
operator|=
operator|new
name|DataOutputStream
argument_list|(
operator|new
name|BufferedOutputStream
argument_list|(
name|scriptPid
operator|.
name|getOutputStream
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|scriptIn
operator|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|BufferedInputStream
argument_list|(
name|scriptPid
operator|.
name|getInputStream
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|scriptErr
operator|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|BufferedInputStream
argument_list|(
name|scriptPid
operator|.
name|getErrorStream
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|outThread
operator|=
operator|new
name|StreamThread
argument_list|(
name|scriptIn
argument_list|,
operator|new
name|OutputStreamProcessor
argument_list|(
name|scriptOutputDeserializer
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
argument_list|,
literal|"OutputProcessor"
argument_list|)
expr_stmt|;
name|outThread
operator|.
name|start
argument_list|()
expr_stmt|;
name|errThread
operator|=
operator|new
name|StreamThread
argument_list|(
name|scriptErr
argument_list|,
operator|new
name|ErrorStreamProcessor
argument_list|()
argument_list|,
literal|"ErrorProcessor"
argument_list|)
expr_stmt|;
name|errThread
operator|.
name|start
argument_list|()
expr_stmt|;
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
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Cannot initialize ScriptOperator"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
name|Text
name|text
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|scriptError
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|scriptError
argument_list|)
throw|;
block|}
try|try
block|{
name|text
operator|=
operator|(
name|Text
operator|)
name|scriptInputSerializer
operator|.
name|serialize
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|)
expr_stmt|;
name|scriptOut
operator|.
name|write
argument_list|(
name|text
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|text
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|scriptOut
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|newLineCode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error in serializing the row: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|scriptError
operator|=
name|e
expr_stmt|;
name|serialize_error_count
operator|.
name|set
argument_list|(
name|serialize_error_count
operator|.
name|get
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error in writing to script: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|scriptError
operator|=
name|e
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
name|boolean
name|new_abort
init|=
name|abort
decl_stmt|;
if|if
condition|(
operator|!
name|abort
condition|)
block|{
comment|// everything ok. try normal shutdown
try|try
block|{
name|scriptOut
operator|.
name|flush
argument_list|()
expr_stmt|;
name|scriptOut
operator|.
name|close
argument_list|()
expr_stmt|;
name|int
name|exitVal
init|=
name|scriptPid
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
literal|"Script failed with code "
operator|+
name|exitVal
argument_list|)
expr_stmt|;
name|new_abort
operator|=
literal|true
expr_stmt|;
block|}
empty_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|new_abort
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{ }
block|}
try|try
block|{
comment|// try these best effort
name|outThread
operator|.
name|join
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|errThread
operator|.
name|join
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|scriptPid
operator|.
name|destroy
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{}
name|super
operator|.
name|close
argument_list|(
name|new_abort
argument_list|)
expr_stmt|;
if|if
condition|(
name|new_abort
operator|&&
operator|!
name|abort
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Hit error while closing .."
argument_list|)
throw|;
block|}
block|}
interface|interface
name|StreamProcessor
block|{
specifier|public
name|void
name|processLine
parameter_list|(
name|Text
name|line
parameter_list|)
throws|throws
name|HiveException
function_decl|;
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|HiveException
function_decl|;
block|}
class|class
name|OutputStreamProcessor
implements|implements
name|StreamProcessor
block|{
name|Object
name|row
decl_stmt|;
name|ObjectInspector
name|rowInspector
decl_stmt|;
specifier|public
name|OutputStreamProcessor
parameter_list|(
name|ObjectInspector
name|rowInspector
parameter_list|)
block|{
name|this
operator|.
name|rowInspector
operator|=
name|rowInspector
expr_stmt|;
block|}
specifier|public
name|void
name|processLine
parameter_list|(
name|Text
name|line
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|row
operator|=
name|scriptOutputDeserializer
operator|.
name|deserialize
argument_list|(
name|line
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
name|deserialize_error_count
operator|.
name|set
argument_list|(
name|deserialize_error_count
operator|.
name|get
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
return|return;
block|}
name|forward
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{     }
block|}
class|class
name|ErrorStreamProcessor
implements|implements
name|StreamProcessor
block|{
specifier|public
name|ErrorStreamProcessor
parameter_list|()
block|{}
specifier|public
name|void
name|processLine
parameter_list|(
name|Text
name|line
parameter_list|)
throws|throws
name|HiveException
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|line
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{     }
block|}
class|class
name|StreamThread
extends|extends
name|Thread
block|{
name|InputStream
name|in
decl_stmt|;
name|StreamProcessor
name|proc
decl_stmt|;
name|String
name|name
decl_stmt|;
name|StreamThread
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|StreamProcessor
name|proc
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|in
operator|=
name|in
expr_stmt|;
name|this
operator|.
name|proc
operator|=
name|proc
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|run
parameter_list|()
block|{
name|LineReader
name|lineReader
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Text
name|row
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|lineReader
operator|=
operator|new
name|LineReader
argument_list|(
operator|(
name|InputStream
operator|)
name|in
argument_list|,
name|hconf
argument_list|)
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|row
operator|.
name|clear
argument_list|()
expr_stmt|;
name|long
name|bytes
init|=
name|lineReader
operator|.
name|readLine
argument_list|(
name|row
argument_list|)
decl_stmt|;
if|if
condition|(
name|bytes
operator|<=
literal|0
condition|)
block|{
break|break;
block|}
name|proc
operator|.
name|processLine
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"StreamThread "
operator|+
name|name
operator|+
literal|" done"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|th
parameter_list|)
block|{
name|scriptError
operator|=
name|th
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|th
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
if|if
condition|(
name|lineReader
operator|!=
literal|null
condition|)
block|{
name|lineReader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
name|proc
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|name
operator|+
literal|": error in closing .."
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    *  Wrap the script in a wrapper that allows admins to control    **/
specifier|protected
name|String
index|[]
name|addWrapper
parameter_list|(
name|String
index|[]
name|inArgs
parameter_list|)
block|{
name|String
name|wrapper
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SCRIPTWRAPPER
argument_list|)
decl_stmt|;
if|if
condition|(
name|wrapper
operator|==
literal|null
condition|)
block|{
return|return
name|inArgs
return|;
block|}
name|String
index|[]
name|wrapComponents
init|=
name|splitArgs
argument_list|(
name|wrapper
argument_list|)
decl_stmt|;
name|int
name|totallength
init|=
name|wrapComponents
operator|.
name|length
operator|+
name|inArgs
operator|.
name|length
decl_stmt|;
name|String
index|[]
name|finalArgv
init|=
operator|new
name|String
index|[
name|totallength
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|wrapComponents
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|finalArgv
index|[
name|i
index|]
operator|=
name|wrapComponents
index|[
name|i
index|]
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|inArgs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|finalArgv
index|[
name|wrapComponents
operator|.
name|length
operator|+
name|i
index|]
operator|=
name|inArgs
index|[
name|i
index|]
expr_stmt|;
block|}
return|return
operator|(
name|finalArgv
operator|)
return|;
block|}
comment|// Code below shameless borrowed from Hadoop Streaming
specifier|public
specifier|static
name|String
index|[]
name|splitArgs
parameter_list|(
name|String
name|args
parameter_list|)
block|{
specifier|final
name|int
name|OUTSIDE
init|=
literal|1
decl_stmt|;
specifier|final
name|int
name|SINGLEQ
init|=
literal|2
decl_stmt|;
specifier|final
name|int
name|DOUBLEQ
init|=
literal|3
decl_stmt|;
name|ArrayList
name|argList
init|=
operator|new
name|ArrayList
argument_list|()
decl_stmt|;
name|char
index|[]
name|ch
init|=
name|args
operator|.
name|toCharArray
argument_list|()
decl_stmt|;
name|int
name|clen
init|=
name|ch
operator|.
name|length
decl_stmt|;
name|int
name|state
init|=
name|OUTSIDE
decl_stmt|;
name|int
name|argstart
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<=
name|clen
condition|;
name|c
operator|++
control|)
block|{
name|boolean
name|last
init|=
operator|(
name|c
operator|==
name|clen
operator|)
decl_stmt|;
name|int
name|lastState
init|=
name|state
decl_stmt|;
name|boolean
name|endToken
init|=
literal|false
decl_stmt|;
if|if
condition|(
operator|!
name|last
condition|)
block|{
if|if
condition|(
name|ch
index|[
name|c
index|]
operator|==
literal|'\''
condition|)
block|{
if|if
condition|(
name|state
operator|==
name|OUTSIDE
condition|)
block|{
name|state
operator|=
name|SINGLEQ
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|state
operator|==
name|SINGLEQ
condition|)
block|{
name|state
operator|=
name|OUTSIDE
expr_stmt|;
block|}
name|endToken
operator|=
operator|(
name|state
operator|!=
name|lastState
operator|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ch
index|[
name|c
index|]
operator|==
literal|'"'
condition|)
block|{
if|if
condition|(
name|state
operator|==
name|OUTSIDE
condition|)
block|{
name|state
operator|=
name|DOUBLEQ
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|state
operator|==
name|DOUBLEQ
condition|)
block|{
name|state
operator|=
name|OUTSIDE
expr_stmt|;
block|}
name|endToken
operator|=
operator|(
name|state
operator|!=
name|lastState
operator|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ch
index|[
name|c
index|]
operator|==
literal|' '
condition|)
block|{
if|if
condition|(
name|state
operator|==
name|OUTSIDE
condition|)
block|{
name|endToken
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|last
operator|||
name|endToken
condition|)
block|{
if|if
condition|(
name|c
operator|==
name|argstart
condition|)
block|{
comment|// unquoted space
block|}
else|else
block|{
name|String
name|a
decl_stmt|;
name|a
operator|=
name|args
operator|.
name|substring
argument_list|(
name|argstart
argument_list|,
name|c
argument_list|)
expr_stmt|;
name|argList
operator|.
name|add
argument_list|(
name|a
argument_list|)
expr_stmt|;
block|}
name|argstart
operator|=
name|c
operator|+
literal|1
expr_stmt|;
name|lastState
operator|=
name|state
expr_stmt|;
block|}
block|}
return|return
operator|(
name|String
index|[]
operator|)
name|argList
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
return|;
block|}
block|}
end_class

end_unit

