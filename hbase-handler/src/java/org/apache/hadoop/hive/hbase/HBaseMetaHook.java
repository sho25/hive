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
name|hbase
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
name|io
operator|.
name|IOUtils
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
name|hbase
operator|.
name|HColumnDescriptor
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
name|hbase
operator|.
name|HTableDescriptor
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
name|hbase
operator|.
name|TableName
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
name|hbase
operator|.
name|client
operator|.
name|Admin
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
name|hbase
operator|.
name|client
operator|.
name|Connection
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
name|hbase
operator|.
name|client
operator|.
name|ConnectionFactory
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
name|hbase
operator|.
name|util
operator|.
name|Bytes
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
name|HiveMetaHook
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
name|util
operator|.
name|StringUtils
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
name|java
operator|.
name|io
operator|.
name|Closeable
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

begin_comment
comment|/**  * MetaHook for HBase. Updates the table data in HBase too. Not thread safe, and cleanup should  * be used after usage.  */
end_comment

begin_class
specifier|public
class|class
name|HBaseMetaHook
implements|implements
name|HiveMetaHook
implements|,
name|Closeable
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
name|HBaseMetaHook
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|hbaseConf
decl_stmt|;
specifier|private
name|Admin
name|admin
decl_stmt|;
specifier|public
name|HBaseMetaHook
parameter_list|(
name|Configuration
name|hbaseConf
parameter_list|)
block|{
name|this
operator|.
name|hbaseConf
operator|=
name|hbaseConf
expr_stmt|;
block|}
specifier|private
name|Admin
name|getHBaseAdmin
parameter_list|()
throws|throws
name|MetaException
block|{
try|try
block|{
if|if
condition|(
name|admin
operator|==
literal|null
condition|)
block|{
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|hbaseConf
argument_list|)
decl_stmt|;
name|admin
operator|=
name|conn
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
block|}
return|return
name|admin
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|ioe
argument_list|)
argument_list|)
throw|;
block|}
block|}
specifier|private
name|String
name|getHBaseTableName
parameter_list|(
name|Table
name|tbl
parameter_list|)
block|{
comment|// Give preference to TBLPROPERTIES over SERDEPROPERTIES
comment|// (really we should only use TBLPROPERTIES, so this is just
comment|// for backwards compatibility with the original specs).
name|String
name|tableName
init|=
name|tbl
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_TABLE_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableName
operator|==
literal|null
condition|)
block|{
comment|//convert to lower case in case we are getting from serde
name|tableName
operator|=
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_TABLE_NAME
argument_list|)
expr_stmt|;
comment|//standardize to lower case
if|if
condition|(
name|tableName
operator|!=
literal|null
condition|)
block|{
name|tableName
operator|=
name|tableName
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|tableName
operator|==
literal|null
condition|)
block|{
name|tableName
operator|=
operator|(
name|tbl
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|tbl
operator|.
name|getTableName
argument_list|()
operator|)
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
if|if
condition|(
name|tableName
operator|.
name|startsWith
argument_list|(
name|HBaseStorageHandler
operator|.
name|DEFAULT_PREFIX
argument_list|)
condition|)
block|{
name|tableName
operator|=
name|tableName
operator|.
name|substring
argument_list|(
name|HBaseStorageHandler
operator|.
name|DEFAULT_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|tableName
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preDropTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
block|{
comment|// nothing to do
block|}
annotation|@
name|Override
specifier|public
name|void
name|rollbackDropTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
block|{
comment|// nothing to do
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitDropTable
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|boolean
name|deleteData
parameter_list|)
throws|throws
name|MetaException
block|{
try|try
block|{
name|String
name|tableName
init|=
name|getHBaseTableName
argument_list|(
name|tbl
argument_list|)
decl_stmt|;
name|boolean
name|isPurge
init|=
operator|!
name|MetaStoreUtils
operator|.
name|isExternalTable
argument_list|(
name|tbl
argument_list|)
operator|||
name|MetaStoreUtils
operator|.
name|isExternalTablePurge
argument_list|(
name|tbl
argument_list|)
decl_stmt|;
if|if
condition|(
name|deleteData
operator|&&
name|isPurge
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Dropping with purge all the data for data source {}"
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|getHBaseAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
condition|)
block|{
if|if
condition|(
name|getHBaseAdmin
argument_list|()
operator|.
name|isTableEnabled
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
condition|)
block|{
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|getHBaseAdmin
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|ie
argument_list|)
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|preCreateTable
parameter_list|(
name|Table
name|tbl
parameter_list|)
throws|throws
name|MetaException
block|{
comment|// We'd like to move this to HiveMetaStore for any non-native table, but
comment|// first we need to support storing NULL for location on a table
if|if
condition|(
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"LOCATION may not be specified for HBase."
argument_list|)
throw|;
block|}
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Table
name|htable
init|=
literal|null
decl_stmt|;
try|try
block|{
name|String
name|tableName
init|=
name|getHBaseTableName
argument_list|(
name|tbl
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serdeParam
init|=
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getParameters
argument_list|()
decl_stmt|;
name|String
name|hbaseColumnsMapping
init|=
name|serdeParam
operator|.
name|get
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_COLUMNS_MAPPING
argument_list|)
decl_stmt|;
name|ColumnMappings
name|columnMappings
init|=
name|HBaseSerDe
operator|.
name|parseColumnsMapping
argument_list|(
name|hbaseColumnsMapping
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|tableDesc
decl_stmt|;
if|if
condition|(
operator|!
name|getHBaseAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
condition|)
block|{
comment|// create table from Hive
comment|// create the column descriptors
name|tableDesc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|uniqueColumnFamilies
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ColumnMappings
operator|.
name|ColumnMapping
name|colMap
range|:
name|columnMappings
control|)
block|{
if|if
condition|(
operator|!
name|colMap
operator|.
name|hbaseRowKey
operator|&&
operator|!
name|colMap
operator|.
name|hbaseTimestamp
condition|)
block|{
name|uniqueColumnFamilies
operator|.
name|add
argument_list|(
name|colMap
operator|.
name|familyName
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|String
name|columnFamily
range|:
name|uniqueColumnFamilies
control|)
block|{
name|tableDesc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|columnFamily
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|tableDesc
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// register table in Hive
comment|// make sure the schema mapping is right
name|tableDesc
operator|=
name|getHBaseAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|ColumnMappings
operator|.
name|ColumnMapping
name|colMap
range|:
name|columnMappings
control|)
block|{
if|if
condition|(
name|colMap
operator|.
name|hbaseRowKey
operator|||
name|colMap
operator|.
name|hbaseTimestamp
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|!
name|tableDesc
operator|.
name|hasFamily
argument_list|(
name|colMap
operator|.
name|familyNameBytes
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Column Family "
operator|+
name|colMap
operator|.
name|familyName
operator|+
literal|" is not defined in hbase table "
operator|+
name|tableName
argument_list|)
throw|;
block|}
block|}
block|}
comment|// ensure the table is online
name|htable
operator|=
name|getHBaseAdmin
argument_list|()
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableDesc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|se
parameter_list|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|se
argument_list|)
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|htable
operator|!=
literal|null
condition|)
block|{
name|IOUtils
operator|.
name|closeQuietly
argument_list|(
name|htable
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|rollbackCreateTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
block|{
name|String
name|tableName
init|=
name|getHBaseTableName
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|boolean
name|isPurge
init|=
operator|!
name|MetaStoreUtils
operator|.
name|isExternalTable
argument_list|(
name|table
argument_list|)
operator|||
name|MetaStoreUtils
operator|.
name|isExternalTablePurge
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|isPurge
operator|&&
name|getHBaseAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
condition|)
block|{
comment|// we have created an HBase table, so we delete it to roll back;
if|if
condition|(
name|getHBaseAdmin
argument_list|()
operator|.
name|isTableEnabled
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
condition|)
block|{
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|getHBaseAdmin
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|ie
argument_list|)
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitCreateTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
block|{
comment|// nothing to do
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|admin
operator|!=
literal|null
condition|)
block|{
name|Connection
name|connection
init|=
name|admin
operator|.
name|getConnection
argument_list|()
decl_stmt|;
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
name|admin
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|connection
operator|!=
literal|null
condition|)
block|{
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

