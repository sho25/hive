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
name|WMResourcePlan
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
name|WMTrigger
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
name|TezSessionPoolManager
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
name|junit
operator|.
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
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
name|TestTriggersNoTezSessionPool
extends|extends
name|AbstractJdbcTriggersTest
block|{
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|getTestName
parameter_list|()
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"#"
operator|+
name|testName
operator|.
name|getMethodName
argument_list|()
return|;
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
name|testTriggerSlowQueryExecutionTime
parameter_list|()
throws|throws
name|Exception
block|{
name|Expression
name|expression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"EXECUTION_TIME> 1000"
argument_list|)
decl_stmt|;
name|Trigger
name|trigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"slow_query"
argument_list|,
name|expression
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
name|trigger
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
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
literal|null
argument_list|,
name|trigger
operator|+
literal|" violated"
argument_list|,
literal|50
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
name|testTriggerVertexTotalTasks
parameter_list|()
throws|throws
name|Exception
block|{
name|Expression
name|expression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"VERTEX_TOTAL_TASKS> 20"
argument_list|)
decl_stmt|;
name|Trigger
name|trigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"highly_parallel"
argument_list|,
name|expression
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
name|trigger
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
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|getConfigs
argument_list|()
argument_list|,
name|trigger
operator|+
literal|" violated"
argument_list|,
literal|50
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
name|testTriggerDAGTotalTasks
parameter_list|()
throws|throws
name|Exception
block|{
name|Expression
name|expression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"DAG_TOTAL_TASKS> 20"
argument_list|)
decl_stmt|;
name|Trigger
name|trigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"highly_parallel"
argument_list|,
name|expression
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
name|trigger
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
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|getConfigs
argument_list|()
argument_list|,
name|trigger
operator|+
literal|" violated"
argument_list|,
literal|50
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
name|testTriggerTotalLaunchedTasks
parameter_list|()
throws|throws
name|Exception
block|{
name|Expression
name|expression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"TOTAL_LAUNCHED_TASKS> 20"
argument_list|)
decl_stmt|;
name|Trigger
name|trigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"highly_parallel"
argument_list|,
name|expression
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
name|trigger
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
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|getConfigs
argument_list|()
argument_list|,
name|trigger
operator|+
literal|" violated"
argument_list|,
literal|50
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
block|{
name|WMFullResourcePlan
name|rp
init|=
operator|new
name|WMFullResourcePlan
argument_list|(
operator|new
name|WMResourcePlan
argument_list|(
literal|"rp"
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
for|for
control|(
name|Trigger
name|trigger
range|:
name|triggers
control|)
block|{
name|WMTrigger
name|wmTrigger
init|=
name|wmTriggerFromTrigger
argument_list|(
name|trigger
argument_list|)
decl_stmt|;
name|wmTrigger
operator|.
name|setIsInUnmanaged
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|rp
operator|.
name|addToTriggers
argument_list|(
name|wmTrigger
argument_list|)
expr_stmt|;
block|}
name|TezSessionPoolManager
operator|.
name|getInstance
argument_list|()
operator|.
name|updateTriggers
argument_list|(
name|rp
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

