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
name|jdbc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|ResultSet
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
name|sql
operator|.
name|SQLWarning
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
name|Map
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
name|thrift
operator|.
name|TCLIService
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
name|thrift
operator|.
name|TCancelOperationReq
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
name|thrift
operator|.
name|TCancelOperationResp
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
name|thrift
operator|.
name|TCloseOperationReq
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
name|thrift
operator|.
name|TCloseOperationResp
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
name|thrift
operator|.
name|TExecuteStatementReq
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
name|thrift
operator|.
name|TExecuteStatementResp
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
name|thrift
operator|.
name|TGetOperationStatusReq
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
name|thrift
operator|.
name|TGetOperationStatusResp
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
name|thrift
operator|.
name|TOperationHandle
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
name|thrift
operator|.
name|TSessionHandle
import|;
end_import

begin_comment
comment|/**  * HiveStatement.  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveStatement
implements|implements
name|java
operator|.
name|sql
operator|.
name|Statement
block|{
specifier|private
name|TCLIService
operator|.
name|Iface
name|client
decl_stmt|;
specifier|private
name|TOperationHandle
name|stmtHandle
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|TSessionHandle
name|sessHandle
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sessConf
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
name|int
name|fetchSize
init|=
literal|50
decl_stmt|;
comment|/**    * We need to keep a reference to the result set to support the following:    *<code>    * statement.execute(String sql);    * statement.getResultSet();    *</code>.    */
specifier|private
name|ResultSet
name|resultSet
init|=
literal|null
decl_stmt|;
comment|/**    * Sets the limit for the maximum number of rows that any ResultSet object produced by this    * Statement can contain to the given number. If the limit is exceeded, the excess rows    * are silently dropped. The value must be>= 0, and 0 means there is not limit.    */
specifier|private
name|int
name|maxRows
init|=
literal|0
decl_stmt|;
comment|/**    * Add SQLWarnings to the warningChain if needed.    */
specifier|private
name|SQLWarning
name|warningChain
init|=
literal|null
decl_stmt|;
comment|/**    * Keep state so we can fail certain calls made after close().    */
specifier|private
name|boolean
name|isClosed
init|=
literal|false
decl_stmt|;
comment|/**    *    */
specifier|public
name|HiveStatement
parameter_list|(
name|TCLIService
operator|.
name|Iface
name|client
parameter_list|,
name|TSessionHandle
name|sessHandle
parameter_list|)
block|{
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|sessHandle
operator|=
name|sessHandle
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#addBatch(java.lang.String)    */
specifier|public
name|void
name|addBatch
parameter_list|(
name|String
name|sql
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#cancel()    */
specifier|public
name|void
name|cancel
parameter_list|()
throws|throws
name|SQLException
block|{
if|if
condition|(
name|isClosed
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Can't cancel after statement has been closed"
argument_list|)
throw|;
block|}
if|if
condition|(
name|stmtHandle
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|TCancelOperationReq
name|cancelReq
init|=
operator|new
name|TCancelOperationReq
argument_list|()
decl_stmt|;
name|cancelReq
operator|.
name|setOperationHandle
argument_list|(
name|stmtHandle
argument_list|)
expr_stmt|;
try|try
block|{
name|TCancelOperationResp
name|cancelResp
init|=
name|client
operator|.
name|CancelOperation
argument_list|(
name|cancelReq
argument_list|)
decl_stmt|;
name|Utils
operator|.
name|verifySuccessWithInfo
argument_list|(
name|cancelResp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
literal|"08S01"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#clearBatch()    */
specifier|public
name|void
name|clearBatch
parameter_list|()
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#clearWarnings()    */
specifier|public
name|void
name|clearWarnings
parameter_list|()
throws|throws
name|SQLException
block|{
name|warningChain
operator|=
literal|null
expr_stmt|;
block|}
name|void
name|closeClientOperation
parameter_list|()
throws|throws
name|SQLException
block|{
try|try
block|{
if|if
condition|(
name|stmtHandle
operator|!=
literal|null
condition|)
block|{
name|TCloseOperationReq
name|closeReq
init|=
operator|new
name|TCloseOperationReq
argument_list|()
decl_stmt|;
name|closeReq
operator|.
name|setOperationHandle
argument_list|(
name|stmtHandle
argument_list|)
expr_stmt|;
name|TCloseOperationResp
name|closeResp
init|=
name|client
operator|.
name|CloseOperation
argument_list|(
name|closeReq
argument_list|)
decl_stmt|;
name|Utils
operator|.
name|verifySuccessWithInfo
argument_list|(
name|closeResp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
literal|"08S01"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|stmtHandle
operator|=
literal|null
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#close()    */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|SQLException
block|{
if|if
condition|(
name|isClosed
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|stmtHandle
operator|!=
literal|null
condition|)
block|{
name|closeClientOperation
argument_list|()
expr_stmt|;
block|}
name|client
operator|=
literal|null
expr_stmt|;
name|resultSet
operator|=
literal|null
expr_stmt|;
name|isClosed
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|closeOnCompletion
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// JDK 1.7
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#execute(java.lang.String)    */
specifier|public
name|boolean
name|execute
parameter_list|(
name|String
name|sql
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
name|isClosed
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Can't execute after statement has been closed"
argument_list|)
throw|;
block|}
try|try
block|{
if|if
condition|(
name|stmtHandle
operator|!=
literal|null
condition|)
block|{
name|closeClientOperation
argument_list|()
expr_stmt|;
block|}
name|TExecuteStatementReq
name|execReq
init|=
operator|new
name|TExecuteStatementReq
argument_list|(
name|sessHandle
argument_list|,
name|sql
argument_list|)
decl_stmt|;
name|execReq
operator|.
name|setConfOverlay
argument_list|(
name|sessConf
argument_list|)
expr_stmt|;
name|TExecuteStatementResp
name|execResp
init|=
name|client
operator|.
name|ExecuteStatement
argument_list|(
name|execReq
argument_list|)
decl_stmt|;
name|Utils
operator|.
name|verifySuccessWithInfo
argument_list|(
name|execResp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|stmtHandle
operator|=
name|execResp
operator|.
name|getOperationHandle
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|eS
parameter_list|)
block|{
throw|throw
name|eS
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
name|ex
operator|.
name|toString
argument_list|()
argument_list|,
literal|"08S01"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|stmtHandle
operator|.
name|isHasResultSet
argument_list|()
condition|)
block|{
comment|// Poll until the query has completed one way or another. DML queries will not return a result
comment|// set, but we should not return from this method until the query has completed to avoid
comment|// racing with possible subsequent session shutdown, or queries that depend on the results
comment|// materialised here.
name|TGetOperationStatusReq
name|statusReq
init|=
operator|new
name|TGetOperationStatusReq
argument_list|(
name|stmtHandle
argument_list|)
decl_stmt|;
name|boolean
name|requestComplete
init|=
literal|false
decl_stmt|;
while|while
condition|(
operator|!
name|requestComplete
condition|)
block|{
try|try
block|{
name|TGetOperationStatusResp
name|statusResp
init|=
name|client
operator|.
name|GetOperationStatus
argument_list|(
name|statusReq
argument_list|)
decl_stmt|;
name|Utils
operator|.
name|verifySuccessWithInfo
argument_list|(
name|statusResp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|statusResp
operator|.
name|isSetOperationState
argument_list|()
condition|)
block|{
switch|switch
condition|(
name|statusResp
operator|.
name|getOperationState
argument_list|()
condition|)
block|{
case|case
name|CLOSED_STATE
case|:
case|case
name|FINISHED_STATE
case|:
return|return
literal|false
return|;
case|case
name|CANCELED_STATE
case|:
comment|// 01000 -> warning
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Query was cancelled"
argument_list|,
literal|"01000"
argument_list|)
throw|;
case|case
name|ERROR_STATE
case|:
comment|// HY000 -> general error
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Query failed"
argument_list|,
literal|"HY000"
argument_list|)
throw|;
case|case
name|UKNOWN_STATE
case|:
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Unknown query"
argument_list|,
literal|"HY000"
argument_list|)
throw|;
case|case
name|INITIALIZED_STATE
case|:
case|case
name|RUNNING_STATE
case|:
break|break;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
name|ex
operator|.
name|toString
argument_list|()
argument_list|,
literal|"08S01"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
comment|// Ignore
block|}
block|}
return|return
literal|false
return|;
block|}
name|resultSet
operator|=
operator|new
name|HiveQueryResultSet
operator|.
name|Builder
argument_list|()
operator|.
name|setClient
argument_list|(
name|client
argument_list|)
operator|.
name|setSessionHandle
argument_list|(
name|sessHandle
argument_list|)
operator|.
name|setStmtHandle
argument_list|(
name|stmtHandle
argument_list|)
operator|.
name|setHiveStatement
argument_list|(
name|this
argument_list|)
operator|.
name|setMaxRows
argument_list|(
name|maxRows
argument_list|)
operator|.
name|setFetchSize
argument_list|(
name|fetchSize
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#execute(java.lang.String, int)    */
specifier|public
name|boolean
name|execute
parameter_list|(
name|String
name|sql
parameter_list|,
name|int
name|autoGeneratedKeys
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#execute(java.lang.String, int[])    */
specifier|public
name|boolean
name|execute
parameter_list|(
name|String
name|sql
parameter_list|,
name|int
index|[]
name|columnIndexes
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])    */
specifier|public
name|boolean
name|execute
parameter_list|(
name|String
name|sql
parameter_list|,
name|String
index|[]
name|columnNames
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#executeBatch()    */
specifier|public
name|int
index|[]
name|executeBatch
parameter_list|()
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#executeQuery(java.lang.String)    */
specifier|public
name|ResultSet
name|executeQuery
parameter_list|(
name|String
name|sql
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
operator|!
name|execute
argument_list|(
name|sql
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"The query did not generate a result set!"
argument_list|)
throw|;
block|}
return|return
name|resultSet
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#executeUpdate(java.lang.String)    */
specifier|public
name|int
name|executeUpdate
parameter_list|(
name|String
name|sql
parameter_list|)
throws|throws
name|SQLException
block|{
name|execute
argument_list|(
name|sql
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#executeUpdate(java.lang.String, int)    */
specifier|public
name|int
name|executeUpdate
parameter_list|(
name|String
name|sql
parameter_list|,
name|int
name|autoGeneratedKeys
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#executeUpdate(java.lang.String, int[])    */
specifier|public
name|int
name|executeUpdate
parameter_list|(
name|String
name|sql
parameter_list|,
name|int
index|[]
name|columnIndexes
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])    */
specifier|public
name|int
name|executeUpdate
parameter_list|(
name|String
name|sql
parameter_list|,
name|String
index|[]
name|columnNames
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getConnection()    */
specifier|public
name|Connection
name|getConnection
parameter_list|()
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getFetchDirection()    */
specifier|public
name|int
name|getFetchDirection
parameter_list|()
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getFetchSize()    */
specifier|public
name|int
name|getFetchSize
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
name|fetchSize
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getGeneratedKeys()    */
specifier|public
name|ResultSet
name|getGeneratedKeys
parameter_list|()
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getMaxFieldSize()    */
specifier|public
name|int
name|getMaxFieldSize
parameter_list|()
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getMaxRows()    */
specifier|public
name|int
name|getMaxRows
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
name|maxRows
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getMoreResults()    */
specifier|public
name|boolean
name|getMoreResults
parameter_list|()
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getMoreResults(int)    */
specifier|public
name|boolean
name|getMoreResults
parameter_list|(
name|int
name|current
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getQueryTimeout()    */
specifier|public
name|int
name|getQueryTimeout
parameter_list|()
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getResultSet()    */
specifier|public
name|ResultSet
name|getResultSet
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
name|resultSet
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getResultSetConcurrency()    */
specifier|public
name|int
name|getResultSetConcurrency
parameter_list|()
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getResultSetHoldability()    */
specifier|public
name|int
name|getResultSetHoldability
parameter_list|()
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getResultSetType()    */
specifier|public
name|int
name|getResultSetType
parameter_list|()
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getUpdateCount()    */
specifier|public
name|int
name|getUpdateCount
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
literal|0
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#getWarnings()    */
specifier|public
name|SQLWarning
name|getWarnings
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
name|warningChain
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#isClosed()    */
specifier|public
name|boolean
name|isClosed
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
name|isClosed
return|;
block|}
specifier|public
name|boolean
name|isCloseOnCompletion
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// JDK 1.7
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#isPoolable()    */
specifier|public
name|boolean
name|isPoolable
parameter_list|()
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#setCursorName(java.lang.String)    */
specifier|public
name|void
name|setCursorName
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#setEscapeProcessing(boolean)    */
specifier|public
name|void
name|setEscapeProcessing
parameter_list|(
name|boolean
name|enable
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#setFetchDirection(int)    */
specifier|public
name|void
name|setFetchDirection
parameter_list|(
name|int
name|direction
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#setFetchSize(int)    */
specifier|public
name|void
name|setFetchSize
parameter_list|(
name|int
name|rows
parameter_list|)
throws|throws
name|SQLException
block|{
name|fetchSize
operator|=
name|rows
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#setMaxFieldSize(int)    */
specifier|public
name|void
name|setMaxFieldSize
parameter_list|(
name|int
name|max
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#setMaxRows(int)    */
specifier|public
name|void
name|setMaxRows
parameter_list|(
name|int
name|max
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
name|max
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"max must be>= 0"
argument_list|)
throw|;
block|}
name|maxRows
operator|=
name|max
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#setPoolable(boolean)    */
specifier|public
name|void
name|setPoolable
parameter_list|(
name|boolean
name|poolable
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Statement#setQueryTimeout(int)    */
specifier|public
name|void
name|setQueryTimeout
parameter_list|(
name|int
name|seconds
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)    */
specifier|public
name|boolean
name|isWrapperFor
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|iface
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Wrapper#unwrap(java.lang.Class)    */
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|unwrap
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|iface
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

