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
name|hive
operator|.
name|jdbc
operator|.
name|miniHS2
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
name|TestMiniHS2
block|{
specifier|private
name|MiniHS2
name|miniHS2
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
name|miniHS2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test if the MiniHS2 configuration gets passed down to the session    * configuration    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testConfInSession
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
specifier|final
name|String
name|DUMMY_CONF_KEY
init|=
literal|"hive.test.minihs2.dummy.config"
decl_stmt|;
specifier|final
name|String
name|DUMMY_CONF_VAL
init|=
literal|"dummy.val"
decl_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|DUMMY_CONF_KEY
argument_list|,
name|DUMMY_CONF_VAL
argument_list|)
expr_stmt|;
comment|// also check a config that has default in hiveconf
specifier|final
name|String
name|ZK_TIMEOUT_KEY
init|=
name|ConfVars
operator|.
name|HIVE_ZOOKEEPER_SESSION_TIMEOUT
operator|.
name|varname
decl_stmt|;
specifier|final
name|String
name|ZK_TIMEOUT
init|=
literal|"2562"
decl_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|ZK_TIMEOUT_KEY
argument_list|,
name|ZK_TIMEOUT
argument_list|)
expr_stmt|;
comment|// check the config used very often!
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
argument_list|(
name|hiveConf
argument_list|)
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
name|stmt
init|=
name|hs2Conn
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|checkConfVal
argument_list|(
name|DUMMY_CONF_KEY
argument_list|,
name|DUMMY_CONF_KEY
operator|+
literal|"="
operator|+
name|DUMMY_CONF_VAL
argument_list|,
name|stmt
argument_list|)
expr_stmt|;
name|checkConfVal
argument_list|(
name|ZK_TIMEOUT_KEY
argument_list|,
name|ZK_TIMEOUT_KEY
operator|+
literal|"="
operator|+
name|ZK_TIMEOUT
argument_list|,
name|stmt
argument_list|)
expr_stmt|;
name|checkConfVal
argument_list|(
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
operator|+
literal|"="
operator|+
literal|"false"
argument_list|,
name|stmt
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
specifier|private
name|void
name|checkConfVal
parameter_list|(
name|String
name|confKey
parameter_list|,
name|String
name|confResult
parameter_list|,
name|Statement
name|stmt
parameter_list|)
throws|throws
name|SQLException
block|{
name|ResultSet
name|res
init|=
name|stmt
operator|.
name|executeQuery
argument_list|(
literal|"set "
operator|+
name|confKey
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
literal|"Expected config result"
argument_list|,
name|confResult
argument_list|,
name|res
operator|.
name|getString
argument_list|(
literal|1
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

