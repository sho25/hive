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
name|Map
import|;
end_import

begin_comment
comment|/**  * TestReplScenariosWithStrictManaged - Test all replication scenarios with strict managed enabled  * at source and target.  */
end_comment

begin_class
specifier|public
class|class
name|TestReplScenariosWithStrictManaged
extends|extends
name|BaseReplicationAcrossInstances
block|{
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|classLevelSetup
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|overrides
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|overrides
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_STRICT_MANAGED_TABLES
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|overrides
operator|.
name|put
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CREATE_TABLES_AS_ACID
operator|.
name|getHiveName
argument_list|()
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|overrides
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|overrides
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_MANAGER
operator|.
name|varname
argument_list|,
literal|"org.apache.hadoop.hive.ql.lockmgr.DbTxnManager"
argument_list|)
expr_stmt|;
name|internalBeforeClassSetup
argument_list|(
name|overrides
argument_list|,
name|TestReplScenariosWithStrictManaged
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|dynamicallyConvertManagedToExternalTable
parameter_list|()
throws|throws
name|Throwable
block|{
comment|// All tables are automatically converted to ACID tables when strict managed is enabled.
comment|// Also, it is not possible to convert ACID table to external table.
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table t1 (id int) stored as orc"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into table t1 values (1)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"create table t2 (id int) partitioned by (key int) stored as orc"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into table t2 partition(key=10) values (1)"
argument_list|)
operator|.
name|runFailure
argument_list|(
literal|"alter table t1 set tblproperties('EXTERNAL'='true')"
argument_list|)
operator|.
name|runFailure
argument_list|(
literal|"alter table t1 set tblproperties('EXTERNAL'='true', 'TRANSACTIONAL'='false')"
argument_list|)
operator|.
name|runFailure
argument_list|(
literal|"alter table t2 set tblproperties('EXTERNAL'='true')"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|dynamicallyConvertExternalToManagedTable
parameter_list|()
throws|throws
name|Throwable
block|{
comment|// With Strict managed enabled, it is not possible to convert external table to ACID table.
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|run
argument_list|(
literal|"create external table t1 (id int) stored as orc"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into table t1 values (1)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"create external table t2 (place string) partitioned by (country string)"
argument_list|)
operator|.
name|run
argument_list|(
literal|"insert into table t2 partition(country='india') values ('bangalore')"
argument_list|)
operator|.
name|runFailure
argument_list|(
literal|"alter table t1 set tblproperties('EXTERNAL'='false')"
argument_list|)
operator|.
name|runFailure
argument_list|(
literal|"alter table t1 set tblproperties('EXTERNAL'='false', 'TRANSACTIONAL'='true')"
argument_list|)
operator|.
name|runFailure
argument_list|(
literal|"alter table t2 set tblproperties('EXTERNAL'='false')"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

