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
name|util
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
name|TxnCommandsBaseForTests
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

begin_class
specifier|public
class|class
name|TestUpgradeTool
extends|extends
name|TxnCommandsBaseForTests
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestUpgradeTool
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
name|TestUpgradeTool
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
name|Override
specifier|protected
name|String
name|getTestDataDir
parameter_list|()
block|{
return|return
name|TEST_DATA_DIR
return|;
block|}
comment|/**    * includes 'execute' for postUpgrade    */
annotation|@
name|Test
specifier|public
name|void
name|testPostUpgrade
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
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DYNAMICPARTITIONINGMODE
argument_list|,
literal|"dynamic"
argument_list|)
expr_stmt|;
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
comment|//should be converted to Acid
name|runStatementOnDriver
argument_list|(
literal|"create table TAcid (a int, b int) clustered by (b) into 2 buckets"
operator|+
literal|" stored as orc TBLPROPERTIES ('transactional'='false')"
argument_list|)
expr_stmt|;
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
literal|"insert into TAcid"
operator|+
name|makeValuesClause
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
comment|//should now be copy_1
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
comment|//should now be copy_2
comment|//should be converted to Acid
name|runStatementOnDriver
argument_list|(
literal|"create table TAcidPart (a int, b int) partitioned by (p int)"
operator|+
literal|" clustered by (b) into 2 buckets  stored as orc TBLPROPERTIES ('transactional'='false')"
argument_list|)
expr_stmt|;
comment|//to create some partitions
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
comment|//and copy_1 files
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
comment|//should be converted to Acid
comment|//todo add some files with non-standard names
name|runStatementOnDriver
argument_list|(
literal|"create table TFlat (a int, b int) stored as orc "
operator|+
literal|"tblproperties('transactional'='false')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"insert into TFlat values(1,2)"
argument_list|)
expr_stmt|;
comment|//create 0000_0
name|runStatementOnDriver
argument_list|(
literal|"insert into TFlat values(2,3)"
argument_list|)
expr_stmt|;
comment|//create 0000_0_copy_1
name|runStatementOnDriver
argument_list|(
literal|"insert into TFlat values(3,4)"
argument_list|)
expr_stmt|;
comment|//create 0000_0_copy_2
name|runStatementOnDriver
argument_list|(
literal|"insert into TFlat values(4,5)"
argument_list|)
expr_stmt|;
comment|//create 0000_0_copy_3
name|runStatementOnDriver
argument_list|(
literal|"insert into TFlat values(5,6)"
argument_list|)
expr_stmt|;
comment|//create 0000_0_copy_4
name|runStatementOnDriver
argument_list|(
literal|"insert into TFlat values(6,7)"
argument_list|)
expr_stmt|;
comment|//create 0000_0_copy_5
comment|/*      ├── 000000_0      ├── 000000_0_copy_1      ├── 000000_0_copy_2      ├── 000000_0_copy_3      └── 000000_0_copy_4      └── 000000_0_copy_5       to        ├── 000000_0       ├── 000000_0_copy_2       ├── 1       │   └── 000000_0       ├── 2       │   └── 000000_0       └── subdir       |  └── part-0001       |--.hive-staging_hive_2018-07-04_11-12-18_760_5286422535984490754-1395/000000_0  */
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
comment|//simulate Spark (non-Hive) write
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
literal|"/tflat/000000_0_copy_1"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|getWarehouseDir
argument_list|()
operator|+
literal|"/tflat/subdir/part-0001"
argument_list|)
argument_list|)
expr_stmt|;
comment|//simulate Insert ... Select ... Union All...
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
literal|"/tflat/000000_0_copy_3"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|getWarehouseDir
argument_list|()
operator|+
literal|"/tflat/1/000000_0"
argument_list|)
argument_list|)
expr_stmt|;
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
literal|"/tflat/000000_0_copy_4"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|getWarehouseDir
argument_list|()
operator|+
literal|"/tflat/2/000000_0"
argument_list|)
argument_list|)
expr_stmt|;
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
literal|"/tflat/000000_0_copy_5"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|getWarehouseDir
argument_list|()
operator|+
literal|"/tflat/.hive-staging_hive_2018-07-04_11-12-18_760_5286422535984490754-1395/000000_0"
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|testQuery0
init|=
literal|"select a, b from TFlat order by a"
decl_stmt|;
name|String
index|[]
index|[]
name|expected0
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"1\t2"
block|,
literal|""
block|}
block|,
block|{
literal|"2\t3"
block|,
literal|""
block|}
block|,
block|{
literal|"3\t4"
block|,
literal|""
block|}
block|,
block|{
literal|"4\t5"
block|,
literal|""
block|}
block|,
block|{
literal|"5\t6"
block|,
literal|""
block|}
block|,     }
decl_stmt|;
name|checkResult
argument_list|(
name|expected0
argument_list|,
name|testQuery0
argument_list|,
literal|true
argument_list|,
literal|"TFlat pre-check"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|//should be converted to MM
name|runStatementOnDriver
argument_list|(
literal|"create table TFlatText (a int, b int) stored as textfile "
operator|+
literal|"tblproperties('transactional'='false')"
argument_list|)
expr_stmt|;
name|Hive
name|db
init|=
name|Hive
operator|.
name|get
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
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
name|tacid
init|=
name|db
operator|.
name|getTable
argument_list|(
literal|"default"
argument_list|,
literal|"tacid"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected TAcid to not be full acid"
argument_list|,
literal|false
argument_list|,
name|AcidUtils
operator|.
name|isFullAcidTable
argument_list|(
name|tacid
argument_list|)
argument_list|)
expr_stmt|;
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
name|tacidpart
init|=
name|db
operator|.
name|getTable
argument_list|(
literal|"default"
argument_list|,
literal|"tacidpart"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected TAcidPart to not be full acid"
argument_list|,
literal|false
argument_list|,
name|AcidUtils
operator|.
name|isFullAcidTable
argument_list|(
name|tacidpart
argument_list|)
argument_list|)
expr_stmt|;
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
name|t
init|=
name|db
operator|.
name|getTable
argument_list|(
literal|"default"
argument_list|,
literal|"tflat"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected TAcid to not be full acid"
argument_list|,
literal|false
argument_list|,
name|AcidUtils
operator|.
name|isFullAcidTable
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|=
name|db
operator|.
name|getTable
argument_list|(
literal|"default"
argument_list|,
literal|"tflattext"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected TAcidPart to not be full acid"
argument_list|,
literal|false
argument_list|,
name|AcidUtils
operator|.
name|isInsertOnlyTable
argument_list|(
name|tacidpart
argument_list|)
argument_list|)
expr_stmt|;
name|String
index|[]
name|args2
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
name|UpgradeTool
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|UpgradeTool
operator|.
name|main
argument_list|(
name|args2
argument_list|)
expr_stmt|;
name|tacid
operator|=
name|db
operator|.
name|getTable
argument_list|(
literal|"default"
argument_list|,
literal|"tacid"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected TAcid to become full acid"
argument_list|,
literal|true
argument_list|,
name|AcidUtils
operator|.
name|isFullAcidTable
argument_list|(
name|tacid
argument_list|)
argument_list|)
expr_stmt|;
name|tacidpart
operator|=
name|db
operator|.
name|getTable
argument_list|(
literal|"default"
argument_list|,
literal|"tacidpart"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected TAcidPart to become full acid"
argument_list|,
literal|true
argument_list|,
name|AcidUtils
operator|.
name|isFullAcidTable
argument_list|(
name|tacidpart
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|=
name|db
operator|.
name|getTable
argument_list|(
literal|"default"
argument_list|,
literal|"tflat"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected TAcid to become acid"
argument_list|,
literal|true
argument_list|,
name|AcidUtils
operator|.
name|isFullAcidTable
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|=
name|db
operator|.
name|getTable
argument_list|(
literal|"default"
argument_list|,
literal|"tflattext"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected TAcidPart to become MM"
argument_list|,
literal|true
argument_list|,
name|AcidUtils
operator|.
name|isInsertOnlyTable
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
comment|/*make sure we still get the same data and row_ids are assigned and deltas are as expected:      * each set of copy_N goes into matching delta_N_N.*/
name|String
name|testQuery
init|=
literal|"select ROW__ID, a, b, INPUT__FILE__NAME from TAcid order by a, b, ROW__ID"
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
literal|"{\"writeid\":0,\"bucketid\":536870912,\"rowid\":0}\t1\t2"
block|,
literal|"tacid/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":0}\t1\t2"
block|,
literal|"tacid/delta_0000001_0000001/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":2,\"bucketid\":536870912,\"rowid\":0}\t1\t2"
block|,
literal|"tacid/delta_0000002_0000002/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":0,\"bucketid\":536936448,\"rowid\":0}\t3\t4"
block|,
literal|"tacid/000001_0"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536936448,\"rowid\":0}\t3\t4"
block|,
literal|"tacid/delta_0000001_0000001/000001_0"
block|}
block|,
block|{
literal|"{\"writeid\":2,\"bucketid\":536936448,\"rowid\":0}\t3\t4"
block|,
literal|"tacid/delta_0000002_0000002/000001_0"
block|}
block|,
block|{
literal|"{\"writeid\":0,\"bucketid\":536870912,\"rowid\":1}\t5\t6"
block|,
literal|"tacid/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":1}\t5\t6"
block|,
literal|"tacid/delta_0000001_0000001/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":2,\"bucketid\":536870912,\"rowid\":1}\t5\t6"
block|,
literal|"tacid/delta_0000002_0000002/000000_0"
block|}
block|}
decl_stmt|;
name|checkResult
argument_list|(
name|expected
argument_list|,
name|testQuery
argument_list|,
literal|false
argument_list|,
literal|"TAcid post-check"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|testQuery
operator|=
literal|"select ROW__ID, a, b, INPUT__FILE__NAME from TAcidPart order by a, b, p, ROW__ID"
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
literal|"{\"writeid\":0,\"bucketid\":536870912,\"rowid\":0}\t1\t2"
block|,
literal|"warehouse/tacidpart/p=10/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":0}\t1\t2"
block|,
literal|"tacidpart/p=10/delta_0000001_0000001/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":0,\"bucketid\":536936448,\"rowid\":0}\t3\t4"
block|,
literal|"tacidpart/p=11/000001_0"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536936448,\"rowid\":0}\t3\t4"
block|,
literal|"tacidpart/p=11/delta_0000001_0000001/000001_0"
block|}
block|,
block|{
literal|"{\"writeid\":0,\"bucketid\":536870912,\"rowid\":0}\t5\t6"
block|,
literal|"tacidpart/p=12/000000_0"
block|}
block|,
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":0}\t5\t6"
block|,
literal|"tacidpart/p=12/delta_0000001_0000001/000000_0"
block|}
block|}
decl_stmt|;
name|checkResult
argument_list|(
name|expected2
argument_list|,
name|testQuery
argument_list|,
literal|false
argument_list|,
literal|"TAcidPart post-check"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
comment|/* Verify that we re-arranged/renamed so that files names follow hive naming convention     and are spread among deltas/buckets     The order of files in RemoteIterator<LocatedFileStatus> iter = fs.listFiles(p, true)     is what determines which delta/file any original file ends up in      The test is split into 2 parts to test data and metadata because RemoteIterator walks in     different order on different machines*/
name|testQuery
operator|=
literal|"select a, b from TFlat order by a"
expr_stmt|;
name|String
index|[]
index|[]
name|expectedData
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"1\t2"
block|}
block|,
block|{
literal|"2\t3"
block|}
block|,
block|{
literal|"3\t4"
block|}
block|,
block|{
literal|"4\t5"
block|}
block|,
block|{
literal|"5\t6"
block|}
block|}
decl_stmt|;
name|checkResult
argument_list|(
name|expectedData
argument_list|,
name|testQuery
argument_list|,
literal|true
argument_list|,
literal|"TFlat post-check data"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
name|testQuery
operator|=
literal|"select ROW__ID, INPUT__FILE__NAME from TFlat order by INPUT__FILE__NAME"
expr_stmt|;
name|String
index|[]
index|[]
name|expectedMetaData
init|=
operator|new
name|String
index|[]
index|[]
block|{
block|{
literal|"{\"writeid\":1,\"bucketid\":536870912,\"rowid\":0}"
block|,
literal|"tflat/delta_0000001_0000001/00000_0"
block|}
block|,
block|{
literal|"{\"writeid\":2,\"bucketid\":536870912,\"rowid\":0}"
block|,
literal|"tflat/delta_0000002_0000002/00000_0"
block|}
block|,
block|{
literal|"{\"writeid\":3,\"bucketid\":536870912,\"rowid\":0}"
block|,
literal|"tflat/delta_0000003_0000003/00000_0"
block|}
block|,
block|{
literal|"{\"writeid\":4,\"bucketid\":536870912,\"rowid\":0}"
block|,
literal|"tflat/delta_0000004_0000004/00000_0"
block|}
block|,
block|{
literal|"{\"writeid\":5,\"bucketid\":536870912,\"rowid\":0}"
block|,
literal|"tflat/delta_0000005_0000005/00000_0"
block|}
block|}
decl_stmt|;
name|checkResult
argument_list|(
name|expectedMetaData
argument_list|,
name|testQuery
argument_list|,
literal|false
argument_list|,
literal|"TFlat post-check files"
argument_list|,
name|LOG
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGuessNumBuckets
parameter_list|()
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|UpgradeTool
operator|.
name|guessNumBuckets
argument_list|(
literal|123
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|UpgradeTool
operator|.
name|guessNumBuckets
argument_list|(
literal|30393930
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|UpgradeTool
operator|.
name|guessNumBuckets
argument_list|(
operator|(
name|long
operator|)
name|Math
operator|.
name|pow
argument_list|(
literal|10
argument_list|,
literal|9
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|32
argument_list|,
name|UpgradeTool
operator|.
name|guessNumBuckets
argument_list|(
operator|(
name|long
operator|)
name|Math
operator|.
name|pow
argument_list|(
literal|10
argument_list|,
literal|13
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|//10 TB
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|128
argument_list|,
name|UpgradeTool
operator|.
name|guessNumBuckets
argument_list|(
operator|(
name|long
operator|)
name|Math
operator|.
name|pow
argument_list|(
literal|10
argument_list|,
literal|15
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|//PB
block|}
block|}
end_class

end_unit

