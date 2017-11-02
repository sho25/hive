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
argument_list|(
name|srcPaths
argument_list|,
name|dst
argument_list|)
decl_stmt|;
name|options
operator|.
name|setSyncFolder
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|options
operator|.
name|setSkipCRC
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|options
operator|.
name|preserve
argument_list|(
name|DistCpOptions
operator|.
name|FileAttribute
operator|.
name|BLOCKSIZE
argument_list|)
expr_stmt|;
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
literal|"-skipcrccheck"
argument_list|)
expr_stmt|;
name|params
operator|.
name|add
argument_list|(
literal|"-pb"
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
block|}
end_class

end_unit

