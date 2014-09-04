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
name|service
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
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileStatus
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
name|shims
operator|.
name|HadoopShims
operator|.
name|MiniDFSShim
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
comment|/**  * Test HiveServer2 sends correct user name to remote MetaStore server for user impersonation.  */
end_comment

begin_class
specifier|public
class|class
name|TestHS2ImpersonationWithRemoteMS
block|{
specifier|private
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
literal|1
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
literal|1
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|METASTORE_EXECUTE_SET_UGI
argument_list|,
literal|true
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
name|testImpersonation
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
comment|// Create two tables one as user "foo" and other as user "bar"
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
literal|"foo"
argument_list|,
literal|null
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
name|String
name|tableName
init|=
literal|"foo_table"
decl_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"drop table if exists "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"create table "
operator|+
name|tableName
operator|+
literal|" (value string)"
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
name|hs2Conn
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|miniHS2
operator|.
name|getJdbcURL
argument_list|()
argument_list|,
literal|"bar"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|stmt
operator|=
name|hs2Conn
operator|.
name|createStatement
argument_list|()
expr_stmt|;
name|tableName
operator|=
literal|"bar_table"
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"drop table if exists "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"create table "
operator|+
name|tableName
operator|+
literal|" (value string)"
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
name|MiniDFSShim
name|dfs
init|=
name|miniHS2
operator|.
name|getDfs
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|dfs
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|FileStatus
index|[]
name|files
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|miniHS2
operator|.
name|getWareHouseDir
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|fooTableValidated
init|=
literal|false
decl_stmt|;
name|boolean
name|barTableValidated
init|=
literal|false
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
specifier|final
name|String
name|name
init|=
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|final
name|String
name|owner
init|=
name|file
operator|.
name|getOwner
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"foo_table"
argument_list|)
condition|)
block|{
name|fooTableValidated
operator|=
name|owner
operator|.
name|equals
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"User 'foo' table has wrong ownership '%s'"
argument_list|,
name|owner
argument_list|)
argument_list|,
name|fooTableValidated
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"bar_table"
argument_list|)
condition|)
block|{
name|barTableValidated
operator|=
name|owner
operator|.
name|equals
argument_list|(
literal|"bar"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"User 'bar' table has wrong ownership '%s'"
argument_list|,
name|owner
argument_list|)
argument_list|,
name|barTableValidated
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fail
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Unexpected table directory '%s' in warehouse"
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"File: %s, Owner: %s"
argument_list|,
name|name
argument_list|,
name|owner
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"User 'foo' table not found in warehouse"
argument_list|,
name|fooTableValidated
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"User 'bar' table not found in warehouse"
argument_list|,
name|barTableValidated
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

