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
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|metastore
operator|.
name|api
operator|.
name|Table
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
name|processors
operator|.
name|CommandProcessorResponse
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
name|DefaultHiveAuthorizationProvider
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

begin_comment
comment|/**  * TestClientSideAuthorizationProvider : Simple base test for client side  * Authorization Providers. By default, tests DefaultHiveAuthorizationProvider  */
end_comment

begin_class
specifier|public
class|class
name|TestClientSideAuthorizationProvider
extends|extends
name|TestCase
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
specifier|protected
name|String
name|getAuthorizationProvider
parameter_list|()
block|{
return|return
name|DefaultHiveAuthorizationProvider
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
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|int
name|port
init|=
name|MetaStoreUtils
operator|.
name|findFreePort
argument_list|()
decl_stmt|;
comment|// Turn off metastore-side authorization
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
literal|""
argument_list|)
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
argument_list|)
expr_stmt|;
name|clientHiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
comment|// Turn on client-side authorization
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
literal|true
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
name|HIVE_AUTHORIZATION_MANAGER
operator|.
name|varname
argument_list|,
name|getAuthorizationProvider
argument_list|()
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
name|HIVE_AUTHENTICATOR_MANAGER
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
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_TABLE_OWNER_GRANTS
operator|.
name|varname
argument_list|,
literal|""
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
argument_list|,
literal|null
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
block|}
specifier|private
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
specifier|private
name|void
name|validateCreateTable
parameter_list|(
name|Table
name|expectedTable
parameter_list|,
name|String
name|tblName
parameter_list|,
name|String
name|dbName
parameter_list|)
block|{
name|assertNotNull
argument_list|(
name|expectedTable
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedTable
operator|.
name|getTableName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|tblName
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedTable
operator|.
name|getDbName
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
specifier|protected
name|String
name|getTestDbName
parameter_list|()
block|{
return|return
literal|"smp_cl_db"
return|;
block|}
specifier|protected
name|String
name|getTestTableName
parameter_list|()
block|{
return|return
literal|"smp_cl_tbl"
return|;
block|}
specifier|public
name|void
name|testSimplePrivileges
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|dbName
init|=
name|getTestDbName
argument_list|()
decl_stmt|;
name|String
name|tblName
init|=
name|getTestTableName
argument_list|()
decl_stmt|;
name|String
name|userName
init|=
name|ugi
operator|.
name|getUserName
argument_list|()
decl_stmt|;
name|allowCreateDatabase
argument_list|(
name|userName
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|ret
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"create database "
operator|+
name|dbName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ret
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|Database
name|db
init|=
name|msc
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
name|String
name|dbLocn
init|=
name|db
operator|.
name|getLocationUri
argument_list|()
decl_stmt|;
name|disallowCreateDatabase
argument_list|(
name|userName
argument_list|)
expr_stmt|;
name|validateCreateDb
argument_list|(
name|db
argument_list|,
name|dbName
argument_list|)
expr_stmt|;
name|disallowCreateInDb
argument_list|(
name|dbName
argument_list|,
name|userName
argument_list|,
name|dbLocn
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|dbName
argument_list|)
expr_stmt|;
name|ret
operator|=
name|driver
operator|.
name|run
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"create table %s (a string) partitioned by (b string)"
argument_list|,
name|tblName
argument_list|)
argument_list|)
expr_stmt|;
comment|// failure from not having permissions to create table
name|assertNoPrivileges
argument_list|(
name|ret
argument_list|)
expr_stmt|;
name|allowCreateInDb
argument_list|(
name|dbName
argument_list|,
name|userName
argument_list|,
name|dbLocn
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|dbName
argument_list|)
expr_stmt|;
name|ret
operator|=
name|driver
operator|.
name|run
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"create table %s (a string) partitioned by (b string)"
argument_list|,
name|tblName
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ret
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
comment|// now it succeeds.
name|Table
name|tbl
init|=
name|msc
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
decl_stmt|;
name|validateCreateTable
argument_list|(
name|tbl
argument_list|,
name|tblName
argument_list|,
name|dbName
argument_list|)
expr_stmt|;
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
name|InjectableDummyAuthenticator
operator|.
name|injectMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|allowSelectOnTable
argument_list|(
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|,
name|fakeUser
argument_list|,
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|=
name|driver
operator|.
name|run
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"select * from %s limit 10"
argument_list|,
name|tblName
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ret
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|=
name|driver
operator|.
name|run
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"create table %s (a string) partitioned by (b string)"
argument_list|,
name|tblName
operator|+
literal|"mal"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNoPrivileges
argument_list|(
name|ret
argument_list|)
expr_stmt|;
name|disallowCreateInTbl
argument_list|(
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|,
name|userName
argument_list|,
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"alter table "
operator|+
name|tblName
operator|+
literal|" add partition (b='2011')"
argument_list|)
expr_stmt|;
name|assertNoPrivileges
argument_list|(
name|ret
argument_list|)
expr_stmt|;
name|InjectableDummyAuthenticator
operator|.
name|injectMode
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|allowCreateInTbl
argument_list|(
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|,
name|userName
argument_list|,
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|=
name|driver
operator|.
name|run
argument_list|(
literal|"alter table "
operator|+
name|tblName
operator|+
literal|" add partition (b='2011')"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ret
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|allowDropOnTable
argument_list|(
name|tblName
argument_list|,
name|userName
argument_list|,
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|allowDropOnDb
argument_list|(
name|dbName
argument_list|,
name|userName
argument_list|,
name|db
operator|.
name|getLocationUri
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop database if exists "
operator|+
name|getTestDbName
argument_list|()
operator|+
literal|" cascade"
argument_list|)
expr_stmt|;
block|}
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
name|driver
operator|.
name|run
argument_list|(
literal|"grant create on table "
operator|+
name|tableName
operator|+
literal|" to user "
operator|+
name|userName
argument_list|)
expr_stmt|;
block|}
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
comment|// nothing needed here by default
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
name|driver
operator|.
name|run
argument_list|(
literal|"grant create to user "
operator|+
name|userName
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|disallowCreateDatabase
parameter_list|(
name|String
name|userName
parameter_list|)
throws|throws
name|Exception
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"revoke create from user "
operator|+
name|userName
argument_list|)
expr_stmt|;
block|}
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
name|driver
operator|.
name|run
argument_list|(
literal|"grant create on database "
operator|+
name|dbName
operator|+
literal|" to user "
operator|+
name|userName
argument_list|)
expr_stmt|;
block|}
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
comment|// nothing needed here by default
block|}
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
name|driver
operator|.
name|run
argument_list|(
literal|"grant drop on table "
operator|+
name|tblName
operator|+
literal|" to user "
operator|+
name|userName
argument_list|)
expr_stmt|;
block|}
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
name|driver
operator|.
name|run
argument_list|(
literal|"grant drop on database "
operator|+
name|dbName
operator|+
literal|" to user "
operator|+
name|userName
argument_list|)
expr_stmt|;
block|}
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
name|driver
operator|.
name|run
argument_list|(
literal|"grant select on table "
operator|+
name|tblName
operator|+
literal|" to user "
operator|+
name|userName
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|assertNoPrivileges
parameter_list|(
name|CommandProcessorResponse
name|ret
parameter_list|)
block|{
name|assertNotNull
argument_list|(
name|ret
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|0
operator|==
name|ret
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ret
operator|.
name|getErrorMessage
argument_list|()
operator|.
name|indexOf
argument_list|(
literal|"No privilege"
argument_list|)
operator|!=
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

