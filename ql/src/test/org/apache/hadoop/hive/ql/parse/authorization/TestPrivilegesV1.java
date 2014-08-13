begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|authorization
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
name|metadata
operator|.
name|Hive
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
name|metadata
operator|.
name|Partition
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
name|metadata
operator|.
name|Table
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
name|security
operator|.
name|authorization
operator|.
name|PrivilegeType
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
name|Before
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
name|Mockito
import|;
end_import

begin_class
specifier|public
class|class
name|TestPrivilegesV1
extends|extends
name|PrivilegesTestBase
block|{
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|Hive
name|db
decl_stmt|;
specifier|private
name|Table
name|table
decl_stmt|;
specifier|private
name|Partition
name|partition
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|db
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Hive
operator|.
name|class
argument_list|)
expr_stmt|;
name|table
operator|=
operator|new
name|Table
argument_list|(
name|DB
argument_list|,
name|TABLE
argument_list|)
expr_stmt|;
name|partition
operator|=
operator|new
name|Partition
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|db
operator|.
name|getTable
argument_list|(
name|DB
argument_list|,
name|TABLE
argument_list|,
literal|false
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|db
operator|.
name|getTable
argument_list|(
name|TABLE_QNAME
argument_list|,
literal|false
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|db
operator|.
name|getPartition
argument_list|(
name|table
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|,
literal|false
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|partition
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check acceptable privileges in grant statement    * @return    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testPrivInGrant
parameter_list|()
throws|throws
name|Exception
block|{
name|grantUserTable
argument_list|(
literal|"all"
argument_list|,
name|PrivilegeType
operator|.
name|ALL
argument_list|)
expr_stmt|;
name|grantUserTable
argument_list|(
literal|"update"
argument_list|,
name|PrivilegeType
operator|.
name|ALTER_DATA
argument_list|)
expr_stmt|;
name|grantUserTable
argument_list|(
literal|"alter"
argument_list|,
name|PrivilegeType
operator|.
name|ALTER_METADATA
argument_list|)
expr_stmt|;
name|grantUserTable
argument_list|(
literal|"create"
argument_list|,
name|PrivilegeType
operator|.
name|CREATE
argument_list|)
expr_stmt|;
name|grantUserTable
argument_list|(
literal|"drop"
argument_list|,
name|PrivilegeType
operator|.
name|DROP
argument_list|)
expr_stmt|;
name|grantUserTable
argument_list|(
literal|"index"
argument_list|,
name|PrivilegeType
operator|.
name|INDEX
argument_list|)
expr_stmt|;
name|grantUserTable
argument_list|(
literal|"lock"
argument_list|,
name|PrivilegeType
operator|.
name|LOCK
argument_list|)
expr_stmt|;
name|grantUserTable
argument_list|(
literal|"select"
argument_list|,
name|PrivilegeType
operator|.
name|SELECT
argument_list|)
expr_stmt|;
name|grantUserTable
argument_list|(
literal|"show_database"
argument_list|,
name|PrivilegeType
operator|.
name|SHOW_DATABASE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check acceptable privileges in grant statement    * @return    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testPrivInGrantNotAccepted
parameter_list|()
throws|throws
name|Exception
block|{
name|grantUserTableFail
argument_list|(
literal|"insert"
argument_list|)
expr_stmt|;
name|grantUserTableFail
argument_list|(
literal|"delete"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|grantUserTableFail
parameter_list|(
name|String
name|privName
parameter_list|)
block|{
try|try
block|{
name|grantUserTable
argument_list|(
name|privName
argument_list|,
name|PrivilegeType
operator|.
name|UNKNOWN
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Exception expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{      }
block|}
specifier|private
name|void
name|grantUserTable
parameter_list|(
name|String
name|privName
parameter_list|,
name|PrivilegeType
name|privType
parameter_list|)
throws|throws
name|Exception
block|{
name|grantUserTable
argument_list|(
name|privName
argument_list|,
name|privType
argument_list|,
name|conf
argument_list|,
name|db
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

