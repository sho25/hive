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
name|metastore
package|;
end_package

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
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|cli
operator|.
name|CliSessionState
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
name|metastore
operator|.
name|events
operator|.
name|ListenerEvent
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
name|security
operator|.
name|HadoopThriftAuthBridge
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_comment
comment|/**  * Ensure that the status of MetaStore events depend on the RawStore's commit status.  */
end_comment

begin_class
specifier|public
class|class
name|TestMetaStoreEventListenerOnlyOnCommit
extends|extends
name|TestCase
block|{
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|HiveMetaStoreClient
name|msc
decl_stmt|;
specifier|private
name|Driver
name|driver
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|DummyRawStoreControlledCommit
operator|.
name|setCommitSucceed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EVENT_LISTENERS
operator|.
name|varname
argument_list|,
name|DummyListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_RAW_STORE_IMPL
operator|.
name|varname
argument_list|,
name|DummyRawStoreControlledCommit
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|port
init|=
name|MetaStoreTestUtils
operator|.
name|findFreePort
argument_list|()
decl_stmt|;
name|MetaStoreTestUtils
operator|.
name|startMetaStore
argument_list|(
name|port
argument_list|,
name|HadoopThriftAuthBridge
operator|.
name|getBridge
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|,
literal|"thrift://localhost:"
operator|+
name|port
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORETHRIFTCONNECTIONRETRIES
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|msc
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|DummyListener
operator|.
name|notifyList
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testEventStatus
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|listSize
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|ListenerEvent
argument_list|>
name|notifyList
init|=
name|DummyListener
operator|.
name|notifyList
decl_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"CREATE DATABASE tmpDb"
argument_list|)
expr_stmt|;
name|listSize
operator|+=
literal|1
expr_stmt|;
name|notifyList
operator|=
name|DummyListener
operator|.
name|notifyList
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|DummyListener
operator|.
name|getLastEvent
argument_list|()
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"CREATE TABLE unittest_TestMetaStoreEventListenerOnlyOnCommit (id INT) "
operator|+
literal|"PARTITIONED BY (ds STRING)"
argument_list|)
expr_stmt|;
name|listSize
operator|+=
literal|1
expr_stmt|;
name|notifyList
operator|=
name|DummyListener
operator|.
name|notifyList
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|DummyListener
operator|.
name|getLastEvent
argument_list|()
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"ALTER TABLE unittest_TestMetaStoreEventListenerOnlyOnCommit "
operator|+
literal|"ADD PARTITION(ds='foo1')"
argument_list|)
expr_stmt|;
name|listSize
operator|+=
literal|1
expr_stmt|;
name|notifyList
operator|=
name|DummyListener
operator|.
name|notifyList
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|DummyListener
operator|.
name|getLastEvent
argument_list|()
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|DummyRawStoreControlledCommit
operator|.
name|setCommitSucceed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"ALTER TABLE unittest_TestMetaStoreEventListenerOnlyOnCommit "
operator|+
literal|"ADD PARTITION(ds='foo2')"
argument_list|)
expr_stmt|;
name|listSize
operator|+=
literal|1
expr_stmt|;
name|notifyList
operator|=
name|DummyListener
operator|.
name|notifyList
expr_stmt|;
name|assertEquals
argument_list|(
name|notifyList
operator|.
name|size
argument_list|()
argument_list|,
name|listSize
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|DummyListener
operator|.
name|getLastEvent
argument_list|()
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

