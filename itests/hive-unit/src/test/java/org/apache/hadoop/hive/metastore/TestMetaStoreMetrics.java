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
name|common
operator|.
name|metrics
operator|.
name|MetricsTestUtils
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
name|metrics
operator|.
name|common
operator|.
name|MetricsConstant
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
name|metrics
operator|.
name|common
operator|.
name|MetricsFactory
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
name|metrics
operator|.
name|metrics2
operator|.
name|CodahaleMetrics
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
name|Driver
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
name|shims
operator|.
name|ShimLoader
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
name|Driver
name|driver
decl_stmt|;
specifier|private
specifier|static
name|CodahaleMetrics
name|metrics
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
name|int
name|port
init|=
name|MetaStoreUtils
operator|.
name|findFreePort
argument_list|()
decl_stmt|;
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
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|,
literal|"thrift://localhost:"
operator|+
name|port
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
name|MetaStoreUtils
operator|.
name|startMetaStore
argument_list|(
name|port
argument_list|,
name|ShimLoader
operator|.
name|getHadoopThriftAuthBridge
argument_list|()
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
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
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|metrics
operator|=
operator|(
name|CodahaleMetrics
operator|)
name|MetricsFactory
operator|.
name|getInstance
argument_list|()
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
name|String
name|json
init|=
name|metrics
operator|.
name|dumpJson
argument_list|()
decl_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|TIMER
argument_list|,
literal|"api_get_all_databases"
argument_list|,
literal|1
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
comment|//give timer thread a chance to print the metrics
name|CodahaleMetrics
name|metrics
init|=
operator|(
name|CodahaleMetrics
operator|)
name|MetricsFactory
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|String
name|json
init|=
name|metrics
operator|.
name|dumpJson
argument_list|()
decl_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
name|MetricsConstant
operator|.
name|DELTA_TOTAL_DATABASES
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
name|MetricsConstant
operator|.
name|DELTA_TOTAL_TABLES
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
name|MetricsConstant
operator|.
name|DELTA_TOTAL_PARTITIONS
argument_list|,
literal|6
argument_list|)
expr_stmt|;
comment|//to test initial metadata count metrics.
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_RAW_STORE_IMPL
argument_list|,
name|ObjectStore
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|HiveMetaStore
operator|.
name|HMSHandler
name|baseHandler
init|=
operator|new
name|HiveMetaStore
operator|.
name|HMSHandler
argument_list|(
literal|"test"
argument_list|,
name|hiveConf
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|baseHandler
operator|.
name|init
argument_list|()
expr_stmt|;
name|baseHandler
operator|.
name|updateMetrics
argument_list|()
expr_stmt|;
comment|//1 new db + default
name|json
operator|=
name|metrics
operator|.
name|dumpJson
argument_list|()
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|GAUGE
argument_list|,
name|MetricsConstant
operator|.
name|INIT_TOTAL_DATABASES
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|GAUGE
argument_list|,
name|MetricsConstant
operator|.
name|INIT_TOTAL_TABLES
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|GAUGE
argument_list|,
name|MetricsConstant
operator|.
name|INIT_TOTAL_PARTITIONS
argument_list|,
literal|6
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
comment|//initial state is one connection
name|String
name|json
init|=
name|metrics
operator|.
name|dumpJson
argument_list|()
decl_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
name|MetricsConstant
operator|.
name|OPEN_CONNECTIONS
argument_list|,
literal|1
argument_list|)
expr_stmt|;
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
name|json
operator|=
name|metrics
operator|.
name|dumpJson
argument_list|()
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
name|MetricsConstant
operator|.
name|OPEN_CONNECTIONS
argument_list|,
literal|3
argument_list|)
expr_stmt|;
comment|//close one connection, verify still two left
name|msc
operator|.
name|close
argument_list|()
expr_stmt|;
name|json
operator|=
name|metrics
operator|.
name|dumpJson
argument_list|()
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
name|MetricsConstant
operator|.
name|OPEN_CONNECTIONS
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|//close one connection, verify still one left
name|msc2
operator|.
name|close
argument_list|()
expr_stmt|;
name|json
operator|=
name|metrics
operator|.
name|dumpJson
argument_list|()
expr_stmt|;
name|MetricsTestUtils
operator|.
name|verifyMetricsJson
argument_list|(
name|json
argument_list|,
name|MetricsTestUtils
operator|.
name|COUNTER
argument_list|,
name|MetricsConstant
operator|.
name|OPEN_CONNECTIONS
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

