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
package|;
end_package

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
name|List
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
name|common
operator|.
name|ndv
operator|.
name|hll
operator|.
name|HyperLogLog
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
name|annotation
operator|.
name|MetastoreUnitTest
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
name|Catalog
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
name|ColumnStatistics
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
name|ColumnStatisticsDesc
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
name|FileMetadataExprType
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
name|InvalidInputException
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
name|LongColumnStatsData
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
name|client
operator|.
name|builder
operator|.
name|DatabaseBuilder
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
name|io
operator|.
name|sarg
operator|.
name|SearchArgument
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
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
import|import static
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
operator|.
name|DEFAULT_CATALOG_NAME
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|MetastoreUnitTest
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestOldSchema
block|{
specifier|private
name|ObjectStore
name|store
init|=
literal|null
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
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
name|TestOldSchema
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
class|class
name|MockPartitionExpressionProxy
implements|implements
name|PartitionExpressionProxy
block|{
annotation|@
name|Override
specifier|public
name|String
name|convertExprToFilter
parameter_list|(
name|byte
index|[]
name|expr
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterPartitionsByExpr
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partColumns
parameter_list|,
name|byte
index|[]
name|expr
parameter_list|,
name|String
name|defaultPartitionName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partitionNames
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileMetadataExprType
name|getMetadataType
parameter_list|(
name|String
name|inputFormat
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|SearchArgument
name|createSarg
parameter_list|(
name|byte
index|[]
name|expr
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileFormatProxy
name|getFileFormatProxy
parameter_list|(
name|FileMetadataExprType
name|type
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|byte
name|bitVectors
index|[]
index|[]
init|=
operator|new
name|byte
index|[
literal|2
index|]
index|[]
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
expr_stmt|;
name|MetastoreConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|STATS_FETCH_BITVECTOR
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|MetaStoreTestUtils
operator|.
name|setConfForStandloneMode
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|store
operator|=
operator|new
name|ObjectStore
argument_list|()
expr_stmt|;
name|store
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|dropAllStoreObjects
argument_list|(
name|store
argument_list|)
expr_stmt|;
name|HiveMetaStore
operator|.
name|HMSHandler
operator|.
name|createDefaultCatalog
argument_list|(
name|store
argument_list|,
operator|new
name|Warehouse
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|HyperLogLog
name|hll
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|hll
operator|.
name|addLong
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|bitVectors
index|[
literal|1
index|]
operator|=
name|hll
operator|.
name|serialize
argument_list|()
expr_stmt|;
name|hll
operator|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
expr_stmt|;
name|hll
operator|.
name|addLong
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|hll
operator|.
name|addLong
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|hll
operator|.
name|addLong
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|hll
operator|.
name|addLong
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|bitVectors
index|[
literal|0
index|]
operator|=
name|hll
operator|.
name|serialize
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{   }
comment|/**    * Tests partition operations    */
annotation|@
name|Test
specifier|public
name|void
name|testPartitionOps
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|dbName
init|=
literal|"default"
decl_stmt|;
name|String
name|tableName
init|=
literal|"snp"
decl_stmt|;
name|Database
name|db1
init|=
operator|new
name|DatabaseBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|dbName
argument_list|)
operator|.
name|setDescription
argument_list|(
literal|"description"
argument_list|)
operator|.
name|setLocation
argument_list|(
literal|"locationurl"
argument_list|)
operator|.
name|build
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|store
operator|.
name|createDatabase
argument_list|(
name|db1
argument_list|)
expr_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
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
name|cols
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"col1"
argument_list|,
literal|"long"
argument_list|,
literal|"nocomment"
argument_list|)
argument_list|)
expr_stmt|;
name|SerDeInfo
name|serde
init|=
operator|new
name|SerDeInfo
argument_list|(
literal|"serde"
argument_list|,
literal|"seriallib"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|(
name|cols
argument_list|,
literal|"file:/tmp"
argument_list|,
literal|"input"
argument_list|,
literal|"output"
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
name|serde
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|partCols
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"ds"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
operator|new
name|Table
argument_list|(
name|tableName
argument_list|,
name|dbName
argument_list|,
literal|"me"
argument_list|,
operator|(
name|int
operator|)
name|now
argument_list|,
operator|(
name|int
operator|)
name|now
argument_list|,
literal|0
argument_list|,
name|sd
argument_list|,
name|partCols
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|store
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|Deadline
operator|.
name|startTimer
argument_list|(
literal|"getPartition"
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|partVal
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|partVal
operator|.
name|add
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|psd
init|=
operator|new
name|StorageDescriptor
argument_list|(
name|sd
argument_list|)
decl_stmt|;
name|psd
operator|.
name|setLocation
argument_list|(
literal|"file:/tmp/default/hit/ds="
operator|+
name|partVal
argument_list|)
expr_stmt|;
name|Partition
name|part
init|=
operator|new
name|Partition
argument_list|(
name|partVal
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
operator|(
name|int
operator|)
name|now
argument_list|,
operator|(
name|int
operator|)
name|now
argument_list|,
name|psd
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|part
operator|.
name|setCatName
argument_list|(
name|DEFAULT_CATALOG_NAME
argument_list|)
expr_stmt|;
name|store
operator|.
name|addPartition
argument_list|(
name|part
argument_list|)
expr_stmt|;
name|ColumnStatistics
name|cs
init|=
operator|new
name|ColumnStatistics
argument_list|()
decl_stmt|;
name|ColumnStatisticsDesc
name|desc
init|=
operator|new
name|ColumnStatisticsDesc
argument_list|(
literal|false
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|setLastAnalyzed
argument_list|(
name|now
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setPartName
argument_list|(
literal|"ds="
operator|+
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|cs
operator|.
name|setStatsDesc
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|ColumnStatisticsObj
name|obj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|obj
operator|.
name|setColName
argument_list|(
literal|"col1"
argument_list|)
expr_stmt|;
name|obj
operator|.
name|setColType
argument_list|(
literal|"bigint"
argument_list|)
expr_stmt|;
name|ColumnStatisticsData
name|data
init|=
operator|new
name|ColumnStatisticsData
argument_list|()
decl_stmt|;
name|LongColumnStatsData
name|dcsd
init|=
operator|new
name|LongColumnStatsData
argument_list|()
decl_stmt|;
name|dcsd
operator|.
name|setHighValue
argument_list|(
literal|1000
operator|+
name|i
argument_list|)
expr_stmt|;
name|dcsd
operator|.
name|setLowValue
argument_list|(
operator|-
literal|1000
operator|-
name|i
argument_list|)
expr_stmt|;
name|dcsd
operator|.
name|setNumNulls
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|dcsd
operator|.
name|setNumDVs
argument_list|(
literal|10
operator|*
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
name|dcsd
operator|.
name|setBitVectors
argument_list|(
name|bitVectors
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|data
operator|.
name|setLongStats
argument_list|(
name|dcsd
argument_list|)
expr_stmt|;
name|obj
operator|.
name|setStatsData
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|cs
operator|.
name|addToStatsObj
argument_list|(
name|obj
argument_list|)
expr_stmt|;
name|store
operator|.
name|updatePartitionColumnStatistics
argument_list|(
name|cs
argument_list|,
name|partVal
argument_list|)
expr_stmt|;
block|}
name|Checker
name|statChecker
init|=
operator|new
name|Checker
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|checkStats
parameter_list|(
name|AggrStats
name|aggrStats
parameter_list|)
throws|throws
name|Exception
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|aggrStats
operator|.
name|getPartsFound
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aggrStats
operator|.
name|getColStatsSize
argument_list|()
argument_list|)
expr_stmt|;
name|ColumnStatisticsObj
name|cso
init|=
name|aggrStats
operator|.
name|getColStats
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"col1"
argument_list|,
name|cso
operator|.
name|getColName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"bigint"
argument_list|,
name|cso
operator|.
name|getColType
argument_list|()
argument_list|)
expr_stmt|;
name|LongColumnStatsData
name|lcsd
init|=
name|cso
operator|.
name|getStatsData
argument_list|()
operator|.
name|getLongStats
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1009
argument_list|,
name|lcsd
operator|.
name|getHighValue
argument_list|()
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|-
literal|1009
argument_list|,
name|lcsd
operator|.
name|getLowValue
argument_list|()
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|45
argument_list|,
name|lcsd
operator|.
name|getNumNulls
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|91
argument_list|,
name|lcsd
operator|.
name|getNumDVs
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|partNames
operator|.
name|add
argument_list|(
literal|"ds="
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|AggrStats
name|aggrStats
init|=
name|store
operator|.
name|get_aggr_stats_for
argument_list|(
name|DEFAULT_CATALOG_NAME
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partNames
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"col1"
argument_list|)
argument_list|)
decl_stmt|;
name|statChecker
operator|.
name|checkStats
argument_list|(
name|aggrStats
argument_list|)
expr_stmt|;
block|}
specifier|private
interface|interface
name|Checker
block|{
name|void
name|checkStats
parameter_list|(
name|AggrStats
name|aggrStats
parameter_list|)
throws|throws
name|Exception
function_decl|;
block|}
specifier|private
specifier|static
name|void
name|dropAllStoreObjects
parameter_list|(
name|RawStore
name|store
parameter_list|)
throws|throws
name|MetaException
throws|,
name|InvalidObjectException
throws|,
name|InvalidInputException
block|{
try|try
block|{
name|Deadline
operator|.
name|registerIfNot
argument_list|(
literal|100000
argument_list|)
expr_stmt|;
name|Deadline
operator|.
name|startTimer
argument_list|(
literal|"getPartition"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|dbs
init|=
name|store
operator|.
name|getAllDatabases
argument_list|(
name|DEFAULT_CATALOG_NAME
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
name|dbs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|db
init|=
name|dbs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|tbls
init|=
name|store
operator|.
name|getAllTables
argument_list|(
name|DEFAULT_CATALOG_NAME
argument_list|,
name|db
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|tbl
range|:
name|tbls
control|)
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
init|=
name|store
operator|.
name|getPartitions
argument_list|(
name|DEFAULT_CATALOG_NAME
argument_list|,
name|db
argument_list|,
name|tbl
argument_list|,
literal|100
argument_list|)
decl_stmt|;
for|for
control|(
name|Partition
name|part
range|:
name|parts
control|)
block|{
name|store
operator|.
name|dropPartition
argument_list|(
name|DEFAULT_CATALOG_NAME
argument_list|,
name|db
argument_list|,
name|tbl
argument_list|,
name|part
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|store
operator|.
name|dropTable
argument_list|(
name|DEFAULT_CATALOG_NAME
argument_list|,
name|db
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
block|}
name|store
operator|.
name|dropDatabase
argument_list|(
name|DEFAULT_CATALOG_NAME
argument_list|,
name|db
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{     }
block|}
block|}
end_class

end_unit

