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
name|io
operator|.
name|IOException
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
name|Statement
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
name|security
operator|.
name|GroupMappingServiceProvider
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
name|yarn
operator|.
name|conf
operator|.
name|YarnConfiguration
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
name|yarn
operator|.
name|server
operator|.
name|resourcemanager
operator|.
name|scheduler
operator|.
name|fair
operator|.
name|FairSchedulerConfiguration
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
name|TestSchedulerQueue
block|{
comment|// hadoop group mapping that maps user to same group
specifier|public
specifier|static
class|class
name|HiveTestSimpleGroupMapping
implements|implements
name|GroupMappingServiceProvider
block|{
specifier|public
specifier|static
name|String
name|primaryTag
init|=
literal|""
decl_stmt|;
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getGroups
parameter_list|(
name|String
name|user
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|results
operator|.
name|add
argument_list|(
name|user
operator|+
name|primaryTag
argument_list|)
expr_stmt|;
name|results
operator|.
name|add
argument_list|(
name|user
operator|+
literal|"-group"
argument_list|)
expr_stmt|;
return|return
name|results
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cacheGroupsRefresh
parameter_list|()
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|cacheGroupsAdd
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|groups
parameter_list|)
throws|throws
name|IOException
block|{     }
block|}
specifier|private
name|MiniHS2
name|miniHS2
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
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
operator|.
name|set
argument_list|(
literal|"hadoop.security.group.mapping"
argument_list|,
name|HiveTestSimpleGroupMapping
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
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
name|DriverManager
operator|.
name|setLoginTimeout
argument_list|(
literal|0
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
name|miniHS2
operator|.
name|setConfProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|setConfProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_MAP_FAIR_SCHEDULER_QUEUE
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|setConfProperty
argument_list|(
name|YarnConfiguration
operator|.
name|RM_SCHEDULER
argument_list|,
literal|"org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairScheduler"
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
name|HiveTestSimpleGroupMapping
operator|.
name|primaryTag
operator|=
literal|""
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
name|System
operator|.
name|clearProperty
argument_list|(
literal|"mapreduce.job.queuename"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify:    *  Test is running with MR2 and queue mapping defaults are set.    *  Queue mapping is set for the connected user.    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testFairSchedulerQueueMapping
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|"user1"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
literal|"mapreduce.framework.name"
argument_list|,
literal|"yarn"
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_MAP_FAIR_SCHEDULER_QUEUE
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
name|YarnConfiguration
operator|.
name|RM_SCHEDULER
argument_list|,
literal|"org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairScheduler"
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
literal|"mapreduce.job.queuename"
argument_list|,
literal|"root.user1"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify:    *  Test is running with MR2 and queue mapping are set correctly for primary group rule.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testFairSchedulerPrimaryQueueMapping
parameter_list|()
throws|throws
name|Exception
block|{
name|miniHS2
operator|.
name|setConfProperty
argument_list|(
name|FairSchedulerConfiguration
operator|.
name|ALLOCATION_FILE
argument_list|,
literal|"fair-scheduler-test.xml"
argument_list|)
expr_stmt|;
name|HiveTestSimpleGroupMapping
operator|.
name|primaryTag
operator|=
literal|"-test"
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
literal|"user2"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
literal|"mapreduce.job.queuename"
argument_list|,
literal|"root.user2"
operator|+
name|HiveTestSimpleGroupMapping
operator|.
name|primaryTag
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify:    *  Test is running with MR2 and queue mapping are set correctly for primary group rule.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testFairSchedulerSecondaryQueueMapping
parameter_list|()
throws|throws
name|Exception
block|{
name|miniHS2
operator|.
name|setConfProperty
argument_list|(
name|FairSchedulerConfiguration
operator|.
name|ALLOCATION_FILE
argument_list|,
literal|"fair-scheduler-test.xml"
argument_list|)
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
literal|"user3"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
literal|"mapreduce.job.queuename"
argument_list|,
literal|"root.user3-group"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that the queue refresh doesn't happen when configured to be off.    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testQueueMappingCheckDisabled
parameter_list|()
throws|throws
name|Exception
block|{
name|miniHS2
operator|.
name|setConfProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_MAP_FAIR_SCHEDULER_QUEUE
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
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
literal|"user1"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_MAP_FAIR_SCHEDULER_QUEUE
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|verifyProperty
argument_list|(
literal|"mapreduce.job.queuename"
argument_list|,
name|YarnConfiguration
operator|.
name|DEFAULT_QUEUE_NAME
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that the given property contains the expected value.    *    * @param propertyName    * @param expectedValue    * @throws Exception    */
specifier|private
name|void
name|verifyProperty
parameter_list|(
name|String
name|propertyName
parameter_list|,
name|String
name|expectedValue
parameter_list|)
throws|throws
name|Exception
block|{
name|Statement
name|stmt
init|=
name|hs2Conn
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|ResultSet
name|res
init|=
name|stmt
operator|.
name|executeQuery
argument_list|(
literal|"set "
operator|+
name|propertyName
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
name|String
name|results
index|[]
init|=
name|res
operator|.
name|getString
argument_list|(
literal|1
argument_list|)
operator|.
name|split
argument_list|(
literal|"="
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Property should be set"
argument_list|,
name|results
operator|.
name|length
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Property should be set"
argument_list|,
name|expectedValue
argument_list|,
name|results
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

