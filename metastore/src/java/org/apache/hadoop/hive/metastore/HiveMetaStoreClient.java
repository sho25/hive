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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|AlreadyExistsException
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
name|ConfigValSecurityException
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
name|FieldSchema
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
name|InvalidObjectException
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
name|InvalidOperationException
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
name|NoSuchObjectException
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
name|api
operator|.
name|Type
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
name|UnknownDBException
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
name|UnknownTableException
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
comment|/**  * Hive Metastore Client.  */
end_comment

begin_class
specifier|public
class|class
name|HiveMetaStoreClient
implements|implements
name|IMetaStoreClient
block|{
name|ThriftHiveMetastore
operator|.
name|Iface
name|client
init|=
literal|null
decl_stmt|;
specifier|private
name|TTransport
name|transport
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|open
init|=
literal|false
decl_stmt|;
specifier|private
name|URI
name|metastoreUris
index|[]
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|standAloneClient
init|=
literal|false
decl_stmt|;
specifier|private
name|HiveMetaHookLoader
name|hookLoader
decl_stmt|;
comment|// for thrift connects
specifier|private
name|int
name|retries
init|=
literal|5
decl_stmt|;
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"hive.metastore"
argument_list|)
decl_stmt|;
specifier|public
name|HiveMetaStoreClient
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|MetaException
block|{
name|this
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveMetaStoreClient
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|HiveMetaHookLoader
name|hookLoader
parameter_list|)
throws|throws
name|MetaException
block|{
name|this
operator|.
name|hookLoader
operator|=
name|hookLoader
expr_stmt|;
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|HiveMetaStoreClient
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
name|boolean
name|localMetaStore
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hive.metastore.local"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|localMetaStore
condition|)
block|{
comment|// instantiate the metastore server handler directly instead of connecting
comment|// through the network
name|client
operator|=
operator|new
name|HiveMetaStore
operator|.
name|HMSHandler
argument_list|(
literal|"hive client"
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|open
operator|=
literal|true
expr_stmt|;
return|return;
block|}
comment|// get the number retries
name|retries
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hive.metastore.connect.retries"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
comment|// user wants file store based configuration
if|if
condition|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|String
name|metastoreUrisString
index|[]
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|)
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|metastoreUris
operator|=
operator|new
name|URI
index|[
name|metastoreUrisString
operator|.
name|length
index|]
expr_stmt|;
try|try
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|metastoreUrisString
control|)
block|{
name|URI
name|tmpUri
init|=
operator|new
name|URI
argument_list|(
name|s
argument_list|)
decl_stmt|;
if|if
condition|(
name|tmpUri
operator|.
name|getScheme
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"URI: "
operator|+
name|s
operator|+
literal|" does not have a scheme"
argument_list|)
throw|;
block|}
name|metastoreUris
index|[
name|i
operator|++
index|]
operator|=
name|tmpUri
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|e
operator|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|MetaStoreUtils
operator|.
name|logAndThrowMetaException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREDIRECTORY
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|metastoreUris
operator|=
operator|new
name|URI
index|[
literal|1
index|]
expr_stmt|;
try|try
block|{
name|metastoreUris
index|[
literal|0
index|]
operator|=
operator|new
name|URI
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREDIRECTORY
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
name|MetaStoreUtils
operator|.
name|logAndThrowMetaException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"NOT getting uris from conf"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"MetaStoreURIs not found in conf file"
argument_list|)
throw|;
block|}
comment|// finally open the store
name|open
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param dbname    * @param tbl_name    * @param new_tbl    * @throws InvalidOperationException    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#alter_table(java.lang.String,    *      java.lang.String, org.apache.hadoop.hive.metastore.api.Table)    */
specifier|public
name|void
name|alter_table
parameter_list|(
name|String
name|dbname
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|Table
name|new_tbl
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|MetaException
throws|,
name|TException
block|{
name|client
operator|.
name|alter_table
argument_list|(
name|dbname
argument_list|,
name|tbl_name
argument_list|,
name|new_tbl
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|open
parameter_list|()
throws|throws
name|MetaException
block|{
for|for
control|(
name|URI
name|store
range|:
name|metastoreUris
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Trying to connect to metastore with URI "
operator|+
name|store
argument_list|)
expr_stmt|;
try|try
block|{
name|openStore
argument_list|(
name|store
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|e
operator|.
name|getStackTrace
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to connect metastore with URI "
operator|+
name|store
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|open
condition|)
block|{
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|open
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Could not connect to meta store using any of the URIs provided"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Connected to metastore."
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|openStore
parameter_list|(
name|URI
name|store
parameter_list|)
throws|throws
name|MetaException
block|{
name|open
operator|=
literal|false
expr_stmt|;
name|transport
operator|=
operator|new
name|TSocket
argument_list|(
name|store
operator|.
name|getHost
argument_list|()
argument_list|,
name|store
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
operator|(
operator|(
name|TSocket
operator|)
name|transport
operator|)
operator|.
name|setTimeout
argument_list|(
literal|20000
argument_list|)
expr_stmt|;
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
name|ThriftHiveMetastore
operator|.
name|Client
argument_list|(
name|protocol
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|retries
operator|&&
operator|!
name|open
condition|;
operator|++
name|i
control|)
block|{
try|try
block|{
name|transport
operator|.
name|open
argument_list|()
expr_stmt|;
name|open
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TTransportException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"failed to connect to MetaStore, re-trying..."
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ignore
parameter_list|)
block|{         }
block|}
block|}
if|if
condition|(
operator|!
name|open
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"could not connect to meta store"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
name|open
operator|=
literal|false
expr_stmt|;
if|if
condition|(
operator|(
name|transport
operator|!=
literal|null
operator|)
operator|&&
name|transport
operator|.
name|isOpen
argument_list|()
condition|)
block|{
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|standAloneClient
condition|)
block|{
try|try
block|{
name|client
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
comment|// TODO:pc cleanup the exceptions
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to shutdown local metastore client"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|getStackTrace
argument_list|()
argument_list|)
expr_stmt|;
comment|// throw new RuntimeException(e.getMessage());
block|}
block|}
block|}
specifier|public
name|void
name|dropTable
parameter_list|(
name|String
name|tableName
parameter_list|,
name|boolean
name|deleteData
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
comment|// assume that it is default database
try|try
block|{
name|this
operator|.
name|dropTable
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
name|tableName
argument_list|,
name|deleteData
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
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
name|MetaStoreUtils
operator|.
name|logAndThrowMetaException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @param new_part    * @return the added partition    * @throws InvalidObjectException    * @throws AlreadyExistsException    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#add_partition(org.apache.hadoop.hive.metastore.api.Partition)    */
specifier|public
name|Partition
name|add_partition
parameter_list|(
name|Partition
name|new_part
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|AlreadyExistsException
throws|,
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|add_partition
argument_list|(
name|new_part
argument_list|)
return|;
block|}
comment|/**    * @param table_name    * @param db_name    * @param part_vals    * @return the appended partition    * @throws InvalidObjectException    * @throws AlreadyExistsException    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#append_partition(java.lang.String,    *      java.lang.String, java.util.List)    */
specifier|public
name|Partition
name|appendPartition
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|table_name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|AlreadyExistsException
throws|,
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|append_partition
argument_list|(
name|db_name
argument_list|,
name|table_name
argument_list|,
name|part_vals
argument_list|)
return|;
block|}
specifier|public
name|Partition
name|appendPartition
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|partName
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|AlreadyExistsException
throws|,
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|append_partition_by_name
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partName
argument_list|)
return|;
block|}
comment|/**    * @param name    * @param location_uri    * @return true or false    * @throws AlreadyExistsException    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#create_database(java.lang.String,    *      java.lang.String)    */
specifier|public
name|boolean
name|createDatabase
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|location_uri
parameter_list|)
throws|throws
name|AlreadyExistsException
throws|,
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|create_database
argument_list|(
name|name
argument_list|,
name|location_uri
argument_list|)
return|;
block|}
comment|/**    * @param tbl    * @throws MetaException    * @throws NoSuchObjectException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#create_table(org.apache.hadoop.hive.metastore.api.Table)    */
specifier|public
name|void
name|createTable
parameter_list|(
name|Table
name|tbl
parameter_list|)
throws|throws
name|AlreadyExistsException
throws|,
name|InvalidObjectException
throws|,
name|MetaException
throws|,
name|NoSuchObjectException
throws|,
name|TException
block|{
name|HiveMetaHook
name|hook
init|=
name|getHook
argument_list|(
name|tbl
argument_list|)
decl_stmt|;
if|if
condition|(
name|hook
operator|!=
literal|null
condition|)
block|{
name|hook
operator|.
name|preCreateTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
block|}
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|client
operator|.
name|create_table
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
if|if
condition|(
name|hook
operator|!=
literal|null
condition|)
block|{
name|hook
operator|.
name|commitCreateTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
block|}
name|success
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|success
operator|&&
operator|(
name|hook
operator|!=
literal|null
operator|)
condition|)
block|{
name|hook
operator|.
name|rollbackCreateTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @param type    * @return true or false    * @throws AlreadyExistsException    * @throws InvalidObjectException    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#create_type(org.apache.hadoop.hive.metastore.api.Type)    */
specifier|public
name|boolean
name|createType
parameter_list|(
name|Type
name|type
parameter_list|)
throws|throws
name|AlreadyExistsException
throws|,
name|InvalidObjectException
throws|,
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|create_type
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**    * @param name    * @return true or false    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_database(java.lang.String)    */
specifier|public
name|boolean
name|dropDatabase
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|drop_database
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**    * @param tbl_name    * @param db_name    * @param part_vals    * @return true or false    * @throws NoSuchObjectException    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_partition(java.lang.String,    *      java.lang.String, java.util.List, boolean)    */
specifier|public
name|boolean
name|dropPartition
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|TException
block|{
return|return
name|dropPartition
argument_list|(
name|db_name
argument_list|,
name|tbl_name
argument_list|,
name|part_vals
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|dropPartition
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|partName
parameter_list|,
name|boolean
name|deleteData
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|drop_partition_by_name
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partName
argument_list|,
name|deleteData
argument_list|)
return|;
block|}
comment|/**    * @param db_name    * @param tbl_name    * @param part_vals    * @param deleteData    *          delete the underlying data or just delete the table in metadata    * @return true or false    * @throws NoSuchObjectException    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_partition(java.lang.String,    *      java.lang.String, java.util.List, boolean)    */
specifier|public
name|boolean
name|dropPartition
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|,
name|boolean
name|deleteData
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|drop_partition
argument_list|(
name|db_name
argument_list|,
name|tbl_name
argument_list|,
name|part_vals
argument_list|,
name|deleteData
argument_list|)
return|;
block|}
comment|/**    * @param name    * @param dbname    * @throws NoSuchObjectException    * @throws ExistingDependentsException    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_table(java.lang.String,    *      java.lang.String, boolean)    */
specifier|public
name|void
name|dropTable
parameter_list|(
name|String
name|dbname
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|TException
block|{
name|dropTable
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param dbname    * @param name    * @param deleteData    *          delete the underlying data or just delete the table in metadata    * @throws NoSuchObjectException    * @throws ExistingDependentsException    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_table(java.lang.String,    *      java.lang.String, boolean)    */
specifier|public
name|void
name|dropTable
parameter_list|(
name|String
name|dbname
parameter_list|,
name|String
name|name
parameter_list|,
name|boolean
name|deleteData
parameter_list|,
name|boolean
name|ignoreUknownTab
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|NoSuchObjectException
block|{
name|Table
name|tbl
decl_stmt|;
try|try
block|{
name|tbl
operator|=
name|getTable
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|ignoreUknownTab
condition|)
block|{
throw|throw
name|e
throw|;
block|}
return|return;
block|}
name|HiveMetaHook
name|hook
init|=
name|getHook
argument_list|(
name|tbl
argument_list|)
decl_stmt|;
if|if
condition|(
name|hook
operator|!=
literal|null
condition|)
block|{
name|hook
operator|.
name|preDropTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
block|}
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|client
operator|.
name|drop_table
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|,
name|deleteData
argument_list|)
expr_stmt|;
if|if
condition|(
name|hook
operator|!=
literal|null
condition|)
block|{
name|hook
operator|.
name|commitDropTable
argument_list|(
name|tbl
argument_list|,
name|deleteData
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|ignoreUknownTab
condition|)
block|{
throw|throw
name|e
throw|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|success
operator|&&
operator|(
name|hook
operator|!=
literal|null
operator|)
condition|)
block|{
name|hook
operator|.
name|rollbackDropTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @param type    * @return true if the type is dropped    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_type(java.lang.String)    */
specifier|public
name|boolean
name|dropType
parameter_list|(
name|String
name|type
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|drop_type
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**    * @param name    * @return map of types    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_type_all(java.lang.String)    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Type
argument_list|>
name|getTypeAll
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|get_type_all
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**    * @return the list of databases    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_databases()    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getDatabases
parameter_list|()
throws|throws
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|get_databases
argument_list|()
return|;
block|}
comment|/**    * @param tbl_name    * @param db_name    * @param max_parts    * @return list of partitions    * @throws NoSuchObjectException    * @throws MetaException    * @throws TException    */
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|listPartitions
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|short
name|max_parts
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|get_partitions
argument_list|(
name|db_name
argument_list|,
name|tbl_name
argument_list|,
name|max_parts
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|listPartitions
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|,
name|short
name|max_parts
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|get_partitions_ps
argument_list|(
name|db_name
argument_list|,
name|tbl_name
argument_list|,
name|part_vals
argument_list|,
name|max_parts
argument_list|)
return|;
block|}
comment|/**    * @param name    * @return the database    * @throws NoSuchObjectException    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_database(java.lang.String)    */
specifier|public
name|Database
name|getDatabase
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|get_database
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**    * @param tbl_name    * @param db_name    * @param part_vals    * @return the partition    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_partition(java.lang.String,    *      java.lang.String, java.util.List)    */
specifier|public
name|Partition
name|getPartition
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|get_partition
argument_list|(
name|db_name
argument_list|,
name|tbl_name
argument_list|,
name|part_vals
argument_list|)
return|;
block|}
comment|/**    * @param name    * @param dbname    * @return the table    * @throws NoSuchObjectException    * @throws MetaException    * @throws TException    * @throws NoSuchObjectException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_table(java.lang.String,    *      java.lang.String)    */
specifier|public
name|Table
name|getTable
parameter_list|(
name|String
name|dbname
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|NoSuchObjectException
block|{
return|return
name|client
operator|.
name|get_table
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|)
return|;
block|}
comment|/**    * @param name    * @return the type    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_type(java.lang.String)    */
specifier|public
name|Type
name|getType
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|get_type
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getTables
parameter_list|(
name|String
name|dbname
parameter_list|,
name|String
name|tablePattern
parameter_list|)
throws|throws
name|MetaException
block|{
try|try
block|{
return|return
name|client
operator|.
name|get_tables
argument_list|(
name|dbname
argument_list|,
name|tablePattern
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|MetaStoreUtils
operator|.
name|logAndThrowMetaException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getTables
parameter_list|(
name|String
name|tablePattern
parameter_list|)
throws|throws
name|MetaException
block|{
name|String
name|dbname
init|=
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
decl_stmt|;
return|return
name|this
operator|.
name|getTables
argument_list|(
name|dbname
argument_list|,
name|tablePattern
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|tableExists
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|UnknownDBException
block|{
try|try
block|{
name|client
operator|.
name|get_table
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|Table
name|getTable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|NoSuchObjectException
block|{
return|return
name|getTable
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|listPartitionNames
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|short
name|max
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|get_partition_names
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|max
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|listPartitionNames
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|,
name|short
name|max_parts
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
block|{
return|return
name|client
operator|.
name|get_partition_names_ps
argument_list|(
name|db_name
argument_list|,
name|tbl_name
argument_list|,
name|part_vals
argument_list|,
name|max_parts
argument_list|)
return|;
block|}
specifier|public
name|void
name|alter_partition
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|Partition
name|newPart
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|MetaException
throws|,
name|TException
block|{
name|client
operator|.
name|alter_partition
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|newPart
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param db    * @param tableName    * @throws UnknownTableException    * @throws UnknownDBException    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_fields(java.lang.String,    *      java.lang.String)    */
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getFields
parameter_list|(
name|String
name|db
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|UnknownTableException
throws|,
name|UnknownDBException
block|{
return|return
name|client
operator|.
name|get_fields
argument_list|(
name|db
argument_list|,
name|tableName
argument_list|)
return|;
block|}
comment|/**    * @param db    * @param tableName    * @throws UnknownTableException    * @throws UnknownDBException    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_schema(java.lang.String,    *      java.lang.String)    */
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|getSchema
parameter_list|(
name|String
name|db
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|UnknownTableException
throws|,
name|UnknownDBException
block|{
return|return
name|client
operator|.
name|get_schema
argument_list|(
name|db
argument_list|,
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|String
name|getConfigValue
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|defaultValue
parameter_list|)
throws|throws
name|TException
throws|,
name|ConfigValSecurityException
block|{
return|return
name|client
operator|.
name|get_config_value
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
specifier|public
name|Partition
name|getPartition
parameter_list|(
name|String
name|db
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|partName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|UnknownTableException
throws|,
name|NoSuchObjectException
block|{
return|return
name|client
operator|.
name|get_partition_by_name
argument_list|(
name|db
argument_list|,
name|tableName
argument_list|,
name|partName
argument_list|)
return|;
block|}
specifier|private
name|HiveMetaHook
name|getHook
parameter_list|(
name|Table
name|tbl
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|hookLoader
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|hookLoader
operator|.
name|getHook
argument_list|(
name|tbl
argument_list|)
return|;
block|}
block|}
end_class

end_unit

