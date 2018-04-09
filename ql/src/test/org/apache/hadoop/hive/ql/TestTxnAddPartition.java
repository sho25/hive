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
name|FileStatus
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
name|FileSystem
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
name|lockmgr
operator|.
name|TestDbTxnManager2
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
name|Ignore
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
name|TemporaryFolder
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
name|io
operator|.
name|File
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
comment|/**  * Tests related to support of ADD PARTITION with Acid/MM tables   * Most tests run in vectorized and non-vectorized mode since we currently have a vectorized and  * a non-vectorized acid readers and it's critical that ROW_IDs are generated the same way.  *  * Side Note:  Alter Table Add Partition does no validations on the data - not file name checks,  * not Input/OutputFormat, bucketing etc...  */
end_comment

begin_class
specifier|public
class|class
name|TestTxnAddPartition
extends|extends
name|TxnCommandsBaseForTests
block|{
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestTxnAddPartition
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestTxnLoadData
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
annotation|@
name|Rule
specifier|public
name|TemporaryFolder
name|folder
init|=
operator|new
name|TemporaryFolder
argument_list|()
decl_stmt|;
annotation|@
name|Override
name|String
name|getTestDataDir
parameter_list|()
block|{
return|return
name|TEST_DATA_DIR
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|addPartition
parameter_list|()
throws|throws
name|Exception
block|{
name|addPartition
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|addPartitionVectorized
parameter_list|()
throws|throws
name|Exception
block|{
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|addPartition
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests adding multiple partitions    * adding partition w/o location    * adding partition when it already exists    * adding partition when it already exists with "if not exists"    */
specifier|private
name|void
name|addPartition
parameter_list|(
name|boolean
name|isVectorized
parameter_list|)
throws|throws
name|Exception
block|{
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists T"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists Tstage"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table T (a int, b int) partitioned by (p int) stored as orc"
operator|+
literal|" tblproperties('transactional'='true')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table Tstage (a int, b int) stored as orc"
operator|+
literal|" tblproperties('transactional'='false')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into Tstage values(0,2),(0,4)"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"export table Tstage to '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/1'"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"export table Tstage to '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/2'"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"ALTER TABLE T ADD"
operator|+
literal|" PARTITION (p=0) location '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/1/data'"
operator|+
literal|" PARTITION (p=1) location '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/2/data'"
operator|+
literal|" PARTITION (p=2)"
argument_list|)
expr_stmt|;
name|String
name|testQuery
init|=
name|isVectorized
condition|?
literal|"select ROW__ID, p, a, b from T order by p, ROW__ID"
else|:
literal|"select ROW__ID, p, a, b, INPUT__FILE__NAME from T order by p, ROW__ID"
decl_stmt|;
name|String
index|[]
index|[]
name|expected
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":0}\t0\t0\t2"
block|,
literal|"warehouse/t/p=0/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":1}\t0\t0\t4"
block|,
literal|"warehouse/t/p=0/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":0}\t1\t0\t2"
block|,
literal|"warehouse/t/p=1/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":1}\t1\t0\t4"
block|,
literal|"warehouse/t/p=1/delta_0000001_0000001_0000/000000_0"
block|}
block|}
decl_stmt|;
name|checkResult
argument_list|(
name|expected
argument_list|,
name|testQuery
argument_list|,
name|isVectorized
argument_list|,
literal|"add 2 parts w/data and 1 empty"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"export table Tstage to '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/3'"
argument_list|)
expr_stmt|;
comment|//should be an error since p=3 exists
name|CommandProcessorResponse
name|cpr
init|=
name|runStatementOnDriverNegative
argument_list|(
literal|"ALTER TABLE T ADD PARTITION (p=0) location '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/3/data'"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"add existing partition"
argument_list|,
name|cpr
operator|.
name|getErrorMessage
argument_list|()
operator|!=
literal|null
operator|&&
name|cpr
operator|.
name|getErrorMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Partition already exists"
argument_list|)
argument_list|)
expr_stmt|;
comment|//should be no-op since p=3 exists
name|String
name|stmt
init|=
literal|"ALTER TABLE T ADD IF NOT EXISTS "
operator|+
literal|"PARTITION (p=0) location '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/3/data' "
comment|//p=0 exists and is not empty
operator|+
literal|"PARTITION (p=2) location '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/3/data'"
comment|//p=2 exists and is empty
operator|+
literal|"PARTITION (p=3) location '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/3/data'"
decl_stmt|;
comment|//p=3 doesn't exist
name|runStatementOnDriver
argument_list|(
name|stmt
argument_list|)
expr_stmt|;
name|String
index|[]
index|[]
name|expected2
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":0}\t0\t0\t2"
block|,
literal|"warehouse/t/p=0/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":1}\t0\t0\t4"
block|,
literal|"warehouse/t/p=0/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":0}\t1\t0\t2"
block|,
literal|"warehouse/t/p=1/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":1}\t1\t0\t4"
block|,
literal|"warehouse/t/p=1/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":3,\"bucketid\":536870912,\"rowid\":0}\t3\t0\t2"
block|,
literal|"warehouse/t/p=3/delta_0000003_0000003_0000/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":3,\"bucketid\":536870912,\"rowid\":1}\t3\t0\t4"
block|,
literal|"warehouse/t/p=3/delta_0000003_0000003_0000/000000_0"
block|}
block|}
decl_stmt|;
name|checkResult
argument_list|(
name|expected2
argument_list|,
name|testQuery
argument_list|,
name|isVectorized
argument_list|,
literal|"add 2 existing parts and 1 empty"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|addPartitionMM
parameter_list|()
throws|throws
name|Exception
block|{
name|addPartitionMM
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|addPartitionMMVectorized
parameter_list|()
throws|throws
name|Exception
block|{
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|addPartitionMM
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Micro managed table test    * Tests adding multiple partitions    * adding partition w/o location    * adding partition when it already exists    * adding partition when it already exists with "if not exists"    */
specifier|private
name|void
name|addPartitionMM
parameter_list|(
name|boolean
name|isVectorized
parameter_list|)
throws|throws
name|Exception
block|{
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists T"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists Tstage"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table T (a int, b int) partitioned by (p int) stored as orc"
operator|+
literal|" tblproperties('transactional'='true', 'transactional_properties'='insert_only')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table Tstage (a int, b int) stored as orc"
operator|+
literal|" tblproperties('transactional'='false')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into Tstage values(0,2),(0,4)"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"export table Tstage to '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/1'"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"export table Tstage to '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/2'"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"ALTER TABLE T ADD"
operator|+
literal|" PARTITION (p=0) location '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/1/data'"
operator|+
literal|" PARTITION (p=1) location '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/2/data'"
operator|+
literal|" PARTITION (p=2)"
argument_list|)
expr_stmt|;
name|String
name|testQuery
init|=
name|isVectorized
condition|?
literal|"select p, a, b from T order by p, a, b"
else|:
literal|"select p, a, b, INPUT__FILE__NAME from T order by p, a, b"
decl_stmt|;
name|String
index|[]
index|[]
name|expected
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"0\t0\t2"
block|,
literal|"warehouse/t/p=0/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"0\t0\t4"
block|,
literal|"warehouse/t/p=0/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"1\t0\t2"
block|,
literal|"warehouse/t/p=1/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"1\t0\t4"
block|,
literal|"warehouse/t/p=1/delta_0000001_0000001_0000/000000_0"
block|}
block|}
decl_stmt|;
name|checkResult
argument_list|(
name|expected
argument_list|,
name|testQuery
argument_list|,
name|isVectorized
argument_list|,
literal|"add 2 parts w/data and 1 empty"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"export table Tstage to '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/3'"
argument_list|)
expr_stmt|;
comment|//should be an error since p=3 exists
name|CommandProcessorResponse
name|cpr
init|=
name|runStatementOnDriverNegative
argument_list|(
literal|"ALTER TABLE T ADD PARTITION (p=0) location '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/3/data'"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"add existing partition"
argument_list|,
name|cpr
operator|.
name|getErrorMessage
argument_list|()
operator|!=
literal|null
operator|&&
name|cpr
operator|.
name|getErrorMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Partition already exists"
argument_list|)
argument_list|)
expr_stmt|;
comment|//should be no-op since p=3 exists
name|runStatementOnDriver
argument_list|(
literal|"ALTER TABLE T ADD IF NOT EXISTS "
operator|+
literal|"PARTITION (p=0) location '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/3/data' "
comment|//p=0 exists and is not empty
operator|+
literal|"PARTITION (p=2) location '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/3/data'"
comment|//p=2 exists and is empty
operator|+
literal|"PARTITION (p=3) location '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/3/data'"
argument_list|)
expr_stmt|;
comment|//p=3 doesn't exist
name|String
index|[]
index|[]
name|expected2
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"0\t0\t2"
block|,
literal|"warehouse/t/p=0/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"0\t0\t4"
block|,
literal|"warehouse/t/p=0/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"1\t0\t2"
block|,
literal|"warehouse/t/p=1/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"1\t0\t4"
block|,
literal|"warehouse/t/p=1/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"3\t0\t2"
block|,
literal|"warehouse/t/p=3/delta_0000003_0000003_0000/000000_0"
block|}
block|,
block|{
literal|"3\t0\t4"
block|,
literal|"warehouse/t/p=3/delta_0000003_0000003_0000/000000_0"
block|}
block|}
decl_stmt|;
name|checkResult
argument_list|(
name|expected2
argument_list|,
name|testQuery
argument_list|,
name|isVectorized
argument_list|,
literal|"add 2 existing parts and 1 empty"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|addPartitionBucketed
parameter_list|()
throws|throws
name|Exception
block|{
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists T"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists Tstage"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table T (a int, b int) partitioned by (p int) "
operator|+
literal|"clustered by (a) into 2 buckets stored as orc tblproperties('transactional'='true')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table Tstage (a int, b int)  clustered by (a) into 2 "
operator|+
literal|"buckets stored as orc tblproperties('transactional'='false')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into Tstage values(0,2),(1,4)"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"export table Tstage to '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/1'"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"ALTER TABLE T ADD PARTITION (p=0) location '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/1/data'"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rs
init|=
name|runStatementOnDriver
argument_list|(
literal|"select ROW__ID, p, a, b, INPUT__FILE__NAME from T order by p, ROW__ID"
argument_list|)
decl_stmt|;
name|String
index|[]
index|[]
name|expected
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":0}\t0\t0\t2"
block|,
literal|"warehouse/t/p=0/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536936448,\"rowid\":0}\t0\t1\t4"
block|,
literal|"warehouse/t/p=0/delta_0000001_0000001_0000/000001_0"
block|}
block|}
decl_stmt|;
name|checkExpected
argument_list|(
name|rs
argument_list|,
name|expected
argument_list|,
literal|"add partition (p=0)"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkExpected
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|rs
parameter_list|,
name|String
index|[]
index|[]
name|expected
parameter_list|,
name|String
name|msg
parameter_list|)
block|{
name|super
operator|.
name|checkExpected
argument_list|(
name|rs
argument_list|,
name|expected
argument_list|,
name|msg
argument_list|,
name|LOG
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check to make sure that if files being loaded don't have standard Hive names, that they are    * renamed during add.    */
annotation|@
name|Test
specifier|public
name|void
name|addPartitionReaname
parameter_list|()
throws|throws
name|Exception
block|{
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists T"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"drop table if exists Tstage"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"create table T (a int, b int) partitioned by (p int) "
operator|+
literal|"stored as orc tblproperties('transactional'='true')"
argument_list|)
expr_stmt|;
comment|//bucketed just so that we get 2 files
name|runStatementOnDriver
argument_list|(
literal|"create table Tstage (a int, b int)  clustered by (a) into 2 "
operator|+
literal|"buckets stored as orc tblproperties('transactional'='false')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into Tstage values(0,2),(1,4)"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"export table Tstage to '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/1'"
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|status
init|=
name|fs
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|getWarehouseDir
argument_list|()
operator|+
literal|"/1/data"
argument_list|)
argument_list|,
name|AcidUtils
operator|.
name|originalBucketFilter
argument_list|)
decl_stmt|;
name|boolean
name|b
init|=
name|fs
operator|.
name|rename
argument_list|(
operator|new
name|Path
argument_list|(
name|getWarehouseDir
argument_list|()
operator|+
literal|"/1/data/000000_0"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|getWarehouseDir
argument_list|()
operator|+
literal|"/1/data/part-m000"
argument_list|)
argument_list|)
decl_stmt|;
name|b
operator|=
name|fs
operator|.
name|rename
argument_list|(
operator|new
name|Path
argument_list|(
name|getWarehouseDir
argument_list|()
operator|+
literal|"/1/data/000001_0"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|getWarehouseDir
argument_list|()
operator|+
literal|"/1/data/part-m001"
argument_list|)
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"ALTER TABLE T ADD PARTITION (p=0) location '"
operator|+
name|getWarehouseDir
argument_list|()
operator|+
literal|"/1/data'"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rs
init|=
name|runStatementOnDriver
argument_list|(
literal|"select ROW__ID, p, a, b, INPUT__FILE__NAME from T order by p, ROW__ID"
argument_list|)
decl_stmt|;
name|String
index|[]
index|[]
name|expected
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":0}\t0\t0\t2"
block|,
literal|"warehouse/t/p=0/delta_0000001_0000001_0000/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536936448,\"rowid\":0}\t0\t1\t4"
block|,
literal|"warehouse/t/p=0/delta_0000001_0000001_0000/000001_0"
block|}
block|}
decl_stmt|;
name|checkExpected
argument_list|(
name|rs
argument_list|,
name|expected
argument_list|,
literal|"add partition (p=0)"
argument_list|)
expr_stmt|;
block|}
comment|/**    * {@link TestDbTxnManager2#testAddPartitionLocks}    */
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testLocks
parameter_list|()
throws|throws
name|Exception
block|{   }
block|}
end_class

end_unit

