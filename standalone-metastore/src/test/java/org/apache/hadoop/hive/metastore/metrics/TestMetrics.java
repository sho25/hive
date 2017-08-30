begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|metrics
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
name|Gauge
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
name|Histogram
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
name|Meter
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
name|fs
operator|.
name|FileSystem
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
name|Arrays
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
name|TimeUnit
import|;
end_import

begin_class
specifier|public
class|class
name|TestMetrics
block|{
annotation|@
name|Test
specifier|public
name|void
name|jsonReporter
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|jsonFile
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"file.separator"
argument_list|)
operator|+
literal|"TestMetricsOutput.json"
decl_stmt|;
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METRICS_REPORTERS
argument_list|,
literal|"json"
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METRICS_JSON_FILE_LOCATION
argument_list|,
name|jsonFile
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setTimeVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METRICS_JSON_FILE_INTERVAL
argument_list|,
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|Metrics
operator|.
name|initialize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|words
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"mary"
argument_list|,
literal|"had"
argument_list|,
literal|"a"
argument_list|,
literal|"little"
argument_list|,
literal|"lamb"
argument_list|)
decl_stmt|;
name|MetricRegistry
name|registry
init|=
name|Metrics
operator|.
name|getRegistry
argument_list|()
decl_stmt|;
name|registry
operator|.
name|register
argument_list|(
literal|"my-gauge"
argument_list|,
operator|new
name|Gauge
argument_list|<
name|Integer
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Integer
name|getValue
parameter_list|()
block|{
return|return
name|words
operator|.
name|size
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Counter
name|counter
init|=
name|Metrics
operator|.
name|getOrCreateCounter
argument_list|(
literal|"my-counter"
argument_list|)
decl_stmt|;
name|counter
operator|.
name|inc
argument_list|()
expr_stmt|;
name|counter
operator|.
name|inc
argument_list|()
expr_stmt|;
name|Meter
name|meter
init|=
name|registry
operator|.
name|meter
argument_list|(
literal|"my-meter"
argument_list|)
decl_stmt|;
name|meter
operator|.
name|mark
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|meter
operator|.
name|mark
argument_list|()
expr_stmt|;
name|Timer
name|timer
init|=
name|Metrics
operator|.
name|getOrCreateTimer
argument_list|(
literal|"my-timer"
argument_list|)
decl_stmt|;
name|timer
operator|.
name|time
argument_list|(
operator|new
name|Callable
argument_list|<
name|Long
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Long
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
return|return
literal|1L
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Make sure it has a chance to dump it.
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|jsonFile
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|json
init|=
operator|new
name|String
argument_list|(
name|MetricsTestUtils
operator|.
name|getFileData
argument_list|(
name|jsonFile
argument_list|,
literal|200
argument_list|,
literal|10
argument_list|)
argument_list|)
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
literal|"my-counter"
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
name|METER
argument_list|,
literal|"my-meter"
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
name|TIMER
argument_list|,
literal|"my-timer"
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
name|GAUGE
argument_list|,
literal|"my-gauge"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|allReporters
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|jsonFile
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"file.separator"
argument_list|)
operator|+
literal|"TestMetricsOutput.json"
decl_stmt|;
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METRICS_REPORTERS
argument_list|,
literal|"json,jmx,console,hadoop"
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METRICS_JSON_FILE_LOCATION
argument_list|,
name|jsonFile
argument_list|)
expr_stmt|;
name|Metrics
operator|.
name|initialize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|Metrics
operator|.
name|getReporters
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|allReportersHiveConfig
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|jsonFile
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"file.separator"
argument_list|)
operator|+
literal|"TestMetricsOutput.json"
decl_stmt|;
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|HIVE_CODAHALE_METRICS_REPORTER_CLASSES
operator|.
name|hiveName
argument_list|,
literal|"org.apache.hadoop.hive.common.metrics.metrics2.JsonFileMetricsReporter,"
operator|+
literal|"org.apache.hadoop.hive.common.metrics.metrics2.JmxMetricsReporter,"
operator|+
literal|"org.apache.hadoop.hive.common.metrics.metrics2.ConsoleMetricsReporter,"
operator|+
literal|"org.apache.hadoop.hive.common.metrics.metrics2.Metrics2Reporter"
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METRICS_JSON_FILE_LOCATION
argument_list|,
name|jsonFile
argument_list|)
expr_stmt|;
name|Metrics
operator|.
name|initialize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|Metrics
operator|.
name|getReporters
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|allReportersOldHiveConfig
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|jsonFile
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"file.separator"
argument_list|)
operator|+
literal|"TestMetricsOutput.json"
decl_stmt|;
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|HIVE_METRICS_REPORTER
operator|.
name|hiveName
argument_list|,
literal|"JSON_FILE,JMX,CONSOLE,HADOOP2"
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METRICS_JSON_FILE_LOCATION
argument_list|,
name|jsonFile
argument_list|)
expr_stmt|;
name|Metrics
operator|.
name|initialize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|Metrics
operator|.
name|getReporters
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|defaults
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|jsonFile
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"file.separator"
argument_list|)
operator|+
literal|"TestMetricsOutput.json"
decl_stmt|;
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METRICS_JSON_FILE_LOCATION
argument_list|,
name|jsonFile
argument_list|)
expr_stmt|;
name|Metrics
operator|.
name|initialize
argument_list|(
name|conf
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
name|getReporters
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|shutdownMetrics
parameter_list|()
block|{
name|Metrics
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
comment|// Stolen from Hive's MetricsTestUtils.  Probably should break it out into it's own class.
specifier|private
specifier|static
class|class
name|MetricsTestUtils
block|{
specifier|static
specifier|final
name|MetricsCategory
name|COUNTER
init|=
operator|new
name|MetricsCategory
argument_list|(
literal|"counters"
argument_list|,
literal|"count"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|MetricsCategory
name|TIMER
init|=
operator|new
name|MetricsCategory
argument_list|(
literal|"timers"
argument_list|,
literal|"count"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|MetricsCategory
name|GAUGE
init|=
operator|new
name|MetricsCategory
argument_list|(
literal|"gauges"
argument_list|,
literal|"value"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|MetricsCategory
name|METER
init|=
operator|new
name|MetricsCategory
argument_list|(
literal|"meters"
argument_list|,
literal|"count"
argument_list|)
decl_stmt|;
specifier|static
class|class
name|MetricsCategory
block|{
name|String
name|category
decl_stmt|;
name|String
name|metricsHandle
decl_stmt|;
name|MetricsCategory
parameter_list|(
name|String
name|category
parameter_list|,
name|String
name|metricsHandle
parameter_list|)
block|{
name|this
operator|.
name|category
operator|=
name|category
expr_stmt|;
name|this
operator|.
name|metricsHandle
operator|=
name|metricsHandle
expr_stmt|;
block|}
block|}
specifier|static
name|void
name|verifyMetricsJson
parameter_list|(
name|String
name|json
parameter_list|,
name|MetricsCategory
name|category
parameter_list|,
name|String
name|metricsName
parameter_list|,
name|Object
name|expectedValue
parameter_list|)
throws|throws
name|Exception
block|{
name|JsonNode
name|jsonNode
init|=
name|getJsonNode
argument_list|(
name|json
argument_list|,
name|category
argument_list|,
name|metricsName
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedValue
operator|.
name|toString
argument_list|()
argument_list|,
name|jsonNode
operator|.
name|asText
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|static
name|JsonNode
name|getJsonNode
parameter_list|(
name|String
name|json
parameter_list|,
name|MetricsCategory
name|category
parameter_list|,
name|String
name|metricsName
parameter_list|)
throws|throws
name|Exception
block|{
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
name|json
argument_list|)
decl_stmt|;
name|JsonNode
name|categoryNode
init|=
name|rootNode
operator|.
name|path
argument_list|(
name|category
operator|.
name|category
argument_list|)
decl_stmt|;
name|JsonNode
name|metricsNode
init|=
name|categoryNode
operator|.
name|path
argument_list|(
name|metricsName
argument_list|)
decl_stmt|;
return|return
name|metricsNode
operator|.
name|path
argument_list|(
name|category
operator|.
name|metricsHandle
argument_list|)
return|;
block|}
specifier|static
name|byte
index|[]
name|getFileData
parameter_list|(
name|String
name|path
parameter_list|,
name|int
name|timeoutInterval
parameter_list|,
name|int
name|tries
parameter_list|)
throws|throws
name|Exception
block|{
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|path
argument_list|)
decl_stmt|;
do|do
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|timeoutInterval
argument_list|)
expr_stmt|;
name|tries
operator|--
expr_stmt|;
block|}
do|while
condition|(
name|tries
operator|>
literal|0
operator|&&
operator|!
name|file
operator|.
name|exists
argument_list|()
condition|)
do|;
return|return
name|Files
operator|.
name|readAllBytes
argument_list|(
name|Paths
operator|.
name|get
argument_list|(
name|path
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

