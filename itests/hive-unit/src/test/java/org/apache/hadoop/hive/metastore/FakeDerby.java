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
name|metastore
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|Exception
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|Override
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|RuntimeException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|StackTraceElement
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
name|DriverManager
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|DriverPropertyInfo
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
name|SQLFeatureNotSupportedException
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|logging
operator|.
name|Logger
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
name|jdo
operator|.
name|JDOCanRetryException
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|derby
operator|.
name|jdbc
operator|.
name|EmbeddedDriver
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|ObjectStore
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
name|TestObjectStoreInitRetry
import|;
end_import

begin_comment
comment|/**  * Fake derby driver - companion class to enable testing by TestObjectStoreInitRetry  */
end_comment

begin_class
specifier|public
class|class
name|FakeDerby
extends|extends
name|org
operator|.
name|apache
operator|.
name|derby
operator|.
name|jdbc
operator|.
name|EmbeddedDriver
block|{
specifier|public
class|class
name|Connection
implements|implements
name|java
operator|.
name|sql
operator|.
name|Connection
block|{
specifier|private
name|java
operator|.
name|sql
operator|.
name|Connection
name|_baseConn
decl_stmt|;
specifier|public
name|Connection
parameter_list|(
name|java
operator|.
name|sql
operator|.
name|Connection
name|connection
parameter_list|)
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|this
operator|.
name|_baseConn
operator|=
name|connection
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Statement
name|createStatement
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|createStatement
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|prepareStatement
argument_list|(
name|sql
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|prepareCall
argument_list|(
name|sql
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|nativeSQL
argument_list|(
name|sql
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|TestObjectStoreInitRetry
operator|.
name|misbehave
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|setAutoCommit
argument_list|(
name|autoCommit
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getAutoCommit
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|getAutoCommit
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|commit
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|commit
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|rollback
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|rollback
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isClosed
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|isClosed
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|DatabaseMetaData
name|getMetaData
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|getMetaData
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|setReadOnly
argument_list|(
name|readOnly
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isReadOnly
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|isReadOnly
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|setCatalog
argument_list|(
name|catalog
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getCatalog
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|getCatalog
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|setTransactionIsolation
argument_list|(
name|level
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTransactionIsolation
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|getTransactionIsolation
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SQLWarning
name|getWarnings
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|getWarnings
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clearWarnings
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|clearWarnings
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|createStatement
argument_list|(
name|resultSetType
argument_list|,
name|resultSetConcurrency
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|prepareStatement
argument_list|(
name|sql
argument_list|,
name|resultSetType
argument_list|,
name|resultSetConcurrency
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|prepareCall
argument_list|(
name|sql
argument_list|,
name|resultSetType
argument_list|,
name|resultSetConcurrency
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|getTypeMap
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|setTypeMap
argument_list|(
name|map
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|setHoldability
argument_list|(
name|holdability
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getHoldability
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|getHoldability
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Savepoint
name|setSavepoint
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|setSavepoint
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|setSavepoint
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|rollback
argument_list|(
name|savepoint
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|releaseSavepoint
argument_list|(
name|savepoint
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|createStatement
argument_list|(
name|resultSetType
argument_list|,
name|resultSetConcurrency
argument_list|,
name|resultSetHoldability
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|prepareStatement
argument_list|(
name|sql
argument_list|,
name|resultSetType
argument_list|,
name|resultSetConcurrency
argument_list|,
name|resultSetHoldability
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|prepareCall
argument_list|(
name|sql
argument_list|,
name|resultSetType
argument_list|,
name|resultSetConcurrency
argument_list|,
name|resultSetHoldability
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|prepareStatement
argument_list|(
name|sql
argument_list|,
name|autoGeneratedKeys
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|prepareStatement
argument_list|(
name|sql
argument_list|,
name|columnIndexes
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|prepareStatement
argument_list|(
name|sql
argument_list|,
name|columnNames
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Clob
name|createClob
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|createClob
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Blob
name|createBlob
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|createBlob
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|NClob
name|createNClob
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|createNClob
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SQLXML
name|createSQLXML
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|createSQLXML
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|isValid
argument_list|(
name|timeout
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|setClientInfo
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|setClientInfo
argument_list|(
name|properties
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|getClientInfo
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Properties
name|getClientInfo
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|getClientInfo
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Array
name|createArrayOf
parameter_list|(
name|String
name|typeName
parameter_list|,
name|Object
index|[]
name|elements
parameter_list|)
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|createArrayOf
argument_list|(
name|typeName
argument_list|,
name|elements
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|createStruct
argument_list|(
name|typeName
argument_list|,
name|attributes
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setSchema
parameter_list|(
name|String
name|schema
parameter_list|)
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|setSchema
argument_list|(
name|schema
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getSchema
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|getSchema
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|Executor
name|executor
parameter_list|)
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|abort
argument_list|(
name|executor
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setNetworkTimeout
parameter_list|(
name|Executor
name|executor
parameter_list|,
name|int
name|milliseconds
parameter_list|)
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
name|_baseConn
operator|.
name|setNetworkTimeout
argument_list|(
name|executor
argument_list|,
name|milliseconds
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getNetworkTimeout
parameter_list|()
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|getNetworkTimeout
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|unwrap
argument_list|(
name|iface
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|TestObjectStoreInitRetry
operator|.
name|debugTrace
argument_list|()
expr_stmt|;
return|return
name|_baseConn
operator|.
name|isWrapperFor
argument_list|(
name|iface
argument_list|)
return|;
block|}
block|}
specifier|public
name|FakeDerby
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|boolean
name|acceptsURL
parameter_list|(
name|String
name|url
parameter_list|)
throws|throws
name|SQLException
block|{
name|url
operator|=
name|url
operator|.
name|replace
argument_list|(
literal|"fderby"
argument_list|,
literal|"derby"
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|acceptsURL
argument_list|(
name|url
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Connection
name|connect
parameter_list|(
name|java
operator|.
name|lang
operator|.
name|String
name|url
parameter_list|,
name|java
operator|.
name|util
operator|.
name|Properties
name|info
parameter_list|)
throws|throws
name|SQLException
block|{
name|TestObjectStoreInitRetry
operator|.
name|misbehave
argument_list|()
expr_stmt|;
name|url
operator|=
name|url
operator|.
name|replace
argument_list|(
literal|"fderby"
argument_list|,
literal|"derby"
argument_list|)
expr_stmt|;
return|return
operator|new
name|FakeDerby
operator|.
name|Connection
argument_list|(
name|super
operator|.
name|connect
argument_list|(
name|url
argument_list|,
name|info
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Logger
name|getParentLogger
parameter_list|()
throws|throws
name|SQLFeatureNotSupportedException
block|{
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|()
throw|;
comment|// hope this is respected properly
block|}
block|}
end_class

begin_empty_stmt
empty_stmt|;
end_empty_stmt

end_unit

