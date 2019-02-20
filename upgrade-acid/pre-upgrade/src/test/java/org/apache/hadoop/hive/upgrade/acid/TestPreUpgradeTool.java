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
name|upgrade
operator|.
name|acid
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
name|fs
operator|.
name|FileUtil
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
name|common
operator|.
name|FileUtils
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
name|MetaException
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
name|ShowCompactRequest
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
name|ShowCompactResponse
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
name|ShowCompactResponseElement
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
name|QueryState
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
name|mr
operator|.
name|MapRedTask
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
name|io
operator|.
name|AcidUtils
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
name|io
operator|.
name|HiveInputFormat
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
name|Hive
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
name|HiveException
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
name|processors
operator|.
name|CommandProcessorResponse
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|txn
operator|.
name|compactor
operator|.
name|Worker
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
name|Test
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
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|attribute
operator|.
name|FileAttribute
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|attribute
operator|.
name|PosixFilePermission
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|attribute
operator|.
name|PosixFilePermissions
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_class
specifier|public
class|class
name|TestPreUpgradeTool
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TEST_DATA_DIR
init|=
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
name|TestPreUpgradeTool
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
operator|+
literal|"-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|getPath
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"\\\\"
argument_list|,
literal|"/"
argument_list|)
decl_stmt|;
specifier|private
name|String
name|getTestDataDir
parameter_list|()
block|{
return|return
name|TEST_DATA_DIR
return|;
block|}
comment|/**    * preUpgrade: test tables that need to be compacted, waits for compaction    * postUpgrade: generates scripts w/o asserts    */
annotation|@
name|Test
specifier|public
name|void
name|testUpgrade
parameter_list|()
throws|throws
name|Exception
block|{
name|int
index|[]
index|[]
name|data
init|=
block|{
block|{
literal|1
block|,
literal|2
block|}
block|,
block|{
literal|3
block|,
literal|4
block|}
block|,
block|{
literal|5
block|,
literal|6
block|}
block|}
decl_stmt|;
name|int
index|[]
index|[]
name|dataPart
init|=
block|{
block|{
literal|1
block|,
literal|2
block|,
literal|10
block|}
block|,
block|{
literal|3
block|,
literal|4
block|,
literal|11
block|}
block|,
block|{
literal|5
block|,
literal|6
block|,
literal|12
block|}
block|}
decl_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists TAcid"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists TAcidPart"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists TFlat"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists TFlatText"
argument_list|)
expr_stmt|;
try|try
block|{
name|runStatementOnDriver
argument_list|(
literal|"create table TAcid (a int, b int) clustered by (b) into 2 buckets stored as orc TBLPROPERTIES ('transactional'='true')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table TAcidPart (a int, b int) partitioned by (p tinyint)  clustered by (b) into 2 buckets  stored"
operator|+
literal|" as orc TBLPROPERTIES ('transactional'='true')"
argument_list|)
expr_stmt|;
comment|//on 2.x these are guaranteed to not be acid
name|runStatementOnDriver
argument_list|(
literal|"create table TFlat (a int, b int) stored as orc tblproperties('transactional'='false')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table TFlatText (a int, b int) stored as textfile tblproperties('transactional'='false')"
argument_list|)
expr_stmt|;
comment|//this needs major compaction
name|runStatementOnDriver
argument_list|(
literal|"insert into TAcid"
operator|+
name|makeValuesClause
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"update TAcid set a = 1 where b = 2"
argument_list|)
expr_stmt|;
comment|//this table needs to be converted to CRUD Acid
name|runStatementOnDriver
argument_list|(
literal|"insert into TFlat"
operator|+
name|makeValuesClause
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
comment|//this table needs to be converted to MM
name|runStatementOnDriver
argument_list|(
literal|"insert into TFlatText"
operator|+
name|makeValuesClause
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
comment|//p=10 needs major compaction
name|runStatementOnDriver
argument_list|(
literal|"insert into TAcidPart partition(p)"
operator|+
name|makeValuesClause
argument_list|(
name|dataPart
argument_list|)
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"update TAcidPart set a = 1 where b = 2 and p = 10"
argument_list|)
expr_stmt|;
comment|//todo: add partitioned table that needs conversion to MM/Acid
comment|//todo: rename files case
name|String
index|[]
name|args
init|=
block|{
literal|"-location"
block|,
name|getTestDataDir
argument_list|()
block|,
literal|"-execute"
block|}
decl_stmt|;
name|PreUpgradeTool
operator|.
name|callback
operator|=
operator|new
name|PreUpgradeTool
operator|.
name|Callback
argument_list|()
block|{
annotation|@
name|Override
name|void
name|onWaitForCompaction
parameter_list|()
throws|throws
name|MetaException
block|{
name|runWorker
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
name|PreUpgradeTool
operator|.
name|pollIntervalMs
operator|=
literal|1
expr_stmt|;
name|PreUpgradeTool
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|PreUpgradeTool
operator|.
name|main
argument_list|(
name|args
argument_list|)
expr_stmt|;
comment|/*     todo: parse     target/tmp/org.apache.hadoop.hive.upgrade.acid.TestPreUpgradeTool-1527286256834/compacts_1527286277624.sql     make sure it's the only 'compacts' file and contains     ALTER TABLE default.tacid COMPACT 'major'; ALTER TABLE default.tacidpart PARTITION(p=10Y) COMPACT 'major';     * */
name|TxnStore
name|txnHandler
init|=
name|TxnUtils
operator|.
name|getTxnStore
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|ShowCompactResponse
name|resp
init|=
name|txnHandler
operator|.
name|showCompact
argument_list|(
operator|new
name|ShowCompactRequest
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|resp
operator|.
name|getCompactsSize
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ShowCompactResponseElement
name|e
range|:
name|resp
operator|.
name|getCompacts
argument_list|()
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|TxnStore
operator|.
name|CLEANING_RESPONSE
argument_list|,
name|e
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|args2
init|=
block|{
literal|"-location"
block|,
name|getTestDataDir
argument_list|()
block|}
decl_stmt|;
name|PreUpgradeTool
operator|.
name|main
argument_list|(
name|args2
argument_list|)
expr_stmt|;
comment|/*        * todo: parse compacts script - make sure there is nothing in it        * */
block|}
finally|finally
block|{
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists TAcid"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists TAcidPart"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists TFlat"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists TFlatText"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpgradeExternalTableNoReadPermissionForDatabase
parameter_list|()
throws|throws
name|Exception
block|{
name|int
index|[]
index|[]
name|data
init|=
block|{
block|{
literal|1
block|,
literal|2
block|}
block|,
block|{
literal|3
block|,
literal|4
block|}
block|,
block|{
literal|5
block|,
literal|6
block|}
block|}
decl_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop database if exists test cascade"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists TExternal"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create database test"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table test.TExternal (a int, b int) stored as orc tblproperties"
operator|+
literal|"('transactional'='false')"
argument_list|)
expr_stmt|;
comment|//this needs major compaction
name|runStatementOnDriver
argument_list|(
literal|"insert into test.TExternal"
operator|+
name|makeValuesClause
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|dbDir
init|=
name|getWarehouseDir
argument_list|()
operator|+
literal|"/test.db"
decl_stmt|;
name|File
name|dbPath
init|=
operator|new
name|File
argument_list|(
name|dbDir
argument_list|)
decl_stmt|;
try|try
block|{
name|Set
argument_list|<
name|PosixFilePermission
argument_list|>
name|perms
init|=
name|PosixFilePermissions
operator|.
name|fromString
argument_list|(
literal|"-w-------"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|setPosixFilePermissions
argument_list|(
name|dbPath
operator|.
name|toPath
argument_list|()
argument_list|,
name|perms
argument_list|)
expr_stmt|;
name|String
index|[]
name|args
init|=
block|{
literal|"-location"
block|,
name|getTestDataDir
argument_list|()
block|,
literal|"-execute"
block|}
decl_stmt|;
name|PreUpgradeTool
operator|.
name|pollIntervalMs
operator|=
literal|1
expr_stmt|;
name|PreUpgradeTool
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|Exception
name|expected
init|=
literal|null
decl_stmt|;
try|try
block|{
name|PreUpgradeTool
operator|.
name|main
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|expected
operator|=
name|e
expr_stmt|;
block|}
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|expected
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|expected
operator|instanceof
name|HiveException
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Pre-upgrade tool requires "
operator|+
literal|"read-access to databases and tables to determine if a table has to be compacted."
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|Set
argument_list|<
name|PosixFilePermission
argument_list|>
name|perms
init|=
name|PosixFilePermissions
operator|.
name|fromString
argument_list|(
literal|"rwxrw----"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|setPosixFilePermissions
argument_list|(
name|dbPath
operator|.
name|toPath
argument_list|()
argument_list|,
name|perms
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpgradeExternalTableNoReadPermissionForTable
parameter_list|()
throws|throws
name|Exception
block|{
name|int
index|[]
index|[]
name|data
init|=
block|{
block|{
literal|1
block|,
literal|2
block|}
block|,
block|{
literal|3
block|,
literal|4
block|}
block|,
block|{
literal|5
block|,
literal|6
block|}
block|}
decl_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists TExternal"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table TExternal (a int, b int) stored as orc tblproperties('transactional'='false')"
argument_list|)
expr_stmt|;
comment|//this needs major compaction
name|runStatementOnDriver
argument_list|(
literal|"insert into TExternal"
operator|+
name|makeValuesClause
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|tableDir
init|=
name|getWarehouseDir
argument_list|()
operator|+
literal|"/texternal"
decl_stmt|;
name|File
name|tablePath
init|=
operator|new
name|File
argument_list|(
name|tableDir
argument_list|)
decl_stmt|;
try|try
block|{
name|Set
argument_list|<
name|PosixFilePermission
argument_list|>
name|perms
init|=
name|PosixFilePermissions
operator|.
name|fromString
argument_list|(
literal|"-w-------"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|setPosixFilePermissions
argument_list|(
name|tablePath
operator|.
name|toPath
argument_list|()
argument_list|,
name|perms
argument_list|)
expr_stmt|;
name|String
index|[]
name|args
init|=
block|{
literal|"-location"
block|,
name|getTestDataDir
argument_list|()
block|,
literal|"-execute"
block|}
decl_stmt|;
name|PreUpgradeTool
operator|.
name|pollIntervalMs
operator|=
literal|1
expr_stmt|;
name|PreUpgradeTool
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|Exception
name|expected
init|=
literal|null
decl_stmt|;
try|try
block|{
name|PreUpgradeTool
operator|.
name|main
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|expected
operator|=
name|e
expr_stmt|;
block|}
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|expected
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|expected
operator|instanceof
name|HiveException
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Pre-upgrade tool requires"
operator|+
literal|" read-access to databases and tables to determine if a table has to be compacted."
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|Set
argument_list|<
name|PosixFilePermission
argument_list|>
name|perms
init|=
name|PosixFilePermissions
operator|.
name|fromString
argument_list|(
literal|"rwxrw----"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|setPosixFilePermissions
argument_list|(
name|tablePath
operator|.
name|toPath
argument_list|()
argument_list|,
name|perms
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|runWorker
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|MetaException
block|{
name|AtomicBoolean
name|stop
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|Worker
name|t
init|=
operator|new
name|Worker
argument_list|()
decl_stmt|;
name|t
operator|.
name|setThreadId
argument_list|(
operator|(
name|int
operator|)
name|t
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|setHiveConf
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|AtomicBoolean
name|looped
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|t
operator|.
name|init
argument_list|(
name|stop
argument_list|,
name|looped
argument_list|)
expr_stmt|;
name|t
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
name|makeValuesClause
parameter_list|(
name|int
index|[]
index|[]
name|rows
parameter_list|)
block|{
assert|assert
name|rows
operator|.
name|length
operator|>
literal|0
assert|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|" values"
argument_list|)
decl_stmt|;
for|for
control|(
name|int
index|[]
name|row
range|:
name|rows
control|)
block|{
assert|assert
name|row
operator|.
name|length
operator|>
literal|0
assert|;
if|if
condition|(
name|row
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|value
range|:
name|row
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|value
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|setLength
argument_list|(
name|sb
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|//remove trailing comma
if|if
condition|(
name|row
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|setLength
argument_list|(
name|sb
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|//remove trailing comma
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|runStatementOnDriver
parameter_list|(
name|String
name|stmt
parameter_list|)
throws|throws
name|Exception
block|{
name|CommandProcessorResponse
name|cpr
init|=
name|d
operator|.
name|run
argument_list|(
name|stmt
argument_list|)
decl_stmt|;
if|if
condition|(
name|cpr
operator|.
name|getResponseCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|stmt
operator|+
literal|" failed: "
operator|+
name|cpr
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|rs
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|d
operator|.
name|getResults
argument_list|(
name|rs
argument_list|)
expr_stmt|;
return|return
name|rs
return|;
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
name|setUpInternal
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|initHiveConf
parameter_list|()
block|{
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
block|}
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|Driver
name|d
decl_stmt|;
specifier|private
name|void
name|setUpInternal
parameter_list|()
throws|throws
name|Exception
block|{
name|initHiveConf
argument_list|()
expr_stmt|;
name|TxnDbUtil
operator|.
name|cleanDb
argument_list|()
expr_stmt|;
comment|//todo: api changed in 3.0
name|FileUtils
operator|.
name|deleteDirectory
argument_list|(
operator|new
name|File
argument_list|(
name|getTestDataDir
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|workDir
init|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|,
literal|"target"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"test"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
literal|"mapred.local.dir"
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
literal|"mapred"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"local"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
literal|"mapred.system.dir"
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
literal|"mapred"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"system"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
literal|"mapreduce.jobtracker.staging.root.dir"
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
literal|"mapred"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"staging"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
literal|"mapred.temp.dir"
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
literal|"mapred"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"temp"
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
name|METASTOREWAREHOUSE
operator|.
name|varname
argument_list|,
name|getWarehouseDir
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
name|HIVEINPUTFORMAT
argument_list|,
name|HiveInputFormat
operator|.
name|class
operator|.
name|getName
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
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
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
name|METASTORE_PRE_EVENT_LISTENERS
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.AuthorizationPreEventListener"
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
name|HIVE_METASTORE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.StorageBasedAuthorizationProvider"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MERGE_CARDINALITY_VIOLATION_CHECK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESTATSCOLAUTOGATHER
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|TxnDbUtil
operator|.
name|setConfValues
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|TxnDbUtil
operator|.
name|prepDb
argument_list|()
expr_stmt|;
comment|//todo: api changed in 3.0
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|getWarehouseDir
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|f
operator|.
name|exists
argument_list|()
condition|)
block|{
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
operator|(
operator|new
name|File
argument_list|(
name|getWarehouseDir
argument_list|()
argument_list|)
operator|.
name|mkdirs
argument_list|()
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Could not create "
operator|+
name|getWarehouseDir
argument_list|()
argument_list|)
throw|;
block|}
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|start
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|ss
operator|.
name|applyAuthorizationPolicy
argument_list|()
expr_stmt|;
name|d
operator|=
operator|new
name|Driver
argument_list|(
operator|new
name|QueryState
argument_list|(
name|hiveConf
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|d
operator|.
name|setMaxRows
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|getWarehouseDir
parameter_list|()
block|{
return|return
name|getTestDataDir
argument_list|()
operator|+
literal|"/warehouse"
return|;
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
name|d
operator|!=
literal|null
condition|)
block|{
name|d
operator|.
name|close
argument_list|()
expr_stmt|;
name|d
operator|.
name|destroy
argument_list|()
expr_stmt|;
name|d
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

