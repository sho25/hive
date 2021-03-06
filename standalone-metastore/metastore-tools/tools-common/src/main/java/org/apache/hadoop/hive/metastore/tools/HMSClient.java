begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|tools
package|;
end_package

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
name|metastore
operator|.
name|api
operator|.
name|Database
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
name|DropPartitionsRequest
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
name|DropPartitionsResult
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
name|MetaException
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
name|Partition
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
name|RequestPartsSpec
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
name|Table
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
name|ThriftHiveMetastore
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
name|conf
operator|.
name|MetastoreConf
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
name|security
operator|.
name|HadoopThriftAuthBridge
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
name|utils
operator|.
name|MetaStoreUtils
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
name|utils
operator|.
name|SecurityUtils
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
name|TCompactProtocol
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
name|TFramedTransport
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
name|jetbrains
operator|.
name|annotations
operator|.
name|NotNull
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jetbrains
operator|.
name|annotations
operator|.
name|Nullable
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
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|MalformedURLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
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
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_comment
comment|/**  *  Wrapper for Thrift HMS interface.  */
end_comment

begin_class
specifier|final
class|class
name|HMSClient
implements|implements
name|AutoCloseable
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
name|HMSClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|METASTORE_URI
init|=
literal|"hive.metastore.uris"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CONFIG_DIR
init|=
literal|"/etc/hive/conf"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_SITE
init|=
literal|"hive-site.xml"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CORE_SITE
init|=
literal|"core-site.xml"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PRINCIPAL_KEY
init|=
literal|"hive.metastore.kerberos.principal"
decl_stmt|;
specifier|private
specifier|final
name|String
name|confDir
decl_stmt|;
specifier|private
name|ThriftHiveMetastore
operator|.
name|Iface
name|client
decl_stmt|;
specifier|private
name|TTransport
name|transport
decl_stmt|;
specifier|private
name|URI
name|serverURI
decl_stmt|;
specifier|public
name|URI
name|getServerURI
parameter_list|()
block|{
return|return
name|serverURI
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|serverURI
operator|.
name|toString
argument_list|()
return|;
block|}
name|HMSClient
parameter_list|(
annotation|@
name|Nullable
name|URI
name|uri
parameter_list|)
throws|throws
name|TException
throws|,
name|IOException
throws|,
name|InterruptedException
throws|,
name|LoginException
throws|,
name|URISyntaxException
block|{
name|this
argument_list|(
name|uri
argument_list|,
name|CONFIG_DIR
argument_list|)
expr_stmt|;
block|}
name|HMSClient
parameter_list|(
annotation|@
name|Nullable
name|URI
name|uri
parameter_list|,
annotation|@
name|Nullable
name|String
name|confDir
parameter_list|)
throws|throws
name|TException
throws|,
name|IOException
throws|,
name|InterruptedException
throws|,
name|LoginException
throws|,
name|URISyntaxException
block|{
name|this
operator|.
name|confDir
operator|=
name|confDir
operator|==
literal|null
condition|?
name|CONFIG_DIR
else|:
name|confDir
expr_stmt|;
name|getClient
argument_list|(
name|uri
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addResource
parameter_list|(
name|Configuration
name|conf
parameter_list|,
annotation|@
name|NotNull
name|String
name|r
parameter_list|)
throws|throws
name|MalformedURLException
block|{
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|confDir
operator|+
literal|"/"
operator|+
name|r
argument_list|)
decl_stmt|;
if|if
condition|(
name|f
operator|.
name|exists
argument_list|()
operator|&&
operator|!
name|f
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding configuration resource {}"
argument_list|,
name|r
argument_list|)
expr_stmt|;
name|conf
operator|.
name|addResource
argument_list|(
name|f
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Configuration {} does not exist"
argument_list|,
name|r
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Create a client to Hive Metastore.    * If principal is specified, create kerberised client.    *    * @param uri server uri    * @throws MetaException        if fails to login using kerberos credentials    * @throws IOException          if fails connecting to metastore    * @throws InterruptedException if interrupted during kerberos setup    */
specifier|private
name|void
name|getClient
parameter_list|(
annotation|@
name|Nullable
name|URI
name|uri
parameter_list|)
throws|throws
name|TException
throws|,
name|IOException
throws|,
name|InterruptedException
throws|,
name|URISyntaxException
throws|,
name|LoginException
block|{
name|Configuration
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|addResource
argument_list|(
name|conf
argument_list|,
name|HIVE_SITE
argument_list|)
expr_stmt|;
if|if
condition|(
name|uri
operator|!=
literal|null
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|METASTORE_URI
argument_list|,
name|uri
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Pick up the first URI from the list of available URIs
name|serverURI
operator|=
name|uri
operator|!=
literal|null
condition|?
name|uri
else|:
operator|new
name|URI
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|METASTORE_URI
argument_list|)
operator|.
name|split
argument_list|(
literal|","
argument_list|)
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|String
name|principal
init|=
name|conf
operator|.
name|get
argument_list|(
name|PRINCIPAL_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|principal
operator|==
literal|null
condition|)
block|{
name|open
argument_list|(
name|conf
argument_list|,
name|serverURI
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Opening kerberos connection to HMS"
argument_list|)
expr_stmt|;
name|addResource
argument_list|(
name|conf
argument_list|,
name|CORE_SITE
argument_list|)
expr_stmt|;
name|Configuration
name|hadoopConf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|addResource
argument_list|(
name|hadoopConf
argument_list|,
name|HIVE_SITE
argument_list|)
expr_stmt|;
name|addResource
argument_list|(
name|hadoopConf
argument_list|,
name|CORE_SITE
argument_list|)
expr_stmt|;
comment|// Kerberos magic
name|UserGroupInformation
operator|.
name|setConfiguration
argument_list|(
name|hadoopConf
argument_list|)
expr_stmt|;
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
operator|.
name|doAs
argument_list|(
call|(
name|PrivilegedExceptionAction
argument_list|<
name|TTransport
argument_list|>
call|)
argument_list|()
operator|->
name|open
argument_list|(
name|conf
argument_list|,
name|serverURI
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|boolean
name|dbExists
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|getAllDatabases
argument_list|(
name|dbName
argument_list|)
operator|.
name|contains
argument_list|(
name|dbName
argument_list|)
return|;
block|}
name|boolean
name|tableExists
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|getAllTables
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
operator|.
name|contains
argument_list|(
name|tableName
argument_list|)
return|;
block|}
name|Database
name|getDatabase
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|get_database
argument_list|(
name|dbName
argument_list|)
return|;
block|}
comment|/**    * Return all databases with name matching the filter.    *    * @param filter Regexp. Can be null or empty in which case everything matches    * @return list of database names matching the filter    * @throws MetaException    */
name|Set
argument_list|<
name|String
argument_list|>
name|getAllDatabases
parameter_list|(
annotation|@
name|Nullable
name|String
name|filter
parameter_list|)
throws|throws
name|TException
block|{
if|if
condition|(
name|filter
operator|==
literal|null
operator|||
name|filter
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|client
operator|.
name|get_all_databases
argument_list|()
argument_list|)
return|;
block|}
return|return
name|client
operator|.
name|get_all_databases
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|n
lambda|->
name|n
operator|.
name|matches
argument_list|(
name|filter
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
return|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|getAllTables
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|Nullable
name|String
name|filter
parameter_list|)
throws|throws
name|TException
block|{
if|if
condition|(
name|filter
operator|==
literal|null
operator|||
name|filter
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|client
operator|.
name|get_all_tables
argument_list|(
name|dbName
argument_list|)
argument_list|)
return|;
block|}
return|return
name|client
operator|.
name|get_all_tables
argument_list|(
name|dbName
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|n
lambda|->
name|n
operator|.
name|matches
argument_list|(
name|filter
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Create database with the given name if it doesn't exist    *    * @param name database name    */
name|boolean
name|createDatabase
parameter_list|(
annotation|@
name|NotNull
name|String
name|name
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|createDatabase
argument_list|(
name|name
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Create database if it doesn't exist    *    * @param name        Database name    * @param description Database description    * @param location    Database location    * @param params      Database params    * @throws TException if database exists    */
name|boolean
name|createDatabase
parameter_list|(
annotation|@
name|NotNull
name|String
name|name
parameter_list|,
annotation|@
name|Nullable
name|String
name|description
parameter_list|,
annotation|@
name|Nullable
name|String
name|location
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
throws|throws
name|TException
block|{
name|Database
name|db
init|=
operator|new
name|Database
argument_list|(
name|name
argument_list|,
name|description
argument_list|,
name|location
argument_list|,
name|params
argument_list|)
decl_stmt|;
name|client
operator|.
name|create_database
argument_list|(
name|db
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|boolean
name|createDatabase
parameter_list|(
name|Database
name|db
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|create_database
argument_list|(
name|db
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|boolean
name|dropDatabase
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|drop_database
argument_list|(
name|dbName
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|boolean
name|createTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|create_table
argument_list|(
name|table
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|boolean
name|dropTable
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|drop_table
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|Table
name|getTable
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|get_table
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
return|;
block|}
name|Partition
name|createPartition
parameter_list|(
annotation|@
name|NotNull
name|Table
name|table
parameter_list|,
annotation|@
name|NotNull
name|List
argument_list|<
name|String
argument_list|>
name|values
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|add_partition
argument_list|(
operator|new
name|Util
operator|.
name|PartitionBuilder
argument_list|(
name|table
argument_list|)
operator|.
name|withValues
argument_list|(
name|values
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
name|Partition
name|addPartition
parameter_list|(
annotation|@
name|NotNull
name|Partition
name|partition
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|add_partition
argument_list|(
name|partition
argument_list|)
return|;
block|}
name|void
name|addPartitions
parameter_list|(
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|add_partitions
argument_list|(
name|partitions
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Partition
argument_list|>
name|listPartitions
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|get_partitions
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
return|;
block|}
name|Long
name|getCurrentNotificationId
parameter_list|()
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|get_current_notificationEventId
argument_list|()
operator|.
name|getEventId
argument_list|()
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|getPartitionNames
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|get_partition_names
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|dropPartition
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|,
annotation|@
name|NotNull
name|List
argument_list|<
name|String
argument_list|>
name|arguments
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|drop_partition
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|arguments
argument_list|,
literal|true
argument_list|)
return|;
block|}
name|List
argument_list|<
name|Partition
argument_list|>
name|getPartitions
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|)
throws|throws
name|TException
block|{
return|return
name|client
operator|.
name|get_partitions
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
return|;
block|}
name|DropPartitionsResult
name|dropPartitions
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|,
annotation|@
name|Nullable
name|List
argument_list|<
name|String
argument_list|>
name|partNames
parameter_list|)
throws|throws
name|TException
block|{
if|if
condition|(
name|partNames
operator|==
literal|null
condition|)
block|{
return|return
name|dropPartitions
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|getPartitionNames
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|partNames
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|client
operator|.
name|drop_partitions_req
argument_list|(
operator|new
name|DropPartitionsRequest
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|RequestPartsSpec
operator|.
name|names
argument_list|(
name|partNames
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
name|List
argument_list|<
name|Partition
argument_list|>
name|getPartitionsByNames
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|,
annotation|@
name|Nullable
name|List
argument_list|<
name|String
argument_list|>
name|names
parameter_list|)
throws|throws
name|TException
block|{
if|if
condition|(
name|names
operator|==
literal|null
condition|)
block|{
return|return
name|client
operator|.
name|get_partitions_by_names
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|getPartitionNames
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
argument_list|)
return|;
block|}
return|return
name|client
operator|.
name|get_partitions_by_names
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|names
argument_list|)
return|;
block|}
name|boolean
name|alterTable
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|,
annotation|@
name|NotNull
name|Table
name|newTable
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|alter_table
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|newTable
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|void
name|alterPartition
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|,
annotation|@
name|NotNull
name|Partition
name|partition
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|alter_partition
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partition
argument_list|)
expr_stmt|;
block|}
name|void
name|alterPartitions
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|,
annotation|@
name|NotNull
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|alter_partitions
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partitions
argument_list|)
expr_stmt|;
block|}
name|void
name|appendPartition
parameter_list|(
annotation|@
name|NotNull
name|String
name|dbName
parameter_list|,
annotation|@
name|NotNull
name|String
name|tableName
parameter_list|,
annotation|@
name|NotNull
name|List
argument_list|<
name|String
argument_list|>
name|partitionValues
parameter_list|)
throws|throws
name|TException
block|{
name|client
operator|.
name|append_partition_with_environment_context
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partitionValues
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|TTransport
name|open
parameter_list|(
name|Configuration
name|conf
parameter_list|,
annotation|@
name|NotNull
name|URI
name|uri
parameter_list|)
throws|throws
name|TException
throws|,
name|IOException
throws|,
name|LoginException
block|{
name|boolean
name|useSSL
init|=
name|MetastoreConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|USE_SSL
argument_list|)
decl_stmt|;
name|boolean
name|useSasl
init|=
name|MetastoreConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|USE_THRIFT_SASL
argument_list|)
decl_stmt|;
name|boolean
name|useFramedTransport
init|=
name|MetastoreConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|USE_THRIFT_FRAMED_TRANSPORT
argument_list|)
decl_stmt|;
name|boolean
name|useCompactProtocol
init|=
name|MetastoreConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|USE_THRIFT_COMPACT_PROTOCOL
argument_list|)
decl_stmt|;
name|int
name|clientSocketTimeout
init|=
operator|(
name|int
operator|)
name|MetastoreConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CLIENT_SOCKET_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Connecting to {}, framedTransport = {}"
argument_list|,
name|uri
argument_list|,
name|useFramedTransport
argument_list|)
expr_stmt|;
name|String
name|host
init|=
name|uri
operator|.
name|getHost
argument_list|()
decl_stmt|;
name|int
name|port
init|=
name|uri
operator|.
name|getPort
argument_list|()
decl_stmt|;
comment|// Sasl/SSL code is copied from HiveMetastoreCLient
if|if
condition|(
operator|!
name|useSSL
condition|)
block|{
name|transport
operator|=
operator|new
name|TSocket
argument_list|(
name|host
argument_list|,
name|port
argument_list|,
name|clientSocketTimeout
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|trustStorePath
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|SSL_TRUSTSTORE_PATH
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|trustStorePath
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|SSL_TRUSTSTORE_PATH
operator|.
name|toString
argument_list|()
operator|+
literal|" Not configured for SSL connection"
argument_list|)
throw|;
block|}
name|String
name|trustStorePassword
init|=
name|MetastoreConf
operator|.
name|getPassword
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|SSL_TRUSTSTORE_PASSWORD
argument_list|)
decl_stmt|;
comment|// Create an SSL socket and connect
name|transport
operator|=
name|SecurityUtils
operator|.
name|getSSLSocket
argument_list|(
name|host
argument_list|,
name|port
argument_list|,
name|clientSocketTimeout
argument_list|,
name|trustStorePath
argument_list|,
name|trustStorePassword
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Opened an SSL connection to metastore, current connections"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|useSasl
condition|)
block|{
comment|// Wrap thrift connection with SASL for secure connection.
name|HadoopThriftAuthBridge
operator|.
name|Client
name|authBridge
init|=
name|HadoopThriftAuthBridge
operator|.
name|getBridge
argument_list|()
operator|.
name|createClient
argument_list|()
decl_stmt|;
comment|// check if we should use delegation tokens to authenticate
comment|// the call below gets hold of the tokens if they are set up by hadoop
comment|// this should happen on the map/reduce tasks if the client added the
comment|// tokens into hadoop's credential store in the front end during job
comment|// submission.
name|String
name|tokenSig
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|TOKEN_SIGNATURE
argument_list|)
decl_stmt|;
comment|// tokenSig could be null
name|String
name|tokenStrForm
init|=
name|SecurityUtils
operator|.
name|getTokenStrForm
argument_list|(
name|tokenSig
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokenStrForm
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"HMSC::open(): Found delegation token. Creating DIGEST-based thrift connection."
argument_list|)
expr_stmt|;
comment|// authenticate using delegation tokens via the "DIGEST" mechanism
name|transport
operator|=
name|authBridge
operator|.
name|createClientTransport
argument_list|(
literal|null
argument_list|,
name|host
argument_list|,
literal|"DIGEST"
argument_list|,
name|tokenStrForm
argument_list|,
name|transport
argument_list|,
name|MetaStoreUtils
operator|.
name|getMetaStoreSaslProperties
argument_list|(
name|conf
argument_list|,
name|useSSL
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"HMSC::open(): Could not find delegation token. Creating KERBEROS-based thrift connection."
argument_list|)
expr_stmt|;
name|String
name|principalConfig
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|KERBEROS_PRINCIPAL
argument_list|)
decl_stmt|;
name|transport
operator|=
name|authBridge
operator|.
name|createClientTransport
argument_list|(
name|principalConfig
argument_list|,
name|host
argument_list|,
literal|"KERBEROS"
argument_list|,
literal|null
argument_list|,
name|transport
argument_list|,
name|MetaStoreUtils
operator|.
name|getMetaStoreSaslProperties
argument_list|(
name|conf
argument_list|,
name|useSSL
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|useFramedTransport
condition|)
block|{
name|transport
operator|=
operator|new
name|TFramedTransport
argument_list|(
name|transport
argument_list|)
expr_stmt|;
block|}
block|}
specifier|final
name|TProtocol
name|protocol
decl_stmt|;
if|if
condition|(
name|useCompactProtocol
condition|)
block|{
name|protocol
operator|=
operator|new
name|TCompactProtocol
argument_list|(
name|transport
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|protocol
operator|=
operator|new
name|TBinaryProtocol
argument_list|(
name|transport
argument_list|)
expr_stmt|;
block|}
name|client
operator|=
operator|new
name|ThriftHiveMetastore
operator|.
name|Client
argument_list|(
name|protocol
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|transport
operator|.
name|isOpen
argument_list|()
condition|)
block|{
name|transport
operator|.
name|open
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Opened a connection to metastore, current connections"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|useSasl
operator|&&
name|MetastoreConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|EXECUTE_SET_UGI
argument_list|)
condition|)
block|{
comment|// Call set_ugi, only in unsecure mode.
try|try
block|{
name|UserGroupInformation
name|ugi
init|=
name|SecurityUtils
operator|.
name|getUGI
argument_list|()
decl_stmt|;
name|client
operator|.
name|set_ugi
argument_list|(
name|ugi
operator|.
name|getUserName
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|ugi
operator|.
name|getGroupNames
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LoginException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to do login. set_ugi() is not successful, "
operator|+
literal|"Continuing without it."
argument_list|,
name|e
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
name|warn
argument_list|(
literal|"Failed to find ugi of client set_ugi() is not successful, "
operator|+
literal|"Continuing without it."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"set_ugi() not successful, Likely cause: new client talking to old server. "
operator|+
literal|"Continuing without it."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Connected to metastore, using compact protocol = {}"
argument_list|,
name|useCompactProtocol
argument_list|)
expr_stmt|;
return|return
name|transport
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|transport
operator|!=
literal|null
operator|&&
name|transport
operator|.
name|isOpen
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Closing thrift transport"
argument_list|)
expr_stmt|;
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

