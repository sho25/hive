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
name|CallableStatement
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
name|DatabaseMetaData
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
name|PreparedStatement
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLClientInfoException
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
name|Savepoint
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Statement
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Struct
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
name|Map
operator|.
name|Entry
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
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|SaslException
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
name|hive
operator|.
name|service
operator|.
name|auth
operator|.
name|KerberosSaslHelper
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
name|auth
operator|.
name|PlainSaslHelper
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
name|EmbeddedThriftCLIService
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
name|TCloseSessionReq
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
name|TOpenSessionReq
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
name|TOpenSessionResp
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
name|TProtocolVersion
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TSocket
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportException
import|;
end_import

begin_comment
comment|/**  * HiveConnection.  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveConnection
implements|implements
name|java
operator|.
name|sql
operator|.
name|Connection
block|{
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_AUTH_TYPE
init|=
literal|"auth"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_AUTH_SIMPLE
init|=
literal|"noSasl"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_AUTH_USER
init|=
literal|"user"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_AUTH_PRINCIPAL
init|=
literal|"principal"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_AUTH_PASSWD
init|=
literal|"password"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_ANONYMOUS_USER
init|=
literal|"anonymous"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_ANONYMOUS_PASSWD
init|=
literal|"anonymous"
decl_stmt|;
specifier|private
name|TTransport
name|transport
decl_stmt|;
specifier|private
name|TCLIService
operator|.
name|Iface
name|client
decl_stmt|;
specifier|private
name|boolean
name|isClosed
init|=
literal|true
decl_stmt|;
specifier|private
name|SQLWarning
name|warningChain
init|=
literal|null
decl_stmt|;
specifier|private
name|TSessionHandle
name|sessHandle
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|TProtocolVersion
argument_list|>
name|supportedProtocols
init|=
operator|new
name|LinkedList
argument_list|<
name|TProtocolVersion
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * TODO: - parse uri (use java.net.URI?).    */
specifier|public
name|HiveConnection
parameter_list|(
name|String
name|uri
parameter_list|,
name|Properties
name|info
parameter_list|)
throws|throws
name|SQLException
block|{
name|Utils
operator|.
name|JdbcConnectionParams
name|connParams
init|=
name|Utils
operator|.
name|parseURL
argument_list|(
name|uri
argument_list|)
decl_stmt|;
if|if
condition|(
name|connParams
operator|.
name|isEmbeddedMode
argument_list|()
condition|)
block|{
name|client
operator|=
operator|new
name|EmbeddedThriftCLIService
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// extract user/password from JDBC connection properties if its not supplied in the connection URL
if|if
condition|(
name|info
operator|.
name|containsKey
argument_list|(
name|HIVE_AUTH_USER
argument_list|)
condition|)
block|{
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|put
argument_list|(
name|HIVE_AUTH_USER
argument_list|,
name|info
operator|.
name|getProperty
argument_list|(
name|HIVE_AUTH_USER
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|info
operator|.
name|containsKey
argument_list|(
name|HIVE_AUTH_PASSWD
argument_list|)
condition|)
block|{
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|put
argument_list|(
name|HIVE_AUTH_PASSWD
argument_list|,
name|info
operator|.
name|getProperty
argument_list|(
name|HIVE_AUTH_PASSWD
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|openTransport
argument_list|(
name|uri
argument_list|,
name|connParams
operator|.
name|getHost
argument_list|()
argument_list|,
name|connParams
operator|.
name|getPort
argument_list|()
argument_list|,
name|connParams
operator|.
name|getSessionVars
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// currently only V1 is supported
name|supportedProtocols
operator|.
name|add
argument_list|(
name|TProtocolVersion
operator|.
name|HIVE_CLI_SERVICE_PROTOCOL_V1
argument_list|)
expr_stmt|;
comment|// open client session
name|openSession
argument_list|(
name|uri
argument_list|)
expr_stmt|;
name|configureConnection
argument_list|(
name|connParams
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|configureConnection
parameter_list|(
name|Utils
operator|.
name|JdbcConnectionParams
name|connParams
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// set the hive variable in session state for local mode
if|if
condition|(
name|connParams
operator|.
name|isEmbeddedMode
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|connParams
operator|.
name|getHiveVars
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|setHiveVariables
argument_list|(
name|connParams
operator|.
name|getHiveVars
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// for remote JDBC client, try to set the conf var using 'set foo=bar'
name|Statement
name|stmt
init|=
name|createStatement
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveConf
range|:
name|connParams
operator|.
name|getHiveConfs
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|stmt
operator|.
name|execute
argument_list|(
literal|"set "
operator|+
name|hiveConf
operator|.
name|getKey
argument_list|()
operator|+
literal|"="
operator|+
name|hiveConf
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|openTransport
parameter_list|(
name|String
name|uri
parameter_list|,
name|String
name|host
parameter_list|,
name|int
name|port
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sessConf
parameter_list|)
throws|throws
name|SQLException
block|{
name|transport
operator|=
operator|new
name|TSocket
argument_list|(
name|host
argument_list|,
name|port
argument_list|)
expr_stmt|;
comment|// handle secure connection if specified
if|if
condition|(
operator|!
name|sessConf
operator|.
name|containsKey
argument_list|(
name|HIVE_AUTH_TYPE
argument_list|)
operator|||
operator|!
name|sessConf
operator|.
name|get
argument_list|(
name|HIVE_AUTH_TYPE
argument_list|)
operator|.
name|equals
argument_list|(
name|HIVE_AUTH_SIMPLE
argument_list|)
condition|)
block|{
try|try
block|{
if|if
condition|(
name|sessConf
operator|.
name|containsKey
argument_list|(
name|HIVE_AUTH_PRINCIPAL
argument_list|)
condition|)
block|{
name|transport
operator|=
name|KerberosSaslHelper
operator|.
name|getKerberosTransport
argument_list|(
name|sessConf
operator|.
name|get
argument_list|(
name|HIVE_AUTH_PRINCIPAL
argument_list|)
argument_list|,
name|host
argument_list|,
name|transport
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|userName
init|=
name|sessConf
operator|.
name|get
argument_list|(
name|HIVE_AUTH_USER
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|userName
operator|==
literal|null
operator|)
operator|||
name|userName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|userName
operator|=
name|HIVE_ANONYMOUS_USER
expr_stmt|;
block|}
name|String
name|passwd
init|=
name|sessConf
operator|.
name|get
argument_list|(
name|HIVE_AUTH_PASSWD
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|passwd
operator|==
literal|null
operator|)
operator|||
name|passwd
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|passwd
operator|=
name|HIVE_ANONYMOUS_PASSWD
expr_stmt|;
block|}
name|transport
operator|=
name|PlainSaslHelper
operator|.
name|getPlainTransport
argument_list|(
name|userName
argument_list|,
name|passwd
argument_list|,
name|transport
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SaslException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Could not establish secure connection to "
operator|+
name|uri
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|" 08S01"
argument_list|)
throw|;
block|}
block|}
name|TProtocol
name|protocol
init|=
operator|new
name|TBinaryProtocol
argument_list|(
name|transport
argument_list|)
decl_stmt|;
name|client
operator|=
operator|new
name|TCLIService
operator|.
name|Client
argument_list|(
name|protocol
argument_list|)
expr_stmt|;
try|try
block|{
name|transport
operator|.
name|open
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TTransportException
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
name|SQLException
argument_list|(
literal|"Could not establish connection to "
operator|+
name|uri
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|" 08S01"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|openSession
parameter_list|(
name|String
name|uri
parameter_list|)
throws|throws
name|SQLException
block|{
name|TOpenSessionReq
name|openReq
init|=
operator|new
name|TOpenSessionReq
argument_list|()
decl_stmt|;
comment|// set the session configuration
comment|// openReq.setConfiguration(null);
try|try
block|{
name|TOpenSessionResp
name|openResp
init|=
name|client
operator|.
name|OpenSession
argument_list|(
name|openReq
argument_list|)
decl_stmt|;
comment|// validate connection
name|Utils
operator|.
name|verifySuccess
argument_list|(
name|openResp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|supportedProtocols
operator|.
name|contains
argument_list|(
name|openResp
operator|.
name|getServerProtocolVersion
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|TException
argument_list|(
literal|"Unsupported Hive2 protocol"
argument_list|)
throw|;
block|}
name|sessHandle
operator|=
name|openResp
operator|.
name|getSessionHandle
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Could not establish connection to "
operator|+
name|uri
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|" 08S01"
argument_list|)
throw|;
block|}
name|isClosed
operator|=
literal|false
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#clearWarnings()    */
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#close()    */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|SQLException
block|{
if|if
condition|(
operator|!
name|isClosed
condition|)
block|{
name|TCloseSessionReq
name|closeReq
init|=
operator|new
name|TCloseSessionReq
argument_list|(
name|sessHandle
argument_list|)
decl_stmt|;
try|try
block|{
name|client
operator|.
name|CloseSession
argument_list|(
name|closeReq
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Error while cleaning up the server resources"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|isClosed
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|transport
operator|!=
literal|null
condition|)
block|{
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#commit()    */
specifier|public
name|void
name|commit
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#createArrayOf(java.lang.String,    * java.lang.Object[])    */
specifier|public
name|Array
name|createArrayOf
parameter_list|(
name|String
name|arg0
parameter_list|,
name|Object
index|[]
name|arg1
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#createBlob()    */
specifier|public
name|Blob
name|createBlob
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#createClob()    */
specifier|public
name|Clob
name|createClob
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#createNClob()    */
specifier|public
name|NClob
name|createNClob
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#createSQLXML()    */
specifier|public
name|SQLXML
name|createSQLXML
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
comment|/**    * Creates a Statement object for sending SQL statements to the database.    *    * @throws SQLException    *           if a database access error occurs.    * @see java.sql.Connection#createStatement()    */
specifier|public
name|Statement
name|createStatement
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
literal|"Can't create Statement, connection is closed"
argument_list|)
throw|;
block|}
return|return
operator|new
name|HiveStatement
argument_list|(
name|client
argument_list|,
name|sessHandle
argument_list|)
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#createStatement(int, int)    */
specifier|public
name|Statement
name|createStatement
parameter_list|(
name|int
name|resultSetType
parameter_list|,
name|int
name|resultSetConcurrency
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#createStatement(int, int, int)    */
specifier|public
name|Statement
name|createStatement
parameter_list|(
name|int
name|resultSetType
parameter_list|,
name|int
name|resultSetConcurrency
parameter_list|,
name|int
name|resultSetHoldability
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#createStruct(java.lang.String, java.lang.Object[])    */
specifier|public
name|Struct
name|createStruct
parameter_list|(
name|String
name|typeName
parameter_list|,
name|Object
index|[]
name|attributes
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#getAutoCommit()    */
specifier|public
name|boolean
name|getAutoCommit
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
literal|true
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#getCatalog()    */
specifier|public
name|String
name|getCatalog
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
literal|""
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#getClientInfo()    */
specifier|public
name|Properties
name|getClientInfo
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#getClientInfo(java.lang.String)    */
specifier|public
name|String
name|getClientInfo
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#getHoldability()    */
specifier|public
name|int
name|getHoldability
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#getMetaData()    */
specifier|public
name|DatabaseMetaData
name|getMetaData
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
operator|new
name|HiveDatabaseMetaData
argument_list|(
name|client
argument_list|,
name|sessHandle
argument_list|)
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#getTransactionIsolation()    */
specifier|public
name|int
name|getTransactionIsolation
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
name|Connection
operator|.
name|TRANSACTION_NONE
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#getTypeMap()    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|getTypeMap
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#getWarnings()    */
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#isClosed()    */
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#isReadOnly()    */
specifier|public
name|boolean
name|isReadOnly
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
literal|false
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#isValid(int)    */
specifier|public
name|boolean
name|isValid
parameter_list|(
name|int
name|timeout
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#nativeSQL(java.lang.String)    */
specifier|public
name|String
name|nativeSQL
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#prepareCall(java.lang.String)    */
specifier|public
name|CallableStatement
name|prepareCall
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#prepareCall(java.lang.String, int, int)    */
specifier|public
name|CallableStatement
name|prepareCall
parameter_list|(
name|String
name|sql
parameter_list|,
name|int
name|resultSetType
parameter_list|,
name|int
name|resultSetConcurrency
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)    */
specifier|public
name|CallableStatement
name|prepareCall
parameter_list|(
name|String
name|sql
parameter_list|,
name|int
name|resultSetType
parameter_list|,
name|int
name|resultSetConcurrency
parameter_list|,
name|int
name|resultSetHoldability
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#prepareStatement(java.lang.String)    */
specifier|public
name|PreparedStatement
name|prepareStatement
parameter_list|(
name|String
name|sql
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
operator|new
name|HivePreparedStatement
argument_list|(
name|client
argument_list|,
name|sessHandle
argument_list|,
name|sql
argument_list|)
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#prepareStatement(java.lang.String, int)    */
specifier|public
name|PreparedStatement
name|prepareStatement
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
return|return
operator|new
name|HivePreparedStatement
argument_list|(
name|client
argument_list|,
name|sessHandle
argument_list|,
name|sql
argument_list|)
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#prepareStatement(java.lang.String, int[])    */
specifier|public
name|PreparedStatement
name|prepareStatement
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#prepareStatement(java.lang.String,    * java.lang.String[])    */
specifier|public
name|PreparedStatement
name|prepareStatement
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)    */
specifier|public
name|PreparedStatement
name|prepareStatement
parameter_list|(
name|String
name|sql
parameter_list|,
name|int
name|resultSetType
parameter_list|,
name|int
name|resultSetConcurrency
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
operator|new
name|HivePreparedStatement
argument_list|(
name|client
argument_list|,
name|sessHandle
argument_list|,
name|sql
argument_list|)
return|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)    */
specifier|public
name|PreparedStatement
name|prepareStatement
parameter_list|(
name|String
name|sql
parameter_list|,
name|int
name|resultSetType
parameter_list|,
name|int
name|resultSetConcurrency
parameter_list|,
name|int
name|resultSetHoldability
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)    */
specifier|public
name|void
name|releaseSavepoint
parameter_list|(
name|Savepoint
name|savepoint
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#rollback()    */
specifier|public
name|void
name|rollback
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#rollback(java.sql.Savepoint)    */
specifier|public
name|void
name|rollback
parameter_list|(
name|Savepoint
name|savepoint
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#setAutoCommit(boolean)    */
specifier|public
name|void
name|setAutoCommit
parameter_list|(
name|boolean
name|autoCommit
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
name|autoCommit
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"enabling autocommit is not supported"
argument_list|)
throw|;
block|}
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#setCatalog(java.lang.String)    */
specifier|public
name|void
name|setCatalog
parameter_list|(
name|String
name|catalog
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#setClientInfo(java.util.Properties)    */
specifier|public
name|void
name|setClientInfo
parameter_list|(
name|Properties
name|properties
parameter_list|)
throws|throws
name|SQLClientInfoException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLClientInfoException
argument_list|(
literal|"Method not supported"
argument_list|,
literal|null
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#setClientInfo(java.lang.String, java.lang.String)    */
specifier|public
name|void
name|setClientInfo
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
throws|throws
name|SQLClientInfoException
block|{
comment|// TODO Auto-generated method stub
throw|throw
operator|new
name|SQLClientInfoException
argument_list|(
literal|"Method not supported"
argument_list|,
literal|null
argument_list|)
throw|;
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#setHoldability(int)    */
specifier|public
name|void
name|setHoldability
parameter_list|(
name|int
name|holdability
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#setReadOnly(boolean)    */
specifier|public
name|void
name|setReadOnly
parameter_list|(
name|boolean
name|readOnly
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#setSavepoint()    */
specifier|public
name|Savepoint
name|setSavepoint
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#setSavepoint(java.lang.String)    */
specifier|public
name|Savepoint
name|setSavepoint
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
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#setTransactionIsolation(int)    */
specifier|public
name|void
name|setTransactionIsolation
parameter_list|(
name|int
name|level
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// TODO: throw an exception?
block|}
comment|/*    * (non-Javadoc)    *    * @see java.sql.Connection#setTypeMap(java.util.Map)    */
specifier|public
name|void
name|setTypeMap
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|map
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
comment|// TODO Auto-generated method stub
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

