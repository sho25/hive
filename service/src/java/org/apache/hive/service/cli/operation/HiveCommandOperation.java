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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|operation
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|FileNotFoundException
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
name|FileReader
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
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
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
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Schema
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
name|CommandProcessorResponse
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
name|io
operator|.
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|FetchOrientation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|HiveSQLException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|OperationState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|RowSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|TableSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|session
operator|.
name|HiveSession
import|;
end_import

begin_comment
comment|/**  * Executes a HiveCommand  */
end_comment

begin_class
specifier|public
class|class
name|HiveCommandOperation
extends|extends
name|ExecuteStatementOperation
block|{
specifier|private
name|CommandProcessorResponse
name|response
decl_stmt|;
specifier|private
name|CommandProcessor
name|commandProcessor
decl_stmt|;
specifier|private
name|TableSchema
name|resultSchema
init|=
literal|null
decl_stmt|;
comment|/**    * For processors other than Hive queries (Driver), they output to session.out (a temp file)    * first and the fetchOne/fetchN/fetchAll functions get the output from pipeIn.    */
specifier|private
name|BufferedReader
name|resultReader
decl_stmt|;
specifier|protected
name|HiveCommandOperation
parameter_list|(
name|HiveSession
name|parentSession
parameter_list|,
name|String
name|statement
parameter_list|,
name|CommandProcessor
name|commandProcessor
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
parameter_list|)
block|{
name|super
argument_list|(
name|parentSession
argument_list|,
name|statement
argument_list|,
name|confOverlay
argument_list|)
expr_stmt|;
name|this
operator|.
name|commandProcessor
operator|=
name|commandProcessor
expr_stmt|;
name|setupSessionIO
argument_list|(
name|parentSession
operator|.
name|getSessionState
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setupSessionIO
parameter_list|(
name|SessionState
name|sessionState
parameter_list|)
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Putting temp output to file "
operator|+
name|sessionState
operator|.
name|getTmpOutputFile
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sessionState
operator|.
name|in
operator|=
literal|null
expr_stmt|;
comment|// hive server's session input stream is not used
comment|// open a per-session file in auto-flush mode for writing temp results
name|sessionState
operator|.
name|out
operator|=
operator|new
name|PrintStream
argument_list|(
operator|new
name|FileOutputStream
argument_list|(
name|sessionState
operator|.
name|getTmpOutputFile
argument_list|()
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
comment|// TODO: for hadoop jobs, progress is printed out to session.err,
comment|// we should find a way to feed back job progress to client
name|sessionState
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
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error in creating temp output file "
argument_list|,
name|e
argument_list|)
expr_stmt|;
try|try
block|{
name|sessionState
operator|.
name|in
operator|=
literal|null
expr_stmt|;
name|sessionState
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
name|sessionState
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
name|ee
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error creating PrintStream"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|ee
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|sessionState
operator|.
name|out
operator|=
literal|null
expr_stmt|;
name|sessionState
operator|.
name|err
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|tearDownSessionIO
parameter_list|()
block|{
name|IOUtils
operator|.
name|cleanup
argument_list|(
name|LOG
argument_list|,
name|parentSession
operator|.
name|getSessionState
argument_list|()
operator|.
name|out
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|cleanup
argument_list|(
name|LOG
argument_list|,
name|parentSession
operator|.
name|getSessionState
argument_list|()
operator|.
name|err
argument_list|)
expr_stmt|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.operation.Operation#run()    */
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
try|try
block|{
name|String
name|command
init|=
name|getStatement
argument_list|()
operator|.
name|trim
argument_list|()
decl_stmt|;
name|String
index|[]
name|tokens
init|=
name|statement
operator|.
name|split
argument_list|(
literal|"\\s"
argument_list|)
decl_stmt|;
name|String
name|commandArgs
init|=
name|command
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
name|response
operator|=
name|commandProcessor
operator|.
name|run
argument_list|(
name|commandArgs
argument_list|)
expr_stmt|;
name|int
name|returnCode
init|=
name|response
operator|.
name|getResponseCode
argument_list|()
decl_stmt|;
name|String
name|sqlState
init|=
name|response
operator|.
name|getSQLState
argument_list|()
decl_stmt|;
name|String
name|errorMessage
init|=
name|response
operator|.
name|getErrorMessage
argument_list|()
decl_stmt|;
name|Schema
name|schema
init|=
name|response
operator|.
name|getSchema
argument_list|()
decl_stmt|;
if|if
condition|(
name|schema
operator|!=
literal|null
condition|)
block|{
name|setHasResultSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|resultSchema
operator|=
operator|new
name|TableSchema
argument_list|(
name|schema
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setHasResultSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|resultSchema
operator|=
operator|new
name|TableSchema
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|ERROR
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"Error running query: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|setState
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|)
expr_stmt|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.operation.Operation#close()    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|CLOSED
argument_list|)
expr_stmt|;
name|tearDownSessionIO
argument_list|()
expr_stmt|;
name|cleanTmpFile
argument_list|()
expr_stmt|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.operation.Operation#getResultSetSchema()    */
annotation|@
name|Override
specifier|public
name|TableSchema
name|getResultSetSchema
parameter_list|()
throws|throws
name|HiveSQLException
block|{
return|return
name|resultSchema
return|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.operation.Operation#getNextRowSet(org.apache.hive.service.cli.FetchOrientation, long)    */
annotation|@
name|Override
specifier|public
name|RowSet
name|getNextRowSet
parameter_list|(
name|FetchOrientation
name|orientation
parameter_list|,
name|long
name|maxRows
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|rows
init|=
name|readResults
argument_list|(
operator|(
name|int
operator|)
name|maxRows
argument_list|)
decl_stmt|;
name|RowSet
name|rowSet
init|=
operator|new
name|RowSet
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|row
range|:
name|rows
control|)
block|{
name|rowSet
operator|.
name|addRow
argument_list|(
name|resultSchema
argument_list|,
operator|new
name|String
index|[]
block|{
name|row
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|rowSet
return|;
block|}
comment|/**    * Reads the temporary results for non-Hive (non-Driver) commands to the    * resulting List of strings.    * @param results list of strings containing the results    * @param nLines number of lines read at once. If it is<= 0, then read all lines.    */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|readResults
parameter_list|(
name|int
name|nLines
parameter_list|)
throws|throws
name|HiveSQLException
block|{
if|if
condition|(
name|resultReader
operator|==
literal|null
condition|)
block|{
name|SessionState
name|sessionState
init|=
name|getParentSession
argument_list|()
operator|.
name|getSessionState
argument_list|()
decl_stmt|;
name|File
name|tmp
init|=
name|sessionState
operator|.
name|getTmpOutputFile
argument_list|()
decl_stmt|;
try|try
block|{
name|resultReader
operator|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|FileReader
argument_list|(
name|tmp
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
name|LOG
operator|.
name|error
argument_list|(
literal|"File "
operator|+
name|tmp
operator|+
literal|" not found. "
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
name|List
argument_list|<
name|String
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
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
name|nLines
operator|||
name|nLines
operator|<=
literal|0
condition|;
operator|++
name|i
control|)
block|{
try|try
block|{
name|String
name|line
init|=
name|resultReader
operator|.
name|readLine
argument_list|()
decl_stmt|;
if|if
condition|(
name|line
operator|==
literal|null
condition|)
block|{
comment|// reached the end of the result file
break|break;
block|}
else|else
block|{
name|results
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Reading temp results encountered an exception: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|results
return|;
block|}
specifier|private
name|void
name|cleanTmpFile
parameter_list|()
block|{
if|if
condition|(
name|resultReader
operator|!=
literal|null
condition|)
block|{
name|SessionState
name|sessionState
init|=
name|getParentSession
argument_list|()
operator|.
name|getSessionState
argument_list|()
decl_stmt|;
name|File
name|tmp
init|=
name|sessionState
operator|.
name|getTmpOutputFile
argument_list|()
decl_stmt|;
name|IOUtils
operator|.
name|cleanup
argument_list|(
name|LOG
argument_list|,
name|resultReader
argument_list|)
expr_stmt|;
name|tmp
operator|.
name|delete
argument_list|()
expr_stmt|;
name|resultReader
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

