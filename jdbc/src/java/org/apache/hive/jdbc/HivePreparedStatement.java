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
name|Reader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
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
name|java
operator|.
name|sql
operator|.
name|Array
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Blob
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Clob
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|NClob
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|ParameterMetaData
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|PreparedStatement
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Ref
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
name|ResultSetMetaData
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|RowId
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
name|SQLFeatureNotSupportedException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLXML
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Time
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Types
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|MessageFormat
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
name|Calendar
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Scanner
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
name|rpc
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
name|rpc
operator|.
name|thrift
operator|.
name|TSessionHandle
import|;
end_import

begin_comment
comment|/**  * HivePreparedStatement.  *  */
end_comment

begin_class
specifier|public
class|class
name|HivePreparedStatement
extends|extends
name|HiveStatement
implements|implements
name|PreparedStatement
block|{
specifier|private
specifier|final
name|String
name|sql
decl_stmt|;
comment|/**    * save the SQL parameters {paramLoc:paramValue}    */
specifier|private
specifier|final
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|parameters
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|HivePreparedStatement
parameter_list|(
name|HiveConnection
name|connection
parameter_list|,
name|TCLIService
operator|.
name|Iface
name|client
parameter_list|,
name|TSessionHandle
name|sessHandle
parameter_list|,
name|String
name|sql
parameter_list|)
block|{
name|super
argument_list|(
name|connection
argument_list|,
name|client
argument_list|,
name|sessHandle
argument_list|)
expr_stmt|;
name|this
operator|.
name|sql
operator|=
name|sql
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#addBatch()    */
specifier|public
name|void
name|addBatch
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#clearParameters()    */
specifier|public
name|void
name|clearParameters
parameter_list|()
throws|throws
name|SQLException
block|{
name|this
operator|.
name|parameters
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/**    *  Invokes executeQuery(sql) using the sql provided to the constructor.    *    *  @return boolean Returns true if a resultSet is created, false if not.    *                  Note: If the result set is empty a true is returned.    *    *  @throws SQLException    */
specifier|public
name|boolean
name|execute
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
name|super
operator|.
name|execute
argument_list|(
name|updateSql
argument_list|(
name|sql
argument_list|,
name|parameters
argument_list|)
argument_list|)
return|;
block|}
comment|/**    *  Invokes executeQuery(sql) using the sql provided to the constructor.    *    *  @return ResultSet    *  @throws SQLException    */
specifier|public
name|ResultSet
name|executeQuery
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
name|super
operator|.
name|executeQuery
argument_list|(
name|updateSql
argument_list|(
name|sql
argument_list|,
name|parameters
argument_list|)
argument_list|)
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#executeUpdate()    */
specifier|public
name|int
name|executeUpdate
parameter_list|()
throws|throws
name|SQLException
block|{
name|super
operator|.
name|executeUpdate
argument_list|(
name|updateSql
argument_list|(
name|sql
argument_list|,
name|parameters
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
comment|/**    * update the SQL string with parameters set by setXXX methods of {@link PreparedStatement}    *    * @param sql    * @param parameters    * @return updated SQL string    * @throws SQLException     */
specifier|private
name|String
name|updateSql
parameter_list|(
specifier|final
name|String
name|sql
parameter_list|,
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|)
throws|throws
name|SQLException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|parts
init|=
name|splitSqlStatement
argument_list|(
name|sql
argument_list|)
decl_stmt|;
name|StringBuilder
name|newSql
init|=
operator|new
name|StringBuilder
argument_list|(
name|parts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|parts
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|parameters
operator|.
name|containsKey
argument_list|(
name|i
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Parameter #"
operator|+
name|i
operator|+
literal|" is unset"
argument_list|)
throw|;
block|}
name|newSql
operator|.
name|append
argument_list|(
name|parameters
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|newSql
operator|.
name|append
argument_list|(
name|parts
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|newSql
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Splits the parametered sql statement at parameter boundaries.    *     * taking into account ' and \ escaping.    *     * output for: 'select 1 from ? where a = ?'    *  ['select 1 from ',' where a = ','']    *     * @param sql    * @return    */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|splitSqlStatement
parameter_list|(
name|String
name|sql
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|parts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|apCount
init|=
literal|0
decl_stmt|;
name|int
name|off
init|=
literal|0
decl_stmt|;
name|boolean
name|skip
init|=
literal|false
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
name|sql
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|char
name|c
init|=
name|sql
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|skip
condition|)
block|{
name|skip
operator|=
literal|false
expr_stmt|;
continue|continue;
block|}
switch|switch
condition|(
name|c
condition|)
block|{
case|case
literal|'\''
case|:
name|apCount
operator|++
expr_stmt|;
break|break;
case|case
literal|'\\'
case|:
name|skip
operator|=
literal|true
expr_stmt|;
break|break;
case|case
literal|'?'
case|:
if|if
condition|(
operator|(
name|apCount
operator|&
literal|1
operator|)
operator|==
literal|0
condition|)
block|{
name|parts
operator|.
name|add
argument_list|(
name|sql
operator|.
name|substring
argument_list|(
name|off
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|off
operator|=
name|i
operator|+
literal|1
expr_stmt|;
block|}
break|break;
default|default:
break|break;
block|}
block|}
name|parts
operator|.
name|add
argument_list|(
name|sql
operator|.
name|substring
argument_list|(
name|off
argument_list|,
name|sql
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|parts
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#getMetaData()    */
specifier|public
name|ResultSetMetaData
name|getMetaData
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#getParameterMetaData()    */
specifier|public
name|ParameterMetaData
name|getParameterMetaData
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)    */
specifier|public
name|void
name|setArray
parameter_list|(
name|int
name|i
parameter_list|,
name|Array
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)    */
specifier|public
name|void
name|setAsciiStream
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|InputStream
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream,    * int)    */
specifier|public
name|void
name|setAsciiStream
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|InputStream
name|x
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream,    * long)    */
specifier|public
name|void
name|setAsciiStream
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|InputStream
name|x
parameter_list|,
name|long
name|length
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)    */
specifier|public
name|void
name|setBigDecimal
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|BigDecimal
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|parameterIndex
argument_list|,
name|x
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)    */
specifier|public
name|void
name|setBinaryStream
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|InputStream
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
name|String
name|str
init|=
operator|new
name|Scanner
argument_list|(
name|x
argument_list|,
literal|"UTF-8"
argument_list|)
operator|.
name|useDelimiter
argument_list|(
literal|"\\A"
argument_list|)
operator|.
name|next
argument_list|()
decl_stmt|;
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|parameterIndex
argument_list|,
name|str
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream,    * int)    */
specifier|public
name|void
name|setBinaryStream
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|InputStream
name|x
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream,    * long)    */
specifier|public
name|void
name|setBinaryStream
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|InputStream
name|x
parameter_list|,
name|long
name|length
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)    */
specifier|public
name|void
name|setBlob
parameter_list|(
name|int
name|i
parameter_list|,
name|Blob
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)    */
specifier|public
name|void
name|setBlob
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|InputStream
name|inputStream
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)    */
specifier|public
name|void
name|setBlob
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|InputStream
name|inputStream
parameter_list|,
name|long
name|length
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setBoolean(int, boolean)    */
specifier|public
name|void
name|setBoolean
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|boolean
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|parameterIndex
argument_list|,
literal|""
operator|+
name|x
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setByte(int, byte)    */
specifier|public
name|void
name|setByte
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|byte
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|parameterIndex
argument_list|,
literal|""
operator|+
name|x
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setBytes(int, byte[])    */
specifier|public
name|void
name|setBytes
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|byte
index|[]
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)    */
specifier|public
name|void
name|setCharacterStream
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Reader
name|reader
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader,    * int)    */
specifier|public
name|void
name|setCharacterStream
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Reader
name|reader
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader,    * long)    */
specifier|public
name|void
name|setCharacterStream
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Reader
name|reader
parameter_list|,
name|long
name|length
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)    */
specifier|public
name|void
name|setClob
parameter_list|(
name|int
name|i
parameter_list|,
name|Clob
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)    */
specifier|public
name|void
name|setClob
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Reader
name|reader
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)    */
specifier|public
name|void
name|setClob
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Reader
name|reader
parameter_list|,
name|long
name|length
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)    */
specifier|public
name|void
name|setDate
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Date
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|parameterIndex
argument_list|,
literal|"'"
operator|+
name|x
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setDate(int, java.sql.Date,    * java.util.Calendar)    */
specifier|public
name|void
name|setDate
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Date
name|x
parameter_list|,
name|Calendar
name|cal
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setDouble(int, double)    */
specifier|public
name|void
name|setDouble
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|double
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|parameterIndex
argument_list|,
literal|""
operator|+
name|x
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setFloat(int, float)    */
specifier|public
name|void
name|setFloat
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|float
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|parameterIndex
argument_list|,
literal|""
operator|+
name|x
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setInt(int, int)    */
specifier|public
name|void
name|setInt
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|int
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|parameterIndex
argument_list|,
literal|""
operator|+
name|x
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setLong(int, long)    */
specifier|public
name|void
name|setLong
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|long
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|parameterIndex
argument_list|,
literal|""
operator|+
name|x
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)    */
specifier|public
name|void
name|setNCharacterStream
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Reader
name|value
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader,    * long)    */
specifier|public
name|void
name|setNCharacterStream
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Reader
name|value
parameter_list|,
name|long
name|length
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)    */
specifier|public
name|void
name|setNClob
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|NClob
name|value
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)    */
specifier|public
name|void
name|setNClob
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Reader
name|reader
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)    */
specifier|public
name|void
name|setNClob
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Reader
name|reader
parameter_list|,
name|long
name|length
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setNString(int, java.lang.String)    */
specifier|public
name|void
name|setNString
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|String
name|value
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setNull(int, int)    */
specifier|public
name|void
name|setNull
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|int
name|sqlType
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|parameterIndex
argument_list|,
literal|"NULL"
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)    */
specifier|public
name|void
name|setNull
parameter_list|(
name|int
name|paramIndex
parameter_list|,
name|int
name|sqlType
parameter_list|,
name|String
name|typeName
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|paramIndex
argument_list|,
literal|"NULL"
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)    */
specifier|public
name|void
name|setObject
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Object
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
name|x
operator|==
literal|null
condition|)
block|{
name|setNull
argument_list|(
name|parameterIndex
argument_list|,
name|Types
operator|.
name|NULL
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|x
operator|instanceof
name|String
condition|)
block|{
name|setString
argument_list|(
name|parameterIndex
argument_list|,
operator|(
name|String
operator|)
name|x
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|x
operator|instanceof
name|Short
condition|)
block|{
name|setShort
argument_list|(
name|parameterIndex
argument_list|,
operator|(
operator|(
name|Short
operator|)
name|x
operator|)
operator|.
name|shortValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|x
operator|instanceof
name|Integer
condition|)
block|{
name|setInt
argument_list|(
name|parameterIndex
argument_list|,
operator|(
operator|(
name|Integer
operator|)
name|x
operator|)
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|x
operator|instanceof
name|Long
condition|)
block|{
name|setLong
argument_list|(
name|parameterIndex
argument_list|,
operator|(
operator|(
name|Long
operator|)
name|x
operator|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|x
operator|instanceof
name|Float
condition|)
block|{
name|setFloat
argument_list|(
name|parameterIndex
argument_list|,
operator|(
operator|(
name|Float
operator|)
name|x
operator|)
operator|.
name|floatValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|x
operator|instanceof
name|Double
condition|)
block|{
name|setDouble
argument_list|(
name|parameterIndex
argument_list|,
operator|(
operator|(
name|Double
operator|)
name|x
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|x
operator|instanceof
name|Boolean
condition|)
block|{
name|setBoolean
argument_list|(
name|parameterIndex
argument_list|,
operator|(
operator|(
name|Boolean
operator|)
name|x
operator|)
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|x
operator|instanceof
name|Byte
condition|)
block|{
name|setByte
argument_list|(
name|parameterIndex
argument_list|,
operator|(
operator|(
name|Byte
operator|)
name|x
operator|)
operator|.
name|byteValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|x
operator|instanceof
name|Character
condition|)
block|{
name|setString
argument_list|(
name|parameterIndex
argument_list|,
name|x
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|x
operator|instanceof
name|Timestamp
condition|)
block|{
name|setTimestamp
argument_list|(
name|parameterIndex
argument_list|,
operator|(
name|Timestamp
operator|)
name|x
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|x
operator|instanceof
name|BigDecimal
condition|)
block|{
name|setString
argument_list|(
name|parameterIndex
argument_list|,
name|x
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Can't infer a type.
throw|throw
operator|new
name|SQLException
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"Can''t infer the SQL type to use for an instance of {0}. Use setObject() with an explicit Types value to specify the type to use."
argument_list|,
name|x
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)    */
specifier|public
name|void
name|setObject
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Object
name|x
parameter_list|,
name|int
name|targetSqlType
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)    */
specifier|public
name|void
name|setObject
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Object
name|x
parameter_list|,
name|int
name|targetSqlType
parameter_list|,
name|int
name|scale
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)    */
specifier|public
name|void
name|setRef
parameter_list|(
name|int
name|i
parameter_list|,
name|Ref
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)    */
specifier|public
name|void
name|setRowId
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|RowId
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)    */
specifier|public
name|void
name|setSQLXML
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|SQLXML
name|xmlObject
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setShort(int, short)    */
specifier|public
name|void
name|setShort
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|short
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|parameterIndex
argument_list|,
literal|""
operator|+
name|x
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setString(int, java.lang.String)    */
specifier|public
name|void
name|setString
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|String
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
name|x
operator|=
name|x
operator|.
name|replace
argument_list|(
literal|"'"
argument_list|,
literal|"\\'"
argument_list|)
expr_stmt|;
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|parameterIndex
argument_list|,
literal|"'"
operator|+
name|x
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)    */
specifier|public
name|void
name|setTime
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Time
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setTime(int, java.sql.Time,    * java.util.Calendar)    */
specifier|public
name|void
name|setTime
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Time
name|x
parameter_list|,
name|Calendar
name|cal
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)    */
specifier|public
name|void
name|setTimestamp
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Timestamp
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|parameters
operator|.
name|put
argument_list|(
name|parameterIndex
argument_list|,
literal|"'"
operator|+
name|x
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp,    * java.util.Calendar)    */
specifier|public
name|void
name|setTimestamp
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|Timestamp
name|x
parameter_list|,
name|Calendar
name|cal
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setURL(int, java.net.URL)    */
specifier|public
name|void
name|setURL
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|URL
name|x
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.PreparedStatement#setUnicodeStream(int, java.io.InputStream,    * int)    */
specifier|public
name|void
name|setUnicodeStream
parameter_list|(
name|int
name|parameterIndex
parameter_list|,
name|InputStream
name|x
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

