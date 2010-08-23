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
name|hbase
package|;
end_package

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
name|ArrayList
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
name|Properties
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
name|HBaseConfiguration
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
name|MasterNotRunningException
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
name|HBaseAdmin
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
name|HTable
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
name|ql
operator|.
name|metadata
operator|.
name|HiveStorageHandler
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
name|plan
operator|.
name|TableDesc
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
name|serde2
operator|.
name|SerDe
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
name|serde2
operator|.
name|SerDeException
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
name|mapred
operator|.
name|InputFormat
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
name|mapred
operator|.
name|OutputFormat
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

begin_comment
comment|/**  * HBaseStorageHandler provides a HiveStorageHandler implementation for  * HBase.  */
end_comment

begin_class
specifier|public
class|class
name|HBaseStorageHandler
implements|implements
name|HiveStorageHandler
implements|,
name|HiveMetaHook
block|{
specifier|private
name|HBaseConfiguration
name|hbaseConf
decl_stmt|;
specifier|private
name|HBaseAdmin
name|admin
decl_stmt|;
specifier|private
name|HBaseAdmin
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
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|hbaseConf
argument_list|)
expr_stmt|;
block|}
return|return
name|admin
return|;
block|}
catch|catch
parameter_list|(
name|MasterNotRunningException
name|mnre
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
name|mnre
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
name|tbl
operator|.
name|getTableName
argument_list|()
expr_stmt|;
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
name|isExternal
init|=
name|MetaStoreUtils
operator|.
name|isExternalTable
argument_list|(
name|tbl
argument_list|)
decl_stmt|;
if|if
condition|(
name|deleteData
operator|&&
operator|!
name|isExternal
condition|)
block|{
if|if
condition|(
name|getHBaseAdmin
argument_list|()
operator|.
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|getHBaseAdmin
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|tableName
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
name|preCreateTable
parameter_list|(
name|Table
name|tbl
parameter_list|)
throws|throws
name|MetaException
block|{
name|boolean
name|isExternal
init|=
name|MetaStoreUtils
operator|.
name|isExternalTable
argument_list|(
name|tbl
argument_list|)
decl_stmt|;
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
if|if
condition|(
name|hbaseColumnsMapping
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"No hbase.columns.mapping defined in Serde."
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|hbaseColumnFamilies
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|hbaseColumnQualifiers
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|hbaseColumnFamiliesBytes
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|hbaseColumnQualifiersBytes
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|iKey
init|=
name|HBaseSerDe
operator|.
name|parseColumnMapping
argument_list|(
name|hbaseColumnsMapping
argument_list|,
name|hbaseColumnFamilies
argument_list|,
name|hbaseColumnFamiliesBytes
argument_list|,
name|hbaseColumnQualifiers
argument_list|,
name|hbaseColumnQualifiersBytes
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
name|tableName
argument_list|)
condition|)
block|{
comment|// if it is not an external table then create one
if|if
condition|(
operator|!
name|isExternal
condition|)
block|{
comment|// Create the column descriptors
name|tableDesc
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
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
argument_list|(
name|hbaseColumnFamilies
argument_list|)
decl_stmt|;
name|uniqueColumnFamilies
operator|.
name|remove
argument_list|(
name|hbaseColumnFamilies
operator|.
name|get
argument_list|(
name|iKey
argument_list|)
argument_list|)
expr_stmt|;
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
comment|// an external table
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"HBase table "
operator|+
name|tableName
operator|+
literal|" doesn't exist while the table is declared as an external table."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|isExternal
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Table "
operator|+
name|tableName
operator|+
literal|" already exists"
operator|+
literal|" within HBase; use CREATE EXTERNAL TABLE instead to"
operator|+
literal|" register it in Hive."
argument_list|)
throw|;
block|}
comment|// make sure the schema mapping is right
name|tableDesc
operator|=
name|getHBaseAdmin
argument_list|()
operator|.
name|getTableDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
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
name|hbaseColumnFamilies
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
name|i
operator|==
name|iKey
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
name|hbaseColumnFamiliesBytes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Column Family "
operator|+
name|hbaseColumnFamilies
operator|.
name|get
argument_list|(
name|i
argument_list|)
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
operator|new
name|HTable
argument_list|(
name|hbaseConf
argument_list|,
name|tableDesc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MasterNotRunningException
name|mnre
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
name|mnre
argument_list|)
argument_list|)
throw|;
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
catch|catch
parameter_list|(
name|SerDeException
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
name|boolean
name|isExternal
init|=
name|MetaStoreUtils
operator|.
name|isExternalTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|String
name|tableName
init|=
name|getHBaseTableName
argument_list|(
name|table
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|isExternal
operator|&&
name|getHBaseAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
name|tableName
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
name|tableName
argument_list|)
condition|)
block|{
name|getHBaseAdmin
argument_list|()
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|getHBaseAdmin
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|tableName
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
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|hbaseConf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|hbaseConf
operator|=
operator|new
name|HBaseConfiguration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|getInputFormatClass
parameter_list|()
block|{
return|return
name|HiveHBaseTableInputFormat
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|getOutputFormatClass
parameter_list|()
block|{
return|return
name|HiveHBaseTableOutputFormat
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|SerDe
argument_list|>
name|getSerDeClass
parameter_list|()
block|{
return|return
name|HBaseSerDe
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveMetaHook
name|getMetaHook
parameter_list|()
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|configureTableJobProperties
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|)
block|{
name|Properties
name|tableProperties
init|=
name|tableDesc
operator|.
name|getProperties
argument_list|()
decl_stmt|;
name|jobProperties
operator|.
name|put
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_COLUMNS_MAPPING
argument_list|,
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_COLUMNS_MAPPING
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
name|tableProperties
operator|.
name|getProperty
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
name|tableName
operator|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
name|jobProperties
operator|.
name|put
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_TABLE_NAME
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

