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
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|JsonNode
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|ObjectMapper
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
name|metrics2
operator|.
name|MetricsReporting
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
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|server
operator|.
name|HiveServer2
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Paths
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
name|File
name|workDir
init|=
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|File
name|jsonReportFile
decl_stmt|;
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
name|jsonReportFile
operator|=
operator|new
name|File
argument_list|(
name|workDir
argument_list|,
literal|"json_reporting"
argument_list|)
expr_stmt|;
name|jsonReportFile
operator|.
name|delete
argument_list|()
expr_stmt|;
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
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METRICS_REPORTER
argument_list|,
name|MetricsReporting
operator|.
name|JSON_FILE
operator|.
name|name
argument_list|()
operator|+
literal|","
operator|+
name|MetricsReporting
operator|.
name|JMX
operator|.
name|name
argument_list|()
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
name|HIVE_METRICS_JSON_FILE_LOCATION
argument_list|,
name|jsonReportFile
operator|.
name|toString
argument_list|()
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
name|HIVE_METRICS_JSON_FILE_INTERVAL
argument_list|,
literal|"100ms"
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMetricsFile
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
comment|//give timer thread a chance to print the metrics
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
comment|//As the file is being written, try a few times.
comment|//This can be replaced by CodahaleMetrics's JsonServlet reporter once it is exposed.
name|byte
index|[]
name|jsonData
init|=
name|Files
operator|.
name|readAllBytes
argument_list|(
name|Paths
operator|.
name|get
argument_list|(
name|jsonReportFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ObjectMapper
name|objectMapper
init|=
operator|new
name|ObjectMapper
argument_list|()
decl_stmt|;
name|JsonNode
name|rootNode
init|=
name|objectMapper
operator|.
name|readTree
argument_list|(
name|jsonData
argument_list|)
decl_stmt|;
name|JsonNode
name|timersNode
init|=
name|rootNode
operator|.
name|path
argument_list|(
literal|"timers"
argument_list|)
decl_stmt|;
name|JsonNode
name|methodCounterNode
init|=
name|timersNode
operator|.
name|path
argument_list|(
literal|"api_get_all_databases"
argument_list|)
decl_stmt|;
name|JsonNode
name|methodCountNode
init|=
name|methodCounterNode
operator|.
name|path
argument_list|(
literal|"count"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|methodCountNode
operator|.
name|asInt
argument_list|()
operator|>
literal|0
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
name|byte
index|[]
name|jsonData
init|=
name|Files
operator|.
name|readAllBytes
argument_list|(
name|Paths
operator|.
name|get
argument_list|(
name|jsonReportFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ObjectMapper
name|objectMapper
init|=
operator|new
name|ObjectMapper
argument_list|()
decl_stmt|;
name|JsonNode
name|rootNode
init|=
name|objectMapper
operator|.
name|readTree
argument_list|(
name|jsonData
argument_list|)
decl_stmt|;
name|JsonNode
name|countersNode
init|=
name|rootNode
operator|.
name|path
argument_list|(
literal|"counters"
argument_list|)
decl_stmt|;
name|JsonNode
name|openCnxNode
init|=
name|countersNode
operator|.
name|path
argument_list|(
literal|"open_connections"
argument_list|)
decl_stmt|;
name|JsonNode
name|openCnxCountNode
init|=
name|openCnxNode
operator|.
name|path
argument_list|(
literal|"count"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|openCnxCountNode
operator|.
name|asInt
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
comment|//create a second connection
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
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|jsonData
operator|=
name|Files
operator|.
name|readAllBytes
argument_list|(
name|Paths
operator|.
name|get
argument_list|(
name|jsonReportFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|rootNode
operator|=
name|objectMapper
operator|.
name|readTree
argument_list|(
name|jsonData
argument_list|)
expr_stmt|;
name|countersNode
operator|=
name|rootNode
operator|.
name|path
argument_list|(
literal|"counters"
argument_list|)
expr_stmt|;
name|openCnxNode
operator|=
name|countersNode
operator|.
name|path
argument_list|(
literal|"open_connections"
argument_list|)
expr_stmt|;
name|openCnxCountNode
operator|=
name|openCnxNode
operator|.
name|path
argument_list|(
literal|"count"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|openCnxCountNode
operator|.
name|asInt
argument_list|()
operator|==
literal|3
argument_list|)
expr_stmt|;
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
name|jsonData
operator|=
name|Files
operator|.
name|readAllBytes
argument_list|(
name|Paths
operator|.
name|get
argument_list|(
name|jsonReportFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|rootNode
operator|=
name|objectMapper
operator|.
name|readTree
argument_list|(
name|jsonData
argument_list|)
expr_stmt|;
name|countersNode
operator|=
name|rootNode
operator|.
name|path
argument_list|(
literal|"counters"
argument_list|)
expr_stmt|;
name|openCnxNode
operator|=
name|countersNode
operator|.
name|path
argument_list|(
literal|"open_connections"
argument_list|)
expr_stmt|;
name|openCnxCountNode
operator|=
name|openCnxNode
operator|.
name|path
argument_list|(
literal|"count"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|openCnxCountNode
operator|.
name|asInt
argument_list|()
operator|==
literal|2
argument_list|)
expr_stmt|;
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
name|jsonData
operator|=
name|Files
operator|.
name|readAllBytes
argument_list|(
name|Paths
operator|.
name|get
argument_list|(
name|jsonReportFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|rootNode
operator|=
name|objectMapper
operator|.
name|readTree
argument_list|(
name|jsonData
argument_list|)
expr_stmt|;
name|countersNode
operator|=
name|rootNode
operator|.
name|path
argument_list|(
literal|"counters"
argument_list|)
expr_stmt|;
name|openCnxNode
operator|=
name|countersNode
operator|.
name|path
argument_list|(
literal|"open_connections"
argument_list|)
expr_stmt|;
name|openCnxCountNode
operator|=
name|openCnxNode
operator|.
name|path
argument_list|(
literal|"count"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|openCnxCountNode
operator|.
name|asInt
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

