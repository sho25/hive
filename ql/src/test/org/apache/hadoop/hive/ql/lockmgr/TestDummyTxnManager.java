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
name|lockmgr
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|*
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
name|Assert
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
name|ErrorMsg
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
name|DriverState
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
name|lockmgr
operator|.
name|HiveLockObject
operator|.
name|HiveLockObjectData
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
name|lockmgr
operator|.
name|zookeeper
operator|.
name|ZooKeeperHiveLock
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
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|ArgumentCaptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|runners
operator|.
name|MockitoJUnitRunner
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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

begin_class
annotation|@
name|RunWith
argument_list|(
name|MockitoJUnitRunner
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestDummyTxnManager
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
init|=
literal|1
decl_stmt|;
annotation|@
name|Mock
name|HiveLockManager
name|mockLockManager
decl_stmt|;
annotation|@
name|Mock
name|HiveLockManagerCtx
name|mockLockManagerCtx
decl_stmt|;
annotation|@
name|Mock
name|QueryPlan
name|mockQueryPlan
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_MANAGER
argument_list|,
name|DummyTxnManager
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
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
name|DummyTxnManager
argument_list|)
expr_stmt|;
comment|// Use reflection to set LockManager since creating the object using the
comment|// relection in DummyTxnManager won't take Mocked object
name|Field
name|field
init|=
name|DummyTxnManager
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"lockMgr"
argument_list|)
decl_stmt|;
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|field
operator|.
name|set
argument_list|(
name|txnMgr
argument_list|,
name|mockLockManager
argument_list|)
expr_stmt|;
name|Field
name|field2
init|=
name|DummyTxnManager
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"lockManagerCtx"
argument_list|)
decl_stmt|;
name|field2
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|field2
operator|.
name|set
argument_list|(
name|txnMgr
argument_list|,
name|mockLockManagerCtx
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
if|if
condition|(
name|txnMgr
operator|!=
literal|null
condition|)
block|{
name|txnMgr
operator|.
name|closeTxnManager
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Verifies the current database object is not locked if the table read is against different database    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testSingleReadTable
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Setup
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|setCurrentDatabase
argument_list|(
literal|"db1"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|expectedLocks
init|=
operator|new
name|ArrayList
argument_list|<
name|HiveLock
argument_list|>
argument_list|()
decl_stmt|;
name|expectedLocks
operator|.
name|add
argument_list|(
operator|new
name|ZooKeeperHiveLock
argument_list|(
literal|"default"
argument_list|,
operator|new
name|HiveLockObject
argument_list|()
argument_list|,
name|HiveLockMode
operator|.
name|SHARED
argument_list|)
argument_list|)
expr_stmt|;
name|expectedLocks
operator|.
name|add
argument_list|(
operator|new
name|ZooKeeperHiveLock
argument_list|(
literal|"default.table1"
argument_list|,
operator|new
name|HiveLockObject
argument_list|()
argument_list|,
name|HiveLockMode
operator|.
name|SHARED
argument_list|)
argument_list|)
expr_stmt|;
name|DriverState
name|driverState
init|=
operator|new
name|DriverState
argument_list|()
decl_stmt|;
name|DriverState
name|driverInterrupted
init|=
operator|new
name|DriverState
argument_list|()
decl_stmt|;
name|driverInterrupted
operator|.
name|abort
argument_list|()
expr_stmt|;
name|LockException
name|lEx
init|=
operator|new
name|LockException
argument_list|(
name|ErrorMsg
operator|.
name|LOCK_ACQUIRE_CANCELLED
operator|.
name|getMsg
argument_list|()
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockLockManager
operator|.
name|lock
argument_list|(
name|anyListOf
argument_list|(
name|HiveLockObj
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|false
argument_list|)
argument_list|,
name|eq
argument_list|(
name|driverState
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|expectedLocks
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockLockManager
operator|.
name|lock
argument_list|(
name|anyListOf
argument_list|(
name|HiveLockObj
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|false
argument_list|)
argument_list|,
name|eq
argument_list|(
name|driverInterrupted
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenThrow
argument_list|(
name|lEx
argument_list|)
expr_stmt|;
name|doNothing
argument_list|()
operator|.
name|when
argument_list|(
name|mockLockManager
argument_list|)
operator|.
name|setContext
argument_list|(
name|any
argument_list|(
name|HiveLockManagerCtx
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|doNothing
argument_list|()
operator|.
name|when
argument_list|(
name|mockLockManager
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|ArgumentCaptor
argument_list|<
name|List
argument_list|>
name|lockObjsCaptor
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|List
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockQueryPlan
operator|.
name|getInputs
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|createReadEntities
argument_list|()
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mockQueryPlan
operator|.
name|getOutputs
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
comment|// Execute
name|txnMgr
operator|.
name|acquireLocks
argument_list|(
name|mockQueryPlan
argument_list|,
name|ctx
argument_list|,
literal|"fred"
argument_list|,
name|driverState
argument_list|)
expr_stmt|;
comment|// Verify
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"db1"
argument_list|,
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getCurrentDatabase
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLock
argument_list|>
name|resultLocks
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
name|expectedLocks
operator|.
name|size
argument_list|()
argument_list|,
name|resultLocks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedLocks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getHiveLockMode
argument_list|()
argument_list|,
name|resultLocks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getHiveLockMode
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedLocks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getHiveLockObject
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|resultLocks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getHiveLockObject
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedLocks
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getHiveLockMode
argument_list|()
argument_list|,
name|resultLocks
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getHiveLockMode
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedLocks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getHiveLockObject
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|resultLocks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getHiveLockObject
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockLockManager
argument_list|)
operator|.
name|lock
argument_list|(
name|lockObjsCaptor
operator|.
name|capture
argument_list|()
argument_list|,
name|eq
argument_list|(
literal|false
argument_list|)
argument_list|,
name|eq
argument_list|(
name|driverState
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HiveLockObj
argument_list|>
name|lockObjs
init|=
name|lockObjsCaptor
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|lockObjs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"default"
argument_list|,
name|lockObjs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HiveLockMode
operator|.
name|SHARED
argument_list|,
name|lockObjs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|mode
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"default/table1"
argument_list|,
name|lockObjs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HiveLockMode
operator|.
name|SHARED
argument_list|,
name|lockObjs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|mode
argument_list|)
expr_stmt|;
comment|// Execute
try|try
block|{
name|txnMgr
operator|.
name|acquireLocks
argument_list|(
name|mockQueryPlan
argument_list|,
name|ctx
argument_list|,
literal|"fred"
argument_list|,
name|driverInterrupted
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockException
name|le
parameter_list|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|le
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ErrorMsg
operator|.
name|LOCK_ACQUIRE_CANCELLED
operator|.
name|getMsg
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDedupLockObjects
parameter_list|()
block|{
name|List
argument_list|<
name|HiveLockObj
argument_list|>
name|lockObjs
init|=
operator|new
name|ArrayList
argument_list|<
name|HiveLockObj
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|path1
init|=
literal|"path1"
decl_stmt|;
name|String
name|path2
init|=
literal|"path2"
decl_stmt|;
name|HiveLockObjectData
name|lockData1
init|=
operator|new
name|HiveLockObjectData
argument_list|(
literal|"query1"
argument_list|,
literal|"1"
argument_list|,
literal|"IMPLICIT"
argument_list|,
literal|"drop table table1"
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|HiveLockObjectData
name|lockData2
init|=
operator|new
name|HiveLockObjectData
argument_list|(
literal|"query1"
argument_list|,
literal|"1"
argument_list|,
literal|"IMPLICIT"
argument_list|,
literal|"drop table table1"
argument_list|,
name|conf
argument_list|)
decl_stmt|;
comment|// Start with the following locks:
comment|// [path1, shared]
comment|// [path1, exclusive]
comment|// [path2, shared]
comment|// [path2, shared]
comment|// [path2, shared]
name|lockObjs
operator|.
name|add
argument_list|(
operator|new
name|HiveLockObj
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|path1
argument_list|,
name|lockData1
argument_list|)
argument_list|,
name|HiveLockMode
operator|.
name|SHARED
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|name1
init|=
name|lockObjs
operator|.
name|get
argument_list|(
name|lockObjs
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|getName
argument_list|()
decl_stmt|;
name|lockObjs
operator|.
name|add
argument_list|(
operator|new
name|HiveLockObj
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|path1
argument_list|,
name|lockData1
argument_list|)
argument_list|,
name|HiveLockMode
operator|.
name|EXCLUSIVE
argument_list|)
argument_list|)
expr_stmt|;
name|lockObjs
operator|.
name|add
argument_list|(
operator|new
name|HiveLockObj
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|path2
argument_list|,
name|lockData2
argument_list|)
argument_list|,
name|HiveLockMode
operator|.
name|SHARED
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|name2
init|=
name|lockObjs
operator|.
name|get
argument_list|(
name|lockObjs
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|getName
argument_list|()
decl_stmt|;
name|lockObjs
operator|.
name|add
argument_list|(
operator|new
name|HiveLockObj
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|path2
argument_list|,
name|lockData2
argument_list|)
argument_list|,
name|HiveLockMode
operator|.
name|SHARED
argument_list|)
argument_list|)
expr_stmt|;
name|lockObjs
operator|.
name|add
argument_list|(
operator|new
name|HiveLockObj
argument_list|(
operator|new
name|HiveLockObject
argument_list|(
name|path2
argument_list|,
name|lockData2
argument_list|)
argument_list|,
name|HiveLockMode
operator|.
name|SHARED
argument_list|)
argument_list|)
expr_stmt|;
name|DummyTxnManager
operator|.
name|dedupLockObjects
argument_list|(
name|lockObjs
argument_list|)
expr_stmt|;
comment|// After dedup we should be left with 2 locks:
comment|// [path1, exclusive]
comment|// [path2, shared]
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Locks should be deduped"
argument_list|,
literal|2
argument_list|,
name|lockObjs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Comparator
argument_list|<
name|HiveLockObj
argument_list|>
name|cmp
init|=
operator|new
name|Comparator
argument_list|<
name|HiveLockObj
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|HiveLockObj
name|lock1
parameter_list|,
name|HiveLockObj
name|lock2
parameter_list|)
block|{
return|return
name|lock1
operator|.
name|getName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|lock2
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|lockObjs
argument_list|,
name|cmp
argument_list|)
expr_stmt|;
name|HiveLockObj
name|lockObj
init|=
name|lockObjs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|name1
argument_list|,
name|lockObj
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HiveLockMode
operator|.
name|EXCLUSIVE
argument_list|,
name|lockObj
operator|.
name|getMode
argument_list|()
argument_list|)
expr_stmt|;
name|lockObj
operator|=
name|lockObjs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|name2
argument_list|,
name|lockObj
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HiveLockMode
operator|.
name|SHARED
argument_list|,
name|lockObj
operator|.
name|getMode
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|createReadEntities
parameter_list|()
block|{
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|readEntities
init|=
operator|new
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
argument_list|()
decl_stmt|;
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
return|return
name|readEntities
return|;
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
block|}
end_class

end_unit

