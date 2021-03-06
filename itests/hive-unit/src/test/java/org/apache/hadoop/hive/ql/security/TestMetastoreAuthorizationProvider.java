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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Collections
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
name|MetaStoreTestUtils
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
name|FieldSchema
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
name|metastore
operator|.
name|api
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
name|metastore
operator|.
name|api
operator|.
name|SerDeInfo
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
name|StorageDescriptor
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
name|DriverFactory
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
name|IDriver
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
name|io
operator|.
name|HiveInputFormat
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
name|io
operator|.
name|HiveOutputFormat
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
name|DefaultHiveMetastoreAuthorizationProvider
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
name|serde
operator|.
name|serdeConstants
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
name|junit
operator|.
name|Assert
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
name|assertEquals
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
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|Test
import|;
end_import

begin_comment
comment|/**  * TestHiveMetastoreAuthorizationProvider. Test case for  * HiveMetastoreAuthorizationProvider, and by default,  * for DefaultHiveMetaStoreAuthorizationProvider  * using {@link org.apache.hadoop.hive.metastore.AuthorizationPreEventListener}  * and {@link org.apache.hadoop.hive.}  *  * Note that while we do use the hive driver to test, that is mostly for test  * writing ease, and it has the same effect as using a metastore client directly  * because we disable hive client-side authorization for this test, and only  * turn on server-side auth.  *  * This test is also intended to be extended to provide tests for other  * authorization providers like StorageBasedAuthorizationProvider  */
end_comment

