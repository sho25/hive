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
name|parse
package|;
end_package

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
name|messaging
operator|.
name|json
operator|.
name|gzip
operator|.
name|GzipJSONMessageEncoder
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
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestName
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

begin_comment
comment|/**  * Tests statistics replication for ACID tables.  */
end_comment

begin_class
specifier|public
class|class
name|TestStatsReplicationScenariosMigrationNoAutogather
extends|extends
name|TestStatsReplicationScenarios
block|{
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|classLevelSetup
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
name|overrides
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|overrides
operator|.
name|put
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|EVENT_MESSAGE_FACTORY
operator|.
name|getHiveName
argument_list|()
argument_list|,
name|GzipJSONMessageEncoder
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|replicaConfigs
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
literal|"hive.support.concurrency"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.txn.manager"
argument_list|,
literal|"org.apache.hadoop.hive.ql.lockmgr.DbTxnManager"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.metastore.client.capability.check"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.repl.bootstrap.dump.open.txn.timeout"
argument_list|,
literal|"1s"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.strict.checks.bucketing"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.mapred.mode"
argument_list|,
literal|"nonstrict"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"mapred.input.dir.recursive"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.metastore.disallow.incompatible.col.type.changes"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.strict.managed.tables"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|replicaConfigs
operator|.
name|putAll
argument_list|(
name|overrides
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|primaryConfigs
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
literal|"hive.metastore.client.capability.check"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.repl.bootstrap.dump.open.txn.timeout"
argument_list|,
literal|"1s"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.strict.checks.bucketing"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.mapred.mode"
argument_list|,
literal|"nonstrict"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"mapred.input.dir.recursive"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.metastore.disallow.incompatible.col.type.changes"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.support.concurrency"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.txn.manager"
argument_list|,
literal|"org.apache.hadoop.hive.ql.lockmgr.DummyTxnManager"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"hive.strict.managed.tables"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|primaryConfigs
operator|.
name|putAll
argument_list|(
name|overrides
argument_list|)
expr_stmt|;
name|internalBeforeClassSetup
argument_list|(
name|primaryConfigs
argument_list|,
name|replicaConfigs
argument_list|,
name|TestStatsReplicationScenariosMigrationNoAutogather
operator|.
name|class
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

