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
name|security
operator|.
name|authorization
operator|.
name|plugin
package|;
end_package

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
name|Warehouse
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
name|Database
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
name|HiveObjectPrivilege
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
name|HiveObjectRef
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
name|HiveObjectType
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
name|metastore
operator|.
name|api
operator|.
name|PrivilegeBag
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
name|PrivilegeGrantInfo
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
name|Role
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
name|RolePrincipalGrant
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
name|HiveException
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
name|parse
operator|.
name|SemanticException
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
name|AuthorizationUtils
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
name|PrivilegeScope
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
name|sqlstd
operator|.
name|SQLStdHiveAccessController
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

begin_class
specifier|public
class|class
name|HiveV1Authorizer
extends|extends
name|AbstractHiveAuthorizer
block|{
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|static
specifier|private
specifier|final
name|String
name|AUTHORIZER
init|=
literal|"v1"
decl_stmt|;
specifier|public
name|HiveV1Authorizer
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
comment|// Leave this ctor around for backward compat.
annotation|@
name|Deprecated
specifier|public
name|HiveV1Authorizer
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Hive
name|hive
parameter_list|)
block|{
name|this
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|VERSION
name|getVersion
parameter_list|()
block|{
return|return
name|VERSION
operator|.
name|V1
return|;
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
name|inputsHObjs
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|outputHObjs
parameter_list|,
name|HiveAuthzContext
name|context
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Should not be called for v1 authorizer"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|grantPrivileges
parameter_list|(
name|List
argument_list|<
name|HivePrincipal
argument_list|>
name|principals
parameter_list|,
name|List
argument_list|<
name|HivePrivilege
argument_list|>
name|privileges
parameter_list|,
name|HivePrivilegeObject
name|privObject
parameter_list|,
name|HivePrincipal
name|grantor
parameter_list|,
name|boolean
name|grantOption
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
try|try
block|{
name|PrivilegeBag
name|privBag
init|=
name|toPrivilegeBag
argument_list|(
name|privileges
argument_list|,
name|privObject
argument_list|,
name|grantor
argument_list|,
name|grantOption
argument_list|,
name|AUTHORIZER
argument_list|)
decl_stmt|;
name|grantOrRevokePrivs
argument_list|(
name|principals
argument_list|,
name|privBag
argument_list|,
literal|true
argument_list|,
name|grantOption
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|revokePrivileges
parameter_list|(
name|List
argument_list|<
name|HivePrincipal
argument_list|>
name|principals
parameter_list|,
name|List
argument_list|<
name|HivePrivilege
argument_list|>
name|privileges
parameter_list|,
name|HivePrivilegeObject
name|privObject
parameter_list|,
name|HivePrincipal
name|grantor
parameter_list|,
name|boolean
name|grantOption
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
try|try
block|{
name|PrivilegeBag
name|privBag
init|=
name|toPrivilegeBag
argument_list|(
name|privileges
argument_list|,
name|privObject
argument_list|,
name|grantor
argument_list|,
name|grantOption
argument_list|,
name|AUTHORIZER
argument_list|)
decl_stmt|;
name|grantOrRevokePrivs
argument_list|(
name|principals
argument_list|,
name|privBag
argument_list|,
literal|false
argument_list|,
name|grantOption
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|grantOrRevokePrivs
parameter_list|(
name|List
argument_list|<
name|HivePrincipal
argument_list|>
name|principals
parameter_list|,
name|PrivilegeBag
name|privBag
parameter_list|,
name|boolean
name|isGrant
parameter_list|,
name|boolean
name|grantOption
parameter_list|)
throws|throws
name|HiveException
block|{
for|for
control|(
name|HivePrincipal
name|principal
range|:
name|principals
control|)
block|{
name|PrincipalType
name|type
init|=
name|AuthorizationUtils
operator|.
name|getThriftPrincipalType
argument_list|(
name|principal
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HiveObjectPrivilege
name|priv
range|:
name|privBag
operator|.
name|getPrivileges
argument_list|()
control|)
block|{
name|priv
operator|.
name|setPrincipalName
argument_list|(
name|principal
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|priv
operator|.
name|setPrincipalType
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
name|Hive
name|hive
init|=
name|Hive
operator|.
name|getWithFastCheck
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|isGrant
condition|)
block|{
name|hive
operator|.
name|grantPrivileges
argument_list|(
name|privBag
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hive
operator|.
name|revokePrivileges
argument_list|(
name|privBag
argument_list|,
name|grantOption
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|PrivilegeBag
name|toPrivilegeBag
parameter_list|(
name|List
argument_list|<
name|HivePrivilege
argument_list|>
name|privileges
parameter_list|,
name|HivePrivilegeObject
name|privObject
parameter_list|,
name|HivePrincipal
name|grantor
parameter_list|,
name|boolean
name|grantOption
parameter_list|,
name|String
name|authorizer
parameter_list|)
throws|throws
name|HiveException
block|{
name|PrivilegeBag
name|privBag
init|=
operator|new
name|PrivilegeBag
argument_list|()
decl_stmt|;
if|if
condition|(
name|privileges
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|privBag
return|;
block|}
name|String
name|grantorName
init|=
name|grantor
operator|.
name|getName
argument_list|()
decl_stmt|;
name|PrincipalType
name|grantorType
init|=
name|AuthorizationUtils
operator|.
name|getThriftPrincipalType
argument_list|(
name|grantor
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|privObject
operator|.
name|getType
argument_list|()
operator|==
literal|null
operator|||
name|privObject
operator|.
name|getType
argument_list|()
operator|==
name|HivePrivilegeObject
operator|.
name|HivePrivilegeObjectType
operator|.
name|GLOBAL
condition|)
block|{
for|for
control|(
name|HivePrivilege
name|priv
range|:
name|privileges
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|columns
init|=
name|priv
operator|.
name|getColumns
argument_list|()
decl_stmt|;
if|if
condition|(
name|columns
operator|!=
literal|null
operator|&&
operator|!
name|columns
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"For user-level privileges, column sets should be null. columns="
operator|+
name|columns
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
name|privBag
operator|.
name|addToPrivileges
argument_list|(
operator|new
name|HiveObjectPrivilege
argument_list|(
operator|new
name|HiveObjectRef
argument_list|(
name|HiveObjectType
operator|.
name|GLOBAL
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|PrivilegeGrantInfo
argument_list|(
name|priv
operator|.
name|getName
argument_list|()
argument_list|,
literal|0
argument_list|,
name|grantor
operator|.
name|getName
argument_list|()
argument_list|,
name|grantorType
argument_list|,
name|grantOption
argument_list|)
argument_list|,
name|authorizer
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|privBag
return|;
block|}
if|if
condition|(
name|privObject
operator|.
name|getPartKeys
argument_list|()
operator|!=
literal|null
operator|&&
name|grantOption
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Grant does not support partition level."
argument_list|)
throw|;
block|}
name|Hive
name|hive
init|=
name|Hive
operator|.
name|getWithFastCheck
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|Database
name|dbObj
init|=
name|hive
operator|.
name|getDatabase
argument_list|(
name|privObject
operator|.
name|getDbname
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|dbObj
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Database "
operator|+
name|privObject
operator|.
name|getDbname
argument_list|()
operator|+
literal|" does not exists"
argument_list|)
throw|;
block|}
name|Table
name|tableObj
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|privObject
operator|.
name|getObjectName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tableObj
operator|=
name|hive
operator|.
name|getTable
argument_list|(
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
name|privObject
operator|.
name|getObjectName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|partValues
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|tableObj
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|(
operator|!
name|tableObj
operator|.
name|isPartitioned
argument_list|()
operator|)
operator|&&
name|privObject
operator|.
name|getPartKeys
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Table is not partitioned, but partition name is present: partSpec="
operator|+
name|privObject
operator|.
name|getPartKeys
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|privObject
operator|.
name|getPartKeys
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|Warehouse
operator|.
name|makeSpecFromValues
argument_list|(
name|tableObj
operator|.
name|getPartitionKeys
argument_list|()
argument_list|,
name|privObject
operator|.
name|getPartKeys
argument_list|()
argument_list|)
decl_stmt|;
name|Partition
name|partObj
init|=
name|hive
operator|.
name|getPartition
argument_list|(
name|tableObj
argument_list|,
name|partSpec
argument_list|,
literal|false
argument_list|)
operator|.
name|getTPartition
argument_list|()
decl_stmt|;
name|partValues
operator|=
name|partObj
operator|.
name|getValues
argument_list|()
expr_stmt|;
block|}
block|}
for|for
control|(
name|HivePrivilege
name|priv
range|:
name|privileges
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|columns
init|=
name|priv
operator|.
name|getColumns
argument_list|()
decl_stmt|;
if|if
condition|(
name|columns
operator|!=
literal|null
operator|&&
operator|!
name|columns
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|priv
operator|.
name|supportsScope
argument_list|(
name|PrivilegeScope
operator|.
name|COLUMN_LEVEL_SCOPE
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|priv
operator|.
name|getName
argument_list|()
operator|+
literal|" does not support column level privilege."
argument_list|)
throw|;
block|}
if|if
condition|(
name|tableObj
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"For user-level/database-level privileges, column sets should be null. columns="
operator|+
name|columns
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|columns
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|privBag
operator|.
name|addToPrivileges
argument_list|(
operator|new
name|HiveObjectPrivilege
argument_list|(
operator|new
name|HiveObjectRef
argument_list|(
name|HiveObjectType
operator|.
name|COLUMN
argument_list|,
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
name|tableObj
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partValues
argument_list|,
name|columns
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|PrivilegeGrantInfo
argument_list|(
name|priv
operator|.
name|getName
argument_list|()
argument_list|,
literal|0
argument_list|,
name|grantorName
argument_list|,
name|grantorType
argument_list|,
name|grantOption
argument_list|)
argument_list|,
name|authorizer
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|tableObj
operator|==
literal|null
condition|)
block|{
name|privBag
operator|.
name|addToPrivileges
argument_list|(
operator|new
name|HiveObjectPrivilege
argument_list|(
operator|new
name|HiveObjectRef
argument_list|(
name|HiveObjectType
operator|.
name|DATABASE
argument_list|,
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|PrivilegeGrantInfo
argument_list|(
name|priv
operator|.
name|getName
argument_list|()
argument_list|,
literal|0
argument_list|,
name|grantorName
argument_list|,
name|grantorType
argument_list|,
name|grantOption
argument_list|)
argument_list|,
name|authorizer
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|partValues
operator|==
literal|null
condition|)
block|{
name|privBag
operator|.
name|addToPrivileges
argument_list|(
operator|new
name|HiveObjectPrivilege
argument_list|(
operator|new
name|HiveObjectRef
argument_list|(
name|HiveObjectType
operator|.
name|TABLE
argument_list|,
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
name|tableObj
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|PrivilegeGrantInfo
argument_list|(
name|priv
operator|.
name|getName
argument_list|()
argument_list|,
literal|0
argument_list|,
name|grantorName
argument_list|,
name|grantorType
argument_list|,
name|grantOption
argument_list|)
argument_list|,
name|authorizer
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|privBag
operator|.
name|addToPrivileges
argument_list|(
operator|new
name|HiveObjectPrivilege
argument_list|(
operator|new
name|HiveObjectRef
argument_list|(
name|HiveObjectType
operator|.
name|PARTITION
argument_list|,
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
name|tableObj
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partValues
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|PrivilegeGrantInfo
argument_list|(
name|priv
operator|.
name|getName
argument_list|()
argument_list|,
literal|0
argument_list|,
name|grantorName
argument_list|,
name|grantorType
argument_list|,
name|grantOption
argument_list|)
argument_list|,
name|authorizer
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|privBag
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|createRole
parameter_list|(
name|String
name|roleName
parameter_list|,
name|HivePrincipal
name|adminGrantor
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
try|try
block|{
name|Hive
name|hive
init|=
name|Hive
operator|.
name|getWithFastCheck
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|hive
operator|.
name|createRole
argument_list|(
name|roleName
argument_list|,
name|adminGrantor
operator|==
literal|null
condition|?
literal|null
else|:
name|adminGrantor
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|dropRole
parameter_list|(
name|String
name|roleName
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
try|try
block|{
name|Hive
name|hive
init|=
name|Hive
operator|.
name|getWithFastCheck
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|hive
operator|.
name|dropRole
argument_list|(
name|roleName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HiveRoleGrant
argument_list|>
name|getPrincipalGrantInfoForRole
parameter_list|(
name|String
name|roleName
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
try|try
block|{
name|Hive
name|hive
init|=
name|Hive
operator|.
name|getWithFastCheck
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
return|return
name|SQLStdHiveAccessController
operator|.
name|getHiveRoleGrants
argument_list|(
name|hive
operator|.
name|getMSC
argument_list|()
argument_list|,
name|roleName
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HiveRoleGrant
argument_list|>
name|getRoleGrantInfoForPrincipal
parameter_list|(
name|HivePrincipal
name|principal
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
name|PrincipalType
name|type
init|=
name|AuthorizationUtils
operator|.
name|getThriftPrincipalType
argument_list|(
name|principal
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|HiveRoleGrant
argument_list|>
name|grants
init|=
operator|new
name|ArrayList
argument_list|<
name|HiveRoleGrant
argument_list|>
argument_list|()
decl_stmt|;
name|Hive
name|hive
init|=
name|Hive
operator|.
name|getWithFastCheck
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
for|for
control|(
name|RolePrincipalGrant
name|grant
range|:
name|hive
operator|.
name|getRoleGrantInfoForPrincipal
argument_list|(
name|principal
operator|.
name|getName
argument_list|()
argument_list|,
name|type
argument_list|)
control|)
block|{
name|grants
operator|.
name|add
argument_list|(
operator|new
name|HiveRoleGrant
argument_list|(
name|grant
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|grants
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|grantRole
parameter_list|(
name|List
argument_list|<
name|HivePrincipal
argument_list|>
name|principals
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|roles
parameter_list|,
name|boolean
name|grantOption
parameter_list|,
name|HivePrincipal
name|grantor
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
try|try
block|{
name|grantOrRevokeRole
argument_list|(
name|principals
argument_list|,
name|roles
argument_list|,
name|grantOption
argument_list|,
name|grantor
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|revokeRole
parameter_list|(
name|List
argument_list|<
name|HivePrincipal
argument_list|>
name|principals
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|roles
parameter_list|,
name|boolean
name|grantOption
parameter_list|,
name|HivePrincipal
name|grantor
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
try|try
block|{
name|grantOrRevokeRole
argument_list|(
name|principals
argument_list|,
name|roles
argument_list|,
name|grantOption
argument_list|,
name|grantor
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|grantOrRevokeRole
parameter_list|(
name|List
argument_list|<
name|HivePrincipal
argument_list|>
name|principals
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|roles
parameter_list|,
name|boolean
name|grantOption
parameter_list|,
name|HivePrincipal
name|grantor
parameter_list|,
name|boolean
name|isGrant
parameter_list|)
throws|throws
name|HiveException
block|{
name|PrincipalType
name|grantorType
init|=
name|AuthorizationUtils
operator|.
name|getThriftPrincipalType
argument_list|(
name|grantor
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
name|Hive
name|hive
init|=
name|Hive
operator|.
name|getWithFastCheck
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
for|for
control|(
name|HivePrincipal
name|principal
range|:
name|principals
control|)
block|{
name|PrincipalType
name|principalType
init|=
name|AuthorizationUtils
operator|.
name|getThriftPrincipalType
argument_list|(
name|principal
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|userName
init|=
name|principal
operator|.
name|getName
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|roleName
range|:
name|roles
control|)
block|{
if|if
condition|(
name|isGrant
condition|)
block|{
name|hive
operator|.
name|grantRole
argument_list|(
name|roleName
argument_list|,
name|userName
argument_list|,
name|principalType
argument_list|,
name|grantor
operator|.
name|getName
argument_list|()
argument_list|,
name|grantorType
argument_list|,
name|grantOption
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hive
operator|.
name|revokeRole
argument_list|(
name|roleName
argument_list|,
name|userName
argument_list|,
name|principalType
argument_list|,
name|grantOption
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllRoles
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
try|try
block|{
name|Hive
name|hive
init|=
name|Hive
operator|.
name|getWithFastCheck
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
return|return
name|hive
operator|.
name|getAllRoleNames
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HivePrivilegeInfo
argument_list|>
name|showPrivileges
parameter_list|(
name|HivePrincipal
name|principal
parameter_list|,
name|HivePrivilegeObject
name|privObj
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
name|String
name|name
init|=
name|principal
operator|==
literal|null
condition|?
literal|null
else|:
name|principal
operator|.
name|getName
argument_list|()
decl_stmt|;
name|PrincipalType
name|type
init|=
name|AuthorizationUtils
operator|.
name|getThriftPrincipalType
argument_list|(
name|principal
operator|==
literal|null
condition|?
literal|null
else|:
name|principal
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HiveObjectPrivilege
argument_list|>
name|privs
init|=
operator|new
name|ArrayList
argument_list|<
name|HiveObjectPrivilege
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|Hive
name|hive
init|=
name|Hive
operator|.
name|getWithFastCheck
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|privObj
operator|==
literal|null
condition|)
block|{
comment|// show user level privileges
name|privs
operator|.
name|addAll
argument_list|(
name|hive
operator|.
name|showPrivilegeGrant
argument_list|(
name|HiveObjectType
operator|.
name|GLOBAL
argument_list|,
name|name
argument_list|,
name|type
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|privObj
operator|.
name|getDbname
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// show all privileges
name|privs
operator|.
name|addAll
argument_list|(
name|hive
operator|.
name|showPrivilegeGrant
argument_list|(
literal|null
argument_list|,
name|name
argument_list|,
name|type
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Database
name|dbObj
init|=
name|hive
operator|.
name|getDatabase
argument_list|(
name|privObj
operator|.
name|getDbname
argument_list|()
argument_list|)
decl_stmt|;
empty_stmt|;
if|if
condition|(
name|dbObj
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Database "
operator|+
name|privObj
operator|.
name|getDbname
argument_list|()
operator|+
literal|" does not exists"
argument_list|)
throw|;
block|}
name|Table
name|tableObj
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|privObj
operator|.
name|getObjectName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tableObj
operator|=
name|hive
operator|.
name|getTable
argument_list|(
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
name|privObj
operator|.
name|getObjectName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|partValues
init|=
name|privObj
operator|.
name|getPartKeys
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableObj
operator|==
literal|null
condition|)
block|{
comment|// show database level privileges
name|privs
operator|.
name|addAll
argument_list|(
name|hive
operator|.
name|showPrivilegeGrant
argument_list|(
name|HiveObjectType
operator|.
name|DATABASE
argument_list|,
name|name
argument_list|,
name|type
argument_list|,
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|String
argument_list|>
name|columns
init|=
name|privObj
operator|.
name|getColumns
argument_list|()
decl_stmt|;
if|if
condition|(
name|columns
operator|!=
literal|null
operator|&&
operator|!
name|columns
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// show column level privileges
for|for
control|(
name|String
name|columnName
range|:
name|columns
control|)
block|{
name|privs
operator|.
name|addAll
argument_list|(
name|hive
operator|.
name|showPrivilegeGrant
argument_list|(
name|HiveObjectType
operator|.
name|COLUMN
argument_list|,
name|name
argument_list|,
name|type
argument_list|,
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
name|tableObj
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partValues
argument_list|,
name|columnName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|partValues
operator|==
literal|null
condition|)
block|{
comment|// show table level privileges
name|privs
operator|.
name|addAll
argument_list|(
name|hive
operator|.
name|showPrivilegeGrant
argument_list|(
name|HiveObjectType
operator|.
name|TABLE
argument_list|,
name|name
argument_list|,
name|type
argument_list|,
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
name|tableObj
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// show partition level privileges
name|privs
operator|.
name|addAll
argument_list|(
name|hive
operator|.
name|showPrivilegeGrant
argument_list|(
name|HiveObjectType
operator|.
name|PARTITION
argument_list|,
name|name
argument_list|,
name|type
argument_list|,
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
name|tableObj
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partValues
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|AuthorizationUtils
operator|.
name|getPrivilegeInfos
argument_list|(
name|privs
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setCurrentRole
parameter_list|(
name|String
name|roleName
parameter_list|)
throws|throws
name|HiveAccessControlException
throws|,
name|HiveAuthzPluginException
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Unsupported operation 'setCurrentRole' for V1 auth"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getCurrentRoleNames
parameter_list|()
throws|throws
name|HiveAuthzPluginException
block|{
name|String
name|userName
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getUserName
argument_list|()
decl_stmt|;
if|if
condition|(
name|userName
operator|==
literal|null
condition|)
block|{
name|userName
operator|=
name|SessionState
operator|.
name|getUserFromAuthenticator
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|userName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
literal|"Cannot resolve current user name"
argument_list|)
throw|;
block|}
try|try
block|{
name|Hive
name|hive
init|=
name|Hive
operator|.
name|getWithFastCheck
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|roleNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Role
name|role
range|:
name|hive
operator|.
name|listRoles
argument_list|(
name|userName
argument_list|,
name|PrincipalType
operator|.
name|USER
argument_list|)
control|)
block|{
name|roleNames
operator|.
name|add
argument_list|(
name|role
operator|.
name|getRoleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|roleNames
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveAuthzPluginException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|applyAuthorizationConfigPolicy
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|filterListCmdObjects
parameter_list|(
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|listObjs
parameter_list|,
name|HiveAuthzContext
name|context
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
comment|// do no filtering in old authorizer
return|return
name|listObjs
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needTransform
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|applyRowFilterAndColumnMasking
parameter_list|(
name|HiveAuthzContext
name|context
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|privObjs
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

