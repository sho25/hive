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
name|ql
operator|.
name|ddl
operator|.
name|table
operator|.
name|info
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|common
operator|.
name|StatsSetupConst
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
name|ValidTxnList
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
name|ValidTxnWriteIdList
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
name|StatObjectConverter
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
name|api
operator|.
name|AggrStats
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
name|ColumnStatisticsData
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
name|ColumnStatisticsObj
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
name|ql
operator|.
name|ddl
operator|.
name|DDLOperationContext
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
name|ddl
operator|.
name|DDLUtils
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
name|exec
operator|.
name|ColumnInfo
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
name|exec
operator|.
name|Utilities
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
name|lockmgr
operator|.
name|LockException
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
name|ErrorMsg
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
name|ddl
operator|.
name|DDLOperation
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
name|Hive
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
name|HiveException
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
name|ql
operator|.
name|metadata
operator|.
name|PartitionIterable
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
name|parse
operator|.
name|SemanticException
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
name|ColStatistics
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
name|session
operator|.
name|SessionState
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
name|stats
operator|.
name|StatsUtils
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
name|hive
operator|.
name|serde2
operator|.
name|AbstractSerDe
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
name|typeinfo
operator|.
name|DecimalTypeInfo
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
name|typeinfo
operator|.
name|TypeInfoUtils
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Operation process of dropping a table.  */
end_comment

