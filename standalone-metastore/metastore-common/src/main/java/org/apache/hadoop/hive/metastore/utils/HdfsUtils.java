begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|utils
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|base
operator|.
name|Joiner
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
name|base
operator|.
name|Objects
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
name|base
operator|.
name|Predicate
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
name|Iterables
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
name|ArrayUtils
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
name|FsShell
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
name|AclEntry
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
name|AclEntryScope
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
name|AclEntryType
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
name|AclStatus
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
name|DistributedFileSystem
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
name|client
operator|.
name|HdfsAdmin
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
name|AccessControlException
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
name|tools
operator|.
name|DistCp
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
name|tools
operator|.
name|DistCpOptions
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
name|tools
operator|.
name|DistCpOptions
operator|.
name|FileAttribute
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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
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
name|Collections
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

begin_class
specifier|public
class|class
name|HdfsUtils
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
name|HdfsUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DISTCP_OPTIONS_PREFIX
init|=
literal|"distcp.options."
decl_stmt|;
comment|// TODO: this relies on HDFS not changing the format; we assume if we could get inode ID, this
comment|//       is still going to work. Otherwise, file IDs can be turned off. Later, we should use
comment|//       as public utility method in HDFS to obtain the inode-based path.
specifier|private
specifier|static
specifier|final
name|String
name|HDFS_ID_PATH_PREFIX
init|=
literal|"/.reserved/.inodes/"
decl_stmt|;
comment|/**    * Check the permissions on a file.    * @param fs Filesystem the file is contained in    * @param stat Stat info for the file    * @param action action to be performed    * @throws IOException If thrown by Hadoop    * @throws AccessControlException if the file cannot be accessed    */
specifier|public
specifier|static
name|void
name|checkFileAccess
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|FileStatus
name|stat
parameter_list|,
name|FsAction
name|action
parameter_list|)
throws|throws
name|IOException
throws|,
name|LoginException
block|{
name|checkFileAccess
argument_list|(
name|fs
argument_list|,
name|stat
argument_list|,
name|action
argument_list|,
name|SecurityUtils
operator|.
name|getUGI
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check the permissions on a file    * @param fs Filesystem the file is contained in    * @param stat Stat info for the file    * @param action action to be performed    * @param ugi user group info for the current user.  This is passed in so that tests can pass    *            in mock ones.    * @throws IOException If thrown by Hadoop    * @throws AccessControlException if the file cannot be accessed    */
annotation|@
name|VisibleForTesting
specifier|static
name|void
name|checkFileAccess
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|FileStatus
name|stat
parameter_list|,
name|FsAction
name|action
parameter_list|,
name|UserGroupInformation
name|ugi
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|user
init|=
name|ugi
operator|.
name|getShortUserName
argument_list|()
decl_stmt|;
name|String
index|[]
name|groups
init|=
name|ugi
operator|.
name|getGroupNames
argument_list|()
decl_stmt|;
if|if
condition|(
name|groups
operator|!=
literal|null
condition|)
block|{
name|String
name|superGroupName
init|=
name|fs
operator|.
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
literal|"dfs.permissions.supergroup"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
if|if
condition|(
name|arrayContains
argument_list|(
name|groups
argument_list|,
name|superGroupName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
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
literal|"Permission granted for action: "
operator|+
name|action
operator|+
literal|"."
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|FsPermission
name|dirPerms
init|=
name|stat
operator|.
name|getPermission
argument_list|()
decl_stmt|;
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
return|return;
block|}
block|}
elseif|else
if|if
condition|(
name|arrayContains
argument_list|(
name|groups
argument_list|,
name|stat
operator|.
name|getGroup
argument_list|()
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
return|return;
block|}
block|}
elseif|else
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
return|return;
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
name|stat
operator|.
name|getPath
argument_list|()
operator|+
literal|" for user "
operator|+
name|user
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|boolean
name|isPathEncrypted
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|URI
name|fsUri
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|fullPath
decl_stmt|;
if|if
condition|(
name|path
operator|.
name|isAbsolute
argument_list|()
condition|)
block|{
name|fullPath
operator|=
name|path
expr_stmt|;
block|}
else|else
block|{
name|fullPath
operator|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|makeQualified
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
literal|"hdfs"
operator|.
name|equalsIgnoreCase
argument_list|(
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
try|try
block|{
name|HdfsAdmin
name|hdfsAdmin
init|=
operator|new
name|HdfsAdmin
argument_list|(
name|fsUri
argument_list|,
name|conf
argument_list|)
decl_stmt|;
return|return
operator|(
name|hdfsAdmin
operator|.
name|getEncryptionZoneForPath
argument_list|(
name|fullPath
argument_list|)
operator|!=
literal|null
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|fnfe
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Failed to get EZ for non-existent path: "
operator|+
name|fullPath
argument_list|,
name|fnfe
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
specifier|private
specifier|static
name|boolean
name|arrayContains
parameter_list|(
name|String
index|[]
name|array
parameter_list|,
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|array
operator|==
literal|null
condition|)
return|return
literal|false
return|;
for|for
control|(
name|String
name|element
range|:
name|array
control|)
block|{
if|if
condition|(
name|element
operator|.
name|equals
argument_list|(
name|value
argument_list|)
condition|)
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|public
specifier|static
name|boolean
name|runDistCpAs
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|srcPaths
parameter_list|,
name|Path
name|dst
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|doAsUser
parameter_list|)
throws|throws
name|IOException
block|{
name|UserGroupInformation
name|proxyUser
init|=
name|UserGroupInformation
operator|.
name|createProxyUser
argument_list|(
name|doAsUser
argument_list|,
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|proxyUser
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Boolean
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Boolean
name|run
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|runDistCp
argument_list|(
name|srcPaths
argument_list|,
name|dst
argument_list|,
name|conf
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|boolean
name|runDistCp
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|srcPaths
parameter_list|,
name|Path
name|dst
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|DistCpOptions
name|options
init|=
operator|new
name|DistCpOptions
operator|.
name|Builder
argument_list|(
name|srcPaths
argument_list|,
name|dst
argument_list|)
operator|.
name|withSyncFolder
argument_list|(
literal|true
argument_list|)
operator|.
name|withCRC
argument_list|(
literal|true
argument_list|)
operator|.
name|preserve
argument_list|(
name|FileAttribute
operator|.
name|BLOCKSIZE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// Creates the command-line parameters for distcp
name|List
argument_list|<
name|String
argument_list|>
name|params
init|=
name|constructDistCpParams
argument_list|(
name|srcPaths
argument_list|,
name|dst
argument_list|,
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"mapred.mapper.new-api"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|DistCp
name|distcp
init|=
operator|new
name|DistCp
argument_list|(
name|conf
argument_list|,
name|options
argument_list|)
decl_stmt|;
comment|// HIVE-13704 states that we should use run() instead of execute() due to a hadoop known issue
comment|// added by HADOOP-10459
if|if
condition|(
name|distcp
operator|.
name|run
argument_list|(
name|params
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|params
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot execute DistCp process: "
operator|+
name|e
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"mapred.mapper.new-api"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|constructDistCpParams
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|srcPaths
parameter_list|,
name|Path
name|dst
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|params
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
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
name|entry
range|:
name|conf
operator|.
name|getPropsWithPrefix
argument_list|(
name|DISTCP_OPTIONS_PREFIX
argument_list|)
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|distCpOption
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|distCpVal
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|params
operator|.
name|add
argument_list|(
literal|"-"
operator|+
name|distCpOption
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|distCpVal
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|distCpVal
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|params
operator|.
name|add
argument_list|(
name|distCpVal
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|params
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// if no entries were added via conf, we initiate our defaults
name|params
operator|.
name|add
argument_list|(
literal|"-update"
argument_list|)
expr_stmt|;
name|params
operator|.
name|add
argument_list|(
literal|"-pbx"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Path
name|src
range|:
name|srcPaths
control|)
block|{
name|params
operator|.
name|add
argument_list|(
name|src
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|params
operator|.
name|add
argument_list|(
name|dst
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|params
return|;
block|}
specifier|public
specifier|static
name|Path
name|getFileIdPath
parameter_list|(
name|FileSystem
name|fileSystem
parameter_list|,
name|Path
name|path
parameter_list|,
name|long
name|fileId
parameter_list|)
block|{
return|return
operator|(
name|fileSystem
operator|instanceof
name|DistributedFileSystem
operator|)
condition|?
operator|new
name|Path
argument_list|(
name|HDFS_ID_PATH_PREFIX
operator|+
name|fileId
argument_list|)
else|:
name|path
return|;
block|}
specifier|public
specifier|static
name|long
name|getFileId
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|String
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ensureDfs
argument_list|(
name|fs
argument_list|)
operator|.
name|getClient
argument_list|()
operator|.
name|getFileInfo
argument_list|(
name|path
argument_list|)
operator|.
name|getFileId
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|DistributedFileSystem
name|ensureDfs
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|fs
operator|instanceof
name|DistributedFileSystem
operator|)
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Only supported for DFS; got "
operator|+
name|fs
operator|.
name|getClass
argument_list|()
argument_list|)
throw|;
block|}
return|return
operator|(
name|DistributedFileSystem
operator|)
name|fs
return|;
block|}
specifier|public
specifier|static
class|class
name|HadoopFileStatus
block|{
specifier|private
specifier|final
name|FileStatus
name|fileStatus
decl_stmt|;
specifier|private
specifier|final
name|AclStatus
name|aclStatus
decl_stmt|;
specifier|public
name|HadoopFileStatus
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|file
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
name|fileStatus
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|file
argument_list|)
decl_stmt|;
name|AclStatus
name|aclStatus
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|Objects
operator|.
name|equal
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"dfs.namenode.acls.enabled"
argument_list|)
argument_list|,
literal|"true"
argument_list|)
condition|)
block|{
comment|//Attempt extended Acl operations only if its enabled, but don't fail the operation regardless.
try|try
block|{
name|aclStatus
operator|=
name|fs
operator|.
name|getAclStatus
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipping ACL inheritance: File system for path "
operator|+
name|file
operator|+
literal|" "
operator|+
literal|"does not support ACLs but dfs.namenode.acls.enabled is set to true. "
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"The details are: "
operator|+
name|e
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|fileStatus
operator|=
name|fileStatus
expr_stmt|;
name|this
operator|.
name|aclStatus
operator|=
name|aclStatus
expr_stmt|;
block|}
specifier|public
name|FileStatus
name|getFileStatus
parameter_list|()
block|{
return|return
name|fileStatus
return|;
block|}
name|List
argument_list|<
name|AclEntry
argument_list|>
name|getAclEntries
parameter_list|()
block|{
return|return
name|aclStatus
operator|==
literal|null
condition|?
literal|null
else|:
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|aclStatus
operator|.
name|getEntries
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
name|AclStatus
name|getAclStatus
parameter_list|()
block|{
return|return
name|this
operator|.
name|aclStatus
return|;
block|}
block|}
comment|/**    * Copy the permissions, group, and ACLs from a source {@link HadoopFileStatus} to a target {@link Path}. This method    * will only log a warning if permissions cannot be set, no exception will be thrown.    *    * @param conf the {@link Configuration} used when setting permissions and ACLs    * @param sourceStatus the source {@link HadoopFileStatus} to copy permissions and ACLs from    * @param targetGroup the group of the target {@link Path}, if this is set and it is equal to the source group, an    *                    extra set group operation is avoided    * @param fs the {@link FileSystem} that contains the target {@link Path}    * @param target the {@link Path} to copy permissions, group, and ACLs to    * @param recursion recursively set permissions and ACLs on the target {@link Path}    */
specifier|public
specifier|static
name|void
name|setFullFileStatus
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HdfsUtils
operator|.
name|HadoopFileStatus
name|sourceStatus
parameter_list|,
name|String
name|targetGroup
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|target
parameter_list|,
name|boolean
name|recursion
parameter_list|)
block|{
name|setFullFileStatus
argument_list|(
name|conf
argument_list|,
name|sourceStatus
argument_list|,
name|targetGroup
argument_list|,
name|fs
argument_list|,
name|target
argument_list|,
name|recursion
argument_list|,
name|recursion
condition|?
operator|new
name|FsShell
argument_list|()
else|:
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|static
name|void
name|setFullFileStatus
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HdfsUtils
operator|.
name|HadoopFileStatus
name|sourceStatus
parameter_list|,
name|String
name|targetGroup
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|target
parameter_list|,
name|boolean
name|recursion
parameter_list|,
name|FsShell
name|fsShell
parameter_list|)
block|{
try|try
block|{
name|FileStatus
name|fStatus
init|=
name|sourceStatus
operator|.
name|getFileStatus
argument_list|()
decl_stmt|;
name|String
name|group
init|=
name|fStatus
operator|.
name|getGroup
argument_list|()
decl_stmt|;
name|boolean
name|aclEnabled
init|=
name|Objects
operator|.
name|equal
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"dfs.namenode.acls.enabled"
argument_list|)
argument_list|,
literal|"true"
argument_list|)
decl_stmt|;
name|FsPermission
name|sourcePerm
init|=
name|fStatus
operator|.
name|getPermission
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|AclEntry
argument_list|>
name|aclEntries
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|aclEnabled
condition|)
block|{
if|if
condition|(
name|sourceStatus
operator|.
name|getAclEntries
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
name|sourceStatus
operator|.
name|getAclStatus
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|aclEntries
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|sourceStatus
operator|.
name|getAclEntries
argument_list|()
argument_list|)
expr_stmt|;
name|removeBaseAclEntries
argument_list|(
name|aclEntries
argument_list|)
expr_stmt|;
comment|//the ACL api's also expect the tradition user/group/other permission in the form of ACL
name|aclEntries
operator|.
name|add
argument_list|(
name|newAclEntry
argument_list|(
name|AclEntryScope
operator|.
name|ACCESS
argument_list|,
name|AclEntryType
operator|.
name|USER
argument_list|,
name|sourcePerm
operator|.
name|getUserAction
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|aclEntries
operator|.
name|add
argument_list|(
name|newAclEntry
argument_list|(
name|AclEntryScope
operator|.
name|ACCESS
argument_list|,
name|AclEntryType
operator|.
name|GROUP
argument_list|,
name|sourcePerm
operator|.
name|getGroupAction
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|aclEntries
operator|.
name|add
argument_list|(
name|newAclEntry
argument_list|(
name|AclEntryScope
operator|.
name|ACCESS
argument_list|,
name|AclEntryType
operator|.
name|OTHER
argument_list|,
name|sourcePerm
operator|.
name|getOtherAction
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|recursion
condition|)
block|{
comment|//use FsShell to change group, permissions, and extended ACL's recursively
name|fsShell
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|//If there is no group of a file, no need to call chgrp
if|if
condition|(
name|group
operator|!=
literal|null
operator|&&
operator|!
name|group
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|run
argument_list|(
name|fsShell
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-chgrp"
block|,
literal|"-R"
block|,
name|group
block|,
name|target
operator|.
name|toString
argument_list|()
block|}
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|aclEnabled
condition|)
block|{
if|if
condition|(
literal|null
operator|!=
name|aclEntries
condition|)
block|{
comment|//Attempt extended Acl operations only if its enabled, 8791but don't fail the operation regardless.
try|try
block|{
comment|//construct the -setfacl command
name|String
name|aclEntry
init|=
name|Joiner
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|join
argument_list|(
name|aclEntries
argument_list|)
decl_stmt|;
name|run
argument_list|(
name|fsShell
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-setfacl"
block|,
literal|"-R"
block|,
literal|"--set"
block|,
name|aclEntry
block|,
name|target
operator|.
name|toString
argument_list|()
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skipping ACL inheritance: File system for path "
operator|+
name|target
operator|+
literal|" "
operator|+
literal|"does not support ACLs but dfs.namenode.acls.enabled is set to true. "
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"The details are: "
operator|+
name|e
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|String
name|permission
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|sourcePerm
operator|.
name|toShort
argument_list|()
argument_list|,
literal|8
argument_list|)
decl_stmt|;
name|run
argument_list|(
name|fsShell
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-chmod"
block|,
literal|"-R"
block|,
name|permission
block|,
name|target
operator|.
name|toString
argument_list|()
block|}
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|group
operator|!=
literal|null
operator|&&
operator|!
name|group
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|targetGroup
operator|==
literal|null
operator|||
operator|!
name|group
operator|.
name|equals
argument_list|(
name|targetGroup
argument_list|)
condition|)
block|{
name|fs
operator|.
name|setOwner
argument_list|(
name|target
argument_list|,
literal|null
argument_list|,
name|group
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|aclEnabled
condition|)
block|{
if|if
condition|(
literal|null
operator|!=
name|aclEntries
condition|)
block|{
name|fs
operator|.
name|setAcl
argument_list|(
name|target
argument_list|,
name|aclEntries
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|fs
operator|.
name|setPermission
argument_list|(
name|target
argument_list|,
name|sourcePerm
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to inherit permissions for file "
operator|+
name|target
operator|+
literal|" from file "
operator|+
name|sourceStatus
operator|.
name|getFileStatus
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Exception while inheriting permissions"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Removes basic permission acls (unamed acls) from the list of acl entries    * @param entries acl entries to remove from.    */
specifier|private
specifier|static
name|void
name|removeBaseAclEntries
parameter_list|(
name|List
argument_list|<
name|AclEntry
argument_list|>
name|entries
parameter_list|)
block|{
name|Iterables
operator|.
name|removeIf
argument_list|(
name|entries
argument_list|,
operator|new
name|Predicate
argument_list|<
name|AclEntry
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|AclEntry
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
operator|.
name|getName
argument_list|()
operator|==
literal|null
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
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a new AclEntry with scope, type and permission (no name).    *    * @param scope    *          AclEntryScope scope of the ACL entry    * @param type    *          AclEntryType ACL entry type    * @param permission    *          FsAction set of permissions in the ACL entry    * @return AclEntry new AclEntry    */
specifier|private
specifier|static
name|AclEntry
name|newAclEntry
parameter_list|(
name|AclEntryScope
name|scope
parameter_list|,
name|AclEntryType
name|type
parameter_list|,
name|FsAction
name|permission
parameter_list|)
block|{
return|return
operator|new
name|AclEntry
operator|.
name|Builder
argument_list|()
operator|.
name|setScope
argument_list|(
name|scope
argument_list|)
operator|.
name|setType
argument_list|(
name|type
argument_list|)
operator|.
name|setPermission
argument_list|(
name|permission
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|void
name|run
parameter_list|(
name|FsShell
name|shell
parameter_list|,
name|String
index|[]
name|command
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|ArrayUtils
operator|.
name|toString
argument_list|(
name|command
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|retval
init|=
name|shell
operator|.
name|run
argument_list|(
name|command
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Return value is :"
operator|+
name|retval
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

