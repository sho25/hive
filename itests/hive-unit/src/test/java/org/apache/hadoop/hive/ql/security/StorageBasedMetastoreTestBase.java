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
name|cli
operator|.
name|CliSessionState
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
name|HiveMetaStoreClient
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
name|MetaStoreUtils
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
name|ql
operator|.
name|Driver
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
name|AuthorizationPreEventListener
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
name|session
operator|.
name|SessionState
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
name|WindowsPathUtil
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
name|util
operator|.
name|Shell
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|Before
import|;
end_import

begin_comment
comment|/**  * Base class for some storage based authorization test classes  */
end_comment

begin_class
specifier|public
class|class
name|StorageBasedMetastoreTestBase
block|{
specifier|protected
name|HiveConf
name|clientHiveConf
decl_stmt|;
specifier|protected
name|HiveMetaStoreClient
name|msc
decl_stmt|;
specifier|protected
name|Driver
name|driver
decl_stmt|;
specifier|protected
name|UserGroupInformation
name|ugi
decl_stmt|;
specifier|private
specifier|static
name|int
name|objNum
init|=
literal|0
decl_stmt|;
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
specifier|protected
name|HiveConf
name|createHiveConf
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|Shell
operator|.
name|WINDOWS
condition|)
block|{
name|WindowsPathUtil
operator|.
name|convertPathsFromWindowsToHdfs
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
return|return
name|conf
return|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|port
init|=
name|MetaStoreUtils
operator|.
name|findFreePort
argument_list|()
decl_stmt|;
comment|// Turn on metastore-side authorization
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_PRE_EVENT_LISTENERS
operator|.
name|varname
argument_list|,
name|AuthorizationPreEventListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_AUTHORIZATION_MANAGER
operator|.
name|varname
argument_list|,
name|getAuthorizationProvider
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_AUTHENTICATOR_MANAGER
operator|.
name|varname
argument_list|,
name|InjectableDummyAuthenticator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|clientHiveConf
operator|=
name|createHiveConf
argument_list|()
expr_stmt|;
name|MetaStoreUtils
operator|.
name|startMetaStore
argument_list|(
name|port
argument_list|,
name|ShimLoader
operator|.
name|getHadoopThriftAuthBridge
argument_list|()
argument_list|,
name|clientHiveConf
argument_list|)
expr_stmt|;
comment|// Turn off client-side authorization
name|clientHiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_ENABLED
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|clientHiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|,
literal|"thrift://localhost:"
operator|+
name|port
argument_list|)
expr_stmt|;
name|clientHiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORETHRIFTCONNECTIONRETRIES
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|clientHiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|clientHiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|clientHiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|ugi
operator|=
name|Utils
operator|.
name|getUGI
argument_list|()
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|clientHiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|msc
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|clientHiveConf
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|clientHiveConf
argument_list|)
expr_stmt|;
name|setupFakeUser
argument_list|()
expr_stmt|;
name|InjectableDummyAuthenticator
operator|.
name|injectMode
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|setupFakeUser
parameter_list|()
block|{
name|String
name|fakeUser
init|=
literal|"mal"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fakeGroupNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|fakeGroupNames
operator|.
name|add
argument_list|(
literal|"groupygroup"
argument_list|)
expr_stmt|;
name|InjectableDummyAuthenticator
operator|.
name|injectUserName
argument_list|(
name|fakeUser
argument_list|)
expr_stmt|;
name|InjectableDummyAuthenticator
operator|.
name|injectGroupNames
argument_list|(
name|fakeGroupNames
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|String
name|setupUser
parameter_list|()
block|{
return|return
name|ugi
operator|.
name|getUserName
argument_list|()
return|;
block|}
specifier|protected
name|String
name|getTestTableName
parameter_list|()
block|{
return|return
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"tab"
operator|+
operator|++
name|objNum
return|;
block|}
specifier|protected
name|String
name|getTestDbName
parameter_list|()
block|{
return|return
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"db"
operator|+
operator|++
name|objNum
return|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|InjectableDummyAuthenticator
operator|.
name|injectMode
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|protected
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
specifier|protected
name|void
name|validateCreateDb
parameter_list|(
name|Database
name|expectedDb
parameter_list|,
name|String
name|dbName
parameter_list|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedDb
operator|.
name|getName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|dbName
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

