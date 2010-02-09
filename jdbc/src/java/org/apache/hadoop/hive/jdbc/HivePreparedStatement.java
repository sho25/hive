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
name|Connection
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
name|SQLWarning
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
name|util
operator|.
name|Calendar
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

begin_comment
comment|/**  * HivePreparedStatement.  *  */
end_comment

begin_class
specifier|public
class|class
name|HivePreparedStatement
implements|implements
name|PreparedStatement
block|{
name|String
name|sql
decl_stmt|;
name|JdbcSessionState
name|session
decl_stmt|;
name|HiveInterface
name|client
decl_stmt|;
comment|/**    *    */
specifier|public
name|HivePreparedStatement
parameter_list|(
name|JdbcSessionState
name|session
parameter_list|,
name|HiveInterface
name|client
parameter_list|,
name|String
name|sql
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
name|this
operator|.
name|sql
operator|=
name|sql
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#addBatch()    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#clearParameters()    */
specifier|public
name|void
name|clearParameters
parameter_list|()
throws|throws
name|SQLException
block|{
comment|// TODO Auto-generated method stub
comment|// throw new SQLException("Method not supported");
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#execute()    */
specifier|public
name|boolean
name|execute
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
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#executeQuery()    */
specifier|public
name|ResultSet
name|executeQuery
parameter_list|()
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
return|return
operator|new
name|HiveResultSet
argument_list|(
name|client
argument_list|)
return|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#executeUpdate()    */
specifier|public
name|int
name|executeUpdate
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
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#getMetaData()    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#getParameterMetaData()    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream,    * int)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream,    * long)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream,    * int)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream,    * long)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setBoolean(int, boolean)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setByte(int, byte)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setBytes(int, byte[])    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader,    * int)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader,    * long)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setDate(int, java.sql.Date,    * java.util.Calendar)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setDouble(int, double)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setFloat(int, float)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setInt(int, int)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setLong(int, long)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader,    * long)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setNString(int, java.lang.String)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setNull(int, int)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setShort(int, short)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setString(int, java.lang.String)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setTime(int, java.sql.Time,    * java.util.Calendar)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)    */
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp,    * java.util.Calendar)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setURL(int, java.net.URL)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.PreparedStatement#setUnicodeStream(int, java.io.InputStream,    * int)    */
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
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#close()    */
specifier|public
name|void
name|close
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
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
comment|// TODO Auto-generated method stub
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
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
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#getWarnings()    */
specifier|public
name|SQLWarning
name|getWarnings
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
comment|/*    * (non-Javadoc)    *     * @see java.sql.Statement#isClosed()    */
specifier|public
name|boolean
name|isClosed
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
comment|// TODO Auto-generated method stub
comment|// throw new SQLException("Method not supported");
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
comment|// throw new SQLException("Method not supported");
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

