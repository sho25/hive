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
operator|.
name|authorization
operator|.
name|plugin
package|;
end_package

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
name|mockito
operator|.
name|Matchers
operator|.
name|any
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|UtilsForTest
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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|CommandNeedRetryException
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
name|HiveAuthenticationProvider
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
name|SessionStateUserAuthenticator
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
name|plugin
operator|.
name|HivePrivilegeObject
operator|.
name|HivePrivilegeObjectType
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
name|junit
operator|.
name|AfterClass
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
name|BeforeClass
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

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_comment
comment|/**  * Test HiveAuthorizer api invocation for filtering objects  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveAuthorizerShowFilters
block|{
specifier|protected
specifier|static
name|HiveConf
name|conf
decl_stmt|;
specifier|protected
specifier|static
name|Driver
name|driver
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|tableName1
init|=
operator|(
name|TestHiveAuthorizerShowFilters
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"table1"
operator|)
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|tableName2
init|=
operator|(
name|TestHiveAuthorizerShowFilters
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"table2"
operator|)
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|dbName1
init|=
operator|(
name|TestHiveAuthorizerShowFilters
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"db1"
operator|)
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|dbName2
init|=
operator|(
name|TestHiveAuthorizerShowFilters
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"db2"
operator|)
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|HiveAuthorizer
name|mockedAuthorizer
decl_stmt|;
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|AllTables
init|=
name|getSortedList
argument_list|(
name|tableName1
argument_list|,
name|tableName2
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|AllDbs
init|=
name|getSortedList
argument_list|(
literal|"default"
argument_list|,
name|dbName1
argument_list|,
name|dbName2
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|filterArguments
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|filteredResults
init|=
operator|new
name|ArrayList
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * This factory creates a mocked HiveAuthorizer class. The mocked class is    * used to capture the argument passed to HiveAuthorizer.filterListCmdObjects.    * It returns fileredResults object for call to    * HiveAuthorizer.filterListCmdObjects, and stores the list argument in    * filterArguments    */
specifier|protected
specifier|static
class|class
name|MockedHiveAuthorizerFactory
implements|implements
name|HiveAuthorizerFactory
block|{
specifier|protected
specifier|abstract
class|class
name|AuthorizerWithFilterCmdImpl
implements|implements
name|HiveAuthorizer
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|filterListCmdObjects
parameter_list|(
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|listObjs
parameter_list|,
name|HiveAuthzContext
name|context
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
comment|// capture arguments in static
name|filterArguments
operator|=
name|listObjs
expr_stmt|;
comment|// return static variable with results, if it is set to some set of
comment|// values
comment|// otherwise return the arguments
if|if
condition|(
name|filteredResults
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|filterArguments
return|;
block|}
return|return
name|filteredResults
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|HiveAuthorizer
name|createHiveAuthorizer
parameter_list|(
name|HiveMetastoreClientFactory
name|metastoreClientFactory
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|HiveAuthenticationProvider
name|authenticator
parameter_list|,
name|HiveAuthzSessionContext
name|ctx
parameter_list|)
block|{
name|Mockito
operator|.
name|validateMockitoUsage
argument_list|()
expr_stmt|;
name|mockedAuthorizer
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|AuthorizerWithFilterCmdImpl
operator|.
name|class
argument_list|,
name|Mockito
operator|.
name|withSettings
argument_list|()
operator|.
name|verboseLogging
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|Mockito
operator|.
name|when
argument_list|(
name|mockedAuthorizer
operator|.
name|filterListCmdObjects
argument_list|(
operator|(
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
operator|)
name|any
argument_list|()
argument_list|,
operator|(
name|HiveAuthzContext
operator|)
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenCallRealMethod
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
argument_list|(
literal|"Caught exception "
operator|+
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|mockedAuthorizer
return|;
block|}
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeTest
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
comment|// Turn on mocked authorization
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
name|MockedHiveAuthorizerFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_AUTHENTICATOR_MANAGER
argument_list|,
name|SessionStateUserAuthenticator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|UtilsForTest
operator|.
name|setNewDerbyDbLocation
argument_list|(
name|conf
argument_list|,
name|TestHiveAuthorizerShowFilters
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|runCmd
argument_list|(
literal|"create table "
operator|+
name|tableName1
operator|+
literal|" (i int, j int, k string) partitioned by (city string, `date` string) "
argument_list|)
expr_stmt|;
name|runCmd
argument_list|(
literal|"create table "
operator|+
name|tableName2
operator|+
literal|"(i int)"
argument_list|)
expr_stmt|;
name|runCmd
argument_list|(
literal|"create database "
operator|+
name|dbName1
argument_list|)
expr_stmt|;
name|runCmd
argument_list|(
literal|"create database "
operator|+
name|dbName2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|filterArguments
operator|=
literal|null
expr_stmt|;
name|filteredResults
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterTests
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Drop the tables when we're done. This makes the test work inside an IDE
name|runCmd
argument_list|(
literal|"drop table if exists "
operator|+
name|tableName1
argument_list|)
expr_stmt|;
name|runCmd
argument_list|(
literal|"drop table if exists "
operator|+
name|tableName2
argument_list|)
expr_stmt|;
name|runCmd
argument_list|(
literal|"drop database if exists "
operator|+
name|dbName1
argument_list|)
expr_stmt|;
name|runCmd
argument_list|(
literal|"drop database if exists "
operator|+
name|dbName2
argument_list|)
expr_stmt|;
name|driver
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testShowDatabasesAll
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
throws|,
name|IOException
block|{
name|runShowDbTest
argument_list|(
name|AllDbs
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testShowDatabasesSelected
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
throws|,
name|IOException
block|{
name|setFilteredResults
argument_list|(
name|HivePrivilegeObjectType
operator|.
name|DATABASE
argument_list|,
name|dbName2
argument_list|)
expr_stmt|;
name|runShowDbTest
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|dbName2
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runShowDbTest
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|expectedDbList
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
throws|,
name|IOException
block|{
name|runCmd
argument_list|(
literal|"show databases"
argument_list|)
expr_stmt|;
name|verifyAllDb
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"filtered result check "
argument_list|,
name|expectedDbList
argument_list|,
name|getSortedResults
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testShowTablesAll
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
throws|,
name|IOException
block|{
name|runShowTablesTest
argument_list|(
name|AllTables
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testShowTablesSelected
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
throws|,
name|IOException
block|{
name|setFilteredResults
argument_list|(
name|HivePrivilegeObjectType
operator|.
name|TABLE_OR_VIEW
argument_list|,
name|tableName2
argument_list|)
expr_stmt|;
name|runShowTablesTest
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|tableName2
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runShowTablesTest
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|expectedTabs
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
throws|,
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
name|runCmd
argument_list|(
literal|"show tables"
argument_list|)
expr_stmt|;
name|verifyAllTables
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"filtered result check "
argument_list|,
name|expectedTabs
argument_list|,
name|getSortedResults
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getSortedResults
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// set results to be returned
name|driver
operator|.
name|getResults
argument_list|(
name|res
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|res
argument_list|)
expr_stmt|;
return|return
name|res
return|;
block|}
comment|/**    * Verify that arguments to call to HiveAuthorizer.filterListCmdObjects are of    * type DATABASE and contain all databases.    *    * @throws HiveAccessControlException    * @throws HiveAuthzPluginException    */
specifier|private
name|void
name|verifyAllDb
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|privObjs
init|=
name|filterArguments
decl_stmt|;
comment|// get the db names out
name|List
argument_list|<
name|String
argument_list|>
name|dbArgs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HivePrivilegeObject
name|privObj
range|:
name|privObjs
control|)
block|{
name|assertEquals
argument_list|(
literal|"Priv object type should be db"
argument_list|,
name|HivePrivilegeObjectType
operator|.
name|DATABASE
argument_list|,
name|privObj
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|dbArgs
operator|.
name|add
argument_list|(
name|privObj
operator|.
name|getDbname
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// sort before comparing with expected results
name|Collections
operator|.
name|sort
argument_list|(
name|dbArgs
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"All db should be passed as arguments"
argument_list|,
name|AllDbs
argument_list|,
name|dbArgs
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that arguments to call to HiveAuthorizer.filterListCmdObjects are of    * type TABLE and contain all tables.    *    * @throws HiveAccessControlException    * @throws HiveAuthzPluginException    */
specifier|private
name|void
name|verifyAllTables
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|privObjs
init|=
name|filterArguments
decl_stmt|;
comment|// get the table names out
name|List
argument_list|<
name|String
argument_list|>
name|tables
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HivePrivilegeObject
name|privObj
range|:
name|privObjs
control|)
block|{
name|assertEquals
argument_list|(
literal|"Priv object type should be db"
argument_list|,
name|HivePrivilegeObjectType
operator|.
name|TABLE_OR_VIEW
argument_list|,
name|privObj
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Database name"
argument_list|,
literal|"default"
argument_list|,
name|privObj
operator|.
name|getDbname
argument_list|()
argument_list|)
expr_stmt|;
name|tables
operator|.
name|add
argument_list|(
name|privObj
operator|.
name|getObjectName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// sort before comparing with expected results
name|Collections
operator|.
name|sort
argument_list|(
name|tables
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"All tables should be passed as arguments"
argument_list|,
name|AllTables
argument_list|,
name|tables
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|setFilteredResults
parameter_list|(
name|HivePrivilegeObjectType
name|type
parameter_list|,
name|String
modifier|...
name|objs
parameter_list|)
block|{
name|filteredResults
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|obj
range|:
name|objs
control|)
block|{
name|String
name|dbname
decl_stmt|;
name|String
name|tabname
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|type
operator|==
name|HivePrivilegeObjectType
operator|.
name|DATABASE
condition|)
block|{
name|dbname
operator|=
name|obj
expr_stmt|;
block|}
else|else
block|{
name|dbname
operator|=
literal|"default"
expr_stmt|;
name|tabname
operator|=
name|obj
expr_stmt|;
block|}
name|filteredResults
operator|.
name|add
argument_list|(
operator|new
name|HivePrivilegeObject
argument_list|(
name|type
argument_list|,
name|dbname
argument_list|,
name|tabname
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|runCmd
parameter_list|(
name|String
name|cmd
parameter_list|)
throws|throws
name|CommandNeedRetryException
block|{
name|CommandProcessorResponse
name|resp
init|=
name|driver
operator|.
name|run
argument_list|(
name|cmd
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getSortedList
parameter_list|(
name|String
modifier|...
name|strings
parameter_list|)
block|{
return|return
name|getSortedList
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|strings
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getSortedList
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|sortedCols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|columns
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|sortedCols
argument_list|)
expr_stmt|;
return|return
name|sortedCols
return|;
block|}
block|}
end_class

end_unit

