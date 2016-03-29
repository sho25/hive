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
name|hive
operator|.
name|jdbc
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
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|DriverManager
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|ResultSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Statement
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
name|Map
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|curator
operator|.
name|test
operator|.
name|TestingServer
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
name|hive
operator|.
name|jdbc
operator|.
name|miniHS2
operator|.
name|MiniHS2
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

begin_class
specifier|public
class|class
name|TestServiceDiscoveryWithMiniHS2
block|{
specifier|private
specifier|static
name|MiniHS2
name|miniHS2
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|static
name|TestingServer
name|zkServer
decl_stmt|;
specifier|private
specifier|static
name|String
name|zkRootNamespace
init|=
literal|"hs2test"
decl_stmt|;
specifier|private
specifier|static
name|String
name|dataFileDir
decl_stmt|;
specifier|private
specifier|static
name|Path
name|kvDataFilePath
decl_stmt|;
specifier|private
name|Connection
name|hs2Conn
init|=
literal|null
decl_stmt|;
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
name|zkServer
operator|=
operator|new
name|TestingServer
argument_list|()
expr_stmt|;
name|Class
operator|.
name|forName
argument_list|(
name|MiniHS2
operator|.
name|getJdbcDriverName
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|hiveConf
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
comment|// Set up zookeeper dynamic service discovery configs
name|enableZKServiceDiscoveryConfigs
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|dataFileDir
operator|=
name|hiveConf
operator|.
name|get
argument_list|(
literal|"test.data.files"
argument_list|)
operator|.
name|replace
argument_list|(
literal|'\\'
argument_list|,
literal|'/'
argument_list|)
operator|.
name|replace
argument_list|(
literal|"c:"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|kvDataFilePath
operator|=
operator|new
name|Path
argument_list|(
name|dataFileDir
argument_list|,
literal|"kv1.txt"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterTest
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|zkServer
operator|!=
literal|null
condition|)
block|{
name|zkServer
operator|.
name|close
argument_list|()
expr_stmt|;
name|zkServer
operator|=
literal|null
expr_stmt|;
block|}
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
name|miniHS2
operator|=
operator|new
name|MiniHS2
argument_list|(
name|hiveConf
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
block|{
if|if
condition|(
name|hs2Conn
operator|!=
literal|null
condition|)
block|{
name|hs2Conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|miniHS2
operator|!=
literal|null
operator|)
operator|&&
name|miniHS2
operator|.
name|isStarted
argument_list|()
condition|)
block|{
name|miniHS2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConnectionWithConfigsPublished
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|confOverlay
operator|.
name|put
argument_list|(
literal|"hive.server2.zookeeper.publish.configs"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
name|openConnectionAndRunQuery
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConnectionWithoutConfigsPublished
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|confOverlay
operator|.
name|put
argument_list|(
literal|"hive.server2.zookeeper.publish.configs"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
name|openConnectionAndRunQuery
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|enableZKServiceDiscoveryConfigs
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_SUPPORT_DYNAMIC_SERVICE_DISCOVERY
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
name|HIVE_ZOOKEEPER_QUORUM
argument_list|,
name|zkServer
operator|.
name|getConnectString
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ZOOKEEPER_NAMESPACE
argument_list|,
name|zkRootNamespace
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Connection
name|getConnection
parameter_list|(
name|String
name|jdbcURL
parameter_list|,
name|String
name|user
parameter_list|,
name|String
name|pwd
parameter_list|)
throws|throws
name|SQLException
block|{
name|Connection
name|conn
init|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|jdbcURL
argument_list|,
name|user
argument_list|,
name|pwd
argument_list|)
decl_stmt|;
return|return
name|conn
return|;
block|}
specifier|private
name|void
name|openConnectionAndRunQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|hs2Conn
operator|=
name|getConnection
argument_list|(
name|miniHS2
operator|.
name|getJdbcURL
argument_list|()
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|String
name|tableName
init|=
literal|"testTab1"
decl_stmt|;
name|Statement
name|stmt
init|=
name|hs2Conn
operator|.
name|createStatement
argument_list|()
decl_stmt|;
comment|// create table
name|stmt
operator|.
name|execute
argument_list|(
literal|"DROP TABLE IF EXISTS "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"CREATE TABLE "
operator|+
name|tableName
operator|+
literal|" (under_col INT COMMENT 'the under column', value STRING) COMMENT ' test table'"
argument_list|)
expr_stmt|;
comment|// load data
name|stmt
operator|.
name|execute
argument_list|(
literal|"load data local inpath '"
operator|+
name|kvDataFilePath
operator|.
name|toString
argument_list|()
operator|+
literal|"' into table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|ResultSet
name|res
init|=
name|stmt
operator|.
name|executeQuery
argument_list|(
literal|"SELECT * FROM "
operator|+
name|tableName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|res
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"val_238"
argument_list|,
name|res
operator|.
name|getString
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|res
operator|.
name|close
argument_list|()
expr_stmt|;
name|stmt
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

