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
name|LinkedList
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadFactoryBuilder
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
name|apache
operator|.
name|hive
operator|.
name|jdbc
operator|.
name|miniHS2
operator|.
name|MiniHS2
operator|.
name|MiniClusterType
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
name|service
operator|.
name|cli
operator|.
name|HiveSQLException
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
name|service
operator|.
name|cli
operator|.
name|session
operator|.
name|HiveSessionHook
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
name|service
operator|.
name|cli
operator|.
name|session
operator|.
name|HiveSessionHookContext
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

begin_class
specifier|public
class|class
name|TestMultiSessionsHS2WithLocalClusterSpark
block|{
specifier|public
specifier|static
specifier|final
name|String
name|TEST_TAG
init|=
literal|"miniHS2.localClusterSpark.tag"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEST_TAG_VALUE
init|=
literal|"miniHS2.localClusterSpark.value"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|PARALLEL_NUMBER
init|=
literal|3
decl_stmt|;
specifier|public
specifier|static
class|class
name|LocalClusterSparkSessionHook
implements|implements
name|HiveSessionHook
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|HiveSessionHookContext
name|sessionHookContext
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|sessionHookContext
operator|.
name|getSessionConf
argument_list|()
operator|.
name|set
argument_list|(
name|TEST_TAG
argument_list|,
name|TEST_TAG_VALUE
argument_list|)
expr_stmt|;
block|}
block|}
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
name|conf
decl_stmt|;
specifier|private
specifier|static
name|Path
name|dataFilePath
decl_stmt|;
specifier|private
specifier|static
name|String
name|dbName
init|=
literal|"sparkTestDb"
decl_stmt|;
specifier|private
name|ThreadLocal
argument_list|<
name|Connection
argument_list|>
name|localConnection
init|=
operator|new
name|ThreadLocal
argument_list|<
name|Connection
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|ThreadLocal
argument_list|<
name|Statement
argument_list|>
name|localStatement
init|=
operator|new
name|ThreadLocal
argument_list|<
name|Statement
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|ExecutorService
name|pool
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|createHiveConf
parameter_list|()
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hive.exec.parallel"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hive.execution.engine"
argument_list|,
literal|"spark"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"spark.serializer"
argument_list|,
literal|"org.apache.spark.serializer.KryoSerializer"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"spark.master"
argument_list|,
literal|"local-cluster[2,2,1024]"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"spark.deploy.defaultCores"
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
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
name|conf
operator|=
name|createHiveConf
argument_list|()
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
name|String
name|dataFileDir
init|=
name|conf
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
decl_stmt|;
name|dataFilePath
operator|=
operator|new
name|Path
argument_list|(
name|dataFileDir
argument_list|,
literal|"kv1.txt"
argument_list|)
expr_stmt|;
name|DriverManager
operator|.
name|setLoginTimeout
argument_list|(
literal|0
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
name|miniHS2
operator|=
operator|new
name|MiniHS2
argument_list|(
name|conf
argument_list|,
name|MiniClusterType
operator|.
name|MR
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|overlayProps
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
name|overlayProps
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_SESSION_HOOK
operator|.
name|varname
argument_list|,
name|LocalClusterSparkSessionHook
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|start
argument_list|(
name|overlayProps
argument_list|)
expr_stmt|;
name|createDb
argument_list|()
expr_stmt|;
block|}
comment|// setup DB
specifier|private
specifier|static
name|void
name|createDb
parameter_list|()
throws|throws
name|Exception
block|{
name|Connection
name|conn
init|=
name|DriverManager
operator|.
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
decl_stmt|;
name|Statement
name|stmt2
init|=
name|conn
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|stmt2
operator|.
name|execute
argument_list|(
literal|"DROP DATABASE IF EXISTS "
operator|+
name|dbName
operator|+
literal|" CASCADE"
argument_list|)
expr_stmt|;
name|stmt2
operator|.
name|execute
argument_list|(
literal|"CREATE DATABASE "
operator|+
name|dbName
argument_list|)
expr_stmt|;
name|stmt2
operator|.
name|close
argument_list|()
expr_stmt|;
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|pool
operator|=
name|Executors
operator|.
name|newFixedThreadPool
argument_list|(
name|PARALLEL_NUMBER
argument_list|,
operator|new
name|ThreadFactoryBuilder
argument_list|()
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
operator|.
name|setNameFormat
argument_list|(
literal|"Test-Thread-%d"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|createConnection
argument_list|()
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
name|pool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
name|closeConnection
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|createConnection
parameter_list|()
throws|throws
name|Exception
block|{
name|Connection
name|connection
init|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|miniHS2
operator|.
name|getJdbcURL
argument_list|(
name|dbName
argument_list|)
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
decl_stmt|;
name|Statement
name|statement
init|=
name|connection
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|localConnection
operator|.
name|set
argument_list|(
name|connection
argument_list|)
expr_stmt|;
name|localStatement
operator|.
name|set
argument_list|(
name|statement
argument_list|)
expr_stmt|;
name|statement
operator|.
name|execute
argument_list|(
literal|"USE "
operator|+
name|dbName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|closeConnection
parameter_list|()
throws|throws
name|SQLException
block|{
if|if
condition|(
name|localStatement
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|localStatement
operator|.
name|get
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|localConnection
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|localConnection
operator|.
name|get
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
name|miniHS2
operator|!=
literal|null
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
comment|/**    * Run nonSpark query    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testNonSparkQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|tableName
init|=
literal|"kvTable1"
decl_stmt|;
name|setupTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|Callable
argument_list|<
name|Void
argument_list|>
name|runNonSparkQuery
init|=
name|getNonSparkQueryCallable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|runInParallel
argument_list|(
name|runNonSparkQuery
argument_list|)
expr_stmt|;
name|dropTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|/**    * Run spark query    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testSparkQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|tableName
init|=
literal|"kvTable2"
decl_stmt|;
name|setupTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|Callable
argument_list|<
name|Void
argument_list|>
name|runSparkQuery
init|=
name|getSparkQueryCallable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|runInParallel
argument_list|(
name|runSparkQuery
argument_list|)
expr_stmt|;
name|dropTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runInParallel
parameter_list|(
name|Callable
argument_list|<
name|Void
argument_list|>
name|runNonSparkQuery
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|List
argument_list|<
name|Future
argument_list|>
name|futureList
init|=
operator|new
name|LinkedList
argument_list|<
name|Future
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|PARALLEL_NUMBER
condition|;
name|i
operator|++
control|)
block|{
name|Future
name|future
init|=
name|pool
operator|.
name|submit
argument_list|(
name|runNonSparkQuery
argument_list|)
decl_stmt|;
name|futureList
operator|.
name|add
argument_list|(
name|future
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Future
name|future
range|:
name|futureList
control|)
block|{
name|future
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|Callable
argument_list|<
name|Void
argument_list|>
name|getNonSparkQueryCallable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
return|return
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|resultVal
init|=
literal|"val_238"
decl_stmt|;
name|String
name|queryStr
init|=
literal|"SELECT * FROM "
operator|+
name|tableName
decl_stmt|;
name|testKvQuery
argument_list|(
name|queryStr
argument_list|,
name|resultVal
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
return|;
block|}
specifier|private
name|Callable
argument_list|<
name|Void
argument_list|>
name|getSparkQueryCallable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
return|return
operator|new
name|Callable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|resultVal
init|=
literal|"val_238"
decl_stmt|;
name|String
name|queryStr
init|=
literal|"SELECT * FROM "
operator|+
name|tableName
operator|+
literal|" where value = '"
operator|+
name|resultVal
operator|+
literal|"'"
decl_stmt|;
name|testKvQuery
argument_list|(
name|queryStr
argument_list|,
name|resultVal
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
return|;
block|}
specifier|private
name|void
name|testKvQuery
parameter_list|(
name|String
name|queryStr
parameter_list|,
name|String
name|resultVal
parameter_list|)
throws|throws
name|Exception
block|{
name|createConnection
argument_list|()
expr_stmt|;
name|verifyResult
argument_list|(
name|queryStr
argument_list|,
name|resultVal
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|closeConnection
argument_list|()
expr_stmt|;
block|}
comment|// create table and load kv1.txt
specifier|private
name|void
name|setupTable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|SQLException
block|{
name|Statement
name|statement
init|=
name|localStatement
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// create table
name|statement
operator|.
name|execute
argument_list|(
literal|"CREATE TABLE "
operator|+
name|tableName
operator|+
literal|" (under_col INT COMMENT 'the under column', value STRING)"
operator|+
literal|" COMMENT ' test table'"
argument_list|)
expr_stmt|;
comment|// load data
name|statement
operator|.
name|execute
argument_list|(
literal|"LOAD DATA LOCAL INPATH '"
operator|+
name|dataFilePath
operator|.
name|toString
argument_list|()
operator|+
literal|"' INTO TABLE "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|dropTable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|SQLException
block|{
name|localStatement
operator|.
name|get
argument_list|()
operator|.
name|execute
argument_list|(
literal|"DROP TABLE "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|// run given query and validate expected result
specifier|private
name|void
name|verifyResult
parameter_list|(
name|String
name|queryStr
parameter_list|,
name|String
name|expString
parameter_list|,
name|int
name|colPos
parameter_list|)
throws|throws
name|SQLException
block|{
name|ResultSet
name|res
init|=
name|localStatement
operator|.
name|get
argument_list|()
operator|.
name|executeQuery
argument_list|(
name|queryStr
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
name|expString
argument_list|,
name|res
operator|.
name|getString
argument_list|(
name|colPos
argument_list|)
argument_list|)
expr_stmt|;
name|res
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

