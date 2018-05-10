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
name|metadata
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
name|api
operator|.
name|PrincipalType
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
name|DriverFactory
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
name|IDriver
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
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_class
specifier|public
class|class
name|TestAlterTableMetadata
block|{
annotation|@
name|Test
specifier|public
name|void
name|testAlterTableOwner
parameter_list|()
throws|throws
name|HiveException
block|{
comment|/*      * This test verifies that the ALTER TABLE ... SET OWNER command will change the      * owner metadata of the table in HMS.      */
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|IDriver
name|driver
init|=
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|CommandProcessorResponse
name|resp
decl_stmt|;
name|Table
name|table
decl_stmt|;
name|resp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"create table t1(id int)"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
comment|// Changes the owner to a user and verify the change
name|resp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"alter table t1 set owner user u1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
operator|.
name|getTable
argument_list|(
literal|"t1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrincipalType
operator|.
name|USER
argument_list|,
name|table
operator|.
name|getOwnerType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"u1"
argument_list|,
name|table
operator|.
name|getOwner
argument_list|()
argument_list|)
expr_stmt|;
comment|// Changes the owner to a group and verify the change
name|resp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"alter table t1 set owner group g1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
operator|.
name|getTable
argument_list|(
literal|"t1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrincipalType
operator|.
name|GROUP
argument_list|,
name|table
operator|.
name|getOwnerType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"g1"
argument_list|,
name|table
operator|.
name|getOwner
argument_list|()
argument_list|)
expr_stmt|;
comment|// Changes the owner to a role and verify the change
name|resp
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"alter table t1 set owner role r1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
operator|.
name|getTable
argument_list|(
literal|"t1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrincipalType
operator|.
name|ROLE
argument_list|,
name|table
operator|.
name|getOwnerType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"r1"
argument_list|,
name|table
operator|.
name|getOwner
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

