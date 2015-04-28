begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|AggregateStatsCache
operator|.
name|AggrColStats
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
name|AggregateStatsCache
operator|.
name|Key
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|BloomFilter
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
name|AfterClass
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
name|BeforeClass
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

begin_class
specifier|public
class|class
name|TestAggregateStatsCache
block|{
specifier|static
name|String
name|DB_NAME
init|=
literal|"db"
decl_stmt|;
specifier|static
name|String
name|TAB_PREFIX
init|=
literal|"tab"
decl_stmt|;
specifier|static
name|String
name|PART_PREFIX
init|=
literal|"part"
decl_stmt|;
specifier|static
name|String
name|COL_PREFIX
init|=
literal|"col"
decl_stmt|;
specifier|static
name|int
name|NUM_TABS
init|=
literal|2
decl_stmt|;
specifier|static
name|int
name|NUM_PARTS
init|=
literal|20
decl_stmt|;
specifier|static
name|int
name|NUM_COLS
init|=
literal|5
decl_stmt|;
specifier|static
name|int
name|MAX_CACHE_NODES
init|=
literal|10
decl_stmt|;
specifier|static
name|int
name|MAX_PARTITIONS_PER_CACHE_NODE
init|=
literal|10
decl_stmt|;
specifier|static
name|String
name|TIME_TO_LIVE
init|=
literal|"20s"
decl_stmt|;
specifier|static
name|String
name|MAX_WRITER_WAIT
init|=
literal|"1s"
decl_stmt|;
specifier|static
name|String
name|MAX_READER_WAIT
init|=
literal|"1s"
decl_stmt|;
specifier|static
name|float
name|FALSE_POSITIVE_PROBABILITY
init|=
operator|(
name|float
operator|)
literal|0.01
decl_stmt|;
specifier|static
name|float
name|MAX_VARIANCE
init|=
operator|(
name|float
operator|)
literal|0.5
decl_stmt|;
specifier|static
name|AggregateStatsCache
name|cache
decl_stmt|;
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|tables
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|tabParts
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|tabCols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeTest
parameter_list|()
block|{
comment|// All data intitializations
name|initializeTables
argument_list|()
expr_stmt|;
name|initializePartitions
argument_list|()
expr_stmt|;
name|initializeColumns
argument_list|()
expr_stmt|;
block|}
comment|// tab1, tab2
specifier|private
specifier|static
name|void
name|initializeTables
parameter_list|()
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|NUM_TABS
condition|;
name|i
operator|++
control|)
block|{
name|tables
operator|.
name|add
argument_list|(
name|TAB_PREFIX
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
block|}
comment|// part1 ... part20
specifier|private
specifier|static
name|void
name|initializePartitions
parameter_list|()
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|NUM_PARTS
condition|;
name|i
operator|++
control|)
block|{
name|tabParts
operator|.
name|add
argument_list|(
name|PART_PREFIX
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
block|}
comment|// col1 ... col5
specifier|private
specifier|static
name|void
name|initializeColumns
parameter_list|()
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|NUM_COLS
condition|;
name|i
operator|++
control|)
block|{
name|tabCols
operator|.
name|add
argument_list|(
name|COL_PREFIX
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterTest
parameter_list|()
block|{   }
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_SIZE
argument_list|,
name|MAX_CACHE_NODES
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_PARTITIONS
argument_list|,
name|MAX_PARTITIONS_PER_CACHE_NODE
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setFloatVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_FPP
argument_list|,
name|FALSE_POSITIVE_PROBABILITY
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setFloatVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_VARIANCE
argument_list|,
name|MAX_VARIANCE
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_TTL
argument_list|,
name|TIME_TO_LIVE
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_WRITER_WAIT
argument_list|,
name|MAX_WRITER_WAIT
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AGGREGATE_STATS_CACHE_MAX_READER_WAIT
argument_list|,
name|MAX_READER_WAIT
argument_list|)
expr_stmt|;
name|cache
operator|=
name|AggregateStatsCache
operator|.
name|getInstance
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
block|{   }
annotation|@
name|Test
specifier|public
name|void
name|testCacheKey
parameter_list|()
block|{
name|Key
name|k1
init|=
operator|new
name|Key
argument_list|(
literal|"db"
argument_list|,
literal|"tbl1"
argument_list|,
literal|"col"
argument_list|)
decl_stmt|;
name|Key
name|k2
init|=
operator|new
name|Key
argument_list|(
literal|"db"
argument_list|,
literal|"tbl1"
argument_list|,
literal|"col"
argument_list|)
decl_stmt|;
comment|// k1 equals k2
name|Assert
operator|.
name|assertEquals
argument_list|(
name|k1
argument_list|,
name|k2
argument_list|)
expr_stmt|;
name|Key
name|k3
init|=
operator|new
name|Key
argument_list|(
literal|"db"
argument_list|,
literal|"tbl2"
argument_list|,
literal|"col"
argument_list|)
decl_stmt|;
comment|// k1 not equals k3
name|Assert
operator|.
name|assertNotEquals
argument_list|(
name|k1
argument_list|,
name|k3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasicAddAndGet
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Partnames: [tab1part1...tab1part9]
name|List
argument_list|<
name|String
argument_list|>
name|partNames
init|=
name|preparePartNames
argument_list|(
name|tables
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|1
argument_list|,
literal|9
argument_list|)
decl_stmt|;
comment|// Prepare the bloom filter
name|BloomFilter
name|bloomFilter
init|=
name|prepareBloomFilter
argument_list|(
name|partNames
argument_list|)
decl_stmt|;
comment|// Add a dummy aggregate stats object for the above parts (part1...part9) of tab1 for col1
name|String
name|tblName
init|=
name|tables
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|colName
init|=
name|tabCols
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|int
name|highVal
init|=
literal|100
decl_stmt|,
name|lowVal
init|=
literal|10
decl_stmt|,
name|numDVs
init|=
literal|50
decl_stmt|,
name|numNulls
init|=
literal|5
decl_stmt|;
comment|// We'll treat this as the aggregate col stats for part1...part9 of tab1, col1
name|ColumnStatisticsObj
name|aggrColStats
init|=
name|getDummyLongColStat
argument_list|(
name|colName
argument_list|,
name|highVal
argument_list|,
name|lowVal
argument_list|,
name|numDVs
argument_list|,
name|numNulls
argument_list|)
decl_stmt|;
comment|// Now add to cache the dummy colstats for these 10 partitions
name|cache
operator|.
name|add
argument_list|(
name|DB_NAME
argument_list|,
name|tblName
argument_list|,
name|colName
argument_list|,
literal|10
argument_list|,
name|aggrColStats
argument_list|,
name|bloomFilter
argument_list|)
expr_stmt|;
comment|// Now get from cache
name|AggrColStats
name|aggrStatsCached
init|=
name|cache
operator|.
name|get
argument_list|(
name|DB_NAME
argument_list|,
name|tblName
argument_list|,
name|colName
argument_list|,
name|partNames
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|aggrStatsCached
argument_list|)
expr_stmt|;
name|ColumnStatisticsObj
name|aggrColStatsCached
init|=
name|aggrStatsCached
operator|.
name|getColStats
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|aggrColStats
argument_list|,
name|aggrColStatsCached
argument_list|)
expr_stmt|;
comment|// Now get a non-existant entry
name|aggrStatsCached
operator|=
name|cache
operator|.
name|get
argument_list|(
literal|"dbNotThere"
argument_list|,
name|tblName
argument_list|,
name|colName
argument_list|,
name|partNames
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|aggrStatsCached
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAddGetWithVariance
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Partnames: [tab1part1...tab1part9]
name|List
argument_list|<
name|String
argument_list|>
name|partNames
init|=
name|preparePartNames
argument_list|(
name|tables
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|1
argument_list|,
literal|9
argument_list|)
decl_stmt|;
comment|// Prepare the bloom filter
name|BloomFilter
name|bloomFilter
init|=
name|prepareBloomFilter
argument_list|(
name|partNames
argument_list|)
decl_stmt|;
comment|// Add a dummy aggregate stats object for the above parts (part1...part9) of tab1 for col1
name|String
name|tblName
init|=
name|tables
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|colName
init|=
name|tabCols
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|int
name|highVal
init|=
literal|100
decl_stmt|,
name|lowVal
init|=
literal|10
decl_stmt|,
name|numDVs
init|=
literal|50
decl_stmt|,
name|numNulls
init|=
literal|5
decl_stmt|;
comment|// We'll treat this as the aggregate col stats for part1...part9 of tab1, col1
name|ColumnStatisticsObj
name|aggrColStats
init|=
name|getDummyLongColStat
argument_list|(
name|colName
argument_list|,
name|highVal
argument_list|,
name|lowVal
argument_list|,
name|numDVs
argument_list|,
name|numNulls
argument_list|)
decl_stmt|;
comment|// Now add to cache
name|cache
operator|.
name|add
argument_list|(
name|DB_NAME
argument_list|,
name|tblName
argument_list|,
name|colName
argument_list|,
literal|10
argument_list|,
name|aggrColStats
argument_list|,
name|bloomFilter
argument_list|)
expr_stmt|;
comment|// Now prepare partnames with only 5 partitions: [tab1part1...tab1part5]
name|partNames
operator|=
name|preparePartNames
argument_list|(
name|tables
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|1
argument_list|,
literal|5
argument_list|)
expr_stmt|;
comment|// This get should fail because its variance ((10-5)/5) is way past MAX_VARIANCE (0.5)
name|AggrColStats
name|aggrStatsCached
init|=
name|cache
operator|.
name|get
argument_list|(
name|DB_NAME
argument_list|,
name|tblName
argument_list|,
name|colName
argument_list|,
name|partNames
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|aggrStatsCached
argument_list|)
expr_stmt|;
comment|// Now prepare partnames with 10 partitions: [tab1part11...tab1part20], but with no overlap
name|partNames
operator|=
name|preparePartNames
argument_list|(
name|tables
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|11
argument_list|,
literal|20
argument_list|)
expr_stmt|;
comment|// This get should fail because its variance ((10-0)/10) is way past MAX_VARIANCE (0.5)
name|aggrStatsCached
operator|=
name|cache
operator|.
name|get
argument_list|(
name|DB_NAME
argument_list|,
name|tblName
argument_list|,
name|colName
argument_list|,
name|partNames
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|aggrStatsCached
argument_list|)
expr_stmt|;
comment|// Now prepare partnames with 9 partitions: [tab1part1...tab1part8], which are contained in the
comment|// object that we added to the cache
name|partNames
operator|=
name|preparePartNames
argument_list|(
name|tables
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|1
argument_list|,
literal|8
argument_list|)
expr_stmt|;
comment|// This get should succeed because its variance ((10-9)/9) is within past MAX_VARIANCE (0.5)
name|aggrStatsCached
operator|=
name|cache
operator|.
name|get
argument_list|(
name|DB_NAME
argument_list|,
name|tblName
argument_list|,
name|colName
argument_list|,
name|partNames
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|aggrStatsCached
argument_list|)
expr_stmt|;
name|ColumnStatisticsObj
name|aggrColStatsCached
init|=
name|aggrStatsCached
operator|.
name|getColStats
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|aggrColStats
argument_list|,
name|aggrColStatsCached
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimeToLive
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Add a dummy node to cache
comment|// Partnames: [tab1part1...tab1part9]
name|List
argument_list|<
name|String
argument_list|>
name|partNames
init|=
name|preparePartNames
argument_list|(
name|tables
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|1
argument_list|,
literal|9
argument_list|)
decl_stmt|;
comment|// Prepare the bloom filter
name|BloomFilter
name|bloomFilter
init|=
name|prepareBloomFilter
argument_list|(
name|partNames
argument_list|)
decl_stmt|;
comment|// Add a dummy aggregate stats object for the above parts (part1...part9) of tab1 for col1
name|String
name|tblName
init|=
name|tables
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|colName
init|=
name|tabCols
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|int
name|highVal
init|=
literal|100
decl_stmt|,
name|lowVal
init|=
literal|10
decl_stmt|,
name|numDVs
init|=
literal|50
decl_stmt|,
name|numNulls
init|=
literal|5
decl_stmt|;
comment|// We'll treat this as the aggregate col stats for part1...part9 of tab1, col1
name|ColumnStatisticsObj
name|aggrColStats
init|=
name|getDummyLongColStat
argument_list|(
name|colName
argument_list|,
name|highVal
argument_list|,
name|lowVal
argument_list|,
name|numDVs
argument_list|,
name|numNulls
argument_list|)
decl_stmt|;
comment|// Now add to cache
name|cache
operator|.
name|add
argument_list|(
name|DB_NAME
argument_list|,
name|tblName
argument_list|,
name|colName
argument_list|,
literal|10
argument_list|,
name|aggrColStats
argument_list|,
name|bloomFilter
argument_list|)
expr_stmt|;
comment|// Sleep for 30 seconds
name|Thread
operator|.
name|sleep
argument_list|(
literal|30000
argument_list|)
expr_stmt|;
comment|// Get should fail now (since TTL is 20s) and we've snoozed for 30 seconds
name|AggrColStats
name|aggrStatsCached
init|=
name|cache
operator|.
name|get
argument_list|(
name|DB_NAME
argument_list|,
name|tblName
argument_list|,
name|colName
argument_list|,
name|partNames
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|aggrStatsCached
argument_list|)
expr_stmt|;
block|}
comment|/**    * Prepares an array of partition names by getting partitions from minPart ... maxPart and    * prepending with table name    * Example: [tab1part1, tab1part2 ...]    *    * @param tabName    * @param minPart    * @param maxPart    * @return    * @throws Exception    */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|preparePartNames
parameter_list|(
name|String
name|tabName
parameter_list|,
name|int
name|minPart
parameter_list|,
name|int
name|maxPart
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
operator|(
name|minPart
operator|<
literal|1
operator|)
operator|||
operator|(
name|maxPart
operator|>
name|NUM_PARTS
operator|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"tabParts does not have these partition numbers"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|partNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|minPart
init|;
name|i
operator|<=
name|maxPart
condition|;
name|i
operator|++
control|)
block|{
name|String
name|partName
init|=
name|tabParts
operator|.
name|get
argument_list|(
name|i
operator|-
literal|1
argument_list|)
decl_stmt|;
name|partNames
operator|.
name|add
argument_list|(
name|tabName
operator|+
name|partName
argument_list|)
expr_stmt|;
block|}
return|return
name|partNames
return|;
block|}
comment|/**    * Prepares a bloom filter from the list of partition names    * @param partNames    * @return    */
specifier|private
name|BloomFilter
name|prepareBloomFilter
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partNames
parameter_list|)
block|{
name|BloomFilter
name|bloomFilter
init|=
operator|new
name|BloomFilter
argument_list|(
name|MAX_PARTITIONS_PER_CACHE_NODE
argument_list|,
name|FALSE_POSITIVE_PROBABILITY
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|partName
range|:
name|partNames
control|)
block|{
name|bloomFilter
operator|.
name|add
argument_list|(
name|partName
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|bloomFilter
return|;
block|}
specifier|private
name|ColumnStatisticsObj
name|getDummyLongColStat
parameter_list|(
name|String
name|colName
parameter_list|,
name|int
name|highVal
parameter_list|,
name|int
name|lowVal
parameter_list|,
name|int
name|numDVs
parameter_list|,
name|int
name|numNulls
parameter_list|)
block|{
name|ColumnStatisticsObj
name|aggrColStats
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|aggrColStats
operator|.
name|setColName
argument_list|(
name|colName
argument_list|)
expr_stmt|;
name|aggrColStats
operator|.
name|setColType
argument_list|(
literal|"long"
argument_list|)
expr_stmt|;
name|LongColumnStatsData
name|longStatsData
init|=
operator|new
name|LongColumnStatsData
argument_list|()
decl_stmt|;
name|longStatsData
operator|.
name|setHighValue
argument_list|(
name|highVal
argument_list|)
expr_stmt|;
name|longStatsData
operator|.
name|setLowValue
argument_list|(
name|lowVal
argument_list|)
expr_stmt|;
name|longStatsData
operator|.
name|setNumDVs
argument_list|(
name|numDVs
argument_list|)
expr_stmt|;
name|longStatsData
operator|.
name|setNumNulls
argument_list|(
name|numNulls
argument_list|)
expr_stmt|;
name|ColumnStatisticsData
name|aggrColStatsData
init|=
operator|new
name|ColumnStatisticsData
argument_list|()
decl_stmt|;
name|aggrColStatsData
operator|.
name|setLongStats
argument_list|(
name|longStatsData
argument_list|)
expr_stmt|;
name|aggrColStats
operator|.
name|setStatsData
argument_list|(
name|aggrColStatsData
argument_list|)
expr_stmt|;
return|return
name|aggrColStats
return|;
block|}
block|}
end_class

end_unit

