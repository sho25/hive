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
name|lang
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
name|CheckConstraint
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
name|DefaultConstraint
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
name|ForeignKeyInfo
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
name|NotNullConstraint
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
name|PrimaryKeyInfo
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
name|StorageHandlerInfo
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
name|metadata
operator|.
name|UniqueConstraint
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|IOUtils
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
name|String
name|colPath
init|=
name|desc
operator|.
name|getColumnPath
argument_list|()
decl_stmt|;
name|String
name|tableName
init|=
name|desc
operator|.
name|getTableName
argument_list|()
decl_stmt|;
comment|// describe the table - populate the output stream
name|Table
name|tbl
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|tbl
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
name|tableName
argument_list|)
throw|;
block|}
name|Partition
name|part
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|getPartSpec
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
name|tbl
argument_list|,
name|desc
operator|.
name|getPartSpec
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
name|getPartSpec
argument_list|()
operator|.
name|keySet
argument_list|()
argument_list|,
literal|','
argument_list|)
argument_list|,
name|tableName
argument_list|)
throw|;
block|}
name|tbl
operator|=
name|part
operator|.
name|getTable
argument_list|()
expr_stmt|;
block|}
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
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"DDLTask: got data for {}"
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|colStats
init|=
literal|null
decl_stmt|;
name|Deserializer
name|deserializer
init|=
name|tbl
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
name|errorMsgs
operator|!=
literal|null
operator|&&
operator|!
name|errorMsgs
operator|.
name|isEmpty
argument_list|()
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
if|if
condition|(
name|colPath
operator|.
name|equals
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|cols
operator|=
operator|(
name|part
operator|==
literal|null
operator|||
name|tbl
operator|.
name|getTableType
argument_list|()
operator|==
name|TableType
operator|.
name|VIRTUAL_VIEW
operator|)
condition|?
name|tbl
operator|.
name|getCols
argument_list|()
else|:
name|part
operator|.
name|getCols
argument_list|()
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
name|tbl
operator|.
name|getPartCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tbl
operator|.
name|isPartitioned
argument_list|()
operator|&&
name|part
operator|==
literal|null
condition|)
block|{
comment|// No partitioned specified for partitioned table, lets fetch all.
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
init|=
name|tbl
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
name|tbl
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
name|tbl
argument_list|,
literal|null
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_BATCH_RETRIEVE_MAX
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
name|partition
range|:
name|parts
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
init|=
name|partition
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
name|props
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
name|props
operator|!=
literal|null
operator|&&
name|props
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
name|props
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
name|tbl
operator|.
name|setParameters
argument_list|(
name|tblProps
argument_list|)
expr_stmt|;
block|}
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
comment|// when column name is specified in describe table DDL, colPath will
comment|// will be table_name.column_name
name|String
name|colName
init|=
name|colPath
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
index|[
literal|1
index|]
decl_stmt|;
name|String
index|[]
name|dbTab
init|=
name|Utilities
operator|.
name|getDbTableName
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|colNames
operator|.
name|add
argument_list|(
name|colName
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
literal|null
operator|==
name|part
condition|)
block|{
if|if
condition|(
name|tbl
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
name|tblProps
init|=
name|tbl
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
name|tbl
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|tbl
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
name|FieldSchema
name|partCol
init|=
name|tbl
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
operator|=
name|Collections
operator|.
name|singletonList
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
name|tbl
argument_list|,
literal|null
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_BATCH_RETRIEVE_MAX
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
operator|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|cso
argument_list|)
expr_stmt|;
name|StatsSetupConst
operator|.
name|setColumnStatsState
argument_list|(
name|tblProps
argument_list|,
name|colNames
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cols
operator|=
name|Hive
operator|.
name|getFieldsFromDeserializer
argument_list|(
name|colPath
argument_list|,
name|deserializer
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
operator|=
name|aggrStats
operator|.
name|getColStats
argument_list|()
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
name|tblProps
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
name|tblProps
argument_list|,
name|colNames
argument_list|)
expr_stmt|;
block|}
block|}
name|tbl
operator|.
name|setParameters
argument_list|(
name|tblProps
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cols
operator|=
name|Hive
operator|.
name|getFieldsFromDeserializer
argument_list|(
name|colPath
argument_list|,
name|deserializer
argument_list|)
expr_stmt|;
name|colStats
operator|=
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
operator|=
name|Hive
operator|.
name|getFieldsFromDeserializer
argument_list|(
name|colPath
argument_list|,
name|deserializer
argument_list|)
expr_stmt|;
name|colStats
operator|=
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
expr_stmt|;
block|}
block|}
else|else
block|{
name|cols
operator|=
name|Hive
operator|.
name|getFieldsFromDeserializer
argument_list|(
name|colPath
argument_list|,
name|deserializer
argument_list|)
expr_stmt|;
block|}
block|}
name|PrimaryKeyInfo
name|pkInfo
init|=
literal|null
decl_stmt|;
name|ForeignKeyInfo
name|fkInfo
init|=
literal|null
decl_stmt|;
name|UniqueConstraint
name|ukInfo
init|=
literal|null
decl_stmt|;
name|NotNullConstraint
name|nnInfo
init|=
literal|null
decl_stmt|;
name|DefaultConstraint
name|dInfo
init|=
literal|null
decl_stmt|;
name|CheckConstraint
name|cInfo
init|=
literal|null
decl_stmt|;
name|StorageHandlerInfo
name|storageHandlerInfo
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|isExt
argument_list|()
operator|||
name|desc
operator|.
name|isFormatted
argument_list|()
condition|)
block|{
name|pkInfo
operator|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getPrimaryKeys
argument_list|(
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|fkInfo
operator|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getForeignKeys
argument_list|(
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|ukInfo
operator|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getUniqueConstraints
argument_list|(
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|nnInfo
operator|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getNotNullConstraints
argument_list|(
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|dInfo
operator|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getDefaultConstraints
argument_list|(
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|cInfo
operator|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getCheckConstraints
argument_list|(
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|storageHandlerInfo
operator|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getStorageHandlerInfo
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
block|}
name|fixDecimalColumnTypeName
argument_list|(
name|cols
argument_list|)
expr_stmt|;
comment|// Information for materialized views
if|if
condition|(
name|tbl
operator|.
name|isMaterializedView
argument_list|()
condition|)
block|{
specifier|final
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
name|tbl
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
name|tbl
operator|.
name|setOutdatedForRewriting
argument_list|(
name|Hive
operator|.
name|isOutdatedMaterializedView
argument_list|(
name|tbl
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
name|colPath
argument_list|,
name|tableName
argument_list|,
name|tbl
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
name|isExt
argument_list|()
argument_list|,
name|isOutputPadded
argument_list|,
name|colStats
argument_list|,
name|pkInfo
argument_list|,
name|fkInfo
argument_list|,
name|ukInfo
argument_list|,
name|nnInfo
argument_list|,
name|dInfo
argument_list|,
name|cInfo
argument_list|,
name|storageHandlerInfo
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"DDLTask: written data for {}"
argument_list|,
name|tableName
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
name|tableName
argument_list|)
throw|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|outStream
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
return|;
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
block|}
end_class

end_unit

