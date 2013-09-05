begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
package|;
end_package

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
name|security
operator|.
name|Groups
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
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|MessageFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

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
name|HashMap
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
name|Map
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

begin_comment
comment|/**  * When WebHCat is run with doAs query parameter this class ensures that user making the  * call is allowed to impersonate doAs user and is making a call from authorized host.  */
end_comment

begin_class
specifier|final
class|class
name|ProxyUserSupport
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ProxyUserSupport
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CONF_PROXYUSER_PREFIX
init|=
literal|"webhcat.proxyuser."
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CONF_GROUPS_SUFFIX
init|=
literal|".groups"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CONF_HOSTS_SUFFIX
init|=
literal|".hosts"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|WILD_CARD
init|=
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|proxyUserGroups
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|proxyUserHosts
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|static
name|void
name|processProxyuserConfig
parameter_list|(
name|AppConfig
name|conf
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confEnt
range|:
name|conf
control|)
block|{
if|if
condition|(
name|confEnt
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
name|CONF_PROXYUSER_PREFIX
argument_list|)
operator|&&
name|confEnt
operator|.
name|getKey
argument_list|()
operator|.
name|endsWith
argument_list|(
name|CONF_GROUPS_SUFFIX
argument_list|)
condition|)
block|{
comment|//process user groups for which doAs is authorized
name|String
name|proxyUser
init|=
name|confEnt
operator|.
name|getKey
argument_list|()
operator|.
name|substring
argument_list|(
name|CONF_PROXYUSER_PREFIX
operator|.
name|length
argument_list|()
argument_list|,
name|confEnt
operator|.
name|getKey
argument_list|()
operator|.
name|lastIndexOf
argument_list|(
name|CONF_GROUPS_SUFFIX
argument_list|)
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|groups
decl_stmt|;
if|if
condition|(
literal|"*"
operator|.
name|equals
argument_list|(
name|confEnt
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
name|groups
operator|=
name|WILD_CARD
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"User ["
operator|+
name|proxyUser
operator|+
literal|"] is authorized to do doAs any user."
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|confEnt
operator|.
name|getValue
argument_list|()
operator|!=
literal|null
operator|&&
name|confEnt
operator|.
name|getValue
argument_list|()
operator|.
name|trim
argument_list|()
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|groups
operator|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|confEnt
operator|.
name|getValue
argument_list|()
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"User ["
operator|+
name|proxyUser
operator|+
literal|"] is authorized to do doAs for users in the following groups: ["
operator|+
name|confEnt
operator|.
name|getValue
argument_list|()
operator|.
name|trim
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|groups
operator|=
name|Collections
operator|.
name|emptySet
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"User ["
operator|+
name|proxyUser
operator|+
literal|"] is authorized to do doAs for users in the following groups: []"
argument_list|)
expr_stmt|;
block|}
block|}
name|proxyUserGroups
operator|.
name|put
argument_list|(
name|proxyUser
argument_list|,
name|groups
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|confEnt
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
name|CONF_PROXYUSER_PREFIX
argument_list|)
operator|&&
name|confEnt
operator|.
name|getKey
argument_list|()
operator|.
name|endsWith
argument_list|(
name|CONF_HOSTS_SUFFIX
argument_list|)
condition|)
block|{
comment|//process hosts from which doAs requests are authorized
name|String
name|proxyUser
init|=
name|confEnt
operator|.
name|getKey
argument_list|()
operator|.
name|substring
argument_list|(
name|CONF_PROXYUSER_PREFIX
operator|.
name|length
argument_list|()
argument_list|,
name|confEnt
operator|.
name|getKey
argument_list|()
operator|.
name|lastIndexOf
argument_list|(
name|CONF_HOSTS_SUFFIX
argument_list|)
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|hosts
decl_stmt|;
if|if
condition|(
literal|"*"
operator|.
name|equals
argument_list|(
name|confEnt
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
name|hosts
operator|=
name|WILD_CARD
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"User ["
operator|+
name|proxyUser
operator|+
literal|"] is authorized to do doAs from any host."
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|confEnt
operator|.
name|getValue
argument_list|()
operator|!=
literal|null
operator|&&
name|confEnt
operator|.
name|getValue
argument_list|()
operator|.
name|trim
argument_list|()
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|String
index|[]
name|hostValues
init|=
name|confEnt
operator|.
name|getValue
argument_list|()
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|hosts
operator|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|hostname
range|:
name|hostValues
control|)
block|{
name|String
name|nhn
init|=
name|normalizeHostname
argument_list|(
name|hostname
argument_list|)
decl_stmt|;
if|if
condition|(
name|nhn
operator|!=
literal|null
condition|)
block|{
name|hosts
operator|.
name|add
argument_list|(
name|nhn
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"User ["
operator|+
name|proxyUser
operator|+
literal|"] is authorized to do doAs from the following hosts: ["
operator|+
name|confEnt
operator|.
name|getValue
argument_list|()
operator|.
name|trim
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|hosts
operator|=
name|Collections
operator|.
name|emptySet
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"User ["
operator|+
name|proxyUser
operator|+
literal|"] is authorized to do doAs from the following hosts: []"
argument_list|)
expr_stmt|;
block|}
block|}
name|proxyUserHosts
operator|.
name|put
argument_list|(
name|proxyUser
argument_list|,
name|hosts
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Verifies a that proxyUser is making the request from authorized host and that doAs user      * belongs to one of the groups for which proxyUser is allowed to impersonate users.      *      * @param proxyUser user name of the proxy (logged in) user.      * @param proxyHost host the proxy user is making the request from.      * @param doAsUser user the proxy user is impersonating.      * @throws NotAuthorizedException thrown if the user is not allowed to perform the proxyuser request.      */
specifier|static
name|void
name|validate
parameter_list|(
name|String
name|proxyUser
parameter_list|,
name|String
name|proxyHost
parameter_list|,
name|String
name|doAsUser
parameter_list|)
throws|throws
name|NotAuthorizedException
block|{
name|assertNotEmpty
argument_list|(
name|proxyUser
argument_list|,
literal|"proxyUser"
argument_list|,
literal|"If you're attempting to use user-impersonation via a proxy user, please make sure that "
operator|+
name|CONF_PROXYUSER_PREFIX
operator|+
literal|"#USER#"
operator|+
name|CONF_HOSTS_SUFFIX
operator|+
literal|" and "
operator|+
name|CONF_PROXYUSER_PREFIX
operator|+
literal|"#USER#"
operator|+
name|CONF_GROUPS_SUFFIX
operator|+
literal|" are configured correctly"
argument_list|)
expr_stmt|;
name|assertNotEmpty
argument_list|(
name|proxyHost
argument_list|,
literal|"proxyHost"
argument_list|,
literal|"If you're attempting to use user-impersonation via a proxy user, please make sure that "
operator|+
name|CONF_PROXYUSER_PREFIX
operator|+
name|proxyUser
operator|+
name|CONF_HOSTS_SUFFIX
operator|+
literal|" and "
operator|+
name|CONF_PROXYUSER_PREFIX
operator|+
name|proxyUser
operator|+
name|CONF_GROUPS_SUFFIX
operator|+
literal|" are configured correctly"
argument_list|)
expr_stmt|;
name|assertNotEmpty
argument_list|(
name|doAsUser
argument_list|,
name|Server
operator|.
name|DO_AS_PARAM
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"Authorization check proxyuser [{0}] host [{1}] doAs [{2}]"
argument_list|,
name|proxyUser
argument_list|,
name|proxyHost
argument_list|,
name|doAsUser
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|proxyUserHosts
operator|.
name|containsKey
argument_list|(
name|proxyUser
argument_list|)
condition|)
block|{
name|proxyHost
operator|=
name|normalizeHostname
argument_list|(
name|proxyHost
argument_list|)
expr_stmt|;
name|validateRequestorHost
argument_list|(
name|proxyUser
argument_list|,
name|proxyHost
argument_list|)
expr_stmt|;
name|validateGroup
argument_list|(
name|proxyUser
argument_list|,
name|doAsUser
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|NotAuthorizedException
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"User [{0}] not defined as proxyuser"
argument_list|,
name|proxyUser
argument_list|)
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|validateRequestorHost
parameter_list|(
name|String
name|proxyUser
parameter_list|,
name|String
name|hostname
parameter_list|)
throws|throws
name|NotAuthorizedException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|validHosts
init|=
name|proxyUserHosts
operator|.
name|get
argument_list|(
name|proxyUser
argument_list|)
decl_stmt|;
if|if
condition|(
name|validHosts
operator|==
name|WILD_CARD
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|validHosts
operator|==
literal|null
operator|||
operator|!
name|validHosts
operator|.
name|contains
argument_list|(
name|hostname
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|NotAuthorizedException
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"Unauthorized host [{0}] for proxyuser [{1}]"
argument_list|,
name|hostname
argument_list|,
name|proxyUser
argument_list|)
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|validateGroup
parameter_list|(
name|String
name|proxyUser
parameter_list|,
name|String
name|doAsUser
parameter_list|)
throws|throws
name|NotAuthorizedException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|validGroups
init|=
name|proxyUserGroups
operator|.
name|get
argument_list|(
name|proxyUser
argument_list|)
decl_stmt|;
if|if
condition|(
name|validGroups
operator|==
name|WILD_CARD
condition|)
block|{
return|return;
block|}
elseif|else
if|if
condition|(
name|validGroups
operator|==
literal|null
operator|||
name|validGroups
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|NotAuthorizedException
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"Unauthorized proxyuser [{0}] for doAsUser [{1}], not in proxyuser groups"
argument_list|,
name|proxyUser
argument_list|,
name|doAsUser
argument_list|)
argument_list|)
throw|;
block|}
name|Groups
name|groupsInfo
init|=
operator|new
name|Groups
argument_list|(
name|Main
operator|.
name|getAppConfigInstance
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|String
argument_list|>
name|userGroups
init|=
name|groupsInfo
operator|.
name|getGroups
argument_list|(
name|doAsUser
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|g
range|:
name|validGroups
control|)
block|{
if|if
condition|(
name|userGroups
operator|.
name|contains
argument_list|(
name|g
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|//thrown, for example, if there is no such user on the system
name|LOG
operator|.
name|warn
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"Unable to get list of groups for doAsUser [{0}]."
argument_list|,
name|doAsUser
argument_list|)
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|NotAuthorizedException
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"Unauthorized proxyuser [{0}] for doAsUser [{1}], not in proxyuser groups"
argument_list|,
name|proxyUser
argument_list|,
name|doAsUser
argument_list|)
argument_list|)
throw|;
block|}
specifier|private
specifier|static
name|String
name|normalizeHostname
parameter_list|(
name|String
name|name
parameter_list|)
block|{
try|try
block|{
name|InetAddress
name|address
init|=
name|InetAddress
operator|.
name|getByName
argument_list|(
literal|"localhost"
operator|.
name|equalsIgnoreCase
argument_list|(
name|name
argument_list|)
condition|?
literal|null
else|:
name|name
argument_list|)
decl_stmt|;
return|return
name|address
operator|.
name|getCanonicalHostName
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"Unable to normalize hostname [{0}]"
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**      * Check that a string is not null and not empty. If null or empty       * throws an IllegalArgumentException.      *      * @param str value.      * @param name parameter name for the exception message.      * @return the given value.      */
specifier|private
specifier|static
name|String
name|assertNotEmpty
parameter_list|(
name|String
name|str
parameter_list|,
name|String
name|name
parameter_list|)
block|{
return|return
name|assertNotEmpty
argument_list|(
name|str
argument_list|,
name|name
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**      * Check that a string is not null and not empty. If null or empty       * throws an IllegalArgumentException.      *      * @param str value.      * @param name parameter name for the exception message.      * @param info additional information to be printed with the exception message      * @return the given value.      */
specifier|private
specifier|static
name|String
name|assertNotEmpty
parameter_list|(
name|String
name|str
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|info
parameter_list|)
block|{
if|if
condition|(
name|str
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|name
operator|+
literal|" cannot be null"
operator|+
operator|(
name|info
operator|==
literal|null
condition|?
literal|""
else|:
literal|", "
operator|+
name|info
operator|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|str
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|name
operator|+
literal|" cannot be empty"
operator|+
operator|(
name|info
operator|==
literal|null
condition|?
literal|""
else|:
literal|", "
operator|+
name|info
operator|)
argument_list|)
throw|;
block|}
return|return
name|str
return|;
block|}
block|}
end_class

end_unit

