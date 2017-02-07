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
name|Meter
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
name|junit
operator|.
name|Assert
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

begin_comment
comment|/**  * Utilities for codahale metrics verification.  */
end_comment

begin_class
specifier|public
class|class
name|MetricsTestUtils
block|{
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
name|Double
name|expectedValue
parameter_list|,
name|Double
name|delta
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
argument_list|,
name|Double
operator|.
name|valueOf
argument_list|(
name|jsonNode
operator|.
name|asText
argument_list|()
argument_list|)
argument_list|,
name|delta
argument_list|)
expr_stmt|;
block|}
specifier|public
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
specifier|public
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
end_class

end_unit

