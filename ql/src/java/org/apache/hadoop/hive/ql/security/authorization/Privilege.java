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
name|java
operator|.
name|util
operator|.
name|EnumSet
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
name|HiveParser
import|;
end_import

begin_comment
comment|/**  * Privilege defines a privilege in Hive. Each privilege has a name and scope associated with it.  * This class contains all of the predefined privileges in Hive.  */
end_comment

begin_class
specifier|public
class|class
name|Privilege
block|{
specifier|public
enum|enum
name|PrivilegeType
block|{
name|ALL
block|,
name|ALTER_DATA
block|,
name|ALTER_METADATA
block|,
name|CREATE
block|,
name|DROP
block|,
name|INDEX
block|,
name|LOCK
block|,
name|SELECT
block|,
name|SHOW_DATABASE
block|,
name|UNKNOWN
block|}
specifier|public
specifier|static
name|PrivilegeType
name|getPrivTypeByToken
parameter_list|(
name|int
name|token
parameter_list|)
block|{
switch|switch
condition|(
name|token
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_PRIV_ALL
case|:
return|return
name|PrivilegeType
operator|.
name|ALL
return|;
case|case
name|HiveParser
operator|.
name|TOK_PRIV_ALTER_DATA
case|:
return|return
name|PrivilegeType
operator|.
name|ALTER_DATA
return|;
case|case
name|HiveParser
operator|.
name|TOK_PRIV_ALTER_METADATA
case|:
return|return
name|PrivilegeType
operator|.
name|ALTER_METADATA
return|;
case|case
name|HiveParser
operator|.
name|TOK_PRIV_CREATE
case|:
return|return
name|PrivilegeType
operator|.
name|CREATE
return|;
case|case
name|HiveParser
operator|.
name|TOK_PRIV_DROP
case|:
return|return
name|PrivilegeType
operator|.
name|DROP
return|;
case|case
name|HiveParser
operator|.
name|TOK_PRIV_INDEX
case|:
return|return
name|PrivilegeType
operator|.
name|INDEX
return|;
case|case
name|HiveParser
operator|.
name|TOK_PRIV_LOCK
case|:
return|return
name|PrivilegeType
operator|.
name|LOCK
return|;
case|case
name|HiveParser
operator|.
name|TOK_PRIV_SELECT
case|:
return|return
name|PrivilegeType
operator|.
name|SELECT
return|;
case|case
name|HiveParser
operator|.
name|TOK_PRIV_SHOW_DATABASE
case|:
return|return
name|PrivilegeType
operator|.
name|SHOW_DATABASE
return|;
default|default:
return|return
name|PrivilegeType
operator|.
name|UNKNOWN
return|;
block|}
block|}
specifier|public
specifier|static
name|PrivilegeType
name|getPrivTypeByName
parameter_list|(
name|String
name|privilegeName
parameter_list|)
block|{
name|String
name|canonicalizedName
init|=
name|privilegeName
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
if|if
condition|(
name|canonicalizedName
operator|.
name|equals
argument_list|(
literal|"all"
argument_list|)
condition|)
block|{
return|return
name|PrivilegeType
operator|.
name|ALL
return|;
block|}
elseif|else
if|if
condition|(
name|canonicalizedName
operator|.
name|equals
argument_list|(
literal|"update"
argument_list|)
condition|)
block|{
return|return
name|PrivilegeType
operator|.
name|ALTER_DATA
return|;
block|}
elseif|else
if|if
condition|(
name|canonicalizedName
operator|.
name|equals
argument_list|(
literal|"alter"
argument_list|)
condition|)
block|{
return|return
name|PrivilegeType
operator|.
name|ALTER_METADATA
return|;
block|}
elseif|else
if|if
condition|(
name|canonicalizedName
operator|.
name|equals
argument_list|(
literal|"create"
argument_list|)
condition|)
block|{
return|return
name|PrivilegeType
operator|.
name|CREATE
return|;
block|}
elseif|else
if|if
condition|(
name|canonicalizedName
operator|.
name|equals
argument_list|(
literal|"drop"
argument_list|)
condition|)
block|{
return|return
name|PrivilegeType
operator|.
name|DROP
return|;
block|}
elseif|else
if|if
condition|(
name|canonicalizedName
operator|.
name|equals
argument_list|(
literal|"index"
argument_list|)
condition|)
block|{
return|return
name|PrivilegeType
operator|.
name|INDEX
return|;
block|}
elseif|else
if|if
condition|(
name|canonicalizedName
operator|.
name|equals
argument_list|(
literal|"lock"
argument_list|)
condition|)
block|{
return|return
name|PrivilegeType
operator|.
name|LOCK
return|;
block|}
elseif|else
if|if
condition|(
name|canonicalizedName
operator|.
name|equals
argument_list|(
literal|"select"
argument_list|)
condition|)
block|{
return|return
name|PrivilegeType
operator|.
name|SELECT
return|;
block|}
elseif|else
if|if
condition|(
name|canonicalizedName
operator|.
name|equals
argument_list|(
literal|"show_database"
argument_list|)
condition|)
block|{
return|return
name|PrivilegeType
operator|.
name|SHOW_DATABASE
return|;
block|}
return|return
name|PrivilegeType
operator|.
name|UNKNOWN
return|;
block|}
specifier|private
name|PrivilegeType
name|priv
decl_stmt|;
specifier|private
name|EnumSet
argument_list|<
name|PrivilegeScope
argument_list|>
name|supportedScopeSet
decl_stmt|;
specifier|private
name|Privilege
parameter_list|(
name|PrivilegeType
name|priv
parameter_list|,
name|EnumSet
argument_list|<
name|PrivilegeScope
argument_list|>
name|scopeSet
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|priv
operator|=
name|priv
expr_stmt|;
name|this
operator|.
name|supportedScopeSet
operator|=
name|scopeSet
expr_stmt|;
block|}
specifier|public
name|Privilege
parameter_list|(
name|PrivilegeType
name|priv
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|priv
operator|=
name|priv
expr_stmt|;
block|}
specifier|public
name|PrivilegeType
name|getPriv
parameter_list|()
block|{
return|return
name|priv
return|;
block|}
specifier|public
name|void
name|setPriv
parameter_list|(
name|PrivilegeType
name|priv
parameter_list|)
block|{
name|this
operator|.
name|priv
operator|=
name|priv
expr_stmt|;
block|}
specifier|public
name|boolean
name|supportColumnLevel
parameter_list|()
block|{
return|return
name|supportedScopeSet
operator|!=
literal|null
operator|&&
name|supportedScopeSet
operator|.
name|contains
argument_list|(
name|PrivilegeScope
operator|.
name|COLUMN_LEVEL_SCOPE
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|supportDBLevel
parameter_list|()
block|{
return|return
name|supportedScopeSet
operator|!=
literal|null
operator|&&
name|supportedScopeSet
operator|.
name|contains
argument_list|(
name|PrivilegeScope
operator|.
name|DB_LEVEL_SCOPE
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|supportTableLevel
parameter_list|()
block|{
return|return
name|supportedScopeSet
operator|!=
literal|null
operator|&&
name|supportedScopeSet
operator|.
name|contains
argument_list|(
name|PrivilegeScope
operator|.
name|TABLE_LEVEL_SCOPE
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
switch|switch
condition|(
name|this
operator|.
name|priv
condition|)
block|{
case|case
name|ALL
case|:
return|return
literal|"All"
return|;
case|case
name|ALTER_DATA
case|:
return|return
literal|"Update"
return|;
case|case
name|ALTER_METADATA
case|:
return|return
literal|"Alter"
return|;
case|case
name|CREATE
case|:
return|return
literal|"Create"
return|;
case|case
name|DROP
case|:
return|return
literal|"Drop"
return|;
case|case
name|INDEX
case|:
return|return
literal|"Index"
return|;
case|case
name|LOCK
case|:
return|return
literal|"Lock"
return|;
case|case
name|SELECT
case|:
return|return
literal|"Select"
return|;
case|case
name|SHOW_DATABASE
case|:
return|return
literal|"Show_Database"
return|;
default|default:
return|return
literal|"Unknown"
return|;
block|}
block|}
specifier|public
name|Privilege
parameter_list|()
block|{   }
specifier|public
specifier|static
name|Privilege
name|ALL
init|=
operator|new
name|Privilege
argument_list|(
name|PrivilegeType
operator|.
name|ALL
argument_list|,
name|PrivilegeScope
operator|.
name|ALLSCOPE_EXCEPT_COLUMN
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Privilege
name|ALTER_METADATA
init|=
operator|new
name|Privilege
argument_list|(
name|PrivilegeType
operator|.
name|ALTER_METADATA
argument_list|,
name|PrivilegeScope
operator|.
name|ALLSCOPE_EXCEPT_COLUMN
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Privilege
name|ALTER_DATA
init|=
operator|new
name|Privilege
argument_list|(
name|PrivilegeType
operator|.
name|ALTER_DATA
argument_list|,
name|PrivilegeScope
operator|.
name|ALLSCOPE_EXCEPT_COLUMN
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Privilege
name|CREATE
init|=
operator|new
name|Privilege
argument_list|(
name|PrivilegeType
operator|.
name|CREATE
argument_list|,
name|PrivilegeScope
operator|.
name|ALLSCOPE_EXCEPT_COLUMN
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Privilege
name|DROP
init|=
operator|new
name|Privilege
argument_list|(
name|PrivilegeType
operator|.
name|DROP
argument_list|,
name|PrivilegeScope
operator|.
name|ALLSCOPE_EXCEPT_COLUMN
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Privilege
name|INDEX
init|=
operator|new
name|Privilege
argument_list|(
name|PrivilegeType
operator|.
name|INDEX
argument_list|,
name|PrivilegeScope
operator|.
name|ALLSCOPE
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Privilege
name|LOCK
init|=
operator|new
name|Privilege
argument_list|(
name|PrivilegeType
operator|.
name|LOCK
argument_list|,
name|PrivilegeScope
operator|.
name|ALLSCOPE_EXCEPT_COLUMN
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Privilege
name|SELECT
init|=
operator|new
name|Privilege
argument_list|(
name|PrivilegeType
operator|.
name|SELECT
argument_list|,
name|PrivilegeScope
operator|.
name|ALLSCOPE
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|Privilege
name|SHOW_DATABASE
init|=
operator|new
name|Privilege
argument_list|(
name|PrivilegeType
operator|.
name|SHOW_DATABASE
argument_list|,
name|EnumSet
operator|.
name|of
argument_list|(
name|PrivilegeScope
operator|.
name|USER_LEVEL_SCOPE
argument_list|)
argument_list|)
decl_stmt|;
block|}
end_class

end_unit

