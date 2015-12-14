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
name|session
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

begin_comment
comment|/**  * Test metrics from SessionManager.  */
end_comment

begin_class
specifier|public
class|class
name|TestSessionManagerMetrics
block|{
specifier|private
specifier|static
name|SessionManager
name|sm
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
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ASYNC_EXEC_THREADS
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ASYNC_EXEC_WAIT_QUEUE_SIZE
argument_list|,
literal|10
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
name|HIVE_SERVER2_ASYNC_EXEC_KEEPALIVE_TIME
argument_list|,
literal|"1000000s"
argument_list|)
expr_stmt|;
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
name|conf
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
name|MetricsFactory
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|HiveServer2
name|hs2
init|=
operator|new
name|HiveServer2
argument_list|()
decl_stmt|;
name|sm
operator|=
operator|new
name|SessionManager
argument_list|(
name|hs2
argument_list|)
expr_stmt|;
name|sm
operator|.
name|init
argument_list|(
name|conf
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
specifier|final
name|Object
name|barrier
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
class|class
name|BarrierRunnable
implements|implements
name|Runnable
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
synchronized|synchronized
init|(
name|barrier
init|)
block|{
try|try
block|{
name|barrier
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
comment|/**    * Tests metrics regarding async thread pool.    */
annotation|@
name|Test
specifier|public
name|void
name|testThreadPoolMetrics
parameter_list|()
throws|throws
name|Exception
block|{
name|sm
operator|.
name|submitBackgroundOperation
argument_list|(
operator|new
name|BarrierRunnable
argument_list|()
argument_list|)
expr_stmt|;
name|sm
operator|.
name|submitBackgroundOperation
argument_list|(
operator|new
name|BarrierRunnable
argument_list|()
argument_list|)
expr_stmt|;
name|sm
operator|.
name|submitBackgroundOperation
argument_list|(
operator|new
name|BarrierRunnable
argument_list|()
argument_list|)
expr_stmt|;
name|sm
operator|.
name|submitBackgroundOperation
argument_list|(
operator|new
name|BarrierRunnable
argument_list|()
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
name|GAUGE
argument_list|,
name|MetricsConstant
operator|.
name|EXEC_ASYNC_POOL_SIZE
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
name|EXEC_ASYNC_QUEUE_SIZE
argument_list|,
literal|2
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|barrier
init|)
block|{
name|barrier
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
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
name|EXEC_ASYNC_POOL_SIZE
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
name|EXEC_ASYNC_QUEUE_SIZE
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

