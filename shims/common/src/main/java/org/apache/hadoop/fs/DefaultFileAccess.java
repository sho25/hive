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
name|fs
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
name|EnumSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterators
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
name|hive
operator|.
name|shims
operator|.
name|Utils
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

begin_comment
comment|/**  * Implements the default file access logic for HadoopShims.checkFileAccess(), for Hadoop  * versions which do not implement FileSystem.access().  *  */
end_comment

begin_class
specifier|public
class|class
name|DefaultFileAccess
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|DefaultFileAccess
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|emptyGroups
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
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
name|AccessControlException
throws|,
name|LoginException
block|{
comment|// Get the user/groups for checking permissions based on the current UGI.
name|UserGroupInformation
name|currentUgi
init|=
name|Utils
operator|.
name|getUGI
argument_list|()
decl_stmt|;
name|DefaultFileAccess
operator|.
name|checkFileAccess
argument_list|(
name|fs
argument_list|,
name|stat
argument_list|,
name|action
argument_list|,
name|currentUgi
operator|.
name|getShortUserName
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|currentUgi
operator|.
name|getGroupNames
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|checkFileAccess
argument_list|(
name|fs
argument_list|,
name|Iterators
operator|.
name|singletonIterator
argument_list|(
name|stat
argument_list|)
argument_list|,
name|EnumSet
operator|.
name|of
argument_list|(
name|action
argument_list|)
argument_list|,
name|user
argument_list|,
name|groups
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|checkFileAccess
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Iterator
argument_list|<
name|FileStatus
argument_list|>
name|statuses
parameter_list|,
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
if|if
condition|(
name|groups
operator|==
literal|null
condition|)
block|{
name|groups
operator|=
name|emptyGroups
expr_stmt|;
block|}
comment|// Short-circuit for super-users.
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
literal|"Permission granted for actions: "
operator|+
name|actions
operator|+
literal|"."
argument_list|)
expr_stmt|;
return|return;
block|}
while|while
condition|(
name|statuses
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|FileStatus
name|stat
init|=
name|statuses
operator|.
name|next
argument_list|()
decl_stmt|;
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
name|FsAction
name|combinedAction
init|=
name|combine
argument_list|(
name|actions
argument_list|)
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
name|combinedAction
argument_list|)
condition|)
block|{
continue|continue;
block|}
block|}
elseif|else
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
name|combinedAction
argument_list|)
condition|)
block|{
continue|continue;
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
name|combinedAction
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
name|combinedAction
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
comment|// for_each(fileStatus);
block|}
specifier|private
specifier|static
name|FsAction
name|combine
parameter_list|(
name|EnumSet
argument_list|<
name|FsAction
argument_list|>
name|actions
parameter_list|)
block|{
name|FsAction
name|resultantAction
init|=
name|FsAction
operator|.
name|NONE
decl_stmt|;
for|for
control|(
name|FsAction
name|action
range|:
name|actions
control|)
block|{
name|resultantAction
operator|=
name|resultantAction
operator|.
name|or
argument_list|(
name|action
argument_list|)
expr_stmt|;
block|}
return|return
name|resultantAction
return|;
block|}
specifier|public
specifier|static
name|void
name|checkFileAccess
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Iterator
argument_list|<
name|FileStatus
argument_list|>
name|statuses
parameter_list|,
name|EnumSet
argument_list|<
name|FsAction
argument_list|>
name|actions
parameter_list|)
throws|throws
name|IOException
throws|,
name|AccessControlException
throws|,
name|LoginException
block|{
name|UserGroupInformation
name|ugi
init|=
name|Utils
operator|.
name|getUGI
argument_list|()
decl_stmt|;
name|checkFileAccess
argument_list|(
name|fs
argument_list|,
name|statuses
argument_list|,
name|actions
argument_list|,
name|ugi
operator|.
name|getShortUserName
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|ugi
operator|.
name|getGroupNames
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
literal|"dfs.permissions.supergroup"
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
block|}
end_class

end_unit

