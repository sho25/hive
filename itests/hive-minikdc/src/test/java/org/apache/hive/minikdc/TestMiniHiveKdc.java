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
name|hive
operator|.
name|minikdc
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
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
name|shims
operator|.
name|Utils
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
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|io
operator|.
name|Files
import|;
end_import

begin_class
specifier|public
class|class
name|TestMiniHiveKdc
block|{
specifier|private
specifier|static
name|File
name|baseDir
decl_stmt|;
specifier|private
name|MiniHiveKdc
name|miniHiveKdc
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
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
name|baseDir
operator|=
name|Files
operator|.
name|createTempDir
argument_list|()
expr_stmt|;
name|baseDir
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|miniHiveKdc
operator|=
name|MiniHiveKdc
operator|.
name|getMiniHiveKdc
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|miniHiveKdc
operator|.
name|shutDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLogin
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|servicePrinc
init|=
name|miniHiveKdc
operator|.
name|getHiveServicePrincipal
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|servicePrinc
argument_list|)
expr_stmt|;
name|miniHiveKdc
operator|.
name|loginUser
argument_list|(
name|servicePrinc
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|UserGroupInformation
operator|.
name|isLoginKeytabBased
argument_list|()
argument_list|)
expr_stmt|;
name|UserGroupInformation
name|ugi
init|=
name|Utils
operator|.
name|getUGIForConf
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_SERVICE_PRINCIPAL
argument_list|,
name|ugi
operator|.
name|getShortUserName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterTest
parameter_list|()
throws|throws
name|Exception
block|{    }
block|}
end_class

end_unit

