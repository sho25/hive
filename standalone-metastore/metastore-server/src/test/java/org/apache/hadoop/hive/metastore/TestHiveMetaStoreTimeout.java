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
name|metastore
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|hive
operator|.
name|metastore
operator|.
name|annotation
operator|.
name|MetastoreCheckinTest
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
name|client
operator|.
name|builder
operator|.
name|DatabaseBuilder
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
name|conf
operator|.
name|MetastoreConf
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
name|conf
operator|.
name|MetastoreConf
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
name|util
operator|.
name|StringUtils
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
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * Test long running request timeout functionality in MetaStore Server  * HiveMetaStore.HMSHandler.create_database() is used to simulate a long running method.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MetastoreCheckinTest
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestHiveMetaStoreTimeout
block|{
specifier|protected
specifier|static
name|HiveMetaStoreClient
name|client
decl_stmt|;
specifier|protected
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|protected
specifier|static
name|Warehouse
name|warehouse
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveMetaStore
operator|.
name|TEST_TIMEOUT_ENABLED
operator|=
literal|true
expr_stmt|;
name|conf
operator|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
expr_stmt|;
name|MetastoreConf
operator|.
name|setClass
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|EXPRESSION_PROXY_CLASS
argument_list|,
name|MockPartitionExpressionForMetastore
operator|.
name|class
argument_list|,
name|PartitionExpressionProxy
operator|.
name|class
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setTimeVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|CLIENT_SOCKET_TIMEOUT
argument_list|,
literal|1000
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|MetaStoreTestUtils
operator|.
name|setConfForStandloneMode
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|warehouse
operator|=
operator|new
name|Warehouse
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|client
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveMetaStore
operator|.
name|TEST_TIMEOUT_ENABLED
operator|=
literal|false
expr_stmt|;
try|try
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Unable to close metastore"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoTimeout
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveMetaStore
operator|.
name|TEST_TIMEOUT_VALUE
operator|=
literal|250
expr_stmt|;
name|String
name|dbName
init|=
literal|"db"
decl_stmt|;
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
operator|new
name|DatabaseBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|dbName
argument_list|)
operator|.
name|create
argument_list|(
name|client
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimeout
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveMetaStore
operator|.
name|TEST_TIMEOUT_VALUE
operator|=
literal|2
operator|*
literal|1000
expr_stmt|;
name|String
name|dbName
init|=
literal|"db"
decl_stmt|;
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Database
name|db
init|=
operator|new
name|DatabaseBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|dbName
argument_list|)
operator|.
name|build
argument_list|(
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|client
operator|.
name|createDatabase
argument_list|(
name|db
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"should throw timeout exception."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"unexpected MetaException"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Timeout when "
operator|+
literal|"executing method: create_database"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// restore
name|HiveMetaStore
operator|.
name|TEST_TIMEOUT_VALUE
operator|=
literal|1
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testResetTimeout
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveMetaStore
operator|.
name|TEST_TIMEOUT_VALUE
operator|=
literal|250
expr_stmt|;
name|String
name|dbName
init|=
literal|"db"
decl_stmt|;
comment|// no timeout before reset
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Database
name|db
init|=
operator|new
name|DatabaseBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|dbName
argument_list|)
operator|.
name|build
argument_list|(
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|client
operator|.
name|createDatabase
argument_list|(
name|db
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"should not throw timeout exception: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// reset
name|HiveMetaStore
operator|.
name|TEST_TIMEOUT_VALUE
operator|=
literal|2000
expr_stmt|;
name|client
operator|.
name|setMetaConf
argument_list|(
name|ConfVars
operator|.
name|CLIENT_SOCKET_TIMEOUT
operator|.
name|getVarname
argument_list|()
argument_list|,
literal|"1s"
argument_list|)
expr_stmt|;
comment|// timeout after reset
try|try
block|{
name|client
operator|.
name|createDatabase
argument_list|(
name|db
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"should throw timeout exception."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"unexpected MetaException"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Timeout when "
operator|+
literal|"executing method: create_database"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// restore
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|client
operator|.
name|setMetaConf
argument_list|(
name|ConfVars
operator|.
name|CLIENT_SOCKET_TIMEOUT
operator|.
name|getVarname
argument_list|()
argument_list|,
literal|"10s"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

