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

begin_comment
comment|/**  * Test that set/unset of metaconf:hive.metastore.disallow.incompatible.col.type.changes is local  * to the session  *  * Two connections are created.  In one connection  * metaconf:hive.metastore.disallow.incompatible.col.type.changes is set to false.  Then the  * columm type can changed from int to smallint.  In the other connection  * metaconf:hive.metastore.disallow.incompatible.col.type.changes is left with the default value  * true.  In that connection changing column type from int to smallint will throw an error.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveMetaStoreAlterColumnPar
block|{
specifier|public
specifier|static
name|MiniHS2
name|miniHS2
init|=
literal|null
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|startServices
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_MIN_WORKER_THREADS
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_MAX_WORKER_THREADS
argument_list|,
literal|2
argument_list|)
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
name|miniHS2
operator|=
operator|new
name|MiniHS2
operator|.
name|Builder
argument_list|()
operator|.
name|withMiniMR
argument_list|()
operator|.
name|withRemoteMetastore
argument_list|()
operator|.
name|withConf
argument_list|(
name|hiveConf
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|miniHS2
operator|.
name|start
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
name|Connection
name|hs2Conn
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
literal|"hive"
argument_list|,
literal|"hive"
argument_list|)
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
literal|"drop table if exists t1"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"create table t1 (c1 int)"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|close
argument_list|()
expr_stmt|;
name|hs2Conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|stopServices
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
annotation|@
name|Test
specifier|public
name|void
name|testAlterColumn
parameter_list|()
throws|throws
name|Exception
block|{
name|assertTrue
argument_list|(
literal|"Test setup failed. MiniHS2 is not initialized"
argument_list|,
name|miniHS2
operator|!=
literal|null
operator|&&
name|miniHS2
operator|.
name|isStarted
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|Connection
name|hs2Conn1
init|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|TestHiveMetaStoreAlterColumnPar
operator|.
name|miniHS2
operator|.
name|getJdbcURL
argument_list|()
argument_list|,
literal|"hive"
argument_list|,
literal|"hive"
argument_list|)
init|;
name|Statement
name|stmt1
operator|=
name|hs2Conn1
operator|.
name|createStatement
argument_list|()
init|;
name|Connection
name|hs2Conn2
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|TestHiveMetaStoreAlterColumnPar
operator|.
name|miniHS2
operator|.
name|getJdbcURL
argument_list|()
argument_list|,
literal|"hive"
argument_list|,
literal|"hive"
argument_list|)
init|;
name|Statement
name|stmt2
operator|=
name|hs2Conn2
operator|.
name|createStatement
argument_list|()
init|;
init|)
block|{
comment|// Set parameter to be false in connection 1.  int to smallint allowed
name|stmt1
operator|.
name|execute
argument_list|(
literal|"set metaconf:hive.metastore.disallow.incompatible.col.type.changes=false"
argument_list|)
expr_stmt|;
name|stmt1
operator|.
name|execute
argument_list|(
literal|"alter table t1 change column c1 c1 smallint"
argument_list|)
expr_stmt|;
comment|// Change the type back to int so that the same alter can be attempted from connection 2.
name|stmt1
operator|.
name|execute
argument_list|(
literal|"alter table t1 change column c1 c1 int"
argument_list|)
expr_stmt|;
comment|// parameter value not changed to false in connection 2.  int to smallint throws exception
try|try
block|{
name|stmt2
operator|.
name|execute
argument_list|(
literal|"alter table t1 change column c1 c1 smallint"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Exception not thrown"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|"Unexpected exception: "
operator|+
name|e1
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e1
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Unable to alter table. The following columns have types incompatible with the existing columns in their respective positions"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// parameter value is still false in 1st connection.  The alter still goes through.
name|stmt1
operator|.
name|execute
argument_list|(
literal|"alter table t1 change column c1 c1 smallint"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e2
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|"Unexpected Exception: "
operator|+
name|e2
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

