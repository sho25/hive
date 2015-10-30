begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|metastore
operator|.
name|hbase
package|;
end_package

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|processors
operator|.
name|CommandProcessorResponse
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
name|Assert
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
name|IOException
import|;
end_import

begin_comment
comment|/**  * Integration tests with HBase Mini-cluster using actual SQL  */
end_comment

begin_class
specifier|public
class|class
name|TestHBaseMetastoreSql
extends|extends
name|HBaseIntegrationTests
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestHBaseStoreIntegration
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|startup
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseIntegrationTests
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|shutdown
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseIntegrationTests
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|IOException
block|{
name|setupConnection
argument_list|()
expr_stmt|;
name|setupDriver
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|insertIntoTable
parameter_list|()
throws|throws
name|Exception
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"create table iit (c int)"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|rsp
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"insert into table iit values (3)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|insertIntoPartitionTable
parameter_list|()
throws|throws
name|Exception
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"create table iipt (c int) partitioned by (ds string)"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|rsp
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"insert into table iipt partition(ds) values (1, 'today'), (2, 'yesterday'),"
operator|+
literal|"(3, 'tomorrow')"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|database
parameter_list|()
throws|throws
name|Exception
block|{
name|CommandProcessorResponse
name|rsp
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"create database db"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"set role admin"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
comment|// security doesn't let me change the properties
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"alter database db set dbproperties ('key' = 'value')"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"drop database db"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|table
parameter_list|()
throws|throws
name|Exception
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"create table tbl (c int)"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|rsp
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"insert into table tbl values (3)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"select * from tbl"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"alter table tbl set tblproperties ('example', 'true')"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"drop table tbl"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|partitionedTable
parameter_list|()
throws|throws
name|Exception
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"create table parttbl (c int) partitioned by (ds string)"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|rsp
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"insert into table parttbl partition(ds) values (1, 'today'), (2, 'yesterday')"
operator|+
literal|", (3, 'tomorrow')"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
comment|// Do it again, to check insert into existing partitions
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"insert into table parttbl partition(ds) values (4, 'today'), (5, 'yesterday')"
operator|+
literal|", (6, 'tomorrow')"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"insert into table parttbl partition(ds = 'someday') values (1)"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"insert into table parttbl partition(ds = 'someday') values (2)"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"alter table parttbl add partition (ds = 'whenever')"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"insert into table parttbl partition(ds = 'whenever') values (2)"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"alter table parttbl touch partition (ds = 'whenever')"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO - Can't do this until getPartitionsByExpr implemented
comment|/*     rsp = driver.run("alter table parttbl drop partition (ds = 'whenever')");     Assert.assertEquals(0, rsp.getResponseCode());     */
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"select * from parttbl"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"select * from parttbl where ds = 'today'"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|role
parameter_list|()
throws|throws
name|Exception
block|{
name|CommandProcessorResponse
name|rsp
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"set role admin"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"create role role1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"grant role1 to user fred with admin option"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"create role role2"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"grant role1 to role role2"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"show principals role1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"show role grant role role1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"show role grant user "
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"show roles"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"revoke admin option for role1 from user fred"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"revoke role1 from user fred"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"revoke role1 from role role2"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"show current roles"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"drop role role2"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"drop role role1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|grant
parameter_list|()
throws|throws
name|Exception
block|{
name|CommandProcessorResponse
name|rsp
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"set role admin"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"create role role3"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create table granttbl (c int)"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"grant select on granttbl to "
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"grant select on granttbl to role3 with grant option"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"revoke select on granttbl from "
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"revoke grant option for select on granttbl from role3"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|describeNonpartitionedTable
parameter_list|()
throws|throws
name|Exception
block|{
name|CommandProcessorResponse
name|rsp
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"create table alter1(a int, b int)"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"describe extended alter1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"alter table alter1 set serdeproperties('s1'='9')"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"describe extended alter1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|alterRenamePartitioned
parameter_list|()
throws|throws
name|Exception
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"create table alterrename (c int) partitioned by (ds string)"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"alter table alterrename add partition (ds = 'a')"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|rsp
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"describe extended alterrename partition (ds='a')"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"alter table alterrename rename to alter_renamed"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"describe extended alter_renamed partition (ds='a')"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"describe extended alterrename partition (ds='a')"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|10001
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|alterRename
parameter_list|()
throws|throws
name|Exception
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"create table alterrename1 (c int)"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|rsp
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"describe alterrename1"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"alter table alterrename1 rename to alter_renamed1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"describe alter_renamed1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"describe alterrename1"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|10001
argument_list|,
name|rsp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

