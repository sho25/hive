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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Enumeration
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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|Properties
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
name|fs
operator|.
name|Path
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
name|Constants
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
name|ThriftMetaStore
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
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
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
name|com
operator|.
name|facebook
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
name|com
operator|.
name|facebook
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
name|com
operator|.
name|facebook
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
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportException
import|;
end_import

begin_comment
comment|/**  * TODO Unnecessary when the server sides for both dbstore and filestore are merged  */
end_comment

begin_class
specifier|public
class|class
name|MetaStoreClient
implements|implements
name|IMetaStoreClient
block|{
specifier|private
name|TTransport
name|transport
decl_stmt|;
specifier|private
name|ThriftMetaStore
operator|.
name|Iface
name|client
decl_stmt|;
specifier|private
name|boolean
name|open
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|URI
name|metastoreUris
index|[]
decl_stmt|;
specifier|private
name|Warehouse
name|wh
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"hive.metastore.client"
argument_list|)
decl_stmt|;
comment|// for thrift connects
specifier|private
specifier|static
specifier|final
name|int
name|retries
init|=
literal|5
decl_stmt|;
specifier|public
name|MetaStoreClient
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|configuration
operator|==
literal|null
condition|)
block|{
name|configuration
operator|=
operator|new
name|HiveConf
argument_list|(
name|MetaStoreClient
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
name|wh
operator|=
operator|new
name|Warehouse
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|configuration
argument_list|,
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
name|HiveConf
operator|.
name|getVar
argument_list|(
name|configuration
argument_list|,
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
name|this
operator|.
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
operator|.
name|trim
argument_list|()
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
name|this
operator|.
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
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Exception getting uri to connect to the store with: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|configuration
argument_list|,
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
name|this
operator|.
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
name|this
operator|.
name|metastoreUris
index|[
literal|0
index|]
operator|=
operator|new
name|URI
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|configuration
argument_list|,
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
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Exception getting uri to connect to the store with: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"NOT getting uris from conf"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"MetaStoreURIs not found in conf file"
argument_list|)
throw|;
block|}
name|this
operator|.
name|conf
operator|=
name|configuration
expr_stmt|;
name|transport
operator|=
literal|null
expr_stmt|;
name|client
operator|=
literal|null
expr_stmt|;
name|open
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|void
name|open
parameter_list|(
name|URI
name|store
parameter_list|)
throws|throws
name|TException
block|{
name|this
operator|.
name|open
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|store
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"thrift"
argument_list|)
condition|)
block|{
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
literal|2000
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
name|ThriftMetaStore
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
name|this
operator|.
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
name|this
operator|.
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
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"WARN: failed to connect to MetaStore, re-trying..."
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
block|{ }
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
name|TException
argument_list|(
literal|"could not connect to meta store"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|store
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"file"
argument_list|)
condition|)
block|{
name|client
operator|=
operator|new
name|MetaStoreServer
operator|.
name|ThriftMetaStoreHandler
argument_list|(
literal|"temp_server"
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
try|try
block|{
comment|// for some reason setOption in FB303 doesn't allow one to throw a TException,
comment|// so I'm having it throw a RuntimeException since that doesn't require changing
comment|// the method signature.
name|client
operator|.
name|setOption
argument_list|(
literal|"metastore.path"
argument_list|,
name|store
operator|.
name|toASCIIString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Could not setoption metastore.path to "
operator|+
name|store
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|TException
argument_list|(
literal|"could not set metastore path to: "
operator|+
name|store
operator|.
name|getPath
argument_list|()
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|TException
argument_list|(
literal|"Unknown scheme to connect to MetaStore: "
operator|+
name|store
operator|.
name|getScheme
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|open
condition|)
block|{
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getTables
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tablePattern
parameter_list|)
throws|throws
name|MetaException
throws|,
name|UnknownTableException
throws|,
name|TException
throws|,
name|UnknownDBException
block|{
name|TException
name|firstException
init|=
literal|null
decl_stmt|;
for|for
control|(
name|URI
name|store
range|:
name|this
operator|.
name|metastoreUris
control|)
block|{
try|try
block|{
name|this
operator|.
name|open
argument_list|(
name|store
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|ret
init|=
name|client
operator|.
name|get_tables
argument_list|(
name|dbName
argument_list|,
name|tablePattern
argument_list|)
decl_stmt|;
name|this
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|ret
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"get_tables got exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|firstException
operator|==
literal|null
condition|)
block|{
name|firstException
operator|=
name|e
expr_stmt|;
block|}
block|}
comment|// note - do not catchMetaException or UnkownTableException as we want those to propagate up w/o retrying backup stores.
block|}
throw|throw
name|firstException
throw|;
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
name|UnknownTableException
throws|,
name|TException
block|{
name|TException
name|firstException
init|=
literal|null
decl_stmt|;
name|String
name|dbName
init|=
literal|"default"
decl_stmt|;
comment|// Ignore deleteData for now
for|for
control|(
name|URI
name|store
range|:
name|this
operator|.
name|metastoreUris
control|)
block|{
try|try
block|{
name|this
operator|.
name|open
argument_list|(
name|store
argument_list|)
expr_stmt|;
try|try
block|{
name|client
operator|.
name|drop_table
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownDBException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UnknownTableException
argument_list|()
throw|;
block|}
name|close
argument_list|()
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"get_tables got exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|firstException
operator|==
literal|null
condition|)
block|{
name|firstException
operator|=
name|e
expr_stmt|;
block|}
block|}
comment|// note - do not catchMetaException or UnkownTableException as we want those to propagate up w/o retrying backup stores.
block|}
if|if
condition|(
name|firstException
operator|!=
literal|null
condition|)
block|{
throw|throw
name|firstException
throw|;
block|}
block|}
specifier|public
name|void
name|createTable
parameter_list|(
name|String
name|tableName
parameter_list|,
name|Properties
name|schema
parameter_list|)
throws|throws
name|MetaException
throws|,
name|UnknownTableException
throws|,
name|TException
block|{
name|TException
name|firstException
init|=
literal|null
decl_stmt|;
name|String
name|dbName
init|=
literal|"default"
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hm
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
for|for
control|(
name|Enumeration
argument_list|<
name|?
argument_list|>
name|e
init|=
name|schema
operator|.
name|propertyNames
argument_list|()
init|;
name|e
operator|.
name|hasMoreElements
argument_list|()
condition|;
control|)
block|{
name|String
name|key
init|=
operator|(
name|String
operator|)
name|e
operator|.
name|nextElement
argument_list|()
decl_stmt|;
name|String
name|val
init|=
name|schema
operator|.
name|getProperty
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|hm
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|URI
name|store
range|:
name|this
operator|.
name|metastoreUris
control|)
block|{
try|try
block|{
name|this
operator|.
name|open
argument_list|(
name|store
argument_list|)
expr_stmt|;
try|try
block|{
name|client
operator|.
name|create_table
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|hm
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownDBException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UnknownTableException
argument_list|()
throw|;
block|}
name|close
argument_list|()
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"get_tables got exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|firstException
operator|==
literal|null
condition|)
block|{
name|firstException
operator|=
name|e
expr_stmt|;
block|}
block|}
comment|// note - do not catchMetaException or UnkownTableException as we want those to propagate up w/o retrying backup stores.
block|}
if|if
condition|(
name|firstException
operator|!=
literal|null
condition|)
block|{
throw|throw
name|firstException
throw|;
block|}
block|}
specifier|public
name|Properties
name|getSchema
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
name|TException
name|firstException
init|=
literal|null
decl_stmt|;
for|for
control|(
name|URI
name|store
range|:
name|this
operator|.
name|metastoreUris
control|)
block|{
try|try
block|{
name|this
operator|.
name|open
argument_list|(
name|store
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|schema_map
init|=
name|client
operator|.
name|get_schema
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Properties
name|p
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
for|for
control|(
name|Iterator
name|it
init|=
name|schema_map
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Map
operator|.
name|Entry
name|e
init|=
operator|(
name|Map
operator|.
name|Entry
operator|)
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|key
init|=
operator|(
name|String
operator|)
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|val
init|=
operator|(
name|String
operator|)
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|p
operator|.
name|setProperty
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
name|p
operator|=
name|MetaStoreUtils
operator|.
name|hive1Tohive3ClassNames
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|this
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|p
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"get_schema got exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
if|if
condition|(
name|firstException
operator|==
literal|null
condition|)
block|{
name|firstException
operator|=
name|e
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|UnknownTableException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|NoSuchObjectException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|UnknownDBException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|NoSuchObjectException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
throw|throw
name|firstException
throw|;
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
name|TException
name|firstException
init|=
literal|null
decl_stmt|;
name|String
name|dbName
init|=
literal|"default"
decl_stmt|;
for|for
control|(
name|URI
name|store
range|:
name|this
operator|.
name|metastoreUris
control|)
block|{
try|try
block|{
name|this
operator|.
name|open
argument_list|(
name|store
argument_list|)
expr_stmt|;
name|boolean
name|ret
init|=
name|client
operator|.
name|table_exists
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|this
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|ret
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"schema_exists got exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|firstException
operator|==
literal|null
condition|)
block|{
name|firstException
operator|=
name|e
expr_stmt|;
block|}
block|}
comment|// note - do not catchMetaException or UnkownTableException as we want those to propagate up w/o retrying backup stores.
block|}
throw|throw
name|firstException
throw|;
block|}
specifier|public
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|get_fields
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|UnknownTableException
throws|,
name|TException
block|{
name|TException
name|firstException
init|=
literal|null
decl_stmt|;
name|String
name|dbName
init|=
literal|"default"
decl_stmt|;
for|for
control|(
name|URI
name|store
range|:
name|this
operator|.
name|metastoreUris
control|)
block|{
try|try
block|{
name|this
operator|.
name|open
argument_list|(
name|store
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
init|=
name|client
operator|.
name|get_fields
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|this
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|fields
return|;
block|}
catch|catch
parameter_list|(
name|UnknownDBException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"get_schema got exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
if|if
condition|(
name|firstException
operator|==
literal|null
condition|)
block|{
name|firstException
operator|=
name|e
expr_stmt|;
block|}
block|}
comment|// note - do not catchMetaException or UnkownTableException as we want those to propagate up w/o retrying backup stores.
block|}
throw|throw
name|firstException
throw|;
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
name|Properties
name|schema
init|=
name|this
operator|.
name|getSchema
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
return|return
name|MetaStoreUtils
operator|.
name|getTable
argument_list|(
name|schema
argument_list|)
return|;
block|}
comment|//These will disappear when the server is unified for both filestore and dbstore
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
name|dbName
parameter_list|,
name|String
name|tableName
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
comment|//TODO: move the code from Table.getPartitions() to here
return|return
operator|new
name|ArrayList
argument_list|<
name|Partition
argument_list|>
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Partition
name|getPartition
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
block|{
if|if
condition|(
name|partVals
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
comment|// need to get the table for partition key names.
comment|// this is inefficient because caller of this function has table already
name|Partition
name|part
init|=
name|getPartitionObject
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partVals
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|wh
operator|.
name|isDir
argument_list|(
operator|new
name|Path
argument_list|(
name|part
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
comment|// partition doesn't exist in hdfs so return nothing
return|return
literal|null
return|;
block|}
return|return
name|part
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
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
name|Properties
name|schema
init|=
name|MetaStoreUtils
operator|.
name|getSchema
argument_list|(
name|tbl
argument_list|)
decl_stmt|;
try|try
block|{
name|this
operator|.
name|createTable
argument_list|(
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|,
name|schema
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownTableException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|NoSuchObjectException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
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
name|List
argument_list|<
name|String
argument_list|>
name|partVals
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
if|if
condition|(
name|partVals
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Partition
name|part
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// need to get the table for partition key names.
comment|// this is inefficient because caller of this function has table already
name|part
operator|=
name|getPartitionObject
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partVals
argument_list|)
expr_stmt|;
name|wh
operator|.
name|mkdirs
argument_list|(
operator|new
name|Path
argument_list|(
name|part
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// this will throw an exception if the dir couldn't be created
return|return
name|part
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|InvalidObjectException
argument_list|(
literal|"table or database doesn't exist"
argument_list|)
throw|;
block|}
block|}
comment|/**    * @param tableName    * @param dbName    * @param partVals    * @return    * @throws MetaException    * @throws TException    * @throws NoSuchObjectException    */
specifier|private
name|Partition
name|getPartitionObject
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|NoSuchObjectException
block|{
name|Properties
name|schema
init|=
name|this
operator|.
name|getSchema
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Table
name|tbl
init|=
name|MetaStoreUtils
operator|.
name|getTable
argument_list|(
name|schema
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partKeys
init|=
name|tbl
operator|.
name|getPartitionKeys
argument_list|()
decl_stmt|;
if|if
condition|(
name|partKeys
operator|.
name|size
argument_list|()
operator|!=
name|partVals
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Invalid partition key values: "
operator|+
name|partVals
argument_list|)
throw|;
block|}
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|pm
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|partKeys
operator|.
name|size
argument_list|()
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
name|partKeys
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
name|partVals
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|==
literal|null
operator|||
name|partVals
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Invalid partition spec: "
operator|+
name|partVals
argument_list|)
throw|;
block|}
name|pm
operator|.
name|put
argument_list|(
name|partKeys
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
name|partVals
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Path
name|partPath
init|=
name|wh
operator|.
name|getPartitionPath
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|pm
argument_list|)
decl_stmt|;
name|Partition
name|tPartition
init|=
operator|new
name|Partition
argument_list|()
decl_stmt|;
name|tPartition
operator|.
name|setValues
argument_list|(
name|partVals
argument_list|)
expr_stmt|;
name|tPartition
operator|.
name|setSd
argument_list|(
name|tbl
operator|.
name|getSd
argument_list|()
argument_list|)
expr_stmt|;
name|tPartition
operator|.
name|setParameters
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|tPartition
operator|.
name|getSd
argument_list|()
operator|.
name|setLocation
argument_list|(
name|partPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|tPartition
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|alter_table
parameter_list|(
name|String
name|defaultDatabaseName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|Table
name|table
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|MetaException
throws|,
name|TException
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Not yet implementd in filestore"
argument_list|)
throw|;
block|}
annotation|@
name|Override
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
comment|// TODO Auto-generated method stub
return|return
literal|false
return|;
block|}
annotation|@
name|Override
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
comment|// TODO Auto-generated method stub
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

