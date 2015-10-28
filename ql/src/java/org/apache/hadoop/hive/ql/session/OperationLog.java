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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|FileUtils
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
name|io
operator|.
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|sql
operator|.
name|SQLException
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
name|List
import|;
end_import

begin_comment
comment|/**  * OperationLog wraps the actual operation log file, and provides interface  * for accessing, reading, writing, and removing the file.  */
end_comment

begin_class
specifier|public
class|class
name|OperationLog
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|OperationLog
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|operationName
decl_stmt|;
specifier|private
specifier|final
name|LogFile
name|logFile
decl_stmt|;
specifier|private
name|LoggingLevel
name|opLoggingLevel
init|=
name|LoggingLevel
operator|.
name|UNKNOWN
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|LoggingLevel
block|{
name|NONE
block|,
name|EXECUTION
block|,
name|PERFORMANCE
block|,
name|VERBOSE
block|,
name|UNKNOWN
block|}
specifier|public
name|OperationLog
parameter_list|(
name|String
name|name
parameter_list|,
name|File
name|file
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|FileNotFoundException
block|{
name|operationName
operator|=
name|name
expr_stmt|;
name|logFile
operator|=
operator|new
name|LogFile
argument_list|(
name|file
argument_list|)
expr_stmt|;
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_LOGGING_OPERATION_ENABLED
argument_list|)
condition|)
block|{
name|String
name|logLevel
init|=
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_LOGGING_OPERATION_LEVEL
argument_list|)
decl_stmt|;
name|opLoggingLevel
operator|=
name|getLoggingLevel
argument_list|(
name|logLevel
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|LoggingLevel
name|getLoggingLevel
parameter_list|(
name|String
name|mode
parameter_list|)
block|{
if|if
condition|(
name|mode
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"none"
argument_list|)
condition|)
block|{
return|return
name|LoggingLevel
operator|.
name|NONE
return|;
block|}
elseif|else
if|if
condition|(
name|mode
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"execution"
argument_list|)
condition|)
block|{
return|return
name|LoggingLevel
operator|.
name|EXECUTION
return|;
block|}
elseif|else
if|if
condition|(
name|mode
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"verbose"
argument_list|)
condition|)
block|{
return|return
name|LoggingLevel
operator|.
name|VERBOSE
return|;
block|}
elseif|else
if|if
condition|(
name|mode
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"performance"
argument_list|)
condition|)
block|{
return|return
name|LoggingLevel
operator|.
name|PERFORMANCE
return|;
block|}
else|else
block|{
return|return
name|LoggingLevel
operator|.
name|UNKNOWN
return|;
block|}
block|}
specifier|public
name|LoggingLevel
name|getOpLoggingLevel
parameter_list|()
block|{
return|return
name|opLoggingLevel
return|;
block|}
comment|/**    * Singleton OperationLog object per thread.    */
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|OperationLog
argument_list|>
name|THREAD_LOCAL_OPERATION_LOG
init|=
operator|new
name|ThreadLocal
argument_list|<
name|OperationLog
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|OperationLog
name|initialValue
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
name|void
name|setCurrentOperationLog
parameter_list|(
name|OperationLog
name|operationLog
parameter_list|)
block|{
name|THREAD_LOCAL_OPERATION_LOG
operator|.
name|set
argument_list|(
name|operationLog
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|OperationLog
name|getCurrentOperationLog
parameter_list|()
block|{
return|return
name|THREAD_LOCAL_OPERATION_LOG
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|void
name|removeCurrentOperationLog
parameter_list|()
block|{
name|THREAD_LOCAL_OPERATION_LOG
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
comment|/**    * Write operation execution logs into log file    * @param operationLogMessage one line of log emitted from log4j    */
specifier|public
name|void
name|writeOperationLog
parameter_list|(
name|String
name|operationLogMessage
parameter_list|)
block|{
name|logFile
operator|.
name|write
argument_list|(
name|operationLogMessage
argument_list|)
expr_stmt|;
block|}
comment|/**    * Read operation execution logs from log file    * @param isFetchFirst true if the Enum FetchOrientation value is Fetch_First    * @param maxRows the max number of fetched lines from log    * @return    * @throws java.sql.SQLException    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|readOperationLog
parameter_list|(
name|boolean
name|isFetchFirst
parameter_list|,
name|long
name|maxRows
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
name|logFile
operator|.
name|read
argument_list|(
name|isFetchFirst
argument_list|,
name|maxRows
argument_list|)
return|;
block|}
comment|/**    * Close this OperationLog when operation is closed. The log file will be removed.    */
specifier|public
name|void
name|close
parameter_list|()
block|{
name|logFile
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
comment|/**    * Wrapper for read/write the operation log file    */
specifier|private
class|class
name|LogFile
block|{
specifier|private
specifier|final
name|File
name|file
decl_stmt|;
specifier|private
name|BufferedReader
name|in
decl_stmt|;
specifier|private
specifier|final
name|PrintStream
name|out
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|isRemoved
decl_stmt|;
name|LogFile
parameter_list|(
name|File
name|file
parameter_list|)
throws|throws
name|FileNotFoundException
block|{
name|this
operator|.
name|file
operator|=
name|file
expr_stmt|;
name|in
operator|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
operator|new
name|FileInputStream
argument_list|(
name|file
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|=
operator|new
name|PrintStream
argument_list|(
operator|new
name|FileOutputStream
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
name|isRemoved
operator|=
literal|false
expr_stmt|;
block|}
specifier|synchronized
name|void
name|write
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
comment|// write log to the file
name|out
operator|.
name|print
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
specifier|synchronized
name|List
argument_list|<
name|String
argument_list|>
name|read
parameter_list|(
name|boolean
name|isFetchFirst
parameter_list|,
name|long
name|maxRows
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// reset the BufferReader, if fetching from the beginning of the file
if|if
condition|(
name|isFetchFirst
condition|)
block|{
name|resetIn
argument_list|()
expr_stmt|;
block|}
return|return
name|readResults
argument_list|(
name|maxRows
argument_list|)
return|;
block|}
name|void
name|remove
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|in
operator|!=
literal|null
condition|)
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|out
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|FileUtils
operator|.
name|forceDelete
argument_list|(
name|file
argument_list|)
expr_stmt|;
name|isRemoved
operator|=
literal|true
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
name|error
argument_list|(
literal|"Failed to remove corresponding log file of operation: "
operator|+
name|operationName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|resetIn
parameter_list|()
block|{
if|if
condition|(
name|in
operator|!=
literal|null
condition|)
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|in
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|readResults
parameter_list|(
name|long
name|nLines
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
name|in
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|in
operator|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
operator|new
name|FileInputStream
argument_list|(
name|file
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
if|if
condition|(
name|isRemoved
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"The operation has been closed and its log file "
operator|+
name|file
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|" has been removed."
argument_list|,
name|e
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Operation Log file "
operator|+
name|file
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|" is not found."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
name|List
argument_list|<
name|String
argument_list|>
name|logs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|line
init|=
literal|""
decl_stmt|;
comment|// if nLines<= 0, read all lines in log file.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nLines
operator|||
name|nLines
operator|<=
literal|0
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|line
operator|=
name|in
operator|.
name|readLine
argument_list|()
expr_stmt|;
if|if
condition|(
name|line
operator|==
literal|null
condition|)
block|{
break|break;
block|}
else|else
block|{
name|logs
operator|.
name|add
argument_list|(
name|line
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|isRemoved
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"The operation has been closed and its log file "
operator|+
name|file
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|" has been removed."
argument_list|,
name|e
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Reading operation log file encountered an exception: "
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
return|return
name|logs
return|;
block|}
block|}
block|}
end_class

end_unit

