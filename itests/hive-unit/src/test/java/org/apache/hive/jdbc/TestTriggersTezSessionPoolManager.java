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
name|TestTriggersTezSessionPoolManager
extends|extends
name|AbstractJdbcTriggersTest
block|{
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testTriggerSlowQueryElapsedTime
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
literal|"ELAPSED_TIME> 20000"
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
literal|"select sleep(t1.under_col, 500), t1.value from "
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
name|testTriggerShortQueryElapsedTime
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
literal|"ELAPSED_TIME> 100"
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
literal|"select sleep(t1.under_col, 500), t1.value from "
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
name|testTriggerHighShuffleBytes
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
literal|"SHUFFLE_BYTES> 100"
argument_list|)
decl_stmt|;
name|Trigger
name|trigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"big_shuffle"
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
name|List
argument_list|<
name|String
argument_list|>
name|cmds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|cmds
operator|.
name|add
argument_list|(
literal|"set hive.auto.convert.join=false"
argument_list|)
expr_stmt|;
comment|// to slow down the reducer so that SHUFFLE_BYTES publishing and validation can happen, adding sleep between
comment|// multiple reduce stages
name|String
name|query
init|=
literal|"select count(distinct t.under_col), sleep(t.under_col, 10) from (select t1.under_col from "
operator|+
name|tableName
operator|+
literal|" t1 "
operator|+
literal|"join "
operator|+
name|tableName
operator|+
literal|" t2 on t1.under_col=t2.under_col order by sleep(t1.under_col, 0))"
operator|+
literal|" t group by t.under_col"
decl_stmt|;
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|cmds
argument_list|,
name|trigger
operator|+
literal|" violated"
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
name|testTriggerHighBytesRead
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
literal|"HDFS_BYTES_READ> 100"
argument_list|)
decl_stmt|;
name|Trigger
name|trigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"big_read"
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
name|testTriggerHighBytesWrite
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
literal|"FILE_BYTES_WRITTEN> 100"
argument_list|)
decl_stmt|;
name|Trigger
name|trigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"big_write"
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
name|testTriggerTotalTasks
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
literal|"VERTEX_TOTAL_TASKS> 50"
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
name|testTriggerDagTotalTasks
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
literal|"DAG_TOTAL_TASKS> 50"
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
name|testTriggerCustomReadOps
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
literal|"HDFS_READ_OPS> 50"
argument_list|)
decl_stmt|;
name|Trigger
name|trigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"high_read_ops"
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|120000
argument_list|)
specifier|public
name|void
name|testTriggerCustomCreatedFiles
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|cmds
init|=
name|getConfigs
argument_list|()
decl_stmt|;
name|Expression
name|expression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"CREATED_FILES> 5"
argument_list|)
decl_stmt|;
name|Trigger
name|trigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"high_read_ops"
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
literal|"create table testtab2 as select * from "
operator|+
name|tableName
decl_stmt|;
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|cmds
argument_list|,
name|trigger
operator|+
literal|" violated"
argument_list|)
expr_stmt|;
comment|// partitioned insert
name|expression
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"CREATED_FILES> 10"
argument_list|)
expr_stmt|;
name|trigger
operator|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"high_read_ops"
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
expr_stmt|;
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
name|cmds
operator|.
name|add
argument_list|(
literal|"drop table src3"
argument_list|)
expr_stmt|;
name|cmds
operator|.
name|add
argument_list|(
literal|"create table src3 (key int) partitioned by (value string)"
argument_list|)
expr_stmt|;
name|query
operator|=
literal|"insert overwrite table src3 partition (value) select sleep(under_col, 10), value from "
operator|+
name|tableName
operator|+
literal|" where under_col< 100"
expr_stmt|;
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|cmds
argument_list|,
name|trigger
operator|+
literal|" violated"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|240000
argument_list|)
specifier|public
name|void
name|testTriggerCustomCreatedDynamicPartitions
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|cmds
init|=
name|getConfigs
argument_list|()
decl_stmt|;
name|cmds
operator|.
name|add
argument_list|(
literal|"drop table src2"
argument_list|)
expr_stmt|;
name|cmds
operator|.
name|add
argument_list|(
literal|"create table src2 (key int) partitioned by (value string)"
argument_list|)
expr_stmt|;
comment|// query will get cancelled before creating 57 partitions
name|String
name|query
init|=
literal|"insert overwrite table src2 partition (value) select * from "
operator|+
name|tableName
operator|+
literal|" where under_col< 100"
decl_stmt|;
name|Expression
name|expression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"CREATED_DYNAMIC_PARTITIONS> 20"
argument_list|)
decl_stmt|;
name|Trigger
name|trigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"high_read_ops"
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
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|cmds
argument_list|,
name|trigger
operator|+
literal|" violated"
argument_list|)
expr_stmt|;
name|cmds
operator|=
name|getConfigs
argument_list|()
expr_stmt|;
comment|// let it create 57 partitions without any triggers
name|query
operator|=
literal|"insert overwrite table src2 partition (value) select under_col, value from "
operator|+
name|tableName
operator|+
literal|" where under_col< 100"
expr_stmt|;
name|setupTriggers
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|cmds
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// query will try to add 64 more partitions to already existing 57 partitions but will get cancelled for violation
name|query
operator|=
literal|"insert into table src2 partition (value) select * from "
operator|+
name|tableName
operator|+
literal|" where under_col< 200"
expr_stmt|;
name|expression
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"CREATED_DYNAMIC_PARTITIONS> 30"
argument_list|)
expr_stmt|;
name|trigger
operator|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"high_read_ops"
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
expr_stmt|;
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
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|cmds
argument_list|,
name|trigger
operator|+
literal|" violated"
argument_list|)
expr_stmt|;
comment|// let it create 64 more partitions (total 57 + 64 = 121) without any triggers
name|query
operator|=
literal|"insert into table src2 partition (value) select * from "
operator|+
name|tableName
operator|+
literal|" where under_col< 200"
expr_stmt|;
name|setupTriggers
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|)
expr_stmt|;
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|cmds
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// re-run insert into but this time no new partitions will be created, so there will be no violation
name|query
operator|=
literal|"insert into table src2 partition (value) select * from "
operator|+
name|tableName
operator|+
literal|" where under_col< 200"
expr_stmt|;
name|expression
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"CREATED_DYNAMIC_PARTITIONS> 10"
argument_list|)
expr_stmt|;
name|trigger
operator|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"high_read_ops"
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
expr_stmt|;
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
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|cmds
argument_list|,
literal|null
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
name|testTriggerCustomCreatedDynamicPartitionsMultiInsert
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|cmds
init|=
name|getConfigs
argument_list|()
decl_stmt|;
name|cmds
operator|.
name|add
argument_list|(
literal|"drop table src2"
argument_list|)
expr_stmt|;
name|cmds
operator|.
name|add
argument_list|(
literal|"drop table src3"
argument_list|)
expr_stmt|;
name|cmds
operator|.
name|add
argument_list|(
literal|"create table src2 (key int) partitioned by (value string)"
argument_list|)
expr_stmt|;
name|cmds
operator|.
name|add
argument_list|(
literal|"create table src3 (key int) partitioned by (value string)"
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"from "
operator|+
name|tableName
operator|+
literal|" insert overwrite table src2 partition (value) select * where under_col< 100 "
operator|+
literal|" insert overwrite table src3 partition (value) select * where under_col>= 100 and under_col< 200"
decl_stmt|;
name|Expression
name|expression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"CREATED_DYNAMIC_PARTITIONS> 70"
argument_list|)
decl_stmt|;
name|Trigger
name|trigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"high_partitions"
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
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|cmds
argument_list|,
name|trigger
operator|+
literal|" violated"
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
name|testTriggerCustomCreatedDynamicPartitionsUnionAll
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|cmds
init|=
name|getConfigs
argument_list|()
decl_stmt|;
name|cmds
operator|.
name|add
argument_list|(
literal|"drop table src2"
argument_list|)
expr_stmt|;
name|cmds
operator|.
name|add
argument_list|(
literal|"create table src2 (key int) partitioned by (value string)"
argument_list|)
expr_stmt|;
comment|// query will get cancelled before creating 57 partitions
name|String
name|query
init|=
literal|"insert overwrite table src2 partition (value) "
operator|+
literal|"select temps.* from ("
operator|+
literal|"select * from "
operator|+
name|tableName
operator|+
literal|" where under_col< 100 "
operator|+
literal|"union all "
operator|+
literal|"select * from "
operator|+
name|tableName
operator|+
literal|" where under_col>= 100 and under_col< 200) temps"
decl_stmt|;
name|Expression
name|expression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"CREATED_DYNAMIC_PARTITIONS> 70"
argument_list|)
decl_stmt|;
name|Trigger
name|trigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"high_partitions"
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
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
name|cmds
argument_list|,
name|trigger
operator|+
literal|" violated"
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
name|testTriggerCustomNonExistent
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
literal|"OPEN_FILES> 50"
argument_list|)
decl_stmt|;
name|Trigger
name|trigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"non_existent"
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
literal|"select l.under_col, l.value from "
operator|+
name|tableName
operator|+
literal|" l join "
operator|+
name|tableName
operator|+
literal|" r on l.under_col>=r.under_col"
decl_stmt|;
name|runQueryWithTrigger
argument_list|(
name|query
argument_list|,
literal|null
argument_list|,
literal|null
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
name|testTriggerDagRawInputSplitsKill
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Map 1 - 55 splits
comment|// Map 3 - 55 splits
name|Expression
name|expression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"DAG_RAW_INPUT_SPLITS> 100"
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
literal|"select t1.under_col, t1.value from "
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
literal|"Query was cancelled"
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
name|testTriggerVertexRawInputSplitsNoKill
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Map 1 - 55 splits
comment|// Map 3 - 55 splits
name|Expression
name|expression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"VERTEX_RAW_INPUT_SPLITS> 100"
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
literal|"select t1.under_col, t1.value from "
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
literal|null
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
name|testTriggerVertexRawInputSplitsKill
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Map 1 - 55 splits
comment|// Map 3 - 55 splits
name|Expression
name|expression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"VERTEX_RAW_INPUT_SPLITS> 50"
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
literal|"select t1.under_col, t1.value from "
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
literal|"Query was cancelled"
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
name|testTriggerDefaultRawInputSplits
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Map 1 - 55 splits
comment|// Map 3 - 55 splits
name|Expression
name|expression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"RAW_INPUT_SPLITS> 50"
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
literal|"select t1.under_col, t1.value from "
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
literal|"Query was cancelled"
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
name|testMultipleTriggers1
parameter_list|()
throws|throws
name|Exception
block|{
name|Expression
name|shuffleExpression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"HDFS_BYTES_READ> 1000000"
argument_list|)
decl_stmt|;
name|Trigger
name|shuffleTrigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"big_shuffle"
argument_list|,
name|shuffleExpression
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
name|Expression
name|execTimeExpression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"EXECUTION_TIME> 1000"
argument_list|)
decl_stmt|;
name|Trigger
name|execTimeTrigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"slow_query"
argument_list|,
name|execTimeExpression
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
name|shuffleTrigger
argument_list|,
name|execTimeTrigger
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
name|execTimeTrigger
operator|+
literal|" violated"
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
name|testMultipleTriggers2
parameter_list|()
throws|throws
name|Exception
block|{
name|Expression
name|shuffleExpression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"HDFS_BYTES_READ> 100"
argument_list|)
decl_stmt|;
name|Trigger
name|shuffleTrigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"big_shuffle"
argument_list|,
name|shuffleExpression
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
name|Expression
name|execTimeExpression
init|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"EXECUTION_TIME> 100000"
argument_list|)
decl_stmt|;
name|Trigger
name|execTimeTrigger
init|=
operator|new
name|ExecutionTrigger
argument_list|(
literal|"slow_query"
argument_list|,
name|execTimeExpression
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
name|shuffleTrigger
argument_list|,
name|execTimeTrigger
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
name|shuffleTrigger
operator|+
literal|" violated"
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

