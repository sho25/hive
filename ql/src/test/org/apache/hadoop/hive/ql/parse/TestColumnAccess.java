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
name|parse
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
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|CommandNeedRetryException
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
name|Driver
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
name|QueryPlan
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
name|hooks
operator|.
name|ReadEntity
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
name|junit
operator|.
name|AfterClass
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

begin_class
specifier|public
class|class
name|TestColumnAccess
block|{
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|Setup
parameter_list|()
throws|throws
name|CommandNeedRetryException
block|{
name|Driver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|int
name|ret
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"create table t1(id1 int, name1 string)"
argument_list|)
operator|.
name|getResponseCode
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Checking command success"
argument_list|,
literal|0
argument_list|,
name|ret
argument_list|)
expr_stmt|;
name|ret
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"create table t2(id2 int, id1 int, name2 string)"
argument_list|)
operator|.
name|getResponseCode
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Checking command success"
argument_list|,
literal|0
argument_list|,
name|ret
argument_list|)
expr_stmt|;
name|ret
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"create view v1 as select * from t1"
argument_list|)
operator|.
name|getResponseCode
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Checking command success"
argument_list|,
literal|0
argument_list|,
name|ret
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|Teardown
parameter_list|()
throws|throws
name|Exception
block|{
name|Driver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop table t1"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop table t2"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop view v1"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQueryTable1
parameter_list|()
throws|throws
name|ParseException
block|{
name|String
name|query
init|=
literal|"select * from t1"
decl_stmt|;
name|Driver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|int
name|rc
init|=
name|driver
operator|.
name|compile
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Checking command success"
argument_list|,
literal|0
argument_list|,
name|rc
argument_list|)
expr_stmt|;
name|QueryPlan
name|plan
init|=
name|driver
operator|.
name|getPlan
argument_list|()
decl_stmt|;
comment|// check access columns from ColumnAccessInfo
name|ColumnAccessInfo
name|columnAccessInfo
init|=
name|plan
operator|.
name|getColumnAccessInfo
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|cols
init|=
name|columnAccessInfo
operator|.
name|getTableToColumnAccessMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"default@t1"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|cols
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"id1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"name1"
argument_list|)
argument_list|)
expr_stmt|;
comment|// check access columns from readEntity
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableColsMap
init|=
name|getColsFromReadEntity
argument_list|(
name|plan
operator|.
name|getInputs
argument_list|()
argument_list|)
decl_stmt|;
name|cols
operator|=
name|tableColsMap
operator|.
name|get
argument_list|(
literal|"default@t1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|cols
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"id1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"name1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testJoinTable1AndTable2
parameter_list|()
throws|throws
name|ParseException
block|{
name|String
name|query
init|=
literal|"select * from t1 join t2 on (t1.id1 = t2.id1)"
decl_stmt|;
name|Driver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|int
name|rc
init|=
name|driver
operator|.
name|compile
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Checking command success"
argument_list|,
literal|0
argument_list|,
name|rc
argument_list|)
expr_stmt|;
name|QueryPlan
name|plan
init|=
name|driver
operator|.
name|getPlan
argument_list|()
decl_stmt|;
comment|// check access columns from ColumnAccessInfo
name|ColumnAccessInfo
name|columnAccessInfo
init|=
name|plan
operator|.
name|getColumnAccessInfo
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|cols
init|=
name|columnAccessInfo
operator|.
name|getTableToColumnAccessMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"default@t1"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|cols
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"id1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"name1"
argument_list|)
argument_list|)
expr_stmt|;
name|cols
operator|=
name|columnAccessInfo
operator|.
name|getTableToColumnAccessMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"default@t2"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|cols
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"id2"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"id1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"name1"
argument_list|)
argument_list|)
expr_stmt|;
comment|// check access columns from readEntity
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableColsMap
init|=
name|getColsFromReadEntity
argument_list|(
name|plan
operator|.
name|getInputs
argument_list|()
argument_list|)
decl_stmt|;
name|cols
operator|=
name|tableColsMap
operator|.
name|get
argument_list|(
literal|"default@t1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|cols
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"id1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"name1"
argument_list|)
argument_list|)
expr_stmt|;
name|cols
operator|=
name|tableColsMap
operator|.
name|get
argument_list|(
literal|"default@t2"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|cols
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"id2"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"id1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"name1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testJoinView1AndTable2
parameter_list|()
throws|throws
name|ParseException
block|{
name|String
name|query
init|=
literal|"select * from v1 join t2 on (v1.id1 = t2.id1)"
decl_stmt|;
name|Driver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|int
name|rc
init|=
name|driver
operator|.
name|compile
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Checking command success"
argument_list|,
literal|0
argument_list|,
name|rc
argument_list|)
expr_stmt|;
name|QueryPlan
name|plan
init|=
name|driver
operator|.
name|getPlan
argument_list|()
decl_stmt|;
comment|// check access columns from ColumnAccessInfo
name|ColumnAccessInfo
name|columnAccessInfo
init|=
name|plan
operator|.
name|getColumnAccessInfo
argument_list|()
decl_stmt|;
comment|// t1 is inside v1, we should not care about its access info.
name|List
argument_list|<
name|String
argument_list|>
name|cols
init|=
name|columnAccessInfo
operator|.
name|getTableToColumnAccessMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"default@t1"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|cols
argument_list|)
expr_stmt|;
comment|// v1 is top level view, we should care about its access info.
name|cols
operator|=
name|columnAccessInfo
operator|.
name|getTableToColumnAccessMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"default@v1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|cols
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"id1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"name1"
argument_list|)
argument_list|)
expr_stmt|;
name|cols
operator|=
name|columnAccessInfo
operator|.
name|getTableToColumnAccessMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"default@t2"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|cols
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"id2"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"id1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"name1"
argument_list|)
argument_list|)
expr_stmt|;
comment|// check access columns from readEntity
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableColsMap
init|=
name|getColsFromReadEntity
argument_list|(
name|plan
operator|.
name|getInputs
argument_list|()
argument_list|)
decl_stmt|;
name|cols
operator|=
name|tableColsMap
operator|.
name|get
argument_list|(
literal|"default@t1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|cols
operator|=
name|tableColsMap
operator|.
name|get
argument_list|(
literal|"default@v1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|cols
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"id1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"name1"
argument_list|)
argument_list|)
expr_stmt|;
name|cols
operator|=
name|tableColsMap
operator|.
name|get
argument_list|(
literal|"default@t2"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|cols
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"id2"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"id1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|cols
operator|.
name|contains
argument_list|(
literal|"name1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getColsFromReadEntity
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableColsMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ReadEntity
name|entity
range|:
name|inputs
control|)
block|{
switch|switch
condition|(
name|entity
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|TABLE
case|:
if|if
condition|(
name|entity
operator|.
name|getAccessedColumns
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|entity
operator|.
name|getAccessedColumns
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tableColsMap
operator|.
name|put
argument_list|(
name|entity
operator|.
name|getTable
argument_list|()
operator|.
name|getCompleteName
argument_list|()
argument_list|,
name|entity
operator|.
name|getAccessedColumns
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|PARTITION
case|:
if|if
condition|(
name|entity
operator|.
name|getAccessedColumns
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|entity
operator|.
name|getAccessedColumns
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tableColsMap
operator|.
name|put
argument_list|(
name|entity
operator|.
name|getPartition
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|getCompleteName
argument_list|()
argument_list|,
name|entity
operator|.
name|getAccessedColumns
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
comment|// no-op
block|}
block|}
return|return
name|tableColsMap
return|;
block|}
specifier|private
specifier|static
name|Driver
name|createDriver
parameter_list|()
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|Driver
operator|.
name|class
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
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
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_STATS_COLLECT_SCANCOLS
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Driver
name|driver
init|=
operator|new
name|Driver
argument_list|(
name|conf
argument_list|)
decl_stmt|;
return|return
name|driver
return|;
block|}
block|}
end_class

end_unit