begin_class
specifier|public
class|class
name|DescTableOperation
extends|extends
name|DDLOperation
argument_list|<
name|DescTableDesc
argument_list|>
block|{
specifier|public
name|DescTableOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|DescTableDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|Exception
block|{
name|Table
name|table
init|=
name|getTable
argument_list|()
decl_stmt|;
name|Partition
name|part
init|=
name|getPartition
argument_list|(
name|table
argument_list|)
decl_stmt|;
specifier|final
name|String
name|dbTableName
init|=
name|desc
operator|.
name|getDbTableName
argument_list|()
decl_stmt|;
try|try
init|(
name|DataOutputStream
name|outStream
init|=
name|DDLUtils
operator|.
name|getOutputStream
argument_list|(
operator|new
name|Path
argument_list|(
name|desc
operator|.
name|getResFile
argument_list|()
argument_list|)
argument_list|,
name|context
argument_list|)
init|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"DDLTask: got data for {}"
argument_list|,
name|dbTableName
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|colStats
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Deserializer
name|deserializer
init|=
name|getDeserializer
argument_list|(
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|getColumnPath
argument_list|()
operator|==
literal|null
condition|)
block|{
name|getColumnsNoColumnPath
argument_list|(
name|table
argument_list|,
name|part
argument_list|,
name|cols
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|desc
operator|.
name|isFormatted
argument_list|()
condition|)
block|{
name|getColumnDataColPathSpecified
argument_list|(
name|table
argument_list|,
name|part
argument_list|,
name|cols
argument_list|,
name|colStats
argument_list|,
name|deserializer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cols
operator|.
name|addAll
argument_list|(
name|Hive
operator|.
name|getFieldsFromDeserializer
argument_list|(
name|desc
operator|.
name|getColumnPath
argument_list|()
argument_list|,
name|deserializer
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|fixDecimalColumnTypeName
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|setConstraintsAndStorageHandlerInfo
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|handleMaterializedView
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// In case the query is served by HiveServer2, don't pad it with spaces,
comment|// as HiveServer2 output is consumed by JDBC/ODBC clients.
name|boolean
name|isOutputPadded
init|=
operator|!
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|isHiveServerQuery
argument_list|()
decl_stmt|;
name|context
operator|.
name|getFormatter
argument_list|()
operator|.
name|describeTable
argument_list|(
name|outStream
argument_list|,
name|desc
operator|.
name|getColumnPath
argument_list|()
argument_list|,
name|dbTableName
argument_list|,
name|table
argument_list|,
name|part
argument_list|,
name|cols
argument_list|,
name|desc
operator|.
name|isFormatted
argument_list|()
argument_list|,
name|desc
operator|.
name|isExtended
argument_list|()
argument_list|,
name|isOutputPadded
argument_list|,
name|colStats
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"DDLTask: written data for {}"
argument_list|,
name|dbTableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|,
name|ErrorMsg
operator|.
name|GENERIC_ERROR
argument_list|,
name|dbTableName
argument_list|)
throw|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|Table
name|getTable
parameter_list|()
throws|throws
name|HiveException
block|{
name|Table
name|table
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
operator|.
name|getDb
argument_list|()
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
operator|.
name|getTable
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|table
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_TABLE
argument_list|,
name|desc
operator|.
name|getDbTableName
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|table
return|;
block|}
specifier|private
name|Partition
name|getPartition
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|HiveException
block|{
name|Partition
name|part
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|getPartitionSpec
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|part
operator|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getPartition
argument_list|(
name|table
argument_list|,
name|desc
operator|.
name|getPartitionSpec
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|part
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PARTITION
argument_list|,
name|StringUtils
operator|.
name|join
argument_list|(
name|desc
operator|.
name|getPartitionSpec
argument_list|()
operator|.
name|keySet
argument_list|()
argument_list|,
literal|','
argument_list|)
argument_list|,
name|desc
operator|.
name|getDbTableName
argument_list|()
argument_list|)
throw|;
block|}
block|}
return|return
name|part
return|;
block|}
specifier|private
name|Deserializer
name|getDeserializer
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|SQLException
block|{
name|Deserializer
name|deserializer
init|=
name|table
operator|.
name|getDeserializer
argument_list|(
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|deserializer
operator|instanceof
name|AbstractSerDe
condition|)
block|{
name|String
name|errorMsgs
init|=
operator|(
operator|(
name|AbstractSerDe
operator|)
name|deserializer
operator|)
operator|.
name|getConfigurationErrors
argument_list|()
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|errorMsgs
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
name|errorMsgs
argument_list|)
throw|;
block|}
block|}
return|return
name|deserializer
return|;
block|}
specifier|private
name|void
name|getColumnsNoColumnPath
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|partition
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|)
throws|throws
name|HiveException
block|{
name|cols
operator|.
name|addAll
argument_list|(
name|partition
operator|==
literal|null
operator|||
name|table
operator|.
name|getTableType
argument_list|()
operator|==
name|TableType
operator|.
name|VIRTUAL_VIEW
condition|?
name|table
operator|.
name|getCols
argument_list|()
else|:
name|partition
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|desc
operator|.
name|isFormatted
argument_list|()
condition|)
block|{
name|cols
operator|.
name|addAll
argument_list|(
name|table
operator|.
name|getPartCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|table
operator|.
name|isPartitioned
argument_list|()
operator|&&
name|partition
operator|==
literal|null
condition|)
block|{
comment|// No partition specified for partitioned table, lets fetch all.
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
init|=
name|table
operator|.
name|getParameters
argument_list|()
operator|==
literal|null
condition|?
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
else|:
name|table
operator|.
name|getParameters
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|valueMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|stateMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|stat
range|:
name|StatsSetupConst
operator|.
name|SUPPORTED_STATS
control|)
block|{
name|valueMap
operator|.
name|put
argument_list|(
name|stat
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|stateMap
operator|.
name|put
argument_list|(
name|stat
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|PartitionIterable
name|partitions
init|=
operator|new
name|PartitionIterable
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
argument_list|,
name|table
argument_list|,
literal|null
argument_list|,
name|MetastoreConf
operator|.
name|getIntVar
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|BATCH_RETRIEVE_MAX
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|numParts
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Partition
name|p
range|:
name|partitions
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionProps
init|=
name|p
operator|.
name|getParameters
argument_list|()
decl_stmt|;
name|Boolean
name|state
init|=
name|StatsSetupConst
operator|.
name|areBasicStatsUptoDate
argument_list|(
name|partitionProps
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|stat
range|:
name|StatsSetupConst
operator|.
name|SUPPORTED_STATS
control|)
block|{
name|stateMap
operator|.
name|put
argument_list|(
name|stat
argument_list|,
name|stateMap
operator|.
name|get
argument_list|(
name|stat
argument_list|)
operator|&&
name|state
argument_list|)
expr_stmt|;
if|if
condition|(
name|partitionProps
operator|!=
literal|null
operator|&&
name|partitionProps
operator|.
name|get
argument_list|(
name|stat
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|valueMap
operator|.
name|put
argument_list|(
name|stat
argument_list|,
name|valueMap
operator|.
name|get
argument_list|(
name|stat
argument_list|)
operator|+
name|Long
operator|.
name|parseLong
argument_list|(
name|partitionProps
operator|.
name|get
argument_list|(
name|stat
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|numParts
operator|++
expr_stmt|;
block|}
name|tblProps
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|NUM_PARTITIONS
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|numParts
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|stat
range|:
name|StatsSetupConst
operator|.
name|SUPPORTED_STATS
control|)
block|{
name|StatsSetupConst
operator|.
name|setBasicStatsState
argument_list|(
name|tblProps
argument_list|,
name|Boolean
operator|.
name|toString
argument_list|(
name|stateMap
operator|.
name|get
argument_list|(
name|stat
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tblProps
operator|.
name|put
argument_list|(
name|stat
argument_list|,
name|valueMap
operator|.
name|get
argument_list|(
name|stat
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|setParameters
argument_list|(
name|tblProps
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|getColumnDataColPathSpecified
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|part
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|,
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|colStats
parameter_list|,
name|Deserializer
name|deserializer
parameter_list|)
throws|throws
name|SemanticException
throws|,
name|HiveException
throws|,
name|MetaException
block|{
comment|// when column name is specified in describe table DDL, colPath will be db_name.table_name.column_name
name|String
name|colName
init|=
name|desc
operator|.
name|getColumnPath
argument_list|()
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
index|[
literal|2
index|]
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|colName
operator|.
name|toLowerCase
argument_list|()
argument_list|)
decl_stmt|;
name|String
index|[]
name|dbTab
init|=
name|Utilities
operator|.
name|getDbTableName
argument_list|(
name|desc
operator|.
name|getDbTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|part
condition|)
block|{
if|if
condition|(
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableProps
init|=
name|table
operator|.
name|getParameters
argument_list|()
operator|==
literal|null
condition|?
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
else|:
name|table
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|.
name|isPartitionKey
argument_list|(
name|colNames
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
condition|)
block|{
name|getColumnDataForPartitionKeyColumn
argument_list|(
name|table
argument_list|,
name|cols
argument_list|,
name|colStats
argument_list|,
name|colNames
argument_list|,
name|tableProps
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|getColumnsForNotPartitionKeyColumn
argument_list|(
name|cols
argument_list|,
name|colStats
argument_list|,
name|deserializer
argument_list|,
name|colNames
argument_list|,
name|dbTab
argument_list|,
name|tableProps
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|setParameters
argument_list|(
name|tableProps
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cols
operator|.
name|addAll
argument_list|(
name|Hive
operator|.
name|getFieldsFromDeserializer
argument_list|(
name|desc
operator|.
name|getColumnPath
argument_list|()
argument_list|,
name|deserializer
argument_list|)
argument_list|)
expr_stmt|;
name|colStats
operator|.
name|addAll
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getTableColumnStatistics
argument_list|(
name|dbTab
index|[
literal|0
index|]
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|dbTab
index|[
literal|1
index|]
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|colNames
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|List
argument_list|<
name|String
argument_list|>
name|partitions
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|partitions
operator|.
name|add
argument_list|(
name|part
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|cols
operator|.
name|addAll
argument_list|(
name|Hive
operator|.
name|getFieldsFromDeserializer
argument_list|(
name|desc
operator|.
name|getColumnPath
argument_list|()
argument_list|,
name|deserializer
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|partitionColStat
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getPartitionColumnStatistics
argument_list|(
name|dbTab
index|[
literal|0
index|]
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|dbTab
index|[
literal|1
index|]
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|partitions
argument_list|,
name|colNames
argument_list|,
literal|false
argument_list|)
operator|.
name|get
argument_list|(
name|part
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|partitionColStat
operator|!=
literal|null
condition|)
block|{
name|colStats
operator|.
name|addAll
argument_list|(
name|partitionColStat
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|getColumnDataForPartitionKeyColumn
parameter_list|(
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|,
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|colStats
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableProps
parameter_list|)
throws|throws
name|HiveException
throws|,
name|MetaException
block|{
name|FieldSchema
name|partCol
init|=
name|table
operator|.
name|getPartColByName
argument_list|(
name|colNames
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|cols
operator|.
name|add
argument_list|(
name|partCol
argument_list|)
expr_stmt|;
name|PartitionIterable
name|parts
init|=
operator|new
name|PartitionIterable
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
argument_list|,
name|table
argument_list|,
literal|null
argument_list|,
name|MetastoreConf
operator|.
name|getIntVar
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|BATCH_RETRIEVE_MAX
argument_list|)
argument_list|)
decl_stmt|;
name|ColumnInfo
name|ci
init|=
operator|new
name|ColumnInfo
argument_list|(
name|partCol
operator|.
name|getName
argument_list|()
argument_list|,
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
name|partCol
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ColStatistics
name|cs
init|=
name|StatsUtils
operator|.
name|getColStatsForPartCol
argument_list|(
name|ci
argument_list|,
name|parts
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|ColumnStatisticsData
name|data
init|=
operator|new
name|ColumnStatisticsData
argument_list|()
decl_stmt|;
name|ColStatistics
operator|.
name|Range
name|r
init|=
name|cs
operator|.
name|getRange
argument_list|()
decl_stmt|;
name|StatObjectConverter
operator|.
name|fillColumnStatisticsData
argument_list|(
name|partCol
operator|.
name|getType
argument_list|()
argument_list|,
name|data
argument_list|,
name|r
operator|==
literal|null
condition|?
literal|null
else|:
name|r
operator|.
name|minValue
argument_list|,
name|r
operator|==
literal|null
condition|?
literal|null
else|:
name|r
operator|.
name|maxValue
argument_list|,
name|r
operator|==
literal|null
condition|?
literal|null
else|:
name|r
operator|.
name|minValue
argument_list|,
name|r
operator|==
literal|null
condition|?
literal|null
else|:
name|r
operator|.
name|maxValue
argument_list|,
name|r
operator|==
literal|null
condition|?
literal|null
else|:
name|r
operator|.
name|minValue
operator|.
name|toString
argument_list|()
argument_list|,
name|r
operator|==
literal|null
condition|?
literal|null
else|:
name|r
operator|.
name|maxValue
operator|.
name|toString
argument_list|()
argument_list|,
name|cs
operator|.
name|getNumNulls
argument_list|()
argument_list|,
name|cs
operator|.
name|getCountDistint
argument_list|()
argument_list|,
literal|null
argument_list|,
name|cs
operator|.
name|getAvgColLen
argument_list|()
argument_list|,
name|cs
operator|.
name|getAvgColLen
argument_list|()
argument_list|,
name|cs
operator|.
name|getNumTrues
argument_list|()
argument_list|,
name|cs
operator|.
name|getNumFalses
argument_list|()
argument_list|)
expr_stmt|;
name|ColumnStatisticsObj
name|cso
init|=
operator|new
name|ColumnStatisticsObj
argument_list|(
name|partCol
operator|.
name|getName
argument_list|()
argument_list|,
name|partCol
operator|.
name|getType
argument_list|()
argument_list|,
name|data
argument_list|)
decl_stmt|;
name|colStats
operator|.
name|add
argument_list|(
name|cso
argument_list|)
expr_stmt|;
name|StatsSetupConst
operator|.
name|setColumnStatsState
argument_list|(
name|tableProps
argument_list|,
name|colNames
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|getColumnsForNotPartitionKeyColumn
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|,
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|colStats
parameter_list|,
name|Deserializer
name|deserializer
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|String
index|[]
name|dbTab
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tableProps
parameter_list|)
throws|throws
name|HiveException
block|{
name|cols
operator|.
name|addAll
argument_list|(
name|Hive
operator|.
name|getFieldsFromDeserializer
argument_list|(
name|desc
operator|.
name|getColumnPath
argument_list|()
argument_list|,
name|deserializer
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|parts
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getPartitionNames
argument_list|(
name|dbTab
index|[
literal|0
index|]
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|dbTab
index|[
literal|1
index|]
operator|.
name|toLowerCase
argument_list|()
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|AggrStats
name|aggrStats
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getAggrColStatsFor
argument_list|(
name|dbTab
index|[
literal|0
index|]
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|dbTab
index|[
literal|1
index|]
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|colNames
argument_list|,
name|parts
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|colStats
operator|.
name|addAll
argument_list|(
name|aggrStats
operator|.
name|getColStats
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|parts
operator|.
name|size
argument_list|()
operator|==
name|aggrStats
operator|.
name|getPartsFound
argument_list|()
condition|)
block|{
name|StatsSetupConst
operator|.
name|setColumnStatsState
argument_list|(
name|tableProps
argument_list|,
name|colNames
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|StatsSetupConst
operator|.
name|removeColumnStatsState
argument_list|(
name|tableProps
argument_list|,
name|colNames
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Fix the type name of a column of type decimal w/o precision/scale specified. This makes    * the describe table show "decimal(10,0)" instead of "decimal" even if the type stored    * in metastore is "decimal", which is possible with previous hive.    *    * @param cols columns that to be fixed as such    */
specifier|private
specifier|static
name|void
name|fixDecimalColumnTypeName
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|)
block|{
for|for
control|(
name|FieldSchema
name|col
range|:
name|cols
control|)
block|{
if|if
condition|(
name|serdeConstants
operator|.
name|DECIMAL_TYPE_NAME
operator|.
name|equals
argument_list|(
name|col
operator|.
name|getType
argument_list|()
argument_list|)
condition|)
block|{
name|col
operator|.
name|setType
argument_list|(
name|DecimalTypeInfo
operator|.
name|getQualifiedName
argument_list|(
name|HiveDecimal
operator|.
name|USER_DEFAULT_PRECISION
argument_list|,
name|HiveDecimal
operator|.
name|USER_DEFAULT_SCALE
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|setConstraintsAndStorageHandlerInfo
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|desc
operator|.
name|isExtended
argument_list|()
operator|||
name|desc
operator|.
name|isFormatted
argument_list|()
condition|)
block|{
name|table
operator|.
name|setPrimaryKeyInfo
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getPrimaryKeys
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|setForeignKeyInfo
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getForeignKeys
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|setUniqueKeyInfo
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getUniqueConstraints
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|setNotNullConstraint
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getNotNullConstraints
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|setDefaultConstraint
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getDefaultConstraints
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|setCheckConstraint
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getCheckConstraints
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|setStorageHandlerInfo
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getStorageHandlerInfo
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|handleMaterializedView
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|LockException
block|{
if|if
condition|(
name|table
operator|.
name|isMaterializedView
argument_list|()
condition|)
block|{
name|String
name|validTxnsList
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|ValidTxnList
operator|.
name|VALID_TXNS_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|validTxnsList
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|tablesUsed
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|table
operator|.
name|getCreationMetadata
argument_list|()
operator|.
name|getTablesUsed
argument_list|()
argument_list|)
decl_stmt|;
name|ValidTxnWriteIdList
name|currentTxnWriteIds
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getTxnMgr
argument_list|()
operator|.
name|getValidWriteIds
argument_list|(
name|tablesUsed
argument_list|,
name|validTxnsList
argument_list|)
decl_stmt|;
name|long
name|defaultTimeWindow
init|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_MATERIALIZED_VIEW_REWRITING_TIME_WINDOW
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|table
operator|.
name|setOutdatedForRewriting
argument_list|(
name|Hive
operator|.
name|isOutdatedMaterializedView
argument_list|(
name|table
argument_list|,
name|currentTxnWriteIds
argument_list|,
name|defaultTimeWindow
argument_list|,
name|tablesUsed
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

