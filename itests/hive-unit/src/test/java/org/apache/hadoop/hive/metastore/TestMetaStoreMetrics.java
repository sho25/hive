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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|cli
operator|.
name|CliSessionState
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
name|metrics
operator|.
name|Metrics
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
name|metrics
operator|.
name|MetricsConstants
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
name|DriverFactory
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
name|IDriver
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

begin_comment
comment|/**  * Tests Hive Metastore Metrics.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestMetaStoreMetrics
block|{
specifier|private
specifier|static
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|static
name|IDriver
name|driver
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|TestMetaStoreMetrics
operator|.
name|class
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
name|METASTORETHRIFTCONNECTIONRETRIES
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_METRICS
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
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
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|)
expr_stmt|;
comment|//Increments one HMS connection
name|MetaStoreTestUtils
operator|.
name|startMetaStoreWithRetry
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
comment|//Increments one HMS connection (Hive.get())
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|driver
operator|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMethodCounts
parameter_list|()
throws|throws
name|Exception
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"show databases"
argument_list|)
expr_stmt|;
comment|//one call by init, one called here.
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getTimers
argument_list|()
operator|.
name|get
argument_list|(
literal|"api_get_databases"
argument_list|)
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMetaDataCounts
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|initDbCount
init|=
operator|(
name|Integer
operator|)
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getGauges
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|TOTAL_DATABASES
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|int
name|initTblCount
init|=
operator|(
name|Integer
operator|)
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getGauges
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|TOTAL_TABLES
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|int
name|initPartCount
init|=
operator|(
name|Integer
operator|)
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getGauges
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|TOTAL_PARTITIONS
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|//1 databases created
name|driver
operator|.
name|run
argument_list|(
literal|"create database testdb1"
argument_list|)
expr_stmt|;
comment|//4 tables
name|driver
operator|.
name|run
argument_list|(
literal|"create table testtbl1 (key string)"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create table testtblpart (key string) partitioned by (partkey string)"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"use testdb1"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create table testtbl2 (key string)"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create table testtblpart2 (key string) partitioned by (partkey string)"
argument_list|)
expr_stmt|;
comment|//6 partitions
name|driver
operator|.
name|run
argument_list|(
literal|"alter table default.testtblpart add partition (partkey='a')"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table default.testtblpart add partition (partkey='b')"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table default.testtblpart add partition (partkey='c')"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table testdb1.testtblpart2 add partition (partkey='a')"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table testdb1.testtblpart2 add partition (partkey='b')"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table testdb1.testtblpart2 add partition (partkey='c')"
argument_list|)
expr_stmt|;
comment|//create and drop some additional metadata, to test drop counts.
name|driver
operator|.
name|run
argument_list|(
literal|"create database tempdb"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"use tempdb"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create table delete_by_table (key string) partitioned by (partkey string)"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table delete_by_table add partition (partkey='temp')"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop table delete_by_table"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create table delete_by_part (key string) partitioned by (partkey string)"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table delete_by_part add partition (partkey='temp')"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table delete_by_part drop partition (partkey='temp')"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create table delete_by_db (key string) partitioned by (partkey string)"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table delete_by_db add partition (partkey='temp')"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"use default"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop database tempdb cascade"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getCounters
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|CREATE_TOTAL_DATABASES
argument_list|)
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|7
argument_list|,
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getCounters
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|CREATE_TOTAL_TABLES
argument_list|)
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|9
argument_list|,
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getCounters
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|CREATE_TOTAL_PARTITIONS
argument_list|)
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getCounters
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|DELETE_TOTAL_DATABASES
argument_list|)
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getCounters
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|DELETE_TOTAL_TABLES
argument_list|)
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getCounters
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|DELETE_TOTAL_PARTITIONS
argument_list|)
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
comment|//to test initial metadata count metrics.
name|Assert
operator|.
name|assertEquals
argument_list|(
name|initDbCount
operator|+
literal|1
argument_list|,
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getGauges
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|TOTAL_DATABASES
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|initTblCount
operator|+
literal|4
argument_list|,
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getGauges
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|TOTAL_TABLES
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|initPartCount
operator|+
literal|6
argument_list|,
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getGauges
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|TOTAL_PARTITIONS
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConnections
parameter_list|()
throws|throws
name|Exception
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
comment|// TODO Evil!  Need to figure out a way to remove this sleep.
comment|//initial state is one connection
name|int
name|initialCount
init|=
operator|(
name|Integer
operator|)
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getGauges
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|OPEN_CONNECTIONS
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|//create two connections
name|HiveMetaStoreClient
name|msc
init|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|HiveMetaStoreClient
name|msc2
init|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|initialCount
operator|+
literal|2
argument_list|,
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getGauges
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|OPEN_CONNECTIONS
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
comment|//close one connection, verify still two left
name|msc
operator|.
name|close
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
comment|// TODO Evil!  Need to figure out a way to remove this sleep.
name|Assert
operator|.
name|assertEquals
argument_list|(
name|initialCount
operator|+
literal|1
argument_list|,
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getGauges
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|OPEN_CONNECTIONS
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
comment|//close one connection, verify still one left
name|msc2
operator|.
name|close
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
comment|// TODO Evil!  Need to figure out a way to remove this sleep.
name|Assert
operator|.
name|assertEquals
argument_list|(
name|initialCount
argument_list|,
name|Metrics
operator|.
name|getRegistry
argument_list|()
operator|.
name|getGauges
argument_list|()
operator|.
name|get
argument_list|(
name|MetricsConstants
operator|.
name|OPEN_CONNECTIONS
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

