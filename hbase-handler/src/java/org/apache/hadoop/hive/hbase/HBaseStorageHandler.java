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
name|LinkedHashSet
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
name|mapred
operator|.
name|TableOutputFormat
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
name|mapreduce
operator|.
name|TableInputFormatBase
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
name|mapreduce
operator|.
name|TableMapReduceUtil
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
name|security
operator|.
name|User
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
name|hbase
operator|.
name|ColumnMappings
operator|.
name|ColumnMapping
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
name|api
operator|.
name|hive_metastoreConstants
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
name|index
operator|.
name|IndexPredicateAnalyzer
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
name|index
operator|.
name|IndexSearchCondition
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
name|DefaultStorageHandler
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
name|HiveStoragePredicateHandler
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
name|ExprNodeDesc
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
name|ExprNodeGenericFuncDesc
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
name|Deserializer
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
name|JobConf
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
name|mapreduce
operator|.
name|Job
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
extends|extends
name|DefaultStorageHandler
implements|implements
name|HiveMetaHook
implements|,
name|HiveStoragePredicateHandler
block|{
specifier|final
specifier|static
specifier|public
name|String
name|DEFAULT_PREFIX
init|=
literal|"default."
decl_stmt|;
comment|//Check if the configure job properties is called from input
comment|// or output for setting asymmetric properties
specifier|private
name|boolean
name|configureInputJobProps
init|=
literal|true
decl_stmt|;
specifier|private
name|Configuration
name|jobConf
decl_stmt|;
specifier|private
name|Configuration
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
name|HTable
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
argument_list|()
decl_stmt|;
for|for
control|(
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
specifier|public
name|Configuration
name|getJobConf
parameter_list|()
block|{
return|return
name|jobConf
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
name|jobConf
operator|=
name|conf
expr_stmt|;
name|hbaseConf
operator|=
name|HBaseConfiguration
operator|.
name|create
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
if|if
condition|(
name|isHBaseGenerateHFiles
argument_list|(
name|jobConf
argument_list|)
condition|)
block|{
return|return
name|HiveHFileOutputFormat
operator|.
name|class
return|;
block|}
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
name|configureInputJobProperties
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
comment|//Input
name|this
operator|.
name|configureInputJobProps
operator|=
literal|true
expr_stmt|;
name|configureTableJobProperties
argument_list|(
name|tableDesc
argument_list|,
name|jobProperties
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|configureOutputJobProperties
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
comment|//Output
name|this
operator|.
name|configureInputJobProps
operator|=
literal|false
expr_stmt|;
name|configureTableJobProperties
argument_list|(
name|tableDesc
argument_list|,
name|jobProperties
argument_list|)
expr_stmt|;
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
name|jobProperties
operator|.
name|put
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_COLUMNS_REGEX_MATCHING
argument_list|,
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_COLUMNS_REGEX_MATCHING
argument_list|,
literal|"true"
argument_list|)
argument_list|)
expr_stmt|;
name|jobProperties
operator|.
name|put
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_TABLE_DEFAULT_STORAGE_TYPE
argument_list|,
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_TABLE_DEFAULT_STORAGE_TYPE
argument_list|,
literal|"string"
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|scanCache
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_SCAN_CACHE
argument_list|)
decl_stmt|;
if|if
condition|(
name|scanCache
operator|!=
literal|null
condition|)
block|{
name|jobProperties
operator|.
name|put
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_SCAN_CACHE
argument_list|,
name|scanCache
argument_list|)
expr_stmt|;
block|}
name|String
name|scanCacheBlocks
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_SCAN_CACHEBLOCKS
argument_list|)
decl_stmt|;
if|if
condition|(
name|scanCacheBlocks
operator|!=
literal|null
condition|)
block|{
name|jobProperties
operator|.
name|put
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_SCAN_CACHEBLOCKS
argument_list|,
name|scanCacheBlocks
argument_list|)
expr_stmt|;
block|}
name|String
name|scanBatch
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_SCAN_BATCH
argument_list|)
decl_stmt|;
if|if
condition|(
name|scanBatch
operator|!=
literal|null
condition|)
block|{
name|jobProperties
operator|.
name|put
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_SCAN_BATCH
argument_list|,
name|scanBatch
argument_list|)
expr_stmt|;
block|}
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
name|hive_metastoreConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|tableName
operator|=
name|tableName
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
name|DEFAULT_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|Configuration
name|jobConf
init|=
name|getJobConf
argument_list|()
decl_stmt|;
name|addHBaseResources
argument_list|(
name|jobConf
argument_list|,
name|jobProperties
argument_list|)
expr_stmt|;
comment|// do this for reconciling HBaseStorageHandler for use in HCatalog
comment|// check to see if this an input job or an outputjob
if|if
condition|(
name|this
operator|.
name|configureInputJobProps
condition|)
block|{
for|for
control|(
name|String
name|k
range|:
name|jobProperties
operator|.
name|keySet
argument_list|()
control|)
block|{
name|jobConf
operator|.
name|set
argument_list|(
name|k
argument_list|,
name|jobProperties
operator|.
name|get
argument_list|(
name|k
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|addHBaseDelegationToken
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
block|}
comment|//try
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Error while configuring input job properties"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|//input job properties
block|}
else|else
block|{
if|if
condition|(
name|isHBaseGenerateHFiles
argument_list|(
name|jobConf
argument_list|)
condition|)
block|{
comment|// only support bulkload when a hfile.family.path has been specified.
comment|// TODO: support detecting cf's from column mapping
comment|// TODO: support loading into multiple CF's at a time
name|String
name|path
init|=
name|HiveHFileOutputFormat
operator|.
name|getFamilyPath
argument_list|(
name|jobConf
argument_list|,
name|tableProperties
argument_list|)
decl_stmt|;
if|if
condition|(
name|path
operator|==
literal|null
operator|||
name|path
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Please set "
operator|+
name|HiveHFileOutputFormat
operator|.
name|HFILE_FAMILY_PATH
operator|+
literal|" to target location for HFiles"
argument_list|)
throw|;
block|}
comment|// TODO: should call HiveHFileOutputFormat#setOutputPath
name|jobProperties
operator|.
name|put
argument_list|(
literal|"mapred.output.dir"
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|jobProperties
operator|.
name|put
argument_list|(
name|TableOutputFormat
operator|.
name|OUTPUT_TABLE
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
comment|// output job properties
block|}
comment|/**    * Return true when HBaseStorageHandler should generate hfiles instead of operate against the    * online table. This mode is implicitly applied when "hive.hbase.completebulkload" is true.    */
specifier|public
specifier|static
name|boolean
name|isHBaseGenerateHFiles
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"hive.hbase.generatehfiles"
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Utility method to add hbase-default.xml and hbase-site.xml properties to a new map    * if they are not already present in the jobConf.    * @param jobConf Job configuration    * @param newJobProperties  Map to which new properties should be added    */
specifier|private
name|void
name|addHBaseResources
parameter_list|(
name|Configuration
name|jobConf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|newJobProperties
parameter_list|)
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|HBaseConfiguration
operator|.
name|addHbaseResources
argument_list|(
name|conf
argument_list|)
expr_stmt|;
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
name|conf
control|)
block|{
if|if
condition|(
name|jobConf
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|==
literal|null
condition|)
block|{
name|newJobProperties
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|addHBaseDelegationToken
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|User
operator|.
name|isHBaseSecurityEnabled
argument_list|(
name|conf
argument_list|)
condition|)
block|{
try|try
block|{
name|User
operator|.
name|getCurrent
argument_list|()
operator|.
name|obtainAuthTokenForJob
argument_list|(
name|conf
argument_list|,
operator|new
name|Job
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Error while obtaining hbase delegation token"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|configureJobConf
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|JobConf
name|jobConf
parameter_list|)
block|{
try|try
block|{
name|HBaseSerDe
operator|.
name|configureJobConf
argument_list|(
name|tableDesc
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
comment|/*        * HIVE-6356        * The following code change is only needed for hbase-0.96.0 due to HBASE-9165, and        * will not be required once Hive bumps up its hbase version). At that time , we will        * only need TableMapReduceUtil.addDependencyJars(jobConf) here.        */
name|TableMapReduceUtil
operator|.
name|addDependencyJars
argument_list|(
name|jobConf
argument_list|,
name|HBaseStorageHandler
operator|.
name|class
argument_list|,
name|TableInputFormatBase
operator|.
name|class
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|merged
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|jobConf
operator|.
name|getStringCollection
argument_list|(
literal|"tmpjars"
argument_list|)
argument_list|)
decl_stmt|;
name|Job
name|copy
init|=
operator|new
name|Job
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|TableMapReduceUtil
operator|.
name|addDependencyJars
argument_list|(
name|copy
argument_list|)
expr_stmt|;
name|merged
operator|.
name|addAll
argument_list|(
name|copy
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getStringCollection
argument_list|(
literal|"tmpjars"
argument_list|)
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
literal|"tmpjars"
argument_list|,
name|StringUtils
operator|.
name|arrayToString
argument_list|(
name|merged
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|DecomposedPredicate
name|decomposePredicate
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Deserializer
name|deserializer
parameter_list|,
name|ExprNodeDesc
name|predicate
parameter_list|)
block|{
name|HBaseKeyFactory
name|keyFactory
init|=
operator|(
operator|(
name|HBaseSerDe
operator|)
name|deserializer
operator|)
operator|.
name|getKeyFactory
argument_list|()
decl_stmt|;
return|return
name|keyFactory
operator|.
name|decomposePredicate
argument_list|(
name|jobConf
argument_list|,
name|deserializer
argument_list|,
name|predicate
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|DecomposedPredicate
name|decomposePredicate
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|HBaseSerDe
name|hBaseSerDe
parameter_list|,
name|ExprNodeDesc
name|predicate
parameter_list|)
block|{
name|ColumnMapping
name|keyMapping
init|=
name|hBaseSerDe
operator|.
name|getHBaseSerdeParam
argument_list|()
operator|.
name|getKeyColumnMapping
argument_list|()
decl_stmt|;
name|IndexPredicateAnalyzer
name|analyzer
init|=
name|HiveHBaseTableInputFormat
operator|.
name|newIndexPredicateAnalyzer
argument_list|(
name|keyMapping
operator|.
name|columnName
argument_list|,
name|keyMapping
operator|.
name|columnType
argument_list|,
name|keyMapping
operator|.
name|binaryStorage
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|IndexSearchCondition
argument_list|>
name|searchConditions
init|=
operator|new
name|ArrayList
argument_list|<
name|IndexSearchCondition
argument_list|>
argument_list|()
decl_stmt|;
name|ExprNodeGenericFuncDesc
name|residualPredicate
init|=
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|analyzer
operator|.
name|analyzePredicate
argument_list|(
name|predicate
argument_list|,
name|searchConditions
argument_list|)
decl_stmt|;
name|int
name|scSize
init|=
name|searchConditions
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|scSize
operator|<
literal|1
operator|||
literal|2
operator|<
name|scSize
condition|)
block|{
comment|// Either there was nothing which could be pushed down (size = 0),
comment|// there were complex predicates which we don't support yet.
comment|// Currently supported are one of the form:
comment|// 1. key< 20                        (size = 1)
comment|// 2. key = 20                        (size = 1)
comment|// 3. key< 20 and key> 10           (size = 2)
return|return
literal|null
return|;
block|}
if|if
condition|(
name|scSize
operator|==
literal|2
operator|&&
operator|(
name|searchConditions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getComparisonOp
argument_list|()
operator|.
name|equals
argument_list|(
literal|"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqual"
argument_list|)
operator|||
name|searchConditions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getComparisonOp
argument_list|()
operator|.
name|equals
argument_list|(
literal|"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqual"
argument_list|)
operator|)
condition|)
block|{
comment|// If one of the predicates is =, then any other predicate with it is illegal.
return|return
literal|null
return|;
block|}
name|DecomposedPredicate
name|decomposedPredicate
init|=
operator|new
name|DecomposedPredicate
argument_list|()
decl_stmt|;
name|decomposedPredicate
operator|.
name|pushedPredicate
operator|=
name|analyzer
operator|.
name|translateSearchConditions
argument_list|(
name|searchConditions
argument_list|)
expr_stmt|;
name|decomposedPredicate
operator|.
name|residualPredicate
operator|=
name|residualPredicate
expr_stmt|;
return|return
name|decomposedPredicate
return|;
block|}
block|}
end_class

end_unit

