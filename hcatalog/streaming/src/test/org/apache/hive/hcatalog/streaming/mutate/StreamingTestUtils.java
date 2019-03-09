begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
operator|.
name|mutate
package|;
end_package

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
name|FileNotFoundException
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|Map
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
name|FileStatus
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
name|fs
operator|.
name|RawLocalFileSystem
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
name|permission
operator|.
name|FsPermission
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
name|HiveMetaStoreClient
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
name|IMetaStoreClient
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
name|TableType
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
name|Warehouse
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
name|SerDeInfo
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
name|StorageDescriptor
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
name|txn
operator|.
name|TxnDbUtil
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
name|io
operator|.
name|orc
operator|.
name|OrcInputFormat
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
name|io
operator|.
name|orc
operator|.
name|OrcOutputFormat
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
name|io
operator|.
name|orc
operator|.
name|OrcSerde
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
name|thrift
operator|.
name|TException
import|;
end_import

begin_class
specifier|public
class|class
name|StreamingTestUtils
block|{
specifier|public
name|HiveConf
name|newHiveConf
parameter_list|(
name|String
name|metaStoreUri
parameter_list|)
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"fs.raw.impl"
argument_list|,
name|RawFileSystem
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|metaStoreUri
operator|!=
literal|null
condition|)
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|,
name|metaStoreUri
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EXECUTE_SET_UGI
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
specifier|public
name|void
name|prepareTransactionDatabase
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|TxnDbUtil
operator|.
name|setConfValues
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|TxnDbUtil
operator|.
name|cleanDb
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|TxnDbUtil
operator|.
name|prepDb
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|IMetaStoreClient
name|newMetaStoreClient
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
return|return
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|RawFileSystem
extends|extends
name|RawLocalFileSystem
block|{
specifier|private
specifier|static
specifier|final
name|URI
name|NAME
decl_stmt|;
static|static
block|{
try|try
block|{
name|NAME
operator|=
operator|new
name|URI
argument_list|(
literal|"raw:///"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|se
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"bad uri"
argument_list|,
name|se
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|URI
name|getUri
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getScheme
parameter_list|()
block|{
return|return
literal|"raw"
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileStatus
name|getFileStatus
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|File
name|file
init|=
name|pathToFile
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|file
operator|.
name|exists
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
literal|"Can't find "
operator|+
name|path
argument_list|)
throw|;
block|}
comment|// get close enough
name|short
name|mod
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|file
operator|.
name|canRead
argument_list|()
condition|)
block|{
name|mod
operator||=
literal|0444
expr_stmt|;
block|}
if|if
condition|(
name|file
operator|.
name|canWrite
argument_list|()
condition|)
block|{
name|mod
operator||=
literal|0200
expr_stmt|;
block|}
if|if
condition|(
name|file
operator|.
name|canExecute
argument_list|()
condition|)
block|{
name|mod
operator||=
literal|0111
expr_stmt|;
block|}
return|return
operator|new
name|FileStatus
argument_list|(
name|file
operator|.
name|length
argument_list|()
argument_list|,
name|file
operator|.
name|isDirectory
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|1024
argument_list|,
name|file
operator|.
name|lastModified
argument_list|()
argument_list|,
name|file
operator|.
name|lastModified
argument_list|()
argument_list|,
name|FsPermission
operator|.
name|createImmutable
argument_list|(
name|mod
argument_list|)
argument_list|,
literal|"owen"
argument_list|,
literal|"users"
argument_list|,
name|path
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|DatabaseBuilder
name|databaseBuilder
parameter_list|(
name|File
name|warehouseFolder
parameter_list|)
block|{
return|return
operator|new
name|DatabaseBuilder
argument_list|(
name|warehouseFolder
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|DatabaseBuilder
block|{
specifier|private
name|Database
name|database
decl_stmt|;
specifier|private
name|File
name|warehouseFolder
decl_stmt|;
specifier|public
name|DatabaseBuilder
parameter_list|(
name|File
name|warehouseFolder
parameter_list|)
block|{
name|this
operator|.
name|warehouseFolder
operator|=
name|warehouseFolder
expr_stmt|;
name|database
operator|=
operator|new
name|Database
argument_list|()
expr_stmt|;
block|}
specifier|public
name|DatabaseBuilder
name|name
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|database
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|File
name|databaseFolder
init|=
operator|new
name|File
argument_list|(
name|warehouseFolder
argument_list|,
name|name
operator|+
literal|".db"
argument_list|)
decl_stmt|;
name|String
name|databaseLocation
init|=
literal|"raw://"
operator|+
name|databaseFolder
operator|.
name|toURI
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|database
operator|.
name|setLocationUri
argument_list|(
name|databaseLocation
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Database
name|dropAndCreate
parameter_list|(
name|IMetaStoreClient
name|metaStoreClient
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|metaStoreClient
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
block|}
try|try
block|{
for|for
control|(
name|String
name|table
range|:
name|metaStoreClient
operator|.
name|listTableNamesByFilter
argument_list|(
name|database
operator|.
name|getName
argument_list|()
argument_list|,
literal|""
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
control|)
block|{
name|metaStoreClient
operator|.
name|dropTable
argument_list|(
name|database
operator|.
name|getName
argument_list|()
argument_list|,
name|table
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|metaStoreClient
operator|.
name|dropDatabase
argument_list|(
name|database
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{       }
name|metaStoreClient
operator|.
name|createDatabase
argument_list|(
name|database
argument_list|)
expr_stmt|;
return|return
name|database
return|;
block|}
specifier|public
name|Database
name|build
parameter_list|()
block|{
return|return
name|database
return|;
block|}
block|}
specifier|public
specifier|static
name|TableBuilder
name|tableBuilder
parameter_list|(
name|Database
name|database
parameter_list|)
block|{
return|return
operator|new
name|TableBuilder
argument_list|(
name|database
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|TableBuilder
block|{
specifier|private
name|Table
name|table
decl_stmt|;
specifier|private
name|StorageDescriptor
name|sd
decl_stmt|;
specifier|private
name|SerDeInfo
name|serDeInfo
decl_stmt|;
specifier|private
name|Database
name|database
decl_stmt|;
specifier|private
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|partitions
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|columnTypes
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|partitionKeys
decl_stmt|;
specifier|public
name|TableBuilder
parameter_list|(
name|Database
name|database
parameter_list|)
block|{
name|this
operator|.
name|database
operator|=
name|database
expr_stmt|;
name|partitions
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|columnNames
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|columnTypes
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|partitionKeys
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
name|table
operator|=
operator|new
name|Table
argument_list|()
expr_stmt|;
name|table
operator|.
name|setDbName
argument_list|(
name|database
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|setTableType
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableParams
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
name|tableParams
operator|.
name|put
argument_list|(
literal|"transactional"
argument_list|,
name|Boolean
operator|.
name|TRUE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|setParameters
argument_list|(
name|tableParams
argument_list|)
expr_stmt|;
name|sd
operator|=
operator|new
name|StorageDescriptor
argument_list|()
expr_stmt|;
name|sd
operator|.
name|setInputFormat
argument_list|(
name|OrcInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setOutputFormat
argument_list|(
name|OrcOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setNumBuckets
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|table
operator|.
name|setSd
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|serDeInfo
operator|=
operator|new
name|SerDeInfo
argument_list|()
expr_stmt|;
name|serDeInfo
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
name|serDeInfo
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|serDeInfo
operator|.
name|setSerializationLib
argument_list|(
name|OrcSerde
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setSerdeInfo
argument_list|(
name|serDeInfo
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TableBuilder
name|name
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|sd
operator|.
name|setLocation
argument_list|(
name|database
operator|.
name|getLocationUri
argument_list|()
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|name
argument_list|)
expr_stmt|;
name|table
operator|.
name|setTableName
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|serDeInfo
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|buckets
parameter_list|(
name|int
name|buckets
parameter_list|)
block|{
name|sd
operator|.
name|setNumBuckets
argument_list|(
name|buckets
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|bucketCols
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|)
block|{
name|sd
operator|.
name|setBucketCols
argument_list|(
name|columnNames
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|addColumn
parameter_list|(
name|String
name|columnName
parameter_list|,
name|String
name|columnType
parameter_list|)
block|{
name|columnNames
operator|.
name|add
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
name|columnTypes
operator|.
name|add
argument_list|(
name|columnType
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|partitionKeys
parameter_list|(
name|String
modifier|...
name|partitionKeys
parameter_list|)
block|{
name|this
operator|.
name|partitionKeys
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|partitionKeys
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|addPartition
parameter_list|(
name|String
modifier|...
name|partitionValues
parameter_list|)
block|{
name|partitions
operator|.
name|add
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|partitionValues
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TableBuilder
name|addPartition
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partitionValues
parameter_list|)
block|{
name|partitions
operator|.
name|add
argument_list|(
name|partitionValues
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Table
name|create
parameter_list|(
name|IMetaStoreClient
name|metaStoreClient
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|metaStoreClient
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|()
throw|;
block|}
return|return
name|internalCreate
argument_list|(
name|metaStoreClient
argument_list|)
return|;
block|}
specifier|public
name|Table
name|build
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|internalCreate
argument_list|(
literal|null
argument_list|)
return|;
block|}
specifier|private
name|Table
name|internalCreate
parameter_list|(
name|IMetaStoreClient
name|metaStoreClient
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
name|columnNames
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
name|columnNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|fields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
name|columnNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|columnTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|sd
operator|.
name|setCols
argument_list|(
name|fields
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|partitionKeys
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partitionFields
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|partitionKey
range|:
name|partitionKeys
control|)
block|{
name|partitionFields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
name|partitionKey
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|setPartitionKeys
argument_list|(
name|partitionFields
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|metaStoreClient
operator|!=
literal|null
condition|)
block|{
name|metaStoreClient
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|List
argument_list|<
name|String
argument_list|>
name|partitionValues
range|:
name|partitions
control|)
block|{
name|Partition
name|partition
init|=
operator|new
name|Partition
argument_list|()
decl_stmt|;
name|partition
operator|.
name|setDbName
argument_list|(
name|database
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|partition
operator|.
name|setTableName
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|partitionSd
init|=
operator|new
name|StorageDescriptor
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
argument_list|)
decl_stmt|;
name|partitionSd
operator|.
name|setLocation
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|Warehouse
operator|.
name|makePartName
argument_list|(
name|table
operator|.
name|getPartitionKeys
argument_list|()
argument_list|,
name|partitionValues
argument_list|)
argument_list|)
expr_stmt|;
name|partition
operator|.
name|setSd
argument_list|(
name|partitionSd
argument_list|)
expr_stmt|;
name|partition
operator|.
name|setValues
argument_list|(
name|partitionValues
argument_list|)
expr_stmt|;
if|if
condition|(
name|metaStoreClient
operator|!=
literal|null
condition|)
block|{
name|metaStoreClient
operator|.
name|add_partition
argument_list|(
name|partition
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|table
return|;
block|}
block|}
block|}
end_class

end_unit

