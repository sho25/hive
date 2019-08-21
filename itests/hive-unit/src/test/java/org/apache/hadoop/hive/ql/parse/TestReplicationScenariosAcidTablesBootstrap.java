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
name|api
operator|.
name|CurrentNotificationEventId
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
name|TxnStore
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
name|TxnUtils
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
name|InjectableBehaviourObjectStore
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
name|InjectableBehaviourObjectStore
operator|.
name|CallerArguments
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
name|InjectableBehaviourObjectStore
operator|.
name|BehaviourInjection
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
name|exec
operator|.
name|repl
operator|.
name|util
operator|.
name|ReplUtils
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
name|Test
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
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|LinkedList
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
name|Map
import|;
end_import

begin_comment
comment|/**  * TestReplicationScenariosAcidTables - test bootstrap of ACID tables during an incremental.  */
end_comment

begin_class
specifier|public
class|class
name|TestReplicationScenariosAcidTablesBootstrap
extends|extends
name|BaseReplicationScenariosAcidTables
block|{
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|dumpWithoutAcidClause
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"'"
operator|+
name|ReplUtils
operator|.
name|REPL_DUMP_INCLUDE_ACID_TABLES
operator|+
literal|"'='false'"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|dumpWithAcidBootstrapClause
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"'"
operator|+
name|ReplUtils
operator|.
name|REPL_DUMP_INCLUDE_ACID_TABLES
operator|+
literal|"'='true'"
argument_list|,
literal|"'"
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPL_BOOTSTRAP_ACID_TABLES
operator|+
literal|"'='true'"
argument_list|)
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
name|internalBeforeClassSetup
argument_list|(
name|overrides
argument_list|,
name|TestReplicationScenariosAcidTablesBootstrap
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAcidTablesBootstrapDuringIncremental
parameter_list|()
throws|throws
name|Throwable
block|{
comment|// Take a bootstrap dump without acid tables
name|WarehouseInstance
operator|.
name|Tuple
name|bootstrapDump
init|=
name|prepareDataAndDump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|,
name|dumpWithoutAcidClause
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": loading dump without acid tables."
argument_list|)
expr_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|bootstrapDump
operator|.
name|dumpLocation
argument_list|)
expr_stmt|;
name|verifyLoadExecution
argument_list|(
name|replicatedDbName
argument_list|,
name|bootstrapDump
operator|.
name|lastReplicationId
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Take a incremental dump with acid table bootstrap
name|prepareIncAcidData
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
name|prepareIncNonAcidData
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": incremental dump and load dump with acid table bootstrap."
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|incrementalDump
init|=
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
name|bootstrapDump
operator|.
name|lastReplicationId
argument_list|,
name|dumpWithAcidBootstrapClause
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|incrementalDump
operator|.
name|dumpLocation
argument_list|)
expr_stmt|;
name|verifyIncLoad
argument_list|(
name|replicatedDbName
argument_list|,
name|incrementalDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
comment|// Ckpt should be set on bootstrapped tables.
name|replica
operator|.
name|verifyIfCkptSetForTables
argument_list|(
name|replicatedDbName
argument_list|,
name|acidTableNames
argument_list|,
name|incrementalDump
operator|.
name|dumpLocation
argument_list|)
expr_stmt|;
comment|// Take a second normal incremental dump after Acid table boostrap
name|prepareInc2AcidData
argument_list|(
name|primaryDbName
argument_list|,
name|primary
operator|.
name|hiveConf
argument_list|)
expr_stmt|;
name|prepareInc2NonAcidData
argument_list|(
name|primaryDbName
argument_list|,
name|primary
operator|.
name|hiveConf
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": second incremental dump and load dump after incremental with acid table "
operator|+
literal|"bootstrap."
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|inc2Dump
init|=
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
name|incrementalDump
operator|.
name|lastReplicationId
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|inc2Dump
operator|.
name|dumpLocation
argument_list|)
expr_stmt|;
name|verifyInc2Load
argument_list|(
name|replicatedDbName
argument_list|,
name|inc2Dump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRetryAcidTablesBootstrapFromDifferentDump
parameter_list|()
throws|throws
name|Throwable
block|{
name|WarehouseInstance
operator|.
name|Tuple
name|bootstrapDump
init|=
name|prepareDataAndDump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|,
name|dumpWithoutAcidClause
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": loading dump without acid tables."
argument_list|)
expr_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|bootstrapDump
operator|.
name|dumpLocation
argument_list|)
expr_stmt|;
name|verifyLoadExecution
argument_list|(
name|replicatedDbName
argument_list|,
name|bootstrapDump
operator|.
name|lastReplicationId
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|prepareIncAcidData
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
name|prepareIncNonAcidData
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": first incremental dump with acid table bootstrap."
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|incDump
init|=
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
name|bootstrapDump
operator|.
name|lastReplicationId
argument_list|,
name|dumpWithAcidBootstrapClause
argument_list|)
decl_stmt|;
comment|// Fail setting ckpt property for table t5 but success for earlier tables
name|BehaviourInjection
argument_list|<
name|CallerArguments
argument_list|,
name|Boolean
argument_list|>
name|callerVerifier
init|=
operator|new
name|BehaviourInjection
argument_list|<
name|CallerArguments
argument_list|,
name|Boolean
argument_list|>
argument_list|()
block|{
annotation|@
name|Nullable
annotation|@
name|Override
specifier|public
name|Boolean
name|apply
parameter_list|(
annotation|@
name|Nullable
name|CallerArguments
name|args
parameter_list|)
block|{
if|if
condition|(
name|args
operator|.
name|tblName
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"t5"
argument_list|)
operator|&&
name|args
operator|.
name|dbName
operator|.
name|equalsIgnoreCase
argument_list|(
name|replicatedDbName
argument_list|)
condition|)
block|{
name|injectionPathCalled
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Verifier - DB : "
operator|+
name|args
operator|.
name|dbName
operator|+
literal|" TABLE : "
operator|+
name|args
operator|.
name|tblName
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
decl_stmt|;
comment|// Fail repl load before the ckpt property is set for t4 and after it is set for t2.
comment|// In the retry, these half baked tables should be dropped and bootstrap should be successful.
name|InjectableBehaviourObjectStore
operator|.
name|setAlterTableModifier
argument_list|(
name|callerVerifier
argument_list|)
expr_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": loading first incremental dump with acid table bootstrap (will fail)"
argument_list|)
expr_stmt|;
name|replica
operator|.
name|loadFailure
argument_list|(
name|replicatedDbName
argument_list|,
name|incDump
operator|.
name|dumpLocation
argument_list|)
expr_stmt|;
name|callerVerifier
operator|.
name|assertInjectionsPerformed
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|InjectableBehaviourObjectStore
operator|.
name|resetAlterTableModifier
argument_list|()
expr_stmt|;
block|}
name|prepareInc2AcidData
argument_list|(
name|primaryDbName
argument_list|,
name|primary
operator|.
name|hiveConf
argument_list|)
expr_stmt|;
name|prepareInc2NonAcidData
argument_list|(
name|primaryDbName
argument_list|,
name|primary
operator|.
name|hiveConf
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": second incremental dump with acid table bootstrap"
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|inc2Dump
init|=
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
name|bootstrapDump
operator|.
name|lastReplicationId
argument_list|,
name|dumpWithAcidBootstrapClause
argument_list|)
decl_stmt|;
comment|// Set incorrect bootstrap dump to clean tables. Here, used the full bootstrap dump which is invalid.
comment|// So, REPL LOAD fails.
name|List
argument_list|<
name|String
argument_list|>
name|loadWithClause
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"'"
operator|+
name|ReplUtils
operator|.
name|REPL_CLEAN_TABLES_FROM_BOOTSTRAP_CONFIG
operator|+
literal|"'='"
operator|+
name|bootstrapDump
operator|.
name|dumpLocation
operator|+
literal|"'"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": trying to load second incremental dump with wrong bootstrap dump "
operator|+
literal|" specified for cleaning ACID tables. Should fail."
argument_list|)
expr_stmt|;
name|replica
operator|.
name|loadFailure
argument_list|(
name|replicatedDbName
argument_list|,
name|inc2Dump
operator|.
name|dumpLocation
argument_list|,
name|loadWithClause
argument_list|)
expr_stmt|;
comment|// Set previously failed bootstrap dump to clean-up. Now, new bootstrap should overwrite the old one.
name|loadWithClause
operator|=
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"'"
operator|+
name|ReplUtils
operator|.
name|REPL_CLEAN_TABLES_FROM_BOOTSTRAP_CONFIG
operator|+
literal|"'='"
operator|+
name|incDump
operator|.
name|dumpLocation
operator|+
literal|"'"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": trying to load second incremental dump with correct bootstrap dump "
operator|+
literal|"specified for cleaning ACID tables. Should succeed."
argument_list|)
expr_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|inc2Dump
operator|.
name|dumpLocation
argument_list|,
name|loadWithClause
argument_list|)
expr_stmt|;
name|verifyInc2Load
argument_list|(
name|replicatedDbName
argument_list|,
name|inc2Dump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
comment|// Once the REPL LOAD is successful, the this config should be unset or else, the subsequent REPL LOAD
comment|// will also drop those tables which will cause data loss.
name|loadWithClause
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
comment|// Verify if bootstrapping with same dump is idempotent and return same result
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": trying to load second incremental dump (with acid bootstrap) again."
operator|+
literal|" Should succeed."
argument_list|)
expr_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|inc2Dump
operator|.
name|dumpLocation
argument_list|,
name|loadWithClause
argument_list|)
expr_stmt|;
name|verifyInc2Load
argument_list|(
name|replicatedDbName
argument_list|,
name|inc2Dump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|retryIncBootstrapAcidFromDifferentDumpWithoutCleanTablesConfig
parameter_list|()
throws|throws
name|Throwable
block|{
name|WarehouseInstance
operator|.
name|Tuple
name|bootstrapDump
init|=
name|prepareDataAndDump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|,
name|dumpWithoutAcidClause
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|bootstrapDump
operator|.
name|dumpLocation
argument_list|)
expr_stmt|;
name|prepareIncAcidData
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
name|prepareIncNonAcidData
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|incDump
init|=
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
name|bootstrapDump
operator|.
name|lastReplicationId
argument_list|,
name|dumpWithAcidBootstrapClause
argument_list|)
decl_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|inc2Dump
init|=
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
name|bootstrapDump
operator|.
name|lastReplicationId
argument_list|,
name|dumpWithAcidBootstrapClause
argument_list|)
decl_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|incDump
operator|.
name|dumpLocation
argument_list|)
expr_stmt|;
comment|// Re-bootstrapping from different bootstrap dump without clean tables config should fail.
name|replica
operator|.
name|loadFailure
argument_list|(
name|replicatedDbName
argument_list|,
name|inc2Dump
operator|.
name|dumpLocation
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|,
name|ErrorMsg
operator|.
name|REPL_BOOTSTRAP_LOAD_PATH_NOT_VALID
operator|.
name|getErrorCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAcidTablesBootstrapDuringIncrementalWithOpenTxnsTimeout
parameter_list|()
throws|throws
name|Throwable
block|{
comment|// Take a dump without ACID tables
name|WarehouseInstance
operator|.
name|Tuple
name|bootstrapDump
init|=
name|prepareDataAndDump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|,
name|dumpWithoutAcidClause
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": loading dump without acid tables."
argument_list|)
expr_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|bootstrapDump
operator|.
name|dumpLocation
argument_list|)
expr_stmt|;
comment|// Open concurrent transactions, create data for incremental and take an incremental dump
comment|// with ACID table bootstrap.
name|int
name|numTxns
init|=
literal|5
decl_stmt|;
name|HiveConf
name|primaryConf
init|=
name|primary
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|TxnStore
name|txnHandler
init|=
name|TxnUtils
operator|.
name|getTxnStore
argument_list|(
name|primary
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
comment|// Open 5 txns
name|List
argument_list|<
name|Long
argument_list|>
name|txns
init|=
name|openTxns
argument_list|(
name|numTxns
argument_list|,
name|txnHandler
argument_list|,
name|primaryConf
argument_list|)
decl_stmt|;
name|prepareIncNonAcidData
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
name|prepareIncAcidData
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
comment|// Allocate write ids for tables t1 and t2 for all txns
comment|// t1=5+2(insert) and t2=5+5(insert, alter add column)
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|tables
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|tables
operator|.
name|put
argument_list|(
literal|"t1"
argument_list|,
name|numTxns
operator|+
literal|2L
argument_list|)
expr_stmt|;
name|tables
operator|.
name|put
argument_list|(
literal|"t2"
argument_list|,
name|numTxns
operator|+
literal|5L
argument_list|)
expr_stmt|;
name|allocateWriteIdsForTables
argument_list|(
name|primaryDbName
argument_list|,
name|tables
argument_list|,
name|txnHandler
argument_list|,
name|txns
argument_list|,
name|primaryConf
argument_list|)
expr_stmt|;
comment|// Bootstrap dump with open txn timeout as 1s.
name|List
argument_list|<
name|String
argument_list|>
name|withConfigs
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|(
name|dumpWithAcidBootstrapClause
argument_list|)
decl_stmt|;
name|withConfigs
operator|.
name|add
argument_list|(
literal|"'hive.repl.bootstrap.dump.open.txn.timeout'='1s'"
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|incDump
init|=
name|primary
operator|.
name|run
argument_list|(
literal|"use "
operator|+
name|primaryDbName
argument_list|)
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
name|bootstrapDump
operator|.
name|lastReplicationId
argument_list|,
name|withConfigs
argument_list|)
decl_stmt|;
comment|// After bootstrap dump, all the opened txns should be aborted. Verify it.
name|verifyAllOpenTxnsAborted
argument_list|(
name|txns
argument_list|,
name|primaryConf
argument_list|)
expr_stmt|;
name|verifyNextId
argument_list|(
name|tables
argument_list|,
name|primaryDbName
argument_list|,
name|primaryConf
argument_list|)
expr_stmt|;
comment|// Incremental load with ACID bootstrap should also replicate the aborted write ids on
comment|// tables t1 and t2
name|HiveConf
name|replicaConf
init|=
name|replica
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": loading incremental dump with ACID bootstrap."
argument_list|)
expr_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|incDump
operator|.
name|dumpLocation
argument_list|)
expr_stmt|;
name|verifyIncLoad
argument_list|(
name|replicatedDbName
argument_list|,
name|incDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
comment|// Verify if HWM is properly set after REPL LOAD
name|verifyNextId
argument_list|(
name|tables
argument_list|,
name|replicatedDbName
argument_list|,
name|replicaConf
argument_list|)
expr_stmt|;
comment|// Verify if all the aborted write ids are replicated to the replicated DB
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|tables
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|entry
operator|.
name|setValue
argument_list|(
operator|(
name|long
operator|)
name|numTxns
argument_list|)
expr_stmt|;
block|}
name|verifyWriteIdsForTables
argument_list|(
name|tables
argument_list|,
name|replicaConf
argument_list|,
name|replicatedDbName
argument_list|)
expr_stmt|;
comment|// Verify if entries added in COMPACTION_QUEUE for each table/partition
comment|// t1-> 1 entry and t2-> 2 entries (1 per partition)
name|tables
operator|.
name|clear
argument_list|()
expr_stmt|;
name|tables
operator|.
name|put
argument_list|(
literal|"t1"
argument_list|,
literal|1L
argument_list|)
expr_stmt|;
name|tables
operator|.
name|put
argument_list|(
literal|"t2"
argument_list|,
literal|4L
argument_list|)
expr_stmt|;
name|verifyCompactionQueue
argument_list|(
name|tables
argument_list|,
name|replicatedDbName
argument_list|,
name|replicaConf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBootstrapAcidTablesDuringIncrementalWithConcurrentWrites
parameter_list|()
throws|throws
name|Throwable
block|{
comment|// Dump and load bootstrap without ACID tables.
name|WarehouseInstance
operator|.
name|Tuple
name|bootstrapDump
init|=
name|prepareDataAndDump
argument_list|(
name|primaryDbName
argument_list|,
literal|null
argument_list|,
name|dumpWithoutAcidClause
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": loading dump without acid tables."
argument_list|)
expr_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|bootstrapDump
operator|.
name|dumpLocation
argument_list|)
expr_stmt|;
comment|// Create incremental data for incremental load with bootstrap of ACID
name|prepareIncNonAcidData
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
name|prepareIncAcidData
argument_list|(
name|primaryDbName
argument_list|)
expr_stmt|;
comment|// Perform concurrent writes. Bootstrap won't see the written data but the subsequent
comment|// incremental repl should see it. We can not inject callerVerifier since an incremental dump
comment|// would not cause an ALTER DATABASE event. Instead we piggy back on
comment|// getCurrentNotificationEventId() which is anyway required for a bootstrap.
name|BehaviourInjection
argument_list|<
name|CurrentNotificationEventId
argument_list|,
name|CurrentNotificationEventId
argument_list|>
name|callerInjectedBehavior
init|=
operator|new
name|BehaviourInjection
argument_list|<
name|CurrentNotificationEventId
argument_list|,
name|CurrentNotificationEventId
argument_list|>
argument_list|()
block|{
annotation|@
name|Nullable
annotation|@
name|Override
specifier|public
name|CurrentNotificationEventId
name|apply
parameter_list|(
annotation|@
name|Nullable
name|CurrentNotificationEventId
name|input
parameter_list|)
block|{
if|if
condition|(
name|injectionPathCalled
condition|)
block|{
name|nonInjectedPathCalled
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
comment|// Do some writes through concurrent thread
name|injectionPathCalled
operator|=
literal|true
expr_stmt|;
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Entered new thread"
argument_list|)
expr_stmt|;
try|try
block|{
name|prepareInc2NonAcidData
argument_list|(
name|primaryDbName
argument_list|,
name|primary
operator|.
name|hiveConf
argument_list|)
expr_stmt|;
name|prepareInc2AcidData
argument_list|(
name|primaryDbName
argument_list|,
name|primary
operator|.
name|hiveConf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|Assert
operator|.
name|assertNull
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Exit new thread success"
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Created new thread {}"
argument_list|,
name|t
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|input
return|;
block|}
block|}
decl_stmt|;
name|InjectableBehaviourObjectStore
operator|.
name|setGetCurrentNotificationEventIdBehaviour
argument_list|(
name|callerInjectedBehavior
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|incDump
init|=
literal|null
decl_stmt|;
try|try
block|{
name|incDump
operator|=
name|primary
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
name|bootstrapDump
operator|.
name|lastReplicationId
argument_list|,
name|dumpWithAcidBootstrapClause
argument_list|)
expr_stmt|;
name|callerInjectedBehavior
operator|.
name|assertInjectionsPerformed
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// reset the behaviour
name|InjectableBehaviourObjectStore
operator|.
name|resetGetCurrentNotificationEventIdBehaviour
argument_list|()
expr_stmt|;
block|}
comment|// While bootstrapping ACID tables it has taken snapshot before concurrent thread performed
comment|// write. So concurrent writes won't be dumped.
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": loading incremental dump containing bootstrapped ACID tables."
argument_list|)
expr_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|incDump
operator|.
name|dumpLocation
argument_list|)
expr_stmt|;
name|verifyIncLoad
argument_list|(
name|replicatedDbName
argument_list|,
name|incDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
comment|// Next Incremental should include the concurrent writes
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": dumping second normal incremental dump from event id = "
operator|+
name|incDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
name|WarehouseInstance
operator|.
name|Tuple
name|inc2Dump
init|=
name|primary
operator|.
name|dump
argument_list|(
name|primaryDbName
argument_list|,
name|incDump
operator|.
name|lastReplicationId
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|": loading second normal incremental dump from event id = "
operator|+
name|incDump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
name|replica
operator|.
name|load
argument_list|(
name|replicatedDbName
argument_list|,
name|inc2Dump
operator|.
name|dumpLocation
argument_list|)
expr_stmt|;
name|verifyInc2Load
argument_list|(
name|replicatedDbName
argument_list|,
name|inc2Dump
operator|.
name|lastReplicationId
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

