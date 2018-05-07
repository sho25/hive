begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|storage
operator|.
name|jdbc
operator|.
name|dao
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
name|dbcp
operator|.
name|BasicDataSourceFactory
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
name|serde
operator|.
name|serdeConstants
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
name|security
operator|.
name|Credentials
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
name|security
operator|.
name|UserGroupInformation
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
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|storage
operator|.
name|jdbc
operator|.
name|conf
operator|.
name|JdbcStorageConfig
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
name|storage
operator|.
name|jdbc
operator|.
name|conf
operator|.
name|JdbcStorageConfigManager
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
name|storage
operator|.
name|jdbc
operator|.
name|exception
operator|.
name|HiveJdbcDatabaseAccessException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|sql
operator|.
name|DataSource
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
name|PreparedStatement
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

begin_comment
comment|/**  * A data accessor that should in theory work with all JDBC compliant database drivers.  */
end_comment

begin_class
specifier|public
class|class
name|GenericJdbcDatabaseAccessor
implements|implements
name|DatabaseAccessor
block|{
specifier|protected
specifier|static
specifier|final
name|String
name|DBCP_CONFIG_PREFIX
init|=
name|JdbcStorageConfigManager
operator|.
name|CONFIG_PREFIX
operator|+
literal|".dbcp"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|DEFAULT_FETCH_SIZE
init|=
literal|1000
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|GenericJdbcDatabaseAccessor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|DataSource
name|dbcpDataSource
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Text
name|DBCP_PWD
init|=
operator|new
name|Text
argument_list|(
name|DBCP_CONFIG_PREFIX
operator|+
literal|".password"
argument_list|)
decl_stmt|;
specifier|public
name|GenericJdbcDatabaseAccessor
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getColumnNames
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveJdbcDatabaseAccessException
block|{
name|Connection
name|conn
init|=
literal|null
decl_stmt|;
name|PreparedStatement
name|ps
init|=
literal|null
decl_stmt|;
name|ResultSet
name|rs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|initializeDatabaseConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|String
name|metadataQuery
init|=
name|getMetaDataQuery
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|LOGGER
operator|.
name|debug
argument_list|(
literal|"Query to execute is [{}]"
argument_list|,
name|metadataQuery
argument_list|)
expr_stmt|;
name|conn
operator|=
name|dbcpDataSource
operator|.
name|getConnection
argument_list|()
expr_stmt|;
name|ps
operator|=
name|conn
operator|.
name|prepareStatement
argument_list|(
name|metadataQuery
argument_list|)
expr_stmt|;
name|rs
operator|=
name|ps
operator|.
name|executeQuery
argument_list|()
expr_stmt|;
name|ResultSetMetaData
name|metadata
init|=
name|rs
operator|.
name|getMetaData
argument_list|()
decl_stmt|;
name|int
name|numColumns
init|=
name|metadata
operator|.
name|getColumnCount
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|numColumns
argument_list|)
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
name|numColumns
condition|;
name|i
operator|++
control|)
block|{
name|columnNames
operator|.
name|add
argument_list|(
name|metadata
operator|.
name|getColumnName
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|columnNames
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
literal|"Error while trying to get column names."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveJdbcDatabaseAccessException
argument_list|(
literal|"Error while trying to get column names: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|cleanupResources
argument_list|(
name|conn
argument_list|,
name|ps
argument_list|,
name|rs
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|String
name|getMetaDataQuery
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|sql
init|=
name|JdbcStorageConfigManager
operator|.
name|getQueryToExecute
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|String
name|metadataQuery
init|=
name|addLimitToQuery
argument_list|(
name|sql
argument_list|,
literal|1
argument_list|)
decl_stmt|;
return|return
name|metadataQuery
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTotalNumberOfRecords
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveJdbcDatabaseAccessException
block|{
name|Connection
name|conn
init|=
literal|null
decl_stmt|;
name|PreparedStatement
name|ps
init|=
literal|null
decl_stmt|;
name|ResultSet
name|rs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|initializeDatabaseConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|String
name|sql
init|=
name|JdbcStorageConfigManager
operator|.
name|getQueryToExecute
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|String
name|countQuery
init|=
literal|"SELECT COUNT(*) FROM ("
operator|+
name|sql
operator|+
literal|") tmptable"
decl_stmt|;
name|LOGGER
operator|.
name|info
argument_list|(
literal|"Query to execute is [{}]"
argument_list|,
name|countQuery
argument_list|)
expr_stmt|;
name|conn
operator|=
name|dbcpDataSource
operator|.
name|getConnection
argument_list|()
expr_stmt|;
name|ps
operator|=
name|conn
operator|.
name|prepareStatement
argument_list|(
name|countQuery
argument_list|)
expr_stmt|;
name|rs
operator|=
name|ps
operator|.
name|executeQuery
argument_list|()
expr_stmt|;
if|if
condition|(
name|rs
operator|.
name|next
argument_list|()
condition|)
block|{
return|return
name|rs
operator|.
name|getInt
argument_list|(
literal|1
argument_list|)
return|;
block|}
else|else
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"The count query did not return any results."
argument_list|,
name|countQuery
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveJdbcDatabaseAccessException
argument_list|(
literal|"Count query did not return any results."
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveJdbcDatabaseAccessException
name|he
parameter_list|)
block|{
throw|throw
name|he
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
literal|"Caught exception while trying to get the number of records"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveJdbcDatabaseAccessException
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|cleanupResources
argument_list|(
name|conn
argument_list|,
name|ps
argument_list|,
name|rs
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|JdbcRecordIterator
name|getRecordIterator
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|limit
parameter_list|,
name|int
name|offset
parameter_list|)
throws|throws
name|HiveJdbcDatabaseAccessException
block|{
name|Connection
name|conn
init|=
literal|null
decl_stmt|;
name|PreparedStatement
name|ps
init|=
literal|null
decl_stmt|;
name|ResultSet
name|rs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|initializeDatabaseConnection
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|String
name|sql
init|=
name|JdbcStorageConfigManager
operator|.
name|getQueryToExecute
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|String
name|limitQuery
init|=
name|addLimitAndOffsetToQuery
argument_list|(
name|sql
argument_list|,
name|limit
argument_list|,
name|offset
argument_list|)
decl_stmt|;
name|LOGGER
operator|.
name|info
argument_list|(
literal|"Query to execute is [{}]"
argument_list|,
name|limitQuery
argument_list|)
expr_stmt|;
name|conn
operator|=
name|dbcpDataSource
operator|.
name|getConnection
argument_list|()
expr_stmt|;
name|ps
operator|=
name|conn
operator|.
name|prepareStatement
argument_list|(
name|limitQuery
argument_list|,
name|ResultSet
operator|.
name|TYPE_FORWARD_ONLY
argument_list|,
name|ResultSet
operator|.
name|CONCUR_READ_ONLY
argument_list|)
expr_stmt|;
name|ps
operator|.
name|setFetchSize
argument_list|(
name|getFetchSize
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|rs
operator|=
name|ps
operator|.
name|executeQuery
argument_list|()
expr_stmt|;
return|return
operator|new
name|JdbcRecordIterator
argument_list|(
name|conn
argument_list|,
name|ps
argument_list|,
name|rs
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
literal|"Caught exception while trying to execute query"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|cleanupResources
argument_list|(
name|conn
argument_list|,
name|ps
argument_list|,
name|rs
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveJdbcDatabaseAccessException
argument_list|(
literal|"Caught exception while trying to execute query"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Uses generic JDBC escape functions to add a limit and offset clause to a query string    *    * @param sql    * @param limit    * @param offset    * @return    */
specifier|protected
name|String
name|addLimitAndOffsetToQuery
parameter_list|(
name|String
name|sql
parameter_list|,
name|int
name|limit
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
if|if
condition|(
name|offset
operator|==
literal|0
condition|)
block|{
return|return
name|addLimitToQuery
argument_list|(
name|sql
argument_list|,
name|limit
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|sql
operator|+
literal|" {LIMIT "
operator|+
name|limit
operator|+
literal|" OFFSET "
operator|+
name|offset
operator|+
literal|"}"
return|;
block|}
block|}
comment|/*    * Uses generic JDBC escape functions to add a limit clause to a query string    */
specifier|protected
name|String
name|addLimitToQuery
parameter_list|(
name|String
name|sql
parameter_list|,
name|int
name|limit
parameter_list|)
block|{
return|return
name|sql
operator|+
literal|" {LIMIT "
operator|+
name|limit
operator|+
literal|"}"
return|;
block|}
specifier|protected
name|void
name|cleanupResources
parameter_list|(
name|Connection
name|conn
parameter_list|,
name|PreparedStatement
name|ps
parameter_list|,
name|ResultSet
name|rs
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|rs
operator|!=
literal|null
condition|)
block|{
name|rs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Caught exception during resultset cleanup."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|ps
operator|!=
literal|null
condition|)
block|{
name|ps
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Caught exception during statement cleanup."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|conn
operator|!=
literal|null
condition|)
block|{
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Caught exception during connection cleanup."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|initializeDatabaseConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|dbcpDataSource
operator|==
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|dbcpDataSource
operator|==
literal|null
condition|)
block|{
name|Properties
name|props
init|=
name|getConnectionPoolProperties
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|dbcpDataSource
operator|=
name|BasicDataSourceFactory
operator|.
name|createDataSource
argument_list|(
name|props
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|protected
name|Properties
name|getConnectionPoolProperties
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Create the default properties object
name|Properties
name|dbProperties
init|=
name|getDefaultDBCPProperties
argument_list|()
decl_stmt|;
comment|// override with user defined properties
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|userProperties
init|=
name|conf
operator|.
name|getValByRegex
argument_list|(
name|DBCP_CONFIG_PREFIX
operator|+
literal|"\\.*"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|userProperties
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|userProperties
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|userProperties
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|dbProperties
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|replaceFirst
argument_list|(
name|DBCP_CONFIG_PREFIX
operator|+
literal|"\\."
argument_list|,
literal|""
argument_list|)
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// handle password
name|Credentials
name|credentials
init|=
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
operator|.
name|getCredentials
argument_list|()
decl_stmt|;
if|if
condition|(
name|credentials
operator|.
name|getSecretKey
argument_list|(
name|DBCP_PWD
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|LOGGER
operator|.
name|info
argument_list|(
literal|"found token in credentials"
argument_list|)
expr_stmt|;
name|dbProperties
operator|.
name|put
argument_list|(
name|DBCP_PWD
argument_list|,
operator|new
name|String
argument_list|(
name|credentials
operator|.
name|getSecretKey
argument_list|(
name|DBCP_PWD
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// essential properties that shouldn't be overridden by users
name|dbProperties
operator|.
name|put
argument_list|(
literal|"url"
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|JdbcStorageConfig
operator|.
name|JDBC_URL
operator|.
name|getPropertyName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|dbProperties
operator|.
name|put
argument_list|(
literal|"driverClassName"
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|JdbcStorageConfig
operator|.
name|JDBC_DRIVER_CLASS
operator|.
name|getPropertyName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|dbProperties
operator|.
name|put
argument_list|(
literal|"type"
argument_list|,
literal|"javax.sql.DataSource"
argument_list|)
expr_stmt|;
return|return
name|dbProperties
return|;
block|}
specifier|protected
name|Properties
name|getDefaultDBCPProperties
parameter_list|()
block|{
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|props
operator|.
name|put
argument_list|(
literal|"initialSize"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|props
operator|.
name|put
argument_list|(
literal|"maxActive"
argument_list|,
literal|"3"
argument_list|)
expr_stmt|;
name|props
operator|.
name|put
argument_list|(
literal|"maxIdle"
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
name|props
operator|.
name|put
argument_list|(
literal|"maxWait"
argument_list|,
literal|"10000"
argument_list|)
expr_stmt|;
name|props
operator|.
name|put
argument_list|(
literal|"timeBetweenEvictionRunsMillis"
argument_list|,
literal|"30000"
argument_list|)
expr_stmt|;
return|return
name|props
return|;
block|}
specifier|protected
name|int
name|getFetchSize
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getInt
argument_list|(
name|JdbcStorageConfig
operator|.
name|JDBC_FETCH_SIZE
operator|.
name|getPropertyName
argument_list|()
argument_list|,
name|DEFAULT_FETCH_SIZE
argument_list|)
return|;
block|}
block|}
end_class

end_unit

