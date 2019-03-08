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
comment|/**  * Privilege type  */
end_comment

begin_enum
specifier|public
enum|enum
name|PrivilegeType
block|{
name|ALL
argument_list|(
name|HiveParser
operator|.
name|TOK_PRIV_ALL
argument_list|,
literal|"All"
argument_list|)
block|,
name|ALTER_DATA
argument_list|(
name|HiveParser
operator|.
name|TOK_PRIV_ALTER_DATA
argument_list|,
literal|"Update"
argument_list|)
block|,
name|ALTER_METADATA
argument_list|(
name|HiveParser
operator|.
name|TOK_PRIV_ALTER_METADATA
argument_list|,
literal|"Alter"
argument_list|)
block|,
name|CREATE
argument_list|(
name|HiveParser
operator|.
name|TOK_PRIV_CREATE
argument_list|,
literal|"Create"
argument_list|)
block|,
name|DROP
argument_list|(
name|HiveParser
operator|.
name|TOK_PRIV_DROP
argument_list|,
literal|"Drop"
argument_list|)
block|,
name|LOCK
argument_list|(
name|HiveParser
operator|.
name|TOK_PRIV_LOCK
argument_list|,
literal|"Lock"
argument_list|)
block|,
name|SELECT
argument_list|(
name|HiveParser
operator|.
name|TOK_PRIV_SELECT
argument_list|,
literal|"Select"
argument_list|)
block|,
name|SHOW_DATABASE
argument_list|(
name|HiveParser
operator|.
name|TOK_PRIV_SHOW_DATABASE
argument_list|,
literal|"Show_Database"
argument_list|)
block|,
name|INSERT
argument_list|(
name|HiveParser
operator|.
name|TOK_PRIV_INSERT
argument_list|,
literal|"Insert"
argument_list|)
block|,
name|DELETE
argument_list|(
name|HiveParser
operator|.
name|TOK_PRIV_DELETE
argument_list|,
literal|"Delete"
argument_list|)
block|,
name|UNKNOWN
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
block|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|Integer
name|token
decl_stmt|;
name|PrivilegeType
parameter_list|(
name|Integer
name|token
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|token
operator|=
name|token
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|name
operator|==
literal|null
condition|?
literal|"unkown"
else|:
name|name
return|;
block|}
specifier|public
name|Integer
name|getToken
parameter_list|()
block|{
return|return
name|token
return|;
block|}
specifier|private
specifier|static
name|Map
argument_list|<
name|Integer
argument_list|,
name|PrivilegeType
argument_list|>
name|token2Type
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|PrivilegeType
argument_list|>
name|name2Type
decl_stmt|;
comment|/**    * Do case lookup of PrivilegeType associated with this antlr token    * @param token    * @return corresponding PrivilegeType    */
specifier|public
specifier|static
name|PrivilegeType
name|getPrivTypeByToken
parameter_list|(
name|int
name|token
parameter_list|)
block|{
name|populateToken2Type
argument_list|()
expr_stmt|;
name|PrivilegeType
name|privType
init|=
name|token2Type
operator|.
name|get
argument_list|(
name|token
argument_list|)
decl_stmt|;
if|if
condition|(
name|privType
operator|!=
literal|null
condition|)
block|{
return|return
name|privType
return|;
block|}
return|return
name|PrivilegeType
operator|.
name|UNKNOWN
return|;
block|}
specifier|private
specifier|static
specifier|synchronized
name|void
name|populateToken2Type
parameter_list|()
block|{
if|if
condition|(
name|token2Type
operator|!=
literal|null
condition|)
block|{
return|return;
block|}
name|token2Type
operator|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|PrivilegeType
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|PrivilegeType
name|privType
range|:
name|PrivilegeType
operator|.
name|values
argument_list|()
control|)
block|{
name|token2Type
operator|.
name|put
argument_list|(
name|privType
operator|.
name|getToken
argument_list|()
argument_list|,
name|privType
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Do case insensitive lookup of PrivilegeType with this name    * @param privilegeName    * @return corresponding PrivilegeType    */
specifier|public
specifier|static
name|PrivilegeType
name|getPrivTypeByName
parameter_list|(
name|String
name|privilegeName
parameter_list|)
block|{
name|populateName2Type
argument_list|()
expr_stmt|;
name|String
name|canonicalizedName
init|=
name|privilegeName
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
name|PrivilegeType
name|privType
init|=
name|name2Type
operator|.
name|get
argument_list|(
name|canonicalizedName
argument_list|)
decl_stmt|;
if|if
condition|(
name|privType
operator|!=
literal|null
condition|)
block|{
return|return
name|privType
return|;
block|}
return|return
name|PrivilegeType
operator|.
name|UNKNOWN
return|;
block|}
specifier|private
specifier|static
specifier|synchronized
name|void
name|populateName2Type
parameter_list|()
block|{
if|if
condition|(
name|name2Type
operator|!=
literal|null
condition|)
block|{
return|return;
block|}
name|name2Type
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|PrivilegeType
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|PrivilegeType
name|privType
range|:
name|PrivilegeType
operator|.
name|values
argument_list|()
control|)
block|{
name|name2Type
operator|.
name|put
argument_list|(
name|privType
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|privType
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_enum

end_unit

