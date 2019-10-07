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
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|TestRemoteHiveMetaStore
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
operator|.
name|ConfVars
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

begin_class
specifier|public
class|class
name|RemoteHiveMetaStoreDualAuthTest
extends|extends
name|TestRemoteHiveMetaStore
block|{
specifier|protected
specifier|static
name|String
name|correctUser
init|=
literal|"correct_user"
decl_stmt|;
specifier|protected
specifier|static
name|String
name|correctPassword
init|=
literal|"correct_passwd"
decl_stmt|;
specifier|protected
specifier|static
name|String
name|wrongUser
init|=
literal|"wrong_user"
decl_stmt|;
specifier|protected
specifier|static
name|String
name|wrongPassword
init|=
literal|"wrong_password"
decl_stmt|;
specifier|private
specifier|static
name|MiniHiveKdc
name|miniKDC
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|static
name|Configuration
name|clientConf
decl_stmt|;
specifier|protected
specifier|static
name|String
name|hiveMetastorePrincipal
decl_stmt|;
specifier|protected
specifier|static
name|String
name|hiveMetastoreKeytab
decl_stmt|;
specifier|protected
specifier|static
name|String
name|wrongKeytab
decl_stmt|;
specifier|protected
specifier|static
name|String
name|wrongPrincipal
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
literal|null
operator|==
name|miniKDC
condition|)
block|{
name|miniKDC
operator|=
operator|new
name|MiniHiveKdc
argument_list|()
expr_stmt|;
name|hiveMetastorePrincipal
operator|=
name|miniKDC
operator|.
name|getFullyQualifiedServicePrincipal
argument_list|(
name|miniKDC
operator|.
name|getHiveMetastoreServicePrincipal
argument_list|()
argument_list|)
expr_stmt|;
name|hiveMetastoreKeytab
operator|=
name|miniKDC
operator|.
name|getKeyTabFile
argument_list|(
name|miniKDC
operator|.
name|getServicePrincipalForUser
argument_list|(
name|miniKDC
operator|.
name|getHiveMetastoreServicePrincipal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|wrongKeytab
operator|=
name|miniKDC
operator|.
name|getKeyTabFile
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_TEST_USER_2
argument_list|)
expr_stmt|;
comment|// We don't expect wrongUser to be part of KDC
name|wrongPrincipal
operator|=
name|miniKDC
operator|.
name|getFullyQualifiedServicePrincipal
argument_list|(
name|wrongUser
argument_list|)
expr_stmt|;
name|initConf
argument_list|()
expr_stmt|;
name|MetastoreConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|EXECUTE_SET_UGI
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|clientConf
operator|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|THRIFT_METASTORE_AUTHENTICATION
argument_list|,
literal|"CONFIG"
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|THRIFT_AUTH_CONFIG_USERNAME
argument_list|,
name|correctUser
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|THRIFT_AUTH_CONFIG_PASSWORD
argument_list|,
name|correctPassword
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|USE_THRIFT_SASL
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|KERBEROS_PRINCIPAL
argument_list|,
name|hiveMetastorePrincipal
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|KERBEROS_KEYTAB_FILE
argument_list|,
name|hiveMetastoreKeytab
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

