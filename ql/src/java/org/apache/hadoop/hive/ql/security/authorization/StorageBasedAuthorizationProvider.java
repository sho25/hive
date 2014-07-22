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
name|io
operator|.
name|FileNotFoundException
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
name|security
operator|.
name|AccessControlException
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
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
name|fs
operator|.
name|FileStatus
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
name|FileSystem
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
name|fs
operator|.
name|permission
operator|.
name|FsAction
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
name|permission
operator|.
name|FsPermission
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
name|hdfs
operator|.
name|DFSConfigKeys
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
name|HiveMetaStore
operator|.
name|HMSHandler
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
name|MetaException
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

begin_comment
comment|/**  * StorageBasedAuthorizationProvider is an implementation of  * HiveMetastoreAuthorizationProvider that tries to look at the hdfs  * permissions of files and directories associated with objects like  * databases, tables and partitions to determine whether or not an  * operation is allowed. The rule of thumb for which location to check  * in hdfs is as follows:  *  * CREATE : on location specified, or on location determined from metadata  * READS : not checked (the preeventlistener does not have an event to fire)  * UPDATES : on location in metadata  * DELETES : on location in metadata  *  * If the location does not yet exist, as the case is with creates, it steps  * out to the parent directory recursively to determine its permissions till  * it finds a parent that does exist.  */
end_comment

