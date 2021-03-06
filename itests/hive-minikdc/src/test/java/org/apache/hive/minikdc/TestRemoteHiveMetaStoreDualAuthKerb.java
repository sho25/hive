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
name|metastore
operator|.
name|HiveMetaStoreClient
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
name|MetaStorePasswdAuthenticationProvider
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
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|AuthenticationException
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

begin_class
specifier|public
class|class
name|TestRemoteHiveMetaStoreDualAuthKerb
extends|extends
name|RemoteHiveMetaStoreDualAuthTest
block|{
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|HiveMetaStoreClient
name|createClient
parameter_list|()
throws|throws
name|Exception
block|{
name|boolean
name|gotException
init|=
literal|false
decl_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|clientConf
argument_list|,
name|ConfVars
operator|.
name|THRIFT_URIS
argument_list|,
literal|"thrift://localhost:"
operator|+
name|port
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setBoolVar
argument_list|(
name|clientConf
argument_list|,
name|ConfVars
operator|.
name|USE_THRIFT_SASL
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Trying to log in using wrong username and password should fail
try|try
block|{
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|clientConf
argument_list|,
name|ConfVars
operator|.
name|KERBEROS_KEYTAB_FILE
argument_list|,
name|wrongKeytab
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|clientConf
argument_list|,
name|ConfVars
operator|.
name|KERBEROS_PRINCIPAL
argument_list|,
name|wrongPrincipal
argument_list|)
expr_stmt|;
name|HiveMetaStoreClient
name|tmpClient
init|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|clientConf
argument_list|)
decl_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|gotException
operator|=
literal|true
expr_stmt|;
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
name|gotException
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|clientConf
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
name|clientConf
argument_list|,
name|ConfVars
operator|.
name|KERBEROS_KEYTAB_FILE
argument_list|,
name|hiveMetastoreKeytab
argument_list|)
expr_stmt|;
return|return
operator|new
name|HiveMetaStoreClient
argument_list|(
name|clientConf
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|SimpleAuthenticationProviderImpl
implements|implements
name|MetaStorePasswdAuthenticationProvider
block|{
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|userMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|SimpleAuthenticationProviderImpl
parameter_list|()
block|{
name|init
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|init
parameter_list|()
block|{
name|userMap
operator|.
name|put
argument_list|(
name|correctUser
argument_list|,
name|correctPassword
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|Authenticate
parameter_list|(
name|String
name|user
parameter_list|,
name|String
name|password
parameter_list|)
throws|throws
name|AuthenticationException
block|{
if|if
condition|(
operator|!
name|userMap
operator|.
name|containsKey
argument_list|(
name|user
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AuthenticationException
argument_list|(
literal|"Invalid user : "
operator|+
name|user
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|userMap
operator|.
name|get
argument_list|(
name|user
argument_list|)
operator|.
name|equals
argument_list|(
name|password
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AuthenticationException
argument_list|(
literal|"Invalid passwd : "
operator|+
name|password
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

