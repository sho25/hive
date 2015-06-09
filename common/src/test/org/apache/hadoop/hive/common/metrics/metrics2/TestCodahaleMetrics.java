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
name|common
operator|.
name|metrics
operator|.
name|metrics2
package|;
end_package

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Counter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|MetricRegistry
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Timer
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
name|concurrent
operator|.
name|Callable
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
name|ExecutorService
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
name|Executors
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_comment
comment|/**  * Unit test for new Metrics subsystem.  */
end_comment

begin_class
specifier|public
class|class
name|TestCodahaleMetrics
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
specifier|public
specifier|static
name|MetricRegistry
name|metricRegistry
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
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
name|String
name|defaultFsName
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHadoopConfNames
argument_list|()
operator|.
name|get
argument_list|(
literal|"HADOOPFS"
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|defaultFsName
argument_list|,
literal|"local"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METRICS_CLASS
argument_list|,
name|CodahaleMetrics
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
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
name|conf
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
name|conf
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
name|MetricsFactory
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|metricRegistry
operator|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|MetricsFactory
operator|.
name|getMetricsInstance
argument_list|()
operator|)
operator|.
name|getMetricRegistry
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|after
parameter_list|()
throws|throws
name|Exception
block|{
name|MetricsFactory
operator|.
name|deInit
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScope
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|runs
init|=
literal|5
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
name|runs
condition|;
name|i
operator|++
control|)
block|{
name|MetricsFactory
operator|.
name|getMetricsInstance
argument_list|()
operator|.
name|startScope
argument_list|(
literal|"method1"
argument_list|)
expr_stmt|;
name|MetricsFactory
operator|.
name|getMetricsInstance
argument_list|()
operator|.
name|endScope
argument_list|(
literal|"method1"
argument_list|)
expr_stmt|;
block|}
name|Timer
name|timer
init|=
name|metricRegistry
operator|.
name|getTimers
argument_list|()
operator|.
name|get
argument_list|(
literal|"api_method1"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|timer
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|timer
operator|.
name|getMeanRate
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
name|testCount
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|runs
init|=
literal|5
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
name|runs
condition|;
name|i
operator|++
control|)
block|{
name|MetricsFactory
operator|.
name|getMetricsInstance
argument_list|()
operator|.
name|incrementCounter
argument_list|(
literal|"count1"
argument_list|)
expr_stmt|;
block|}
name|Counter
name|counter
init|=
name|metricRegistry
operator|.
name|getCounters
argument_list|()
operator|.
name|get
argument_list|(
literal|"count1"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|5L
argument_list|,
name|counter
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
name|testConcurrency
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|threads
init|=
literal|4
decl_stmt|;
name|ExecutorService
name|executorService
init|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|threads
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
name|threads
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|n
init|=
name|i
decl_stmt|;
name|executorService
operator|.
name|submit
argument_list|(
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|MetricsFactory
operator|.
name|getMetricsInstance
argument_list|()
operator|.
name|startScope
argument_list|(
literal|"method2"
argument_list|)
expr_stmt|;
name|MetricsFactory
operator|.
name|getMetricsInstance
argument_list|()
operator|.
name|endScope
argument_list|(
literal|"method2"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|executorService
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|executorService
operator|.
name|awaitTermination
argument_list|(
literal|10000
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
expr_stmt|;
name|Timer
name|timer
init|=
name|metricRegistry
operator|.
name|getTimers
argument_list|()
operator|.
name|get
argument_list|(
literal|"api_method2"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|timer
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|timer
operator|.
name|getMeanRate
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
name|testFileReporting
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|runs
init|=
literal|5
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
name|runs
condition|;
name|i
operator|++
control|)
block|{
name|MetricsFactory
operator|.
name|getMetricsInstance
argument_list|()
operator|.
name|incrementCounter
argument_list|(
literal|"count2"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
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
name|methodCounterNode
init|=
name|countersNode
operator|.
name|path
argument_list|(
literal|"count2"
argument_list|)
decl_stmt|;
name|JsonNode
name|countNode
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
name|assertEquals
argument_list|(
name|countNode
operator|.
name|asInt
argument_list|()
argument_list|,
literal|5
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

