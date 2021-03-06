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
name|ql
operator|.
name|security
package|;
end_package

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
name|ql
operator|.
name|processors
operator|.
name|CommandProcessorException
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
name|security
operator|.
name|authorization
operator|.
name|StorageBasedAuthorizationProvider
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_comment
comment|/**  * TestStorageBasedClientSideAuthorizationProvider : Overrides  * TestClientSideAuthorizationProvider to test StorageBasedAuthorizationProvider  * on the client side.  */
end_comment

begin_class
specifier|public
class|class
name|TestStorageBasedClientSideAuthorizationProvider
extends|extends
name|TestClientSideAuthorizationProvider
block|{
annotation|@
name|Override
specifier|protected
name|String
name|getAuthorizationProvider
parameter_list|()
block|{
return|return
name|StorageBasedAuthorizationProvider
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
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
name|setPermissions
argument_list|(
name|location
argument_list|,
literal|"-rwxr--r--"
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
name|setPermissions
argument_list|(
name|location
argument_list|,
literal|"-r--r--r--"
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
name|setPermissions
argument_list|(
name|location
argument_list|,
literal|"-rwxr--r--"
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
name|setPermissions
argument_list|(
name|location
argument_list|,
literal|"-r--r--r--"
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
name|setPermissions
argument_list|(
name|location
argument_list|,
literal|"-rwxr--r--"
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
name|setPermissions
argument_list|(
name|location
argument_list|,
literal|"-rwxr--r--"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|allowSelectOnTable
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
name|setPermissions
argument_list|(
name|location
argument_list|,
literal|"-r--r--r--"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setPermissions
parameter_list|(
name|String
name|locn
parameter_list|,
name|String
name|permissions
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
name|locn
argument_list|)
argument_list|,
name|clientHiveConf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|setPermission
argument_list|(
operator|new
name|Path
argument_list|(
name|locn
argument_list|)
argument_list|,
name|FsPermission
operator|.
name|valueOf
argument_list|(
name|permissions
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|assertNoPrivileges
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
name|assertNotNull
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|0
operator|==
name|e
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|indexOf
argument_list|(
literal|"AccessControlException"
argument_list|)
operator|!=
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|getTestDbName
parameter_list|()
block|{
return|return
name|super
operator|.
name|getTestDbName
argument_list|()
operator|+
literal|"_SBAP"
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|getTestTableName
parameter_list|()
block|{
return|return
name|super
operator|.
name|getTestTableName
argument_list|()
operator|+
literal|"_SBAP"
return|;
block|}
block|}
end_class

end_unit

