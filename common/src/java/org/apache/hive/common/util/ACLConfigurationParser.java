begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|lang3
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|classification
operator|.
name|InterfaceAudience
operator|.
name|Private
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_comment
comment|/**  * Parser for extracting ACL information from Configs  */
end_comment

begin_class
annotation|@
name|Private
specifier|public
class|class
name|ACLConfigurationParser
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ACLConfigurationParser
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|WILDCARD_ACL_VALUE
init|=
literal|"*"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
name|splitPattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\s+"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|allowedUsers
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|allowedGroups
decl_stmt|;
specifier|public
name|ACLConfigurationParser
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|confPropertyName
parameter_list|)
block|{
name|allowedUsers
operator|=
name|Sets
operator|.
name|newLinkedHashSet
argument_list|()
expr_stmt|;
name|allowedGroups
operator|=
name|Sets
operator|.
name|newLinkedHashSet
argument_list|()
expr_stmt|;
name|parse
argument_list|(
name|conf
argument_list|,
name|confPropertyName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|isWildCard
parameter_list|(
name|String
name|aclStr
parameter_list|)
block|{
return|return
name|aclStr
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
name|WILDCARD_ACL_VALUE
argument_list|)
return|;
block|}
specifier|private
name|void
name|parse
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|configProperty
parameter_list|)
block|{
name|String
name|aclsStr
init|=
name|conf
operator|.
name|get
argument_list|(
name|configProperty
argument_list|)
decl_stmt|;
if|if
condition|(
name|aclsStr
operator|==
literal|null
operator|||
name|aclsStr
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|isWildCard
argument_list|(
name|aclsStr
argument_list|)
condition|)
block|{
name|allowedUsers
operator|.
name|add
argument_list|(
name|WILDCARD_ACL_VALUE
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|String
index|[]
name|splits
init|=
name|splitPattern
operator|.
name|split
argument_list|(
name|aclsStr
argument_list|)
decl_stmt|;
name|int
name|counter
init|=
operator|-
literal|1
decl_stmt|;
name|String
name|userListStr
init|=
literal|null
decl_stmt|;
name|String
name|groupListStr
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|splits
control|)
block|{
if|if
condition|(
name|s
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|userListStr
operator|!=
literal|null
condition|)
block|{
continue|continue;
block|}
block|}
operator|++
name|counter
expr_stmt|;
if|if
condition|(
name|counter
operator|==
literal|0
condition|)
block|{
name|userListStr
operator|=
name|s
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|counter
operator|==
literal|1
condition|)
block|{
name|groupListStr
operator|=
name|s
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Invalid configuration specified for "
operator|+
name|configProperty
operator|+
literal|", ignoring configured ACLs, value="
operator|+
name|aclsStr
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
if|if
condition|(
name|userListStr
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|userListStr
operator|.
name|length
argument_list|()
operator|>=
literal|1
condition|)
block|{
name|allowedUsers
operator|.
name|addAll
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|getTrimmedStringCollection
argument_list|(
name|userListStr
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|groupListStr
operator|!=
literal|null
operator|&&
name|groupListStr
operator|.
name|length
argument_list|()
operator|>=
literal|1
condition|)
block|{
name|allowedGroups
operator|.
name|addAll
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|getTrimmedStringCollection
argument_list|(
name|groupListStr
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getAllowedUsers
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|allowedUsers
argument_list|)
return|;
block|}
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getAllowedGroups
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|allowedGroups
argument_list|)
return|;
block|}
specifier|public
name|void
name|addAllowedUser
parameter_list|(
name|String
name|user
parameter_list|)
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|user
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|allowedUsers
operator|.
name|contains
argument_list|(
name|WILDCARD_ACL_VALUE
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|user
operator|.
name|equals
argument_list|(
name|WILDCARD_ACL_VALUE
argument_list|)
condition|)
block|{
name|allowedUsers
operator|.
name|clear
argument_list|()
expr_stmt|;
name|allowedGroups
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|allowedUsers
operator|.
name|add
argument_list|(
name|user
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addAllowedGroup
parameter_list|(
name|String
name|group
parameter_list|)
block|{
name|allowedGroups
operator|.
name|add
argument_list|(
name|group
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|toAclString
parameter_list|()
block|{
return|return
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|getAllowedUsers
argument_list|()
operator|.
name|contains
argument_list|(
name|WILDCARD_ACL_VALUE
argument_list|)
condition|)
block|{
return|return
name|WILDCARD_ACL_VALUE
return|;
block|}
else|else
block|{
if|if
condition|(
name|allowedUsers
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|allowedGroups
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|" "
return|;
block|}
name|String
name|userString
init|=
name|constructCsv
argument_list|(
name|allowedUsers
argument_list|)
decl_stmt|;
name|String
name|groupString
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|allowedGroups
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|groupString
operator|=
literal|" "
operator|+
name|constructCsv
argument_list|(
name|allowedGroups
argument_list|)
expr_stmt|;
block|}
return|return
name|userString
operator|+
name|groupString
return|;
block|}
block|}
specifier|private
name|String
name|constructCsv
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|inSet
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|inSet
operator|!=
literal|null
condition|)
block|{
name|boolean
name|isFirst
init|=
literal|true
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|inSet
control|)
block|{
if|if
condition|(
operator|!
name|isFirst
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|isFirst
operator|=
literal|false
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

