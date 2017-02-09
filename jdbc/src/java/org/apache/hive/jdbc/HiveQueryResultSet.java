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
import|import static
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
name|TCLIServiceConstants
operator|.
name|TYPE_NAMES
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
name|Statement
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
name|Iterator
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
name|concurrent
operator|.
name|locks
operator|.
name|ReentrantLock
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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|RowSetFactory
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
name|TCLIServiceConstants
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
name|rpc
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
name|rpc
operator|.
name|thrift
operator|.
name|TColumnDesc
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
name|TFetchOrientation
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
name|TFetchResultsReq
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
name|TFetchResultsResp
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
name|TGetResultSetMetadataReq
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
name|TGetResultSetMetadataResp
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
name|rpc
operator|.
name|thrift
operator|.
name|TPrimitiveTypeEntry
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
name|rpc
operator|.
name|thrift
operator|.
name|TRowSet
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
name|TTableSchema
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
name|TTypeQualifierValue
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
name|TTypeQualifiers
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

begin_comment
comment|/**  * HiveQueryResultSet.  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveQueryResultSet
extends|extends
name|HiveBaseResultSet
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HiveQueryResultSet
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TCLIService
operator|.
name|Iface
name|client
decl_stmt|;
specifier|private
name|TOperationHandle
name|stmtHandle
decl_stmt|;
specifier|private
name|TSessionHandle
name|sessHandle
decl_stmt|;
specifier|private
name|int
name|maxRows
decl_stmt|;
specifier|private
name|int
name|fetchSize
decl_stmt|;
specifier|private
name|int
name|rowsFetched
init|=
literal|0
decl_stmt|;
specifier|private
name|RowSet
name|fetchedRows
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|Object
index|[]
argument_list|>
name|fetchedRowsItr
decl_stmt|;
specifier|private
name|boolean
name|isClosed
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|emptyResultSet
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|isScrollable
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|fetchFirst
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|TProtocolVersion
name|protocol
decl_stmt|;
specifier|public
specifier|static
class|class
name|Builder
block|{
specifier|private
specifier|final
name|Connection
name|connection
decl_stmt|;
specifier|private
specifier|final
name|Statement
name|statement
decl_stmt|;
specifier|private
name|TCLIService
operator|.
name|Iface
name|client
init|=
literal|null
decl_stmt|;
specifier|private
name|TOperationHandle
name|stmtHandle
init|=
literal|null
decl_stmt|;
specifier|private
name|TSessionHandle
name|sessHandle
init|=
literal|null
decl_stmt|;
comment|/**      * Sets the limit for the maximum number of rows that any ResultSet object produced by this      * Statement can contain to the given number. If the limit is exceeded, the excess rows      * are silently dropped. The value must be>= 0, and 0 means there is not limit.      */
specifier|private
name|int
name|maxRows
init|=
literal|0
decl_stmt|;
specifier|private
name|boolean
name|retrieveSchema
init|=
literal|true
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|colNames
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|colTypes
decl_stmt|;
specifier|private
name|List
argument_list|<
name|JdbcColumnAttributes
argument_list|>
name|colAttributes
decl_stmt|;
specifier|private
name|int
name|fetchSize
init|=
literal|50
decl_stmt|;
specifier|private
name|boolean
name|emptyResultSet
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|isScrollable
init|=
literal|false
decl_stmt|;
specifier|private
name|ReentrantLock
name|transportLock
init|=
literal|null
decl_stmt|;
specifier|public
name|Builder
parameter_list|(
name|Statement
name|statement
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|statement
operator|=
name|statement
expr_stmt|;
name|this
operator|.
name|connection
operator|=
name|statement
operator|.
name|getConnection
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Builder
parameter_list|(
name|Connection
name|connection
parameter_list|)
block|{
name|this
operator|.
name|statement
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
block|}
specifier|public
name|Builder
name|setClient
parameter_list|(
name|TCLIService
operator|.
name|Iface
name|client
parameter_list|)
block|{
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setStmtHandle
parameter_list|(
name|TOperationHandle
name|stmtHandle
parameter_list|)
block|{
name|this
operator|.
name|stmtHandle
operator|=
name|stmtHandle
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setSessionHandle
parameter_list|(
name|TSessionHandle
name|sessHandle
parameter_list|)
block|{
name|this
operator|.
name|sessHandle
operator|=
name|sessHandle
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setMaxRows
parameter_list|(
name|int
name|maxRows
parameter_list|)
block|{
name|this
operator|.
name|maxRows
operator|=
name|maxRows
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setSchema
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colTypes
parameter_list|)
block|{
comment|// no column attributes provided - create list of null attributes.
name|List
argument_list|<
name|JdbcColumnAttributes
argument_list|>
name|colAttributes
init|=
operator|new
name|ArrayList
argument_list|<
name|JdbcColumnAttributes
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|colTypes
operator|.
name|size
argument_list|()
condition|;
operator|++
name|idx
control|)
block|{
name|colAttributes
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
name|setSchema
argument_list|(
name|colNames
argument_list|,
name|colTypes
argument_list|,
name|colAttributes
argument_list|)
return|;
block|}
specifier|public
name|Builder
name|setSchema
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colTypes
parameter_list|,
name|List
argument_list|<
name|JdbcColumnAttributes
argument_list|>
name|colAttributes
parameter_list|)
block|{
name|this
operator|.
name|colNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|colNames
operator|.
name|addAll
argument_list|(
name|colNames
argument_list|)
expr_stmt|;
name|this
operator|.
name|colTypes
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|colTypes
operator|.
name|addAll
argument_list|(
name|colTypes
argument_list|)
expr_stmt|;
name|this
operator|.
name|colAttributes
operator|=
operator|new
name|ArrayList
argument_list|<
name|JdbcColumnAttributes
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|colAttributes
operator|.
name|addAll
argument_list|(
name|colAttributes
argument_list|)
expr_stmt|;
name|this
operator|.
name|retrieveSchema
operator|=
literal|false
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setFetchSize
parameter_list|(
name|int
name|fetchSize
parameter_list|)
block|{
name|this
operator|.
name|fetchSize
operator|=
name|fetchSize
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setEmptyResultSet
parameter_list|(
name|boolean
name|emptyResultSet
parameter_list|)
block|{
name|this
operator|.
name|emptyResultSet
operator|=
name|emptyResultSet
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setScrollable
parameter_list|(
name|boolean
name|setScrollable
parameter_list|)
block|{
name|this
operator|.
name|isScrollable
operator|=
name|setScrollable
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setTransportLock
parameter_list|(
name|ReentrantLock
name|transportLock
parameter_list|)
block|{
name|this
operator|.
name|transportLock
operator|=
name|transportLock
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|HiveQueryResultSet
name|build
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
operator|new
name|HiveQueryResultSet
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|public
name|TProtocolVersion
name|getProtocolVersion
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
operator|(
operator|(
name|HiveConnection
operator|)
name|connection
operator|)
operator|.
name|getProtocol
argument_list|()
return|;
block|}
block|}
specifier|protected
name|HiveQueryResultSet
parameter_list|(
name|Builder
name|builder
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|statement
operator|=
name|builder
operator|.
name|statement
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|builder
operator|.
name|client
expr_stmt|;
name|this
operator|.
name|stmtHandle
operator|=
name|builder
operator|.
name|stmtHandle
expr_stmt|;
name|this
operator|.
name|sessHandle
operator|=
name|builder
operator|.
name|sessHandle
expr_stmt|;
name|this
operator|.
name|fetchSize
operator|=
name|builder
operator|.
name|fetchSize
expr_stmt|;
name|columnNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|normalizedColumnNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|columnTypes
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|columnAttributes
operator|=
operator|new
name|ArrayList
argument_list|<
name|JdbcColumnAttributes
argument_list|>
argument_list|()
expr_stmt|;
if|if
condition|(
name|builder
operator|.
name|retrieveSchema
condition|)
block|{
name|retrieveSchema
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|setSchema
argument_list|(
name|builder
operator|.
name|colNames
argument_list|,
name|builder
operator|.
name|colTypes
argument_list|,
name|builder
operator|.
name|colAttributes
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|emptyResultSet
operator|=
name|builder
operator|.
name|emptyResultSet
expr_stmt|;
if|if
condition|(
name|builder
operator|.
name|emptyResultSet
condition|)
block|{
name|this
operator|.
name|maxRows
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|maxRows
operator|=
name|builder
operator|.
name|maxRows
expr_stmt|;
block|}
name|this
operator|.
name|isScrollable
operator|=
name|builder
operator|.
name|isScrollable
expr_stmt|;
name|this
operator|.
name|protocol
operator|=
name|builder
operator|.
name|getProtocolVersion
argument_list|()
expr_stmt|;
block|}
comment|/**    * Generate ColumnAttributes object from a TTypeQualifiers    * @param primitiveTypeEntry primitive type    * @return generated ColumnAttributes, or null    */
specifier|private
specifier|static
name|JdbcColumnAttributes
name|getColumnAttributes
parameter_list|(
name|TPrimitiveTypeEntry
name|primitiveTypeEntry
parameter_list|)
block|{
name|JdbcColumnAttributes
name|ret
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|primitiveTypeEntry
operator|.
name|isSetTypeQualifiers
argument_list|()
condition|)
block|{
name|TTypeQualifiers
name|tq
init|=
name|primitiveTypeEntry
operator|.
name|getTypeQualifiers
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|primitiveTypeEntry
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|CHAR_TYPE
case|:
case|case
name|VARCHAR_TYPE
case|:
name|TTypeQualifierValue
name|val
init|=
name|tq
operator|.
name|getQualifiers
argument_list|()
operator|.
name|get
argument_list|(
name|TCLIServiceConstants
operator|.
name|CHARACTER_MAXIMUM_LENGTH
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|!=
literal|null
condition|)
block|{
comment|// precision is char length
name|ret
operator|=
operator|new
name|JdbcColumnAttributes
argument_list|(
name|val
operator|.
name|getI32Value
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|DECIMAL_TYPE
case|:
name|TTypeQualifierValue
name|prec
init|=
name|tq
operator|.
name|getQualifiers
argument_list|()
operator|.
name|get
argument_list|(
name|TCLIServiceConstants
operator|.
name|PRECISION
argument_list|)
decl_stmt|;
name|TTypeQualifierValue
name|scale
init|=
name|tq
operator|.
name|getQualifiers
argument_list|()
operator|.
name|get
argument_list|(
name|TCLIServiceConstants
operator|.
name|SCALE
argument_list|)
decl_stmt|;
name|ret
operator|=
operator|new
name|JdbcColumnAttributes
argument_list|(
name|prec
operator|==
literal|null
condition|?
name|HiveDecimal
operator|.
name|USER_DEFAULT_PRECISION
else|:
name|prec
operator|.
name|getI32Value
argument_list|()
argument_list|,
name|scale
operator|==
literal|null
condition|?
name|HiveDecimal
operator|.
name|USER_DEFAULT_SCALE
else|:
name|scale
operator|.
name|getI32Value
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
break|break;
block|}
block|}
return|return
name|ret
return|;
block|}
comment|/**    * Retrieve schema from the server    */
specifier|private
name|void
name|retrieveSchema
parameter_list|()
throws|throws
name|SQLException
block|{
try|try
block|{
name|TGetResultSetMetadataReq
name|metadataReq
init|=
operator|new
name|TGetResultSetMetadataReq
argument_list|(
name|stmtHandle
argument_list|)
decl_stmt|;
comment|// TODO need session handle
name|TGetResultSetMetadataResp
name|metadataResp
decl_stmt|;
name|metadataResp
operator|=
name|client
operator|.
name|GetResultSetMetadata
argument_list|(
name|metadataReq
argument_list|)
expr_stmt|;
name|Utils
operator|.
name|verifySuccess
argument_list|(
name|metadataResp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|StringBuilder
name|namesSb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|StringBuilder
name|typesSb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|TTableSchema
name|schema
init|=
name|metadataResp
operator|.
name|getSchema
argument_list|()
decl_stmt|;
if|if
condition|(
name|schema
operator|==
literal|null
operator|||
operator|!
name|schema
operator|.
name|isSetColumns
argument_list|()
condition|)
block|{
comment|// TODO: should probably throw an exception here.
return|return;
block|}
name|setSchema
argument_list|(
operator|new
name|TableSchema
argument_list|(
name|schema
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TColumnDesc
argument_list|>
name|columns
init|=
name|schema
operator|.
name|getColumns
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|schema
operator|.
name|getColumnsSize
argument_list|()
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
name|pos
operator|!=
literal|0
condition|)
block|{
name|namesSb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|typesSb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|String
name|columnName
init|=
name|columns
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getColumnName
argument_list|()
decl_stmt|;
name|columnNames
operator|.
name|add
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
name|normalizedColumnNames
operator|.
name|add
argument_list|(
name|columnName
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|TPrimitiveTypeEntry
name|primitiveTypeEntry
init|=
name|columns
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getTypeDesc
argument_list|()
operator|.
name|getTypes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPrimitiveEntry
argument_list|()
decl_stmt|;
name|String
name|columnTypeName
init|=
name|TYPE_NAMES
operator|.
name|get
argument_list|(
name|primitiveTypeEntry
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
name|columnTypes
operator|.
name|add
argument_list|(
name|columnTypeName
argument_list|)
expr_stmt|;
name|columnAttributes
operator|.
name|add
argument_list|(
name|getColumnAttributes
argument_list|(
name|primitiveTypeEntry
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
comment|// rethrow the SQLException as is
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Could not create ResultSet: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
comment|/**    * Set the specified schema to the resultset    * @param colNames    * @param colTypes    */
specifier|private
name|void
name|setSchema
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colTypes
parameter_list|,
name|List
argument_list|<
name|JdbcColumnAttributes
argument_list|>
name|colAttributes
parameter_list|)
block|{
name|columnNames
operator|.
name|addAll
argument_list|(
name|colNames
argument_list|)
expr_stmt|;
name|columnTypes
operator|.
name|addAll
argument_list|(
name|colTypes
argument_list|)
expr_stmt|;
name|columnAttributes
operator|.
name|addAll
argument_list|(
name|colAttributes
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|colName
range|:
name|colNames
control|)
block|{
name|normalizedColumnNames
operator|.
name|add
argument_list|(
name|colName
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|this
operator|.
name|statement
operator|!=
literal|null
operator|&&
operator|(
name|this
operator|.
name|statement
operator|instanceof
name|HiveStatement
operator|)
condition|)
block|{
name|HiveStatement
name|s
init|=
operator|(
name|HiveStatement
operator|)
name|this
operator|.
name|statement
decl_stmt|;
name|s
operator|.
name|closeClientOperation
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// for those stmtHandle passed from HiveDatabaseMetaData instead of Statement
name|closeOperationHandle
argument_list|(
name|stmtHandle
argument_list|)
expr_stmt|;
block|}
comment|// Need reset during re-open when needed
name|client
operator|=
literal|null
expr_stmt|;
name|stmtHandle
operator|=
literal|null
expr_stmt|;
name|sessHandle
operator|=
literal|null
expr_stmt|;
name|isClosed
operator|=
literal|true
expr_stmt|;
block|}
specifier|private
name|void
name|closeOperationHandle
parameter_list|(
name|TOperationHandle
name|stmtHandle
parameter_list|)
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
argument_list|(
name|stmtHandle
argument_list|)
decl_stmt|;
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
block|}
comment|/**    * Moves the cursor down one row from its current position.    *    * @see java.sql.ResultSet#next()    * @throws SQLException    *           if a database access error occurs.    */
specifier|public
name|boolean
name|next
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
literal|"Resultset is closed"
argument_list|)
throw|;
block|}
if|if
condition|(
name|emptyResultSet
operator|||
operator|(
name|maxRows
operator|>
literal|0
operator|&&
name|rowsFetched
operator|>=
name|maxRows
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|/**      * Poll on the operation status, till the operation is complete.      * We need to wait only for HiveStatement to complete.      * HiveDatabaseMetaData which also uses this ResultSet returns only after the RPC is complete.      */
if|if
condition|(
operator|(
name|statement
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|statement
operator|instanceof
name|HiveStatement
operator|)
condition|)
block|{
operator|(
operator|(
name|HiveStatement
operator|)
name|statement
operator|)
operator|.
name|waitForOperationToComplete
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|TFetchOrientation
name|orientation
init|=
name|TFetchOrientation
operator|.
name|FETCH_NEXT
decl_stmt|;
if|if
condition|(
name|fetchFirst
condition|)
block|{
comment|// If we are asked to start from begining, clear the current fetched resultset
name|orientation
operator|=
name|TFetchOrientation
operator|.
name|FETCH_FIRST
expr_stmt|;
name|fetchedRows
operator|=
literal|null
expr_stmt|;
name|fetchedRowsItr
operator|=
literal|null
expr_stmt|;
name|fetchFirst
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|fetchedRows
operator|==
literal|null
operator|||
operator|!
name|fetchedRowsItr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|TFetchResultsReq
name|fetchReq
init|=
operator|new
name|TFetchResultsReq
argument_list|(
name|stmtHandle
argument_list|,
name|orientation
argument_list|,
name|fetchSize
argument_list|)
decl_stmt|;
name|TFetchResultsResp
name|fetchResp
decl_stmt|;
name|fetchResp
operator|=
name|client
operator|.
name|FetchResults
argument_list|(
name|fetchReq
argument_list|)
expr_stmt|;
name|Utils
operator|.
name|verifySuccessWithInfo
argument_list|(
name|fetchResp
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|TRowSet
name|results
init|=
name|fetchResp
operator|.
name|getResults
argument_list|()
decl_stmt|;
name|fetchedRows
operator|=
name|RowSetFactory
operator|.
name|create
argument_list|(
name|results
argument_list|,
name|protocol
argument_list|)
expr_stmt|;
name|fetchedRowsItr
operator|=
name|fetchedRows
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|fetchedRowsItr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|row
operator|=
name|fetchedRowsItr
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
name|rowsFetched
operator|++
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
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Error retrieving next row"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
comment|// NOTE: fetchOne doesn't throw new SQLFeatureNotSupportedException("Method not supported").
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|ResultSetMetaData
name|getMetaData
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
literal|"Resultset is closed"
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|getMetaData
argument_list|()
return|;
block|}
annotation|@
name|Override
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
if|if
condition|(
name|isClosed
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Resultset is closed"
argument_list|)
throw|;
block|}
name|fetchSize
operator|=
name|rows
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getType
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
literal|"Resultset is closed"
argument_list|)
throw|;
block|}
if|if
condition|(
name|isScrollable
condition|)
block|{
return|return
name|ResultSet
operator|.
name|TYPE_SCROLL_INSENSITIVE
return|;
block|}
else|else
block|{
return|return
name|ResultSet
operator|.
name|TYPE_FORWARD_ONLY
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getFetchSize
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
literal|"Resultset is closed"
argument_list|)
throw|;
block|}
return|return
name|fetchSize
return|;
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|getObject
parameter_list|(
name|String
name|columnLabel
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
comment|//JDK 1.7
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|getObject
parameter_list|(
name|int
name|columnIndex
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
comment|//JDK 1.7
throw|throw
operator|new
name|SQLFeatureNotSupportedException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
comment|/**    * Moves the cursor before the first row of the resultset.    *    * @see java.sql.ResultSet#next()    * @throws SQLException    *           if a database access error occurs.    */
annotation|@
name|Override
specifier|public
name|void
name|beforeFirst
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
literal|"Resultset is closed"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|isScrollable
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported for TYPE_FORWARD_ONLY resultset"
argument_list|)
throw|;
block|}
name|fetchFirst
operator|=
literal|true
expr_stmt|;
name|rowsFetched
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isBeforeFirst
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
literal|"Resultset is closed"
argument_list|)
throw|;
block|}
return|return
operator|(
name|rowsFetched
operator|==
literal|0
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRow
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
name|rowsFetched
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isClosed
parameter_list|()
block|{
return|return
name|isClosed
return|;
block|}
block|}
end_class

end_unit

