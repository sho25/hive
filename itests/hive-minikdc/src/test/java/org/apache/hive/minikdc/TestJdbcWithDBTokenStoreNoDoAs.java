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
name|jdbc
operator|.
name|miniHS2
operator|.
name|MiniHS2
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

begin_comment
comment|/**  * Runs the tests defined in TestJdbcWithMiniKdc when DBTokenStore  * is configured and HMS is setup in a remote secure mode and  * impersonation is turned OFF  */
end_comment

begin_class
specifier|public
class|class
name|TestJdbcWithDBTokenStoreNoDoAs
extends|extends
name|TestJdbcWithMiniKdc
block|{
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
name|Class
operator|.
name|forName
argument_list|(
name|MiniHS2
operator|.
name|getJdbcDriverName
argument_list|()
argument_list|)
expr_stmt|;
name|confOverlay
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_SESSION_HOOK
operator|.
name|varname
argument_list|,
name|SessionHookTest
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|miniHiveKdc
operator|=
operator|new
name|MiniHiveKdc
argument_list|()
expr_stmt|;
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|METASTORE_CLUSTER_DELEGATION_TOKEN_STORE_CLS
argument_list|,
literal|"org.apache.hadoop.hive.thrift.DBTokenStore"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|miniHS2
operator|=
name|MiniHiveKdc
operator|.
name|getMiniHS2WithKerbWithRemoteHMS
argument_list|(
name|miniHiveKdc
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
name|String
name|metastorePrincipal
init|=
name|miniHS2
operator|.
name|getConfProperty
argument_list|(
name|ConfVars
operator|.
name|METASTORE_KERBEROS_PRINCIPAL
operator|.
name|varname
argument_list|)
decl_stmt|;
name|String
name|hs2Principal
init|=
name|miniHS2
operator|.
name|getConfProperty
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_KERBEROS_PRINCIPAL
operator|.
name|varname
argument_list|)
decl_stmt|;
name|String
name|hs2KeyTab
init|=
name|miniHS2
operator|.
name|getConfProperty
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_KERBEROS_KEYTAB
operator|.
name|varname
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"HS2 principal : "
operator|+
name|hs2Principal
operator|+
literal|" HS2 keytab : "
operator|+
name|hs2KeyTab
operator|+
literal|" Metastore principal : "
operator|+
name|metastorePrincipal
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

