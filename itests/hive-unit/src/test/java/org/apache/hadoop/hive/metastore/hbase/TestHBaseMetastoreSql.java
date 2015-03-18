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
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|Ignore
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
name|IMockUtils
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
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
name|IMockUtils
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
name|IMockUtils
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
literal|"alter database db set owner user me"
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
name|Ignore
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
name|Ignore
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
name|rsp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"alter table parttbl drop partition (ds = 'whenever')"
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
block|}
end_class

end_unit

