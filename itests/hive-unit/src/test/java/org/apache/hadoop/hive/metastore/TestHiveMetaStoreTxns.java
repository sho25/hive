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
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|common
operator|.
name|ValidTxnList
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
name|common
operator|.
name|ValidReadTxnList
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
name|api
operator|.
name|DataOperationType
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
name|HeartbeatTxnRangeResponse
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
name|LockResponse
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
name|LockState
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
name|txn
operator|.
name|TxnDbUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|LogManager
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
name|Test
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

begin_comment
comment|/**  * Unit tests for {@link org.apache.hadoop.hive.metastore.HiveMetaStoreClient}.  For now this just has  * transaction and locking tests.  The goal here is not to test all  * functionality possible through the interface, as all permutations of DB  * operations should be tested in the appropriate DB handler classes.  The  * goal is to test that we can properly pass the messages through the thrift  * service.  *  * This is in the ql directory rather than the metastore directory because it  * required the hive-exec jar, and hive-exec jar already depends on  * hive-metastore jar, thus I can't make hive-metastore depend on hive-exec.  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveMetaStoreTxns
block|{
specifier|private
specifier|final
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
specifier|private
name|IMetaStoreClient
name|client
decl_stmt|;
specifier|public
name|TestHiveMetaStoreTxns
parameter_list|()
throws|throws
name|Exception
block|{
name|TxnDbUtil
operator|.
name|setConfValues
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|LogManager
operator|.
name|getRootLogger
argument_list|()
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|DEBUG
argument_list|)
expr_stmt|;
name|tearDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTxns
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Long
argument_list|>
name|tids
init|=
name|client
operator|.
name|openTxns
argument_list|(
literal|"me"
argument_list|,
literal|3
argument_list|)
operator|.
name|getTxn_ids
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1L
argument_list|,
operator|(
name|long
operator|)
name|tids
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2L
argument_list|,
operator|(
name|long
operator|)
name|tids
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3L
argument_list|,
operator|(
name|long
operator|)
name|tids
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|rollbackTxn
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|client
operator|.
name|commitTxn
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|ValidTxnList
name|validTxns
init|=
name|client
operator|.
name|getValidTxns
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|validTxns
operator|.
name|isTxnValid
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|validTxns
operator|.
name|isTxnValid
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|validTxns
operator|.
name|isTxnValid
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|validTxns
operator|.
name|isTxnValid
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOpenTxnNotExcluded
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Long
argument_list|>
name|tids
init|=
name|client
operator|.
name|openTxns
argument_list|(
literal|"me"
argument_list|,
literal|3
argument_list|)
operator|.
name|getTxn_ids
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1L
argument_list|,
operator|(
name|long
operator|)
name|tids
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2L
argument_list|,
operator|(
name|long
operator|)
name|tids
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3L
argument_list|,
operator|(
name|long
operator|)
name|tids
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|rollbackTxn
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|client
operator|.
name|commitTxn
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|ValidTxnList
name|validTxns
init|=
name|client
operator|.
name|getValidTxns
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|validTxns
operator|.
name|isTxnValid
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|validTxns
operator|.
name|isTxnValid
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|validTxns
operator|.
name|isTxnValid
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|validTxns
operator|.
name|isTxnValid
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTxnRange
parameter_list|()
throws|throws
name|Exception
block|{
name|ValidTxnList
name|validTxns
init|=
name|client
operator|.
name|getValidTxns
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|NONE
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|1L
argument_list|,
literal|3L
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|tids
init|=
name|client
operator|.
name|openTxns
argument_list|(
literal|"me"
argument_list|,
literal|5
argument_list|)
operator|.
name|getTxn_ids
argument_list|()
decl_stmt|;
name|HeartbeatTxnRangeResponse
name|rsp
init|=
name|client
operator|.
name|heartbeatTxnRange
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getNosuch
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rsp
operator|.
name|getAborted
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|rollbackTxn
argument_list|(
literal|1L
argument_list|)
expr_stmt|;
name|client
operator|.
name|commitTxn
argument_list|(
literal|2L
argument_list|)
expr_stmt|;
name|client
operator|.
name|commitTxn
argument_list|(
literal|3L
argument_list|)
expr_stmt|;
name|client
operator|.
name|commitTxn
argument_list|(
literal|4L
argument_list|)
expr_stmt|;
name|validTxns
operator|=
name|client
operator|.
name|getValidTxns
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"validTxns = "
operator|+
name|validTxns
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|ALL
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|2L
argument_list|,
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|ALL
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|2L
argument_list|,
literal|3L
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|ALL
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|2L
argument_list|,
literal|4L
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|ALL
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|3L
argument_list|,
literal|4L
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|SOME
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|1L
argument_list|,
literal|4L
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|SOME
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|2L
argument_list|,
literal|5L
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|SOME
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|1L
argument_list|,
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|SOME
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|4L
argument_list|,
literal|5L
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|NONE
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|1L
argument_list|,
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|NONE
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|5L
argument_list|,
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
name|validTxns
operator|=
operator|new
name|ValidReadTxnList
argument_list|(
literal|"10:5:4:5:6"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|NONE
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|4
argument_list|,
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|ALL
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|7
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|SOME
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|7
argument_list|,
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|SOME
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|3
argument_list|,
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|SOME
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|4
argument_list|,
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|SOME
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|1
argument_list|,
literal|12
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidTxnList
operator|.
name|RangeResponse
operator|.
name|ALL
argument_list|,
name|validTxns
operator|.
name|isTxnRangeValid
argument_list|(
literal|1
argument_list|,
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLocks
parameter_list|()
throws|throws
name|Exception
block|{
name|LockRequestBuilder
name|rqstBuilder
init|=
operator|new
name|LockRequestBuilder
argument_list|()
decl_stmt|;
name|rqstBuilder
operator|.
name|addLockComponent
argument_list|(
operator|new
name|LockComponentBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
literal|"mydb"
argument_list|)
operator|.
name|setTableName
argument_list|(
literal|"mytable"
argument_list|)
operator|.
name|setPartitionName
argument_list|(
literal|"mypartition"
argument_list|)
operator|.
name|setExclusive
argument_list|()
operator|.
name|setOperationType
argument_list|(
name|DataOperationType
operator|.
name|NO_TXN
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|rqstBuilder
operator|.
name|addLockComponent
argument_list|(
operator|new
name|LockComponentBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
literal|"mydb"
argument_list|)
operator|.
name|setTableName
argument_list|(
literal|"yourtable"
argument_list|)
operator|.
name|setSemiShared
argument_list|()
operator|.
name|setOperationType
argument_list|(
name|DataOperationType
operator|.
name|NO_TXN
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|rqstBuilder
operator|.
name|addLockComponent
argument_list|(
operator|new
name|LockComponentBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
literal|"yourdb"
argument_list|)
operator|.
name|setOperationType
argument_list|(
name|DataOperationType
operator|.
name|NO_TXN
argument_list|)
operator|.
name|setShared
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|rqstBuilder
operator|.
name|setUser
argument_list|(
literal|"fred"
argument_list|)
expr_stmt|;
name|LockResponse
name|res
init|=
name|client
operator|.
name|lock
argument_list|(
name|rqstBuilder
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|res
operator|.
name|getLockid
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|LockState
operator|.
name|ACQUIRED
argument_list|,
name|res
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|res
operator|=
name|client
operator|.
name|checkLock
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|res
operator|.
name|getLockid
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|LockState
operator|.
name|ACQUIRED
argument_list|,
name|res
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|heartbeat
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|client
operator|.
name|unlock
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLocksWithTxn
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|txnid
init|=
name|client
operator|.
name|openTxn
argument_list|(
literal|"me"
argument_list|)
decl_stmt|;
name|LockRequestBuilder
name|rqstBuilder
init|=
operator|new
name|LockRequestBuilder
argument_list|()
decl_stmt|;
name|rqstBuilder
operator|.
name|setTransactionId
argument_list|(
name|txnid
argument_list|)
operator|.
name|addLockComponent
argument_list|(
operator|new
name|LockComponentBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
literal|"mydb"
argument_list|)
operator|.
name|setTableName
argument_list|(
literal|"mytable"
argument_list|)
operator|.
name|setPartitionName
argument_list|(
literal|"mypartition"
argument_list|)
operator|.
name|setSemiShared
argument_list|()
operator|.
name|setOperationType
argument_list|(
name|DataOperationType
operator|.
name|UPDATE
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|addLockComponent
argument_list|(
operator|new
name|LockComponentBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
literal|"mydb"
argument_list|)
operator|.
name|setTableName
argument_list|(
literal|"yourtable"
argument_list|)
operator|.
name|setSemiShared
argument_list|()
operator|.
name|setOperationType
argument_list|(
name|DataOperationType
operator|.
name|UPDATE
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|addLockComponent
argument_list|(
operator|new
name|LockComponentBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
literal|"yourdb"
argument_list|)
operator|.
name|setShared
argument_list|()
operator|.
name|setOperationType
argument_list|(
name|DataOperationType
operator|.
name|SELECT
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|setUser
argument_list|(
literal|"fred"
argument_list|)
expr_stmt|;
name|LockResponse
name|res
init|=
name|client
operator|.
name|lock
argument_list|(
name|rqstBuilder
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|res
operator|.
name|getLockid
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|LockState
operator|.
name|ACQUIRED
argument_list|,
name|res
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|res
operator|=
name|client
operator|.
name|checkLock
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|res
operator|.
name|getLockid
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|LockState
operator|.
name|ACQUIRED
argument_list|,
name|res
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|heartbeat
argument_list|(
name|txnid
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|client
operator|.
name|commitTxn
argument_list|(
name|txnid
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|stringifyValidTxns
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Test with just high water mark
name|ValidTxnList
name|validTxns
init|=
operator|new
name|ValidReadTxnList
argument_list|(
literal|"1:"
operator|+
name|Long
operator|.
name|MAX_VALUE
operator|+
literal|":"
argument_list|)
decl_stmt|;
name|String
name|asString
init|=
name|validTxns
operator|.
name|toString
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"1:"
operator|+
name|Long
operator|.
name|MAX_VALUE
operator|+
literal|":"
argument_list|,
name|asString
argument_list|)
expr_stmt|;
name|validTxns
operator|=
operator|new
name|ValidReadTxnList
argument_list|(
name|asString
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|validTxns
operator|.
name|getHighWatermark
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|validTxns
operator|.
name|getInvalidTransactions
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|validTxns
operator|.
name|getInvalidTransactions
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|asString
operator|=
name|validTxns
operator|.
name|toString
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"1:"
operator|+
name|Long
operator|.
name|MAX_VALUE
operator|+
literal|":"
argument_list|,
name|asString
argument_list|)
expr_stmt|;
name|validTxns
operator|=
operator|new
name|ValidReadTxnList
argument_list|(
name|asString
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|validTxns
operator|.
name|getHighWatermark
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|validTxns
operator|.
name|getInvalidTransactions
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|validTxns
operator|.
name|getInvalidTransactions
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Test with open transactions
name|validTxns
operator|=
operator|new
name|ValidReadTxnList
argument_list|(
literal|"10:3:5:3"
argument_list|)
expr_stmt|;
name|asString
operator|=
name|validTxns
operator|.
name|toString
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|asString
operator|.
name|equals
argument_list|(
literal|"10:3:3:5"
argument_list|)
operator|&&
operator|!
name|asString
operator|.
name|equals
argument_list|(
literal|"10:3:5:3"
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Unexpected string value "
operator|+
name|asString
argument_list|)
expr_stmt|;
block|}
name|validTxns
operator|=
operator|new
name|ValidReadTxnList
argument_list|(
name|asString
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|validTxns
operator|.
name|getHighWatermark
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|validTxns
operator|.
name|getInvalidTransactions
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|validTxns
operator|.
name|getInvalidTransactions
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|boolean
name|sawThree
init|=
literal|false
decl_stmt|,
name|sawFive
init|=
literal|false
decl_stmt|;
for|for
control|(
name|long
name|tid
range|:
name|validTxns
operator|.
name|getInvalidTransactions
argument_list|()
control|)
block|{
if|if
condition|(
name|tid
operator|==
literal|3
condition|)
name|sawThree
operator|=
literal|true
expr_stmt|;
elseif|else
if|if
condition|(
name|tid
operator|==
literal|5
condition|)
name|sawFive
operator|=
literal|true
expr_stmt|;
else|else
name|Assert
operator|.
name|fail
argument_list|(
literal|"Unexpected value "
operator|+
name|tid
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
name|sawThree
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|sawFive
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
name|TxnDbUtil
operator|.
name|prepDb
argument_list|()
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
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|TxnDbUtil
operator|.
name|cleanDb
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

