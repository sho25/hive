begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|jdbc
package|;
end_package

begin_import
import|import static
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
operator|.
name|tez
operator|.
name|TestWorkloadManager
operator|.
name|plan
import|;
end_import

begin_import
import|import static
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
operator|.
name|tez
operator|.
name|TestWorkloadManager
operator|.
name|pool
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
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

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
name|List
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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|WMFullResourcePlan
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
name|api
operator|.
name|WMPool
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
name|api
operator|.
name|WMPoolTrigger
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
name|exec
operator|.
name|tez
operator|.
name|WorkloadManager
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
name|wm
operator|.
name|Action
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
name|wm
operator|.
name|ExecutionTrigger
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
name|wm
operator|.
name|Expression
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
name|wm
operator|.
name|ExpressionFactory
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
name|wm
operator|.
name|Trigger
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
specifier|public
class|class
name|TestTriggersMoveWorkloadManager
extends|extends
name|AbstractJdbcTriggersTest
block|{
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeTest
parameter_list|()
throws|throws
name|Exception
block|{
name|Class
operator|.
name|forName
argument_list|(
name|MiniHS2
operator|.
name|getJdbcDriverName
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|confDir
init|=
literal|"../../data/conf/llap/"
decl_stmt|;
name|HiveConf
operator|.
name|setHiveSiteLocation
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file://"
operator|+
operator|new
name|File
argument_list|(
name|confDir
argument_list|)
operator|.
name|toURI
argument_list|()
operator|.
name|getPath
argument_list|()
operator|+
literal|"/hive-site.xml"
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Setting hive-site: "
operator|+
name|HiveConf
operator|.
name|getHiveSiteLocation
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setTimeVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_TRIGGER_VALIDATION_INTERVAL
argument_list|,
literal|50
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setTimeVar
argument_list|(
name|ConfVars
operator|.
name|TEZ_DAG_STATUS_CHECK_INTERVAL
argument_list|,
literal|50
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TEZ_INTERACTIVE_QUEUE
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hive.test.workload.management"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|TEZ_EXEC_SUMMARY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_STRICT_CHECKS_CARTESIAN
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// don't want cache hits from llap io for testing filesystem bytes read counters
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|LLAP_IO_MEMORY_MODE
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|addResource
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file://"
operator|+
operator|new
name|File
argument_list|(
name|confDir
argument_list|)
operator|.
name|toURI
argument_list|()
operator|.
name|getPath
argument_list|()
operator|+
literal|"/tez-site.xml"
argument_list|)
argument_list|)
expr_stmt|;
name|miniHS2
operator|=
operator|new
name|MiniHS2
argument_list|(
name|conf
argument_list|,
name|MiniClusterType
operator|.
name|LLAP
argument_list|)
expr_stmt|;
name|dataFileDir
operator|=
name|conf
operator|.
name|get
argument_list|(
literal|"test.data.files"
argument_list|)
operator|.
name|replace
argument_list|(
literal|'\\'
argument_list|,
literal|'/'
argument_list|)
operator|.
name|replace
argument_list|(
literal|"c:"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|kvDataFilePath
operator|=
operator|new
name|Path
argument_list|(
name|dataFileDir
argument_list|,
literal|"kv1.txt"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|miniHS2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|getDFS
argument_list|()
operator|.
name|getFileSystem
argument_list|()
operator|.
name|mkdirs
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/apps_staging_dir/anonymous"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testTriggerMoveAndKill
parameter_list|()
throws|throws
name|Exception
block|{
name|Expression
name|moveExpression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"EXECUTION_TIME> 1000"
argument_list|)
decl_stmt|;
name|Expression
name|killExpression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"EXECUTION_TIME> 5000"
argument_list|)
decl_stmt|;
name|Trigger
name|moveTrigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"slow_query_move"
argument_list|,
name|moveExpression
argument_list|,
operator|new
name|Action
argument_list|(
name|Action
operator|.
name|Type
operator|.
name|MOVE_TO_POOL
argument_list|,
literal|"ETL"
argument_list|)
argument_list|)
decl_stmt|;
name|Trigger
name|killTrigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"slow_query_kill"
argument_list|,
name|killExpression
argument_list|,
operator|new
name|Action
argument_list|(
name|Action
operator|.
name|Type
operator|.
name|KILL_QUERY
argument_list|)
argument_list|)
decl_stmt|;
name|setupTriggers
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|moveTrigger
argument_list|)
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|killTrigger
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"select sleep(t1.under_col, 5), t1.value from "
operator|+
name|tableName
operator|+
literal|" t1 join "
operator|+
name|tableName
operator|+
literal|" t2 on t1.under_col>=t2.under_col"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|setCmds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set hive.tez.session.events.print.summary=json"
argument_list|)
expr_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set hive.exec.post.hooks=org.apache.hadoop.hive.ql.hooks.PostExecWMEventsSummaryPrinter"
argument_list|)
expr_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set hive.exec.failure.hooks=org.apache.hadoop.hive.ql.hooks.PostExecWMEventsSummaryPrinter"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|errCaptureExpect
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Workload Manager Events Summary"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: GET Pool: BI Cluster %: 80.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: MOVE Pool: ETL Cluster %: 20.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: KILL Pool: null Cluster %: 0.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: RETURN Pool: null Cluster %: 0.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"GET\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"MOVE\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"KILL\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"RETURN\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"name\" : \"slow_query_move\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"name\" : \"slow_query_kill\""
argument_list|)
expr_stmt|;
comment|// violation in BI queue
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"violationMsg\" : \"Trigger "
operator|+
name|moveTrigger
operator|+
literal|" violated"
argument_list|)
expr_stmt|;
comment|// violation in ETL queue
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"violationMsg\" : \"Trigger "
operator|+
name|killTrigger
operator|+
literal|" violated"
argument_list|)
expr_stmt|;
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|setCmds
argument_list|,
name|killTrigger
operator|+
literal|" violated"
argument_list|,
name|errCaptureExpect
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testTriggerMoveEscapeKill
parameter_list|()
throws|throws
name|Exception
block|{
name|Expression
name|moveExpression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"HDFS_BYTES_READ> 100"
argument_list|)
decl_stmt|;
name|Expression
name|killExpression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"EXECUTION_TIME> 5000"
argument_list|)
decl_stmt|;
name|Trigger
name|moveTrigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"move_big_read"
argument_list|,
name|moveExpression
argument_list|,
operator|new
name|Action
argument_list|(
name|Action
operator|.
name|Type
operator|.
name|MOVE_TO_POOL
argument_list|,
literal|"ETL"
argument_list|)
argument_list|)
decl_stmt|;
name|Trigger
name|killTrigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"slow_query_kill"
argument_list|,
name|killExpression
argument_list|,
operator|new
name|Action
argument_list|(
name|Action
operator|.
name|Type
operator|.
name|KILL_QUERY
argument_list|)
argument_list|)
decl_stmt|;
name|setupTriggers
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|moveTrigger
argument_list|,
name|killTrigger
argument_list|)
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"select sleep(t1.under_col, 1), t1.value from "
operator|+
name|tableName
operator|+
literal|" t1 join "
operator|+
name|tableName
operator|+
literal|" t2 on t1.under_col==t2.under_col"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|setCmds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set hive.tez.session.events.print.summary=json"
argument_list|)
expr_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set hive.exec.post.hooks=org.apache.hadoop.hive.ql.hooks.PostExecWMEventsSummaryPrinter"
argument_list|)
expr_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set hive.exec.failure.hooks=org.apache.hadoop.hive.ql.hooks.PostExecWMEventsSummaryPrinter"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|errCaptureExpect
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Workload Manager Events Summary"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: GET Pool: BI Cluster %: 80.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: MOVE Pool: ETL Cluster %: 20.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: RETURN Pool: null Cluster %: 0.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"GET\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"MOVE\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"RETURN\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"name\" : \"move_big_read\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"name\" : \"slow_query_kill\""
argument_list|)
expr_stmt|;
comment|// violation in BI queue
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"violationMsg\" : \"Trigger "
operator|+
name|moveTrigger
operator|+
literal|" violated"
argument_list|)
expr_stmt|;
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|setCmds
argument_list|,
literal|null
argument_list|,
name|errCaptureExpect
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testTriggerMoveBackKill
parameter_list|()
throws|throws
name|Exception
block|{
name|Expression
name|moveExpression1
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"HDFS_BYTES_READ> 100"
argument_list|)
decl_stmt|;
name|Expression
name|moveExpression2
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"SHUFFLE_BYTES> 200"
argument_list|)
decl_stmt|;
name|Expression
name|killExpression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"EXECUTION_TIME> 2000"
argument_list|)
decl_stmt|;
name|Trigger
name|moveTrigger1
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"move_big_read"
argument_list|,
name|moveExpression1
argument_list|,
operator|new
name|Action
argument_list|(
name|Action
operator|.
name|Type
operator|.
name|MOVE_TO_POOL
argument_list|,
literal|"ETL"
argument_list|)
argument_list|)
decl_stmt|;
name|Trigger
name|moveTrigger2
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"move_high"
argument_list|,
name|moveExpression2
argument_list|,
operator|new
name|Action
argument_list|(
name|Action
operator|.
name|Type
operator|.
name|MOVE_TO_POOL
argument_list|,
literal|"BI"
argument_list|)
argument_list|)
decl_stmt|;
name|Trigger
name|killTrigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"slow_query_kill"
argument_list|,
name|killExpression
argument_list|,
operator|new
name|Action
argument_list|(
name|Action
operator|.
name|Type
operator|.
name|KILL_QUERY
argument_list|)
argument_list|)
decl_stmt|;
name|setupTriggers
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|moveTrigger1
argument_list|,
name|killTrigger
argument_list|)
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|moveTrigger2
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"select sleep(t1.under_col, 1), t1.value from "
operator|+
name|tableName
operator|+
literal|" t1 join "
operator|+
name|tableName
operator|+
literal|" t2 on t1.under_col>=t2.under_col"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|setCmds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set hive.tez.session.events.print.summary=json"
argument_list|)
expr_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set hive.exec.post.hooks=org.apache.hadoop.hive.ql.hooks.PostExecWMEventsSummaryPrinter"
argument_list|)
expr_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set hive.exec.failure.hooks=org.apache.hadoop.hive.ql.hooks.PostExecWMEventsSummaryPrinter"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|errCaptureExpect
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Workload Manager Events Summary"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: GET Pool: BI Cluster %: 80.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: MOVE Pool: ETL Cluster %: 20.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: MOVE Pool: BI Cluster %: 80.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: KILL Pool: null Cluster %: 0.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: RETURN Pool: null Cluster %: 0.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"GET\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"MOVE\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"MOVE\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"KILL\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"RETURN\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"name\" : \"move_big_read\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"name\" : \"slow_query_kill\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"name\" : \"move_high\""
argument_list|)
expr_stmt|;
comment|// violation in BI queue
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"violationMsg\" : \"Trigger "
operator|+
name|moveTrigger1
operator|+
literal|" violated"
argument_list|)
expr_stmt|;
comment|// violation in ETL queue
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"violationMsg\" : \"Trigger "
operator|+
name|moveTrigger2
operator|+
literal|" violated"
argument_list|)
expr_stmt|;
comment|// violation in BI queue
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"violationMsg\" : \"Trigger "
operator|+
name|killTrigger
operator|+
literal|" violated"
argument_list|)
expr_stmt|;
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|setCmds
argument_list|,
name|killTrigger
operator|+
literal|" violated"
argument_list|,
name|errCaptureExpect
argument_list|)
expr_stmt|;
block|}
comment|// TODO: disabling this test as tez publishes counters only after task completion which will cause write side counters
comment|// to be not validated correctly (DAG will be completed before validation)
comment|//  @Test(timeout = 60000)
comment|//  public void testTriggerMoveKill() throws Exception {
comment|//    Expression moveExpression1 = ExpressionFactory.fromString("HDFS_BYTES_READ> 100");
comment|//    Expression moveExpression2 = ExpressionFactory.fromString("HDFS_BYTES_WRITTEN> 200");
comment|//    Trigger moveTrigger1 = new ExecutionTrigger("move_big_read", moveExpression1,
comment|//      new Action(Action.Type.MOVE_TO_POOL, "ETL"));
comment|//    Trigger killTrigger = new ExecutionTrigger("big_write_kill", moveExpression2,
comment|//      new Action(Action.Type.KILL_QUERY));
comment|//    setupTriggers(Lists.newArrayList(moveTrigger1), Lists.newArrayList(killTrigger));
comment|//    String query = "select t1.under_col, t1.value from " + tableName + " t1 join " + tableName +
comment|//      " t2 on t1.under_col>=t2.under_col order by t1.under_col, t1.value";
comment|//    List<String> setCmds = new ArrayList<>();
comment|//    setCmds.add("set hive.tez.session.events.print.summary=json");
comment|//    setCmds.add("set hive.exec.post.hooks=org.apache.hadoop.hive.ql.hooks.PostExecWMEventsSummaryPrinter");
comment|//    setCmds.add("set hive.exec.failure.hooks=org.apache.hadoop.hive.ql.hooks.PostExecWMEventsSummaryPrinter");
comment|//    List<String> errCaptureExpect = new ArrayList<>();
comment|//    errCaptureExpect.add("Workload Manager Events Summary");
comment|//    errCaptureExpect.add("Event: GET Pool: BI Cluster %: 80.00");
comment|//    errCaptureExpect.add("Event: MOVE Pool: ETL Cluster %: 20.00");
comment|//    errCaptureExpect.add("Event: KILL Pool: null Cluster %: 0.00");
comment|//    errCaptureExpect.add("Event: RETURN Pool: null Cluster %: 0.00");
comment|//    errCaptureExpect.add("\"eventType\" : \"GET\"");
comment|//    errCaptureExpect.add("\"eventType\" : \"MOVE\"");
comment|//    errCaptureExpect.add("\"eventType\" : \"KILL\"");
comment|//    errCaptureExpect.add("\"eventType\" : \"RETURN\"");
comment|//    errCaptureExpect.add("\"name\" : \"move_big_read\"");
comment|//    errCaptureExpect.add("\"name\" : \"big_write_kill\"");
comment|//    // violation in BI queue
comment|//    errCaptureExpect.add("\"violationMsg\" : \"Trigger " + moveTrigger1 + " violated");
comment|//    // violation in ETL queue
comment|//    errCaptureExpect.add("\"violationMsg\" : \"Trigger " + killTrigger + " violated");
comment|//    runQueryWithTrigger(query, setCmds, killTrigger + " violated", errCaptureExpect);
comment|//  }
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testTriggerMoveConflictKill
parameter_list|()
throws|throws
name|Exception
block|{
name|Expression
name|moveExpression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"HDFS_BYTES_READ> 100"
argument_list|)
decl_stmt|;
name|Expression
name|killExpression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"HDFS_BYTES_READ> 100"
argument_list|)
decl_stmt|;
name|Trigger
name|moveTrigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"move_big_read"
argument_list|,
name|moveExpression
argument_list|,
operator|new
name|Action
argument_list|(
name|Action
operator|.
name|Type
operator|.
name|MOVE_TO_POOL
argument_list|,
literal|"ETL"
argument_list|)
argument_list|)
decl_stmt|;
name|Trigger
name|killTrigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"kill_big_read"
argument_list|,
name|killExpression
argument_list|,
operator|new
name|Action
argument_list|(
name|Action
operator|.
name|Type
operator|.
name|KILL_QUERY
argument_list|)
argument_list|)
decl_stmt|;
name|setupTriggers
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|moveTrigger
argument_list|,
name|killTrigger
argument_list|)
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"select sleep(t1.under_col, 5), t1.value from "
operator|+
name|tableName
operator|+
literal|" t1 join "
operator|+
name|tableName
operator|+
literal|" t2 on t1.under_col>=t2.under_col"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|setCmds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set hive.tez.session.events.print.summary=json"
argument_list|)
expr_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set hive.exec.post.hooks=org.apache.hadoop.hive.ql.hooks.PostExecWMEventsSummaryPrinter"
argument_list|)
expr_stmt|;
name|setCmds
operator|.
name|add
argument_list|(
literal|"set hive.exec.failure.hooks=org.apache.hadoop.hive.ql.hooks.PostExecWMEventsSummaryPrinter"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|errCaptureExpect
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Workload Manager Events Summary"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: GET Pool: BI Cluster %: 80.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: KILL Pool: null Cluster %: 0.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"Event: RETURN Pool: null Cluster %: 0.00"
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"GET\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"KILL\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"eventType\" : \"RETURN\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"name\" : \"move_big_read\""
argument_list|)
expr_stmt|;
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"name\" : \"kill_big_read\""
argument_list|)
expr_stmt|;
comment|// violation in BI queue
name|errCaptureExpect
operator|.
name|add
argument_list|(
literal|"\"violationMsg\" : \"Trigger "
operator|+
name|killTrigger
operator|+
literal|" violated"
argument_list|)
expr_stmt|;
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|setCmds
argument_list|,
name|killTrigger
operator|+
literal|" violated"
argument_list|,
name|errCaptureExpect
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setupTriggers
parameter_list|(
specifier|final
name|List
argument_list|<
name|Trigger
argument_list|>
name|triggers
parameter_list|)
throws|throws
name|Exception
block|{
name|setupTriggers
argument_list|(
name|triggers
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setupTriggers
parameter_list|(
specifier|final
name|List
argument_list|<
name|Trigger
argument_list|>
name|biTriggers
parameter_list|,
specifier|final
name|List
argument_list|<
name|Trigger
argument_list|>
name|etlTriggers
parameter_list|)
throws|throws
name|Exception
block|{
name|WorkloadManager
name|wm
init|=
name|WorkloadManager
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|WMPool
name|biPool
init|=
name|pool
argument_list|(
literal|"BI"
argument_list|,
literal|1
argument_list|,
literal|0.8f
argument_list|)
decl_stmt|;
name|WMPool
name|etlPool
init|=
name|pool
argument_list|(
literal|"ETL"
argument_list|,
literal|1
argument_list|,
literal|0.2f
argument_list|)
decl_stmt|;
name|WMFullResourcePlan
name|plan
init|=
operator|new
name|WMFullResourcePlan
argument_list|(
name|plan
argument_list|()
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|biPool
argument_list|,
name|etlPool
argument_list|)
argument_list|)
decl_stmt|;
name|plan
operator|.
name|getPlan
argument_list|()
operator|.
name|setDefaultPoolPath
argument_list|(
literal|"BI"
argument_list|)
expr_stmt|;
for|for
control|(
name|Trigger
name|trigger
range|:
name|biTriggers
control|)
block|{
name|plan
operator|.
name|addToTriggers
argument_list|(
name|wmTriggerFromTrigger
argument_list|(
name|trigger
argument_list|)
argument_list|)
expr_stmt|;
name|plan
operator|.
name|addToPoolTriggers
argument_list|(
operator|new
name|WMPoolTrigger
argument_list|(
literal|"BI"
argument_list|,
name|trigger
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Trigger
name|trigger
range|:
name|etlTriggers
control|)
block|{
name|plan
operator|.
name|addToTriggers
argument_list|(
name|wmTriggerFromTrigger
argument_list|(
name|trigger
argument_list|)
argument_list|)
expr_stmt|;
name|plan
operator|.
name|addToPoolTriggers
argument_list|(
operator|new
name|WMPoolTrigger
argument_list|(
literal|"ETL"
argument_list|,
name|trigger
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|wm
operator|.
name|updateResourcePlanAsync
argument_list|(
name|plan
argument_list|)
operator|.
name|get
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