begin_class
specifier|public
class|class
name|TestMetastoreAuthorizationProvider
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
name|IDriver
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
name|DefaultHiveMetastoreAuthorizationProvider
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
return|return
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
return|;
block|}
specifier|protected
name|String
name|getProxyUserName
parameter_list|()
block|{
return|return
literal|null
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
name|HIVE_AUTHORIZATION_MANAGER
operator|.
name|varname
argument_list|,
name|getAuthorizationProvider
argument_list|()
argument_list|)
expr_stmt|;
name|setupMetaStoreReadAuthorization
argument_list|()
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
name|System
operator|.
name|setProperty
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
name|int
name|port
init|=
name|MetaStoreTestUtils
operator|.
name|startMetaStoreWithRetry
argument_list|()
decl_stmt|;
name|clientHiveConf
operator|=
name|createHiveConf
argument_list|()
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
name|DriverFactory
operator|.
name|newDriver
argument_list|(
name|clientHiveConf
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|setupMetaStoreReadAuthorization
parameter_list|()
block|{
comment|// read authorization does not work with default/legacy authorization mode
comment|// It is a chicken and egg problem granting select privilege to database, as the
comment|// grant statement would invoke get_database which needs select privilege
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_AUTHORIZATION_AUTH_READS
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{    }
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
literal|"smp_ms_db"
return|;
block|}
specifier|protected
name|String
name|getTestTableName
parameter_list|()
block|{
return|return
literal|"smp_ms_tbl"
return|;
block|}
specifier|protected
name|boolean
name|isTestEnabled
parameter_list|()
block|{
return|return
literal|true
return|;
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
annotation|@
name|Test
specifier|public
name|void
name|testSimplePrivileges
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|isTestEnabled
argument_list|()
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Skipping test "
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
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
name|setupUser
argument_list|()
decl_stmt|;
name|allowCreateDatabase
argument_list|(
name|userName
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create database "
operator|+
name|dbName
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
name|validateCreateDb
argument_list|(
name|db
argument_list|,
name|dbName
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
name|disallowCreateInDb
argument_list|(
name|dbName
argument_list|,
name|userName
argument_list|,
name|dbLocn
argument_list|)
expr_stmt|;
name|disallowCreateDatabase
argument_list|(
name|userName
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
try|try
block|{
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
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|e
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Even if table location is specified table creation should fail
name|String
name|tblNameLoc
init|=
name|tblName
operator|+
literal|"_loc"
decl_stmt|;
name|String
name|tblLocation
init|=
operator|new
name|Path
argument_list|(
name|dbLocn
argument_list|)
operator|.
name|getParent
argument_list|()
operator|.
name|toUri
argument_list|()
operator|+
literal|"/"
operator|+
name|tblNameLoc
decl_stmt|;
if|if
condition|(
name|mayTestLocation
argument_list|()
condition|)
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|dbName
argument_list|)
expr_stmt|;
try|try
block|{
name|driver
operator|.
name|run
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"create table %s (a string) partitioned by (b string) location '"
operator|+
name|tblLocation
operator|+
literal|"'"
argument_list|,
name|tblNameLoc
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|e
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// failure from not having permissions to create table
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|fields
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"a"
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|ttbl
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|ttbl
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|ttbl
operator|.
name|setTableName
argument_list|(
name|tblName
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|ttbl
operator|.
name|setSd
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setCols
argument_list|(
name|fields
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setParameters
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
literal|"test_param_1"
argument_list|,
literal|"Use this for comments etc"
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setSerdeInfo
argument_list|(
operator|new
name|SerDeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setName
argument_list|(
name|ttbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setParameters
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde
operator|.
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setSerializationLib
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|lazy
operator|.
name|LazySimpleSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setInputFormat
argument_list|(
name|HiveInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setOutputFormat
argument_list|(
name|HiveOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|ttbl
operator|.
name|setPartitionKeys
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|MetaException
name|me
init|=
literal|null
decl_stmt|;
try|try
block|{
name|msc
operator|.
name|createTable
argument_list|(
name|ttbl
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
name|me
operator|=
name|e
expr_stmt|;
block|}
name|assertNoPrivileges
argument_list|(
name|me
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
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tbl
operator|.
name|isSetId
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|unsetId
argument_list|()
expr_stmt|;
name|validateCreateTable
argument_list|(
name|tbl
argument_list|,
name|tblName
argument_list|,
name|dbName
argument_list|)
expr_stmt|;
comment|// Table creation should succeed even if location is specified
if|if
condition|(
name|mayTestLocation
argument_list|()
condition|)
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|dbName
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"create table %s (a string) partitioned by (b string) location '"
operator|+
name|tblLocation
operator|+
literal|"'"
argument_list|,
name|tblNameLoc
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|tblLoc
init|=
name|msc
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tblNameLoc
argument_list|)
decl_stmt|;
name|validateCreateTable
argument_list|(
name|tblLoc
argument_list|,
name|tblNameLoc
argument_list|,
name|dbName
argument_list|)
expr_stmt|;
block|}
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
try|try
block|{
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
block|}
catch|catch
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|e
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ttbl
operator|.
name|setTableName
argument_list|(
name|tblName
operator|+
literal|"mal"
argument_list|)
expr_stmt|;
name|me
operator|=
literal|null
expr_stmt|;
try|try
block|{
name|msc
operator|.
name|createTable
argument_list|(
name|ttbl
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
name|me
operator|=
name|e
expr_stmt|;
block|}
name|assertNoPrivileges
argument_list|(
name|me
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
try|try
block|{
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
block|}
catch|catch
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|e
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|ptnVals
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ptnVals
operator|.
name|add
argument_list|(
literal|"b=2011"
argument_list|)
expr_stmt|;
name|Partition
name|tpart
init|=
operator|new
name|Partition
argument_list|()
decl_stmt|;
name|tpart
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|tpart
operator|.
name|setTableName
argument_list|(
name|tblName
argument_list|)
expr_stmt|;
name|tpart
operator|.
name|setValues
argument_list|(
name|ptnVals
argument_list|)
expr_stmt|;
name|tpart
operator|.
name|setParameters
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|tpart
operator|.
name|setSd
argument_list|(
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|deepCopy
argument_list|()
argument_list|)
expr_stmt|;
name|tpart
operator|.
name|getSd
argument_list|()
operator|.
name|setSerdeInfo
argument_list|(
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|deepCopy
argument_list|()
argument_list|)
expr_stmt|;
name|tpart
operator|.
name|getSd
argument_list|()
operator|.
name|setLocation
argument_list|(
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
operator|+
literal|"/tpart"
argument_list|)
expr_stmt|;
name|me
operator|=
literal|null
expr_stmt|;
try|try
block|{
name|msc
operator|.
name|add_partition
argument_list|(
name|tpart
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
name|me
operator|=
name|e
expr_stmt|;
block|}
name|assertNoPrivileges
argument_list|(
name|me
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
name|String
name|proxyUserName
init|=
name|getProxyUserName
argument_list|()
decl_stmt|;
if|if
condition|(
name|proxyUserName
operator|!=
literal|null
condition|)
block|{
comment|// for storage based authorization, user having proxy privilege should be allowed to do operation
comment|// even if the file permission is not there.
name|InjectableDummyAuthenticator
operator|.
name|injectUserName
argument_list|(
name|proxyUserName
argument_list|)
expr_stmt|;
name|InjectableDummyAuthenticator
operator|.
name|injectGroupNames
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|proxyUserName
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
name|disallowCreateInTbl
argument_list|(
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|,
name|proxyUserName
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
name|driver
operator|.
name|run
argument_list|(
literal|"alter table "
operator|+
name|tblName
operator|+
literal|" add partition (b='2012')"
argument_list|)
expr_stmt|;
name|InjectableDummyAuthenticator
operator|.
name|injectMode
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
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
name|InjectableDummyAuthenticator
operator|.
name|injectUserName
argument_list|(
name|userName
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
name|ugi
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
name|allowCreateDatabase
argument_list|(
name|userName
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create database "
operator|+
name|dbName
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
name|tbl
operator|.
name|setTableType
argument_list|(
literal|"EXTERNAL_TABLE"
argument_list|)
expr_stmt|;
name|msc
operator|.
name|createTable
argument_list|(
name|tbl
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
name|disallowDropOnTable
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
try|try
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"drop table "
operator|+
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|e
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|boolean
name|mayTestLocation
parameter_list|()
block|{
return|return
literal|true
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
name|driver
operator|.
name|run
argument_list|(
literal|"revoke create on table "
operator|+
name|tableName
operator|+
literal|" from user "
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
name|driver
operator|.
name|run
argument_list|(
literal|"revoke create on database "
operator|+
name|dbName
operator|+
literal|" from user "
operator|+
name|userName
argument_list|)
expr_stmt|;
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
name|driver
operator|.
name|run
argument_list|(
literal|"revoke drop on table "
operator|+
name|tblName
operator|+
literal|" from user "
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
name|assertNoPrivileges
parameter_list|(
name|MetaException
name|me
parameter_list|)
block|{
name|assertNotNull
argument_list|(
name|me
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|me
operator|.
name|getMessage
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

