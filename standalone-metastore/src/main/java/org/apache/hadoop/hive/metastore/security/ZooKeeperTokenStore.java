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
name|metastore
operator|.
name|security
package|;
end_package

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
name|Arrays
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|framework
operator|.
name|CuratorFramework
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|framework
operator|.
name|CuratorFrameworkFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|framework
operator|.
name|api
operator|.
name|ACLProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|framework
operator|.
name|imps
operator|.
name|CuratorFrameworkState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|retry
operator|.
name|ExponentialBackoffRetry
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
name|metastore
operator|.
name|utils
operator|.
name|SecurityUtils
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
name|hadoop
operator|.
name|security
operator|.
name|token
operator|.
name|delegation
operator|.
name|AbstractDelegationTokenSecretManager
operator|.
name|DelegationTokenInformation
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
name|token
operator|.
name|delegation
operator|.
name|MetastoreDelegationTokenSupport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|CreateMode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooDefs
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooDefs
operator|.
name|Ids
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooDefs
operator|.
name|Perms
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|ACL
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|Id
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

begin_comment
comment|/**  * ZooKeeper token store implementation.  */
end_comment

begin_class
specifier|public
class|class
name|ZooKeeperTokenStore
implements|implements
name|DelegationTokenStore
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ZooKeeperTokenStore
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|ZK_SEQ_FORMAT
init|=
literal|"%010d"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|NODE_KEYS
init|=
literal|"/keys"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|NODE_TOKENS
init|=
literal|"/tokens"
decl_stmt|;
specifier|private
name|String
name|rootNode
init|=
literal|""
decl_stmt|;
specifier|private
specifier|volatile
name|CuratorFramework
name|zkSession
decl_stmt|;
specifier|private
name|String
name|zkConnectString
decl_stmt|;
specifier|private
name|int
name|connectTimeoutMillis
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ACL
argument_list|>
name|newNodeAcl
init|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|ACL
argument_list|(
name|Perms
operator|.
name|ALL
argument_list|,
name|Ids
operator|.
name|AUTH_IDS
argument_list|)
argument_list|)
decl_stmt|;
comment|/**    * ACLProvider permissions will be used in case parent dirs need to be created    */
specifier|private
specifier|final
name|ACLProvider
name|aclDefaultProvider
init|=
operator|new
name|ACLProvider
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ACL
argument_list|>
name|getDefaultAcl
parameter_list|()
block|{
return|return
name|newNodeAcl
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ACL
argument_list|>
name|getAclForPath
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|getDefaultAcl
argument_list|()
return|;
block|}
block|}
decl_stmt|;
specifier|private
specifier|final
name|String
name|WHEN_ZK_DSTORE_MSG
init|=
literal|"when zookeeper based delegation token storage is enabled"
operator|+
literal|"(hive.cluster.delegation.token.store.class="
operator|+
name|ZooKeeperTokenStore
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|")"
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|HadoopThriftAuthBridge
operator|.
name|Server
operator|.
name|ServerMode
name|serverMode
decl_stmt|;
comment|/**    * Default constructor for dynamic instantiation w/ Configurable    * (ReflectionUtils does not support Configuration constructor injection).    */
specifier|protected
name|ZooKeeperTokenStore
parameter_list|()
block|{   }
specifier|private
name|CuratorFramework
name|getSession
parameter_list|()
block|{
if|if
condition|(
name|zkSession
operator|==
literal|null
operator|||
name|zkSession
operator|.
name|getState
argument_list|()
operator|==
name|CuratorFrameworkState
operator|.
name|STOPPED
condition|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|zkSession
operator|==
literal|null
operator|||
name|zkSession
operator|.
name|getState
argument_list|()
operator|==
name|CuratorFrameworkState
operator|.
name|STOPPED
condition|)
block|{
name|zkSession
operator|=
name|CuratorFrameworkFactory
operator|.
name|builder
argument_list|()
operator|.
name|connectString
argument_list|(
name|zkConnectString
argument_list|)
operator|.
name|connectionTimeoutMs
argument_list|(
name|connectTimeoutMillis
argument_list|)
operator|.
name|aclProvider
argument_list|(
name|aclDefaultProvider
argument_list|)
operator|.
name|retryPolicy
argument_list|(
operator|new
name|ExponentialBackoffRetry
argument_list|(
literal|1000
argument_list|,
literal|3
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|zkSession
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|return
name|zkSession
return|;
block|}
specifier|private
name|void
name|setupJAASConfig
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
operator|.
name|isFromKeytab
argument_list|()
condition|)
block|{
comment|// The process has not logged in using keytab
comment|// this should be a test mode, can't use keytab to authenticate
comment|// with zookeeper.
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Login is not from keytab"
argument_list|)
expr_stmt|;
return|return;
block|}
name|String
name|principal
decl_stmt|;
name|String
name|keytab
decl_stmt|;
switch|switch
condition|(
name|serverMode
condition|)
block|{
case|case
name|METASTORE
case|:
name|principal
operator|=
name|getNonEmptyConfVar
argument_list|(
name|conf
argument_list|,
literal|"hive.metastore.kerberos.principal"
argument_list|)
expr_stmt|;
name|keytab
operator|=
name|getNonEmptyConfVar
argument_list|(
name|conf
argument_list|,
literal|"hive.metastore.kerberos.keytab.file"
argument_list|)
expr_stmt|;
break|break;
case|case
name|HIVESERVER2
case|:
name|principal
operator|=
name|getNonEmptyConfVar
argument_list|(
name|conf
argument_list|,
literal|"hive.server2.authentication.kerberos.principal"
argument_list|)
expr_stmt|;
name|keytab
operator|=
name|getNonEmptyConfVar
argument_list|(
name|conf
argument_list|,
literal|"hive.server2.authentication.kerberos.keytab"
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unexpected server mode "
operator|+
name|serverMode
argument_list|)
throw|;
block|}
name|SecurityUtils
operator|.
name|setZookeeperClientKerberosJaasConfig
argument_list|(
name|principal
argument_list|,
name|keytab
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|getNonEmptyConfVar
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|param
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|val
init|=
name|conf
operator|.
name|get
argument_list|(
name|param
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|==
literal|null
operator|||
name|val
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Configuration parameter "
operator|+
name|param
operator|+
literal|" should be set, "
operator|+
name|WHEN_ZK_DSTORE_MSG
argument_list|)
throw|;
block|}
return|return
name|val
return|;
block|}
comment|/**    * Create a path if it does not already exist ("mkdir -p")    * @param path string with '/' separator    * @param acl list of ACL entries    * @throws TokenStoreException    */
specifier|public
name|void
name|ensurePath
parameter_list|(
name|String
name|path
parameter_list|,
name|List
argument_list|<
name|ACL
argument_list|>
name|acl
parameter_list|)
throws|throws
name|TokenStoreException
block|{
try|try
block|{
name|CuratorFramework
name|zk
init|=
name|getSession
argument_list|()
decl_stmt|;
name|String
name|node
init|=
name|zk
operator|.
name|create
argument_list|()
operator|.
name|creatingParentsIfNeeded
argument_list|()
operator|.
name|withMode
argument_list|(
name|CreateMode
operator|.
name|PERSISTENT
argument_list|)
operator|.
name|withACL
argument_list|(
name|acl
argument_list|)
operator|.
name|forPath
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|LOGGER
operator|.
name|info
argument_list|(
literal|"Created path: {} "
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NodeExistsException
name|e
parameter_list|)
block|{
comment|// node already exists
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|TokenStoreException
argument_list|(
literal|"Error creating path "
operator|+
name|path
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Parse ACL permission string, from ZooKeeperMain private method    * @param permString    * @return    */
specifier|public
specifier|static
name|int
name|getPermFromString
parameter_list|(
name|String
name|permString
parameter_list|)
block|{
name|int
name|perm
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|permString
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
switch|switch
condition|(
name|permString
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
condition|)
block|{
case|case
literal|'r'
case|:
name|perm
operator||=
name|ZooDefs
operator|.
name|Perms
operator|.
name|READ
expr_stmt|;
break|break;
case|case
literal|'w'
case|:
name|perm
operator||=
name|ZooDefs
operator|.
name|Perms
operator|.
name|WRITE
expr_stmt|;
break|break;
case|case
literal|'c'
case|:
name|perm
operator||=
name|ZooDefs
operator|.
name|Perms
operator|.
name|CREATE
expr_stmt|;
break|break;
case|case
literal|'d'
case|:
name|perm
operator||=
name|ZooDefs
operator|.
name|Perms
operator|.
name|DELETE
expr_stmt|;
break|break;
case|case
literal|'a'
case|:
name|perm
operator||=
name|ZooDefs
operator|.
name|Perms
operator|.
name|ADMIN
expr_stmt|;
break|break;
default|default:
name|LOGGER
operator|.
name|error
argument_list|(
literal|"Unknown perm type: "
operator|+
name|permString
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|perm
return|;
block|}
comment|/**    * Parse comma separated list of ACL entries to secure generated nodes, e.g.    *<code>sasl:hive/host1@MY.DOMAIN:cdrwa,sasl:hive/host2@MY.DOMAIN:cdrwa</code>    * @param aclString    * @return ACL list    */
specifier|public
specifier|static
name|List
argument_list|<
name|ACL
argument_list|>
name|parseACLs
parameter_list|(
name|String
name|aclString
parameter_list|)
block|{
name|String
index|[]
name|aclComps
init|=
name|StringUtils
operator|.
name|splitByWholeSeparator
argument_list|(
name|aclString
argument_list|,
literal|","
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ACL
argument_list|>
name|acl
init|=
operator|new
name|ArrayList
argument_list|<
name|ACL
argument_list|>
argument_list|(
name|aclComps
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|a
range|:
name|aclComps
control|)
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|a
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|a
operator|=
name|a
operator|.
name|trim
argument_list|()
expr_stmt|;
comment|// from ZooKeeperMain private method
name|int
name|firstColon
init|=
name|a
operator|.
name|indexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
name|int
name|lastColon
init|=
name|a
operator|.
name|lastIndexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
if|if
condition|(
name|firstColon
operator|==
operator|-
literal|1
operator|||
name|lastColon
operator|==
operator|-
literal|1
operator|||
name|firstColon
operator|==
name|lastColon
condition|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
name|a
operator|+
literal|" does not have the form scheme:id:perm"
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|ACL
name|newAcl
init|=
operator|new
name|ACL
argument_list|()
decl_stmt|;
name|newAcl
operator|.
name|setId
argument_list|(
operator|new
name|Id
argument_list|(
name|a
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|firstColon
argument_list|)
argument_list|,
name|a
operator|.
name|substring
argument_list|(
name|firstColon
operator|+
literal|1
argument_list|,
name|lastColon
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|newAcl
operator|.
name|setPerms
argument_list|(
name|getPermFromString
argument_list|(
name|a
operator|.
name|substring
argument_list|(
name|lastColon
operator|+
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|acl
operator|.
name|add
argument_list|(
name|newAcl
argument_list|)
expr_stmt|;
block|}
return|return
name|acl
return|;
block|}
specifier|private
name|void
name|initClientAndPaths
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|zkSession
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|zkSession
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|ensurePath
argument_list|(
name|rootNode
operator|+
name|NODE_KEYS
argument_list|,
name|newNodeAcl
argument_list|)
expr_stmt|;
name|ensurePath
argument_list|(
name|rootNode
operator|+
name|NODE_TOKENS
argument_list|,
name|newNodeAcl
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TokenStoreException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"conf is null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
literal|null
return|;
comment|// not required
block|}
specifier|private
name|Map
argument_list|<
name|Integer
argument_list|,
name|byte
index|[]
argument_list|>
name|getAllKeys
parameter_list|()
throws|throws
name|KeeperException
throws|,
name|InterruptedException
block|{
name|String
name|masterKeyNode
init|=
name|rootNode
operator|+
name|NODE_KEYS
decl_stmt|;
comment|// get children of key node
name|List
argument_list|<
name|String
argument_list|>
name|nodes
init|=
name|zkGetChildren
argument_list|(
name|masterKeyNode
argument_list|)
decl_stmt|;
comment|// read each child node, add to results
name|Map
argument_list|<
name|Integer
argument_list|,
name|byte
index|[]
argument_list|>
name|result
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|node
range|:
name|nodes
control|)
block|{
name|String
name|nodePath
init|=
name|masterKeyNode
operator|+
literal|"/"
operator|+
name|node
decl_stmt|;
name|byte
index|[]
name|data
init|=
name|zkGetData
argument_list|(
name|nodePath
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|put
argument_list|(
name|getSeq
argument_list|(
name|node
argument_list|)
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|zkGetChildren
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|CuratorFramework
name|zk
init|=
name|getSession
argument_list|()
decl_stmt|;
try|try
block|{
return|return
name|zk
operator|.
name|getChildren
argument_list|()
operator|.
name|forPath
argument_list|(
name|path
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
name|TokenStoreException
argument_list|(
literal|"Error getting children for "
operator|+
name|path
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|byte
index|[]
name|zkGetData
parameter_list|(
name|String
name|nodePath
parameter_list|)
block|{
name|CuratorFramework
name|zk
init|=
name|getSession
argument_list|()
decl_stmt|;
try|try
block|{
return|return
name|zk
operator|.
name|getData
argument_list|()
operator|.
name|forPath
argument_list|(
name|nodePath
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|ex
parameter_list|)
block|{
return|return
literal|null
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
name|TokenStoreException
argument_list|(
literal|"Error reading "
operator|+
name|nodePath
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|int
name|getSeq
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|String
index|[]
name|pathComps
init|=
name|path
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
return|return
name|Integer
operator|.
name|parseInt
argument_list|(
name|pathComps
index|[
name|pathComps
operator|.
name|length
operator|-
literal|1
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|addMasterKey
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|String
name|keysPath
init|=
name|rootNode
operator|+
name|NODE_KEYS
operator|+
literal|"/"
decl_stmt|;
name|CuratorFramework
name|zk
init|=
name|getSession
argument_list|()
decl_stmt|;
name|String
name|newNode
decl_stmt|;
try|try
block|{
name|newNode
operator|=
name|zk
operator|.
name|create
argument_list|()
operator|.
name|withMode
argument_list|(
name|CreateMode
operator|.
name|PERSISTENT_SEQUENTIAL
argument_list|)
operator|.
name|withACL
argument_list|(
name|newNodeAcl
argument_list|)
operator|.
name|forPath
argument_list|(
name|keysPath
argument_list|,
name|s
operator|.
name|getBytes
argument_list|()
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
name|TokenStoreException
argument_list|(
literal|"Error creating new node with path "
operator|+
name|keysPath
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|LOGGER
operator|.
name|info
argument_list|(
literal|"Added key {}"
argument_list|,
name|newNode
argument_list|)
expr_stmt|;
return|return
name|getSeq
argument_list|(
name|newNode
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateMasterKey
parameter_list|(
name|int
name|keySeq
parameter_list|,
name|String
name|s
parameter_list|)
block|{
name|CuratorFramework
name|zk
init|=
name|getSession
argument_list|()
decl_stmt|;
name|String
name|keyPath
init|=
name|rootNode
operator|+
name|NODE_KEYS
operator|+
literal|"/"
operator|+
name|String
operator|.
name|format
argument_list|(
name|ZK_SEQ_FORMAT
argument_list|,
name|keySeq
argument_list|)
decl_stmt|;
try|try
block|{
name|zk
operator|.
name|setData
argument_list|()
operator|.
name|forPath
argument_list|(
name|keyPath
argument_list|,
name|s
operator|.
name|getBytes
argument_list|()
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
name|TokenStoreException
argument_list|(
literal|"Error setting data in "
operator|+
name|keyPath
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|removeMasterKey
parameter_list|(
name|int
name|keySeq
parameter_list|)
block|{
name|String
name|keyPath
init|=
name|rootNode
operator|+
name|NODE_KEYS
operator|+
literal|"/"
operator|+
name|String
operator|.
name|format
argument_list|(
name|ZK_SEQ_FORMAT
argument_list|,
name|keySeq
argument_list|)
decl_stmt|;
name|zkDelete
argument_list|(
name|keyPath
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|private
name|void
name|zkDelete
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|CuratorFramework
name|zk
init|=
name|getSession
argument_list|()
decl_stmt|;
try|try
block|{
name|zk
operator|.
name|delete
argument_list|()
operator|.
name|forPath
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|ex
parameter_list|)
block|{
comment|// already deleted
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|TokenStoreException
argument_list|(
literal|"Error deleting "
operator|+
name|path
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getMasterKeys
parameter_list|()
block|{
try|try
block|{
name|Map
argument_list|<
name|Integer
argument_list|,
name|byte
index|[]
argument_list|>
name|allKeys
init|=
name|getAllKeys
argument_list|()
decl_stmt|;
name|String
index|[]
name|result
init|=
operator|new
name|String
index|[
name|allKeys
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|resultIdx
init|=
literal|0
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|keyBytes
range|:
name|allKeys
operator|.
name|values
argument_list|()
control|)
block|{
name|result
index|[
name|resultIdx
operator|++
index|]
operator|=
operator|new
name|String
argument_list|(
name|keyBytes
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|TokenStoreException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|TokenStoreException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
specifier|private
name|String
name|getTokenPath
parameter_list|(
name|DelegationTokenIdentifier
name|tokenIdentifier
parameter_list|)
block|{
try|try
block|{
return|return
name|rootNode
operator|+
name|NODE_TOKENS
operator|+
literal|"/"
operator|+
name|TokenStoreDelegationTokenSecretManager
operator|.
name|encodeWritable
argument_list|(
name|tokenIdentifier
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|TokenStoreException
argument_list|(
literal|"Failed to encode token identifier"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|addToken
parameter_list|(
name|DelegationTokenIdentifier
name|tokenIdentifier
parameter_list|,
name|DelegationTokenInformation
name|token
parameter_list|)
block|{
name|byte
index|[]
name|tokenBytes
init|=
name|MetastoreDelegationTokenSupport
operator|.
name|encodeDelegationTokenInformation
argument_list|(
name|token
argument_list|)
decl_stmt|;
name|String
name|tokenPath
init|=
name|getTokenPath
argument_list|(
name|tokenIdentifier
argument_list|)
decl_stmt|;
name|CuratorFramework
name|zk
init|=
name|getSession
argument_list|()
decl_stmt|;
name|String
name|newNode
decl_stmt|;
try|try
block|{
name|newNode
operator|=
name|zk
operator|.
name|create
argument_list|()
operator|.
name|withMode
argument_list|(
name|CreateMode
operator|.
name|PERSISTENT
argument_list|)
operator|.
name|withACL
argument_list|(
name|newNodeAcl
argument_list|)
operator|.
name|forPath
argument_list|(
name|tokenPath
argument_list|,
name|tokenBytes
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
name|TokenStoreException
argument_list|(
literal|"Error creating new node with path "
operator|+
name|tokenPath
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|LOGGER
operator|.
name|info
argument_list|(
literal|"Added token: {}"
argument_list|,
name|newNode
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|removeToken
parameter_list|(
name|DelegationTokenIdentifier
name|tokenIdentifier
parameter_list|)
block|{
name|String
name|tokenPath
init|=
name|getTokenPath
argument_list|(
name|tokenIdentifier
argument_list|)
decl_stmt|;
name|zkDelete
argument_list|(
name|tokenPath
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|DelegationTokenInformation
name|getToken
parameter_list|(
name|DelegationTokenIdentifier
name|tokenIdentifier
parameter_list|)
block|{
name|byte
index|[]
name|tokenBytes
init|=
name|zkGetData
argument_list|(
name|getTokenPath
argument_list|(
name|tokenIdentifier
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokenBytes
operator|==
literal|null
condition|)
block|{
comment|// The token is already removed.
return|return
literal|null
return|;
block|}
try|try
block|{
return|return
name|MetastoreDelegationTokenSupport
operator|.
name|decodeDelegationTokenInformation
argument_list|(
name|tokenBytes
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
name|TokenStoreException
argument_list|(
literal|"Failed to decode token"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
name|getAllDelegationTokenIdentifiers
parameter_list|()
block|{
name|String
name|containerNode
init|=
name|rootNode
operator|+
name|NODE_TOKENS
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|nodes
init|=
name|zkGetChildren
argument_list|(
name|containerNode
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
name|result
init|=
operator|new
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
argument_list|(
name|nodes
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|node
range|:
name|nodes
control|)
block|{
name|DelegationTokenIdentifier
name|id
init|=
operator|new
name|DelegationTokenIdentifier
argument_list|()
decl_stmt|;
try|try
block|{
name|TokenStoreDelegationTokenSecretManager
operator|.
name|decodeWritable
argument_list|(
name|id
argument_list|,
name|node
argument_list|)
expr_stmt|;
name|result
operator|.
name|add
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Failed to decode token '{}'"
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|zkSession
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|zkSession
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Object
name|hmsHandler
parameter_list|,
name|HadoopThriftAuthBridge
operator|.
name|Server
operator|.
name|ServerMode
name|sMode
parameter_list|)
block|{
name|this
operator|.
name|serverMode
operator|=
name|sMode
expr_stmt|;
name|zkConnectString
operator|=
name|conf
operator|.
name|get
argument_list|(
name|MetastoreDelegationTokenManager
operator|.
name|DELEGATION_TOKEN_STORE_ZK_CONNECT_STR
argument_list|,
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|zkConnectString
operator|==
literal|null
operator|||
name|zkConnectString
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// try alternate config param
name|zkConnectString
operator|=
name|conf
operator|.
name|get
argument_list|(
name|MetastoreDelegationTokenManager
operator|.
name|DELEGATION_TOKEN_STORE_ZK_CONNECT_STR_ALTERNATE
argument_list|,
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|zkConnectString
operator|==
literal|null
operator|||
name|zkConnectString
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Zookeeper connect string has to be specified through "
operator|+
literal|"either "
operator|+
name|MetastoreDelegationTokenManager
operator|.
name|DELEGATION_TOKEN_STORE_ZK_CONNECT_STR
operator|+
literal|" or "
operator|+
name|MetastoreDelegationTokenManager
operator|.
name|DELEGATION_TOKEN_STORE_ZK_CONNECT_STR_ALTERNATE
operator|+
name|WHEN_ZK_DSTORE_MSG
argument_list|)
throw|;
block|}
block|}
name|connectTimeoutMillis
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|MetastoreDelegationTokenManager
operator|.
name|DELEGATION_TOKEN_STORE_ZK_CONNECT_TIMEOUTMILLIS
argument_list|,
name|CuratorFrameworkFactory
operator|.
name|builder
argument_list|()
operator|.
name|getConnectionTimeoutMs
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|aclStr
init|=
name|conf
operator|.
name|get
argument_list|(
name|MetastoreDelegationTokenManager
operator|.
name|DELEGATION_TOKEN_STORE_ZK_ACL
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|aclStr
argument_list|)
condition|)
block|{
name|this
operator|.
name|newNodeAcl
operator|=
name|parseACLs
argument_list|(
name|aclStr
argument_list|)
expr_stmt|;
block|}
name|rootNode
operator|=
name|conf
operator|.
name|get
argument_list|(
name|MetastoreDelegationTokenManager
operator|.
name|DELEGATION_TOKEN_STORE_ZK_ZNODE
argument_list|,
name|MetastoreDelegationTokenManager
operator|.
name|DELEGATION_TOKEN_STORE_ZK_ZNODE_DEFAULT
argument_list|)
operator|+
name|serverMode
expr_stmt|;
try|try
block|{
comment|// Install the JAAS Configuration for the runtime
name|setupJAASConfig
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|TokenStoreException
argument_list|(
literal|"Error setting up JAAS configuration for zookeeper client "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|initClientAndPaths
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

