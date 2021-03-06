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
name|hive
operator|.
name|minikdc
package|;
end_package

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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|auth
operator|.
name|HiveAuthConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|auth
operator|.
name|HiveAuthFactory
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
name|TestHiveAuthFactory
block|{
specifier|private
specifier|static
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|static
name|MiniHiveKdc
name|miniHiveKdc
init|=
literal|null
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|miniHiveKdc
operator|=
operator|new
name|MiniHiveKdc
argument_list|()
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{   }
comment|/**    * Verify that delegation token manager is started with no exception for MemoryTokenStore    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testStartTokenManagerForMemoryTokenStore
parameter_list|()
throws|throws
name|Exception
block|{
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_AUTHENTICATION
argument_list|,
name|HiveAuthConstants
operator|.
name|AuthTypes
operator|.
name|KERBEROS
operator|.
name|getAuthName
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|principalName
init|=
name|miniHiveKdc
operator|.
name|getFullHiveServicePrincipal
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Principal: "
operator|+
name|principalName
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_KERBEROS_PRINCIPAL
argument_list|,
name|principalName
argument_list|)
expr_stmt|;
name|String
name|keyTabFile
init|=
name|miniHiveKdc
operator|.
name|getKeyTabFile
argument_list|(
name|miniHiveKdc
operator|.
name|getHiveServicePrincipal
argument_list|()
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"keyTabFile: "
operator|+
name|keyTabFile
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|keyTabFile
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_KERBEROS_KEYTAB
argument_list|,
name|keyTabFile
argument_list|)
expr_stmt|;
name|HiveAuthFactory
name|authFactory
init|=
operator|new
name|HiveAuthFactory
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|authFactory
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"org.apache.hadoop.hive.metastore.security.HadoopThriftAuthBridge$Server$TUGIAssumingTransportFactory"
argument_list|,
name|authFactory
operator|.
name|getAuthTransFactory
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that delegation token manager is started with no exception for DBTokenStore    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testStartTokenManagerForDBTokenStore
parameter_list|()
throws|throws
name|Exception
block|{
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_AUTHENTICATION
argument_list|,
name|HiveAuthConstants
operator|.
name|AuthTypes
operator|.
name|KERBEROS
operator|.
name|getAuthName
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|principalName
init|=
name|miniHiveKdc
operator|.
name|getFullHiveServicePrincipal
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Principal: "
operator|+
name|principalName
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_KERBEROS_PRINCIPAL
argument_list|,
name|principalName
argument_list|)
expr_stmt|;
name|String
name|keyTabFile
init|=
name|miniHiveKdc
operator|.
name|getKeyTabFile
argument_list|(
name|miniHiveKdc
operator|.
name|getHiveServicePrincipal
argument_list|()
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"keyTabFile: "
operator|+
name|keyTabFile
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|keyTabFile
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_KERBEROS_KEYTAB
argument_list|,
name|keyTabFile
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|METASTORE_CLUSTER_DELEGATION_TOKEN_STORE_CLS
argument_list|,
literal|"org.apache.hadoop.hive.metastore.security.DBTokenStore"
argument_list|)
expr_stmt|;
name|HiveAuthFactory
name|authFactory
init|=
operator|new
name|HiveAuthFactory
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|authFactory
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"org.apache.hadoop.hive.metastore.security.HadoopThriftAuthBridge$Server$TUGIAssumingTransportFactory"
argument_list|,
name|authFactory
operator|.
name|getAuthTransFactory
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

