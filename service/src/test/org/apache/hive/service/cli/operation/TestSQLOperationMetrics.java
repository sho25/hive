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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|operation
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|OperationState
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
name|cli
operator|.
name|session
operator|.
name|HiveSession
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_comment
comment|/**  * TestSQLOperationMetrics  */
end_comment

begin_class
specifier|public
class|class
name|TestSQLOperationMetrics
block|{
specifier|private
name|SQLOperation
name|operation
decl_stmt|;
specifier|private
name|CodahaleMetrics
name|metrics
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
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
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_METRICS_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|MetricsFactory
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|HiveSession
name|session
init|=
name|mock
argument_list|(
name|HiveSession
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|session
operator|.
name|getHiveConf
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|session
operator|.
name|getSessionState
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mock
argument_list|(
name|SessionState
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|session
operator|.
name|getUserName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"userName"
argument_list|)
expr_stmt|;
name|operation
operator|=
operator|new
name|SQLOperation
argument_list|(
name|session
argument_list|,
literal|"select * from dummy"
argument_list|,
name|Maps
operator|.
expr|<
name|String
argument_list|,
name|String
operator|>
name|newHashMap
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|0L
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
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|MetricsFactory
operator|.
name|getInstance
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSubmittedQueryCount
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|json
init|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|MetricsConstant
operator|.
name|HS2_SUBMITTED_QURIES
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
name|operation
operator|.
name|onNewState
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|,
name|OperationState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|json
operator|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|TIMER
argument_list|,
name|MetricsConstant
operator|.
name|HS2_SUBMITTED_QURIES
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testActiveUserQueriesCount
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|name
init|=
name|MetricsConstant
operator|.
name|SQL_OPERATION_PREFIX
operator|+
literal|"active_user"
decl_stmt|;
name|String
name|json
init|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|name
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|operation
operator|.
name|onNewState
argument_list|(
name|OperationState
operator|.
name|RUNNING
argument_list|,
name|OperationState
operator|.
name|INITIALIZED
argument_list|)
expr_stmt|;
name|json
operator|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|name
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|operation
operator|.
name|onNewState
argument_list|(
name|OperationState
operator|.
name|RUNNING
argument_list|,
name|OperationState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|json
operator|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|name
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|operation
operator|.
name|onNewState
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|,
name|OperationState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|json
operator|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|name
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSucceededQueriesCount
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|json
init|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|METER
argument_list|,
name|MetricsConstant
operator|.
name|HS2_SUCEEDED_QUERIES
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|operation
operator|.
name|onNewState
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|,
name|OperationState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|json
operator|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|METER
argument_list|,
name|MetricsConstant
operator|.
name|HS2_SUCEEDED_QUERIES
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|operation
operator|.
name|onNewState
argument_list|(
name|OperationState
operator|.
name|ERROR
argument_list|,
name|OperationState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|json
operator|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|METER
argument_list|,
name|MetricsConstant
operator|.
name|HS2_SUCEEDED_QUERIES
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|operation
operator|.
name|onNewState
argument_list|(
name|OperationState
operator|.
name|CANCELED
argument_list|,
name|OperationState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|json
operator|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|METER
argument_list|,
name|MetricsConstant
operator|.
name|HS2_SUCEEDED_QUERIES
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|operation
operator|.
name|onNewState
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|,
name|OperationState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|json
operator|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|METER
argument_list|,
name|MetricsConstant
operator|.
name|HS2_SUCEEDED_QUERIES
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailedQueriesCount
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|json
init|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|METER
argument_list|,
name|MetricsConstant
operator|.
name|HS2_FAILED_QUERIES
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|operation
operator|.
name|onNewState
argument_list|(
name|OperationState
operator|.
name|ERROR
argument_list|,
name|OperationState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|json
operator|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|METER
argument_list|,
name|MetricsConstant
operator|.
name|HS2_FAILED_QUERIES
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|operation
operator|.
name|onNewState
argument_list|(
name|OperationState
operator|.
name|FINISHED
argument_list|,
name|OperationState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|json
operator|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|METER
argument_list|,
name|MetricsConstant
operator|.
name|HS2_FAILED_QUERIES
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|operation
operator|.
name|onNewState
argument_list|(
name|OperationState
operator|.
name|CANCELED
argument_list|,
name|OperationState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|json
operator|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|METER
argument_list|,
name|MetricsConstant
operator|.
name|HS2_FAILED_QUERIES
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|operation
operator|.
name|onNewState
argument_list|(
name|OperationState
operator|.
name|ERROR
argument_list|,
name|OperationState
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|json
operator|=
operator|(
operator|(
name|CodahaleMetrics
operator|)
name|metrics
operator|)
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
name|METER
argument_list|,
name|MetricsConstant
operator|.
name|HS2_FAILED_QUERIES
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

