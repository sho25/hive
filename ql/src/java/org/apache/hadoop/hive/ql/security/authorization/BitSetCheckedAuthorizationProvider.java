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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
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
name|PrincipalPrivilegeSet
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
name|metadata
operator|.
name|AuthorizationException
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

begin_class
specifier|public
specifier|abstract
class|class
name|BitSetCheckedAuthorizationProvider
extends|extends
name|HiveAuthorizationProviderBase
block|{
specifier|static
class|class
name|BitSetChecker
block|{
name|boolean
index|[]
name|inputCheck
init|=
literal|null
decl_stmt|;
name|boolean
index|[]
name|outputCheck
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
name|BitSetChecker
name|getBitSetChecker
parameter_list|(
name|Privilege
index|[]
name|inputRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|outputRequiredPriv
parameter_list|)
block|{
name|BitSetChecker
name|checker
init|=
operator|new
name|BitSetChecker
argument_list|()
decl_stmt|;
if|if
condition|(
name|inputRequiredPriv
operator|!=
literal|null
condition|)
block|{
name|checker
operator|.
name|inputCheck
operator|=
operator|new
name|boolean
index|[
name|inputRequiredPriv
operator|.
name|length
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|checker
operator|.
name|inputCheck
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|checker
operator|.
name|inputCheck
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
name|outputRequiredPriv
operator|!=
literal|null
condition|)
block|{
name|checker
operator|.
name|outputCheck
operator|=
operator|new
name|boolean
index|[
name|outputRequiredPriv
operator|.
name|length
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|checker
operator|.
name|outputCheck
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|checker
operator|.
name|outputCheck
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
block|}
block|}
return|return
name|checker
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Privilege
index|[]
name|inputRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|outputRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
name|BitSetChecker
name|checker
init|=
name|BitSetChecker
operator|.
name|getBitSetChecker
argument_list|(
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|)
decl_stmt|;
name|boolean
index|[]
name|inputCheck
init|=
name|checker
operator|.
name|inputCheck
decl_stmt|;
name|boolean
index|[]
name|outputCheck
init|=
name|checker
operator|.
name|outputCheck
decl_stmt|;
name|authorizeUserPriv
argument_list|(
name|inputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputRequiredPriv
argument_list|,
name|outputCheck
argument_list|)
expr_stmt|;
name|checkAndThrowAuthorizationException
argument_list|(
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputCheck
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Database
name|db
parameter_list|,
name|Privilege
index|[]
name|inputRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|outputRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
name|BitSetChecker
name|checker
init|=
name|BitSetChecker
operator|.
name|getBitSetChecker
argument_list|(
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|)
decl_stmt|;
name|boolean
index|[]
name|inputCheck
init|=
name|checker
operator|.
name|inputCheck
decl_stmt|;
name|boolean
index|[]
name|outputCheck
init|=
name|checker
operator|.
name|outputCheck
decl_stmt|;
name|authorizeUserAndDBPriv
argument_list|(
name|db
argument_list|,
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputCheck
argument_list|)
expr_stmt|;
name|checkAndThrowAuthorizationException
argument_list|(
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputCheck
argument_list|,
name|db
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
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Table
name|table
parameter_list|,
name|Privilege
index|[]
name|inputRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|outputRequiredPriv
parameter_list|)
throws|throws
name|HiveException
block|{
name|BitSetChecker
name|checker
init|=
name|BitSetChecker
operator|.
name|getBitSetChecker
argument_list|(
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|)
decl_stmt|;
name|boolean
index|[]
name|inputCheck
init|=
name|checker
operator|.
name|inputCheck
decl_stmt|;
name|boolean
index|[]
name|outputCheck
init|=
name|checker
operator|.
name|outputCheck
decl_stmt|;
name|authorizeUserDBAndTable
argument_list|(
name|table
argument_list|,
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputCheck
argument_list|)
expr_stmt|;
name|checkAndThrowAuthorizationException
argument_list|(
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputCheck
argument_list|,
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Partition
name|part
parameter_list|,
name|Privilege
index|[]
name|inputRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|outputRequiredPriv
parameter_list|)
throws|throws
name|HiveException
block|{
comment|//if the partition does not have partition level privilege, go to table level.
name|Table
name|table
init|=
name|part
operator|.
name|getTable
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
literal|"PARTITION_LEVEL_PRIVILEGE"
argument_list|)
operator|==
literal|null
operator|||
operator|(
literal|"FALSE"
operator|.
name|equalsIgnoreCase
argument_list|(
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
literal|"PARTITION_LEVEL_PRIVILEGE"
argument_list|)
argument_list|)
operator|)
condition|)
block|{
name|this
operator|.
name|authorize
argument_list|(
name|part
operator|.
name|getTable
argument_list|()
argument_list|,
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|)
expr_stmt|;
return|return;
block|}
name|BitSetChecker
name|checker
init|=
name|BitSetChecker
operator|.
name|getBitSetChecker
argument_list|(
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|)
decl_stmt|;
name|boolean
index|[]
name|inputCheck
init|=
name|checker
operator|.
name|inputCheck
decl_stmt|;
name|boolean
index|[]
name|outputCheck
init|=
name|checker
operator|.
name|outputCheck
decl_stmt|;
if|if
condition|(
name|authorizeUserDbAndPartition
argument_list|(
name|part
argument_list|,
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputCheck
argument_list|)
condition|)
block|{
return|return;
block|}
name|checkAndThrowAuthorizationException
argument_list|(
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputCheck
argument_list|,
name|part
operator|.
name|getTable
argument_list|()
operator|.
name|getDbName
argument_list|()
argument_list|,
name|part
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|,
name|part
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|part
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|,
name|Privilege
index|[]
name|inputRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|outputRequiredPriv
parameter_list|)
throws|throws
name|HiveException
block|{
name|BitSetChecker
name|checker
init|=
name|BitSetChecker
operator|.
name|getBitSetChecker
argument_list|(
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|)
decl_stmt|;
name|boolean
index|[]
name|inputCheck
init|=
name|checker
operator|.
name|inputCheck
decl_stmt|;
name|boolean
index|[]
name|outputCheck
init|=
name|checker
operator|.
name|outputCheck
decl_stmt|;
name|String
name|partName
init|=
literal|null
decl_stmt|;
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
name|part
operator|!=
literal|null
operator|&&
operator|(
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
literal|"PARTITION_LEVEL_PRIVILEGE"
argument_list|)
operator|!=
literal|null
operator|&&
operator|(
literal|"TRUE"
operator|.
name|equalsIgnoreCase
argument_list|(
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
literal|"PARTITION_LEVEL_PRIVILEGE"
argument_list|)
argument_list|)
operator|)
operator|)
condition|)
block|{
name|partName
operator|=
name|part
operator|.
name|getName
argument_list|()
expr_stmt|;
name|partValues
operator|=
name|part
operator|.
name|getValues
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|partValues
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|authorizeUserDBAndTable
argument_list|(
name|table
argument_list|,
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputCheck
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
else|else
block|{
if|if
condition|(
name|authorizeUserDbAndPartition
argument_list|(
name|part
argument_list|,
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputCheck
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
for|for
control|(
name|String
name|col
range|:
name|columns
control|)
block|{
name|BitSetChecker
name|checker2
init|=
name|BitSetChecker
operator|.
name|getBitSetChecker
argument_list|(
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|)
decl_stmt|;
name|boolean
index|[]
name|inputCheck2
init|=
name|checker2
operator|.
name|inputCheck
decl_stmt|;
name|boolean
index|[]
name|outputCheck2
init|=
name|checker2
operator|.
name|outputCheck
decl_stmt|;
name|PrincipalPrivilegeSet
name|partColumnPrivileges
init|=
name|hive_db
operator|.
name|get_privilege_set
argument_list|(
name|HiveObjectType
operator|.
name|COLUMN
argument_list|,
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partValues
argument_list|,
name|col
argument_list|,
name|this
operator|.
name|getAuthenticator
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|,
name|this
operator|.
name|getAuthenticator
argument_list|()
operator|.
name|getGroupNames
argument_list|()
argument_list|)
decl_stmt|;
name|authorizePrivileges
argument_list|(
name|partColumnPrivileges
argument_list|,
name|inputRequiredPriv
argument_list|,
name|inputCheck2
argument_list|,
name|outputRequiredPriv
argument_list|,
name|outputCheck2
argument_list|)
expr_stmt|;
if|if
condition|(
name|inputCheck2
operator|!=
literal|null
condition|)
block|{
name|booleanArrayOr
argument_list|(
name|inputCheck2
argument_list|,
name|inputCheck
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|outputCheck2
operator|!=
literal|null
condition|)
block|{
name|booleanArrayOr
argument_list|(
name|inputCheck2
argument_list|,
name|inputCheck
argument_list|)
expr_stmt|;
block|}
name|checkAndThrowAuthorizationException
argument_list|(
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|,
name|inputCheck2
argument_list|,
name|outputCheck2
argument_list|,
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partName
argument_list|,
name|col
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|boolean
name|authorizeUserPriv
parameter_list|(
name|Privilege
index|[]
name|inputRequiredPriv
parameter_list|,
name|boolean
index|[]
name|inputCheck
parameter_list|,
name|Privilege
index|[]
name|outputRequiredPriv
parameter_list|,
name|boolean
index|[]
name|outputCheck
parameter_list|)
throws|throws
name|HiveException
block|{
name|PrincipalPrivilegeSet
name|privileges
init|=
name|hive_db
operator|.
name|get_privilege_set
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
argument_list|,
name|this
operator|.
name|getAuthenticator
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|,
name|this
operator|.
name|getAuthenticator
argument_list|()
operator|.
name|getGroupNames
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|authorizePrivileges
argument_list|(
name|privileges
argument_list|,
name|inputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputRequiredPriv
argument_list|,
name|outputCheck
argument_list|)
return|;
block|}
comment|/**    * Check privileges on User and DB. This is used before doing a check on    * table/partition objects, first check the user and DB privileges. If it    * passed on this check, no need to check against the table/partition hive    * object.    *    * @param db    * @param inputRequiredPriv    * @param outputRequiredPriv    * @param inputCheck    * @param outputCheck    * @return true if the check on user and DB privilege passed, which means no    *         need for privilege check on concrete hive objects.    * @throws HiveException    */
specifier|private
name|boolean
name|authorizeUserAndDBPriv
parameter_list|(
name|Database
name|db
parameter_list|,
name|Privilege
index|[]
name|inputRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|outputRequiredPriv
parameter_list|,
name|boolean
index|[]
name|inputCheck
parameter_list|,
name|boolean
index|[]
name|outputCheck
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|authorizeUserPriv
argument_list|(
name|inputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputRequiredPriv
argument_list|,
name|outputCheck
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
name|PrincipalPrivilegeSet
name|dbPrivileges
init|=
name|hive_db
operator|.
name|get_privilege_set
argument_list|(
name|HiveObjectType
operator|.
name|DATABASE
argument_list|,
name|db
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|this
operator|.
name|getAuthenticator
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|,
name|this
operator|.
name|getAuthenticator
argument_list|()
operator|.
name|getGroupNames
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|authorizePrivileges
argument_list|(
name|dbPrivileges
argument_list|,
name|inputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputRequiredPriv
argument_list|,
name|outputCheck
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Check privileges on User, DB and table objects.    *    * @param table    * @param inputRequiredPriv    * @param outputRequiredPriv    * @param inputCheck    * @param outputCheck    * @return true if the check passed    * @throws HiveException    */
specifier|private
name|boolean
name|authorizeUserDBAndTable
parameter_list|(
name|Table
name|table
parameter_list|,
name|Privilege
index|[]
name|inputRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|outputRequiredPriv
parameter_list|,
name|boolean
index|[]
name|inputCheck
parameter_list|,
name|boolean
index|[]
name|outputCheck
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|authorizeUserAndDBPriv
argument_list|(
name|hive_db
operator|.
name|getDatabase
argument_list|(
name|table
operator|.
name|getCatName
argument_list|()
argument_list|,
name|table
operator|.
name|getDbName
argument_list|()
argument_list|)
argument_list|,
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputCheck
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
name|PrincipalPrivilegeSet
name|tablePrivileges
init|=
name|hive_db
operator|.
name|get_privilege_set
argument_list|(
name|HiveObjectType
operator|.
name|TABLE
argument_list|,
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|this
operator|.
name|getAuthenticator
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|,
name|this
operator|.
name|getAuthenticator
argument_list|()
operator|.
name|getGroupNames
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|authorizePrivileges
argument_list|(
name|tablePrivileges
argument_list|,
name|inputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputRequiredPriv
argument_list|,
name|outputCheck
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Check privileges on User, DB and table/Partition objects.    *    * @param part    * @param inputRequiredPriv    * @param outputRequiredPriv    * @param inputCheck    * @param outputCheck    * @return true if the check passed    * @throws HiveException    */
specifier|private
name|boolean
name|authorizeUserDbAndPartition
parameter_list|(
name|Partition
name|part
parameter_list|,
name|Privilege
index|[]
name|inputRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|outputRequiredPriv
parameter_list|,
name|boolean
index|[]
name|inputCheck
parameter_list|,
name|boolean
index|[]
name|outputCheck
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|authorizeUserAndDBPriv
argument_list|(
name|hive_db
operator|.
name|getDatabase
argument_list|(
name|part
operator|.
name|getTable
argument_list|()
operator|.
name|getCatName
argument_list|()
argument_list|,
name|part
operator|.
name|getTable
argument_list|()
operator|.
name|getDbName
argument_list|()
argument_list|)
argument_list|,
name|inputRequiredPriv
argument_list|,
name|outputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputCheck
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
name|PrincipalPrivilegeSet
name|partPrivileges
init|=
name|part
operator|.
name|getTPartition
argument_list|()
operator|.
name|getPrivileges
argument_list|()
decl_stmt|;
if|if
condition|(
name|partPrivileges
operator|==
literal|null
condition|)
block|{
name|partPrivileges
operator|=
name|hive_db
operator|.
name|get_privilege_set
argument_list|(
name|HiveObjectType
operator|.
name|PARTITION
argument_list|,
name|part
operator|.
name|getTable
argument_list|()
operator|.
name|getDbName
argument_list|()
argument_list|,
name|part
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|,
name|part
operator|.
name|getValues
argument_list|()
argument_list|,
literal|null
argument_list|,
name|this
operator|.
name|getAuthenticator
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|,
name|this
operator|.
name|getAuthenticator
argument_list|()
operator|.
name|getGroupNames
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|authorizePrivileges
argument_list|(
name|partPrivileges
argument_list|,
name|inputRequiredPriv
argument_list|,
name|inputCheck
argument_list|,
name|outputRequiredPriv
argument_list|,
name|outputCheck
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|protected
name|boolean
name|authorizePrivileges
parameter_list|(
name|PrincipalPrivilegeSet
name|privileges
parameter_list|,
name|Privilege
index|[]
name|inputPriv
parameter_list|,
name|boolean
index|[]
name|inputCheck
parameter_list|,
name|Privilege
index|[]
name|outputPriv
parameter_list|,
name|boolean
index|[]
name|outputCheck
parameter_list|)
throws|throws
name|HiveException
block|{
name|boolean
name|pass
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|inputPriv
operator|!=
literal|null
condition|)
block|{
name|pass
operator|=
name|pass
operator|&&
name|matchPrivs
argument_list|(
name|inputPriv
argument_list|,
name|privileges
argument_list|,
name|inputCheck
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|outputPriv
operator|!=
literal|null
condition|)
block|{
name|pass
operator|=
name|pass
operator|&&
name|matchPrivs
argument_list|(
name|outputPriv
argument_list|,
name|privileges
argument_list|,
name|outputCheck
argument_list|)
expr_stmt|;
block|}
return|return
name|pass
return|;
block|}
comment|/**    * try to match an array of privileges from user/groups/roles grants.    *    */
specifier|private
name|boolean
name|matchPrivs
parameter_list|(
name|Privilege
index|[]
name|inputPriv
parameter_list|,
name|PrincipalPrivilegeSet
name|privileges
parameter_list|,
name|boolean
index|[]
name|check
parameter_list|)
block|{
if|if
condition|(
name|inputPriv
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|privileges
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|/*      * user grants      */
name|Set
argument_list|<
name|String
argument_list|>
name|privSet
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|privileges
operator|.
name|getUserPrivileges
argument_list|()
operator|!=
literal|null
operator|&&
name|privileges
operator|.
name|getUserPrivileges
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Collection
argument_list|<
name|List
argument_list|<
name|PrivilegeGrantInfo
argument_list|>
argument_list|>
name|privCollection
init|=
name|privileges
operator|.
name|getUserPrivileges
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|userPrivs
init|=
name|getPrivilegeStringList
argument_list|(
name|privCollection
argument_list|)
decl_stmt|;
if|if
condition|(
name|userPrivs
operator|!=
literal|null
operator|&&
name|userPrivs
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|String
name|priv
range|:
name|userPrivs
control|)
block|{
if|if
condition|(
name|priv
operator|==
literal|null
operator|||
name|priv
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|priv
operator|.
name|equalsIgnoreCase
argument_list|(
name|Privilege
operator|.
name|ALL
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
name|setBooleanArray
argument_list|(
name|check
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|privSet
operator|.
name|add
argument_list|(
name|priv
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/*      * group grants      */
if|if
condition|(
name|privileges
operator|.
name|getGroupPrivileges
argument_list|()
operator|!=
literal|null
operator|&&
name|privileges
operator|.
name|getGroupPrivileges
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Collection
argument_list|<
name|List
argument_list|<
name|PrivilegeGrantInfo
argument_list|>
argument_list|>
name|groupPrivCollection
init|=
name|privileges
operator|.
name|getGroupPrivileges
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|groupPrivs
init|=
name|getPrivilegeStringList
argument_list|(
name|groupPrivCollection
argument_list|)
decl_stmt|;
if|if
condition|(
name|groupPrivs
operator|!=
literal|null
operator|&&
name|groupPrivs
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|String
name|priv
range|:
name|groupPrivs
control|)
block|{
if|if
condition|(
name|priv
operator|==
literal|null
operator|||
name|priv
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|priv
operator|.
name|equalsIgnoreCase
argument_list|(
name|Privilege
operator|.
name|ALL
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
name|setBooleanArray
argument_list|(
name|check
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|privSet
operator|.
name|add
argument_list|(
name|priv
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/*      * roles grants      */
if|if
condition|(
name|privileges
operator|.
name|getRolePrivileges
argument_list|()
operator|!=
literal|null
operator|&&
name|privileges
operator|.
name|getRolePrivileges
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Collection
argument_list|<
name|List
argument_list|<
name|PrivilegeGrantInfo
argument_list|>
argument_list|>
name|rolePrivsCollection
init|=
name|privileges
operator|.
name|getRolePrivileges
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
empty_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rolePrivs
init|=
name|getPrivilegeStringList
argument_list|(
name|rolePrivsCollection
argument_list|)
decl_stmt|;
if|if
condition|(
name|rolePrivs
operator|!=
literal|null
operator|&&
name|rolePrivs
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|String
name|priv
range|:
name|rolePrivs
control|)
block|{
if|if
condition|(
name|priv
operator|==
literal|null
operator|||
name|priv
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|priv
operator|.
name|equalsIgnoreCase
argument_list|(
name|Privilege
operator|.
name|ALL
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
name|setBooleanArray
argument_list|(
name|check
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|privSet
operator|.
name|add
argument_list|(
name|priv
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
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
name|inputPriv
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|toMatch
init|=
name|inputPriv
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|check
index|[
name|i
index|]
condition|)
block|{
name|check
index|[
name|i
index|]
operator|=
name|privSet
operator|.
name|contains
argument_list|(
name|toMatch
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|firstFalseIndex
argument_list|(
name|check
argument_list|)
operator|<
literal|0
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getPrivilegeStringList
parameter_list|(
name|Collection
argument_list|<
name|List
argument_list|<
name|PrivilegeGrantInfo
argument_list|>
argument_list|>
name|privCollection
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|userPrivs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|privCollection
operator|!=
literal|null
operator|&&
name|privCollection
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|List
argument_list|<
name|PrivilegeGrantInfo
argument_list|>
name|grantList
range|:
name|privCollection
control|)
block|{
if|if
condition|(
name|grantList
operator|==
literal|null
condition|)
block|{
continue|continue;
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
name|grantList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|PrivilegeGrantInfo
name|grant
init|=
name|grantList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|userPrivs
operator|.
name|add
argument_list|(
name|grant
operator|.
name|getPrivilege
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|userPrivs
return|;
block|}
specifier|private
specifier|static
name|void
name|setBooleanArray
parameter_list|(
name|boolean
index|[]
name|check
parameter_list|,
name|boolean
name|b
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|check
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|check
index|[
name|i
index|]
operator|=
name|b
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|booleanArrayOr
parameter_list|(
name|boolean
index|[]
name|output
parameter_list|,
name|boolean
index|[]
name|input
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|output
operator|.
name|length
operator|&&
name|i
operator|<
name|input
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|output
index|[
name|i
index|]
operator|=
name|output
index|[
name|i
index|]
operator|||
name|input
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|checkAndThrowAuthorizationException
parameter_list|(
name|Privilege
index|[]
name|inputRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|outputRequiredPriv
parameter_list|,
name|boolean
index|[]
name|inputCheck
parameter_list|,
name|boolean
index|[]
name|outputCheck
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|partitionName
parameter_list|,
name|String
name|columnName
parameter_list|)
block|{
name|String
name|hiveObject
init|=
literal|"{ "
decl_stmt|;
if|if
condition|(
name|dbName
operator|!=
literal|null
condition|)
block|{
name|hiveObject
operator|=
name|hiveObject
operator|+
literal|"database:"
operator|+
name|dbName
expr_stmt|;
block|}
if|if
condition|(
name|tableName
operator|!=
literal|null
condition|)
block|{
name|hiveObject
operator|=
name|hiveObject
operator|+
literal|", table:"
operator|+
name|tableName
expr_stmt|;
block|}
if|if
condition|(
name|partitionName
operator|!=
literal|null
condition|)
block|{
name|hiveObject
operator|=
name|hiveObject
operator|+
literal|", partitionName:"
operator|+
name|partitionName
expr_stmt|;
block|}
if|if
condition|(
name|columnName
operator|!=
literal|null
condition|)
block|{
name|hiveObject
operator|=
name|hiveObject
operator|+
literal|", columnName:"
operator|+
name|columnName
expr_stmt|;
block|}
name|hiveObject
operator|=
name|hiveObject
operator|+
literal|"}"
expr_stmt|;
if|if
condition|(
name|inputCheck
operator|!=
literal|null
condition|)
block|{
name|int
name|input
init|=
name|this
operator|.
name|firstFalseIndex
argument_list|(
name|inputCheck
argument_list|)
decl_stmt|;
if|if
condition|(
name|input
operator|>=
literal|0
condition|)
block|{
throw|throw
operator|new
name|AuthorizationException
argument_list|(
literal|"No privilege '"
operator|+
name|inputRequiredPriv
index|[
name|input
index|]
operator|.
name|toString
argument_list|()
operator|+
literal|"' found for inputs "
operator|+
name|hiveObject
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|outputCheck
operator|!=
literal|null
condition|)
block|{
name|int
name|output
init|=
name|this
operator|.
name|firstFalseIndex
argument_list|(
name|outputCheck
argument_list|)
decl_stmt|;
if|if
condition|(
name|output
operator|>=
literal|0
condition|)
block|{
throw|throw
operator|new
name|AuthorizationException
argument_list|(
literal|"No privilege '"
operator|+
name|outputRequiredPriv
index|[
name|output
index|]
operator|.
name|toString
argument_list|()
operator|+
literal|"' found for outputs "
operator|+
name|hiveObject
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|int
name|firstFalseIndex
parameter_list|(
name|boolean
index|[]
name|inputCheck
parameter_list|)
block|{
if|if
condition|(
name|inputCheck
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|inputCheck
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|inputCheck
index|[
name|i
index|]
condition|)
block|{
return|return
name|i
return|;
block|}
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
block|}
end_class

end_unit

