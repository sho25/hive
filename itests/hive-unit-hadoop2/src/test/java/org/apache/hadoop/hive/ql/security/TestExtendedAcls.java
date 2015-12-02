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
package|;
end_package

begin_import
import|import static
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
operator|.
name|ACCESS
import|;
end_import

begin_import
import|import static
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
operator|.
name|GROUP
import|;
end_import

begin_import
import|import static
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
operator|.
name|OTHER
import|;
end_import

begin_import
import|import static
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
operator|.
name|USER
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|ImmutableList
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
name|Lists
import|;
end_import

begin_class
specifier|public
class|class
name|TestExtendedAcls
extends|extends
name|FolderPermissionBase
block|{
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|TestExtendedAcls
operator|.
name|class
argument_list|)
expr_stmt|;
comment|//setup the mini DFS with acl's enabled.
name|conf
operator|.
name|set
argument_list|(
literal|"dfs.namenode.acls.enabled"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPREDMODE
argument_list|,
literal|"nonstrict"
argument_list|)
expr_stmt|;
name|baseSetup
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|AclEntry
argument_list|>
name|aclSpec1
init|=
name|ImmutableList
operator|.
name|of
argument_list|(
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|USER
argument_list|,
name|FsAction
operator|.
name|ALL
argument_list|)
argument_list|,
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|GROUP
argument_list|,
name|FsAction
operator|.
name|ALL
argument_list|)
argument_list|,
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|OTHER
argument_list|,
name|FsAction
operator|.
name|ALL
argument_list|)
argument_list|,
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|USER
argument_list|,
literal|"bar"
argument_list|,
name|FsAction
operator|.
name|READ_WRITE
argument_list|)
argument_list|,
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|USER
argument_list|,
literal|"foo"
argument_list|,
name|FsAction
operator|.
name|READ_EXECUTE
argument_list|)
argument_list|,
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|GROUP
argument_list|,
literal|"bar"
argument_list|,
name|FsAction
operator|.
name|READ_WRITE
argument_list|)
argument_list|,
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|GROUP
argument_list|,
literal|"foo"
argument_list|,
name|FsAction
operator|.
name|READ_EXECUTE
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|AclEntry
argument_list|>
name|aclSpec2
init|=
name|ImmutableList
operator|.
name|of
argument_list|(
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|USER
argument_list|,
name|FsAction
operator|.
name|ALL
argument_list|)
argument_list|,
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|GROUP
argument_list|,
name|FsAction
operator|.
name|ALL
argument_list|)
argument_list|,
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|OTHER
argument_list|,
name|FsAction
operator|.
name|READ_EXECUTE
argument_list|)
argument_list|,
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|USER
argument_list|,
literal|"bar2"
argument_list|,
name|FsAction
operator|.
name|READ_WRITE
argument_list|)
argument_list|,
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|USER
argument_list|,
literal|"foo2"
argument_list|,
name|FsAction
operator|.
name|READ_EXECUTE
argument_list|)
argument_list|,
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|GROUP
argument_list|,
literal|"bar2"
argument_list|,
name|FsAction
operator|.
name|READ
argument_list|)
argument_list|,
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|GROUP
argument_list|,
literal|"foo2"
argument_list|,
name|FsAction
operator|.
name|READ_EXECUTE
argument_list|)
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setPermission
parameter_list|(
name|String
name|locn
parameter_list|,
name|int
name|permIndex
parameter_list|)
throws|throws
name|Exception
block|{
switch|switch
condition|(
name|permIndex
condition|)
block|{
case|case
literal|0
case|:
name|setAcl
argument_list|(
name|locn
argument_list|,
name|aclSpec1
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|setAcl
argument_list|(
name|locn
argument_list|,
name|aclSpec2
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Only 2 permissions by this test"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|verifyPermission
parameter_list|(
name|String
name|locn
parameter_list|,
name|int
name|permIndex
parameter_list|)
throws|throws
name|Exception
block|{
switch|switch
condition|(
name|permIndex
condition|)
block|{
case|case
literal|0
case|:
name|FsPermission
name|perm
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|locn
argument_list|)
argument_list|)
operator|.
name|getPermission
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Location: "
operator|+
name|locn
argument_list|,
literal|"rwxrwxrwx"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|perm
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|AclEntry
argument_list|>
name|actual
init|=
name|getAcl
argument_list|(
name|locn
argument_list|)
decl_stmt|;
name|verifyAcls
argument_list|(
name|aclSpec1
argument_list|,
name|actual
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|perm
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|locn
argument_list|)
argument_list|)
operator|.
name|getPermission
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Location: "
operator|+
name|locn
argument_list|,
literal|"rwxrwxr-x"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|perm
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|AclEntry
argument_list|>
name|acls
init|=
name|getAcl
argument_list|(
name|locn
argument_list|)
decl_stmt|;
name|verifyAcls
argument_list|(
name|aclSpec2
argument_list|,
name|acls
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Only 2 permissions by this test: "
operator|+
name|permIndex
argument_list|)
throw|;
block|}
block|}
comment|/**    * Create a new AclEntry with scope, type and permission (no name).    *    * @param scope    *          AclEntryScope scope of the ACL entry    * @param type    *          AclEntryType ACL entry type    * @param permission    *          FsAction set of permissions in the ACL entry    * @return AclEntry new AclEntry    */
specifier|private
name|AclEntry
name|aclEntry
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
comment|/**    * Create a new AclEntry with scope, type, name and permission.    *    * @param scope    *          AclEntryScope scope of the ACL entry    * @param type    *          AclEntryType ACL entry type    * @param name    *          String optional ACL entry name    * @param permission    *          FsAction set of permissions in the ACL entry    * @return AclEntry new AclEntry    */
specifier|private
name|AclEntry
name|aclEntry
parameter_list|(
name|AclEntryScope
name|scope
parameter_list|,
name|AclEntryType
name|type
parameter_list|,
name|String
name|name
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
name|setName
argument_list|(
name|name
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
name|void
name|verifyAcls
parameter_list|(
name|List
argument_list|<
name|AclEntry
argument_list|>
name|expectedList
parameter_list|,
name|List
argument_list|<
name|AclEntry
argument_list|>
name|actualList
parameter_list|)
block|{
for|for
control|(
name|AclEntry
name|expected
range|:
name|expectedList
control|)
block|{
if|if
condition|(
name|expected
operator|.
name|getName
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|//the non-named acl's are coming as regular permission, and not as aclEntries.
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|AclEntry
name|actual
range|:
name|actualList
control|)
block|{
if|if
condition|(
name|actual
operator|.
name|equals
argument_list|(
name|expected
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|found
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Following Acl does not have a match: "
operator|+
name|expected
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|setAcl
parameter_list|(
name|String
name|locn
parameter_list|,
name|List
argument_list|<
name|AclEntry
argument_list|>
name|aclSpec
parameter_list|)
throws|throws
name|Exception
block|{
name|fs
operator|.
name|setAcl
argument_list|(
operator|new
name|Path
argument_list|(
name|locn
argument_list|)
argument_list|,
name|aclSpec
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|AclEntry
argument_list|>
name|getAcl
parameter_list|(
name|String
name|locn
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|fs
operator|.
name|getAclStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|locn
argument_list|)
argument_list|)
operator|.
name|getEntries
argument_list|()
return|;
block|}
block|}
end_class

end_unit

