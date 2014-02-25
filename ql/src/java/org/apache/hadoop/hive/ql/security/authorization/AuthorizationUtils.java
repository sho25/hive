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
name|classification
operator|.
name|InterfaceAudience
operator|.
name|LimitedPrivate
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
name|ql
operator|.
name|ErrorMsg
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
name|hooks
operator|.
name|Entity
operator|.
name|Type
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
name|HivePrivilege
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

begin_comment
comment|/**  * Utility code shared by hive internal code and sql standard authorization plugin implementation  */
end_comment

begin_class
annotation|@
name|LimitedPrivate
argument_list|(
name|value
operator|=
block|{
literal|"Sql standard authorization plugin"
block|}
argument_list|)
specifier|public
class|class
name|AuthorizationUtils
block|{
comment|/**    * Convert thrift principal type to authorization plugin principal type    * @param type - thrift principal type    * @return    * @throws HiveException    */
specifier|public
specifier|static
name|HivePrincipalType
name|getHivePrincipalType
parameter_list|(
name|PrincipalType
name|type
parameter_list|)
throws|throws
name|HiveException
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|USER
case|:
return|return
name|HivePrincipalType
operator|.
name|USER
return|;
case|case
name|ROLE
case|:
return|return
name|HivePrincipalType
operator|.
name|ROLE
return|;
case|case
name|GROUP
case|:
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|UNNSUPPORTED_AUTHORIZATION_PRINCIPAL_TYPE_GROUP
argument_list|)
throw|;
default|default:
comment|//should not happen as we take care of all existing types
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unsupported authorization type specified"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Convert thrift object type to hive authorization plugin object type    * @param type - thrift object type    * @return    */
specifier|public
specifier|static
name|HivePrivilegeObjectType
name|getHivePrivilegeObjectType
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|DATABASE
case|:
return|return
name|HivePrivilegeObjectType
operator|.
name|DATABASE
return|;
case|case
name|TABLE
case|:
return|return
name|HivePrivilegeObjectType
operator|.
name|TABLE_OR_VIEW
return|;
case|case
name|LOCAL_DIR
case|:
return|return
name|HivePrivilegeObjectType
operator|.
name|LOCAL_URI
return|;
case|case
name|DFS_DIR
case|:
return|return
name|HivePrivilegeObjectType
operator|.
name|DFS_URI
return|;
case|case
name|PARTITION
case|:
case|case
name|DUMMYPARTITION
case|:
comment|//need to determine if a different type is needed for dummy partitions
return|return
name|HivePrivilegeObjectType
operator|.
name|PARTITION
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Convert authorization plugin principal type to thrift principal type    * @param type    * @return    * @throws HiveException    */
specifier|public
specifier|static
name|PrincipalType
name|getThriftPrincipalType
parameter_list|(
name|HivePrincipalType
name|type
parameter_list|)
block|{
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|USER
case|:
return|return
name|PrincipalType
operator|.
name|USER
return|;
case|case
name|ROLE
case|:
return|return
name|PrincipalType
operator|.
name|ROLE
return|;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Invalid principal type "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
comment|/**    * Get thrift privilege grant info    * @param privilege    * @param grantorPrincipal    * @param grantOption    * @return    * @throws HiveException    */
specifier|public
specifier|static
name|PrivilegeGrantInfo
name|getThriftPrivilegeGrantInfo
parameter_list|(
name|HivePrivilege
name|privilege
parameter_list|,
name|HivePrincipal
name|grantorPrincipal
parameter_list|,
name|boolean
name|grantOption
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
operator|new
name|PrivilegeGrantInfo
argument_list|(
name|privilege
operator|.
name|getName
argument_list|()
argument_list|,
literal|0
comment|/* time gets added by server */
argument_list|,
name|grantorPrincipal
operator|.
name|getName
argument_list|()
argument_list|,
name|getThriftPrincipalType
argument_list|(
name|grantorPrincipal
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
name|grantOption
argument_list|)
return|;
block|}
comment|/**    * Convert plugin privilege object type to thrift type    * @param type    * @return    * @throws HiveException    */
specifier|public
specifier|static
name|HiveObjectType
name|getThriftHiveObjType
parameter_list|(
name|HivePrivilegeObjectType
name|type
parameter_list|)
throws|throws
name|HiveException
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|DATABASE
case|:
return|return
name|HiveObjectType
operator|.
name|DATABASE
return|;
case|case
name|TABLE_OR_VIEW
case|:
return|return
name|HiveObjectType
operator|.
name|TABLE
return|;
case|case
name|PARTITION
case|:
return|return
name|HiveObjectType
operator|.
name|PARTITION
return|;
case|case
name|LOCAL_URI
case|:
case|case
name|DFS_URI
case|:
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unsupported type "
operator|+
name|type
argument_list|)
throw|;
default|default:
comment|//should not happen as we have accounted for all types
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unsupported type "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
comment|/**    * Convert thrift HiveObjectRef to plugin HivePrivilegeObject    * @param privObj    * @return    * @throws HiveException    */
specifier|public
specifier|static
name|HiveObjectRef
name|getThriftHiveObjectRef
parameter_list|(
name|HivePrivilegeObject
name|privObj
parameter_list|)
throws|throws
name|HiveException
block|{
name|HiveObjectType
name|objType
init|=
name|getThriftHiveObjType
argument_list|(
name|privObj
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|HiveObjectRef
argument_list|(
name|objType
argument_list|,
name|privObj
operator|.
name|getDbname
argument_list|()
argument_list|,
name|privObj
operator|.
name|getTableViewURI
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
end_class

end_unit

