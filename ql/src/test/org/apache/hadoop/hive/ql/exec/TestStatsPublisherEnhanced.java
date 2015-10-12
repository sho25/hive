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
name|ql
operator|.
name|exec
package|;
end_package

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
name|Map
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|ql
operator|.
name|stats
operator|.
name|StatsAggregator
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
name|StatsCollectionContext
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
name|StatsFactory
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
name|StatsPublisher
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

begin_comment
comment|/**  * TestPublisher jdbc.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestStatsPublisherEnhanced
extends|extends
name|TestCase
block|{
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|String
name|statsImplementationClass
decl_stmt|;
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|stats
decl_stmt|;
specifier|protected
name|StatsFactory
name|factory
decl_stmt|;
specifier|public
name|TestStatsPublisherEnhanced
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|conf
operator|=
operator|new
name|JobConf
argument_list|(
name|TestStatsPublisherEnhanced
operator|.
name|class
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hive.stats.dbclass"
argument_list|,
literal|"jdbc:derby"
argument_list|)
expr_stmt|;
name|factory
operator|=
name|StatsFactory
operator|.
name|newFactory
argument_list|(
name|conf
argument_list|)
expr_stmt|;
assert|assert
name|factory
operator|!=
literal|null
assert|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
block|{
name|stats
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
block|{
name|StatsAggregator
name|sa
init|=
name|factory
operator|.
name|getStatsAggregator
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|sa
argument_list|)
expr_stmt|;
name|StatsCollectionContext
name|sc
init|=
operator|new
name|StatsCollectionContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|sa
operator|.
name|connect
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|sa
operator|.
name|cleanUp
argument_list|(
literal|"file_0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|sa
operator|.
name|closeConnection
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|fillStatMap
parameter_list|(
name|String
name|numRows
parameter_list|,
name|String
name|rawDataSize
parameter_list|)
block|{
name|stats
operator|.
name|clear
argument_list|()
expr_stmt|;
name|stats
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|,
name|numRows
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|rawDataSize
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|stats
operator|.
name|put
argument_list|(
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|,
name|rawDataSize
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testStatsPublisherOneStat
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"StatsPublisher - one stat published per key - aggregating matching key"
argument_list|)
expr_stmt|;
comment|// instantiate stats publisher
name|StatsPublisher
name|statsPublisher
init|=
name|Utilities
operator|.
name|getStatsPublisher
argument_list|(
operator|(
name|JobConf
operator|)
name|conf
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|statsPublisher
argument_list|)
expr_stmt|;
name|StatsCollectionContext
name|sc
init|=
operator|new
name|StatsCollectionContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|init
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|connect
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
comment|// instantiate stats aggregator
name|StatsAggregator
name|statsAggregator
init|=
name|factory
operator|.
name|getStatsAggregator
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|statsAggregator
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|connect
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
comment|// publish stats
name|fillStatMap
argument_list|(
literal|"200"
argument_list|,
literal|"1000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"400"
argument_list|,
literal|"3000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00001"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
comment|// aggregate existing stats
name|String
name|rows0
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00000"
argument_list|,
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"200"
argument_list|,
name|rows0
argument_list|)
expr_stmt|;
name|String
name|usize0
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00000"
argument_list|,
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1000"
argument_list|,
name|usize0
argument_list|)
expr_stmt|;
name|String
name|rows1
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00001"
argument_list|,
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"400"
argument_list|,
name|rows1
argument_list|)
expr_stmt|;
name|String
name|usize1
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00001"
argument_list|,
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"3000"
argument_list|,
name|usize1
argument_list|)
expr_stmt|;
comment|// close connections
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|closeConnection
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|closeConnection
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"StatsPublisher - one stat published per key - aggregating matching key - OK"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|public
name|void
name|testStatsPublisher
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"StatsPublisher - basic functionality"
argument_list|)
expr_stmt|;
comment|// instantiate stats publisher
name|StatsPublisher
name|statsPublisher
init|=
name|Utilities
operator|.
name|getStatsPublisher
argument_list|(
operator|(
name|JobConf
operator|)
name|conf
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|statsPublisher
argument_list|)
expr_stmt|;
name|StatsCollectionContext
name|sc
init|=
operator|new
name|StatsCollectionContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|init
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|connect
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
comment|// instantiate stats aggregator
name|StatsAggregator
name|statsAggregator
init|=
name|factory
operator|.
name|getStatsAggregator
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|statsAggregator
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|connect
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
comment|// statsAggregator.cleanUp("file_0000");
comment|// assertTrue(statsAggregator.connect(conf));
comment|// publish stats
name|fillStatMap
argument_list|(
literal|"200"
argument_list|,
literal|"1000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000_a"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"300"
argument_list|,
literal|"2000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000_b"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"400"
argument_list|,
literal|"3000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00001_a"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"500"
argument_list|,
literal|"4000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00001_b"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
comment|// aggregate existing stats
name|String
name|rows0
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00000"
argument_list|,
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"500"
argument_list|,
name|rows0
argument_list|)
expr_stmt|;
name|String
name|usize0
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00000"
argument_list|,
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"3000"
argument_list|,
name|usize0
argument_list|)
expr_stmt|;
name|String
name|rows1
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00001"
argument_list|,
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"900"
argument_list|,
name|rows1
argument_list|)
expr_stmt|;
name|String
name|usize1
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00001"
argument_list|,
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"7000"
argument_list|,
name|usize1
argument_list|)
expr_stmt|;
comment|// aggregate non-existent stats
name|String
name|rowsX
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00002"
argument_list|,
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"0"
argument_list|,
name|rowsX
argument_list|)
expr_stmt|;
name|String
name|usizeX
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00002"
argument_list|,
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"0"
argument_list|,
name|usizeX
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|cleanUp
argument_list|(
literal|"file_0000"
argument_list|)
argument_list|)
expr_stmt|;
comment|// close connections
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|closeConnection
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|closeConnection
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"StatsPublisher - basic functionality - OK"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|public
name|void
name|testStatsPublisherMultipleUpdates
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"StatsPublisher - multiple updates"
argument_list|)
expr_stmt|;
comment|// instantiate stats publisher
name|StatsPublisher
name|statsPublisher
init|=
name|Utilities
operator|.
name|getStatsPublisher
argument_list|(
operator|(
name|JobConf
operator|)
name|conf
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|statsPublisher
argument_list|)
expr_stmt|;
name|StatsCollectionContext
name|sc
init|=
operator|new
name|StatsCollectionContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|init
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|connect
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
comment|// instantiate stats aggregator
name|StatsAggregator
name|statsAggregator
init|=
name|factory
operator|.
name|getStatsAggregator
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|statsAggregator
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|connect
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
comment|// publish stats
name|fillStatMap
argument_list|(
literal|"200"
argument_list|,
literal|"1000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000_a"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"300"
argument_list|,
literal|"2000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000_b"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"400"
argument_list|,
literal|"3000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00001_a"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"500"
argument_list|,
literal|"4000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00001_b"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
comment|// update which should not take any effect
name|fillStatMap
argument_list|(
literal|"190"
argument_list|,
literal|"1000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000_a"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"290"
argument_list|,
literal|"2000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000_b"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
comment|// update that should take effect
name|fillStatMap
argument_list|(
literal|"500"
argument_list|,
literal|"5000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00001_a"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"600"
argument_list|,
literal|"6000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00001_b"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
comment|// aggregate existing stats
name|String
name|rows0
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00000"
argument_list|,
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"500"
argument_list|,
name|rows0
argument_list|)
expr_stmt|;
name|String
name|usize0
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00000"
argument_list|,
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"3000"
argument_list|,
name|usize0
argument_list|)
expr_stmt|;
name|String
name|rows1
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00001"
argument_list|,
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1100"
argument_list|,
name|rows1
argument_list|)
expr_stmt|;
name|String
name|usize1
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00001"
argument_list|,
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"11000"
argument_list|,
name|usize1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|cleanUp
argument_list|(
literal|"file_0000"
argument_list|)
argument_list|)
expr_stmt|;
comment|// close connections
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|closeConnection
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|closeConnection
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"StatsPublisher - multiple updates - OK"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|public
name|void
name|testStatsPublisherMultipleUpdatesSubsetStatistics
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"StatsPublisher - (multiple updates + publishing subset of supported statistics)"
argument_list|)
expr_stmt|;
comment|// instantiate stats publisher
name|StatsPublisher
name|statsPublisher
init|=
name|Utilities
operator|.
name|getStatsPublisher
argument_list|(
operator|(
name|JobConf
operator|)
name|conf
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|statsPublisher
argument_list|)
expr_stmt|;
name|StatsCollectionContext
name|sc
init|=
operator|new
name|StatsCollectionContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|init
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|connect
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
comment|// instantiate stats aggregator
name|StatsAggregator
name|statsAggregator
init|=
name|factory
operator|.
name|getStatsAggregator
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|statsAggregator
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|connect
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
comment|// publish stats
name|fillStatMap
argument_list|(
literal|"200"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000_a"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"300"
argument_list|,
literal|"2000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000_b"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
comment|// aggregate existing stats
name|String
name|rows0
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00000"
argument_list|,
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"500"
argument_list|,
name|rows0
argument_list|)
expr_stmt|;
name|String
name|usize0
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00000"
argument_list|,
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2000"
argument_list|,
name|usize0
argument_list|)
expr_stmt|;
comment|// update which should not take any effect - plus the map published is a supset of supported
comment|// stats
name|fillStatMap
argument_list|(
literal|"190"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000_a"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"290"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000_b"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
comment|// nothing changed
name|rows0
operator|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00000"
argument_list|,
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"500"
argument_list|,
name|rows0
argument_list|)
expr_stmt|;
name|usize0
operator|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00000"
argument_list|,
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"2000"
argument_list|,
name|usize0
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"500"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000_a"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"500"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000_b"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
comment|// changed + the rawDataSize size was overwriten !!!
name|rows0
operator|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00000"
argument_list|,
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1000"
argument_list|,
name|rows0
argument_list|)
expr_stmt|;
name|usize0
operator|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00000"
argument_list|,
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"0"
argument_list|,
name|usize0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|cleanUp
argument_list|(
literal|"file_0000"
argument_list|)
argument_list|)
expr_stmt|;
comment|// close connections
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|closeConnection
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|closeConnection
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"StatsPublisher - (multiple updates + publishing subset of supported statistics) - OK"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|public
name|void
name|testStatsAggregatorCleanUp
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"StatsAggregator - clean-up"
argument_list|)
expr_stmt|;
comment|// instantiate stats publisher
name|StatsPublisher
name|statsPublisher
init|=
name|Utilities
operator|.
name|getStatsPublisher
argument_list|(
operator|(
name|JobConf
operator|)
name|conf
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|statsPublisher
argument_list|)
expr_stmt|;
name|StatsCollectionContext
name|sc
init|=
operator|new
name|StatsCollectionContext
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|init
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|connect
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
comment|// instantiate stats aggregator
name|StatsAggregator
name|statsAggregator
init|=
name|factory
operator|.
name|getStatsAggregator
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|statsAggregator
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|connect
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
comment|// publish stats
name|fillStatMap
argument_list|(
literal|"200"
argument_list|,
literal|"1000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000_a"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"300"
argument_list|,
literal|"2000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00000_b"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"400"
argument_list|,
literal|"3000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00001_a"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
name|fillStatMap
argument_list|(
literal|"500"
argument_list|,
literal|"4000"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|publishStat
argument_list|(
literal|"file_00001_b"
argument_list|,
name|stats
argument_list|)
argument_list|)
expr_stmt|;
comment|// cleanUp
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|cleanUp
argument_list|(
literal|"file_00000"
argument_list|)
argument_list|)
expr_stmt|;
comment|// now clean-up just for one key
name|String
name|rows0
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00000"
argument_list|,
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"0"
argument_list|,
name|rows0
argument_list|)
expr_stmt|;
name|String
name|usize0
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00000"
argument_list|,
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"0"
argument_list|,
name|usize0
argument_list|)
expr_stmt|;
comment|// this should still be in the table
name|String
name|rows1
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00001"
argument_list|,
name|StatsSetupConst
operator|.
name|ROW_COUNT
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"900"
argument_list|,
name|rows1
argument_list|)
expr_stmt|;
name|String
name|usize1
init|=
name|statsAggregator
operator|.
name|aggregateStats
argument_list|(
literal|"file_00001"
argument_list|,
name|StatsSetupConst
operator|.
name|RAW_DATA_SIZE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"7000"
argument_list|,
name|usize1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|cleanUp
argument_list|(
literal|"file_0000"
argument_list|)
argument_list|)
expr_stmt|;
comment|// close connections
name|assertTrue
argument_list|(
name|statsPublisher
operator|.
name|closeConnection
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|statsAggregator
operator|.
name|closeConnection
argument_list|(
name|sc
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"StatsAggregator - clean-up - OK"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
end_class

end_unit

