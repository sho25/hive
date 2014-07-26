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
name|ql
operator|.
name|lockmgr
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|Context
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
name|QueryPlan
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|metadata
operator|.
name|Partition
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
name|metadata
operator|.
name|Table
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
name|*
import|;
end_import

begin_comment
comment|/**  * Unit tests for {@link DbTxnManager}.  */
end_comment

begin_class
specifier|public
class|class
name|TestDbTxnManager
block|{
specifier|private
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
specifier|private
name|HiveTxnManager
name|txnMgr
decl_stmt|;
specifier|private
name|Context
name|ctx
decl_stmt|;
specifier|private
name|int
name|nextInput
decl_stmt|;
specifier|private
name|int
name|nextOutput
decl_stmt|;
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|readEntities
decl_stmt|;
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|writeEntities
decl_stmt|;
specifier|public
name|TestDbTxnManager
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
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ctx
operator|=
operator|new
name|Context
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
name|testSingleReadTable
parameter_list|()
throws|throws
name|Exception
block|{
name|addTableInput
argument_list|()
expr_stmt|;
name|QueryPlan
name|qp
init|=
operator|new
name|MockQueryPlan
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|txnMgr
operator|.
name|acquireLocks
argument_list|(
name|qp
argument_list|,
name|ctx
argument_list|,
literal|"fred"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|ctx
operator|.
name|getHiveLocks
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|TxnDbUtil
operator|.
name|countLockComponents
argument_list|(
operator|(
operator|(
name|DbLockManager
operator|.
name|DbHiveLock
operator|)
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|lockId
argument_list|)
argument_list|)
expr_stmt|;
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|unlock
argument_list|(
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|locks
operator|=
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|getLocks
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSingleReadPartition
parameter_list|()
throws|throws
name|Exception
block|{
name|addPartitionInput
argument_list|(
name|newTable
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|QueryPlan
name|qp
init|=
operator|new
name|MockQueryPlan
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|txnMgr
operator|.
name|acquireLocks
argument_list|(
name|qp
argument_list|,
name|ctx
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|ctx
operator|.
name|getHiveLocks
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|TxnDbUtil
operator|.
name|countLockComponents
argument_list|(
operator|(
operator|(
name|DbLockManager
operator|.
name|DbHiveLock
operator|)
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|lockId
argument_list|)
argument_list|)
expr_stmt|;
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|unlock
argument_list|(
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|locks
operator|=
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|getLocks
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSingleReadMultiPartition
parameter_list|()
throws|throws
name|Exception
block|{
name|Table
name|t
init|=
name|newTable
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|addPartitionInput
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|addPartitionInput
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|addPartitionInput
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|QueryPlan
name|qp
init|=
operator|new
name|MockQueryPlan
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|txnMgr
operator|.
name|acquireLocks
argument_list|(
name|qp
argument_list|,
name|ctx
argument_list|,
literal|"fred"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|ctx
operator|.
name|getHiveLocks
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|TxnDbUtil
operator|.
name|countLockComponents
argument_list|(
operator|(
operator|(
name|DbLockManager
operator|.
name|DbHiveLock
operator|)
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|lockId
argument_list|)
argument_list|)
expr_stmt|;
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|unlock
argument_list|(
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|locks
operator|=
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|getLocks
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testJoin
parameter_list|()
throws|throws
name|Exception
block|{
name|Table
name|t
init|=
name|newTable
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|addPartitionInput
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|addPartitionInput
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|addPartitionInput
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|addTableInput
argument_list|()
expr_stmt|;
name|QueryPlan
name|qp
init|=
operator|new
name|MockQueryPlan
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|txnMgr
operator|.
name|acquireLocks
argument_list|(
name|qp
argument_list|,
name|ctx
argument_list|,
literal|"fred"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|ctx
operator|.
name|getHiveLocks
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|TxnDbUtil
operator|.
name|countLockComponents
argument_list|(
operator|(
operator|(
name|DbLockManager
operator|.
name|DbHiveLock
operator|)
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|lockId
argument_list|)
argument_list|)
expr_stmt|;
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|unlock
argument_list|(
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|locks
operator|=
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|getLocks
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSingleWriteTable
parameter_list|()
throws|throws
name|Exception
block|{
name|WriteEntity
name|we
init|=
name|addTableOutput
argument_list|(
name|WriteEntity
operator|.
name|WriteType
operator|.
name|INSERT
argument_list|)
decl_stmt|;
name|QueryPlan
name|qp
init|=
operator|new
name|MockQueryPlan
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|txnMgr
operator|.
name|acquireLocks
argument_list|(
name|qp
argument_list|,
name|ctx
argument_list|,
literal|"fred"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|ctx
operator|.
name|getHiveLocks
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|TxnDbUtil
operator|.
name|countLockComponents
argument_list|(
operator|(
operator|(
name|DbLockManager
operator|.
name|DbHiveLock
operator|)
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|lockId
argument_list|)
argument_list|)
expr_stmt|;
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|unlock
argument_list|(
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|locks
operator|=
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|getLocks
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadWrite
parameter_list|()
throws|throws
name|Exception
block|{
name|Table
name|t
init|=
name|newTable
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|addPartitionInput
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|addPartitionInput
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|addPartitionInput
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|WriteEntity
name|we
init|=
name|addTableOutput
argument_list|(
name|WriteEntity
operator|.
name|WriteType
operator|.
name|INSERT
argument_list|)
decl_stmt|;
name|QueryPlan
name|qp
init|=
operator|new
name|MockQueryPlan
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|txnMgr
operator|.
name|acquireLocks
argument_list|(
name|qp
argument_list|,
name|ctx
argument_list|,
literal|"fred"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|ctx
operator|.
name|getHiveLocks
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|TxnDbUtil
operator|.
name|countLockComponents
argument_list|(
operator|(
operator|(
name|DbLockManager
operator|.
name|DbHiveLock
operator|)
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|lockId
argument_list|)
argument_list|)
expr_stmt|;
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|unlock
argument_list|(
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|locks
operator|=
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|getLocks
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdate
parameter_list|()
throws|throws
name|Exception
block|{
name|WriteEntity
name|we
init|=
name|addTableOutput
argument_list|(
name|WriteEntity
operator|.
name|WriteType
operator|.
name|UPDATE
argument_list|)
decl_stmt|;
name|QueryPlan
name|qp
init|=
operator|new
name|MockQueryPlan
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|txnMgr
operator|.
name|acquireLocks
argument_list|(
name|qp
argument_list|,
name|ctx
argument_list|,
literal|"fred"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|ctx
operator|.
name|getHiveLocks
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|TxnDbUtil
operator|.
name|countLockComponents
argument_list|(
operator|(
operator|(
name|DbLockManager
operator|.
name|DbHiveLock
operator|)
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|lockId
argument_list|)
argument_list|)
expr_stmt|;
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|unlock
argument_list|(
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|locks
operator|=
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|getLocks
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDelete
parameter_list|()
throws|throws
name|Exception
block|{
name|WriteEntity
name|we
init|=
name|addTableOutput
argument_list|(
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DELETE
argument_list|)
decl_stmt|;
name|QueryPlan
name|qp
init|=
operator|new
name|MockQueryPlan
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|txnMgr
operator|.
name|acquireLocks
argument_list|(
name|qp
argument_list|,
name|ctx
argument_list|,
literal|"fred"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|ctx
operator|.
name|getHiveLocks
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|TxnDbUtil
operator|.
name|countLockComponents
argument_list|(
operator|(
operator|(
name|DbLockManager
operator|.
name|DbHiveLock
operator|)
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|lockId
argument_list|)
argument_list|)
expr_stmt|;
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|unlock
argument_list|(
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|locks
operator|=
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|getLocks
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDDLExclusive
parameter_list|()
throws|throws
name|Exception
block|{
name|WriteEntity
name|we
init|=
name|addTableOutput
argument_list|(
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DDL_EXCLUSIVE
argument_list|)
decl_stmt|;
name|QueryPlan
name|qp
init|=
operator|new
name|MockQueryPlan
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|txnMgr
operator|.
name|acquireLocks
argument_list|(
name|qp
argument_list|,
name|ctx
argument_list|,
literal|"fred"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|ctx
operator|.
name|getHiveLocks
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|TxnDbUtil
operator|.
name|countLockComponents
argument_list|(
operator|(
operator|(
name|DbLockManager
operator|.
name|DbHiveLock
operator|)
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|lockId
argument_list|)
argument_list|)
expr_stmt|;
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|unlock
argument_list|(
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|locks
operator|=
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|getLocks
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDDLShared
parameter_list|()
throws|throws
name|Exception
block|{
name|WriteEntity
name|we
init|=
name|addTableOutput
argument_list|(
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DDL_SHARED
argument_list|)
decl_stmt|;
name|QueryPlan
name|qp
init|=
operator|new
name|MockQueryPlan
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|txnMgr
operator|.
name|acquireLocks
argument_list|(
name|qp
argument_list|,
name|ctx
argument_list|,
literal|"fred"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|ctx
operator|.
name|getHiveLocks
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|TxnDbUtil
operator|.
name|countLockComponents
argument_list|(
operator|(
operator|(
name|DbLockManager
operator|.
name|DbHiveLock
operator|)
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|lockId
argument_list|)
argument_list|)
expr_stmt|;
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|unlock
argument_list|(
name|locks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|locks
operator|=
name|txnMgr
operator|.
name|getLockManager
argument_list|()
operator|.
name|getLocks
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|locks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDDLNoLock
parameter_list|()
throws|throws
name|Exception
block|{
name|WriteEntity
name|we
init|=
name|addTableOutput
argument_list|(
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DDL_NO_LOCK
argument_list|)
decl_stmt|;
name|QueryPlan
name|qp
init|=
operator|new
name|MockQueryPlan
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|txnMgr
operator|.
name|acquireLocks
argument_list|(
name|qp
argument_list|,
name|ctx
argument_list|,
literal|"fred"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|locks
init|=
name|ctx
operator|.
name|getHiveLocks
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|locks
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
name|txnMgr
operator|=
name|TxnManagerFactory
operator|.
name|getTxnManagerFactory
argument_list|()
operator|.
name|getTxnManager
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|txnMgr
operator|instanceof
name|DbTxnManager
argument_list|)
expr_stmt|;
name|nextInput
operator|=
literal|1
expr_stmt|;
name|nextOutput
operator|=
literal|1
expr_stmt|;
name|readEntities
operator|=
operator|new
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
argument_list|()
expr_stmt|;
name|writeEntities
operator|=
operator|new
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
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
name|TxnDbUtil
operator|.
name|cleanDb
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|MockQueryPlan
extends|extends
name|QueryPlan
block|{
specifier|private
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
decl_stmt|;
specifier|private
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
decl_stmt|;
name|MockQueryPlan
parameter_list|(
name|TestDbTxnManager
name|test
parameter_list|)
block|{
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|r
init|=
name|test
operator|.
name|readEntities
decl_stmt|;
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|w
init|=
name|test
operator|.
name|writeEntities
decl_stmt|;
name|inputs
operator|=
operator|(
name|r
operator|==
literal|null
operator|)
condition|?
operator|new
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
argument_list|()
else|:
name|r
expr_stmt|;
name|outputs
operator|=
operator|(
name|w
operator|==
literal|null
operator|)
condition|?
operator|new
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
argument_list|()
else|:
name|w
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|getInputs
parameter_list|()
block|{
return|return
name|inputs
return|;
block|}
annotation|@
name|Override
specifier|public
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|getOutputs
parameter_list|()
block|{
return|return
name|outputs
return|;
block|}
block|}
specifier|private
name|Table
name|newTable
parameter_list|(
name|boolean
name|isPartitioned
parameter_list|)
block|{
name|Table
name|t
init|=
operator|new
name|Table
argument_list|(
literal|"default"
argument_list|,
literal|"table"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|nextInput
operator|++
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|isPartitioned
condition|)
block|{
name|FieldSchema
name|fs
init|=
operator|new
name|FieldSchema
argument_list|()
decl_stmt|;
name|fs
operator|.
name|setName
argument_list|(
literal|"version"
argument_list|)
expr_stmt|;
name|fs
operator|.
name|setType
argument_list|(
literal|"String"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|partCols
operator|.
name|add
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|t
operator|.
name|setPartCols
argument_list|(
name|partCols
argument_list|)
expr_stmt|;
block|}
return|return
name|t
return|;
block|}
specifier|private
name|void
name|addTableInput
parameter_list|()
block|{
name|ReadEntity
name|re
init|=
operator|new
name|ReadEntity
argument_list|(
name|newTable
argument_list|(
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|readEntities
operator|.
name|add
argument_list|(
name|re
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addPartitionInput
parameter_list|(
name|Table
name|t
parameter_list|)
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
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
name|partSpec
operator|.
name|put
argument_list|(
literal|"version"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|nextInput
operator|++
argument_list|)
argument_list|)
expr_stmt|;
name|Partition
name|p
init|=
operator|new
name|Partition
argument_list|(
name|t
argument_list|,
name|partSpec
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/dev/null"
argument_list|)
argument_list|)
decl_stmt|;
name|ReadEntity
name|re
init|=
operator|new
name|ReadEntity
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|readEntities
operator|.
name|add
argument_list|(
name|re
argument_list|)
expr_stmt|;
block|}
specifier|private
name|WriteEntity
name|addTableOutput
parameter_list|(
name|WriteEntity
operator|.
name|WriteType
name|writeType
parameter_list|)
block|{
name|WriteEntity
name|we
init|=
operator|new
name|WriteEntity
argument_list|(
name|newTable
argument_list|(
literal|false
argument_list|)
argument_list|,
name|writeType
argument_list|)
decl_stmt|;
name|writeEntities
operator|.
name|add
argument_list|(
name|we
argument_list|)
expr_stmt|;
return|return
name|we
return|;
block|}
specifier|private
name|WriteEntity
name|addPartitionOutput
parameter_list|(
name|Table
name|t
parameter_list|,
name|WriteEntity
operator|.
name|WriteType
name|writeType
parameter_list|)
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
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
name|partSpec
operator|.
name|put
argument_list|(
literal|"version"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|nextInput
operator|++
argument_list|)
argument_list|)
expr_stmt|;
name|Partition
name|p
init|=
operator|new
name|Partition
argument_list|(
name|t
argument_list|,
name|partSpec
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/dev/null"
argument_list|)
argument_list|)
decl_stmt|;
name|WriteEntity
name|we
init|=
operator|new
name|WriteEntity
argument_list|(
name|p
argument_list|,
name|writeType
argument_list|)
decl_stmt|;
name|writeEntities
operator|.
name|add
argument_list|(
name|we
argument_list|)
expr_stmt|;
return|return
name|we
return|;
block|}
block|}
end_class

end_unit

