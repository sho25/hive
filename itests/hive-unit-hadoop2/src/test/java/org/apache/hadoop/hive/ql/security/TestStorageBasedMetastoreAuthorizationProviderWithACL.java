begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|Arrays
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
name|shims
operator|.
name|ShimLoader
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
name|hive
operator|.
name|shims
operator|.
name|HadoopShims
operator|.
name|MiniDFSShim
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
name|TestStorageBasedMetastoreAuthorizationProviderWithACL
extends|extends
name|TestStorageBasedMetastoreAuthorizationProvider
block|{
specifier|protected
specifier|static
name|MiniDFSShim
name|dfs
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|static
name|Path
name|warehouseDir
init|=
literal|null
decl_stmt|;
specifier|protected
name|UserGroupInformation
name|userUgi
init|=
literal|null
decl_stmt|;
specifier|protected
name|String
name|testUserName
init|=
literal|"test_user"
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|boolean
name|isTestEnabled
parameter_list|()
block|{
comment|// This test with HDFS ACLs will only work if FileSystem.access() is available in the
comment|// version of hadoop-2 used to build Hive.
return|return
name|doesAccessAPIExist
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|boolean
name|doesAccessAPIExist
parameter_list|()
block|{
name|boolean
name|foundMethod
init|=
literal|false
decl_stmt|;
try|try
block|{
name|Method
name|method
init|=
name|FileSystem
operator|.
name|class
operator|.
name|getMethod
argument_list|(
literal|"access"
argument_list|,
name|Path
operator|.
name|class
argument_list|,
name|FsAction
operator|.
name|class
argument_list|)
decl_stmt|;
name|foundMethod
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|err
parameter_list|)
block|{     }
return|return
name|foundMethod
return|;
block|}
annotation|@
name|Override
specifier|protected
name|HiveConf
name|createHiveConf
parameter_list|()
throws|throws
name|Exception
block|{
name|userUgi
operator|=
name|UserGroupInformation
operator|.
name|createUserForTesting
argument_list|(
name|testUserName
argument_list|,
operator|new
name|String
index|[]
block|{}
argument_list|)
expr_stmt|;
comment|// Hadoop FS ACLs do not work with LocalFileSystem, so set up MiniDFS.
name|HiveConf
name|conf
init|=
name|super
operator|.
name|createHiveConf
argument_list|()
decl_stmt|;
name|String
name|currentUserName
init|=
name|Utils
operator|.
name|getUGI
argument_list|()
operator|.
name|getShortUserName
argument_list|()
decl_stmt|;
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
name|set
argument_list|(
literal|"hadoop.proxyuser."
operator|+
name|currentUserName
operator|+
literal|".groups"
argument_list|,
literal|"*"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hadoop.proxyuser."
operator|+
name|currentUserName
operator|+
literal|".hosts"
argument_list|,
literal|"*"
argument_list|)
expr_stmt|;
name|dfs
operator|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getMiniDfs
argument_list|(
name|conf
argument_list|,
literal|4
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|dfs
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|warehouseDir
operator|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|fs
operator|.
name|getUri
argument_list|()
argument_list|)
argument_list|,
literal|"/warehouse"
argument_list|)
expr_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|warehouseDir
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
name|METASTOREWAREHOUSE
argument_list|,
name|warehouseDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_WAREHOUSE_SUBDIR_INHERIT_PERMS
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Set up scratch directory
name|Path
name|scratchDir
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|fs
operator|.
name|getUri
argument_list|()
argument_list|)
argument_list|,
literal|"/scratchdir"
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SCRATCHDIR
argument_list|,
name|scratchDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
specifier|protected
name|String
name|setupUser
parameter_list|()
block|{
comment|// Using MiniDFS, the permissions don't work properly because
comment|// the current user gets treated as a superuser.
comment|// For this test, specify a different (non-super) user.
name|InjectableDummyAuthenticator
operator|.
name|injectUserName
argument_list|(
name|userUgi
operator|.
name|getShortUserName
argument_list|()
argument_list|)
expr_stmt|;
name|InjectableDummyAuthenticator
operator|.
name|injectGroupNames
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|userUgi
operator|.
name|getGroupNames
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|InjectableDummyAuthenticator
operator|.
name|injectMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|userUgi
operator|.
name|getShortUserName
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
if|if
condition|(
name|dfs
operator|!=
literal|null
condition|)
block|{
name|dfs
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|dfs
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|allowWriteAccessViaAcl
parameter_list|(
name|String
name|userName
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Set the FS perms to read-only access, and create ACL entries allowing write access.
name|List
argument_list|<
name|AclEntry
argument_list|>
name|aclSpec
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|aclEntry
argument_list|(
name|ACCESS
argument_list|,
name|USER
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
name|FsAction
operator|.
name|READ_EXECUTE
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
name|userName
argument_list|,
name|FsAction
operator|.
name|ALL
argument_list|)
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
operator|new
name|URI
argument_list|(
name|location
argument_list|)
argument_list|,
name|clientHiveConf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|setAcl
argument_list|(
operator|new
name|Path
argument_list|(
name|location
argument_list|)
argument_list|,
name|aclSpec
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|disallowWriteAccessViaAcl
parameter_list|(
name|String
name|userName
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|Exception
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
operator|new
name|URI
argument_list|(
name|location
argument_list|)
argument_list|,
name|clientHiveConf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|removeAcl
argument_list|(
operator|new
name|Path
argument_list|(
name|location
argument_list|)
argument_list|)
expr_stmt|;
name|setPermissions
argument_list|(
name|location
argument_list|,
literal|"-r-xr-xr-x"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a new AclEntry with scope, type and permission (no name).    * Borrowed from TestExtendedAcls    *    * @param scope    *          AclEntryScope scope of the ACL entry    * @param type    *          AclEntryType ACL entry type    * @param permission    *          FsAction set of permissions in the ACL entry    * @return AclEntry new AclEntry    */
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
comment|/**    * Create a new AclEntry with scope, type, name and permission.    * Borrowed from TestExtendedAcls    *    * @param scope    *          AclEntryScope scope of the ACL entry    * @param type    *          AclEntryType ACL entry type    * @param name    *          String optional ACL entry name    * @param permission    *          FsAction set of permissions in the ACL entry    * @return AclEntry new AclEntry    */
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
specifier|protected
name|void
name|allowCreateDatabase
parameter_list|(
name|String
name|userName
parameter_list|)
throws|throws
name|Exception
block|{
name|allowWriteAccessViaAcl
argument_list|(
name|userName
argument_list|,
name|warehouseDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|allowCreateInDb
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|userName
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|Exception
block|{
name|allowWriteAccessViaAcl
argument_list|(
name|userName
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|disallowCreateInDb
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|userName
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|Exception
block|{
name|disallowWriteAccessViaAcl
argument_list|(
name|userName
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|allowCreateInTbl
parameter_list|(
name|String
name|tableName
parameter_list|,
name|String
name|userName
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|Exception
block|{
name|allowWriteAccessViaAcl
argument_list|(
name|userName
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|disallowCreateInTbl
parameter_list|(
name|String
name|tableName
parameter_list|,
name|String
name|userName
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|Exception
block|{
name|disallowWriteAccessViaAcl
argument_list|(
name|userName
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|allowDropOnTable
parameter_list|(
name|String
name|tblName
parameter_list|,
name|String
name|userName
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|Exception
block|{
name|allowWriteAccessViaAcl
argument_list|(
name|userName
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|disallowDropOnTable
parameter_list|(
name|String
name|tblName
parameter_list|,
name|String
name|userName
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|Exception
block|{
name|disallowWriteAccessViaAcl
argument_list|(
name|userName
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|allowDropOnDb
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|userName
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|Exception
block|{
name|allowWriteAccessViaAcl
argument_list|(
name|userName
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

