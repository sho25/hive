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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|sqlstd
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|fs
operator|.
name|Path
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
name|metastore
operator|.
name|IMetaStoreClient
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
name|HiveAuthenticationProvider
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
name|plugin
operator|.
name|HiveAccessControlException
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
name|plugin
operator|.
name|HiveAuthorizationValidator
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
name|plugin
operator|.
name|HiveAuthzPluginException
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
name|plugin
operator|.
name|HiveMetastoreClientFactory
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
name|plugin
operator|.
name|HiveOperationType
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
name|plugin
operator|.
name|HivePrincipal
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
name|plugin
operator|.
name|HivePrincipal
operator|.
name|HivePrincipalType
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
name|plugin
operator|.
name|HivePrivilegeObject
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
name|plugin
operator|.
name|HivePrivilegeObject
operator|.
name|HivePrivilegeObjectType
import|;
end_import

begin_class
specifier|public
class|class
name|SQLStdHiveAuthorizationValidator
implements|implements
name|HiveAuthorizationValidator
block|{
specifier|private
specifier|final
name|HiveMetastoreClientFactory
name|metastoreClientFactory
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|final
name|HiveAuthenticationProvider
name|authenticator
decl_stmt|;
specifier|private
specifier|final
name|SQLStdHiveAccessController
name|privController
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SQLStdHiveAuthorizationValidator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|SQLStdHiveAuthorizationValidator
parameter_list|(
name|HiveMetastoreClientFactory
name|metastoreClientFactory
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|HiveAuthenticationProvider
name|authenticator
parameter_list|,
name|SQLStdHiveAccessController
name|privController
parameter_list|)
block|{
name|this
operator|.
name|metastoreClientFactory
operator|=
name|metastoreClientFactory
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|authenticator
operator|=
name|authenticator
expr_stmt|;
name|this
operator|.
name|privController
operator|=
name|privController
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkPrivileges
parameter_list|(
name|HiveOperationType
name|hiveOpType
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inputHObjs
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|outputHObjs
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|String
name|msg
init|=
literal|"Checking privileges for operation "
operator|+
name|hiveOpType
operator|+
literal|" by user "
operator|+
name|authenticator
operator|.
name|getUserName
argument_list|()
operator|+
literal|" on "
operator|+
literal|" input objects "
operator|+
name|inputHObjs
operator|+
literal|" and output objects "
operator|+
name|outputHObjs
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
name|String
name|userName
init|=
name|authenticator
operator|.
name|getUserName
argument_list|()
decl_stmt|;
name|IMetaStoreClient
name|metastoreClient
init|=
name|metastoreClientFactory
operator|.
name|getHiveMetastoreClient
argument_list|()
decl_stmt|;
comment|// get privileges required on input and check
name|SQLPrivTypeGrant
index|[]
name|inputPrivs
init|=
name|Operation2Privilege
operator|.
name|getInputPrivs
argument_list|(
name|hiveOpType
argument_list|)
decl_stmt|;
name|checkPrivileges
argument_list|(
name|inputPrivs
argument_list|,
name|inputHObjs
argument_list|,
name|metastoreClient
argument_list|,
name|userName
argument_list|)
expr_stmt|;
comment|// get privileges required on input and check
name|SQLPrivTypeGrant
index|[]
name|outputPrivs
init|=
name|Operation2Privilege
operator|.
name|getOutputPrivs
argument_list|(
name|hiveOpType
argument_list|)
decl_stmt|;
name|checkPrivileges
argument_list|(
name|outputPrivs
argument_list|,
name|outputHObjs
argument_list|,
name|metastoreClient
argument_list|,
name|userName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkPrivileges
parameter_list|(
name|SQLPrivTypeGrant
index|[]
name|reqPrivs
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|hObjs
parameter_list|,
name|IMetaStoreClient
name|metastoreClient
parameter_list|,
name|String
name|userName
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
name|RequiredPrivileges
name|requiredInpPrivs
init|=
operator|new
name|RequiredPrivileges
argument_list|()
decl_stmt|;
name|requiredInpPrivs
operator|.
name|addAll
argument_list|(
name|reqPrivs
argument_list|)
expr_stmt|;
comment|// check if this user has these privileges on the objects
for|for
control|(
name|HivePrivilegeObject
name|hObj
range|:
name|hObjs
control|)
block|{
name|RequiredPrivileges
name|availPrivs
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|hObj
operator|.
name|getType
argument_list|()
operator|==
name|HivePrivilegeObjectType
operator|.
name|LOCAL_URI
operator|||
name|hObj
operator|.
name|getType
argument_list|()
operator|==
name|HivePrivilegeObjectType
operator|.
name|DFS_URI
condition|)
block|{
name|availPrivs
operator|=
name|SQLAuthorizationUtils
operator|.
name|getPrivilegesFromFS
argument_list|(
operator|new
name|Path
argument_list|(
name|hObj
operator|.
name|getTableViewURI
argument_list|()
argument_list|)
argument_list|,
name|conf
argument_list|,
name|userName
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|hObj
operator|.
name|getType
argument_list|()
operator|==
name|HivePrivilegeObjectType
operator|.
name|PARTITION
condition|)
block|{
comment|// sql std authorization is managing privileges at the table/view levels
comment|// only
comment|// ignore partitions
block|}
else|else
block|{
comment|// get the privileges that this user has on the object
name|availPrivs
operator|=
name|SQLAuthorizationUtils
operator|.
name|getPrivilegesFromMetaStore
argument_list|(
name|metastoreClient
argument_list|,
name|userName
argument_list|,
name|hObj
argument_list|,
name|privController
operator|.
name|getCurrentRoleNames
argument_list|()
argument_list|,
name|privController
operator|.
name|isUserAdmin
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Collection
argument_list|<
name|SQLPrivTypeGrant
argument_list|>
name|missingPriv
init|=
name|requiredInpPrivs
operator|.
name|findMissingPrivs
argument_list|(
name|availPrivs
argument_list|)
decl_stmt|;
name|SQLAuthorizationUtils
operator|.
name|assertNoMissingPrivilege
argument_list|(
name|missingPriv
argument_list|,
operator|new
name|HivePrincipal
argument_list|(
name|userName
argument_list|,
name|HivePrincipalType
operator|.
name|USER
argument_list|)
argument_list|,
name|hObj
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

