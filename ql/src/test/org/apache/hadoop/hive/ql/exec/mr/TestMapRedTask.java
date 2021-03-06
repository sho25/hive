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
name|ql
operator|.
name|exec
operator|.
name|mr
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
name|security
operator|.
name|UserGroupInformation
operator|.
name|AuthenticationMethod
operator|.
name|PROXY
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
name|never
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
name|times
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
name|verify
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
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
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
name|common
operator|.
name|metrics
operator|.
name|common
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
name|Context
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
name|TaskQueue
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
name|QueryState
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
name|plan
operator|.
name|MapWork
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
name|plan
operator|.
name|MapredWork
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
name|Utils
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
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|ArgumentCaptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_class
specifier|public
class|class
name|TestMapRedTask
block|{
annotation|@
name|Test
specifier|public
name|void
name|mrTask_updates_Metrics
parameter_list|()
throws|throws
name|IOException
block|{
name|Metrics
name|mockMetrics
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Metrics
operator|.
name|class
argument_list|)
decl_stmt|;
name|MapRedTask
name|mapRedTask
init|=
operator|new
name|MapRedTask
argument_list|()
decl_stmt|;
name|mapRedTask
operator|.
name|updateTaskMetrics
argument_list|(
name|mockMetrics
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockMetrics
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|incrementCounter
argument_list|(
name|MetricsConstant
operator|.
name|HIVE_MR_TASKS
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockMetrics
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|incrementCounter
argument_list|(
name|MetricsConstant
operator|.
name|HIVE_TEZ_TASKS
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockMetrics
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|incrementCounter
argument_list|(
name|MetricsConstant
operator|.
name|HIVE_SPARK_TASKS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|mrTaskSumbitViaChildWithImpersonation
parameter_list|()
throws|throws
name|IOException
throws|,
name|LoginException
block|{
name|Utils
operator|.
name|getUGI
argument_list|()
operator|.
name|setAuthenticationMethod
argument_list|(
name|PROXY
argument_list|)
expr_stmt|;
name|Context
name|ctx
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Context
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|ctx
operator|.
name|getLocalTmpPath
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|TaskQueue
name|taskQueue
init|=
operator|new
name|TaskQueue
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|QueryState
name|queryState
init|=
operator|new
name|QueryState
operator|.
name|Builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|HiveConf
name|conf
init|=
name|queryState
operator|.
name|getConf
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
name|SUBMITVIACHILD
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|MapredWork
name|mrWork
init|=
operator|new
name|MapredWork
argument_list|()
decl_stmt|;
name|mrWork
operator|.
name|setMapWork
argument_list|(
name|Mockito
operator|.
name|mock
argument_list|(
name|MapWork
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|MapRedTask
name|mrTask
init|=
name|Mockito
operator|.
name|spy
argument_list|(
operator|new
name|MapRedTask
argument_list|()
argument_list|)
decl_stmt|;
name|mrTask
operator|.
name|setWork
argument_list|(
name|mrWork
argument_list|)
expr_stmt|;
name|mrTask
operator|.
name|initialize
argument_list|(
name|queryState
argument_list|,
literal|null
argument_list|,
name|taskQueue
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|mrTask
operator|.
name|jobExecHelper
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HadoopJobExecHelper
operator|.
name|class
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mrTask
operator|.
name|jobExecHelper
operator|.
name|progressLocal
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|Process
operator|.
name|class
argument_list|)
argument_list|,
name|Mockito
operator|.
name|anyString
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|mrTask
operator|.
name|execute
argument_list|()
expr_stmt|;
name|ArgumentCaptor
argument_list|<
name|String
index|[]
argument_list|>
name|captor
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|String
index|[]
operator|.
expr|class
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|mrTask
argument_list|)
operator|.
name|spawn
argument_list|(
name|Mockito
operator|.
name|anyString
argument_list|()
argument_list|,
name|Mockito
operator|.
name|anyString
argument_list|()
argument_list|,
name|captor
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|expected
init|=
literal|"HADOOP_PROXY_USER="
operator|+
name|Utils
operator|.
name|getUGI
argument_list|()
operator|.
name|getUserName
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|captor
operator|.
name|getValue
argument_list|()
argument_list|)
operator|.
name|contains
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

