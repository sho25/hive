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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|service
operator|.
name|HiveInterface
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
name|service
operator|.
name|HiveServerException
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
name|JdbcSessionState
name|session
decl_stmt|;
name|HiveInterface
name|client
decl_stmt|;
comment|/**    * We need to keep a reference to the result set to support the following:    *<code>    * statement.execute(String sql);    * statement.getResultSet();    *</code>.    */
name|ResultSet
name|resultSet
init|=
literal|null
decl_stmt|;
comment|/**    * The maximum number of rows this statement should return (0 => all rows).    */
name|int
name|maxRows
init|=
literal|0
decl_stmt|;
comment|/**    * Add SQLWarnings to the warningChain if needed.    */
name|SQLWarning
name|warningChain
init|=
literal|null
decl_stmt|;
comment|/**    * Keep state so we can fail certain calls made after close().    */
name|boolean
name|isClosed
init|=
literal|false
decl_stmt|;
comment|/**    *    */
specifier|public
name|HiveStatement
parameter_list|(
name|JdbcSessionState
name|session
parameter_list|,
name|HiveInterface
name|client
parameter_list|)
block|{
name|this
operator|.
name|session
operator|=
name|session
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#addBatch(java.lang.String)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#cancel()    */
specifier|public
name|void
name|cancel
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#clearBatch()    */
specifier|public
name|void
name|clearBatch
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#clearWarnings()    */
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
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#close()    */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO: how to properly shut down the client?
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
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#execute(java.lang.String)    */
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
name|ResultSet
name|rs
init|=
name|executeQuery
argument_list|(
name|sql
argument_list|)
decl_stmt|;
comment|// TODO: this should really check if there are results, but there's no easy
comment|// way to do that without calling rs.next();
return|return
name|rs
operator|!=
literal|null
return|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#execute(java.lang.String, int)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#execute(java.lang.String, int[])    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#executeBatch()    */
specifier|public
name|int
index|[]
name|executeBatch
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#executeQuery(java.lang.String)    */
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
name|resultSet
operator|=
literal|null
expr_stmt|;
name|client
operator|.
name|execute
argument_list|(
name|sql
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveServerException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getSQLState
argument_list|()
argument_list|,
name|e
operator|.
name|getErrorCode
argument_list|()
argument_list|)
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
argument_list|)
throw|;
block|}
name|resultSet
operator|=
operator|new
name|HiveResultSet
argument_list|(
name|client
argument_list|,
name|maxRows
argument_list|)
expr_stmt|;
return|return
name|resultSet
return|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#executeUpdate(java.lang.String)    */
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
try|try
block|{
name|client
operator|.
name|execute
argument_list|(
name|sql
argument_list|)
expr_stmt|;
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
argument_list|)
throw|;
block|}
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#executeUpdate(java.lang.String, int)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#executeUpdate(java.lang.String, int[])    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getConnection()    */
specifier|public
name|Connection
name|getConnection
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getFetchDirection()    */
specifier|public
name|int
name|getFetchDirection
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getFetchSize()    */
specifier|public
name|int
name|getFetchSize
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getGeneratedKeys()    */
specifier|public
name|ResultSet
name|getGeneratedKeys
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getMaxFieldSize()    */
specifier|public
name|int
name|getMaxFieldSize
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getMaxRows()    */
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
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getMoreResults()    */
specifier|public
name|boolean
name|getMoreResults
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getMoreResults(int)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getQueryTimeout()    */
specifier|public
name|int
name|getQueryTimeout
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getResultSet()    */
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
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getResultSetConcurrency()    */
specifier|public
name|int
name|getResultSetConcurrency
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getResultSetHoldability()    */
specifier|public
name|int
name|getResultSetHoldability
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getResultSetType()    */
specifier|public
name|int
name|getResultSetType
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getUpdateCount()    */
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
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getWarnings()    */
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
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#isClosed()    */
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
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#isPoolable()    */
specifier|public
name|boolean
name|isPoolable
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#setCursorName(java.lang.String)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#setEscapeProcessing(boolean)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#setFetchDirection(int)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#setFetchSize(int)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#setMaxFieldSize(int)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#setMaxRows(int)    */
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
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#setPoolable(boolean)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#setQueryTimeout(int)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Wrapper#unwrap(java.lang.Class)    */
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
comment|// TODO Auto-generated method stub
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

