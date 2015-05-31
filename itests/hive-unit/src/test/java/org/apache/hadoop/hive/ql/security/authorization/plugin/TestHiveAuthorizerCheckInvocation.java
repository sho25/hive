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
name|junit
operator|.
name|Assert
operator|.
name|assertNull
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
name|fail
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|reset
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
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
name|commons
operator|.
name|lang3
operator|.
name|tuple
operator|.
name|ImmutablePair
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
name|tuple
operator|.
name|Pair
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
name|lockmgr
operator|.
name|DbTxnManager
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
name|ArgumentCaptor
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
comment|/**  * Test HiveAuthorizer api invocation  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveAuthorizerCheckInvocation
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
empty_stmt|;
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
name|tableName
init|=
name|TestHiveAuthorizerCheckInvocation
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"Table"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|inDbTableName
init|=
name|tableName
operator|+
literal|"_in_db"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|acidTableName
init|=
name|tableName
operator|+
literal|"_acid"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|dbName
init|=
name|TestHiveAuthorizerCheckInvocation
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"Db"
decl_stmt|;
specifier|static
name|HiveAuthorizer
name|mockedAuthorizer
decl_stmt|;
comment|/**    * This factory creates a mocked HiveAuthorizer class. Use the mocked class to    * capture the argument passed to it in the test case.    */
specifier|static
class|class
name|MockedHiveAuthorizerFactory
implements|implements
name|HiveAuthorizerFactory
block|{
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
name|TestHiveAuthorizerCheckInvocation
operator|.
name|mockedAuthorizer
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HiveAuthorizer
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|TestHiveAuthorizerCheckInvocation
operator|.
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
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_TXN_MANAGER
argument_list|,
name|DbTxnManager
operator|.
name|class
operator|.
name|getName
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
name|tableName
operator|+
literal|" (i int, j int, k string) partitioned by (city string, `date` string) "
argument_list|)
expr_stmt|;
name|runCmd
argument_list|(
literal|"create database "
operator|+
name|dbName
argument_list|)
expr_stmt|;
name|runCmd
argument_list|(
literal|"create table "
operator|+
name|dbName
operator|+
literal|"."
operator|+
name|inDbTableName
operator|+
literal|"(i int)"
argument_list|)
expr_stmt|;
comment|// Need a separate table for ACID testing since it has to be bucketed and it has to be Acid
name|runCmd
argument_list|(
literal|"create table "
operator|+
name|acidTableName
operator|+
literal|" (i int, j int, k int) clustered by (k) into 2 buckets "
operator|+
literal|"stored as orc TBLPROPERTIES ('transactional'='true')"
argument_list|)
expr_stmt|;
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
comment|// Drop the tables when we're done.  This makes the test work inside an IDE
name|runCmd
argument_list|(
literal|"drop table if exists "
operator|+
name|acidTableName
argument_list|)
expr_stmt|;
name|runCmd
argument_list|(
literal|"drop table if exists "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|runCmd
argument_list|(
literal|"drop table if exists "
operator|+
name|dbName
operator|+
literal|"."
operator|+
name|inDbTableName
argument_list|)
expr_stmt|;
name|runCmd
argument_list|(
literal|"drop database if exists "
operator|+
name|dbName
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
name|testInputSomeColumnsUsed
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
block|{
name|reset
argument_list|(
name|mockedAuthorizer
argument_list|)
expr_stmt|;
name|int
name|status
init|=
name|driver
operator|.
name|compile
argument_list|(
literal|"select i from "
operator|+
name|tableName
operator|+
literal|" where k = 'X' and city = 'Scottsdale-AZ' "
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inputs
init|=
name|getHivePrivilegeObjectInputs
argument_list|()
operator|.
name|getLeft
argument_list|()
decl_stmt|;
name|checkSingleTableInput
argument_list|(
name|inputs
argument_list|)
expr_stmt|;
name|HivePrivilegeObject
name|tableObj
init|=
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"no of columns used"
argument_list|,
literal|3
argument_list|,
name|tableObj
operator|.
name|getColumns
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Columns used"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"city"
argument_list|,
literal|"i"
argument_list|,
literal|"k"
argument_list|)
argument_list|,
name|getSortedList
argument_list|(
name|tableObj
operator|.
name|getColumns
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
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
annotation|@
name|Test
specifier|public
name|void
name|testInputAllColumnsUsed
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
block|{
name|reset
argument_list|(
name|mockedAuthorizer
argument_list|)
expr_stmt|;
name|int
name|status
init|=
name|driver
operator|.
name|compile
argument_list|(
literal|"select * from "
operator|+
name|tableName
operator|+
literal|" order by i"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inputs
init|=
name|getHivePrivilegeObjectInputs
argument_list|()
operator|.
name|getLeft
argument_list|()
decl_stmt|;
name|checkSingleTableInput
argument_list|(
name|inputs
argument_list|)
expr_stmt|;
name|HivePrivilegeObject
name|tableObj
init|=
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"no of columns used"
argument_list|,
literal|5
argument_list|,
name|tableObj
operator|.
name|getColumns
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Columns used"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"city"
argument_list|,
literal|"date"
argument_list|,
literal|"i"
argument_list|,
literal|"j"
argument_list|,
literal|"k"
argument_list|)
argument_list|,
name|getSortedList
argument_list|(
name|tableObj
operator|.
name|getColumns
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateTableWithDb
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
block|{
specifier|final
name|String
name|newTable
init|=
literal|"ctTableWithDb"
decl_stmt|;
name|checkCreateViewOrTableWithDb
argument_list|(
name|newTable
argument_list|,
literal|"create table "
operator|+
name|dbName
operator|+
literal|"."
operator|+
name|newTable
operator|+
literal|"(i int)"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateViewWithDb
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
block|{
specifier|final
name|String
name|newTable
init|=
literal|"ctViewWithDb"
decl_stmt|;
name|checkCreateViewOrTableWithDb
argument_list|(
name|newTable
argument_list|,
literal|"create table "
operator|+
name|dbName
operator|+
literal|"."
operator|+
name|newTable
operator|+
literal|"(i int)"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkCreateViewOrTableWithDb
parameter_list|(
name|String
name|newTable
parameter_list|,
name|String
name|cmd
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
name|reset
argument_list|(
name|mockedAuthorizer
argument_list|)
expr_stmt|;
name|int
name|status
init|=
name|driver
operator|.
name|compile
argument_list|(
name|cmd
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|outputs
init|=
name|getHivePrivilegeObjectInputs
argument_list|()
operator|.
name|getRight
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"num outputs"
argument_list|,
literal|2
argument_list|,
name|outputs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HivePrivilegeObject
name|output
range|:
name|outputs
control|)
block|{
switch|switch
condition|(
name|output
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|DATABASE
case|:
name|assertTrue
argument_list|(
literal|"database name"
argument_list|,
name|output
operator|.
name|getDbname
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|dbName
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|TABLE_OR_VIEW
case|:
name|assertTrue
argument_list|(
literal|"database name"
argument_list|,
name|output
operator|.
name|getDbname
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|dbName
argument_list|)
argument_list|)
expr_stmt|;
name|assertEqualsIgnoreCase
argument_list|(
literal|"table name"
argument_list|,
name|output
operator|.
name|getObjectName
argument_list|()
argument_list|,
name|newTable
argument_list|)
expr_stmt|;
break|break;
default|default:
name|fail
argument_list|(
literal|"Unexpected type : "
operator|+
name|output
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|assertEqualsIgnoreCase
parameter_list|(
name|String
name|msg
parameter_list|,
name|String
name|expected
parameter_list|,
name|String
name|actual
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|msg
argument_list|,
name|expected
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|actual
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInputNoColumnsUsed
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
block|{
name|reset
argument_list|(
name|mockedAuthorizer
argument_list|)
expr_stmt|;
name|int
name|status
init|=
name|driver
operator|.
name|compile
argument_list|(
literal|"describe "
operator|+
name|tableName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inputs
init|=
name|getHivePrivilegeObjectInputs
argument_list|()
operator|.
name|getLeft
argument_list|()
decl_stmt|;
name|checkSingleTableInput
argument_list|(
name|inputs
argument_list|)
expr_stmt|;
name|HivePrivilegeObject
name|tableObj
init|=
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"columns used"
argument_list|,
name|tableObj
operator|.
name|getColumns
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPermFunction
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
block|{
name|reset
argument_list|(
name|mockedAuthorizer
argument_list|)
expr_stmt|;
specifier|final
name|String
name|funcName
init|=
literal|"testauthfunc1"
decl_stmt|;
name|int
name|status
init|=
name|driver
operator|.
name|compile
argument_list|(
literal|"create function "
operator|+
name|dbName
operator|+
literal|"."
operator|+
name|funcName
operator|+
literal|" as 'org.apache.hadoop.hive.ql.udf.UDFPI'"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|outputs
init|=
name|getHivePrivilegeObjectInputs
argument_list|()
operator|.
name|getRight
argument_list|()
decl_stmt|;
name|HivePrivilegeObject
name|funcObj
decl_stmt|;
name|HivePrivilegeObject
name|dbObj
decl_stmt|;
name|assertEquals
argument_list|(
literal|"number of output object"
argument_list|,
literal|2
argument_list|,
name|outputs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|outputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
operator|==
name|HivePrivilegeObjectType
operator|.
name|FUNCTION
condition|)
block|{
name|funcObj
operator|=
name|outputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|dbObj
operator|=
name|outputs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|funcObj
operator|=
name|outputs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|dbObj
operator|=
name|outputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"input type"
argument_list|,
name|HivePrivilegeObjectType
operator|.
name|FUNCTION
argument_list|,
name|funcObj
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"function name"
argument_list|,
name|funcName
operator|.
name|equalsIgnoreCase
argument_list|(
name|funcObj
operator|.
name|getObjectName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"db name"
argument_list|,
name|dbName
operator|.
name|equalsIgnoreCase
argument_list|(
name|funcObj
operator|.
name|getDbname
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"input type"
argument_list|,
name|HivePrivilegeObjectType
operator|.
name|DATABASE
argument_list|,
name|dbObj
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"db name"
argument_list|,
name|dbName
operator|.
name|equalsIgnoreCase
argument_list|(
name|dbObj
operator|.
name|getDbname
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTempFunction
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
block|{
name|reset
argument_list|(
name|mockedAuthorizer
argument_list|)
expr_stmt|;
specifier|final
name|String
name|funcName
init|=
literal|"testAuthFunc2"
decl_stmt|;
name|int
name|status
init|=
name|driver
operator|.
name|compile
argument_list|(
literal|"create temporary function "
operator|+
name|funcName
operator|+
literal|" as 'org.apache.hadoop.hive.ql.udf.UDFPI'"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|outputs
init|=
name|getHivePrivilegeObjectInputs
argument_list|()
operator|.
name|getRight
argument_list|()
decl_stmt|;
name|HivePrivilegeObject
name|funcObj
init|=
name|outputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"input type"
argument_list|,
name|HivePrivilegeObjectType
operator|.
name|FUNCTION
argument_list|,
name|funcObj
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"function name"
argument_list|,
name|funcName
operator|.
name|equalsIgnoreCase
argument_list|(
name|funcObj
operator|.
name|getObjectName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"db name"
argument_list|,
literal|null
argument_list|,
name|funcObj
operator|.
name|getDbname
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateSomeColumnsUsed
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
block|{
name|reset
argument_list|(
name|mockedAuthorizer
argument_list|)
expr_stmt|;
name|int
name|status
init|=
name|driver
operator|.
name|compile
argument_list|(
literal|"update "
operator|+
name|acidTableName
operator|+
literal|" set i = 5 where j = 3"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|>
name|io
init|=
name|getHivePrivilegeObjectInputs
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|outputs
init|=
name|io
operator|.
name|getRight
argument_list|()
decl_stmt|;
name|HivePrivilegeObject
name|tableObj
init|=
name|outputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Got privilege object "
operator|+
name|tableObj
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"no of columns used"
argument_list|,
literal|1
argument_list|,
name|tableObj
operator|.
name|getColumns
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Column used"
argument_list|,
literal|"i"
argument_list|,
name|tableObj
operator|.
name|getColumns
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inputs
init|=
name|io
operator|.
name|getLeft
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|inputs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|tableObj
operator|=
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tableObj
operator|.
name|getColumns
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"j"
argument_list|,
name|tableObj
operator|.
name|getColumns
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateSomeColumnsUsedExprInSet
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
block|{
name|reset
argument_list|(
name|mockedAuthorizer
argument_list|)
expr_stmt|;
name|int
name|status
init|=
name|driver
operator|.
name|compile
argument_list|(
literal|"update "
operator|+
name|acidTableName
operator|+
literal|" set i = 5, l = k where j = 3"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|>
name|io
init|=
name|getHivePrivilegeObjectInputs
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|outputs
init|=
name|io
operator|.
name|getRight
argument_list|()
decl_stmt|;
name|HivePrivilegeObject
name|tableObj
init|=
name|outputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Got privilege object "
operator|+
name|tableObj
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"no of columns used"
argument_list|,
literal|2
argument_list|,
name|tableObj
operator|.
name|getColumns
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Columns used"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"i"
argument_list|,
literal|"l"
argument_list|)
argument_list|,
name|getSortedList
argument_list|(
name|tableObj
operator|.
name|getColumns
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inputs
init|=
name|io
operator|.
name|getLeft
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|inputs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|tableObj
operator|=
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tableObj
operator|.
name|getColumns
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Columns used"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"j"
argument_list|,
literal|"k"
argument_list|)
argument_list|,
name|getSortedList
argument_list|(
name|tableObj
operator|.
name|getColumns
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDelete
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
block|{
name|reset
argument_list|(
name|mockedAuthorizer
argument_list|)
expr_stmt|;
name|int
name|status
init|=
name|driver
operator|.
name|compile
argument_list|(
literal|"delete from "
operator|+
name|acidTableName
operator|+
literal|" where j = 3"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|>
name|io
init|=
name|getHivePrivilegeObjectInputs
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inputs
init|=
name|io
operator|.
name|getLeft
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|inputs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HivePrivilegeObject
name|tableObj
init|=
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tableObj
operator|.
name|getColumns
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"j"
argument_list|,
name|tableObj
operator|.
name|getColumns
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testShowTables
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
block|{
name|reset
argument_list|(
name|mockedAuthorizer
argument_list|)
expr_stmt|;
name|int
name|status
init|=
name|driver
operator|.
name|compile
argument_list|(
literal|"show tables"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|>
name|io
init|=
name|getHivePrivilegeObjectInputs
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inputs
init|=
name|io
operator|.
name|getLeft
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|inputs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HivePrivilegeObject
name|dbObj
init|=
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"default"
argument_list|,
name|dbObj
operator|.
name|getDbname
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDescDatabase
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
throws|,
name|CommandNeedRetryException
block|{
name|reset
argument_list|(
name|mockedAuthorizer
argument_list|)
expr_stmt|;
name|int
name|status
init|=
name|driver
operator|.
name|compile
argument_list|(
literal|"describe database "
operator|+
name|dbName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|>
name|io
init|=
name|getHivePrivilegeObjectInputs
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inputs
init|=
name|io
operator|.
name|getLeft
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|inputs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HivePrivilegeObject
name|dbObj
init|=
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|dbName
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|dbObj
operator|.
name|getDbname
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkSingleTableInput
parameter_list|(
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inputs
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"number of inputs"
argument_list|,
literal|1
argument_list|,
name|inputs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HivePrivilegeObject
name|tableObj
init|=
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"input type"
argument_list|,
name|HivePrivilegeObjectType
operator|.
name|TABLE_OR_VIEW
argument_list|,
name|tableObj
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"table name"
argument_list|,
name|tableName
operator|.
name|equalsIgnoreCase
argument_list|(
name|tableObj
operator|.
name|getObjectName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return pair with left value as inputs and right value as outputs,    *  passed in current call to authorizer.checkPrivileges    * @throws HiveAuthzPluginException    * @throws HiveAccessControlException    */
specifier|private
name|Pair
argument_list|<
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|>
name|getHivePrivilegeObjectInputs
parameter_list|()
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
comment|// Create argument capturer
comment|// a class variable cast to this generic of generic class
name|Class
argument_list|<
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|>
name|class_listPrivObjects
init|=
operator|(
name|Class
operator|)
name|List
operator|.
name|class
decl_stmt|;
name|ArgumentCaptor
argument_list|<
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|>
name|inputsCapturer
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|class_listPrivObjects
argument_list|)
decl_stmt|;
name|ArgumentCaptor
argument_list|<
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|>
name|outputsCapturer
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|class_listPrivObjects
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|mockedAuthorizer
argument_list|)
operator|.
name|checkPrivileges
argument_list|(
name|any
argument_list|(
name|HiveOperationType
operator|.
name|class
argument_list|)
argument_list|,
name|inputsCapturer
operator|.
name|capture
argument_list|()
argument_list|,
name|outputsCapturer
operator|.
name|capture
argument_list|()
argument_list|,
name|any
argument_list|(
name|HiveAuthzContext
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|ImmutablePair
argument_list|(
name|inputsCapturer
operator|.
name|getValue
argument_list|()
argument_list|,
name|outputsCapturer
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

