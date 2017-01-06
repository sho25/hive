begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|jdbc
operator|.
name|miniHS2
operator|.
name|MiniHS2
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
name|jdbc
operator|.
name|miniHS2
operator|.
name|MiniHS2
operator|.
name|MiniClusterType
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

begin_comment
comment|/**  * TestOperationLoggingAPIWithTez  * Test the FetchResults of TFetchType.LOG in thrift level in Tez mode.  */
end_comment

begin_class
specifier|public
class|class
name|TestOperationLoggingAPIWithTez
extends|extends
name|OperationLoggingAPITestBase
block|{
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|tableName
operator|=
literal|"testOperationLoggingAPIWithTez_table"
expr_stmt|;
name|expectedLogsVerbose
operator|=
operator|new
name|String
index|[]
block|{
literal|"Starting Semantic Analysis"
block|}
expr_stmt|;
name|expectedLogsExecution
operator|=
operator|new
name|String
index|[]
block|{
literal|"Compiling command"
block|,
literal|"Completed compiling command"
block|,
literal|"Executing command"
block|,
literal|"Completed executing command"
block|,
literal|"Semantic Analysis Completed"
block|,
literal|"Executing on YARN cluster with App id"
block|,
literal|"Setting Tez DAG access"
block|}
expr_stmt|;
name|expectedLogsPerformance
operator|=
operator|new
name|String
index|[]
block|{
literal|"<PERFLOG method=compile from=org.apache.hadoop.hive.ql.Driver>"
block|,
literal|"<PERFLOG method=parse from=org.apache.hadoop.hive.ql.Driver>"
block|,
literal|"<PERFLOG method=Driver.run from=org.apache.hadoop.hive.ql.Driver>"
block|,
literal|"from=org.apache.hadoop.hive.ql.exec.tez.monitoring.TezJobMonitor"
block|,
literal|"org.apache.tez.common.counters.DAGCounter"
block|,
literal|"NUM_SUCCEEDED_TASKS"
block|,
literal|"TOTAL_LAUNCHED_TASKS"
block|,
literal|"CPU_MILLISECONDS"
block|}
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_LOGGING_OPERATION_LEVEL
operator|.
name|varname
argument_list|,
literal|"verbose"
argument_list|)
expr_stmt|;
comment|// Change the engine to tez
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|,
literal|"tez"
argument_list|)
expr_stmt|;
comment|// Set tez execution summary to false.
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|TEZ_EXEC_SUMMARY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|miniHS2
operator|=
operator|new
name|MiniHS2
argument_list|(
name|hiveConf
argument_list|,
name|MiniClusterType
operator|.
name|TEZ
argument_list|)
expr_stmt|;
name|confOverlay
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
name|confOverlay
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

