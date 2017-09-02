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
name|assertNotNull
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|List
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|minikdc
operator|.
name|MiniKdc
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
name|GroupMappingServiceProvider
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

begin_comment
comment|/**  * Wrapper around Hadoop's MiniKdc for use in hive tests.  * Has functions to manager users and their keytabs. This includes a hive service principal,  * a superuser principal for testing proxy user privilegs.  * Has a set of default users that it initializes.  * See hive-minikdc/src/test/resources/core-site.xml for users granted proxy user privileges.  */
end_comment

begin_class
specifier|public
class|class
name|MiniHiveKdc
block|{
specifier|public
specifier|static
name|String
name|HIVE_SERVICE_PRINCIPAL
init|=
literal|"hive"
decl_stmt|;
specifier|public
specifier|static
name|String
name|HIVE_TEST_USER_1
init|=
literal|"user1"
decl_stmt|;
specifier|public
specifier|static
name|String
name|HIVE_TEST_USER_2
init|=
literal|"user2"
decl_stmt|;
specifier|public
specifier|static
name|String
name|HIVE_TEST_SUPER_USER
init|=
literal|"superuser"
decl_stmt|;
specifier|public
specifier|static
name|String
name|AUTHENTICATION_TYPE
init|=
literal|"KERBEROS"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_METASTORE_SERVICE_PRINCIPAL
init|=
literal|"hive"
decl_stmt|;
specifier|private
specifier|final
name|MiniKdc
name|miniKdc
decl_stmt|;
specifier|private
specifier|final
name|File
name|workDir
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|userPrincipals
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Properties
name|kdcConf
init|=
name|MiniKdc
operator|.
name|createConf
argument_list|()
decl_stmt|;
specifier|private
name|int
name|keyTabCounter
init|=
literal|1
decl_stmt|;
comment|// hadoop group mapping that maps user to same group
specifier|public
specifier|static
class|class
name|HiveTestSimpleGroupMapping
implements|implements
name|GroupMappingServiceProvider
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getGroups
parameter_list|(
name|String
name|user
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|results
operator|.
name|add
argument_list|(
name|user
argument_list|)
expr_stmt|;
return|return
name|results
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cacheGroupsRefresh
parameter_list|()
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|cacheGroupsAdd
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|groups
parameter_list|)
throws|throws
name|IOException
block|{     }
block|}
specifier|public
specifier|static
name|MiniHiveKdc
name|getMiniHiveKdc
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
return|return
operator|new
name|MiniHiveKdc
argument_list|(
name|conf
argument_list|)
return|;
block|}
specifier|public
name|MiniHiveKdc
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|File
name|baseDir
init|=
name|Files
operator|.
name|createTempDir
argument_list|()
decl_stmt|;
name|baseDir
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|workDir
operator|=
operator|new
name|File
argument_list|(
name|baseDir
argument_list|,
literal|"HiveMiniKdc"
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
comment|/**      *  Hadoop security classes read the default realm via static initialization,      *  before miniKdc is initialized. Hence we set the realm via a test configuration      *  and propagate that to miniKdc.      */
name|assertNotNull
argument_list|(
literal|"java.security.krb5.conf is needed for hadoop security"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.security.krb5.conf"
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|clearProperty
argument_list|(
literal|"java.security.krb5.conf"
argument_list|)
expr_stmt|;
name|miniKdc
operator|=
operator|new
name|MiniKdc
argument_list|(
name|kdcConf
argument_list|,
operator|new
name|File
argument_list|(
name|workDir
argument_list|,
literal|"miniKdc"
argument_list|)
argument_list|)
expr_stmt|;
name|miniKdc
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// create default users
name|addUserPrincipal
argument_list|(
name|getServicePrincipalForUser
argument_list|(
name|HIVE_SERVICE_PRINCIPAL
argument_list|)
argument_list|)
expr_stmt|;
name|addUserPrincipal
argument_list|(
name|HIVE_TEST_USER_1
argument_list|)
expr_stmt|;
name|addUserPrincipal
argument_list|(
name|HIVE_TEST_USER_2
argument_list|)
expr_stmt|;
name|addUserPrincipal
argument_list|(
name|HIVE_TEST_SUPER_USER
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getKeyTabFile
parameter_list|(
name|String
name|principalName
parameter_list|)
block|{
return|return
name|userPrincipals
operator|.
name|get
argument_list|(
name|principalName
argument_list|)
return|;
block|}
specifier|public
name|void
name|shutDown
parameter_list|()
block|{
name|miniKdc
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|addUserPrincipal
parameter_list|(
name|String
name|principal
parameter_list|)
throws|throws
name|Exception
block|{
name|File
name|keytab
init|=
operator|new
name|File
argument_list|(
name|workDir
argument_list|,
literal|"miniKdc"
operator|+
name|keyTabCounter
operator|++
operator|+
literal|".keytab"
argument_list|)
decl_stmt|;
name|miniKdc
operator|.
name|createPrincipal
argument_list|(
name|keytab
argument_list|,
name|principal
argument_list|)
expr_stmt|;
name|userPrincipals
operator|.
name|put
argument_list|(
name|principal
argument_list|,
name|keytab
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Login the given principal, using corresponding keytab file from internal map    * @param principal    * @return    * @throws Exception    */
specifier|public
name|UserGroupInformation
name|loginUser
parameter_list|(
name|String
name|principal
parameter_list|)
throws|throws
name|Exception
block|{
name|UserGroupInformation
operator|.
name|loginUserFromKeytab
argument_list|(
name|principal
argument_list|,
name|getKeyTabFile
argument_list|(
name|principal
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|Utils
operator|.
name|getUGI
argument_list|()
return|;
block|}
specifier|public
name|Properties
name|getKdcConf
parameter_list|()
block|{
return|return
name|kdcConf
return|;
block|}
specifier|public
name|String
name|getFullyQualifiedUserPrincipal
parameter_list|(
name|String
name|shortUserName
parameter_list|)
block|{
return|return
name|shortUserName
operator|+
literal|"@"
operator|+
name|miniKdc
operator|.
name|getRealm
argument_list|()
return|;
block|}
specifier|public
name|String
name|getFullyQualifiedServicePrincipal
parameter_list|(
name|String
name|shortUserName
parameter_list|)
block|{
return|return
name|getServicePrincipalForUser
argument_list|(
name|shortUserName
argument_list|)
operator|+
literal|"@"
operator|+
name|miniKdc
operator|.
name|getRealm
argument_list|()
return|;
block|}
specifier|public
name|String
name|getServicePrincipalForUser
parameter_list|(
name|String
name|shortUserName
parameter_list|)
block|{
return|return
name|shortUserName
operator|+
literal|"/"
operator|+
name|miniKdc
operator|.
name|getHost
argument_list|()
return|;
block|}
specifier|public
name|String
name|getHiveServicePrincipal
parameter_list|()
block|{
return|return
name|getServicePrincipalForUser
argument_list|(
name|HIVE_SERVICE_PRINCIPAL
argument_list|)
return|;
block|}
specifier|public
name|String
name|getFullHiveServicePrincipal
parameter_list|()
block|{
return|return
name|getServicePrincipalForUser
argument_list|(
name|HIVE_SERVICE_PRINCIPAL
argument_list|)
operator|+
literal|"@"
operator|+
name|miniKdc
operator|.
name|getRealm
argument_list|()
return|;
block|}
specifier|public
name|String
name|getDefaultUserPrincipal
parameter_list|()
block|{
return|return
name|HIVE_TEST_USER_1
return|;
block|}
comment|/**    * Create a MiniHS2 with the hive service principal and keytab in MiniHiveKdc    * @param miniHiveKdc    * @param hiveConf    * @return new MiniHS2 instance    * @throws Exception    */
specifier|public
specifier|static
name|MiniHS2
name|getMiniHS2WithKerb
parameter_list|(
name|MiniHiveKdc
name|miniHiveKdc
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|getMiniHS2WithKerb
argument_list|(
name|miniHiveKdc
argument_list|,
name|hiveConf
argument_list|,
name|AUTHENTICATION_TYPE
argument_list|)
return|;
block|}
comment|/**   * Create a MiniHS2 with the hive service principal and keytab in MiniHiveKdc   * @param miniHiveKdc   * @param hiveConf   * @param authType   * @return new MiniHS2 instance   * @throws Exception   */
specifier|public
specifier|static
name|MiniHS2
name|getMiniHS2WithKerb
parameter_list|(
name|MiniHiveKdc
name|miniHiveKdc
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|authType
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|hivePrincipal
init|=
name|miniHiveKdc
operator|.
name|getFullyQualifiedServicePrincipal
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_SERVICE_PRINCIPAL
argument_list|)
decl_stmt|;
name|String
name|hiveKeytab
init|=
name|miniHiveKdc
operator|.
name|getKeyTabFile
argument_list|(
name|miniHiveKdc
operator|.
name|getServicePrincipalForUser
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_SERVICE_PRINCIPAL
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|MiniHS2
operator|.
name|Builder
argument_list|()
operator|.
name|withConf
argument_list|(
name|hiveConf
argument_list|)
operator|.
name|withMiniKdc
argument_list|(
name|hivePrincipal
argument_list|,
name|hiveKeytab
argument_list|)
operator|.
name|withAuthenticationType
argument_list|(
name|authType
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Create a MiniHS2 with the hive service principal and keytab in MiniHiveKdc    * @param miniHiveKdc    * @param hiveConf    * @return new MiniHS2 instance    * @throws Exception    */
specifier|public
specifier|static
name|MiniHS2
name|getMiniHS2WithKerbWithRemoteHMS
parameter_list|(
name|MiniHiveKdc
name|miniHiveKdc
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|getMiniHS2WithKerbWithRemoteHMS
argument_list|(
name|miniHiveKdc
argument_list|,
name|hiveConf
argument_list|,
name|AUTHENTICATION_TYPE
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|MiniHS2
name|getMiniHS2WithKerbWithRemoteHMSWithKerb
parameter_list|(
name|MiniHiveKdc
name|miniHiveKdc
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|getMiniHS2WithKerbWithRemoteHMSWithKerb
argument_list|(
name|miniHiveKdc
argument_list|,
name|hiveConf
argument_list|,
name|AUTHENTICATION_TYPE
argument_list|)
return|;
block|}
comment|/**    * Create a MiniHS2 with the hive service principal and keytab in MiniHiveKdc. It uses remote HMS    * and can support a different Sasl authType. It creates a metastore service principal and keytab    * which can be used for secure HMS    * @param miniHiveKdc    * @param hiveConf    * @param authenticationType    * @return new MiniHS2 instance    * @throws Exception    */
specifier|private
specifier|static
name|MiniHS2
name|getMiniHS2WithKerbWithRemoteHMSWithKerb
parameter_list|(
name|MiniHiveKdc
name|miniHiveKdc
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|authenticationType
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|hivePrincipal
init|=
name|miniHiveKdc
operator|.
name|getFullyQualifiedServicePrincipal
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_SERVICE_PRINCIPAL
argument_list|)
decl_stmt|;
name|String
name|hiveKeytab
init|=
name|miniHiveKdc
operator|.
name|getKeyTabFile
argument_list|(
name|miniHiveKdc
operator|.
name|getServicePrincipalForUser
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_SERVICE_PRINCIPAL
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|hiveMetastorePrincipal
init|=
name|miniHiveKdc
operator|.
name|getFullyQualifiedServicePrincipal
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_METASTORE_SERVICE_PRINCIPAL
argument_list|)
decl_stmt|;
name|String
name|hiveMetastoreKeytab
init|=
name|miniHiveKdc
operator|.
name|getKeyTabFile
argument_list|(
name|miniHiveKdc
operator|.
name|getServicePrincipalForUser
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_METASTORE_SERVICE_PRINCIPAL
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|MiniHS2
operator|.
name|Builder
argument_list|()
operator|.
name|withConf
argument_list|(
name|hiveConf
argument_list|)
operator|.
name|withSecureRemoteMetastore
argument_list|(
name|hiveMetastorePrincipal
argument_list|,
name|hiveMetastoreKeytab
argument_list|)
operator|.
name|withMiniKdc
argument_list|(
name|hivePrincipal
argument_list|,
name|hiveKeytab
argument_list|)
operator|.
name|withAuthenticationType
argument_list|(
name|authenticationType
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Create a MiniHS2 with the hive service principal and keytab in MiniHiveKdc. It uses remote HMS    * and can support a different Sasl authType    * @param miniHiveKdc    * @param hiveConf    * @param authType    * @return new MiniHS2 instance    * @throws Exception    */
specifier|public
specifier|static
name|MiniHS2
name|getMiniHS2WithKerbWithRemoteHMS
parameter_list|(
name|MiniHiveKdc
name|miniHiveKdc
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|authType
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|hivePrincipal
init|=
name|miniHiveKdc
operator|.
name|getFullyQualifiedServicePrincipal
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_SERVICE_PRINCIPAL
argument_list|)
decl_stmt|;
name|String
name|hiveKeytab
init|=
name|miniHiveKdc
operator|.
name|getKeyTabFile
argument_list|(
name|miniHiveKdc
operator|.
name|getServicePrincipalForUser
argument_list|(
name|MiniHiveKdc
operator|.
name|HIVE_SERVICE_PRINCIPAL
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|MiniHS2
operator|.
name|Builder
argument_list|()
operator|.
name|withConf
argument_list|(
name|hiveConf
argument_list|)
operator|.
name|withRemoteMetastore
argument_list|()
operator|.
name|withMiniKdc
argument_list|(
name|hivePrincipal
argument_list|,
name|hiveKeytab
argument_list|)
operator|.
name|withAuthenticationType
argument_list|(
name|authType
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

