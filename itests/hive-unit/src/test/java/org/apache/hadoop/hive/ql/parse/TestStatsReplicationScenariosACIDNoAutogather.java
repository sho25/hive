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
name|Ignore
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
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
annotation|@
name|Ignore
argument_list|(
literal|"HIVE-22626 should fix this"
argument_list|)
specifier|public
class|class
name|TestStatsReplicationScenariosACIDNoAutogather
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
name|overrides
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|overrides
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_MANAGER
operator|.
name|varname
argument_list|,
literal|"org.apache.hadoop.hive.ql.lockmgr.DbTxnManager"
argument_list|)
expr_stmt|;
name|overrides
operator|.
name|put
argument_list|(
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CAPABILITY_CHECK
operator|.
name|getHiveName
argument_list|()
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|overrides
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPL_BOOTSTRAP_DUMP_OPEN_TXN_TIMEOUT
operator|.
name|varname
argument_list|,
literal|"1s"
argument_list|)
expr_stmt|;
name|internalBeforeClassSetup
argument_list|(
name|overrides
argument_list|,
name|overrides
argument_list|,
name|TestStatsReplicationScenariosACIDNoAutogather
operator|.
name|class
argument_list|,
literal|false
argument_list|,
name|AcidTableKind
operator|.
name|FULL_ACID
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