begin_class
specifier|public
class|class
name|StorageBasedAuthorizationProvider
extends|extends
name|HiveAuthorizationProviderBase
implements|implements
name|HiveMetastoreAuthorizationProvider
block|{
specifier|private
name|Warehouse
name|wh
decl_stmt|;
specifier|private
name|boolean
name|isRunFromMetaStore
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|StorageBasedAuthorizationProvider
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Make sure that the warehouse variable is set up properly.    * @throws MetaException if unable to instantiate    */
specifier|private
name|void
name|initWh
parameter_list|()
throws|throws
name|MetaException
throws|,
name|HiveException
block|{
if|if
condition|(
name|wh
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|isRunFromMetaStore
condition|)
block|{
comment|// Note, although HiveProxy has a method that allows us to check if we're being
comment|// called from the metastore or from the client, we don't have an initialized HiveProxy
comment|// till we explicitly initialize it as being from the client side. So, we have a
comment|// chicken-and-egg problem. So, we now track whether or not we're running from client-side
comment|// in the SBAP itself.
name|hive_db
operator|=
operator|new
name|HiveProxy
argument_list|(
name|Hive
operator|.
name|get
argument_list|(
operator|new
name|HiveConf
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|StorageBasedAuthorizationProvider
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|wh
operator|=
operator|new
name|Warehouse
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|wh
operator|==
literal|null
condition|)
block|{
comment|// If wh is still null after just having initialized it, bail out - something's very wrong.
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unable to initialize Warehouse from clientside."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|// not good if we reach here, this was initialized at setMetaStoreHandler() time.
comment|// this means handler.getWh() is returning null. Error out.
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Uninitialized Warehouse from MetastoreHandler"
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|hive_db
operator|=
operator|new
name|HiveProxy
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Privilege
index|[]
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
comment|// Currently not used in hive code-base, but intended to authorize actions
comment|// that are directly user-level. As there's no storage based aspect to this,
comment|// we can follow one of two routes:
comment|// a) We can allow by default - that way, this call stays out of the way
comment|// b) We can deny by default - that way, no privileges are authorized that
comment|// is not understood and explicitly allowed.
comment|// Both approaches have merit, but given that things like grants and revokes
comment|// that are user-level do not make sense from the context of storage-permission
comment|// based auth, denying seems to be more canonical here.
comment|// Update to previous comment: there does seem to be one place that uses this
comment|// and that is to authorize "show databases" in hcat commandline, which is used
comment|// by webhcat. And user-level auth seems to be a resonable default in this case.
comment|// The now deprecated HdfsAuthorizationProvider in hcatalog approached this in
comment|// another way, and that was to see if the user had said above appropriate requested
comment|// privileges for the hive root warehouse directory. That seems to be the best
comment|// mapping for user level privileges to storage. Using that strategy here.
name|Path
name|root
init|=
literal|null
decl_stmt|;
try|try
block|{
name|initWh
argument_list|()
expr_stmt|;
name|root
operator|=
name|wh
operator|.
name|getWhRoot
argument_list|()
expr_stmt|;
name|authorize
argument_list|(
name|root
argument_list|,
name|readRequiredPriv
argument_list|,
name|writeRequiredPriv
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|ex
parameter_list|)
block|{
throw|throw
name|hiveException
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
name|authorize
parameter_list|(
name|Database
name|db
parameter_list|,
name|Privilege
index|[]
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
name|Path
name|path
init|=
name|getDbLocation
argument_list|(
name|db
argument_list|)
decl_stmt|;
name|authorize
argument_list|(
name|path
argument_list|,
name|readRequiredPriv
argument_list|,
name|writeRequiredPriv
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
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
comment|// Table path can be null in the case of a new create table - in this case,
comment|// we try to determine what the path would be after the create table is issued.
name|Path
name|path
init|=
literal|null
decl_stmt|;
try|try
block|{
name|initWh
argument_list|()
expr_stmt|;
name|String
name|location
init|=
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
decl_stmt|;
if|if
condition|(
name|location
operator|==
literal|null
operator|||
name|location
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|path
operator|=
name|wh
operator|.
name|getTablePath
argument_list|(
name|hive_db
operator|.
name|getDatabase
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|)
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|path
operator|=
operator|new
name|Path
argument_list|(
name|location
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|MetaException
name|ex
parameter_list|)
block|{
throw|throw
name|hiveException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
name|authorize
argument_list|(
name|path
argument_list|,
name|readRequiredPriv
argument_list|,
name|writeRequiredPriv
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
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
name|authorize
argument_list|(
name|part
operator|.
name|getTable
argument_list|()
argument_list|,
name|part
argument_list|,
name|readRequiredPriv
argument_list|,
name|writeRequiredPriv
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|authorize
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|part
parameter_list|,
name|Privilege
index|[]
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
comment|// Partition path can be null in the case of a new create partition - in this case,
comment|// we try to default to checking the permissions of the parent table.
comment|// Partition itself can also be null, in cases where this gets called as a generic
comment|// catch-all call in cases like those with CTAS onto an unpartitioned table (see HIVE-1887)
if|if
condition|(
operator|(
name|part
operator|==
literal|null
operator|)
operator|||
operator|(
name|part
operator|.
name|getLocation
argument_list|()
operator|==
literal|null
operator|)
condition|)
block|{
name|authorize
argument_list|(
name|table
argument_list|,
name|readRequiredPriv
argument_list|,
name|writeRequiredPriv
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|authorize
argument_list|(
name|part
operator|.
name|getDataLocation
argument_list|()
argument_list|,
name|readRequiredPriv
argument_list|,
name|writeRequiredPriv
argument_list|)
expr_stmt|;
block|}
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
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
comment|// In a simple storage-based auth, we have no information about columns
comment|// living in different files, so we do simple partition-auth and ignore
comment|// the columns parameter.
if|if
condition|(
operator|(
name|part
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|part
operator|.
name|getTable
argument_list|()
operator|!=
literal|null
operator|)
condition|)
block|{
name|authorize
argument_list|(
name|part
operator|.
name|getTable
argument_list|()
argument_list|,
name|part
argument_list|,
name|readRequiredPriv
argument_list|,
name|writeRequiredPriv
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|authorize
argument_list|(
name|table
argument_list|,
name|part
argument_list|,
name|readRequiredPriv
argument_list|,
name|writeRequiredPriv
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setMetaStoreHandler
parameter_list|(
name|HMSHandler
name|handler
parameter_list|)
block|{
name|hive_db
operator|.
name|setHandler
argument_list|(
name|handler
argument_list|)
expr_stmt|;
name|this
operator|.
name|wh
operator|=
name|handler
operator|.
name|getWh
argument_list|()
expr_stmt|;
name|this
operator|.
name|isRunFromMetaStore
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * Given a privilege, return what FsActions are required    */
specifier|protected
name|FsAction
name|getFsAction
parameter_list|(
name|Privilege
name|priv
parameter_list|)
block|{
switch|switch
condition|(
name|priv
operator|.
name|getPriv
argument_list|()
condition|)
block|{
case|case
name|ALL
case|:
return|return
name|FsAction
operator|.
name|READ_WRITE
return|;
case|case
name|ALTER_DATA
case|:
return|return
name|FsAction
operator|.
name|WRITE
return|;
case|case
name|ALTER_METADATA
case|:
return|return
name|FsAction
operator|.
name|WRITE
return|;
case|case
name|CREATE
case|:
return|return
name|FsAction
operator|.
name|WRITE
return|;
case|case
name|DROP
case|:
return|return
name|FsAction
operator|.
name|WRITE
return|;
case|case
name|INDEX
case|:
throw|throw
operator|new
name|AuthorizationException
argument_list|(
literal|"StorageBasedAuthorizationProvider cannot handle INDEX privilege"
argument_list|)
throw|;
case|case
name|LOCK
case|:
throw|throw
operator|new
name|AuthorizationException
argument_list|(
literal|"StorageBasedAuthorizationProvider cannot handle LOCK privilege"
argument_list|)
throw|;
case|case
name|SELECT
case|:
return|return
name|FsAction
operator|.
name|READ
return|;
case|case
name|SHOW_DATABASE
case|:
return|return
name|FsAction
operator|.
name|READ
return|;
case|case
name|UNKNOWN
case|:
default|default:
throw|throw
operator|new
name|AuthorizationException
argument_list|(
literal|"Unknown privilege"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Given a Privilege[], find out what all FsActions are required    */
specifier|protected
name|EnumSet
argument_list|<
name|FsAction
argument_list|>
name|getFsActions
parameter_list|(
name|Privilege
index|[]
name|privs
parameter_list|)
block|{
name|EnumSet
argument_list|<
name|FsAction
argument_list|>
name|actions
init|=
name|EnumSet
operator|.
name|noneOf
argument_list|(
name|FsAction
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|privs
operator|==
literal|null
condition|)
block|{
return|return
name|actions
return|;
block|}
for|for
control|(
name|Privilege
name|priv
range|:
name|privs
control|)
block|{
name|actions
operator|.
name|add
argument_list|(
name|getFsAction
argument_list|(
name|priv
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|actions
return|;
block|}
comment|/**    * Authorization privileges against a path.    *    * @param path    *          a filesystem path    * @param readRequiredPriv    *          a list of privileges needed for inputs.    * @param writeRequiredPriv    *          a list of privileges needed for outputs.    */
specifier|public
name|void
name|authorize
parameter_list|(
name|Path
name|path
parameter_list|,
name|Privilege
index|[]
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
try|try
block|{
name|EnumSet
argument_list|<
name|FsAction
argument_list|>
name|actions
init|=
name|getFsActions
argument_list|(
name|readRequiredPriv
argument_list|)
decl_stmt|;
name|actions
operator|.
name|addAll
argument_list|(
name|getFsActions
argument_list|(
name|writeRequiredPriv
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|actions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|checkPermissions
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|path
argument_list|,
name|actions
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AccessControlException
name|ex
parameter_list|)
block|{
throw|throw
name|authorizationException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|LoginException
name|ex
parameter_list|)
block|{
throw|throw
name|authorizationException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
name|hiveException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
comment|/**    * Checks the permissions for the given path and current user on Hadoop FS.    * If the given path does not exists, it checks for its parent folder.    */
specifier|protected
name|void
name|checkPermissions
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
specifier|final
name|EnumSet
argument_list|<
name|FsAction
argument_list|>
name|actions
parameter_list|)
throws|throws
name|IOException
throws|,
name|LoginException
block|{
if|if
condition|(
name|path
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"path is null"
argument_list|)
throw|;
block|}
specifier|final
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|checkPermissions
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|actions
argument_list|,
name|authenticator
operator|.
name|getUserName
argument_list|()
argument_list|,
name|authenticator
operator|.
name|getGroupNames
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|path
operator|.
name|getParent
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// find the ancestor which exists to check its permissions
name|Path
name|par
init|=
name|path
operator|.
name|getParent
argument_list|()
decl_stmt|;
while|while
condition|(
name|par
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|par
argument_list|)
condition|)
block|{
break|break;
block|}
name|par
operator|=
name|par
operator|.
name|getParent
argument_list|()
expr_stmt|;
block|}
name|checkPermissions
argument_list|(
name|fs
argument_list|,
name|par
argument_list|,
name|actions
argument_list|,
name|authenticator
operator|.
name|getUserName
argument_list|()
argument_list|,
name|authenticator
operator|.
name|getGroupNames
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Checks the permissions for the given path and current user on Hadoop FS. If the given path    * does not exists, it returns.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|protected
specifier|static
name|void
name|checkPermissions
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
specifier|final
name|EnumSet
argument_list|<
name|FsAction
argument_list|>
name|actions
parameter_list|,
name|String
name|user
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|groups
parameter_list|)
throws|throws
name|IOException
throws|,
name|AccessControlException
block|{
name|String
name|superGroupName
init|=
name|getSuperGroupName
argument_list|(
name|fs
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|userBelongsToSuperGroup
argument_list|(
name|superGroupName
argument_list|,
name|groups
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"User \""
operator|+
name|user
operator|+
literal|"\" belongs to super-group \""
operator|+
name|superGroupName
operator|+
literal|"\". "
operator|+
literal|"Permission granted for actions: ("
operator|+
name|actions
operator|+
literal|")."
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|FileStatus
name|stat
decl_stmt|;
try|try
block|{
name|stat
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|fnfe
parameter_list|)
block|{
comment|// File named by path doesn't exist; nothing to validate.
return|return;
block|}
catch|catch
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|permission
operator|.
name|AccessControlException
name|ace
parameter_list|)
block|{
comment|// Older hadoop version will throw this @deprecated Exception.
throw|throw
name|accessControlException
argument_list|(
name|ace
argument_list|)
throw|;
block|}
specifier|final
name|FsPermission
name|dirPerms
init|=
name|stat
operator|.
name|getPermission
argument_list|()
decl_stmt|;
specifier|final
name|String
name|grp
init|=
name|stat
operator|.
name|getGroup
argument_list|()
decl_stmt|;
for|for
control|(
name|FsAction
name|action
range|:
name|actions
control|)
block|{
if|if
condition|(
name|user
operator|.
name|equals
argument_list|(
name|stat
operator|.
name|getOwner
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|dirPerms
operator|.
name|getUserAction
argument_list|()
operator|.
name|implies
argument_list|(
name|action
argument_list|)
condition|)
block|{
continue|continue;
block|}
block|}
if|if
condition|(
name|groups
operator|.
name|contains
argument_list|(
name|grp
argument_list|)
condition|)
block|{
if|if
condition|(
name|dirPerms
operator|.
name|getGroupAction
argument_list|()
operator|.
name|implies
argument_list|(
name|action
argument_list|)
condition|)
block|{
continue|continue;
block|}
block|}
if|if
condition|(
name|dirPerms
operator|.
name|getOtherAction
argument_list|()
operator|.
name|implies
argument_list|(
name|action
argument_list|)
condition|)
block|{
continue|continue;
block|}
throw|throw
operator|new
name|AccessControlException
argument_list|(
literal|"action "
operator|+
name|action
operator|+
literal|" not permitted on path "
operator|+
name|path
operator|+
literal|" for user "
operator|+
name|user
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|String
name|getSuperGroupName
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
return|return
name|configuration
operator|.
name|get
argument_list|(
name|DFSConfigKeys
operator|.
name|DFS_PERMISSIONS_SUPERUSERGROUP_KEY
argument_list|,
literal|""
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|userBelongsToSuperGroup
parameter_list|(
name|String
name|superGroupName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|groups
parameter_list|)
block|{
return|return
name|groups
operator|.
name|contains
argument_list|(
name|superGroupName
argument_list|)
return|;
block|}
specifier|protected
name|Path
name|getDbLocation
parameter_list|(
name|Database
name|db
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|initWh
argument_list|()
expr_stmt|;
name|String
name|location
init|=
name|db
operator|.
name|getLocationUri
argument_list|()
decl_stmt|;
if|if
condition|(
name|location
operator|==
literal|null
condition|)
block|{
return|return
name|wh
operator|.
name|getDefaultDatabasePath
argument_list|(
name|db
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|wh
operator|.
name|getDnsPath
argument_list|(
name|wh
operator|.
name|getDatabasePath
argument_list|(
name|db
argument_list|)
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|MetaException
name|ex
parameter_list|)
block|{
throw|throw
name|hiveException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
specifier|private
name|HiveException
name|hiveException
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
return|;
block|}
specifier|private
name|AuthorizationException
name|authorizationException
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
operator|new
name|AuthorizationException
argument_list|(
name|e
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|AccessControlException
name|accessControlException
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|permission
operator|.
name|AccessControlException
name|e
parameter_list|)
block|{
name|AccessControlException
name|ace
init|=
operator|new
name|AccessControlException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|ace
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
name|ace
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|authorizeAuthorizationApiInvocation
parameter_list|()
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
comment|// no-op - SBA does not attempt to authorize auth api call. Allow it
block|}
block|}
end_class

end_unit

