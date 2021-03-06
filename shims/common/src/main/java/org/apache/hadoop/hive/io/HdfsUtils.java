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
name|io
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
literal|"shims.HdfsUtils"
argument_list|)
decl_stmt|;
comment|/**    * Copy the permissions, group, and ACLs from a source {@link HadoopFileStatus} to a target {@link Path}. This method    * will only log a warning if permissions cannot be set, no exception will be thrown.    *    * @param conf the {@link Configuration} used when setting permissions and ACLs    * @param sourceStatus the source {@link HadoopFileStatus} to copy permissions and ACLs from    * @param fs the {@link FileSystem} that contains the target {@link Path}    * @param target the {@link Path} to copy permissions, group, and ACLs to    * @param recursion recursively set permissions and ACLs on the target {@link Path}    */
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
literal|null
argument_list|,
name|fs
argument_list|,
name|target
argument_list|,
name|recursion
argument_list|)
expr_stmt|;
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
specifier|public
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
block|}
end_class

end_unit

